package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.ParentAccessLink
import com.example.data.model.QuranArabicFontChoice
import com.example.data.model.QuranCatalog
import com.example.data.model.QuranReaderTheme
import com.example.data.model.SchoolProfile
import com.example.data.model.StudentReligionHelper
import com.example.ui.HealthViewModel
import com.example.ui.LoginResult
import com.example.ui.components.CategoryHubOverlay
import com.example.ui.components.DashboardCategoryHub
import com.example.ui.dialogs.QuranReaderSheet
import com.example.ui.screens.ParentPortalScreen
import com.example.util.QuranContentRepository
import com.example.util.QuranOfflineData
import com.example.util.QuranUserPrefsStore
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
class QuranDigitalRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `114 surah catalog and built-in offline Juz Amma data are complete and consistent`() {
        assertEquals(114, QuranCatalog.ALL_SURAHS.size)
        assertEquals((1..114).toList(), QuranCatalog.ALL_SURAHS.map { it.number })
        QuranCatalog.ALL_SURAHS.forEach { surah ->
            assertTrue("Arabic name blank on ${surah.number}", surah.nameArabic.isNotBlank())
            assertTrue("Latin name blank on ${surah.number}", surah.nameLatin.isNotBlank())
            assertTrue("Verse count invalid on ${surah.number}", surah.versesCount > 0)
        }

