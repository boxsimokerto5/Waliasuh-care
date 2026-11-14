package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MengajiRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MengajiRecordDao {
    @Query("SELECT * FROM mengaji_records ORDER BY sessionDate DESC, createdAt DESC")
    fun getAllRecords(): Flow<List<MengajiRecord>>

    @Query("SELECT * FROM mengaji_records ORDER BY sessionDate DESC, createdAt DESC")
    suspend fun getAllRecordsList(): List<MengajiRecord>

    @Query("SELECT * FROM mengaji_records WHERE schoolId = :schoolId ORDER BY sessionDate DESC, createdAt DESC")
    suspend fun getRecordsBySchoolList(schoolId: String): List<MengajiRecord>

    @Query("SELECT * FROM mengaji_records WHERE studentId = :studentId ORDER BY sessionDate DESC, createdAt DESC")
    suspend fun getRecordsForStudentList(studentId: Long): List<MengajiRecord>

    @Query("SELECT * FROM mengaji_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MengajiRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MengajiRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MengajiRecord>)

    @Update
    suspend fun update(record: MengajiRecord)

    @Delete
    suspend fun delete(record: MengajiRecord)

    @Query("DELETE FROM mengaji_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM mengaji_records WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}
