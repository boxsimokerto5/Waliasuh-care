package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import kotlin.math.roundToInt

object HaidDateCalculator {
    const val MAX_HAID_DAYS = 15
    const val MIN_SUCI_DAYS = 15
    private const val MILLIS_PER_DAY = 86_400_000.0

    fun calendarDaysBetween(startMillis: Long, endMillis: Long): Int {
        val startNoon = Calendar.getInstance().apply {
            timeInMillis = startMillis
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endNoon = Calendar.getInstance().apply {
            timeInMillis = endMillis
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return ((endNoon - startNoon) / MILLIS_PER_DAY).roundToInt().coerceAtLeast(0)
    }

    fun addCalendarDaysStartOfDay(startMillis: Long, days: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = startMillis
            add(Calendar.DAY_OF_YEAR, days)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun calculateStartDateForHaidDay(haidDay: Int, nowMillis: Long = System.currentTimeMillis()): Long {
        val clampedDay = haidDay.coerceIn(1, MAX_HAID_DAYS)
        if (clampedDay == 1) return nowMillis
        return Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, -(clampedDay - 1))
        }.timeInMillis
    }
}

@Entity(tableName = "haid_records")
data class HaidRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long = 0,
    val studentName: String,
    val studentNik: String = "",
    val studentNis: String = "",
    val studentClass: String = "",
    val studentDorm: String = "",
    val startDate: Long,
    val endDate: Long? = null,
    val status: String = "HAID", // "HAID", "SUCI", "SUCI_AUTO"
    val symptoms: String = "Normal",
    val notes: String = "",
    val waliName: String = "Wali Asuh",
    val schoolId: String = "SRT1-KDR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Jumlah hari kalender sejak startDate hingga sekarang (atau endDate jika sudah selesai).
     */
    private val rawElapsedHaidDays: Int
        get() {
            val end = endDate ?: System.currentTimeMillis()
            return (HaidDateCalculator.calendarDaysBetween(startDate, end) + 1).coerceAtLeast(1)
        }

    /**
     * True jika catatan masih berstatus HAID namun sudah melewati batas maksimal 15 hari
     * sehingga otomatis dipindahkan ke Daftar Suci pada hari ke-16.
     */
    val isAutoCompletedByLimit: Boolean
        get() = (endDate == null && status == "HAID" && rawElapsedHaidDays > HaidDateCalculator.MAX_HAID_DAYS) ||
            status == "SUCI_AUTO"

    /**
     * Tanggal berakhir efektif: memakai endDate tersimpan, atau awal hari ke-16 jika melewati batas 15 hari.
     */
    val effectiveEndDate: Long?
        get() = endDate ?: if (status == "HAID" && rawElapsedHaidDays > HaidDateCalculator.MAX_HAID_DAYS) {
            HaidDateCalculator.addCalendarDaysStartOfDay(startDate, HaidDateCalculator.MAX_HAID_DAYS)
        } else {
            null
        }

    /**
     * Aktif di Daftar Haid hanya jika status HAID dan durasi berjalan <= 15 hari.
     */
    val isActive: Boolean
        get() = status == "HAID" && effectiveEndDate == null

    /**
     * Durasi haid dalam hitungan hari kalender (maksimal 15 hari bila otomatis pindah ke Daftar Suci).
     */
    val durationDays: Int
        get() {
            if (isAutoCompletedByLimit) {
                return HaidDateCalculator.MAX_HAID_DAYS
            }
            val end = effectiveEndDate ?: System.currentTimeMillis()
            return (HaidDateCalculator.calendarDaysBetween(startDate, end) + 1).coerceAtLeast(1)
        }

    /**
     * Hitungan Hari Suci berjalan sejak tanggal mandi suci (atau sejak hari ke-16 bila auto-pindah).
     * Mengembalikan 0 jika masih aktif haid.
     */
    val suciDayCount: Int
        get() {
            if (isActive) return 0
            val suciStart = effectiveEndDate ?: startDate
            return (HaidDateCalculator.calendarDaysBetween(suciStart, System.currentTimeMillis()) + 1).coerceAtLeast(1)
        }

    /**
     * True jika siswi berada pada Daftar Suci dengan masa suci belum genap 15 hari (Hari ke-1 s.d. 14).
     */
    val isInMinimumSuciPeriod: Boolean
        get() = !isActive && suciDayCount in 1 until HaidDateCalculator.MIN_SUCI_DAYS

    /**
     * Peringatan batas maksimal haid (Hari ke-15: hari terakhir sebelum otomatis dipindahkan ke Daftar Suci).
     */
    val isIstihadhahWarning: Boolean
        get() = isActive && durationDays >= HaidDateCalculator.MAX_HAID_DAYS
}
