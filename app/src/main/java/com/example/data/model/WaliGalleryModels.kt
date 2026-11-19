package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.remote.pocketbase.PocketBaseIdCodec

@Entity(tableName = "wali_gallery_posts")
data class WaliGalleryPost(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val schoolId: String = "SRT1-KDR",
    val waliUsername: String = "",
    val waliName: String = "Wali Asuh",
    val caption: String = "",
    val activityTag: String = WaliGalleryHelper.TAG_KEGIATAN,
    val targetScope: String = WaliGalleryHelper.SCOPE_ALL_MY_STUDENTS,
    val targetStudentIdsCsv: String = "",
    val photoUrl: String = "",
    val photoFallbackUrl: String = "",
    val pbRecordId: String = "",
    val pbCollectionId: String = "",
    val pbFilename: String = "",
    val retentionDays: Int = WaliGalleryHelper.DEFAULT_RETENTION_DAYS,
    val expiresAt: Long = System.currentTimeMillis() + WaliGalleryHelper.DEFAULT_RETENTION_DAYS * WaliGalleryHelper.DAY_MILLIS,
    val expectedRecipientCount: Int = 0,
    val downloadedByCsv: String = "",
    val isMediaPurgedFromServer: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wali_gallery_local_saves")
data class WaliGalleryLocalSave(
    @PrimaryKey val postId: Long = 0L,
    val localGalleryUri: String = "",
    val displayFileName: String = "",
    val savedAt: Long = System.currentTimeMillis()
)

object WaliGalleryHelper {
    const val DAY_MILLIS = 86_400_000L
    const val DEFAULT_RETENTION_DAYS = 14
    val RETENTION_DAY_OPTIONS = listOf(7, 14, 30)

    const val SCOPE_ALL_MY_STUDENTS = "ALL_MY_STUDENTS"
    const val SCOPE_SPECIFIC_STUDENTS = "SPECIFIC_STUDENTS"

    const val TAG_KEGIATAN = "KEGIATAN"
    const val TAG_BELAJAR = "BELAJAR"
    const val TAG_IBADAH = "IBADAH"
    const val TAG_KESEHATAN = "KESEHATAN"
    const val TAG_KEBERSAMAAN = "KEBERSAMAAN"

    val ACTIVITY_TAGS = listOf(
        TAG_KEGIATAN,
        TAG_BELAJAR,
        TAG_IBADAH,
        TAG_KESEHATAN,
        TAG_KEBERSAMAAN
    )

    fun tagLabel(tag: String, isEn: Boolean): String {
        return when (tag.trim().uppercase()) {
            TAG_BELAJAR -> if (isEn) "Learning" else "Belajar"
            TAG_IBADAH -> if (isEn) "Worship" else "Ibadah"
            TAG_KESEHATAN -> if (isEn) "Health" else "Kesehatan"
            TAG_KEBERSAMAAN -> if (isEn) "Togetherness" else "Kebersamaan"
            else -> if (isEn) "Activity" else "Kegiatan"
        }
    }

    fun parseIdCsv(csv: String): Set<Long> {
        if (csv.isBlank()) return emptySet()
        return csv.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .filter { it > 0L }
            .toSet()
    }

    fun formatIdCsv(ids: Collection<Long>): String {
        return ids.filter { it > 0L }
            .distinct()
            .sorted()
            .joinToString(",")
    }

    fun parseRecipientKeys(csv: String): Set<String> {
        if (csv.isBlank()) return emptySet()
        return csv.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()
    }

    fun buildParentRecipientKey(studentId: Long, linkUsername: String = ""): String {
        return when {
            studentId > 0L -> "stu_$studentId"
            linkUsername.isNotBlank() -> "usr_${linkUsername.trim().lowercase()}"
            else -> ""
        }
    }

    fun mergeDownloadedRecipient(existingCsv: String, recipientKey: String): String {
        val cleanKey = recipientKey.trim()
        val existing = parseRecipientKeys(existingCsv).toMutableSet()
        if (cleanKey.isNotBlank()) existing.add(cleanKey)
        return existing.sorted().joinToString(",")
    }

    fun downloadedCount(post: WaliGalleryPost): Int = parseRecipientKeys(post.downloadedByCsv).size

    fun isAllRecipientsDownloaded(post: WaliGalleryPost): Boolean {
        val expected = post.expectedRecipientCount
        if (expected <= 0) return false
        return downloadedCount(post) >= expected
    }

    fun isExpiredOnServer(post: WaliGalleryPost, nowMillis: Long = System.currentTimeMillis()): Boolean {
        return post.expiresAt > 0L && nowMillis >= post.expiresAt
    }

    fun shouldAutoPurgeServerMedia(
        post: WaliGalleryPost,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (post.isDeleted || post.isMediaPurgedFromServer) return false
        val hasServerFile = post.pbFilename.isNotBlank() ||
            post.photoUrl.startsWith("http", ignoreCase = true) ||
            post.photoFallbackUrl.startsWith("http", ignoreCase = true)
        if (!hasServerFile) return false
        return isExpiredOnServer(post, nowMillis) || isAllRecipientsDownloaded(post)
    }

    fun remainingDaysText(expiresAt: Long, nowMillis: Long = System.currentTimeMillis(), isEn: Boolean): String {
        if (expiresAt <= 0L) return if (isEn) "Temporary relay" else "Relay sementara"
        val diff = expiresAt - nowMillis
        if (diff <= 0L) return if (isEn) "Expired on server" else "Kedaluwarsa di server"
        val days = ((diff + DAY_MILLIS - 1L) / DAY_MILLIS).toInt().coerceAtLeast(1)
        return if (isEn) "$days day(s) left on server" else "Sisa $days hari di server"
    }

    fun matchesWaliOwner(
        post: WaliGalleryPost,
        waliUsername: String,
        waliName: String
    ): Boolean {
        val pUser = post.waliUsername.trim()
        val pName = post.waliName.trim()
        val cUser = waliUsername.trim()
        val cName = waliName.trim()
        if (pUser.isNotBlank() && cUser.isNotBlank() && pUser.equals(cUser, ignoreCase = true)) {
            return true
        }
        if (pName.isNotBlank() && cName.isNotBlank() && pName.equals(cName, ignoreCase = true)) {
            return true
        }
        return false
    }

    fun matchesParentStudent(
        post: WaliGalleryPost,
        student: ChildStudent?,
        link: ParentAccessLink?,
        assignedWaliUsername: String,
        assignedWaliName: String
    ): Boolean {
        if (post.isDeleted) return false
        val activeSchool = (student?.schoolId?.ifBlank { null } ?: link?.schoolId).orEmpty().trim()
        if (activeSchool.isNotBlank() && post.schoolId.isNotBlank() &&
            !post.schoolId.equals(activeSchool, ignoreCase = true)
        ) {
            return false
        }
        val studentId = student?.id ?: link?.studentId ?: 0L
        val targetIds = parseIdCsv(post.targetStudentIdsCsv)
        if (post.targetScope.equals(SCOPE_SPECIFIC_STUDENTS, ignoreCase = true)) {
            return studentId > 0L && studentId in targetIds
        }
        // ALL_MY_STUDENTS: cocok jika wali asuh anak sama dengan pemosting ATAU studentId masuk target snapshot
        val waliMatches = matchesWaliOwner(post, assignedWaliUsername, assignedWaliName)
        val idMatches = studentId > 0L && studentId in targetIds
        return waliMatches || idMatches
    }

    fun resolveRecordPbId(post: WaliGalleryPost): String {
        val explicit = post.pbRecordId.trim()
        if (explicit.length == 15) return explicit
        val safeId = if (post.id > 0L) post.id else PocketBaseIdCodec.stablePositiveHash(
            "wali_gallery:${post.schoolId}:${post.waliUsername}:${post.createdAt}"
        )
        return PocketBaseIdCodec.toPbId(safeId)
    }
}
