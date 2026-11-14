package com.example.data.remote.pocketbase

import android.content.Context
import android.util.Log
import com.example.data.model.ChildStudent
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolProfile
import com.example.data.model.SchoolRegistration
import com.example.data.model.ShiftMessage
import com.example.data.model.WaliAccount
import com.example.data.repository.HealthRepository
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.Response
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

    /**
     * Menghapus seluruh data satu tenant sekolah dari PocketBase saat akun sekolah dihapus permanen.
     */
    suspend fun deleteAllSchoolData(context: Context, schoolId: String) =
        withContext(Dispatchers.IO) {
            if (!PocketBaseConfig.isConfigured(context) || schoolId.isBlank()) return@withContext
            val filter = buildSchoolFilter(schoolId)
            val service = PocketBaseClient.getService(context) ?: return@withContext
            runCatching { deleteCollectionByFilter(service, "fasting_records", filter) }
            runCatching { deleteCollectionByFilter(service, "fasting_sessions", filter) }
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

        runCatching { syncFastingSessionsMerged(service, cleanSchoolId, filter, repository) }
        runCatching { syncFastingRecordsMerged(service, cleanSchoolId, filter, repository) }
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

    private suspend fun syncFastingRecordsMerged(
        service: PocketBaseService,
        cleanSchoolId: String,
        filter: String,
        repository: HealthRepository
    ) {
        val localRecords = repository.getFastingRecordsBySchoolList(cleanSchoolId)
        val remoteRes = fetchAllPages { page ->
            service.listFastingRecords(page = page, perPage = 500, filter = filter)
        }
        val remoteDtos = remoteRes.getOrNull() ?: return
        val remoteRecords = remoteDtos.map { it.toDomainModel(cleanSchoolId) }

        val localById = localRecords.associateBy { it.id }
        val remoteById = remoteRecords.associateBy { it.id }
        val dirtyRemoteIds = remoteDtos.mapNotNull { dto ->
            val rawNik = dto.studentNik.orEmpty()
            val cleanedNik = com.example.util.StudentCsvParser.cleanDigitsOnly(rawNik)
            if (rawNik != cleanedNik) PocketBaseIdCodec.resolveLocalId(dto.localId, dto.id) else null
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
        onRemoteSchoolProfileUpdated: ((SchoolProfile, Long) -> Unit)? = null
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

            // 3. Sinkronkan Rekam Medis & Kontrol (medical_records) - Merge by timestamp
            var recordCount = 0
            runCatching {
                val localRecords = repository.getAllRecordsList().filter { it.studentId in validStudentIds }
                val remoteRecRes = fetchAllPages { page ->
                    service.listMedicalRecords(page = page, perPage = 500, filter = filter)
                }
                if (remoteRecRes.isSuccess) {
                    val remoteRecords = (remoteRecRes.getOrNull() ?: emptyList())
                        .map { it.toDomainModel() }
                        .filter { it.studentId in validStudentIds }

                    val localById = localRecords.associateBy { it.id }
                    val remoteById = remoteRecords.associateBy { it.id }

                    val toInsertLocal = remoteRecords.filter { rem ->
                        val loc = localById[rem.id]
                        loc == null || medicalRecordTimestamp(rem) >= medicalRecordTimestamp(loc)
                    }
                    if (toInsertLocal.isNotEmpty()) {
                        repository.insertAllRecords(toInsertLocal)
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
                syncFastingRecordsMerged(service, cleanSchoolId, filter, repository)
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
}
