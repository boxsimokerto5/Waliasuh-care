package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.model.ChatMediaLocalSave
import com.example.data.model.ParentMessage
import com.example.data.model.WaliGalleryLocalSave
import com.example.data.model.WaliGalleryPost
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object WaliGalleryMediaHelper {
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .build()
    }

    fun compressGalleryPhotoBytes(context: Context, imageUri: Uri): ByteArray? {
        return runCatching {
            val orientation = runCatching {
                context.contentResolver.openInputStream(imageUri)?.use { exifStream ->
                    ExifInterface(exifStream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                }
            }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOpts)
            }
            val srcW = boundsOpts.outWidth
            val srcH = boundsOpts.outHeight
            var sampleSize = 1
            if (srcW > 1440 || srcH > 1440) {
                while ((srcW / sampleSize) > 1440 || (srcH / sampleSize) > 1440) {
                    sampleSize *= 2
                }
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = context.contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOpts)
            } ?: return@runCatching null

            val rotated = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(decoded, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(decoded, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(decoded, 270f)
                else -> decoded
            }

            val maxDim = 1280
            val scaled = if (rotated.width > maxDim || rotated.height > maxDim) {
                val ratio = minOf(maxDim.toFloat() / rotated.width, maxDim.toFloat() / rotated.height)
                val targetW = (rotated.width * ratio).toInt().coerceAtLeast(1)
                val targetH = (rotated.height * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(rotated, targetW, targetH, true).also {
                    if (it !== rotated) rotated.recycle()
                }
            } else {
                rotated
            }

            @Suppress("DEPRECATION")
            val webpFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                Bitmap.CompressFormat.WEBP
            }

            var bestBytes: ByteArray? = null
            for (quality in intArrayOf(82, 72, 62, 50)) {
                val attempt = ByteArrayOutputStream().use { out ->
                    if (scaled.compress(webpFormat, quality, out)) out.toByteArray() else null
                }
                if (attempt != null && attempt.isNotEmpty()) {
                    bestBytes = attempt
                    if (attempt.size <= 220 * 1024) break
                }
            }
            scaled.recycle()
            bestBytes?.takeIf { it.isNotEmpty() }
        }.getOrNull()
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val m = Matrix().apply { postRotate(angle) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, m, true).also {
            if (it !== source) source.recycle()
        }
    }

    fun isLocalSaveAccessible(context: Context, localUri: String): Boolean {
        val clean = localUri.trim()
        if (clean.isBlank()) return false
        return runCatching {
            val uri = Uri.parse(clean)
            if (uri.scheme == "file" || clean.startsWith("/")) {
                val path = uri.path ?: clean
                File(path).let { it.exists() && it.length() > 0L }
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.read() != -1
                } ?: false
            }
        }.getOrDefault(false)
    }

    suspend fun savePostPhotoToParentGallery(
        context: Context,
        post: WaliGalleryPost
    ): Result<WaliGalleryLocalSave> = withContext(Dispatchers.IO) {
        val candidateUrls = listOf(post.photoUrl.trim(), post.photoFallbackUrl.trim())
            .filter { it.isNotBlank() }
            .distinct()
        val downloaded = downloadFromCandidateUrls(context, candidateUrls)
        val bytes = downloaded.getOrElse { return@withContext Result.failure(it) }

        val safeWali = post.waliName
            .replace(Regex("[^A-Za-z0-9]"), "_")
            .take(18)
            .ifBlank { "WaliAsuh" }
        val (ext, mimeType) = resolveExtensionAndMime(candidateUrls)
        val displayFileName = "WaliAsuh_${safeWali}_${post.id}.$ext"
        val now = System.currentTimeMillis()
        writeImageBytesToDeviceGallery(context, displayFileName, mimeType, bytes).map { savedUri ->
            WaliGalleryLocalSave(
                postId = post.id,
                localGalleryUri = savedUri,
                displayFileName = displayFileName,
                savedAt = now
            )
        }
    }

    suspend fun saveChatPhotoToDeviceGallery(
        context: Context,
        message: ParentMessage
    ): Result<ChatMediaLocalSave> = withContext(Dispatchers.IO) {
        val candidateUrls = listOf(message.photoUrl.trim(), message.photoFallbackUrl.trim())
            .filter { it.isNotBlank() }
            .distinct()
        val downloaded = downloadFromCandidateUrls(context, candidateUrls)
        val bytes = downloaded.getOrElse { return@withContext Result.failure(it) }

        val safeSender = message.senderName
            .replace(Regex("[^A-Za-z0-9]"), "_")
            .take(18)
            .ifBlank { if (message.senderRole.equals("PARENT", true)) "OrangTua" else "WaliAsuh" }
        val (ext, mimeType) = resolveExtensionAndMime(candidateUrls)
        val displayFileName = "Chat_${safeSender}_${message.id}.$ext"
        val now = System.currentTimeMillis()
        writeImageBytesToDeviceGallery(context, displayFileName, mimeType, bytes).map { savedUri ->
            ChatMediaLocalSave(
                messageId = message.id,
                localGalleryUri = savedUri,
                displayFileName = displayFileName,
                savedAt = now
            )
        }
    }

    suspend fun saveCompressedChatBytesToLocalGallery(
        context: Context,
        messageId: Long,
        senderName: String,
        bytes: ByteArray
    ): Result<ChatMediaLocalSave> = withContext(Dispatchers.IO) {
        val safeSender = senderName
            .replace(Regex("[^A-Za-z0-9]"), "_")
            .take(18)
            .ifBlank { "WaliAsuhCare" }
        val displayFileName = "Chat_${safeSender}_${messageId}.webp"
        val now = System.currentTimeMillis()
        writeImageBytesToDeviceGallery(context, displayFileName, "image/webp", bytes).map { savedUri ->
            ChatMediaLocalSave(
                messageId = messageId,
                localGalleryUri = savedUri,
                displayFileName = displayFileName,
                savedAt = now
            )
        }
    }

    private fun resolveExtensionAndMime(candidateUrls: List<String>): Pair<String, String> {
        val ext = when {
            candidateUrls.any { it.contains(".png", ignoreCase = true) } -> "png"
            candidateUrls.any { it.contains(".jpg", ignoreCase = true) || it.contains(".jpeg", ignoreCase = true) } -> "jpg"
            else -> "webp"
        }
        val mimeType = when (ext) {
            "png" -> "image/png"
            "jpg" -> "image/jpeg"
            else -> "image/webp"
        }
        return ext to mimeType
    }

    private fun downloadFromCandidateUrls(context: Context, candidateUrls: List<String>): Result<ByteArray> {
        if (candidateUrls.isEmpty()) {
            return Result.failure(Exception("File foto sudah dibersihkan dari server atau URL tidak tersedia."))
        }
        var downloadedBytes: ByteArray? = null
        var lastError: Throwable? = null
        for (url in candidateUrls) {
            val attempt = runCatching { fetchBytesFromUrlOrUri(context, url) }
            val bytes = attempt.getOrNull()
            if (bytes != null && bytes.isNotEmpty()) {
                downloadedBytes = bytes
                break
            } else {
                lastError = attempt.exceptionOrNull()
            }
        }
        val bytes = downloadedBytes ?: return Result.failure(
            lastError ?: Exception("Gagal mengunduh foto dari server.")
        )
        return Result.success(bytes)
    }

    private fun writeImageBytesToDeviceGallery(
        context: Context,
        displayFileName: String,
        mimeType: String,
        bytes: ByteArray
    ): Result<String> {
        // Simpan ke MediaStore (Pictures/WaliAsuhCare/) di Android 10+ tanpa izin penyimpanan sensitif
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !Build.FINGERPRINT.equals("robolectric", ignoreCase = true)
        ) {
            val mediaStoreRes = runCatching {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, displayFileName)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/WaliAsuhCare")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val itemUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: error("Gagal membuat entri MediaStore")
                try {
                    resolver.openOutputStream(itemUri)?.use { out ->
                        out.write(bytes)
                        out.flush()
                    } ?: error("Gagal membuka OutputStream MediaStore")
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(itemUri, values, null, null)
                    itemUri.toString()
                } catch (e: Exception) {
                    runCatching { resolver.delete(itemUri, null, null) }
                    throw e
                }
            }
            mediaStoreRes.getOrNull()?.let { return Result.success(it) }
        }

        // Fallback untuk API 24-28 atau environment unit test
        return runCatching {
            val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
            val galleryDir = File(baseDir, "WaliAsuhCare").apply { mkdirs() }
            val dest = File(galleryDir, displayFileName)
            dest.writeBytes(bytes)
            Uri.fromFile(dest).toString()
        }
    }

    private fun fetchBytesFromUrlOrUri(context: Context, urlOrUri: String): ByteArray {
        val clean = urlOrUri.trim()
        if (clean.startsWith("http://", ignoreCase = true) ||
            clean.startsWith("https://", ignoreCase = true)
        ) {
            val req = Request.Builder().url(clean).get().build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) error("HTTP ${resp.code}")
                return resp.body?.bytes() ?: error("Empty response body")
            }
        }
        val uri = Uri.parse(clean)
        if (uri.scheme == "file" || clean.startsWith("/")) {
            val file = File(uri.path ?: clean)
            if (!file.exists()) error("Local file not found")
            return file.readBytes()
        }
        return context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Unable to open URI")
    }
}
