package com.example.data.remote.pocketbase

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
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
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
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

    // --- School Profiles ---
    @GET("api/collections/school_profiles/records")
    suspend fun listSchoolProfiles(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-updated_at"
    ): Response<PbListResponse<PbSchoolProfileDto>>

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
