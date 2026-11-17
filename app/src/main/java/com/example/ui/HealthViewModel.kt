package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.DashboardBannerConfig
import com.example.data.model.DashboardBannerItem
import com.example.data.model.DashboardIconKey
import com.example.data.model.DeletionLog
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.KonselingHelper
import com.example.data.model.KonselingLog
import com.example.data.model.KonselingRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentMessage
import com.example.data.model.SchoolProfile
import com.example.data.model.SchoolRegistration
import com.example.data.model.ShiftMessage
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganLog
import com.example.data.model.TabunganTransaction
import com.example.data.model.UploadedBannerMedia
import com.example.data.model.VisitHelper
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.WaliAccount
import com.example.data.remote.pocketbase.PbSchoolProfileDto
import com.example.data.remote.pocketbase.PocketBaseConfig
import com.example.data.remote.pocketbase.PocketBaseSyncManager
import com.example.data.repository.HealthRepository
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import com.example.util.PasswordGenerator
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SwitchableSchoolOption(
    val tenantCode: String,
    val schoolName: String,
    val city: String,
    val isDefaultPilot: Boolean = false
)

enum class UserRole {
    NOT_LOGGED_IN,
    WALI,
    GURU_NGAJI,
    ADMIN,
    SUPERADMIN,
    PARENT
}

