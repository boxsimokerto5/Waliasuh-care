package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VisitConfirmation
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitSessionDao {
    @Query("SELECT * FROM visit_sessions ORDER BY isActive DESC, monthYear DESC, createdAt DESC")
    fun getAllSessions(): Flow<List<VisitSession>>

    @Query("SELECT * FROM visit_sessions WHERE schoolId = :schoolId ORDER BY isActive DESC, monthYear DESC, createdAt DESC")
    suspend fun getSessionsBySchoolList(schoolId: String): List<VisitSession>

    @Query("SELECT * FROM visit_sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): VisitSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: VisitSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<VisitSession>)

    @Update
    suspend fun update(session: VisitSession)

    @Delete
    suspend fun delete(session: VisitSession)

    @Query("DELETE FROM visit_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM visit_sessions WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}

@Dao
interface VisitRecordDao {
    @Query("SELECT * FROM visit_records ORDER BY checkedInAt DESC, createdAt DESC")
    fun getAllRecords(): Flow<List<VisitRecord>>

    @Query("SELECT * FROM visit_records ORDER BY checkedInAt DESC, createdAt DESC")
    suspend fun getAllRecordsList(): List<VisitRecord>

    @Query("SELECT * FROM visit_records WHERE schoolId = :schoolId ORDER BY checkedInAt DESC, createdAt DESC")
    suspend fun getRecordsBySchoolList(schoolId: String): List<VisitRecord>

    @Query("SELECT * FROM visit_records WHERE sessionId = :sessionId ORDER BY checkedInAt DESC, studentName ASC")
    suspend fun getRecordsBySessionList(sessionId: Long): List<VisitRecord>

    @Query("SELECT * FROM visit_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): VisitRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: VisitRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<VisitRecord>)

    @Update
    suspend fun update(record: VisitRecord)

    @Delete
    suspend fun delete(record: VisitRecord)

    @Query("DELETE FROM visit_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM visit_records WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: Long)

    @Query("DELETE FROM visit_records WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}

@Dao
interface VisitConfirmationDao {
    @Query("SELECT * FROM visit_confirmations ORDER BY updatedAt DESC, confirmedAt DESC")
    fun getAllConfirmations(): Flow<List<VisitConfirmation>>

    @Query("SELECT * FROM visit_confirmations WHERE schoolId = :schoolId ORDER BY updatedAt DESC, confirmedAt DESC")
    suspend fun getConfirmationsBySchoolList(schoolId: String): List<VisitConfirmation>

    @Query("SELECT * FROM visit_confirmations WHERE sessionId = :sessionId ORDER BY updatedAt DESC, studentName ASC")
    suspend fun getConfirmationsBySessionList(sessionId: Long): List<VisitConfirmation>

    @Query("SELECT * FROM visit_confirmations WHERE sessionId = :sessionId AND studentId = :studentId LIMIT 1")
    suspend fun getBySessionAndStudent(sessionId: Long, studentId: Long): VisitConfirmation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(confirmation: VisitConfirmation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(confirmations: List<VisitConfirmation>)

    @Update
    suspend fun update(confirmation: VisitConfirmation)

    @Delete
    suspend fun delete(confirmation: VisitConfirmation)

    @Query("DELETE FROM visit_confirmations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM visit_confirmations WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: Long)

    @Query("DELETE FROM visit_confirmations WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}
