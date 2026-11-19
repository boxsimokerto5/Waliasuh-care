package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.example.data.model.KonselingRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max
import kotlin.math.roundToInt

data class KonselingPhotoSlot(
    val localPath: String = "",
    val remoteUrl: String = ""
) {
    val hasAny: Boolean
        get() = localPath.isNotBlank() || remoteUrl.isNotBlank()

    fun displayModel(): Any? {
        if (localPath.isNotBlank()) {
            val file = File(localPath)
            if (file.exists() && file.length() > 0L) return file
        }
        return remoteUrl.trim().takeIf { it.isNotBlank() }
    }
}

object KonselingPhotoHelper {
    const val MAX_PHOTOS = 3
    private const val KONSELING_DIR = "konseling_photos"
    private const val KONSELING_CACHE_DIR = "konseling_photos_cache"
    private const val MAX_DIMENSION_PX = 1080
    private const val JPEG_QUALITY = 80

    fun parsePhotoList(raw: String?): List<String> {
        val clean = raw?.trim().orEmpty()
        if (clean.isBlank()) return emptyList()
        return runCatching {
            if (clean.startsWith("[")) {
                val arr = JSONArray(clean)
                buildList {
                    for (i in 0 until arr.length()) {
                        add(arr.optString(i, "").trim())
                    }
                }.take(MAX_PHOTOS)
            } else {
                clean.split("|||")
                    .map { it.trim() }
                    .take(MAX_PHOTOS)
            }
        }.getOrDefault(emptyList())
    }

    fun serializePhotoList(items: List<String>): String {
        val trimmed = items.take(MAX_PHOTOS).map { it.trim() }
        if (trimmed.all { it.isBlank() }) return ""
        return JSONArray().apply {
            trimmed.forEach { put(it) }
        }.toString()
    }

    fun extractSlots(record: KonselingRecord): List<KonselingPhotoSlot> {
        val paths = parsePhotoList(record.photoPaths)
        val urls = parsePhotoList(record.photoUrls)
        val count = max(paths.size, urls.size).coerceAtMost(MAX_PHOTOS)
        return buildList {
            for (i in 0 until count) {
                val p = paths.getOrNull(i).orEmpty()
                val u = urls.getOrNull(i).orEmpty()
                val slot = KonselingPhotoSlot(localPath = p, remoteUrl = u)
                if (slot.hasAny) add(slot)
            }
        }
    }

    fun packSlots(slots: List<KonselingPhotoSlot>): Pair<String, String> {
        val clean = slots.filter { it.hasAny }.take(MAX_PHOTOS)
        if (clean.isEmpty()) return "" to ""
        val pathsJson = serializePhotoList(clean.map { it.localPath })
        val urlsJson = serializePhotoList(clean.map { it.remoteUrl })
        return pathsJson to urlsJson
    }

    fun countPhotos(record: KonselingRecord): Int = extractSlots(record).size

    suspend fun compressAndSavePhoto(
        context: Context,
        sourceUri: Uri,
        idHint: Long = System.currentTimeMillis()
    ): File? = withContext(Dispatchers.IO) {
        try {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            val srcW = boundsOpts.outWidth
            val srcH = boundsOpts.outHeight
            if (srcW <= 0 || srcH <= 0) return@withContext null

            var inSample = 1
            while ((srcW / inSample) > MAX_DIMENSION_PX * 2 || (srcH / inSample) > MAX_DIMENSION_PX * 2) {
                inSample *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = inSample }
            val decoded = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: return@withContext null

            val rotationDeg = runCatching {
                context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                }
            }.getOrNull() ?: 0f

            val rotated = if (rotationDeg != 0f) {
                val matrix = Matrix().apply { postRotate(rotationDeg) }
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true).also {
                    if (it !== decoded) decoded.recycle()
                }
            } else {
                decoded
            }

            val maxSide = max(rotated.width, rotated.height)
            val finalBmp = if (maxSide > MAX_DIMENSION_PX) {
                val scale = MAX_DIMENSION_PX.toFloat() / maxSide.toFloat()
                val newW = (rotated.width * scale).roundToInt().coerceAtLeast(1)
                val newH = (rotated.height * scale).roundToInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(rotated, newW, newH, true).also {
                    if (it !== rotated) rotated.recycle()
                }
            } else {
                rotated
            }

