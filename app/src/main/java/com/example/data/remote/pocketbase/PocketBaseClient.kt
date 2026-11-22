package com.example.data.remote.pocketbase

import android.content.Context
import com.example.data.model.SavingRetentionHelper
import com.example.data.model.SavingTransaction
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File
import java.util.concurrent.TimeUnit

interface PocketBaseService {
    @GET("api/health")
    suspend fun getHealth(): Response<PbHealthResponse>

    // --- Child Students ---
    @GET("api/collections/child_students/records")
    suspend fun listStudents(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "name"
    ): Response<PbListResponse<PbStudentDto>>

    // --- Medical Records ---
    @GET("api/collections/medical_records/records")
    suspend fun listMedicalRecords(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-created_at"
    ): Response<PbListResponse<PbMedicalRecordDto>>

    // --- Haid Records ---
    @GET("api/collections/haid_records/records")
    suspend fun listHaidRecords(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-start_date"
    ): Response<PbListResponse<PbHaidRecordDto>>

    // --- Wali Accounts ---
    @GET("api/collections/wali_accounts/records")
    suspend fun listWaliAccounts(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "name"
    ): Response<PbListResponse<PbWaliAccountDto>>

    // --- Shift Messages ---
    @GET("api/collections/shift_messages/records")
    suspend fun listShiftMessages(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-created_at"
    ): Response<PbListResponse<PbShiftMessageDto>>

    // --- School Registrations ---
    @GET("api/collections/school_registrations/records")
    suspend fun listSchoolRegistrations(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-submission_date"
    ): Response<PbListResponse<PbSchoolRegistrationDto>>

    // --- Fasting Sessions ---
    @GET("api/collections/fasting_sessions/records")
    suspend fun listFastingSessions(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-session_date"
    ): Response<PbListResponse<PbFastingSessionDto>>

    // --- Fasting Records ---
    @GET("api/collections/fasting_records/records")
    suspend fun listFastingRecords(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-created_at"
    ): Response<PbListResponse<PbFastingRecordDto>>

    // --- Mengaji Records ---
    @GET("api/collections/mengaji_records/records")
    suspend fun listMengajiRecords(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-session_date"
    ): Response<PbListResponse<PbMengajiRecordDto>>

    // --- Visit Sessions (Kunjungan) ---
    @GET("api/collections/visit_sessions/records")
    suspend fun listVisitSessions(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-created_at"
    ): Response<PbListResponse<PbVisitSessionDto>>

    // --- Visit Records (Kehadiran Kunjungan) ---
    @GET("api/collections/visit_records/records")
    suspend fun listVisitRecords(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-checked_in_at"
    ): Response<PbListResponse<PbVisitRecordDto>>

    // --- Visit Confirmations (Konfirmasi Kehadiran Orang Tua) ---
    @GET("api/collections/visit_confirmations/records")
    suspend fun listVisitConfirmations(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-updated_at"
    ): Response<PbListResponse<PbVisitConfirmationDto>>

    // --- Visit Session Swaps (Pindah Sesi Kunjungan per Anak Asuh) ---
    @GET("api/collections/visit_session_swaps/records")
    suspend fun listVisitSessionSwaps(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-updated_at"
    ): Response<PbListResponse<PbVisitSessionSwapDto>>

    // --- School Profiles ---
    @GET("api/collections/school_profiles/records")
    suspend fun listSchoolProfiles(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-updated_at"
    ): Response<PbListResponse<PbSchoolProfileDto>>

    // --- Parent Access Links (Kartu Akses & Pilihan Akses Orang Tua) ---
    @GET("api/collections/parent_access_links/records")
    suspend fun listParentAccessLinks(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "student_name"
    ): Response<PbListResponse<PbParentAccessLinkDto>>

    // --- Parent Messages (Pesan Dua Arah Wali Asuh <-> Orang Tua) ---
    @GET("api/collections/parent_messages/records")
    suspend fun listParentMessages(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "created_at"
    ): Response<PbListResponse<PbParentMessageDto>>

    // --- Parent Expectations (Harapan Karakter Orang Tua untuk Anak) ---
    @GET("api/collections/parent_expectations/records")
    suspend fun listParentExpectations(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-updated_at"
    ): Response<PbListResponse<PbParentExpectationDto>>

    // --- Deletion Logs (Log Audit Hapus Riwayat) ---
    @GET("api/collections/deletion_logs/records")
    suspend fun listDeletionLogs(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-created_at"
    ): Response<PbListResponse<PbDeletionLogDto>>

    // --- Tabungan Transactions & Logs (Penitipan Uang Pribadi Wali Asuh) ---
    @GET("api/collections/tabungan_transactions/records")
    suspend fun listTabunganTransactions(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-transaction_date"
    ): Response<PbListResponse<PbTabunganTransactionDto>>

