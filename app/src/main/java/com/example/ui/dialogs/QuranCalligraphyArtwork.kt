package com.example.ui.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.QuranArabicFontChoice
import com.example.data.model.QuranCatalog
import com.example.data.model.QuranReaderTheme
import com.example.data.model.QuranSurahSpec
import kotlin.math.cos
import kotlin.math.sin

data class QuranPalette(
    val isDark: Boolean,
    val paperTop: Color,
    val paperBottom: Color,
    val topBarSurface: Color,
    val cardSurface: Color,
    val cardSoft: Color,
    val goldBright: Color,
    val goldPrimary: Color,
    val goldDeep: Color,
    val goldGlow: Color,
    val inkArabic: Color,
    val inkSecondary: Color,
    val inkMuted: Color,
    val border: Color,
    val playingBg: Color,
    val emeraldAccent: Color
)

fun resolveQuranPalette(theme: QuranReaderTheme): QuranPalette = when (theme) {
    QuranReaderTheme.IVORY_GOLD -> QuranPalette(
        isDark = false,
        paperTop = Color(0xFFFFFCF4),
        paperBottom = Color(0xFFF9EED4),
        topBarSurface = Color(0xFFFBF3DC),
        cardSurface = Color(0xFFFFFAF0),
        cardSoft = Color(0xFFF5E6C4),
        goldBright = Color(0xFFE4B94F),
        goldPrimary = Color(0xFFB7791F),
        goldDeep = Color(0xFF74470B),
        goldGlow = Color(0xFFFAE7A7),
        inkArabic = Color(0xFF1E1509),
        inkSecondary = Color(0xFF5D4625),
        inkMuted = Color(0xFF8B7149),
        border = Color(0xFFE2C788),
        playingBg = Color(0xFFFFF0BF),
        emeraldAccent = Color(0xFF0F766E)
    )
    QuranReaderTheme.AMBER_SEPIA -> QuranPalette(
        isDark = false,
        paperTop = Color(0xFFF7EBD0),
        paperBottom = Color(0xFFECD6AA),
        topBarSurface = Color(0xFFF3E2BE),
        cardSurface = Color(0xFFFAF0D8),
        cardSoft = Color(0xFFEBD3A1),
        goldBright = Color(0xFFD6A437),
        goldPrimary = Color(0xFF9B6112),
        goldDeep = Color(0xFF5F3806),
        goldGlow = Color(0xFFF3D785),
        inkArabic = Color(0xFF211608),
        inkSecondary = Color(0xFF59401E),
        inkMuted = Color(0xFF81643B),
        border = Color(0xFFD2AE68),
        playingBg = Color(0xFFF9E29C),
        emeraldAccent = Color(0xFF0F766E)
    )
    QuranReaderTheme.NIGHT_GOLD -> QuranPalette(
        isDark = true,
        paperTop = Color(0xFF121611),
        paperBottom = Color(0xFF191F16),
        topBarSurface = Color(0xFF1B2118),
        cardSurface = Color(0xFF1F261C),
        cardSoft = Color(0xFF2B3224),
        goldBright = Color(0xFFF0CC6B),
        goldPrimary = Color(0xFFD4A742),
        goldDeep = Color(0xFFF6DC8E),
        goldGlow = Color(0xFF3E351A),
        inkArabic = Color(0xFFFDF8EA),
        inkSecondary = Color(0xFFD7C69C),
        inkMuted = Color(0xFF9F8F6A),
        border = Color(0xFF5C4B24),
        playingBg = Color(0xFF352C14),
        emeraldAccent = Color(0xFF34D399)
    )
}

fun resolveArabicFontFamily(choice: QuranArabicFontChoice): FontFamily = when (choice) {
    QuranArabicFontChoice.SCHEHERAZADE -> FontFamily(Font(R.font.scheherazade_new, FontWeight.Normal))
    QuranArabicFontChoice.AMIRI -> FontFamily(Font(R.font.amiri, FontWeight.Normal))
    QuranArabicFontChoice.NOTO_NASKH -> FontFamily(Font(R.font.noto_naskh_arabic, FontWeight.Normal))
}

/**
 * Bingkai Kaligrafi Mushaf (Gaya Tezhip Emas & Krem Gading Hangat):
 * Menggambar bingkai triple-border, rantai arabesque bergelombang di keempat sisi,
 * dan medali sudut bintang 8-titik (Rub el Hizb) di seluruh tepi halaman.
 */
