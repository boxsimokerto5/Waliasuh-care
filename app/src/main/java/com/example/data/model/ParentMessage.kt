package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.remote.pocketbase.PocketBaseIdCodec

@Entity(tableName = "parent_messages")
data class ParentMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String = "",
    val parentName: String = "",
    val waliUsername: String = "",
    val waliName: String = "",
    val senderRole: String = "WALI", // "WALI" or "PARENT"
    val senderName: String = "",
    val messageContent: String = "",
    val isReadByWali: Boolean = false,
    val isReadByParent: Boolean = false,
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis(),
    val photoUrl: String = "",
    val photoFallbackUrl: String = "",
    val pbRecordId: String = "",
    val pbCollectionId: String = "",
    val pbFilename: String = "",
    val isDownloadedByRecipient: Boolean = false,
    val isMediaPurgedFromServer: Boolean = false,
    val updatedAt: Long = 0L
)

/**
 * Menyimpan referensi URI lokal di Galeri HP (Pictures/WaliAsuhCare/) untuk foto chat 2 arah
 * agar foto tetap dapat dilihat walau relay sementara di PocketBase / Cloudflare R2 sudah dibersihkan.
 */
@Entity(tableName = "chat_media_local_saves")
data class ChatMediaLocalSave(
    @PrimaryKey val messageId: Long,
    val localGalleryUri: String = "",
    val displayFileName: String = "",
    val savedAt: Long = System.currentTimeMillis()
)

object ParentMessageHelper {
    const val AUTO_PURGE_FALLBACK_DAYS = 14
    const val DAY_MILLIS = 86_400_000L

    fun hasServerMedia(msg: ParentMessage): Boolean {
        if (msg.isMediaPurgedFromServer) return false
        return msg.photoUrl.isNotBlank() || msg.photoFallbackUrl.isNotBlank() || msg.pbFilename.isNotBlank()
    }

    fun isPhotoMessage(msg: ParentMessage, localSave: ChatMediaLocalSave? = null): Boolean {
        return hasServerMedia(msg) || msg.isMediaPurgedFromServer || localSave != null
    }

    fun shouldAutoPurgeServerMedia(
        msg: ParentMessage,
        now: Long = System.currentTimeMillis()
    ): Boolean {
        if (!hasServerMedia(msg)) return false
        if (msg.isDownloadedByRecipient) return true
        if (msg.createdAt > 0L && now >= msg.createdAt + AUTO_PURGE_FALLBACK_DAYS * DAY_MILLIS) return true
        return false
    }

    fun resolveRecordPbId(msg: ParentMessage): String {
        val explicit = msg.pbRecordId.trim()
        if (explicit.length == 15) return explicit
        return PocketBaseIdCodec.toPbId(msg.id)
    }

    fun displayPreviewText(msg: ParentMessage, isEn: Boolean): String {
        val cleanText = msg.messageContent.trim()
        if (!isPhotoMessage(msg)) return cleanText
        val photoLabel = if (isEn) "📷 Photo" else "📷 Foto"
        return if (cleanText.isBlank()) photoLabel else "$photoLabel: $cleanText"
    }
}
