package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import kotlinx.coroutines.delay
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.FastingRecord
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentExpectation
import com.example.data.model.ParentMessage
import com.example.data.model.ResolvedWaliBadgeInfo
import com.example.data.model.SholatHelper
import com.example.data.model.SholatRecord
import com.example.data.model.SholatStatus
import com.example.data.model.SholatWaktu
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganTransaction
import com.example.data.model.WaliAccount
import com.example.util.DateUtils
import com.example.util.i18n.AppLanguage

data class ParentMenuTileSpec(
    val id: String,
    val title: String,
    @DrawableRes val imageRes: Int,
    val accentColor: Color,
    val softBg: Color,
    val badgeText: String? = null,
    val badgeColor: Color = Color(0xFF0F766E),
    val testTag: String,
    val onClick: () -> Unit
)

private data class ParentHighlightCardData(
    val id: String,
    val badge: String,
    val title: String,
    val mainValue: String,
    val detailLine: String,
    val footerAction: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val accentColor: Color,
    val badgeBg: Color,
    val onClick: () -> Unit
)

@Composable
fun ParentGreetingHeroCard(
    student: ChildStudent?,
    link: ParentAccessLink?,
    unreadMessageCount: Int,
    isEn: Boolean,
    onOpenChat: () -> Unit,
    onOpenProfile: (() -> Unit)? = null,
    onAvatarClick: (() -> Unit)? = null,
    waliAccount: WaliAccount? = null,
    schoolName: String = "",
    onOpenWaliProfile: (() -> Unit)? = null,
    waliBadges: List<ResolvedWaliBadgeInfo> = emptyList(),
    isChristianStudent: Boolean = false
) {
    val studentName = student?.name ?: link?.studentName.orEmpty().ifBlank { "Ananda" }
    val parentName = link?.parentName?.ifBlank { student?.motherName.orEmpty() }.orEmpty()
    val classGrade = student?.classGrade ?: link?.studentClass ?: "-"
    val dorm = student?.dormitory ?: link?.studentDorm ?: "-"
    val room = student?.roomNumber?.takeIf { it.isNotBlank() }
    val onDutyBadge = waliBadges.firstOrNull { it.isOnDutyNow }
    val displayPrimaryBadge = onDutyBadge ?: waliBadges.firstOrNull()
    val waliName = when {
        waliBadges.size > 1 -> waliBadges.joinToString(", ") { it.name }
        else -> displayPrimaryBadge?.name?.takeIf { it.isNotBlank() }
            ?: waliAccount?.name?.takeIf { it.isNotBlank() }
            ?: student?.assignedWaliName?.ifBlank { link?.assignedWaliName.orEmpty() }
            ?: link?.assignedWaliName.orEmpty()
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_parent_hero"),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A),
                            Color(0xFF3730A3),
                            Color(0xFF5B21B6)
                        )
                    )
                )
        ) {
            // Subtle decorative bubbles for warmth and depth
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 34.dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-24).dp, y = 26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF38BDF8).copy(alpha = 0.12f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Warm greeting bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isChristianStudent) {
                            if (isEn) {
                                if (parentName.isNotBlank()) "Warm Greetings, $parentName" else "Warm Greetings, Parent"
                            } else {
                                if (parentName.isNotBlank()) "Salam Sejahtera, $parentName" else "Salam Sejahtera, Ayah / Bunda"
                            }
                        } else {
                            if (isEn) {
                                if (parentName.isNotBlank()) "Assalamu'alaikum, $parentName" else "Assalamu'alaikum, Parent"
                            } else {
                                if (parentName.isNotBlank()) "Assalamu'alaikum, $parentName" else "Assalamu'alaikum, Ayah / Bunda"
                            }
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE0E7FF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFF34D399).copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, Color(0xFF6EE7B7).copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = if (isEn) "Live Report" else "Pantauan Aktif",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Child identity row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .let { mod -> if (onOpenProfile != null) mod.clickable(onClick = onOpenProfile) else mod },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heroPhotoUrl = student?.photoUrl?.takeIf { it.isNotBlank() } ?: link?.studentPhotoUrl.orEmpty()
                    val heroPhotoFallbackUrl = student?.photoFallbackUrl?.takeIf { it.isNotBlank() }
                        ?: link?.studentPhotoFallbackUrl.orEmpty()
                    val hasHeroPhoto = heroPhotoUrl.isNotBlank() || heroPhotoFallbackUrl.isNotBlank()
                    StudentLargeProfilePhoto(
                        name = studentName,
                        gender = student?.gender ?: "L",
                        photoUrl = heroPhotoUrl,
                        photoFallbackUrl = heroPhotoFallbackUrl,
                        showZoomBadge = hasHeroPhoto,
                        contentScale = ContentScale.Fit,
                        shape = RoundedCornerShape(16.dp),
                        borderColor = Color.White.copy(alpha = 0.78f),
                        placeholderAvatarSize = 58.dp,
                        modifier = Modifier
                            .width(96.dp)
                            .height(128.dp)
                            .let { mod ->
                                if (onAvatarClick != null) {
                                    mod.clickable(onClick = onAvatarClick).testTag("btn_hero_student_avatar")
                                } else mod
                            }
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = studentName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = buildString {
                                append(if (isEn) "Class $classGrade" else "Kelas $classGrade")
                                append(" • ")
                                append(dorm)
                                if (room != null) {
                                    append(if (isEn) " • Room $room" else " • Kamar $room")
                                }
                            },
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!student?.nis.isNullOrBlank()) {
                            Text(
                                text = "NIS/NISN: ${student?.nis}",
                                fontSize = 10.5.sp,
                                color = Color(0xFFC7D2FE)
                            )
                        }
                    }
                }

                // Frosted Wali Asuh bar with real caregiver photo, verified badge & quick chat button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val displayedAcc = displayPrimaryBadge?.account ?: waliAccount
                    val waliPhotoUrl = displayedAcc?.photoUrl.orEmpty()
                    val waliPhotoFallback = displayedAcc?.photoFallbackUrl.orEmpty()
                    val hasWaliIdentity = waliName.isNotBlank() || displayedAcc != null
                    val waliAvatarGender = if (
                        (displayedAcc?.dormitory ?: dorm).contains("putri", ignoreCase = true)
                    ) "P" else "L"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .let { mod ->
                                    if (onOpenWaliProfile != null && hasWaliIdentity) {
                                        mod.clickable(onClick = onOpenWaliProfile)
                                            .testTag("btn_hero_open_wali_profile")
                                    } else mod
                                }
                                .padding(end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasWaliIdentity) {
                                    StudentAvatar(
                                        name = waliName.ifBlank { "Wali" },
                                        gender = waliAvatarGender,
                                        size = 38.dp,
                                        photoUrl = waliPhotoUrl,
                                        photoFallbackUrl = waliPhotoFallback
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(9.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = when {
                                            waliBadges.size > 1 -> if (isEn) {
                                                "Caregiver Team (${waliBadges.size})"
                                            } else {
                                                "Tim Wali Asuh (${waliBadges.size})"
                                            }
                                            else -> if (isEn) "Foster Caregiver" else "Wali Asuh Pembina"
                                        },
                                        fontSize = 10.sp,
                                        color = Color(0xFFE0E7FF)
                                    )
                                    if (displayedAcc != null) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = if (isEn) "Verified" else "Terverifikasi",
                                            tint = Color(0xFF6EE7B7),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    if (onDutyBadge != null) {
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.28f),
                                            border = BorderStroke(1.dp, Color(0xFF6EE7B7).copy(alpha = 0.7f))
                                        ) {
                                            Text(
                                                text = if (waliBadges.size > 1) {
                                                    if (isEn) "On Duty: ${onDutyBadge.name}" else "Piket: ${onDutyBadge.name}"
                                                } else {
                                                    if (isEn) "On Duty" else "Sedang Piket"
                                                },
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFD1FAE5),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = waliName.ifBlank { if (isEn) "Not assigned yet" else "Belum ditentukan" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (hasWaliIdentity) {
                                    val subLine = when {
                                        waliBadges.size > 1 -> if (isEn) {
                                            "Lead & tandem active • Tap for team profiles"
                                        } else {
                                            "Utama & tandem aktif • Ketuk profil pembina"
                                        }
                                        else -> displayedAcc?.let { acc ->
                                            val parts = listOfNotNull(
                                                acc.effectiveTitle(isEn),
                                                acc.employeeCode.takeIf { it.isNotBlank() }?.let { "ID: $it" }
                                            )
                                            parts.joinToString(" • ")
                                        }?.takeIf { it.isNotBlank() } ?: if (onOpenWaliProfile != null) {
                                            if (isEn) "Tap for verified profile" else "Ketuk lihat profil resmi"
                                        } else ""
                                    }
                                    if (subLine.isNotBlank()) {
                                        Text(
                                            text = subLine,
                                            fontSize = 10.sp,
                                            color = Color(0xFFDDD6FE),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onOpenChat)
                                .testTag("btn_overview_open_chat")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = Color(0xFF3730A3),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (unreadMessageCount > 0) {
                                        if (isEn) "Chat ($unreadMessageCount)" else "Pesan ($unreadMessageCount)"
                                    } else {
                                        if (isEn) "Chat" else "Kirim Pesan"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF3730A3)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentSuperAppIconGrid(
    tiles: List<ParentMenuTileSpec>,
    isEn: Boolean
) {
    if (tiles.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEn) "Child Services & Reports" else "Layanan & Laporan Ananda",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isEn) "${tiles.size} menus" else "${tiles.size} menu",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                tiles.chunked(4).forEach { rowTiles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowTiles.forEach { tile ->
                            ParentIllustratedMenuTile(
                                tile = tile,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(4 - rowTiles.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentIllustratedMenuTile(
    tile: ParentMenuTileSpec,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = tile.onClick)
            .padding(vertical = 3.dp, horizontal = 2.dp)
            .testTag(tile.testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(contentAlignment = Alignment.TopStart) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(16.dp),
                color = tile.softBg,
                border = BorderStroke(1.2.dp, tile.accentColor.copy(alpha = 0.28f)),
                shadowElevation = 1.dp
            ) {
                Image(
                    painter = painterResource(id = tile.imageRes),
                    contentDescription = tile.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp)
                        .clip(RoundedCornerShape(13.dp))
                )
            }

            if (!tile.badgeText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(7.dp),
                    color = tile.badgeColor,
                    shadowElevation = 2.dp,
                    modifier = Modifier.offset(x = (-4).dp, y = (-5).dp)
                ) {
                    Text(
                        text = tile.badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                    )
                }
            }
        }

        Text(
            text = tile.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 13.5.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ParentSwipeableHighlightsSection(
    link: ParentAccessLink?,
    isFemaleStudent: Boolean,
    mengajiRecords: List<MengajiRecord>,
    medicalRecords: List<MedicalRecord>,
    fastingRecords: List<FastingRecord>,
    haidRecords: List<HaidRecord>,
    tabunganTransactions: List<TabunganTransaction>,
    tabunganBalance: Long,
    messages: List<ParentMessage>,
    expectations: List<ParentExpectation> = emptyList(),
    sholatRecords: List<SholatRecord> = emptyList(),
    isChristianStudent: Boolean = false,
    christianWorshipRecords: List<ChristianWorshipRecord> = emptyList(),
    isEn: Boolean,
    lang: AppLanguage,
    onOpenMengaji: () -> Unit,
    onOpenSholat: () -> Unit = {},
    onOpenChristianWorship: () -> Unit = {},
    onOpenTabungan: () -> Unit,
    onOpenMedical: () -> Unit,
    onOpenFasting: () -> Unit,
    onOpenHaid: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenHarapan: () -> Unit = {}
) {
    val cards = buildList<ParentHighlightCardData> {
        if (isChristianStudent) {
            val activeChristian = christianWorshipRecords.filter { !it.isDeleted }
            val latestChristian = activeChristian.firstOrNull()
            val pagiCount = activeChristian.count {
                ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_PAGI
            }
            val malamCount = activeChristian.count {
                ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_MALAM
            }
            val gerejaCount = activeChristian.count {
                ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_KE_GEREJA
            }
            add(
                ParentHighlightCardData(
                    id = "hl_kristen",
                    badge = if (isEn) "Christian Worship Report" else "Laporan Ibadah Kristen",
                    title = if (latestChristian != null) {
                        ChristianWorshipHelper.activityLabel(latestChristian.activityType, isEn)
                    } else {
                        if (isEn) "Christian Nurture Active" else "Menu Ibadah Kristen Aktif"
                    },
                    mainValue = if (activeChristian.isNotEmpty()) {
                        if (isEn) "Morning:$pagiCount • Evening:$malamCount • Church:$gerejaCount"
                        else "Pagi:$pagiCount • Malam:$malamCount • Gereja:$gerejaCount"
                    } else {
                        if (isEn) "Waiting for caregiver worship logs" else "Menunggu catatan ibadah dari Wali Asuh"
                    },
                    detailLine = latestChristian?.let { rec ->
                        val noteOrTitle = rec.notes.ifBlank { rec.title }
                        noteOrTitle.ifBlank {
                            if (isEn) "Logged by ${rec.waliName} • ${DateUtils.formatShortDate(rec.activityDate, lang)}"
                            else "Dicatat oleh ${rec.waliName} • ${DateUtils.formatShortDate(rec.activityDate, lang)}"
                        }
                    } ?: if (isEn) {
                        "Morning prayer, evening worship, church service & Bible verse progress appear here."
                    } else {
                        "Catatan ibadah pagi, malam, kegiatan gereja & ayat hafalan tampil otomatis."
                    },
                    footerAction = if (isEn) "Open Christian Worship" else "Buka Menu Agama Kristen",
                    icon = Icons.Default.Church,
                    gradientColors = listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF)),
                    accentColor = Color(0xFF4338CA),
                    badgeBg = Color(0xFF3730A3),
                    onClick = onOpenChristianWorship
                )
            )
        }

        if (!isChristianStudent && link?.canViewSholat == true) {
            val todayKey = SholatHelper.todayDateKey()
            val todayRecords = sholatRecords.filter { it.dateKey == todayKey }
            val byWaktu = todayRecords.associateBy { SholatWaktu.fromCode(it.waktu) }
            val stripText = SholatWaktu.entries.joinToString(" • ") { w ->
                val badge = byWaktu[w]?.let { SholatStatus.fromCode(it.status).shortCode } ?: "·"
                "${w.shortCode}:$badge"
            }
            val curMonthKey = SholatHelper.todayDateKey().substring(0, 7)
            val monthRecords = sholatRecords.filter {
                SholatHelper.monthKeyFromDateKey(it.dateKey) == curMonthKey
            }
            val wajibEligible = monthRecords.count {
                SholatStatus.fromCode(it.status) != SholatStatus.UDZUR_HAID
            }
            val jamaahMonthCount = monthRecords.count {
                SholatStatus.fromCode(it.status) == SholatStatus.JAMAAH
            }
            val pct = if (wajibEligible > 0) ((jamaahMonthCount * 100f) / wajibEligible).toInt() else 0
            val todayHasUdzurHaid = todayRecords.any {
                SholatStatus.fromCode(it.status) == SholatStatus.UDZUR_HAID
            }
            add(
                ParentHighlightCardData(
                    id = "hl_sholat",
                    badge = if (isEn) "5-Time Prayers Today" else "Pantauan Sholat 5 Waktu",
                    title = if (todayRecords.isNotEmpty()) {
                        if (isEn) "${todayRecords.size}/5 Prayers Logged Today" else "${todayRecords.size}/5 Waktu Tercatat Hari Ini"
                    } else if (monthRecords.isNotEmpty()) {
                        if (isEn) "$pct% Jamaah This Month" else "$pct% Disiplin Jamaah Bulan Ini"
                    } else {
                        if (isEn) "5-Time Prayer Monitor" else "Pantauan Sholat Harian"
                    },
                    mainValue = stripText,
                    detailLine = when {
                        todayHasUdzurHaid -> if (isEn) {
                            "Udzur Syar'i (Haid) recorded today • Monthly Jamaah: $pct%"
                        } else {
                            "Hari ini berstatus Udzur Syar'i (Haid) • Jamaah bulan ini: $pct%"
                        }
                        monthRecords.isNotEmpty() -> if (isEn) {
                            "$jamaahMonthCount Jamaah out of $wajibEligible obligations ($pct%)"
                        } else {
                            "$jamaahMonthCount kali berjamaah dari $wajibEligible kewajiban ($pct%)"
                        }
                        else -> if (isEn) {
                            "Subuh, Dzuhur, Ashar, Maghrib, and Isya updates appear here."
                        } else {
                            "Kehadiran Subuh, Dzuhur, Ashar, Maghrib & Isya ananda tampil otomatis."
                        }
                    },
                    footerAction = if (isEn) "View Prayer Log" else "Lihat Pantauan Sholat",
                    icon = Icons.Default.AutoAwesome,
                    gradientColors = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1)),
                    accentColor = Color(0xFF0F766E),
                    badgeBg = Color(0xFF0D9488),
                    onClick = onOpenSholat
                )
            )
        }

        if (!isChristianStudent && link?.canViewMengaji == true) {
            val latest = mengajiRecords.firstOrNull()
            val fluency = latest?.let { MengajiHelper.fluencyLabel(it.fluencyStatus, isEn) }
            add(
                ParentHighlightCardData(
                    id = "hl_mengaji",
                    badge = if (isEn) "Latest Mengaji" else "Histori Mengaji Terbaru",
                    title = if (latest != null) {
                        MengajiHelper.formatCompactPosition(latest)
                    } else {
                        if (isEn) "No Mengaji Records Yet" else "Belum Ada Setoran Mengaji"
                    },
                    mainValue = if (latest != null) {
                        "${fluency.orEmpty()} • ${DateUtils.formatShortDate(latest.sessionDate, lang)}"
                    } else {
                        if (isEn) "Waiting for caregiver input" else "Menunggu catatan pembina"
                    },
                    detailLine = if (latest != null) {
                        latest.notes.ifBlank {
                            if (isEn) "Guided by ${latest.waliName} (${mengajiRecords.size} total sessions)"
                            else "Dibimbing oleh ${latest.waliName} (${mengajiRecords.size} total setoran)"
                        }
                    } else {
                        if (isEn) "All Quran / Iqro reading progress will appear here."
                        else "Perkembangan bacaan Iqro/Al-Qur'an ananda akan tampil otomatis di sini."
                    },
                    footerAction = if (isEn) "View Mengaji History" else "Lihat Semua Riwayat Mengaji",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    gradientColors = listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5)),
                    accentColor = Color(0xFF047857),
                    badgeBg = Color(0xFF059669),
                    onClick = onOpenMengaji
                )
            )
        }

        if (link?.canViewTabungan == true) {
            val latestTx = tabunganTransactions.firstOrNull()
            add(
                ParentHighlightCardData(
                    id = "hl_tabungan",
                    badge = if (isEn) "Child Savings Wallet" else "Dompet Tabungan Ananda",
                    title = TabunganHelper.formatRupiah(tabunganBalance),
                    mainValue = if (latestTx != null) {
                        val sign = if (TabunganHelper.isIncome(latestTx.transactionType)) "+" else "-"
                        val label = latestTx.title.ifBlank { if (sign == "+") "Titip Masuk" else "Pengeluaran" }
                        "$label ($sign${TabunganHelper.formatRupiah(latestTx.amount)})"
                    } else {
                        if (isEn) "No transactions yet" else "Belum ada transaksi tercatat"
                    },
                    detailLine = if (latestTx != null) {
                        if (isEn) "Last updated ${DateUtils.formatShortDate(latestTx.transactionDate, lang)} • ${tabunganTransactions.size} entries"
                        else "Terakhir ${DateUtils.formatShortDate(latestTx.transactionDate, lang)} • ${tabunganTransactions.size} mutasi"
                    } else {
                        if (isEn) "Deposits and allowance spending managed transparently."
                        else "Titipan masuk dan jajan ananda tercatat rapi & transparan."
                    },
                    footerAction = if (isEn) "Open Savings Ledger" else "Buka Buku Tabungan",
                    icon = Icons.Default.AccountBalanceWallet,
                    gradientColors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)),
                    accentColor = Color(0xFFB45309),
                    badgeBg = Color(0xFFD97706),
                    onClick = onOpenTabungan
                )
            )
        }

        if (link?.canViewMedical == true) {
            val latestMed = medicalRecords.firstOrNull()
            add(
                ParentHighlightCardData(
                    id = "hl_medical",
                    badge = if (isEn) "Health Condition" else "Kondisi Kesehatan",
                    title = if (latestMed != null) {
                        latestMed.complaint.ifBlank { if (isEn) "Health Check" else "Pemeriksaan Kesehatan" }
                    } else {
                        if (isEn) "Alhamdulillah, Healthy" else "Alhamdulillah, Sehat"
                    },
                    mainValue = if (latestMed != null) {
                        "${latestMed.status} • ${DateUtils.formatShortDate(latestMed.planDate, lang)}"
                    } else {
                        if (isEn) "0 illness records logged" else "Belum ada riwayat keluhan sakit"
                    },
                    detailLine = if (latestMed != null) {
                        latestMed.diagnosis.ifBlank {
                            latestMed.faskes.ifBlank {
                                if (isEn) "${medicalRecords.size} total health records"
                                else "Total ${medicalRecords.size} catatan kesehatan tersimpan"
                            }
                        }
                    } else {
                        if (isEn) "Caregivers continuously monitor your child's well-being."
                        else "Wali asuh selalu memantau kondisi kesehatan ananda di asrama."
                    },
                    footerAction = if (isEn) "View Health History" else "Lihat Riwayat Kesehatan",
                    icon = Icons.Default.LocalHospital,
                    gradientColors = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE)),
                    accentColor = Color(0xFF0369A1),
                    badgeBg = Color(0xFF0284C7),
                    onClick = onOpenMedical
                )
            )
        }

        if (link?.canViewFasting == true) {
            val latestFast = fastingRecords.firstOrNull()
            add(
                ParentHighlightCardData(
                    id = "hl_fasting",
                    badge = if (isEn) "Fasting Activity" else "Aktivitas Puasa Sunnah",
                    title = if (isEn) "${fastingRecords.size} Fasting Sessions" else "${fastingRecords.size} Kali Ikut Puasa",
                    mainValue = if (latestFast != null) {
                        val sahur = if (latestFast.isSahurChecked) "Sahur ✓" else "Sahur -"
                        val buka = if (latestFast.isBukaChecked) "Buka ✓" else "Buka -"
                        "${DateUtils.formatShortDate(latestFast.sessionDate, lang)} • $sahur • $buka"
                    } else {
                        if (isEn) "No fasting sessions yet" else "Belum ada catatan puasa"
                    },
                    detailLine = if (latestFast != null) {
                        latestFast.sessionTitle.ifBlank {
                            if (isEn) "Sahur & Iftar attendance tracked"
                            else "Kehadiran sahur & berbuka terpantau"
                        }
                    } else {
                        if (isEn) "Sunnah Monday-Thursday fasting participation appears here."
                        else "Keikutsertaan puasa Senin-Kamis ananda akan tampil di sini."
                    },
                    footerAction = if (isEn) "View Fasting Log" else "Lihat Riwayat Puasa",
                    icon = Icons.Default.NightlightRound,
                    gradientColors = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1)),
                    accentColor = Color(0xFF0F766E),
                    badgeBg = Color(0xFF0D9488),
                    onClick = onOpenFasting
                )
            )
        }

        if (isFemaleStudent && link?.canViewHaid == true) {
            val activeHaid = haidRecords.firstOrNull { it.isActive }
            add(
                ParentHighlightCardData(
                    id = "hl_haid",
                    badge = if (isEn) "Haid & Suci Tracker" else "Pantauan Haid & Masa Suci",
                    title = if (activeHaid != null) {
                        if (isEn) "Active Haid • Day ${activeHaid.durationDays}" else "Sedang Haid • Hari ke-${activeHaid.durationDays}"
                    } else {
                        if (isEn) "Currently Suci (Clean)" else "Sedang Masa Suci"
                    },
                    mainValue = if (activeHaid != null) {
                        "${if (isEn) "Started" else "Mulai"} ${DateUtils.formatShortDate(activeHaid.startDate, lang)}"
                    } else {
                        if (isEn) "${haidRecords.size} cycle records" else "${haidRecords.size} catatan siklus tercatat"
                    },
                    detailLine = if (isEn) {
                        "Monitored attentively by female dormitory caregivers."
                    } else {
                        "Dipantau dengan penuh perhatian oleh pembina asrama putri."
                    },
                    footerAction = if (isEn) "View Cycle History" else "Lihat Riwayat Siklus",
                    icon = Icons.Default.Favorite,
                    gradientColors = listOf(Color(0xFFFDF2F8), Color(0xFFFCE7F3)),
                    accentColor = Color(0xFFBE185D),
                    badgeBg = Color(0xFFDB2777),
                    onClick = onOpenHaid
                )
            )
        }

        val latestMsg = messages.lastOrNull()
        add(
            ParentHighlightCardData(
                id = "hl_chat",
                badge = if (isEn) "Caregiver Messages" else "Pesan Wali Asuh",
                title = if (latestMsg != null) {
                    latestMsg.senderName.ifBlank { if (isEn) "Caregiver" else "Wali Asuh" }
                } else {
                    if (isEn) "Start a Conversation" else "Hubungi Wali Asuh"
                },
                mainValue = if (latestMsg != null) {
                    DateUtils.formatShortDate(latestMsg.createdAt, lang)
                } else {
                    if (isEn) "Direct chat with your child's caregiver" else "Komunikasi langsung dengan pembina"
                },
                detailLine = latestMsg?.messageContent ?: if (isEn) {
                    "Ask about your child's daily routine, studies, or well-being anytime."
                } else {
                    "Tanyakan kabar, kegiatan, atau perkembangan ananda kapan saja."
                },
                footerAction = if (isEn) "Open Conversation" else "Buka Percakapan",
                icon = Icons.Default.ChatBubbleOutline,
                gradientColors = listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF)),
                accentColor = Color(0xFF4338CA),
                badgeBg = Color(0xFF4F46E5),
                onClick = onOpenMessages
            )
        )

        val latestExpectation = expectations.firstOrNull()
        add(
            ParentHighlightCardData(
                id = "hl_harapan",
                badge = if (isEn) "Character Expectations" else "Harapan Karakter • Ikhtiar Bersama",
                title = if (latestExpectation != null) {
                    latestExpectation.characterExpectation.take(48)
                } else {
                    if (isEn) "Share Your Hopes for Your Child" else "Tuliskan Harapan Karakter Ananda"
                },
                mainValue = if (latestExpectation != null) {
                    if (latestExpectation.waliFollowUpNote.isNotBlank()) {
                        if (isEn) "Responded by Wali Asuh" else "Sudah Ditanggapi Wali Asuh"
                    } else if (latestExpectation.isReadByWali) {
                        if (isEn) "Read by Wali Asuh" else "Sudah Dibaca Wali Asuh"
                    } else {
                        if (isEn) "Sent to Wali Asuh" else "Tersimpan untuk Wali Asuh"
                    }
                } else {
                    if (isEn) "3+ Years at Sekolah Rakyat" else "3 Tahun+ di Sekolah Rakyat"
                },
                detailLine = latestExpectation?.waliFollowUpNote?.takeIf { it.isNotBlank() }
                    ?: if (isEn) {
                        "Share attitude hopes so Wali Asuh can advise your child in a shared effort."
                    } else {
                        "Tuliskan harapan sikap anak agar dinasehatkan Wali Asuh sebagai ikhtiar bersama."
                    },
                footerAction = if (isEn) "Open Character Form" else "Isi / Lihat Harapan Karakter",
                icon = Icons.Default.VolunteerActivism,
                gradientColors = listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5)),
                accentColor = Color(0xFF047857),
                badgeBg = Color(0xFF059669),
                onClick = onOpenHarapan
            )
        )
    }

    if (cards.isEmpty()) return

    val total = cards.size
    val virtualPageCount = if (total > 1) total * 400 else total
    val initialVirtualPage = if (total > 1) {
        val mid = virtualPageCount / 2
        mid - (mid % total)
    } else {
        0
    }

    key(total) {
        val pagerState = rememberPagerState(
            initialPage = initialVirtualPage,
            pageCount = { virtualPageCount }
        )
        val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

        LaunchedEffect(total, isDragged) {
            if (total <= 1 || isDragged) return@LaunchedEffect
            while (true) {
                delay(5_000L)
                if (!pagerState.isScrollInProgress) {
                    val nextPage = if (pagerState.currentPage + 1 < virtualPageCount) {
                        pagerState.currentPage + 1
                    } else {
                        initialVirtualPage
                    }
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        animationSpec = tween(
                            durationMillis = 700,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEn) "Latest Child Highlights" else "Kabar Terbaru Ananda",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (total > 1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Swipe,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isEn) "Swipe left/right" else "Geser kiri/kanan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 12.dp,
                beyondViewportPageCount = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pager_parent_highlights")
            ) { page ->
                val realIdx = page % total
                val card = cards[realIdx]
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                    .absoluteValue
                    .coerceIn(0f, 1f)
                val cardScale = lerp(0.96f, 1f, 1f - pageOffset)
                val cardAlpha = lerp(0.84f, 1f, 1f - pageOffset)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = cardScale
                            scaleY = cardScale
                            alpha = cardAlpha
                        }
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = card.onClick)
                        .testTag("card_highlight_${card.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, card.accentColor.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(card.gradientColors))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = card.badgeBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = card.icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = card.badge,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Text(
                                    text = "${realIdx + 1}/$total",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = card.accentColor.copy(alpha = 0.7f)
                                )
                            }

                            Text(
                                text = card.title,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = card.mainValue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = card.accentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = card.detailLine,
                                fontSize = 12.sp,
                                lineHeight = 16.5.sp,
                                color = Color(0xFF334155),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = card.footerAction,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = card.accentColor
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = card.accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Smooth pill dot indicators
            if (total > 1) {
                val activeIndex = pagerState.currentPage % total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(total) { idx ->
                        val isCurrent = activeIndex == idx
                        val dotWidth by animateDpAsState(
                            targetValue = if (isCurrent) 20.dp else 6.dp,
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = FastOutSlowInEasing
                            ),
                            label = "highlight_dot_width_$idx"
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (isCurrent) Color(0xFF4338CA) else MaterialTheme.colorScheme.outlineVariant,
                            animationSpec = tween(durationMillis = 350),
                            label = "highlight_dot_color_$idx"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(6.dp)
                                .width(dotWidth)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }
        }
    }
}
