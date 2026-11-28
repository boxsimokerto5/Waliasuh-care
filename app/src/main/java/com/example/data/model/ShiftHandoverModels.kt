package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

@Entity(tableName = "shift_handovers")
data class ShiftHandoverReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val schoolId: String = "SRT1-KDR",
    val monthKey: String = WaliScheduleHelper.currentMonthKey(),
    val day: Int = WaliScheduleHelper.currentDayOfMonth(),
    val reportDateMillis: Long = System.currentTimeMillis(),
    val handoverType: String = ShiftHandoverHelper.TYPE_PAGI_KE_SORE,
    val status: String = ShiftHandoverHelper.STATUS_DRAFT,
    val title: String = "SERAH TERIMA SHIFT PAGI KE SORE",
    val schoolName: String = "SEKOLAH RAKYAT",
    val serviceUnit: String = "Pelayanan Wali Asuh",
    val dateLabel: String = "",
    val handoverTime: String = "15:00 WIB",
    val fromShiftLabel: String = "",
    val toShiftLabel: String = "",
    val totalStudents: Int = 0,
    val presentStudents: Int = 0,
    val sickStudentsCount: Int = 0,
    val fastingStudentsCount: Int = 0,
    val payloadJson: String = "{}",
    val authorUsername: String = "",
    val authorName: String = "Wali Asuh",
    val lastEditedBy: String = "Wali Asuh",
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class HandoverSickStudentItem(
    val id: String = UUID.randomUUID().toString().take(8),
    val studentName: String = "",
    val classGrade: String = "",
    val statusText: String = "Pemulihan di asrama"
)

data class HandoverLeaveItem(
    val id: String = UUID.randomUUID().toString().take(8),
    val waliName: String = "",
    val leaveDetail: String = "",
    val driveUrl: String = ""
)

data class HandoverOutsideDutyItem(
    val id: String = UUID.randomUUID().toString().take(8),
    val waliName: String = "",
    val locationLabel: String = "",
    val shiftInfo: String = ""
)

data class ShiftHandoverPayload(
    val sickStudents: List<HandoverSickStudentItem> = emptyList(),
    val leaveEntries: List<HandoverLeaveItem> = emptyList(),
    val handingOverStaff: List<String> = emptyList(),
    val receivingStaff: List<String> = emptyList(),
    val outsideDutyStaff: List<HandoverOutsideDutyItem> = emptyList(),
    val followUpNotes: String = "",
    val completedActivities: List<String> = emptyList(),
    val footerText: String = "",
    val syncedShiftMessageId: Long = 0L,
    val autoSyncShiftMessage: Boolean = false,
    val haidStudentsCount: Int? = null
)

object ShiftHandoverHelper {
    const val TYPE_PAGI_KE_SORE = "PAGI_KE_SORE"
    const val TYPE_SORE_KE_MALAM = "SORE_KE_MALAM"
    const val TYPE_MALAM_KE_PAGI = "MALAM_KE_PAGI"
    const val TYPE_CUSTOM = "CUSTOM"

    const val STATUS_DRAFT = "DRAFT"
    const val STATUS_FINAL = "FINAL"

    // Retensi otomatis 3 bulan (90 hari) untuk riwayat serah terima shift
    const val RETENTION_DAYS = 90L
    const val RETENTION_MILLIS = RETENTION_DAYS * 24L * 60L * 60L * 1000L

    fun retentionCutoffMillis(nowMillis: Long = System.currentTimeMillis()): Long =
        nowMillis - RETENTION_MILLIS

    fun isExpiredForRetention(
        report: ShiftHandoverReport,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        val cutoff = retentionCutoffMillis(nowMillis)
        val anchorMillis = if (report.reportDateMillis > 0L) {
            report.reportDateMillis
        } else {
            maxOf(report.updatedAt, report.createdAt)
        }
        return anchorMillis < cutoff
    }

    val DEFAULT_ACTIVITY_PRESETS = listOf(
        "Mendata siswa sakit",
        "Mengantarkan siswa sakit ke UKS",
        "Pendampingan makan siang / malam",
        "Pendampingan laundry time siswa",
        "Mendampingi ekstrakurikuler / mengaji",
        "Pendampingan sholat berjamaah",
        "Kontrol keliling asrama"
    )

    fun normalizeType(raw: String?): String {
        return when (raw?.trim()?.uppercase()) {
            TYPE_SORE_KE_MALAM -> TYPE_SORE_KE_MALAM
            TYPE_MALAM_KE_PAGI -> TYPE_MALAM_KE_PAGI
            TYPE_CUSTOM -> TYPE_CUSTOM
            else -> TYPE_PAGI_KE_SORE
        }
    }

    fun normalizeStatus(raw: String?): String {
        return if (raw?.trim()?.uppercase() == STATUS_FINAL) STATUS_FINAL else STATUS_DRAFT
    }

    fun typeDisplayLabel(type: String, isEn: Boolean): String {
        return when (normalizeType(type)) {
            TYPE_SORE_KE_MALAM -> if (isEn) "Afternoon → Night" else "Sore ke Malam"
            TYPE_MALAM_KE_PAGI -> if (isEn) "Night → Morning" else "Malam ke Pagi"
            TYPE_CUSTOM -> if (isEn) "Custom Handover" else "Custom"
            else -> if (isEn) "Morning → Afternoon" else "Pagi ke Sore"
        }
    }

    fun defaultTitleForType(type: String): String {
        return when (normalizeType(type)) {
            TYPE_SORE_KE_MALAM -> "SERAH TERIMA SHIFT SORE KE MALAM"
            TYPE_MALAM_KE_PAGI -> "SERAH TERIMA SHIFT MALAM KE PAGI"
            TYPE_CUSTOM -> "SERAH TERIMA SHIFT WALI ASUH"
            else -> "SERAH TERIMA SHIFT PAGI KE SORE"
        }
    }

