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

import com.example.data.dao.DeletionLogDao
import com.example.data.dao.FastingRecordDao
import com.example.data.dao.FastingSessionDao
import com.example.data.dao.HaidRecordDao
import com.example.data.dao.MengajiRecordDao
import com.example.data.dao.VisitRecordDao
import com.example.data.dao.VisitSessionDao
import com.example.data.dao.ParentAccessLinkDao
import com.example.data.dao.ParentMessageDao
import com.example.data.dao.KonselingLogDao
import com.example.data.dao.KonselingRecordDao
import com.example.data.dao.TabunganLogDao
import com.example.data.dao.TabunganTransactionDao
import com.example.data.model.DeletionLog
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.KonselingLog
import com.example.data.model.KonselingRecord
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentMessage
import com.example.data.model.TabunganLog
import com.example.data.model.TabunganTransaction
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession

@Database(
    entities = [
        ChildStudent::class,
        MedicalRecord::class,
        WaliAccount::class,
        SchoolRegistration::class,
        ShiftMessage::class,
        HaidRecord::class,
        FastingSession::class,
        FastingRecord::class,
        MengajiRecord::class,
        VisitSession::class,
        VisitRecord::class,
        ParentAccessLink::class,
        ParentMessage::class,
        DeletionLog::class,
        TabunganTransaction::class,
        TabunganLog::class,
        KonselingRecord::class,
        KonselingLog::class
    ],
    version = 23,
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
    abstract fun mengajiRecordDao(): MengajiRecordDao
    abstract fun visitSessionDao(): VisitSessionDao
    abstract fun visitRecordDao(): VisitRecordDao
    abstract fun parentAccessLinkDao(): ParentAccessLinkDao
    abstract fun parentMessageDao(): ParentMessageDao
    abstract fun deletionLogDao(): DeletionLogDao
    abstract fun tabunganTransactionDao(): TabunganTransactionDao
    abstract fun tabunganLogDao(): TabunganLogDao
    abstract fun konselingRecordDao(): KonselingRecordDao
    abstract fun konselingLogDao(): KonselingLogDao

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

        val MIGRATION_14_15 = object : androidx.room.migration.Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS mengaji_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL DEFAULT 0,
                        studentName TEXT NOT NULL,
                        studentNik TEXT NOT NULL DEFAULT '',
                        studentNis TEXT NOT NULL DEFAULT '',
                        studentClass TEXT NOT NULL DEFAULT '',
                        studentDorm TEXT NOT NULL DEFAULT '',
                        studentJenjang TEXT NOT NULL DEFAULT '',
                        sessionDate INTEGER NOT NULL DEFAULT 0,
                        bookType TEXT NOT NULL DEFAULT 'Iqro''',
                        jilidOrJuz TEXT NOT NULL DEFAULT 'Jilid 1',
                        pageOrVerse TEXT NOT NULL DEFAULT 'Hal. 1',
                        startPage INTEGER,
                        endPage INTEGER,
                        fluencyStatus TEXT NOT NULL DEFAULT 'LANCAR',
                        notes TEXT NOT NULL DEFAULT '',
                        waliName TEXT NOT NULL DEFAULT 'Wali Asuh',
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_15_16 = object : androidx.room.migration.Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE child_students ADD COLUMN district TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_16_17 = object : androidx.room.migration.Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS visit_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        monthYear TEXT NOT NULL DEFAULT '',
                        sessionDatesCsv TEXT NOT NULL DEFAULT '',
                        selectedDistrictsCsv TEXT NOT NULL DEFAULT '',
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
                    CREATE TABLE IF NOT EXISTS visit_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sessionId INTEGER NOT NULL,
                        sessionTitle TEXT NOT NULL DEFAULT '',
                        visitDate INTEGER NOT NULL DEFAULT 0,
                        checkedInAt INTEGER NOT NULL DEFAULT 0,
                        studentId INTEGER NOT NULL DEFAULT 0,
                        studentName TEXT NOT NULL,
                        studentNik TEXT NOT NULL DEFAULT '',
                        studentNis TEXT NOT NULL DEFAULT '',
                        studentClass TEXT NOT NULL DEFAULT '',
                        studentDorm TEXT NOT NULL DEFAULT '',
                        studentDistrict TEXT NOT NULL DEFAULT '',
                        visitorName TEXT NOT NULL DEFAULT '',
                        visitorRelation TEXT NOT NULL DEFAULT 'Orang Tua / Wali',
                        checkInMethod TEXT NOT NULL DEFAULT 'BARCODE',
                        isOutOfScheduleDistrict INTEGER NOT NULL DEFAULT 0,
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

        val MIGRATION_17_18 = object : androidx.room.migration.Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE child_students ADD COLUMN parentPhone TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE child_students ADD COLUMN assignedWaliUsername TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE child_students ADD COLUMN assignedWaliName TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS parent_access_links (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL,
                        studentName TEXT NOT NULL,
                        studentNis TEXT NOT NULL DEFAULT '',
                        studentClass TEXT NOT NULL DEFAULT '',
                        studentDorm TEXT NOT NULL DEFAULT '',
                        parentName TEXT NOT NULL DEFAULT '',
                        parentPhone TEXT NOT NULL DEFAULT '',
                        username TEXT NOT NULL,
                        password TEXT NOT NULL,
                        assignedWaliUsername TEXT NOT NULL DEFAULT '',
                        assignedWaliName TEXT NOT NULL DEFAULT '',
                        canViewMengaji INTEGER NOT NULL DEFAULT 1,
                        canViewMedical INTEGER NOT NULL DEFAULT 1,
                        canViewFasting INTEGER NOT NULL DEFAULT 1,
                        canViewHaid INTEGER NOT NULL DEFAULT 1,
                        canViewProfile INTEGER NOT NULL DEFAULT 1,
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS parent_messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL,
                        studentName TEXT NOT NULL DEFAULT '',
                        parentName TEXT NOT NULL DEFAULT '',
                        waliUsername TEXT NOT NULL DEFAULT '',
                        waliName TEXT NOT NULL DEFAULT '',
                        senderRole TEXT NOT NULL DEFAULT 'WALI',
                        senderName TEXT NOT NULL DEFAULT '',
                        messageContent TEXT NOT NULL,
                        isReadByWali INTEGER NOT NULL DEFAULT 0,
                        isReadByParent INTEGER NOT NULL DEFAULT 0,
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_18_19 = object : androidx.room.migration.Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE parent_access_links ADD COLUMN canViewHaid INTEGER NOT NULL DEFAULT 1")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE parent_access_links ADD COLUMN canViewProfile INTEGER NOT NULL DEFAULT 1")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_19_20 = object : androidx.room.migration.Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS deletion_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        category TEXT NOT NULL,
                        studentId INTEGER NOT NULL DEFAULT 0,
                        studentName TEXT NOT NULL,
                        studentClass TEXT NOT NULL DEFAULT '',
                        recordId INTEGER NOT NULL DEFAULT 0,
                        deletedSummary TEXT NOT NULL DEFAULT '',
                        deletedBy TEXT NOT NULL DEFAULT 'Wali Asuh',
                        deletedByUsername TEXT NOT NULL DEFAULT '',
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_20_21 = object : androidx.room.migration.Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE wali_accounts ADD COLUMN role TEXT NOT NULL DEFAULT 'WALI'")
                } catch (_: Exception) {
                    // Column may already exist
                }
            }
        }

        val MIGRATION_21_22 = object : androidx.room.migration.Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE parent_access_links ADD COLUMN canViewTabungan INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {
                    // Column may already exist
                }
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tabungan_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL DEFAULT 0,
                        studentName TEXT NOT NULL,
                        studentNis TEXT NOT NULL DEFAULT '',
                        studentClass TEXT NOT NULL DEFAULT '',
                        studentDorm TEXT NOT NULL DEFAULT '',
                        ownerWaliUsername TEXT NOT NULL DEFAULT '',
                        ownerWaliName TEXT NOT NULL DEFAULT 'Wali Asuh',
                        isPrivate INTEGER NOT NULL DEFAULT 1,
                        transactionType TEXT NOT NULL DEFAULT 'IN',
                        amount INTEGER NOT NULL DEFAULT 0,
                        title TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        transactionDate INTEGER NOT NULL DEFAULT 0,
                        status TEXT NOT NULL DEFAULT 'ACTIVE',
                        cancelledBy TEXT NOT NULL DEFAULT '',
                        cancelledReason TEXT NOT NULL DEFAULT '',
                        cancelledAt INTEGER,
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tabungan_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        transactionId INTEGER NOT NULL DEFAULT 0,
                        studentId INTEGER NOT NULL DEFAULT 0,
                        studentName TEXT NOT NULL,
                        studentClass TEXT NOT NULL DEFAULT '',
                        ownerWaliUsername TEXT NOT NULL DEFAULT '',
                        ownerWaliName TEXT NOT NULL DEFAULT 'Wali Asuh',
                        isPrivate INTEGER NOT NULL DEFAULT 1,
                        actionType TEXT NOT NULL DEFAULT 'INPUT_IN',
                        amount INTEGER NOT NULL DEFAULT 0,
                        balanceBefore INTEGER NOT NULL DEFAULT 0,
                        balanceAfter INTEGER NOT NULL DEFAULT 0,
                        summary TEXT NOT NULL DEFAULT '',
                        actorName TEXT NOT NULL DEFAULT 'Wali Asuh',
                        actorUsername TEXT NOT NULL DEFAULT '',
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_22_23 = object : androidx.room.migration.Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS konseling_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL DEFAULT 0,
                        studentName TEXT NOT NULL,
                        studentNis TEXT NOT NULL DEFAULT '',
                        studentClass TEXT NOT NULL DEFAULT '',
                        studentDorm TEXT NOT NULL DEFAULT '',
                        ownerWaliUsername TEXT NOT NULL DEFAULT '',
                        ownerWaliName TEXT NOT NULL DEFAULT 'Wali Asuh',
                        isConfidential INTEGER NOT NULL DEFAULT 1,
                        entryType TEXT NOT NULL DEFAULT 'SESI',
                        topic TEXT NOT NULL DEFAULT 'ADAPTASI',
                        urgencyLevel TEXT NOT NULL DEFAULT 'RINGAN',
                        moodLevel TEXT NOT NULL DEFAULT 'NETRAL',
                        title TEXT NOT NULL DEFAULT '',
                        summary TEXT NOT NULL DEFAULT '',
                        intervention TEXT NOT NULL DEFAULT '',
                        followUpPlan TEXT NOT NULL DEFAULT '',
                        followUpDate INTEGER,
                        status TEXT NOT NULL DEFAULT 'DIPANTAU',
                        sessionDate INTEGER NOT NULL DEFAULT 0,
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS konseling_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        recordId INTEGER NOT NULL DEFAULT 0,
                        studentId INTEGER NOT NULL DEFAULT 0,
                        studentName TEXT NOT NULL,
                        studentClass TEXT NOT NULL DEFAULT '',
                        ownerWaliUsername TEXT NOT NULL DEFAULT '',
                        ownerWaliName TEXT NOT NULL DEFAULT 'Wali Asuh',
                        isConfidential INTEGER NOT NULL DEFAULT 1,
                        actionType TEXT NOT NULL DEFAULT 'INPUT_SESI',
                        summary TEXT NOT NULL DEFAULT '',
                        actorName TEXT NOT NULL DEFAULT 'Wali Asuh',
                        actorUsername TEXT NOT NULL DEFAULT '',
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        createdAt INTEGER NOT NULL DEFAULT 0
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
                    .addMigrations(
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_14_15,
                        MIGRATION_15_16,
                        MIGRATION_16_17,
                        MIGRATION_17_18,
                        MIGRATION_18_19,
                        MIGRATION_19_20,
                        MIGRATION_20_21,
                        MIGRATION_21_22,
                        MIGRATION_22_23
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
                            nik = com.example.util.StudentCsvParser.cleanDigitsOnly(obj.optString("nik", "")),
                            noKk = com.example.util.StudentCsvParser.cleanDigitsOnly(obj.optString("noKk", "")),
                            birthPlace = obj.optString("birthPlace", ""),
                            birthDate = obj.optString("birthDate", ""),
                            motherName = obj.optString("motherName", ""),
                            address = obj.optString("address", ""),
                            district = obj.optString("district", obj.optString("kecamatan", "")),
                            roomNumber = obj.optString("roomNumber", ""),
                            jenjang = obj.optString("jenjang", ""),
                            schoolId = "SRT1-KDR",
                            parentPhone = obj.optString("parentPhone", obj.optString("noHpOrangTua", ""))
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
