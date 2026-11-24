package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.ui.dialogs.DirectTreatmentDialog
import com.example.ui.dialogs.InputTreatmentResultDialog
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ControlDatePickerRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `InputTreatmentResultDialog exposes calendar date picker when control schedule is enabled`() {
        val indonesianStrings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        var submittedControlDate: Long? = null
        var submittedNeedsControl = false

        val record = MedicalRecord(
            id = 101L,
            studentId = 1L,
            studentName = "SILMA ANDARISTA",
            studentDormitory = "Asrama Putri",
            complaint = "mual muntah nyeri perut",
            faskes = "Puskesmas Plosoklaten",
            planDate = System.currentTimeMillis(),
            status = "PLANNED"
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides indonesianStrings) {
                InputTreatmentResultDialog(
                    record = record,
                    onDismiss = {},
                    onConfirm = { _, _, _, needsControl, controlDate, _, _, _ ->
                        submittedNeedsControl = needsControl
                        submittedControlDate = controlDate
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("input_diagnosis").performScrollTo().performTextInput("Gastritis")
        composeTestRule.onNodeWithTag("input_prescription").performScrollTo().performTextInput("Antasida 3x1")
        composeTestRule.onNodeWithTag("switch_needs_control").performScrollTo().performClick()

        composeTestRule.onNodeWithTag("btn_pick_control_date").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_control_date_picker").performScrollTo().assertIsDisplayed()

        // Open calendar DatePickerDialog
        composeTestRule.onNodeWithTag("btn_pick_control_date").performClick()
        composeTestRule.onNodeWithText("Pilih Tanggal Kontrol Kembali").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_confirm_control_date_picker").performClick()

        // Submit result
        composeTestRule.onNodeWithTag("confirm_submit_treatment_button").performScrollTo().performClick()

        assertTrue(submittedNeedsControl)
        assertNotNull(submittedControlDate)
    }

    @Test
    fun `DirectTreatmentDialog exposes calendar date picker when control plan is enabled`() {
        val indonesianStrings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        var submittedControlDate: Long? = null
        var submittedNeedsControl = false

        val student = ChildStudent(
            id = 5L,
            name = "Budi Santoso",
            nis = "2026005",
            gender = "L",
            age = 13,
            dormitory = "Asrama Putra",
            classGrade = "7B"
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides indonesianStrings) {
                DirectTreatmentDialog(
                    students = listOf(student),
                    existingRecord = MedicalRecord(
                        id = 202L,
                        studentId = 5L,
                        studentName = "Budi Santoso",
                        studentDormitory = "Asrama Putra",
                        complaint = "Demam",
                        diagnosis = "Febris",
                        prescription = "Paracetamol 3x1",
                        faskes = "Puskesmas Wates",
                        planDate = System.currentTimeMillis(),
                        needsControl = false,
                        status = "COMPLETED"
                    ),
                    onDismiss = {},
                    onConfirm = { _, _, _, _, _, _, needsControl, controlDate, _, _, _, _ ->
                        submittedNeedsControl = needsControl
                        submittedControlDate = controlDate
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("direct_switch_needs_control").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("direct_btn_pick_control_date").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("direct_card_control_date_picker").performScrollTo().assertIsDisplayed()

        // Open calendar DatePickerDialog via the control date card
        composeTestRule.onNodeWithTag("direct_card_control_date_picker").performClick()
        composeTestRule.onNodeWithText("Pilih Tanggal Kontrol Kembali").assertIsDisplayed()
        composeTestRule.onNodeWithTag("direct_btn_confirm_control_date_picker").performClick()

        composeTestRule.onNodeWithTag("confirm_direct_treatment_button").performScrollTo().performClick()

        assertTrue(submittedNeedsControl)
        assertNotNull(submittedControlDate)
    }
}
