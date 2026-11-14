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
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.SchoolProfile
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.AppLanguageManager
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

    private fun drawHeader(
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

    private fun drawStudentBio(
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
                    val waliCell = TextUtils.ellipsize(rec.waliName.ifBlank { waliName }, TextPaint(paint), 124f, TextUtils.TruncateAt.END).toString()
                    canvas.drawText(waliCell, colWali + 5f, textY, paint)

                    currentY = rTop + rowHeight
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
}
