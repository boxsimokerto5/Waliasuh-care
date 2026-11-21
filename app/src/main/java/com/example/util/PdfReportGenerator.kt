package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.TextPaint
import android.text.TextUtils
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.KonselingHelper
import com.example.data.model.KonselingRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.SchoolProfile
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganTransaction
import com.example.data.model.VisitHelper
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.DayShiftBreakdown
import com.example.data.model.PinnedTaskItem
import com.example.data.model.SholatDisciplineTag
import com.example.data.model.SholatHelper
import com.example.data.model.SholatRecord
import com.example.data.model.SholatStatus
import com.example.data.model.SholatWaktu
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliScheduleHelper
import com.example.data.model.WaliShiftCode
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    enum class ReportPeriodType {
        MONTHLY,
        YEARLY,
        ALL
    }

    /**
     * Membuat dokumen PDF resmi Rekap Screening Kesehatan Siswa Sekolah Rakyat.
     * Mendukung rekap bulanan, tahunan, atau seluruh riwayat.
     */
    fun generateHealthReportPdf(
        context: Context,
        student: ChildStudent,
        records: List<MedicalRecord>,
        periodType: ReportPeriodType,
        periodLabel: String,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? {
        try {
            val lang = AppLanguageManager.getLanguage(context)
            val isEn = lang == AppLanguage.ENGLISH
            val document = PdfDocument()
            val pageWidth = 595 // Standard A4 width in points
            val pageHeight = 842 // Standard A4 height in points

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }

            var currentY = 35f

            // 1. Header / Kop Surat Resmi Tenant Sekolah
            currentY = drawHeader(context, canvas, paint, pageWidth, currentY, schoolProfile)

            // 2. Judul Dokumen & Periode
            currentY = drawTitleAndPeriod(canvas, paint, pageWidth, currentY, periodLabel, lang)

            // 3. Biodata Identitas Anak Asuh (Primary)
            currentY = drawStudentBio(canvas, paint, pageWidth, currentY, student, isEn)

            // 4. Ringkasan Eksekutif Frekuensi Sakit / Screening
            currentY = drawScreeningSummary(canvas, paint, pageWidth, currentY, records, periodLabel, isEn)

            // 5. Daftar Riwayat Kunjungan / Tindakan Medis
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            paint.color = Color.rgb(15, 23, 42)
            val detailHeader = if (isEn) {
                "EXAMINATION & MEDICAL ACTION DETAILS (${records.size} Visits):"
            } else {
                "RINCIAN PEMERIKSAAN & TINDAKAN MEDIS (${records.size} Kunjungan):"
            }
            canvas.drawText(detailHeader, 35f, currentY, paint)
            currentY += 14f

            if (records.isEmpty()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 10f
                paint.color = Color.rgb(100, 116, 139)
                val emptyDetail = if (isEn) {
                    "No medical / illness records found in this period (Healthy Status)."
                } else {
                    "Tidak ada catatan riwayat sakit / berobat pada periode ini (Kondisi Sehat)."
                }
                canvas.drawText(emptyDetail, 40f, currentY + 12f, paint)
                currentY += 30f
            } else {
                for ((index, record) in records.withIndex()) {
                    // Cek jika halaman hampir penuh, buat halaman baru
                    if (currentY > pageHeight - 140f) {
                        document.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = 40f

                        // Sub-header halaman lanjutan
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.textSize = 9f
                        paint.color = Color.rgb(100, 116, 139)
                        val contHeader = if (isEn) {
                            "Health Screening Recap: ${student.name} (Page $pageNumber)"
                        } else {
                            "Rekap Screening Kesehatan: ${student.name} (Halaman $pageNumber)"
                        }
                        canvas.drawText(contHeader, 35f, currentY, paint)
                        currentY += 16f
                    }

                    currentY = drawRecordItem(canvas, paint, pageWidth, currentY, index + 1, record, lang)
                    currentY += 10f
                }
            }

            // 6. Tanda Tangan & Validasi
            if (currentY > pageHeight - 140f) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = 40f
            }

            drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)

            document.finishPage(page)

            // Simpan file ke direktori cache aplikasi
            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanName = student.name.replace(Regex("[^a-zA-Z0-9]"), "_")
            val cleanPeriod = periodLabel.replace(Regex("[^a-zA-Z0-9]"), "_")
            val fileName = "Rekap_Kesehatan_${cleanName}_${cleanPeriod}.pdf"
            val file = File(outputDir, fileName)

            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    internal const val UNIT_LAYANAN_KESEHATAN = "UNIT LAYANAN KESEHATAN WALIASUH"
    internal const val UNIT_BIMBINGAN_SPIRITUAL = "UNIT BIMBINGAN SPIRITUAL WALIASUH"

    internal fun buildHeaderTitle(schoolProfile: SchoolProfile): String {
        return schoolProfile.schoolName.trim()
            .ifBlank { schoolProfile.shortName.trim().ifBlank { "WALI ASUH CARE" } }
            .uppercase(Locale("id", "ID"))
    }

    internal fun buildSubHeaderLine(schoolProfile: SchoolProfile): String {
        val cleanAddress = schoolProfile.address.trim()
        return when {
            cleanAddress.isNotBlank() -> cleanAddress
            schoolProfile.city.isNotBlank() -> schoolProfile.city.trim()
            else -> "Sekolah Rakyat • Indonesia"
        }
    }

    internal fun buildFooterSignatureTitle(schoolProfile: SchoolProfile): String {
        val schoolSignLabel = schoolProfile.shortName.trim().ifBlank { schoolProfile.schoolName.trim() }
        val signTitle = if (schoolSignLabel.isNotBlank()) "Wali Asuh $schoolSignLabel," else "Wali Asuh,"
        return signTitle.take(60)
    }

    private fun drawLogoAspectFit(canvas: Canvas, bitmap: Bitmap, targetBox: RectF) {
        if (bitmap.width <= 0 || bitmap.height <= 0) return
        val scale = minOf(targetBox.width() / bitmap.width, targetBox.height() / bitmap.height)
        val drawW = bitmap.width * scale
        val drawH = bitmap.height * scale
        val dx = targetBox.left + (targetBox.width() - drawW) / 2f
        val dy = targetBox.top + (targetBox.height() - drawH) / 2f
        val destRect = RectF(dx, dy, dx + drawW, dy + drawH)
        val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmap, null, destRect, bmpPaint)
    }

    internal fun drawHeader(
        context: Context,
        canvas: Canvas,
        paint: Paint,
        width: Int,
        startY: Float,
        schoolProfile: SchoolProfile = SchoolProfile(),
        unitTitle: String = UNIT_LAYANAN_KESEHATAN
    ): Float {
        val marginX = 35f
        val topY = startY - 12f
        val logoBoxSize = 44f

        val kemensosBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.logo_kemensos)
        } catch (_: Exception) {
            null
        }
        val sekolahRakyatBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.logo_sekolah_rakyat)
        } catch (_: Exception) {
            null
        }

        // Logo Kiri: Kemensos
        kemensosBitmap?.let { bmp ->
            drawLogoAspectFit(
                canvas,
                bmp,
                RectF(marginX, topY, marginX + logoBoxSize, topY + logoBoxSize)
            )
            bmp.recycle()
        }

        // Logo Kanan: Sekolah Rakyat
        sekolahRakyatBitmap?.let { bmp ->
            drawLogoAspectFit(
                canvas,
                bmp,
                RectF(width - marginX - logoBoxSize, topY, width - marginX, topY + logoBoxSize)
            )
            bmp.recycle()
        }

        val headerSchoolTitle = buildHeaderTitle(schoolProfile)
        val maxCenterTextWidth = width - (marginX + logoBoxSize + 12f) * 2f

        // Header text center
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        var titleSize = if (headerSchoolTitle.length > 42) 11f else 12.5f
        paint.textSize = titleSize
        while (titleSize > 8.5f && paint.measureText(headerSchoolTitle) > maxCenterTextWidth) {
            titleSize -= 0.5f
            paint.textSize = titleSize
        }
        paint.color = Color.rgb(15, 118, 110) // Teal color
        val titleToDraw = TextUtils.ellipsize(
            headerSchoolTitle,
            TextPaint(paint),
            maxCenterTextWidth,
            TextUtils.TruncateAt.END
        ).toString()
        var y = topY + 13f
        canvas.drawText(titleToDraw, width / 2f, y, paint)

        y += 13f
        paint.textSize = 10f
        paint.color = Color.rgb(30, 41, 59)
        canvas.drawText(unitTitle, width / 2f, y, paint)

        y += 11f
        val subHeaderText = buildSubHeaderLine(schoolProfile)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = if (subHeaderText.length > 62) 7.5f else 8f
        paint.color = Color.rgb(100, 116, 139)
        val subHeaderToDraw = TextUtils.ellipsize(
            subHeaderText,
            TextPaint(paint),
            maxCenterTextWidth,
            TextUtils.TruncateAt.END
        ).toString()
        canvas.drawText(subHeaderToDraw, width / 2f, y, paint)

        y = maxOf(y, topY + logoBoxSize) + 7f
        // Garis Pembatas Kop Surat Ganda
        paint.strokeWidth = 2f
        paint.color = Color.rgb(15, 118, 110)
        canvas.drawLine(marginX, y, width - marginX, y, paint)

        y += 3f
        paint.strokeWidth = 0.8f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawLine(marginX, y, width - marginX, y, paint)

        paint.textAlign = Paint.Align.LEFT
        return y + 14f
    }

    private fun drawTitleAndPeriod(
        canvas: Canvas,
        paint: Paint,
        width: Int,
        startY: Float,
        period: String,
        language: AppLanguage = AppLanguage.INDONESIAN
    ): Float {
        val isEn = language == AppLanguage.ENGLISH
        var y = startY
        paint.textAlign = Paint.Align.CENTER

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        paint.color = Color.rgb(15, 23, 42)
        val docTitle = if (isEn) {
            "STUDENT SCREENING & MEDICAL RECORD RECAP SHEET"
        } else {
            "LEMBAR REKAPITULASI SCREENING & REKAM MEDIS ANAK ASUH"
        }
        canvas.drawText(docTitle, width / 2f, y, paint)

        y += 13f
        paint.textSize = 10f
        paint.color = Color.rgb(2, 132, 199)
        val periodPrefix = if (isEn) "REPORT PERIOD" else "PERIODE LAPORAN"
        canvas.drawText("$periodPrefix: ${period.uppercase(language.locale)}", width / 2f, y, paint)

        paint.textAlign = Paint.Align.LEFT
        return y + 14f
    }

    internal fun drawStudentBio(
        canvas: Canvas,
        paint: Paint,
        width: Int,
        startY: Float,
        student: ChildStudent,
        isEn: Boolean = false
    ): Float {
        val boxLeft = 35f
        val boxRight = width - 35f
        val boxHeight = 62f
        val boxRect = RectF(boxLeft, startY, boxRight, startY + boxHeight)

        // Background box
        paint.color = Color.rgb(248, 250, 252)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(boxRect, 6f, 6f, paint)

        // Border box
        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(boxRect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        var y = startY + 14f

        // Kolom Kiri
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(if (isEn) "Student Name :" else "Nama Anak Asuh :", boxLeft + 10f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(15, 118, 110)
        canvas.drawText(student.name, boxLeft + 95f, y, paint)

        // Kolom Kanan
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(if (isEn) "Asrama :" else "Asrama Santri :", width / 2f + 10f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(student.dormitory, width / 2f + 85f, y, paint)

        y += 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(if (isEn) "NIS / Student ID : ${student.nis}" else "NIS / ID Siswa   : ${student.nis}", boxLeft + 10f, y, paint)
        canvas.drawText(if (isEn) "Class / Grade   : ${student.classGrade}" else "Kelas Pendidikan: ${student.classGrade}", width / 2f + 10f, y, paint)

        y += 14f
        val genderStr = if (student.gender.equals("P", ignoreCase = true)) {
            if (isEn) "Female (Putri)" else "Perempuan (Putri)"
        } else {
            if (isEn) "Male (Putra)" else "Laki-laki (Putra)"
        }
        canvas.drawText(if (isEn) "Gender           : $genderStr" else "Jenis Kelamin     : $genderStr", boxLeft + 10f, y, paint)
        val bloodAgeStr = if (isEn) {
            "Blood Type     : ${student.bloodType}  |  Age: ${student.age} Yrs"
        } else {
            "Golongan Darah : ${student.bloodType}  |  Usia: ${student.age} Th"
        }
        canvas.drawText(bloodAgeStr, width / 2f + 10f, y, paint)

        return startY + boxHeight + 12f
    }

    private fun drawScreeningSummary(
        canvas: Canvas,
        paint: Paint,
        width: Int,
        startY: Float,
        records: List<MedicalRecord>,
        period: String,
        isEn: Boolean = false
    ): Float {
        val boxLeft = 35f
        val boxRight = width - 35f
        val boxHeight = 36f
        val boxRect = RectF(boxLeft, startY, boxRight, startY + boxHeight)

        val isMultiSickness = records.size >= 2
        paint.color = if (isMultiSickness) Color.rgb(254, 242, 242) else Color.rgb(240, 253, 244)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(boxRect, 6f, 6f, paint)

        paint.color = if (isMultiSickness) Color.rgb(254, 202, 202) else Color.rgb(187, 247, 208)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(boxRect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        var y = startY + 14f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        paint.color = if (isMultiSickness) Color.rgb(185, 28, 28) else Color.rgb(21, 128, 61)
        val alertText = if (isEn) {
            when {
                isMultiSickness -> "ATTENTION: This student was recorded ${records.size} TIMES SICK / TREATED in $period."
                records.size == 1 -> "STATUS: Recorded 1 medical visit in $period."
                else -> "STATUS: Student is HEALTHY (0 medical visits) in $period."
            }
        } else {
            when {
                isMultiSickness -> "PERHATIAN: Anak asuh ini tercatat ${records.size} KALI SAKIT / BEROBAT dalam periode $period."
                records.size == 1 -> "STATUS: Tercatat 1 kali kunjungan pengobatan dalam periode $period."
                else -> "STATUS: Anak asuh dalam kondisi SEHAT (0 kali berobat) dalam periode $period."
            }
        }
        canvas.drawText(alertText, boxLeft + 10f, y, paint)

        y += 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(71, 85, 105)
        val controlCount = records.count { it.needsControl }
        val subLine = if (isEn) {
            "Total Linked Screening Records: ${records.size} Entries  •  Follow-ups Issued: $controlCount"
        } else {
            "Total Riwayat Screening Terdaftar: ${records.size} Data  •  Jadwal Kontrol Terbit: $controlCount Kali"
        }
        canvas.drawText(subLine, boxLeft + 10f, y, paint)

        return startY + boxHeight + 14f
    }

    private fun drawRecordItem(
        canvas: Canvas,
        paint: Paint,
        width: Int,
        startY: Float,
        number: Int,
        record: MedicalRecord,
        language: AppLanguage = AppLanguage.INDONESIAN
    ): Float {
        val isEn = language == AppLanguage.ENGLISH
        val boxLeft = 35f
        val boxRight = width - 35f
        val boxHeight = 78f
        val boxRect = RectF(boxLeft, startY, boxRight, startY + boxHeight)

        // Box container
        paint.color = Color.rgb(255, 255, 255)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(boxRect, 6f, 6f, paint)

        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(boxRect, 6f, 6f, paint)

        // Bar header item
        paint.style = Paint.Style.FILL
        val dateStr = record.treatmentDate?.let { DateUtils.formatFullDate(it, language) }
            ?: DateUtils.formatFullDate(record.planDate, language)

        var y = startY + 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        paint.color = Color.rgb(15, 118, 110)
        val visitLabel = if (isEn) "Visit" else "Kunjungan"
        canvas.drawText("$visitLabel #$number  •  $dateStr (${record.departureTime} WIB)", boxLeft + 10f, y, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Faskes: ${record.faskes}", boxRight - 10f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        // Garis pemisah tipis
        y += 4f
        paint.color = Color.rgb(241, 245, 249)
        paint.strokeWidth = 0.8f
        canvas.drawLine(boxLeft + 10f, y, boxRight - 10f, y, paint)

        y += 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(51, 65, 85)
        canvas.drawText(
            if (isEn) "• Initial Complaint : ${record.complaint}" else "• Keluhan Awal   : ${record.complaint}",
            boxLeft + 10f,
            y,
            paint
        )

        y += 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(2, 132, 199)
        val defaultDiag = if (isEn) "General Puskesmas checkup" else "Pemeriksaan umum puskesmas"
        canvas.drawText(
            if (isEn) "• Doctor Diagnosis  : ${record.diagnosis.ifBlank { defaultDiag }}" else "• Diagnosa Dokter: ${record.diagnosis.ifBlank { defaultDiag }}",
            boxLeft + 10f,
            y,
            paint
        )

        y += 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(51, 65, 85)
        val defaultAction = if (isEn) "Examination and observation" else "Pemeriksaan dan observasi"
        canvas.drawText(
            if (isEn) "• Medical Action    : ${record.actionsTaken.ifBlank { defaultAction }}" else "• Tindakan Medis: ${record.actionsTaken.ifBlank { defaultAction }}",
            boxLeft + 10f,
            y,
            paint
        )

        y += 11f
        paint.color = Color.rgb(109, 40, 217)
        val defaultPresc = if (isEn) "No special prescription" else "Tidak ada resep obat khusus"
        canvas.drawText(
            if (isEn) "• Prescription / Med: ${record.prescription.ifBlank { defaultPresc }}" else "• Resep / Obat    : ${record.prescription.ifBlank { defaultPresc }}",
            boxLeft + 10f,
            y,
            paint
        )

        y += 11f
        paint.color = if (record.needsControl) Color.rgb(180, 83, 9) else Color.rgb(100, 116, 139)
        val controlInfo = if (record.needsControl) {
            val ctrlDateStr = record.controlDate?.let { DateUtils.formatShortDate(it, language) }
                ?: if (isEn) "Scheduled" else "Terjadwal"
            val ctrlState = if (record.isControlCompleted) {
                if (isEn) "Done" else "Selesai"
            } else {
                if (isEn) "Pending" else "Belum Kontrol"
            }
            if (isEn) "Follow-up: $ctrlDateStr ($ctrlState)" else "Kontrol Ulang: $ctrlDateStr ($ctrlState)"
        } else {
            if (isEn) "Follow-up: Not Needed" else "Kontrol: Tidak Perlu Kontrol"
        }
        val escortInfo = if (record.escortName.isNotBlank()) {
            if (isEn) " | Escort: ${record.escortName}" else " | Pendamping: ${record.escortName}"
        } else ""
        canvas.drawText(
            if (isEn) "• Follow-up Status  : $controlInfo$escortInfo" else "• Status Kontrol  : $controlInfo$escortInfo",
            boxLeft + 10f,
            y,
            paint
        )

        return startY + boxHeight
    }

    internal fun drawFooterSignatures(
        context: Context,
        canvas: Canvas,
        paint: Paint,
        width: Int,
        height: Int,
        startY: Float,
        waliName: String,
        schoolProfile: SchoolProfile = SchoolProfile()
    ) {
        val lang = AppLanguageManager.getLanguage(context)
        val localizedContext = AppLanguageManager.createLocalizedContext(context, lang)
        val y = (startY + 15f).coerceAtMost(height - 122f)
        val datePattern = if (lang == AppLanguage.ENGLISH) "MMMM d, yyyy" else "d MMMM yyyy"
        val todayDate = SimpleDateFormat(datePattern, lang.locale).format(Date())
        val cityLabel = schoolProfile.city.trim().ifBlank { "Indonesia" }
        val effectiveWaliName = waliName.trim().ifBlank { "Wali Asuh" }
        val rightX = width - 50f

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(71, 85, 105)

        // Satu Entitas Kanan: Tanggal, Wali Asuh <Nama Sekolah>, Barcode QR Nama Penginput, Nama Wali Asuh
        canvas.drawText("$cityLabel, $todayDate", rightX, y, paint)

        val footerSignTitle = buildFooterSignatureTitle(schoolProfile)
        paint.textSize = if (footerSignTitle.length > 44) 7.5f else 8.5f
        canvas.drawText(footerSignTitle, rightX, y + 12f, paint)

        val qrSize = 54f
        val qrRight = rightX
        val qrLeft = qrRight - qrSize
        val qrTop = y + 18f
        val qrBottom = qrTop + qrSize

        val qrBitmap = StudentCardRenderer.generateQrBitmap(effectiveWaliName, 256)
        if (qrBitmap != null) {
            val frameRect = RectF(qrLeft - 2f, qrTop - 2f, qrRight + 2f, qrBottom + 2f)
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(frameRect, 4f, 4f, paint)

            paint.color = Color.rgb(203, 213, 225)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawRoundRect(frameRect, 4f, 4f, paint)
            paint.style = Paint.Style.FILL

            val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(qrBitmap, null, RectF(qrLeft, qrTop, qrRight, qrBottom), bmpPaint)
            qrBitmap.recycle()
        }

        val nameY = qrBottom + 13f
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(15, 118, 110)
        canvas.drawText(effectiveWaliName, rightX, nameY, paint)

        // Catatan kaki halus di bagian paling bawah lembar A4
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.textSize = 6.5f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText(
            localizedContext.getString(R.string.disclaimer_pdf_footer),
            width / 2f,
            height - 15f,
            paint
        )
        paint.textAlign = Paint.Align.LEFT
    }

    /**
     * Membuat dokumen PDF resmi Rekapitulasi Kesehatan Bulanan Seluruh Siswa (Laporan Agregat).
     */
    fun generateMonthlyAggregatePdf(
        context: Context,
        records: List<MedicalRecord>,
        totalStudentsCount: Int,
        monthYearLabel: String,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? {
        try {
            val lang = AppLanguageManager.getLanguage(context)
            val isEn = lang == AppLanguage.ENGLISH
            val document = PdfDocument()
            val pageWidth = 595 // Standard A4 width
            val pageHeight = 842 // Standard A4 height

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            var currentY = 35f

            // 1. Header / Kop Surat
            currentY = drawHeader(context, canvas, paint, pageWidth, currentY, schoolProfile)

            // 2. Judul Dokumen
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 12f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(
                if (isEn) "MONTHLY STUDENT HEALTH RECAPITULATION REPORT" else "LAPORAN REKAPITULASI KESEHATAN ANAK ASUH BULANAN",
                pageWidth / 2f,
                currentY,
                paint
            )

            currentY += 13f
            paint.textSize = 10f
            paint.color = Color.rgb(2, 132, 199)
            val periodPrefix = if (isEn) "MONTH PERIOD" else "PERIODE BULAN"
            canvas.drawText("$periodPrefix: ${monthYearLabel.uppercase(lang.locale)}", pageWidth / 2f, currentY, paint)
            paint.textAlign = Paint.Align.LEFT
            currentY += 16f

            // 3. Ringkasan Eksekutif (Card Box)
            val boxLeft = 35f
            val boxRight = pageWidth - 35f
            val boxHeight = 75f
            val boxRect = RectF(boxLeft, currentY, boxRight, currentY + boxHeight)

            paint.color = Color.rgb(240, 253, 250)
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(boxRect, 6f, 6f, paint)

            paint.color = Color.rgb(15, 118, 110)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(boxRect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            val uniqueSick = records.map { it.studentId }.distinct().size
            val maleCount = records.count { it.studentDormitory.contains("Putra", ignoreCase = true) }
            val femaleCount = records.count { it.studentDormitory.contains("Putri", ignoreCase = true) }
            val tuntasCount = records.count { !it.needsControl }
            val kontrolCount = records.count { it.needsControl }
            val faskesList = schoolProfile.getAllFaskes()
            val faskesSummaryText = if (faskesList.isNotEmpty()) {
                val topTwo = faskesList.take(2).joinToString(" | ") { fName ->
                    val cnt = records.count { it.faskes.equals(fName, ignoreCase = true) || it.faskes.contains(fName, ignoreCase = true) }
                    val shortF = fName.replace("Puskesmas ", "Pusk. ")
                    "$cnt $shortF"
                }
                "• Faskes: $topTwo"
            } else {
                if (isEn) "• Referrals: ${records.size}" else "• Total Rujukan: ${records.size} Kali"
            }
            val morbiditasPct = if (totalStudentsCount > 0) (uniqueSick.toFloat() / totalStudentsCount * 100).toInt() else 0

            var statY = currentY + 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.5f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(
                if (isEn) "MONTHLY EXECUTIVE SUMMARY:" else "RINGKASAN EKSEKUTIF BULANAN:",
                boxLeft + 10f,
                statY,
                paint
            )

            statY += 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.color = Color.rgb(51, 65, 85)
            if (isEn) {
                canvas.drawText("• Total Population: $totalStudentsCount Students", boxLeft + 10f, statY, paint)
                canvas.drawText("• Total Visits: ${records.size} Times", boxLeft + 175f, statY, paint)
                canvas.drawText("• Sick Students: $uniqueSick ($morbiditasPct%)", boxLeft + 350f, statY, paint)
                statY += 12f
                canvas.drawText("• Asrama Breakdown: $maleCount Putra | $femaleCount Putri", boxLeft + 10f, statY, paint)
                canvas.drawText("• Status: $tuntasCount Done | $kontrolCount Control", boxLeft + 175f, statY, paint)
                canvas.drawText(faskesSummaryText, boxLeft + 350f, statY, paint)
            } else {
                canvas.drawText("• Total Populasi Anak: $totalStudentsCount Anak", boxLeft + 10f, statY, paint)
                canvas.drawText("• Total Kunjungan Berobat: ${records.size} Kali", boxLeft + 175f, statY, paint)
                canvas.drawText("• Jumlah Anak Sakit: $uniqueSick Anak ($morbiditasPct%)", boxLeft + 350f, statY, paint)
                statY += 12f
                canvas.drawText("• Distribusi Asrama: $maleCount Putra | $femaleCount Putri", boxLeft + 10f, statY, paint)
                canvas.drawText("• Status: $tuntasCount Tuntas | $kontrolCount Perlu Kontrol", boxLeft + 175f, statY, paint)
                canvas.drawText(faskesSummaryText, boxLeft + 350f, statY, paint)
            }

            currentY += boxHeight + 14f

            // 4. Daftar Kasus Sakit
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10.5f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(
                if (isEn) "MEDICAL RECORDS & VISITS LIST (${records.size} Cases):" else "DAFTAR REKAM MEDIS & KUNJUNGAN BEROBAT (${records.size} Kasus):",
                35f,
                currentY,
                paint
            )
            currentY += 14f

            if (records.isEmpty()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 9.5f
                paint.color = Color.rgb(100, 116, 139)
                canvas.drawText(
                    if (isEn) "No illness / treatment records in this month (All Healthy)." else "Tidak ada riwayat sakit / berobat pada periode bulan ini (Kondisi Nihil/Sehat).",
                    40f,
                    currentY + 12f,
                    paint
                )
                currentY += 30f
            } else {
                for ((index, record) in records.withIndex()) {
                    if (currentY > pageHeight - 120f) {
                        document.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = 40f

                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.textSize = 9f
                        paint.color = Color.rgb(100, 116, 139)
                        val contMonthly = if (isEn) {
                            "Continued Monthly Health Report - $monthYearLabel (Page $pageNumber)"
                        } else {
                            "Lanjutan Laporan Bulanan Kesehatan - $monthYearLabel (Halaman $pageNumber)"
                        }
                        canvas.drawText(contMonthly, 35f, currentY, paint)
                        currentY += 16f
                    }

                    currentY = drawRecordItem(canvas, paint, pageWidth, currentY, index + 1, record, lang)
                    currentY += 8f
                }
            }

            // 5. Tanda Tangan
            if (currentY > pageHeight - 140f) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = 40f
            }

            drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
            document.finishPage(page)

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanPeriod = monthYearLabel.replace(Regex("[^a-zA-Z0-9]"), "_")
            val fileName = "Laporan_Bulanan_Kesehatan_${cleanPeriod}.pdf"
            val file = File(outputDir, fileName)

            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Membuka dokumen PDF hasil cetak menggunakan PDF viewer bawaan Android atau dialog print.
     */
    fun openPdfFile(context: Context, pdfFile: File) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, if (isEn) "Open / Print PDF Recap" else "Buka / Cetak Rekap PDF")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                if (isEn) "No PDF viewer app found: ${e.message}" else "Tidak ditemukan aplikasi pembuka PDF: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Membagikan file dokumen PDF langsung via WhatsApp, Google Drive, Email, dll.
     */
    fun sharePdfFile(
        context: Context,
        pdfFile: File,
        studentName: String,
        schoolName: String = "Wali Asuh Care"
    ) {
        val s = AppLanguageManager.getStrings(context).report
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
            val cleanSchool = schoolName.ifBlank { "Wali Asuh Care" }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    if (isEn) "Health Screening Recap - $studentName" else "Rekap Screening Kesehatan - $studentName"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    if (isEn) {
                        "Official Health Screening Recapitulation document for $studentName ($cleanSchool)."
                    } else {
                        "Dokumen resmi Rekapitulasi Screening Kesehatan Anak Asuh $studentName ($cleanSchool)."
                    }
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, s.pdfPassportChooserTitle))
        } catch (e: Exception) {
            Toast.makeText(context, s.pdfErrorToast(e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    fun shareDocumentPdf(
        context: Context,
        pdfFile: File,
        subject: String,
        text: String,
        chooserTitle: String? = null
    ) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        val s = AppLanguageManager.getStrings(context).report
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    chooserTitle ?: if (isEn) "Share PDF Report" else "Bagikan Laporan PDF"
                )
            )
        } catch (e: Exception) {
            Toast.makeText(context, s.pdfErrorToast(e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Membuat dokumen PDF resmi Laporan Harian Siswi Sedang Haid (Uzur Ibadah)
     * lengkap dengan kop surat, logo Kemensos & Sekolah Rakyat, serta profil sekolah multi-tenant.
     */
    fun generateDailyHaidReportPdf(
        context: Context,
        activeHaidRecords: List<HaidRecord>,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile(),
        reportDateMillis: Long = System.currentTimeMillis()
    ): File? {
        try {
            val lang = AppLanguageManager.getLanguage(context)
            val isEn = lang == AppLanguage.ENGLISH
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            val fullPattern = if (isEn) "EEEE, MMMM d, yyyy" else "EEEE, d MMMM yyyy"
            val fullDateLabel = SimpleDateFormat(fullPattern, lang.locale).format(Date(reportDateMillis))
            val shortDateFmt = SimpleDateFormat("dd MMM yyyy", lang.locale)

            var currentY = 35f

            // 1. Kop Surat Resmi Tenant Sekolah (Logo Kemensos Kiri & Sekolah Rakyat Kanan)
            currentY = drawHeader(context, canvas, paint, pageWidth, currentY, schoolProfile)

            // 2. Judul Laporan & Tanggal Hari Ini
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 13f
            paint.color = Color.rgb(157, 23, 77) // Rose 800
            canvas.drawText(
                if (isEn) "DAILY FEMALE STUDENT HAID REPORT" else "LAPORAN HARIAN SISWI SEDANG HAID (UZUR IBADAH)",
                pageWidth / 2f,
                currentY + 12f,
                paint
            )
            currentY += 27f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10f
            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText(
                if (isEn) "Day / Date: $fullDateLabel" else "Hari / Tanggal: $fullDateLabel",
                pageWidth / 2f,
                currentY,
                paint
            )
            currentY += 16f
            paint.textAlign = Paint.Align.LEFT

            // 3. Kotak Ringkasan Harian
            val maxDayCount = activeHaidRecords.count { it.isIstihadhahWarning }
            val boxLeft = 35f
            val boxRight = pageWidth - 35f
            val boxTop = currentY
            val summaryHeight = 44f
            paint.color = Color.rgb(253, 242, 248) // Rose 50
            canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + summaryHeight), 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(251, 207, 232) // Rose 200
            paint.strokeWidth = 1f
            canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + summaryHeight), 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.5f
            paint.color = Color.rgb(131, 24, 67)
            canvas.drawText(if (isEn) "DAILY SUMMARY:" else "RINGKASAN HARIAN:", boxLeft + 10f, boxTop + 15f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 9f
            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText(
                if (isEn) "• Total Active Haid: ${activeHaidRecords.size} Students" else "• Total Sedang Haid: ${activeHaidRecords.size} Siswi",
                boxLeft + 10f,
                boxTop + 31f,
                paint
            )
            canvas.drawText(
                if (isEn) "• 15-Day Limit: $maxDayCount Students" else "• Batas Hari ke-15: $maxDayCount Siswi",
                boxLeft + 195f,
                boxTop + 31f,
                paint
            )
            val pencetak = TextUtils.ellipsize(
                if (isEn) "Officer: $waliName" else "Petugas: $waliName",
                TextPaint(paint),
                170f,
                TextUtils.TruncateAt.END
            ).toString()
            canvas.drawText(pencetak, boxLeft + 355f, boxTop + 31f, paint)

            currentY = boxTop + summaryHeight + 14f

            // 4. Tabel Daftar Siswi Sedang Haid
            val sortedRecords = activeHaidRecords.sortedWith(
                compareBy<HaidRecord> { it.studentClass.lowercase(lang.locale) }
                    .thenBy { it.studentName.lowercase(lang.locale) }
            )

            fun drawHaidTableHeader(startYPos: Float): Float {
                val hTop = startYPos
                val hHeight = 24f
                paint.color = Color.rgb(252, 231, 243) // Rose 100
                canvas.drawRect(boxLeft, hTop, boxRight, hTop + hHeight, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(244, 114, 182)
                paint.strokeWidth = 0.9f
                canvas.drawRect(boxLeft, hTop, boxRight, hTop + hHeight, paint)
                paint.style = Paint.Style.FILL

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.5f
                paint.color = Color.rgb(131, 24, 67)
                val textY = hTop + 15.5f
                canvas.drawText("No", 42f, textY, paint)
                canvas.drawText(if (isEn) "Student Name" else "Nama Siswi", 68f, textY, paint)
                canvas.drawText(if (isEn) "Class / Asrama" else "Kelas / Asrama", 222f, textY, paint)
                canvas.drawText(if (isEn) "Haid Start" else "Mulai Haid", 332f, textY, paint)
                canvas.drawText(if (isEn) "Duration" else "Waktu Berjalan", 404f, textY, paint)
                canvas.drawText(if (isEn) "Wali / Notes" else "Wali / Catatan", 482f, textY, paint)
                return hTop + hHeight
            }

            if (sortedRecords.isEmpty()) {
                val emptyBoxTop = currentY
                val emptyBoxHeight = 46f
                paint.color = Color.rgb(240, 253, 244)
                canvas.drawRoundRect(RectF(boxLeft, emptyBoxTop, boxRight, emptyBoxTop + emptyBoxHeight), 6f, 6f, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(187, 247, 208)
                paint.strokeWidth = 1f
                canvas.drawRoundRect(RectF(boxLeft, emptyBoxTop, boxRight, emptyBoxTop + emptyBoxHeight), 6f, 6f, paint)
                paint.style = Paint.Style.FILL

                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 10f
                paint.color = Color.rgb(21, 128, 61)
                canvas.drawText(
                    if (isEn) {
                        "None — No female students recorded on Haid today (All Suci)."
                    } else {
                        "Nihil — Tidak ada siswi yang tercatat sedang haid pada hari ini (Semua Suci)."
                    },
                    pageWidth / 2f,
                    emptyBoxTop + 27f,
                    paint
                )
                paint.textAlign = Paint.Align.LEFT
                currentY = emptyBoxTop + emptyBoxHeight + 16f
            } else {
                currentY = drawHaidTableHeader(currentY)
                val rowHeight = 24f

                sortedRecords.forEachIndexed { idx, rec ->
                    if (currentY > pageHeight - 125f) {
                        document.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = 40f

                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.textSize = 9f
                        paint.color = Color.rgb(100, 116, 139)
                        canvas.drawText(
                            if (isEn) {
                                "Continued Daily Haid Report — $fullDateLabel (Page $pageNumber)"
                            } else {
                                "Lanjutan Laporan Harian Siswi Sedang Haid — $fullDateLabel (Halaman $pageNumber)"
                            },
                            35f,
                            currentY,
                            paint
                        )
                        currentY += 12f
                        currentY = drawHaidTableHeader(currentY)
                    }

                    val rTop = currentY
                    paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                    canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowHeight, paint)
                    paint.style = Paint.Style.STROKE
                    paint.color = Color.rgb(226, 232, 240)
                    paint.strokeWidth = 0.6f
                    canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowHeight, paint)
                    paint.style = Paint.Style.FILL

                    val textY = rTop + 15f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 8.5f
                    paint.color = Color.rgb(15, 23, 42)
                    canvas.drawText("${idx + 1}.", 42f, textY, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    val nameCell = TextUtils.ellipsize(rec.studentName, TextPaint(paint), 148f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(nameCell, 68f, textY, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.rgb(51, 65, 85)
                    val classDorm = listOf(rec.studentClass.trim(), rec.studentDorm.trim())
                        .filter { it.isNotBlank() }
                        .joinToString(" • ")
                        .ifBlank { "-" }
                    val classCell = TextUtils.ellipsize(classDorm, TextPaint(paint), 104f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(classCell, 222f, textY, paint)

                    val startCell = shortDateFmt.format(Date(rec.startDate))
                    canvas.drawText(startCell, 332f, textY, paint)

                    val days = rec.durationDays
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = when {
                        rec.isIstihadhahWarning -> Color.rgb(185, 28, 28)
                        days >= 12 -> Color.rgb(180, 83, 9)
                        else -> Color.rgb(190, 24, 93)
                    }
                    val dayText = if (isEn) {
                        if (rec.isIstihadhahWarning) "Day $days (Max)" else "Day $days / 15"
                    } else {
                        if (rec.isIstihadhahWarning) "Hari ke-$days (Maks)" else "Hari ke-$days / 15"
                    }
                    canvas.drawText(dayText, 404f, textY, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.rgb(71, 85, 105)
                    val noteOrWali = rec.notes.trim().ifBlank { rec.waliName.trim() }.ifBlank { "-" }
                    val noteCell = TextUtils.ellipsize(noteOrWali, TextPaint(paint), 74f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(noteCell, 482f, textY, paint)

                    currentY = rTop + rowHeight
                }
                currentY += 14f
            }

            // 5. Tanda Tangan & Disclaimer Footer
            if (currentY > pageHeight - 140f) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = 40f
            }

            drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
            document.finishPage(page)

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val dateSlug = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(reportDateMillis))
            val tenantSlug = schoolProfile.tenantCode.replace(Regex("[^a-zA-Z0-9]"), "_").ifBlank { "Sekolah" }
            val fileName = "Laporan_Haid_Harian_${tenantSlug}_$dateSlug.pdf"
            val file = File(outputDir, fileName)

            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun buildDailyHaidSummaryText(
        activeHaidRecords: List<HaidRecord>,
        waliName: String,
        schoolProfile: SchoolProfile,
        reportDateMillis: Long = System.currentTimeMillis(),
        language: AppLanguage = AppLanguageManager.currentLanguage
    ): String {
        val isEn = language == AppLanguage.ENGLISH
        val fullPattern = if (isEn) "EEEE, MMMM d, yyyy" else "EEEE, d MMMM yyyy"
        val fullDateLabel = SimpleDateFormat(fullPattern, language.locale).format(Date(reportDateMillis))
        val schoolTitle = schoolProfile.schoolName.trim().ifBlank { schoolProfile.shortName.ifBlank { "Sekolah Rakyat" } }
        val sorted = activeHaidRecords.sortedWith(
            compareBy<HaidRecord> { it.studentClass.lowercase(language.locale) }
                .thenBy { it.studentName.lowercase(language.locale) }
        )

        return buildString {
            appendLine(if (isEn) "*DAILY FEMALE STUDENT HAID REPORT*" else "*LAPORAN HARIAN SISWI SEDANG HAID*")
            appendLine("*$schoolTitle*")
            appendLine(if (isEn) "Date: $fullDateLabel" else "Tanggal: $fullDateLabel")
            appendLine(if (isEn) "Total: ${sorted.size} Students" else "Total: ${sorted.size} Siswi")
            appendLine()
            if (sorted.isEmpty()) {
                appendLine(if (isEn) "_None — All female students are Suci._" else "_Nihil — Seluruh siswi dalam status Suci._")
            } else {
                sorted.forEachIndexed { idx, rec ->
                    val classLabel = rec.studentClass.ifBlank { rec.studentDorm }
                    val classSuffix = if (classLabel.isNotBlank()) " ($classLabel)" else ""
                    val warn = if (rec.isIstihadhahWarning) {
                        if (isEn) " ⚠️ Day 15" else " ⚠️ Hari ke-15"
                    } else ""
                    val dayStr = if (isEn) "Day ${rec.durationDays}/15" else "Hari ke-${rec.durationDays}/15"
                    appendLine("${idx + 1}. ${rec.studentName}$classSuffix — $dayStr$warn")
                }
            }
            appendLine()
            append(if (isEn) "Reported by: $waliName" else "Dilaporkan oleh: $waliName")
        }
    }

    fun shareHaidReportPdf(
        context: Context,
        pdfFile: File,
        schoolProfile: SchoolProfile,
        reportDateMillis: Long = System.currentTimeMillis()
    ) {
        val s = AppLanguageManager.getStrings(context).report
        val lang = AppLanguageManager.getLanguage(context)
        val isEn = lang == AppLanguage.ENGLISH
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
            val datePattern = if (isEn) "MMMM d, yyyy" else "d MMMM yyyy"
            val dateLabel = SimpleDateFormat(datePattern, lang.locale).format(Date(reportDateMillis))
            val cleanSchool = schoolProfile.schoolName.ifBlank { "Wali Asuh Care" }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    if (isEn) "Daily Haid Report ($dateLabel) - $cleanSchool" else "Laporan Haid Harian ($dateLabel) - $cleanSchool"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    if (isEn) {
                        "Attached is the Daily Female Student Haid Report ($dateLabel) for $cleanSchool."
                    } else {
                        "Terlampir Laporan Harian Siswi Sedang Haid ($dateLabel) untuk $cleanSchool."
                    }
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, s.pdfHaidChooserTitle))
        } catch (e: Exception) {
            Toast.makeText(context, s.pdfErrorToast(e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Membuat dokumen PDF resmi Laporan Daftar Peserta Puasa per Sesi (beserta rincian SD, SMP, SMA & Total).
     */
    fun generateFastingListPdf(
        context: Context,
        session: FastingSession,
        records: List<FastingRecord>,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? {
        try {
            val lang = AppLanguageManager.getLanguage(context)
            val isEn = lang == AppLanguage.ENGLISH
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            val fullPattern = if (isEn) "EEEE, MMMM d, yyyy" else "EEEE, d MMMM yyyy"
            val sessionDateLabel = SimpleDateFormat(fullPattern, lang.locale).format(Date(session.sessionDate))

            var currentY = 35f

            // 1. Kop Surat Resmi (Unit Bimbingan Spiritual)
            currentY = drawHeader(
                context = context,
                canvas = canvas,
                paint = paint,
                width = pageWidth,
                startY = currentY,
                schoolProfile = schoolProfile,
                unitTitle = UNIT_BIMBINGAN_SPIRITUAL
            )

            // 2. Judul Laporan & Info Sesi
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 13f
            paint.color = Color.rgb(15, 118, 110)
            canvas.drawText(
                if (isEn) "STUDENT PUASA PARTICIPANTS REPORT" else "LAPORAN DAFTAR PESERTA PUASA SISWA",
                pageWidth / 2f,
                currentY + 12f,
                paint
            )
            currentY += 26f

            paint.textSize = 10.5f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText("${session.title.uppercase(lang.locale)} — $sessionDateLabel", pageWidth / 2f, currentY, paint)
            currentY += 16f
            paint.textAlign = Paint.Align.LEFT

            // 3. Kotak Ringkasan Jumlah Per Jenjang (SD, SMP, SMA & Total)
            val sdCount = records.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SD }
            val smpCount = records.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SMP }
            val smaCount = records.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SMA }
            val otherCount = records.size - sdCount - smpCount - smaCount

            val boxLeft = 35f
            val boxRight = pageWidth - 35f
            val boxTop = currentY
            val summaryHeight = 46f
            paint.color = Color.rgb(240, 253, 250)
            canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + summaryHeight), 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(153, 246, 228)
            paint.strokeWidth = 1f
            canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + summaryHeight), 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.5f
            paint.color = Color.rgb(15, 118, 110)
            canvas.drawText(
                if (isEn) "PUASA PARTICIPANTS SUMMARY:" else "RINGKASAN PESERTA PUASA:",
                boxLeft + 10f,
                boxTop + 15f,
                paint
            )

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            paint.color = Color.rgb(15, 23, 42)
            val studentUnit = if (isEn) "Students" else "Anak"
            val breakdownText = buildString {
                append("• SMA: $smaCount $studentUnit   • SMP: $smpCount $studentUnit   • SD: $sdCount $studentUnit")
                if (otherCount > 0) append(if (isEn) "   • Other: $otherCount" else "   • Lainnya: $otherCount")
                append(if (isEn) "   |   TOTAL: ${records.size} STUDENTS" else "   |   TOTAL: ${records.size} SISWA")
            }
            canvas.drawText(breakdownText, boxLeft + 10f, boxTop + 33f, paint)

            currentY = boxTop + summaryHeight + 14f

            // 4. Tabel Daftar Siswa Berpuasa
            val sortedRecords = records.sortedWith(
                compareBy<FastingRecord> {
                    when (FastingJenjangHelper.resolveJenjang(it)) {
                        FastingJenjangHelper.JENJANG_SMA -> 0
                        FastingJenjangHelper.JENJANG_SMP -> 1
                        FastingJenjangHelper.JENJANG_SD -> 2
                        else -> 3
                    }
                }.thenBy { it.studentClass.lowercase(lang.locale) }
                    .thenBy { it.studentName.lowercase(lang.locale) }
            )

            fun drawTableHeader(startYPos: Float): Float {
                val hTop = startYPos
                val hHeight = 24f
                paint.color = Color.rgb(204, 251, 241)
                canvas.drawRect(boxLeft, hTop, boxRight, hTop + hHeight, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(45, 212, 191)
                paint.strokeWidth = 0.9f
                canvas.drawRect(boxLeft, hTop, boxRight, hTop + hHeight, paint)
                paint.style = Paint.Style.FILL

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.5f
                paint.color = Color.rgb(17, 94, 89)
                val textY = hTop + 15.5f
                canvas.drawText("No", 42f, textY, paint)
                canvas.drawText(if (isEn) "Student Name" else "Nama Siswa", 70f, textY, paint)
                canvas.drawText(if (isEn) "Level" else "Jenjang", 242f, textY, paint)
                canvas.drawText(if (isEn) "Class / Asrama" else "Kelas / Asrama", 300f, textY, paint)
                canvas.drawText(if (isEn) "Recorded By" else "Penginput", 448f, textY, paint)
                return hTop + hHeight
            }

            if (sortedRecords.isEmpty()) {
                val emptyTop = currentY
                val emptyH = 44f
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRoundRect(RectF(boxLeft, emptyTop, boxRight, emptyTop + emptyH), 6f, 6f, paint)
                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 9.5f
                paint.color = Color.rgb(100, 116, 139)
                canvas.drawText(
                    if (isEn) "No students registered for Puasa in this session yet." else "Belum ada siswa terdaftar berpuasa pada sesi ini.",
                    pageWidth / 2f,
                    emptyTop + 26f,
                    paint
                )
                paint.textAlign = Paint.Align.LEFT
                currentY = emptyTop + emptyH + 14f
            } else {
                currentY = drawTableHeader(currentY)
                val rowHeight = 22f
                sortedRecords.forEachIndexed { idx, rec ->
                    if (currentY > pageHeight - 125f) {
                        document.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = 40f

                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.textSize = 9f
                        paint.color = Color.rgb(100, 116, 139)
                        canvas.drawText(
                            if (isEn) "Continued Puasa List (${session.title}) - Page $pageNumber" else "Lanjutan Daftar Puasa (${session.title}) - Halaman $pageNumber",
                            35f,
                            currentY,
                            paint
                        )
                        currentY += 12f
                        currentY = drawTableHeader(currentY)
                    }

                    val rTop = currentY
                    paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                    canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowHeight, paint)
                    paint.style = Paint.Style.STROKE
                    paint.color = Color.rgb(226, 232, 240)
                    paint.strokeWidth = 0.6f
                    canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowHeight, paint)
                    paint.style = Paint.Style.FILL

                    val textY = rTop + 14.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 8.5f
                    paint.color = Color.rgb(15, 23, 42)
                    canvas.drawText("${idx + 1}.", 42f, textY, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    val nameCell = TextUtils.ellipsize(rec.studentName, TextPaint(paint), 165f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(nameCell, 70f, textY, paint)

                    val jenjang = FastingJenjangHelper.resolveJenjang(rec)
                    paint.color = when (jenjang) {
                        FastingJenjangHelper.JENJANG_SMA -> Color.rgb(109, 40, 217)
                        FastingJenjangHelper.JENJANG_SMP -> Color.rgb(3, 105, 161)
                        FastingJenjangHelper.JENJANG_SD -> Color.rgb(21, 128, 61)
                        else -> Color.rgb(71, 85, 105)
                    }
                    canvas.drawText(jenjang, 242f, textY, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.rgb(51, 65, 85)
                    val classDorm = listOf(rec.studentClass.trim(), rec.studentDorm.trim())
                        .filter { it.isNotBlank() }
                        .joinToString(" • ")
                        .ifBlank { "-" }
                    val classCell = TextUtils.ellipsize(classDorm, TextPaint(paint), 140f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(classCell, 300f, textY, paint)

                    val waliCell = TextUtils.ellipsize(rec.waliName.ifBlank { "-" }, TextPaint(paint), 105f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(waliCell, 448f, textY, paint)

                    currentY = rTop + rowHeight
                }
                currentY += 14f
            }

            if (currentY > pageHeight - 140f) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = 40f
            }

            drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
            document.finishPage(page)

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanTitle = session.title.replace(Regex("[^a-zA-Z0-9]"), "_").take(25)
            val dateSlug = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(session.sessionDate))
            val file = File(outputDir, "Daftar_Puasa_${cleanTitle}_$dateSlug.pdf")

            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Membuat dokumen PDF resmi Laporan Checklist Sahur & Berbuka Puasa Siswa per Sesi.
     */
    fun generateFastingChecklistPdf(
        context: Context,
        session: FastingSession,
        records: List<FastingRecord>,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? {
        try {
            val lang = AppLanguageManager.getLanguage(context)
            val isEn = lang == AppLanguage.ENGLISH
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            val fullPattern = if (isEn) "EEEE, MMMM d, yyyy" else "EEEE, d MMMM yyyy"
            val sessionDateLabel = SimpleDateFormat(fullPattern, lang.locale).format(Date(session.sessionDate))

            var currentY = 35f

            // 1. Kop Surat Resmi (Unit Bimbingan Spiritual)
            currentY = drawHeader(
                context = context,
                canvas = canvas,
                paint = paint,
                width = pageWidth,
                startY = currentY,
                schoolProfile = schoolProfile,
                unitTitle = UNIT_BIMBINGAN_SPIRITUAL
            )

            // 2. Judul Laporan
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 13f
            paint.color = Color.rgb(180, 83, 9)
            canvas.drawText(
                if (isEn) "SAHUR & BERBUKA PUASA CHECKLIST REPORT" else "LAPORAN CHECKLIST SAHUR & BERBUKA PUASA",
                pageWidth / 2f,
                currentY + 12f,
                paint
            )
            currentY += 26f

            paint.textSize = 10.5f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText("${session.title.uppercase(lang.locale)} — $sessionDateLabel", pageWidth / 2f, currentY, paint)
            currentY += 16f
            paint.textAlign = Paint.Align.LEFT

            // 3. Kotak Ringkasan Sahur & Berbuka
            val sahurDone = records.count { it.isSahurChecked }
            val bukaDone = records.count { it.isBukaChecked }
            val bothDone = records.count { it.isSahurChecked && it.isBukaChecked }

            val boxLeft = 35f
            val boxRight = pageWidth - 35f
            val boxTop = currentY
            val summaryHeight = 46f
            paint.color = Color.rgb(255, 251, 235)
            canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + summaryHeight), 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(253, 230, 138)
            paint.strokeWidth = 1f
            canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + summaryHeight), 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.5f
            paint.color = Color.rgb(146, 64, 14)
            canvas.drawText(
                if (isEn) "SAHUR & BERBUKA CHECKLIST SUMMARY:" else "REKAPITULASI CHECKLIST SAHUR & BERBUKA:",
                boxLeft + 10f,
                boxTop + 15f,
                paint
            )

            paint.textSize = 9f
            paint.color = Color.rgb(51, 65, 85)
            val statsLine = if (isEn) {
                "• Registered: ${records.size} Students   • Sahur: $sahurDone / ${records.size}   • Berbuka: $bukaDone / ${records.size}   • Complete: $bothDone"
            } else {
                "• Total Terdaftar: ${records.size} Siswa   • Sudah Sahur: $sahurDone / ${records.size}   • Sudah Berbuka: $bukaDone / ${records.size}   • Lengkap: $bothDone"
            }
            canvas.drawText(statsLine, boxLeft + 10f, boxTop + 33f, paint)
            currentY = boxTop + summaryHeight + 14f

            val sortedRecords = records.sortedWith(
                compareBy<FastingRecord> { FastingJenjangHelper.resolveJenjang(it) }
                    .thenBy { it.studentClass.lowercase(lang.locale) }
                    .thenBy { it.studentName.lowercase(lang.locale) }
            )

            fun drawChecklistHeader(startYPos: Float): Float {
                val hTop = startYPos
                val hHeight = 24f
                paint.color = Color.rgb(254, 243, 199)
                canvas.drawRect(boxLeft, hTop, boxRight, hTop + hHeight, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(245, 158, 11)
                paint.strokeWidth = 0.9f
                canvas.drawRect(boxLeft, hTop, boxRight, hTop + hHeight, paint)
                paint.style = Paint.Style.FILL

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.5f
                paint.color = Color.rgb(146, 64, 14)
                val textY = hTop + 15.5f
                canvas.drawText("No", 42f, textY, paint)
                canvas.drawText(if (isEn) "Student Name" else "Nama Siswa", 70f, textY, paint)
                canvas.drawText(if (isEn) "Level / Class" else "Jenjang / Kelas", 242f, textY, paint)
                canvas.drawText("Sahur", 386f, textY, paint)
                canvas.drawText("Berbuka", 470f, textY, paint)
                return hTop + hHeight
            }

            if (sortedRecords.isEmpty()) {
                val emptyTop = currentY
                val emptyH = 44f
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRoundRect(RectF(boxLeft, emptyTop, boxRight, emptyTop + emptyH), 6f, 6f, paint)
                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 9.5f
                paint.color = Color.rgb(100, 116, 139)
                canvas.drawText(
                    if (isEn) "No Puasa participants to check in this session yet." else "Belum ada peserta puasa untuk dichecklist pada sesi ini.",
                    pageWidth / 2f,
                    emptyTop + 26f,
                    paint
                )
                paint.textAlign = Paint.Align.LEFT
                currentY = emptyTop + emptyH + 14f
            } else {
                currentY = drawChecklistHeader(currentY)
                val rowHeight = 22f
                val doneLabel = if (isEn) "✓ Checked" else "✓ Sudah"
                val pendingLabel = if (isEn) "Pending" else "Belum"
                sortedRecords.forEachIndexed { idx, rec ->
                    if (currentY > pageHeight - 125f) {
                        document.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = 40f

                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.textSize = 9f
                        paint.color = Color.rgb(100, 116, 139)
                        canvas.drawText(
                            if (isEn) "Continued Sahur & Berbuka Checklist (${session.title}) - Page $pageNumber" else "Lanjutan Checklist Sahur & Berbuka (${session.title}) - Halaman $pageNumber",
                            35f,
                            currentY,
                            paint
                        )
                        currentY += 12f
                        currentY = drawChecklistHeader(currentY)
                    }

                    val rTop = currentY
                    paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                    canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowHeight, paint)
                    paint.style = Paint.Style.STROKE
                    paint.color = Color.rgb(226, 232, 240)
                    paint.strokeWidth = 0.6f
                    canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowHeight, paint)
                    paint.style = Paint.Style.FILL

                    val textY = rTop + 14.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 8.5f
                    paint.color = Color.rgb(15, 23, 42)
                    canvas.drawText("${idx + 1}.", 42f, textY, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    val nameCell = TextUtils.ellipsize(rec.studentName, TextPaint(paint), 165f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(nameCell, 70f, textY, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.rgb(51, 65, 85)
                    val jenjang = FastingJenjangHelper.resolveJenjang(rec)
                    val classLabel = listOf(jenjang, rec.studentClass.trim())
                        .filter { it.isNotBlank() }
                        .joinToString(" • ")
                    val classCell = TextUtils.ellipsize(classLabel, TextPaint(paint), 134f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(classCell, 242f, textY, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = if (rec.isSahurChecked) Color.rgb(21, 128, 61) else Color.rgb(148, 163, 184)
                    canvas.drawText(if (rec.isSahurChecked) doneLabel else pendingLabel, 386f, textY, paint)

                    paint.color = if (rec.isBukaChecked) Color.rgb(21, 128, 61) else Color.rgb(148, 163, 184)
                    canvas.drawText(if (rec.isBukaChecked) doneLabel else pendingLabel, 470f, textY, paint)

                    currentY = rTop + rowHeight
                }
                currentY += 14f
            }

            if (currentY > pageHeight - 140f) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = 40f
            }

            drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
            document.finishPage(page)

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanTitle = session.title.replace(Regex("[^a-zA-Z0-9]"), "_").take(25)
            val dateSlug = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(session.sessionDate))
            val file = File(outputDir, "Checklist_Puasa_${cleanTitle}_$dateSlug.pdf")

            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun buildFastingListWaSummary(
        session: FastingSession,
        records: List<FastingRecord>,
        waliName: String,
        schoolProfile: SchoolProfile,
        language: AppLanguage = AppLanguageManager.currentLanguage
    ): String {
        val isEn = language == AppLanguage.ENGLISH
        val fullPattern = if (isEn) "EEEE, MMMM d, yyyy" else "EEEE, d MMMM yyyy"
        val dateLabel = SimpleDateFormat(fullPattern, language.locale).format(Date(session.sessionDate))
        val schoolTitle = schoolProfile.schoolName.trim().ifBlank { schoolProfile.shortName.ifBlank { "Wali Asuh Care" } }
        val sdCount = records.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SD }
        val smpCount = records.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SMP }
        val smaCount = records.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SMA }

        return buildString {
            appendLine(if (isEn) "*STUDENT PUASA PARTICIPANTS LIST*" else "*DAFTAR PESERTA PUASA SISWA*")
            appendLine("*$schoolTitle*")
            appendLine(if (isEn) "Session: *${session.title}*" else "Sesi: *${session.title}*")
            appendLine(if (isEn) "Date: $dateLabel" else "Tanggal: $dateLabel")
            appendLine()
            appendLine(if (isEn) "*Level Summary:*" else "*Rekap Jenjang:*")
            val unitWord = if (isEn) "Students" else "Anak"
            appendLine("• SMA: $smaCount $unitWord")
            appendLine("• SMP: $smpCount $unitWord")
            appendLine("• SD : $sdCount $unitWord")
            appendLine(if (isEn) "*Total Puasa Participants: ${records.size} Students*" else "*Total Berpuasa: ${records.size} Siswa*")
            appendLine()
            if (records.isNotEmpty()) {
                records.sortedBy { it.studentName }.forEachIndexed { idx, rec ->
                    val jenjang = FastingJenjangHelper.resolveJenjang(rec)
                    val kelas = rec.studentClass.ifBlank { rec.studentDorm }
                    appendLine("${idx + 1}. ${rec.studentName} ($jenjang${if (kelas.isNotBlank()) " - $kelas" else ""})")
                }
                appendLine()
            }
            append(if (isEn) "Reported by: $waliName" else "Dilaporkan oleh: $waliName")
        }
    }

    fun buildFastingChecklistWaSummary(
        session: FastingSession,
        records: List<FastingRecord>,
        waliName: String,
        schoolProfile: SchoolProfile,
        language: AppLanguage = AppLanguageManager.currentLanguage
    ): String {
        val isEn = language == AppLanguage.ENGLISH
        val fullPattern = if (isEn) "EEEE, MMMM d, yyyy" else "EEEE, d MMMM yyyy"
        val dateLabel = SimpleDateFormat(fullPattern, language.locale).format(Date(session.sessionDate))
        val schoolTitle = schoolProfile.schoolName.trim().ifBlank { schoolProfile.shortName.ifBlank { "Wali Asuh Care" } }
        val sahurCount = records.count { it.isSahurChecked }
        val bukaCount = records.count { it.isBukaChecked }

        return buildString {
            appendLine(if (isEn) "*SAHUR & BERBUKA PUASA CHECKLIST*" else "*CHECKLIST SAHUR & BERBUKA PUASA*")
            appendLine("*$schoolTitle*")
            appendLine(if (isEn) "Session: *${session.title}*" else "Sesi: *${session.title}*")
            appendLine(if (isEn) "Date: $dateLabel" else "Tanggal: $dateLabel")
            appendLine(if (isEn) "• Registered: ${records.size} Students" else "• Terdaftar: ${records.size} Siswa")
            appendLine(if (isEn) "• Sahur Checked: $sahurCount/${records.size}" else "• Sudah Sahur: $sahurCount/${records.size}")
            appendLine(if (isEn) "• Berbuka Checked: $bukaCount/${records.size}" else "• Sudah Berbuka: $bukaCount/${records.size}")
            appendLine()
            records.sortedBy { it.studentName }.forEachIndexed { idx, rec ->
                val sMark = if (rec.isSahurChecked) "✅" else "⬜"
                val bMark = if (rec.isBukaChecked) "✅" else "⬜"
                appendLine("${idx + 1}. ${rec.studentName} — Sahur: $sMark | Berbuka: $bMark")
            }
            appendLine()
            append(if (isEn) "Reported by: $waliName" else "Dilaporkan oleh: $waliName")
        }
    }

    fun shareFastingPdf(
        context: Context,
        pdfFile: File,
        subject: String,
        text: String
    ) {
        val s = AppLanguageManager.getStrings(context).report
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, s.pdfPuasaChooserTitle))
        } catch (e: Exception) {
            Toast.makeText(context, s.pdfErrorToast(e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    internal fun renderKartuMengajiPages(
        context: Context,
        student: ChildStudent,
        records: List<MengajiRecord>,
        periodLabel: String,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile(),
        pageWidth: Int = 595,
        pageHeight: Int = 842,
        startNewCanvas: (pageNumber: Int) -> Canvas,
        finishCurrentCanvas: () -> Unit = {}
    ) {
        val lang = AppLanguageManager.getLanguage(context)
        val isEn = lang == AppLanguage.ENGLISH

        var pageNumber = 1
        var canvas = startNewCanvas(pageNumber)

        val paint = Paint().apply { isAntiAlias = true }
        val dateFmt = SimpleDateFormat("d MMM yyyy", lang.locale)
        val orderedRecords = records.sortedWith(compareBy<MengajiRecord> { it.sessionDate }.thenBy { it.createdAt })

        var currentY = 35f

        // 1. Kop Surat Resmi (Unit Bimbingan Spiritual)
        currentY = drawHeader(
            context = context,
            canvas = canvas,
            paint = paint,
            width = pageWidth,
            startY = currentY,
            schoolProfile = schoolProfile,
            unitTitle = UNIT_BIMBINGAN_SPIRITUAL
        )

        // 2. Judul Kartu Mengaji & Periode
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13.5f
        paint.color = Color.rgb(15, 118, 110)
        canvas.drawText(
            if (isEn) "STUDENT QURAN / MENGAJI PROGRESS CARD" else "KARTU PRESTASI & KENDALI MENGAJI SISWA",
            pageWidth / 2f,
            currentY + 12f,
            paint
        )
        currentY += 26f

        paint.textSize = 10f
        paint.color = Color.rgb(15, 23, 42)
        val periodDisplay = if (isEn) "Period: $periodLabel" else "Periode: $periodLabel"
        canvas.drawText(periodDisplay.uppercase(lang.locale), pageWidth / 2f, currentY, paint)
        currentY += 14f
        paint.textAlign = Paint.Align.LEFT

        // 3. Kotak Identitas Siswa & Statistik Mengaji Bulan Ini
        val boxLeft = 35f
        val boxRight = pageWidth - 35f
        val boxTop = currentY
        val bioHeight = 76f
        paint.color = Color.rgb(240, 253, 250)
        canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + bioHeight), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(153, 246, 228)
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + bioHeight), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        val jenjangResolved = student.jenjang.ifBlank { FastingJenjangHelper.resolveJenjang(student) }
        val fluentCount = orderedRecords.count { MengajiHelper.isFluent(it.fluencyStatus) }
        val notFluentCount = orderedRecords.size - fluentCount
        val latestPos = orderedRecords.lastOrNull()?.let { MengajiHelper.formatCompactPosition(it) } ?: "-"

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(
            if (isEn) "Student: ${student.name}" else "Nama Siswa: ${student.name}",
            boxLeft + 10f,
            boxTop + 18f,
            paint
        )
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(51, 65, 85)
        canvas.drawText(
            "NIS: ${student.nis.ifBlank { "-" }}  |  ${if (isEn) "Level/Class" else "Jenjang/Kelas"}: $jenjangResolved • ${student.classGrade.ifBlank { "-" }}  |  Asrama: ${student.dormitory.ifBlank { "-" }}",
            boxLeft + 10f,
            boxTop + 34f,
            paint
        )
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(15, 118, 110)
        canvas.drawText(
            if (isEn) "Latest Position: $latestPos" else "Posisi Bacaan Terakhir: $latestPos",
            boxLeft + 10f,
            boxTop + 50f,
            paint
        )
        paint.textSize = 8.5f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(
            if (isEn) {
                "Sessions: ${orderedRecords.size}   |   Fluent (Lancar): $fluentCount   |   Needs Practice (Tidak Lancar): $notFluentCount"
            } else {
                "Total Setoran: ${orderedRecords.size} Kali   |   Lancar: $fluentCount   |   Tidak Lancar: $notFluentCount"
            },
            boxLeft + 10f,
            boxTop + 65f,
            paint
        )

        currentY = boxTop + bioHeight + 14f

        // Kolom tabel: No (30) | Tanggal (78) | Jilid / Kitab (122) | Halaman (95) | Keterangan (100) | Paraf Wali (100)
        val colNo = boxLeft
        val colTgl = boxLeft + 30f
        val colJilid = boxLeft + 108f
        val colHal = boxLeft + 230f
        val colKet = boxLeft + 325f
        val colWali = boxLeft + 425f

        fun drawKartuTableHeader(startYPos: Float): Float {
            val hTop = startYPos
            val hHeight = 25f
            paint.color = Color.rgb(204, 251, 241)
            canvas.drawRect(boxLeft, hTop, boxRight, hTop + hHeight, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(45, 212, 191)
            paint.strokeWidth = 0.9f
            canvas.drawRect(boxLeft, hTop, boxRight, hTop + hHeight, paint)
            listOf(colTgl, colJilid, colHal, colKet, colWali).forEach { x ->
                canvas.drawLine(x, hTop, x, hTop + hHeight, paint)
            }
            paint.style = Paint.Style.FILL

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            paint.color = Color.rgb(17, 94, 89)
            val textY = hTop + 16f
            canvas.drawText("No", colNo + 7f, textY, paint)
            canvas.drawText(if (isEn) "Date" else "Tanggal", colTgl + 6f, textY, paint)
            canvas.drawText(if (isEn) "Volume / Book" else "Jilid / Kitab", colJilid + 6f, textY, paint)
            canvas.drawText(if (isEn) "Page / Verse" else "Halaman", colHal + 6f, textY, paint)
            canvas.drawText(if (isEn) "Status" else "Keterangan", colKet + 6f, textY, paint)
            canvas.drawText(if (isEn) "Guardian Sign" else "Paraf Wali", colWali + 6f, textY, paint)
            return hTop + hHeight
        }

        if (orderedRecords.isEmpty()) {
            val emptyTop = currentY
            val emptyH = 44f
            paint.color = Color.rgb(248, 250, 252)
            canvas.drawRoundRect(RectF(boxLeft, emptyTop, boxRight, emptyTop + emptyH), 6f, 6f, paint)
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 9.5f
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText(
                if (isEn) "No mengaji records found for this period." else "Belum ada catatan mengaji pada periode ini.",
                pageWidth / 2f,
                emptyTop + 26f,
                paint
            )
            paint.textAlign = Paint.Align.LEFT
            currentY = emptyTop + emptyH + 16f
        } else {
            currentY = drawKartuTableHeader(currentY)
            val rowHeight = 23f
            orderedRecords.forEachIndexed { idx, rec ->
                if (currentY > pageHeight - 135f) {
                    finishCurrentCanvas()
                    pageNumber++
                    canvas = startNewCanvas(pageNumber)
                    currentY = 40f

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 9f
                    paint.color = Color.rgb(100, 116, 139)
                    canvas.drawText(
                        if (isEn) {
                            "Continued Mengaji Card (${student.name}) - Page $pageNumber"
                        } else {
                            "Lanjutan Kartu Mengaji (${student.name}) - Halaman $pageNumber"
                        },
                        boxLeft,
                        currentY,
                        paint
                    )
                    currentY += 12f
                    currentY = drawKartuTableHeader(currentY)
                }

                val rTop = currentY
                paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowHeight, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(203, 213, 225)
                paint.strokeWidth = 0.6f
                canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowHeight, paint)
                listOf(colTgl, colJilid, colHal, colKet, colWali).forEach { x ->
                    canvas.drawLine(x, rTop, x, rTop + rowHeight, paint)
                }
                paint.style = Paint.Style.FILL

                val textY = rTop + 15f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8.2f
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText("${idx + 1}.", colNo + 7f, textY, paint)

                val dateCell = dateFmt.format(Date(rec.sessionDate))
                canvas.drawText(dateCell, colTgl + 5f, textY, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val jilidDisplay = listOf(rec.bookType.trim(), rec.jilidOrJuz.trim())
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString(" • ")
                val jilidCell = TextUtils.ellipsize(jilidDisplay, TextPaint(paint), 112f, TextUtils.TruncateAt.END).toString()
                canvas.drawText(jilidCell, colJilid + 5f, textY, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val halCell = TextUtils.ellipsize(rec.pageOrVerse.ifBlank { "-" }, TextPaint(paint), 85f, TextUtils.TruncateAt.END).toString()
                canvas.drawText(halCell, colHal + 5f, textY, paint)

                val isFluent = MengajiHelper.isFluent(rec.fluencyStatus)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (isFluent) Color.rgb(21, 128, 61) else Color.rgb(185, 28, 28)
                val statusLabel = MengajiHelper.fluencyLabel(rec.fluencyStatus, isEn)
                val ketCell = TextUtils.ellipsize(statusLabel, TextPaint(paint), 90f, TextUtils.TruncateAt.END).toString()
                canvas.drawText(ketCell, colKet + 5f, textY, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(51, 65, 85)
                val waliCell = TextUtils.ellipsize(rec.waliName.ifBlank { waliName }, TextPaint(paint), 90f, TextUtils.TruncateAt.END).toString()
                canvas.drawText(waliCell, colWali + 5f, textY, paint)

                currentY = rTop + rowHeight
            }
            currentY += 16f
        }

        if (currentY > pageHeight - 140f) {
            finishCurrentCanvas()
            pageNumber++
            canvas = startNewCanvas(pageNumber)
            currentY = 40f
        }

        drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
        finishCurrentCanvas()
    }

    /**
     * Membuat dokumen PDF resmi Kartu Kendali Mengaji Bulanan Siswa (No, Tanggal, Jilid, Halaman,
     * Keterangan Lancar/Tidak Lancar, dan Tanda Tangan Wali Asuh).
     */
    fun generateKartuMengajiPdf(
        context: Context,
        student: ChildStudent,
        records: List<MengajiRecord>,
        periodLabel: String,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? {
        try {
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var activePage: PdfDocument.Page? = null

            renderKartuMengajiPages(
                context = context,
                student = student,
                records = records,
                periodLabel = periodLabel,
                waliName = waliName,
                schoolProfile = schoolProfile,
                pageWidth = pageWidth,
                pageHeight = pageHeight,
                startNewCanvas = { num ->
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, num).create()
                    val started = document.startPage(pageInfo)
                    activePage = started
                    started.canvas
                },
                finishCurrentCanvas = {
                    activePage?.let { document.finishPage(it) }
                    activePage = null
                }
            )

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanStudent = student.name.replace(Regex("[^a-zA-Z0-9]"), "_").take(25)
            val cleanPeriod = periodLabel.replace(Regex("[^a-zA-Z0-9]"), "_").take(20)
            val file = File(outputDir, "Kartu_Mengaji_${cleanStudent}_$cleanPeriod.pdf")

            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun buildKartuMengajiWaSummary(
        student: ChildStudent,
        records: List<MengajiRecord>,
        periodLabel: String,
        waliName: String,
        schoolProfile: SchoolProfile,
        language: AppLanguage = AppLanguageManager.currentLanguage
    ): String {
        val isEn = language == AppLanguage.ENGLISH
        val dateFmt = SimpleDateFormat("d MMM", language.locale)
        val schoolTitle = schoolProfile.schoolName.trim().ifBlank { schoolProfile.shortName.ifBlank { "Wali Asuh Care" } }
        val ordered = records.sortedWith(compareBy<MengajiRecord> { it.sessionDate }.thenBy { it.createdAt })
        val fluentCount = ordered.count { MengajiHelper.isFluent(it.fluencyStatus) }
        val notFluentCount = ordered.size - fluentCount
        val latestPos = ordered.lastOrNull()?.let { MengajiHelper.formatCompactPosition(it) } ?: "-"

        return buildString {
            appendLine(if (isEn) "*STUDENT MENGAJI PROGRESS CARD*" else "*KARTU KENDALI MENGAJI SISWA*")
            appendLine("*$schoolTitle*")
            appendLine(if (isEn) "Student: *${student.name}* (${student.classGrade.ifBlank { "-" }})" else "Siswa: *${student.name}* (${student.classGrade.ifBlank { "-" }})")
            appendLine(if (isEn) "Period: $periodLabel" else "Periode: $periodLabel")
            appendLine(if (isEn) "Latest Position: *$latestPos*" else "Posisi Terakhir: *$latestPos*")
            appendLine(if (isEn) "• Sessions: ${ordered.size} | Fluent: $fluentCount | Needs Practice: $notFluentCount" else "• Total: ${ordered.size}x | Lancar: $fluentCount | Tidak Lancar: $notFluentCount")
            appendLine()
            ordered.forEachIndexed { idx, rec ->
                val statusText = MengajiHelper.fluencyLabel(rec.fluencyStatus, isEn)
                val emoji = if (MengajiHelper.isFluent(rec.fluencyStatus)) "✅" else "⚠️"
                appendLine("${idx + 1}. ${dateFmt.format(Date(rec.sessionDate))} — ${rec.bookType} ${rec.jilidOrJuz} (${rec.pageOrVerse}) • $emoji $statusText")
            }
            appendLine()
            append(if (isEn) "Signed by Guardian: $waliName" else "Tanda Tangan Wali Asuh: $waliName")
        }
    }

    fun printPdfWithSystemPrintManager(
        context: Context,
        pdfFile: File,
        jobName: String
    ) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(
                context,
                if (isEn) "Print service is unavailable on this device." else "Layanan cetak tidak tersedia di perangkat ini.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: android.os.Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(pdfFile.name)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    FileInputStream(pdfFile).use { input ->
                        FileOutputStream(destination!!.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .build()
        printManager.print(jobName, adapter, attributes)
    }

    /**
     * Membuat dokumen PDF resmi Daftar Hadir Manual Kunjungan Orang Tua/Wali per Sesi
     * lengkap dengan kolom tanda tangan/paraf untuk pencatatan manual di lapangan.
     */
    fun generateVisitManualRosterPdf(
        context: Context,
        session: VisitSession,
        scheduledStudents: List<ChildStudent>,
        existingRecords: List<VisitRecord>,
        targetDateMillis: Long? = null,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile(),
        selectedDistrictFilter: String = "Semua",
        visitConfirmations: List<com.example.data.model.VisitConfirmation> = emptyList()
    ): File? {
        try {
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 0
            var currentPage: PdfDocument.Page? = null

            renderVisitManualRosterPages(
                context = context,
                session = session,
                scheduledStudents = scheduledStudents,
                existingRecords = existingRecords,
                targetDateMillis = targetDateMillis,
                waliName = waliName,
                schoolProfile = schoolProfile,
                selectedDistrictFilter = selectedDistrictFilter,
                visitConfirmations = visitConfirmations,
                startNewCanvas = {
                    currentPage?.let { document.finishPage(it) }
                    pageNumber++
                    val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    val created = document.startPage(info)
                    currentPage = created
                    created.canvas
                }
            )
            currentPage?.let { document.finishPage(it) }

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanTitle = session.title.replace(Regex("[^a-zA-Z0-9]"), "_").take(25)
            val distSlug = if (!selectedDistrictFilter.equals("Semua", ignoreCase = true) && selectedDistrictFilter.isNotBlank()) {
                "_Kec_" + VisitHelper.displayDistrict(selectedDistrictFilter, false).replace(Regex("[^a-zA-Z0-9]"), "_").take(18)
            } else ""
            val dateSlug = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(targetDateMillis ?: System.currentTimeMillis()))
            val file = File(outputDir, "Daftar_Hadir_Kunjungan_${cleanTitle}${distSlug}_$dateSlug.pdf")

            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    internal fun renderVisitManualRosterPages(
        context: Context,
        session: VisitSession,
        scheduledStudents: List<ChildStudent>,
        existingRecords: List<VisitRecord>,
        targetDateMillis: Long? = null,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile(),
        selectedDistrictFilter: String = "Semua",
        visitConfirmations: List<com.example.data.model.VisitConfirmation> = emptyList(),
        startNewCanvas: () -> Canvas
    ) {
        val lang = AppLanguageManager.getLanguage(context)
        val isEn = lang == AppLanguage.ENGLISH
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1
        var canvas = startNewCanvas()

        val paint = Paint().apply { isAntiAlias = true }
        val dateFmt = SimpleDateFormat("d MMM yyyy", lang.locale)
        val timeFmt = SimpleDateFormat("HH:mm", lang.locale)

        var currentY = 35f
        currentY = drawHeader(
            context = context,
            canvas = canvas,
            paint = paint,
            width = pageWidth,
            startY = currentY,
            schoolProfile = schoolProfile
        )

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        paint.color = Color.rgb(3, 105, 161)
        canvas.drawText(
            if (isEn) "PARENT VISIT MANUAL ATTENDANCE SHEET" else "DAFTAR HADIR MANUAL KUNJUNGAN ORANG TUA / WALI",
            pageWidth / 2f,
            currentY + 12f,
            paint
        )
        currentY += 25f

        val monthLabel = VisitHelper.formatMonthYearLabel(session.monthYear, lang.locale)
        val datesLabel = if (targetDateMillis != null && targetDateMillis > 0L) {
            dateFmt.format(Date(targetDateMillis))
        } else {
            val dates = session.getVisitDatesList()
            if (dates.isEmpty()) monthLabel else dates.joinToString(", ") { dateFmt.format(Date(it)) }
        }
        paint.textSize = 10f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(
            "${session.title.uppercase(lang.locale)} ($monthLabel)",
            pageWidth / 2f,
            currentY,
            paint
        )
        currentY += 13f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.8f
        paint.color = Color.rgb(71, 85, 105)
        val scheduleLine = TextUtils.ellipsize(
            if (isEn) {
                "Visit Date: $datesLabel  •  Hours: ${session.displayTimeRange()}"
            } else {
                "Tanggal Sesi: $datesLabel  •  Jam Kunjungan: ${session.displayTimeRange()}"
            },
            TextPaint(paint),
            pageWidth - 70f,
            TextUtils.TruncateAt.END
        ).toString()
        canvas.drawText(scheduleLine, pageWidth / 2f, currentY, paint)
        currentY += 14f
        paint.textAlign = Paint.Align.LEFT

        val boxLeft = 35f
        val boxRight = pageWidth - 35f
        val boxTop = currentY
        val summaryHeight = 46f
        paint.color = Color.rgb(240, 249, 255)
        canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + summaryHeight), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(186, 230, 253)
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + summaryHeight), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        val isSpecificDistrict = !selectedDistrictFilter.equals("Semua", ignoreCase = true) && selectedDistrictFilter.isNotBlank()
        val districtsText = if (isSpecificDistrict) {
            VisitHelper.displayDistrict(selectedDistrictFilter, isEn)
        } else {
            session.getDistrictsList()
                .joinToString(", ") { VisitHelper.displayDistrict(it, isEn) }
                .ifBlank { if (isEn) "All Districts" else "Semua Kecamatan" }
        }
        val presentCount = scheduledStudents.count { st ->
            VisitHelper.findVisitForStudent(existingRecords, st, targetDateMillis) != null
        }
        val confirmedCount = scheduledStudents.count { st ->
            VisitHelper.findConfirmationForStudent(visitConfirmations, session.id, st, targetDateMillis)?.isHadir == true
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        paint.color = Color.rgb(3, 105, 161)
        val distLinePrefix = if (isSpecificDistrict) {
            if (isEn) "Printed District: " else "Kecamatan Dicetak: "
        } else {
            if (isEn) "Districts: " else "Kecamatan Terjadwal: "
        }
        val distLine = TextUtils.ellipsize(
            "$distLinePrefix$districtsText",
            TextPaint(paint),
            boxRight - boxLeft - 20f,
            TextUtils.TruncateAt.END
        ).toString()
        canvas.drawText(distLine, boxLeft + 10f, boxTop + 16f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(51, 65, 85)
        val statLine = if (isEn) {
            "• Scheduled: ${scheduledStudents.size}   • RSVP Confirmed: $confirmedCount   • Checked-in: $presentCount"
        } else {
            "• Total Siswa: ${scheduledStudents.size}   • Konfirmasi Hadir: $confirmedCount   • Sudah Check-in: $presentCount"
        }
        canvas.drawText(statLine, boxLeft + 10f, boxTop + 34f, paint)
        currentY = boxTop + summaryHeight + 12f

        val colNo = 35f
        val colName = 59f
        val colClass = 185f
        val colWaliGuest = 295f
        val colArrival = 430f
        val colSign = 492f

        fun drawManualHeader(startYPos: Float): Float {
            val hTop = startYPos
            val hHeight = 26f
            paint.color = Color.rgb(224, 242, 254)
            canvas.drawRect(boxLeft, hTop, boxRight, hTop + hHeight, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(56, 189, 248)
            paint.strokeWidth = 0.9f
            canvas.drawRect(boxLeft, hTop, boxRight, hTop + hHeight, paint)
            listOf(colName, colClass, colWaliGuest, colArrival, colSign).forEach { x ->
                canvas.drawLine(x, hTop, x, hTop + hHeight, paint)
            }
            paint.style = Paint.Style.FILL

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.2f
            paint.color = Color.rgb(3, 105, 161)
            val textY = hTop + 16.5f
            canvas.drawText("No", colNo + 5f, textY, paint)
            canvas.drawText(if (isEn) "Student Name" else "Nama Siswa", colName + 5f, textY, paint)
            canvas.drawText(if (isEn) "Class/Dorm" else "Kelas/Asrama", colClass + 4f, textY, paint)
            canvas.drawText(if (isEn) "Wali Anak Asuh (Visitor)" else "Wali Anak Asuh", colWaliGuest + 5f, textY, paint)
            canvas.drawText(if (isEn) "Time" else "Jam Datang", colArrival + 4f, textY, paint)
            canvas.drawText(if (isEn) "Signature" else "Paraf / TTD", colSign + 5f, textY, paint)
            return hTop + hHeight
        }

        if (scheduledStudents.isEmpty()) {
            val emptyTop = currentY
            val emptyH = 44f
            paint.color = Color.rgb(248, 250, 252)
            canvas.drawRoundRect(RectF(boxLeft, emptyTop, boxRight, emptyTop + emptyH), 6f, 6f, paint)
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 9.5f
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText(
                if (isEn) "No scheduled students in selected districts for this visit session." else "Belum ada siswa pada kecamatan terpilih untuk sesi kunjungan ini.",
                pageWidth / 2f,
                emptyTop + 26f,
                paint
            )
            paint.textAlign = Paint.Align.LEFT
            currentY = emptyTop + emptyH + 14f
        } else {
            currentY = drawManualHeader(currentY)
            val rowHeight = 28f
            scheduledStudents.forEachIndexed { idx, st ->
                if (currentY > pageHeight - 130f) {
                    pageNumber++
                    canvas = startNewCanvas()
                    currentY = 40f

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 9f
                    paint.color = Color.rgb(100, 116, 139)
                    canvas.drawText(
                        if (isEn) "Continued Manual Visit Attendance (${session.title}) - Page $pageNumber" else "Lanjutan Daftar Hadir Kunjungan (${session.title}) - Halaman $pageNumber",
                        35f,
                        currentY,
                        paint
                    )
                    currentY += 12f
                    currentY = drawManualHeader(currentY)
                }

                val rTop = currentY
                paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowHeight, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(203, 213, 225)
                paint.strokeWidth = 0.6f
                canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowHeight, paint)
                listOf(colName, colClass, colWaliGuest, colArrival, colSign).forEach { x ->
                    canvas.drawLine(x, rTop, x, rTop + rowHeight, paint)
                }
                paint.style = Paint.Style.FILL

                val textY = rTop + 17.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8.2f
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText("${idx + 1}.", colNo + 5f, textY, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val nameCell = TextUtils.ellipsize(st.name, TextPaint(paint), colClass - colName - 8f, TextUtils.TruncateAt.END).toString()
                canvas.drawText(nameCell, colName + 5f, textY, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(51, 65, 85)
                val distLabel = VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(st), isEn)
                val classParts = buildList {
                    if (st.classGrade.isNotBlank()) add(st.classGrade.trim())
                    if (st.dormitory.isNotBlank()) add(st.dormitory.trim())
                    if (!isSpecificDistrict && distLabel.isNotBlank()) add(distLabel)
                }
                val classDorm = classParts.joinToString(" • ").ifBlank { "-" }
                val classCell = TextUtils.ellipsize(classDorm, TextPaint(paint), colWaliGuest - colClass - 8f, TextUtils.TruncateAt.END).toString()
                canvas.drawText(classCell, colClass + 4f, textY, paint)

                val matchedRec = VisitHelper.findVisitForStudent(existingRecords, st, targetDateMillis)
                if (matchedRec != null) {
                    val visitorText = listOf(matchedRec.visitorRelation, matchedRec.visitorName)
                        .filter { it.isNotBlank() }
                        .joinToString(": ")
                        .ifBlank { matchedRec.visitorRelation }
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.rgb(15, 23, 42)
                    val visitorCell = TextUtils.ellipsize(visitorText, TextPaint(paint), colArrival - colWaliGuest - 8f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(visitorCell, colWaliGuest + 5f, textY, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = Color.rgb(21, 128, 61)
                    val arrivalText = "${timeFmt.format(Date(matchedRec.checkedInAt))} WIB"
                    canvas.drawText(arrivalText, colArrival + 4f, textY, paint)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText(if (isEn) "✓ Checked" else "✓ Hadir", colSign + 5f, textY, paint)
                }
                // Ketika belum check-in digital: kolom "Wali Anak Asuh", "Jam Datang", dan "Paraf / TTD"
                // sengaja dibiarkan kosong bersih agar siap ditulis manual dengan pulpen saat absensi fisik.

                currentY = rTop + rowHeight
            }
            currentY += 14f
        }

        if (currentY > pageHeight - 140f) {
            pageNumber++
            canvas = startNewCanvas()
            currentY = 40f
        }

        drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
    }

    /**
     * Membuat dokumen PDF resmi Laporan Kehadiran Kunjungan (Sudah Hadir vs Belum Berkunjung).
     */
    fun generateVisitAttendanceReportPdf(
        context: Context,
        session: VisitSession,
        scheduledStudents: List<ChildStudent>,
        sessionRecords: List<VisitRecord>,
        targetDateMillis: Long? = null,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? {
        try {
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 0
            var currentPage: PdfDocument.Page? = null

            renderVisitAttendanceReportPages(
                context = context,
                session = session,
                scheduledStudents = scheduledStudents,
                sessionRecords = sessionRecords,
                targetDateMillis = targetDateMillis,
                waliName = waliName,
                schoolProfile = schoolProfile,
                startNewCanvas = {
                    currentPage?.let { document.finishPage(it) }
                    pageNumber++
                    val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    val created = document.startPage(info)
                    currentPage = created
                    created.canvas
                }
            )
            currentPage?.let { document.finishPage(it) }

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanTitle = session.title.replace(Regex("[^a-zA-Z0-9]"), "_").take(25)
            val dateSlug = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(targetDateMillis ?: System.currentTimeMillis()))
            val file = File(outputDir, "Laporan_Kehadiran_Kunjungan_${cleanTitle}_$dateSlug.pdf")

            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    internal fun renderVisitAttendanceReportPages(
        context: Context,
        session: VisitSession,
        scheduledStudents: List<ChildStudent>,
        sessionRecords: List<VisitRecord>,
        targetDateMillis: Long? = null,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile(),
        startNewCanvas: () -> Canvas
    ) {
        val lang = AppLanguageManager.getLanguage(context)
        val isEn = lang == AppLanguage.ENGLISH
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1
        var canvas = startNewCanvas()

        val paint = Paint().apply { isAntiAlias = true }
        val dateFmt = SimpleDateFormat("d MMM yyyy", lang.locale)
        val timeFmt = SimpleDateFormat("HH:mm", lang.locale)

        val normalizedTarget = targetDateMillis?.takeIf { it > 0L }?.let { VisitHelper.normalizeDayStart(it) }
        val filteredRecords = if (normalizedTarget == null) {
            sessionRecords
        } else {
            sessionRecords.filter { VisitHelper.normalizeDayStart(it.visitDate) == normalizedTarget }
        }

        val presentPairs = scheduledStudents.mapNotNull { st ->
            val rec = VisitHelper.findVisitForStudent(filteredRecords, st, normalizedTarget)
            if (rec != null) st to rec else null
        }
        val extraRecords = filteredRecords.filter { rec ->
            scheduledStudents.none { st -> VisitHelper.matchesRecordToStudent(rec, st) }
        }
        val absentStudents = scheduledStudents.filter { st ->
            VisitHelper.findVisitForStudent(filteredRecords, st, normalizedTarget) == null
        }
        val totalScheduled = scheduledStudents.size
        val totalPresent = presentPairs.size + extraRecords.size
        val pct = if (totalScheduled > 0) ((presentPairs.size * 100) / totalScheduled) else 0

        var currentY = 35f
        currentY = drawHeader(
            context = context,
            canvas = canvas,
            paint = paint,
            width = pageWidth,
            startY = currentY,
            schoolProfile = schoolProfile
        )

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        paint.color = Color.rgb(4, 120, 87)
        canvas.drawText(
            if (isEn) "PARENT VISIT ATTENDANCE REPORT" else "LAPORAN KEHADIRAN KUNJUNGAN ORANG TUA",
            pageWidth / 2f,
            currentY + 12f,
            paint
        )
        currentY += 25f

        val periodSub = if (normalizedTarget != null) {
            "${session.title} • ${dateFmt.format(Date(normalizedTarget))}"
        } else {
            "${session.title} • ${VisitHelper.formatMonthYearLabel(session.monthYear, lang.locale)}"
        }
        paint.textSize = 10f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(periodSub, pageWidth / 2f, currentY, paint)
        currentY += 15f
        paint.textAlign = Paint.Align.LEFT

        val boxLeft = 35f
        val boxRight = pageWidth - 35f
        val boxTop = currentY
        val summaryHeight = 46f
        paint.color = Color.rgb(236, 253, 245)
        canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + summaryHeight), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(167, 243, 208)
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + summaryHeight), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        paint.color = Color.rgb(6, 95, 70)
        canvas.drawText(
            if (isEn) "VISIT ATTENDANCE SUMMARY ($pct% Attended):" else "RINGKASAN KEHADIRAN KUNJUNGAN ($pct% Terlaksana):",
            boxLeft + 10f,
            boxTop + 15f,
            paint
        )
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.color = Color.rgb(30, 41, 59)
        canvas.drawText(
            if (isEn) {
                "• Scheduled: $totalScheduled Students   • Attended: $totalPresent   • Not Yet Visited: ${absentStudents.size}"
            } else {
                "• Terjadwal: $totalScheduled Siswa   • Sudah Hadir: $totalPresent   • Belum Berkunjung: ${absentStudents.size}"
            },
            boxLeft + 10f,
            boxTop + 33f,
            paint
        )
        currentY = boxTop + summaryHeight + 14f

        fun ensureSpace(needed: Float, contTitle: String, redrawHeader: (() -> Unit)? = null) {
            if (currentY > pageHeight - needed) {
                pageNumber++
                canvas = startNewCanvas()
                currentY = 40f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 9f
                paint.color = Color.rgb(100, 116, 139)
                canvas.drawText("$contTitle (Hal. $pageNumber)", 35f, currentY, paint)
                currentY += 14f
                redrawHeader?.invoke()
            }
        }

        val allPresentRows = presentPairs.map { it.second } + extraRecords
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        paint.color = Color.rgb(21, 128, 61)
        canvas.drawText(
            if (isEn) "A. ATTENDED (${allPresentRows.size} Students)" else "A. SUDAH HADIR BERKUNJUNG (${allPresentRows.size} Siswa)",
            boxLeft,
            currentY,
            paint
        )
        currentY += 8f

        fun drawPresentHeader() {
            val hTop = currentY
            val hH = 22f
            paint.color = Color.rgb(220, 252, 231)
            canvas.drawRect(boxLeft, hTop, boxRight, hTop + hH, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(134, 239, 172)
            paint.strokeWidth = 0.8f
            canvas.drawRect(boxLeft, hTop, boxRight, hTop + hH, paint)
            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.2f
            paint.color = Color.rgb(22, 101, 52)
            val tY = hTop + 14.5f
            canvas.drawText("No", 42f, tY, paint)
            canvas.drawText(if (isEn) "Student Name" else "Nama Siswa", 66f, tY, paint)
            canvas.drawText(if (isEn) "District" else "Kecamatan", 225f, tY, paint)
            canvas.drawText(if (isEn) "Time & Date" else "Waktu Hadir", 322f, tY, paint)
            canvas.drawText(if (isEn) "Visitor / Method" else "Pengunjung / Cara", 425f, tY, paint)
            currentY = hTop + hH
        }

        if (allPresentRows.isEmpty()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 9f
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText(
                if (isEn) "No check-ins recorded yet." else "Belum ada siswa yang tercatat hadir berkunjung.",
                boxLeft + 6f,
                currentY + 12f,
                paint
            )
            currentY += 24f
        } else {
            drawPresentHeader()
            val rH = 21f
            allPresentRows.forEachIndexed { idx, rec ->
                ensureSpace(125f, session.title) { drawPresentHeader() }
                val rTop = currentY
                paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                canvas.drawRect(boxLeft, rTop, boxRight, rTop + rH, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(226, 232, 240)
                paint.strokeWidth = 0.5f
                canvas.drawRect(boxLeft, rTop, boxRight, rTop + rH, paint)
                paint.style = Paint.Style.FILL

                val tY = rTop + 14f
                paint.textSize = 8.2f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText("${idx + 1}.", 42f, tY, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(
                    TextUtils.ellipsize(rec.studentName, TextPaint(paint), 152f, TextUtils.TruncateAt.END).toString(),
                    66f,
                    tY,
                    paint
                )
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(51, 65, 85)
                canvas.drawText(
                    TextUtils.ellipsize(VisitHelper.displayDistrict(rec.studentDistrict, isEn), TextPaint(paint), 92f, TextUtils.TruncateAt.END).toString(),
                    225f,
                    tY,
                    paint
                )
                val timeLabel = "${dateFmt.format(Date(rec.visitDate))} ${timeFmt.format(Date(rec.checkedInAt))}"
                canvas.drawText(timeLabel, 322f, tY, paint)
                val visitorSummary = buildString {
                    append(rec.visitorRelation.ifBlank { "Wali" })
                    if (rec.visitorName.isNotBlank()) append(" (${rec.visitorName})")
                    append(" • ${rec.checkInMethod}")
                }
                canvas.drawText(
                    TextUtils.ellipsize(visitorSummary, TextPaint(paint), 130f, TextUtils.TruncateAt.END).toString(),
                    425f,
                    tY,
                    paint
                )
                currentY = rTop + rH
            }
            currentY += 16f
        }

        ensureSpace(145f, session.title)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        paint.color = Color.rgb(185, 28, 28)
        canvas.drawText(
            if (isEn) "B. NOT YET VISITED (${absentStudents.size} Students)" else "B. BELUM BERKUNJUNG (${absentStudents.size} Siswa)",
            boxLeft,
            currentY,
            paint
        )
        currentY += 8f

        fun drawAbsentHeader() {
            val hTop = currentY
            val hH = 22f
            paint.color = Color.rgb(254, 226, 226)
            canvas.drawRect(boxLeft, hTop, boxRight, hTop + hH, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(252, 165, 165)
            paint.strokeWidth = 0.8f
            canvas.drawRect(boxLeft, hTop, boxRight, hTop + hH, paint)
            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.2f
            paint.color = Color.rgb(153, 27, 27)
            val tY = hTop + 14.5f
            canvas.drawText("No", 42f, tY, paint)
            canvas.drawText(if (isEn) "Student Name" else "Nama Siswa", 66f, tY, paint)
            canvas.drawText(if (isEn) "Class / Dorm" else "Kelas / Asrama", 230f, tY, paint)
            canvas.drawText(if (isEn) "District" else "Kecamatan", 355f, tY, paint)
            canvas.drawText(if (isEn) "Parent Contact" else "Kontak Ibu / Wali", 448f, tY, paint)
            currentY = hTop + hH
        }

        if (absentStudents.isEmpty()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.2f
            paint.color = Color.rgb(21, 128, 61)
            canvas.drawText(
                if (isEn) "All scheduled students have been visited! (100%)" else "Alhamdulillah — Seluruh siswa terjadwal sudah dikunjungi! (100%)",
                boxLeft + 6f,
                currentY + 12f,
                paint
            )
            currentY += 26f
        } else {
            drawAbsentHeader()
            val rH = 21f
            absentStudents.forEachIndexed { idx, st ->
                ensureSpace(125f, session.title) { drawAbsentHeader() }
                val rTop = currentY
                paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(254, 242, 242)
                canvas.drawRect(boxLeft, rTop, boxRight, rTop + rH, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(226, 232, 240)
                paint.strokeWidth = 0.5f
                canvas.drawRect(boxLeft, rTop, boxRight, rTop + rH, paint)
                paint.style = Paint.Style.FILL

                val tY = rTop + 14f
                paint.textSize = 8.2f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText("${idx + 1}.", 42f, tY, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(
                    TextUtils.ellipsize(st.name, TextPaint(paint), 158f, TextUtils.TruncateAt.END).toString(),
                    66f,
                    tY,
                    paint
                )
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(51, 65, 85)
                val classDorm = listOf(st.classGrade.trim(), st.dormitory.trim()).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "-" }
                canvas.drawText(
                    TextUtils.ellipsize(classDorm, TextPaint(paint), 118f, TextUtils.TruncateAt.END).toString(),
                    230f,
                    tY,
                    paint
                )
                canvas.drawText(
                    TextUtils.ellipsize(VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(st), isEn), TextPaint(paint), 88f, TextUtils.TruncateAt.END).toString(),
                    355f,
                    tY,
                    paint
                )
                val contactText = st.motherName.ifBlank { st.emergencyContact }.ifBlank { "-" }
                canvas.drawText(
                    TextUtils.ellipsize(contactText, TextPaint(paint), 108f, TextUtils.TruncateAt.END).toString(),
                    448f,
                    tY,
                    paint
                )
                currentY = rTop + rH
            }
            currentY += 14f
        }

        ensureSpace(140f, session.title)
        drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
    }

    fun buildVisitReportWaSummary(
        session: VisitSession,
        scheduledStudents: List<ChildStudent>,
        sessionRecords: List<VisitRecord>,
        targetDateMillis: Long?,
        waliName: String,
        schoolProfile: SchoolProfile,
        language: AppLanguage = AppLanguageManager.currentLanguage
    ): String {
        val isEn = language == AppLanguage.ENGLISH
        val dateFmt = SimpleDateFormat("d MMM yyyy", language.locale)
        val timeFmt = SimpleDateFormat("HH:mm", language.locale)
        val normalizedTarget = targetDateMillis?.takeIf { it > 0L }?.let { VisitHelper.normalizeDayStart(it) }
        val filteredRecords = if (normalizedTarget == null) {
            sessionRecords
        } else {
            sessionRecords.filter { VisitHelper.normalizeDayStart(it.visitDate) == normalizedTarget }
        }

        val presentStudents = scheduledStudents.mapNotNull { st ->
            VisitHelper.findVisitForStudent(filteredRecords, st, normalizedTarget)?.let { st to it }
        }
        val absentStudents = scheduledStudents.filter { st ->
            VisitHelper.findVisitForStudent(filteredRecords, st, normalizedTarget) == null
        }
        val schoolTitle = schoolProfile.schoolName.trim().ifBlank { schoolProfile.shortName.ifBlank { "Wali Asuh Care" } }
        val dateLabel = if (normalizedTarget != null) {
            dateFmt.format(Date(normalizedTarget))
        } else {
            val dList = session.getVisitDatesList()
            if (dList.isEmpty()) VisitHelper.formatMonthYearLabel(session.monthYear, language.locale)
            else dList.joinToString(", ") { dateFmt.format(Date(it)) }
        }

        return buildString {
            appendLine(if (isEn) "*PARENT VISIT ATTENDANCE REPORT*" else "*LAPORAN KEHADIRAN KUNJUNGAN ORANG TUA*")
            appendLine("*$schoolTitle*")
            appendLine(if (isEn) "Session: *${session.title}*" else "Sesi: *${session.title}*")
            appendLine(if (isEn) "Date: $dateLabel" else "Tanggal: $dateLabel")
            appendLine(if (isEn) "• Scheduled: ${scheduledStudents.size} | Attended: ${presentStudents.size} | Not Visited: ${absentStudents.size}" else "• Terjadwal: ${scheduledStudents.size} | Sudah Hadir: ${presentStudents.size} | Belum Berkunjung: ${absentStudents.size}")
            appendLine()
            if (presentStudents.isNotEmpty()) {
                appendLine(if (isEn) "✅ *Attended (${presentStudents.size}):*" else "✅ *Sudah Hadir (${presentStudents.size}):*")
                presentStudents.forEachIndexed { idx, (st, rec) ->
                    appendLine("${idx + 1}. ${st.name} (${VisitHelper.displayDistrict(rec.studentDistrict, isEn)}) — ${timeFmt.format(Date(rec.checkedInAt))} WIB")
                }
                appendLine()
            }
            if (absentStudents.isNotEmpty()) {
                appendLine(if (isEn) "⏳ *Not Yet Visited (${absentStudents.size}):*" else "⏳ *Belum Berkunjung (${absentStudents.size}):*")
                absentStudents.forEachIndexed { idx, st ->
                    val dist = VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(st), isEn)
                    appendLine("${idx + 1}. ${st.name} ($dist • ${st.classGrade.ifBlank { "-" }})")
                }
                appendLine()
            }
            append(if (isEn) "Reported by: $waliName" else "Dilaporkan oleh: $waliName")
        }
    }

    /**
     * Membuat PDF Laporan Tabungan Per Anak Asuh (Pemasukan, Pengeluaran, Saldo & Status Aktif/Dibatalkan)
     * menggunakan satu tanda tangan Wali Asuh kanan bawah + QR Code.
     */
    fun generateTabunganStudentPdf(
        context: Context,
        student: ChildStudent,
        transactions: List<TabunganTransaction>,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? {
        try {
            val lang = AppLanguageManager.getLanguage(context)
            val isEn = lang == AppLanguage.ENGLISH
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            val paint = Paint().apply { isAntiAlias = true }
            val dateFmt = SimpleDateFormat("d MMM yyyy HH:mm", lang.locale)

            var currentY = 35f
            currentY = drawHeader(
                context = context,
                canvas = canvas,
                paint = paint,
                width = pageWidth,
                startY = currentY,
                schoolProfile = schoolProfile,
                unitTitle = if (isEn) "PRIVATE STUDENT SAVINGS LEDGER (WALI ASUH)" else "CATATAN TABUNGAN PRIBADI WALI ASUH"
            )

            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 13f
            paint.color = Color.rgb(180, 83, 9)
            canvas.drawText(
                if (isEn) "STUDENT PERSONAL SAVINGS & ALLOWANCE LEDGER" else "LAPORAN TABUNGAN TITIPAN UANG SISWA",
                pageWidth / 2f,
                currentY + 12f,
                paint
            )
            currentY += 24f
            paint.textSize = 9.5f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(
                if (isEn) "Private Record Managed by: $waliName" else "Catatan Pribadi Dikelola Oleh: $waliName",
                pageWidth / 2f,
                currentY,
                paint
            )
            currentY += 16f
            paint.textAlign = Paint.Align.LEFT

            val summary = TabunganHelper.computeSummaryForStudent(transactions, student)
            val boxLeft = 35f
            val boxRight = pageWidth - 35f
            val boxTop = currentY
            val boxH = 74f
            paint.color = Color.rgb(255, 251, 235)
            canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + boxH), 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(253, 230, 138)
            paint.strokeWidth = 1f
            canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + boxH), 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText("${student.name} (NIS: ${student.nis.ifBlank { "-" }})", boxLeft + 10f, boxTop + 18f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.color = Color.rgb(71, 85, 105)
            canvas.drawText(
                "${if (isEn) "Class" else "Kelas"}: ${student.classGrade.ifBlank { "-" }}  |  Asrama: ${student.dormitory.ifBlank { "-" }}  |  ${if (isEn) "Status: Private" else "Sifat: Pribadi Wali Asuh"}",
                boxLeft + 10f,
                boxTop + 34f,
                paint
            )
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.5f
            paint.color = Color.rgb(21, 128, 61)
            canvas.drawText("${if (isEn) "Deposit (+)" else "Masuk (+)"}: ${TabunganHelper.formatRupiah(summary.totalIn)}", boxLeft + 10f, boxTop + 54f, paint)
            paint.color = Color.rgb(185, 28, 28)
            canvas.drawText("${if (isEn) "Expense (-)" else "Keluar (-)"}: ${TabunganHelper.formatRupiah(summary.totalOut)}", boxLeft + 180f, boxTop + 54f, paint)
            paint.color = Color.rgb(180, 83, 9)
            canvas.drawText("${if (isEn) "Balance" else "Saldo Akhir"}: ${TabunganHelper.formatRupiah(summary.balance)}", boxLeft + 355f, boxTop + 54f, paint)
            currentY = boxTop + boxH + 14f

            val sortedTxs = transactions
                .filter { TabunganHelper.matchesStudent(it, student) }
                .sortedWith(compareByDescending<TabunganTransaction> { it.transactionDate }.thenByDescending { it.createdAt })

            fun drawTxHeader(y: Float): Float {
                val h = 23f
                paint.color = Color.rgb(254, 243, 199)
                canvas.drawRect(boxLeft, y, boxRight, y + h, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(245, 158, 11)
                paint.strokeWidth = 0.8f
                canvas.drawRect(boxLeft, y, boxRight, y + h, paint)
                paint.style = Paint.Style.FILL
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.2f
                paint.color = Color.rgb(146, 64, 14)
                val tY = y + 15f
                canvas.drawText("No", boxLeft + 6f, tY, paint)
                canvas.drawText(if (isEn) "Date" else "Tanggal", boxLeft + 30f, tY, paint)
                canvas.drawText(if (isEn) "Description / Notes" else "Keterangan", boxLeft + 135f, tY, paint)
                canvas.drawText(if (isEn) "Type" else "Jenis", boxLeft + 335f, tY, paint)
                canvas.drawText(if (isEn) "Nominal" else "Nominal", boxLeft + 410f, tY, paint)
                return y + h
            }

            if (sortedTxs.isEmpty()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 9.5f
                paint.color = Color.rgb(100, 116, 139)
                canvas.drawText(
                    if (isEn) "No transactions recorded yet." else "Belum ada transaksi tabungan tercatat.",
                    boxLeft + 8f,
                    currentY + 14f,
                    paint
                )
                currentY += 30f
            } else {
                currentY = drawTxHeader(currentY)
                val rowH = 22f
                sortedTxs.forEachIndexed { idx, tx ->
                    if (currentY > pageHeight - 130f) {
                        document.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = 40f
                        currentY = drawTxHeader(currentY)
                    }
                    val rTop = currentY
                    paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                    canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowH, paint)
                    paint.style = Paint.Style.STROKE
                    paint.color = Color.rgb(226, 232, 240)
                    paint.strokeWidth = 0.5f
                    canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowH, paint)
                    paint.style = Paint.Style.FILL

                    val tY = rTop + 14.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 8f
                    paint.color = Color.rgb(15, 23, 42)
                    canvas.drawText("${idx + 1}.", boxLeft + 6f, tY, paint)
                    canvas.drawText(dateFmt.format(Date(tx.transactionDate)), boxLeft + 30f, tY, paint)

                    val isActive = TabunganHelper.isActive(tx.status)
                    val desc = buildString {
                        append(tx.title.ifBlank { if (TabunganHelper.isIncome(tx.transactionType)) "Titip Uang" else "Pengeluaran" })
                        if (tx.notes.isNotBlank()) append(" (${tx.notes})")
                        if (!isActive) append(" [DIBATALKAN]")
                    }
                    canvas.drawText(
                        TextUtils.ellipsize(desc, TextPaint(paint), 192f, TextUtils.TruncateAt.END).toString(),
                        boxLeft + 135f,
                        tY,
                        paint
                    )

                    val isIn = TabunganHelper.isIncome(tx.transactionType)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = when {
                        !isActive -> Color.rgb(148, 163, 184)
                        isIn -> Color.rgb(21, 128, 61)
                        else -> Color.rgb(185, 28, 28)
                    }
                    canvas.drawText(
                        when {
                            !isActive -> if (isEn) "VOID" else "BATAL"
                            isIn -> if (isEn) "IN (+)" else "MASUK"
                            else -> if (isEn) "OUT (-)" else "KELUAR"
                        },
                        boxLeft + 335f,
                        tY,
                        paint
                    )
                    val sign = if (isIn) "+" else "-"
                    canvas.drawText("$sign${TabunganHelper.formatRupiah(tx.amount)}", boxLeft + 410f, tY, paint)
                    currentY = rTop + rowH
                }
                currentY += 16f
            }

            if (currentY > pageHeight - 140f) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = 40f
            }
            drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
            document.finishPage(page)

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanName = student.name.replace(Regex("[^a-zA-Z0-9]"), "_").take(25)
            val file = File(outputDir, "Tabungan_${cleanName}.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Membuat PDF Rekap Saldo Seluruh Anak Binaan Pribadi Wali Asuh.
     */
    fun generateTabunganSummaryPdf(
        context: Context,
        students: List<ChildStudent>,
        transactions: List<TabunganTransaction>,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? {
        try {
            val lang = AppLanguageManager.getLanguage(context)
            val isEn = lang == AppLanguage.ENGLISH
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            val paint = Paint().apply { isAntiAlias = true }

            var currentY = 35f
            currentY = drawHeader(
                context = context,
                canvas = canvas,
                paint = paint,
                width = pageWidth,
                startY = currentY,
                schoolProfile = schoolProfile,
                unitTitle = if (isEn) "PRIVATE STUDENT SAVINGS RECAP (WALI ASUH)" else "REKAP TABUNGAN PRIBADI WALI ASUH"
            )

            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 13f
            paint.color = Color.rgb(180, 83, 9)
            canvas.drawText(
                if (isEn) "PRIVATE STUDENT SAVINGS BALANCE SUMMARY" else "REKAP SALDO TABUNGAN ANAK BINAAN SAYA",
                pageWidth / 2f,
                currentY + 12f,
                paint
            )
            currentY += 24f
            paint.textSize = 9.5f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(
                if (isEn) "Holder: $waliName • Confidential (Only visible to managing Caregiver)" else "Pemegang Titipan: $waliName • Bersifat Pribadi",
                pageWidth / 2f,
                currentY,
                paint
            )
            currentY += 16f
            paint.textAlign = Paint.Align.LEFT

            val totals = TabunganHelper.computeAggregateTotals(transactions)
            val boxLeft = 35f
            val boxRight = pageWidth - 35f
            val boxTop = currentY
            val boxH = 44f
            paint.color = Color.rgb(255, 251, 235)
            canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + boxH), 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(253, 230, 138)
            paint.strokeWidth = 1f
            canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + boxH), 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.2f
            paint.color = Color.rgb(146, 64, 14)
            canvas.drawText(
                if (isEn) "TOTAL HELD BALANCE: ${TabunganHelper.formatRupiah(totals.totalBalance)}" else "TOTAL SALDO TITIPAN SAYA: ${TabunganHelper.formatRupiah(totals.totalBalance)}",
                boxLeft + 10f,
                boxTop + 17f,
                paint
            )
            paint.textSize = 8.5f
            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText(
                if (isEn) {
                    "In: ${TabunganHelper.formatRupiah(totals.totalIn)}  •  Out: ${TabunganHelper.formatRupiah(totals.totalOut)}  •  Students: ${students.size}"
                } else {
                    "Total Masuk: ${TabunganHelper.formatRupiah(totals.totalIn)}  •  Total Keluar: ${TabunganHelper.formatRupiah(totals.totalOut)}  •  Anak Binaan: ${students.size}"
                },
                boxLeft + 10f,
                boxTop + 34f,
                paint
            )
            currentY = boxTop + boxH + 14f

            fun drawSummaryHeader(y: Float): Float {
                val h = 23f
                paint.color = Color.rgb(254, 243, 199)
                canvas.drawRect(boxLeft, y, boxRight, y + h, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(245, 158, 11)
                paint.strokeWidth = 0.8f
                canvas.drawRect(boxLeft, y, boxRight, y + h, paint)
                paint.style = Paint.Style.FILL
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.2f
                paint.color = Color.rgb(146, 64, 14)
                val tY = y + 15f
                canvas.drawText("No", boxLeft + 6f, tY, paint)
                canvas.drawText(if (isEn) "Student Name" else "Nama Anak", boxLeft + 30f, tY, paint)
                canvas.drawText(if (isEn) "Class" else "Kelas", boxLeft + 190f, tY, paint)
                canvas.drawText(if (isEn) "In (+)" else "Masuk", boxLeft + 255f, tY, paint)
                canvas.drawText(if (isEn) "Out (-)" else "Keluar", boxLeft + 345f, tY, paint)
                canvas.drawText(if (isEn) "Balance" else "Saldo", boxLeft + 435f, tY, paint)
                return y + h
            }

            currentY = drawSummaryHeader(currentY)
            val rowH = 22f
            students.sortedBy { it.name }.forEachIndexed { idx, st ->
                if (currentY > pageHeight - 130f) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 40f
                    currentY = drawSummaryHeader(currentY)
                }
                val s = TabunganHelper.computeSummaryForStudent(transactions, st)
                val rTop = currentY
                paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowH, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(226, 232, 240)
                paint.strokeWidth = 0.5f
                canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowH, paint)
                paint.style = Paint.Style.FILL

                val tY = rTop + 14.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8f
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText("${idx + 1}.", boxLeft + 6f, tY, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(
                    TextUtils.ellipsize(st.name, TextPaint(paint), 152f, TextUtils.TruncateAt.END).toString(),
                    boxLeft + 30f,
                    tY,
                    paint
                )
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(st.classGrade.ifBlank { "-" }, boxLeft + 190f, tY, paint)
                paint.color = Color.rgb(21, 128, 61)
                canvas.drawText(TabunganHelper.formatRupiah(s.totalIn), boxLeft + 255f, tY, paint)
                paint.color = Color.rgb(185, 28, 28)
                canvas.drawText(TabunganHelper.formatRupiah(s.totalOut), boxLeft + 345f, tY, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.rgb(180, 83, 9)
                canvas.drawText(TabunganHelper.formatRupiah(s.balance), boxLeft + 435f, tY, paint)
                currentY = rTop + rowH
            }
            currentY += 16f

            if (currentY > pageHeight - 140f) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = 40f
            }
            drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
            document.finishPage(page)

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val dateSlug = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val file = File(outputDir, "Rekap_Tabungan_${dateSlug}.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun resolveKonselingPhotoFilesSync(
        rec: KonselingRecord,
        preloadedPhotos: Map<Long, List<File>>
    ): List<File> {
        val preloaded = preloadedPhotos[rec.id].orEmpty().filter { it.exists() && it.length() > 0L }
        if (preloaded.isNotEmpty()) return preloaded.take(KonselingPhotoHelper.MAX_PHOTOS)
        return KonselingPhotoHelper.extractSlots(rec).mapNotNull { slot ->
            slot.localPath.takeIf { it.isNotBlank() }?.let { path ->
                File(path).takeIf { it.exists() && it.length() > 0L }
            }
        }.take(KonselingPhotoHelper.MAX_PHOTOS)
    }

    private fun calculateKonselingPhotoBlockHeight(photoCount: Int): Float = when {
        photoCount <= 0 -> 0f
        photoCount == 1 -> 150f
        photoCount == 2 -> 138f
        else -> 118f
    }

    private fun drawKonselingPhotoGrid(
        canvas: Canvas,
        paint: Paint,
        leftX: Float,
        topY: Float,
        innerWidth: Float,
        photoFiles: List<File>,
        isEn: Boolean
    ): Float {
        if (photoFiles.isEmpty()) return topY
        val files = photoFiles.take(KonselingPhotoHelper.MAX_PHOTOS)
        val count = files.size

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8f
        paint.color = Color.rgb(91, 33, 182)
        val headerLabel = if (isEn) {
            "Photo Documentation ($count ${if (count == 1) "Photo" else "Photos"}):"
        } else {
            "Lampiran Dokumentasi ($count Foto):"
        }
        canvas.drawText(headerLabel, leftX, topY + 10f, paint)

        val gridTop = topY + 15f
        val (boxW, boxH, gap, startOffsetX) = when (count) {
            1 -> {
                val w = (innerWidth * 0.56f).coerceAtLeast(220f).coerceAtMost(innerWidth)
                val offset = (innerWidth - w) / 2f
                listOf(w, 128f, 0f, offset)
            }
            2 -> {
                val g = 10f
                val w = (innerWidth - g) / 2f
                listOf(w, 116f, g, 0f)
            }
            else -> {
                val g = 8f
                val w = (innerWidth - g * 2f) / 3f
                listOf(w, 96f, g, 0f)
            }
        }

        val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val captionH = 14f

        files.forEachIndexed { idx, file ->
            val cellLeft = leftX + startOffsetX + idx * (boxW + gap)
            val cellRight = cellLeft + boxW
            val cellBottom = gridTop + boxH

            // Bingkai kartu foto lembut
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(248, 250, 252)
            canvas.drawRoundRect(RectF(cellLeft, gridTop, cellRight, cellBottom), 5f, 5f, paint)

            val imgAreaLeft = cellLeft + 3f
            val imgAreaTop = gridTop + 3f
            val imgAreaRight = cellRight - 3f
            val imgAreaBottom = cellBottom - captionH - 2f
            val availW = (imgAreaRight - imgAreaLeft).coerceAtLeast(10f)
            val availH = (imgAreaBottom - imgAreaTop).coerceAtLeast(10f)

            val bmp = KonselingPhotoHelper.decodePdfBitmap(file, maxDimension = 680)
            if (bmp != null) {
                try {
                    val scale = minOf(availW / bmp.width.toFloat(), availH / bmp.height.toFloat())
                    val drawW = bmp.width * scale
                    val drawH = bmp.height * scale
                    val drawLeft = imgAreaLeft + (availW - drawW) / 2f
                    val drawTop = imgAreaTop + (availH - drawH) / 2f
                    canvas.drawBitmap(
                        bmp,
                        null,
                        RectF(drawLeft, drawTop, drawLeft + drawW, drawTop + drawH),
                        bmpPaint
                    )
                } finally {
                    bmp.recycle()
                }
            } else {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 7f
                paint.color = Color.rgb(148, 163, 184)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(
                    if (isEn) "Photo unavailable" else "Foto tidak tersedia",
                    cellLeft + boxW / 2f,
                    imgAreaTop + availH / 2f,
                    paint
                )
                paint.textAlign = Paint.Align.LEFT
            }

            // Garis pemisah & label nomor foto
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(226, 232, 240)
            paint.strokeWidth = 0.6f
            canvas.drawLine(cellLeft, cellBottom - captionH, cellRight, cellBottom - captionH, paint)

            paint.color = Color.rgb(203, 213, 225)
            paint.strokeWidth = 0.8f
            canvas.drawRoundRect(RectF(cellLeft, gridTop, cellRight, cellBottom), 5f, 5f, paint)

            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 7f
            paint.color = Color.rgb(71, 85, 105)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                if (isEn) "Photo ${idx + 1}" else "Foto ${idx + 1}",
                cellLeft + boxW / 2f,
                cellBottom - 4f,
                paint
            )
            paint.textAlign = Paint.Align.LEFT
        }

        return gridTop + boxH + 6f
    }

    /**
     * Membuat PDF Catatan Konseling Rahasia Per Anak (Hanya untuk Wali Asuh bersangkutan),
     * termasuk mengunduh cache foto lampiran dari cloud terlebih dahulu bila diperlukan.
     */
    suspend fun generateKonselingStudentPdfWithPhotos(
        context: Context,
        student: ChildStudent,
        records: List<KonselingRecord>,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? = withContext(Dispatchers.IO) {
        val studentRecs = records.filter { KonselingHelper.matchesStudent(it, student) }
        val preloaded = KonselingPhotoHelper.preloadPhotosForPdfRecords(context, studentRecs)
        generateKonselingStudentPdf(
            context = context,
            student = student,
            records = records,
            waliName = waliName,
            schoolProfile = schoolProfile,
            preloadedPhotos = preloaded
        )
    }

    /**
     * Membuat PDF Catatan Konseling Rahasia Per Anak (Hanya untuk Wali Asuh bersangkutan).
     */
    fun generateKonselingStudentPdf(
        context: Context,
        student: ChildStudent,
        records: List<KonselingRecord>,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile(),
        preloadedPhotos: Map<Long, List<File>> = emptyMap()
    ): File? {
        try {
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var currentPage: PdfDocument.Page? = null

            renderKonselingStudentPages(
                context = context,
                student = student,
                records = records,
                waliName = waliName,
                schoolProfile = schoolProfile,
                preloadedPhotos = preloadedPhotos,
                pageWidth = pageWidth,
                pageHeight = pageHeight,
                startNewCanvas = { pageNum ->
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                    val started = document.startPage(pageInfo)
                    currentPage = started
                    started.canvas
                },
                finishCurrentCanvas = {
                    currentPage?.let { document.finishPage(it) }
                    currentPage = null
                }
            )

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanName = student.name.replace(Regex("[^a-zA-Z0-9]"), "_").take(25)
            val file = File(outputDir, "Konseling_Rahasia_${cleanName}.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    internal fun renderKonselingStudentPages(
        context: Context,
        student: ChildStudent,
        records: List<KonselingRecord>,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile(),
        preloadedPhotos: Map<Long, List<File>> = emptyMap(),
        pageWidth: Int = 595,
        pageHeight: Int = 842,
        startNewCanvas: (pageNumber: Int) -> Canvas,
        finishCurrentCanvas: () -> Unit = {}
    ) {
        val lang = AppLanguageManager.getLanguage(context)
        val isEn = lang == AppLanguage.ENGLISH
        var pageNumber = 1
        var canvas = startNewCanvas(pageNumber)
        val paint = Paint().apply { isAntiAlias = true }
        val dateFmt = SimpleDateFormat("d MMM yyyy", lang.locale)

        var currentY = 35f
        currentY = drawHeader(
            context = context,
            canvas = canvas,
            paint = paint,
            width = pageWidth,
            startY = currentY,
            schoolProfile = schoolProfile,
            unitTitle = if (isEn) "CONFIDENTIAL STUDENT COUNSELING DOSSIER" else "DOKUMEN KONSELING RAHASIA WALI ASUH"
        )

        val boxLeft = 35f
        val boxRight = pageWidth - 35f
        val innerWidth = boxRight - boxLeft - 18f

        fun wrapPdfLines(
            text: String,
            textPaint: Paint,
            firstLineWidth: Float,
            otherLinesWidth: Float = firstLineWidth
        ): List<String> {
            val normalized = text.replace("\r\n", "\n").trim()
            if (normalized.isEmpty()) return listOf("-")
            val result = mutableListOf<String>()
            var isFirstOutputLine = true
            val paragraphs = normalized.split("\n")
            paragraphs.forEach { rawPara ->
                val para = rawPara.trim()
                if (para.isEmpty()) {
                    if (result.isNotEmpty() && result.last().isNotEmpty()) {
                        result.add("")
                    }
                    return@forEach
                }
                var remaining = para
                while (remaining.isNotEmpty()) {
                    val limitW = (if (isFirstOutputLine) firstLineWidth else otherLinesWidth).coerceAtLeast(32f)
                    val fitCount = textPaint.breakText(remaining, true, limitW, null).coerceAtLeast(1)
                    if (fitCount >= remaining.length) {
                        result.add(remaining)
                        remaining = ""
                    } else {
                        val breakAtSpace = remaining[fitCount].isWhitespace()
                        val splitIdx = if (breakAtSpace) {
                            fitCount
                        } else {
                            val lastSpace = remaining.lastIndexOf(' ', fitCount - 1)
                            if (lastSpace > 0) lastSpace else fitCount
                        }
                        result.add(remaining.substring(0, splitIdx).trimEnd())
                        remaining = remaining.substring(splitIdx).trimStart()
                    }
                    isFirstOutputLine = false
                }
            }
            return if (result.isEmpty()) listOf("-") else result
        }

        val bannerText = if (isEn) {
            "CONFIDENTIAL • STRICTLY VISIBLE ONLY TO OWNING CAREGIVER ($waliName)"
        } else {
            "RAHASIA • HANYA DILIHAT OLEH WALI ASUH BERSANGKUTAN ($waliName)"
        }
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.2f
        val bannerLines = wrapPdfLines(bannerText, paint, boxRight - boxLeft - 20f)
        val bannerH = (bannerLines.size * 12f + 16f).coerceAtLeast(30f)
        paint.color = Color.rgb(245, 243, 255)
        canvas.drawRoundRect(RectF(boxLeft, currentY + 4f, boxRight, currentY + 4f + bannerH), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(124, 58, 237)
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(RectF(boxLeft, currentY + 4f, boxRight, currentY + 4f + bannerH), 6f, 6f, paint)
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.rgb(91, 33, 182)
        var bannerTextY = currentY + 18f
        bannerLines.forEach { line ->
            canvas.drawText(line, pageWidth / 2f, bannerTextY, paint)
            bannerTextY += 12f
        }
        currentY += bannerH + 14f
        paint.textAlign = Paint.Align.LEFT

        val studentRecs = records
            .filter { KonselingHelper.matchesStudent(it, student) }
            .sortedWith(compareByDescending<KonselingRecord> { it.sessionDate }.thenByDescending { it.createdAt })
        val stats = KonselingHelper.computeStudentStats(studentRecs, student)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        val studentTitleLines = wrapPdfLines(
            "${student.name} (NIS: ${student.nis.ifBlank { "-" }})",
            paint,
            innerWidth
        )
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        val studentMetaLines = wrapPdfLines(
            "${if (isEn) "Class" else "Kelas"}: ${student.classGrade.ifBlank { "-" }}  |  Asrama: ${student.dormitory.ifBlank { "-" }}  |  Wali Asuh: $waliName",
            paint,
            innerWidth
        )
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.8f
        val studentStatsLines = wrapPdfLines(
            if (isEn) {
                "Total: ${stats.totalEntries}  •  Sessions: ${stats.sessionsCount}  •  Growth: ${stats.growthCount}  •  Follow-Up: ${stats.followUpCount}  •  Resolved: ${stats.completedCount}"
            } else {
                "Total: ${stats.totalEntries}  •  Sesi: ${stats.sessionsCount}  •  Perkembangan: ${stats.growthCount}  •  Pantau: ${stats.followUpCount}  •  Selesai: ${stats.completedCount}"
            },
            paint,
            innerWidth
        )

        val infoTop = currentY
        val infoH = 12f +
            studentTitleLines.size * 13f + 2f +
            studentMetaLines.size * 11.5f + 3f +
            studentStatsLines.size * 12f + 6f
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(RectF(boxLeft, infoTop, boxRight, infoTop + infoH), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(boxLeft, infoTop, boxRight, infoTop + infoH), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        var infoY = infoTop + 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        paint.color = Color.rgb(15, 23, 42)
        studentTitleLines.forEach { line ->
            canvas.drawText(line, boxLeft + 10f, infoY, paint)
            infoY += 13f
        }
        infoY += 2f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(71, 85, 105)
        studentMetaLines.forEach { line ->
            canvas.drawText(line, boxLeft + 10f, infoY, paint)
            infoY += 11.5f
        }
        infoY += 3f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.8f
        paint.color = Color.rgb(91, 33, 182)
        studentStatsLines.forEach { line ->
            canvas.drawText(line, boxLeft + 10f, infoY, paint)
            infoY += 12f
        }
        currentY = infoTop + infoH + 12f

        fun startContinuationPage(contLabel: String) {
            finishCurrentCanvas()
            pageNumber++
            canvas = startNewCanvas(pageNumber)
            currentY = 38f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText("$contLabel (${if (isEn) "Page" else "Hal."} $pageNumber)", boxLeft, currentY, paint)
            currentY += 12f
        }

        if (studentRecs.isEmpty()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 9.5f
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText(
                if (isEn) "No confidential counseling entries recorded yet." else "Belum ada catatan konseling rahasia.",
                boxLeft + 8f,
                currentY + 14f,
                paint
            )
            currentY += 32f
        } else {
            val pageBottomLimit = pageHeight - 45f
            val bodyLineH = 11.5f

            data class StyledFieldRow(
                val prefix: String,
                val text: String,
                val color: Int,
                val isFirstInField: Boolean,
                val extraGapAfter: Float = 0f
            )

            fun buildWrappedFieldRows(
                prefix: String,
                rawValue: String,
                color: Int
            ): List<StyledFieldRow> {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.3f
                val prefixW = paint.measureText(prefix)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8.3f
                val lines = wrapPdfLines(
                    text = rawValue.ifBlank { "-" },
                    textPaint = paint,
                    firstLineWidth = (innerWidth - prefixW).coerceAtLeast(120f),
                    otherLinesWidth = innerWidth
                )
                return lines.mapIndexed { lineIdx, lineText ->
                    StyledFieldRow(
                        prefix = if (lineIdx == 0) prefix else "",
                        text = lineText,
                        color = color,
                        isFirstInField = lineIdx == 0,
                        extraGapAfter = if (lineIdx == lines.lastIndex) 3.5f else 0f
                    )
                }
            }

            studentRecs.forEachIndexed { idx, rec ->
                val typeLabel = if (KonselingHelper.isGrowthEntry(rec.entryType)) {
                    if (isEn) "GROWTH" else "PERKEMBANGAN"
                } else {
                    if (isEn) "SESSION" else "SESI KONSELING"
                }
                val statusLabel = KonselingHelper.formatStatusLabel(rec.status, isEn)
                val dateStatusText = "${dateFmt.format(Date(rec.sessionDate))} • $statusLabel"

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8f
                val dateStatusW = paint.measureText(dateStatusText)

                paint.textSize = 9.2f
                val headerText = "${idx + 1}. [$typeLabel] ${rec.title.ifBlank { KonselingHelper.formatTopicLabel(rec.topic, isEn) }}"
                val headerLines = wrapPdfLines(
                    text = headerText,
                    textPaint = paint,
                    firstLineWidth = (innerWidth - dateStatusW - 12f).coerceAtLeast(120f),
                    otherLinesWidth = innerWidth
                )

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8f
                val metaText = "${KonselingHelper.formatTopicLabel(rec.topic, isEn)}  |  Mood: ${KonselingHelper.formatMoodLabel(rec.moodLevel, isEn)}  |  Prioritas: ${KonselingHelper.formatUrgencyLabel(rec.urgencyLevel, isEn)}"
                val metaLines = wrapPdfLines(metaText, paint, innerWidth)

                val summaryPrefix = "${if (isEn) "Summary" else "Ringkasan"}: "
                val actionPrefix = "${if (isEn) "Action" else "Pendampingan"}: "
                val fuDateText = rec.followUpDate?.let { " (${dateFmt.format(Date(it))})" } ?: ""
                val fuPrefix = "${if (isEn) "Follow-Up" else "Tindak Lanjut"}$fuDateText: "

                val fieldRows = buildList {
                    addAll(buildWrappedFieldRows(summaryPrefix, rec.summary, Color.rgb(15, 23, 42)))
                    addAll(buildWrappedFieldRows(actionPrefix, rec.intervention, Color.rgb(15, 23, 42)))
                    addAll(buildWrappedFieldRows(fuPrefix, rec.followUpPlan, Color.rgb(91, 33, 182)))
                }

                val photoFiles = resolveKonselingPhotoFilesSync(rec, preloadedPhotos)
                val photoBlockH = calculateKonselingPhotoBlockHeight(photoFiles.size)

                val headerBlockH = headerLines.size * 12f + 2f + metaLines.size * 10.5f + 5f
                val bodyBlockH = fieldRows.sumOf { (bodyLineH + it.extraGapAfter).toDouble() }.toFloat()
                val totalCardH = 10f + headerBlockH + bodyBlockH + photoBlockH + 6f
                val maxSinglePageCardH = pageBottomLimit - 55f
                val cardBg = if (idx % 2 == 0) Color.WHITE else Color.rgb(250, 245, 255)

                fun drawCardFrame(topY: Float, bottomY: Float) {
                    paint.style = Paint.Style.FILL
                    paint.color = cardBg
                    canvas.drawRoundRect(RectF(boxLeft, topY, boxRight, bottomY), 6f, 6f, paint)
                    paint.style = Paint.Style.STROKE
                    paint.color = Color.rgb(221, 214, 254)
                    paint.strokeWidth = 0.9f
                    canvas.drawRoundRect(RectF(boxLeft, topY, boxRight, bottomY), 6f, 6f, paint)
                    paint.style = Paint.Style.FILL
                }

                fun drawHeaderSection(startY: Float): Float {
                    var y = startY + 15f
                    paint.textAlign = Paint.Align.RIGHT
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 8f
                    paint.color = if (KonselingHelper.isCompleted(rec.status)) Color.rgb(21, 128, 61) else Color.rgb(180, 83, 9)
                    canvas.drawText(dateStatusText, boxRight - 8f, y, paint)
                    paint.textAlign = Paint.Align.LEFT

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 9.2f
                    paint.color = Color.rgb(76, 29, 149)
                    headerLines.forEach { line ->
                        canvas.drawText(line, boxLeft + 9f, y, paint)
                        y += 12f
                    }
                    y += 1f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 8f
                    paint.color = Color.rgb(71, 85, 105)
                    metaLines.forEach { line ->
                        canvas.drawText(line, boxLeft + 9f, y, paint)
                        y += 10.5f
                    }
                    paint.color = Color.rgb(237, 233, 254)
                    paint.strokeWidth = 0.7f
                    canvas.drawLine(boxLeft + 9f, y - 5f, boxRight - 9f, y - 5f, paint)
                    return y + 5f
                }

                fun drawFieldRow(row: StyledFieldRow, baselineY: Float) {
                    paint.color = row.color
                    paint.textSize = 8.3f
                    var textStartX = boxLeft + 9f
                    if (row.prefix.isNotEmpty()) {
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        canvas.drawText(row.prefix, textStartX, baselineY, paint)
                        textStartX += paint.measureText(row.prefix)
                    }
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText(row.text, textStartX, baselineY, paint)
                }

                if (totalCardH <= maxSinglePageCardH) {
                    if (currentY + totalCardH > pageBottomLimit) {
                        startContinuationPage(
                            if (isEn) "Confidential Counseling: ${student.name}" else "Catatan Konseling: ${student.name}"
                        )
                    }
                    drawCardFrame(currentY, currentY + totalCardH)
                    var rowY = drawHeaderSection(currentY)
                    fieldRows.forEach { row ->
                        drawFieldRow(row, rowY)
                        rowY += bodyLineH + row.extraGapAfter
                    }
                    if (photoFiles.isNotEmpty()) {
                        drawKonselingPhotoGrid(
                            canvas = canvas,
                            paint = paint,
                            leftX = boxLeft + 9f,
                            topY = rowY - 4f,
                            innerWidth = innerWidth,
                            photoFiles = photoFiles,
                            isEn = isEn
                        )
                    }
                    currentY += totalCardH + 7f
                } else {
                    // Ultra-long single entry: split gracefully across pages without clipping a single line or photo
                    if (currentY + headerBlockH + 40f > pageBottomLimit) {
                        startContinuationPage(
                            if (isEn) "Confidential Counseling: ${student.name}" else "Catatan Konseling: ${student.name}"
                        )
                    }
                    var rowIndex = 0
                    var isFirstSlice = true
                    while (isFirstSlice || rowIndex < fieldRows.size) {
                        val sliceTop = currentY
                        val headerReserve = if (isFirstSlice) headerBlockH + 10f else 18f
                        var simY = sliceTop + headerReserve
                        var sliceEndIndex = rowIndex
                        while (sliceEndIndex < fieldRows.size) {
                            val step = bodyLineH + fieldRows[sliceEndIndex].extraGapAfter
                            if (simY + step + 8f > pageBottomLimit && sliceEndIndex > rowIndex) break
                            simY += step
                            sliceEndIndex++
                        }
                        val sliceBottom = (simY + 4f).coerceAtMost(pageBottomLimit)
                        drawCardFrame(sliceTop, sliceBottom)
                        var drawY = if (isFirstSlice) {
                            drawHeaderSection(sliceTop)
                        } else {
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                            paint.textSize = 7.8f
                            paint.color = Color.rgb(109, 40, 217)
                            canvas.drawText(
                                "${idx + 1}. ${rec.title.ifBlank { KonselingHelper.formatTopicLabel(rec.topic, isEn) }} (${if (isEn) "cont." else "lanjutan"})",
                                boxLeft + 9f,
                                sliceTop + 13f,
                                paint
                            )
                            sliceTop + 26f
                        }
                        while (rowIndex < sliceEndIndex) {
                            val row = fieldRows[rowIndex]
                            drawFieldRow(row, drawY)
                            drawY += bodyLineH + row.extraGapAfter
                            rowIndex++
                        }
                        isFirstSlice = false
                        currentY = sliceBottom + 7f
                        if (rowIndex < fieldRows.size) {
                            startContinuationPage(
                                if (isEn) "Confidential Counseling: ${student.name}" else "Catatan Konseling: ${student.name}"
                            )
                        }
                    }
                    if (photoFiles.isNotEmpty()) {
                        val standalonePhotoCardH = 22f + photoBlockH + 6f
                        if (currentY + standalonePhotoCardH > pageBottomLimit) {
                            startContinuationPage(
                                if (isEn) "Confidential Counseling: ${student.name}" else "Catatan Konseling: ${student.name}"
                            )
                        }
                        drawCardFrame(currentY, currentY + standalonePhotoCardH)
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                        paint.textSize = 7.8f
                        paint.color = Color.rgb(109, 40, 217)
                        canvas.drawText(
                            "${idx + 1}. ${rec.title.ifBlank { KonselingHelper.formatTopicLabel(rec.topic, isEn) }} — ${if (isEn) "Attachments" else "Lampiran Foto"}",
                            boxLeft + 9f,
                            currentY + 13f,
                            paint
                        )
                        drawKonselingPhotoGrid(
                            canvas = canvas,
                            paint = paint,
                            leftX = boxLeft + 9f,
                            topY = currentY + 18f,
                            innerWidth = innerWidth,
                            photoFiles = photoFiles,
                            isEn = isEn
                        )
                        currentY += standalonePhotoCardH + 7f
                    }
                }
            }
        }

        if (currentY > pageHeight - 140f) {
            finishCurrentCanvas()
            pageNumber++
            canvas = startNewCanvas(pageNumber)
            currentY = 40f
        }
        drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
        finishCurrentCanvas()
    }

    /**
     * Membuat PDF Rekap Konseling Rahasia Seluruh Anak Binaan Wali Asuh Bersangkutan,
     * lengkap dengan preload foto lampiran agar ikut tercetak rapi pada bagian lampiran PDF.
     */
    suspend fun generateKonselingSummaryPdfWithPhotos(
        context: Context,
        students: List<ChildStudent>,
        records: List<KonselingRecord>,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? = withContext(Dispatchers.IO) {
        val preloaded = KonselingPhotoHelper.preloadPhotosForPdfRecords(context, records)
        generateKonselingSummaryPdf(
            context = context,
            students = students,
            records = records,
            waliName = waliName,
            schoolProfile = schoolProfile,
            preloadedPhotos = preloaded
        )
    }

    /**
     * Membuat PDF Rekap Konseling Rahasia Seluruh Anak Binaan Wali Asuh Bersangkutan.
     */
    fun generateKonselingSummaryPdf(
        context: Context,
        students: List<ChildStudent>,
        records: List<KonselingRecord>,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile(),
        preloadedPhotos: Map<Long, List<File>> = emptyMap()
    ): File? {
        try {
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var currentPage: PdfDocument.Page? = null

            renderKonselingSummaryPages(
                context = context,
                students = students,
                records = records,
                waliName = waliName,
                schoolProfile = schoolProfile,
                preloadedPhotos = preloadedPhotos,
                pageWidth = pageWidth,
                pageHeight = pageHeight,
                startNewCanvas = { pageNum ->
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                    val started = document.startPage(pageInfo)
                    currentPage = started
                    started.canvas
                },
                finishCurrentCanvas = {
                    currentPage?.let { document.finishPage(it) }
                    currentPage = null
                }
            )

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val dateSlug = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val file = File(outputDir, "Rekap_Konseling_Rahasia_${dateSlug}.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    internal fun renderKonselingSummaryPages(
        context: Context,
        students: List<ChildStudent>,
        records: List<KonselingRecord>,
        waliName: String = "Wali Asuh",
        schoolProfile: SchoolProfile = SchoolProfile(),
        preloadedPhotos: Map<Long, List<File>> = emptyMap(),
        pageWidth: Int = 595,
        pageHeight: Int = 842,
        startNewCanvas: (pageNumber: Int) -> Canvas,
        finishCurrentCanvas: () -> Unit = {}
    ) {
        val lang = AppLanguageManager.getLanguage(context)
        val isEn = lang == AppLanguage.ENGLISH
        var pageNumber = 1
        var canvas = startNewCanvas(pageNumber)
        val paint = Paint().apply { isAntiAlias = true }
        val dateFmt = SimpleDateFormat("d MMM yyyy", lang.locale)

        var currentY = 35f
        currentY = drawHeader(
            context = context,
            canvas = canvas,
            paint = paint,
            width = pageWidth,
            startY = currentY,
            schoolProfile = schoolProfile,
            unitTitle = if (isEn) "CONFIDENTIAL COUNSELING RECAP (WALI ASUH)" else "REKAP KONSELING RAHASIA WALI ASUH"
        )

        val boxLeft = 35f
        val boxRight = pageWidth - 35f

        fun wrapSummaryCellLines(text: String, cellPaint: Paint, maxWidth: Float): List<String> {
            val normalized = text.replace("\r\n", "\n").trim()
            if (normalized.isEmpty()) return listOf("-")
            val result = mutableListOf<String>()
            normalized.split("\n").forEach { rawPara ->
                val para = rawPara.trim()
                if (para.isEmpty()) return@forEach
                var remaining = para
                while (remaining.isNotEmpty()) {
                    val count = cellPaint.breakText(remaining, true, maxWidth.coerceAtLeast(24f), null).coerceAtLeast(1)
                    if (count >= remaining.length) {
                        result.add(remaining)
                        remaining = ""
                    } else {
                        val breakAtSpace = remaining[count].isWhitespace()
                        val splitIdx = if (breakAtSpace) {
                            count
                        } else {
                            val lastSpace = remaining.lastIndexOf(' ', count - 1)
                            if (lastSpace > 0) lastSpace else count
                        }
                        result.add(remaining.substring(0, splitIdx).trimEnd())
                        remaining = remaining.substring(splitIdx).trimStart()
                    }
                }
            }
            return if (result.isEmpty()) listOf("-") else result
        }

        paint.color = Color.rgb(245, 243, 255)
        canvas.drawRoundRect(RectF(boxLeft, currentY + 4f, boxRight, currentY + 34f), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(124, 58, 237)
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(RectF(boxLeft, currentY + 4f, boxRight, currentY + 34f), 6f, 6f, paint)
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.2f
        paint.color = Color.rgb(91, 33, 182)
        canvas.drawText(
            if (isEn) {
                "CONFIDENTIAL • STRICTLY FOR OWNING CAREGIVER ONLY: $waliName"
            } else {
                "DOKUMEN RAHASIA • HANYA DILIHAT WALI ASUH BERSANGKUTAN: $waliName"
            },
            pageWidth / 2f,
            currentY + 22f,
            paint
        )
        currentY += 44f
        paint.textAlign = Paint.Align.LEFT

        val agg = KonselingHelper.computeAggregateStats(records)
        val boxTop = currentY
        val boxH = 42f
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + boxH), 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxTop + boxH), 6f, 6f, paint)
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.2f
        paint.color = Color.rgb(76, 29, 149)
        canvas.drawText(
            if (isEn) {
                "CHILDREN: ${students.size}  •  NOTES: ${agg.totalEntries}  •  FOLLOW-UP: ${agg.followUpCount}  •  GROWTH: ${agg.growthCount}"
            } else {
                "ANAK: ${students.size}  •  CATATAN: ${agg.totalEntries}  •  PANTAU: ${agg.followUpCount}  •  PERKEMBANGAN: ${agg.growthCount}"
            },
            boxLeft + 10f,
            boxTop + 25f,
            paint
        )
        currentY = boxTop + boxH + 12f

        fun drawSummaryHeader(y: Float): Float {
            val h = 23f
            paint.color = Color.rgb(237, 233, 254)
            canvas.drawRect(boxLeft, y, boxRight, y + h, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(124, 58, 237)
            paint.strokeWidth = 0.8f
            canvas.drawRect(boxLeft, y, boxRight, y + h, paint)
            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8f
            paint.color = Color.rgb(76, 29, 149)
            val tY = y + 15f
            canvas.drawText("No", boxLeft + 5f, tY, paint)
            canvas.drawText(if (isEn) "Date" else "Tanggal", boxLeft + 25f, tY, paint)
            canvas.drawText(if (isEn) "Student" else "Nama Anak", boxLeft + 88f, tY, paint)
            canvas.drawText(if (isEn) "Topic, Title & Notes" else "Topik, Judul & Rincian", boxLeft + 184f, tY, paint)
            canvas.drawText(if (isEn) "Mood" else "Kondisi", boxLeft + 388f, tY, paint)
            canvas.drawText(if (isEn) "Status" else "Status", boxLeft + 455f, tY, paint)
            return y + h
        }

        val sortedRecords = records.sortedWith(compareByDescending<KonselingRecord> { it.sessionDate }.thenByDescending { it.createdAt })
        currentY = drawSummaryHeader(currentY)
        val pageBottomLimit = pageHeight - 45f
        val lineH = 9.8f

        sortedRecords.forEachIndexed { idx, rec ->
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 7.8f
            val nameLines = wrapSummaryCellLines(rec.studentName, paint, 90f)

            val topicTitle = "${KonselingHelper.formatTopicLabel(rec.topic, isEn)}: ${rec.title.ifBlank { "-" }}"
            val titleLines = wrapSummaryCellLines(topicTitle, paint, 196f)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 7.3f
            val photoCount = resolveKonselingPhotoFilesSync(rec, preloadedPhotos).size
            val detailLines = buildList {
                if (rec.summary.isNotBlank()) {
                    addAll(wrapSummaryCellLines("${if (isEn) "Summary" else "Ringkasan"}: ${rec.summary}", paint, 196f))
                }
                if (rec.intervention.isNotBlank()) {
                    addAll(wrapSummaryCellLines("${if (isEn) "Action" else "Pendampingan"}: ${rec.intervention}", paint, 196f))
                }
                if (rec.followUpPlan.isNotBlank()) {
                    addAll(wrapSummaryCellLines("${if (isEn) "Follow-Up" else "Tindak Lanjut"}: ${rec.followUpPlan}", paint, 196f))
                }
                if (photoCount > 0) {
                    val photoNote = if (isEn) {
                        "Attachments: $photoCount photo(s) (see Photo Appendix below)"
                    } else {
                        "Lampiran: $photoCount foto dokumentasi (lihat Lampiran di bawah)"
                    }
                    addAll(wrapSummaryCellLines(photoNote, paint, 196f))
                }
            }

            paint.textSize = 7.6f
            val moodLines = wrapSummaryCellLines(KonselingHelper.formatMoodLabel(rec.moodLevel, isEn), paint, 62f)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val statusLines = wrapSummaryCellLines(KonselingHelper.formatStatusLabel(rec.status, isEn), paint, 64f)

            val centerColLinesCount = titleLines.size + detailLines.size
            val maxLines = maxOf(nameLines.size, centerColLinesCount, moodLines.size, statusLines.size, 1)
            val rowH = maxOf(22f, maxLines * lineH + 10f)

            if (currentY + rowH > pageBottomLimit && rowH <= (pageBottomLimit - 70f)) {
                finishCurrentCanvas()
                pageNumber++
                canvas = startNewCanvas(pageNumber)
                currentY = 40f
                currentY = drawSummaryHeader(currentY)
            }

            val rTop = currentY
            paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
            canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowH, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(226, 232, 240)
            paint.strokeWidth = 0.5f
            canvas.drawRect(boxLeft, rTop, boxRight, rTop + rowH, paint)
            paint.style = Paint.Style.FILL

            val startTextY = rTop + 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 7.6f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText("${idx + 1}.", boxLeft + 5f, startTextY, paint)
            canvas.drawText(dateFmt.format(Date(rec.sessionDate)), boxLeft + 25f, startTextY, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 7.8f
            var yName = startTextY
            nameLines.forEach { l ->
                canvas.drawText(l, boxLeft + 88f, yName, paint)
                yName += lineH
            }

            var yCenter = startTextY
            titleLines.forEach { l ->
                canvas.drawText(l, boxLeft + 184f, yCenter, paint)
                yCenter += lineH
            }
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 7.3f
            paint.color = Color.rgb(51, 65, 85)
            detailLines.forEach { l ->
                canvas.drawText(l, boxLeft + 184f, yCenter, paint)
                yCenter += lineH
            }

            paint.textSize = 7.6f
            paint.color = Color.rgb(15, 23, 42)
            var yMood = startTextY
            moodLines.forEach { l ->
                canvas.drawText(l, boxLeft + 388f, yMood, paint)
                yMood += lineH
            }

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = if (KonselingHelper.isCompleted(rec.status)) Color.rgb(21, 128, 61) else Color.rgb(180, 83, 9)
            var yStatus = startTextY
            statusLines.forEach { l ->
                canvas.drawText(l, boxLeft + 455f, yStatus, paint)
                yStatus += lineH
            }

            currentY = rTop + rowH
        }
        currentY += 16f

        // Bagian Lampiran Foto Sesi Konseling yang Terstruktur Rapi
        val recordsWithPhotos = sortedRecords.mapNotNull { rec ->
            val files = resolveKonselingPhotoFilesSync(rec, preloadedPhotos)
            if (files.isNotEmpty()) rec to files else null
        }
        if (recordsWithPhotos.isNotEmpty()) {
            val sectionHeaderH = 26f
            if (currentY + sectionHeaderH + 140f > pageBottomLimit) {
                finishCurrentCanvas()
                pageNumber++
                canvas = startNewCanvas(pageNumber)
                currentY = 40f
            }
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10f
            paint.color = Color.rgb(76, 29, 149)
            canvas.drawText(
                if (isEn) {
                    "COUNSELING SESSION PHOTO DOCUMENTATION APPENDIX"
                } else {
                    "LAMPIRAN FOTO DOKUMENTASI SESI KONSELING"
                },
                boxLeft,
                currentY + 12f,
                paint
            )
            currentY += 22f

            val innerWidth = boxRight - boxLeft - 18f
            recordsWithPhotos.forEachIndexed { photoIdx, (rec, photoFiles) ->
                val photoBlockH = calculateKonselingPhotoBlockHeight(photoFiles.size)
                val cardH = 30f + photoBlockH + 4f
                if (currentY + cardH > pageBottomLimit) {
                    finishCurrentCanvas()
                    pageNumber++
                    canvas = startNewCanvas(pageNumber)
                    currentY = 40f
                }

                paint.style = Paint.Style.FILL
                paint.color = if (photoIdx % 2 == 0) Color.WHITE else Color.rgb(250, 245, 255)
                canvas.drawRoundRect(RectF(boxLeft, currentY, boxRight, currentY + cardH), 6f, 6f, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(221, 214, 254)
                paint.strokeWidth = 0.9f
                canvas.drawRoundRect(RectF(boxLeft, currentY, boxRight, currentY + cardH), 6f, 6f, paint)
                paint.style = Paint.Style.FILL

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.6f
                paint.color = Color.rgb(76, 29, 149)
                val sessionTitle = rec.title.ifBlank { KonselingHelper.formatTopicLabel(rec.topic, isEn) }
                val cardHeaderText = "${photoIdx + 1}. ${rec.studentName} • ${dateFmt.format(Date(rec.sessionDate))} — $sessionTitle"
                val truncatedHeader = TextUtils.ellipsize(
                    cardHeaderText,
                    TextPaint(paint),
                    innerWidth,
                    TextUtils.TruncateAt.END
                ).toString()
                canvas.drawText(truncatedHeader, boxLeft + 9f, currentY + 15f, paint)

                drawKonselingPhotoGrid(
                    canvas = canvas,
                    paint = paint,
                    leftX = boxLeft + 9f,
                    topY = currentY + 22f,
                    innerWidth = innerWidth,
                    photoFiles = photoFiles,
                    isEn = isEn
                )
                currentY += cardH + 8f
            }
            currentY += 8f
        }

        if (currentY > pageHeight - 140f) {
            finishCurrentCanvas()
            pageNumber++
            canvas = startNewCanvas(pageNumber)
            currentY = 40f
        }
        drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
        finishCurrentCanvas()
    }

    /**
     * Membuat dokumen PDF resmi Matriks Jadwal Kerja Wali Asuh Bulanan (Landscape A4 842x595)
     * lengkap dengan kop surat sekolah, sel kode shift harian berwarna, rekap jam kerja per wali,
     * rincian kebutuhan shift harian, serta petunjuk kode shift milik sekolah.
     */
    fun generateJadwalMatriksPdf(
        context: Context,
        monthKey: String,
        entries: List<WaliScheduleEntry>,
        shiftCodes: List<WaliShiftCode>,
        waliName: String = "Admin Sekolah",
        schoolProfile: SchoolProfile = SchoolProfile()
    ): File? {
        return try {
            val document = PdfDocument()
            val pageWidth = 842 // Landscape A4 width
            val pageHeight = 595 // Landscape A4 height
            var activePage: PdfDocument.Page? = null

            renderJadwalMatriksPages(
                context = context,
                monthKey = monthKey,
                entries = entries,
                shiftCodes = shiftCodes,
                waliName = waliName,
                schoolProfile = schoolProfile,
                pageWidth = pageWidth,
                pageHeight = pageHeight,
                startNewCanvas = { num ->
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, num).create()
                    val started = document.startPage(pageInfo)
                    activePage = started
                    started.canvas
                },
                finishCurrentCanvas = {
                    activePage?.let { document.finishPage(it) }
                    activePage = null
                }
            )

            val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val safeMonth = monthKey.replace(Regex("[^0-9A-Za-z_-]"), "")
            val outFile = File(outputDir, "Jadwal_Resmi_Wali_Asuh_${safeMonth}_${System.currentTimeMillis()}.pdf")
            FileOutputStream(outFile).use { fos ->
                document.writeTo(fos)
            }
            document.close()
            outFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    internal fun renderJadwalMatriksPages(
        context: Context,
        monthKey: String,
        entries: List<WaliScheduleEntry>,
        shiftCodes: List<WaliShiftCode>,
        waliName: String = "Admin Sekolah",
        schoolProfile: SchoolProfile = SchoolProfile(),
        pageWidth: Int = 842,
        pageHeight: Int = 595,
        startNewCanvas: (pageNumber: Int) -> Canvas,
        finishCurrentCanvas: () -> Unit = {}
    ) {
        val lang = AppLanguageManager.getLanguage(context)
        val isEn = lang == AppLanguage.ENGLISH
        val daysInMonth = WaliScheduleHelper.daysInMonth(monthKey)
        val monthTitle = WaliScheduleHelper.formatMonthTitle(monthKey, isEn).uppercase(lang.locale)
        val effectiveCodes = shiftCodes.ifEmpty { WaliScheduleHelper.defaultShiftCodes(schoolProfile.tenantCode) }
        val shiftMap = WaliScheduleHelper.buildShiftMap(effectiveCodes, schoolProfile.tenantCode)
        val sortedEntries = entries.sortedWith(compareBy<WaliScheduleEntry> { it.sortOrder }.thenBy { it.waliName.lowercase() })

        val paint = Paint().apply { isAntiAlias = true }
        val marginX = 22f
        val tableRight = pageWidth - marginX
        val noColW = 20f
        val nameColW = 126f
        val rightSummaryCols = listOf("PF", "S", "M", "LP", "OF", "P1", "P2", "P3", "P4", "JK")
        val summaryTotalW = 154f
        val summaryColW = (summaryTotalW - 23.5f) / 9f
        val jkColW = 23.5f
        val daysTotalW = (tableRight - marginX) - noColW - nameColW - summaryTotalW
        val dayColW = daysTotalW / daysInMonth.toFloat()

        var pageNumber = 1
        var canvas = startNewCanvas(pageNumber)
        var currentY = 30f

        currentY = drawHeader(
            context = context,
            canvas = canvas,
            paint = paint,
            width = pageWidth,
            startY = currentY,
            schoolProfile = schoolProfile,
            unitTitle = if (isEn) "CAREGIVER DUTY ROSTER & SHIFT MANAGEMENT" else "TATA KELOLA JADWAL PIKET WALI ASUH"
        )

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11.5f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText(
            if (isEn) "OFFICIAL WALI ASUH WORK SCHEDULE • $monthTitle" else "JADWAL KERJA RESMI WALI ASUH • BULAN $monthTitle",
            pageWidth / 2f,
            currentY + 10f,
            paint
        )
        currentY += 18f
        paint.textAlign = Paint.Align.LEFT

        fun parseHexColor(hex: String, fallback: Int): Int = try {
            Color.parseColor(hex)
        } catch (_: Exception) {
            fallback
        }

        fun drawMatrixHeader(y: Float): Float {
            val h1 = 13f
            val h2 = 10f
            val totalH = h1 + h2

            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(15, 118, 110)
            canvas.drawRect(marginX, y, tableRight, y + h1, paint)
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRect(marginX, y + h1, tableRight, y + totalH, paint)

            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(203, 213, 225)
            paint.strokeWidth = 0.6f
            canvas.drawRect(marginX, y, tableRight, y + totalH, paint)

            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 6.8f
            paint.color = Color.WHITE
            canvas.drawText("No", marginX + noColW / 2f, y + 9.2f, paint)
            canvas.drawText(if (isEn) "Caregiver Name" else "Nama Wali Asuh", marginX + noColW + nameColW / 2f, y + 9.2f, paint)

            val dayStartX = marginX + noColW + nameColW
            for (d in 1..daysInMonth) {
                val cellX = dayStartX + (d - 1) * dayColW
                val dow = WaliScheduleHelper.dayOfWeekShort(monthKey, d, isEn)
                val isWeekend = dow.equals("Min", true) || dow.equals("Sun", true) || dow.equals("Sab", true) || dow.equals("Sat", true)
                if (isWeekend) {
                    paint.color = Color.rgb(190, 18, 60)
                    canvas.drawRect(cellX, y, cellX + dayColW, y + h1, paint)
                }
                paint.color = Color.WHITE
                paint.textSize = 6.5f
                canvas.drawText(d.toString(), cellX + dayColW / 2f, y + 9.2f, paint)

                paint.color = if (isWeekend) Color.rgb(190, 18, 60) else Color.rgb(71, 85, 105)
                paint.textSize = 5.4f
                canvas.drawText(dow.take(2), cellX + dayColW / 2f, y + h1 + 7.4f, paint)
            }

            var sumX = dayStartX + daysTotalW
            rightSummaryCols.forEachIndexed { idx, label ->
                val cw = if (idx == rightSummaryCols.lastIndex) jkColW else summaryColW
                paint.color = Color.WHITE
                paint.textSize = 6.2f
                canvas.drawText(label, sumX + cw / 2f, y + 9.2f, paint)
                sumX += cw
            }
            paint.textAlign = Paint.Align.LEFT
            return y + totalH
        }

        currentY = drawMatrixHeader(currentY)

        val rowH = 12.2f
        val dayStartX = marginX + noColW + nameColW
        sortedEntries.forEachIndexed { idx, entry ->
            if (currentY + rowH > pageHeight - 36f) {
                finishCurrentCanvas()
                pageNumber++
                canvas = startNewCanvas(pageNumber)
                currentY = 26f
                currentY = drawMatrixHeader(currentY)
            }

            val rowTop = currentY
            paint.style = Paint.Style.FILL
            paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
            canvas.drawRect(marginX, rowTop, tableRight, rowTop + rowH, paint)

            val dayCodes = WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)
            val dayPins = WaliScheduleHelper.parseDayPins(entry.dayPinsJson)

            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 6.2f
            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText("${idx + 1}", marginX + noColW / 2f, rowTop + 8.8f, paint)

            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val displayName = if (entry.waliCode.isNotBlank()) "[${entry.waliCode}] ${entry.waliName}" else entry.waliName
            canvas.drawText(
                TextUtils.ellipsize(displayName, TextPaint(paint), nameColW - 6f, TextUtils.TruncateAt.END).toString(),
                marginX + noColW + 3f,
                rowTop + 8.8f,
                paint
            )

            for (d in 1..daysInMonth) {
                val cellX = dayStartX + (d - 1) * dayColW
                val rawCode = dayCodes[d]?.let { WaliScheduleHelper.normalizeCode(it) }.orEmpty()
                val pins = dayPins[d].orEmpty()
                if (rawCode.isNotBlank()) {
                    val shift = shiftMap[rawCode]
                    val baseColor = parseHexColor(shift?.colorHex ?: "#64748B", Color.rgb(100, 116, 139))
                    val tintedBg = Color.argb(
                        34,
                        Color.red(baseColor),
                        Color.green(baseColor),
                        Color.blue(baseColor)
                    )
                    paint.style = Paint.Style.FILL
                    paint.color = tintedBg
                    canvas.drawRect(cellX, rowTop, cellX + dayColW, rowTop + rowH, paint)

                    paint.textAlign = Paint.Align.CENTER
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = if (rawCode.length >= 4) 4.8f else 5.8f
                    paint.color = baseColor
                    canvas.drawText(rawCode.take(4), cellX + dayColW / 2f, rowTop + 8.8f, paint)
                }
                if (pins.isNotEmpty()) {
                    paint.style = Paint.Style.FILL
                    paint.color = if (pins.any { it.category == WaliScheduleHelper.PIN_FASKES }) {
                        Color.rgb(220, 38, 38)
                    } else {
                        Color.rgb(15, 118, 110)
                    }
                    canvas.drawCircle(cellX + dayColW - 2.3f, rowTop + 2.3f, 1.5f, paint)
                }
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(226, 232, 240)
                paint.strokeWidth = 0.4f
                canvas.drawRect(cellX, rowTop, cellX + dayColW, rowTop + rowH, paint)
            }

            val stats = WaliScheduleHelper.computeRowStats(entry, daysInMonth, shiftMap)
            val statVals = listOf(
                stats.pFull, stats.sCount, stats.mCount, stats.lpCount, stats.offCount,
                stats.p1Count, stats.p2Count, stats.p3Count, stats.p4Count, stats.totalJamKerja
            )
            var sumX = dayStartX + daysTotalW
            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 5.8f
            statVals.forEachIndexed { sIdx, valInt ->
                val cw = if (sIdx == statVals.lastIndex) jkColW else summaryColW
                paint.typeface = Typeface.create(
                    Typeface.DEFAULT,
                    if (sIdx == 0 || sIdx == statVals.lastIndex) Typeface.BOLD else Typeface.NORMAL
                )
                paint.color = if (sIdx == statVals.lastIndex) Color.rgb(15, 118, 110) else Color.rgb(30, 41, 59)
                canvas.drawText(valInt.toString(), sumX + cw / 2f, rowTop + 8.8f, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(226, 232, 240)
                paint.strokeWidth = 0.4f
                canvas.drawRect(sumX, rowTop, sumX + cw, rowTop + rowH, paint)
                paint.style = Paint.Style.FILL
                sumX += cw
            }

            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(203, 213, 225)
            paint.strokeWidth = 0.5f
            canvas.drawRect(marginX, rowTop, marginX + noColW + nameColW, rowTop + rowH, paint)
            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.LEFT
            currentY = rowTop + rowH
        }

        // Daily Breakdown Footer Rows (matching official November 2026 matrix sheet)
        val breakdownRows: List<Triple<String, Boolean, (DayShiftBreakdown) -> Int>> = listOf(
            Triple("(P1) 07:00 - 15:00", false) { it.p1 },
            Triple("(P2) 08:00 - 16:00", false) { it.p2 },
            Triple("(P3) 07:00 - 16:00 (Upacara)", false) { it.p3 },
            Triple("(P4) 07:00 - 20:00 (Kunjungan)", false) { it.p4 },
            Triple("(PAGI FULL)", true) { it.pagiFull },
            Triple("(S) TOTAL SORE (15:00-23:00)", true) { it.totalSore },
            Triple("(M) TOTAL MALAM (15:00-07:00)", true) { it.totalMalam },
            Triple("CUTI / IZIN", false) { it.cuti + it.izin },
            Triple("OFF / LIBUR + LEPAS", false) { it.offAndLepas },
            Triple(if (isEn) "TOTAL ASSIGNED" else "JUMLAH PERSONIL", true) { it.totalAssigned }
        )
        val bRowH = 10.5f
        val neededBreakdownH = breakdownRows.size * bRowH + 14f
        if (currentY + neededBreakdownH > pageHeight - 30f) {
            finishCurrentCanvas()
            pageNumber++
            canvas = startNewCanvas(pageNumber)
            currentY = 26f
            currentY = drawMatrixHeader(currentY)
        } else {
            currentY += 4f
        }

        val breakdowns = (1..daysInMonth).map { d -> WaliScheduleHelper.computeDayBreakdown(sortedEntries, d, shiftMap) }
        breakdownRows.forEach { (label, isBold, extractor) ->
            val rTop = currentY
            paint.style = Paint.Style.FILL
            paint.color = if (isBold) Color.rgb(240, 253, 250) else Color.rgb(248, 250, 252)
            canvas.drawRect(marginX, rTop, tableRight, rTop + bRowH, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
            paint.textSize = 5.8f
            paint.color = if (isBold) Color.rgb(15, 118, 110) else Color.rgb(51, 65, 85)
            canvas.drawText(label, marginX + 4f, rTop + 7.8f, paint)

            paint.textAlign = Paint.Align.CENTER
            breakdowns.forEachIndexed { idx, bd ->
                val cellX = dayStartX + idx * dayColW
                val count = extractor(bd)
                paint.color = if (count > 0 && isBold) Color.rgb(15, 23, 42) else Color.rgb(100, 116, 139)
                canvas.drawText(count.toString(), cellX + dayColW / 2f, rTop + 7.8f, paint)
            }
            paint.textAlign = Paint.Align.LEFT

            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(203, 213, 225)
            paint.strokeWidth = 0.45f
            canvas.drawRect(marginX, rTop, tableRight, rTop + bRowH, paint)
            paint.style = Paint.Style.FILL
            currentY = rTop + bRowH
        }

        // Petunjuk Kode Shift (Left) + Tanda Tangan Resmi (Right)
        currentY += 12f
        val footerHeightNeeded = 128f
        if (currentY + footerHeightNeeded > pageHeight - 16f) {
            finishCurrentCanvas()
            pageNumber++
            canvas = startNewCanvas(pageNumber)
            currentY = 30f
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8f
        paint.color = Color.rgb(15, 118, 110)
        canvas.drawText(
            if (isEn) "SHIFT CODE DIRECTORY & SOP LEGEND:" else "PETUNJUK KODE SHIFT & PENUGASAN SEKOLAH:",
            marginX,
            currentY + 10f,
            paint
        )
        var legendY = currentY + 21f
        val legendMaxX = pageWidth - 250f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 6.6f
        effectiveCodes.take(14).forEach { sc ->
            if (legendY < pageHeight - 24f) {
                val line = "• ${sc.code} (${sc.name} • ${sc.timeRange}): ${sc.sopNotes.ifBlank { sc.zoneLabel }}"
                paint.color = Color.rgb(51, 65, 85)
                canvas.drawText(
                    TextUtils.ellipsize(line, TextPaint(paint), legendMaxX - marginX, TextUtils.TruncateAt.END).toString(),
                    marginX,
                    legendY,
                    paint
                )
                legendY += 9f
            }
        }

        drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
        finishCurrentCanvas()
    }

    fun shareJadwalPdf(
        context: Context,
        pdfFile: File,
        subject: String,
        text: String
    ) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        val s = AppLanguageManager.getStrings(context).report
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    if (isEn) "Share Work Schedule PDF" else "Bagikan PDF Jadwal Wali Asuh"
                )
            )
        } catch (e: Exception) {
            Toast.makeText(context, s.pdfErrorToast(e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Membuat dokumen PDF Rekapitulasi Absensi Sholat 5 Waktu Sekolah / Asrama
     * (mendukung mode Harian per S·D·A·M·I maupun mode Rekap Bulanan per siswa).
     */
    fun generateSholatRecapPdf(
        context: Context,
        schoolProfile: SchoolProfile,
        students: List<ChildStudent>,
        sholatRecords: List<SholatRecord>,
        periodLabel: String,
        dateKeyOrMonthKey: String,
        isMonthly: Boolean,
        waliName: String = "Wali Asuh"
    ): File? {
        return try {
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var currentPage: PdfDocument.Page? = null

            renderSholatRecapPages(
                context = context,
                schoolProfile = schoolProfile,
                students = students,
                sholatRecords = sholatRecords,
                periodLabel = periodLabel,
                dateKeyOrMonthKey = dateKeyOrMonthKey,
                isMonthly = isMonthly,
                waliName = waliName,
                startNewCanvas = { num ->
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, num).create()
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
            val cleanKey = dateKeyOrMonthKey.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val file = File(outputDir, "Rekap_Sholat_${if (isMonthly) "Bulanan" else "Harian"}_$cleanKey.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    internal fun renderSholatRecapPages(
        context: Context,
        schoolProfile: SchoolProfile,
        students: List<ChildStudent>,
        sholatRecords: List<SholatRecord>,
        periodLabel: String,
        dateKeyOrMonthKey: String,
        isMonthly: Boolean,
        waliName: String = "Wali Asuh",
        startNewCanvas: (pageNumber: Int) -> Canvas,
        finishCanvas: () -> Unit = {}
    ) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        val pageWidth = 595
        val pageHeight = 842
        val marginX = 32f
        val contentRight = pageWidth - marginX
        val tableWidth = contentRight - marginX

        var pageNumber = 1
        var canvas = startNewCanvas(pageNumber)
        val paint = Paint().apply { isAntiAlias = true }

        var currentY = 34f
        currentY = drawHeader(
                context = context,
                canvas = canvas,
                paint = paint,
                width = pageWidth,
                startY = currentY,
                schoolProfile = schoolProfile,
                unitTitle = UNIT_BIMBINGAN_SPIRITUAL
            )

            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 12f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(
                if (isEn) {
                    if (isMonthly) "MONTHLY 5 DAILY PRAYERS ATTENDANCE RECAP" else "DAILY 5 DAILY PRAYERS ATTENDANCE RECAP"
                } else {
                    if (isMonthly) "REKAPITULASI BULANAN ABSENSI SHOLAT 5 WAKTU" else "REKAPITULASI HARIAN ABSENSI SHOLAT 5 WAKTU"
                },
                pageWidth / 2f,
                currentY,
                paint
            )
            currentY += 13f
            paint.textSize = 9.5f
            paint.color = Color.rgb(5, 150, 105)
            canvas.drawText(
                (if (isEn) "PERIOD: " else "PERIODE: ") + periodLabel.uppercase(Locale.getDefault()),
                pageWidth / 2f,
                currentY,
                paint
            )
            paint.textAlign = Paint.Align.LEFT
            currentY += 14f

            val scopedRecords = if (isMonthly) {
                sholatRecords.filter { it.dateKey.startsWith(dateKeyOrMonthKey) }
            } else {
                sholatRecords.filter { it.dateKey == dateKeyOrMonthKey }
            }
            val orderedStudents = students.sortedBy { it.name.lowercase() }
            val studentIds = orderedStudents.map { it.id }.toSet()
            val relevantRecords = scopedRecords.filter {
                it.studentId in studentIds || orderedStudents.any { st -> SholatHelper.matchesStudent(it, st) }
            }

            val totalJamaah = relevantRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.JAMAAH }
            val totalMunfarid = relevantRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.MUNFARID }
            val totalHaid = relevantRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.UDZUR_HAID }
            val totalSakitIzin = relevantRecords.count {
                val st = SholatStatus.fromCode(it.status)
                st == SholatStatus.SAKIT || st == SholatStatus.IZIN
            }
            val totalAlfa = relevantRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.ALFA }
            val wajibDenom = (relevantRecords.size - totalHaid).coerceAtLeast(0)
            val jamaahPct = if (wajibDenom > 0) ((totalJamaah * 100f) / wajibDenom).toInt().coerceIn(0, 100) else 0

            // Summary KPI Banner
            val kpiRect = RectF(marginX, currentY, contentRight, currentY + 42f)
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(236, 253, 245)
            canvas.drawRoundRect(kpiRect, 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(167, 243, 208)
            paint.strokeWidth = 1f
            canvas.drawRoundRect(kpiRect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            val kpiItems = listOf(
                (if (isEn) "Students" else "Siswa") to "${orderedStudents.size}",
                "Jamaah %" to "$jamaahPct%",
                "Jamaah" to "$totalJamaah",
                "Munfarid" to "$totalMunfarid",
                (if (isEn) "Udzur" else "Haid") to "$totalHaid",
                (if (isEn) "Sick/Ex" else "S/I") to "$totalSakitIzin",
                "Alfa" to "$totalAlfa"
            )
            val kpiColW = tableWidth / kpiItems.size
            kpiItems.forEachIndexed { idx, (lbl, v) ->
                val cx = marginX + idx * kpiColW + kpiColW / 2f
                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 7.5f
                paint.color = Color.rgb(6, 95, 70)
                canvas.drawText(lbl, cx, currentY + 15f, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 11f
                paint.color = if (lbl == "Alfa" && totalAlfa > 0) Color.rgb(185, 28, 28) else Color.rgb(15, 23, 42)
                canvas.drawText(v, cx, currentY + 32f, paint)
            }
            paint.textAlign = Paint.Align.LEFT
            currentY += 52f

            fun startNewPageWithSubtitle() {
                finishCanvas()
                pageNumber++
                canvas = startNewCanvas(pageNumber)
                currentY = 34f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 9f
                paint.color = Color.rgb(100, 116, 139)
                canvas.drawText(
                    "${if (isEn) "Prayer Attendance Recap" else "Rekap Sholat"} ($periodLabel) • Hal. $pageNumber",
                    marginX,
                    currentY,
                    paint
                )
                currentY += 14f
            }

            fun statusColor(st: SholatStatus): Int = when (st) {
                SholatStatus.JAMAAH -> Color.rgb(21, 128, 61)
                SholatStatus.MUNFARID -> Color.rgb(3, 105, 161)
                SholatStatus.UDZUR_HAID -> Color.rgb(190, 24, 93)
                SholatStatus.SAKIT -> Color.rgb(180, 83, 9)
                SholatStatus.IZIN -> Color.rgb(109, 40, 217)
                SholatStatus.ALFA -> Color.rgb(185, 28, 28)
            }

            if (!isMonthly) {
                // Daily Matrix: No | Nama | Kelas/Asrama | S | D | A | M | I | Ket
                val colWidths = floatArrayOf(24f, 155f, 105f, 34f, 34f, 34f, 34f, 34f, tableWidth - 454f)
                val headers = listOf("No", if (isEn) "Student" else "Nama Siswa", if (isEn) "Class / Dorm" else "Kelas / Asrama", "S", "D", "A", "M", "I", if (isEn) "Notes" else "Ket")

                fun drawDailyHeader(yPos: Float): Float {
                    val h = 20f
                    paint.style = Paint.Style.FILL
                    paint.color = Color.rgb(15, 118, 110)
                    canvas.drawRect(marginX, yPos, contentRight, yPos + h, paint)
                    var cx = marginX
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 8f
                    paint.color = Color.WHITE
                    headers.forEachIndexed { i, title ->
                        if (i in 3..7 || i == 0) {
                            paint.textAlign = Paint.Align.CENTER
                            canvas.drawText(title, cx + colWidths[i] / 2f, yPos + 13.5f, paint)
                        } else {
                            paint.textAlign = Paint.Align.LEFT
                            canvas.drawText(title, cx + 4f, yPos + 13.5f, paint)
                        }
                        cx += colWidths[i]
                    }
                    paint.textAlign = Paint.Align.LEFT
                    return yPos + h
                }

                currentY = drawDailyHeader(currentY)
                val dailyMatrix = SholatHelper.buildDailyMatrix(relevantRecords, dateKeyOrMonthKey)
                val rowH = 18f

                orderedStudents.forEachIndexed { idx, st ->
                    if (currentY + rowH > pageHeight - 110f) {
                        startNewPageWithSubtitle()
                        currentY = drawDailyHeader(currentY)
                    }
                    val stMap = dailyMatrix[st.id].orEmpty()
                    paint.style = Paint.Style.FILL
                    paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                    canvas.drawRect(marginX, currentY, contentRight, currentY + rowH, paint)

                    var cx = marginX
                    paint.textSize = 7.8f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.rgb(51, 65, 85)
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("${idx + 1}", cx + colWidths[0] / 2f, currentY + 12.5f, paint)
                    cx += colWidths[0]

                    paint.textAlign = Paint.Align.LEFT
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = Color.rgb(15, 23, 42)
                    val clippedName = TextUtils.ellipsize(st.name, TextPaint(paint), colWidths[1] - 8f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(clippedName, cx + 4f, currentY + 12.5f, paint)
                    cx += colWidths[1]

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.rgb(71, 85, 105)
                    val dormClass = listOf(st.classGrade, st.dormitory).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "-" }
                    val clippedDorm = TextUtils.ellipsize(dormClass, TextPaint(paint), colWidths[2] - 8f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(clippedDorm, cx + 4f, currentY + 12.5f, paint)
                    cx += colWidths[2]

                    val notesParts = mutableListOf<String>()
                    SholatWaktu.entries.forEachIndexed { wIdx, w ->
                        val rec = stMap[w]
                        paint.textAlign = Paint.Align.CENTER
                        if (rec != null) {
                            val stEnum = SholatStatus.fromCode(rec.status)
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            paint.color = statusColor(stEnum)
                            canvas.drawText(stEnum.shortCode, cx + colWidths[3 + wIdx] / 2f, currentY + 12.5f, paint)
                            if (rec.notes.isNotBlank()) notesParts.add("${w.shortCode}:${rec.notes}")
                        } else {
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                            paint.color = Color.rgb(148, 163, 184)
                            canvas.drawText("-", cx + colWidths[3 + wIdx] / 2f, currentY + 12.5f, paint)
                        }
                        cx += colWidths[3 + wIdx]
                    }

                    paint.textAlign = Paint.Align.LEFT
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.rgb(71, 85, 105)
                    val noteSummary = notesParts.joinToString("; ").ifBlank {
                        val logged = stMap.size
                        "$logged/5"
                    }
                    val clippedNote = TextUtils.ellipsize(noteSummary, TextPaint(paint), colWidths[8] - 6f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(clippedNote, cx + 4f, currentY + 12.5f, paint)

                    paint.style = Paint.Style.STROKE
                    paint.color = Color.rgb(226, 232, 240)
                    paint.strokeWidth = 0.5f
                    canvas.drawRect(marginX, currentY, contentRight, currentY + rowH, paint)
                    paint.style = Paint.Style.FILL
                    currentY += rowH
                }

                currentY += 8f
                paint.textSize = 7.5f
                paint.color = Color.rgb(71, 85, 105)
                canvas.drawText(
                    if (isEn) {
                        "Legend: S=Fajr D=Dhuhr A=Asr M=Maghrib I=Isha | J=Congregation M=Individual H=Period Excused S=Sick I=Excused A=Absent"
                    } else {
                        "Keterangan: S=Subuh D=Dzuhur A=Ashar M=Maghrib I=Isya | J=Jamaah M=Munfarid H=Udzur Haid S=Sakit I=Izin A=Alfa"
                    },
                    marginX,
                    currentY,
                    paint
                )
                currentY += 12f
            } else {
                // Monthly Matrix: No | Nama | Kelas | Total | Jam | Munf | Haid | S/I | Alfa | %Jam
                val colWidths = floatArrayOf(24f, 150f, 95f, 36f, 36f, 36f, 36f, 36f, 36f, tableWidth - 485f)
                val headers = listOf(
                    "No",
                    if (isEn) "Student" else "Nama Siswa",
                    if (isEn) "Class/Dorm" else "Kelas/Asrama",
                    "Tot",
                    "Jam",
                    "Munf",
                    "Haid",
                    "S/I",
                    "Alfa",
                    "%Jam"
                )

                fun drawMonthlyHeader(yPos: Float): Float {
                    val h = 20f
                    paint.style = Paint.Style.FILL
                    paint.color = Color.rgb(15, 118, 110)
                    canvas.drawRect(marginX, yPos, contentRight, yPos + h, paint)
                    var cx = marginX
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 7.8f
                    paint.color = Color.WHITE
                    headers.forEachIndexed { i, title ->
                        if (i == 1 || i == 2) {
                            paint.textAlign = Paint.Align.LEFT
                            canvas.drawText(title, cx + 4f, yPos + 13.5f, paint)
                        } else {
                            paint.textAlign = Paint.Align.CENTER
                            canvas.drawText(title, cx + colWidths[i] / 2f, yPos + 13.5f, paint)
                        }
                        cx += colWidths[i]
                    }
                    paint.textAlign = Paint.Align.LEFT
                    return yPos + h
                }

                currentY = drawMonthlyHeader(currentY)
                val analytics = SholatHelper.computeStudentAnalytics(orderedStudents, relevantRecords, dateKeyOrMonthKey)
                val rowH = 18f

                analytics.forEachIndexed { idx, row ->
                    if (currentY + rowH > pageHeight - 110f) {
                        startNewPageWithSubtitle()
                        currentY = drawMonthlyHeader(currentY)
                    }
                    paint.style = Paint.Style.FILL
                    paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                    canvas.drawRect(marginX, currentY, contentRight, currentY + rowH, paint)

                    var cx = marginX
                    paint.textSize = 7.8f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.rgb(51, 65, 85)
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("${idx + 1}", cx + colWidths[0] / 2f, currentY + 12.5f, paint)
                    cx += colWidths[0]

                    paint.textAlign = Paint.Align.LEFT
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = Color.rgb(15, 23, 42)
                    val clippedName = TextUtils.ellipsize(row.student.name, TextPaint(paint), colWidths[1] - 8f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(clippedName, cx + 4f, currentY + 12.5f, paint)
                    cx += colWidths[1]

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.rgb(71, 85, 105)
                    val dormClass = listOf(row.student.classGrade, row.student.dormitory).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "-" }
                    val clippedDorm = TextUtils.ellipsize(dormClass, TextPaint(paint), colWidths[2] - 8f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(clippedDorm, cx + 4f, currentY + 12.5f, paint)
                    cx += colWidths[2]

                    val nums = listOf(
                        row.totalLogged,
                        row.jamaahCount,
                        row.munfaridCount,
                        row.udzurHaidCount,
                        row.sakitCount + row.izinCount,
                        row.alfaCount
                    )
                    nums.forEachIndexed { nIdx, value ->
                        paint.textAlign = Paint.Align.CENTER
                        paint.typeface = Typeface.create(
                            Typeface.DEFAULT,
                            if (nIdx == 1 || (nIdx == 5 && value > 0)) Typeface.BOLD else Typeface.NORMAL
                        )
                        paint.color = when {
                            nIdx == 1 && value > 0 -> Color.rgb(21, 128, 61)
                            nIdx == 3 && value > 0 -> Color.rgb(190, 24, 93)
                            nIdx == 5 && value > 0 -> Color.rgb(185, 28, 28)
                            else -> Color.rgb(30, 41, 59)
                        }
                        canvas.drawText(value.toString(), cx + colWidths[3 + nIdx] / 2f, currentY + 12.5f, paint)
                        cx += colWidths[3 + nIdx]
                    }

                    paint.textAlign = Paint.Align.CENTER
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = if (row.jamaahRatePercent >= 80) Color.rgb(5, 150, 105) else Color.rgb(15, 23, 42)
                    canvas.drawText("${row.jamaahRatePercent}%", cx + colWidths[9] / 2f, currentY + 12.5f, paint)
                    paint.textAlign = Paint.Align.LEFT

                    paint.style = Paint.Style.STROKE
                    paint.color = Color.rgb(226, 232, 240)
                    paint.strokeWidth = 0.5f
                    canvas.drawRect(marginX, currentY, contentRight, currentY + rowH, paint)
                    paint.style = Paint.Style.FILL
                    currentY += rowH
                }
                currentY += 12f
            }

            if (currentY > pageHeight - 135f) {
                startNewPageWithSubtitle()
            }
            drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
            finishCanvas()
    }

    /**
     * Membuat dokumen PDF Rapor Absensi Sholat 5 Waktu Per Siswa untuk periode bulanan.
     */
    fun generateSholatStudentReportPdf(
        context: Context,
        schoolProfile: SchoolProfile,
        student: ChildStudent,
        sholatRecords: List<SholatRecord>,
        monthKey: String,
        waliName: String = "Wali Asuh"
    ): File? {
        return try {
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var currentPage: PdfDocument.Page? = null

            renderSholatStudentReportPages(
                context = context,
                schoolProfile = schoolProfile,
                student = student,
                sholatRecords = sholatRecords,
                monthKey = monthKey,
                waliName = waliName,
                startNewCanvas = { num ->
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, num).create()
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
            val cleanMonth = monthKey.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val file = File(outputDir, "Rapor_Sholat_${cleanStudent}_$cleanMonth.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    internal fun renderSholatStudentReportPages(
        context: Context,
        schoolProfile: SchoolProfile,
        student: ChildStudent,
        sholatRecords: List<SholatRecord>,
        monthKey: String,
        waliName: String = "Wali Asuh",
        startNewCanvas: (pageNumber: Int) -> Canvas,
        finishCanvas: () -> Unit = {}
    ) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        val pageWidth = 595
        val pageHeight = 842
        val marginX = 35f
        val contentRight = pageWidth - marginX

        var pageNumber = 1
        var canvas = startNewCanvas(pageNumber)
        val paint = Paint().apply { isAntiAlias = true }

        var currentY = 34f
        currentY = drawHeader(
                context = context,
                canvas = canvas,
                paint = paint,
                width = pageWidth,
                startY = currentY,
                schoolProfile = schoolProfile,
                unitTitle = UNIT_BIMBINGAN_SPIRITUAL
            )

            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 12f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(
                if (isEn) "STUDENT 5 DAILY PRAYERS REPORT" else "RAPOR PEMBINAAN SHOLAT 5 WAKTU ANAK ASUH",
                pageWidth / 2f,
                currentY,
                paint
            )
            currentY += 13f
            val isAllPeriod = monthKey.isBlank() || monthKey.equals("ALL", ignoreCase = true)
            paint.textSize = 9.5f
            paint.color = Color.rgb(5, 150, 105)
            val periodDisplay = if (isAllPeriod) {
                if (isEn) "All History" else "Semua Riwayat"
            } else {
                SholatHelper.formatMonthKeyDisplay(monthKey, isEn)
            }
            canvas.drawText(
                (if (isAllPeriod) {
                    if (isEn) "PERIOD: " else "PERIODE: "
                } else {
                    if (isEn) "MONTH: " else "BULAN: "
                }) + periodDisplay.uppercase(Locale.getDefault()),
                pageWidth / 2f,
                currentY,
                paint
            )
            paint.textAlign = Paint.Align.LEFT
            currentY += 14f

            currentY = drawStudentBio(canvas, paint, pageWidth, currentY, student, isEn)

            val studentRecords = SholatHelper.recordsForStudent(sholatRecords, student)
                .let { list -> if (isAllPeriod) list else list.filter { it.dateKey.startsWith(monthKey) } }
            val stats = SholatHelper.computeStudentAnalytics(
                students = listOf(student),
                records = studentRecords,
                monthKey = if (isAllPeriod) null else monthKey
            ).first()

            val kpiRect = RectF(marginX, currentY, contentRight, currentY + 42f)
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(236, 253, 245)
            canvas.drawRoundRect(kpiRect, 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(167, 243, 208)
            paint.strokeWidth = 1f
            canvas.drawRoundRect(kpiRect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            val kpis = listOf(
                "Jamaah %" to "${stats.jamaahRatePercent}%",
                "Jamaah" to "${stats.jamaahCount}",
                "Subuh Jam." to "${stats.subuhJamaahCount}",
                "Munfarid" to "${stats.munfaridCount}",
                "Udzur Haid" to "${stats.udzurHaidCount}",
                "Sakit/Izin" to "${stats.sakitCount + stats.izinCount}",
                "Alfa" to "${stats.alfaCount}"
            )
            val colW = (contentRight - marginX) / kpis.size
            kpis.forEachIndexed { idx, (lbl, v) ->
                val cx = marginX + idx * colW + colW / 2f
                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 7.3f
                paint.color = Color.rgb(6, 95, 70)
                canvas.drawText(lbl, cx, currentY + 15f, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 10.5f
                paint.color = if (lbl == "Alfa" && stats.alfaCount > 0) Color.rgb(185, 28, 28) else Color.rgb(15, 23, 42)
                canvas.drawText(v, cx, currentY + 32f, paint)
            }
            paint.textAlign = Paint.Align.LEFT
            currentY += 54f

            val dateGroups = studentRecords.groupBy { it.dateKey }.toSortedMap()
            val tWidths = floatArrayOf(100f, 52f, 52f, 52f, 52f, 52f, (contentRight - marginX) - 360f)
            val tHeaders = listOf(
                if (isEn) "Date" else "Tanggal",
                SholatWaktu.SUBUH.label(isEn),
                SholatWaktu.DZUHUR.label(isEn),
                SholatWaktu.ASHAR.label(isEn),
                SholatWaktu.MAGHRIB.label(isEn),
                SholatWaktu.ISYA.label(isEn),
                if (isEn) "Supervisor Notes" else "Catatan Pembina"
            )

            fun drawTableHeader(yPos: Float): Float {
                val h = 20f
                paint.style = Paint.Style.FILL
                paint.color = Color.rgb(15, 118, 110)
                canvas.drawRect(marginX, yPos, contentRight, yPos + h, paint)
                var cx = marginX
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8f
                paint.color = Color.WHITE
                tHeaders.forEachIndexed { i, title ->
                    if (i in 1..5) {
                        paint.textAlign = Paint.Align.CENTER
                        canvas.drawText(title, cx + tWidths[i] / 2f, yPos + 13.5f, paint)
                    } else {
                        paint.textAlign = Paint.Align.LEFT
                        canvas.drawText(title, cx + 5f, yPos + 13.5f, paint)
                    }
                    cx += tWidths[i]
                }
                paint.textAlign = Paint.Align.LEFT
                return yPos + h
            }

            if (dateGroups.isEmpty()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 9.5f
                paint.color = Color.rgb(100, 116, 139)
                canvas.drawText(
                    if (isEn) {
                        if (isAllPeriod) "No prayer attendance logs recorded yet." else "No prayer attendance logs recorded for this month."
                    } else {
                        if (isAllPeriod) "Belum ada catatan absensi sholat." else "Belum ada catatan absensi sholat pada bulan ini."
                    },
                    marginX,
                    currentY + 12f,
                    paint
                )
                currentY += 32f
            } else {
                currentY = drawTableHeader(currentY)
                val rowH = 18f
                dateGroups.entries.forEachIndexed { idx, (dateKey, dayList) ->
                    if (currentY + rowH > pageHeight - 125f) {
                        finishCanvas()
                        pageNumber++
                        canvas = startNewCanvas(pageNumber)
                        currentY = 34f
                        currentY = drawTableHeader(currentY)
                    }

                    val byWaktu = dayList.associateBy { SholatWaktu.fromCode(it.waktu) }
                    paint.style = Paint.Style.FILL
                    paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                    canvas.drawRect(marginX, currentY, contentRight, currentY + rowH, paint)

                    var cx = marginX
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 7.8f
                    paint.color = Color.rgb(15, 23, 42)
                    canvas.drawText(SholatHelper.formatDateKeyDisplay(dateKey, isEn), cx + 5f, currentY + 12.5f, paint)
                    cx += tWidths[0]

                    val dayNotes = mutableListOf<String>()
                    SholatWaktu.entries.forEachIndexed { wIdx, w ->
                        val rec = byWaktu[w]
                        paint.textAlign = Paint.Align.CENTER
                        if (rec != null) {
                            val stEnum = SholatStatus.fromCode(rec.status)
                            val tag = SholatDisciplineTag.fromCode(rec.disciplineTag)
                            val cellLabel = when {
                                stEnum == SholatStatus.JAMAAH && tag == SholatDisciplineTag.MASBUK -> "J*"
                                stEnum == SholatStatus.JAMAAH && tag == SholatDisciplineTag.SAF_DEPAN -> "J+"
                                else -> stEnum.shortCode
                            }
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            paint.color = when (stEnum) {
                                SholatStatus.JAMAAH -> Color.rgb(21, 128, 61)
                                SholatStatus.MUNFARID -> Color.rgb(3, 105, 161)
                                SholatStatus.UDZUR_HAID -> Color.rgb(190, 24, 93)
                                SholatStatus.SAKIT -> Color.rgb(180, 83, 9)
                                SholatStatus.IZIN -> Color.rgb(109, 40, 217)
                                SholatStatus.ALFA -> Color.rgb(185, 28, 28)
                            }
                            canvas.drawText(cellLabel, cx + tWidths[1 + wIdx] / 2f, currentY + 12.5f, paint)
                            if (rec.notes.isNotBlank()) dayNotes.add("${w.shortCode}:${rec.notes}")
                        } else {
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                            paint.color = Color.rgb(148, 163, 184)
                            canvas.drawText("-", cx + tWidths[1 + wIdx] / 2f, currentY + 12.5f, paint)
                        }
                        cx += tWidths[1 + wIdx]
                    }

                    paint.textAlign = Paint.Align.LEFT
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.rgb(71, 85, 105)
                    val noteText = dayNotes.joinToString("; ").ifBlank { "-" }
                    val clippedNote = TextUtils.ellipsize(noteText, TextPaint(paint), tWidths[6] - 8f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(clippedNote, cx + 5f, currentY + 12.5f, paint)

                    paint.style = Paint.Style.STROKE
                    paint.color = Color.rgb(226, 232, 240)
                    paint.strokeWidth = 0.5f
                    canvas.drawRect(marginX, currentY, contentRight, currentY + rowH, paint)
                    paint.style = Paint.Style.FILL
                    currentY += rowH
                }
                currentY += 8f
                paint.textSize = 7.4f
                paint.color = Color.rgb(100, 116, 139)
                canvas.drawText(
                    if (isEn) {
                        "J=Congregation (J+=Front Row, J*=Masbuq) • M=Individual • H=Period Excused • S=Sick • I=Excused • A=Absent"
                    } else {
                        "J=Jamaah (J+=Saf Depan, J*=Masbuk) • M=Munfarid • H=Udzur Syar'i (Haid) • S=Sakit • I=Izin • A=Alfa"
                    },
                    marginX,
                    currentY,
                    paint
                )
                currentY += 12f
            }

            if (currentY > pageHeight - 135f) {
                finishCanvas()
                pageNumber++
                canvas = startNewCanvas(pageNumber)
                currentY = 34f
            }
            drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
            finishCanvas()
    }

    fun shareSholatPdf(
        context: Context,
        pdfFile: File,
        subject: String,
        text: String
    ) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        val s = AppLanguageManager.getStrings(context).report
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    if (isEn) "Share Prayer Attendance PDF" else "Bagikan PDF Absensi Sholat"
                )
            )
        } catch (e: Exception) {
            Toast.makeText(context, s.pdfErrorToast(e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Membuat dokumen PDF Rekap Laporan Kegiatan Ibadah Agama Kristen
     * (Doa Ibadah Pagi, Doa Ibadah Malam, Kegiatan ke Gereja).
     */
    fun generateChristianWorshipPdf(
        context: Context,
        records: List<ChristianWorshipRecord>,
        students: List<ChildStudent>,
        schoolProfile: SchoolProfile,
        filterTitle: String = "",
        waliName: String = "Wali Asuh"
    ): File? {
        return try {
            val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val marginX = 35f
            val contentRight = pageWidth - marginX
            var pageNumber = 1
            var currentPage = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            var canvas = currentPage.canvas
            val paint = Paint().apply { isAntiAlias = true }

            var currentY = 34f
            currentY = drawHeader(
                context = context,
                canvas = canvas,
                paint = paint,
                width = pageWidth,
                startY = currentY,
                schoolProfile = schoolProfile,
                unitTitle = UNIT_BIMBINGAN_SPIRITUAL
            )

            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 12f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(
                if (isEn) "CHRISTIAN WORSHIP & PRAYER ACTIVITY REPORT" else "REKAP LAPORAN KEGIATAN IBADAH AGAMA KRISTEN",
                pageWidth / 2f,
                currentY,
                paint
            )
            currentY += 13f
            if (filterTitle.isNotBlank()) {
                paint.textSize = 9.5f
                paint.color = Color.rgb(3, 105, 161)
                canvas.drawText(filterTitle.uppercase(Locale.getDefault()), pageWidth / 2f, currentY, paint)
                currentY += 14f
            } else {
                currentY += 6f
            }
            paint.textAlign = Paint.Align.LEFT

            val activeRecords = records.filter { !it.isDeleted }
                .sortedWith(compareByDescending<ChristianWorshipRecord> { it.activityDate }.thenByDescending { it.createdAt })
            val pagiCount = activeRecords.count { ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_PAGI }
            val malamCount = activeRecords.count { ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_MALAM }
            val gerejaCount = activeRecords.count { ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_KE_GEREJA }
            val pembinaanCount = activeRecords.count { ChristianWorshipHelper.isExtraType(it.activityType) }
            val photoCount = activeRecords.count { ChristianWorshipHelper.hasAnyPhoto(it) }

            val kpiRect = RectF(marginX, currentY, contentRight, currentY + 42f)
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(240, 249, 255)
            canvas.drawRoundRect(kpiRect, 6f, 6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(186, 230, 253)
            paint.strokeWidth = 1f
            canvas.drawRoundRect(kpiRect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            val kpis = listOf(
                (if (isEn) "Christian Kids" else "Anak Kristen") to "${students.size}",
                (if (isEn) "Morning" else "Ibadah Pagi") to "$pagiCount",
                (if (isEn) "Evening" else "Ibadah Malam") to "$malamCount",
                (if (isEn) "Church" else "Gereja") to "$gerejaCount",
                (if (isEn) "Verse/Prayer" else "Pembinaan") to "$pembinaanCount",
                (if (isEn) "Photos" else "Bukti Foto") to "$photoCount"
            )
            val colW = (contentRight - marginX) / kpis.size
            kpis.forEachIndexed { idx, (lbl, v) ->
                val cx = marginX + idx * colW + colW / 2f
                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 7.3f
                paint.color = Color.rgb(3, 105, 161)
                canvas.drawText(lbl, cx, currentY + 15f, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 10.5f
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText(v, cx, currentY + 32f, paint)
            }
            paint.textAlign = Paint.Align.LEFT
            currentY += 54f

            val tWidths = floatArrayOf(68f, 118f, 84f, 115f, 80f, (contentRight - marginX) - 465f)
            val tHeaders = listOf(
                if (isEn) "Date" else "Tanggal",
                if (isEn) "Student" else "Nama Anak",
                if (isEn) "Activity" else "Jenis Ibadah",
                if (isEn) "Theme / Notes" else "Tema & Catatan",
                if (isEn) "Caregiver" else "Pembina",
                if (isEn) "Photo" else "Foto"
            )

            fun drawTableHeader(yPos: Float): Float {
                val h = 20f
                paint.style = Paint.Style.FILL
                paint.color = Color.rgb(3, 105, 161)
                canvas.drawRect(marginX, yPos, contentRight, yPos + h, paint)
                var cx = marginX
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8f
                paint.color = Color.WHITE
                tHeaders.forEachIndexed { i, title ->
                    canvas.drawText(title, cx + 4f, yPos + 13.5f, paint)
                    cx += tWidths[i]
                }
                return yPos + h
            }

            if (activeRecords.isEmpty()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 9.5f
                paint.color = Color.rgb(100, 116, 139)
                canvas.drawText(
                    if (isEn) "No Christian worship activities recorded for this filter."
                    else "Belum ada catatan kegiatan ibadah Agama Kristen pada filter ini.",
                    marginX,
                    currentY + 12f,
                    paint
                )
                currentY += 32f
            } else {
                currentY = drawTableHeader(currentY)
                val rowH = 20f
                val dateFmt = SimpleDateFormat("dd/MM/yy", Locale.US)
                activeRecords.forEachIndexed { idx, rec ->
                    if (currentY + rowH > pageHeight - 125f) {
                        document.finishPage(currentPage)
                        pageNumber++
                        currentPage = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                        canvas = currentPage.canvas
                        currentY = 34f
                        currentY = drawTableHeader(currentY)
                    }
                    paint.style = Paint.Style.FILL
                    paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                    canvas.drawRect(marginX, currentY, contentRight, currentY + rowH, paint)

                    val dateText = dateFmt.format(Date(rec.activityDate))
                    val nameText = if (rec.studentClass.isNotBlank()) "${rec.studentName} (${rec.studentClass})" else rec.studentName
                    val actLabel = ChristianWorshipHelper.activityShortLabel(rec.activityType, isEn)
                    val themeNote = listOfNotNull(
                        rec.title.takeIf { it.isNotBlank() },
                        rec.notes.takeIf { it.isNotBlank() }
                    ).joinToString(" - ").ifBlank { "-" }
                    val photoLabel = when {
                        ChristianWorshipHelper.isProofActive(rec) -> if (isEn) "Active" else "Ada Foto"
                        rec.isMediaPurgedFromServer -> if (isEn) "Expired" else "Arsip"
                        else -> "-"
                    }
                    val rowCells = listOf(dateText, nameText, actLabel, themeNote, rec.waliName, photoLabel)
                    var cx = marginX
                    paint.textSize = 7.7f
                    rowCells.forEachIndexed { cIdx, cellVal ->
                        paint.typeface = Typeface.create(
                            Typeface.DEFAULT,
                            if (cIdx == 1 || cIdx == 2) Typeface.BOLD else Typeface.NORMAL
                        )
                        paint.color = if (cIdx == 2) Color.rgb(3, 105, 161) else Color.rgb(15, 23, 42)
                        val ellipsized = TextUtils.ellipsize(
                            cellVal,
                            TextPaint(paint),
                            tWidths[cIdx] - 7f,
                            TextUtils.TruncateAt.END
                        ).toString()
                        canvas.drawText(ellipsized, cx + 4f, currentY + 13f, paint)
                        cx += tWidths[cIdx]
                    }

                    paint.style = Paint.Style.STROKE
                    paint.color = Color.rgb(226, 232, 240)
                    paint.strokeWidth = 0.5f
                    canvas.drawRect(marginX, currentY, contentRight, currentY + rowH, paint)
                    paint.style = Paint.Style.FILL
                    currentY += rowH
                }
                currentY += 12f
            }

            if (currentY > pageHeight - 135f) {
                document.finishPage(currentPage)
                pageNumber++
                currentPage = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = currentPage.canvas
                currentY = 34f
            }
            drawFooterSignatures(context, canvas, paint, pageWidth, pageHeight, currentY, waliName, schoolProfile)
            document.finishPage(currentPage)

            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val file = File(outputDir, "Rekap_Ibadah_Kristen_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
                out.flush()
            }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareChristianWorshipPdf(
        context: Context,
        pdfFile: File,
        subject: String,
        text: String
    ) {
        val isEn = AppLanguageManager.getLanguage(context) == AppLanguage.ENGLISH
        val s = AppLanguageManager.getStrings(context).report
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    if (isEn) "Share Christian Worship Report PDF" else "Bagikan PDF Rekap Ibadah Kristen"
                )
            )
        } catch (e: Exception) {
            Toast.makeText(context, s.pdfErrorToast(e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }
}
