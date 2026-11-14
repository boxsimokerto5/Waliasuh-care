package com.example.util.i18n

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

enum class AppLanguage(
    val code: String,
    val locale: Locale,
    val displayName: String,
    val shortBadge: String
) {
    INDONESIAN(
        code = "id",
        locale = Locale("id", "ID"),
        displayName = "Bahasa Indonesia",
        shortBadge = "ID"
    ),
    ENGLISH(
        code = "en",
        locale = Locale.US,
        displayName = "English",
        shortBadge = "EN"
    );

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return when (code?.lowercase()?.trim()) {
                "en" -> ENGLISH
                else -> INDONESIAN
            }
        }
    }
}

object AppLanguageManager {
    private const val PREFS_NAME = "waliasuh_prefs"
    private const val KEY_LANGUAGE = "app_language"

    @Volatile
    var currentLanguage: AppLanguage = AppLanguage.INDONESIAN
        private set

    fun init(context: Context): AppLanguage {
        val loaded = getLanguage(context)
        currentLanguage = loaded
        return loaded
    }

    fun getLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, AppLanguage.INDONESIAN.code))
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        currentLanguage = language
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.code)
            .apply()
    }

    fun stringsFor(language: AppLanguage): AppStrings {
        return when (language) {
            AppLanguage.INDONESIAN -> IdStrings
            AppLanguage.ENGLISH -> EnStrings
        }
    }

    fun getStrings(context: Context? = null): AppStrings {
        val lang = if (context != null) {
            getLanguage(context).also { currentLanguage = it }
        } else {
            currentLanguage
        }
        return stringsFor(lang)
    }

    fun createLocalizedContext(base: Context, language: AppLanguage): Context {
        val config = Configuration(base.resources.configuration)
        config.setLocale(language.locale)
        val localizedResources = base.createConfigurationContext(config).resources
        return object : ContextWrapper(base) {
            override fun getResources(): Resources = localizedResources
        }
    }
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { IdStrings }
