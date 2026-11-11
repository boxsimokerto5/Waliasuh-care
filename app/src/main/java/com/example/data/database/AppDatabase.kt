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
    version = 8,
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

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE child_students ADD COLUMN schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR'")
                } catch (e: Exception) {
                    // Column may already exist
                }
                try {
                    db.execSQL("ALTER TABLE wali_accounts ADD COLUMN schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR'")
                } catch (e: Exception) {
                    // Column may already exist
                }
            }
        }

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("DELETE FROM school_registrations WHERE schoolName LIKE '%Al-Hikmah%' OR schoolName LIKE '%Kasih Mandiri%'")
                } catch (e: Exception) {
                    // Table or data may vary
                }
                try {
                    db.execSQL("DELETE FROM wali_accounts WHERE username IN ('saridevi', 'budisantoso', 'ahmadfauzi', 'dewilestari')")
                } catch (e: Exception) {
                    // Table or data may vary
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "waliasuh_care.db"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
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
                            jenjang = obj.optString("jenjang", ""),
                            schoolId = "SRT1-KDR"
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
            // Database dimulai bersih (0 siswa & 0 wali) untuk menjamin multi-tenant isolasi mutlak
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Bersihkan seluruh data siswa, akun wali asuh, dan rekam medis lama sesuai instruksi user
                        db.execSQL("DELETE FROM child_students")
                        db.execSQL("DELETE FROM wali_accounts")
                        db.execSQL("DELETE FROM medical_records")
                        db.execSQL("DELETE FROM school_registrations WHERE schoolName LIKE '%Al-Hikmah%' OR schoolName LIKE '%Kasih Mandiri%'")
                    } catch (e: Exception) {
                        // ignore if already clean
                    }
                }
            }
        }
    }
}
