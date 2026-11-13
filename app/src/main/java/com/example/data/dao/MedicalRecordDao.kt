package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MedicalRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalRecordDao {
    @Query("SELECT * FROM medical_records ORDER BY planDate DESC, createdAt DESC")
    fun getAllRecords(): Flow<List<MedicalRecord>>

    @Query("SELECT * FROM medical_records WHERE status IN ('PLANNED', 'DEPARTED') ORDER BY planDate ASC")
    fun getActivePlans(): Flow<List<MedicalRecord>>

    @Query("SELECT * FROM medical_records WHERE status = 'COMPLETED' ORDER BY treatmentDate DESC, createdAt DESC")
    fun getCompletedRecords(): Flow<List<MedicalRecord>>

    @Query("SELECT * FROM medical_records WHERE needsControl = 1 AND isControlCompleted = 0 ORDER BY controlDate ASC")
    fun getPendingControlRecords(): Flow<List<MedicalRecord>>

    @Query("SELECT * FROM medical_records WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getRecordsForStudent(studentId: Long): Flow<List<MedicalRecord>>

    @Query("SELECT * FROM medical_records WHERE id = :id")
    fun getRecordById(id: Long): Flow<MedicalRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MedicalRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MedicalRecord>)

    @Update
    suspend fun update(record: MedicalRecord)

    @Delete
    suspend fun delete(record: MedicalRecord)

    @Query("UPDATE medical_records SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE medical_records SET status = :status, faskes = :faskes, escortName = :escortName WHERE id = :id")
    suspend fun updateDepartureWithFaskes(id: Long, status: String, faskes: String, escortName: String)

    @Query("UPDATE medical_records SET isControlCompleted = :isCompleted WHERE id = :id")
    suspend fun updateControlCompleted(id: Long, isCompleted: Boolean)

    @Query("SELECT * FROM medical_records ORDER BY planDate DESC, createdAt DESC")
    suspend fun getAllRecordsList(): List<MedicalRecord>

    @Query("SELECT COUNT(*) FROM medical_records")
    suspend fun count(): Int

    @Query("DELETE FROM medical_records")
    suspend fun deleteAll()
}
