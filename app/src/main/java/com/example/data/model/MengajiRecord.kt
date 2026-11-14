package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.util.DateUtils
import com.example.util.StudentCsvParser
import java.util.Locale

@Entity(tableName = "mengaji_records")
data class MengajiRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long = 0L,
    val studentName: String,
    val studentNik: String = "",
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val studentJenjang: String = "",
    val sessionDate: Long = System.currentTimeMillis(),
    val bookType: String = "Iqro'",
    val jilidOrJuz: String = "Jilid 1",
    val pageOrVerse: String = "Hal. 1",
    val startPage: Int? = 1,
    val endPage: Int? = null,
    val fluencyStatus: String = MengajiHelper.FLUENCY_LANCAR,
    val notes: String = "",
    val waliName: String = "Wali Asuh",
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class MengajiSuggestion(
    val bookType: String,
    val jilidOrJuz: String,
    val pageOrVerse: String,
    val startPage: Int?,
    val endPage: Int?,
    val isRepeatingLast: Boolean,
    val previousRecord: MengajiRecord?
)

object MengajiHelper {
    const val FLUENCY_LANCAR = "LANCAR"
    const val FLUENCY_TIDAK_LANCAR = "TIDAK_LANCAR"

    val BOOK_TYPES = listOf("Iqro'", "Qiroati", "Tilawati", "Yanbu'a", "Al-Qur'an", "Juz 'Amma")
    val JILID_OPTIONS = (1..6).map { "Jilid $it" }
    val JUZ_OPTIONS = (1..30).map { "Juz $it" }

    fun jilidOrJuzOptionsForBook(bookType: String): List<String> {
        return when {
            bookType.contains("Qur", ignoreCase = true) -> JUZ_OPTIONS
            bookType.contains("Juz", ignoreCase = true) -> listOf("Juz 30") + JUZ_OPTIONS.dropLast(1)
            else -> JILID_OPTIONS
        }
    }

    fun isFluent(status: String): Boolean {
        return !status.equals(FLUENCY_TIDAK_LANCAR, ignoreCase = true) &&
            !status.contains("tidak", ignoreCase = true)
    }

    fun fluencyLabel(status: String, isEnglish: Boolean = false): String {
        return if (isFluent(status)) {
            if (isEnglish) "Fluent" else "Lancar"
        } else {
            if (isEnglish) "Needs Practice" else "Tidak Lancar"
        }
    }

    fun matchesStudent(record: MengajiRecord, student: ChildStudent): Boolean {
        if (student.id > 0L && record.studentId == student.id) return true
        val studentNik = StudentCsvParser.cleanDigitsOnly(student.nik)
        val recordNik = StudentCsvParser.cleanDigitsOnly(record.studentNik)
        if (studentNik.isNotBlank() && recordNik.isNotBlank() && studentNik == recordNik) return true
        if (student.nis.isNotBlank() && record.studentNis.isNotBlank() &&
            record.studentNis.trim().equals(student.nis.trim(), ignoreCase = true)
        ) {
            return true
        }
        return false
    }

    fun recordsForStudent(records: List<MengajiRecord>, student: ChildStudent): List<MengajiRecord> {
        return records
            .filter { matchesStudent(it, student) }
            .sortedWith(compareByDescending<MengajiRecord> { it.sessionDate }.thenByDescending { it.createdAt })
    }

    fun latestRecordForStudent(records: List<MengajiRecord>, student: ChildStudent): MengajiRecord? {
        return recordsForStudent(records, student).firstOrNull()
    }

    fun extractEndingPageNumber(raw: String): Int? {
        val matches = Regex("\\d+").findAll(raw).mapNotNull { it.value.toIntOrNull() }.toList()
        return matches.lastOrNull()
    }

    fun extractStartingPageNumber(raw: String): Int? {
        val matches = Regex("\\d+").findAll(raw).mapNotNull { it.value.toIntOrNull() }.toList()
        return matches.firstOrNull()
    }

    fun formatPageLabel(page: Int): String = "Hal. ${page.coerceAtLeast(1)}"

