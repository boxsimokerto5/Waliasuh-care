package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "haid_records")
data class HaidRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long = 0,
    val studentName: String,
    val studentNik: String = "",
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val startDate: Long,
    val endDate: Long? = null,
    val status: String = "HAID", // "HAID", "SUCI", "ISTIHADHAH"
    val symptoms: String = "Normal", // "Nyeri Haid (Dismenore)", "Kram Perut", "Pusing/Mual", "Sakit Pinggang", "Normal"
    val notes: String = "",
    val waliName: String = "Wali Asuh",
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isActive: Boolean
        get() = endDate == null || status == "HAID"

    val durationDays: Int
        get() {
            val end = endDate ?: System.currentTimeMillis()
            val diff = end - startDate
            val days = (diff / (1000L * 60 * 60 * 24)).toInt() + 1
            return if (days < 1) 1 else days
        }

    val isIstihadhahWarning: Boolean
        get() = isActive && durationDays >= 15
}
