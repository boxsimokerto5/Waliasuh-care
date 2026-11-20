package com.example

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
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
            db.schoolProfileDao().insert(
                SchoolProfile(
                    id = 1,
                    schoolName = "Sekolah Rakyat Kediri",
                    shortName = "SR Kediri",
                    tenantCode = schoolId,
                    activeThemeId = "EMERALD_CLEAN"
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
            vm.updateLanguage(AppLanguage.INDONESIAN)
            val result = vm.executeLogin("budi_jq1", "pw", rememberMe = false)
            assertEquals(LoginResult.WALI, result)
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
        val profile = SchoolProfile(id = 1, schoolName = "SR Kediri", shortName = "SRK", tenantCode = schoolId)
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)

        var canManageFlag = false
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
                        canManageSchedule = canManageFlag,
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
}