    @GET("api/collections/tabungan_logs/records")
    suspend fun listTabunganLogs(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-created_at"
    ): Response<PbListResponse<PbTabunganLogDto>>

    // --- Konseling Rahasia (Hanya Wali Asuh Bersangkutan) ---
    @GET("api/collections/konseling_records/records")
    suspend fun listKonselingRecords(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-session_date"
    ): Response<PbListResponse<PbKonselingRecordDto>>

    @GET("api/collections/konseling_logs/records")
    suspend fun listKonselingLogs(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-created_at"
    ): Response<PbListResponse<PbKonselingLogDto>>

    // --- Jadwal Kerja Wali Asuh (Shift Codes & Matriks Bulanan) ---
    @GET("api/collections/wali_shift_codes/records")
    suspend fun listWaliShiftCodes(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "sort_order"
    ): Response<PbListResponse<PbWaliShiftCodeDto>>

    @GET("api/collections/wali_schedule_entries/records")
    suspend fun listWaliScheduleEntries(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "sort_order"
    ): Response<PbListResponse<PbWaliScheduleEntryDto>>

    // --- Channel Info-Info Penting (Global Seluruh Sekolah) ---
    @GET("api/collections/important_info_posts/records")
    suspend fun listImportantInfoPosts(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-created_at"
    ): Response<PbListResponse<PbInfoPentingPostDto>>

    @GET("api/collections/important_info_reactions/records")
    suspend fun listImportantInfoReactions(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-updated_at"
    ): Response<PbListResponse<PbInfoPentingReactionDto>>

    @GET("api/collections/important_info_comments/records")
    suspend fun listImportantInfoComments(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "created_at"
    ): Response<PbListResponse<PbInfoPentingCommentDto>>

    // --- Absen Sholat 5 Waktu ---
    @GET("api/collections/sholat_records/records")
    suspend fun listSholatRecords(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-date_key,-updated_at"
    ): Response<PbListResponse<PbSholatRecordDto>>

    // --- Galeri Wali Asuh (Relay R2 & Simpan ke Galeri Ortu) ---
    @GET("api/collections/wali_gallery_posts/records")
    suspend fun listWaliGalleryPosts(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-created_at"
    ): Response<PbListResponse<PbWaliGalleryPostDto>>

    // --- Modul Agama Kristen (Doa Pagi, Doa Malam, Kegiatan ke Gereja + Foto 90 Hari R2) ---
    @GET("api/collections/christian_worship_records/records")
    suspend fun listChristianWorshipRecords(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-activity_date,-created_at"
    ): Response<PbListResponse<PbChristianWorshipRecordDto>>

    // --- Modul Alkitab Digital & Catatan Pribadi (Bookmark & Catatan Pribadi Sinkron) ---
    @GET("api/collections/bible_bookmarks/records")
    suspend fun listBibleBookmarks(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-created_at"
    ): Response<PbListResponse<PbBibleBookmarkDto>>

    @GET("api/collections/bible_notes/records")
    suspend fun listBibleNotes(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-updated_at"
    ): Response<PbListResponse<PbBibleNoteDto>>

    // --- Generic Record IDs & CRUD ---
    @GET("api/collections/{collection}/records")
    suspend fun listRecordIds(
        @Path("collection") collection: String,
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("fields") fields: String = "id"
    ): Response<PbListResponse<PbIdOnlyDto>>

    @POST("api/collections/{collection}/records")
    suspend fun createRecord(
        @Path("collection") collection: String,
        @Body body: RequestBody
    ): Response<ResponseBody>

    @PATCH("api/collections/{collection}/records/{id}")
    suspend fun updateRecord(
        @Path("collection") collection: String,
        @Path("id") id: String,
        @Body body: RequestBody
    ): Response<ResponseBody>

    @DELETE("api/collections/{collection}/records/{id}")
    suspend fun deleteRecord(
        @Path("collection") collection: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    // --- Saving Transactions (Tabungan Anak Asuh & Bukti Foto 3 Bulan) ---
    @Multipart
    @POST("api/collections/saving_transactions/records")
    suspend fun createSavingTransactionMultipart(
        @Part parts: List<MultipartBody.Part>
    ): Response<ResponseBody>

    @Multipart
    @PATCH("api/collections/saving_transactions/records/{id}")
    suspend fun updateSavingTransactionMultipart(
        @Path("id") id: String,
        @Part parts: List<MultipartBody.Part>
    ): Response<ResponseBody>

    @POST("api/batch")
    suspend fun executeBatch(
        @Body body: RequestBody
    ): Response<ResponseBody>
}

object PocketBaseClient {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private var cachedService: PocketBaseService? = null
    private var cachedUrl: String? = null

    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> toJsonString(dto: T): String {
        val adapter = moshi.adapter(dto::class.java as Class<T>)
        return adapter.toJson(dto)
    }

