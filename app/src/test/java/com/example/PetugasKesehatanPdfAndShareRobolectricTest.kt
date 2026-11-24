package com.example

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolProfile
import com.example.ui.dialogs.MonthlyReportContent
import com.example.ui.dialogs.ShareWhatsAppDialog
import com.example.util.PdfReportGenerator
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PetugasKesehatanPdfAndShareRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `PdfReportGenerator signature and kop constants reflect Petugas Kesehatan vs Wali Asuh`() {
        val school = SchoolProfile(
            schoolName = "SEKOLAH RAKYAT KABUPATEN KEDIRI",
            shortName = "SEKOLAH RAKYAT KABUPATEN KEDIRI"
        )

        assertEquals(
            "Wali Asuh SEKOLAH RAKYAT KABUPATEN KEDIRI,",
            PdfReportGenerator.buildFooterSignatureTitle(school)
        )
        assertEquals(
            "Petugas Kesehatan SEKOLAH RAKYAT KABUPATEN KEDIRI,",
            PdfReportGenerator.buildFooterSignatureTitle(
                schoolProfile = school,
                roleTitle = PdfReportGenerator.SIGNER_ROLE_PETUGAS_KESEHATAN
            )
        )
        assertEquals("UNIT KESEHATAN SEKOLAH", PdfReportGenerator.UNIT_KESEHATAN_SEKOLAH)
        assertEquals("UNIT LAYANAN KESEHATAN WALIASUH", PdfReportGenerator.UNIT_LAYANAN_KESEHATAN)
    }

    @Test
    fun `PdfReportGenerator creates student and monthly PDF files cleanly for Petugas Kesehatan`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val school = SchoolProfile(
            schoolName = "SEKOLAH RAKYAT KABUPATEN KEDIRI",
            shortName = "SEKOLAH RAKYAT KABUPATEN KEDIRI",
            address = "Ploso Kidul, Plosoklaten, Kediri",
            city = "KEDIRI"
        )
        val student = ChildStudent(
            id = 77L,
            name = "Silma Andarista",
            nis = "5759666",
            gender = "P",
            age = 17,
            dormitory = "Asrama Putri",
            classGrade = "XI-3 (IPS)"
        )
        val now = System.currentTimeMillis()
        val record = MedicalRecord(
            id = 88L,
            studentId = student.id,
            studentName = student.name,
            studentDormitory = student.dormitory,
            faskes = "Puskesmas Plosoklaten",
            complaint = "Mual muntah",
            diagnosis = "Maag akut",
            actionsTaken = "Infus",
            prescription = "Paracetamol",
            planDate = now,
            treatmentDate = now,
            status = "COMPLETED"
        )

        val studentPdf = PdfReportGenerator.generateHealthReportPdf(
            context = context,
            student = student,
            records = listOf(record),
            periodType = PdfReportGenerator.ReportPeriodType.MONTHLY,
            periodLabel = "November 2026",
            waliName = "Bu Ana",
            schoolProfile = school,
            isPetugasKesehatan = true
        )
        assertNotNull(studentPdf)
        assertTrue(studentPdf!!.exists())

        val monthlyPdf = PdfReportGenerator.generateMonthlyAggregatePdf(
            context = context,
            records = listOf(record),
            totalStudentsCount = 20,
            monthYearLabel = "November 2026",
            waliName = "Bu Ana",
            schoolProfile = school,
            isPetugasKesehatan = true
        )
        assertNotNull(monthlyPdf)
        assertTrue(monthlyPdf!!.exists())
    }

    @Test
    fun `MonthlyReportContent renders UKS WhatsApp share preview with Petugas Kesehatan signature`() {
        val school = SchoolProfile(
            schoolName = "SEKOLAH RAKYAT KABUPATEN KEDIRI",
            shortName = "SEKOLAH RAKYAT KABUPATEN KEDIRI"
        )
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAppStrings provides AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
            ) {
                MonthlyReportContent(
                    records = emptyList(),
                    students = emptyList(),
                    waliName = "Bu Ana",
                    schoolProfile = school,
                    isPetugasKesehatan = true
                )
            }
        }
        composeTestRule.waitForIdle()

        // Switch to WhatsApp tab (tab index 1: "Format WhatsApp")
        composeTestRule.onNodeWithText("Format WhatsApp").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("UNIT KESEHATAN SEKOLAH — SEKOLAH RAKYAT KABUPATEN KEDIRI", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Petugas Kesehatan:* Bu Ana", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `ShareWhatsAppDialog uses Unit Kesehatan Sekolah text when isPetugasKesehatan is true`() {
        val school = SchoolProfile(
            schoolName = "SEKOLAH RAKYAT KABUPATEN KEDIRI",
            shortName = "SRT Kediri"
        )
        val record = MedicalRecord(
            id = 91L,
            studentId = 1L,
            studentName = "Silma Andarista",
            studentDormitory = "Asrama Putri",
            faskes = "Puskesmas Plosoklaten",
            complaint = "Demam",
            diagnosis = "Febris",
            actionsTaken = "Observasi UKS",
            prescription = "Paracetamol",
            planDate = System.currentTimeMillis(),
            treatmentDate = System.currentTimeMillis(),
            status = "COMPLETED"
        )

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAppStrings provides AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
            ) {
                ShareWhatsAppDialog(
                    record = record,
                    schoolProfile = school,
                    isPetugasKesehatan = true,
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("share_whatsapp_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("UNIT KESEHATAN SEKOLAH — SEKOLAH RAKYAT KABUPATEN KEDIRI", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Unit Kesehatan Sekolah SEKOLAH RAKYAT KABUPATEN KEDIRI", substring = true)
            .assertIsDisplayed()
    }
}
