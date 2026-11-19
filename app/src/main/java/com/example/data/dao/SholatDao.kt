package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SholatRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SholatRecordDao {
    @Query("SELECT * FROM sholat_records ORDER BY dateKey DESC, updatedAt DESC")
    fun getAllRecords(): Flow<List<SholatRecord>>

    @Query("SELECT * FROM sholat_records ORDER BY dateKey DESC, updatedAt DESC")
    suspend fun getAllRecordsList(): List<SholatRecord>

    @Query("SELECT * FROM sholat_records WHERE schoolId = :schoolId ORDER BY dateKey DESC, updatedAt DESC")
    suspend fun getRecordsBySchoolList(schoolId: String): List<SholatRecord>

    @Query("SELECT * FROM sholat_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SholatRecord?

    @Query("SELECT * FROM sholat_records WHERE studentId = :studentId AND dateKey = :dateKey AND waktu = :waktu LIMIT 1")
    suspend fun getByStudentDateWaktu(studentId: Long, dateKey: String, waktu: String): SholatRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SholatRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<SholatRecord>)

    @Update
    suspend fun update(record: SholatRecord)

    @Delete
    suspend fun delete(record: SholatRecord)

    @Query("DELETE FROM sholat_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sholat_records WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}
