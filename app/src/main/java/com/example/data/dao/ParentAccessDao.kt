package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentExpectation
import com.example.data.model.ParentMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ParentAccessLinkDao {
    @Query("SELECT * FROM parent_access_links ORDER BY studentName ASC")
    fun getAllLinks(): Flow<List<ParentAccessLink>>

    @Query("SELECT * FROM parent_access_links ORDER BY studentName ASC")
    suspend fun getAllLinksList(): List<ParentAccessLink>

    @Query("SELECT * FROM parent_access_links WHERE schoolId = :schoolId ORDER BY studentName ASC")
    suspend fun getLinksBySchoolList(schoolId: String): List<ParentAccessLink>

    @Query("SELECT * FROM parent_access_links WHERE studentId = :studentId AND schoolId = :schoolId LIMIT 1")
    suspend fun getByStudentId(studentId: Long, schoolId: String): ParentAccessLink?

    @Query("SELECT * FROM parent_access_links WHERE LOWER(TRIM(username)) = LOWER(TRIM(:username)) LIMIT 1")
    suspend fun getByUsername(username: String): ParentAccessLink?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: ParentAccessLink): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<ParentAccessLink>)

    @Update
    suspend fun update(link: ParentAccessLink)

    @Delete
    suspend fun delete(link: ParentAccessLink)

    @Query("DELETE FROM parent_access_links WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}

@Dao
interface ParentMessageDao {
    @Query("SELECT * FROM parent_messages ORDER BY createdAt ASC")
    fun getAllMessages(): Flow<List<ParentMessage>>

    @Query("SELECT * FROM parent_messages WHERE schoolId = :schoolId ORDER BY createdAt ASC")
    suspend fun getMessagesBySchoolList(schoolId: String): List<ParentMessage>

    @Query("SELECT * FROM parent_messages WHERE studentId = :studentId AND schoolId = :schoolId ORDER BY createdAt ASC")
    suspend fun getMessagesForStudentList(studentId: Long, schoolId: String): List<ParentMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ParentMessage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ParentMessage>)

    @Update
    suspend fun update(message: ParentMessage)

    @Delete
    suspend fun delete(message: ParentMessage)

    @Query("DELETE FROM parent_messages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE parent_messages SET isReadByWali = 1 WHERE studentId = :studentId AND schoolId = :schoolId AND isReadByWali = 0")
    suspend fun markReadByWali(studentId: Long, schoolId: String)

    @Query("UPDATE parent_messages SET isReadByParent = 1 WHERE studentId = :studentId AND schoolId = :schoolId AND isReadByParent = 0")
    suspend fun markReadByParent(studentId: Long, schoolId: String)

    @Query("DELETE FROM parent_messages WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}

@Dao
interface ParentExpectationDao {
    @Query("SELECT * FROM parent_expectations ORDER BY updatedAt DESC")
    fun getAllExpectations(): Flow<List<ParentExpectation>>

    @Query("SELECT * FROM parent_expectations WHERE schoolId = :schoolId ORDER BY updatedAt DESC")
    suspend fun getExpectationsBySchoolList(schoolId: String): List<ParentExpectation>

    @Query("SELECT * FROM parent_expectations WHERE studentId = :studentId AND schoolId = :schoolId ORDER BY updatedAt DESC")
    suspend fun getExpectationsForStudentList(studentId: Long, schoolId: String): List<ParentExpectation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expectation: ParentExpectation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expectations: List<ParentExpectation>)

    @Update
    suspend fun update(expectation: ParentExpectation)

    @Delete
    suspend fun delete(expectation: ParentExpectation)

    @Query("UPDATE parent_expectations SET isReadByWali = 1, updatedAt = :updatedAt WHERE studentId = :studentId AND schoolId = :schoolId AND isReadByWali = 0")
    suspend fun markReadByWali(studentId: Long, schoolId: String, updatedAt: Long)

    @Query("DELETE FROM parent_expectations WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}
