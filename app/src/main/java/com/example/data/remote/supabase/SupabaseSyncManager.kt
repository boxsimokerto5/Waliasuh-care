package com.example.data.remote.supabase

import android.content.Context
import android.util.Log
import com.example.data.model.ChildStudent
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolRegistration
import com.example.data.model.ShiftMessage
import com.example.data.model.WaliAccount
import com.example.data.repository.HealthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong

object SupabaseSyncManager {
    private const val TAG = "SupabaseSyncManager"
    private val idSequence = AtomicLong(0L)

    /**
     * Menghasilkan ID unik berbasis timestamp agar antar-sekolah dan antar-perangkat
     * tidak saling menimpa baris data saat sinkronisasi ke Supabase.
     */
    fun generateCloudSafeId(): Long {
        val nowBase = System.currentTimeMillis() * 1000L
        while (true) {
            val prev = idSequence.get()
            val next = if (nowBase > prev) nowBase else prev + 1L
            if (idSequence.compareAndSet(prev, next)) return next
        }
    }

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
     * Mengunggah daftar data siswa ke tabel child_students di Supabase.
     */
    suspend fun uploadStudents(context: Context, students: List<ChildStudent>): Result<Int> =
        withContext(Dispatchers.IO) {
            val service = SupabaseClient.getService(context)
                ?: return@withContext Result.failure(Exception("Supabase belum terkonfigurasi."))

            if (students.isEmpty()) {
                return@withContext Result.success(0)
            }

            try {
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
                SupabaseConfig.setLastSyncTime(context, System.currentTimeMillis())
                Result.success(uploaded)
            } catch (e: Exception) {
                Log.e(TAG, "Error uploadStudents", e)
                Result.failure(e)
            }
        }

