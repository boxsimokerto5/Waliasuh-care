package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
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
            db.haidRecordDao(),
            db.fastingSessionDao(),
            db.fastingRecordDao()
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
        if (raw == null) return listOf("Puskesmas Plosoklaten")
        if (raw.isBlank()) return emptyList()
        return raw.split("|||").map { it.trim() }.filter { it.isNotBlank() }
    }

    private val initialSchoolName = schoolPrefs.getString("school_name", "Sekolah Rakyat SRT 1 Kabupaten Kediri")
        ?: "Sekolah Rakyat SRT 1 Kabupaten Kediri"

    private val _schoolProfile = MutableStateFlow(
        SchoolProfile(
            schoolName = initialSchoolName,
            shortName = SchoolProfile.sanitizeShortName(
                schoolName = initialSchoolName,
                savedShortName = schoolPrefs.getString("short_name", "SRT 1 Kediri")
            ),
            adminName = schoolPrefs.getString("admin_name", "Admin Utama") ?: "Admin Utama",
            address = schoolPrefs.getString("school_address", "") ?: "",
            city = schoolPrefs.getString("city", "Kabupaten Kediri") ?: "Kabupaten Kediri",
            tenantCode = schoolPrefs.getString("tenant_code", "SRT1-KDR") ?: "SRT1-KDR",
            primaryFaskesName = schoolPrefs.getString("primary_faskes", "UKS Sekolah") ?: "UKS Sekolah",
            secondaryFaskesName = schoolPrefs.getString("secondary_faskes", "Puskesmas Wates") ?: "Puskesmas Wates",
            additionalFaskesList = parseFaskesList(schoolPrefs.getString("additional_faskes", null)),
            faskesContacts = SchoolProfile.parseFaskesContacts(schoolPrefs.getString("faskes_contacts", null))
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

    private fun persistSchoolProfile(profile: SchoolProfile, markCustomized: Boolean) {
        _schoolProfile.value = profile
        schoolPrefs.edit().apply {
            putString("school_name", profile.schoolName)
            putString("short_name", profile.shortName)
            putString("admin_name", profile.adminName)
            putString("school_address", profile.address)
            putString("city", profile.city)
            putString("tenant_code", profile.tenantCode)
            putString("primary_faskes", profile.primaryFaskesName)
            putString("secondary_faskes", profile.secondaryFaskesName)
            putString("additional_faskes", profile.additionalFaskesList.joinToString("|||"))
            putString("faskes_contacts", SchoolProfile.serializeFaskesContacts(profile.faskesContacts))
            if (markCustomized) {
                putString("customized_tenant", profile.tenantCode)
            }
        }.apply()
    }

    private fun buildProfileForSchool(reg: SchoolRegistration): SchoolProfile {
        val cleanSchoolName = reg.schoolName.trim().ifBlank { "Sekolah Rakyat" }
        val cleanCity = reg.city.trim().ifBlank { "Indonesia" }
        val cleanCode = reg.generatedSchoolCode.trim()
        val customizedTenant = schoolPrefs.getString("customized_tenant", "").orEmpty()
        val isCustomizedForThisTenant = customizedTenant.equals(cleanCode, ignoreCase = true) &&
            _schoolProfile.value.tenantCode.equals(cleanCode, ignoreCase = true)

        return if (isCustomizedForThisTenant) {
            val current = _schoolProfile.value
            val resolvedFullName = current.schoolName.ifBlank { cleanSchoolName }
            current.copy(
                schoolName = resolvedFullName,
                shortName = SchoolProfile.sanitizeShortName(resolvedFullName, current.shortName),
                adminName = current.adminName.ifBlank { reg.picName },
                address = current.address.ifBlank { reg.address.trim() },
                city = current.city.ifBlank { cleanCity },
                tenantCode = cleanCode
            )
        } else {
            val isDefaultKediri = cleanCode.equals("SRT1-KDR", ignoreCase = true)
            SchoolProfile(
                schoolName = cleanSchoolName,
                shortName = cleanSchoolName,
                adminName = reg.picName.trim().ifBlank { "Admin Sekolah" },
                address = reg.address.trim(),
                city = cleanCity,
                tenantCode = cleanCode,
                primaryFaskesName = "UKS Sekolah",
                secondaryFaskesName = if (isDefaultKediri) {
                    "Puskesmas Wates"
                } else {
                    val cityLabel = cleanCity.removePrefix("Kabupaten ").removePrefix("Kab. ").removePrefix("Kota ").trim()
                    if (cityLabel.isNotBlank()) "Puskesmas $cityLabel" else "Puskesmas Rujukan"
                },
                additionalFaskesList = if (isDefaultKediri) listOf("Puskesmas Plosoklaten") else emptyList(),
                faskesContacts = emptyMap()
            )
        }
    }

    fun updateSchoolProfile(profile: SchoolProfile, onComplete: () -> Unit = {}) {
        val cleanProfile = profile.copy(
            schoolName = profile.schoolName.trim(),
            shortName = SchoolProfile.sanitizeShortName(profile.schoolName, profile.shortName),
            adminName = profile.adminName.trim().ifBlank { "Admin Utama" },
            address = profile.address.trim(),
            city = profile.city.trim().ifBlank { "Indonesia" }
        )
        persistSchoolProfile(cleanProfile, markCustomized = true)
        if (cleanProfile.tenantCode.isNotBlank()) {
            viewModelScope.launch {
                val existingReg = repository.getSchoolByCode(cleanProfile.tenantCode)
                if (existingReg != null) {
                    val updatedReg = existingReg.copy(
                        schoolName = cleanProfile.schoolName,
                        address = cleanProfile.address.ifBlank { existingReg.address },
                        city = cleanProfile.city,
                        picName = cleanProfile.adminName
                    )
                    repository.updateSchoolRegistration(updatedReg)
                    if (!isUnitTestEnvironment() && SupabaseConfig.isConfigured(getApplication())) {
                        SupabaseSyncManager.uploadSingleSchoolRegistration(getApplication(), updatedReg)
                    }
                }
            }
        }
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

    private val autoCompletedHaidIds = java.util.Collections.synchronizedSet(mutableSetOf<Long>())

    val activeHaidRecords: StateFlow<List<HaidRecord>> = combine(
        allHaidRecords,
        students
    ) { records, _ ->
        val expiredRecords = records.filter { it.status == "HAID" && it.endDate == null && it.isAutoCompletedByLimit }
        if (expiredRecords.isNotEmpty()) {
            viewModelScope.launch {
                expiredRecords.forEach { rec ->
                    if (autoCompletedHaidIds.add(rec.id)) {
                        val autoEnd = rec.effectiveEndDate ?: System.currentTimeMillis()
                        repository.completeHaidRecord(rec.id, autoEnd, "SUCI_AUTO")
                        if (!isUnitTestEnvironment()) {
                            repository.getHaidRecordById(rec.id)?.let { updated ->
                                SupabaseSyncManager.uploadSingleHaidRecord(getApplication(), updated)
                            }
                        }
                    }
                }
            }
        }
        records.filter { it.isActive }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val suciRecords: StateFlow<List<HaidRecord>> = combine(
        allHaidRecords,
        students
    ) { records, _ ->
        records
            .filter { !it.isActive }
            .sortedByDescending { it.effectiveEndDate ?: it.endDate ?: it.startDate }
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

    fun updateHaidRecord(record: HaidRecord, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val updated = record.copy(updatedAt = System.currentTimeMillis())
            repository.updateHaidRecord(updated)
            onComplete()
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleHaidRecord(getApplication(), updated)
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

    // Multi-tenant isolated fasting sessions & records (Pendataan Puasa Siswa Asrama):
    val fastingSessions: StateFlow<List<FastingSession>> = combine(
        repository.allFastingSessions,
        _schoolProfile,
        _currentUserRole
    ) { sessions, profile, role ->
        if (role == UserRole.SUPERADMIN) {
            sessions
        } else {
            val tenant = profile.tenantCode.ifBlank { "SRT1-KDR" }
            sessions.filter { it.schoolId.equals(tenant, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val fastingRecords: StateFlow<List<FastingRecord>> = combine(
        repository.allFastingRecords,
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

    fun createFastingSession(
        title: String,
        sessionDate: Long,
        description: String = "",
        onComplete: (FastingSession?) -> Unit = {}
    ) {
        if (_currentUserRole.value != UserRole.ADMIN && _currentUserRole.value != UserRole.SUPERADMIN) {
            onComplete(null)
            return
        }
        viewModelScope.launch {
            val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            val now = System.currentTimeMillis()
            val creator = _schoolProfile.value.adminName.ifBlank { _waliName.value.ifBlank { "Admin Sekolah" } }
            val newSession = FastingSession(
                id = SupabaseSyncManager.generateCloudSafeId(),
                title = title.trim(),
                sessionDate = sessionDate,
                description = description.trim(),
                isActive = true,
                createdBy = creator,
                schoolId = activeSchool,
                createdAt = now,
                updatedAt = now
            )
            repository.insertFastingSession(newSession)
            onComplete(newSession)
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleFastingSession(getApplication(), newSession)
            }
        }
    }

    fun toggleFastingSessionActive(session: FastingSession, onComplete: () -> Unit = {}) {
        if (_currentUserRole.value != UserRole.ADMIN && _currentUserRole.value != UserRole.SUPERADMIN) {
            onComplete()
            return
        }
        viewModelScope.launch {
            val updated = session.copy(
                isActive = !session.isActive,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateFastingSession(updated)
            onComplete()
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleFastingSession(getApplication(), updated)
            }
        }
    }

    fun deleteFastingSession(session: FastingSession, onComplete: () -> Unit = {}) {
        if (_currentUserRole.value != UserRole.ADMIN && _currentUserRole.value != UserRole.SUPERADMIN) {
            onComplete()
            return
        }
        viewModelScope.launch {
            repository.deleteFastingSession(session)
            onComplete()
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.deleteRemoteFastingSession(getApplication(), session.id)
            }
        }
    }

    fun registerStudentForFasting(
        session: FastingSession,
        student: ChildStudent,
        onResult: (added: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch {
            val existingInSession = repository.getFastingRecordsBySessionList(session.id)
            val alreadyRegistered = existingInSession.any { rec ->
                (student.id > 0L && rec.studentId == student.id) ||
                    (student.nik.isNotBlank() && rec.studentNik.equals(student.nik, ignoreCase = true)) ||
                    (student.nis.isNotBlank() && rec.studentNis.equals(student.nis, ignoreCase = true))
            }
            if (alreadyRegistered) {
                onResult(false, "${student.name} sudah terdaftar pada sesi ${session.title}.")
                return@launch
            }

            val activeSchool = session.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val now = System.currentTimeMillis()
            val resolvedJenjang = FastingJenjangHelper.resolveJenjang(student)
            val record = FastingRecord(
                id = SupabaseSyncManager.generateCloudSafeId(),
                sessionId = session.id,
                sessionTitle = session.title,
                sessionDate = session.sessionDate,
                studentId = student.id,
                studentName = student.name,
                studentNik = student.nik,
                studentNis = student.nis,
                studentClass = student.classGrade,
                studentDorm = student.dormitory,
                studentJenjang = resolvedJenjang,
                isSahurChecked = false,
                isBukaChecked = false,
                waliName = _waliName.value.ifBlank { "Wali Asuh" },
                schoolId = activeSchool,
                createdAt = now,
                updatedAt = now
            )
            repository.insertFastingRecord(record)
            onResult(true, "${student.name} ($resolvedJenjang) berhasil didaftarkan berpuasa.")
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleFastingRecord(getApplication(), record)
            }
        }
    }

    fun toggleFastingSahur(record: FastingRecord) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newState = !record.isSahurChecked
            val updated = record.copy(
                isSahurChecked = newState,
                sahurCheckedAt = if (newState) now else null,
                sahurCheckedBy = if (newState) _waliName.value.ifBlank { "Wali Asuh" } else "",
                updatedAt = now
            )
            repository.updateFastingRecord(updated)
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleFastingRecord(getApplication(), updated)
            }
        }
    }

    fun toggleFastingBuka(record: FastingRecord) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newState = !record.isBukaChecked
            val updated = record.copy(
                isBukaChecked = newState,
                bukaCheckedAt = if (newState) now else null,
                bukaCheckedBy = if (newState) _waliName.value.ifBlank { "Wali Asuh" } else "",
                updatedAt = now
            )
            repository.updateFastingRecord(updated)
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.uploadSingleFastingRecord(getApplication(), updated)
            }
        }
    }

    fun deleteFastingRecord(record: FastingRecord) {
        viewModelScope.launch {
            repository.deleteFastingRecord(record)
            if (!isUnitTestEnvironment()) {
                SupabaseSyncManager.deleteRemoteFastingRecord(getApplication(), record.id)
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
            val activeWali = _waliName.value.ifBlank { escortName.ifBlank { "Wali Asuh" } }
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
                notes = notes,
                createdBy = activeWali
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
        additionalNotes: String,
        isInpatient: Boolean = false,
        inpatientRoom: String = ""
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
                isInpatient = isInpatient,
                inpatientRoom = if (isInpatient) inpatientRoom.trim() else "",
                dischargeDate = if (isInpatient) null else record.dischargeDate,
                createdBy = record.createdBy.ifBlank { _waliName.value.ifBlank { record.escortName } },
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
        notes: String = "",
        isInpatient: Boolean = false,
        inpatientRoom: String = ""
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val activeSchool = student.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val activeWali = _waliName.value.ifBlank { escortName.ifBlank { "Wali Asuh" } }
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
                notes = notes,
                isInpatient = isInpatient,
                inpatientRoom = if (isInpatient) inpatientRoom.trim() else "",
                dischargeDate = null,
                createdBy = activeWali
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

    // Edit Rencana Berobat (Terbuka untuk siapapun wali asuh/admin, mencatat editor terakhir)
    fun editTreatmentPlan(
        existingRecord: MedicalRecord,
        student: ChildStudent,
        faskes: String,
        complaint: String,
        planDate: Long,
        departureTime: String,
        escortName: String,
        notes: String
    ) {
        viewModelScope.launch {
            val editor = _waliName.value.ifBlank { escortName.ifBlank { "Wali Asuh" } }
            val updated = existingRecord.copy(
                studentId = student.id,
                studentName = student.name,
                studentDormitory = student.dormitory,
                faskes = faskes,
                complaint = complaint,
                planDate = planDate,
                departureTime = departureTime,
                escortName = escortName,
                notes = notes,
                createdBy = existingRecord.createdBy.ifBlank { existingRecord.escortName },
                editedBy = editor,
                editedAt = System.currentTimeMillis()
            )
            repository.updateRecord(updated)
            if (!isUnitTestEnvironment()) {
                val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                SupabaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
            }
        }
    }

    // Edit Rekam Tindakan / Pengobatan (Terbuka untuk siapapun wali asuh/admin, mencatat editor terakhir)
    fun editCompletedTreatment(
        existingRecord: MedicalRecord,
        student: ChildStudent,
        faskes: String,
        complaint: String,
        diagnosis: String,
        actionsTaken: String,
        prescription: String,
        needsControl: Boolean,
        controlDate: Long?,
        escortName: String,
        notes: String,
        isInpatient: Boolean,
        inpatientRoom: String
    ) {
        viewModelScope.launch {
            val editor = _waliName.value.ifBlank { escortName.ifBlank { "Wali Asuh" } }
            val updated = existingRecord.copy(
                studentId = student.id,
                studentName = student.name,
                studentDormitory = student.dormitory,
                faskes = faskes,
                complaint = complaint,
                diagnosis = diagnosis,
                actionsTaken = actionsTaken,
                prescription = prescription,
                needsControl = needsControl,
                controlDate = if (needsControl) controlDate else null,
                escortName = if (escortName.isBlank()) existingRecord.escortName else escortName,
                notes = notes,
                isInpatient = isInpatient,
                inpatientRoom = if (isInpatient) inpatientRoom.trim() else "",
                dischargeDate = if (isInpatient) null else existingRecord.dischargeDate,
                createdBy = existingRecord.createdBy.ifBlank { existingRecord.escortName },
                editedBy = editor,
                editedAt = System.currentTimeMillis()
            )
            repository.updateRecord(updated)
            if (!isUnitTestEnvironment()) {
                val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                SupabaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
            }
        }
    }

    // Tandai anak asuh sudah pulang / selesai rawat inap
    fun dischargeInpatient(
        record: MedicalRecord,
        updatedDiagnosis: String,
        updatedPrescription: String,
        needsControl: Boolean,
        controlDate: Long?,
        dischargeNotes: String = ""
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val editor = _waliName.value.ifBlank { record.escortName.ifBlank { "Wali Asuh" } }
            val mergedNotes = listOf(record.notes.trim(), dischargeNotes.trim())
                .filter { it.isNotBlank() }
                .joinToString("\n")
            val updated = record.copy(
                isInpatient = false,
                dischargeDate = now,
                diagnosis = updatedDiagnosis.trim().ifBlank { record.diagnosis },
                prescription = updatedPrescription.trim().ifBlank { record.prescription },
                needsControl = needsControl,
                controlDate = if (needsControl) controlDate else null,
                notes = mergedNotes,
                createdBy = record.createdBy.ifBlank { record.escortName },
                editedBy = editor,
                editedAt = now
            )
            repository.updateRecord(updated)
            if (!isUnitTestEnvironment()) {
                val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                SupabaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
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

    fun refreshSuperadminCloudData(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            if (!SupabaseConfig.isConfigured(getApplication())) {
                onResult(false, "Supabase belum terkonfigurasi.")
                return@launch
            }
            _isSyncing.value = true
            try {
                SupabaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
                _isSyncing.value = false
                onResult(true, "Data sekolah, akun admin, & wali terbaru berhasil dimuat dari Supabase.")
            } catch (e: Exception) {
                _isSyncing.value = false
                onResult(false, "Gagal memuat data dari Supabase: ${e.localizedMessage}")
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

        // 1. Cek Kredensial Administrator Resmi (ekowahyudiadmin / SRT1-KDR)
        if (cleanUser.equals("ekowahyudiadmin", ignoreCase = true)) {
            if (!isUnitTestEnvironment() && SupabaseConfig.isConfigured(getApplication())) {
                val existingPilotLocal = repository.getSchoolByAdminUsername("ekowahyudiadmin")
                    ?: repository.getSchoolByCode("SRT1-KDR")
                if (existingPilotLocal == null) {
                    SupabaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
                }
            }
            val pilotOverride = repository.getSchoolByAdminUsername("ekowahyudiadmin")
                ?: repository.getAllSchoolRegistrationsList().firstOrNull {
                    it.generatedAdminUsername.equals("ekowahyudiadmin", ignoreCase = true) ||
                        it.generatedSchoolCode.equals("SRT1-KDR", ignoreCase = true)
                }

            val expectedPilotPassword = pilotOverride?.generatedAdminPassword?.takeIf { it.isNotBlank() }
                ?: "Woyowoyo12@"

            if (pilotOverride?.status == "SUSPENDED") {
                return LoginResult.Error("Maaf akun sedang disuspend oleh admin")
            }

            if (cleanPass == expectedPilotPassword) {
                _currentUserRole.value = UserRole.ADMIN
                _currentUsername.value = "ekowahyudiadmin"
                if (!_schoolProfile.value.tenantCode.equals("SRT1-KDR", ignoreCase = true)) {
                    persistSchoolProfile(SchoolProfile(), markCustomized = false)
                }
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
            return LoginResult.Error("Password untuk Admin 'ekowahyudiadmin' salah. Silakan periksa kembali.")
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
            persistSchoolProfile(buildProfileForSchool(matchedSchool), markCustomized = false)
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
                    persistSchoolProfile(buildProfileForSchool(matchedSchoolWali), markCustomized = false)
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

    // --- Ganti Password Mandiri oleh Admin Sekolah (Username Dikunci, Tersinkron ke Superadmin) ---
    fun updateAdminSelfPassword(
        newPassword: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val cleanPass = newPassword.trim()
            if (cleanPass.length < 6) {
                onResult(false, "Password baru minimal 6 karakter.")
                return@launch
            }

            val activeUsername = _currentUsername.value?.trim().orEmpty()
            val activeTenantCode = _schoolProfile.value.tenantCode.trim().ifBlank { "SRT1-KDR" }

            val allRegistrations = repository.getAllSchoolRegistrationsList()
            val existingReg = when {
                activeUsername.isNotBlank() -> {
                    repository.getSchoolByAdminUsername(activeUsername)
                        ?: allRegistrations.firstOrNull {
                            it.generatedAdminUsername.equals(activeUsername, ignoreCase = true)
                        }
                }
                else -> null
            } ?: repository.getSchoolByCode(activeTenantCode)
                ?: allRegistrations.firstOrNull {
                    it.generatedSchoolCode.equals(activeTenantCode, ignoreCase = true)
                }

            if (existingReg != null) {
                val lockedUsername = existingReg.generatedAdminUsername.ifBlank {
                    activeUsername.ifBlank { "admin_${activeTenantCode.lowercase()}" }
                }
                val updated = existingReg.copy(
                    generatedAdminUsername = lockedUsername,
                    generatedAdminPassword = cleanPass
                )
                repository.updateSchoolRegistration(updated)
                _currentUsername.value = lockedUsername
                if (!isUnitTestEnvironment()) {
                    SupabaseSyncManager.uploadSingleSchoolRegistration(getApplication(), updated)
                }
                onResult(true, "Password admin berhasil diperbarui & tersinkron ke Superadmin!")
            } else if (activeUsername.equals("ekowahyudiadmin", ignoreCase = true) ||
                activeTenantCode.equals("SRT1-KDR", ignoreCase = true)
            ) {
                val profile = _schoolProfile.value
                val lockedUsername = "ekowahyudiadmin"
                val pilotReg = SchoolRegistration(
                    id = SupabaseSyncManager.generateCloudSafeId(),
                    schoolName = profile.schoolName.ifBlank { "Sekolah Rakyat SRT 1 Kabupaten Kediri" },
                    address = profile.address.ifBlank { profile.city.ifBlank { "Kabupaten Kediri" } },
                    city = profile.city.ifBlank { "Kabupaten Kediri" },
                    estimatedWaliCount = waliAccounts.value.count {
                        it.schoolId.equals("SRT1-KDR", ignoreCase = true)
                    },
                    picName = profile.adminName.ifBlank { "Admin Utama" },
                    picEmail = "",
                    picPhone = "-",
                    submissionDate = System.currentTimeMillis(),
                    status = "APPROVED",
                    rejectionReason = "",
                    generatedSchoolCode = "SRT1-KDR",
                    generatedAdminUsername = lockedUsername,
                    generatedAdminPassword = cleanPass,
                    approvedDate = System.currentTimeMillis()
                )
                val insertedId = repository.insertSchoolRegistration(pilotReg)
                val saved = pilotReg.copy(id = insertedId)
                _currentUsername.value = lockedUsername
                if (!isUnitTestEnvironment()) {
                    SupabaseSyncManager.uploadSingleSchoolRegistration(getApplication(), saved)
                }
                onResult(true, "Password admin berhasil diperbarui & tersinkron ke Superadmin!")
            } else {
                onResult(false, "Data akun admin sekolah tidak ditemukan. Silakan sinkronkan data terlebih dahulu.")
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

    /**
     * Hapus akun pribadi Wali Asuh yang sedang login (lokal Room + Supabase).
     * Data siswa & catatan sekolah tetap aman terjaga untuk Admin & Wali lain.
     */
    fun deleteCurrentWaliAccount(
        passwordConfirmation: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val cleanPass = passwordConfirmation.trim()
            if (cleanPass.isBlank()) {
                onResult(false, "Masukkan password akun Anda untuk konfirmasi.")
                return@launch
            }

            val username = _currentUsername.value?.trim().orEmpty()
            var account: WaliAccount? = currentWaliAccount.value
            if (account == null && username.isNotBlank()) {
                account = repository.getWaliAccountByUsername(username)
            }
            if (account == null) {
                val all = repository.getAllWaliAccountsList()
                account = all.firstOrNull {
                    (username.isNotBlank() && it.username.equals(username, ignoreCase = true)) ||
                        it.name.equals(_waliName.value, ignoreCase = true) ||
                        it.username.equals(_waliName.value, ignoreCase = true)
                }
            }

            if (account != null) {
                if (account.password.trim() != cleanPass) {
                    onResult(false, "Password tidak sesuai. Penghapusan akun dibatalkan.")
                    return@launch
                }
                repository.deleteWaliAccount(account)
                if (!isUnitTestEnvironment()) {
                    SupabaseSyncManager.deleteRemoteWaliAccount(getApplication(), account.id)
                }
            }

            _waliName.value = "Wali Asuh"
            logout()
            onResult(true, "Akun Wali Asuh Anda telah dihapus permanen.")
        }
    }

    /**
     * Hapus akun Admin Sekolah & seluruh data tenant sekolah yang sedang aktif (lokal Room + Supabase).
     */
    fun deleteCurrentSchoolAdminAccount(
        passwordConfirmation: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val cleanPass = passwordConfirmation.trim()
            if (cleanPass.isBlank()) {
                onResult(false, "Masukkan password Admin Sekolah untuk konfirmasi.")
                return@launch
            }

            val activeUsername = _currentUsername.value?.trim().orEmpty()
            val activeTenantCode = _schoolProfile.value.tenantCode.trim().ifBlank { "SRT1-KDR" }

            val allRegistrations = repository.getAllSchoolRegistrationsList()
            val existingReg = when {
                activeUsername.isNotBlank() -> {
                    repository.getSchoolByAdminUsername(activeUsername)
                        ?: allRegistrations.firstOrNull {
                            it.generatedAdminUsername.equals(activeUsername, ignoreCase = true)
                        }
                }
                else -> null
            } ?: repository.getSchoolByCode(activeTenantCode)
                ?: allRegistrations.firstOrNull {
                    it.generatedSchoolCode.equals(activeTenantCode, ignoreCase = true)
                }

            val isPilotAdmin = activeUsername.equals("ekowahyudiadmin", ignoreCase = true) ||
                activeTenantCode.equals("SRT1-KDR", ignoreCase = true)

            if (existingReg == null && !isPilotAdmin) {
                onResult(false, "Data akun admin sekolah tidak ditemukan.")
                return@launch
            }

            val expectedPassword = existingReg?.generatedAdminPassword?.takeIf { it.isNotBlank() }
                ?: if (isPilotAdmin) "Woyowoyo12@" else ""

            if (expectedPassword.isNotBlank() && cleanPass != expectedPassword) {
                onResult(false, "Password Admin Sekolah salah. Penghapusan akun dibatalkan.")
                return@launch
            }

            val targetSchoolId = existingReg?.generatedSchoolCode?.takeIf { it.isNotBlank() }
                ?: activeTenantCode

            // 1. Bersihkan seluruh data sekolah di Room lokal (students cascade ke medical_records)
            if (targetSchoolId.isNotBlank()) {
                repository.deleteFastingRecordsBySchool(targetSchoolId)
                repository.deleteFastingSessionsBySchool(targetSchoolId)
                repository.deleteHaidRecordsBySchool(targetSchoolId)
                repository.deleteShiftMessagesBySchool(targetSchoolId)
                repository.deleteStudentsBySchool(targetSchoolId)
                repository.deleteWaliAccountsBySchool(targetSchoolId)
            }

            // 2. Bersihkan seluruh data sekolah + registrasi akun admin di Supabase
            if (!isUnitTestEnvironment()) {
                if (targetSchoolId.isNotBlank()) {
                    SupabaseSyncManager.deleteAllSchoolData(getApplication(), targetSchoolId)
                }
                if (existingReg != null) {
                    SupabaseSyncManager.deleteRemoteSchoolRegistration(getApplication(), existingReg.id)
                }
            }

            // 3. Hapus entri pendaftaran sekolah dari Room lokal
            if (existingReg != null) {
                repository.deleteSchoolRegistration(existingReg)
            }

            // 4. Reset preferensi profil sekolah & sesi login
            schoolPrefs.edit().clear().apply()
            _schoolProfile.value = SchoolProfile()
            _waliName.value = "Wali Asuh"
            logout()
            onResult(true, "Akun Admin Sekolah dan seluruh data sekolah berhasil dihapus permanen.")
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

