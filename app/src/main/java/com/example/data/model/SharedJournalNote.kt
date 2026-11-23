package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class JournalChecklistItem(
    val id: String = UUID.randomUUID().toString().take(8),
    val text: String = "",
    val isChecked: Boolean = false
)

data class JournalImageAttachment(
    val id: String = UUID.randomUUID().toString().take(10),
    val url: String = "",
    val fallbackUrl: String = "",
    val localPath: String = ""
) {
    fun preferredDisplaySource(): String {
        return when {
            url.startsWith("http") -> url
            fallbackUrl.startsWith("http") -> fallbackUrl
            localPath.isNotBlank() -> localPath
            else -> url
        }
    }
}

@Entity(tableName = "shared_journal_notes")
data class SharedJournalNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val schoolId: String = "SRT1-KDR",
    val category: String = SharedJournalHelper.CATEGORY_JURNAL,
    val title: String = "",
    val content: String = "",
    val checklistJson: String = "",
    val imageUrlsJson: String = "",
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

    fun parseChecklist(rawJson: String?): List<JournalChecklistItem> {
        if (rawJson.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(rawJson)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("id", "").ifBlank { "item_$i" }
                    val text = obj.optString("text", "")
                    val checked = obj.optBoolean("checked", false)
                    add(JournalChecklistItem(id = id, text = text, isChecked = checked))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun serializeChecklist(items: List<JournalChecklistItem>): String {
        val cleaned = items.filter { it.text.isNotBlank() || items.size <= 1 }
        if (cleaned.isEmpty()) return ""
        val arr = JSONArray()
        cleaned.forEach { item ->
            arr.put(
                JSONObject()
                    .put("id", item.id)
                    .put("text", item.text)
                    .put("checked", item.isChecked)
            )
        }
        return arr.toString()
    }

    fun parseImages(rawJson: String?): List<JournalImageAttachment> {
        if (rawJson.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(rawJson)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("id", "").ifBlank { "img_$i" }
                    val url = obj.optString("url", "")
                    val fallbackUrl = obj.optString("fallbackUrl", "")
                    val localPath = obj.optString("localPath", "")
                    if (url.isNotBlank() || fallbackUrl.isNotBlank() || localPath.isNotBlank()) {
                        add(
                            JournalImageAttachment(
                                id = id,
                                url = url,
                                fallbackUrl = fallbackUrl,
                                localPath = localPath
                            )
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun serializeImages(images: List<JournalImageAttachment>): String {
        val cleaned = images.filter {
            it.url.isNotBlank() || it.fallbackUrl.isNotBlank() || it.localPath.isNotBlank()
        }
        if (cleaned.isEmpty()) return ""
        val arr = JSONArray()
        cleaned.forEach { img ->
            arr.put(
                JSONObject()
                    .put("id", img.id)
                    .put("url", img.url)
                    .put("fallbackUrl", img.fallbackUrl)
                    .put("localPath", img.localPath)
            )
        }
        return arr.toString()
    }

    fun buildPreviewSummary(
        content: String,
        checklist: List<JournalChecklistItem>,
        isEn: Boolean
    ): String {
        val cleanContent = content.trim().replace("\n", " ")
        if (cleanContent.isNotBlank()) return cleanContent
        val nonBlankItems = checklist.filter { it.text.isNotBlank() }
        if (nonBlankItems.isNotEmpty()) {
            val doneCount = nonBlankItems.count { it.isChecked }
            val prefix = if (isEn) "☑ $doneCount/${nonBlankItems.size} checked" else "☑ $doneCount/${nonBlankItems.size} selesai"
            val firstText = nonBlankItems.first().text.trim()
            return "$prefix • $firstText"
        }
        return if (isEn) "No text" else "Tidak ada teks"
    }

    fun countCharacters(
        title: String,
        content: String,
        checklist: List<JournalChecklistItem>
    ): Int {
        val checklistChars = checklist.sumOf { it.text.trim().length }
        return title.trim().length + content.trim().length + checklistChars
    }

    fun buildShareText(
        note: SharedJournalNote,
        isEn: Boolean
    ): String {
        val checklist = parseChecklist(note.checklistJson).filter { it.text.isNotBlank() }
        val sb = StringBuilder()
        sb.append("[").append(categoryLabel(note.category, isEn)).append("]\n")
        if (note.title.isNotBlank()) {
            sb.append(note.title.trim()).append("\n")
        }
        if (note.content.isNotBlank()) {
            sb.append("\n").append(note.content.trim()).append("\n")
        }
        if (checklist.isNotEmpty()) {
            sb.append("\n")
            checklist.forEach { item ->
                val mark = if (item.isChecked) "☑" else "☐"
                sb.append("$mark ${item.text.trim()}\n")
            }
        }
        sb.append("\n— ${note.authorName} (${formatCompactDate(note.updatedAt, isEn)})")
        return sb.toString().trim()
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
        val fmt = SimpleDateFormat("dd MMM HH:mm", locale)
        return fmt.format(Date(timestamp))
    }

    fun formatEditorTimestamp(timestamp: Long, isEn: Boolean): String {
        if (timestamp <= 0L) return "-"
        val locale = if (isEn) Locale.ENGLISH else Locale("id", "ID")
        val fmt = SimpleDateFormat("dd MMMM  HH:mm", locale)
        return fmt.format(Date(timestamp))
    }
}
