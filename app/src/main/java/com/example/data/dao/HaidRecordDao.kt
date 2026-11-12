package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.HaidRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HaidRecordDao {
    @Query("SELECT * FROM haid_records ORDER BY startDate DESC")
    fun getAllRecords(): Flow<List<HaidRecord>>

    @Query("SELECT * FROM haid_records WHERE endDate IS NULL OR status = 'HAID' ORDER BY startDate DESC")
    fun getActiveHaidRecords(): Flow<List<HaidRecord>>

    @Query("SELECT * FROM haid_records WHERE endDate IS NOT NULL AND status != 'HAID' ORDER BY endDate DESC")
    fun getSuciRecords(): Flow<List<HaidRecord>>

    @Query("SELECT * FROM haid_records WHERE studentId = :studentId ORDER BY startDate DESC")
    fun getRecordsByStudentId(studentId: Long): Flow<List<HaidRecord>>

    @Query("SELECT * FROM haid_records WHERE studentId = :studentId AND (endDate IS NULL OR status = 'HAID') LIMIT 1")
    suspend fun getActiveRecordForStudent(studentId: Long): HaidRecord?

    @Query("SELECT * FROM haid_records WHERE studentNik = :nik AND (endDate IS NULL OR status = 'HAID') LIMIT 1")
    suspend fun getActiveRecordForNik(nik: String): HaidRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HaidRecord): Long

    @Update
    suspend fun update(record: HaidRecord)

    @Delete
    suspend fun delete(record: HaidRecord)

    @Query("DELETE FROM haid_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE haid_records SET endDate = :endDate, status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun completeHaid(id: Long, endDate: Long, status: String = "SUCI", updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM haid_records WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)

    @Query("DELETE FROM haid_records")
    suspend fun deleteAll()
}
