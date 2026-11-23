package com.example

import android.app.Application
import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.DashboardIconKey
import com.example.data.model.SchoolProfile
import com.example.data.model.SharedJournalHelper
import com.example.data.model.SharedJournalNote
import com.example.data.model.WaliAccount
import com.example.data.remote.pocketbase.PbSharedJournalNoteDto
import com.example.ui.HealthViewModel
import com.example.ui.components.QuickMenuTiles
import com.example.ui.dialogs.SharedJournalSheet
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
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
class SharedJournalRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `QuickMenuTiles hides Bible and shows Jurnal & Aturan tile while mapping legacy BIBLE layout`() {
        val normalized = DashboardIconKey.normalizeOrder(listOf("BEROBAT", "BIBLE", "KUNJUNGAN"))
        assertFalse(normalized.contains(DashboardIconKey.BIBLE))
        assertTrue(normalized.contains(DashboardIconKey.JURNAL_ATURAN))
        assertEquals(DashboardIconKey.JURNAL_ATURAN, normalized[1])

        val indonesianStrings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        var openedSharedJournal = false

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides indonesianStrings) {
                QuickMenuTiles(
                    onNavigateToPlan = {},
                    onNavigateToDirectTreatment = {},
                    onNavigateToRecords = {},
                    onNavigateToStudents = {},
                    onShowPuskesmasInfo = {},
                    onOpenSharedJournal = { openedSharedJournal = true },
                    sharedJournalCount = 3
                )
            }
        }
        composeTestRule.waitForIdle()

        // Pastikan menu Bible tidak lagi muncul pada dashboard utama
        composeTestRule.onNodeWithTag("tile_bible").assertDoesNotExist()

        // Gulir ke ikon Jurnal & Aturan dan pastikan ukurannya >= 48.dp serta bisa diklik
        composeTestRule.onNodeWithTag("quick_menu_carousel_row")
            .performScrollToNode(hasTestTag("tile_jurnal_aturan"))
        composeTestRule.onNodeWithTag("tile_jurnal_aturan")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(openedSharedJournal)
    }

    @Test
    fun `SharedJournalSheet creates card via plus button and opens detail dialog on card click`() {
        val indonesianStrings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)

        composeTestRule.setContent {
            var notesState by remember {
                mutableStateOf(
                    listOf(
                        SharedJournalNote(
                            id = 101L,
                            schoolId = "SRT1-KDR",
                            category = SharedJournalHelper.CATEGORY_ATURAN,
                            title = "Aturan Jam Malam Asrama",
                            content = "Seluruh siswa wajib sudah berada di kamar masing-masing pukul 21.30 WIB.",
                            isPinned = true,
                            authorUsername = "wali_eko",
                            authorName = "Eko Wahyudi",
                            createdAt = 1700000000000L,
                            updatedAt = 1700000000000L
                        )
                    )
                )
            }

            CompositionLocalProvider(LocalAppStrings provides indonesianStrings) {
                SharedJournalSheet(
                    notes = notesState,
                    currentUsername = "wali_eko",
                    currentWaliName = "Eko Wahyudi",
                    isAdmin = false,
                    isAnakSuperAdmin = false,
                    onSaveNote = { existing, category, title, content, isPinned, onResult ->
                        val nextId = existing?.id ?: (200L + notesState.size)
                        val saved = SharedJournalNote(
                            id = nextId,
                            schoolId = "SRT1-KDR",
                            category = category,
                            title = title,
                            content = content,
                            isPinned = isPinned,
                            authorUsername = "wali_eko",
                            authorName = "Eko Wahyudi",
                            createdAt = 1700000005000L,
                            updatedAt = 1700000005000L
                        )
                        notesState = notesState.filterNot { it.id == nextId } + saved
                        onResult(true, "Tersimpan")
                    },
                    onTogglePin = { target, onResult ->
                        notesState = notesState.map {
                            if (it.id == target.id) it.copy(isPinned = !it.isPinned) else it
                        }
                        onResult(true, "Pin diperbarui")
                    },
                    onDeleteNote = { target, onResult ->
                        notesState = notesState.filterNot { it.id == target.id }
                        onResult(true, "Terhapus")
                    },
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Kartu awal muncul dengan rapi; detail belum terbuka sebelum kartu diklik
        composeTestRule.onNodeWithTag("card_shared_journal_101").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dialog_shared_journal_detail").assertDoesNotExist()

        // Klik kartu untuk membuka dialog detail
        composeTestRule.onNodeWithTag("card_shared_journal_101").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dialog_shared_journal_detail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Seluruh siswa wajib sudah berada di kamar masing-masing pukul 21.30 WIB.")
            .assertIsDisplayed()

        // Tutup detail
        composeTestRule.onNodeWithTag("btn_close_detail_shared_journal").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dialog_shared_journal_detail").assertDoesNotExist()

        // Tambah catatan baru lewat tombol +
        composeTestRule.onNodeWithTag("btn_add_shared_journal")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dialog_shared_journal_form").assertIsDisplayed()

        composeTestRule.onNodeWithTag("btn_category_jurnal").performClick()
        composeTestRule.onNodeWithTag("input_shared_journal_title")
            .performTextReplacement("Jurnal Pendampingan Sore")
        composeTestRule.onNodeWithTag("input_shared_journal_content")
            .performTextReplacement("Evaluasi kebersihan kamar putra berjalan tertib hari ini.")
        composeTestRule.onNodeWithTag("btn_save_shared_journal").performClick()
        composeTestRule.waitForIdle()

        // Kartu baru muncul pada daftar
        composeTestRule.onNodeWithTag("card_shared_journal_201").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jurnal Pendampingan Sore").assertIsDisplayed()
    }

    @Test
    fun `SharedJournalNote persists in Room, enforces school & author permissions, and round-trips PocketBase DTO`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        val db = AppDatabase.getDatabase(app)
        val dao = db.sharedJournalNoteDao()
        dao.deleteBySchool("SCH-JRN-A")
        dao.deleteBySchool("SCH-JRN-B")

        db.waliAccountDao().insert(
            WaliAccount(
                name = "Wali Penulis",
                username = "wali_penulis",
                password = "Password1!",
                dormitory = "Putra",
                schoolId = "SCH-JRN-A"
            )
        )
        db.waliAccountDao().insert(
            WaliAccount(
                name = "Wali Lain",
                username = "wali_lain",
                password = "Password1!",
                dormitory = "Putri",
                schoolId = "SCH-JRN-A"
            )
        )

        val vm = HealthViewModel(app)
        vm.updateSchoolProfile(
            SchoolProfile(
                schoolName = "SRT Journal Test",
                shortName = "SRT-JRN",
                tenantCode = "SCH-JRN-A"
            )
        )
        vm.executeLogin("wali_penulis", "Password1!", rememberMe = false)
        ShadowLooper.idleMainLooper()

        var saveSuccess = false
        vm.saveSharedJournalNote(
            category = SharedJournalHelper.CATEGORY_ATURAN,
            title = "Aturan Piket Dapur",
            content = "Piket dilakukan bergilir setiap pagi setelah apel.",
            isPinned = true
        ) { ok, _ -> saveSuccess = ok }
        delay(200)
        ShadowLooper.idleMainLooper()
        assertTrue(saveSuccess)

        val savedInA = dao.getNotesBySchoolList("SCH-JRN-A")
        assertEquals(1, savedInA.size)
        val note = savedInA.first()
        assertEquals(SharedJournalHelper.CATEGORY_ATURAN, note.category)
        assertTrue(note.isPinned)
        assertEquals("wali_penulis", note.authorUsername)

        // Multi-tenant isolation: sekolah lain kosong
        assertEquals(0, dao.getNotesBySchoolList("SCH-JRN-B").size)

        // Izin kelola: penulis & admin boleh ubah/hapus, wali lain tidak boleh
        assertTrue(
            SharedJournalHelper.canManageNote(
                note = note,
                currentUsername = "wali_penulis",
                currentWaliName = "Wali Penulis",
                isAdmin = false,
                isAnakSuperAdmin = false
            )
        )
        assertTrue(
            SharedJournalHelper.canManageNote(
                note = note,
                currentUsername = "admin1",
                currentWaliName = "Admin",
                isAdmin = true,
                isAnakSuperAdmin = false
            )
        )
        assertFalse(
            SharedJournalHelper.canManageNote(
                note = note,
                currentUsername = "wali_lain",
                currentWaliName = "Wali Lain",
                isAdmin = false,
                isAnakSuperAdmin = false
            )
        )

        // PocketBase DTO round-trip
        val dto = PbSharedJournalNoteDto.fromDomain(note)
        val restored = dto.toDomainModel("SCH-JRN-A")
        assertEquals(note.id, restored.id)
        assertEquals(note.title, restored.title)
        assertEquals(note.content, restored.content)
        assertEquals(note.category, restored.category)
        assertEquals(note.isPinned, restored.isPinned)
        assertEquals(note.authorUsername, restored.authorUsername)
    }
}
