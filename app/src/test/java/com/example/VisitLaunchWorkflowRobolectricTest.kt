package com.example

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
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
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.ParentAccessLink
import com.example.data.model.VisitLaunchStage
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionDraft
import com.example.data.remote.pocketbase.PbVisitSessionDto
import com.example.ui.HealthViewModel
import com.example.ui.LoginResult
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.ParentPortalScreen
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
class VisitLaunchWorkflowRobolectricTest {

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
    fun `VisitSession launchStage and PbVisitSessionDto serialize two-stage flags accurately`() {
        val draft = VisitSession(
            id = 101L,
            title = "Kunjungan Desember Sesi 1",
            monthYear = "2026-12",
            isActive = true,
            publishedToWali = false,
            publishedToParent = false
        )
        assertEquals(VisitLaunchStage.DRAFT, draft.launchStage())

        val waliPreview = draft.copy(publishedToWali = true, waliLaunchedAt = 1796000000000L)
        assertEquals(VisitLaunchStage.WALI_PREVIEW, waliPreview.launchStage())

        val parentLive = waliPreview.copy(
            publishedToParent = true,
            parentLaunchedAt = 1796005000000L,
            updatedNoticeAfterParentLaunch = true
        )
        assertEquals(VisitLaunchStage.PARENT_LIVE, parentLive.launchStage())

        val closed = parentLive.copy(isActive = false)
        assertEquals(VisitLaunchStage.CLOSED, closed.launchStage())

        val dto = PbVisitSessionDto.fromDomain(waliPreview)
        val restored = dto.toDomainModel()
        assertTrue(restored.publishedToWali)
        assertFalse(restored.publishedToParent)
        assertEquals(1796000000000L, restored.waliLaunchedAt)

        // Legacy remote doc with null launch flags defaults safely to visible (backward-compatible)
        val legacyDto = PbVisitSessionDto(
            localId = 102L,
            title = "Legacy Session",
            publishedToWali = null,
            publishedToParent = null
        )
        val legacyRestored = legacyDto.toDomainModel()
        assertTrue(legacyRestored.publishedToWali)
        assertTrue(legacyRestored.publishedToParent)
    }

    @Test
    fun `HealthViewModel enforces Draft to Wali Preview to Parent Live Edit Notice Pullback and Bulk Launch`(): Unit = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val db = AppDatabase.getDatabase(app)
        val date1 = dayMillis(2026, Calendar.DECEMBER, 6)
        val date2 = dayMillis(2026, Calendar.DECEMBER, 13)

        assertTrue(vm.executeLogin("superadmin", "Woyowoyo12@") is LoginResult.SuccessSuperAdmin)

        // 1. Admin creates single & batch sessions -> both start as DRAFT (not visible to Wali or Parent)
        var singleSession: VisitSession? = null
        vm.createVisitSession(
            title = "Kunjungan Des Single",
            monthYear = "2026-12",
            visitDates = listOf(date1),
            selectedDistricts = listOf("Wates")
        ) { singleSession = it }
        waitUntil { singleSession != null }
        assertNotNull(singleSession)
        assertFalse(singleSession!!.publishedToWali)
        assertFalse(singleSession!!.publishedToParent)
        assertEquals(VisitLaunchStage.DRAFT, singleSession!!.launchStage())

        var batchCreated: List<VisitSession> = emptyList()
        vm.createVisitSessionsBatch(
            drafts = listOf(
                VisitSessionDraft("Kunjungan Des Batch 1", "2026-12", date2, listOf("Pare"))
            )
        ) { list, _ -> batchCreated = list }
        waitUntil { batchCreated.isNotEmpty() }
        val batchSess = batchCreated.first()
        assertFalse(batchSess.publishedToWali)
        assertFalse(batchSess.publishedToParent)

        // Switch to WALI -> draft sessions must NOT be visible in waliVisibleVisitSessions or parentVisibleVisitSessions
        vm.switchRoleToWali()
        waitUntil { vm.waliVisibleVisitSessions.value.none { it.id == singleSession!!.id } }
        assertTrue(vm.waliVisibleVisitSessions.value.none { it.id == singleSession!!.id })
        assertTrue(vm.parentVisibleVisitSessions.value.none { it.id == singleSession!!.id })

        // 2. Stage 1 Launch -> Wali Asuh only
        vm.switchRoleToAdmin()
        var stage1Ok = false
        vm.setVisitSessionWaliLaunch(singleSession!!, true) { ok, _ -> stage1Ok = ok }
        waitUntil { stage1Ok && vm.visitSessions.value.firstOrNull { it.id == singleSession!!.id }?.publishedToWali == true }

        vm.switchRoleToWali()
        waitUntil { vm.waliVisibleVisitSessions.value.any { it.id == singleSession!!.id } }
        assertTrue("Wali Asuh must see stage 1 session", vm.waliVisibleVisitSessions.value.any { it.id == singleSession!!.id })
        assertTrue("Parents must NOT see stage 1 session yet", vm.parentVisibleVisitSessions.value.none { it.id == singleSession!!.id })

