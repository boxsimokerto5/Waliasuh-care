package com.example.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt

enum class WeatherConditionCategory {
    SUNNY,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    THUNDERSTORM
}

data class WeatherDescriptor(
    val category: WeatherConditionCategory,
    val labelId: String,
    val labelEn: String,
    val isRainRisk: Boolean
)

data class WeatherPeriodSlot(
    val key: String,
    val labelId: String,
    val labelEn: String,
    val targetHour: Int,
    val tempC: Int,
    val rainChancePercent: Int,
    val weatherCode: Int,
    val descriptor: WeatherDescriptor
)

data class CityWeatherForecast(
    val city: IndonesiaCitySpec,
    val currentTempC: Int,
    val apparentTempC: Int,
    val humidityPercent: Int,
    val windKmh: Int,
    val currentRainChancePercent: Int,
    val minTempC: Int,
    val maxTempC: Int,
    val maxRainChancePercent: Int,
    val isDay: Boolean,
    val currentCode: Int,
    val currentDescriptor: WeatherDescriptor,
    val periods: List<WeatherPeriodSlot>,
    val activePeriodKey: String,
    val fetchedAtMillis: Long,
    val isFromCache: Boolean = false
)

object WeatherForecastHelper {

    private const val PREFS_NAME = "waliasuh_weather_prefs"
    private const val KEY_JSON_PREFIX = "weather_json_"
    private const val KEY_TS_PREFIX = "weather_ts_"
    const val CACHE_TTL_MS: Long = 30L * 60L * 1000L

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    fun buildOpenMeteoUrl(city: IndonesiaCitySpec): String {
        val lat = String.format(Locale.US, "%.4f", city.latitude)
        val lon = String.format(Locale.US, "%.4f", city.longitude)
        return "https://api.open-meteo.com/v1/forecast" +
            "?latitude=$lat" +
            "&longitude=$lon" +
            "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,weather_code,wind_speed_10m,is_day" +
            "&hourly=temperature_2m,precipitation_probability,weather_code" +
            "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
            "&timezone=auto" +
            "&forecast_days=1"
    }

    fun describeWmoWeather(code: Int, isDay: Boolean = true): WeatherDescriptor {
        return when (code) {
            0 -> WeatherDescriptor(
                category = WeatherConditionCategory.SUNNY,
                labelId = if (isDay) "Cerah" else "Cerah Malam",
                labelEn = if (isDay) "Clear" else "Clear Night",
                isRainRisk = false
            )
            1 -> WeatherDescriptor(
                category = WeatherConditionCategory.PARTLY_CLOUDY,
                labelId = "Cerah Berawan",
                labelEn = "Mostly Clear",
                isRainRisk = false
            )
            2 -> WeatherDescriptor(
                category = WeatherConditionCategory.PARTLY_CLOUDY,
                labelId = "Berawan Sebagian",
                labelEn = "Partly Cloudy",
                isRainRisk = false
            )
            3 -> WeatherDescriptor(
                category = WeatherConditionCategory.CLOUDY,
                labelId = "Mendung",
                labelEn = "Overcast",
                isRainRisk = false
            )
            45, 48 -> WeatherDescriptor(
                category = WeatherConditionCategory.FOG,
                labelId = "Berkabut",
                labelEn = "Foggy",
                isRainRisk = false
            )
            51, 53, 55, 56, 57 -> WeatherDescriptor(
                category = WeatherConditionCategory.DRIZZLE,
                labelId = "Gerimis",
                labelEn = "Drizzle",
                isRainRisk = true
            )
            61, 80 -> WeatherDescriptor(
                category = WeatherConditionCategory.RAIN,
                labelId = "Hujan Ringan",
                labelEn = "Light Rain",
                isRainRisk = true
            )
            63, 66, 81 -> WeatherDescriptor(
                category = WeatherConditionCategory.RAIN,
                labelId = "Hujan Sedang",
                labelEn = "Moderate Rain",
                isRainRisk = true
            )
            65, 67, 82 -> WeatherDescriptor(
                category = WeatherConditionCategory.RAIN,
                labelId = "Hujan Lebat",
                labelEn = "Heavy Rain",
                isRainRisk = true
            )
            95, 96, 99 -> WeatherDescriptor(
                category = WeatherConditionCategory.THUNDERSTORM,
                labelId = "Hujan Petir",
                labelEn = "Thunderstorm",
                isRainRisk = true
            )
            else -> WeatherDescriptor(
                category = WeatherConditionCategory.CLOUDY,
                labelId = "Berawan",
                labelEn = "Cloudy",
                isRainRisk = false
            )
        }
    }

