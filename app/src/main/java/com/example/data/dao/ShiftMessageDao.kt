package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ShiftMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftMessageDao {
    @Query("SELECT * FROM shift_messages ORDER BY isCompleted ASC, followUpDate ASC, createdAt DESC")
    fun getAllMessages(): Flow<List<ShiftMessage>>

    @Query("SELECT * FROM shift_messages WHERE schoolId = :schoolId ORDER BY isCompleted ASC, followUpDate ASC, createdAt DESC")
    fun getMessagesBySchool(schoolId: String): Flow<List<ShiftMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ShiftMessage): Long

    @Update
    suspend fun update(message: ShiftMessage)

    @Delete
    suspend fun delete(message: ShiftMessage)

    @Query("UPDATE shift_messages SET isCompleted = :isCompleted, completedBy = :completedBy, completedAt = :completedAt WHERE id = :id")
    suspend fun updateCompleted(id: Long, isCompleted: Boolean, completedBy: String, completedAt: Long)

    @Query("DELETE FROM shift_messages WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)

    @Query("DELETE FROM shift_messages")
    suspend fun deleteAll()
}
