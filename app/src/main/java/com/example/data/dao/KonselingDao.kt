package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.KonselingLog
import com.example.data.model.KonselingRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface KonselingRecordDao {
    @Query("SELECT * FROM konseling_records ORDER BY sessionDate DESC, createdAt DESC")
    fun getAllRecords(): Flow<List<KonselingRecord>>

    @Query("SELECT * FROM konseling_records ORDER BY sessionDate DESC, createdAt DESC")
    suspend fun getAllRecordsList(): List<KonselingRecord>

    @Query("SELECT * FROM konseling_records WHERE schoolId = :schoolId ORDER BY sessionDate DESC, createdAt DESC")
    suspend fun getRecordsBySchoolList(schoolId: String): List<KonselingRecord>

    @Query("SELECT * FROM konseling_records WHERE studentId = :studentId AND schoolId = :schoolId ORDER BY sessionDate DESC, createdAt DESC")
    suspend fun getRecordsForStudentList(studentId: Long, schoolId: String): List<KonselingRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: KonselingRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<KonselingRecord>)

    @Update
    suspend fun update(record: KonselingRecord)

    @Delete
    suspend fun delete(record: KonselingRecord)

    @Query("DELETE FROM konseling_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM konseling_records WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}

@Dao
interface KonselingLogDao {
    @Query("SELECT * FROM konseling_logs ORDER BY createdAt DESC")
    fun getAllLogs(): Flow<List<KonselingLog>>

    @Query("SELECT * FROM konseling_logs ORDER BY createdAt DESC")
    suspend fun getAllLogsList(): List<KonselingLog>

    @Query("SELECT * FROM konseling_logs WHERE schoolId = :schoolId ORDER BY createdAt DESC")
    suspend fun getLogsBySchoolList(schoolId: String): List<KonselingLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: KonselingLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<KonselingLog>)

    @Query("DELETE FROM konseling_logs WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}
