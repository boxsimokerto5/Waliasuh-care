package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.example.data.model.ChildStudent
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

enum class CardJenjang(val label: String) {
    SD("SD"),
    SMP("SMP"),
    SMA("SMA")
}

data class StudentCardPalette(
    val jenjang: CardJenjang,
    val isMale: Boolean,
    val gradientStartArgb: Int,
    val gradientMidArgb: Int,
    val gradientEndArgb: Int,
    val headerOverlayArgb: Int,
    val trimNeonArgb: Int,
    val trimSoftGlowArgb: Int,
    val genderBadgeBgArgb: Int,
    val genderBadgeTextArgb: Int,
    val genderLabel: String
)

object StudentCardRenderer {

    private val smaRegex = Regex("""\b(SMA|SMK|MA|XII|XI|X|10|11|12)\b""")
    private val smaPrefixNumberRegex = Regex("""^(10|11|12|XII|XI|X)(?:[-/\s.]|$)""")
    private val smpRegex = Regex("""\b(SMP|MTS|VII|VIII|IX|7|8|9)\b""")
    private val smpPrefixNumberRegex = Regex("""^(7|8|9|VII|VIII|IX)(?:[-/\s.]|$)""")
    private val sdRegex = Regex("""\b(SD|MI|VI|V|IV|III|II|I|1|2|3|4|5|6)\b""")
    private val sdPrefixNumberRegex = Regex("""^(1|2|3|4|5|6|VI|V|IV|III|II|I)(?:[-/\s.]|$)""")

    fun resolveCardJenjang(student: ChildStudent): CardJenjang {
        val explicitJenjang = student.jenjang.trim().uppercase()
        when {
            explicitJenjang.contains("SMA") || explicitJenjang.contains("SMK") || explicitJenjang == "MA" ->
                return CardJenjang.SMA
            explicitJenjang.contains("SMP") || explicitJenjang.contains("MTS") ->
                return CardJenjang.SMP
            explicitJenjang.contains("SD") || explicitJenjang == "MI" ->
                return CardJenjang.SD
        }

        val grade = student.classGrade.trim().uppercase()
        if (grade.isNotEmpty()) {
            if (smaPrefixNumberRegex.containsMatchIn(grade) || smaRegex.containsMatchIn(grade)) {
                return CardJenjang.SMA
            }
            if (smpPrefixNumberRegex.containsMatchIn(grade) || smpRegex.containsMatchIn(grade)) {
                return CardJenjang.SMP
            }
            if (sdPrefixNumberRegex.containsMatchIn(grade) || sdRegex.containsMatchIn(grade)) {
                return CardJenjang.SD
            }
        }
        return CardJenjang.SMP
    }