sealed class LoginResult {
    object SuccessSuperAdmin : LoginResult()
    object SuccessAdmin : LoginResult()
    data class SuccessWali(val account: WaliAccount) : LoginResult()
    data class SuccessGuruNgaji(val account: WaliAccount) : LoginResult()
    data class SuccessParent(val link: ParentAccessLink) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HealthRepository
    private val authPrefs = application.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE)
    private val notifPrefs = application.getSharedPreferences("waliasuh_notif_prefs", Context.MODE_PRIVATE)

    init {
        AppLanguageManager.init(application)
        val db = AppDatabase.getDatabase(application)
        repository = HealthRepository(
            db.childStudentDao(),
            db.medicalRecordDao(),
            db.waliAccountDao(),
            db.schoolRegistrationDao(),
            db.shiftMessageDao(),
            db.haidRecordDao(),
            db.fastingSessionDao(),
            db.fastingRecordDao(),
            db.mengajiRecordDao(),
            db.visitSessionDao(),
            db.visitRecordDao(),
            db.parentAccessLinkDao(),
            db.parentMessageDao(),
            db.deletionLogDao(),
            db.tabunganTransactionDao(),
            db.tabunganLogDao(),
            db.konselingRecordDao(),
            db.konselingLogDao()
        )
    }

    private val _appLanguage = MutableStateFlow(AppLanguageManager.init(application))
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    fun setAppLanguage(language: AppLanguage) {
        AppLanguageManager.setLanguage(getApplication(), language)
        _appLanguage.value = language
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
            "GURU_NGAJI" -> UserRole.GURU_NGAJI
            "PARENT" -> UserRole.PARENT
            else -> UserRole.NOT_LOGGED_IN
        }
    )
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _currentParentStudentId = MutableStateFlow(authPrefs.getLong("saved_parent_student_id", 0L))
    val currentParentStudentId: StateFlow<Long> = _currentParentStudentId.asStateFlow()

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

    val isAnakSuperAdmin: StateFlow<Boolean> = combine(
        _currentUserRole,
        _currentUsername
    ) { role, username ->
        role == UserRole.WALI && username.equals("anaksuperadmin", ignoreCase = true)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        _currentUserRole.value == UserRole.WALI &&
            _currentUsername.value.equals("anaksuperadmin", ignoreCase = true)
    )

    // Global Dashboard Icon Layout Preferences (Synced with PocketBase across all Wali Asuh)
    private val layoutPrefs = application.getSharedPreferences("waliasuh_dashboard_layout_prefs", Context.MODE_PRIVATE)
    private val _dashboardIconOrder = MutableStateFlow(loadSavedDashboardIconOrder())
    val dashboardIconOrder: StateFlow<List<DashboardIconKey>> = _dashboardIconOrder.asStateFlow()

    private val _isSavingDashboardLayout = MutableStateFlow(false)
    val isSavingDashboardLayout: StateFlow<Boolean> = _isSavingDashboardLayout.asStateFlow()

    // Interactive Coach Marks Tour State (Muncul otomatis sekali bagi Wali baru, bisa diputar ulang kapan saja)
    private val tourPrefs = application.getSharedPreferences("waliasuh_tour_prefs", Context.MODE_PRIVATE)
    private val _showDashboardTour = MutableStateFlow(false)
    val showDashboardTour: StateFlow<Boolean> = _showDashboardTour.asStateFlow()

    fun maybeStartDashboardTourOnWaliEntry() {
        if (isUnitTestEnvironment()) return
        if (_currentUserRole.value != UserRole.WALI) return
        if (!tourPrefs.getBoolean("dashboard_tour_v1_done", false)) {
            _showDashboardTour.value = true
        }
    }

    fun startDashboardTour() {
        _showDashboardTour.value = true
    }

    fun dismissDashboardTour(markCompleted: Boolean = true) {
        _showDashboardTour.value = false
        if (markCompleted) {
            tourPrefs.edit().putBoolean("dashboard_tour_v1_done", true).apply()
        }
    }

    fun isDashboardTourCompleted(): Boolean {
        return tourPrefs.getBoolean("dashboard_tour_v1_done", false)
    }

    private fun loadSavedDashboardIconOrder(): List<DashboardIconKey> {
        val raw = layoutPrefs.getString("icon_order_json", null)
        return DashboardIconKey.parseJson(raw) ?: DashboardIconKey.DEFAULT_ORDER
    }

    private fun getDashboardLayoutUpdatedAt(): Long {
        return layoutPrefs.getLong("icon_order_updated_at", 0L)
    }

    private fun persistDashboardIconOrder(order: List<DashboardIconKey>, updatedAt: Long) {
        val normalized = DashboardIconKey.normalizeOrder(order.map { it.key })
        _dashboardIconOrder.value = normalized
        layoutPrefs.edit()
            .putString("icon_order_json", DashboardIconKey.serializeJson(normalized))
            .putLong("icon_order_updated_at", updatedAt)
            .apply()
    }

    fun applyRemoteDashboardIconOrder(order: List<DashboardIconKey>, updatedAt: Long) {
        val localTs = getDashboardLayoutUpdatedAt()
        if (updatedAt >= localTs) {
            persistDashboardIconOrder(order, updatedAt)
        }
    }

    fun syncDashboardIconLayoutNow() {
        if (isUnitTestEnvironment() || !PocketBaseConfig.isConfigured(getApplication())) return
        viewModelScope.launch {
            PocketBaseSyncManager.syncDashboardIconLayoutQuietly(
                context = getApplication(),
                localOrder = _dashboardIconOrder.value,
                localUpdatedAt = getDashboardLayoutUpdatedAt(),
                allowPushToCloud = isAnakSuperAdmin.value,
                onRemoteLayoutUpdated = { remoteOrder, remoteTs ->
                    applyRemoteDashboardIconOrder(remoteOrder, remoteTs)
                }
            )
        }
    }

    fun saveDashboardIconOrder(
        newOrder: List<DashboardIconKey>,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
        if (!isAnakSuperAdmin.value) {
            onResult(
                false,
                if (isEn) {
                    "Only anaksuperadmin can change the dashboard icon layout."
                } else {
                    "Hanya akun anaksuperadmin yang dapat mengubah tata letak ikon dashboard."
                }
            )
            return
        }
        val normalized = DashboardIconKey.normalizeOrder(newOrder.map { it.key })
        val now = System.currentTimeMillis()
        persistDashboardIconOrder(normalized, now)

        if (isUnitTestEnvironment()) {
            onResult(
                true,
                if (isEn) "Dashboard icon layout saved!" else "Tata letak ikon dashboard berhasil disimpan!"
            )
            return
        }

        viewModelScope.launch {
            _isSavingDashboardLayout.value = true
            val res = PocketBaseSyncManager.uploadDashboardIconLayout(
                context = getApplication(),
                order = normalized,
                updatedAt = now
            )
            _isSavingDashboardLayout.value = false
            if (res.isSuccess) {
                onResult(
                    true,
                    if (isEn) {
                        "Layout saved & synced to PocketBase for all Wali Asuh!"
                    } else {
                        "Tata letak disimpan & disinkronkan ke PocketBase untuk semua Wali Asuh!"
                    }
                )
            } else {
                val errDetail = res.exceptionOrNull()?.message.orEmpty()
                onResult(
                    false,
                    if (isEn) {
                        "Saved locally, but PocketBase sync failed: $errDetail"
                    } else {
                        "Tersimpan di perangkat, namun sinkronisasi PocketBase gagal: $errDetail"
                    }
                )
            }
        }
    }

    fun resetDashboardIconOrder(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        saveDashboardIconOrder(DashboardIconKey.DEFAULT_ORDER, onResult)
    }

    // Global Dashboard Banner Carousel Preferences (Synced via PocketBase + Cloudflare R2 CDN across all Wali Asuh)
    private val bannerPrefs = application.getSharedPreferences("waliasuh_dashboard_banner_prefs", Context.MODE_PRIVATE)
    private val _dashboardBannerConfig = MutableStateFlow(loadSavedDashboardBannerConfig())
    val dashboardBannerConfig: StateFlow<DashboardBannerConfig> = _dashboardBannerConfig.asStateFlow()

    private val _isSavingDashboardBanner = MutableStateFlow(false)
    val isSavingDashboardBanner: StateFlow<Boolean> = _isSavingDashboardBanner.asStateFlow()

    private val _isUploadingBannerImage = MutableStateFlow(false)
    val isUploadingBannerImage: StateFlow<Boolean> = _isUploadingBannerImage.asStateFlow()

    private fun loadSavedDashboardBannerConfig(): DashboardBannerConfig {
        val raw = bannerPrefs.getString("banner_config_json", null)
        return DashboardBannerConfig.fromJsonString(raw) ?: DashboardBannerConfig.defaultConfig()
    }

    private fun persistDashboardBannerConfig(config: DashboardBannerConfig) {
        val normalized = config.normalized()
        _dashboardBannerConfig.value = normalized
        bannerPrefs.edit()
            .putString("banner_config_json", normalized.toJsonString())
            .apply()
    }

    fun applyRemoteDashboardBannerConfig(remoteConfig: DashboardBannerConfig) {
        val localTs = _dashboardBannerConfig.value.updatedAt
        if (remoteConfig.updatedAt >= localTs) {
            persistDashboardBannerConfig(remoteConfig)
        }
    }

    fun syncDashboardBannersNow() {
        if (isUnitTestEnvironment() || !PocketBaseConfig.isConfigured(getApplication())) return
        viewModelScope.launch {
            PocketBaseSyncManager.syncDashboardBannerConfigQuietly(
                context = getApplication(),
                localConfig = _dashboardBannerConfig.value,
                allowPushToCloud = isAnakSuperAdmin.value,
                onRemoteBannerUpdated = { remoteConfig ->
                    applyRemoteDashboardBannerConfig(remoteConfig)
                }
            )
        }
    }

    fun uploadBannerImageToCloud(
        imageUri: Uri,
        bannerId: String,
        title: String,
        onResult: (Boolean, String, UploadedBannerMedia?) -> Unit
    ) {
        val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
        if (!isAnakSuperAdmin.value) {
            onResult(
                false,
                if (isEn) {
                    "Only anaksuperadmin can upload banner images."
                } else {
                    "Hanya akun anaksuperadmin yang dapat mengunggah gambar banner."
                },
                null
            )
            return
        }
        if (isUnitTestEnvironment()) {
            val mockMedia = UploadedBannerMedia(
                pbRecordId = "testrecord00001",
                pbCollectionId = "dashboard_banners",
                pbFilename = "banner_test.jpg",
                cdnImageUrl = DashboardBannerItem.buildR2CdnUrl("dashboard_banners", "testrecord00001", "banner_test.jpg"),
                fallbackApiFileUrl = DashboardBannerItem.buildPbApiFileUrl(
                    DashboardBannerItem.DEFAULT_POCKETBASE_BASE_URL,
                    "dashboard_banners",
                    "testrecord00001",
                    "banner_test.jpg"
                )
            )
            onResult(true, "CDN Ready", mockMedia)
            return
        }
        viewModelScope.launch {
            _isUploadingBannerImage.value = true
            val res = PocketBaseSyncManager.uploadBannerImageToCloud(
                context = getApplication(),
                imageUri = imageUri,
                bannerId = bannerId,
                title = title
            )
            _isUploadingBannerImage.value = false
            if (res.isSuccess) {
                val media = res.getOrNull()
                val msg = when (media?.mediaType) {
                    "VIDEO" -> if (isEn) {
                        "Video uploaded to R2 CDN!"
                    } else {
                        "Video banner diunggah ke R2 CDN (${DashboardBannerItem.R2_CDN_BASE_URL})!"
                    }
                    "GIF" -> if (isEn) {
                        "Animated GIF uploaded to R2 CDN!"
                    } else {
                        "GIF animasi diunggah ke R2 CDN (${DashboardBannerItem.R2_CDN_BASE_URL})!"
                    }
                    else -> if (isEn) {
                        "Image uploaded to R2 CDN!"
                    } else {
                        "Gambar diunggah ke R2 CDN (${DashboardBannerItem.R2_CDN_BASE_URL})!"
                    }
                }
                onResult(true, msg, media)
            } else {
                onResult(
                    false,
                    res.exceptionOrNull()?.message
                        ?: if (isEn) "Failed to upload banner media." else "Gagal mengunggah media banner.",
                    null
                )
            }
        }
    }

    fun saveDashboardBannerConfig(
        newConfig: DashboardBannerConfig,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
        if (!isAnakSuperAdmin.value) {
            onResult(
                false,
                if (isEn) {
                    "Only anaksuperadmin can modify dashboard banners."
                } else {
                    "Hanya akun anaksuperadmin yang dapat mengatur banner dashboard."
                }
            )
            return
        }
        val stamped = newConfig.copy(updatedAt = System.currentTimeMillis()).normalized()
        persistDashboardBannerConfig(stamped)

        if (isUnitTestEnvironment()) {
            onResult(
                true,
                if (isEn) "Dashboard banners saved!" else "Pengaturan banner dashboard berhasil disimpan!"
            )
            return
        }

        viewModelScope.launch {
            _isSavingDashboardBanner.value = true
            val res = PocketBaseSyncManager.uploadDashboardBannerConfig(
                context = getApplication(),
                config = stamped
            )
            _isSavingDashboardBanner.value = false
            if (res.isSuccess) {
                onResult(
                    true,
                    if (isEn) {
                        "Global banners & timer synced via PocketBase!"
                    } else {
                        "Banner global & durasi geser disinkronkan ke semua Wali Asuh!"
                    }
                )
            } else {
                val errDetail = res.exceptionOrNull()?.message.orEmpty()
                onResult(
                    false,
                    if (isEn) {
                        "Saved locally, but PocketBase sync failed: $errDetail"
                    } else {
                        "Tersimpan di perangkat, namun sinkronisasi PocketBase gagal: $errDetail"
                    }
                )
            }
        }
    }

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

    private suspend fun runUnifiedSchoolCloudSync(schoolId: String): Result<String> {
        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        return PocketBaseSyncManager.syncAllSchoolData(
            context = getApplication(),
            schoolId = cleanSchoolId,
            repository = repository,
            localSchoolProfile = _schoolProfile.value,
            localProfileUpdatedAt = getProfileUpdatedAt(cleanSchoolId),
            onRemoteSchoolProfileUpdated = { remoteProfile, remoteTs ->
                applyRemoteSchoolProfile(remoteProfile, remoteTs)
            },
            localDashboardIconOrder = _dashboardIconOrder.value,
            localDashboardLayoutUpdatedAt = getDashboardLayoutUpdatedAt(),
            allowPushDashboardLayout = isAnakSuperAdmin.value,
            onRemoteDashboardLayoutUpdated = { remoteOrder, remoteTs ->
                applyRemoteDashboardIconOrder(remoteOrder, remoteTs)
            },
            localDashboardBannerConfig = _dashboardBannerConfig.value,
            allowPushDashboardBanners = isAnakSuperAdmin.value,
            onRemoteDashboardBannerUpdated = { remoteBannerConfig ->
                applyRemoteDashboardBannerConfig(remoteBannerConfig)
            },
            onNewRemoteMedicalRecords = { newRecords, schoolStudents, syncedSchoolId ->
                processIncomingRemoteMedicalRecords(newRecords, schoolStudents, syncedSchoolId)
            },
            onNewRemoteDeletionLogs = { newLogs, syncedSchoolId ->
                processIncomingRemoteDeletionLogs(newLogs, syncedSchoolId)
            }
        )
    }

    init {
        viewModelScope.launch {
            sanitizeExistingIdentityDataInRoom()
        }
        if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(application)) {
            when (_currentUserRole.value) {
                UserRole.ADMIN, UserRole.WALI, UserRole.PARENT -> {
                    val activeCode = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                    viewModelScope.launch {
                        PocketBaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
                        runUnifiedSchoolCloudSync(activeCode)
                    }
                }
                UserRole.SUPERADMIN -> {
                    viewModelScope.launch {
                        PocketBaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
                    }
                }
                else -> Unit
            }
            startMultiTenantForegroundSyncLoop()
        }
    }

    private fun startMultiTenantForegroundSyncLoop() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(45_000L)
                val role = _currentUserRole.value
                if (role != UserRole.ADMIN && role != UserRole.WALI && role != UserRole.PARENT) continue
                if (_isSyncing.value) continue
                if (!PocketBaseConfig.isConfigured(getApplication())) continue
                val activeCode = _schoolProfile.value.tenantCode.trim().ifBlank { "SRT1-KDR" }
                runCatching {
                    runUnifiedSchoolCloudSync(activeCode)
                }
            }
        }
    }

    /**
     * Membersihkan data NIK / No. KK lama yang terlanjur menyimpan karakter sampah (misal â▯▯)
     * di database lokal Room, lalu menyinkronkan versi bersihnya kembali ke cloud bila aktif.
     */
    suspend fun sanitizeExistingIdentityDataInRoom() {
        val allStudents = repository.getAllStudentsList()
        val dirtyStudents = allStudents.mapNotNull { student ->
            val cleanNik = com.example.util.StudentCsvParser.cleanDigitsOnly(student.nik)
            val cleanKk = com.example.util.StudentCsvParser.cleanDigitsOnly(student.noKk)
            if (cleanNik != student.nik || cleanKk != student.noKk) {
                student.copy(nik = cleanNik, noKk = cleanKk)
            } else null
        }
        if (dirtyStudents.isNotEmpty()) {
            repository.insertAllStudents(dirtyStudents)
            if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                PocketBaseSyncManager.uploadStudents(getApplication(), dirtyStudents)
            }
        }

        val allHaid = repository.getAllHaidRecordsList()
        val dirtyHaid = allHaid.mapNotNull { rec ->
            val cleanNik = com.example.util.StudentCsvParser.cleanDigitsOnly(rec.studentNik)
            if (cleanNik != rec.studentNik) rec.copy(studentNik = cleanNik) else null
        }
        if (dirtyHaid.isNotEmpty()) {
            repository.insertAllHaidRecords(dirtyHaid)
            if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                dirtyHaid.forEach { PocketBaseSyncManager.uploadSingleHaidRecord(getApplication(), it) }
            }
        }

        val allFasting = repository.getAllFastingRecordsList()
        val dirtyFasting = allFasting.mapNotNull { rec ->
            val cleanNik = com.example.util.StudentCsvParser.cleanDigitsOnly(rec.studentNik)
            if (cleanNik != rec.studentNik) rec.copy(studentNik = cleanNik) else null
        }
        if (dirtyFasting.isNotEmpty()) {
            repository.insertAllFastingRecords(dirtyFasting)
            if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                dirtyFasting.forEach { PocketBaseSyncManager.uploadSingleFastingRecord(getApplication(), it) }
            }
        }
    }

    private fun profileUpdatedAtKey(tenantCode: String): String {
        val clean = tenantCode.trim().uppercase().ifBlank { "SRT1-KDR" }
        return "profile_updated_at_$clean"
    }

    private fun getProfileUpdatedAt(tenantCode: String): Long {
        return schoolPrefs.getLong(profileUpdatedAtKey(tenantCode), 0L)
    }

    private val _remoteSchoolProfiles = MutableStateFlow<Map<String, Pair<SchoolProfile, Long>>>(emptyMap())
    private val _isSwitchingSchool = MutableStateFlow(false)
    val isSwitchingSchool: StateFlow<Boolean> = _isSwitchingSchool.asStateFlow()

    private fun applyRemoteSchoolProfile(profile: SchoolProfile, updatedAt: Long) {
        val codeUpper = profile.tenantCode.trim().uppercase()
        if (codeUpper.isNotBlank() &&
            codeUpper != PbSchoolProfileDto.GLOBAL_DASHBOARD_LAYOUT_SCHOOL_ID &&
            codeUpper != PbSchoolProfileDto.GLOBAL_DASHBOARD_BANNER_SCHOOL_ID
        ) {
            _remoteSchoolProfiles.value = _remoteSchoolProfiles.value.toMutableMap().apply {
                put(codeUpper, profile.copy(tenantCode = codeUpper) to updatedAt)
            }
        }
        persistSchoolProfile(profile, markCustomized = true, updatedAt = updatedAt)
    }

    private fun persistSchoolProfile(
        profile: SchoolProfile,
        markCustomized: Boolean,
        updatedAt: Long = getProfileUpdatedAt(profile.tenantCode)
    ) {
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
            if (updatedAt > 0L) {
                putLong(profileUpdatedAtKey(profile.tenantCode), updatedAt)
            }
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
        val now = System.currentTimeMillis()
        val cleanProfile = profile.copy(
            schoolName = profile.schoolName.trim(),
            shortName = SchoolProfile.sanitizeShortName(profile.schoolName, profile.shortName),
            adminName = profile.adminName.trim().ifBlank { "Admin Utama" },
            address = profile.address.trim(),
            city = profile.city.trim().ifBlank { "Indonesia" }
        )
        persistSchoolProfile(cleanProfile, markCustomized = true, updatedAt = now)
        if (_currentUserRole.value == UserRole.ADMIN && cleanProfile.adminName.isNotBlank()) {
            _waliName.value = cleanProfile.adminName
            if (authPrefs.getString("saved_role", "") == "ADMIN") {
                authPrefs.edit().putString("saved_wali_name", cleanProfile.adminName).apply()
            }
        }
        if (cleanProfile.tenantCode.isNotBlank()) {
            viewModelScope.launch {
                if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                    PocketBaseSyncManager.uploadSchoolProfile(getApplication(), cleanProfile, now)
                }
                val existingReg = repository.getSchoolByCode(cleanProfile.tenantCode)
                if (existingReg != null) {
                    val updatedReg = existingReg.copy(
                        schoolName = cleanProfile.schoolName,
                        address = cleanProfile.address.ifBlank { existingReg.address },
                        city = cleanProfile.city,
                        picName = cleanProfile.adminName
                    )
                    repository.updateSchoolRegistration(updatedReg)
                    if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                        PocketBaseSyncManager.uploadSingleSchoolRegistration(getApplication(), updatedReg)
                    }
                }
            }
        }
        onComplete()
    }

    val schoolRegistrations: StateFlow<List<SchoolRegistration>> = repository.allSchoolRegistrations
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val availableSchoolsForSwitch: StateFlow<List<SwitchableSchoolOption>> = combine(
        schoolRegistrations,
        _remoteSchoolProfiles,
        _schoolProfile
    ) { registrations, remoteProfiles, currentActiveProfile ->
        val blockedStatuses = setOf("PENDING", "REJECTED", "SUSPENDED")
        val blockedCodes = registrations
            .filter { it.status.uppercase() in blockedStatuses && it.generatedSchoolCode.isNotBlank() }
            .map { it.generatedSchoolCode.trim().uppercase() }
            .toSet()

        val optionsMap = linkedMapOf<String, SwitchableSchoolOption>()
        val activeUpper = currentActiveProfile.tenantCode.trim().uppercase().ifBlank { "SRT1-KDR" }

        // 1. Sekolah pilot default (SRT1-KDR) selalu tersedia sebagai opsi dasar
        val pilotRemote = remoteProfiles["SRT1-KDR"]?.first
        val pilotName = when {
            activeUpper == "SRT1-KDR" && currentActiveProfile.schoolName.isNotBlank() -> currentActiveProfile.schoolName
            pilotRemote != null && pilotRemote.schoolName.isNotBlank() -> pilotRemote.schoolName
            else -> "Sekolah Rakyat SRT 1 Kabupaten Kediri"
        }
        val pilotCity = when {
            activeUpper == "SRT1-KDR" && currentActiveProfile.city.isNotBlank() -> currentActiveProfile.city
            pilotRemote != null && pilotRemote.city.isNotBlank() -> pilotRemote.city
            else -> "Kabupaten Kediri"
        }
        optionsMap["SRT1-KDR"] = SwitchableSchoolOption(
            tenantCode = "SRT1-KDR",
            schoolName = pilotName,
            city = pilotCity,
            isDefaultPilot = true
        )

        // 2. Semua sekolah yang berstatus APPROVED dari pendaftaran
        registrations
            .filter {
                it.status.equals("APPROVED", ignoreCase = true) &&
                    it.generatedSchoolCode.isNotBlank() &&
                    !it.generatedSchoolCode.equals(PbSchoolProfileDto.GLOBAL_DASHBOARD_LAYOUT_SCHOOL_ID, ignoreCase = true) &&
                    !it.generatedSchoolCode.equals(PbSchoolProfileDto.GLOBAL_DASHBOARD_BANNER_SCHOOL_ID, ignoreCase = true)
            }
            .forEach { reg ->
                val codeUpper = reg.generatedSchoolCode.trim().uppercase()
                val remoteForCode = remoteProfiles[codeUpper]?.first
                val resolvedName = when {
                    activeUpper == codeUpper && currentActiveProfile.schoolName.isNotBlank() -> currentActiveProfile.schoolName
                    remoteForCode != null && remoteForCode.schoolName.isNotBlank() -> remoteForCode.schoolName
                    else -> reg.schoolName.trim().ifBlank { codeUpper }
                }
                val resolvedCity = when {
                    activeUpper == codeUpper && currentActiveProfile.city.isNotBlank() -> currentActiveProfile.city
                    remoteForCode != null && remoteForCode.city.isNotBlank() -> remoteForCode.city
                    else -> reg.city.trim().ifBlank { "Indonesia" }
                }
                optionsMap[codeUpper] = SwitchableSchoolOption(
                    tenantCode = codeUpper,
                    schoolName = resolvedName,
                    city = resolvedCity,
                    isDefaultPilot = codeUpper == "SRT1-KDR"
                )
            }

        // 3. Profil sekolah jarak jauh dari PocketBase yang mungkin belum ada di Room registrations
        remoteProfiles.forEach { (codeUpper, pair) ->
            if (codeUpper !in blockedCodes &&
                codeUpper != PbSchoolProfileDto.GLOBAL_DASHBOARD_LAYOUT_SCHOOL_ID &&
                codeUpper != PbSchoolProfileDto.GLOBAL_DASHBOARD_BANNER_SCHOOL_ID &&
                !optionsMap.containsKey(codeUpper)
            ) {
                val prof = pair.first
                optionsMap[codeUpper] = SwitchableSchoolOption(
                    tenantCode = codeUpper,
                    schoolName = prof.schoolName.ifBlank { codeUpper },
                    city = prof.city.ifBlank { "Indonesia" },
                    isDefaultPilot = codeUpper == "SRT1-KDR"
                )
            }
        }

        // 4. Pastikan sekolah aktif saat ini selalu masuk dalam opsi bila valid
        if (activeUpper !in blockedCodes &&
            activeUpper != PbSchoolProfileDto.GLOBAL_DASHBOARD_LAYOUT_SCHOOL_ID &&
            activeUpper != PbSchoolProfileDto.GLOBAL_DASHBOARD_BANNER_SCHOOL_ID &&
            !optionsMap.containsKey(activeUpper)
        ) {
            optionsMap[activeUpper] = SwitchableSchoolOption(
                tenantCode = activeUpper,
                schoolName = currentActiveProfile.schoolName.ifBlank { activeUpper },
                city = currentActiveProfile.city.ifBlank { "Indonesia" },
                isDefaultPilot = activeUpper == "SRT1-KDR"
            )
        }

        optionsMap.values.sortedWith(
            compareByDescending<SwitchableSchoolOption> { it.isDefaultPilot }
                .thenBy { it.schoolName.lowercase() }
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun refreshAvailableSchoolsForSwitch(onComplete: (Boolean) -> Unit = {}) {
        if (!isAnakSuperAdmin.value) {
            onComplete(false)
            return
        }
        if (isUnitTestEnvironment() || !PocketBaseConfig.isConfigured(getApplication())) {
            onComplete(true)
            return
        }
        viewModelScope.launch {
            _isSwitchingSchool.value = true
            try {
                PocketBaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
                val remoteList = PocketBaseSyncManager.fetchAllRemoteSchoolProfiles(getApplication())
                if (remoteList.isNotEmpty()) {
                    val updatedMap = _remoteSchoolProfiles.value.toMutableMap()
                    remoteList.forEach { (prof, ts) ->
                        val codeUpper = prof.tenantCode.trim().uppercase()
                        if (codeUpper.isNotBlank() &&
                            codeUpper != PbSchoolProfileDto.GLOBAL_DASHBOARD_LAYOUT_SCHOOL_ID &&
                            codeUpper != PbSchoolProfileDto.GLOBAL_DASHBOARD_BANNER_SCHOOL_ID
                        ) {
                            updatedMap[codeUpper] = prof.copy(tenantCode = codeUpper) to ts
                        }
                    }
                    _remoteSchoolProfiles.value = updatedMap
                }
                onComplete(true)
            } catch (_: Exception) {
                onComplete(false)
            } finally {
                _isSwitchingSchool.value = false
            }
        }
    }

    fun switchActiveSchoolForAnakSuperAdmin(
        targetSchoolCode: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
        if (!isAnakSuperAdmin.value) {
            onResult(
                false,
                if (isEn) {
                    "Only anaksuperadmin can switch active schools."
                } else {
                    "Hanya akun anaksuperadmin yang dapat berpindah sekolah aktif."
                }
            )
            return
        }
        val cleanCode = targetSchoolCode.trim().uppercase()
        if (cleanCode.isBlank() ||
            cleanCode == PbSchoolProfileDto.GLOBAL_DASHBOARD_LAYOUT_SCHOOL_ID ||
            cleanCode == PbSchoolProfileDto.GLOBAL_DASHBOARD_BANNER_SCHOOL_ID
        ) {
            onResult(
                false,
                if (isEn) "Invalid target school code." else "Kode sekolah tujuan tidak valid."
            )
            return
        }

        viewModelScope.launch {
            val matchingReg = repository.getSchoolByCode(cleanCode)
                ?: repository.getAllSchoolRegistrationsList().firstOrNull {
                    it.generatedSchoolCode.equals(cleanCode, ignoreCase = true)
                }

            if (matchingReg != null && !matchingReg.status.equals("APPROVED", ignoreCase = true)) {
                onResult(
                    false,
                    if (isEn) {
                        "School $cleanCode is not active (Status: ${matchingReg.status})."
                    } else {
                        "Sekolah $cleanCode belum berstatus aktif (Status: ${matchingReg.status})."
                    }
                )
                return@launch
            }

            val remotePair = _remoteSchoolProfiles.value[cleanCode]
            val current = _schoolProfile.value
            val (resolvedProfile, resolvedUpdatedAt) = when {
                current.tenantCode.equals(cleanCode, ignoreCase = true) -> {
                    current.copy(tenantCode = cleanCode) to getProfileUpdatedAt(cleanCode)
                }
                remotePair != null -> {
                    remotePair.first.copy(tenantCode = cleanCode) to maxOf(remotePair.second, getProfileUpdatedAt(cleanCode))
                }
                matchingReg != null -> {
                    buildProfileForSchool(matchingReg).copy(tenantCode = cleanCode) to getProfileUpdatedAt(cleanCode)
                }
                cleanCode == "SRT1-KDR" -> {
                    SchoolProfile() to getProfileUpdatedAt("SRT1-KDR")
                }
                else -> {
                    val option = availableSchoolsForSwitch.value.firstOrNull {
                        it.tenantCode.equals(cleanCode, ignoreCase = true)
                    }
                    SchoolProfile(
                        schoolName = option?.schoolName?.ifBlank { cleanCode } ?: cleanCode,
                        shortName = option?.schoolName?.ifBlank { cleanCode } ?: cleanCode,
                        city = option?.city?.ifBlank { "Indonesia" } ?: "Indonesia",
                        tenantCode = cleanCode
                    ) to getProfileUpdatedAt(cleanCode)
                }
            }

            persistSchoolProfile(resolvedProfile, markCustomized = false, updatedAt = resolvedUpdatedAt)

            if (isUnitTestEnvironment() || !PocketBaseConfig.isConfigured(getApplication())) {
                onResult(
                    true,
                    if (isEn) {
                        "Switched to ${resolvedProfile.schoolName} ($cleanCode). Student data & activities now follow this school."
                    } else {
                        "Beralih ke ${resolvedProfile.schoolName} ($cleanCode). Data siswa & aktivitas kini masuk ke sekolah ini."
                    }
                )
                return@launch
            }

            _isSwitchingSchool.value = true
            _isSyncing.value = true
            val syncRes = runUnifiedSchoolCloudSync(cleanCode)
            _isSyncing.value = false
            _isSwitchingSchool.value = false

            val syncedName = _schoolProfile.value.schoolName.ifBlank { resolvedProfile.schoolName }
            if (syncRes.isSuccess) {
                val msg = if (isEn) {
                    "Switched to $syncedName ($cleanCode) & synced with PocketBase!"
                } else {
                    "Berhasil masuk ke $syncedName ($cleanCode) & tersinkron PocketBase!"
                }
                _cloudSyncMessage.value = msg
                onResult(true, msg)
            } else {
                val msg = if (isEn) {
                    "Switched to $syncedName ($cleanCode) locally (cloud sync pending)."
                } else {
                    "Beralih ke $syncedName ($cleanCode) di perangkat (sinkronisasi cloud tertunda)."
                }
                onResult(true, msg)
            }
        }
    }

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

    // Akun Wali Asuh yang sedang aktif login di sesi aplikasi saat ini (terisolasi per sekolah aktif)
    val currentWaliAccount: StateFlow<WaliAccount?> = combine(
        waliAccounts,
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
                                PocketBaseSyncManager.uploadSingleHaidRecord(getApplication(), updated)
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
            val assignedId = if (record.id > 0L) record.id else PocketBaseSyncManager.generateCloudSafeId()
            val stamped = record.copy(id = assignedId, schoolId = activeSchool)
            repository.insertHaidRecord(stamped)
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleHaidRecord(getApplication(), stamped)
            }
        }
    }

    fun updateHaidRecord(record: HaidRecord, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val updated = record.copy(updatedAt = System.currentTimeMillis())
            repository.updateHaidRecord(updated)
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleHaidRecord(getApplication(), updated)
            }
        }
    }

    fun completeHaidRecord(id: Long, endDate: Long = System.currentTimeMillis(), onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.completeHaidRecord(id, endDate, "SUCI")
            onComplete()
            if (!isUnitTestEnvironment()) {
                repository.getHaidRecordById(id)?.let { updated ->
                    PocketBaseSyncManager.uploadSingleHaidRecord(getApplication(), updated)
                }
            }
        }
    }

    fun deleteHaidRecord(record: HaidRecord, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteHaidRecord(record)
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.deleteRemoteHaidRecord(getApplication(), record.id)
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
        val role = _currentUserRole.value
        if (role == UserRole.NOT_LOGGED_IN) {
            onComplete(null)
            return
        }
        viewModelScope.launch {
            val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            val now = System.currentTimeMillis()
            val creator = when (role) {
                UserRole.ADMIN, UserRole.SUPERADMIN ->
                    _schoolProfile.value.adminName.ifBlank { _waliName.value.ifBlank { "Admin Sekolah" } }
                UserRole.WALI, UserRole.GURU_NGAJI ->
                    _waliName.value.ifBlank { "Wali Asuh" }
                UserRole.PARENT, UserRole.NOT_LOGGED_IN -> "Wali Asuh"
            }
            val newSession = FastingSession(
                id = PocketBaseSyncManager.generateCloudSafeId(),
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
                PocketBaseSyncManager.uploadSingleFastingSession(getApplication(), newSession)
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
                PocketBaseSyncManager.uploadSingleFastingSession(getApplication(), updated)
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
                PocketBaseSyncManager.deleteRemoteFastingSession(getApplication(), session.id)
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
            val studentCleanNik = com.example.util.StudentCsvParser.cleanDigitsOnly(student.nik)
            val alreadyRegistered = existingInSession.any { rec ->
                val recCleanNik = com.example.util.StudentCsvParser.cleanDigitsOnly(rec.studentNik)
                (student.id > 0L && rec.studentId == student.id) ||
                    (studentCleanNik.isNotBlank() && recCleanNik == studentCleanNik) ||
                    (student.nis.isNotBlank() && rec.studentNis.equals(student.nis, ignoreCase = true))
            }
            val s = AppLanguageManager.getStrings(getApplication()).nav
            if (alreadyRegistered) {
                onResult(false, s.vmFastingAlreadyRegistered(student.name, session.title))
                return@launch
            }

            val activeSchool = session.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val now = System.currentTimeMillis()
            val resolvedJenjang = FastingJenjangHelper.resolveJenjang(student)
            val record = FastingRecord(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                sessionId = session.id,
                sessionTitle = session.title,
                sessionDate = session.sessionDate,
                studentId = student.id,
                studentName = student.name,
                studentNik = studentCleanNik,
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
            onResult(true, s.vmFastingRegisteredSuccess(student.name, resolvedJenjang))
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleFastingRecord(getApplication(), record)
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
                PocketBaseSyncManager.uploadSingleFastingRecord(getApplication(), updated)
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
                PocketBaseSyncManager.uploadSingleFastingRecord(getApplication(), updated)
            }
        }
    }

    fun deleteFastingRecord(record: FastingRecord) {
        viewModelScope.launch {
            val activeSchool = record.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val dateTs = record.sessionDate.takeIf { it > 0L } ?: record.createdAt
            val summary = "${record.sessionTitle.ifBlank { "Sesi Puasa" }} • ${DateUtils.formatShortDate(dateTs)}"
            val log = createAndRecordDeletionLog(
                category = DeletionLog.CATEGORY_PUASA,
                studentId = record.studentId,
                studentName = record.studentName,
                studentClass = record.studentClass,
                recordId = record.id,
                deletedSummary = summary,
                schoolId = activeSchool
            )
            repository.deleteFastingRecord(record)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.deleteRemoteFastingRecord(getApplication(), record.id)
                PocketBaseSyncManager.uploadSingleDeletionLog(getApplication(), log)
            }
        }
    }

    fun syncFastingOnlineQuietly() {
        if (isUnitTestEnvironment()) return
        val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
        viewModelScope.launch {
            PocketBaseSyncManager.syncFastingOnlineQuietly(getApplication(), activeSchool, repository)
        }
    }

    // Multi-tenant isolated Mengaji Berkelanjutan records (melekat pada siswa):
    val mengajiRecords: StateFlow<List<MengajiRecord>> = combine(
        repository.allMengajiRecords,
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

    fun saveMengajiRecord(
        student: ChildStudent,
        sessionDate: Long,
        bookType: String,
        jilidOrJuz: String,
        pageOrVerse: String,
        fluencyStatus: String,
        notes: String = "",
        existingRecordId: Long? = null,
        onComplete: (Boolean, MengajiRecord?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val activeSchool = student.schoolId.ifBlank {
                _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            }
            val cleanNik = com.example.util.StudentCsvParser.cleanDigitsOnly(student.nik)
            val resolvedJenjang = FastingJenjangHelper.resolveJenjang(student)
            val cleanPage = pageOrVerse.trim().ifBlank { "Hal. 1" }
            val parsedStart = MengajiHelper.extractStartingPageNumber(cleanPage)
            val parsedEnd = MengajiHelper.extractEndingPageNumber(cleanPage)
            val signerName = _waliName.value.trim().ifBlank {
                if (_currentUserRole.value == UserRole.GURU_NGAJI) "Guru Ngaji" else "Wali Asuh"
            }

            val record = if (existingRecordId != null && existingRecordId > 0L) {
                val existing = repository.getAllMengajiRecordsList().firstOrNull { it.id == existingRecordId }
                MengajiRecord(
                    id = existingRecordId,
                    studentId = student.id,
                    studentName = student.name,
                    studentNik = cleanNik,
                    studentNis = student.nis,
                    studentClass = student.classGrade,
                    studentDorm = student.dormitory,
                    studentJenjang = resolvedJenjang,
                    sessionDate = sessionDate,
                    bookType = bookType.trim().ifBlank { "Iqro'" },
                    jilidOrJuz = jilidOrJuz.trim().ifBlank { "Jilid 1" },
                    pageOrVerse = cleanPage,
                    startPage = parsedStart,
                    endPage = parsedEnd,
                    fluencyStatus = fluencyStatus,
                    notes = notes.trim(),
                    waliName = signerName,
                    schoolId = existing?.schoolId?.ifBlank { activeSchool } ?: activeSchool,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
            } else {
                MengajiRecord(
                    id = PocketBaseSyncManager.generateCloudSafeId(),
                    studentId = student.id,
                    studentName = student.name,
                    studentNik = cleanNik,
                    studentNis = student.nis,
                    studentClass = student.classGrade,
                    studentDorm = student.dormitory,
                    studentJenjang = resolvedJenjang,
                    sessionDate = sessionDate,
                    bookType = bookType.trim().ifBlank { "Iqro'" },
                    jilidOrJuz = jilidOrJuz.trim().ifBlank { "Jilid 1" },
                    pageOrVerse = cleanPage,
                    startPage = parsedStart,
                    endPage = parsedEnd,
                    fluencyStatus = fluencyStatus,
                    notes = notes.trim(),
                    waliName = signerName,
                    schoolId = activeSchool,
                    createdAt = now,
                    updatedAt = now
                )
            }

            repository.insertMengajiRecord(record)
            onComplete(true, record)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleMengajiRecord(getApplication(), record)
            }
        }
    }

    fun deleteMengajiRecord(record: MengajiRecord) {
        if (_currentUserRole.value == UserRole.GURU_NGAJI || _currentUserRole.value == UserRole.PARENT) {
            return
        }
        viewModelScope.launch {
            val activeSchool = record.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val summary = "${MengajiHelper.formatCompactPosition(record)} (${MengajiHelper.fluencyLabel(record.fluencyStatus, false)}, ${DateUtils.formatShortDate(record.sessionDate)})"
            val log = createAndRecordDeletionLog(
                category = DeletionLog.CATEGORY_MENGAJI,
                studentId = record.studentId,
                studentName = record.studentName,
                studentClass = record.studentClass,
                recordId = record.id,
                deletedSummary = summary,
                schoolId = activeSchool
            )
            repository.deleteMengajiRecord(record)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.deleteRemoteMengajiRecord(getApplication(), record.id)
                PocketBaseSyncManager.uploadSingleDeletionLog(getApplication(), log)
            }
        }
    }

    fun syncShiftMessagesOnlineQuietly() {
        if (isUnitTestEnvironment()) return
        val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
        viewModelScope.launch {
            PocketBaseSyncManager.syncShiftMessagesOnlineQuietly(getApplication(), activeSchool, repository)
        }
    }

    // Multi-tenant isolated Parent Visit (Kunjungan Orang Tua) sessions & attendance records:
    val visitSessions: StateFlow<List<VisitSession>> = combine(
        repository.allVisitSessions,
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

    val visitRecords: StateFlow<List<VisitRecord>> = combine(
        repository.allVisitRecords,
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

    // ==========================================
    // HUBUNGAN ORANG TUA (ANAK BINAAN, KARTU AKSES, IZIN & CHAT 2 ARAH)
    // ==========================================
    val parentAccessLinks: StateFlow<List<ParentAccessLink>> = combine(
        repository.allParentAccessLinks,
        _schoolProfile,
        _currentUserRole
    ) { links, profile, role ->
        if (role == UserRole.SUPERADMIN) {
            links
        } else {
            val tenant = profile.tenantCode.ifBlank { "SRT1-KDR" }
            links.filter { it.schoolId.equals(tenant, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val parentMessages: StateFlow<List<ParentMessage>> = combine(
        repository.allParentMessages,
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

    val activeParentAccessLink: StateFlow<ParentAccessLink?> = combine(
        parentAccessLinks,
        _currentUserRole,
        _currentUsername,
        _currentParentStudentId
    ) { links, role, username, studentId ->
        if (role != UserRole.PARENT) {
            null
        } else {
            links.firstOrNull { studentId > 0L && it.studentId == studentId }
                ?: links.firstOrNull { !username.isNullOrBlank() && it.username.equals(username.trim(), ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun isStudentClaimedByOtherWali(student: ChildStudent): Boolean {
        val claimedUser = student.assignedWaliUsername.trim()
        val claimedName = student.assignedWaliName.trim()
        if (claimedUser.isBlank() && claimedName.isBlank()) return false
        val role = _currentUserRole.value
        if (role == UserRole.ADMIN || role == UserRole.SUPERADMIN) return false
        val activeUser = _currentUsername.value?.trim().orEmpty()
        val activeName = _waliName.value.trim()
        val matchesUser = activeUser.isNotBlank() && claimedUser.isNotBlank() && claimedUser.equals(activeUser, ignoreCase = true)
        val matchesName = activeName.isNotBlank() && claimedName.isNotBlank() && claimedName.equals(activeName, ignoreCase = true)
        return !(matchesUser || matchesName)
    }

    fun isStudentClaimedByCurrentWali(student: ChildStudent): Boolean {
        val claimedUser = student.assignedWaliUsername.trim()
        val claimedName = student.assignedWaliName.trim()
        if (claimedUser.isBlank() && claimedName.isBlank()) return false
        val activeUser = _currentUsername.value?.trim().orEmpty()
        val activeName = _waliName.value.trim()
        val matchesUser = activeUser.isNotBlank() && claimedUser.isNotBlank() && claimedUser.equals(activeUser, ignoreCase = true)
        val matchesName = activeName.isNotBlank() && claimedName.isNotBlank() && claimedName.equals(activeName, ignoreCase = true)
        return matchesUser || matchesName
    }

    fun canCurrentUserReleaseStudent(student: ChildStudent): Boolean {
        if (student.assignedWaliUsername.isBlank() && student.assignedWaliName.isBlank()) return false
        val role = _currentUserRole.value
        if (role == UserRole.ADMIN || role == UserRole.SUPERADMIN) return true
        return isStudentClaimedByCurrentWali(student)
    }

    private fun buildParentAccessUsername(student: ChildStudent, existingUsernames: Set<String>): String {
        val nameSlug = student.name.trim().lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .take(8)
            .ifBlank { "siswa" }
        val codeSuffix = student.nis.filter { it.isLetterOrDigit() }.takeLast(4)
            .ifBlank { (student.id % 10000L).toString() }
        var candidate = "ortu_${nameSlug}_$codeSuffix"
        var attempt = 1
        while (candidate.lowercase() in existingUsernames) {
            attempt++
            candidate = "ortu_${nameSlug}_${codeSuffix}_$attempt"
        }
        return candidate
    }

    private suspend fun createOrSyncParentLinkForStudent(
        student: ChildStudent,
        assignedWaliUser: String = student.assignedWaliUsername,
        assignedWaliDisplay: String = student.assignedWaliName
    ): ParentAccessLink {
        val activeSchool = student.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
        val existing = repository.getParentAccessLinkByStudentId(student.id, activeSchool)
        val now = System.currentTimeMillis()
        val cleanPhone = com.example.util.StudentCsvParser.cleanPhoneNumber(student.parentPhone)
        val resolvedParentName = student.motherName.trim().ifBlank {
            existing?.parentName?.trim().orEmpty().ifBlank { "Orang Tua ${student.name}" }
        }
        val link = if (existing != null) {
            existing.copy(
                studentName = student.name,
                studentNis = student.nis,
                studentClass = student.classGrade,
                studentDorm = student.dormitory,
                parentName = resolvedParentName,
                parentPhone = cleanPhone.ifBlank { existing.parentPhone },
                assignedWaliUsername = assignedWaliUser.ifBlank { existing.assignedWaliUsername },
                assignedWaliName = assignedWaliDisplay.ifBlank { existing.assignedWaliName },
                updatedAt = now
            )
        } else {
            val allUsernames = repository.getAllParentAccessLinksList().map { it.username.lowercase() }.toSet()
            ParentAccessLink(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                studentId = student.id,
                studentName = student.name,
                studentNis = student.nis,
                studentClass = student.classGrade,
                studentDorm = student.dormitory,
                parentName = resolvedParentName,
                parentPhone = cleanPhone,
                username = buildParentAccessUsername(student, allUsernames),
                password = PasswordGenerator.generateRandomPassword(),
                assignedWaliUsername = assignedWaliUser,
                assignedWaliName = assignedWaliDisplay,
                canViewMengaji = true,
                canViewMedical = true,
                canViewFasting = true,
                canViewHaid = true,
                canViewProfile = true,
                schoolId = activeSchool,
                createdAt = now,
                updatedAt = now
            )
        }
        repository.insertParentAccessLink(link)
        if (!isUnitTestEnvironment()) {
            PocketBaseSyncManager.uploadSingleParentAccessLink(getApplication(), link)
        }
        return link
    }

    fun claimStudentsForCurrentWali(
        selectedStudents: List<ChildStudent>,
        onResult: (claimedCount: Int, skippedCount: Int) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val waliDisplay = _waliName.value.trim().ifBlank { "Wali Asuh" }
            val waliUser = _currentUsername.value?.trim().orEmpty().ifBlank {
                waliDisplay.lowercase().replace(" ", "")
            }
            var claimedCount = 0
            var skippedCount = 0
            selectedStudents.forEach { st ->
                val latest = repository.getStudentById(st.id) ?: st
                if (isStudentClaimedByOtherWali(latest)) {
                    skippedCount++
                } else {
                    val updatedStudent = latest.copy(
                        assignedWaliUsername = waliUser,
                        assignedWaliName = waliDisplay
                    )
                    repository.updateStudent(updatedStudent)
                    if (!isUnitTestEnvironment()) {
                        PocketBaseSyncManager.uploadSingleStudent(getApplication(), updatedStudent)
                    }
                    createOrSyncParentLinkForStudent(updatedStudent, waliUser, waliDisplay)
                    claimedCount++
                }
            }
            onResult(claimedCount, skippedCount)
        }
    }

    fun releaseStudentClaim(
        student: ChildStudent,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
            val latest = repository.getStudentById(student.id) ?: student
            if (!canCurrentUserReleaseStudent(latest)) {
                onResult(
                    false,
                    if (isEn) {
                        "Only School Admin or the assigned Wali Asuh can release this student."
                    } else {
                        "Hanya Admin Sekolah atau Wali Asuh pembina bersangkutan yang dapat melepas siswa ini."
                    }
                )
                return@launch
            }
            val releasedStudent = latest.copy(
                assignedWaliUsername = "",
                assignedWaliName = ""
            )
            repository.updateStudent(releasedStudent)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleStudent(getApplication(), releasedStudent)
            }
            val activeSchool = latest.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            repository.getParentAccessLinkByStudentId(latest.id, activeSchool)?.let { existingLink ->
                val updatedLink = existingLink.copy(
                    assignedWaliUsername = "",
                    assignedWaliName = "",
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateParentAccessLink(updatedLink)
                if (!isUnitTestEnvironment()) {
                    PocketBaseSyncManager.uploadSingleParentAccessLink(getApplication(), updatedLink)
                }
            }
            onResult(
                true,
                if (isEn) "${latest.name} has been released." else "${latest.name} berhasil dilepas dari binaan."
            )
        }
    }

    fun ensureParentAccessLinkForStudent(
        student: ChildStudent,
        onReady: (ParentAccessLink) -> Unit = {}
    ) {
        viewModelScope.launch {
            val link = createOrSyncParentLinkForStudent(student)
            onReady(link)
        }
    }

    fun resetParentAccessPassword(
        link: ParentAccessLink,
        onComplete: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val newPass = PasswordGenerator.generateRandomPassword()
            val updated = link.copy(
                password = newPass,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateParentAccessLink(updated)
            onComplete(newPass)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleParentAccessLink(getApplication(), updated)
            }
        }
    }

    fun updateParentAccessIdentity(
        link: ParentAccessLink,
        parentName: String,
        parentPhone: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val cleanName = parentName.trim().ifBlank { link.parentName }
            val cleanPhone = com.example.util.StudentCsvParser.cleanPhoneNumber(parentPhone)
            val updatedLink = link.copy(
                parentName = cleanName,
                parentPhone = cleanPhone,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateParentAccessLink(updatedLink)
            repository.getStudentById(link.studentId)?.let { student ->
                val updatedStudent = student.copy(
                    motherName = if (cleanName.isNotBlank() && !cleanName.startsWith("Orang Tua ", ignoreCase = true)) cleanName else student.motherName,
                    parentPhone = cleanPhone
                )
                repository.updateStudent(updatedStudent)
                if (!isUnitTestEnvironment()) {
                    PocketBaseSyncManager.uploadSingleStudent(getApplication(), updatedStudent)
                }
            }
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleParentAccessLink(getApplication(), updatedLink)
            }
        }
    }

    fun updateParentAccessPermissions(
        link: ParentAccessLink,
        canMengaji: Boolean,
        canMedical: Boolean,
        canFasting: Boolean,
        canHaid: Boolean = link.canViewHaid,
        canProfile: Boolean = link.canViewProfile,
        canTabungan: Boolean = link.canViewTabungan,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val updated = link.copy(
                canViewMengaji = canMengaji,
                canViewMedical = canMedical,
                canViewFasting = canFasting,
                canViewHaid = canHaid,
                canViewProfile = canProfile,
                canViewTabungan = canTabungan,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateParentAccessLink(updated)
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleParentAccessLink(getApplication(), updated)
            }
        }
    }

    // ==========================================
    // TABUNGAN PRIBADI WALI ASUH (PENITIPAN UANG SISWA - ISOLASI PRIVAT)
    // Hanya terlihat oleh Wali Asuh pemiliknya & anaksuperadmin (serta Portal Ortu jika dicentang)
    // ==========================================
    val tabunganTransactions: StateFlow<List<TabunganTransaction>> = combine(
        repository.allTabunganTransactions,
        _schoolProfile,
        _currentUserRole,
        combine(_currentUsername, _waliName, isAnakSuperAdmin) { u, w, superW -> Triple(u, w, superW) }
    ) { allTxs, profile, role, actorTriple ->
        val tenant = profile.tenantCode.ifBlank { "SRT1-KDR" }
        TabunganHelper.filterPrivateTransactions(
            allTransactions = allTxs,
            schoolId = tenant,
            role = role,
            isAnakSuperAdmin = actorTriple.third,
            currentUsername = actorTriple.first,
            currentWaliName = actorTriple.second
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val tabunganLogs: StateFlow<List<TabunganLog>> = combine(
        repository.allTabunganLogs,
        _schoolProfile,
        _currentUserRole,
        combine(_currentUsername, _waliName, isAnakSuperAdmin) { u, w, superW -> Triple(u, w, superW) }
    ) { allLogs, profile, role, actorTriple ->
        val tenant = profile.tenantCode.ifBlank { "SRT1-KDR" }
        TabunganHelper.filterPrivateLogs(
            allLogs = allLogs,
            schoolId = tenant,
            role = role,
            isAnakSuperAdmin = actorTriple.third,
            currentUsername = actorTriple.first,
            currentWaliName = actorTriple.second
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val parentTabunganTransactions: StateFlow<List<TabunganTransaction>> = combine(
        repository.allTabunganTransactions,
        activeParentAccessLink,
        _currentUserRole
    ) { allTxs, parentLink, role ->
        if (role != UserRole.PARENT || parentLink == null || !parentLink.canViewTabungan) {
            emptyList()
        } else {
            allTxs.filter { tx ->
                tx.schoolId.equals(parentLink.schoolId, ignoreCase = true) &&
                    TabunganHelper.matchesStudentIdOrNis(tx, parentLink.studentId, parentLink.studentNis) &&
                    TabunganHelper.isActive(tx.status)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun syncParentRelationsOnlineQuietly() {
        if (isUnitTestEnvironment()) return
        val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
        viewModelScope.launch {
            PocketBaseSyncManager.syncParentRelationsOnlineQuietly(getApplication(), activeSchool, repository)
        }
    }

    fun syncTabunganOnlineQuietly() {
        if (isUnitTestEnvironment()) return
        val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
        viewModelScope.launch {
            PocketBaseSyncManager.syncTabunganOnlineQuietly(getApplication(), activeSchool, repository)
        }
    }

    fun saveTabunganTransaction(
        student: ChildStudent,
        transactionType: String,
        amount: Long,
        title: String,
        notes: String = "",
        transactionDate: Long = System.currentTimeMillis(),
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
        if (amount <= 0L) {
            onResult(false, if (isEn) "Nominal must be greater than Rp 0." else "Nominal harus lebih dari Rp 0.")
            return
        }
        val cleanType = if (TabunganHelper.isIncome(transactionType)) TabunganHelper.TYPE_IN else TabunganHelper.TYPE_OUT
        viewModelScope.launch {
            val activeSchool = student.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val currentUser = _currentUsername.value?.trim().orEmpty()
            val currentName = _waliName.value.trim().ifBlank { "Wali Asuh" }
            val ownerUser = student.assignedWaliUsername.trim().ifBlank { currentUser }
            val ownerName = student.assignedWaliName.trim().ifBlank { currentName }

            val existingForStudent = repository.getTabunganTransactionsBySchoolList(activeSchool)
                .filter {
                    TabunganHelper.matchesStudentIdOrNis(it, student.id, student.nis) &&
                        TabunganHelper.matchesPrivateOwner(
                            ownerUsername = it.ownerWaliUsername,
                            ownerName = it.ownerWaliName,
                            currentUsername = ownerUser,
                            currentWaliName = ownerName
                        )
                }
            val summaryBefore = TabunganHelper.computeSummaryByStudentId(existingForStudent, student.id, student.nis)
            if (cleanType == TabunganHelper.TYPE_OUT && amount > summaryBefore.balance) {
                onResult(
                    false,
                    if (isEn) {
                        "Insufficient balance (${TabunganHelper.formatRupiah(summaryBefore.balance)})."
                    } else {
                        "Saldo ${student.name} tidak mencukupi (tersedia ${TabunganHelper.formatRupiah(summaryBefore.balance)})."
                    }
                )
                return@launch
            }

            val now = System.currentTimeMillis()
            val txId = PocketBaseSyncManager.generateCloudSafeId()
            val resolvedTitle = title.trim().ifBlank {
                if (cleanType == TabunganHelper.TYPE_IN) "Titip Uang Saku" else "Pengeluaran Kebutuhan"
            }
            val newTx = TabunganTransaction(
                id = txId,
                studentId = student.id,
                studentName = student.name,
                studentNis = student.nis,
                studentClass = student.classGrade,
                studentDorm = student.dormitory,
                ownerWaliUsername = ownerUser,
                ownerWaliName = ownerName,
                isPrivate = true,
                transactionType = cleanType,
                amount = amount,
                title = resolvedTitle,
                notes = notes.trim(),
                transactionDate = transactionDate,
                status = TabunganHelper.STATUS_ACTIVE,
                schoolId = activeSchool,
                createdAt = now,
                updatedAt = now
            )
            repository.insertTabunganTransaction(newTx)

            val balanceAfter = if (cleanType == TabunganHelper.TYPE_IN) {
                summaryBefore.balance + amount
            } else {
                (summaryBefore.balance - amount).coerceAtLeast(0L)
            }
            val logEntry = TabunganLog(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                transactionId = newTx.id,
                studentId = student.id,
                studentName = student.name,
                studentClass = student.classGrade,
                ownerWaliUsername = ownerUser,
                ownerWaliName = ownerName,
                isPrivate = true,
                actionType = if (cleanType == TabunganHelper.TYPE_IN) TabunganHelper.LOG_INPUT_IN else TabunganHelper.LOG_INPUT_OUT,
                amount = amount,
                balanceBefore = summaryBefore.balance,
                balanceAfter = balanceAfter,
                summary = buildString {
                    append(resolvedTitle)
                    if (notes.isNotBlank()) append(" • ${notes.trim()}")
                },
                actorName = currentName,
                actorUsername = currentUser,
                schoolId = activeSchool,
                createdAt = now
            )
            repository.insertTabunganLog(logEntry)
            onResult(
                true,
                if (isEn) "Savings transaction saved & synced." else "Transaksi tabungan berhasil dicatat (privat)."
            )
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleTabunganTransaction(getApplication(), newTx)
                PocketBaseSyncManager.uploadSingleTabunganLog(getApplication(), logEntry)
            }
        }
    }

    fun editTabunganTransaction(
        existingTx: TabunganTransaction,
        transactionType: String,
        amount: Long,
        title: String,
        notes: String = "",
        transactionDate: Long = existingTx.transactionDate,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
        if (!TabunganHelper.isActive(existingTx.status)) {
            onResult(false, if (isEn) "Cancelled transactions cannot be edited." else "Transaksi yang sudah dibatalkan tidak dapat diubah.")
            return
        }
        if (amount <= 0L) {
            onResult(false, if (isEn) "Nominal must be greater than Rp 0." else "Nominal harus lebih dari Rp 0.")
            return
        }
        val cleanType = if (TabunganHelper.isIncome(transactionType)) TabunganHelper.TYPE_IN else TabunganHelper.TYPE_OUT
        viewModelScope.launch {
            val activeSchool = existingTx.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val currentUser = _currentUsername.value?.trim().orEmpty()
            val currentName = _waliName.value.trim().ifBlank { "Wali Asuh" }
            val now = System.currentTimeMillis()
            val resolvedTitle = title.trim().ifBlank {
                if (cleanType == TabunganHelper.TYPE_IN) "Titip Uang Saku" else "Pengeluaran Kebutuhan"
            }

            val existingForStudent = repository.getTabunganTransactionsBySchoolList(activeSchool)
                .filter {
                    TabunganHelper.matchesStudentIdOrNis(it, existingTx.studentId, existingTx.studentNis) &&
                        TabunganHelper.matchesPrivateOwner(
                            ownerUsername = it.ownerWaliUsername,
                            ownerName = it.ownerWaliName,
                            currentUsername = existingTx.ownerWaliUsername,
                            currentWaliName = existingTx.ownerWaliName
                        )
                }
            val balanceBefore = TabunganHelper.computeSummaryByStudentId(
                existingForStudent,
                existingTx.studentId,
                existingTx.studentNis
            ).balance

            val updatedTx = existingTx.copy(
                transactionType = cleanType,
                amount = amount,
                title = resolvedTitle,
                notes = notes.trim(),
                transactionDate = transactionDate,
                updatedAt = now
            )
            val simulatedList = existingForStudent.map { if (it.id == existingTx.id) updatedTx else it }
            val rawNetAfter = TabunganHelper.computeRawNetBalance(simulatedList)
            if (rawNetAfter < 0L) {
                val deficit = kotlin.math.abs(rawNetAfter)
                onResult(
                    false,
                    if (isEn) {
                        "Edit rejected: resulting balance would be negative (-${TabunganHelper.formatRupiah(deficit)})."
                    } else {
                        "Perubahan ditolak: saldo ${existingTx.studentName} akan menjadi minus (-${TabunganHelper.formatRupiah(deficit)})."
                    }
                )
                return@launch
            }

            repository.updateTabunganTransaction(updatedTx)
            val logEntry = TabunganLog(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                transactionId = updatedTx.id,
                studentId = updatedTx.studentId,
                studentName = updatedTx.studentName,
                studentClass = updatedTx.studentClass,
                ownerWaliUsername = updatedTx.ownerWaliUsername,
                ownerWaliName = updatedTx.ownerWaliName,
                isPrivate = true,
                actionType = TabunganHelper.LOG_EDIT,
                amount = amount,
                balanceBefore = balanceBefore,
                balanceAfter = rawNetAfter.coerceAtLeast(0L),
                summary = buildString {
                    append("Edit: $resolvedTitle (${TabunganHelper.formatRupiah(existingTx.amount)} → ${TabunganHelper.formatRupiah(amount)})")
                    if (notes.isNotBlank()) append(" • ${notes.trim()}")
                },
                actorName = currentName,
                actorUsername = currentUser,
                schoolId = activeSchool,
                createdAt = now
            )
            repository.insertTabunganLog(logEntry)
            onResult(true, if (isEn) "Transaction updated & synced." else "Transaksi tabungan berhasil diperbarui.")
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleTabunganTransaction(getApplication(), updatedTx)
                PocketBaseSyncManager.uploadSingleTabunganLog(getApplication(), logEntry)
            }
        }
    }

    fun cancelTabunganTransaction(
        tx: TabunganTransaction,
        reason: String = "",
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
        if (!TabunganHelper.isActive(tx.status)) {
            onResult(false, if (isEn) "Transaction is already cancelled." else "Transaksi ini sudah dibatalkan.")
            return
        }
        viewModelScope.launch {
            val activeSchool = tx.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val currentUser = _currentUsername.value?.trim().orEmpty()
            val currentName = _waliName.value.trim().ifBlank { "Wali Asuh" }
            val now = System.currentTimeMillis()
            val cleanReason = reason.trim().ifBlank { "Dibatalkan oleh Wali Asuh" }

            val existingForStudent = repository.getTabunganTransactionsBySchoolList(activeSchool)
                .filter {
                    TabunganHelper.matchesStudentIdOrNis(it, tx.studentId, tx.studentNis) &&
                        TabunganHelper.matchesPrivateOwner(
                            ownerUsername = it.ownerWaliUsername,
                            ownerName = it.ownerWaliName,
                            currentUsername = tx.ownerWaliUsername,
                            currentWaliName = tx.ownerWaliName
                        )
                }
            val balanceBefore = TabunganHelper.computeSummaryByStudentId(existingForStudent, tx.studentId, tx.studentNis).balance
            val updatedTx = tx.copy(
                status = TabunganHelper.STATUS_CANCELLED,
                cancelledBy = currentName,
                cancelledReason = cleanReason,
                cancelledAt = now,
                updatedAt = now
            )
            val updatedList = existingForStudent.map { if (it.id == tx.id) updatedTx else it }
            val rawNetAfter = TabunganHelper.computeRawNetBalance(updatedList)
            if (rawNetAfter < 0L) {
                val deficit = kotlin.math.abs(rawNetAfter)
                onResult(
                    false,
                    if (isEn) {
                        "Cannot cancel deposit: balance would become negative (-${TabunganHelper.formatRupiah(deficit)}). Cancel or delete related expense(s) first."
                    } else {
                        "Titipan tidak bisa dibatalkan karena saldo akan minus (-${TabunganHelper.formatRupiah(deficit)}). Batalkan/hapus pengeluaran terlebih dahulu."
                    }
                )
                return@launch
            }

            repository.updateTabunganTransaction(updatedTx)
            val balanceAfter = rawNetAfter.coerceAtLeast(0L)

            val logEntry = TabunganLog(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                transactionId = tx.id,
                studentId = tx.studentId,
                studentName = tx.studentName,
                studentClass = tx.studentClass,
                ownerWaliUsername = tx.ownerWaliUsername,
                ownerWaliName = tx.ownerWaliName,
                isPrivate = true,
                actionType = TabunganHelper.LOG_CANCEL,
                amount = tx.amount,
                balanceBefore = balanceBefore,
                balanceAfter = balanceAfter,
                summary = "Batalkan: ${tx.title.ifBlank { tx.transactionType }} ($cleanReason)",
                actorName = currentName,
                actorUsername = currentUser,
                schoolId = activeSchool,
                createdAt = now
            )
            repository.insertTabunganLog(logEntry)
            onResult(true, if (isEn) "Transaction cancelled." else "Transaksi dibatalkan & tercatat di log tabungan.")
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleTabunganTransaction(getApplication(), updatedTx)
                PocketBaseSyncManager.uploadSingleTabunganLog(getApplication(), logEntry)
            }
        }
    }

    fun deleteTabunganTransaction(
        tx: TabunganTransaction,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
        viewModelScope.launch {
            val activeSchool = tx.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val currentUser = _currentUsername.value?.trim().orEmpty()
            val currentName = _waliName.value.trim().ifBlank { "Wali Asuh" }
            val now = System.currentTimeMillis()

            val existingForStudent = repository.getTabunganTransactionsBySchoolList(activeSchool)
                .filter {
                    TabunganHelper.matchesStudentIdOrNis(it, tx.studentId, tx.studentNis) &&
                        TabunganHelper.matchesPrivateOwner(
                            ownerUsername = it.ownerWaliUsername,
                            ownerName = it.ownerWaliName,
                            currentUsername = tx.ownerWaliUsername,
                            currentWaliName = tx.ownerWaliName
                        )
                }
            val balanceBefore = TabunganHelper.computeSummaryByStudentId(existingForStudent, tx.studentId, tx.studentNis).balance
            val afterList = existingForStudent.filterNot { it.id == tx.id }
            val rawNetAfter = TabunganHelper.computeRawNetBalance(afterList)
            if (rawNetAfter < 0L) {
                val deficit = kotlin.math.abs(rawNetAfter)
                onResult(
                    false,
                    if (isEn) {
                        "Cannot delete deposit: balance would become negative (-${TabunganHelper.formatRupiah(deficit)}). Cancel or delete related expense(s) first."
                    } else {
                        "Titipan tidak bisa dihapus karena saldo akan minus (-${TabunganHelper.formatRupiah(deficit)}). Hapus/batalkan pengeluaran terkait terlebih dahulu."
                    }
                )
                return@launch
            }

            repository.deleteTabunganTransaction(tx)
            val balanceAfter = rawNetAfter.coerceAtLeast(0L)

            // Catat log privat internal Tabungan SAJA — TIDAK menerbitkan DeletionLog publik sekolah
            val logEntry = TabunganLog(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                transactionId = tx.id,
                studentId = tx.studentId,
                studentName = tx.studentName,
                studentClass = tx.studentClass,
                ownerWaliUsername = tx.ownerWaliUsername,
                ownerWaliName = tx.ownerWaliName,
                isPrivate = true,
                actionType = TabunganHelper.LOG_DELETE,
                amount = tx.amount,
                balanceBefore = balanceBefore,
                balanceAfter = balanceAfter,
                summary = "Hapus permanen: ${tx.title.ifBlank { tx.transactionType }} (${TabunganHelper.formatRupiah(tx.amount)})",
                actorName = currentName,
                actorUsername = currentUser,
                schoolId = activeSchool,
                createdAt = now
            )
            repository.insertTabunganLog(logEntry)
            onResult(true, if (isEn) "Transaction deleted." else "Transaksi dihapus permanen (tercatat di log pribadi).")
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.deleteRemoteTabunganTransaction(getApplication(), tx.id)
                PocketBaseSyncManager.uploadSingleTabunganLog(getApplication(), logEntry)
            }
        }
    }

    // ==========================================
    // KONSELING RAHASIA WALI ASUH (PRIVASI MUTLAK)
    // HANYA dilihat & dikelola oleh Wali Asuh bersangkutan (pemilik catatan)
    // Tidak dapat dilihat Admin Sekolah, Guru Ngaji, Wali lain, Portal Ortu, maupun lintas akun
    // ==========================================
    val konselingRecords: StateFlow<List<KonselingRecord>> = combine(
        repository.allKonselingRecords,
        _schoolProfile,
        _currentUserRole,
        combine(_currentUsername, _waliName) { u, w -> u to w }
    ) { allRecs, profile, role, actorPair ->
        val tenant = profile.tenantCode.ifBlank { "SRT1-KDR" }
        KonselingHelper.filterPrivateRecords(
            allRecords = allRecs,
            schoolId = tenant,
            role = role,
            currentUsername = actorPair.first,
            currentWaliName = actorPair.second
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val konselingLogs: StateFlow<List<KonselingLog>> = combine(
        repository.allKonselingLogs,
        _schoolProfile,
        _currentUserRole,
        combine(_currentUsername, _waliName) { u, w -> u to w }
    ) { allLogs, profile, role, actorPair ->
        val tenant = profile.tenantCode.ifBlank { "SRT1-KDR" }
        KonselingHelper.filterPrivateLogs(
            allLogs = allLogs,
            schoolId = tenant,
            role = role,
            currentUsername = actorPair.first,
            currentWaliName = actorPair.second
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun syncKonselingOnlineQuietly() {
        if (isUnitTestEnvironment()) return
        if (_currentUserRole.value != UserRole.WALI) return
        val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
        viewModelScope.launch {
            PocketBaseSyncManager.syncKonselingOnlineQuietly(getApplication(), activeSchool, repository)
        }
    }

    fun saveKonselingRecord(
        student: ChildStudent,
        entryType: String,
        topic: String,
        urgencyLevel: String,
        moodLevel: String,
        title: String,
        summary: String,
        intervention: String = "",
        followUpPlan: String = "",
        followUpDate: Long? = null,
        sessionDate: Long = System.currentTimeMillis(),
        markCompleted: Boolean = false,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
        if (_currentUserRole.value != UserRole.WALI) {
            onResult(false, if (isEn) "Only the owning Wali Asuh can manage private counseling notes." else "Hanya Wali Asuh bersangkutan yang dapat mencatat konseling rahasia.")
            return
        }
        val cleanSummary = summary.trim()
        val cleanTitle = title.trim()
        if (cleanSummary.isBlank() && cleanTitle.isBlank()) {
            onResult(false, if (isEn) "Please fill in the counseling summary or title." else "Isi judul atau ringkasan catatan konseling terlebih dahulu.")
            return
        }
        val cleanEntryType = if (KonselingHelper.isGrowthEntry(entryType)) {
            KonselingHelper.ENTRY_PERKEMBANGAN
        } else {
            KonselingHelper.ENTRY_SESI
        }
        val resolvedTitle = cleanTitle.ifBlank {
            KonselingHelper.formatTopicLabel(topic, isEn)
        }
        val resolvedStatus = if (markCompleted) KonselingHelper.STATUS_SELESAI else KonselingHelper.STATUS_DIPANTAU

        viewModelScope.launch {
            val activeSchool = student.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val currentUser = _currentUsername.value?.trim().orEmpty()
            val currentName = _waliName.value.trim().ifBlank { "Wali Asuh" }
            val ownerUser = currentUser.ifBlank { student.assignedWaliUsername.trim() }
            val ownerName = currentName
            val now = System.currentTimeMillis()
            val recId = PocketBaseSyncManager.generateCloudSafeId()

            val newRecord = KonselingRecord(
                id = recId,
                studentId = student.id,
                studentName = student.name,
                studentNis = student.nis,
                studentClass = student.classGrade,
                studentDorm = student.dormitory,
                ownerWaliUsername = ownerUser,
                ownerWaliName = ownerName,
                isConfidential = true,
                entryType = cleanEntryType,
                topic = topic.trim().ifBlank { KonselingHelper.TOPIC_ADAPTASI },
                urgencyLevel = urgencyLevel.trim().ifBlank { KonselingHelper.URGENCY_RINGAN },
                moodLevel = moodLevel.trim().ifBlank { KonselingHelper.MOOD_NETRAL },
                title = resolvedTitle,
                summary = cleanSummary,
                intervention = intervention.trim(),
                followUpPlan = followUpPlan.trim(),
                followUpDate = followUpDate,
                status = resolvedStatus,
                sessionDate = sessionDate,
                schoolId = activeSchool,
                createdAt = now,
                updatedAt = now
            )
            repository.insertKonselingRecord(newRecord)

            val logEntry = KonselingLog(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                recordId = newRecord.id,
                studentId = student.id,
                studentName = student.name,
                studentClass = student.classGrade,
                ownerWaliUsername = ownerUser,
                ownerWaliName = ownerName,
                isConfidential = true,
                actionType = if (cleanEntryType == KonselingHelper.ENTRY_PERKEMBANGAN) {
                    KonselingHelper.LOG_INPUT_GROWTH
                } else {
                    KonselingHelper.LOG_INPUT_SESI
                },
                summary = buildString {
                    append(resolvedTitle)
                    append(" • ")
                    append(KonselingHelper.formatTopicLabel(newRecord.topic, false))
                },
                actorName = currentName,
                actorUsername = currentUser,
                schoolId = activeSchool,
                createdAt = now
            )
            repository.insertKonselingLog(logEntry)
            onResult(
                true,
                if (isEn) "Private counseling note saved (owner-only)." else "Catatan konseling rahasia berhasil disimpan (khusus Anda)."
            )
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleKonselingRecord(getApplication(), newRecord)
                PocketBaseSyncManager.uploadSingleKonselingLog(getApplication(), logEntry)
            }
        }
    }

    fun editKonselingRecord(
        existingRecord: KonselingRecord,
        entryType: String,
        topic: String,
        urgencyLevel: String,
        moodLevel: String,
        title: String,
        summary: String,
        intervention: String = "",
        followUpPlan: String = "",
        followUpDate: Long? = existingRecord.followUpDate,
        sessionDate: Long = existingRecord.sessionDate,
        markCompleted: Boolean = KonselingHelper.isCompleted(existingRecord.status),
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
        val currentUser = _currentUsername.value?.trim().orEmpty()
        val currentName = _waliName.value.trim().ifBlank { "Wali Asuh" }
        if (_currentUserRole.value != UserRole.WALI ||
            !KonselingHelper.matchesOwnerOnly(
                ownerUsername = existingRecord.ownerWaliUsername,
                ownerName = existingRecord.ownerWaliName,
                currentUsername = currentUser,
                currentWaliName = currentName
            )
        ) {
            onResult(false, if (isEn) "Access denied: confidential record belongs to another caregiver." else "Akses ditolak: catatan rahasia ini bukan milik akun Anda.")
            return
        }
        val cleanSummary = summary.trim()
        val cleanTitle = title.trim()
        if (cleanSummary.isBlank() && cleanTitle.isBlank()) {
            onResult(false, if (isEn) "Please fill in the counseling summary or title." else "Isi judul atau ringkasan catatan konseling.")
            return
        }
        val cleanEntryType = if (KonselingHelper.isGrowthEntry(entryType)) {
            KonselingHelper.ENTRY_PERKEMBANGAN
        } else {
            KonselingHelper.ENTRY_SESI
        }
        val resolvedTitle = cleanTitle.ifBlank { KonselingHelper.formatTopicLabel(topic, isEn) }
        val resolvedStatus = if (markCompleted) KonselingHelper.STATUS_SELESAI else KonselingHelper.STATUS_DIPANTAU

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val activeSchool = existingRecord.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val updated = existingRecord.copy(
                isConfidential = true,
                entryType = cleanEntryType,
                topic = topic.trim().ifBlank { KonselingHelper.TOPIC_ADAPTASI },
                urgencyLevel = urgencyLevel.trim().ifBlank { KonselingHelper.URGENCY_RINGAN },
                moodLevel = moodLevel.trim().ifBlank { KonselingHelper.MOOD_NETRAL },
                title = resolvedTitle,
                summary = cleanSummary,
                intervention = intervention.trim(),
                followUpPlan = followUpPlan.trim(),
                followUpDate = followUpDate,
                status = resolvedStatus,
                sessionDate = sessionDate,
                updatedAt = now
            )
            repository.updateKonselingRecord(updated)

            val logEntry = KonselingLog(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                recordId = updated.id,
                studentId = updated.studentId,
                studentName = updated.studentName,
                studentClass = updated.studentClass,
                ownerWaliUsername = updated.ownerWaliUsername,
                ownerWaliName = updated.ownerWaliName,
                isConfidential = true,
                actionType = KonselingHelper.LOG_EDIT,
                summary = "Edit: $resolvedTitle",
                actorName = currentName,
                actorUsername = currentUser,
                schoolId = activeSchool,
                createdAt = now
            )
            repository.insertKonselingLog(logEntry)
            onResult(true, if (isEn) "Counseling note updated." else "Catatan konseling rahasia berhasil diperbarui.")
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleKonselingRecord(getApplication(), updated)
                PocketBaseSyncManager.uploadSingleKonselingLog(getApplication(), logEntry)
            }
        }
    }

    fun toggleKonselingStatus(
        record: KonselingRecord,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
        val currentUser = _currentUsername.value?.trim().orEmpty()
        val currentName = _waliName.value.trim().ifBlank { "Wali Asuh" }
        if (_currentUserRole.value != UserRole.WALI ||
            !KonselingHelper.matchesOwnerOnly(
                ownerUsername = record.ownerWaliUsername,
                ownerName = record.ownerWaliName,
                currentUsername = currentUser,
                currentWaliName = currentName
            )
        ) {
            onResult(false, if (isEn) "Access denied." else "Akses ditolak.")
            return
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val activeSchool = record.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val currentlyCompleted = KonselingHelper.isCompleted(record.status)
            val nextStatus = if (currentlyCompleted) KonselingHelper.STATUS_DIPANTAU else KonselingHelper.STATUS_SELESAI
            val updated = record.copy(status = nextStatus, updatedAt = now)
            repository.updateKonselingRecord(updated)

            val logEntry = KonselingLog(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                recordId = record.id,
                studentId = record.studentId,
                studentName = record.studentName,
                studentClass = record.studentClass,
                ownerWaliUsername = record.ownerWaliUsername,
                ownerWaliName = record.ownerWaliName,
                isConfidential = true,
                actionType = if (currentlyCompleted) KonselingHelper.LOG_REOPEN else KonselingHelper.LOG_COMPLETE,
                summary = if (currentlyCompleted) {
                    "Pantau kembali: ${record.title}"
                } else {
                    "Dituntaskan / Membaik: ${record.title}"
                },
                actorName = currentName,
                actorUsername = currentUser,
                schoolId = activeSchool,
                createdAt = now
            )
            repository.insertKonselingLog(logEntry)
            onResult(
                true,
                if (currentlyCompleted) {
                    if (isEn) "Reopened for follow-up." else "Status diubah ke Perlu Tindak Lanjut."
                } else {
                    if (isEn) "Marked as resolved." else "Catatan ditandai Selesai / Membaik."
                }
            )
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleKonselingRecord(getApplication(), updated)
                PocketBaseSyncManager.uploadSingleKonselingLog(getApplication(), logEntry)
            }
        }
    }

    fun deleteKonselingRecord(
        record: KonselingRecord,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
        val currentUser = _currentUsername.value?.trim().orEmpty()
        val currentName = _waliName.value.trim().ifBlank { "Wali Asuh" }
        if (_currentUserRole.value != UserRole.WALI ||
            !KonselingHelper.matchesOwnerOnly(
                ownerUsername = record.ownerWaliUsername,
                ownerName = record.ownerWaliName,
                currentUsername = currentUser,
                currentWaliName = currentName
            )
        ) {
            onResult(false, if (isEn) "Access denied." else "Akses ditolak.")
            return
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val activeSchool = record.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            repository.deleteKonselingRecord(record)

            // Log audit PRIVAT internal konseling SAJA — TIDAK pernah mengirim DeletionLog publik sekolah
            val logEntry = KonselingLog(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                recordId = record.id,
                studentId = record.studentId,
                studentName = record.studentName,
                studentClass = record.studentClass,
                ownerWaliUsername = record.ownerWaliUsername,
                ownerWaliName = record.ownerWaliName,
                isConfidential = true,
                actionType = KonselingHelper.LOG_DELETE,
                summary = "Hapus permanen: ${record.title.ifBlank { KonselingHelper.formatTopicLabel(record.topic, false) }}",
                actorName = currentName,
                actorUsername = currentUser,
                schoolId = activeSchool,
                createdAt = now
            )
            repository.insertKonselingLog(logEntry)
            onResult(true, if (isEn) "Counseling note deleted privately." else "Catatan konseling dihapus (tercatat di log privat Anda).")
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.deleteRemoteKonselingRecord(getApplication(), record.id)
                PocketBaseSyncManager.uploadSingleKonselingLog(getApplication(), logEntry)
            }
        }
    }

    fun sendParentMessage(
        student: ChildStudent,
        messageContent: String,
        senderRole: String,
        onComplete: () -> Unit = {}
    ) {
        val cleanText = messageContent.trim()
        if (cleanText.isBlank()) return
        viewModelScope.launch {
            val activeSchool = student.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val existingLink = repository.getParentAccessLinkByStudentId(student.id, activeSchool)
            val isParentSender = senderRole.equals("PARENT", ignoreCase = true)
            val senderDisplay = if (isParentSender) {
                existingLink?.parentName?.ifBlank { student.motherName.ifBlank { "Orang Tua ${student.name}" } }
                    ?: student.motherName.ifBlank { "Orang Tua ${student.name}" }
            } else {
                _waliName.value.trim().ifBlank { "Wali Asuh" }
            }
            val waliUser = student.assignedWaliUsername.ifBlank {
                if (!isParentSender) _currentUsername.value.orEmpty() else existingLink?.assignedWaliUsername.orEmpty()
            }
            val waliDisplay = student.assignedWaliName.ifBlank {
                if (!isParentSender) _waliName.value else existingLink?.assignedWaliName.orEmpty()
            }
            val msg = ParentMessage(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                studentId = student.id,
                studentName = student.name,
                parentName = existingLink?.parentName?.ifBlank { student.motherName } ?: student.motherName,
                waliUsername = waliUser,
                waliName = waliDisplay,
                senderRole = if (isParentSender) "PARENT" else "WALI",
                senderName = senderDisplay,
                messageContent = cleanText,
                isReadByWali = !isParentSender,
                isReadByParent = isParentSender,
                schoolId = activeSchool,
                createdAt = System.currentTimeMillis()
            )
            repository.insertParentMessage(msg)
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleParentMessage(getApplication(), msg)
            }
        }
    }

    fun markParentMessagesAsRead(studentId: Long, readerRole: String) {
        viewModelScope.launch {
            val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            if (readerRole.equals("PARENT", ignoreCase = true)) {
                repository.markParentMessagesReadByParent(studentId, activeSchool)
            } else {
                repository.markParentMessagesReadByWali(studentId, activeSchool)
            }
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.syncParentRelationsOnlineQuietly(getApplication(), activeSchool, repository)
            }
        }
    }

    fun syncParentRelationsQuietly() {
        if (isUnitTestEnvironment()) return
        val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
        viewModelScope.launch {
            PocketBaseSyncManager.syncParentRelationsOnlineQuietly(getApplication(), activeSchool, repository)
        }
    }

    fun createVisitSession(
        title: String,
        monthYear: String,
        visitDates: List<Long>,
        selectedDistricts: List<String>,
        description: String = "",
        onComplete: (VisitSession?) -> Unit = {}
    ) {
        val role = _currentUserRole.value
        if (role != UserRole.ADMIN && role != UserRole.SUPERADMIN) {
            onComplete(null)
            return
        }
        viewModelScope.launch {
            val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            val now = System.currentTimeMillis()
            val creator = _schoolProfile.value.adminName.ifBlank { _waliName.value.ifBlank { "Admin Sekolah" } }
            val newSession = VisitSession(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                title = title.trim(),
                monthYear = monthYear.trim().ifBlank { VisitHelper.buildDefaultMonthYear(now) },
                sessionDatesCsv = VisitHelper.formatDatesCsv(visitDates),
                selectedDistrictsCsv = VisitHelper.formatDistrictsCsv(selectedDistricts),
                description = description.trim(),
                isActive = true,
                createdBy = creator,
                schoolId = activeSchool,
                createdAt = now,
                updatedAt = now
            )
            repository.insertVisitSession(newSession)
            onComplete(newSession)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleVisitSession(getApplication(), newSession)
            }
        }
    }

    fun updateVisitSession(
        session: VisitSession,
        title: String,
        monthYear: String,
        visitDates: List<Long>,
        selectedDistricts: List<String>,
        description: String = "",
        onComplete: (VisitSession?) -> Unit = {}
    ) {
        val role = _currentUserRole.value
        if (role != UserRole.ADMIN && role != UserRole.SUPERADMIN) {
            onComplete(null)
            return
        }
        viewModelScope.launch {
            val updated = session.copy(
                title = title.trim(),
                monthYear = monthYear.trim().ifBlank { session.monthYear },
                sessionDatesCsv = VisitHelper.formatDatesCsv(visitDates),
                selectedDistrictsCsv = VisitHelper.formatDistrictsCsv(selectedDistricts),
                description = description.trim(),
                updatedAt = System.currentTimeMillis()
            )
            repository.updateVisitSession(updated)
            onComplete(updated)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleVisitSession(getApplication(), updated)
            }
        }
    }

    fun toggleVisitSessionActive(session: VisitSession, onComplete: () -> Unit = {}) {
        if (_currentUserRole.value != UserRole.ADMIN && _currentUserRole.value != UserRole.SUPERADMIN) {
            onComplete()
            return
        }
        viewModelScope.launch {
            val updated = session.copy(
                isActive = !session.isActive,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateVisitSession(updated)
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleVisitSession(getApplication(), updated)
            }
        }
    }

    fun deleteVisitSession(session: VisitSession, onComplete: () -> Unit = {}) {
        if (_currentUserRole.value != UserRole.ADMIN && _currentUserRole.value != UserRole.SUPERADMIN) {
            onComplete()
            return
        }
        viewModelScope.launch {
            repository.deleteVisitSession(session)
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.deleteRemoteVisitSession(getApplication(), session.id)
            }
        }
    }

    fun recordStudentVisitCheckIn(
        session: VisitSession,
        student: ChildStudent,
        visitDate: Long = System.currentTimeMillis(),
        visitorRelation: String = "Orang Tua / Wali",
        visitorName: String = "",
        checkInMethod: String = VisitRecord.METHOD_BARCODE,
        notes: String = "",
        onResult: (added: Boolean, message: String, record: VisitRecord?) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch {
            val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
            val normalizedDay = VisitHelper.normalizeDayStart(visitDate)
            val existingInSession = repository.getVisitRecordsBySessionList(session.id)
            val duplicate = VisitHelper.findVisitForStudent(
                sessionRecords = existingInSession,
                student = student,
                targetDayStart = normalizedDay
            )
            if (duplicate != null) {
                val dateFmt = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())
                val msg = if (isEn) {
                    "${student.name} already checked in on ${dateFmt.format(java.util.Date(normalizedDay))} (${session.title})."
                } else {
                    "${student.name} sudah tercatat hadir kunjungan pada ${dateFmt.format(java.util.Date(normalizedDay))} (${session.title})."
                }
                onResult(false, msg, duplicate)
                return@launch
            }

            val now = System.currentTimeMillis()
            val activeSchool = session.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val studentCleanNik = com.example.util.StudentCsvParser.cleanDigitsOnly(student.nik)
            val cleanDistrict = VisitHelper.normalizeDistrict(student.district)
            val outOfSchedule = session.getDistrictsList().isNotEmpty() &&
                !VisitHelper.isStudentInSessionSchedule(session, student)

            val record = VisitRecord(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                sessionId = session.id,
                sessionTitle = session.title,
                visitDate = normalizedDay,
                checkedInAt = now,
                studentId = student.id,
                studentName = student.name,
                studentNik = studentCleanNik,
                studentNis = student.nis,
                studentClass = student.classGrade,
                studentDorm = student.dormitory,
                studentDistrict = cleanDistrict,
                visitorName = visitorName.trim(),
                visitorRelation = visitorRelation.trim().ifBlank { "Orang Tua / Wali" },
                checkInMethod = checkInMethod,
                isOutOfScheduleDistrict = outOfSchedule,
                waliName = _waliName.value.ifBlank { "Wali Asuh" },
                notes = notes.trim(),
                schoolId = activeSchool,
                createdAt = now,
                updatedAt = now
            )
            repository.insertVisitRecord(record)
            val successMsg = if (isEn) {
                "Visit check-in recorded for ${student.name}${if (outOfSchedule) " (outside scheduled district)" else ""}."
            } else {
                "Kehadiran kunjungan ${student.name} berhasil dicatat${if (outOfSchedule) " (di luar jadwal kecamatan)" else ""}."
            }
            onResult(true, successMsg, record)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleVisitRecord(getApplication(), record)
            }
        }
    }

    fun deleteVisitRecord(record: VisitRecord, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteVisitRecord(record)
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.deleteRemoteVisitRecord(getApplication(), record.id)
            }
        }
    }

    fun syncVisitsOnlineQuietly() {
        if (isUnitTestEnvironment()) return
        val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
        viewModelScope.launch {
            PocketBaseSyncManager.syncVisitsOnlineQuietly(getApplication(), activeSchool, repository)
        }
    }

    fun deleteHaidRecordById(id: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteHaidRecordById(id)
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.deleteRemoteHaidRecord(getApplication(), id)
            }
        }
    }

    fun findStudentByCode(code: String): ChildStudent? {
        val q = code.trim()
        if (q.isEmpty()) return null
        val qDigits = com.example.util.StudentCsvParser.cleanDigitsOnly(q)
        return students.value.firstOrNull { student ->
            val cleanNik = com.example.util.StudentCsvParser.cleanDigitsOnly(student.nik)
            (cleanNik.isNotBlank() && (cleanNik.equals(q, ignoreCase = true) || (qDigits.length >= 6 && cleanNik == qDigits))) ||
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
            repository.deleteVisitRecordsBySchool(targetId)
            repository.deleteVisitSessionsBySchool(targetId)
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.deleteStudentsBySchool(getApplication(), targetId)
                PocketBaseSyncManager.deleteWaliAccountsBySchool(getApplication(), targetId)
                PocketBaseSyncManager.deleteShiftMessagesBySchool(getApplication(), targetId)
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

            val existingByNik = existingList
                .mapNotNull { st ->
                    com.example.util.StudentCsvParser.cleanDigitsOnly(st.nik)
                        .takeIf { it.isNotBlank() }
                        ?.let { it to st }
                }
                .toMap()
            val existingByNis = existingList.filter { it.nis.isNotBlank() }.associateBy { it.nis.trim() }
            val existingByName = existingList.groupBy { it.name.trim().lowercase() }

            val stampedStudents = studentList.map { rawImported ->
                val imported = rawImported.copy(
                    nik = com.example.util.StudentCsvParser.cleanDigitsOnly(rawImported.nik),
                    noKk = com.example.util.StudentCsvParser.cleanDigitsOnly(rawImported.noKk)
                )
                val matchedExisting = if (!replace) {
                    (imported.nik.takeIf { it.isNotBlank() }?.let { existingByNik[it] })
                        ?: (imported.nis.takeIf { it.isNotBlank() }?.let { existingByNis[it.trim()] })
                        ?: existingByName[imported.name.trim().lowercase()]?.firstOrNull()
                } else null

                if (matchedExisting != null) {
                    imported.copy(
                        id = matchedExisting.id,
                        schoolId = targetSchoolId,
                        nis = imported.nis.ifBlank { matchedExisting.nis },
                        nik = imported.nik.ifBlank { com.example.util.StudentCsvParser.cleanDigitsOnly(matchedExisting.nik) },
                        noKk = imported.noKk.ifBlank { com.example.util.StudentCsvParser.cleanDigitsOnly(matchedExisting.noKk) },
                        birthPlace = imported.birthPlace.ifBlank { matchedExisting.birthPlace },
                        birthDate = imported.birthDate.ifBlank { matchedExisting.birthDate },
                        motherName = imported.motherName.ifBlank { matchedExisting.motherName },
                        address = imported.address.ifBlank { matchedExisting.address },
                        district = imported.district.ifBlank { matchedExisting.district },
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
                    val assignedId = if (imported.id > 0L) imported.id else PocketBaseSyncManager.generateCloudSafeId()
                    imported.copy(id = assignedId, schoolId = targetSchoolId)
                }
            }
            repository.insertAllStudents(stampedStudents)
            onComplete(stampedStudents.size)

            if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                viewModelScope.launch {
                    if (replace) {
                        PocketBaseSyncManager.deleteStudentsBySchool(getApplication(), targetSchoolId)
                    }
                    PocketBaseSyncManager.uploadStudents(getApplication(), stampedStudents)
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
                    val assignedId = if (it.id > 0L) it.id else PocketBaseSyncManager.generateCloudSafeId()
                    it.copy(id = assignedId, schoolId = "SRT1-KDR")
                }
                repository.insertAllStudents(stamped)
                if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                    viewModelScope.launch {
                        PocketBaseSyncManager.deleteStudentsBySchool(getApplication(), "SRT1-KDR")
                        PocketBaseSyncManager.uploadStudents(getApplication(), stamped)
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
                val assignedId = if (acc.id > 0L) acc.id else PocketBaseSyncManager.generateCloudSafeId()
                acc.copy(id = assignedId, schoolId = targetSchoolId)
            }
            repository.insertAllWaliAccounts(stampedAccounts)
            onComplete(stampedAccounts.size)

            if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                viewModelScope.launch {
                    if (replace) {
                        PocketBaseSyncManager.deleteWaliAccountsBySchool(getApplication(), targetSchoolId)
                    }
                    PocketBaseSyncManager.uploadWaliAccounts(getApplication(), stampedAccounts)
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

    private fun notifHistoryKey(schoolId: String): String {
        val clean = schoolId.trim().uppercase().ifBlank { "SRT1-KDR" }
        return "notified_plan_ids_$clean"
    }

    private fun hasPlanBeenNotified(schoolId: String, recordId: Long): Boolean {
        val raw = notifPrefs.getString(notifHistoryKey(schoolId), "").orEmpty()
        if (raw.isBlank()) return false
        return raw.split(",").any { it == recordId.toString() }
    }

    private fun markPlanNotified(schoolId: String, recordId: Long) {
        if (recordId <= 0L) return
        val key = notifHistoryKey(schoolId)
        val existing = notifPrefs.getString(key, "").orEmpty()
            .split(",")
            .filter { it.isNotBlank() }
            .toMutableList()
        val idStr = recordId.toString()
        if (!existing.contains(idStr)) {
            existing.add(idStr)
            val trimmed = if (existing.size > 300) existing.takeLast(300) else existing
            notifPrefs.edit().putString(key, trimmed.joinToString(",")).apply()
        }
    }

    private data class DeletionActorContext(
        val role: UserRole,
        val username: String?,
        val waliName: String,
        val seenVersion: Long
    )

    private val _deletionSeenVersion = MutableStateFlow(0L)

    val deletionLogs: StateFlow<List<DeletionLog>> = combine(
        repository.allDeletionLogs,
        _schoolProfile,
        _currentUserRole
    ) { logs, profile, role ->
        val tenant = profile.tenantCode.ifBlank { "SRT1-KDR" }
        val filtered = if (role == UserRole.SUPERADMIN) {
            logs
        } else {
            logs.filter { it.schoolId.equals(tenant, ignoreCase = true) }
        }
        filtered.sortedByDescending { it.createdAt }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val deletionActorContextFlow = combine(
        _currentUserRole,
        _currentUsername,
        _waliName,
        _deletionSeenVersion
    ) { role, username, wali, version ->
        DeletionActorContext(role, username, wali, version)
    }

    val unreadDeletionLogsCount: StateFlow<Int> = combine(
        deletionLogs,
        _schoolProfile,
        deletionActorContextFlow
    ) { logs, profile, ctx ->
        if (ctx.role == UserRole.NOT_LOGGED_IN || ctx.role == UserRole.PARENT) {
            0
        } else {
            val tenant = profile.tenantCode.ifBlank { "SRT1-KDR" }
            val seenIds = getSeenDeletionLogIds(tenant, ctx.username, ctx.waliName)
            logs.count { log ->
                log.id !in seenIds && !isDeletionLogByCurrentUser(log, ctx.username, ctx.waliName)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private fun seenDeletionKey(schoolId: String, username: String?, waliDisplayName: String): String {
        val cleanSchool = schoolId.trim().uppercase().ifBlank { "SRT1-KDR" }
        val actorToken = username?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
            ?: waliDisplayName.trim().lowercase().replace(Regex("[^a-z0-9]"), "_").ifBlank { "wali" }
        return "seen_deletion_log_ids_${cleanSchool}_$actorToken"
    }

    private fun getSeenDeletionLogIds(
        schoolId: String,
        username: String? = _currentUsername.value,
        waliDisplayName: String = _waliName.value
    ): Set<Long> {
        val raw = notifPrefs.getString(seenDeletionKey(schoolId, username, waliDisplayName), "").orEmpty()
        if (raw.isBlank()) return emptySet()
        return raw.split(",").mapNotNull { it.toLongOrNull() }.toSet()
    }

    private fun markDeletionLogIdsSeen(
        schoolId: String,
        ids: List<Long>,
        username: String? = _currentUsername.value,
        waliDisplayName: String = _waliName.value
    ) {
        val validIds = ids.filter { it > 0L }
        if (validIds.isEmpty()) return
        val key = seenDeletionKey(schoolId, username, waliDisplayName)
        val existing = notifPrefs.getString(key, "").orEmpty()
            .split(",")
            .filter { it.isNotBlank() }
            .toMutableList()
        var changed = false
        validIds.forEach { id ->
            val str = id.toString()
            if (!existing.contains(str)) {
                existing.add(str)
                changed = true
            }
        }
        if (changed) {
            val trimmed = if (existing.size > 500) existing.takeLast(500) else existing
            notifPrefs.edit().putString(key, trimmed.joinToString(",")).apply()
        }
        _deletionSeenVersion.value = _deletionSeenVersion.value + 1L
    }

    fun isDeletionLogByCurrentUser(
        log: DeletionLog,
        username: String? = _currentUsername.value,
        waliDisplayName: String = _waliName.value
    ): Boolean {
        val cleanUser = username?.trim().orEmpty()
        val logUser = log.deletedByUsername.trim()
        if (cleanUser.isNotBlank() && logUser.isNotBlank()) {
            return cleanUser.equals(logUser, ignoreCase = true)
        }
        val cleanWali = waliDisplayName.trim()
        val logActor = log.deletedBy.trim()
        return cleanWali.isNotBlank() &&
            !cleanWali.equals("Wali Asuh", ignoreCase = true) &&
            cleanWali.equals(logActor, ignoreCase = true)
    }

    fun markDeletionLogsRead() {
        val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
        val currentIds = deletionLogs.value.map { it.id }
        markDeletionLogIdsSeen(activeSchool, currentIds)
    }

    private suspend fun createAndRecordDeletionLog(
        category: String,
        studentId: Long,
        studentName: String,
        studentClass: String,
        recordId: Long,
        deletedSummary: String,
        schoolId: String
    ): DeletionLog {
        val cleanSchool = schoolId.trim().ifBlank { "SRT1-KDR" }
        val actorDisplayName = _waliName.value.trim().ifBlank { "Wali Asuh" }
        val actorUsername = _currentUsername.value?.trim().orEmpty()
        val newId = PocketBaseSyncManager.generateCloudSafeId()
        val log = DeletionLog(
            id = newId,
            category = category,
            studentId = studentId,
            studentName = studentName,
            studentClass = studentClass,
            recordId = recordId,
            deletedSummary = deletedSummary,
            deletedBy = actorDisplayName,
            deletedByUsername = actorUsername,
            schoolId = cleanSchool,
            createdAt = System.currentTimeMillis()
        )
        // Tandai langsung sebagai sudah dilihat oleh penghapus agar badge loncengnya tetap 0
        markDeletionLogIdsSeen(cleanSchool, listOf(newId), actorUsername, actorDisplayName)
        repository.insertDeletionLog(log)
        return log
    }

    private fun deletionPushNotifKey(schoolId: String): String {
        val clean = schoolId.trim().uppercase().ifBlank { "SRT1-KDR" }
        return "notified_deletion_log_ids_$clean"
    }

    fun processIncomingRemoteDeletionLogs(
        newLogs: List<DeletionLog>,
        syncedSchoolId: String
    ): Int {
        if (_currentUserRole.value == UserRole.NOT_LOGGED_IN ||
            _currentUserRole.value == UserRole.PARENT ||
            _currentUserRole.value == UserRole.GURU_NGAJI
        ) return 0
        val cleanSchool = syncedSchoolId.trim().ifBlank { "SRT1-KDR" }
        if (!shouldTriggerNotificationForStudent(cleanSchool)) return 0

        val pushKey = deletionPushNotifKey(cleanSchool)
        val alreadyPushed = notifPrefs.getString(pushKey, "").orEmpty()
            .split(",")
            .mapNotNull { it.toLongOrNull() }
            .toMutableSet()

        val peerLogs = newLogs.filter { log ->
            log.id > 0L &&
                log.id !in alreadyPushed &&
                !isDeletionLogByCurrentUser(log)
        }
        if (peerLogs.isEmpty()) return 0

        peerLogs.forEach { alreadyPushed.add(it.id) }
        val trimmed = alreadyPushed.toList().let { if (it.size > 400) it.takeLast(400) else it }
        notifPrefs.edit().putString(pushKey, trimmed.joinToString(",")).apply()

        NotificationHelper.showDeletionAlertNotification(
            context = getApplication(),
            logs = peerLogs.sortedByDescending { it.createdAt },
            schoolName = _schoolProfile.value.shortName
        )
        return peerLogs.size
    }

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

    /**
     * Filter ketat notifikasi rencana berobat baru yang datang dari sinkronisasi PocketBase perangkat lain.
     * User di Sekolah B atau yang belum login tidak akan pernah menerima notifikasi dari Sekolah A.
     */
    fun shouldNotifyIncomingSyncedPlan(
        record: MedicalRecord,
        studentSchoolId: String,
        syncedSchoolId: String
    ): Boolean {
        if (_currentUserRole.value == UserRole.NOT_LOGGED_IN ||
            _currentUserRole.value == UserRole.PARENT ||
            _currentUserRole.value == UserRole.GURU_NGAJI
        ) return false
        val targetSyncedSchool = syncedSchoolId.trim().ifBlank { "SRT1-KDR" }
        val targetStudentSchool = studentSchoolId.trim().ifBlank { targetSyncedSchool }
        if (!shouldTriggerNotificationForStudent(targetSyncedSchool)) return false
        if (!shouldTriggerNotificationForStudent(targetStudentSchool)) return false
        if (!record.status.equals("PLANNED", ignoreCase = true) || record.isDirectTreatment) return false
        return !hasPlanBeenNotified(targetSyncedSchool, record.id)
    }

    fun processIncomingRemoteMedicalRecords(
        newRecords: List<MedicalRecord>,
        schoolStudents: List<ChildStudent>,
        syncedSchoolId: String
    ): Int {
        if (_currentUserRole.value == UserRole.NOT_LOGGED_IN) return 0
        val studentsById = schoolStudents.associateBy { it.id }
        var notifiedCount = 0
        newRecords.forEach { rec ->
            val matchedStudent = studentsById[rec.studentId]
            val studentSchool = matchedStudent?.schoolId?.ifBlank { syncedSchoolId } ?: syncedSchoolId
            if (shouldNotifyIncomingSyncedPlan(rec, studentSchool, syncedSchoolId)) {
                markPlanNotified(syncedSchoolId, rec.id)
                NotificationHelper.showSickStudentNotification(
                    context = getApplication(),
                    studentName = matchedStudent?.name ?: rec.studentName,
                    className = matchedStudent?.classGrade ?: "",
                    roomNumber = matchedStudent?.roomNumber?.ifBlank { matchedStudent.dormitory }
                        ?: rec.studentDormitory,
                    complaint = rec.complaint,
                    faskes = rec.faskes,
                    schoolName = _schoolProfile.value.shortName,
                    notificationId = ((rec.id % 100000L).toInt()).coerceAtLeast(1)
                )
                notifiedCount++
            }
        }
        return notifiedCount
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
                id = PocketBaseSyncManager.generateCloudSafeId(),
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
            markPlanNotified(activeSchool, newId)
            // Multi-tenant filter: Hanya kirim notifikasi resmi sistem Android jika user login di sekolah yang sama atau SuperAdmin
            if (shouldTriggerNotificationForStudent(activeSchool)) {
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
            // Sinkronisasi latar belakang ke PocketBase Cloud
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleRecord(getApplication(), recordWithId, activeSchool)
            }
        }
    }

    // Mark departed (Dianter / Siap Jalan)
    fun markDeparted(recordId: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = repository.getRecordById(recordId).firstOrNull()
            if (existing != null) {
                val updated = existing.copy(status = "DEPARTED", editedAt = now)
                repository.updateRecord(updated)
                if (!isUnitTestEnvironment()) {
                    val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                    PocketBaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
                }
            } else {
                repository.updateStatus(recordId, "DEPARTED")
            }
        }
    }

    // Mark departed with specific referral faskes and escort
    fun markDepartedToFaskes(recordId: Long, targetFaskes: String, escortName: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = repository.getRecordById(recordId).firstOrNull()
            if (existing != null) {
                val updated = existing.copy(
                    status = "DEPARTED",
                    faskes = targetFaskes,
                    escortName = escortName,
                    editedAt = now
                )
                repository.updateRecord(updated)
                if (!isUnitTestEnvironment()) {
                    val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                    PocketBaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
                }
            } else {
                repository.updateDepartureWithFaskes(recordId, "DEPARTED", targetFaskes, escortName)
            }
        }
    }

    fun updateRecord(record: MedicalRecord) {
        viewModelScope.launch {
            val updated = record.copy(editedAt = System.currentTimeMillis())
            repository.updateRecord(updated)
            if (!isUnitTestEnvironment()) {
                val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                PocketBaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
            }
        }
    }

    // Update custom status
    fun updateStatus(recordId: Long, status: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = repository.getRecordById(recordId).firstOrNull()
            if (existing != null) {
                val updated = existing.copy(status = status, editedAt = now)
                repository.updateRecord(updated)
                if (!isUnitTestEnvironment()) {
                    val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                    PocketBaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
                }
            } else {
                repository.updateStatus(recordId, status)
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
            val now = System.currentTimeMillis()
            val updated = record.copy(
                status = "COMPLETED",
                treatmentDate = now,
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
                editedAt = now,
                notes = if (additionalNotes.isNotBlank()) "${record.notes}\n$additionalNotes".trim() else record.notes
            )
            repository.updateRecord(updated)
            // Sinkronisasi latar belakang ke PocketBase Cloud
            if (!isUnitTestEnvironment()) {
                val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                PocketBaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
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
                id = PocketBaseSyncManager.generateCloudSafeId(),
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
            // Sinkronisasi latar belakang ke PocketBase Cloud
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleRecord(getApplication(), recordWithId, activeSchool)
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
                PocketBaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
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
                PocketBaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
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
                PocketBaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
            }
        }
    }

    // Kirim notifikasi tes untuk memverifikasi fitur di perangkat pengguna (terikat tenant aktif)
    fun sendTestNotification() {
        if (_currentUserRole.value == UserRole.NOT_LOGGED_IN) return
        NotificationHelper.showTestNotification(
            context = getApplication(),
            schoolName = _schoolProfile.value.shortName
        )
    }

    // Mark Control Visit as completed
    fun markControlCompleted(recordId: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = repository.getRecordById(recordId).firstOrNull()
            if (existing != null) {
                val updated = existing.copy(isControlCompleted = true, editedAt = now)
                repository.updateRecord(updated)
                if (!isUnitTestEnvironment()) {
                    val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                    PocketBaseSyncManager.uploadSingleRecord(getApplication(), updated, activeSchool)
                }
            } else {
                repository.updateControlCompleted(recordId, true)
            }
        }
    }

    // Delete record
    fun deleteRecord(record: MedicalRecord) {
        viewModelScope.launch {
            val student = if (record.studentId > 0L) repository.getStudentById(record.studentId) else null
            val activeSchool = student?.schoolId?.ifBlank { null }
                ?: _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            val dateTs = record.treatmentDate ?: record.planDate
            val detail = record.complaint.ifBlank { record.diagnosis }.ifBlank { "Rekam Medis" }
            val summary = "${record.faskes.ifBlank { "Faskes" }} • $detail (${DateUtils.formatShortDate(dateTs)})"
            val log = createAndRecordDeletionLog(
                category = DeletionLog.CATEGORY_BEROBAT,
                studentId = record.studentId,
                studentName = student?.name?.ifBlank { null } ?: record.studentName,
                studentClass = student?.classGrade.orEmpty(),
                recordId = record.id,
                deletedSummary = summary,
                schoolId = activeSchool
            )
            repository.deleteRecord(record)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.deleteRemoteRecord(getApplication(), record.id)
                PocketBaseSyncManager.uploadSingleDeletionLog(getApplication(), log)
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
        district: String = "",
        roomNumber: String = "",
        jenjang: String = "",
        parentPhone: String = ""
    ) {
        viewModelScope.launch {
            val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            val resolvedJenjang = jenjang.trim().ifBlank {
                com.example.util.StudentCsvParser.deriveJenjang(classGrade)
            }
            val student = ChildStudent(
                id = PocketBaseSyncManager.generateCloudSafeId(),
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
                nik = com.example.util.StudentCsvParser.cleanDigitsOnly(nik),
                noKk = com.example.util.StudentCsvParser.cleanDigitsOnly(noKk),
                birthPlace = birthPlace.trim(),
                birthDate = birthDate.trim(),
                motherName = motherName.trim(),
                address = address.trim(),
                district = district.trim(),
                roomNumber = roomNumber.trim(),
                jenjang = resolvedJenjang,
                parentPhone = com.example.util.StudentCsvParser.cleanPhoneNumber(parentPhone),
                schoolId = activeSchool
            )
            val newId = repository.insertStudent(student)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleStudent(getApplication(), student.copy(id = newId))
            }
        }
    }

    fun updateStudent(student: ChildStudent, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val cleanParentPhone = com.example.util.StudentCsvParser.cleanPhoneNumber(student.parentPhone)
            val sanitizedStudent = student.copy(
                name = student.name.trim(),
                nis = student.nis.trim(),
                dormitory = student.dormitory.trim(),
                classGrade = student.classGrade.trim(),
                birthPlace = student.birthPlace.trim(),
                birthDate = student.birthDate.trim(),
                motherName = student.motherName.trim(),
                address = student.address.trim(),
                district = student.district.trim(),
                roomNumber = student.roomNumber.trim(),
                jenjang = student.jenjang.trim(),
                nik = com.example.util.StudentCsvParser.cleanDigitsOnly(student.nik),
                noKk = com.example.util.StudentCsvParser.cleanDigitsOnly(student.noKk),
                parentPhone = cleanParentPhone
            )
            repository.updateStudent(sanitizedStudent)
            val activeSchool = sanitizedStudent.schoolId.ifBlank { _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" } }
            val existingLink = repository.getParentAccessLinkByStudentId(sanitizedStudent.id, activeSchool)
            if (existingLink != null) {
                val syncedLink = existingLink.copy(
                    studentName = sanitizedStudent.name,
                    studentNis = sanitizedStudent.nis,
                    studentClass = sanitizedStudent.classGrade,
                    studentDorm = sanitizedStudent.dormitory,
                    parentName = sanitizedStudent.motherName.ifBlank { existingLink.parentName },
                    parentPhone = cleanParentPhone,
                    assignedWaliUsername = sanitizedStudent.assignedWaliUsername,
                    assignedWaliName = sanitizedStudent.assignedWaliName,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateParentAccessLink(syncedLink)
                if (!isUnitTestEnvironment()) {
                    PocketBaseSyncManager.uploadSingleParentAccessLink(getApplication(), syncedLink)
                }
            }
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleStudent(getApplication(), sanitizedStudent)
            }
        }
    }

    fun deleteStudent(student: ChildStudent) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.deleteRemoteStudent(getApplication(), student.id)
            }
        }
    }

    // ==========================================
    // POCKETBASE CLOUD DATABASE SYNC & MANAGEMENT
    // ==========================================
    private val _isCloudConfigured = MutableStateFlow(PocketBaseConfig.isConfigured(application))
    val isCloudConfigured: StateFlow<Boolean> = _isCloudConfigured.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _cloudSyncMessage = MutableStateFlow<String?>(null)
    val cloudSyncMessage: StateFlow<String?> = _cloudSyncMessage.asStateFlow()

    fun refreshCloudConfig() {
        _isCloudConfigured.value = PocketBaseConfig.isConfigured(getApplication())
    }

    fun testCloudConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
            _isSyncing.value = true
            val result = PocketBaseSyncManager.testConnection(getApplication())
            _isSyncing.value = false
            refreshCloudConfig()
            if (result.isSuccess) {
                val msg = result.getOrNull() ?: if (isEn) "PocketBase connection succeeded!" else "Koneksi PocketBase Berhasil!"
                _cloudSyncMessage.value = msg
                onResult(true, msg)
            } else {
                val err = result.exceptionOrNull()?.message ?: if (isEn) "Failed to connect to PocketBase server." else "Gagal terhubung ke server PocketBase."
                _cloudSyncMessage.value = err
                onResult(false, err)
            }
        }
    }

    fun syncAllWithCloud(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
            _isSyncing.value = true
            val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            PocketBaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
            val result = runUnifiedSchoolCloudSync(activeSchool)
            _isSyncing.value = false
            refreshCloudConfig()
            if (result.isSuccess) {
                val msg = result.getOrNull() ?: if (isEn) "Sync succeeded!" else "Sinkronisasi Berhasil!"
                _cloudSyncMessage.value = msg
                onResult(true, msg)
            } else {
                val err = result.exceptionOrNull()?.message ?: if (isEn) "Failed to sync with PocketBase." else "Gagal sinkronisasi dengan PocketBase."
                _cloudSyncMessage.value = err
                onResult(false, err)
            }
        }
    }

    fun refreshSuperadminCloudData(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
            if (!PocketBaseConfig.isConfigured(getApplication())) {
                onResult(false, if (isEn) "PocketBase server is not configured yet." else "Server PocketBase belum terkonfigurasi.")
                return@launch
            }
            _isSyncing.value = true
            try {
                PocketBaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
                _isSyncing.value = false
                onResult(
                    true,
                    if (isEn) {
                        "Latest school, admin & Wali Asuh data loaded from PocketBase."
                    } else {
                        "Data sekolah, akun admin, & wali terbaru berhasil dimuat dari PocketBase."
                    }
                )
            } catch (e: Exception) {
                _isSyncing.value = false
                onResult(
                    false,
                    if (isEn) {
                        "Failed to load data from PocketBase: ${e.localizedMessage}"
                    } else {
                        "Gagal memuat data dari PocketBase: ${e.localizedMessage}"
                    }
                )
            }
        }
    }

    fun uploadAllStudentsToCloud(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
            _isSyncing.value = true
            val activeSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            val studentList = students.value
            val result = PocketBaseSyncManager.uploadStudents(getApplication(), studentList)
            if (result.isSuccess) {
                PocketBaseSyncManager.uploadWaliAccounts(getApplication(), waliAccounts.value)
                PocketBaseSyncManager.uploadSchoolProfile(
                    getApplication(),
                    _schoolProfile.value,
                    getProfileUpdatedAt(activeSchool).takeIf { it > 0L } ?: System.currentTimeMillis()
                )
                val count = result.getOrNull() ?: 0
                val msg = if (isEn) {
                    "Successfully uploaded $count student records ($activeSchool) to PocketBase!"
                } else {
                    "Berhasil mengunggah $count data siswa ($activeSchool) ke PocketBase!"
                }
                _isSyncing.value = false
                _cloudSyncMessage.value = msg
                onResult(true, msg)
            } else {
                _isSyncing.value = false
                val err = result.exceptionOrNull()?.message ?: if (isEn) {
                    "Failed to upload student data to PocketBase."
                } else {
                    "Gagal upload data siswa ke PocketBase."
                }
                _cloudSyncMessage.value = err
                onResult(false, err)
            }
        }
    }

    // --- Authentication & Role Management ---
    suspend fun executeLogin(user: String, pass: String, rememberMe: Boolean = false): LoginResult {
        val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
        val cleanUser = user.trim()
        val cleanPass = pass.trim()

        if (cleanUser.isEmpty() || cleanPass.isEmpty()) {
            return LoginResult.Error(
                if (isEn) "Username and password are required." else "Username dan password wajib diisi."
            )
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
            if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                viewModelScope.launch {
                    PocketBaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
                }
            }
            return LoginResult.SuccessSuperAdmin
        }

        // 1. Cek Kredensial Administrator Resmi (ekowahyudiadmin / SRT1-KDR)
        if (cleanUser.equals("ekowahyudiadmin", ignoreCase = true)) {
            if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                val existingPilotLocal = repository.getSchoolByAdminUsername("ekowahyudiadmin")
                    ?: repository.getSchoolByCode("SRT1-KDR")
                if (existingPilotLocal == null) {
                    PocketBaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
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
                return LoginResult.Error(
                    if (isEn) {
                        "Sorry, this account is currently suspended by the admin."
                    } else {
                        "Maaf akun sedang disuspend oleh admin"
                    }
                )
            }

            if (cleanPass == expectedPilotPassword) {
                _currentUserRole.value = UserRole.ADMIN
                _currentUsername.value = "ekowahyudiadmin"
                if (!_schoolProfile.value.tenantCode.equals("SRT1-KDR", ignoreCase = true)) {
                    persistSchoolProfile(SchoolProfile(), markCustomized = false)
                }
                val pilotDisplayName = pilotOverride?.picName?.trim()?.takeIf { it.isNotBlank() }
                    ?: _schoolProfile.value.adminName.trim().takeIf { it.isNotBlank() }
                    ?: "Eko Wahyudi"
                _waliName.value = pilotDisplayName
                if (rememberMe) {
                    authPrefs.edit()
                        .putString("saved_role", "ADMIN")
                        .putString("saved_username", "ekowahyudiadmin")
                        .putString("saved_wali_name", pilotDisplayName)
                        .apply()
                }
                if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                    val activeCode = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                    viewModelScope.launch {
                        runUnifiedSchoolCloudSync(activeCode)
                    }
                }
                return LoginResult.SuccessAdmin
            }
            return LoginResult.Error(
                if (isEn) {
                    "Incorrect password for Admin 'ekowahyudiadmin'. Please check again."
                } else {
                    "Password untuk Admin 'ekowahyudiadmin' salah. Silakan periksa kembali."
                }
            )
        }

        // 1b. Cek Kredensial Khusus Anak Superadmin (Masuk sebagai Wali Asuh + Hak Atur Tata Letak Dashboard)
        if (cleanUser.equals("anaksuperadmin", ignoreCase = true)) {
            if (cleanPass != "Woyowoyo12@") {
                return LoginResult.Error(
                    if (isEn) {
                        "Incorrect password for 'anaksuperadmin'. Please check again."
                    } else {
                        "Password untuk akun 'anaksuperadmin' salah. Silakan periksa kembali."
                    }
                )
            }
            val displayName = "Anak Superadmin"
            val activeSchoolCode = _schoolProfile.value.tenantCode.trim().ifBlank { "SRT1-KDR" }
            _currentUserRole.value = UserRole.WALI
            _currentUsername.value = "anaksuperadmin"
            _waliName.value = displayName
            if (rememberMe) {
                authPrefs.edit()
                    .putString("saved_role", "WALI")
                    .putString("saved_username", "anaksuperadmin")
                    .putString("saved_wali_name", displayName)
                    .apply()
            }
            if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                viewModelScope.launch {
                    PocketBaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
                    val remoteList = PocketBaseSyncManager.fetchAllRemoteSchoolProfiles(getApplication())
                    if (remoteList.isNotEmpty()) {
                        val updatedMap = _remoteSchoolProfiles.value.toMutableMap()
                        remoteList.forEach { (prof, ts) ->
                            val codeUpper = prof.tenantCode.trim().uppercase()
                            if (codeUpper.isNotBlank() &&
                                codeUpper != PbSchoolProfileDto.GLOBAL_DASHBOARD_LAYOUT_SCHOOL_ID &&
                                codeUpper != PbSchoolProfileDto.GLOBAL_DASHBOARD_BANNER_SCHOOL_ID
                            ) {
                                updatedMap[codeUpper] = prof.copy(tenantCode = codeUpper) to ts
                            }
                        }
                        _remoteSchoolProfiles.value = updatedMap
                    }
                    runUnifiedSchoolCloudSync(activeSchoolCode)
                }
            }
            val specialAccount = WaliAccount(
                id = 0L,
                name = displayName,
                username = "anaksuperadmin",
                password = "",
                dormitory = "Semua Asrama",
                schoolId = activeSchoolCode
            )
            return LoginResult.SuccessWali(specialAccount)
        }

        // Helper mencari admin sekolah & akun wali di database lokal
        suspend fun findLocalSchoolAdmin(username: String): SchoolRegistration? {
            return repository.getSchoolByAdminUsername(username)
                ?: repository.getAllSchoolRegistrationsList().firstOrNull {
                    it.generatedAdminUsername.equals(username, ignoreCase = true)
                }
        }

        suspend fun findLocalWali(username: String, password: String): WaliAccount? {
            val all = repository.getAllWaliAccountsList()
            val candidates = all.filter {
                it.username.trim().equals(username, ignoreCase = true) ||
                    it.username.trim().replace(" ", "").equals(username.replace(" ", ""), ignoreCase = true) ||
                    it.name.trim().equals(username, ignoreCase = true) ||
                    it.name.trim().replace(" ", "").equals(username.replace(" ", ""), ignoreCase = true) ||
                    (it.phone.isNotBlank() && it.phone.trim().equals(username, ignoreCase = true))
            }
            return candidates.firstOrNull { it.password.trim() == password }
                ?: candidates.firstOrNull()
                ?: repository.getWaliAccountByUsername(username)
        }

        suspend fun findLocalParentLink(username: String, password: String): ParentAccessLink? {
            val all = repository.getAllParentAccessLinksList()
            val candidates = all.filter {
                it.username.trim().equals(username, ignoreCase = true) ||
                    it.username.trim().replace(" ", "").equals(username.replace(" ", ""), ignoreCase = true)
            }
            return candidates.firstOrNull { it.password.trim() == password }
                ?: candidates.firstOrNull()
                ?: repository.getParentAccessLinkByUsername(username)
        }

        // Jika belum ditemukan di lokal (misal baru login di perangkat lain), tarik katalog auth dari PocketBase
        var matchedSchool = findLocalSchoolAdmin(cleanUser)
        var account = if (matchedSchool == null) findLocalWali(cleanUser, cleanPass) else null
        var parentLink = if (matchedSchool == null && account == null) findLocalParentLink(cleanUser, cleanPass) else null
        if (matchedSchool == null && account == null && parentLink == null && !isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
            PocketBaseSyncManager.syncAuthCatalogFromCloud(getApplication(), repository)
            matchedSchool = findLocalSchoolAdmin(cleanUser)
            if (matchedSchool == null) {
                account = findLocalWali(cleanUser, cleanPass)
            }
            if (matchedSchool == null && account == null) {
                parentLink = findLocalParentLink(cleanUser, cleanPass)
            }
        }

        // 2. Cek Akun Admin Sekolah Terdaftar / Baru
        if (matchedSchool != null && matchedSchool.generatedAdminPassword == cleanPass) {
            if (matchedSchool.status == "SUSPENDED") {
                return LoginResult.Error(
                    if (isEn) {
                        "Sorry, this account is currently suspended by the admin."
                    } else {
                        "Maaf akun sedang disuspend oleh admin"
                    }
                )
            }
            if (matchedSchool.status != "APPROVED") {
                return LoginResult.Error(
                    if (isEn) {
                        "This school registration has not been approved yet (Status: ${matchedSchool.status})."
                    } else {
                        "Pengajuan pendaftaran sekolah ini belum disetujui (Status: ${matchedSchool.status})."
                    }
                )
            }
            _currentUserRole.value = UserRole.ADMIN
            _currentUsername.value = matchedSchool.generatedAdminUsername
            persistSchoolProfile(buildProfileForSchool(matchedSchool), markCustomized = false)
            val adminDisplayName = _schoolProfile.value.adminName.trim()
                .ifBlank { matchedSchool.picName.trim().ifBlank { matchedSchool.generatedAdminUsername } }
            _waliName.value = adminDisplayName
            if (rememberMe) {
                authPrefs.edit()
                    .putString("saved_role", "ADMIN")
                    .putString("saved_username", matchedSchool.generatedAdminUsername)
                    .putString("saved_wali_name", adminDisplayName)
                    .apply()
            }
            if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                viewModelScope.launch {
                    runUnifiedSchoolCloudSync(matchedSchool.generatedSchoolCode)
                }
            }
            return LoginResult.SuccessAdmin
        }

        // 3. Cek Akun Wali Asuh
        if (account != null) {
            if (account.password.trim() != cleanPass) {
                return LoginResult.Error(
                    if (isEn) {
                        "Incorrect password for '${account.name}'. Please check capitalization and try again."
                    } else {
                        "Password untuk akun '${account.name}' salah. Silakan periksa kembali huruf besar/kecil."
                    }
                )
            }

            // Cek apakah sekolah akun wali asuh ini sedang disuspend
            val matchedSchoolWali = repository.getSchoolByCode(account.schoolId)
                ?: repository.getAllSchoolRegistrationsList().firstOrNull {
                    it.generatedSchoolCode.equals(account.schoolId, ignoreCase = true)
                }
            if (matchedSchoolWali != null && matchedSchoolWali.status == "SUSPENDED") {
                return LoginResult.Error(
                    if (isEn) {
                        "Sorry, this account is currently suspended by the admin."
                    } else {
                        "Maaf akun sedang disuspend oleh admin"
                    }
                )
            }

            val resolvedRole = if (account.isGuruNgaji()) UserRole.GURU_NGAJI else UserRole.WALI
            _currentUserRole.value = resolvedRole
            _waliName.value = account.name
            _currentUsername.value = account.username

            // Sinkronkan tenant aktif sesuai sekolah akun Wali Asuh / Guru Ngaji
            val activeSchoolCode = when {
                matchedSchoolWali != null -> {
                    persistSchoolProfile(buildProfileForSchool(matchedSchoolWali), markCustomized = false)
                    matchedSchoolWali.generatedSchoolCode
                }
                account.schoolId.isNotBlank() -> {
                    val cleanCode = account.schoolId.trim()
                    if (!_schoolProfile.value.tenantCode.equals(cleanCode, ignoreCase = true)) {
                        persistSchoolProfile(
                            if (cleanCode.equals("SRT1-KDR", ignoreCase = true)) {
                                SchoolProfile()
                            } else {
                                SchoolProfile(
                                    schoolName = cleanCode,
                                    shortName = cleanCode,
                                    tenantCode = cleanCode
                                )
                            },
                            markCustomized = false
                        )
                    }
                    cleanCode
                }
                else -> _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            }

            if (rememberMe) {
                authPrefs.edit()
                    .putString("saved_role", if (resolvedRole == UserRole.GURU_NGAJI) "GURU_NGAJI" else "WALI")
                    .putString("saved_wali_name", account.name)
                    .putString("saved_username", account.username)
                    .apply()
            }

            if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                viewModelScope.launch {
                    runUnifiedSchoolCloudSync(activeSchoolCode)
                }
            }
            return if (resolvedRole == UserRole.GURU_NGAJI) {
                LoginResult.SuccessGuruNgaji(account)
            } else {
                LoginResult.SuccessWali(account)
            }
        }

        // 4. Cek Akun Orang Tua (Kartu Akses Orang Tua)
        if (parentLink != null) {
            if (parentLink.password.trim() != cleanPass) {
                return LoginResult.Error(
                    if (isEn) {
                        "Incorrect password for parent account '${parentLink.username}'. Please check capitalization and try again."
                    } else {
                        "Password untuk akun orang tua '${parentLink.username}' salah. Silakan periksa kembali huruf besar/kecil."
                    }
                )
            }

            val matchedSchoolParent = repository.getSchoolByCode(parentLink.schoolId)
                ?: repository.getAllSchoolRegistrationsList().firstOrNull {
                    it.generatedSchoolCode.equals(parentLink.schoolId, ignoreCase = true)
                }
            if (matchedSchoolParent != null && matchedSchoolParent.status == "SUSPENDED") {
                return LoginResult.Error(
                    if (isEn) {
                        "Sorry, this school account is currently suspended by the admin."
                    } else {
                        "Maaf akun sekolah sedang disuspend oleh admin"
                    }
                )
            }

            val displayParentName = parentLink.parentName.trim().ifBlank { "Orang Tua ${parentLink.studentName}" }
            _currentUserRole.value = UserRole.PARENT
            _waliName.value = displayParentName
            _currentUsername.value = parentLink.username
            _currentParentStudentId.value = parentLink.studentId

            val activeSchoolCode = when {
                matchedSchoolParent != null -> {
                    persistSchoolProfile(buildProfileForSchool(matchedSchoolParent), markCustomized = false)
                    matchedSchoolParent.generatedSchoolCode
                }
                parentLink.schoolId.isNotBlank() -> {
                    val cleanCode = parentLink.schoolId.trim()
                    if (!_schoolProfile.value.tenantCode.equals(cleanCode, ignoreCase = true)) {
                        persistSchoolProfile(
                            if (cleanCode.equals("SRT1-KDR", ignoreCase = true)) {
                                SchoolProfile()
                            } else {
                                SchoolProfile(
                                    schoolName = cleanCode,
                                    shortName = cleanCode,
                                    tenantCode = cleanCode
                                )
                            },
                            markCustomized = false
                        )
                    }
                    cleanCode
                }
                else -> _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
            }

            if (rememberMe) {
                authPrefs.edit()
                    .putString("saved_role", "PARENT")
                    .putString("saved_wali_name", displayParentName)
                    .putString("saved_username", parentLink.username)
                    .putLong("saved_parent_student_id", parentLink.studentId)
                    .apply()
            }

            if (!isUnitTestEnvironment() && PocketBaseConfig.isConfigured(getApplication())) {
                viewModelScope.launch {
                    runUnifiedSchoolCloudSync(activeSchoolCode)
                }
            }
            return LoginResult.SuccessParent(parentLink)
        }

        return LoginResult.Error(
            if (isEn) {
                "Username '$cleanUser' was not found. Make sure the account has been registered by an Administrator or Wali Asuh."
            } else {
                "Username '$cleanUser' tidak ditemukan. Pastikan akun telah terdaftar di Administrator atau Wali Asuh."
            }
        )
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
                id = PocketBaseSyncManager.generateCloudSafeId(),
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
            PocketBaseConfig.clearSchoolRegistrationDeletedTokens(getApplication(), saved)
            onComplete(saved)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleSchoolRegistration(getApplication(), saved)
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
                PocketBaseSyncManager.uploadSingleSchoolRegistration(getApplication(), updated)
            }
        }
    }

    fun rejectSchoolRegistration(
        registration: SchoolRegistration,
        reason: String,
        onComplete: (SchoolRegistration) -> Unit = {}
    ) {
        viewModelScope.launch {
            val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
            val updated = registration.copy(
                status = "REJECTED",
                rejectionReason = reason.trim().ifBlank {
                    if (isEn) {
                        "Registration data does not meet document requirements."
                    } else {
                        "Data pendaftaran belum memenuhi kelengkapan berkas."
                    }
                }
            )
            repository.updateSchoolRegistration(updated)
            onComplete(updated)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadSingleSchoolRegistration(getApplication(), updated)
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
                PocketBaseSyncManager.uploadSingleSchoolRegistration(getApplication(), updated)
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
                PocketBaseSyncManager.uploadSingleSchoolRegistration(getApplication(), updated)
            }
        }
    }

    fun deleteSchoolRegistration(
        registration: SchoolRegistration,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            PocketBaseConfig.markSchoolRegistrationDeleted(getApplication(), registration)
            if (registration.generatedSchoolCode.isNotBlank()) {
                repository.deleteStudentsBySchool(registration.generatedSchoolCode)
                repository.deleteWaliAccountsBySchool(registration.generatedSchoolCode)
                repository.deleteTabunganTransactionsBySchool(registration.generatedSchoolCode)
                repository.deleteTabunganLogsBySchool(registration.generatedSchoolCode)
                repository.deleteKonselingRecordsBySchool(registration.generatedSchoolCode)
                repository.deleteKonselingLogsBySchool(registration.generatedSchoolCode)
                repository.deleteSchoolRegistrationsByCode(registration.generatedSchoolCode)
            }
            if (registration.id > 0L) {
                repository.deleteSchoolRegistrationById(registration.id)
            }
            repository.deleteSchoolRegistration(registration)
            onComplete()
            if (!isUnitTestEnvironment()) {
                if (registration.generatedSchoolCode.isNotBlank()) {
                    PocketBaseSyncManager.deleteAllSchoolData(getApplication(), registration.generatedSchoolCode)
                }
                PocketBaseSyncManager.deleteRemoteSchoolRegistration(getApplication(), registration)
            }
        }
    }

    // --- Ganti Password Mandiri oleh Admin Sekolah (Username Dikunci, Tersinkron ke Superadmin) ---
    fun updateAdminSelfPassword(
        newPassword: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
            val cleanPass = newPassword.trim()
            if (cleanPass.length < 6) {
                onResult(
                    false,
                    if (isEn) "New password must be at least 6 characters." else "Password baru minimal 6 karakter."
                )
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
                    PocketBaseSyncManager.uploadSingleSchoolRegistration(getApplication(), updated)
                }
                onResult(
                    true,
                    if (isEn) {
                        "Admin password updated & synced to Superadmin!"
                    } else {
                        "Password admin berhasil diperbarui & tersinkron ke Superadmin!"
                    }
                )
            } else if (activeUsername.equals("ekowahyudiadmin", ignoreCase = true) ||
                activeTenantCode.equals("SRT1-KDR", ignoreCase = true)
            ) {
                val profile = _schoolProfile.value
                val lockedUsername = "ekowahyudiadmin"
                val pilotReg = SchoolRegistration(
                    id = PocketBaseSyncManager.generateCloudSafeId(),
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
                    PocketBaseSyncManager.uploadSingleSchoolRegistration(getApplication(), saved)
                }
                onResult(
                    true,
                    if (isEn) {
                        "Admin password updated & synced to Superadmin!"
                    } else {
                        "Password admin berhasil diperbarui & tersinkron ke Superadmin!"
                    }
                )
            } else {
                onResult(
                    false,
                    if (isEn) {
                        "School admin account data not found. Please sync your data first."
                    } else {
                        "Data akun admin sekolah tidak ditemukan. Silakan sinkronkan data terlebih dahulu."
                    }
                )
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
        _currentParentStudentId.value = 0L
        authPrefs.edit().clear().apply()
        NotificationHelper.cancelAllNotifications(getApplication())
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
        role: String = WaliAccount.ROLE_WALI,
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

            val normalizedRole = if (role.trim().equals(WaliAccount.ROLE_GURU_NGAJI, ignoreCase = true)) {
                WaliAccount.ROLE_GURU_NGAJI
            } else {
                WaliAccount.ROLE_WALI
            }

            val newAccount = WaliAccount(
                id = PocketBaseSyncManager.generateCloudSafeId(),
                name = name.trim(),
                username = finalUsername,
                password = password,
                dormitory = dormitory.ifBlank { "Asrama Putra" },
                phone = phone.trim(),
                schoolId = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" },
                role = normalizedRole
            )

            val id = repository.insertWaliAccount(newAccount)
            val saved = newAccount.copy(id = id)
            onComplete(saved)
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadWaliAccounts(getApplication(), listOf(saved))
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
                PocketBaseSyncManager.uploadWaliAccounts(getApplication(), listOf(updated))
            }
        }
    }

    fun deleteWaliAccount(account: WaliAccount, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteWaliAccount(account)
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.deleteRemoteWaliAccount(getApplication(), account.id)
            }
        }
    }

    fun updateWaliAccount(account: WaliAccount, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateWaliAccount(account)
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.uploadWaliAccounts(getApplication(), listOf(account))
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
            val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
            if (_currentUsername.value.equals("anaksuperadmin", ignoreCase = true)) {
                onResult(
                    false,
                    if (isEn) {
                        "System account 'anaksuperadmin' credentials cannot be changed from profile."
                    } else {
                        "Kredensial akun khusus 'anaksuperadmin' dikelola sistem dan tidak dapat diubah dari profil."
                    }
                )
                return@launch
            }
            val s = AppLanguageManager.getStrings(getApplication()).nav
            val cleanPass = newPassword.trim()
            if (cleanPass.length < 6) {
                onResult(false, s.vmPasswordMinError)
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
                val savedRole = authPrefs.getString("saved_role", "")
                if (savedRole == "WALI" || savedRole == "GURU_NGAJI") {
                    authPrefs.edit()
                        .putString("saved_wali_name", cleanName)
                        .putString("saved_username", updated.username)
                        .apply()
                }
                onResult(true, s.vmPasswordUpdatedSuccess)
                if (!isUnitTestEnvironment()) {
                    PocketBaseSyncManager.uploadWaliAccounts(getApplication(), listOf(updated))
                }
            } else {
                val defaultSchool = _schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
                val generatedUser = cleanName.lowercase().replace(" ", "").filter { it.isLetterOrDigit() }.ifBlank { "wali" }
                val newAcc = WaliAccount(
                    id = PocketBaseSyncManager.generateCloudSafeId(),
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
                onResult(true, s.vmPasswordCreatedSuccess)
                if (!isUnitTestEnvironment()) {
                    PocketBaseSyncManager.uploadWaliAccounts(getApplication(), listOf(newAcc.copy(id = insertedId)))
                }
            }
        }
    }

    /**
     * Hapus akun pribadi Wali Asuh yang sedang login (lokal Room + PocketBase).
     * Data siswa & catatan sekolah tetap aman terjaga untuk Admin & Wali lain.
     */
    fun deleteCurrentWaliAccount(
        passwordConfirmation: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
            if (_currentUsername.value.equals("anaksuperadmin", ignoreCase = true)) {
                onResult(
                    false,
                    if (isEn) {
                        "System account 'anaksuperadmin' cannot be deleted."
                    } else {
                        "Akun khusus 'anaksuperadmin' tidak dapat dihapus."
                    }
                )
                return@launch
            }
            val s = AppLanguageManager.getStrings(getApplication()).nav
            val cleanPass = passwordConfirmation.trim()
            if (cleanPass.isBlank()) {
                onResult(false, s.vmDeletePassRequired)
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
                    onResult(false, s.vmDeletePassMismatch)
                    return@launch
                }
                repository.deleteWaliAccount(account)
                if (!isUnitTestEnvironment()) {
                    PocketBaseSyncManager.deleteRemoteWaliAccount(getApplication(), account.id)
                }
            }

            _waliName.value = "Wali Asuh"
            logout()
            onResult(true, s.vmDeleteSuccess)
        }
    }

    /**
     * Hapus akun Admin Sekolah & seluruh data tenant sekolah yang sedang aktif (lokal Room + PocketBase).
     */
    fun deleteCurrentSchoolAdminAccount(
        passwordConfirmation: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val isEn = AppLanguageManager.getStrings(getApplication()).isEnglish
            val cleanPass = passwordConfirmation.trim()
            if (cleanPass.isBlank()) {
                onResult(
                    false,
                    if (isEn) {
                        "Enter the School Admin password to confirm."
                    } else {
                        "Masukkan password Admin Sekolah untuk konfirmasi."
                    }
                )
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
                onResult(
                    false,
                    if (isEn) "School admin account data not found." else "Data akun admin sekolah tidak ditemukan."
                )
                return@launch
            }

            val expectedPassword = existingReg?.generatedAdminPassword?.takeIf { it.isNotBlank() }
                ?: if (isPilotAdmin) "Woyowoyo12@" else ""

            if (expectedPassword.isNotBlank() && cleanPass != expectedPassword) {
                onResult(
                    false,
                    if (isEn) {
                        "Incorrect School Admin password. Account deletion cancelled."
                    } else {
                        "Password Admin Sekolah salah. Penghapusan akun dibatalkan."
                    }
                )
                return@launch
            }

            val targetSchoolId = existingReg?.generatedSchoolCode?.takeIf { it.isNotBlank() }
                ?: activeTenantCode

            // 1. Bersihkan seluruh data sekolah di Room lokal (students cascade ke medical_records)
            if (targetSchoolId.isNotBlank()) {
                repository.deleteKonselingLogsBySchool(targetSchoolId)
                repository.deleteKonselingRecordsBySchool(targetSchoolId)
                repository.deleteTabunganLogsBySchool(targetSchoolId)
                repository.deleteTabunganTransactionsBySchool(targetSchoolId)
                repository.deleteDeletionLogsBySchool(targetSchoolId)
                repository.deleteParentMessagesBySchool(targetSchoolId)
                repository.deleteParentAccessLinksBySchool(targetSchoolId)
                repository.deleteFastingRecordsBySchool(targetSchoolId)
                repository.deleteFastingSessionsBySchool(targetSchoolId)
                repository.deleteVisitRecordsBySchool(targetSchoolId)
                repository.deleteVisitSessionsBySchool(targetSchoolId)
                repository.deleteMengajiRecordsBySchool(targetSchoolId)
                repository.deleteHaidRecordsBySchool(targetSchoolId)
                repository.deleteShiftMessagesBySchool(targetSchoolId)
                repository.deleteStudentsBySchool(targetSchoolId)
                repository.deleteWaliAccountsBySchool(targetSchoolId)
            }

            // 2. Bersihkan seluruh data sekolah + registrasi akun admin di PocketBase
            if (existingReg != null) {
                PocketBaseConfig.markSchoolRegistrationDeleted(getApplication(), existingReg)
            }
            if (!isUnitTestEnvironment()) {
                if (targetSchoolId.isNotBlank()) {
                    PocketBaseSyncManager.deleteAllSchoolData(getApplication(), targetSchoolId)
                }
                if (existingReg != null) {
                    PocketBaseSyncManager.deleteRemoteSchoolRegistration(getApplication(), existingReg)
                }
            }

            // 3. Hapus entri pendaftaran sekolah dari Room lokal
            if (targetSchoolId.isNotBlank()) {
                repository.deleteSchoolRegistrationsByCode(targetSchoolId)
            }
            if (existingReg != null) {
                repository.deleteSchoolRegistration(existingReg)
            }

            // 4. Reset preferensi profil sekolah & sesi login
            schoolPrefs.edit().clear().apply()
            _schoolProfile.value = SchoolProfile()
            _waliName.value = "Wali Asuh"
            logout()
            onResult(
                true,
                if (isEn) {
                    "School Admin account and all school data permanently deleted."
                } else {
                    "Akun Admin Sekolah dan seluruh data sekolah berhasil dihapus permanen."
                }
            )
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
                id = PocketBaseSyncManager.generateCloudSafeId(),
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
                PocketBaseSyncManager.uploadSingleShiftMessage(getApplication(), saved)
            }
        }
    }

    fun toggleShiftMessageCompleted(message: ShiftMessage, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val newStatus = !message.isCompleted
            val completedBy = if (newStatus) _waliName.value else ""
            val completedAt = System.currentTimeMillis()
            repository.updateShiftMessageCompleted(message.id, newStatus, completedBy, completedAt)
            onComplete()
            if (!isUnitTestEnvironment()) {
                repository.getShiftMessageById(message.id)?.let { updated ->
                    PocketBaseSyncManager.uploadSingleShiftMessage(getApplication(), updated)
                }
            }
        }
    }

    fun deleteShiftMessage(message: ShiftMessage, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteShiftMessage(message)
            onComplete()
            if (!isUnitTestEnvironment()) {
                PocketBaseSyncManager.deleteRemoteShiftMessage(getApplication(), message.id)
            }
        }
    }
}

