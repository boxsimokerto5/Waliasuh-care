package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TabunganLog
import com.example.data.model.TabunganTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TabunganTransactionDao {
    @Query("SELECT * FROM tabungan_transactions ORDER BY transactionDate DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<TabunganTransaction>>

    @Query("SELECT * FROM tabungan_transactions ORDER BY transactionDate DESC, createdAt DESC")
    suspend fun getAllTransactionsList(): List<TabunganTransaction>

    @Query("SELECT * FROM tabungan_transactions WHERE schoolId = :schoolId ORDER BY transactionDate DESC, createdAt DESC")
    suspend fun getTransactionsBySchoolList(schoolId: String): List<TabunganTransaction>

    @Query("SELECT * FROM tabungan_transactions WHERE studentId = :studentId AND schoolId = :schoolId ORDER BY transactionDate DESC, createdAt DESC")
    suspend fun getTransactionsForStudentList(studentId: Long, schoolId: String): List<TabunganTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TabunganTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TabunganTransaction>)

    @Update
    suspend fun update(transaction: TabunganTransaction)

    @Delete
    suspend fun delete(transaction: TabunganTransaction)

    @Query("DELETE FROM tabungan_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tabungan_transactions WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}

@Dao
interface TabunganLogDao {
    @Query("SELECT * FROM tabungan_logs ORDER BY createdAt DESC")
    fun getAllLogs(): Flow<List<TabunganLog>>

    @Query("SELECT * FROM tabungan_logs ORDER BY createdAt DESC")
    suspend fun getAllLogsList(): List<TabunganLog>

    @Query("SELECT * FROM tabungan_logs WHERE schoolId = :schoolId ORDER BY createdAt DESC")
    suspend fun getLogsBySchoolList(schoolId: String): List<TabunganLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: TabunganLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<TabunganLog>)

    @Query("DELETE FROM tabungan_logs WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}
