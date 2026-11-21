package com.example.util

import android.content.Context
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

data class IndonesiaCitySpec(
    val id: String,
    val name: String,
    val province: String,
    val latitude: Double,
    val longitude: Double,
    val utcOffsetHours: Int
) {
    val zoneLabel: String
        get() = when (utcOffsetHours) {
            8 -> "WITA"
            9 -> "WIT"
            else -> "WIB"
        }
}

data class PrayerTimeSlot(
    val key: String,
    val labelId: String,
    val labelEn: String,
    val timeText: String,
    val totalMinutes: Int
)

data class DailyPrayerSchedule(
    val city: IndonesiaCitySpec,
    val imsak: String,
    val subuh: String,
    val terbit: String,
    val dzuhur: String,
    val ashar: String,
    val maghrib: String,
    val isya: String,
    val slots: List<PrayerTimeSlot>,
    val nextSlotIndex: Int,
    val nextPrayerNameId: String,
    val nextPrayerNameEn: String,
    val nextPrayerTime: String,
    val minutesToNext: Int,
    val countdownId: String,
    val countdownEn: String
)

object PrayerTimeHelper {

    private const val PREFS_NAME = "waliasuh_prayer_prefs"
    private const val KEY_SELECTED_CITY_ID = "selected_prayer_city_id"

