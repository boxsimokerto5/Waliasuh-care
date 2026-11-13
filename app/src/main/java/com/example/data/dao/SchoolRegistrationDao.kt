package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SchoolRegistration
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolRegistrationDao {
    @Query("SELECT * FROM school_registrations ORDER BY submissionDate DESC")
    fun getAllRegistrations(): Flow<List<SchoolRegistration>>

    @Query("SELECT * FROM school_registrations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SchoolRegistration?

    @Query("SELECT * FROM school_registrations WHERE generatedAdminUsername = :username LIMIT 1")
    suspend fun getByAdminUsername(username: String): SchoolRegistration?

    @Query("SELECT * FROM school_registrations WHERE generatedSchoolCode = :code LIMIT 1")
    suspend fun getBySchoolCode(code: String): SchoolRegistration?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registration: SchoolRegistration): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(registrations: List<SchoolRegistration>)

    @Update
    suspend fun update(registration: SchoolRegistration)

    @Delete
    suspend fun delete(registration: SchoolRegistration)

    @Query("DELETE FROM school_registrations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM school_registrations ORDER BY submissionDate DESC")
    suspend fun getAllRegistrationsList(): List<SchoolRegistration>

    @Query("SELECT COUNT(*) FROM school_registrations")
    suspend fun count(): Int
}
