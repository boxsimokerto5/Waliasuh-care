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
import com.example.data.model.DashboardIconKey
import com.example.ui.theme.WaliPrimary
import com.example.util.i18n.LocalAppStrings

data class DashboardIconUiMeta(
    val key: DashboardIconKey,
    val title: String,
    val imageRes: Int,
    val tileBg: Color
)

fun dashboardIconUiMeta(key: DashboardIconKey, isEn: Boolean): DashboardIconUiMeta {
    return when (key) {
        DashboardIconKey.BEROBAT -> DashboardIconUiMeta(
            key = key,
            title = if (isEn) "Treatment" else "Berobat",
            imageRes = R.drawable.img_menu_rencana,
            tileBg = Color(0xFFE0F2FE)
        )
        DashboardIconKey.KUNJUNGAN -> DashboardIconUiMeta(
            key = key,
            title = if (isEn) "Visits" else "Kunjungan",
            imageRes = R.drawable.img_menu_kunjungan,
            tileBg = Color(0xFFDBEAFE)
        )
        DashboardIconKey.ORANG_TUA -> DashboardIconUiMeta(
            key = key,
            title = if (isEn) "Parents" else "Hubungan Ortu",
            imageRes = R.drawable.img_menu_orang_tua,
            tileBg = Color(0xFFE0E7FF)
        )
        DashboardIconKey.ANAK_ASUH -> DashboardIconUiMeta(
            key = key,
            title = if (isEn) "Students" else "Anak Asuh",
            imageRes = R.drawable.img_menu_anak_asuh,
            tileBg = Color(0xFFF3E8FF)
        )
        DashboardIconKey.PUASA -> DashboardIconUiMeta(
            key = key,
            title = if (isEn) "Fasting" else "Puasa",
            imageRes = R.drawable.img_menu_input_puasa,
            tileBg = Color(0xFFCCFBF1)
        )
        DashboardIconKey.HAID -> DashboardIconUiMeta(
            key = key,
            title = if (isEn) "Period" else "Haid",
            imageRes = R.drawable.img_menu_daftar_haid,
            tileBg = Color(0xFFFCE7F3)
        )
        DashboardIconKey.PESAN -> DashboardIconUiMeta(
            key = key,
            title = if (isEn) "Shift Notes" else "Pesan Shift",
            imageRes = R.drawable.img_menu_pesan_shift,
            tileBg = Color(0xFFFEF3C7)
        )
        DashboardIconKey.MENGAJI -> DashboardIconUiMeta(
            key = key,
            title = if (isEn) "Recitation" else "Mengaji",
            imageRes = R.drawable.img_menu_mengaji,
            tileBg = Color(0xFFD1FAE5)
        )
    }
}

