package com.example.data.remote.supabase

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

object SupabaseConfig {
    private const val PREF_NAME = "supabase_config_prefs"
    private const val KEY_URL = "pref_supabase_url"
    private const val KEY_ANON_KEY = "pref_supabase_anon_key"
    private const val KEY_LAST_SYNC = "pref_supabase_last_sync"

    // Kredensial bawaan yang ditanamkan langsung agar otomatis terhubung (termasuk saat diexport ke GitHub)
    const val EMBEDDED_URL = "https://illsflsvxmtahlmovogv.supabase.co"
    const val EMBEDDED_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlsbHNmbHN2eG10YWhsbW92b2d2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTQzMDExNTgsImV4cCI6MjEwOTg3NzE1OH0.jjPLeJkorxThwQPf5muwJxap-xYH_SOp_40OVuZQoqA"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Membersihkan URL dari akhiran /rest/v1/ agar Retrofit memanggil endpoint dengan benar.
     */
    fun normalizeBaseUrl(raw: String): String {
        var clean = raw.trim()
        if (clean.endsWith("/")) clean = clean.dropLast(1)
        if (clean.endsWith("/rest/v1")) clean = clean.removeSuffix("/rest/v1")
        if (clean.endsWith("/")) clean = clean.dropLast(1)
        return clean
    }

    /**
     * Mendapatkan URL Supabase dari SharedPreferences, BuildConfig, atau default EMBEDDED_URL.
     */
    fun getSupabaseUrl(context: Context): String {
        val saved = getPrefs(context).getString(KEY_URL, "") ?: ""
        if (saved.isNotBlank()) return normalizeBaseUrl(saved)

        // Fallback to BuildConfig if configured
        val buildConfigUrl = try {
            val field = BuildConfig::class.java.getField("SUPABASE_URL")
            (field.get(null) as? String) ?: ""
        } catch (e: Exception) {
            ""
        }
        if (buildConfigUrl.isNotBlank() && !buildConfigUrl.contains("your-project.supabase.co")) {
            return normalizeBaseUrl(buildConfigUrl)
        }

        return normalizeBaseUrl(EMBEDDED_URL)
    }

    /**
     * Mendapatkan Anon Key Supabase dari SharedPreferences, BuildConfig, atau default EMBEDDED_ANON_KEY.
     */
    fun getSupabaseKey(context: Context): String {
        val saved = getPrefs(context).getString(KEY_ANON_KEY, "") ?: ""
        if (saved.isNotBlank()) return saved.trim()

        // Fallback to BuildConfig if configured
        val buildConfigKey = try {
            val field = BuildConfig::class.java.getField("SUPABASE_KEY")
            (field.get(null) as? String) ?: ""
        } catch (e: Exception) {
            ""
        }
        if (buildConfigKey.isNotBlank() && !buildConfigKey.contains("your_supabase_anon_key")) {
            return buildConfigKey.trim()
        }

        return EMBEDDED_ANON_KEY
    }

    /**
     * Menyimpan URL dan Anon Key Supabase secara kustom jika pengguna mengubahnya di UI.
     */
    fun saveCredentials(context: Context, url: String, anonKey: String) {
        getPrefs(context).edit()
            .putString(KEY_URL, normalizeBaseUrl(url))
            .putString(KEY_ANON_KEY, anonKey.trim())
            .apply()
    }

    /**
     * Cek apakah konfigurasi Supabase sudah siap digunakan.
     */
    fun isConfigured(context: Context): Boolean {
        val url = getSupabaseUrl(context)
        val key = getSupabaseKey(context)
        return url.isNotBlank() && key.isNotBlank() && url.startsWith("http")
    }

    fun setLastSyncTime(context: Context, timestamp: Long) {
        getPrefs(context).edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }

    fun getLastSyncTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_SYNC, 0L)
    }
}
