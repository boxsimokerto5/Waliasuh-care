package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.remote.pocketbase.PocketBaseIdCodec
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Entity(tableName = "christian_worship_records")
data class ChristianWorshipRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val schoolId: String = "SRT1-KDR",
    val studentId: Long = 0L,
    val studentName: String,
    val studentNik: String = "",
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val activityType: String = TYPE_DOA_PAGI, // "DOA_PAGI" | "DOA_MALAM" | "KE_GEREJA"
    val activityDate: Long = System.currentTimeMillis(),
    val dateKey: String = "",
    val title: String = "",
    val notes: String = "",
    val waliUsername: String = "",
    val waliName: String = "Wali Asuh",
    val photoUrl: String = "",
    val photoFallbackUrl: String = "",
    val localPhotoPath: String = "",
    val pbRecordId: String = "",
    val pbCollectionId: String = "",
    val pbFilename: String = "",
    val proofUploadedAt: Long? = null,
    val proofExpiresAt: Long? = null,
    val proofStatus: String = PROOF_STATUS_NONE, // "NONE" | "ACTIVE" | "EXPIRED"
    val isMediaPurgedFromServer: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_DOA_PAGI = "DOA_PAGI"
        const val TYPE_DOA_MALAM = "DOA_MALAM"
        const val TYPE_KE_GEREJA = "KE_GEREJA"
        const val TYPE_AYAT_HAFALAN = "AYAT_HAFALAN"
        const val TYPE_POKOK_DOA = "POKOK_DOA"
        const val TYPE_IZIN_IBADAH = "IZIN_IBADAH"

        const val PROOF_STATUS_NONE = "NONE"
        const val PROOF_STATUS_ACTIVE = "ACTIVE"
        const val PROOF_STATUS_EXPIRED = "EXPIRED"
    }
}

@Entity(tableName = "christian_worship_local_saves")
data class ChristianWorshipLocalSave(
    @PrimaryKey
    val recordId: Long,
    val localGalleryUri: String,
    val displayFileName: String = "",
    val savedAt: Long = System.currentTimeMillis()
)

data class ChristianPhotoItem(
    val primaryModel: String,
    val remoteUrl: String,
    val fallbackUrl: String,
    val localPath: String,
    val galleryUri: String
)

object ChristianWorshipHelper {
    const val RETENTION_DAYS = 90L
    const val DAY_MILLIS = 86_400_000L
    const val RETENTION_MILLIS = RETENTION_DAYS * DAY_MILLIS
    const val MAX_PHOTOS_PER_ACTIVITY = 2
    private const val MULTI_VALUE_SEPARATOR = "\n"

    val CORE_TYPES = listOf(
        ChristianWorshipRecord.TYPE_DOA_PAGI,
        ChristianWorshipRecord.TYPE_DOA_MALAM,
        ChristianWorshipRecord.TYPE_KE_GEREJA
    )

    val EXTRA_TYPES = listOf(
        ChristianWorshipRecord.TYPE_AYAT_HAFALAN,
        ChristianWorshipRecord.TYPE_POKOK_DOA,
        ChristianWorshipRecord.TYPE_IZIN_IBADAH
    )

    val ALL_TYPES = CORE_TYPES + EXTRA_TYPES

    fun computeExpiresAt(uploadedAt: Long): Long = uploadedAt + RETENTION_MILLIS

    fun encodePhotoValues(
        values: List<String>,
        maxItems: Int = MAX_PHOTOS_PER_ACTIVITY
    ): String {
        return values.asSequence()
            .flatMap { it.split('\n', '\r') }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(maxItems.coerceAtLeast(1))
            .joinToString(MULTI_VALUE_SEPARATOR)
    }

    fun decodePhotoValues(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split('\n', '\r')
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(MAX_PHOTOS_PER_ACTIVITY)
            .toList()
    }

