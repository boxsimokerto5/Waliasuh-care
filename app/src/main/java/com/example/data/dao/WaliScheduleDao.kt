package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliShiftCode
import kotlinx.coroutines.flow.Flow

@Dao
interface WaliShiftCodeDao {
    @Query("SELECT * FROM wali_shift_codes ORDER BY sortOrder ASC, code ASC")
    fun getAllShiftCodes(): Flow<List<WaliShiftCode>>

    @Query("SELECT * FROM wali_shift_codes WHERE schoolId = :schoolId ORDER BY sortOrder ASC, code ASC")
    suspend fun getShiftCodesBySchoolList(schoolId: String): List<WaliShiftCode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shiftCode: WaliShiftCode): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shiftCodes: List<WaliShiftCode>)

    @Update
    suspend fun update(shiftCode: WaliShiftCode)

    @Delete
    suspend fun delete(shiftCode: WaliShiftCode)

    @Query("DELETE FROM wali_shift_codes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM wali_shift_codes WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}

@Dao
interface WaliScheduleEntryDao {
    @Query("SELECT * FROM wali_schedule_entries ORDER BY monthKey DESC, sortOrder ASC, waliName ASC")
    fun getAllScheduleEntries(): Flow<List<WaliScheduleEntry>>

    @Query("SELECT * FROM wali_schedule_entries WHERE schoolId = :schoolId ORDER BY monthKey DESC, sortOrder ASC, waliName ASC")
    suspend fun getEntriesBySchoolList(schoolId: String): List<WaliScheduleEntry>

    @Query("SELECT * FROM wali_schedule_entries WHERE schoolId = :schoolId AND monthKey = :monthKey ORDER BY sortOrder ASC, waliName ASC")
    suspend fun getEntriesBySchoolAndMonthList(schoolId: String, monthKey: String): List<WaliScheduleEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WaliScheduleEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WaliScheduleEntry>)

    @Update
    suspend fun update(entry: WaliScheduleEntry)

    @Delete
    suspend fun delete(entry: WaliScheduleEntry)

    @Query("DELETE FROM wali_schedule_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM wali_schedule_entries WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)

    @Query("DELETE FROM wali_schedule_entries WHERE schoolId = :schoolId AND monthKey = :monthKey")
    suspend fun deleteBySchoolAndMonth(schoolId: String, monthKey: String)
}