@Composable
fun MushafIlluminatedPageFrame(
    palette: QuranPalette,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .drawBehind {
                val cornerR = 18.dp.toPx()
                // Latar kertas gading hangat bercahaya lembut
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            palette.cardSurface,
                            palette.paperTop,
                            palette.cardSurface
                        )
                    ),
                    cornerRadius = CornerRadius(cornerR, cornerR)
                )

                val outerInset = 3.dp.toPx()
                val bandInset = 8.dp.toPx()
                val innerInset = 13.dp.toPx()

                // Pita emas lembut di sekeliling bingkai
                drawRoundRect(
                    color = palette.goldGlow.copy(alpha = if (palette.isDark) 0.25f else 0.48f),
                    topLeft = Offset(outerInset, outerInset),
                    size = Size(size.width - outerInset * 2f, size.height - outerInset * 2f),
                    cornerRadius = CornerRadius(cornerR - 2.dp.toPx()),
                    style = Stroke(width = 8.dp.toPx())
                )

                // Garis luar perunggu-emas tebal
                drawRoundRect(
                    brush = Brush.linearGradient(
                        listOf(palette.goldDeep, palette.goldBright, palette.goldPrimary, palette.goldDeep)
                    ),
                    topLeft = Offset(outerInset, outerInset),
                    size = Size(size.width - outerInset * 2f, size.height - outerInset * 2f),
                    cornerRadius = CornerRadius(cornerR - 2.dp.toPx()),
                    style = Stroke(width = 1.8.dp.toPx())
                )

                // Garis dalam halus
                drawRoundRect(
                    color = palette.goldPrimary.copy(alpha = 0.8f),
                    topLeft = Offset(innerInset, innerInset),
                    size = Size(size.width - innerInset * 2f, size.height - innerInset * 2f),
                    cornerRadius = CornerRadius((cornerR - 10.dp.toPx()).coerceAtLeast(6f)),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Rangkaian ornamen berlian kecil sepanjang bingkai atas & bawah
                val step = 18.dp.toPx()
                val xStart = 28.dp.toPx()
                val xEnd = size.width - 28.dp.toPx()
                if (xEnd > xStart) {
                    var x = xStart
                    while (x <= xEnd) {
                        drawDiamondFiligree(Offset(x, bandInset), 2.8.dp.toPx(), palette.goldPrimary.copy(alpha = 0.72f))
                        drawDiamondFiligree(Offset(x, size.height - bandInset), 2.8.dp.toPx(), palette.goldPrimary.copy(alpha = 0.72f))
                        x += step
                    }
                }

                // Rangkaian ornamen sepanjang bingkai kiri & kanan
                val yStart = 28.dp.toPx()
                val yEnd = size.height - 28.dp.toPx()
                if (yEnd > yStart) {
                    var y = yStart
                    while (y <= yEnd) {
                        drawDiamondFiligree(Offset(bandInset, y), 2.6.dp.toPx(), palette.goldPrimary.copy(alpha = 0.65f))
                        drawDiamondFiligree(Offset(size.width - bandInset, y), 2.6.dp.toPx(), palette.goldPrimary.copy(alpha = 0.65f))
                        y += step
                    }
                }

                // 4 Medali sudut Rub el Hizb (bintang 8 sudut + permata tengah)
                val medOffset = 15.dp.toPx()
                val medRadius = 11.dp.toPx()
                drawCornerShamsaMedallion(Offset(medOffset, medOffset), medRadius, palette)
                drawCornerShamsaMedallion(Offset(size.width - medOffset, medOffset), medRadius, palette)
                drawCornerShamsaMedallion(Offset(medOffset, size.height - medOffset), medRadius, palette)
                drawCornerShamsaMedallion(Offset(size.width - medOffset, size.height - medOffset), medRadius, palette)
            }
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

/**
 * Bagian Atas Bingkai Kaligrafi Mushaf (menyambung mulus dengan MushafAyahSideFrame & MushafBottomCalligraphyCap)
 * agar surah panjang seperti Al-Baqarah tetap lancar dan dapat melompat presisi ke ayat target.
 */
