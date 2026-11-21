package com.example

import android.app.Application
import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.DashboardTopHeroPager
import com.example.ui.components.TourCoordinateRegistry
import com.example.util.PrayerTimeHelper
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PrayerScheduleHeroPagerRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `PrayerTimeHelper resolves Indonesian cities and computes chronological Kemenag RI schedule`() {
        val kediri = PrayerTimeHelper.resolveCity("Kabupaten Kediri", null)
        assertEquals("kab_kediri", kediri.id)
        assertEquals("WIB", kediri.zoneLabel)

        val makassar = PrayerTimeHelper.resolveCity("Makassar", null)
        assertEquals("kota_makassar", makassar.id)
        assertEquals("WITA", makassar.zoneLabel)

        val jayapura = PrayerTimeHelper.resolveCity("Jayapura", null)
        assertEquals("kota_jayapura", jayapura.id)
        assertEquals("WIT", jayapura.zoneLabel)

        val morningCal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 21, 10, 0, 0)
        }
        val schedule = PrayerTimeHelper.calculateDailyPrayerSchedule(kediri, morningCal)
        val minutes = schedule.slots.map { it.totalMinutes }
        assertEquals(5, minutes.size)
        for (i in 0 until minutes.lastIndex) {
            assertTrue("Expected slot $i (${minutes[i]}) < slot ${i + 1} (${minutes[i + 1]})", minutes[i] < minutes[i + 1])
        }
        // At 10:00 WIB, next prayer is Dzuhur (index 1)
        assertEquals(1, schedule.nextSlotIndex)
        assertEquals("Dzuhur", schedule.nextPrayerNameId)
        assertTrue(schedule.subuh.startsWith("03:") || schedule.subuh.startsWith("04:"))
        assertTrue(schedule.maghrib.startsWith("17:"))

        // After Isya (21:30), next prayer wraps to Subuh (index 0)
        val nightCal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 21, 21, 30, 0)
        }
        val nightSchedule = PrayerTimeHelper.calculateDailyPrayerSchedule(kediri, nightCal)
        assertEquals(0, nightSchedule.nextSlotIndex)
        assertEquals("Subuh", nightSchedule.nextPrayerNameId)
    }

    @Test
    fun `DashboardTopHeroPager switches between Berobat and Jadwal Sholat cards and updates city via dropdown`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_prayer_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        var absenSholatClicked = false
        val registry = TourCoordinateRegistry()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                DashboardTopHeroPager(
                    todayShortFormatted = "21 Nov 2026",
                    waliName = "Eko Wahyudi",
                    currentWaliAccount = null,
                    schoolCityName = "Kabupaten Kediri",
                    sortedFaskesEntries = listOf(
                        Triple("UKS Sekolah", 0, emptyList()),
                        Triple("Puskesmas Wates", 0, emptyList())
                    ),
                    customFaskesInpatients = emptyMap(),
                    totalToday = 0,
                    totalSchoolStudents = 343,
                    upcomingCount = 2,
                    progressRatio = 0f,
                    isEn = false,
                    tourRegistry = registry,
                    onNavigateToProfile = {},
                    onAddPlan = {},
                    onSelectInpatientRecord = {},
                    onOpenInpatientPicker = { _, _ -> },
                    onOpenAbsenSholat = { absenSholatClicked = true }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Page 0: Berobat Hari Ini
        composeTestRule.onNodeWithTag("card_summary_today").assertIsDisplayed()
        composeTestRule.onNodeWithTag("row_faskes_strip").assertIsDisplayed()
        composeTestRule.onNodeWithTag("fab_dashboard_add").assertIsDisplayed()

        // Switch to Page 1: Jadwal Sholat Hari Ini
        composeTestRule.onNodeWithTag("hero_pager_tab_prayer").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_prayer_times_today").assertIsDisplayed()
        composeTestRule.onNodeWithTag("badge_next_prayer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_prayer_subuh").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_prayer_dzuhur").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_prayer_ashar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_prayer_maghrib").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_prayer_isya").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kab. Kediri").assertIsDisplayed()

        // Open City Dropdown Picker and select Kota Makassar (WITA)
        composeTestRule.onNodeWithTag("btn_prayer_city_dropdown").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("prayer_city_picker_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_search_prayer_city").performTextInput("Makassar")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("item_prayer_city_kota_makassar").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Makassar").assertIsDisplayed()
        composeTestRule.onNodeWithText("WITA", substring = true).assertIsDisplayed()
        assertEquals("kota_makassar", PrayerTimeHelper.getSavedCityId(app))

        // Quick shortcut to Absen Sholat
        composeTestRule.onNodeWithTag("btn_prayer_card_open_absen").performClick()
        composeTestRule.waitForIdle()
        assertTrue(absenSholatClicked)
    }
}
