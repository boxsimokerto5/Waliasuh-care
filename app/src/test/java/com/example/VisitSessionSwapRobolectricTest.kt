package com.example

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.ParentAccessLink
import com.example.data.model.SchoolProfile
import com.example.data.model.TandemWaliMember
import com.example.data.model.VisitConfirmation
import com.example.data.model.VisitHelper
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionSwap
import com.example.data.model.WaliAccount
import com.example.data.remote.pocketbase.PbVisitSessionSwapDto
import com.example.ui.HealthViewModel
import com.example.ui.LoginResult
import com.example.ui.dialogs.DaftarHadirKunjunganSheet
import com.example.ui.screens.ParentPortalScreen
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
class VisitSessionSwapRobolectricTest {

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

    private suspend fun waitUntil(maxTicks: Int = 150, condition: () -> Boolean) {
        var ticks = 0
        while (ticks < maxTicks && !condition()) {
            ShadowLooper.idleMainLooper()
            delay(35)
            ticks++
        }
    }

    @Test
    fun `VisitHelper authorization and PbVisitSessionSwapDto round-trip work accurately`() {
        val tandemJson = Json.encodeToString(
            listOf(TandemWaliMember(username = "ustzah_siti", name = "Ustadzah Siti"))
        )
        val student = ChildStudent(
            id = 5101L,
            name = "Ahmad Fauzi",
            nis = "5101",
            nik = "3506510100010001",
            gender = "L",
            age = 13,
            dormitory = "Asrama Putra",
            classGrade = "VII-A",
            district = "Wates",
            assignedWaliUsername = "ust_budi",
            assignedWaliName = "Ustadz Budi",
            tandemWalisJson = tandemJson
        )

        val unclaimedStudent = student.copy(
            id = 5102L,
            assignedWaliUsername = "",
            assignedWaliName = "",
            tandemWalisJson = ""
        )

        assertTrue(VisitHelper.canWaliSwapStudent(student, false, "ust_budi", "Ustadz Budi"))
        assertTrue(VisitHelper.canWaliSwapStudent(student, false, "ustzah_siti", "Ustadzah Siti"))
        assertFalse(VisitHelper.canWaliSwapStudent(student, true, "admin1", "Admin"))
        assertFalse(VisitHelper.canWaliSwapStudent(student, false, "other", "Other", isAnakSuperAdmin = true))
        assertFalse(VisitHelper.canWaliSwapStudent(student, false, "ust_lain", "Ustadz Lain"))
        assertFalse(VisitHelper.canWaliSwapStudent(unclaimedStudent, false, "ust_budi", "Ustadz Budi"))

        val swap = VisitSessionSwap(
            id = 701L,
            schoolId = "SRT1-KDR",
            monthYear = "2026-12",
            fromSessionId = 801L,
            fromSessionTitle = "Sesi 1 Tgl 18",
            toSessionId = 802L,
            toSessionTitle = "Sesi 2 Tgl 25",
            studentId = 5101L,
            studentName = "Ahmad Fauzi",
            studentNik = "3506510100010001",
            studentNis = "5101",
            studentClass = "VII-A",
            studentDorm = "Asrama Putra",
            studentDistrict = "Wates",
            reason = "Orang tua bisanya ikut sesi tgl 25",
            hadPriorCheckIn = true,
            priorCheckInAt = 1797584400000L,
            swappedByUsername = "ust_budi",
            swappedByName = "Ustadz Budi"
        )
        val dto = PbVisitSessionSwapDto.fromDomain(swap)
        val restored = dto.toDomainModel()
        assertEquals(swap.id, restored.id)
        assertEquals(swap.fromSessionId, restored.fromSessionId)
        assertEquals(swap.toSessionId, restored.toSessionId)
        assertEquals(swap.reason, restored.reason)
        assertTrue(restored.hadPriorCheckIn)
        assertEquals(1797584400000L, restored.priorCheckInAt)
    }

    @Test
    fun `swapStudentVisitSession allows moving already checked-in student chains cleanly migrates confirmation and reverts`(): Unit = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SRT1-KDR"
        val date1 = dayMillis(2026, Calendar.DECEMBER, 18)
        val date2 = dayMillis(2026, Calendar.DECEMBER, 25)
        val date3 = dayMillis(2026, Calendar.DECEMBER, 27)

