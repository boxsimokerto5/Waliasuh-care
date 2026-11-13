package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FastingSessionDao {
    @Query("SELECT * FROM fasting_sessions ORDER BY sessionDate DESC, createdAt DESC")
    fun getAllSessions(): Flow<List<FastingSession>>

    @Query("SELECT * FROM fasting_sessions WHERE schoolId = :schoolId ORDER BY sessionDate DESC, createdAt DESC")
    suspend fun getSessionsBySchoolList(schoolId: String): List<FastingSession>

    @Query("SELECT * FROM fasting_sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FastingSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FastingSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<FastingSession>)

    @Update
    suspend fun update(session: FastingSession)

    @Delete
    suspend fun delete(session: FastingSession)

    @Query("DELETE FROM fasting_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM fasting_sessions WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}

@Dao
interface FastingRecordDao {
    @Query("SELECT * FROM fasting_records ORDER BY sessionDate DESC, createdAt DESC")
    fun getAllRecords(): Flow<List<FastingRecord>>

    @Query("SELECT * FROM fasting_records WHERE schoolId = :schoolId ORDER BY sessionDate DESC, createdAt DESC")
    suspend fun getRecordsBySchoolList(schoolId: String): List<FastingRecord>

    @Query("SELECT * FROM fasting_records WHERE sessionId = :sessionId ORDER BY studentName ASC")
    suspend fun getRecordsBySessionList(sessionId: Long): List<FastingRecord>

    @Query("SELECT * FROM fasting_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FastingRecord?

    @Query("SELECT * FROM fasting_records WHERE sessionId = :sessionId AND studentId = :studentId LIMIT 1")
    suspend fun getRecordInSessionForStudent(sessionId: Long, studentId: Long): FastingRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: FastingRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<FastingRecord>)

    @Update
    suspend fun update(record: FastingRecord)

    @Delete
    suspend fun delete(record: FastingRecord)

    @Query("DELETE FROM fasting_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM fasting_records WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: Long)

    @Query("DELETE FROM fasting_records WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}