        val offlineSurahIds = listOf(1) + (93..114).toList()
        offlineSurahIds.forEach { num ->
            val spec = QuranCatalog.findSurah(num)
            val ayahs = QuranOfflineData.getOfflineSurah(num)
            assertTrue("Missing offline data for surah $num", !ayahs.isNullOrEmpty())
            assertEquals("Verse count mismatch for surah $num", spec.versesCount, ayahs!!.size)
            ayahs.forEach { a ->
                assertTrue(a.arabicText.isNotBlank())
                assertTrue(a.latinText.isNotBlank())
                assertTrue(a.translationId.isNotBlank())
                assertTrue(a.translationEn.isNotBlank())
            }
        }
    }

    @Test
    fun `QuranUserPrefsStore persists last read, bookmarks, notes, Ivory Gold default, and typography`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_quran_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        val initial = QuranUserPrefsStore.load(app)
        assertEquals(QuranReaderTheme.IVORY_GOLD, initial.theme)
        assertEquals(QuranArabicFontChoice.SCHEHERAZADE, initial.fontChoice)

        val savedLast = QuranUserPrefsStore.saveLastRead(app, 112, 3)
        assertEquals(112, savedLast.surahNumber)
        assertEquals(3, savedLast.ayahNumber)

        val (afterAddBm, added) = QuranUserPrefsStore.toggleBookmark(
            context = app,
            currentList = initial.bookmarks,
            surahNumber = 112,
            ayahNumber = 1,
            arabicPreview = "قُلْ هُوَ اللّٰهُ اَحَدٌۚ",
            translationId = "Katakanlah: Dialah Allah, Yang Maha Esa."
        )
        assertTrue(added)
        assertEquals(1, afterAddBm.size)

        val afterNote = QuranUserPrefsStore.saveNote(
            context = app,
            currentList = initial.notes,
            existingId = null,
            surahNumber = 112,
            ayahNumber = 1,
            title = "Target Setoran Al-Ikhlas",
            content = "Perhatikan qalqalah kubra di akhir ayat.",
            categoryTag = "TAJWID"
        )
        assertEquals(1, afterNote.size)

        QuranUserPrefsStore.saveSettings(
            app,
            initial.copy(
                theme = QuranReaderTheme.AMBER_SEPIA,
                fontChoice = QuranArabicFontChoice.AMIRI,
                arabicFontSizeSp = 32
            )
        )

        val reloaded = QuranUserPrefsStore.load(app)
        assertEquals(112, reloaded.lastRead.surahNumber)
        assertEquals(3, reloaded.lastRead.ayahNumber)
        assertEquals(1, reloaded.bookmarks.size)
        assertEquals(1, reloaded.notes.size)
        assertEquals(QuranReaderTheme.AMBER_SEPIA, reloaded.theme)
        assertEquals(QuranArabicFontChoice.AMIRI, reloaded.fontChoice)
        assertEquals(32, reloaded.arabicFontSizeSp)
    }

    @Test
    fun `CategoryHubOverlay MENGAJI exposes Al-Quran Digital tile and triggers callback`() {
        var quranOpened = false
        composeTestRule.setContent {
            CategoryHubOverlay(
                category = DashboardCategoryHub.MENGAJI,
                schoolProfile = SchoolProfile(),
                activePlans = emptyList(),
                inpatientCount = 0,
                fastingSessions = emptyList(),
                fastingRecords = emptyList(),
                activeHaidRecords = emptyList(),
                suciRecords = emptyList(),
                shiftMessages = emptyList(),
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
                onOpenQuranFromMengaji = { quranOpened = true }
            )
        }

        composeTestRule.onNodeWithTag("tile_alquran_digital").assertIsDisplayed().performClick()
        assertTrue(quranOpened)
    }

    @Test
    fun `QuranReaderSheet renders Mushaf frame, loads offline surah, switches surahs, and opens notes & doa tabs`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_quran_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        QuranContentRepository.clearMemoryCacheForTesting()

        composeTestRule.setContent {
            QuranReaderSheet(
                initialEnglish = false,
                onRecordToMengaji = null,
                onDismiss = {}
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("quran_reader_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("quran_ayah_row_1").assertIsDisplayed()

        // Buka pengaturan tipografi & tema
        composeTestRule.onNodeWithTag("btn_quran_typography").performClick()
        composeTestRule.onNodeWithText("Malam Emas").assertIsDisplayed()

        // Pindah ke tab 114 Surah, cari 112 (Al-Ikhlas), dan buka
        composeTestRule.onNodeWithTag("quran_nav_surah").performClick()
        composeTestRule.onNodeWithTag("quran_surah_search_input").performTextInput("112")
        composeTestRule.onNodeWithTag("quran_surah_row_112").performClick()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("112. Al-Ikhlāṣ").assertIsDisplayed()

        // Buka tab Doa & Cari
        composeTestRule.onNodeWithTag("quran_nav_doa_cari").performClick()
        composeTestRule.onNodeWithTag("btn_quran_download_offline_pack").assertIsDisplayed()
    }

    @Test
    fun `ParentPortalScreen shows Al-Quran Digital only for Muslim students and hides for Christian students`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        val db = AppDatabase.getDatabase(app)
        val vm = HealthViewModel(app)
        val schoolId = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

        val muslimStudent = ChildStudent(
            id = 9901L,
            name = "Zaid Al-Ghifari",
            nis = "990101",
            gender = "L",
            age = 13,
            dormitory = "Asrama B",
            classGrade = "VII-B",
            schoolId = schoolId,
            religion = ""
        )
        val christianStudent = ChildStudent(
            id = 9902L,
            name = "Grace Natalia",
            nis = "990201",
            gender = "P",
            age = 13,
            dormitory = "Asrama C",
            classGrade = "VII-B",
            schoolId = schoolId,
            religion = StudentReligionHelper.RELIGION_KRISTEN
        )
        db.childStudentDao().insert(muslimStudent)
        db.childStudentDao().insert(christianStudent)

        val linkMuslim = ParentAccessLink(
            id = 9911L,
            studentId = 9901L,
            studentName = muslimStudent.name,
            studentNis = muslimStudent.nis,
            parentName = "Ibu Aminah",
            username = "ortu_zaid",
            password = "Pass#123",
            schoolId = schoolId,
            canViewMengaji = true
        )
        val linkChristian = ParentAccessLink(
            id = 9912L,
            studentId = 9902L,
            studentName = christianStudent.name,
            studentNis = christianStudent.nis,
            parentName = "Ibu Marta",
            username = "ortu_grace",
            password = "Pass#123",
            schoolId = schoolId,
            canViewMengaji = false
        )
        db.parentAccessLinkDao().insert(linkMuslim)
        db.parentAccessLinkDao().insert(linkChristian)

        val loginRes = vm.executeLogin(linkMuslim.username, linkMuslim.password)
        assertTrue(loginRes is LoginResult.SuccessParent)
        var loops = 0
        while (loops < 30 && vm.activeParentAccessLink.value == null) {
            ShadowLooper.idleMainLooper()
            delay(30)
            loops++
        }

        composeTestRule.setContent {
            ParentPortalScreen(
                viewModel = vm,
                onLogout = {}
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("scroll_parent_home")
            .performScrollToNode(hasTestTag("card_parent_summary_alquran"))
        composeTestRule.onNodeWithTag("card_parent_summary_alquran").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("quran_reader_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_quran_close").performClick()

        // Verifikasi bahwa siswa Kristen tidak menampilkan tile Al-Qur'an
        val christianLoginRes = vm.executeLogin(linkChristian.username, linkChristian.password)
        assertTrue(christianLoginRes is LoginResult.SuccessParent)
        loops = 0
        while (loops < 30 && vm.activeParentAccessLink.value?.studentId != 9902L) {
            ShadowLooper.idleMainLooper()
            delay(30)
            loops++
        }
        composeTestRule.waitForIdle()
        assertFalse(
            composeTestRule.onAllNodesWithTag("card_parent_summary_alquran").fetchSemanticsNodes().isNotEmpty()
        )
        vm.logout()
        db.parentAccessLinkDao().delete(linkMuslim)
        db.parentAccessLinkDao().delete(linkChristian)
        db.childStudentDao().delete(muslimStudent)
        db.childStudentDao().delete(christianStudent)
        Unit
    }
}