    fun resolvePalette(student: ChildStudent): StudentCardPalette {
        val jenjang = resolveCardJenjang(student)
        val isMale = StudentClassifier.isMaleStudent(student)

        // Aksen warna pemanis manis & modern berdasarkan gender:
        // Putra -> Hijau Emerald, Putri -> Pink Rose
        val trimAccent = if (isMale) 0xFF10B981.toInt() else 0xFFEC4899.toInt()
        val trimSoft = if (isMale) 0xFFA7F3D0.toInt() else 0xFFFBCFE8.toInt()
        val genderBadgeBg = if (isMale) 0xFFECFDF5.toInt() else 0xFFFDF2F8.toInt()
        val genderBadgeText = if (isMale) 0xFF047857.toInt() else 0xFFBE185D.toInt()
        val genderLabel = if (isMale) "PUTRA" else "PUTRI"

        return when (jenjang) {
            // SMA (Kelas 10, 11, 12) -> Header Abu-abu Slate Elegan
            CardJenjang.SMA -> StudentCardPalette(
                jenjang = jenjang,
                isMale = isMale,
                gradientStartArgb = 0xFF334155.toInt(),
                gradientMidArgb = 0xFF475569.toInt(),
                gradientEndArgb = 0xFF64748B.toInt(),
                headerOverlayArgb = 0xFF334155.toInt(),
                trimNeonArgb = trimAccent,
                trimSoftGlowArgb = trimSoft,
                genderBadgeBgArgb = genderBadgeBg,
                genderBadgeTextArgb = genderBadgeText,
                genderLabel = genderLabel
            )
            // SMP (Kelas 7, 8, 9) -> Header Biru Royal
            CardJenjang.SMP -> StudentCardPalette(
                jenjang = jenjang,
                isMale = isMale,
                gradientStartArgb = 0xFF1E3A8A.toInt(),
                gradientMidArgb = 0xFF1D4ED8.toInt(),
                gradientEndArgb = 0xFF2563EB.toInt(),
                headerOverlayArgb = 0xFF1E3A8A.toInt(),
                trimNeonArgb = trimAccent,
                trimSoftGlowArgb = trimSoft,
                genderBadgeBgArgb = genderBadgeBg,
                genderBadgeTextArgb = genderBadgeText,
                genderLabel = genderLabel
            )
            // SD (Kelas 1-5 / 1-6) -> Header Merah Crimson
            CardJenjang.SD -> StudentCardPalette(
                jenjang = jenjang,
                isMale = isMale,
                gradientStartArgb = 0xFF991B1B.toInt(),
                gradientMidArgb = 0xFFB91C1C.toInt(),
                gradientEndArgb = 0xFFDC2626.toInt(),
                headerOverlayArgb = 0xFF991B1B.toInt(),
                trimNeonArgb = trimAccent,
                trimSoftGlowArgb = trimSoft,
                genderBadgeBgArgb = genderBadgeBg,
                genderBadgeTextArgb = genderBadgeText,
                genderLabel = genderLabel
            )
        }
    }

    fun resolveQrPayload(student: ChildStudent): String {
        return student.nik.trim()
            .ifBlank { student.nis.trim() }
            .ifBlank { student.id.toString() }
    }

    fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap? {
        val cleanContent = content.trim()
        if (cleanContent.isEmpty()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1,
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
            )
            val bitMatrix = QRCodeWriter().encode(
                cleanContent,
                BarcodeFormat.QR_CODE,
                sizePx,
                sizePx,
                hints
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun drawBitmapAspectFit(canvas: Canvas, bitmap: Bitmap, targetBox: RectF, paint: Paint) {
        if (bitmap.width <= 0 || bitmap.height <= 0) return
        val scale = minOf(targetBox.width() / bitmap.width, targetBox.height() / bitmap.height)
        val drawW = bitmap.width * scale
        val drawH = bitmap.height * scale
        val dx = targetBox.left + (targetBox.width() - drawW) / 2f
        val dy = targetBox.top + (targetBox.height() - drawH) / 2f
        canvas.drawBitmap(bitmap, null, RectF(dx, dy, dx + drawW, dy + drawH), paint)
    }

    /**
     * Menggambar Kartu Anak Asuh Konsep A:
     * Header berwarna Jenjang (SMA Abu-abu / SMP Biru / SD Merah) dengan judul sekolah utuh 2 baris,
     * body putih bersih ramah cetak, micro-badge jenjang-gender di atas QR, dan NIK utuh pada biodata.
     */
    fun drawStudentCardOnCanvas(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        student: ChildStudent,
        schoolName: String,
        qrBitmap: Bitmap?,
        logoBitmap: Bitmap? = null,
        kemensosBitmap: Bitmap? = null,
        sekolahRakyatBitmap: Bitmap? = null
    ) {
        val palette = resolvePalette(student)
        val cornerRadius = height * 0.065f
        val cardRect = RectF(left, top, left + width, top + height)

        val saveCount = canvas.save()
        val clipPath = Path().apply {
            addRoundRect(cardRect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

        // 1. Dasar Body Kartu: Putih Bersih (#FFFFFF)
        val whiteBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRect(cardRect, whiteBodyPaint)

        // Ornamen watermark lembut bernuansa jenjang pada background putih
        val watermarkColor = Color.argb(
            13,
            Color.red(palette.gradientMidArgb),
            Color.green(palette.gradientMidArgb),
            Color.blue(palette.gradientMidArgb)
        )
        val decoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = watermarkColor }
        canvas.drawCircle(left + width * 0.90f, top + height * 0.85f, height * 0.34f, decoPaint)
        canvas.drawCircle(left + width * 0.10f, top + height * 0.95f, height * 0.24f, decoPaint)

        // 2. Strip Pemanis Atas + Header Berwarna Jenjang
        val topRibbonH = height * 0.020f
        val headerHeight = height * 0.275f
        val dividerRibbonH = height * 0.014f

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                left, top, left + width, top + headerHeight,
                intArrayOf(palette.gradientStartArgb, palette.gradientMidArgb, palette.gradientEndArgb),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(left, top, left + width, top + headerHeight, headerPaint)

        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.trimNeonArgb }
        canvas.drawRect(left, top, left + width, top + topRibbonH, accentPaint)
        canvas.drawRect(left, top + headerHeight, left + width, top + headerHeight + dividerRibbonH, accentPaint)

        // Garis aksen vertikal manis di sisi kiri area putih
        val sideStripeW = width * 0.014f
        canvas.drawRect(left, top + headerHeight + dividerRibbonH, left + sideStripeW, top + height, accentPaint)

        // 3. Isi Header: 3 Logo di Kiri + Nama Sekolah Penuh (2 baris) + Subjudul
        val padX = width * 0.038f
        val hasExtraLogos = kemensosBitmap != null || sekolahRakyatBitmap != null
        val logoSize = if (hasExtraLogos) headerHeight * 0.44f else headerHeight * 0.50f
        val logoGap = width * 0.010f
        var logoCursorX = left + padX
        val headerInnerTop = top + topRibbonH
        val headerInnerH = headerHeight - topRibbonH
        val logoTop = headerInnerTop + (headerInnerH - logoSize) * 0.5f
        val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val whiteBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }

        if (kemensosBitmap != null) {
            val badgeRect = RectF(logoCursorX, logoTop, logoCursorX + logoSize, logoTop + logoSize)
            canvas.drawRoundRect(badgeRect, logoSize * 0.24f, logoSize * 0.24f, whiteBadgePaint)
            val innerPad = logoSize * 0.11f
            drawBitmapAspectFit(
                canvas,
                kemensosBitmap,
                RectF(badgeRect.left + innerPad, badgeRect.top + innerPad, badgeRect.right - innerPad, badgeRect.bottom - innerPad),
                bmpPaint
            )
            logoCursorX += logoSize + logoGap
        }

        if (logoBitmap != null) {
            val logoRect = RectF(logoCursorX, logoTop, logoCursorX + logoSize, logoTop + logoSize)
            canvas.drawBitmap(logoBitmap, null, logoRect, bmpPaint)
            logoCursorX += logoSize + logoGap
        } else if (!hasExtraLogos) {
            val cx = logoCursorX + logoSize / 2f
            val cy = logoTop + logoSize / 2f
            canvas.drawCircle(cx, cy, logoSize / 2f, whiteBadgePaint)
            val monoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = palette.gradientMidArgb
                textSize = logoSize * 0.44f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val yOffset = (monoPaint.descent() + monoPaint.ascent()) / 2f
            canvas.drawText("WA", cx, cy - yOffset, monoPaint)
            logoCursorX += logoSize + logoGap
        }

        if (sekolahRakyatBitmap != null) {
            val badgeRect = RectF(logoCursorX, logoTop, logoCursorX + logoSize, logoTop + logoSize)
            canvas.drawRoundRect(badgeRect, logoSize * 0.24f, logoSize * 0.24f, whiteBadgePaint)
            val innerPad = logoSize * 0.10f
            drawBitmapAspectFit(
                canvas,
                sekolahRakyatBitmap,
                RectF(badgeRect.left + innerPad, badgeRect.top + innerPad, badgeRect.right - innerPad, badgeRect.bottom - innerPad),
                bmpPaint
            )
            logoCursorX += logoSize + logoGap
        }

        val cleanSchool = schoolName.trim().ifBlank { "WALI ASUH CARE SCHOOL" }.uppercase()
        val schoolTextLeft = logoCursorX + width * 0.014f
        val schoolTextAvailW = (left + width - padX - schoolTextLeft).coerceAtLeast(40f).toInt()

        val schoolPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = if (cleanSchool.length > 36) height * 0.044f else height * 0.049f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val schoolLayout = StaticLayout.Builder.obtain(
            cleanSchool,
            0,
            cleanSchool.length,
            schoolPaint,
            schoolTextAvailW
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(2)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setLineSpacing(0f, 0.94f)
            .build()

        val subLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.trimSoftGlowArgb
            textSize = height * 0.032f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.05f
        }
        val subLineH = subLabelPaint.fontMetrics.let { it.descent - it.ascent }
        val totalHeaderTextH = schoolLayout.height + height * 0.012f + subLineH
        val headerTextStartY = headerInnerTop + ((headerInnerH - totalHeaderTextH) * 0.5f).coerceAtLeast(height * 0.015f)

        canvas.save()
        canvas.translate(schoolTextLeft, headerTextStartY)
        schoolLayout.draw(canvas)
        canvas.restore()

        val subBaselineY = headerTextStartY + schoolLayout.height + height * 0.012f - subLabelPaint.fontMetrics.ascent
        canvas.drawText("KARTU IDENTITAS ANAK ASUH", schoolTextLeft, subBaselineY, subLabelPaint)

        // 4. Area Konten Putih: Kiri (Biodata lengkap termasuk NIK utuh) & Kanan (Micro-pill + QR Code)
        val bodyTop = top + headerHeight + dividerRibbonH
        val contentTop = bodyTop + height * 0.042f
        val contentBottom = top + height - height * 0.036f
        val contentHeight = (contentBottom - contentTop).coerceAtLeast(24f)

        // Kolom Kanan: Micro-Pill Jenjang • Gender (kecil & manis) + QR Code + Caption
        val badgeText = "${palette.jenjang.label} • ${palette.genderLabel}"
        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.genderBadgeTextArgb
            textSize = height * 0.033f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val badgeH = height * 0.080f
        val qrFrameSize = minOf(contentHeight * 0.68f, width * 0.265f)
        val rightColRight = left + width - padX
        val qrFrameLeft = rightColRight - qrFrameSize
        val rightCenterX = qrFrameLeft + qrFrameSize / 2f

        val badgeW = maxOf(badgeTextPaint.measureText(badgeText) + width * 0.036f, qrFrameSize * 0.78f)
        val badgeLeft = (rightCenterX - badgeW / 2f).coerceAtMost(rightColRight - badgeW)
        val badgeRect = RectF(badgeLeft, contentTop, badgeLeft + badgeW, contentTop + badgeH)

        val badgeFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.genderBadgeBgArgb }
        canvas.drawRoundRect(badgeRect, badgeH * 0.5f, badgeH * 0.5f, badgeFillPaint)
        val badgeStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = height * 0.0055f
            color = palette.trimNeonArgb
        }
        canvas.drawRoundRect(badgeRect, badgeH * 0.5f, badgeH * 0.5f, badgeStrokePaint)
        val badgeTextW = badgeTextPaint.measureText(badgeText)
        val badgeTextY = badgeRect.centerY() - (badgeTextPaint.descent() + badgeTextPaint.ascent()) / 2f
        canvas.drawText(badgeText, badgeRect.centerX() - badgeTextW / 2f, badgeTextY, badgeTextPaint)

