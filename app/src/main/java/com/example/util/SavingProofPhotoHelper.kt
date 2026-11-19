package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.model.SavingRetentionHelper
import com.example.data.model.SavingTransaction
import com.example.data.remote.pocketbase.PocketBaseConfig
import com.example.data.remote.pocketbase.PocketBaseIdCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max
import kotlin.math.roundToInt

object SavingProofPhotoHelper {
    private const val PROOF_DIR = "saving_proofs"
    private const val MAX_DIMENSION_PX = 1080
    private const val JPEG_QUALITY = 78

    fun createTempCameraImageUri(context: Context): Pair<Uri, File> {
        val dir = File(context.cacheDir, PROOF_DIR).apply { if (!exists()) mkdirs() }
        val file = File(dir, "cam_proof_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        return uri to file
    }

    suspend fun compressAndSaveProofPhoto(
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

            val rotationDeg = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f

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

            val outDir = File(context.filesDir, PROOF_DIR).apply { if (!exists()) mkdirs() }
            val outFile = File(outDir, "proof_${idHint}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { fos ->
                finalBmp.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos)
            }
            finalBmp.recycle()
            outFile
        } catch (_: Exception) {
            null
        }
    }

    fun deleteLocalProofFile(path: String) {
        if (path.isBlank()) return
        runCatching {
            val f = File(path)
            if (f.exists()) f.delete()
        }
    }

    fun buildRemoteProofUrl(context: Context, tx: SavingTransaction): String? {
        if (SavingRetentionHelper.isProofExpired(tx)) return null
        val fileName = tx.proofPhotoRemoteFile.trim()
        if (fileName.isBlank()) return null
        val baseUrl = PocketBaseConfig.getBaseUrl(context).trim().trimEnd('/')
        if (!baseUrl.startsWith("http")) return null
        val pbRecordId = PocketBaseIdCodec.toPbId(tx.id)
        return "$baseUrl/api/files/saving_transactions/$pbRecordId/$fileName"
    }

    fun resolvePhotoModel(context: Context, tx: SavingTransaction): Any? {
        if (SavingRetentionHelper.isProofExpired(tx)) return null
        if (tx.proofPhotoLocalPath.isNotBlank()) {
            val localFile = File(tx.proofPhotoLocalPath)
            if (localFile.exists() && localFile.length() > 0L) {
                return localFile
            }
        }
        return buildRemoteProofUrl(context, tx)
    }

    suspend fun resolveOrFetchProofFile(context: Context, tx: SavingTransaction): File? = withContext(Dispatchers.IO) {
        if (SavingRetentionHelper.isProofExpired(tx)) return@withContext null
        if (tx.proofPhotoLocalPath.isNotBlank()) {
            val f = File(tx.proofPhotoLocalPath)
            if (f.exists() && f.length() > 0L) return@withContext f
        }
        val remoteUrl = buildRemoteProofUrl(context, tx) ?: return@withContext null
        return@withContext try {
            val dir = File(context.cacheDir, PROOF_DIR).apply { if (!exists()) mkdirs() }
            val dest = File(dir, "remote_proof_${tx.id}.jpg")
            val conn = (URL(remoteUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 15_000
            }
            conn.inputStream.use { input ->
                FileOutputStream(dest).use { out -> input.copyTo(out) }
            }
            if (dest.exists() && dest.length() > 0L) dest else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Merangkai foto bukti dengan footer struk rapi (Nama Anak, Nominal, Keperluan, Sisa Saldo, Tanggal)
     * sehingga saat dikirim via WhatsApp dan otomatis masuk ke galeri HP orang tua, gambar tetap informatif.
     */
    suspend fun buildShareableReceiptImage(
        context: Context,
        tx: SavingTransaction,
        currentBalance: Long,
        schoolName: String,
        isEn: Boolean = false
    ): File? = withContext(Dispatchers.IO) {
        try {
            val sourceFile = resolveOrFetchProofFile(context, tx)
            val sourceBmp = sourceFile?.let { BitmapFactory.decodeFile(it.absolutePath) }

            val targetWidth = 1080
            val photoHeight = if (sourceBmp != null && sourceBmp.width > 0) {
                ((sourceBmp.height.toFloat() / sourceBmp.width.toFloat()) * targetWidth).roundToInt().coerceIn(480, 1200)
            } else {
                0
            }
            val footerHeight = 320
            val totalHeight = photoHeight + footerHeight

            val outBmp = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(outBmp)
            canvas.drawColor(Color.WHITE)

            if (sourceBmp != null && photoHeight > 0) {
                val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                val destRect = Rect(0, 0, targetWidth, photoHeight)
                canvas.drawBitmap(sourceBmp, null, destRect, bmpPaint)
                sourceBmp.recycle()
            }

            // Footer struk gelap kontras tinggi agar mudah dibaca di Galeri Orang Tua
            val footerTop = photoHeight.toFloat()
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F3D34")
            }
            canvas.drawRect(RectF(0f, footerTop, targetWidth.toFloat(), totalHeight.toFloat()), footerPaint)

            val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (tx.transactionType == SavingTransaction.TYPE_EXPENSE) {
                    Color.parseColor("#EF4444")
                } else {
                    Color.parseColor("#10B981")
                }
            }
            canvas.drawRect(RectF(0f, footerTop, targetWidth.toFloat(), footerTop + 10f), accentPaint)

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#A7F3D0")
                textSize = 26f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 36f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E2E8F0")
                textSize = 27f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#CBD5E1")
                textSize = 23f
            }

            val isExpense = tx.transactionType == SavingTransaction.TYPE_EXPENSE
            val sign = if (isExpense) "-" else "+"
            val headerTitle = if (isEn) {
                if (isExpense) "SAVINGS EXPENSE PROOF • ${schoolName.ifBlank { "Wali Asuh Care" }}"
                else "SAVINGS DEPOSIT RECEIPT • ${schoolName.ifBlank { "Wali Asuh Care" }}"
            } else {
                if (isExpense) "BUKTI PENGELUARAN TABUNGAN • ${schoolName.ifBlank { "Wali Asuh Care" }}"
                else "BUKTI TITIP MASUK TABUNGAN • ${schoolName.ifBlank { "Wali Asuh Care" }}"
            }
            val purposeText = tx.purpose.ifBlank { if (isExpense) "Keperluan Anak Asuh" else "Titipan Tabungan" }
            val dateText = DateUtils.formatFullDate(tx.transactionDate)
            val balanceText = SavingRetentionHelper.formatRupiah(currentBalance)

            canvas.drawText(headerTitle.take(58), 38f, footerTop + 54f, titlePaint)
            canvas.drawText("${tx.studentName}  ($sign${SavingRetentionHelper.formatRupiah(tx.amount)})".take(46), 38f, footerTop + 108f, mainPaint)
            canvas.drawText("${if (isEn) "Purpose" else "Keperluan"}: $purposeText".take(54), 38f, footerTop + 160f, subPaint)
            canvas.drawText("${if (isEn) "Remaining Balance" else "Sisa Saldo"}: $balanceText • $dateText".take(58), 38f, footerTop + 208f, subPaint)
            canvas.drawText("${if (isEn) "Recorded by" else "Dicatat oleh"}: ${tx.waliName} • ${if (isEn) "Photo stored 90 days" else "Foto tersimpan 3 bulan di DB"}", 38f, footerTop + 266f, notePaint)

            val outDir = File(context.cacheDir, PROOF_DIR).apply { if (!exists()) mkdirs() }
            val outFile = File(outDir, "wa_receipt_${tx.id}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { fos ->
                outBmp.compress(Bitmap.CompressFormat.JPEG, 86, fos)
            }
            outBmp.recycle()
            outFile
        } catch (_: Exception) {
            null
        }
    }

    fun buildWhatsAppCaption(
        tx: SavingTransaction,
        currentBalance: Long,
        schoolName: String,
        remotePhotoUrl: String? = null,
        isEn: Boolean = false
    ): String {
        val cleanSchool = schoolName.trim().ifBlank { "Wali Asuh Care" }
        val isExpense = tx.transactionType == SavingTransaction.TYPE_EXPENSE
        val dateStr = DateUtils.formatFullDate(tx.transactionDate)
        val amountStr = SavingRetentionHelper.formatRupiah(tx.amount)
        val balanceStr = SavingRetentionHelper.formatRupiah(currentBalance)

        return if (isEn) {
            buildString {
                appendLine(if (isExpense) "🧾 *SAVINGS EXPENSE PROOF*" else "💰 *SAVINGS DEPOSIT RECEIPT*")
                appendLine("*$cleanSchool*")
                appendLine("----------------------------------------")
                appendLine("👦 *Student:* ${tx.studentName}${if (tx.studentClass.isNotBlank()) " (${tx.studentClass})" else ""}")
                appendLine("📅 *Date:* $dateStr")
                appendLine("💵 *Amount:* ${if (isExpense) "-$amountStr" else "+$amountStr"}")
                appendLine("📝 *Purpose:* ${tx.purpose.ifBlank { "-" }}")
                if (tx.notes.isNotBlank()) appendLine("📌 *Notes:* ${tx.notes}")
                appendLine("💳 *Current Balance:* $balanceStr")
                appendLine("👤 *Wali Asuh:* ${tx.waliName}")
                if (!remotePhotoUrl.isNullOrBlank()) {
                    appendLine("🖼️ *Cloud Proof (valid 90 days):* $remotePhotoUrl")
                }
                appendLine("----------------------------------------")
                append("_Photo automatically saves to WhatsApp Gallery when received._")
            }.trim()
        } else {
            buildString {
                appendLine(if (isExpense) "🧾 *BUKTI PENGELUARAN TABUNGAN ANAK ASUH*" else "💰 *BUKTI TITIP MASUK TABUNGAN ANAK ASUH*")
                appendLine("*$cleanSchool*")
                appendLine("----------------------------------------")
                appendLine("👦 *Nama Anak:* ${tx.studentName}${if (tx.studentClass.isNotBlank()) " (${tx.studentClass})" else ""}")
                appendLine("📅 *Tanggal:* $dateStr")
                appendLine("💵 *Nominal:* ${if (isExpense) "-$amountStr" else "+$amountStr"}")
                appendLine("📝 *Keperluan:* ${tx.purpose.ifBlank { "-" }}")
                if (tx.notes.isNotBlank()) appendLine("📌 *Catatan:* ${tx.notes}")
                appendLine("💳 *Sisa Saldo:* $balanceStr")
                appendLine("👤 *Dicatat oleh:* ${tx.waliName}")
                if (!remotePhotoUrl.isNullOrBlank()) {
                    appendLine("🖼️ *Link Bukti (aktif 3 bulan):* $remotePhotoUrl")
                }
                appendLine("----------------------------------------")
                append("_Bukti foto terlampir otomatis tersimpan di galeri WhatsApp Orang Tua._")
            }.trim()
        }
    }

    fun normalizeIndonesianPhoneForWhatsApp(rawContact: String): String? {
        val digits = rawContact.filter { it.isDigit() }
        if (digits.length < 9) return null
        val normalized = when {
            digits.startsWith("62") -> digits
            digits.startsWith("0") -> "62" + digits.drop(1)
            digits.startsWith("8") -> "62$digits"
            else -> return null
        }
        return if (normalized.length in 10..15) normalized else null
    }

    suspend fun shareProofToWhatsApp(
        context: Context,
        tx: SavingTransaction,
        currentBalance: Long,
        schoolName: String,
        parentContactHint: String = "",
        isEn: Boolean = false
    ): Boolean {
        val remoteUrl = buildRemoteProofUrl(context, tx)
        val caption = buildWhatsAppCaption(tx, currentBalance, schoolName, remoteUrl, isEn)
        val receiptImage = buildShareableReceiptImage(context, tx, currentBalance, schoolName, isEn)

        val intent = if (receiptImage != null && receiptImage.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", receiptImage)
            Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, caption)
            }
        }

        val waPhone = normalizeIndonesianPhoneForWhatsApp(parentContactHint)
        val pm = context.packageManager
        val waPackage = listOf("com.whatsapp", "com.whatsapp.w4b").firstOrNull { pkg ->
            runCatching { pm.getPackageInfo(pkg, 0) }.isSuccess
        }

        return withContext(Dispatchers.Main) {
            try {
                if (waPackage != null) {
                    val directIntent = Intent(intent).apply {
                        setPackage(waPackage)
                        if (waPhone != null) {
                            putExtra("jid", "$waPhone@s.whatsapp.net")
                        }
                    }
                    runCatching {
                        context.startActivity(directIntent)
                        return@withContext true
                    }
                }
                context.startActivity(
                    Intent.createChooser(
                        intent,
                        if (isEn) "Send Expense Proof via WhatsApp" else "Kirim Bukti ke WhatsApp Orang Tua"
                    )
                )
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    suspend fun saveProofToDeviceGallery(
        context: Context,
        tx: SavingTransaction,
        currentBalance: Long,
        schoolName: String,
        isEn: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        val imageFile = buildShareableReceiptImage(context, tx, currentBalance, schoolName, isEn)
            ?: return@withContext false
        return@withContext try {
            val displayName = "Bukti_Tabungan_${tx.studentName.replace(" ", "_")}_${tx.id}.jpg"
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/WaliAsuhCare")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext false
            resolver.openOutputStream(uri)?.use { out ->
                imageFile.inputStream().use { input -> input.copyTo(out) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val clearPending = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
                resolver.update(uri, clearPending, null, null)
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
