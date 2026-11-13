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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChildStudent
import com.example.util.StudentCardRenderer

@Composable
fun StudentIdCardPreview(
    student: ChildStudent,
    schoolName: String,
    modifier: Modifier = Modifier
) {
    val palette = remember(student.jenjang, student.classGrade, student.gender, student.dormitory, student.roomNumber) {
        StudentCardRenderer.resolvePalette(student)
    }
    val qrPayload = remember(student.nik, student.nis, student.id) {
        StudentCardRenderer.resolveQrPayload(student)
    }
    val qrBitmap = remember(qrPayload) {
        StudentCardRenderer.generateQrBitmap(qrPayload, sizePx = 360)
    }

    val startColor = Color(palette.gradientStartArgb)
    val midColor = Color(palette.gradientMidArgb)
    val endColor = Color(palette.gradientEndArgb)
    val headerBg = Color(palette.headerOverlayArgb).copy(alpha = 0.88f)
    val neonColor = Color(palette.trimNeonArgb)
    val glowColor = Color(palette.trimSoftGlowArgb)
    val genderBadgeBg = Color(palette.genderBadgeBgArgb)
    val cleanSchool = schoolName.trim().ifBlank { "Wali Asuh Care School" }.uppercase()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .border(width = 2.5.dp, color = neonColor, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = startColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(startColor, midColor, endColor)
                    )
                )
        ) {
            // Ornamen geometris halus latar belakang kartu
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = size.height * 0.34f,
                    center = Offset(size.width * 0.88f, size.height * 0.18f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = size.height * 0.28f,
                    center = Offset(size.width * 0.12f, size.height * 0.94f)
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Garis Pemanis Atas Menyala (Hijau Menyala / Pink Menyala)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(neonColor)
                )

                // Header Nama Sekolah & Badge Jenjang + Gender
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(23.dp)
                                .clip(RoundedCornerShape(6.dp))
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
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )

                        Box(
                            modifier = Modifier
                                .size(23.dp)
                                .clip(RoundedCornerShape(6.dp))
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

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cleanSchool,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "KARTU IDENTITAS ANAK ASUH",
                            color = glowColor,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(genderBadgeBg)
                            .border(1.2.dp, neonColor, RoundedCornerShape(50))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${palette.jenjang.label} • ${palette.genderLabel}",
                            color = neonColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }

                // Garis Pemanis Bawah Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(glowColor.copy(alpha = 0.78f))
                )

                // Isi Utama Kartu: Sisi Kiri (Garis Vertikal + Biodata) & Sisi Kanan (QR Code Besar)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Garis pemanis vertikal sisi kiri
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .fillMaxHeight()
                            .background(neonColor)
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kolom Biodata
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = student.name.trim().uppercase(),
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .width(54.dp)
                                    .height(2.5.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(neonColor)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            CardFieldLine(label = "NISN", value = student.nis.ifBlank { "-" })
                            val kelasInfo = buildString {
                                append(student.classGrade.ifBlank { "-" })
                                if (student.dormitory.isNotBlank()) {
                                    append(" • ")
                                    append(student.dormitory)
                                }
                            }
                            CardFieldLine(label = "Kelas", value = kelasInfo)
                            CardFieldLine(
                                label = "Alamat",
                                value = student.address.trim().ifBlank { "-" },
                                maxLines = 2
                            )
                        }

                        // Kolom Kanan: QR Code Besar (Berisi NIK)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .border(1.8.dp, neonColor, RoundedCornerShape(10.dp))
                                    .padding(4.dp),
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
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "NIK: ${student.nik.ifBlank { student.nis.ifBlank { student.id.toString() } }}",
                                color = Color.White,
                                fontSize = 7.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(96.dp)
                            )
                        }
                    }
                }

                // Garis Pemanis Bawah Menyala
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(neonColor)
                )
            }
        }
    }
}

@Composable
private fun CardFieldLine(
    label: String,
    value: String,
    maxLines: Int = 1
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.80f),
            fontSize = 8.8.sp,
            modifier = Modifier.width(38.dp)
        )
        Text(
            text = ": ",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 8.8.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