    fun defaultTimeForType(type: String): String {
        return when (normalizeType(type)) {
            TYPE_SORE_KE_MALAM -> "23:00 WIB"
            TYPE_MALAM_KE_PAGI -> "07:00 WIB"
            TYPE_CUSTOM -> "15:00 WIB"
            else -> "15:00 WIB"
        }
    }

    fun suggestHandoverTypeForHour(hourOfDay: Int = WaliScheduleHelper.currentHourOfDay()): String {
        return when (hourOfDay) {
            in 4..10 -> TYPE_MALAM_KE_PAGI
            in 11..18 -> TYPE_PAGI_KE_SORE
            else -> TYPE_SORE_KE_MALAM
        }
    }

    enum class ShiftSlot { PAGI, SIANG_SORE, MALAM, ALL }

    fun resolveShiftSlotFromText(raw: String): ShiftSlot {
        val lower = raw.trim().lowercase()
        if (lower.isBlank() || lower.contains("semua") || lower.contains("all")) {
            return ShiftSlot.ALL
        }
        return when {
            lower.contains("pagi") || lower.contains("morning") || lower.contains("07-14") || lower.contains("07.00") || lower.contains("07:00") ->
                ShiftSlot.PAGI
            lower.contains("siang") || lower.contains("sore") || lower.contains("afternoon") || lower.contains("14-21") || lower.contains("15.00") || lower.contains("15:00") ->
                ShiftSlot.SIANG_SORE
            lower.contains("malam") || lower.contains("night") || lower.contains("21-07") || lower.contains("23.00") || lower.contains("23:00") ->
                ShiftSlot.MALAM
            else -> {
                val hourMatch = Regex("""\b(\d{1,2})[:.]\d{2}\b""").find(lower)
                val hour = hourMatch?.groupValues?.getOrNull(1)?.toIntOrNull()
                when (hour) {
                    in 7..13 -> ShiftSlot.PAGI
                    in 14..20 -> ShiftSlot.SIANG_SORE
                    in 21..23, in 0..6 -> ShiftSlot.MALAM
                    else -> ShiftSlot.ALL
                }
            }
        }
    }

    fun resolveHandoverTargetShiftSlot(
        handoverType: String,
        toShiftLabel: String = "",
        handoverTime: String = ""
    ): ShiftSlot {
        val normalized = normalizeType(handoverType)
        if (normalized != TYPE_CUSTOM) {
            return when (normalized) {
                TYPE_PAGI_KE_SORE -> ShiftSlot.SIANG_SORE
                TYPE_SORE_KE_MALAM -> ShiftSlot.MALAM
                TYPE_MALAM_KE_PAGI -> ShiftSlot.PAGI
                else -> ShiftSlot.SIANG_SORE
            }
        }
        val fromLabel = resolveShiftSlotFromText(toShiftLabel)
        if (fromLabel != ShiftSlot.ALL) return fromLabel
        return resolveShiftSlotFromText(handoverTime)
    }

    fun filterShiftMessagesForHandover(
        shiftMessages: List<ShiftMessage>,
        monthKey: String,
        day: Int,
        handoverType: String,
        toShiftLabel: String = "",
        handoverTime: String = "",
        excludeMessageId: Long = 0L,
        includeAllShiftForDay: Boolean = true
    ): List<ShiftMessage> {
        if (shiftMessages.isEmpty()) return emptyList()
        val maxDay = WaliScheduleHelper.daysInMonth(monthKey)
        val safeDay = day.coerceIn(1, maxDay)
        val targetSlot = resolveHandoverTargetShiftSlot(handoverType, toShiftLabel, handoverTime)
        val priorityWeight: (String) -> Int = { priority ->
            when (priority.trim().uppercase()) {
                "URGENT" -> 0
                "SEDANG" -> 1
                else -> 2
            }
        }
        return shiftMessages
            .asSequence()
            .filter { msg ->
                !msg.isCompleted &&
                    (excludeMessageId <= 0L || msg.id != excludeMessageId) &&
                    msg.followUpDate > 0L &&
                    isSameMonthDay(msg.followUpDate, monthKey, safeDay)
            }
            .filter { msg ->
                val msgSlot = resolveShiftSlotFromText(msg.shift)
                when (targetSlot) {
                    ShiftSlot.ALL -> true
                    else -> msgSlot == targetSlot || (includeAllShiftForDay && msgSlot == ShiftSlot.ALL)
                }
            }
            .sortedWith(
                compareBy<ShiftMessage> { priorityWeight(it.priority) }
                    .thenByDescending { it.createdAt }
            )
            .toList()
    }

    fun formatShiftMessagesAsFollowUpNotes(
        messages: List<ShiftMessage>,
        limit: Int = 6,
        bulletPrefix: String = ""
    ): String {
        if (messages.isEmpty()) return ""
        return messages
            .take(limit)
            .mapNotNull { msg ->
                val body = msg.messageContent.trim().ifBlank { msg.title.trim() }
                if (body.isBlank()) {
                    null
                } else {
                    val line = if (msg.title.isNotBlank() && !body.equals(msg.title.trim(), ignoreCase = true)) {
                        "${msg.title.trim()}: $body"
                    } else {
                        body
                    }
                    if (bulletPrefix.isNotEmpty()) "$bulletPrefix$line" else line
                }
            }
            .joinToString("\n")
    }

