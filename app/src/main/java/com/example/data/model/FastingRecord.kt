package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fasting_sessions")
data class FastingSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val sessionDate: Long,
    val description: String = "",
    val isActive: Boolean = true,
    val createdBy: String = "Admin Sekolah",
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "fasting_records")
data class FastingRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val sessionTitle: String = "",
    val sessionDate: Long = 0L,
    val studentId: Long = 0L,
    val studentName: String,
    val studentNik: String = "",
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val studentJenjang: String = "",
    val isSahurChecked: Boolean = false,
    val sahurCheckedAt: Long? = null,
    val sahurCheckedBy: String = "",
    val isBukaChecked: Boolean = false,
    val bukaCheckedAt: Long? = null,
    val bukaCheckedBy: String = "",
    val waliName: String = "Wali Asuh",
    val notes: String = "",
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object FastingJenjangHelper {
    const val JENJANG_SD = "SD"
    const val JENJANG_SMP = "SMP"
    const val JENJANG_SMA = "SMA"
    const val JENJANG_LAINNYA = "Lainnya"

    fun resolveJenjang(rawJenjang: String, rawClassGrade: String): String {
        val j = rawJenjang.trim().uppercase()
        if (j.isNotBlank()) {
            when {
                j.contains("SMA") || j.contains("SMK") || j == "MA" || j.startsWith("MA ") -> return JENJANG_SMA
                j.contains("SMP") || j.contains("MTS") -> return JENJANG_SMP
                j == "SD" || j.startsWith("SD") || j == "MI" || j.startsWith("MI ") -> return JENJANG_SD
            }
        }

        val c = rawClassGrade.trim().uppercase()
        if (c.isNotBlank()) {
            when {
                c.contains("SMA") || c.contains("SMK") || c.startsWith("MA ") -> return JENJANG_SMA
                c.contains("SMP") || c.contains("MTS") -> return JENJANG_SMP
                c.contains("SD") || c.startsWith("MI ") -> return JENJANG_SD
            }

            // Cek angka Romawi kelas (urutkan dari yang terpanjang / X-XII dulu agar tidak ketukar IX/XI/XII)
            val tokens = c.split(Regex("[^A-Z0-9]+")).filter { it.isNotBlank() }
            for (token in tokens) {
                when (token) {
                    "XII", "XI", "X", "12", "11", "10" -> return JENJANG_SMA
                    "IX", "VIII", "VII", "9", "8", "7" -> return JENJANG_SMP
                    "VI", "V", "IV", "III", "II", "I", "6", "5", "4", "3", "2", "1" -> return JENJANG_SD
                }
            }
        }

        return JENJANG_LAINNYA
    }

    fun resolveJenjang(student: ChildStudent): String {
        return resolveJenjang(student.jenjang, student.classGrade)
    }

    fun resolveJenjang(record: FastingRecord): String {
        return resolveJenjang(record.studentJenjang, record.studentClass)
    }
}
