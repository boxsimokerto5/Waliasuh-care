package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_students")
data class ChildStudent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nis: String,
    val gender: String, // "L" (Laki-laki) atau "P" (Perempuan)
    val age: Int,
    val dormitory: String, // Asrama Putra / Asrama Putri
    val classGrade: String, // Kelas SD/SMP/SMA
    val allergies: String = "Tidak ada",
    val bloodType: String = "-",
    val emergencyContact: String = "Pengasuh Asrama",
    val specialNotes: String = "",
    val nik: String = "",
    val noKk: String = "",
    val birthPlace: String = "",
    val birthDate: String = "",
    val motherName: String = "",
    val address: String = "",
    val district: String = "",
    val roomNumber: String = "",
    val jenjang: String = "",
    val schoolId: String = "SRT1-KDR",
    val parentPhone: String = "",
    val assignedWaliUsername: String = "",
    val assignedWaliName: String = "",
    val photoUrl: String = "",
    val photoFallbackUrl: String = "",
    val religion: String = "" // "" / "ISLAM" (default) atau "KRISTEN"
)

object StudentReligionHelper {
    const val RELIGION_ISLAM = "ISLAM"
    const val RELIGION_KRISTEN = "KRISTEN"

    fun isChristian(raw: String?): Boolean {
        val clean = raw?.trim()?.uppercase().orEmpty()
        return clean == RELIGION_KRISTEN ||
            clean.contains("KRISTEN") ||
            clean == "PROTESTAN" ||
            clean == "KATOLIK" ||
            clean == "CHRISTIAN"
    }

    fun isChristian(student: ChildStudent?): Boolean = isChristian(student?.religion)

    fun isChristian(student: ChildStudent?, link: ParentAccessLink?): Boolean {
        return isChristian(student?.religion) || isChristian(link?.studentReligion)
    }

    fun normalize(raw: String?): String {
        val clean = raw?.trim().orEmpty()
        if (clean.isBlank()) return ""
        return if (isChristian(clean)) RELIGION_KRISTEN else RELIGION_ISLAM
    }
}

