package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ShiftHandoverReport
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftHandoverDao {
    @Query("SELECT * FROM shift_handovers WHERE isDeleted = 0 ORDER BY reportDateMillis DESC, updatedAt DESC, createdAt DESC")
    fun getAllActiveReports(): Flow<List<ShiftHandoverReport>>

    @Query("SELECT * FROM shift_handovers ORDER BY reportDateMillis DESC, updatedAt DESC, createdAt DESC")
    suspend fun getAllReportsIncludingDeletedList(): List<ShiftHandoverReport>

    @Query("SELECT * FROM shift_handovers WHERE schoolId = :schoolId AND isDeleted = 0 ORDER BY reportDateMillis DESC, updatedAt DESC, createdAt DESC")
    suspend fun getReportsBySchoolList(schoolId: String): List<ShiftHandoverReport>

    @Query("SELECT * FROM shift_handovers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ShiftHandoverReport?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: ShiftHandoverReport): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<ShiftHandoverReport>)

    @Update
    suspend fun update(report: ShiftHandoverReport)

    @Query("DELETE FROM shift_handovers WHERE schoolId = :schoolId")
    suspend fun deleteBySchool(schoolId: String)
}
