package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shift_messages")
data class ShiftMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val followUpDate: Long,
    val shift: String = "Semua Shift",
    val targetWali: String = "Semua Wali Asuh",
    val messageContent: String,
    val priority: String = "BIASA", // URGENT, SEDANG, BIASA
    val senderName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val completedBy: String = "",
    val completedAt: Long = 0L,
    val schoolId: String = "SRT1-KDR"
)
