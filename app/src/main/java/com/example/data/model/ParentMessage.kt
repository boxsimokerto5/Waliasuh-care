package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parent_messages")
data class ParentMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String = "",
    val parentName: String = "",
    val waliUsername: String = "",
    val waliName: String = "",
    val senderRole: String = "WALI", // "WALI" or "PARENT"
    val senderName: String = "",
    val messageContent: String,
    val isReadByWali: Boolean = false,
    val isReadByParent: Boolean = false,
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis()
)
