package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HealthViewModel
import com.example.ui.components.StudentAvatar
import com.example.ui.dialogs.ChangeWaliPasswordDialog
import com.example.ui.dialogs.MonthlyReportContent
import com.example.util.i18n.LocalAppStrings

private enum class PetugasKesehatanTab(val testTag: String) {
    RENCANA("nav_petugas_rencana"),
    TINDAKAN("nav_petugas_tindakan"),
    LAPORAN("nav_petugas_laporan"),
    ANAK_ASUH("nav_petugas_anak_asuh")
}

/**
 * Portal khusus akun Petugas Kesehatan / UKS Sekolah:
 * 1. Rencana: melihat & membuat rencana berobat / kontrol kesehatan
 * 2. Tindakan: mencatat tindakan & hasil pengobatan medis
 * 3. Laporan: laporan bulanan kesehatan, statistik diagnosa, rujukan faskes, WA & PDF
 * 4. Anak Asuh: daftar seluruh anak asuh sekolah, klik membuka Profil & Riwayat Kesehatannya saja
 */
@Composable
fun PetugasKesehatanScreen(
    viewModel: HealthViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish

    val medicalTeal = Color(0xFF0E7490)
    val medicalDark = Color(0xFF155E75)
    val medicalLight = Color(0xFFCFFAFE)

    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val activePlans by viewModel.activePlans.collectAsStateWithLifecycle()
    val pendingControls by viewModel.pendingControlRecords.collectAsStateWithLifecycle()
    val completedRecords by viewModel.filteredCompletedRecords.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val historySearchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()
    val studentSearchQuery by viewModel.studentSearchQuery.collectAsStateWithLifecycle()
    val waliName by viewModel.waliName.collectAsStateWithLifecycle()
    val currentUsername by viewModel.currentUsername.collectAsStateWithLifecycle()
    val waliAccounts by viewModel.waliAccounts.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    val currentAccount = remember(waliAccounts, currentUsername, waliName) {
        waliAccounts.firstOrNull {
            (!currentUsername.isNullOrBlank() && it.username.equals(currentUsername, ignoreCase = true)) ||
                it.name.equals(waliName, ignoreCase = true)
        }
    }

    var currentTab by rememberSaveable { mutableStateOf(PetugasKesehatanTab.RENCANA) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = currentTab != PetugasKesehatanTab.RENCANA) {
        currentTab = PetugasKesehatanTab.RENCANA
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(medicalLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalHospital,
                                    contentDescription = null,
                                    tint = medicalTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) "Health Officer Portal" else "Portal Petugas Kesehatan",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.5.sp,
                                    color = medicalDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${waliName.ifBlank { if (isEn) "Health Officer" else "Petugas Kesehatan" }} • ${schoolProfile.shortName.ifBlank { schoolProfile.schoolName }}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    viewModel.syncAllWithCloud { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isSyncing,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_petugas_sync")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = medicalTeal
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = if (isEn) "Sync Cloud" else "Sinkronisasi Cloud",
                                        tint = medicalTeal
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showChangePasswordDialog = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_petugas_profile")
                            ) {
                                if (currentAccount?.hasProfilePhoto() == true) {
                                    StudentAvatar(
                                        name = currentAccount.name.ifBlank { waliName },
                                        gender = "L",
                                        size = 32.dp,
                                        photoUrl = currentAccount.photoUrl,
                                        photoFallbackUrl = currentAccount.photoFallbackUrl
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = if (isEn) "Profile & Password" else "Profil & Kata Sandi",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showLogoutDialog = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_petugas_logout")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = if (isEn) "Sign Out" else "Keluar",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                val items: List<Triple<PetugasKesehatanTab, String, ImageVector>> = listOf(
                    Triple(PetugasKesehatanTab.RENCANA, if (isEn) "Plans" else "Rencana", Icons.Default.EventNote),
                    Triple(PetugasKesehatanTab.TINDAKAN, if (isEn) "Actions" else "Tindakan", Icons.Default.MedicalServices),
                    Triple(PetugasKesehatanTab.LAPORAN, if (isEn) "Reports" else "Laporan", Icons.Default.Assessment),
                    Triple(PetugasKesehatanTab.ANAK_ASUH, if (isEn) "Students" else "Anak Asuh", Icons.Default.People)
                )
                items.forEach { (tab, label, icon) ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = medicalTeal,
                            selectedTextColor = medicalTeal,
                            indicatorColor = medicalLight.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            when (currentTab) {
                PetugasKesehatanTab.RENCANA -> {
                    PlanScreen(
                        viewModel = viewModel,
                        plans = activePlans,
                        pendingControls = pendingControls,
                        students = students,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                PetugasKesehatanTab.TINDAKAN -> {
                    MedicalRecordScreen(
                        viewModel = viewModel,
                        waliName = waliName,
                        completedRecords = completedRecords,
                        allRecords = allRecords,
                        students = students,
                        searchQuery = historySearchQuery,
                        onNavigateToProfile = { showChangePasswordDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                PetugasKesehatanTab.LAPORAN -> {
                    MonthlyReportContent(
                        records = allRecords,
                        students = students,
                        waliName = waliName,
                        schoolProfile = schoolProfile,
                        showCloseButton = false,
                        fillAvailableHeight = true,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 680.dp)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("petugas_kesehatan_monthly_report")
                    )
                }
                PetugasKesehatanTab.ANAK_ASUH -> {
                    StudentsScreen(
                        viewModel = viewModel,
                        students = students,
                        records = allRecords,
                        searchQuery = studentSearchQuery,
                        medicalOnlyMode = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (showChangePasswordDialog) {
        ChangeWaliPasswordDialog(
            viewModel = viewModel,
            currentAccount = currentAccount,
            currentWaliName = waliName,
            schoolName = schoolProfile.schoolName,
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = if (isEn) "Sign Out?" else "Keluar dari Portal Kesehatan?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (isEn) {
                        "You will return to the login screen."
                    } else {
                        "Anda akan keluar dari sesi Petugas Kesehatan dan kembali ke halaman masuk."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_logout_petugas")
                ) {
                    Text(if (isEn) "Sign Out" else "Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }
}