        val waliAccount = WaliAccount(
            id = 5501L,
            username = "ust_swap_test",
            password = "Pass#Swap1",
            name = "Ustadz Swap",
            dormitory = "Asrama Putra",
            schoolId = schoolId
        )
        val studentWates = ChildStudent(
            id = 5511L,
            name = "Budi Wates",
            nis = "5511",
            nik = "3506551100010001",
            gender = "L",
            age = 13,
            dormitory = "Asrama Putra",
            classGrade = "VII-A",
            district = "Wates",
            assignedWaliUsername = "ust_swap_test",
            assignedWaliName = "Ustadz Swap",
            schoolId = schoolId
        )
        val studentPare = ChildStudent(
            id = 5512L,
            name = "Citra Pare",
            nis = "5512",
            nik = "3506551200010002",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-B",
            district = "Pare",
            assignedWaliUsername = "ust_swap_test",
            assignedWaliName = "Ustadz Swap",
            schoolId = schoolId
        )
        val session1 = VisitSession(
            id = 5601L,
            title = "Kunjungan Sesi 1 (18 Des)",
            monthYear = "2026-12",
            sessionDatesCsv = date1.toString(),
            selectedDistrictsCsv = "Wates",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            publishedToWali = true,
            publishedToParent = true,
            schoolId = schoolId
        )
        val session2 = VisitSession(
            id = 5602L,
            title = "Kunjungan Sesi 2 (25 Des)",
            monthYear = "2026-12",
            sessionDatesCsv = date2.toString(),
            selectedDistrictsCsv = "Pare",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            publishedToWali = true,
            publishedToParent = true,
            schoolId = schoolId
        )
        val session3 = VisitSession(
            id = 5603L,
            title = "Kunjungan Sesi 3 (27 Des)",
            monthYear = "2026-12",
            sessionDatesCsv = date3.toString(),
            selectedDistrictsCsv = "Ngancar",
            visitTimeRange = "09.00 - 12.00 WIB",
            isActive = true,
            publishedToWali = true,
            publishedToParent = true,
            schoolId = schoolId
        )

        db.waliAccountDao().insert(waliAccount)
        db.childStudentDao().insert(studentWates)
        db.childStudentDao().insert(studentPare)
        db.visitSessionDao().insert(session1)
        db.visitSessionDao().insert(session2)
        db.visitSessionDao().insert(session3)

        assertTrue(vm.executeLogin("ust_swap_test", "Pass#Swap1") is LoginResult.SuccessWali)
        waitUntil { vm.visitSessions.value.any { it.id == 5601L } }

        // 1. Parent confirms HADIR & gets accidentally checked in on Session 1 (tgl 18)
        var confSaved = false
        vm.saveVisitConfirmation(
            session = session1,
            student = studentWates,
            visitDate = date1,
            visitorName = "Pak Joko",
            visitorRelation = "Ayah",
            attendanceStatus = VisitConfirmation.STATUS_HADIR,
            estimatedArrivalTime = "09:00 WIB",
            notes = "Siap datang"
        ) { _, _ -> confSaved = true }
        waitUntil { confSaved && vm.visitConfirmations.value.any { it.sessionId == 5601L && it.studentId == 5511L } }

        var checkedIn = false
        vm.recordStudentVisitCheckIn(
            session = session1,
            student = studentWates,
            visitDate = date1,
            visitorRelation = "Ayah",
            visitorName = "Pak Joko"
        ) { ok, _, _ -> checkedIn = ok }
        waitUntil { checkedIn && vm.visitRecords.value.any { it.sessionId == 5601L && it.studentId == 5511L } }

        // 2. Swap already-checked-in student from Session 1 -> Session 2 (must succeed!)
        var swapMsg = ""
        vm.swapStudentVisitSession(
            student = studentWates,
            fromSession = session1,
            targetSession = session2,
            reason = "Bisanya ikut sesi tanggal 25",
            clearPriorCheckIn = true
        ) { _, msg, _ -> swapMsg = msg }
        waitUntil { vm.visitSessionSwaps.value.any { it.studentId == 5511L && it.toSessionId == 5602L } }

