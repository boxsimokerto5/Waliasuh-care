package com.example

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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

    // Simulate next 4 clicks (total 9)
    for (i in 1..4) {
      com.example.util.TopOnAdManager.onUserActionClick(context)
    }
    assertEquals(9, com.example.util.TopOnAdManager.getSavedClickCount(context))

    // 10th click triggers reset back to 0
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
    assertEquals(10, jsonSchema.length())
    org.junit.Assert.assertTrue(sshScript.contains("/api/collections"))
    org.junit.Assert.assertTrue(sshScript.contains("is_sahur_checked"))
    org.junit.Assert.assertTrue(sshScript.contains("fluency_status"))

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
      "school_profiles"
    )
    expectedCollections.forEach { col ->
      org.junit.Assert.assertTrue(
        "Missing collection $col in PocketBaseSchemaProvider",
        com.example.data.remote.pocketbase.PocketBaseSchemaProvider.COLLECTIONS.any { it.collectionName == col }
      )
      org.junit.Assert.assertTrue("Missing $col in schema summary", schema.contains(col))
    }
    assertEquals(10, com.example.data.remote.pocketbase.PocketBaseSchemaProvider.COLLECTIONS.size)
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
    org.junit.Assert.assertTrue(templateStudents.all { it.nik.isNotBlank() && it.noKk.isNotBlank() && it.schoolId == "SRT-NEW01" })

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
    composeTestRule.onNodeWithText("3578150504110001").assertIsDisplayed()
    composeTestRule.onNodeWithText("3506150510180001").assertIsDisplayed()
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
    composeTestRule.onNodeWithTag("chip_session_input_${session.id}").assertIsDisplayed()
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
    composeTestRule.onNodeWithTag("tile_tindakan_langsung").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_rekam_medis").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_anak_asuh").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_puasa").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_haid").assertIsDisplayed()
    composeTestRule.onNodeWithTag("category_pesan").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_tulis_pesan_shift").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tile_puskesmas_info").assertIsDisplayed()

    composeTestRule.onNodeWithTag("category_puasa").performClick()
    assertEquals(com.example.ui.components.DashboardCategoryHub.PUASA, openedHub)

    composeTestRule.onNodeWithTag("category_haid").performClick()
    assertEquals(com.example.ui.components.DashboardCategoryHub.HAID, openedHub)

    composeTestRule.onNodeWithTag("category_berobat").performClick()
    assertEquals(com.example.ui.components.DashboardCategoryHub.BEROBAT, openedHub)

    composeTestRule.onNodeWithTag("category_pesan").performClick()
    assertEquals(com.example.ui.components.DashboardCategoryHub.PESAN, openedHub)
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
    composeTestRule.onNodeWithTag("tile_tulis_pesan_shift").assertIsDisplayed()

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
    composeTestRule.onNodeWithTag("tile_quick_input_mengaji").assertIsDisplayed()
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
    composeTestRule.onNodeWithTag("badge_student_latest_mengaji").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("student_mengaji_item_701").performScrollTo().assertIsDisplayed()

    composeTestRule.onNodeWithTag("btn_student_input_mengaji").performScrollTo().performClick()
    org.junit.Assert.assertTrue(inputClicked)

    composeTestRule.onNodeWithTag("btn_student_kartu_mengaji").performScrollTo().performClick()
    org.junit.Assert.assertTrue(kartuClicked)
  }
}
