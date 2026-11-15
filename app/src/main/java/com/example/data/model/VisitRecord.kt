package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.util.StudentCsvParser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Entity(tableName = "visit_sessions")
data class VisitSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val monthYear: String = "", // Canonical "yyyy-MM" e.g. "2026-11"
    val sessionDatesCsv: String = "", // Comma-separated epoch millis (day-start) supporting multiple dates per session
    val selectedDistrictsCsv: String = "", // Pipe-separated normalized kecamatan names
    val description: String = "",
    val isActive: Boolean = true,
    val createdBy: String = "Admin Sekolah",
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getVisitDatesList(): List<Long> = VisitHelper.parseDatesCsv(sessionDatesCsv)

    fun getDistrictsList(): List<String> = VisitHelper.parseDistrictsCsv(selectedDistrictsCsv)

    fun isDateScheduled(dateMillis: Long): Boolean {
        val targetDay = VisitHelper.normalizeDayStart(dateMillis)
        return getVisitDatesList().any { VisitHelper.normalizeDayStart(it) == targetDay }
    }
}

@Entity(tableName = "visit_records")
data class VisitRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val sessionTitle: String = "",
    val visitDate: Long = System.currentTimeMillis(), // Day-start millis or scheduled slot millis
    val checkedInAt: Long = System.currentTimeMillis(), // Exact arrival check-in timestamp
    val studentId: Long = 0L,
    val studentName: String,
    val studentNik: String = "",
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val studentDistrict: String = "",
    val visitorName: String = "",
    val visitorRelation: String = "Orang Tua / Wali",
    val checkInMethod: String = METHOD_BARCODE,
    val isOutOfScheduleDistrict: Boolean = false,
    val waliName: String = "Wali Asuh",
    val notes: String = "",
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val METHOD_BARCODE = "BARCODE"
        const val METHOD_MANUAL = "MANUAL"
    }
}

data class DistrictStudentGroup(
    val normalizedKey: String,
    val displayName: String,
    val students: List<ChildStudent>
)

object VisitHelper {
    const val UNASSIGNED_DISTRICT = "__UNASSIGNED__"

    private val leadingPrefixRegex = Regex("""^(?:kec\.?|kecamatan)\s+""", RegexOption.IGNORE_CASE)
    private val multiSpaceRegex = Regex("""\s+""")

