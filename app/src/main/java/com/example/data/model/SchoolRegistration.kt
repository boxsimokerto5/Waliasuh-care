package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_registrations")
data class SchoolRegistration(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val schoolName: String,
    val address: String,
    val city: String,
    val estimatedWaliCount: Int,
    val picName: String,
    val picEmail: String,
    val picPhone: String,
    val submissionDate: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val rejectionReason: String = "",
    val generatedSchoolCode: String = "",
    val generatedAdminUsername: String = "",
    val generatedAdminPassword: String = "",
    val approvedDate: Long = 0L
)
