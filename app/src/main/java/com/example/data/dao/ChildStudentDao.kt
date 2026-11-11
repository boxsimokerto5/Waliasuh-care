package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChildStudent
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildStudentDao {
    @Query("SELECT * FROM child_students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<ChildStudent>>

    @Query("SELECT * FROM child_students WHERE id = :id")
    fun getStudentById(id: Long): Flow<ChildStudent?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: ChildStudent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<ChildStudent>)

    @Update
    suspend fun update(student: ChildStudent)

    @Delete
    suspend fun delete(student: ChildStudent)

    @Query("SELECT COUNT(*) FROM child_students")
    suspend fun count(): Int

    @Query("SELECT * FROM child_students WHERE schoolId = :schoolId ORDER BY name ASC")
    fun getStudentsBySchool(schoolId: String): Flow<List<ChildStudent>>

    @Query("DELETE FROM child_students WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)

    @Query("DELETE FROM child_students")
    suspend fun deleteAll()
}
