package com.example.data.remote.supabase

import android.content.Context
import android.util.Log
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SupabaseSyncManager {
    private const val TAG = "SupabaseSyncManager"

    /**
     * Menguji koneksi ke Supabase dan memverifikasi akses ke tabel.
     */
    suspend fun testConnection(context: Context): Result<String> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured(context)) {
            return@withContext Result.failure(Exception("URL atau Anon Key Supabase belum diisi."))
        }

        val service = SupabaseClient.getService(context)
            ?: return@withContext Result.failure(Exception("Gagal membuat koneksi ke Supabase."))

        try {
            val response = service.getStudents()
            if (response.isSuccessful) {
                val studentCount = response.body()?.size ?: 0
                Result.success("Koneksi berhasil! Ditemukan $studentCount siswa di Supabase.")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Status HTTP: ${response.code()}"
                Result.failure(Exception("Respon Supabase (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal uji koneksi Supabase", e)
            Result.failure(Exception("Gagal menghubungi server Supabase: ${e.localizedMessage}"))
        }
    }

    /**
     * Mengunggah seluruh data siswa lokal ke tabel child_students di Supabase.
     */
    suspend fun uploadStudents(context: Context, students: List<ChildStudent>): Result<Int> =
        withContext(Dispatchers.IO) {
            val service = SupabaseClient.getService(context)
                ?: return@withContext Result.failure(Exception("Supabase belum terkonfigurasi."))

            if (students.isEmpty()) {
                return@withContext Result.success(0)
            }

            try {
                // Batch per 50 siswa agar aman dari batas payload HTTP
                var uploaded = 0
                students.chunked(50).forEach { batch ->
                    val dtos = batch.map { SupabaseStudentDto.fromDomain(it) }
                    val response = service.upsertStudents(dtos)
                    if (!response.isSuccessful) {
                        val err = response.errorBody()?.string() ?: "${response.code()}"
                        return@withContext Result.failure(Exception("Gagal unggah siswa: $err"))
                    }
                    uploaded += batch.size
                }
                Result.success(uploaded)
            } catch (e: Exception) {
                Log.e(TAG, "Error uploadStudents", e)
                Result.failure(e)
            }
        }

    /**
     * Sinkronisasi dua arah untuk rekam medis:
     * 1. Unggah rekam medis lokal ke Supabase.
     * 2. Ambil data rekam medis dari Supabase dan panggil callback untuk disimpan ke lokal.
     */
    suspend fun syncMedicalRecords(
        context: Context,
        localRecords: List<MedicalRecord>,
        onRemoteRecordsFetched: suspend (List<MedicalRecord>) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        val service = SupabaseClient.getService(context)
            ?: return@withContext Result.failure(Exception("Supabase belum terkonfigurasi."))

        try {
            // 1. Upload local records jika ada
            if (localRecords.isNotEmpty()) {
                val localDtos = localRecords.map { SupabaseMedicalRecordDto.fromDomain(it) }
                localDtos.chunked(50).forEach { batch ->
                    val response = service.upsertMedicalRecords(batch)
                    if (!response.isSuccessful) {
                        Log.w(TAG, "Gagal push records ke Supabase: ${response.code()}")
                    }
                }
            }

            // 2. Fetch all from Supabase
            val remoteResponse = service.getMedicalRecords()
            if (remoteResponse.isSuccessful) {
                val remoteList = remoteResponse.body() ?: emptyList()
                val domainRecords = remoteList.map { it.toDomainModel() }
                onRemoteRecordsFetched(domainRecords)
                SupabaseConfig.setLastSyncTime(context, System.currentTimeMillis())
                Result.success("Sinkronisasi berhasil! ${domainRecords.size} rekam medis terhubung.")
            } else {
                val err = remoteResponse.errorBody()?.string() ?: "${remoteResponse.code()}"
                Result.failure(Exception("Gagal sinkron dari Supabase: $err"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncMedicalRecords", e)
            Result.failure(e)
        }
    }

    /**
     * Mengunggah satu rekam medis secara instan di latar belakang saat baru dibuat / diperbarui.
     */
    suspend fun uploadSingleRecord(context: Context, record: MedicalRecord) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                val dto = SupabaseMedicalRecordDto.fromDomain(record)
                service.upsertMedicalRecords(listOf(dto))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleRecord ke Supabase", e)
            }
        }

    /**
     * Menghapus rekam medis dari Supabase jika dihapus di lokal.
     */
    suspend fun deleteRemoteRecord(context: Context, recordId: Long) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.deleteMedicalRecord("eq.$recordId")
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteRecord ke Supabase", e)
            }
        }
}
