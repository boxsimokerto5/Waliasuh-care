package com.example

import android.app.Application
import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.IncomingTandemRequestBundle
import com.example.data.model.KonselingHelper
import com.example.data.model.KonselingRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.SchoolProfile
import com.example.data.model.TabunganHelper
import com.example.data.model.TandemJoinRequest
import com.example.data.model.TandemWaliHelper
import com.example.data.model.TandemWaliMember
import com.example.data.model.WaliAccount
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliScheduleHelper
import com.example.data.remote.pocketbase.PbKonselingRecordDto
import com.example.data.remote.pocketbase.PbParentAccessLinkDto
import com.example.data.remote.pocketbase.PbStudentDto
import com.example.data.remote.pocketbase.PocketBaseSchemaProvider
import com.example.ui.HealthViewModel
import com.example.ui.LoginResult
import com.example.ui.dialogs.InputKonselingDialog
import com.example.ui.dialogs.KartuAksesOrangTuaSheet
import com.example.ui.dialogs.KonselingStudentDetailSheet
import com.example.ui.dialogs.PilihAnakAsuhSheet
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
class TandemWaliRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private suspend fun waitUntil(maxAttempts: Int = 40, condition: () -> Boolean) {
        for (i in 1..maxAttempts) {
            ShadowLooper.idleMainLooper()
            if (condition()) return
            delay(25)
        }
        ShadowLooper.idleMainLooper()
    }

    private suspend fun clearSchoolData(db: AppDatabase, schoolId: String) {
        db.tabunganTransactionDao().deleteBySchoolId(schoolId)
        db.tabunganLogDao().deleteBySchoolId(schoolId)
        db.konselingRecordDao().deleteBySchoolId(schoolId)
        db.konselingLogDao().deleteBySchoolId(schoolId)
        db.deletionLogDao().deleteBySchoolId(schoolId)
        db.parentAccessLinkDao().deleteBySchoolId(schoolId)
        db.waliScheduleEntryDao().deleteBySchoolId(schoolId)
        db.waliShiftCodeDao().deleteBySchoolId(schoolId)
        db.childStudentDao().deleteBySchoolId(schoolId)
        db.waliAccountDao().deleteBySchoolId(schoolId)
    }

    @Test
    fun `verify TandemWaliHelper enforces max 3 walis approves bundles promotes on release renames and resolves live piket`() {
        val w1 = WaliAccount(id = 1L, name = "Ustadz Budi", username = "budi", password = "p", schoolId = "SCH-T1")
        val w2 = WaliAccount(id = 2L, name = "Ustadz Eko", username = "eko", password = "p", schoolId = "SCH-T1")
        val w3 = WaliAccount(id = 3L, name = "Ustadz Candra", username = "candra", password = "p", schoolId = "SCH-T1")
        val w4 = WaliAccount(id = 4L, name = "Ustadz Dedi", username = "dedi", password = "p", schoolId = "SCH-T1")

        var st = ChildStudent(
            id = 101L,
            name = "Ahmad Fauzan",
            nis = "202601",
            gender = "L",
            age = 13,
            dormitory = "Asrama Putra",
            classGrade = "VIII-A",
            assignedWaliUsername = w1.username,
            assignedWaliName = w1.name,
            schoolId = "SCH-T1"
        )

        // 1. W2 and W3 can request tandem join, W1 (already primary) cannot
        assertFalse(TandemWaliHelper.canRequestTandemJoin(st, w1.username, w1.name))
        assertTrue(TandemWaliHelper.canRequestTandemJoin(st, w2.username, w2.name))
        st = TandemWaliHelper.withAddedRequest(st, w2.username, w2.name, 1000L)
        st = TandemWaliHelper.withAddedRequest(st, w3.username, w3.name, 2000L)
        // Duplicate request from W2 is ignored
        st = TandemWaliHelper.withAddedRequest(st, w2.username, w2.name, 3000L)
        assertEquals(2, TandemWaliHelper.pendingRequests(st).size)

        // 2. Grouping bundles for Primary Wali (budi)
        val bundles = TandemWaliHelper.groupIncomingRequestsForPrimary(listOf(st), w1.username, w1.name)
        assertEquals(2, bundles.size)
        assertEquals("candra", bundles[0].requesterUsername)
        assertEquals("eko", bundles[1].requesterUsername)

        // 3. Approve W2 and W3 -> total 3 walis (1 Primary + 2 Tandems), 0 remaining slots
        st = TandemWaliHelper.withApprovedRequest(st, w2.username, w2.name, 4000L)
        st = TandemWaliHelper.withApprovedRequest(st, w3.username, w3.name, 5000L)
        assertEquals(3, TandemWaliHelper.totalAssignedCount(st))
        assertEquals(0, TandemWaliHelper.remainingTandemSlots(st))
        assertTrue(TandemWaliHelper.isAssignedWali(st, w2.username, w2.name))
        assertTrue(TandemWaliHelper.isAssignedWali(st, w3.username, w3.name))

        // 4. 4th Wali blocked from joining or being invited
        assertFalse(TandemWaliHelper.canRequestTandemJoin(st, w4.username, w4.name))
        val unchangedAfterInvite = TandemWaliHelper.withInvitedTandemMember(st, w4.username, w4.name)
        assertEquals(2, TandemWaliHelper.tandemMembers(unchangedAfterInvite).size)

        // 5. Promote W3 -> W3 becomes primary, W1 & W2 become tandem members (max 2)
        val promoted = TandemWaliHelper.withPromotedTandemToPrimary(st, w3.username, w3.name, 6000L)
        assertTrue(TandemWaliHelper.isPrimaryWali(promoted, w3.username, w3.name))
        assertTrue(TandemWaliHelper.isTandemMember(promoted, w1.username, w1.name))
        assertTrue(TandemWaliHelper.isTandemMember(promoted, w2.username, w2.name))
        assertEquals(2, TandemWaliHelper.tandemMembers(promoted).size)

        // 6. When Primary releases -> 1st Tandem becomes new Primary; Tandem release removes only self
        val afterPrimaryRelease = TandemWaliHelper.withReleasedActor(st, w1.username, w1.name, isAdmin = false)
        assertTrue(TandemWaliHelper.isPrimaryWali(afterPrimaryRelease, w2.username, w2.name))
        assertEquals(1, TandemWaliHelper.tandemMembers(afterPrimaryRelease).size)
        assertTrue(TandemWaliHelper.isTandemMember(afterPrimaryRelease, w3.username, w3.name))

        val afterTandemRelease = TandemWaliHelper.withReleasedActor(afterPrimaryRelease, w3.username, w3.name, isAdmin = false)
        assertTrue(TandemWaliHelper.isPrimaryWali(afterTandemRelease, w2.username, w2.name))
        assertEquals(0, TandemWaliHelper.tandemMembers(afterTandemRelease).size)

        // 7. Admin release clears all primary, tandems, and pending requests
        val afterAdminRelease = TandemWaliHelper.withReleasedActor(st, "admin", "Admin", isAdmin = true)
        assertFalse(TandemWaliHelper.hasPrimaryWali(afterAdminRelease))
        assertEquals(0, TandemWaliHelper.tandemMembers(afterAdminRelease).size)

        // 8. Rename propagates to ChildStudent & ParentAccessLink
        val link = ParentAccessLink(
            id = 501L,
            studentId = st.id,
            studentName = st.name,
            username = "ortu_ahmad",
            password = "pw",
            assignedWaliUsername = w1.username,
            assignedWaliName = w1.name,
            tandemWalisJson = st.tandemWalisJson,
            schoolId = "SCH-T1"
        )
        val renamedW2 = w2.copy(name = "Ustadz Eko Santoso")
        val renamedStudent = TandemWaliHelper.applyWaliRename(st, "Ustadz Eko", renamedW2)
        val renamedLink = TandemWaliHelper.applyWaliRename(link, "Ustadz Eko", renamedW2, 7000L)
        assertNotNull(renamedStudent)
        assertNotNull(renamedLink)
        assertEquals("Ustadz Eko Santoso", TandemWaliHelper.tandemMembers(renamedStudent!!).first { it.username == "eko" }.name)
        assertEquals("Ustadz Eko Santoso", TandemWaliHelper.parseMembers(renamedLink!!.tandemWalisJson).first { it.username == "eko" }.name)

        // 9. Resolve live schedule piket badges at specific hour (10:00 -> P1 active, S1A inactive)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.NOVEMBER)
            set(Calendar.DAY_OF_MONTH, 20)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
        }
        val monthKey = WaliScheduleHelper.currentMonthKey(cal.timeInMillis)
        val day = WaliScheduleHelper.currentDayOfMonth(cal.timeInMillis)
        val schedEntries = listOf(
            WaliScheduleEntry(
                schoolId = "SCH-T1",
                monthKey = monthKey,
                waliUsername = w1.username,
                waliName = w1.name,
                dayCodesJson = WaliScheduleHelper.serializeDayCodes(mapOf(day to "LP"))
            ),
            WaliScheduleEntry(
                schoolId = "SCH-T1",
                monthKey = monthKey,
                waliUsername = w2.username,
                waliName = w2.name,
                dayCodesJson = WaliScheduleHelper.serializeDayCodes(mapOf(day to "P1"))
            ),
            WaliScheduleEntry(
                schoolId = "SCH-T1",
                monthKey = monthKey,
                waliUsername = w3.username,
                waliName = w3.name,
                dayCodesJson = WaliScheduleHelper.serializeDayCodes(mapOf(day to "S1A"))
            )
        )
        val badges = TandemWaliHelper.resolveWaliBadges(
            student = st,
            link = link,
            waliAccounts = listOf(w1, w2, w3),
            scheduleEntries = schedEntries,
            shiftCodes = emptyList(),
            schoolId = "SCH-T1",
            nowMillis = cal.timeInMillis
        )
        assertEquals(3, badges.size)
        assertTrue(badges[0].isPrimary)
        assertFalse(badges[0].isOnDutyNow) // LP
        assertFalse(badges[1].isPrimary)
        assertTrue(badges[1].isOnDutyNow) // P1 at 10:00
        assertTrue(badges[1].shiftLabel.contains("07:00"))
        assertFalse(badges[2].isPrimary)
        assertFalse(badges[2].isOnDutyNow) // S1A starts 15:00
    }

    @Test
    fun `verify HealthViewModel 3-wali 20-child dormancy workflow parent-deletion guard tabungan isolation and konseling tandem sharing`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        app.getSharedPreferences("waliasuh_school_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SCH-TANDEM-E2E"
        clearSchoolData(db, schoolId)

        val w1 = WaliAccount(id = 7101L, name = "Wali Utama A", username = "wali_a", password = "Pw#1", dormitory = "Asrama Putra", role = WaliAccount.ROLE_WALI, schoolId = schoolId)
        val w2 = WaliAccount(id = 7102L, name = "Wali Tandem B", username = "wali_b", password = "Pw#2", dormitory = "Asrama Putra", role = WaliAccount.ROLE_WALI, schoolId = schoolId)
        val w3 = WaliAccount(id = 7103L, name = "Wali Tandem C", username = "wali_c", password = "Pw#3", dormitory = "Asrama Putra", role = WaliAccount.ROLE_WALI, schoolId = schoolId)
        val w4 = WaliAccount(id = 7104L, name = "Wali Luar D", username = "wali_d", password = "Pw#4", dormitory = "Asrama Putra", role = WaliAccount.ROLE_WALI, schoolId = schoolId)
        db.waliAccountDao().insertAll(listOf(w1, w2, w3, w4))

        val dormStudents = (1..20).map { idx ->
            ChildStudent(
                id = 7200L + idx,
                name = "Santri Kamar $idx",
                nis = "9900$idx",
                gender = "L",
                age = 13,
                dormitory = "Asrama Putra",
                classGrade = "VII-A",
                schoolId = schoolId
            )
        }
        db.childStudentDao().insertAll(dormStudents)

        val vm = HealthViewModel(app)
        vm.updateSchoolProfile(SchoolProfile(tenantCode = schoolId, schoolName = "Sekolah Tandem E2E", shortName = "TandemE2E"))

        // 1. Wali A logs in & claims all 20 dormitory students -> becomes Primary
        assertTrue(vm.executeLogin("wali_a", "Pw#1", rememberMe = false) is LoginResult.SuccessWali)
        waitUntil { vm.students.value.size == 20 }

        var claimCount = 0
        vm.claimStudentsForCurrentWali(vm.students.value) { claimed, _ -> claimCount = claimed }
        waitUntil { claimCount == 20 && vm.parentAccessLinks.value.size == 20 }
        assertEquals(20, claimCount)

        // 2. Wali B logs in & requests bulk tandem for all 20 students
        vm.logout()
        assertTrue(vm.executeLogin("wali_b", "Pw#2", rememberMe = false) is LoginResult.SuccessWali)
        waitUntil { vm.students.value.size == 20 }
        var reqBCount = 0
        vm.requestTandemJoinForStudents(vm.students.value) { req, _, _ -> reqBCount = req }
        waitUntil { reqBCount == 20 }

        // 3. Wali C logs in & requests bulk tandem for all 20 students
        vm.logout()
        assertTrue(vm.executeLogin("wali_c", "Pw#3", rememberMe = false) is LoginResult.SuccessWali)
        waitUntil { vm.students.value.size == 20 }
        var reqCCount = 0
        vm.requestTandemJoinForStudents(vm.students.value) { req, _, _ -> reqCCount = req }
        waitUntil { reqCCount == 20 }

        // 4. Wali A sees 2 incoming bundles (Wali B & Wali C) -> approves all 20 for both
        vm.logout()
        assertTrue(vm.executeLogin("wali_a", "Pw#1", rememberMe = false) is LoginResult.SuccessWali)
        waitUntil { vm.incomingTandemRequestBundles.value.size == 2 }
        val bundleB = vm.incomingTandemRequestBundles.value.first { it.requesterUsername == "wali_b" }
        val bundleC = vm.incomingTandemRequestBundles.value.first { it.requesterUsername == "wali_c" }
        assertEquals(20, bundleB.students.size)
        assertEquals(20, bundleC.students.size)

        var approvedB = 0
        vm.approveTandemRequestBundle("wali_b", w2.name, bundleB.students.map { it.id }.toSet(), bundleB.students.map { it.id }.toSet()) { appCnt, _, _ ->
            approvedB = appCnt
        }
        waitUntil { approvedB == 20 }

        var approvedC = 0
        vm.approveTandemRequestBundle("wali_c", w3.name, bundleC.students.map { it.id }.toSet(), bundleC.students.map { it.id }.toSet()) { appCnt, _, _ ->
            approvedC = appCnt
        }
        waitUntil { approvedC == 20 && vm.incomingTandemRequestBundles.value.isEmpty() }

        val firstLinkAfter = vm.parentAccessLinks.value.first { it.studentId == dormStudents.first().id }
        assertEquals(2, TandemWaliHelper.parseMembers(firstLinkAfter.tandemWalisJson).size)

        // 5. 4th caregiver Wali D cannot request tandem join (cap 3 reached)
        vm.logout()
        assertTrue(vm.executeLogin("wali_d", "Pw#4", rememberMe = false) is LoginResult.SuccessWali)
        waitUntil { vm.students.value.size == 20 }
        var reqDCount: Int? = null
        var skippedDCount = 0
        vm.requestTandemJoinForStudents(vm.students.value) { req, skip, _ ->
            reqDCount = req
            skippedDCount = skip
        }
        waitUntil { reqDCount != null }
        assertEquals(0, reqDCount)
        assertEquals(20, skippedDCount)

        // 6. Parent account deletion permission: Tandem (wali_b) blocked, Primary (wali_a) allowed
        val targetStudent = vm.students.value.first { it.id == dormStudents.first().id }
        val targetLink = vm.parentAccessLinks.value.first { it.studentId == targetStudent.id }.copy(
            deletionRequested = true,
            deletionReason = "Lulus",
            deletionRequestedAt = System.currentTimeMillis(),
            canViewTabungan = true
        )
        db.parentAccessLinkDao().update(targetLink)

        vm.logout()
        assertTrue(vm.executeLogin("wali_b", "Pw#2", rememberMe = false) is LoginResult.SuccessWali)
        waitUntil { vm.parentAccessLinks.value.firstOrNull { it.id == targetLink.id }?.deletionRequested == true }

        var tandemRejectOk: Boolean? = null
        vm.rejectParentAccountDeletion(targetLink) { ok, _ -> tandemRejectOk = ok }
        waitUntil { tandemRejectOk != null }
        assertEquals(false, tandemRejectOk)

        var tandemApproveOk: Boolean? = null
        vm.approveAndDeleteParentAccount(targetLink) { ok, _ -> tandemApproveOk = ok }
        waitUntil { tandemApproveOk != null }
        assertEquals(false, tandemApproveOk)

        // 7. Tabungan isolation: Wali A holds 100k, Wali B holds 50k -> neither sees the other's ledger, Parent sees 150k total
        var txBOk: Boolean? = null
        vm.saveTabunganTransaction(targetStudent, TabunganHelper.TYPE_IN, 50_000L, "Titipan ke Wali B") { ok, _ -> txBOk = ok }
        waitUntil { txBOk == true && vm.tabunganTransactions.value.size == 1 }
        assertEquals("wali_b", vm.tabunganTransactions.value.first().ownerWaliUsername)

        vm.logout()
        assertTrue(vm.executeLogin("wali_a", "Pw#1", rememberMe = false) is LoginResult.SuccessWali)
        waitUntil { vm.students.value.isNotEmpty() }
        // Wali A must NOT see Wali B's 50k transaction
        assertEquals(0, vm.tabunganTransactions.value.size)

        var txAOk: Boolean? = null
        vm.saveTabunganTransaction(targetStudent, TabunganHelper.TYPE_IN, 100_000L, "Titipan ke Wali A") { ok, _ -> txAOk = ok }
        waitUntil { txAOk == true && vm.tabunganTransactions.value.size == 1 }
        assertEquals("wali_a", vm.tabunganTransactions.value.first().ownerWaliUsername)
        assertEquals(100_000L, TabunganHelper.computeAggregateTotals(vm.tabunganTransactions.value).totalBalance)

        // 8. Konseling privacy & optional read-only tandem share
        var privOk: Boolean? = null
        vm.saveKonselingRecord(
            student = targetStudent,
            entryType = KonselingHelper.ENTRY_SESI,
            topic = KonselingHelper.TOPIC_ADAPTASI,
            urgencyLevel = KonselingHelper.URGENCY_RINGAN,
            moodLevel = KonselingHelper.MOOD_NETRAL,
            title = "Catatan Rahasia Pribadi A",
            summary = "Hanya untuk Wali A",
            shareWithTandem = false
        ) { ok, _ -> privOk = ok }
        waitUntil { privOk == true }

        var sharedOk: Boolean? = null
        vm.saveKonselingRecord(
            student = targetStudent,
            entryType = KonselingHelper.ENTRY_PERKEMBANGAN,
            topic = KonselingHelper.TOPIC_APRESIASI,
            urgencyLevel = KonselingHelper.URGENCY_RINGAN,
            moodLevel = KonselingHelper.MOOD_BAIK,
            title = "Catatan Koordinasi Shift",
            summary = "Anak mulai betah di asrama",
            shareWithTandem = true
        ) { ok, _ -> sharedOk = ok }
        waitUntil { sharedOk == true && vm.konselingRecords.value.size == 2 }

        // Switch to Wali B -> sees ONLY the shared record, and is blocked from editing it
        vm.logout()
        assertTrue(vm.executeLogin("wali_b", "Pw#2", rememberMe = false) is LoginResult.SuccessWali)
        waitUntil { vm.konselingRecords.value.size == 1 }
        val sharedForB = vm.konselingRecords.value.first()
        assertEquals("Catatan Koordinasi Shift", sharedForB.title)

        var editByTandemOk: Boolean? = null
        vm.editKonselingRecord(
            existingRecord = sharedForB,
            entryType = sharedForB.entryType,
            topic = sharedForB.topic,
            urgencyLevel = sharedForB.urgencyLevel,
            moodLevel = sharedForB.moodLevel,
            title = "Coba Edit Tandem",
            summary = "Harus Ditolak"
        ) { ok, _ -> editByTandemOk = ok }
        waitUntil { editByTandemOk != null }
        assertEquals(false, editByTandemOk)

        // Parent Portal sees BOTH Tabungan holders (150k total) and ZERO Konseling records
        vm.logout()
        assertTrue(vm.executeLogin(targetLink.username, targetLink.password, rememberMe = false) is LoginResult.SuccessParent)
        waitUntil { vm.parentTabunganTransactions.value.size == 2 }
        assertEquals(150_000L, TabunganHelper.computeAggregateTotals(vm.parentTabunganTransactions.value).totalBalance)
        assertEquals(0, vm.konselingRecords.value.size)

        // 9. Admin override team assignment & Primary release promotion
        vm.switchRoleToAdmin()
        var adminAssignOk: Boolean? = null
        vm.adminAssignPrimaryAndTandems(
            student = targetStudent,
            primaryWali = w1,
            tandemWalis = listOf(w2, w4)
        ) { ok, _ -> adminAssignOk = ok }
        waitUntil { adminAssignOk == true }
        val overridden = vm.students.value.first { it.id == targetStudent.id }
        assertEquals("wali_a", overridden.assignedWaliUsername)
        assertTrue(TandemWaliHelper.isTandemMember(overridden, "wali_d", w4.name))

        // Primary Wali (wali_a) approves parent deletion and then releases claim -> promotes wali_b
        vm.logout()
        assertTrue(vm.executeLogin("wali_a", "Pw#1", rememberMe = false) is LoginResult.SuccessWali)
        waitUntil { vm.students.value.any { it.id == targetStudent.id } }

        val updatedTargetLink = vm.parentAccessLinks.value.first { it.studentId == targetStudent.id }
        var primaryApproveDelOk: Boolean? = null
        vm.approveAndDeleteParentAccount(updatedTargetLink) { ok, _ -> primaryApproveDelOk = ok }
        waitUntil { primaryApproveDelOk == true }

        val latestSt = vm.students.value.first { it.id == targetStudent.id }
        var releaseOk: Boolean? = null
        vm.releaseStudentClaim(latestSt) { ok, _ -> releaseOk = ok }
        waitUntil { releaseOk == true && vm.students.value.firstOrNull { it.id == targetStudent.id }?.assignedWaliUsername == "wali_b" }

        vm.logout()
        clearSchoolData(db, schoolId)
    }

    @Test
    fun `verify Compose UI guards across PilihAnakAsuhSheet KartuAksesOrangTuaSheet InputKonselingDialog and KonselingStudentDetailSheet`() {
        val indonesianStrings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        val student = ChildStudent(
            id = 8801L,
            name = "Zaki Alfarizi",
            nis = "880101",
            gender = "L",
            age = 14,
            dormitory = "Asrama Putra",
            classGrade = "IX-A",
            assignedWaliUsername = "wali_utama",
            assignedWaliName = "Ustadz Utama",
            tandemWalisJson = TandemWaliHelper.serializeMembers(listOf(TandemWaliMember("wali_tandem", "Ustadz Tandem"))),
            schoolId = "SCH-UI"
        )
        val linkWithDeleteReq = ParentAccessLink(
            id = 8802L,
            studentId = student.id,
            studentName = student.name,
            parentName = "Ibu Zaki",
            username = "ortu_zaki",
            password = "pw",
            assignedWaliUsername = student.assignedWaliUsername,
            assignedWaliName = student.assignedWaliName,
            tandemWalisJson = student.tandemWalisJson,
            deletionRequested = true,
            deletionReason = "Pindah sekolah",
            deletionRequestedAt = 1_700_000_000_000L,
            schoolId = "SCH-UI"
        )
        val sharedRecordByOther = KonselingRecord(
            id = 8803L,
            studentId = student.id,
            studentName = student.name,
            studentNis = student.nis,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            ownerWaliUsername = "wali_utama",
            ownerWaliName = "Ustadz Utama",
            isConfidential = true,
            shareWithTandem = true,
            entryType = KonselingHelper.ENTRY_SESI,
            topic = KonselingHelper.TOPIC_ADAPTASI,
            urgencyLevel = KonselingHelper.URGENCY_RINGAN,
            moodLevel = KonselingHelper.MOOD_NETRAL,
            title = "Observasi Adaptasi",
            summary = "Catatan baca-saja dari Wali Utama",
            schoolId = "SCH-UI"
        )

        // 1. PilihAnakAsuhSheet bundle approve & request callback
        var approvedFromSheet = false
        val bundle = IncomingTandemRequestBundle(
            requesterUsername = "wali_baru",
            requesterName = "Ustadz Baru",
            students = listOf(student),
            latestRequestedAt = 1000L
        )
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides indonesianStrings) {
                PilihAnakAsuhSheet(
                    students = listOf(student),
                    currentWaliDisplayName = "Ustadz Utama",
                    isStudentClaimedByCurrentWali = { true },
                    isStudentClaimedByOtherWali = { false },
                    canReleaseStudent = { true },
                    onClaimStudents = {},
                    onReleaseStudent = {},
                    onDismiss = {},
                    incomingTandemBundles = listOf(bundle),
                    currentUsername = "wali_utama",
                    onApproveTandemBundle = { _, _, approvedIds, _ ->
                        approvedFromSheet = student.id in approvedIds
                    }
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("card_tandem_bundle_wali_baru").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_approve_tandem_all_wali_baru").performClick()
        assertTrue(approvedFromSheet)

        // 2. KartuAksesOrangTuaSheet blocks deletion buttons for non-primary tandem caregiver
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides indonesianStrings) {
                KartuAksesOrangTuaSheet(
                    students = listOf(student),
                    parentAccessLinks = listOf(linkWithDeleteReq),
                    schoolProfile = SchoolProfile(tenantCode = "SCH-UI", schoolName = "Sekolah Uji"),
                    isStudentClaimedByCurrentWali = { true },
                    isAdmin = false,
                    onGenerateAccessCard = {},
                    onResetPassword = {},
                    onEditStudentProfile = {},
                    onDismiss = {},
                    isPrimaryWaliForStudent = { false }
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Persetujuan hapus akun orang tua khusus Wali Utama atau Admin Sekolah.")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_approve_parent_delete_${student.id}").assertDoesNotExist()

        // 3. InputKonselingDialog share-with-tandem toggle passes flag to onSaveWithShare
        var capturedShareFlag: Boolean? = null
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides indonesianStrings) {
                InputKonselingDialog(
                    claimedStudents = listOf(student),
                    initialStudent = student,
                    onOpenPilihAnakAsuh = {},
                    onSave = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
                    onDismiss = {},
                    onSaveWithShare = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, shareWithTandem, _ ->
                        capturedShareFlag = shareWithTandem
                    }
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("input_konseling_summary").performScrollTo().performTextInput("Observasi hari ini")
        composeTestRule.onNodeWithTag("switch_konseling_share_tandem").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("btn_save_konseling").performScrollTo().performClick()
        assertEquals(true, capturedShareFlag)

        // 4. KonselingStudentDetailSheet enforces read-only view for non-owned shared tandem notes
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides indonesianStrings) {
                KonselingStudentDetailSheet(
                    student = student,
                    studentRecords = listOf(sharedRecordByOther),
                    studentLogs = emptyList(),
                    waliName = "Ustadz Tandem",
                    schoolProfile = SchoolProfile(tenantCode = "SCH-UI", schoolName = "Sekolah Uji"),
                    isRecordOwnedByCurrentWali = { false },
                    onQuickInput = {},
                    onEditRecord = {},
                    onToggleStatus = {},
                    onDeleteRecord = {},
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Dibagikan • Oleh Ustadz Utama (Baca-Saja)").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_edit_konseling_${sharedRecordByOther.id}").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_toggle_konseling_${sharedRecordByOther.id}").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_delete_konseling_${sharedRecordByOther.id}").assertDoesNotExist()
    }

    @Test
    fun `verify ParentPortalScreen displays 3-wali tandem team live piket badge per-wali official sheet and tabungan holder labels`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = AppDatabase.getDatabase(app)
        val vm = HealthViewModel(app)
        val schoolId = "SCH-PARENT-TANDEM"
        clearSchoolData(db, schoolId)

        val now = System.currentTimeMillis()
        val monthKey = WaliScheduleHelper.currentMonthKey(now)
        val day = WaliScheduleHelper.currentDayOfMonth(now)
        val hour = WaliScheduleHelper.currentHourOfDay(now)
        val activeShift = com.example.data.model.WaliShiftCode(
            schoolId = schoolId,
            code = "FULL24",
            name = "Piket Aktif",
            shiftGroup = WaliScheduleHelper.GROUP_PAGI,
            timeRange = "00:00 - 23:59",
            startHour = 0,
            endHour = 23,
            workHours = 24
        )
        db.waliShiftCodeDao().insert(activeShift)

        val w1 = WaliAccount(id = 9101L, name = "Ustadz Ridwan", username = "ridwan_u", password = "s1", dormitory = "Asrama Putra", phone = "08110000111", titlePosition = "Wali Utama Putra", schoolId = schoolId)
        val w2 = WaliAccount(id = 9102L, name = "Ustadz Fajar", username = "fajar_t", password = "s2", dormitory = "Asrama Putra", phone = "08110000222", titlePosition = "Wali Tandem Sesi Sore", schoolId = schoolId)
        val w3 = WaliAccount(id = 9103L, name = "Ustadz Ilham", username = "ilham_t", password = "s3", dormitory = "Asrama Putra", phone = "08110000333", titlePosition = "Wali Tandem Malam", schoolId = schoolId)
        db.waliAccountDao().insertAll(listOf(w1, w2, w3))

        db.waliScheduleEntryDao().insert(
            WaliScheduleEntry(
                schoolId = schoolId,
                monthKey = monthKey,
                waliAccountId = w2.id,
                waliUsername = w2.username,
                waliName = w2.name,
                dayCodesJson = WaliScheduleHelper.serializeDayCodes(mapOf(day to "FULL24"))
            )
        )

        val tandemJson = TandemWaliHelper.serializeMembers(
            listOf(
                TandemWaliMember(username = w2.username, name = w2.name, addedAt = now),
                TandemWaliMember(username = w3.username, name = w3.name, addedAt = now)
            )
        )
        val student = ChildStudent(
            id = 9110L,
            name = "Farhan Maulana",
            nis = "911001",
            gender = "L",
            age = 15,
            dormitory = "Asrama Putra",
            classGrade = "IX-B",
            assignedWaliUsername = w1.username,
            assignedWaliName = w1.name,
            tandemWalisJson = tandemJson,
            schoolId = schoolId
        )
        db.childStudentDao().insert(student)

        val link = ParentAccessLink(
            id = 9120L,
            studentId = student.id,
            studentName = student.name,
            studentNis = student.nis,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            parentName = "Bapak Farhan",
            parentPhone = "081234567890",
            username = "ortu_farhan_91",
            password = "Parent91Pass",
            assignedWaliUsername = w1.username,
            assignedWaliName = w1.name,
            tandemWalisJson = tandemJson,
            canViewProfile = true,
            canViewTabungan = true,
            schoolId = schoolId
        )
        db.parentAccessLinkDao().insert(link)

        db.tabunganTransactionDao().insertAll(
            listOf(
                com.example.data.model.TabunganTransaction(
                    id = 91001L,
                    studentId = student.id,
                    studentName = student.name,
                    studentNis = student.nis,
                    ownerWaliUsername = w1.username,
                    ownerWaliName = w1.name,
                    transactionType = TabunganHelper.TYPE_IN,
                    amount = 80_000L,
                    title = "Titip Saku 1",
                    schoolId = schoolId,
                    transactionDate = now - 1000L
                ),
                com.example.data.model.TabunganTransaction(
                    id = 91002L,
                    studentId = student.id,
                    studentName = student.name,
                    studentNis = student.nis,
                    ownerWaliUsername = w2.username,
                    ownerWaliName = w2.name,
                    transactionType = TabunganHelper.TYPE_IN,
                    amount = 45_000L,
                    title = "Titip Saku 2",
                    schoolId = schoolId,
                    transactionDate = now
                )
            )
        )

        vm.updateSchoolProfile(SchoolProfile(tenantCode = schoolId, schoolName = "Sekolah Rakyat Tandem"))
        assertTrue(vm.executeLogin("ortu_farhan_91", "Parent91Pass", rememberMe = false) is LoginResult.SuccessParent)
        waitUntil {
            vm.activeParentAccessLink.value != null &&
                vm.waliAccounts.value.size == 3 &&
                vm.parentTabunganTransactions.value.size == 2
        }

        val indonesianStrings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides indonesianStrings) {
                ParentPortalScreen(viewModel = vm, onLogout = {})
            }
        }
        composeTestRule.waitForIdle()

        // Profile tab shows all 3 team members & live Sedang Piket badge on Fajar (unless hour == 23)
        composeTestRule.onNodeWithTag("card_parent_summary_profile").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("parent_profile_content")
            .performScrollToNode(hasTestTag("card_parent_wali_legitimacy"))
        composeTestRule.onNodeWithTag("card_parent_wali_member_ridwan_u").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_parent_wali_member_fajar_t").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_parent_wali_member_ilham_t").performScrollTo().assertIsDisplayed()
        if (hour < 23) {
            composeTestRule.onNodeWithText("Sedang Piket").performScrollTo().assertIsDisplayed()
        }

        // Open tandem member's Official Card -> opens Ustadz Fajar's WaliOfficialProfileSheet
        composeTestRule.onNodeWithTag("btn_parent_open_wali_legitimacy_fajar_t").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("sheet_wali_official_profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wali Tandem Sesi Sore").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_close_wali_official_profile").performClick()
        composeTestRule.waitForIdle()

        // Switch back Home -> open Tabungan -> shows per-holder breakdown & Pemegang tags
        composeTestRule.onNodeWithContentDescription("Ringkasan").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("card_parent_summary_tabungan").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("label_parent_tabungan_holder_91001").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("label_parent_tabungan_holder_91002").performScrollTo().assertIsDisplayed()

        vm.logout()
        clearSchoolData(db, schoolId)
    }

    @Test
    fun `verify PocketBase DTO roundtrip and SchemaProvider include tandem and shareWithTandem fields`() {
        val tandemJson = TandemWaliHelper.serializeMembers(listOf(TandemWaliMember("wali2", "Wali Dua", 1234L)))
        val reqJson = TandemWaliHelper.serializeRequests(listOf(TandemJoinRequest("wali3", "Wali Tiga", 5678L)))

        val student = ChildStudent(
            id = 123L,
            name = "Siswa DTO",
            nis = "321",
            gender = "L",
            age = 12,
            dormitory = "Asrama",
            classGrade = "VII",
            assignedWaliUsername = "wali1",
            assignedWaliName = "Wali Satu",
            tandemWalisJson = tandemJson,
            tandemRequestsJson = reqJson,
            schoolId = "SCH-DTO"
        )
        val studentDomain = PbStudentDto.fromDomain(student).toDomainModel()
        assertEquals(tandemJson, studentDomain.tandemWalisJson)
        assertEquals(reqJson, studentDomain.tandemRequestsJson)

        val link = ParentAccessLink(
            id = 456L,
            studentId = 123L,
            studentName = "Siswa DTO",
            username = "ortu_dto",
            password = "pw",
            assignedWaliUsername = "wali1",
            assignedWaliName = "Wali Satu",
            tandemWalisJson = tandemJson,
            schoolId = "SCH-DTO"
        )
        val linkDomain = PbParentAccessLinkDto.fromDomain(link).toDomainModel()
        assertEquals(tandemJson, linkDomain.tandemWalisJson)

        val konseling = KonselingRecord(
            id = 9004L,
            studentId = 123L,
            studentName = "Siswa DTO",
            ownerWaliUsername = "wali1",
            ownerWaliName = "Wali Satu",
            isConfidential = true,
            shareWithTandem = true,
            title = "Judul",
            summary = "Isi",
            schoolId = "SCH-DTO"
        )
        val konselingDomain = PbKonselingRecordDto.fromDomain(konseling).toDomainModel()
        assertTrue(konselingDomain.shareWithTandem)

        val schemas = PocketBaseSchemaProvider.COLLECTIONS
        val studentsCols = schemas.first { it.collectionName == "child_students" }.fields.map { it.name }
        val linkCols = schemas.first { it.collectionName == "parent_access_links" }.fields.map { it.name }
        val konselingCols = schemas.first { it.collectionName == "konseling_records" }.fields.map { it.name }

        assertTrue("tandem_walis_json" in studentsCols)
        assertTrue("tandem_requests_json" in studentsCols)
        assertTrue("tandem_walis_json" in linkCols)
        assertTrue("share_with_tandem" in konselingCols)
    }
}
