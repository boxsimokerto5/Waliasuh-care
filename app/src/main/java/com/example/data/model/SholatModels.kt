package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.util.StudentClassifier
import com.example.util.StudentCsvParser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class SholatWaktu(
    val code: String,
    val labelId: String,
    val labelEn: String,
    val shortCode: String,
    val startHour: Int,
    val endHour: Int
) {
    SUBUH("SUBUH", "Subuh", "Fajr", "S", 4, 6),
    DZUHUR("DZUHUR", "Dzuhur", "Dhuhr", "D", 11, 14),
    ASHAR("ASHAR", "Ashar", "Asr", "A", 14, 17),
    MAGHRIB("MAGHRIB", "Maghrib", "Maghrib", "M", 17, 19),
    ISYA("ISYA", "Isya", "Isha", "I", 19, 23);

    fun label(isEn: Boolean): String = if (isEn) labelEn else labelId

    companion object {
        fun fromCode(raw: String?): SholatWaktu {
            val clean = raw?.trim()?.uppercase().orEmpty()
            return entries.firstOrNull { it.code == clean } ?: DZUHUR
        }
    }
}

enum class SholatStatus(
    val code: String,
    val labelId: String,
    val labelEn: String,
    val shortCode: String
) {
    JAMAAH("JAMAAH", "Jamaah", "Congregation", "J"),
    MUNFARID("MUNFARID", "Munfarid", "Individual", "M"),
    UDZUR_HAID("UDZUR_HAID", "Udzur Haid", "Period Excused", "H"),
    SAKIT("SAKIT", "Sakit", "Sick", "S"),
    IZIN("IZIN", "Izin", "Excused", "I"),
    ALFA("ALFA", "Alfa", "Absent", "A");

    fun label(isEn: Boolean): String = if (isEn) labelEn else labelId

    companion object {
        fun fromCode(raw: String?): SholatStatus {
            val clean = raw?.trim()?.uppercase().orEmpty()
            return entries.firstOrNull { it.code == clean } ?: JAMAAH
        }
    }
}

enum class SholatDisciplineTag(
    val code: String,
    val labelId: String,
    val labelEn: String
) {
    NONE("", "Standar", "Standard"),
    SAF_DEPAN("SAF_DEPAN", "Saf Depan", "Front Row"),
    TEPAT_WAKTU("TEPAT_WAKTU", "Tepat Waktu", "On Time"),
    MASBUK("MASBUK", "Masbuk", "Masbuq");

    fun label(isEn: Boolean): String = if (isEn) labelEn else labelId

    companion object {
        fun fromCode(raw: String?): SholatDisciplineTag {
            val clean = raw?.trim()?.uppercase().orEmpty()
            return entries.firstOrNull { it.code == clean } ?: NONE
        }
    }
}

@Entity(tableName = "sholat_records")
data class SholatRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String, // "yyyy-MM-dd"
    val dateMillis: Long,
    val waktu: String, // SholatWaktu.code
    val status: String, // SholatStatus.code
    val disciplineTag: String = "", // SholatDisciplineTag.code
    val studentId: Long = 0L,
    val studentName: String,
    val studentNik: String = "",
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val studentJenjang: String = "",
    val notes: String = "",
    val inputMethod: String = "MANUAL", // MANUAL, BULK_JAMAAH, BARCODE, AUTO_HAID
    val recordedBy: String = "Wali Asuh",
    val recordedByUsername: String = "",
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SholatStudentAnalytics(
    val student: ChildStudent,
    val totalLogged: Int,
    val jamaahCount: Int,
    val munfaridCount: Int,
    val udzurHaidCount: Int,
    val sakitCount: Int,
    val izinCount: Int,
    val alfaCount: Int,
    val masbukCount: Int,
    val safDepanCount: Int,
    val subuhJamaahCount: Int,
    val jamaahRatePercent: Int
)

data class SholatWaktuSummary(
    val waktu: SholatWaktu,
    val totalLogged: Int,
    val jamaahCount: Int,
    val munfaridCount: Int,
    val udzurHaidCount: Int,
    val sakitIzinCount: Int,
    val alfaCount: Int,
    val jamaahPercent: Int
)

object SholatHelper {
    const val METHOD_MANUAL = "MANUAL"
    const val METHOD_BULK_JAMAAH = "BULK_JAMAAH"
    const val METHOD_BARCODE = "BARCODE"
    const val METHOD_AUTO_HAID = "AUTO_HAID"

