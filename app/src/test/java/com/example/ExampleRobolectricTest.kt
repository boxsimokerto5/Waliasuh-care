package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.flow.first

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

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
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val departedRecord = db.medicalRecordDao().getRecordById(referralId).first()
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
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    org.junit.Assert.assertNotNull(createdMessage)
    val msgId = createdMessage!!.id
    assertEquals("Pantau Suhu Santri Rizky", createdMessage?.title)
    assertEquals("URGENT", createdMessage?.priority)
    assertEquals("Shift Malam (21-07)", createdMessage?.shift)
    org.junit.Assert.assertFalse(createdMessage!!.isCompleted)

    // 2. Tandai selesai ditindaklanjuti
    vm.toggleShiftMessageCompleted(createdMessage!!)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val messages = db.shiftMessageDao().getAllMessages().first()
    val updated = messages.firstOrNull { it.id == msgId }
    org.junit.Assert.assertNotNull(updated)
    org.junit.Assert.assertTrue(updated!!.isCompleted)
    assertEquals("Ustadz Fauzi", updated.completedBy)

    // 3. Hapus pesan shift
    vm.deleteShiftMessage(updated)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val messagesAfterDelete = db.shiftMessageDao().getAllMessages().first()
    org.junit.Assert.assertNull(messagesAfterDelete.firstOrNull { it.id == msgId })
  }
}


