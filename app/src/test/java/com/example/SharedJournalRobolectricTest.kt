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
import com.example.data.model.JournalChecklistItem
import com.example.data.model.JournalImageAttachment
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
    fun `SharedJournalSheet opens full screen editor with checklist support and saves new card cleanly`() {
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
                    onSaveNote = { existing, category, title, content, checklistJson, imageUrlsJson, isPinned, _, onResult ->
                        val nextId = existing?.id ?: (200L + notesState.size)
                        val saved = SharedJournalNote(
                            id = nextId,
                            schoolId = "SRT1-KDR",
                            category = category,
                            title = title,
                            content = content,
                            checklistJson = checklistJson,
                            imageUrlsJson = imageUrlsJson,
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

        // Daftar full-screen muncul dan editor belum aktif
        composeTestRule.onNodeWithTag("screen_shared_journal_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_shared_journal_101").assertIsDisplayed()
        composeTestRule.onNodeWithTag("screen_shared_journal_editor").assertDoesNotExist()

        // Klik kartu untuk membuka tampilan layar penuh catatan
        composeTestRule.onNodeWithTag("card_shared_journal_101").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("screen_shared_journal_editor").assertIsDisplayed()
        composeTestRule.onNodeWithText("Seluruh siswa wajib sudah berada di kamar masing-masing pukul 21.30 WIB.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_toolbar_checklist").assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("btn_toolbar_image").assertHeightIsAtLeast(48.dp)

        // Kembali ke daftar catatan
        composeTestRule.onNodeWithTag("btn_back_shared_journal_editor").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("screen_shared_journal_list").assertIsDisplayed()

        // Tambah catatan baru dengan checklist melalui tombol FAB +
        composeTestRule.onNodeWithTag("btn_add_shared_journal")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("screen_shared_journal_editor").assertIsDisplayed()

        composeTestRule.onNodeWithTag("btn_category_jurnal").performClick()
        composeTestRule.onNodeWithTag("input_shared_journal_title")
            .performTextReplacement("Persiapan Apel Pagi")
        composeTestRule.onNodeWithTag("btn_toolbar_checklist").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("checklist_input_0")
            .performTextReplacement("Cek seragam rapi seluruh anak asuh")
        composeTestRule.onNodeWithTag("checklist_checkbox_0").performClick()
        composeTestRule.onNodeWithTag("btn_save_shared_journal").performClick()
        composeTestRule.waitForIdle()

        // Kembali ke daftar & kartu baru menampilkan judul + ringkasan progres checklist
        composeTestRule.onNodeWithTag("screen_shared_journal_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_shared_journal_201").assertIsDisplayed()
        composeTestRule.onNodeWithText("Persiapan Apel Pagi").assertIsDisplayed()
        composeTestRule.onNodeWithText("☑ 1/1 selesai • Cek seragam rapi seluruh anak asuh")
            .assertIsDisplayed()
    }

    @Test
    fun `SharedJournalSheet enforces read-only view for non-author wali`() {
        val indonesianStrings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides indonesianStrings) {
                SharedJournalSheet(
                    notes = listOf(
                        SharedJournalNote(
                            id = 301L,
                            schoolId = "SRT1-KDR",
                            category = SharedJournalHelper.CATEGORY_ATURAN,
                            title = "Aturan Pemakaian Air",
                            content = "Matikan keran setelah digunakan.",
                            authorUsername = "wali_eko",
                            authorName = "Eko Wahyudi"
                        )
                    ),
                    currentUsername = "wali_budi",
                    currentWaliName = "Budi Santoso",
                    isAdmin = false,
                    isAnakSuperAdmin = false,
                    onSaveNote = { _, _, _, _, _, _, _, _, _ -> },
                    onTogglePin = { _, _ -> },
                    onDeleteNote = { _, _ -> },
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_shared_journal_301").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("screen_shared_journal_editor").assertIsDisplayed()
        composeTestRule.onNodeWithTag("badge_journal_read_only").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_save_shared_journal").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_delete_shared_journal").assertDoesNotExist()
    }

    @Test
    fun `SharedJournalNote persists checklist & images in Room, enforces permissions, and round-trips PocketBase DTO`() = runBlocking {
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

        val checklistJson = SharedJournalHelper.serializeChecklist(
            listOf(
                JournalChecklistItem(id = "c1", text = "Kunci pintu dapur", isChecked = false),
                JournalChecklistItem(id = "c2", text = "Matikan kompor", isChecked = true)
            )
        )
        val imageUrlsJson = SharedJournalHelper.serializeImages(
            listOf(
                JournalImageAttachment(
                    id = "img1",
                    url = "https://cdn.example.com/jrn/test.webp",
                    fallbackUrl = "https://pb.example.com/api/files/shared_journal_notes/rec/test.webp"
                )
            )
        )

        var saveSuccess = false
        vm.saveSharedJournalNote(
            category = SharedJournalHelper.CATEGORY_ATURAN,
            title = "Aturan Piket Dapur",
            content = "Piket dilakukan bergilir setiap pagi setelah apel.",
            checklistJson = checklistJson,
            imageUrlsJson = imageUrlsJson,
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
        assertEquals(2, SharedJournalHelper.parseChecklist(note.checklistJson).size)
        assertEquals(1, SharedJournalHelper.parseImages(note.imageUrlsJson).size)

        // Toggle item checklist via ViewModel
        var toggleOk = false
        vm.toggleSharedJournalChecklistItem(note, "c1") { ok, _ -> toggleOk = ok }
        delay(150)
        ShadowLooper.idleMainLooper()
        assertTrue(toggleOk)
        val updatedChecklist = SharedJournalHelper.parseChecklist(
            dao.getNotesBySchoolList("SCH-JRN-A").first().checklistJson
        )
        assertTrue(updatedChecklist.first { it.id == "c1" }.isChecked)

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

        // PocketBase DTO round-trip including checklistJson & imageUrlsJson
        val latestNote = dao.getNotesBySchoolList("SCH-JRN-A").first()
        val dto = PbSharedJournalNoteDto.fromDomain(latestNote)
        val restored = dto.toDomainModel("SCH-JRN-A")
        assertEquals(latestNote.id, restored.id)
        assertEquals(latestNote.title, restored.title)
        assertEquals(latestNote.content, restored.content)
        assertEquals(latestNote.checklistJson, restored.checklistJson)
        assertEquals(latestNote.imageUrlsJson, restored.imageUrlsJson)
        assertEquals(latestNote.category, restored.category)
        assertEquals(latestNote.isPinned, restored.isPinned)
        assertEquals(latestNote.authorUsername, restored.authorUsername)
    }
}
