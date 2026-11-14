package com.example.util

import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.US)

    fun formatFullDate(
        timestamp: Long,
        language: AppLanguage = AppLanguageManager.currentLanguage
    ): String {
        val pattern = if (language == AppLanguage.ENGLISH) "EEEE, MMMM d, yyyy" else "EEEE, d MMMM yyyy"
        return SimpleDateFormat(pattern, language.locale).format(Date(timestamp))
    }

    fun formatShortDate(
        timestamp: Long,
        language: AppLanguage = AppLanguageManager.currentLanguage
    ): String {
        return SimpleDateFormat("d MMM yyyy", language.locale).format(Date(timestamp))
    }

    fun formatTime(
        timestamp: Long,
        language: AppLanguage = AppLanguageManager.currentLanguage
    ): String {
        return SimpleDateFormat("HH:mm", language.locale).format(Date(timestamp))
    }

    fun formatDateTime(
        timestamp: Long,
        language: AppLanguage = AppLanguageManager.currentLanguage
    ): String {
        return "${formatShortDate(timestamp, language)} ${formatTime(timestamp, language)} WIB"
    }

    fun isToday(timestamp: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
        val cal2 = Calendar.getInstance()
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isTomorrow(timestamp: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
        val cal2 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isTomorrowOrLater(timestamp: Long): Boolean {
        val calTodayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return timestamp > calTodayEnd.timeInMillis
    }

    fun utcDateMillisToLocalNoon(utcMillis: Long): Long {
        val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcMillis
        }
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
            set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun isThisWeek(timestamp: Long): Boolean {
        val calNow = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val cal7DaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -6)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calItem = Calendar.getInstance().apply { timeInMillis = timestamp }
        val isSameCalendarWeek = calItem.get(Calendar.WEEK_OF_YEAR) == calNow.get(Calendar.WEEK_OF_YEAR) &&
                calItem.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
        val isWithin7Days = timestamp in cal7DaysAgo.timeInMillis..calNow.timeInMillis
        return isWithin7Days || isSameCalendarWeek
    }

    fun formatMonthYear(
        timestamp: Long,
        language: AppLanguage = AppLanguageManager.currentLanguage
    ): String {
        return SimpleDateFormat("MMMM yyyy", language.locale).format(Date(timestamp))
    }

    fun getMonthKey(timestamp: Long): String {
        return monthKeyFormat.format(Date(timestamp))
    }

    fun isBeforeToday(timestamp: Long): Boolean {
        val calTodayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return timestamp < calTodayStart.timeInMillis
    }

    fun isPastDate(timestamp: Long): Boolean {
        val cal1 = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        val now = Calendar.getInstance()
        return cal1.before(now)
    }

    fun getRelativeDateLabel(
        timestamp: Long,
        language: AppLanguage = AppLanguageManager.currentLanguage
    ): String {
        val formattedShort = formatShortDate(timestamp, language)
        val strings = AppLanguageManager.stringsFor(language).report
        return when {
            isToday(timestamp) -> strings.dateTodayPrefix(formattedShort)
            isTomorrow(timestamp) -> strings.dateTomorrowPrefix(formattedShort)
            else -> formattedShort
        }
    }
}
