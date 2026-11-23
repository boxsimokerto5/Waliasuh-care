package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "shared_journal_notes")
data class SharedJournalNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val schoolId: String = "SRT1-KDR",
    val category: String = SharedJournalHelper.CATEGORY_JURNAL,
    val title: String = "",
    val content: String = "",
    val isPinned: Boolean = false,
    val authorUsername: String = "",
    val authorName: String = "Wali Asuh",
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object SharedJournalHelper {
    const val CATEGORY_JURNAL = "JURNAL"
    const val CATEGORY_ATURAN = "ATURAN"

    fun normalizeCategory(raw: String?): String {
        val clean = raw?.trim()?.uppercase().orEmpty()
        return when (clean) {
            CATEGORY_ATURAN, "RULE", "RULES", "ATURAN BERSAMA" -> CATEGORY_ATURAN
            else -> CATEGORY_JURNAL
        }
    }

    fun categoryLabel(category: String, isEn: Boolean): String {
        return when (normalizeCategory(category)) {
            CATEGORY_ATURAN -> if (isEn) "Joint Rule" else "Aturan Bersama"
            else -> if (isEn) "Journal Note" else "Catatan Jurnal"
        }
    }

    fun canManageNote(
        note: SharedJournalNote,
        currentUsername: String?,
        currentWaliName: String?,
        isAdmin: Boolean,
        isAnakSuperAdmin: Boolean
    ): Boolean {
        if (isAdmin || isAnakSuperAdmin) return true
        val user = currentUsername?.trim().orEmpty()
        val authorUser = note.authorUsername.trim()
        if (user.isNotBlank() && authorUser.isNotBlank() && user.equals(authorUser, ignoreCase = true)) {
            return true
        }
        val wali = currentWaliName?.trim().orEmpty()
        val authorDisplay = note.authorName.trim()
        if (authorUser.isBlank() && wali.isNotBlank() && authorDisplay.isNotBlank() && wali.equals(authorDisplay, ignoreCase = true)) {
            return true
        }
        return false
    }

    fun formatCompactDate(timestamp: Long, isEn: Boolean): String {
        if (timestamp <= 0L) return "-"
        val locale = if (isEn) Locale.ENGLISH else Locale("id", "ID")
        val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", locale)
        return fmt.format(Date(timestamp))
    }
}
