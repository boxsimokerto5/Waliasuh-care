package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deletion_logs")
data class DeletionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val studentId: Long = 0L,
    val studentName: String,
    val studentClass: String = "",
    val recordId: Long = 0L,
    val deletedSummary: String = "",
    val deletedBy: String = "Wali Asuh",
    val deletedByUsername: String = "",
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val CATEGORY_MENGAJI = "MENGAJI"
        const val CATEGORY_PUASA = "PUASA"
        const val CATEGORY_BEROBAT = "BEROBAT"
        const val CATEGORY_PARENT_MESSAGE = "PARENT_MESSAGE"
    }
}