    val INDONESIA_CITIES: List<IndonesiaCitySpec> = listOf(
        // Jawa Timur
        IndonesiaCitySpec("kab_kediri", "Kabupaten Kediri", "Jawa Timur", -7.8480, 112.0178, 7),
        IndonesiaCitySpec("kota_kediri", "Kota Kediri", "Jawa Timur", -7.8169, 112.0114, 7),
        IndonesiaCitySpec("kota_surabaya", "Kota Surabaya", "Jawa Timur", -7.2575, 112.7521, 7),
        IndonesiaCitySpec("kota_malang", "Kota Malang", "Jawa Timur", -7.9666, 112.6326, 7),
        IndonesiaCitySpec("kab_malang", "Kabupaten Malang", "Jawa Timur", -8.1326, 112.5736, 7),
        IndonesiaCitySpec("kota_batu", "Kota Batu", "Jawa Timur", -7.8700, 112.5249, 7),
        IndonesiaCitySpec("kab_sidoarjo", "Kabupaten Sidoarjo", "Jawa Timur", -7.4478, 112.7183, 7),
        IndonesiaCitySpec("kab_gresik", "Kabupaten Gresik", "Jawa Timur", -7.1566, 112.6555, 7),
        IndonesiaCitySpec("kota_mojokerto", "Kota Mojokerto", "Jawa Timur", -7.4705, 112.4401, 7),
        IndonesiaCitySpec("kab_jombang", "Kabupaten Jombang", "Jawa Timur", -7.5469, 112.2331, 7),
        IndonesiaCitySpec("kab_nganjuk", "Kabupaten Nganjuk", "Jawa Timur", -7.6051, 111.9035, 7),
        IndonesiaCitySpec("kab_tulungagung", "Kabupaten Tulungagung", "Jawa Timur", -8.0657, 111.9025, 7),
        IndonesiaCitySpec("kab_trenggalek", "Kabupaten Trenggalek", "Jawa Timur", -8.0500, 111.7167, 7),
        IndonesiaCitySpec("kota_blitar", "Kota Blitar", "Jawa Timur", -8.0955, 112.1609, 7),
        IndonesiaCitySpec("kota_madiun", "Kota Madiun", "Jawa Timur", -7.6298, 111.5239, 7),
        IndonesiaCitySpec("kab_ponorogo", "Kabupaten Ponorogo", "Jawa Timur", -7.8686, 111.4620, 7),
        IndonesiaCitySpec("kab_pasuruan", "Kabupaten Pasuruan", "Jawa Timur", -7.6453, 112.9075, 7),
        IndonesiaCitySpec("kota_probolinggo", "Kota Probolinggo", "Jawa Timur", -7.7543, 113.2159, 7),
        IndonesiaCitySpec("kab_jember", "Kabupaten Jember", "Jawa Timur", -8.1724, 113.6995, 7),
        IndonesiaCitySpec("kab_banyuwangi", "Kabupaten Banyuwangi", "Jawa Timur", -8.2192, 114.3691, 7),
        IndonesiaCitySpec("kab_tuban", "Kabupaten Tuban", "Jawa Timur", -6.8976, 112.0649, 7),
        IndonesiaCitySpec("kab_lamongan", "Kabupaten Lamongan", "Jawa Timur", -7.1167, 112.4167, 7),
        IndonesiaCitySpec("kab_bojonegoro", "Kabupaten Bojonegoro", "Jawa Timur", -7.1502, 111.8817, 7),
        IndonesiaCitySpec("kab_bangkalan", "Kabupaten Bangkalan", "Jawa Timur", -7.0455, 112.7351, 7),
        IndonesiaCitySpec("kab_pamekasan", "Kabupaten Pamekasan", "Jawa Timur", -7.1606, 113.4823, 7),

        // DKI Jakarta, Banten & Jawa Barat
        IndonesiaCitySpec("kota_jakarta_pusat", "Kota Jakarta Pusat", "DKI Jakarta", -6.1865, 106.8341, 7),
        IndonesiaCitySpec("kota_jakarta_selatan", "Kota Jakarta Selatan", "DKI Jakarta", -6.2615, 106.8106, 7),
        IndonesiaCitySpec("kota_jakarta_timur", "Kota Jakarta Timur", "DKI Jakarta", -6.2250, 106.9004, 7),
        IndonesiaCitySpec("kota_bogor", "Kota Bogor", "Jawa Barat", -6.5971, 106.8060, 7),
        IndonesiaCitySpec("kab_bogor", "Kabupaten Bogor", "Jawa Barat", -6.4797, 106.8250, 7),
        IndonesiaCitySpec("kota_depok", "Kota Depok", "Jawa Barat", -6.4025, 106.7942, 7),
        IndonesiaCitySpec("kota_bekasi", "Kota Bekasi", "Jawa Barat", -6.2383, 106.9756, 7),
        IndonesiaCitySpec("kota_tangerang", "Kota Tangerang", "Banten", -6.1783, 106.6319, 7),
        IndonesiaCitySpec("kota_tangerang_selatan", "Kota Tangerang Selatan", "Banten", -6.2886, 106.7179, 7),
        IndonesiaCitySpec("kota_serang", "Kota Serang", "Banten", -6.1200, 106.1503, 7),
        IndonesiaCitySpec("kota_bandung", "Kota Bandung", "Jawa Barat", -6.9175, 107.6191, 7),
        IndonesiaCitySpec("kota_cimahi", "Kota Cimahi", "Jawa Barat", -6.8722, 107.5425, 7),
        IndonesiaCitySpec("kab_garut", "Kabupaten Garut", "Jawa Barat", -7.2279, 107.9087, 7),
        IndonesiaCitySpec("kota_tasikmalaya", "Kota Tasikmalaya", "Jawa Barat", -7.3274, 108.2207, 7),
        IndonesiaCitySpec("kota_cirebon", "Kota Cirebon", "Jawa Barat", -6.7320, 108.5523, 7),
        IndonesiaCitySpec("kota_sukabumi", "Kota Sukabumi", "Jawa Barat", -6.9277, 106.9300, 7),
        IndonesiaCitySpec("kab_karawang", "Kabupaten Karawang", "Jawa Barat", -6.3227, 107.3376, 7),

        // Jawa Tengah & DI Yogyakarta
        IndonesiaCitySpec("kota_semarang", "Kota Semarang", "Jawa Tengah", -6.9667, 110.4167, 7),
        IndonesiaCitySpec("kota_surakarta", "Kota Surakarta (Solo)", "Jawa Tengah", -7.5755, 110.8243, 7),
        IndonesiaCitySpec("kota_yogyakarta", "Kota Yogyakarta", "DI Yogyakarta", -7.7956, 110.3695, 7),
        IndonesiaCitySpec("kab_sleman", "Kabupaten Sleman", "DI Yogyakarta", -7.7161, 110.3556, 7),
        IndonesiaCitySpec("kab_bantul", "Kabupaten Bantul", "DI Yogyakarta", -7.8886, 110.3289, 7),
        IndonesiaCitySpec("kota_magelang", "Kota Magelang", "Jawa Tengah", -7.4797, 110.2177, 7),
        IndonesiaCitySpec("kab_banyumas", "Kabupaten Banyumas (Purwokerto)", "Jawa Tengah", -7.4243, 109.2391, 7),
        IndonesiaCitySpec("kota_tegal", "Kota Tegal", "Jawa Tengah", -6.8694, 109.1402, 7),
        IndonesiaCitySpec("kota_pekalongan", "Kota Pekalongan", "Jawa Tengah", -6.8898, 109.6746, 7),
        IndonesiaCitySpec("kab_kudus", "Kabupaten Kudus", "Jawa Tengah", -6.8048, 110.8405, 7),
        IndonesiaCitySpec("kab_pati", "Kabupaten Pati", "Jawa Tengah", -6.7559, 111.0380, 7),
        IndonesiaCitySpec("kab_cilacap", "Kabupaten Cilacap", "Jawa Tengah", -7.7279, 109.0059, 7),

        // Sumatera (WIB)
        IndonesiaCitySpec("kota_banda_aceh", "Kota Banda Aceh", "Aceh", 5.5483, 95.3238, 7),
        IndonesiaCitySpec("kota_medan", "Kota Medan", "Sumatera Utara", 3.5952, 98.6722, 7),
        IndonesiaCitySpec("kota_pematangsiantar", "Kota Pematangsiantar", "Sumatera Utara", 2.9595, 99.0687, 7),
        IndonesiaCitySpec("kota_padang", "Kota Padang", "Sumatera Barat", -0.9471, 100.4172, 7),
        IndonesiaCitySpec("kota_bukittinggi", "Kota Bukittinggi", "Sumatera Barat", -0.3056, 100.3692, 7),
        IndonesiaCitySpec("kota_pekanbaru", "Kota Pekanbaru", "Riau", 0.5071, 101.4478, 7),
        IndonesiaCitySpec("kota_batam", "Kota Batam", "Kepulauan Riau", 1.0456, 104.0305, 7),
        IndonesiaCitySpec("kota_tanjungpinang", "Kota Tanjungpinang", "Kepulauan Riau", 0.9168, 104.4433, 7),
        IndonesiaCitySpec("kota_jambi", "Kota Jambi", "Jambi", -1.6101, 103.6131, 7),
        IndonesiaCitySpec("kota_palembang", "Kota Palembang", "Sumatera Selatan", -2.9761, 104.7754, 7),
        IndonesiaCitySpec("kota_bengkulu", "Kota Bengkulu", "Bengkulu", -3.7928, 102.2608, 7),
        IndonesiaCitySpec("kota_bandar_lampung", "Kota Bandar Lampung", "Lampung", -5.4292, 105.2610, 7),
        IndonesiaCitySpec("kota_pangkalpinang", "Kota Pangkalpinang", "Bangka Belitung", -2.1316, 106.1169, 7),

        // Kalimantan Barat & Tengah (WIB)
        IndonesiaCitySpec("kota_pontianak", "Kota Pontianak", "Kalimantan Barat", -0.0263, 109.3425, 7),
        IndonesiaCitySpec("kota_palangkaraya", "Kota Palangka Raya", "Kalimantan Tengah", -2.2136, 113.9108, 7),

        // Bali, NTB, NTT & Kalimantan Timur/Selatan/Utara (WITA)
        IndonesiaCitySpec("kota_denpasar", "Kota Denpasar", "Bali", -8.6705, 115.2126, 8),
        IndonesiaCitySpec("kota_mataram", "Kota Mataram", "Nusa Tenggara Barat", -8.5833, 116.1167, 8),
        IndonesiaCitySpec("kota_kupang", "Kota Kupang", "Nusa Tenggara Timur", -10.1772, 123.6070, 8),
        IndonesiaCitySpec("kota_banjarmasin", "Kota Banjarmasin", "Kalimantan Selatan", -3.3186, 114.5944, 8),
        IndonesiaCitySpec("kota_banjarbaru", "Kota Banjarbaru", "Kalimantan Selatan", -3.4437, 114.8300, 8),
        IndonesiaCitySpec("kota_samarinda", "Kota Samarinda", "Kalimantan Timur", -0.5022, 117.1536, 8),
        IndonesiaCitySpec("kota_balikpapan", "Kota Balikpapan", "Kalimantan Timur", -1.2379, 116.8529, 8),
        IndonesiaCitySpec("kota_tarakan", "Kota Tarakan", "Kalimantan Utara", 3.3274, 117.5785, 8),

        // Sulawesi (WITA)
        IndonesiaCitySpec("kota_makassar", "Kota Makassar", "Sulawesi Selatan", -5.1477, 119.4327, 8),
        IndonesiaCitySpec("kota_parepare", "Kota Parepare", "Sulawesi Selatan", -4.0096, 119.6236, 8),
        IndonesiaCitySpec("kota_palu", "Kota Palu", "Sulawesi Tengah", -0.8917, 119.8707, 8),
        IndonesiaCitySpec("kota_kendari", "Kota Kendari", "Sulawesi Tenggara", -3.9985, 122.5130, 8),
        IndonesiaCitySpec("kota_gorontalo", "Kota Gorontalo", "Gorontalo", 0.5435, 123.0568, 8),
        IndonesiaCitySpec("kota_manado", "Kota Manado", "Sulawesi Utara", 1.4748, 124.8421, 8),
        IndonesiaCitySpec("kab_mamuju", "Kabupaten Mamuju", "Sulawesi Barat", -2.6775, 118.8878, 8),

        // Maluku & Papua (WIT)
        IndonesiaCitySpec("kota_ambon", "Kota Ambon", "Maluku", -3.6954, 128.1814, 9),
        IndonesiaCitySpec("kota_ternate", "Kota Ternate", "Maluku Utara", 0.7893, 127.3878, 9),
        IndonesiaCitySpec("kota_sorong", "Kota Sorong", "Papua Barat Daya", -0.8762, 131.2558, 9),
        IndonesiaCitySpec("kab_manokwari", "Kabupaten Manokwari", "Papua Barat", -0.8615, 134.0620, 9),
        IndonesiaCitySpec("kota_jayapura", "Kota Jayapura", "Papua", -2.5916, 140.6690, 9),
        IndonesiaCitySpec("kab_merauke", "Kabupaten Merauke", "Papua Selatan", -8.4932, 140.4018, 9)
    ).sortedBy { it.name }

