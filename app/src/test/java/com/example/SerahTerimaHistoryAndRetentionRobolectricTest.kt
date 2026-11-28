package com.example

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.SchoolProfile
import com.example.data.model.ShiftHandoverHelper
import com.example.data.model.ShiftHandoverPayload
import com.example.data.model.ShiftHandoverReport
import com.example.ui.HealthViewModel
import com.example.ui.dialogs.SerahTerimaShiftSheet
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
class SerahTerimaHistoryAndRetentionRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val profile = SchoolProfile(
        schoolName = "Sekolah Rakyat Terintegrasi 1 Kediri",
        shortName = "SRT 1 Kediri",
        tenantCode = "SRT1-KDR"
    )

    @Test
    fun `SerahTerimaShiftSheet orders presets from Malam ke Pagi first and expands compact card on click`() {
        val samplePayload = ShiftHandoverHelper.serializePayload(
            ShiftHandoverPayload(
                followUpNotes = "Pantau suhu siswa di kamar 4."
            )
        )
        val report = ShiftHandoverReport(
            id = 501L,
            schoolId = "SRT1-KDR",
            monthKey = "2026-11",
            day = 28,
            reportDateMillis = ShiftHandoverHelper.dateMillisFor("2026-11", 28),
            handoverType = ShiftHandoverHelper.TYPE_MALAM_KE_PAGI,
            status = ShiftHandoverHelper.STATUS_FINAL,
            title = "SERAH TERIMA SHIFT MALAM KE PAGI",
            schoolName = "SEKOLAH RAKYAT KABUPATEN KEDIRI",
            serviceUnit = "Pelayanan Wali Asuh",
            dateLabel = "Sabtu, 28 November 2026",
            handoverTime = "07:00 WIB",
            fromShiftLabel = "Shift Malam (Tgl 28)",
            toShiftLabel = "Shift Pagi (Tgl 28)",
            totalStudents = 343,
            presentStudents = 340,
            sickStudentsCount = 2,
            fastingStudentsCount = 15,
            payloadJson = samplePayload,
            authorUsername = "eko",
            authorName = "Eko Wahyudi",
            lastEditedBy = "Eko Wahyudi",
            createdAt = 1_795_800_000_000L,
            updatedAt = 1_795_800_000_000L
        )

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAppStrings provides AppLanguageManager.getStrings(AppLanguage.INDONESIAN)
            ) {
                SerahTerimaShiftSheet(
                    isEn = false,
                    reports = listOf(report),
                    schoolProfile = profile,
                    initialMonthKey = "2026-11",
                    initialDay = 28,
                    allScheduleEntries = emptyList(),
                    shiftCodes = emptyList(),
                    students = emptyList(),
                    medicalRecords = emptyList(),
                    fastingRecords = emptyList(),
                    haidRecords = emptyList(),
                    shiftMessages = emptyList(),
                    waliAccounts = emptyList(),
                    currentUsername = "eko",
                    currentWaliName = "Eko Wahyudi",
                    onSyncOnline = { it() },
                    onSaveReport = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, onDone ->
                        onDone(true, "OK", null)
                    },
                    onDeleteReport = { _, onDone -> onDone(true, "OK") },
                    onDismiss = {}
                )
            }
        }

        // 1. Verifikasi urutan tombol preset: Baris 1 = Malam -> Pagi & Pagi -> Sore, Baris 2 = Sore -> Malam & Custom
        val malamPagiBounds = composeTestRule.onNodeWithTag("btn_create_handover_malam_pagi")
            .assertIsDisplayed()
            .getUnclippedBoundsInRoot()
        val pagiSoreBounds = composeTestRule.onNodeWithTag("btn_create_handover_pagi_sore")
            .assertIsDisplayed()
            .getUnclippedBoundsInRoot()
        val soreMalamBounds = composeTestRule.onNodeWithTag("btn_create_handover_sore_malam")
            .assertIsDisplayed()
            .getUnclippedBoundsInRoot()
        val customBounds = composeTestRule.onNodeWithTag("btn_create_handover_custom")
            .assertIsDisplayed()
            .getUnclippedBoundsInRoot()

        assertTrue(malamPagiBounds.left < pagiSoreBounds.left)
        assertTrue(malamPagiBounds.top < soreMalamBounds.top)
        assertTrue(pagiSoreBounds.top < customBounds.top)
        assertTrue(soreMalamBounds.left < customBounds.left)

        // 2. Verifikasi kartu history awalnya ringkas (detail & tombol aksi belum ditampilkan)
        composeTestRule.onNodeWithTag("card_handover_report_501").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_handover_detail_501").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_edit_handover_501").assertDoesNotExist()

        // 3. Klik header kartu untuk membuka detail beserta tombol aksi dengan ukuran sentuh >= 48dp
        composeTestRule.onNodeWithTag("header_handover_report_501").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_handover_detail_501").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total 343").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hadir 340").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_edit_handover_501")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("btn_share_wa_handover_501")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("btn_copy_handover_501")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("btn_delete_handover_501")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)

        // 4. Klik header kartu lagi untuk menutup kembali detail
        composeTestRule.onNodeWithTag("header_handover_report_501").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("card_handover_detail_501").assertDoesNotExist()
    }

    @Test
    fun `ShiftHandoverHelper and HealthViewModel automatically purge reports older than 90 days while keeping recent ones`() = runBlocking {
        val now = 1_800_000_000_000L
        val oldReport = ShiftHandoverReport(
            id = 9101L,
            schoolId = "SRT1-KDR",
            monthKey = "2026-01",
            day = 10,
            reportDateMillis = now - (95L * 24L * 60L * 60L * 1000L),
            handoverType = ShiftHandoverHelper.TYPE_MALAM_KE_PAGI,
            status = ShiftHandoverHelper.STATUS_FINAL,
            title = "Serah Terima Lama 95 Hari",
            schoolName = "SRT 1 Kediri",
            serviceUnit = "Pelayanan Wali Asuh",
            dateLabel = "10 Januari 2026",
            handoverTime = "07:00 WIB",
            fromShiftLabel = "Shift Malam",
            toShiftLabel = "Shift Pagi",
            createdAt = now - (95L * 24L * 60L * 60L * 1000L),
            updatedAt = now - (95L * 24L * 60L * 60L * 1000L)
        )
        val freshReport = oldReport.copy(
            id = 9102L,
            monthKey = "2026-11",
            day = 20,
            reportDateMillis = now - (10L * 24L * 60L * 60L * 1000L),
            title = "Serah Terima Baru 10 Hari",
            dateLabel = "20 November 2026",
            createdAt = now - (10L * 24L * 60L * 60L * 1000L),
            updatedAt = now - (10L * 24L * 60L * 60L * 1000L)
        )

        assertTrue(ShiftHandoverHelper.isExpiredForRetention(oldReport, now))
        assertFalse(ShiftHandoverHelper.isExpiredForRetention(freshReport, now))

        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = AppDatabase.getDatabase(app)
        val dao = db.shiftHandoverDao()

        val realNow = System.currentTimeMillis()
        val dbExpiredReport = oldReport.copy(
            id = 9201L,
            reportDateMillis = realNow - (95L * 24L * 60L * 60L * 1000L),
            createdAt = realNow - (95L * 24L * 60L * 60L * 1000L),
            updatedAt = realNow - (95L * 24L * 60L * 60L * 1000L)
        )
        val dbFreshReport = freshReport.copy(
            id = 9202L,
            reportDateMillis = realNow - (10L * 24L * 60L * 60L * 1000L),
            createdAt = realNow - (10L * 24L * 60L * 60L * 1000L),
            updatedAt = realNow - (10L * 24L * 60L * 60L * 1000L)
        )
        dao.insertAll(listOf(dbExpiredReport, dbFreshReport))

        // Inisialisasi ViewModel harus otomatis memicu pembersihan retensi 90 hari
        HealthViewModel(app)
        for (attempt in 1..40) {
            ShadowLooper.idleMainLooper()
            val remaining = dao.getAllIncludingDeletedList()
            if (remaining.none { it.id == 9201L }) break
            delay(50)
        }

        val finalList = dao.getAllIncludingDeletedList()
        assertNull(finalList.firstOrNull { it.id == 9201L })
        assertNotNull(finalList.firstOrNull { it.id == 9202L })
    }
}
