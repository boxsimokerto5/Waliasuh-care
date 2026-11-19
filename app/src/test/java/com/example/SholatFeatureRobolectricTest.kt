package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.HaidRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.SchoolProfile
import com.example.data.model.SholatDisciplineTag
import com.example.data.model.SholatHelper
import com.example.data.model.SholatRecord
import com.example.data.model.SholatStatus
import com.example.data.model.SholatWaktu
import com.example.data.remote.pocketbase.PbParentAccessLinkDto
import com.example.data.remote.pocketbase.PbSholatRecordDto
import com.example.ui.components.QuickMenuTiles
import com.example.ui.dialogs.PilihanAksesOrangTuaSheet
import com.example.ui.dialogs.SholatAttendanceSheet
import com.example.ui.dialogs.StudentDetailDialog
import com.example.util.PdfReportGenerator
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import java.util.Calendar
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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
class SholatFeatureRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `SholatHelper deterministicId, detectActiveWaktu, activeHaid detection, and analytics work accurately`() {
        val id1 = SholatHelper.deterministicId("SRT1-KDR", 42L, "2026-11-19", SholatWaktu.DZUHUR)
        val id2 = SholatHelper.deterministicId("SRT1-KDR", 42L, "2026-11-19", SholatWaktu.DZUHUR)
        val id3 = SholatHelper.deterministicId("SRT1-KDR", 42L, "2026-11-19", SholatWaktu.ASHAR)
        assertEquals(id1, id2)
        assertTrue(id1 != id3)
        assertTrue(id1 > 0L)

