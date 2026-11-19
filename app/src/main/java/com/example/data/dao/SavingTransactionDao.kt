package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SavingTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingTransactionDao {
    @Query("SELECT * FROM saving_transactions ORDER BY transactionDate DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<SavingTransaction>>

    @Query("SELECT * FROM saving_transactions WHERE schoolId = :schoolId ORDER BY transactionDate DESC, createdAt DESC")
    suspend fun getBySchoolList(schoolId: String): List<SavingTransaction>

    @Query("SELECT * FROM saving_transactions ORDER BY transactionDate DESC, createdAt DESC")
    suspend fun getAllList(): List<SavingTransaction>

    @Query("SELECT * FROM saving_transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SavingTransaction?

    @Query(
        """
        SELECT * FROM saving_transactions
        WHERE proofStatus = 'ACTIVE'
          AND proofExpiresAt IS NOT NULL
          AND proofExpiresAt <= :now
        """
    )
    suspend fun getExpiredProofTransactions(now: Long): List<SavingTransaction>

    @Query(
        """
        UPDATE saving_transactions
        SET proofPhotoLocalPath = '',
            proofPhotoRemoteFile = '',
            proofStatus = 'EXPIRED',
            updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun markProofExpired(id: Long, updatedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: SavingTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<SavingTransaction>)

    @Update
    suspend fun update(transaction: SavingTransaction)

    @Delete
    suspend fun delete(transaction: SavingTransaction)

    @Query("DELETE FROM saving_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saving_transactions WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}
