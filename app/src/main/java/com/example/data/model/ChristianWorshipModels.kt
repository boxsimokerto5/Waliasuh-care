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

object ChristianWorshipHelper {
    const val RETENTION_DAYS = 90L
    const val DAY_MILLIS = 86_400_000L
    const val RETENTION_MILLIS = RETENTION_DAYS * DAY_MILLIS

    val ALL_TYPES = listOf(
        ChristianWorshipRecord.TYPE_DOA_PAGI,
        ChristianWorshipRecord.TYPE_DOA_MALAM,
        ChristianWorshipRecord.TYPE_KE_GEREJA
    )

    fun computeExpiresAt(uploadedAt: Long): Long = uploadedAt + RETENTION_MILLIS

    fun normalizeType(raw: String?): String {
        return when (raw?.trim()?.uppercase()) {
            ChristianWorshipRecord.TYPE_DOA_MALAM, "MALAM" -> ChristianWorshipRecord.TYPE_DOA_MALAM
            ChristianWorshipRecord.TYPE_KE_GEREJA, "GEREJA" -> ChristianWorshipRecord.TYPE_KE_GEREJA
            else -> ChristianWorshipRecord.TYPE_DOA_PAGI
        }
    }

    fun activityLabel(type: String, isEn: Boolean): String {
        return when (normalizeType(type)) {
            ChristianWorshipRecord.TYPE_DOA_PAGI -> if (isEn) "Morning Prayer" else "Doa Ibadah Pagi"
            ChristianWorshipRecord.TYPE_DOA_MALAM -> if (isEn) "Evening Prayer" else "Doa Ibadah Malam"
            ChristianWorshipRecord.TYPE_KE_GEREJA -> if (isEn) "Church Service" else "Kegiatan ke Gereja"
            else -> if (isEn) "Worship Activity" else "Kegiatan Ibadah"
        }
    }

    fun activityShortLabel(type: String, isEn: Boolean): String {
        return when (normalizeType(type)) {
            ChristianWorshipRecord.TYPE_DOA_PAGI -> if (isEn) "Morning" else "Doa Pagi"
            ChristianWorshipRecord.TYPE_DOA_MALAM -> if (isEn) "Evening" else "Doa Malam"
            ChristianWorshipRecord.TYPE_KE_GEREJA -> if (isEn) "Church" else "Ke Gereja"
            else -> if (isEn) "Worship" else "Ibadah"
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
            (record.photoUrl.isNotBlank() || record.photoFallbackUrl.isNotBlank())
    }

    fun hasAnyPhoto(record: ChristianWorshipRecord): Boolean {
        return !record.isMediaPurgedFromServer &&
            (record.photoUrl.isNotBlank() || record.photoFallbackUrl.isNotBlank() || record.localPhotoPath.isNotBlank())
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
