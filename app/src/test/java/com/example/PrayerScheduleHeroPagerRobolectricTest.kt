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
import com.example.util.IndonesiaCitySpec
import com.example.util.PrayerTimeHelper
import com.example.util.WeatherConditionCategory
import com.example.util.WeatherForecastHelper
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    private fun sampleOpenMeteoJson(
        currentTemp: Double = 28.6,
        apparentTemp: Double = 31.2,
        humidity: Int = 78,
        windSpeed: Double = 11.4,
        rainProb: Int = 65,
        weatherCode: Int = 61,
        morningTemp: Double = 24.0,
        noonTemp: Double = 31.0,
        afternoonTemp: Double = 27.0,
        nightTemp: Double = 23.0
    ): String = """
        {
          "current": {
            "temperature_2m": $currentTemp,
            "relative_humidity_2m": $humidity,
            "apparent_temperature": $apparentTemp,
            "precipitation_probability": $rainProb,
            "weather_code": $weatherCode,
            "wind_speed_10m": $windSpeed,
            "is_day": 1
          },
          "hourly": {
            "time": [
              "2026-11-21T07:00",
              "2026-11-21T12:00",
              "2026-11-21T16:00",
              "2026-11-21T20:00"
            ],
            "temperature_2m": [$morningTemp, $noonTemp, $afternoonTemp, $nightTemp],
            "precipitation_probability": [20, 40, $rainProb, 30],
            "weather_code": [1, 2, $weatherCode, 3]
          },
          "daily": {
            "temperature_2m_max": [32.0],
            "temperature_2m_min": [22.5],
            "precipitation_probability_max": [$rainProb]
          }
        }
    """.trimIndent()

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
    fun `WeatherForecastHelper parses Open-Meteo JSON, maps WMO codes, and caches per city ID`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_weather_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        val kediri = PrayerTimeHelper.resolveCity("Kabupaten Kediri", null)
        val sunny = WeatherForecastHelper.describeWmoWeather(0, isDay = true)
        assertEquals(WeatherConditionCategory.SUNNY, sunny.category)
        assertEquals("Cerah", sunny.labelId)
        assertTrue(!sunny.isRainRisk)

        val storm = WeatherForecastHelper.describeWmoWeather(95, isDay = true)
        assertEquals(WeatherConditionCategory.THUNDERSTORM, storm.category)
        assertEquals("Hujan Petir", storm.labelId)
        assertTrue(storm.isRainRisk)

        val nowMs = 1_795_220_000_000L
        val parsed = WeatherForecastHelper.parseOpenMeteoJson(
            city = kediri,
            rawJson = sampleOpenMeteoJson(),
            fetchedAtMillis = nowMs
        )
        assertNotNull(parsed)
        assertEquals(29, parsed!!.currentTempC)
        assertEquals(31, parsed.apparentTempC)
        assertEquals(78, parsed.humidityPercent)
        assertEquals(11, parsed.windKmh)
        assertEquals(65, parsed.currentRainChancePercent)
        assertEquals("Hujan Ringan", parsed.currentDescriptor.labelId)
        assertEquals(4, parsed.periods.size)
        assertEquals(listOf("PAGI", "SIANG", "SORE", "MALAM"), parsed.periods.map { it.key })
        assertEquals(24, parsed.periods[0].tempC)
        assertEquals(31, parsed.periods[1].tempC)

        // Cache persistence & stale fallback
        WeatherForecastHelper.saveCachedWeatherJson(app, kediri.id, sampleOpenMeteoJson(currentTemp = 27.0), fetchedAtMillis = nowMs - WeatherForecastHelper.CACHE_TTL_MS - 60_000L)
        val freshOnly = WeatherForecastHelper.getCachedWeather(app, kediri, allowExpired = false, nowMillis = nowMs)
        assertNull(freshOnly)
        val staleAllowed = WeatherForecastHelper.getCachedWeather(app, kediri, allowExpired = true, nowMillis = nowMs)
        assertNotNull(staleAllowed)
        assertEquals(27, staleAllowed!!.currentTempC)
        assertTrue(staleAllowed.isFromCache)
    }

    @Test
    fun `DashboardTopHeroPager syncs shared city ID across Berobat, Jadwal Sholat, and Prakiraan Cuaca cards`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_prayer_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        app.getSharedPreferences("waliasuh_weather_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        var absenSholatClicked = false
        val registry = TourCoordinateRegistry()
        val fetchedCityIds = mutableListOf<String>()
        val fakeWeatherFetcher: suspend (IndonesiaCitySpec) -> String? = { city ->
            fetchedCityIds.add(city.id)
            if (city.id == "kota_makassar") {
                sampleOpenMeteoJson(currentTemp = 32.0, rainProb = 15, weatherCode = 0, noonTemp = 33.0)
            } else {
                sampleOpenMeteoJson(currentTemp = 28.0, rainProb = 65, weatherCode = 61)
            }
        }

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
                    onOpenAbsenSholat = { absenSholatClicked = true },
                    weatherFetcher = fakeWeatherFetcher
                )
            }
        }
        composeTestRule.waitForIdle()

        // Page 0: Berobat Hari Ini
        composeTestRule.onNodeWithTag("card_summary_today").assertIsDisplayed()
        composeTestRule.onNodeWithTag("row_faskes_strip").assertIsDisplayed()
        composeTestRule.onNodeWithTag("fab_dashboard_add").assertIsDisplayed()

        // Page 2: Prakiraan Cuaca (defaults to school city Kab. Kediri)
        composeTestRule.onNodeWithTag("hero_pager_tab_weather").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_weather_today").assertIsDisplayed()
        composeTestRule.onNodeWithTag("badge_weather_summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("28°C • Hujan Ringan • Siaga Hujan 65%").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_weather_pagi").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_weather_siang").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_weather_sore").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_weather_malam").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kab. Kediri").assertIsDisplayed()

        // Pick Kota Makassar from Weather card dropdown -> updates savedCityId and fetches Makassar weather
        composeTestRule.onNodeWithTag("btn_weather_city_dropdown").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("prayer_city_picker_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_search_prayer_city").performTextInput("Makassar")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("item_prayer_city_kota_makassar").performClick()
        composeTestRule.waitForIdle()

        assertEquals("kota_makassar", PrayerTimeHelper.getSavedCityId(app))
        composeTestRule.onNodeWithText("Makassar").assertIsDisplayed()
        composeTestRule.onNodeWithText("32°C • Cerah • Hujan 15%").assertIsDisplayed()
        assertTrue(fetchedCityIds.contains("kota_makassar"))

        // Switch to Page 1 (Jadwal Sholat) -> automatically synced to Kota Makassar (WITA)
        composeTestRule.onNodeWithTag("hero_pager_tab_prayer").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_prayer_times_today").assertIsDisplayed()
        composeTestRule.onNodeWithTag("badge_next_prayer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_prayer_subuh").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_prayer_dzuhur").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_prayer_ashar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_prayer_maghrib").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_prayer_isya").assertIsDisplayed()
        composeTestRule.onNodeWithText("Makassar").assertIsDisplayed()
        composeTestRule.onNodeWithText("WITA", substring = true).assertIsDisplayed()

        // Quick shortcut to Absen Sholat still works
        composeTestRule.onNodeWithTag("btn_prayer_card_open_absen").performClick()
        composeTestRule.waitForIdle()
        assertTrue(absenSholatClicked)
    }

    @Test
    fun `DashboardTopHeroPager weather card displays retry state when network fails and recovers on retry`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_prayer_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        app.getSharedPreferences("waliasuh_weather_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        var shouldFail = true
        val registry = TourCoordinateRegistry()
        val retryFetcher: suspend (IndonesiaCitySpec) -> String? = {
            if (shouldFail) null else sampleOpenMeteoJson(currentTemp = 30.0, rainProb = 10, weatherCode = 1)
        }

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                DashboardTopHeroPager(
                    todayShortFormatted = "21 Nov 2026",
                    waliName = "Eko Wahyudi",
                    currentWaliAccount = null,
                    schoolCityName = "Kabupaten Kediri",
                    sortedFaskesEntries = emptyList(),
                    customFaskesInpatients = emptyMap(),
                    totalToday = 0,
                    totalSchoolStudents = 343,
                    upcomingCount = 0,
                    progressRatio = 0f,
                    isEn = false,
                    tourRegistry = registry,
                    onNavigateToProfile = {},
                    onAddPlan = {},
                    onSelectInpatientRecord = {},
                    onOpenInpatientPicker = { _, _ -> },
                    onOpenAbsenSholat = {},
                    weatherFetcher = retryFetcher
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("hero_pager_tab_weather").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_weather_retry").assertIsDisplayed()

        shouldFail = false
        composeTestRule.onNodeWithTag("btn_weather_retry").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("30°C • Cerah Berawan • Hujan 10%").assertIsDisplayed()
        composeTestRule.onNodeWithTag("row_weather_periods_strip").assertIsDisplayed()
    }
}

