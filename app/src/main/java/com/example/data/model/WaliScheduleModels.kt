package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale

@Entity(tableName = "wali_shift_codes")
data class WaliShiftCode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: String = "SRT1-KDR",
    val code: String,
    val name: String,
    val shiftGroup: String = WaliScheduleHelper.GROUP_PAGI, // PAGI | SORE | MALAM | LEPAS | IZIN
    val zoneLabel: String = "",
    val timeRange: String = "",
    val startHour: Int = 7,
    val endHour: Int = 15,
    val workHours: Int = 8,
    val colorHex: String = "#0284C7",
    val sopNotes: String = "",
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wali_schedule_entries")
data class WaliScheduleEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: String = "SRT1-KDR",
    val monthKey: String, // YYYY-MM (e.g., 2026-11)
    val waliAccountId: Long = 0L,
    val waliUsername: String = "",
    val waliName: String,
    val waliCode: String = "", // Misal "L12", "P8"
    val sortOrder: Int = 0,
    val dayCodesJson: String = "{}", // {"1":"M3","2":"LP",...}
    val dayPinsJson: String = "{}",  // {"15":[{"label":"Jaga Puskesmas Wates","category":"FASKES","note":""}]}
    val dayLeaveJson: String = "{}", // {"17":{"status":"SAKIT","driveUrl":"https://...","note":"..."}}
    val updatedBy: String = "Admin Sekolah",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PinnedTaskItem(
    val label: String,
    val category: String = WaliScheduleHelper.PIN_TUGAS, // TUGAS | FASKES
    val note: String = ""
)

data class LeaveDayInfo(
    val status: String = "IZIN", // SAKIT | IZIN | CUTI
    val driveUrl: String = "",
    val note: String = ""
)

data class WaliRowStats(
    val pFull: Int,
    val sCount: Int,
    val mCount: Int,
    val lpCount: Int,
    val offCount: Int,
    val p1Count: Int,
    val p2Count: Int,
    val p3Count: Int,
    val p4Count: Int,
    val p5Count: Int,
    val izinCount: Int,
    val cutiCount: Int,
    val totalJamKerja: Int
)

data class DayShiftBreakdown(
    val day: Int,
    val p1: Int,
    val p2: Int,
    val p3: Int,
    val p4: Int,
    val p5: Int,
    val pagiFull: Int,
    val s1a: Int,
    val s2a: Int,
    val s3a: Int,
    val s4a: Int,
    val totalSore: Int,
    val m1: Int,
    val m2: Int,
    val m3: Int,
    val totalMalam: Int,
    val cuti: Int,
    val offAndLepas: Int,
    val izin: Int,
    val totalAssigned: Int
)

object WaliScheduleHelper {
    const val GROUP_PAGI = "PAGI"
    const val GROUP_SORE = "SORE"
    const val GROUP_MALAM = "MALAM"
    const val GROUP_LEPAS = "LEPAS"
    const val GROUP_IZIN = "IZIN"

    val ALL_GROUPS = listOf(GROUP_PAGI, GROUP_SORE, GROUP_MALAM, GROUP_LEPAS, GROUP_IZIN)

    const val PIN_TUGAS = "TUGAS"
    const val PIN_FASKES = "FASKES"

    val DEFAULT_PIN_PRESETS = listOf(
        "Jaga Asrama SD",
        "Jaga Asrama SMP",
        "Jaga Asrama SMA",
        "Mendampingi Mengaji Di Masjid",
        "Pencatatan Siswa Sakit",
        "Pendampingan Tugas Khusus",
        "Kontrol Keliling Malam"
    )

    fun currentMonthKey(timeMillis: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        return monthKeyFor(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun currentDayOfMonth(timeMillis: Long = System.currentTimeMillis()): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        return cal.get(Calendar.DAY_OF_MONTH)
    }

    fun currentHourOfDay(timeMillis: Long = System.currentTimeMillis()): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        return cal.get(Calendar.HOUR_OF_DAY)
    }

    fun monthKeyFor(year: Int, month1Based: Int): String {
        val safeYear = year.coerceIn(2020, 2100)
        val safeMonth = month1Based.coerceIn(1, 12)
        return String.format(Locale.US, "%04d-%02d", safeYear, safeMonth)
    }

