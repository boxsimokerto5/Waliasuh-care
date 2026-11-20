package com.example.util

import android.content.Context
import com.example.data.model.QuranArabicFontChoice
import com.example.data.model.QuranBookmark
import com.example.data.model.QuranCatalog
import com.example.data.model.QuranLastRead
import com.example.data.model.QuranNote
import com.example.data.model.QuranReaderTheme
import org.json.JSONArray
import org.json.JSONObject

data class QuranReaderPrefsState(
    val lastRead: QuranLastRead = QuranLastRead(),
    val bookmarks: List<QuranBookmark> = emptyList(),
    val notes: List<QuranNote> = emptyList(),
    val theme: QuranReaderTheme = QuranReaderTheme.IVORY_GOLD,
    val fontChoice: QuranArabicFontChoice = QuranArabicFontChoice.SCHEHERAZADE,
    val arabicFontSizeSp: Int = 28,
    val translationFontSizeSp: Int = 13,
    val showLatin: Boolean = true,
    val showTranslation: Boolean = true,
    val isMushafFocusMode: Boolean = false,
    val selectedQariId: String = "alafasy"
)

object QuranUserPrefsStore {

    private const val PREFS_NAME = "waliasuh_quran_prefs"
    private const val KEY_LAST_READ_SURAH = "last_read_surah"
    private const val KEY_LAST_READ_AYAH = "last_read_ayah"
    private const val KEY_LAST_READ_AT = "last_read_at"
    private const val KEY_BOOKMARKS_JSON = "bookmarks_json"
    private const val KEY_NOTES_JSON = "notes_json"
    private const val KEY_THEME = "reader_theme"
    private const val KEY_FONT = "arabic_font"
    private const val KEY_ARABIC_SIZE = "arabic_size_sp"
    private const val KEY_TRANS_SIZE = "trans_size_sp"
    private const val KEY_SHOW_LATIN = "show_latin"
    private const val KEY_SHOW_TRANS = "show_trans"
    private const val KEY_MUSHAF_FOCUS = "mushaf_focus"
    private const val KEY_QARI_ID = "qari_id"

    fun load(context: Context): QuranReaderPrefsState {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val surahNum = prefs.getInt(KEY_LAST_READ_SURAH, 1).coerceIn(1, 114)
        val ayahNum = prefs.getInt(KEY_LAST_READ_AYAH, 1).coerceAtLeast(1)
        val lastAt = prefs.getLong(KEY_LAST_READ_AT, 0L)
        val spec = QuranCatalog.findSurah(surahNum)
        return QuranReaderPrefsState(
            lastRead = QuranLastRead(
                surahNumber = spec.number,
                surahNameLatin = spec.nameLatin,
                ayahNumber = ayahNum,
                updatedAt = lastAt
            ),
            bookmarks = parseBookmarks(prefs.getString(KEY_BOOKMARKS_JSON, null)),
            notes = parseNotes(prefs.getString(KEY_NOTES_JSON, null)),
            theme = QuranReaderTheme.fromId(prefs.getString(KEY_THEME, QuranReaderTheme.IVORY_GOLD.id)),
            fontChoice = QuranArabicFontChoice.fromId(prefs.getString(KEY_FONT, QuranArabicFontChoice.SCHEHERAZADE.id)),
            arabicFontSizeSp = prefs.getInt(KEY_ARABIC_SIZE, 28).coerceIn(20, 42),
            translationFontSizeSp = prefs.getInt(KEY_TRANS_SIZE, 13).coerceIn(11, 18),
            showLatin = prefs.getBoolean(KEY_SHOW_LATIN, true),
            showTranslation = prefs.getBoolean(KEY_SHOW_TRANS, true),
            isMushafFocusMode = prefs.getBoolean(KEY_MUSHAF_FOCUS, false),
            selectedQariId = prefs.getString(KEY_QARI_ID, "alafasy") ?: "alafasy"
        )
    }

