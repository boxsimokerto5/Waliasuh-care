package com.example

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.ParentAccessLink
import com.example.data.model.WaliGalleryHelper
import com.example.data.model.WaliGalleryLocalSave
import com.example.data.model.WaliGalleryPost
import com.example.data.remote.pocketbase.PbWaliGalleryPostDto
import com.example.ui.dialogs.CreateWaliGalleryPostDialog
import com.example.ui.dialogs.WaliGallerySheet
import com.example.ui.screens.ParentGalleryContent
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WaliGalleryRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `WaliGalleryHelper filters by matching Wali Asuh, school, and target scope`() {
        val studentA = ChildStudent(
            id = 101L,
            name = "Zaid Al-Farisi",
            nis = "101",
            gender = "L",
            age = 13,
            dormitory = "Asrama Putra 1",
            classGrade = "VII-A",
            assignedWaliUsername = "ust_ahmad",
            assignedWaliName = "Ustadz Ahmad",
            schoolId = "SRT1-KDR"
        )
        val studentB = studentA.copy(
            id = 102L,
            name = "Hasan Basri",
            nis = "102"
        )
        val linkA = ParentAccessLink(
            id = 501L,
            studentId = 101L,
            studentName = "Zaid Al-Farisi",
            username = "ortu_zaid",
            password = "pass",
            assignedWaliUsername = "ust_ahmad",
            assignedWaliName = "Ustadz Ahmad",
            schoolId = "SRT1-KDR"
        )

        val now = 1_800_000_000_000L
        val allPostSameWali = WaliGalleryPost(
            id = 1L,
            schoolId = "SRT1-KDR",
            waliUsername = "ust_ahmad",
            waliName = "Ustadz Ahmad",
            caption = "Kerja bakti asrama pagi ini",
            targetScope = WaliGalleryHelper.SCOPE_ALL_MY_STUDENTS,
            expiresAt = now + 7 * WaliGalleryHelper.DAY_MILLIS,
            createdAt = now,
            updatedAt = now
        )
        val postDifferentWali = allPostSameWali.copy(
            id = 2L,
            waliUsername = "ust_budi",
            waliName = "Ustadz Budi"
        )
        val postDifferentSchool = allPostSameWali.copy(
            id = 3L,
            schoolId = "SRT2-SBY"
        )
        val specificPostForStudentB = allPostSameWali.copy(
            id = 4L,
            targetScope = WaliGalleryHelper.SCOPE_SPECIFIC_STUDENTS,
            targetStudentIdsCsv = "102"
        )
        val specificPostForStudentA = allPostSameWali.copy(
            id = 5L,
            targetScope = WaliGalleryHelper.SCOPE_SPECIFIC_STUDENTS,
            targetStudentIdsCsv = "101,102"
        )

        assertTrue(
            WaliGalleryHelper.matchesParentStudent(
                post = allPostSameWali,
                student = studentA,
                link = linkA,
                assignedWaliUsername = "ust_ahmad",
                assignedWaliName = "Ustadz Ahmad"
            )
        )
        assertFalse(
            WaliGalleryHelper.matchesParentStudent(
                post = postDifferentWali,
                student = studentA,
                link = linkA,
                assignedWaliUsername = "ust_ahmad",
                assignedWaliName = "Ustadz Ahmad"
            )
        )
        assertFalse(
            WaliGalleryHelper.matchesParentStudent(
                post = postDifferentSchool,
                student = studentA,
                link = linkA,
                assignedWaliUsername = "ust_ahmad",
                assignedWaliName = "Ustadz Ahmad"
            )
        )
        assertFalse(
            WaliGalleryHelper.matchesParentStudent(
                post = specificPostForStudentB,
                student = studentA,
                link = linkA,
                assignedWaliUsername = "ust_ahmad",
                assignedWaliName = "Ustadz Ahmad"
            )
        )
        assertTrue(
            WaliGalleryHelper.matchesParentStudent(
                post = specificPostForStudentA,
                student = studentA,
                link = linkA,
                assignedWaliUsername = "ust_ahmad",
                assignedWaliName = "Ustadz Ahmad"
            )
        )
        assertTrue(
            WaliGalleryHelper.matchesParentStudent(
                post = allPostSameWali.copy(waliUsername = "", waliName = "Ustadz Ahmad"),
                student = studentB,
                link = null,
                assignedWaliUsername = "",
                assignedWaliName = "ustadz ahmad"
            )
        )
    }

    @Test
    fun `WaliGalleryHelper WhatsApp relay auto-purge triggers on full delivery or expiry`() {
        val now = 1_800_000_000_000L
        var post = WaliGalleryPost(
            id = 88L,
            schoolId = "SRT1-KDR",
            waliUsername = "ust_ahmad",
            waliName = "Ustadz Ahmad",
            caption = "Setoran hafalan",
            photoUrl = "https://waliasuhcareapp.areksurabaya.my.id/col_gal/rec88/f.webp",
            retentionDays = 14,
            expiresAt = now + 14 * WaliGalleryHelper.DAY_MILLIS,
            expectedRecipientCount = 2,
            downloadedByCsv = "",
            isMediaPurgedFromServer = false,
            createdAt = now,
            updatedAt = now
        )

        assertFalse(WaliGalleryHelper.shouldAutoPurgeServerMedia(post, now))

        // Parent 1 downloads (deduped if repeated)
        val key1 = WaliGalleryHelper.buildParentRecipientKey(101L, "ortu_101")
        val csv1 = WaliGalleryHelper.mergeDownloadedRecipient(post.downloadedByCsv, key1)
        val csv1Repeat = WaliGalleryHelper.mergeDownloadedRecipient(csv1, key1)
        assertEquals(csv1, csv1Repeat)
        post = post.copy(downloadedByCsv = csv1)
        assertEquals(1, WaliGalleryHelper.downloadedCount(post))
        assertFalse(WaliGalleryHelper.shouldAutoPurgeServerMedia(post, now))

        // Parent 2 downloads -> reaches expectedRecipientCount = 2 -> ready to purge from server/R2
        val key2 = WaliGalleryHelper.buildParentRecipientKey(102L, "ortu_102")
        post = post.copy(downloadedByCsv = WaliGalleryHelper.mergeDownloadedRecipient(post.downloadedByCsv, key2))
        assertEquals(2, WaliGalleryHelper.downloadedCount(post))
        assertTrue(WaliGalleryHelper.shouldAutoPurgeServerMedia(post, now))

        // Expired post also triggers auto-purge even if 0 parents downloaded
        val expiredPost = post.copy(
            downloadedByCsv = "",
            expiresAt = now - 1000L
        )
        assertTrue(WaliGalleryHelper.shouldAutoPurgeServerMedia(expiredPost, now))
    }

    @Test
    fun `Room DAO persistence and PocketBase R2 DTO roundtrip preserve WaliGalleryPost`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val postDao = db.waliGalleryPostDao()
            val localSaveDao = db.waliGalleryLocalSaveDao()

            val post = WaliGalleryPost(
                id = 7701L,
                schoolId = "SRT1-KDR",
                waliUsername = "ust_ahmad",
                waliName = "Ustadz Ahmad",
                caption = "Olahraga pagi bersama binaan",
                activityTag = WaliGalleryHelper.TAG_KEBERSAMAAN,
                targetScope = WaliGalleryHelper.SCOPE_ALL_MY_STUDENTS,
                photoUrl = "https://waliasuhcareapp.areksurabaya.my.id/col_gal/rec123456789012/foto.webp",
                photoFallbackUrl = "https://waliasuh.areksurabaya.my.id/api/files/col_gal/rec123456789012/foto.webp",
                pbRecordId = "rec123456789012",
                pbCollectionId = "col_gal",
                pbFilename = "foto.webp",
                retentionDays = 14,
                expiresAt = 1_801_209_600_000L,
                expectedRecipientCount = 3,
                downloadedByCsv = "ortu_101",
                isMediaPurgedFromServer = false,
                createdAt = 1_800_000_000_000L,
                updatedAt = 1_800_000_100_000L
            )
            postDao.insert(post)
            val loaded = postDao.getById(7701L)
            assertNotNull(loaded)
            assertEquals("ust_ahmad", loaded?.waliUsername)
            assertEquals(WaliGalleryHelper.TAG_KEBERSAMAAN, loaded?.activityTag)

            val localSave = WaliGalleryLocalSave(
                postId = 7701L,
                localGalleryUri = "content://media/external/images/media/999",
                displayFileName = "WaliAsuhCare_7701.webp",
                savedAt = 1_800_000_200_000L
            )
            localSaveDao.upsert(localSave)
            assertEquals("content://media/external/images/media/999", localSaveDao.getByPostId(7701L)?.localGalleryUri)

            // PocketBase DTO roundtrip with R2 + PB fallback URL resolution
            val dto = PbWaliGalleryPostDto.fromDomain(post)
            val restored = dto.toDomainModel("SRT1-KDR", "https://waliasuh.areksurabaya.my.id")
            assertEquals(post.id, restored.id)
            assertEquals(post.waliUsername, restored.waliUsername)
            assertEquals(post.photoUrl, restored.photoUrl)
            assertEquals(post.photoFallbackUrl, restored.photoFallbackUrl)
            assertEquals(post.downloadedByCsv, restored.downloadedByCsv)

            // Purged server DTO clears media URLs while keeping caption/metadata
            val purgedDto = PbWaliGalleryPostDto.fromDomain(post.copy(isMediaPurgedFromServer = true))
            val purgedRestored = purgedDto.toDomainModel("SRT1-KDR", "https://waliasuh.areksurabaya.my.id")
            assertTrue(purgedRestored.isMediaPurgedFromServer)
            assertEquals("", purgedRestored.photoUrl)
            assertEquals("", purgedRestored.photoFallbackUrl)

            localSaveDao.deleteByPostId(7701L)
            assertNull(localSaveDao.getByPostId(7701L))
        } finally {
            db.close()
        }
    }

    @Test
    fun `WaliGallerySheet renders posts and opens compose dialog`() {
        val students = listOf(
            ChildStudent(
                id = 101L,
                name = "Zaid Al-Farisi",
                nis = "101",
                gender = "L",
                age = 13,
                dormitory = "Asrama Putra 1",
                classGrade = "VII-A",
                assignedWaliUsername = "ust_ahmad",
                assignedWaliName = "Ustadz Ahmad",
                schoolId = "SRT1-KDR"
            )
        )
        val post = WaliGalleryPost(
            id = 5001L,
            schoolId = "SRT1-KDR",
            waliUsername = "ust_ahmad",
            waliName = "Ustadz Ahmad",
            caption = "Belajar kelompok sore di masjid",
            activityTag = WaliGalleryHelper.TAG_IBADAH,
            expectedRecipientCount = 4,
            downloadedByCsv = "ortu_101",
            expiresAt = System.currentTimeMillis() + 5 * WaliGalleryHelper.DAY_MILLIS,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        var purgeCalled = false

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAppStrings provides AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
            ) {
                WaliGallerySheet(
                    posts = listOf(post),
                    myStudents = students,
                    currentWaliUsername = "ust_ahmad",
                    currentWaliName = "Ustadz Ahmad",
                    isAdminOrSuper = false,
                    isPublishing = false,
                    onPublishPost = { _, _, _, _, _, _, _ -> },
                    onPurgeServerMedia = { _, _ -> purgeCalled = true },
                    onDeletePost = { _, _ -> },
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("sheet_wali_gallery").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_wali_gallery_post_5001").assertIsDisplayed()
        composeTestRule.onNodeWithText("Belajar kelompok sore di masjid").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_purge_gallery_server_5001").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertTrue(purgeCalled)

        composeTestRule.onNodeWithTag("btn_open_create_wali_gallery").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dialog_create_wali_gallery").assertIsDisplayed()
    }

    @Test
    fun `ParentGalleryContent shows auto-save toggle, saves pending photo, and reflects saved badge`() {
        val now = System.currentTimeMillis()
        val pendingPost = WaliGalleryPost(
            id = 6001L,
            schoolId = "SRT1-KDR",
            waliUsername = "ust_ahmad",
            waliName = "Ustadz Ahmad",
            caption = "Lomba kebersihan kamar asrama",
            activityTag = WaliGalleryHelper.TAG_KEGIATAN,
            photoUrl = "https://waliasuhcareapp.areksurabaya.my.id/col/rec/f.webp",
            expiresAt = now + 7 * WaliGalleryHelper.DAY_MILLIS,
            createdAt = now,
            updatedAt = now
        )
        var autoSaveState by mutableStateOf(false)
        var localSavesMap by mutableStateOf<Map<Long, WaliGalleryLocalSave>>(emptyMap())
        var manualSaveTriggered = false

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAppStrings provides AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
            ) {
                ParentGalleryContent(
                    posts = listOf(pendingPost),
                    localSavesByPostId = localSavesMap,
                    waliName = "Ustadz Ahmad",
                    autoSaveEnabled = autoSaveState,
                    isSavingMedia = false,
                    onToggleAutoSave = { autoSaveState = it },
                    onSavePostToDevice = { post, _ ->
                        manualSaveTriggered = true
                        localSavesMap = mapOf(
                            post.id to WaliGalleryLocalSave(
                                postId = post.id,
                                localGalleryUri = "content://media/external/images/media/6001",
                                displayFileName = "WaliAsuhCare_6001.webp",
                                savedAt = now
                            )
                        )
                    },
                    onSaveAllPending = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("parent_gallery_content").assertIsDisplayed()
        composeTestRule.onNodeWithTag("parent_gallery_content")
            .performScrollToNode(hasTestTag("card_parent_gallery_post_6001"))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_save_post_to_gallery_6001").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertTrue(manualSaveTriggered)
        composeTestRule.onNodeWithText("Tersimpan di Galeri HP ✓").assertIsDisplayed()
    }

    @Test
    fun `ParentPortalScreen routes to Galeri Wali Asuh and isolates photos to matching caregiver`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val db = AppDatabase.getDatabase(app)
        val vm = com.example.ui.HealthViewModel(app)
        val activeSchool = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

        val student = ChildStudent(
            id = 9551L,
            name = "Farhan Maulana",
            nis = "3199551",
            gender = "L",
            age = 14,
            dormitory = "Asrama Putra 2",
            classGrade = "VIII-B",
            motherName = "Ibu Fatimah",
            assignedWaliUsername = "ust_galeri",
            assignedWaliName = "Ustadz Galeri",
            schoolId = activeSchool
        )
        db.childStudentDao().insert(student)

        val link = ParentAccessLink(
            id = 9561L,
            studentId = 9551L,
            studentName = student.name,
            studentNis = student.nis,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            parentName = "Ibu Fatimah",
            username = "ortu_farhan_9551",
            password = "Ortu#9551",
            assignedWaliUsername = "ust_galeri",
            assignedWaliName = "Ustadz Galeri",
            schoolId = activeSchool
        )
        db.parentAccessLinkDao().insert(link)

        val now = System.currentTimeMillis()
        val matchingPost = WaliGalleryPost(
            id = 9571L,
            schoolId = activeSchool,
            waliUsername = "ust_galeri",
            waliName = "Ustadz Galeri",
            caption = "Kerja bakti halaman asrama",
            activityTag = WaliGalleryHelper.TAG_KEBERSAMAAN,
            targetScope = WaliGalleryHelper.SCOPE_ALL_MY_STUDENTS,
            photoUrl = "https://waliasuhcareapp.areksurabaya.my.id/c/r/match.webp",
            expiresAt = now + 7 * WaliGalleryHelper.DAY_MILLIS,
            createdAt = now,
            updatedAt = now
        )
        val otherWaliPost = WaliGalleryPost(
            id = 9572L,
            schoolId = activeSchool,
            waliUsername = "ust_lain",
            waliName = "Ustadz Lain",
            caption = "Foto dari wali asuh berbeda",
            activityTag = WaliGalleryHelper.TAG_KEGIATAN,
            targetScope = WaliGalleryHelper.SCOPE_ALL_MY_STUDENTS,
            photoUrl = "https://waliasuhcareapp.areksurabaya.my.id/c/r/other.webp",
            expiresAt = now + 7 * WaliGalleryHelper.DAY_MILLIS,
            createdAt = now,
            updatedAt = now
        )
        db.waliGalleryPostDao().insertAll(listOf(matchingPost, otherWaliPost))

        val loginRes = vm.executeLogin("ortu_farhan_9551", "Ortu#9551")
        assertTrue(loginRes is com.example.ui.LoginResult.SuccessParent)
        var loops = 0
        while (loops < 25 && (vm.activeParentAccessLink.value == null || vm.waliGalleryPosts.value.isEmpty())) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(40)
            loops++
        }

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                com.example.ui.screens.ParentPortalScreen(
                    viewModel = vm,
                    onLogout = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_parent_summary_gallery").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("parent_gallery_content").assertIsDisplayed()
        composeTestRule.onNodeWithTag("parent_gallery_content")
            .performScrollToNode(hasTestTag("card_parent_gallery_post_9571"))
        composeTestRule.onNodeWithTag("card_parent_gallery_post_9571").assertExists()
        composeTestRule.onNodeWithTag("card_parent_gallery_post_9572").assertDoesNotExist()

        vm.logout()
        db.waliGalleryPostDao().deleteById(9571L)
        db.waliGalleryPostDao().deleteById(9572L)
        db.parentAccessLinkDao().delete(link)
        db.childStudentDao().delete(student)
        Unit
    }

    @Test
    fun `CreateWaliGalleryPostDialog caps multi-photo selection at 6, supports removal, and submits batch URIs`() {
        val sampleUris = (1..8).map { idx -> Uri.parse("content://media/external/images/media/$idx") }
        var publishedUris: List<Uri> = emptyList()
        var publishedCaption = ""

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAppStrings provides AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
            ) {
                CreateWaliGalleryPostDialog(
                    myStudents = emptyList(),
                    isPublishing = false,
                    initialSelectedUris = sampleUris,
                    onPublishPhotos = { uris, caption, _, _, _, _ ->
                        publishedUris = uris
                        publishedCaption = caption
                    },
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Capped at max 6 photos
        composeTestRule.onNodeWithTag("text_selected_gallery_photo_count").assertIsDisplayed()
        composeTestRule.onNodeWithText("6/6 Foto Dipilih").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_add_more_gallery_photos").assertDoesNotExist()
        composeTestRule.onNodeWithText("Posting 6 Foto Sekarang").assertIsDisplayed()

        // Remove first photo -> 5/6 and Add button becomes available
        composeTestRule.onNodeWithTag("btn_remove_gallery_photo_0").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("5/6 Foto Dipilih").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_add_more_gallery_photos").assertExists()
        composeTestRule.onNodeWithText("Posting 5 Foto Sekarang").assertIsDisplayed()

        // Fill caption and submit
        composeTestRule.onNodeWithTag("input_gallery_caption").performScrollTo().performTextInput("Karya tulis bersama")
        composeTestRule.onNodeWithTag("btn_submit_create_wali_gallery").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(5, publishedUris.size)
        assertEquals(sampleUris.drop(1).take(5), publishedUris)
        assertEquals("Karya tulis bersama", publishedCaption)
    }

    @Test
    fun `HealthViewModel publishWaliGalleryPosts publishes batch up to 6 photos`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val db = AppDatabase.getDatabase(app)
        val vm = com.example.ui.HealthViewModel(app)
        val uris = (1..7).map { idx -> Uri.parse("content://media/external/images/media/batch_$idx") }

        var callbackOk = false
        var callbackMsg = ""
        var done = false

        vm.publishWaliGalleryPosts(
            imageUris = uris,
            caption = "Dokumentasi multi foto kegiatan",
            activityTag = WaliGalleryHelper.TAG_KEGIATAN,
            targetScope = WaliGalleryHelper.SCOPE_ALL_MY_STUDENTS,
            retentionDays = 14
        ) { ok, msg ->
            callbackOk = ok
            callbackMsg = msg
            done = true
        }

        var loops = 0
        while (!done && loops < 50) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(40)
            loops++
        }

        assertTrue(done)
        assertTrue(callbackOk)
        assertTrue(callbackMsg.contains("6"))

        val inserted = vm.waliGalleryPosts.value.filter { it.caption == "Dokumentasi multi foto kegiatan" }
        assertEquals(6, inserted.size)

        inserted.forEach { db.waliGalleryPostDao().deleteById(it.id) }
        Unit
    }
}