    suspend fun uploadSingleStudent(context: Context, student: ChildStudent) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.upsertStudents(listOf(SupabaseStudentDto.fromDomain(student)))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleStudent ke Supabase", e)
            }
        }

    suspend fun deleteRemoteStudent(context: Context, studentId: Long) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.deleteStudent("eq.$studentId")
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteStudent ke Supabase", e)
            }
        }

    suspend fun deleteStudentsBySchool(context: Context, schoolId: String) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.deleteStudentsBySchool("eq.$schoolId")
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteStudentsBySchool ke Supabase", e)
            }
        }

    private suspend fun upsertMedicalRecordsWithFallback(
        service: SupabaseService,
        records: List<MedicalRecord>,
        schoolId: String
    ) {
        val dtos = records.map { SupabaseMedicalRecordDto.fromDomain(it, schoolId) }
        val resp = service.upsertMedicalRecords(dtos)
        if (!resp.isSuccessful && resp.code() == 400) {
            Log.w(TAG, "Kolom rawat inap belum ada di Supabase, fallback ke upsert legacy.")
            service.upsertMedicalRecordsLegacy(records.map { SupabaseMedicalRecordLegacyDto.fromDomain(it, schoolId) })
        } else if (!resp.isSuccessful) {
            Log.w(TAG, "Gagal push records ke Supabase: ${resp.code()}")
        }
    }

    // --- Medical Records ---
    suspend fun syncMedicalRecords(
        context: Context,
        schoolId: String,
        localRecords: List<MedicalRecord>,
        validStudentIds: Set<Long>,
        onRemoteRecordsFetched: suspend (List<MedicalRecord>) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        val service = SupabaseClient.getService(context)
            ?: return@withContext Result.failure(Exception("Supabase belum terkonfigurasi."))

        try {
            if (localRecords.isNotEmpty()) {
                localRecords.chunked(50).forEach { batch ->
                    upsertMedicalRecordsWithFallback(service, batch, schoolId)
                }
            }

            val remoteResponse = service.getMedicalRecordsBySchool("eq.$schoolId")
            if (remoteResponse.isSuccessful) {
                val remoteList = remoteResponse.body() ?: emptyList()
                val domainRecords = remoteList
                    .map { it.toDomainModel() }
                    .filter { it.studentId in validStudentIds }
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

    suspend fun uploadSingleRecord(
        context: Context,
        record: MedicalRecord,
        schoolId: String = "SRT1-KDR"
    ) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured(context)) return@withContext
        try {
            val service = SupabaseClient.getService(context) ?: return@withContext
            upsertMedicalRecordsWithFallback(service, listOf(record), schoolId)
        } catch (e: Exception) {
            Log.e(TAG, "Silent error uploadSingleRecord ke Supabase", e)
        }
    }

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

    // --- Haid Records ---
    suspend fun uploadSingleHaidRecord(context: Context, record: HaidRecord) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.upsertHaidRecords(listOf(SupabaseHaidRecordDto.fromDomain(record)))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleHaidRecord ke Supabase", e)
            }
        }

    suspend fun deleteRemoteHaidRecord(context: Context, recordId: Long) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.deleteHaidRecord("eq.$recordId")
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteHaidRecord ke Supabase", e)
            }
        }

    // --- Fasting Sessions & Records ---
    suspend fun uploadSingleFastingSession(context: Context, session: FastingSession) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.upsertFastingSessions(listOf(SupabaseFastingSessionDto.fromDomain(session)))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleFastingSession ke Supabase", e)
            }
        }

    suspend fun deleteRemoteFastingSession(context: Context, sessionId: Long) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.deleteFastingRecordsBySession("eq.$sessionId")
                service.deleteFastingSession("eq.$sessionId")
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteFastingSession ke Supabase", e)
            }
        }

    suspend fun uploadSingleFastingRecord(context: Context, record: FastingRecord) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.upsertFastingRecords(listOf(SupabaseFastingRecordDto.fromDomain(record)))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleFastingRecord ke Supabase", e)
            }
        }

    suspend fun deleteRemoteFastingRecord(context: Context, recordId: Long) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.deleteFastingRecord("eq.$recordId")
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteFastingRecord ke Supabase", e)
            }
        }

    // --- Wali Accounts ---
    suspend fun uploadWaliAccounts(context: Context, accounts: List<WaliAccount>) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context) || accounts.isEmpty()) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                accounts.chunked(50).forEach { batch ->
                    service.upsertWaliAccounts(batch.map { SupabaseWaliAccountDto.fromDomain(it) })
                }
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadWaliAccounts ke Supabase", e)
            }
        }

    suspend fun deleteRemoteWaliAccount(context: Context, accountId: Long) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.deleteWaliAccount("eq.$accountId")
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteWaliAccount ke Supabase", e)
            }
        }

    suspend fun deleteWaliAccountsBySchool(context: Context, schoolId: String) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.deleteWaliAccountsBySchool("eq.$schoolId")
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteWaliAccountsBySchool ke Supabase", e)
            }
        }

    // --- Shift Messages ---
    suspend fun uploadSingleShiftMessage(context: Context, message: ShiftMessage) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.upsertShiftMessages(listOf(SupabaseShiftMessageDto.fromDomain(message)))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleShiftMessage ke Supabase", e)
            }
        }

    suspend fun deleteRemoteShiftMessage(context: Context, messageId: Long) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.deleteShiftMessage("eq.$messageId")
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteShiftMessage ke Supabase", e)
            }
        }

    suspend fun deleteShiftMessagesBySchool(context: Context, schoolId: String) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.deleteShiftMessagesBySchool("eq.$schoolId")
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteShiftMessagesBySchool ke Supabase", e)
            }
        }

    // --- School Registrations ---
    suspend fun uploadSingleSchoolRegistration(context: Context, reg: SchoolRegistration) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.upsertSchoolRegistrations(listOf(SupabaseSchoolRegistrationDto.fromDomain(reg)))
            } catch (e: Exception) {
                Log.e(TAG, "Silent error uploadSingleSchoolRegistration ke Supabase", e)
            }
        }

    suspend fun deleteRemoteSchoolRegistration(context: Context, regId: Long) =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext
            try {
                val service = SupabaseClient.getService(context) ?: return@withContext
                service.deleteSchoolRegistration("eq.$regId")
            } catch (e: Exception) {
                Log.e(TAG, "Silent error deleteRemoteSchoolRegistration ke Supabase", e)
            }
        }

    /**
     * Menarik daftar pendaftaran sekolah & akun wali dari Supabase saat login lintas perangkat.
     */
    suspend fun syncAuthCatalogFromCloud(
        context: Context,
        repository: HealthRepository
    ) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured(context)) return@withContext
        val service = SupabaseClient.getService(context) ?: return@withContext

        try {
            val regResp = service.getAllSchoolRegistrations()
            if (regResp.isSuccessful) {
                val remoteRegs = (regResp.body() ?: emptyList()).map { it.toDomainModel() }
                if (remoteRegs.isNotEmpty()) {
                    repository.insertAllSchoolRegistrations(remoteRegs)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Silent error sync school_registrations", e)
        }

        try {
            val waliResp = service.getAllWaliAccounts()
            if (waliResp.isSuccessful) {
                val remoteWali = (waliResp.body() ?: emptyList()).map { it.toDomainModel() }
                if (remoteWali.isNotEmpty()) {
                    repository.insertAllWaliAccounts(remoteWali)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Silent error sync wali_accounts", e)
        }
    }

    /**
     * Sinkronisasi penuh seluruh data satu sekolah (Siswa, Akun Wali, Rekam Medis, Haid, Pesan Shift)
     * agar data yang diunggah Admin langsung tersedia di seluruh fitur pada semua perangkat.
     */
    suspend fun syncAllSchoolData(
        context: Context,
        schoolId: String,
        repository: HealthRepository
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured(context)) {
            return@withContext Result.failure(Exception("Supabase belum terkonfigurasi."))
        }
        val service = SupabaseClient.getService(context)
            ?: return@withContext Result.failure(Exception("Gagal membuat layanan Supabase."))

        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }

        try {
            // 1. Sinkronkan Data Siswa (child_students)
            var studentCount = 0
            val localStudents = repository.getStudentsBySchoolList(cleanSchoolId)
            val remoteStudentResp = service.getStudentsBySchool("eq.$cleanSchoolId")
            if (remoteStudentResp.isSuccessful) {
                val remoteStudents = (remoteStudentResp.body() ?: emptyList())
                    .map { it.toDomainModel(cleanSchoolId) }
                if (remoteStudents.isNotEmpty()) {
                    repository.insertAllStudents(remoteStudents)
                    val remoteIds = remoteStudents.map { it.id }.toSet()
                    val staleLocalIds = localStudents.filter { it.id !in remoteIds }.map { it.id }
                    if (staleLocalIds.isNotEmpty()) {
                        repository.deleteStudentsByIds(staleLocalIds)
                    }
                    studentCount = remoteStudents.size
                } else if (localStudents.isNotEmpty()) {
                    localStudents.chunked(50).forEach { batch ->
                        service.upsertStudents(batch.map { SupabaseStudentDto.fromDomain(it) })
                    }
                    studentCount = localStudents.size
                }
            }

            // Ambil ID siswa terbaru di Room agar FK medical_records aman 100%
            val syncedStudents = repository.getStudentsBySchoolList(cleanSchoolId)
            val validStudentIds = syncedStudents.map { it.id }.toSet()
            if (studentCount == 0) studentCount = syncedStudents.size

            // 2. Sinkronkan Akun Wali Asuh (wali_accounts)
            val localWali = repository.getWaliAccountsBySchoolList(cleanSchoolId)
            try {
                val remoteWaliResp = service.getWaliAccountsBySchool("eq.$cleanSchoolId")
                if (remoteWaliResp.isSuccessful) {
                    val remoteWali = (remoteWaliResp.body() ?: emptyList())
                        .map { it.toDomainModel(cleanSchoolId) }
                    if (remoteWali.isNotEmpty()) {
                        repository.insertAllWaliAccounts(remoteWali)
                    } else if (localWali.isNotEmpty()) {
                        localWali.chunked(50).forEach { batch ->
                            service.upsertWaliAccounts(batch.map { SupabaseWaliAccountDto.fromDomain(it) })
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Skip sync wali_accounts: ${e.message}")
            }

            // 3. Sinkronkan Rekam Medis (medical_records)
            var recordCount = 0
            try {
                val localRecords = repository.getAllRecordsList().filter { it.studentId in validStudentIds }
                if (localRecords.isNotEmpty()) {
                    localRecords.chunked(50).forEach { batch ->
                        upsertMedicalRecordsWithFallback(service, batch, cleanSchoolId)
                    }
                }
                val remoteRecResp = service.getMedicalRecordsBySchool("eq.$cleanSchoolId")
                if (remoteRecResp.isSuccessful) {
                    val remoteRecords = (remoteRecResp.body() ?: emptyList())
                        .map { it.toDomainModel() }
                        .filter { it.studentId in validStudentIds }
                    if (remoteRecords.isNotEmpty()) {
                        repository.insertAllRecords(remoteRecords)
                    }
                    recordCount = remoteRecords.size.coerceAtLeast(localRecords.size)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Skip sync medical_records: ${e.message}")
            }

            // 4. Sinkronkan Pendataan Haid (haid_records)
            var haidCount = 0
            try {
                val localHaid = repository.getHaidRecordsBySchoolList(cleanSchoolId)
                if (localHaid.isNotEmpty()) {
                    localHaid.chunked(50).forEach { batch ->
                        service.upsertHaidRecords(batch.map { SupabaseHaidRecordDto.fromDomain(it) })
                    }
                }
                val remoteHaidResp = service.getHaidRecordsBySchool("eq.$cleanSchoolId")
                if (remoteHaidResp.isSuccessful) {
                    val remoteHaid = (remoteHaidResp.body() ?: emptyList())
                        .map { it.toDomainModel(cleanSchoolId) }
                    if (remoteHaid.isNotEmpty()) {
                        repository.insertAllHaidRecords(remoteHaid)
                    }
                    haidCount = remoteHaid.size.coerceAtLeast(localHaid.size)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Skip sync haid_records: ${e.message}")
            }

            // 5. Sinkronkan Pesan Shift (shift_messages)
            try {
                val localShift = repository.getShiftMessagesBySchoolList(cleanSchoolId)
                if (localShift.isNotEmpty()) {
                    localShift.chunked(50).forEach { batch ->
                        service.upsertShiftMessages(batch.map { SupabaseShiftMessageDto.fromDomain(it) })
                    }
                }
                val remoteShiftResp = service.getShiftMessagesBySchool("eq.$cleanSchoolId")
                if (remoteShiftResp.isSuccessful) {
                    val remoteShift = (remoteShiftResp.body() ?: emptyList())
                        .map { it.toDomainModel(cleanSchoolId) }
                    if (remoteShift.isNotEmpty()) {
                        repository.insertAllShiftMessages(remoteShift)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Skip sync shift_messages: ${e.message}")
            }

            // 6. Sinkronkan Sesi & Data Puasa (fasting_sessions & fasting_records)
            try {
                val localFastingSessions = repository.getFastingSessionsBySchoolList(cleanSchoolId)
                if (localFastingSessions.isNotEmpty()) {
                    localFastingSessions.chunked(50).forEach { batch ->
                        service.upsertFastingSessions(batch.map { SupabaseFastingSessionDto.fromDomain(it) })
                    }
                }
                val remoteFSResp = service.getFastingSessionsBySchool("eq.$cleanSchoolId")
                if (remoteFSResp.isSuccessful) {
                    val remoteFS = (remoteFSResp.body() ?: emptyList())
                        .map { it.toDomainModel(cleanSchoolId) }
                    if (remoteFS.isNotEmpty()) {
                        repository.insertAllFastingSessions(remoteFS)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Skip sync fasting_sessions: ${e.message}")
            }

            try {
                val localFastingRecords = repository.getFastingRecordsBySchoolList(cleanSchoolId)
                if (localFastingRecords.isNotEmpty()) {
                    localFastingRecords.chunked(50).forEach { batch ->
                        service.upsertFastingRecords(batch.map { SupabaseFastingRecordDto.fromDomain(it) })
                    }
                }
                val remoteFRResp = service.getFastingRecordsBySchool("eq.$cleanSchoolId")
                if (remoteFRResp.isSuccessful) {
                    val remoteFR = (remoteFRResp.body() ?: emptyList())
                        .map { it.toDomainModel(cleanSchoolId) }
                    if (remoteFR.isNotEmpty()) {
                        repository.insertAllFastingRecords(remoteFR)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Skip sync fasting_records: ${e.message}")
            }

            SupabaseConfig.setLastSyncTime(context, System.currentTimeMillis())
            Result.success("Sinkronisasi lengkap! $studentCount siswa, $recordCount rekam medis, & $haidCount data haid terhubung.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncAllSchoolData", e)
            Result.failure(e)
        }
    }
}