        // Kotak QR di bawah micro-pill
        val qrFrameTop = badgeRect.bottom + height * 0.026f
        val qrFrameBottom = qrFrameTop + qrFrameSize
        val qrFrameRect = RectF(qrFrameLeft, qrFrameTop, rightColRight, qrFrameBottom)

        val qrBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(qrFrameRect, height * 0.036f, height * 0.036f, qrBgPaint)
        val qrBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = height * 0.007f
            color = palette.trimNeonArgb
        }
        canvas.drawRoundRect(qrFrameRect, height * 0.036f, height * 0.036f, qrBorderPaint)

        if (qrBitmap != null) {
            val qrPad = qrFrameSize * 0.055f
            val qrDrawRect = RectF(
                qrFrameRect.left + qrPad,
                qrFrameRect.top + qrPad,
                qrFrameRect.right - qrPad,
                qrFrameRect.bottom - qrPad
            )
            // isFilterBitmap = false agar modul QR tajam sempurna saat dicetak
            val qrPaint = Paint().apply {
                isAntiAlias = false
                isFilterBitmap = false
            }
            canvas.drawBitmap(qrBitmap, null, qrDrawRect, qrPaint)
        }

        val qrCaptionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = height * 0.030f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.06f
        }
        canvas.drawText("SCAN QR NIK", qrFrameRect.centerX(), qrFrameRect.bottom + height * 0.052f, qrCaptionPaint)

        // Kolom Kiri: Nama Siswa + Garis Pemanis + NISN, NIK (Utuh), Kelas, Alamat
        val infoLeft = left + sideStripeW + padX * 0.72f
        val infoRight = qrFrameLeft - width * 0.032f
        val infoWidth = (infoRight - infoLeft).coerceAtLeast(40f).toInt()

        val namePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = height * 0.068f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val cleanStudentName = student.name.trim().uppercase()
        val nameLayout = StaticLayout.Builder.obtain(
            cleanStudentName,
            0,
            cleanStudentName.length,
            namePaint,
            infoWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(2)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setLineSpacing(0f, 0.96f)
            .build()

        canvas.save()
        canvas.translate(infoLeft, contentTop)
        nameLayout.draw(canvas)
        canvas.restore()

        var cursorY = contentTop + nameLayout.height + height * 0.022f

        // Garis pemanis tipis di bawah nama siswa
        canvas.drawRect(
            infoLeft,
            cursorY,
            infoLeft + infoWidth * 0.28f,
            cursorY + height * 0.009f,
            accentPaint
        )
        cursorY += height * 0.048f

        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = height * 0.043f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val valuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = height * 0.045f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val monoValuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = height * 0.042f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        val labelColW = width * 0.098f
        val rowStep = height * 0.086f

        fun drawFieldRow(label: String, value: String, useMono: Boolean = false) {
            val cleanVal = value.trim().ifBlank { "-" }
            canvas.drawText(label, infoLeft, cursorY, labelPaint)
            val valLeft = infoLeft + labelColW
            val availValW = (infoRight - valLeft).coerceAtLeast(20f)
            val paintToUse = if (useMono) monoValuePaint else valuePaint
            val ellipsized = TextUtils.ellipsize(": $cleanVal", paintToUse, availValW, TextUtils.TruncateAt.END).toString()
            canvas.drawText(ellipsized, valLeft, cursorY, paintToUse)
            cursorY += rowStep
        }

        drawFieldRow("NISN", student.nis.ifBlank { "-" })
        drawFieldRow("NIK", student.nik.ifBlank { "-" }, useMono = true)
        val kelasText = buildString {
            append(student.classGrade.ifBlank { "-" })
            if (student.dormitory.isNotBlank()) {
                append(" • ")
                append(student.dormitory)
            }
        }
        drawFieldRow("Kelas", kelasText)

        // Alamat (maks 2 baris rapi)
        canvas.drawText("Alamat", infoLeft, cursorY, labelPaint)
        val addrLeft = infoLeft + labelColW
        val addrWidth = (infoRight - addrLeft).coerceAtLeast(20f).toInt()
        val addrValue = ": ${student.address.trim().ifBlank { "-" }}"
        val addrPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = height * 0.042f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val addrLayout = StaticLayout.Builder.obtain(
            addrValue,
            0,
            addrValue.length,
            addrPaint,
            addrWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(2)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setLineSpacing(0f, 0.95f)
            .build()

        canvas.save()
        canvas.translate(addrLeft, cursorY - addrPaint.textSize * 0.85f)
        addrLayout.draw(canvas)
        canvas.restore()

        canvas.restoreToCount(saveCount)

        // 5. Outer Border Tipis & Manis (1.2pt proporsional)
        val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = height * 0.010f
            color = palette.trimNeonArgb
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, outerBorderPaint)
    }
}
