package com.example.util

import android.content.Context
import com.example.data.model.QuranAyahItem
import com.example.data.model.QuranCatalog
import com.example.data.model.QuranQariSpec
import com.example.data.model.QuranSurahSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class QuranSurahLoadResult(
    val surah: QuranSurahSpec,
    val ayahs: List<QuranAyahItem>,
    val isFromOnlineOrCache: Boolean,
    val sourceLabelId: String,
    val sourceLabelEn: String,
    val isOfflinePlaceholder: Boolean = false
)

data class QuranSearchHit(
    val surah: QuranSurahSpec,
    val ayahNumber: Int,
    val arabicPreview: String,
    val snippet: String
)

object QuranContentRepository {

    private data class CachedSurahPayload(
        val sourceLabel: String,
        val ayahs: List<QuranAyahItem>
    )

    private val memoryCache = mutableMapOf<Int, CachedSurahPayload>()

    internal fun clearMemoryCacheForTesting() {
        memoryCache.clear()
    }

    fun buildAyahAudioUrl(
        surahNumber: Int,
        ayahNumber: Int,
        qari: QuranQariSpec
    ): String {
        val s = surahNumber.coerceIn(1, 114).toString().padStart(3, '0')
        val a = ayahNumber.coerceAtLeast(1).toString().padStart(3, '0')
        return "https://everyayah.com/data/${qari.everyAyahFolder}/$s$a.mp3"
    }

    fun buildEquranAyahAudioFallback(
        surahNumber: Int,
        ayahNumber: Int,
        qari: QuranQariSpec
    ): String {
        val s = surahNumber.coerceIn(1, 114).toString().padStart(3, '0')
        val a = ayahNumber.coerceAtLeast(1).toString().padStart(3, '0')
        return "https://equran.nos.wjv-1.neo.id/audio-partial/${qari.equranCode}/$s$a.mp3"
    }

    suspend fun loadSurah(
        context: Context,
        surahNumber: Int,
        isEnglish: Boolean = false,
        forceRefresh: Boolean = false
    ): QuranSurahLoadResult = withContext(Dispatchers.IO) {
        val spec = QuranCatalog.findSurah(surahNumber)

        if (!forceRefresh) {
            memoryCache[spec.number]?.takeIf { it.ayahs.isNotEmpty() }?.let { cachedMem ->
                val needsEnFetch = isEnglish && cachedMem.ayahs.firstOrNull()?.translationEn.isNullOrBlank()
                if (!needsEnFetch) {
                    return@withContext QuranSurahLoadResult(
                        surah = spec,
                        ayahs = cachedMem.ayahs,
                        isFromOnlineOrCache = true,
                        sourceLabelId = "Tersimpan Offline (${cachedMem.sourceLabel})",
                        sourceLabelEn = "Saved Offline (${cachedMem.sourceLabel})"
                    )
                }
            }

            val cacheFile = getCacheFile(context, spec.number)
            if (cacheFile.exists()) {
                val parsed = parseNormalizedCacheFile(cacheFile)
                if (parsed != null && parsed.ayahs.isNotEmpty()) {
                    memoryCache[spec.number] = parsed
                    val needsEnFetch = isEnglish && parsed.ayahs.firstOrNull()?.translationEn.isNullOrBlank()
                    if (!needsEnFetch) {
                        return@withContext QuranSurahLoadResult(
                            surah = spec,
                            ayahs = parsed.ayahs,
                            isFromOnlineOrCache = true,
                            sourceLabelId = "Tersimpan Offline (${parsed.sourceLabel})",
                            sourceLabelEn = "Saved Offline (${parsed.sourceLabel})"
                        )
                    }
                }
            }
        }

        val onlinePayload = fetchSurahOnline(spec.number, includeEnglish = isEnglish)
        if (onlinePayload != null && onlinePayload.ayahs.isNotEmpty()) {
            memoryCache[spec.number] = onlinePayload
            writeNormalizedCacheFile(getCacheFile(context, spec.number), onlinePayload)
            return@withContext QuranSurahLoadResult(
                surah = spec,
                ayahs = onlinePayload.ayahs,
                isFromOnlineOrCache = true,
                sourceLabelId = onlinePayload.sourceLabel,
                sourceLabelEn = onlinePayload.sourceLabel
            )
        }

        // Fallback to cached file even if English translation wasn't present
        val cacheFile = getCacheFile(context, spec.number)
        if (cacheFile.exists()) {
            val parsed = parseNormalizedCacheFile(cacheFile)
            if (parsed != null && parsed.ayahs.isNotEmpty()) {
                memoryCache[spec.number] = parsed
                return@withContext QuranSurahLoadResult(
                    surah = spec,
                    ayahs = parsed.ayahs,
                    isFromOnlineOrCache = true,
                    sourceLabelId = "Tersimpan Offline (${parsed.sourceLabel})",
                    sourceLabelEn = "Saved Offline (${parsed.sourceLabel})"
                )
            }
        }

        val offlineBuiltIn = QuranOfflineData.getOfflineSurah(spec.number)
        if (!offlineBuiltIn.isNullOrEmpty()) {
            return@withContext QuranSurahLoadResult(
                surah = spec,
                ayahs = offlineBuiltIn,
                isFromOnlineOrCache = true,
                sourceLabelId = "Mushaf Bawaan Offline (Rasm Utsmani)",
                sourceLabelEn = "Built-in Offline Mushaf (Uthmani Script)"
            )
        }

        val placeholder = listOf(
            QuranAyahItem(
                numberInSurah = 1,
                arabicText = QuranCatalog.BISMILLAH_ARABIC,
                latinText = QuranCatalog.BISMILLAH_LATIN,
                translationId = "Surah ${spec.nameLatin} (${spec.versesCount} ayat, ${spec.revelationType}) belum diunduh ke penyimpanan offline. Aktifkan internet sekali untuk memuat & menyimpannya secara otomatis.",
                translationEn = "Surah ${spec.nameLatin} (${spec.versesCount} ayahs, ${spec.revelationType}) has not been cached offline yet. Connect to the internet once to download & cache it automatically."
            )
        )
        QuranSurahLoadResult(
            surah = spec,
            ayahs = placeholder,
            isFromOnlineOrCache = false,
            sourceLabelId = "Mode Offline • Ketuk Muat Ulang saat terhubung internet",
            sourceLabelEn = "Offline Mode • Tap Reload when connected to internet",
            isOfflinePlaceholder = true
        )
    }