    fun resolveTargetShiftMessageLabel(handoverType: String, toShiftLabel: String = ""): String {
        return when (normalizeType(handoverType)) {
            TYPE_PAGI_KE_SORE -> "Shift Sore"
            TYPE_SORE_KE_MALAM -> "Shift Malam"
            TYPE_MALAM_KE_PAGI -> "Shift Pagi"
            else -> {
                val lower = toShiftLabel.lowercase()
                when {
                    lower.contains("pagi") -> "Shift Pagi"
                    lower.contains("sore") || lower.contains("siang") -> "Shift Sore"
                    lower.contains("malam") -> "Shift Malam"
                    else -> "Semua Shift"
                }
            }
        }
    }

    fun formatFullIndonesianDate(monthKey: String, day: Int): String {
        val (year, month) = WaliScheduleHelper.parseMonthYear(monthKey)
        val maxDay = WaliScheduleHelper.daysInMonth(monthKey)
        val safeDay = day.coerceIn(1, maxDay)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, safeDay)
        }
        val dayNames = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
        val monthNames = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val dow = dayNames[(cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY).coerceIn(0, 6)]
        val mName = monthNames[(month - 1).coerceIn(0, 11)]
        return "$dow, $safeDay $mName $year"
    }

    fun dateMillisFor(monthKey: String, day: Int): Long {
        val (year, month) = WaliScheduleHelper.parseMonthYear(monthKey)
        val maxDay = WaliScheduleHelper.daysInMonth(monthKey)
        val safeDay = day.coerceIn(1, maxDay)
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, safeDay)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun previousDayInSchedule(
        monthKey: String,
        day: Int,
        allScheduleEntries: List<WaliScheduleEntry>
    ): Triple<String, Int, List<WaliScheduleEntry>> {
        return if (day > 1) {
            val prevDay = day - 1
            Triple(monthKey, prevDay, allScheduleEntries.filter { it.monthKey == monthKey })
        } else {
            val prevMonth = WaliScheduleHelper.shiftMonth(monthKey, -1)
            val prevDay = WaliScheduleHelper.daysInMonth(prevMonth)
            Triple(prevMonth, prevDay, allScheduleEntries.filter { it.monthKey == prevMonth })
        }
    }

    fun parsePayload(rawJson: String?): ShiftHandoverPayload {
        if (rawJson.isNullOrBlank()) return ShiftHandoverPayload()
        return runCatching {
            val obj = JSONObject(rawJson)
            val sickArr = obj.optJSONArray("sickStudents") ?: JSONArray()
            val sickList = buildList {
                for (i in 0 until sickArr.length()) {
                    val item = sickArr.optJSONObject(i) ?: continue
                    add(
                        HandoverSickStudentItem(
                            id = item.optString("id", "").ifBlank { "sick_$i" },
                            studentName = item.optString("studentName", ""),
                            classGrade = item.optString("classGrade", ""),
                            statusText = item.optString("statusText", "")
                        )
                    )
                }
            }
            val leaveArr = obj.optJSONArray("leaveEntries") ?: JSONArray()
            val leaveList = buildList {
                for (i in 0 until leaveArr.length()) {
                    val item = leaveArr.optJSONObject(i) ?: continue
                    add(
                        HandoverLeaveItem(
                            id = item.optString("id", "").ifBlank { "leave_$i" },
                            waliName = item.optString("waliName", ""),
                            leaveDetail = item.optString("leaveDetail", ""),
                            driveUrl = item.optString("driveUrl", "")
                        )
                    )
                }
            }
            val handArr = obj.optJSONArray("handingOverStaff") ?: JSONArray()
            val handList = buildList {
                for (i in 0 until handArr.length()) {
                    val s = handArr.optString(i, "").trim()
                    if (s.isNotBlank()) add(s)
                }
            }
            val recvArr = obj.optJSONArray("receivingStaff") ?: JSONArray()
            val recvList = buildList {
                for (i in 0 until recvArr.length()) {
                    val s = recvArr.optString(i, "").trim()
                    if (s.isNotBlank()) add(s)
                }
            }
            val outArr = obj.optJSONArray("outsideDutyStaff") ?: JSONArray()
            val outList = buildList {
                for (i in 0 until outArr.length()) {
                    val item = outArr.optJSONObject(i) ?: continue
                    add(
                        HandoverOutsideDutyItem(
                            id = item.optString("id", "").ifBlank { "out_$i" },
                            waliName = item.optString("waliName", ""),
                            locationLabel = item.optString("locationLabel", ""),
                            shiftInfo = item.optString("shiftInfo", "")
                        )
                    )
                }
            }
            val actArr = obj.optJSONArray("completedActivities") ?: JSONArray()
            val actList = buildList {
                for (i in 0 until actArr.length()) {
                    val s = actArr.optString(i, "").trim()
                    if (s.isNotBlank()) add(s)
                }
            }
            ShiftHandoverPayload(
                sickStudents = sickList,
                leaveEntries = leaveList,
                handingOverStaff = handList,
                receivingStaff = recvList,
                outsideDutyStaff = outList,
                followUpNotes = obj.optString("followUpNotes", ""),
                completedActivities = actList,
                footerText = obj.optString("footerText", ""),
                syncedShiftMessageId = obj.optLong("syncedShiftMessageId", 0L),
                autoSyncShiftMessage = obj.optBoolean("autoSyncShiftMessage", false),
                haidStudentsCount = if (obj.has("haidStudentsCount") && !obj.isNull("haidStudentsCount")) {
                    obj.optInt("haidStudentsCount", 0).coerceAtLeast(0)
                } else {
                    null
                }
            )
        }.getOrDefault(ShiftHandoverPayload())
    }

    fun serializePayload(payload: ShiftHandoverPayload): String {
        val obj = JSONObject()
        val sickArr = JSONArray().apply {
            payload.sickStudents.filter { it.studentName.isNotBlank() }.forEach { item ->
                put(
                    JSONObject()
                        .put("id", item.id.ifBlank { UUID.randomUUID().toString().take(8) })
                        .put("studentName", item.studentName.trim())
                        .put("classGrade", item.classGrade.trim())
                        .put("statusText", item.statusText.trim())
                )
            }
        }
        val leaveArr = JSONArray().apply {
            payload.leaveEntries.filter { it.waliName.isNotBlank() }.forEach { item ->
                put(
                    JSONObject()
                        .put("id", item.id.ifBlank { UUID.randomUUID().toString().take(8) })
                        .put("waliName", item.waliName.trim())
                        .put("leaveDetail", item.leaveDetail.trim())
                        .put("driveUrl", item.driveUrl.trim())
                )
            }
        }
        val handArr = JSONArray().apply {
            payload.handingOverStaff.map { it.trim() }.filter { it.isNotBlank() }.forEach { put(it) }
        }
        val recvArr = JSONArray().apply {
            payload.receivingStaff.map { it.trim() }.filter { it.isNotBlank() }.forEach { put(it) }
        }
        val outArr = JSONArray().apply {
            payload.outsideDutyStaff.filter { it.waliName.isNotBlank() || it.locationLabel.isNotBlank() }.forEach { item ->
                put(
                    JSONObject()
                        .put("id", item.id.ifBlank { UUID.randomUUID().toString().take(8) })
                        .put("waliName", item.waliName.trim())
                        .put("locationLabel", item.locationLabel.trim())
                        .put("shiftInfo", item.shiftInfo.trim())
                )
            }
        }
        val actArr = JSONArray().apply {
            payload.completedActivities.map { it.trim() }.filter { it.isNotBlank() }.forEach { put(it) }
        }
        obj.put("sickStudents", sickArr)
        obj.put("leaveEntries", leaveArr)
        obj.put("handingOverStaff", handArr)
        obj.put("receivingStaff", recvArr)
        obj.put("outsideDutyStaff", outArr)
        obj.put("followUpNotes", payload.followUpNotes.trim())
        obj.put("completedActivities", actArr)
        obj.put("footerText", payload.footerText.trim())
        if (payload.syncedShiftMessageId > 0L) {
            obj.put("syncedShiftMessageId", payload.syncedShiftMessageId)
        }
        obj.put("autoSyncShiftMessage", payload.autoSyncShiftMessage)
        if (payload.haidStudentsCount != null) {
            obj.put("haidStudentsCount", payload.haidStudentsCount.coerceAtLeast(0))
        }
        return obj.toString()
    }

    private fun shiftGroupShortLabel(group: String): String {
        return when (group) {
            WaliScheduleHelper.GROUP_PAGI -> "Pagi"
            WaliScheduleHelper.GROUP_SORE -> "Sore"
            WaliScheduleHelper.GROUP_MALAM -> "Malam"
            WaliScheduleHelper.GROUP_LEPAS -> "Lepas"
            WaliScheduleHelper.GROUP_IZIN -> "Izin"
            else -> group
        }
    }

    private fun formatLeaveDetailText(info: LeaveDayInfo): String {
        val note = info.note.trim()
        val upperNote = note.uppercase()
        val statusUpper = info.status.trim().uppercase()
        return when {
            upperNote.contains("DINAS LUAR") && note.equals("Dinas Luar", ignoreCase = true) -> "Dinas Luar"
            statusUpper == "SAKIT" && note.isBlank() -> "Izin Sakit"
            statusUpper == "SAKIT" -> "Izin Sakit [Catatan: $note]"
            statusUpper == "CUTI" && note.isBlank() -> "Cuti"
            statusUpper == "CUTI" -> "Cuti [Catatan: $note]"
            note.isBlank() -> "Izin"
            else -> "Izin Keperluan Lain [Catatan: $note]"
        }
    }

    private fun isSameMonthDay(timestampMillis: Long?, monthKey: String, day: Int): Boolean {
        if (timestampMillis == null || timestampMillis <= 0L) return false
        val (targetYear, targetMonth) = WaliScheduleHelper.parseMonthYear(monthKey)
        val cal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
        return cal.get(Calendar.YEAR) == targetYear &&
            (cal.get(Calendar.MONTH) + 1) == targetMonth &&
            cal.get(Calendar.DAY_OF_MONTH) == day
    }

    private fun startOfDayMillis(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun countActiveHaidForDate(
        haidRecords: List<HaidRecord>,
        monthKey: String,
        day: Int
    ): Int {
        if (haidRecords.isEmpty()) return 0
        val (targetYear, targetMonth) = WaliScheduleHelper.parseMonthYear(monthKey)
        val maxDay = WaliScheduleHelper.daysInMonth(monthKey)
        val safeDay = day.coerceIn(1, maxDay)
        val todayCal = Calendar.getInstance()
        val isTargetToday = todayCal.get(Calendar.YEAR) == targetYear &&
            (todayCal.get(Calendar.MONTH) + 1) == targetMonth &&
            todayCal.get(Calendar.DAY_OF_MONTH) == safeDay

        val targetStart = Calendar.getInstance().apply {
            set(Calendar.YEAR, targetYear)
            set(Calendar.MONTH, targetMonth - 1)
            set(Calendar.DAY_OF_MONTH, safeDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val targetEnd = targetStart + 86_399_999L

        return haidRecords.filter { haid ->
            if (isTargetToday) {
                haid.isActive
            } else {
                val haidStartDay = startOfDayMillis(haid.startDate)
                val effEnd = haid.effectiveEndDate
                val haidEndDay = when {
                    effEnd != null -> startOfDayMillis(effEnd) + 86_399_999L
                    haid.isActive -> maxOf(targetEnd, System.currentTimeMillis() + 86_399_999L)
                    else -> startOfDayMillis(haid.startDate) + 86_399_999L
                }
                targetEnd >= haidStartDay && targetStart <= haidEndDay
            }
        }.map { rec ->
            when {
                rec.studentId > 0L -> "id:${rec.studentId}"
                rec.studentNik.isNotBlank() -> "nik:${rec.studentNik.trim()}"
                else -> "name:${rec.studentName.trim().lowercase()}"
            }
        }.distinct().size
    }

    fun buildAutoPopulatedDraft(
        schoolProfile: SchoolProfile,
        monthKey: String,
        day: Int,
        handoverType: String,
        allScheduleEntries: List<WaliScheduleEntry>,
        shiftCodes: List<WaliShiftCode>,
        students: List<ChildStudent>,
        medicalRecords: List<MedicalRecord>,
        fastingRecords: List<FastingRecord>,
        shiftMessages: List<ShiftMessage>,
        authorUsername: String,
        authorName: String,
        existingFollowUpNotes: String = "",
        existingActivities: List<String> = emptyList(),
        haidRecords: List<HaidRecord> = emptyList()
    ): Pair<ShiftHandoverReport, ShiftHandoverPayload> {
        val cleanSchoolId = schoolProfile.tenantCode.trim().ifBlank { "SRT1-KDR" }
        val normalizedType = normalizeType(handoverType)
        val maxDay = WaliScheduleHelper.daysInMonth(monthKey)
        val safeDay = day.coerceIn(1, maxDay)
        val effectiveCodes = shiftCodes.ifEmpty { WaliScheduleHelper.defaultShiftCodes(cleanSchoolId) }
        val shiftMap = WaliScheduleHelper.buildShiftMap(effectiveCodes, cleanSchoolId)

        val monthEntries = allScheduleEntries
            .filter { it.monthKey == monthKey }
            .sortedWith(compareBy<WaliScheduleEntry> { it.sortOrder }.thenBy { it.waliName.lowercase() })

        val (fromGroup, toGroup, fromDay, fromEntries) = when (normalizedType) {
            TYPE_SORE_KE_MALAM -> Quadruple(WaliScheduleHelper.GROUP_SORE, WaliScheduleHelper.GROUP_MALAM, safeDay, monthEntries)
            TYPE_MALAM_KE_PAGI -> {
                val (_, prevDay, prevEntries) = previousDayInSchedule(monthKey, safeDay, allScheduleEntries)
                if (prevEntries.isNotEmpty()) {
                    Quadruple(WaliScheduleHelper.GROUP_MALAM, WaliScheduleHelper.GROUP_PAGI, prevDay, prevEntries)
                } else {
                    Quadruple(WaliScheduleHelper.GROUP_MALAM, WaliScheduleHelper.GROUP_PAGI, safeDay, monthEntries)
                }
            }
            else -> Quadruple(WaliScheduleHelper.GROUP_PAGI, WaliScheduleHelper.GROUP_SORE, safeDay, monthEntries)
        }

        val fromShiftLabel = "Shift ${shiftGroupShortLabel(fromGroup)} (Tgl $fromDay)"
        val toShiftLabel = "Shift ${shiftGroupShortLabel(toGroup)} (Tgl $safeDay)"

        val handingOverStaff = fromEntries.mapNotNull { entry ->
            val code = WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)[fromDay]
                ?.let { WaliScheduleHelper.normalizeCode(it) }.orEmpty()
            if (code.isBlank()) return@mapNotNull null
            if (WaliScheduleHelper.resolveShiftGroup(code, shiftMap) == fromGroup) entry.waliName.trim() else null
        }.distinct()

        val receivingStaff = monthEntries.mapNotNull { entry ->
            val code = WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)[safeDay]
                ?.let { WaliScheduleHelper.normalizeCode(it) }.orEmpty()
            if (code.isBlank()) return@mapNotNull null
            if (WaliScheduleHelper.resolveShiftGroup(code, shiftMap) == toGroup) entry.waliName.trim() else null
        }.distinct()

        // Leave Wali Asuh pada hari laporan
        val leaveEntries = extractScheduleLeavesForDay(
            schoolId = cleanSchoolId,
            monthKey = monthKey,
            day = safeDay,
            allScheduleEntries = monthEntries,
            shiftCodes = effectiveCodes
        )

        // Petugas Jaga di Luar Sekolah Rakyat (Pin Faskes / RS pada hari tersebut)
        val outsideDutyStaff = mutableListOf<HandoverOutsideDutyItem>()
        monthEntries.forEach { entry ->
            val code = WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)[safeDay]
                ?.let { WaliScheduleHelper.normalizeCode(it) }.orEmpty()
            val group = if (code.isNotBlank()) WaliScheduleHelper.resolveShiftGroup(code, shiftMap) else ""
            val pins = WaliScheduleHelper.parseDayPins(entry.dayPinsJson)[safeDay].orEmpty()
            pins.filter { it.category.equals(WaliScheduleHelper.PIN_FASKES, ignoreCase = true) }.forEach { pin ->
                val shiftDesc = buildString {
                    if (group.isNotBlank()) {
                        append("Shif ${shiftGroupShortLabel(group)}")
                        if (code.isNotBlank()) append(" - $code")
                    } else if (code.isNotBlank()) {
                        append("Shif $code")
                    }
                }
                outsideDutyStaff.add(
                    HandoverOutsideDutyItem(
                        waliName = entry.waliName.trim(),
                        locationLabel = pin.label.trim(),
                        shiftInfo = shiftDesc
                    )
                )
            }
        }

        // Siswa sakit / UKS / rawat inap
        val studentById = students.associateBy { it.id }
        val studentByName = students.associateBy { it.name.trim().lowercase() }
        val activeMedical = medicalRecords.filter { rec ->
            rec.status != "CANCELLED" && (
                rec.isInpatient ||
                    rec.status == "PLANNED" ||
                    rec.status == "DEPARTED" ||
                    isSameMonthDay(rec.treatmentDate, monthKey, safeDay) ||
                    isSameMonthDay(rec.planDate, monthKey, safeDay)
                )
        }.sortedWith(
            compareByDescending<MedicalRecord> { it.isInpatient }
                .thenByDescending { it.editedAt ?: it.treatmentDate ?: it.createdAt }
        )

        val seenSickKeys = mutableSetOf<String>()
        val sickItems = mutableListOf<HandoverSickStudentItem>()
        var inpatientUniqueCount = 0

        activeMedical.forEach { rec ->
            val key = if (rec.studentId > 0L) "id:${rec.studentId}" else "name:${rec.studentName.trim().lowercase()}"
            if (!seenSickKeys.add(key)) return@forEach
            val matchedStudent = studentById[rec.studentId] ?: studentByName[rec.studentName.trim().lowercase()]
            val classGrade = matchedStudent?.classGrade?.trim().orEmpty()
            val statusText = when {
                rec.isInpatient -> {
                    inpatientUniqueCount++
                    val faskesText = rec.faskes.trim().ifBlank { "RS Rujukan" }
                    val roomSuffix = rec.inpatientRoom.trim().takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
                    "Ranap di $faskesText$roomSuffix"
                }
                rec.status == "PLANNED" || rec.status == "DEPARTED" -> {
                    val faskesText = rec.faskes.trim().ifBlank { "UKS / Faskes" }
                    val complaintText = rec.complaint.trim().takeIf { it.isNotBlank() }?.let { " - $it" } ?: ""
                    "Periksa/Rujukan $faskesText$complaintText"
                }
                else -> {
                    val noteOrDiagnosis = rec.notes.trim().ifBlank { rec.diagnosis.trim() }.ifBlank { rec.complaint.trim() }
                    if (noteOrDiagnosis.isNotBlank()) noteOrDiagnosis else "Pemulihan di asrama"
                }
            }
            sickItems.add(
                HandoverSickStudentItem(
                    studentName = rec.studentName.trim().uppercase(),
                    classGrade = classGrade,
                    statusText = statusText
                )
            )
        }

        // Siswa berpuasa pada tanggal laporan
        val fastingStudentsCount = fastingRecords.filter { rec ->
            isSameMonthDay(rec.sessionDate, monthKey, safeDay) && (rec.isSahurChecked || rec.isBukaChecked)
        }.map { rec ->
            if (rec.studentId > 0L) "id:${rec.studentId}" else "name:${rec.studentName.trim().lowercase()}"
        }.distinct().size

        // Tindak lanjut otomatis hanya dari pesan shift yang cocok dengan tanggal & shift kerja laporan ini;
        // jika tidak ada pesan yang cocok, dibiarkan kosong.
        val resolvedFollowUp = if (existingFollowUpNotes.isNotBlank()) {
            existingFollowUpNotes
        } else {
            val matchedShiftMsgs = filterShiftMessagesForHandover(
                shiftMessages = shiftMessages,
                monthKey = monthKey,
                day = safeDay,
                handoverType = normalizedType,
                toShiftLabel = toShiftLabel
            )
            formatShiftMessagesAsFollowUpNotes(matchedShiftMsgs)
        }

        val totalStudents = students.size
        val presentStudents = (totalStudents - inpatientUniqueCount).coerceAtLeast(0)
        val shortSchool = schoolProfile.shortName.trim().ifBlank { "SRT 1 Kediri" }
        val fullSchool = schoolProfile.schoolName.trim().ifBlank { shortSchool }.uppercase()
        val now = System.currentTimeMillis()

        val report = ShiftHandoverReport(
            schoolId = cleanSchoolId,
            monthKey = monthKey,
            day = safeDay,
            reportDateMillis = dateMillisFor(monthKey, safeDay),
            handoverType = normalizedType,
            status = STATUS_DRAFT,
            title = defaultTitleForType(normalizedType),
            schoolName = fullSchool,
            serviceUnit = "Pelayanan Wali Asuh",
            dateLabel = formatFullIndonesianDate(monthKey, safeDay),
            handoverTime = defaultTimeForType(normalizedType),
            fromShiftLabel = fromShiftLabel,
            toShiftLabel = toShiftLabel,
            totalStudents = totalStudents,
            presentStudents = presentStudents,
            sickStudentsCount = sickItems.size,
            fastingStudentsCount = fastingStudentsCount,
            authorUsername = authorUsername.trim(),
            authorName = authorName.trim().ifBlank { "Wali Asuh" },
            lastEditedBy = authorName.trim().ifBlank { "Wali Asuh" },
            createdAt = now,
            updatedAt = now
        )
        // Siswi aktif haid pada tanggal laporan
        val haidStudentsCount = countActiveHaidForDate(haidRecords, monthKey, safeDay)

        val payload = ShiftHandoverPayload(
            sickStudents = sickItems,
            leaveEntries = leaveEntries,
            handingOverStaff = handingOverStaff,
            receivingStaff = receivingStaff,
            outsideDutyStaff = outsideDutyStaff,
            followUpNotes = resolvedFollowUp,
            completedActivities = existingActivities,
            footerText = "Laporan Serah Terima disusun oleh Wali Asuh $shortSchool",
            haidStudentsCount = haidStudentsCount
        )
        return report.copy(payloadJson = serializePayload(payload)) to payload
    }

    fun extractScheduleLeavesForDay(
        schoolId: String,
        monthKey: String,
        day: Int,
        allScheduleEntries: List<WaliScheduleEntry>,
        shiftCodes: List<WaliShiftCode>
    ): List<HandoverLeaveItem> {
        if (allScheduleEntries.isEmpty()) return emptyList()
        val cleanSchoolId = schoolId.trim().ifBlank { "SRT1-KDR" }
        val maxDay = WaliScheduleHelper.daysInMonth(monthKey)
        val safeDay = day.coerceIn(1, maxDay)
        val effectiveCodes = shiftCodes.ifEmpty { WaliScheduleHelper.defaultShiftCodes(cleanSchoolId) }
        val shiftMap = WaliScheduleHelper.buildShiftMap(effectiveCodes, cleanSchoolId)

        val monthEntries = allScheduleEntries
            .filter { it.monthKey == monthKey }
            .sortedWith(compareBy<WaliScheduleEntry> { it.sortOrder }.thenBy { it.waliName.lowercase() })

        return monthEntries.mapNotNull { entry ->
            val leaveInfo = WaliScheduleHelper.parseDayLeaves(entry.dayLeaveJson)[safeDay]
            val code = WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)[safeDay]
                ?.let { WaliScheduleHelper.normalizeCode(it) }.orEmpty()
            val group = if (code.isNotBlank()) WaliScheduleHelper.resolveShiftGroup(code, shiftMap) else ""
            when {
                leaveInfo != null -> {
                    val formatted = formatLeaveDetailText(leaveInfo)
                    val fallbackCodeName = shiftMap[code]?.name?.takeIf { it.isNotBlank() }
                    val resolvedDetail = if (formatted == "Izin" && fallbackCodeName != null) {
                        fallbackCodeName
                    } else {
                        formatted
                    }
                    HandoverLeaveItem(
                        waliName = entry.waliName.trim(),
                        leaveDetail = resolvedDetail,
                        driveUrl = leaveInfo.driveUrl.trim()
                    )
                }
                group == WaliScheduleHelper.GROUP_IZIN -> {
                    val shiftName = shiftMap[code]?.name?.takeIf { it.isNotBlank() } ?: "Izin"
                    HandoverLeaveItem(
                        waliName = entry.waliName.trim(),
                        leaveDetail = shiftName,
                        driveUrl = ""
                    )
                }
                else -> null
            }
        }
    }

    private fun isGenericLeaveDetail(detail: String): Boolean {
        val norm = detail.trim().lowercase()
        return norm.isBlank() ||
            norm == "izin" ||
            norm == "izin / sakit" ||
            norm == "izin/sakit" ||
            norm == "cuti" ||
            norm == "cuti resmi"
    }

    fun enrichLeaveEntriesFromSchedule(
        existingLeaves: List<HandoverLeaveItem>,
        schoolId: String,
        monthKey: String,
        day: Int,
        allScheduleEntries: List<WaliScheduleEntry>,
        shiftCodes: List<WaliShiftCode>
    ): List<HandoverLeaveItem> {
        if (existingLeaves.isEmpty() || allScheduleEntries.isEmpty()) return existingLeaves
        val scheduleLeaves = extractScheduleLeavesForDay(
            schoolId = schoolId,
            monthKey = monthKey,
            day = day,
            allScheduleEntries = allScheduleEntries,
            shiftCodes = shiftCodes
        )
        if (scheduleLeaves.isEmpty()) return existingLeaves
        val scheduleByName = scheduleLeaves
            .filter { it.waliName.isNotBlank() }
            .associateBy { it.waliName.trim().lowercase() }

        return existingLeaves.map { item ->
            val key = item.waliName.trim().lowercase()
            val matched = scheduleByName[key] ?: return@map item
            val mergedUrl = item.driveUrl.trim().ifBlank { matched.driveUrl.trim() }
            val mergedDetail = if (
                isGenericLeaveDetail(item.leaveDetail) &&
                !isGenericLeaveDetail(matched.leaveDetail)
            ) {
                matched.leaveDetail
            } else {
                item.leaveDetail
            }
            if (mergedUrl == item.driveUrl && mergedDetail == item.leaveDetail) {
                item
            } else {
                item.copy(leaveDetail = mergedDetail, driveUrl = mergedUrl)
            }
        }
    }

    fun formatWhatsAppReport(
        report: ShiftHandoverReport,
        payload: ShiftHandoverPayload = parsePayload(report.payloadJson),
        allScheduleEntries: List<WaliScheduleEntry> = emptyList(),
        shiftCodes: List<WaliShiftCode> = emptyList(),
        haidRecords: List<HaidRecord> = emptyList()
    ): String {
        val sb = StringBuilder()
        val cleanTitle = report.title.trim().ifBlank { defaultTitleForType(report.handoverType) }.uppercase()
        val cleanSchool = report.schoolName.trim().ifBlank { "SEKOLAH RAKYAT" }.uppercase()
        val cleanUnit = report.serviceUnit.trim().ifBlank { "Pelayanan Wali Asuh" }
        val cleanDate = report.dateLabel.trim().ifBlank { formatFullIndonesianDate(report.monthKey, report.day) }
        val cleanTime = report.handoverTime.trim().ifBlank { defaultTimeForType(report.handoverType) }
        val cleanFrom = report.fromShiftLabel.trim().ifBlank { "Shift Menyerahkan" }
        val cleanTo = report.toShiftLabel.trim().ifBlank { "Shift Menerima" }
        val effectiveHaidCount = (
            payload.haidStudentsCount ?: if (haidRecords.isNotEmpty()) {
                countActiveHaidForDate(haidRecords, report.monthKey, report.day)
            } else {
                0
            }
            ).coerceAtLeast(0)

        sb.appendLine("*📋 LAPORAN $cleanTitle*")
        sb.appendLine("*$cleanSchool*")
        sb.appendLine("*$cleanUnit*")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("📅 *Hari/Tanggal:* $cleanDate")
        sb.appendLine("⏰ *Waktu Serah Terima:* $cleanTime")
        sb.appendLine("🔄 *Pergantian:* *$cleanTitle*")
        sb.appendLine("    • Menyerahkan: $cleanFrom")
        sb.appendLine("    • Menerima: $cleanTo")
        sb.appendLine()
        sb.appendLine("📊 *KONDISI SISWA & ASRAMA:*")
        sb.appendLine("• Total Siswa Asrama: *${report.totalStudents} Anak Asuh*")
        sb.appendLine("• Hadir di Asrama: *${report.presentStudents} Anak Asuh*")
        sb.appendLine("• Sakit / di UKS: *${report.sickStudentsCount} Anak Asuh*")
        sb.appendLine("• Siswa Berpuasa: *${report.fastingStudentsCount} Anak Asuh*")
        sb.appendLine("• Siswi Sedang Haid: *$effectiveHaidCount Anak Asuh*")
        sb.appendLine()
        sb.appendLine("🏥 *DATA SISWA SAKIT / UKS:*")
        val validSick = payload.sickStudents.filter { it.studentName.isNotBlank() }
        if (validSick.isEmpty()) {
            sb.appendLine("  - Tidak ada siswa sakit / di UKS")
        } else {
            validSick.forEachIndexed { index, item ->
                val cls = item.classGrade.trim().takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
                val st = item.statusText.trim().takeIf { it.isNotBlank() }?.let { " - $it" } ?: ""
                sb.appendLine("  ${index + 1}. ${item.studentName.trim()}$cls$st")
            }
        }
        sb.appendLine()
        sb.appendLine("📄 *DATA IZIN WALI ASUH (DILENGKAPI BUKTI):*")
        val enrichedLeaves = if (allScheduleEntries.isNotEmpty() && payload.leaveEntries.isNotEmpty()) {
            enrichLeaveEntriesFromSchedule(
                existingLeaves = payload.leaveEntries,
                schoolId = report.schoolId,
                monthKey = report.monthKey,
                day = report.day,
                allScheduleEntries = allScheduleEntries,
                shiftCodes = shiftCodes
            )
        } else {
            payload.leaveEntries
        }
        val validLeaves = enrichedLeaves.filter { it.waliName.isNotBlank() }
        if (validLeaves.isEmpty()) {
            sb.appendLine("  - Tidak ada Wali Asuh izin hari ini")
        } else {
            validLeaves.forEachIndexed { index, item ->
                val detail = item.leaveDetail.trim().takeIf { it.isNotBlank() }?.let { " - $it" } ?: ""
                sb.appendLine("  ${index + 1}. *${item.waliName.trim()}*$detail")
                sb.appendLine("     🔗 Link Bukti: ${item.driveUrl.trim()}")
            }
        }
        sb.appendLine()
        sb.appendLine("👤 *PETUGAS YANG MENYERAHKAN ($cleanFrom):*")
        val validHand = payload.handingOverStaff.map { it.trim() }.filter { it.isNotBlank() }
        if (validHand.isEmpty()) {
            sb.appendLine("  - Belum ada daftar petugas")
        } else {
            validHand.forEachIndexed { index, name ->
                sb.appendLine("  ${index + 1}. *$name*")
            }
        }
        sb.appendLine()
        sb.appendLine("👥 *PETUGAS YANG MENERIMA ($cleanTo):*")
        val validRecv = payload.receivingStaff.map { it.trim() }.filter { it.isNotBlank() }
        if (validRecv.isEmpty()) {
            sb.appendLine("  - Belum ada daftar petugas")
        } else {
            validRecv.forEachIndexed { index, name ->
                sb.appendLine("  ${index + 1}. *$name*")
            }
        }
        sb.appendLine()
        sb.appendLine("🚑 *PETUGAS JAGA DI LUAR SEKOLAH RAKYAT:*")
        val validOutside = payload.outsideDutyStaff.filter { it.waliName.isNotBlank() || it.locationLabel.isNotBlank() }
        if (validOutside.isEmpty()) {
            sb.appendLine("  - Tidak ada petugas jaga di luar")
        } else {
            validOutside.forEachIndexed { index, item ->
                val loc = item.locationLabel.trim().takeIf { it.isNotBlank() }?.let { " — $it" } ?: ""
                val sh = item.shiftInfo.trim().takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
                sb.appendLine("  ${index + 1}. *${item.waliName.trim()}*$loc$sh")
            }
        }
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("📝 *CATATAN TINDAK LANJUT:*")
        sb.appendLine(payload.followUpNotes.trim().ifBlank { "-" })
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("📌 *KEGIATAN YANG TELAH DILAKSANAKAN:*")
        val validActivities = payload.completedActivities.map { it.trim() }.filter { it.isNotBlank() }
        if (validActivities.isEmpty()) {
            sb.appendLine("  - Belum ada kegiatan tercatat")
        } else {
            validActivities.forEachIndexed { index, act ->
                sb.appendLine("  ${index + 1}. $act")
            }
        }
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        val footer = payload.footerText.trim().ifBlank { "Laporan Serah Terima disusun oleh Wali Asuh" }
        sb.append("_${footer}_")
        return sb.toString()
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
