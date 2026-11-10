package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
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
        waliName: String = "Sari Devi"
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

            // 1. Header / Kop Surat Resmi Sekolah Rakyat SRT 1
            currentY = drawHeader(canvas, paint, pageWidth, currentY)

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

            drawFooterSignatures(canvas, paint, pageWidth, pageHeight, currentY, waliName)

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

    private fun drawHeader(canvas: Canvas, paint: Paint, width: Int, startY: Float): Float {
        var y = startY

        // Header text center
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        paint.color = Color.rgb(15, 118, 110) // Teal color
        canvas.drawText("SEKOLAH RAKYAT SRT 1 KABUPATEN KEDIRI", width / 2f, y, paint)

        y += 13f
        paint.textSize = 10.5f
        paint.color = Color.rgb(30, 41, 59)
        canvas.drawText("UNIT LAYANAN KESEHATAN SANTRI & ANAK ASUH (UKSA)", width / 2f, y, paint)

        y += 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("Kerjasama Fasilitas Kesehatan: Puskesmas Wates & Puskesmas Plosoklaten", width / 2f, y, paint)

        y += 10f
        // Garis Pembatas Kop Surat Ganda
        paint.strokeWidth = 2f
        paint.color = Color.rgb(15, 118, 110)
        canvas.drawLine(35f, y, width - 35f, y, paint)

        y += 3f
        paint.strokeWidth = 0.8f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawLine(35f, y, width - 35f, y, paint)

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
        canvas: Canvas,
        paint: Paint,
        width: Int,
        height: Int,
        startY: Float,
        waliName: String
    ) {
        val y = (startY + 15f).coerceAtMost(height - 80f)
        val todayDate = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(Date())

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(71, 85, 105)

        // Kiri: Petugas Pengasuh
        canvas.drawText("Kediri, $todayDate", 50f, y, paint)
        canvas.drawText("Petugas / Pengasuh Asrama,", 50f, y + 12f, paint)
        canvas.drawText("( ................................................ )", 50f, y + 52f, paint)

        // Kanan: Wali Asuh Kesehatan
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Mengetahui,", width - 50f, y, paint)
        canvas.drawText("Wali Asuh Kesehatan SRT 1,", width - 50f, y + 12f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(15, 118, 110)
        canvas.drawText("( $waliName )", width - 50f, y + 52f, paint)
        paint.textAlign = Paint.Align.LEFT
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
    fun sharePdfFile(context: Context, pdfFile: File, studentName: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Rekap Screening Kesehatan - $studentName")
                putExtra(Intent.EXTRA_TEXT, "Dokumen resmi Rekapitulasi Screening Kesehatan Anak Asuh $studentName (Sekolah Rakyat SRT 1 Kediri).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Dokumen PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
