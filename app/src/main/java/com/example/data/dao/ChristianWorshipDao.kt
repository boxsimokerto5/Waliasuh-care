package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChristianWorshipLocalSave
import com.example.data.model.ChristianWorshipRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ChristianWorshipDao {
    @Query("SELECT * FROM christian_worship_records WHERE isDeleted = 0 ORDER BY activityDate DESC, createdAt DESC")
    fun getAllRecords(): Flow<List<ChristianWorshipRecord>>

    @Query("SELECT * FROM christian_worship_records ORDER BY activityDate DESC, createdAt DESC")
    suspend fun getAllRecordsIncludingDeletedList(): List<ChristianWorshipRecord>

    @Query("SELECT * FROM christian_worship_records WHERE schoolId = :schoolId AND isDeleted = 0 ORDER BY activityDate DESC, createdAt DESC")
    suspend fun getRecordsBySchoolList(schoolId: String): List<ChristianWorshipRecord>

    @Query("SELECT * FROM christian_worship_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): ChristianWorshipRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ChristianWorshipRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRecords(records: List<ChristianWorshipRecord>)

    @Update
    suspend fun updateRecord(record: ChristianWorshipRecord)

    @Delete
    suspend fun deleteRecord(record: ChristianWorshipRecord)

    @Query("DELETE FROM christian_worship_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM christian_worship_records WHERE schoolId = :schoolId")
    suspend fun deleteBySchool(schoolId: String)
}

@Dao
interface ChristianWorshipLocalSaveDao {
    @Query("SELECT * FROM christian_worship_local_saves ORDER BY savedAt DESC")
    fun getAllLocalSaves(): Flow<List<ChristianWorshipLocalSave>>

    @Query("SELECT * FROM christian_worship_local_saves WHERE recordId = :recordId LIMIT 1")
    suspend fun getByRecordId(recordId: Long): ChristianWorshipLocalSave?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalSave(save: ChristianWorshipLocalSave)

    @Query("DELETE FROM christian_worship_local_saves WHERE recordId = :recordId")
    suspend fun deleteByRecordId(recordId: Long)
}