    fun getSavedCityId(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_CITY_ID, null)
            ?.takeIf { it.isNotBlank() }
    }

    fun saveSelectedCityId(context: Context, cityId: String?) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        if (cityId.isNullOrBlank()) {
            editor.remove(KEY_SELECTED_CITY_ID)
        } else {
            editor.putString(KEY_SELECTED_CITY_ID, cityId.trim())
        }
        editor.apply()
    }

    fun resolveCity(schoolCity: String, savedCityId: String?): IndonesiaCitySpec {
        if (!savedCityId.isNullOrBlank()) {
            INDONESIA_CITIES.firstOrNull { it.id.equals(savedCityId.trim(), ignoreCase = true) }?.let {
                return it
            }
        }
        return findBestCityMatch(schoolCity)
    }

    fun findBestCityMatch(rawCity: String): IndonesiaCitySpec {
        val clean = rawCity.trim().lowercase()
        if (clean.isNotBlank()) {
            INDONESIA_CITIES.firstOrNull { it.name.equals(clean, ignoreCase = true) }?.let { return it }
            val normalizedInput = clean
                .replace("kabupaten ", "kab ")
                .replace("kab. ", "kab ")
                .replace("kota ", "")
                .trim()
            INDONESIA_CITIES.firstOrNull { spec ->
                val specNorm = spec.name.lowercase()
                    .replace("kabupaten ", "kab ")
                    .replace("kota ", "")
                    .trim()
                specNorm == normalizedInput
            }?.let { return it }
            INDONESIA_CITIES.firstOrNull { spec ->
                spec.name.lowercase().contains(normalizedInput) ||
                    normalizedInput.contains(
                        spec.name.lowercase()
                            .replace("kabupaten ", "")
                            .replace("kota ", "")
                            .trim()
                    )
            }?.let { return it }
        }
        return INDONESIA_CITIES.firstOrNull { it.id == "kab_kediri" } ?: INDONESIA_CITIES.first()
    }

    /**
     * Menghitung jadwal sholat harian berdasarkan metode standar Kementerian Agama RI (Kemenag):
     * - Sudut Subuh: 20° (plus ikhtiyat 2 menit)
     * - Imsak: 10 menit sebelum Subuh
     * - Dzuhur: Zawāl + 2.5 menit ikhtiyat
     * - Ashar: Mazhab Syafi'i (bayangan 1x) + 2 menit ikhtiyat
     * - Maghrib: Terbenam (-0.8333°) + 2 menit ikhtiyat
     * - Sudut Isya: 18° + 2 menit ikhtiyat
     */
    fun calculateDailyPrayerSchedule(
        city: IndonesiaCitySpec,
        calendar: Calendar = Calendar.getInstance(),
        isEn: Boolean = false
    ): DailyPrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        val jd = julianDayAtLocalNoon(year, month, day, city.utcOffsetHours)
        val d = jd - 2451545.0

        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2.0 * g)))

        val e = 23.439 - 0.00000036 * d
        val raHours = radToDeg(atan2(cos(degToRad(e)) * sin(degToRad(l)), cos(degToRad(l)))) / 15.0
        val declDeg = radToDeg(asin(sin(degToRad(e)) * sin(degToRad(l))))

        var dRa = (q / 15.0) - fixHour(raHours)
        while (dRa < -12.0) dRa += 24.0
        while (dRa > 12.0) dRa -= 24.0
        val equationOfTimeHours = dRa

        val transit = 12.0 + city.utcOffsetHours - (city.longitude / 15.0) - equationOfTimeHours
        val ikhtiyat = 2.0 / 60.0

        val subuhHours = transit - hourAngleForAltitude(-20.0, city.latitude, declDeg) + ikhtiyat
        val imsakHours = subuhHours - (10.0 / 60.0)
        val terbitHours = transit - hourAngleForAltitude(-0.8333, city.latitude, declDeg)
        val dzuhurHours = transit + (2.5 / 60.0)

        val asharAltDeg = radToDeg(atan(1.0 / (1.0 + tan(degToRad(abs(city.latitude - declDeg))))))
        val asharHours = transit + hourAngleForAltitude(asharAltDeg, city.latitude, declDeg) + ikhtiyat
        val maghribHours = transit + hourAngleForAltitude(-0.8333, city.latitude, declDeg) + ikhtiyat
        val isyaHours = transit + hourAngleForAltitude(-18.0, city.latitude, declDeg) + ikhtiyat

        val imsakMin = toMinutesOfDay(imsakHours)
        val subuhMin = toMinutesOfDay(subuhHours)
        val terbitMin = toMinutesOfDay(terbitHours)
        val dzuhurMin = toMinutesOfDay(dzuhurHours)
        val asharMin = toMinutesOfDay(asharHours)
        val maghribMin = toMinutesOfDay(maghribHours)
        val isyaMin = toMinutesOfDay(isyaHours)

        val slots = listOf(
            PrayerTimeSlot("SUBUH", "Subuh", "Fajr", formatMinutes(subuhMin), subuhMin),
            PrayerTimeSlot("DZUHUR", "Dzuhur", "Dhuhr", formatMinutes(dzuhurMin), dzuhurMin),
            PrayerTimeSlot("ASHAR", "Ashar", "Asr", formatMinutes(asharMin), asharMin),
            PrayerTimeSlot("MAGHRIB", "Maghrib", "Maghrib", formatMinutes(maghribMin), maghribMin),
            PrayerTimeSlot("ISYA", "Isya", "Isha", formatMinutes(isyaMin), isyaMin)
        )

        val nextIdx = slots.indexOfFirst { it.totalMinutes > nowMinutes }
        val resolvedNextIdx = if (nextIdx >= 0) nextIdx else 0
        val nextSlot = slots[resolvedNextIdx]
        val remainingMinutes = if (nextIdx >= 0) {
            (nextSlot.totalMinutes - nowMinutes).coerceAtLeast(1)
        } else {
            ((24 * 60 - nowMinutes) + nextSlot.totalMinutes).coerceAtLeast(1)
        }

        val hoursRem = remainingMinutes / 60
        val minsRem = remainingMinutes % 60
        val countdownId = when {
            hoursRem > 0 && minsRem > 0 -> "${hoursRem}j ${minsRem}m lagi"
            hoursRem > 0 -> "${hoursRem}j lagi"
            else -> "${minsRem}m lagi"
        }
        val countdownEn = when {
            hoursRem > 0 && minsRem > 0 -> "in ${hoursRem}h ${minsRem}m"
            hoursRem > 0 -> "in ${hoursRem}h"
            else -> "in ${minsRem}m"
        }

        return DailyPrayerSchedule(
            city = city,
            imsak = formatMinutes(imsakMin),
            subuh = formatMinutes(subuhMin),
            terbit = formatMinutes(terbitMin),
            dzuhur = formatMinutes(dzuhurMin),
            ashar = formatMinutes(asharMin),
            maghrib = formatMinutes(maghribMin),
            isya = formatMinutes(isyaMin),
            slots = slots,
            nextSlotIndex = resolvedNextIdx,
            nextPrayerNameId = nextSlot.labelId,
            nextPrayerNameEn = nextSlot.labelEn,
            nextPrayerTime = nextSlot.timeText,
            minutesToNext = remainingMinutes,
            countdownId = countdownId,
            countdownEn = countdownEn
        )
    }

    private fun julianDayAtLocalNoon(year: Int, month: Int, day: Int, utcOffsetHours: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        val jd0 = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
        return jd0 + 0.5 - (utcOffsetHours / 24.0)
    }

    private fun hourAngleForAltitude(altDeg: Double, latDeg: Double, declDeg: Double): Double {
        val numerator = sin(degToRad(altDeg)) - sin(degToRad(latDeg)) * sin(degToRad(declDeg))
        val denominator = cos(degToRad(latDeg)) * cos(degToRad(declDeg))
        val cosH = (numerator / denominator).coerceIn(-1.0, 1.0)
        return radToDeg(acos(cosH)) / 15.0
    }

    private fun toMinutesOfDay(hours: Double): Int {
        val raw = (hours * 60.0).roundToInt()
        val mod = raw % (24 * 60)
        return if (mod < 0) mod + 24 * 60 else mod
    }

    private fun formatMinutes(totalMinutes: Int): String {
        val hh = (totalMinutes / 60).coerceIn(0, 23)
        val mm = (totalMinutes % 60).coerceIn(0, 59)
        return "%02d:%02d".format(hh, mm)
    }

    private fun fixAngle(deg: Double): Double {
        var a = deg % 360.0
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(h: Double): Double {
        var x = h % 24.0
        if (x < 0) x += 24.0
        return x
    }

    private fun degToRad(deg: Double): Double = deg * Math.PI / 180.0
    private fun radToDeg(rad: Double): Double = rad * 180.0 / Math.PI
}
