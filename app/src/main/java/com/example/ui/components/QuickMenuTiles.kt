package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.WaliPrimary
import kotlinx.coroutines.launch

@Composable
fun QuickMenuTiles(
    onNavigateToPlan: () -> Unit,
    onNavigateToDirectTreatment: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onShowPuskesmasInfo: () -> Unit,
    onOpenShiftMessages: () -> Unit = {},
    onOpenCreateShiftMessage: () -> Unit = {},
    studentsCount: Int = 0,
    activeShiftMessagesCount: Int = 0,
    urgentShiftMessagesCount: Int = 0,
    schoolShortName: String = "",
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Horizontal Pager: Ikon besar dapat digeser ke kiri dan kanan
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (page == 0) {
                    // ==========================================
                    // HALAMAN 1: LAYANAN UTAMA KESEHATAN ANAK
                    // ==========================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CartoonMenuCard(
                            title = "Rencana Berobat",
                            subtitle = "Jadwal Puskesmas",
                            imageRes = R.drawable.img_menu_rencana,
                            badgeText = "Rujukan",
                            badgeBg = Color(0xFFE0F2FE),
                            badgeColor = WaliPrimary,
                            bgGradient = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE).copy(alpha = 0.5f)),
                            borderColor = Color(0xFFBAE6FD),
                            onClick = onNavigateToPlan,
                            testTag = "tile_rencana_berobat",
                            modifier = Modifier.weight(1f)
                        )

                        CartoonMenuCard(
                            title = "Tindakan Medis",
                            subtitle = "Input Pengobatan",
                            imageRes = R.drawable.img_menu_tindakan,
                            badgeText = "Langsung",
                            badgeBg = Color(0xFFFEF3C7),
                            badgeColor = Color(0xFFD97706),
                            bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.5f)),
                            borderColor = Color(0xFFFDE68A),
                            onClick = onNavigateToDirectTreatment,
                            testTag = "tile_tindakan_langsung",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CartoonMenuCard(
                            title = "Rekam Medis",
                            subtitle = "Riwayat & Diagnosa",
                            imageRes = R.drawable.img_menu_rekam_medis,
                            badgeText = "Resep Obat",
                            badgeBg = Color(0xFFDCFCE7),
                            badgeColor = Color(0xFF16A34A),
                            bgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7).copy(alpha = 0.5f)),
                            borderColor = Color(0xFFBBF7D0),
                            onClick = onNavigateToRecords,
                            testTag = "tile_rekam_medis",
                            modifier = Modifier.weight(1f)
                        )

                        val studentSubtitle = if (studentsCount > 0) {
                            if (schoolShortName.isNotBlank()) "$studentsCount Siswa $schoolShortName" else "$studentsCount Siswa Terdaftar"
                        } else {
                            "Data Siswa Asrama"
                        }

                        CartoonMenuCard(
                            title = "Anak Asuh",
                            subtitle = studentSubtitle,
                            imageRes = R.drawable.img_menu_anak_asuh,
                            badgeText = "Data Asrama",
                            badgeBg = Color(0xFFF3E8FF),
                            badgeColor = Color(0xFF9333EA),
                            bgGradient = listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF).copy(alpha = 0.5f)),
                            borderColor = Color(0xFFE9D5FF),
                            onClick = onNavigateToStudents,
                            testTag = "tile_anak_asuh",
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // ==========================================
                    // HALAMAN 2: LAYANAN OPERASIONAL & PESAN SHIFT
                    // ==========================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val shiftBadgeText = when {
                            urgentShiftMessagesCount > 0 -> "$urgentShiftMessagesCount URGENT"
                            activeShiftMessagesCount > 0 -> "$activeShiftMessagesCount Pesan"
                            else -> "Operan Jaga"
                        }
                        val shiftBadgeBg = when {
                            urgentShiftMessagesCount > 0 -> Color(0xFFFEE2E2)
                            activeShiftMessagesCount > 0 -> Color(0xFFFEF3C7)
                            else -> Color(0xFFE0F2FE)
                        }
                        val shiftBadgeColor = when {
                            urgentShiftMessagesCount > 0 -> Color(0xFFDC2626)
                            activeShiftMessagesCount > 0 -> Color(0xFFD97706)
                            else -> WaliPrimary
                        }

                        CartoonMenuCard(
                            title = "Pesan Shift",
                            subtitle = "Operan Jaga Asrama",
                            imageRes = R.drawable.img_menu_pesan_shift,
                            badgeText = shiftBadgeText,
                            badgeBg = shiftBadgeBg,
                            badgeColor = shiftBadgeColor,
                            bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.55f)),
                            borderColor = Color(0xFFFDE68A),
                            onClick = onOpenShiftMessages,
                            testTag = "tile_pesan_shift_berikutnya",
                            modifier = Modifier.weight(1f)
                        )

                        CartoonMenuCard(
                            title = "Tulis Pesan",
                            subtitle = "Voice to Text (Mic)",
                            imageRes = R.drawable.img_doctor_panda,
                            badgeText = "Input Suara",
                            badgeBg = Color(0xFFDCFCE7),
                            badgeColor = Color(0xFF16A34A),
                            bgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7).copy(alpha = 0.55f)),
                            borderColor = Color(0xFFBBF7D0),
                            onClick = onOpenCreateShiftMessage,
                            testTag = "tile_tulis_pesan_shift",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CartoonMenuCard(
                            title = "Riwayat Operan",
                            subtitle = "Daftar Serah Terima",
                            imageRes = R.drawable.img_menu_rekam_medis,
                            badgeText = "Riwayat Jaga",
                            badgeBg = Color(0xFFF1F5F9),
                            badgeColor = Color(0xFF475569),
                            bgGradient = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)),
                            borderColor = Color(0xFFCBD5E1),
                            onClick = onOpenShiftMessages,
                            testTag = "tile_riwayat_operan",
                            modifier = Modifier.weight(1f)
                        )

                        CartoonMenuCard(
                            title = "Data Asrama",
                            subtitle = if (schoolShortName.isNotBlank()) schoolShortName else "Sekolah Rakyat",
                            imageRes = R.drawable.img_menu_anak_asuh,
                            badgeText = "Daftar Asrama",
                            badgeBg = Color(0xFFF3E8FF),
                            badgeColor = Color(0xFF9333EA),
                            bgGradient = listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF).copy(alpha = 0.5f)),
                            borderColor = Color(0xFFE9D5FF),
                            onClick = onNavigateToStudents,
                            testTag = "tile_asrama_page2",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Pager Dots Indicator + Teks Petunjuk Geser Elegan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label Halaman Aktif
            Text(
                text = if (pagerState.currentPage == 0) {
                    "Layanan Kesehatan (1/2)"
                } else {
                    "Pesan Shift & Operan Jaga (2/2)"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Indikator Titik (Dots) yang Interaktif
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                repeat(2) { index ->
                    val isSelected = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(targetValue = if (isSelected) 18.dp else 6.dp, label = "dotWidth")
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(if (isSelected) WaliPrimary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }

            // Petunjuk Aksi Geser
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        val target = if (pagerState.currentPage == 0) 1 else 0
                        pagerState.animateScrollToPage(target)
                    }
                }
            ) {
                Text(
                    text = if (pagerState.currentPage == 0) "Geser ❯" else "❮ Kembali",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WaliPrimary
                )
            }
        }

        // Baris 3: Puskesmas Rujukan (Banner Horisontal Cantik)
        PuskesmasWideCard(
            onClick = onShowPuskesmasInfo,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CartoonMenuCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    badgeText: String,
    badgeBg: Color,
    badgeColor: Color,
    bgGradient: List<Color>,
    borderColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(bgGradient))
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 9.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Gambar Ikon Kartun 3D Imut
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(4.dp, CircleShape, spotColor = WaliPrimary.copy(alpha = 0.25f))
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = title,
                        modifier = Modifier.size(54.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Judul Menu
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Subtitle
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Mini Badge Lucu
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }
        }
    }
}

@Composable
fun PuskesmasWideCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("tile_puskesmas_info"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFE0F2FE), Color(0xFFF0FDF4), Color(0xFFFFFFFF))
                    )
                )
                .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon Kartun Puskesmas Imut
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(3.dp, CircleShape, spotColor = Color(0xFF0284C7).copy(alpha = 0.2f))
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_menu_puskesmas),
                    contentDescription = "Puskesmas Rujukan",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Faskes & Puskesmas Rujukan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "UGD 24 Jam",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                }
                Text(
                    text = "Puskesmas Wates • Puskesmas Plosoklaten Kab. Kediri",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
