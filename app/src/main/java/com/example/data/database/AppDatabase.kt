package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ChildStudentDao
import com.example.data.dao.MedicalRecordDao
import com.example.data.dao.SchoolRegistrationDao
import com.example.data.dao.WaliAccountDao
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolRegistration
import com.example.data.model.WaliAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

@Database(
    entities = [ChildStudent::class, MedicalRecord::class, WaliAccount::class, SchoolRegistration::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun childStudentDao(): ChildStudentDao
    abstract fun medicalRecordDao(): MedicalRecordDao
    abstract fun waliAccountDao(): WaliAccountDao
    abstract fun schoolRegistrationDao(): SchoolRegistrationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "waliasuh_care.db"
                )
                    .addCallback(DatabaseCallback(appContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun loadStudentsFromAssets(context: Context): List<ChildStudent> {
            return try {
                val jsonString = context.assets.open("students_data.json").bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val list = ArrayList<ChildStudent>(jsonArray.length())
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        ChildStudent(
                            id = obj.optLong("id", (i + 1).toLong()),
                            name = obj.optString("name", ""),
                            nis = obj.optString("nis", ""),
                            gender = obj.optString("gender", "L"),
                            age = obj.optInt("age", 12),
                            dormitory = obj.optString("dormitory", "Asrama"),
                            classGrade = obj.optString("classGrade", "-"),
                            allergies = obj.optString("allergies", "Tidak ada"),
                            bloodType = obj.optString("bloodType", "-"),
                            emergencyContact = obj.optString("emergencyContact", "Pengasuh Asrama"),
                            specialNotes = obj.optString("specialNotes", ""),
                            nik = obj.optString("nik", ""),
                            noKk = obj.optString("noKk", ""),
                            birthPlace = obj.optString("birthPlace", ""),
                            birthDate = obj.optString("birthDate", ""),
                            motherName = obj.optString("motherName", ""),
                            address = obj.optString("address", ""),
                            roomNumber = obj.optString("roomNumber", ""),
                            jenjang = obj.optString("jenjang", "")
                        )
                    )
                }
                list
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val students = loadStudentsFromAssets(context)
                    if (students.isNotEmpty()) {
                        database.childStudentDao().deleteAll()
                        database.medicalRecordDao().deleteAll()
                        database.childStudentDao().insertAll(students)
                    }
                    seedDefaultWaliAccounts(database)
                    seedDefaultRegistrations(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val count = database.childStudentDao().count()
                    // Jika data masih data dummy lama (< 300 siswa), bersihkan dan ganti dengan 342 siswa baru
                    if (count < 300) {
                        database.childStudentDao().deleteAll()
                        database.medicalRecordDao().deleteAll()
                        val students = loadStudentsFromAssets(context)
                        if (students.isNotEmpty()) {
                            database.childStudentDao().insertAll(students)
                        }
                    }
                    seedDefaultWaliAccounts(database)
                    seedDefaultRegistrations(database)
                }
            }
        }

        private suspend fun seedDefaultRegistrations(database: AppDatabase) {
            val count = database.schoolRegistrationDao().count()
            if (count == 0) {
                val demoRegistrations = listOf(
                    SchoolRegistration(
                        schoolName = "SMP IT Al-Hikmah",
                        address = "Jl. Raya Candi No. 45, Karangploso",
                        city = "Kabupaten Malang",
                        estimatedWaliCount = 8,
                        picName = "Drs. H. Mulyadi, M.Pd",
                        picEmail = "mulyadi.alhikmah@gmail.com",
                        picPhone = "081334567890",
                        submissionDate = System.currentTimeMillis() - 86400000L * 2,
                        status = "PENDING"
                    ),
                    SchoolRegistration(
                        schoolName = "Panti Asuhan Kasih Mandiri",
                        address = "Jl. Kenjeran No. 112",
                        city = "Kota Surabaya",
                        estimatedWaliCount = 5,
                        picName = "Hj. Siti Aminah, S.Sos",
                        picEmail = "kasihmandiri.sby@gmail.com",
                        picPhone = "085712345678",
                        submissionDate = System.currentTimeMillis() - 86400000L * 4,
                        status = "APPROVED",
                        generatedSchoolCode = "SRT-SBY02",
                        generatedAdminUsername = "adminsby_kasihmandiri",
                        generatedAdminPassword = "Mandiri#8419",
                        approvedDate = System.currentTimeMillis() - 86400000L * 3
                    )
                )
                database.schoolRegistrationDao().insertAll(demoRegistrations)
            }
        }

        private suspend fun seedDefaultWaliAccounts(database: AppDatabase) {
            val count = database.waliAccountDao().count()
            if (count == 0) {
                val defaultAccounts = listOf(
                    WaliAccount(name = "Sari Devi", username = "saridevi", password = "Sari#7294", dormitory = "Asrama Putri", phone = "081234567891"),
                    WaliAccount(name = "Budi Santoso", username = "budisantoso", password = "Budi#3815", dormitory = "Asrama Putra", phone = "081234567892"),
                    WaliAccount(name = "Ahmad Fauzi", username = "ahmadfauzi", password = "Fauzi#9162", dormitory = "Asrama Putra", phone = "081234567893"),
                    WaliAccount(name = "Dewi Lestari", username = "dewilestari", password = "Dewi#4921", dormitory = "Asrama Putri", phone = "081234567894")
                )
                database.waliAccountDao().insertAll(defaultAccounts)
            } else {
                // Periksa jika ada akun lama dengan username kosong, otomatis perbaiki
                val existingAccounts = database.waliAccountDao().getAllAccountsList()
                for (acc in existingAccounts) {
                    if (acc.username.isBlank()) {
                        val fixedUsername = acc.name.lowercase().replace(" ", "").filter { it.isLetterOrDigit() }.ifBlank { "wali${acc.id}" }
                        database.waliAccountDao().update(acc.copy(username = fixedUsername))
                    }
                }
            }
        }
    }
}