    suspend fun isSurahCachedOffline(context: Context, surahNumber: Int): Boolean =
        withContext(Dispatchers.IO) {
            QuranOfflineData.hasOfflineSurah(surahNumber) || getCacheFile(context, surahNumber).exists()
        }

    suspend fun downloadEssentialOfflinePack(
        context: Context,
        onProgress: (done: Int, total: Int, currentName: String) -> Unit = { _, _, _ -> }
    ): Int = withContext(Dispatchers.IO) {
        val targets = ((78..114).toList() + listOf(1, 18, 36, 55, 56, 67)).distinct().sorted()
        var cachedCount = 0
        targets.forEachIndexed { idx, surahNum ->
            val spec = QuranCatalog.findSurah(surahNum)
            withContext(Dispatchers.Main) {
                onProgress(idx, targets.size, spec.nameLatin)
            }
            val file = getCacheFile(context, surahNum)
            if (file.exists()) {
                cachedCount++
            } else {
                val fetched = fetchSurahOnline(surahNum, includeEnglish = true)
                if (fetched != null && fetched.ayahs.isNotEmpty()) {
                    memoryCache[surahNum] = fetched
                    writeNormalizedCacheFile(file, fetched)
                    cachedCount++
                } else if (QuranOfflineData.hasOfflineSurah(surahNum)) {
                    cachedCount++
                }
            }
        }
        withContext(Dispatchers.Main) {
            onProgress(targets.size, targets.size, "Selesai")
        }
        cachedCount
    }

