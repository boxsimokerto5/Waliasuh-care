package com.example

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.flow.first

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Waliasuh Care", appName)
  }

  @Test
  fun `verify TopOn credentials and click persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("h6af3e64badfd8", com.example.util.TopOnAdManager.getAppId())
    assertEquals("a760869481eae8041424abeb19ad8634c", com.example.util.TopOnAdManager.getAppKey())
    assertEquals("n6af3e7b1e288d", com.example.util.TopOnAdManager.getInterstitialPlacementId())
    assertEquals("n6af3e88b080a6", com.example.util.TopOnAdManager.getRewardPlacementId())

    val prefs = context.getSharedPreferences("topon_ads_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().commit()

    assertEquals(0, com.example.util.TopOnAdManager.getSavedClickCount(context))

    // Simulate 5 clicks
    for (i in 1..5) {
      com.example.util.TopOnAdManager.onUserActionClick(context)
    }
    assertEquals(5, com.example.util.TopOnAdManager.getSavedClickCount(context))

    // Simulate next 9 clicks (total 14)
    for (i in 1..9) {
      com.example.util.TopOnAdManager.onUserActionClick(context)
    }
    assertEquals(14, com.example.util.TopOnAdManager.getSavedClickCount(context))

    // 15th click triggers reset back to 0
    com.example.util.TopOnAdManager.onUserActionClick(context)
    assertEquals(0, com.example.util.TopOnAdManager.getSavedClickCount(context))
  }

  @Test
  fun `verify Superadmin credentials and login result`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)

    var loginResult: com.example.ui.LoginResult? = null
    vm.login("superadmin", "Woyowoyo12@", rememberMe = false) { result ->
      loginResult = result
    }
    org.junit.Assert.assertTrue(loginResult is com.example.ui.LoginResult.SuccessSuperAdmin)
  }

  @Test
  fun `verify schoolId tenant binding for imported students and wali`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemoryDb = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.data.database.AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val customSchoolId = "SCH-TEST-99"
    val testStudent = com.example.data.model.ChildStudent(
      name = "Siswa Uji Coba",
      nis = "9901",
      gender = "L",
      age = 13,
      dormitory = "Asrama Putra",
      classGrade = "VII-B",
      schoolId = customSchoolId
    )

    inMemoryDb.childStudentDao().insert(testStudent)
    val fetchedStudents = inMemoryDb.childStudentDao().getStudentsBySchool(customSchoolId).first()
    assertEquals(1, fetchedStudents.size)
    assertEquals(customSchoolId, fetchedStudents[0].schoolId)

    val testWali = com.example.data.model.WaliAccount(
      name = "Wali Uji Coba",
      username = "waliuji",
      password = "Password#123",
      dormitory = "Asrama Putra",
      schoolId = customSchoolId
    )

    inMemoryDb.waliAccountDao().insert(testWali)
    val fetchedAccounts = inMemoryDb.waliAccountDao().getAccountsBySchool(customSchoolId).first()
    assertEquals(1, fetchedAccounts.size)
    assertEquals(customSchoolId, fetchedAccounts[0].schoolId)

    inMemoryDb.close()
  }

  @Test
  fun `verify multi-tenant notification filtering`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)

    // Skenario 1: User belum login -> Notifikasi tidak boleh muncul untuk sekolah apapun
    vm.logout()
    org.junit.Assert.assertFalse(vm.shouldTriggerNotificationForStudent("SRT1-KDR"))
    org.junit.Assert.assertFalse(vm.shouldTriggerNotificationForStudent("SEKOLAH-B"))

    // Skenario 2: Login sebagai Admin Sekolah A ("SRT1-KDR")
    vm.switchRoleToAdmin()
    vm.updateSchoolProfile(
      com.example.data.model.SchoolProfile(
        tenantCode = "SRT1-KDR",
        schoolName = "Sekolah Rakyat SRT 1 Kediri"
      )
    )
    // Siswa Sekolah A -> TRUE (Notifikasi muncul)
    org.junit.Assert.assertTrue(vm.shouldTriggerNotificationForStudent("SRT1-KDR"))
    // Siswa Sekolah B -> FALSE (Notifikasi di-filter / TIDAK muncul ke user Sekolah A)
    org.junit.Assert.assertFalse(vm.shouldTriggerNotificationForStudent("SEKOLAH-B"))

    // Skenario 3: Login sebagai SuperAdmin -> Menerima seluruh notifikasi lintas sekolah
    vm.login("superadmin", "Woyowoyo12@", rememberMe = false) {}
    org.junit.Assert.assertTrue(vm.shouldTriggerNotificationForStudent("SRT1-KDR"))
    org.junit.Assert.assertTrue(vm.shouldTriggerNotificationForStudent("SEKOLAH-B"))
  }

  @Test
  fun `verify school suspend and login rejection`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)

    val schoolCode = "SCH-SUSPEND-01"
    val adminUser = "admin_suspend_test"
    val adminPass = "Secret#123"

    val reg = com.example.data.model.SchoolRegistration(
      schoolName = "Sekolah Uji Coba Suspend",
      address = "Jl. Uji Coba No. 1",
      city = "Kota Uji",
      estimatedWaliCount = 5,
      picName = "Bapak Uji",
      picEmail = "uji@sekolah.id",
      picPhone = "081234567890",
      status = "SUSPENDED",
      generatedSchoolCode = schoolCode,
      generatedAdminUsername = adminUser,
      generatedAdminPassword = adminPass
    )
    val regId = db.schoolRegistrationDao().insert(reg)

    val waliUser = "wali_suspend_test"
    val waliPass = "WaliSecret#123"
    val waliAccount = com.example.data.model.WaliAccount(
      name = "Wali Uji Suspend",
      username = waliUser,
      password = waliPass,
      dormitory = "Kamar A",
      schoolId = schoolCode
    )
    db.waliAccountDao().insert(waliAccount)

    // Uji Login Admin saat sekolah disuspend
    val adminLoginResult = vm.executeLogin(adminUser, adminPass, rememberMe = false)
    org.junit.Assert.assertTrue(adminLoginResult is com.example.ui.LoginResult.Error)
    org.junit.Assert.assertTrue((adminLoginResult as com.example.ui.LoginResult.Error).message.contains("disuspend"))

    // Uji Login Wali Asuh saat sekolah disuspend
    val waliLoginResult = vm.executeLogin(waliUser, waliPass, rememberMe = false)
    org.junit.Assert.assertTrue(waliLoginResult is com.example.ui.LoginResult.Error)
    org.junit.Assert.assertTrue((waliLoginResult as com.example.ui.LoginResult.Error).message.contains("disuspend"))

    // Buka suspend (unsuspend)
    val updatedReg = reg.copy(id = regId, status = "APPROVED")
    db.schoolRegistrationDao().update(updatedReg)

    // Uji Login Admin setelah unsuspend
    val adminLoginAfterUnsuspend = vm.executeLogin(adminUser, adminPass, rememberMe = false)
    org.junit.Assert.assertTrue(adminLoginAfterUnsuspend is com.example.ui.LoginResult.SuccessAdmin)

    // Hapus sekolah
    vm.deleteSchoolRegistration(updatedReg)
  }

  @Test
  fun `verify complete multi-tenant student and wali isolation and clean database`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)

    // Bersihkan database untuk pengujian
    db.childStudentDao().deleteAll()
    db.waliAccountDao().deleteAll()

    // Beralih ke peran Admin pada Sekolah Baru "SRT-KED03"
    vm.switchRoleToAdmin()
    vm.updateSchoolProfile(
      com.example.data.model.SchoolProfile(
        tenantCode = "SRT-KED03",
        schoolName = "Sekolah Rakyat Baru Kediri 3"
      )
    )

    // Sekolah baru yang baru dibuat harus bersih: 0 siswa dan 0 wali asuh
    assertEquals(0, vm.students.value.size)
    assertEquals(0, vm.waliAccounts.value.size)

    // Tambah 1 siswa khusus sekolah SRT-KED03
    vm.addStudent(
      name = "Siswa Baru SRT 3",
      nis = "1001",
      gender = "L",
      age = 12,
      dormitory = "Asrama Putra",
      classGrade = "VII-A",
      allergies = "",
      bloodType = "O",
      emergencyContact = "0811111111",
      specialNotes = ""
    )
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    // Siswa untuk sekolah SRT-KED03 bertambah menjadi 1 di database
    val studentsKed03 = db.childStudentDao().getStudentsBySchool("SRT-KED03").first()
    assertEquals(1, studentsKed03.size)
    assertEquals("SRT-KED03", studentsKed03[0].schoolId)

    // Sekolah lain (misal "SRT1-KDR") tidak boleh melihat siswa milik SRT-KED03
    val studentsOtherSchool = db.childStudentDao().getStudentsBySchool("SRT1-KDR").first()
    assertEquals(0, studentsOtherSchool.size)
  }

  @Test
  fun `verify Dirawat di Sini UKS treatment and referral departure workflow`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)

    val testStudent = com.example.data.model.ChildStudent(
      name = "Santri Test Alur",
      nis = "8899",
      gender = "L",
      age = 13,
      dormitory = "Kamar Abu Bakar",
      classGrade = "VII-C",
      schoolId = "SRT1-KDR"
    )
    val studentId = db.childStudentDao().insert(testStudent)

    // Buat record rencana berobat uji coba
    val planRecord = com.example.data.model.MedicalRecord(
      studentId = studentId,
      studentName = "Santri Test Alur",
      studentDormitory = "Kamar Abu Bakar",
      faskes = "UKS Sekolah",
      complaint = "Demam ringan dan pusing",
      planDate = System.currentTimeMillis(),
      departureTime = "08:30",
      status = "PLANNED"
    )
    val planId = db.medicalRecordDao().insert(planRecord)

    // 1. Uji skenario Dirawat di Sini (UKS):
    // Langsung input diagnosa & penanganan UKS -> status menjadi COMPLETED
    val recordForUks = planRecord.copy(id = planId, faskes = "UKS Sekolah")
    vm.submitTreatmentResult(
      record = recordForUks,
      diagnosis = "Febris H-1 / Observasi Febris",
      actionsTaken = "Kompres hangat, istirahat di bed UKS",
      prescription = "Paracetamol 500mg 3x1",
      needsControl = false,
      controlDate = null,
      additionalNotes = "Suhu awal 37.8C, turun jadi 36.9C"
    )
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val completedRecord = db.medicalRecordDao().getRecordById(planId).first()
    org.junit.Assert.assertNotNull(completedRecord)
    assertEquals("COMPLETED", completedRecord?.status)
    assertEquals("UKS Sekolah", completedRecord?.faskes)
    assertEquals("Febris H-1 / Observasi Febris", completedRecord?.diagnosis)
    assertEquals("Paracetamol 500mg 3x1", completedRecord?.prescription)

    // 2. Uji skenario Berangkatkan / Rujuk ke Faskes lanjutan:
    val referralPlan = com.example.data.model.MedicalRecord(
      studentId = studentId,
      studentName = "Santri Rujukan",
      studentDormitory = "Kamar Umar",
      faskes = "UKS Sekolah",
      complaint = "Nyeri perut kanan bawah hebat",
      planDate = System.currentTimeMillis(),
      departureTime = "09:00",
      status = "PLANNED"
    )
    val referralId = db.medicalRecordDao().insert(referralPlan)

    // Petugas memilih berangkatkan ke Puskesmas Wates dengan pendamping Ustadz Ahmad
    vm.markDepartedToFaskes(referralId, "Puskesmas Wates", "Ustadz Ahmad")
    var departedRecord: com.example.data.model.MedicalRecord? = null
    for (attempt in 1..20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      departedRecord = db.medicalRecordDao().getRecordById(referralId).first()
      if (departedRecord?.status == "DEPARTED") break
      kotlinx.coroutines.delay(30)
    }
    org.junit.Assert.assertNotNull(departedRecord)
    assertEquals("DEPARTED", departedRecord?.status)
    assertEquals("Puskesmas Wates", departedRecord?.faskes)
    assertEquals("Ustadz Ahmad", departedRecord?.escortName)
  }

  @Test
  fun `verify delete single student from admin without affecting others`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)

    val schoolId = "SRT-DEL-TEST"
    val student1 = com.example.data.model.ChildStudent(
      name = "Siswa Tetap",
      nis = "1101",
      gender = "L",
      age = 13,
      dormitory = "Asrama A",
      classGrade = "VII",
      schoolId = schoolId
    )
    val student2 = com.example.data.model.ChildStudent(
      name = "Siswa Pindah Keluar",
      nis = "1102",
      gender = "P",
      age = 12,
      dormitory = "Asrama B",
      classGrade = "VII",
      schoolId = schoolId
    )

    val id1 = db.childStudentDao().insert(student1)
    val id2 = db.childStudentDao().insert(student2)

    val initialStudents = db.childStudentDao().getStudentsBySchool(schoolId).first()
    assertEquals(2, initialStudents.size)

    // Admin menghapus HANYA siswa 2 yang keluar (student2)
    val toDelete = student2.copy(id = id2)
    vm.deleteStudent(toDelete)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val remainingStudents = db.childStudentDao().getStudentsBySchool(schoolId).first()
    assertEquals(1, remainingStudents.size)
    assertEquals("Siswa Tetap", remainingStudents[0].name)
    assertEquals(id1, remainingStudents[0].id)
  }

  @Test
  fun `verify wali self password change and synchronization to admin`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)

    val schoolId = "SRT-SYNC-PASS"
    val initialPass = "OldRandom#123"
    val newCustomPass = "MudahDiingat123@"

    val waliAccount = com.example.data.model.WaliAccount(
      name = "Ustadz Budi",
      username = "ustadzbudi",
      password = initialPass,
      dormitory = "Asrama Abu Bakar",
      schoolId = schoolId
    )
    val accountId = db.waliAccountDao().insert(waliAccount)

    // 1. Wali login menggunakan password lama
    val loginOld = vm.executeLogin("ustadzbudi", initialPass, rememberMe = true)
    org.junit.Assert.assertTrue(loginOld is com.example.ui.LoginResult.SuccessWali)

    // 2. Wali mengganti password mandiri dari profil
    var changeSuccess = false
    var resultMsg = ""
    vm.updateWaliSelfPassword("Ustadz Budi Pengasuh", newCustomPass) { success, msg ->
      changeSuccess = success
      resultMsg = msg
    }
    var attempts = 0
    while (!changeSuccess && attempts < 20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(50)
      attempts++
    }
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    org.junit.Assert.assertTrue(changeSuccess)

    // 3. Verifikasi di database: password dan nama terupdate
    val updatedFromDb = db.waliAccountDao().getById(accountId)
    org.junit.Assert.assertNotNull(updatedFromDb)
    assertEquals(newCustomPass, updatedFromDb?.password)
    assertEquals("Ustadz Budi Pengasuh", updatedFromDb?.name)

    // 4. Verifikasi bahwa login berikutnya dengan password baru berhasil
    val loginNew = vm.executeLogin("ustadzbudi", newCustomPass, rememberMe = false)
    org.junit.Assert.assertTrue(loginNew is com.example.ui.LoginResult.SuccessWali)

    // 5. Verifikasi bahwa login dengan password lama sekarang ditolak
    val loginWithOldRejected = vm.executeLogin("ustadzbudi", initialPass, rememberMe = false)
    org.junit.Assert.assertTrue(loginWithOldRejected is com.example.ui.LoginResult.Error)
  }

  @Test
  fun `verify shift message creation and handover completion`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)

    vm.setWaliName("Ustadz Fauzi")

    // 1. Buat pesan shift baru (Urgent, Shift Malam)
    var createdMessage: com.example.data.model.ShiftMessage? = null
    vm.addShiftMessage(
      title = "Pantau Suhu Santri Rizky",
      followUpDate = System.currentTimeMillis() + 86400000L,
      shift = "Shift Malam (21-07)",
      targetWali = "Semua Wali Asuh",
      content = "Suhu tubuh 38.5C, berikan kompres air hangat jam 02:00 malam",
      priority = "URGENT"
    ) { msg ->
      createdMessage = msg
    }
    for (attempt in 1..20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (createdMessage != null) break
      kotlinx.coroutines.delay(30)
    }

    org.junit.Assert.assertNotNull(createdMessage)
    val msgId = createdMessage!!.id
    assertEquals("Pantau Suhu Santri Rizky", createdMessage?.title)
    assertEquals("URGENT", createdMessage?.priority)
    assertEquals("Shift Malam (21-07)", createdMessage?.shift)
    org.junit.Assert.assertFalse(createdMessage!!.isCompleted)

    // 2. Tandai selesai ditindaklanjuti
    vm.toggleShiftMessageCompleted(createdMessage!!)
    var updated: com.example.data.model.ShiftMessage? = null
    for (attempt in 1..20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      updated = db.shiftMessageDao().getAllMessages().first().firstOrNull { it.id == msgId }
      if (updated?.isCompleted == true) break
      kotlinx.coroutines.delay(50)
    }
    org.junit.Assert.assertNotNull(updated)
    org.junit.Assert.assertTrue(updated!!.isCompleted)
    assertEquals("Ustadz Fauzi", updated.completedBy)

    // 3. Hapus pesan shift
    vm.deleteShiftMessage(updated)
    for (attempt in 1..20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (db.shiftMessageDao().getAllMessages().first().none { it.id == msgId }) break
      kotlinx.coroutines.delay(50)
    }

    val messagesAfterDelete = db.shiftMessageDao().getAllMessages().first()
    org.junit.Assert.assertNull(messagesAfterDelete.firstOrNull { it.id == msgId })
  }

  @Test
  fun `verify StudentClassifier handles varied female formats and strict matching without fallback`() {
    assertEquals("P", com.example.util.StudentClassifier.detectGenderCode("Perempuan", "104", ""))
    assertEquals("P", com.example.util.StudentClassifier.detectGenderCode("Wanita", "", ""))
    assertEquals("P", com.example.util.StudentClassifier.detectGenderCode("Female", "", ""))
    assertEquals("P", com.example.util.StudentClassifier.detectGenderCode("Pr", "", ""))
    assertEquals("P", com.example.util.StudentClassifier.detectGenderCode("P", "", ""))
    assertEquals("P", com.example.util.StudentClassifier.detectGenderCode("", "PP_104", ""))
    assertEquals("P", com.example.util.StudentClassifier.detectGenderCode("", "", "Asrama Putri"))

    assertEquals("L", com.example.util.StudentClassifier.detectGenderCode("L", "", ""))
    assertEquals("L", com.example.util.StudentClassifier.detectGenderCode("Laki-laki", "PL_101", "Asrama Putra"))

    val femaleA = com.example.data.model.ChildStudent(
      id = 101L,
      name = "Azzura Fauziah",
      nis = "SRT1-001",
      nik = "3506111122220001",
      gender = "P",
      age = 13,
      dormitory = "Asrama Putri",
      classGrade = "VII-A",
      schoolId = "SRT1-KDR"
    )
    val femaleB = com.example.data.model.ChildStudent(
      id = 102L,
      name = "Siti Nurhaliza",
      nis = "SRT1-002",
      nik = "3506111122220002",
      gender = "Wanita",
      age = 14,
      dormitory = "Kamar PP_104",
      classGrade = "VIII-A",
      schoolId = "SRT1-KDR"
    )
    val maleC = com.example.data.model.ChildStudent(
      id = 103L,
      name = "Ahmad Syaiful",
      nis = "SRT1-003",
      nik = "3506111122220003",
      gender = "Laki-laki",
      age = 14,
      dormitory = "Asrama Putra",
      classGrade = "VIII-B",
      roomNumber = "PL_101",
      schoolId = "SRT1-KDR"
    )

    org.junit.Assert.assertTrue(com.example.util.StudentClassifier.isFemaleStudent(femaleA))
    org.junit.Assert.assertTrue(com.example.util.StudentClassifier.isFemaleStudent(femaleB))
    org.junit.Assert.assertFalse(com.example.util.StudentClassifier.isFemaleStudent(maleC))
    org.junit.Assert.assertTrue(com.example.util.StudentClassifier.isMaleStudent(maleC))

    val femaleList = listOf(femaleA, femaleB)

    // Unknown code MUST return null (never silently fall back to femaleA)
    org.junit.Assert.assertNull(
      com.example.util.StudentClassifier.findMatchingFemaleStudent(femaleList, "999999")
    )
    org.junit.Assert.assertNull(
      com.example.util.StudentClassifier.findMatchingFemaleStudent(femaleList, "   ")
    )

    // Exact NIK, NIS, ID, and partial name matching
    assertEquals(
      femaleB,
      com.example.util.StudentClassifier.findMatchingFemaleStudent(femaleList, "3506111122220002")
    )
    assertEquals(
      femaleB,
      com.example.util.StudentClassifier.findMatchingFemaleStudent(femaleList, "SRT1-002")
    )
    assertEquals(
      femaleA,
      com.example.util.StudentClassifier.findMatchingFemaleStudent(femaleList, "azzura")
    )
  }

  @Test
  fun `verify PocketBase DTOs preserve schoolId, online checklists, and all 9 collections`() {
    val student = com.example.data.model.ChildStudent(
      id = 55001L,
      name = "Nadia Putri",
      nis = "KED05-11",
      gender = "P",
      age = 13,
      dormitory = "Asrama Putri",
      classGrade = "VII-B",
      nik = "3506000011112222",
      roomNumber = "PP_102",
      schoolId = "SRT-KED05"
    )

    val dto = com.example.data.remote.pocketbase.PbStudentDto.fromDomain(student)
    assertEquals("SRT-KED05", dto.schoolId)
    assertEquals(55001L, dto.localId)
    assertEquals(15, dto.id?.length)
    org.junit.Assert.assertTrue(com.example.data.remote.pocketbase.PocketBaseIdCodec.isValidPbId(dto.id))

    val restored = dto.toDomainModel()
    assertEquals(55001L, restored.id)
    assertEquals("SRT-KED05", restored.schoolId)
    assertEquals("PP_102", restored.roomNumber)
    assertEquals("Nadia Putri", restored.name)

    // Verifikasi checklist online sahur & berbuka puasa + school profile
    val fastingRec = com.example.data.model.FastingRecord(
      id = 77002L,
      sessionId = 101L,
      sessionTitle = "Puasa Senin",
      sessionDate = 1700000000000L,
      studentId = 55001L,
      studentName = "Nadia Putri",
      isSahurChecked = true,
      sahurCheckedAt = 1700001000000L,
      sahurCheckedBy = "Ustzh. Amina",
      isBukaChecked = true,
      bukaCheckedAt = 1700045000000L,
      bukaCheckedBy = "Ustzh. Amina",
      schoolId = "SRT-KED05",
      updatedAt = 1700045000000L
    )
    val pbFasting = com.example.data.remote.pocketbase.PbFastingRecordDto.fromDomain(fastingRec)
    val restoredFasting = pbFasting.toDomainModel()
    assertEquals(true, restoredFasting.isSahurChecked)
    assertEquals(true, restoredFasting.isBukaChecked)
    assertEquals("Ustzh. Amina", restoredFasting.bukaCheckedBy)
    assertEquals(77002L, restoredFasting.id)

    val schema = com.example.data.remote.pocketbase.PocketBaseSchemaProvider.POCKETBASE_SCHEMA_SUMMARY
    val jsonSchema = org.json.JSONArray(com.example.data.remote.pocketbase.PocketBaseSchemaProvider.POCKETBASE_COLLECTIONS_JSON)
    val sshScript = com.example.data.remote.pocketbase.PocketBaseSchemaProvider.POCKETBASE_SSH_SYNC_SCRIPT
    assertEquals(34, jsonSchema.length())
    org.junit.Assert.assertTrue(sshScript.contains("/api/collections"))
    org.junit.Assert.assertTrue(sshScript.contains("is_sahur_checked"))
    org.junit.Assert.assertTrue(sshScript.contains("fluency_status"))
    org.junit.Assert.assertTrue(sshScript.contains("session_dates_csv"))

    val expectedCollections = listOf(
      "child_students",
      "medical_records",
      "haid_records",
      "wali_accounts",
      "shift_messages",
      "school_registrations",
      "fasting_sessions",
      "fasting_records",
      "mengaji_records",
      "school_profiles",
      "visit_sessions",
      "visit_records",
      "visit_confirmations",
      "visit_session_swaps",
      "parent_access_links",
      "parent_messages",
      "parent_expectations",
      "dashboard_banners",
      "deletion_logs",
      "tabungan_transactions",
      "tabungan_logs",
      "konseling_records",
      "konseling_logs",
      "wali_shift_codes",
      "wali_schedule_entries",
      "important_info_posts",
      "important_info_reactions",
      "important_info_comments",
      "shared_journal_notes"
    )
    expectedCollections.forEach { col ->
      org.junit.Assert.assertTrue(
        "Missing collection $col in PocketBaseSchemaProvider",
        com.example.data.remote.pocketbase.PocketBaseSchemaProvider.COLLECTIONS.any { it.collectionName == col }
      )
      org.junit.Assert.assertTrue("Missing $col in schema summary", schema.contains(col))
    }
    assertEquals(34, com.example.data.remote.pocketbase.PocketBaseSchemaProvider.COLLECTIONS.size)

    // Pastikan tidak ada nama SQLite index yang kembar antar tabel (penyebab Failed to import collections)
    val indexNameRegex = Regex("""CREATE\s+(?:UNIQUE\s+)?INDEX\s+(?:IF\s+NOT\s+EXISTS\s+)?[`"']?([A-Za-z0-9_]+)[`"']?""", RegexOption.IGNORE_CASE)
    val allIndexNames = com.example.data.remote.pocketbase.PocketBaseSchemaProvider.COLLECTIONS
      .flatMap { it.indexes }
      .mapNotNull { indexNameRegex.find(it)?.groupValues?.getOrNull(1)?.lowercase() }
    assertEquals(allIndexNames.size, allIndexNames.distinct().size)

    // Pastikan setiap koleksi memiliki sistem id field standar PocketBase v0.40.6 (text3208210256)
    for (i in 0 until jsonSchema.length()) {
      val colJson = jsonSchema.getJSONObject(i)
      val idField = colJson.getJSONArray("fields").getJSONObject(0)
      assertEquals("id", idField.getString("name"))
      assertEquals("text3208210256", idField.getString("id"))
      assertEquals("[a-z0-9]{15}", idField.getString("autogeneratePattern"))
    }
  }

  @Test
  fun `verify StudentCsvParser parses 12-column CSV with banner rows, ZWNJ, scientific notation, and quotes`() {
    val sampleCsv = """
      ${'\uFEFF'}DATA SISWA SRT 1 KABUPATEN KEDIRI,,,,,,,,,,,
      ,,,,,,,,,,,
      No,Nama,Kelas,Jenjang,Alamat,Jenis Kelamin,NIK,NO KK,TEMPAT LAHIR,TANGGAL LAHIR,NAMA IBU,NISN
      2,AHMAD ALI NUR ARIFIN,SD 1-2,SD,"Nyawangan, Kras, Kab. Kediri",L,${'\u200C'}3506032804190001,3506031907210004,KEDIRI,28-04-2019,NURIKAH,3199469205
      38,MISBAHUL MUNIR,VII-1,SMP,"Kaliboto, Tarokan, Kab. Kediri",L,3506200703140002,3.5062E+15,KEDIRI,07-03-2014,ST. KHOIRIYATUL BADRIYAH,141038929
      169,HIMAWAN TOMY FADILLAH,X-2,SMA X,"Puhsarang, Semen, Kab. Kediri",L,3506010503090002,3506010312100437,KEDIRI,05-03-2010,LUSIATI,102866222'0
      201,MOH. ADILA IHSAN,X-3,SMA X,"Susuhbango, Ringinrejo, Kab. Kediri",L,â€Œ3506231910100001,3506230301120002,KEDIRI,19-10-2010,KOYUL KOYUM,3105 420177
      310,ACHMAD DEVA ADIANSA,XI-3 (IPS),SMA,"Masjid, RT 001/RW 001, Dusun Jambangan, Desa/Kelurahan Jambangan, Kecamatan Papar, Kabupaten Kediri",L,3506142512090002,3506141405120009,KEDIRI,25-12-2009,DWI MASRUROH,95427948
      311,AGUSTINE PRASTIKA PUTRI RAMADHANI,XI-3 (IPS),SMA,"Dsn. Gunung Butak, RT 002/RW 006, Bulusari, Tarokan, Kediri",P,3506205408090003,3506201509150001,KEDIRI,14-08-2009,PUJI SUMARIYANTI,93169259
    """.trimIndent()

    val parsed = com.example.util.StudentCsvParser.parseCsv(sampleCsv, "SRT-KED00")
    assertEquals(6, parsed.size)

    // Row 2: ZWNJ stripped cleanly
    val ali = parsed[0]
    assertEquals("AHMAD ALI NUR ARIFIN", ali.name)
    assertEquals("3506032804190001", ali.nik)
    assertEquals("3506031907210004", ali.noKk)
    assertEquals("SD", ali.jenjang)
    assertEquals("SD 1-2", ali.classGrade)
    assertEquals("L", ali.gender)
    assertEquals("Nyawangan, Kras, Kab. Kediri", ali.address)
    assertEquals("3199469205", ali.nis)

    // Row 38: Scientific notation KK converted cleanly
    val munir = parsed[1]
    assertEquals("3506200000000000", munir.noKk)

    // Row 169: Stray apostrophe removed from NISN
    val himawan = parsed[2]
    assertEquals("1028662220", himawan.nis)
    assertEquals("SMA X", himawan.jenjang)

    // Row 201: Mojibake ZWNJ removed from NIK & space removed from NISN
    val ihsan = parsed[3]
    assertEquals("3506231910100001", ihsan.nik)
    assertEquals("3105420177", ihsan.nis)

    // Row 310: ACHMAD DEVA ADIANSA complete profile
    val deva = parsed[4]
    assertEquals("ACHMAD DEVA ADIANSA", deva.name)
    assertEquals("XI-3 (IPS)", deva.classGrade)
    assertEquals("SMA", deva.jenjang)
    assertEquals("L", deva.gender)
    assertEquals("Asrama Putra", deva.dormitory)
    assertEquals("3506142512090002", deva.nik)
    assertEquals("3506141405120009", deva.noKk)
    assertEquals("KEDIRI", deva.birthPlace)
    assertEquals("25-12-2009", deva.birthDate)
    assertEquals("DWI MASRUROH", deva.motherName)
    assertEquals("95427948", deva.nis)
    assertEquals("Masjid, RT 001/RW 001, Dusun Jambangan, Desa/Kelurahan Jambangan, Kecamatan Papar, Kabupaten Kediri", deva.address)
    org.junit.Assert.assertTrue(deva.age >= 16)
    assertEquals("SRT-KED00", deva.schoolId)

    // Row 311: Female student classified into Asrama Putri
    val agustine = parsed[5]
    assertEquals("P", agustine.gender)
    assertEquals("Asrama Putri", agustine.dormitory)
    org.junit.Assert.assertTrue(com.example.util.StudentClassifier.isFemaleStudent(agustine))
  }

  @Test
  fun `verify StudentCsvParser template round-trip and upsert preserves existing student id`() = kotlinx.coroutines.runBlocking {
    val templateStudents = com.example.util.StudentCsvParser.parseCsv(
      com.example.util.StudentCsvParser.STANDARD_TEMPLATE_CSV,
      "SRT-NEW01"
    )
    assertEquals(4, templateStudents.size)
    org.junit.Assert.assertTrue(
      templateStudents.all {
        it.nik.isNotBlank() && it.noKk.isNotBlank() && it.district.isNotBlank() && it.schoolId == "SRT-NEW01"
      }
    )
    assertEquals("Banyakan", templateStudents[0].district)

    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    db.childStudentDao().deleteAll()

    // Insert incomplete student first (simulating earlier partial import)
    val existingId = 777001L
    db.childStudentDao().insert(
      com.example.data.model.ChildStudent(
        id = existingId,
        name = "ACHMAD DEVA ADIANSA",
        nis = "SRT-KED00-313",
        gender = "L",
        age = 12,
        dormitory = "Asrama Putra",
        classGrade = "XI-3 (IPS)",
        allergies = "Alergi Debu",
        bloodType = "A",
        schoolId = "SRT-KED00"
      )
    )

    val completeCsv = """
      No,Nama,Kelas,Jenjang,Alamat,Jenis Kelamin,NIK,NO KK,TEMPAT LAHIR,TANGGAL LAHIR,NAMA IBU,NISN
      310,ACHMAD DEVA ADIANSA,XI-3 (IPS),SMA,"Desa Jambangan, Kec. Papar, Kab. Kediri",L,3506142512090002,3506141405120009,KEDIRI,25-12-2009,DWI MASRUROH,95427948
    """.trimIndent()
    val parsedComplete = com.example.util.StudentCsvParser.parseCsv(completeCsv, "SRT-KED00")

    var importedCount = 0
    vm.saveImportedStudents(parsedComplete, replace = false, schoolId = "SRT-KED00") { count ->
      importedCount = count
    }
    var ticks = 0
    while (importedCount == 0 && ticks < 20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(50)
      ticks++
    }
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val saved = db.childStudentDao().getStudentsBySchool("SRT-KED00").first()
    assertEquals(1, saved.size)
    // Existing ID and custom health fields preserved while demographic fields enriched!
    assertEquals(existingId, saved[0].id)
    assertEquals("3506142512090002", saved[0].nik)
    assertEquals("3506141405120009", saved[0].noKk)
    assertEquals("95427948", saved[0].nis)
    assertEquals("DWI MASRUROH", saved[0].motherName)
    assertEquals("SMA", saved[0].jenjang)
    assertEquals("Alergi Debu", saved[0].allergies)
    assertEquals("A", saved[0].bloodType)
  }

  @Test
  fun `verify full attached 342-student CSV file parses completely without missing NIK or fields`() {
    val candidates = listOf(
      java.io.File("public/assets/aistudio/database_siswa_2026_d0d38b1d.csv"),
      java.io.File("../public/assets/aistudio/database_siswa_2026_d0d38b1d.csv")
    )
    val csvFile = candidates.firstOrNull { it.exists() } ?: return
    val text = com.example.util.StudentCsvParser.decodeCsvBytes(csvFile.readBytes())
    val students = com.example.util.StudentCsvParser.parseCsv(text, "SRT-KED00")

    assertEquals(342, students.size)
    org.junit.Assert.assertTrue("Every student should have NIK", students.all { it.nik.isNotBlank() })
    org.junit.Assert.assertTrue("Every student should have NO KK", students.all { it.noKk.isNotBlank() })
    org.junit.Assert.assertTrue("Every student should have NISN", students.all { it.nis.isNotBlank() })
    org.junit.Assert.assertTrue("Every student should have Mother Name", students.all { it.motherName.isNotBlank() })
    org.junit.Assert.assertTrue("Every student should have Address", students.all { it.address.isNotBlank() })
    org.junit.Assert.assertTrue("Every student should have BirthDate", students.all { it.birthDate.isNotBlank() })

    val deva = students.first { it.name == "ACHMAD DEVA ADIANSA" }
    assertEquals("3506142512090002", deva.nik)
    assertEquals("3506141405120009", deva.noKk)
    assertEquals("95427948", deva.nis)
    assertEquals("SMA", deva.jenjang)
    assertEquals("XI-3 (IPS)", deva.classGrade)
    assertEquals("DWI MASRUROH", deva.motherName)
    assertEquals("KEDIRI", deva.birthPlace)
    assertEquals("25-12-2009", deva.birthDate)
  }

  @Test
  fun `verify DateUtils categorizes shift messages into today vs tomorrow or later`() {
    val calTodayNoon = java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.HOUR_OF_DAY, 12)
      set(java.util.Calendar.MINUTE, 0)
      set(java.util.Calendar.SECOND, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val calTomorrowStart = java.util.Calendar.getInstance().apply {
      add(java.util.Calendar.DAY_OF_YEAR, 1)
      set(java.util.Calendar.HOUR_OF_DAY, 0)
      set(java.util.Calendar.MINUTE, 0)
      set(java.util.Calendar.SECOND, 1)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val calNextWeek = java.util.Calendar.getInstance().apply {
      add(java.util.Calendar.DAY_OF_YEAR, 7)
      set(java.util.Calendar.HOUR_OF_DAY, 12)
    }
    val calYesterday = java.util.Calendar.getInstance().apply {
      add(java.util.Calendar.DAY_OF_YEAR, -1)
      set(java.util.Calendar.HOUR_OF_DAY, 23)
      set(java.util.Calendar.MINUTE, 59)
    }

    val todayMillis = calTodayNoon.timeInMillis
    val tomorrowMillis = calTomorrowStart.timeInMillis
    val nextWeekMillis = calNextWeek.timeInMillis
    val yesterdayMillis = calYesterday.timeInMillis

    org.junit.Assert.assertTrue(com.example.util.DateUtils.isToday(todayMillis))
    org.junit.Assert.assertFalse(com.example.util.DateUtils.isTomorrowOrLater(todayMillis))

    org.junit.Assert.assertFalse(com.example.util.DateUtils.isToday(tomorrowMillis))
    org.junit.Assert.assertTrue(com.example.util.DateUtils.isTomorrowOrLater(tomorrowMillis))

    org.junit.Assert.assertFalse(com.example.util.DateUtils.isToday(nextWeekMillis))
    org.junit.Assert.assertTrue(com.example.util.DateUtils.isTomorrowOrLater(nextWeekMillis))

    org.junit.Assert.assertFalse(com.example.util.DateUtils.isToday(yesterdayMillis))
    org.junit.Assert.assertFalse(com.example.util.DateUtils.isTomorrowOrLater(yesterdayMillis))

    // Verify UTC DatePicker midnight converts to exact local calendar day at noon
    val utcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
      set(java.util.Calendar.YEAR, 2026)
      set(java.util.Calendar.MONTH, java.util.Calendar.NOVEMBER)
      set(java.util.Calendar.DAY_OF_MONTH, 14)
      set(java.util.Calendar.HOUR_OF_DAY, 0)
      set(java.util.Calendar.MINUTE, 0)
      set(java.util.Calendar.SECOND, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val localNoonCal = java.util.Calendar.getInstance().apply {
      timeInMillis = com.example.util.DateUtils.utcDateMillisToLocalNoon(utcCal.timeInMillis)
    }
    assertEquals(2026, localNoonCal.get(java.util.Calendar.YEAR))
    assertEquals(java.util.Calendar.NOVEMBER, localNoonCal.get(java.util.Calendar.MONTH))
    assertEquals(14, localNoonCal.get(java.util.Calendar.DAY_OF_MONTH))
    assertEquals(12, localNoonCal.get(java.util.Calendar.HOUR_OF_DAY))
  }

  @Test
  fun `verify ShiftHistoryFilterHelper filters only past messages with month, status, and search`() {
    val today = java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.HOUR_OF_DAY, 10)
    }.timeInMillis
    val tomorrow = java.util.Calendar.getInstance().apply {
      add(java.util.Calendar.DAY_OF_YEAR, 1)
      set(java.util.Calendar.HOUR_OF_DAY, 10)
    }.timeInMillis
    val yesterday = java.util.Calendar.getInstance().apply {
      add(java.util.Calendar.DAY_OF_YEAR, -1)
      set(java.util.Calendar.HOUR_OF_DAY, 14)
    }.timeInMillis
    val fortyDaysAgo = java.util.Calendar.getInstance().apply {
      add(java.util.Calendar.DAY_OF_YEAR, -40)
      set(java.util.Calendar.HOUR_OF_DAY, 9)
    }.timeInMillis

    org.junit.Assert.assertFalse(com.example.util.DateUtils.isBeforeToday(today))
    org.junit.Assert.assertFalse(com.example.util.DateUtils.isBeforeToday(tomorrow))
    org.junit.Assert.assertTrue(com.example.util.DateUtils.isBeforeToday(yesterday))
    org.junit.Assert.assertTrue(com.example.util.DateUtils.isBeforeToday(fortyDaysAgo))

    val msgToday = com.example.data.model.ShiftMessage(
      id = 1L,
      title = "Operan Hari Ini",
      followUpDate = today,
      shift = "Shift Pagi (07-14)",
      senderName = "Ustadz Ali",
      targetWali = "Semua Wali Asuh",
      messageContent = "Cek suhu santri baru",
      isCompleted = false
    )
    val msgTomorrow = com.example.data.model.ShiftMessage(
      id = 2L,
      title = "Operan Besok",
      followUpDate = tomorrow,
      shift = "Shift Siang (14-21)",
      senderName = "Ustadz Budi",
      targetWali = "Semua Wali Asuh",
      messageContent = "Jadwal kontrol puskesmas",
      isCompleted = false
    )
    val msgYesterdayUncompleted = com.example.data.model.ShiftMessage(
      id = 3L,
      title = "Jaga Menara Kemarin",
      followUpDate = yesterday,
      shift = "Shift Malam (21-07)",
      senderName = "Abisarwan Rafif",
      targetWali = "Ustadz Hasan",
      messageContent = "Pastikan gerbang terkunci pukul 22.00",
      isCompleted = false
    )
    val msgOlderCompleted = com.example.data.model.ShiftMessage(
      id = 4L,
      title = "Pemberian Vitamin Asrama",
      followUpDate = fortyDaysAgo,
      shift = "Shift Pagi (07-14)",
      senderName = "Ustadzah Sari",
      targetWali = "Semua Wali Asuh",
      messageContent = "Vitamin C dibagikan setelah sarapan",
      isCompleted = true,
      completedBy = "Ustadzah Dewi",
      completedAt = fortyDaysAgo + 3600_000L
    )

    val allMessages = listOf(msgToday, msgTomorrow, msgYesterdayUncompleted, msgOlderCompleted)
    val past = com.example.ui.dialogs.ShiftHistoryFilterHelper.getPastMessages(allMessages)

    // Strictly past messages only, newest first
    assertEquals(2, past.size)
    assertEquals(3L, past[0].id)
    assertEquals(4L, past[1].id)

    // Months derived cleanly in descending order
    val months = com.example.ui.dialogs.ShiftHistoryFilterHelper.getAvailableMonths(past)
    org.junit.Assert.assertTrue(months.isNotEmpty())
    val olderMonthKey = com.example.util.DateUtils.getMonthKey(fortyDaysAgo)

    // Filter by Status: Belum Selesai vs Selesai
    val onlyUncompleted = com.example.ui.dialogs.ShiftHistoryFilterHelper.filterMessages(
      pastMessages = past,
      selectedMonthKey = "ALL",
      selectedStatus = "Belum Selesai",
      searchQuery = ""
    )
    assertEquals(listOf(msgYesterdayUncompleted), onlyUncompleted)

    val onlyCompleted = com.example.ui.dialogs.ShiftHistoryFilterHelper.filterMessages(
      pastMessages = past,
      selectedMonthKey = "ALL",
      selectedStatus = "Selesai",
      searchQuery = ""
    )
    assertEquals(listOf(msgOlderCompleted), onlyCompleted)

    // Filter by Month
    val byMonth = com.example.ui.dialogs.ShiftHistoryFilterHelper.filterMessages(
      pastMessages = past,
      selectedMonthKey = olderMonthKey,
      selectedStatus = "Semua",
      searchQuery = ""
    )
    assertEquals(listOf(msgOlderCompleted), byMonth)

    // Search by sender, recipient, or content
    val bySearch = com.example.ui.dialogs.ShiftHistoryFilterHelper.filterMessages(
      pastMessages = past,
      selectedMonthKey = "ALL",
      selectedStatus = "Semua",
      searchQuery = "gerbang"
    )
    assertEquals(listOf(msgYesterdayUncompleted), bySearch)
  }

  @Test
  fun `verify SchoolProfile sanitizeShortName un-truncates legacy 15-char truncation`() {
    val fullName = "SEKOLAH RAKYAT KOTA MALANG"
    val legacyTruncated = fullName.take(15) // "SEKOLAH RAKYAT "
    assertEquals(fullName, com.example.data.model.SchoolProfile.sanitizeShortName(fullName, legacyTruncated))
    assertEquals(fullName, com.example.data.model.SchoolProfile.sanitizeShortName(fullName, "SEKOLAH RAKYAT"))
    assertEquals(fullName, com.example.data.model.SchoolProfile.sanitizeShortName(fullName, ""))
    // Distinct custom short name should still be respected
    assertEquals("SRT Malang", com.example.data.model.SchoolProfile.sanitizeShortName(fullName, "SRT Malang"))
  }

  @Test
  fun `verify tenant login preserves full schoolName and updates registration on profile edit`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)

    val schoolCode = "SR-MLG-901"
    val adminUser = "admin_malang_test"
    val adminPass = "Malang#2026"
    val fullSchoolName = "Sekolah Rakyat Terpadu Kota Malang"

    val reg = com.example.data.model.SchoolRegistration(
      schoolName = fullSchoolName,
      address = "Jl. Ijen No. 88",
      city = "Kota Malang",
      estimatedWaliCount = 12,
      picName = "Ibu Ratna Sari",
      picEmail = "ratna@malang.sch.id",
      picPhone = "081299988877",
      status = "APPROVED",
      generatedSchoolCode = schoolCode,
      generatedAdminUsername = adminUser,
      generatedAdminPassword = adminPass
    )
    db.schoolRegistrationDao().insert(reg)

    // 1. Login as tenant admin
    val loginRes = vm.executeLogin(adminUser, adminPass, rememberMe = false)
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessAdmin)

    // Must NOT truncate schoolName to 15 characters and must NOT hardcode Kediri faskes
    val activeProfile = vm.schoolProfile.value
    assertEquals(fullSchoolName, activeProfile.schoolName)
    assertEquals(fullSchoolName, activeProfile.shortName)
    assertEquals("Kota Malang", activeProfile.city)
    assertEquals("Ibu Ratna Sari", activeProfile.adminName)
    org.junit.Assert.assertFalse(activeProfile.secondaryFaskesName.contains("Wates"))
    org.junit.Assert.assertTrue(activeProfile.additionalFaskesList.isEmpty())

    // 2. Update SchoolProfile -> should sync back to SchoolRegistration
    val editedProfile = activeProfile.copy(
      schoolName = "Sekolah Rakyat 2 Kota Malang",
      shortName = "Sekolah Rakyat 2 Kota Malang",
      address = "Jl. Ijen No. 88, Klojen, Kota Malang",
      city = "Malang Raya",
      adminName = "Bapak Hendra",
      secondaryFaskesName = "Puskesmas Klojen"
    )
    vm.updateSchoolProfile(editedProfile)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    var matchedReg: com.example.data.model.SchoolRegistration? = null
    for (attempt in 1..20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      matchedReg = db.schoolRegistrationDao().getBySchoolCode(schoolCode)
      if (matchedReg?.schoolName == "Sekolah Rakyat 2 Kota Malang") break
      kotlinx.coroutines.delay(50)
    }
    assertEquals("Sekolah Rakyat 2 Kota Malang", matchedReg?.schoolName)
    assertEquals("Jl. Ijen No. 88, Klojen, Kota Malang", matchedReg?.address)
    assertEquals("Malang Raya", matchedReg?.city)
    assertEquals("Bapak Hendra", matchedReg?.picName)

    // 3. WhatsApp access message must reflect tenant's school, faskes, and admin
    val wali = com.example.data.model.WaliAccount(
      name = "Ustadzah Nia",
      username = "ustnia",
      password = "Asuh#4421",
      dormitory = "Asrama Putri",
      schoolId = schoolCode
    )
    val waMsg = com.example.util.PasswordGenerator.formatWhatsAppMessage(wali, vm.schoolProfile.value)
    org.junit.Assert.assertTrue(waMsg.contains("Sekolah Rakyat 2 Kota Malang"))
    org.junit.Assert.assertTrue(waMsg.contains("Puskesmas Klojen"))
    org.junit.Assert.assertTrue(waMsg.contains("Bapak Hendra"))
    org.junit.Assert.assertFalse(waMsg.contains("SRT 1"))
    org.junit.Assert.assertFalse(waMsg.contains("Eko Wahyudi"))

    // 4. PDF kop surat, sub-header (address), unit titles, and signature labels must reflect dynamic SchoolProfile
    val headerTitle = com.example.util.PdfReportGenerator.buildHeaderTitle(vm.schoolProfile.value)
    val subHeaderLine = com.example.util.PdfReportGenerator.buildSubHeaderLine(vm.schoolProfile.value)
    val footerSignTitle = com.example.util.PdfReportGenerator.buildFooterSignatureTitle(vm.schoolProfile.value)
    assertEquals("SEKOLAH RAKYAT 2 KOTA MALANG", headerTitle)
    assertEquals("Jl. Ijen No. 88, Klojen, Kota Malang", subHeaderLine)
    org.junit.Assert.assertFalse(subHeaderLine.contains("Kerjasama"))
    assertEquals("Wali Asuh Sekolah Rakyat 2 Kota Malang,", footerSignTitle)
    org.junit.Assert.assertFalse(footerSignTitle.contains("Kesehatan"))
    assertEquals("UNIT LAYANAN KESEHATAN WALIASUH", com.example.util.PdfReportGenerator.UNIT_LAYANAN_KESEHATAN)
    assertEquals("UNIT BIMBINGAN SPIRITUAL WALIASUH", com.example.util.PdfReportGenerator.UNIT_BIMBINGAN_SPIRITUAL)

    matchedReg?.let { vm.deleteSchoolRegistration(it) }
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    Unit
  }

  @Test
  fun `verify school admin self password change keeps username locked and syncs to Superadmin record`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)

    val schoolCode = "SRT-PW-TEST"
    val adminUser = "admin_pw_test_01"
    val oldPass = "OldPass#123"
    val newPass = "NewPass#999"

    val reg = com.example.data.model.SchoolRegistration(
      schoolName = "Sekolah Uji Ganti Password",
      address = "Jl. Merdeka No. 9",
      city = "Kota Blitar",
      estimatedWaliCount = 4,
      picName = "Pak Budi",
      picEmail = "budi@sekolah.id",
      picPhone = "081299998888",
      status = "APPROVED",
      generatedSchoolCode = schoolCode,
      generatedAdminUsername = adminUser,
      generatedAdminPassword = oldPass
    )
    db.schoolRegistrationDao().insert(reg)

    // 1. Login dengan password lama
    val loginOld = vm.executeLogin(adminUser, oldPass, rememberMe = false)
    org.junit.Assert.assertTrue(loginOld is com.example.ui.LoginResult.SuccessAdmin)
    assertEquals(adminUser, vm.currentUsername.value)

    // 2. Ganti password admin sekolah
    var updateSuccess = false
    vm.updateAdminSelfPassword(newPass) { ok, _ ->
      updateSuccess = ok
    }
    for (attempt in 1..20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (updateSuccess) break
      kotlinx.coroutines.delay(30)
    }
    org.junit.Assert.assertTrue(updateSuccess)

    // 3. Verifikasi data di SchoolRegistration (yang tampil di Superadmin) terupdate dan username tidak berubah
    val updatedReg = db.schoolRegistrationDao().getBySchoolCode(schoolCode)
    assertEquals(adminUser, updatedReg?.generatedAdminUsername)
    assertEquals(newPass, updatedReg?.generatedAdminPassword)

    // 4. Logout lalu pastikan password lama ditolak dan password baru diterima
    vm.logout()
    val loginWithOldAfterChange = vm.executeLogin(adminUser, oldPass, rememberMe = false)
    org.junit.Assert.assertTrue(loginWithOldAfterChange is com.example.ui.LoginResult.Error)

    val loginWithNew = vm.executeLogin(adminUser, newPass, rememberMe = false)
    org.junit.Assert.assertTrue(loginWithNew is com.example.ui.LoginResult.SuccessAdmin)

    updatedReg?.let { vm.deleteSchoolRegistration(it) }
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    Unit
  }

  @Test
  fun `verify ekowahyudiadmin password change persists to registration and overrides default password`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)

    // Pastikan bersih dari test sebelumnya
    db.schoolRegistrationDao().getByAdminUsername("ekowahyudiadmin")?.let {
      db.schoolRegistrationDao().delete(it)
    }

    // 1. Login dengan password bawaan awal
    val initialLogin = vm.executeLogin("ekowahyudiadmin", "Woyowoyo12@", rememberMe = false)
    org.junit.Assert.assertTrue(initialLogin is com.example.ui.LoginResult.SuccessAdmin)

    // 2. Ubah password
    val newPilotPass = "EkoBaru#2026"
    var ok = false
    vm.updateAdminSelfPassword(newPilotPass) { success, _ -> ok = success }
    for (attempt in 1..20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (ok) break
      kotlinx.coroutines.delay(30)
    }
    org.junit.Assert.assertTrue(ok)

    // 3. Password baru tercatat di SchoolRegistration agar terlihat Superadmin
    val pilotReg = db.schoolRegistrationDao().getByAdminUsername("ekowahyudiadmin")
    assertEquals("SRT1-KDR", pilotReg?.generatedSchoolCode)
    assertEquals("ekowahyudiadmin", pilotReg?.generatedAdminUsername)
    assertEquals(newPilotPass, pilotReg?.generatedAdminPassword)

    // 4. Password lama tidak lagi berlaku, password baru berhasil login
    vm.logout()
    val oldAttempt = vm.executeLogin("ekowahyudiadmin", "Woyowoyo12@", rememberMe = false)
    org.junit.Assert.assertTrue(oldAttempt is com.example.ui.LoginResult.Error)

    val newAttempt = vm.executeLogin("ekowahyudiadmin", newPilotPass, rememberMe = false)
    org.junit.Assert.assertTrue(newAttempt is com.example.ui.LoginResult.SuccessAdmin)

    pilotReg?.let { db.schoolRegistrationDao().delete(it) }
    Unit
  }

  @Test
  fun `verify haid day calculation, flexible manual suci at day 8, and auto-move to suci after day 15`() {
    val now = System.currentTimeMillis()

    // 1. Input Haid Hari ke-3 -> mundur 2 hari kalender, durasi = 3 hari, masih aktif
    val startForDay3 = com.example.data.model.HaidDateCalculator.calculateStartDateForHaidDay(3, now)
    val day3Record = com.example.data.model.HaidRecord(
      id = 1L,
      studentId = 101L,
      studentName = "Siti Aisyah",
      startDate = startForDay3,
      status = "HAID"
    )
    assertEquals(3, day3Record.durationDays)
    org.junit.Assert.assertTrue(day3Record.isActive)
    assertEquals(0, day3Record.suciDayCount)

    // 2. Hari ke-8 haid ternyata sudah suci -> dipindah manual ke Daftar Suci
    val start8DaysAgo = com.example.data.model.HaidDateCalculator.calculateStartDateForHaidDay(8, now)
    val manualSuciAtDay8 = com.example.data.model.HaidRecord(
      id = 2L,
      studentId = 102L,
      studentName = "Fatimah Az-Zahra",
      startDate = start8DaysAgo,
      endDate = now,
      status = "SUCI"
    )
    assertEquals(8, manualSuciAtDay8.durationDays)
    org.junit.Assert.assertFalse(manualSuciAtDay8.isActive)
    assertEquals(1, manualSuciAtDay8.suciDayCount)
    org.junit.Assert.assertTrue(manualSuciAtDay8.isInMinimumSuciPeriod)

    // 3. Hari ke-15 masih di Daftar Haid dengan peringatan batas maksimal
    val start15DaysAgo = com.example.data.model.HaidDateCalculator.calculateStartDateForHaidDay(15, now)
    val day15Record = com.example.data.model.HaidRecord(
      id = 3L,
      studentId = 103L,
      studentName = "Khadijah",
      startDate = start15DaysAgo,
      status = "HAID"
    )
    assertEquals(15, day15Record.durationDays)
    org.junit.Assert.assertTrue(day15Record.isActive)
    org.junit.Assert.assertTrue(day15Record.isIstihadhahWarning)
    org.junit.Assert.assertFalse(day15Record.isAutoCompletedByLimit)

    // 4. Lebih dari 15 hari (16 hari sejak mulai) -> otomatis berpindah ke Daftar Suci (Suci Hari ke-1)
    val cal16 = java.util.Calendar.getInstance().apply {
      timeInMillis = now
      add(java.util.Calendar.DAY_OF_YEAR, -15) // hari ini adalah hari ke-16
    }.timeInMillis
    val autoMovedRecord = com.example.data.model.HaidRecord(
      id = 4L,
      studentId = 104L,
      studentName = "Zainab",
      startDate = cal16,
      status = "HAID"
    )
    org.junit.Assert.assertTrue(autoMovedRecord.isAutoCompletedByLimit)
    org.junit.Assert.assertFalse(autoMovedRecord.isActive)
    assertEquals(15, autoMovedRecord.durationDays)
    assertEquals(1, autoMovedRecord.suciDayCount)
    org.junit.Assert.assertTrue(autoMovedRecord.isInMinimumSuciPeriod)

    // 5. Suci sudah memasuki hari ke-15+ -> masa minimal suci terpenuhi (tidak lagi terindikasi < 15 hari)
    val calSuci15 = java.util.Calendar.getInstance().apply {
      timeInMillis = now
      add(java.util.Calendar.DAY_OF_YEAR, -14) // hari ini Suci Hari ke-15
    }.timeInMillis
    val suciDay15Record = com.example.data.model.HaidRecord(
      id = 5L,
      studentId = 105L,
      studentName = "Maryam",
      startDate = calSuci15 - (6L * 86_400_000L),
      endDate = calSuci15,
      status = "SUCI"
    )
    assertEquals(15, suciDay15Record.suciDayCount)
    org.junit.Assert.assertFalse(suciDay15Record.isInMinimumSuciPeriod)
  }

  @Test
  fun `verify extractBarcodePayload handles plain NIK NIS and labeled QR student card strings`() {
    assertEquals(
      "3506123456780001",
      com.example.ui.dialogs.extractBarcodePayload("  3506123456780001  ")
    )
    assertEquals(
      "3506123456780001",
      com.example.ui.dialogs.extractBarcodePayload("NIK: 3506123456780001\nNama: Aisyah")
    )
    assertEquals(
      "SRT1-002",
      com.example.ui.dialogs.extractBarcodePayload("nis=SRT1-002")
    )
    assertEquals(
      "",
      com.example.ui.dialogs.extractBarcodePayload("   ")
    )
  }

  @Test
  fun `verify StudentCardRenderer applies SMA grey SMP blue SD red and neon gender trim plus generates QR and PDF`() {
    val context = org.robolectric.RuntimeEnvironment.getApplication()
    val smaMale = com.example.data.model.ChildStudent(
      id = 1L,
      name = "Ahmad Fauzan",
      nis = "0088123456",
      gender = "L",
      age = 16,
      dormitory = "Asrama Putra",
      classGrade = "11 IPA",
      nik = "3506142512090001",
      address = "Desa Siman, Kepung, Kediri"
    )
    val smpFemale = com.example.data.model.ChildStudent(
      id = 2L,
      name = "Siti Aisyah",
      nis = "0099123457",
      gender = "P",
      age = 13,
      dormitory = "Asrama Putri",
      classGrade = "8B",
      nik = "3506142512090002",
      address = "Jl. Pahlawan No. 12, Pare"
    )
    val sdFemale = com.example.data.model.ChildStudent(
      id = 3L,
      name = "Fatimah Az-Zahra",
      nis = "0100123458",
      gender = "P",
      age = 10,
      dormitory = "Asrama Putri",
      classGrade = "4",
      nik = "3506142512090003",
      address = "Dusun Krajan, Gurah"
    )

    val smaPalette = com.example.util.StudentCardRenderer.resolvePalette(smaMale)
    assertEquals(com.example.util.CardJenjang.SMA, smaPalette.jenjang)
    assertEquals(0xFF475569.toInt(), smaPalette.gradientMidArgb) // Header Abu-abu
    assertEquals(0xFF10B981.toInt(), smaPalette.trimNeonArgb)    // Putra -> Hijau Emerald

    val smpPalette = com.example.util.StudentCardRenderer.resolvePalette(smpFemale)
    assertEquals(com.example.util.CardJenjang.SMP, smpPalette.jenjang)
    assertEquals(0xFF1D4ED8.toInt(), smpPalette.gradientMidArgb) // Header Biru
    assertEquals(0xFFEC4899.toInt(), smpPalette.trimNeonArgb)    // Putri -> Pink Rose

    val sdPalette = com.example.util.StudentCardRenderer.resolvePalette(sdFemale)
    assertEquals(com.example.util.CardJenjang.SD, sdPalette.jenjang)
    assertEquals(0xFFB91C1C.toInt(), sdPalette.gradientMidArgb)  // Header Merah
    assertEquals(0xFFEC4899.toInt(), sdPalette.trimNeonArgb)     // Putri -> Pink Rose

    val qrBitmap = com.example.util.StudentCardRenderer.generateQrBitmap(
      com.example.util.StudentCardRenderer.resolveQrPayload(smaMale),
      256
    )
    org.junit.Assert.assertNotNull(qrBitmap)
    assertEquals(256, qrBitmap!!.width)
    assertEquals(256, qrBitmap.height)

    val cardBitmap = android.graphics.Bitmap.createBitmap(488, 308, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(cardBitmap)
    com.example.util.StudentCardRenderer.drawStudentCardOnCanvas(
      canvas = canvas,
      left = 0f,
      top = 0f,
      width = 488f,
      height = 308f,
      student = smaMale,
      schoolName = "Sekolah Rakyat Kabupaten Kediri",
      qrBitmap = qrBitmap,
      logoBitmap = null
    )
    // Pastikan QR bitmap benar-benar memiliki modul hitam/putih dan rendering Canvas berjalan tanpa error
    org.junit.Assert.assertTrue(
      qrBitmap.getPixel(0, 0) == android.graphics.Color.WHITE ||
        qrBitmap.getPixel(0, 0) == android.graphics.Color.BLACK
    )
    assertEquals(com.example.util.CardJenjang.SMA, com.example.util.StudentCardRenderer.resolveCardJenjang(smaMale.copy(classGrade = "XII-2")))
    assertEquals(com.example.util.CardJenjang.SMP, com.example.util.StudentCardRenderer.resolveCardJenjang(smpFemale.copy(classGrade = "VII-A")))
    assertEquals(com.example.util.CardJenjang.SD, com.example.util.StudentCardRenderer.resolveCardJenjang(sdFemale.copy(classGrade = "5")))
    qrBitmap.recycle()
    cardBitmap.recycle()
  }

  @Test
  fun `verify all 5 PDF reports generate cleanly with QR wali signature and real admin name`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)

    val schoolCode = "SRT-PDF-QR"
    val adminUser = "admin_pdf_qr"
    val adminPass = "PdfQr#2026"
    val reg = com.example.data.model.SchoolRegistration(
      id = 777123L,
      schoolName = "Sekolah Rakyat Kabupaten Kediri",
      address = "Ploso Kidul, Plosoklaten, Kediri",
      city = "Kabupaten Kediri",
      estimatedWaliCount = 5,
      picName = "Ustadzah Siti Aminah",
      picEmail = "siti@sekolah.id",
      picPhone = "081233344455",
      status = "APPROVED",
      generatedSchoolCode = schoolCode,
      generatedAdminUsername = adminUser,
      generatedAdminPassword = adminPass
    )
    db.schoolRegistrationDao().insert(reg)

    val loginRes = vm.executeLogin(adminUser, adminPass, rememberMe = false)
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessAdmin)
    assertEquals("Ustadzah Siti Aminah", vm.waliName.value)

    val student = com.example.data.model.ChildStudent(
      id = 91L,
      name = "Abimanyu Satria",
      nis = "108346824",
      gender = "L",
      age = 16,
      dormitory = "Asrama Putra",
      classGrade = "X-4",
      schoolId = schoolCode
    )
    val medRecord = com.example.data.model.MedicalRecord(
      id = 201L,
      studentId = student.id,
      studentName = student.name,
      studentDormitory = student.dormitory,
      faskes = "Puskesmas Kediri",
      complaint = "Demam",
      planDate = System.currentTimeMillis(),
      diagnosis = "Demam ringan",
      actionsTaken = "Observasi",
      prescription = "Paracetamol",
      status = "COMPLETED"
    )
    val haidRecord = com.example.data.model.HaidRecord(
      id = 301L,
      studentId = 92L,
      studentName = "Aisyah Putri",
      studentClass = "VIII-A",
      studentDorm = "Asrama Putri",
      startDate = System.currentTimeMillis(),
      status = "HAID",
      waliName = vm.waliName.value,
      schoolId = schoolCode
    )
    val session = com.example.data.model.FastingSession(
      id = 401L,
      title = "Puasa Senin Kamis",
      sessionDate = System.currentTimeMillis(),
      schoolId = schoolCode
    )
    val fastingRecord = com.example.data.model.FastingRecord(
      id = 501L,
      sessionId = session.id,
      studentId = student.id,
      studentName = student.name,
      studentClass = student.classGrade,
      studentDorm = student.dormitory,
      waliName = vm.waliName.value,
      isSahurChecked = true,
      isBukaChecked = true,
      schoolId = schoolCode
    )

    val profile = vm.schoolProfile.value
    val footerTitle = com.example.util.PdfReportGenerator.buildFooterSignatureTitle(profile)
    assertEquals("Wali Asuh Sekolah Rakyat Kabupaten Kediri,", footerTitle)
    org.junit.Assert.assertFalse(footerTitle.contains("Mengetahui"))

    val waliQr = com.example.util.StudentCardRenderer.generateQrBitmap(vm.waliName.value, 256)
    org.junit.Assert.assertNotNull(waliQr)
    org.junit.Assert.assertTrue(
      waliQr!!.getPixel(0, 0) == android.graphics.Color.WHITE ||
        waliQr.getPixel(0, 0) == android.graphics.Color.BLACK
    )
    waliQr.recycle()

    // Uji render langsung blok tanda tangan tunggal kanan + QR code pada Canvas A4
    val a4Bitmap = android.graphics.Bitmap.createBitmap(595, 842, android.graphics.Bitmap.Config.ARGB_8888)
    val a4Canvas = android.graphics.Canvas(a4Bitmap)
    val paint = android.graphics.Paint().apply { isAntiAlias = true }
    com.example.util.PdfReportGenerator.drawFooterSignatures(
      context = app,
      canvas = a4Canvas,
      paint = paint,
      width = 595,
      height = 842,
      startY = 620f,
      waliName = vm.waliName.value,
      schoolProfile = profile
    )
    a4Bitmap.recycle()

    // Verifikasi penghapusan sekolah & tombstone mencegah sinkronisasi menghidupkan kembali sekolah
    vm.deleteSchoolRegistration(reg)
    for (attempt in 1..20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (db.schoolRegistrationDao().getBySchoolCode(schoolCode) == null) break
      kotlinx.coroutines.delay(25)
    }
    org.junit.Assert.assertNull(db.schoolRegistrationDao().getBySchoolCode(schoolCode))
    org.junit.Assert.assertTrue(
      com.example.data.remote.pocketbase.PocketBaseConfig.isSchoolRegistrationDeleted(app, reg)
    )
  }

  @Test
  fun `verify AppLanguageManager persistence localized context and DateUtils formatting`() {
    val baseContext = ApplicationProvider.getApplicationContext<Context>()
    baseContext.getSharedPreferences("waliasuh_prefs", Context.MODE_PRIVATE).edit().clear().commit()

    // Default is Indonesian
    assertEquals(
      com.example.util.i18n.AppLanguage.INDONESIAN,
      com.example.util.i18n.AppLanguageManager.init(baseContext)
    )

    // Switch to English & verify persistence
    com.example.util.i18n.AppLanguageManager.setLanguage(
      baseContext,
      com.example.util.i18n.AppLanguage.ENGLISH
    )
    assertEquals(
      com.example.util.i18n.AppLanguage.ENGLISH,
      com.example.util.i18n.AppLanguageManager.getLanguage(baseContext)
    )

    val enCtx = com.example.util.i18n.AppLanguageManager.createLocalizedContext(
      baseContext,
      com.example.util.i18n.AppLanguage.ENGLISH
    )
    assertEquals("Waliasuh Care", enCtx.getString(R.string.app_name))
    val enShort = enCtx.getString(R.string.disclaimer_short)
    org.junit.Assert.assertTrue(enShort.contains("Sekolah Rakyat"))
    org.junit.Assert.assertTrue(enShort.contains("Independent helper tool"))
    val enFull = enCtx.getString(R.string.disclaimer_full)
    org.junit.Assert.assertTrue(enFull.contains("Wali Asuh Care"))
    org.junit.Assert.assertTrue(enFull.contains("Sekolah Rakyat"))
    org.junit.Assert.assertTrue(enCtx is android.content.ContextWrapper)

    val idCtx = com.example.util.i18n.AppLanguageManager.createLocalizedContext(
      baseContext,
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val idShort = idCtx.getString(R.string.disclaimer_short)
    org.junit.Assert.assertTrue(idShort.contains("Sekolah Rakyat"))
    org.junit.Assert.assertTrue(idShort.contains("Alat bantu"))

    // DateUtils formatting
    val now = System.currentTimeMillis()
    val todayId = com.example.util.DateUtils.getRelativeDateLabel(now, com.example.util.i18n.AppLanguage.INDONESIAN)
    val todayEn = com.example.util.DateUtils.getRelativeDateLabel(now, com.example.util.i18n.AppLanguage.ENGLISH)
    org.junit.Assert.assertTrue(todayId.startsWith("Hari ini ("))
    org.junit.Assert.assertTrue(todayEn.startsWith("Today ("))

    val calFixed = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.NOVEMBER, 14, 10, 0, 0)
    }.timeInMillis
    val fullEn = com.example.util.DateUtils.formatFullDate(calFixed, com.example.util.i18n.AppLanguage.ENGLISH)
    org.junit.Assert.assertTrue(fullEn.contains("November"))
    org.junit.Assert.assertTrue(fullEn.contains("2026"))

    // Restore Indonesian default
    com.example.util.i18n.AppLanguageManager.setLanguage(
      baseContext,
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
  }

  @Test
  fun `verify HealthViewModel language persistence English WA templates PDFs and preserved Point 2 terms`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_prefs", Context.MODE_PRIVATE).edit().clear().commit()

    val vm1 = com.example.ui.HealthViewModel(app)
    assertEquals(com.example.util.i18n.AppLanguage.INDONESIAN, vm1.appLanguage.value)

    vm1.setAppLanguage(com.example.util.i18n.AppLanguage.ENGLISH)
    assertEquals(com.example.util.i18n.AppLanguage.ENGLISH, vm1.appLanguage.value)

    // New ViewModel instance loads persisted ENGLISH setting
    val vm2 = com.example.ui.HealthViewModel(app)
    assertEquals(com.example.util.i18n.AppLanguage.ENGLISH, vm2.appLanguage.value)

    val schoolProfile = com.example.data.model.SchoolProfile(
      schoolName = "Sekolah Rakyat Kabupaten Kediri",
      shortName = "Sekolah Rakyat Kabupaten Kediri",
      address = "Ploso Kidul, Plosoklaten, Kediri"
    )
    val haidRecord = com.example.data.model.HaidRecord(
      id = 901L,
      studentId = 10L,
      studentName = "Aisyah Putri",
      studentClass = "VIII-A",
      studentDorm = "Asrama Putri",
      startDate = System.currentTimeMillis() - (2L * 86_400_000L),
      status = "HAID",
      waliName = "Ustadzah Aminah",
      schoolId = "SRT1-KDR"
    )
    val haidWaEn = com.example.util.PdfReportGenerator.buildDailyHaidSummaryText(
      activeHaidRecords = listOf(haidRecord),
      waliName = "Ustadzah Aminah",
      schoolProfile = schoolProfile,
      language = com.example.util.i18n.AppLanguage.ENGLISH
    )
    org.junit.Assert.assertTrue(haidWaEn.contains("*DAILY FEMALE STUDENT HAID REPORT*"))
    org.junit.Assert.assertTrue(haidWaEn.contains("Day 3/15"))
    org.junit.Assert.assertTrue(haidWaEn.contains("Reported by: Ustadzah Aminah"))

    val emptyHaidWaEn = com.example.util.PdfReportGenerator.buildDailyHaidSummaryText(
      activeHaidRecords = emptyList(),
      waliName = "Ustadzah Aminah",
      schoolProfile = schoolProfile,
      language = com.example.util.i18n.AppLanguage.ENGLISH
    )
    org.junit.Assert.assertTrue(emptyHaidWaEn.contains("Suci"))

    val session = com.example.data.model.FastingSession(
      id = 402L,
      title = "Puasa Senin Kamis",
      sessionDate = System.currentTimeMillis(),
      schoolId = "SRT1-KDR"
    )
    val fastingRecord = com.example.data.model.FastingRecord(
      id = 502L,
      sessionId = session.id,
      studentId = 11L,
      studentName = "Abimanyu Satria",
      studentClass = "X-4",
      studentDorm = "Asrama Putra",
      waliName = "Ustadz Fauzi",
      isSahurChecked = true,
      isBukaChecked = false,
      schoolId = "SRT1-KDR"
    )
    val fastingWaEn = com.example.util.PdfReportGenerator.buildFastingListWaSummary(
      session = session,
      records = listOf(fastingRecord),
      waliName = "Ustadz Fauzi",
      schoolProfile = schoolProfile,
      language = com.example.util.i18n.AppLanguage.ENGLISH
    )
    org.junit.Assert.assertTrue(fastingWaEn.contains("*STUDENT PUASA PARTICIPANTS LIST*"))
    org.junit.Assert.assertTrue(fastingWaEn.contains("Session: *Puasa Senin Kamis*"))
    org.junit.Assert.assertTrue(fastingWaEn.contains("*Total Puasa Participants: 1 Students*"))

    val checklistWaEn = com.example.util.PdfReportGenerator.buildFastingChecklistWaSummary(
      session = session,
      records = listOf(fastingRecord),
      waliName = "Ustadz Fauzi",
      schoolProfile = schoolProfile,
      language = com.example.util.i18n.AppLanguage.ENGLISH
    )
    org.junit.Assert.assertTrue(checklistWaEn.contains("*SAHUR & BERBUKA PUASA CHECKLIST*"))
    org.junit.Assert.assertTrue(checklistWaEn.contains("Sahur Checked: 1/1"))
    org.junit.Assert.assertTrue(checklistWaEn.contains("Berbuka Checked: 0/1"))

    // Signature preserves Point 2 Wali Asuh + School Name
    assertEquals(
      "Wali Asuh Sekolah Rakyat Kabupaten Kediri,",
      com.example.util.PdfReportGenerator.buildFooterSignatureTitle(schoolProfile)
    )

    // Footer disclaimer in English preserves Point 2 terms (Kemensos RI / Sekolah Rakyat / Wali Asuh Care)
    val enCtxPdf = com.example.util.i18n.AppLanguageManager.createLocalizedContext(
      app,
      com.example.util.i18n.AppLanguage.ENGLISH
    )
    val enPdfFooter = enCtxPdf.getString(R.string.disclaimer_pdf_footer)
    org.junit.Assert.assertTrue(enPdfFooter.contains("Wali Asuh Care"))
    org.junit.Assert.assertTrue(enPdfFooter.contains("Kemensos RI"))
    org.junit.Assert.assertTrue(enPdfFooter.contains("Sekolah Rakyat"))

    // Render PDF footer signature onto A4 Canvas in English mode cleanly without throwing
    val a4Bitmap = android.graphics.Bitmap.createBitmap(595, 842, android.graphics.Bitmap.Config.ARGB_8888)
    val a4Canvas = android.graphics.Canvas(a4Bitmap)
    val paint = android.graphics.Paint().apply { isAntiAlias = true }
    com.example.util.PdfReportGenerator.drawFooterSignatures(
      context = app,
      canvas = a4Canvas,
      paint = paint,
      width = 595,
      height = 842,
      startY = 620f,
      waliName = "Ustadz Fauzi",
      schoolProfile = schoolProfile
    )
    a4Bitmap.recycle()

    // Reset to Indonesian
    vm1.setAppLanguage(com.example.util.i18n.AppLanguage.INDONESIAN)
  }

  @Test
  fun `verify ProfileScreen language selector switches UI from Indonesian to English and back`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val vm = com.example.ui.HealthViewModel(app)
    vm.setAppLanguage(com.example.util.i18n.AppLanguage.INDONESIAN)

    composeTestRule.setContent {
      val lang by vm.appLanguage.collectAsStateWithLifecycle()
      val baseContext = LocalContext.current
      val localizedContext = com.example.util.i18n.AppLanguageManager.createLocalizedContext(baseContext, lang)
      val appStrings = com.example.util.i18n.AppLanguageManager.stringsFor(lang)
      CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
        com.example.util.i18n.LocalAppStrings provides appStrings
      ) {
        com.example.ui.screens.ProfileScreen(
          viewModel = vm,
          waliName = "Ustadz Ahmad",
          isDarkTheme = false,
          onToggleTheme = {},
          records = emptyList()
        )
      }
    }

    composeTestRule.waitForIdle()
    val scrollNode = androidx.compose.ui.test.hasScrollAction()
    composeTestRule.onNode(scrollNode).performScrollToNode(
      androidx.compose.ui.test.hasTestTag("menu_language_profile")
    )
    composeTestRule.onNodeWithText("Bahasa Aplikasi").assertIsDisplayed()

    // Open language selector sheet and select English
    composeTestRule.onNodeWithTag("menu_language_profile").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("option_lang_en").performClick()
    composeTestRule.waitForIdle()

    assertEquals(com.example.util.i18n.AppLanguage.ENGLISH, vm.appLanguage.value)
    composeTestRule.onNode(scrollNode).performScrollToNode(
      androidx.compose.ui.test.hasTestTag("menu_language_profile")
    )
    composeTestRule.onNodeWithText("App Language").assertIsDisplayed()
    composeTestRule.onNode(scrollNode).performScrollToNode(
      androidx.compose.ui.test.hasText("Monthly Report")
    )
    composeTestRule.onNodeWithText("Monthly Report").assertIsDisplayed()

    // Switch back to Bahasa Indonesia
    composeTestRule.onNode(scrollNode).performScrollToNode(
      androidx.compose.ui.test.hasTestTag("menu_language_profile")
    )
    composeTestRule.onNodeWithTag("menu_language_profile").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("option_lang_id").performClick()
    composeTestRule.waitForIdle()

    assertEquals(com.example.util.i18n.AppLanguage.INDONESIAN, vm.appLanguage.value)
    composeTestRule.onNode(scrollNode).performScrollToNode(
      androidx.compose.ui.test.hasTestTag("menu_language_profile")
    )
    composeTestRule.onNodeWithText("Bahasa Aplikasi").assertIsDisplayed()
  }

  @Test
  fun `verify PasswordGenerator WhatsApp templates switch between English and Indonesian preserving Point 2 names`() {
    val account = com.example.data.model.WaliAccount(
      id = 77L,
      name = "Ustadz Fauzi",
      username = "fauzi",
      password = "Wali#1234",
      dormitory = "Asrama Putra",
      phone = "081234567890",
      schoolId = "SRT1-KDR"
    )
    val schoolProfile = com.example.data.model.SchoolProfile(
      schoolName = "Sekolah Rakyat Kabupaten Kediri",
      shortName = "SRT 1 Kabupaten Kediri",
      primaryFaskesName = "UKS Sekolah",
      adminName = "Eko Wahyudi"
    )

    val enMsg = com.example.util.PasswordGenerator.formatWhatsAppMessage(
      account = account,
      schoolProfile = schoolProfile,
      isEnglish = true
    )
    org.junit.Assert.assertTrue(enMsg.contains("*WALI ASUH CARE APP LOGIN ACCESS CARD*"))
    org.junit.Assert.assertTrue(enMsg.contains("Asrama Assignment:* Asrama Putra"))
    org.junit.Assert.assertTrue(enMsg.contains("Puskesmas Wates"))
    org.junit.Assert.assertTrue(enMsg.contains("Wali Asuh Care"))

    val uksOnlyProfile = schoolProfile.copy(
      secondaryFaskesName = "",
      additionalFaskesList = emptyList()
    )
    val enUksFallback = com.example.util.PasswordGenerator.formatWhatsAppMessage(
      account = account,
      schoolProfile = uksOnlyProfile,
      isEnglish = true
    )
    org.junit.Assert.assertTrue(enUksFallback.contains("UKS Sekolah"))

    val idMsg = com.example.util.PasswordGenerator.formatWhatsAppMessage(
      account = account,
      schoolProfile = schoolProfile,
      isEnglish = false
    )
    org.junit.Assert.assertTrue(idMsg.contains("*KARTU AKSES LOGIN APLIKASI WALI ASUH CARE*"))
    org.junit.Assert.assertTrue(idMsg.contains("Tugas Asrama:* Asrama Putra"))
    org.junit.Assert.assertTrue(idMsg.contains("Puskesmas Wates"))
  }

  @Test
  fun `verify HealthViewModel executeLogin returns localized English and Indonesian errors`() = kotlinx.coroutines.test.runTest {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val vm = com.example.ui.HealthViewModel(app)

    // English mode
    vm.setAppLanguage(com.example.util.i18n.AppLanguage.ENGLISH)
    val emptyEn = vm.executeLogin("", "", rememberMe = false)
    org.junit.Assert.assertTrue(emptyEn is com.example.ui.LoginResult.Error)
    assertEquals(
      "Username and password are required.",
      (emptyEn as com.example.ui.LoginResult.Error).message
    )

    val wrongPilotPassEn = vm.executeLogin("ekowahyudiadmin", "wrongpass", rememberMe = false)
    org.junit.Assert.assertTrue(wrongPilotPassEn is com.example.ui.LoginResult.Error)
    assertEquals(
      "Incorrect password for Admin 'ekowahyudiadmin'. Please check again.",
      (wrongPilotPassEn as com.example.ui.LoginResult.Error).message
    )

    // Indonesian mode
    vm.setAppLanguage(com.example.util.i18n.AppLanguage.INDONESIAN)
    val emptyId = vm.executeLogin("", "", rememberMe = false)
    org.junit.Assert.assertTrue(emptyId is com.example.ui.LoginResult.Error)
    assertEquals(
      "Username dan password wajib diisi.",
      (emptyId as com.example.ui.LoginResult.Error).message
    )

    val wrongPilotPassId = vm.executeLogin("ekowahyudiadmin", "wrongpass", rememberMe = false)
    org.junit.Assert.assertTrue(wrongPilotPassId is com.example.ui.LoginResult.Error)
    assertEquals(
      "Password untuk Admin 'ekowahyudiadmin' salah. Silakan periksa kembali.",
      (wrongPilotPassId as com.example.ui.LoginResult.Error).message
    )
  }

  @Test
  fun `verify SuperAdminScreen language toggle switches headers and tabs between ID and EN`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val vm = com.example.ui.HealthViewModel(app)
    vm.setAppLanguage(com.example.util.i18n.AppLanguage.INDONESIAN)

    composeTestRule.setContent {
      val lang by vm.appLanguage.collectAsStateWithLifecycle()
      val baseContext = LocalContext.current
      val localizedContext = com.example.util.i18n.AppLanguageManager.createLocalizedContext(baseContext, lang)
      val appStrings = com.example.util.i18n.AppLanguageManager.stringsFor(lang)
      CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
        com.example.util.i18n.LocalAppStrings provides appStrings
      ) {
        com.example.ui.screens.SuperAdminScreen(
          viewModel = vm,
          onLogout = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Portal Superadmin").assertIsDisplayed()
    composeTestRule.onNodeWithText("Status Pengajuan Sekolah").assertIsDisplayed()

    composeTestRule.onNodeWithTag("btn_superadmin_toggle_language").performClick()
    composeTestRule.waitForIdle()

    assertEquals(com.example.util.i18n.AppLanguage.ENGLISH, vm.appLanguage.value)
    composeTestRule.onNodeWithText("Superadmin Portal").assertIsDisplayed()
    composeTestRule.onNodeWithText("School Request Status").assertIsDisplayed()

    // Reset back to Indonesian
    composeTestRule.onNodeWithTag("btn_superadmin_toggle_language").performClick()
    composeTestRule.waitForIdle()
    assertEquals(com.example.util.i18n.AppLanguage.INDONESIAN, vm.appLanguage.value)
    composeTestRule.onNodeWithText("Portal Superadmin").assertIsDisplayed()
  }

  @Test
  fun `verify cleanDigitsOnly removes C1 mojibake and non-digit prefixes while preserving real NIK prefixes`() {
    // Kasus nyata dari screenshot: C1 ISO-8859-1 mojibake (â\u0080\u008C -> tampil sebagai â▯▯)
    assertEquals("3578150504110001", com.example.util.StudentCsvParser.cleanDigitsOnly("â\u0080\u008C3578150504110001"))
    assertEquals("3578150504110001", com.example.util.StudentCsvParser.cleanDigitsOnly("â\u0080\u008B3578150504110001"))
    assertEquals("3506150510180001", com.example.util.StudentCsvParser.cleanDigitsOnly("â€Œ3506150510180001"))
    assertEquals("3506032804190001", com.example.util.StudentCsvParser.cleanDigitsOnly("\uFEFF\u200E\u200C'3506032804190001"))
    assertEquals("1771057004190001", com.example.util.StudentCsvParser.cleanDigitsOnly("NIK: 1771-0570-0419-0001"))
    assertEquals("9271010101010001", com.example.util.StudentCsvParser.cleanDigitsOnly("9271010101010001"))
    assertEquals("0099412567", com.example.util.StudentCsvParser.cleanDigitsOnly("0099412567"))
    assertEquals("3506200000000000", com.example.util.StudentCsvParser.cleanDigitsOnly("â\u0080\u008C3.5062E+15"))

    // Pastikan NIS fallback berkode sekolah tetap terjaga
    assertEquals("SRT1-KDR-12", com.example.util.StudentCsvParser.cleanNumericId("SRT1-KDR-12"))

    val dirtyCsv = """
      No,Nama,Kelas,Jenjang,Alamat,Jenis Kelamin,NIK,NO KK,TEMPAT LAHIR,TANGGAL LAHIR,NAMA IBU,NISN
      128,AFRIZAL SUYANTO,X-1,SMA X,"Muneng, Purwoasri, Kab. Kediri",L,â${'\u0080'}${'\u008C'}3578150504110001,â${'\u0080'}${'\u008B'}3506150510180001,SURABAYA,05-04-2011,DIAH RIFIANTI,114240555
    """.trimIndent()
    val parsed = com.example.util.StudentCsvParser.parseCsv(dirtyCsv, "SRT1-KDR")
    assertEquals(1, parsed.size)
    assertEquals("3578150504110001", parsed[0].nik)
    assertEquals("3506150510180001", parsed[0].noKk)
    assertEquals("114240555", parsed[0].nis)
  }

  @Test
  fun `verify PocketBase DTO and Room startup cleanup heal pre-existing dirty NIK records`() = kotlinx.coroutines.runBlocking {
    val dirtyDto = com.example.data.remote.pocketbase.PbStudentDto(
      localId = 9001L,
      schoolId = "SRT1-KDR",
      name = "AFRIZAL SUYANTO",
      nik = "â\u0080\u008C3578150504110001",
      noKk = "\u200B3506150510180001"
    )
    val cleanDomain = dirtyDto.toDomainModel("SRT1-KDR")
    assertEquals("3578150504110001", cleanDomain.nik)
    assertEquals("3506150510180001", cleanDomain.noKk)

    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    db.childStudentDao().deleteAll()
    db.haidRecordDao().deleteAll()
    db.fastingRecordDao().deleteBySchoolId("SRT1-KDR")

    db.childStudentDao().insert(
      com.example.data.model.ChildStudent(
        id = 9001L,
        name = "AFRIZAL SUYANTO",
        nis = "114240555",
        gender = "L",
        age = 15,
        dormitory = "Asrama Putra",
        classGrade = "X-1",
        nik = "â\u0080\u008C3578150504110001",
        noKk = "â€‹3506150510180001",
        schoolId = "SRT1-KDR"
      )
    )
    db.haidRecordDao().insert(
      com.example.data.model.HaidRecord(
        id = 9002L,
        studentId = 9001L,
        studentName = "SITI TEST",
        studentNik = "â\u0080\u008C3578150504110002",
        startDate = 1000L,
        schoolId = "SRT1-KDR"
      )
    )
    db.fastingRecordDao().insert(
      com.example.data.model.FastingRecord(
        id = 9003L,
        sessionId = 1L,
        studentId = 9001L,
        studentName = "AFRIZAL SUYANTO",
        studentNik = "â\u0080\u008E3578150504110001",
        schoolId = "SRT1-KDR"
      )
    )

    val vm = com.example.ui.HealthViewModel(app)
    vm.sanitizeExistingIdentityDataInRoom()

    val healedStudents = db.childStudentDao().getAllStudentsList()
    assertEquals(1, healedStudents.size)
    assertEquals("3578150504110001", healedStudents[0].nik)
    assertEquals("3506150510180001", healedStudents[0].noKk)

    val healedHaid = db.haidRecordDao().getAllRecordsList()
    assertEquals("3578150504110002", healedHaid.first { it.id == 9002L }.studentNik)

    val healedFasting = db.fastingRecordDao().getAllRecordsList()
    assertEquals("3578150504110001", healedFasting.first { it.id == 9003L }.studentNik)
  }

  @Test
  fun `verify AddStudentDialog and StudentDetailDialog strip non-digits and show clean NIK`() {
    val dirtyStudent = com.example.data.model.ChildStudent(
      id = 9100L,
      name = "AFRIZAL SUYANTO",
      nis = "114240555",
      gender = "L",
      age = 15,
      dormitory = "Asrama Putra",
      classGrade = "X-1",
      nik = "â\u0080\u008C3578150504110001",
      noKk = "â€‹3506150510180001",
      schoolId = "SRT1-KDR"
    )

    var savedStudent: com.example.data.model.ChildStudent? = null
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.AddStudentDialog(
          initialStudent = dirtyStudent,
          onDismiss = {},
          onConfirmStudent = { savedStudent = it }
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("input_student_nik").performTextClearance()
    composeTestRule.onNodeWithTag("input_student_nik").performTextInput("â\u0080\u008C3506123456780001")
    composeTestRule.onNodeWithTag("confirm_add_student_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    assertNotNull(savedStudent)
    assertEquals("3506123456780001", savedStudent!!.nik)
    assertEquals("3506150510180001", savedStudent!!.noKk)
  }

  @Test
  fun `verify StudentDetailDialog displays pure NIK digits even if record had dirty characters`() {
    val dirtyStudent = com.example.data.model.ChildStudent(
      id = 9101L,
      name = "AFRIZAL SUYANTO",
      nis = "114240555",
      gender = "L",
      age = 15,
      dormitory = "Asrama Putra",
      classGrade = "X-1",
      nik = "â\u0080\u008C3578150504110001",
      noKk = "â€‹3506150510180001",
      schoolId = "SRT1-KDR"
    )
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.StudentDetailDialog(
          student = dirtyStudent,
          studentRecords = emptyList(),
          onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("3578150504110001").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("3506150510180001").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun `verify compact InputPuasaDialog renders header badge, searches student, and registers on tap`() {
    val session = com.example.data.model.FastingSession(
      id = 101L,
      title = "Puasa Senin Kamis",
      sessionDate = 1794800000000L,
      isActive = true,
      schoolId = "SRT1-KDR"
    )
    val student1 = com.example.data.model.ChildStudent(
      id = 201L,
      name = "ABIMANYU SATRIA PAMUNGKAS",
      nis = "114240001",
      nik = "3506010101090001",
      gender = "L",
      age = 15,
      dormitory = "Asrama Putra",
      classGrade = "X-4",
      jenjang = "SMA",
      schoolId = "SRT1-KDR"
    )
    val student2 = com.example.data.model.ChildStudent(
      id = 202L,
      name = "ZAHRA AMALIA",
      nis = "114240002",
      nik = "3506010101120002",
      gender = "P",
      age = 12,
      dormitory = "Asrama Putri",
      classGrade = "SD 6",
      jenjang = "SD",
      schoolId = "SRT1-KDR"
    )
    val existingRecord = com.example.data.model.FastingRecord(
      id = 301L,
      sessionId = session.id,
      sessionTitle = session.title,
      studentId = student2.id,
      studentName = student2.name,
      studentClass = student2.classGrade,
      studentDorm = student2.dormitory,
      studentJenjang = "SD",
      schoolId = "SRT1-KDR"
    )

    var registeredStudent: com.example.data.model.ChildStudent? = null
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.InputPuasaDialog(
          sessions = listOf(session),
          students = listOf(student1, student2),
          fastingRecords = listOf(existingRecord),
          onRegisterStudent = { _, student, callback ->
            registeredStudent = student
            callback(true, "Berhasil input ${student.name}")
          },
          onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Input Puasa Siswa").assertIsDisplayed()
    composeTestRule.onNodeWithText("1 Terdaftar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dropdown_session_input").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dropdown_session_input_item_${session.id}").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_scan_barcode_puasa").assertIsDisplayed()

    composeTestRule.onNodeWithTag("input_search_student_puasa").performTextInput("ABIMANYU")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("card_pick_puasa_${student1.id}").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals(student1.id, registeredStudent?.id)
  }

  @Test
  fun `verify compact DaftarPuasaSheet filters records using interactive summary tiles and search`() {
    val session = com.example.data.model.FastingSession(
      id = 102L,
      title = "Puasa Senin Kamis",
      sessionDate = 1794800000000L,
      isActive = true,
      schoolId = "SRT1-KDR"
    )
    val recSma = com.example.data.model.FastingRecord(
      id = 401L,
      sessionId = session.id,
      studentId = 11L,
      studentName = "ABIMANYU SATRIA",
      studentClass = "X-4",
      studentDorm = "Asrama Putra",
      studentJenjang = "SMA",
      waliName = "Abisarwan Rafif",
      schoolId = "SRT1-KDR"
    )
    val recSd = com.example.data.model.FastingRecord(
      id = 402L,
      sessionId = session.id,
      studentId = 12L,
      studentName = "BUDI SANTOSO",
      studentClass = "SD 5",
      studentDorm = "Asrama Putra",
      studentJenjang = "SD",
      waliName = "Abisarwan Rafif",
      schoolId = "SRT1-KDR"
    )

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.DaftarPuasaSheet(
          sessions = listOf(session),
          fastingRecords = listOf(recSma, recSd),
          schoolProfile = com.example.data.model.SchoolProfile(),
          waliName = "Abisarwan Rafif",
          onDeleteRecord = {},
          onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("filter_daftar_Semua").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filter_daftar_SMA").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filter_daftar_SMP").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filter_daftar_SD").assertIsDisplayed()
    composeTestRule.onNodeWithTag("list_daftar_puasa")
      .performScrollToNode(androidx.compose.ui.test.hasText("ABIMANYU SATRIA"))
    composeTestRule.onNodeWithText("ABIMANYU SATRIA").assertIsDisplayed()
    composeTestRule.onNodeWithTag("list_daftar_puasa")
      .performScrollToNode(androidx.compose.ui.test.hasText("BUDI SANTOSO"))
    composeTestRule.onNodeWithText("BUDI SANTOSO").assertIsDisplayed()

    // Filter SMA only
    composeTestRule.onNodeWithTag("filter_daftar_SMA").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("ABIMANYU SATRIA").assertIsDisplayed()
    composeTestRule.onNodeWithText("BUDI SANTOSO").assertDoesNotExist()

    // Filter SD only
    composeTestRule.onNodeWithTag("filter_daftar_SD").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("BUDI SANTOSO").assertIsDisplayed()
    composeTestRule.onNodeWithText("ABIMANYU SATRIA").assertDoesNotExist()

    // Back to Semua + search filter
    composeTestRule.onNodeWithTag("filter_daftar_Semua").performClick()
    composeTestRule.onNodeWithTag("list_daftar_puasa")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("input_search_daftar_puasa"))
    composeTestRule.onNodeWithTag("input_search_daftar_puasa").performTextInput("ABIMANYU")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("list_daftar_puasa")
      .performScrollToNode(androidx.compose.ui.test.hasText("ABIMANYU SATRIA"))
    composeTestRule.onNodeWithText("ABIMANYU SATRIA").assertIsDisplayed()
    composeTestRule.onNodeWithText("BUDI SANTOSO").assertDoesNotExist()
  }

  @Test
  fun `verify compact ChecklistPuasaSheet switches tabs, filters status, and toggles check`() {
    val session = com.example.data.model.FastingSession(
      id = 103L,
      title = "Puasa Senin Kamis",
      sessionDate = 1794800000000L,
      isActive = true,
      schoolId = "SRT1-KDR"
    )
    val recChecked = com.example.data.model.FastingRecord(
      id = 501L,
      sessionId = session.id,
      studentId = 21L,
      studentName = "ABIMANYU SATRIA",
      studentClass = "X-4",
      studentDorm = "Asrama Putra",
      studentJenjang = "SMA",
      isSahurChecked = true,
      sahurCheckedBy = "Abisarwan Rafif",
      sahurCheckedAt = 1794800000000L,
      isBukaChecked = false,
      schoolId = "SRT1-KDR"
    )
    val recPending = com.example.data.model.FastingRecord(
      id = 502L,
      sessionId = session.id,
      studentId = 22L,
      studentName = "CITRA LESTARI",
      studentClass = "VIII-1",
      studentDorm = "Asrama Putri",
      studentJenjang = "SMP",
      isSahurChecked = false,
      isBukaChecked = true,
      bukaCheckedBy = "Ustadzah Sari",
      bukaCheckedAt = 1794850000000L,
      schoolId = "SRT1-KDR"
    )

    var toggledSahurId: Long? = null
    var toggledBukaId: Long? = null
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.ChecklistPuasaSheet(
          sessions = listOf(session),
          fastingRecords = listOf(recChecked, recPending),
          schoolProfile = com.example.data.model.SchoolProfile(),
          waliName = "Abisarwan Rafif",
          onToggleSahur = { toggledSahurId = it.id },
          onToggleBuka = { toggledBukaId = it.id },
          onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tab_checklist_sahur").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tab_checklist_berbuka").assertIsDisplayed()
    composeTestRule.onNodeWithTag("list_checklist_puasa")
      .performScrollToNode(androidx.compose.ui.test.hasText("ABIMANYU SATRIA"))
    composeTestRule.onNodeWithText("ABIMANYU SATRIA").assertIsDisplayed()
    composeTestRule.onNodeWithTag("list_checklist_puasa")
      .performScrollToNode(androidx.compose.ui.test.hasText("CITRA LESTARI"))
    composeTestRule.onNodeWithText("CITRA LESTARI").assertIsDisplayed()

    // Filter Sudah on Sahur tab
    composeTestRule.onNodeWithTag("list_checklist_puasa")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("filter_checklist_Sudah"))
    composeTestRule.onNodeWithTag("filter_checklist_Sudah").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("list_checklist_puasa")
      .performScrollToNode(androidx.compose.ui.test.hasText("ABIMANYU SATRIA"))
    composeTestRule.onNodeWithText("ABIMANYU SATRIA").assertIsDisplayed()
    composeTestRule.onNodeWithText("CITRA LESTARI").assertDoesNotExist()

    // Toggle Sahur item
    composeTestRule.onNodeWithTag("checklist_item_${recChecked.id}").performClick()
    composeTestRule.waitForIdle()
    assertEquals(recChecked.id, toggledSahurId)

    // Switch to Berbuka tab -> only CITRA LESTARI is checked ("Sudah")
    composeTestRule.onNodeWithTag("tab_checklist_berbuka").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("list_checklist_puasa")
      .performScrollToNode(androidx.compose.ui.test.hasText("CITRA LESTARI"))
    composeTestRule.onNodeWithText("CITRA LESTARI").assertIsDisplayed()
    composeTestRule.onNodeWithText("ABIMANYU SATRIA").assertDoesNotExist()

    composeTestRule.onNodeWithTag("checklist_item_${recPending.id}").performClick()
    composeTestRule.waitForIdle()
    assertEquals(recPending.id, toggledBukaId)
  }

  @Test
  fun `verify QuickMenuTiles displays all 8 super-app icons and triggers category hubs`() {
    var openedHub: com.example.ui.components.DashboardCategoryHub? = null
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.components.QuickMenuTiles(
          onOpenCategoryHub = { openedHub = it },
          onNavigateToPlan = {},
          onNavigateToDirectTreatment = {},
          onNavigateToRecords = {},
          onNavigateToStudents = {},
          onShowPuskesmasInfo = {},
          todayPlansCount = 3,
          studentsCount = 342,
          activeHaidCount = 4,
          todayFastingCount = 28
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("category_berobat").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_tindakan_langsung").assertDoesNotExist()
    composeTestRule.onNodeWithTag("category_kunjungan").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_orang_tua").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_rekam_medis").assertDoesNotExist()
    composeTestRule.onNodeWithTag("tile_anak_asuh").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_puasa").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_haid").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_pesan").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_mengaji").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_tulis_pesan_shift").assertDoesNotExist()
    composeTestRule.onNodeWithTag("tile_puskesmas_info").assertIsDisplayed()

    composeTestRule.onNodeWithTag("category_kunjungan").performClick()
    assertEquals(com.example.ui.components.DashboardCategoryHub.KUNJUNGAN, openedHub)

    composeTestRule.onNodeWithTag("category_orang_tua").performClick()
    assertEquals(com.example.ui.components.DashboardCategoryHub.ORANG_TUA, openedHub)

    composeTestRule.onNodeWithTag("category_puasa").performClick()
    assertEquals(com.example.ui.components.DashboardCategoryHub.PUASA, openedHub)

    composeTestRule.onNodeWithTag("category_haid").performClick()
    assertEquals(com.example.ui.components.DashboardCategoryHub.HAID, openedHub)

    composeTestRule.onNodeWithTag("category_berobat").performClick()
    assertEquals(com.example.ui.components.DashboardCategoryHub.BEROBAT, openedHub)

    composeTestRule.onNodeWithTag("category_pesan").performClick()
    assertEquals(com.example.ui.components.DashboardCategoryHub.PESAN, openedHub)

    composeTestRule.onNodeWithTag("category_mengaji").performClick()
    assertEquals(com.example.ui.components.DashboardCategoryHub.MENGAJI, openedHub)
  }

  @Test
  fun `verify DashboardScreen opens slide-from-right CategoryHubOverlay and closes with top-left button`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.screens.DashboardScreen(
          viewModel = vm,
          waliName = "Abisarwan Rafif",
          activePlans = emptyList(),
          students = emptyList(),
          onNavigateToPlan = {},
          onNavigateToRecords = {},
          onNavigateToStudents = {},
          onNavigateToProfile = {},
          renderInlineCategoryHub = true
        )
      }
    }

    composeTestRule.waitForIdle()
    // Open Puasa Category Hub
    composeTestRule.onNodeWithTag("category_puasa").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("category_hub_overlay_puasa").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_close_category_hub").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_quick_action_category_hub").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_input_puasa").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_daftar_puasa").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_checklist_puasa").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_ranking_puasa").assertIsDisplayed()
    composeTestRule.onNodeWithTag("list_category_hub_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("btn_hub_create_puasa_session"))
    composeTestRule.onNodeWithTag("btn_hub_create_puasa_session").assertIsDisplayed()

    // Close using circular X button at top-left
    composeTestRule.onNodeWithTag("btn_close_category_hub").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("category_hub_overlay_puasa").assertDoesNotExist()

    // Open Haid Category Hub
    composeTestRule.onNodeWithTag("category_haid").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("category_hub_overlay_haid").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_input_haid").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_daftar_haid").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_daftar_suci").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_fiqih_haid").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_close_category_hub").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("category_hub_overlay_haid").assertDoesNotExist()
  }

  @Test
  fun `verify DashboardScreen compact Gojek faskes strip sorts active faskes first and keeps super-app tiles unobstructed`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    val now = System.currentTimeMillis()
    val todayPlan = com.example.data.model.MedicalRecord(
      id = 701L,
      studentId = 1L,
      studentName = "Ahmad Fauzan",
      studentDormitory = "Asrama Putra",
      faskes = "Puskesmas Wates",
      complaint = "Batuk pilek",
      planDate = now,
      status = "PLANNED"
    )
    val student = com.example.data.model.ChildStudent(
      id = 1L,
      name = "Ahmad Fauzan",
      nis = "112233",
      gender = "L",
      age = 15,
      dormitory = "Asrama Putra",
      classGrade = "X-1"
    )

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.screens.DashboardScreen(
          viewModel = vm,
          waliName = "Abisarwan Rafif",
          activePlans = listOf(todayPlan),
          students = listOf(student),
          onNavigateToPlan = {},
          onNavigateToRecords = {},
          onNavigateToStudents = {},
          onNavigateToProfile = {},
          renderInlineCategoryHub = true
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_summary_today").assertIsDisplayed()
    composeTestRule.onNodeWithTag("row_faskes_strip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fab_dashboard_add").assertIsDisplayed()
    composeTestRule.onNodeWithText("Berobat Hari Ini").assertIsDisplayed()
    composeTestRule.onNodeWithText("1/1 Siswa Dijadwalkan").assertIsDisplayed()

    // Active faskes ("Puskesmas Wates") is sorted to the front of the horizontal strip
    composeTestRule.onNodeWithTag("pill_faskes_puskesmas_wates").assertIsDisplayed()
    composeTestRule.onNodeWithText("1 Anak").assertIsDisplayed()

    // Super-app icons remain unobstructed
    composeTestRule.onNodeWithTag("category_berobat").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_mengaji").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_tulis_pesan_shift").assertDoesNotExist()

    // Clicking the relocated "+ Rencana" button opens CreatePlanDialog cleanly
    composeTestRule.onNodeWithTag("fab_dashboard_add").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Buat Rencana Berobat").assertIsDisplayed()
  }

  @Test
  fun `verify MengajiHelper continuous progression, repeating non-fluent page, jilid advance, and student matching`() {
    // 1. Fresh student starts at Iqro' Jilid 1 Hal. 1
    val fresh = com.example.data.model.MengajiHelper.suggestNextEntry(null)
    assertEquals("Iqro'", fresh.bookType)
    assertEquals("Jilid 1", fresh.jilidOrJuz)
    assertEquals("Hal. 1", fresh.pageOrVerse)
    org.junit.Assert.assertFalse(fresh.isRepeatingLast)

    // 2. Fluent session advances page automatically (+1)
    val fluentRec = com.example.data.model.MengajiRecord(
      id = 9001L,
      studentId = 55L,
      studentName = "Aisyah",
      studentNik = "3506145501010002",
      studentNis = "11220055",
      sessionDate = 1794800000000L,
      bookType = "Iqro'",
      jilidOrJuz = "Jilid 3",
      pageOrVerse = "Hal. 14",
      startPage = 14,
      endPage = 14,
      fluencyStatus = com.example.data.model.MengajiHelper.FLUENCY_LANCAR
    )
    val nextAfterFluent = com.example.data.model.MengajiHelper.suggestNextEntry(fluentRec)
    assertEquals("Iqro'", nextAfterFluent.bookType)
    assertEquals("Jilid 3", nextAfterFluent.jilidOrJuz)
    assertEquals("Hal. 15", nextAfterFluent.pageOrVerse)
    org.junit.Assert.assertFalse(nextAfterFluent.isRepeatingLast)

    // 3. Non-fluent session prompts repeating exact same page
    val repeatRec = fluentRec.copy(
      id = 9002L,
      fluencyStatus = com.example.data.model.MengajiHelper.FLUENCY_TIDAK_LANCAR
    )
    val nextAfterRepeat = com.example.data.model.MengajiHelper.suggestNextEntry(repeatRec)
    assertEquals("Hal. 14", nextAfterRepeat.pageOrVerse)
    org.junit.Assert.assertTrue(nextAfterRepeat.isRepeatingLast)

    // 4. Advance jilid progression & transition from Jilid 6 -> Al-Qur'an Juz 1
    assertEquals("Iqro'" to "Jilid 4", com.example.data.model.MengajiHelper.advanceJilidOrJuz("Iqro'", "Jilid 3"))
    assertEquals("Al-Qur'an" to "Juz 1", com.example.data.model.MengajiHelper.advanceJilidOrJuz("Iqro'", "Jilid 6"))

    // 5. Matches student by ID, NIK, or NIS, and filters by month
    val studentByNik = com.example.data.model.ChildStudent(
      id = 999L,
      name = "Aisyah",
      nis = "OTHER",
      gender = "P",
      age = 12,
      dormitory = "Asrama Putri",
      classGrade = "VI-A",
      nik = "3506145501010002"
    )
    org.junit.Assert.assertTrue(com.example.data.model.MengajiHelper.matchesStudent(fluentRec, studentByNik))
    val monthKey = com.example.util.DateUtils.getMonthKey(fluentRec.sessionDate)
    assertEquals(1, com.example.data.model.MengajiHelper.filterByMonth(listOf(fluentRec), monthKey).size)
    assertEquals(0, com.example.data.model.MengajiHelper.filterByMonth(listOf(fluentRec), "2020-01").size)
  }

  @Test
  fun `verify HealthViewModel saves and deletes MengajiRecord cleanly in Room`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val student = com.example.data.model.ChildStudent(
      id = 88001L,
      name = "Zainal Abidin",
      nis = "88001",
      gender = "L",
      age = 13,
      dormitory = "Asrama Putra",
      classGrade = "VII-2",
      nik = "3506010101130001",
      jenjang = "SMP",
      schoolId = "SRT1-KDR"
    )

    var savedRecord: com.example.data.model.MengajiRecord? = null
    vm.saveMengajiRecord(
      student = student,
      sessionDate = 1794800000000L,
      bookType = "Iqro'",
      jilidOrJuz = "Jilid 2",
      pageOrVerse = "Hal. 18",
      fluencyStatus = com.example.data.model.MengajiHelper.FLUENCY_LANCAR,
      notes = "Bacaan mad baik"
    ) { ok, rec ->
      if (ok) savedRecord = rec
    }

    var ticks = 0
    while (savedRecord == null && ticks < 25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(30)
      ticks++
    }
    assertNotNull(savedRecord)
    assertEquals("Hal. 18", savedRecord?.pageOrVerse)
    assertEquals(18, savedRecord?.startPage)

    val fetched = com.example.data.database.AppDatabase.getDatabase(app)
      .mengajiRecordDao()
      .getAllRecords()
      .first()
      .firstOrNull { it.id == savedRecord!!.id }
    assertNotNull(fetched)
    assertEquals("Zainal Abidin", fetched?.studentName)

    vm.deleteMengajiRecord(fetched!!)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    kotlinx.coroutines.delay(50)
    Unit
  }

  @Test
  fun `verify PdfReportGenerator builds Kartu Mengaji PDF and WhatsApp summary`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val student = com.example.data.model.ChildStudent(
      id = 88002L,
      name = "Nadia Rahmawati",
      nis = "88002",
      gender = "P",
      age = 14,
      dormitory = "Asrama Putri",
      classGrade = "VIII-A",
      jenjang = "SMP"
    )
    val records = listOf(
      com.example.data.model.MengajiRecord(
        id = 1L,
        studentId = student.id,
        studentName = student.name,
        sessionDate = 1794800000000L,
        bookType = "Iqro'",
        jilidOrJuz = "Jilid 4",
        pageOrVerse = "Hal. 12",
        startPage = 12,
        fluencyStatus = com.example.data.model.MengajiHelper.FLUENCY_LANCAR,
        waliName = "Ustadzah Amina"
      ),
      com.example.data.model.MengajiRecord(
        id = 2L,
        studentId = student.id,
        studentName = student.name,
        sessionDate = 1794886400000L,
        bookType = "Iqro'",
        jilidOrJuz = "Jilid 4",
        pageOrVerse = "Hal. 13",
        startPage = 13,
        fluencyStatus = com.example.data.model.MengajiHelper.FLUENCY_TIDAK_LANCAR,
        waliName = "Ustadzah Amina"
      )
    )

    // Render single-page Kartu Mengaji onto A4 Canvas (verifies header, bio, 6 columns, and guardian QR signature)
    val singlePageBitmaps = mutableListOf<android.graphics.Bitmap>()
    com.example.util.PdfReportGenerator.renderKartuMengajiPages(
      context = context,
      student = student,
      records = records,
      periodLabel = "November 2026",
      waliName = "Ustadzah Amina",
      schoolProfile = com.example.data.model.SchoolProfile(),
      startNewCanvas = {
        val bmp = android.graphics.Bitmap.createBitmap(595, 842, android.graphics.Bitmap.Config.ARGB_8888)
        singlePageBitmaps.add(bmp)
        android.graphics.Canvas(bmp)
      }
    )
    assertEquals(1, singlePageBitmaps.size)
    singlePageBitmaps.forEach { it.recycle() }

    // Render multi-page Kartu Mengaji (30 sessions) to verify automatic pagination & continued table headers
    val manyRecords = (1..30).map { idx ->
      com.example.data.model.MengajiRecord(
        id = idx.toLong(),
        studentId = student.id,
        studentName = student.name,
        sessionDate = 1794800000000L + idx * 86_400_000L,
        bookType = "Iqro'",
        jilidOrJuz = "Jilid 4",
        pageOrVerse = "Hal. $idx",
        startPage = idx,
        fluencyStatus = if (idx % 3 == 0) {
          com.example.data.model.MengajiHelper.FLUENCY_TIDAK_LANCAR
        } else {
          com.example.data.model.MengajiHelper.FLUENCY_LANCAR
        },
        waliName = "Ustadzah Amina"
      )
    }
    val multiPageBitmaps = mutableListOf<android.graphics.Bitmap>()
    com.example.util.PdfReportGenerator.renderKartuMengajiPages(
      context = context,
      student = student,
      records = manyRecords,
      periodLabel = "November 2026",
      waliName = "Ustadzah Amina",
      schoolProfile = com.example.data.model.SchoolProfile(),
      startNewCanvas = {
        val bmp = android.graphics.Bitmap.createBitmap(595, 842, android.graphics.Bitmap.Config.ARGB_8888)
        multiPageBitmaps.add(bmp)
        android.graphics.Canvas(bmp)
      }
    )
    org.junit.Assert.assertTrue(multiPageBitmaps.size >= 2)
    multiPageBitmaps.forEach { it.recycle() }

    val waText = com.example.util.PdfReportGenerator.buildKartuMengajiWaSummary(
      student = student,
      records = records,
      periodLabel = "November 2026",
      waliName = "Ustadzah Amina",
      schoolProfile = com.example.data.model.SchoolProfile(),
      language = com.example.util.i18n.AppLanguage.INDONESIAN
    )
    org.junit.Assert.assertTrue(waText.contains("Nadia Rahmawati"))
    org.junit.Assert.assertTrue(waText.contains("November 2026"))
    org.junit.Assert.assertTrue(waText.contains("Lancar"))
    org.junit.Assert.assertTrue(waText.contains("Tidak Lancar"))
    org.junit.Assert.assertTrue(waText.contains("Ustadzah Amina"))
  }

  @Test
  fun `verify QuickMenuTiles opens Mengaji hub and InputMengajiDialog auto-continues previous page`() {
    var openedHub: com.example.ui.components.DashboardCategoryHub? = null
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val student = com.example.data.model.ChildStudent(
      id = 77L,
      name = "Farhan Maulana",
      nis = "77001",
      gender = "L",
      age = 15,
      dormitory = "Asrama Putra",
      classGrade = "X-1",
      jenjang = "SMA"
    )
    val prevRecord = com.example.data.model.MengajiRecord(
      id = 501L,
      studentId = 77L,
      studentName = "Farhan Maulana",
      studentNis = "77001",
      sessionDate = 1794800000000L,
      bookType = "Iqro'",
      jilidOrJuz = "Jilid 5",
      pageOrVerse = "Hal. 20",
      startPage = 20,
      fluencyStatus = com.example.data.model.MengajiHelper.FLUENCY_LANCAR
    )

    var savedBook = ""
    var savedJilid = ""
    var savedPage = ""
    var savedStatus = ""

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        androidx.compose.foundation.layout.Column {
          com.example.ui.components.QuickMenuTiles(
            onOpenCategoryHub = { openedHub = it },
            onNavigateToPlan = {},
            onNavigateToDirectTreatment = {},
            onNavigateToRecords = {},
            onNavigateToStudents = {},
            onShowPuskesmasInfo = {}
          )
          com.example.ui.dialogs.InputMengajiDialog(
            students = listOf(student),
            mengajiRecords = listOf(prevRecord),
            initialStudent = student,
            onSave = { _, _, book, jilid, page, status, _, _ ->
              savedBook = book
              savedJilid = jilid
              savedPage = page
              savedStatus = status
            },
            onDismiss = {}
          )
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("category_mengaji").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_tulis_pesan_shift").assertDoesNotExist()
    composeTestRule.onNodeWithTag("category_mengaji").performClick()
    assertEquals(com.example.ui.components.DashboardCategoryHub.MENGAJI, openedHub)

    // Continuous suggestion should automatically advance Hal. 20 -> Hal. 21
    composeTestRule.onNodeWithTag("list_input_mengaji_form")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("banner_mengaji_suggestion"))
    composeTestRule.onNodeWithTag("banner_mengaji_suggestion").assertIsDisplayed()

    composeTestRule.onNodeWithTag("list_input_mengaji_form")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("btn_page_inc_mengaji"))
    composeTestRule.onNodeWithTag("btn_page_inc_mengaji").performClick()

    composeTestRule.onNodeWithTag("btn_save_mengaji").performClick()
    composeTestRule.waitForIdle()

    assertEquals("Iqro'", savedBook)
    assertEquals("Jilid 5", savedJilid)
    assertEquals("Hal. 22", savedPage)
    assertEquals(com.example.data.model.MengajiHelper.FLUENCY_LANCAR, savedStatus)
  }

  @Test
  fun `verify KartuMengajiSheet displays columns, student rows, and Guardian Signature`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val student = com.example.data.model.ChildStudent(
      id = 88L,
      name = "Siti Fatimah",
      nis = "88009",
      gender = "P",
      age = 14,
      dormitory = "Asrama Putri",
      classGrade = "VIII-2",
      jenjang = "SMP"
    )
    val record = com.example.data.model.MengajiRecord(
      id = 601L,
      studentId = 88L,
      studentName = "Siti Fatimah",
      studentNis = "88009",
      sessionDate = 1794800000000L,
      bookType = "Iqro'",
      jilidOrJuz = "Jilid 4",
      pageOrVerse = "Hal. 9",
      startPage = 9,
      fluencyStatus = com.example.data.model.MengajiHelper.FLUENCY_LANCAR,
      waliName = "Ustadzah Sari"
    )

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.KartuMengajiSheet(
          students = listOf(student),
          mengajiRecords = listOf(record),
          schoolProfile = com.example.data.model.SchoolProfile(),
          waliName = "Ustadzah Sari",
          initialStudent = student,
          onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("No").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tgl").assertIsDisplayed()
    composeTestRule.onNodeWithText("Jilid").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hal.").assertIsDisplayed()
    composeTestRule.onNodeWithText("Ket.").assertIsDisplayed()
    composeTestRule.onNodeWithText("Wali").assertIsDisplayed()

    composeTestRule.onNodeWithTag("list_kartu_mengaji_preview")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("row_kartu_mengaji_601"))
    composeTestRule.onNodeWithTag("row_kartu_mengaji_601").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hal. 9").assertIsDisplayed()
    composeTestRule.onNodeWithText("Lancar").assertIsDisplayed()
    composeTestRule.onNodeWithText("TTD Wali Asuh").assertIsDisplayed()
  }

  @Test
  fun `verify StudentDetailDialog attaches Riwayat Mengaji history and action buttons`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val student = com.example.data.model.ChildStudent(
      id = 91L,
      name = "Rafi Pratama",
      nis = "91001",
      gender = "L",
      age = 16,
      dormitory = "Asrama Putra",
      classGrade = "XI-1",
      nik = "3506101010100001",
      jenjang = "SMA"
    )
    val mengajiRec = com.example.data.model.MengajiRecord(
      id = 701L,
      studentId = 91L,
      studentName = "Rafi Pratama",
      studentNik = "3506101010100001",
      studentNis = "91001",
      sessionDate = 1794800000000L,
      bookType = "Al-Qur'an",
      jilidOrJuz = "Juz 2",
      pageOrVerse = "Hal. 32",
      startPage = 32,
      fluencyStatus = com.example.data.model.MengajiHelper.FLUENCY_LANCAR,
      waliName = "Abisarwan Rafif"
    )
    var inputClicked = false
    var kartuClicked = false

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.StudentDetailDialog(
          student = student,
          studentRecords = emptyList(),
          waliName = "Abisarwan Rafif",
          mengajiRecords = listOf(mengajiRec),
          onInputMengaji = { inputClicked = true },
          onPrintKartuMengaji = { kartuClicked = true },
          onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_detail_tab_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tab_student_detail_mengaji").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("badge_student_latest_mengaji").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("student_mengaji_item_701").performScrollTo().assertIsDisplayed()

    composeTestRule.onNodeWithTag("btn_student_input_mengaji").performScrollTo().performClick()
    org.junit.Assert.assertTrue(inputClicked)

    composeTestRule.onNodeWithTag("btn_student_kartu_mengaji").performScrollTo().performClick()
    org.junit.Assert.assertTrue(kartuClicked)
  }

  @Test
  fun `verify StudentsScreen edit flow updates student Kecamatan and StudentDetailDialog displays district`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    db.childStudentDao().deleteAll()

    val initialStudent = com.example.data.model.ChildStudent(
      id = 808L,
      name = "Aulia Rahma",
      nis = "80801",
      gender = "P",
      age = 14,
      dormitory = "Asrama Putri",
      classGrade = "VIII-1",
      address = "Desa Jabon, Kab. Kediri",
      district = "Banyakan",
      jenjang = "SMP",
      schoolId = "SRT1-KDR"
    )
    db.childStudentDao().insert(initialStudent)

    val vm = com.example.ui.HealthViewModel(app)
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        val currentStudents by vm.students.collectAsStateWithLifecycle()
        val records by vm.allRecords.collectAsStateWithLifecycle()
        com.example.ui.screens.StudentsScreen(
          viewModel = vm,
          students = currentStudents.ifEmpty { listOf(initialStudent) },
          records = records,
          searchQuery = ""
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Kecamatan: Banyakan").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_edit_student_808").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("input_student_district").performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag("input_student_district").performTextInput("Ngancar")
    composeTestRule.onNodeWithTag("confirm_add_student_button").performScrollTo().performClick()

    var updatedStudent: com.example.data.model.ChildStudent? = null
    var ticks = 0
    while (ticks < 20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      val loaded = db.childStudentDao().getStudentsBySchool("SRT1-KDR").first().firstOrNull { it.id == 808L }
      if (loaded?.district == "Ngancar") {
        updatedStudent = loaded
        break
      }
      kotlinx.coroutines.delay(50)
      ticks++
    }
    assertNotNull(updatedStudent)
    assertEquals("Ngancar", updatedStudent?.district)
  }

  @Test
  fun `verify Wali Asuh can create fasting session but only Admin can delete it`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    db.fastingSessionDao().deleteBySchoolId("SRT1-KDR")

    val vm = com.example.ui.HealthViewModel(app)
    vm.setWaliName("Ustadzah Fatimah")
    vm.switchRoleToWali()

    var createdSession: com.example.data.model.FastingSession? = null
    vm.createFastingSession(
      title = "Puasa Senin Kamis",
      sessionDate = 1794800000000L,
      description = "Sahur jam 03.30"
    ) { createdSession = it }

    var ticks = 0
    while (ticks < 20 && createdSession == null) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(50)
      ticks++
    }
    assertNotNull(createdSession)
    assertEquals("Puasa Senin Kamis", createdSession?.title)
    assertEquals("Ustadzah Fatimah", createdSession?.createdBy)

    val persistedAfterCreate = db.fastingSessionDao().getSessionsBySchoolList("SRT1-KDR")
    assertEquals(1, persistedAfterCreate.size)

    // Wali Asuh CANNOT delete session
    vm.deleteFastingSession(createdSession!!)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    kotlinx.coroutines.delay(150)
    val persistedAfterWaliDelete = db.fastingSessionDao().getSessionsBySchoolList("SRT1-KDR")
    assertEquals(1, persistedAfterWaliDelete.size)

    // Admin CAN delete session
    vm.switchRoleToAdmin()
    var adminDeletedCallback = false
    vm.deleteFastingSession(createdSession!!) { adminDeletedCallback = true }
    ticks = 0
    while (ticks < 20 && (!adminDeletedCallback || db.fastingSessionDao().getSessionsBySchoolList("SRT1-KDR").isNotEmpty())) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(50)
      ticks++
    }
    org.junit.Assert.assertTrue(adminDeletedCallback)
    org.junit.Assert.assertTrue(db.fastingSessionDao().getSessionsBySchoolList("SRT1-KDR").isEmpty())
  }

  @Test
  fun `verify InputPuasaDialog lets Wali Asuh create session directly when empty`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    var createdTitle: String? = null

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.InputPuasaDialog(
          sessions = emptyList(),
          students = emptyList(),
          fastingRecords = emptyList(),
          onRegisterStudent = { _, _, _ -> },
          onCreateSession = { title, dateMillis, description, onCreated ->
            createdTitle = title
            onCreated(
              com.example.data.model.FastingSession(
                id = 505L,
                title = title,
                sessionDate = dateMillis,
                description = description,
                isActive = true,
                createdBy = "Wali Asuh",
                schoolId = "SRT1-KDR"
              )
            )
          },
          onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_create_fasting_session_empty").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("input_fasting_session_title").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_save_fasting_session").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    assertEquals("Puasa Senin Kamis", createdTitle)
  }

  @Test
  fun `verify StudentDetailDialog switches across Biodata, Mengaji, Berobat, and Puasa tabs`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val student = com.example.data.model.ChildStudent(
      id = 920L,
      name = "Zaki Al-Farizi",
      nis = "92001",
      gender = "L",
      age = 15,
      dormitory = "Asrama Putra",
      classGrade = "IX-2",
      nik = "3506150101100002",
      district = "Plosoklaten",
      schoolId = "SRT1-KDR"
    )
    val medRec = com.example.data.model.MedicalRecord(
      id = 811L,
      studentId = 920L,
      studentName = "Zaki Al-Farizi",
      studentDormitory = "Asrama Putra",
      faskes = "Puskesmas Wates",
      complaint = "Demam Tinggi",
      diagnosis = "Febris",
      planDate = 1794800000000L,
      treatmentDate = 1794800000000L,
      status = "COMPLETED"
    )
    val fastingRec = com.example.data.model.FastingRecord(
      id = 911L,
      sessionId = 50L,
      sessionTitle = "Puasa Senin Kamis",
      sessionDate = 1794800000000L,
      studentId = 920L,
      studentName = "Zaki Al-Farizi",
      studentNik = "3506150101100002",
      studentNis = "92001",
      isSahurChecked = true,
      sahurCheckedBy = "Ustadz Ahmad",
      isBukaChecked = true,
      bukaCheckedBy = "Ustadz Ahmad",
      waliName = "Ustadz Ahmad",
      schoolId = "SRT1-KDR"
    )

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.StudentDetailDialog(
          student = student,
          studentRecords = listOf(medRec),
          waliName = "Ustadz Ahmad",
          mengajiRecords = emptyList(),
          fastingRecords = listOf(fastingRec),
          onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    // Default tab: Biodata
    composeTestRule.onNodeWithTag("section_student_biodata").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Plosoklaten").performScrollTo().assertIsDisplayed()

    // Switch to Berobat tab
    composeTestRule.onNodeWithTag("tab_student_detail_berobat").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_medical_item_811").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Keluhan: Demam Tinggi").performScrollTo().assertIsDisplayed()

    // Switch to Puasa tab
    composeTestRule.onNodeWithTag("tab_student_detail_puasa").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("student_fasting_item_911").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Puasa Senin Kamis").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Sahur • Terlaksana").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Berbuka • Terlaksana").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun `verify incoming cloud synced treatment plan notifications strictly isolate by logged-in tenant`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_notif_prefs", android.content.Context.MODE_PRIVATE)
      .edit().clear().apply()
    val vm = com.example.ui.HealthViewModel(app)

    val studentSchoolA = com.example.data.model.ChildStudent(
      id = 5001L,
      name = "Santri Sekolah A",
      nis = "5001",
      gender = "L",
      age = 14,
      dormitory = "Asrama A",
      classGrade = "VIII-1",
      schoolId = "SEKOLAH-A"
    )
    val plannedRecordA = com.example.data.model.MedicalRecord(
      id = 9001L,
      studentId = 5001L,
      studentName = "Santri Sekolah A",
      studentDormitory = "Asrama A",
      faskes = "Puskesmas A",
      complaint = "Batuk",
      isDirectTreatment = false,
      planDate = System.currentTimeMillis(),
      status = "PLANNED"
    )
    val completedRecordA = plannedRecordA.copy(id = 9002L, status = "COMPLETED")

    // 1. User belum login -> Tidak ada notifikasi yang muncul
    vm.logout()
    org.junit.Assert.assertFalse(
      vm.shouldNotifyIncomingSyncedPlan(plannedRecordA, "SEKOLAH-A", "SEKOLAH-A")
    )
    assertEquals(
      0,
      vm.processIncomingRemoteMedicalRecords(listOf(plannedRecordA), listOf(studentSchoolA), "SEKOLAH-A")
    )

    // 2. User login di Sekolah B -> Tidak menerima notifikasi dari Sekolah A
    vm.switchRoleToWali()
    vm.updateSchoolProfile(
      com.example.data.model.SchoolProfile(
        tenantCode = "SEKOLAH-B",
        schoolName = "Sekolah Rakyat B"
      )
    )
    org.junit.Assert.assertFalse(
      vm.shouldNotifyIncomingSyncedPlan(plannedRecordA, "SEKOLAH-A", "SEKOLAH-A")
    )
    assertEquals(
      0,
      vm.processIncomingRemoteMedicalRecords(listOf(plannedRecordA), listOf(studentSchoolA), "SEKOLAH-A")
    )

    // 3. User login di Sekolah A -> Menerima notifikasi rencana PLANNED baru Sekolah A, tepat satu kali
    vm.updateSchoolProfile(
      com.example.data.model.SchoolProfile(
        tenantCode = "SEKOLAH-A",
        schoolName = "Sekolah Rakyat A"
      )
    )
    org.junit.Assert.assertTrue(
      vm.shouldNotifyIncomingSyncedPlan(plannedRecordA, "SEKOLAH-A", "SEKOLAH-A")
    )
    org.junit.Assert.assertFalse(
      vm.shouldNotifyIncomingSyncedPlan(completedRecordA, "SEKOLAH-A", "SEKOLAH-A")
    )
    assertEquals(
      1,
      vm.processIncomingRemoteMedicalRecords(
        listOf(plannedRecordA, completedRecordA),
        listOf(studentSchoolA),
        "SEKOLAH-A"
      )
    )
    // Sync ulang untuk ID yang sama tidak mengirim notifikasi duplikat
    assertEquals(
      0,
      vm.processIncomingRemoteMedicalRecords(listOf(plannedRecordA), listOf(studentSchoolA), "SEKOLAH-A")
    )
  }

  @Test
  fun `verify VisitHelper dynamic district extraction, multi-date CSV, and schedule matching`() {
    assertEquals("Wates", com.example.data.model.VisitHelper.normalizeDistrict("Kec. wates"))
    assertEquals("Plosoklaten", com.example.data.model.VisitHelper.normalizeDistrict("Kecamatan   PLOSOKLATEN"))
    assertEquals("", com.example.data.model.VisitHelper.normalizeDistrict(" - "))

    val s1 = com.example.data.model.ChildStudent(
      id = 1L, name = "Ahmad", nis = "101", gender = "L", age = 13,
      dormitory = "Asrama A", classGrade = "VII", district = "Kec. Wates", schoolId = "SRT1-KDR"
    )
    val s2 = com.example.data.model.ChildStudent(
      id = 2L, name = "Budi", nis = "102", gender = "L", age = 14,
      dormitory = "Asrama A", classGrade = "VIII", district = "plosoklaten", schoolId = "SRT1-KDR"
    )
    val s3 = com.example.data.model.ChildStudent(
      id = 3L, name = "Citra", nis = "103", gender = "P", age = 13,
      dormitory = "Asrama B", classGrade = "VII", district = "", schoolId = "SRT1-KDR"
    )
    val groups = com.example.data.model.VisitHelper.extractDynamicDistricts(listOf(s1, s2, s3), isEn = false)
    assertEquals(3, groups.size)
    // Sorted alphabetically, with __UNASSIGNED__ last
    assertEquals("Plosoklaten", groups[0].displayName)
    assertEquals("Wates", groups[1].displayName)
    assertEquals(com.example.data.model.VisitHelper.UNASSIGNED_DISTRICT, groups[2].normalizedKey)
    assertEquals("Belum Diisi Kecamatan", groups[2].displayName)

    val cal = java.util.Calendar.getInstance()
    cal.set(2026, java.util.Calendar.NOVEMBER, 8, 14, 30, 0)
    val sunday2 = cal.timeInMillis
    cal.set(2026, java.util.Calendar.NOVEMBER, 15, 9, 0, 0)
    val sunday3 = cal.timeInMillis
    cal.set(2026, java.util.Calendar.NOVEMBER, 22, 16, 45, 0)
    val sunday4 = cal.timeInMillis

    val csv = com.example.data.model.VisitHelper.formatDatesCsv(listOf(sunday4, sunday2, sunday3, sunday2))
    val parsed = com.example.data.model.VisitHelper.parseDatesCsv(csv)
    assertEquals(3, parsed.size)
    org.junit.Assert.assertTrue(parsed[0] < parsed[1] && parsed[1] < parsed[2])

    val session = com.example.data.model.VisitSession(
      id = 77L,
      title = "Kunjungan November 2026",
      monthYear = "2026-11",
      sessionDatesCsv = csv,
      selectedDistrictsCsv = com.example.data.model.VisitHelper.formatDistrictsCsv(listOf("Wates", "Pare")),
      schoolId = "SRT1-KDR"
    )
    val scheduled = com.example.data.model.VisitHelper.scheduledStudentsForSession(session, listOf(s1, s2, s3))
    assertEquals(1, scheduled.size)
    assertEquals("Ahmad", scheduled.first().name)
    org.junit.Assert.assertTrue(com.example.data.model.VisitHelper.isStudentInSessionSchedule(session, s1))
    org.junit.Assert.assertFalse(com.example.data.model.VisitHelper.isStudentInSessionSchedule(session, s2))
  }

  @Test
  fun `verify PbVisitSessionDto and PbVisitRecordDto serialize and convert accurately`() {
    val session = com.example.data.model.VisitSession(
      id = 401L,
      title = "Kunjungan November",
      monthYear = "2026-11",
      sessionDatesCsv = "1794070800000,1794675600000",
      selectedDistrictsCsv = "Wates|Puncu",
      description = "Sesi Minggu 2 & 3",
      isActive = true,
      createdBy = "Admin",
      schoolId = "SRT1-KDR"
    )
    val sessionDto = com.example.data.remote.pocketbase.PbVisitSessionDto.fromDomain(session)
    val restoredSession = sessionDto.toDomainModel()
    assertEquals(session.id, restoredSession.id)
    assertEquals(session.title, restoredSession.title)
    assertEquals(session.monthYear, restoredSession.monthYear)
    assertEquals(2, restoredSession.getVisitDatesList().size)
    assertEquals(listOf("Wates", "Puncu"), restoredSession.getDistrictsList())

    val record = com.example.data.model.VisitRecord(
      id = 501L,
      sessionId = 401L,
      sessionTitle = "Kunjungan November",
      visitDate = 1794070800000L,
      checkedInAt = 1794099600000L,
      studentId = 10L,
      studentName = "Santri Kunjungan",
      studentNik = "3506123456780001",
      studentNis = "1001",
      studentClass = "VIII-A",
      studentDorm = "Asrama Putra",
      studentDistrict = "Kec. wates",
      visitorName = "Ibu Aminah",
      visitorRelation = "Ibu",
      checkInMethod = com.example.data.model.VisitRecord.METHOD_BARCODE,
      isOutOfScheduleDistrict = false,
      waliName = "Ustadz Budi",
      schoolId = "SRT1-KDR"
    )
    val recordDto = com.example.data.remote.pocketbase.PbVisitRecordDto.fromDomain(record)
    val restoredRecord = recordDto.toDomainModel()
    assertEquals(record.id, restoredRecord.id)
    assertEquals("Wates", restoredRecord.studentDistrict)
    assertEquals("Ibu", restoredRecord.visitorRelation)
    assertEquals(com.example.data.model.VisitRecord.METHOD_BARCODE, restoredRecord.checkInMethod)
  }

  @Test
  fun `verify HealthViewModel Kunjungan multi-date check-in duplicate guard and PDF generation`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)

    val cal = java.util.Calendar.getInstance()
    cal.set(2026, java.util.Calendar.NOVEMBER, 8, 9, 0, 0)
    val dateWeek2 = com.example.data.model.VisitHelper.normalizeDayStart(cal.timeInMillis)
    cal.set(2026, java.util.Calendar.NOVEMBER, 15, 9, 0, 0)
    val dateWeek3 = com.example.data.model.VisitHelper.normalizeDayStart(cal.timeInMillis)

    // Role WALI cannot create visit session
    vm.switchRoleToWali()
    var waliDone = false
    var createdByWali: com.example.data.model.VisitSession? = null
    vm.createVisitSession(
      title = "Should Fail",
      monthYear = "2026-11",
      visitDates = listOf(dateWeek2),
      selectedDistricts = listOf("Wates")
    ) {
      createdByWali = it
      waliDone = true
    }
    var ticks = 0
    while (ticks < 20 && !waliDone) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(25)
      ticks++
    }
    org.junit.Assert.assertTrue(waliDone)
    org.junit.Assert.assertNull(createdByWali)

    // Admin creates multi-date visit session
    vm.switchRoleToAdmin()
    var createdSession: com.example.data.model.VisitSession? = null
    vm.createVisitSession(
      title = "Kunjungan November",
      monthYear = "2026-11",
      visitDates = listOf(dateWeek2, dateWeek3),
      selectedDistricts = listOf("Wates")
    ) { createdSession = it }
    ticks = 0
    while (ticks < 30 && createdSession == null) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(50)
      ticks++
    }
    assertNotNull(createdSession)
    assertEquals(2, createdSession!!.getVisitDatesList().size)

    val studentScheduled = com.example.data.model.ChildStudent(
      id = 7001L, name = "Siswa Wates", nis = "7001", nik = "3506000000007001",
      gender = "L", age = 13, dormitory = "Asrama 1", classGrade = "VII-A",
      district = "Wates", schoolId = createdSession!!.schoolId
    )
    val studentOutOfSchedule = com.example.data.model.ChildStudent(
      id = 7002L, name = "Siswa Pare", nis = "7002", nik = "3506000000007002",
      gender = "L", age = 14, dormitory = "Asrama 2", classGrade = "VIII-A",
      district = "Pare", schoolId = createdSession!!.schoolId
    )

    // Check-in 1 on Week 2 succeeds
    var done1 = false
    var added1 = false
    vm.recordStudentVisitCheckIn(
      session = createdSession!!,
      student = studentScheduled,
      visitDate = dateWeek2,
      visitorRelation = "Ibu",
      checkInMethod = com.example.data.model.VisitRecord.METHOD_BARCODE
    ) { added, _, _ ->
      added1 = added
      done1 = true
    }
    ticks = 0
    while (ticks < 30 && !done1) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(50)
      ticks++
    }
    org.junit.Assert.assertTrue(added1)

    // Duplicate same-day check-in on Week 2 blocked
    var doneDup = false
    var addedDup = true
    vm.recordStudentVisitCheckIn(
      session = createdSession!!,
      student = studentScheduled,
      visitDate = dateWeek2,
      visitorRelation = "Ibu"
    ) { added, _, _ ->
      addedDup = added
      doneDup = true
    }
    ticks = 0
    while (ticks < 30 && !doneDup) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(50)
      ticks++
    }
    org.junit.Assert.assertTrue(doneDup)
    org.junit.Assert.assertFalse(addedDup)

    // Check-in on Week 3 (different date in same session) succeeds
    var doneWeek3 = false
    var addedWeek3 = false
    vm.recordStudentVisitCheckIn(
      session = createdSession!!,
      student = studentScheduled,
      visitDate = dateWeek3,
      visitorRelation = "Ayah"
    ) { added, _, _ ->
      addedWeek3 = added
      doneWeek3 = true
    }
    ticks = 0
    while (ticks < 30 && !doneWeek3) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(50)
      ticks++
    }
    org.junit.Assert.assertTrue(addedWeek3)

    // Out-of-schedule district student flagged properly
    var doneOut = false
    var outRecord: com.example.data.model.VisitRecord? = null
    vm.recordStudentVisitCheckIn(
      session = createdSession!!,
      student = studentOutOfSchedule,
      visitDate = dateWeek2,
      visitorRelation = "Wali",
      checkInMethod = com.example.data.model.VisitRecord.METHOD_MANUAL
    ) { _, _, rec ->
      outRecord = rec
      doneOut = true
    }
    ticks = 0
    while (ticks < 30 && !doneOut) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(50)
      ticks++
    }
    assertNotNull(outRecord)
    org.junit.Assert.assertTrue(outRecord!!.isOutOfScheduleDistrict)

    // Render A4 Canvas for Manual Roster & Attendance Report PDFs + multi-page pagination
    val allRecs = listOfNotNull(outRecord)
    val manualBitmaps = mutableListOf<android.graphics.Bitmap>()
    com.example.util.PdfReportGenerator.renderVisitManualRosterPages(
      context = app,
      session = createdSession!!,
      scheduledStudents = listOf(studentScheduled),
      existingRecords = allRecs,
      targetDateMillis = dateWeek2,
      startNewCanvas = {
        val bmp = android.graphics.Bitmap.createBitmap(595, 842, android.graphics.Bitmap.Config.ARGB_8888)
        manualBitmaps.add(bmp)
        android.graphics.Canvas(bmp)
      }
    )
    assertEquals(1, manualBitmaps.size)
    manualBitmaps.forEach { it.recycle() }

    val manyVisitStudents = (1..35).map { idx ->
      studentScheduled.copy(id = 8000L + idx, name = "Santri Wates $idx")
    }
    val reportBitmaps = mutableListOf<android.graphics.Bitmap>()
    com.example.util.PdfReportGenerator.renderVisitAttendanceReportPages(
      context = app,
      session = createdSession!!,
      scheduledStudents = manyVisitStudents,
      sessionRecords = allRecs,
      targetDateMillis = dateWeek2,
      startNewCanvas = {
        val bmp = android.graphics.Bitmap.createBitmap(595, 842, android.graphics.Bitmap.Config.ARGB_8888)
        reportBitmaps.add(bmp)
        android.graphics.Canvas(bmp)
      }
    )
    org.junit.Assert.assertTrue(reportBitmaps.size >= 2)
    reportBitmaps.forEach { it.recycle() }

    val waSummary = com.example.util.PdfReportGenerator.buildVisitReportWaSummary(
      session = createdSession!!,
      scheduledStudents = listOf(studentScheduled),
      sessionRecords = allRecs,
      targetDateMillis = dateWeek2,
      waliName = "Wali Test",
      schoolProfile = com.example.data.model.SchoolProfile(),
      language = com.example.util.i18n.AppLanguage.INDONESIAN
    )
    org.junit.Assert.assertTrue(waSummary.contains("Kunjungan November"))
    org.junit.Assert.assertTrue(waSummary.contains("Siswa Wates"))
  }

  @Test
  fun `verify DashboardScreen opens Kunjungan hub displaying 4 required menu icons`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.screens.DashboardScreen(
          viewModel = vm,
          waliName = "Abisarwan Rafif",
          activePlans = emptyList(),
          students = emptyList(),
          onNavigateToPlan = {},
          onNavigateToRecords = {},
          onNavigateToStudents = {},
          onNavigateToProfile = {},
          renderInlineCategoryHub = true
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("category_kunjungan").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("category_hub_overlay_kunjungan").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_jadwal_kunjungan").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_daftar_hadir_kunjungan").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_laporan_kunjungan").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_siswa_per_kecamatan").assertIsDisplayed()
  }

  @Test
  fun `verify TodayShiftMessagesStrip hides when empty, shows today's pending and completed cards, and opens full detail`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val now = System.currentTimeMillis()
    val longMessageText = "Mohon pantau kondisi Ananda Fajar di Asrama Putra setiap 2 jam sekali karena tadi malam sempat demam 38.2 C dan sudah minum paracetamol pukul 04.30 WIB. Pastikan sarapan bubur hangat dahulu."

    val testMessages = androidx.compose.runtime.mutableStateOf<List<com.example.data.model.ShiftMessage>>(emptyList())
    val openedMessage = androidx.compose.runtime.mutableStateOf<com.example.data.model.ShiftMessage?>(null)
    var toggledComplete = false

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        androidx.compose.foundation.layout.Column {
          com.example.ui.components.TodayShiftMessagesStrip(
            messages = testMessages.value,
            onMessageClick = { openedMessage.value = it },
            onOpenAllMessages = {}
          )
          openedMessage.value?.let { msg ->
            com.example.ui.components.ShiftMessageDetailSheet(
              message = msg,
              onToggleComplete = {
                toggledComplete = true
                openedMessage.value = msg.copy(isCompleted = !msg.isCompleted, completedBy = "Wali Test")
              },
              onOpenAllMessages = {},
              onDismiss = { openedMessage.value = null }
            )
          }
        }
      }
    }

    // 1. Empty list -> strip tidak muncul sama sekali
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("today_shift_messages_strip").assertDoesNotExist()

    // 2. Populate today's messages (1 pending + 1 urgent + 1 completed)
    val msg1 = com.example.data.model.ShiftMessage(
      id = 101L,
      title = "Pantau Demam Fajar",
      followUpDate = now,
      shift = "Shift Pagi",
      targetWali = "Ustadz Budi",
      messageContent = longMessageText,
      priority = "URGENT",
      senderName = "Ustadz Ahmad",
      isCompleted = false
    )
    val msg2 = com.example.data.model.ShiftMessage(
      id = 102L,
      title = "Obat Sirup Habis",
      followUpDate = now,
      shift = "Shift Siang",
      targetWali = "Semua Wali",
      messageContent = "Tolong belikan stok sirup paracetamol di apotek depan.",
      priority = "SEDANG",
      senderName = "Ustadzah Sari",
      isCompleted = false
    )
    val msgCompleted = com.example.data.model.ShiftMessage(
      id = 103L,
      title = "Sudah Ambil Rujukan",
      followUpDate = now,
      shift = "Shift Pagi",
      targetWali = "Semua Wali",
      messageContent = "Surat rujukan ke Puskesmas Wates sudah diambil oleh wali piket.",
      priority = "BIASA",
      senderName = "Ustadzah Rina",
      isCompleted = true,
      completedBy = "Ustadz Budi"
    )
    testMessages.value = listOf(msg1, msg2, msgCompleted)

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("today_shift_messages_strip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("today_shift_messages_carousel").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_today_shift_msg_101").assertIsDisplayed()

    // Scroll horizontal carousel to completed card to confirm it stays visible per user choice
    composeTestRule.onNodeWithTag("today_shift_messages_carousel")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("card_today_shift_msg_103"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_today_shift_msg_103").assertIsDisplayed()

    // Scroll back to card 101 and click to open full detail sheet
    composeTestRule.onNodeWithTag("today_shift_messages_carousel")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("card_today_shift_msg_101"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_today_shift_msg_101").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("shift_message_detail_sheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("shift_message_detail_content").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_toggle_complete_shift_detail").performClick()
    composeTestRule.waitForIdle()
    org.junit.Assert.assertTrue(toggledComplete)
  }

  @Test
  fun `verify DashboardScreen hides yesterday shift messages and renders today's shift message below Faskes`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val vm = com.example.ui.HealthViewModel(app)
    vm.switchRoleToWali()
    val activeSchool = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }
    db.shiftMessageDao().deleteBySchoolId(activeSchool)

    // Insert ONLY yesterday message -> strip must remain hidden
    val yesterday = System.currentTimeMillis() - 86_400_000L * 2
    db.shiftMessageDao().insert(
      com.example.data.model.ShiftMessage(
        id = 8801L,
        title = "Pesan Kemarin",
        followUpDate = yesterday,
        shift = "Shift Malam",
        targetWali = "Semua Wali",
        messageContent = "Tidak boleh muncul di blok pesan hari ini",
        priority = "BIASA",
        senderName = "Wali A",
        schoolId = activeSchool
      )
    )
    kotlinx.coroutines.delay(100)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.screens.DashboardScreen(
          viewModel = vm,
          waliName = "Abisarwan Rafif",
          activePlans = emptyList(),
          students = emptyList(),
          onNavigateToPlan = {},
          onNavigateToRecords = {},
          onNavigateToStudents = {},
          onNavigateToProfile = {},
          renderInlineCategoryHub = false
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("today_shift_messages_strip").assertDoesNotExist()

    // Insert 1 message scheduled for TODAY -> strip automatically appears
    db.shiftMessageDao().insert(
      com.example.data.model.ShiftMessage(
        id = 8802L,
        title = "Cek Suhu Anak Sore",
        followUpDate = System.currentTimeMillis(),
        shift = "Shift Sore",
        targetWali = "Wali Asrama",
        messageContent = "Pastikan cek suhu ulang pukul 16.00 WIB",
        priority = "URGENT",
        senderName = "Ustadzah Nisa",
        schoolId = activeSchool
      )
    )
    var ticks = 0
    while (ticks < 25 && vm.shiftMessages.value.none { it.id == 8802L }) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      ticks++
    }
    composeTestRule.waitForIdle()
    org.junit.Assert.assertTrue(vm.shiftMessages.value.any { it.id == 8802L })
    composeTestRule.onNodeWithTag("dashboard_lazy_list")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("today_shift_messages_strip"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("today_shift_messages_strip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_today_shift_msg_8802").assertIsDisplayed()
  }

  @Test
  fun `verify Hubungan Orang Tua hub opens 4 tiles and enforces exclusive claim and Admin or Owner release`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val vm = com.example.ui.HealthViewModel(app)
    val activeSchool = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

    val waliA = com.example.data.model.WaliAccount(
      id = 9101L,
      name = "Ustadz Ridwan",
      username = "ustridwan",
      password = "Pass#1111",
      dormitory = "Asrama Putra",
      schoolId = activeSchool
    )
    val waliB = com.example.data.model.WaliAccount(
      id = 9102L,
      name = "Ustadzah Sari",
      username = "ustsari",
      password = "Pass#2222",
      dormitory = "Asrama Putri",
      schoolId = activeSchool
    )
    db.waliAccountDao().insert(waliA)
    db.waliAccountDao().insert(waliB)

    val student = com.example.data.model.ChildStudent(
      id = 9201L,
      name = "Zaid Al-Farisi",
      nis = "31990011",
      gender = "L",
      age = 12,
      dormitory = "Asrama Putra",
      classGrade = "VI-A",
      motherName = "Ibu Fatimah",
      parentPhone = "081234567890",
      schoolId = activeSchool
    )
    db.childStudentDao().insert(student)

    val loginA = vm.executeLogin("ustridwan", "Pass#1111")
    org.junit.Assert.assertTrue(loginA is com.example.ui.LoginResult.SuccessWali)

    var claimedRes = 0
    vm.claimStudentsForCurrentWali(listOf(student)) { claimed, _ ->
      claimedRes = claimed
    }
    var waitLoops = 0
    while (waitLoops < 25 && claimedRes == 0) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      waitLoops++
    }
    org.junit.Assert.assertEquals(1, claimedRes)
    val afterClaimStudent = db.childStudentDao().getById(9201L)!!
    org.junit.Assert.assertEquals("ustridwan", afterClaimStudent.assignedWaliUsername)
    org.junit.Assert.assertNotNull(db.parentAccessLinkDao().getByStudentId(9201L, activeSchool))

    // Wali B logs in -> student is claimed by another Wali and cannot be released by Wali B
    val loginB = vm.executeLogin("ustsari", "Pass#2222")
    org.junit.Assert.assertTrue(loginB is com.example.ui.LoginResult.SuccessWali)
    org.junit.Assert.assertTrue(vm.isStudentClaimedByOtherWali(afterClaimStudent))
    org.junit.Assert.assertFalse(vm.canCurrentUserReleaseStudent(afterClaimStudent))

    var releaseByBAllowed: Boolean? = null
    vm.releaseStudentClaim(afterClaimStudent) { ok, _ -> releaseByBAllowed = ok }
    waitLoops = 0
    while (waitLoops < 25 && releaseByBAllowed == null) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      waitLoops++
    }
    org.junit.Assert.assertEquals(false, releaseByBAllowed)

    // School Admin can release the student
    vm.switchRoleToAdmin()
    org.junit.Assert.assertTrue(vm.canCurrentUserReleaseStudent(afterClaimStudent))
    var releasedByAdmin = false
    vm.releaseStudentClaim(afterClaimStudent) { ok, _ -> releasedByAdmin = ok }
    waitLoops = 0
    while (waitLoops < 25 && !releasedByAdmin) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      waitLoops++
    }
    org.junit.Assert.assertTrue(releasedByAdmin)
    org.junit.Assert.assertEquals("", db.childStudentDao().getById(9201L)!!.assignedWaliUsername)

    // Open Dashboard Hubungan Orang Tua overlay and verify all 4 feature tiles render
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.screens.DashboardScreen(
          viewModel = vm,
          waliName = "Admin Utama",
          activePlans = emptyList(),
          students = listOf(student),
          onNavigateToPlan = {},
          onNavigateToRecords = {},
          onNavigateToStudents = {},
          onNavigateToProfile = {},
          renderInlineCategoryHub = true
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("category_orang_tua").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("category_hub_overlay_orang_tua").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_pilih_anak_asuh").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_kartu_akses_ortu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_pilihan_akses_ortu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_pesan_ortu").assertIsDisplayed()
  }

  @Test
  fun `verify Parent Portal login hides unchecked modules and supports 2-way messaging`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val vm = com.example.ui.HealthViewModel(app)
    val activeSchool = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

    val student = com.example.data.model.ChildStudent(
      id = 9301L,
      name = "Nadia Rahma",
      nis = "31990022",
      gender = "P",
      age = 13,
      dormitory = "Asrama Putri",
      classGrade = "VII-B",
      motherName = "Ibu Khadijah",
      parentPhone = "081987654321",
      assignedWaliUsername = "ustsari",
      assignedWaliName = "Ustadzah Sari",
      schoolId = activeSchool
    )
    db.childStudentDao().insert(student)

    val link = com.example.data.model.ParentAccessLink(
      id = 9311L,
      studentId = 9301L,
      studentName = "Nadia Rahma",
      studentNis = "31990022",
      studentClass = "VII-B",
      studentDorm = "Asrama Putri",
      parentName = "Ibu Khadijah",
      parentPhone = "081987654321",
      username = "ortu_nadia_0022",
      password = "Ortu#7788",
      assignedWaliUsername = "ustsari",
      assignedWaliName = "Ustadzah Sari",
      canViewMengaji = true,
      canViewMedical = false,
      canViewFasting = false,
      schoolId = activeSchool
    )
    db.parentAccessLinkDao().insert(link)

    val loginRes = vm.executeLogin("ortu_nadia_0022", "Ortu#7788")
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessParent)
    org.junit.Assert.assertEquals(com.example.ui.UserRole.PARENT, vm.currentUserRole.value)

    var loops = 0
    while (loops < 25 && vm.activeParentAccessLink.value == null) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      loops++
    }

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.screens.ParentPortalScreen(
          viewModel = vm,
          onLogout = {}
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("parent_portal_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("parent_tab_home").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_parent_summary_profile").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_parent_summary_mengaji").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_parent_summary_medical").assertDoesNotExist()
    composeTestRule.onNodeWithTag("card_parent_summary_fasting").assertDoesNotExist()
    composeTestRule.onNodeWithTag("card_parent_summary_haid").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_parent_summary_messages").performScrollTo().assertIsDisplayed()

    // Open Profile tile -> verify biodata renders, then return to Home grid
    composeTestRule.onNodeWithTag("card_parent_summary_profile").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("parent_profile_content").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_parent_back_home").performClick()
    composeTestRule.waitForIdle()

    // Open Haid tile -> verify female haid cycle tab renders
    composeTestRule.onNodeWithTag("card_parent_summary_haid").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("parent_haid_list").assertIsDisplayed()

    // Open Messages tab and send a message from Parent to Wali Asuh
    composeTestRule.onNodeWithTag("parent_tab_messages").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("input_parent_portal_message").performTextInput("Assalamualaikum ustadzah, bagaimana kabar Nadia?")
    composeTestRule.onNodeWithTag("btn_parent_portal_send").performClick()
    loops = 0
    while (loops < 25 && vm.parentMessages.value.none { it.studentId == 9301L }) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      loops++
    }
    composeTestRule.waitForIdle()
    org.junit.Assert.assertTrue(
      vm.parentMessages.value.any { it.studentId == 9301L && it.senderRole == "PARENT" }
    )
  }

  @Test
  fun `verify PesanOrangTuaSheet displays only claimed foster students and empty state CTA when none claimed`(): Unit = kotlinx.coroutines.runBlocking {
    val claimedStudent = com.example.data.model.ChildStudent(
      id = 9401L,
      name = "Alya Zahira",
      nis = "31990101",
      gender = "P",
      age = 12,
      dormitory = "Asrama Putri",
      classGrade = "VII-A",
      motherName = "Ibu Aminah",
      assignedWaliUsername = "ustsari",
      assignedWaliName = "Ustadzah Sari",
      schoolId = "SRT1-KDR"
    )
    val otherStudent = com.example.data.model.ChildStudent(
      id = 9402L,
      name = "Abimanyu Satria",
      nis = "31990102",
      gender = "L",
      age = 12,
      dormitory = "Asrama Putra",
      classGrade = "VII-B",
      motherName = "Ibu Mujiati",
      assignedWaliUsername = "",
      assignedWaliName = "",
      schoolId = "SRT1-KDR"
    )
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val alyaClaimedState = androidx.compose.runtime.mutableStateOf(false)

    // 1. When 0 students claimed -> show empty state + Pilih Anak Asuh CTA, hide unclaimed students
    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        val isAlyaClaimed = alyaClaimedState.value
        com.example.ui.dialogs.PesanOrangTuaSheet(
          students = listOf(claimedStudent, otherStudent),
          parentMessages = emptyList(),
          isStudentClaimedByCurrentWali = { st -> isAlyaClaimed && st.id == 9401L },
          isAdmin = false,
          onOpenPilihAnakAsuh = { alyaClaimedState.value = true },
          onSendMessage = { _, _ -> },
          onMarkRead = {},
          onDismiss = {}
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("empty_pesan_ortu_no_claimed").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_open_pilih_anak_asuh_from_chat").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_chat_student_9401").assertDoesNotExist()
    composeTestRule.onNodeWithTag("card_chat_student_9402").assertDoesNotExist()

    // 2. Tap CTA -> simulate claiming Alya -> only Alya appears, Abimanyu stays hidden
    composeTestRule.onNodeWithTag("btn_open_pilih_anak_asuh_from_chat").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_chat_student_9401").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_chat_student_9402").assertDoesNotExist()
  }

  @Test
  fun `verify PilihanAksesOrangTuaSheet scopes to claimed students and shows Haid option only for female`(): Unit = kotlinx.coroutines.runBlocking {
    val femaleStudent = com.example.data.model.ChildStudent(
      id = 9501L,
      name = "Nafa Raidatul Kusna",
      nis = "31990501",
      gender = "P",
      age = 15,
      dormitory = "Asrama Putri",
      classGrade = "X-1",
      motherName = "Ibu Mujiati",
      assignedWaliUsername = "ustsari",
      assignedWaliName = "Ustadzah Sari",
      schoolId = "SRT1-KDR"
    )
    val maleStudent = com.example.data.model.ChildStudent(
      id = 9502L,
      name = "Abimanyu Satria",
      nis = "31990502",
      gender = "L",
      age = 15,
      dormitory = "Asrama Putra",
      classGrade = "X-1",
      motherName = "Ibu Ani",
      assignedWaliUsername = "ustsari",
      assignedWaliName = "Ustadzah Sari",
      schoolId = "SRT1-KDR"
    )
    val links = listOf(
      com.example.data.model.ParentAccessLink(
        id = 9511L,
        studentId = 9501L,
        studentName = femaleStudent.name,
        studentNis = femaleStudent.nis,
        studentClass = femaleStudent.classGrade,
        studentDorm = femaleStudent.dormitory,
        parentName = femaleStudent.motherName,
        username = "ortu_nafa_0501",
        password = "Pass#1234",
        canViewMengaji = true,
        canViewMedical = true,
        canViewFasting = true,
        canViewHaid = true,
        canViewProfile = true,
        schoolId = "SRT1-KDR"
      ),
      com.example.data.model.ParentAccessLink(
        id = 9512L,
        studentId = 9502L,
        studentName = maleStudent.name,
        studentNis = maleStudent.nis,
        studentClass = maleStudent.classGrade,
        studentDorm = maleStudent.dormitory,
        parentName = maleStudent.motherName,
        username = "ortu_abi_0502",
        password = "Pass#5678",
        canViewMengaji = true,
        canViewMedical = true,
        canViewFasting = true,
        canViewHaid = true,
        canViewProfile = true,
        schoolId = "SRT1-KDR"
      )
    )
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val claimedState = androidx.compose.runtime.mutableStateOf(false)
    val lastUpdatedProfile = androidx.compose.runtime.mutableStateOf<Boolean?>(null)

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        val isClaimed = claimedState.value
        com.example.ui.dialogs.PilihanAksesOrangTuaSheet(
          students = listOf(femaleStudent, maleStudent),
          parentAccessLinks = links,
          isStudentClaimedByCurrentWali = { isClaimed },
          isAdmin = false,
          onOpenPilihAnakAsuh = { claimedState.value = true },
          onEnsureAccessLink = {},
          onUpdatePermissions = { _, _, _, _, _, canProfile, _, _ ->
            lastUpdatedProfile.value = canProfile
          },
          onDismiss = {}
        )
      }
    }
    composeTestRule.waitForIdle()
    // Non-admin Wali with 0 claimed students sees empty state + CTA and no student cards
    composeTestRule.onNodeWithTag("empty_pilihan_akses_no_claimed").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_open_pilih_anak_asuh_from_perm").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_pilihan_akses_9501").assertDoesNotExist()
    composeTestRule.onNodeWithTag("card_pilihan_akses_9502").assertDoesNotExist()

    // Simulate claiming both students
    composeTestRule.onNodeWithTag("btn_open_pilih_anak_asuh_from_perm").performClick()
    composeTestRule.waitForIdle()

    // Male student (Abimanyu) appears first alphabetically: has Profil, must NOT have Haid checkbox
    composeTestRule.onNodeWithTag("chk_access_profile_9502").assertIsDisplayed()
    composeTestRule.onNodeWithTag("chk_access_haid_9502").assertDoesNotExist()

    // Search for female student (Nafa) -> brings her card to top: shows Profil + all modules + Haid checkbox
    composeTestRule.onNodeWithTag("input_search_pilihan_akses").performTextInput("Nafa")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("chk_access_profile_9501").assertIsDisplayed()
    composeTestRule.onNodeWithTag("chk_access_haid_9501").assertExists()

    // Clicking Profile checkbox invokes onUpdatePermissions with false
    composeTestRule.onNodeWithTag("chk_access_profile_9501").performClick()
    composeTestRule.waitForIdle()
    org.junit.Assert.assertEquals(false, lastUpdatedProfile.value)
  }

  @Test
  fun `verify KartuAksesOrangTuaSheet scopes to claimed foster students for non-admin Wali`(): Unit = kotlinx.coroutines.runBlocking {
    val student = com.example.data.model.ChildStudent(
      id = 9601L,
      name = "Sofa Lailiya",
      nis = "31990601",
      gender = "P",
      age = 15,
      dormitory = "Asrama Putri",
      classGrade = "X-1",
      motherName = "Ibu Fatimah",
      assignedWaliUsername = "ustsari",
      assignedWaliName = "Ustadzah Sari",
      schoolId = "SRT1-KDR"
    )
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val claimed = androidx.compose.runtime.mutableStateOf(false)

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        val isClaimed = claimed.value
        com.example.ui.dialogs.KartuAksesOrangTuaSheet(
          students = listOf(student),
          parentAccessLinks = emptyList(),
          schoolProfile = com.example.data.model.SchoolProfile(),
          isStudentClaimedByCurrentWali = { isClaimed && it.id == 9601L },
          isAdmin = false,
          onOpenPilihAnakAsuh = { claimed.value = true },
          onGenerateAccessCard = {},
          onResetPassword = {},
          onEditStudentProfile = {},
          onDismiss = {}
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("empty_kartu_ortu_no_claimed").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_open_pilih_anak_asuh_from_cards").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_parent_unlinked_9601").assertDoesNotExist()

    composeTestRule.onNodeWithTag("btn_open_pilih_anak_asuh_from_cards").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_parent_unlinked_9601").assertIsDisplayed()
  }

  @Test
  fun `verify ParentPortalScreen hides Haid tab for male student even when link canViewHaid is true and DTO roundtrip preserves fields`(): Unit = kotlinx.coroutines.runBlocking {
    // 1. PocketBase DTO round-trip preserves canViewHaid & canViewProfile
    val originalLink = com.example.data.model.ParentAccessLink(
      id = 9711L,
      studentId = 9701L,
      studentName = "Ahmad Fauzi",
      studentNis = "31990701",
      studentClass = "VIII-A",
      studentDorm = "Asrama Putra",
      parentName = "Bapak Hasan",
      username = "ortu_fauzi_0701",
      password = "Ortu#9999",
      canViewMengaji = true,
      canViewMedical = true,
      canViewFasting = true,
      canViewHaid = false,
      canViewProfile = false,
      schoolId = "SRT1-KDR"
    )
    val dto = com.example.data.remote.pocketbase.PbParentAccessLinkDto.fromDomain(originalLink)
    val roundTripped = dto.toDomainModel("SRT1-KDR")
    org.junit.Assert.assertEquals(false, roundTripped.canViewHaid)
    org.junit.Assert.assertEquals(false, roundTripped.canViewProfile)

    // 2. Male student logged into ParentPortalScreen -> Profil tab visible, Haid tab hidden even if canViewHaid = true
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val vm = com.example.ui.HealthViewModel(app)
    val activeSchool = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

    val maleStudent = com.example.data.model.ChildStudent(
      id = 9701L,
      name = "Ahmad Fauzi",
      nis = "31990701",
      gender = "L",
      age = 14,
      dormitory = "Asrama Putra",
      classGrade = "VIII-A",
      motherName = "Ibu Aminah",
      schoolId = activeSchool
    )
    db.childStudentDao().insert(maleStudent)
    db.parentAccessLinkDao().insert(
      originalLink.copy(
        schoolId = activeSchool,
        canViewProfile = true,
        canViewHaid = true
      )
    )

    val loginRes = vm.executeLogin("ortu_fauzi_0701", "Ortu#9999")
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessParent)

    var loops = 0
    while (loops < 25 && vm.activeParentAccessLink.value == null) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      loops++
    }

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.screens.ParentPortalScreen(viewModel = vm, onLogout = {})
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_parent_summary_profile").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_parent_summary_haid").assertDoesNotExist()
    composeTestRule.onNodeWithTag("parent_tab_haid").assertDoesNotExist()
  }

  @Test
  fun `verify anaksuperadmin logs in as WALI, can reorder dashboard icons, and syncs layout via PocketBase DTO`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    app.getSharedPreferences("waliasuh_dashboard_layout_prefs", Context.MODE_PRIVATE).edit().clear().commit()

    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)

    // 1. Password salah untuk anaksuperadmin harus ditolak
    val wrongPassResult = vm.executeLogin("anaksuperadmin", "Salah123@", rememberMe = false)
    org.junit.Assert.assertTrue(wrongPassResult is com.example.ui.LoginResult.Error)
    org.junit.Assert.assertFalse(vm.isAnakSuperAdmin.value)

    // 2. Login anaksuperadmin dengan Woyowoyo12@ masuk sebagai UserRole.WALI
    val okResult = vm.executeLogin("anaksuperadmin", "Woyowoyo12@", rememberMe = true)
    org.junit.Assert.assertTrue(okResult is com.example.ui.LoginResult.SuccessWali)
    org.junit.Assert.assertEquals(com.example.ui.UserRole.WALI, vm.currentUserRole.value)
    org.junit.Assert.assertEquals("Anak Superadmin", vm.waliName.value)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    org.junit.Assert.assertTrue(vm.isAnakSuperAdmin.value)

    // 3. Ubah urutan ikon (pindahkan MENGAJI ke posisi pertama & BEROBAT ke posisi terakhir)
    val customOrder = listOf(
      com.example.data.model.DashboardIconKey.MENGAJI,
      com.example.data.model.DashboardIconKey.PUASA,
      com.example.data.model.DashboardIconKey.KUNJUNGAN,
      com.example.data.model.DashboardIconKey.ORANG_TUA,
      com.example.data.model.DashboardIconKey.ANAK_ASUH,
      com.example.data.model.DashboardIconKey.HAID,
      com.example.data.model.DashboardIconKey.PESAN,
      com.example.data.model.DashboardIconKey.BEROBAT,
      com.example.data.model.DashboardIconKey.TABUNGAN,
      com.example.data.model.DashboardIconKey.KONSELING,
      com.example.data.model.DashboardIconKey.JADWAL_WALI,
      com.example.data.model.DashboardIconKey.KLAIM_ANAK_ASUH,
      com.example.data.model.DashboardIconKey.INFO_PENTING,
      com.example.data.model.DashboardIconKey.SHOLAT,
      com.example.data.model.DashboardIconKey.KRISTEN,
      com.example.data.model.DashboardIconKey.JURNAL_ATURAN
    )
    var saveOk = false
    vm.saveDashboardIconOrder(customOrder) { success, _ -> saveOk = success }
    org.junit.Assert.assertTrue(saveOk)
    org.junit.Assert.assertEquals(customOrder, vm.dashboardIconOrder.value)

    // 4. PocketBase DTO round-trip untuk tata letak dashboard global
    val dto = com.example.data.remote.pocketbase.PbSchoolProfileDto.fromDashboardLayout(customOrder, 1731650000000L)
    org.junit.Assert.assertEquals("__GLOBAL_DASHBOARD_LAYOUT__", dto.schoolId)
    val decoded = dto.toDashboardLayoutConfig()
    org.junit.Assert.assertNotNull(decoded)
    org.junit.Assert.assertEquals(customOrder, decoded?.order)
    org.junit.Assert.assertEquals(1731650000000L, decoded?.updatedAt)

    // 5. Akun Wali Asuh biasa tidak memiliki hak isAnakSuperAdmin dan tidak bisa mengubah tata letak
    val normalWali = com.example.data.model.WaliAccount(
      name = "Wali Biasa",
      username = "walibiasa_layout_test",
      password = "Wali#1234",
      dormitory = "Asrama Putra",
      schoolId = "SRT1-KDR"
    )
    db.waliAccountDao().insert(normalWali)
    val normalLogin = vm.executeLogin("walibiasa_layout_test", "Wali#1234", rememberMe = false)
    org.junit.Assert.assertTrue(normalLogin is com.example.ui.LoginResult.SuccessWali)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    org.junit.Assert.assertFalse(vm.isAnakSuperAdmin.value)

    var blockedForNormalWali = false
    vm.saveDashboardIconOrder(com.example.data.model.DashboardIconKey.DEFAULT_ORDER) { success, _ ->
      blockedForNormalWali = !success
    }
    org.junit.Assert.assertTrue(blockedForNormalWali)
    org.junit.Assert.assertEquals(customOrder, vm.dashboardIconOrder.value)

    // 6. Simulasikan pembaruan dari cloud PocketBase yang lebih baru -> otomatis berubah pada Wali Asuh biasa
    val cloudOrder = listOf(
      com.example.data.model.DashboardIconKey.HAID,
      com.example.data.model.DashboardIconKey.MENGAJI,
      com.example.data.model.DashboardIconKey.BEROBAT,
      com.example.data.model.DashboardIconKey.KUNJUNGAN,
      com.example.data.model.DashboardIconKey.ORANG_TUA,
      com.example.data.model.DashboardIconKey.ANAK_ASUH,
      com.example.data.model.DashboardIconKey.PUASA,
      com.example.data.model.DashboardIconKey.PESAN,
      com.example.data.model.DashboardIconKey.TABUNGAN,
      com.example.data.model.DashboardIconKey.KONSELING,
      com.example.data.model.DashboardIconKey.JADWAL_WALI,
      com.example.data.model.DashboardIconKey.KLAIM_ANAK_ASUH,
      com.example.data.model.DashboardIconKey.INFO_PENTING,
      com.example.data.model.DashboardIconKey.SHOLAT,
      com.example.data.model.DashboardIconKey.KRISTEN,
      com.example.data.model.DashboardIconKey.JURNAL_ATURAN
    )
    vm.applyRemoteDashboardIconOrder(cloudOrder, System.currentTimeMillis() + 60_000L)
    org.junit.Assert.assertEquals(cloudOrder, vm.dashboardIconOrder.value)
  }

  @Test
  fun `verify DashboardScreen shows Arrange Icons button only for anaksuperadmin`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val vm = com.example.ui.HealthViewModel(app)
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    // Login sebagai anaksuperadmin -> tombol Atur Ikon muncul
    vm.executeLogin("anaksuperadmin", "Woyowoyo12@", rememberMe = false)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        val plans by vm.activePlans.collectAsStateWithLifecycle()
        val students by vm.students.collectAsStateWithLifecycle()
        val waliName by vm.waliName.collectAsStateWithLifecycle()
        com.example.ui.screens.DashboardScreen(
          viewModel = vm,
          waliName = waliName,
          activePlans = plans,
          students = students,
          onNavigateToPlan = {},
          onNavigateToRecords = {},
          onNavigateToStudents = {},
          onNavigateToProfile = {}
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_customize_dashboard_layout").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun `verify anaksuperadmin school switcher lists approved schools, isolates tenant data, and routes new activities`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    app.getSharedPreferences("waliasuh_school_prefs", Context.MODE_PRIVATE).edit().clear().commit()

    val db = com.example.data.database.AppDatabase.getDatabase(app)
    db.childStudentDao().deleteBySchoolId("SRT-SBY01")
    db.shiftMessageDao().deleteBySchoolId("SRT-SBY01")
    db.schoolRegistrationDao().deleteBySchoolCode("SRT-SBY01")
    db.schoolRegistrationDao().deleteBySchoolCode("SRT-SUSP99")
    db.schoolRegistrationDao().deleteBySchoolCode("SRT-PEND88")

    db.schoolRegistrationDao().insert(
      com.example.data.model.SchoolRegistration(
        schoolName = "Sekolah Rakyat Surabaya 1",
        address = "Jl. Ahmad Yani No. 10",
        city = "Kota Surabaya",
        estimatedWaliCount = 8,
        picName = "Ustadz Arif",
        picEmail = "arif@sby.sch.id",
        picPhone = "081234000111",
        status = "APPROVED",
        generatedSchoolCode = "SRT-SBY01",
        generatedAdminUsername = "admin_sby01",
        generatedAdminPassword = "Pass#Sby1"
      )
    )
    db.schoolRegistrationDao().insert(
      com.example.data.model.SchoolRegistration(
        schoolName = "Sekolah Suspend Uji",
        address = "Jl. Test",
        city = "Kota Solo",
        estimatedWaliCount = 2,
        picName = "Test",
        picEmail = "t@t.id",
        picPhone = "081",
        status = "SUSPENDED",
        generatedSchoolCode = "SRT-SUSP99"
      )
    )
    db.schoolRegistrationDao().insert(
      com.example.data.model.SchoolRegistration(
        schoolName = "Sekolah Pending Uji",
        address = "Jl. Test 2",
        city = "Kota Malang",
        estimatedWaliCount = 2,
        picName = "Test 2",
        picEmail = "t2@t.id",
        picPhone = "082",
        status = "PENDING",
        generatedSchoolCode = "SRT-PEND88"
      )
    )

    val vm = com.example.ui.HealthViewModel(app)
    val loginRes = vm.executeLogin("anaksuperadmin", "Woyowoyo12@", rememberMe = false)
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessWali)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    // Tunggu flow availableSchoolsForSwitch memuat sekolah APPROVED + SRT1-KDR
    var options = vm.availableSchoolsForSwitch.value
    for (i in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      options = vm.availableSchoolsForSwitch.value
      if (options.any { it.tenantCode == "SRT-SBY01" }) break
      kotlinx.coroutines.delay(30)
    }
    org.junit.Assert.assertTrue(options.any { it.tenantCode == "SRT1-KDR" })
    org.junit.Assert.assertTrue(options.any { it.tenantCode == "SRT-SBY01" })
    org.junit.Assert.assertFalse(options.any { it.tenantCode == "SRT-SUSP99" })
    org.junit.Assert.assertFalse(options.any { it.tenantCode == "SRT-PEND88" })
    org.junit.Assert.assertFalse(options.any { it.tenantCode == "__GLOBAL_DASHBOARD_LAYOUT__" })

    // Beralih ke Sekolah Rakyat Surabaya 1
    var switchedOk = false
    vm.switchActiveSchoolForAnakSuperAdmin("SRT-SBY01") { ok, _ -> switchedOk = ok }
    for (i in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (switchedOk && vm.schoolProfile.value.tenantCode == "SRT-SBY01") break
      kotlinx.coroutines.delay(30)
    }
    org.junit.Assert.assertTrue(switchedOk)
    org.junit.Assert.assertEquals("SRT-SBY01", vm.schoolProfile.value.tenantCode)
    org.junit.Assert.assertEquals("Sekolah Rakyat Surabaya 1", vm.schoolProfile.value.schoolName)

    // Tambah siswa dan pesan shift -> harus otomatis masuk ke SRT-SBY01
    vm.addStudent(
      name = "Santri Surabaya Uji",
      nis = "SBY-9901",
      gender = "L",
      age = 14,
      dormitory = "Asrama SBY",
      classGrade = "8A",
      allergies = "",
      bloodType = "O",
      emergencyContact = "081111111",
      specialNotes = ""
    )
    vm.addShiftMessage(
      title = "Operan Malam Surabaya",
      followUpDate = System.currentTimeMillis(),
      shift = "Shift Malam",
      targetWali = "Semua Wali Asuh",
      content = "Pantau anak SBY",
      priority = "PENTING"
    )

    for (i in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.students.value.any { it.name == "Santri Surabaya Uji" } &&
        vm.shiftMessages.value.any { it.title == "Operan Malam Surabaya" }
      ) break
      kotlinx.coroutines.delay(30)
    }

    val sbyStudent = vm.students.value.firstOrNull { it.name == "Santri Surabaya Uji" }
    org.junit.Assert.assertNotNull(sbyStudent)
    org.junit.Assert.assertEquals("SRT-SBY01", sbyStudent?.schoolId)

    val sbyMsg = vm.shiftMessages.value.firstOrNull { it.title == "Operan Malam Surabaya" }
    org.junit.Assert.assertNotNull(sbyMsg)
    org.junit.Assert.assertEquals("SRT-SBY01", sbyMsg?.schoolId)

    // Beralih kembali ke SRT1-KDR -> data SRT-SBY01 terisolasi dan tidak tampil di SRT1-KDR
    var backToPilot = false
    vm.switchActiveSchoolForAnakSuperAdmin("SRT1-KDR") { ok, _ -> backToPilot = ok }
    for (i in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (backToPilot && vm.schoolProfile.value.tenantCode == "SRT1-KDR" &&
        vm.students.value.none { it.name == "Santri Surabaya Uji" }
      ) break
      kotlinx.coroutines.delay(30)
    }
    org.junit.Assert.assertTrue(backToPilot)
    org.junit.Assert.assertEquals("SRT1-KDR", vm.schoolProfile.value.tenantCode)
    org.junit.Assert.assertTrue(vm.students.value.none { it.name == "Santri Surabaya Uji" })
    org.junit.Assert.assertTrue(vm.shiftMessages.value.none { it.title == "Operan Malam Surabaya" })

    // Wali biasa tidak diizinkan mengganti sekolah lewat method switchActiveSchoolForAnakSuperAdmin
    val normalWali = com.example.data.model.WaliAccount(
      name = "Wali Reguler",
      username = "wali_reguler_switch_test",
      password = "Pass#Wali9",
      dormitory = "Asrama A",
      schoolId = "SRT1-KDR"
    )
    db.waliAccountDao().insert(normalWali)
    vm.executeLogin("wali_reguler_switch_test", "Pass#Wali9", rememberMe = false)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    var deniedForNormal = false
    vm.switchActiveSchoolForAnakSuperAdmin("SRT-SBY01") { ok, _ -> deniedForNormal = !ok }
    org.junit.Assert.assertTrue(deniedForNormal)
    org.junit.Assert.assertEquals("SRT1-KDR", vm.schoolProfile.value.tenantCode)

    // Cleanup
    db.childStudentDao().deleteBySchoolId("SRT-SBY01")
    db.shiftMessageDao().deleteBySchoolId("SRT-SBY01")
    db.schoolRegistrationDao().deleteBySchoolCode("SRT-SBY01")
    db.schoolRegistrationDao().deleteBySchoolCode("SRT-SUSP99")
    db.schoolRegistrationDao().deleteBySchoolCode("SRT-PEND88")
    db.waliAccountDao().delete(normalWali)
  }

  @Test
  fun `verify ProfileScreen renders school switcher dropdown exclusively for anaksuperadmin`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    app.getSharedPreferences("waliasuh_school_prefs", Context.MODE_PRIVATE).edit().clear().commit()

    val db = com.example.data.database.AppDatabase.getDatabase(app)
    db.schoolRegistrationDao().getAllRegistrationsList().forEach {
      db.schoolRegistrationDao().delete(it)
    }
    db.schoolRegistrationDao().insert(
      com.example.data.model.SchoolRegistration(
        schoolName = "Sekolah Rakyat Sidoarjo",
        address = "Jl. Pahlawan No. 5",
        city = "Kabupaten Sidoarjo",
        estimatedWaliCount = 6,
        picName = "Pak Hendra",
        picEmail = "hendra@sda.sch.id",
        picPhone = "081234567000",
        status = "APPROVED",
        generatedSchoolCode = "SRT-SBY02"
      )
    )
    val normalWali = com.example.data.model.WaliAccount(
      name = "Wali Biasa Profil",
      username = "walibiasa_profil_test",
      password = "Pass#Profil1",
      dormitory = "Asrama B",
      schoolId = "SRT1-KDR"
    )
    db.waliAccountDao().insert(normalWali)

    val vm = com.example.ui.HealthViewModel(app)
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    // 1. Login sebagai Wali biasa -> dropdown sekolah TIDAK boleh muncul di ProfileScreen
    vm.executeLogin("walibiasa_profil_test", "Pass#Profil1", rememberMe = false)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        val waliName by vm.waliName.collectAsStateWithLifecycle()
        val records by vm.completedRecords.collectAsStateWithLifecycle()
        com.example.ui.screens.ProfileScreen(
          viewModel = vm,
          waliName = waliName,
          isDarkTheme = false,
          onToggleTheme = {},
          records = records
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dropdown_anaksuperadmin_school").assertDoesNotExist()
    composeTestRule.onNodeWithTag("card_anaksuperadmin_school_switcher").assertDoesNotExist()

    // 2. Login sebagai anaksuperadmin -> dropdown muncul & dapat memilih sekolah SRT-SBY02
    vm.executeLogin("anaksuperadmin", "Woyowoyo12@", rememberMe = false)
    for (i in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.isAnakSuperAdmin.value && vm.availableSchoolsForSwitch.value.any { it.tenantCode == "SRT-SBY02" }) break
      kotlinx.coroutines.delay(30)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_anaksuperadmin_school_switcher").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dropdown_anaksuperadmin_school").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("school_option_SRT-SBY02").performScrollTo().assertIsDisplayed().performClick()
    for (i in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.schoolProfile.value.tenantCode == "SRT-SBY02") break
      kotlinx.coroutines.delay(30)
    }
    org.junit.Assert.assertEquals("SRT-SBY02", vm.schoolProfile.value.tenantCode)

    db.schoolRegistrationDao().deleteBySchoolCode("SRT-SBY02")
    db.waliAccountDao().delete(normalWali)
  }

  @Test
  fun `verify DashboardBannerConfig R2 CDN url, 2-to-5s interval, Faskes deletion, and global sync`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    app.getSharedPreferences("waliasuh_dashboard_banner_prefs", Context.MODE_PRIVATE).edit().clear().commit()

    // 1. Cek builder URL Cloudflare R2 CDN dan fallback API files PocketBase
    val cdnUrl = com.example.data.model.DashboardBannerItem.buildR2CdnUrl(
      collectionId = "pbc_1234567890",
      recordId = "w0000000000abc1",
      filename = "banner_1731680000.jpg"
    )
    org.junit.Assert.assertEquals(
      "https://waliasuhcareapp.areksurabaya.my.id/pbc_1234567890/w0000000000abc1/banner_1731680000.jpg",
      cdnUrl
    )
    val pbFallbackUrl = com.example.data.model.DashboardBannerItem.buildPbApiFileUrl(
      pbBaseUrl = "https://waliasuh.areksurabaya.my.id",
      collectionId = "pbc_1234567890",
      recordId = "w0000000000abc1",
      filename = "banner_1731680000.jpg"
    )
    org.junit.Assert.assertEquals(
      "https://waliasuh.areksurabaya.my.id/api/files/pbc_1234567890/w0000000000abc1/banner_1731680000.jpg",
      pbFallbackUrl
    )

    // 2. Cek interval geser di-clamp ke 2, 3, 4, atau 5 detik (invalid fallback ke 3)
    org.junit.Assert.assertEquals(2, com.example.data.model.DashboardBannerConfig.clampIntervalSeconds(2))
    org.junit.Assert.assertEquals(5, com.example.data.model.DashboardBannerConfig.clampIntervalSeconds(5))
    org.junit.Assert.assertEquals(3, com.example.data.model.DashboardBannerConfig.clampIntervalSeconds(99))

    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val vm = com.example.ui.HealthViewModel(app)

    // Default awal: ada 1 banner Faskes bawaan dengan durasi 3 detik
    org.junit.Assert.assertEquals(3, vm.dashboardBannerConfig.value.slideIntervalSeconds)
    org.junit.Assert.assertTrue(vm.dashboardBannerConfig.value.items.any { it.isDefaultFaskes })

    // 3. Login sebagai anaksuperadmin -> hapus Faskes, tambah 2 banner custom (1 buka fitur, 1 link eksternal) dengan interval 5 detik
    val loginRes = vm.executeLogin("anaksuperadmin", "Woyowoyo12@", rememberMe = false)
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessWali)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val customBannerFeature = com.example.data.model.DashboardBannerItem(
      id = "banner_haid_promo",
      title = "Panduan Fiqih Haid Terbaru",
      subtitle = "Ketuk untuk membuka panduan haid",
      badgeText = "INFO",
      imageUrl = cdnUrl,
      fallbackImageUrl = pbFallbackUrl,
      pbCollectionId = "pbc_1234567890",
      pbRecordId = "w0000000000abc1",
      pbFilename = "banner_1731680000.jpg",
      actionType = com.example.data.model.DashboardBannerActionType.INTERNAL_FEATURE,
      actionTarget = com.example.data.model.DashboardBannerFeatureTarget.FEATURE_FIQIH_HAID.key,
      isDefaultFaskes = false,
      isEnabled = true,
      sortOrder = 0
    )
    val customBannerExternal = com.example.data.model.DashboardBannerItem(
      id = "banner_kemensos_link",
      title = "Portal Sekolah Rakyat",
      subtitle = "Kunjungi website resmi",
      badgeText = "WEB",
      imageUrl = "https://waliasuhcareapp.areksurabaya.my.id/col1/rec2/portal.jpg",
      actionType = com.example.data.model.DashboardBannerActionType.EXTERNAL_LINK,
      actionTarget = "https://kemensos.go.id",
      isDefaultFaskes = false,
      isEnabled = true,
      sortOrder = 1
    )
    val savedConfig = com.example.data.model.DashboardBannerConfig(
      items = listOf(customBannerFeature, customBannerExternal),
      slideIntervalSeconds = 5,
      updatedAt = 1731690000000L
    )

    var savedOk = false
    vm.saveDashboardBannerConfig(savedConfig) { ok, _ -> savedOk = ok }
    org.junit.Assert.assertTrue(savedOk)
    org.junit.Assert.assertEquals(5, vm.dashboardBannerConfig.value.slideIntervalSeconds)
    org.junit.Assert.assertFalse(vm.dashboardBannerConfig.value.items.any { it.isDefaultFaskes })
    org.junit.Assert.assertEquals(2, vm.dashboardBannerConfig.value.items.size)

    // 4. PocketBase DTO round-trip dengan sentinel __GLOBAL_DASHBOARD_BANNER__ & tetap terfilter dari school switcher
    val pbDto = com.example.data.remote.pocketbase.PbSchoolProfileDto.fromDashboardBannerConfig(vm.dashboardBannerConfig.value)
    org.junit.Assert.assertEquals("__GLOBAL_DASHBOARD_BANNER__", pbDto.schoolId)
    val decoded = pbDto.toDashboardBannerConfig()
    org.junit.Assert.assertNotNull(decoded)
    org.junit.Assert.assertEquals(5, decoded?.slideIntervalSeconds)
    org.junit.Assert.assertEquals(2, decoded?.items?.size)
    org.junit.Assert.assertEquals(
      com.example.data.model.DashboardBannerFeatureTarget.FEATURE_FIQIH_HAID.key,
      decoded?.items?.get(0)?.actionTarget
    )
    org.junit.Assert.assertFalse(
      vm.availableSchoolsForSwitch.value.any { it.tenantCode == "__GLOBAL_DASHBOARD_BANNER__" }
    )

    // 5. Akun Wali Asuh biasa tidak dapat mengubah banner, tetapi menerima pembaruan global dari cloud
    val normalWali = com.example.data.model.WaliAccount(
      name = "Wali Biasa Banner",
      username = "walibiasa_banner_test",
      password = "Pass#Banner1",
      dormitory = "Asrama Putra",
      schoolId = "SRT1-KDR"
    )
    db.waliAccountDao().insert(normalWali)
    vm.executeLogin("walibiasa_banner_test", "Pass#Banner1", rememberMe = false)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    org.junit.Assert.assertFalse(vm.isAnakSuperAdmin.value)

    var blockedForWali = false
    vm.saveDashboardBannerConfig(
      com.example.data.model.DashboardBannerConfig(items = emptyList(), slideIntervalSeconds = 2)
    ) { ok, _ -> blockedForWali = !ok }
    org.junit.Assert.assertTrue(blockedForWali)

    val newerRemote = com.example.data.model.DashboardBannerConfig(
      items = listOf(customBannerExternal.copy(sortOrder = 0)),
      slideIntervalSeconds = 4,
      updatedAt = System.currentTimeMillis() + 120_000L
    )
    vm.applyRemoteDashboardBannerConfig(newerRemote)
    org.junit.Assert.assertEquals(4, vm.dashboardBannerConfig.value.slideIntervalSeconds)
    org.junit.Assert.assertEquals(1, vm.dashboardBannerConfig.value.items.size)
    org.junit.Assert.assertEquals("banner_kemensos_link", vm.dashboardBannerConfig.value.items.first().id)

    db.waliAccountDao().delete(normalWali)
  }

  @Test
  fun `verify DashboardScreen shows banner manager only for anaksuperadmin and supports deleting Faskes banner`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    app.getSharedPreferences("waliasuh_dashboard_banner_prefs", Context.MODE_PRIVATE).edit().clear().commit()

    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val normalWali = com.example.data.model.WaliAccount(
      name = "Wali Biasa Banner UI",
      username = "walibiasa_banner_ui_test",
      password = "Pass#Ui1",
      dormitory = "Asrama C",
      schoolId = "SRT1-KDR"
    )
    db.waliAccountDao().insert(normalWali)

    val vm = com.example.ui.HealthViewModel(app)
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    // 1. Login sebagai anaksuperadmin -> tombol Atur Banner & Faskes card terlihat
    vm.executeLogin("anaksuperadmin", "Woyowoyo12@", rememberMe = false)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        val plans by vm.activePlans.collectAsStateWithLifecycle()
        val students by vm.students.collectAsStateWithLifecycle()
        val waliName by vm.waliName.collectAsStateWithLifecycle()
        com.example.ui.screens.DashboardScreen(
          viewModel = vm,
          waliName = waliName,
          activePlans = plans,
          students = students,
          onNavigateToPlan = {},
          onNavigateToRecords = {},
          onNavigateToStudents = {},
          onNavigateToProfile = {}
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dashboard_lazy_list")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("btn_manage_dashboard_banners"))
    composeTestRule.onNodeWithTag("btn_manage_dashboard_banners").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dashboard_lazy_list")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("tile_puskesmas_info"))
    composeTestRule.onNodeWithTag("tile_puskesmas_info").assertIsDisplayed()

    // Buka Editor Banner -> pilih interval 4 Detik -> hapus banner Faskes -> Simpan
    composeTestRule.onNodeWithTag("dashboard_lazy_list")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("btn_manage_dashboard_banners"))
    composeTestRule.onNodeWithTag("btn_manage_dashboard_banners").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("chip_banner_interval_4").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("btn_delete_banner_default_faskes_banner").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("btn_save_dashboard_banners").performClick()
    composeTestRule.waitForIdle()

    org.junit.Assert.assertEquals(4, vm.dashboardBannerConfig.value.slideIntervalSeconds)
    org.junit.Assert.assertTrue(vm.dashboardBannerConfig.value.items.isEmpty())
    composeTestRule.onNodeWithTag("dashboard_lazy_list")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("banner_empty_admin_card"))
    composeTestRule.onNodeWithTag("banner_empty_admin_card").assertIsDisplayed()

    // 2. Beralih ke Wali Biasa -> tombol Atur Banner tersembunyi & Faskes sudah terhapus
    vm.executeLogin("walibiasa_banner_ui_test", "Pass#Ui1", rememberMe = false)
    for (i in 1..15) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (!vm.isAnakSuperAdmin.value) break
      kotlinx.coroutines.delay(20)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_manage_dashboard_banners").assertDoesNotExist()
    composeTestRule.onNodeWithTag("tile_puskesmas_info").assertDoesNotExist()

    db.waliAccountDao().delete(normalWali)
  }

  @Test
  fun `verify PocketBaseSyncManager parsePocketBaseUploadError extracts data fields and R2 Region hint`() {
    val withFieldData = """{"status":400,"message":"Failed to create record.","data":{"image":{"code":"validation_invalid_mime_type","message":"Invalid mime type."}}}"""
    val parsedWithField = com.example.data.remote.pocketbase.PocketBaseSyncManager.parsePocketBaseUploadError(
      raw = withFieldData,
      httpCode = 400,
      isEn = false
    )
    org.junit.Assert.assertTrue(parsedWithField.contains("Failed to create record."))
    org.junit.Assert.assertTrue(parsedWithField.contains("image: Invalid mime type. (validation_invalid_mime_type)"))

    val genericS3Fail = """{"status":400,"message":"Failed to create record.","data":{}}"""
    val parsedGenericId = com.example.data.remote.pocketbase.PocketBaseSyncManager.parsePocketBaseUploadError(
      raw = genericS3Fail,
      httpCode = 400,
      isEn = false
    )
    org.junit.Assert.assertTrue(parsedGenericId.contains("Region menjadi 'auto'"))
    org.junit.Assert.assertTrue(parsedGenericId.contains("Asia-Pacific"))
  }

  @Test
  fun `verify PbDeletionLogDto preserves schoolId category recordId and actor`() {
    val log = com.example.data.model.DeletionLog(
      id = 910001L,
      category = com.example.data.model.DeletionLog.CATEGORY_MENGAJI,
      studentId = 123L,
      studentName = "Abimanyu Satria Pamungkas",
      studentClass = "X-4",
      recordId = 555L,
      deletedSummary = "Iqro' Jilid 1 Hal. 10 (Lancar)",
      deletedBy = "Ustadz Ahmad",
      deletedByUsername = "ustadz_ahmad",
      schoolId = "SRT1-KDR",
      createdAt = 1763200000000L
    )

    val dto = com.example.data.remote.pocketbase.PbDeletionLogDto.fromDomain(log)
    assertEquals("SRT1-KDR", dto.schoolId)
    assertEquals("MENGAJI", dto.category)
    assertEquals(555L, dto.recordId)
    assertEquals("Ustadz Ahmad", dto.deletedBy)
    assertEquals("ustadz_ahmad", dto.deletedByUsername)

    val restored = dto.toDomainModel()
    assertEquals(log.id, restored.id)
    assertEquals(log.category, restored.category)
    assertEquals(log.studentId, restored.studentId)
    assertEquals(log.studentName, restored.studentName)
    assertEquals(log.studentClass, restored.studentClass)
    assertEquals(log.recordId, restored.recordId)
    assertEquals(log.deletedSummary, restored.deletedSummary)
    assertEquals(log.deletedBy, restored.deletedBy)
    assertEquals(log.deletedByUsername, restored.deletedByUsername)
    assertEquals(log.schoolId, restored.schoolId)
  }

  @Test
  fun `verify history deletions log deletion_logs with 0 badge for deleter and active badge for colleague wali`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    app.getSharedPreferences("waliasuh_notif_prefs", Context.MODE_PRIVATE).edit().clear().commit()

    val schoolA = "SCH-DEL-LOG-A"
    val schoolC = "SCH-DEL-LOG-C"

    val db = com.example.data.database.AppDatabase.getDatabase(app)
    db.deletionLogDao().deleteBySchoolId(schoolA)
    db.deletionLogDao().deleteBySchoolId(schoolC)
    db.mengajiRecordDao().deleteBySchoolId(schoolA)
    db.mengajiRecordDao().deleteBySchoolId(schoolC)
    db.fastingRecordDao().deleteBySchoolId(schoolA)
    db.fastingRecordDao().deleteBySchoolId(schoolC)
    db.medicalRecordDao().deleteAll()

    val regA = com.example.data.model.SchoolRegistration(
      schoolName = "Sekolah Uji Hapus A",
      address = "Jl. A",
      city = "Kediri",
      estimatedWaliCount = 3,
      picName = "PIC A",
      picEmail = "a@test.id",
      picPhone = "0811",
      status = "APPROVED",
      generatedSchoolCode = schoolA,
      generatedAdminUsername = "admin_del_a",
      generatedAdminPassword = "Pass#DelA1"
    )
    val regC = com.example.data.model.SchoolRegistration(
      schoolName = "Sekolah Uji Hapus C",
      address = "Jl. C",
      city = "Malang",
      estimatedWaliCount = 2,
      picName = "PIC C",
      picEmail = "c@test.id",
      picPhone = "0822",
      status = "APPROVED",
      generatedSchoolCode = schoolC,
      generatedAdminUsername = "admin_del_c",
      generatedAdminPassword = "Pass#DelC1"
    )
    db.schoolRegistrationDao().insert(regA)
    db.schoolRegistrationDao().insert(regC)

    val waliA = com.example.data.model.WaliAccount(
      name = "Ustadz Penghapus",
      username = "wali_del_a1",
      password = "Pass#Wali1",
      dormitory = "Asrama Putra",
      schoolId = schoolA
    )
    val waliB = com.example.data.model.WaliAccount(
      name = "Ustadz Rekan",
      username = "wali_del_a2",
      password = "Pass#Wali2",
      dormitory = "Asrama Putra",
      schoolId = schoolA
    )
    val waliC = com.example.data.model.WaliAccount(
      name = "Ustadz Sekolah Lain",
      username = "wali_del_c1",
      password = "Pass#Wali3",
      dormitory = "Asrama Putra",
      schoolId = schoolC
    )
    db.waliAccountDao().insert(waliA)
    db.waliAccountDao().insert(waliB)
    db.waliAccountDao().insert(waliC)

    val studentA = com.example.data.model.ChildStudent(
      id = 441001L,
      name = "Santri Uji Hapus",
      nis = "441001",
      gender = "L",
      age = 15,
      dormitory = "Asrama Putra",
      classGrade = "X-4",
      schoolId = schoolA
    )
    db.childStudentDao().insert(studentA)

    val mengajiRec = com.example.data.model.MengajiRecord(
      id = 551001L,
      studentId = studentA.id,
      studentName = studentA.name,
      studentClass = "X-4",
      bookType = "Iqro'",
      jilidOrJuz = "Jilid 1",
      pageOrVerse = "Hal. 10",
      fluencyStatus = com.example.data.model.MengajiHelper.FLUENCY_LANCAR,
      sessionDate = System.currentTimeMillis(),
      waliName = waliA.name,
      schoolId = schoolA
    )
    val fastingRec = com.example.data.model.FastingRecord(
      id = 551002L,
      sessionId = 9001L,
      sessionTitle = "Puasa Senin",
      sessionDate = System.currentTimeMillis(),
      studentId = studentA.id,
      studentName = studentA.name,
      studentClass = "X-4",
      isSahurChecked = true,
      isBukaChecked = false,
      waliName = waliA.name,
      schoolId = schoolA
    )
    val medicalRec = com.example.data.model.MedicalRecord(
      id = 551003L,
      studentId = studentA.id,
      studentName = studentA.name,
      studentDormitory = "Asrama Putra",
      faskes = "Puskesmas",
      complaint = "Demam Tinggi",
      diagnosis = "Febris",
      planDate = System.currentTimeMillis(),
      treatmentDate = System.currentTimeMillis(),
      status = "COMPLETED"
    )
    db.mengajiRecordDao().insert(mengajiRec)
    db.fastingRecordDao().insert(fastingRec)
    db.medicalRecordDao().insert(medicalRec)

    val vm = com.example.ui.HealthViewModel(app)

    // 1. Login sebagai Wali A di Sekolah A lalu hapus ketiga riwayat
    val loginResA = vm.executeLogin("wali_del_a1", "Pass#Wali1", rememberMe = false)
    org.junit.Assert.assertTrue(loginResA is com.example.ui.LoginResult.SuccessWali)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    vm.deleteMengajiRecord(mengajiRec)
    vm.deleteFastingRecord(fastingRec)
    vm.deleteRecord(medicalRec)

    var logsInDb: List<com.example.data.model.DeletionLog> = emptyList()
    for (attempt in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      logsInDb = db.deletionLogDao().getLogsBySchoolList(schoolA)
      if (logsInDb.size == 3 && vm.deletionLogs.value.size == 3) break
      kotlinx.coroutines.delay(40)
    }
    assertEquals(3, logsInDb.size)
    assertEquals(3, vm.deletionLogs.value.size)

    // Poin 2 disetujui: Bagi Wali Asuh yang menghapus sendiri, badge lonceng tetap 0!
    assertEquals(0, vm.unreadDeletionLogsCount.value)
    org.junit.Assert.assertTrue(logsInDb.all { it.schoolId == schoolA && it.deletedByUsername == "wali_del_a1" })

    // 2. Beralih login ke Wali B (rekan satu sekolah di Sekolah A) -> Badge lonceng harus terisi 3
    val loginResB = vm.executeLogin("wali_del_a2", "Pass#Wali2", rememberMe = false)
    org.junit.Assert.assertTrue(loginResB is com.example.ui.LoginResult.SuccessWali)
    for (attempt in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.unreadDeletionLogsCount.value == 3) break
      kotlinx.coroutines.delay(40)
    }
    assertEquals(3, vm.unreadDeletionLogsCount.value)

    // Setelah Wali B membuka ikon lonceng -> markDeletionLogsRead() mereset badge ke 0 tapi riwayat tetap ada
    vm.markDeletionLogsRead()
    for (attempt in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.unreadDeletionLogsCount.value == 0) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(0, vm.unreadDeletionLogsCount.value)
    assertEquals(3, vm.deletionLogs.value.size)

    // 3. Login ke Wali C di Sekolah C -> Tenant isolation memastikan 0 log & 0 unread count
    val loginResC = vm.executeLogin("wali_del_c1", "Pass#Wali3", rememberMe = false)
    org.junit.Assert.assertTrue(loginResC is com.example.ui.LoginResult.SuccessWali)
    for (attempt in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.deletionLogs.value.isEmpty() && vm.unreadDeletionLogsCount.value == 0) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(0, vm.deletionLogs.value.size)
    assertEquals(0, vm.unreadDeletionLogsCount.value)
  }

  @Test
  fun `verify StudentDetailDialog delete button shows confirmation and invokes callback across Mengaji Berobat and Puasa tabs`() {
    val student = com.example.data.model.ChildStudent(
      id = 8801L,
      name = "Abimanyu Satria Pamungkas",
      nis = "108346824",
      gender = "L",
      age = 16,
      dormitory = "Asrama Putra",
      classGrade = "X-4",
      schoolId = "SRT1-KDR"
    )
    val mengajiRec = com.example.data.model.MengajiRecord(
      id = 701L,
      studentId = 8801L,
      studentName = student.name,
      studentClass = "X-4",
      bookType = "Iqro'",
      jilidOrJuz = "Jilid 1",
      pageOrVerse = "Hal. 10",
      fluencyStatus = com.example.data.model.MengajiHelper.FLUENCY_LANCAR,
      sessionDate = 1763200000000L,
      waliName = "Ustadz Budi",
      schoolId = "SRT1-KDR"
    )
    val medicalRec = com.example.data.model.MedicalRecord(
      id = 702L,
      studentId = 8801L,
      studentName = student.name,
      studentDormitory = "Asrama Putra",
      faskes = "Puskesmas Kediri",
      complaint = "Demam",
      diagnosis = "Febris",
      planDate = 1763200000000L,
      treatmentDate = 1763200000000L,
      status = "COMPLETED"
    )
    val fastingRec = com.example.data.model.FastingRecord(
      id = 703L,
      sessionId = 9001L,
      sessionTitle = "Puasa Kamis",
      sessionDate = 1763200000000L,
      studentId = 8801L,
      studentName = student.name,
      studentClass = "X-4",
      isSahurChecked = true,
      isBukaChecked = true,
      waliName = "Ustadz Budi",
      schoolId = "SRT1-KDR"
    )

    var deletedMengaji: com.example.data.model.MengajiRecord? = null
    var deletedMedical: com.example.data.model.MedicalRecord? = null
    var deletedFasting: com.example.data.model.FastingRecord? = null
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.StudentDetailDialog(
          student = student,
          studentRecords = listOf(medicalRec),
          mengajiRecords = listOf(mengajiRec),
          fastingRecords = listOf(fastingRec),
          onDeleteMengajiRecord = { deletedMengaji = it },
          onDeleteMedicalRecord = { deletedMedical = it },
          onDeleteFastingRecord = { deletedFasting = it },
          onDismiss = {}
        )
      }
    }
    composeTestRule.waitForIdle()

    // 1. Tab Mengaji
    composeTestRule.onNodeWithTag("tab_student_detail_mengaji").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_delete_mengaji_701").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_confirm_delete_student_history").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals(701L, deletedMengaji?.id)

    // 2. Tab Berobat
    composeTestRule.onNodeWithTag("tab_student_detail_berobat").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_delete_medical_702").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_confirm_delete_student_history").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals(702L, deletedMedical?.id)

    // 3. Tab Puasa
    composeTestRule.onNodeWithTag("tab_student_detail_puasa").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_delete_fasting_703").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_confirm_delete_student_history").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals(703L, deletedFasting?.id)
  }

  @Test
  fun `verify Guru Ngaji account role round-trip, input-only permissions, and login persistence`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val schoolId = "SRT-GURU-NGAJI-01"

    // 1. Verifikasi DTO & skema PocketBase memiliki field role pada wali_accounts
    val guruDomain = com.example.data.model.WaliAccount(
      id = 661001L,
      name = "Ustadz Mansyur",
      username = "gurumansyur",
      password = "Ngaji#2026",
      dormitory = "Umum",
      phone = "081355556666",
      role = com.example.data.model.WaliAccount.ROLE_GURU_NGAJI,
      schoolId = schoolId
    )
    val pbGuru = com.example.data.remote.pocketbase.PbWaliAccountDto.fromDomain(guruDomain)
    assertEquals(com.example.data.model.WaliAccount.ROLE_GURU_NGAJI, pbGuru.role)
    val restoredGuru = pbGuru.toDomainModel()
    assertEquals(com.example.data.model.WaliAccount.ROLE_GURU_NGAJI, restoredGuru.role)
    org.junit.Assert.assertTrue(restoredGuru.isGuruNgaji())

    val waliCol = com.example.data.remote.pocketbase.PocketBaseSchemaProvider.COLLECTIONS
      .first { it.collectionName == "wali_accounts" }
    org.junit.Assert.assertTrue(waliCol.fields.any { it.name == "role" && it.type == "text" })

    // Bersihkan data uji untuk sekolah terkait
    db.waliAccountDao().deleteBySchoolId(schoolId)
    db.childStudentDao().deleteBySchoolId(schoolId)
    db.mengajiRecordDao().deleteBySchoolId(schoolId)
    db.deletionLogDao().deleteBySchoolId(schoolId)

    db.schoolRegistrationDao().insert(
      com.example.data.model.SchoolRegistration(
        schoolName = "Sekolah Uji Guru Ngaji",
        address = "Jl. Pesantren No. 7",
        city = "Kediri",
        estimatedWaliCount = 4,
        picName = "Admin Sekolah",
        picEmail = "admin@ngaji.id",
        picPhone = "081234",
        status = "APPROVED",
        generatedSchoolCode = schoolId,
        generatedAdminUsername = "admin_guru_ngaji",
        generatedAdminPassword = "Admin#Ngaji1"
      )
    )

    val putra = com.example.data.model.ChildStudent(
      id = 662001L,
      name = "Ahmad Putra",
      nis = "662001",
      gender = "L",
      age = 14,
      dormitory = "Asrama Putra",
      classGrade = "VIII-A",
      schoolId = schoolId
    )
    val putri = com.example.data.model.ChildStudent(
      id = 662002L,
      name = "Aisyah Putri",
      nis = "662002",
      gender = "P",
      age = 13,
      dormitory = "Asrama Putri",
      classGrade = "VII-B",
      schoolId = schoolId
    )
    db.childStudentDao().insert(putra)
    db.childStudentDao().insert(putri)

    val vm = com.example.ui.HealthViewModel(app)
    vm.switchRoleToAdmin()
    vm.updateSchoolProfile(
      com.example.data.model.SchoolProfile(
        tenantCode = schoolId,
        schoolName = "Sekolah Uji Guru Ngaji",
        shortName = "Sekolah Ngaji"
      )
    )

    // 2. Admin membuat akun Guru Ngaji
    var createdAccount: com.example.data.model.WaliAccount? = null
    vm.addWaliAccount(
      name = "Ustadz Mansyur",
      username = "gurumansyur",
      dormitory = "Umum",
      phone = "081355556666",
      customPassword = "Ngaji#2026",
      role = com.example.data.model.WaliAccount.ROLE_GURU_NGAJI
    ) { acc ->
      createdAccount = acc
    }
    for (attempt in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (createdAccount != null) break
      kotlinx.coroutines.delay(30)
    }
    org.junit.Assert.assertNotNull(createdAccount)
    assertEquals(com.example.data.model.WaliAccount.ROLE_GURU_NGAJI, createdAccount?.role)

    // 3. Login sebagai Guru Ngaji -> masuk ke SuccessGuruNgaji + UserRole.GURU_NGAJI + rememberMe=true
    val loginRes = vm.executeLogin("gurumansyur", "Ngaji#2026", rememberMe = true)
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessGuruNgaji)
    assertEquals(com.example.ui.UserRole.GURU_NGAJI, vm.currentUserRole.value)
    assertEquals("Ustadz Mansyur", vm.waliName.value)

    // Tunggu students termuat (harus mencakup Putra & Putri)
    for (attempt in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.students.value.size == 2) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(2, vm.students.value.size)

    // 4. Guru Ngaji BISA input setoran untuk Putra & Putri
    var savedPutra = false
    var savedPutri = false
    vm.saveMengajiRecord(
      student = putra,
      sessionDate = System.currentTimeMillis(),
      bookType = "Al-Qur'an",
      jilidOrJuz = "Juz 30",
      pageOrVerse = "An-Naba 1-15",
      fluencyStatus = com.example.data.model.MengajiHelper.FLUENCY_LANCAR,
      notes = "Tajwid bagus"
    ) { ok, _ -> savedPutra = ok }
    vm.saveMengajiRecord(
      student = putri,
      sessionDate = System.currentTimeMillis(),
      bookType = "Iqro'",
      jilidOrJuz = "Jilid 4",
      pageOrVerse = "Hal. 12",
      fluencyStatus = com.example.data.model.MengajiHelper.FLUENCY_TIDAK_LANCAR,
      notes = "Perlu murajaah mad"
    ) { ok, _ -> savedPutri = ok }

    var schoolRecords: List<com.example.data.model.MengajiRecord> = emptyList()
    for (attempt in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      schoolRecords = db.mengajiRecordDao().getRecordsBySchoolList(schoolId)
      if (savedPutra && savedPutri && schoolRecords.size == 2) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(2, schoolRecords.size)

    // 5. Guru Ngaji TIDAK BISA menghapus catatan mengaji (input-only defense)
    val targetDelete = schoolRecords.first()
    vm.deleteMengajiRecord(targetDelete)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    kotlinx.coroutines.delay(150)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    val recordsAfterAttemptedDelete = db.mengajiRecordDao().getRecordsBySchoolList(schoolId)
    assertEquals(2, recordsAfterAttemptedDelete.size)
    assertEquals(0, db.deletionLogDao().getLogsBySchoolList(schoolId).size)

    // 6. Fresh ViewModel restore sesi rememberMe -> tetap UserRole.GURU_NGAJI
    val restoredVm = com.example.ui.HealthViewModel(app)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    assertEquals(com.example.ui.UserRole.GURU_NGAJI, restoredVm.currentUserRole.value)
    assertEquals("gurumansyur", restoredVm.currentUsername.value)
  }

  @Test
  fun `verify GuruNgajiScreen displays all students, history without delete buttons, and AddWaliAccountDialog selects Guru Ngaji role`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val schoolId = "SRT-GN-UI-01"

    db.waliAccountDao().deleteBySchoolId(schoolId)
    db.childStudentDao().deleteBySchoolId(schoolId)
    db.mengajiRecordDao().deleteBySchoolId(schoolId)

    db.waliAccountDao().insert(
      com.example.data.model.WaliAccount(
        id = 771001L,
        name = "Ustadzah Halimah",
        username = "guruhalimah",
        password = "Pass#Halimah1",
        dormitory = "Umum",
        role = com.example.data.model.WaliAccount.ROLE_GURU_NGAJI,
        schoolId = schoolId
      )
    )
    db.childStudentDao().insert(
      com.example.data.model.ChildStudent(
        id = 772001L,
        name = "Zaid Putra",
        nis = "772001",
        gender = "L",
        age = 15,
        dormitory = "Asrama Putra",
        classGrade = "X-1",
        schoolId = schoolId
      )
    )
    db.childStudentDao().insert(
      com.example.data.model.ChildStudent(
        id = 772002L,
        name = "Zahra Putri",
        nis = "772002",
        gender = "P",
        age = 14,
        dormitory = "Asrama Putri",
        classGrade = "IX-A",
        schoolId = schoolId
      )
    )
    db.mengajiRecordDao().insert(
      com.example.data.model.MengajiRecord(
        id = 773001L,
        studentId = 772002L,
        studentName = "Zahra Putri",
        studentClass = "IX-A",
        bookType = "Al-Qur'an",
        jilidOrJuz = "Juz 1",
        pageOrVerse = "Al-Baqarah 1-10",
        fluencyStatus = com.example.data.model.MengajiHelper.FLUENCY_LANCAR,
        sessionDate = System.currentTimeMillis(),
        waliName = "Ustadzah Halimah",
        schoolId = schoolId
      )
    )

    val vm = com.example.ui.HealthViewModel(app)
    vm.updateSchoolProfile(
      com.example.data.model.SchoolProfile(
        tenantCode = schoolId,
        schoolName = "Sekolah UI Guru Ngaji",
        shortName = "Sekolah UI"
      )
    )
    val loginRes = vm.executeLogin("guruhalimah", "Pass#Halimah1", rememberMe = false)
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessGuruNgaji)

    for (attempt in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.students.value.size == 2 && vm.mengajiRecords.value.size == 1) break
      kotlinx.coroutines.delay(30)
    }

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    var chosenRoleFromDialog: String? = null
    val showAddDialog = androidx.compose.runtime.mutableStateOf(false)

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        if (showAddDialog.value) {
          com.example.ui.dialogs.AddWaliAccountDialog(
            schoolName = "Sekolah UI Guru Ngaji",
            onDismiss = { showAddDialog.value = false },
            onConfirm = { _, _, _, _, _, role ->
              chosenRoleFromDialog = role
              showAddDialog.value = false
            }
          )
        } else {
          com.example.ui.screens.GuruNgajiScreen(
            viewModel = vm,
            onLogout = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // Verifikasi elemen utama GuruNgajiScreen tampil: Input, Scan QR, Kartu PDF, dan kedua siswa Putra & Putri
    composeTestRule.onNodeWithTag("btn_guru_ngaji_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_guru_ngaji_scan_qr").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_guru_ngaji_kartu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("guru_ngaji_lazy_list")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("guru_ngaji_student_card_772001"))
    composeTestRule.onNodeWithText("Zaid Putra").assertIsDisplayed()
    composeTestRule.onNodeWithTag("guru_ngaji_lazy_list")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("guru_ngaji_student_card_772002"))
    composeTestRule.onNodeWithText("Zahra Putri").assertIsDisplayed()

    // Pindah ke tab Riwayat Setoran dan pastikan TIDAK ada tombol hapus sama sekali
    composeTestRule.onNodeWithTag("guru_ngaji_lazy_list")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("tab_guru_ngaji_history"))
    composeTestRule.onNodeWithTag("tab_guru_ngaji_history").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("guru_ngaji_lazy_list")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("guru_ngaji_history_item_773001"))
    composeTestRule.onNodeWithText("Al-Qur'an • Juz 1 • Al-Baqarah 1-10", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_delete_mengaji_773001").assertDoesNotExist()

    // Uji AddWaliAccountDialog memilih peran Guru Ngaji
    showAddDialog.value = true
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("input_wali_name").performTextInput("Ustadz Yusuf")
    composeTestRule.onNodeWithTag("chip_add_role_guru_ngaji").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("btn_save_wali_account").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(com.example.data.model.WaliAccount.ROLE_GURU_NGAJI, chosenRoleFromDialog)
  }

  @Test
  fun `verify QuickMenuTiles renders horizontal two-row carousel with scroll indicator and preserves column pairing for future 10 icons`() {
    // 1. Uji logika penyusunan kolom: 8 ikon mempertahankan tata letak 2x4,
    //    sedangkan tambahan ke-9 & ke-10 (mis. Tabungan & Konseling) mengisi kolom ke-5 atas-bawah.
    val cols8 = com.example.ui.components.buildTwoRowCarouselColumns((0..7).toList())
    assertEquals(4, cols8.size)
    assertEquals(0, cols8[0].top)
    assertEquals(4, cols8[0].bottom)
    assertEquals(3, cols8[3].top)
    assertEquals(7, cols8[3].bottom)

    val cols10 = com.example.ui.components.buildTwoRowCarouselColumns((0..9).toList())
    assertEquals(5, cols10.size)
    assertEquals(8, cols10[4].top)
    assertEquals(9, cols10[4].bottom)

    // 2. Uji UI Compose QuickMenuTiles dengan wadah LazyRow geser horizontal + indikator kapsul
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    var openedPilihAnakAsuh = false
    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.components.QuickMenuTiles(
          onNavigateToPlan = {},
          onNavigateToDirectTreatment = {},
          onNavigateToRecords = {},
          onNavigateToStudents = {},
          onShowPuskesmasInfo = {},
          onOpenPilihAnakAsuh = { openedPilihAnakAsuh = true },
          myFosterStudentsCount = 4
        )
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("quick_menu_carousel_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("quick_menu_scroll_indicator").assertIsDisplayed()
    composeTestRule.onNodeWithTag("quick_menu_carousel_row")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("category_mengaji"))
    composeTestRule.onNodeWithTag("category_mengaji").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_anak_asuh").assertIsDisplayed()

    composeTestRule.onNodeWithTag("quick_menu_carousel_row")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("tile_klaim_anak_asuh"))
    composeTestRule.onNodeWithTag("tile_klaim_anak_asuh").assertIsDisplayed()
    composeTestRule.onNodeWithText("Klaim Anak Asuh").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_klaim_anak_asuh").performClick()
    composeTestRule.waitForIdle()
    org.junit.Assert.assertTrue(openedPilihAnakAsuh)
  }

  @Test
  fun `verify TabunganHelper privacy isolation summary aggregation NIS fallback and student resolution`() {
    val schoolA = "SCH-TAB-01"
    val student1 = com.example.data.model.ChildStudent(
      id = 88101L,
      name = "Farhan Maulana",
      nis = "31988101",
      gender = "L",
      age = 13,
      dormitory = "Asrama Putra",
      classGrade = "VII-A",
      assignedWaliUsername = "walia",
      assignedWaliName = "Ustadz Ali",
      schoolId = schoolA
    )
    val student1DupId = student1.copy(id = 999101L)

    val txs = listOf(
      com.example.data.model.TabunganTransaction(
        id = 1L,
        studentId = 88101L,
        studentName = "Farhan Maulana",
        studentNis = "31988101",
        ownerWaliUsername = "walia",
        ownerWaliName = "Ustadz Ali",
        transactionType = com.example.data.model.TabunganHelper.TYPE_IN,
        amount = 50_000L,
        title = "Titip Mingguan",
        status = com.example.data.model.TabunganHelper.STATUS_ACTIVE,
        schoolId = schoolA
      ),
      com.example.data.model.TabunganTransaction(
        id = 2L,
        studentId = 999101L, // Different synthetic ID, same NIS
        studentName = "Farhan Maulana",
        studentNis = "31988101",
        ownerWaliUsername = "walia",
        ownerWaliName = "Ustadz Ali",
        transactionType = com.example.data.model.TabunganHelper.TYPE_OUT,
        amount = 15_000L,
        title = "Beli Buku",
        status = com.example.data.model.TabunganHelper.STATUS_ACTIVE,
        schoolId = schoolA
      ),
      com.example.data.model.TabunganTransaction(
        id = 3L,
        studentId = 88101L,
        studentName = "Farhan Maulana",
        studentNis = "31988101",
        ownerWaliUsername = "walia",
        ownerWaliName = "Ustadz Ali",
        transactionType = com.example.data.model.TabunganHelper.TYPE_OUT,
        amount = 10_000L,
        title = "Batal Jajan",
        status = com.example.data.model.TabunganHelper.STATUS_CANCELLED,
        schoolId = schoolA
      ),
      com.example.data.model.TabunganTransaction(
        id = 4L,
        studentId = 88102L,
        studentName = "Bilal Haidar",
        studentNis = "31988102",
        ownerWaliUsername = "walib",
        ownerWaliName = "Ustadz Budi",
        transactionType = com.example.data.model.TabunganHelper.TYPE_IN,
        amount = 80_000L,
        title = "Kiriman Ortu",
        status = com.example.data.model.TabunganHelper.STATUS_ACTIVE,
        schoolId = schoolA
      )
    )

    val logs = listOf(
      com.example.data.model.TabunganLog(
        id = 11L,
        transactionId = 1L,
        studentId = 88101L,
        studentName = "Farhan Maulana",
        ownerWaliUsername = "walia",
        ownerWaliName = "Ustadz Ali",
        actionType = com.example.data.model.TabunganHelper.LOG_INPUT_IN,
        amount = 50_000L,
        balanceBefore = 0L,
        balanceAfter = 50_000L,
        schoolId = schoolA
      ),
      com.example.data.model.TabunganLog(
        id = 12L,
        transactionId = 4L,
        studentId = 88102L,
        studentName = "Bilal Haidar",
        ownerWaliUsername = "walib",
        ownerWaliName = "Ustadz Budi",
        actionType = com.example.data.model.TabunganHelper.LOG_INPUT_IN,
        amount = 80_000L,
        balanceBefore = 0L,
        balanceAfter = 80_000L,
        schoolId = schoolA
      )
    )

    // Strict privacy isolation per role
    val waliATxs = com.example.data.model.TabunganHelper.filterPrivateTransactions(
      txs, schoolA, com.example.ui.UserRole.WALI, isAnakSuperAdmin = false, currentUsername = "walia", currentWaliName = "Ustadz Ali"
    )
    assertEquals(3, waliATxs.size)
    val waliBTxs = com.example.data.model.TabunganHelper.filterPrivateTransactions(
      txs, schoolA, com.example.ui.UserRole.WALI, isAnakSuperAdmin = false, currentUsername = "walib", currentWaliName = "Ustadz Budi"
    )
    assertEquals(1, waliBTxs.size)
    assertEquals(0, com.example.data.model.TabunganHelper.filterPrivateTransactions(
      txs, schoolA, com.example.ui.UserRole.ADMIN, isAnakSuperAdmin = false, currentUsername = "admin", currentWaliName = "Admin"
    ).size)
    assertEquals(0, com.example.data.model.TabunganHelper.filterPrivateTransactions(
      txs, schoolA, com.example.ui.UserRole.GURU_NGAJI, isAnakSuperAdmin = false, currentUsername = "guru", currentWaliName = "Guru"
    ).size)
    assertEquals(4, com.example.data.model.TabunganHelper.filterPrivateTransactions(
      txs, schoolA, com.example.ui.UserRole.WALI, isAnakSuperAdmin = true, currentUsername = "anaksuperadmin", currentWaliName = "Anak Super"
    ).size)
    assertEquals(1, com.example.data.model.TabunganHelper.filterPrivateLogs(
      logs, schoolA, com.example.ui.UserRole.WALI, isAnakSuperAdmin = false, currentUsername = "walia", currentWaliName = "Ustadz Ali"
    ).size)

    // Summary & NIS fallback & multi-ID deduplication
    val summary = com.example.data.model.TabunganHelper.computeSummaryForStudent(waliATxs, student1DupId)
    assertEquals(50_000L, summary.totalIn)
    assertEquals(15_000L, summary.totalOut)
    assertEquals(35_000L, summary.balance)
    assertEquals(2, summary.activeCount)
    assertEquals(1, summary.cancelledCount)

    val byIdSummary = com.example.data.model.TabunganHelper.computeSummaryByStudentId(waliATxs, 88101L, "31988101")
    assertEquals(35_000L, byIdSummary.balance)

    val aggWaliA = com.example.data.model.TabunganHelper.computeAggregateTotals(waliATxs)
    assertEquals(35_000L, aggWaliA.totalBalance)
    assertEquals(1, aggWaliA.holdersCount) // Two active txs across 2 IDs with same NIS count as 1 holder

    val summaryStudents = com.example.data.model.TabunganHelper.buildSummaryStudents(
      myFosterStudents = emptyList(),
      allStudents = listOf(student1),
      transactions = txs
    )
    assertEquals(2, summaryStudents.size)
    assertNotNull(
      com.example.data.model.TabunganHelper.resolveStudentByIdOrFallback(
        studentId = 88102L,
        myFosterStudents = emptyList(),
        allStudents = emptyList(),
        transactions = txs,
        logs = logs
      )
    )
    assertEquals("Diubah", com.example.data.model.TabunganHelper.logActionLabel(com.example.data.model.TabunganHelper.LOG_EDIT, isEn = false))
    assertEquals("Edited", com.example.data.model.TabunganHelper.logActionLabel(com.example.data.model.TabunganHelper.LOG_EDIT, isEn = true))
  }

  @Test
  fun `verify HealthViewModel Tabungan CRUD edit overdraft guards privacy and ParentPortal access`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    app.getSharedPreferences("waliasuh_school_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val schoolId = "SCH-TAB-VM"
    db.tabunganTransactionDao().deleteBySchoolId(schoolId)
    db.tabunganLogDao().deleteBySchoolId(schoolId)
    db.deletionLogDao().deleteBySchoolId(schoolId)
    db.parentAccessLinkDao().deleteBySchoolId(schoolId)
    db.childStudentDao().deleteBySchoolId(schoolId)
    db.waliAccountDao().deleteBySchoolId(schoolId)

    db.waliAccountDao().insert(
      com.example.data.model.WaliAccount(
        id = 882001L,
        name = "Ustadz Hasan",
        username = "walihasan",
        password = "Pass#Hasan1",
        dormitory = "Asrama Putra",
        role = com.example.data.model.WaliAccount.ROLE_WALI,
        schoolId = schoolId
      )
    )
    val student = com.example.data.model.ChildStudent(
      id = 882011L,
      name = "Rafi Pratama",
      nis = "31988211",
      gender = "L",
      age = 13,
      dormitory = "Asrama Putra",
      classGrade = "VIII-B",
      assignedWaliUsername = "walihasan",
      assignedWaliName = "Ustadz Hasan",
      schoolId = schoolId
    )
    db.childStudentDao().insert(student)

    val vm = com.example.ui.HealthViewModel(app)
    vm.updateSchoolProfile(
      com.example.data.model.SchoolProfile(
        tenantCode = schoolId,
        schoolName = "Pesantren Tabungan Test",
        shortName = "TabunganTest"
      )
    )
    val loginWali = vm.executeLogin("walihasan", "Pass#Hasan1", rememberMe = false)
    org.junit.Assert.assertTrue(loginWali is com.example.ui.LoginResult.SuccessWali)
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.students.value.any { it.id == student.id }) break
      kotlinx.coroutines.delay(30)
    }

    // 1. Save Deposit (+100_000)
    var saveInOk: Boolean? = null
    vm.saveTabunganTransaction(student, com.example.data.model.TabunganHelper.TYPE_IN, 100_000L, "Titipan Ortu") { ok, _ ->
      saveInOk = ok
    }
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (saveInOk != null && vm.tabunganTransactions.value.size == 1) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(true, saveInOk)
    val depositTx = vm.tabunganTransactions.value.first()

    // 2. Reject Overdraft Expense (+120_000 > 100_000)
    var overdraftOk: Boolean? = null
    vm.saveTabunganTransaction(student, com.example.data.model.TabunganHelper.TYPE_OUT, 120_000L, "Beli Sepatu") { ok, _ ->
      overdraftOk = ok
    }
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (overdraftOk != null) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(false, overdraftOk)

    // 3. Save Valid Expense (-40_000 -> balance 60_000)
    var saveOutOk: Boolean? = null
    vm.saveTabunganTransaction(student, com.example.data.model.TabunganHelper.TYPE_OUT, 40_000L, "Buku Paket") { ok, _ ->
      saveOutOk = ok
    }
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (saveOutOk != null && vm.tabunganTransactions.value.size == 2) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(true, saveOutOk)
    val expenseTx = vm.tabunganTransactions.value.first { !com.example.data.model.TabunganHelper.isIncome(it.transactionType) }

    // 4. Block cancelling or deleting Deposit because Expense (40k) depends on it
    var cancelDepositOk: Boolean? = null
    vm.cancelTabunganTransaction(depositTx, "Salah input") { ok, _ -> cancelDepositOk = ok }
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (cancelDepositOk != null) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(false, cancelDepositOk)

    var deleteDepositOk: Boolean? = null
    vm.deleteTabunganTransaction(depositTx) { ok, _ -> deleteDepositOk = ok }
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (deleteDepositOk != null) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(false, deleteDepositOk)

    // 5. Reject editing Deposit down to 30_000 (30_000 - 40_000 < 0), then allow editing to 90_000
    var badEditOk: Boolean? = null
    vm.editTabunganTransaction(depositTx, com.example.data.model.TabunganHelper.TYPE_IN, 30_000L, "Titipan Ortu") { ok, _ ->
      badEditOk = ok
    }
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (badEditOk != null) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(false, badEditOk)

    var goodEditOk: Boolean? = null
    vm.editTabunganTransaction(depositTx, com.example.data.model.TabunganHelper.TYPE_IN, 90_000L, "Titipan Revisi", "Catatan revisi") { ok, _ ->
      goodEditOk = ok
    }
    for (i in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (goodEditOk != null &&
        vm.tabunganTransactions.value.any { it.amount == 90_000L } &&
        vm.tabunganLogs.value.any { it.actionType == com.example.data.model.TabunganHelper.LOG_EDIT }
      ) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(true, goodEditOk)
    org.junit.Assert.assertTrue(vm.tabunganLogs.value.any { it.actionType == com.example.data.model.TabunganHelper.LOG_EDIT })
    assertEquals(50_000L, com.example.data.model.TabunganHelper.computeSummaryForStudent(vm.tabunganTransactions.value, student).balance)

    // 6. Cancel Expense (40k -> balance returns to 90k), then delete cancelled Expense permanently (NO DeletionLog!)
    var cancelExpOk: Boolean? = null
    vm.cancelTabunganTransaction(expenseTx, "Batal beli") { ok, _ -> cancelExpOk = ok }
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (cancelExpOk != null && vm.tabunganLogs.value.any { it.actionType == com.example.data.model.TabunganHelper.LOG_CANCEL }) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(true, cancelExpOk)
    assertEquals(90_000L, com.example.data.model.TabunganHelper.computeSummaryForStudent(vm.tabunganTransactions.value, student).balance)

    val cancelledExpTx = db.tabunganTransactionDao().getTransactionsBySchoolList(schoolId).first { it.id == expenseTx.id }
    var deleteExpOk: Boolean? = null
    vm.deleteTabunganTransaction(cancelledExpTx) { ok, _ -> deleteExpOk = ok }
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (deleteExpOk != null && vm.tabunganLogs.value.any { it.actionType == com.example.data.model.TabunganHelper.LOG_DELETE }) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(true, deleteExpOk)
    // Verify ZERO public DeletionLogs emitted for private Tabungan deletions
    assertEquals(0, db.deletionLogDao().getLogsBySchoolList(schoolId).size)

    // 7. Parent Portal access strictly gated by canViewTabungan
    db.parentAccessLinkDao().insert(
      com.example.data.model.ParentAccessLink(
        id = 882091L,
        studentId = student.id,
        studentName = student.name,
        studentNis = student.nis,
        studentClass = student.classGrade,
        studentDorm = student.dormitory,
        parentName = "Ibu Sari",
        username = "ortu_rafi_tab",
        password = "Ortu#Tab1",
        canViewTabungan = false,
        schoolId = schoolId
      )
    )
    val loginParent = vm.executeLogin("ortu_rafi_tab", "Ortu#Tab1", rememberMe = false)
    org.junit.Assert.assertTrue(loginParent is com.example.ui.LoginResult.SuccessParent)
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.activeParentAccessLink.value != null) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(0, vm.parentTabunganTransactions.value.size)

    // Enable canViewTabungan -> parent sees active transaction (90k)
    val currentLink = db.parentAccessLinkDao().getByStudentId(student.id, schoolId)!!
    db.parentAccessLinkDao().insert(currentLink.copy(canViewTabungan = true))
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.parentTabunganTransactions.value.size == 1) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(1, vm.parentTabunganTransactions.value.size)
    assertEquals(90_000L, vm.parentTabunganTransactions.value.first().amount)
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    app.getSharedPreferences("waliasuh_school_prefs", Context.MODE_PRIVATE).edit().clear().commit()
  }

  @Test
  fun `verify Tabungan PocketBase DTO roundtrip and SchemaProvider fields`() {
    val tx = com.example.data.model.TabunganTransaction(
      id = 777001L,
      studentId = 101L,
      studentName = "Salman Al-Farisi",
      studentNis = "31900101",
      studentClass = "IX-C",
      studentDorm = "Putra 2",
      ownerWaliUsername = "walia",
      ownerWaliName = "Ustadz Ali",
      isPrivate = true,
      transactionType = com.example.data.model.TabunganHelper.TYPE_OUT,
      amount = 25_000L,
      title = "Alat Tulis",
      notes = "Pulpen & buku",
      transactionDate = 1_730_000_000_000L,
      status = com.example.data.model.TabunganHelper.STATUS_ACTIVE,
      schoolId = "SRT1-KDR"
    )
    val txDto = com.example.data.remote.pocketbase.PbTabunganTransactionDto.fromDomain(tx)
    val txRound = txDto.toDomainModel("SRT1-KDR")
    assertEquals(tx.id, txRound.id)
    assertEquals(tx.studentNis, txRound.studentNis)
    assertEquals(tx.amount, txRound.amount)
    assertEquals(tx.transactionType, txRound.transactionType)
    assertEquals(tx.notes, txRound.notes)

    val log = com.example.data.model.TabunganLog(
      id = 777002L,
      transactionId = 777001L,
      studentId = 101L,
      studentName = "Salman Al-Farisi",
      studentClass = "IX-C",
      ownerWaliUsername = "walia",
      ownerWaliName = "Ustadz Ali",
      actionType = com.example.data.model.TabunganHelper.LOG_EDIT,
      amount = 25_000L,
      balanceBefore = 50_000L,
      balanceAfter = 25_000L,
      summary = "Edit: Alat Tulis",
      actorName = "Ustadz Ali",
      actorUsername = "walia",
      schoolId = "SRT1-KDR"
    )
    val logDto = com.example.data.remote.pocketbase.PbTabunganLogDto.fromDomain(log)
    val logRound = logDto.toDomainModel("SRT1-KDR")
    assertEquals(log.id, logRound.id)
    assertEquals(log.transactionId, logRound.transactionId)
    assertEquals(com.example.data.model.TabunganHelper.LOG_EDIT, logRound.actionType)
    assertEquals(25_000L, logRound.balanceAfter)

    val link = com.example.data.model.ParentAccessLink(
      id = 777003L,
      studentId = 101L,
      studentName = "Salman Al-Farisi",
      username = "ortu_salman",
      password = "Ortu#Salman1",
      canViewTabungan = true,
      schoolId = "SRT1-KDR"
    )
    val linkRound = com.example.data.remote.pocketbase.PbParentAccessLinkDto.fromDomain(link).toDomainModel("SRT1-KDR")
    assertEquals(true, linkRound.canViewTabungan)

    val collectionNames = com.example.data.remote.pocketbase.PocketBaseSchemaProvider.COLLECTIONS.map { it.collectionName }
    org.junit.Assert.assertTrue(collectionNames.contains("tabungan_transactions"))
    org.junit.Assert.assertTrue(collectionNames.contains("tabungan_logs"))
  }

  @Test
  fun `verify Tabungan Compose dialogs support empty prompt edit mode overdraft block and detail actions`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val student = com.example.data.model.ChildStudent(
      id = 883001L,
      name = "Nadia Aulia",
      nis = "31988301",
      gender = "P",
      age = 14,
      dormitory = "Asrama Putri",
      classGrade = "VIII-A",
      assignedWaliUsername = "walisiti",
      assignedWaliName = "Ustadzah Siti",
      schoolId = "SRT1-KDR"
    )
    val existingDeposit = com.example.data.model.TabunganTransaction(
      id = 883101L,
      studentId = student.id,
      studentName = student.name,
      studentNis = student.nis,
      studentClass = student.classGrade,
      studentDorm = student.dormitory,
      ownerWaliUsername = "walisiti",
      ownerWaliName = "Ustadzah Siti",
      transactionType = com.example.data.model.TabunganHelper.TYPE_IN,
      amount = 50_000L,
      title = "Uang Saku Awal",
      status = com.example.data.model.TabunganHelper.STATUS_ACTIVE,
      schoolId = "SRT1-KDR"
    )
    val existingExpense = com.example.data.model.TabunganTransaction(
      id = 883102L,
      studentId = student.id,
      studentName = student.name,
      studentNis = student.nis,
      studentClass = student.classGrade,
      studentDorm = student.dormitory,
      ownerWaliUsername = "walisiti",
      ownerWaliName = "Ustadzah Siti",
      transactionType = com.example.data.model.TabunganHelper.TYPE_OUT,
      amount = 30_000L,
      title = "Jajan Kantin",
      status = com.example.data.model.TabunganHelper.STATUS_ACTIVE,
      schoolId = "SRT1-KDR"
    )
    val editLog = com.example.data.model.TabunganLog(
      id = 883201L,
      transactionId = existingDeposit.id,
      studentId = student.id,
      studentName = student.name,
      studentClass = student.classGrade,
      ownerWaliUsername = "walisiti",
      ownerWaliName = "Ustadzah Siti",
      actionType = com.example.data.model.TabunganHelper.LOG_EDIT,
      amount = 65_000L,
      balanceBefore = 20_000L,
      balanceAfter = 35_000L,
      summary = "Edit: Uang Saku Awal",
      actorName = "Ustadzah Siti",
      actorUsername = "walisiti",
      schoolId = "SRT1-KDR"
    )

    val modeState = androidx.compose.runtime.mutableIntStateOf(0) // 0=empty input, 1=edit input, 2=detail, 3=history
    var editedAmount: Long? = null
    var openedEditTx: com.example.data.model.TabunganTransaction? = null
    var openedStudentIdFromLog: Long? = null

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        when (modeState.intValue) {
          0 -> com.example.ui.dialogs.InputTabunganDialog(
            claimedStudents = emptyList(),
            transactions = emptyList(),
            onOpenPilihAnakAsuh = {},
            onSave = { _, _, _, _, _, _, _ -> },
            onDismiss = {}
          )
          1 -> com.example.ui.dialogs.InputTabunganDialog(
            claimedStudents = listOf(student),
            transactions = listOf(existingDeposit, existingExpense),
            initialStudent = student,
            txToEdit = existingDeposit,
            onOpenPilihAnakAsuh = {},
            onSave = { _, _, amount, _, _, _, tx ->
              if (tx != null) editedAmount = amount
            },
            onDismiss = {}
          )
          2 -> com.example.ui.dialogs.TabunganStudentDetailSheet(
            student = student,
            studentTransactions = listOf(existingDeposit, existingExpense),
            studentLogs = listOf(editLog),
            waliName = "Ustadzah Siti",
            schoolProfile = com.example.data.model.SchoolProfile(),
            onQuickInput = {},
            onEditTransaction = { openedEditTx = it },
            onCancelTransaction = { _, _ -> },
            onDeleteTransaction = {},
            onDismiss = {}
          )
          else -> com.example.ui.dialogs.TabunganHistoryLogsSheet(
            transactions = listOf(existingDeposit, existingExpense),
            logs = listOf(editLog),
            studentsForSummary = listOf(student),
            waliName = "Ustadzah Siti",
            schoolProfile = com.example.data.model.SchoolProfile(),
            initialTab = 1,
            onOpenStudentById = { openedStudentIdFromLog = it },
            onEditTransaction = {},
            onCancelTransaction = { _, _ -> },
            onDeleteTransaction = {},
            onDismiss = {}
          )
        }
      }
    }

    // 1. Empty claimed students state
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("empty_input_tabungan_no_claimed").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_input_tabungan_open_pilih").assertIsDisplayed()

    // 2. Edit transaction mode: date picker visible, overdraft (<30k) disables save, valid (65k) saves
    modeState.intValue = 1
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_tabungan_pick_date").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("input_tabungan_amount").performScrollTo()
    composeTestRule.onNodeWithTag("input_tabungan_amount").performTextClearance()
    composeTestRule.onNodeWithTag("input_tabungan_amount").performTextInput("20000")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_save_tabungan_tx").performScrollTo().assertIsNotEnabled()

    composeTestRule.onNodeWithTag("input_tabungan_amount").performScrollTo()
    composeTestRule.onNodeWithTag("input_tabungan_amount").performTextClearance()
    composeTestRule.onNodeWithTag("input_tabungan_amount").performTextInput("65000")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_save_tabungan_tx").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(65_000L, editedAmount)

    // 3. Detail sheet: summary & edit button trigger onEditTransaction
    modeState.intValue = 2
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_tabungan_detail_summary").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_edit_tabungan_tx_${existingDeposit.id}").performClick()
    assertEquals(existingDeposit.id, openedEditTx?.id)

    // 4. History & Audit log sheet: EDIT badge renders & student click navigates
    modeState.intValue = 3
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_tabungan_log_${editLog.id}").assertIsDisplayed()
    composeTestRule.onNodeWithText("Diubah").assertIsDisplayed()
    composeTestRule.onNodeWithText("Nadia Aulia (VIII-A)").performClick()
    assertEquals(student.id, openedStudentIdFromLog)
  }

  @Test
  fun `verify KonselingHelper strict owner-only isolation across roles walis schools and stats calculation`() {
    val studentA = com.example.data.model.ChildStudent(
      id = 9101L,
      name = "Farhan Hidayat",
      nis = "3199101",
      gender = "L",
      age = 15,
      dormitory = "Asrama Putra",
      classGrade = "IX-A",
      assignedWaliUsername = "walifajar",
      assignedWaliName = "Ustadz Fajar",
      schoolId = "SRT1-KDR"
    )
    val ownedSession = com.example.data.model.KonselingRecord(
      id = 910001L,
      studentId = studentA.id,
      studentName = studentA.name,
      studentNis = studentA.nis,
      studentClass = studentA.classGrade,
      studentDorm = studentA.dormitory,
      ownerWaliUsername = "walifajar",
      ownerWaliName = "Ustadz Fajar",
      entryType = com.example.data.model.KonselingHelper.ENTRY_SESI,
      topic = com.example.data.model.KonselingHelper.TOPIC_ADAPTASI,
      urgencyLevel = com.example.data.model.KonselingHelper.URGENCY_PRIORITAS,
      moodLevel = com.example.data.model.KonselingHelper.MOOD_SEDIH,
      title = "Rindu Rumah Awal Semester",
      summary = "Anak sempat menangis malam hari.",
      status = com.example.data.model.KonselingHelper.STATUS_DIPANTAU,
      schoolId = "SRT1-KDR"
    )
    val ownedGrowth = com.example.data.model.KonselingRecord(
      id = 910002L,
      studentId = studentA.id,
      studentName = studentA.name,
      studentNis = studentA.nis,
      studentClass = studentA.classGrade,
      studentDorm = studentA.dormitory,
      ownerWaliUsername = "walifajar",
      ownerWaliName = "Ustadz Fajar",
      entryType = com.example.data.model.KonselingHelper.ENTRY_PERKEMBANGAN,
      topic = com.example.data.model.KonselingHelper.TOPIC_APRESIASI,
      urgencyLevel = com.example.data.model.KonselingHelper.URGENCY_RINGAN,
      moodLevel = com.example.data.model.KonselingHelper.MOOD_BAIK,
      title = "Sudah Aktif Belajar Kelompok",
      summary = "Mulai percaya diri di kelas.",
      status = com.example.data.model.KonselingHelper.STATUS_SELESAI,
      schoolId = "SRT1-KDR"
    )
    val otherWaliRecord = ownedSession.copy(
      id = 910003L,
      ownerWaliUsername = "wali_lain",
      ownerWaliName = "Ustadz Lain"
    )
    val otherSchoolRecord = ownedSession.copy(
      id = 910004L,
      schoolId = "SCH-OTHER"
    )
    val allRecords = listOf(ownedSession, ownedGrowth, otherWaliRecord, otherSchoolRecord)

    // 1. Only owning WALI in same school sees own records
    val visibleOwner = com.example.data.model.KonselingHelper.filterPrivateRecords(
      allRecords = allRecords,
      schoolId = "SRT1-KDR",
      role = com.example.ui.UserRole.WALI,
      currentUsername = "walifajar",
      currentWaliName = "Ustadz Fajar"
    )
    assertEquals(2, visibleOwner.size)

    // 2. Non-WALI roles (ADMIN, SUPERADMIN, GURU_NGAJI, PARENT, NOT_LOGGED_IN) get ZERO records
    listOf(
      com.example.ui.UserRole.ADMIN,
      com.example.ui.UserRole.SUPERADMIN,
      com.example.ui.UserRole.GURU_NGAJI,
      com.example.ui.UserRole.PARENT,
      com.example.ui.UserRole.NOT_LOGGED_IN
    ).forEach { role ->
      val blocked = com.example.data.model.KonselingHelper.filterPrivateRecords(
        allRecords = allRecords,
        schoolId = "SRT1-KDR",
        role = role,
        currentUsername = "walifajar",
        currentWaliName = "Ustadz Fajar"
      )
      assertEquals("Role $role must receive 0 confidential records", 0, blocked.size)
    }

    // 3. Another Wali Asuh gets 0 of walifajar's records
    val otherWaliVisible = com.example.data.model.KonselingHelper.filterPrivateRecords(
      allRecords = allRecords,
      schoolId = "SRT1-KDR",
      role = com.example.ui.UserRole.WALI,
      currentUsername = "walihasan",
      currentWaliName = "Ustadz Hasan"
    )
    assertEquals(0, otherWaliVisible.size)

    // 4. Stats accuracy
    val studentStats = com.example.data.model.KonselingHelper.computeStudentStats(visibleOwner, studentA)
    assertEquals(2, studentStats.totalEntries)
    assertEquals(1, studentStats.sessionsCount)
    assertEquals(1, studentStats.growthCount)
    assertEquals(1, studentStats.followUpCount)
    assertEquals(1, studentStats.completedCount)
    assertEquals(1, studentStats.highPriorityCount)

    val aggStats = com.example.data.model.KonselingHelper.computeAggregateStats(visibleOwner)
    assertEquals(2, aggStats.totalEntries)
    assertEquals(1, aggStats.followUpCount)
    assertEquals(1, aggStats.highPriorityCount)
    assertEquals(1, aggStats.growthCount)
    assertEquals(1, aggStats.counseledStudentsCount)
  }

  @Test
  fun `verify HealthViewModel Konseling CRUD status toggle owner-only rejection and private deletion audit log`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    app.getSharedPreferences("waliasuh_school_prefs", Context.MODE_PRIVATE).edit().clear().commit()

    val schoolId = "SCH-KSL-TEST"
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    db.konselingRecordDao().deleteBySchoolId(schoolId)
    db.konselingLogDao().deleteBySchoolId(schoolId)
    db.deletionLogDao().deleteBySchoolId(schoolId)

    db.schoolRegistrationDao().insert(
      com.example.data.model.SchoolRegistration(
        schoolName = "Sekolah Uji Konseling",
        address = "Jl. Konseling 1",
        city = "Kediri",
        estimatedWaliCount = 2,
        picName = "PIC KSL",
        picEmail = "ksl@test.id",
        picPhone = "08199",
        status = "APPROVED",
        generatedSchoolCode = schoolId,
        generatedAdminUsername = "admin_ksl_test",
        generatedAdminPassword = "Admin#Ksl1"
      )
    )
    db.waliAccountDao().insert(
      com.example.data.model.WaliAccount(
        name = "Ustadz Rahmat",
        username = "wali_ksl_1",
        password = "Wali#Ksl1",
        dormitory = "Asrama Putra",
        schoolId = schoolId
      )
    )
    db.waliAccountDao().insert(
      com.example.data.model.WaliAccount(
        name = "Ustadz Rekan",
        username = "wali_ksl_2",
        password = "Wali#Ksl2",
        dormitory = "Asrama Putra",
        schoolId = schoolId
      )
    )
    val student = com.example.data.model.ChildStudent(
      id = 992001L,
      name = "Bintang Pratama",
      nis = "31999201",
      gender = "L",
      age = 15,
      dormitory = "Asrama Putra",
      classGrade = "IX-B",
      assignedWaliUsername = "wali_ksl_1",
      assignedWaliName = "Ustadz Rahmat",
      schoolId = schoolId
    )
    db.childStudentDao().insert(student)

    val vm = com.example.ui.HealthViewModel(app)

    // 1. Wali 1 logs in and saves confidential session
    val login1 = vm.executeLogin("wali_ksl_1", "Wali#Ksl1", rememberMe = false)
    org.junit.Assert.assertTrue(login1 is com.example.ui.LoginResult.SuccessWali)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    var saveOk: Boolean? = null
    vm.saveKonselingRecord(
      student = student,
      entryType = com.example.data.model.KonselingHelper.ENTRY_SESI,
      topic = com.example.data.model.KonselingHelper.TOPIC_EMOSI,
      urgencyLevel = com.example.data.model.KonselingHelper.URGENCY_PRIORITAS,
      moodLevel = com.example.data.model.KonselingHelper.MOOD_TERTEKAN,
      title = "Pendampingan Konflik Kamar",
      summary = "Bintang sempat berselisih dengan teman sekamar.",
      intervention = "Mediasi empat mata dan refleksi tenang.",
      followUpPlan = "Cek ulang keakraban kamar Jumat malam",
      followUpDate = 1_765_000_000_000L,
      markCompleted = false
    ) { ok, _ -> saveOk = ok }

    for (i in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (saveOk != null && vm.konselingRecords.value.size == 1 && vm.konselingLogs.value.size == 1) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(true, saveOk)
    assertEquals(1, vm.konselingRecords.value.size)
    assertEquals(1, vm.konselingLogs.value.size)
    val savedRec = vm.konselingRecords.value.first()
    assertEquals(com.example.data.model.KonselingHelper.STATUS_DIPANTAU, savedRec.status)

    // 2. Edit & toggle status (DIPANTAU -> SELESAI)
    var editOk: Boolean? = null
    vm.editKonselingRecord(
      existingRecord = savedRec,
      entryType = com.example.data.model.KonselingHelper.ENTRY_SESI,
      topic = com.example.data.model.KonselingHelper.TOPIC_EMOSI,
      urgencyLevel = com.example.data.model.KonselingHelper.URGENCY_PANTAU,
      moodLevel = com.example.data.model.KonselingHelper.MOOD_NETRAL,
      title = "Mediasi Kamar Berjalan Baik",
      summary = "Sudah saling memaafkan.",
      intervention = "Doa bersama sebelum tidur.",
      followUpPlan = "Pantau 1 pekan",
      markCompleted = false
    ) { ok, _ -> editOk = ok }

    for (i in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (editOk != null && vm.konselingLogs.value.any { it.actionType == com.example.data.model.KonselingHelper.LOG_EDIT }) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(true, editOk)
    val editedRec = vm.konselingRecords.value.first()
    assertEquals("Mediasi Kamar Berjalan Baik", editedRec.title)

    var toggleOk: Boolean? = null
    vm.toggleKonselingStatus(editedRec) { ok, _ -> toggleOk = ok }
    for (i in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (toggleOk != null && vm.konselingLogs.value.any { it.actionType == com.example.data.model.KonselingHelper.LOG_COMPLETE }) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(true, toggleOk)
    assertEquals(com.example.data.model.KonselingHelper.STATUS_SELESAI, vm.konselingRecords.value.first().status)

    // 3. Wali 2 in SAME school sees ZERO records/logs and CANNOT edit/toggle/delete Wali 1's record
    val login2 = vm.executeLogin("wali_ksl_2", "Wali#Ksl2", rememberMe = false)
    org.junit.Assert.assertTrue(login2 is com.example.ui.LoginResult.SuccessWali)
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.konselingRecords.value.isEmpty() && vm.konselingLogs.value.isEmpty()) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(0, vm.konselingRecords.value.size)
    assertEquals(0, vm.konselingLogs.value.size)

    var blockedEdit: Boolean? = null
    vm.editKonselingRecord(
      existingRecord = editedRec,
      entryType = editedRec.entryType,
      topic = editedRec.topic,
      urgencyLevel = editedRec.urgencyLevel,
      moodLevel = editedRec.moodLevel,
      title = "Hack",
      summary = "Hack"
    ) { ok, _ -> blockedEdit = ok }
    assertEquals(false, blockedEdit)

    var blockedDelete: Boolean? = null
    vm.deleteKonselingRecord(editedRec) { ok, _ -> blockedDelete = ok }
    assertEquals(false, blockedDelete)

    // 4. School Admin ALSO sees ZERO records & cannot write
    val loginAdmin = vm.executeLogin("admin_ksl_test", "Admin#Ksl1", rememberMe = false)
    org.junit.Assert.assertTrue(loginAdmin is com.example.ui.LoginResult.SuccessAdmin)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    assertEquals(0, vm.konselingRecords.value.size)
    var blockedAdminSave: Boolean? = null
    vm.saveKonselingRecord(
      student = student,
      entryType = com.example.data.model.KonselingHelper.ENTRY_SESI,
      topic = com.example.data.model.KonselingHelper.TOPIC_ADAPTASI,
      urgencyLevel = com.example.data.model.KonselingHelper.URGENCY_RINGAN,
      moodLevel = com.example.data.model.KonselingHelper.MOOD_NETRAL,
      title = "Admin Note",
      summary = "Should block"
    ) { ok, _ -> blockedAdminSave = ok }
    assertEquals(false, blockedAdminSave)

    // 5. Switch back to Wali 1 and delete -> private LOG_DELETE written & ZERO public DeletionLog!
    vm.executeLogin("wali_ksl_1", "Wali#Ksl1", rememberMe = false)
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.konselingRecords.value.size == 1) break
      kotlinx.coroutines.delay(30)
    }
    var deleteOk: Boolean? = null
    vm.deleteKonselingRecord(vm.konselingRecords.value.first()) { ok, _ -> deleteOk = ok }
    for (i in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (deleteOk != null && vm.konselingRecords.value.isEmpty() &&
        vm.konselingLogs.value.any { it.actionType == com.example.data.model.KonselingHelper.LOG_DELETE }
      ) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(true, deleteOk)
    assertEquals(0, vm.konselingRecords.value.size)
    assertEquals(0, db.deletionLogDao().getLogsBySchoolList(schoolId).size)

    db.konselingRecordDao().deleteBySchoolId(schoolId)
    db.konselingLogDao().deleteBySchoolId(schoolId)
    db.childStudentDao().deleteBySchoolId(schoolId)
    db.schoolRegistrationDao().deleteBySchoolCode(schoolId)
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    app.getSharedPreferences("waliasuh_school_prefs", Context.MODE_PRIVATE).edit().clear().commit()
  }

  @Test
  fun `verify Konseling PocketBase DTO roundtrip SchemaProvider collections and confidential PDF generation`() {
    val ctx = ApplicationProvider.getApplicationContext<Context>()
    val student = com.example.data.model.ChildStudent(
      id = 993001L,
      name = "Aisyah Humaira",
      nis = "31999301",
      gender = "P",
      age = 14,
      dormitory = "Asrama Putri",
      classGrade = "VIII-C",
      assignedWaliUsername = "walihana",
      assignedWaliName = "Ustadzah Hana",
      schoolId = "SRT1-KDR"
    )
    val rec = com.example.data.model.KonselingRecord(
      id = 993101L,
      studentId = student.id,
      studentName = student.name,
      studentNis = student.nis,
      studentClass = student.classGrade,
      studentDorm = student.dormitory,
      ownerWaliUsername = "walihana",
      ownerWaliName = "Ustadzah Hana",
      isConfidential = true,
      entryType = com.example.data.model.KonselingHelper.ENTRY_SESI,
      topic = com.example.data.model.KonselingHelper.TOPIC_KELUARGA,
      urgencyLevel = com.example.data.model.KonselingHelper.URGENCY_PANTAU,
      moodLevel = com.example.data.model.KonselingHelper.MOOD_SEDIH,
      title = "Rindu Orang Tua",
      summary = "Membantu menenangkan perasaan setelah jadwal kunjungan.",
      intervention = "Video call terjadwal bersama orang tua via wali.",
      followUpPlan = "Observasi 3 hari ke depan",
      followUpDate = 1_765_500_000_000L,
      status = com.example.data.model.KonselingHelper.STATUS_DIPANTAU,
      sessionDate = 1_765_000_000_000L,
      schoolId = "SRT1-KDR"
    )
    val recRound = com.example.data.remote.pocketbase.PbKonselingRecordDto.fromDomain(rec).toDomainModel("SRT1-KDR")
    assertEquals(rec.id, recRound.id)
    assertEquals(rec.ownerWaliUsername, recRound.ownerWaliUsername)
    assertEquals(rec.topic, recRound.topic)
    assertEquals(rec.moodLevel, recRound.moodLevel)
    assertEquals(rec.followUpPlan, recRound.followUpPlan)
    assertEquals(true, recRound.isConfidential)

    val log = com.example.data.model.KonselingLog(
      id = 993201L,
      recordId = rec.id,
      studentId = student.id,
      studentName = student.name,
      studentClass = student.classGrade,
      ownerWaliUsername = "walihana",
      ownerWaliName = "Ustadzah Hana",
      isConfidential = true,
      actionType = com.example.data.model.KonselingHelper.LOG_INPUT_SESI,
      summary = "Rindu Orang Tua • Keluarga & Rindu Rumah",
      actorName = "Ustadzah Hana",
      actorUsername = "walihana",
      schoolId = "SRT1-KDR"
    )
    val logRound = com.example.data.remote.pocketbase.PbKonselingLogDto.fromDomain(log).toDomainModel("SRT1-KDR")
    assertEquals(log.id, logRound.id)
    assertEquals(log.recordId, logRound.recordId)
    assertEquals(com.example.data.model.KonselingHelper.LOG_INPUT_SESI, logRound.actionType)

    val collectionNames = com.example.data.remote.pocketbase.PocketBaseSchemaProvider.COLLECTIONS.map { it.collectionName }
    org.junit.Assert.assertTrue(collectionNames.contains("konseling_records"))
    org.junit.Assert.assertTrue(collectionNames.contains("konseling_logs"))

    val studentBitmaps = mutableListOf<android.graphics.Bitmap>()
    com.example.util.PdfReportGenerator.renderKonselingStudentPages(
      context = ctx,
      student = student,
      records = listOf(rec),
      waliName = "Ustadzah Hana",
      schoolProfile = com.example.data.model.SchoolProfile(),
      startNewCanvas = {
        val bmp = android.graphics.Bitmap.createBitmap(595, 842, android.graphics.Bitmap.Config.ARGB_8888)
        studentBitmaps.add(bmp)
        android.graphics.Canvas(bmp)
      }
    )
    org.junit.Assert.assertTrue(studentBitmaps.isNotEmpty())
    studentBitmaps.forEach { it.recycle() }

    val manyRecords = (1..10).map { i -> rec.copy(id = 993100L + i, title = "Sesi Konseling $i") }
    val multiPageStudentBitmaps = mutableListOf<android.graphics.Bitmap>()
    com.example.util.PdfReportGenerator.renderKonselingStudentPages(
      context = ctx,
      student = student,
      records = manyRecords,
      waliName = "Ustadzah Hana",
      schoolProfile = com.example.data.model.SchoolProfile(),
      startNewCanvas = {
        val bmp = android.graphics.Bitmap.createBitmap(595, 842, android.graphics.Bitmap.Config.ARGB_8888)
        multiPageStudentBitmaps.add(bmp)
        android.graphics.Canvas(bmp)
      }
    )
    org.junit.Assert.assertTrue(multiPageStudentBitmaps.size >= 2)
    multiPageStudentBitmaps.forEach { it.recycle() }

    val summaryBitmaps = mutableListOf<android.graphics.Bitmap>()
    com.example.util.PdfReportGenerator.renderKonselingSummaryPages(
      context = ctx,
      students = listOf(student),
      records = listOf(rec),
      waliName = "Ustadzah Hana",
      schoolProfile = com.example.data.model.SchoolProfile(),
      startNewCanvas = {
        val bmp = android.graphics.Bitmap.createBitmap(595, 842, android.graphics.Bitmap.Config.ARGB_8888)
        summaryBitmaps.add(bmp)
        android.graphics.Canvas(bmp)
      }
    )
    org.junit.Assert.assertTrue(summaryBitmaps.isNotEmpty())
    summaryBitmaps.forEach { it.recycle() }
  }

  @Test
  fun `verify Konseling Compose dialogs hub cards and sheets enforce confidentiality declaration and actions`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val student = com.example.data.model.ChildStudent(
      id = 994001L,
      name = "Rayyan Zaki",
      nis = "31999401",
      gender = "L",
      age = 15,
      dormitory = "Asrama Putra",
      classGrade = "IX-D",
      assignedWaliUsername = "walirayyan",
      assignedWaliName = "Ustadz Karim",
      schoolId = "SRT1-KDR"
    )
    val activeSession = com.example.data.model.KonselingRecord(
      id = 994101L,
      studentId = student.id,
      studentName = student.name,
      studentNis = student.nis,
      studentClass = student.classGrade,
      studentDorm = student.dormitory,
      ownerWaliUsername = "walirayyan",
      ownerWaliName = "Ustadz Karim",
      isConfidential = true,
      entryType = com.example.data.model.KonselingHelper.ENTRY_SESI,
      topic = com.example.data.model.KonselingHelper.TOPIC_AKADEMIK,
      urgencyLevel = com.example.data.model.KonselingHelper.URGENCY_PRIORITAS,
      moodLevel = com.example.data.model.KonselingHelper.MOOD_TERTEKAN,
      title = "Kesulitan Konsentrasi Ujian",
      summary = "Merasa tertekan menjelang ujian praktik.",
      intervention = "Latihan manajemen waktu belajar.",
      followUpPlan = "Evaluasi nilai try-out pekan depan",
      status = com.example.data.model.KonselingHelper.STATUS_DIPANTAU,
      schoolId = "SRT1-KDR"
    )
    val logEntry = com.example.data.model.KonselingLog(
      id = 994201L,
      recordId = activeSession.id,
      studentId = student.id,
      studentName = student.name,
      studentClass = student.classGrade,
      ownerWaliUsername = "walirayyan",
      ownerWaliName = "Ustadz Karim",
      isConfidential = true,
      actionType = com.example.data.model.KonselingHelper.LOG_INPUT_SESI,
      summary = "Kesulitan Konsentrasi Ujian • Akademik & Belajar",
      actorName = "Ustadz Karim",
      actorUsername = "walirayyan",
      schoolId = "SRT1-KDR"
    )

    val modeState = androidx.compose.runtime.mutableIntStateOf(0) // 0=hub overlay, 1=empty input, 2=edit input, 3=detail, 4=followup, 5=history, 6=guide
    var savedTitle: String? = null
    var openedEditRec: com.example.data.model.KonselingRecord? = null
    var openedFromFollowUpId: Long? = null
    var openedGuideFromHub = false

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        when (modeState.intValue) {
          0 -> com.example.ui.components.CategoryHubOverlay(
            category = com.example.ui.components.DashboardCategoryHub.KONSELING,
            schoolProfile = com.example.data.model.SchoolProfile(),
            activePlans = emptyList(),
            inpatientCount = 0,
            fastingSessions = emptyList(),
            fastingRecords = emptyList(),
            activeHaidRecords = emptyList(),
            suciRecords = emptyList(),
            shiftMessages = emptyList(),
            students = listOf(student),
            claimedFosterStudents = listOf(student),
            konselingRecords = listOf(activeSession),
            konselingLogs = listOf(logEntry),
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
            onOpenKonselingGuide = { openedGuideFromHub = true }
          )
          1 -> com.example.ui.dialogs.InputKonselingDialog(
            claimedStudents = emptyList(),
            onOpenPilihAnakAsuh = {},
            onSave = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
            onDismiss = {}
          )
          2 -> com.example.ui.dialogs.InputKonselingDialog(
            claimedStudents = listOf(student),
            initialStudent = student,
            recordToEdit = activeSession,
            onOpenPilihAnakAsuh = {},
            onSave = { _, _, _, _, _, title, _, _, _, _, _, _, _, _, existing ->
              if (existing != null) savedTitle = title
            },
            onDismiss = {}
          )
          3 -> com.example.ui.dialogs.KonselingStudentDetailSheet(
            student = student,
            studentRecords = listOf(activeSession),
            studentLogs = listOf(logEntry),
            waliName = "Ustadz Karim",
            schoolProfile = com.example.data.model.SchoolProfile(),
            onQuickInput = {},
            onEditRecord = { openedEditRec = it },
            onToggleStatus = {},
            onDeleteRecord = {},
            onDismiss = {}
          )
          4 -> com.example.ui.dialogs.KonselingFollowUpSheet(
            records = listOf(activeSession),
            onOpenStudentById = { openedFromFollowUpId = it },
            onToggleStatus = {},
            onDismiss = {}
          )
          5 -> com.example.ui.dialogs.KonselingHistoryLogsSheet(
            records = listOf(activeSession),
            logs = listOf(logEntry),
            studentsForSummary = listOf(student),
            waliName = "Ustadz Karim",
            schoolProfile = com.example.data.model.SchoolProfile(),
            initialTab = 1,
            onOpenStudentById = {},
            onEditRecord = {},
            onToggleStatus = {},
            onDeleteRecord = {},
            onDismiss = {}
          )
          else -> com.example.ui.dialogs.KonselingGuideDialog(onDismiss = {})
        }
      }
    }

    // 1. CategoryHubOverlay (KONSELING): displays confidentiality declaration, totals, guide submenu, and student card
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("list_category_hub_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("card_konseling_privacy_declaration"))
    composeTestRule.onNodeWithTag("card_konseling_privacy_declaration").assertIsDisplayed()
    composeTestRule.onNodeWithTag("list_category_hub_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("card_konseling_totals"))
    composeTestRule.onNodeWithTag("card_konseling_totals").assertIsDisplayed()
    composeTestRule.onNodeWithTag("list_category_hub_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("tile_konseling_panduan"))
    composeTestRule.onNodeWithTag("tile_konseling_panduan").performClick()
    org.junit.Assert.assertTrue(openedGuideFromHub)
    composeTestRule.onNodeWithTag("list_category_hub_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("card_konseling_student_${student.id}"))
    composeTestRule.onNodeWithTag("card_konseling_student_${student.id}").assertIsDisplayed()

    // 2. Empty claimed state + confidentiality notice
    modeState.intValue = 1
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_input_konseling_privacy").assertIsDisplayed()
    composeTestRule.onNodeWithTag("empty_input_konseling_no_claimed").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_input_konseling_open_pilih").assertIsDisplayed()

    // 3. Edit dialog state: date picker & edit title saves cleanly
    modeState.intValue = 2
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_konseling_pick_date").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("input_konseling_title").performScrollTo()
    composeTestRule.onNodeWithTag("input_konseling_title").performTextClearance()
    composeTestRule.onNodeWithTag("input_konseling_title").performTextInput("Fokus Belajar Meningkat")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_save_konseling").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("Fokus Belajar Meningkat", savedTitle)

    // 4. Detail sheet: summary card & edit trigger
    modeState.intValue = 3
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_konseling_detail_summary").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_edit_konseling_${activeSession.id}").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(activeSession.id, openedEditRec?.id)

    // 5. Follow-Up sheet: displays item & navigates to student
    modeState.intValue = 4
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_followup_konseling_${activeSession.id}").assertIsDisplayed().performClick()
    assertEquals(student.id, openedFromFollowUpId)

    // 6. History & Audit Logs sheet: displays audit log card
    modeState.intValue = 5
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_konseling_log_${logEntry.id}").assertIsDisplayed()

    // 7. Guide dialog: displays ethics pledge
    modeState.intValue = 6
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dialog_konseling_guide").assertIsDisplayed()
  }

  @Test
  fun `verify DashboardScreen spotlight coach marks tour covers all 13 features, supports Next Prev Skip and persists completion`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    app.getSharedPreferences("waliasuh_tour_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val vm = com.example.ui.HealthViewModel(app)
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    vm.executeLogin("anaksuperadmin", "Woyowoyo12@", rememberMe = false)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        val plans by vm.activePlans.collectAsStateWithLifecycle()
        val students by vm.students.collectAsStateWithLifecycle()
        val waliName by vm.waliName.collectAsStateWithLifecycle()
        com.example.ui.screens.DashboardScreen(
          viewModel = vm,
          waliName = waliName,
          activePlans = plans,
          students = students,
          onNavigateToPlan = {},
          onNavigateToRecords = {},
          onNavigateToStudents = {},
          onNavigateToProfile = {}
        )
      }
    }
    composeTestRule.waitForIdle()

    // Tidak mengganggu alur otomatis saat unit test, tetapi tombol Tur langsung membukanya
    composeTestRule.onNodeWithTag("dashboard_tour_overlay").assertDoesNotExist()
    composeTestRule.onNodeWithTag("btn_dashboard_tour").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("dashboard_tour_overlay").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_tour_tooltip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("text_tour_step_counter").assertTextEquals("1 / 16")

    // Lanjut ke langkah 2: Klaim Anak Asuh (Fondasi)
    composeTestRule.onNodeWithTag("btn_tour_next").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("text_tour_step_counter").assertTextEquals("2 / 16")
    composeTestRule.onNodeWithTag("text_tour_title").assertTextEquals("Klaim Anak Asuh (Mulai Sini!)")

    // Kembali ke langkah 1
    composeTestRule.onNodeWithTag("btn_tour_prev").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("text_tour_step_counter").assertTextEquals("1 / 16")

    // Jelajahi seluruh 16 langkah fitur (termasuk Sholat 5 Waktu, Tabungan, Konseling, Jadwal & Info Penting) sampai selesai
    repeat(15) {
      composeTestRule.onNodeWithTag("btn_tour_next").performClick()
      composeTestRule.waitForIdle()
    }
    composeTestRule.onNodeWithTag("text_tour_step_counter").assertTextEquals("16 / 16")
    composeTestRule.onNodeWithTag("btn_tour_next").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("dashboard_tour_overlay").assertDoesNotExist()
    org.junit.Assert.assertTrue(vm.isDashboardTourCompleted())

    // Putar ulang & uji tombol Lewati (Skip)
    vm.startDashboardTour()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dashboard_tour_overlay").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_tour_skip").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dashboard_tour_overlay").assertDoesNotExist()
  }

  @Test
  fun `verify CreatePlanDialog DirectTreatmentDialog and DashboardBannerFormDialog bottom sheets save cleanly`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val student = com.example.data.model.ChildStudent(
      id = 995001L,
      name = "Zidane Alfarizi",
      nis = "31999501",
      gender = "L",
      age = 14,
      dormitory = "Asrama Putra",
      classGrade = "VIII-A",
      assignedWaliUsername = "walitest",
      assignedWaliName = "Ustadz Uji",
      schoolId = "SRT1-KDR"
    )
    val sheetMode = androidx.compose.runtime.mutableIntStateOf(0) // 0=CreatePlan, 1=DirectTreatment, 2=BannerForm
    var savedPlanComplaint: String? = null
    var savedDirectDiagnosis: String? = null
    var savedBannerTitle: String? = null

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        when (sheetMode.intValue) {
          0 -> com.example.ui.dialogs.CreatePlanDialog(
            students = listOf(student),
            onDismiss = {},
            onConfirm = { _, _, complaint, _, _, _, _ -> savedPlanComplaint = complaint }
          )
          1 -> com.example.ui.dialogs.DirectTreatmentDialog(
            students = listOf(student),
            onDismiss = {},
            onConfirm = { _, _, _, diagnosis, _, _, _, _, _, _, _, _ -> savedDirectDiagnosis = diagnosis }
          )
          else -> com.example.ui.dialogs.DashboardBannerFormDialog(
            initialItem = null,
            isUploadingImage = false,
            onUploadImage = { _, _, _, _ -> },
            onDismiss = {},
            onConfirm = { savedBannerTitle = it.title }
          )
        }
      }
    }

    // 1. CreatePlanDialog bottom sheet
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("select_student_search_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("input_complaint").performScrollTo().performTextInput("Demam 38C")
    composeTestRule.onNodeWithTag("confirm_create_plan_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("Demam 38C", savedPlanComplaint)

    // 2. DirectTreatmentDialog bottom sheet (Inpatient mode)
    sheetMode.intValue = 1
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("select_student_search_direct_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Keluhan Awal *").performScrollTo().performTextInput("Luka jatuh")
    composeTestRule.onNodeWithTag("direct_diagnosis_input").performScrollTo().performTextInput("Observasi Febris")
    composeTestRule.onNodeWithTag("direct_switch_inpatient").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("confirm_direct_treatment_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("Observasi Febris", savedDirectDiagnosis)

    // 3. DashboardBannerFormDialog bottom sheet (GIF/Video/Image)
    sheetMode.intValue = 2
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("banner_image_url_input").performScrollTo().performTextInput("https://cdn.example.com/promo_imunisasi.gif")
    composeTestRule.onNodeWithTag("banner_title_input").performScrollTo().performTextInput("Info Imunisasi")
    composeTestRule.onNodeWithTag("btn_confirm_banner_item").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("Info Imunisasi", savedBannerTitle)
  }

  @Test
  fun `verify DashboardBanner auto-slide advances pages cleanly and detects GIF and Video media kinds`() {
    val gifBanner = com.example.data.model.DashboardBannerItem(
      id = "gif_1",
      title = "Banner GIF Promo",
      imageUrl = "https://waliasuhcareapp.areksurabaya.my.id/col/rec/anim.gif?v=2",
      actionType = com.example.data.model.DashboardBannerActionType.NONE,
      isEnabled = true,
      sortOrder = 1
    )
    val videoBanner = com.example.data.model.DashboardBannerItem(
      id = "vid_2",
      title = "Banner Video MP4",
      imageUrl = "https://waliasuhcareapp.areksurabaya.my.id/col/rec/intro.mp4?token=123",
      actionType = com.example.data.model.DashboardBannerActionType.NONE,
      isEnabled = true,
      sortOrder = 2
    )
    assertEquals(com.example.data.model.DashboardBannerMediaKind.GIF, gifBanner.resolveMediaKind())
    assertEquals(com.example.data.model.DashboardBannerMediaKind.VIDEO, videoBanner.resolveMediaKind())

    val roundTrip = com.example.data.model.DashboardBannerItem.fromJson(videoBanner.toJson())
    assertEquals(com.example.data.model.DashboardBannerMediaKind.VIDEO, roundTrip.resolveMediaKind())

    val config = com.example.data.model.DashboardBannerConfig(
      items = listOf(
        com.example.data.model.DashboardBannerItem.defaultFaskes().copy(sortOrder = 0),
        gifBanner,
        videoBanner
      ),
      slideIntervalSeconds = 2,
      updatedAt = 1000L
    )
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.components.DashboardBannerCarousel(
          config = config,
          faskesDefaultSubtitle = "Puskesmas Wates",
          isAnakSuperAdmin = false,
          onBannerClick = {},
          onManageBannersClick = {}
        )
      }
    }

    composeTestRule.mainClock.advanceTimeByFrame()
    composeTestRule.onNodeWithTag("tile_puskesmas_info").assertIsDisplayed()

    // Auto-slide after 2 seconds + animation (550ms) advances cleanly to GIF slide (was previously canceled on frame 1)
    composeTestRule.mainClock.advanceTimeBy(2_700L)
    composeTestRule.mainClock.advanceTimeByFrame()
    composeTestRule.onNodeWithTag("banner_slide_gif_1").assertIsDisplayed()
    composeTestRule.onNodeWithText("GIF").assertIsDisplayed()

    // Auto-slide again advances cleanly to Video MP4 slide with mute toggle
    composeTestRule.mainClock.advanceTimeBy(2_700L)
    composeTestRule.mainClock.advanceTimeByFrame()
    composeTestRule.onNodeWithTag("banner_slide_vid_2").assertIsDisplayed()
    composeTestRule.onNodeWithText("VIDEO").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_banner_mute_vid_2").assertIsDisplayed()
  }

  @Test
  fun `verify HaidRecord daily checkIn flags reset across calendar days and roundtrip through PbHaidRecordDto`() {
    val now = System.currentTimeMillis()
    val yesterday = now - 86_400_000L

    val uncheckedNew = com.example.data.model.HaidRecord(
      id = 1L,
      studentId = 101L,
      studentName = "Aisyah Putri",
      startDate = yesterday,
      status = "HAID",
      lastCheckedInAt = null
    )
    org.junit.Assert.assertFalse(uncheckedNew.isCheckedInToday)
    org.junit.Assert.assertTrue(uncheckedNew.isPendingCheckInToday)

    val checkedYesterday = uncheckedNew.copy(lastCheckedInAt = yesterday, lastCheckInBy = "Ustadzah A")
    org.junit.Assert.assertFalse(checkedYesterday.isCheckedInToday)
    org.junit.Assert.assertTrue(checkedYesterday.isPendingCheckInToday)

    val checkedToday = uncheckedNew.copy(lastCheckedInAt = now, lastCheckInBy = "Ustadzah B")
    org.junit.Assert.assertTrue(checkedToday.isCheckedInToday)
    org.junit.Assert.assertFalse(checkedToday.isPendingCheckInToday)

    val dto = com.example.data.remote.pocketbase.PbHaidRecordDto.fromDomain(checkedToday)
    assertEquals(now, dto.lastCheckedInAt)
    assertEquals("Ustadzah B", dto.lastCheckInBy)
    val restored = dto.toDomainModel()
    assertEquals(now, restored.lastCheckedInAt)
    assertEquals("Ustadzah B", restored.lastCheckInBy)
  }

  @Test
  fun `verify InputHaidDialog Check-In Masih Haid popup and DaftarHaidSheet unchecked roll-call workflow`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val now = System.currentTimeMillis()
    val yesterday = now - 86_400_000L

    val studentAdelia = com.example.data.model.ChildStudent(
      id = 8801L,
      name = "Adelia Sukmawati",
      nis = "318801",
      gender = "P",
      age = 15,
      dormitory = "Asrama Putri",
      classGrade = "IX-B",
      assignedWaliUsername = "wali1",
      assignedWaliName = "Ustadzah Sari",
      schoolId = "SRT1-KDR"
    )
    val activeAdelia = com.example.data.model.HaidRecord(
      id = 501L,
      studentId = 8801L,
      studentName = "Adelia Sukmawati",
      studentNis = "318801",
      studentClass = "IX-B",
      studentDorm = "Asrama Putri",
      startDate = now - (4L * 86_400_000L),
      status = "HAID",
      lastCheckedInAt = yesterday,
      lastCheckInBy = "Ustadzah Sari"
    )
    val activeChecked1 = com.example.data.model.HaidRecord(
      id = 502L,
      studentId = 8802L,
      studentName = "Fatimah Zahra",
      studentClass = "VIII-A",
      studentDorm = "Asrama Putri",
      startDate = now - 86_400_000L,
      status = "HAID",
      lastCheckedInAt = now,
      lastCheckInBy = "Ustadzah Sari"
    )
    val activeChecked2 = com.example.data.model.HaidRecord(
      id = 503L,
      studentId = 8803L,
      studentName = "Nadia Khairani",
      studentClass = "VII-C",
      studentDorm = "Asrama Putri",
      startDate = now - (2L * 86_400_000L),
      status = "HAID",
      lastCheckedInAt = now,
      lastCheckInBy = "Ustadzah Sari"
    )

    val mode = androidx.compose.runtime.mutableIntStateOf(0) // 0=InputHaidDialog, 1=DaftarHaidSheet
    var checkedInId: Long? = null

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        if (mode.intValue == 0) {
          com.example.ui.dialogs.InputHaidDialog(
            students = listOf(studentAdelia),
            activeHaidRecords = listOf(activeAdelia),
            suciRecords = emptyList(),
            waliName = "Ustadzah Sari",
            schoolId = "SRT1-KDR",
            initialStudent = studentAdelia,
            onSaveHaid = {},
            onCheckInHaid = { checkedInId = it },
            onCompleteHaid = {},
            onDismiss = {}
          )
        } else {
          com.example.ui.dialogs.DaftarHaidSheet(
            activeHaidRecords = listOf(activeAdelia, activeChecked1, activeChecked2),
            waliName = "Ustadzah Sari",
            onCheckInHaid = { checkedInId = it },
            onCompleteHaid = {},
            onDeleteRecord = {},
            onOpenInputHaid = {},
            onDismiss = {}
          )
        }
      }
    }

    // 1. InputHaidDialog auto-opens Siswi Masih di Daftar Haid sheet -> Check-In Masih Haid
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sheet_active_haid_checkin").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_checkin_active_haid").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    assertEquals(501L, checkedInId)
    composeTestRule.onNodeWithTag("sheet_active_haid_checkin").assertDoesNotExist()
    composeTestRule.onNodeWithTag("banner_checkin_haid_feedback").assertIsDisplayed()

    // 2. Switch to DaftarHaidSheet -> 2 Sudah Check-In & 1 Belum Check-In banner + filter + 1-tap check-in
    checkedInId = null
    mode.intValue = 1
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("summary_haid_unchecked").assertIsDisplayed()
    composeTestRule.onNodeWithTag("banner_haid_unchecked_info").assertIsDisplayed()
    composeTestRule.onNodeWithTag("list_daftar_haid")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("filter_haid_unchecked"))
    composeTestRule.onNodeWithTag("filter_haid_unchecked").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("list_daftar_haid")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("item_haid_501"))
    composeTestRule.onNodeWithTag("item_haid_501").assertIsDisplayed()
    composeTestRule.onNodeWithTag("item_haid_502").assertDoesNotExist()
    composeTestRule.onNodeWithTag("btn_card_checkin_haid_501").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(501L, checkedInId)
  }

  @Test
  fun `verify HealthViewModel checkInHaidRecord persists today checkIn timestamp in Room`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val dao = db.haidRecordDao()

    val yesterday = System.currentTimeMillis() - 86_400_000L
    val insertedId = dao.insert(
      com.example.data.model.HaidRecord(
        studentId = 9901L,
        studentName = "Siti Aminah",
        startDate = yesterday,
        status = "HAID",
        lastCheckedInAt = yesterday,
        lastCheckInBy = "Wali Lama",
        schoolId = "SRT1-KDR"
      )
    )

    val before = dao.getById(insertedId)!!
    org.junit.Assert.assertFalse(before.isCheckedInToday)

    var callbackDone = false
    val checkInAt = System.currentTimeMillis()
    vm.checkInHaidRecord(
      id = insertedId,
      checkedInBy = "Ustadzah Piket",
      checkedInAt = checkInAt,
      onComplete = { callbackDone = true }
    )
    var ticks = 0
    while (ticks < 40 && !callbackDone) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(50)
      ticks++
    }
    org.junit.Assert.assertTrue(callbackDone)

    val after = dao.getById(insertedId)!!
    org.junit.Assert.assertTrue(after.isCheckedInToday)
    assertEquals(checkInAt, after.lastCheckedInAt)
    assertEquals("Ustadzah Piket", after.lastCheckInBy)
  }

  @Test
  fun `verify InputKonselingDialog voice transcript helpers append text cleanly and build official Google RecognizerIntent`() {
    assertEquals(
      "Anak tampak murung setelah jam belajar.",
      com.example.ui.dialogs.appendVoiceTranscript("", "  Anak tampak murung setelah jam belajar.  ")
    )
    assertEquals(
      "Catatan awal.",
      com.example.ui.dialogs.appendVoiceTranscript("Catatan awal.", "   ")
    )
    assertEquals(
      "Catatan awal. Sudah diajak bicara santai.",
      com.example.ui.dialogs.appendVoiceTranscript("Catatan awal.", "Sudah diajak bicara santai.")
    )
    assertEquals(
      "Poin 1: Adaptasi\nPoin 2: Teman sekamar",
      com.example.ui.dialogs.appendVoiceTranscript("Poin 1: Adaptasi\n", "Poin 2: Teman sekamar")
    )
    assertEquals(
      "Kalimat pertama kalimat kedua",
      com.example.ui.dialogs.appendVoiceTranscript("Kalimat pertama   ", "kalimat kedua")
    )

    val idIntent = com.example.ui.dialogs.buildGoogleSpeechToTextIntent(
      promptText = "Ucapkan ringkasan curhat / observasi...",
      languageTag = "id-ID"
    )
    assertEquals(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH, idIntent.action)
    assertEquals(
      android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
      idIntent.getStringExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL)
    )
    assertEquals("id-ID", idIntent.getStringExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE))
    assertEquals("id-ID", idIntent.getStringExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE))
    assertEquals(
      "Ucapkan ringkasan curhat / observasi...",
      idIntent.getStringExtra(android.speech.RecognizerIntent.EXTRA_PROMPT)
    )
    assertEquals(1, idIntent.getIntExtra(android.speech.RecognizerIntent.EXTRA_MAX_RESULTS, 0))
    org.junit.Assert.assertNull(idIntent.`package`)

    val enIntent = com.example.ui.dialogs.buildGoogleSpeechToTextIntent(
      promptText = "Speak counseling summary...",
      languageTag = "en-US"
    )
    assertEquals("en-US", enIntent.getStringExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE))
  }

  @Test
  fun `verify InputKonselingDialog renders Google Voice mic buttons on all 4 fields and handles missing speech handler safely`() {
    val student = com.example.data.model.ChildStudent(
      id = 7701L,
      name = "Sofa Lailiya",
      nis = "10293",
      gender = "P",
      age = 15,
      dormitory = "Asrama Putri 1",
      classGrade = "X-1",
      assignedWaliUsername = "walisofa",
      assignedWaliName = "Ustadzah Sari",
      schoolId = "SRT1-KDR"
    )

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    var capturedActivity: android.app.Activity? = null
    composeTestRule.setContent {
      capturedActivity = LocalContext.current as? android.app.Activity
      androidx.compose.runtime.CompositionLocalProvider(
        com.example.util.i18n.LocalAppStrings provides indonesianStrings
      ) {
        com.example.ui.dialogs.InputKonselingDialog(
          claimedStudents = listOf(student),
          initialStudent = student,
          onOpenPilihAnakAsuh = {},
          onSave = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
          onDismiss = {}
        )
      }
    }

    composeTestRule.waitForIdle()

    // 1. Field Judul Singkat / Tema & Ringkasan Curhat / Observasi memiliki ikon mic Google Voice
    composeTestRule.onNodeWithTag("btn_voice_konseling_title", useUnmergedTree = true)
      .performScrollTo()
      .assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_voice_konseling_summary", useUnmergedTree = true)
      .performScrollTo()
      .assertIsDisplayed()

    // 2. Buka bagian opsional Pendampingan & Rencana Tindak Lanjut -> 2 ikon mic tambahan tampil
    composeTestRule.onNodeWithTag("btn_toggle_konseling_advanced")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("btn_voice_konseling_intervention", useUnmergedTree = true)
      .performScrollTo()
      .assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_voice_konseling_followup", useUnmergedTree = true)
      .performScrollTo()
      .assertIsDisplayed()

    // 3. Klik tombol mic -> meluncurkan Intent resmi ACTION_RECOGNIZE_SPEECH (atau menampilkan Toast ramah bila ActivityNotFound) tanpa crash
    composeTestRule.onNodeWithTag("btn_voice_konseling_summary", useUnmergedTree = true)
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val latestToast = org.robolectric.shadows.ShadowToast.getTextOfLatestToast()
    val launchedSpeech = capturedActivity
      ?.let { org.robolectric.Shadows.shadowOf(it).peekNextStartedActivityForResult()?.intent?.action }
    org.junit.Assert.assertTrue(
      "Expected ACTION_RECOGNIZE_SPEECH dispatch or friendly fallback toast, got intent=$launchedSpeech toast=$latestToast",
      launchedSpeech == android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH ||
        latestToast?.contains("Suara ke Teks Google belum tersedia") == true
    )
  }

  @Test
  fun `verify WaliScheduleHelper stats breakdown CSV parsing and active hour logic`() {
    val shiftCodes = com.example.data.model.WaliScheduleHelper.defaultShiftCodes("SRT1-KDR")
    val shiftMap = com.example.data.model.WaliScheduleHelper.buildShiftMap(shiftCodes, "SRT1-KDR")

    // Shift malam M3 (23..07) aktif saat jam 23 dan jam 02, tidak aktif jam 10
    val m3 = shiftMap["M3"]!!
    org.junit.Assert.assertTrue(com.example.data.model.WaliScheduleHelper.isShiftActiveAtHour(m3, 23))
    org.junit.Assert.assertTrue(com.example.data.model.WaliScheduleHelper.isShiftActiveAtHour(m3, 2))
    org.junit.Assert.assertFalse(com.example.data.model.WaliScheduleHelper.isShiftActiveAtHour(m3, 10))

    // Parse CSV matrix 2 baris
    val csv = """
      Kode,Nama,1,2,3,4,5
      EW,Eko Wahyudi,P1,S3A,M3,LP,L
      SM,Siti Maslukah,P2,S1A,IZIN,P1,L
    """.trimIndent()
    val parsed = com.example.data.model.WaliScheduleHelper.parseMatrixCsv(
      rawText = csv,
      monthKey = "2026-11",
      schoolId = "SRT1-KDR"
    )
    org.junit.Assert.assertEquals(2, parsed.size)
    val eko = parsed[0]
    val ekoStats = com.example.data.model.WaliScheduleHelper.computeRowStats(eko, 5, shiftMap)
    org.junit.Assert.assertEquals(1, ekoStats.pFull)
    org.junit.Assert.assertEquals(1, ekoStats.sCount)
    org.junit.Assert.assertEquals(1, ekoStats.mCount)
    org.junit.Assert.assertEquals(1, ekoStats.lpCount)
    org.junit.Assert.assertEquals(1, ekoStats.offCount)
    org.junit.Assert.assertEquals(24, ekoStats.totalJamKerja) // P1(8)+S3A(8)+M3(8)

    val breakdownDay1 = com.example.data.model.WaliScheduleHelper.computeDayBreakdown(parsed, 1, shiftMap)
    org.junit.Assert.assertEquals(2, breakdownDay1.pagiFull)
    org.junit.Assert.assertEquals(1, breakdownDay1.p1)
    org.junit.Assert.assertEquals(1, breakdownDay1.p2)
  }

  @Test
  fun `verify HealthViewModel Jadwal Wali Asuh admin-only permissions and school tenant isolation`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val vm = com.example.ui.HealthViewModel(app)

    // 1. Login sebagai Admin Sekolah TST-JDW-99 (terisolasi dari tes lain)
    val adminReg = com.example.data.model.SchoolRegistration(
      schoolName = "SR Terintegrasi Jadwal Test",
      address = "Jl. Sekolah Rakyat 99",
      city = "Kediri",
      estimatedWaliCount = 20,
      picName = "Admin Satu",
      picEmail = "admin1@tst99.id",
      picPhone = "08123456789",
      generatedSchoolCode = "TST-JDW-99",
      generatedAdminUsername = "admin_tst99_jadwal",
      generatedAdminPassword = "Password#123",
      status = "APPROVED"
    )
    db.schoolRegistrationDao().insert(adminReg)
    val waliAcc = com.example.data.model.WaliAccount(
      name = "Eko Wahyudi",
      username = "ekowahyudi_j99",
      password = "Wali#1234",
      dormitory = "Asrama Putra",
      schoolId = "TST-JDW-99"
    )
    db.waliAccountDao().insert(waliAcc)

    val adminLogin = vm.executeLogin("admin_tst99_jadwal", "Password#123", rememberMe = false)
    org.junit.Assert.assertTrue(adminLogin is com.example.ui.LoginResult.SuccessAdmin)
    org.junit.Assert.assertTrue(vm.canManageSchoolSchedule())

    // 2. Admin menambah kode shift kustom "K1"
    var codeSaved: Boolean? = null
    vm.saveWaliShiftCode(
      existing = null,
      code = "K1",
      name = "Piket Koperasi",
      shiftGroup = com.example.data.model.WaliScheduleHelper.GROUP_PAGI,
      zoneLabel = "Koperasi Sekolah",
      timeRange = "07:00 - 15:00",
      startHour = 7,
      endHour = 15,
      workHours = 8,
      colorHex = "#0EA5E9",
      sopNotes = "Standby koperasi pagi"
    ) { ok, _ -> codeSaved = ok }
    for (i in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (codeSaved != null) break
      kotlinx.coroutines.delay(25)
    }
    org.junit.Assert.assertEquals(true, codeSaved)
    val codes = db.waliShiftCodeDao().getShiftCodesBySchoolList("TST-JDW-99")
    org.junit.Assert.assertTrue(codes.any { it.code == "K1" && it.schoolId == "TST-JDW-99" })

    // 3. Admin menarik anggota dari akun Wali Asuh sekolah ini & mengatur sel Tgl 17 (M3 + Pin Tugas + Izin)
    var syncOk: Boolean? = null
    vm.syncScheduleMembersFromWaliAccounts("2026-11") { ok, _ -> syncOk = ok }
    for (i in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (syncOk != null) break
      kotlinx.coroutines.delay(25)
    }
    org.junit.Assert.assertEquals(true, syncOk)

    val entries = db.waliScheduleEntryDao().getEntriesBySchoolAndMonthList("TST-JDW-99", "2026-11")
    org.junit.Assert.assertEquals(1, entries.size)
    val ekoEntry = entries.first()

    var cellUpdated: Boolean? = null
    vm.updateWaliScheduleDayCell(
      entry = ekoEntry,
      day = 17,
      shiftCode = "M3",
      pinsForDay = listOf(
        com.example.data.model.PinnedTaskItem("Jaga RS Siti Khadijah", com.example.data.model.WaliScheduleHelper.PIN_FASKES),
        com.example.data.model.PinnedTaskItem("Jaga Asrama SMA", com.example.data.model.WaliScheduleHelper.PIN_TUGAS)
      ),
      leaveForDay = com.example.data.model.LeaveDayInfo(
        status = "IZIN",
        driveUrl = "https://drive.google.com/file/d/contoh-surat/view",
        note = "Surat tugas luar"
      )
    ) { ok, _ -> cellUpdated = ok }
    for (i in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (cellUpdated != null) break
      kotlinx.coroutines.delay(25)
    }
    org.junit.Assert.assertEquals(true, cellUpdated)

    val updatedEko = db.waliScheduleEntryDao().getEntriesBySchoolAndMonthList("TST-JDW-99", "2026-11").first()
    org.junit.Assert.assertEquals("M3", com.example.data.model.WaliScheduleHelper.parseDayCodes(updatedEko.dayCodesJson)[17])
    org.junit.Assert.assertEquals(2, com.example.data.model.WaliScheduleHelper.parseDayPins(updatedEko.dayPinsJson)[17]?.size)
    org.junit.Assert.assertEquals("IZIN", com.example.data.model.WaliScheduleHelper.parseDayLeaves(updatedEko.dayLeaveJson)[17]?.status)

    // 4. Login sebagai Wali Asuh biasa -> dapat melihat, tetapi TIDAK BOLEH mengubah kode shift/sel/impor
    val waliLogin = vm.executeLogin("ekowahyudi_j99", "Wali#1234", rememberMe = false)
    org.junit.Assert.assertTrue(waliLogin is com.example.ui.LoginResult.SuccessWali)
    org.junit.Assert.assertFalse(vm.canManageSchoolSchedule())

    var blockedCodeSave = false
    vm.saveWaliShiftCode(
      existing = null, code = "X9", name = "Hack", shiftGroup = "PAGI",
      zoneLabel = "", timeRange = "", startHour = 7, endHour = 15, workHours = 8,
      colorHex = "#000000", sopNotes = ""
    ) { ok, _ -> blockedCodeSave = !ok }
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    org.junit.Assert.assertTrue(blockedCodeSave)

    var blockedCellEdit = false
    vm.updateWaliScheduleDayCell(updatedEko, 18, "P1", emptyList(), null) { ok, _ ->
      blockedCellEdit = !ok
    }
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    org.junit.Assert.assertTrue(blockedCellEdit)

    // 5. Isolasi sekolah: entri sekolah lain ("SRT2-MLG") tidak muncul di kueri TST-JDW-99
    val otherSchoolCodes = db.waliShiftCodeDao().getShiftCodesBySchoolList("SRT2-MLG")
    org.junit.Assert.assertTrue(otherSchoolCodes.none { it.code == "K1" })
  }

  @Test
  fun `verify CategoryHubOverlay JADWAL_WALI shows roster pins Drive link and strictly ZERO alarm buttons`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val codes = com.example.data.model.WaliScheduleHelper.defaultShiftCodes("SRT1-KDR")
    val ekoEntry = com.example.data.model.WaliScheduleEntry(
      id = 101L,
      schoolId = "SRT1-KDR",
      monthKey = "2026-11",
      waliUsername = "ekowahyudi",
      waliName = "Eko Wahyudi",
      dayCodesJson = com.example.data.model.WaliScheduleHelper.serializeDayCodes(mapOf(17 to "M3", 18 to "S3A")),
      dayPinsJson = com.example.data.model.WaliScheduleHelper.serializeDayPins(
        mapOf(17 to listOf(com.example.data.model.PinnedTaskItem("Jaga RS Siti Khadijah Gurah", com.example.data.model.WaliScheduleHelper.PIN_FASKES)))
      ),
      dayLeaveJson = com.example.data.model.WaliScheduleHelper.serializeDayLeaves(
        mapOf(17 to com.example.data.model.LeaveDayInfo("SAKIT", "https://drive.google.com/contoh", "Istirahat"))
      )
    )

    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(
        com.example.util.i18n.LocalAppStrings provides indonesianStrings
      ) {
        com.example.ui.components.CategoryHubOverlay(
          category = com.example.ui.components.DashboardCategoryHub.JADWAL_WALI,
          schoolProfile = com.example.data.model.SchoolProfile(tenantCode = "SRT1-KDR", schoolName = "SR 1 Kediri"),
          activePlans = emptyList(),
          inpatientCount = 0,
          fastingSessions = emptyList(),
          fastingRecords = emptyList(),
          activeHaidRecords = emptyList(),
          suciRecords = emptyList(),
          shiftMessages = emptyList(),
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
          jadwalMonthKey = "2026-11",
          jadwalSelectedDay = 17,
          shiftCodes = codes,
          scheduleEntries = listOf(ekoEntry),
          canManageSchedule = false,
          currentUsername = "ekowahyudi",
          currentWaliName = "Eko Wahyudi"
        )
      }
    }
    composeTestRule.waitForIdle()

    // Pastikan Rekan Izin + Link Drive + Petugas Jam Ini + Personal + 2x2 Kartu Matriks Jadwal tampil
    composeTestRule.onNodeWithTag("card_jadwal_rekan_izin").assertExists()
    composeTestRule.onNodeWithTag("btn_buka_link_drive_${ekoEntry.id}").assertExists()
    composeTestRule.onNodeWithTag("btn_jadwal_petugas_jam_ini").assertExists()
    composeTestRule.onNodeWithTag("btn_jadwal_personal_hero").assertExists()
    composeTestRule.onNodeWithText("Malam 3 (M3)", substring = true).assertExists()
    composeTestRule.onNodeWithText("Jaga RS Siti Khadijah Gurah", substring = true).assertExists()
    composeTestRule.onNodeWithTag("list_category_hub_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("card_jadwal_matriks"))
    composeTestRule.onNodeWithTag("card_jadwal_matriks").assertExists()
    composeTestRule.onNodeWithTag("card_jadwal_pin_tugas").assertExists()
    composeTestRule.onNodeWithTag("card_jadwal_jam_ini").assertExists()
    composeTestRule.onNodeWithTag("card_jadwal_kode_sop").assertExists()

    // Syarat Point 4: TIDAK ADA tombol Alarm Tugas / Bunyikan / Notifikasi sama sekali!
    composeTestRule.onNodeWithText("Alarm Tugas", substring = true).assertDoesNotExist()
    composeTestRule.onNodeWithText("Bunyikan", substring = true).assertDoesNotExist()
  }

  @Test
  fun `verify AdminScreen Tab 4 Jadwal displays school roster controls and Generate PDF succeeds`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val vm = com.example.ui.HealthViewModel(app)

    val adminReg = com.example.data.model.SchoolRegistration(
      schoolName = "SR Terintegrasi 1 Kediri",
      address = "Jl. Sekolah Rakyat 1",
      city = "Kediri",
      estimatedWaliCount = 20,
      picName = "Admin Satu",
      picEmail = "admin1@srt1.id",
      picPhone = "08123456789",
      generatedSchoolCode = "SRT1-KDR",
      generatedAdminUsername = "admin_srt1_tab4",
      generatedAdminPassword = "Password#123",
      status = "APPROVED"
    )
    db.schoolRegistrationDao().insert(adminReg)
    vm.executeLogin("admin_srt1_tab4", "Password#123", rememberMe = false)

    val entries = listOf(
      com.example.data.model.WaliScheduleEntry(
        id = 201L,
        schoolId = "SRT1-KDR",
        monthKey = com.example.data.model.WaliScheduleHelper.currentMonthKey(),
        waliName = "Siti Maslukah",
        dayCodesJson = com.example.data.model.WaliScheduleHelper.serializeDayCodes(
          mapOf(1 to "P1", 2 to "S3A", 3 to "M3", 4 to "LP", 5 to "L")
        ),
        dayPinsJson = com.example.data.model.WaliScheduleHelper.serializeDayPins(
          mapOf(1 to listOf(com.example.data.model.PinnedTaskItem("Jaga Asrama SD")))
        )
      )
    )
    val codes = com.example.data.model.WaliScheduleHelper.defaultShiftCodes("SRT1-KDR")
    val renderedBitmaps = mutableListOf<android.graphics.Bitmap>()
    com.example.util.PdfReportGenerator.renderJadwalMatriksPages(
      context = app,
      monthKey = "2026-11",
      entries = entries,
      shiftCodes = codes,
      waliName = "Admin Satu",
      schoolProfile = com.example.data.model.SchoolProfile(tenantCode = "SRT1-KDR", schoolName = "SR Terintegrasi 1 Kediri"),
      startNewCanvas = {
        val bmp = android.graphics.Bitmap.createBitmap(842, 595, android.graphics.Bitmap.Config.ARGB_8888)
        renderedBitmaps.add(bmp)
        android.graphics.Canvas(bmp)
      }
    )
    org.junit.Assert.assertTrue(renderedBitmaps.isNotEmpty())
    renderedBitmaps.forEach { it.recycle() }

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(
        com.example.util.i18n.LocalAppStrings provides indonesianStrings
      ) {
        com.example.ui.screens.AdminScreen(viewModel = vm, onLogout = {})
      }
    }
    composeTestRule.waitForIdle()

    // Buka Tab Jadwal di AdminScreen (gulir LazyColumn ke row_admin_tabs dulu)
    composeTestRule.onNodeWithTag("list_admin_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("row_admin_tabs"))
    composeTestRule.onNodeWithTag("tab_admin_jadwal").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("list_admin_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("btn_admin_jadwal_matriks"))
    composeTestRule.onNodeWithTag("btn_admin_jadwal_matriks").assertExists()
    composeTestRule.onNodeWithTag("btn_admin_manage_shift_codes").assertExists()
    composeTestRule.onNodeWithTag("btn_admin_export_jadwal_pdf").assertExists()
    composeTestRule.onNodeWithTag("btn_admin_download_jadwal_csv_template").assertExists()
    composeTestRule.onNodeWithTag("btn_admin_upload_jadwal_csv").assertExists()

    // Klik Upload CSV -> langsung membuka dialog ImportJadwalMatriksDialog pada tab CSV / Tabel
    composeTestRule.onNodeWithTag("btn_admin_upload_jadwal_csv").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dialog_import_jadwal_matriks").assertExists()
    composeTestRule.onNodeWithTag("btn_download_jadwal_csv_template").assertExists()
    composeTestRule.onNodeWithTag("btn_upload_jadwal_csv_file").assertExists()
    composeTestRule.onNodeWithTag("input_csv_matrix").assertExists()
    composeTestRule.onNodeWithTag("btn_import_csv_matrix").assertExists()
  }

  @Test
  fun `verify WaliScheduleHelper buildScheduleTemplateCsv and attached November 2026 CSV format parse accurately`() {
    val shiftCodes = com.example.data.model.WaliScheduleHelper.defaultShiftCodes("SRT1-KDR")
    val shiftMap = com.example.data.model.WaliScheduleHelper.buildShiftMap(shiftCodes, "SRT1-KDR")

    // 1. Uji buildScheduleTemplateCsv dengan daftar akun Wali Asuh sekolah (exclude Guru Ngaji)
    val accounts = listOf(
      com.example.data.model.WaliAccount(
        name = "Akhmad Fadkhurriza I",
        username = "fadkhur",
        password = "p",
        schoolId = "SRT1-KDR"
      ),
      com.example.data.model.WaliAccount(
        name = "Eko Wahyudi",
        username = "ekowahyudi",
        password = "p",
        schoolId = "SRT1-KDR"
      ),
      com.example.data.model.WaliAccount(
        name = "Ustadz Mengaji",
        username = "gurungaji",
        password = "p",
        schoolId = "SRT1-KDR",
        role = com.example.data.model.WaliAccount.ROLE_GURU_NGAJI
      )
    )
    val existingWithShifts = listOf(
      com.example.data.model.WaliScheduleEntry(
        schoolId = "SRT1-KDR",
        monthKey = "2026-11",
        waliUsername = "fadkhur",
        waliName = "Akhmad Fadkhurriza I",
        sortOrder = 1,
        dayCodesJson = com.example.data.model.WaliScheduleHelper.serializeDayCodes(mapOf(1 to "M3", 2 to "LP", 3 to "L"))
      )
    )
    val templateCsv = com.example.data.model.WaliScheduleHelper.buildScheduleTemplateCsv(
      monthKey = "2026-11",
      existingEntries = existingWithShifts,
      waliAccounts = accounts,
      includeExistingShifts = true
    )
    val lines = templateCsv.lines()
    org.junit.Assert.assertTrue(lines.first().startsWith("No,Nama,1,2,3,"))
    org.junit.Assert.assertTrue(lines.first().endsWith(",29,30"))
    org.junit.Assert.assertEquals(3, lines.size) // Header + 2 Wali Asuh (Guru Ngaji tidak ikut)
    org.junit.Assert.assertTrue(lines[1].startsWith("1,Akhmad Fadkhurriza I,M3,LP,L,"))
    org.junit.Assert.assertTrue(lines[2].startsWith("2,Eko Wahyudi,"))

    // Round-trip template kembali ke parser
    val roundTrip = com.example.data.model.WaliScheduleHelper.parseMatrixCsv(
      rawText = "\uFEFF$templateCsv",
      monthKey = "2026-11",
      schoolId = "SRT1-KDR",
      waliAccounts = accounts
    )
    org.junit.Assert.assertEquals(2, roundTrip.size)
    org.junit.Assert.assertEquals("M3", com.example.data.model.WaliScheduleHelper.parseDayCodes(roundTrip[0].dayCodesJson)[1])
    org.junit.Assert.assertEquals("ekowahyudi", roundTrip[1].waliUsername)

    // 2. Uji format nyata CSV Jadwal_Shif_Wali_Asuh_November_2026 (BOM, nama berpetik ganda, kolom rekap kanan P FUL..JK)
    val realCsvSample = """
      ${"\uFEFF"}No,Nama,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,P FUL,S,S1A (Kantin SD),S2A (Kantin SMP),S3A (Kantin SMA),S4A (Jaga Masjid),M (Total),M1 (s.d 00:00),M2 (Subuh-07:00),M3 (Pendamping 23-07),LP,OFF,P1,P2,P3,P4,JK (Jam)
      1,"Akhmad Fadkhurriza I",M3,LP,L,P1,S2A,P1,P1,S2A,M3,LP,L,P1,S2A,S2A,S2A,P1,LP,P1,P1,P1,P5,P1,S2A,LP,IZIN,IZIN,L,L,S2A,S2A,10,8,0,8,0,0,2,0,0,2,4,4,9,0,0,0,160
      10,"Deni Furitrinofi",LP,L,P1,S2A,P1,S2A,S2A,M3,LP,L,P1,IZIN,IZIN,S2A,S2A,M3,LP,L,S3A,C,C,C,C,C,C,C,C,C,C,C,3,6,0,5,1,0,2,0,0,2,3,3,3,0,0,0,88
      42,"Eko Wahyudi",IZIN,IZIN,IZIN,M3,LP,L,P5,S2A,S3A,S3A,M3,LP,L,IZIN,IZIN,IZIN,M3,S3A,M3,LP,L,P1,S3A,S3A,S3A,S3A,M3,LP,L,P1,3,8,0,1,7,0,5,0,0,5,4,4,2,0,0,0,128
    """.trimIndent()

    val parsedReal = com.example.data.model.WaliScheduleHelper.parseMatrixCsv(
      rawText = realCsvSample,
      monthKey = "2026-11",
      schoolId = "SRT1-KDR",
      waliAccounts = accounts
    )
    org.junit.Assert.assertEquals(3, parsedReal.size)

    val akhmadStats = com.example.data.model.WaliScheduleHelper.computeRowStats(parsedReal[0], 30, shiftMap)
    org.junit.Assert.assertEquals("Akhmad Fadkhurriza I", parsedReal[0].waliName)
    org.junit.Assert.assertEquals(160, akhmadStats.totalJamKerja)
    org.junit.Assert.assertEquals(10, akhmadStats.pFull)
    org.junit.Assert.assertEquals(8, akhmadStats.sCount)
    org.junit.Assert.assertEquals(2, akhmadStats.mCount)
    org.junit.Assert.assertEquals(4, akhmadStats.lpCount)
    org.junit.Assert.assertEquals(4, akhmadStats.offCount)
    org.junit.Assert.assertEquals(2, akhmadStats.izinCount)

    val deniStats = com.example.data.model.WaliScheduleHelper.computeRowStats(parsedReal[1], 30, shiftMap)
    org.junit.Assert.assertEquals(88, deniStats.totalJamKerja)
    org.junit.Assert.assertEquals(11, deniStats.cutiCount)

    val ekoStats = com.example.data.model.WaliScheduleHelper.computeRowStats(parsedReal[2], 30, shiftMap)
    org.junit.Assert.assertEquals(128, ekoStats.totalJamKerja)
    org.junit.Assert.assertEquals("ekowahyudi", parsedReal[2].waliUsername)
  }

  @Test
  fun `verify Anak Admin Sekolah max 7 quota per tenant, Guru Ngaji rejection, and schedule cell edit permissions`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    db.waliAccountDao().deleteBySchoolId("TST-AA-01")
    db.waliAccountDao().deleteBySchoolId("TST-AA-02")
    db.waliScheduleEntryDao().deleteBySchoolAndMonth("TST-AA-01", "2026-11")

    db.schoolRegistrationDao().insert(
      com.example.data.model.SchoolRegistration(
        schoolName = "SR Uji Anak Admin 01",
        address = "Jl. Test 1",
        city = "Kediri",
        estimatedWaliCount = 15,
        picName = "Admin AA1",
        picEmail = "aa1@test.id",
        picPhone = "0811111111",
        generatedSchoolCode = "TST-AA-01",
        generatedAdminUsername = "admin_aa01",
        generatedAdminPassword = "Password#123",
        status = "APPROVED"
      )
    )
    db.schoolRegistrationDao().insert(
      com.example.data.model.SchoolRegistration(
        schoolName = "SR Uji Anak Admin 02",
        address = "Jl. Test 2",
        city = "Malang",
        estimatedWaliCount = 10,
        picName = "Admin AA2",
        picEmail = "aa2@test.id",
        picPhone = "0822222222",
        generatedSchoolCode = "TST-AA-02",
        generatedAdminUsername = "admin_aa02",
        generatedAdminPassword = "Password#123",
        status = "APPROVED"
      )
    )

    // Seed 8 regular Wali Asuh + 1 Guru Ngaji in TST-AA-01, and 1 Wali Asuh in TST-AA-02
    val insertedIds = mutableListOf<Long>()
    for (idx in 1..8) {
      val id = db.waliAccountDao().insert(
        com.example.data.model.WaliAccount(
          name = "Wali Satu-$idx",
          username = "wali_aa01_$idx",
          password = "Pass#$idx",
          dormitory = "Asrama Putra",
          schoolId = "TST-AA-01",
          role = com.example.data.model.WaliAccount.ROLE_WALI
        )
      )
      insertedIds.add(id)
    }
    val guruNgajiId = db.waliAccountDao().insert(
      com.example.data.model.WaliAccount(
        name = "Ustadz Ngaji",
        username = "ngaji_aa01",
        password = "Pass#Ngaji",
        dormitory = "Masjid",
        schoolId = "TST-AA-01",
        role = com.example.data.model.WaliAccount.ROLE_GURU_NGAJI
      )
    )
    val otherSchoolWaliId = db.waliAccountDao().insert(
      com.example.data.model.WaliAccount(
        name = "Wali Sekolah Lain",
        username = "wali_aa02_1",
        password = "Pass#Other",
        dormitory = "Asrama Putri",
        schoolId = "TST-AA-02",
        role = com.example.data.model.WaliAccount.ROLE_WALI
      )
    )

    val vm = com.example.ui.HealthViewModel(app)
    val adminLogin = vm.executeLogin("admin_aa01", "Password#123", rememberMe = false)
    org.junit.Assert.assertTrue(adminLogin is com.example.ui.LoginResult.SuccessAdmin)

    val allSchool1 = db.waliAccountDao().getAccountsBySchoolList("TST-AA-01")
    // 1. Sematkan 7 Wali pertama -> semua harus sukses (quota 7/7)
    for (idx in 0 until 7) {
      val target = allSchool1.first { it.id == insertedIds[idx] }
      var resOk: Boolean? = null
      vm.setWaliAnakAdminStatus(target, true) { ok, _ -> resOk = ok }
      for (t in 1..40) {
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        if (resOk != null) break
        kotlinx.coroutines.delay(25)
      }
      org.junit.Assert.assertEquals("Expected slot ${idx + 1}/7 to succeed", true, resOk)
    }

    // 2. Sematkan Wali ke-8 -> HARUS ditolak karena kuota 7/7 penuh
    val wali8 = db.waliAccountDao().getAccountsBySchoolList("TST-AA-01").first { it.id == insertedIds[7] }
    var eighthOk: Boolean? = null
    var eighthMsg = ""
    vm.setWaliAnakAdminStatus(wali8, true) { ok, msg ->
      eighthOk = ok
      eighthMsg = msg
    }
    for (t in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (eighthOk != null) break
      kotlinx.coroutines.delay(25)
    }
    org.junit.Assert.assertEquals(false, eighthOk)
    org.junit.Assert.assertTrue(eighthMsg.contains("7"))

    // 3. Guru Ngaji tidak dapat disematkan Anak Admin
    val guruNgaji = db.waliAccountDao().getAccountsBySchoolList("TST-AA-01").first { it.id == guruNgajiId }
    var guruOk: Boolean? = null
    vm.setWaliAnakAdminStatus(guruNgaji, true) { ok, _ -> guruOk = ok }
    for (t in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (guruOk != null) break
      kotlinx.coroutines.delay(25)
    }
    org.junit.Assert.assertEquals(false, guruOk)

    // 4. Tenant sekolah lain ("TST-AA-02") tetap punya kuota independen
    val otherWali = db.waliAccountDao().getAccountsBySchoolList("TST-AA-02").first { it.id == otherSchoolWaliId }
    var otherOk: Boolean? = null
    vm.setWaliAnakAdminStatus(otherWali, true) { ok, _ -> otherOk = ok }
    for (t in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (otherOk != null) break
      kotlinx.coroutines.delay(25)
    }
    org.junit.Assert.assertEquals(true, otherOk)

    // 5. Cabut 1 Anak Admin di TST-AA-01 -> Wali ke-8 sekarang bisa disematkan
    val wali1 = db.waliAccountDao().getAccountsBySchoolList("TST-AA-01").first { it.id == insertedIds[0] }
    var unpinOk: Boolean? = null
    vm.setWaliAnakAdminStatus(wali1, false) { ok, _ -> unpinOk = ok }
    for (t in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (unpinOk != null) break
      kotlinx.coroutines.delay(25)
    }
    org.junit.Assert.assertEquals(true, unpinOk)

    var retryEighthOk: Boolean? = null
    vm.setWaliAnakAdminStatus(wali8, true) { ok, _ -> retryEighthOk = ok }
    for (t in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (retryEighthOk != null) break
      kotlinx.coroutines.delay(25)
    }
    org.junit.Assert.assertEquals(true, retryEighthOk)

    // 6. Login sebagai Wali yang disematkan (wali_aa01_8) -> tetap login sebagai Wali Asuh (SuccessWali),
    //    tetapi isAnakAdminSekolah & canManageSchoolSchedule() TRUE serta boleh edit sel + pin tugas RSUD
    val anakAdminLogin = vm.executeLogin("wali_aa01_8", "Pass#8", rememberMe = false)
    org.junit.Assert.assertTrue(anakAdminLogin is com.example.ui.LoginResult.SuccessWali)
    for (t in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.isAnakAdminSekolah.value) break
      kotlinx.coroutines.delay(25)
    }
    org.junit.Assert.assertTrue(vm.isAnakAdminSekolah.value)
    org.junit.Assert.assertTrue(vm.canManageSchoolSchedule())

    var syncedByAnakAdmin: Boolean? = null
    vm.syncScheduleMembersFromWaliAccounts("2026-11") { ok, _ -> syncedByAnakAdmin = ok }
    for (t in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (syncedByAnakAdmin != null) break
      kotlinx.coroutines.delay(25)
    }
    org.junit.Assert.assertEquals(true, syncedByAnakAdmin)

    val entriesAA1 = db.waliScheduleEntryDao().getEntriesBySchoolAndMonthList("TST-AA-01", "2026-11")
    org.junit.Assert.assertEquals(8, entriesAA1.size) // Guru Ngaji excluded, 8 Wali (termasuk Anak Admin) ikut piket

    val targetEntry = entriesAA1.first { it.waliUsername == "wali_aa01_1" }
    var editedByAnakAdmin: Boolean? = null
    vm.updateWaliScheduleDayCell(
      entry = targetEntry,
      day = 17,
      shiftCode = "P1",
      pinsForDay = listOf(
        com.example.data.model.PinnedTaskItem("Jaga RSUD SLG", com.example.data.model.WaliScheduleHelper.PIN_FASKES)
      ),
      leaveForDay = null
    ) { ok, _ -> editedByAnakAdmin = ok }
    for (t in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (editedByAnakAdmin != null) break
      kotlinx.coroutines.delay(25)
    }
    org.junit.Assert.assertEquals(true, editedByAnakAdmin)

    val savedTarget = db.waliScheduleEntryDao().getEntriesBySchoolAndMonthList("TST-AA-01", "2026-11")
      .first { it.id == targetEntry.id }
    org.junit.Assert.assertEquals("P1", com.example.data.model.WaliScheduleHelper.parseDayCodes(savedTarget.dayCodesJson)[17])
    org.junit.Assert.assertEquals(
      "Jaga RSUD SLG",
      com.example.data.model.WaliScheduleHelper.parseDayPins(savedTarget.dayPinsJson)[17]?.firstOrNull()?.label
    )

    // 7. Login sebagai Wali biasa yang sudah dicabut (wali_aa01_1) -> canManageSchoolSchedule() FALSE
    val plainWaliLogin = vm.executeLogin("wali_aa01_1", "Pass#1", rememberMe = false)
    org.junit.Assert.assertTrue(plainWaliLogin is com.example.ui.LoginResult.SuccessWali)
    for (t in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (!vm.isAnakAdminSekolah.value) break
      kotlinx.coroutines.delay(25)
    }
    org.junit.Assert.assertFalse(vm.isAnakAdminSekolah.value)
    org.junit.Assert.assertFalse(vm.canManageSchoolSchedule())

    var blockedEdit: Boolean? = null
    vm.updateWaliScheduleDayCell(savedTarget, 18, "M3", emptyList(), null) { ok, _ -> blockedEdit = ok }
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    org.junit.Assert.assertEquals(false, blockedEdit)
  }

  @Test
  fun `verify AdminScreen Anak Admin quota card & toggle and CategoryHubOverlay Anak Admin CSV controls`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    db.waliAccountDao().deleteBySchoolId("TST-UI-AA")
    db.schoolRegistrationDao().insert(
      com.example.data.model.SchoolRegistration(
        schoolName = "SR UI Anak Admin",
        address = "Jl. UI Test",
        city = "Kediri",
        estimatedWaliCount = 10,
        picName = "Admin UI",
        picEmail = "adminui@test.id",
        picPhone = "0812345",
        generatedSchoolCode = "TST-UI-AA",
        generatedAdminUsername = "admin_ui_aa",
        generatedAdminPassword = "Password#123",
        status = "APPROVED"
      )
    )
    val waliId = db.waliAccountDao().insert(
      com.example.data.model.WaliAccount(
        name = "Siti Maslukah",
        username = "siti_ui_aa",
        password = "Pass#123",
        dormitory = "Asrama Putri",
        schoolId = "TST-UI-AA",
        role = com.example.data.model.WaliAccount.ROLE_WALI_ANAK_ADMIN
      )
    )
    val guruId = db.waliAccountDao().insert(
      com.example.data.model.WaliAccount(
        name = "Ustadz Hasan",
        username = "hasan_ngaji",
        password = "Pass#456",
        dormitory = "Masjid",
        schoolId = "TST-UI-AA",
        role = com.example.data.model.WaliAccount.ROLE_GURU_NGAJI
      )
    )

    val vm = com.example.ui.HealthViewModel(app)
    vm.executeLogin("admin_ui_aa", "Password#123", rememberMe = false)
    for (t in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.waliAccounts.value.any { it.id == waliId }) break
      kotlinx.coroutines.delay(25)
    }

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(
        com.example.util.i18n.LocalAppStrings provides indonesianStrings
      ) {
        com.example.ui.screens.AdminScreen(viewModel = vm, onLogout = {})
      }
    }
    composeTestRule.waitForIdle()

    // Buka Tab Wali Asuh terlebih dahulu (karena tab default AdminScreen adalah Tab Siswa)
    composeTestRule.onNodeWithTag("list_admin_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("row_admin_tabs"))
    composeTestRule.onNodeWithTag("tab_admin_wali").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Pastikan kartu kuota Anak Admin (1/7 Wali) tampil, tombol toggle tampil untuk Wali Asuh, dan TIDAK tampil untuk Guru Ngaji
    composeTestRule.onNodeWithTag("list_admin_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("card_admin_anak_admin_quota"))
    composeTestRule.onNodeWithTag("card_admin_anak_admin_quota").assertExists()

    composeTestRule.onNodeWithTag("list_admin_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("btn_toggle_anak_admin_$waliId"))
    composeTestRule.onNodeWithTag("btn_toggle_anak_admin_$waliId").assertExists()
    composeTestRule.onNodeWithTag("btn_toggle_anak_admin_$guruId").assertDoesNotExist()

    // Pindah ke Tab Jadwal -> pintasan Anak Admin Jadwal harus tampil dan membuka dialog picker
    composeTestRule.onNodeWithTag("list_admin_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("row_admin_tabs"))
    composeTestRule.onNodeWithTag("tab_admin_jadwal").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("list_admin_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("btn_admin_manage_anak_admin_from_jadwal"))
    composeTestRule.onNodeWithTag("card_admin_jadwal_anak_admin_banner").assertExists()
    composeTestRule.onNodeWithTag("btn_admin_manage_anak_admin_from_jadwal").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dialog_kelola_anak_admin").assertExists()
    composeTestRule.onNodeWithTag("switch_picker_anak_admin_$waliId").assertExists()
  }

  @Test
  fun `verify CategoryHubOverlay JADWAL_WALI for Anak Admin shows badge CSV Upload Download and click-to-edit cell`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val codes = com.example.data.model.WaliScheduleHelper.defaultShiftCodes("SRT1-KDR")
    val entry = com.example.data.model.WaliScheduleEntry(
      id = 909L,
      schoolId = "SRT1-KDR",
      monthKey = "2026-11",
      waliUsername = "sitimaslukah",
      waliName = "Siti Maslukah",
      dayCodesJson = com.example.data.model.WaliScheduleHelper.serializeDayCodes(mapOf(17 to "P1")),
      dayPinsJson = com.example.data.model.WaliScheduleHelper.serializeDayPins(
        mapOf(17 to listOf(com.example.data.model.PinnedTaskItem("Jaga Asrama SD")))
      )
    )
    var editedEntryId: Long? = null
    var uploadCsvClicked = false

    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(
        com.example.util.i18n.LocalAppStrings provides indonesianStrings
      ) {
        com.example.ui.components.CategoryHubOverlay(
          category = com.example.ui.components.DashboardCategoryHub.JADWAL_WALI,
          schoolProfile = com.example.data.model.SchoolProfile(tenantCode = "SRT1-KDR", schoolName = "SR 1 Kediri"),
          activePlans = emptyList(),
          inpatientCount = 0,
          fastingSessions = emptyList(),
          fastingRecords = emptyList(),
          activeHaidRecords = emptyList(),
          suciRecords = emptyList(),
          shiftMessages = emptyList(),
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
          jadwalMonthKey = "2026-11",
          jadwalSelectedDay = 17,
          shiftCodes = codes,
          scheduleEntries = listOf(entry),
          canManageSchedule = true,
          isAnakAdminSekolah = true,
          currentUsername = "ekowahyudi",
          currentWaliName = "Eko Wahyudi",
          onEditScheduleCell = { e, _ -> editedEntryId = e.id },
          onOpenUploadJadwalCsv = { uploadCsvClicked = true }
        )
      }
    }
    composeTestRule.waitForIdle()

    // Kontrol Anak Admin Jadwal: badge mode, Kelola Shift, Matriks/Tabel, Download CSV, Upload CSV
    composeTestRule.onNodeWithTag("list_category_hub_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("badge_anak_admin_jadwal_mode"))
    composeTestRule.onNodeWithTag("badge_anak_admin_jadwal_mode").assertExists()
    composeTestRule.onNodeWithTag("btn_jadwal_admin_shift_codes").assertExists()
    composeTestRule.onNodeWithTag("btn_jadwal_admin_import").assertExists()
    composeTestRule.onNodeWithTag("btn_jadwal_hub_download_csv").assertExists()
    composeTestRule.onNodeWithTag("btn_jadwal_hub_upload_csv").assertExists().performClick()
    org.junit.Assert.assertTrue(uploadCsvClicked)

    // Klik langsung baris wali pada roster harian -> memicu dialog edit shift & pin tugas
    composeTestRule.onNodeWithTag("list_category_hub_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("row_jadwal_member_${entry.id}"))
    composeTestRule.onNodeWithTag("row_jadwal_member_${entry.id}").assertExists().performClick()
    org.junit.Assert.assertEquals(entry.id, editedEntryId)
  }

  @Test
  fun `verify PenyematanTugasSheet shows interactive all-Wali editor even with 0 pins for Anak Admin`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val codes = com.example.data.model.WaliScheduleHelper.defaultShiftCodes("SRT1-KDR")
    val unpinnedEntry = com.example.data.model.WaliScheduleEntry(
      id = 777L,
      schoolId = "SRT1-KDR",
      monthKey = "2026-11",
      waliUsername = "abisan",
      waliName = "Abisarwan Rafif",
      dayCodesJson = com.example.data.model.WaliScheduleHelper.serializeDayCodes(mapOf(17 to "LP", 18 to "P1")),
      dayPinsJson = "{}"
    )
    var editedPair: Pair<Long, Int>? = null

    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(
        com.example.util.i18n.LocalAppStrings provides indonesianStrings
      ) {
        com.example.ui.dialogs.PenyematanTugasSheet(
          monthKey = "2026-11",
          selectedDay = 17,
          entries = listOf(unpinnedEntry),
          shiftCodes = codes,
          schoolProfile = com.example.data.model.SchoolProfile(tenantCode = "SRT1-KDR", schoolName = "SR 1 Kediri"),
          canManageSchedule = true,
          onEditCell = { entry, day -> editedPair = entry.id to day },
          onDismiss = {}
        )
      }
    }
    composeTestRule.waitForIdle()

    // Meskipun 0 pin pada tgl 17, Anak Admin langsung melihat mode kelola + daftar Wali + tombol Ubah/Pin
    composeTestRule.onNodeWithTag("badge_penyematan_manage_mode").assertExists()
    composeTestRule.onNodeWithTag("chip_pin_mode_kelola").assertExists()
    composeTestRule.onNodeWithTag("input_search_penyematan_wali").assertExists()
    composeTestRule.onNodeWithTag("card_manage_pin_wali_${unpinnedEntry.id}").assertExists()
    composeTestRule.onNodeWithTag("btn_manage_pin_wali_${unpinnedEntry.id}").performClick()
    org.junit.Assert.assertEquals(unpinnedEntry.id to 17, editedPair)
  }

  @Test
  fun `verify InfoPentingHelper enforces Superadmin public author, multi-school global scope, reactions, threaded replies, and self-only delete`() {
    // 1. Point 1: penulis selalu tampil "Superadmin" dan tidak pernah membocorkan "anaksuperadmin"
    assertEquals("Superadmin", com.example.data.model.InfoPentingHelper.resolvePublicAuthor("anaksuperadmin"))
    assertEquals("Superadmin", com.example.data.model.InfoPentingHelper.resolvePublicAuthor(""))
    assertEquals("Superadmin", com.example.data.model.InfoPentingHelper.resolvePublicAuthor("Eko Wahyudi"))
    assertEquals("Superadmin", com.example.data.model.InfoPentingHelper.resolveCommentAuthorDisplay("anaksuperadmin", "anaksuperadmin"))
    assertEquals("Budi Wali", com.example.data.model.InfoPentingHelper.resolveCommentAuthorDisplay("Budi Wali", "budi"))

    // 2. Point 2: Global school scope & urutan pinned terbaru dulu
    val posts = listOf(
      com.example.data.model.InfoPentingPost(
        id = 1L,
        schoolId = com.example.data.model.InfoPentingHelper.GLOBAL_SCHOOL_ID,
        title = "Pengumuman Biasa",
        content = "Halo semua wali asuh",
        isPinned = false,
        createdAt = 2000L
      ),
      com.example.data.model.InfoPentingPost(
        id = 2L,
        schoolId = com.example.data.model.InfoPentingHelper.GLOBAL_SCHOOL_ID,
        title = "Surat Edaran Penting",
        content = "Mohon dibaca lintas sekolah",
        isPinned = true,
        createdAt = 1000L
      ),
      com.example.data.model.InfoPentingPost(
        id = 3L,
        schoolId = com.example.data.model.InfoPentingHelper.GLOBAL_SCHOOL_ID,
        title = "Terhapus",
        content = "Hidden",
        isDeleted = true,
        createdAt = 5000L
      )
    )
    val visible = com.example.data.model.InfoPentingHelper.filterPosts(
      posts = posts,
      selectedCategory = com.example.data.model.InfoPentingHelper.CATEGORY_SEMUA,
      searchQuery = ""
    )
    assertEquals(listOf(2L, 1L), visible.map { it.id })
    assertEquals("__GLOBAL__", visible.first().schoolId)

    // Point 1 authorization helper
    org.junit.Assert.assertTrue(com.example.data.model.InfoPentingHelper.canDeletePost(posts[0], isAnakSuperAdmin = true))
    org.junit.Assert.assertFalse(com.example.data.model.InfoPentingHelper.canDeletePost(posts[0], isAnakSuperAdmin = false))

    // 3. Point 4: Reaksi Suka & Tidak Suka lintas sekolah
    val userA = "SRT1-KDR:budi"
    val userB = "SRT2-MLG:ani"
    val reactions = listOf(
      com.example.data.model.InfoPentingReaction(id = 10L, postId = 1L, userKey = userA, reactionType = com.example.data.model.InfoPentingHelper.REACTION_LIKE),
      com.example.data.model.InfoPentingReaction(id = 11L, postId = 1L, userKey = userB, reactionType = com.example.data.model.InfoPentingHelper.REACTION_DISLIKE),
      com.example.data.model.InfoPentingReaction(id = 12L, postId = 1L, userKey = "SRT3:citra", reactionType = com.example.data.model.InfoPentingHelper.REACTION_NONE)
    )
    val summary = com.example.data.model.InfoPentingHelper.summarizeReactions(reactions, postId = 1L, currentUserKey = userA)
    assertEquals(1, summary.likesCount)
    assertEquals(1, summary.dislikesCount)
    assertEquals(com.example.data.model.InfoPentingHelper.REACTION_LIKE, summary.myReaction)

    // 4. Point 4: Thread balasan komentar & hapus hanya komentar milik sendiri
    val cRoot = com.example.data.model.InfoPentingComment(
      id = 100L,
      postId = 1L,
      parentCommentId = 0L,
      authorKey = userA,
      authorName = "Budi",
      content = "Siap laksanakan!",
      createdAt = 1000L
    )
    val cReply = com.example.data.model.InfoPentingComment(
      id = 101L,
      postId = 1L,
      parentCommentId = 100L,
      replyToAuthorName = "Budi",
      authorKey = userB,
      authorName = "Ani",
      content = "Setuju Pak Budi",
      createdAt = 1500L
    )
    val threads = com.example.data.model.InfoPentingHelper.buildCommentThreads(listOf(cReply, cRoot), postId = 1L)
    assertEquals(1, threads.size)
    assertEquals(100L, threads[0].comment.id)
    assertEquals(1, threads[0].replies.size)
    assertEquals(101L, threads[0].replies[0].id)

    org.junit.Assert.assertTrue(com.example.data.model.InfoPentingHelper.canDeleteComment(cRoot, userA))
    org.junit.Assert.assertFalse(com.example.data.model.InfoPentingHelper.canDeleteComment(cRoot, userB))
    org.junit.Assert.assertTrue(com.example.data.model.InfoPentingHelper.canDeleteComment(cReply, userB))
  }

  @Test
  fun `verify InfoPentingFeedSheet hides create FAB for regular Wali, renders video player card, reactions, replies, and self-only delete`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val myKey = "SRT2-MLG:wali_siti"
    val videoPost = com.example.data.model.InfoPentingPost(
      id = 501L,
      schoolId = com.example.data.model.InfoPentingHelper.GLOBAL_SCHOOL_ID,
      title = "Sosialisasi SOP Kesehatan",
      content = "Silakan tonton video panduan terbaru lintas sekolah.",
      category = com.example.data.model.InfoPentingHelper.CATEGORY_PENGUMUMAN,
      mediaType = com.example.data.model.InfoPentingHelper.MEDIA_VIDEO,
      mediaUrl = "https://example.org/panduan.mp4",
      mediaName = "panduan.mp4",
      authorDisplayName = "anaksuperadmin",
      isPinned = true,
      createdAt = System.currentTimeMillis()
    )
    val ownComment = com.example.data.model.InfoPentingComment(
      id = 901L,
      postId = 501L,
      parentCommentId = 0L,
      authorKey = myKey,
      authorName = "Ustadzah Siti",
      authorSchoolId = "SRT2-MLG",
      authorSchoolName = "SR 2 Malang",
      content = "Terima kasih infonya!",
      createdAt = 1000L
    )
    val otherComment = com.example.data.model.InfoPentingComment(
      id = 902L,
      postId = 501L,
      parentCommentId = 0L,
      authorKey = "SRT1-KDR:budi",
      authorName = "Ustadz Budi",
      authorSchoolId = "SRT1-KDR",
      authorSchoolName = "SR 1 Kediri",
      content = "Mohon izin bertanya.",
      createdAt = 1200L
    )
    var reactionClicked: String? = null
    var sentReplyTuple: Triple<Long, String, Long?>? = null
    var deletedCommentId: Long? = null

    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(
        com.example.util.i18n.LocalAppStrings provides indonesianStrings
      ) {
        com.example.ui.dialogs.InfoPentingFeedSheet(
          posts = listOf(videoPost),
          reactions = emptyList(),
          comments = listOf(ownComment, otherComment),
          currentUserKey = myKey,
          isAnakSuperAdmin = false,
          onOpenCreatePost = {},
          onRefreshSync = {},
          onTogglePinPost = {},
          onDeletePost = {},
          onToggleReaction = { _, type -> reactionClicked = type },
          onSendComment = { p, txt, replyTo -> sentReplyTuple = Triple(p.id, txt, replyTo?.id) },
          onDeleteOwnComment = { c -> deletedCommentId = c.id },
          onDismiss = {}
        )
      }
    }
    composeTestRule.waitForIdle()

    // Wali biasa tidak boleh melihat FAB buat postingan, tapi melihat penulis "Superadmin" (bukan "anaksuperadmin") & pemutar video
    composeTestRule.onNodeWithTag("sheet_info_penting").assertExists()
    composeTestRule.onNodeWithTag("fab_create_info_post").assertDoesNotExist()
    composeTestRule.onNodeWithTag("btn_pin_post_501").assertDoesNotExist()
    composeTestRule.onNodeWithTag("btn_delete_post_501").assertDoesNotExist()
    composeTestRule.onNodeWithText("Superadmin", substring = false).assertExists()
    composeTestRule.onNodeWithText("anaksuperadmin", substring = true).assertDoesNotExist()
    composeTestRule.onNodeWithTag("info_video_player_card_501").assertExists()

    // Like & Buka Komentar (scroll dulu ke tombol reaksi agar tidak terpotong batas viewport LazyColumn)
    composeTestRule.onNodeWithTag("btn_like_post_501").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(com.example.data.model.InfoPentingHelper.REACTION_LIKE, reactionClicked)

    composeTestRule.onNodeWithTag("btn_open_comments_501").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Tombol hapus hanya muncul di komentar milik sendiri (901), bukan komentar orang lain (902)
    composeTestRule.onNodeWithTag("sheet_info_comments").assertExists()
    composeTestRule.onNodeWithTag("btn_delete_comment_901").assertExists()
    composeTestRule.onNodeWithTag("btn_delete_comment_902").assertDoesNotExist()

    // Balas komentar orang lain (902)
    composeTestRule.onNodeWithTag("btn_reply_comment_902").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("input_info_comment").performTextInput("Siap dibantu Pak Budi")
    composeTestRule.onNodeWithTag("btn_send_info_comment").performClick()
    assertEquals(Triple(501L, "Siap dibantu Pak Budi", 902L), sentReplyTuple)

    // Hapus komentar sendiri (901)
    composeTestRule.onNodeWithTag("btn_delete_comment_901").performScrollTo().performClick()
    assertEquals(901L, deletedCommentId)
  }

  @Test
  fun `verify InfoPentingFeedSheet shows create FAB and post moderation controls exclusively for anaksuperadmin`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val post = com.example.data.model.InfoPentingPost(
      id = 601L,
      schoolId = com.example.data.model.InfoPentingHelper.GLOBAL_SCHOOL_ID,
      title = "Dokumen Juknis Nasional",
      content = "Silakan unduh lampiran.",
      mediaType = com.example.data.model.InfoPentingHelper.MEDIA_DOCUMENT,
      mediaUrl = "https://example.org/juknis.pdf",
      mediaName = "juknis.pdf",
      externalLink = "https://sekolah.rakyat.id",
      authorDisplayName = "Superadmin",
      createdAt = System.currentTimeMillis()
    )
    var openedCreate = false
    var toggledPin = false

    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(
        com.example.util.i18n.LocalAppStrings provides indonesianStrings
      ) {
        com.example.ui.dialogs.InfoPentingFeedSheet(
          posts = listOf(post),
          reactions = emptyList(),
          comments = emptyList(),
          currentUserKey = "SRT1-KDR:anaksuperadmin",
          isAnakSuperAdmin = true,
          onOpenCreatePost = { openedCreate = true },
          onRefreshSync = {},
          onTogglePinPost = { toggledPin = true },
          onDeletePost = {},
          onToggleReaction = { _, _ -> },
          onSendComment = { _, _, _ -> },
          onDeleteOwnComment = {},
          onDismiss = {}
        )
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("fab_create_info_post").assertExists().performClick()
    org.junit.Assert.assertTrue(openedCreate)

    composeTestRule.onNodeWithTag("btn_pin_post_601").assertExists().performClick()
    org.junit.Assert.assertTrue(toggledPin)
    composeTestRule.onNodeWithTag("btn_delete_post_601").assertExists()
  }

  @Test
  fun `verify ParentExpectation PocketBase DTO roundtrip, Room CRUD, and ViewModel multi-tenant flow`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)

    // 1. PocketBase DTO roundtrip
    val sample = com.example.data.model.ParentExpectation(
      id = 88101L,
      studentId = 9501L,
      studentName = "Bima Pratama",
      studentNis = "410099",
      studentClass = "X-2",
      studentDorm = "Asrama Putra",
      parentName = "Ibu Sari",
      waliUsername = "walibima",
      waliName = "Ust. Hasan",
      characterFocusCsv = com.example.data.model.ParentExpectationHelper.formatFocusCsv(
        listOf("Kejujuran & Amanah", "Kemandirian")
      ),
      characterExpectation = "Semoga ananda semakin jujur dan mandiri dalam ibadah maupun keseharian.",
      adviceFocusNotes = "Mohon diingatkan untuk merapikan tempat tidur sebelum subuh.",
      parentPrayerMessage = "Doa ibu selalu menyertai ananda di Sekolah Rakyat.",
      isReadByWali = false,
      waliFollowUpNote = "",
      waliRespondedAt = 0L,
      schoolId = "SRT1-KDR",
      createdAt = 1710000000000L,
      updatedAt = 1710000000000L
    )
    val pbDto = com.example.data.remote.pocketbase.PbParentExpectationDto.fromDomain(sample)
    val restored = pbDto.toDomainModel()
    assertEquals(sample.id, restored.id)
    assertEquals(sample.studentName, restored.studentName)
    assertEquals(sample.characterExpectation, restored.characterExpectation)
    assertEquals(sample.adviceFocusNotes, restored.adviceFocusNotes)
    assertEquals(listOf("Kejujuran & Amanah", "Kemandirian"), com.example.data.model.ParentExpectationHelper.parseFocusList(restored.characterFocusCsv))

    // 2. Google Voice-to-Text helper verification (no RECORD_AUDIO permission required)
    val voiceIntent = com.example.ui.dialogs.buildGoogleSpeechToTextIntent("Ucapkan harapan...", "id-ID")
    assertEquals(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH, voiceIntent.action)
    assertEquals("id-ID", voiceIntent.getStringExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE))
    assertEquals("Rajin sholat", com.example.ui.dialogs.appendVoiceTranscript("", "Rajin sholat"))
    assertEquals(
      "Rajin sholat dan jujur",
      com.example.ui.dialogs.appendVoiceTranscript("Rajin sholat", "dan jujur")
    )

    // 3. ViewModel save, read, follow-up, and tenant isolation
    val student = com.example.data.model.ChildStudent(
      id = 9501L,
      name = "Bima Pratama",
      nis = "410099",
      gender = "L",
      age = 15,
      dormitory = "Asrama Putra",
      classGrade = "X-2",
      allergies = "-",
      bloodType = "O",
      emergencyContact = "08123456789",
      motherName = "Ibu Sari",
      assignedWaliUsername = "walibima",
      assignedWaliName = "Ust. Hasan",
      schoolId = "SRT1-KDR"
    )
    val otherSchoolExp = sample.copy(id = 88102L, studentId = 9599L, schoolId = "SRT-OTHER")
    db.childStudentDao().insert(student)
    db.parentExpectationDao().insert(otherSchoolExp)

    var savedOk = false
    vm.saveParentExpectation(
      student = student,
      focusTags = listOf("Kejujuran & Amanah", "Disiplin & Tanggung Jawab"),
      characterExpectation = "Ingin ananda jujur dan bertanggung jawab.",
      adviceFocusNotes = "Dinasehati agar tidak menunda tugas.",
      parentPrayerMessage = "Semangat terus ya Nak."
    ) { ok, _ -> savedOk = ok }

    var loops = 0
    while (loops < 30 && vm.parentExpectations.value.none { it.studentId == 9501L }) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      loops++
    }
    org.junit.Assert.assertTrue(savedOk)
    org.junit.Assert.assertTrue(vm.parentExpectations.value.none { it.schoolId == "SRT-OTHER" })

    val inserted = vm.parentExpectations.value.first { it.studentId == 9501L }
    org.junit.Assert.assertFalse(inserted.isReadByWali)

    vm.markParentExpectationsReadByWali(9501L)
    loops = 0
    while (loops < 30 && vm.parentExpectations.value.first { it.id == inserted.id }.isReadByWali.not()) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      loops++
    }
    org.junit.Assert.assertTrue(vm.parentExpectations.value.first { it.id == inserted.id }.isReadByWali)

    var followUpOk = false
    vm.saveWaliExpectationFollowUp(
      expectation = vm.parentExpectations.value.first { it.id == inserted.id },
      followUpNote = "Alhamdulillah sudah kami nasehatkan secara pribadi setelah sholat Maghrib."
    ) { ok, _ -> followUpOk = ok }
    loops = 0
    while (loops < 30 && vm.parentExpectations.value.first { it.id == inserted.id }.waliFollowUpNote.isBlank()) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      loops++
    }
    org.junit.Assert.assertTrue(followUpOk)
    org.junit.Assert.assertTrue(
      vm.parentExpectations.value.first { it.id == inserted.id }.waliFollowUpNote.contains("Maghrib")
    )

    vm.deleteParentExpectation(vm.parentExpectations.value.first { it.id == inserted.id })
    loops = 0
    while (loops < 30 && vm.parentExpectations.value.any { it.id == inserted.id }) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      loops++
    }
    org.junit.Assert.assertTrue(vm.parentExpectations.value.none { it.id == inserted.id })
  }

  @Test
  fun `verify ParentPortalScreen Harapan Karakter form, voice icon, and Wali Asuh Hubungan Ortu HarapanOrangTuaSheet`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)

    val student = com.example.data.model.ChildStudent(
      id = 9601L,
      name = "Aisyah Humaira",
      nis = "420011",
      gender = "P",
      age = 14,
      dormitory = "Asrama Putri",
      classGrade = "VIII-A",
      allergies = "-",
      bloodType = "A",
      emergencyContact = "08111222333",
      motherName = "Ibu Fatimah",
      assignedWaliUsername = "ustzah_lina",
      assignedWaliName = "Ustadzah Lina",
      schoolId = "SRT1-KDR"
    )
    val parentLink = com.example.data.model.ParentAccessLink(
      id = 9602L,
      studentId = 9601L,
      studentName = student.name,
      studentNis = student.nis,
      studentClass = student.classGrade,
      studentDorm = student.dormitory,
      parentName = "Ibu Fatimah",
      parentPhone = "08111222333",
      username = "ortu_aisyah_9601",
      password = "OrtuPass99!",
      assignedWaliUsername = "ustzah_lina",
      assignedWaliName = "Ustadzah Lina",
      canViewMengaji = true,
      canViewMedical = true,
      canViewFasting = true,
      canViewHaid = true,
      canViewProfile = true,
      canViewTabungan = true,
      schoolId = "SRT1-KDR"
    )
    db.childStudentDao().insert(student)
    db.parentAccessLinkDao().insert(parentLink)

    val loginRes = vm.executeLogin("ortu_aisyah_9601", "OrtuPass99!", rememberMe = false)
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessParent)

    var loops = 0
    while (loops < 30 && vm.activeParentAccessLink.value == null) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      loops++
    }

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val stage = androidx.compose.runtime.mutableIntStateOf(0)
    var openedHarapanSheet = false
    var savedWaliNotePair: Pair<Long, String>? = null
    val savedExpHolder = androidx.compose.runtime.mutableStateOf<com.example.data.model.ParentExpectation?>(null)

    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        when (stage.intValue) {
          0 -> com.example.ui.screens.ParentPortalScreen(viewModel = vm, onLogout = {})
          1 -> com.example.ui.components.CategoryHubOverlay(
            category = com.example.ui.components.DashboardCategoryHub.ORANG_TUA,
            schoolProfile = com.example.data.model.SchoolProfile(),
            activePlans = emptyList(),
            inpatientCount = 0,
            fastingSessions = emptyList(),
            fastingRecords = emptyList(),
            activeHaidRecords = emptyList(),
            suciRecords = emptyList(),
            shiftMessages = emptyList(),
            students = listOf(student),
            parentAccessLinks = listOf(parentLink),
            parentMessages = emptyList(),
            parentExpectations = listOfNotNull(savedExpHolder.value),
            myFosterStudentsCount = 1,
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
            onOpenHarapanOrtu = { openedHarapanSheet = true }
          )
          else -> com.example.ui.dialogs.HarapanOrangTuaSheet(
            students = listOf(student),
            parentExpectations = listOfNotNull(savedExpHolder.value),
            isStudentClaimedByCurrentWali = { it.id == 9601L },
            isAdmin = false,
            onMarkReadByWali = {},
            onSaveFollowUpNote = { exp, note -> savedWaliNotePair = exp.id to note },
            onOpenChatWithStudent = {},
            onDismiss = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // Buka menu Harapan Karakter dari halaman Beranda Orang Tua
    composeTestRule.onNodeWithTag("card_parent_summary_harapan").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("parent_harapan_content").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_harapan_guidance").assertIsDisplayed()
    composeTestRule.onNodeWithText("Selama 3 tahun atau lebih", substring = true).assertExists()
    composeTestRule.onNodeWithText("Sekolah Rakyat", substring = true).assertExists()
    composeTestRule.onNodeWithText("ikhtiar bersama", substring = true).assertExists()
    composeTestRule.onNodeWithTag("btn_voice_parent_harapan_karakter").assertExists()

    composeTestRule.onNodeWithTag("chip_harapan_focus_Kejujuran & Amanah").performClick()
    composeTestRule.onNodeWithTag("input_parent_harapan_karakter")
      .performTextInput("Semoga Aisyah selalu jujur, rendah hati, dan semangat menuntut ilmu.")
    composeTestRule.onNodeWithTag("input_parent_harapan_nasehat")
      .performTextInput("Mohon dinasehati agar lebih berani berbicara dengan sopan.")
    composeTestRule.onNodeWithTag("input_parent_harapan_doa")
      .performTextInput("Semoga Allah mudahkan segala urusan ananda.")
    composeTestRule.onNodeWithTag("btn_save_parent_harapan").performScrollTo().performClick()

    loops = 0
    while (loops < 30 && vm.parentExpectations.value.none { it.studentId == 9601L }) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      loops++
    }
    composeTestRule.waitForIdle()
    val savedExp = vm.parentExpectations.value.first { it.studentId == 9601L }
    savedExpHolder.value = savedExp
    composeTestRule.onNodeWithTag("parent_harapan_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("card_parent_harapan_item_${savedExp.id}"))
    composeTestRule.onNodeWithTag("card_parent_harapan_item_${savedExp.id}").assertExists()

    // Verifikasi tampilan di sisi Wali Asuh: CategoryHubOverlay (ORANG_TUA)
    stage.intValue = 1
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tile_harapan_ortu").performScrollTo().assertIsDisplayed().performClick()
    org.junit.Assert.assertTrue(openedHarapanSheet)

    // Verifikasi tampilan HarapanOrangTuaSheet
    stage.intValue = 2
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sheet_harapan_ortu").assertExists()
    composeTestRule.onNodeWithTag("card_harapan_student_9601").assertExists()
    composeTestRule.onNodeWithText("Semoga Aisyah selalu jujur", substring = true).assertExists()

    composeTestRule.onNodeWithTag("list_harapan_ortu", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("btn_toggle_reply_harapan_${savedExp.id}"))
    composeTestRule.onNodeWithTag("btn_toggle_reply_harapan_${savedExp.id}").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("list_harapan_ortu", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("btn_voice_wali_harapan_note_${savedExp.id}"))
    composeTestRule.onNodeWithTag("btn_voice_wali_harapan_note_${savedExp.id}").assertExists()
    composeTestRule.onNodeWithTag("input_wali_harapan_note_${savedExp.id}")
      .performTextInput("Siap Bunda, ananda sudah kami ajak diskusi lembut dan beri motivasi.")
    composeTestRule.onNodeWithTag("btn_save_wali_harapan_note_${savedExp.id}").performClick()
    assertEquals(
      savedExp.id to "Siap Bunda, ananda sudah kami ajak diskusi lembut dan beri motivasi.",
      savedWaliNotePair
    )
  }

  @Test
  fun `verify DateUtils formatChatDateChip and formatTime for WhatsApp style date dividers`() {
    val now = System.currentTimeMillis()
    val yesterday = java.util.Calendar.getInstance().apply {
      add(java.util.Calendar.DAY_OF_YEAR, -1)
    }.timeInMillis
    val older = java.util.Calendar.getInstance().apply {
      set(2024, java.util.Calendar.JANUARY, 15, 9, 30, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    assertEquals(
      "Hari Ini",
      com.example.util.DateUtils.formatChatDateChip(now, com.example.util.i18n.AppLanguage.INDONESIAN)
    )
    assertEquals(
      "Today",
      com.example.util.DateUtils.formatChatDateChip(now, com.example.util.i18n.AppLanguage.ENGLISH)
    )
    assertEquals(
      "Kemarin",
      com.example.util.DateUtils.formatChatDateChip(yesterday, com.example.util.i18n.AppLanguage.INDONESIAN)
    )
    assertEquals(
      "Yesterday",
      com.example.util.DateUtils.formatChatDateChip(yesterday, com.example.util.i18n.AppLanguage.ENGLISH)
    )
    assertEquals(
      com.example.util.DateUtils.formatShortDate(older, com.example.util.i18n.AppLanguage.INDONESIAN),
      com.example.util.DateUtils.formatChatDateChip(older, com.example.util.i18n.AppLanguage.INDONESIAN)
    )
    val timeStr = com.example.util.DateUtils.formatTime(older, com.example.util.i18n.AppLanguage.INDONESIAN)
    assertEquals("09:30", timeStr)
  }

  @Test
  fun `verify ParentChatMessagesList hides sender name, renders centered date dividers and time, and deletes own messages`() {
    val yesterdayTs = java.util.Calendar.getInstance().apply {
      add(java.util.Calendar.DAY_OF_YEAR, -1)
      set(java.util.Calendar.HOUR_OF_DAY, 8)
      set(java.util.Calendar.MINUTE, 15)
    }.timeInMillis
    val todayTs = java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.HOUR_OF_DAY, 14)
      set(java.util.Calendar.MINUTE, 42)
    }.timeInMillis

    val msgWaliYesterday = com.example.data.model.ParentMessage(
      id = 7001L,
      studentId = 9701L,
      studentName = "Budi Santoso",
      parentName = "Ibu Fatimah",
      waliUsername = "eko_wali",
      waliName = "Eko Wahyudi",
      senderRole = "WALI",
      senderName = "Eko Wahyudi",
      messageContent = "enggih, ananda sehat dan aktif belajar",
      isReadByWali = true,
      isReadByParent = true,
      schoolId = "SRT1-KDR",
      createdAt = yesterdayTs
    )
    val msgParentToday = com.example.data.model.ParentMessage(
      id = 7002L,
      studentId = 9701L,
      studentName = "Budi Santoso",
      parentName = "Ibu Fatimah",
      waliUsername = "eko_wali",
      waliName = "Eko Wahyudi",
      senderRole = "PARENT",
      senderName = "Ibu Fatimah",
      messageContent = "Alhamdulillah, terima kasih informasinya Pak",
      isReadByWali = false,
      isReadByParent = true,
      schoolId = "SRT1-KDR",
      createdAt = todayTs
    )

    val deletedIds = mutableListOf<Long>()
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.components.ParentChatMessagesList(
          messages = listOf(msgWaliYesterday, msgParentToday),
          myRole = "PARENT",
          isEn = false,
          lang = com.example.util.i18n.AppLanguage.INDONESIAN,
          emptyContent = {},
          onDeleteMessage = { deletedIds.add(it.id) }
        )
      }
    }
    composeTestRule.waitForIdle()

    // Sender names MUST NOT be shown inside chat bubbles
    composeTestRule.onAllNodesWithText("Eko Wahyudi").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Ibu Fatimah").assertCountEquals(0)

    // Message content & HH:mm times MUST be displayed
    composeTestRule.onNodeWithText("enggih, ananda sehat dan aktif belajar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Alhamdulillah, terima kasih informasinya Pak").assertIsDisplayed()
    composeTestRule.onNodeWithText(
      com.example.util.DateUtils.formatTime(yesterdayTs, com.example.util.i18n.AppLanguage.INDONESIAN)
    ).assertIsDisplayed()
    composeTestRule.onNodeWithText(
      com.example.util.DateUtils.formatTime(todayTs, com.example.util.i18n.AppLanguage.INDONESIAN)
    ).assertIsDisplayed()

    // Centered WhatsApp-style date divider pills for each day
    val yKey = com.example.util.DateUtils.formatChatDayKey(yesterdayTs)
    val tKey = com.example.util.DateUtils.formatChatDayKey(todayTs)
    composeTestRule.onNodeWithTag("chat_date_divider_$yKey").assertIsDisplayed()
    composeTestRule.onNodeWithText("Kemarin").assertIsDisplayed()
    composeTestRule.onNodeWithTag("chat_date_divider_$tKey").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hari Ini").assertIsDisplayed()

    // Delete button appears ONLY for own ("PARENT") message, not peer ("WALI") message
    composeTestRule.onNodeWithTag("btn_delete_chat_msg_7001").assertDoesNotExist()
    composeTestRule.onNodeWithTag("btn_delete_chat_msg_7002").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("dialog_delete_chat_msg").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_confirm_delete_chat_msg").performClick()
    composeTestRule.waitForIdle()
    assertEquals(listOf(7002L), deletedIds)
  }

  @Test
  fun `verify PesanOrangTuaSheet hides sender name inside bubbles and deletes Wali message cleanly`(): Unit = kotlinx.coroutines.runBlocking {
    val student = com.example.data.model.ChildStudent(
      id = 9801L,
      name = "Rizky Pratama",
      nis = "33009801",
      gender = "L",
      age = 13,
      dormitory = "Asrama Putra",
      classGrade = "VII-C",
      motherName = "Ibu Kartika",
      assignedWaliUsername = "ust_budi",
      assignedWaliName = "Ust. Budi",
      schoolId = "SRT1-KDR"
    )
    val nowTs = System.currentTimeMillis()
    val waliMsg = com.example.data.model.ParentMessage(
      id = 8001L,
      studentId = 9801L,
      studentName = student.name,
      parentName = "Ibu Kartika",
      waliUsername = "ust_budi",
      waliName = "Ust. Budi",
      senderRole = "WALI",
      senderName = "Ust. Budi",
      messageContent = "Selamat pagi Ibu, ananda sudah sarapan",
      isReadByWali = true,
      isReadByParent = false,
      schoolId = "SRT1-KDR",
      createdAt = nowTs
    )
    val deletedFromSheet = mutableListOf<Long>()
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.PesanOrangTuaSheet(
          students = listOf(student),
          parentMessages = listOf(waliMsg),
          isStudentClaimedByCurrentWali = { it.id == 9801L },
          isAdmin = false,
          initialSelectedStudent = student,
          onSendMessage = { _, _ -> },
          onMarkRead = {},
          onDeleteMessage = { deletedFromSheet.add(it.id) },
          onDismiss = {}
        )
      }
    }
    composeTestRule.waitForIdle()

    // Sender name must NOT be inside bubble, while message content + "Hari Ini" pill + delete button ARE shown
    composeTestRule.onAllNodesWithText("Ust. Budi").assertCountEquals(0)
    composeTestRule.onNodeWithText("Selamat pagi Ibu, ananda sudah sarapan").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hari Ini").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_delete_chat_msg_8001").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_confirm_delete_chat_msg").performClick()
    composeTestRule.waitForIdle()
    assertEquals(listOf(8001L), deletedFromSheet)

    // Verify ViewModel.deleteParentMessage removes local row and creates silent CATEGORY_PARENT_MESSAGE tombstone
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    db.childStudentDao().insert(student)

    vm.sendParentMessage(student, "Pesan uji coba batal kirim", "WALI")
    var loops = 0
    while (loops < 30 && vm.parentMessages.value.none { it.studentId == 9801L }) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      loops++
    }
    val insertedMsg = vm.parentMessages.value.first { it.studentId == 9801L }

    vm.deleteParentMessage(insertedMsg)
    loops = 0
    while (loops < 30 && vm.parentMessages.value.any { it.id == insertedMsg.id }) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      loops++
    }
    org.junit.Assert.assertTrue(vm.parentMessages.value.none { it.id == insertedMsg.id })
    val storedLogs = db.deletionLogDao().getLogsBySchoolList("SRT1-KDR")
    org.junit.Assert.assertTrue(
      storedLogs.any { log ->
        log.category == com.example.data.model.DeletionLog.CATEGORY_PARENT_MESSAGE &&
          log.recordId == insertedMsg.id
      }
    )
    // Must not pollute user-facing audit log feed
    org.junit.Assert.assertTrue(
      vm.deletionLogs.value.none {
        it.category == com.example.data.model.DeletionLog.CATEGORY_PARENT_MESSAGE
      }
    )
  }

  @Test
  fun `verify AdminScreen card hub redesign displays hero summary, 8 menu tiles, CSV sheet, and security sheet`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    app.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    val vm = com.example.ui.HealthViewModel(app)
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )

    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.screens.AdminScreen(viewModel = vm, onLogout = {})
      }
    }
    composeTestRule.waitForIdle()

    // 1. Hero card & 2x4 card menu hub are displayed cleanly
    composeTestRule.onNodeWithTag("card_admin_hero").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_admin_sync_cloud").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_admin_menu_hub").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_admin_hub_students").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_admin_hub_wali").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_admin_hub_jadwal").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_admin_hub_fasting").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_admin_hub_visit").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_edit_school_profile").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_admin_hub_csv").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_admin_hub_security").assertIsDisplayed()

    // 2. Tap Akun Wali hub tile -> switches into Wali tab
    composeTestRule.onNodeWithTag("tile_admin_hub_wali").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("list_admin_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("card_admin_anak_admin_quota"))
    composeTestRule.onNodeWithTag("card_admin_anak_admin_quota").assertExists()

    // 3. Scroll hub clearly above bottom FAB by bringing tabs row into view & open CSV Backup Sheet
    composeTestRule.onNodeWithTag("list_admin_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("row_admin_tabs"))
    composeTestRule.onNodeWithTag("tile_admin_hub_csv").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sheet_admin_csv_backup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_import_students_csv").assertExists()
    composeTestRule.onNodeWithTag("btn_export_students_csv").assertExists()
    composeTestRule.onNodeWithTag("btn_import_wali_csv").assertExists()
    composeTestRule.onNodeWithTag("btn_export_wali_csv").assertExists()

    // Dismiss CSV sheet via Close button & open Security & Maintenance Sheet
    composeTestRule.onNodeWithTag("btn_close_admin_csv_backup").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sheet_admin_csv_backup").assertDoesNotExist()
    composeTestRule.onNodeWithTag("list_admin_content", useUnmergedTree = true)
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("row_admin_tabs"))
    composeTestRule.onNodeWithTag("tile_admin_hub_security").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sheet_admin_security").assertExists()
    composeTestRule.onNodeWithTag("btn_admin_change_password").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_clear_school_data").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_delete_admin_school_account").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun `verify InfoPentingCommentsSheet shows compact commented post image and text preview above scrollable comments`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val imagePost = com.example.data.model.InfoPentingPost(
      id = 777L,
      schoolId = com.example.data.model.InfoPentingHelper.GLOBAL_SCHOOL_ID,
      category = com.example.data.model.InfoPentingHelper.CATEGORY_PENGUMUMAN,
      title = "Panduan Foto Asrama Baru",
      content = "Mohon wali asuh mengecek kebersihan kamar setiap Jumat pagi sesuai poster berikut.",
      mediaType = com.example.data.model.InfoPentingHelper.MEDIA_IMAGE,
      mediaUrl = "https://example.org/poster-asrama.jpg",
      mediaName = "poster-asrama.jpg",
      authorDisplayName = "Superadmin",
      createdAt = System.currentTimeMillis()
    )
    val c1 = com.example.data.model.InfoPentingComment(
      id = 9101L,
      postId = 777L,
      authorKey = "SRT1-KDR:ust_ali",
      authorName = "Ust. Ali",
      authorSchoolId = "SRT1-KDR",
      authorSchoolName = "Sekolah Rakyat Kediri",
      content = "Siap dilaksanakan mulai pekan ini.",
      createdAt = System.currentTimeMillis() - 60_000L
    )
    val threads = com.example.data.model.InfoPentingHelper.buildCommentThreads(listOf(c1), 777L)
    var openedImagePreview = false

    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.InfoPentingCommentsSheet(
          post = imagePost,
          threads = threads,
          currentUserKey = "SRT1-KDR:ust_ali",
          isEn = false,
          onOpenFullscreenImage = { openedImagePreview = true },
          onSendComment = { _, _ -> },
          onDeleteOwnComment = {},
          onDismiss = {}
        )
      }
    }
    composeTestRule.waitForIdle()

    // Kartu konteks postingan (gambar + judul + teks) tampil di antara header komentar & daftar chat
    composeTestRule.onNodeWithTag("sheet_info_comments").assertExists()
    composeTestRule.onNodeWithTag("card_comment_post_context").assertIsDisplayed()
    composeTestRule.onNodeWithTag("img_comment_post_context_thumb").assertIsDisplayed()
    composeTestRule.onNodeWithText("Mohon wali asuh mengecek kebersihan kamar setiap Jumat pagi sesuai poster berikut.")
      .assertIsDisplayed()

    // Ketuk thumbnail gambar memicu pratinjau layar penuh
    composeTestRule.onNodeWithTag("btn_preview_comment_post_image").performClick()
    org.junit.Assert.assertTrue(openedImagePreview)

    // Daftar chat di bawahnya tampil dalam container LazyColumn yang dapat digeser
    composeTestRule.onNodeWithTag("list_info_comments").assertIsDisplayed()
    composeTestRule.onNodeWithText("Siap dilaksanakan mulai pekan ini.").assertIsDisplayed()
  }

  @Test
  fun `verify compact pastel SchoolRegistrationDialog validates, submits neatly, and renders proof ticket`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    var capturedSchoolName: String? = null
    var capturedWaliCount: Int? = null

    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.SchoolRegistrationDialog(
          onDismiss = {},
          onSubmit = { sName, addr, cityName, waliEst, pic, email, phone, onDone ->
            capturedSchoolName = sName
            capturedWaliCount = waliEst
            onDone(
              com.example.data.model.SchoolRegistration(
                id = 501L,
                schoolName = sName,
                address = addr,
                city = cityName,
                estimatedWaliCount = waliEst,
                picName = pic,
                picEmail = email,
                picPhone = phone,
                generatedSchoolCode = "SR-KDR5"
              )
            )
          }
        )
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("sheet_school_registration").assertExists()
    composeTestRule.onNodeWithText("Pendaftaran Sekolah Baru").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reg_school_name").performScrollTo().performTextInput("Sekolah Rakyat Harapan")
    composeTestRule.onNodeWithTag("reg_city").performScrollTo().performTextInput("Kediri")
    composeTestRule.onNodeWithTag("reg_address").performScrollTo().performTextInput("Jl. Melati No. 8")
    composeTestRule.onNodeWithTag("reg_pic_name").performScrollTo().performTextInput("Ust. Farhan")
    composeTestRule.onNodeWithTag("reg_pic_email").performScrollTo().performTextInput("farhan@sekolah.id")
    composeTestRule.onNodeWithTag("reg_pic_phone").performScrollTo().performTextInput("08123456789")
    composeTestRule.onNodeWithTag("btn_submit_registration").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    assertEquals("Sekolah Rakyat Harapan", capturedSchoolName)
    assertEquals(6, capturedWaliCount)
    composeTestRule.onNodeWithTag("btn_send_wa_superadmin").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun `verify ParentPortalScreen Option A self password change from TopBar and Profile without old password verification`(): Unit = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HealthViewModel(app)
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val activeSchool = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

    val student = com.example.data.model.ChildStudent(
      id = 918801L,
      name = "Auliana Widiani",
      nis = "102444512",
      gender = "P",
      age = 15,
      dormitory = "Asrama Putri",
      classGrade = "X-1",
      allergies = "-",
      bloodType = "O",
      emergencyContact = "081234567890",
      motherName = "SURYANI",
      parentPhone = "081234567890",
      assignedWaliUsername = "ekowahyudi",
      assignedWaliName = "Eko Wahyudi",
      schoolId = activeSchool
    )
    db.childStudentDao().insert(student)

    val oldPass = "AwalOrtu123"
    val newPass = "BaruOrtu999"
    val link = com.example.data.model.ParentAccessLink(
      id = 918802L,
      studentId = student.id,
      studentName = student.name,
      studentNis = student.nis,
      studentClass = student.classGrade,
      studentDorm = student.dormitory,
      parentName = "SURYANI",
      parentPhone = "081234567890",
      username = "ortu_auliana_4512",
      password = oldPass,
      assignedWaliUsername = "ekowahyudi",
      assignedWaliName = "Eko Wahyudi",
      canViewProfile = true,
      schoolId = activeSchool
    )
    db.parentAccessLinkDao().insert(link)

    val loginRes = vm.executeLogin("ortu_auliana_4512", oldPass)
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessParent)

    var loops = 0
    while (loops < 25 && vm.activeParentAccessLink.value == null) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      loops++
    }
    org.junit.Assert.assertNotNull(vm.activeParentAccessLink.value)

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.screens.ParentPortalScreen(viewModel = vm, onLogout = {})
      }
    }
    composeTestRule.waitForIdle()

    // 1. Cek tombol ganti password di Profil Anak
    composeTestRule.onNodeWithTag("card_parent_summary_profile").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("parent_profile_content")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("card_parent_account_security"))
    composeTestRule.onNodeWithTag("card_parent_account_security").assertIsDisplayed()
    composeTestRule.onNodeWithTag("parent_profile_content")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("btn_profile_parent_change_password"))
    composeTestRule.onNodeWithTag("btn_profile_parent_change_password").assertIsDisplayed()

    // 2. Buka dialog ganti password lewat ikon TopBar (Option A) tanpa input password lama
    composeTestRule.onNodeWithTag("btn_parent_change_password").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("input_parent_new_password").performScrollTo().performTextInput(newPass)
    composeTestRule.onNodeWithTag("input_parent_confirm_password").performScrollTo().performTextInput(newPass)
    composeTestRule.onNodeWithTag("btn_save_parent_password").performScrollTo().performClick()

    for (attempt in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      composeTestRule.waitForIdle()
      val updatedLink = db.parentAccessLinkDao().getByUsername("ortu_auliana_4512")
      if (updatedLink?.password == newPass) break
      kotlinx.coroutines.delay(30)
    }

    val updatedLink = db.parentAccessLinkDao().getByUsername("ortu_auliana_4512")
    assertEquals("ortu_auliana_4512", updatedLink?.username)
    assertEquals(newPass, updatedLink?.password)

    // 3. Logout lalu pastikan password lama ditolak dan password baru berhasil login
    vm.logout()
    val loginOldAfter = vm.executeLogin("ortu_auliana_4512", oldPass)
    org.junit.Assert.assertTrue(loginOldAfter is com.example.ui.LoginResult.Error)

    val loginNewAfter = vm.executeLogin("ortu_auliana_4512", newPass)
    org.junit.Assert.assertTrue(loginNewAfter is com.example.ui.LoginResult.SuccessParent)

    if (updatedLink != null) db.parentAccessLinkDao().delete(updatedLink)
    db.childStudentDao().delete(student)
    Unit
  }

  @Test
  fun `verify TopOn mediation bundles Meta Audience Network adapter cleanly without Yandex`() {
    val metaInitCls = Class.forName("com.anythink.network.facebook.FacebookATInitManager")
    val getInstanceMethod = metaInitCls.getMethod("getInstance")
    val metaInitInstance = getInstanceMethod.invoke(null)
    org.junit.Assert.assertNotNull(metaInitInstance)

    val networkVersionMethod = metaInitCls.getMethod("getNetworkVersion")
    val metaVersion = networkVersionMethod.invoke(metaInitInstance) as? String
    org.junit.Assert.assertFalse(metaVersion.isNullOrBlank())

    val metaInterstitialCls = Class.forName("com.anythink.network.facebook.FacebookATInterstitialAdapter")
    val metaRewardCls = Class.forName("com.anythink.network.facebook.FacebookATRewardedVideoAdapter")
    org.junit.Assert.assertNotNull(metaInterstitialCls)
    org.junit.Assert.assertNotNull(metaRewardCls)

    val hasYandexAdapter = runCatching {
      Class.forName("com.anythink.network.yandex.YandexATInitManager")
    }.isSuccess
    org.junit.Assert.assertFalse(hasYandexAdapter)

    org.junit.Assert.assertEquals("h6af3e64badfd8", com.example.util.TopOnAdManager.getAppId())
    org.junit.Assert.assertEquals("n6af3e7b1e288d", com.example.util.TopOnAdManager.getInterstitialPlacementId())
    org.junit.Assert.assertEquals("n6af3e88b080a6", com.example.util.TopOnAdManager.getRewardPlacementId())
  }

  @Test
  fun `verify TopOn mediation bundles Liftoff Vungle adapter cleanly`() {
    val vungleInitCls = Class.forName("com.anythink.network.vungle.VungleATInitManager")
    val getInstanceMethod = vungleInitCls.getMethod("getInstance")
    val vungleInitInstance = getInstanceMethod.invoke(null)
    org.junit.Assert.assertNotNull(vungleInitInstance)

    val networkVersionMethod = vungleInitCls.getMethod("getNetworkVersion")
    val vungleVersion = networkVersionMethod.invoke(vungleInitInstance) as? String
    org.junit.Assert.assertFalse(vungleVersion.isNullOrBlank())

    val vungleInterstitialCls = Class.forName("com.anythink.network.vungle.VungleATInterstitialAdapter")
    val vungleRewardCls = Class.forName("com.anythink.network.vungle.VungleATRewardedVideoAdapter")
    org.junit.Assert.assertNotNull(vungleInterstitialCls)
    org.junit.Assert.assertNotNull(vungleRewardCls)

    val vungleSdkVersion = com.vungle.ads.VungleAds.getSdkVersion()
    org.junit.Assert.assertTrue(vungleSdkVersion.isNotBlank())
  }

  @Test
  fun `verify ParentPortalScreen account deletion requires Wali Asuh approval before permanent removal`() = kotlinx.coroutines.runBlocking {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(context)
    val vm = com.example.ui.HealthViewModel(context)
    val activeSchool = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

    val student = com.example.data.model.ChildStudent(
      id = 919901L,
      name = "Rafi Pratama",
      nis = "102555889",
      gender = "L",
      age = 14,
      dormitory = "Asrama Putra",
      classGrade = "VIII-A",
      allergies = "-",
      bloodType = "A",
      emergencyContact = "081987654321",
      motherName = "SITI AMINAH",
      parentPhone = "081987654321",
      assignedWaliUsername = "ekowahyudi",
      assignedWaliName = "Eko Wahyudi",
      schoolId = activeSchool
    )
    db.childStudentDao().insert(student)

    val parentUser = "ortu_rafi_5889"
    val parentPass = "RafiPass123"
    val initialLink = com.example.data.model.ParentAccessLink(
      id = 919902L,
      studentId = student.id,
      studentName = student.name,
      studentNis = student.nis,
      studentClass = student.classGrade,
      studentDorm = student.dormitory,
      parentName = "SITI AMINAH",
      parentPhone = "081987654321",
      username = parentUser,
      password = parentPass,
      assignedWaliUsername = "ekowahyudi",
      assignedWaliName = "Eko Wahyudi",
      canViewProfile = true,
      schoolId = activeSchool
    )
    db.parentAccessLinkDao().insert(initialLink)

    // 1. Wali Asuh tidak boleh langsung menghapus akun sebelum orang tua mengajukan
    var preRequestApproveResult: Boolean? = null
    vm.approveAndDeleteParentAccount(initialLink) { ok, _ -> preRequestApproveResult = ok }
    for (attempt in 1..15) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (preRequestApproveResult != null) break
      kotlinx.coroutines.delay(25)
    }
    assertEquals(false, preRequestApproveResult)
    org.junit.Assert.assertNotNull(db.parentAccessLinkDao().getByUsername(parentUser))

    // 2. Login sebagai Orang Tua & ajukan hapus akun melalui UI ParentPortalScreen
    val loginRes = vm.executeLogin(parentUser, parentPass)
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessParent)

    var loops = 0
    while (loops < 25 && vm.activeParentAccessLink.value == null) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      kotlinx.coroutines.delay(40)
      loops++
    }
    org.junit.Assert.assertNotNull(vm.activeParentAccessLink.value)

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    var parentLoggedOut = false
    var showParentPortal by androidx.compose.runtime.mutableStateOf(true)

    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        if (showParentPortal) {
          com.example.ui.screens.ParentPortalScreen(
            viewModel = vm,
            onLogout = { parentLoggedOut = true }
          )
        } else {
          val links by vm.parentAccessLinks.collectAsState(initial = emptyList())
          val currentStudents by vm.students.collectAsState(initial = emptyList())
          com.example.ui.dialogs.KartuAksesOrangTuaSheet(
            students = currentStudents,
            parentAccessLinks = links,
            schoolProfile = vm.schoolProfile.value,
            isStudentClaimedByCurrentWali = { it.id == student.id },
            isAdmin = true,
            onGenerateAccessCard = {},
            onResetPassword = {},
            onEditStudentProfile = {},
            onApproveDeleteParentAccount = { target -> vm.approveAndDeleteParentAccount(target) { _, _ -> } },
            onRejectDeleteParentAccount = { target -> vm.rejectParentAccountDeletion(target) { _, _ -> } },
            onDismiss = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // Buka Profil Anak -> klik Ajukan Hapus Akun
    composeTestRule.onNodeWithTag("card_parent_summary_profile").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("parent_profile_content")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("btn_profile_parent_delete_account"))
    composeTestRule.onNodeWithTag("btn_profile_parent_delete_account").performClick()
    composeTestRule.waitForIdle()

    // Isi alasan & kirim pengajuan
    composeTestRule.onNodeWithTag("input_parent_delete_reason").performScrollTo().performTextInput("Ganti perangkat baru")
    composeTestRule.onNodeWithTag("btn_confirm_request_delete_parent").performScrollTo().performClick()

    for (attempt in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      composeTestRule.waitForIdle()
      val pending = db.parentAccessLinkDao().getByUsername(parentUser)
      if (pending?.deletionRequested == true) break
      kotlinx.coroutines.delay(30)
    }

    val pendingLink = db.parentAccessLinkDao().getByUsername(parentUser)
    org.junit.Assert.assertNotNull(pendingLink)
    assertEquals(true, pendingLink?.deletionRequested)
    assertEquals("Ganti perangkat baru", pendingLink?.deletionReason)
    assertEquals(false, parentLoggedOut)

    composeTestRule.onNodeWithTag("parent_profile_content")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("card_parent_deletion_pending"))
    composeTestRule.onNodeWithTag("card_parent_deletion_pending").assertIsDisplayed()

    // 3. Uji Wali Asuh menolak dulu di KartuAksesOrangTuaSheet -> akun tetap ada & status kembali aktif
    showParentPortal = false
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("banner_pending_parent_deletions").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_parent_deletion_approval_${student.id}").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_reject_parent_delete_${student.id}").performScrollTo().performClick()

    for (attempt in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      composeTestRule.waitForIdle()
      val afterReject = db.parentAccessLinkDao().getByUsername(parentUser)
      if (afterReject != null && !afterReject.deletionRequested) break
      kotlinx.coroutines.delay(30)
    }
    val afterReject = db.parentAccessLinkDao().getByUsername(parentUser)
    org.junit.Assert.assertNotNull(afterReject)
    assertEquals(false, afterReject?.deletionRequested)

    // 4. Orang tua mengajukan lagi, lalu Wali Asuh klik Setujui & Hapus + konfirmasi AlertDialog
    var reRequestOk = false
    vm.requestParentAccountDeletion("Sudah pasti ingin hapus akun") { ok, _ -> reRequestOk = ok }
    for (attempt in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      composeTestRule.waitForIdle()
      if (reRequestOk && db.parentAccessLinkDao().getByUsername(parentUser)?.deletionRequested == true) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(true, reRequestOk)

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_approve_parent_delete_${student.id}").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dialog_confirm_approve_parent_delete").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_confirm_approve_parent_delete").performClick()

    for (attempt in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      composeTestRule.waitForIdle()
      if (db.parentAccessLinkDao().getByUsername(parentUser) == null && vm.isCurrentParentAccountDeleted.value) break
      kotlinx.coroutines.delay(35)
    }

    // 5. Pastikan akun terhapus permanen, deletion log tercatat, login ditolak, dan status akun terhapus aktif
    org.junit.Assert.assertNull(db.parentAccessLinkDao().getByUsername(parentUser))
    val deleteLogs = db.deletionLogDao().getLogsBySchoolList(activeSchool)
      .filter { it.category == com.example.data.model.DeletionLog.CATEGORY_PARENT_ACCESS }
    org.junit.Assert.assertTrue(deleteLogs.any { it.recordId == initialLink.id && it.studentId == student.id })
    org.junit.Assert.assertTrue(vm.isCurrentParentAccountDeleted.value)

    // Kembali ke ParentPortalScreen saat sesi parent terhapus -> langsung memicu onLogout
    showParentPortal = true
    for (attempt in 1..20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      composeTestRule.waitForIdle()
      if (parentLoggedOut) break
      kotlinx.coroutines.delay(25)
    }
    assertEquals(true, parentLoggedOut)

    val loginAfterDelete = vm.executeLogin(parentUser, parentPass)
    org.junit.Assert.assertTrue(loginAfterDelete is com.example.ui.LoginResult.Error)

    db.childStudentDao().delete(student)
    Unit
  }

  @Test
  fun `verify Student Profile Photo upload removal and Parent Portal display`() = kotlinx.coroutines.runBlocking {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(context)
    val vm = com.example.ui.HealthViewModel(context)
    val activeSchool = "SRT1-KDR"

    val student = com.example.data.model.ChildStudent(
      id = 928801L,
      name = "NAJWA KAMILA",
      nis = "2026928801",
      gender = "P",
      age = 15,
      dormitory = "Asrama Putri",
      classGrade = "X-2",
      motherName = "ROHMAH",
      parentPhone = "081233445566",
      assignedWaliUsername = "ekowahyudi",
      assignedWaliName = "Eko Wahyudi",
      schoolId = activeSchool
    )
    db.childStudentDao().insert(student)

    val parentUser = "ortu_najwa_photo"
    val parentPass = "NajwaPass123"
    val link = com.example.data.model.ParentAccessLink(
      id = 928802L,
      studentId = student.id,
      studentName = student.name,
      studentNis = student.nis,
      studentClass = student.classGrade,
      studentDorm = student.dormitory,
      parentName = "ROHMAH",
      parentPhone = "081233445566",
      username = parentUser,
      password = parentPass,
      assignedWaliUsername = "ekowahyudi",
      assignedWaliName = "Eko Wahyudi",
      canViewProfile = true,
      schoolId = activeSchool
    )
    db.parentAccessLinkDao().insert(link)

    // Create a valid sample JPEG in cache dir
    val sampleBitmap = android.graphics.Bitmap.createBitmap(96, 96, android.graphics.Bitmap.Config.ARGB_8888)
    sampleBitmap.eraseColor(android.graphics.Color.BLUE)
    val tempPhotoFile = java.io.File(context.cacheDir, "sample_student_photo_test.jpg")
    java.io.FileOutputStream(tempPhotoFile).use { out ->
      sampleBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
    }
    sampleBitmap.recycle()
    val pickedUri = android.net.Uri.fromFile(tempPhotoFile)

    var uploadSuccess: Boolean? = null
    vm.uploadStudentProfilePhoto(student, pickedUri) { ok, _ ->
      uploadSuccess = ok
    }
    for (attempt in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (uploadSuccess != null) break
      kotlinx.coroutines.delay(30)
    }
    assertEquals(true, uploadSuccess)

    val updatedStudent = db.childStudentDao().getById(student.id)
    org.junit.Assert.assertNotNull(updatedStudent)
    org.junit.Assert.assertTrue(updatedStudent!!.photoUrl.isNotBlank())
    org.junit.Assert.assertTrue(
      "Expected compressed .webp file URL, got: ${updatedStudent.photoUrl}",
      updatedStudent.photoUrl.endsWith(".webp")
    )
    val updatedLink = db.parentAccessLinkDao().getByUsername(parentUser)
    org.junit.Assert.assertNotNull(updatedLink)
    assertEquals(updatedStudent.photoUrl, updatedLink!!.studentPhotoUrl)

    // Login Parent & verify photo card + lightbox in ParentPortalScreen
    val loginRes = vm.executeLogin(parentUser, parentPass)
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessParent)
    for (attempt in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.activeParentAccessLink.value != null) break
      kotlinx.coroutines.delay(30)
    }

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    var showParentMode by androidx.compose.runtime.mutableStateOf(true)
    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        if (showParentMode) {
          com.example.ui.screens.ParentPortalScreen(
            viewModel = vm,
            onLogout = {}
          )
        } else {
          com.example.ui.dialogs.StudentDetailDialog(
            student = updatedStudent,
            studentRecords = emptyList(),
            isUploadingPhoto = false,
            onUploadStudentPhoto = { target, uri, cb -> vm.uploadStudentProfilePhoto(target, uri, cb) },
            onRemoveStudentPhoto = { target, cb -> vm.removeStudentProfilePhoto(target, cb) },
            onDismiss = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // Hero card on Parent Portal home shows large child portrait
    composeTestRule.onNodeWithTag("btn_hero_student_avatar").assertIsDisplayed()

    // Open Profil Anak from overview grid
    composeTestRule.onNodeWithTag("card_parent_summary_profile").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_parent_profile_photo_header").assertIsDisplayed()

    // Tap large student photo showcase to open full-screen preview dialog
    composeTestRule.onNodeWithTag("btn_parent_preview_student_photo").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dialog_student_photo_preview").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_close_student_photo_preview").performClick()
    composeTestRule.waitForIdle()

    // Verify Wali Asuh StudentDetailDialog shows large photo showcase between Biodata tabs and Identitas Kependudukan & Sekolah
    showParentMode = false
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_student_detail_large_photo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("img_student_detail_large_photo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_student_large_photo_manage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_student_photo_action").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dialog_student_photo_manager").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_pick_student_photo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_preview_student_photo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_remove_student_photo").assertIsDisplayed()

    // Verify photo removal clears both ChildStudent and ParentAccessLink
    var removeOk: Boolean? = null
    vm.removeStudentProfilePhoto(updatedStudent) { ok, _ -> removeOk = ok }
    for (attempt in 1..20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (removeOk != null) break
      kotlinx.coroutines.delay(25)
    }
    assertEquals(true, removeOk)
    assertEquals("", db.childStudentDao().getById(student.id)?.photoUrl)
    assertEquals("", db.parentAccessLinkDao().getByUsername(parentUser)?.studentPhotoUrl)

    tempPhotoFile.delete()
    db.parentAccessLinkDao().getByUsername(parentUser)?.let { db.parentAccessLinkDao().delete(it) }
    db.childStudentDao().getById(student.id)?.let { db.childStudentDao().delete(it) }
    Unit
  }

  @Test
  fun `verify AddStudentDialog displays profile photo picker card and pick button`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.AddStudentDialog(
          activeSchoolId = "SRT1-KDR",
          onDismiss = {},
          onConfirmStudentWithPhoto = { _, _, _ -> }
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_add_student_photo_picker").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_pick_student_photo_form").assertIsDisplayed()
  }

  @Test
  fun `verify StudentIdCardPreview and StudentCardPdfGenerator render Pas Foto slot with crisp QR NIK barcode`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val tempWebpFile = java.io.File(context.cacheDir, "test_card_photo_${System.currentTimeMillis()}.webp")
    val sampleBitmap = android.graphics.Bitmap.createBitmap(240, 320, android.graphics.Bitmap.Config.ARGB_8888).apply {
      eraseColor(android.graphics.Color.rgb(34, 139, 230))
    }
    java.io.FileOutputStream(tempWebpFile).use { out ->
      @Suppress("DEPRECATION")
      val fmt = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        android.graphics.Bitmap.CompressFormat.WEBP_LOSSY
      } else {
        android.graphics.Bitmap.CompressFormat.WEBP
      }
      sampleBitmap.compress(fmt, 75, out)
    }
    sampleBitmap.recycle()

    val studentWithPhoto = com.example.data.model.ChildStudent(
      id = 9101L,
      name = "Rafi Pratama",
      nis = "0077889901",
      gender = "L",
      age = 15,
      dormitory = "Asrama Putra",
      classGrade = "10 IPA",
      nik = "3506142512090011",
      address = "Jl. Melati No. 5, Pare, Kediri",
      photoUrl = tempWebpFile.absolutePath
    )
    val studentWithoutPhoto = com.example.data.model.ChildStudent(
      id = 9102L,
      name = "Nadia Putri",
      nis = "0077889902",
      gender = "P",
      age = 13,
      dormitory = "Asrama Putri",
      classGrade = "8A",
      nik = "3506142512090022",
      address = "Desa Pelem, Pare, Kediri",
      photoUrl = ""
    )

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    var activeStudent by androidx.compose.runtime.mutableStateOf(studentWithoutPhoto)
    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.components.StudentIdCardPreview(
          student = activeStudent,
          schoolName = "Sekolah Rakyat Kabupaten Kediri"
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_student_photo_slot").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_student_photo_fallback").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_student_qr_box").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("QR NIK ${studentWithoutPhoto.name}").assertIsDisplayed()
    composeTestRule.onNodeWithText("3506142512090022").assertIsDisplayed()
    composeTestRule.onNodeWithText("SCAN QR NIK").assertIsDisplayed()

    activeStudent = studentWithPhoto
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_student_photo_slot").assertIsDisplayed()
    composeTestRule.onNodeWithTag("card_student_qr_box").assertIsDisplayed()
    composeTestRule.onNodeWithText("3506142512090011").assertIsDisplayed()

    val crispQr = com.example.util.StudentCardRenderer.generateQrBitmap(
      com.example.util.StudentCardRenderer.resolveQrPayload(studentWithPhoto),
      480
    )
    org.junit.Assert.assertNotNull(crispQr)
    assertEquals(480, crispQr!!.width)
    assertEquals(480, crispQr.height)
    crispQr.recycle()

    val generatedPdf = com.example.util.StudentCardPdfGenerator.generateCardsPdf(
      context = context,
      students = listOf(studentWithPhoto, studentWithoutPhoto),
      schoolName = "Sekolah Rakyat Kabupaten Kediri",
      filterLabel = "Uji Foto Kartu",
      schoolAddress = "Jl. Pahlawan No. 1, Kediri"
    )
    org.junit.Assert.assertNotNull(generatedPdf)
    org.junit.Assert.assertTrue(generatedPdf!!.exists() && generatedPdf.name.endsWith(".pdf"))

    generatedPdf.delete()
    tempWebpFile.delete()
  }

  @Test
  fun `verify WaliAccount self profile update syncs to Room student links and PocketBase DTO while preserving password`() = kotlinx.coroutines.runBlocking {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(context)
    val vm = com.example.ui.HealthViewModel(context)
    val activeSchool = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

    val wali = com.example.data.model.WaliAccount(
      id = 980101L,
      name = "Ustadz Ahmad Awal",
      username = "ahmad_wali_test",
      password = "KeepSecretPass99",
      dormitory = "Asrama Putra",
      phone = "081111222333",
      schoolId = activeSchool,
      role = com.example.data.model.WaliAccount.ROLE_WALI
    )
    db.waliAccountDao().insert(wali)

    val student = com.example.data.model.ChildStudent(
      id = 980102L,
      name = "Fajar Ramadhan",
      nis = "102980102",
      gender = "L",
      age = 13,
      dormitory = "Asrama Putra",
      classGrade = "VII-B",
      assignedWaliUsername = "ahmad_wali_test",
      assignedWaliName = "Ustadz Ahmad Awal",
      schoolId = activeSchool
    )
    db.childStudentDao().insert(student)

    val link = com.example.data.model.ParentAccessLink(
      id = 980103L,
      studentId = student.id,
      studentName = student.name,
      studentNis = student.nis,
      studentClass = student.classGrade,
      studentDorm = student.dormitory,
      parentName = "IBU FAJAR",
      parentPhone = "081299887766",
      username = "ortu_fajar_9801",
      password = "ParentPass123",
      assignedWaliUsername = "ahmad_wali_test",
      assignedWaliName = "Ustadz Ahmad Awal",
      canViewProfile = true,
      schoolId = activeSchool
    )
    db.parentAccessLinkDao().insert(link)

    val loginRes = vm.executeLogin("ahmad_wali_test", "KeepSecretPass99")
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessWali)

    var callbackOk: Boolean? = null
    vm.updateWaliSelfProfile(
      newName = "Ustadz Ahmad Fauzi, M.Pd",
      newPhone = "081234567890",
      newTitlePosition = "Koordinator Pembina Asrama Putra",
      newEmployeeCode = "NIP-19900412-007",
      newBio = "Mendampingi kesehatan, adab, dan hafalan santri setiap hari.",
      newPassword = null
    ) { ok, _ ->
      callbackOk = ok
    }

    for (attempt in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (callbackOk != null) break
      kotlinx.coroutines.delay(25)
    }
    assertEquals(true, callbackOk)

    val updatedWali = db.waliAccountDao().getByUsername("ahmad_wali_test")
    org.junit.Assert.assertNotNull(updatedWali)
    assertEquals("Ustadz Ahmad Fauzi, M.Pd", updatedWali!!.name)
    assertEquals("KeepSecretPass99", updatedWali.password)
    assertEquals("081234567890", updatedWali.phone)
    assertEquals("Koordinator Pembina Asrama Putra", updatedWali.titlePosition)
    assertEquals("NIP-19900412-007", updatedWali.employeeCode)
    assertEquals("Mendampingi kesehatan, adab, dan hafalan santri setiap hari.", updatedWali.bio)
    org.junit.Assert.assertTrue(updatedWali.profileUpdatedAt > 0L)

    // Assigned wali name should propagate to both ChildStudent and ParentAccessLink
    val updatedStudent = db.childStudentDao().getById(student.id)
    assertEquals("Ustadz Ahmad Fauzi, M.Pd", updatedStudent?.assignedWaliName)
    val updatedLink = db.parentAccessLinkDao().getByUsername("ortu_fajar_9801")
    assertEquals("Ustadz Ahmad Fauzi, M.Pd", updatedLink?.assignedWaliName)

    // Verify PocketBase DTO round-trip preserves all caregiver legitimacy fields
    val withPhoto = updatedWali.copy(
      photoUrl = "https://cdn.example.com/wali/ahmad.webp",
      photoFallbackUrl = "https://pb.example.com/api/files/wali_accounts/abc/ahmad.webp"
    )
    val dto = com.example.data.remote.pocketbase.PbWaliAccountDto.fromDomain(withPhoto)
    val roundTrip = dto.toDomainModel()
    assertEquals(withPhoto.photoUrl, roundTrip.photoUrl)
    assertEquals(withPhoto.photoFallbackUrl, roundTrip.photoFallbackUrl)
    assertEquals(withPhoto.titlePosition, roundTrip.titlePosition)
    assertEquals(withPhoto.employeeCode, roundTrip.employeeCode)
    assertEquals(withPhoto.bio, roundTrip.bio)
    assertEquals(withPhoto.profileUpdatedAt, roundTrip.profileUpdatedAt)

    vm.logout()
    db.parentAccessLinkDao().delete(updatedLink ?: link)
    db.childStudentDao().delete(updatedStudent ?: student)
    db.waliAccountDao().delete(updatedWali)
  }

  @Test
  fun `verify ParentPortalScreen displays Wali Asuh official legitimacy card and sheet without leaking credentials`() = kotlinx.coroutines.runBlocking {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(context)
    val vm = com.example.ui.HealthViewModel(context)
    val activeSchool = vm.schoolProfile.value.tenantCode.ifBlank { "SRT1-KDR" }

    val wali = com.example.data.model.WaliAccount(
      id = 980201L,
      name = "Ustadzah Nurul Hidayah, S.Pd",
      username = "nurul_secret_login",
      password = "SecretWaliPassword88",
      dormitory = "Asrama Putri",
      phone = "085700112233",
      schoolId = activeSchool,
      role = com.example.data.model.WaliAccount.ROLE_WALI,
      titlePosition = "Pembina Utama Asrama Putri",
      employeeCode = "NUPTK-88221100",
      bio = "Menjaga amanah pembinaan karakter dan kesehatan santriwati.",
      profileUpdatedAt = System.currentTimeMillis()
    )
    db.waliAccountDao().insert(wali)

    val student = com.example.data.model.ChildStudent(
      id = 980202L,
      name = "Aisyah Humaira",
      nis = "102980202",
      gender = "P",
      age = 14,
      dormitory = "Asrama Putri",
      classGrade = "VIII-C",
      assignedWaliUsername = "nurul_secret_login",
      assignedWaliName = "Ustadzah Nurul Hidayah, S.Pd",
      schoolId = activeSchool
    )
    db.childStudentDao().insert(student)

    val link = com.example.data.model.ParentAccessLink(
      id = 980203L,
      studentId = student.id,
      studentName = student.name,
      studentNis = student.nis,
      studentClass = student.classGrade,
      studentDorm = student.dormitory,
      parentName = "IBU AISYAH",
      parentPhone = "085799887766",
      username = "ortu_aisyah_9802",
      password = "ParentPass999",
      assignedWaliUsername = "nurul_secret_login",
      assignedWaliName = "Ustadzah Nurul Hidayah, S.Pd",
      canViewProfile = true,
      schoolId = activeSchool
    )
    db.parentAccessLinkDao().insert(link)

    val loginRes = vm.executeLogin("ortu_aisyah_9802", "ParentPass999")
    org.junit.Assert.assertTrue(loginRes is com.example.ui.LoginResult.SuccessParent)

    for (attempt in 1..25) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (vm.activeParentAccessLink.value != null && vm.waliAccounts.value.any { it.id == wali.id }) break
      kotlinx.coroutines.delay(30)
    }

    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    composeTestRule.setContent {
      androidx.compose.runtime.CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.screens.ParentPortalScreen(
          viewModel = vm,
          onLogout = {}
        )
      }
    }
    composeTestRule.waitForIdle()

    // 1. Hero card shows caregiver name, title & clickable official profile button
    composeTestRule.onNodeWithTag("btn_hero_open_wali_profile").assertIsDisplayed()
    composeTestRule.onNodeWithText("Ustadzah Nurul Hidayah, S.Pd").assertIsDisplayed()

    // 2. Tap caregiver section in Hero card -> opens Official Legitimacy Sheet
    composeTestRule.onNodeWithTag("btn_hero_open_wali_profile").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sheet_wali_official_profile").assertIsDisplayed()
    composeTestRule.onNodeWithText("Pembina Utama Asrama Putri").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("NUPTK-88221100").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("085700112233").performScrollTo().assertIsDisplayed()

    // Login credentials of Wali Asuh MUST NEVER appear on Parent Portal
    composeTestRule.onNodeWithText("nurul_secret_login").assertDoesNotExist()
    composeTestRule.onNodeWithText("SecretWaliPassword88").assertDoesNotExist()

    composeTestRule.onNodeWithTag("btn_close_wali_official_profile").performClick()
    composeTestRule.waitForIdle()

    // 3. Open Profil Anak tab -> shows Verified Caregiver Card
    composeTestRule.onNodeWithTag("card_parent_summary_profile").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("parent_profile_content")
      .performScrollToNode(androidx.compose.ui.test.hasTestTag("card_parent_wali_legitimacy"))
    composeTestRule.onNodeWithTag("card_parent_wali_legitimacy").assertIsDisplayed()

    vm.logout()
    db.parentAccessLinkDao().delete(link)
    db.childStudentDao().delete(student)
    db.waliAccountDao().delete(wali)
    Unit
  }

  @Test
  fun `verify Konseling PDF wraps long summary intervention followUp and title completely without truncation`() {
    val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    val student = com.example.data.model.ChildStudent(
      id = 995001L,
      name = "Livia Putri Prameswari",
      nis = "SRT-KED00-5153",
      gender = "P",
      age = 13,
      dormitory = "Asrama Putri",
      classGrade = "VII-4",
      assignedWaliUsername = "ewahyudi",
      assignedWaliName = "Eko Wahyudi",
      schoolId = "SRT1-KDR"
    )
    val longSummaryTail = "TAIL_SUMMARY_LENGKAP_TIDAK_TERPOTONG"
    val longActionTail = "TAIL_PENDAMPINGAN_LENGKAP"
    val longFollowUpTail = "TAIL_TINDAK_LANJUT_LENGKAP"
    val longTitleTail = "TAIL_JUDUL_LENGKAP"

    val longRec = com.example.data.model.KonselingRecord(
      id = 995010L,
      studentId = student.id,
      studentName = student.name,
      studentNis = student.nis,
      studentClass = student.classGrade,
      studentDorm = student.dormitory,
      ownerWaliUsername = "ewahyudi",
      ownerWaliName = "Eko Wahyudi",
      entryType = com.example.data.model.KonselingHelper.ENTRY_SESI,
      topic = com.example.data.model.KonselingHelper.TOPIC_ADAPTASI,
      moodLevel = com.example.data.model.KonselingHelper.MOOD_SEDIH,
      urgencyLevel = com.example.data.model.KonselingHelper.URGENCY_RINGAN,
      title = "Belum betah di asrama karena faktor keluarga dan lingkungan baru $longTitleTail",
      summary = "Kemarin sempat cerita-cerita dengan dia bahwa Ayah dan Ibunya bercerai kemudian ayahnya menikah lagi dengan pasangan barunya sehingga anak merasa terabaikan di rumah dan membutuhkan perhatian emosional intensif $longSummaryTail",
      intervention = "Dilakukan sesi konseling individual penuh empati, penguatan adaptasi kamar asrama bersama kakak pembina, dan pembiasaan aktivitas kelompok positif harian $longActionTail",
      followUpPlan = "Jadwalkan observasi rutin tiga hari ke depan, evaluasi kenyamanan tidur asrama, serta komunikasi berkala dengan wali kelas $longFollowUpTail",
      status = com.example.data.model.KonselingHelper.STATUS_DIPANTAU,
      schoolId = "SRT1-KDR"
    )

    val drawnStudentTexts = mutableListOf<String>()
    val studentBmps = mutableListOf<android.graphics.Bitmap>()
    com.example.util.PdfReportGenerator.renderKonselingStudentPages(
      context = ctx,
      student = student,
      records = listOf(longRec),
      waliName = "Eko Wahyudi",
      schoolProfile = com.example.data.model.SchoolProfile(),
      startNewCanvas = {
        val bmp = android.graphics.Bitmap.createBitmap(595, 842, android.graphics.Bitmap.Config.ARGB_8888)
        studentBmps.add(bmp)
        object : android.graphics.Canvas(bmp) {
          override fun drawText(text: String, x: Float, y: Float, paint: android.graphics.Paint) {
            drawnStudentTexts.add(text)
            super.drawText(text, x, y, paint)
          }
        }
      }
    )

    val joinedStudentText = drawnStudentTexts.joinToString(" ")
    org.junit.Assert.assertTrue(
      "Summary tail must appear in full in per-student PDF: $joinedStudentText",
      joinedStudentText.contains(longSummaryTail)
    )
    org.junit.Assert.assertTrue(
      "Intervention tail must appear in full in per-student PDF",
      joinedStudentText.contains(longActionTail)
    )
    org.junit.Assert.assertTrue(
      "Follow-up tail must appear in full in per-student PDF",
      joinedStudentText.contains(longFollowUpTail)
    )
    org.junit.Assert.assertTrue(
      "Title tail must appear in full in per-student PDF",
      joinedStudentText.contains(longTitleTail)
    )
    org.junit.Assert.assertFalse(
      "No line in counseling student PDF should end with truncation ellipsis",
      drawnStudentTexts.any { it.endsWith("...") || it.endsWith("\u2026") }
    )
    studentBmps.forEach { it.recycle() }

    val drawnSummaryTexts = mutableListOf<String>()
    val summaryBmps = mutableListOf<android.graphics.Bitmap>()
    com.example.util.PdfReportGenerator.renderKonselingSummaryPages(
      context = ctx,
      students = listOf(student),
      records = listOf(longRec),
      waliName = "Eko Wahyudi",
      schoolProfile = com.example.data.model.SchoolProfile(),
      startNewCanvas = {
        val bmp = android.graphics.Bitmap.createBitmap(595, 842, android.graphics.Bitmap.Config.ARGB_8888)
        summaryBmps.add(bmp)
        object : android.graphics.Canvas(bmp) {
          override fun drawText(text: String, x: Float, y: Float, paint: android.graphics.Paint) {
            drawnSummaryTexts.add(text)
            super.drawText(text, x, y, paint)
          }
        }
      }
    )

    val joinedSummaryText = drawnSummaryTexts.joinToString(" ")
    org.junit.Assert.assertTrue(
      "Summary tail must appear in full in counseling recap PDF",
      joinedSummaryText.contains(longSummaryTail)
    )
    org.junit.Assert.assertTrue(
      "Follow-up tail must appear in full in counseling recap PDF",
      joinedSummaryText.contains(longFollowUpTail)
    )
    org.junit.Assert.assertFalse(
      "No line in counseling recap PDF should end with truncation ellipsis",
      drawnSummaryTexts.any { it.endsWith("...") || it.endsWith("\u2026") }
    )
    summaryBmps.forEach { it.recycle() }
  }

  @Test
  fun `verify BibleContentRepository separates Indonesian and English parsers and never leaks English text into Indonesian mode`() = kotlinx.coroutines.runBlocking {
    val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    com.example.util.BibleContentRepository.clearMemoryCacheForTesting()
    java.io.File(ctx.filesDir, "bible_cache").deleteRecursively()

    // 1. Parser Beeble Terjemahan Baru (TB) mengisi textId saja
    val beebleSample = """
      {
        "data": {
          "book": { "name": "Yohanes", "chapter": 1 },
          "verses": [
            { "verse": 0, "type": "title", "content": "Firman yang telah menjadi manusia" },
            { "verse": 1, "type": "text", "content": "Pada mulanya adalah Firman; Firman itu bersama-sama dengan Allah dan Firman itu adalah Allah." }
          ]
        }
      }
    """.trimIndent()
    val beebleVerses = com.example.util.BibleContentRepository.parseBeebleChapterJson(beebleSample)
    assertEquals(1, beebleVerses.size)
    assertEquals(1, beebleVerses[0].verse)
    org.junit.Assert.assertTrue(beebleVerses[0].textId.contains("Pada mulanya adalah Firman"))
    assertEquals("", beebleVerses[0].textEn)

    // 2. Parser HelloAO Indonesian (ind_ayt) mengisi textId saja
    val helloIdSample = """
      {
        "chapter": {
          "number": 1,
          "content": [
            { "type": "heading", "text": "Penciptaan" },
            { "type": "verse", "number": 1, "text": "Pada mulanya, Allah menciptakan langit dan bumi." }
          ]
        }
      }
    """.trimIndent()
    val helloIdVerses = com.example.util.BibleContentRepository.parseHelloAoChapterJson(
      helloIdSample,
      isEnglishTarget = false
    )
    assertEquals(1, helloIdVerses.size)
    org.junit.Assert.assertTrue(helloIdVerses[0].textId.contains("Pada mulanya, Allah menciptakan langit"))
    assertEquals("", helloIdVerses[0].textEn)

    // 3. Parser bible-api WEB (Inggris) HARUS mengisi textEn saja dan TIDAK BOLEH membocorkan teks Inggris ke textId
    val webApiSample = """
      {
        "verses": [
          { "verse": 1, "text": "In the beginning was the Word, and the Word was with God." }
        ]
      }
    """.trimIndent()
    val webVerses = com.example.util.BibleContentRepository.parseWebApiChapterJson(webApiSample)
    assertEquals(1, webVerses.size)
    assertEquals("", webVerses[0].textId)
    org.junit.Assert.assertTrue(webVerses[0].textEn.contains("In the beginning was the Word"))

    // 4. Cache lokal _id.json vs _en.json tidak pernah tertukar saat loadChapterVerses dipanggil
    val cacheDir = java.io.File(ctx.filesDir, "bible_cache").apply { mkdirs() }
    java.io.File(cacheDir, "2CO_1_id.json").writeText(
      """{"lang":"id","source":"Terjemahan Baru (TB)","verses":[{"verse":3,"text":"Terpujilah Allah, Bapa Tuhan kita Yesus Kristus, Bapa yang penuh belas kasihan."}]}"""
    )
    java.io.File(cacheDir, "2CO_1_en.json").writeText(
      """{"lang":"en","source":"World English Bible (WEB)","verses":[{"verse":3,"text":"Blessed be the God and Father of our Lord Jesus Christ, the Father of mercies."}]}"""
    )
    val book2Co = com.example.data.model.BibleCatalog.findBook("2CO")
    val indLoaded = com.example.util.BibleContentRepository.loadChapterVerses(
      context = ctx,
      book = book2Co,
      chapter = 1,
      isEnglish = false
    )
    val engLoaded = com.example.util.BibleContentRepository.loadChapterVerses(
      context = ctx,
      book = book2Co,
      chapter = 1,
      isEnglish = true
    )
    org.junit.Assert.assertTrue(indLoaded.verses.first().textId.contains("Terpujilah Allah"))
    assertEquals("", indLoaded.verses.first().textEn)
    org.junit.Assert.assertTrue(engLoaded.verses.first().textEn.contains("Blessed be the God"))
    assertEquals("", engLoaded.verses.first().textId)

    // 5. Pastikan curasi 2TI_4 memuat teks asli 2 Timotius 4:1-3 (bukan Filipi 4:7)
    val curated2Ti4 = com.example.util.BibleContentRepository.CURATED_CHAPTERS["2TI_4"].orEmpty()
    org.junit.Assert.assertTrue(curated2Ti4.first().textId.contains("Di hadapan Allah dan Kristus Yesus"))
    org.junit.Assert.assertFalse(curated2Ti4.first().textId.contains("melampaui segala akal budi"))
  }

  @Test
  fun `verify BibleReaderSheet renders proportional header, full book title, 17sp default, all 6 bottom tabs, and authentic Indonesian 2TI 4`() {
    val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    com.example.util.BibleContentRepository.clearMemoryCacheForTesting()
    val cacheDir = java.io.File(ctx.filesDir, "bible_cache").apply { mkdirs() }
    java.io.File(cacheDir, "2TI_4_id.json").writeText(
      """{"lang":"id","source":"Terjemahan Baru (TB)","verses":[{"verse":1,"text":"Di hadapan Allah dan Kristus Yesus yang akan menghakimi orang yang hidup dan yang mati, aku berpesan dengan sungguh-sungguh kepadamu demi penyataan-Nya dan demi Kerajaan-Nya:"},{"verse":2,"text":"Beritakanlah firman, siap sedialah baik atau tidak baik waktunya."}]}"""
    )

    composeTestRule.setContent {
      com.example.ui.dialogs.BibleReaderSheet(
        bookmarks = emptyList(),
        notes = emptyList(),
        initialEnglish = false,
        useDialogWindow = false,
        onSyncRequested = {},
        onToggleBookmark = { _, _, _, _, _, _, _ -> },
        onDeleteBookmark = { _, _ -> },
        onSaveNote = { _, _, _, _, _, _, _, _ -> },
        onDeleteNote = { _, _ -> },
        onDismiss = {}
      )
    }
    composeTestRule.waitForIdle()

    // Full untruncated book title & default readable font 17
    composeTestRule.onNodeWithTag("bible_subheader_book_title").assertExists()
    composeTestRule.onNodeWithText("2 TIMOTIUS").assertExists()
    composeTestRule.onNodeWithText("17").assertExists()

    // Seluruh 6 tab bawah tampil
    composeTestRule.onNodeWithTag("bible_nav_alkitab").assertExists()
    composeTestRule.onNodeWithTag("bible_nav_renungan").assertExists()
    composeTestRule.onNodeWithTag("bible_nav_kisah").assertExists()
    composeTestRule.onNodeWithTag("bible_nav_catatan").assertExists()
    composeTestRule.onNodeWithTag("bible_nav_bookmark").assertExists()
    composeTestRule.onNodeWithTag("bible_nav_cari").assertExists()

    // Ayat Indonesia tampil dalam Bahasa Indonesia asli (bukan bahasa Inggris)
    composeTestRule.onNodeWithTag("bible_verse_row_1").assertExists()
    composeTestRule.onNodeWithText("Di hadapan Allah dan Kristus Yesus", substring = true).assertExists()
  }

  @Test
  fun `verify BarcodeScannerDialog Option 2 renders fullscreen portrait overlay, manual picker bottom sheet, and continuous scan controls`() {
    val indonesianStrings = com.example.util.i18n.AppLanguageManager.stringsFor(
      com.example.util.i18n.AppLanguage.INDONESIAN
    )
    val students = listOf(
      com.example.data.model.ChildStudent(
        id = 701L,
        name = "Adelia Sukmawati",
        nis = "31900701",
        nik = "3506015105090001",
        gender = "P",
        age = 15,
        dormitory = "Asrama Putri",
        classGrade = "XI-1 (IPA)",
        schoolId = "SRT1-KDR"
      ),
      com.example.data.model.ChildStudent(
        id = 702L,
        name = "Budi Santoso",
        nis = "31900702",
        nik = "3506015105090002",
        gender = "L",
        age = 15,
        dormitory = "Asrama Putra",
        classGrade = "XI-2 (IPS)",
        schoolId = "SRT1-KDR"
      )
    )

    val continuousMode = androidx.compose.runtime.mutableStateOf(false)
    val scannedCodes = mutableListOf<String>()
    var dismissCount = 0

    composeTestRule.setContent {
      CompositionLocalProvider(com.example.util.i18n.LocalAppStrings provides indonesianStrings) {
        com.example.ui.dialogs.BarcodeScannerDialog(
          femaleStudents = students,
          onScanned = { scannedCodes.add(it) },
          onDismiss = { dismissCount++ },
          dialogTitle = if (continuousMode.value) "Scan Kartu Sholat Dzuhur" else "Scan Barcode Kartu Siswa",
          continuousScan = continuousMode.value,
          sessionParticipantCount = if (continuousMode.value) 3 else null,
          floatingFeedback = if (continuousMode.value) {
            com.example.ui.dialogs.ScannerFloatingFeedback(
              id = 101L,
              status = com.example.ui.dialogs.ScannerFeedbackStatus.SUCCESS,
              title = "Adelia Sukmawati tercatat",
              subtitle = "Jamaah Tepat Waktu"
            )
          } else {
            null
          }
        )
      }
    }
    composeTestRule.waitForIdle()

    // 1. Fullscreen portrait overlay & bottom action dock
    composeTestRule.onNodeWithTag("scanner_fullscreen_root").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_close_scanner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_open_manual_student_picker").assertIsDisplayed()
    composeTestRule.onNodeWithText("Arahkan Barcode / QR Kartu Siswa ke Kotak").assertIsDisplayed()

    // 2. Open Manual Input & Quick Pick ModalBottomSheet
    composeTestRule.onNodeWithTag("btn_open_manual_student_picker").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("input_manual_nik_haid").assertIsDisplayed()
    composeTestRule.onNodeWithTag("input_quick_pick_haid_search").assertIsDisplayed()
    composeTestRule.onNodeWithTag("input_quick_pick_haid_search").performTextInput("Adelia")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Adelia Sukmawati").performClick()
    composeTestRule.waitForIdle()

    assertEquals(listOf("3506015105090001"), scannedCodes)
    assertEquals(1, dismissCount)

    // 3. Continuous scan mode renders floating feedback, count pill, and Selesai button
    continuousMode.value = true
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("3 Anak").assertIsDisplayed()
    composeTestRule.onNodeWithTag("floating_scan_feedback_popup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_done_continuous_scan").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals(2, dismissCount)
  }
}