    fun normalizeDistrict(raw: String?): String {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.isBlank()) return ""
        val withoutPrefix = leadingPrefixRegex.replace(trimmed, "").trim()
        val collapsed = multiSpaceRegex.replace(withoutPrefix, " ")
        if (collapsed.isBlank() || collapsed == "-" || collapsed.equals("null", ignoreCase = true)) {
            return ""
        }
        return collapsed.split(" ").joinToString(" ") { word ->
            if (word.isBlank()) ""
            else word.lowercase(Locale.US).replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
            }
        }.trim()
    }

    fun resolveStudentDistrictKey(student: ChildStudent): String {
        val clean = normalizeDistrict(student.district)
        return if (clean.isBlank()) UNASSIGNED_DISTRICT else clean
    }

    fun displayDistrict(keyOrRaw: String, isEn: Boolean = false): String {
        if (keyOrRaw == UNASSIGNED_DISTRICT || keyOrRaw.isBlank()) {
            return if (isEn) "District Not Set" else "Belum Diisi Kecamatan"
        }
        val clean = normalizeDistrict(keyOrRaw)
        return clean.ifBlank { if (isEn) "District Not Set" else "Belum Diisi Kecamatan" }
    }

    fun extractDynamicDistricts(
        students: List<ChildStudent>,
        isEn: Boolean = false
    ): List<DistrictStudentGroup> {
        if (students.isEmpty()) return emptyList()
        val grouped = students.groupBy { resolveStudentDistrictKey(it) }
        val namedGroups = grouped.entries
            .filter { it.key != UNASSIGNED_DISTRICT }
            .sortedBy { it.key.lowercase(Locale.US) }
            .map { (key, list) ->
                DistrictStudentGroup(
                    normalizedKey = key,
                    displayName = key,
                    students = list.sortedBy { it.name.lowercase(Locale.US) }
                )
            }
        val unassigned = grouped[UNASSIGNED_DISTRICT]?.let { list ->
            DistrictStudentGroup(
                normalizedKey = UNASSIGNED_DISTRICT,
                displayName = displayDistrict(UNASSIGNED_DISTRICT, isEn),
                students = list.sortedBy { it.name.lowercase(Locale.US) }
            )
        }
        return if (unassigned != null) namedGroups + unassigned else namedGroups
    }

    fun normalizeDayStart(millis: Long): Long {
        if (millis <= 0L) return 0L
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun parseDatesCsv(csv: String): List<Long> {
        if (csv.isBlank()) return emptyList()
        return csv.split(",", "|", ";")
            .mapNotNull { token -> token.trim().toLongOrNull()?.takeIf { it > 0L } }
            .map { normalizeDayStart(it) }
            .distinct()
            .sorted()
    }

    fun formatDatesCsv(dates: List<Long>): String {
        return dates
            .filter { it > 0L }
            .map { normalizeDayStart(it) }
            .distinct()
            .sorted()
            .joinToString(",")
    }

    fun parseDistrictsCsv(csv: String): List<String> {
        if (csv.isBlank()) return emptyList()
        val sep = if (csv.contains("|")) "|" else ","
        return csv.split(sep)
            .mapNotNull { token ->
                val t = token.trim()
                when {
                    t.isBlank() -> null
                    t == UNASSIGNED_DISTRICT -> UNASSIGNED_DISTRICT
                    else -> normalizeDistrict(t).takeIf { it.isNotBlank() }
                }
            }
            .distinctBy { it.lowercase(Locale.US) }
    }

    fun formatDistrictsCsv(districts: List<String>): String {
        return districts
            .mapNotNull { raw ->
                val t = raw.trim()
                when {
                    t.isBlank() -> null
                    t == UNASSIGNED_DISTRICT -> UNASSIGNED_DISTRICT
                    else -> normalizeDistrict(t).takeIf { it.isNotBlank() }
                }
            }
            .distinctBy { it.lowercase(Locale.US) }
            .joinToString("|")
    }

    fun scheduledStudentsForSession(
        session: VisitSession,
        allStudents: List<ChildStudent>
    ): List<ChildStudent> {
        val districts = session.getDistrictsList()
        if (districts.isEmpty()) return emptyList()
        val allowedLower = districts.map { it.lowercase(Locale.US) }.toSet()
        return allStudents
            .filter { student ->
                resolveStudentDistrictKey(student).lowercase(Locale.US) in allowedLower
            }
            .sortedWith(
                compareBy<ChildStudent> { resolveStudentDistrictKey(it).lowercase(Locale.US) }
                    .thenBy { it.name.lowercase(Locale.US) }
            )
    }

    fun isStudentInSessionSchedule(session: VisitSession, student: ChildStudent): Boolean {
        val allowedLower = session.getDistrictsList().map { it.lowercase(Locale.US) }.toSet()
        if (allowedLower.isEmpty()) return false
        return resolveStudentDistrictKey(student).lowercase(Locale.US) in allowedLower
    }

    fun matchesRecordToStudent(record: VisitRecord, student: ChildStudent): Boolean {
        if (student.id > 0L && record.studentId == student.id) return true
        val studentNik = StudentCsvParser.cleanDigitsOnly(student.nik)
        val recordNik = StudentCsvParser.cleanDigitsOnly(record.studentNik)
        if (studentNik.isNotBlank() && studentNik == recordNik) return true
        if (student.nis.isNotBlank() && record.studentNis.equals(student.nis, ignoreCase = true)) return true
        return student.name.isNotBlank() &&
            record.studentName.trim().equals(student.name.trim(), ignoreCase = true)
    }

    fun findVisitForStudent(
        sessionRecords: List<VisitRecord>,
        student: ChildStudent,
        targetDayStart: Long? = null
    ): VisitRecord? {
        val normalizedTarget = targetDayStart?.takeIf { it > 0L }?.let { normalizeDayStart(it) }
        return sessionRecords.firstOrNull { rec ->
            matchesRecordToStudent(rec, student) &&
                (normalizedTarget == null || normalizeDayStart(rec.visitDate) == normalizedTarget)
        }
    }

    fun formatMonthYearLabel(monthYear: String, locale: Locale): String {
        val parts = monthYear.trim().split("-")
        if (parts.size == 2) {
            val year = parts[0].toIntOrNull()
            val month = parts[1].toIntOrNull()
            if (year != null && month != null && month in 1..12) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month - 1)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                return SimpleDateFormat("MMMM yyyy", locale).format(cal.time)
            }
        }
        return monthYear.ifBlank { SimpleDateFormat("MMMM yyyy", locale).format(Date()) }
    }

    fun buildDefaultMonthYear(millis: Long = System.currentTimeMillis()): String {
        return SimpleDateFormat("yyyy-MM", Locale.US).format(Date(millis))
    }

    fun defaultSessionTitleForMonthYear(monthYear: String, locale: Locale, isEn: Boolean): String {
        val label = formatMonthYearLabel(monthYear, locale)
        return if (isEn) "Parent Visit $label" else "Kunjungan $label"
    }
}
