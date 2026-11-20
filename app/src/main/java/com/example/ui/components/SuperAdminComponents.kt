package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WaliPrimary

private data class SuperadminMenuTileSpec(
    val title: String,
    val icon: ImageVector,
    val tileBg: Color,
    val iconTint: Color,
    val badgeText: String,
    val badgeBg: Color,
    val isActive: Boolean,
    val testTag: String,
    val onClick: () -> Unit
)

@Composable
fun SuperadminQuickMenuGridCard(
    selectedMainTab: Int,
    selectedStatusFilter: String,
    totalCount: Int,
    pendingCount: Int,
    approvedCount: Int,
    suspendedCount: Int,
    rejectedCount: Int,
    isSyncing: Boolean,
    isEn: Boolean,
    onSelectStatusFilter: (String) -> Unit,
    onSelectMainTab: (Int) -> Unit,
    onRefreshCloud: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val row1 = listOf(
        SuperadminMenuTileSpec(
            title = if (isEn) "All Schools" else "Semua",
            icon = Icons.Default.School,
            tileBg = if (isDark) Color(0xFF0C4A6E).copy(alpha = 0.45f) else Color(0xFFE0F2FE),
            iconTint = if (isDark) Color(0xFF7DD3FC) else Color(0xFF0369A1),
            badgeText = "$totalCount",
            badgeBg = Color(0xFF0369A1),
            isActive = selectedMainTab == 0 && selectedStatusFilter == "Semua",
            testTag = "tile_superadmin_all",
            onClick = {
                onSelectMainTab(0)
                onSelectStatusFilter("Semua")
            }
        ),
        SuperadminMenuTileSpec(
            title = if (isEn) "Pending" else "Menunggu",
            icon = Icons.Default.HourglassTop,
            tileBg = if (isDark) Color(0xFF78350F).copy(alpha = 0.45f) else Color(0xFFFEF3C7),
            iconTint = if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309),
            badgeText = "$pendingCount",
            badgeBg = Color(0xFFD97706),
            isActive = selectedMainTab == 0 && selectedStatusFilter == "PENDING",
            testTag = "tile_superadmin_pending",
            onClick = {
                onSelectMainTab(0)
                onSelectStatusFilter("PENDING")
            }
        ),
        SuperadminMenuTileSpec(
            title = if (isEn) "Approved" else "Disetujui",
            icon = Icons.Default.CheckCircle,
            tileBg = if (isDark) Color(0xFF064E3B).copy(alpha = 0.45f) else Color(0xFFD1FAE5),
            iconTint = if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857),
            badgeText = "$approvedCount",
            badgeBg = Color(0xFF16A34A),
            isActive = selectedMainTab == 0 && selectedStatusFilter == "APPROVED",
            testTag = "tile_superadmin_approved",
            onClick = {
                onSelectMainTab(0)
                onSelectStatusFilter("APPROVED")
            }
        ),
        SuperadminMenuTileSpec(
            title = if (isEn) "Suspended" else "Suspend",
            icon = Icons.Default.Block,
            tileBg = if (isDark) Color(0xFF7C2D12).copy(alpha = 0.45f) else Color(0xFFFFEDD5),
            iconTint = if (isDark) Color(0xFFFDBA74) else Color(0xFFC2410C),
            badgeText = "$suspendedCount",
            badgeBg = Color(0xFFEA580C),
            isActive = selectedMainTab == 0 && selectedStatusFilter == "SUSPENDED",
            testTag = "tile_superadmin_suspended",
            onClick = {
                onSelectMainTab(0)
                onSelectStatusFilter("SUSPENDED")
            }
        )
    )

    val row2 = listOf(
        SuperadminMenuTileSpec(
            title = if (isEn) "Rejected" else "Ditolak",
            icon = Icons.Default.Cancel,
            tileBg = if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.45f) else Color(0xFFFEE2E2),
            iconTint = if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C),
            badgeText = "$rejectedCount",
            badgeBg = Color(0xFFDC2626),
            isActive = selectedMainTab == 0 && selectedStatusFilter == "REJECTED",
            testTag = "tile_superadmin_rejected",
            onClick = {
                onSelectMainTab(0)
                onSelectStatusFilter("REJECTED")
            }
        ),
        SuperadminMenuTileSpec(
            title = if (isEn) "Reports" else "Laporan",
            icon = Icons.Default.Assessment,
            tileBg = if (isDark) Color(0xFF581C87).copy(alpha = 0.45f) else Color(0xFFF3E8FF),
            iconTint = if (isDark) Color(0xFFD8B4FE) else Color(0xFF7E22CE),
            badgeText = if (isEn) "Stats" else "Rekap",
            badgeBg = Color(0xFF7E22CE),
            isActive = selectedMainTab == 1,
            testTag = "tile_superadmin_reports",
            onClick = { onSelectMainTab(1) }
        ),
        SuperadminMenuTileSpec(
            title = "Cloud DB",
            icon = Icons.Default.Code,
            tileBg = if (isDark) Color(0xFF312E81).copy(alpha = 0.45f) else Color(0xFFE0E7FF),
            iconTint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4338CA),
            badgeText = "SQL/PB",
            badgeBg = Color(0xFF4338CA),
            isActive = selectedMainTab == 2,
            testTag = "tile_superadmin_cloud_db",
            onClick = { onSelectMainTab(2) }
        ),
        SuperadminMenuTileSpec(
            title = if (isEn) "Cloud Sync" else "Sync Cloud",
            icon = Icons.Default.CloudSync,
            tileBg = if (isDark) Color(0xFF134E4A).copy(alpha = 0.45f) else Color(0xFFCCFBF1),
            iconTint = if (isDark) Color(0xFF5EEAD4) else Color(0xFF0F766E),
            badgeText = if (isSyncing) "..." else "Live",
            badgeBg = Color(0xFF0F766E),
            isActive = isSyncing,
            testTag = "tile_superadmin_sync",
            onClick = onRefreshCloud
        )
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_superadmin_menu_hub"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = WaliPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isEn) "Superadmin Quick Menu" else "Menu Kontrol Superadmin",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = if (isEn) "Tap a card" else "Ketuk kartu",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row1.forEach { spec ->
                    SuperadminTile(
                        spec = spec,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row2.forEach { spec ->
                    SuperadminTile(
                        spec = spec,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuperadminTile(
    spec: SuperadminMenuTileSpec,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .minimumInteractiveComponentSize()
            .clickable { spec.onClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag(spec.testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, end = 4.dp)
                    .size(50.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(spec.tileBg)
                    .then(
                        if (spec.isActive) {
                            Modifier.border(
                                width = 2.dp,
                                color = spec.iconTint,
                                shape = RoundedCornerShape(15.dp)
                            )
                        } else {
                            Modifier.border(
                                width = 0.75.dp,
                                color = spec.iconTint.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(15.dp)
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = spec.icon,
                    contentDescription = spec.title,
                    tint = spec.iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = spec.badgeBg,
                border = BorderStroke(1.dp, Color.White)
            ) {
                Text(
                    text = spec.badgeText,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }

        Text(
            text = spec.title,
            fontSize = 11.sp,
            lineHeight = 13.5.sp,
            fontWeight = if (spec.isActive) FontWeight.Bold else FontWeight.SemiBold,
            color = if (spec.isActive) spec.iconTint else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