@Composable
fun MushafTopCalligraphyCap(
    palette: QuranPalette,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .drawBehind {
                drawRect(color = palette.cardSurface)
                val outerInset = 3.dp.toPx()
                val bandInset = 8.dp.toPx()
                val innerInset = 13.dp.toPx()

                // Pita emas lembut tepi atas, kiri & kanan
                drawLine(palette.goldGlow.copy(alpha = if (palette.isDark) 0.25f else 0.48f), Offset(outerInset, outerInset + 3.dp.toPx()), Offset(size.width - outerInset, outerInset + 3.dp.toPx()), strokeWidth = 8.dp.toPx())
                drawLine(palette.goldGlow.copy(alpha = if (palette.isDark) 0.25f else 0.48f), Offset(outerInset + 3.dp.toPx(), outerInset), Offset(outerInset + 3.dp.toPx(), size.height), strokeWidth = 8.dp.toPx())
                drawLine(palette.goldGlow.copy(alpha = if (palette.isDark) 0.25f else 0.48f), Offset(size.width - outerInset - 3.dp.toPx(), outerInset), Offset(size.width - outerInset - 3.dp.toPx(), size.height), strokeWidth = 8.dp.toPx())

                // Garis luar perunggu-emas (atas, kiri, kanan)
                drawLine(palette.goldPrimary, Offset(outerInset, outerInset), Offset(size.width - outerInset, outerInset), strokeWidth = 1.8.dp.toPx())
                drawLine(palette.goldPrimary, Offset(outerInset, outerInset), Offset(outerInset, size.height), strokeWidth = 1.8.dp.toPx())
                drawLine(palette.goldPrimary, Offset(size.width - outerInset, outerInset), Offset(size.width - outerInset, size.height), strokeWidth = 1.8.dp.toPx())

                // Garis dalam halus
                drawLine(palette.goldPrimary.copy(alpha = 0.8f), Offset(innerInset, innerInset), Offset(size.width - innerInset, innerInset), strokeWidth = 1.dp.toPx())
                drawLine(palette.goldPrimary.copy(alpha = 0.8f), Offset(innerInset, innerInset), Offset(innerInset, size.height), strokeWidth = 1.dp.toPx())
                drawLine(palette.goldPrimary.copy(alpha = 0.8f), Offset(size.width - innerInset, innerInset), Offset(size.width - innerInset, size.height), strokeWidth = 1.dp.toPx())

                // Ornamen berlian tepi atas & sisi
                val step = 18.dp.toPx()
                var x = 28.dp.toPx()
                while (x <= size.width - 28.dp.toPx()) {
                    drawDiamondFiligree(Offset(x, bandInset), 2.8.dp.toPx(), palette.goldPrimary.copy(alpha = 0.72f))
                    x += step
                }
                var y = 28.dp.toPx()
                while (y <= size.height - 6.dp.toPx()) {
                    drawDiamondFiligree(Offset(bandInset, y), 2.6.dp.toPx(), palette.goldPrimary.copy(alpha = 0.65f))
                    drawDiamondFiligree(Offset(size.width - bandInset, y), 2.6.dp.toPx(), palette.goldPrimary.copy(alpha = 0.65f))
                    y += step
                }

                // 2 Medali sudut atas Rub el Hizb
                val medOffset = 15.dp.toPx()
                val medRadius = 11.dp.toPx()
                drawCornerShamsaMedallion(Offset(medOffset, medOffset), medRadius, palette)
                drawCornerShamsaMedallion(Offset(size.width - medOffset, medOffset), medRadius, palette)
            }
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 6.dp),
        content = content
    )
}

/**
 * Bingkai Sisi Kiri-Kanan per Ayat untuk LazyColumn Mushaf yang mulus bersambung.
 */
