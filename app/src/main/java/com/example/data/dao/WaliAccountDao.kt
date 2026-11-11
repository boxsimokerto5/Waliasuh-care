package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WaliAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface WaliAccountDao {
    @Query("SELECT * FROM wali_accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<WaliAccount>>

    @Query("SELECT * FROM wali_accounts ORDER BY name ASC")
    suspend fun getAllAccountsList(): List<WaliAccount>

    @Query("""
        SELECT * FROM wali_accounts 
        WHERE LOWER(username) = LOWER(:username) 
           OR LOWER(REPLACE(username, ' ', '')) = LOWER(REPLACE(:username, ' ', '')) 
           OR LOWER(name) = LOWER(:username) 
           OR LOWER(REPLACE(name, ' ', '')) = LOWER(REPLACE(:username, ' ', '')) 
        LIMIT 1
    """)
    suspend fun getByUsername(username: String): WaliAccount?

    @Query("SELECT * FROM wali_accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WaliAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: WaliAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<WaliAccount>)

    @Update
    suspend fun update(account: WaliAccount)

    @Delete
    suspend fun delete(account: WaliAccount)

    @Query("SELECT * FROM wali_accounts WHERE schoolId = :schoolId ORDER BY name ASC")
    fun getAccountsBySchool(schoolId: String): Flow<List<WaliAccount>>

    @Query("DELETE FROM wali_accounts WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)

    @Query("DELETE FROM wali_accounts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM wali_accounts")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM wali_accounts")
    suspend fun count(): Int
}
