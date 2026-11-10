package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.data.remote.supabase.SupabaseConfig
import com.example.data.remote.supabase.SupabaseSyncManager
import com.example.data.repository.HealthRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HealthRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HealthRepository(db.childStudentDao(), db.medicalRecordDao())
    }

    val students: StateFlow<List<ChildStudent>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<MedicalRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePlans: StateFlow<List<MedicalRecord>> = repository.activePlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedRecords: StateFlow<List<MedicalRecord>> = repository.completedRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingControlRecords: StateFlow<List<MedicalRecord>> = repository.pendingControlRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Wali Name (Default "Sari Devi" from mockup)
    private val _waliName = MutableStateFlow("Sari Devi")
    val waliName: StateFlow<String> = _waliName.asStateFlow()

    fun setWaliName(name: String) {
        if (name.isNotBlank()) {
            _waliName.value = name.trim()
        }
    }

    // UI state filters
    private val _selectedFaskesFilter = MutableStateFlow("Semua")
    val selectedFaskesFilter: StateFlow<String> = _selectedFaskesFilter.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    fun setFaskesFilter(filter: String) {
        _selectedFaskesFilter.value = filter
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun setStudentSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    // Filtered Completed Records (Pasca Puskesmas / Rekam Medis)
    val filteredCompletedRecords: StateFlow<List<MedicalRecord>> = combine(
        completedRecords,
        _selectedFaskesFilter,
        _historySearchQuery
    ) { records, faskes, query ->
        records.filter { record ->
            val matchFaskes = if (faskes == "Semua") true else record.faskes.contains(faskes, ignoreCase = true)
            val matchQuery = if (query.isBlank()) true else {
                record.studentName.contains(query, ignoreCase = true) ||
                        record.diagnosis.contains(query, ignoreCase = true) ||
                        record.prescription.contains(query, ignoreCase = true) ||
                        record.complaint.contains(query, ignoreCase = true) ||
                        record.actionsTaken.contains(query, ignoreCase = true)
            }
            matchFaskes && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Create Treatment Plan
    fun createTreatmentPlan(
        student: ChildStudent,
        faskes: String,
        complaint: String,
        planDate: Long,
        departureTime: String = "08:30",
        escortName: String = _waliName.value,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val record = MedicalRecord(
                studentId = student.id,
                studentName = student.name,
                studentDormitory = student.dormitory,
                faskes = faskes,
                complaint = complaint,
                isDirectTreatment = false,
                planDate = planDate,
                departureTime = departureTime,
                status = "PLANNED",
                escortName = if (escortName.isBlank()) _waliName.value else escortName,
                notes = notes
            )
            val newId = repository.insertRecord(record)
            val recordWithId = record.copy(id = newId)
            // Kirim notifikasi resmi sistem Android
            NotificationHelper.showSickStudentNotification(
                context = getApplication(),
                studentName = student.name,
                className = student.classGrade,
                roomNumber = student.roomNumber.ifBlank { student.dormitory },
                complaint = complaint,
                faskes = faskes,
                notificationId = (newId % 100000).toInt()
            )
            // Sinkronisasi latar belakang ke Supabase Cloud jika terkonfigurasi
            SupabaseSyncManager.uploadSingleRecord(getApplication(), recordWithId)
        }
    }

    // Mark departed (Dianter / Siap Jalan)
    fun markDeparted(recordId: Long) {
        viewModelScope.launch {
            repository.updateStatus(recordId, "DEPARTED")
        }
    }

    // Update custom status
    fun updateStatus(recordId: Long, status: String) {
        viewModelScope.launch {
            repository.updateStatus(recordId, status)
        }
    }

    // Submit Treatment Result (Rekam medis nakes/puskesmas)
    fun submitTreatmentResult(
        record: MedicalRecord,
        diagnosis: String,
        actionsTaken: String,
        prescription: String,
        needsControl: Boolean,
        controlDate: Long?,
        additionalNotes: String
    ) {
        viewModelScope.launch {
            val updated = record.copy(
                status = "COMPLETED",
                treatmentDate = System.currentTimeMillis(),
                diagnosis = diagnosis,
                actionsTaken = actionsTaken,
                prescription = prescription,
                needsControl = needsControl,
                controlDate = if (needsControl) controlDate else null,
                isControlCompleted = false,
                notes = if (additionalNotes.isNotBlank()) "${record.notes}\n$additionalNotes".trim() else record.notes
            )
            repository.updateRecord(updated)
            // Sinkronisasi latar belakang ke Supabase Cloud
            SupabaseSyncManager.uploadSingleRecord(getApplication(), updated)
        }
    }

    // Direct Treatment
    fun recordDirectTreatment(
        student: ChildStudent,
        faskes: String,
        complaint: String,
        diagnosis: String,
        actionsTaken: String,
        prescription: String,
        needsControl: Boolean,
        controlDate: Long?,
        escortName: String = _waliName.value,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val record = MedicalRecord(
                studentId = student.id,
                studentName = student.name,
                studentDormitory = student.dormitory,
                faskes = faskes,
                complaint = complaint,
                isDirectTreatment = true,
                planDate = now,
                departureTime = "09:00",
                status = "COMPLETED",
                treatmentDate = now,
                diagnosis = diagnosis,
                actionsTaken = actionsTaken,
                prescription = prescription,
                needsControl = needsControl,
                controlDate = if (needsControl) controlDate else null,
                isControlCompleted = false,
                escortName = if (escortName.isBlank()) _waliName.value else escortName,
                notes = notes
            )
            val newId = repository.insertRecord(record)
            val recordWithId = record.copy(id = newId)
            // Kirim notifikasi resmi sistem Android
            NotificationHelper.showSickStudentNotification(
                context = getApplication(),
                studentName = student.name,
                className = student.classGrade,
                roomNumber = student.roomNumber.ifBlank { student.dormitory },
                complaint = complaint.ifBlank { diagnosis },
                faskes = faskes,
                notificationId = (newId % 100000).toInt()
            )
            // Sinkronisasi latar belakang ke Supabase Cloud
            SupabaseSyncManager.uploadSingleRecord(getApplication(), recordWithId)
        }
    }

    // Kirim notifikasi tes untuk memverifikasi fitur di perangkat pengguna
    fun sendTestNotification() {
        NotificationHelper.showTestNotification(getApplication())
    }

    // Mark Control Visit as completed
    fun markControlCompleted(recordId: Long) {
        viewModelScope.launch {
            repository.updateControlCompleted(recordId, true)
        }
    }

    // Delete record
    fun deleteRecord(record: MedicalRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            // Hapus dari Supabase jika terhubung
            SupabaseSyncManager.deleteRemoteRecord(getApplication(), record.id)
        }
    }

    // Add student
    fun addStudent(
        name: String,
        nis: String,
        gender: String,
        age: Int,
        dormitory: String,
        classGrade: String,
        allergies: String,
        bloodType: String,
        emergencyContact: String,
        specialNotes: String
    ) {
        viewModelScope.launch {
            val student = ChildStudent(
                name = name.trim(),
                nis = nis.trim(),
                gender = gender,
                age = age,
                dormitory = dormitory.trim(),
                classGrade = classGrade.trim(),
                allergies = if (allergies.isBlank()) "Tidak ada" else allergies.trim(),
                bloodType = if (bloodType.isBlank()) "-" else bloodType.trim(),
                emergencyContact = emergencyContact.trim(),
                specialNotes = specialNotes.trim()
            )
            repository.insertStudent(student)
        }
    }

    fun deleteStudent(student: ChildStudent) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    // ==========================================
    // SUPABASE CLOUD DATABASE SYNC & MANAGEMENT
    // ==========================================
    private val _isSupabaseConfigured = MutableStateFlow(SupabaseConfig.isConfigured(application))
    val isSupabaseConfigured: StateFlow<Boolean> = _isSupabaseConfigured.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _supabaseSyncMessage = MutableStateFlow<String?>(null)
    val supabaseSyncMessage: StateFlow<String?> = _supabaseSyncMessage.asStateFlow()

    fun refreshSupabaseConfig() {
        _isSupabaseConfigured.value = SupabaseConfig.isConfigured(getApplication())
    }

    fun testSupabaseConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = SupabaseSyncManager.testConnection(getApplication())
            _isSyncing.value = false
            refreshSupabaseConfig()
            if (result.isSuccess) {
                val msg = result.getOrNull() ?: "Koneksi Supabase Berhasil!"
                _supabaseSyncMessage.value = msg
                onResult(true, msg)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Gagal terhubung ke Supabase."
                _supabaseSyncMessage.value = err
                onResult(false, err)
            }
        }
    }

    fun syncWithSupabase(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            val currentRecords = allRecords.value
            val result = SupabaseSyncManager.syncMedicalRecords(
                context = getApplication(),
                localRecords = currentRecords,
                onRemoteRecordsFetched = { remoteRecords ->
                    if (remoteRecords.isNotEmpty()) {
                        repository.insertAllRecords(remoteRecords)
                    }
                }
            )
            _isSyncing.value = false
            refreshSupabaseConfig()
            if (result.isSuccess) {
                val msg = result.getOrNull() ?: "Sinkronisasi Berhasil!"
                _supabaseSyncMessage.value = msg
                onResult(true, msg)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Gagal sinkronisasi dengan Supabase."
                _supabaseSyncMessage.value = err
                onResult(false, err)
            }
        }
    }

    fun uploadAllStudentsToSupabase(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            val studentList = students.value
            val result = SupabaseSyncManager.uploadStudents(getApplication(), studentList)
            _isSyncing.value = false
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                val msg = "Berhasil mengunggah $count data siswa ke Supabase!"
                _supabaseSyncMessage.value = msg
                onResult(true, msg)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Gagal upload data siswa ke Supabase."
                _supabaseSyncMessage.value = err
                onResult(false, err)
            }
        }
    }
}