@Composable
fun MushafAyahSideFrame(
    palette: QuranPalette,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(color = palette.cardSurface)
                val outerInset = 3.dp.toPx()
                val bandInset = 8.dp.toPx()
                val innerInset = 13.dp.toPx()

                drawLine(palette.goldGlow.copy(alpha = if (palette.isDark) 0.25f else 0.48f), Offset(outerInset + 3.dp.toPx(), 0f), Offset(outerInset + 3.dp.toPx(), size.height), strokeWidth = 8.dp.toPx())
                drawLine(palette.goldGlow.copy(alpha = if (palette.isDark) 0.25f else 0.48f), Offset(size.width - outerInset - 3.dp.toPx(), 0f), Offset(size.width - outerInset - 3.dp.toPx(), size.height), strokeWidth = 8.dp.toPx())

                drawLine(palette.goldPrimary, Offset(outerInset, 0f), Offset(outerInset, size.height), strokeWidth = 1.8.dp.toPx())
                drawLine(palette.goldPrimary, Offset(size.width - outerInset, 0f), Offset(size.width - outerInset, size.height), strokeWidth = 1.8.dp.toPx())

                drawLine(palette.goldPrimary.copy(alpha = 0.8f), Offset(innerInset, 0f), Offset(innerInset, size.height), strokeWidth = 1.dp.toPx())
                drawLine(palette.goldPrimary.copy(alpha = 0.8f), Offset(size.width - innerInset, 0f), Offset(size.width - innerInset, size.height), strokeWidth = 1.dp.toPx())

                val step = 18.dp.toPx()
                var y = 9.dp.toPx()
                while (y < size.height) {
                    drawDiamondFiligree(Offset(bandInset, y), 2.4.dp.toPx(), palette.goldPrimary.copy(alpha = 0.65f))
                    drawDiamondFiligree(Offset(size.width - bandInset, y), 2.4.dp.toPx(), palette.goldPrimary.copy(alpha = 0.65f))
                    y += step
                }
            }
            .padding(horizontal = 18.dp, vertical = 2.dp),
        content = content
    )
}

/**
 * Bagian Bawah Bingkai Kaligrafi Mushaf beserta 2 Medali Sudut Bawah.
 */
@Composable
fun MushafBottomCalligraphyCap(
    palette: QuranPalette,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
            .drawBehind {
                drawRect(color = palette.cardSurface)
                val outerInset = 3.dp.toPx()
                val bandInset = 8.dp.toPx()
                val innerInset = 13.dp.toPx()

                drawLine(palette.goldGlow.copy(alpha = if (palette.isDark) 0.25f else 0.48f), Offset(outerInset, size.height - outerInset - 3.dp.toPx()), Offset(size.width - outerInset, size.height - outerInset - 3.dp.toPx()), strokeWidth = 8.dp.toPx())
                drawLine(palette.goldGlow.copy(alpha = if (palette.isDark) 0.25f else 0.48f), Offset(outerInset + 3.dp.toPx(), 0f), Offset(outerInset + 3.dp.toPx(), size.height - outerInset), strokeWidth = 8.dp.toPx())
                drawLine(palette.goldGlow.copy(alpha = if (palette.isDark) 0.25f else 0.48f), Offset(size.width - outerInset - 3.dp.toPx(), 0f), Offset(size.width - outerInset - 3.dp.toPx(), size.height - outerInset), strokeWidth = 8.dp.toPx())

                drawLine(palette.goldPrimary, Offset(outerInset, size.height - outerInset), Offset(size.width - outerInset, size.height - outerInset), strokeWidth = 1.8.dp.toPx())
                drawLine(palette.goldPrimary, Offset(outerInset, 0f), Offset(outerInset, size.height - outerInset), strokeWidth = 1.8.dp.toPx())
                drawLine(palette.goldPrimary, Offset(size.width - outerInset, 0f), Offset(size.width - outerInset, size.height - outerInset), strokeWidth = 1.8.dp.toPx())

                drawLine(palette.goldPrimary.copy(alpha = 0.8f), Offset(innerInset, size.height - innerInset), Offset(size.width - innerInset, size.height - innerInset), strokeWidth = 1.dp.toPx())
                drawLine(palette.goldPrimary.copy(alpha = 0.8f), Offset(innerInset, 0f), Offset(innerInset, size.height - innerInset), strokeWidth = 1.dp.toPx())
                drawLine(palette.goldPrimary.copy(alpha = 0.8f), Offset(size.width - innerInset, 0f), Offset(size.width - innerInset, size.height - innerInset), strokeWidth = 1.dp.toPx())

                val step = 18.dp.toPx()
                var x = 28.dp.toPx()
                while (x <= size.width - 28.dp.toPx()) {
                    drawDiamondFiligree(Offset(x, size.height - bandInset), 2.8.dp.toPx(), palette.goldPrimary.copy(alpha = 0.72f))
                    x += step
                }

                val medOffset = 15.dp.toPx()
                val medRadius = 11.dp.toPx()
                drawCornerShamsaMedallion(Offset(medOffset, size.height - medOffset), medRadius, palette)
                drawCornerShamsaMedallion(Offset(size.width - medOffset, size.height - medOffset), medRadius, palette)
            }
            .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 18.dp),
        content = content
    )
}