        // 3. Stage 2 Launch -> Live to Parents
        vm.switchRoleToAdmin()
        val afterWaliLaunch = vm.visitSessions.value.first { it.id == singleSession!!.id }
        var stage2Ok = false
        vm.setVisitSessionParentLaunch(afterWaliLaunch, true) { ok, _ -> stage2Ok = ok }
        waitUntil { stage2Ok && vm.parentVisibleVisitSessions.value.any { it.id == singleSession!!.id } }
        val liveSess = vm.visitSessions.value.first { it.id == singleSession!!.id }
        assertTrue(liveSess.publishedToParent)
        assertTrue(liveSess.publishedToWali)
        assertTrue(liveSess.parentLaunchedAt > 0L)

        // Cannot pull from Wali directly while still live to Parents
        var pullWaliBlocked = true
        vm.setVisitSessionWaliLaunch(liveSess, false) { ok, _ -> pullWaliBlocked = ok }
        assertFalse(pullWaliBlocked)

        // Editing schedule while live marks updatedNoticeAfterParentLaunch = true
        var editedSess: VisitSession? = null
        vm.updateVisitSession(
            session = liveSess,
            title = "Kunjungan Des Single",
            monthYear = "2026-12",
            visitDates = listOf(date1),
            selectedDistricts = listOf("Wates", "Ngancar"),
            visitTimeRange = "09.00 - 12.00 WIB"
        ) { editedSess = it }
        waitUntil { editedSess != null }
        assertTrue(editedSess!!.updatedNoticeAfterParentLaunch)

        // Pulling back from Parents hides from parentVisibleVisitSessions cleanly
        var pullParentOk = false
        vm.setVisitSessionParentLaunch(editedSess!!, false) { ok, _ -> pullParentOk = ok }
        waitUntil { pullParentOk && vm.parentVisibleVisitSessions.value.none { it.id == singleSession!!.id } }
        assertFalse(vm.visitSessions.value.first { it.id == singleSession!!.id }.publishedToParent)
        assertTrue(vm.visitSessions.value.first { it.id == singleSession!!.id }.publishedToWali)

        // 4. Bulk launch December sessions -> Wali first, then Parent
        var bulkWaliCount = 0
        vm.bulkLaunchMonthVisitSessions("2026-12", "WALI") { count, _ -> bulkWaliCount = count }
        waitUntil { vm.visitSessions.value.firstOrNull { it.id == batchSess.id }?.publishedToWali == true }
        assertEquals(1, bulkWaliCount)

        var bulkParentCount = 0
        vm.bulkLaunchMonthVisitSessions("2026-12", "PARENT") { count, _ -> bulkParentCount = count }
        waitUntil { vm.parentVisibleVisitSessions.value.count { it.monthYear == "2026-12" } >= 2 }
        assertEquals(2, bulkParentCount)