    fun extractPhotoEntries(
        record: ChristianWorshipRecord,
        localSave: ChristianWorshipLocalSave? = null
    ): List<ChristianPhotoItem> {
        if (record.isMediaPurgedFromServer) {
            val savedOnly = decodePhotoValues(localSave?.localGalleryUri)
            return savedOnly.map { uri ->
                ChristianPhotoItem(
                    primaryModel = uri,
                    remoteUrl = "",
                    fallbackUrl = "",
                    localPath = "",
                    galleryUri = uri
                )
            }
        }
        val galleryUris = decodePhotoValues(localSave?.localGalleryUri)
        val localPaths = decodePhotoValues(record.localPhotoPath)
        val remoteUrls = decodePhotoValues(record.photoUrl)
        val fallbackUrls = decodePhotoValues(record.photoFallbackUrl)
        val slotCount = maxOf(
            galleryUris.size,
            localPaths.size,
            remoteUrls.size,
            fallbackUrls.size
        ).coerceAtMost(MAX_PHOTOS_PER_ACTIVITY)

        val result = ArrayList<ChristianPhotoItem>(slotCount)
        for (idx in 0 until slotCount) {
            val gallery = galleryUris.getOrNull(idx).orEmpty()
            val local = localPaths.getOrNull(idx).orEmpty()
            val remote = remoteUrls.getOrNull(idx).orEmpty()
            val fallback = fallbackUrls.getOrNull(idx).orEmpty()
            val primary = gallery.ifBlank { local.ifBlank { remote.ifBlank { fallback } } }
            if (primary.isNotBlank()) {
                result.add(
                    ChristianPhotoItem(
                        primaryModel = primary,
                        remoteUrl = remote,
                        fallbackUrl = fallback,
                        localPath = local,
                        galleryUri = gallery
                    )
                )
            }
        }
        return result
    }

    fun photoCount(
        record: ChristianWorshipRecord,
        localSave: ChristianWorshipLocalSave? = null
    ): Int = extractPhotoEntries(record, localSave).size

    fun remotePhotoCount(record: ChristianWorshipRecord): Int {
        if (record.isMediaPurgedFromServer) return 0
        val remoteCount = decodePhotoValues(record.photoUrl).size
        val fallbackCount = decodePhotoValues(record.photoFallbackUrl).size
        return maxOf(remoteCount, fallbackCount).coerceAtMost(MAX_PHOTOS_PER_ACTIVITY)
    }

    fun hasSavedAllPhotosLocally(
        record: ChristianWorshipRecord,
        localSave: ChristianWorshipLocalSave?
    ): Boolean {
        if (localSave == null) return false
        val savedCount = decodePhotoValues(localSave.localGalleryUri).size
        if (savedCount <= 0) return false
        val expectedCount = maxOf(remotePhotoCount(record), decodePhotoValues(record.localPhotoPath).size, 1)
        return savedCount >= expectedCount
    }

    fun sharesRemotePhotoWith(
        first: ChristianWorshipRecord,
        second: ChristianWorshipRecord
    ): Boolean {
        val urlsA = (decodePhotoValues(first.photoUrl) + decodePhotoValues(first.photoFallbackUrl)).toSet()
        if (urlsA.isEmpty()) return false
        val urlsB = decodePhotoValues(second.photoUrl) + decodePhotoValues(second.photoFallbackUrl)
        return urlsB.any { it in urlsA }
    }

    fun normalizeType(raw: String?): String {
        return when (raw?.trim()?.uppercase()) {
            ChristianWorshipRecord.TYPE_DOA_MALAM, "MALAM", "IBADAH_MALAM" -> ChristianWorshipRecord.TYPE_DOA_MALAM
            ChristianWorshipRecord.TYPE_KE_GEREJA, "GEREJA", "KEGIATAN_GEREJA" -> ChristianWorshipRecord.TYPE_KE_GEREJA
            ChristianWorshipRecord.TYPE_AYAT_HAFALAN, "HAFALAN", "PA", "PENDALAMAN_ALKITAB" -> ChristianWorshipRecord.TYPE_AYAT_HAFALAN
            ChristianWorshipRecord.TYPE_POKOK_DOA, "DOA", "PEMBINAAN_ROHANI" -> ChristianWorshipRecord.TYPE_POKOK_DOA
            ChristianWorshipRecord.TYPE_IZIN_IBADAH, "IZIN", "SAKIT" -> ChristianWorshipRecord.TYPE_IZIN_IBADAH
            else -> ChristianWorshipRecord.TYPE_DOA_PAGI
        }
    }