private fun DrawScope.drawDiamondFiligree(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius, center.y)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - radius, center.y)
        close()
    }
    drawPath(path, color = color)
}

private fun DrawScope.drawCornerShamsaMedallion(
    center: Offset,
    radius: Float,
    palette: QuranPalette
) {
    drawCircle(color = palette.cardSurface, radius = radius * 0.92f, center = center)
    val squareSide = radius * 1.25f
    val topLeft = Offset(center.x - squareSide / 2f, center.y - squareSide / 2f)
    rotate(degrees = 0f, pivot = center) {
        drawRect(
            color = palette.goldBright.copy(alpha = 0.45f),
            topLeft = topLeft,
            size = Size(squareSide, squareSide)
        )
        drawRect(
            color = palette.goldPrimary,
            topLeft = topLeft,
            size = Size(squareSide, squareSide),
            style = Stroke(width = 1.2.dp.toPx())
        )
    }
    rotate(degrees = 45f, pivot = center) {
        drawRect(
            color = palette.goldGlow.copy(alpha = 0.55f),
            topLeft = topLeft,
            size = Size(squareSide, squareSide)
        )
        drawRect(
            color = palette.goldDeep,
            topLeft = topLeft,
            size = Size(squareSide, squareSide),
            style = Stroke(width = 1.2.dp.toPx())
        )
    }
    drawCircle(
        color = palette.goldBright,
        radius = radius * 0.34f,
        center = center
    )
    drawCircle(
        color = palette.goldDeep,
        radius = radius * 0.16f,
        center = center
    )
}

/**
 * Cartouche (Unwan) Kaligrafi Kepala Surah berhias lengkung kubah di kiri & kanan.
 */
@Composable
fun SurahCalligraphyCartouche(
    surah: QuranSurahSpec,
    palette: QuranPalette,
    arabicFontFamily: FontFamily,
    isEnglish: Boolean,
    modifier: Modifier = Modifier
) {
    val meaning = if (isEnglish) surah.meaningEn else surah.meaningId
    val ayahWord = if (isEnglish) "Ayahs" else "Ayat"
    val arabNumber = QuranCatalog.toArabicIndicDigits(surah.number)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .drawBehind {
                // Latar gradien pita emas hangat
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            palette.goldGlow.copy(alpha = if (palette.isDark) 0.25f else 0.75f),
                            palette.cardSurface,
                            palette.goldGlow.copy(alpha = if (palette.isDark) 0.25f else 0.75f)
                        )
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )

                val pad = 4.dp.toPx()
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(palette.goldDeep, palette.goldBright, palette.goldDeep)
                    ),
                    topLeft = Offset(pad, pad),
                    size = Size(size.width - pad * 2f, size.height - pad * 2f),
                    cornerRadius = CornerRadius(13.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Lengkungan kubah ornamen simetris kiri & kanan
                val archW = 24.dp.toPx()
                val cy = size.height / 2f
                val leftArch = Path().apply {
                    moveTo(pad, pad + 8.dp.toPx())
                    quadraticTo(pad + archW, cy, pad, size.height - pad - 8.dp.toPx())
                }
                val rightArch = Path().apply {
                    moveTo(size.width - pad, pad + 8.dp.toPx())
                    quadraticTo(size.width - pad - archW, cy, size.width - pad, size.height - pad - 8.dp.toPx())
                }
                drawPath(leftArch, color = palette.goldPrimary, style = Stroke(width = 1.6.dp.toPx()))
                drawPath(rightArch, color = palette.goldPrimary, style = Stroke(width = 1.6.dp.toPx()))

                drawCircle(color = palette.goldBright, radius = 3.5.dp.toPx(), center = Offset(pad + 10.dp.toPx(), cy))
                drawCircle(color = palette.goldBright, radius = 3.5.dp.toPx(), center = Offset(size.width - pad - 10.dp.toPx(), cy))
            }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "سُورَةُ ${surah.nameArabic}",
                fontFamily = arabicFontFamily,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = palette.goldDeep,
                textAlign = TextAlign.Center
            )
            Text(
                text = "$arabNumber • ${surah.number}. ${surah.nameLatin} ($meaning)",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = palette.inkArabic,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = palette.cardSoft,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, palette.goldPrimary.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = surah.revelationType,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.goldDeep,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = palette.cardSoft,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, palette.goldPrimary.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "${surah.versesCount} $ayahWord • ${surah.juzRange}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.goldDeep,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Kaligrafi Bismillah di awal Surah (kecuali At-Taubah & Al-Fatihah ayat 1 sendiri).
 */
@Composable
fun BismillahOrnamentBanner(
    palette: QuranPalette,
    arabicFontFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TezhipWingFlourish(palette = palette, flipHorizontal = false)
        Spacer(Modifier.width(8.dp))
        Text(
            text = QuranCatalog.BISMILLAH_ARABIC,
            fontFamily = arabicFontFamily,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = palette.inkArabic,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(8.dp))
        TezhipWingFlourish(palette = palette, flipHorizontal = true)
    }
}

