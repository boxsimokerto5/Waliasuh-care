package com.example.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun QuickMenuTiles(
    onOpenCategoryHub: (DashboardCategoryHub) -> Unit = {},
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
    onOpenInputMengaji: () -> Unit = {},
    todayPlansCount: Int = 0,
    inpatientCount: Int = 0,
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
    mengajiTodayCount: Int = 0,
    mengajiTotalCount: Int = 0,
    schoolShortName: String = "",
    faskesBannerSubtitle: String = "",
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Super-App 2x4 Hub Grid ala Gojek (Semua kategori langsung terlihat dalam 1 layar)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Baris 1: Kategori Berobat + Akses Cepat Kesehatan & Siswa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val berobatBadge = when {
                        inpatientCount > 0 -> "$inpatientCount Inap"
                        todayPlansCount > 0 -> "$todayPlansCount"
                        else -> if (isEn) "Hub" else "Menu"
                    }
                    val berobatBadgeBg = if (inpatientCount > 0) Color(0xFFDC2626) else Color(0xFF0284C7)

                    SuperAppGridItem(
                        title = if (isEn) "Treatment" else "Berobat",
                        imageRes = R.drawable.img_menu_rencana,
                        tileBg = Color(0xFFE0F2FE),
                        badgeText = berobatBadge,
                        badgeBg = berobatBadgeBg,
                        onClick = { onOpenCategoryHub(DashboardCategoryHub.BEROBAT) },
                        testTag = "category_berobat",
                        modifier = Modifier.weight(1f)
                    )

                    SuperAppGridItem(
                        title = if (isEn) "Action" else "Tindakan",
                        imageRes = R.drawable.img_menu_tindakan,
                        tileBg = Color(0xFFFEF3C7),
                        badgeText = if (isEn) "Direct" else "Cepat",
                        badgeBg = Color(0xFFD97706),
                        onClick = onNavigateToDirectTreatment,
                        testTag = "tile_tindakan_langsung",
                        modifier = Modifier.weight(1f)
                    )

                    SuperAppGridItem(
                        title = if (isEn) "Records" else "Rekam Medis",
                        imageRes = R.drawable.img_menu_rekam_medis,
                        tileBg = Color(0xFFDCFCE7),
                        badgeText = null,
                        badgeBg = Color(0xFF16A34A),
                        onClick = onNavigateToRecords,
                        testTag = "tile_rekam_medis",
                        modifier = Modifier.weight(1f)
                    )

                    SuperAppGridItem(
                        title = if (isEn) "Students" else "Anak Asuh",
                        imageRes = R.drawable.img_menu_anak_asuh,
                        tileBg = Color(0xFFF3E8FF),
                        badgeText = if (studentsCount > 0) "$studentsCount" else null,
                        badgeBg = Color(0xFF7E22CE),
                        onClick = onNavigateToStudents,
                        testTag = "tile_anak_asuh",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Baris 2: Hub Kategori (Puasa, Haid, Pesan Shift) + Tulis Pesan Cepat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val puasaBadge = when {
                        todayFastingCount > 0 -> "$todayFastingCount"
                        fastingSessionsCount > 0 -> "$fastingSessionsCount Sesi"
                        else -> if (isEn) "Hub" else "Menu"
                    }
                    SuperAppGridItem(
                        title = "Puasa",
                        imageRes = R.drawable.img_menu_input_puasa,
                        tileBg = Color(0xFFCCFBF1),
                        badgeText = puasaBadge,
                        badgeBg = Color(0xFF0F766E),
                        onClick = { onOpenCategoryHub(DashboardCategoryHub.PUASA) },
                        testTag = "category_puasa",
                        modifier = Modifier.weight(1f)
                    )

                    val haidBadge = when {
                        istihadhahCount > 0 -> "H15 ($istihadhahCount)"
                        activeHaidCount > 0 -> "$activeHaidCount"
                        else -> "Fiqih"
                    }
                    val haidBadgeBg = if (istihadhahCount > 0) Color(0xFFDC2626) else Color(0xFFDB2777)
                    SuperAppGridItem(
                        title = "Haid",
                        imageRes = R.drawable.img_menu_daftar_haid,
                        tileBg = Color(0xFFFCE7F3),
                        badgeText = haidBadge,
                        badgeBg = haidBadgeBg,
                        onClick = { onOpenCategoryHub(DashboardCategoryHub.HAID) },
                        testTag = "category_haid",
                        modifier = Modifier.weight(1f)
                    )

                    val pesanBadge = when {
                        urgentShiftMessagesCount > 0 -> "${urgentShiftMessagesCount}!"
                        activeShiftMessagesCount > 0 -> "$activeShiftMessagesCount"
                        uncompletedPastShiftCount > 0 -> "$uncompletedPastShiftCount"
                        else -> null
                    }
                    val pesanBadgeBg = when {
                        urgentShiftMessagesCount > 0 -> Color(0xFFDC2626)
                        activeShiftMessagesCount > 0 -> Color(0xFFB45309)
                        else -> Color(0xFF475569)
                    }
                    SuperAppGridItem(
                        title = if (isEn) "Shift Notes" else "Pesan Shift",
                        imageRes = R.drawable.img_menu_pesan_shift,
                        tileBg = Color(0xFFFEF3C7),
                        badgeText = pesanBadge,
                        badgeBg = pesanBadgeBg,
                        onClick = { onOpenCategoryHub(DashboardCategoryHub.PESAN) },
                        testTag = "category_pesan",
                        modifier = Modifier.weight(1f)
                    )

                    SuperAppGridItem(
                        title = if (isEn) "Write Note" else "Tulis Pesan",
                        imageRes = R.drawable.img_doctor_panda,
                        tileBg = Color(0xFFE0F2FE),
                        badgeText = if (isEn) "Quick" else "Cepat",
                        badgeBg = WaliPrimary,
                        onClick = onOpenCreateShiftMessage,
                        testTag = "tile_tulis_pesan_shift",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Baris 3: Mengaji Berkelanjutan & Cetak Kartu Mengaji (Gojek Super-App Hub Card)
        MengajiWideCard(
            mengajiTodayCount = mengajiTodayCount,
            mengajiTotalCount = mengajiTotalCount,
            onOpenHub = { onOpenCategoryHub(DashboardCategoryHub.MENGAJI) },
            onQuickInput = onOpenInputMengaji,
            modifier = Modifier.fillMaxWidth()
        )

        // Baris 4: Puskesmas Rujukan (Banner Horisontal Cantik)
        PuskesmasWideCard(
            faskesSubtitle = faskesBannerSubtitle,
            onClick = onShowPuskesmasInfo,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SuperAppGridItem(
    title: String,
    imageRes: Int,
    tileBg: Color,
    badgeText: String? = null,
    badgeBg: Color = WaliPrimary,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .heightIn(min = 48.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = tileBg)
                    .clip(RoundedCornerShape(16.dp))
                    .background(tileBg)
                    .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (!badgeText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-4).dp, y = (-5).dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(badgeBg)
                        .border(1.dp, Color.White, RoundedCornerShape(7.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 13.sp,
            overflow = TextOverflow.Ellipsis
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
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(4.dp, CircleShape, spotColor = WaliPrimary.copy(alpha = 0.25f))
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = title,
                        modifier = Modifier.size(52.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

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
                        color = badgeColor,
                        maxLines = 1
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(3.dp, CircleShape, spotColor = Color(0xFF0284C7).copy(alpha = 0.2f))
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_menu_puskesmas),
                    contentDescription = if (isEn) "Referral Puskesmas" else "Puskesmas Rujukan",
                    modifier = Modifier.size(46.dp),
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

@Composable
fun MengajiWideCard(
    mengajiTodayCount: Int = 0,
    mengajiTotalCount: Int = 0,
    onOpenHub: () -> Unit,
    onQuickInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    val emerald = Color(0xFF047857)
    ElevatedCard(
        modifier = modifier
            .clickable(onClick = onOpenHub)
            .testTag("category_mengaji"),
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
                        listOf(Color(0xFFD1FAE5), Color(0xFFECFDF5), Color(0xFFFFFFFF))
                    )
                )
                .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(3.dp, CircleShape, spotColor = emerald.copy(alpha = 0.2f))
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_menu_mengaji),
                    contentDescription = if (isEn) "Mengaji Progress" else "Mengaji Berkelanjutan",
                    modifier = Modifier.size(46.dp),
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
                        text = if (isEn) "Mengaji & Progress Card" else "Mengaji & Kartu Kendali",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF047857))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = when {
                                mengajiTodayCount > 0 -> if (isEn) "$mengajiTodayCount Today" else "$mengajiTodayCount Hari Ini"
                                mengajiTotalCount > 0 -> "$mengajiTotalCount Log"
                                else -> if (isEn) "New" else "Baru"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Text(
                    text = if (isEn) {
                        "Continuous reading log, volume recap & monthly card print"
                    } else {
                        "Input lanjutan jilid/halaman, riwayat anak & cetak kartu"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(emerald)
                    .clickable(onClick = onQuickInput)
                    .padding(horizontal = 10.dp, vertical = 7.dp)
                    .testTag("tile_quick_input_mengaji")
            ) {
                Text(
                    text = if (isEn) "+ Input" else "+ Setor",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}
