package com.example

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.PinnedTaskItem
import com.example.data.model.SchoolProfile
import com.example.data.model.WaliAccount
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliScheduleHelper
import com.example.ui.HealthViewModel
import com.example.ui.LoginResult
import com.example.ui.components.rememberDashboardModalState
import com.example.ui.screens.JadwalQuickScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class JadwalQuickTabRobolectricTest {

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
        db.waliScheduleEntryDao().deleteBySchoolId(schoolId)
        db.waliShiftCodeDao().deleteBySchoolId(schoolId)
        db.waliAccountDao().deleteBySchoolId(schoolId)
    }

    @Test
    fun `bottom bar replaces Tindakan with Jadwal and clicking opens quick daily roster starting at date selector`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SCH-JQ1"
        val monthKey = WaliScheduleHelper.currentMonthKey()
        val todayDay = WaliScheduleHelper.currentDayOfMonth()
        val waliId = runBlocking {
            clearSchoolData(db, schoolId)
            val wId = db.waliAccountDao().insert(
                WaliAccount(
                    name = "Ustadz Budi",
                    username = "budi_jq1",
                    password = "pw",
                    schoolId = schoolId,
                    dormitory = "Asrama Putra"
                )
            )
            db.waliScheduleEntryDao().insert(
                WaliScheduleEntry(
                    schoolId = schoolId,
                    monthKey = monthKey,
                    waliUsername = "budi_jq1",
                    waliName = "Ustadz Budi",
                    sortOrder = 1,
                    dayCodesJson = WaliScheduleHelper.serializeDayCodes(
                        mapOf(todayDay to "P1", (if (todayDay < 28) todayDay + 1 else 1) to "S3A")
                    ),
                    dayPinsJson = WaliScheduleHelper.serializeDayPins(
                        mapOf(todayDay to listOf(PinnedTaskItem("Pencatatan Siswa Sakit")))
                    )
                )
            )
            wId
        }
        assert(waliId > 0)

        val vm = HealthViewModel(app)
        runBlocking {
            vm.setAppLanguage(AppLanguage.INDONESIAN)
            vm.updateSchoolProfile(
                SchoolProfile(
                    schoolName = "Sekolah Rakyat Kediri",
                    shortName = "SR Kediri",
                    tenantCode = schoolId
                )
            )
            val result = vm.executeLogin("budi_jq1", "pw", rememberMe = false)
            org.junit.Assert.assertTrue(result is LoginResult.SuccessWali)
            waitUntil { vm.schoolScheduleEntries.value.any { it.schoolId == schoolId } }
        }

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                MyApplicationTheme {
                    WaliAsuhCareApp(
                        isDarkTheme = false,
                        onToggleTheme = {},
                        initialScreen = WaliNavScreen.DASHBOARD,
                        viewModel = vm
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        // Bottom bar shows Jadwal in place of Tindakan
        composeTestRule.onNodeWithTag("bottom_nav_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_item_jadwal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jadwal").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_item_tindakan").assertDoesNotExist()

        // Clicking Jadwal opens the clean daily roster view directly
        composeTestRule.onNodeWithTag("nav_item_jadwal").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("jadwal_quick_screen_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_jadwal_prev_day").assertIsDisplayed()
        composeTestRule.onNodeWithTag("text_jadwal_selected_date").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_jadwal_next_day").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_jadwal_hub_search").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_jadwal_group_PAGI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ustadz Budi").assertIsDisplayed()

        // Option A verification: hero, 2x2 hub cards, WA/PDF buttons, and leave banner are absent
        composeTestRule.onNodeWithTag("card_jadwal_rekan_izin").assertDoesNotExist()
        composeTestRule.onNodeWithTag("card_jadwal_personal_hero").assertDoesNotExist()
        composeTestRule.onNodeWithTag("card_jadwal_matriks").assertDoesNotExist()
        composeTestRule.onNodeWithTag("card_jadwal_pin_tugas").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_jadwal_share_wa").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_jadwal_pdf").assertDoesNotExist()
    }

    @Test
    fun `jadwal quick screen date switcher search filter and role-based member tap dialogs work`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SCH-JQ2"
        val monthKey = "2026-11"
        val (entry1Id, entry2Id) = runBlocking {
            clearSchoolData(db, schoolId)
            val id1 = db.waliScheduleEntryDao().insert(
                WaliScheduleEntry(
                    schoolId = schoolId,
                    monthKey = monthKey,
                    waliUsername = "wali_satu",
                    waliName = "Ambika Widya",
                    sortOrder = 1,
                    dayCodesJson = WaliScheduleHelper.serializeDayCodes(
                        mapOf(20 to "P1", 21 to "S3A")
                    )
                )
            )
            val id2 = db.waliScheduleEntryDao().insert(
                WaliScheduleEntry(
                    schoolId = schoolId,
                    monthKey = monthKey,
                    waliUsername = "wali_dua",
                    waliName = "Abisarwan Rafif",
                    sortOrder = 2,
                    dayCodesJson = WaliScheduleHelper.serializeDayCodes(
                        mapOf(20 to "S3A", 21 to "M1")
                    )
                )
            )
            id1 to id2
        }

        val vm = HealthViewModel(app)
        val shiftCodes = WaliScheduleHelper.defaultShiftCodes(schoolId)
        val entries = listOf(
            WaliScheduleEntry(
                id = entry1Id,
                schoolId = schoolId,
                monthKey = monthKey,
                waliUsername = "wali_satu",
                waliName = "Ambika Widya",
                sortOrder = 1,
                dayCodesJson = WaliScheduleHelper.serializeDayCodes(mapOf(20 to "P1", 21 to "S3A"))
            ),
            WaliScheduleEntry(
                id = entry2Id,
                schoolId = schoolId,
                monthKey = monthKey,
                waliUsername = "wali_dua",
                waliName = "Abisarwan Rafif",
                sortOrder = 2,
                dayCodesJson = WaliScheduleHelper.serializeDayCodes(mapOf(20 to "S3A", 21 to "M1"))
            )
        )
        val profile = SchoolProfile(schoolName = "SR Kediri", shortName = "SRK", tenantCode = schoolId)
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)

        val canManageState = androidx.compose.runtime.mutableStateOf(false)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                MyApplicationTheme {
                    val modalState = rememberDashboardModalState().apply {
                        jadwalSelectedMonthKey = monthKey
                        jadwalSelectedDay = 20
                    }
                    JadwalQuickScreen(
                        viewModel = vm,
                        schoolProfile = profile,
                        shiftCodes = shiftCodes,
                        scheduleEntries = entries,
                        canManageSchedule = canManageState.value,
                        waliAccounts = emptyList(),
                        currentUsername = "wali_satu",
                        currentWaliName = "Ambika Widya",
                        modalState = modalState
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        val day20Label = WaliScheduleHelper.formatDateChipLabel(monthKey, 20, false)
        val day21Label = WaliScheduleHelper.formatDateChipLabel(monthKey, 21, false)
        composeTestRule.onNodeWithTag("text_jadwal_selected_date").assertTextEquals(day20Label)
        composeTestRule.onNodeWithTag("card_jadwal_group_PAGI").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_jadwal_group_SORE").assertIsDisplayed()

        // Filter chip SORE hides PAGI group
        composeTestRule.onNodeWithTag("chip_jadwal_group_SORE").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("card_jadwal_group_PAGI").assertDoesNotExist()
        composeTestRule.onNodeWithTag("card_jadwal_group_SORE").assertIsDisplayed()

        // Reset to ALL and switch to next day (day 21 -> Abisarwan moves to MALAM)
        composeTestRule.onNodeWithTag("chip_jadwal_group_ALL").performClick()
        composeTestRule.onNodeWithTag("btn_jadwal_next_day").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("text_jadwal_selected_date").assertTextEquals(day21Label)
        composeTestRule.onNodeWithTag("card_jadwal_group_MALAM").assertIsDisplayed()

        // Switch back to day 20 and filter via search
        composeTestRule.onNodeWithTag("btn_jadwal_prev_day").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("input_jadwal_hub_search").performTextInput("Ambika")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("card_jadwal_group_PAGI").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_jadwal_group_SORE").assertDoesNotExist()

        // Regular Wali tap -> opens JadwalPersonalSheet
        composeTestRule.onNodeWithTag("row_jadwal_member_$entry1Id").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("sheet_jadwal_personal").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_close_jadwal_personal").performClick()
        composeTestRule.waitForIdle()

        // Admin / Anak Admin mode -> tapping opens EditJadwalSelDialog
        composeTestRule.runOnUiThread { canManageState.value = true }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("row_jadwal_member_$entry1Id").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dialog_edit_jadwal_sel").assertIsDisplayed()
    }

    @Test
    fun `medical records screen TINDAKAN remains directly reachable via initialScreen`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                MyApplicationTheme {
                    WaliAsuhCareApp(
                        isDarkTheme = false,
                        onToggleTheme = {},
                        initialScreen = WaliNavScreen.TINDAKAN,
                        viewModel = vm
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("fab_direct_treatment").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_item_jadwal").assertIsDisplayed()
    }

    @Test
    fun `anak admin penyematan has separate 7 quota and locks shift and leave on schedule update`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SCH-PIN-1"
        val monthKey = "2026-11"
        val initialLeave = com.example.data.model.LeaveDayInfo(status = "IZIN", driveUrl = "", note = "Rapat")

        runBlocking {
            clearSchoolData(db, schoolId)
            // Fill 7/7 Anak Admin Utama slots
            for (i in 1..7) {
                db.waliAccountDao().insert(
                    WaliAccount(
                        name = "Utama $i",
                        username = "utama_$i",
                        password = "pw",
                        schoolId = schoolId,
                        role = WaliAccount.ROLE_WALI_ANAK_ADMIN
                    )
                )
            }
            // Add 1 Anak Admin Penyematan (separate quota 1/7)
            db.waliAccountDao().insert(
                WaliAccount(
                    name = "Pin Admin Satu",
                    username = "pin_admin_1",
                    password = "pw",
                    schoolId = schoolId,
                    role = WaliAccount.ROLE_WALI_ANAK_ADMIN_PIN
                )
            )
            db.waliScheduleEntryDao().insert(
                WaliScheduleEntry(
                    schoolId = schoolId,
                    monthKey = monthKey,
                    waliUsername = "target_wali",
                    waliName = "Wali Target",
                    sortOrder = 1,
                    dayCodesJson = WaliScheduleHelper.serializeDayCodes(mapOf(15 to "P1")),
                    dayLeaveJson = WaliScheduleHelper.serializeDayLeaves(mapOf(15 to initialLeave)),
                    dayPinsJson = "{}"
                )
            )
        }

        val vm = HealthViewModel(app)
        runBlocking {
            vm.updateSchoolProfile(
                SchoolProfile(schoolName = "SR Uji", shortName = "SRU", tenantCode = schoolId)
            )
            val loginRes = vm.executeLogin("pin_admin_1", "pw", rememberMe = false)
            org.junit.Assert.assertTrue(loginRes is LoginResult.SuccessWali)
            waitUntil { vm.schoolScheduleEntries.value.any { it.schoolId == schoolId } && vm.isAnakAdminPenyematan.value }

            org.junit.Assert.assertFalse(vm.canManageSchoolSchedule())
            org.junit.Assert.assertTrue(vm.canPinScheduleTasks())

            val targetEntry = vm.schoolScheduleEntries.value.first { it.waliUsername == "target_wali" }
            var saveOk = false
            vm.updateWaliScheduleDayCell(
                entry = targetEntry,
                day = 15,
                shiftCode = "M3", // Attempted shift change MUST be ignored for Penyematan
                pinsForDay = listOf(PinnedTaskItem("Jaga RSUD", WaliScheduleHelper.PIN_FASKES)),
                leaveForDay = null // Attempted leave clear MUST be ignored for Penyematan
            ) { ok, _ -> saveOk = ok }

            waitUntil {
                saveOk && (WaliScheduleHelper.parseDayPins(
                    vm.schoolScheduleEntries.value.firstOrNull { it.id == targetEntry.id }?.dayPinsJson.orEmpty()
                )[15]?.isNotEmpty() == true)
            }

            val saved = vm.schoolScheduleEntries.value.first { it.id == targetEntry.id }
            assertEquals("P1", WaliScheduleHelper.parseDayCodes(saved.dayCodesJson)[15])
            assertEquals("IZIN", WaliScheduleHelper.parseDayLeaves(saved.dayLeaveJson)[15]?.status)
            assertEquals(
                listOf("Jaga RSUD"),
                WaliScheduleHelper.parseDayPins(saved.dayPinsJson)[15]?.map { it.label }
            )
        }
    }

    @Test
    fun `EditJadwalSelDialog in pinOnlyMode locks shift and leave while saving pinned tasks`() {
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        val profile = SchoolProfile(schoolName = "SR Kediri", tenantCode = "SCH-PIN-2")
        val shiftCodes = WaliScheduleHelper.defaultShiftCodes("SCH-PIN-2")
        val existingLeave = com.example.data.model.LeaveDayInfo(status = "SAKIT", note = "Demam")
        val entry = WaliScheduleEntry(
            id = 301L,
            schoolId = "SCH-PIN-2",
            monthKey = "2026-11",
            waliUsername = "wali_uji",
            waliName = "Ustadz Uji",
            dayCodesJson = WaliScheduleHelper.serializeDayCodes(mapOf(10 to "P1")),
            dayLeaveJson = WaliScheduleHelper.serializeDayLeaves(mapOf(10 to existingLeave)),
            dayPinsJson = "{}"
        )
        var savedTriple: Triple<String, List<PinnedTaskItem>, com.example.data.model.LeaveDayInfo?>? = null

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                MyApplicationTheme {
                    com.example.ui.dialogs.EditJadwalSelDialog(
                        entry = entry,
                        day = 10,
                        monthKey = "2026-11",
                        shiftCodes = shiftCodes,
                        schoolProfile = profile,
                        pinOnlyMode = true,
                        onSave = { code, pins, leave -> savedTriple = Triple(code, pins, leave) },
                        onDismiss = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("banner_pin_only_mode").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_locked_shift_preview").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chip_pick_shift_P1").assertDoesNotExist()
        composeTestRule.onNodeWithTag("chip_leave_status_IZIN").assertDoesNotExist()

        composeTestRule.onNodeWithTag("input_pin_custom_label").performTextInput("Jaga UKS Malam")
        composeTestRule.onNodeWithTag("btn_add_custom_pin").performClick()
        composeTestRule.onNodeWithTag("btn_save_jadwal_sel").performClick()
        composeTestRule.waitForIdle()

        assertEquals("P1", savedTriple?.first)
        assertEquals(existingLeave, savedTriple?.third)
        assertEquals(listOf("Jaga UKS Malam"), savedTriple?.second?.map { it.label })
    }

    @Test
    fun `KelolaAnakAdminDialog supports segmented Utama and Penyematan tabs with separate quotas`() {
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        val profile = SchoolProfile(schoolName = "SR Kediri", tenantCode = "SCH-PIN-3")
        val accounts = listOf(
            WaliAccount(id = 1L, name = "A. Utama", username = "u1", password = "pw", schoolId = "SCH-PIN-3", role = WaliAccount.ROLE_WALI_ANAK_ADMIN),
            WaliAccount(id = 2L, name = "B. Reguler", username = "r1", password = "pw", schoolId = "SCH-PIN-3", role = WaliAccount.ROLE_WALI)
        )
        var selectedRoleCall: Pair<Long, String>? = null

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                MyApplicationTheme {
                    com.example.ui.dialogs.KelolaAnakAdminDialog(
                        waliAccounts = accounts,
                        schoolProfile = profile,
                        activeSchoolId = "SCH-PIN-3",
                        onToggleAnakAdmin = { _, _ -> },
                        onSelectAnakAdminRole = { acc, role -> selectedRoleCall = acc.id to role },
                        onDismiss = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tab_picker_anak_admin_utama").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_picker_anak_admin_pin").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("switch_picker_anak_admin_2").performClick()
        assertEquals(2L to WaliAccount.ROLE_WALI_ANAK_ADMIN_PIN, selectedRoleCall)
    }

    @Test
    fun `jadwal quick screen shows Izin and Cuti block above Jaga Pagi when present and hides it when empty`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val schoolId = "SCH-IZIN-TOP"
        val monthKey = "2026-11"
        val shiftCodes = WaliScheduleHelper.defaultShiftCodes(schoolId)
        val entries = listOf(
            WaliScheduleEntry(
                id = 1L,
                schoolId = schoolId,
                monthKey = monthKey,
                waliUsername = "wali_pagi",
                waliName = "Ambika Widya",
                sortOrder = 1,
                dayCodesJson = WaliScheduleHelper.serializeDayCodes(mapOf(20 to "P1", 21 to "P1"))
            ),
            WaliScheduleEntry(
                id = 2L,
                schoolId = schoolId,
                monthKey = monthKey,
                waliUsername = "wali_cuti",
                waliName = "Deni Furitrinofi",
                sortOrder = 2,
                dayCodesJson = WaliScheduleHelper.serializeDayCodes(mapOf(20 to "C", 21 to "S3A"))
            )
        )
        val profile = SchoolProfile(schoolName = "SR Kediri", shortName = "SRK", tenantCode = schoolId)
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                MyApplicationTheme {
                    val modalState = rememberDashboardModalState().apply {
                        jadwalSelectedMonthKey = monthKey
                        jadwalSelectedDay = 20
                    }
                    JadwalQuickScreen(
                        viewModel = vm,
                        schoolProfile = profile,
                        shiftCodes = shiftCodes,
                        scheduleEntries = entries,
                        canManageSchedule = false,
                        waliAccounts = emptyList(),
                        currentUsername = "wali_pagi",
                        currentWaliName = "Ambika Widya",
                        modalState = modalState
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        // Day 20 has Deni on Cuti ("C"): Izin & Cuti block MUST appear strictly ABOVE Jaga Pagi
        composeTestRule.onNodeWithTag("card_jadwal_group_IZIN").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_jadwal_group_PAGI").assertIsDisplayed()
        val izinTop = composeTestRule.onNodeWithTag("card_jadwal_group_IZIN").getBoundsInRoot().top
        val pagiTop = composeTestRule.onNodeWithTag("card_jadwal_group_PAGI").getBoundsInRoot().top
        org.junit.Assert.assertTrue("Expected Izin & Cuti ($izinTop) above Jaga Pagi ($pagiTop)", izinTop < pagiTop)

        // Day 21 has NO leave/cuti entries: Izin & Cuti block MUST be completely hidden
        composeTestRule.onNodeWithTag("btn_jadwal_next_day").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("card_jadwal_group_IZIN").assertDoesNotExist()
        composeTestRule.onNodeWithTag("card_jadwal_group_PAGI").assertIsDisplayed()
    }
}
