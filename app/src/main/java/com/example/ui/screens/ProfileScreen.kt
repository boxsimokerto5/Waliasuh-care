package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.LocalAppStrings
import com.example.ui.dialogs.ChangeWaliPasswordDialog
import com.example.ui.dialogs.DeleteAccountBottomSheet
import com.example.ui.dialogs.FaskesReferralSheet
import com.example.ui.dialogs.MonthlyReportDialog
import com.example.ui.dialogs.ProfileLegalBottomSheet
import com.example.ui.dialogs.ProfileLegalSheetType
import com.example.ui.dialogs.RewardAdDeclarationDialog
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val currentLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val role by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val currentWaliAccount by viewModel.currentWaliAccount.collectAsStateWithLifecycle()
    var showEditAccountDialog by remember { mutableStateOf(false) }
    var showPuskesmasDialog by remember { mutableStateOf(false) }
    var showNotifDialog by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showMonthlyReportDialog by remember { mutableStateOf(false) }
    var showMonthlyReportAdConfirm by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showDeleteAccountSheet by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var deleteAccountError by remember { mutableStateOf<String?>(null) }
    var activeLegalSheet by remember { mutableStateOf<ProfileLegalSheetType?>(null) }

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
            // Header Titles
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "More Menu" else "Menu Lainnya",
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
                SectionHeader(title = s.nav.profileAppSettingsSection)
            }

            item {
                MenuSettingCard(
                    title = if (isEn) "My Puskesmas" else "Puskesmas Saya",
                    subtitle = if (isEn) "Registered referral Puskesmas list" else "Daftar Puskesmas langganan",
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
                    title = if (isEn) "Notifications" else "Notifikasi",
                    subtitle = if (isEn) "Manage medical treatment alerts" else "Atur pengingat berobat",
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
                    title = if (isEn) "Account & Password" else "Akun & Kata Sandi",
                    subtitle = if (isEn) "Account profile & change login password" else "Profil akun & ganti kata sandi login",
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
                // Theme Toggle Item
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
                                    text = if (isEn) {
                                        if (isDarkTheme) s.nav.profileThemeDarkActive else s.nav.profileThemeLightActive
                                    } else {
                                        if (isDarkTheme) "Tema Gelap (Aktif)" else "Tema Terang (Aktif)"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEn) "Switch light / dark display" else "Beralih tampilan terang / gelap",
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

            item {
                // Language Selector Card
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clickable {
                            TopOnAdManager.onUserActionClick(context)
                            showLanguageSheet = true
                        }
                        .testTag("menu_language_profile"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = s.nav.profileLanguageTitle,
                                    tint = WaliPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = s.nav.profileLanguageTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = s.nav.profileLanguageSubtitle(currentLanguage.displayName),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WaliPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = currentLanguage.shortBadge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WaliPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // GROUP 2: Laporan
            item {
                SectionHeader(title = if (isEn) "Reports" else "Laporan")
            }

            item {
                MenuSettingCard(
                    title = if (isEn) "Monthly Report" else "Laporan Bulanan",
                    subtitle = if (isEn) {
                        "Full recap, illness stats & PDF/WA export • Watch short sponsor video"
                    } else {
                        "Rekapitulasi lengkap, statistik sakit & ekspor PDF/WA • Tonton iklan video singkat"
                    },
                    icon = Icons.Default.Assessment,
                    iconBg = Color(0xFFE0F2FE),
                    iconColor = WaliPrimary,
                    badgeText = if (isEn) "Video Ad" else "Iklan Video",
                    onClick = {
                        showMonthlyReportAdConfirm = true
                    },
                    testTag = "menu_laporan_bulanan"
                )
            }

            // GROUP 3: Lainnya
            item {
                SectionHeader(title = if (isEn) "Others" else "Lainnya")
            }

            item {
                MenuSettingCard(
                    title = if (isEn) "User Guide" else "Panduan",
                    subtitle = if (isEn) "How to use Wali Asuh Care" else "Panduan penggunaan aplikasi",
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

            // GROUP 4: Informasi & Legal
            item {
                SectionHeader(title = if (isEn) "Information & Legal" else "Informasi & Legal")
            }

            item {
                MenuSettingCard(
                    title = s.nav.profileAboutTitle,
                    subtitle = if (isEn) "App version, functions & independence notice" else "Versi aplikasi, fungsi & status independensi",
                    icon = Icons.Default.Info,
                    iconBg = Color(0xFFE0F2FE),
                    iconColor = WaliPrimary,
                    onClick = { activeLegalSheet = ProfileLegalSheetType.ABOUT },
                    testTag = "menu_tentang_aplikasi"
                )
            }

            item {
                MenuSettingCard(
                    title = s.nav.profilePrivacyTitle,
                    subtitle = if (isEn) "Privacy policy, student data & device permissions" else "Kebijakan privasi, data siswa & izin perangkat",
                    icon = Icons.Default.PrivacyTip,
                    iconBg = Color(0xFFCCFBF1),
                    iconColor = Color(0xFF0F766E),
                    onClick = { activeLegalSheet = ProfileLegalSheetType.PRIVACY_POLICY },
                    testTag = "menu_privacy_policy"
                )
            }

            item {
                MenuSettingCard(
                    title = s.nav.profileTermsTitle,
                    subtitle = if (isEn) "Terms, conditions & usage disclaimer" else "Syarat, ketentuan & disclaimer penggunaan",
                    icon = Icons.Default.Gavel,
                    iconBg = Color(0xFFF3E8FF),
                    iconColor = Color(0xFF7C3AED),
                    onClick = { activeLegalSheet = ProfileLegalSheetType.TERMS_OF_SERVICE },
                    testTag = "menu_terms_service"
                )
            }

            item {
                MenuSettingCard(
                    title = if (isEn) "Contact Us" else "Hubungi Kami",
                    subtitle = if (isEn) "Contact developer support (Email & WhatsApp)" else "Hubungi pengembang (Email & WhatsApp dukungan)",
                    icon = Icons.Default.SupportAgent,
                    iconBg = Color(0xFFDCFCE7),
                    iconColor = Color(0xFF16A34A),
                    onClick = { activeLegalSheet = ProfileLegalSheetType.CONTACT_US },
                    testTag = "menu_contact_us"
                )
            }

            if (role == UserRole.WALI) {
                item {
                    MenuSettingCard(
                        title = if (isEn) "Delete My Account" else "Hapus Akun Saya",
                        subtitle = if (isEn) "Permanently delete this Wali Asuh login account" else "Hapus permanen akun login Wali Asuh ini",
                        icon = Icons.Default.DeleteForever,
                        iconBg = Color(0xFFFEE2E2),
                        iconColor = Color(0xFFB91C1C),
                        onClick = {
                            deleteAccountError = null
                            showDeleteAccountSheet = true
                        },
                        testTag = "menu_hapus_akun_wali"
                    )
                }
            }

            item {
                MenuSettingCard(
                    title = if (isEn) s.nav.logoutBtn else "Keluar",
                    subtitle = if (isEn) "Sign out from current app session" else "Keluar dari sesi login aplikasi",
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .testTag("profile_branding_logos"),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            shadowElevation = 1.dp
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_kemensos),
                                contentDescription = "Logo Kementerian Sosial RI",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.logo_sr_care),
                            contentDescription = "Logo Wali Asuh Care",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )

                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            shadowElevation = 1.dp
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_sekolah_rakyat),
                                contentDescription = "Logo Sekolah Rakyat",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(5.dp)
                            )
                        }
                    }
                    Text(
                        text = "Wali Asuh Care v1.0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = schoolProfile.schoolName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        onClick = { showGuideDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .testTag("profile_independent_disclaimer")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = s.nav.profileDisclaimerLabel,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = stringResource(R.string.disclaimer_short),
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Profil & Ganti Kata Sandi Wali Asuh (Tersinkronisasi ke Admin Sekolah)
    if (showEditAccountDialog) {
        ChangeWaliPasswordDialog(
            viewModel = viewModel,
            currentAccount = currentWaliAccount,
            currentWaliName = waliName,
            schoolName = schoolProfile.schoolName,
            onDismiss = { showEditAccountDialog = false }
        )
    }

    // Modal Bottom Sheet: Bahasa Aplikasi
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
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
                        .padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = s.nav.profileLanguageSheetTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = s.nav.profileLanguageSheetSubtitle,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    LanguageOptionCard(
                        language = AppLanguage.INDONESIAN,
                        description = s.nav.profileLanguageIdDesc,
                        isSelected = currentLanguage == AppLanguage.INDONESIAN,
                        activeTag = s.nav.profileLanguageActiveTag,
                        testTag = "option_lang_id",
                        onClick = {
                            viewModel.setAppLanguage(AppLanguage.INDONESIAN)
                            showLanguageSheet = false
                        }
                    )

                    LanguageOptionCard(
                        language = AppLanguage.ENGLISH,
                        description = s.nav.profileLanguageEnDesc,
                        isSelected = currentLanguage == AppLanguage.ENGLISH,
                        activeTag = s.nav.profileLanguageActiveTag,
                        testTag = "option_lang_en",
                        onClick = {
                            viewModel.setAppLanguage(AppLanguage.ENGLISH)
                            showLanguageSheet = false
                        }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet: Puskesmas Saya (Dinamis sesuai Faskes Tenant Sekolah)
    if (showPuskesmasDialog) {
        FaskesReferralSheet(
            schoolProfile = schoolProfile,
            title = if (isEn) "Registered Referral Puskesmas" else "Puskesmas Langganan Rujukan",
            onDismiss = { showPuskesmasDialog = false }
        )
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
                    Text(
                        text = if (isEn) "Official Notification Settings" else "Pengaturan Notifikasi Resmi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (isEn) "✅ Official Android Notifications: Active" else "✅ Notifikasi Resmi Google Android: Aktif",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WaliPrimary
                            )
                            Text(
                                text = if (isEn) "🔔 Channel: ${s.report.notifChannelName}" else "🔔 Saluran: Laporan Kesehatan Anak Asuh",
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isEn) {
                                    "📌 Alerts appear in your status bar automatically when a sick student report is added."
                                } else {
                                    "📌 Notifikasi otomatis muncul di bar atas ponsel saat ada anak asuh sakit baru diinput."
                                },
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isEn) "✅ Departure Reminder: Active (08:00 WIB)" else "✅ Pengingat Berangkat: Aktif (08:00 WIB)",
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isEn) "✅ Follow-up Checkup Alerts: Active (D-1 & Day D)" else "✅ Notifikasi Jadwal Kontrol: Aktif (H-1 & Hari H)",
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isEn) "✅ WhatsApp Medical Report: Ready" else "✅ Laporan Rekam Medis WhatsApp: Siap",
                                fontSize = 12.sp
                            )
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
                            Text(if (isEn) "🔔 Send Test Alert" else "🔔 Coba Tes Notifikasi", fontSize = 12.sp)
                        }
                        Button(onClick = { showNotifDialog = false }, shape = RoundedCornerShape(10.dp)) {
                            Text(s.nav.closeBtn, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Laporan Bulanan
    if (showMonthlyReportDialog) {
        MonthlyReportDialog(
            records = records,
            students = students,
            waliName = waliName,
            schoolProfile = schoolProfile,
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
                    Text(
                        text = if (isEn) "App Usage Guide" else "Panduan Penggunaan Aplikasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val guideFaskes = schoolProfile.getAllFaskes()
                                .takeIf { it.isNotEmpty() }
                                ?.joinToString(" / ")
                                ?: if (isEn) "Referral Facility" else "Faskes Rujukan"
                            if (isEn) {
                                Text("1. Add a sick student via the (+) button on Dashboard or Plans.", fontSize = 12.sp)
                                Text("2. Choose destination facility ($guideFaskes) and departure time.", fontSize = 12.sp)
                                Text("3. After arriving at UKS/Puskesmas, record the diagnosis & medication.", fontSize = 12.sp)
                                Text("4. Set a follow-up checkup date or mark as no follow-up needed.", fontSize = 12.sp)
                                Text("5. Share reports to the Wali Asuh WhatsApp group via the WA Report button.", fontSize = 12.sp)
                            } else {
                                Text("1. Masukkan anak sakit melalui tombol (+) di Dashboard atau Rencana.", fontSize = 12.sp)
                                Text("2. Pilih faskes tujuan ($guideFaskes) dan jam berangkat.", fontSize = 12.sp)
                                Text("3. Setelah tiba di faskes/puskesmas, nakes/pengasuh mencatat hasil diagnosa & obat.", fontSize = 12.sp)
                                Text("4. Tentukan tanggal kontrol atau centang tidak perlu kontrol.", fontSize = 12.sp)
                                Text("5. Bagikan laporan ke grup WhatsApp pengasuh dengan tombol Laporan WA.", fontSize = 12.sp)
                            }
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isEn) "About & App Status" else "Tentang & Status Aplikasi",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = stringResource(R.string.disclaimer_full),
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { showGuideDialog = false }, shape = RoundedCornerShape(10.dp)) {
                            Text(s.nav.closeBtn)
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
                    Text(
                        text = if (isEn) s.nav.logoutConfirmTitle else "Konfirmasi Keluar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = if (isEn) "Are you sure you want to log out of the Wali Asuh Care session?" else "Apakah Anda yakin ingin keluar dari sesi Wali Asuh Care?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showExitDialog = false }, shape = RoundedCornerShape(10.dp)) {
                            Text(s.nav.cancelBtn)
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
                            Text(s.nav.logoutBtn)
                        }
                    }
                }
            }
        }
    }

    if (showMonthlyReportAdConfirm) {
        RewardAdDeclarationDialog(
            title = if (isEn) "Open Monthly Report" else "Buka Laporan Bulanan",
            actionDescription = if (isEn) "open the full Monthly Report & export PDF" else "membuka Laporan Bulanan lengkap & ekspor PDF",
            confirmButtonText = if (isEn) "Watch & Open Report" else "Tonton & Buka Laporan",
            onConfirmWatchAd = {
                showMonthlyReportAdConfirm = false
                TopOnAdManager.showRewardAdForMonthlyReport(context) {
                    showMonthlyReportDialog = true
                }
            },
            onDismiss = { showMonthlyReportAdConfirm = false }
        )
    }

    activeLegalSheet?.let { sheetType ->
        ProfileLegalBottomSheet(
            type = sheetType,
            onDismiss = { activeLegalSheet = null }
        )
    }

    if (showDeleteAccountSheet) {
        val waliLabel = currentWaliAccount?.let { acc ->
            "${acc.name} (@${acc.username.ifBlank { waliName }}) • ${schoolProfile.shortName}"
        } ?: "$waliName • ${schoolProfile.shortName}"

        DeleteAccountBottomSheet(
            isSchoolAdmin = false,
            accountLabel = waliLabel,
            isProcessing = isDeletingAccount,
            errorMessage = deleteAccountError,
            onConfirmDelete = { password ->
                isDeletingAccount = true
                deleteAccountError = null
                viewModel.deleteCurrentWaliAccount(password) { success, message ->
                    isDeletingAccount = false
                    if (success) {
                        showDeleteAccountSheet = false
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        onLogout()
                    } else {
                        deleteAccountError = message
                    }
                }
            },
            onDismiss = {
                if (!isDeletingAccount) {
                    showDeleteAccountSheet = false
                    deleteAccountError = null
                }
            }
        )
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
    testTag: String,
    badgeText: String? = null
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

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!badgeText.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(iconColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.OndemandVideo,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = badgeText,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconColor
                            )
                        }
                    }
                }
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

@Composable
private fun LanguageOptionCard(
    language: AppLanguage,
    description: String,
    isSelected: Boolean,
    activeTag: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) WaliPrimary.copy(alpha = 0.09f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) WaliPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) WaliPrimary else MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = language.shortBadge,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = language.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSelected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = activeTag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WaliPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = activeTag,
                        tint = WaliPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
