package com.example.data.remote.pocketbase

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.SchoolRegistration

object PocketBaseConfig {
    private const val PREF_NAME = "pocketbase_config_prefs"
    private const val KEY_URL = "pref_pocketbase_url"
    private const val KEY_LAST_SYNC = "pref_pocketbase_last_sync"
    private const val KEY_DELETED_SCHOOL_TOKENS = "pref_deleted_school_tokens"

    // Endpoint produksi PocketBase milik sekolah/yayasan (HTTPS via Nginx + Let's Encrypt)
    const val EMBEDDED_URL = "https://waliasuh.areksurabaya.my.id"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Membersihkan URL dari akhiran /api/, /_/, atau garis miring berlebih.
     */
    fun normalizeBaseUrl(raw: String): String {
        var clean = raw.trim()
        while (clean.endsWith("/")) clean = clean.dropLast(1)
        if (clean.endsWith("/_")) clean = clean.removeSuffix("/_")
        if (clean.endsWith("/api")) clean = clean.removeSuffix("/api")
        while (clean.endsWith("/")) clean = clean.dropLast(1)
        return clean
    }

    fun getBaseUrl(context: Context): String {
        val saved = getPrefs(context).getString(KEY_URL, "") ?: ""
        if (saved.isNotBlank()) return normalizeBaseUrl(saved)

        val buildConfigUrl = try {
            val field = BuildConfig::class.java.getField("POCKETBASE_URL")
            (field.get(null) as? String) ?: ""
        } catch (_: Exception) {
            ""
        }
        if (buildConfigUrl.isNotBlank() && buildConfigUrl.startsWith("http")) {
            return normalizeBaseUrl(buildConfigUrl)
        }

        return normalizeBaseUrl(EMBEDDED_URL)
    }

    fun saveBaseUrl(context: Context, url: String) {
        val normalized = normalizeBaseUrl(url)
        getPrefs(context).edit()
            .putString(KEY_URL, normalized)
            .apply()
    }

    fun resetToDefaultUrl(context: Context) {
        getPrefs(context).edit().remove(KEY_URL).apply()
    }

    fun isConfigured(context: Context): Boolean {
        val url = getBaseUrl(context)
        return url.isNotBlank() && (url.startsWith("https://") || url.startsWith("http://"))
    }

    fun setLastSyncTime(context: Context, timestamp: Long) {
        getPrefs(context).edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }

    fun getLastSyncTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_SYNC, 0L)
    }

    fun buildSchoolDeletionTokens(reg: SchoolRegistration): Set<String> {
        val tokens = linkedSetOf<String>()
        if (reg.id > 0L) tokens.add("id:${reg.id}")
        val cleanCode = reg.generatedSchoolCode.trim().uppercase()
        if (cleanCode.isNotBlank()) tokens.add("code:$cleanCode")
        val cleanName = reg.schoolName.trim().lowercase()
        val cleanPhone = reg.picPhone.trim()
        if (cleanName.isNotBlank()) {
            tokens.add("sig:$cleanName|$cleanPhone|${reg.submissionDate}")
        }
        return tokens
    }

    fun markSchoolRegistrationDeleted(context: Context, reg: SchoolRegistration) {
        val prefs = getPrefs(context)
        val existing = prefs.getStringSet(KEY_DELETED_SCHOOL_TOKENS, emptySet())?.toMutableSet()
            ?: mutableSetOf()
        val added = buildSchoolDeletionTokens(reg)
        if (added.isNotEmpty()) {
            existing.addAll(added)
            prefs.edit().putStringSet(KEY_DELETED_SCHOOL_TOKENS, existing).apply()
        }
    }

    fun clearSchoolRegistrationDeletedTokens(context: Context, reg: SchoolRegistration) {
        val prefs = getPrefs(context)
        val existing = prefs.getStringSet(KEY_DELETED_SCHOOL_TOKENS, emptySet())?.toMutableSet() ?: return
        if (existing.removeAll(buildSchoolDeletionTokens(reg))) {
            prefs.edit().putStringSet(KEY_DELETED_SCHOOL_TOKENS, existing).apply()
        }
    }

    fun isSchoolRegistrationDeleted(context: Context, reg: SchoolRegistration): Boolean {
        val deleted = getPrefs(context).getStringSet(KEY_DELETED_SCHOOL_TOKENS, emptySet()) ?: emptySet()
        if (deleted.isEmpty()) return false
        return buildSchoolDeletionTokens(reg).any { it in deleted }
    }

    fun isSchoolCodeDeleted(context: Context, schoolCode: String): Boolean {
        val cleanCode = schoolCode.trim().uppercase()
        if (cleanCode.isBlank()) return false
        val deleted = getPrefs(context).getStringSet(KEY_DELETED_SCHOOL_TOKENS, emptySet()) ?: emptySet()
        return "code:$cleanCode" in deleted
    }
}