    fun <T : Any> toJsonRequestBody(dto: T): RequestBody {
        return toJsonString(dto).toRequestBody(jsonMediaType)
    }

    fun <T : Any> buildUpsertBatchRequestBody(collection: String, dtos: List<T>): RequestBody {
        val reqArr = JSONArray()
        dtos.forEach { dto ->
            val item = JSONObject().apply {
                put("method", "PUT")
                put("url", "/api/collections/$collection/records")
                put("body", JSONObject(toJsonString(dto)))
            }
            reqArr.put(item)
        }
        val root = JSONObject().apply {
            put("requests", reqArr)
        }
        return root.toString().toRequestBody(jsonMediaType)
    }

    fun buildDeleteBatchRequestBody(collection: String, ids: List<String>): RequestBody {
        val reqArr = JSONArray()
        ids.forEach { id ->
            val item = JSONObject().apply {
                put("method", "DELETE")
                put("url", "/api/collections/$collection/records/$id")
            }
            reqArr.put(item)
        }
        val root = JSONObject().apply {
            put("requests", reqArr)
        }
        return root.toString().toRequestBody(jsonMediaType)
    }

    fun buildSavingMultipartParts(
        model: SavingTransaction,
        photoFile: File?,
        includeId: Boolean
    ): List<MultipartBody.Part> {
        val safeId = if (model.id > 0L) model.id else PocketBaseIdCodec.stablePositiveHash(
            "saving_tx:${model.schoolId}:${model.studentId}:${model.transactionDate}:${model.amount}:${model.purpose}"
        )
        val pbId = PocketBaseIdCodec.toPbId(safeId)
        val isExpired = SavingRetentionHelper.isProofExpired(model)
        val parts = mutableListOf<MultipartBody.Part>()
        if (includeId) {
            parts.add(MultipartBody.Part.createFormData("id", pbId))
        }
        parts.add(MultipartBody.Part.createFormData("local_id", safeId.toString()))
        parts.add(MultipartBody.Part.createFormData("school_id", model.schoolId.ifBlank { "SRT1-KDR" }))
        parts.add(MultipartBody.Part.createFormData("student_id", model.studentId.toString()))
        parts.add(MultipartBody.Part.createFormData("student_name", model.studentName))
        parts.add(MultipartBody.Part.createFormData("student_nik", com.example.util.StudentCsvParser.cleanDigitsOnly(model.studentNik)))
        parts.add(MultipartBody.Part.createFormData("student_nis", model.studentNis))
        parts.add(MultipartBody.Part.createFormData("student_class", model.studentClass))
        parts.add(MultipartBody.Part.createFormData("student_dorm", model.studentDorm))
        parts.add(MultipartBody.Part.createFormData("transaction_type", model.transactionType))
        parts.add(MultipartBody.Part.createFormData("amount", model.amount.toString()))
        parts.add(MultipartBody.Part.createFormData("transaction_date", model.transactionDate.toString()))
        parts.add(MultipartBody.Part.createFormData("purpose", model.purpose))
        parts.add(MultipartBody.Part.createFormData("notes", model.notes))
        parts.add(MultipartBody.Part.createFormData("wali_name", model.waliName))
        parts.add(MultipartBody.Part.createFormData("proof_uploaded_at", (model.proofUploadedAt ?: 0L).toString()))
        parts.add(MultipartBody.Part.createFormData("proof_expires_at", (model.proofExpiresAt ?: 0L).toString()))
        parts.add(
            MultipartBody.Part.createFormData(
                "proof_status",
                if (isExpired) SavingTransaction.PROOF_STATUS_EXPIRED else model.proofStatus
            )
        )
        parts.add(MultipartBody.Part.createFormData("created_at", model.createdAt.toString()))
        parts.add(MultipartBody.Part.createFormData("updated_at", model.updatedAt.toString()))

        if (!isExpired && photoFile != null && photoFile.exists() && photoFile.length() > 0L) {
            val reqFile = photoFile.asRequestBody("image/jpeg".toMediaType())
            parts.add(MultipartBody.Part.createFormData("proof_photo", photoFile.name, reqFile))
        } else if (isExpired) {
            parts.add(MultipartBody.Part.createFormData("proof_photo", ""))
        }
        return parts
    }

    fun getService(context: Context): PocketBaseService? {
        val url = PocketBaseConfig.getBaseUrl(context)
        if (url.isBlank() || !url.startsWith("http")) {
            return null
        }

        if (cachedService != null && cachedUrl == url) {
            return cachedService
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()

        val baseUrl = if (url.endsWith("/")) url else "$url/"

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(PocketBaseService::class.java)
        cachedService = service
        cachedUrl = url
        return service
    }

    fun invalidateCache() {
        cachedService = null
        cachedUrl = null
    }
}
