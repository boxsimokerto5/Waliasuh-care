package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.ChildStudent
import com.example.util.StudentCardRenderer
import com.example.util.StudentClassifier
import com.example.util.StudentCsvParser
import com.example.util.i18n.LocalAppStrings
import java.io.File

@Composable
fun StudentIdCardPreview(
    student: ChildStudent,
    schoolName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    val palette = remember(student.jenjang, student.classGrade, student.gender, student.dormitory, student.roomNumber) {
        StudentCardRenderer.resolvePalette(student)
    }
    val qrPayload = remember(student.nik, student.nis, student.id) {
        StudentCardRenderer.resolveQrPayload(student)
    }
    val qrBitmap = remember(qrPayload) {
        StudentCardRenderer.generateQrBitmap(qrPayload, sizePx = 480)
    }

    val primaryPhotoUrl = student.photoUrl.trim()
    val fallbackPhotoUrl = student.photoFallbackUrl.trim()
    var useFallbackPhoto by remember(primaryPhotoUrl, fallbackPhotoUrl) { mutableStateOf(false) }
    var photoLoadFailed by remember(primaryPhotoUrl, fallbackPhotoUrl) { mutableStateOf(false) }
    val activePhotoUrl = when {
        photoLoadFailed -> ""
        !useFallbackPhoto && primaryPhotoUrl.isNotBlank() -> primaryPhotoUrl
        fallbackPhotoUrl.isNotBlank() -> fallbackPhotoUrl
        else -> ""
    }

    val startColor = Color(palette.gradientStartArgb)
    val midColor = Color(palette.gradientMidArgb)
    val endColor = Color(palette.gradientEndArgb)
    val accentColor = Color(palette.trimNeonArgb)
    val softGlowColor = Color(palette.trimSoftGlowArgb)
    val genderBadgeBg = Color(palette.genderBadgeBgArgb)
    val genderBadgeText = Color(palette.genderBadgeTextArgb)
    val cleanSchool = schoolName.trim().ifBlank { "Wali Asuh Care School" }.uppercase()
    val isMale = StudentClassifier.isMaleStudent(student)
    val photoFallbackGradient = remember(isMale) {
        if (isMale) {
            listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
        } else {
            listOf(Color(0xFFFDE8E8), Color(0xFFFBCFE8))
        }
    }
    val initials = remember(student.name) {
        student.name.trim()
            .split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifBlank { "SA" }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .border(width = 1.5.dp, color = accentColor, shape = RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Watermark geometris lembut bernuansa jenjang di latar putih
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = midColor.copy(alpha = 0.05f),
                    radius = size.height * 0.34f,
                    center = Offset(size.width * 0.90f, size.height * 0.85f)
                )
                drawCircle(
                    color = midColor.copy(alpha = 0.04f),
                    radius = size.height * 0.24f,
                    center = Offset(size.width * 0.10f, size.height * 0.95f)
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Strip pemanis paling atas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(accentColor)
                )

                // Header Sekolah Berwarna Jenjang (3 Logo + Nama Sekolah 2 baris utuh + Subjudul)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(startColor, midColor, endColor)
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(21.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_kemensos),
                                contentDescription = "Logo Kementerian Sosial RI",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.logo_sr_care),
                            contentDescription = "Logo Wali Asuh Care",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )

                        Box(
                            modifier = Modifier
                                .size(21.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_sekolah_rakyat),
                                contentDescription = "Logo Sekolah Rakyat",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(7.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cleanSchool,
                            color = Color.White,
                            fontSize = if (cleanSchool.length > 36) 8.8.sp else 9.6.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = if (isEn) "STUDENT IDENTITY CARD" else "KARTU IDENTITAS ANAK ASUH",
                            color = softGlowColor,
                            fontSize = 6.6.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Garis pemanis pembatas header & body putih
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(accentColor)
                )

                // Isi Utama 3 Kolom: Pas Foto (Kiri) + Biodata (Tengah) + Micro-Pill & QR Tajam (Kanan)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(accentColor)
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kolom 1 (Kiri): Pas Foto Siswa 3:4
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(58.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Brush.linearGradient(photoFallbackGradient))
                                .border(1.2.dp, accentColor, RoundedCornerShape(7.dp))
                                .testTag("card_student_photo_slot"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isMale) Color(0xFF0369A1) else Color(0xFF9D174D),
                                modifier = Modifier.testTag("card_student_photo_fallback")
                            )

                            if (activePhotoUrl.isNotBlank()) {
                                val requestData: Any = if (activePhotoUrl.startsWith("/")) {
                                    File(activePhotoUrl)
                                } else {
                                    activePhotoUrl
                                }
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(requestData)
                                        .crossfade(true)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                        .build(),
                                    contentDescription = if (isEn) "Student photo ${student.name}" else "Pas foto ${student.name}",
                                    contentScale = ContentScale.Crop,
                                    onError = {
                                        if (!useFallbackPhoto && fallbackPhotoUrl.isNotBlank() && fallbackPhotoUrl != primaryPhotoUrl) {
                                            useFallbackPhoto = true
                                        } else {
                                            photoLoadFailed = true
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(7.dp))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(7.dp))

                        // Kolom 2 (Tengah): Nama Siswa + Garis Aksen + Biodata (NIK utuh tanpa terpotong)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = student.name.trim().uppercase(),
                                color = Color(0xFF0F172A),
                                fontSize = 10.6.sp,
                                lineHeight = 12.2.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.5.dp))

                            Box(
                                modifier = Modifier
                                    .width(34.dp)
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(accentColor)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            CardFieldLine(label = "NISN", value = student.nis.ifBlank { "-" })
                            val cleanNik = StudentCsvParser.cleanDigitsOnly(student.nik)
                                .ifBlank { student.nik.trim().ifBlank { "-" } }
                            CardFieldLine(
                                label = "NIK",
                                value = cleanNik,
                                useMonospace = true
                            )
                            val kelasInfo = buildString {
                                append(student.classGrade.ifBlank { "-" })
                                if (student.dormitory.isNotBlank()) {
                                    append(" • ")
                                    append(student.dormitory)
                                }
                            }
                            CardFieldLine(label = if (isEn) "Class" else "Kelas", value = kelasInfo)
                            CardFieldLine(
                                label = if (isEn) "Address" else "Alamat",
                                value = student.address.trim().ifBlank { "-" },
                                maxLines = 2,
                                isMutedValue = true
                            )
                        }

                        // Kolom 3 (Kanan): Micro-Pill Jenjang • Gender + QR Code Tajam + Caption
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(genderBadgeBg)
                                    .border(0.9.dp, accentColor, RoundedCornerShape(50))
                                    .padding(horizontal = 5.dp, vertical = 1.5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${palette.jenjang.label} • ${palette.genderLabel}",
                                    color = genderBadgeText,
                                    fontSize = 6.3.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.2.dp, accentColor, RoundedCornerShape(8.dp))
                                    .padding(3.dp)
                                    .testTag("card_student_qr_box"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (qrBitmap != null) {
                                    Image(
                                        painter = BitmapPainter(
                                            image = qrBitmap.asImageBitmap(),
                                            filterQuality = FilterQuality.None
                                        ),
                                        contentDescription = "QR NIK ${student.name}",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(1.dp))

                            Text(
                                text = "SCAN QR NIK",
                                color = Color(0xFF64748B),
                                fontSize = 5.8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardFieldLine(
    label: String,
    value: String,
    maxLines: Int = 1,
    useMonospace: Boolean = false,
    isMutedValue: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = Color(0xFF64748B),
            fontSize = 7.8.sp,
            modifier = Modifier.width(if (label.length > 6) 36.dp else 30.dp)
        )
        Text(
            text = ": ",
            color = Color(0xFF64748B),
            fontSize = 7.8.sp
        )
        Text(
            text = value,
            color = if (isMutedValue) Color(0xFF334155) else Color(0xFF0F172A),
            fontSize = if (useMonospace) 7.6.sp else 7.9.sp,
            lineHeight = 9.8.sp,
            fontFamily = if (useMonospace) FontFamily.Monospace else FontFamily.Default,
            fontWeight = if (isMutedValue) FontWeight.Medium else FontWeight.SemiBold,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
