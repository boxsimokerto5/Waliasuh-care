package com.example

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.example.ui.screens.ParentVisitContent
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
@Config(sdk = [34], shadows = [ShadowPdfDocument::class])
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
        val nowDay = VisitHelper.normalizeDayStart(System.currentTimeMillis())
        val visitDate1 = nowDay + 3L * 24L * 60L * 60L * 1000L
        val visitDate2 = nowDay + 10L * 24L * 60L * 60L * 1000L

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
        composeTestRule.onNodeWithTag("card_parent_visit_session_8801")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("sheet_parent_visit_detail_8801").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("08.00 - 11.00 WIB", substring = true).onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_parent_visit_session_8802").assertDoesNotExist()

        // Confirm attendance & verify QR pass renders inside the bottom sheet
        composeTestRule.onNodeWithTag("input_parent_visit_visitor_name_8801")
            .performScrollTo()
            .performTextReplacement("Ibu Aminah Wates")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_parent_visit_confirm_8801")
            .performScrollTo()
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

        // Barcode card & QR image appear inside the open bottom sheet!
        composeTestRule.onNodeWithTag("card_parent_visit_barcode_8801").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("img_parent_visit_qrcode_8801").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_parent_visit_sheet_close").performClick()
        composeTestRule.waitForIdle()

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

    @Test
    fun `ParentVisitContent groups Jadwal Mendatang and Riwayat Selesai into compact cards opening bottom sheet detail`() {
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        val now = System.currentTimeMillis()
        val futureDate = now + 7L * 24L * 60L * 60L * 1000L
        val pastDate = now - 10L * 24L * 60L * 60L * 1000L

        val student = ChildStudent(
            id = 5901L,
            name = "Ahmad Fauzi",
            nis = "5901",
            nik = "3506590100010001",
            gender = "L",
            age = 13,
            dormitory = "Asrama Putra",
            classGrade = "VII-A",
            district = "Puncu",
            schoolId = "SRT1-KDR"
        )
        val upcomingSession = VisitSession(
            id = 9101L,
            title = "Kunjungan Desember Sesi 1",
            monthYear = "2026-12",
            sessionDatesCsv = futureDate.toString(),
            selectedDistrictsCsv = "Badas|Gurah|Puncu",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            publishedToParent = true,
            schoolId = "SRT1-KDR"
        )
        val pastSession = VisitSession(
            id = 9102L,
            title = "Kunjungan Oktober Sesi 2",
            monthYear = "2026-10",
            sessionDatesCsv = pastDate.toString(),
            selectedDistrictsCsv = "Puncu",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = false,
            publishedToParent = true,
            schoolId = "SRT1-KDR"
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                ParentVisitContent(
                    student = student,
                    sessions = listOf(upcomingSession, pastSession),
                    confirmations = emptyList(),
                    visitRecords = emptyList(),
                    onSaveConfirmation = { _, _, _, _, _, _, _, _ -> }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Both section headers and compact cards are shown; inline form & bottom sheet are closed initially
        composeTestRule.onNodeWithTag("card_parent_visit_district").assertIsDisplayed()
        composeTestRule.onNodeWithTag("header_parent_visit_upcoming").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_parent_visit_session_9101")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("parent_visit_matched_sessions_list")
            .performScrollToNode(hasTestTag("header_parent_visit_completed"))
        composeTestRule.onNodeWithTag("header_parent_visit_completed").assertIsDisplayed()
        composeTestRule.onNodeWithTag("parent_visit_matched_sessions_list")
            .performScrollToNode(hasTestTag("card_parent_visit_session_9102"))
        composeTestRule.onNodeWithTag("card_parent_visit_session_9102")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("sheet_parent_visit_detail_9101").assertDoesNotExist()
        composeTestRule.onNodeWithTag("input_parent_visit_visitor_name_9101").assertDoesNotExist()

        // Tap upcoming card -> bottom sheet opens with full details and confirmation form
        composeTestRule.onNodeWithTag("parent_visit_matched_sessions_list")
            .performScrollToNode(hasTestTag("card_parent_visit_session_9101"))
        composeTestRule.onNodeWithTag("card_parent_visit_session_9101").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("sheet_parent_visit_detail_9101").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_parent_visit_visitor_name_9101").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_parent_visit_sheet_close").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("sheet_parent_visit_detail_9101").assertDoesNotExist()
    }

    @Test
    fun `ParentPortalHome hides passed visit banner next day and shows upcoming or today visit`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SRT1-KDR"
        val today = VisitHelper.normalizeDayStart(System.currentTimeMillis())
        val yesterday = today - 24L * 60L * 60L * 1000L
        val tomorrow = today + 24L * 60L * 60L * 1000L
        val nextWeek = today + 7L * 24L * 60L * 60L * 1000L

        val student = ChildStudent(
            id = 5911L,
            name = "Auliana Widiani",
            nis = "5911",
            nik = "3506591100010001",
            gender = "P",
            age = 15,
            dormitory = "Asrama Putri",
            classGrade = "X-1",
            district = "Puncu",
            schoolId = schoolId
        )
        val pastActiveSession = VisitSession(
            id = 9201L,
            title = "Kunjungan Lampau Sesi 1",
            monthYear = "2026-10",
            sessionDatesCsv = yesterday.toString(),
            selectedDistrictsCsv = "Puncu",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            publishedToParent = true,
            schoolId = schoolId
        )
        val upcomingSoonSession = VisitSession(
            id = 9202L,
            title = "Kunjungan Besok Sesi 2",
            monthYear = "2026-11",
            sessionDatesCsv = tomorrow.toString(),
            selectedDistrictsCsv = "Puncu",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            publishedToParent = true,
            schoolId = schoolId
        )
        val upcomingLaterSession = VisitSession(
            id = 9203L,
            title = "Kunjungan Pekan Depan Sesi 3",
            monthYear = "2026-12",
            sessionDatesCsv = nextWeek.toString(),
            selectedDistrictsCsv = "Puncu",
            visitTimeRange = "09.00 - 12.00 WIB",
            isActive = true,
            publishedToParent = true,
            schoolId = schoolId
        )
        val parentLink = ParentAccessLink(
            id = 5991L,
            username = "ortu_auliana_5911",
            password = "Pass#5911",
            studentId = 5911L,
            studentName = student.name,
            studentNis = student.nis,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            studentDistrict = "Puncu",
            parentName = "Ibu Widiani",
            assignedWaliUsername = "ust_eko",
            assignedWaliName = "Eko Wahyudi",
            schoolId = schoolId
        )

        db.childStudentDao().insert(student)
        db.visitSessionDao().insert(pastActiveSession)
        db.parentAccessLinkDao().insert(parentLink)

        assertTrue(vm.executeLogin("ortu_auliana_5911", "Pass#5911") is LoginResult.SuccessParent)
        var loops = 0
        while (loops < 30 && vm.activeParentAccessLink.value == null) {
            ShadowLooper.idleMainLooper()
            delay(40)
            loops++
        }

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                ParentPortalScreen(viewModel = vm, onLogout = {})
            }
        }
        composeTestRule.waitForIdle()

        // 1. Only yesterday's session exists -> Home banner must NOT appear
        composeTestRule.onNodeWithTag("banner_parent_home_visit_schedule").assertDoesNotExist()

        // 2. Add two upcoming sessions -> Home banner appears showing the nearest upcoming one (tomorrow)
        db.visitSessionDao().insert(upcomingLaterSession)
        db.visitSessionDao().insert(upcomingSoonSession)
        var sessLoops = 0
        while (sessLoops < 40 && vm.parentVisibleVisitSessions.value.none { it.id == 9202L }) {
            ShadowLooper.idleMainLooper()
            delay(40)
            sessLoops++
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("banner_parent_home_visit_schedule")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Kunjungan Besok Sesi 2", substring = true)

        composeTestRule.onNodeWithTag("parent_home_branding_logos")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("parent_home_scenic_footer")
            .performScrollTo()
            .assertIsDisplayed()

        vm.logout()
        db.visitSessionDao().deleteById(9201L)
        db.visitSessionDao().deleteById(9202L)
        db.visitSessionDao().deleteById(9203L)
        db.parentAccessLinkDao().delete(parentLink)
        db.childStudentDao().delete(student)
    }

    @Test
    fun `Per-session PDF ticket unlocks only after HADIR confirmation and renders complete 1-page boarding pass`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        val futureDate = System.currentTimeMillis() + 5L * 24L * 60L * 60L * 1000L

        val student = ChildStudent(
            id = 5931L,
            name = "Nadia Putri",
            nis = "593101",
            nik = "3506593100010001",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-B",
            district = "Puncu",
            assignedWaliName = "Ustadzah Rina",
            schoolId = "SRT1-KDR"
        )
        val session = VisitSession(
            id = 9301L,
            title = "Kunjungan Desember Sesi 1",
            monthYear = "2026-12",
            sessionDatesCsv = futureDate.toString(),
            selectedDistrictsCsv = "Puncu|Gurah",
            description = "Membawa perlengkapan sekolah secukupnya.",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            publishedToParent = true,
            schoolId = "SRT1-KDR"
        )

        val absentConf = VisitConfirmation(
            id = 9391L,
            sessionId = session.id,
            sessionTitle = session.title,
            visitDate = futureDate,
            studentId = student.id,
            studentName = student.name,
            studentNis = student.nis,
            studentNik = student.nik,
            studentDistrict = "Puncu",
            visitorName = "Ibu Dewi",
            visitorRelation = "Ibu",
            attendanceStatus = VisitConfirmation.STATUS_BERHALANGAN,
            notes = "Sedang berada di luar kota",
            barcodeToken = "",
            schoolId = "SRT1-KDR"
        )

        val hadirConf = VisitConfirmation(
            id = 9392L,
            sessionId = session.id,
            sessionTitle = session.title,
            visitDate = futureDate,
            studentId = student.id,
            studentName = student.name,
            studentNis = student.nis,
            studentNik = student.nik,
            studentDistrict = "Puncu",
            visitorName = "Ibu Dewi",
            visitorRelation = "Ibu",
            attendanceStatus = VisitConfirmation.STATUS_HADIR,
            estimatedArrivalTime = "09:15 WIB",
            notes = "Mohon izin membawakan alat tulis.",
            barcodeToken = VisitHelper.buildBarcodeToken(session.id, student.id),
            schoolId = "SRT1-KDR"
        )

        // 1. When absent / not attending: ticket buttons & generateSingleVisitSessionTicketPdf stay locked/null
        assertNull(
            ParentPortalPdfHelper.generateSingleVisitSessionTicketPdf(
                context = app,
                student = student,
                session = session,
                confirmation = absentConf
            )
        )

        var activeConfirmations by androidx.compose.runtime.mutableStateOf(listOf(absentConf))
        var printedReadyTitle = ""

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                ParentVisitContent(
                    student = student,
                    sessions = listOf(session),
                    confirmations = activeConfirmations,
                    visitRecords = emptyList(),
                    waliName = "Ustadzah Rina",
                    schoolProfile = SchoolProfile(
                        schoolName = "Sekolah Rakyat Terpadu 1 Kediri",
                        shortName = "SRT 1",
                        tenantCode = "SRT1-KDR"
                    ),
                    onSaveConfirmation = { _, _, _, _, _, _, _, _ -> },
                    onPdfReady = { data -> printedReadyTitle = data.title }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Absent confirmation -> card quick print button does NOT exist; bottom sheet also hides QR & print button
        composeTestRule.onNodeWithTag("btn_parent_visit_card_print_ticket_9301").assertDoesNotExist()
        composeTestRule.onNodeWithTag("card_parent_visit_session_9301").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("card_parent_visit_absent_9301").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_parent_visit_print_ticket_9301").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_parent_visit_sheet_close").performClick()
        composeTestRule.waitForIdle()

        // 2. Switch to HADIR confirmation -> both card print button and sheet print button appear (>= 48dp)
        composeTestRule.runOnUiThread {
            activeConfirmations = listOf(hadirConf)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_parent_visit_card_print_ticket_9301")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)

        composeTestRule.onNodeWithTag("card_parent_visit_session_9301").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("card_parent_visit_barcode_9301").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_parent_visit_print_ticket_9301")
            .performScrollTo()
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals("E-Tiket Kunjungan Sesi Siap", printedReadyTitle)

        // 3. Render 1-page boarding-pass ticket PDF & verify complete session + barcode text
        var pageCount = 0
        val (pages, ticketTexts) = capturePdfTexts { startNew ->
            ParentPortalPdfHelper.renderSingleVisitSessionTicketPage(
                context = app,
                student = student,
                session = session,
                confirmation = hadirConf,
                waliName = "Ustadzah Rina",
                schoolProfile = SchoolProfile(
                    schoolName = "Sekolah Rakyat Terpadu 1 Kediri",
                    shortName = "SRT 1",
                    tenantCode = "SRT1-KDR"
                ),
                startNewCanvas = { startNew(++pageCount) }
            )
        }
        assertEquals(1, pages)
        assertTrue(ticketTexts.contains("Kunjungan Desember Sesi 1"))
        assertTrue(ticketTexts.contains("08.00 - 11.00 WIB"))
        assertTrue(ticketTexts.contains("09:15 WIB"))
        assertTrue(ticketTexts.contains("Nadia Putri"))
        assertTrue(ticketTexts.contains("Ibu — Ibu Dewi"))
        assertTrue(ticketTexts.contains(hadirConf.barcodeToken))
        assertTrue(ticketTexts.contains("PINDAI DI GERBANG ASRAMA"))
        assertTrue(ticketTexts.contains("KODE VERIFIKASI GERBANG"))
    }
}

