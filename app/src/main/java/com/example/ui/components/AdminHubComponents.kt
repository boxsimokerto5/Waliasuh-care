package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolProfile
import com.example.ui.theme.WaliPrimary

@Composable
fun AdminHeroSummaryCard(
    schoolProfile: SchoolProfile,
    activeSchoolCode: String,
    studentsCount: Int,
    waliCount: Int,
    jadwalCount: Int,
    sessionsCount: Int,
    isSyncing: Boolean,
    isEn: Boolean,
    onSyncCloud: () -> Unit,
    onEditSchool: () -> Unit,
    onSelectMetricTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_admin_hero"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            WaliPrimary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(WaliPrimary, Color(0xFF0F766E))
                            )
                        )
                        .clickable { onEditSchool() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = if (isEn) "School Profile" else "Profil Sekolah",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WaliPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = activeSchoolCode,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = WaliPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = schoolProfile.address.ifBlank {
                            schoolProfile.city.ifBlank {
                                if (isEn) "Tap icon to edit school profile" else "Ketuk ikon untuk atur profil sekolah"
                            }
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onSyncCloud,
                    enabled = !isSyncing,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("btn_admin_sync_cloud")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isEn) "Sync..." else "Sinkron...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = if (isEn) "Sync Cloud" else "Sinkron Cloud",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEn) "Cloud" else "Sinkron",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4 Metric Quick-Jump Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AdminMetricPill(
                    label = if (isEn) "Students" else "Siswa",
                    value = "$studentsCount",
                    bgColor = Color(0xFFE0F2FE),
                    textColor = Color(0xFF0369A1),
                    onClick = { onSelectMetricTab(0) },
                    modifier = Modifier.weight(1f)
                )
                AdminMetricPill(
                    label = if (isEn) "Wali" else "Wali",
                    value = "$waliCount",
                    bgColor = Color(0xFFE0E7FF),
                    textColor = Color(0xFF4338CA),
                    onClick = { onSelectMetricTab(1) },
                    modifier = Modifier.weight(1f)
                )
                AdminMetricPill(
                    label = if (isEn) "Roster" else "Jadwal",
                    value = "$jadwalCount",
                    bgColor = Color(0xFFCCFBF1),
                    textColor = Color(0xFF0F766E),
                    onClick = { onSelectMetricTab(4) },
                    modifier = Modifier.weight(1f)
                )
                AdminMetricPill(
                    label = if (isEn) "Sessions" else "Sesi",
                    value = "$sessionsCount",
                    bgColor = Color(0xFFFEF3C7),
                    textColor = Color(0xFFB45309),
                    onClick = { onSelectMetricTab(2) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AdminMetricPill(
    label: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor.copy(alpha = 0.72f),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class AdminMenuTileSpec(
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
fun AdminQuickMenuGridCard(
    selectedTab: Int,
    studentsCount: Int,
    waliCount: Int,
    jadwalCount: Int,
    activeFastingCount: Int,
    activeVisitCount: Int,
    isEn: Boolean,
    onSelectTab: (Int) -> Unit,
    onOpenSchoolProfile: () -> Unit,
    onOpenCsvBackupSheet: () -> Unit,
    onOpenSecuritySheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val row1 = listOf(
        AdminMenuTileSpec(
            title = if (isEn) "Students" else "Data Siswa",
            icon = Icons.Default.People,
            tileBg = Color(0xFFE0F2FE),
            iconTint = Color(0xFF0369A1),
            badgeText = "$studentsCount",
            badgeBg = Color(0xFF0369A1),
            isActive = selectedTab == 0,
            testTag = "tile_admin_hub_students",
            onClick = { onSelectTab(0) }
        ),
        AdminMenuTileSpec(
            title = if (isEn) "Wali Acct" else "Akun Wali",
            icon = Icons.Default.Key,
            tileBg = Color(0xFFE0E7FF),
            iconTint = Color(0xFF4338CA),
            badgeText = "$waliCount",
            badgeBg = Color(0xFF4338CA),
            isActive = selectedTab == 1,
            testTag = "tile_admin_hub_wali",
            onClick = { onSelectTab(1) }
        ),
        AdminMenuTileSpec(
            title = if (isEn) "Roster" else "Jadwal",
            icon = Icons.Default.TableChart,
            tileBg = Color(0xFFCCFBF1),
            iconTint = Color(0xFF0F766E),
            badgeText = "$jadwalCount",
            badgeBg = Color(0xFF0F766E),
            isActive = selectedTab == 4,
            testTag = "tile_admin_hub_jadwal",
            onClick = { onSelectTab(4) }
        ),
        AdminMenuTileSpec(
            title = if (isEn) "Puasa" else "Sesi Puasa",
            icon = Icons.Default.RestaurantMenu,
            tileBg = Color(0xFFD1FAE5),
            iconTint = Color(0xFF047857),
            badgeText = if (activeFastingCount > 0) "$activeFastingCount Aktif" else "Sesi",
            badgeBg = Color(0xFF047857),
            isActive = selectedTab == 2,
            testTag = "tile_admin_hub_fasting",
            onClick = { onSelectTab(2) }
        )
    )

    val row2 = listOf(
        AdminMenuTileSpec(
            title = if (isEn) "Visits" else "Kunjungan",
            icon = Icons.Default.CalendarMonth,
            tileBg = Color(0xFFDBEAFE),
            iconTint = Color(0xFF1D4ED8),
            badgeText = if (activeVisitCount > 0) "$activeVisitCount Aktif" else "Sesi",
            badgeBg = Color(0xFF1D4ED8),
            isActive = selectedTab == 3,
            testTag = "tile_admin_hub_visit",
            onClick = { onSelectTab(3) }
        ),
        AdminMenuTileSpec(
            title = if (isEn) "School Info" else "Profil & Kop",
            icon = Icons.Default.Business,
            tileBg = Color(0xFFF3E8FF),
            iconTint = Color(0xFF7E22CE),
            badgeText = if (isEn) "Edit" else "Atur",
            badgeBg = Color(0xFF7E22CE),
            isActive = false,
            testTag = "btn_edit_school_profile",
            onClick = onOpenSchoolProfile
        ),
        AdminMenuTileSpec(
            title = if (isEn) "CSV Backup" else "Impor/Ekspor",
            icon = Icons.Default.FileUpload,
            tileBg = Color(0xFFFEF3C7),
            iconTint = Color(0xFFB45309),
            badgeText = "CSV",
            badgeBg = Color(0xFFD97706),
            isActive = false,
            testTag = "tile_admin_hub_csv",
            onClick = onOpenCsvBackupSheet
        ),
        AdminMenuTileSpec(
            title = if (isEn) "Security" else "Keamanan",
            icon = Icons.Default.Security,
            tileBg = Color(0xFFFCE7F3),
            iconTint = Color(0xFFBE185D),
            badgeText = if (isEn) "Admin" else "Reset",
            badgeBg = Color(0xFFBE185D),
            isActive = false,
            testTag = "tile_admin_hub_security",
            onClick = onOpenSecuritySheet
        )
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_admin_menu_hub"),
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
                        text = if (isEn) "School Admin Menu" else "Menu Pengelola Sekolah",
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
                    AdminSuperAppTile(
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
                    AdminSuperAppTile(
                        spec = spec,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminSuperAppTile(
    spec: AdminMenuTileSpec,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCsvBackupSheet(
    studentsCount: Int,
    waliCount: Int,
    isEn: Boolean,
    onImportStudents: () -> Unit,
    onExportStudents: () -> Unit,
    onImportWali: () -> Unit,
    onExportWali: () -> Unit,
    onOpenSchoolProfile: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                    .widthIn(max = 600.dp)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp)
                    .testTag("sheet_admin_csv_backup"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Import, Export CSV & Profile" else "Impor, Ekspor CSV & Profil",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEn) {
                                "Bulk manage student & Wali Asuh records via CSV files."
                            } else {
                                "Kelola data anak asuh & akun wali asuh secara massal lewat file CSV."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_admin_csv_backup")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                // Siswa CSV Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "Student Master Data (CSV)" else "Master Data Siswa (CSV)",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = if (isEn) "$studentsCount Registered" else "$studentsCount Terdaftar",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onDismiss()
                                    onImportStudents()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_import_students_csv")
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEn) "Import Students" else "Import Siswa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onExportStudents()
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF16A34A)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_export_students_csv")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF15803D))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEn) "Export (.csv)" else "Export (.csv)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                        }
                    }
                }

                // Wali CSV Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "Wali Asuh Accounts (CSV)" else "Akun Wali Asuh (CSV)",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                            Text(
                                text = if (isEn) "$waliCount Accounts" else "$waliCount Akun",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onDismiss()
                                    onImportWali()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_import_wali_csv")
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEn) "Import Wali" else "Import Wali", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onExportWali()
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF2563EB)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_export_wali_csv")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1D4ED8))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEn) "Export (.csv)" else "Export (.csv)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenSchoolProfile()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(17.dp), tint = WaliPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEn) "Edit School Name, Address & Referral Facility" else "Atur Profil Sekolah, Kop Surat & Faskes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WaliPrimary
                    )
                }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSecurityMaintenanceSheet(
    schoolName: String,
    activeSchoolCode: String,
    isEn: Boolean,
    onChangePassword: () -> Unit,
    onClearSchoolData: () -> Unit,
    onDeleteSchoolAccount: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                    .widthIn(max = 600.dp)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp)
                    .testTag("sheet_admin_security"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Security & Data Maintenance" else "Keamanan & Pemeliharaan Data",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$schoolName ($activeSchoolCode)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_admin_security")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                // Change Admin Password
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isEn) "School Admin Credentials" else "Keamanan Akun Admin",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEn) {
                                "Update the Administrator login password regularly to protect student and caregiver access."
                            } else {
                                "Perbarui password login Admin Sekolah secara berkala agar akses data siswa & wali tetap aman."
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onChangePassword()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_admin_change_password"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(17.dp), tint = WaliPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEn) "Change School Admin Password" else "Ganti Password Admin Sekolah",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WaliPrimary
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Danger Zone
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    border = BorderStroke(1.dp, Color(0xFFFECDD3))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isEn) "Danger Zone" else "Zona Berbahaya (Tindakan Permanen)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onClearSchoolData()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_clear_school_data"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.55f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Clear School Students & Wali Data (Reset to 0)" else "Bersihkan Data Siswa & Wali (Reset ke 0)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Button(
                            onClick = {
                                onDismiss()
                                onDeleteSchoolAccount()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_delete_admin_school_account"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = if (isEn) "Delete School Admin Account Permanently" else "Hapus Akun Admin Sekolah Permanen",
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Permanently Delete Admin & School Data" else "Hapus Akun Admin & Data Sekolah Permanen",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                }
            }
        }
    }
}