    suspend fun searchVersesAndSurahs(
        context: Context,
        query: String,
        isEnglish: Boolean
    ): List<QuranSearchHit> = withContext(Dispatchers.IO) {
        val clean = query.trim().lowercase()
        if (clean.length < 2) return@withContext emptyList()
        val hits = ArrayList<QuranSearchHit>()

        // 1. Cari pada metadata 114 Surah
        QuranCatalog.ALL_SURAHS.forEach { spec ->
            if (spec.nameLatin.lowercase().contains(clean) ||
                spec.meaningId.lowercase().contains(clean) ||
                spec.meaningEn.lowercase().contains(clean) ||
                spec.number.toString() == clean
            ) {
                val meaning = if (isEnglish) spec.meaningEn else spec.meaningId
                hits.add(
                    QuranSearchHit(
                        surah = spec,
                        ayahNumber = 1,
                        arabicPreview = spec.nameArabic,
                        snippet = "${spec.number}. ${spec.nameLatin} ($meaning) • ${spec.versesCount} ${if (isEnglish) "verses" else "ayat"}"
                    )
                )
            }
        }

        // 2. Cari pada ayat tersimpan di cache disk + data bawaan
        for (spec in QuranCatalog.ALL_SURAHS) {
            if (hits.size >= 45) break
            val ayahs = memoryCache[spec.number]?.ayahs
                ?: getCacheFile(context, spec.number).takeIf { it.exists() }?.let { parseNormalizedCacheFile(it)?.ayahs }
                ?: QuranOfflineData.getOfflineSurah(spec.number)
                ?: continue
            for (ayah in ayahs) {
                val matched = ayah.translationId.lowercase().contains(clean) ||
                    ayah.translationEn.lowercase().contains(clean) ||
                    ayah.latinText.lowercase().contains(clean) ||
                    ayah.arabicText.contains(clean)
                if (matched) {
                    val snippetText = if (isEnglish && ayah.translationEn.isNotBlank()) {
                        ayah.translationEn
                    } else {
                        ayah.translationId.ifBlank { ayah.latinText }
                    }
                    hits.add(
                        QuranSearchHit(
                            surah = spec,
                            ayahNumber = ayah.numberInSurah,
                            arabicPreview = ayah.arabicText,
                            snippet = snippetText
                        )
                    )
                    if (hits.size >= 45) break
                }
            }
        }

        hits
    }

