package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.DashboardBannerActionType
import com.example.data.model.DashboardBannerConfig
import com.example.data.model.DashboardBannerFeatureTarget
import com.example.data.model.DashboardBannerItem
import com.example.data.remote.pocketbase.PocketBaseConfig
import com.example.ui.theme.WaliPrimary
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.delay

@Composable
fun DashboardBannerCarousel(
    config: DashboardBannerConfig,
    faskesDefaultSubtitle: String,
    isAnakSuperAdmin: Boolean,
    onBannerClick: (DashboardBannerItem) -> Unit,
    onManageBannersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    val activeItems = remember(config.items) {
        config.normalized().items.filter { it.isEnabled }
    }
    val intervalSeconds = DashboardBannerConfig.clampIntervalSeconds(config.slideIntervalSeconds)

    if (activeItems.isEmpty() && !isAnakSuperAdmin) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_banner_carousel"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (isAnakSuperAdmin) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEn) {
                        "Info & Service Banner (${intervalSeconds}s auto-slide)"
                    } else {
                        "Banner Info & Layanan (geser ${intervalSeconds} dtk)"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(WaliPrimary.copy(alpha = 0.12f))
                        .clickable(onClick = onManageBannersClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("btn_manage_dashboard_banners"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = if (isEn) "Manage banners" else "Atur banner",
                        tint = WaliPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (isEn) "Manage Banner" else "Atur Banner",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WaliPrimary
                    )
                }
            }
        }

        if (activeItems.isEmpty()) {
            Surface(
                onClick = onManageBannersClick,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .testTag("banner_empty_admin_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = WaliPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEn) {
                            "No active banners. Tap 'Manage Banner' to add or restore Faskes."
                        } else {
                            "Semua banner disembunyikan/dihapus. Ketuk untuk tambah atau pulihkan Faskes."
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return
        }

        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { activeItems.size }
        )

        LaunchedEffect(activeItems, intervalSeconds, pagerState.isScrollInProgress) {
            if (activeItems.size <= 1) return@LaunchedEffect
            while (true) {
                delay(intervalSeconds * 1000L)
                if (!pagerState.isScrollInProgress) {
                    val nextPage = (pagerState.currentPage + 1) % activeItems.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            pageSpacing = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val item = activeItems[page]
            BannerSlideCard(
                item = item,
                faskesDefaultSubtitle = faskesDefaultSubtitle,
                onClick = { onBannerClick(item) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (activeItems.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                activeItems.forEachIndexed { index, _ ->
                    val isSelected = pagerState.currentPage == index
                    val dotColor by animateColorAsState(
                        targetValue = if (isSelected) WaliPrimary else MaterialTheme.colorScheme.outlineVariant,
                        label = "banner_dot_color"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(if (isSelected) 16.dp else 6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerSlideCard(
    item: DashboardBannerItem,
    faskesDefaultSubtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    val pbBaseUrl = remember(context) { PocketBaseConfig.getBaseUrl(context) }
    val primaryUrl = item.resolvePrimaryImageUrl()
    val fallbackUrl = item.resolveFallbackImageUrl(pbBaseUrl)
    val slideTag = if (item.isDefaultFaskes) "tile_puskesmas_info" else "banner_slide_${item.id}"
    val isClickable = item.actionType != DashboardBannerActionType.NONE

    if (primaryUrl.isBlank() && item.isDefaultFaskes) {
        val resolvedTitle = item.title.ifBlank {
            if (isEn) "Referral Faskes & Puskesmas" else "Faskes & Puskesmas Rujukan"
        }
        val resolvedBadge = item.badgeText.ifBlank {
            if (isEn) "24h ER" else "UGD 24 Jam"
        }
        val resolvedSubtitle = item.subtitle.ifBlank {
            faskesDefaultSubtitle.ifBlank {
                if (isEn) "View health facilities directory & emergency SOP" else "Lihat daftar fasilitas kesehatan & SOP darurat"
            }
        }
        ElevatedCard(
            modifier = modifier
                .then(if (isClickable) Modifier.clickable(onClick = onClick) else Modifier)
                .testTag(slideTag),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 84.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFE0F2FE), Color(0xFFF0FDF4), Color(0xFFFFFFFF))
                        )
                    )
                    .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
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
                        contentDescription = resolvedTitle,
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
                            text = resolvedTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (resolvedBadge.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = resolvedBadge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Text(
                        text = resolvedSubtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        return
    }

    // Custom Image Banner (Cloudflare R2 CDN with PocketBase API fallback)
    var activeModelUrl by remember(primaryUrl, fallbackUrl) {
        mutableStateOf(primaryUrl.ifBlank { fallbackUrl })
    }
    var imageFailedAll by remember(primaryUrl, fallbackUrl) {
        mutableStateOf(primaryUrl.isBlank() && fallbackUrl.isBlank())
    }

    val effectiveTitle = item.title.trim()
    val effectiveSubtitle = item.subtitle.trim()
    val effectiveBadge = item.badgeText.trim()
    val hasTextOverlay = effectiveTitle.isNotBlank() || effectiveSubtitle.isNotBlank()

    ElevatedCard(
        modifier = modifier
            .height(106.dp)
            .then(if (isClickable) Modifier.clickable(onClick = onClick) else Modifier)
            .testTag(slideTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!imageFailedAll && activeModelUrl.isNotBlank()) {
                val request = remember(activeModelUrl, context) {
                    ImageRequest.Builder(context)
                        .data(activeModelUrl)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build()
                }
                AsyncImage(
                    model = request,
                    contentDescription = effectiveTitle.ifBlank {
                        if (isEn) "Dashboard banner" else "Banner informasi dashboard"
                    },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = {
                        if (activeModelUrl != fallbackUrl && fallbackUrl.isNotBlank()) {
                            activeModelUrl = fallbackUrl
                        } else {
                            imageFailedAll = true
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F766E), Color(0xFF0284C7), Color(0xFF1D4ED8))
                            )
                        )
                )
            }

            if (hasTextOverlay || imageFailedAll) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.08f),
                                    Color.Black.copy(alpha = 0.62f)
                                )
                            )
                        )
                )
            }

            if (effectiveBadge.isNotBlank() || item.actionType == DashboardBannerActionType.EXTERNAL_LINK) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (effectiveBadge.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.62f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = effectiveBadge,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                    if (item.actionType == DashboardBannerActionType.EXTERNAL_LINK) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.58f))
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = if (isEn) "External link" else "Link eksternal",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            if (hasTextOverlay || imageFailedAll) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val displayTitle = effectiveTitle.ifBlank {
                        if (imageFailedAll) {
                            DashboardBannerFeatureTarget.fromKey(item.actionTarget)?.label(isEn)
                                ?: if (isEn) "Featured Information" else "Informasi Layanan"
                        } else {
                            ""
                        }
                    }
                    if (displayTitle.isNotBlank()) {
                        Text(
                            text = displayTitle,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (effectiveSubtitle.isNotBlank()) {
                        Text(
                            text = effectiveSubtitle,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.92f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
