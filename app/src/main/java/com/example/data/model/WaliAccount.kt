package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wali_accounts")
data class WaliAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val username: String,
    val password: String,
    val dormitory: String = "Asrama Putra", // Asrama Putra / Asrama Putri / Umum
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
