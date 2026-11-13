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
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

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
  fun `verify SupabaseStudentDto preserves schoolId and SupabaseSchemaProvider includes all 6 tables`() {
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

    val dto = com.example.data.remote.supabase.SupabaseStudentDto.fromDomain(student)
    assertEquals("SRT-KED05", dto.schoolId)
    assertEquals(55001L, dto.id)

    val restored = dto.toDomainModel()
    assertEquals("SRT-KED05", restored.schoolId)
    assertEquals("PP_102", restored.roomNumber)
    assertEquals("Nadia Putri", restored.name)

    val schema = com.example.data.remote.supabase.SupabaseSchemaProvider.FULL_SQL_SCHEMA
    val expectedTables = listOf(
      "child_students",
      "medical_records",
      "haid_records",
      "wali_accounts",
      "shift_messages",
      "school_registrations"
    )
    expectedTables.forEach { table ->
      org.junit.Assert.assertTrue("Missing table $table in SQL schema", schema.contains("public.$table"))
      org.junit.Assert.assertTrue("Missing RLS enable for $table", schema.contains("alter table public.$table enable row level security"))
      org.junit.Assert.assertTrue("Missing RLS policy for $table", schema.contains("create policy \"Akses Penuh $table\""))
    }
    org.junit.Assert.assertTrue(schema.contains("school_id"))
    assertEquals(6, com.example.data.remote.supabase.SupabaseSchemaProvider.TABLES.size)
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
}




