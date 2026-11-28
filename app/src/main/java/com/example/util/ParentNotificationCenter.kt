package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.FastingRecord
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentMessage
import com.example.data.model.SholatHelper
import com.example.data.model.SholatRecord
import com.example.data.model.SholatStatus
import com.example.data.model.SholatWaktu
import com.example.data.model.StudentReligionHelper
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganTransaction
import com.example.data.model.TandemWaliHelper
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.WaliGalleryHelper
import com.example.data.model.WaliGalleryPost
import com.example.data.repository.HealthRepository
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

enum class ParentNotifCategory(
    val tabKey: String,
    val labelId: String,
    val labelEn: String
) {
    MEDICAL("MEDICAL", "Kesehatan", "Medical"),
    MESSAGE("MESSAGES", "Pesan Wali", "Caregiver Chat"),
    VISIT("VISIT", "Kunjungan", "Visits"),
    MENGAJI("MENGAJI", "Mengaji", "Mengaji"),
    SHOLAT("SHOLAT", "Sholat 5 Waktu", "Prayers"),
    KRISTEN("KRISTEN", "Agama Kristen", "Christian"),
    FASTING("FASTING", "Puasa", "Fasting"),
    HAID("HAID", "Riwayat Haid", "Haid Cycle"),
    TABUNGAN("TABUNGAN", "Tabungan", "Savings"),
    GALLERY("GALLERY", "Galeri Wali", "Gallery")
}

