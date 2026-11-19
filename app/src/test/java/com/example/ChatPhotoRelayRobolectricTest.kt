package com.example

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChatMediaLocalSave
import com.example.data.model.ChildStudent
import com.example.data.model.ParentMessage
import com.example.data.model.ParentMessageHelper
import com.example.data.remote.pocketbase.PbParentMessageDto
import com.example.ui.HealthViewModel
import com.example.ui.components.ParentChatMessagesList
import com.example.ui.dialogs.PesanOrangTuaSheet
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ChatPhotoRelayRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `ParentMessageHelper auto-purge and PbParentMessageDto R2 roundtrip work cleanly`() {
        val now = 1_800_000_000_000L
        val activePhotoMsg = ParentMessage(
            id = 8801L,
            studentId = 101L,
            studentName = "Zaid Al-Farisi",
            parentName = "Ibu Fatimah",
            waliUsername = "ust_ahmad",
            waliName = "Ustadz Ahmad",
            senderRole = "WALI",
            senderName = "Ustadz Ahmad",
            messageContent = "Laporan kegiatan sore ananda",
            isReadByWali = true,
            isReadByParent = false,
            schoolId = "SRT1-KDR",
            createdAt = now,
            photoUrl = "https://waliasuhcareapp.areksurabaya.my.id/col_pm/rec8801/chat_8801.webp",
            photoFallbackUrl = "https://waliasuh.areksurabaya.my.id/api/files/col_pm/rec8801/chat_8801.webp",
            pbRecordId = "rec000000008801",
            pbCollectionId = "col_pm",
            pbFilename = "chat_8801.webp",
            isDownloadedByRecipient = false,
            isMediaPurgedFromServer = false,
            updatedAt = now
        )

        // Belum diunduh & baru dibuat -> tidak di-purge
        assertTrue(ParentMessageHelper.hasServerMedia(activePhotoMsg))
        assertTrue(ParentMessageHelper.isPhotoMessage(activePhotoMsg))
        assertFalse(ParentMessageHelper.shouldAutoPurgeServerMedia(activePhotoMsg, now))
        assertEquals("📷 Foto: Laporan kegiatan sore ananda", ParentMessageHelper.displayPreviewText(activePhotoMsg, false))

        // Tanpa caption -> preview "📷 Foto"
        val captionless = activePhotoMsg.copy(messageContent = "")
        assertEquals("📷 Foto", ParentMessageHelper.displayPreviewText(captionless, false))
        assertEquals("📷 Photo", ParentMessageHelper.displayPreviewText(captionless, true))

        // Penerima sudah mengunduh ke galeri HP -> siap auto-purge dari R2
        val downloadedByRecipient = activePhotoMsg.copy(isDownloadedByRecipient = true)
        assertTrue(ParentMessageHelper.shouldAutoPurgeServerMedia(downloadedByRecipient, now))

        // Kadaluarsa 14 hari meski belum diunduh -> siap auto-purge
        val expired = activePhotoMsg.copy(
            createdAt = now - (ParentMessageHelper.AUTO_PURGE_FALLBACK_DAYS + 1) * ParentMessageHelper.DAY_MILLIS
        )
        assertTrue(ParentMessageHelper.shouldAutoPurgeServerMedia(expired, now))

        // DTO roundtrip menjaga URL R2 + PB fallback
        val dto = PbParentMessageDto.fromDomain(activePhotoMsg)
        val restored = dto.toDomainModel("SRT1-KDR", "https://waliasuh.areksurabaya.my.id")
        assertEquals(activePhotoMsg.id, restored.id)
        assertEquals(activePhotoMsg.photoUrl, restored.photoUrl)
        assertEquals(activePhotoMsg.photoFallbackUrl, restored.photoFallbackUrl)
        assertEquals(activePhotoMsg.pbFilename, restored.pbFilename)

        // DTO purged mengosongkan URL server namun mempertahankan teks & status
        val purgedDto = PbParentMessageDto.fromDomain(activePhotoMsg.copy(isMediaPurgedFromServer = true))
        val purgedRestored = purgedDto.toDomainModel("SRT1-KDR", "https://waliasuh.areksurabaya.my.id")
        assertTrue(purgedRestored.isMediaPurgedFromServer)
        assertEquals("", purgedRestored.photoUrl)
        assertEquals("", purgedRestored.photoFallbackUrl)
        assertEquals("Laporan kegiatan sore ananda", purgedRestored.messageContent)
    }

    @Test
    fun `Room DAO v33 CRUD and ViewModel sendParentMessageWithPhoto + purge work cleanly`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val msgDao = inMemoryDb.parentMessageDao()
            val saveDao = inMemoryDb.chatMediaLocalSaveDao()

            val msg = ParentMessage(
                id = 9101L,
                studentId = 201L,
                studentName = "Aisyah",
                senderRole = "PARENT",
                senderName = "Ibu Aminah",
                messageContent = "Foto obat ananda",
                photoUrl = "https://waliasuhcareapp.areksurabaya.my.id/c/r/obat.webp",
                photoFallbackUrl = "https://waliasuh.areksurabaya.my.id/api/files/c/r/obat.webp",
                pbRecordId = "rec000000009101",
                pbCollectionId = "parent_messages",
                pbFilename = "obat.webp",
                schoolId = "SRT1-KDR",
                createdAt = 1_800_000_000_000L,
                updatedAt = 1_800_000_000_000L
            )
            msgDao.insert(msg)
            val loaded = msgDao.getById(9101L)
            assertNotNull(loaded)
            assertEquals("obat.webp", loaded?.pbFilename)

            val localSave = ChatMediaLocalSave(
                messageId = 9101L,
                localGalleryUri = "content://media/external/images/media/9101",
                displayFileName = "Chat_Ibu_Aminah_9101.webp",
                savedAt = 1_800_000_050_000L
            )
            saveDao.upsert(localSave)
            assertEquals("content://media/external/images/media/9101", saveDao.getByMessageId(9101L)?.localGalleryUri)

            saveDao.deleteByMessageId(9101L)
            assertNull(saveDao.getByMessageId(9101L))
        } finally {
            inMemoryDb.close()
        }

        // ViewModel sendParentMessageWithPhoto + purgeChatPhotoServerMediaNow
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = AppDatabase.getDatabase(app)
        val vm = HealthViewModel(app)
        val school = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
        val student = ChildStudent(
            id = 9601L,
            name = "Bilal Haidar",
            nis = "3199601",
            gender = "L",
            age = 13,
            dormitory = "Asrama Putra 1",
            classGrade = "VII-B",
            motherName = "Ibu Zahra",
            assignedWaliUsername = "ust_bilal",
            assignedWaliName = "Ustadz Bilal",
            schoolId = school
        )
        db.childStudentDao().insert(student)

        var sentOk = false
        vm.sendParentMessageWithPhoto(
            student = student,
            imageUri = Uri.parse("file:///tmp/test_chat_photo.webp"),
            captionText = "Foto belajar malam",
            senderRole = "WALI"
        ) { ok, _ ->
            sentOk = ok
        }
        var loops = 0
        while (loops < 30 && (!sentOk || vm.parentMessages.value.none { it.studentId == 9601L })) {
            ShadowLooper.idleMainLooper()
            delay(40)
            loops++
        }
        assertTrue(sentOk)
        val sentMsg = vm.parentMessages.value.firstOrNull { it.studentId == 9601L }
        assertNotNull(sentMsg)
        assertEquals("Foto belajar malam", sentMsg?.messageContent)
        assertTrue(sentMsg?.photoUrl?.contains("test_chat_photo") == true)

        var purgeOk = false
        vm.purgeChatPhotoServerMediaNow(sentMsg!!) { ok, _ ->
            purgeOk = ok
        }
        loops = 0
        while (loops < 25 && !purgeOk) {
            ShadowLooper.idleMainLooper()
            delay(40)
            loops++
        }
        assertTrue(purgeOk)
        val afterPurge = db.parentMessageDao().getById(sentMsg.id)
        assertTrue(afterPurge?.isMediaPurgedFromServer == true)
        assertEquals("", afterPurge?.photoUrl)

        db.parentMessageDao().deleteById(sentMsg.id)
        db.chatMediaLocalSaveDao().deleteByMessageId(sentMsg.id)
        db.childStudentDao().delete(student)
        Unit
    }

    @Test
    fun `PesanOrangTuaSheet shows photo preview, attach button, save button, and purged R2 notice`() {
        val student = ChildStudent(
            id = 301L,
            name = "Salma Nur",
            nis = "301",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri 1",
            classGrade = "VIII-A",
            motherName = "Ibu Mariam",
            assignedWaliUsername = "ustz_nisa",
            assignedWaliName = "Ustadzah Nisa",
            schoolId = "SRT1-KDR"
        )
        val now = System.currentTimeMillis()
        val incomingPhotoMsg = ParentMessage(
            id = 4101L,
            studentId = 301L,
            studentName = "Salma Nur",
            parentName = "Ibu Mariam",
            waliUsername = "ustz_nisa",
            waliName = "Ustadzah Nisa",
            senderRole = "PARENT",
            senderName = "Ibu Mariam",
            messageContent = "Resep vitamin dari rumah",
            photoUrl = "https://waliasuhcareapp.areksurabaya.my.id/c/r/resep.webp",
            isReadByWali = false,
            isReadByParent = true,
            schoolId = "SRT1-KDR",
            createdAt = now - 60_000L,
            updatedAt = now - 60_000L
        )
        val purgedOwnPhotoMsg = ParentMessage(
            id = 4102L,
            studentId = 301L,
            studentName = "Salma Nur",
            parentName = "Ibu Mariam",
            waliUsername = "ustz_nisa",
            waliName = "Ustadzah Nisa",
            senderRole = "WALI",
            senderName = "Ustadzah Nisa",
            messageContent = "Foto kegiatan kemarin",
            isReadByWali = true,
            isReadByParent = true,
            isDownloadedByRecipient = true,
            isMediaPurgedFromServer = true,
            schoolId = "SRT1-KDR",
            createdAt = now - 10_000L,
            updatedAt = now - 5_000L
        )

        var localSaves by mutableStateOf<Map<Long, ChatMediaLocalSave>>(emptyMap())
        var saveClicked = false

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAppStrings provides AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
            ) {
                PesanOrangTuaSheet(
                    students = listOf(student),
                    parentMessages = listOf(incomingPhotoMsg, purgedOwnPhotoMsg),
                    isStudentClaimedByCurrentWali = { true },
                    isAdmin = false,
                    onSendMessage = { _, _ -> },
                    onMarkRead = {},
                    localSavesByMessageId = localSaves,
                    autoSaveMediaEnabled = false,
                    onSendPhotoMessage = { _, _, _ -> },
                    onSavePhotoToGallery = { msg, _ ->
                        saveClicked = true
                        localSaves = mapOf(
                            msg.id to ChatMediaLocalSave(
                                messageId = msg.id,
                                localGalleryUri = "content://media/external/images/media/4101",
                                displayFileName = "Chat_Ibu_Mariam_4101.webp",
                                savedAt = now
                            )
                        )
                    },
                    onPurgeServerMedia = {},
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Conversation list renders photo preview label
        composeTestRule.onNodeWithText("Anda: 📷 Foto: Foto kegiatan kemarin").assertIsDisplayed()

        // Open thread
        composeTestRule.onNodeWithTag("card_chat_student_301").performClick()
        composeTestRule.waitForIdle()

        // Attach photo button & auto-save switch are displayed
        composeTestRule.onNodeWithTag("btn_attach_chat_photo_wali").assertIsDisplayed()
        composeTestRule.onNodeWithTag("switch_chat_auto_save_media").assertIsDisplayed()

        // Purged message notice is shown
        composeTestRule.onNodeWithTag("chat_photo_purged_4102").assertIsDisplayed()

        // Manual save button for incoming photo updates to saved badge
        composeTestRule.onNodeWithTag("btn_save_chat_photo_4101").performClick()
        composeTestRule.waitForIdle()
        assertTrue(saveClicked)
        composeTestRule.onNodeWithTag("badge_chat_photo_saved_4101").assertIsDisplayed()
    }

    @Test
    fun `ParentChatMessagesList shows save button and fullscreen preview on Parent side`() {
        val now = System.currentTimeMillis()
        val waliPhotoMsg = ParentMessage(
            id = 5201L,
            studentId = 101L,
            studentName = "Zaid Al-Farisi",
            senderRole = "WALI",
            senderName = "Ustadz Ahmad",
            messageContent = "Foto setoran tahfidz pagi ini",
            photoUrl = "https://waliasuhcareapp.areksurabaya.my.id/c/r/tahfidz.webp",
            createdAt = now,
            updatedAt = now
        )
        var localSaves by mutableStateOf<Map<Long, ChatMediaLocalSave>>(emptyMap())

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAppStrings provides AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
            ) {
                ParentChatMessagesList(
                    messages = listOf(waliPhotoMsg),
                    myRole = "PARENT",
                    isEn = false,
                    lang = AppLanguage.INDONESIAN,
                    emptyContent = {},
                    onDeleteMessage = {},
                    localSavesByMessageId = localSaves,
                    autoSaveMediaEnabled = false,
                    onSavePhotoToGallery = { msg, _ ->
                        localSaves = mapOf(
                            msg.id to ChatMediaLocalSave(
                                messageId = msg.id,
                                localGalleryUri = "content://media/external/images/media/5201",
                                displayFileName = "Chat_Ustadz_Ahmad_5201.webp",
                                savedAt = now
                            )
                        )
                    }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("chat_photo_5201").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_save_chat_photo_5201").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("badge_chat_photo_saved_5201").assertIsDisplayed()

        // Tap photo to open fullscreen preview dialog
        composeTestRule.onNodeWithTag("chat_photo_5201").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dialog_gallery_fullscreen_preview").assertIsDisplayed()
    }
}
