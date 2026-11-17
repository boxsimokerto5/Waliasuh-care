package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parent_expectations")
data class ParentExpectation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String = "",
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val parentName: String = "",
    val waliUsername: String = "",
    val waliName: String = "",
    val characterFocusCsv: String = "",
    val characterExpectation: String = "",
    val adviceFocusNotes: String = "",
    val parentPrayerMessage: String = "",
    val isReadByWali: Boolean = false,
    val waliFollowUpNote: String = "",
    val waliRespondedAt: Long = 0L,
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class CharacterFocusOption(
    val key: String,
    val labelId: String,
    val labelEn: String
)

object ParentExpectationHelper {
    val FOCUS_OPTIONS = listOf(
        CharacterFocusOption(
            key = "Kejujuran & Amanah",
            labelId = "Kejujuran & Amanah",
            labelEn = "Honesty & Trustworthiness"
        ),
        CharacterFocusOption(
            key = "Kemandirian",
            labelId = "Kemandirian",
            labelEn = "Independence"
        ),
        CharacterFocusOption(
            key = "Disiplin & Tanggung Jawab",
            labelId = "Disiplin & Tanggung Jawab",
            labelEn = "Discipline & Responsibility"
        ),
        CharacterFocusOption(
            key = "Ibadah & Akhlak Mulia",
            labelId = "Ibadah & Akhlak Mulia",
            labelEn = "Worship & Noble Manners"
        ),
        CharacterFocusOption(
            key = "Sopan Santun & Adab",
            labelId = "Sopan Santun & Adab",
            labelEn = "Politeness & Respect"
        ),
        CharacterFocusOption(
            key = "Semangat Belajar",
            labelId = "Semangat Belajar",
            labelEn = "Love of Learning"
        ),
        CharacterFocusOption(
            key = "Pengendalian Emosi",
            labelId = "Pengendalian Emosi",
            labelEn = "Emotional Self-Control"
        )
    )

    fun parseFocusList(csv: String): List<String> {
        return csv.split("|", ",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun formatFocusCsv(tags: Collection<String>): String {
        return tags.map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString("|")
    }

    fun displayFocusLabel(key: String, isEn: Boolean): String {
        val clean = key.trim()
        val match = FOCUS_OPTIONS.firstOrNull { it.key.equals(clean, ignoreCase = true) }
        return if (match != null) {
            if (isEn) match.labelEn else match.labelId
        } else {
            clean
        }
    }

    fun matchesStudent(expectation: ParentExpectation, student: ChildStudent): Boolean {
        if (expectation.studentId > 0L && student.id > 0L && expectation.studentId == student.id) {
            return true
        }
        val cleanExpNis = expectation.studentNis.trim()
        val cleanStuNis = student.nis.trim()
        if (cleanExpNis.isNotBlank() && cleanStuNis.isNotBlank() && cleanExpNis.equals(cleanStuNis, ignoreCase = true)) {
            return true
        }
        return expectation.studentName.isNotBlank() &&
            expectation.studentName.equals(student.name, ignoreCase = true)
    }
}
