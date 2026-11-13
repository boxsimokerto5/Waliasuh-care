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

        // Garis warna pemanis menyala berdasarkan gender:
        // Putra -> Hijau menyala, Putri -> Pink menyala
        val trimNeon = if (isMale) 0xFF00FF66.toInt() else 0xFFFF1E99.toInt()
        val trimGlow = if (isMale) 0xFF6EE7B7.toInt() else 0xFFF472B6.toInt()
        val genderBadgeBg = if (isMale) 0xFF065F46.toInt() else 0xFF9D174D.toInt()
        val genderLabel = if (isMale) "PUTRA" else "PUTRI"

        return when (jenjang) {
            // SMA (Kelas 10, 11, 12) -> Abu-abu elegan
            CardJenjang.SMA -> StudentCardPalette(
                jenjang = jenjang,
                isMale = isMale,
                gradientStartArgb = 0xFF2B3442.toInt(),
                gradientMidArgb = 0xFF475569.toInt(),
                gradientEndArgb = 0xFF64748B.toInt(),
                headerOverlayArgb = 0xFF1E293B.toInt(),
                trimNeonArgb = trimNeon,
                trimSoftGlowArgb = trimGlow,
                genderBadgeBgArgb = genderBadgeBg,
                genderLabel = genderLabel
            )
            // SMP (Kelas 7, 8, 9) -> Biru
            CardJenjang.SMP -> StudentCardPalette(
                jenjang = jenjang,
                isMale = isMale,
                gradientStartArgb = 0xFF0F2D6B.toInt(),
                gradientMidArgb = 0xFF1D4ED8.toInt(),
                gradientEndArgb = 0xFF2563EB.toInt(),
                headerOverlayArgb = 0xFF172554.toInt(),
                trimNeonArgb = trimNeon,
                trimSoftGlowArgb = trimGlow,
                genderBadgeBgArgb = genderBadgeBg,
                genderLabel = genderLabel
            )
            // SD (Kelas 1-5 / 1-6) -> Merah
            CardJenjang.SD -> StudentCardPalette(
                jenjang = jenjang,
                isMale = isMale,
                gradientStartArgb = 0xFF7F1D1D.toInt(),
                gradientMidArgb = 0xFFB91C1C.toInt(),
                gradientEndArgb = 0xFFDC2626.toInt(),
                headerOverlayArgb = 0xFF5F1313.toInt(),
                trimNeonArgb = trimNeon,
                trimSoftGlowArgb = trimGlow,
                genderBadgeBgArgb = genderBadgeBg,
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

    /**
     * Menggambar Kartu Anak Asuh berdesain ID Card modern di atas Canvas (digunakan untuk export PDF & cetak A4).
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
        logoBitmap: Bitmap? = null
    ) {
        val palette = resolvePalette(student)
        val cornerRadius = height * 0.065f
        val cardRect = RectF(left, top, left + width, top + height)

        val saveCount = canvas.save()
        val clipPath = Path().apply {
            addRoundRect(cardRect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

        // 1. Background Gradasi Jenjang (SMA Abu-abu / SMP Biru / SD Merah)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                left, top, left + width, top + height,
                intArrayOf(palette.gradientStartArgb, palette.gradientMidArgb, palette.gradientEndArgb),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(cardRect, bgPaint)

        // 2. Ornamen geometris halus
        val decoPaint1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(28, 255, 255, 255)
        }
        canvas.drawCircle(left + width * 0.88f, top + height * 0.18f, height * 0.34f, decoPaint1)
        val decoPaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(18, 255, 255, 255)
        }
        canvas.drawCircle(left + width * 0.12f, top + height * 0.94f, height * 0.28f, decoPaint2)

        // 3. Header Strip Sekolah
        val headerHeight = height * 0.23f
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.headerOverlayArgb
            alpha = 215
        }
        canvas.drawRect(left, top, left + width, top + headerHeight, headerPaint)

        // 4. Garis Pemanis Warna Menyala (Putra = Hijau Menyala, Putri = Pink Menyala)
        val neonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.trimNeonArgb
        }
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.trimSoftGlowArgb
            alpha = 175
        }
        val topRibbonH = height * 0.028f
        canvas.drawRect(left, top, left + width, top + topRibbonH, neonPaint)
        canvas.drawRect(left, top + headerHeight - topRibbonH * 0.7f, left + width, top + headerHeight, glowPaint)
        canvas.drawRect(left, top + height - topRibbonH, left + width, top + height, neonPaint)

        // Garis pemanis vertikal di sisi kiri kartu
        val sideStripeW = width * 0.018f
        canvas.drawRect(left, top + headerHeight, left + sideStripeW, top + height - topRibbonH, neonPaint)

        // 5. Header Content (Logo + Nama Sekolah + Badge Jenjang/Gender)
        val padX = width * 0.045f
        val logoSize = headerHeight * 0.56f
        val logoLeft = left + padX
        val logoTop = top + topRibbonH + (headerHeight - topRibbonH - logoSize) * 0.5f

        if (logoBitmap != null) {
            val logoRect = RectF(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize)
            canvas.drawBitmap(logoBitmap, null, logoRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        } else {
            val badgeCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
            val cx = logoLeft + logoSize / 2f
            val cy = logoTop + logoSize / 2f
            canvas.drawCircle(cx, cy, logoSize / 2f, badgeCirclePaint)
            val monoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = palette.gradientMidArgb
                textSize = logoSize * 0.44f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val yOffset = (monoPaint.descent() + monoPaint.ascent()) / 2f
            canvas.drawText("WA", cx, cy - yOffset, monoPaint)
        }

        val cleanSchool = schoolName.trim().ifBlank { "WALI ASUH CARE SCHOOL" }.uppercase()
        val badgeText = "${palette.jenjang.label} • ${palette.genderLabel}"
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.trimNeonArgb
            textSize = height * 0.044f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val badgeW = badgePaint.measureText(badgeText) + width * 0.045f
        val badgeH = height * 0.095f
        val badgeRight = left + width - padX * 0.8f
        val badgeLeft = badgeRight - badgeW
        val badgeTop = top + topRibbonH + (headerHeight - topRibbonH - badgeH) * 0.5f
        val badgeRect = RectF(badgeLeft, badgeTop, badgeRight, badgeTop + badgeH)

        val badgeFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.genderBadgeBgArgb }
        canvas.drawRoundRect(badgeRect, badgeH * 0.45f, badgeH * 0.45f, badgeFillPaint)
        val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = height * 0.008f
            color = palette.trimNeonArgb
        }
        canvas.drawRoundRect(badgeRect, badgeH * 0.45f, badgeH * 0.45f, badgeBorderPaint)
        val badgeTextY = badgeRect.centerY() - (badgePaint.descent() + badgePaint.ascent()) / 2f
        canvas.drawText(badgeText, badgeRect.left + width * 0.0225f, badgeTextY, badgePaint)

        // Nama Sekolah & Sub-label "KARTU ANAK ASUH"
        val schoolTextLeft = logoLeft + logoSize + width * 0.025f
        val schoolTextAvailW = (badgeLeft - schoolTextLeft - width * 0.02f).coerceAtLeast(30f)
        val schoolPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = height * 0.056f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val ellipsizedSchool = TextUtils.ellipsize(cleanSchool, schoolPaint, schoolTextAvailW, TextUtils.TruncateAt.END).toString()
        val schoolTitleY = top + topRibbonH + headerHeight * 0.39f
        canvas.drawText(ellipsizedSchool, schoolTextLeft, schoolTitleY, schoolPaint)

        val subLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.trimSoftGlowArgb
            textSize = height * 0.039f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        canvas.drawText("KARTU IDENTITAS ANAK ASUH", schoolTextLeft, schoolTitleY + height * 0.068f, subLabelPaint)

        // 6. Content Area: Kiri (Biodata) + Kanan (QR Code Besar)
        val contentTop = top + headerHeight + height * 0.045f
        val contentBottom = top + height - topRibbonH - height * 0.035f
        val contentHeight = (contentBottom - contentTop).coerceAtLeast(20f)

        // Kotak QR Besar di Kanan
        val qrFrameSize = minOf(contentHeight * 0.84f, width * 0.34f)
        val qrFrameRight = left + width - padX * 0.85f
        val qrFrameLeft = qrFrameRight - qrFrameSize
        val qrFrameTop = contentTop
        val qrFrameBottom = qrFrameTop + qrFrameSize
        val qrFrameRect = RectF(qrFrameLeft, qrFrameTop, qrFrameRight, qrFrameBottom)

        val qrBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(qrFrameRect, height * 0.04f, height * 0.04f, qrBgPaint)
        val qrBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = height * 0.011f
            color = palette.trimNeonArgb
        }
        canvas.drawRoundRect(qrFrameRect, height * 0.04f, height * 0.04f, qrBorderPaint)

        if (qrBitmap != null) {
            val qrPad = qrFrameSize * 0.055f
            val qrDrawRect = RectF(
                qrFrameRect.left + qrPad,
                qrFrameRect.top + qrPad,
                qrFrameRect.right - qrPad,
                qrFrameRect.bottom - qrPad
            )
            // Penting: isFilterBitmap = false agar modul QR tajam sempurna saat dicetak
            val qrPaint = Paint().apply {
                isAntiAlias = false
                isFilterBitmap = false
            }
            canvas.drawBitmap(qrBitmap, null, qrDrawRect, qrPaint)
        }

        // Label NIK di bawah QR Code
        val nikDisplay = student.nik.ifBlank { student.nis.ifBlank { student.id.toString() } }
        val nikCaptionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = height * 0.042f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val ellipsizedNik = TextUtils.ellipsize(
            "NIK: $nikDisplay",
            nikCaptionPaint,
            qrFrameSize + width * 0.03f,
            TextUtils.TruncateAt.MIDDLE
        ).toString()
        canvas.drawText(
            ellipsizedNik,
            qrFrameRect.centerX(),
            qrFrameRect.bottom + height * 0.068f,
            nikCaptionPaint
        )

        // Kolom Kiri: Nama Siswa, NISN, Kelas, Alamat
        val infoLeft = left + padX * 0.85f
        val infoRight = qrFrameLeft - width * 0.035f
        val infoWidth = (infoRight - infoLeft).coerceAtLeast(40f).toInt()

        val namePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = height * 0.072f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val nameLayout = StaticLayout.Builder.obtain(
            student.name.trim().uppercase(),
            0,
            student.name.trim().length,
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

        var cursorY = contentTop + nameLayout.height + height * 0.025f

        // Divider pemanis neon tipis di bawah nama
        canvas.drawRect(
            infoLeft,
            cursorY,
            infoLeft + infoWidth * 0.35f,
            cursorY + height * 0.01f,
            neonPaint
        )
        cursorY += height * 0.045f

        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(205, 241, 245, 249)
            textSize = height * 0.046f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val valuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = height * 0.049f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val labelColW = width * 0.105f
        val rowStep = height * 0.095f

        fun drawFieldRow(label: String, value: String) {
            val cleanVal = value.trim().ifBlank { "-" }
            canvas.drawText(label, infoLeft, cursorY, labelPaint)
            val valLeft = infoLeft + labelColW
            val availValW = (infoRight - valLeft).coerceAtLeast(20f)
            val ellipsized = TextUtils.ellipsize(": $cleanVal", valuePaint, availValW, TextUtils.TruncateAt.END).toString()
            canvas.drawText(ellipsized, valLeft, cursorY, valuePaint)
            cursorY += rowStep
        }

        drawFieldRow("NISN", student.nis.ifBlank { "-" })
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
            color = Color.WHITE
            textSize = height * 0.045f
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
        // Geser baseline StaticLayout ke posisi atas baris
        canvas.translate(addrLeft, cursorY - addrPaint.textSize * 0.85f)
        addrLayout.draw(canvas)
        canvas.restore()

        canvas.restoreToCount(saveCount)

        // 7. Outer Glow Neon Border (Hijau Menyala / Pink Menyala) di keliling kartu
        val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = height * 0.015f
            color = palette.trimNeonArgb
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, outerBorderPaint)
    }
}