    fun parseMonthYear(monthKey: String): Pair<Int, Int> {
        val parts = monthKey.trim().split("-")
        val now = Calendar.getInstance()
        val year = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(2020, 2100) ?: now.get(Calendar.YEAR)
        val month = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(1, 12) ?: (now.get(Calendar.MONTH) + 1)
        return year to month
    }

    fun shiftMonth(monthKey: String, deltaMonths: Int): String {
        val (year, month) = parseMonthYear(monthKey)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, deltaMonths)
        }
        return monthKeyFor(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun daysInMonth(monthKey: String): Int {
        val (year, month) = parseMonthYear(monthKey)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun formatMonthTitle(monthKey: String, isEn: Boolean): String {
        val (year, month) = parseMonthYear(monthKey)
        val idMonths = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val enMonths = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val mLabel = if (isEn) enMonths[month - 1] else idMonths[month - 1]
        return "$mLabel $year"
    }

    fun dayOfWeekShort(monthKey: String, day: Int, isEn: Boolean = false): String {
        val (year, month) = parseMonthYear(monthKey)
        val maxDay = daysInMonth(monthKey)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day.coerceIn(1, maxDay))
        }
        val idDays = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        val enDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val idx = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY).coerceIn(0, 6)
        return if (isEn) enDays[idx] else idDays[idx]
    }

    fun formatDateChipLabel(monthKey: String, day: Int, isEn: Boolean): String {
        val (year, month) = parseMonthYear(monthKey)
        val shortMonthsId = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
        val shortMonthsEn = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val dow = dayOfWeekShort(monthKey, day, isEn)
        val mName = if (isEn) shortMonthsEn[month - 1] else shortMonthsId[month - 1]
        return "$dow, $day $mName $year"
    }

    fun normalizeCode(code: String): String = code.trim().uppercase()

    fun defaultShiftCodes(schoolId: String): List<WaliShiftCode> {
        val cleanSchool = schoolId.trim().ifBlank { "SRT1-KDR" }
        val now = System.currentTimeMillis()
        return listOf(
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "P1",
                name = "Jaga Pagi 1",
                shiftGroup = GROUP_PAGI,
                zoneLabel = "UKS / Mobile Asrama",
                timeRange = "07:00 - 15:00",
                startHour = 7,
                endHour = 15,
                workHours = 8,
                colorHex = "#0284C7",
                sopNotes = "Piket pagi utama (07:00-15:00): apel pagi, pendampingan asrama/UKS, rujukan faskes, dan serah terima ke shift sore.",
                sortOrder = 1,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "P2",
                name = "Jaga Pagi 2",
                shiftGroup = GROUP_PAGI,
                zoneLabel = "Asrama & Sekolah (08-16)",
                timeRange = "08:00 - 16:00",
                startHour = 8,
                endHour = 16,
                workHours = 8,
                colorHex = "#0EA5E9",
                sopNotes = "Piket pagi reguler 08:00-16:00 mendukung administrasi pengasuhan, UKS, dan kegiatan siang siswa.",
                sortOrder = 2,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "P3",
                name = "Pagi Upacara",
                shiftGroup = GROUP_PAGI,
                zoneLabel = "Pendampingan Upacara",
                timeRange = "07:00 - 16:00",
                startHour = 7,
                endHour = 16,
                workHours = 9,
                colorHex = "#0891B2",
                sopNotes = "Piket pagi khusus hari upacara / agenda seremonial (07:00-16:00) hingga kegiatan siang tuntas.",
                sortOrder = 3,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "P4",
                name = "Pagi Kunjungan",
                shiftGroup = GROUP_PAGI,
                zoneLabel = "Kunjungan Ortu (s.d 20:00)",
                timeRange = "07:00 - 20:00",
                startHour = 7,
                endHour = 20,
                workHours = 13,
                colorHex = "#1D4ED8",
                sopNotes = "Pengalihan shift saat kunjungan/acara: 07:00-15:00 bantu kunjungan; 15:00-20:00 patroli lapangan/jogging track & bantu makan malam sebelum pulang.",
                sortOrder = 4,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "P5",
                name = "Pagi Vokasi",
                shiftGroup = GROUP_PAGI,
                zoneLabel = "Pendampingan Vokasi",
                timeRange = "07:00 - 15:00",
                startHour = 7,
                endHour = 15,
                workHours = 8,
                colorHex = "#0D9488",
                sopNotes = "Pendampingan program vokasi/ekstrakurikuler siswa pukul 07:00-15:00.",
                sortOrder = 5,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "S1A",
                name = "Sore Kantin SD",
                shiftGroup = GROUP_SORE,
                zoneLabel = "Kantin SD",
                timeRange = "15:00 - 23:00",
                startHour = 15,
                endHour = 23,
                workHours = 8,
                colorHex = "#EA580C",
                sopNotes = "Menjaga asrama SD, merawat anak asuh sakit, pengawasan kantin SD, dan memimpin makan malam di SD.",
                sortOrder = 6,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "S2A",
                name = "Sore Kantin SMP",
                shiftGroup = GROUP_SORE,
                zoneLabel = "Kantin SMP",
                timeRange = "15:00 - 23:00",
                startHour = 15,
                endHour = 23,
                workHours = 8,
                colorHex = "#D97706",
                sopNotes = "Menjaga asrama SMP, merawat anak asuh sakit, pengawasan kantin SMP, dan memimpin makan malam di SMP.",
                sortOrder = 7,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "S3A",
                name = "Sore Kantin SMA",
                shiftGroup = GROUP_SORE,
                zoneLabel = "Kantin SMA",
                timeRange = "15:00 - 23:00",
                startHour = 15,
                endHour = 23,
                workHours = 8,
                colorHex = "#C2410C",
                sopNotes = "Menjaga asrama SMA, merawat anak asuh sakit, pengawasan kantin SMA, dan memimpin makan malam di SMA.",
                sortOrder = 8,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "S4A",
                name = "Sore Jaga Masjid",
                shiftGroup = GROUP_SORE,
                zoneLabel = "Masjid & Luar Kantin",
                timeRange = "15:00 - 23:00",
                startHour = 15,
                endHour = 23,
                workHours = 8,
                colorHex = "#F59E0B",
                sopNotes = "Pengarahan dan pendampingan ibadah di masjid serta monitoring area luar kantin dan asrama.",
                sortOrder = 9,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "M1",
                name = "Malam 1 (s.d 00:00)",
                shiftGroup = GROUP_MALAM,
                zoneLabel = "Sesi 1 Malam (Putra)",
                timeRange = "15:00 - 00:00",
                startHour = 15,
                endHour = 0,
                workHours = 8,
                colorHex = "#7C3AED",
                sopNotes = "Bertugas sampai jam 00:00 WIB (Sesi 1 malam - kontrol asrama & ketenangan jam tidur).",
                sortOrder = 10,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "M2",
                name = "Malam 2 (Subuh-07:00)",
                shiftGroup = GROUP_MALAM,
                zoneLabel = "Sesi 2 Subuh (Putri)",
                timeRange = "03:00 - 07:00",
                startHour = 3,
                endHour = 7,
                workHours = 4,
                colorHex = "#9333EA",
                sopNotes = "Bertugas mulai Subuh/03:00 hingga 07:00 WIB: bangunkan santri, salat Subuh, persiapan sekolah & sarapan.",
                sortOrder = 11,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "M3",
                name = "Malam 3 (M3)",
                shiftGroup = GROUP_MALAM,
                zoneLabel = "Pendamping (23:00-07:00)",
                timeRange = "23:00 - 07:00",
                startHour = 23,
                endHour = 7,
                workHours = 8,
                colorHex = "#6D28D9",
                sopNotes = "Piket malam pendamping full shift (23:00-07:00). Datang jam 23:00 wajib keliling asrama & lingkungan sekitar serta kirim dokumentasi ke grup dinas. Pukul 03:00 menjalankan SOP M2.",
                sortOrder = 12,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "LP",
                name = "Lepas Piket",
                shiftGroup = GROUP_LEPAS,
                zoneLabel = "Istirahat Lepas Malam",
                timeRange = "Lepas Piket",
                startHour = 0,
                endHour = 0,
                workHours = 0,
                colorHex = "#15803D",
                sopNotes = "Lepas Piket setelah jaga malam (waktu pemulihan/istirahat resmi).",
                sortOrder = 13,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "L",
                name = "Libur / Off",
                shiftGroup = GROUP_LEPAS,
                zoneLabel = "Istirahat / Off",
                timeRange = "Libur",
                startHour = 0,
                endHour = 0,
                workHours = 0,
                colorHex = "#DC2626",
                sopNotes = "Hari libur mingguan (Off) Wali Asuh.",
                sortOrder = 14,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "IZIN",
                name = "Izin / Sakit",
                shiftGroup = GROUP_IZIN,
                zoneLabel = "Izin / Sakit",
                timeRange = "-",
                startHour = 0,
                endHour = 0,
                workHours = 0,
                colorHex = "#B45309",
                sopNotes = "Wali Asuh berhalangan (Sakit/Izin) dengan persetujuan admin sekolah.",
                sortOrder = 15,
                createdAt = now,
                updatedAt = now
            ),
            WaliShiftCode(
                schoolId = cleanSchool,
                code = "C",
                name = "Cuti Resmi",
                shiftGroup = GROUP_IZIN,
                zoneLabel = "Cuti",
                timeRange = "-",
                startHour = 0,
                endHour = 0,
                workHours = 0,
                colorHex = "#475569",
                sopNotes = "Cuti tahunan / cuti resmi Wali Asuh.",
                sortOrder = 16,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    fun buildShiftMap(shiftCodes: List<WaliShiftCode>, schoolId: String): Map<String, WaliShiftCode> {
        val merged = linkedMapOf<String, WaliShiftCode>()
        defaultShiftCodes(schoolId).forEach { merged[normalizeCode(it.code)] = it }
        shiftCodes.filter { it.isActive }.forEach { merged[normalizeCode(it.code)] = it }
        // Alias "O" -> "L" (Off)
        if ("O" !in merged && "L" in merged) {
            merged["O"] = merged["L"]!!.copy(code = "O")
        }
        return merged
    }

    fun resolveShiftGroup(code: String, shiftMap: Map<String, WaliShiftCode>): String {
        val norm = normalizeCode(code)
        if (norm.isBlank()) return ""
        shiftMap[norm]?.let { return it.shiftGroup.uppercase() }
        return when {
            norm.startsWith("P") -> GROUP_PAGI
            norm.startsWith("S") -> GROUP_SORE
            norm.startsWith("M") -> GROUP_MALAM
            norm == "LP" || norm == "L" || norm == "O" || norm == "OFF" -> GROUP_LEPAS
            norm == "IZIN" || norm == "SAKIT" || norm == "C" || norm == "CUTI" -> GROUP_IZIN
            else -> GROUP_PAGI
        }
    }

    fun isShiftActiveAtHour(shiftCode: WaliShiftCode, hour: Int): Boolean {
        val group = shiftCode.shiftGroup.uppercase()
        if (group == GROUP_LEPAS || group == GROUP_IZIN) return false
        val start = shiftCode.startHour.coerceIn(0, 23)
        val end = shiftCode.endHour.coerceIn(0, 23)
        if (start == end) return shiftCode.workHours > 0
        return if (start < end) {
            hour in start until end
        } else {
            hour >= start || hour < end
        }
    }

    fun parseDayCodes(json: String?): Map<Int, String> {
        if (json.isNullOrBlank()) return emptyMap()
        return try {
            val obj = JSONObject(json)
            val result = linkedMapOf<Int, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val day = k.toIntOrNull() ?: continue
                val code = obj.optString(k, "").trim().uppercase()
                if (day in 1..31 && code.isNotBlank()) {
                    result[day] = code
                }
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun serializeDayCodes(dayCodes: Map<Int, String>): String {
        val obj = JSONObject()
        dayCodes.entries
            .filter { it.key in 1..31 && it.value.isNotBlank() }
            .sortedBy { it.key }
            .forEach { (day, code) ->
                obj.put(day.toString(), code.trim().uppercase())
            }
        return obj.toString()
    }

    fun parseDayPins(json: String?): Map<Int, List<PinnedTaskItem>> {
        if (json.isNullOrBlank()) return emptyMap()
        return try {
            val obj = JSONObject(json)
            val result = linkedMapOf<Int, List<PinnedTaskItem>>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val day = k.toIntOrNull() ?: continue
                val arr = obj.optJSONArray(k) ?: continue
                val list = mutableListOf<PinnedTaskItem>()
                for (i in 0 until arr.length()) {
                    val item = arr.optJSONObject(i) ?: continue
                    val label = item.optString("label", "").trim()
                    if (label.isNotBlank()) {
                        list.add(
                            PinnedTaskItem(
                                label = label,
                                category = item.optString("category", PIN_TUGAS).trim().uppercase()
                                    .ifBlank { PIN_TUGAS },
                                note = item.optString("note", "").trim()
                            )
                        )
                    }
                }
                if (day in 1..31 && list.isNotEmpty()) {
                    result[day] = list
                }
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun serializeDayPins(dayPins: Map<Int, List<PinnedTaskItem>>): String {
        val obj = JSONObject()
        dayPins.entries
            .filter { it.key in 1..31 && it.value.isNotEmpty() }
            .sortedBy { it.key }
            .forEach { (day, items) ->
                val arr = JSONArray()
                items.filter { it.label.isNotBlank() }.forEach { pin ->
                    arr.put(
                        JSONObject().apply {
                            put("label", pin.label.trim())
                            put("category", pin.category.trim().uppercase().ifBlank { PIN_TUGAS })
                            put("note", pin.note.trim())
                        }
                    )
                }
                if (arr.length() > 0) {
                    obj.put(day.toString(), arr)
                }
            }
        return obj.toString()
    }

    fun parseDayLeaves(json: String?): Map<Int, LeaveDayInfo> {
        if (json.isNullOrBlank()) return emptyMap()
        return try {
            val obj = JSONObject(json)
            val result = linkedMapOf<Int, LeaveDayInfo>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val day = k.toIntOrNull() ?: continue
                val item = obj.optJSONObject(k) ?: continue
                val status = item.optString("status", "IZIN").trim().uppercase().ifBlank { "IZIN" }
                val driveUrl = item.optString("driveUrl", "").trim()
                val note = item.optString("note", "").trim()
                if (day in 1..31) {
                    result[day] = LeaveDayInfo(status = status, driveUrl = driveUrl, note = note)
                }
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun serializeDayLeaves(dayLeaves: Map<Int, LeaveDayInfo>): String {
        val obj = JSONObject()
        dayLeaves.entries
            .filter { it.key in 1..31 }
            .sortedBy { it.key }
            .forEach { (day, leave) ->
                obj.put(
                    day.toString(),
                    JSONObject().apply {
                        put("status", leave.status.trim().uppercase().ifBlank { "IZIN" })
                        put("driveUrl", leave.driveUrl.trim())
                        put("note", leave.note.trim())
                    }
                )
            }
        return obj.toString()
    }

    fun matchesWaliEntry(entry: WaliScheduleEntry, username: String?, waliName: String): Boolean {
        val cleanUser = username?.trim().orEmpty()
        val entryUser = entry.waliUsername.trim()
        if (cleanUser.isNotBlank() && entryUser.isNotBlank() && entryUser.equals(cleanUser, ignoreCase = true)) {
            return true
        }
        val cleanName = waliName.trim()
        val entryName = entry.waliName.trim()
        if (cleanName.isNotBlank() && entryName.isNotBlank()) {
            if (entryName.equals(cleanName, ignoreCase = true)) return true
            val strippedEntry = entryName.replace(Regex("^\\[[^\\]]+\\]\\s*"), "").trim()
            if (strippedEntry.equals(cleanName, ignoreCase = true)) return true
        }
        return false
    }

    fun computeRowStats(
        entry: WaliScheduleEntry,
        daysInMonth: Int,
        shiftMap: Map<String, WaliShiftCode>
    ): WaliRowStats {
        val codes = parseDayCodes(entry.dayCodesJson)
        var p1 = 0
        var p2 = 0
        var p3 = 0
        var p4 = 0
        var p5 = 0
        var pFull = 0
        var sCount = 0
        var mCount = 0
        var lpCount = 0
        var offCount = 0
        var izinCount = 0
        var cutiCount = 0
        var totalJam = 0

        for (day in 1..daysInMonth) {
            val code = codes[day]?.let { normalizeCode(it) }.orEmpty()
            if (code.isBlank()) continue
            val def = shiftMap[code]
            val group = resolveShiftGroup(code, shiftMap)
            totalJam += def?.workHours ?: when (group) {
                GROUP_PAGI, GROUP_SORE, GROUP_MALAM -> 8
                else -> 0
            }
            when {
                code == "P1" -> { p1++; pFull++ }
                code == "P2" -> { p2++; pFull++ }
                code == "P3" -> { p3++; pFull++ }
                code == "P4" -> { p4++; pFull++ }
                code == "P5" -> { p5++; pFull++ }
                group == GROUP_PAGI -> pFull++
                group == GROUP_SORE -> sCount++
                group == GROUP_MALAM -> mCount++
                code == "LP" -> lpCount++
                code == "L" || code == "O" || code == "OFF" -> offCount++
                code == "C" || code == "CUTI" -> cutiCount++
                group == GROUP_IZIN -> izinCount++
            }
        }
        return WaliRowStats(
            pFull = pFull,
            sCount = sCount,
            mCount = mCount,
            lpCount = lpCount,
            offCount = offCount,
            p1Count = p1,
            p2Count = p2,
            p3Count = p3,
            p4Count = p4,
            p5Count = p5,
            izinCount = izinCount,
            cutiCount = cutiCount,
            totalJamKerja = totalJam
        )
    }

    fun computeDayBreakdown(
        entries: List<WaliScheduleEntry>,
        day: Int,
        shiftMap: Map<String, WaliShiftCode>
    ): DayShiftBreakdown {
        var p1 = 0
        var p2 = 0
        var p3 = 0
        var p4 = 0
        var p5 = 0
        var pagiFull = 0
        var s1a = 0
        var s2a = 0
        var s3a = 0
        var s4a = 0
        var totalSore = 0
        var m1 = 0
        var m2 = 0
        var m3 = 0
        var totalMalam = 0
        var cuti = 0
        var offAndLepas = 0
        var izin = 0
        var totalAssigned = 0

        entries.forEach { entry ->
            val code = parseDayCodes(entry.dayCodesJson)[day]?.let { normalizeCode(it) }.orEmpty()
            if (code.isBlank()) return@forEach
            totalAssigned++
            val group = resolveShiftGroup(code, shiftMap)
            when {
                code == "P1" -> { p1++; pagiFull++ }
                code == "P2" -> { p2++; pagiFull++ }
                code == "P3" -> { p3++; pagiFull++ }
                code == "P4" -> { p4++; pagiFull++ }
                code == "P5" -> { p5++; pagiFull++ }
                group == GROUP_PAGI -> pagiFull++
                code == "S1A" -> { s1a++; totalSore++ }
                code == "S2A" -> { s2a++; totalSore++ }
                code == "S3A" -> { s3a++; totalSore++ }
                code == "S4A" -> { s4a++; totalSore++ }
                group == GROUP_SORE -> totalSore++
                code == "M1" -> { m1++; totalMalam++ }
                code == "M2" -> { m2++; totalMalam++ }
                code == "M3" -> { m3++; totalMalam++ }
                group == GROUP_MALAM -> totalMalam++
                code == "C" || code == "CUTI" -> cuti++
                code == "LP" || code == "L" || code == "O" || code == "OFF" -> offAndLepas++
                group == GROUP_IZIN -> izin++
            }
        }
        return DayShiftBreakdown(
            day = day,
            p1 = p1,
            p2 = p2,
            p3 = p3,
            p4 = p4,
            p5 = p5,
            pagiFull = pagiFull,
            s1a = s1a,
            s2a = s2a,
            s3a = s3a,
            s4a = s4a,
            totalSore = totalSore,
            m1 = m1,
            m2 = m2,
            m3 = m3,
            totalMalam = totalMalam,
            cuti = cuti,
            offAndLepas = offAndLepas,
            izin = izin,
            totalAssigned = totalAssigned
        )
    }

    /**
     * Parse tabel CSV / TSV Matriks (tempel dari Excel / Google Sheets):
     * Format baris yang didukung:
     * - Dengan kolom No & Kode:  No, Kode, Nama Wali, 1, 2, ..., 30
     * - Atau langsung Nama Wali: Nama Wali, 1, 2, ..., 30
     * Separator otomatis mendeteksi Tab (`\t`), titik koma (`;`), atau koma (`,`).
     */
    fun parseMatrixCsv(
        rawText: String,
        monthKey: String,
        schoolId: String,
        existingEntries: List<WaliScheduleEntry>,
        waliAccounts: List<WaliAccount> = emptyList(),
        updatedBy: String = "Admin Sekolah"
    ): List<WaliScheduleEntry> {
        val lines = rawText.lines().map { it.trimEnd() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()
        val maxDay = daysInMonth(monthKey)
        val firstLine = lines.first()
        val delimiter = when {
            firstLine.contains('\t') -> '\t'
            firstLine.contains(';') -> ';'
            else -> ','
        }

        val parsedRows = lines.map { line ->
            line.split(delimiter).map { cell -> cell.trim().removeSurrounding("\"") }
        }

        // Cari indeks kolom hari ke-1 pada header (jika ada header angka "1","2",...)
        var startRow = 0
        var day1Col = -1
        var nameCol = 0
        var codeCol = -1
        val header = parsedRows.first()
        val oneIndex = header.indexOfFirst { it == "1" || it == "01" }
        if (oneIndex >= 1) {
            day1Col = oneIndex
            nameCol = oneIndex - 1
            if (oneIndex >= 3) {
                codeCol = 1
                nameCol = 2
            } else if (oneIndex == 2) {
                // Bisa No, Nama -> day 1 di col 2
                nameCol = 1
            }
            startRow = 1
        }

        val now = System.currentTimeMillis()
        val existingByName = existingEntries.associateBy {
            it.waliName.replace(Regex("^\\[[^\\]]+\\]\\s*"), "").trim().lowercase()
        }
        val accountsByName = waliAccounts.associateBy { it.name.trim().lowercase() }

        val output = mutableListOf<WaliScheduleEntry>()
        for (idx in startRow until parsedRows.size) {
            val cols = parsedRows[idx]
            if (cols.size < 2) continue

            val effectiveDay1Col = if (day1Col >= 1) {
                day1Col
            } else {
                // Deteksi otomatis: apakah cols[0] angka No?
                if (cols[0].toIntOrNull() != null && cols.size >= 3) {
                    nameCol = 1
                    2
                } else {
                    nameCol = 0
                    1
                }
            }

            val rawName = cols.getOrNull(nameCol)?.trim().orEmpty()
            if (rawName.isBlank() ||
                rawName.equals("Nama Wali Asuh", ignoreCase = true) ||
                rawName.equals("Nama", ignoreCase = true) ||
                rawName.startsWith("TOTAL", ignoreCase = true) ||
                rawName.startsWith("JUMLAH", ignoreCase = true)
            ) {
                continue
            }

            // Ekstrak kode misal "[L12] Akhmad Fadkhurriza" -> waliCode="L12"
            val bracketMatch = Regex("^\\[([^\\]]+)\\]\\s*(.+)$").find(rawName)
            val parsedCodeFromBracket = bracketMatch?.groupValues?.getOrNull(1)?.trim().orEmpty()
            val cleanName = bracketMatch?.groupValues?.getOrNull(2)?.trim()?.ifBlank { rawName } ?: rawName
            val explicitCode = if (codeCol >= 0) cols.getOrNull(codeCol)?.trim().orEmpty() else ""
            val waliCode = parsedCodeFromBracket.ifBlank { explicitCode }

            val dayMap = linkedMapOf<Int, String>()
            for (d in 1..maxDay) {
                val cellVal = cols.getOrNull(effectiveDay1Col + d - 1)?.trim()?.uppercase().orEmpty()
                if (cellVal.isNotBlank() && cellVal != "-") {
                    dayMap[d] = if (cellVal == "OFF") "L" else cellVal
                }
            }

            val existing = existingByName[cleanName.lowercase()]
            val matchedAccount = accountsByName[cleanName.lowercase()]
            val mergedDayMap = parseDayCodes(existing?.dayCodesJson).toMutableMap()
            mergedDayMap.putAll(dayMap)

            output.add(
                WaliScheduleEntry(
                    id = existing?.id ?: 0L,
                    schoolId = schoolId.trim().ifBlank { "SRT1-KDR" },
                    monthKey = monthKey,
                    waliAccountId = existing?.waliAccountId?.takeIf { it > 0L } ?: (matchedAccount?.id ?: 0L),
                    waliUsername = existing?.waliUsername?.ifBlank { matchedAccount?.username.orEmpty() }
                        ?: matchedAccount?.username.orEmpty(),
                    waliName = cleanName,
                    waliCode = waliCode.ifBlank { existing?.waliCode.orEmpty() },
                    sortOrder = existing?.sortOrder ?: (output.size + 1),
                    dayCodesJson = serializeDayCodes(mergedDayMap),
                    dayPinsJson = existing?.dayPinsJson ?: "{}",
                    dayLeaveJson = existing?.dayLeaveJson ?: "{}",
                    updatedBy = updatedBy,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
            )
        }
        return output
    }

    fun buildDailyWhatsappSummary(
        monthKey: String,
        day: Int,
        schoolName: String,
        entries: List<WaliScheduleEntry>,
        shiftMap: Map<String, WaliShiftCode>,
        isEn: Boolean
    ): String {
        val sb = StringBuilder()
        sb.appendLine(
            if (isEn) "*DAILY WALI ASUH DUTY ROSTER*"
            else "*JADWAL PIKET HARIAN WALI ASUH*"
        )
        sb.appendLine("${schoolName.ifBlank { "Sekolah Rakyat" }} • ${formatDateChipLabel(monthKey, day, isEn)}")
        sb.appendLine("--------------------------------")

        val grouped = linkedMapOf(
            GROUP_PAGI to mutableListOf<Triple<WaliScheduleEntry, String, List<PinnedTaskItem>>>(),
            GROUP_SORE to mutableListOf(),
            GROUP_MALAM to mutableListOf(),
            GROUP_LEPAS to mutableListOf(),
            GROUP_IZIN to mutableListOf()
        )
        entries.sortedBy { it.sortOrder }.forEach { entry ->
            val code = parseDayCodes(entry.dayCodesJson)[day]?.let { normalizeCode(it) }.orEmpty()
            if (code.isBlank()) return@forEach
            val group = resolveShiftGroup(code, shiftMap)
            val pins = parseDayPins(entry.dayPinsJson)[day].orEmpty()
            grouped.getOrPut(group) { mutableListOf() }.add(Triple(entry, code, pins))
        }

        val labels = mapOf(
            GROUP_PAGI to if (isEn) "Morning Shift (P)" else "Jaga Pagi (P)",
            GROUP_SORE to if (isEn) "Afternoon Shift (S)" else "Jaga Sore (S)",
            GROUP_MALAM to if (isEn) "Night Shift (M)" else "Jaga Malam (M)",
            GROUP_LEPAS to if (isEn) "Off / Post-Night (LP/L)" else "Lepas & Libur (LP/L)",
            GROUP_IZIN to if (isEn) "Leave / Sick (IZIN/C)" else "Izin & Cuti"
        )
        grouped.forEach { (grp, list) ->
            if (list.isNotEmpty()) {
                sb.appendLine("\n*${labels[grp]} (${list.size} Org)*")
                list.forEach { (entry, code, pins) ->
                    val zone = shiftMap[code]?.zoneLabel?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
                    val pinText = if (pins.isNotEmpty()) {
                        " -> " + pins.joinToString(", ") { pin ->
                            if (pin.category == PIN_FASKES) "[RS/Faskes] ${pin.label}" else "[Pin] ${pin.label}"
                        }
                    } else ""
                    sb.appendLine("- ${entry.waliName} [$code]$zone$pinText")
                }
            }
        }
        return sb.toString().trim()
    }
}
