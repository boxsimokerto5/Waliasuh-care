package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DeletionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DeletionLogDao {
    @Query("SELECT * FROM deletion_logs ORDER BY createdAt DESC")
    fun getAllLogs(): Flow<List<DeletionLog>>

    @Query("SELECT * FROM deletion_logs WHERE schoolId = :schoolId ORDER BY createdAt DESC")
    suspend fun getLogsBySchoolList(schoolId: String): List<DeletionLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DeletionLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<DeletionLog>)

    @Query("DELETE FROM deletion_logs WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)

    @Query("DELETE FROM deletion_logs WHERE createdAt < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long)
}
