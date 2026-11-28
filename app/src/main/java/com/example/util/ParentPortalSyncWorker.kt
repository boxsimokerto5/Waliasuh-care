package com.example.util

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.database.AppDatabase
import com.example.data.remote.pocketbase.PocketBaseConfig
import com.example.data.remote.pocketbase.PocketBaseSyncManager
import com.example.data.repository.HealthRepository
import java.util.concurrent.TimeUnit

class ParentPortalSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val authPrefs = applicationContext.getSharedPreferences("waliasuh_auth_prefs", Context.MODE_PRIVATE)
        val savedRole = authPrefs.getString("saved_role", "NOT_LOGGED_IN").orEmpty()
        val savedStudentId = authPrefs.getLong("saved_parent_student_id", 0L)
        val savedUsername = authPrefs.getString("saved_username", "").orEmpty()

        if (savedRole != "PARENT" || savedStudentId <= 0L) {
            return Result.success()
        }
        if (!ParentNotificationCenter.isBackgroundAlertsEnabled(applicationContext)) {
            return Result.success()
        }
        if (!PocketBaseConfig.isConfigured(applicationContext)) {
            return Result.success()
        }

        val schoolPrefs = applicationContext.getSharedPreferences("waliasuh_school_prefs", Context.MODE_PRIVATE)
        val cleanSchoolId = schoolPrefs.getString("tenant_code", "SRT1-KDR")
            .orEmpty()
            .trim()
            .ifBlank { "SRT1-KDR" }
        val schoolShortName = schoolPrefs.getString("short_name", "").orEmpty().trim()

        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = HealthRepository(
                db.childStudentDao(),
                db.medicalRecordDao(),
                db.waliAccountDao(),
                db.schoolRegistrationDao(),
                db.shiftMessageDao(),
                db.haidRecordDao(),
                db.fastingSessionDao(),
                db.fastingRecordDao(),
                db.mengajiRecordDao(),
                db.visitSessionDao(),
                db.visitRecordDao(),
                db.parentAccessLinkDao(),
                db.parentMessageDao(),
                db.deletionLogDao(),
                db.tabunganTransactionDao(),
                db.tabunganLogDao(),
                db.konselingRecordDao(),
                db.konselingLogDao(),
                db.waliShiftCodeDao(),
                db.waliScheduleEntryDao(),
                db.infoPentingPostDao(),
                db.infoPentingReactionDao(),
                db.infoPentingCommentDao(),
                db.parentExpectationDao(),
                db.sholatRecordDao(),
                db.waliGalleryPostDao(),
                db.waliGalleryLocalSaveDao(),
                db.chatMediaLocalSaveDao(),
                db.christianWorshipDao(),
                db.christianWorshipLocalSaveDao(),
                db.bibleBookmarkDao(),
                db.bibleNoteDao(),
                db.visitConfirmationDao(),
                db.visitSessionSwapDao(),
                db.sharedJournalNoteDao(),
                db.shiftHandoverDao()
            )

            runCatching {
                PocketBaseSyncManager.syncAllSchoolData(
                    context = applicationContext,
                    schoolId = cleanSchoolId,
                    repository = repository
                )
            }
            runCatching {
                PocketBaseSyncManager.syncWaliGalleryOnlineQuietly(
                    context = applicationContext,
                    schoolId = cleanSchoolId,
                    repository = repository
                )
            }
            runCatching {
                PocketBaseSyncManager.syncChristianWorshipOnlineQuietly(
                    context = applicationContext,
                    schoolId = cleanSchoolId,
                    repository = repository
                )
            }

            val snapshot = ParentNotificationCenter.fromRoomSnapshot(
                repository = repository,
                schoolId = cleanSchoolId,
                studentId = savedStudentId,
                username = savedUsername,
                schoolShortName = schoolShortName
            )
            if (snapshot != null) {
                ParentNotificationCenter.evaluateAndNotify(
                    context = applicationContext,
                    snapshot = snapshot,
                    triggerSystemNotification = true
                )
            }
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "Parent background sync failed (attempt $runAttemptCount): ${e.message}")
            if (runAttemptCount < 2) Result.retry() else Result.success()
        }
    }

    companion object {
        private const val TAG = "ParentPortalSyncWorker"
    }
}

object ParentSyncNotificationScheduler {
    private const val WORK_NAME = "parent_portal_periodic_sync"
    private const val TAG = "ParentSyncScheduler"

    private fun isRobolectric(): Boolean {
        return Build.FINGERPRINT.equals("robolectric", ignoreCase = true)
    }

    fun schedule(context: Context) {
        if (isRobolectric()) return
        if (!ParentNotificationCenter.isBackgroundAlertsEnabled(context)) {
            cancel(context)
            return
        }
        runCatching {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = PeriodicWorkRequestBuilder<ParentPortalSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }.onFailure { err ->
            Log.w(TAG, "Unable to schedule parent background sync: ${err.message}")
        }
    }

    fun cancel(context: Context) {
        if (isRobolectric()) return
        runCatching {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WORK_NAME)
        }.onFailure { err ->
            Log.w(TAG, "Unable to cancel parent background sync: ${err.message}")
        }
    }
}
