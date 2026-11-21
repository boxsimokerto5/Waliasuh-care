package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import android.text.TextUtils
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.FastingRecord
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentExpectation
import com.example.data.model.ParentExpectationHelper
import com.example.data.model.SchoolProfile
import com.example.data.model.SholatHelper
import com.example.data.model.SholatRecord
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganTransaction
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

object ParentPortalPdfHelper {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    /**
     * PDF Rekap Riwayat Puasa Khusus Per Anak (untuk Portal Orang Tua).
     */
    fun generateStudentFastingReportPdf(
        context: Context,
        student: ChildStudent,
        records: List<FastingRecord>,
        periodLabel: String,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? {
        val document = PdfDocument()
        var currentPage: PdfDocument.Page? = null
        return try {
            renderStudentFastingReportPages(
                context = context,
                student = student,
                records = records,
                periodLabel = periodLabel,
                waliName = waliName,
                schoolProfile = schoolProfile,
                startNewCanvas = { num ->
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, num).create()
                    currentPage = document.startPage(pageInfo)
                    currentPage!!.canvas
                },
                finishCanvas = {
                    currentPage?.let {
                        document.finishPage(it)
                        currentPage = null
                    }
                }
            )

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanStudent = student.name.replace(Regex("[^a-zA-Z0-9]"), "_").take(25)
            val cleanPeriod = periodLabel.replace(Regex("[^a-zA-Z0-9]"), "_").take(20)
            val file = File(outputDir, "Rekap_Puasa_${cleanStudent}_$cleanPeriod.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            runCatching { currentPage?.let { document.finishPage(it) } }
            runCatching { document.close() }
        }
    }

    internal fun renderStudentFastingReportPages(
        context: Context,
        student: ChildStudent,
        records: List<FastingRecord>,
        periodLabel: String,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile(),
        startNewCanvas: (pageNumber: Int) -> Canvas,
        finishCanvas: () -> Unit = {}
    ) {
        val lang = AppLanguageManager.getLanguage(context)
        val isEn = lang == AppLanguage.ENGLISH
        val pageWidth = PAGE_WIDTH
        val pageHeight = PAGE_HEIGHT
        val marginX = 35f
        val contentRight = pageWidth - marginX

        var pageNumber = 1
        var canvas = startNewCanvas(pageNumber)
        val paint = Paint().apply { isAntiAlias = true }

        var currentY = 35f
        currentY = PdfReportGenerator.drawHeader(
            context = context,
            canvas = canvas,
            paint = paint,
            width = pageWidth,
            startY = currentY,
            schoolProfile = schoolProfile,
            unitTitle = PdfReportGenerator.UNIT_BIMBINGAN_SPIRITUAL
        )

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(
            if (isEn) "STUDENT FASTING DEVELOPMENT REPORT" else "LAPORAN RIWAYAT PUASA ANAK ASUH",
            pageWidth / 2f,
            currentY,
            paint
        )
        currentY += 13f
        paint.textSize = 9.5f
        paint.color = Color.rgb(15, 118, 110)
        canvas.drawText(
            (if (isEn) "PERIOD: " else "PERIODE: ") + periodLabel.uppercase(lang.locale),
            pageWidth / 2f,
            currentY,
            paint
        )
        paint.textAlign = Paint.Align.LEFT
        currentY += 14f

        currentY = PdfReportGenerator.drawStudentBio(canvas, paint, pageWidth, currentY, student, isEn)

        val sorted = records.sortedWith(compareByDescending<FastingRecord> { it.sessionDate }.thenByDescending { it.createdAt })
        val sahurCount = sorted.count { it.isSahurChecked }
        val bukaCount = sorted.count { it.isBukaChecked }
        val fullCount = sorted.count { it.isSahurChecked && it.isBukaChecked }
        val ratePct = if (sorted.isNotEmpty()) ((fullCount * 100f) / sorted.size).toInt() else 0

        val kpiRect = RectF(marginX, currentY, contentRight, currentY + 42f)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(240, 253, 250)
        canvas.drawRoundRect(kpiRect, 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(153, 246, 228)
        paint.strokeWidth = 1f
        canvas.drawRoundRect(kpiRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        val kpis = listOf(
            (if (isEn) "Total Sessions" else "Total Sesi") to "${sorted.size}",
            (if (isEn) "Sahur Checked" else "Sahur") to "$sahurCount",
            (if (isEn) "Iftar Checked" else "Buka Puasa") to "$bukaCount",
            (if (isEn) "Full Complete" else "Tuntas Penuh") to "$fullCount ($ratePct%)"
        )
        val colW = (contentRight - marginX) / kpis.size
        kpis.forEachIndexed { idx, (lbl, v) ->
            val cx = marginX + idx * colW + colW / 2f
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 7.5f
            paint.color = Color.rgb(17, 94, 89)
            canvas.drawText(lbl, cx, currentY + 15f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10.5f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(v, cx, currentY + 32f, paint)
        }
        paint.textAlign = Paint.Align.LEFT
        currentY += 54f

        val dateFmt = SimpleDateFormat("d MMM yyyy", lang.locale)
        val tWidths = floatArrayOf(26f, 82f, 165f, 68f, 68f, (contentRight - marginX) - 409f)
        val tHeaders = listOf(
            "No",
            if (isEn) "Date" else "Tanggal",
            if (isEn) "Fasting Session" else "Sesi / Jenis Puasa",
            "Sahur",
            if (isEn) "Iftar" else "Buka",
            if (isEn) "Notes" else "Pembina / Catatan"
        )

        fun drawTableHeader(yPos: Float): Float {
            val h = 21f
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(15, 118, 110)
            canvas.drawRect(marginX, yPos, contentRight, yPos + h, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8f
            paint.color = Color.WHITE
            var cx = marginX
            tHeaders.forEachIndexed { idx, title ->
                if (idx == 3 || idx == 4) {
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText(title, cx + tWidths[idx] / 2f, yPos + 14f, paint)
                } else {
                    paint.textAlign = Paint.Align.LEFT
                    canvas.drawText(title, cx + 5f, yPos + 14f, paint)
                }
                cx += tWidths[idx]
            }
            paint.textAlign = Paint.Align.LEFT
            return yPos + h
        }

        if (sorted.isEmpty()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 9.5f
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText(
                if (isEn) "No fasting attendance records for this period." else "Belum ada catatan riwayat puasa pada periode ini.",
                marginX + 6f,
                currentY + 12f,
                paint
            )
            currentY += 30f
        } else {
            currentY = drawTableHeader(currentY)
            val rowH = 20f
            sorted.forEachIndexed { idx, rec ->
                if (currentY + rowH > pageHeight - 130f) {
                    finishCanvas()
                    pageNumber++
                    canvas = startNewCanvas(pageNumber)
                    currentY = 38f
                    currentY = drawTableHeader(currentY)
                }

                paint.style = Paint.Style.FILL
                paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                canvas.drawRect(marginX, currentY, contentRight, currentY + rowH, paint)

                var cx = marginX
                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText("${idx + 1}.", cx + 5f, currentY + 13.5f, paint)
                cx += tWidths[0]

                val dStr = if (rec.sessionDate > 0L) dateFmt.format(Date(rec.sessionDate)) else "-"
                canvas.drawText(dStr, cx + 5f, currentY + 13.5f, paint)
                cx += tWidths[1]

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val titleClip = TextUtils.ellipsize(
                    rec.sessionTitle.ifBlank { if (isEn) "Fasting" else "Puasa" },
                    TextPaint(paint),
                    tWidths[2] - 8f,
                    TextUtils.TruncateAt.END
                ).toString()
                canvas.drawText(titleClip, cx + 5f, currentY + 13.5f, paint)
                cx += tWidths[2]

                paint.textAlign = Paint.Align.CENTER
                paint.color = if (rec.isSahurChecked) Color.rgb(21, 128, 61) else Color.rgb(148, 163, 184)
                canvas.drawText(
                    if (rec.isSahurChecked) (if (isEn) "Yes" else "Ya") else "-",
                    cx + tWidths[3] / 2f,
                    currentY + 13.5f,
                    paint
                )
                cx += tWidths[3]

                paint.color = if (rec.isBukaChecked) Color.rgb(21, 128, 61) else Color.rgb(148, 163, 184)
                canvas.drawText(
                    if (rec.isBukaChecked) (if (isEn) "Yes" else "Ya") else "-",
                    cx + tWidths[4] / 2f,
                    currentY + 13.5f,
                    paint
                )
                cx += tWidths[4]

                paint.textAlign = Paint.Align.LEFT
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(71, 85, 105)
                val noteSummary = listOfNotNull(
                    rec.waliName.takeIf { it.isNotBlank() },
                    rec.notes.takeIf { it.isNotBlank() }
                ).joinToString(" • ").ifBlank { "-" }
                val noteClip = TextUtils.ellipsize(
                    noteSummary,
                    TextPaint(paint),
                    tWidths[5] - 8f,
                    TextUtils.TruncateAt.END
                ).toString()
                canvas.drawText(noteClip, cx + 5f, currentY + 13.5f, paint)

                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(226, 232, 240)
                paint.strokeWidth = 0.5f
                canvas.drawRect(marginX, currentY, contentRight, currentY + rowH, paint)
                paint.style = Paint.Style.FILL
                currentY += rowH
            }
            currentY += 14f
        }

        if (currentY > pageHeight - 135f) {
            finishCanvas()
            pageNumber++
            canvas = startNewCanvas(pageNumber)
            currentY = 38f
        }
        PdfReportGenerator.drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
        finishCanvas()
    }

    /**
     * PDF Rekap Siklus Haid Khusus Per Siswi (untuk Portal Orang Tua).
     */
    fun generateStudentHaidReportPdf(
        context: Context,
        student: ChildStudent,
        records: List<HaidRecord>,
        periodLabel: String,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? {
        val document = PdfDocument()
        var currentPage: PdfDocument.Page? = null
        return try {
            renderStudentHaidReportPages(
                context = context,
                student = student,
                records = records,
                periodLabel = periodLabel,
                waliName = waliName,
                schoolProfile = schoolProfile,
                startNewCanvas = { num ->
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, num).create()
                    currentPage = document.startPage(pageInfo)
                    currentPage!!.canvas
                },
                finishCanvas = {
                    currentPage?.let {
                        document.finishPage(it)
                        currentPage = null
                    }
                }
            )

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanStudent = student.name.replace(Regex("[^a-zA-Z0-9]"), "_").take(25)
            val cleanPeriod = periodLabel.replace(Regex("[^a-zA-Z0-9]"), "_").take(20)
            val file = File(outputDir, "Rekap_Haid_${cleanStudent}_$cleanPeriod.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            runCatching { currentPage?.let { document.finishPage(it) } }
            runCatching { document.close() }
        }
    }

    internal fun renderStudentHaidReportPages(
        context: Context,
        student: ChildStudent,
        records: List<HaidRecord>,
        periodLabel: String,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile(),
        startNewCanvas: (pageNumber: Int) -> Canvas,
        finishCanvas: () -> Unit = {}
    ) {
        val lang = AppLanguageManager.getLanguage(context)
        val isEn = lang == AppLanguage.ENGLISH
        val pageWidth = PAGE_WIDTH
        val pageHeight = PAGE_HEIGHT
        val marginX = 35f
        val contentRight = pageWidth - marginX

        var pageNumber = 1
        var canvas = startNewCanvas(pageNumber)
        val paint = Paint().apply { isAntiAlias = true }

        var currentY = 35f
        currentY = PdfReportGenerator.drawHeader(
            context = context,
            canvas = canvas,
            paint = paint,
            width = pageWidth,
            startY = currentY,
            schoolProfile = schoolProfile,
            unitTitle = PdfReportGenerator.UNIT_LAYANAN_KESEHATAN
        )

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(
            if (isEn) "STUDENT MENSTRUAL CYCLE & CARE REPORT" else "LAPORAN PEMANTAUAN SIKLUS HAID SISWI",
            pageWidth / 2f,
            currentY,
            paint
        )
        currentY += 13f
        paint.textSize = 9.5f
        paint.color = Color.rgb(190, 24, 93)
        canvas.drawText(
            (if (isEn) "PERIOD: " else "PERIODE: ") + periodLabel.uppercase(lang.locale),
            pageWidth / 2f,
            currentY,
            paint
        )
        paint.textAlign = Paint.Align.LEFT
        currentY += 14f

        currentY = PdfReportGenerator.drawStudentBio(canvas, paint, pageWidth, currentY, student, isEn)

        val sorted = records.sortedByDescending { it.startDate }
        val latest = sorted.firstOrNull()
        val avgDuration = if (sorted.isNotEmpty()) {
            (sorted.sumOf { it.durationDays }.toFloat() / sorted.size).toInt()
        } else 0
        val istihadhahCount = sorted.count { it.isIstihadhahWarning || it.isAutoCompletedByLimit }
        val statusStr = when {
            latest == null -> if (isEn) "No Logs" else "Belum Ada Catatan"
            latest.isActive -> if (isEn) "Active (Day ${latest.durationDays})" else "Haid Hari ke-${latest.durationDays}"
            else -> if (isEn) "Suci (Day ${latest.suciDayCount})" else "Masa Suci Hari ke-${latest.suciDayCount}"
        }

        val kpiRect = RectF(marginX, currentY, contentRight, currentY + 42f)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(253, 242, 248)
        canvas.drawRoundRect(kpiRect, 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(249, 168, 212)
        paint.strokeWidth = 1f
        canvas.drawRoundRect(kpiRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        val kpis = listOf(
            (if (isEn) "Current Status" else "Status Terkini") to statusStr,
            (if (isEn) "Recorded Cycles" else "Total Siklus") to "${sorted.size}",
            (if (isEn) "Avg Duration" else "Rata-rata Durasi") to if (avgDuration > 0) "$avgDuration Hr" else "-",
            (if (isEn) "15-Day Flag" else "Batas 15 Hari") to "$istihadhahCount"
        )
        val colW = (contentRight - marginX) / kpis.size
        kpis.forEachIndexed { idx, (lbl, v) ->
            val cx = marginX + idx * colW + colW / 2f
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 7.4f
            paint.color = Color.rgb(157, 23, 77)
            canvas.drawText(lbl, cx, currentY + 15f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.5f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(
                TextUtils.ellipsize(v, TextPaint(paint), colW - 8f, TextUtils.TruncateAt.END).toString(),
                cx,
                currentY + 32f,
                paint
            )
        }
        paint.textAlign = Paint.Align.LEFT
        currentY += 54f

        val dateFmt = SimpleDateFormat("d MMM yyyy", lang.locale)
        val tWidths = floatArrayOf(26f, 84f, 84f, 52f, 92f, (contentRight - marginX) - 338f)
        val tHeaders = listOf(
            "No",
            if (isEn) "Start Date" else "Tgl Mulai",
            if (isEn) "Suci Date" else "Tgl Suci",
            if (isEn) "Days" else "Durasi",
            "Status",
            if (isEn) "Symptoms & Notes" else "Keluhan & Catatan"
        )

        fun drawTableHeader(yPos: Float): Float {
            val h = 21f
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(190, 24, 93)
            canvas.drawRect(marginX, yPos, contentRight, yPos + h, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8f
            paint.color = Color.WHITE
            var cx = marginX
            tHeaders.forEachIndexed { idx, title ->
                if (idx == 3) {
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText(title, cx + tWidths[idx] / 2f, yPos + 14f, paint)
                } else {
                    paint.textAlign = Paint.Align.LEFT
                    canvas.drawText(title, cx + 5f, yPos + 14f, paint)
                }
                cx += tWidths[idx]
            }
            paint.textAlign = Paint.Align.LEFT
            return yPos + h
        }

        if (sorted.isEmpty()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 9.5f
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText(
                if (isEn) "No haid cycle records found." else "Belum ada catatan siklus haid untuk siswi ini.",
                marginX + 6f,
                currentY + 12f,
                paint
            )
            currentY += 30f
        } else {
            currentY = drawTableHeader(currentY)
            val rowH = 22f
            sorted.forEachIndexed { idx, rec ->
                if (currentY + rowH > pageHeight - 130f) {
                    finishCanvas()
                    pageNumber++
                    canvas = startNewCanvas(pageNumber)
                    currentY = 38f
                    currentY = drawTableHeader(currentY)
                }

                paint.style = Paint.Style.FILL
                paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(252, 249, 251)
                canvas.drawRect(marginX, currentY, contentRight, currentY + rowH, paint)

                var cx = marginX
                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText("${idx + 1}.", cx + 5f, currentY + 14f, paint)
                cx += tWidths[0]

                canvas.drawText(dateFmt.format(Date(rec.startDate)), cx + 5f, currentY + 14f, paint)
                cx += tWidths[1]

                val endStr = rec.effectiveEndDate?.let { dateFmt.format(Date(it)) } ?: "-"
                canvas.drawText(endStr, cx + 5f, currentY + 14f, paint)
                cx += tWidths[2]

                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("${rec.durationDays} ${if (isEn) "d" else "Hr"}", cx + tWidths[3] / 2f, currentY + 14f, paint)
                cx += tWidths[3]

                paint.textAlign = Paint.Align.LEFT
                val stText = when {
                    rec.isActive -> if (isEn) "Active Haid" else "Sedang Haid"
                    rec.isAutoCompletedByLimit -> if (isEn) "Auto Suci (15d)" else "Suci Otomatis (15h)"
                    else -> if (isEn) "Completed (Suci)" else "Selesai (Suci)"
                }
                paint.color = if (rec.isActive) Color.rgb(190, 24, 93) else Color.rgb(21, 128, 61)
                canvas.drawText(stText, cx + 5f, currentY + 14f, paint)
                cx += tWidths[4]

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(71, 85, 105)
                val symNotes = listOfNotNull(
                    rec.symptoms.takeIf { it.isNotBlank() && it != "-" },
                    rec.notes.takeIf { it.isNotBlank() }
                ).joinToString(" • ").ifBlank { "-" }
                val symClip = TextUtils.ellipsize(
                    symNotes,
                    TextPaint(paint),
                    tWidths[5] - 8f,
                    TextUtils.TruncateAt.END
                ).toString()
                canvas.drawText(symClip, cx + 5f, currentY + 14f, paint)

                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(226, 232, 240)
                paint.strokeWidth = 0.5f
                canvas.drawRect(marginX, currentY, contentRight, currentY + rowH, paint)
                paint.style = Paint.Style.FILL
                currentY += rowH
            }
            currentY += 14f
        }

        if (currentY > pageHeight - 135f) {
            finishCanvas()
            pageNumber++
            canvas = startNewCanvas(pageNumber)
            currentY = 38f
        }
        PdfReportGenerator.drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
        finishCanvas()
    }

    /**
     * PDF Catatan Harapan Karakter & Doa Orang Tua (untuk Portal Orang Tua).
     */
    fun generateStudentHarapanReportPdf(
        context: Context,
        student: ChildStudent,
        expectations: List<ParentExpectation>,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? {
        val document = PdfDocument()
        var currentPage: PdfDocument.Page? = null
        return try {
            renderStudentHarapanReportPages(
                context = context,
                student = student,
                expectations = expectations,
                waliName = waliName,
                schoolProfile = schoolProfile,
                startNewCanvas = { num ->
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, num).create()
                    currentPage = document.startPage(pageInfo)
                    currentPage!!.canvas
                },
                finishCanvas = {
                    currentPage?.let {
                        document.finishPage(it)
                        currentPage = null
                    }
                }
            )

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanStudent = student.name.replace(Regex("[^a-zA-Z0-9]"), "_").take(25)
            val file = File(outputDir, "Harapan_Orang_Tua_$cleanStudent.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            runCatching { currentPage?.let { document.finishPage(it) } }
            runCatching { document.close() }
        }
    }

    internal fun renderStudentHarapanReportPages(
        context: Context,
        student: ChildStudent,
        expectations: List<ParentExpectation>,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile(),
        startNewCanvas: (pageNumber: Int) -> Canvas,
        finishCanvas: () -> Unit = {}
    ) {
        val lang = AppLanguageManager.getLanguage(context)
        val isEn = lang == AppLanguage.ENGLISH
        val pageWidth = PAGE_WIDTH
        val pageHeight = PAGE_HEIGHT
        val marginX = 35f
        val contentRight = pageWidth - marginX
        val boxW = contentRight - marginX

        var pageNumber = 1
        var canvas = startNewCanvas(pageNumber)
        val paint = Paint().apply { isAntiAlias = true }

        var currentY = 35f
        currentY = PdfReportGenerator.drawHeader(
            context = context,
            canvas = canvas,
            paint = paint,
            width = pageWidth,
            startY = currentY,
            schoolProfile = schoolProfile,
            unitTitle = PdfReportGenerator.UNIT_BIMBINGAN_SPIRITUAL
        )

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(
            if (isEn) "PARENT CHARACTER EXPECTATIONS & PRAYERS" else "CATATAN HARAPAN KARAKTER & DOA ORANG TUA",
            pageWidth / 2f,
            currentY,
            paint
        )
        currentY += 16f
        paint.textAlign = Paint.Align.LEFT

        currentY = PdfReportGenerator.drawStudentBio(canvas, paint, pageWidth, currentY, student, isEn)

        val sorted = expectations.sortedByDescending { it.updatedAt }
        val dateFmt = SimpleDateFormat("d MMM yyyy HH:mm", lang.locale)

        if (sorted.isEmpty()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 9.5f
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText(
                if (isEn) "No parent expectations recorded yet." else "Belum ada catatan harapan orang tua tersimpan.",
                marginX + 6f,
                currentY + 12f,
                paint
            )
            currentY += 32f
        } else {
            sorted.forEachIndexed { idx, item ->
                val focusLabels = ParentExpectationHelper.parseFocusList(item.characterFocusCsv)
                    .joinToString(", ") { ParentExpectationHelper.displayFocusLabel(it, isEn) }
                val lines = buildList {
                    if (focusLabels.isNotBlank()) {
                        add((if (isEn) "Focus: " else "Fokus: ") + focusLabels)
                    }
                    if (item.characterExpectation.isNotBlank()) {
                        add((if (isEn) "Expectation: " else "Harapan Karakter: ") + item.characterExpectation)
                    }
                    if (item.adviceFocusNotes.isNotBlank()) {
                        add((if (isEn) "Advice Focus: " else "Poin Nasehat: ") + item.adviceFocusNotes)
                    }
                    if (item.parentPrayerMessage.isNotBlank()) {
                        add((if (isEn) "Prayer / Message: " else "Doa & Pesan: ") + item.parentPrayerMessage)
                    }
                    if (item.waliFollowUpNote.isNotBlank()) {
                        add((if (isEn) "Wali Asuh Response: " else "Tindak Lanjut Wali Asuh: ") + item.waliFollowUpNote)
                    }
                }
                val cardHeight = 28f + lines.size * 15f + 8f
                if (currentY + cardHeight > pageHeight - 130f) {
                    finishCanvas()
                    pageNumber++
                    canvas = startNewCanvas(pageNumber)
                    currentY = 38f
                }

                val cardRect = RectF(marginX, currentY, contentRight, currentY + cardHeight)
                paint.style = Paint.Style.FILL
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRoundRect(cardRect, 6f, 6f, paint)
                paint.style = Paint.Style.STROKE
                paint.color = if (item.waliFollowUpNote.isNotBlank()) Color.rgb(16, 185, 129) else Color.rgb(203, 213, 225)
                paint.strokeWidth = 0.9f
                canvas.drawRoundRect(cardRect, 6f, 6f, paint)
                paint.style = Paint.Style.FILL

                var ly = currentY + 15f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 9f
                paint.color = Color.rgb(4, 120, 87)
                canvas.drawText(
                    "#${idx + 1} • ${dateFmt.format(Date(item.updatedAt))}",
                    marginX + 10f,
                    ly,
                    paint
                )
                ly += 14f

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8.5f
                paint.color = Color.rgb(15, 23, 42)
                lines.forEach { line ->
                    val clipped = TextUtils.ellipsize(
                        line.replace("\n", " "),
                        TextPaint(paint),
                        boxW - 20f,
                        TextUtils.TruncateAt.END
                    ).toString()
                    canvas.drawText(clipped, marginX + 10f, ly, paint)
                    ly += 15f
                }
                currentY += cardHeight + 8f
            }
        }

        if (currentY > pageHeight - 135f) {
            finishCanvas()
            pageNumber++
            canvas = startNewCanvas(pageNumber)
            currentY = 38f
        }
        PdfReportGenerator.drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
        finishCanvas()
    }

    /**
     * Rapor Terpadu Lengkap Ananda (1 PDF Ringkas mencakup Profil Kesehatan Dasar,
     * Pembinaan Ibadah/Mengaji/Kristen, Puasa, Haid (khusus siswi), Riwayat Berobat,
     * Saldo Tabungan Titipan, dan Harapan Orang Tua).
     */
    fun generateParentComprehensiveReportPdf(
        context: Context,
        student: ChildStudent,
        schoolProfile: SchoolProfile,
        waliName: String,
        isChristianStudent: Boolean,
        isFemaleStudent: Boolean,
        sholatRecords: List<SholatRecord>,
        mengajiRecords: List<MengajiRecord>,
        christianRecords: List<ChristianWorshipRecord>,
        fastingRecords: List<FastingRecord>,
        haidRecords: List<HaidRecord>,
        medicalRecords: List<MedicalRecord>,
        tabunganTransactions: List<TabunganTransaction>,
        expectations: List<ParentExpectation>
    ): File? {
        val document = PdfDocument()
        var currentPage: PdfDocument.Page? = null
        return try {
            renderParentComprehensiveReportPages(
                context = context,
                student = student,
                schoolProfile = schoolProfile,
                waliName = waliName,
                isChristianStudent = isChristianStudent,
                isFemaleStudent = isFemaleStudent,
                sholatRecords = sholatRecords,
                mengajiRecords = mengajiRecords,
                christianRecords = christianRecords,
                fastingRecords = fastingRecords,
                haidRecords = haidRecords,
                medicalRecords = medicalRecords,
                tabunganTransactions = tabunganTransactions,
                expectations = expectations,
                startNewCanvas = { num ->
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, num).create()
                    currentPage = document.startPage(pageInfo)
                    currentPage!!.canvas
                },
                finishCanvas = {
                    currentPage?.let {
                        document.finishPage(it)
                        currentPage = null
                    }
                }
            )

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanStudent = student.name.replace(Regex("[^a-zA-Z0-9]"), "_").take(25)
            val file = File(outputDir, "Rapor_Terpadu_${cleanStudent}.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            runCatching { currentPage?.let { document.finishPage(it) } }
            runCatching { document.close() }
        }
    }

    internal fun renderParentComprehensiveReportPages(
        context: Context,
        student: ChildStudent,
        schoolProfile: SchoolProfile,
        waliName: String,
        isChristianStudent: Boolean,
        isFemaleStudent: Boolean,
        sholatRecords: List<SholatRecord>,
        mengajiRecords: List<MengajiRecord>,
        christianRecords: List<ChristianWorshipRecord>,
        fastingRecords: List<FastingRecord>,
        haidRecords: List<HaidRecord>,
        medicalRecords: List<MedicalRecord>,
        tabunganTransactions: List<TabunganTransaction>,
        expectations: List<ParentExpectation>,
        startNewCanvas: (pageNumber: Int) -> Canvas,
        finishCanvas: () -> Unit = {}
    ) {
        val lang = AppLanguageManager.getLanguage(context)
        val isEn = lang == AppLanguage.ENGLISH
        val pageWidth = PAGE_WIDTH
        val pageHeight = PAGE_HEIGHT
        val marginX = 35f
        val contentRight = pageWidth - marginX
        val boxW = contentRight - marginX

        var pageNumber = 1
        var canvas = startNewCanvas(pageNumber)
        val paint = Paint().apply { isAntiAlias = true }

        val dateFmt = SimpleDateFormat("d MMM yyyy", lang.locale)
        val monthKey = SholatHelper.todayDateKey().substring(0, 7)

        var currentY = 35f
        currentY = PdfReportGenerator.drawHeader(
            context = context,
            canvas = canvas,
            paint = paint,
            width = pageWidth,
            startY = currentY,
            schoolProfile = schoolProfile,
            unitTitle = if (isEn) "STUDENT INTEGRATED DEVELOPMENT REPORT" else "RAPOR TERPADU PERKEMBANGAN ANANDA"
        )

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(
            if (isEn) "PARENT PORTAL COMPREHENSIVE SUMMARY" else "RINGKASAN LENGKAP PORTAL ORANG TUA",
            pageWidth / 2f,
            currentY,
            paint
        )
        currentY += 12f
        paint.textSize = 9f
        paint.color = Color.rgb(4, 120, 87)
        canvas.drawText(
            (if (isEn) "PRINTED: " else "DICETAK: ") + dateFmt.format(Date()).uppercase(lang.locale),
            pageWidth / 2f,
            currentY,
            paint
        )
        paint.textAlign = Paint.Align.LEFT
        currentY += 14f

        currentY = PdfReportGenerator.drawStudentBio(canvas, paint, pageWidth, currentY, student, isEn)

        fun drawSectionBlock(
            title: String,
            accentColor: Int,
            bgColor: Int,
            borderColor: Int,
            lines: List<String>
        ) {
            val blockHeight = 24f + lines.size * 14f + 6f
            if (currentY + blockHeight > pageHeight - 125f) {
                finishCanvas()
                pageNumber++
                canvas = startNewCanvas(pageNumber)
                currentY = 38f
            }

            val rect = RectF(marginX, currentY, contentRight, currentY + blockHeight)
            paint.style = Paint.Style.FILL
            paint.color = bgColor
            canvas.drawRoundRect(rect, 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = borderColor
            paint.strokeWidth = 0.9f
            canvas.drawRoundRect(rect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.5f
            paint.color = accentColor
            canvas.drawText(title, marginX + 10f, currentY + 15f, paint)

            var ly = currentY + 30f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.4f
            paint.color = Color.rgb(30, 41, 59)
            lines.forEach { rawLine ->
                val clipped = TextUtils.ellipsize(
                    rawLine.replace("\n", " "),
                    TextPaint(paint),
                    boxW - 20f,
                    TextUtils.TruncateAt.END
                ).toString()
                canvas.drawText(clipped, marginX + 10f, ly, paint)
                ly += 14f
            }
            currentY += blockHeight + 8f
        }

        // 1. Profil Kesehatan Dasar & Pembina
        drawSectionBlock(
            title = if (isEn) "1. HEALTH PROFILE & CAREGIVER" else "1. PROFIL KESEHATAN DASAR & PEMBINA",
            accentColor = Color.rgb(67, 56, 202),
            bgColor = Color.rgb(238, 242, 255),
            borderColor = Color.rgb(199, 210, 254),
            lines = listOf(
                (if (isEn) "Blood Type: " else "Golongan Darah: ") + student.bloodType.ifBlank { "-" } +
                    "   |   " + (if (isEn) "Allergies: " else "Riwayat Alergi: ") + student.allergies.ifBlank { if (isEn) "None" else "Tidak ada" },
                (if (isEn) "Caregiver Team: " else "Tim Wali Asuh: ") + waliName.ifBlank { "Wali Asuh" }
            )
        )

        // 2. Pembinaan Ibadah (Sesuai Agama)
        if (isChristianStudent) {
            val pagi = christianRecords.count { ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_PAGI }
            val malam = christianRecords.count { ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_MALAM }
            val gereja = christianRecords.count { ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_KE_GEREJA }
            val latestChristian = christianRecords.maxByOrNull { it.activityDate }
            drawSectionBlock(
                title = if (isEn) "2. CHRISTIAN WORSHIP & NURTURE" else "2. PEMBINAAN IBADAH AGAMA KRISTEN",
                accentColor = Color.rgb(30, 58, 138),
                bgColor = Color.rgb(239, 246, 255),
                borderColor = Color.rgb(191, 219, 254),
                lines = listOf(
                    if (isEn) "Total Records: ${christianRecords.size} (Morning $pagi • Evening $malam • Church $gereja)"
                    else "Total Catatan: ${christianRecords.size} (Doa Pagi $pagi • Doa Malam $malam • Gereja $gereja)",
                    if (latestChristian != null) {
                        (if (isEn) "Latest Activity: " else "Kegiatan Terakhir: ") +
                            "${dateFmt.format(Date(latestChristian.activityDate))} — ${latestChristian.title.ifBlank { latestChristian.activityType }}"
                    } else {
                        if (isEn) "Latest Activity: No worship logs recorded yet." else "Kegiatan Terakhir: Belum ada catatan ibadah."
                    }
                )
            )
        } else {
            val sholatStats = SholatHelper.computeStudentAnalytics(listOf(student), sholatRecords, monthKey).first()
            val sortedMengaji = mengajiRecords.sortedByDescending { it.sessionDate }
            val latestMengaji = sortedMengaji.firstOrNull()
            val fluentMengaji = mengajiRecords.count { MengajiHelper.isFluent(it.fluencyStatus) }
            drawSectionBlock(
                title = if (isEn) "2. 5 DAILY PRAYERS & MENGAJI" else "2. PEMBINAAN SHOLAT 5 WAKTU & MENGAJI",
                accentColor = Color.rgb(15, 118, 110),
                bgColor = Color.rgb(240, 253, 250),
                borderColor = Color.rgb(153, 246, 228),
                lines = listOf(
                    if (isEn) "Prayer This Month ($monthKey): Jamaah ${sholatStats.jamaahRatePercent}% (${sholatStats.jamaahCount}x Jamaah, ${sholatStats.munfaridCount}x Munfarid, ${sholatStats.alfaCount}x Alfa)"
                    else "Sholat Bulan Ini ($monthKey): Jamaah ${sholatStats.jamaahRatePercent}% (${sholatStats.jamaahCount}x Jamaah, ${sholatStats.munfaridCount}x Munfarid, ${sholatStats.alfaCount}x Alfa)",
                    if (latestMengaji != null) {
                        (if (isEn) "Mengaji (${mengajiRecords.size}x, $fluentMengaji fluent): Last " else "Mengaji (${mengajiRecords.size}x, $fluentMengaji lancar): Terakhir ") +
                            "${MengajiHelper.formatCompactPosition(latestMengaji)} (${dateFmt.format(Date(latestMengaji.sessionDate))})"
                    } else {
                        if (isEn) "Mengaji: No mengaji sessions recorded yet." else "Mengaji: Belum ada catatan sesi mengaji."
                    }
                )
            )
        }

        // 3. Puasa & (Khusus Siswi) Haid
        val sahurCount = fastingRecords.count { it.isSahurChecked }
        val bukaCount = fastingRecords.count { it.isBukaChecked }
        val latestFast = fastingRecords.maxByOrNull { it.sessionDate }
        val fastingLine = if (fastingRecords.isNotEmpty()) {
            if (isEn) {
                "Fasting: ${fastingRecords.size} sessions (Sahur $sahurCount • Iftar $bukaCount) • Latest: ${latestFast?.sessionTitle.orEmpty()}"
            } else {
                "Puasa: ${fastingRecords.size} sesi (Sahur $sahurCount • Buka $bukaCount) • Terakhir: ${latestFast?.sessionTitle.orEmpty()}"
            }
        } else {
            if (isEn) "Fasting: No fasting records yet." else "Puasa: Belum ada catatan puasa."
        }
        val worshipHealthLines = buildList {
            add(fastingLine)
            if (isFemaleStudent) {
                val latestHaid = haidRecords.maxByOrNull { it.startDate }
                add(
                    when {
                        latestHaid == null -> if (isEn) "Haid Monitoring: No cycle logs recorded yet." else "Pemantauan Haid: Belum ada catatan siklus haid."
                        latestHaid.isActive -> if (isEn) "Haid Monitoring: Active cycle (Day ${latestHaid.durationDays}) • Total ${haidRecords.size} cycles"
                        else "Pemantauan Haid: Sedang Haid (Hari ke-${latestHaid.durationDays}) • Total ${haidRecords.size} siklus"
                        else -> if (isEn) "Haid Monitoring: Suci Period (Day ${latestHaid.suciDayCount}) • Total ${haidRecords.size} cycles"
                        else "Pemantauan Haid: Masa Suci (Hari ke-${latestHaid.suciDayCount}) • Total ${haidRecords.size} siklus"
                    }
                )
            }
        }
        drawSectionBlock(
            title = if (isFemaleStudent) {
                if (isEn) "3. FASTING & HAID CYCLE MONITORING" else "3. IBADAH PUASA & PEMANTAUAN SIKLUS HAID"
            } else {
                if (isEn) "3. FASTING DEVELOPMENT" else "3. PEMBINAAN IBADAH PUASA"
            },
            accentColor = if (isFemaleStudent) Color.rgb(190, 24, 93) else Color.rgb(15, 118, 110),
            bgColor = if (isFemaleStudent) Color.rgb(253, 242, 248) else Color.rgb(236, 253, 245),
            borderColor = if (isFemaleStudent) Color.rgb(249, 168, 212) else Color.rgb(167, 243, 208),
            lines = worshipHealthLines
        )

        // 4. Riwayat Kesehatan / Berobat
        val sortedMed = medicalRecords.sortedByDescending { it.planDate }
        val latestMed = sortedMed.firstOrNull()
        drawSectionBlock(
            title = if (isEn) "4. HEALTH & MEDICAL VISITS" else "4. KESEHATAN & RIWAYAT BEROBAT",
            accentColor = Color.rgb(2, 132, 199),
            bgColor = Color.rgb(240, 249, 255),
            borderColor = Color.rgb(186, 230, 253),
            lines = listOf(
                if (sortedMed.isEmpty()) {
                    if (isEn) "Total Visits: 0 (Alhamdulillah, no illness records)"
                    else "Total Kunjungan: 0 (Alhamdulillah, belum ada riwayat sakit)"
                } else {
                    if (isEn) "Total Medical Visits: ${sortedMed.size}x" else "Total Riwayat Berobat: ${sortedMed.size} kali"
                },
                if (latestMed != null) {
                    (if (isEn) "Latest Visit: " else "Kunjungan Terakhir: ") +
                        "${dateFmt.format(Date(latestMed.planDate))} • ${latestMed.complaint.ifBlank { "-" }} (${latestMed.faskes})"
                } else {
                    if (isEn) "Current Condition: Healthy & Monitored" else "Kondisi Saat Ini: Sehat & Terpantau"
                }
            )
        )

        // 5. Tabungan Titipan & Harapan Orang Tua
        val tabTotals = TabunganHelper.computeAggregateTotals(tabunganTransactions)
        val latestExpectation = expectations.maxByOrNull { it.updatedAt }
        drawSectionBlock(
            title = if (isEn) "5. ALLOWANCE SAVINGS & CHARACTER HOPES" else "5. TABUNGAN TITIPAN & HARAPAN KARAKTER",
            accentColor = Color.rgb(180, 83, 9),
            bgColor = Color.rgb(255, 251, 235),
            borderColor = Color.rgb(253, 230, 138),
            lines = listOf(
                if (isEn) "Savings Balance: ${TabunganHelper.formatRupiah(tabTotals.totalBalance)} (In ${TabunganHelper.formatRupiah(tabTotals.totalIn)} • Out ${TabunganHelper.formatRupiah(tabTotals.totalOut)})"
                else "Saldo Tabungan: ${TabunganHelper.formatRupiah(tabTotals.totalBalance)} (Masuk ${TabunganHelper.formatRupiah(tabTotals.totalIn)} • Keluar ${TabunganHelper.formatRupiah(tabTotals.totalOut)})",
                if (latestExpectation != null) {
                    val base = (if (isEn) "Latest Hope (${expectations.size} total): " else "Harapan Terakhir (${expectations.size} catatan): ") +
                        latestExpectation.characterExpectation
                    if (latestExpectation.waliFollowUpNote.isNotBlank()) {
                        "$base • Follow-up: ${latestExpectation.waliFollowUpNote}"
                    } else base
                } else {
                    if (isEn) "Character Hopes: Not submitted yet." else "Harapan Karakter: Belum ada catatan dikirim."
                }
            )
        )

        if (currentY > pageHeight - 135f) {
            finishCanvas()
            pageNumber++
            canvas = startNewCanvas(pageNumber)
            currentY = 38f
        }
        PdfReportGenerator.drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
        finishCanvas()
    }
}