        vm.logout()
        db.visitSessionDao().deleteById(singleSession!!.id)
        db.visitSessionDao().deleteById(batchSess.id)
    }

    @Test
    fun `ParentPortal hides Draft and WaliPreview sessions and shows session once ParentLive`(): Unit = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SRT1-KDR"
        val visitDate = dayMillis(2026, Calendar.DECEMBER, 20)

        val student = ChildStudent(
            id = 99101L,
            name = "Nadia Putri",
            nis = "99101",
            nik = "3506991010001001",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-B",
            district = "Wates",
            assignedWaliUsername = "ustbudi",
            assignedWaliName = "Ustadz Budi",
            schoolId = schoolId
        )
        val session = VisitSession(
            id = 99201L,
            title = "Kunjungan Desember Uji",
            monthYear = "2026-12",
            sessionDatesCsv = visitDate.toString(),
            selectedDistrictsCsv = "Wates",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            publishedToWali = false,
            publishedToParent = false,
            schoolId = schoolId
        )
        val parentLink = ParentAccessLink(
            id = 99301L,
            username = "ortu_nadia_99101",
            password = "Pass#99101",
            studentId = 99101L,
            studentName = student.name,
            studentNis = student.nis,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            studentDistrict = "Wates",
            parentName = "Ibu Sari",
            assignedWaliUsername = "ustbudi",
            assignedWaliName = "Ustadz Budi",
            schoolId = schoolId
        )
        db.childStudentDao().insert(student)
        db.visitSessionDao().insert(session)
        db.parentAccessLinkDao().insert(parentLink)

        assertTrue(vm.executeLogin("ortu_nadia_99101", "Pass#99101") is LoginResult.SuccessParent)
        waitUntil { vm.activeParentAccessLink.value != null }

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                ParentPortalScreen(viewModel = vm, onLogout = {})
            }
        }
        composeTestRule.waitForIdle()

        // 1. Draft -> hidden in Parent Home & Visit Tab
        composeTestRule.onNodeWithTag("banner_parent_home_visit_schedule").assertDoesNotExist()
        composeTestRule.onNodeWithTag("card_parent_summary_visit").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("card_parent_visit_empty").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_parent_visit_session_99201").assertDoesNotExist()

        // 2. Stage 1 Wali Preview -> STILL hidden in Parent Portal!
        db.visitSessionDao().update(session.copy(publishedToWali = true, waliLaunchedAt = System.currentTimeMillis()))
        waitUntil { vm.visitSessions.value.firstOrNull { it.id == 99201L }?.publishedToWali == true }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("card_parent_visit_session_99201").assertDoesNotExist()

        // 3. Stage 2 Parent Live -> immediately visible in Parent Portal!
        db.visitSessionDao().update(
            session.copy(
                publishedToWali = true,
                publishedToParent = true,
                parentLaunchedAt = System.currentTimeMillis()
            )
        )
        waitUntil { vm.parentVisibleVisitSessions.value.any { it.id == 99201L } }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("parent_visit_matched_sessions_list")
            .performScrollToNode(hasTestTag("card_parent_visit_session_99201"))
        composeTestRule.onNodeWithTag("card_parent_visit_session_99201").assertIsDisplayed()

        vm.logout()
        db.visitSessionDao().deleteById(99201L)
        db.parentAccessLinkDao().delete(parentLink)
        db.childStudentDao().delete(student)
    }

    @Test
    fun `AdminScreen Kunjungan tab renders both Launch buttons and opens confirmation dialog before going Parent Live`(): Unit = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SRT1-KDR"
        val visitDate = dayMillis(2026, Calendar.DECEMBER, 20)

        val student = ChildStudent(
            id = 99102L,
            name = "Nadia Putri 2",
            nis = "99102",
            nik = "3506991020001002",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-B",
            district = "Wates",
            schoolId = schoolId
        )
        val draftSession = VisitSession(
            id = 99202L,
            title = "Kunjungan Desember Uji Admin",
            monthYear = "2026-12",
            sessionDatesCsv = visitDate.toString(),
            selectedDistrictsCsv = "Wates",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            publishedToWali = false,
            publishedToParent = false,
            waliLaunchedAt = 0L,
            parentLaunchedAt = 0L,
            schoolId = schoolId
        )
        db.childStudentDao().insert(student)
        db.visitSessionDao().insert(draftSession)

        assertTrue(vm.executeLogin("superadmin", "Woyowoyo12@") is LoginResult.SuccessSuperAdmin)

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
            .performScrollToNode(hasTestTag("row_admin_tabs"))
        composeTestRule.onNodeWithText("Kunjungan").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("list_admin_content")
            .performScrollToNode(hasTestTag("card_admin_visit_bulk_launch"))
        composeTestRule.onNodeWithTag("btn_admin_bulk_launch_wali").assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("btn_admin_bulk_launch_parent").assertHeightIsAtLeast(48.dp)

        composeTestRule.onNodeWithTag("list_admin_content")
            .performScrollToNode(hasTestTag("badge_visit_stage_99202"))
        composeTestRule.onNodeWithTag("badge_visit_stage_99202")
            .assertIsDisplayed()
            .assertTextContains("Draf", substring = true)

        // Stage 1: Click "Luncurkan ke Wali Asuh"
        composeTestRule.onNodeWithTag("list_admin_content")
            .performScrollToNode(hasTestTag("btn_launch_visit_wali_99202"))
        composeTestRule.onNodeWithTag("btn_launch_visit_wali_99202")
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        waitUntil { vm.visitSessions.value.firstOrNull { it.id == 99202L }?.publishedToWali == true }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("list_admin_content")
            .performScrollToNode(hasTestTag("badge_visit_stage_99202"))
        composeTestRule.onNodeWithTag("badge_visit_stage_99202")
            .assertTextContains("Preview Wali Asuh", substring = true)

        // Stage 2: Click "Luncurkan ke Wali Siswa" -> Confirmation Dialog appears
        composeTestRule.onNodeWithTag("list_admin_content")
            .performScrollToNode(hasTestTag("btn_launch_visit_parent_99202"))
        composeTestRule.onNodeWithTag("btn_launch_visit_parent_99202")
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dialog_confirm_launch_visit_parent").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("08.00 - 11.00 WIB", substring = true).onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_confirm_launch_visit_parent").performClick()
        waitUntil { vm.visitSessions.value.firstOrNull { it.id == 99202L }?.publishedToParent == true }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("list_admin_content")
            .performScrollToNode(hasTestTag("badge_visit_stage_99202"))
        composeTestRule.onNodeWithTag("badge_visit_stage_99202")
            .assertTextContains("Live ke Wali Siswa", substring = true)
        composeTestRule.onNodeWithTag("list_admin_content")
            .performScrollToNode(hasTestTag("btn_launch_visit_wali_99202"))
        composeTestRule.onNodeWithTag("btn_launch_visit_wali_99202").assertIsNotEnabled()

        vm.logout()
        db.visitSessionDao().deleteById(99202L)
        db.childStudentDao().delete(student)
    }
}
