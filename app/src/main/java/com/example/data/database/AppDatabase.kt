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
import com.example.data.dao.ParentExpectationDao
import com.example.data.dao.ParentMessageDao
import com.example.data.dao.KonselingLogDao
import com.example.data.dao.KonselingRecordDao
import com.example.data.dao.TabunganLogDao
import com.example.data.dao.TabunganTransactionDao
import com.example.data.dao.InfoPentingCommentDao
import com.example.data.dao.InfoPentingPostDao
import com.example.data.dao.InfoPentingReactionDao
import com.example.data.dao.SholatRecordDao
import com.example.data.dao.ChatMediaLocalSaveDao
import com.example.data.dao.ChristianWorshipDao
import com.example.data.dao.ChristianWorshipLocalSaveDao
import com.example.data.dao.WaliGalleryLocalSaveDao
import com.example.data.dao.WaliGalleryPostDao
import com.example.data.dao.WaliScheduleEntryDao
import com.example.data.dao.WaliShiftCodeDao
import com.example.data.model.ChatMediaLocalSave
import com.example.data.model.ChristianWorshipLocalSave
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.DeletionLog
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.InfoPentingComment
import com.example.data.model.InfoPentingPost
import com.example.data.model.InfoPentingReaction
import com.example.data.model.KonselingLog
import com.example.data.model.KonselingRecord
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentExpectation
import com.example.data.model.ParentMessage
import com.example.data.model.SholatRecord
import com.example.data.model.TabunganLog
import com.example.data.model.TabunganTransaction
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.WaliGalleryLocalSave
import com.example.data.model.WaliGalleryPost
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliShiftCode

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
        ParentExpectation::class,
        DeletionLog::class,
        TabunganTransaction::class,
        TabunganLog::class,
        KonselingRecord::class,
        KonselingLog::class,
        WaliShiftCode::class,
        WaliScheduleEntry::class,
        InfoPentingPost::class,
        InfoPentingReaction::class,
        InfoPentingComment::class,
        SholatRecord::class,
        WaliGalleryPost::class,
        WaliGalleryLocalSave::class,
        ChatMediaLocalSave::class,
        ChristianWorshipRecord::class,
        ChristianWorshipLocalSave::class
    ],
    version = 35,
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
    abstract fun parentExpectationDao(): ParentExpectationDao
    abstract fun deletionLogDao(): DeletionLogDao
    abstract fun tabunganTransactionDao(): TabunganTransactionDao
    abstract fun tabunganLogDao(): TabunganLogDao
    abstract fun konselingRecordDao(): KonselingRecordDao
    abstract fun konselingLogDao(): KonselingLogDao
    abstract fun waliShiftCodeDao(): WaliShiftCodeDao
    abstract fun waliScheduleEntryDao(): WaliScheduleEntryDao
    abstract fun infoPentingPostDao(): InfoPentingPostDao
    abstract fun infoPentingReactionDao(): InfoPentingReactionDao
    abstract fun infoPentingCommentDao(): InfoPentingCommentDao
    abstract fun sholatRecordDao(): SholatRecordDao
    abstract fun waliGalleryPostDao(): WaliGalleryPostDao
    abstract fun waliGalleryLocalSaveDao(): WaliGalleryLocalSaveDao
    abstract fun chatMediaLocalSaveDao(): ChatMediaLocalSaveDao
    abstract fun christianWorshipDao(): ChristianWorshipDao
    abstract fun christianWorshipLocalSaveDao(): ChristianWorshipLocalSaveDao

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

        val MIGRATION_23_24 = object : androidx.room.migration.Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE haid_records ADD COLUMN lastCheckedInAt INTEGER")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE haid_records ADD COLUMN lastCheckInBy TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_24_25 = object : androidx.room.migration.Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS wali_shift_codes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        code TEXT NOT NULL,
                        name TEXT NOT NULL,
                        shiftGroup TEXT NOT NULL DEFAULT 'PAGI',
                        zoneLabel TEXT NOT NULL DEFAULT '',
                        timeRange TEXT NOT NULL DEFAULT '',
                        startHour INTEGER NOT NULL DEFAULT 7,
                        endHour INTEGER NOT NULL DEFAULT 15,
                        workHours INTEGER NOT NULL DEFAULT 8,
                        colorHex TEXT NOT NULL DEFAULT '#0284C7',
                        sopNotes TEXT NOT NULL DEFAULT '',
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS wali_schedule_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        monthKey TEXT NOT NULL,
                        waliAccountId INTEGER NOT NULL DEFAULT 0,
                        waliUsername TEXT NOT NULL DEFAULT '',
                        waliName TEXT NOT NULL,
                        waliCode TEXT NOT NULL DEFAULT '',
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        dayCodesJson TEXT NOT NULL DEFAULT '{}',
                        dayPinsJson TEXT NOT NULL DEFAULT '{}',
                        dayLeaveJson TEXT NOT NULL DEFAULT '{}',
                        updatedBy TEXT NOT NULL DEFAULT 'Admin Sekolah',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_25_26 = object : androidx.room.migration.Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS info_penting_posts (
                        id INTEGER NOT NULL PRIMARY KEY,
                        schoolId TEXT NOT NULL DEFAULT '__GLOBAL__',
                        title TEXT NOT NULL DEFAULT '',
                        content TEXT NOT NULL DEFAULT '',
                        category TEXT NOT NULL DEFAULT 'PENGUMUMAN',
                        mediaType TEXT NOT NULL DEFAULT 'NONE',
                        mediaUrl TEXT NOT NULL DEFAULT '',
                        fallbackMediaUrl TEXT NOT NULL DEFAULT '',
                        pbRecordId TEXT NOT NULL DEFAULT '',
                        pbCollectionId TEXT NOT NULL DEFAULT '',
                        pbFilename TEXT NOT NULL DEFAULT '',
                        mediaName TEXT NOT NULL DEFAULT '',
                        mediaSizeBytes INTEGER NOT NULL DEFAULT 0,
                        externalLink TEXT NOT NULL DEFAULT '',
                        authorDisplayName TEXT NOT NULL DEFAULT 'Superadmin',
                        authorSchoolName TEXT NOT NULL DEFAULT '',
                        isPinned INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS info_penting_reactions (
                        id INTEGER NOT NULL PRIMARY KEY,
                        schoolId TEXT NOT NULL DEFAULT '__GLOBAL__',
                        postId INTEGER NOT NULL DEFAULT 0,
                        userKey TEXT NOT NULL DEFAULT '',
                        userName TEXT NOT NULL DEFAULT '',
                        userSchoolId TEXT NOT NULL DEFAULT '',
                        reactionType TEXT NOT NULL DEFAULT 'NONE',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS info_penting_comments (
                        id INTEGER NOT NULL PRIMARY KEY,
                        schoolId TEXT NOT NULL DEFAULT '__GLOBAL__',
                        postId INTEGER NOT NULL DEFAULT 0,
                        parentCommentId INTEGER NOT NULL DEFAULT 0,
                        replyToAuthorName TEXT NOT NULL DEFAULT '',
                        authorKey TEXT NOT NULL DEFAULT '',
                        authorName TEXT NOT NULL DEFAULT '',
                        authorSchoolId TEXT NOT NULL DEFAULT '',
                        authorSchoolName TEXT NOT NULL DEFAULT '',
                        content TEXT NOT NULL DEFAULT '',
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_26_27 = object : androidx.room.migration.Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS parent_expectations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL,
                        studentName TEXT NOT NULL DEFAULT '',
                        studentNis TEXT NOT NULL DEFAULT '',
                        studentClass TEXT NOT NULL DEFAULT '',
                        studentDorm TEXT NOT NULL DEFAULT '',
                        parentName TEXT NOT NULL DEFAULT '',
                        waliUsername TEXT NOT NULL DEFAULT '',
                        waliName TEXT NOT NULL DEFAULT '',
                        characterFocusCsv TEXT NOT NULL DEFAULT '',
                        characterExpectation TEXT NOT NULL DEFAULT '',
                        adviceFocusNotes TEXT NOT NULL DEFAULT '',
                        parentPrayerMessage TEXT NOT NULL DEFAULT '',
                        isReadByWali INTEGER NOT NULL DEFAULT 0,
                        waliFollowUpNote TEXT NOT NULL DEFAULT '',
                        waliRespondedAt INTEGER NOT NULL DEFAULT 0,
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_27_28 = object : androidx.room.migration.Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE parent_access_links ADD COLUMN deletionRequested INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE parent_access_links ADD COLUMN deletionReason TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE parent_access_links ADD COLUMN deletionRequestedAt INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_28_29 = object : androidx.room.migration.Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE child_students ADD COLUMN photoUrl TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE child_students ADD COLUMN photoFallbackUrl TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE parent_access_links ADD COLUMN studentPhotoUrl TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE parent_access_links ADD COLUMN studentPhotoFallbackUrl TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_29_30 = object : androidx.room.migration.Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE wali_accounts ADD COLUMN photoUrl TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE wali_accounts ADD COLUMN photoFallbackUrl TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE wali_accounts ADD COLUMN titlePosition TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE wali_accounts ADD COLUMN employeeCode TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE wali_accounts ADD COLUMN bio TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE wali_accounts ADD COLUMN profileUpdatedAt INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_30_31 = object : androidx.room.migration.Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sholat_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dateKey TEXT NOT NULL,
                        dateMillis INTEGER NOT NULL,
                        waktu TEXT NOT NULL,
                        status TEXT NOT NULL,
                        disciplineTag TEXT NOT NULL DEFAULT '',
                        studentId INTEGER NOT NULL DEFAULT 0,
                        studentName TEXT NOT NULL,
                        studentNik TEXT NOT NULL DEFAULT '',
                        studentNis TEXT NOT NULL DEFAULT '',
                        studentClass TEXT NOT NULL DEFAULT '',
                        studentDorm TEXT NOT NULL DEFAULT '',
                        studentJenjang TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        inputMethod TEXT NOT NULL DEFAULT 'MANUAL',
                        recordedBy TEXT NOT NULL DEFAULT 'Wali Asuh',
                        recordedByUsername TEXT NOT NULL DEFAULT '',
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                try {
                    db.execSQL("ALTER TABLE parent_access_links ADD COLUMN canViewSholat INTEGER NOT NULL DEFAULT 1")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_31_32 = object : androidx.room.migration.Migration(31, 32) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS wali_gallery_posts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        waliUsername TEXT NOT NULL DEFAULT '',
                        waliName TEXT NOT NULL DEFAULT 'Wali Asuh',
                        caption TEXT NOT NULL DEFAULT '',
                        activityTag TEXT NOT NULL DEFAULT 'KEGIATAN',
                        targetScope TEXT NOT NULL DEFAULT 'ALL_MY_STUDENTS',
                        targetStudentIdsCsv TEXT NOT NULL DEFAULT '',
                        photoUrl TEXT NOT NULL DEFAULT '',
                        photoFallbackUrl TEXT NOT NULL DEFAULT '',
                        pbRecordId TEXT NOT NULL DEFAULT '',
                        pbCollectionId TEXT NOT NULL DEFAULT '',
                        pbFilename TEXT NOT NULL DEFAULT '',
                        retentionDays INTEGER NOT NULL DEFAULT 14,
                        expiresAt INTEGER NOT NULL DEFAULT 0,
                        expectedRecipientCount INTEGER NOT NULL DEFAULT 0,
                        downloadedByCsv TEXT NOT NULL DEFAULT '',
                        isMediaPurgedFromServer INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS wali_gallery_local_saves (
                        postId INTEGER PRIMARY KEY NOT NULL,
                        localGalleryUri TEXT NOT NULL DEFAULT '',
                        displayFileName TEXT NOT NULL DEFAULT '',
                        savedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_32_33 = object : androidx.room.migration.Migration(32, 33) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val alters = listOf(
                    "ALTER TABLE parent_messages ADD COLUMN photoUrl TEXT NOT NULL DEFAULT ''",
                    "ALTER TABLE parent_messages ADD COLUMN photoFallbackUrl TEXT NOT NULL DEFAULT ''",
                    "ALTER TABLE parent_messages ADD COLUMN pbRecordId TEXT NOT NULL DEFAULT ''",
                    "ALTER TABLE parent_messages ADD COLUMN pbCollectionId TEXT NOT NULL DEFAULT ''",
                    "ALTER TABLE parent_messages ADD COLUMN pbFilename TEXT NOT NULL DEFAULT ''",
                    "ALTER TABLE parent_messages ADD COLUMN isDownloadedByRecipient INTEGER NOT NULL DEFAULT 0",
                    "ALTER TABLE parent_messages ADD COLUMN isMediaPurgedFromServer INTEGER NOT NULL DEFAULT 0",
                    "ALTER TABLE parent_messages ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0"
                )
                alters.forEach { sql ->
                    try {
                        db.execSQL(sql)
                    } catch (_: Exception) {
                        // Column may already exist
                    }
                }
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS chat_media_local_saves (
                        messageId INTEGER PRIMARY KEY NOT NULL,
                        localGalleryUri TEXT NOT NULL DEFAULT '',
                        displayFileName TEXT NOT NULL DEFAULT '',
                        savedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_33_34 = object : androidx.room.migration.Migration(33, 34) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE child_students ADD COLUMN religion TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE parent_access_links ADD COLUMN studentReligion TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS christian_worship_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        schoolId TEXT NOT NULL DEFAULT 'SRT1-KDR',
                        studentId INTEGER NOT NULL DEFAULT 0,
                        studentName TEXT NOT NULL,
                        studentNik TEXT NOT NULL DEFAULT '',
                        studentNis TEXT NOT NULL DEFAULT '',
                        studentClass TEXT NOT NULL DEFAULT '',
                        studentDorm TEXT NOT NULL DEFAULT '',
                        activityType TEXT NOT NULL DEFAULT 'DOA_PAGI',
                        activityDate INTEGER NOT NULL DEFAULT 0,
                        dateKey TEXT NOT NULL DEFAULT '',
                        title TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        waliUsername TEXT NOT NULL DEFAULT '',
                        waliName TEXT NOT NULL DEFAULT 'Wali Asuh',
                        photoUrl TEXT NOT NULL DEFAULT '',
                        photoFallbackUrl TEXT NOT NULL DEFAULT '',
                        localPhotoPath TEXT NOT NULL DEFAULT '',
                        pbRecordId TEXT NOT NULL DEFAULT '',
                        pbCollectionId TEXT NOT NULL DEFAULT '',
                        pbFilename TEXT NOT NULL DEFAULT '',
                        proofUploadedAt INTEGER,
                        proofExpiresAt INTEGER,
                        proofStatus TEXT NOT NULL DEFAULT 'NONE',
                        isMediaPurgedFromServer INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS christian_worship_local_saves (
                        recordId INTEGER PRIMARY KEY NOT NULL,
                        localGalleryUri TEXT NOT NULL DEFAULT '',
                        displayFileName TEXT NOT NULL DEFAULT '',
                        savedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_34_35 = object : androidx.room.migration.Migration(34, 35) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE konseling_records ADD COLUMN photoPaths TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE konseling_records ADD COLUMN photoUrls TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
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
                        MIGRATION_22_23,
                        MIGRATION_23_24,
                        MIGRATION_24_25,
                        MIGRATION_25_26,
                        MIGRATION_26_27,
                        MIGRATION_27_28,
                        MIGRATION_28_29,
                        MIGRATION_29_30,
                        MIGRATION_30_31,
                        MIGRATION_31_32,
                        MIGRATION_32_33,
                        MIGRATION_33_34,
                        MIGRATION_34_35
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
                            parentPhone = obj.optString("parentPhone", obj.optString("noHpOrangTua", "")),
                            religion = com.example.data.model.StudentReligionHelper.normalize(
                                obj.optString("religion", obj.optString("agama", ""))
                            )
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