        // Verify active waktu hour ranges
        assertEquals(
            SholatWaktu.SUBUH,
            SholatHelper.detectActiveWaktu(Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 5)
                set(Calendar.MINUTE, 15)
            })
        )
        assertEquals(
            SholatWaktu.DZUHUR,
            SholatHelper.detectActiveWaktu(Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 30)
            })
        )
        assertEquals(
            SholatWaktu.ASHAR,
            SholatHelper.detectActiveWaktu(Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 15)
                set(Calendar.MINUTE, 30)
            })
        )
        assertEquals(
            SholatWaktu.MAGHRIB,
            SholatHelper.detectActiveWaktu(Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 18)
                set(Calendar.MINUTE, 15)
            })
        )
        assertEquals(
            SholatWaktu.ISYA,
            SholatHelper.detectActiveWaktu(Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
            })
        )

        val femaleStudent = ChildStudent(
            id = 101L,
            name = "Aisyah Putri",
            nis = "101",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-1",
            schoolId = "SRT1-KDR"
        )
        val maleStudent = ChildStudent(
            id = 102L,
            name = "Budi Santoso",
            nis = "102",
            gender = "L",
            age = 14,
            dormitory = "Asrama Putra",
            classGrade = "VIII-1",
            schoolId = "SRT1-KDR"
        )
        val todayKey = SholatHelper.todayDateKey()
        val todayMillis = SholatHelper.startOfDayMillis(todayKey)
        val activeHaid = HaidRecord(
            id = 701L,
            studentId = 101L,
            studentName = "Aisyah Putri",
            studentClass = "VIII-1",
            studentDorm = "Asrama Putri",
            startDate = todayMillis - 86_400_000L,
            endDate = null,
            status = "HAID",
            waliName = "Ustadzah Sari",
            schoolId = "SRT1-KDR"
        )
        assertNotNull(SholatHelper.findActiveHaidOnDate(femaleStudent, listOf(activeHaid), todayKey))
        assertNull(SholatHelper.findActiveHaidOnDate(maleStudent, listOf(activeHaid), todayKey))

        // Analytics excludes UDZUR_HAID from wajib obligation denominator so female students are not penalized
        val records = listOf(
            SholatRecord(
                id = 1L,
                dateKey = "2026-11-19",
                dateMillis = SholatHelper.startOfDayMillis("2026-11-19"),
                waktu = SholatWaktu.SUBUH.code,
                status = SholatStatus.JAMAAH.code,
                disciplineTag = SholatDisciplineTag.SAF_DEPAN.code,
                studentId = 101L,
                studentName = "Aisyah Putri",
                schoolId = "SRT1-KDR"
            ),
            SholatRecord(
                id = 2L,
                dateKey = "2026-11-19",
                dateMillis = SholatHelper.startOfDayMillis("2026-11-19"),
                waktu = SholatWaktu.DZUHUR.code,
                status = SholatStatus.UDZUR_HAID.code,
                studentId = 101L,
                studentName = "Aisyah Putri",
                schoolId = "SRT1-KDR"
            ),
            SholatRecord(
                id = 3L,
                dateKey = "2026-11-19",
                dateMillis = SholatHelper.startOfDayMillis("2026-11-19"),
                waktu = SholatWaktu.ASHAR.code,
                status = SholatStatus.UDZUR_HAID.code,
                studentId = 101L,
                studentName = "Aisyah Putri",
                schoolId = "SRT1-KDR"
            )
        )
        val analytics = SholatHelper.computeStudentAnalytics(
            students = listOf(femaleStudent),
            records = records,
            monthKey = "2026-11"
        ).first()
        assertEquals(3, analytics.totalLogged)
        assertEquals(1, analytics.jamaahCount)
        assertEquals(2, analytics.udzurHaidCount)
        assertEquals(1, analytics.subuhJamaahCount)
        assertEquals(100, analytics.jamaahRatePercent)
    }

    @Test
    fun `SholatRecordDao CRUD and PocketBase DTO roundtrip preserve all fields`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val dao = db.sholatRecordDao()
            val sample = SholatRecord(
                id = 9901L,
                dateKey = "2026-11-19",
                dateMillis = SholatHelper.startOfDayMillis("2026-11-19"),
                waktu = SholatWaktu.MAGHRIB.code,
                status = SholatStatus.JAMAAH.code,
                disciplineTag = SholatDisciplineTag.TEPAT_WAKTU.code,
                studentId = 55L,
                studentName = "Ahmad Fikri",
                studentNik = "3506150101100099",
                studentNis = "5501",
                studentClass = "IX-2",
                studentDorm = "Asrama Putra",
                studentJenjang = "SMP IX",
                notes = "Shaf pertama",
                inputMethod = SholatHelper.METHOD_MANUAL,
                recordedBy = "Ustadz Hasan",
                recordedByUsername = "uhasan",
                schoolId = "SRT1-KDR",
                createdAt = 1795000000000L,
                updatedAt = 1795000100000L
            )
            dao.insert(sample)
            val loaded = dao.getByStudentDateWaktu(55L, "2026-11-19", SholatWaktu.MAGHRIB.code)
            assertNotNull(loaded)
            assertEquals("Ahmad Fikri", loaded?.studentName)
            assertEquals(SholatDisciplineTag.TEPAT_WAKTU.code, loaded?.disciplineTag)

            val dto = PbSholatRecordDto.fromDomain(sample)
            val restored = dto.toDomainModel("SRT1-KDR")
            assertEquals(sample.id, restored.id)
            assertEquals(sample.dateKey, restored.dateKey)
            assertEquals(sample.waktu, restored.waktu)
            assertEquals(sample.status, restored.status)
            assertEquals(sample.disciplineTag, restored.disciplineTag)
            assertEquals(sample.notes, restored.notes)

            val link = ParentAccessLink(
                id = 12L,
                studentId = 55L,
                studentName = "Ahmad Fikri",
                username = "ortu_fikri",
                password = "Pass#1234",
                canViewSholat = false,
                schoolId = "SRT1-KDR"
            )
            val linkDto = PbParentAccessLinkDto.fromDomain(link)
            assertEquals(false, linkDto.canViewSholat)
            assertEquals(false, linkDto.toDomainModel("SRT1-KDR").canViewSholat)

            dao.delete(sample)
            assertNull(dao.getByStudentDateWaktu(55L, "2026-11-19", SholatWaktu.MAGHRIB.code))
        } finally {
            db.close()
        }
    }

    @Test
    fun `PdfReportGenerator generates valid Sholat recap and per-student report PDFs`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val school = SchoolProfile(
            schoolName = "Sekolah Rakyat Terintegrasi 1 Kediri",
            shortName = "SRT 1 Kediri",
            tenantCode = "SRT1-KDR"
        )
        val student = ChildStudent(
            id = 201L,
            name = "Zaki Al-Farizi",
            nis = "20101",
            gender = "L",
            age = 15,
            dormitory = "Asrama Putra",
            classGrade = "X-1",
            schoolId = "SRT1-KDR"
        )
        val records = listOf(
            SholatRecord(
                id = 11L,
                dateKey = "2026-11-19",
                dateMillis = SholatHelper.startOfDayMillis("2026-11-19"),
                waktu = SholatWaktu.SUBUH.code,
                status = SholatStatus.JAMAAH.code,
                disciplineTag = SholatDisciplineTag.SAF_DEPAN.code,
                studentId = 201L,
                studentName = student.name,
                studentClass = student.classGrade,
                studentDorm = student.dormitory,
                schoolId = "SRT1-KDR"
            ),
            SholatRecord(
                id = 12L,
                dateKey = "2026-11-19",
                dateMillis = SholatHelper.startOfDayMillis("2026-11-19"),
                waktu = SholatWaktu.DZUHUR.code,
                status = SholatStatus.MUNFARID.code,
                studentId = 201L,
                studentName = student.name,
                studentClass = student.classGrade,
                studentDorm = student.dormitory,
                schoolId = "SRT1-KDR"
            )
        )

        val recapTexts = mutableListOf<String>()
        val recapBmps = mutableListOf<Bitmap>()
        PdfReportGenerator.renderSholatRecapPages(
            context = context,
            schoolProfile = school,
            students = listOf(student),
            sholatRecords = records,
            periodLabel = "Harian",
            dateKeyOrMonthKey = "2026-11-19",
            isMonthly = false,
            waliName = "Ustadz Ahmad",
            startNewCanvas = {
                val bmp = Bitmap.createBitmap(595, 842, Bitmap.Config.ARGB_8888)
                recapBmps.add(bmp)
                object : Canvas(bmp) {
                    override fun drawText(text: String, x: Float, y: Float, paint: Paint) {
                        recapTexts.add(text)
                        super.drawText(text, x, y, paint)
                    }
                }
            }
        )
        assertTrue(recapBmps.isNotEmpty())
        val joinedRecap = recapTexts.joinToString(" ")
        assertTrue(joinedRecap.contains("REKAPITULASI HARIAN ABSENSI SHOLAT 5 WAKTU"))
        assertTrue(joinedRecap.contains("Zaki Al-Farizi"))
        assertTrue(joinedRecap.contains("Ustadz Ahmad"))

        val studentTexts = mutableListOf<String>()
        val studentBmps = mutableListOf<Bitmap>()
        PdfReportGenerator.renderSholatStudentReportPages(
            context = context,
            schoolProfile = school,
            student = student,
            sholatRecords = records,
            monthKey = "2026-11",
            waliName = "Ustadz Ahmad",
            startNewCanvas = {
                val bmp = Bitmap.createBitmap(595, 842, Bitmap.Config.ARGB_8888)
                studentBmps.add(bmp)
                object : Canvas(bmp) {
                    override fun drawText(text: String, x: Float, y: Float, paint: Paint) {
                        studentTexts.add(text)
                        super.drawText(text, x, y, paint)
                    }
                }
            }
        )
        assertTrue(studentBmps.isNotEmpty())
        val joinedStudent = studentTexts.joinToString(" ")
        assertTrue(joinedStudent.contains("RAPOR PEMBINAAN SHOLAT 5 WAKTU ANAK ASUH"))
        assertTrue(joinedStudent.contains("Zaki Al-Farizi"))
        assertTrue(joinedStudent.contains("J+"))
        recapBmps.forEach { it.recycle() }
        studentBmps.forEach { it.recycle() }
    }

    @Test
    fun `SholatAttendanceSheet switches tabs and triggers bulk mark Jamaah`() {
        val student = ChildStudent(
            id = 301L,
            name = "Hafiz Ramadhan",
            nis = "30101",
            gender = "L",
            age = 15,
            dormitory = "Asrama Putra",
            classGrade = "X-2",
            schoolId = "SRT1-KDR"
        )
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        var bulkTriggeredCount = 0

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                SholatAttendanceSheet(
                    students = listOf(student),
                    sholatRecords = emptyList(),
                    haidRecords = emptyList(),
                    currentWaliUsername = "u_ahmad",
                    currentWaliName = "Ustadz Ahmad",
                    schoolProfile = SchoolProfile(schoolName = "SRT 1 Kediri", tenantCode = "SRT1-KDR"),
                    onSaveRecord = { _, _, _, _, _, _, _ -> },
                    onBulkMarkJamaah = { targets, _, _, onResult ->
                        bulkTriggeredCount = targets.size
                        onResult(targets.size, 0)
                    },
                    onDeleteRecord = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("sheet_absen_sholat").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_bulk_sholat_jamaah").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, bulkTriggeredCount)

        composeTestRule.onNodeWithTag("tab_sholat_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("list_sholat_monitoring").assertExists()
        composeTestRule.onNodeWithText("Jamaah %").assertIsDisplayed()

        composeTestRule.onNodeWithTag("tab_sholat_2").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_export_sholat_recap_pdf").assertIsDisplayed()
    }

    @Test
    fun `StudentDetailDialog renders SHOLAT tab with Udzur Syari and PDF button`() {
        val student = ChildStudent(
            id = 401L,
            name = "Nadia Aulia",
            nis = "40101",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-1",
            assignedWaliUsername = "ustsari",
            assignedWaliName = "Ustadzah Sari",
            schoolId = "SRT1-KDR"
        )
        val todayKey = SholatHelper.todayDateKey()
        val sholatRec = SholatRecord(
            id = 4001L,
            dateKey = todayKey,
            dateMillis = SholatHelper.startOfDayMillis(todayKey),
            waktu = SholatWaktu.SUBUH.code,
            status = SholatStatus.UDZUR_HAID.code,
            studentId = 401L,
            studentName = student.name,
            notes = "Masa haid hari ke-2",
            recordedBy = "Ustadzah Sari",
            schoolId = "SRT1-KDR"
        )
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                StudentDetailDialog(
                    student = student,
                    studentRecords = emptyList(),
                    sholatRecords = listOf(sholatRec),
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tab_student_detail_sholat").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_student_detail_sholat_pdf").assertIsDisplayed()
    }

    @Test
    fun `PilihanAksesOrangTuaSheet toggles canViewSholat and QuickMenuTiles invokes Absen Sholat`() {
        val student = ChildStudent(
            id = 402L,
            name = "Farhan Ali",
            nis = "40201",
            gender = "L",
            age = 15,
            dormitory = "Asrama Putra",
            classGrade = "IX-1",
            schoolId = "SRT1-KDR"
        )
        val link = ParentAccessLink(
            id = 42L,
            studentId = 402L,
            studentName = student.name,
            username = "ortu_farhan",
            password = "Pass#402",
            canViewSholat = true,
            schoolId = "SRT1-KDR"
        )
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        var capturedSholatPerm: Boolean? = null

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                PilihanAksesOrangTuaSheet(
                    students = listOf(student),
                    parentAccessLinks = listOf(link),
                    isStudentClaimedByCurrentWali = { true },
                    isAdmin = true,
                    onOpenPilihAnakAsuh = {},
                    onEnsureAccessLink = {},
                    onUpdatePermissions = { _, _, _, _, _, _, _, canSholat ->
                        capturedSholatPerm = canSholat
                    },
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("chk_access_sholat_402").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(false, capturedSholatPerm)
    }

    @Test
    fun `QuickMenuTiles opens Absen Sholat tile`() {
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        var sholatOpened = false

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                QuickMenuTiles(
                    onNavigateToPlan = {},
                    onNavigateToDirectTreatment = {},
                    onNavigateToRecords = {},
                    onNavigateToStudents = {},
                    onShowPuskesmasInfo = {},
                    onOpenAbsenSholat = { sholatOpened = true },
                    sholatTodayCount = 12,
                    activeSholatWaktuLabel = "D"
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("quick_menu_carousel_row")
            .performScrollToNode(hasTestTag("tile_absen_sholat"))
        composeTestRule.onNodeWithTag("tile_absen_sholat").performClick()
        composeTestRule.waitForIdle()
        assertTrue(sholatOpened)
    }

    @Test
    fun `ParentPortalScreen renders Pantauan Sholat tile and displays Udzur Syari Haid respectfully`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val db = AppDatabase.getDatabase(app)
        val vm = com.example.ui.HealthViewModel(app)
        val activeSchool = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

        val student = ChildStudent(
            id = 9451L,
            name = "Aisyah Humaira",
            nis = "3199451",
            gender = "P",
            age = 14,
            dormitory = "Asrama Putri",
            classGrade = "VIII-A",
            motherName = "Ibu Aminah",
            assignedWaliUsername = "ustsari",
            assignedWaliName = "Ustadzah Sari",
            schoolId = activeSchool
        )
        db.childStudentDao().insert(student)

        val link = ParentAccessLink(
            id = 9461L,
            studentId = 9451L,
            studentName = student.name,
            studentNis = student.nis,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            parentName = "Ibu Aminah",
            username = "ortu_aisyah_9451",
            password = "Ortu#9451",
            assignedWaliUsername = "ustsari",
            assignedWaliName = "Ustadzah Sari",
            canViewSholat = true,
            schoolId = activeSchool
        )
        db.parentAccessLinkDao().insert(link)

        val todayKey = SholatHelper.todayDateKey()
        val rec = SholatRecord(
            id = SholatHelper.deterministicId(activeSchool, 9451L, todayKey, SholatWaktu.SUBUH),
            dateKey = todayKey,
            dateMillis = SholatHelper.startOfDayMillis(todayKey),
            waktu = SholatWaktu.SUBUH.code,
            status = SholatStatus.UDZUR_HAID.code,
            studentId = 9451L,
            studentName = student.name,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            recordedBy = "Ustadzah Sari",
            schoolId = activeSchool
        )
        db.sholatRecordDao().insert(rec)

        val loginRes = vm.executeLogin("ortu_aisyah_9451", "Ortu#9451")
        assertTrue(loginRes is com.example.ui.LoginResult.SuccessParent)
        var loops = 0
        while (loops < 25 && vm.activeParentAccessLink.value == null) {
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

        composeTestRule.onNodeWithTag("card_parent_summary_sholat").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("parent_sholat_content").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_parent_sholat_pdf").assertIsDisplayed()
        composeTestRule.onNodeWithText("Udzur Syar'i (Haid)").assertExists()

        vm.logout()
        db.sholatRecordDao().deleteById(rec.id)
        db.parentAccessLinkDao().delete(link)
        db.childStudentDao().delete(student)
        Unit
    }

    @Test
    fun `CategoryHubOverlay SHOLAT renders Tabungan-styled hub and routes actions`() {
        val student = ChildStudent(
            id = 505L,
            name = "Bilal Al-Ghifari",
            nis = "50501",
            gender = "L",
            age = 14,
            dormitory = "Asrama Putra",
            classGrade = "VIII-B",
            schoolId = "SRT1-KDR"
        )
        val todayKey = SholatHelper.todayDateKey()
        val activeWaktu = SholatHelper.detectActiveWaktu()
        val rec = SholatRecord(
            id = SholatHelper.deterministicId("SRT1-KDR", 505L, todayKey, activeWaktu),
            dateKey = todayKey,
            dateMillis = SholatHelper.startOfDayMillis(todayKey),
            waktu = activeWaktu.code,
            status = SholatStatus.JAMAAH.code,
            studentId = 505L,
            studentName = student.name,
            studentClass = student.classGrade,
            studentDorm = student.dormitory,
            schoolId = "SRT1-KDR"
        )
        val strings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        var capturedTab = -1
        var capturedScanner = false
        var capturedScope = ""
        var capturedStudent: ChildStudent? = null
        var openedParentAccess = false

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides strings) {
                com.example.ui.components.CategoryHubOverlay(
                    category = com.example.ui.components.DashboardCategoryHub.SHOLAT,
                    schoolProfile = SchoolProfile(schoolName = "SRT 1 Kediri", shortName = "SRT 1 Kediri", tenantCode = "SRT1-KDR"),
                    activePlans = emptyList(),
                    inpatientCount = 0,
                    fastingSessions = emptyList(),
                    fastingRecords = emptyList(),
                    activeHaidRecords = emptyList(),
                    suciRecords = emptyList(),
                    shiftMessages = emptyList(),
                    students = listOf(student),
                    claimedFosterStudents = listOf(student),
                    sholatRecords = listOf(rec),
                    onDismiss = {},
                    onNavigateToPlan = {},
                    onNavigateToRecords = {},
                    onOpenCreatePlan = {},
                    onOpenDirectTreatment = {},
                    onOpenFaskesInfo = {},
                    onOpenInputPuasa = {},
                    onOpenDaftarPuasa = {},
                    onOpenChecklistPuasa = {},
                    onOpenRankingPuasa = {},
                    onOpenInputHaid = {},
                    onOpenDaftarHaid = {},
                    onOpenDaftarSuci = {},
                    onOpenFiqihHaid = {},
                    onOpenShiftMessages = {},
                    onOpenCreateShiftMessage = {},
                    onOpenShiftHistory = {},
                    onOpenPilihanAksesOrtu = { openedParentAccess = true },
                    onOpenAbsenSholatSheet = { tabIdx, autoScan, scope, target ->
                        capturedTab = tabIdx
                        capturedScanner = autoScan
                        capturedScope = scope
                        capturedStudent = target
                    }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("category_hub_overlay_sholat").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_sholat_hub_info").assertExists()
        composeTestRule.onNodeWithTag("card_sholat_totals").assertExists()
        composeTestRule.onNodeWithTag("tile_sholat_scan_qr").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(0, capturedTab)
        assertTrue(capturedScanner)

        composeTestRule.onNodeWithTag("tile_sholat_pantauan").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, capturedTab)
        assertEquals(false, capturedScanner)

        composeTestRule.onNodeWithTag("tile_sholat_akses_ortu").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertTrue(openedParentAccess)

        composeTestRule.onNodeWithTag("card_sholat_student_505").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(505L, capturedStudent?.id)
        assertEquals("MINE", capturedScope)
    }
}
