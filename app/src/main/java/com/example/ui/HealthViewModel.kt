package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolProfile
import com.example.data.model.SchoolRegistration
import com.example.data.model.WaliAccount
import com.example.data.remote.supabase.SupabaseConfig
import com.example.data.remote.supabase.SupabaseSyncManager
import com.example.data.repository.HealthRepository
import com.example.util.NotificationHelper
import com.example.util.PasswordGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UserRole {
    NOT_LOGGED_IN,
    WALI,
    ADMIN,
    SUPERADMIN
}

sealed class LoginResult {
    object SuccessSuperAdmin : LoginResult()
    object SuccessAdmin : LoginResult()
    data class SuccessWali(val account: WaliAccount) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HealthRepository
    private val authPrefs = application.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HealthRepository(
            db.childStudentDao(),
            db.medicalRecordDao(),
            db.waliAccountDao(),
            db.schoolRegistrationDao()
        )
    }

    val schoolRegistrations: StateFlow<List<SchoolRegistration>> = repository.allSchoolRegistrations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    val waliAccounts: StateFlow<List<WaliAccount>> = repository.allWaliAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // School Profile & Tenant Customization
    private val schoolPrefs = application.getSharedPreferences("waliasuh_school_prefs", Context.MODE_PRIVATE)
    private val _schoolProfile = MutableStateFlow(
        SchoolProfile(
            schoolName = schoolPrefs.getString("school_name", "Sekolah Rakyat SRT 1 Kabupaten Kediri") ?: "Sekolah Rakyat SRT 1 Kabupaten Kediri",
            shortName = schoolPrefs.getString("short_name", "SRT 1 Kediri") ?: "SRT 1 Kediri",
            adminName = schoolPrefs.getString("admin_name", "Admin Utama") ?: "Admin Utama",
            city = schoolPrefs.getString("city", "Kabupaten Kediri") ?: "Kabupaten Kediri",
            tenantCode = schoolPrefs.getString("tenant_code", "SRT1-KDR") ?: "SRT1-KDR",
            primaryFaskesName = schoolPrefs.getString("primary_faskes", "Puskesmas Wates") ?: "Puskesmas Wates",
            secondaryFaskesName = schoolPrefs.getString("secondary_faskes", "Puskesmas Plosoklaten") ?: "Puskesmas Plosoklaten"
        )
    )
    val schoolProfile: StateFlow<SchoolProfile> = _schoolProfile.asStateFlow()

    fun updateSchoolProfile(profile: SchoolProfile, onComplete: () -> Unit = {}) {
        _schoolProfile.value = profile
        schoolPrefs.edit()
            .putString("school_name", profile.schoolName)
            .putString("short_name", profile.shortName)
            .putString("admin_name", profile.adminName)
            .putString("city", profile.city)
            .putString("tenant_code", profile.tenantCode)
            .putString("primary_faskes", profile.primaryFaskesName)
            .putString("secondary_faskes", profile.secondaryFaskesName)
            .apply()
        onComplete()
    }

    fun saveImportedStudents(
        studentList: List<ChildStudent>,
        replace: Boolean,
        schoolId: String? = null,
        onComplete: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val targetSchoolId = schoolId?.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
                ?: _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            if (replace) {
                repository.deleteStudentsBySchool(targetSchoolId)
            }
            val stampedStudents = studentList.map { student ->
                student.copy(schoolId = targetSchoolId)
            }
            repository.insertAllStudents(stampedStudents)
            onComplete(stampedStudents.size)
        }
    }

    fun loadDefaultKediriStudents(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val defaultStudents = AppDatabase.loadStudentsFromAssets(getApplication())
            if (defaultStudents.isNotEmpty()) {
                repository.deleteStudentsBySchool("SRT1-KDR")
                val stamped = defaultStudents.map { it.copy(schoolId = "SRT1-KDR") }
                repository.insertAllStudents(stamped)
            }
            onComplete(defaultStudents.size)
        }
    }

    fun saveImportedWaliAccounts(
        accountList: List<WaliAccount>,
        replace: Boolean,
        schoolId: String? = null,
        onComplete: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val targetSchoolId = schoolId?.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
                ?: _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            if (replace) {
                repository.deleteWaliAccountsBySchool(targetSchoolId)
            }
            val stampedAccounts = accountList.map { acc ->
                acc.copy(schoolId = targetSchoolId)
            }
            repository.insertAllWaliAccounts(stampedAccounts)
            onComplete(stampedAccounts.size)
        }
    }

    // Active User Role & Session
    private val _currentUserRole = MutableStateFlow(
        when (authPrefs.getString("saved_role", "NOT_LOGGED_IN")) {
            "SUPERADMIN" -> UserRole.SUPERADMIN
            "ADMIN" -> UserRole.ADMIN
            "WALI" -> UserRole.WALI
            else -> UserRole.NOT_LOGGED_IN
        }
    )
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    // Active Wali Name (Loaded from preferences or active login)
    private val _waliName = MutableStateFlow(
        authPrefs.getString("saved_wali_name", null)?.takeIf { it.isNotBlank() } ?: "Wali Asuh"
    )
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
                specialNotes = specialNotes.trim(),
                schoolId = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
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

    // --- Authentication & Role Management ---

    fun login(
        username: String,
        password: String,
        rememberMe: Boolean,
        onResult: (LoginResult) -> Unit
    ) {
        val cleanUser = username.trim()
        val cleanPass = password.trim()

        if (cleanUser.isEmpty() || cleanPass.isEmpty()) {
            onResult(LoginResult.Error("Username dan password wajib diisi."))
            return
        }

        // 0. Cek Kredensial Superadmin Resmi
        if (cleanUser.equals("superadmin", ignoreCase = true) && cleanPass == "Woyowoyo12@") {
            _currentUserRole.value = UserRole.SUPERADMIN
            if (rememberMe) {
                authPrefs.edit()
                    .putString("saved_role", "SUPERADMIN")
                    .putString("saved_username", "superadmin")
                    .apply()
            }
            onResult(LoginResult.SuccessSuperAdmin)
            return
        }

        // 1. Cek Kredensial Administrator Resmi
        if (cleanUser.equals("ekowahyudiadmin", ignoreCase = true) && cleanPass == "Woyowoyo12@") {
            _currentUserRole.value = UserRole.ADMIN
            if (rememberMe) {
                authPrefs.edit()
                    .putString("saved_role", "ADMIN")
                    .putString("saved_username", "ekowahyudiadmin")
                    .apply()
            }
            onResult(LoginResult.SuccessAdmin)
            return
        }

        // 2. Cek Akun Admin Sekolah Terdaftar / Baru
        val matchedApprovedSchool = schoolRegistrations.value.firstOrNull {
            it.status == "APPROVED" &&
            it.generatedAdminUsername.equals(cleanUser, ignoreCase = true) &&
            it.generatedAdminPassword == cleanPass
        }
        if (matchedApprovedSchool != null) {
            _currentUserRole.value = UserRole.ADMIN
            // Update schoolProfile to the matched approved school
            updateSchoolProfile(
                SchoolProfile(
                    schoolName = matchedApprovedSchool.schoolName,
                    shortName = matchedApprovedSchool.schoolName.take(15),
                    adminName = matchedApprovedSchool.picName,
                    city = matchedApprovedSchool.city,
                    tenantCode = matchedApprovedSchool.generatedSchoolCode
                )
            )
            if (rememberMe) {
                authPrefs.edit()
                    .putString("saved_role", "ADMIN")
                    .putString("saved_username", matchedApprovedSchool.generatedAdminUsername)
                    .apply()
            }
            onResult(LoginResult.SuccessAdmin)
            return
        }

        // 3. Cek Akun Wali Asuh
        viewModelScope.launch {
            val account = repository.getWaliAccountByUsername(cleanUser)
            if (account != null && account.password == cleanPass) {
                _currentUserRole.value = UserRole.WALI
                _waliName.value = account.name
                if (rememberMe) {
                    authPrefs.edit()
                        .putString("saved_role", "WALI")
                        .putString("saved_wali_name", account.name)
                        .putString("saved_username", account.username)
                        .apply()
                }
                onResult(LoginResult.SuccessWali(account))
            } else {
                onResult(LoginResult.Error("Username atau password salah. Silakan periksa kembali."))
            }
        }
    }

    // --- Manajemen Pendaftaran Sekolah (Superadmin & Publik) ---

    fun submitSchoolRegistration(
        schoolName: String,
        address: String,
        city: String,
        estimatedWaliCount: Int,
        picName: String,
        picEmail: String,
        picPhone: String,
        onComplete: (SchoolRegistration) -> Unit = {}
    ) {
        viewModelScope.launch {
            val reg = SchoolRegistration(
                schoolName = schoolName.trim(),
                address = address.trim(),
                city = city.trim(),
                estimatedWaliCount = estimatedWaliCount,
                picName = picName.trim(),
                picEmail = picEmail.trim(),
                picPhone = picPhone.trim(),
                submissionDate = System.currentTimeMillis(),
                status = "PENDING"
            )
            val newId = repository.insertSchoolRegistration(reg)
            onComplete(reg.copy(id = newId))
        }
    }

    fun approveSchoolRegistration(
        registration: SchoolRegistration,
        onApproved: (SchoolRegistration) -> Unit = {}
    ) {
        viewModelScope.launch {
            val cleanSchool = registration.schoolName.trim().lowercase().replace(" ", "").filter { it.isLetterOrDigit() }.take(10)
            val cityAbbr = registration.city.trim().replace("Kota", "").replace("Kabupaten", "").replace("Kab.", "").trim().take(3).uppercase()
            val schoolCode = "SRT-${cityAbbr.ifBlank { "REG" }}${registration.id.toString().padStart(2, '0')}"
            val adminUsername = "admin_${cleanSchool.ifBlank { "sekolah" }}_${registration.id}"
            val adminPassword = PasswordGenerator.generateRandomPassword()

            val updated = registration.copy(
                status = "APPROVED",
                rejectionReason = "",
                generatedSchoolCode = schoolCode,
                generatedAdminUsername = adminUsername,
                generatedAdminPassword = adminPassword,
                approvedDate = System.currentTimeMillis()
            )
            repository.updateSchoolRegistration(updated)
            onApproved(updated)
        }
    }

    fun rejectSchoolRegistration(
        registration: SchoolRegistration,
        reason: String,
        onComplete: (SchoolRegistration) -> Unit = {}
    ) {
        viewModelScope.launch {
            val updated = registration.copy(
                status = "REJECTED",
                rejectionReason = reason.trim().ifBlank { "Data pendaftaran belum memenuhi kelengkapan berkas." }
            )
            repository.updateSchoolRegistration(updated)
            onComplete(updated)
        }
    }

    fun deleteSchoolRegistration(registration: SchoolRegistration) {
        viewModelScope.launch {
            repository.deleteSchoolRegistration(registration)
        }
    }

    fun logout() {
        _currentUserRole.value = UserRole.NOT_LOGGED_IN
        authPrefs.edit().clear().apply()
    }

    fun switchRoleToAdmin() {
        _currentUserRole.value = UserRole.ADMIN
    }

    fun switchRoleToWali() {
        _currentUserRole.value = UserRole.WALI
    }

    // --- Manajemen Akun Wali Asuh (Khusus Administrator) ---

    fun addWaliAccount(
        name: String,
        username: String = "",
        dormitory: String,
        phone: String,
        customPassword: String? = null,
        onComplete: (WaliAccount) -> Unit = {}
    ) {
        viewModelScope.launch {
            val password = if (!customPassword.isNullOrBlank()) {
                customPassword.trim()
            } else {
                PasswordGenerator.generateRandomPassword()
            }

            val finalUsername = username.trim().ifBlank {
                name.trim().lowercase().replace(" ", "").filter { it.isLetterOrDigit() }.ifBlank { "wali${System.currentTimeMillis() % 1000}" }
            }

            val newAccount = WaliAccount(
                name = name.trim(),
                username = finalUsername,
                password = password,
                dormitory = dormitory.ifBlank { "Asrama Putra" },
                phone = phone.trim(),
                schoolId = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            )

            val id = repository.insertWaliAccount(newAccount)
            onComplete(newAccount.copy(id = id))
        }
    }

    fun resetWaliPassword(account: WaliAccount, onComplete: (String) -> Unit = {}) {
        viewModelScope.launch {
            val newPass = PasswordGenerator.generateRandomPassword()
            val updated = account.copy(password = newPass)
            repository.updateWaliAccount(updated)
            onComplete(newPass)
        }
    }

    fun deleteWaliAccount(account: WaliAccount, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteWaliAccount(account)
            onComplete()
        }
    }

    fun updateWaliAccount(account: WaliAccount, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateWaliAccount(account)
            onComplete()
        }
    }
}

