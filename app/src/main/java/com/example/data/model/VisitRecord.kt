package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.util.StudentCsvParser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class VisitLaunchStage {
    DRAFT,
    WALI_PREVIEW,
    PARENT_LIVE,
    CLOSED
}

@Entity(tableName = "visit_sessions")
data class VisitSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val monthYear: String = "", // Canonical "yyyy-MM" e.g. "2026-11"
    val sessionDatesCsv: String = "", // Comma-separated epoch millis (day-start) supporting multiple dates per session
    val selectedDistrictsCsv: String = "", // Pipe-separated normalized kecamatan names
    val description: String = "",
    val visitTimeRange: String = DEFAULT_TIME_RANGE,
    val isActive: Boolean = true,
    val publishedToWali: Boolean = true,
    val publishedToParent: Boolean = true,
    val waliLaunchedAt: Long = 0L,
    val parentLaunchedAt: Long = 0L,
    val updatedNoticeAfterParentLaunch: Boolean = false,
    val createdBy: String = "Admin Sekolah",
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getVisitDatesList(): List<Long> = VisitHelper.parseDatesCsv(sessionDatesCsv)

    fun getDistrictsList(): List<String> = VisitHelper.parseDistrictsCsv(selectedDistrictsCsv)

    fun displayTimeRange(): String = VisitHelper.normalizeTimeRange(visitTimeRange)

    fun isDateScheduled(dateMillis: Long): Boolean {
        val targetDay = VisitHelper.normalizeDayStart(dateMillis)
        return getVisitDatesList().any { VisitHelper.normalizeDayStart(it) == targetDay }
    }

    fun launchStage(): VisitLaunchStage = when {
        !isActive -> VisitLaunchStage.CLOSED
        publishedToParent -> VisitLaunchStage.PARENT_LIVE
        publishedToWali -> VisitLaunchStage.WALI_PREVIEW
        else -> VisitLaunchStage.DRAFT
    }

    companion object {
        const val DEFAULT_TIME_RANGE = "08.00 - 11.00 WIB"
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

@Entity(tableName = "visit_confirmations")
data class VisitConfirmation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val sessionTitle: String = "",
    val visitDate: Long = 0L,
    val studentId: Long = 0L,
    val studentName: String,
    val studentNik: String = "",
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val studentDistrict: String = "",
    val parentUsername: String = "",
    val visitorName: String = "",
    val visitorRelation: String = "Orang Tua / Wali",
    val attendanceStatus: String = STATUS_HADIR,
    val estimatedArrivalTime: String = "",
    val notes: String = "",
    val barcodeToken: String = "",
    val schoolId: String = "SRT1-KDR",
    val confirmedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isHadir: Boolean
        get() = attendanceStatus.equals(STATUS_HADIR, ignoreCase = true)

    fun qrPayload(): String = VisitHelper.buildVisitBarcodePayload(
        sessionId = sessionId,
        studentId = studentId,
        token = barcodeToken,
        studentNik = studentNik
    )

    companion object {
        const val STATUS_HADIR = "HADIR"
        const val STATUS_BERHALANGAN = "BERHALANGAN"
    }
}

