package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HaidDateCalculator
import com.example.data.model.HaidRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Timeline berjalan 1 s.d. 15 hari untuk Daftar Haid aktif.
 */
@Composable
fun HaidTimelineBar(
    currentDay: Int,
    maxDays: Int = HaidDateCalculator.MAX_HAID_DAYS,
    modifier: Modifier = Modifier
) {
    val clampedDay = currentDay.coerceIn(1, maxDays)
    val accentColor = when {
        clampedDay >= maxDays -> Color(0xFFDC2626)
        clampedDay >= 12 -> Color(0xFFD97706)
        else -> Color(0xFFDB2777)
    }
    val trackBgColor = when {
        clampedDay >= maxDays -> Color(0xFFFEE2E2)
        clampedDay >= 12 -> Color(0xFFFEF3C7)
        else -> Color(0xFFFCE7F3)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Timeline Haid Berjalan",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }
            Text(
                text = "Hari ke-$clampedDay / $maxDays",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }

        // 15 segmen harian agar pengasuh langsung melihat posisi hari berjalan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (day in 1..maxDays) {
                val isFilled = day <= clampedDay
                val isCurrent = day == clampedDay
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isCurrent) 8.dp else 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            when {
                                isCurrent -> accentColor
                                isFilled -> accentColor.copy(alpha = 0.72f)
                                else -> trackBgColor
                            }
                        )
                        .align(Alignment.CenterVertically)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Hari 1", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Umum 6-7h", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = if (clampedDay >= maxDays) "Hari 15 (Besok Auto Suci)" else "Maks 15h",
                fontSize = 9.5.sp,
                fontWeight = if (clampedDay >= maxDays) FontWeight.Bold else FontWeight.Normal,
                color = if (clampedDay >= maxDays) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Timeline masa suci berjalan (Hari ke-1 s.d. 15 minimal suci, dan hari ke-15+ terpenuhi).
 */
@Composable
fun SuciTimelineBar(
    suciDay: Int,
    minSuciDays: Int = HaidDateCalculator.MIN_SUCI_DAYS,
    modifier: Modifier = Modifier
) {
    val isMinimumMet = suciDay >= minSuciDays
    val progressDays = suciDay.coerceIn(1, minSuciDays)
    val accentColor = if (isMinimumMet) Color(0xFF16A34A) else Color(0xFF0284C7)
    val trackBgColor = if (isMinimumMet) Color(0xFFDCFCE7) else Color(0xFFE0F2FE)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.75f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isMinimumMet) Icons.Default.CheckCircle else Icons.Default.Spa,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (isMinimumMet) "Masa Suci Berjalan" else "Timeline Masa Minimal Suci",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }
            Text(
                text = if (isMinimumMet) "Suci Hari ke-$suciDay" else "Suci Hari ke-$suciDay / $minSuciDays",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (day in 1..minSuciDays) {
                val isFilled = day <= progressDays
                val isCurrent = !isMinimumMet && day == progressDays
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isCurrent) 8.dp else 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            when {
                                isCurrent -> accentColor
                                isFilled -> accentColor.copy(alpha = 0.75f)
                                else -> trackBgColor
                            }
                        )
                        .align(Alignment.CenterVertically)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Suci H-1", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = if (isMinimumMet) {
                    "Minimal 15 hari suci terpenuhi"
                } else {
                    "Masa pantau ketat (<15 hari suci)"
                },
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                color = accentColor
            )
        }
    }
}

/**
 * Pop-up Peringatan & Verifikasi Tes Tisu saat kartu siswi yang masih di Daftar Suci (< 15 hari)
 * di-scan atau dipilih untuk dilaporkan haid lagi.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuciTissueVerificationSheet(
    studentName: String,
    suciRecord: HaidRecord,
    onCancelStaySuci: () -> Unit,
    onConfirmMoveToHaid: () -> Unit
) {
    val suciDay = suciRecord.suciDayCount
    val suciDateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    val suciSinceText = suciDateFormat.format(Date(suciRecord.effectiveEndDate ?: suciRecord.endDate ?: suciRecord.startDate))

    ModalBottomSheet(
        onDismissRequest = onCancelStaySuci,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Peringatan
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Terindikasi Masih di Daftar Suci!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB91C1C)
                        )
                        Text(
                            text = "Suci Hari ke-$suciDay (belum genap 15 hari masa suci)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                // Detail status siswi di Daftar Suci
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Siswi \"$studentName\" tercatat sudah mandi suci sejak $suciSinceText dan saat ini baru memasuki Daftar Suci Hari ke-$suciDay.",
                            fontSize = 12.5.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 18.sp
                        )
                        Text(
                            text = "Karena belum melewati batas minimal suci 15 hari, klaim haid ini terindikasi tidak sesuai / berbohong (atau darah penyakit/istihadhah).",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E),
                            lineHeight = 17.sp
                        )
                    }
                }

                // Instruksi Wajib Tes Darah Dengan Tisu
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier
                                .size(22.dp)
                                .padding(top = 2.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Wajib Cek Bukti Sebelum Memindahkan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Pastikan siswi benar-benar haid! Minta siswi mengambil tes/sampel darah menggunakan tisu untuk membuktikan memang benar keluar darah.",
                                fontSize = 12.sp,
                                color = Color(0xFF7F1D1D),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // Tombol Aksi
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onConfirmMoveToHaid,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_confirm_tissue_move_haid")
                    ) {
                        Text(
                            text = "Sudah Tes Tisu & Benar Haid • Pindah ke Haid Hari ke-...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = onCancelStaySuci,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_cancel_tissue_verification")
                    ) {
                        Text(
                            text = "Batal, Siswi Tetap di Daftar Suci",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
