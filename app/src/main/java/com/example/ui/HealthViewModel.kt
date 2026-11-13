package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolProfile
import com.example.data.model.SchoolRegistration
import com.example.data.model.ShiftMessage
import com.example.data.model.WaliAccount
import com.example.data.remote.supabase.SupabaseConfig
import com.example.data.remote.supabase.SupabaseSyncManager
import com.example.data.repository.HealthRepository
import com.example.util.NotificationHelper
import com.example.util.PasswordGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
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
            db.schoolRegistrationDao(),
            db.shiftMessageDao(),
            db.haidRecordDao()
        )
    }

    private fun isUnitTestEnvironment(): Boolean {
        return android.os.Build.FINGERPRINT.equals("robolectric", ignoreCase = true)
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

    // Active Username (Loaded from preferences or active login)
    private val _currentUsername = MutableStateFlow<String?>(authPrefs.getString("saved_username", null))
    val currentUsername: StateFlow<String?> = _currentUsername.asStateFlow()

    // School Profile & Tenant Customization
    private val schoolPrefs = application.getSharedPreferences("waliasuh_school_prefs", Context.MODE_PRIVATE)
    private fun parseFaskesList(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return listOf("Puskesmas Plosoklaten")
        return raw.split("|||").map { it.trim() }.filter { it.isNotBlank() }
    }

    private val _schoolProfile = MutableStateFlow(
        SchoolProfile(
            schoolName = schoolPrefs.getString("school_name", "Sekolah Rakyat SRT 1 Kabupaten Kediri") ?: "Sekolah Rakyat SRT 1 Kabupaten Kediri",
            shortName = schoolPrefs.getString("short_name", "SRT 1 Kediri") ?: "SRT 1 Kediri",
            adminName = schoolPrefs.getString("admin_name", "Admin Utama") ?: "Admin Utama",
            city = schoolPrefs.getString("city", "Kabupaten Kediri") ?: "Kabupaten Kediri",
            tenantCode = schoolPrefs.getString("tenant_code", "SRT1-KDR") ?: "SRT1-KDR",
            primaryFaskesName = schoolPrefs.getString("primary_faskes", "UKS Sekolah") ?: "UKS Sekolah",
            secondaryFaskesName = schoolPrefs.getString("secondary_faskes", "Puskesmas Wates") ?: "Puskesmas Wates",
            additionalFaskesList = parseFaskesList(schoolPrefs.getString("additional_faskes", "Puskesmas Plosoklaten"))
        )
    )
    val schoolProfile: StateFlow<SchoolProfile> = _schoolProfile.asStateFlow()

    init {
        if (!isUnitTestEnvironment() && SupabaseConfig.isConfigured(application)) {
            when (_currentUserRole.value) {
                UserRole.ADMIN, UserRole.WALI -> {
                    val activeCode = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                    viewModelScope.launch {
                        SupabaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
                        SupabaseSyncManager.syncAllSchoolData(getApplication(), activeCode, repository)
                    }
                }
                UserRole.SUPERADMIN -> {
                    viewModelScope.launch {
                        SupabaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
                    }
                }
                else -> Unit
            }
        }
    }

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
            .putString("additional_faskes", profile.additionalFaskesList.joinToString("|||"))
            .apply()
        onComplete()
    }

    val schoolRegistrations: StateFlow<List<SchoolRegistration>> = repository.allSchoolRegistrations
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Raw global flows for SuperAdmin
    val allGlobalStudents: Flow<List<ChildStudent>> = repository.allStudents
    val allGlobalWaliAccounts: Flow<List<WaliAccount>> = repository.allWaliAccounts

    // Multi-tenant isolated students:
    // SuperAdmin -> melihat semua siswa secara global
    // Admin Sekolah / Wali Asuh -> hanya melihat siswa pada sekolah aktif (tenantCode)
    val students: StateFlow<List<ChildStudent>> = combine(
        repository.allStudents,
        _schoolProfile,
        _currentUserRole
    ) { studentList, profile, role ->
        if (role == UserRole.SUPERADMIN) {
            studentList
        } else {
            val tenant = profile.tenantCode.ifBlank { "SRT1-KDR" }
            studentList.filter { it.schoolId.equals(tenant, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Multi-tenant isolated wali accounts:
    // SuperAdmin -> melihat semua akun wali secara global
    // Admin Sekolah / Wali Asuh -> hanya melihat akun wali pada sekolah aktif (tenantCode)
    val waliAccounts: StateFlow<List<WaliAccount>> = combine(
        repository.allWaliAccounts,
        _schoolProfile,
        _currentUserRole
    ) { accounts, profile, role ->
        if (role == UserRole.SUPERADMIN) {
            accounts
        } else {
            val tenant = profile.tenantCode.ifBlank { "SRT1-KDR" }
            accounts.filter { it.schoolId.equals(tenant, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Akun Wali Asuh yang sedang aktif login di sesi aplikasi saat ini
    val currentWaliAccount: StateFlow<WaliAccount?> = combine(
        repository.allWaliAccounts,
        _currentUsername,
        _waliName
    ) { accounts, username, name ->
        if (!username.isNullOrBlank()) {
            accounts.firstOrNull { it.username.equals(username, ignoreCase = true) }
        } else {
            accounts.firstOrNull { it.name.equals(name, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Multi-tenant isolated medical records:
    val allRecords: StateFlow<List<MedicalRecord>> = combine(
        repository.allRecords,
        students,
        _currentUserRole
    ) { records, tenantStudents, role ->
        if (role == UserRole.SUPERADMIN) {
            records
        } else {
            val validStudentIds = tenantStudents.map { it.id }.toSet()
            records.filter { it.studentId in validStudentIds }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activePlans: StateFlow<List<MedicalRecord>> = combine(
        repository.activePlans,
        students,
        _currentUserRole
    ) { plans, tenantStudents, role ->
        if (role == UserRole.SUPERADMIN) {
            plans
        } else {
            val validStudentIds = tenantStudents.map { it.id }.toSet()
            plans.filter { it.studentId in validStudentIds }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val completedRecords: StateFlow<List<MedicalRecord>> = combine(
        repository.completedRecords,
        students,
        _currentUserRole
    ) { records, tenantStudents, role ->
        if (role == UserRole.SUPERADMIN) {
            records
        } else {
            val validStudentIds = tenantStudents.map { it.id }.toSet()
            records.filter { it.studentId in validStudentIds }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val pendingControlRecords: StateFlow<List<MedicalRecord>> = combine(
        repository.pendingControlRecords,
        students,
        _currentUserRole
    ) { records, tenantStudents, role ->
        if (role == UserRole.SUPERADMIN) {
            records
        } else {
            val validStudentIds = tenantStudents.map { it.id }.toSet()
            records.filter { it.studentId in validStudentIds }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Multi-tenant isolated shift messages (Operan Jaga Antar-Shift Wali Asuh):
    val shiftMessages: StateFlow<List<ShiftMessage>> = combine(
        repository.allShiftMessages,
        _schoolProfile,
        _currentUserRole
    ) { messages, profile, role ->
        if (role == UserRole.SUPERADMIN) {
            messages
        } else {
            val tenant = profile.tenantCode.ifBlank { "SRT1-KDR" }
            messages.filter { it.schoolId.equals(tenant, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Multi-tenant isolated haid records (Pendataan Siklus & Fiqih Haid Siswi Asrama):
    val allHaidRecords: StateFlow<List<HaidRecord>> = combine(
        repository.allHaidRecords,
        _schoolProfile,
        _currentUserRole
    ) { records, profile, role ->
        if (role == UserRole.SUPERADMIN) {
            records
        } else {
            val tenant = profile.tenantCode.ifBlank { "SRT1-KDR" }
            records.filter { it.schoolId.equals(tenant, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeHaidRecords: StateFlow<List<HaidRecord>> = combine(
        allHaidRecords,
        students
    ) { records, _ ->
        records.filter { it.isActive }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val suciRecords: StateFlow<List<HaidRecord>> = combine(
        allHaidRecords,
        students
    ) { records, _ ->
        records.filter { !it.isActive }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun saveHaidRecord(record: HaidRecord, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val activeSchool = record.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val assignedId = if (record.id > 0L) record.id else SupabaseSyncManager.generateCloudSafeId()
            val stamped = record.copy(id = assignedId, schoolId = activeSchool)
            repository.insertHaidRecord(stamped)
            onComplete()
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleHaidRecord(getApplication(), stamped)
            }
        }
    }

    fun completeHaidRecord(id: Long, endDate: Long = System.currentTimeMillis(), onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.completeHaidRecord(id, endDate, "SUCI")
            onComplete()
            if (!isUnitTestEnvironment()) {
                repository.getHaidRecordById(id)?.let { updated ->
                    SupabaseSyncManager.uploadSingleHaidRecord(getApplication(), updated)
                }
            }
        }
    }

    fun deleteHaidRecord(record: HaidRecord, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteHaidRecord(record)
            onComplete()
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.deleteRemoteHaidRecord(getApplication(), record.id)
            }
        }
    }

    fun deleteHaidRecordById(id: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteHaidRecordById(id)
            onComplete()
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.deleteRemoteHaidRecord(getApplication(), id)
            }
        }
    }

    fun findStudentByCode(code: String): ChildStudent? {
        val q = code.trim()
        if (q.isEmpty()) return null
        return students.value.firstOrNull { student ->
            (student.nik.isNotBlank() && student.nik.trim().equals(q, ignoreCase = true)) ||
                (student.nis.isNotBlank() && student.nis.trim().equals(q, ignoreCase = true)) ||
                student.id.toString() == q
        }
    }

    fun cleanAllStudentsAndWaliData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val activeCode = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            repository.deleteAllStudents()
            repository.deleteAllWaliAccounts()
            repository.deleteAllRecords()
            repository.deleteShiftMessagesBySchool(activeCode)
            onComplete()
        }
    }

    fun clearSchoolData(schoolId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val targetId = schoolId.trim().ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            repository.deleteStudentsBySchool(targetId)
            repository.deleteWaliAccountsBySchool(targetId)
            repository.deleteShiftMessagesBySchool(targetId)
            repository.deleteHaidRecordsBySchool(targetId)
            onComplete()
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.deleteStudentsBySchool(getApplication(), targetId)
                SupabaseSyncManager.deleteWaliAccountsBySchool(getApplication(), targetId)
                SupabaseSyncManager.deleteShiftMessagesBySchool(getApplication(), targetId)
            }
        }
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
            val existingList = if (replace) {
                repository.deleteStudentsBySchool(targetSchoolId)
                emptyList()
            } else {
                repository.getStudentsBySchoolList(targetSchoolId)
            }

            val existingByNik = existingList.filter { it.nik.isNotBlank() }.associateBy { it.nik.trim() }
            val existingByNis = existingList.filter { it.nis.isNotBlank() }.associateBy { it.nis.trim() }
            val existingByName = existingList.groupBy { it.name.trim().lowercase() }

            val stampedStudents = studentList.map { imported ->
                val matchedExisting = if (!replace) {
                    (imported.nik.takeIf { it.isNotBlank() }?.let { existingByNik[it.trim()] })
                        ?: (imported.nis.takeIf { it.isNotBlank() }?.let { existingByNis[it.trim()] })
                        ?: existingByName[imported.name.trim().lowercase()]?.firstOrNull()
                } else null

                if (matchedExisting != null) {
                    imported.copy(
                        id = matchedExisting.id,
                        schoolId = targetSchoolId,
                        nis = imported.nis.ifBlank { matchedExisting.nis },
                        nik = imported.nik.ifBlank { matchedExisting.nik },
                        noKk = imported.noKk.ifBlank { matchedExisting.noKk },
                        birthPlace = imported.birthPlace.ifBlank { matchedExisting.birthPlace },
                        birthDate = imported.birthDate.ifBlank { matchedExisting.birthDate },
                        motherName = imported.motherName.ifBlank { matchedExisting.motherName },
                        address = imported.address.ifBlank { matchedExisting.address },
                        roomNumber = imported.roomNumber.ifBlank { matchedExisting.roomNumber },
                        jenjang = imported.jenjang.ifBlank { matchedExisting.jenjang },
                        allergies = if (imported.allergies == "Tidak ada" && matchedExisting.allergies != "Tidak ada" && matchedExisting.allergies.isNotBlank()) {
                            matchedExisting.allergies
                        } else {
                            imported.allergies
                        },
                        bloodType = if (imported.bloodType == "-" && matchedExisting.bloodType != "-" && matchedExisting.bloodType.isNotBlank()) {
                            matchedExisting.bloodType
                        } else {
                            imported.bloodType
                        },
                        specialNotes = imported.specialNotes.ifBlank { matchedExisting.specialNotes }
                    )
                } else {
                    val assignedId = if (imported.id > 0L) imported.id else SupabaseSyncManager.generateCloudSafeId()
                    imported.copy(id = assignedId, schoolId = targetSchoolId)
                }
            }
            repository.insertAllStudents(stampedStudents)
            onComplete(stampedStudents.size)

            if (!isUnitTestEnvironment() && SupabaseConfig.isConfigured(getApplication())) {
                viewModelScope.launch {
                    if (replace) {
                        SupabaseSyncManager.deleteStudentsBySchool(getApplication(), targetSchoolId)
                    }
                    SupabaseSyncManager.uploadStudents(getApplication(), stampedStudents)
                }
            }
        }
    }

    fun loadDefaultKediriStudents(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val defaultStudents = AppDatabase.loadStudentsFromAssets(getApplication())
            if (defaultStudents.isNotEmpty()) {
                repository.deleteStudentsBySchool("SRT1-KDR")
                val stamped = defaultStudents.map {
                    val assignedId = if (it.id > 0L) it.id else SupabaseSyncManager.generateCloudSafeId()
                    it.copy(id = assignedId, schoolId = "SRT1-KDR")
                }
                repository.insertAllStudents(stamped)
                if (!isUnitTestEnvironment() && SupabaseConfig.isConfigured(getApplication())) {
                    viewModelScope.launch {
                        SupabaseSyncManager.deleteStudentsBySchool(getApplication(), "SRT1-KDR")
                        SupabaseSyncManager.uploadStudents(getApplication(), stamped)
                    }
                }
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
                val assignedId = if (acc.id > 0L) acc.id else SupabaseSyncManager.generateCloudSafeId()
                acc.copy(id = assignedId, schoolId = targetSchoolId)
            }
            repository.insertAllWaliAccounts(stampedAccounts)
            onComplete(stampedAccounts.size)

            if (!isUnitTestEnvironment() && SupabaseConfig.isConfigured(getApplication())) {
                viewModelScope.launch {
                    if (replace) {
                        SupabaseSyncManager.deleteWaliAccountsBySchool(getApplication(), targetSchoolId)
                    }
                    SupabaseSyncManager.uploadWaliAccounts(getApplication(), stampedAccounts)
                }
            }
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

    /**
     * Memverifikasi apakah notifikasi boleh dimunculkan untuk siswa tertentu.
     * Dalam arsitektur Multi-Tenant:
     * - Pengguna yang belum login TIDAK menerima notifikasi.
     * - Pengguna SuperAdmin menerima notifikasi seluruh sekolah.
     * - Pengguna Admin / Wali Asuh HANYA menerima notifikasi jika terdaftar & login di sekolah yang sama dengan siswa.
     */
    fun shouldTriggerNotificationForStudent(studentSchoolId: String): Boolean {
        if (_currentUserRole.value == UserRole.NOT_LOGGED_IN) return false
        if (_currentUserRole.value == UserRole.SUPERADMIN) return true

        val activeSchoolCode = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
        val targetSchoolCode = studentSchoolId.ifBlank { "SRT1-KDR" }
        return activeSchoolCode.equals(targetSchoolCode, ignoreCase = true)
    }

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
            val activeSchool = student.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val record = MedicalRecord(
                id = SupabaseSyncManager.generateCloudSafeId(),
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
            // Multi-tenant filter: Hanya kirim notifikasi resmi sistem Android jika user login di sekolah yang sama atau SuperAdmin
            if (shouldTriggerNotificationForStudent(student.schoolId)) {
                NotificationHelper.showSickStudentNotification(
                    context = getApplication(),
                    studentName = student.name,
                    className = student.classGrade,
                    roomNumber = student.roomNumber.ifBlank { student.dormitory },
                    complaint = complaint,
                    faskes = faskes,
                    schoolName = _schoolProfile.value.shortName,
                    notificationId = (newId % 100000).toInt()
                )
            }
            // Sinkronisasi latar belakang ke Supabase Cloud jika terkonfigurasi
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleRecord(getApplication(), recordWithId, activeSchool)
            }
        }
    }

    // Mark departed (Dianter / Siap Jalan)
    fun markDeparted(recordId: Long) {
        viewModelScope.launch {
            repository.updateStatus(recordId, "DEPARTED")
            if (!isUnitTestEnvironment()) {
                val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                repository.getRecordById(recordId).firstOrNull()?.let { updated ->
                    SupabaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
                }
            }
        }
    }

    // Mark departed with specific referral faskes and escort
    fun markDepartedToFaskes(recordId: Long, targetFaskes: String, escortName: String) {
        viewModelScope.launch {
            repository.updateDepartureWithFaskes(recordId, "DEPARTED", targetFaskes, escortName)
            if (!isUnitTestEnvironment()) {
                val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                repository.getRecordById(recordId).firstOrNull()?.let { updated ->
                    SupabaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
                }
            }
        }
    }

    fun updateRecord(record: MedicalRecord) {
        viewModelScope.launch {
            repository.updateRecord(record)
            if (!isUnitTestEnvironment()) {
                val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                SupabaseSyncManager.uploadSingleRecord(getApplication(), record, activeSchool)
            }
        }
    }

    // Update custom status
    fun updateStatus(recordId: Long, status: String) {
        viewModelScope.launch {
            repository.updateStatus(recordId, status)
            if (!isUnitTestEnvironment()) {
                val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                repository.getRecordById(recordId).firstOrNull()?.let { updated ->
                    SupabaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
                }
            }
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
            if (!isUnitTestEnvironment()) {
                val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                SupabaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
            }
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
            val activeSchool = student.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val record = MedicalRecord(
                id = SupabaseSyncManager.generateCloudSafeId(),
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
            // Multi-tenant filter: Hanya kirim notifikasi resmi sistem Android jika user login di sekolah yang sama atau SuperAdmin
            if (shouldTriggerNotificationForStudent(student.schoolId)) {
                NotificationHelper.showSickStudentNotification(
                    context = getApplication(),
                    studentName = student.name,
                    className = student.classGrade,
                    roomNumber = student.roomNumber.ifBlank { student.dormitory },
                    complaint = complaint.ifBlank { diagnosis },
                    faskes = faskes,
                    schoolName = _schoolProfile.value.shortName,
                    notificationId = (newId % 100000).toInt()
                )
            }
            // Sinkronisasi latar belakang ke Supabase Cloud
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleRecord(getApplication(), recordWithId, activeSchool)
            }
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
            if (!isUnitTestEnvironment()) {
                val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                repository.getRecordById(recordId).firstOrNull()?.let { updated ->
                    SupabaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
                }
            }
        }
    }

    // Delete record
    fun deleteRecord(record: MedicalRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            // Hapus dari Supabase jika terhubung
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.deleteRemoteRecord(getApplication(), record.id)
            }
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
        specialNotes: String,
        nik: String = "",
        noKk: String = "",
        birthPlace: String = "",
        birthDate: String = "",
        motherName: String = "",
        address: String = "",
        roomNumber: String = "",
        jenjang: String = ""
    ) {
        viewModelScope.launch {
            val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            val resolvedJenjang = jenjang.trim().ifBlank {
                com.example.util.StudentCsvParser.deriveJenjang(classGrade)
            }
            val student = ChildStudent(
                id = SupabaseSyncManager.generateCloudSafeId(),
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
                nik = nik.trim(),
                noKk = noKk.trim(),
                birthPlace = birthPlace.trim(),
                birthDate = birthDate.trim(),
                motherName = motherName.trim(),
                address = address.trim(),
                roomNumber = roomNumber.trim(),
                jenjang = resolvedJenjang,
                schoolId = activeSchool
            )
            val newId = repository.insertStudent(student)
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleStudent(getApplication(), student.copy(id = newId))
            }
        }
    }

    fun updateStudent(student: ChildStudent, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateStudent(student)
            onComplete()
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleStudent(getApplication(), student)
            }
        }
    }

    fun deleteStudent(student: ChildStudent) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.deleteRemoteStudent(getApplication(), student.id)
            }
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
        syncAllWithSupabase(onResult)
    }

    fun syncAllWithSupabase(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isSyncing.value = true
            val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            SupabaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
            val result = SupabaseSyncManager.syncAllSchoolData(
                context = getApplication(),
                schoolId = activeSchool,
                repository = repository
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
            val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            val studentList = students.value
            val result = SupabaseSyncManager.uploadStudents(getApplication(), studentList)
            if (result.isSuccess) {
                SupabaseSyncManager.uploadWaliAccounts(getApplication(), waliAccounts.value)
                val count = result.getOrNull() ?: 0
                val msg = "Berhasil mengunggah $count data siswa ($activeSchool) ke Supabase!"
                _isSyncing.value = false
                _supabaseSyncMessage.value = msg
                onResult(true, msg)
            } else {
                _isSyncing.value = false
                val err = result.exceptionOrNull()?.message ?: "Gagal upload data siswa ke Supabase."
                _supabaseSyncMessage.value = err
                onResult(false, err)
            }
        }
    }

    // --- Authentication & Role Management ---
    suspend fun executeLogin(user: String, pass: String, rememberMe: Boolean = false): LoginResult {
        val cleanUser = user.trim()
        val cleanPass = pass.trim()

        if (cleanUser.isEmpty() || cleanPass.isEmpty()) {
            return LoginResult.Error("Username dan password wajib diisi.")
        }

        // 0. Cek Kredensial Superadmin Resmi
        if (cleanUser.equals("superadmin", ignoreCase = true) && cleanPass == "Woyowoyo12@") {
            _currentUserRole.value = UserRole.SUPERADMIN
            _currentUsername.value = "superadmin"
            if (rememberMe) {
                authPrefs.edit()
                    .putString("saved_role", "SUPERADMIN")
                    .putString("saved_username", "superadmin")
                    .apply()
            }
            if (!isUnitTestEnvironment() && SupabaseConfig.isConfigured(getApplication())) {
                viewModelScope.launch {
                    SupabaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
                }
            }
            return LoginResult.SuccessSuperAdmin
        }

        // 1. Cek Kredensial Administrator Resmi
        if (cleanUser.equals("ekowahyudiadmin", ignoreCase = true) && cleanPass == "Woyowoyo12@") {
            _currentUserRole.value = UserRole.ADMIN
            _currentUsername.value = "ekowahyudiadmin"
            if (rememberMe) {
                authPrefs.edit()
                    .putString("saved_role", "ADMIN")
                    .putString("saved_username", "ekowahyudiadmin")
                    .apply()
            }
            if (!isUnitTestEnvironment() && SupabaseConfig.isConfigured(getApplication())) {
                val activeCode = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                viewModelScope.launch {
                    SupabaseSyncManager.syncAllSchoolData(getApplication(), activeCode, repository)
                }
            }
            return LoginResult.SuccessAdmin
        }

        // Helper mencari admin sekolah & akun wali di database lokal
        suspend fun findLocalSchoolAdmin(username: String): SchoolRegistration? {
            return repository.getSchoolByAdminUsername(username)
                ?: repository.getAllSchoolRegistrationsList().firstOrNull {
                    it.generatedAdminUsername.equals(username, ignoreCase = true)
                }
        }

        suspend fun findLocalWali(username: String): WaliAccount? {
            return repository.getWaliAccountByUsername(username)
                ?: repository.getAllWaliAccountsList().firstOrNull {
                    it.username.trim().equals(username, ignoreCase = true) ||
                        it.username.trim().replace(" ", "").equals(username.replace(" ", ""), ignoreCase = true) ||
                        it.name.trim().equals(username, ignoreCase = true) ||
                        it.name.trim().replace(" ", "").equals(username.replace(" ", ""), ignoreCase = true) ||
                        (it.phone.isNotBlank() && it.phone.trim().equals(username, ignoreCase = true))
                }
        }

        // Jika belum ditemukan di lokal (misal baru login di perangkat lain), tarik katalog auth dari Supabase
        var matchedSchool = findLocalSchoolAdmin(cleanUser)
        var account = if (matchedSchool == null) findLocalWali(cleanUser) else null
        if (matchedSchool == null && account == null && !isUnitTestEnvironment() && SupabaseConfig.isConfigured(getApplication())) {
            SupabaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
            matchedSchool = findLocalSchoolAdmin(cleanUser)
            if (matchedSchool == null) {
                account = findLocalWali(cleanUser)
            }
        }

        // 2. Cek Akun Admin Sekolah Terdaftar / Baru
        if (matchedSchool != null && matchedSchool.generatedAdminPassword == cleanPass) {
            if (matchedSchool.status == "SUSPENDED") {
                return LoginResult.Error("Maaf akun sedang disuspend oleh admin")
            }
            if (matchedSchool.status != "APPROVED") {
                return LoginResult.Error("Pengajuan pendaftaran sekolah ini belum disetujui (Status: ${matchedSchool.status}).")
            }
            _currentUserRole.value = UserRole.ADMIN
            _currentUsername.value = matchedSchool.generatedAdminUsername
            updateSchoolProfile(
                SchoolProfile(
                    schoolName = matchedSchool.schoolName,
                    shortName = matchedSchool.schoolName.take(15),
                    adminName = matchedSchool.picName,
                    city = matchedSchool.city,
                    tenantCode = matchedSchool.generatedSchoolCode
                )
            )
            if (rememberMe) {
                authPrefs.edit()
                    .putString("saved_role", "ADMIN")
                    .putString("saved_username", matchedSchool.generatedAdminUsername)
                    .apply()
            }
            if (!isUnitTestEnvironment() && SupabaseConfig.isConfigured(getApplication())) {
                viewModelScope.launch {
                    SupabaseSyncManager.syncAllSchoolData(
                        getApplication(),
                        matchedSchool.generatedSchoolCode,
                        repository
                    )
                }
            }
            return LoginResult.SuccessAdmin
        }

        // 3. Cek Akun Wali Asuh
        if (account != null) {
            if (account.password.trim() != cleanPass) {
                return LoginResult.Error("Password untuk akun '${account.name}' salah. Silakan periksa kembali huruf besar/kecil.")
            }

            // Cek apakah sekolah akun wali asuh ini sedang disuspend
            val matchedSchoolWali = repository.getSchoolByCode(account.schoolId)
                ?: repository.getAllSchoolRegistrationsList().firstOrNull {
                    it.generatedSchoolCode.equals(account.schoolId, ignoreCase = true)
                }
            if (matchedSchoolWali != null && matchedSchoolWali.status == "SUSPENDED") {
                return LoginResult.Error("Maaf akun sedang disuspend oleh admin")
            }

            _currentUserRole.value = UserRole.WALI
            _waliName.value = account.name
            _currentUsername.value = account.username

            // Sinkronkan tenant aktif sesuai sekolah akun Wali Asuh
            val activeSchoolCode = when {
                matchedSchoolWali != null -> {
                    updateSchoolProfile(
                        SchoolProfile(
                            schoolName = matchedSchoolWali.schoolName,
                            shortName = matchedSchoolWali.schoolName.take(15),
                            adminName = matchedSchoolWali.picName,
                            city = matchedSchoolWali.city,
                            tenantCode = matchedSchoolWali.generatedSchoolCode
                        )
                    )
                    matchedSchoolWali.generatedSchoolCode
                }
                account.schoolId.isNotBlank() -> {
                    _schoolProfile.value = _schoolProfile.value.copy(tenantCode = account.schoolId)
                    schoolPrefs.edit().putString("tenant_code", account.schoolId).apply()
                    account.schoolId
                }
                else -> _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            }

            if (rememberMe) {
                authPrefs.edit()
                    .putString("saved_role", "WALI")
                    .putString("saved_wali_name", account.name)
                    .putString("saved_username", account.username)
                    .apply()
            }

            if (!isUnitTestEnvironment() && SupabaseConfig.isConfigured(getApplication())) {
                viewModelScope.launch {
                    SupabaseSyncManager.syncAllSchoolData(getApplication(), activeSchoolCode, repository)
                }
            }
            return LoginResult.SuccessWali(account)
        }

        return LoginResult.Error("Username '$cleanUser' tidak ditemukan. Pastikan akun telah didaftarkan oleh Administrator.")
    }

    fun login(user: String, pass: String, rememberMe: Boolean = false, onResult: (LoginResult) -> Unit) {
        viewModelScope.launch {
            val result = executeLogin(user, pass, rememberMe)
            onResult(result)
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
                id = SupabaseSyncManager.generateCloudSafeId(),
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
            val saved = reg.copy(id = newId)
            onComplete(saved)
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleSchoolRegistration(getApplication(), saved)
            }
        }
    }

    fun approveSchoolRegistration(
        registration: SchoolRegistration,
        onApproved: (SchoolRegistration) -> Unit = {}
    ) {
        viewModelScope.launch {
            val cleanSchool = registration.schoolName.trim().lowercase().replace(" ", "").filter { it.isLetterOrDigit() }.take(10)
            val cityAbbr = registration.city.trim().replace("Kota", "").replace("Kabupaten", "").replace("Kab.", "").trim().take(3).uppercase()
            val codeSuffix = (registration.id % 1000L).toString().padStart(2, '0')
            val schoolCode = "SRT-${cityAbbr.ifBlank { "REG" }}$codeSuffix"
            val adminUsername = "admin_${cleanSchool.ifBlank { "sekolah" }}_${registration.id % 10000L}"
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
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleSchoolRegistration(getApplication(), updated)
            }
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
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleSchoolRegistration(getApplication(), updated)
            }
        }
    }

    fun suspendSchool(
        registration: SchoolRegistration,
        onComplete: (SchoolRegistration) -> Unit = {}
    ) {
        viewModelScope.launch {
            val updated = registration.copy(status = "SUSPENDED")
            repository.updateSchoolRegistration(updated)
            onComplete(updated)
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleSchoolRegistration(getApplication(), updated)
            }
        }
    }

    fun unsuspendSchool(
        registration: SchoolRegistration,
        onComplete: (SchoolRegistration) -> Unit = {}
    ) {
        viewModelScope.launch {
            val updated = registration.copy(status = "APPROVED")
            repository.updateSchoolRegistration(updated)
            onComplete(updated)
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleSchoolRegistration(getApplication(), updated)
            }
        }
    }

    fun deleteSchoolRegistration(
        registration: SchoolRegistration,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (registration.generatedSchoolCode.isNotBlank()) {
                repository.deleteStudentsBySchool(registration.generatedSchoolCode)
                repository.deleteWaliAccountsBySchool(registration.generatedSchoolCode)
                if (!isUnitTestEnvironment()) {
                    SupabaseSyncManager.deleteStudentsBySchool(getApplication(), registration.generatedSchoolCode)
                    SupabaseSyncManager.deleteWaliAccountsBySchool(getApplication(), registration.generatedSchoolCode)
                }
            }
            repository.deleteSchoolRegistration(registration)
            onComplete()
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.deleteRemoteSchoolRegistration(getApplication(), registration.id)
            }
        }
    }

    fun cleanLegacyPilotData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteStudentsBySchool("SRT1-KDR")
            repository.deleteStudentsBySchool("SRT-KED03")
            repository.deleteWaliAccountsBySchool("SRT1-KDR")
            repository.deleteWaliAccountsBySchool("SRT-KED03")
            onComplete()
        }
    }

    fun logout() {
        _currentUserRole.value = UserRole.NOT_LOGGED_IN
        _currentUsername.value = null
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
                id = SupabaseSyncManager.generateCloudSafeId(),
                name = name.trim(),
                username = finalUsername,
                password = password,
                dormitory = dormitory.ifBlank { "Asrama Putra" },
                phone = phone.trim(),
                schoolId = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            )

            val id = repository.insertWaliAccount(newAccount)
            val saved = newAccount.copy(id = id)
            onComplete(saved)
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadWaliAccounts(getApplication(), listOf(saved))
            }
        }
    }

    fun resetWaliPassword(account: WaliAccount, onComplete: (String) -> Unit = {}) {
        viewModelScope.launch {
            val newPass = PasswordGenerator.generateRandomPassword()
            val updated = account.copy(password = newPass)
            repository.updateWaliAccount(updated)
            onComplete(newPass)
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadWaliAccounts(getApplication(), listOf(updated))
            }
        }
    }

    fun deleteWaliAccount(account: WaliAccount, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteWaliAccount(account)
            onComplete()
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.deleteRemoteWaliAccount(getApplication(), account.id)
            }
        }
    }

    fun updateWaliAccount(account: WaliAccount, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateWaliAccount(account)
            onComplete()
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadWaliAccounts(getApplication(), listOf(account))
            }
        }
    }

    // --- Ganti Password Mandiri oleh Wali Asuh (Tersinkronisasi ke Admin Sekolah) ---
    fun updateWaliSelfPassword(
        newName: String,
        newPassword: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val cleanPass = newPassword.trim()
            if (cleanPass.length < 6) {
                onResult(false, "Kata sandi baru minimal harus 6 karakter.")
                return@launch
            }

            val cleanName = newName.trim().ifBlank { _waliName.value }
            val username = _currentUsername.value

            var account: WaliAccount? = null
            if (!username.isNullOrBlank()) {
                account = repository.getWaliAccountByUsername(username)
            }
            if (account == null) {
                val all = repository.getAllWaliAccountsList()
                account = all.firstOrNull {
                    it.name.equals(_waliName.value, ignoreCase = true) ||
                    it.username.equals(_waliName.value, ignoreCase = true)
                }
            }

            if (account != null) {
                val updated = account.copy(
                    name = cleanName,
                    password = cleanPass
                )
                repository.updateWaliAccount(updated)
                _waliName.value = cleanName
                _currentUsername.value = updated.username
                if (authPrefs.getString("saved_role", "") == "WALI") {
                    authPrefs.edit()
                        .putString("saved_wali_name", cleanName)
                        .putString("saved_username", updated.username)
                        .apply()
                }
                onResult(true, "Kata sandi berhasil diperbarui dan tersinkronisasi ke Admin Sekolah.")
                if (!isUnitTestEnvironment()) {
                    SupabaseSyncManager.uploadWaliAccounts(getApplication(), listOf(updated))
                }
            } else {
                val defaultSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                val generatedUser = cleanName.lowercase().replace(" ", "").filter { it.isLetterOrDigit() }.ifBlank { "wali" }
                val newAcc = WaliAccount(
                    id = SupabaseSyncManager.generateCloudSafeId(),
                    name = cleanName,
                    username = generatedUser,
                    password = cleanPass,
                    dormitory = "Asrama Putra",
                    schoolId = defaultSchool
                )
                val insertedId = repository.insertWaliAccount(newAcc)
                _waliName.value = cleanName
                _currentUsername.value = newAcc.username
                if (authPrefs.getString("saved_role", "") == "WALI") {
                    authPrefs.edit()
                        .putString("saved_wali_name", cleanName)
                        .putString("saved_username", newAcc.username)
                        .apply()
                }
                onResult(true, "Kata sandi berhasil dibuat dan tersinkronisasi ke Admin Sekolah.")
                if (!isUnitTestEnvironment()) {
                    SupabaseSyncManager.uploadWaliAccounts(getApplication(), listOf(newAcc.copy(id = insertedId)))
                }
            }
        }
    }

    // --- Layanan Pesan Shift Berikutnya (Handover Jaga Asrama) ---

    fun addShiftMessage(
        title: String,
        followUpDate: Long,
        shift: String,
        targetWali: String,
        content: String,
        priority: String,
        onComplete: (ShiftMessage) -> Unit = {}
    ) {
        viewModelScope.launch {
            val tenant = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            val newMsg = ShiftMessage(
                id = SupabaseSyncManager.generateCloudSafeId(),
                title = title.trim(),
                followUpDate = followUpDate,
                shift = shift.trim().ifBlank { "Semua Shift" },
                targetWali = targetWali.trim().ifBlank { "Semua Wali Asuh" },
                messageContent = content.trim(),
                priority = priority.uppercase().ifBlank { "BIASA" },
                senderName = _waliName.value,
                createdAt = System.currentTimeMillis(),
                isCompleted = false,
                schoolId = tenant
            )
            val newId = repository.insertShiftMessage(newMsg)
            val saved = newMsg.copy(id = newId)
            onComplete(saved)
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleShiftMessage(getApplication(), saved)
            }
        }
    }

    fun toggleShiftMessageCompleted(message: ShiftMessage, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val newStatus = !message.isCompleted
            val completedBy = if (newStatus) _waliName.value else ""
            val completedAt = if (newStatus) System.currentTimeMillis() else 0L
            repository.updateShiftMessageCompleted(message.id, newStatus, completedBy, completedAt)
            onComplete()
            if (!isUnitTestEnvironment()) {
                repository.getShiftMessageById(message.id)?.let { updated ->
                    SupabaseSyncManager.uploadSingleShiftMessage(getApplication(), updated)
                }
            }
        }
    }

    fun deleteShiftMessage(message: ShiftMessage, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteShiftMessage(message)
            onComplete()
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.deleteRemoteShiftMessage(getApplication(), message.id)
            }
        }
    }
}