@Entity(tableName = "visit_session_swaps")
data class VisitSessionSwap(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: String = "SRT1-KDR",
    val monthYear: String = "",
    val fromSessionId: Long,
    val fromSessionTitle: String = "",
    val toSessionId: Long,
    val toSessionTitle: String = "",
    val studentId: Long = 0L,
    val studentName: String = "",
    val studentNik: String = "",
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val studentDistrict: String = "",
    val reason: String = "",
    val hadPriorCheckIn: Boolean = false,
    val priorCheckInAt: Long = 0L,
    val swappedByUsername: String = "",
    val swappedByName: String = "Wali Asuh",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class VisitSessionDraft(
    val title: String,
    val monthYear: String,
    val visitDateMillis: Long,
    val selectedDistricts: List<String>,
    val description: String = "",
    val visitTimeRange: String = VisitHelper.DEFAULT_VISIT_TIME_RANGE
)

data class VisitQrPayload(
    val sessionId: Long,
    val studentId: Long,
    val token: String,
    val studentNik: String = ""
)

data class DistrictStudentGroup(
    val normalizedKey: String,
    val displayName: String,
    val students: List<ChildStudent>
)

object VisitHelper {
    const val UNASSIGNED_DISTRICT = "__UNASSIGNED__"
    const val QR_PREFIX = "WALIVISIT"
    const val DEFAULT_VISIT_TIME_RANGE = "08.00 - 11.00 WIB"
    val VISIT_TIME_PRESETS = listOf(
        "08.00 - 11.00 WIB",
        "08.00 - 12.00 WIB",
        "09.00 - 12.00 WIB",
        "13.00 - 16.00 WIB"
    )

    fun normalizeTimeRange(raw: String?): String {
        val clean = raw?.trim().orEmpty().replace(Regex("""\s+"""), " ")
        return clean.ifBlank { DEFAULT_VISIT_TIME_RANGE }
    }

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

    fun matchesSwapToStudent(swap: VisitSessionSwap, student: ChildStudent): Boolean {
        if (student.id > 0L && swap.studentId == student.id) return true
        val studentNik = StudentCsvParser.cleanDigitsOnly(student.nik)
        val swapNik = StudentCsvParser.cleanDigitsOnly(swap.studentNik)
        if (studentNik.isNotBlank() && studentNik == swapNik) return true
        if (student.nis.isNotBlank() && swap.studentNis.equals(student.nis, ignoreCase = true)) return true
        return student.name.isNotBlank() &&
            swap.studentName.trim().equals(student.name.trim(), ignoreCase = true)
    }

    fun canWaliSwapStudent(
        student: ChildStudent,
        isAdmin: Boolean = false,
        currentUsername: String,
        currentWaliName: String,
        isAnakSuperAdmin: Boolean = false
    ): Boolean {
        if (!TandemWaliHelper.hasPrimaryWali(student)) return false
        return TandemWaliHelper.isAssignedWali(student, currentUsername, currentWaliName)
    }

    fun findOutgoingSwapFromSession(
        sessionId: Long,
        student: ChildStudent,
        swaps: List<VisitSessionSwap>,
        allSessions: List<VisitSession> = emptyList()
    ): VisitSessionSwap? {
        return swaps
            .filter { swap ->
                swap.fromSessionId == sessionId &&
                    swap.toSessionId != sessionId &&
                    matchesSwapToStudent(swap, student) &&
                    (allSessions.isEmpty() || allSessions.any { it.id == swap.toSessionId })
            }
            .maxByOrNull { it.updatedAt }
    }

    fun findIncomingSwapToSession(
        sessionId: Long,
        student: ChildStudent,
        swaps: List<VisitSessionSwap>
    ): VisitSessionSwap? {
        return swaps
            .filter { swap ->
                swap.toSessionId == sessionId &&
                    swap.fromSessionId != sessionId &&
                    matchesSwapToStudent(swap, student)
            }
            .maxByOrNull { it.updatedAt }
    }

    fun effectiveScheduledStudentsForSession(
        session: VisitSession,
        allStudents: List<ChildStudent>,
        swaps: List<VisitSessionSwap>,
        allSessions: List<VisitSession> = emptyList()
    ): List<ChildStudent> {
        if (swaps.isEmpty()) return scheduledStudentsForSession(session, allStudents)
        val base = scheduledStudentsForSession(session, allStudents)
            .filter { student ->
                findOutgoingSwapFromSession(session.id, student, swaps, allSessions) == null
            }
        val incoming = allStudents.filter { student ->
            findIncomingSwapToSession(session.id, student, swaps) != null
        }
        return (base + incoming)
            .distinctBy { it.id }
            .sortedWith(
                compareBy<ChildStudent> { resolveStudentDistrictKey(it).lowercase(Locale.US) }
                    .thenBy { it.name.lowercase(Locale.US) }
            )
    }

    fun swappedOutFromSession(
        session: VisitSession,
        allStudents: List<ChildStudent>,
        swaps: List<VisitSessionSwap>,
        allSessions: List<VisitSession> = emptyList()
    ): List<Pair<ChildStudent, VisitSessionSwap>> {
        if (swaps.isEmpty()) return emptyList()
        return scheduledStudentsForSession(session, allStudents)
            .mapNotNull { student ->
                findOutgoingSwapFromSession(session.id, student, swaps, allSessions)?.let { swap ->
                    student to swap
                }
            }
    }

    fun isStudentEffectiveInSession(
        session: VisitSession,
        student: ChildStudent,
        swaps: List<VisitSessionSwap>,
        allSessions: List<VisitSession> = emptyList()
    ): Boolean {
        if (findIncomingSwapToSession(session.id, student, swaps) != null) return true
        if (findOutgoingSwapFromSession(session.id, student, swaps, allSessions) != null) return false
        return isStudentInSessionSchedule(session, student)
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

    fun matchesConfirmationToStudent(conf: VisitConfirmation, student: ChildStudent): Boolean {
        if (student.id > 0L && conf.studentId == student.id) return true
        val studentNik = StudentCsvParser.cleanDigitsOnly(student.nik)
        val confNik = StudentCsvParser.cleanDigitsOnly(conf.studentNik)
        if (studentNik.isNotBlank() && studentNik == confNik) return true
        if (student.nis.isNotBlank() && conf.studentNis.equals(student.nis, ignoreCase = true)) return true
        return student.name.isNotBlank() &&
            conf.studentName.trim().equals(student.name.trim(), ignoreCase = true)
    }

    fun findConfirmationForStudent(
        confirmations: List<VisitConfirmation>,
        sessionId: Long,
        student: ChildStudent,
        targetDayStart: Long? = null
    ): VisitConfirmation? {
        val normalizedTarget = targetDayStart?.takeIf { it > 0L }?.let { normalizeDayStart(it) }
        val inSession = confirmations.filter { it.sessionId == sessionId && matchesConfirmationToStudent(it, student) }
        if (inSession.isEmpty()) return null
        if (normalizedTarget != null) {
            inSession.firstOrNull { it.visitDate > 0L && normalizeDayStart(it.visitDate) == normalizedTarget }?.let {
                return it
            }
        }
        return inSession.maxByOrNull { it.updatedAt }
    }

    fun buildBarcodeToken(sessionId: Long, studentId: Long): String {
        val sPart = sessionId.coerceAtLeast(0L).toString(36).uppercase(Locale.US)
        val uPart = studentId.coerceAtLeast(0L).toString(36).uppercase(Locale.US)
        val sumPart = ((sessionId * 31L + studentId * 17L) and 0xFFFFL).toString(16).uppercase(Locale.US).padStart(4, '0')
        return "VST-$sPart-$uPart-$sumPart"
    }

    fun buildVisitBarcodePayload(
        sessionId: Long,
        studentId: Long,
        token: String,
        studentNik: String = ""
    ): String {
        val safeToken = token.trim().ifBlank { buildBarcodeToken(sessionId, studentId) }
        val cleanNik = StudentCsvParser.cleanDigitsOnly(studentNik)
        return "$QR_PREFIX|$sessionId|$studentId|$safeToken|$cleanNik"
    }

    fun parseVisitBarcodePayload(raw: String): VisitQrPayload? {
        val trimmed = raw.trim()
        if (!trimmed.startsWith("$QR_PREFIX|", ignoreCase = true)) return null
        val parts = trimmed.split("|")
        if (parts.size < 4) return null
        val sessionId = parts[1].trim().toLongOrNull() ?: return null
        val studentId = parts[2].trim().toLongOrNull() ?: return null
        val token = parts[3].trim()
        val nik = parts.getOrNull(4)?.trim().orEmpty()
        return VisitQrPayload(
            sessionId = sessionId,
            studentId = studentId,
            token = token,
            studentNik = StudentCsvParser.cleanDigitsOnly(nik)
        )
    }

    /**
     * Mengembalikan map kecamatan (lowercase) -> indeks sesi yang sudah memilih kecamatan tsb,
     * kecuali sesi [excludeIndex]. Memastikan 1 kecamatan hanya dipilih oleh 1 sesi.
     */
    fun usedDistrictsByOtherDrafts(
        draftDistricts: List<Set<String>>,
        excludeIndex: Int
    ): Map<String, Int> {
        val map = linkedMapOf<String, Int>()
        draftDistricts.forEachIndexed { idx, keys ->
            if (idx != excludeIndex) {
                keys.forEach { key ->
                    val normLower = key.trim().lowercase(Locale.US)
                    if (normLower.isNotBlank() && normLower !in map) {
                        map[normLower] = idx
                    }
                }
            }
        }
        return map
    }

    /**
     * Mengembalikan map kecamatan (lowercase) -> judul sesi aktif lain di bulan yang sama.
     */
    fun usedDistrictsInExistingSessions(
        sessions: List<VisitSession>,
        monthYear: String,
        excludeSessionId: Long = -1L
    ): Map<String, String> {
        val cleanMonth = monthYear.trim()
        val map = linkedMapOf<String, String>()
        sessions
            .filter { it.isActive && it.id != excludeSessionId && (cleanMonth.isBlank() || it.monthYear.equals(cleanMonth, ignoreCase = true)) }
            .forEach { sess ->
                sess.getDistrictsList().forEach { dist ->
                    val normLower = dist.trim().lowercase(Locale.US)
                    if (normLower.isNotBlank() && normLower !in map) {
                        map[normLower] = sess.title
                    }
                }
            }
        return map
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
