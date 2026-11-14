package com.example.data.remote.supabase

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface SupabaseService {
    @GET("rest/v1/child_students?select=*&order=id.asc")
    suspend fun getStudents(): Response<List<SupabaseStudentDto>>

    @POST("rest/v1/child_students")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertStudents(@Body students: List<SupabaseStudentDto>): Response<ResponseBody>

    @GET("rest/v1/medical_records?select=*&order=id.desc")
    suspend fun getMedicalRecords(): Response<List<SupabaseMedicalRecordDto>>

    @POST("rest/v1/medical_records")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertMedicalRecords(@Body records: List<SupabaseMedicalRecordDto>): Response<ResponseBody>

    @DELETE("rest/v1/medical_records")
    suspend fun deleteMedicalRecord(@Query("id") idFilter: String): Response<ResponseBody>
}

object SupabaseClient {
    private var cachedService: SupabaseService? = null
    private var cachedUrl: String? = null
    private var cachedKey: String? = null

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun getService(context: Context): SupabaseService? {
        val url = SupabaseConfig.getSupabaseUrl(context)
        val key = SupabaseConfig.getSupabaseKey(context)

        if (url.isBlank() || key.isBlank() || !url.startsWith("http")) {
            return null
        }

        if (cachedService != null && cachedUrl == url && cachedKey == key) {
            return cachedService
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("apikey", key)
                .header("Authorization", "Bearer $key")
                .header("Content-Type", "application/json")
                .method(original.method, original.body)
            chain.proceed(requestBuilder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val baseUrl = if (url.endsWith("/")) url else "$url/"

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(SupabaseService::class.java)
        cachedService = service
        cachedUrl = url
        cachedKey = key
        return service
    }

    fun invalidateCache() {
        cachedService = null
        cachedUrl = null
        cachedKey = null
    }
}
