package com.example

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.ParentAccessLink
import com.example.data.model.SchoolProfile
import com.example.data.model.VisitConfirmation
import com.example.data.model.VisitHelper
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionDraft
import com.example.ui.HealthViewModel
import com.example.ui.LoginResult
import com.example.ui.dialogs.CreateVisitSessionDialog
import com.example.ui.dialogs.DaftarHadirKunjunganSheet
import com.example.ui.screens.ParentPortalScreen
import com.example.util.ParentPortalPdfHelper
import com.example.util.PdfReportGenerator
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VisitMultiSessionConfirmationRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun dayMillis(year: Int, monthZeroBased: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun capturePdfTexts(block: (startNew: (Int) -> Canvas) -> Unit): Pair<Int, String> {
        val texts = mutableListOf<String>()
        val bitmaps = mutableListOf<Bitmap>()
        try {
            block {
                val bmp = Bitmap.createBitmap(595, 842, Bitmap.Config.ARGB_8888)
                bitmaps.add(bmp)
                object : Canvas(bmp) {
                    override fun drawText(text: String, x: Float, y: Float, paint: Paint) {
                        texts.add(text)
                        super.drawText(text, x, y, paint)
                    }
                }
            }
            return bitmaps.size to texts.joinToString(" | ")
        } finally {
            bitmaps.forEach { it.recycle() }
        }
    }

    @Test
    fun `VisitHelper and batch creation enforce distinct dates exclusive districts and QR payload parsing`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val db = AppDatabase.getDatabase(app)
        val date1 = dayMillis(2026, Calendar.NOVEMBER, 1)
        val date2 = dayMillis(2026, Calendar.NOVEMBER, 8)
        val date3 = dayMillis(2026, Calendar.NOVEMBER, 15)

        val loginRes = vm.executeLogin("superadmin", "Woyowoyo12@")
        assertTrue(loginRes is LoginResult.SuccessSuperAdmin)

        // 1. Payload roundtrip check
        val token = VisitHelper.buildBarcodeToken(910L, 4501L)
        val payload = VisitHelper.buildVisitBarcodePayload(
            sessionId = 910L,
            studentId = 4501L,
            token = token,
            studentNik = "3506123456780001"
        )
        val parsed = VisitHelper.parseVisitBarcodePayload(payload)
        assertNotNull(parsed)
        assertEquals(910L, parsed?.sessionId)
        assertEquals(4501L, parsed?.studentId)
        assertEquals(token, parsed?.token)
        assertEquals("3506123456780001", parsed?.studentNik)
        assertNull(VisitHelper.parseVisitBarcodePayload("INVALID|1|2"))

        // 2. Helper lock map check
        val usedByOthers = VisitHelper.usedDistrictsByOtherDrafts(
            draftDistricts = listOf(setOf("Wates", "Plosoklaten"), setOf("Pare"), setOf("Ngancar")),
            excludeIndex = 1
        )
        assertEquals(0, usedByOthers["wates"])
        assertEquals(0, usedByOthers["plosoklaten"])
        assertEquals(2, usedByOthers["ngancar"])
        assertNull(usedByOthers["pare"])

        // 3. Reject duplicate dates in batch (Point 1)
        var dupCreated: List<VisitSession> = listOf(VisitSession(id = 1L, title = "dummy"))
        var dupMsg = ""
        vm.createVisitSessionsBatch(
            drafts = listOf(
                VisitSessionDraft("Kunjungan Nov Batch - Sesi 1", "2026-11", date1, listOf("Wates")),
                VisitSessionDraft("Kunjungan Nov Batch - Sesi 2", "2026-11", date1, listOf("Pare"))
            )
        ) { created, msg ->
            dupCreated = created
            dupMsg = msg
        }
        repeat(10) {
            ShadowLooper.idleMainLooper()
            delay(20)
        }
        assertTrue(dupCreated.isEmpty())
        assertTrue(dupMsg.contains("berbeda", ignoreCase = true) || dupMsg.contains("different", ignoreCase = true))

        // 4. Reject overlapping district across sessions in batch
        var overlapCreated: List<VisitSession> = listOf(VisitSession(id = 1L, title = "dummy"))
        vm.createVisitSessionsBatch(
            drafts = listOf(
                VisitSessionDraft("Kunjungan Nov Batch - Sesi 1", "2026-11", date1, listOf("Wates")),
                VisitSessionDraft("Kunjungan Nov Batch - Sesi 2", "2026-11", date2, listOf("wates"))
            )
        ) { created, _ ->
            overlapCreated = created
        }
        repeat(10) {
            ShadowLooper.idleMainLooper()
            delay(20)
        }
        assertTrue(overlapCreated.isEmpty())

        // 5. Valid 3-session batch succeeds with distinct visit time ranges
        var validCreated: List<VisitSession> = emptyList()
        vm.createVisitSessionsBatch(
            drafts = listOf(
                VisitSessionDraft("Kunjungan Nov Batch - Sesi 1", "2026-11", date1, listOf("Wates"), visitTimeRange = "08.00 - 11.00 WIB"),
                VisitSessionDraft("Kunjungan Nov Batch - Sesi 2", "2026-11", date2, listOf("Pare"), visitTimeRange = "09.00 - 12.00 WIB"),
                VisitSessionDraft("Kunjungan Nov Batch - Sesi 3", "2026-11", date3, listOf("Ngancar"), visitTimeRange = "13.00 - 16.00 WIB")
            )
        ) { created, _ ->
            validCreated = created
        }
        var loops = 0
        while (loops < 30 && validCreated.isEmpty()) {
            ShadowLooper.idleMainLooper()
            delay(30)
            loops++
        }
        assertEquals(3, validCreated.size)
        assertEquals("08.00 - 11.00 WIB", validCreated[0].displayTimeRange())
        assertEquals("09.00 - 12.00 WIB", validCreated[1].displayTimeRange())
        assertEquals("13.00 - 16.00 WIB", validCreated[2].displayTimeRange())

        // Clean up created sessions
        vm.logout()
        validCreated.forEach { db.visitSessionDao().deleteById(it.id) }
    }

    @Test
    fun `CreateVisitSessionDialog locks district selected in session 1 so session 2 cannot pick it`() {
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        val students = listOf(
            ChildStudent(id = 1L, name = "Alya", nis = "101", gender = "P", age = 13, dormitory = "Asrama Putri", classGrade = "VII-A", district = "Wates", schoolId = "SRT1-KDR"),
            ChildStudent(id = 2L, name = "Bima", nis = "102", gender = "L", age = 13, dormitory = "Asrama Putra", classGrade = "VII-B", district = "Pare", schoolId = "SRT1-KDR"),
            ChildStudent(id = 3L, name = "Citra", nis = "103", gender = "P", age = 14, dormitory = "Asrama Putri", classGrade = "VIII-A", district = "Ngancar", schoolId = "SRT1-KDR")
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                CreateVisitSessionDialog(
                    students = students,
                    existingSessions = emptyList(),
                    onDismiss = {},
                    onConfirmBatch = {},
                    onConfirm = { _, _, _, _, _, _ -> }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_close_create_visit_sheet").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("btn_save_visit_session").assertIsDisplayed().assertHeightIsAtLeast(48.dp)

        // Choose 3 sessions (pinned header above scroll)
        composeTestRule.onNodeWithTag("chip_visit_session_count_3").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // Default time range is 08.00 - 11.00 WIB; selecting preset updates session 1 time range
        composeTestRule.onNodeWithTag("input_visit_time_range").performScrollTo().assertTextContains("08.00 - 11.00 WIB")
        composeTestRule.onNodeWithTag("chip_visit_time_preset_2").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("input_visit_time_range").performScrollTo().assertTextContains("09.00 - 12.00 WIB")

        // In Sesi 1, pick Wates
        composeTestRule.onNodeWithTag("chip_admin_district_Wates").performScrollTo().assertIsEnabled().performClick()
        composeTestRule.waitForIdle()

        // Switch to Sesi 2 tab -> Wates must be disabled/locked, while Pare remains enabled!
        composeTestRule.onNodeWithTag("tab_visit_draft_1").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("chip_admin_district_Wates").performScrollTo().assertIsNotEnabled()
        composeTestRule.onNodeWithTag("chip_admin_district_Pare").performScrollTo().assertIsEnabled()
    }

    @Test
    fun `ParentPortal auto enables Visit tab shows child session confirms attendance generates QR and Wali sees badge and per-district PDF`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SRT1-KDR"
        val visitDate1 = dayMillis(2026, Calendar.NOVEMBER, 15)
        val visitDate2 = dayMillis(2026, Calendar.NOVEMBER, 22)

        val studentWates = ChildStudent(
            id = 7701L,
            name = "Rizky Pratama",
            nis = "770101",
            nik = "3506770100010001",
            gender = "L",
            age = 13,
            dormitory = "Asrama Putra",
            classGrade = "VII-B",
            district = "Wates",
            assignedWaliUsername = "ustbudi",
            assignedWaliName = "Ustadz Budi",
            schoolId = schoolId
        )
        val studentPare = ChildStudent(
            id = 7702L,
            name = "Siti Aisyah",
            nis = "770201",
            nik = "3506770200010002",
            gender = "P",
            age = 13,
            dormitory = "Asrama Putri",
            classGrade = "VII-A",
            district = "Pare",
            assignedWaliUsername = "ustbudi",
            assignedWaliName = "Ustadz Budi",
            schoolId = schoolId
        )
        db.childStudentDao().insert(studentWates)
        db.childStudentDao().insert(studentPare)

        val sessionWates = VisitSession(
            id = 8801L,
            title = "Kunjungan November - Sesi 1",
            monthYear = "2026-11",
            sessionDatesCsv = visitDate1.toString(),
            selectedDistrictsCsv = "Wates|Ngancar",
            description = "Kunjungan resmi wali murid",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            schoolId = schoolId
        )
        val sessionPare = VisitSession(
            id = 8802L,
            title = "Kunjungan November - Sesi 2",
            monthYear = "2026-11",
            sessionDatesCsv = visitDate2.toString(),
            selectedDistrictsCsv = "Pare",
            description = "Kunjungan sesi 2",
            isActive = true,
            schoolId = schoolId
        )
        db.visitSessionDao().insert(sessionWates)
        db.visitSessionDao().insert(sessionPare)

        // All other optional permissions false -> Visit MUST still be auto-enabled (Point 2)
        val parentLink = ParentAccessLink(
            id = 7791L,
            username = "ortu_rizky_7701",
            password = "Pass#7701",
            studentId = 7701L,
            studentName = studentWates.name,
            studentNis = studentWates.nis,
            studentClass = studentWates.classGrade,
            studentDorm = studentWates.dormitory,
            studentDistrict = "Wates",
            parentName = "Ibu Aminah",
            parentPhone = "081234567890",
            assignedWaliUsername = "ustbudi",
            assignedWaliName = "Ustadz Budi",
            schoolId = schoolId,
            canViewProfile = false,
            canViewMengaji = false,
            canViewMedical = false,
            canViewFasting = false,
            canViewHaid = false,
            canViewTabungan = false,
            canViewSholat = false
        )
        db.parentAccessLinkDao().insert(parentLink)

        val loginRes = vm.executeLogin("ortu_rizky_7701", "Pass#7701")
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

        // Home auto-displays Visit invitation banner with session hours and auto-enabled Visit tile
        composeTestRule.onNodeWithTag("banner_parent_home_visit_schedule").performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("08.00 - 11.00 WIB", substring = true).onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_parent_summary_visit").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // Only sessionWates matches child's district "Wates" (sessionPare must NOT be shown)
        composeTestRule.onNodeWithTag("card_parent_visit_district").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Wates", substring = true).onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithTag("parent_visit_matched_sessions_list")
            .performScrollToNode(hasTestTag("card_parent_visit_session_8801"))
        composeTestRule.onNodeWithTag("card_parent_visit_session_8801").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("08.00 - 11.00 WIB", substring = true).onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_parent_visit_session_8802").assertDoesNotExist()

        // Confirm attendance & verify QR pass renders
        composeTestRule.onNodeWithTag("parent_visit_matched_sessions_list")
            .performScrollToNode(hasTestTag("input_parent_visit_visitor_name_8801"))
        composeTestRule.onNodeWithTag("input_parent_visit_visitor_name_8801")
            .performTextReplacement("Ibu Aminah Wates")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("parent_visit_matched_sessions_list")
            .performScrollToNode(hasTestTag("btn_parent_visit_confirm_8801"))
        composeTestRule.onNodeWithTag("parent_visit_matched_sessions_list")
            .performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_parent_visit_confirm_8801")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()

        var confLoops = 0
        while (confLoops < 60 && vm.visitConfirmations.value.none { it.sessionId == 8801L && it.studentId == 7701L }) {
            ShadowLooper.idleMainLooper()
            delay(50)
            confLoops++
        }
        assertTrue(
            "Confirmation should appear in ViewModel StateFlow after clicking confirm",
            vm.visitConfirmations.value.any { it.sessionId == 8801L && it.studentId == 7701L }
        )
        composeTestRule.waitForIdle()

        // Barcode card & QR image appear!
        composeTestRule.onNodeWithTag("parent_visit_matched_sessions_list")
            .performScrollToNode(hasTestTag("card_parent_visit_barcode_8801"))
        composeTestRule.onNodeWithTag("img_parent_visit_qrcode_8801").assertIsDisplayed()

        // Check PDF download button & close dialog
        composeTestRule.onNodeWithTag("parent_visit_matched_sessions_list")
            .performScrollToNode(hasTestTag("btn_parent_visit_pdf"))
        composeTestRule.onNodeWithTag("btn_parent_visit_pdf")
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dialog_parent_pdf_ready").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_parent_pdf_close").performClick()
        composeTestRule.waitForIdle()

        // Verify confirmation saved in DB & rendered in PDF helper
        val savedConfs = db.visitConfirmationDao().getConfirmationsBySessionList(8801L)
        assertEquals(1, savedConfs.size)
        val savedConf = savedConfs.first()
        assertEquals("Ibu Aminah Wates", savedConf.visitorName)

        val (passPages, passTexts) = capturePdfTexts { startNew ->
            ParentPortalPdfHelper.renderStudentVisitPassPages(
                context = app,
                student = studentWates,
                matchedSessions = listOf(sessionWates),
                confirmations = savedConfs,
                visitRecords = emptyList(),
                waliName = "Ustadz Budi",
                schoolProfile = SchoolProfile(schoolName = "SRT 1 Kediri", shortName = "SRT 1", tenantCode = schoolId),
                startNewCanvas = startNew
            )
        }
        assertTrue(passPages >= 1)
        assertTrue(passTexts.contains("Rizky Pratama"))
        assertTrue(passTexts.contains("08.00 - 11.00 WIB"))
        assertTrue(passTexts.contains("Ibu Aminah Wates"))
        assertTrue(passTexts.contains(savedConf.barcodeToken))

        // Verify Wali per-district manual roster PDF renders Wates, visiting hours, and blank "Wali Anak Asuh" column
        var rosterPageNo = 0
        val (rosterPages, rosterTexts) = capturePdfTexts { startNew ->
            PdfReportGenerator.renderVisitManualRosterPages(
                context = app,
                session = sessionWates,
                scheduledStudents = listOf(studentWates),
                existingRecords = emptyList(),
                targetDateMillis = visitDate1,
                waliName = "Ustadz Budi",
                schoolProfile = SchoolProfile(schoolName = "SRT 1 Kediri", shortName = "SRT 1", tenantCode = schoolId),
                selectedDistrictFilter = "Wates",
                visitConfirmations = savedConfs,
                startNewCanvas = { startNew(++rosterPageNo) }
            )
        }
        assertTrue(rosterPages >= 1)
        assertTrue(rosterTexts.contains("Rizky Pratama"))
        assertTrue(rosterTexts.contains("Wates", ignoreCase = true))
        assertTrue(rosterTexts.contains("08.00 - 11.00 WIB"))
        assertTrue(rosterTexts.contains("Wali Anak Asuh"))
        assertFalse(rosterTexts.contains("____ : ____"))
        assertFalse(rosterTexts.contains("Konfirm"))

        vm.logout()
        db.visitConfirmationDao().deleteBySessionId(8801L)
        db.visitSessionDao().delete(sessionWates)
        db.visitSessionDao().delete(sessionPare)
        db.parentAccessLinkDao().delete(parentLink)
        db.childStudentDao().delete(studentWates)
        db.childStudentDao().delete(studentPare)
    }

    @Test
    fun `DaftarHadirKunjunganSheet displays Point 3 confirmation badge on Manual and Scan tabs`() {
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        val visitDate = dayMillis(2026, Calendar.NOVEMBER, 15)
        val student = ChildStudent(
            id = 5501L,
            name = "Ahmad Fauzi",
            nis = "550101",
            gender = "L",
            age = 14,
            classGrade = "VIII-C",
            dormitory = "Asrama Putra",
            district = "Wates",
            schoolId = "SRT1-KDR"
        )
        val session = VisitSession(
            id = 6601L,
            title = "Kunjungan Sesi 1",
            monthYear = "2026-11",
            sessionDatesCsv = visitDate.toString(),
            selectedDistrictsCsv = "Wates",
            isActive = true,
            schoolId = "SRT1-KDR"
        )
        val confirmation = VisitConfirmation(
            id = 6691L,
            sessionId = 6601L,
            sessionTitle = session.title,
            visitDate = visitDate,
            studentId = 5501L,
            studentName = student.name,
            studentNis = student.nis,
            studentDistrict = "Wates",
            visitorName = "Bapak Ahmad",
            visitorRelation = "Ayah",
            attendanceStatus = VisitConfirmation.STATUS_HADIR,
            estimatedArrivalTime = "09:30",
            barcodeToken = VisitHelper.buildBarcodeToken(6601L, 5501L),
            schoolId = "SRT1-KDR"
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                DaftarHadirKunjunganSheet(
                    sessions = listOf(session),
                    students = listOf(student),
                    visitRecords = emptyList(),
                    visitConfirmations = listOf(confirmation),
                    schoolProfile = SchoolProfile(schoolName = "SRT 1 Kediri", shortName = "SRT 1"),
                    waliName = "Ustadz Budi",
                    initialTab = 1,
                    onRecordVisitCheckIn = { _, _, _, _, _, _, _ -> },
                    onDeleteVisitRecord = {},
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Manual tab shows confirmation badge and district filter chip
        composeTestRule.onNodeWithTag("chip_visit_dist_filter_Wates").assertIsDisplayed()
        composeTestRule.onNodeWithTag("list_visit_manual_tab")
            .performScrollToNode(hasTestTag("badge_visit_confirmed_5501"))
        composeTestRule.onNodeWithTag("badge_visit_confirmed_5501").assertIsDisplayed()

        // Switch to Scan tab -> scan search card also shows confirmation badge
        composeTestRule.onNodeWithTag("tab_visit_scan").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("list_visit_barcode_tab")
            .performScrollToNode(hasTestTag("badge_visit_scan_confirmed_5501"))
        composeTestRule.onNodeWithTag("badge_visit_scan_confirmed_5501").assertIsDisplayed()
    }
}
