package com.example.data.remote.pocketbase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import com.example.data.model.BibleBookmark
import com.example.data.model.BibleNote
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.DashboardBannerConfig
import com.example.data.model.DashboardBannerItem
import com.example.data.model.DashboardBannerMediaKind
import com.example.data.model.DashboardIconKey
import com.example.data.model.DashboardIconLayoutConfig
import com.example.data.model.DeletionLog
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.InfoPentingComment
import com.example.data.model.InfoPentingHelper
import com.example.data.model.InfoPentingPost
import com.example.data.model.InfoPentingReaction
import com.example.data.model.KonselingHelper
import com.example.data.model.KonselingLog
import com.example.data.model.KonselingRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentExpectation
import com.example.data.model.ParentMessage
import com.example.data.model.SchoolProfile
import com.example.data.model.SchoolRegistration
import com.example.data.model.SharedJournalNote
import com.example.data.model.ShiftMessage
import com.example.data.model.SholatRecord
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganLog
import com.example.data.model.TabunganTransaction
import com.example.data.model.UploadedBannerMedia
import com.example.data.model.UploadedInfoPentingMedia
import com.example.data.model.VisitConfirmation
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionSwap
import com.example.data.model.WaliAccount
import com.example.data.model.WaliGalleryHelper
import com.example.data.model.WaliGalleryPost
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliShiftCode
import com.example.data.repository.HealthRepository
import com.example.util.WaliGalleryMediaHelper
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

object PocketBaseSyncManager {
    private const val TAG = "PocketBaseSyncManager"
    private val idSequence = AtomicLong(0L)

    /**
     * Menghasilkan ID unik berbasis timestamp agar antar-sekolah dan antar-perangkat
     * tidak saling menimpa baris data saat sinkronisasi ke PocketBase.
     */
    fun generateCloudSafeId(): Long {
        val nowBase = System.currentTimeMillis() * 1000L
        while (true) {
            val prev = idSequence.get()
            val next = if (nowBase > prev) nowBase else prev + 1L
            if (idSequence.compareAndSet(prev, next)) return next
        }
    }

    private fun buildSchoolFilter(schoolId: String): String {
        val escaped = schoolId.trim().replace("\\", "\\\\").replace("'", "\\'")
        return "(school_id='$escaped')"
    }

    private fun buildSessionFilter(sessionId: Long): String {
        return "(session_id=$sessionId)"
    }

