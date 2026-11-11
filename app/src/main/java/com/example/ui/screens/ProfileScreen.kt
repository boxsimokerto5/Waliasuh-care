package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.MonthlyReportDialog
import com.example.ui.dialogs.SupabaseConfigDialog
import com.example.util.TopOnAdManager
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicalRecord
import com.example.ui.HealthViewModel
import com.example.ui.UserRole
import com.example.ui.theme.WaliPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: HealthViewModel,
    waliName: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    records: List<MedicalRecord>,
    onLogout: () -> Unit = {},
    onNavigateToAdmin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsStateWithLifecycle()
    val role by viewModel.currentUserRole.collectAsStateWithLifecycle()
    var showEditAccountDialog by remember { mutableStateOf(false) }
    var showPuskesmasDialog by remember { mutableStateOf(false) }
    var showNotifDialog by remember { mutableStateOf(false) }
    var showSupabaseDialog by remember { mutableStateOf(false) }
    var showMonthlyReportDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Titles from Image 1: "Menu Lainnya", "Wali: Sari Devi"
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Menu Lainnya",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Wali: $waliName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // GROUP 1: Pengaturan App
            item {
                SectionHeader(title = "Pengaturan App")
            }

            item {
                MenuSettingCard(
                    title = "Database Supabase Cloud",
                    subtitle = "Sinkronisasi data online & backup PostgreSQL",
                    icon = Icons.Default.CloudSync,
                    iconBg = Color(0xFFDCFCE7),
                    iconColor = Color(0xFF059669),
                    onClick = {
                        TopOnAdManager.onUserActionClick(context)
                        showSupabaseDialog = true
                    },
                    testTag = "menu_supabase"
                )
            }

            item {
                MenuSettingCard(
                    title = "Puskesmas Saya",
                    subtitle = "Daftar Puskesmas langganan",
                    icon = Icons.Default.LocalHospital,
                    iconBg = Color(0xFFE0F2FE),
                    iconColor = WaliPrimary,
                    onClick = {
                        TopOnAdManager.onUserActionClick(context)
                        showPuskesmasDialog = true
                    },
                    testTag = "menu_puskesmas_saya"
                )
            }

            item {
                MenuSettingCard(
                    title = "Notifikasi",
                    subtitle = "Atur pengingat berobat",
                    icon = Icons.Default.Notifications,
                    iconBg = Color(0xFFEFF6FF),
                    iconColor = Color(0xFF2563EB),
                    onClick = {
                        TopOnAdManager.onUserActionClick(context)
                        showNotifDialog = true
                    },
                    testTag = "menu_notifikasi"
                )
            }

            item {
                MenuSettingCard(
                    title = "Akun",
                    subtitle = "Detail wali dan kata sandi",
                    icon = Icons.Default.AccountCircle,
                    iconBg = Color(0xFFE2E8F0),
                    iconColor = Color(0xFF475569),
                    onClick = {
                        TopOnAdManager.onUserActionClick(context)
                        showEditAccountDialog = true
                    },
                    testTag = "menu_akun"
                )
            }

            item {
                // Theme Toggle Item (matching Image 1 style)
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isDarkTheme) "Tema Gelap (Aktif)" else "Tema Terang (Aktif)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Beralih tampilan terang / gelap",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = {
                                TopOnAdManager.onUserActionClick(context)
                                onToggleTheme()
                            },
                            modifier = Modifier.testTag("switch_theme_profile")
                        )
                    }
                }
            }

            // GROUP 2: Laporan
            item {
                SectionHeader(title = "Laporan")
            }

            item {
                MenuSettingCard(
                    title = "Laporan Bulanan",
                    subtitle = "Rekapitulasi lengkap, statistik sakit & ekspor PDF/WA",
                    icon = Icons.Default.Assessment,
                    iconBg = Color(0xFFE0F2FE),
                    iconColor = WaliPrimary,
                    onClick = {
                        TopOnAdManager.showRewardAdForMonthlyReport(context) {
                            showMonthlyReportDialog = true
                        }
                    },
                    testTag = "menu_laporan_bulanan"
                )
            }

            // GROUP 3: Lainnya
            item {
                SectionHeader(title = "Lainnya")
            }

            if (role == UserRole.ADMIN && onNavigateToAdmin != null) {
                item {
                    MenuSettingCard(
                        title = "Portal Administrator",
                        subtitle = "Kelola akun & kartu akses login wali asuh",
                        icon = Icons.Default.AdminPanelSettings,
                        iconBg = Color(0xFFE0F2FE),
                        iconColor = Color(0xFF0288D1),
                        onClick = onNavigateToAdmin,
                        testTag = "menu_portal_admin"
                    )
                }
            }

            item {
                MenuSettingCard(
                    title = "Panduan",
                    subtitle = "Panduan penggunaan aplikasi",
                    icon = Icons.Default.MenuBook,
                    iconBg = Color(0xFFE2E8F0),
                    iconColor = Color(0xFF334155),
                    onClick = {
                        TopOnAdManager.onUserActionClick(context)
                        showGuideDialog = true
                    },
                    testTag = "menu_panduan"
                )
            }

            item {
                MenuSettingCard(
                    title = "Keluar",
                    subtitle = "Keluar dari sesi login aplikasi",
                    icon = Icons.Default.ExitToApp,
                    iconBg = Color(0xFFFEE2E2),
                    iconColor = Color(0xFFDC2626),
                    onClick = { showExitDialog = true },
                    testTag = "menu_keluar"
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_sr_care),
                        contentDescription = "Logo Wali Asuh Care",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = "Wali Asuh Care v1.0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sekolah Rakyat SRT 1 Kabupaten Kediri",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet: Akun Wali (Google Maps Style)
    if (showEditAccountDialog) {
        var tempName by remember { mutableStateOf(waliName) }
        ModalBottomSheet(
            onDismissRequest = { showEditAccountDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Detail Akun Wali Asuh", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Nama Wali / Pengasuh") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Peran: Wali Asrama & Koordinator Kesehatan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Sekolah: Sekolah Rakyat SRT 1 Kab. Kediri", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showEditAccountDialog = false }, shape = RoundedCornerShape(10.dp)) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.setWaliName(tempName)
                                showEditAccountDialog = false
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Puskesmas Saya (Google Maps Style)
    if (showPuskesmasDialog) {
        ModalBottomSheet(
            onDismissRequest = { showPuskesmasDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Puskesmas Langganan Rujukan", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("1. Puskesmas Wates", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Jl. Raya Wates No. 45, Kab. Kediri", fontSize = 11.sp)
                            Text("Telepon: (0354) 441012 • UGD 24 Jam", fontSize = 11.sp)
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("2. Puskesmas Plosoklaten", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Jl. Raya Plosoklaten No. 12, Kab. Kediri", fontSize = 11.sp)
                            Text("Telepon: (0354) 479155 • UGD 24 Jam", fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { showPuskesmasDialog = false }, shape = RoundedCornerShape(10.dp)) {
                            Text("Tutup")
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Notifikasi
    if (showNotifDialog) {
        ModalBottomSheet(
            onDismissRequest = { showNotifDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Pengaturan Notifikasi Resmi", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("✅ Notifikasi Resmi Google Android: Aktif", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WaliPrimary)
                            Text("🔔 Saluran: Laporan Kesehatan Anak Asuh", fontSize = 12.sp)
                            Text("📌 Notifikasi otomatis muncul di bar atas ponsel saat ada anak asuh sakit baru diinput.", fontSize = 12.sp)
                            Text("✅ Pengingat Berangkat: Aktif (08:00 WIB)", fontSize = 12.sp)
                            Text("✅ Notifikasi Jadwal Kontrol: Aktif (H-1 & Hari H)", fontSize = 12.sp)
                            Text("✅ Laporan Rekam Medis WhatsApp: Siap", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.sendTestNotification()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🔔 Coba Tes Notifikasi", fontSize = 12.sp)
                        }
                        Button(onClick = { showNotifDialog = false }, shape = RoundedCornerShape(10.dp)) {
                            Text("Tutup", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Konfigurasi & Sinkronisasi Supabase
    if (showSupabaseDialog) {
        SupabaseConfigDialog(
            viewModel = viewModel,
            onDismiss = { showSupabaseDialog = false }
        )
    }

    // Modal Bottom Sheet: Laporan Bulanan
    if (showMonthlyReportDialog) {
        MonthlyReportDialog(
            records = records,
            students = students,
            waliName = waliName,
            onDismiss = { showMonthlyReportDialog = false }
        )
    }

    // Modal Bottom Sheet: Panduan
    if (showGuideDialog) {
        ModalBottomSheet(
            onDismissRequest = { showGuideDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Panduan Penggunaan Aplikasi", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("1. Masukkan anak sakit melalui tombol (+) di Dashboard atau Rencana.", fontSize = 12.sp)
                            Text("2. Pilih Puskesmas Wates atau Puskesmas Plosoklaten dan jam berangkat.", fontSize = 12.sp)
                            Text("3. Setelah tiba di puskesmas, nakes/pengasuh mencatat hasil diagnosa & obat.", fontSize = 12.sp)
                            Text("4. Tentukan tanggal kontrol atau centang tidak perlu kontrol.", fontSize = 12.sp)
                            Text("5. Bagikan laporan ke grup WhatsApp pengasuh dengan tombol Laporan WA.", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { showGuideDialog = false }, shape = RoundedCornerShape(10.dp)) {
                            Text("Tutup")
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Keluar
    if (showExitDialog) {
        ModalBottomSheet(
            onDismissRequest = { showExitDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Konfirmasi Keluar", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Apakah Anda yakin ingin keluar dari sesi Wali Asuh Care?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showExitDialog = false }, shape = RoundedCornerShape(10.dp)) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showExitDialog = false
                                viewModel.logout()
                                onLogout()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Keluar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 2.dp, bottom = 1.dp, start = 2.dp)
    )
}

@Composable
fun MenuSettingCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