        assertTrue("Expected non-empty feedback", swapMsg.isNotBlank())
        val activeSwap = vm.visitSessionSwaps.value.first { it.studentId == 5511L }
        assertEquals(5601L, activeSwap.fromSessionId)
        assertEquals(5602L, activeSwap.toSessionId)
        assertTrue("Audit flag must capture prior check-in", activeSwap.hadPriorCheckIn)
        assertTrue(activeSwap.priorCheckInAt > 0L)

        // Prior check-in in Session 1 must be cleared cleanly
        waitUntil { vm.visitRecords.value.none { it.sessionId == 5601L && it.studentId == 5511L } }
        assertTrue(vm.visitRecords.value.none { it.sessionId == 5601L && it.studentId == 5511L })

        // HADIR confirmation must migrate to Session 2 with updated session QR token
        val migratedConf = vm.visitConfirmations.value.firstOrNull { it.studentId == 5511L && it.sessionId == 5602L }
        assertNotNull("Confirmation should migrate to target Session 2", migratedConf)
        assertTrue(migratedConf!!.barcodeToken.contains("-S5602-"))
        assertTrue(vm.visitConfirmations.value.none { it.studentId == 5511L && it.sessionId == 5601L })

        // Effective schedules reflect swap
        val sess1Effective = VisitHelper.effectiveScheduledStudentsForSession(
            session1, listOf(studentWates, studentPare), vm.visitSessionSwaps.value, listOf(session1, session2, session3)
        )
        val sess2Effective = VisitHelper.effectiveScheduledStudentsForSession(
            session2, listOf(studentWates, studentPare), vm.visitSessionSwaps.value, listOf(session1, session2, session3)
        )
        assertTrue(sess1Effective.none { it.id == 5511L })
        assertTrue(sess2Effective.any { it.id == 5511L })

        // Checking in on Session 2 must NOT flag isOutOfScheduleDistrict
        var sess2CheckInOk = false
        var sess2Record: VisitRecord? = null
        vm.recordStudentVisitCheckIn(
            session = session2,
            student = studentWates,
            visitDate = date2,
            visitorRelation = "Ayah",
            visitorName = "Pak Joko"
        ) { ok, _, rec ->
            sess2CheckInOk = ok
            sess2Record = rec
        }
        waitUntil { sess2CheckInOk && sess2Record != null }
        assertFalse("Swapped-in student must not be flagged out-of-schedule", sess2Record!!.isOutOfScheduleDistrict)

        // 3. Chained swap Session 2 -> Session 3 keeps original fromSessionId == 5601L
        vm.swapStudentVisitSession(
            student = studentWates,
            fromSession = session2,
            targetSession = session3,
            reason = "Pindah lagi ke sesi 3",
            clearPriorCheckIn = true
        ) { _, _, _ -> }
        waitUntil { vm.visitSessionSwaps.value.firstOrNull { it.studentId == 5511L }?.toSessionId == 5603L }
        val chained = vm.visitSessionSwaps.value.first { it.studentId == 5511L }
        assertEquals(5601L, chained.fromSessionId)
        assertEquals(5603L, chained.toSessionId)

        // 4. Moving back to origin Session 1 deletes the swap cleanly and moves confirmation back
        vm.swapStudentVisitSession(
            student = studentWates,
            fromSession = session3,
            targetSession = session1,
            reason = "",
            clearPriorCheckIn = true
        ) { _, _, _ -> }
        waitUntil { vm.visitSessionSwaps.value.none { it.studentId == 5511L } }
        assertTrue(vm.visitConfirmations.value.any { it.studentId == 5511L && it.sessionId == 5601L })