    private suspend fun <T> fetchAllPages(
        fetcher: suspend (page: Int) -> Response<PbListResponse<T>>
    ): Result<List<T>> {
        val allItems = mutableListOf<T>()
        var page = 1
        while (true) {
            val resp = fetcher(page)
            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string() ?: "HTTP ${resp.code()}"
                return Result.failure(Exception(err))
            }
            val body = resp.body() ?: break
            allItems.addAll(body.items)
            if (page >= body.totalPages || body.items.isEmpty()) break
            page++
        }
        return Result.success(allItems)
    }

    private suspend fun <T : Any> upsertSingleDto(
        service: PocketBaseService,
        collection: String,
        recordId: String,
        dto: T
    ): Boolean {
        val body = PocketBaseClient.toJsonRequestBody(dto)
        val patchResp = service.updateRecord(collection, recordId, body)
        if (patchResp.isSuccessful) return true

        if (patchResp.code() == 404) {
            val createBody = PocketBaseClient.toJsonRequestBody(dto)
            val createResp = service.createRecord(collection, createBody)
            if (createResp.isSuccessful) return true

            // Jika bentrok paralel antar-perangkat saat create, ulangi PATCH satu kali
            if (createResp.code() == 400) {
                val retryPatch = service.updateRecord(collection, recordId, PocketBaseClient.toJsonRequestBody(dto))
                if (retryPatch.isSuccessful) return true
                Log.w(TAG, "Gagal upsert $collection/$recordId: ${retryPatch.code()} ${retryPatch.errorBody()?.string()}")
                return false
            }
            Log.w(TAG, "Gagal create $collection/$recordId: ${createResp.code()} ${createResp.errorBody()?.string()}")
            return false
        }

        Log.w(TAG, "Gagal patch $collection/$recordId: ${patchResp.code()} ${patchResp.errorBody()?.string()}")
        return false
    }

    /**
     * Mencoba /api/batch (jika diaktifkan di PocketBase Settings -> Application).
     * Bila Batch API belum diaktifkan (403/400/404/405), otomatis beralih ke PATCH/POST paralel per 10 record.
     */
    private suspend fun <T : Any> upsertBatchOrFallback(
        service: PocketBaseService,
        collection: String,
        pairs: List<Pair<String, T>>
    ): Result<Int> {
        if (pairs.isEmpty()) return Result.success(0)

        var batchSupported = true
        var uploaded = 0

        for (chunk in pairs.chunked(40)) {
            if (batchSupported) {
                val batchBody = PocketBaseClient.buildUpsertBatchRequestBody(collection, chunk.map { it.second })
                val batchResp = runCatching { service.executeBatch(batchBody) }.getOrNull()
                if (batchResp != null && batchResp.isSuccessful) {
                    uploaded += chunk.size
                    continue
                } else {
                    // Batch API nonaktif atau ditolak -> fallback per record secara paralel
                    batchSupported = false
                }
            }

            // Fallback paralel aman per 10 record
            for (subChunk in chunk.chunked(10)) {
                val results = coroutineScope {
                    subChunk.map { (recId, dto) ->
                        async { upsertSingleDto(service, collection, recId, dto) }
                    }.awaitAll()
                }
                uploaded += results.count { it }
            }
        }

        return Result.success(uploaded)
    }

    private suspend fun deleteCollectionByFilter(
        service: PocketBaseService,
        collection: String,
        filter: String
    ) {
        val idsResult = fetchAllPages { page ->
            service.listRecordIds(collection = collection, page = page, perPage = 500, filter = filter)
        }
        val ids = idsResult.getOrNull()?.map { it.id }?.filter { it.isNotBlank() }.orEmpty()
        if (ids.isEmpty()) return

        var batchSupported = true
        for (chunk in ids.chunked(40)) {
            if (batchSupported) {
                val batchBody = PocketBaseClient.buildDeleteBatchRequestBody(collection, chunk)
                val batchResp = runCatching { service.executeBatch(batchBody) }.getOrNull()
                if (batchResp != null && batchResp.isSuccessful) {
                    continue
                } else {
                    batchSupported = false
                }
            }
            for (subChunk in chunk.chunked(10)) {
                coroutineScope {
                    subChunk.map { id ->
                        async { runCatching { service.deleteRecord(collection, id) } }
                    }.awaitAll()
                }
            }
        }
    }

    /**
     * Menguji koneksi ke PocketBase dan memverifikasi akses ke koleksi.
     */
    suspend fun testConnection(context: Context): Result<String> = withContext(Dispatchers.IO) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.failure(
                Exception(if (isEn) "PocketBase server URL is empty." else "URL server PocketBase belum diisi.")
            )
        }

        val service = PocketBaseClient.getService(context)
            ?: return@withContext Result.failure(
                Exception(if (isEn) "Failed to create PocketBase connection." else "Gagal membuat koneksi ke PocketBase.")
            )

        try {
            val health = service.getHealth()
            if (!health.isSuccessful) {
                val err = health.errorBody()?.string() ?: "HTTP Status: ${health.code()}"
                return@withContext Result.failure(
                    Exception(
                        if (isEn) "Server not ready (${health.code()}): $err" else "Server belum siap (${health.code()}): $err"
                    )
                )
            }

            val studentResp = service.listStudents(page = 1, perPage = 1)
            if (studentResp.isSuccessful) {
                val totalStudents = studentResp.body()?.totalItems ?: 0
                Result.success(
                    if (isEn) {
                        "PocketBase connection succeeded! Server healthy & found $totalStudents students in cloud."
                    } else {
                        "Koneksi PocketBase berhasil! Server sehat & ditemukan $totalStudents siswa di cloud."
                    }
                )
            } else {
                val errorBody = studentResp.errorBody()?.string() ?: "HTTP Status: ${studentResp.code()}"
                Result.failure(
                    Exception(
                        if (isEn) {
                            "PocketBase collection response (${studentResp.code()}): $errorBody"
                        } else {
                            "Respon koleksi PocketBase (${studentResp.code()}): $errorBody"
                        }
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal uji koneksi PocketBase", e)
            Result.failure(
                Exception(
                    if (isEn) {
                        "Failed to reach PocketBase server: ${e.localizedMessage}"
                    } else {
                        "Gagal menghubungi server PocketBase: ${e.localizedMessage}"
                    }
                )
            )
        }
    }

    // --- Child Students ---
    suspend fun uploadStudents(context: Context, students: List<ChildStudent>): Result<Int> =
        withContext(Dispatchers.IO) {
            val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
            val service = PocketBaseClient.getService(context)
                ?: return@withContext Result.failure(
                    Exception(if (isEn) "PocketBase is not configured yet." else "PocketBase belum terkonfigurasi.")
                )

            if (students.isEmpty()) return@withContext Result.success(0)

            try {
                val pairs = students.map { student ->
                    val dto = PbStudentDto.fromDomain(student)
                    dto.id!! to dto
                }
                val res = upsertBatchOrFallback(service, "child_students", pairs)
                if (res.isSuccess) {
                    PocketBaseConfig.setLastSyncTime(context, System.currentTimeMillis())
                }
                res
            } catch (e: Exception) {
                Log.e(TAG, "Error uploadStudents", e)
                Result.failure(e)
            }
        }

    suspend fun uploadSingleStudent(context: Context, student: ChildStudent) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbStudentDto.fromDomain(student)
                upsertSingleDto(service, "child_students", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleStudent ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteStudent(context: Context, studentId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("child_students", PocketBaseIdCodec.toPbId(studentId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteStudent ke PocketBase", e)
            }
        }

    suspend fun deleteStudentsBySchool(context: Context, schoolId: String) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                deleteCollectionByFilter(service, "child_students", buildSchoolFilter(schoolId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteStudentsBySchool ke PocketBase", e)
            }
        }

    // --- Medical Records ---
    suspend fun uploadSingleRecord(
        context: Context,
        record: MedicalRecord,
        schoolId: String = "SRT1-KDR"
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        try {
            val service = PocketBaseClient.getService(context) ?: return@withContext
            val dto = PbMedicalRecordDto.fromDomain(record, schoolId)
            upsertSingleDto(service, "medical_records", dto.id!!, dto)
        } catch (e: Exception) {
            Log.e(TAG, "Silent error uploadSingleRecord ke PocketBase", e)
        }
    }

    suspend fun deleteRemoteRecord(context: Context, recordId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("medical_records", PocketBaseIdCodec.toPbId(recordId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteRecord ke PocketBase", e)
            }
        }

    // --- Haid Records ---
    suspend fun uploadSingleHaidRecord(context: Context, record: HaidRecord) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbHaidRecordDto.fromDomain(record)
                upsertSingleDto(service, "haid_records", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleHaidRecord ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteHaidRecord(context: Context, recordId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("haid_records", PocketBaseIdCodec.toPbId(recordId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteHaidRecord ke PocketBase", e)
            }
        }

    // --- Fasting Sessions & Records (Termasuk Checklist Sahur & Berbuka Online) ---
    suspend fun uploadSingleFastingSession(context: Context, session: FastingSession) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbFastingSessionDto.fromDomain(session)
                upsertSingleDto(service, "fasting_sessions", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleFastingSession ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteFastingSession(context: Context, sessionId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                deleteCollectionByFilter(service, "fasting_records", buildSessionFilter(sessionId))
                service.deleteRecord("fasting_sessions", PocketBaseIdCodec.toPbId(sessionId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteFastingSession ke PocketBase", e)
            }
        }

    suspend fun uploadSingleFastingRecord(context: Context, record: FastingRecord) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbFastingRecordDto.fromDomain(record)
                upsertSingleDto(service, "fasting_records", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleFastingRecord ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteFastingRecord(context: Context, recordId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("fasting_records", PocketBaseIdCodec.toPbId(recordId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteFastingRecord ke PocketBase", e)
            }
        }

    // --- Mengaji Records ---
    suspend fun uploadSingleMengajiRecord(context: Context, record: MengajiRecord) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbMengajiRecordDto.fromDomain(record)
                upsertSingleDto(service, "mengaji_records", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleMengajiRecord ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteMengajiRecord(context: Context, recordId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("mengaji_records", PocketBaseIdCodec.toPbId(recordId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteMengajiRecord ke PocketBase", e)
            }
        }

    // --- Absen Sholat 5 Waktu ---
    suspend fun uploadSingleSholatRecord(context: Context, record: SholatRecord) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbSholatRecordDto.fromDomain(record)
                upsertSingleDto(service, "sholat_records", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleSholatRecord ke PocketBase", e)
            }
        }

    suspend fun uploadSholatRecords(context: Context, records: List<SholatRecord>) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context) || records.isEmpty()) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val pairs = records.map { rec ->
                    val dto = PbSholatRecordDto.fromDomain(rec)
                    dto.id!! to dto
                }
                upsertBatchOrFallback(service, "sholat_records", pairs)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSholatRecords ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteSholatRecord(context: Context, recordId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("sholat_records", PocketBaseIdCodec.toPbId(recordId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteSholatRecord ke PocketBase", e)
            }
        }

    // --- Parent Visit (Kunjungan) Sessions & Records ---
    suspend fun uploadSingleVisitSession(context: Context, session: VisitSession) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbVisitSessionDto.fromDomain(session)
                upsertSingleDto(service, "visit_sessions", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleVisitSession ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteVisitSession(context: Context, sessionId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                deleteCollectionByFilter(service, "visit_records", buildSessionFilter(sessionId))
                runCatching { deleteCollectionByFilter(service, "visit_confirmations", buildSessionFilter(sessionId)) }
                runCatching {
                    deleteCollectionByFilter(
                        service,
                        "visit_session_swaps",
                        "from_session_id = $sessionId || to_session_id = $sessionId"
                    )
                }
                service.deleteRecord("visit_sessions", PocketBaseIdCodec.toPbId(sessionId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteVisitSession ke PocketBase", e)
            }
        }

    suspend fun uploadSingleVisitRecord(context: Context, record: VisitRecord) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbVisitRecordDto.fromDomain(record)
                upsertSingleDto(service, "visit_records", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleVisitRecord ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteVisitRecord(context: Context, recordId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("visit_records", PocketBaseIdCodec.toPbId(recordId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteVisitRecord ke PocketBase", e)
            }
        }

    suspend fun uploadSingleVisitConfirmation(context: Context, confirmation: VisitConfirmation) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbVisitConfirmationDto.fromDomain(confirmation)
                upsertSingleDto(service, "visit_confirmations", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleVisitConfirmation ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteVisitConfirmation(context: Context, confirmationId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("visit_confirmations", PocketBaseIdCodec.toPbId(confirmationId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteVisitConfirmation ke PocketBase", e)
            }
        }

    suspend fun uploadSingleVisitSessionSwap(context: Context, swap: VisitSessionSwap) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbVisitSessionSwapDto.fromDomain(swap)
                upsertSingleDto(service, "visit_session_swaps", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleVisitSessionSwap ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteVisitSessionSwap(context: Context, swapId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("visit_session_swaps", PocketBaseIdCodec.toPbId(swapId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteVisitSessionSwap ke PocketBase", e)
            }
        }

    // --- Wali Accounts ---
    suspend fun uploadWaliAccounts(context: Context, accounts: List<WaliAccount>) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context) || accounts.isEmpty()) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val pairs = accounts.map { acc ->
                    val dto = PbWaliAccountDto.fromDomain(acc)
                    dto.id!! to dto
                }
                upsertBatchOrFallback(service, "wali_accounts", pairs)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadWaliAccounts ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteWaliAccount(context: Context, accountId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("wali_accounts", PocketBaseIdCodec.toPbId(accountId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteWaliAccount ke PocketBase", e)
            }
        }

    suspend fun deleteWaliAccountsBySchool(context: Context, schoolId: String) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                deleteCollectionByFilter(service, "wali_accounts", buildSchoolFilter(schoolId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteWaliAccountsBySchool ke PocketBase", e)
            }
        }

    // --- Shift Messages ---
    suspend fun uploadSingleShiftMessage(context: Context, message: ShiftMessage) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbShiftMessageDto.fromDomain(message)
                upsertSingleDto(service, "shift_messages", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleShiftMessage ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteShiftMessage(context: Context, messageId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("shift_messages", PocketBaseIdCodec.toPbId(messageId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteShiftMessage ke PocketBase", e)
            }
        }

    suspend fun deleteShiftMessagesBySchool(context: Context, schoolId: String) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                deleteCollectionByFilter(service, "shift_messages", buildSchoolFilter(schoolId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteShiftMessagesBySchool ke PocketBase", e)
            }
        }

    // --- School Registrations ---
    suspend fun uploadSingleSchoolRegistration(context: Context, reg: SchoolRegistration) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbSchoolRegistrationDto.fromDomain(reg)
                upsertSingleDto(service, "school_registrations", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleSchoolRegistration ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteSchoolRegistration(context: Context, regId: Long) =
        deleteRemoteSchoolRegistration(
            context,
            SchoolRegistration(
                id = regId,
                schoolName = "",
                address = "",
                city = "",
                estimatedWaliCount = 0,
                picName = "",
                picEmail = "",
                picPhone = ""
            )
        )

    suspend fun deleteRemoteSchoolRegistration(context: Context, reg: SchoolRegistration) =
        withContext(Dispatchers.IO) {
            PocketBaseConfig.markSchoolRegistrationDeleted(context, reg)
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                if (reg.id > 0L) {
                    runCatching {
                        service.deleteRecord("school_registrations", PocketBaseIdCodec.toPbId(reg.id))
                    }
                }
                val remoteRegs = fetchAllPages { page ->
                    service.listSchoolRegistrations(page = page, perPage = 500)
                }.getOrNull().orEmpty()

                val targetCode = reg.generatedSchoolCode.trim()
                val targetName = reg.schoolName.trim()
                val targetPhone = reg.picPhone.trim()
                val matchingIds = remoteRegs.filter { dto ->
                    val domain = dto.toDomainModel()
                    val idMatch = reg.id > 0L && (
                        dto.localId == reg.id ||
                        domain.id == reg.id ||
                        dto.id == PocketBaseIdCodec.toPbId(reg.id)
                    )
                    val codeMatch = targetCode.isNotBlank() &&
                        domain.generatedSchoolCode.trim().equals(targetCode, ignoreCase = true)
                    val sigMatch = targetName.isNotBlank() &&
                        domain.schoolName.trim().equals(targetName, ignoreCase = true) &&
                        domain.picPhone.trim() == targetPhone &&
                        (reg.submissionDate <= 0L || domain.submissionDate == reg.submissionDate)
                    idMatch || codeMatch || sigMatch
                }.mapNotNull { it.id?.takeIf { id -> id.isNotBlank() } }.distinct()

                for (remoteId in matchingIds) {
                    runCatching { service.deleteRecord("school_registrations", remoteId) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteSchoolRegistration ke PocketBase", e)
            }
        }

    // --- School Profiles (Tabel ke-9) ---
    suspend fun uploadSchoolProfile(
        context: Context,
        profile: SchoolProfile,
        updatedAt: Long = System.currentTimeMillis()
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context) || profile.tenantCode.isBlank()) return@withContext
        try {
            val service = PocketBaseClient.getService(context) ?: return@withContext
            val dto = PbSchoolProfileDto.fromDomain(profile, updatedAt)
            upsertSingleDto(service, "school_profiles", dto.id!!, dto)
        } catch (e: Exception) {
            Log.e(TAG, "Silent error uploadSchoolProfile ke PocketBase", e)
        }
    }

    suspend fun fetchAllRemoteSchoolProfiles(
        context: Context
    ): List<Pair<SchoolProfile, Long>> = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext emptyList()
        val service = PocketBaseClient.getService(context) ?: return@withContext emptyList()
        return@withContext try {
            val res = fetchAllPages { page ->
                service.listSchoolProfiles(page = page, perPage = 500)
            }
            res.getOrNull()
                ?.mapNotNull { dto ->
                    val rawSchoolId = dto.schoolId?.trim().orEmpty()
                    if (rawSchoolId.isBlank() ||
                        rawSchoolId.equals(PbSchoolProfileDto.GLOBAL_DASHBOARD_LAYOUT_SCHOOL_ID, ignoreCase = true) ||
                        rawSchoolId.equals(PbSchoolProfileDto.GLOBAL_DASHBOARD_BANNER_SCHOOL_ID, ignoreCase = true) ||
                        PocketBaseConfig.isSchoolCodeDeleted(context, rawSchoolId)
                    ) {
                        null
                    } else {
                        dto.toDomainModel(rawSchoolId) to (dto.updatedAt ?: 0L)
                    }
                }
                .orEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Silent error fetchAllRemoteSchoolProfiles ke PocketBase", e)
            emptyList()
        }
    }

    // --- Global Dashboard Icon Layout (Disimpan di school_profiles dengan tenant khusus) ---
    suspend fun uploadDashboardIconLayout(
        context: Context,
        order: List<DashboardIconKey>,
        updatedAt: Long = System.currentTimeMillis()
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.failure(
                Exception(if (isEn) "PocketBase is not configured yet." else "PocketBase belum terkonfigurasi.")
            )
        }
        val service = PocketBaseClient.getService(context)
            ?: return@withContext Result.failure(
                Exception(if (isEn) "Failed to connect to PocketBase." else "Gagal terhubung ke layanan PocketBase.")
            )
        return@withContext try {
            val dto = PbSchoolProfileDto.fromDashboardLayout(order, updatedAt)
            val ok = upsertSingleDto(service, "school_profiles", dto.id!!, dto)
            if (ok) {
                PocketBaseConfig.setLastSyncTime(context, System.currentTimeMillis())
                Result.success(true)
            } else {
                Result.failure(
                    Exception(
                        if (isEn) "Failed to save dashboard layout to PocketBase cloud." else "Gagal menyimpan tata letak ikon ke cloud PocketBase."
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploadDashboardIconLayout", e)
            Result.failure(e)
        }
    }

    private suspend fun syncDashboardIconLayoutMerged(
        service: PocketBaseService,
        localOrder: List<DashboardIconKey>,
        localUpdatedAt: Long,
        allowPushToCloud: Boolean,
        onRemoteLayoutUpdated: ((List<DashboardIconKey>, Long) -> Unit)?
    ) {
        val globalFilter = buildSchoolFilter(PbSchoolProfileDto.GLOBAL_DASHBOARD_LAYOUT_SCHOOL_ID)
        val remoteRes = fetchAllPages { page ->
            service.listSchoolProfiles(page = page, perPage = 10, filter = globalFilter)
        }
        if (!remoteRes.isSuccess) return
        val remoteConfig = remoteRes.getOrNull()
            ?.mapNotNull { it.toDashboardLayoutConfig() }
            ?.maxByOrNull { it.updatedAt }

        if (remoteConfig != null) {
            if (remoteConfig.updatedAt > localUpdatedAt) {
                onRemoteLayoutUpdated?.invoke(remoteConfig.order, remoteConfig.updatedAt)
            } else if (allowPushToCloud && localUpdatedAt > remoteConfig.updatedAt) {
                val dto = PbSchoolProfileDto.fromDashboardLayout(localOrder, localUpdatedAt)
                upsertSingleDto(service, "school_profiles", dto.id!!, dto)
            }
        } else if (allowPushToCloud && localUpdatedAt > 0L) {
            val dto = PbSchoolProfileDto.fromDashboardLayout(localOrder, localUpdatedAt)
            upsertSingleDto(service, "school_profiles", dto.id!!, dto)
        }
    }

    suspend fun syncDashboardIconLayoutQuietly(
        context: Context,
        localOrder: List<DashboardIconKey>,
        localUpdatedAt: Long,
        allowPushToCloud: Boolean = false,
        onRemoteLayoutUpdated: (List<DashboardIconKey>, Long) -> Unit
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext
        runCatching {
            syncDashboardIconLayoutMerged(
                service = service,
                localOrder = localOrder,
                localUpdatedAt = localUpdatedAt,
                allowPushToCloud = allowPushToCloud,
                onRemoteLayoutUpdated = onRemoteLayoutUpdated
            )
        }
    }

    // --- Global Dashboard Banner Carousel & Cloudflare R2 CDN Image Upload ---
    private fun compressBannerImageBytes(context: Context, imageUri: Uri): ByteArray? {
        return runCatching {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOpts)
            }
            val srcW = boundsOpts.outWidth
            val srcH = boundsOpts.outHeight
            var sampleSize = 1
            if (srcW > 1440 || srcH > 1440) {
                while ((srcW / sampleSize) > 1440 || (srcH / sampleSize) > 1440) {
                    sampleSize *= 2
                }
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = context.contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOpts)
            } ?: return@runCatching null

            val maxDim = 1280
            val scaled = if (decoded.width > maxDim || decoded.height > maxDim) {
                val ratio = minOf(maxDim.toFloat() / decoded.width, maxDim.toFloat() / decoded.height)
                val targetW = (decoded.width * ratio).toInt().coerceAtLeast(1)
                val targetH = (decoded.height * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(decoded, targetW, targetH, true).also {
                    if (it !== decoded) decoded.recycle()
                }
            } else {
                decoded
            }
            ByteArrayOutputStream().use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
                scaled.recycle()
                out.toByteArray()
            }
        }.getOrNull()
    }

    internal fun parsePocketBaseUploadError(raw: String, httpCode: Int, isEn: Boolean): String {
        val json = runCatching { JSONObject(raw) }.getOrNull()
        val message = json?.optString("message")?.trim().orEmpty()
        val dataObj = json?.optJSONObject("data")
        val fieldDetails = mutableListOf<String>()
        if (dataObj != null) {
            val keys = dataObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val childObj = dataObj.optJSONObject(key)
                if (childObj != null) {
                    val fMsg = childObj.optString("message").trim()
                    val fCode = childObj.optString("code").trim()
                    val combined = listOfNotNull(
                        fMsg.takeIf { it.isNotBlank() },
                        fCode.takeIf { it.isNotBlank() }?.let { "($it)" }
                    ).joinToString(" ")
                    if (combined.isNotBlank()) fieldDetails.add("$key: $combined")
                } else {
                    val strVal = dataObj.optString(key).trim()
                    if (strVal.isNotBlank()) fieldDetails.add("$key: $strVal")
                }
            }
        }
        val baseErr = buildString {
            append(message.ifBlank { "HTTP $httpCode" })
            if (fieldDetails.isNotEmpty()) {
                append(" [")
                append(fieldDetails.joinToString("; "))
                append("]")
            }
        }
        val looksLikeRecordCreateFailure = message.contains("Failed to create record", ignoreCase = true) ||
            httpCode == 400
        return if (looksLikeRecordCreateFailure && fieldDetails.isEmpty()) {
            val hint = if (isEn) {
                "Check PocketBase Settings -> Files storage (S3): set Region to 'auto' (not 'Asia-Pacific') and Endpoint without trailing bucket name."
            } else {
                "Periksa Settings -> Files storage (S3) di PocketBase: ubah Region menjadi 'auto' (bukan 'Asia-Pacific') dan pastikan Endpoint tanpa nama bucket."
            }
            "$baseErr — $hint"
        } else {
            baseErr
        }
    }

    private data class ResolvedBannerUploadSpec(
        val mediaKind: DashboardBannerMediaKind,
        val filename: String,
        val mimeType: String,
        val body: okhttp3.RequestBody
    )

    private fun queryUriMeta(context: Context, uri: Uri): Pair<String?, Long> {
        var displayName: String? = null
        var size = -1L
        runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx >= 0) displayName = cursor.getString(nameIdx)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
                }
            }
        }
        return displayName to size
    }

    private fun peekMagicBytes(context: Context, uri: Uri): ByteArray {
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val buf = ByteArray(16)
                val n = input.read(buf)
                if (n > 0) buf.copyOf(n) else ByteArray(0)
            } ?: ByteArray(0)
        }.getOrDefault(ByteArray(0))
    }

    private fun readUriBytesLimited(context: Context, uri: Uri, maxBytes: Long): ByteArray? {
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val out = ByteArrayOutputStream()
                val buf = ByteArray(16 * 1024)
                var total = 0L
                while (true) {
                    val read = input.read(buf)
                    if (read <= 0) break
                    total += read
                    if (total > maxBytes) return@use null
                    out.write(buf, 0, read)
                }
                out.toByteArray()
            }
        }.getOrNull()
    }

    private fun buildContentStreamRequestBody(
        context: Context,
        uri: Uri,
        mediaType: okhttp3.MediaType,
        knownLength: Long
    ): okhttp3.RequestBody = object : okhttp3.RequestBody() {
        override fun contentType() = mediaType
        override fun contentLength(): Long = if (knownLength > 0L) knownLength else -1L
        override fun writeTo(sink: okio.BufferedSink) {
            val stream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Unable to open banner media stream.")
            stream.use { input ->
                val buffer = ByteArray(32 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    sink.write(buffer, 0, read)
                }
            }
        }
    }

    suspend fun uploadBannerImageToCloud(
        context: Context,
        imageUri: Uri,
        bannerId: String,
        title: String
    ): Result<UploadedBannerMedia> = withContext(Dispatchers.IO) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.failure(
                Exception(if (isEn) "PocketBase server is not configured." else "Server PocketBase belum terkonfigurasi.")
            )
        }
        val baseUrl = PocketBaseConfig.getBaseUrl(context).trim().trimEnd('/')
        if (!baseUrl.startsWith("http")) {
            return@withContext Result.failure(
                Exception(if (isEn) "Invalid PocketBase URL." else "URL PocketBase tidak valid.")
            )
        }

        val now = System.currentTimeMillis()
        val mimeHint = context.contentResolver.getType(imageUri).orEmpty().lowercase()
        val (displayName, queriedSize) = queryUriMeta(context, imageUri)
        val extHint = displayName.orEmpty().substringAfterLast('.', "").lowercase()
        val header = peekMagicBytes(context, imageUri)
        val isGifMagic = header.size >= 6 &&
            header[0] == 'G'.code.toByte() &&
            header[1] == 'I'.code.toByte() &&
            header[2] == 'F'.code.toByte() &&
            header[3] == '8'.code.toByte()
        val isWebpMagic = header.size >= 12 &&
            header[0] == 'R'.code.toByte() &&
            header[1] == 'I'.code.toByte() &&
            header[2] == 'F'.code.toByte() &&
            header[3] == 'F'.code.toByte() &&
            header[8] == 'W'.code.toByte() &&
            header[9] == 'E'.code.toByte() &&
            header[10] == 'B'.code.toByte() &&
            header[11] == 'P'.code.toByte()
        val isMp4Magic = header.size >= 8 &&
            header[4] == 'f'.code.toByte() &&
            header[5] == 't'.code.toByte() &&
            header[6] == 'y'.code.toByte() &&
            header[7] == 'p'.code.toByte()
        val isWebmMagic = header.size >= 4 &&
            header[0] == 0x1A.toByte() &&
            header[1] == 0x45.toByte() &&
            header[2] == 0xDF.toByte() &&
            header[3] == 0xA3.toByte()

        val isVideo = mimeHint.startsWith("video/") ||
            extHint in listOf("mp4", "webm", "mov", "m4v", "mkv", "3gp") ||
            isMp4Magic || isWebmMagic
        val isGif = !isVideo && (mimeHint == "image/gif" || extHint == "gif" || isGifMagic)
        val isWebp = !isVideo && !isGif && (mimeHint == "image/webp" || extHint == "webp" || isWebpMagic)

        val maxVideoBytes = 20L * 1024L * 1024L
        val maxGifBytes = 10L * 1024L * 1024L

        val spec: ResolvedBannerUploadSpec = when {
            isVideo -> {
                if (queriedSize > maxVideoBytes) {
                    return@withContext Result.failure(
                        Exception(
                            if (isEn) "Maximum banner video size is 20 MB."
                            else "Ukuran video banner maksimal 20 MB agar pemuatan tetap ringan."
                        )
                    )
                }
                val isWebm = mimeHint.contains("webm") || extHint == "webm" || isWebmMagic
                val videoExt = if (isWebm) "webm" else "mp4"
                val videoMime = if (isWebm) "video/webm" else "video/mp4"
                ResolvedBannerUploadSpec(
                    mediaKind = DashboardBannerMediaKind.VIDEO,
                    filename = "banner_${now}.$videoExt",
                    mimeType = videoMime,
                    body = buildContentStreamRequestBody(
                        context = context,
                        uri = imageUri,
                        mediaType = videoMime.toMediaType(),
                        knownLength = queriedSize
                    )
                )
            }
            isGif || isWebp -> {
                val rawBytes = readUriBytesLimited(context, imageUri, maxGifBytes)
                    ?: return@withContext Result.failure(
                        Exception(
                            if (isEn) "Unable to read GIF/WebP or file exceeds 10 MB."
                            else "Gagal membaca GIF/WebP atau ukuran file melebihi 10 MB."
                        )
                    )
                val ext = if (isGif) "gif" else "webp"
                val mime = if (isGif) "image/gif" else "image/webp"
                ResolvedBannerUploadSpec(
                    mediaKind = if (isGif) DashboardBannerMediaKind.GIF else DashboardBannerMediaKind.IMAGE,
                    filename = "banner_${now}.$ext",
                    mimeType = mime,
                    body = rawBytes.toRequestBody(mime.toMediaType())
                )
            }
            else -> {
                val imageBytes = compressBannerImageBytes(context, imageUri)
                    ?: return@withContext Result.failure(
                        Exception(if (isEn) "Unable to read selected image." else "Gagal membaca file gambar yang dipilih.")
                    )
                ResolvedBannerUploadSpec(
                    mediaKind = DashboardBannerMediaKind.IMAGE,
                    filename = "banner_${now}.jpg",
                    mimeType = "image/jpeg",
                    body = imageBytes.toRequestBody("image/jpeg".toMediaType())
                )
            }
        }

        val cleanBannerId = bannerId.ifBlank { "banner_$now" }
        val localHash = PocketBaseIdCodec.stablePositiveHash("banner_media:$cleanBannerId:$now")

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("local_id", localHash.toString())
            .addFormDataPart("school_id", "__GLOBAL__")
            .addFormDataPart("banner_id", cleanBannerId)
            .addFormDataPart("title", title.ifBlank { "Dashboard Banner" })
            .addFormDataPart("is_enabled", "true")
            .addFormDataPart("updated_at", now.toString())
            .addFormDataPart("image", spec.filename, spec.body)
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("$baseUrl/api/collections/dashboard_banners/records")
            .post(multipartBody)
            .build()

        return@withContext try {
            client.newCall(request).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (resp.code == 404) {
                    return@withContext Result.failure(
                        Exception(
                            if (isEn) {
                                "Collection 'dashboard_banners' not synced on server yet. Run Sync Schema in SuperAdmin or paste a CDN URL."
                            } else {
                                "Koleksi 'dashboard_banners' belum disinkronkan di server. Jalankan Sync Skema di menu SuperAdmin atau tempel URL CDN."
                            }
                        )
                    )
                }
                if (!resp.isSuccessful) {
                    Log.e(TAG, "Banner upload HTTP ${resp.code}: $raw")
                    val errMsg = parsePocketBaseUploadError(raw, resp.code, isEn)
                    return@withContext Result.failure(
                        Exception(
                            if (isEn) "Failed to upload banner media: $errMsg" else "Gagal mengunggah media banner: $errMsg"
                        )
                    )
                }
                val json = JSONObject(raw)
                val recordId = json.optString("id", "").trim()
                val collectionId = json.optString("collectionId", "dashboard_banners").ifBlank { "dashboard_banners" }
                val uploadedFilename = when {
                    json.has("image") && json.optJSONArray("image") != null -> {
                        json.optJSONArray("image")?.optString(0, "").orEmpty()
                    }
                    else -> json.optString("image", "")
                }.trim()
                if (recordId.isBlank() || uploadedFilename.isBlank()) {
                    return@withContext Result.failure(
                        Exception(
                            if (isEn) "PocketBase did not return uploaded media metadata." else "PocketBase tidak mengembalikan metadata media."
                        )
                    )
                }
                val cdnUrl = DashboardBannerItem.buildR2CdnUrl(collectionId, recordId, uploadedFilename)
                val fallbackUrl = DashboardBannerItem.buildPbApiFileUrl(baseUrl, collectionId, recordId, uploadedFilename)
                Result.success(
                    UploadedBannerMedia(
                        pbRecordId = recordId,
                        pbCollectionId = collectionId,
                        pbFilename = uploadedFilename,
                        cdnImageUrl = cdnUrl,
                        fallbackApiFileUrl = fallbackUrl,
                        mediaType = spec.mediaKind.key
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploadBannerImageToCloud", e)
            Result.failure(e)
        }
    }

    suspend fun uploadDashboardBannerConfig(
        context: Context,
        config: DashboardBannerConfig
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.failure(
                Exception(if (isEn) "PocketBase is not configured yet." else "PocketBase belum terkonfigurasi.")
            )
        }
        val service = PocketBaseClient.getService(context)
            ?: return@withContext Result.failure(
                Exception(if (isEn) "Failed to connect to PocketBase." else "Gagal terhubung ke layanan PocketBase.")
            )
        return@withContext try {
            val dto = PbSchoolProfileDto.fromDashboardBannerConfig(config)
            val ok = upsertSingleDto(service, "school_profiles", dto.id!!, dto)
            if (ok) {
                PocketBaseConfig.setLastSyncTime(context, System.currentTimeMillis())
                Result.success(true)
            } else {
                Result.failure(
                    Exception(
                        if (isEn) "Failed to save dashboard banners to PocketBase cloud." else "Gagal menyimpan banner dashboard ke cloud PocketBase."
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploadDashboardBannerConfig", e)
            Result.failure(e)
        }
    }

    private suspend fun syncDashboardBannerConfigMerged(
        service: PocketBaseService,
        localConfig: DashboardBannerConfig,
        allowPushToCloud: Boolean,
        onRemoteBannerUpdated: ((DashboardBannerConfig) -> Unit)?
    ) {
        val globalFilter = buildSchoolFilter(PbSchoolProfileDto.GLOBAL_DASHBOARD_BANNER_SCHOOL_ID)
        val remoteRes = fetchAllPages { page ->
            service.listSchoolProfiles(page = page, perPage = 10, filter = globalFilter)
        }
        if (!remoteRes.isSuccess) return
        val remoteConfig = remoteRes.getOrNull()
            ?.mapNotNull { it.toDashboardBannerConfig() }
            ?.maxByOrNull { it.updatedAt }

        if (remoteConfig != null) {
            if (remoteConfig.updatedAt > localConfig.updatedAt) {
                onRemoteBannerUpdated?.invoke(remoteConfig)
            } else if (allowPushToCloud && localConfig.updatedAt > remoteConfig.updatedAt) {
                val dto = PbSchoolProfileDto.fromDashboardBannerConfig(localConfig)
                upsertSingleDto(service, "school_profiles", dto.id!!, dto)
            }
        } else if (allowPushToCloud && localConfig.updatedAt > 0L) {
            val dto = PbSchoolProfileDto.fromDashboardBannerConfig(localConfig)
            upsertSingleDto(service, "school_profiles", dto.id!!, dto)
        }
    }

    suspend fun syncDashboardBannerConfigQuietly(
        context: Context,
        localConfig: DashboardBannerConfig,
        allowPushToCloud: Boolean = false,
        onRemoteBannerUpdated: (DashboardBannerConfig) -> Unit
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext
        runCatching {
            syncDashboardBannerConfigMerged(
                service = service,
                localConfig = localConfig,
                allowPushToCloud = allowPushToCloud,
                onRemoteBannerUpdated = onRemoteBannerUpdated
            )
        }
    }

    // --- Parent Access Links & Parent Messages ---
    suspend fun uploadSingleParentAccessLink(context: Context, link: ParentAccessLink) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbParentAccessLinkDto.fromDomain(link)
                upsertSingleDto(service, "parent_access_links", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleParentAccessLink ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteParentAccessLink(context: Context, linkId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("parent_access_links", PocketBaseIdCodec.toPbId(linkId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteParentAccessLink ke PocketBase", e)
            }
        }

    suspend fun uploadSingleParentMessage(context: Context, message: ParentMessage) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbParentMessageDto.fromDomain(message)
                upsertSingleDto(service, "parent_messages", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleParentMessage ke PocketBase", e)
            }
        }

    suspend fun uploadParentMessageWithPhoto(
        context: Context,
        draftMessage: ParentMessage,
        compressedBytes: ByteArray
    ): Result<ParentMessage> = withContext(Dispatchers.IO) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.failure(
                Exception(if (isEn) "PocketBase server is not configured." else "Server PocketBase belum dikonfigurasi.")
            )
        }
        val baseUrl = PocketBaseConfig.getBaseUrl(context).trim().trimEnd('/')
        if (!baseUrl.startsWith("http")) {
            return@withContext Result.failure(
                Exception(if (isEn) "Invalid PocketBase URL." else "URL PocketBase tidak valid.")
            )
        }

        val safeId = if (draftMessage.id > 0L) draftMessage.id else generateCloudSafeId()
        val pbId = PocketBaseIdCodec.toPbId(safeId)
        val filename = "chat_${safeId}.webp"
        val cleanSchool = draftMessage.schoolId.trim().ifBlank { "SRT1-KDR" }

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("id", pbId)
            .addFormDataPart("local_id", safeId.toString())
            .addFormDataPart("school_id", cleanSchool)
            .addFormDataPart("student_id", draftMessage.studentId.toString())
            .addFormDataPart("student_name", draftMessage.studentName)
            .addFormDataPart("parent_name", draftMessage.parentName)
            .addFormDataPart("wali_username", draftMessage.waliUsername)
            .addFormDataPart("wali_name", draftMessage.waliName)
            .addFormDataPart("sender_role", draftMessage.senderRole)
            .addFormDataPart("sender_name", draftMessage.senderName)
            .addFormDataPart("message_content", draftMessage.messageContent)
            .addFormDataPart("is_read_by_wali", draftMessage.isReadByWali.toString())
            .addFormDataPart("is_read_by_parent", draftMessage.isReadByParent.toString())
            .addFormDataPart("is_downloaded_by_recipient", "false")
            .addFormDataPart("is_media_purged_from_server", "false")
            .addFormDataPart("created_at", draftMessage.createdAt.toString())
            .addFormDataPart("updated_at", draftMessage.updatedAt.coerceAtLeast(draftMessage.createdAt).toString())
            .addFormDataPart("photo_file", filename, compressedBytes.toRequestBody("image/webp".toMediaType()))
            .build()

        val postRequest = Request.Builder()
            .url("$baseUrl/api/collections/parent_messages/records")
            .post(multipart)
            .build()

        return@withContext try {
            var responseCode = 0
            var responseRaw = ""
            var wasSuccess = false

            client.newCall(postRequest).execute().use { resp ->
                responseCode = resp.code
                responseRaw = resp.body?.string().orEmpty()
                wasSuccess = resp.isSuccessful
            }

            if (responseCode == 404) {
                return@withContext Result.failure(
                    Exception(
                        if (isEn) {
                            "Collection 'parent_messages' is not synced on server yet. Run Sync Schema in Superadmin."
                        } else {
                            "Koleksi 'parent_messages' belum disinkronkan di server. Jalankan Sync Skema di Superadmin terlebih dahulu."
                        }
                    )
                )
            }

            if (!wasSuccess) {
                val errMsg = parsePocketBaseUploadError(responseRaw, responseCode, isEn)
                return@withContext Result.failure(
                    Exception(if (isEn) "Failed to upload chat photo: $errMsg" else "Gagal mengunggah foto chat ke R2/PocketBase: $errMsg")
                )
            }

            val json = JSONObject(responseRaw)
            val recordId = json.optString("id", pbId).trim().ifBlank { pbId }
            val collectionId = json.optString("collectionId", "parent_messages").ifBlank { "parent_messages" }
            val uploadedFilename = when {
                json.has("photo_file") && json.optJSONArray("photo_file") != null -> {
                    json.optJSONArray("photo_file")?.optString(0, "").orEmpty()
                }
                else -> json.optString("photo_file", "")
            }.trim()

            if (uploadedFilename.isBlank()) {
                return@withContext Result.failure(
                    Exception(
                        if (isEn) {
                            "Column 'photo_file' on parent_messages is not active yet. Run Sync Schema in Superadmin."
                        } else {
                            "Kolom 'photo_file' pada parent_messages belum aktif di server. Jalankan Sync Skema di Superadmin."
                        }
                    )
                )
            }

            val cdnUrl = DashboardBannerItem.buildR2CdnUrl(collectionId, recordId, uploadedFilename)
            val fallbackUrl = DashboardBannerItem.buildPbApiFileUrl(baseUrl, collectionId, recordId, uploadedFilename)
            val finalized = draftMessage.copy(
                id = safeId,
                schoolId = cleanSchool,
                photoUrl = cdnUrl,
                photoFallbackUrl = fallbackUrl,
                pbRecordId = recordId,
                pbCollectionId = collectionId,
                pbFilename = uploadedFilename,
                isMediaPurgedFromServer = false
            )
            uploadSingleParentMessage(context, finalized)
            Result.success(finalized)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploadParentMessageWithPhoto", e)
            Result.failure(e)
        }
    }

    suspend fun purgeParentMessageServerMedia(
        context: Context,
        message: ParentMessage
    ): Result<ParentMessage> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val purgedLocal = message.copy(
            photoUrl = "",
            photoFallbackUrl = "",
            pbFilename = "",
            isMediaPurgedFromServer = true,
            updatedAt = maxOf(now, message.updatedAt + 1L)
        )
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.success(purgedLocal)
        }
        val baseUrl = PocketBaseConfig.getBaseUrl(context).trim().trimEnd('/')
        if (!baseUrl.startsWith("http")) {
            return@withContext Result.success(purgedLocal)
        }
        val pbId = com.example.data.model.ParentMessageHelper.resolveRecordPbId(message)
        val payload = JSONObject().apply {
            put("photo_file", JSONObject.NULL)
            put("photo_url", "")
            put("photo_fallback_url", "")
            put("pb_filename", "")
            put("is_media_purged_from_server", true)
            put("updated_at", purgedLocal.updatedAt)
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("$baseUrl/api/collections/parent_messages/records/$pbId")
            .patch(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        return@withContext runCatching {
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful && resp.code != 404) {
                    error("HTTP ${resp.code}: ${resp.body?.string().orEmpty()}")
                }
            }
            purgedLocal
        }
    }

    suspend fun deleteRemoteParentMessage(context: Context, messageId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("parent_messages", PocketBaseIdCodec.toPbId(messageId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteParentMessage ke PocketBase", e)
            }
        }

    suspend fun uploadSingleParentExpectation(context: Context, expectation: ParentExpectation) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbParentExpectationDto.fromDomain(expectation)
                upsertSingleDto(service, "parent_expectations", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleParentExpectation ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteParentExpectation(context: Context, expectationId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("parent_expectations", PocketBaseIdCodec.toPbId(expectationId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteParentExpectation ke PocketBase", e)
            }
        }

    suspend fun uploadSingleDeletionLog(context: Context, log: DeletionLog) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbDeletionLogDto.fromDomain(log)
                upsertSingleDto(service, "deletion_logs", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleDeletionLog ke PocketBase", e)
            }
        }

    // --- Tabungan Pribadi Wali Asuh (Transactions & Logs) ---
    suspend fun uploadSingleTabunganTransaction(context: Context, tx: TabunganTransaction) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbTabunganTransactionDto.fromDomain(tx)
                upsertSingleDto(service, "tabungan_transactions", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleTabunganTransaction ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteTabunganTransaction(context: Context, txId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("tabungan_transactions", PocketBaseIdCodec.toPbId(txId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteTabunganTransaction ke PocketBase", e)
            }
        }

    suspend fun uploadSingleTabunganLog(context: Context, log: TabunganLog) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbTabunganLogDto.fromDomain(log)
                upsertSingleDto(service, "tabungan_logs", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleTabunganLog ke PocketBase", e)
            }
        }

    // --- Konseling Rahasia Wali Asuh (Records & Logs) ---
    suspend fun uploadSingleKonselingRecordWithPhotos(
        context: Context,
        rec: KonselingRecord
    ): KonselingRecord = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext rec
        val slots = com.example.util.KonselingPhotoHelper.extractSlots(rec)
        val pendingLocalFiles = slots.mapNotNull { slot ->
            if (slot.localPath.isNotBlank() && slot.remoteUrl.isBlank()) {
                val f = java.io.File(slot.localPath)
                if (f.exists() && f.length() > 0L) slot to f else null
            } else null
        }

        if (pendingLocalFiles.isEmpty()) {
            uploadSingleKonselingRecord(context, rec)
            return@withContext rec
        }

        val baseUrl = PocketBaseConfig.getBaseUrl(context).trim().trimEnd('/')
        if (!baseUrl.startsWith("http")) {
            uploadSingleKonselingRecord(context, rec)
            return@withContext rec
        }

        val safeId = if (rec.id > 0L) rec.id else PocketBaseIdCodec.stablePositiveHash(
            "kons_rec:${rec.schoolId}:${rec.ownerWaliUsername}:${rec.studentId}:${rec.createdAt}"
        )
        val pbId = PocketBaseIdCodec.toPbId(safeId)

        val uploadedRecord = runCatching {
            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .build()

            fun buildMultipart(includeId: Boolean): MultipartBody {
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                if (includeId) builder.addFormDataPart("id", pbId)
                builder
                    .addFormDataPart("local_id", safeId.toString())
                    .addFormDataPart("school_id", rec.schoolId.ifBlank { "SRT1-KDR" })
                    .addFormDataPart("student_id", rec.studentId.toString())
                    .addFormDataPart("student_name", rec.studentName)
                    .addFormDataPart("student_nis", rec.studentNis)
                    .addFormDataPart("student_class", rec.studentClass)
                    .addFormDataPart("student_dorm", rec.studentDorm)
                    .addFormDataPart("owner_wali_username", rec.ownerWaliUsername)
                    .addFormDataPart("owner_wali_name", rec.ownerWaliName)
                    .addFormDataPart("is_confidential", rec.isConfidential.toString())
                    .addFormDataPart("entry_type", rec.entryType)
                    .addFormDataPart("topic", rec.topic)
                    .addFormDataPart("urgency_level", rec.urgencyLevel)
                    .addFormDataPart("mood_level", rec.moodLevel)
                    .addFormDataPart("title", rec.title)
                    .addFormDataPart("summary", rec.summary)
                    .addFormDataPart("intervention", rec.intervention)
                    .addFormDataPart("follow_up_plan", rec.followUpPlan)
                    .addFormDataPart("follow_up_date", (rec.followUpDate ?: 0L).toString())
                    .addFormDataPart("status", rec.status)
                    .addFormDataPart("session_date", rec.sessionDate.toString())
                    .addFormDataPart("photo_urls", rec.photoUrls)
                    .addFormDataPart("created_at", rec.createdAt.toString())
                    .addFormDataPart("updated_at", rec.updatedAt.toString())

                pendingLocalFiles.forEachIndexed { idx, (_, file) ->
                    val fname = "konseling_${safeId}_${System.currentTimeMillis()}_$idx.jpg"
                    builder.addFormDataPart(
                        "photo_files",
                        fname,
                        file.readBytes().toRequestBody("image/jpeg".toMediaType())
                    )
                }
                return builder.build()
            }

            val patchReq = Request.Builder()
                .url("$baseUrl/api/collections/konseling_records/records/$pbId")
                .patch(buildMultipart(includeId = false))
                .build()

            var respCode = 0
            var respBody = ""
            var ok = false
            client.newCall(patchReq).execute().use { resp ->
                respCode = resp.code
                respBody = resp.body?.string().orEmpty()
                ok = resp.isSuccessful
            }

            if (!ok && respCode == 404) {
                val postReq = Request.Builder()
                    .url("$baseUrl/api/collections/konseling_records/records")
                    .post(buildMultipart(includeId = true))
                    .build()
                client.newCall(postReq).execute().use { resp ->
                    respCode = resp.code
                    respBody = resp.body?.string().orEmpty()
                    ok = resp.isSuccessful
                }
            }

            if (!ok || respBody.isBlank()) return@runCatching null
            val json = JSONObject(respBody)
            val recordId = json.optString("id", pbId).trim().ifBlank { pbId }
            val collectionId = json.optString("collectionId", "konseling_records").ifBlank { "konseling_records" }
            val filenames = mutableListOf<String>()
            val arr = json.optJSONArray("photo_files")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val fn = arr.optString(i, "").trim()
                    if (fn.isNotBlank()) filenames.add(fn)
                }
            } else {
                val single = json.optString("photo_files", "").trim()
                if (single.isNotBlank()) filenames.add(single)
            }
            if (filenames.isEmpty()) return@runCatching null

            // Ambil N nama file terbaru yang cocok untuk pending slots
            val newUrls = filenames.takeLast(pendingLocalFiles.size).map { fn ->
                DashboardBannerItem.buildR2CdnUrl(collectionId, recordId, fn)
                    .ifBlank { DashboardBannerItem.buildPbApiFileUrl(baseUrl, collectionId, recordId, fn) }
            }
            var pendingIdx = 0
            val mergedSlots = slots.map { slot ->
                if (slot.localPath.isNotBlank() && slot.remoteUrl.isBlank()) {
                    val url = newUrls.getOrNull(pendingIdx++).orEmpty()
                    slot.copy(remoteUrl = url)
                } else {
                    slot
                }
            }
            val (packedPaths, packedUrls) = com.example.util.KonselingPhotoHelper.packSlots(mergedSlots)
            rec.copy(photoPaths = packedPaths, photoUrls = packedUrls)
        }.getOrNull()

        if (uploadedRecord != null) {
            uploadSingleKonselingRecord(context, uploadedRecord)
            uploadedRecord
        } else {
            uploadSingleKonselingRecord(context, rec)
            rec
        }
    }

    suspend fun uploadSingleKonselingRecord(context: Context, rec: KonselingRecord) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbKonselingRecordDto.fromDomain(rec)
                upsertSingleDto(service, "konseling_records", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleKonselingRecord ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteKonselingRecord(context: Context, recId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("konseling_records", PocketBaseIdCodec.toPbId(recId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteKonselingRecord ke PocketBase", e)
            }
        }

    suspend fun uploadSingleKonselingLog(context: Context, log: KonselingLog) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbKonselingLogDto.fromDomain(log)
                upsertSingleDto(service, "konseling_logs", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleKonselingLog ke PocketBase", e)
            }
        }

    // --- Jadwal Kerja Wali Asuh (Shift Codes & Matriks Bulanan) ---
    suspend fun uploadSingleWaliShiftCode(context: Context, code: WaliShiftCode) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbWaliShiftCodeDto.fromDomain(code)
                upsertSingleDto(service, "wali_shift_codes", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleWaliShiftCode ke PocketBase", e)
            }
        }

    suspend fun uploadWaliShiftCodes(context: Context, codes: List<WaliShiftCode>) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context) || codes.isEmpty()) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val pairs = codes.map { c ->
                    val dto = PbWaliShiftCodeDto.fromDomain(c)
                    dto.id!! to dto
                }
                upsertBatchOrFallback(service, "wali_shift_codes", pairs)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadWaliShiftCodes ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteWaliShiftCode(context: Context, shiftCodeId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("wali_shift_codes", PocketBaseIdCodec.toPbId(shiftCodeId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteWaliShiftCode ke PocketBase", e)
            }
        }

    suspend fun uploadSingleWaliScheduleEntry(context: Context, entry: WaliScheduleEntry) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val dto = PbWaliScheduleEntryDto.fromDomain(entry)
                upsertSingleDto(service, "wali_schedule_entries", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleWaliScheduleEntry ke PocketBase", e)
            }
        }

    suspend fun uploadWaliScheduleEntries(context: Context, entries: List<WaliScheduleEntry>) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context) || entries.isEmpty()) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                val pairs = entries.map { e ->
                    val dto = PbWaliScheduleEntryDto.fromDomain(e)
                    dto.id!! to dto
                }
                upsertBatchOrFallback(service, "wali_schedule_entries", pairs)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadWaliScheduleEntries ke PocketBase", e)
            }
        }

    suspend fun deleteRemoteWaliScheduleEntry(context: Context, entryId: Long) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext
                service.deleteRecord("wali_schedule_entries", PocketBaseIdCodec.toPbId(entryId))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteWaliScheduleEntry ke PocketBase", e)
            }
        }

    /**
     * Menghapus seluruh data satu tenant sekolah dari PocketBase saat akun sekolah dihapus permanen.
     */
    suspend fun deleteAllSchoolData(context: Context, schoolId: String) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context) || schoolId.isBlank()) return@withContext
            val filter = buildSchoolFilter(schoolId)
            val service = PocketBaseClient.getService(context) ?: return@withContext
            runCatching { deleteCollectionByFilter(service, "wali_schedule_entries", filter) }
            runCatching { deleteCollectionByFilter(service, "wali_shift_codes", filter) }
            runCatching { deleteCollectionByFilter(service, "konseling_logs", filter) }
            runCatching { deleteCollectionByFilter(service, "konseling_records", filter) }
            runCatching { deleteCollectionByFilter(service, "tabungan_logs", filter) }
            runCatching { deleteCollectionByFilter(service, "tabungan_transactions", filter) }
            runCatching { deleteCollectionByFilter(service, "deletion_logs", filter) }
            runCatching { deleteCollectionByFilter(service, "parent_expectations", filter) }
            runCatching { deleteCollectionByFilter(service, "parent_messages", filter) }
            runCatching { deleteCollectionByFilter(service, "wali_gallery_posts", filter) }
            runCatching { deleteCollectionByFilter(service, "christian_worship_records", filter) }
            runCatching { deleteCollectionByFilter(service, "bible_bookmarks", filter) }
            runCatching { deleteCollectionByFilter(service, "bible_notes", filter) }
            runCatching { deleteCollectionByFilter(service, "shared_journal_notes", filter) }
            runCatching { deleteCollectionByFilter(service, "parent_access_links", filter) }
            runCatching { deleteCollectionByFilter(service, "fasting_records", filter) }
            runCatching { deleteCollectionByFilter(service, "fasting_sessions", filter) }
            runCatching { deleteCollectionByFilter(service, "visit_session_swaps", filter) }
            runCatching { deleteCollectionByFilter(service, "visit_confirmations", filter) }
            runCatching { deleteCollectionByFilter(service, "visit_records", filter) }
            runCatching { deleteCollectionByFilter(service, "visit_sessions", filter) }
            runCatching { deleteCollectionByFilter(service, "sholat_records", filter) }
            runCatching { deleteCollectionByFilter(service, "mengaji_records", filter) }
            runCatching { deleteCollectionByFilter(service, "medical_records", filter) }
            runCatching { deleteCollectionByFilter(service, "haid_records", filter) }
            runCatching { deleteCollectionByFilter(service, "shift_messages", filter) }
            runCatching { deleteCollectionByFilter(service, "child_students", filter) }
            runCatching { deleteCollectionByFilter(service, "wali_accounts", filter) }
            runCatching { deleteCollectionByFilter(service, "school_profiles", filter) }
        }

    /**
     * Menarik daftar pendaftaran sekolah & akun wali dari PocketBase saat login lintas perangkat.
     */
    suspend fun syncAuthCatalogFromCloud(
        context: Context,
        repository: HealthRepository
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext

        try {
            val remoteRegsResult = fetchAllPages { page ->
                service.listSchoolRegistrations(page = page, perPage = 500)
            }
            val remoteDtos = remoteRegsResult.getOrNull().orEmpty()
            val activeRegs = mutableListOf<SchoolRegistration>()
            for (dto in remoteDtos) {
                val domain = dto.toDomainModel()
                if (PocketBaseConfig.isSchoolRegistrationDeleted(context, domain)) {
                    dto.id?.takeIf { it.isNotBlank() }?.let { remoteId ->
                        runCatching { service.deleteRecord("school_registrations", remoteId) }
                    }
                    if (domain.generatedSchoolCode.isNotBlank()) {
                        runCatching { repository.deleteSchoolRegistrationsByCode(domain.generatedSchoolCode) }
                    }
                    if (domain.id > 0L) {
                        runCatching { repository.deleteSchoolRegistrationById(domain.id) }
                    }
                } else {
                    activeRegs.add(domain)
                }
            }
            if (activeRegs.isNotEmpty()) {
                repository.insertAllSchoolRegistrations(activeRegs)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Silent error sync school_registrations", e)
        }

        try {
            val remoteWaliResult = fetchAllPages { page ->
                service.listWaliAccounts(page = page, perPage = 500)
            }
            val remoteWali = remoteWaliResult.getOrNull()
                ?.map { it.toDomainModel() }
                ?.filterNot { PocketBaseConfig.isSchoolCodeDeleted(context, it.schoolId) }
                .orEmpty()
            if (remoteWali.isNotEmpty()) {
                repository.insertAllWaliAccounts(remoteWali)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Silent error sync wali_accounts", e)
        }

        try {
            val remoteDeletedLinkIds = runCatching {
                fetchAllPages { page ->
                    service.listDeletionLogs(
                        page = page,
                        perPage = 500,
                        filter = "category = '${DeletionLog.CATEGORY_PARENT_ACCESS}'"
                    )
                }.getOrNull()
                    ?.map { it.toDomainModel() }
                    ?.also { logs ->
                        if (logs.isNotEmpty()) repository.insertAllDeletionLogs(logs)
                    }
                    ?.map { it.recordId }
                    ?.filter { it > 0L }
                    ?.toSet()
                    ?: emptySet()
            }.getOrDefault(emptySet())

            val remoteParentResult = fetchAllPages { page ->
                service.listParentAccessLinks(page = page, perPage = 500)
            }
            val remoteParents = remoteParentResult.getOrNull()
                ?.map { it.toDomainModel() }
                ?.filterNot { PocketBaseConfig.isSchoolCodeDeleted(context, it.schoolId) }
                .orEmpty()
            val activeParents = mutableListOf<ParentAccessLink>()
            for (link in remoteParents) {
                if (link.id in remoteDeletedLinkIds) {
                    runCatching { service.deleteRecord("parent_access_links", PocketBaseIdCodec.toPbId(link.id)) }
                } else {
                    activeParents.add(link)
                }
            }
            if (activeParents.isNotEmpty()) {
                repository.insertAllParentAccessLinks(activeParents)
            }
            if (remoteDeletedLinkIds.isNotEmpty()) {
                repository.getAllParentAccessLinksList()
                    .filter { it.id in remoteDeletedLinkIds }
                    .forEach { repository.deleteParentAccessLink(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Silent error sync parent_access_links", e)
        }
    }

    /**
     * Sinkronisasi cepat khusus checklist online (Puasa & Pesan Shift & Rekam Medis Kontrol)
     * agar centang Sahur, Berbuka, Operan Shift, dan Kontrol langsung selaras antar perangkat.
     */
    suspend fun syncFastingOnlineQuietly(
        context: Context,
        schoolId: String,
        repository: HealthRepository
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext
        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        val filter = buildSchoolFilter(cleanSchoolId)

        val mergedLogs = runCatching {
            syncDeletionLogsMerged(service, cleanSchoolId, filter, repository, null)
        }.getOrDefault(repository.getDeletionLogsBySchoolList(cleanSchoolId))
        val deletedPuasaIds = mergedLogs
            .filter { it.category == DeletionLog.CATEGORY_PUASA && it.recordId > 0L }
            .map { it.recordId }
            .toSet()

        runCatching { syncFastingSessionsMerged(service, cleanSchoolId, filter, repository) }
        runCatching { syncFastingRecordsMerged(service, cleanSchoolId, filter, repository, deletedPuasaIds) }
    }

    suspend fun syncSholatOnlineQuietly(
        context: Context,
        schoolId: String,
        repository: HealthRepository
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext
        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        val filter = buildSchoolFilter(cleanSchoolId)

        val mergedLogs = runCatching {
            syncDeletionLogsMerged(service, cleanSchoolId, filter, repository, null)
        }.getOrDefault(repository.getDeletionLogsBySchoolList(cleanSchoolId))
        val deletedSholatIds = mergedLogs
            .filter { it.category == DeletionLog.CATEGORY_SHOLAT && it.recordId > 0L }
            .map { it.recordId }
            .toSet()

        runCatching { syncSholatRecordsMerged(service, cleanSchoolId, filter, repository, deletedSholatIds) }
    }

    suspend fun syncShiftMessagesOnlineQuietly(
        context: Context,
        schoolId: String,
        repository: HealthRepository
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext
        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        val filter = buildSchoolFilter(cleanSchoolId)

        runCatching { syncShiftMessagesMerged(service, cleanSchoolId, filter, repository) }
    }

    suspend fun syncVisitsOnlineQuietly(
        context: Context,
        schoolId: String,
        repository: HealthRepository
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext
        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        val filter = buildSchoolFilter(cleanSchoolId)

        runCatching { syncVisitSessionsMerged(service, cleanSchoolId, filter, repository) }
        runCatching { syncVisitRecordsMerged(service, cleanSchoolId, filter, repository) }
        runCatching { syncVisitConfirmationsMerged(service, cleanSchoolId, filter, repository) }
        runCatching { syncVisitSessionSwapsMerged(service, cleanSchoolId, filter, repository) }
    }

    suspend fun syncParentRelationsOnlineQuietly(
        context: Context,
        schoolId: String,
        repository: HealthRepository
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext
        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        val filter = buildSchoolFilter(cleanSchoolId)

        val mergedDeletionLogs = runCatching {
            syncDeletionLogsMerged(service, cleanSchoolId, filter, repository, null)
        }.getOrDefault(repository.getDeletionLogsBySchoolList(cleanSchoolId))
        val deletedParentMsgIds = mergedDeletionLogs
            .filter { it.category == DeletionLog.CATEGORY_PARENT_MESSAGE && it.recordId > 0L }
            .map { it.recordId }
            .toSet()
        val deletedParentLinkIds = mergedDeletionLogs
            .filter { it.category == DeletionLog.CATEGORY_PARENT_ACCESS && it.recordId > 0L }
            .map { it.recordId }
            .toSet()

        val pbBaseUrl = PocketBaseConfig.getBaseUrl(context)
        runCatching { syncParentAccessLinksMerged(service, cleanSchoolId, filter, repository, deletedParentLinkIds) }
        runCatching { syncWaliShiftCodesMerged(service, cleanSchoolId, filter, repository) }
        runCatching { syncWaliScheduleEntriesMerged(service, cleanSchoolId, filter, repository) }
        runCatching { syncParentMessagesMerged(service, cleanSchoolId, filter, repository, deletedParentMsgIds, pbBaseUrl) }
        runCatching { syncParentExpectationsMerged(service, cleanSchoolId, filter, repository) }
        runCatching {
            syncWaliGalleryMerged(
                service = service,
                schoolId = cleanSchoolId,
                filter = filter,
                repository = repository,
                pbBaseUrl = pbBaseUrl
            )
        }
        val now = System.currentTimeMillis()
        val latestMsgs = runCatching { repository.getParentMessagesBySchoolList(cleanSchoolId) }.getOrDefault(emptyList())
        for (msg in latestMsgs) {
            if (com.example.data.model.ParentMessageHelper.shouldAutoPurgeServerMedia(msg, now)) {
                purgeParentMessageServerMedia(context, msg).getOrNull()?.let { purged ->
                    repository.updateParentMessage(purged)
                }
            }
        }
    }

    suspend fun syncTabunganOnlineQuietly(
        context: Context,
        schoolId: String,
        repository: HealthRepository
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext
        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        val filter = buildSchoolFilter(cleanSchoolId)

        val logs = runCatching {
            syncTabunganLogsMerged(service, cleanSchoolId, filter, repository)
        }.getOrDefault(repository.getTabunganLogsBySchoolList(cleanSchoolId))
        val deletedTxIds = logs
            .filter { it.actionType.equals(TabunganHelper.LOG_DELETE, ignoreCase = true) && it.transactionId > 0L }
            .map { it.transactionId }
            .toSet()
        runCatching { syncTabunganTransactionsMerged(service, cleanSchoolId, filter, repository, deletedTxIds) }
    }

    private suspend fun syncTabunganLogsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository
    ): List<TabunganLog> {
        val localLogs = repository.getTabunganLogsBySchoolList(cleanSchoolId)
        val remoteRes = fetchAllPages { page ->
            service.listTabunganLogs(page = page, perPage = 500, filter = filter)
        }
        val remoteLogs = remoteRes.getOrNull()?.map { it.toDomainModel(cleanSchoolId) } ?: return localLogs

        val localById = localLogs.associateBy { it.id }
        val remoteById = remoteLogs.associateBy { it.id }

        val toInsertLocal = remoteLogs.filter { localById[it.id] == null }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllTabunganLogs(toInsertLocal)
        }

        val toPushRemote = localLogs.filter { remoteById[it.id] == null }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { log ->
                val dto = PbTabunganLogDto.fromDomain(log)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "tabungan_logs", pairs)
        }
        return repository.getTabunganLogsBySchoolList(cleanSchoolId)
    }

    private suspend fun syncTabunganTransactionsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository,
        deletedTxIds: Set<Long> = emptySet()
    ) {
        val localAll = repository.getTabunganTransactionsBySchoolList(cleanSchoolId)
        if (deletedTxIds.isNotEmpty()) {
            localAll.filter { it.id in deletedTxIds }.forEach {
                repository.deleteTabunganTransaction(it)
            }
        }
        val localTxs = localAll.filter { it.id !in deletedTxIds }

        val remoteRes = fetchAllPages { page ->
            service.listTabunganTransactions(page = page, perPage = 500, filter = filter)
        }
        val remoteTxs = remoteRes.getOrNull()
            ?.map { it.toDomainModel(cleanSchoolId) }
            ?.filter { it.id !in deletedTxIds }
            ?: return

        val localById = localTxs.associateBy { it.id }
        val remoteById = remoteTxs.associateBy { it.id }

        val toInsertLocal = remoteTxs.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllTabunganTransactions(toInsertLocal)
        }

        val toPushRemote = localTxs.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { tx ->
                val dto = PbTabunganTransactionDto.fromDomain(tx)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "tabungan_transactions", pairs)
        }
    }

    suspend fun syncKonselingOnlineQuietly(
        context: Context,
        schoolId: String,
        repository: HealthRepository
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext
        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        val filter = buildSchoolFilter(cleanSchoolId)

        val logs = runCatching {
            syncKonselingLogsMerged(service, cleanSchoolId, filter, repository)
        }.getOrDefault(repository.getKonselingLogsBySchoolList(cleanSchoolId))
        val deletedRecordIds = logs
            .filter { it.actionType.equals(KonselingHelper.LOG_DELETE, ignoreCase = true) && it.recordId > 0L }
            .map { it.recordId }
            .toSet()
        runCatching { syncKonselingRecordsMerged(service, cleanSchoolId, filter, repository, deletedRecordIds) }
    }

    private suspend fun syncKonselingLogsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository
    ): List<KonselingLog> {
        val localLogs = repository.getKonselingLogsBySchoolList(cleanSchoolId)
        val remoteRes = fetchAllPages { page ->
            service.listKonselingLogs(page = page, perPage = 500, filter = filter)
        }
        val remoteLogs = remoteRes.getOrNull()?.map { it.toDomainModel(cleanSchoolId) } ?: return localLogs

        val localById = localLogs.associateBy { it.id }
        val remoteById = remoteLogs.associateBy { it.id }

        val toInsertLocal = remoteLogs.filter { localById[it.id] == null }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllKonselingLogs(toInsertLocal)
        }

        val toPushRemote = localLogs.filter { remoteById[it.id] == null }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { log ->
                val dto = PbKonselingLogDto.fromDomain(log)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "konseling_logs", pairs)
        }
        return repository.getKonselingLogsBySchoolList(cleanSchoolId)
    }

    private suspend fun syncKonselingRecordsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository,
        deletedRecordIds: Set<Long> = emptySet()
    ) {
        val localAll = repository.getKonselingRecordsBySchoolList(cleanSchoolId)
        if (deletedRecordIds.isNotEmpty()) {
            localAll.filter { it.id in deletedRecordIds }.forEach {
                repository.deleteKonselingRecord(it)
            }
        }
        val localRecs = localAll.filter { it.id !in deletedRecordIds }

        val remoteRes = fetchAllPages { page ->
            service.listKonselingRecords(page = page, perPage = 500, filter = filter)
        }
        val remoteRecs = remoteRes.getOrNull()
            ?.map { it.toDomainModel(cleanSchoolId) }
            ?.filter { it.id !in deletedRecordIds }
            ?: return

        val localById = localRecs.associateBy { it.id }
        val remoteById = remoteRecs.associateBy { it.id }

        val toInsertLocal = remoteRecs.mapNotNull { rem ->
            val loc = localById[rem.id]
            if (loc == null || rem.updatedAt >= loc.updatedAt) {
                if (loc != null && loc.photoPaths.isNotBlank()) {
                    rem.copy(photoPaths = loc.photoPaths)
                } else {
                    rem
                }
            } else null
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllKonselingRecords(toInsertLocal)
        }

        val toPushRemote = localRecs.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { rec ->
                val dto = PbKonselingRecordDto.fromDomain(rec)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "konseling_records", pairs)
        }
    }

    private suspend fun syncWaliShiftCodesMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository
    ) {
        val localCodes = repository.getWaliShiftCodesBySchoolList(cleanSchoolId)
        val remoteRes = fetchAllPages { page ->
            service.listWaliShiftCodes(page = page, perPage = 500, filter = filter)
        }
        val remoteCodes = remoteRes.getOrNull()?.map { it.toDomainModel(cleanSchoolId) } ?: return

        val localById = localCodes.associateBy { it.id }
        val remoteById = remoteCodes.associateBy { it.id }

        val toInsertLocal = remoteCodes.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllWaliShiftCodes(toInsertLocal)
        }

        val toPushRemote = localCodes.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { c ->
                val dto = PbWaliShiftCodeDto.fromDomain(c)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "wali_shift_codes", pairs)
        }
    }

    private suspend fun syncWaliScheduleEntriesMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository
    ) {
        val localEntries = repository.getWaliScheduleEntriesBySchoolList(cleanSchoolId)
        val remoteRes = fetchAllPages { page ->
            service.listWaliScheduleEntries(page = page, perPage = 500, filter = filter)
        }
        val remoteEntries = remoteRes.getOrNull()?.map { it.toDomainModel(cleanSchoolId) } ?: return

        val localById = localEntries.associateBy { it.id }
        val remoteById = remoteEntries.associateBy { it.id }

        val toInsertLocal = remoteEntries.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllWaliScheduleEntries(toInsertLocal)
        }

        val toPushRemote = localEntries.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { e ->
                val dto = PbWaliScheduleEntryDto.fromDomain(e)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "wali_schedule_entries", pairs)
        }
    }

    private suspend fun syncParentAccessLinksMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository,
        deletedLinkIds: Set<Long> = emptySet()
    ) {
        val localAll = repository.getParentAccessLinksBySchoolList(cleanSchoolId)
        if (deletedLinkIds.isNotEmpty()) {
            localAll.filter { it.id in deletedLinkIds }.forEach { link ->
                repository.deleteParentAccessLink(link)
            }
        }
        val localLinks = localAll.filter { it.id !in deletedLinkIds }
        val remoteRes = fetchAllPages { page ->
            service.listParentAccessLinks(page = page, perPage = 500, filter = filter)
        }
        val remoteAll = remoteRes.getOrNull()?.map { it.toDomainModel(cleanSchoolId) } ?: return
        if (deletedLinkIds.isNotEmpty()) {
            remoteAll.filter { it.id in deletedLinkIds }.forEach { stale ->
                runCatching { service.deleteRecord("parent_access_links", PocketBaseIdCodec.toPbId(stale.id)) }
            }
        }
        val remoteLinks = remoteAll.filter { it.id !in deletedLinkIds }

        val localById = localLinks.associateBy { it.id }
        val remoteById = remoteLinks.associateBy { it.id }

        val toInsertLocal = remoteLinks.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllParentAccessLinks(toInsertLocal)
        }

        val toPushRemote = localLinks.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { link ->
                val dto = PbParentAccessLinkDto.fromDomain(link)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "parent_access_links", pairs)
        }
    }

    private suspend fun syncParentMessagesMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository,
        deletedMessageIds: Set<Long> = emptySet(),
        pbBaseUrl: String = DashboardBannerItem.DEFAULT_POCKETBASE_BASE_URL
    ) {
        val localMsgs = repository.getParentMessagesBySchoolList(cleanSchoolId)
        val localDeleted = localMsgs.filter { it.id in deletedMessageIds }
        localDeleted.forEach { msg ->
            repository.deleteParentMessageById(msg.id)
            repository.deleteChatMediaLocalSave(msg.id)
        }
        val activeLocalMsgs = localMsgs.filter { it.id !in deletedMessageIds }

        val remoteRes = fetchAllPages { page ->
            service.listParentMessages(page = page, perPage = 500, filter = filter)
        }
        val remoteMsgs = remoteRes.getOrNull()?.map { it.toDomainModel(cleanSchoolId, pbBaseUrl) } ?: return
        val activeRemoteMsgs = remoteMsgs.filter { it.id !in deletedMessageIds }

        val localById = activeLocalMsgs.associateBy { it.id }
        val remoteById = activeRemoteMsgs.associateBy { it.id }

        val mergedReadRemote = activeRemoteMsgs.map { rem ->
            val loc = localById[rem.id]
            if (loc != null) {
                val purged = rem.isMediaPurgedFromServer || loc.isMediaPurgedFromServer
                val downloaded = rem.isDownloadedByRecipient || loc.isDownloadedByRecipient
                val useRemoteUrls = !purged && (rem.photoUrl.isNotBlank() || rem.photoFallbackUrl.isNotBlank())
                val resolvedPhoto = when {
                    purged -> ""
                    useRemoteUrls -> rem.photoUrl
                    else -> loc.photoUrl
                }
                val resolvedFallback = when {
                    purged -> ""
                    useRemoteUrls -> rem.photoFallbackUrl
                    else -> loc.photoFallbackUrl
                }
                val resolvedFilename = when {
                    purged -> ""
                    rem.pbFilename.isNotBlank() -> rem.pbFilename
                    else -> loc.pbFilename
                }
                rem.copy(
                    isReadByWali = rem.isReadByWali || loc.isReadByWali,
                    isReadByParent = rem.isReadByParent || loc.isReadByParent,
                    isDownloadedByRecipient = downloaded,
                    isMediaPurgedFromServer = purged,
                    photoUrl = resolvedPhoto,
                    photoFallbackUrl = resolvedFallback,
                    pbRecordId = rem.pbRecordId.ifBlank { loc.pbRecordId },
                    pbCollectionId = rem.pbCollectionId.ifBlank { loc.pbCollectionId },
                    pbFilename = resolvedFilename,
                    updatedAt = maxOf(rem.updatedAt, loc.updatedAt)
                )
            } else rem
        }
        val toInsertLocal = mergedReadRemote.filter { rem ->
            val loc = localById[rem.id]
            loc == null || loc != rem
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllParentMessages(toInsertLocal)
        }

        val toPushRemote = activeLocalMsgs.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null ||
                (loc.isReadByWali && !rem.isReadByWali) ||
                (loc.isReadByParent && !rem.isReadByParent) ||
                (loc.isDownloadedByRecipient && !rem.isDownloadedByRecipient) ||
                (loc.isMediaPurgedFromServer && !rem.isMediaPurgedFromServer) ||
                loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { msg ->
                val dto = PbParentMessageDto.fromDomain(msg)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "parent_messages", pairs)
        }
    }

    private suspend fun syncParentExpectationsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository
    ) {
        val localExpectations = repository.getParentExpectationsBySchoolList(cleanSchoolId)
        val remoteRes = fetchAllPages { page ->
            service.listParentExpectations(page = page, perPage = 500, filter = filter)
        }
        val remoteExpectations = remoteRes.getOrNull()?.map { it.toDomainModel(cleanSchoolId) } ?: return

        val localById = localExpectations.associateBy { it.id }
        val remoteById = remoteExpectations.associateBy { it.id }

        val mergedForLocal = remoteExpectations.map { rem ->
            val loc = localById[rem.id]
            if (loc == null) {
                rem
            } else if (rem.updatedAt >= loc.updatedAt) {
                rem.copy(
                    isReadByWali = rem.isReadByWali || loc.isReadByWali,
                    waliFollowUpNote = rem.waliFollowUpNote.ifBlank { loc.waliFollowUpNote },
                    waliRespondedAt = maxOf(rem.waliRespondedAt, loc.waliRespondedAt)
                )
            } else {
                loc.copy(
                    isReadByWali = loc.isReadByWali || rem.isReadByWali,
                    waliFollowUpNote = loc.waliFollowUpNote.ifBlank { rem.waliFollowUpNote },
                    waliRespondedAt = maxOf(loc.waliRespondedAt, rem.waliRespondedAt)
                )
            }
        }
        val toInsertLocal = mergedForLocal.filter { merged ->
            val loc = localById[merged.id]
            loc == null || loc != merged
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllParentExpectations(toInsertLocal)
        }

        val toPushRemote = localExpectations.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null ||
                loc.updatedAt > rem.updatedAt ||
                (loc.isReadByWali && !rem.isReadByWali) ||
                (loc.waliFollowUpNote.isNotBlank() && rem.waliFollowUpNote.isBlank())
        }.map { loc ->
            val rem = remoteById[loc.id]
            if (rem != null && loc.updatedAt < rem.updatedAt) {
                rem.copy(
                    isReadByWali = rem.isReadByWali || loc.isReadByWali,
                    waliFollowUpNote = rem.waliFollowUpNote.ifBlank { loc.waliFollowUpNote },
                    waliRespondedAt = maxOf(rem.waliRespondedAt, loc.waliRespondedAt)
                )
            } else {
                loc
            }
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { exp ->
                val dto = PbParentExpectationDto.fromDomain(exp)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "parent_expectations", pairs)
        }
    }

    private suspend fun syncVisitSessionsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository
    ) {
        val localSessions = repository.getVisitSessionsBySchoolList(cleanSchoolId)
        val remoteRes = fetchAllPages { page ->
            service.listVisitSessions(page = page, perPage = 500, filter = filter)
        }
        val remoteSessions = remoteRes.getOrNull()?.map { it.toDomainModel(cleanSchoolId) } ?: return

        val localById = localSessions.associateBy { it.id }
        val remoteById = remoteSessions.associateBy { it.id }

        val toInsertLocal = remoteSessions.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllVisitSessions(toInsertLocal)
        }

        val toPushRemote = localSessions.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { s ->
                val dto = PbVisitSessionDto.fromDomain(s)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "visit_sessions", pairs)
        }
    }

    private suspend fun syncVisitRecordsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository
    ) {
        val localRecords = repository.getVisitRecordsBySchoolList(cleanSchoolId)
        val remoteRes = fetchAllPages { page ->
            service.listVisitRecords(page = page, perPage = 500, filter = filter)
        }
        val remoteDtos = remoteRes.getOrNull() ?: return
        val remoteRecords = remoteDtos.map { it.toDomainModel(cleanSchoolId) }

        val localById = localRecords.associateBy { it.id }
        val remoteById = remoteRecords.associateBy { it.id }

        val toInsertLocal = remoteRecords.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllVisitRecords(toInsertLocal)
        }

        val toPushRemote = localRecords.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { rec ->
                val dto = PbVisitRecordDto.fromDomain(rec)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "visit_records", pairs)
        }
    }

    private suspend fun syncVisitConfirmationsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository
    ) {
        val localConfirmations = repository.getVisitConfirmationsBySchoolList(cleanSchoolId)
        val remoteRes = fetchAllPages { page ->
            service.listVisitConfirmations(page = page, perPage = 500, filter = filter)
        }
        val remoteDtos = remoteRes.getOrNull() ?: return
        val remoteConfirmations = remoteDtos.map { it.toDomainModel(cleanSchoolId) }

        val localById = localConfirmations.associateBy { it.id }
        val remoteById = remoteConfirmations.associateBy { it.id }

        val toInsertLocal = remoteConfirmations.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllVisitConfirmations(toInsertLocal)
        }

        val toPushRemote = localConfirmations.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { conf ->
                val dto = PbVisitConfirmationDto.fromDomain(conf)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "visit_confirmations", pairs)
        }
    }

    private suspend fun syncVisitSessionSwapsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository
    ) {
        val localSwaps = repository.getVisitSessionSwapsBySchoolList(cleanSchoolId)
        val remoteRes = fetchAllPages { page ->
            service.listVisitSessionSwaps(page = page, perPage = 500, filter = filter)
        }
        val remoteDtos = remoteRes.getOrNull() ?: return
        val remoteSwaps = remoteDtos.map { it.toDomainModel(cleanSchoolId) }

        val localById = localSwaps.associateBy { it.id }
        val remoteById = remoteSwaps.associateBy { it.id }

        val toInsertLocal = remoteSwaps.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllVisitSessionSwaps(toInsertLocal)
        }

        val toPushRemote = localSwaps.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { swap ->
                val dto = PbVisitSessionSwapDto.fromDomain(swap)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "visit_session_swaps", pairs)
        }
    }

    private suspend fun syncFastingSessionsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository
    ) {
        val localSessions = repository.getFastingSessionsBySchoolList(cleanSchoolId)
        val remoteRes = fetchAllPages { page ->
            service.listFastingSessions(page = page, perPage = 500, filter = filter)
        }
        val remoteSessions = remoteRes.getOrNull()?.map { it.toDomainModel(cleanSchoolId) } ?: return

        val localById = localSessions.associateBy { it.id }
        val remoteById = remoteSessions.associateBy { it.id }

        val toInsertLocal = remoteSessions.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllFastingSessions(toInsertLocal)
        }

        val toPushRemote = localSessions.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { s ->
                val dto = PbFastingSessionDto.fromDomain(s)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "fasting_sessions", pairs)
        }
    }

    private suspend fun syncDeletionLogsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository,
        onNewRemoteDeletionLogs: ((List<DeletionLog>, String) -> Unit)? = null
    ): List<DeletionLog> {
        val localLogs = repository.getDeletionLogsBySchoolList(cleanSchoolId)
        val remoteRes = fetchAllPages { page ->
            service.listDeletionLogs(page = page, perPage = 500, filter = filter)
        }
        val remoteLogs = remoteRes.getOrNull()?.map { it.toDomainModel(cleanSchoolId) }
            ?: return localLogs

        val localById = localLogs.associateBy { it.id }
        val remoteById = remoteLogs.associateBy { it.id }

        val toInsertLocal = remoteLogs.filter { localById[it.id] == null }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllDeletionLogs(toInsertLocal)
            onNewRemoteDeletionLogs?.invoke(toInsertLocal, cleanSchoolId)
        }

        val toPushRemote = localLogs.filter { remoteById[it.id] == null }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { log ->
                val dto = PbDeletionLogDto.fromDomain(log)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "deletion_logs", pairs)
        }

        return repository.getDeletionLogsBySchoolList(cleanSchoolId)
    }

    private suspend fun syncFastingRecordsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository,
        deletedRecordIds: Set<Long> = emptySet()
    ) {
        val localRecordsAll = repository.getFastingRecordsBySchoolList(cleanSchoolId)
        if (deletedRecordIds.isNotEmpty()) {
            localRecordsAll.filter { it.id in deletedRecordIds }.forEach {
                repository.deleteFastingRecord(it)
            }
        }
        val localRecords = localRecordsAll.filter { it.id !in deletedRecordIds }

        val remoteRes = fetchAllPages { page ->
            service.listFastingRecords(page = page, perPage = 500, filter = filter)
        }
        val remoteDtos = remoteRes.getOrNull() ?: return
        val remoteRecords = remoteDtos
            .map { it.toDomainModel(cleanSchoolId) }
            .filter { it.id !in deletedRecordIds }

        val localById = localRecords.associateBy { it.id }
        val remoteById = remoteRecords.associateBy { it.id }
        val dirtyRemoteIds = remoteDtos.mapNotNull { dto ->
            val rawNik = dto.studentNik.orEmpty()
            val cleanedNik = com.example.util.StudentCsvParser.cleanDigitsOnly(rawNik)
            val resolvedId = PocketBaseIdCodec.resolveLocalId(dto.localId, dto.id)
            if (rawNik != cleanedNik && resolvedId !in deletedRecordIds) resolvedId else null
        }.toSet()

        val toInsertLocal = remoteRecords.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllFastingRecords(toInsertLocal)
        }

        val toPushRemote = remoteRecords.filter { it.id in dirtyRemoteIds } + localRecords.filter { loc ->
            if (loc.id in dirtyRemoteIds) return@filter false
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { rec ->
                val dto = PbFastingRecordDto.fromDomain(rec)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "fasting_records", pairs)
        }
    }

    private suspend fun syncMengajiRecordsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository,
        deletedRecordIds: Set<Long> = emptySet()
    ) {
        val localRecordsAll = repository.getMengajiRecordsBySchoolList(cleanSchoolId)
        if (deletedRecordIds.isNotEmpty()) {
            localRecordsAll.filter { it.id in deletedRecordIds }.forEach {
                repository.deleteMengajiRecord(it)
            }
        }
        val localRecords = localRecordsAll.filter { it.id !in deletedRecordIds }

        val remoteRes = fetchAllPages { page ->
            service.listMengajiRecords(page = page, perPage = 500, filter = filter)
        }
        val remoteDtos = remoteRes.getOrNull() ?: return
        val remoteRecords = remoteDtos
            .map { it.toDomainModel(cleanSchoolId) }
            .filter { it.id !in deletedRecordIds }

        val localById = localRecords.associateBy { it.id }
        val remoteById = remoteRecords.associateBy { it.id }
        val dirtyRemoteIds = remoteDtos.mapNotNull { dto ->
            val rawNik = dto.studentNik.orEmpty()
            val cleanedNik = com.example.util.StudentCsvParser.cleanDigitsOnly(rawNik)
            val resolvedId = PocketBaseIdCodec.resolveLocalId(dto.localId, dto.id)
            if (rawNik != cleanedNik && resolvedId !in deletedRecordIds) resolvedId else null
        }.toSet()

        val toInsertLocal = remoteRecords.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllMengajiRecords(toInsertLocal)
        }

        val toPushRemote = remoteRecords.filter { it.id in dirtyRemoteIds } + localRecords.filter { loc ->
            if (loc.id in dirtyRemoteIds) return@filter false
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { rec ->
                val dto = PbMengajiRecordDto.fromDomain(rec)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "mengaji_records", pairs)
        }
    }

    private suspend fun syncSholatRecordsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository,
        deletedRecordIds: Set<Long> = emptySet()
    ) {
        val localRecordsAll = repository.getSholatRecordsBySchoolList(cleanSchoolId)
        if (deletedRecordIds.isNotEmpty()) {
            localRecordsAll.filter { it.id in deletedRecordIds }.forEach {
                repository.deleteSholatRecord(it)
            }
        }
        val localRecords = localRecordsAll.filter { it.id !in deletedRecordIds }

        val remoteRes = fetchAllPages { page ->
            service.listSholatRecords(page = page, perPage = 500, filter = filter)
        }
        val remoteDtos = remoteRes.getOrNull() ?: return
        val remoteRecords = remoteDtos
            .map { it.toDomainModel(cleanSchoolId) }
            .filter { it.id !in deletedRecordIds }

        val localById = localRecords.associateBy { it.id }
        val remoteById = remoteRecords.associateBy { it.id }
        val dirtyRemoteIds = remoteDtos.mapNotNull { dto ->
            val rawNik = dto.studentNik.orEmpty()
            val cleanedNik = com.example.util.StudentCsvParser.cleanDigitsOnly(rawNik)
            val resolvedId = PocketBaseIdCodec.resolveLocalId(dto.localId, dto.id)
            if (rawNik != cleanedNik && resolvedId !in deletedRecordIds) resolvedId else null
        }.toSet()

        val toInsertLocal = remoteRecords.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllSholatRecords(toInsertLocal)
        }

        val toPushRemote = remoteRecords.filter { it.id in dirtyRemoteIds } + localRecords.filter { loc ->
            if (loc.id in dirtyRemoteIds) return@filter false
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { rec ->
                val dto = PbSholatRecordDto.fromDomain(rec)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "sholat_records", pairs)
        }
    }

    private suspend fun syncShiftMessagesMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository
    ) {
        val localShift = repository.getShiftMessagesBySchoolList(cleanSchoolId)
        val remoteRes = fetchAllPages { page ->
            service.listShiftMessages(page = page, perPage = 500, filter = filter)
        }
        val remoteShift = remoteRes.getOrNull()?.map { it.toDomainModel(cleanSchoolId) } ?: return

        fun shiftTimestamp(msg: ShiftMessage): Long = maxOf(msg.completedAt, msg.createdAt)

        val localById = localShift.associateBy { it.id }
        val remoteById = remoteShift.associateBy { it.id }

        val toInsertLocal = remoteShift.filter { rem ->
            val loc = localById[rem.id]
            loc == null || shiftTimestamp(rem) >= shiftTimestamp(loc)
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllShiftMessages(toInsertLocal)
        }

        val toPushRemote = localShift.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || shiftTimestamp(loc) > shiftTimestamp(rem)
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { msg ->
                val dto = PbShiftMessageDto.fromDomain(msg)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "shift_messages", pairs)
        }
    }

    private fun medicalRecordTimestamp(rec: MedicalRecord): Long {
        return maxOf(
            rec.editedAt ?: 0L,
            rec.dischargeDate ?: 0L,
            rec.treatmentDate ?: 0L,
            rec.createdAt
        )
    }

    /**
     * Sinkronisasi penuh seluruh 9 tabel satu sekolah dengan penggabungan timestamp
     * agar centang online (Sahur/Berbuka, Shift, Kontrol, Haid, Rawat Inap) tidak saling menimpa.
     */
    suspend fun syncAllSchoolData(
        context: Context,
        schoolId: String,
        repository: HealthRepository,
        localSchoolProfile: SchoolProfile? = null,
        localProfileUpdatedAt: Long = 0L,
        onRemoteSchoolProfileUpdated: ((SchoolProfile, Long) -> Unit)? = null,
        localDashboardIconOrder: List<DashboardIconKey> = DashboardIconKey.DEFAULT_ORDER,
        localDashboardLayoutUpdatedAt: Long = 0L,
        allowPushDashboardLayout: Boolean = false,
        onRemoteDashboardLayoutUpdated: ((List<DashboardIconKey>, Long) -> Unit)? = null,
        localDashboardBannerConfig: DashboardBannerConfig = DashboardBannerConfig.defaultConfig(),
        allowPushDashboardBanners: Boolean = false,
        onRemoteDashboardBannerUpdated: ((DashboardBannerConfig) -> Unit)? = null,
        onNewRemoteMedicalRecords: ((List<MedicalRecord>, List<ChildStudent>, String) -> Unit)? = null,
        onNewRemoteDeletionLogs: ((List<DeletionLog>, String) -> Unit)? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.failure(
                Exception(if (isEn) "PocketBase server is not configured yet." else "Server PocketBase belum terkonfigurasi.")
            )
        }
        val service = PocketBaseClient.getService(context)
            ?: return@withContext Result.failure(
                Exception(if (isEn) "Failed to create PocketBase service." else "Gagal membuat layanan PocketBase.")
            )

        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        val filter = buildSchoolFilter(cleanSchoolId)

        try {
            // 0. Sinkronkan Profil Sekolah & Faskes (school_profiles)
            runCatching {
                val remoteProfileRes = fetchAllPages { page ->
                    service.listSchoolProfiles(page = page, perPage = 50, filter = filter)
                }
                val remoteProfileDto = remoteProfileRes.getOrNull()?.firstOrNull()
                if (remoteProfileDto != null) {
                    val remoteUpdated = remoteProfileDto.updatedAt ?: 0L
                    if (localSchoolProfile == null || remoteUpdated > localProfileUpdatedAt) {
                        onRemoteSchoolProfileUpdated?.invoke(
                            remoteProfileDto.toDomainModel(cleanSchoolId),
                            remoteUpdated
                        )
                    } else if (localProfileUpdatedAt > remoteUpdated) {
                        val dto = PbSchoolProfileDto.fromDomain(localSchoolProfile, localProfileUpdatedAt)
                        upsertSingleDto(service, "school_profiles", dto.id!!, dto)
                    }
                } else if (localSchoolProfile != null) {
                    val ts = if (localProfileUpdatedAt > 0L) localProfileUpdatedAt else System.currentTimeMillis()
                    val dto = PbSchoolProfileDto.fromDomain(localSchoolProfile, ts)
                    upsertSingleDto(service, "school_profiles", dto.id!!, dto)
                }
            }

            // 0b. Sinkronkan Tata Letak Ikon Dashboard Global (untuk semua Wali Asuh)
            runCatching {
                syncDashboardIconLayoutMerged(
                    service = service,
                    localOrder = localDashboardIconOrder,
                    localUpdatedAt = localDashboardLayoutUpdatedAt,
                    allowPushToCloud = allowPushDashboardLayout,
                    onRemoteLayoutUpdated = onRemoteDashboardLayoutUpdated
                )
            }

            // 0c. Sinkronkan Banner Carousel Global Dashboard (untuk semua Wali Asuh)
            runCatching {
                syncDashboardBannerConfigMerged(
                    service = service,
                    localConfig = localDashboardBannerConfig,
                    allowPushToCloud = allowPushDashboardBanners,
                    onRemoteBannerUpdated = onRemoteDashboardBannerUpdated
                )
            }

            // 1. Sinkronkan Data Siswa (child_students)
            var studentCount = 0
            val localStudents = repository.getStudentsBySchoolList(cleanSchoolId)
            val remoteStudentRes = fetchAllPages { page ->
                service.listStudents(page = page, perPage = 500, filter = filter)
            }
            if (remoteStudentRes.isSuccess) {
                val remoteStudentDtos = remoteStudentRes.getOrNull() ?: emptyList()
                val remoteStudents = remoteStudentDtos.map { it.toDomainModel(cleanSchoolId) }
                if (remoteStudents.isNotEmpty()) {
                    repository.insertAllStudents(remoteStudents)
                    val remoteIds = remoteStudents.map { it.id }.toSet()
                    val staleLocalIds = localStudents.filter { it.id !in remoteIds }.map { it.id }
                    if (staleLocalIds.isNotEmpty()) {
                        repository.deleteStudentsByIds(staleLocalIds)
                    }
                    // Jika ada data siswa di cloud yang NIK/No. KK-nya masih kotor (misal ada mojibake â▯▯),
                    // langsung unggah ulang versi bersihnya ke cloud agar tidak muncul lagi.
                    val healedCloudStudents = remoteStudentDtos.zip(remoteStudents).mapNotNull { (dto, domain) ->
                        val dirtyNik = dto.nik.orEmpty() != domain.nik
                        val dirtyKk = dto.noKk.orEmpty() != domain.noKk
                        if (dirtyNik || dirtyKk) domain else null
                    }
                    if (healedCloudStudents.isNotEmpty()) {
                        val healedPairs = healedCloudStudents.map { s ->
                            val dto = PbStudentDto.fromDomain(s)
                            dto.id!! to dto
                        }
                        upsertBatchOrFallback(service, "child_students", healedPairs)
                    }
                    studentCount = remoteStudents.size
                } else if (localStudents.isNotEmpty()) {
                    val pairs = localStudents.map { s ->
                        val dto = PbStudentDto.fromDomain(s)
                        dto.id!! to dto
                    }
                    upsertBatchOrFallback(service, "child_students", pairs)
                    studentCount = localStudents.size
                }
            }

            val syncedStudents = repository.getStudentsBySchoolList(cleanSchoolId)
            val validStudentIds = syncedStudents.map { it.id }.toSet()
            if (studentCount == 0) studentCount = syncedStudents.size

            // 2. Sinkronkan Akun Wali Asuh (wali_accounts)
            val localWali = repository.getWaliAccountsBySchoolList(cleanSchoolId)
            runCatching {
                val remoteWaliRes = fetchAllPages { page ->
                    service.listWaliAccounts(page = page, perPage = 500, filter = filter)
                }
                if (remoteWaliRes.isSuccess) {
                    val remoteWali = (remoteWaliRes.getOrNull() ?: emptyList())
                        .map { it.toDomainModel(cleanSchoolId) }
                    if (remoteWali.isNotEmpty()) {
                        repository.insertAllWaliAccounts(remoteWali)
                    } else if (localWali.isNotEmpty()) {
                        val pairs = localWali.map { w ->
                            val dto = PbWaliAccountDto.fromDomain(w)
                            dto.id!! to dto
                        }
                        upsertBatchOrFallback(service, "wali_accounts", pairs)
                    }
                }
            }

            // 2b. Sinkronkan Log Audit Hapus Riwayat (deletion_logs) SEBELUM rekam medis/puasa/mengaji
            // agar catatan yang sudah dihapus Wali Asuh lain tidak terunggah ulang (tombstone anti-resurrection)
            val mergedDeletionLogs = runCatching {
                syncDeletionLogsMerged(
                    service = service,
                    cleanSchoolId = cleanSchoolId,
                    filter = filter,
                    repository = repository,
                    onNewRemoteDeletionLogs = onNewRemoteDeletionLogs
                )
            }.getOrDefault(repository.getDeletionLogsBySchoolList(cleanSchoolId))

            val deletedMedicalIds = mergedDeletionLogs
                .filter { it.category == DeletionLog.CATEGORY_BEROBAT && it.recordId > 0L }
                .map { it.recordId }
                .toSet()
            val deletedFastingIds = mergedDeletionLogs
                .filter { it.category == DeletionLog.CATEGORY_PUASA && it.recordId > 0L }
                .map { it.recordId }
                .toSet()
            val deletedMengajiIds = mergedDeletionLogs
                .filter { it.category == DeletionLog.CATEGORY_MENGAJI && it.recordId > 0L }
                .map { it.recordId }
                .toSet()
            val deletedSholatIds = mergedDeletionLogs
                .filter { it.category == DeletionLog.CATEGORY_SHOLAT && it.recordId > 0L }
                .map { it.recordId }
                .toSet()

            // 3. Sinkronkan Rekam Medis & Kontrol (medical_records) - Merge by timestamp
            var recordCount = 0
            runCatching {
                val allSchoolLocalRecords = repository.getAllRecordsList().filter { it.studentId in validStudentIds }
                if (deletedMedicalIds.isNotEmpty()) {
                    allSchoolLocalRecords.filter { it.id in deletedMedicalIds }.forEach {
                        repository.deleteRecord(it)
                    }
                }
                val localRecords = allSchoolLocalRecords.filter { it.id !in deletedMedicalIds }
                val remoteRecRes = fetchAllPages { page ->
                    service.listMedicalRecords(page = page, perPage = 500, filter = filter)
                }
                if (remoteRecRes.isSuccess) {
                    val remoteRecords = (remoteRecRes.getOrNull() ?: emptyList())
                        .map { it.toDomainModel() }
                        .filter { it.studentId in validStudentIds && it.id !in deletedMedicalIds }

                    val localById = localRecords.associateBy { it.id }
                    val remoteById = remoteRecords.associateBy { it.id }

                    val toInsertLocal = remoteRecords.filter { rem ->
                        val loc = localById[rem.id]
                        loc == null || medicalRecordTimestamp(rem) >= medicalRecordTimestamp(loc)
                    }
                    val newlyInsertedRemote = toInsertLocal.filter { localById[it.id] == null }
                    if (toInsertLocal.isNotEmpty()) {
                        repository.insertAllRecords(toInsertLocal)
                    }
                    if (newlyInsertedRemote.isNotEmpty()) {
                        onNewRemoteMedicalRecords?.invoke(newlyInsertedRemote, syncedStudents, cleanSchoolId)
                    }

                    val toPushRemote = localRecords.filter { loc ->
                        val rem = remoteById[loc.id]
                        rem == null || medicalRecordTimestamp(loc) > medicalRecordTimestamp(rem)
                    }
                    if (toPushRemote.isNotEmpty()) {
                        val pairs = toPushRemote.map { rec ->
                            val dto = PbMedicalRecordDto.fromDomain(rec, cleanSchoolId)
                            dto.id!! to dto
                        }
                        upsertBatchOrFallback(service, "medical_records", pairs)
                    }
                    recordCount = maxOf(remoteRecords.size, localRecords.size)
                }
            }

            // 4. Sinkronkan Pendataan Haid (haid_records) - Merge by updatedAt
            var haidCount = 0
            runCatching {
                val localHaid = repository.getHaidRecordsBySchoolList(cleanSchoolId)
                val remoteHaidRes = fetchAllPages { page ->
                    service.listHaidRecords(page = page, perPage = 500, filter = filter)
                }
                if (remoteHaidRes.isSuccess) {
                    val remoteHaidDtos = remoteHaidRes.getOrNull() ?: emptyList()
                    val remoteHaid = remoteHaidDtos.map { it.toDomainModel(cleanSchoolId) }

                    val localById = localHaid.associateBy { it.id }
                    val remoteById = remoteHaid.associateBy { it.id }
                    val dirtyRemoteHaidIds = remoteHaidDtos.mapNotNull { dto ->
                        val rawNik = dto.studentNik.orEmpty()
                        val cleanedNik = com.example.util.StudentCsvParser.cleanDigitsOnly(rawNik)
                        if (rawNik != cleanedNik) PocketBaseIdCodec.resolveLocalId(dto.localId, dto.id) else null
                    }.toSet()

                    val toInsertLocal = remoteHaid.filter { rem ->
                        val loc = localById[rem.id]
                        loc == null || rem.updatedAt >= loc.updatedAt
                    }
                    if (toInsertLocal.isNotEmpty()) {
                        repository.insertAllHaidRecords(toInsertLocal)
                    }

                    val toPushRemote = remoteHaid.filter { it.id in dirtyRemoteHaidIds } + localHaid.filter { loc ->
                        if (loc.id in dirtyRemoteHaidIds) return@filter false
                        val rem = remoteById[loc.id]
                        rem == null || loc.updatedAt > rem.updatedAt
                    }
                    if (toPushRemote.isNotEmpty()) {
                        val pairs = toPushRemote.map { rec ->
                            val dto = PbHaidRecordDto.fromDomain(rec)
                            dto.id!! to dto
                        }
                        upsertBatchOrFallback(service, "haid_records", pairs)
                    }
                    haidCount = maxOf(remoteHaid.size, localHaid.size)
                }
            }

            // 5. Sinkronkan Pesan Shift & Checklist Selesai (shift_messages)
            runCatching {
                syncShiftMessagesMerged(service, cleanSchoolId, filter, repository)
            }

            // 6. Sinkronkan Sesi & Checklist Puasa Sahur/Berbuka (fasting_sessions & fasting_records)
            runCatching {
                syncFastingSessionsMerged(service, cleanSchoolId, filter, repository)
            }
            runCatching {
                syncFastingRecordsMerged(service, cleanSchoolId, filter, repository, deletedFastingIds)
            }

            // 7. Sinkronkan Riwayat Mengaji Berkelanjutan (mengaji_records)
            runCatching {
                syncMengajiRecordsMerged(service, cleanSchoolId, filter, repository, deletedMengajiIds)
            }

            // 7b. Sinkronkan Absen Sholat 5 Waktu (sholat_records)
            runCatching {
                syncSholatRecordsMerged(service, cleanSchoolId, filter, repository, deletedSholatIds)
            }

            // 8. Sinkronkan Sesi, Konfirmasi, Daftar Hadir & Pindah Sesi Kunjungan (visit_sessions, visit_records, visit_confirmations, visit_session_swaps)
            runCatching {
                syncVisitSessionsMerged(service, cleanSchoolId, filter, repository)
            }
            runCatching {
                syncVisitRecordsMerged(service, cleanSchoolId, filter, repository)
            }
            runCatching {
                syncVisitConfirmationsMerged(service, cleanSchoolId, filter, repository)
            }
            runCatching {
                syncVisitSessionSwapsMerged(service, cleanSchoolId, filter, repository)
            }

            // 9. Sinkronkan Kartu Akses, Pesan & Harapan Karakter Orang Tua
            val deletedParentMsgIds = mergedDeletionLogs
                .filter { it.category == DeletionLog.CATEGORY_PARENT_MESSAGE && it.recordId > 0L }
                .map { it.recordId }
                .toSet()
            val deletedParentLinkIds = mergedDeletionLogs
                .filter { it.category == DeletionLog.CATEGORY_PARENT_ACCESS && it.recordId > 0L }
                .map { it.recordId }
                .toSet()
            runCatching {
                syncParentAccessLinksMerged(service, cleanSchoolId, filter, repository, deletedParentLinkIds)
            }
            runCatching {
                syncParentMessagesMerged(
                    service = service,
                    cleanSchoolId = cleanSchoolId,
                    filter = filter,
                    repository = repository,
                    deletedMessageIds = deletedParentMsgIds,
                    pbBaseUrl = PocketBaseConfig.getBaseUrl(context)
                )
            }
            runCatching {
                syncParentExpectationsMerged(service, cleanSchoolId, filter, repository)
            }
            runCatching {
                syncWaliGalleryMerged(
                    service = service,
                    schoolId = cleanSchoolId,
                    filter = filter,
                    repository = repository,
                    pbBaseUrl = PocketBaseConfig.getBaseUrl(context)
                )
            }
            runCatching {
                syncChristianWorshipMerged(
                    service = service,
                    schoolId = cleanSchoolId,
                    filter = filter,
                    repository = repository,
                    pbBaseUrl = PocketBaseConfig.getBaseUrl(context)
                )
            }

            // 10. Sinkronkan Tabungan Pribadi Wali Asuh & Log Audit (tabungan_logs & tabungan_transactions)
            runCatching {
                val mergedTabLogs = syncTabunganLogsMerged(service, cleanSchoolId, filter, repository)
                val deletedTabIds = mergedTabLogs
                    .filter { it.actionType.equals(TabunganHelper.LOG_DELETE, ignoreCase = true) && it.transactionId > 0L }
                    .map { it.transactionId }
                    .toSet()
                syncTabunganTransactionsMerged(service, cleanSchoolId, filter, repository, deletedTabIds)
            }

            // 11. Sinkronkan Konseling Rahasia Wali Asuh & Log Audit (konseling_logs & konseling_records)
            runCatching {
                val mergedKonsLogs = syncKonselingLogsMerged(service, cleanSchoolId, filter, repository)
                val deletedKonsIds = mergedKonsLogs
                    .filter { it.actionType.equals(KonselingHelper.LOG_DELETE, ignoreCase = true) && it.recordId > 0L }
                    .map { it.recordId }
                    .toSet()
                syncKonselingRecordsMerged(service, cleanSchoolId, filter, repository, deletedKonsIds)
            }

            // 12. Sinkronkan Jadwal Kerja Wali Asuh (wali_shift_codes & wali_schedule_entries)
            runCatching {
                syncWaliShiftCodesMerged(service, cleanSchoolId, filter, repository)
                syncWaliScheduleEntriesMerged(service, cleanSchoolId, filter, repository)
            }

            // 13. Sinkronkan Channel Info-Info Penting Global (lintas semua sekolah)
            runCatching {
                syncInfoPentingPostsMerged(service, repository, PocketBaseConfig.getBaseUrl(context))
                syncInfoPentingReactionsMerged(service, repository)
                syncInfoPentingCommentsMerged(service, repository)
            }

            // 14. Sinkronkan Bookmark & Catatan Pribadi Alkitab (bible_bookmarks & bible_notes)
            runCatching {
                syncBibleBookmarksMerged(service, cleanSchoolId, filter, repository)
                syncBibleNotesMerged(service, cleanSchoolId, filter, repository)
            }

            // 15. Sinkronkan Catatan Jurnal & Aturan Bersama (shared_journal_notes)
            runCatching {
                syncSharedJournalNotesMerged(service, cleanSchoolId, filter, repository)
            }

            PocketBaseConfig.setLastSyncTime(context, System.currentTimeMillis())
            Result.success(
                if (isEn) {
                    "PocketBase sync complete! $studentCount students, $recordCount medical records, & $haidCount menstruation logs connected."
                } else {
                    "Sinkronisasi PocketBase selesai! $studentCount siswa, $recordCount rekam medis, & $haidCount data haid terhubung."
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error syncAllSchoolData", e)
            Result.failure(e)
        }
    }

    // --- Channel Info-Info Penting (Global Lintas Semua Sekolah) ---

    suspend fun uploadInfoPentingMediaToCloud(
        context: Context,
        mediaUri: Uri,
        postId: Long,
        title: String,
        preferredMediaType: String
    ): Result<UploadedInfoPentingMedia> = withContext(Dispatchers.IO) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.failure(
                Exception(if (isEn) "PocketBase server is not configured." else "Server PocketBase belum terkonfigurasi.")
            )
        }
        val baseUrl = PocketBaseConfig.getBaseUrl(context).trim().trimEnd('/')
        if (!baseUrl.startsWith("http")) {
            return@withContext Result.failure(
                Exception(if (isEn) "Invalid PocketBase URL." else "URL PocketBase tidak valid.")
            )
        }

        val now = System.currentTimeMillis()
        val mimeHint = context.contentResolver.getType(mediaUri).orEmpty().lowercase()
        val (displayName, queriedSize) = queryUriMeta(context, mediaUri)
        val cleanDisplayName = displayName.orEmpty().trim().ifBlank { "lampiran_$now" }
        val extHint = cleanDisplayName.substringAfterLast('.', "").lowercase()
        val header = peekMagicBytes(context, mediaUri)
        val isGifMagic = header.size >= 4 &&
            header[0] == 'G'.code.toByte() &&
            header[1] == 'I'.code.toByte() &&
            header[2] == 'F'.code.toByte() &&
            header[3] == '8'.code.toByte()
        val isMp4Magic = header.size >= 8 &&
            header[4] == 'f'.code.toByte() &&
            header[5] == 't'.code.toByte() &&
            header[6] == 'y'.code.toByte() &&
            header[7] == 'p'.code.toByte()
        val isWebmMagic = header.size >= 4 &&
            header[0] == 0x1A.toByte() &&
            header[1] == 0x45.toByte() &&
            header[2] == 0xDF.toByte() &&
            header[3] == 0xA3.toByte()

        val inferredType = when {
            preferredMediaType.equals(InfoPentingHelper.MEDIA_VIDEO, ignoreCase = true) ||
                mimeHint.startsWith("video/") ||
                extHint in listOf("mp4", "webm", "mov", "m4v", "mkv", "3gp") ||
                isMp4Magic || isWebmMagic -> InfoPentingHelper.MEDIA_VIDEO
            mimeHint == "image/gif" || extHint == "gif" || isGifMagic -> InfoPentingHelper.MEDIA_GIF
            preferredMediaType.equals(InfoPentingHelper.MEDIA_DOCUMENT, ignoreCase = true) ||
                mimeHint.startsWith("application/") ||
                mimeHint.startsWith("text/") ||
                extHint in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "zip", "rar") ->
                InfoPentingHelper.MEDIA_DOCUMENT
            else -> InfoPentingHelper.MEDIA_IMAGE
        }

        val maxUploadBytes = 50L * 1024L * 1024L
        if (queriedSize > maxUploadBytes) {
            return@withContext Result.failure(
                Exception(
                    if (isEn) "Maximum file size is 50 MB."
                    else "Ukuran file maksimal 50 MB."
                )
            )
        }

        val resolvedFilename: String
        val resolvedMime: String
        val requestBody: okhttp3.RequestBody

        when (inferredType) {
            InfoPentingHelper.MEDIA_VIDEO -> {
                val isWebm = mimeHint.contains("webm") || extHint == "webm" || isWebmMagic
                val isMov = mimeHint.contains("quicktime") || extHint == "mov"
                val ext = when {
                    isWebm -> "webm"
                    isMov -> "mov"
                    else -> "mp4"
                }
                resolvedMime = when {
                    isWebm -> "video/webm"
                    isMov -> "video/quicktime"
                    else -> "video/mp4"
                }
                resolvedFilename = "info_video_${now}.$ext"
                requestBody = buildContentStreamRequestBody(
                    context = context,
                    uri = mediaUri,
                    mediaType = resolvedMime.toMediaType(),
                    knownLength = queriedSize
                )
            }
            InfoPentingHelper.MEDIA_DOCUMENT -> {
                resolvedMime = mimeHint.ifBlank {
                    when (extHint) {
                        "pdf" -> "application/pdf"
                        "doc" -> "application/msword"
                        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        "xls" -> "application/vnd.ms-excel"
                        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        "ppt" -> "application/vnd.ms-powerpoint"
                        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                        "csv" -> "text/csv"
                        "txt" -> "text/plain"
                        "zip" -> "application/zip"
                        else -> "application/octet-stream"
                    }
                }
                val safeExt = extHint.ifBlank { "pdf" }
                resolvedFilename = "info_doc_${now}.$safeExt"
                requestBody = buildContentStreamRequestBody(
                    context = context,
                    uri = mediaUri,
                    mediaType = resolvedMime.toMediaType(),
                    knownLength = queriedSize
                )
            }
            InfoPentingHelper.MEDIA_GIF -> {
                val rawBytes = readUriBytesLimited(context, mediaUri, 15L * 1024L * 1024L)
                    ?: return@withContext Result.failure(
                        Exception(if (isEn) "Unable to read GIF or exceeds 15 MB." else "Gagal membaca GIF atau ukuran melebihi 15 MB.")
                    )
                resolvedMime = "image/gif"
                resolvedFilename = "info_gif_${now}.gif"
                requestBody = rawBytes.toRequestBody(resolvedMime.toMediaType())
            }
            else -> {
                val compressed = compressBannerImageBytes(context, mediaUri)
                    ?: return@withContext Result.failure(
                        Exception(if (isEn) "Unable to read selected image." else "Gagal membaca foto yang dipilih.")
                    )
                resolvedMime = "image/jpeg"
                resolvedFilename = "info_img_${now}.jpg"
                requestBody = compressed.toRequestBody(resolvedMime.toMediaType())
            }
        }

        val pbId = PocketBaseIdCodec.toPbId(postId)
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("id", pbId)
            .addFormDataPart("local_id", postId.toString())
            .addFormDataPart("school_id", InfoPentingHelper.GLOBAL_SCHOOL_ID)
            .addFormDataPart("title", title.ifBlank { "Info Penting" })
            .addFormDataPart("media_type", inferredType)
            .addFormDataPart("media_name", cleanDisplayName)
            .addFormDataPart("media_size_bytes", queriedSize.coerceAtLeast(0L).toString())
            .addFormDataPart("author_display_name", InfoPentingHelper.PUBLIC_AUTHOR_NAME)
            .addFormDataPart("created_at", now.toString())
            .addFormDataPart("updated_at", now.toString())
            .addFormDataPart("media_file", resolvedFilename, requestBody)
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(150, TimeUnit.SECONDS)
            .writeTimeout(150, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("$baseUrl/api/collections/important_info_posts/records")
            .post(multipartBody)
            .build()

        return@withContext try {
            client.newCall(request).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (resp.code == 404) {
                    return@withContext Result.failure(
                        Exception(
                            if (isEn) {
                                "Collection 'important_info_posts' not synced on server yet."
                            } else {
                                "Koleksi 'important_info_posts' belum disinkronkan di server. Jalankan Sync Skema di Pengaturan PocketBase."
                            }
                        )
                    )
                }
                if (!resp.isSuccessful) {
                    val errMsg = parsePocketBaseUploadError(raw, resp.code, isEn)
                    return@withContext Result.failure(
                        Exception(if (isEn) "Failed to upload media: $errMsg" else "Gagal mengunggah media: $errMsg")
                    )
                }
                val json = JSONObject(raw)
                val recordId = json.optString("id", pbId).trim().ifBlank { pbId }
                val collectionId = json.optString("collectionId", "important_info_posts")
                    .ifBlank { "important_info_posts" }
                val uploadedFilename = when {
                    json.has("media_file") && json.optJSONArray("media_file") != null -> {
                        json.optJSONArray("media_file")?.optString(0, "").orEmpty()
                    }
                    else -> json.optString("media_file", "")
                }.trim()
                if (uploadedFilename.isBlank()) {
                    return@withContext Result.failure(
                        Exception(
                            if (isEn) "Server did not return uploaded filename."
                            else "Server tidak mengembalikan nama file terunggah."
                        )
                    )
                }
                val cdnUrl = DashboardBannerItem.buildR2CdnUrl(collectionId, recordId, uploadedFilename)
                val fallbackUrl = DashboardBannerItem.buildPbApiFileUrl(baseUrl, collectionId, recordId, uploadedFilename)
                Result.success(
                    UploadedInfoPentingMedia(
                        pbRecordId = recordId,
                        pbCollectionId = collectionId,
                        pbFilename = uploadedFilename,
                        cdnUrl = cdnUrl,
                        fallbackApiFileUrl = fallbackUrl,
                        mediaType = inferredType,
                        fileName = cleanDisplayName,
                        fileSizeBytes = queriedSize.coerceAtLeast(0L)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploadInfoPentingMediaToCloud", e)
            Result.failure(e)
        }
    }

    suspend fun uploadSingleInfoPentingPost(context: Context, post: InfoPentingPost): Boolean = withContext(Dispatchers.IO) {
        val service = PocketBaseClient.getService(context) ?: return@withContext false
        val dto = PbInfoPentingPostDto.fromDomain(post)
        upsertSingleDto(service, "important_info_posts", dto.id!!, dto)
    }

    suspend fun uploadSingleInfoPentingReaction(context: Context, reaction: InfoPentingReaction): Boolean = withContext(Dispatchers.IO) {
        val service = PocketBaseClient.getService(context) ?: return@withContext false
        val dto = PbInfoPentingReactionDto.fromDomain(reaction)
        upsertSingleDto(service, "important_info_reactions", dto.id!!, dto)
    }

    suspend fun uploadSingleInfoPentingComment(context: Context, comment: InfoPentingComment): Boolean = withContext(Dispatchers.IO) {
        val service = PocketBaseClient.getService(context) ?: return@withContext false
        val dto = PbInfoPentingCommentDto.fromDomain(comment)
        upsertSingleDto(service, "important_info_comments", dto.id!!, dto)
    }

    private suspend fun syncInfoPentingPostsMerged(
        service: PocketBaseService,
        repository: HealthRepository,
        pbBaseUrl: String
    ) {
        val localPosts = repository.getAllInfoPentingPostsIncludingDeletedList()
        val remoteRes = fetchAllPages { page ->
            service.listImportantInfoPosts(page = page, perPage = 500, filter = null)
        }
        val remotePosts = remoteRes.getOrNull()?.map { it.toDomainModel(pbBaseUrl) } ?: return
        val localById = localPosts.associateBy { it.id }
        val remoteById = remotePosts.associateBy { it.id }

        val toInsertLocal = remotePosts.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllInfoPentingPosts(toInsertLocal)
        }

        val toPushRemote = localPosts.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { p ->
                val dto = PbInfoPentingPostDto.fromDomain(p)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "important_info_posts", pairs)
        }
    }

    private suspend fun syncInfoPentingReactionsMerged(
        service: PocketBaseService,
        repository: HealthRepository
    ) {
        val localReactions = repository.getAllInfoPentingReactionsList()
        val remoteRes = fetchAllPages { page ->
            service.listImportantInfoReactions(page = page, perPage = 500, filter = null)
        }
        val remoteReactions = remoteRes.getOrNull()?.map { it.toDomainModel() } ?: return
        val localById = localReactions.associateBy { it.id }
        val remoteById = remoteReactions.associateBy { it.id }

        val toInsertLocal = remoteReactions.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllInfoPentingReactions(toInsertLocal)
        }

        val toPushRemote = localReactions.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { r ->
                val dto = PbInfoPentingReactionDto.fromDomain(r)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "important_info_reactions", pairs)
        }
    }

    private suspend fun syncInfoPentingCommentsMerged(
        service: PocketBaseService,
        repository: HealthRepository
    ) {
        val localComments = repository.getAllInfoPentingCommentsIncludingDeletedList()
        val remoteRes = fetchAllPages { page ->
            service.listImportantInfoComments(page = page, perPage = 500, filter = null)
        }
        val remoteComments = remoteRes.getOrNull()?.map { it.toDomainModel() } ?: return
        val localById = localComments.associateBy { it.id }
        val remoteById = remoteComments.associateBy { it.id }

        val toInsertLocal = remoteComments.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllInfoPentingComments(toInsertLocal)
        }

        val toPushRemote = localComments.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { c ->
                val dto = PbInfoPentingCommentDto.fromDomain(c)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "important_info_comments", pairs)
        }
    }

    suspend fun syncInfoPentingChannelQuietly(
        context: Context,
        repository: HealthRepository
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.success(Unit)
        }
        val service = PocketBaseClient.getService(context)
            ?: return@withContext Result.success(Unit)
        return@withContext runCatching {
            val baseUrl = PocketBaseConfig.getBaseUrl(context)
            syncInfoPentingPostsMerged(service, repository, baseUrl)
            syncInfoPentingReactionsMerged(service, repository)
            syncInfoPentingCommentsMerged(service, repository)
        }
    }

    // --- Upload & Kompresi Foto Profil Siswa (Wali Asuh -> PocketBase / R2 CDN -> Portal Ortu) ---

    data class UploadedStudentPhoto(
        val cdnUrl: String,
        val fallbackUrl: String
    )

    fun compressStudentPhotoBytes(context: Context, imageUri: Uri): ByteArray? {
        return runCatching {
            val orientation = runCatching {
                context.contentResolver.openInputStream(imageUri)?.use { exifStream ->
                    ExifInterface(exifStream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                }
            }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOpts)
            }
            val srcW = boundsOpts.outWidth
            val srcH = boundsOpts.outHeight
            var sampleSize = 1
            if (srcW > 1080 || srcH > 1080) {
                while ((srcW / sampleSize) > 1080 || (srcH / sampleSize) > 1080) {
                    sampleSize *= 2
                }
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = context.contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOpts)
            } ?: return@runCatching null

            val rotated = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    val m = Matrix().apply { postRotate(90f) }
                    Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, m, true).also {
                        if (it !== decoded) decoded.recycle()
                    }
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    val m = Matrix().apply { postRotate(180f) }
                    Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, m, true).also {
                        if (it !== decoded) decoded.recycle()
                    }
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    val m = Matrix().apply { postRotate(270f) }
                    Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, m, true).also {
                        if (it !== decoded) decoded.recycle()
                    }
                }
                else -> decoded
            }

            val maxDim = 640
            val scaled = if (rotated.width > maxDim || rotated.height > maxDim) {
                val ratio = minOf(maxDim.toFloat() / rotated.width, maxDim.toFloat() / rotated.height)
                val targetW = (rotated.width * ratio).toInt().coerceAtLeast(1)
                val targetH = (rotated.height * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(rotated, targetW, targetH, true).also {
                    if (it !== rotated) rotated.recycle()
                }
            } else {
                rotated
            }

            @Suppress("DEPRECATION")
            val webpFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                Bitmap.CompressFormat.WEBP
            }

            var bestBytes: ByteArray? = null
            for (quality in intArrayOf(78, 66, 54, 42)) {
                val attempt = ByteArrayOutputStream().use { out ->
                    val ok = scaled.compress(webpFormat, quality, out)
                    if (ok) out.toByteArray() else null
                }
                if (attempt != null && attempt.isNotEmpty()) {
                    bestBytes = attempt
                    if (attempt.size <= 85 * 1024) break
                }
            }
            scaled.recycle()
            bestBytes?.takeIf { it.isNotEmpty() }
        }.getOrNull()
    }

    fun saveStudentPhotoLocally(context: Context, studentId: Long, webpBytes: ByteArray): String? {
        return runCatching {
            val dir = java.io.File(context.filesDir, "student_photos").apply { mkdirs() }
            dir.listFiles()?.forEach { f ->
                if (f.name.startsWith("student_${studentId}_")) {
                    runCatching { f.delete() }
                }
            }
            val dest = java.io.File(dir, "student_${studentId}_${System.currentTimeMillis()}.webp")
            dest.writeBytes(webpBytes)
            Uri.fromFile(dest).toString()
        }.getOrNull()
    }

    fun clearLocalStudentPhotos(context: Context, studentId: Long) {
        runCatching {
            val dir = java.io.File(context.filesDir, "student_photos")
            if (dir.exists()) {
                dir.listFiles()?.forEach { f ->
                    if (f.name.startsWith("student_${studentId}_")) {
                        f.delete()
                    }
                }
            }
        }
    }

    suspend fun uploadStudentProfilePhotoToCloud(
        context: Context,
        student: ChildStudent,
        imageUri: Uri
    ): Result<UploadedStudentPhoto> = withContext(Dispatchers.IO) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.failure(
                Exception(if (isEn) "PocketBase server is not configured." else "Server PocketBase belum terkonfigurasi.")
            )
        }
        val baseUrl = PocketBaseConfig.getBaseUrl(context).trim().trimEnd('/')
        if (!baseUrl.startsWith("http")) {
            return@withContext Result.failure(
                Exception(if (isEn) "Invalid PocketBase URL." else "URL PocketBase tidak valid.")
            )
        }

        val compressed = compressStudentPhotoBytes(context, imageUri)
            ?: return@withContext Result.failure(
                Exception(if (isEn) "Unable to read selected image." else "Gagal membaca foto yang dipilih.")
            )

        val now = System.currentTimeMillis()
        val safeId = if (student.id > 0L) student.id else PocketBaseIdCodec.stablePositiveHash(
            "${student.schoolId}:${student.nik}:${student.nis}:${student.name}"
        )
        val pbId = PocketBaseIdCodec.toPbId(safeId)
        val filename = "student_${safeId}_${now}.webp"

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()

        val patchBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "photo_file",
                filename,
                compressed.toRequestBody("image/webp".toMediaType())
            )
            .build()

        val patchRequest = Request.Builder()
            .url("$baseUrl/api/collections/child_students/records/$pbId")
            .patch(patchBody)
            .build()

        return@withContext try {
            var responseRaw = ""
            var responseCode = 0
            var wasSuccess = false

            client.newCall(patchRequest).execute().use { resp ->
                responseCode = resp.code
                responseRaw = resp.body?.string().orEmpty()
                wasSuccess = resp.isSuccessful
            }

            if (responseCode == 404) {
                val activeSchool = student.schoolId.ifBlank { "SRT1-KDR" }
                val createBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("id", pbId)
                    .addFormDataPart("local_id", safeId.toString())
                    .addFormDataPart("school_id", activeSchool)
                    .addFormDataPart("name", student.name)
                    .addFormDataPart("nis", student.nis)
                    .addFormDataPart("gender", student.gender)
                    .addFormDataPart("age", student.age.toString())
                    .addFormDataPart("dormitory", student.dormitory)
                    .addFormDataPart("class_grade", student.classGrade)
                    .addFormDataPart("allergies", student.allergies)
                    .addFormDataPart("blood_type", student.bloodType)
                    .addFormDataPart("emergency_contact", student.emergencyContact)
                    .addFormDataPart("special_notes", student.specialNotes)
                    .addFormDataPart("nik", com.example.util.StudentCsvParser.cleanDigitsOnly(student.nik))
                    .addFormDataPart("no_kk", com.example.util.StudentCsvParser.cleanDigitsOnly(student.noKk))
                    .addFormDataPart("birth_place", student.birthPlace)
                    .addFormDataPart("birth_date", student.birthDate)
                    .addFormDataPart("mother_name", student.motherName)
                    .addFormDataPart("address", student.address)
                    .addFormDataPart("district", student.district)
                    .addFormDataPart("room_number", student.roomNumber)
                    .addFormDataPart("jenjang", student.jenjang)
                    .addFormDataPart("parent_phone", com.example.util.StudentCsvParser.cleanPhoneNumber(student.parentPhone))
                    .addFormDataPart("assigned_wali_username", student.assignedWaliUsername)
                    .addFormDataPart("assigned_wali_name", student.assignedWaliName)
                    .addFormDataPart("religion", student.religion)
                    .addFormDataPart("tandem_walis_json", student.tandemWalisJson)
                    .addFormDataPart("tandem_requests_json", student.tandemRequestsJson)
                    .addFormDataPart(
                        "photo_file",
                        filename,
                        compressed.toRequestBody("image/webp".toMediaType())
                    )
                    .build()
                val postRequest = Request.Builder()
                    .url("$baseUrl/api/collections/child_students/records")
                    .post(createBody)
                    .build()
                client.newCall(postRequest).execute().use { postResp ->
                    responseCode = postResp.code
                    responseRaw = postResp.body?.string().orEmpty()
                    wasSuccess = postResp.isSuccessful
                }
            }

            if (!wasSuccess) {
                Log.e(TAG, "Student photo upload HTTP $responseCode: $responseRaw")
                val errMsg = parsePocketBaseUploadError(responseRaw, responseCode, isEn)
                return@withContext Result.failure(
                    Exception(
                        if (isEn) "Failed to upload student photo: $errMsg"
                        else "Gagal mengunggah foto siswa: $errMsg"
                    )
                )
            }

            val json = JSONObject(responseRaw)
            val recordId = json.optString("id", "").trim().ifBlank { pbId }
            val collectionId = json.optString("collectionId", "child_students").ifBlank { "child_students" }
            val uploadedFilename = when {
                json.has("photo_file") && json.optJSONArray("photo_file") != null -> {
                    json.optJSONArray("photo_file")?.optString(0, "").orEmpty()
                }
                else -> json.optString("photo_file", "")
            }.trim()

            if (uploadedFilename.isBlank()) {
                return@withContext Result.failure(
                    Exception(
                        if (isEn) {
                            "Server has not synced 'photo_file' column on child_students yet. Run Sync Schema in Superadmin."
                        } else {
                            "Kolom 'photo_file' pada child_students belum aktif di server. Jalankan Sync Skema di Superadmin terlebih dahulu."
                        }
                    )
                )
            }

            val cdnUrl = DashboardBannerItem.buildR2CdnUrl(collectionId, recordId, uploadedFilename)
            val fallbackUrl = DashboardBannerItem.buildPbApiFileUrl(baseUrl, collectionId, recordId, uploadedFilename)

            val updatedStudent = student.copy(
                id = safeId,
                photoUrl = cdnUrl,
                photoFallbackUrl = fallbackUrl
            )
            uploadSingleStudent(context, updatedStudent)
            clearLocalStudentPhotos(context, safeId)

            Result.success(
                UploadedStudentPhoto(
                    cdnUrl = cdnUrl,
                    fallbackUrl = fallbackUrl
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error uploadStudentProfilePhotoToCloud", e)
            Result.failure(e)
        }
    }

    fun saveWaliPhotoLocally(context: Context, accountId: Long, webpBytes: ByteArray): String? {
        return runCatching {
            val dir = java.io.File(context.filesDir, "wali_photos")
            if (!dir.exists()) dir.mkdirs()
            dir.listFiles()?.forEach { f ->
                if (f.name.startsWith("wali_${accountId}_")) {
                    f.delete()
                }
            }
            val dest = java.io.File(dir, "wali_${accountId}_${System.currentTimeMillis()}.webp")
            dest.writeBytes(webpBytes)
            Uri.fromFile(dest).toString()
        }.getOrNull()
    }

    fun clearLocalWaliPhotos(context: Context, accountId: Long) {
        runCatching {
            val dir = java.io.File(context.filesDir, "wali_photos")
            if (dir.exists()) {
                dir.listFiles()?.forEach { f ->
                    if (f.name.startsWith("wali_${accountId}_")) {
                        f.delete()
                    }
                }
            }
        }
    }

    suspend fun uploadWaliProfilePhotoToCloud(
        context: Context,
        account: WaliAccount,
        imageUri: Uri
    ): Result<UploadedStudentPhoto> = withContext(Dispatchers.IO) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.failure(
                Exception(if (isEn) "PocketBase server is not configured." else "Server PocketBase belum terkonfigurasi.")
            )
        }
        val baseUrl = PocketBaseConfig.getBaseUrl(context).trim().trimEnd('/')
        if (!baseUrl.startsWith("http")) {
            return@withContext Result.failure(
                Exception(if (isEn) "Invalid PocketBase URL." else "URL PocketBase tidak valid.")
            )
        }

        val compressed = compressStudentPhotoBytes(context, imageUri)
            ?: return@withContext Result.failure(
                Exception(if (isEn) "Unable to read selected image." else "Gagal membaca foto yang dipilih.")
            )

        val now = System.currentTimeMillis()
        val safeId = if (account.id > 0L) account.id else PocketBaseIdCodec.stablePositiveHash(
            "${account.schoolId}:${account.username}"
        )
        val pbId = PocketBaseIdCodec.toPbId(safeId)
        val filename = "wali_${safeId}_${now}.webp"

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()

        val patchBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "photo_file",
                filename,
                compressed.toRequestBody("image/webp".toMediaType())
            )
            .build()

        val patchRequest = Request.Builder()
            .url("$baseUrl/api/collections/wali_accounts/records/$pbId")
            .patch(patchBody)
            .build()

        return@withContext try {
            var responseRaw = ""
            var responseCode = 0
            var wasSuccess = false

            client.newCall(patchRequest).execute().use { resp ->
                responseCode = resp.code
                responseRaw = resp.body?.string().orEmpty()
                wasSuccess = resp.isSuccessful
            }

            if (responseCode == 404) {
                val activeSchool = account.schoolId.ifBlank { "SRT1-KDR" }
                val createBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("id", pbId)
                    .addFormDataPart("local_id", safeId.toString())
                    .addFormDataPart("school_id", activeSchool)
                    .addFormDataPart("name", account.name)
                    .addFormDataPart("username", account.username)
                    .addFormDataPart("password", account.password)
                    .addFormDataPart("dormitory", account.dormitory)
                    .addFormDataPart("phone", account.phone)
                    .addFormDataPart("role", account.role.ifBlank { WaliAccount.ROLE_WALI })
                    .addFormDataPart("created_at", account.createdAt.toString())
                    .addFormDataPart("title_position", account.titlePosition)
                    .addFormDataPart("employee_code", account.employeeCode)
                    .addFormDataPart("bio", account.bio)
                    .addFormDataPart("profile_updated_at", now.toString())
                    .addFormDataPart(
                        "photo_file",
                        filename,
                        compressed.toRequestBody("image/webp".toMediaType())
                    )
                    .build()
                val postRequest = Request.Builder()
                    .url("$baseUrl/api/collections/wali_accounts/records")
                    .post(createBody)
                    .build()
                client.newCall(postRequest).execute().use { postResp ->
                    responseCode = postResp.code
                    responseRaw = postResp.body?.string().orEmpty()
                    wasSuccess = postResp.isSuccessful
                }
            }

            if (!wasSuccess) {
                Log.e(TAG, "Wali photo upload HTTP $responseCode: $responseRaw")
                val errMsg = parsePocketBaseUploadError(responseRaw, responseCode, isEn)
                return@withContext Result.failure(
                    Exception(
                        if (isEn) "Failed to upload caregiver photo: $errMsg"
                        else "Gagal mengunggah foto profil Wali Asuh: $errMsg"
                    )
                )
            }

            val json = JSONObject(responseRaw)
            val recordId = json.optString("id", "").trim().ifBlank { pbId }
            val collectionId = json.optString("collectionId", "wali_accounts").ifBlank { "wali_accounts" }
            val uploadedFilename = when {
                json.has("photo_file") && json.optJSONArray("photo_file") != null -> {
                    json.optJSONArray("photo_file")?.optString(0, "").orEmpty()
                }
                else -> json.optString("photo_file", "")
            }.trim()

            if (uploadedFilename.isBlank()) {
                return@withContext Result.failure(
                    Exception(
                        if (isEn) {
                            "Server has not synced 'photo_file' column on wali_accounts yet. Run Sync Schema in Superadmin."
                        } else {
                            "Kolom 'photo_file' pada wali_accounts belum aktif di server. Jalankan Sync Skema di Superadmin terlebih dahulu."
                        }
                    )
                )
            }

            val cdnUrl = DashboardBannerItem.buildR2CdnUrl(collectionId, recordId, uploadedFilename)
            val fallbackUrl = DashboardBannerItem.buildPbApiFileUrl(baseUrl, collectionId, recordId, uploadedFilename)

            val updatedAccount = account.copy(
                id = safeId,
                photoUrl = cdnUrl,
                photoFallbackUrl = fallbackUrl,
                profileUpdatedAt = now
            )
            uploadWaliAccounts(context, listOf(updatedAccount))
            clearLocalWaliPhotos(context, safeId)

            Result.success(
                UploadedStudentPhoto(
                    cdnUrl = cdnUrl,
                    fallbackUrl = fallbackUrl
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error uploadWaliProfilePhotoToCloud", e)
            Result.failure(e)
        }
    }

    // --- Galeri Wali Asuh (Relay PocketBase + Cloudflare R2 Sementara -> Galeri HP Orang Tua) ---

    suspend fun uploadWaliGalleryPostWithPhoto(
        context: Context,
        draftPost: WaliGalleryPost,
        imageUri: Uri
    ): Result<WaliGalleryPost> = withContext(Dispatchers.IO) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.failure(
                Exception(if (isEn) "PocketBase server is not configured." else "Server PocketBase belum terkonfigurasi.")
            )
        }
        val baseUrl = PocketBaseConfig.getBaseUrl(context).trim().trimEnd('/')
        if (!baseUrl.startsWith("http")) {
            return@withContext Result.failure(
                Exception(if (isEn) "Invalid PocketBase URL." else "URL PocketBase tidak valid.")
            )
        }

        val compressed = WaliGalleryMediaHelper.compressGalleryPhotoBytes(context, imageUri)
            ?: return@withContext Result.failure(
                Exception(if (isEn) "Unable to read selected image." else "Gagal membaca foto yang dipilih.")
            )

        val safeId = if (draftPost.id > 0L) draftPost.id else generateCloudSafeId()
        val pbId = PocketBaseIdCodec.toPbId(safeId)
        val filename = "galeri_${safeId}.webp"
        val cleanSchool = draftPost.schoolId.trim().ifBlank { "SRT1-KDR" }

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("id", pbId)
            .addFormDataPart("local_id", safeId.toString())
            .addFormDataPart("school_id", cleanSchool)
            .addFormDataPart("wali_username", draftPost.waliUsername)
            .addFormDataPart("wali_name", draftPost.waliName)
            .addFormDataPart("caption", draftPost.caption)
            .addFormDataPart("activity_tag", draftPost.activityTag)
            .addFormDataPart("target_scope", draftPost.targetScope)
            .addFormDataPart("target_student_ids_csv", draftPost.targetStudentIdsCsv)
            .addFormDataPart("retention_days", draftPost.retentionDays.toString())
            .addFormDataPart("expires_at", draftPost.expiresAt.toString())
            .addFormDataPart("expected_recipient_count", draftPost.expectedRecipientCount.toString())
            .addFormDataPart("downloaded_by_csv", draftPost.downloadedByCsv)
            .addFormDataPart("is_media_purged_from_server", "false")
            .addFormDataPart("is_deleted", "false")
            .addFormDataPart("created_at", draftPost.createdAt.toString())
            .addFormDataPart("updated_at", draftPost.updatedAt.toString())
            .addFormDataPart("photo_file", filename, compressed.toRequestBody("image/webp".toMediaType()))
            .build()

        val postRequest = Request.Builder()
            .url("$baseUrl/api/collections/wali_gallery_posts/records")
            .post(multipart)
            .build()

        return@withContext try {
            var responseCode = 0
            var responseRaw = ""
            var wasSuccess = false

            client.newCall(postRequest).execute().use { resp ->
                responseCode = resp.code
                responseRaw = resp.body?.string().orEmpty()
                wasSuccess = resp.isSuccessful
            }

            if (responseCode == 404) {
                return@withContext Result.failure(
                    Exception(
                        if (isEn) {
                            "Collection 'wali_gallery_posts' is not synced on server yet. Run Sync Schema in Superadmin."
                        } else {
                            "Koleksi 'wali_gallery_posts' belum disinkronkan di server. Jalankan Sync Skema di Superadmin terlebih dahulu."
                        }
                    )
                )
            }

            if (!wasSuccess) {
                val errMsg = parsePocketBaseUploadError(responseRaw, responseCode, isEn)
                return@withContext Result.failure(
                    Exception(if (isEn) "Failed to upload photo: $errMsg" else "Gagal mengunggah foto ke R2/PocketBase: $errMsg")
                )
            }

            val json = JSONObject(responseRaw)
            val recordId = json.optString("id", pbId).trim().ifBlank { pbId }
            val collectionId = json.optString("collectionId", "wali_gallery_posts").ifBlank { "wali_gallery_posts" }
            val uploadedFilename = when {
                json.has("photo_file") && json.optJSONArray("photo_file") != null -> {
                    json.optJSONArray("photo_file")?.optString(0, "").orEmpty()
                }
                else -> json.optString("photo_file", "")
            }.trim()

            if (uploadedFilename.isBlank()) {
                return@withContext Result.failure(
                    Exception(
                        if (isEn) {
                            "Column 'photo_file' on wali_gallery_posts is not active yet. Run Sync Schema."
                        } else {
                            "Kolom 'photo_file' pada wali_gallery_posts belum aktif di server. Jalankan Sync Skema di Superadmin."
                        }
                    )
                )
            }

            val cdnUrl = DashboardBannerItem.buildR2CdnUrl(collectionId, recordId, uploadedFilename)
            val fallbackUrl = DashboardBannerItem.buildPbApiFileUrl(baseUrl, collectionId, recordId, uploadedFilename)
            val finalized = draftPost.copy(
                id = safeId,
                schoolId = cleanSchool,
                photoUrl = cdnUrl,
                photoFallbackUrl = fallbackUrl,
                pbRecordId = recordId,
                pbCollectionId = collectionId,
                pbFilename = uploadedFilename,
                isMediaPurgedFromServer = false
            )
            uploadSingleWaliGalleryPost(context, finalized)
            Result.success(finalized)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploadWaliGalleryPostWithPhoto", e)
            Result.failure(e)
        }
    }

    suspend fun uploadSingleWaliGalleryPost(context: Context, post: WaliGalleryPost): Boolean =
        withContext(Dispatchers.IO) {
            val service = PocketBaseClient.getService(context) ?: return@withContext false
            val dto = PbWaliGalleryPostDto.fromDomain(post)
            upsertSingleDto(service, "wali_gallery_posts", dto.id!!, dto)
        }

    /**
     * Menghapus file foto sementara dari PocketBase & Cloudflare R2 (PATCH photo_file = null)
     * agar ruang penyimpanan server tetap hemat setelah orang tua mengunduh ke galeri HP.
     */
    suspend fun purgeWaliGalleryServerMedia(
        context: Context,
        post: WaliGalleryPost
    ): Result<WaliGalleryPost> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val purgedLocal = post.copy(
            photoUrl = "",
            photoFallbackUrl = "",
            pbFilename = "",
            isMediaPurgedFromServer = true,
            updatedAt = maxOf(now, post.updatedAt + 1L)
        )
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.success(purgedLocal)
        }
        val baseUrl = PocketBaseConfig.getBaseUrl(context).trim().trimEnd('/')
        if (!baseUrl.startsWith("http")) {
            return@withContext Result.success(purgedLocal)
        }
        val pbId = WaliGalleryHelper.resolveRecordPbId(post)
        val payload = JSONObject().apply {
            put("photo_file", JSONObject.NULL)
            put("photo_url", "")
            put("photo_fallback_url", "")
            put("pb_filename", "")
            put("is_media_purged_from_server", true)
            put("updated_at", purgedLocal.updatedAt)
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("$baseUrl/api/collections/wali_gallery_posts/records/$pbId")
            .patch(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        return@withContext runCatching {
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful && resp.code != 404) {
                    error("HTTP ${resp.code}: ${resp.body?.string().orEmpty()}")
                }
            }
            purgedLocal
        }
    }

    suspend fun deleteRemoteWaliGalleryPost(context: Context, post: WaliGalleryPost): Boolean =
        withContext(Dispatchers.IO) {
            val service = PocketBaseClient.getService(context) ?: return@withContext false
            val pbId = WaliGalleryHelper.resolveRecordPbId(post)
            val resp = runCatching { service.deleteRecord("wali_gallery_posts", pbId) }.getOrNull()
            resp != null && (resp.isSuccessful || resp.code() == 404)
        }

    private suspend fun syncWaliGalleryMerged(
        service: PocketBaseService,
        schoolId: String,
        filter: String,
        repository: HealthRepository,
        pbBaseUrl: String
    ) {
        val localPosts = repository.getAllWaliGalleryPostsIncludingDeletedList()
            .filter { it.schoolId.equals(schoolId, ignoreCase = true) }
        val remoteRes = fetchAllPages { page ->
            service.listWaliGalleryPosts(page = page, perPage = 500, filter = filter)
        }
        val remotePosts = remoteRes.getOrNull()
            ?.map { it.toDomainModel(schoolId, pbBaseUrl) }
            ?: return

        val localById = localPosts.associateBy { it.id }
        val remoteById = remotePosts.associateBy { it.id }

        val mergedRemoteToLocal = remotePosts.map { rem ->
            val loc = localById[rem.id]
            if (loc == null) {
                rem
            } else {
                val mergedDownloaded = (WaliGalleryHelper.parseRecipientKeys(loc.downloadedByCsv) +
                    WaliGalleryHelper.parseRecipientKeys(rem.downloadedByCsv)).sorted().joinToString(",")
                val newest = if (rem.updatedAt >= loc.updatedAt) rem else loc
                val isPurged = loc.isMediaPurgedFromServer || rem.isMediaPurgedFromServer
                newest.copy(
                    downloadedByCsv = mergedDownloaded,
                    isMediaPurgedFromServer = isPurged,
                    photoUrl = if (isPurged) "" else newest.photoUrl,
                    photoFallbackUrl = if (isPurged) "" else newest.photoFallbackUrl,
                    pbFilename = if (isPurged) "" else newest.pbFilename
                )
            }
        }
        if (mergedRemoteToLocal.isNotEmpty()) {
            repository.insertAllWaliGalleryPosts(mergedRemoteToLocal)
        }

        val toPush = localPosts.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null ||
                loc.updatedAt > rem.updatedAt ||
                (WaliGalleryHelper.parseRecipientKeys(loc.downloadedByCsv) -
                    WaliGalleryHelper.parseRecipientKeys(rem.downloadedByCsv)).isNotEmpty()
        }.map { loc ->
            val rem = remoteById[loc.id]
            val mergedDownloaded = if (rem != null) {
                (WaliGalleryHelper.parseRecipientKeys(loc.downloadedByCsv) +
                    WaliGalleryHelper.parseRecipientKeys(rem.downloadedByCsv)).sorted().joinToString(",")
            } else loc.downloadedByCsv
            loc.copy(downloadedByCsv = mergedDownloaded)
        }
        if (toPush.isNotEmpty()) {
            val pairs = toPush.map { p ->
                val dto = PbWaliGalleryPostDto.fromDomain(p)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "wali_gallery_posts", pairs)
        }
    }

    suspend fun syncWaliGalleryOnlineQuietly(
        context: Context,
        schoolId: String,
        repository: HealthRepository
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext
        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        val filter = buildSchoolFilter(cleanSchoolId)
        val baseUrl = PocketBaseConfig.getBaseUrl(context)

        runCatching { syncWaliGalleryMerged(service, cleanSchoolId, filter, repository, baseUrl) }

        // Pembersihan otomatis file server (PocketBase + R2) untuk postingan yang sudah kedaluwarsa
        // atau sudah selesai diunduh oleh seluruh orang tua target.
        val activePosts = repository.getActiveWaliGalleryPostsBySchoolList(cleanSchoolId)
        val now = System.currentTimeMillis()
        for (post in activePosts) {
            if (WaliGalleryHelper.shouldAutoPurgeServerMedia(post, now)) {
                val purged = purgeWaliGalleryServerMedia(context, post).getOrNull()
                if (purged != null) {
                    repository.updateWaliGalleryPost(purged)
                }
            }
        }
    }

    // --- Modul Agama Kristen (Doa Pagi, Doa Malam, Kegiatan Gereja + Foto 90 Hari R2) ---

    suspend fun uploadChristianWorshipWithPhoto(
        context: Context,
        draftRecord: ChristianWorshipRecord,
        imageUri: Uri
    ): Result<ChristianWorshipRecord> = withContext(Dispatchers.IO) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.failure(
                Exception(if (isEn) "PocketBase server is not configured." else "Server PocketBase belum terkonfigurasi.")
            )
        }
        val baseUrl = PocketBaseConfig.getBaseUrl(context).trim().trimEnd('/')
        if (!baseUrl.startsWith("http")) {
            return@withContext Result.failure(
                Exception(if (isEn) "Invalid PocketBase URL." else "URL PocketBase tidak valid.")
            )
        }

        val compressed = com.example.util.WaliGalleryMediaHelper.compressGalleryPhotoBytes(context, imageUri)
            ?: return@withContext Result.failure(
                Exception(if (isEn) "Unable to compress worship photo." else "Gagal memproses foto kegiatan ibadah.")
            )

        val now = System.currentTimeMillis()
        val safeId = if (draftRecord.id > 0L) draftRecord.id else generateCloudSafeId()
        val pbId = draftRecord.pbRecordId.trim().takeIf { it.length == 15 } ?: PocketBaseIdCodec.toPbId(safeId)
        val cleanSchool = draftRecord.schoolId.ifBlank { "SRT1-KDR" }
        val uploadedAt = draftRecord.proofUploadedAt ?: now
        val expiresAt = draftRecord.proofExpiresAt ?: ChristianWorshipHelper.computeExpiresAt(uploadedAt)
        val filename = "ibadah_kristen_${safeId}_${now}.webp"

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()

        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("id", pbId)
            .addFormDataPart("local_id", safeId.toString())
            .addFormDataPart("school_id", cleanSchool)
            .addFormDataPart("student_id", draftRecord.studentId.toString())
            .addFormDataPart("student_name", draftRecord.studentName)
            .addFormDataPart("student_nik", com.example.util.StudentCsvParser.cleanDigitsOnly(draftRecord.studentNik))
            .addFormDataPart("student_nis", draftRecord.studentNis)
            .addFormDataPart("student_class", draftRecord.studentClass)
            .addFormDataPart("student_dorm", draftRecord.studentDorm)
            .addFormDataPart("activity_type", ChristianWorshipHelper.normalizeType(draftRecord.activityType))
            .addFormDataPart("activity_date", draftRecord.activityDate.toString())
            .addFormDataPart("date_key", draftRecord.dateKey.ifBlank { ChristianWorshipHelper.dateKeyFromMillis(draftRecord.activityDate) })
            .addFormDataPart("title", draftRecord.title)
            .addFormDataPart("notes", draftRecord.notes)
            .addFormDataPart("wali_username", draftRecord.waliUsername)
            .addFormDataPart("wali_name", draftRecord.waliName)
            .addFormDataPart("proof_uploaded_at", uploadedAt.toString())
            .addFormDataPart("proof_expires_at", expiresAt.toString())
            .addFormDataPart("proof_status", ChristianWorshipRecord.PROOF_STATUS_ACTIVE)
            .addFormDataPart("is_media_purged_from_server", "false")
            .addFormDataPart("is_deleted", "false")
            .addFormDataPart("created_at", draftRecord.createdAt.toString())
            .addFormDataPart("updated_at", draftRecord.updatedAt.toString())
            .addFormDataPart("photo_file", filename, compressed.toRequestBody("image/webp".toMediaType()))
            .build()

        val postRequest = Request.Builder()
            .url("$baseUrl/api/collections/christian_worship_records/records")
            .post(multipart)
            .build()

        return@withContext try {
            var responseCode = 0
            var responseRaw = ""
            var wasSuccess = false

            client.newCall(postRequest).execute().use { resp ->
                responseCode = resp.code
                responseRaw = resp.body?.string().orEmpty()
                wasSuccess = resp.isSuccessful
            }

            if (responseCode == 404) {
                return@withContext Result.failure(
                    Exception(
                        if (isEn) {
                            "Collection 'christian_worship_records' is not synced on server yet. Run Sync Schema in Superadmin."
                        } else {
                            "Koleksi 'christian_worship_records' belum disinkronkan di server. Jalankan Sync Skema di Superadmin terlebih dahulu."
                        }
                    )
                )
            }

            if (!wasSuccess) {
                val errMsg = parsePocketBaseUploadError(responseRaw, responseCode, isEn)
                return@withContext Result.failure(
                    Exception(if (isEn) "Failed to upload photo: $errMsg" else "Gagal mengunggah foto ke R2/PocketBase: $errMsg")
                )
            }

            val json = JSONObject(responseRaw)
            val recordId = json.optString("id", pbId).trim().ifBlank { pbId }
            val collectionId = json.optString("collectionId", "christian_worship_records")
                .ifBlank { "christian_worship_records" }
            val uploadedFilename = when {
                json.has("photo_file") && json.optJSONArray("photo_file") != null -> {
                    json.optJSONArray("photo_file")?.optString(0, "").orEmpty()
                }
                else -> json.optString("photo_file", "")
            }.trim()

            if (uploadedFilename.isBlank()) {
                return@withContext Result.failure(
                    Exception(
                        if (isEn) {
                            "Column 'photo_file' on christian_worship_records is not active yet. Run Sync Schema."
                        } else {
                            "Kolom 'photo_file' pada christian_worship_records belum aktif di server. Jalankan Sync Skema di Superadmin."
                        }
                    )
                )
            }

            val cdnUrl = DashboardBannerItem.buildR2CdnUrl(collectionId, recordId, uploadedFilename)
            val fallbackUrl = DashboardBannerItem.buildPbApiFileUrl(baseUrl, collectionId, recordId, uploadedFilename)
            val finalized = draftRecord.copy(
                id = safeId,
                schoolId = cleanSchool,
                photoUrl = cdnUrl,
                photoFallbackUrl = fallbackUrl,
                pbRecordId = recordId,
                pbCollectionId = collectionId,
                pbFilename = uploadedFilename,
                proofUploadedAt = uploadedAt,
                proofExpiresAt = expiresAt,
                proofStatus = ChristianWorshipRecord.PROOF_STATUS_ACTIVE,
                isMediaPurgedFromServer = false
            )
            uploadSingleChristianWorshipRecord(context, finalized)
            Result.success(finalized)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploadChristianWorshipWithPhoto", e)
            Result.failure(e)
        }
    }

    suspend fun uploadSingleChristianWorshipRecord(context: Context, record: ChristianWorshipRecord): Boolean =
        withContext(Dispatchers.IO) {
            val service = PocketBaseClient.getService(context) ?: return@withContext false
            val dto = PbChristianWorshipRecordDto.fromDomain(record)
            upsertSingleDto(service, "christian_worship_records", dto.id!!, dto)
        }

    /**
     * Menghapus file foto ibadah Kristen dari PocketBase & R2 setelah masa retensi 90 hari berakhir,
     * sementara catatan ibadah tetap tersimpan permanen.
     */
    suspend fun purgeChristianWorshipServerMedia(
        context: Context,
        record: ChristianWorshipRecord
    ): Result<ChristianWorshipRecord> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val purgedLocal = record.copy(
            photoUrl = "",
            photoFallbackUrl = "",
            localPhotoPath = "",
            pbFilename = "",
            proofStatus = ChristianWorshipRecord.PROOF_STATUS_EXPIRED,
            isMediaPurgedFromServer = true,
            updatedAt = maxOf(now, record.updatedAt + 1L)
        )
        if (!PocketBaseConfig.isConfigured(context)) {
            return@withContext Result.success(purgedLocal)
        }
        val baseUrl = PocketBaseConfig.getBaseUrl(context).trim().trimEnd('/')
        if (!baseUrl.startsWith("http")) {
            return@withContext Result.success(purgedLocal)
        }
        val pbId = ChristianWorshipHelper.resolveRecordPbId(record)
        val payload = JSONObject().apply {
            put("photo_file", JSONObject.NULL)
            put("photo_url", "")
            put("photo_fallback_url", "")
            put("pb_filename", "")
            put("proof_status", ChristianWorshipRecord.PROOF_STATUS_EXPIRED)
            put("is_media_purged_from_server", true)
            put("updated_at", purgedLocal.updatedAt)
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("$baseUrl/api/collections/christian_worship_records/records/$pbId")
            .patch(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        return@withContext runCatching {
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful && resp.code != 404) {
                    error("HTTP ${resp.code}: ${resp.body?.string().orEmpty()}")
                }
            }
            purgedLocal
        }
    }

    suspend fun deleteRemoteChristianWorshipRecord(context: Context, record: ChristianWorshipRecord): Boolean =
        withContext(Dispatchers.IO) {
            val service = PocketBaseClient.getService(context) ?: return@withContext false
            val pbId = ChristianWorshipHelper.resolveRecordPbId(record)
            val resp = runCatching { service.deleteRecord("christian_worship_records", pbId) }.getOrNull()
            resp != null && (resp.isSuccessful || resp.code() == 404)
        }

    private suspend fun syncChristianWorshipMerged(
        service: PocketBaseService,
        schoolId: String,
        filter: String,
        repository: HealthRepository,
        pbBaseUrl: String
    ) {
        val localRecords = repository.getAllChristianWorshipRecordsIncludingDeletedList()
            .filter { it.schoolId.equals(schoolId, ignoreCase = true) }
        val remoteRes = fetchAllPages { page ->
            service.listChristianWorshipRecords(page = page, perPage = 500, filter = filter)
        }
        val remoteRecords = remoteRes.getOrNull()
            ?.map { it.toDomainModel(schoolId, pbBaseUrl) }
            ?: return

        val localById = localRecords.associateBy { it.id }
        val remoteById = remoteRecords.associateBy { it.id }

        val mergedRemoteToLocal = remoteRecords.map { rem ->
            val loc = localById[rem.id]
            if (loc == null) {
                rem
            } else {
                val newest = if (rem.updatedAt >= loc.updatedAt) rem else loc
                val isPurged = loc.isMediaPurgedFromServer || rem.isMediaPurgedFromServer
                newest.copy(
                    isMediaPurgedFromServer = isPurged,
                    proofStatus = if (isPurged) ChristianWorshipRecord.PROOF_STATUS_EXPIRED else newest.proofStatus,
                    photoUrl = if (isPurged) "" else newest.photoUrl,
                    photoFallbackUrl = if (isPurged) "" else newest.photoFallbackUrl,
                    pbFilename = if (isPurged) "" else newest.pbFilename
                )
            }
        }
        if (mergedRemoteToLocal.isNotEmpty()) {
            repository.insertAllChristianWorshipRecords(mergedRemoteToLocal)
        }

        val toPush = localRecords.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPush.isNotEmpty()) {
            val pairs = toPush.map { r ->
                val dto = PbChristianWorshipRecordDto.fromDomain(r)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "christian_worship_records", pairs)
        }
    }

    suspend fun syncChristianWorshipOnlineQuietly(
        context: Context,
        schoolId: String,
        repository: HealthRepository
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext
        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        val filter = buildSchoolFilter(cleanSchoolId)
        val baseUrl = PocketBaseConfig.getBaseUrl(context)

        runCatching { syncChristianWorshipMerged(service, cleanSchoolId, filter, repository, baseUrl) }

        // Pembersihan otomatis file foto ibadah Kristen hanya setelah masa retensi 90 hari berakhir
        val activeRecords = repository.getChristianWorshipRecordsBySchoolList(cleanSchoolId)
        val now = System.currentTimeMillis()
        for (record in activeRecords) {
            if (!record.isMediaPurgedFromServer &&
                record.proofExpiresAt != null &&
                record.proofExpiresAt <= now &&
                (record.photoUrl.isNotBlank() || record.photoFallbackUrl.isNotBlank())
            ) {
                val purged = purgeChristianWorshipServerMedia(context, record).getOrNull()
                if (purged != null) {
                    repository.updateChristianWorshipRecord(purged)
                }
            }
        }
    }

    // --- Modul Alkitab Digital & Catatan Pribadi (Bookmark & Catatan Pribadi Sinkron) ---
    suspend fun uploadSingleBibleBookmark(context: Context, bookmark: BibleBookmark): Boolean =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext false
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext false
                val dto = PbBibleBookmarkDto.fromDomain(bookmark)
                upsertSingleDto(service, "bible_bookmarks", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleBibleBookmark ke PocketBase", e)
                false
            }
        }

    suspend fun uploadSingleBibleNote(context: Context, note: BibleNote): Boolean =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext false
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext false
                val dto = PbBibleNoteDto.fromDomain(note)
                upsertSingleDto(service, "bible_notes", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleBibleNote ke PocketBase", e)
                false
            }
        }

    suspend fun deleteRemoteBibleDataByOwner(
        context: Context,
        schoolId: String,
        ownerUsername: String
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val cleanSchool = schoolId.trim().ifBlank { "SRT1-KDR" }
        val cleanUser = ownerUsername.trim()
        if (cleanUser.isBlank()) return@withContext
        val safeSchool = cleanSchool.replace("'", "\\'")
        val safeUser = cleanUser.replace("'", "\\'")
        val filter = "school_id = '$safeSchool' && owner_wali_username = '$safeUser'"
        val service = PocketBaseClient.getService(context) ?: return@withContext
        runCatching { deleteCollectionByFilter(service, "bible_bookmarks", filter) }
        runCatching { deleteCollectionByFilter(service, "bible_notes", filter) }
    }

    private suspend fun syncBibleBookmarksMerged(
        service: PocketBaseService,
        schoolId: String,
        filter: String,
        repository: HealthRepository
    ) {
        val localAll = repository.getAllBibleBookmarksIncludingDeletedList()
            .filter { it.schoolId.equals(schoolId, ignoreCase = true) }
        val remoteRes = fetchAllPages { page ->
            service.listBibleBookmarks(page = page, perPage = 500, filter = filter)
        }
        val remoteList = remoteRes.getOrNull()?.map { it.toDomainModel(schoolId) } ?: return

        val localById = localAll.associateBy { it.id }
        val remoteById = remoteList.associateBy { it.id }

        val toInsertLocal = remoteList.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllBibleBookmarks(toInsertLocal)
        }

        val toPushRemote = localAll.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { item ->
                val dto = PbBibleBookmarkDto.fromDomain(item)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "bible_bookmarks", pairs)
        }
    }

    private suspend fun syncBibleNotesMerged(
        service: PocketBaseService,
        schoolId: String,
        filter: String,
        repository: HealthRepository
    ) {
        val localAll = repository.getAllBibleNotesIncludingDeletedList()
            .filter { it.schoolId.equals(schoolId, ignoreCase = true) }
        val remoteRes = fetchAllPages { page ->
            service.listBibleNotes(page = page, perPage = 500, filter = filter)
        }
        val remoteList = remoteRes.getOrNull()?.map { it.toDomainModel(schoolId) } ?: return

        val localById = localAll.associateBy { it.id }
        val remoteById = remoteList.associateBy { it.id }

        val toInsertLocal = remoteList.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllBibleNotes(toInsertLocal)
        }

        val toPushRemote = localAll.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { item ->
                val dto = PbBibleNoteDto.fromDomain(item)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "bible_notes", pairs)
        }
    }

    suspend fun syncBibleOnlineQuietly(
        context: Context,
        schoolId: String,
        repository: HealthRepository
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext
        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        val filter = buildSchoolFilter(cleanSchoolId)
        runCatching { syncBibleBookmarksMerged(service, cleanSchoolId, filter, repository) }
        runCatching { syncBibleNotesMerged(service, cleanSchoolId, filter, repository) }
    }

    suspend fun uploadSingleSharedJournalNote(context: Context, note: SharedJournalNote): Boolean =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context)) return@withContext false
            try {
                val service = PocketBaseClient.getService(context) ?: return@withContext false
                val dto = PbSharedJournalNoteDto.fromDomain(note)
                upsertSingleDto(service, "shared_journal_notes", dto.id!!, dto)
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleSharedJournalNote ke PocketBase", e)
                false
            }
        }

    private suspend fun syncSharedJournalNotesMerged(
        service: PocketBaseService,
        schoolId: String,
        filter: String,
        repository: HealthRepository
    ) {
        val localAll = repository.getAllSharedJournalNotesIncludingDeletedList()
            .filter { it.schoolId.equals(schoolId, ignoreCase = true) }
        val remoteRes = fetchAllPages { page ->
            service.listSharedJournalNotes(page = page, perPage = 500, filter = filter)
        }
        val remoteList = remoteRes.getOrNull()?.map { it.toDomainModel(schoolId) } ?: return

        val localById = localAll.associateBy { it.id }
        val remoteById = remoteList.associateBy { it.id }

        val toInsertLocal = remoteList.filter { rem ->
            val loc = localById[rem.id]
            loc == null || rem.updatedAt >= loc.updatedAt
        }
        if (toInsertLocal.isNotEmpty()) {
            repository.insertAllSharedJournalNotes(toInsertLocal)
        }

        val toPushRemote = localAll.filter { loc ->
            val rem = remoteById[loc.id]
            rem == null || loc.updatedAt > rem.updatedAt
        }
        if (toPushRemote.isNotEmpty()) {
            val pairs = toPushRemote.map { item ->
                val dto = PbSharedJournalNoteDto.fromDomain(item)
                dto.id!! to dto
            }
            upsertBatchOrFallback(service, "shared_journal_notes", pairs)
        }
    }

    suspend fun syncSharedJournalNotesOnlineQuietly(
        context: Context,
        schoolId: String,
        repository: HealthRepository
    ) = withContext(Dispatchers.IO) {
        if (!PocketBaseConfig.isConfigured(context)) return@withContext
        val service = PocketBaseClient.getService(context) ?: return@withContext
        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        val filter = buildSchoolFilter(cleanSchoolId)
        runCatching { syncSharedJournalNotesMerged(service, cleanSchoolId, filter, repository) }
    }
}