    fun todayDateKey(nowMillis: Long = System.currentTimeMillis()): String {
        return dateKeyFromMillis(nowMillis)
    }

    fun dateKeyFromMillis(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(millis))
    }

    fun monthKeyFromDateKey(dateKey: String): String {
        return if (dateKey.length >= 7) dateKey.substring(0, 7) else todayDateKey().substring(0, 7)
    }

    fun startOfDayMillis(dateKey: String): Long {
        return try {
            val parts = dateKey.split("-")
            if (parts.size == 3) {
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, parts[0].toInt())
                    set(Calendar.MONTH, parts[1].toInt() - 1)
                    set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            } else {
                System.currentTimeMillis()
            }
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    fun shiftDateKey(dateKey: String, deltaDays: Int): String {
        val cal = Calendar.getInstance().apply {
            timeInMillis = startOfDayMillis(dateKey)
            add(Calendar.DAY_OF_YEAR, deltaDays)
        }
        return dateKeyFromMillis(cal.timeInMillis)
    }

    fun shiftMonthKey(monthKey: String, deltaMonths: Int): String {
        return try {
            val parts = monthKey.split("-")
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, parts[0].toInt())
                set(Calendar.MONTH, parts[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, deltaMonths)
            }
            String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        } catch (_: Exception) {
            todayDateKey().substring(0, 7)
        }
    }

    fun formatDateKeyDisplay(dateKey: String, isEn: Boolean): String {
        return try {
            val millis = startOfDayMillis(dateKey)
            val locale = if (isEn) Locale.ENGLISH else Locale("id", "ID")
            SimpleDateFormat("EEE, dd MMM yyyy", locale).format(Date(millis))
        } catch (_: Exception) {
            dateKey
        }
    }

    fun formatMonthKeyDisplay(monthKey: String, isEn: Boolean): String {
        return try {
            val parts = monthKey.split("-")
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, parts[0].toInt())
                set(Calendar.MONTH, parts[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val locale = if (isEn) Locale.ENGLISH else Locale("id", "ID")
            SimpleDateFormat("MMMM yyyy", locale).format(cal.time)
        } catch (_: Exception) {
            monthKey
        }
    }

    fun detectActiveWaktu(calendar: Calendar = Calendar.getInstance()): SholatWaktu {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..10 -> SholatWaktu.SUBUH
            in 11..14 -> SholatWaktu.DZUHUR
            in 15..16 -> SholatWaktu.ASHAR
            in 17..18 -> SholatWaktu.MAGHRIB
            else -> SholatWaktu.ISYA
        }
    }

    /**
     * ID deterministik agar entri yang sama (sekolah + siswa + tanggal + waktu sholat)
     * otomatis melakukan upsert bersih baik di Room maupun saat sinkron lintas perangkat via PocketBase.
     */
    fun deterministicId(
        schoolId: String,
        studentId: Long,
        dateKey: String,
        waktu: SholatWaktu
    ): Long {
        val cleanSchool = schoolId.trim().uppercase().ifBlank { "SRT1-KDR" }
        val raw = "sholat:$cleanSchool:$studentId:${dateKey.trim()}:${waktu.code}"
        var hash = -3750763034362895579L
        val prime = 1099511628211L
        for (ch in raw) {
            hash = hash xor ch.code.toLong()
            hash *= prime
        }
        val positive56 = hash and 0x00FFFFFFFFFFFFFFL
        return if (positive56 > 0L) positive56 else 1L
    }

    fun matchesStudent(record: SholatRecord, student: ChildStudent): Boolean {
        val sameSchool = record.schoolId.isBlank() || student.schoolId.isBlank() ||
            record.schoolId.equals(student.schoolId, ignoreCase = true)
        if (!sameSchool) return false
        if (student.id > 0L && record.studentId == student.id) return true
        val studentNik = StudentCsvParser.cleanDigitsOnly(student.nik)
        val recordNik = StudentCsvParser.cleanDigitsOnly(record.studentNik)
        if (studentNik.isNotBlank() && recordNik.isNotBlank() && studentNik == recordNik) return true
        if (student.nis.isNotBlank() && record.studentNis.isNotBlank() &&
            record.studentNis.trim().equals(student.nis.trim(), ignoreCase = true)
        ) {
            return true
        }
        return record.studentName.equals(student.name, ignoreCase = true)
    }

    fun recordsForStudent(records: List<SholatRecord>, student: ChildStudent): List<SholatRecord> {
        return records
            .filter { matchesStudent(it, student) }
            .sortedWith(
                compareByDescending<SholatRecord> { it.dateKey }
                    .thenByDescending { SholatWaktu.fromCode(it.waktu).ordinal }
                    .thenByDescending { it.updatedAt }
            )
    }

    fun findActiveHaidOnDate(
        student: ChildStudent,
        haidRecords: List<HaidRecord>,
        dateKey: String
    ): HaidRecord? {
        if (!StudentClassifier.isFemaleStudent(student)) return null
        val targetStart = startOfDayMillis(dateKey)
        val targetEnd = targetStart + 86_399_999L
        val todayKey = todayDateKey()
        val studentNik = StudentCsvParser.cleanDigitsOnly(student.nik)

        return haidRecords.firstOrNull { haid ->
            val matches = (student.id > 0L && haid.studentId == student.id) ||
                (studentNik.isNotBlank() && StudentCsvParser.cleanDigitsOnly(haid.studentNik) == studentNik) ||
                haid.studentName.equals(student.name, ignoreCase = true)
            if (!matches) return@firstOrNull false

            if (dateKey == todayKey && haid.isActive) {
                true
            } else {
                val haidStartDay = startOfDayMillis(dateKeyFromMillis(haid.startDate))
                val effEnd = haid.effectiveEndDate
                val haidEndDay = if (effEnd != null) {
                    startOfDayMillis(dateKeyFromMillis(effEnd)) + 86_399_999L
                } else if (haid.isActive) {
                    System.currentTimeMillis() + 86_399_999L
                } else {
                    startOfDayMillis(dateKeyFromMillis(haid.startDate)) + 86_399_999L
                }
                targetEnd >= haidStartDay && targetStart <= haidEndDay
            }
        }
    }

    fun buildDailyMatrix(
        records: List<SholatRecord>,
        dateKey: String
    ): Map<Long, Map<SholatWaktu, SholatRecord>> {
        val result = LinkedHashMap<Long, MutableMap<SholatWaktu, SholatRecord>>()
        records.filter { it.dateKey == dateKey }.forEach { rec ->
            val map = result.getOrPut(rec.studentId) { LinkedHashMap() }
            val w = SholatWaktu.fromCode(rec.waktu)
            val prev = map[w]
            if (prev == null || rec.updatedAt >= prev.updatedAt) {
                map[w] = rec
            }
        }
        return result
    }

    fun computeWaktuSummaries(
        records: List<SholatRecord>,
        targetStudentsCount: Int
    ): List<SholatWaktuSummary> {
        return SholatWaktu.entries.map { waktu ->
            val sub = records.filter { SholatWaktu.fromCode(it.waktu) == waktu }
            val jamaah = sub.count { SholatStatus.fromCode(it.status) == SholatStatus.JAMAAH }
            val munfarid = sub.count { SholatStatus.fromCode(it.status) == SholatStatus.MUNFARID }
            val udzur = sub.count { SholatStatus.fromCode(it.status) == SholatStatus.UDZUR_HAID }
            val sakitIzin = sub.count {
                val st = SholatStatus.fromCode(it.status)
                st == SholatStatus.SAKIT || st == SholatStatus.IZIN
            }
            val alfa = sub.count { SholatStatus.fromCode(it.status) == SholatStatus.ALFA }
            val obligatoryCount = (sub.size - udzur).coerceAtLeast(0)
            val denominator = if (obligatoryCount > 0) obligatoryCount else targetStudentsCount.coerceAtLeast(1)
            val pct = if (obligatoryCount == 0 && sub.isEmpty()) 0 else ((jamaah * 100f) / denominator).toInt().coerceIn(0, 100)
            SholatWaktuSummary(
                waktu = waktu,
                totalLogged = sub.size,
                jamaahCount = jamaah,
                munfaridCount = munfarid,
                udzurHaidCount = udzur,
                sakitIzinCount = sakitIzin,
                alfaCount = alfa,
                jamaahPercent = pct
            )
        }
    }

    fun computeStudentAnalytics(
        students: List<ChildStudent>,
        records: List<SholatRecord>,
        monthKey: String? = null
    ): List<SholatStudentAnalytics> {
        val scopedRecords = if (monthKey.isNullOrBlank()) records else records.filter { it.dateKey.startsWith(monthKey) }
        return students.map { student ->
            val stRecords = recordsForStudent(scopedRecords, student)
            val jamaah = stRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.JAMAAH }
            val munfarid = stRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.MUNFARID }
            val udzur = stRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.UDZUR_HAID }
            val sakit = stRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.SAKIT }
            val izin = stRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.IZIN }
            val alfa = stRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.ALFA }
            val masbuk = stRecords.count { SholatDisciplineTag.fromCode(it.disciplineTag) == SholatDisciplineTag.MASBUK }
            val safDepan = stRecords.count { SholatDisciplineTag.fromCode(it.disciplineTag) == SholatDisciplineTag.SAF_DEPAN }
            val subuhJamaah = stRecords.count {
                SholatWaktu.fromCode(it.waktu) == SholatWaktu.SUBUH &&
                    SholatStatus.fromCode(it.status) == SholatStatus.JAMAAH
            }
            val wajibDenominator = (stRecords.size - udzur).coerceAtLeast(0)
            val rate = if (wajibDenominator > 0) ((jamaah * 100f) / wajibDenominator).toInt().coerceIn(0, 100) else 0
            SholatStudentAnalytics(
                student = student,
                totalLogged = stRecords.size,
                jamaahCount = jamaah,
                munfaridCount = munfarid,
                udzurHaidCount = udzur,
                sakitCount = sakit,
                izinCount = izin,
                alfaCount = alfa,
                masbukCount = masbuk,
                safDepanCount = safDepan,
                subuhJamaahCount = subuhJamaah,
                jamaahRatePercent = rate
            )
        }
    }

    fun buildWhatsappRecap(
        schoolProfile: SchoolProfile,
        dateKey: String,
        selectedWaktu: SholatWaktu,
        students: List<ChildStudent>,
        records: List<SholatRecord>,
        isEn: Boolean
    ): String {
        val schoolName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName }.ifBlank { "Waliasuh Care" }
        val dayRecords = records.filter { it.dateKey == dateKey }
        val waktuRecords = dayRecords.filter { SholatWaktu.fromCode(it.waktu) == selectedWaktu }
        val jamaah = waktuRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.JAMAAH }
        val munfarid = waktuRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.MUNFARID }
        val udzur = waktuRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.UDZUR_HAID }
        val sakit = waktuRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.SAKIT }
        val izin = waktuRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.IZIN }
        val alfa = waktuRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.ALFA }
        val belum = (students.size - waktuRecords.size).coerceAtLeast(0)

        val alfaNames = waktuRecords
            .filter { SholatStatus.fromCode(it.status) == SholatStatus.ALFA }
            .joinToString(", ") { it.studentName }

        return if (isEn) {
            buildString {
                appendLine("*PRAYER ATTENDANCE REPORT — ${selectedWaktu.labelEn.uppercase()}*")
                appendLine("_${schoolName}_")
                appendLine("Date: ${formatDateKeyDisplay(dateKey, true)}")
                appendLine("Total Students: ${students.size}")
                appendLine("")
                appendLine("• Congregation (Jamaah): $jamaah")
                appendLine("• Individual (Munfarid): $munfarid")
                appendLine("• Period Excused (Udzur Haid): $udzur")
                appendLine("• Sick / Excused: ${sakit + izin}")
                appendLine("• Absent (Alfa): $alfa")
                appendLine("• Unmarked: $belum")
                if (alfaNames.isNotBlank()) {
                    appendLine("")
                    appendLine("*Follow-up Needed (Alfa):* $alfaNames")
                }
            }.trim()
        } else {
            buildString {
                appendLine("*LAPORAN ABSEN SHOLAT ${selectedWaktu.labelId.uppercase()}*")
                appendLine("_${schoolName}_")
                appendLine("Tanggal: ${formatDateKeyDisplay(dateKey, false)}")
                appendLine("Total Siswa: ${students.size}")
                appendLine("")
                appendLine("• Jamaah: $jamaah")
                appendLine("• Munfarid: $munfarid")
                appendLine("• Udzur Syar'i (Haid): $udzur")
                appendLine("• Sakit / Izin: ${sakit + izin}")
                appendLine("• Alfa: $alfa")
                appendLine("• Belum Diabsen: $belum")
                if (alfaNames.isNotBlank()) {
                    appendLine("")
                    appendLine("*Perlu Pembinaan (Alfa):* $alfaNames")
                }
            }.trim()
        }
    }
}
