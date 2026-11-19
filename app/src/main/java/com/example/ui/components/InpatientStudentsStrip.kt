package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicalRecord
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings
import java.util.concurrent.TimeUnit

private data class InpatientCardPalette(
    val gradientStart: Color,
    val gradientEnd: Color,
    val borderColor: Color,
    val accentColor: Color,
    val chipBg: Color
)

private val inpatientCardPalettes = listOf(
    InpatientCardPalette(
        gradientStart = Color(0xFFFFF5F5),
        gradientEnd = Color(0xFFFFE4E6),
        borderColor = Color(0xFFFCA5A5),
        accentColor = Color(0xFFB91C1C),
        chipBg = Color(0xFFFEE2E2)
    ),
    InpatientCardPalette(
        gradientStart = Color(0xFFFFF7ED),
        gradientEnd = Color(0xFFFFEDD5),
        borderColor = Color(0xFFFDBA74),
        accentColor = Color(0xFFC2410C),
        chipBg = Color(0xFFFFEDD5)
    ),
    InpatientCardPalette(
        gradientStart = Color(0xFFFDF2F8),
        gradientEnd = Color(0xFFFCE7F3),
        borderColor = Color(0xFFF9A8D4),
        accentColor = Color(0xFFBE185D),
        chipBg = Color(0xFFFCE7F3)
    )
)

@Composable
fun InpatientStudentsStrip(
    records: List<MedicalRecord>,
    onRecordClick: (MedicalRecord) -> Unit,
    onOpenAllInpatients: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (records.isEmpty()) return

    val isEn = LocalAppStrings.current.isEnglish
    val listState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("inpatient_students_strip"),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = if (isEn) "Currently Inpatient" else "Sedang Rawat Inap",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFFEE2E2))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isEn) {
                            "${records.size} ${if (records.size == 1) "Student" else "Students"}"
                        } else {
                            "${records.size} Siswa"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB91C1C),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenAllInpatients)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
                    .testTag("btn_see_all_inpatients")
            ) {
                Text(
                    text = if (isEn) "View All" else "Semua",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFB91C1C)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (isEn) "View all inpatient students" else "Lihat semua siswa rawat inap",
                    tint = Color(0xFFB91C1C),
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cardWidth = if (records.size == 1) {
                maxWidth
            } else {
                (maxWidth * 0.85f).coerceIn(256.dp, 360.dp)
            }

            LazyRow(
                state = listState,
                flingBehavior = snapBehavior,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inpatient_students_carousel"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = if (records.size > 1) 4.dp else 0.dp)
            ) {
                itemsIndexed(
                    items = records,
                    key = { _, rec -> "inpatient_card_${rec.id}" }
                ) { index, rec ->
                    val palette = inpatientCardPalettes[index % inpatientCardPalettes.size]
                    SlimInpatientStudentCard(
                        record = rec,
                        palette = palette,
                        onClick = { onRecordClick(rec) },
                        modifier = Modifier.width(cardWidth)
                    )
                }
            }
        }
    }
}

@Composable
private fun SlimInpatientStudentCard(
    record: MedicalRecord,
    palette: InpatientCardPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    val startMillis = record.treatmentDate ?: record.planDate
    val daysCount = (TimeUnit.MILLISECONDS.toDays((System.currentTimeMillis() - startMillis).coerceAtLeast(0L)) + 1L)
        .coerceAtLeast(1L)

    val faskesDisplay = record.faskes.ifBlank {
        if (isEn) "Referral facility not set" else "Lokasi faskes belum diisi"
    }
    val roomDisplay = record.inpatientRoom.ifBlank {
        if (isEn) "Room not specified" else "Kamar belum diisi"
    }
    val diagnosisDisplay = record.diagnosis.ifBlank {
        record.complaint.ifBlank {
            if (isEn) "Under medical observation" else "Dalam observasi dokter"
        }
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("card_inpatient_student_${record.id}"),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.2.dp, palette.borderColor),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(palette.gradientStart, palette.gradientEnd)
                    )
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Baris Badge: RAWAT INAP + Hari ke-X + Detail ›
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.chipBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isEn) "INPATIENT" else "RAWAT INAP",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.accentColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.78f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isEn) "Day $daysCount" else "Hari ke-$daysCount",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.accentColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = if (isEn) "Detail ›" else "Detail ›",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.accentColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Nama Siswa + Asrama
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = record.studentName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (record.studentDormitory.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.72f))
                                .padding(horizontal = 6.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = record.studentDormitory,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 1. Lokasi Faskes
                InpatientInfoRow(
                    icon = Icons.Default.LocalHospital,
                    label = if (isEn) "Facility" else "Faskes",
                    value = faskesDisplay,
                    accentColor = palette.accentColor,
                    maxLines = 1
                )

                // 2. Kamar / Ruang
                InpatientInfoRow(
                    icon = Icons.Default.MeetingRoom,
                    label = if (isEn) "Room" else "Kamar",
                    value = roomDisplay,
                    accentColor = palette.accentColor,
                    maxLines = 1
                )

                // 3. Diagnosa
                InpatientInfoRow(
                    icon = Icons.Default.MedicalServices,
                    label = if (isEn) "Diagnosis" else "Diagnosa",
                    value = diagnosisDisplay,
                    accentColor = palette.accentColor,
                    maxLines = 2
                )

                // Footer: Tanggal masuk & Pendamping
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (record.escortName.isNotBlank()) {
                            "${if (isEn) "Escort" else "Pendamping"}: ${record.escortName}"
                        } else {
                            if (isEn) "Tap for medical details" else "Ketuk untuk lihat detail lengkap"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.accentColor.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = DateUtils.formatShortDate(startMillis),
                        fontSize = 10.sp,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun InpatientInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    maxLines: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accentColor,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(13.dp)
        )
        Text(
            text = "$label: $value",
            fontSize = 11.5.sp,
            lineHeight = 15.sp,
            color = Color(0xFF334155),
            fontWeight = FontWeight.Medium,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
