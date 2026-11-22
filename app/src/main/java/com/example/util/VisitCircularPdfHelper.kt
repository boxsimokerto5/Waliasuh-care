package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.ChildStudent
import com.example.data.model.SchoolProfile
import com.example.data.model.VisitHelper
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionSwap
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VisitCircularPdfConfig(
    val monthYear: String = "ALL",
    val circularNumber: String = "",
    val openingParagraph: String = "",
    val guidelinesText: String = "",
    val pageBreakPerSession: Boolean = true,
    val includeUnscheduledAppendix: Boolean = true,
    val signerName: String = ""
)

object VisitCircularPdfHelper {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_X = 35f

    fun defaultOpeningParagraph(periodLabel: String, isEn: Boolean): String {
        return if (isEn) {
            "Peace and blessings be upon you. Through this official circular, we hereby inform all Parents/Guardians of the grouped student visit schedule for $periodLabel. Kindly adhere to the assigned session date, visiting hours, and attached group roster below."
        } else {
            "Dengan hormat, bersama surat edaran ini kami sampaikan pemberitahuan resmi pembagian jadwal kunjungan Orang Tua/Wali siswa untuk periode $periodLabel. Demi ketertiban dan kenyamanan bersama, Orang Tua/Wali dimohon hadir sesuai kelompok sesi, tanggal, dan jam kunjungan terlampir di bawah ini."
        }
    }

    fun defaultGuidelinesText(isEn: Boolean): String {
        return if (isEn) {
            listOf(
                "1. Please arrive according to your assigned session date and visiting hours.",
                "2. Show your Parent Portal QR pass or check in manually at the Wali Asuh reception desk upon arrival.",
                "3. If a parent cannot attend on the assigned date, contact the child's Wali Asuh beforehand to request a session swap.",
                "4. Maintain dormitory cleanliness, modesty, and refrain from leaving prohibited items or unapproved cash."
            ).joinToString("\n")
        } else {
            listOf(
                "1. Orang Tua/Wali hadir sesuai tanggal dan jam sesi kelompok yang telah ditetapkan.",
                "2. Wajib melapor / check-in di meja Wali Asuh saat tiba (tunjukkan QR Tiket atau catat daftar hadir manual).",
                "3. Apabila berhalangan pada tanggal sesi, silakan berkoordinasi dengan Wali Asuh anak untuk pengajuan tukar sesi.",
                "4. Menjaga ketertiban, kebersihan lingkungan asrama, serta tidak membawa/menitipkan barang terlarang."
            ).joinToString("\n")
        }
    }