            val outDir = File(context.filesDir, KONSELING_DIR).apply { if (!exists()) mkdirs() }
            val outFile = File(outDir, "konseling_${idHint}_${System.nanoTime()}.jpg")
            FileOutputStream(outFile).use { fos ->
                finalBmp.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos)
            }
            finalBmp.recycle()
            if (outFile.exists() && outFile.length() > 0L) outFile else null
        } catch (_: Exception) {
            null
        }
    }

    fun deleteLocalPhotoFile(path: String) {
        if (path.isBlank()) return
        runCatching {
            val f = File(path)
            if (f.exists()) f.delete()
        }
    }

    fun deleteRemovedLocalPhotos(oldRecord: KonselingRecord?, newSlots: List<KonselingPhotoSlot>) {
        if (oldRecord == null) return
        val keepSet = newSlots.mapNotNull { it.localPath.trim().takeIf(String::isNotBlank) }.toSet()
        extractSlots(oldRecord).forEach { slot ->
            val p = slot.localPath.trim()
            if (p.isNotBlank() && p !in keepSet) {
                deleteLocalPhotoFile(p)
            }
        }
    }

    suspend fun resolveOrFetchPhotoFiles(
        context: Context,
        record: KonselingRecord
    ): List<File> = withContext(Dispatchers.IO) {
        val slots = extractSlots(record)
        if (slots.isEmpty()) return@withContext emptyList()
        val cacheDir = File(context.cacheDir, KONSELING_CACHE_DIR).apply { if (!exists()) mkdirs() }

        slots.mapIndexedNotNull { idx, slot ->
            if (slot.localPath.isNotBlank()) {
                val localFile = File(slot.localPath)
                if (localFile.exists() && localFile.length() > 0L) {
                    return@mapIndexedNotNull localFile
                }
            }
            val url = slot.remoteUrl.trim()
            if (!url.startsWith("http")) return@mapIndexedNotNull null

            val hashTag = url.hashCode().toUInt().toString(16)
            val cached = File(cacheDir, "k_${record.id}_${idx}_$hashTag.jpg")
            if (cached.exists() && cached.length() > 0L) {
                return@mapIndexedNotNull cached
            }

            runCatching {
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10_000
                    readTimeout = 15_000
                }
                conn.inputStream.use { input ->
                    FileOutputStream(cached).use { out -> input.copyTo(out) }
                }
                if (cached.exists() && cached.length() > 0L) cached else null
            }.getOrNull()
        }
    }

    suspend fun preloadPhotosForPdfRecords(
        context: Context,
        records: List<KonselingRecord>
    ): Map<Long, List<File>> = withContext(Dispatchers.IO) {
        val map = LinkedHashMap<Long, List<File>>()
        for (rec in records) {
            val files = resolveOrFetchPhotoFiles(context, rec)
            if (files.isNotEmpty()) {
                map[rec.id] = files
            }
        }
        map
    }

    fun decodePdfBitmap(file: File, maxDimension: Int = 640): Bitmap? {
        if (!file.exists() || file.length() <= 0L) return null
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            var inSample = 1
            while ((bounds.outWidth / inSample) > maxDimension * 2 ||
                (bounds.outHeight / inSample) > maxDimension * 2
            ) {
                inSample *= 2
            }

            val opts = BitmapFactory.Options().apply {
                inSampleSize = inSample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return@runCatching null
            val maxSide = max(decoded.width, decoded.height)
            if (maxSide > maxDimension) {
                val scale = maxDimension.toFloat() / maxSide.toFloat()
                val targetW = (decoded.width * scale).roundToInt().coerceAtLeast(1)
                val targetH = (decoded.height * scale).roundToInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(decoded, targetW, targetH, true).also {
                    if (it !== decoded) decoded.recycle()
                }
            } else {
                decoded
            }
        }.getOrNull()
    }
}
