package com.example

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
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
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.FastingRecord
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentExpectation
import com.example.data.model.SchoolProfile
import com.example.data.model.SholatDisciplineTag
import com.example.data.model.SholatHelper
import com.example.data.model.SholatRecord
import com.example.data.model.SholatStatus
import com.example.data.model.SholatWaktu
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganTransaction
import com.example.ui.HealthViewModel
import com.example.ui.LoginResult
import com.example.ui.components.ParentPdfReadyDialog
import com.example.ui.components.ParentReadyPdfDialogData
import com.example.ui.screens.ParentPortalScreen
import com.example.util.ParentPortalPdfHelper
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
class ParentPortalPdfRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun captureRenderedPdfTexts(block: (startNewCanvas: (Int) -> Canvas) -> Unit): Pair<Int, String> {
        val texts = mutableListOf<String>()
        val bitmaps = mutableListOf<Bitmap>()
        try {
            block {
                val bmp = Bitmap.createBitmap(595, 842, Bitmap.Config.ARGB_8888)
                bitmaps.add(bmp)
                object : Canvas(bmp) {
                    override fun drawText(text: String, x: Float, y: Float, paint: Paint) {
                        texts.add(text)
                        super.drawText(text, x, y, paint)
                    }
                }
            }
            return bitmaps.size to texts.joinToString(" | ")
        } finally {
            bitmaps.forEach { it.recycle() }
        }
    }

    @Test
    fun `ParentPortalPdfHelper renders Fasting Haid Harapan and Comprehensive pages with accurate student-scoped content`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val school = SchoolProfile(
            schoolName = "Sekolah Rakyat Terintegrasi 1 Kediri",
            shortName = "SRT 1 Kediri",
            tenantCode = "SRT1-KDR"
        )
        val now = System.currentTimeMillis()
        val femaleMuslimStudent = ChildStudent(
            id = 8801L,
            name = "Zahra Salsabila",
            nis = "880101",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-A",
            allergies = "Debu",
            bloodType = "O",
            religion = "Islam",
            assignedWaliUsername = "ustsari",
            assignedWaliName = "Ustadzah Sari",
            schoolId = "SRT1-KDR"
        )
        val fasting = listOf(
            FastingRecord(
                id = 1L,
                sessionId = 10L,
                sessionTitle = "Puasa Sunnah Senin",
                sessionDate = now,
                studentId = 8801L,
                studentName = femaleMuslimStudent.name,
                isSahurChecked = true,
                isBukaChecked = true,
                waliName = "Ustadzah Sari",
                notes = "Sahur dan buka tuntas",
                schoolId = "SRT1-KDR"
            )
        )
        val haid = listOf(
            HaidRecord(
                id = 2L,
                studentId = 8801L,
                studentName = femaleMuslimStudent.name,
                studentClass = "VIII-A",
                studentDorm = "Asrama Putri",
                startDate = now - 3 * 86_400_000L,
                endDate = null,
                status = "HAID",
                symptoms = "Nyeri ringan",
                waliName = "Ustadzah Sari",
                notes = "Diberi minuman hangat",
                schoolId = "SRT1-KDR"
            )
        )
        val expectations = listOf(
            ParentExpectation(
                id = 3L,
                studentId = 8801L,
                studentName = femaleMuslimStudent.name,
                parentName = "Ibu Fatimah",
                characterFocusCsv = "Kemandirian|Ibadah & Akhlak Mulia",
                characterExpectation = "Istiqomah sholat berjamaah dan mandiri merapikan lemari.",
                adviceFocusNotes = "Mohon diingatkan tidur tepat waktu.",
                parentPrayerMessage = "Semoga sehat dan berkah ilmunya.",
                waliFollowUpNote = "Siap Bunda, sudah kami bimbing rutin.",
                schoolId = "SRT1-KDR",
                updatedAt = now
            )
        )
        val sholat = listOf(
            SholatRecord(
                id = 4L,
                dateKey = SholatHelper.todayDateKey(),
                dateMillis = now,
                waktu = SholatWaktu.SUBUH.code,
                status = SholatStatus.JAMAAH.code,
                disciplineTag = SholatDisciplineTag.SAF_DEPAN.code,
                studentId = 8801L,
                studentName = femaleMuslimStudent.name,
                schoolId = "SRT1-KDR"
            )
        )
        val mengaji = listOf(
            MengajiRecord(
                id = 5L,
                studentId = 8801L,
                studentName = femaleMuslimStudent.name,
                sessionDate = now,
                bookType = "Al-Qur'an",
                jilidOrJuz = "Juz 30",
                pageOrVerse = "An-Naba 1-15",
                fluencyStatus = MengajiHelper.FLUENCY_LANCAR,
                waliName = "Ustadzah Sari",
                schoolId = "SRT1-KDR"
            )
        )
        val medical = listOf(
            MedicalRecord(
                id = 6L,
                studentId = 8801L,
                studentName = femaleMuslimStudent.name,
                studentDormitory = "Asrama Putri",
                faskes = "UKS Sekolah",
                complaint = "Demam ringan",
                planDate = now,
                status = "COMPLETED",
                diagnosis = "Kelelahan",
                prescription = "Paracetamol & istirahat",
                createdBy = "Ustadzah Sari"
            )
        )
        val tabungan = listOf(
            TabunganTransaction(
                id = 7L,
                studentId = 8801L,
                studentName = femaleMuslimStudent.name,
                ownerWaliUsername = "ustsari",
                ownerWaliName = "Ustadzah Sari",
                transactionType = TabunganHelper.TYPE_IN,
                amount = 100_000L,
                title = "Titipan Uang Saku",
                transactionDate = now,
                schoolId = "SRT1-KDR"
            )
        )

        val (fastPages, fastText) = captureRenderedPdfTexts { startNew ->
            ParentPortalPdfHelper.renderStudentFastingReportPages(
                context = context,
                student = femaleMuslimStudent,
                records = fasting,
                periodLabel = "November 2026",
                waliName = "Ustadzah Sari",
                schoolProfile = school,
                startNewCanvas = startNew
            )
        }
        assertTrue(fastPages >= 1)
        assertTrue(fastText.contains("LAPORAN RIWAYAT PUASA ANAK ASUH"))
        assertTrue(fastText.contains("Zahra Salsabila"))
        assertTrue(fastText.contains("Puasa Sunnah Senin"))
        assertTrue(fastText.contains("Ustadzah Sari"))

        val (haidPages, haidText) = captureRenderedPdfTexts { startNew ->
            ParentPortalPdfHelper.renderStudentHaidReportPages(
                context = context,
                student = femaleMuslimStudent,
                records = haid,
                periodLabel = "Semua Riwayat",
                waliName = "Ustadzah Sari",
                schoolProfile = school,
                startNewCanvas = startNew
            )
        }
        assertTrue(haidPages >= 1)
        assertTrue(haidText.contains("LAPORAN PEMANTAUAN SIKLUS HAID SISWI"))
        assertTrue(haidText.contains("Zahra Salsabila"))
        assertTrue(haidText.contains("Sedang Haid"))

        val (harapanPages, harapanText) = captureRenderedPdfTexts { startNew ->
            ParentPortalPdfHelper.renderStudentHarapanReportPages(
                context = context,
                student = femaleMuslimStudent,
                expectations = expectations,
                waliName = "Ustadzah Sari",
                schoolProfile = school,
                startNewCanvas = startNew
            )
        }
        assertTrue(harapanPages >= 1)
        assertTrue(harapanText.contains("CATATAN HARAPAN KARAKTER & DOA ORANG TUA"))
        assertTrue(harapanText.contains("Zahra Salsabila"))
        assertTrue(harapanText.contains("Istiqomah sholat berjamaah"))

        val (compMuslimPages, compMuslimText) = captureRenderedPdfTexts { startNew ->
            ParentPortalPdfHelper.renderParentComprehensiveReportPages(
                context = context,
                student = femaleMuslimStudent,
                schoolProfile = school,
                waliName = "Ustadzah Sari",
                isChristianStudent = false,
                isFemaleStudent = true,
                sholatRecords = sholat,
                mengajiRecords = mengaji,
                christianRecords = emptyList(),
                fastingRecords = fasting,
                haidRecords = haid,
                medicalRecords = medical,
                tabunganTransactions = tabungan,
                expectations = expectations,
                startNewCanvas = startNew
            )
        }
        assertTrue(compMuslimPages >= 1)
        assertTrue(compMuslimText.contains("RINGKASAN LENGKAP PORTAL ORANG TUA"))
        assertTrue(compMuslimText.contains("SHOLAT 5 WAKTU & MENGAJI"))
        assertTrue(compMuslimText.contains("SIKLUS HAID"))
        assertTrue(compMuslimText.contains("Demam ringan"))
        assertTrue(compMuslimText.contains("Rp 100.000"))

        val christianMaleStudent = femaleMuslimStudent.copy(
            id = 8802L,
            name = "Daniel Pratama",
            gender = "L",
            dormitory = "Asrama Putra",
            religion = "Kristen"
        )
        val christianLogs = listOf(
            ChristianWorshipRecord(
                id = 8L,
                schoolId = "SRT1-KDR",
                studentId = 8802L,
                studentName = christianMaleStudent.name,
                activityType = ChristianWorshipRecord.TYPE_DOA_PAGI,
                activityDate = now,
                dateKey = SholatHelper.todayDateKey(),
                title = "Saat Teduh Pagi",
                waliName = "Pak Yohanes"
            )
        )
        val (compChristianPages, compChristianText) = captureRenderedPdfTexts { startNew ->
            ParentPortalPdfHelper.renderParentComprehensiveReportPages(
                context = context,
                student = christianMaleStudent,
                schoolProfile = school,
                waliName = "Pak Yohanes",
                isChristianStudent = true,
                isFemaleStudent = false,
                sholatRecords = emptyList(),
                mengajiRecords = emptyList(),
                christianRecords = christianLogs,
                fastingRecords = emptyList(),
                haidRecords = emptyList(),
                medicalRecords = emptyList(),
                tabunganTransactions = tabungan,
                expectations = expectations,
                startNewCanvas = startNew
            )
        }
        assertTrue(compChristianPages >= 1)
        assertTrue(compChristianText.contains("Daniel Pratama"))
        assertTrue(compChristianText.contains("PEMBINAAN IBADAH AGAMA KRISTEN"))
        assertTrue(compChristianText.contains("Saat Teduh Pagi"))
        assertFalse(compChristianText.contains("SIKLUS HAID"))
    }

    @Test
    fun `ParentPdfReadyDialog renders Open Share Close controls and dismisses properly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tempPdf = File(context.cacheDir, "test_ready.pdf").apply {
            writeText("%PDF-1.4\n%%EOF")
        }
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                var open by remember { mutableStateOf(true) }
                if (open) {
                    ParentPdfReadyDialog(
                        data = ParentReadyPdfDialogData(
                            file = tempPdf,
                            title = "PDF Rapor Terpadu Siap",
                            description = "Pilih buka PDF atau bagikan rapor perkembangan.",
                            shareSubject = "Rapor Terpadu",
                            shareText = "Terlampir PDF"
                        ),
                        isEn = false,
                        onDismiss = { open = false }
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dialog_parent_pdf_ready").assertIsDisplayed()
        composeTestRule.onNodeWithText("PDF Rapor Terpadu Siap").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_parent_pdf_open").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_parent_pdf_share").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_parent_pdf_close").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dialog_parent_pdf_ready").assertDoesNotExist()
        tempPdf.delete()
    }

    @Test
    fun `ParentPortalScreen renders accessible 48dp PDF download buttons and filter chips across all parent menus`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        app.getSharedPreferences("waliasuh_school_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        val db = AppDatabase.getDatabase(app)
        val vm = HealthViewModel(app)
        val schoolId = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
        val now = System.currentTimeMillis()
        val todayKey = SholatHelper.todayDateKey()

        val student = ChildStudent(
            id = 9811L,
            name = "Nayla Azzahra",
            nis = "3199811",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-B",
            allergies = "Tidak ada",
            bloodType = "A",
            motherName = "Ibu Rahmawati",
            assignedWaliUsername = "ustsari_pdf",
            assignedWaliName = "Ustadzah Sari",
            schoolId = schoolId
        )
        db.childStudentDao().insert(student)

        val link = ParentAccessLink(
            id = 9821L,
            studentId = 9811L,
            studentName = student.name,
            studentNis = student.nis,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            parentName = "Ibu Rahmawati",
            username = "ortu_nayla_9811",
            password = "Ortu#9811",
            assignedWaliUsername = "ustsari_pdf",
            assignedWaliName = "Ustadzah Sari",
            canViewProfile = true,
            canViewSholat = true,
            canViewMengaji = true,
            canViewMedical = true,
            canViewFasting = true,
            canViewHaid = true,
            canViewTabungan = true,
            schoolId = schoolId
        )
        db.parentAccessLinkDao().insert(link)

        val sholatRec = SholatRecord(
            id = SholatHelper.deterministicId(schoolId, 9811L, todayKey, SholatWaktu.DZUHUR),
            dateKey = todayKey,
            dateMillis = SholatHelper.startOfDayMillis(todayKey),
            waktu = SholatWaktu.DZUHUR.code,
            status = SholatStatus.JAMAAH.code,
            studentId = 9811L,
            studentName = student.name,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            recordedBy = "Ustadzah Sari",
            schoolId = schoolId
        )
        db.sholatRecordDao().insert(sholatRec)

        val mengajiRec = MengajiRecord(
            id = 9831L,
            studentId = 9811L,
            studentName = student.name,
            sessionDate = now,
            bookType = "Iqro'",
            jilidOrJuz = "Jilid 5",
            pageOrVerse = "Hal. 14",
            fluencyStatus = MengajiHelper.FLUENCY_LANCAR,
            waliName = "Ustadzah Sari",
            schoolId = schoolId
        )
        db.mengajiRecordDao().insert(mengajiRec)

        val medRec = MedicalRecord(
            studentId = 9811L,
            studentName = student.name,
            studentDormitory = student.dormitory,
            faskes = "UKS Sekolah",
            complaint = "Sakit kepala ringan",
            planDate = now,
            status = "COMPLETED",
            createdBy = "Ustadzah Sari"
        )
        val medId = db.medicalRecordDao().insert(medRec)

        val fastRec = FastingRecord(
            id = 9841L,
            sessionId = 50L,
            sessionTitle = "Puasa Sunnah Kamis",
            sessionDate = now,
            studentId = 9811L,
            studentName = student.name,
            isSahurChecked = true,
            isBukaChecked = true,
            waliName = "Ustadzah Sari",
            schoolId = schoolId
        )
        db.fastingRecordDao().insert(fastRec)

        val haidRec = HaidRecord(
            id = 9851L,
            studentId = 9811L,
            studentName = student.name,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            startDate = now - 86_400_000L,
            endDate = null,
            status = "HAID",
            waliName = "Ustadzah Sari",
            schoolId = schoolId
        )
        db.haidRecordDao().insert(haidRec)

        val tabTx = TabunganTransaction(
            id = 9861L,
            studentId = 9811L,
            studentName = student.name,
            ownerWaliUsername = "ustsari_pdf",
            ownerWaliName = "Ustadzah Sari",
            transactionType = TabunganHelper.TYPE_IN,
            amount = 50_000L,
            title = "Uang Saku Bulanan",
            transactionDate = now,
            schoolId = schoolId
        )
        db.tabunganTransactionDao().insert(tabTx)

        val exp = ParentExpectation(
            id = 9871L,
            studentId = 9811L,
            studentName = student.name,
            parentName = "Ibu Rahmawati",
            characterFocusCsv = "Disiplin & Tanggung Jawab",
            characterExpectation = "Menjaga kerapian dan sholat tepat waktu.",
            schoolId = schoolId,
            updatedAt = now
        )
        db.parentExpectationDao().insert(exp)

        val loginRes = vm.executeLogin("ortu_nayla_9811", "Ortu#9811")
        assertTrue(loginRes is LoginResult.SuccessParent)
        var loops = 0
        while (loops < 30 && vm.activeParentAccessLink.value == null) {
            ShadowLooper.idleMainLooper()
            delay(40)
            loops++
        }

        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                ParentPortalScreen(
                    viewModel = vm,
                    onLogout = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        fun assertPdfButtonWorks(buttonTag: String, listTag: String? = null) {
            if (listTag != null) {
                composeTestRule.onNodeWithTag(listTag).performScrollToNode(hasTestTag(buttonTag))
                composeTestRule.waitForIdle()
            } else {
                composeTestRule.onNodeWithTag(buttonTag).performScrollTo()
                composeTestRule.waitForIdle()
            }
            composeTestRule.onNodeWithTag(buttonTag)
                .assertIsDisplayed()
                .assertHeightIsAtLeast(48.dp)
                .performClick()
            composeTestRule.waitForIdle()
        }

        fun openHomeTileAssertPdfAndReturn(tileTag: String, listTag: String, pdfTag: String, checkFilterAll: Boolean = false) {
            composeTestRule.onNodeWithTag(tileTag).performScrollTo().performClick()
            composeTestRule.waitForIdle()
            if (checkFilterAll) {
                composeTestRule.onNodeWithTag(listTag).performScrollToNode(hasTestTag("${pdfTag}_filter_all"))
                composeTestRule.onNodeWithTag("${pdfTag}_filter_all").performClick()
                composeTestRule.waitForIdle()
                composeTestRule.onNodeWithTag("${pdfTag}_filter_month").performClick()
                composeTestRule.waitForIdle()
            }
            assertPdfButtonWorks(pdfTag, listTag)
            composeTestRule.onNodeWithTag("btn_parent_back_home").performClick()
            composeTestRule.waitForIdle()
        }

        // 1. Ringkasan -> Rapor Terpadu PDF
        assertPdfButtonWorks("btn_parent_home_comprehensive_pdf")

        // 2. Profil Anak
        openHomeTileAssertPdfAndReturn("card_parent_summary_profile", "parent_profile_content", "btn_parent_profile_pdf")

        // 3. Sholat 5 Waktu
        composeTestRule.onNodeWithTag("card_parent_summary_sholat").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_parent_sholat_pdf")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_parent_back_home").performClick()
        composeTestRule.waitForIdle()

        // 4. Mengaji (+ filter chips)
        openHomeTileAssertPdfAndReturn("card_parent_summary_mengaji", "parent_mengaji_list", "btn_parent_mengaji_pdf", checkFilterAll = true)

        // 5. Riwayat Sakit (+ filter chips)
        openHomeTileAssertPdfAndReturn("card_parent_summary_medical", "parent_medical_list", "btn_parent_medical_pdf", checkFilterAll = true)

        // 6. Puasa (+ filter chips)
        openHomeTileAssertPdfAndReturn("card_parent_summary_fasting", "parent_fasting_list", "btn_parent_fasting_pdf", checkFilterAll = true)

        // 7. Riwayat Haid (+ filter chips)
        openHomeTileAssertPdfAndReturn("card_parent_summary_haid", "parent_haid_list", "btn_parent_haid_pdf", checkFilterAll = true)

        // 8. Tabungan
        openHomeTileAssertPdfAndReturn("card_parent_summary_tabungan", "parent_tabungan_list", "btn_parent_tabungan_pdf")

        // 9. Harapan Karakter
        openHomeTileAssertPdfAndReturn("card_parent_summary_harapan", "parent_harapan_content", "btn_parent_harapan_pdf")

        vm.logout()
        db.parentExpectationDao().delete(exp)
        db.tabunganTransactionDao().deleteBySchoolId(schoolId)
        db.haidRecordDao().deleteById(haidRec.id)
        db.fastingRecordDao().deleteBySchoolId(schoolId)
        db.medicalRecordDao().delete(medRec.copy(id = medId))
        db.mengajiRecordDao().deleteBySchoolId(schoolId)
        db.sholatRecordDao().deleteById(sholatRec.id)
        db.parentAccessLinkDao().delete(link)
        db.childStudentDao().delete(student)
        Unit
    }
}
