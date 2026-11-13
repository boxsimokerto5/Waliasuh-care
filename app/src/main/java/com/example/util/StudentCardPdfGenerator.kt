package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.ChildStudent
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

object StudentCardPdfGenerator {

    private const val A4_WIDTH_PT = 595
    private const val A4_HEIGHT_PT = 842
    private const val CARDS_PER_PAGE = 8
    private const val COLS = 2
    private const val ROWS = 4

    fun generateCardsPdf(
        context: Context,
        students: List<ChildStudent>,
        schoolName: String,
        filterLabel: String
    ): File? {
        if (students.isEmpty()) return null
        val document = PdfDocument()
        var logoBitmap: android.graphics.Bitmap? = null
        return try {
            val cleanSchool = schoolName.trim().ifBlank { "Wali Asuh Care School" }
            val totalPages = ceil(students.size.toDouble() / CARDS_PER_PAGE).toInt().coerceAtLeast(1)
            val datePrint = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())

            logoBitmap = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.logo_sr_care)
            } catch (_: Exception) {
                null
            }

            // Ukuran standar ID Card CR80 dalam A4 points (85.6mm x 54mm ≈ 243pt x 153pt)
            val cardW = 244f
            val cardH = 154f
            val gapX = 24f
            val gapY = 20f
            val gridW = COLS * cardW + (COLS - 1) * gapX
            val startX = (A4_WIDTH_PT - gridW) / 2f
            val startY = 74f

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F172A")
                textSize = 11.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#475569")
                textSize = 8.5f
            }
            val pageCountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1D4ED8")
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            val cutGuidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#94A3B8")
                style = Paint.Style.STROKE
                strokeWidth = 0.6f
                pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
            }
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#64748B")
                textSize = 7.5f
                textAlign = Paint.Align.CENTER
            }

            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH_PT, A4_HEIGHT_PT, pageIndex + 1).create()
                val page = document.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                canvas.drawColor(Color.WHITE)

                // Header Halaman A4
                canvas.drawText("KARTU ANAK ASUH — ${cleanSchool.uppercase()}", startX, 34f, titlePaint)
                canvas.drawText(
                    "Kategori: $filterLabel • Total: ${students.size} Siswa • Dicetak: $datePrint",
                    startX,
                    48f,
                    subTitlePaint
                )
                canvas.drawText(
                    "Hal. ${pageIndex + 1} / $totalPages",
                    A4_WIDTH_PT - startX,
                    36f,
                    pageCountPaint
                )
                canvas.drawLine(startX, 56f, A4_WIDTH_PT - startX, 56f, cutGuidePaint)

                val pageStart = pageIndex * CARDS_PER_PAGE
                val pageEnd = minOf(pageStart + CARDS_PER_PAGE, students.size)

                for (slot in pageStart until pageEnd) {
                    val localIdx = slot - pageStart
                    val row = localIdx / COLS
                    val col = localIdx % COLS

                    val cardLeft = startX + col * (cardW + gapX)
                    val cardTop = startY + row * (cardH + gapY)

                    val student = students[slot]
                    val qrPayload = StudentCardRenderer.resolveQrPayload(student)
                    val qrBitmap = StudentCardRenderer.generateQrBitmap(qrPayload, sizePx = 400)

                    // Garis bantu potong (cut marks) tipis di sekeliling slot kartu
                    canvas.drawRect(
                        cardLeft - 3f,
                        cardTop - 3f,
                        cardLeft + cardW + 3f,
                        cardTop + cardH + 3f,
                        cutGuidePaint
                    )

                    StudentCardRenderer.drawStudentCardOnCanvas(
                        canvas = canvas,
                        left = cardLeft,
                        top = cardTop,
                        width = cardW,
                        height = cardH,
                        student = student,
                        schoolName = cleanSchool,
                        qrBitmap = qrBitmap,
                        logoBitmap = logoBitmap
                    )

                    qrBitmap?.recycle()
                }

                // Footer Legend
                canvas.drawText(
                    "Warna Kartu: SMA (Abu-abu) • SMP (Biru) • SD (Merah)  |  Garis Pemanis: Putra (Hijau Menyala) • Putri (Pink Menyala)",
                    A4_WIDTH_PT / 2f,
                    A4_HEIGHT_PT - 22f,
                    footerPaint
                )

                document.finishPage(page)
            }

            logoBitmap?.recycle()

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val safeFilter = filterLabel.replace(Regex("[^a-zA-Z0-9]+"), "_").trim('_')
            val file = File(outputDir, "Kartu_Anak_Asuh_${safeFilter}.pdf")

            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            logoBitmap?.recycle()
            try {
                document.close()
            } catch (_: Exception) {
            }
        }
    }

    fun printCardsWithSystemPrintManager(
        context: Context,
        pdfFile: File,
        jobName: String
    ) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                openCardsPdf(context, pdfFile)
                return
            }

            val adapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder(pdfFile.name)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onWriteCancelled()
                        return
                    }
                    try {
                        val destFd = destination ?: run {
                            callback?.onWriteFailed("Tujuan cetak tidak tersedia")
                            return
                        }
                        FileInputStream(pdfFile).use { input ->
                            FileOutputStream(destFd.fileDescriptor).use { output ->
                                input.copyTo(output)
                            }
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message ?: "Gagal menulis dokumen cetak")
                    }
                }
            }

            val attributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build()

            printManager.print(jobName, adapter, attributes)
        } catch (e: Exception) {
            Toast.makeText(context, "Membuka PDF untuk cetak: ${e.message}", Toast.LENGTH_SHORT).show()
            openCardsPdf(context, pdfFile)
        }
    }

    fun openCardsPdf(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Buka PDF Kartu Anak Asuh"))
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak ditemukan aplikasi pembuka PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareCardsPdf(
        context: Context,
        pdfFile: File,
        filterLabel: String,
        schoolName: String
    ) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
            val cleanSchool = schoolName.ifBlank { "Wali Asuh Care" }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Kartu Anak Asuh ($filterLabel) - $cleanSchool")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Dokumen PDF Kartu Anak Asuh ($filterLabel) dengan QR NIK dari $cleanSchool. Siap cetak A4 & gunting."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan Kartu Anak Asuh PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