    private fun getCacheFile(context: Context, surahNumber: Int): File {
        val dir = File(context.filesDir, "quran_cache")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "surah_$surahNumber.json")
    }

    private fun httpGetJson(urlString: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6500
                readTimeout = 9000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "WaliasuhCareAndroid/1.0")
            }
            if (conn.responseCode !in 200..299) return null
            conn.inputStream.bufferedReader().use { it.readText() }.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun fetchSurahOnline(
        surahNumber: Int,
        includeEnglish: Boolean
    ): CachedSurahPayload? {
        // 1. Primary: eQuran.id v2 (Arab Utsmani, Latin, Terjemahan Kemenag RI, Audio per ayat)
        val equranBody = httpGetJson("https://equran.id/api/v2/surat/$surahNumber")
        if (equranBody != null) {
            val englishMap = if (includeEnglish) fetchEnglishEditionMap(surahNumber) else emptyMap()
            val parsed = parseEquranV2Json(equranBody, englishMap)
            if (parsed.isNotEmpty()) {
                return CachedSurahPayload(
                    sourceLabel = "Kemenag RI (eQuran)",
                    ayahs = parsed
                )
            }
        }

        // 2. Fallback: AlQuran Cloud multi-editions
        val cloudBody = httpGetJson(
            "https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,en.transliteration,id.indonesian,en.sahih"
        )
        if (cloudBody != null) {
            val parsed = parseAlQuranCloudJson(cloudBody)
            if (parsed.isNotEmpty()) {
                return CachedSurahPayload(
                    sourceLabel = "Mushaf Utsmani (AlQuran Cloud)",
                    ayahs = parsed
                )
            }
        }
        return null
    }

    private fun fetchEnglishEditionMap(surahNumber: Int): Map<Int, String> {
        val body = httpGetJson("https://api.alquran.cloud/v1/surah/$surahNumber/en.sahih") ?: return emptyMap()
        return try {
            val root = JSONObject(body)
            val data = root.optJSONObject("data") ?: return emptyMap()
            val ayahs = data.optJSONArray("ayahs") ?: return emptyMap()
            val result = HashMap<Int, String>(ayahs.length())
            for (i in 0 until ayahs.length()) {
                val obj = ayahs.optJSONObject(i) ?: continue
                val num = obj.optInt("numberInSurah", i + 1)
                val text = obj.optString("text", "").trim()
                if (text.isNotBlank()) result[num] = text
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun parseEquranV2Json(
        jsonString: String,
        englishByAyah: Map<Int, String>
    ): List<QuranAyahItem> {
        return try {
            val root = JSONObject(jsonString)
            val data = root.optJSONObject("data") ?: return emptyList()
            val ayatArr = data.optJSONArray("ayat") ?: return emptyList()
            val items = ArrayList<QuranAyahItem>(ayatArr.length())
            for (i in 0 until ayatArr.length()) {
                val obj = ayatArr.optJSONObject(i) ?: continue
                val num = obj.optInt("nomorAyat", i + 1)
                val arabic = obj.optString("teksArab", "").trim()
                val latin = obj.optString("teksLatin", "").trim()
                val idText = obj.optString("teksIndonesia", "").trim()
                val audioObj = obj.optJSONObject("audio")
                val audioPrimary = audioObj?.optString("05", "")?.trim().orEmpty()
                val audioFallback = audioObj?.optString("03", "")?.trim().orEmpty()
                if (arabic.isNotBlank()) {
                    items.add(
                        QuranAyahItem(
                            numberInSurah = num,
                            arabicText = arabic,
                            latinText = latin,
                            translationId = idText,
                            translationEn = englishByAyah[num].orEmpty(),
                            audioUrlPrimary = audioPrimary,
                            audioUrlFallback = audioFallback
                        )
                    )
                }
            }
            items
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseAlQuranCloudJson(jsonString: String): List<QuranAyahItem> {
        return try {
            val root = JSONObject(jsonString)
            val dataArr = root.optJSONArray("data") ?: return emptyList()
            if (dataArr.length() == 0) return emptyList()
            val arAyahs = dataArr.optJSONObject(0)?.optJSONArray("ayahs") ?: return emptyList()
            val latinAyahs = dataArr.optJSONObject(1)?.optJSONArray("ayahs")
            val idAyahs = dataArr.optJSONObject(2)?.optJSONArray("ayahs")
            val enAyahs = dataArr.optJSONObject(3)?.optJSONArray("ayahs")
            val items = ArrayList<QuranAyahItem>(arAyahs.length())
            for (i in 0 until arAyahs.length()) {
                val arObj = arAyahs.optJSONObject(i) ?: continue
                val num = arObj.optInt("numberInSurah", i + 1)
                val arabic = arObj.optString("text", "").trim()
                val latin = latinAyahs?.optJSONObject(i)?.optString("text", "")?.trim().orEmpty()
                val idText = idAyahs?.optJSONObject(i)?.optString("text", "")?.trim().orEmpty()
                val enText = enAyahs?.optJSONObject(i)?.optString("text", "")?.trim().orEmpty()
                if (arabic.isNotBlank()) {
                    items.add(
                        QuranAyahItem(
                            numberInSurah = num,
                            arabicText = arabic,
                            latinText = latin,
                            translationId = idText,
                            translationEn = enText
                        )
                    )
                }
            }
            items
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseNormalizedCacheFile(file: File): CachedSurahPayload? {
        return try {
            val obj = JSONObject(file.readText())
            val label = obj.optString("sourceLabel", "Offline Cache")
            val arr = obj.optJSONArray("ayahs") ?: return null
            val list = ArrayList<QuranAyahItem>(arr.length())
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                list.add(
                    QuranAyahItem(
                        numberInSurah = item.optInt("n", i + 1),
                        arabicText = item.optString("ar", ""),
                        latinText = item.optString("la", ""),
                        translationId = item.optString("id", ""),
                        translationEn = item.optString("en", ""),
                        audioUrlPrimary = item.optString("au1", ""),
                        audioUrlFallback = item.optString("au2", "")
                    )
                )
            }
            CachedSurahPayload(sourceLabel = label, ayahs = list)
        } catch (_: Exception) {
            null
        }
    }

    private fun writeNormalizedCacheFile(file: File, payload: CachedSurahPayload) {
        try {
            val arr = JSONArray()
            payload.ayahs.forEach { a ->
                arr.put(
                    JSONObject().apply {
                        put("n", a.numberInSurah)
                        put("ar", a.arabicText)
                        put("la", a.latinText)
                        put("id", a.translationId)
                        put("en", a.translationEn)
                        put("au1", a.audioUrlPrimary)
                        put("au2", a.audioUrlFallback)
                    }
                )
            }
            val root = JSONObject().apply {
                put("sourceLabel", payload.sourceLabel)
                put("ayahs", arr)
            }
            file.writeText(root.toString())
        } catch (_: Exception) {
        }
    }
}
