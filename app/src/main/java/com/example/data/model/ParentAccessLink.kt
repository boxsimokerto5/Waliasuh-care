package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parent_access_links")
data class ParentAccessLink(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val parentName: String = "",
    val parentPhone: String = "",
    val username: String,
    val password: String,
    val assignedWaliUsername: String = "",
    val assignedWaliName: String = "",
    val canViewMengaji: Boolean = true,
    val canViewMedical: Boolean = true,
    val canViewFasting: Boolean = true,
    val canViewHaid: Boolean = true,
    val canViewProfile: Boolean = true,
    val canViewTabungan: Boolean = false,
    val schoolId: String = "SRT1-KDR",
    val deletionRequested: Boolean = false,
    val deletionReason: String = "",
    val deletionRequestedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
