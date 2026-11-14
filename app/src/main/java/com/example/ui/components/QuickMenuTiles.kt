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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.WaliPrimary
import com.example.util.i18n.LocalAppStrings
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
    onOpenShiftHistory: () -> Unit = {},
    onOpenInputHaid: () -> Unit = {},
    onOpenDaftarHaid: () -> Unit = {},
    onOpenDaftarSuci: () -> Unit = {},
    onOpenFiqihHaid: () -> Unit = {},
    onOpenInputPuasa: () -> Unit = {},
    onOpenDaftarPuasa: () -> Unit = {},
    onOpenChecklistPuasa: () -> Unit = {},
    onOpenRankingPuasa: () -> Unit = {},
    studentsCount: Int = 0,
    activeShiftMessagesCount: Int = 0,
    urgentShiftMessagesCount: Int = 0,
    pastShiftMessagesCount: Int = 0,
    uncompletedPastShiftCount: Int = 0,
    activeHaidCount: Int = 0,
    suciCount: Int = 0,
    istihadhahCount: Int = 0,
    fastingSessionsCount: Int = 0,
    todayFastingCount: Int = 0,
    schoolShortName: String = "",
    faskesBannerSubtitle: String = "",
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    val pagerState = rememberPagerState(pageCount = { 4 })
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
                when (page) {
                    0 -> {
                        // ==========================================
                        // HALAMAN 1: LAYANAN UTAMA KESEHATAN ANAK
                        // ==========================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CartoonMenuCard(
                                title = if (isEn) "Treatment Plans" else "Rencana Berobat",
                                subtitle = if (isEn) "Puskesmas Schedule" else "Jadwal Puskesmas",
                                imageRes = R.drawable.img_menu_rencana,
                                badgeText = if (isEn) "Referral" else "Rujukan",
                                badgeBg = Color(0xFFE0F2FE),
                                badgeColor = WaliPrimary,
                                bgGradient = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE).copy(alpha = 0.5f)),
                                borderColor = Color(0xFFBAE6FD),
                                onClick = onNavigateToPlan,
                                testTag = "tile_rencana_berobat",
                                modifier = Modifier.weight(1f)
                            )

                            CartoonMenuCard(
                                title = if (isEn) "Medical Action" else "Tindakan Medis",
                                subtitle = if (isEn) "Log Treatment" else "Input Pengobatan",
                                imageRes = R.drawable.img_menu_tindakan,
                                badgeText = if (isEn) "Direct" else "Langsung",
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
                                title = if (isEn) "Medical Records" else "Rekam Medis",
                                subtitle = if (isEn) "History & Diagnosis" else "Riwayat & Diagnosa",
                                imageRes = R.drawable.img_menu_rekam_medis,
                                badgeText = if (isEn) "Prescriptions" else "Resep Obat",
                                badgeBg = Color(0xFFDCFCE7),
                                badgeColor = Color(0xFF16A34A),
                                bgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7).copy(alpha = 0.5f)),
                                borderColor = Color(0xFFBBF7D0),
                                onClick = onNavigateToRecords,
                                testTag = "tile_rekam_medis",
                                modifier = Modifier.weight(1f)
                            )

                            val studentSubtitle = if (studentsCount > 0) {
                                if (isEn) {
                                    if (schoolShortName.isNotBlank()) "$studentsCount Students • $schoolShortName" else "$studentsCount Registered Students"
                                } else {
                                    if (schoolShortName.isNotBlank()) "$studentsCount Siswa $schoolShortName" else "$studentsCount Siswa Terdaftar"
                                }
                            } else {
                                if (isEn) "Asrama Students Data" else "Data Siswa Asrama"
                            }

                            CartoonMenuCard(
                                title = if (isEn) "Students" else "Anak Asuh",
                                subtitle = studentSubtitle,
                                imageRes = R.drawable.img_menu_anak_asuh,
                                badgeText = if (isEn) "Asrama Data" else "Data Asrama",
                                badgeBg = Color(0xFFF3E8FF),
                                badgeColor = Color(0xFF9333EA),
                                bgGradient = listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF).copy(alpha = 0.5f)),
                                borderColor = Color(0xFFE9D5FF),
                                onClick = onNavigateToStudents,
                                testTag = "tile_anak_asuh",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    1 -> {
                        // ==========================================
                        // HALAMAN 2: LAYANAN OPERASIONAL & PESAN SHIFT
                        // ==========================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val shiftBadgeText = when {
                                urgentShiftMessagesCount > 0 -> "$urgentShiftMessagesCount URGENT"
                                activeShiftMessagesCount > 0 -> if (isEn) "$activeShiftMessagesCount Notes" else "$activeShiftMessagesCount Pesan"
                                else -> if (isEn) "Shift Duty" else "Operan Jaga"
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
                                title = if (isEn) "Shift Messages" else "Pesan Shift",
                                subtitle = if (isEn) "Asrama Duty Handover" else "Operan Jaga Asrama",
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
                                title = if (isEn) "Write Note" else "Tulis Pesan",
                                subtitle = "Voice to Text (Mic)",
                                imageRes = R.drawable.img_doctor_panda,
                                badgeText = if (isEn) "Voice Input" else "Input Suara",
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
                            val historyBadgeText = when {
                                uncompletedPastShiftCount > 0 -> if (isEn) "$uncompletedPastShiftCount Missed" else "$uncompletedPastShiftCount Terlewat"
                                pastShiftMessagesCount > 0 -> if (isEn) "$pastShiftMessagesCount Archive" else "$pastShiftMessagesCount Arsip"
                                else -> if (isEn) "Duty History" else "Riwayat Jaga"
                            }
                            val historyBadgeBg = when {
                                uncompletedPastShiftCount > 0 -> Color(0xFFFEF3C7)
                                pastShiftMessagesCount > 0 -> Color(0xFFDCFCE7)
                                else -> Color(0xFFF1F5F9)
                            }
                            val historyBadgeColor = when {
                                uncompletedPastShiftCount > 0 -> Color(0xFFD97706)
                                pastShiftMessagesCount > 0 -> Color(0xFF16A34A)
                                else -> Color(0xFF475569)
                            }

                            CartoonMenuCard(
                                title = if (isEn) "Handover History" else "Riwayat Operan",
                                subtitle = if (isEn) "Past Days Archive" else "Arsip Lewat Hari Ini",
                                imageRes = R.drawable.img_menu_rekam_medis,
                                badgeText = historyBadgeText,
                                badgeBg = historyBadgeBg,
                                badgeColor = historyBadgeColor,
                                bgGradient = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)),
                                borderColor = Color(0xFFCBD5E1),
                                onClick = onOpenShiftHistory,
                                testTag = "tile_riwayat_operan",
                                modifier = Modifier.weight(1f)
                            )

                            CartoonMenuCard(
                                title = if (isEn) "Asrama Data" else "Data Asrama",
                                subtitle = if (schoolShortName.isNotBlank()) schoolShortName else "Sekolah Rakyat",
                                imageRes = R.drawable.img_menu_anak_asuh,
                                badgeText = if (isEn) "Asrama Roster" else "Daftar Asrama",
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
                    2 -> {
                        // ==========================================
                        // HALAMAN 3: PENDATAAN & FIQIH HAID (4 IKON BESAR)
                        // ==========================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Ikon 1: Input Haid (Scan Barcode Kartu Siswa)
                            CartoonMenuCard(
                                title = "Input Haid",
                                subtitle = if (isEn) "Scan Card / NIK" else "Scan Kartu / NIK",
                                imageRes = R.drawable.img_menu_input_haid,
                                badgeText = "Scan Barcode",
                                badgeBg = Color(0xFFFCE7F3),
                                badgeColor = Color(0xFFDB2777),
                                bgGradient = listOf(Color(0xFFFFF1F2), Color(0xFFFCE7F3).copy(alpha = 0.6f)),
                                borderColor = Color(0xFFFBCFE8),
                                onClick = onOpenInputHaid,
                                testTag = "tile_input_haid",
                                modifier = Modifier.weight(1f)
                            )

                            // Ikon 2: Daftar Haid (Sedang Aktif)
                            val haidBadgeText = if (istihadhahCount > 0) {
                                if (isEn) "$istihadhahCount Day 15" else "$istihadhahCount Hari ke-15"
                            } else {
                                if (isEn) "$activeHaidCount Active" else "$activeHaidCount Siswi"
                            }
                            val haidBadgeBg = if (istihadhahCount > 0) Color(0xFFFEE2E2) else Color(0xFFFCE7F3)
                            val haidBadgeColor = if (istihadhahCount > 0) Color(0xFFDC2626) else Color(0xFFBE185D)

                            CartoonMenuCard(
                                title = "Daftar Haid",
                                subtitle = if (activeHaidCount > 0) {
                                    if (isEn) "$activeHaidCount Active Haid" else "$activeHaidCount Sedang Haid"
                                } else {
                                    if (isEn) "No Active Haid" else "Tidak Ada Haid"
                                },
                                imageRes = R.drawable.img_menu_daftar_haid,
                                badgeText = haidBadgeText,
                                badgeBg = haidBadgeBg,
                                badgeColor = haidBadgeColor,
                                bgGradient = listOf(Color(0xFFFDF2F8), Color(0xFFFCE7F3).copy(alpha = 0.6f)),
                                borderColor = Color(0xFFF472B6),
                                onClick = onOpenDaftarHaid,
                                testTag = "tile_daftar_haid",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Ikon 3: Daftar Suci (Riwayat Bersuci)
                            CartoonMenuCard(
                                title = "Daftar Suci",
                                subtitle = if (isEn) "History & Ready for Prayer" else "Riwayat & Siap Ibadah",
                                imageRes = R.drawable.img_menu_daftar_suci,
                                badgeText = if (suciCount > 0) "$suciCount Suci" else "Suci",
                                badgeBg = Color(0xFFDCFCE7),
                                badgeColor = Color(0xFF16A34A),
                                bgGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7).copy(alpha = 0.6f)),
                                borderColor = Color(0xFFBBF7D0),
                                onClick = onOpenDaftarSuci,
                                testTag = "tile_daftar_suci",
                                modifier = Modifier.weight(1f)
                            )

                            // Ikon 4: Panduan Aplikasi & Fiqih Haid
                            CartoonMenuCard(
                                title = if (isEn) "Guide & Fiqih Haid" else "Panduan & Fiqih",
                                subtitle = if (isEn) "Fiqih Haid Rules" else "Kaidah Syariat Islam",
                                imageRes = R.drawable.img_menu_fiqih_haid,
                                badgeText = "Fiqih Haid",
                                badgeBg = Color(0xFFCCFBF1),
                                badgeColor = Color(0xFF0F766E),
                                bgGradient = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1).copy(alpha = 0.6f)),
                                borderColor = Color(0xFF99F6E4),
                                onClick = onOpenFiqihHaid,
                                testTag = "tile_fiqih_haid",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    else -> {
                        // ==========================================
                        // HALAMAN 4: PENDATAAN PUASA (4 IKON BESAR)
                        // ==========================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Ikon 1: Input Puasa (Sesi, Scan Barcode & Cari Siswa)
                            CartoonMenuCard(
                                title = "Input Puasa",
                                subtitle = if (isEn) "Scan Card / Find Student" else "Scan Kartu / Cari Siswa",
                                imageRes = R.drawable.img_menu_input_puasa,
                                badgeText = if (isEn) "Scan & Search" else "Scan & Cari",
                                badgeBg = Color(0xFFCCFBF1),
                                badgeColor = Color(0xFF0F766E),
                                bgGradient = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1).copy(alpha = 0.6f)),
                                borderColor = Color(0xFF99F6E4),
                                onClick = onOpenInputPuasa,
                                testTag = "tile_input_puasa",
                                modifier = Modifier.weight(1f)
                            )

                            // Ikon 2: Daftar Puasa (SD, SMP, SMA, Total & PDF)
                            CartoonMenuCard(
                                title = "Daftar Puasa",
                                subtitle = "SD • SMP • SMA & PDF",
                                imageRes = R.drawable.img_menu_daftar_puasa,
                                badgeText = if (todayFastingCount > 0) "$todayFastingCount Puasa" else if (isEn) "Recap & PDF" else "Rekap & PDF",
                                badgeBg = Color(0xFFE0F2FE),
                                badgeColor = Color(0xFF0369A1),
                                bgGradient = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE).copy(alpha = 0.6f)),
                                borderColor = Color(0xFFBAE6FD),
                                onClick = onOpenDaftarPuasa,
                                testTag = "tile_daftar_puasa",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Ikon 3: Checklist Puasa (Sahur & Berbuka + PDF)
                            CartoonMenuCard(
                                title = if (isEn) "Puasa Checklist" else "Checklist Puasa",
                                subtitle = if (isEn) "Sahur & Berbuka + PDF" else "Sahur & Berbuka + PDF",
                                imageRes = R.drawable.img_menu_checklist_puasa,
                                badgeText = if (isEn) "Sahur & Berbuka" else "Sahur & Buka",
                                badgeBg = Color(0xFFFEF3C7),
                                badgeColor = Color(0xFFB45309),
                                bgGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7).copy(alpha = 0.6f)),
                                borderColor = Color(0xFFFDE68A),
                                onClick = onOpenChecklistPuasa,
                                testTag = "tile_checklist_puasa",
                                modifier = Modifier.weight(1f)
                            )

                            // Ikon 4: Ranking & History Sesi Puasa
                            CartoonMenuCard(
                                title = if (isEn) "Puasa Ranking" else "Ranking & History",
                                subtitle = if (isEn) "Top Students & Sessions" else "Siswa Rajin & Sesi",
                                imageRes = R.drawable.img_menu_ranking_puasa,
                                badgeText = if (fastingSessionsCount > 0) {
                                    if (isEn) "$fastingSessionsCount Sessions" else "$fastingSessionsCount Sesi"
                                } else {
                                    if (isEn) "Ranking" else "Peringkat"
                                },
                                badgeBg = Color(0xFFF3E8FF),
                                badgeColor = Color(0xFF7E22CE),
                                bgGradient = listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF).copy(alpha = 0.6f)),
                                borderColor = Color(0xFFE9D5FF),
                                onClick = onOpenRankingPuasa,
                                testTag = "tile_ranking_puasa",
                                modifier = Modifier.weight(1f)
                            )
                        }
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
                text = when (pagerState.currentPage) {
                    0 -> if (isEn) "Health Services (1/4)" else "Layanan Kesehatan (1/4)"
                    1 -> if (isEn) "Shift Messages & Duty (2/4)" else "Pesan Shift & Jaga (2/4)"
                    2 -> if (isEn) "Fiqih Haid Tracking (3/4)" else "Pendataan & Fiqih Haid (3/4)"
                    else -> if (isEn) "Puasa Attendance (4/4)" else "Pendataan Puasa (4/4)"
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
                repeat(4) { index ->
                    val isSelected = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(targetValue = if (isSelected) 18.dp else 6.dp, label = "dotWidth")
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    when (index) {
                                        2 -> Color(0xFFDB2777)
                                        3 -> Color(0xFF0F766E)
                                        else -> WaliPrimary
                                    }
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
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
                        val next = (pagerState.currentPage + 1) % 4
                        pagerState.animateScrollToPage(next)
                    }
                }
            ) {
                Text(
                    text = if (pagerState.currentPage < 3) {
                        if (isEn) "Swipe ❯" else "Geser ❯"
                    } else {
                        if (isEn) "Back ↺" else "Kembali ↺"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (pagerState.currentPage) {
                        2 -> Color(0xFFDB2777)
                        3 -> Color(0xFF0F766E)
                        else -> WaliPrimary
                    }
                )
            }
        }

        // Baris 3: Puskesmas Rujukan (Banner Horisontal Cantik)
        PuskesmasWideCard(
            faskesSubtitle = faskesBannerSubtitle,
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
    faskesSubtitle: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
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
                    contentDescription = if (isEn) "Referral Puskesmas" else "Puskesmas Rujukan",
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
                        text = if (isEn) "Referral Faskes & Puskesmas" else "Faskes & Puskesmas Rujukan",
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
                            text = if (isEn) "24h ER" else "UGD 24 Jam",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                }
                Text(
                    text = faskesSubtitle.ifBlank {
                        if (isEn) "View health facilities directory & emergency SOP" else "Lihat daftar fasilitas kesehatan & SOP darurat"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
