package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.SchoolProfile
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionSwap
import com.example.ui.HealthViewModel
import com.example.ui.LoginResult
import com.example.ui.screens.AdminScreen
import com.example.util.VisitCircularPdfConfig
import com.example.util.VisitCircularPdfHelper
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VisitCircularPdfRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun dayMillis(year: Int, monthZeroBased: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun capturePagesTexts(
        block: (startNew: (Int) -> Canvas, finishPage: () -> Unit) -> Unit
    ): List<String> {
        val pages = mutableListOf<MutableList<String>>()
        val bitmaps = mutableListOf<Bitmap>()
        try {
            block(
                {
                    val bmp = Bitmap.createBitmap(595, 842, Bitmap.Config.ARGB_8888)
                    bitmaps.add(bmp)
                    val pageTexts = mutableListOf<String>()
                    pages.add(pageTexts)
                    object : Canvas(bmp) {
                        override fun drawText(text: String, x: Float, y: Float, paint: Paint) {
                            pageTexts.add(text)
                            super.drawText(text, x, y, paint)
                        }
                    }
                },
                {}
            )
            return pages.map { it.joinToString(" | ") }
        } finally {
            bitmaps.forEach { it.recycle() }
        }
    }

    private suspend fun waitUntil(maxTicks: Int = 150, condition: () -> Boolean) {
        var ticks = 0
        while (ticks < maxTicks && !condition()) {
            ShadowLooper.idleMainLooper()
            delay(35)
            ticks++
        }
    }

    @Test
    fun `VisitCircularPdfHelper builds single combined PDF with cover notice session-grouped students swaps and unscheduled section`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val schoolId = "SRT1-KDR"
        val date1 = dayMillis(2026, Calendar.NOVEMBER, 15)
        val date2 = dayMillis(2026, Calendar.NOVEMBER, 22)

        val session1 = VisitSession(
            id = 6101L,
            title = "Kunjungan November 2026 - Sesi 1",
            monthYear = "2026-11",
            sessionDatesCsv = date1.toString(),
            selectedDistrictsCsv = "Wates",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            publishedToWali = true,
            publishedToParent = true,
            schoolId = schoolId
        )
        val session2 = VisitSession(
            id = 6102L,
            title = "Kunjungan November 2026 - Sesi 2",
            monthYear = "2026-11",
            sessionDatesCsv = date2.toString(),
            selectedDistrictsCsv = "Pare,Puncu",
            visitTimeRange = "08.00 - 12.00 WIB",
            isActive = true,
            publishedToWali = true,
            publishedToParent = true,
            schoolId = schoolId
        )

        val budiWates = ChildStudent(
            id = 6201L,
            name = "Budi Santoso",
            nis = "6201",
            nik = "3506620100010001",
            gender = "L",
            age = 13,
            dormitory = "Asrama Putra",
            classGrade = "VII-A",
            district = "Wates",
            assignedWaliUsername = "ust_budi",
            assignedWaliName = "Ustadz Budi",
            schoolId = schoolId
        )
        val citraPare = ChildStudent(
            id = 6202L,
            name = "Citra Lestari",
            nis = "6202",
            nik = "3506620200010002",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-B",
            district = "Pare",
            assignedWaliUsername = "ust_siti",
            assignedWaliName = "Ustadzah Siti",
            schoolId = schoolId
        )
        val dinaKras = ChildStudent(
            id = 6203L,
            name = "Dina Rahmawati",
            nis = "6203",
            nik = "3506620300010003",
            gender = "P",
            age = 15,
            dormitory = "Asrama Putri",
            classGrade = "IX-A",
            district = "Kras",
            assignedWaliUsername = "ust_siti",
            assignedWaliName = "Ustadzah Siti",
            schoolId = schoolId
        )

        val swapBudiToSesi2 = VisitSessionSwap(
            id = 6301L,
            schoolId = schoolId,
            monthYear = "2026-11",
            fromSessionId = session1.id,
            fromSessionTitle = session1.title,
            toSessionId = session2.id,
            toSessionTitle = session2.title,
            studentId = budiWates.id,
            studentName = budiWates.name,
            studentNik = budiWates.nik,
            studentNis = budiWates.nis,
            studentClass = budiWates.classGrade,
            studentDorm = budiWates.dormitory,
            studentDistrict = budiWates.district,
            reason = "Ortu bisanya tanggal 22 Nov"
        )

        val profile = SchoolProfile(
            schoolName = "Sekolah Rakyat Terintegrasi 1 Kediri",
            shortName = "SRT 1 Kediri",
            city = "Kabupaten Kediri",
            tenantCode = schoolId
        )

        val pageBreakPages = capturePagesTexts { startNew, finish ->
            VisitCircularPdfHelper.renderMonthlyCircularPages(
                context = app,
                sessions = listOf(session1, session2),
                allStudents = listOf(budiWates, citraPare, dinaKras),
                swaps = listOf(swapBudiToSesi2),
                allSchoolSessions = listOf(session1, session2),
                schoolProfile = profile,
                config = VisitCircularPdfConfig(
                    monthYear = "2026-11",
                    circularNumber = "B-12/SRT1/XI/2026",
                    pageBreakPerSession = true,
                    includeUnscheduledAppendix = true,
                    signerName = "Drs. Ahmad Koordinator"
                ),
                startNewCanvas = startNew,
                finishCanvas = finish
            )
        }

        // Page 1 = Cover Letter + Session summary table; Page 2 = Appendix 1; Page 3 = Appendix 2 (+ unscheduled)
        assertEquals(3, pageBreakPages.size)
        val coverText = pageBreakPages[0]
        assertTrue(coverText.contains("EDARAN PEMBAGIAN JADWAL KUNJUNGAN WALI SISWA"))
        assertTrue(coverText.contains("B-12/SRT1/XI/2026"))
        assertTrue(coverText.contains("Kunjungan November 2026 - Sesi 1"))
        assertTrue(coverText.contains("Kunjungan November 2026 - Sesi 2"))
        assertTrue(coverText.contains("Drs. Ahmad Koordinator"))

        val app1Text = pageBreakPages[1]
        assertTrue(app1Text.contains("LAMPIRAN 1"))
        assertTrue(app1Text.contains("Belum ada siswa terjadwal pada sesi ini."))
        assertFalse("Swapped student Budi must no longer appear under Sesi 1", app1Text.contains("Budi Santoso"))

        val app2Text = pageBreakPages[2]
        assertTrue(app2Text.contains("LAMPIRAN 2"))
        assertTrue("Budi Santoso must appear under Sesi 2", app2Text.contains("Budi Santoso"))
        assertTrue(app2Text.contains("Pindahan (Kunjungan November 2026 - Sesi 1)"))
        assertTrue("Citra Lestari must appear under Sesi 2", app2Text.contains("Citra Lestari"))
        assertTrue("Unscheduled student Dina must appear in unscheduled appendix", app2Text.contains("Dina Rahmawati"))
        assertTrue(app2Text.contains("BELUM MASUK KELOMPOK SESI TERPILIH"))

        // Compact mode (no forced page break per session) merges Appendix 1 and 2 together on Page 2
        val compactPages = capturePagesTexts { startNew, finish ->
            VisitCircularPdfHelper.renderMonthlyCircularPages(
                context = app,
                sessions = listOf(session1, session2),
                allStudents = listOf(budiWates, citraPare, dinaKras),
                swaps = listOf(swapBudiToSesi2),
                allSchoolSessions = listOf(session1, session2),
                schoolProfile = profile,
                config = VisitCircularPdfConfig(
                    monthYear = "2026-11",
                    pageBreakPerSession = false,
                    includeUnscheduledAppendix = false
                ),
                startNewCanvas = startNew,
                finishCanvas = finish
            )
        }
        assertEquals(2, compactPages.size)
        assertFalse("Unscheduled student should be omitted when toggle is off", compactPages[1].contains("Dina Rahmawati"))

        val outFile = VisitCircularPdfHelper.generateMonthlyCircularPdf(
            context = app,
            sessions = listOf(session1, session2),
            allStudents = listOf(budiWates, citraPare, dinaKras),
            swaps = listOf(swapBudiToSesi2),
            allSchoolSessions = listOf(session1, session2),
            schoolProfile = profile,
            config = VisitCircularPdfConfig(monthYear = "2026-11", circularNumber = "B-12/SRT1/XI/2026")
        )
        assertNotNull("Combined PDF file must be generated", outFile)
        assertTrue(outFile!!.exists())
        assertTrue(outFile.length() > 0L)
        outFile.delete()
    }

    @Test
    fun `AdminScreen Kunjungan tab opens Circular PDF dialog customizes text and generates combined PDF`(): Unit = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = HealthViewModel(app)
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SRT1-KDR"
        val date1 = dayMillis(2026, Calendar.NOVEMBER, 15)
        val date2 = dayMillis(2026, Calendar.NOVEMBER, 22)

        val student = ChildStudent(
            id = 6401L,
            name = "Farhan Maulana",
            nis = "6401",
            nik = "3506640100010001",
            gender = "L",
            age = 14,
            dormitory = "Asrama Putra",
            classGrade = "VIII-A",
            district = "Papar",
            schoolId = schoolId
        )
        val s1 = VisitSession(
            id = 6501L,
            title = "Kunjungan Nov Sesi 1",
            monthYear = "2026-11",
            sessionDatesCsv = date1.toString(),
            selectedDistrictsCsv = "Papar",
            visitTimeRange = "08.00 - 11.00 WIB",
            isActive = true,
            publishedToWali = true,
            publishedToParent = true,
            schoolId = schoolId
        )
        val s2 = VisitSession(
            id = 6502L,
            title = "Kunjungan Nov Sesi 2",
            monthYear = "2026-11",
            sessionDatesCsv = date2.toString(),
            selectedDistrictsCsv = "Puncu",
            visitTimeRange = "08.00 - 12.00 WIB",
            isActive = true,
            publishedToWali = true,
            publishedToParent = false,
            schoolId = schoolId
        )
        db.childStudentDao().insert(student)
        db.visitSessionDao().insert(s1)
        db.visitSessionDao().insert(s2)

        assertTrue(vm.executeLogin("superadmin", "Woyowoyo12@") is LoginResult.SuccessSuperAdmin)
        waitUntil { vm.visitSessions.value.any { it.id == 6501L } }

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                AdminScreen(
                    viewModel = vm,
                    onLogout = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("list_admin_content")
            .performScrollToNode(hasTestTag("row_admin_tabs"))
        composeTestRule.onNodeWithTag("tab_admin_visit").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("list_admin_content")
            .performScrollToNode(hasTestTag("btn_admin_visit_circular_pdf"))
        composeTestRule.onNodeWithTag("btn_admin_visit_circular_pdf")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dialog_visit_circular_pdf").assertIsDisplayed()
        composeTestRule.onNodeWithTag("row_circular_session_6501").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("row_circular_session_6502").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("btn_toggle_circular_text_editor")
            .performScrollTo()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("input_circular_number")
            .performScrollTo()
            .performTextReplacement("B-099/SRT1/XI/2026")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_generate_visit_circular_pdf")
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dialog_visit_circular_pdf_ready").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_open_visit_circular_pdf").assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("btn_share_visit_circular_pdf").assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("btn_close_visit_circular_pdf")
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()

        vm.logout()
        db.visitSessionDao().deleteById(6501L)
        db.visitSessionDao().deleteById(6502L)
        db.childStudentDao().delete(student)
    }
}