    fun suggestNextEntry(latest: MengajiRecord?): MengajiSuggestion {
        if (latest == null) {
            return MengajiSuggestion(
                bookType = "Iqro'",
                jilidOrJuz = "Jilid 1",
                pageOrVerse = "Hal. 1",
                startPage = 1,
                endPage = null,
                isRepeatingLast = false,
                previousRecord = null
            )
        }

        val lastEndPage = latest.endPage
            ?: latest.startPage
            ?: extractEndingPageNumber(latest.pageOrVerse)

        val repeatSame = !isFluent(latest.fluencyStatus)
        if (repeatSame) {
            val repeatedPageLabel = latest.pageOrVerse.ifBlank {
                lastEndPage?.let { formatPageLabel(it) } ?: "Hal. 1"
            }
            return MengajiSuggestion(
                bookType = latest.bookType.ifBlank { "Iqro'" },
                jilidOrJuz = latest.jilidOrJuz.ifBlank { "Jilid 1" },
                pageOrVerse = repeatedPageLabel,
                startPage = latest.startPage ?: lastEndPage,
                endPage = latest.endPage,
                isRepeatingLast = true,
                previousRecord = latest
            )
        }

        val nextPage = lastEndPage?.let { (it + 1).coerceAtLeast(1) }
        val suggestedPageText = if (nextPage != null) {
            formatPageLabel(nextPage)
        } else {
            latest.pageOrVerse.ifBlank { "Hal. 1" }
        }

        return MengajiSuggestion(
            bookType = latest.bookType.ifBlank { "Iqro'" },
            jilidOrJuz = latest.jilidOrJuz.ifBlank { "Jilid 1" },
            pageOrVerse = suggestedPageText,
            startPage = nextPage ?: latest.startPage,
            endPage = null,
            isRepeatingLast = false,
            previousRecord = latest
        )
    }

    fun advanceJilidOrJuz(bookType: String, currentJilidOrJuz: String): Pair<String, String> {
        val num = Regex("\\d+").find(currentJilidOrJuz)?.value?.toIntOrNull()
        val isQuran = bookType.contains("Qur", ignoreCase = true) || currentJilidOrJuz.contains("Juz", ignoreCase = true)
        return if (!isQuran) {
            when {
                num == null -> bookType to "Jilid 2"
                num < 6 -> bookType to "Jilid ${num + 1}"
                else -> "Al-Qur'an" to "Juz 1"
            }
        } else {
            val nextJuz = if (num == null || num >= 30) 1 else num + 1
            "Al-Qur'an" to "Juz $nextJuz"
        }
    }

    fun formatCompactPosition(record: MengajiRecord): String {
        val jilidPart = record.jilidOrJuz.trim().ifBlank { record.bookType.trim() }
        val pagePart = record.pageOrVerse.trim()
        return listOf(record.bookType.trim(), jilidPart, pagePart)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" • ")
    }

    fun availableMonthKeys(records: List<MengajiRecord>): List<String> {
        return records.map { DateUtils.getMonthKey(it.sessionDate) }
            .distinct()
            .sortedDescending()
    }

    fun filterByMonth(records: List<MengajiRecord>, monthKey: String): List<MengajiRecord> {
        val sorted = records.sortedWith(compareBy<MengajiRecord> { it.sessionDate }.thenBy { it.createdAt })
        if (monthKey.isBlank() || monthKey == "ALL") return sorted
        return sorted.filter { DateUtils.getMonthKey(it.sessionDate) == monthKey }
    }

    fun formatMonthLabel(monthKey: String, isEnglish: Boolean = false): String {
        if (monthKey == "ALL" || monthKey.isBlank()) {
            return if (isEnglish) "All Months" else "Semua Bulan"
        }
        return try {
            val parts = monthKey.split("-")
            if (parts.size == 2) {
                val cal = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.YEAR, parts[0].toInt())
                    set(java.util.Calendar.MONTH, parts[1].toInt() - 1)
                    set(java.util.Calendar.DAY_OF_MONTH, 1)
                }
                val locale = if (isEnglish) Locale.ENGLISH else Locale("id", "ID")
                java.text.SimpleDateFormat("MMMM yyyy", locale).format(cal.time)
            } else {
                monthKey
            }
        } catch (_: Exception) {
            monthKey
        }
    }
}