@Composable
private fun TezhipWingFlourish(
    palette: QuranPalette,
    flipHorizontal: Boolean
) {
    Canvas(modifier = Modifier.size(width = 30.dp, height = 18.dp)) {
        val w = size.width
        val h = size.height
        val cy = h / 2f
        val startX = if (flipHorizontal) 0f else w
        val endX = if (flipHorizontal) w else 0f
        val midX = w * 0.5f

        val upperCurve = Path().apply {
            moveTo(startX, cy)
            quadraticTo(midX, cy - h * 0.45f, endX, cy - 2.dp.toPx())
        }
        val lowerCurve = Path().apply {
            moveTo(startX, cy)
            quadraticTo(midX, cy + h * 0.45f, endX, cy + 2.dp.toPx())
        }
        drawPath(upperCurve, color = palette.goldPrimary, style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round))
        drawPath(lowerCurve, color = palette.goldPrimary, style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(color = palette.goldBright, radius = 2.6.dp.toPx(), center = Offset(midX, cy))
        drawCircle(color = palette.goldDeep, radius = 1.6.dp.toPx(), center = Offset(endX, cy))
    }
}

/**
 * Medali Roset Emas Nomor Ayat (۝١, ۝٢) bergaya Mushaf Utsmani.
 */
@Composable
fun ArabicAyahRosetteBadge(
    ayahNumber: Int,
    isPlaying: Boolean,
    isBookmarked: Boolean,
    palette: QuranPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val arabicDigits = QuranCatalog.toArabicIndicDigits(ayahNumber)
    val fontSize = when {
        ayahNumber >= 100 -> 10.5.sp
        ayahNumber >= 10 -> 12.sp
        else -> 13.5.sp
    }

    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(36.dp)) {
            val c = center
            val outerR = size.minDimension * 0.46f
            val innerR = size.minDimension * 0.34f

            if (isPlaying) {
                drawCircle(
                    color = palette.goldBright.copy(alpha = 0.45f),
                    radius = size.minDimension * 0.50f,
                    center = c
                )
            }

            // 8 kelopak roset sunburst emas
            val petalCount = 8
            val petalPath = Path()
            for (i in 0 until petalCount * 2) {
                val angle = (i * Math.PI / petalCount).toFloat()
                val r = if (i % 2 == 0) outerR else innerR
                val px = c.x + r * cos(angle)
                val py = c.y + r * sin(angle)
                if (i == 0) petalPath.moveTo(px, py) else petalPath.lineTo(px, py)
            }
            petalPath.close()

            drawPath(
                path = petalPath,
                color = when {
                    isPlaying -> palette.goldBright
                    isBookmarked -> palette.goldPrimary.copy(alpha = 0.9f)
                    else -> palette.goldGlow.copy(alpha = if (palette.isDark) 0.35f else 0.72f)
                }
            )
            drawPath(
                path = petalPath,
                color = if (isPlaying || isBookmarked) palette.goldDeep else palette.goldPrimary,
                style = Stroke(width = 1.2.dp.toPx())
            )
            drawCircle(
                color = palette.cardSurface,
                radius = innerR * 0.78f,
                center = c
            )
            drawCircle(
                color = palette.goldPrimary.copy(alpha = 0.55f),
                radius = innerR * 0.78f,
                center = c,
                style = Stroke(width = 0.8.dp.toPx())
            )
        }
        Text(
            text = arabicDigits,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = if (isPlaying || isBookmarked) palette.goldDeep else palette.inkArabic
        )
    }
}
