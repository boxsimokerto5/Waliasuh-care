package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import com.example.data.model.ChildStudent
import com.example.data.model.SchoolProfile
import com.example.data.model.ShiftHandoverHelper
import com.example.data.model.ShiftMessage
import com.example.ui.dialogs.SerahTerimaShiftEditor
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ShiftHandoverFollowUpFilterRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val profile = SchoolProfile(
        schoolName = "Sekolah Rakyat Terintegrasi 1 Kediri",
        shortName = "SRT 1 Kediri",
        tenantCode = "SRT1-KDR"
    )

    private val sampleStudents = listOf(
        ChildStudent(
            id = 101L,
            name = "Alya Putri",
            nis = "101",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-A",
            schoolId = "SRT1-KDR"
        )
    )

    @Test
    fun `buildAutoPopulatedDraft filters ShiftMessages strictly by report date and target shift and leaves empty when none match`() {
        val monthKey = "2026-11"
        val day = 28
        val todayMillis = ShiftHandoverHelper.dateMillisFor(monthKey, day)
        val yesterdayMillis = ShiftHandoverHelper.dateMillisFor(monthKey, 27)
        val tomorrowMillis = ShiftHandoverHelper.dateMillisFor(monthKey, 29)

        val messages = listOf(
            ShiftMessage(
                id = 1L,
                title = "Tes Kemarin",
                followUpDate = yesterdayMillis,
                shift = "Shift Siang (14-21)",
                messageContent = "Pesan lama kemarin",
                priority = "URGENT",
                senderName = "Ustadz A"
            ),
            ShiftMessage(
                id = 2L,
                title = "Besok Pagi",
                followUpDate = tomorrowMillis,
                shift = "Shift Pagi (07-14)",
                messageContent = "Pesan besok",
                priority = "BIASA",
                senderName = "Ustadz A"
            ),
            ShiftMessage(
                id = 3L,
                title = "Selesai Hari Ini",
                followUpDate = todayMillis,
                shift = "Shift Malam (21-07)",
                messageContent = "Sudah tuntas",
                priority = "URGENT",
                senderName = "Ustadz A",
                isCompleted = true
            ),
            ShiftMessage(
                id = 4L,
                title = "Cek Obat Malam",
                followUpDate = todayMillis,
                shift = "Shift Malam (21-07)",
                messageContent = "Minumkan vitamin Alya pukul 21.30",
                priority = "URGENT",
                senderName = "Ustadzah B"
            ),
            ShiftMessage(
                id = 5L,
                title = "Pagi Hari Ini",
                followUpDate = todayMillis,
                shift = "Shift Pagi (07-14)",
                messageContent = "Apel pagi pukul 07.00",
                priority = "SEDANG",
                senderName = "Ustadz C"
            )
        )

        // 1. Pagi -> Sore (Target Shift Sore/Siang) on Nov 28: NO matching active message -> MUST be blank
        val (_, pagiKeSorePayload) = ShiftHandoverHelper.buildAutoPopulatedDraft(
            schoolProfile = profile,
            monthKey = monthKey,
            day = day,
            handoverType = ShiftHandoverHelper.TYPE_PAGI_KE_SORE,
            allScheduleEntries = emptyList(),
            shiftCodes = emptyList(),
            students = sampleStudents,
            medicalRecords = emptyList(),
            fastingRecords = emptyList(),
            shiftMessages = messages,
            authorUsername = "ust_test",
            authorName = "Ustadz Test"
        )
        assertEquals("", pagiKeSorePayload.followUpNotes)

        // 2. Sore -> Malam (Target Shift Malam) on Nov 28: ONLY id=4 matches
        val (_, soreKeMalamPayload) = ShiftHandoverHelper.buildAutoPopulatedDraft(
            schoolProfile = profile,
            monthKey = monthKey,
            day = day,
            handoverType = ShiftHandoverHelper.TYPE_SORE_KE_MALAM,
            allScheduleEntries = emptyList(),
            shiftCodes = emptyList(),
            students = sampleStudents,
            medicalRecords = emptyList(),
            fastingRecords = emptyList(),
            shiftMessages = messages,
            authorUsername = "ust_test",
            authorName = "Ustadz Test"
        )
        assertEquals("Cek Obat Malam: Minumkan vitamin Alya pukul 21.30", soreKeMalamPayload.followUpNotes)

        // 3. Malam -> Pagi (Target Shift Pagi) on Nov 28: ONLY id=5 matches
        val (_, malamKePagiPayload) = ShiftHandoverHelper.buildAutoPopulatedDraft(
            schoolProfile = profile,
            monthKey = monthKey,
            day = day,
            handoverType = ShiftHandoverHelper.TYPE_MALAM_KE_PAGI,
            allScheduleEntries = emptyList(),
            shiftCodes = emptyList(),
            students = sampleStudents,
            medicalRecords = emptyList(),
            fastingRecords = emptyList(),
            shiftMessages = messages,
            authorUsername = "ust_test",
            authorName = "Ustadz Test"
        )
        assertEquals("Pagi Hari Ini: Apel pagi pukul 07.00", malamKePagiPayload.followUpNotes)

        // 4. Same-day "Semua Shift" message IS included for matching date only
        val allShiftToday = ShiftMessage(
            id = 6L,
            title = "Pengumuman Hari Ini",
            followUpDate = todayMillis,
            shift = "Semua Shift",
            messageContent = "Pantau gerbang asrama",
            priority = "BIASA",
            senderName = "Admin"
        )
        val filteredWithAllShift = ShiftHandoverHelper.filterShiftMessagesForHandover(
            shiftMessages = messages + allShiftToday,
            monthKey = monthKey,
            day = day,
            handoverType = ShiftHandoverHelper.TYPE_PAGI_KE_SORE
        )
        assertEquals(listOf(6L), filteredWithAllShift.map { it.id })
        assertTrue(filteredWithAllShift.none { it.id == 1L || it.id == 2L || it.id == 3L || it.id == 4L || it.id == 5L })
    }

    @Test
    fun `SerahTerimaShiftEditor updates and clears auto followUpNotes when switching shift and filters Pull button`() {
        val monthKey = "2026-11"
        val day = 28
        val todayMillis = ShiftHandoverHelper.dateMillisFor(monthKey, day)
        val yesterdayMillis = ShiftHandoverHelper.dateMillisFor(monthKey, 27)

        val messages = listOf(
            ShiftMessage(
                id = 21L,
                title = "Pesan Lama",
                followUpDate = yesterdayMillis,
                shift = "Semua Shift",
                messageContent = "Harus tidak muncul di Tgl 28",
                priority = "URGENT",
                senderName = "Ustadz A"
            ),
            ShiftMessage(
                id = 22L,
                title = "Tugas Malam",
                followUpDate = todayMillis,
                shift = "Shift Malam (21-07)",
                messageContent = "Kunci pintu gerbang jam 22.00",
                priority = "URGENT",
                senderName = "Ustadz B"
            )
        )

        val (initialDraft, _) = ShiftHandoverHelper.buildAutoPopulatedDraft(
            schoolProfile = profile,
            monthKey = monthKey,
            day = day,
            handoverType = ShiftHandoverHelper.TYPE_PAGI_KE_SORE,
            allScheduleEntries = emptyList(),
            shiftCodes = emptyList(),
            students = sampleStudents,
            medicalRecords = emptyList(),
            fastingRecords = emptyList(),
            shiftMessages = messages,
            authorUsername = "ust_test",
            authorName = "Ustadz Test"
        )

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                SerahTerimaShiftEditor(
                    initialReport = initialDraft,
                    isExistingRecord = false,
                    isEn = false,
                    schoolProfile = profile,
                    allScheduleEntries = emptyList(),
                    shiftCodes = emptyList(),
                    students = sampleStudents,
                    medicalRecords = emptyList(),
                    fastingRecords = emptyList(),
                    shiftMessages = messages,
                    waliAccounts = emptyList(),
                    currentUsername = "ust_test",
                    currentWaliName = "Ustadz Test",
                    onSaveReport = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
                    onBack = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Initially Pagi -> Sore has NO matching messages today -> empty field, no Pull button, info note visible
        composeTestRule.onNodeWithTag("text_handover_no_matching_shift_msgs")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_handover_import_shift_msgs").assertDoesNotExist()

        // Switch to Sore -> Malam (matches id=22 on Nov 28 Malam)
        composeTestRule.onNodeWithText("Sore → Malam")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("input_handover_followup_notes")
            .performScrollTo()
            .assertTextContains("Kunci pintu gerbang jam 22.00", substring = true)
        composeTestRule.onNodeWithTag("btn_handover_import_shift_msgs")
            .performScrollTo()
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .assertTextContains("Ambil Pesan (1 Sesuai)", substring = true)

        // Switch back to Pagi -> Sore -> auto-populated field clears cleanly to empty!
        composeTestRule.onNodeWithText("Pagi → Sore")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_handover_import_shift_msgs").assertDoesNotExist()
        composeTestRule.onNodeWithTag("text_handover_no_matching_shift_msgs")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
