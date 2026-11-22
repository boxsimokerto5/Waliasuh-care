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
        val result = mutableListOf<String>()
        text.split('\n').forEach { rawPara ->
            val trimmed = rawPara.trim()
            if (trimmed.isEmpty()) {
                result.add("")
            } else {
                val words = trimmed.split(Regex("\\s+"))
                var current = StringBuilder()
                for (w in words) {
                    val candidate = if (current.isEmpty()) w else "${current} $w"
                    if (paint.measureText(candidate) <= maxWidth) {
                        current = StringBuilder(candidate)
                    } else {
                        if (current.isNotEmpty()) {
                            result.add(current.toString())
                            current = StringBuilder(w)
                        } else {
                            result.add(w)
                            current = StringBuilder()
                        }
                    }
                }
                if (current.isNotEmpty()) result.add(current.toString())
            }
        }
        return result
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
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12.5f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(
            if (isEn) "PARENT VISIT SCHEDULE CIRCULAR" else "EDARAN PEMBAGIAN JADWAL KUNJUNGAN WALI SISWA",
            PAGE_WIDTH / 2f,
            currentY + 6f,
            paint
        )
        currentY += 20f

        paint.textSize = 9.5f
        paint.color = Color.rgb(15, 118, 110)
        canvas.drawText(
            (if (isEn) "PERIOD: " else "PERIODE: ") + periodLabel.uppercase(lang.locale),
            PAGE_WIDTH / 2f,
            currentY,
            paint
        )
        paint.textAlign = Paint.Align.LEFT
        currentY += 16f

        // Metadata box (Nomor, Perihal, Tanggal Cetak)
        val cleanNo = config.circularNumber.trim()
        val metaHeight = if (cleanNo.isNotBlank()) 52f else 38f
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(RectF(boxLeft, currentY, boxRight, currentY + metaHeight), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 0.8f
        canvas.drawRoundRect(RectF(boxLeft, currentY, boxRight, currentY + metaHeight), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        var metaY = currentY + 14f
        paint.textSize = 8.6f
        if (cleanNo.isNotBlank()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText(if (isEn) "Ref No   : $cleanNo" else "Nomor    : $cleanNo", boxLeft + 10f, metaY, paint)
            metaY += 13f
        }
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(
            if (isEn) {
                "Subject  : Parent Visit Schedule & Student Grouping ($periodLabel)"
            } else {
                "Perihal  : Pemberitahuan Jadwal & Pengelompokan Sesi Kunjungan ($periodLabel)"
            },
            boxLeft + 10f,
            metaY,
            paint
        )
        metaY += 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText(
            if (isEn) {
                "Issued   : ${printDateFmt.format(Date())}  •  ${orderedSessions.size} Session(s)  •  ${coveredStudentIds.size} Student(s)"
            } else {
                "Tanggal  : ${printDateFmt.format(Date())}  •  ${orderedSessions.size} Sesi Terjadwal  •  ${coveredStudentIds.size} Anak Asuh"
            },
            boxLeft + 10f,
            metaY,
            paint
        )
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
        val sColTitle = boxLeft + 26f
        val sColDate = boxLeft + 178f
        val sColDist = boxLeft + 332f
        val sColCount = boxRight - 56f

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
            sessionStudentPairs.forEachIndexed { idx, (sess, roster) ->
                if (currentY > PAGE_HEIGHT - 120f) {
                    canvas = nextPage()
                    currentY = 40f
                    currentY = drawSummaryHeader(currentY)
                }
                val rH = 26f
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

                val ty = currentY + 16.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8f
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText("${idx + 1}.", sColNo + 6f, ty, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(
                    TextUtils.ellipsize(sess.title, TextPaint(paint), sColDate - sColTitle - 8f, TextUtils.TruncateAt.END).toString(),
                    sColTitle + 5f,
                    ty,
                    paint
                )

                val datesLabel = sess.getVisitDatesList()
                    .joinToString(", ") { dateShortFmt.format(Date(it)) }
                    .ifBlank { "-" }
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 7.6f
                canvas.drawText(
                    TextUtils.ellipsize(datesLabel, TextPaint(paint), sColDist - sColDate - 8f, TextUtils.TruncateAt.END).toString(),
                    sColDate + 5f,
                    currentY + 11.5f,
                    paint
                )
                paint.color = Color.rgb(15, 118, 110)
                canvas.drawText(
                    TextUtils.ellipsize(sess.displayTimeRange(), TextPaint(paint), sColDist - sColDate - 8f, TextUtils.TruncateAt.END).toString(),
                    sColDate + 5f,
                    currentY + 21.5f,
                    paint
                )

                paint.color = Color.rgb(51, 65, 85)
                val distSummary = sess.getDistrictsList()
                    .joinToString(", ") { VisitHelper.displayDistrict(it, isEn) }
                    .ifBlank { "-" }
                canvas.drawText(
                    TextUtils.ellipsize(distSummary, TextPaint(paint), sColCount - sColDist - 8f, TextUtils.TruncateAt.END).toString(),
                    sColDist + 5f,
                    ty,
                    paint
                )

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.2f
                paint.color = Color.rgb(29, 78, 216)
                canvas.drawText(
                    if (isEn) "${roster.size} ch." else "${roster.size} anak",
                    sColCount + 5f,
                    ty,
                    paint
                )
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
            val gBoxH = 26f + guideLines.size * 12.5f + 6f
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
        val cColClass = boxLeft + 150f
        val cColDist = boxLeft + 252f
        val cColWali = boxLeft + 336f
        val cColNote = boxLeft + 424f

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

        fun drawSessionSectionHeader(sessionIndex: Int, session: VisitSession, count: Int, yPos: Float): Float {
            val bannerH = 44f
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
            val titleText = if (isEn) {
                "APPENDIX ${sessionIndex + 1}: ${session.title.uppercase(lang.locale)} ($count Students)"
            } else {
                "LAMPIRAN ${sessionIndex + 1}: ${session.title.uppercase(lang.locale)} ($count Anak)"
            }
            canvas.drawText(
                TextUtils.ellipsize(titleText, TextPaint(paint), contentW - 18f, TextUtils.TruncateAt.END).toString(),
                boxLeft + 9f,
                yPos + 16f,
                paint
            )

            val datesText = session.getVisitDatesList()
                .joinToString(", ") { dateFullFmt.format(Date(it)) }
                .ifBlank { "-" }
            val distsText = session.getDistrictsList()
                .joinToString(", ") { VisitHelper.displayDistrict(it, isEn) }
                .ifBlank { "-" }
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f
            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText(
                TextUtils.ellipsize(
                    "$datesText  •  ${session.displayTimeRange()}  •  Kec: $distsText",
                    TextPaint(paint),
                    contentW - 18f,
                    TextUtils.TruncateAt.END
                ).toString(),
                boxLeft + 9f,
                yPos + 32f,
                paint
            )
            return yPos + bannerH + 6f
        }

        if (sessionStudentPairs.isNotEmpty()) {
            canvas = nextPage()
            currentY = 38f
        }

        sessionStudentPairs.forEachIndexed { sIdx, (sess, roster) ->
            if (sIdx > 0) {
                if (config.pageBreakPerSession || currentY > PAGE_HEIGHT - 140f) {
                    canvas = nextPage()
                    currentY = 38f
                } else {
                    currentY += 14f
                }
            }
            currentY = drawSessionSectionHeader(sIdx, sess, roster.size, currentY)

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
                val rowH = 22f
                roster.forEachIndexed { rowIdx, st ->
                    if (currentY > PAGE_HEIGHT - 55f) {
                        canvas = nextPage()
                        currentY = 38f
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.textSize = 8.6f
                        paint.color = Color.rgb(100, 116, 139)
                        canvas.drawText(
                            if (isEn) {
                                "Continued: ${sess.title} (Page $pageNumber)"
                            } else {
                                "Lanjutan Lampiran: ${sess.title} (Halaman $pageNumber)"
                            },
                            boxLeft,
                            currentY,
                            paint
                        )
                        currentY += 10f
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

                    val ty = currentY + 14.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 7.9f
                    paint.color = Color.rgb(15, 23, 42)
                    canvas.drawText("${rowIdx + 1}.", cColNo + 5f, ty, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(
                        TextUtils.ellipsize(st.name, TextPaint(paint), cColClass - cColName - 7f, TextUtils.TruncateAt.END).toString(),
                        cColName + 4f,
                        ty,
                        paint
                    )

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.rgb(51, 65, 85)
                    val classDorm = listOf(st.classGrade.trim(), st.dormitory.trim())
                        .filter { it.isNotBlank() }
                        .joinToString(" • ")
                        .ifBlank { "-" }
                    canvas.drawText(
                        TextUtils.ellipsize(classDorm, TextPaint(paint), cColDist - cColClass - 7f, TextUtils.TruncateAt.END).toString(),
                        cColClass + 4f,
                        ty,
                        paint
                    )

                    val distLabel = VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(st), isEn)
                    canvas.drawText(
                        TextUtils.ellipsize(distLabel, TextPaint(paint), cColWali - cColDist - 7f, TextUtils.TruncateAt.END).toString(),
                        cColDist + 4f,
                        ty,
                        paint
                    )

                    val waliText = st.assignedWaliName.ifBlank { st.assignedWaliUsername }.ifBlank { "-" }
                    canvas.drawText(
                        TextUtils.ellipsize(waliText, TextPaint(paint), cColNote - cColWali - 7f, TextUtils.TruncateAt.END).toString(),
                        cColWali + 4f,
                        ty,
                        paint
                    )

                    val incomingSwap = VisitHelper.findIncomingSwapToSession(sess.id, st, swaps)
                    val noteLabel = if (incomingSwap != null) {
                        val originName = incomingSwap.fromSessionTitle.ifBlank {
                            if (isEn) "previous session" else "sesi asal"
                        }
                        if (isEn) "Moved ($originName)" else "Pindahan ($originName)"
                    } else {
                        sess.title
                    }
                    paint.color = if (incomingSwap != null) Color.rgb(67, 56, 202) else Color.rgb(100, 116, 139)
                    paint.typeface = Typeface.create(
                        Typeface.DEFAULT,
                        if (incomingSwap != null) Typeface.BOLD else Typeface.NORMAL
                    )
                    canvas.drawText(
                        TextUtils.ellipsize(noteLabel, TextPaint(paint), boxRight - cColNote - 7f, TextUtils.TruncateAt.END).toString(),
                        cColNote + 4f,
                        ty,
                        paint
                    )

                    currentY += rowH
                }
            }
        }

        // Optional Unscheduled Students Appendix
        if (unscheduledStudents.isNotEmpty()) {
            if (currentY > PAGE_HEIGHT - 140f) {
                canvas = nextPage()
                currentY = 38f
            } else {
                currentY += 16f
            }
            val warnH = 34f
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
            canvas.drawText(
                if (isEn) {
                    "NOTICE: STUDENTS NOT YET ASSIGNED TO SELECTED SESSIONS (${unscheduledStudents.size} Students)"
                } else {
                    "CATATAN: SISWA BELUM MASUK KELOMPOK SESI TERPILIH (${unscheduledStudents.size} Anak)"
                },
                boxLeft + 9f,
                currentY + 21f,
                paint
            )
            currentY += warnH + 6f
            currentY = drawStudentTableHeader(currentY)
            val rowH = 22f
            unscheduledStudents.forEachIndexed { idx, st ->
                if (currentY > PAGE_HEIGHT - 55f) {
                    canvas = nextPage()
                    currentY = 38f
                    currentY = drawStudentTableHeader(currentY)
                }
                paint.style = Paint.Style.FILL
                paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                canvas.drawRect(boxLeft, currentY, boxRight, currentY + rowH, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(203, 213, 225)
                paint.strokeWidth = 0.5f
                canvas.drawRect(boxLeft, currentY, boxRight, currentY + rowH, paint)
                listOf(cColName, cColClass, cColDist, cColWali, cColNote).forEach { x ->
                    canvas.drawLine(x, currentY, x, currentY + rowH, paint)
                }
                paint.style = Paint.Style.FILL
                val ty = currentY + 14.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 7.9f
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText("${idx + 1}.", cColNo + 5f, ty, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(
                    TextUtils.ellipsize(st.name, TextPaint(paint), cColClass - cColName - 7f, TextUtils.TruncateAt.END).toString(),
                    cColName + 4f,
                    ty,
                    paint
                )
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(51, 65, 85)
                val classDorm = listOf(st.classGrade.trim(), st.dormitory.trim()).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "-" }
                canvas.drawText(
                    TextUtils.ellipsize(classDorm, TextPaint(paint), cColDist - cColClass - 7f, TextUtils.TruncateAt.END).toString(),
                    cColClass + 4f,
                    ty,
                    paint
                )
                val distKey = VisitHelper.resolveStudentDistrictKey(st)
                val distLabel = VisitHelper.displayDistrict(distKey, isEn)
                paint.color = if (distKey == VisitHelper.UNASSIGNED_DISTRICT) Color.rgb(180, 83, 9) else Color.rgb(51, 65, 85)
                canvas.drawText(
                    TextUtils.ellipsize(distLabel, TextPaint(paint), cColWali - cColDist - 7f, TextUtils.TruncateAt.END).toString(),
                    cColDist + 4f,
                    ty,
                    paint
                )
                paint.color = Color.rgb(51, 65, 85)
                val waliText = st.assignedWaliName.ifBlank { st.assignedWaliUsername }.ifBlank { "-" }
                canvas.drawText(
                    TextUtils.ellipsize(waliText, TextPaint(paint), cColNote - cColWali - 7f, TextUtils.TruncateAt.END).toString(),
                    cColWali + 4f,
                    ty,
                    paint
                )
                paint.color = Color.rgb(180, 83, 9)
                canvas.drawText(
                    if (distKey == VisitHelper.UNASSIGNED_DISTRICT) {
                        if (isEn) "No District" else "Kecamatan Kosong"
                    } else {
                        if (isEn) "Unassigned" else "Belum Masuk Sesi"
                    },
                    cColNote + 4f,
                    ty,
                    paint
                )
                currentY += rowH
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