    fun getCachedWeather(
        context: Context,
        city: IndonesiaCitySpec,
        allowExpired: Boolean = false,
        nowMillis: Long = System.currentTimeMillis()
    ): CityWeatherForecast? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawJson = prefs.getString("$KEY_JSON_PREFIX${city.id}", null) ?: return null
        val savedAt = prefs.getLong("$KEY_TS_PREFIX${city.id}", 0L)
        if (savedAt <= 0L) return null
        val isFresh = (nowMillis - savedAt) in 0 until CACHE_TTL_MS
        if (!allowExpired && !isFresh) return null
        return parseOpenMeteoJson(
            city = city,
            rawJson = rawJson,
            fetchedAtMillis = savedAt,
            isFromCache = !isFresh,
            nowMillis = nowMillis
        )
    }

    fun saveCachedWeatherJson(
        context: Context,
        cityId: String,
        rawJson: String,
        fetchedAtMillis: Long = System.currentTimeMillis()
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("$KEY_JSON_PREFIX$cityId", rawJson)
            .putLong("$KEY_TS_PREFIX$cityId", fetchedAtMillis)
            .apply()
    }

    suspend fun fetchWeatherForCity(
        context: Context,
        city: IndonesiaCitySpec,
        forceRefresh: Boolean = false,
        customFetcher: (suspend (IndonesiaCitySpec) -> String?)? = null
    ): Result<CityWeatherForecast> {
        if (!forceRefresh) {
            getCachedWeather(context, city, allowExpired = false)?.let { fresh ->
                return Result.success(fresh)
            }
        }

        val nowMillis = System.currentTimeMillis()
        val fetchedJson: String? = withContext(Dispatchers.IO) {
            if (customFetcher != null) {
                customFetcher(city)
            } else {
                executeHttpGet(buildOpenMeteoUrl(city))
            }
        }

        if (!fetchedJson.isNullOrBlank()) {
            val parsed = parseOpenMeteoJson(
                city = city,
                rawJson = fetchedJson,
                fetchedAtMillis = nowMillis,
                isFromCache = false,
                nowMillis = nowMillis
            )
            if (parsed != null) {
                saveCachedWeatherJson(context, city.id, fetchedJson, nowMillis)
                return Result.success(parsed)
            }
        }

        // Network or parse failure: fall back to expired cache if available
        getCachedWeather(context, city, allowExpired = true, nowMillis = nowMillis)?.let { stale ->
            return Result.success(stale.copy(isFromCache = true))
        }

        return Result.failure(IllegalStateException("Weather unavailable for ${city.name}"))
    }

    private fun executeHttpGet(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                response.body?.string()
            }
        } catch (_: Exception) {
            null
        }
    }

    fun parseOpenMeteoJson(
        city: IndonesiaCitySpec,
        rawJson: String,
        fetchedAtMillis: Long,
        isFromCache: Boolean = false,
        nowMillis: Long = fetchedAtMillis
    ): CityWeatherForecast? {
        return try {
            val root = JSONObject(rawJson)
            val current = root.optJSONObject("current") ?: return null

            val currentTemp = current.optDouble("temperature_2m", Double.NaN)
            if (currentTemp.isNaN()) return null
            val currentTempC = currentTemp.roundToInt()

            val apparentTempRaw = current.optDouble("apparent_temperature", currentTemp)
            val apparentTempC = if (apparentTempRaw.isNaN()) currentTempC else apparentTempRaw.roundToInt()
            val humidityPercent = current.optDouble("relative_humidity_2m", 75.0).roundToInt().coerceIn(0, 100)
            val windKmh = current.optDouble("wind_speed_10m", 0.0).roundToInt().coerceAtLeast(0)
            val isDay = current.optInt("is_day", 1) == 1
            val currentCode = current.optInt("weather_code", 2)

            val hourly = root.optJSONObject("hourly")
            val hTimes = hourly?.optJSONArray("time")
            val hTemps = hourly?.optJSONArray("temperature_2m")
            val hRainProbs = hourly?.optJSONArray("precipitation_probability")
            val hCodes = hourly?.optJSONArray("weather_code")

            val daily = root.optJSONObject("daily")
            val dailyMaxTemp = daily?.optJSONArray("temperature_2m_max")?.optDouble(0, Double.NaN)
            val dailyMinTemp = daily?.optJSONArray("temperature_2m_min")?.optDouble(0, Double.NaN)
            val dailyMaxRain = daily?.optJSONArray("precipitation_probability_max")?.optDouble(0, Double.NaN)

            val hourlyPoints = mutableListOf<Triple<Int, Int, Pair<Int, Int>>>() // hour -> (tempC, (rainProb, code))
            if (hTimes != null && hTemps != null) {
                val len = minOf(hTimes.length(), hTemps.length())
                for (i in 0 until len) {
                    val timeStr = hTimes.optString(i, "")
                    val hourVal = timeStr.substringAfter('T', "").take(2).toIntOrNull() ?: continue
                    val tVal = hTemps.optDouble(i, Double.NaN)
                    if (tVal.isNaN()) continue
                    val rVal = (hRainProbs?.optDouble(i, 0.0) ?: 0.0).roundToInt().coerceIn(0, 100)
                    val cVal = hCodes?.optInt(i, currentCode) ?: currentCode
                    hourlyPoints.add(Triple(hourVal, tVal.roundToInt(), rVal to cVal))
                }
            }

            val currentCal = Calendar.getInstance(cityTimeZone(city.utcOffsetHours)).apply {
                timeInMillis = nowMillis
            }
            val currentLocalHour = currentCal.get(Calendar.HOUR_OF_DAY)

            val currentRainFromField = if (current.has("precipitation_probability") && !current.isNull("precipitation_probability")) {
                current.optDouble("precipitation_probability", Double.NaN)
            } else {
                Double.NaN
            }
            val nearestHourlyRain = hourlyPoints.minByOrNull { abs(it.first - currentLocalHour) }?.third?.first
            val currentRainChance = when {
                !currentRainFromField.isNaN() -> currentRainFromField.roundToInt().coerceIn(0, 100)
                nearestHourlyRain != null -> nearestHourlyRain
                dailyMaxRain != null && !dailyMaxRain.isNaN() -> dailyMaxRain.roundToInt().coerceIn(0, 100)
                else -> 0
            }

            val minTempC = when {
                dailyMinTemp != null && !dailyMinTemp.isNaN() -> dailyMinTemp.roundToInt()
                hourlyPoints.isNotEmpty() -> hourlyPoints.minOf { it.second }
                else -> currentTempC
            }
            val maxTempC = when {
                dailyMaxTemp != null && !dailyMaxTemp.isNaN() -> dailyMaxTemp.roundToInt()
                hourlyPoints.isNotEmpty() -> hourlyPoints.maxOf { it.second }
                else -> currentTempC
            }
            val maxRainChance = when {
                dailyMaxRain != null && !dailyMaxRain.isNaN() -> dailyMaxRain.roundToInt().coerceIn(0, 100)
                hourlyPoints.isNotEmpty() -> hourlyPoints.maxOf { it.third.first }
                else -> currentRainChance
            }

            val periodSpecs = listOf(
                Triple("PAGI", "Pagi", "Morning") to 7,
                Triple("SIANG", "Siang", "Noon") to 12,
                Triple("SORE", "Sore", "Afternoon") to 16,
                Triple("MALAM", "Malam", "Night") to 20
            )
            val periods = periodSpecs.map { (labels, targetHour) ->
                val match = hourlyPoints.minByOrNull { abs(it.first - targetHour) }
                val temp = match?.second ?: currentTempC
                val rain = match?.third?.first ?: currentRainChance
                val code = match?.third?.second ?: currentCode
                val slotIsDay = targetHour in 6..17
                WeatherPeriodSlot(
                    key = labels.first,
                    labelId = labels.second,
                    labelEn = labels.third,
                    targetHour = targetHour,
                    tempC = temp,
                    rainChancePercent = rain,
                    weatherCode = code,
                    descriptor = describeWmoWeather(code, isDay = slotIsDay)
                )
            }

            val activePeriodKey = when (currentLocalHour) {
                in 5..10 -> "PAGI"
                in 11..14 -> "SIANG"
                in 15..18 -> "SORE"
                else -> "MALAM"
            }

            CityWeatherForecast(
                city = city,
                currentTempC = currentTempC,
                apparentTempC = apparentTempC,
                humidityPercent = humidityPercent,
                windKmh = windKmh,
                currentRainChancePercent = currentRainChance,
                minTempC = minTempC,
                maxTempC = maxTempC,
                maxRainChancePercent = maxRainChance,
                isDay = isDay,
                currentCode = currentCode,
                currentDescriptor = describeWmoWeather(currentCode, isDay = isDay),
                periods = periods,
                activePeriodKey = activePeriodKey,
                fetchedAtMillis = fetchedAtMillis,
                isFromCache = isFromCache
            )
        } catch (_: Exception) {
            null
        }
    }

    fun formatFetchedTime(fetchedAtMillis: Long, utcOffsetHours: Int): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US).apply {
            timeZone = cityTimeZone(utcOffsetHours)
        }
        return sdf.format(fetchedAtMillis)
    }

    private fun cityTimeZone(utcOffsetHours: Int): TimeZone {
        val sign = if (utcOffsetHours >= 0) "+" else "-"
        return TimeZone.getTimeZone("GMT$sign${abs(utcOffsetHours)}")
    }
}