private data class GridTileSpec(
    val meta: DashboardIconUiMeta,
    val badgeText: String?,
    val badgeBg: Color,
    val testTag: String,
    val onClick: () -> Unit
)

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
    visitSessionsCount: Int = 0,
    todayVisitsCount: Int = 0,
    unreadParentMessagesCount: Int = 0,
    myFosterStudentsCount: Int = 0,
    schoolShortName: String = "",
    faskesBannerSubtitle: String = "",
    iconOrder: List<DashboardIconKey> = DashboardIconKey.DEFAULT_ORDER,
    showPuskesmasCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    val normalizedOrder = DashboardIconKey.normalizeOrder(iconOrder.map { it.key })

    val orderedSpecs = normalizedOrder.map { key ->
        val meta = dashboardIconUiMeta(key, isEn)
        when (key) {
            DashboardIconKey.BEROBAT -> {
                val berobatBadge = when {
                    inpatientCount > 0 -> "$inpatientCount Inap"
                    todayPlansCount > 0 -> "$todayPlansCount"
                    else -> if (isEn) "Hub" else "Menu"
                }
                val berobatBadgeBg = if (inpatientCount > 0) Color(0xFFDC2626) else Color(0xFF0284C7)
                GridTileSpec(
                    meta = meta,
                    badgeText = berobatBadge,
                    badgeBg = berobatBadgeBg,
                    testTag = "category_berobat",
                    onClick = { onOpenCategoryHub(DashboardCategoryHub.BEROBAT) }
                )
            }
            DashboardIconKey.KUNJUNGAN -> {
                val kunjunganBadge = when {
                    todayVisitsCount > 0 -> "$todayVisitsCount"
                    visitSessionsCount > 0 -> "$visitSessionsCount Sesi"
                    else -> if (isEn) "Hub" else "Menu"
                }
                GridTileSpec(
                    meta = meta,
                    badgeText = kunjunganBadge,
                    badgeBg = Color(0xFF1D4ED8),
                    testTag = "category_kunjungan",
                    onClick = { onOpenCategoryHub(DashboardCategoryHub.KUNJUNGAN) }
                )
            }
            DashboardIconKey.ORANG_TUA -> {
                val ortuBadge = when {
                    unreadParentMessagesCount > 0 -> "$unreadParentMessagesCount Chat"
                    myFosterStudentsCount > 0 -> "$myFosterStudentsCount"
                    else -> if (isEn) "Hub" else "Menu"
                }
                val ortuBadgeBg = if (unreadParentMessagesCount > 0) Color(0xFFDC2626) else Color(0xFF4F46E5)
                GridTileSpec(
                    meta = meta,
                    badgeText = ortuBadge,
                    badgeBg = ortuBadgeBg,
                    testTag = "category_orang_tua",
                    onClick = { onOpenCategoryHub(DashboardCategoryHub.ORANG_TUA) }
                )
            }
            DashboardIconKey.ANAK_ASUH -> {
                GridTileSpec(
                    meta = meta,
                    badgeText = if (studentsCount > 0) "$studentsCount" else null,
                    badgeBg = Color(0xFF7E22CE),
                    testTag = "tile_anak_asuh",
                    onClick = onNavigateToStudents
                )
            }
            DashboardIconKey.PUASA -> {
                val puasaBadge = when {
                    todayFastingCount > 0 -> "$todayFastingCount"
                    fastingSessionsCount > 0 -> "$fastingSessionsCount Sesi"
                    else -> if (isEn) "Hub" else "Menu"
                }
                GridTileSpec(
                    meta = meta,
                    badgeText = puasaBadge,
                    badgeBg = Color(0xFF0F766E),
                    testTag = "category_puasa",
                    onClick = { onOpenCategoryHub(DashboardCategoryHub.PUASA) }
                )
            }
            DashboardIconKey.HAID -> {
                val haidBadge = when {
                    istihadhahCount > 0 -> "H15 ($istihadhahCount)"
                    activeHaidCount > 0 -> "$activeHaidCount"
                    else -> "Fiqih"
                }
                val haidBadgeBg = if (istihadhahCount > 0) Color(0xFFDC2626) else Color(0xFFDB2777)
                GridTileSpec(
                    meta = meta,
                    badgeText = haidBadge,
                    badgeBg = haidBadgeBg,
                    testTag = "category_haid",
                    onClick = { onOpenCategoryHub(DashboardCategoryHub.HAID) }
                )
            }
            DashboardIconKey.PESAN -> {
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
                GridTileSpec(
                    meta = meta,
                    badgeText = pesanBadge,
                    badgeBg = pesanBadgeBg,
                    testTag = "category_pesan",
                    onClick = { onOpenCategoryHub(DashboardCategoryHub.PESAN) }
                )
            }
            DashboardIconKey.MENGAJI -> {
                val mengajiBadge = when {
                    mengajiTodayCount > 0 -> "$mengajiTodayCount"
                    mengajiTotalCount > 0 -> "$mengajiTotalCount Log"
                    else -> if (isEn) "Hub" else "Menu"
                }
                GridTileSpec(
                    meta = meta,
                    badgeText = mengajiBadge,
                    badgeBg = Color(0xFF047857),
                    testTag = "category_mengaji",
                    onClick = { onOpenCategoryHub(DashboardCategoryHub.MENGAJI) }
                )
            }
        }
    }

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
                orderedSpecs.chunked(4).forEach { rowSpecs ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowSpecs.forEach { spec ->
                            SuperAppGridItem(
                                title = spec.meta.title,
                                imageRes = spec.meta.imageRes,
                                tileBg = spec.meta.tileBg,
                                badgeText = spec.badgeText,
                                badgeBg = spec.badgeBg,
                                onClick = spec.onClick,
                                testTag = spec.testTag,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Baris 3: Puskesmas Rujukan (Banner Horisontal Cantik — opsional bila digantikan Carousel)
        if (showPuskesmasCard) {
            PuskesmasWideCard(
                faskesSubtitle = faskesBannerSubtitle,
                onClick = onShowPuskesmasInfo,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