        vm.logout()
        db.visitConfirmationDao().deleteBySchoolId(schoolId)
        db.visitRecordDao().deleteBySchoolId(schoolId)
        db.visitSessionSwapDao().deleteBySchoolId(schoolId)
        db.visitSessionDao().deleteById(5601L)
        db.visitSessionDao().deleteById(5602L)
        db.visitSessionDao().deleteById(5603L)
        db.childStudentDao().delete(studentWates)
        db.childStudentDao().delete(studentPare)
        db.waliAccountDao().delete(waliAccount)
    }

    @Test
    fun `DaftarHadirKunjunganSheet supports swapping checked-in child with prior check-in warning`(): Unit = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SRT1-KDR"
        val date1 = dayMillis(2026, Calendar.DECEMBER, 18)
        val date2 = dayMillis(2026, Calendar.DECEMBER, 25)

        val student = ChildStudent(
            id = 5701L,
            name = "Zahra Aulia",
            nis = "5701",
            nik = "3506570100010001",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-A",
            district = "Wates",
            assignedWaliUsername = "ust_ui_swap",
            assignedWaliName = "Ustadz UI",
            schoolId = schoolId
        )
        val session1 = VisitSession(
            id = 5801L,
            title = "Kunjungan Sesi 1 (18 Des)",
            monthYear = "2026-12",
            sessionDatesCsv = date1.toString(),
            selectedDistrictsCsv = "Wates",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            publishedToWali = true,
            publishedToParent = true,
            schoolId = schoolId
        )
        val session2 = VisitSession(
            id = 5802L,
            title = "Kunjungan Sesi 2 (25 Des)",
            monthYear = "2026-12",
            sessionDatesCsv = date2.toString(),
            selectedDistrictsCsv = "Pare",
            visitTimeRange = "09.00 - 12.00 WIB",
            isActive = true,
            publishedToWali = true,
            publishedToParent = true,
            schoolId = schoolId
        )
        val priorCheckIn = VisitRecord(
            id = 5901L,
            sessionId = 5801L,
            visitDate = date1,
            studentId = 5701L,
            studentName = student.name,
            studentNik = student.nik,
            studentNis = student.nis,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            studentDistrict = "Wates",
            visitorRelation = "Ibu",
            visitorName = "Ibu Zahra",
            schoolId = schoolId
        )
        val waliAccount = WaliAccount(
            id = 5961L,
            username = "ust_ui_swap",
            password = "Pass#SwapUi",
            name = "Ustadz UI",
            dormitory = "Asrama Putri",
            schoolId = schoolId
        )

        db.childStudentDao().insert(student)
        db.visitSessionDao().insert(session1)
        db.visitSessionDao().insert(session2)
        db.visitRecordDao().insert(priorCheckIn)
        db.waliAccountDao().insert(waliAccount)

        assertTrue(vm.executeLogin("ust_ui_swap", "Pass#SwapUi") is LoginResult.SuccessWali)
        waitUntil { vm.visitRecords.value.any { it.id == 5901L } }

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                DaftarHadirKunjunganSheet(
                    sessions = listOf(session1, session2),
                    students = listOf(student),
                    visitRecords = listOf(priorCheckIn),
                    visitSessionSwaps = emptyList(),
                    schoolProfile = SchoolProfile(schoolName = "SRT 1 Kediri", tenantCode = schoolId),
                    waliName = "Ustadz UI",
                    currentUsername = "ust_ui_swap",
                    onRecordVisitCheckIn = { _, _, _, _, _, _, _ -> },
                    onDeleteVisitRecord = {},
                    onSwapStudentSession = { st, fromSess, toSess, reason, clearPrior ->
                        vm.swapStudentVisitSession(st, fromSess, toSess, reason, clearPrior)
                    },
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Swap button must still be visible and >= 48dp even though Zahra already checked in
        composeTestRule.onNodeWithTag("btn_swap_visit_session_5701")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()

        // Dialog shows prior check-in warning & checkbox + target session selector
        composeTestRule.onNodeWithTag("dialog_visit_session_swap").assertIsDisplayed()
        composeTestRule.onNodeWithTag("banner_swap_prior_checkin_warning").assertIsDisplayed()
        composeTestRule.onNodeWithTag("checkbox_clear_prior_checkin").assertIsDisplayed()
        composeTestRule.onNodeWithTag("option_swap_target_5802").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("input_swap_reason").performScrollTo().performTextReplacement("Ortu hadir tgl 25")
        composeTestRule.onNodeWithTag("btn_confirm_visit_swap")
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        waitUntil { vm.visitSessionSwaps.value.any { it.studentId == 5701L && it.toSessionId == 5802L } }

        val savedSwap = vm.visitSessionSwaps.value.first { it.studentId == 5701L }
        assertEquals(5801L, savedSwap.fromSessionId)
        assertEquals(5802L, savedSwap.toSessionId)
        assertTrue(savedSwap.hadPriorCheckIn)
        assertEquals("Ortu hadir tgl 25", savedSwap.reason)

        vm.logout()
        db.visitSessionSwapDao().deleteBySchoolId(schoolId)
        db.visitRecordDao().deleteBySchoolId(schoolId)
        db.visitSessionDao().deleteById(5801L)
        db.visitSessionDao().deleteById(5802L)
        db.childStudentDao().delete(student)
        db.waliAccountDao().delete(waliAccount)
    }

    @Test
    fun `ParentPortalScreen reflects swapped visit session and displays swap notice banner`(): Unit = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SRT1-KDR"
        val date1 = dayMillis(2026, Calendar.DECEMBER, 18)
        val date2 = dayMillis(2026, Calendar.DECEMBER, 25)

        val student = ChildStudent(
            id = 5702L,
            name = "Nisa Maharani",
            nis = "5702",
            nik = "3506570200010002",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-B",
            district = "Wates",
            assignedWaliUsername = "ust_ui_swap2",
            assignedWaliName = "Ustadz UI 2",
            schoolId = schoolId
        )
        val session1 = VisitSession(
            id = 5811L,
            title = "Kunjungan Sesi 1 (18 Des)",
            monthYear = "2026-12",
            sessionDatesCsv = date1.toString(),
            selectedDistrictsCsv = "Wates",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            publishedToWali = true,
            publishedToParent = true,
            schoolId = schoolId
        )
        val session2 = VisitSession(
            id = 5812L,
            title = "Kunjungan Sesi 2 (25 Des)",
            monthYear = "2026-12",
            sessionDatesCsv = date2.toString(),
            selectedDistrictsCsv = "Pare",
            visitTimeRange = "09.00 - 12.00 WIB",
            isActive = true,
            publishedToWali = true,
            publishedToParent = true,
            schoolId = schoolId
        )
        val swap = VisitSessionSwap(
            id = 5899L,
            schoolId = schoolId,
            monthYear = "2026-12",
            fromSessionId = 5811L,
            fromSessionTitle = session1.title,
            toSessionId = 5812L,
            toSessionTitle = session2.title,
            studentId = 5702L,
            studentName = student.name,
            studentNik = student.nik,
            studentNis = student.nis,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            studentDistrict = "Wates",
            reason = "Ortu hadir tgl 25",
            swappedByUsername = "ust_ui_swap2",
            swappedByName = "Ustadz UI 2"
        )
        val parentLink = ParentAccessLink(
            id = 5952L,
            username = "ortu_nisa_5702",
            password = "Pass#5702",
            studentId = 5702L,
            studentName = student.name,
            studentNis = student.nis,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            studentDistrict = "Wates",
            parentName = "Ibu Nisa",
            assignedWaliUsername = "ust_ui_swap2",
            assignedWaliName = "Ustadz UI 2",
            schoolId = schoolId
        )

        db.childStudentDao().insert(student)
        db.visitSessionDao().insert(session1)
        db.visitSessionDao().insert(session2)
        db.visitSessionSwapDao().insert(swap)
        db.parentAccessLinkDao().insert(parentLink)

        assertTrue(vm.executeLogin("ortu_nisa_5702", "Pass#5702") is LoginResult.SuccessParent)
        waitUntil {
            vm.activeParentAccessLink.value != null &&
                vm.visitSessionSwaps.value.any { it.studentId == 5702L && it.toSessionId == 5812L }
        }

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                ParentPortalScreen(viewModel = vm, onLogout = {})
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("banner_parent_home_visit_schedule").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Jadwal Dipindah", substring = true).onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_parent_summary_visit").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_parent_visit_session_5811").assertDoesNotExist()
        composeTestRule.onNodeWithTag("parent_visit_matched_sessions_list")
            .performScrollToNode(hasTestTag("card_parent_visit_session_5812"))
        composeTestRule.onNodeWithTag("card_parent_visit_session_5812").assertIsDisplayed()
        composeTestRule.onNodeWithTag("parent_visit_matched_sessions_list")
            .performScrollToNode(hasTestTag("banner_parent_visit_swapped_5812"))
        composeTestRule.onNodeWithTag("banner_parent_visit_swapped_5812")
            .assertIsDisplayed()
            .assertTextContains("Ortu hadir tgl 25", substring = true)

        vm.logout()
        db.visitSessionSwapDao().deleteBySchoolId(schoolId)
        db.visitSessionDao().deleteById(5811L)
        db.visitSessionDao().deleteById(5812L)
        db.parentAccessLinkDao().delete(parentLink)
        db.childStudentDao().delete(student)
    }

    @Test
    fun `Option B shows swap button only for own foster child while keeping district rosters intact`() {
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        val date1 = dayMillis(2026, Calendar.DECEMBER, 18)
        val session = VisitSession(
            id = 5851L,
            title = "Kunjungan Wates Sesi 1",
            monthYear = "2026-12",
            sessionDatesCsv = date1.toString(),
            selectedDistrictsCsv = "Wates",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            publishedToWali = true,
            publishedToParent = true,
            schoolId = "SRT1-KDR"
        )
        val myKid = ChildStudent(
            id = 5861L,
            name = "Binaanku Satu",
            nis = "5861",
            gender = "L",
            age = 13,
            classGrade = "VII-A",
            dormitory = "Asrama Putra",
            district = "Wates",
            assignedWaliUsername = "ust_budi",
            assignedWaliName = "Ustadz Budi",
            schoolId = "SRT1-KDR"
        )
        val otherWaliKid = ChildStudent(
            id = 5862L,
            name = "Binaan Wali Lain",
            nis = "5862",
            gender = "L",
            age = 13,
            classGrade = "VII-B",
            dormitory = "Asrama Putra",
            district = "Wates",
            assignedWaliUsername = "ust_andi",
            assignedWaliName = "Ustadz Andi",
            schoolId = "SRT1-KDR"
        )
        val unclaimedKid = ChildStudent(
            id = 5863L,
            name = "Siswa Belum Diklaim",
            nis = "5863",
            gender = "P",
            age = 14,
            classGrade = "VIII-A",
            dormitory = "Asrama Putri",
            district = "Wates",
            schoolId = "SRT1-KDR"
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                com.example.ui.dialogs.JadwalKunjunganSheet(
                    sessions = listOf(session),
                    students = listOf(myKid, otherWaliKid, unclaimedKid),
                    visitRecords = emptyList(),
                    isAdmin = true,
                    currentUsername = "ust_budi",
                    waliName = "Ustadz Budi",
                    isAnakSuperAdmin = true,
                    onOpenDaftarHadir = {},
                    onOpenLaporan = {},
                    onSwapStudentSession = { _, _, _, _, _ -> },
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Semua siswa kecamatan Wates terlihat, namun tombol Tukar HANYA muncul untuk Binaanku Satu
        composeTestRule.onNodeWithTag("list_jadwal_kunjungan")
            .performScrollToNode(hasTestTag("btn_jadwal_swap_5861"))
        composeTestRule.onNodeWithTag("btn_jadwal_swap_5861")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("badge_jadwal_my_foster_5861").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_jadwal_swap_5862").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_jadwal_swap_5863").assertDoesNotExist()

        // Filter Binaan Saya menyembunyikan anak non-binaan dengan tetap menyediakan filter Semua Siswa
        composeTestRule.onNodeWithTag("list_jadwal_kunjungan")
            .performScrollToNode(hasTestTag("chip_jadwal_scope_foster"))
        composeTestRule.onNodeWithTag("chip_jadwal_scope_foster")
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_jadwal_swap_5861").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Binaan Wali Lain", substring = false).fetchSemanticsNodes().let {
            assertEquals(0, it.size)
        }
        composeTestRule.onAllNodesWithText("Siswa Belum Diklaim", substring = false).fetchSemanticsNodes().let {
            assertEquals(0, it.size)
        }

        composeTestRule.onNodeWithTag("chip_jadwal_scope_all")
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("list_jadwal_kunjungan")
            .performScrollToNode(androidx.compose.ui.test.hasText("Siswa Belum Diklaim"))
    }
}
