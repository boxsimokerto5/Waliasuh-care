package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ChildStudentDao
import com.example.data.dao.MedicalRecordDao
import com.example.data.dao.SchoolRegistrationDao
import com.example.data.dao.ShiftMessageDao
import com.example.data.dao.WaliAccountDao
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolRegistration
import com.example.data.model.ShiftMessage
import com.example.data.model.WaliAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

import com.example.data.dao.FastingRecordDao
import com.example.data.dao.FastingSessionDao
import com.example.data.dao.HaidRecordDao
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord

@Database(
    entities = [
        ChildStudent::class,
        MedicalRecord::class,
        WaliAccount::class,
        SchoolRegistration::class,
        ShiftMessage::class,
        HaidRecord::class,
        FastingSession::class,
        FastingRecord::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun childStudentDao(): ChildStudentDao
    abstract fun medicalRecordDao(): MedicalRecordDao
    abstract fun waliAccountDao(): WaliAccountDao
    abstract fun schoolRegistrationDao(): SchoolRegistrationDao
    abstract fun shiftMessageDao(): ShiftMessageDao
    abstract fun haidRecordDao(): HaidRecordDao
    abstract fun fastingSessionDao(): FastingSessionDao
    abstract fun fastingRecordDao(): FastingRecordDao

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

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("DELETE FROM child_students")
                    db.execSQL("DELETE FROM wali_accounts")
                    db.execSQL("DELETE FROM medical_records")
                } catch (e: Exception) {
                    // Table or data may vary
                }
            }
        }

        val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE medical_records ADD COLUMN isInpatient INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {
                    // Column may already exist
                }
                try {
                    db.execSQL("ALTER TABLE medical_records ADD COLUMN inpatientRoom TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) {
                    // Column may already exist
                }
                try {
                    db.execSQL("ALTER TABLE medical_records ADD COLUMN dischargeDate INTEGER")
                } catch (e: Exception) {
                    // Column may already exist
                }
            }
        }

        val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE medical_records ADD COLUMN createdBy TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE medical_records ADD COLUMN editedBy TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE medical_records ADD COLUMN editedAt INTEGER")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_13_14 = object : androidx.room.migration.Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS fasting_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        sessionDate INTEGER NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdBy TEXT NOT NULL DEFAULT 'Admin Sekolah',
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS fasting_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sessionId INTEGER NOT NULL,
                        sessionTitle TEXT NOT NULL DEFAULT '',
                        sessionDate INTEGER NOT NULL DEFAULT 0,
                        studentId INTEGER NOT NULL DEFAULT 0,
                        studentName TEXT NOT NULL,
                        studentNik TEXT NOT NULL DEFAULT '',
                        studentNis TEXT NOT NULL DEFAULT '',
                        studentClass TEXT NOT NULL DEFAULT '',
                        studentDorm TEXT NOT NULL DEFAULT '',
                        studentJenjang TEXT NOT NULL DEFAULT '',
                        isSahurChecked INTEGER NOT NULL DEFAULT 0,
                        sahurCheckedAt INTEGER,
                        sahurCheckedBy TEXT NOT NULL DEFAULT '',
                        isBukaChecked INTEGER NOT NULL DEFAULT 0,
                        bukaCheckedAt INTEGER,
                        bukaCheckedBy TEXT NOT NULL DEFAULT '',
                        waliName TEXT NOT NULL DEFAULT 'Wali Asuh',
                        notes TEXT NOT NULL DEFAULT '',
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
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
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
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
            // Data persisten terjaga aman dan tidak dihapus otomatis
        }
    }
}
