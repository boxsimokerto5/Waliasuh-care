package com.example

import android.app.Application
import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.SchoolProfile
import com.example.data.model.SchoolRegistration
import com.example.data.model.StudentReligionHelper
import com.example.ui.HealthViewModel
import com.example.ui.LoginResult
import com.example.ui.components.CategoryHubOverlay
import com.example.ui.components.DashboardCategoryHub
import com.example.ui.dialogs.ClaimChristianStudentsSheet
import com.example.ui.dialogs.PilihanAksesOrangTuaSheet
import com.example.ui.dialogs.SholatAttendanceSheet
import com.example.ui.dialogs.StudentDetailDialog
import com.example.ui.dialogs.StudentDetailTab
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.ParentPortalScreen
import com.example.ui.screens.SuperAdminScreen
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ChristianWorshipAndSuperAdminRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `claiming Christian student updates religion, syncs ParentAccessLink, and excludes student from Sholat while keeping Puasa`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        val db = AppDatabase.getDatabase(app)
        val vm = HealthViewModel(app)
        val schoolId = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

        val christianStudent = ChildStudent(
            id = 9801L,
            name = "Daniel Christian",
            nis = "980101",
            gender = "L",
            age = 14,
            dormitory = "Asrama Putra",
            classGrade = "VIII-A",
            schoolId = schoolId,
            religion = ""
        )
        val muslimStudent = ChildStudent(
            id = 9802L,
            name = "Ahmad Fauzan",
            nis = "980201",
            gender = "L",
            age = 14,
            dormitory = "Asrama Putra",
            classGrade = "VIII-A",
            schoolId = schoolId,
            religion = ""
        )
        db.childStudentDao().insert(christianStudent)
        db.childStudentDao().insert(muslimStudent)

        val linkChristian = ParentAccessLink(
            id = 9811L,
            studentId = 9801L,
            studentName = christianStudent.name,
            studentNis = christianStudent.nis,
            studentClass = christianStudent.classGrade,
            studentDorm = christianStudent.dormitory,
            parentName = "Pak Yohanes",
            username = "ortu_daniel_9801",
            password = "Ortu#9801",
            schoolId = schoolId,
            canViewSholat = true,
            canViewMengaji = true,
            canViewFasting = true
        )
        db.parentAccessLinkDao().insert(linkChristian)

        // Non-admin / belum login sebagai Admin wajib ditolak
        var blockedCount = -1
        var blockedMsg = ""
        vm.claimStudentsChristianReligion(listOf(christianStudent), true) { count, msg ->
            blockedCount = count
            blockedMsg = msg
        }
        assertEquals(0, blockedCount)
        assertTrue(blockedMsg.contains("Admin", ignoreCase = true))

        // Login sebagai Admin Sekolah lalu klaim -> berhasil
        val adminReg = SchoolRegistration(
            id = 9815L,
            schoolName = "SRT 1 Kediri",
            city = "Kediri",
            address = "Kediri",
            picName = "Admin Test",
            picEmail = "admin@test.id",
            picPhone = "08123456789",
            estimatedWaliCount = 2,
            status = "APPROVED",
            generatedSchoolCode = schoolId,
            generatedAdminUsername = "admin_chris_9815",
            generatedAdminPassword = "Admin#9815"
        )
        db.schoolRegistrationDao().insert(adminReg)
        val adminLogin = vm.executeLogin("admin_chris_9815", "Admin#9815", rememberMe = false)
        assertTrue(adminLogin is LoginResult.SuccessAdmin)

        var claimedCount = 0
        vm.claimStudentsChristianReligion(listOf(christianStudent), true) { count, _ ->
            claimedCount = count
        }
        var loops = 0
        while (loops < 30 && claimedCount == 0) {
            ShadowLooper.idleMainLooper()
            delay(30)
            loops++
        }
        assertEquals(1, claimedCount)
        db.schoolRegistrationDao().delete(adminReg)
        vm.logout()

        val updatedDbStudent = db.childStudentDao().getById(9801L)!!
        val updatedDbLink = db.parentAccessLinkDao().getByStudentId(9801L, schoolId)!!
        assertTrue(StudentReligionHelper.isChristian(updatedDbStudent))
        assertTrue(StudentReligionHelper.isChristian(updatedDbStudent, updatedDbLink))
        assertFalse(StudentReligionHelper.isChristian(muslimStudent))

        val testStudents: List<ChildStudent> = listOf(updatedDbStudent, muslimStudent)
        // Verify SholatAttendanceSheet excludes Christian student while showing Muslim student
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                SholatAttendanceSheet(
                    students = testStudents,
                    sholatRecords = emptyList(),
                    haidRecords = emptyList(),
                    currentWaliUsername = "walitest",
                    currentWaliName = "Wali Test",
                    schoolProfile = SchoolProfile(schoolName = "SRT 1 Kediri", tenantCode = schoolId),
                    onSaveRecord = { _, _, _, _, _, _, _ -> },
                    onBulkMarkJamaah = { _, _, _, _ -> },
                    onDeleteRecord = {},
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Ahmad Fauzan").assertExists()
        composeTestRule.onNodeWithText("Daniel Christian").assertDoesNotExist()

        // Verify PilihanAksesOrangTuaSheet hides Sholat & Mengaji checkboxes for Christian student and shows automatic Christian portal notice
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                PilihanAksesOrangTuaSheet(
                    students = testStudents,
                    parentAccessLinks = listOf(updatedDbLink),
                    isStudentClaimedByCurrentWali = { true },
                    isAdmin = true,
                    onEnsureAccessLink = {},
                    onUpdatePermissions = { _, _, _, _, _, _, _, _ -> },
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("info_christian_auto_portal_9801").assertExists()
        composeTestRule.onNodeWithTag("chk_access_sholat_9801").assertDoesNotExist()
        composeTestRule.onNodeWithTag("chk_access_mengaji_9801").assertDoesNotExist()

        db.parentAccessLinkDao().delete(updatedDbLink)
        db.childStudentDao().delete(updatedDbStudent)
        db.childStudentDao().delete(muslimStudent)
        Unit
    }

    @Test
    fun `HealthViewModel saves single and batch Christian worship logs and deletes record`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        val db = AppDatabase.getDatabase(app)
        val vm = HealthViewModel(app)
        val schoolId = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

        val student = ChildStudent(
            id = 9821L,
            name = "Maria Kristin",
            nis = "982101",
            gender = "P",
            age = 15,
            dormitory = "Asrama Putri",
            classGrade = "IX-B",
            schoolId = schoolId,
            religion = StudentReligionHelper.RELIGION_KRISTEN
        )
        db.childStudentDao().insert(student)

        var singleOk = false
        vm.saveChristianWorshipActivity(
            student = student,
            activityType = ChristianWorshipRecord.TYPE_DOA_PAGI,
            activityDate = System.currentTimeMillis(),
            title = "Renungan Pagi Mazmur 23",
            notes = "Mengikuti saat teduh pagi dengan tertib"
        ) { ok, _ -> singleOk = ok }

        var batchCount = 0
        vm.saveChristianWorshipBatch(
            students = listOf(student),
            activityType = ChristianWorshipRecord.TYPE_KE_GEREJA,
            activityDate = System.currentTimeMillis(),
            title = "Ibadah Minggu Pagi",
            notes = "Hadir di GKJW bersama pendamping"
        ) { count, _ -> batchCount = count }

        var loops = 0
        while (loops < 40 && (!singleOk || batchCount == 0 || vm.christianWorshipRecords.value.size < 2)) {
            ShadowLooper.idleMainLooper()
            delay(30)
            loops++
        }
        assertTrue(singleOk)
        assertEquals(1, batchCount)

        val saved = ChristianWorshipHelper.recordsForStudent(vm.christianWorshipRecords.value, student)
        assertEquals(2, saved.size)
        assertTrue(saved.any { it.activityType == ChristianWorshipRecord.TYPE_DOA_PAGI && it.title.contains("Mazmur 23") })
        assertTrue(saved.any { it.activityType == ChristianWorshipRecord.TYPE_KE_GEREJA })

        var deletedOk = false
        vm.deleteChristianWorshipRecord(saved.first()) { ok, _ -> deletedOk = ok }
        loops = 0
        while (loops < 30 && (!deletedOk || ChristianWorshipHelper.recordsForStudent(vm.christianWorshipRecords.value, student).size > 1)) {
            ShadowLooper.idleMainLooper()
            delay(30)
            loops++
        }
        assertTrue(deletedOk)
        assertEquals(1, ChristianWorshipHelper.recordsForStudent(vm.christianWorshipRecords.value, student).size)

        db.christianWorshipDao().getAllRecordsIncludingDeletedList().filter { it.studentId == 9821L }.forEach {
            db.christianWorshipDao().deleteRecord(it)
        }
        db.childStudentDao().delete(student)
        Unit
    }

    @Test
    fun `CategoryHubOverlay KRISTEN displays 6 tiles and invokes all callbacks`() {
        val student = ChildStudent(
            id = 9831L,
            name = "Joshua Emmanuel",
            nis = "983101",
            gender = "L",
            age = 15,
            dormitory = "Asrama Putra",
            classGrade = "IX-A",
            schoolId = "SRT1-KDR",
            religion = StudentReligionHelper.RELIGION_KRISTEN
        )
        val record = ChristianWorshipRecord(
            id = 9832L,
            schoolId = "SRT1-KDR",
            studentId = 9831L,
            studentName = student.name,
            activityType = ChristianWorshipRecord.TYPE_DOA_MALAM,
            dateKey = ChristianWorshipHelper.todayDateKey(),
            title = "Doa Malam",
            notes = "Tertib"
        )
        var openedClaim = false
        var openedInputType = ""
        var openedReport = false
        var openedBible = false
        var openedChurch = false

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                CategoryHubOverlay(
                    category = DashboardCategoryHub.KRISTEN,
                    schoolProfile = SchoolProfile(schoolName = "SRT 1 Kediri", shortName = "SRT 1 Kediri", tenantCode = "SRT1-KDR"),
                    activePlans = emptyList(),
                    inpatientCount = 0,
                    fastingSessions = emptyList(),
                    fastingRecords = emptyList(),
                    activeHaidRecords = emptyList(),
                    suciRecords = emptyList(),
                    shiftMessages = emptyList(),
                    students = listOf(student),
                    claimedFosterStudents = listOf(student),
                    christianWorshipRecords = listOf(record),
                    onDismiss = {},
                    onNavigateToPlan = {},
                    onNavigateToRecords = {},
                    onOpenCreatePlan = {},
                    onOpenDirectTreatment = {},
                    onOpenFaskesInfo = {},
                    onOpenInputPuasa = {},
                    onOpenDaftarPuasa = {},
                    onOpenChecklistPuasa = {},
                    onOpenRankingPuasa = {},
                    onOpenInputHaid = {},
                    onOpenDaftarHaid = {},
                    onOpenDaftarSuci = {},
                    onOpenFiqihHaid = {},
                    onOpenShiftMessages = {},
                    onOpenCreateShiftMessage = {},
                    onOpenShiftHistory = {},
                    onOpenClaimChristianStudents = { openedClaim = true },
                    onOpenInputChristianWorship = { _, type, _ -> openedInputType = type },
                    onOpenChristianReport = { openedReport = true },
                    onOpenBibleFromKristen = { openedBible = true },
                    onOpenSundayChurch = { openedChurch = true }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("category_hub_overlay_kristen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_kristen_hub_info").assertExists()

        composeTestRule.onNodeWithTag("list_category_hub_content")
            .performScrollToNode(hasTestTag("tile_klaim_kristen"))
        composeTestRule.onNodeWithTag("tile_klaim_kristen").performClick()
        assertTrue(openedClaim)

        composeTestRule.onNodeWithTag("list_category_hub_content")
            .performScrollToNode(hasTestTag("tile_input_ibadah_kristen"))
        composeTestRule.onNodeWithTag("tile_input_ibadah_kristen").performClick()
        assertEquals(ChristianWorshipRecord.TYPE_DOA_PAGI, openedInputType)

        composeTestRule.onNodeWithTag("list_category_hub_content")
            .performScrollToNode(hasTestTag("tile_pembinaan_kristen"))
        composeTestRule.onNodeWithTag("tile_pembinaan_kristen").performClick()
        assertEquals(ChristianWorshipRecord.TYPE_AYAT_HAFALAN, openedInputType)

        composeTestRule.onNodeWithTag("list_category_hub_content")
            .performScrollToNode(hasTestTag("tile_laporan_kristen"))
        composeTestRule.onNodeWithTag("tile_laporan_kristen").performClick()
        assertTrue(openedReport)

        composeTestRule.onNodeWithTag("list_category_hub_content")
            .performScrollToNode(hasTestTag("tile_alkitab_kristen"))
        composeTestRule.onNodeWithTag("tile_alkitab_kristen").performClick()
        assertTrue(openedBible)

        composeTestRule.onNodeWithTag("list_category_hub_content")
            .performScrollToNode(hasTestTag("tile_jadwal_gereja_minggu"))
        composeTestRule.onNodeWithTag("tile_jadwal_gereja_minggu").performClick()
        assertTrue(openedChurch)
    }

    @Test
    fun `InputChristianWorshipDialog renders ModalBottomSheet slider with colored activity grid and saves log`() {
        val student = ChildStudent(
            id = 9835L,
            name = "Cristin Dea Musa",
            nis = "983501",
            gender = "P",
            age = 15,
            dormitory = "Asrama Putri",
            classGrade = "IX-A",
            schoolId = "SRT1-KDR",
            religion = StudentReligionHelper.RELIGION_KRISTEN
        )
        var savedType = ""
        var savedTitle = ""
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                com.example.ui.dialogs.InputChristianWorshipDialog(
                    christianStudents = listOf(student),
                    initialTab = 0,
                    defaultActivityType = ChristianWorshipRecord.TYPE_DOA_PAGI,
                    targetStudent = student,
                    isSaving = false,
                    onOpenClaimChristian = {},
                    onSaveSingle = { _, actType, _, title, _, _ ->
                        savedType = actType
                        savedTitle = title
                    },
                    onSaveBatch = { _, _, _, _, _ -> },
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("sheet_input_christian_worship").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_christian_input_single").assertExists()
        composeTestRule.onNodeWithTag("tab_christian_input_batch").assertExists()
        composeTestRule.onNodeWithTag("chip_christian_type_KE_GEREJA").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_save_christian_worship").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(ChristianWorshipRecord.TYPE_KE_GEREJA, savedType)
        assertTrue(savedTitle.isNotBlank())
    }

    @Test
    fun `ParentPortalScreen automatically switches to Agama Kristen portal and hides Sholat and Mengaji when student is Christian`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        val db = AppDatabase.getDatabase(app)
        val vm = HealthViewModel(app)
        val schoolId = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

        val student = ChildStudent(
            id = 9841L,
            name = "Grace Natali",
            nis = "984101",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-C",
            motherName = "Ibu Ester",
            assignedWaliUsername = "walitest",
            assignedWaliName = "Ibu Dina",
            schoolId = schoolId,
            religion = StudentReligionHelper.RELIGION_KRISTEN
        )
        db.childStudentDao().insert(student)

        val link = ParentAccessLink(
            id = 9842L,
            studentId = 9841L,
            studentName = student.name,
            studentNis = student.nis,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            studentReligion = StudentReligionHelper.RELIGION_KRISTEN,
            parentName = "Ibu Ester",
            username = "ortu_grace_9841",
            password = "Ortu#9841",
            assignedWaliUsername = "walitest",
            assignedWaliName = "Ibu Dina",
            canViewSholat = true,
            canViewMengaji = true,
            canViewFasting = true,
            schoolId = schoolId
        )
        db.parentAccessLinkDao().insert(link)

        val worshipRec = ChristianWorshipRecord(
            id = 9843L,
            schoolId = schoolId,
            studentId = 9841L,
            studentName = student.name,
            activityType = ChristianWorshipRecord.TYPE_DOA_PAGI,
            dateKey = ChristianWorshipHelper.todayDateKey(),
            title = "Doa Pagi Bersama",
            notes = "Mengikuti renungan pagi dengan sukacita"
        )
        db.christianWorshipDao().insertRecord(worshipRec)

        val loginRes = vm.executeLogin("ortu_grace_9841", "Ortu#9841")
        assertTrue(loginRes is LoginResult.SuccessParent)
        var loops = 0
        while (loops < 30 && vm.activeParentAccessLink.value == null) {
            ShadowLooper.idleMainLooper()
            delay(40)
            loops++
        }

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                ParentPortalScreen(
                    viewModel = vm,
                    onLogout = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Salam Sejahtera", substring = true).assertExists()
        composeTestRule.onNodeWithTag("card_parent_summary_kristen").performScrollTo().assertExists()
        composeTestRule.onNodeWithTag("card_parent_summary_bible").performScrollTo().assertExists()
        composeTestRule.onNodeWithTag("card_parent_summary_fasting").assertExists()
        composeTestRule.onNodeWithTag("card_parent_summary_sholat").assertDoesNotExist()
        composeTestRule.onNodeWithTag("card_parent_summary_mengaji").assertDoesNotExist()
        composeTestRule.onNodeWithTag("card_parent_summary_alquran").assertDoesNotExist()

        composeTestRule.onNodeWithTag("card_parent_summary_bible").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("bible_reader_root").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bible_close_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("bible_reader_root").assertDoesNotExist()

        composeTestRule.onNodeWithTag("card_parent_summary_kristen").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("parent_christian_content").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_parent_christian_pdf").assertExists()
        composeTestRule.onNodeWithTag("btn_parent_christian_open_bible").assertExists().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("bible_reader_root").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bible_close_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("parent_christian_list")
            .performScrollToNode(hasText("Doa Pagi Bersama", substring = true))
        composeTestRule.onNodeWithText("Doa Pagi Bersama", substring = true).assertExists()

        vm.logout()
        db.christianWorshipDao().deleteRecord(worshipRec)
        db.parentAccessLinkDao().delete(link)
        db.childStudentDao().delete(student)
        Unit
    }

    @Test
    fun `SuperAdminScreen displays Wali-Asuh-styled quick menu grid, filters status, and switches tabs`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        val db = AppDatabase.getDatabase(app)

        val pendingReg = SchoolRegistration(
            id = 9851L,
            schoolName = "Sekolah Harapan Nusantara",
            city = "Surabaya",
            address = "Jl. Melati No. 10",
            picName = "Pak Daniel",
            picEmail = "daniel@harapan.sch.id",
            picPhone = "081234567890",
            estimatedWaliCount = 4,
            status = "PENDING",
            generatedSchoolCode = "SHN-SBY"
        )
        val approvedReg = SchoolRegistration(
            id = 9852L,
            schoolName = "Sekolah Rakyat Maju",
            city = "Kediri",
            address = "Jl. Raya No. 2",
            picName = "Ibu Sari",
            picEmail = "sari@maju.sch.id",
            picPhone = "081298765432",
            estimatedWaliCount = 6,
            status = "APPROVED",
            generatedSchoolCode = "SRM-KDR",
            generatedAdminUsername = "admin_srm",
            generatedAdminPassword = "Pass#123"
        )
        db.schoolRegistrationDao().insert(pendingReg)
        db.schoolRegistrationDao().insert(approvedReg)

        val vm = HealthViewModel(app)
        vm.setAppLanguage(AppLanguage.INDONESIAN)
        var loops = 0
        while (loops < 30 && vm.schoolRegistrations.value.none { it.id == 9851L }) {
            ShadowLooper.idleMainLooper()
            delay(30)
            loops++
        }

        composeTestRule.setContent {
            val lang by vm.appLanguage.collectAsStateWithLifecycle()
            val baseContext = LocalContext.current
            val localizedContext = AppLanguageManager.createLocalizedContext(baseContext, lang)
            val appStrings = AppLanguageManager.stringsFor(lang)
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration,
                LocalAppStrings provides appStrings
            ) {
                SuperAdminScreen(
                    viewModel = vm,
                    onLogout = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Existing header texts & Superadmin quick menu hub card are present
        composeTestRule.onNodeWithText("Portal Superadmin").assertIsDisplayed()
        composeTestRule.onNodeWithText("Status Pengajuan Sekolah").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_superadmin_menu_hub").assertExists()

        // Filter via tile_superadmin_pending shows Pending school and hides Approved school
        composeTestRule.onNodeWithTag("tile_superadmin_pending").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("school_reg_item_9851").assertExists()
        composeTestRule.onNodeWithTag("school_reg_item_9852").assertDoesNotExist()

        // Filter via tile_superadmin_approved shows Approved school and hides Pending school
        composeTestRule.onNodeWithTag("tile_superadmin_approved").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("school_reg_item_9852").assertExists()
        composeTestRule.onNodeWithTag("school_reg_item_9851").assertDoesNotExist()

        // Quick tile navigation to Reports and Cloud DB preserves full tabs
        composeTestRule.onNodeWithTag("tile_superadmin_reports").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_share_report_wa").assertExists()

        composeTestRule.onNodeWithTag("tab_superadmin_pengajuan").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tile_superadmin_cloud_db").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_auto_sync_schema_pocketbase").assertExists()

        db.schoolRegistrationDao().delete(pendingReg)
        db.schoolRegistrationDao().delete(approvedReg)
        Unit
    }

    @Test
    fun `ClaimChristianStudentsSheet locks editing for non-Admin Wali and requires double confirmation for Admin`() {
        val studentA = ChildStudent(
            id = 9861L,
            name = "Kevin Immanuel",
            nis = "986101",
            gender = "L",
            age = 15,
            dormitory = "Asrama Putra",
            classGrade = "IX-A",
            schoolId = "SRT1-KDR",
            religion = StudentReligionHelper.RELIGION_KRISTEN
        )
        val studentB = ChildStudent(
            id = 9862L,
            name = "Bima Pratama",
            nis = "986201",
            gender = "L",
            age = 15,
            dormitory = "Asrama Putra",
            classGrade = "IX-A",
            schoolId = "SRT1-KDR",
            religion = ""
        )
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)

        // 1. Mode Wali Asuh (canManageClaim = false): terkunci / hanya baca
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                ClaimChristianStudentsSheet(
                    students = listOf(studentA, studentB),
                    canManageClaim = false,
                    onClaimStudents = { _, _ -> },
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("badge_claim_christian_locked").assertIsDisplayed()
        composeTestRule.onNodeWithTag("status_christian_readonly_9861").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_toggle_christian_9861").assertDoesNotExist()
        composeTestRule.onNodeWithTag("chk_claim_christian_9861").assertDoesNotExist()

        // 2. Mode Admin (canManageClaim = true): butuh konfirmasi ganda sebelum mengubah status
        var committedTargets = emptyList<ChildStudent>()
        var committedMark = false
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                ClaimChristianStudentsSheet(
                    students = listOf(studentA, studentB),
                    canManageClaim = true,
                    onClaimStudents = { targets, mark ->
                        committedTargets = targets
                        committedMark = mark
                    },
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("badge_claim_christian_locked").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_toggle_christian_9862").performClick()
        composeTestRule.waitForIdle()

        // Belum berubah sebelum konfirmasi ganda ditekan
        assertTrue(committedTargets.isEmpty())
        composeTestRule.onNodeWithTag("dialog_confirm_christian_claim").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_confirm_christian_action")
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, committedTargets.size)
        assertEquals(9862L, committedTargets.first().id)
        assertTrue(committedMark)
    }

    @Test
    fun `AdminScreen displays dedicated Christian claim menu card and opens ClaimChristianStudentsSheet`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        val db = AppDatabase.getDatabase(app)
        val vm = HealthViewModel(app)
        val schoolId = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

        val student = ChildStudent(
            id = 9871L,
            name = "Rachel Natalia",
            nis = "987101",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-A",
            schoolId = schoolId,
            religion = StudentReligionHelper.RELIGION_KRISTEN
        )
        db.childStudentDao().insert(student)

        var loops = 0
        while (loops < 30 && vm.students.value.none { it.id == 9871L }) {
            ShadowLooper.idleMainLooper()
            delay(30)
            loops++
        }

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                AdminScreen(
                    viewModel = vm,
                    onLogout = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("list_admin_content")
            .performScrollToNode(hasTestTag("card_admin_christian_claim"))
        composeTestRule.onNodeWithTag("card_admin_christian_claim").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_admin_open_claim_christian")
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("sheet_claim_christian_students").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_toggle_christian_9871").assertExists()

        db.childStudentDao().delete(student)
        Unit
    }

    @Test
    fun `StudentDetailDialog hides Mengaji and Sholat for Christian student and shows Ibadah Kristen history`() {
        val christianStudent = ChildStudent(
            id = 9881L,
            name = "Kevin Immanuel",
            nis = "988101",
            gender = "L",
            age = 16,
            dormitory = "Asrama Putra",
            classGrade = "XI-1",
            schoolId = "SRT1-KDR",
            religion = StudentReligionHelper.RELIGION_KRISTEN
        )
        val worshipLog = ChristianWorshipRecord(
            id = 98811L,
            schoolId = "SRT1-KDR",
            studentId = 9881L,
            studentName = christianStudent.name,
            studentNis = christianStudent.nis,
            activityType = ChristianWorshipRecord.TYPE_KE_GEREJA,
            activityDate = System.currentTimeMillis(),
            dateKey = ChristianWorshipHelper.todayDateKey(),
            title = "Ibadah Minggu GKJW",
            notes = "Mengikuti kebaktian Minggu pagi dengan tertib",
            waliName = "Bu Ana"
        )
        var inputClickedStudent: ChildStudent? = null
        var deletedRecord: ChristianWorshipRecord? = null
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                StudentDetailDialog(
                    student = christianStudent,
                    studentRecords = emptyList(),
                    christianWorshipRecords = listOf(worshipLog),
                    onInputChristianWorship = { inputClickedStudent = it },
                    onDeleteChristianWorshipRecord = { deletedRecord = it },
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tag_student_detail_christian_badge").assertExists()
        composeTestRule.onNodeWithTag("tab_student_detail_biodata").assertExists()
        composeTestRule.onNodeWithTag("tab_student_detail_kristen").assertExists()
        composeTestRule.onNodeWithTag("tab_student_detail_berobat").assertExists()
        composeTestRule.onNodeWithTag("tab_student_detail_puasa").assertExists()
        composeTestRule.onNodeWithTag("tab_student_detail_mengaji").assertDoesNotExist()
        composeTestRule.onNodeWithTag("tab_student_detail_sholat").assertDoesNotExist()

        composeTestRule.onNodeWithTag("tab_student_detail_kristen").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("student_christian_item_98811").assertExists()
        composeTestRule.onNodeWithText("Ibadah Minggu GKJW").assertExists()
        composeTestRule.onNodeWithTag("btn_student_detail_input_christian")
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(9881L, inputClickedStudent?.id)

        composeTestRule.onNodeWithTag("btn_delete_christian_98811")
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_confirm_delete_student_christian")
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(98811L, deletedRecord?.id)
    }

    @Test
    fun `StudentDetailDialog keeps Mengaji and Sholat for regular students and stays medical-only when restricted`() {
        val regularStudent = ChildStudent(
            id = 9882L,
            name = "Abimanyu Satria",
            nis = "988201",
            gender = "L",
            age = 16,
            dormitory = "Asrama Putra",
            classGrade = "X-4",
            schoolId = "SRT1-KDR",
            religion = ""
        )
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                StudentDetailDialog(
                    student = regularStudent,
                    studentRecords = emptyList(),
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tag_student_detail_christian_badge").assertDoesNotExist()
        composeTestRule.onNodeWithTag("tab_student_detail_kristen").assertDoesNotExist()
        composeTestRule.onNodeWithTag("tab_student_detail_mengaji").assertExists()
        composeTestRule.onNodeWithTag("tab_student_detail_sholat").assertExists()

        val christianStudent = regularStudent.copy(
            id = 9883L,
            name = "Mita Roro",
            gender = "P",
            religion = StudentReligionHelper.RELIGION_KRISTEN
        )
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                StudentDetailDialog(
                    student = christianStudent,
                    studentRecords = emptyList(),
                    allowedTabs = listOf(StudentDetailTab.BIODATA, StudentDetailTab.BEROBAT),
                    isPetugasKesehatan = true,
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tab_student_detail_biodata").assertExists()
        composeTestRule.onNodeWithTag("tab_student_detail_berobat").assertExists()
        composeTestRule.onNodeWithTag("tab_student_detail_kristen").assertDoesNotExist()
        composeTestRule.onNodeWithTag("tab_student_detail_mengaji").assertDoesNotExist()
        composeTestRule.onNodeWithTag("tab_student_detail_sholat").assertDoesNotExist()
        composeTestRule.onNodeWithTag("tab_student_detail_puasa").assertDoesNotExist()
    }
}