    fun isExtraType(type: String): Boolean = normalizeType(type) in EXTRA_TYPES

    fun activityLabel(type: String, isEn: Boolean): String {
        return when (normalizeType(type)) {
            ChristianWorshipRecord.TYPE_DOA_PAGI -> if (isEn) "Morning Worship & Prayer" else "Ibadah Pagi"
            ChristianWorshipRecord.TYPE_DOA_MALAM -> if (isEn) "Evening Worship & Prayer" else "Ibadah Malam"
            ChristianWorshipRecord.TYPE_KE_GEREJA -> if (isEn) "Church Service" else "Kegiatan Gereja"
            ChristianWorshipRecord.TYPE_AYAT_HAFALAN -> if (isEn) "Memory Verse & Bible Study" else "Ayat Hafalan & PA"
            ChristianWorshipRecord.TYPE_POKOK_DOA -> if (isEn) "Prayer Requests & Counseling" else "Pokok Doa & Pembinaan"
            ChristianWorshipRecord.TYPE_IZIN_IBADAH -> if (isEn) "Sick / Worship Leave" else "Sakit / Izin Ibadah"
            else -> if (isEn) "Worship Activity" else "Kegiatan Ibadah"
        }
    }

    fun activityShortLabel(type: String, isEn: Boolean): String {
        return when (normalizeType(type)) {
            ChristianWorshipRecord.TYPE_DOA_PAGI -> if (isEn) "Morning" else "Ibadah Pagi"
            ChristianWorshipRecord.TYPE_DOA_MALAM -> if (isEn) "Evening" else "Ibadah Malam"
            ChristianWorshipRecord.TYPE_KE_GEREJA -> if (isEn) "Church" else "Gereja"
            ChristianWorshipRecord.TYPE_AYAT_HAFALAN -> if (isEn) "Verse/PA" else "Ayat/PA"
            ChristianWorshipRecord.TYPE_POKOK_DOA -> if (isEn) "Prayer Note" else "Pokok Doa"
            ChristianWorshipRecord.TYPE_IZIN_IBADAH -> if (isEn) "Sick/Leave" else "Sakit/Izin"
            else -> if (isEn) "Worship" else "Ibadah"
        }
    }

    fun buildDailyWhatsappSummary(
        records: List<ChristianWorshipRecord>,
        students: List<ChildStudent>,
        filterTitle: String,
        waliName: String,
        isEn: Boolean = false
    ): String {
        val active = records.filter { !it.isDeleted }
            .sortedWith(compareByDescending<ChristianWorshipRecord> { it.activityDate }.thenByDescending { it.createdAt })
        val pagi = active.count { normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_PAGI }
        val malam = active.count { normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_MALAM }
        val gereja = active.count { normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_KE_GEREJA }
        val pembinaan = active.count { isExtraType(it.activityType) }
        val dateFmt = SimpleDateFormat("dd MMM", if (isEn) Locale.ENGLISH else Locale("id", "ID"))

        return buildString {
            appendLine(if (isEn) "*CHRISTIAN WORSHIP REPORT*" else "*LAPORAN IBADAH AGAMA KRISTEN*")
            if (filterTitle.isNotBlank()) appendLine("_${filterTitle}_")
            appendLine(if (isEn) "Caregiver: *$waliName*" else "Wali Asuh: *$waliName*")
            appendLine(if (isEn) "Christian Students: *${students.size}* • Total Logs: *${active.size}*" else "Anak Kristen: *${students.size}* • Total Catatan: *${active.size}*")
            appendLine("• ${activityShortLabel(ChristianWorshipRecord.TYPE_DOA_PAGI, isEn)}: *$pagi*")
            appendLine("• ${activityShortLabel(ChristianWorshipRecord.TYPE_DOA_MALAM, isEn)}: *$malam*")
            appendLine("• ${activityShortLabel(ChristianWorshipRecord.TYPE_KE_GEREJA, isEn)}: *$gereja*")
            appendLine("• ${if (isEn) "Bible/Prayer/Leave" else "Ayat/Doa/Izin"}: *$pembinaan*")
            if (active.isNotEmpty()) {
                appendLine()
                appendLine(if (isEn) "*Recent Records:*" else "*Catatan Terbaru:*")
                active.take(15).forEachIndexed { idx, rec ->
                    val dStr = dateFmt.format(Date(rec.activityDate))
                    val typeLbl = activityShortLabel(rec.activityType, isEn)
                    val titlePart = rec.title.takeIf { it.isNotBlank() }?.let { " — $it" }.orEmpty()
                    val count = photoCount(rec)
                    val photoSuffix = if (count > 0) " [📷 $count]" else ""
                    appendLine("${idx + 1}. [$dStr] *${rec.studentName}* ($typeLbl)$titlePart$photoSuffix")
                }
            }
        }
    }