    fun saveSettings(context: Context, state: QuranReaderPrefsState) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_THEME, state.theme.id)
            .putString(KEY_FONT, state.fontChoice.id)
            .putInt(KEY_ARABIC_SIZE, state.arabicFontSizeSp.coerceIn(20, 42))
            .putInt(KEY_TRANS_SIZE, state.translationFontSizeSp.coerceIn(11, 18))
            .putBoolean(KEY_SHOW_LATIN, state.showLatin)
            .putBoolean(KEY_SHOW_TRANS, state.showTranslation)
            .putBoolean(KEY_MUSHAF_FOCUS, state.isMushafFocusMode)
            .putString(KEY_QARI_ID, state.selectedQariId)
            .apply()
    }

    fun saveLastRead(context: Context, surahNumber: Int, ayahNumber: Int): QuranLastRead {
        val spec = QuranCatalog.findSurah(surahNumber)
        val now = System.currentTimeMillis()
        val updated = QuranLastRead(
            surahNumber = spec.number,
            surahNameLatin = spec.nameLatin,
            ayahNumber = ayahNumber.coerceIn(1, spec.versesCount),
            updatedAt = now
        )
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_LAST_READ_SURAH, updated.surahNumber)
            .putInt(KEY_LAST_READ_AYAH, updated.ayahNumber)
            .putLong(KEY_LAST_READ_AT, now)
            .apply()
        return updated
    }

    fun toggleBookmark(
        context: Context,
        currentList: List<QuranBookmark>,
        surahNumber: Int,
        ayahNumber: Int,
        arabicPreview: String,
        translationId: String,
        highlightTag: String = "GOLD"
    ): Pair<List<QuranBookmark>, Boolean> {
        val existing = currentList.firstOrNull { it.surahNumber == surahNumber && it.ayahNumber == ayahNumber }
        val next = if (existing != null) {
            currentList.filterNot { it.id == existing.id }
        } else {
            val spec = QuranCatalog.findSurah(surahNumber)
            val created = QuranBookmark(
                id = "${surahNumber}_$ayahNumber",
                surahNumber = surahNumber,
                surahNameLatin = spec.nameLatin,
                surahNameArabic = spec.nameArabic,
                ayahNumber = ayahNumber,
                arabicPreview = arabicPreview,
                translationId = translationId,
                highlightTag = highlightTag,
                createdAt = System.currentTimeMillis()
            )
            listOf(created) + currentList
        }
        persistBookmarks(context, next)
        return next to (existing == null)
    }

    fun deleteBookmark(
        context: Context,
        currentList: List<QuranBookmark>,
        bookmark: QuranBookmark
    ): List<QuranBookmark> {
        val next = currentList.filterNot { it.id == bookmark.id }
        persistBookmarks(context, next)
        return next
    }

    fun saveNote(
        context: Context,
        currentList: List<QuranNote>,
        existingId: String?,
        surahNumber: Int,
        ayahNumber: Int,
        title: String,
        content: String,
        categoryTag: String
    ): List<QuranNote> {
        val spec = QuranCatalog.findSurah(surahNumber)
        val cleanTitle = title.trim().ifBlank { "${spec.nameLatin} : $ayahNumber" }
        val note = QuranNote(
            id = existingId ?: "qn_${System.currentTimeMillis()}",
            surahNumber = spec.number,
            surahNameLatin = spec.nameLatin,
            ayahNumber = ayahNumber.coerceIn(1, spec.versesCount),
            title = cleanTitle,
            content = content.trim(),
            categoryTag = categoryTag,
            updatedAt = System.currentTimeMillis()
        )
        val next = listOf(note) + currentList.filterNot { it.id == note.id }
        persistNotes(context, next)
        return next
    }

    fun deleteNote(
        context: Context,
        currentList: List<QuranNote>,
        note: QuranNote
    ): List<QuranNote> {
        val next = currentList.filterNot { it.id == note.id }
        persistNotes(context, next)
        return next
    }

    private fun persistBookmarks(context: Context, list: List<QuranBookmark>) {
        val arr = JSONArray()
        list.forEach { b ->
            arr.put(
                JSONObject().apply {
                    put("id", b.id)
                    put("s", b.surahNumber)
                    put("sl", b.surahNameLatin)
                    put("sa", b.surahNameArabic)
                    put("a", b.ayahNumber)
                    put("ar", b.arabicPreview)
                    put("tr", b.translationId)
                    put("hg", b.highlightTag)
                    put("at", b.createdAt)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_BOOKMARKS_JSON, arr.toString())
            .apply()
    }

    private fun parseBookmarks(raw: String?): List<QuranBookmark> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                QuranBookmark(
                    id = o.optString("id"),
                    surahNumber = o.optInt("s", 1),
                    surahNameLatin = o.optString("sl", "Al-Fātiḥah"),
                    surahNameArabic = o.optString("sa", "الْفَاتِحَة"),
                    ayahNumber = o.optInt("a", 1),
                    arabicPreview = o.optString("ar", ""),
                    translationId = o.optString("tr", ""),
                    highlightTag = o.optString("hg", "GOLD"),
                    createdAt = o.optLong("at", 0L)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun persistNotes(context: Context, list: List<QuranNote>) {
        val arr = JSONArray()
        list.forEach { n ->
            arr.put(
                JSONObject().apply {
                    put("id", n.id)
                    put("s", n.surahNumber)
                    put("sl", n.surahNameLatin)
                    put("a", n.ayahNumber)
                    put("ti", n.title)
                    put("co", n.content)
                    put("tg", n.categoryTag)
                    put("at", n.updatedAt)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_NOTES_JSON, arr.toString())
            .apply()
    }

    private fun parseNotes(raw: String?): List<QuranNote> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                QuranNote(
                    id = o.optString("id"),
                    surahNumber = o.optInt("s", 1),
                    surahNameLatin = o.optString("sl", "Al-Fātiḥah"),
                    ayahNumber = o.optInt("a", 1),
                    title = o.optString("ti", ""),
                    content = o.optString("co", ""),
                    categoryTag = o.optString("tg", "HAFALAN"),
                    updatedAt = o.optLong("at", 0L)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
