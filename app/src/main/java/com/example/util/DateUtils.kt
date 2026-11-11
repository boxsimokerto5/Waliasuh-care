package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val fullDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
    private val shortDateFormat = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))

    fun formatFullDate(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return "${formatShortDate(timestamp)} ${formatTime(timestamp)} WIB"
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

    fun getRelativeDateLabel(timestamp: Long): String {
        return when {
            isToday(timestamp) -> "Hari ini (${formatShortDate(timestamp)})"
            isTomorrow(timestamp) -> "Besok (${formatShortDate(timestamp)})"
            else -> formatShortDate(timestamp)
        }
    }
}