    fun todayDateKey(): String = dateKeyFromMillis(System.currentTimeMillis())

    fun dateKeyFromMillis(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(millis))
    }

    fun monthKeyFromRecord(record: ChristianWorshipRecord): String {
        val key = record.dateKey.ifBlank { dateKeyFromMillis(record.activityDate) }
        return if (key.length >= 7) key.substring(0, 7) else dateKeyFromMillis(record.activityDate).substring(0, 7)
    }

    fun resolveRecordPbId(record: ChristianWorshipRecord): String {
        val existing = record.pbRecordId.trim()
        if (existing.length == 15) return existing
        return PocketBaseIdCodec.toPbId(record.id)
    }

    fun hasRemotePhoto(record: ChristianWorshipRecord): Boolean {
        return !record.isMediaPurgedFromServer &&
            (decodePhotoValues(record.photoUrl).isNotEmpty() || decodePhotoValues(record.photoFallbackUrl).isNotEmpty())
    }

    fun hasAnyPhoto(record: ChristianWorshipRecord): Boolean {
        return !record.isMediaPurgedFromServer &&
            (decodePhotoValues(record.photoUrl).isNotEmpty() ||
                decodePhotoValues(record.photoFallbackUrl).isNotEmpty() ||
                decodePhotoValues(record.localPhotoPath).isNotEmpty())
    }

    fun isProofExpired(record: ChristianWorshipRecord, now: Long = System.currentTimeMillis()): Boolean {
        if (record.isMediaPurgedFromServer || record.proofStatus == ChristianWorshipRecord.PROOF_STATUS_EXPIRED) {
            return true
        }
        val exp = record.proofExpiresAt ?: return false
        return exp <= now
    }

    fun isProofActive(record: ChristianWorshipRecord, now: Long = System.currentTimeMillis()): Boolean {
        if (isProofExpired(record, now)) return false
        return hasAnyPhoto(record)
    }

    fun remainingDays(record: ChristianWorshipRecord, now: Long = System.currentTimeMillis()): Long {
        val exp = record.proofExpiresAt ?: return 0L
        val diff = exp - now
        if (diff <= 0L) return 0L
        return max(1L, (diff + DAY_MILLIS - 1L) / DAY_MILLIS)
    }

    fun matchesStudent(record: ChristianWorshipRecord, student: ChildStudent): Boolean {
        if (record.studentId > 0L && student.id > 0L && record.studentId == student.id) return true
        val rNik = record.studentNik.trim()
        val sNik = student.nik.trim()
        if (rNik.isNotBlank() && sNik.isNotBlank() && rNik == sNik) return true
        val rNis = record.studentNis.trim()
        val sNis = student.nis.trim()
        if (rNis.isNotBlank() && sNis.isNotBlank() && rNis == sNis) return true
        return record.studentName.trim().equals(student.name.trim(), ignoreCase = true)
    }

    fun recordsForStudent(records: List<ChristianWorshipRecord>, student: ChildStudent): List<ChristianWorshipRecord> {
        return records.filter { !it.isDeleted && matchesStudent(it, student) }
            .sortedWith(compareByDescending<ChristianWorshipRecord> { it.activityDate }.thenByDescending { it.createdAt })
    }
}
