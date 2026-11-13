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
import android.text.TextPaint
import android.text.TextUtils
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolProfile
import java.io.File
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
     * Membuat dokumen PDF resmi Rekap Screening Kesehatan Siswa Sekolah Rakyat SRT 1 Kediri.
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
            currentY = drawTitleAndPeriod(canvas, paint, pageWidth, currentY, periodLabel)

            // 3. Biodata Identitas Anak Asuh (Primary)
            currentY = drawStudentBio(canvas, paint, pageWidth, currentY, student)

            // 4. Ringkasan Eksekutif Frekuensi Sakit / Screening
            currentY = drawScreeningSummary(canvas, paint, pageWidth, currentY, records, periodLabel)

            // 5. Daftar Riwayat Kunjungan / Tindakan Medis
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText("RINCIAN PEMERIKSAAN & TINDAKAN MEDIS (${records.size} Kunjungan):", 35f, currentY, paint)
            currentY += 14f

            if (records.isEmpty()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 10f
                paint.color = Color.rgb(100, 116, 139)
                canvas.drawText("Tidak ada catatan riwayat sakit / berobat pada periode ini (Kondisi Sehat).", 40f, currentY + 12f, paint)
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
                        canvas.drawText("Rekap Screening Kesehatan: ${student.name} (Halaman $pageNumber)", 35f, currentY, paint)
                        currentY += 16f
                    }

                    currentY = drawRecordItem(canvas, paint, pageWidth, currentY, index + 1, record)
                    currentY += 10f
                }
            }

            // 6. Tanda Tangan & Validasi
            if (currentY > pageHeight - 110f) {
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

    internal fun buildHeaderTitle(schoolProfile: SchoolProfile): String {
        return schoolProfile.schoolName.trim()
            .ifBlank { schoolProfile.shortName.trim().ifBlank { "WALI ASUH CARE" } }
            .uppercase(Locale("id", "ID"))
    }

    internal fun buildSubHeaderLine(schoolProfile: SchoolProfile): String {
        val referralFaskes = schoolProfile.getAllFaskes().drop(1)
        return when {
            referralFaskes.isNotEmpty() -> "Kerjasama Fasilitas Kesehatan: ${referralFaskes.joinToString(" & ")}"
            schoolProfile.city.isNotBlank() -> "Wilayah Operasional: ${schoolProfile.city}"
            else -> "Layanan Kesehatan Anak Asuh & Pesantren / Asrama"
        }
    }

    internal fun buildFooterSignatureTitle(schoolProfile: SchoolProfile): String {
        val schoolSignLabel = schoolProfile.shortName.trim().ifBlank { schoolProfile.schoolName.trim() }
        val signTitle = if (schoolSignLabel.isNotBlank()) "Wali Asuh Kesehatan $schoolSignLabel," else "Wali Asuh Kesehatan,"
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
        schoolProfile: SchoolProfile = SchoolProfile()
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
        canvas.drawText("UNIT LAYANAN KESEHATAN SANTRI & ANAK ASUH (UKSA)", width / 2f, y, paint)

        y += 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        paint.color = Color.rgb(100, 116, 139)
        val subHeaderToDraw = TextUtils.ellipsize(
            buildSubHeaderLine(schoolProfile),
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

    private fun drawTitleAndPeriod(canvas: Canvas, paint: Paint, width: Int, startY: Float, period: String): Float {
        var y = startY
        paint.textAlign = Paint.Align.CENTER

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText("LEMBAR REKAPITULASI SCREENING & REKAM MEDIS ANAK ASUH", width / 2f, y, paint)

        y += 13f
        paint.textSize = 10f
        paint.color = Color.rgb(2, 132, 199)
        canvas.drawText("PERIODE LAPORAN: ${period.uppercase(Locale("id", "ID"))}", width / 2f, y, paint)

        paint.textAlign = Paint.Align.LEFT
        return y + 14f
    }

    private fun drawStudentBio(canvas: Canvas, paint: Paint, width: Int, startY: Float, student: ChildStudent): Float {
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
        canvas.drawText("Nama Anak Asuh :", boxLeft + 10f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(15, 118, 110)
        canvas.drawText(student.name, boxLeft + 95f, y, paint)

        // Kolom Kanan
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText("Asrama Santri :", width / 2f + 10f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(student.dormitory, width / 2f + 85f, y, paint)

        y += 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawText("NIS / ID Siswa   : ${student.nis}", boxLeft + 10f, y, paint)
        canvas.drawText("Kelas Pendidikan: ${student.classGrade}", width / 2f + 10f, y, paint)

        y += 14f
        val genderStr = if (student.gender.equals("P", ignoreCase = true)) "Perempuan (Putri)" else "Laki-laki (Putra)"
        canvas.drawText("Jenis Kelamin     : $genderStr", boxLeft + 10f, y, paint)
        canvas.drawText("Golongan Darah : ${student.bloodType}  |  Usia: ${student.age} Th", width / 2f + 10f, y, paint)

        return startY + boxHeight + 12f
    }

    private fun drawScreeningSummary(
        canvas: Canvas,
        paint: Paint,
        width: Int,
        startY: Float,
        records: List<MedicalRecord>,
        period: String
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
        val alertText = if (isMultiSickness) {
            "PERHATIAN: Anak asuh ini tercatat ${records.size} KALI SAKIT / BEROBAT dalam periode $period."
        } else if (records.size == 1) {
            "STATUS: Tercatat 1 kali kunjungan pengobatan dalam periode $period."
        } else {
            "STATUS: Anak asuh dalam kondisi SEHAT (0 kali berobat) dalam periode $period."
        }
        canvas.drawText(alertText, boxLeft + 10f, y, paint)

        y += 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(71, 85, 105)
        val controlCount = records.count { it.needsControl }
        canvas.drawText("Total Riwayat Screening Terdaftar: ${records.size} Data  •  Jadwal Kontrol Terbit: $controlCount Kali", boxLeft + 10f, y, paint)

        return startY + boxHeight + 14f
    }

    private fun drawRecordItem(
        canvas: Canvas,
        paint: Paint,
        width: Int,
        startY: Float,
        number: Int,
        record: MedicalRecord
    ): Float {
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
        val dateStr = record.treatmentDate?.let { DateUtils.formatFullDate(it) }
            ?: DateUtils.formatFullDate(record.planDate)

        var y = startY + 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        paint.color = Color.rgb(15, 118, 110)
        canvas.drawText("Kunjungan #$number  •  $dateStr (${record.departureTime} WIB)", boxLeft + 10f, y, paint)

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
        canvas.drawText("• Keluhan Awal   : ${record.complaint}", boxLeft + 10f, y, paint)

        y += 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(2, 132, 199)
        canvas.drawText("• Diagnosa Dokter: ${record.diagnosis.ifBlank { "Pemeriksaan umum puskesmas" }}", boxLeft + 10f, y, paint)

        y += 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(51, 65, 85)
        canvas.drawText("• Tindakan Medis: ${record.actionsTaken.ifBlank { "Pemeriksaan dan observasi" }}", boxLeft + 10f, y, paint)

        y += 11f
        paint.color = Color.rgb(109, 40, 217)
        canvas.drawText("• Resep / Obat    : ${record.prescription.ifBlank { "Tidak ada resep obat khusus" }}", boxLeft + 10f, y, paint)

        y += 11f
        paint.color = if (record.needsControl) Color.rgb(180, 83, 9) else Color.rgb(100, 116, 139)
        val controlInfo = if (record.needsControl) {
            "Kontrol Ulang: ${record.controlDate?.let { DateUtils.formatShortDate(it) } ?: "Terjadwal"} (${if (record.isControlCompleted) "Selesai" else "Belum Kontrol"})"
        } else {
            "Kontrol: Tidak Perlu Kontrol"
        }
        val escortInfo = if (record.escortName.isNotBlank()) " | Pendamping: ${record.escortName}" else ""
        canvas.drawText("• Status Kontrol  : $controlInfo$escortInfo", boxLeft + 10f, y, paint)

        return startY + boxHeight
    }

    private fun drawFooterSignatures(
        context: Context,
        canvas: Canvas,
        paint: Paint,
        width: Int,
        height: Int,
        startY: Float,
        waliName: String,
        schoolProfile: SchoolProfile = SchoolProfile()
    ) {
        val y = (startY + 15f).coerceAtMost(height - 92f)
        val todayDate = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(Date())
        val cityLabel = schoolProfile.city.trim().ifBlank { "Indonesia" }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(71, 85, 105)

        // Kiri: Petugas Pengasuh
        canvas.drawText("$cityLabel, $todayDate", 50f, y, paint)
        canvas.drawText("Petugas / Pengasuh Asrama,", 50f, y + 12f, paint)
        canvas.drawText("( ................................................ )", 50f, y + 52f, paint)

        // Kanan: Wali Asuh Kesehatan
        val footerSignTitle = buildFooterSignatureTitle(schoolProfile)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Mengetahui,", width - 50f, y, paint)
        paint.textSize = if (footerSignTitle.length > 44) 7.5f else 8.5f
        canvas.drawText(footerSignTitle, width - 50f, y + 12f, paint)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(15, 118, 110)
        canvas.drawText("( $waliName )", width - 50f, y + 52f, paint)

        // Catatan kaki halus di bagian paling bawah lembar A4
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.textSize = 6.5f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText(
            context.getString(R.string.disclaimer_pdf_footer),
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
            canvas.drawText("LAPORAN REKAPITULASI KESEHATAN ANAK ASUH BULANAN", pageWidth / 2f, currentY, paint)

            currentY += 13f
            paint.textSize = 10f
            paint.color = Color.rgb(2, 132, 199)
            canvas.drawText("PERIODE BULAN: ${monthYearLabel.uppercase(Locale("id", "ID"))}", pageWidth / 2f, currentY, paint)
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
                "• Total Rujukan: ${records.size} Kali"
            }
            val morbiditasPct = if (totalStudentsCount > 0) (uniqueSick.toFloat() / totalStudentsCount * 100).toInt() else 0

            var statY = currentY + 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.5f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText("RINGKASAN EKSEKUTIF BULANAN:", boxLeft + 10f, statY, paint)

            statY += 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText("• Total Populasi Anak: $totalStudentsCount Anak", boxLeft + 10f, statY, paint)
            canvas.drawText("• Total Kunjungan Berobat: ${records.size} Kali", boxLeft + 175f, statY, paint)
            canvas.drawText("• Jumlah Anak Sakit: $uniqueSick Anak ($morbiditasPct%)", boxLeft + 350f, statY, paint)

            statY += 12f
            canvas.drawText("• Distribusi Asrama: $maleCount Putra | $femaleCount Putri", boxLeft + 10f, statY, paint)
            canvas.drawText("• Status: $tuntasCount Tuntas | $kontrolCount Perlu Kontrol", boxLeft + 175f, statY, paint)
            canvas.drawText(faskesSummaryText, boxLeft + 350f, statY, paint)

            currentY += boxHeight + 14f

            // 4. Daftar Kasus Sakit
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10.5f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText("DAFTAR REKAM MEDIS & KUNJUNGAN BEROBAT (${records.size} Kasus):", 35f, currentY, paint)
            currentY += 14f

            if (records.isEmpty()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                paint.textSize = 9.5f
                paint.color = Color.rgb(100, 116, 139)
                canvas.drawText("Tidak ada riwayat sakit / berobat pada periode bulan ini (Kondisi Nihil/Sehat).", 40f, currentY + 12f, paint)
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
                        canvas.drawText("Lanjutan Laporan Bulanan Kesehatan - $monthYearLabel (Halaman $pageNumber)", 35f, currentY, paint)
                        currentY += 16f
                    }

                    currentY = drawRecordItem(canvas, paint, pageWidth, currentY, index + 1, record)
                    currentY += 8f
                }
            }

            // 5. Tanda Tangan
            if (currentY > pageHeight - 110f) {
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

            val chooser = Intent.createChooser(intent, "Buka / Cetak Rekap PDF")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak ditemukan aplikasi pembuka PDF: ${e.message}", Toast.LENGTH_SHORT).show()
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
                putExtra(Intent.EXTRA_SUBJECT, "Rekap Screening Kesehatan - $studentName")
                putExtra(Intent.EXTRA_TEXT, "Dokumen resmi Rekapitulasi Screening Kesehatan Anak Asuh $studentName ($cleanSchool).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Dokumen PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