data class ParentNotificationItem(
    val key: String,
    val category: ParentNotifCategory,
    val titleId: String,
    val titleEn: String,
    val bodyId: String,
    val bodyEn: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

data class ParentStudentSnapshot(
    val schoolId: String,
    val schoolShortName: String,
    val studentId: Long,
    val studentName: String,
    val student: ChildStudent?,
    val link: ParentAccessLink?,
    val medicalRecords: List<MedicalRecord>,
    val mengajiRecords: List<MengajiRecord>,
    val sholatRecords: List<SholatRecord>,
    val christianWorshipRecords: List<ChristianWorshipRecord>,
    val fastingRecords: List<FastingRecord>,
    val haidRecords: List<HaidRecord>,
    val tabunganTransactions: List<TabunganTransaction>,
    val visitSessions: List<VisitSession>,
    val visitRecords: List<VisitRecord>,
    val waliGalleryPosts: List<WaliGalleryPost>,
    val parentMessages: List<ParentMessage>
)

object ParentNotificationCenter {

    private const val PREFS_NAME = "waliasuh_parent_notif_prefs"
    private const val KEY_BG_ALERTS_ENABLED = "parent_bg_alerts_enabled"
    private const val MAX_HISTORY_ITEMS = 80
    private const val MAX_KNOWN_KEYS = 1500
    private const val TAG = "ParentNotificationCenter"

    fun isBackgroundAlertsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BG_ALERTS_ENABLED, true)
    }

    fun setBackgroundAlertsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BG_ALERTS_ENABLED, enabled)
            .apply()
    }

    fun scopeKey(schoolId: String, studentId: Long): String {
        val cleanSchool = schoolId.trim().ifBlank { "SRT1-KDR" }.uppercase(Locale.ROOT)
        return "${cleanSchool}_$studentId"
    }

    fun resetBaseline(context: Context, schoolId: String, studentId: Long) {
        if (studentId <= 0L) return
        val scope = scopeKey(schoolId, studentId)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove("baseline_ready_$scope")
            .remove("known_keys_$scope")
            .apply()
    }

    fun loadHistory(context: Context, schoolId: String, studentId: Long): List<ParentNotificationItem> {
        if (studentId <= 0L) return emptyList()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString("history_${scopeKey(schoolId, studentId)}", null).orEmpty()
        if (raw.isBlank()) return emptyList()
        return parseHistoryJson(raw).sortedByDescending { it.timestamp }
    }

    fun markAsRead(context: Context, schoolId: String, studentId: Long, key: String): List<ParentNotificationItem> {
        if (studentId <= 0L || key.isBlank()) return emptyList()
        val current = loadHistory(context, schoolId, studentId)
        val updated = current.map { item ->
            if (item.key == key) item.copy(isRead = true) else item
        }
        saveHistory(context, schoolId, studentId, updated)
        return updated
    }

    fun markAllAsRead(context: Context, schoolId: String, studentId: Long): List<ParentNotificationItem> {
        if (studentId <= 0L) return emptyList()
        val current = loadHistory(context, schoolId, studentId)
        val updated = current.map { it.copy(isRead = true) }
        saveHistory(context, schoolId, studentId, updated)
        return updated
    }

    fun clearHistory(context: Context, schoolId: String, studentId: Long): List<ParentNotificationItem> {
        if (studentId <= 0L) return emptyList()
        saveHistory(context, schoolId, studentId, emptyList())
        return emptyList()
    }

    fun evaluateAndNotify(
        context: Context,
        snapshot: ParentStudentSnapshot,
        triggerSystemNotification: Boolean
    ): List<ParentNotificationItem> {
        if (snapshot.studentId <= 0L) return emptyList()

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val scope = scopeKey(snapshot.schoolId, snapshot.studentId)
        val baselineReadyKey = "baseline_ready_$scope"
        val knownKeysKey = "known_keys_$scope"

        val candidates = buildNotificationCandidates(snapshot)
        val knownKeys = loadKnownKeys(prefs.getString(knownKeysKey, null))

        if (!prefs.getBoolean(baselineReadyKey, false)) {
            val seededKeys = candidates.map { it.key }.toMutableSet().apply { addAll(knownKeys) }
            saveKnownKeys(prefs, knownKeysKey, seededKeys)
            prefs.edit().putBoolean(baselineReadyKey, true).apply()
            return loadHistory(context, snapshot.schoolId, snapshot.studentId)
        }

        val existingHistory = loadHistory(context, snapshot.schoolId, snapshot.studentId)
        val existingHistoryKeys = existingHistory.mapTo(HashSet()) { it.key }
        val newItems = candidates
            .filter { it.key !in knownKeys && it.key !in existingHistoryKeys }
            .sortedByDescending { it.timestamp }

        if (newItems.isEmpty()) {
            return existingHistory
        }

        val updatedKnown = knownKeys.toMutableSet().apply {
            newItems.forEach { add(it.key) }
        }
        saveKnownKeys(prefs, knownKeysKey, updatedKnown)

        val mergedHistory = (newItems + existingHistory)
            .distinctBy { it.key }
            .sortedByDescending { it.timestamp }
            .take(MAX_HISTORY_ITEMS)
        saveHistory(context, snapshot.schoolId, snapshot.studentId, mergedHistory)

        if (triggerSystemNotification && isBackgroundAlertsEnabled(context)) {
            NotificationHelper.showParentPortalUpdateNotification(
                context = context,
                studentName = snapshot.studentName,
                schoolName = snapshot.schoolShortName,
                items = newItems
            )
        }

        return mergedHistory
    }

    fun buildNotificationCandidates(snapshot: ParentStudentSnapshot): List<ParentNotificationItem> {
        val student = snapshot.student
        val link = snapshot.link
        val studentId = snapshot.studentId
        if (studentId <= 0L) return emptyList()

        val isChristian = StudentReligionHelper.isChristian(student, link)
        val isFemale = student != null && StudentClassifier.isFemaleStudent(student)
        val allowMedical = link?.canViewMedical ?: true
        val allowSholat = !isChristian && (link?.canViewSholat ?: true)
        val allowMengaji = !isChristian && (link?.canViewMengaji ?: true)
        val allowFasting = link?.canViewFasting ?: true
        val allowHaid = isFemale && (link?.canViewHaid ?: true)
        val allowTabungan = link?.canViewTabungan ?: true

        val items = mutableListOf<ParentNotificationItem>()

        // 1. Rekam Medis / Kesehatan
        if (allowMedical) {
            snapshot.medicalRecords
                .filter { it.studentId == studentId }
                .forEach { rec ->
                    val statusId = when {
                        rec.isInpatient -> "Rawat Inap"
                        rec.status.equals("COMPLETED", ignoreCase = true) -> "Pemeriksaan Selesai"
                        rec.status.equals("DEPARTED", ignoreCase = true) -> "Sedang Berangkat ke Faskes"
                        rec.status.equals("CANCELLED", ignoreCase = true) -> "Rencana Dibatalkan"
                        else -> "Rencana Berobat Baru"
                    }
                    val statusEn = when {
                        rec.isInpatient -> "Inpatient Care"
                        rec.status.equals("COMPLETED", ignoreCase = true) -> "Treatment Completed"
                        rec.status.equals("DEPARTED", ignoreCase = true) -> "Departed to Clinic"
                        rec.status.equals("CANCELLED", ignoreCase = true) -> "Plan Cancelled"
                        else -> "New Medical Plan"
                    }
                    val ts = rec.editedAt ?: rec.treatmentDate ?: rec.planDate
                    val detailId = listOfNotNull(
                        rec.complaint.takeIf { it.isNotBlank() },
                        rec.faskes.takeIf { it.isNotBlank() }
                    ).joinToString(" • ").ifBlank { "Lihat detail di Riwayat Sakit" }
                    val detailEn = listOfNotNull(
                        rec.complaint.takeIf { it.isNotBlank() },
                        rec.faskes.takeIf { it.isNotBlank() }
                    ).joinToString(" • ").ifBlank { "View details in Medical History" }

                    items.add(
                        ParentNotificationItem(
                            key = "MED:${rec.id}:${rec.status}:${rec.isInpatient}",
                            category = ParentNotifCategory.MEDICAL,
                            titleId = "$statusId • ${snapshot.studentName}",
                            titleEn = "$statusEn • ${snapshot.studentName}",
                            bodyId = detailId,
                            bodyEn = detailEn,
                            timestamp = ts
                        )
                    )
                }
        }

        // 2. Pesan dari Wali Asuh
        snapshot.parentMessages
            .filter {
                it.studentId == studentId &&
                    !it.senderRole.equals("PARENT", ignoreCase = true)
            }
            .forEach { msg ->
                val sender = msg.senderName.ifBlank { msg.waliName.ifBlank { "Wali Asuh" } }
                val bodyPreview = when {
                    msg.messageContent.isNotBlank() -> msg.messageContent.trim()
                    msg.photoUrl.isNotBlank() || msg.photoFallbackUrl.isNotBlank() -> "[Mengirim foto]"
                    else -> "Pesan baru masuk"
                }
                val bodyPreviewEn = when {
                    msg.messageContent.isNotBlank() -> msg.messageContent.trim()
                    msg.photoUrl.isNotBlank() || msg.photoFallbackUrl.isNotBlank() -> "[Sent a photo]"
                    else -> "New message received"
                }
                items.add(
                    ParentNotificationItem(
                        key = "MSG:${msg.id}",
                        category = ParentNotifCategory.MESSAGE,
                        titleId = "Pesan dari $sender",
                        titleEn = "Message from $sender",
                        bodyId = bodyPreview,
                        bodyEn = bodyPreviewEn,
                        timestamp = msg.createdAt
                    )
                )
            }

        // 3. Jadwal & Kehadiran Kunjungan
        val studentDistrict = student?.district?.ifBlank { link?.studentDistrict.orEmpty() }
            ?: link?.studentDistrict.orEmpty()
        snapshot.visitSessions
            .filter { session ->
                session.isActive && session.publishedToParent &&
                    (studentDistrict.isBlank() ||
                        session.getDistrictsList().isEmpty() ||
                        session.getDistrictsList().any { d -> d.equals(studentDistrict, ignoreCase = true) })
            }
            .forEach { session ->
                items.add(
                    ParentNotificationItem(
                        key = "VISIT_SESSION:${session.id}",
                        category = ParentNotifCategory.VISIT,
                        titleId = "Jadwal Kunjungan Baru",
                        titleEn = "New Visit Schedule",
                        bodyId = "${session.title} • ${session.displayTimeRange()}",
                        bodyEn = "${session.title} • ${session.displayTimeRange()}",
                        timestamp = session.parentLaunchedAt.takeIf { it > 0L } ?: session.updatedAt
                    )
                )
            }

        snapshot.visitRecords
            .filter { it.studentId == studentId }
            .forEach { rec ->
                items.add(
                    ParentNotificationItem(
                        key = "VISIT_REC:${rec.id}",
                        category = ParentNotifCategory.VISIT,
                        titleId = "Check-In Kunjungan Tercatat",
                        titleEn = "Visit Check-In Recorded",
                        bodyId = "${rec.sessionTitle.ifBlank { "Sesi Kunjungan" }} • Pengunjung: ${rec.visitorName.ifBlank { "Orang Tua/Wali" }}",
                        bodyEn = "${rec.sessionTitle.ifBlank { "Visit Session" }} • Visitor: ${rec.visitorName.ifBlank { "Parent/Guardian" }}",
                        timestamp = rec.checkedInAt.takeIf { it > 0L } ?: rec.updatedAt
                    )
                )
            }

        // 4. Mengaji
        if (allowMengaji) {
            val matchedMengaji = if (student != null) {
                MengajiHelper.recordsForStudent(snapshot.mengajiRecords, student)
            } else {
                snapshot.mengajiRecords.filter { it.studentId == studentId }
            }
            matchedMengaji.forEach { rec ->
                items.add(
                    ParentNotificationItem(
                        key = "MENGAJI:${rec.id}",
                        category = ParentNotifCategory.MENGAJI,
                        titleId = "Catatan Mengaji Baru",
                        titleEn = "New Mengaji Record",
                        bodyId = "${rec.bookType} ${rec.jilidOrJuz} (${rec.pageOrVerse}) • ${rec.fluencyStatus}",
                        bodyEn = "${rec.bookType} ${rec.jilidOrJuz} (${rec.pageOrVerse}) • ${rec.fluencyStatus}",
                        timestamp = rec.updatedAt.coerceAtLeast(rec.sessionDate)
                    )
                )
            }
        }

        // 5. Sholat 5 Waktu
        if (allowSholat) {
            val matchedSholat = if (student != null) {
                SholatHelper.recordsForStudent(snapshot.sholatRecords, student)
            } else {
                snapshot.sholatRecords.filter { it.studentId == studentId }
            }
            matchedSholat.forEach { rec ->
                val waktuObj = SholatWaktu.fromCode(rec.waktu)
                val statusObj = SholatStatus.fromCode(rec.status)
                items.add(
                    ParentNotificationItem(
                        key = "SHOLAT:${rec.dateKey}:${rec.waktu}:${rec.status}",
                        category = ParentNotifCategory.SHOLAT,
                        titleId = "Sholat ${waktuObj.labelId} (${rec.dateKey})",
                        titleEn = "${waktuObj.labelEn} Prayer (${rec.dateKey})",
                        bodyId = "Status: ${statusObj.labelId}${rec.notes.takeIf { it.isNotBlank() }?.let { " • $it" }.orEmpty()}",
                        bodyEn = "Status: ${statusObj.labelEn}${rec.notes.takeIf { it.isNotBlank() }?.let { " • $it" }.orEmpty()}",
                        timestamp = rec.updatedAt.coerceAtLeast(rec.createdAt)
                    )
                )
            }
        }

        // 6. Ibadah Siswa Kristen
        if (isChristian) {
            val matchedChristian = if (student != null) {
                ChristianWorshipHelper.recordsForStudent(snapshot.christianWorshipRecords, student)
            } else {
                snapshot.christianWorshipRecords.filter { !it.isDeleted && it.studentId == studentId }
            }
            matchedChristian.forEach { rec ->
                val typeLabelId = ChristianWorshipHelper.activityShortLabel(rec.activityType, false)
                val typeLabelEn = ChristianWorshipHelper.activityShortLabel(rec.activityType, true)
                items.add(
                    ParentNotificationItem(
                        key = "KRISTEN:${rec.id}",
                        category = ParentNotifCategory.KRISTEN,
                        titleId = "Catatan $typeLabelId",
                        titleEn = "$typeLabelEn Update",
                        bodyId = rec.title.ifBlank { rec.notes.ifBlank { typeLabelId } },
                        bodyEn = rec.title.ifBlank { rec.notes.ifBlank { typeLabelEn } },
                        timestamp = rec.updatedAt.coerceAtLeast(rec.activityDate)
                    )
                )
            }
        }

        // 7. Puasa
        if (allowFasting) {
            snapshot.fastingRecords
                .filter { it.studentId == studentId && (it.isSahurChecked || it.isBukaChecked) }
                .forEach { rec ->
                    val statusPartsId = buildList {
                        if (rec.isSahurChecked) add("Sahur")
                        if (rec.isBukaChecked) add("Buka Puasa")
                    }
                    val statusPartsEn = buildList {
                        if (rec.isSahurChecked) add("Suhoor")
                        if (rec.isBukaChecked) add("Iftar")
                    }
                    items.add(
                        ParentNotificationItem(
                            key = "FASTING:${rec.id}:${rec.isSahurChecked}:${rec.isBukaChecked}",
                            category = ParentNotifCategory.FASTING,
                            titleId = "Absen Puasa • ${rec.sessionTitle.ifBlank { "Sesi Puasa" }}",
                            titleEn = "Fasting Log • ${rec.sessionTitle.ifBlank { "Fasting Session" }}",
                            bodyId = "Tercatat: ${statusPartsId.joinToString(" & ")}",
                            bodyEn = "Recorded: ${statusPartsEn.joinToString(" & ")}",
                            timestamp = rec.updatedAt.coerceAtLeast(rec.sessionDate)
                        )
                    )
                }
        }

        // 8. Haid & Suci
        if (allowHaid) {
            snapshot.haidRecords
                .filter { rec ->
                    rec.studentId == studentId ||
                        (student != null && (
                            (rec.studentNik.isNotBlank() && student.nik.isNotBlank() && rec.studentNik == student.nik) ||
                                rec.studentName.equals(student.name, ignoreCase = true)
                            ))
                }
                .forEach { rec ->
                    val isSuci = rec.status.startsWith("SUCI", ignoreCase = true)
                    items.add(
                        ParentNotificationItem(
                            key = "HAID:${rec.id}:${rec.status}",
                            category = ParentNotifCategory.HAID,
                            titleId = if (isSuci) "Status Suci Tercatat" else "Catatan Siklus Haid",
                            titleEn = if (isSuci) "Purity Status Recorded" else "Menstrual Cycle Log",
                            bodyId = rec.notes.ifBlank { if (isSuci) "Ananda sudah suci kembali" else "Keluhan/gejala: ${rec.symptoms}" },
                            bodyEn = rec.notes.ifBlank { if (isSuci) "Cycle marked completed" else "Symptoms: ${rec.symptoms}" },
                            timestamp = rec.updatedAt.coerceAtLeast(rec.startDate)
                        )
                    )
                }
        }

        // 9. Tabungan
        if (allowTabungan) {
            val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
                maximumFractionDigits = 0
            }
            snapshot.tabunganTransactions
                .filter { it.studentId == studentId }
                .forEach { tx ->
                    val amountText = rupiahFormat.format(tx.amount)
                    val isCancelled = tx.status.equals(TabunganHelper.STATUS_CANCELLED, ignoreCase = true)
                    val isDeposit = tx.transactionType.equals(TabunganHelper.TYPE_IN, ignoreCase = true)
                    val titleId = when {
                        isCancelled -> "Transaksi Tabungan Dibatalkan"
                        isDeposit -> "Setoran Tabungan +$amountText"
                        else -> "Penarikan Tabungan -$amountText"
                    }
                    val titleEn = when {
                        isCancelled -> "Savings Transaction Cancelled"
                        isDeposit -> "Savings Deposit +$amountText"
                        else -> "Savings Withdrawal -$amountText"
                    }
                    items.add(
                        ParentNotificationItem(
                            key = "TABUNGAN:${tx.id}:${tx.status}",
                            category = ParentNotifCategory.TABUNGAN,
                            titleId = titleId,
                            titleEn = titleEn,
                            bodyId = tx.title.ifBlank { tx.notes.ifBlank { "Ketuk untuk melihat saldo tabungan" } },
                            bodyEn = tx.title.ifBlank { tx.notes.ifBlank { "Tap to view savings balance" } },
                            timestamp = tx.updatedAt.coerceAtLeast(tx.transactionDate)
                        )
                    )
                }
        }

        // 10. Galeri Wali Asuh
        val assignedWaliUsername = student?.assignedWaliUsername?.ifBlank { link?.assignedWaliUsername.orEmpty() }
            ?: link?.assignedWaliUsername.orEmpty()
        val assignedWaliName = student?.assignedWaliName?.ifBlank { link?.assignedWaliName.orEmpty() }
            ?: link?.assignedWaliName.orEmpty()
        val assignedRefs = TandemWaliHelper.allAssignedWaliRefs(student, link)
        snapshot.waliGalleryPosts
            .filter { post ->
                !post.isDeleted &&
                    WaliGalleryHelper.matchesParentStudent(
                        post = post,
                        student = student,
                        link = link,
                        assignedWaliUsername = assignedWaliUsername,
                        assignedWaliName = assignedWaliName,
                        assignedWalis = assignedRefs
                    )
            }
            .forEach { post ->
                items.add(
                    ParentNotificationItem(
                        key = "GALLERY:${post.id}",
                        category = ParentNotifCategory.GALLERY,
                        titleId = "Foto Baru di Galeri Wali Asuh",
                        titleEn = "New Photo in Caregiver Gallery",
                        bodyId = post.caption.ifBlank { "${post.waliName} membagikan momen kegiatan ${post.activityTag}" },
                        bodyEn = post.caption.ifBlank { "${post.waliName} shared an activity photo (${post.activityTag})" },
                        timestamp = post.updatedAt.coerceAtLeast(post.createdAt)
                    )
                )
            }

        return items.sortedByDescending { it.timestamp }
    }

    suspend fun fromRoomSnapshot(
        repository: HealthRepository,
        schoolId: String,
        studentId: Long,
        username: String = "",
        schoolShortName: String = ""
    ): ParentStudentSnapshot? {
        if (studentId <= 0L) return null
        val cleanSchool = schoolId.trim().ifBlank { "SRT1-KDR" }
        val links = repository.getParentAccessLinksBySchoolList(cleanSchool)
        val link = links.firstOrNull { it.studentId == studentId }
            ?: links.firstOrNull { username.isNotBlank() && it.username.equals(username, ignoreCase = true) }
        val resolvedStudentId = link?.studentId ?: studentId
        val student = repository.getStudentById(resolvedStudentId)
            ?: repository.getStudentsBySchoolList(cleanSchool).firstOrNull { it.id == resolvedStudentId }
        val studentName = student?.name?.ifBlank { link?.studentName.orEmpty() }
            ?: link?.studentName?.ifBlank { "Ananda" }
            ?: "Ananda"

        return ParentStudentSnapshot(
            schoolId = cleanSchool,
            schoolShortName = schoolShortName,
            studentId = resolvedStudentId,
            studentName = studentName,
            student = student,
            link = link,
            medicalRecords = repository.getAllRecordsList(),
            mengajiRecords = repository.getMengajiRecordsBySchoolList(cleanSchool),
            sholatRecords = repository.getSholatRecordsBySchoolList(cleanSchool),
            christianWorshipRecords = repository.getChristianWorshipRecordsBySchoolList(cleanSchool),
            fastingRecords = repository.getFastingRecordsBySchoolList(cleanSchool),
            haidRecords = repository.getHaidRecordsBySchoolList(cleanSchool),
            tabunganTransactions = repository.getTabunganTransactionsForStudentList(resolvedStudentId, cleanSchool),
            visitSessions = repository.getVisitSessionsBySchoolList(cleanSchool),
            visitRecords = repository.getVisitRecordsBySchoolList(cleanSchool),
            waliGalleryPosts = repository.getActiveWaliGalleryPostsBySchoolList(cleanSchool),
            parentMessages = repository.getParentMessagesForStudentList(resolvedStudentId, cleanSchool)
        )
    }

    private fun loadKnownKeys(raw: String?): Set<String> {
        if (raw.isNullOrBlank()) return emptySet()
        return runCatching {
            val arr = JSONArray(raw)
            buildSet {
                for (i in 0 until arr.length()) {
                    val value = arr.optString(i)
                    if (value.isNotBlank()) add(value)
                }
            }
        }.getOrElse { emptySet() }
    }

    private fun saveKnownKeys(prefs: android.content.SharedPreferences, prefKey: String, keys: Set<String>) {
        val trimmed = if (keys.size > MAX_KNOWN_KEYS) keys.toList().takeLast(MAX_KNOWN_KEYS) else keys.toList()
        val arr = JSONArray()
        trimmed.forEach { arr.put(it) }
        prefs.edit().putString(prefKey, arr.toString()).apply()
    }

    private fun saveHistory(
        context: Context,
        schoolId: String,
        studentId: Long,
        items: List<ParentNotificationItem>
    ) {
        val arr = JSONArray()
        items.take(MAX_HISTORY_ITEMS).forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("key", item.key)
                    put("category", item.category.name)
                    put("titleId", item.titleId)
                    put("titleEn", item.titleEn)
                    put("bodyId", item.bodyId)
                    put("bodyEn", item.bodyEn)
                    put("timestamp", item.timestamp)
                    put("isRead", item.isRead)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("history_${scopeKey(schoolId, studentId)}", arr.toString())
            .apply()
    }

    private fun parseHistoryJson(raw: String): List<ParentNotificationItem> {
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val key = obj.optString("key")
                    val catName = obj.optString("category")
                    val category = ParentNotifCategory.entries.firstOrNull { it.name == catName }
                        ?: ParentNotifCategory.MEDICAL
                    if (key.isNotBlank()) {
                        add(
                            ParentNotificationItem(
                                key = key,
                                category = category,
                                titleId = obj.optString("titleId"),
                                titleEn = obj.optString("titleEn"),
                                bodyId = obj.optString("bodyId"),
                                bodyEn = obj.optString("bodyEn"),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                                isRead = obj.optBoolean("isRead", false)
                            )
                        )
                    }
                }
            }
        }.onFailure { err ->
            Log.w(TAG, "Failed parsing parent notification history: ${err.message}")
        }.getOrElse { emptyList() }
    }
}