    private fun wrapParagraphLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return emptyList()
        val safeMaxW = maxWidth.coerceAtLeast(24f)
        val result = mutableListOf<String>()
        text.split('\n').forEach { rawPara ->
            val trimmed = rawPara.trim()
            if (trimmed.isEmpty()) {
                result.add("")
            } else {
                val words = trimmed.split(Regex("\\s+"))
                var current = StringBuilder()

                fun appendOversizedToken(token: String) {
                    var start = 0
                    while (start < token.length) {
                        var end = start + 1
                        while (end < token.length && paint.measureText(token.substring(start, end + 1)) <= safeMaxW) {
                            end++
                        }
                        val chunk = token.substring(start, end)
                        if (end < token.length) {
                            result.add(chunk)
                        } else {
                            current = StringBuilder(chunk)
                        }
                        start = end
                    }
                }

                for (w in words) {
                    val candidate = if (current.isEmpty()) w else "$current $w"
                    if (paint.measureText(candidate) <= safeMaxW) {
                        current = StringBuilder(candidate)
                    } else {
                        if (current.isNotEmpty()) {
                            result.add(current.toString())
                            current = StringBuilder()
                        }
                        if (paint.measureText(w) <= safeMaxW) {
                            current = StringBuilder(w)
                        } else {
                            appendOversizedToken(w)
                        }
                    }
                }
                if (current.isNotEmpty()) result.add(current.toString())
            }
        }
        return result
    }

    private fun measureWrappedLines(
        text: String,
        paint: Paint,
        textSize: Float,
        isBold: Boolean,
        maxWidth: Float,
        fallback: String = "-"
    ): List<String> {
        paint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
        paint.textSize = textSize
        val lines = wrapParagraphLines(text, paint, maxWidth).filter { it.isNotEmpty() }
        return if (lines.isEmpty()) listOf(fallback) else lines
    }

    private fun drawWrappedLines(
        canvas: Canvas,
        lines: List<String>,
        x: Float,
        firstBaselineY: Float,
        lineHeight: Float,
        paint: Paint
    ) {
        lines.forEachIndexed { idx, line ->
            canvas.drawText(line, x, firstBaselineY + idx * lineHeight, paint)
        }
    }

    fun generateMonthlyCircularPdf(
        context: Context,
        sessions: List<VisitSession>,
        allStudents: List<ChildStudent>,
        swaps: List<VisitSessionSwap> = emptyList(),
        allSchoolSessions: List<VisitSession> = sessions,
        schoolProfile: SchoolProfile = SchoolProfile(),
        config: VisitCircularPdfConfig = VisitCircularPdfConfig()
    ): File? {
        val document = PdfDocument()
        var currentPage: PdfDocument.Page? = null
        return try {
            var pageNum = 0
            renderMonthlyCircularPages(
                context = context,
                sessions = sessions,
                allStudents = allStudents,
                swaps = swaps,
                allSchoolSessions = allSchoolSessions,
                schoolProfile = schoolProfile,
                config = config,
                startNewCanvas = {
                    currentPage?.let { document.finishPage(it) }
                    currentPage = null
                    pageNum++
                    val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                    val created = document.startPage(info)
                    currentPage = created
                    created.canvas
                },
                finishCanvas = {
                    currentPage?.let {
                        document.finishPage(it)
                        currentPage = null
                    }
                }
            )
            currentPage?.let {
                document.finishPage(it)
                currentPage = null
            }

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val monthSlug = config.monthYear
                .replace(Regex("[^a-zA-Z0-9_-]"), "_")
                .ifBlank { "Semua" }
            val file = File(outputDir, "Edaran_Jadwal_Kunjungan_${monthSlug}_${System.currentTimeMillis()}.pdf")
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

    internal fun renderMonthlyCircularPages(
        context: Context,
        sessions: List<VisitSession>,
        allStudents: List<ChildStudent>,
        swaps: List<VisitSessionSwap> = emptyList(),
        allSchoolSessions: List<VisitSession> = sessions,
        schoolProfile: SchoolProfile = SchoolProfile(),
        config: VisitCircularPdfConfig = VisitCircularPdfConfig(),
        startNewCanvas: (pageNumber: Int) -> Canvas,
        finishCanvas: () -> Unit = {}
    ) {
        val lang = AppLanguageManager.getLanguage(context)
        val isEn = lang == AppLanguage.ENGLISH
        val boxLeft = MARGIN_X
        val boxRight = PAGE_WIDTH - MARGIN_X
        val contentW = boxRight - boxLeft

        val orderedSessions = sessions.sortedWith(
            compareBy<VisitSession> { it.getVisitDatesList().firstOrNull() ?: Long.MAX_VALUE }
                .thenBy { it.title.lowercase(lang.locale) }
        )
        val sessionStudentPairs = orderedSessions.map { sess ->
            val roster = VisitHelper.effectiveScheduledStudentsForSession(
                session = sess,
                allStudents = allStudents,
                swaps = swaps,
                allSessions = allSchoolSessions
            )
            sess to roster
        }
        val coveredStudentIds = sessionStudentPairs.flatMap { (_, list) -> list.map { it.id } }.toSet()
        val unscheduledStudents = if (config.includeUnscheduledAppendix) {
            allStudents
                .filter { it.id !in coveredStudentIds }
                .sortedWith(
                    compareBy<ChildStudent> { VisitHelper.resolveStudentDistrictKey(it).lowercase(Locale.US) }
                        .thenBy { it.name.lowercase(Locale.US) }
                )
        } else {
            emptyList()
        }

        val periodLabel = when {
            config.monthYear != "ALL" && config.monthYear.isNotBlank() ->
                VisitHelper.formatMonthYearLabel(config.monthYear, lang.locale)
            orderedSessions.map { it.monthYear }.distinct().size == 1 ->
                VisitHelper.formatMonthYearLabel(orderedSessions.first().monthYear, lang.locale)
            else -> if (isEn) "All Active Visit Sessions" else "Seluruh Sesi Kunjungan Aktif"
        }

        val dateFullFmt = SimpleDateFormat("EEEE, d MMMM yyyy", lang.locale)
        val dateShortFmt = SimpleDateFormat("EEE, d MMM yyyy", lang.locale)
        val printDateFmt = SimpleDateFormat(if (isEn) "MMMM d, yyyy" else "d MMMM yyyy", lang.locale)

        var pageNumber = 1
        var canvas = startNewCanvas(pageNumber)
        val paint = Paint().apply { isAntiAlias = true }

        fun nextPage(): Canvas {
            finishCanvas()
            pageNumber++
            return startNewCanvas(pageNumber)
        }

        // ============================================================
        // PAGE 1: SURAT EDARAN PEMBERITAHUAN RESMI & RINGKASAN SESI
        // ============================================================
        var currentY = 35f
        currentY = PdfReportGenerator.drawHeader(
            context = context,
            canvas = canvas,
            paint = paint,
            width = PAGE_WIDTH,
            startY = currentY,
            schoolProfile = schoolProfile,
            unitTitle = if (isEn) "OFFICIAL PARENT VISIT ANNOUNCEMENT CIRCULAR" else "SURAT EDARAN PEMBERITAHUAN KUNJUNGAN ORANG TUA"
        )

        paint.textAlign = Paint.Align.CENTER
        val mainTitleLines = measureWrappedLines(
            text = if (isEn) "PARENT VISIT SCHEDULE CIRCULAR" else "EDARAN PEMBAGIAN JADWAL KUNJUNGAN WALI SISWA",
            paint = paint,
            textSize = 12.5f,
            isBold = true,
            maxWidth = contentW
        )
        paint.color = Color.rgb(15, 23, 42)
        mainTitleLines.forEachIndexed { idx, line ->
            canvas.drawText(line, PAGE_WIDTH / 2f, currentY + 6f + idx * 15f, paint)
        }
        currentY += 6f + (mainTitleLines.size - 1) * 15f + 14f

        val periodLines = measureWrappedLines(
            text = (if (isEn) "PERIOD: " else "PERIODE: ") + periodLabel.uppercase(lang.locale),
            paint = paint,
            textSize = 9.5f,
            isBold = true,
            maxWidth = contentW
        )
        paint.color = Color.rgb(15, 118, 110)
        periodLines.forEachIndexed { idx, line ->
            canvas.drawText(line, PAGE_WIDTH / 2f, currentY + idx * 12f, paint)
        }
        paint.textAlign = Paint.Align.LEFT
        currentY += (periodLines.size - 1) * 12f + 16f

        // Metadata box (Nomor, Perihal, Tanggal Cetak) dengan wrap otomatis
        val cleanNo = config.circularNumber.trim()
        val metaTextMaxW = contentW - 20f
        val refLines = if (cleanNo.isNotBlank()) {
            measureWrappedLines(
                if (isEn) "Ref No   : $cleanNo" else "Nomor    : $cleanNo",
                paint, 8.6f, true, metaTextMaxW
            )
        } else {
            emptyList()
        }
        val subjectLines = measureWrappedLines(
            if (isEn) {
                "Subject  : Parent Visit Schedule & Student Grouping ($periodLabel)"
            } else {
                "Perihal  : Pemberitahuan Jadwal & Pengelompokan Sesi Kunjungan ($periodLabel)"
            },
            paint, 8.6f, true, metaTextMaxW
        )
        val issuedLines = measureWrappedLines(
            if (isEn) {
                "Issued   : ${printDateFmt.format(Date())}  •  ${orderedSessions.size} Session(s)  •  ${coveredStudentIds.size} Student(s)"
            } else {
                "Tanggal  : ${printDateFmt.format(Date())}  •  ${orderedSessions.size} Sesi Terjadwal  •  ${coveredStudentIds.size} Anak Asuh"
            },
            paint, 8.6f, false, metaTextMaxW
        )
        val metaLineH = 12.5f
        val totalMetaLines = refLines.size + subjectLines.size + issuedLines.size
        val metaHeight = 10f + totalMetaLines * metaLineH + 4f

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(RectF(boxLeft, currentY, boxRight, currentY + metaHeight), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 0.8f
        canvas.drawRoundRect(RectF(boxLeft, currentY, boxRight, currentY + metaHeight), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        var metaY = currentY + 14.5f
        paint.textSize = 8.6f
        if (refLines.isNotEmpty()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.rgb(51, 65, 85)
            drawWrappedLines(canvas, refLines, boxLeft + 10f, metaY, metaLineH, paint)
            metaY += refLines.size * metaLineH
        }
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(15, 23, 42)
        drawWrappedLines(canvas, subjectLines, boxLeft + 10f, metaY, metaLineH, paint)
        metaY += subjectLines.size * metaLineH

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(71, 85, 105)
        drawWrappedLines(canvas, issuedLines, boxLeft + 10f, metaY, metaLineH, paint)
        currentY += metaHeight + 12f

        // Opening Paragraph
        val effectiveOpening = config.openingParagraph.trim().ifBlank {
            defaultOpeningParagraph(periodLabel, isEn)
        }
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.2f
        paint.color = Color.rgb(30, 41, 59)
        val openingLines = wrapParagraphLines(effectiveOpening, paint, contentW)
        openingLines.forEach { line ->
            if (currentY > PAGE_HEIGHT - 120f) {
                canvas = nextPage()
                currentY = 40f
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, boxLeft, currentY, paint)
                currentY += 13f
            } else {
                currentY += 6f
            }
        }
        currentY += 10f

        // Section: Ringkasan Jadwal Sesi
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        paint.color = Color.rgb(3, 105, 161)
        canvas.drawText(
            if (isEn) "SUMMARY OF VISIT SESSIONS & STUDENT GROUPS:" else "RINGKASAN PEMBAGIAN SESI KUNJUNGAN:",
            boxLeft,
            currentY,
            paint
        )
        currentY += 8f

        val sColNo = boxLeft
        val sColTitle = boxLeft + 24f
        val sColDate = boxLeft + 154f
        val sColDist = boxLeft + 292f
        val sColCount = boxRight - 58f

        fun drawSummaryHeader(yPos: Float): Float {
            val hH = 22f
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(224, 242, 254)
            canvas.drawRect(boxLeft, yPos, boxRight, yPos + hH, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(56, 189, 248)
            paint.strokeWidth = 0.8f
            canvas.drawRect(boxLeft, yPos, boxRight, yPos + hH, paint)
            listOf(sColTitle, sColDate, sColDist, sColCount).forEach { x ->
                canvas.drawLine(x, yPos, x, yPos + hH, paint)
            }
            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8f
            paint.color = Color.rgb(3, 105, 161)
            val ty = yPos + 14.5f
            canvas.drawText("No", sColNo + 5f, ty, paint)
            canvas.drawText(if (isEn) "Session" else "Sesi", sColTitle + 5f, ty, paint)
            canvas.drawText(if (isEn) "Date & Hours" else "Tanggal & Jam", sColDate + 5f, ty, paint)
            canvas.drawText(if (isEn) "Districts" else "Kecamatan", sColDist + 5f, ty, paint)
            canvas.drawText(if (isEn) "Students" else "Siswa", sColCount + 5f, ty, paint)
            return yPos + hH
        }

        if (sessionStudentPairs.isEmpty()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 9f
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText(
                if (isEn) "No sessions selected for this circular." else "Belum ada sesi kunjungan dipilih untuk edaran ini.",
                boxLeft + 4f,
                currentY + 14f,
                paint
            )
            currentY += 28f
        } else {
            currentY = drawSummaryHeader(currentY)
            val sumLineH = 10.5f
            sessionStudentPairs.forEachIndexed { idx, (sess, roster) ->
                val titleLines = measureWrappedLines(
                    sess.title.ifBlank { "-" },
                    paint, 8f, true, sColDate - sColTitle - 10f
                )
                val datesLabel = sess.getVisitDatesList()
                    .joinToString(", ") { dateShortFmt.format(Date(it)) }
                    .ifBlank { "-" }
                val dateLines = measureWrappedLines(
                    datesLabel,
                    paint, 7.6f, false, sColDist - sColDate - 10f
                )
                val timeLines = measureWrappedLines(
                    sess.displayTimeRange(),
                    paint, 7.6f, false, sColDist - sColDate - 10f
                )
                val distSummary = sess.getDistrictsList()
                    .joinToString(", ") { VisitHelper.displayDistrict(it, isEn) }
                    .ifBlank { "-" }
                val distLines = measureWrappedLines(
                    distSummary,
                    paint, 7.8f, false, sColCount - sColDist - 10f
                )
                val countLabel = if (isEn) "${roster.size} ch." else "${roster.size} anak"
                val countLines = measureWrappedLines(
                    countLabel,
                    paint, 8.2f, true, boxRight - sColCount - 10f
                )

                val maxLines = maxOf(
                    1,
                    titleLines.size,
                    dateLines.size + timeLines.size,
                    distLines.size,
                    countLines.size
                )
                val contentBlockH = maxLines * sumLineH
                val rH = maxOf(26f, contentBlockH + 10f)

                if (currentY + rH > PAGE_HEIGHT - 70f) {
                    canvas = nextPage()
                    currentY = 40f
                    currentY = drawSummaryHeader(currentY)
                }

                paint.style = Paint.Style.FILL
                paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                canvas.drawRect(boxLeft, currentY, boxRight, currentY + rH, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(203, 213, 225)
                paint.strokeWidth = 0.6f
                canvas.drawRect(boxLeft, currentY, boxRight, currentY + rH, paint)
                listOf(sColTitle, sColDate, sColDist, sColCount).forEach { x ->
                    canvas.drawLine(x, currentY, x, currentY + rH, paint)
                }
                paint.style = Paint.Style.FILL

                val firstBaseY = currentY + (rH - contentBlockH) / 2f + 7.6f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8f
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText("${idx + 1}.", sColNo + 5f, firstBaseY, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8f
                drawWrappedLines(canvas, titleLines, sColTitle + 5f, firstBaseY, sumLineH, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 7.6f
                paint.color = Color.rgb(15, 23, 42)
                drawWrappedLines(canvas, dateLines, sColDate + 5f, firstBaseY, sumLineH, paint)
                paint.color = Color.rgb(15, 118, 110)
                drawWrappedLines(
                    canvas,
                    timeLines,
                    sColDate + 5f,
                    firstBaseY + dateLines.size * sumLineH,
                    sumLineH,
                    paint
                )

                paint.textSize = 7.8f
                paint.color = Color.rgb(51, 65, 85)
                drawWrappedLines(canvas, distLines, sColDist + 5f, firstBaseY, sumLineH, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.2f
                paint.color = Color.rgb(29, 78, 216)
                drawWrappedLines(canvas, countLines, sColCount + 5f, firstBaseY, sumLineH, paint)

                currentY += rH
            }
            currentY += 12f
        }

        // Guidelines Box
        val effectiveGuidelines = config.guidelinesText.trim().ifBlank {
            defaultGuidelinesText(isEn)
        }
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.4f
        val guideLines = wrapParagraphLines(effectiveGuidelines, paint, contentW - 20f)
        if (guideLines.isNotEmpty()) {
            val gContentH = guideLines.sumOf { if (it.isNotEmpty()) 12.5 else 5.0 }.toFloat()
            val gBoxH = 30f + gContentH + 8f
            if (currentY + gBoxH > PAGE_HEIGHT - 135f) {
                canvas = nextPage()
                currentY = 40f
            }
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(240, 253, 250)
            canvas.drawRoundRect(RectF(boxLeft, currentY, boxRight, currentY + gBoxH), 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(153, 246, 228)
            paint.strokeWidth = 0.9f
            canvas.drawRoundRect(RectF(boxLeft, currentY, boxRight, currentY + gBoxH), 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.8f
            paint.color = Color.rgb(15, 118, 110)
            canvas.drawText(
                if (isEn) "VISITING GUIDELINES & REMINDERS:" else "KETENTUAN & TATA TERTIB KUNJUNGAN:",
                boxLeft + 10f,
                currentY + 16f,
                paint
            )
            var gy = currentY + 30f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.3f
            paint.color = Color.rgb(30, 41, 59)
            guideLines.forEach { gl ->
                if (gl.isNotEmpty()) {
                    canvas.drawText(gl, boxLeft + 10f, gy, paint)
                    gy += 12.5f
                } else {
                    gy += 5f
                }
            }
            currentY += gBoxH + 12f
        }

        // Cover Letter Footer Signature
        if (currentY > PAGE_HEIGHT - 135f) {
            canvas = nextPage()
            currentY = 40f
        }
        val effectiveSigner = config.signerName.trim().ifBlank {
            if (isEn) "School Admin / Principal" else "Admin / Koordinator Sekolah"
        }
        PdfReportGenerator.drawFooterSignatures(
            context = context,
            canvas = canvas,
            paint = paint,
            width = PAGE_WIDTH,
            height = PAGE_HEIGHT,
            startY = currentY,
            waliName = effectiveSigner,
            schoolProfile = schoolProfile
        )

        // ============================================================
        // APPENDICES: DAFTAR ANAK TERKELOMPOK PER SESI
        // ============================================================
        val cColNo = boxLeft
        val cColName = boxLeft + 24f
        val cColClass = boxLeft + 144f
        val cColDist = boxLeft + 236f
        val cColWali = boxLeft + 314f
        val cColNote = boxLeft + 402f

        fun drawStudentTableHeader(yPos: Float): Float {
            val hH = 22f
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(224, 242, 254)
            canvas.drawRect(boxLeft, yPos, boxRight, yPos + hH, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(56, 189, 248)
            paint.strokeWidth = 0.8f
            canvas.drawRect(boxLeft, yPos, boxRight, yPos + hH, paint)
            listOf(cColName, cColClass, cColDist, cColWali, cColNote).forEach { x ->
                canvas.drawLine(x, yPos, x, yPos + hH, paint)
            }
            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 7.9f
            paint.color = Color.rgb(3, 105, 161)
            val ty = yPos + 14.5f
            canvas.drawText("No", cColNo + 5f, ty, paint)
            canvas.drawText(if (isEn) "Student Name" else "Nama Anak Asuh", cColName + 4f, ty, paint)
            canvas.drawText(if (isEn) "Class / Dorm" else "Kelas / Asrama", cColClass + 4f, ty, paint)
            canvas.drawText(if (isEn) "District" else "Kecamatan", cColDist + 4f, ty, paint)
            canvas.drawText(if (isEn) "Wali Asuh" else "Wali Asuh", cColWali + 4f, ty, paint)
            canvas.drawText(if (isEn) "Notes" else "Keterangan", cColNote + 4f, ty, paint)
            return yPos + hH
        }

        fun measureSessionBanner(
            sessionIndex: Int,
            session: VisitSession,
            count: Int
        ): Triple<List<String>, List<String>, Float> {
            val titleText = if (isEn) {
                "APPENDIX ${sessionIndex + 1}: ${session.title.uppercase(lang.locale)} ($count Students)"
            } else {
                "LAMPIRAN ${sessionIndex + 1}: ${session.title.uppercase(lang.locale)} ($count Anak)"
            }
            val titleLines = measureWrappedLines(titleText, paint, 10f, true, contentW - 18f)

            val datesText = session.getVisitDatesList()
                .joinToString(", ") { dateFullFmt.format(Date(it)) }
                .ifBlank { "-" }
            val distsText = session.getDistrictsList()
                .joinToString(", ") { VisitHelper.displayDistrict(it, isEn) }
                .ifBlank { "-" }
            val metaText = "$datesText  •  ${session.displayTimeRange()}  •  Kec: $distsText"
            val metaLines = measureWrappedLines(metaText, paint, 8f, false, contentW - 18f)

            val bannerH = 8f + titleLines.size * 13f + 3f + metaLines.size * 11f + 6f
            return Triple(titleLines, metaLines, bannerH)
        }

        fun drawSessionSectionHeader(
            titleLines: List<String>,
            metaLines: List<String>,
            bannerH: Float,
            yPos: Float
        ): Float {
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(239, 246, 255)
            canvas.drawRoundRect(RectF(boxLeft, yPos, boxRight, yPos + bannerH), 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(147, 197, 253)
            paint.strokeWidth = 1f
            canvas.drawRoundRect(RectF(boxLeft, yPos, boxRight, yPos + bannerH), 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10f
            paint.color = Color.rgb(29, 78, 216)
            drawWrappedLines(canvas, titleLines, boxLeft + 9f, yPos + 17f, 13f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f
            paint.color = Color.rgb(51, 65, 85)
            val metaStartY = yPos + 8f + titleLines.size * 13f + 3f + 8.5f
            drawWrappedLines(canvas, metaLines, boxLeft + 9f, metaStartY, 11f, paint)
            return yPos + bannerH + 6f
        }

        fun drawStudentRow(
            rowIdx: Int,
            student: ChildStudent,
            distColor: Int,
            noteLabel: String,
            noteColor: Int,
            noteBold: Boolean,
            continuationTitle: String?
        ) {
            val cellFontSize = 7.9f
            val rowLineH = 10f
            val nameLines = measureWrappedLines(
                student.name.ifBlank { "-" },
                paint, cellFontSize, true, cColClass - cColName - 8f
            )
            val classDorm = listOf(student.classGrade.trim(), student.dormitory.trim())
                .filter { it.isNotBlank() }
                .joinToString(" • ")
                .ifBlank { "-" }
            val classLines = measureWrappedLines(
                classDorm,
                paint, cellFontSize, false, cColDist - cColClass - 8f
            )
            val distKey = VisitHelper.resolveStudentDistrictKey(student)
            val distLabel = VisitHelper.displayDistrict(distKey, isEn)
            val distLines = measureWrappedLines(
                distLabel,
                paint, cellFontSize, false, cColWali - cColDist - 8f
            )
            val waliText = student.assignedWaliName.ifBlank { student.assignedWaliUsername }.ifBlank { "-" }
            val waliLines = measureWrappedLines(
                waliText,
                paint, cellFontSize, false, cColNote - cColWali - 8f
            )
            val noteLines = measureWrappedLines(
                noteLabel,
                paint, cellFontSize, noteBold, boxRight - cColNote - 8f
            )

            val maxLines = maxOf(
                1,
                nameLines.size,
                classLines.size,
                distLines.size,
                waliLines.size,
                noteLines.size
            )
            val contentBlockH = maxLines * rowLineH
            val rowH = maxOf(22f, contentBlockH + 8f)

            if (currentY + rowH > PAGE_HEIGHT - 42f) {
                canvas = nextPage()
                currentY = 38f
                if (!continuationTitle.isNullOrBlank()) {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 8.6f
                    paint.color = Color.rgb(100, 116, 139)
                    canvas.drawText(
                        if (isEn) {
                            "Continued: $continuationTitle (Page $pageNumber)"
                        } else {
                            "Lanjutan Lampiran: $continuationTitle (Halaman $pageNumber)"
                        },
                        boxLeft,
                        currentY,
                        paint
                    )
                    currentY += 10f
                }
                currentY = drawStudentTableHeader(currentY)
            }

            paint.style = Paint.Style.FILL
            paint.color = if (rowIdx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
            canvas.drawRect(boxLeft, currentY, boxRight, currentY + rowH, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(203, 213, 225)
            paint.strokeWidth = 0.5f
            canvas.drawRect(boxLeft, currentY, boxRight, currentY + rowH, paint)
            listOf(cColName, cColClass, cColDist, cColWali, cColNote).forEach { x ->
                canvas.drawLine(x, currentY, x, currentY + rowH, paint)
            }
            paint.style = Paint.Style.FILL

            val firstBaseY = currentY + (rowH - contentBlockH) / 2f + 7.4f
            paint.textSize = cellFontSize

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText("${rowIdx + 1}.", cColNo + 4f, firstBaseY, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            drawWrappedLines(canvas, nameLines, cColName + 4f, firstBaseY, rowLineH, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(51, 65, 85)
            drawWrappedLines(canvas, classLines, cColClass + 4f, firstBaseY, rowLineH, paint)

            paint.color = distColor
            drawWrappedLines(canvas, distLines, cColDist + 4f, firstBaseY, rowLineH, paint)

            paint.color = Color.rgb(51, 65, 85)
            drawWrappedLines(canvas, waliLines, cColWali + 4f, firstBaseY, rowLineH, paint)

            paint.color = noteColor
            paint.typeface = Typeface.create(
                Typeface.DEFAULT,
                if (noteBold) Typeface.BOLD else Typeface.NORMAL
            )
            drawWrappedLines(canvas, noteLines, cColNote + 4f, firstBaseY, rowLineH, paint)

            currentY += rowH
        }

        if (sessionStudentPairs.isNotEmpty()) {
            canvas = nextPage()
            currentY = 38f
        }

        sessionStudentPairs.forEachIndexed { sIdx, (sess, roster) ->
            val (titleLines, metaLines, bannerH) = measureSessionBanner(sIdx, sess, roster.size)
            val minNeededAfterBanner = bannerH + 6f + 22f + 26f
            if (sIdx > 0) {
                if (config.pageBreakPerSession || currentY + minNeededAfterBanner > PAGE_HEIGHT - 45f) {
                    canvas = nextPage()
                    currentY = 38f
                } else {
                    currentY += 14f
                }
            }
            currentY = drawSessionSectionHeader(titleLines, metaLines, bannerH, currentY)

            if (roster.isEmpty()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 8.8f
                paint.color = Color.rgb(100, 116, 139)
                canvas.drawText(
                    if (isEn) "No students assigned to this session." else "Belum ada siswa terjadwal pada sesi ini.",
                    boxLeft + 6f,
                    currentY + 14f,
                    paint
                )
                currentY += 28f
            } else {
                currentY = drawStudentTableHeader(currentY)
                roster.forEachIndexed { rowIdx, st ->
                    val incomingSwap = VisitHelper.findIncomingSwapToSession(sess.id, st, swaps)
                    val noteLabel = if (incomingSwap != null) {
                        val originName = incomingSwap.fromSessionTitle.ifBlank {
                            if (isEn) "previous session" else "sesi asal"
                        }
                        if (isEn) "Moved ($originName)" else "Pindahan ($originName)"
                    } else {
                        sess.title
                    }
                    drawStudentRow(
                        rowIdx = rowIdx,
                        student = st,
                        distColor = Color.rgb(51, 65, 85),
                        noteLabel = noteLabel,
                        noteColor = if (incomingSwap != null) Color.rgb(67, 56, 202) else Color.rgb(100, 116, 139),
                        noteBold = incomingSwap != null,
                        continuationTitle = sess.title
                    )
                }
            }
        }

        // Optional Unscheduled Students Appendix
        if (unscheduledStudents.isNotEmpty()) {
            val warnText = if (isEn) {
                "NOTICE: STUDENTS NOT YET ASSIGNED TO SELECTED SESSIONS (${unscheduledStudents.size} Students)"
            } else {
                "CATATAN: SISWA BELUM MASUK KELOMPOK SESI TERPILIH (${unscheduledStudents.size} Anak)"
            }
            val warnLines = measureWrappedLines(warnText, paint, 9.2f, true, contentW - 18f)
            val warnH = 12f + warnLines.size * 12.5f + 6f

            if (currentY + warnH + 6f + 22f + 26f > PAGE_HEIGHT - 45f) {
                canvas = nextPage()
                currentY = 38f
            } else {
                currentY += 16f
            }
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(255, 251, 235)
            canvas.drawRoundRect(RectF(boxLeft, currentY, boxRight, currentY + warnH), 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(245, 158, 11)
            paint.strokeWidth = 0.9f
            canvas.drawRoundRect(RectF(boxLeft, currentY, boxRight, currentY + warnH), 6f, 6f, paint)
            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.2f
            paint.color = Color.rgb(146, 64, 14)
            drawWrappedLines(canvas, warnLines, boxLeft + 9f, currentY + 19f, 12.5f, paint)

            currentY += warnH + 6f
            currentY = drawStudentTableHeader(currentY)
            unscheduledStudents.forEachIndexed { idx, st ->
                val distKey = VisitHelper.resolveStudentDistrictKey(st)
                val noteText = if (distKey == VisitHelper.UNASSIGNED_DISTRICT) {
                    if (isEn) "No District" else "Kecamatan Kosong"
                } else {
                    if (isEn) "Unassigned" else "Belum Masuk Sesi"
                }
                drawStudentRow(
                    rowIdx = idx,
                    student = st,
                    distColor = if (distKey == VisitHelper.UNASSIGNED_DISTRICT) Color.rgb(180, 83, 9) else Color.rgb(51, 65, 85),
                    noteLabel = noteText,
                    noteColor = Color.rgb(180, 83, 9),
                    noteBold = false,
                    continuationTitle = if (isEn) "Unassigned Students" else "Siswa Belum Masuk Sesi"
                )
            }
        }

        // Document footer note at bottom of last page
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.textSize = 7f
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText(
            if (isEn) {
                "Official Visit Circular ($periodLabel) • ${orderedSessions.size} Session(s) • ${coveredStudentIds.size} Grouped Students"
            } else {
                "Edaran Resmi Kunjungan ($periodLabel) • ${orderedSessions.size} Sesi • ${coveredStudentIds.size} Anak Asuh Terkelompok"
            },
            PAGE_WIDTH / 2f,
            PAGE_HEIGHT - 16f,
            paint
        )
        paint.textAlign = Paint.Align.LEFT
        finishCanvas()
    }
}
