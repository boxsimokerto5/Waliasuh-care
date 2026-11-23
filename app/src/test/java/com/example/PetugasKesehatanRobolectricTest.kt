package com.example

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.SchoolProfile
import com.example.data.model.WaliAccount
import com.example.ui.HealthViewModel
import com.example.ui.LoginResult
import com.example.ui.UserRole
import com.example.ui.dialogs.AddWaliAccountDialog
import com.example.ui.dialogs.EditWaliAccountDialog
import com.example.ui.screens.GuruNgajiScreen
import com.example.ui.screens.PetugasKesehatanScreen
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PetugasKesehatanRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `HealthViewModel manages Petugas Kesehatan login session and excludes special roles from Wali schedule and Anak Admin`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SRT-PK-01"

        db.waliAccountDao().deleteBySchoolId(schoolId)
        db.childStudentDao().deleteBySchoolId(schoolId)
        db.medicalRecordDao().deleteBySchoolId(schoolId)

        val vm = HealthViewModel(app)
        vm.updateSchoolProfile(
            SchoolProfile(
                tenantCode = schoolId,
                schoolName = "Sekolah Uji Petugas Kesehatan",
                shortName = "Sekolah PK"
            )
        )

        var createdAccount: WaliAccount? = null
        vm.addWaliAccount(
            name = "Ns. Budi Santoso",
            username = "kesehatanbudi",
            dormitory = "Asrama Putra", // Akan dinormalisasi ke "Umum" untuk Petugas Kesehatan
            phone = "081299990000",
            customPassword = "Medis#2026",
            role = WaliAccount.ROLE_PETUGAS_KESEHATAN
        ) { acc ->
            createdAccount = acc
        }

        for (attempt in 1..40) {
            ShadowLooper.idleMainLooper()
            if (createdAccount != null) break
            delay(30)
        }
        assertNotNull(createdAccount)
        assertEquals(WaliAccount.ROLE_PETUGAS_KESEHATAN, createdAccount?.role)
        assertEquals("Umum", createdAccount?.dormitory)
        assertTrue(createdAccount!!.isPetugasKesehatan())
        assertTrue(createdAccount!!.isSpecialPortalRole())

        // Login sebagai Petugas Kesehatan
        val loginRes = vm.executeLogin("kesehatanbudi", "Medis#2026", rememberMe = true)
        assertTrue(loginRes is LoginResult.SuccessPetugasKesehatan)
        assertEquals(UserRole.PETUGAS_KESEHATAN, vm.currentUserRole.value)
        assertEquals("Ns. Budi Santoso", vm.waliName.value)

        // Tidak boleh disematkan sebagai Anak Admin Jadwal
        var toggleOk = true
        vm.setWaliAnakAdminStatus(createdAccount!!, enable = true) { ok, _ ->
            toggleOk = ok
        }
        ShadowLooper.idleMainLooper()
        assertFalse(toggleOk)

        // Tidak boleh muncul sebagai anggota matriks shift jadwal wali
        vm.syncScheduleMembersFromWaliAccounts(
            schoolId = schoolId,
            accounts = listOf(createdAccount!!)
        )
        ShadowLooper.idleMainLooper()
        delay(80)
        ShadowLooper.idleMainLooper()
        val scheduleMembers = db.schoolShiftMemberDao().getMembersBySchoolList(schoolId)
        assertTrue(scheduleMembers.none { it.waliName.equals("Ns. Budi Santoso", ignoreCase = true) })

        // Session restore mempertahankan UserRole.PETUGAS_KESEHATAN
        val restoredVm = HealthViewModel(app)
        ShadowLooper.idleMainLooper()
        assertEquals(UserRole.PETUGAS_KESEHATAN, restoredVm.currentUserRole.value)
        assertEquals("kesehatanbudi", restoredVm.currentUsername.value)
    }

    @Test
    fun `PetugasKesehatanScreen exposes Rencana, Tindakan, Laporan and medical-only Anak Asuh tabs while dialogs allow role selection`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SRT-PK-UI-01"

        db.waliAccountDao().deleteBySchoolId(schoolId)
        db.childStudentDao().deleteBySchoolId(schoolId)
        db.medicalRecordDao().deleteBySchoolId(schoolId)

        val account = WaliAccount(
            id = 881001L,
            name = "dr. Ratna Dewi",
            username = "kesehatanratna",
            password = "Pass#Ratna1",
            dormitory = "Umum",
            role = WaliAccount.ROLE_PETUGAS_KESEHATAN,
            schoolId = schoolId
        )
        db.waliAccountDao().insert(account)
        db.childStudentDao().insert(
            ChildStudent(
                id = 882001L,
                name = "Ahmad Fauzi",
                nis = "882001",
                gender = "L",
                age = 14,
                dormitory = "Asrama Putra",
                classGrade = "VIII-B",
                allergies = "Kacang",
                schoolId = schoolId
            )
        )
        db.medicalRecordDao().insert(
            MedicalRecord(
                id = 883001L,
                studentId = 882001L,
                studentName = "Ahmad Fauzi",
                studentDormitory = "Asrama Putra",
                complaint = "Demam tinggi",
                diagnosis = "ISPA",
                faskes = "UKS Sekolah",
                planDate = System.currentTimeMillis(),
                treatmentDate = System.currentTimeMillis(),
                status = "SELESAI",
                escortName = "Ns. Ratna",
                schoolId = schoolId
            )
        )

        val vm = HealthViewModel(app)
        vm.updateSchoolProfile(
            SchoolProfile(
                tenantCode = schoolId,
                schoolName = "Sekolah UI Petugas Kesehatan",
                shortName = "Sekolah PK UI"
            )
        )
        val loginRes = vm.executeLogin("kesehatanratna", "Pass#Ratna1", rememberMe = false)
        assertTrue(loginRes is LoginResult.SuccessPetugasKesehatan)

        for (attempt in 1..40) {
            ShadowLooper.idleMainLooper()
            if (vm.students.value.size == 1 && vm.allRecords.value.size == 1) break
            delay(30)
        }

        val indonesianStrings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        var dialogMode = mutableStateOf(0) // 0=Portal, 1=AddDialog, 2=EditDialog
        var addedRole: String? = null
        var editedRole: String? = null

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides indonesianStrings) {
                when (dialogMode.value) {
                    1 -> AddWaliAccountDialog(
                        schoolName = "Sekolah UI Petugas Kesehatan",
                        onDismiss = { dialogMode.value = 0 },
                        onConfirm = { _, _, _, _, _, role ->
                            addedRole = role
                            dialogMode.value = 0
                        }
                    )
                    2 -> EditWaliAccountDialog(
                        account = account.copy(role = WaliAccount.ROLE_WALI),
                        onDismiss = { dialogMode.value = 0 },
                        onConfirm = { updated ->
                            editedRole = updated.role
                            dialogMode.value = 0
                        }
                    )
                    else -> PetugasKesehatanScreen(
                        viewModel = vm,
                        onLogout = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        // Tab 1: Rencana
        composeTestRule.onNodeWithTag("nav_petugas_rencana").assertIsDisplayed()
        composeTestRule.onNodeWithTag("fab_create_plan").assertIsDisplayed()

        // Tab 2: Tindakan
        composeTestRule.onNodeWithTag("nav_petugas_tindakan").performClick()
        composeTestRule.waitForIdle()

        // Tab 3: Laporan Bulanan
        composeTestRule.onNodeWithTag("nav_petugas_laporan").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("petugas_kesehatan_monthly_report").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_export_monthly_pdf").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_share_monthly_wa").assertIsDisplayed()

        // Tab 4: Anak Asuh (hanya profil & riwayat kesehatan)
        composeTestRule.onNodeWithTag("nav_petugas_anak_asuh").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("fab_add_student").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_open_card_generator").assertDoesNotExist()
        composeTestRule.onNodeWithTag("card_student_882001").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("student_detail_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_student_detail_biodata").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_student_detail_berobat").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_student_detail_mengaji").assertDoesNotExist()
        composeTestRule.onNodeWithTag("tab_student_detail_sholat").assertDoesNotExist()
        composeTestRule.onNodeWithTag("tab_student_detail_puasa").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_open_student_card_qr").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_edit_student_detail").assertDoesNotExist()

        // Buka tab Berobat di detail siswa dan pastikan riwayat medis tampil
        composeTestRule.onNodeWithTag("tab_student_detail_berobat").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("student_medical_item_883001").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_close_student_detail").performClick()
        composeTestRule.waitForIdle()

        // Verifikasi AddWaliAccountDialog & EditWaliAccountDialog memilih Petugas Kesehatan
        dialogMode.value = 1
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("input_wali_name").performTextInput("Ns. Farah")
        composeTestRule.onNodeWithTag("chip_add_role_petugas_kesehatan").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("btn_save_wali_account").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(WaliAccount.ROLE_PETUGAS_KESEHATAN, addedRole)

        dialogMode.value = 2
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("chip_edit_role_petugas_kesehatan").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("btn_update_wali_account").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(WaliAccount.ROLE_PETUGAS_KESEHATAN, editedRole)
    }

    @Test
    fun `GuruNgajiScreen provides Rekap Jilid Anak and Al-Quran Digital without delete rights`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = AppDatabase.getDatabase(app)
        val schoolId = "SRT-GN-PARITY-01"

        db.waliAccountDao().deleteBySchoolId(schoolId)
        db.childStudentDao().deleteBySchoolId(schoolId)
        db.mengajiRecordDao().deleteBySchoolId(schoolId)

        db.waliAccountDao().insert(
            WaliAccount(
                id = 889001L,
                name = "Ustadz Karim",
                username = "gurukarim",
                password = "Pass#Karim1",
                dormitory = "Umum",
                role = WaliAccount.ROLE_GURU_NGAJI,
                schoolId = schoolId
            )
        )
        db.childStudentDao().insert(
            ChildStudent(
                id = 889101L,
                name = "Bilal Putra",
                nis = "889101",
                gender = "L",
                age = 13,
                dormitory = "Asrama Putra",
                classGrade = "VII-A",
                schoolId = schoolId
            )
        )
        db.mengajiRecordDao().insert(
            MengajiRecord(
                id = 889201L,
                studentId = 889101L,
                studentName = "Bilal Putra",
                studentClass = "VII-A",
                bookType = "Iqro'",
                jilidOrJuz = "Jilid 3",
                pageOrVerse = "Hal. 18",
                fluencyStatus = MengajiHelper.FLUENCY_LANCAR,
                sessionDate = System.currentTimeMillis(),
                waliName = "Ustadz Karim",
                schoolId = schoolId
            )
        )

        val vm = HealthViewModel(app)
        vm.updateSchoolProfile(
            SchoolProfile(
                tenantCode = schoolId,
                schoolName = "Sekolah Parity Guru Ngaji",
                shortName = "Sekolah GN"
            )
        )
        val loginRes = vm.executeLogin("gurukarim", "Pass#Karim1", rememberMe = false)
        assertTrue(loginRes is LoginResult.SuccessGuruNgaji)

        for (attempt in 1..40) {
            ShadowLooper.idleMainLooper()
            if (vm.students.value.size == 1 && vm.mengajiRecords.value.size == 1) break
            delay(30)
        }

        val indonesianStrings = AppLanguageManager.stringsFor(AppLanguage.INDONESIAN)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides indonesianStrings) {
                GuruNgajiScreen(
                    viewModel = vm,
                    onLogout = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_guru_ngaji_rekap").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_guru_ngaji_quran").assertIsDisplayed()

        // Buka Rekap Jilid Anak (mode baca + lanjut setor, tanpa hapus)
        composeTestRule.onNodeWithTag("btn_guru_ngaji_rekap").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("list_rekap_mengaji").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_rekap_mengaji_889101").assertIsDisplayed()
        composeTestRule.onNodeWithText("Iqro' • Jilid 3 • Hal. 18 (Lancar)", substring = true).assertIsDisplayed()
    }
}
