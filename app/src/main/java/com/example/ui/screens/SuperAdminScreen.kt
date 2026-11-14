package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ChildStudent
import com.example.data.model.SchoolProfile
import com.example.data.model.SchoolRegistration
import com.example.data.model.WaliAccount
import com.example.data.remote.pocketbase.PocketBaseSchemaProvider
import com.example.ui.HealthViewModel
import com.example.ui.dialogs.PocketBaseConfigDialog
import com.example.ui.dialogs.SuperadminSchoolDetailSheet
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.LocalAppStrings

data class SchoolReportItem(
    val schoolCode: String,
    val schoolName: String,
    val city: String,
    val address: String,
    val waliCount: Int,
    val waliNames: List<String> = emptyList(),
    val picName: String,
    val picPhone: String,
    val isPrimaryPilot: Boolean = false,
    val status: String = "AKTIF",
    val originalRegistration: SchoolRegistration? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SuperAdminScreen(
    viewModel: HealthViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appStrings = LocalAppStrings.current
    val isEn = appStrings.isEnglish
    val registrations by viewModel.schoolRegistrations.collectAsStateWithLifecycle()
    val waliAccounts by viewModel.waliAccounts.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    var selectedMainTab by remember { mutableStateOf(0) } // 0: Pengajuan Sekolah, 1: Laporan & Rekapitulasi, 2: SQL Editor

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Semua") } // "Semua", "PENDING", "APPROVED", "SUSPENDED", "REJECTED"

    var registrationToReject by remember { mutableStateOf<SchoolRegistration?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    var approvedRegistrationDialog by remember { mutableStateOf<SchoolRegistration?>(null) }
    var schoolToSuspend by remember { mutableStateOf<SchoolRegistration?>(null) }
    var schoolToDelete by remember { mutableStateOf<SchoolRegistration?>(null) }
    var selectedSchoolForDetail by remember { mutableStateOf<SchoolRegistration?>(null) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // Intercept back button
    BackHandler {
        showLogoutConfirmDialog = true
    }

    // Filter registrations
    val filteredList = registrations.filter { reg ->
        val matchQuery = searchQuery.isBlank() ||
                reg.schoolName.contains(searchQuery, ignoreCase = true) ||
                reg.city.contains(searchQuery, ignoreCase = true) ||
                reg.picName.contains(searchQuery, ignoreCase = true) ||
                reg.picPhone.contains(searchQuery, ignoreCase = true) ||
                reg.generatedSchoolCode.contains(searchQuery, ignoreCase = true)

        val normStatus = reg.status.trim().uppercase()
        val matchStatus = when (selectedStatusFilter) {
            "PENDING" -> normStatus == "PENDING"
            "APPROVED" -> normStatus == "APPROVED"
            "SUSPENDED" -> normStatus == "SUSPENDED"
            "REJECTED" -> normStatus == "REJECTED"
            else -> true
        }

        matchQuery && matchStatus
    }

    val totalCount = registrations.size
    val pendingCount = registrations.count { it.status.trim().uppercase() == "PENDING" }
    val approvedCount = registrations.count { it.status.trim().uppercase() == "APPROVED" }
    val suspendedCount = registrations.count { it.status.trim().uppercase() == "SUSPENDED" }
    val rejectedCount = registrations.count { it.status.trim().uppercase() == "REJECTED" }

    fun sendAccessCardViaWhatsApp(reg: SchoolRegistration) {
        val message = if (isEn) {
            """
                *OFFICIAL SCHOOL ADMIN LOGIN ACCESS CARD*
                _Wali Asuh Care App Platform_
                --------------------------------------------------
                Dear *${reg.picName}*,
                Congratulations! Your school registration request has been *APPROVED* by the Superadmin.
                
                🏫 *School:* ${reg.schoolName}
                🏙️ *City/Regency:* ${reg.city}
                🔑 *School ID / Code:* ${reg.generatedSchoolCode}
                
                Here are your School Administrator login credentials:
                👤 *Username:* ${reg.generatedAdminUsername}
                🔒 *Password:* ${reg.generatedAdminPassword}
                
                --------------------------------------------------
                Please open the *Wali Asuh Care* app and use the username and password above to start managing your healthcare profile, student records, and Wali Asuh accounts.
                
                _Issued by Main Superadmin of Wali Asuh Care._
            """.trimIndent()
        } else {
            """
                *KARTU AKSES LOGIN ADMIN SEKOLAH RESMI*
                _Platform Aplikasi Wali Asuh Care_
                --------------------------------------------------
                Yth. *${reg.picName}*,
                Selamat! Pengajuan pendaftaran sekolah Anda telah *DISETUJUI* oleh Superadmin.
                
                🏫 *Sekolah:* ${reg.schoolName}
                🏙️ *Kota/Kabupaten:* ${reg.city}
                🔑 *ID / Kode Sekolah:* ${reg.generatedSchoolCode}
                
                Berikut kredensial login akun Administrator Sekolah Anda:
                👤 *Username:* ${reg.generatedAdminUsername}
                🔒 *Password:* ${reg.generatedAdminPassword}
                
                --------------------------------------------------
                Silakan buka aplikasi *Wali Asuh Care* dan gunakan username serta password di atas untuk mulai mengelola profil faskes, data siswa anak asuh, dan akun wali asuh.
                
                _Diterbitkan oleh Superadmin Utama Wali Asuh Care._
            """.trimIndent()
        }

        val cleanPhone = reg.picPhone.replace("-", "").replace(" ", "").trim()
        val intlPhone = if (cleanPhone.startsWith("0")) "62" + cleanPhone.drop(1) else cleanPhone
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$intlPhone&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(
                ClipData.newPlainText(
                    if (isEn) "School Admin Access Card" else "Kartu Akses Admin Sekolah",
                    message
                )
            )
            Toast.makeText(
                context,
                if (isEn) "Access card copied to clipboard! Please send it via WhatsApp."
                else "Kartu akses disalin ke clipboard! Silakan kirimkan via WhatsApp.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun copyAdminCredentials(reg: SchoolRegistration) {
        val text = "ID: ${reg.generatedSchoolCode}\nUsername: ${reg.generatedAdminUsername}\nPassword: ${reg.generatedAdminPassword}"
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(if (isEn) "Admin Account" else "Akun Admin", text))
        Toast.makeText(
            context,
            if (isEn) "Credentials copied!" else "Kredensial berhasil disalin!",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun contactPicViaWhatsApp(picName: String, schoolName: String, picPhone: String) {
        val cleanPhone = picPhone.replace("-", "").replace(" ", "").trim()
        val intlPhone = if (cleanPhone.startsWith("0")) "62" + cleanPhone.drop(1) else cleanPhone
        val msg = if (isEn) {
            "Hello Dear $picName ($schoolName), I am contacting you from Wali Asuh Care Superadmin regarding your school's account and data management."
        } else {
            "Halo Yth. $picName ($schoolName), saya menghubungi dari Superadmin Wali Asuh Care mengenai akun dan pengelolaan data sekolah Anda."
        }
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$intlPhone&text=${Uri.encode(msg)}")
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            Toast.makeText(
                context,
                if (isEn) "Unable to open WhatsApp" else "Tidak dapat membuka WhatsApp",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_sr_care),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isEn) "Superadmin Portal" else "Portal Superadmin",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF8B5CF6))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "MASTER",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = if (isEn) "Multi-School Authorization & Registration" else "Otorisasi & Pendaftaran Multi-Sekolah",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        Surface(
                            onClick = {
                                viewModel.setAppLanguage(
                                    if (isEn) AppLanguage.INDONESIAN else AppLanguage.ENGLISH
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = WaliPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, WaliPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .widthIn(min = 48.dp)
                                .padding(vertical = 4.dp)
                                .semantics {
                                    contentDescription = if (isEn) {
                                        "Switch language to Indonesian"
                                    } else {
                                        "Ubah bahasa ke Inggris"
                                    }
                                }
                                .testTag("btn_superadmin_toggle_language")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isEn) "EN" else "ID",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WaliPrimary
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                viewModel.refreshSuperadminCloudData { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isSyncing,
                            modifier = Modifier.testTag("btn_superadmin_refresh_cloud")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = if (isEn) "Sync Data from Cloud" else "Sinkronkan Data dari Cloud",
                                    tint = WaliPrimary
                                )
                            }
                        }
                        IconButton(
                            onClick = { showLogoutConfirmDialog = true },
                            modifier = Modifier.testTag("btn_superadmin_logout")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = if (isEn) "Log Out" else "Logout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )

                TabRow(
                    selectedTabIndex = selectedMainTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = WaliPrimary
                ) {
                    Tab(
                        selected = selectedMainTab == 0,
                        onClick = { selectedMainTab = 0 },
                        text = {
                            Text(
                                text = if (isEn) "Requests (${registrations.size})" else "Pengajuan (${registrations.size})",
                                fontWeight = if (selectedMainTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("tab_superadmin_pengajuan")
                    )
                    Tab(
                        selected = selectedMainTab == 1,
                        onClick = { selectedMainTab = 1 },
                        text = {
                            Text(
                                text = if (isEn) "Reports" else "Laporan",
                                fontWeight = if (selectedMainTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.Assessment,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("tab_superadmin_laporan")
                    )
                    Tab(
                        selected = selectedMainTab == 2,
                        onClick = { selectedMainTab = 2 },
                        text = {
                            Text(
                                text = "Cloud DB",
                                fontWeight = if (selectedMainTab == 2) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.Code,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("tab_superadmin_sql")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (selectedMainTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 768.dp)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                // Ringkasan KPI Cards
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (isEn) "School Request Status" else "Status Pengajuan Sekolah",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SuperadminKpiCard(
                                title = if (isEn) "All" else "Semua",
                                count = totalCount,
                                color = WaliPrimary,
                                isSelected = selectedStatusFilter == "Semua",
                                onClick = { selectedStatusFilter = "Semua" },
                                modifier = Modifier.weight(1f)
                            )
                            SuperadminKpiCard(
                                title = if (isEn) "Pending" else "Menunggu",
                                count = pendingCount,
                                color = Color(0xFFD97706),
                                isSelected = selectedStatusFilter == "PENDING",
                                onClick = { selectedStatusFilter = "PENDING" },
                                modifier = Modifier.weight(1f)
                            )
                            SuperadminKpiCard(
                                title = if (isEn) "Approved" else "Disetujui",
                                count = approvedCount,
                                color = Color(0xFF16A34A),
                                isSelected = selectedStatusFilter == "APPROVED",
                                onClick = { selectedStatusFilter = "APPROVED" },
                                modifier = Modifier.weight(1f)
                            )
                            SuperadminKpiCard(
                                title = if (isEn) "Suspended" else "Suspend",
                                count = suspendedCount,
                                color = Color(0xFFEA580C),
                                isSelected = selectedStatusFilter == "SUSPENDED",
                                onClick = { selectedStatusFilter = "SUSPENDED" },
                                modifier = Modifier.weight(1f)
                            )
                            SuperadminKpiCard(
                                title = if (isEn) "Rejected" else "Ditolak",
                                count = rejectedCount,
                                color = Color(0xFFDC2626),
                                isSelected = selectedStatusFilter == "REJECTED",
                                onClick = { selectedStatusFilter = "REJECTED" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Search Bar & Filter Chips
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    if (isEn) "Search school name, city, PIC, WhatsApp..."
                                    else "Cari nama sekolah, kota, penanggung jawab, WA..."
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = WaliPrimary)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = if (isEn) "Clear" else "Hapus pencarian"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_superadmin_input")
                        )

                        // Filter Chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Semua", "PENDING", "APPROVED", "SUSPENDED", "REJECTED").forEach { status ->
                                val label = when (status) {
                                    "PENDING" -> if (isEn) "Pending ($pendingCount)" else "Menunggu ($pendingCount)"
                                    "APPROVED" -> if (isEn) "Approved ($approvedCount)" else "Disetujui ($approvedCount)"
                                    "SUSPENDED" -> if (isEn) "Suspended ($suspendedCount)" else "Suspend ($suspendedCount)"
                                    "REJECTED" -> if (isEn) "Rejected ($rejectedCount)" else "Ditolak ($rejectedCount)"
                                    else -> if (isEn) "All ($totalCount)" else "Semua ($totalCount)"
                                }
                                FilterChip(
                                    selected = selectedStatusFilter == status,
                                    onClick = { selectedStatusFilter = status },
                                    label = { Text(label, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }

                // Header List
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Request List (${filteredList.size})" else "Daftar Pengajuan (${filteredList.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (filteredList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = if (isEn) "No School Requests" else "Tidak Ada Pengajuan Sekolah",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEn) {
                                        "No requests match the selected filter or search."
                                    } else {
                                        "Belum ada pengajuan dengan filter atau pencarian yang dipilih."
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredList, key = { it.id }) { item ->
                        SuperadminRegistrationItemCard(
                            registration = item,
                            onApprove = {
                                viewModel.approveSchoolRegistration(item) { approved ->
                                    approvedRegistrationDialog = approved
                                    Toast.makeText(
                                        context,
                                        if (isEn) "School ${item.schoolName} approved successfully!"
                                        else "Sekolah ${item.schoolName} berhasil disetujui!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onReject = {
                                registrationToReject = item
                                rejectionReasonInput = ""
                            },
                            onSuspend = {
                                schoolToSuspend = item
                            },
                            onUnsuspend = {
                                schoolToSuspend = item
                            },
                            onDelete = {
                                schoolToDelete = item
                            },
                            onSendWaCard = {
                                sendAccessCardViaWhatsApp(item)
                            },
                            onCopyCredentials = {
                                copyAdminCredentials(item)
                            },
                            onClick = {
                                selectedSchoolForDetail = item
                            }
                        )
                    }
                }
            }
        } else if (selectedMainTab == 1) {
            SuperadminReportsView(
                schoolProfile = schoolProfile,
                registrations = registrations,
                waliAccounts = waliAccounts,
                students = students,
                onSelectSchool = { reg ->
                    selectedSchoolForDetail = reg
                },
                onDeleteRegistration = { reg ->
                    schoolToDelete = reg
                }
            )
        } else {
            SuperAdminSqlEditorView(
                viewModel = viewModel,
                isSyncing = isSyncing,
                onTestConnection = { onResult ->
                    viewModel.testCloudConnection(onResult)
                }
            )
        }
    }
}

    // DIALOG SUSPEND / UNSUSPEND SEKOLAH
    if (schoolToSuspend != null) {
        val target = schoolToSuspend!!
        val isCurrentlySuspended = target.status == "SUSPENDED"
        AlertDialog(
            onDismissRequest = { schoolToSuspend = null },
            icon = {
                Icon(
                    imageVector = if (isCurrentlySuspended) Icons.Default.PlayArrow else Icons.Default.Block,
                    contentDescription = null,
                    tint = if (isCurrentlySuspended) Color(0xFF16A34A) else Color(0xFFEA580C),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isCurrentlySuspended) {
                        if (isEn) "Unsuspend School?" else "Buka Suspend Sekolah?"
                    } else {
                        if (isEn) "Suspend School Account?" else "Suspend Akun Sekolah?"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isCurrentlySuspended) {
                            if (isEn) {
                                "Are you sure you want to reactivate access for ${target.schoolName} (${target.generatedSchoolCode})?\n\nAll admin and Wali Asuh accounts at this school will be able to log in again."
                            } else {
                                "Apakah Anda yakin ingin mengaktifkan kembali akses untuk sekolah ${target.schoolName} (${target.generatedSchoolCode})?\n\nSeluruh admin dan wali asuh pada sekolah ini akan dapat login kembali."
                            }
                        } else {
                            if (isEn) {
                                "Are you sure you want to suspend ${target.schoolName} (${target.generatedSchoolCode})?\n\nAll admin and Wali Asuh accounts at this school will not be able to log in and will receive the notice:\n\"Sorry, this account is currently suspended by the admin.\"."
                            } else {
                                "Apakah Anda yakin ingin menangguhkan (suspend) sekolah ${target.schoolName} (${target.generatedSchoolCode})?\n\nSeluruh akun admin dan wali asuh pada sekolah ini tidak akan dapat login dan akan menerima notifikasi:\n\"Maaf akun sedang disuspend oleh admin\"."
                            }
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isCurrentlySuspended) {
                            viewModel.unsuspendSchool(target) {
                                schoolToSuspend = null
                                Toast.makeText(
                                    context,
                                    if (isEn) "School ${target.schoolName} reactivated successfully."
                                    else "Sekolah ${target.schoolName} berhasil diaktifkan kembali.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            viewModel.suspendSchool(target) {
                                schoolToSuspend = null
                                Toast.makeText(
                                    context,
                                    if (isEn) "School ${target.schoolName} suspended successfully."
                                    else "Sekolah ${target.schoolName} berhasil disuspend.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentlySuspended) Color(0xFF16A34A) else Color(0xFFEA580C)
                    )
                ) {
                    Text(
                        if (isCurrentlySuspended) {
                            if (isEn) "Unsuspend" else "Buka Suspend"
                        } else {
                            if (isEn) "Yes, Suspend School" else "Ya, Suspend Sekolah"
                        }
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { schoolToSuspend = null }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    // DIALOG HAPUS SEKOLAH
    if (schoolToDelete != null) {
        val target = schoolToDelete!!
        AlertDialog(
            onDismissRequest = { schoolToDelete = null },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    if (isEn) "Delete School Permanently?" else "Hapus Sekolah Permanen?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isEn) {
                            "Are you sure you want to delete school data for ${target.schoolName}?"
                        } else {
                            "Apakah Anda yakin ingin menghapus data sekolah ${target.schoolName}?"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isEn) {
                            "This action is permanent. All registration data, as well as students and Wali Asuh accounts linked to school ID (${target.generatedSchoolCode.ifBlank { "New" }}) will be deleted from the system."
                        } else {
                            "Tindakan ini permanen. Seluruh data pendaftaran, serta data siswa dan akun wali asuh yang terikat dengan ID sekolah (${target.generatedSchoolCode.ifBlank { "Baru" }}) akan dihapus dari sistem."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSchoolRegistration(target) {
                            schoolToDelete = null
                            Toast.makeText(
                                context,
                                if (isEn) "School ${target.schoolName} permanently deleted."
                                else "Sekolah ${target.schoolName} berhasil dihapus permanen.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isEn) "Delete Permanently" else "Hapus Permanen")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { schoolToDelete = null }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    // DIALOG TOLAK PENGAJUAN
    if (registrationToReject != null) {
        val target = registrationToReject!!
        AlertDialog(
            onDismissRequest = { registrationToReject = null },
            icon = { Icon(Icons.Default.Clear, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(if (isEn) "Reject School Request" else "Tolak Pengajuan Sekolah") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isEn) {
                            "Are you sure you want to reject the request from ${target.schoolName}?"
                        } else {
                            "Apakah Anda yakin ingin menolak pengajuan dari ${target.schoolName}?"
                        },
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        label = { Text(if (isEn) "Rejection Reason" else "Alasan Penolakan") },
                        placeholder = {
                            Text(
                                if (isEn) "Example: Inactive contact number / duplicate data"
                                else "Contoh: Berkas nomor kontak tidak aktif / duplikasi data"
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectSchoolRegistration(target, rejectionReasonInput) {
                            registrationToReject = null
                            Toast.makeText(
                                context,
                                if (isEn) "School request has been rejected." else "Pengajuan sekolah telah ditolak.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isEn) "Reject Request" else "Tolak Pengajuan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { registrationToReject = null }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    // DIALOG KARTU AKSES BERHASIL SETELAH DISETUJUI
    if (approvedRegistrationDialog != null) {
        val appReg = approvedRegistrationDialog!!
        AlertDialog(
            onDismissRequest = { approvedRegistrationDialog = null },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (isEn) "School Approved Successfully!" else "Sekolah Berhasil Disetujui!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isEn) {
                            "The School Administrator account and official Access Card have been automatically issued:"
                        } else {
                            "Akun Administrator Sekolah dan Kartu Akses resmi telah otomatis diterbitkan:"
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Kartu Akses Preview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = appReg.schoolName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn) "School Code: ${appReg.generatedSchoolCode}" else "Kode Sekolah: ${appReg.generatedSchoolCode}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WaliPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Username: ${appReg.generatedAdminUsername}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Password: ${appReg.generatedAdminPassword}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                    }

                    Text(
                        text = if (isEn) {
                            "Send this access card to the person in charge via WhatsApp:"
                        } else {
                            "Kirimkan kartu akses ini kepada penanggung jawab via WhatsApp:"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        sendAccessCardViaWhatsApp(appReg)
                        approvedRegistrationDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isEn) "Send to PIC WhatsApp" else "Kirim ke WhatsApp PJ")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { approvedRegistrationDialog = null }) {
                    Text(if (isEn) "Done" else "Selesai")
                }
            }
        )
    }

    // DIALOG KONFIRMASI LOGOUT
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
            title = { Text(if (isEn) "Exit Superadmin Portal?" else "Keluar dari Portal Superadmin?") },
            text = {
                Text(
                    if (isEn) "You will be redirected back to the app login screen."
                    else "Anda akan diarahkan kembali ke halaman login aplikasi."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isEn) "Log Out" else "Keluar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    // BOTTOM SHEET DETAIL SEKOLAH LENGKAP (SLIDE DARI BAWAH)
    if (selectedSchoolForDetail != null) {
        val target = selectedSchoolForDetail!!
        SuperadminSchoolDetailSheet(
            registration = target,
            students = students,
            waliAccounts = waliAccounts,
            onCopyCredentials = { copyAdminCredentials(target) },
            onSendWaCard = { sendAccessCardViaWhatsApp(target) },
            onContactWa = { contactPicViaWhatsApp(target.picName, target.schoolName, target.picPhone) },
            onDelete = {
                selectedSchoolForDetail = null
                schoolToDelete = target
            },
            onDismiss = { selectedSchoolForDetail = null }
        )
    }
}

@Composable
fun SuperadminKpiCard(
    title: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, color, RoundedCornerShape(12.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardElevation().let {
            CardDefaults.cardColors(containerColor = if (isSelected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SuperadminRegistrationItemCard(
    registration: SchoolRegistration,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onSuspend: () -> Unit,
    onUnsuspend: () -> Unit,
    onDelete: () -> Unit,
    onSendWaCard: () -> Unit,
    onCopyCredentials: () -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val appStrings = LocalAppStrings.current
    val isEn = appStrings.isEnglish
    var passwordVisible by remember { mutableStateOf(false) }
    val normalizedStatus = registration.status.trim().uppercase()

    val statusColor = when (normalizedStatus) {
        "APPROVED" -> Color(0xFF16A34A)
        "SUSPENDED" -> Color(0xFFEA580C)
        "REJECTED" -> Color(0xFFDC2626)
        else -> Color(0xFFD97706)
    }

    val statusLabel = when (normalizedStatus) {
        "APPROVED" -> if (isEn) "APPROVED" else "DISETUJUI"
        "SUSPENDED" -> if (isEn) "SUSPENDED" else "DITANGGUHKAN / SUSPENDED"
        "REJECTED" -> if (isEn) "REJECTED" else "DITOLAK"
        else -> if (isEn) "PENDING APPROVAL" else "MENUNGGU PERSETUJUAN"
    }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("school_reg_item_${registration.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardElevation().let {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row Atas: Nama Sekolah & Badge Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = registration.schoolName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${registration.city} • ${DateUtils.formatFullDate(registration.submissionDate, appStrings.language)}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info Alamat & PIC
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = WaliPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = registration.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = WaliPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEn) "PIC: ${registration.picName} (${registration.picPhone})" else "PJ: ${registration.picName} (${registration.picPhone})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = WaliPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = registration.picEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = WaliPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEn) "Estimated ${registration.estimatedWaliCount} Wali Asuh" else "Estimasi ${registration.estimatedWaliCount} Wali Asuh",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // TAMPILAN JIKA STATUS APPROVED: KARTU AKSES DIGITAL + TOMBOL SUSPEND & HAPUS
            if (normalizedStatus == "APPROVED") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(1.5.dp, Color(0xFF86EFAC), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "OFFICIAL ADMIN ACCESS CARD" else "KARTU AKSES ADMIN RESMI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF15803D)
                                )
                            }
                            Text(
                                text = registration.generatedSchoolCode,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF15803D)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Username:", fontSize = 12.sp, color = Color(0xFF166534))
                            Text(
                                text = registration.generatedAdminUsername,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Password:", fontSize = 12.sp, color = Color(0xFF166534))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (passwordVisible) registration.generatedAdminPassword else "••••••••",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isEn) "Toggle password visibility" else "Lihat atau sembunyikan password",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onCopyCredentials,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEn) "Copy" else "Salin", fontSize = 11.sp)
                            }

                            Button(
                                onClick = onSendWaCard,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEn) "Send to PIC WA" else "Kirim ke WA PJ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Row Tombol Suspend & Hapus untuk Sekolah yang Disetujui
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSuspend,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("btn_suspend_school_${registration.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEA580C)),
                        border = BorderStroke(1.dp, Color(0xFFEA580C)),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Suspend School" else "Suspend Sekolah", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("btn_delete_school_${registration.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Delete School" else "Hapus Sekolah", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // TAMPILAN JIKA STATUS SUSPENDED: BANNER & TOMBOL BUKA SUSPEND / HAPUS
            if (normalizedStatus == "SUSPENDED") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF7ED))
                        .border(1.5.dp, Color(0xFFFDBA74), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "THIS SCHOOL ACCOUNT IS SUSPENDED" else "AKUN SEKOLAH INI SEDANG DISUSPEND",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color(0xFF9A3412)
                            )
                        }

                        Text(
                            text = if (isEn) {
                                "All users (Admin and Wali Asuh) under ID ${registration.generatedSchoolCode} are blocked from logging in and will receive the notice:\n\"Sorry, this account is currently suspended by the admin.\"."
                            } else {
                                "Seluruh user (Admin dan Wali Asuh) pada ID ${registration.generatedSchoolCode} diblokir dari login dan akan menerima notifikasi:\n\"Maaf akun sedang disuspend oleh admin\"."
                            },
                            fontSize = 11.5.sp,
                            color = Color(0xFF7C2D12),
                            lineHeight = 16.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onUnsuspend,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(38.dp)
                                    .testTag("btn_unsuspend_school_${registration.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEn) "Unsuspend" else "Buka Suspend", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("btn_delete_school_${registration.id}"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEn) "Delete" else "Hapus", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // TAMPILAN JIKA STATUS REJECTED: ALASAN PENOLAKAN + TOMBOL HAPUS
            if (normalizedStatus == "REJECTED") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isEn) "Rejection Reason:" else "Alasan Penolakan:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFB91C1C)
                        )
                        Text(
                            text = registration.rejectionReason.ifBlank {
                                if (isEn) "Incomplete data." else "Data belum lengkap."
                            },
                            fontSize = 11.5.sp,
                            color = Color(0xFF7F1D1D)
                        )
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEn) "Delete This Request" else "Hapus Pengajuan Ini", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // AKSI BUTTONS UNTUK STATUS PENDING ATAU STATUS LAINNYA
            if (normalizedStatus == "PENDING") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(42.dp)
                            .testTag("btn_delete_pending_${registration.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isEn) "Delete" else "Hapus", fontSize = 11.5.sp)
                    }

                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEA580C)),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isEn) "Reject" else "Tolak", fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = onApprove,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(42.dp)
                            .testTag("btn_approve_school_${registration.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Approve" else "Setujui", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (normalizedStatus !in setOf("APPROVED", "SUSPENDED", "REJECTED")) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("btn_delete_school_${registration.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isEn) "Delete School" else "Hapus Sekolah", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuperadminReportsView(
    schoolProfile: SchoolProfile,
    registrations: List<SchoolRegistration>,
    waliAccounts: List<WaliAccount>,
    students: List<ChildStudent>,
    onSelectSchool: (SchoolRegistration) -> Unit = {},
    onDeleteRegistration: (SchoolRegistration) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appStrings = LocalAppStrings.current
    val isEn = appStrings.isEnglish
    var reportSearchQuery by remember { mutableStateOf("") }

    // Seluruh sekolah tenant resmi yang telah disetujui / aktif (memakai ID Sekolah resmi)
    val approvedSchools = registrations
        .filter {
            val st = it.status.trim().uppercase()
            st == "APPROVED" || st == "SUSPENDED"
        }
        .map { reg ->
            val normStatus = reg.status.trim().uppercase()
            val schoolWalies = waliAccounts.filter { it.schoolId == reg.generatedSchoolCode }
            val effectiveWaliCount = if (schoolWalies.isNotEmpty()) schoolWalies.size else reg.estimatedWaliCount
            val waliNames = schoolWalies.map { "${it.name} (${it.dormitory})" }

            SchoolReportItem(
                schoolCode = reg.generatedSchoolCode,
                schoolName = reg.schoolName,
                city = reg.city,
                address = reg.address,
                waliCount = effectiveWaliCount,
                waliNames = waliNames,
                picName = reg.picName,
                picPhone = reg.picPhone,
                isPrimaryPilot = false,
                status = if (normStatus == "SUSPENDED") {
                    if (isEn) "Suspended" else "Disuspend"
                } else {
                    if (isEn) "Registered & Active" else "Terdaftar & Aktif"
                },
                originalRegistration = reg
            )
        }

    val allSchools = approvedSchools
    val totalActiveSchools = allSchools.size
    val totalWaliAccounts = allSchools.sumOf { it.waliCount }
    val avgWaliPerSchool = if (totalActiveSchools > 0) {
        String.format("%.1f", totalWaliAccounts.toDouble() / totalActiveSchools)
    } else "0"
    val totalStudentsCount = students.count { s -> approvedSchools.any { it.schoolCode == s.schoolId } }
    val pendingRegistrationsCount = registrations.count { it.status.trim().uppercase() == "PENDING" }

    // Filter pencarian
    val filteredSchools = allSchools.filter { item ->
        reportSearchQuery.isBlank() ||
                item.schoolName.contains(reportSearchQuery, ignoreCase = true) ||
                item.city.contains(reportSearchQuery, ignoreCase = true) ||
                item.schoolCode.contains(reportSearchQuery, ignoreCase = true) ||
                item.picName.contains(reportSearchQuery, ignoreCase = true)
    }

    fun generateReportSummaryText(): String {
        val dateFormatted = DateUtils.formatFullDate(System.currentTimeMillis(), appStrings.language)
        val sb = StringBuilder()
        if (isEn) {
            sb.appendLine("📊 *OFFICIAL SCHOOL & WALI ASUH SUMMARY REPORT*")
            sb.appendLine("_Wali Asuh Care Superadmin Portal_")
            sb.appendLine("📅 Date: $dateFormatted")
            sb.appendLine("--------------------------------------------------")
            sb.appendLine("🏫 *Total Active Schools:* $totalActiveSchools Schools")
            sb.appendLine("👥 *Total Wali Asuh Users:* $totalWaliAccounts Wali Asuh")
            sb.appendLine("📈 *Avg. Wali Asuh/School:* $avgWaliPerSchool Wali")
            sb.appendLine("🧑‍🎓 *Total Foster Students:* $totalStudentsCount Students")
            if (pendingRegistrationsCount > 0) {
                sb.appendLine("⏳ *Pending Approval Requests:* $pendingRegistrationsCount Schools")
            }
            sb.appendLine("--------------------------------------------------")
            sb.appendLine("*BREAKDOWN BY SCHOOL:*")
            allSchools.forEachIndexed { idx, sc ->
                sb.appendLine("")
                sb.appendLine("${idx + 1}. *${sc.schoolName}* [${sc.schoolCode}]")
                sb.appendLine("   📍 Region: ${sc.city}")
                sb.appendLine("   👥 Wali Asuh Count: *${sc.waliCount} Users*")
                sb.appendLine("   👤 Person in Charge: ${sc.picName} (${sc.picPhone})")
                if (sc.waliNames.isNotEmpty()) {
                    sb.appendLine("   📋 Registered Wali Asuh Accounts: ${sc.waliNames.joinToString(", ")}")
                }
            }
            sb.appendLine("")
            sb.appendLine("--------------------------------------------------")
            sb.appendLine("_Issued by Wali Asuh Care Superadmin_")
        } else {
            sb.appendLine("📊 *REKAPITULASI RESMI SEKOLAH & WALI ASUH*")
            sb.appendLine("_Portal Superadmin Wali Asuh Care_")
            sb.appendLine("📅 Tanggal: $dateFormatted")
            sb.appendLine("--------------------------------------------------")
            sb.appendLine("🏫 *Total Sekolah Aktif:* $totalActiveSchools Sekolah")
            sb.appendLine("👥 *Total User Wali Asuh:* $totalWaliAccounts Wali Asuh")
            sb.appendLine("📈 *Rata-rata Wali/Sekolah:* $avgWaliPerSchool Wali")
            sb.appendLine("🧑‍🎓 *Total Siswa Asuh:* $totalStudentsCount Siswa")
            if (pendingRegistrationsCount > 0) {
                sb.appendLine("⏳ *Pengajuan Menunggu Persetujuan:* $pendingRegistrationsCount Sekolah")
            }
            sb.appendLine("--------------------------------------------------")
            sb.appendLine("*RINCIAN PER SEKOLAH:*")
            allSchools.forEachIndexed { idx, sc ->
                sb.appendLine("")
                sb.appendLine("${idx + 1}. *${sc.schoolName}* [${sc.schoolCode}]")
                sb.appendLine("   📍 Wilayah: ${sc.city}")
                sb.appendLine("   👥 Jumlah Wali Asuh: *${sc.waliCount} Orang*")
                sb.appendLine("   👤 Penanggung Jawab: ${sc.picName} (${sc.picPhone})")
                if (sc.waliNames.isNotEmpty()) {
                    sb.appendLine("   📋 Akun Wali Terdaftar: ${sc.waliNames.joinToString(", ")}")
                }
            }
            sb.appendLine("")
            sb.appendLine("--------------------------------------------------")
            sb.appendLine("_Diterbitkan oleh Superadmin Aplikasi Wali Asuh Care_")
        }
        return sb.toString().trimIndent()
    }

    fun shareViaWhatsApp() {
        val text = generateReportSummaryText()
        val uri = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(text)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            try {
                context.startActivity(
                    Intent.createChooser(
                        shareIntent,
                        if (isEn) "Share Summary" else "Bagikan Rekapitulasi"
                    )
                )
            } catch (e2: Exception) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        if (isEn) "School & Wali Asuh Summary" else "Rekapitulasi Sekolah & Wali Asuh",
                        text
                    )
                )
                Toast.makeText(
                    context,
                    if (isEn) "Report copied to clipboard!" else "Laporan disalin ke clipboard!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun copyToClipboard() {
        val text = generateReportSummaryText()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(
                if (isEn) "School & Wali Asuh Summary" else "Rekapitulasi Sekolah & Wali Asuh",
                text
            )
        )
        Toast.makeText(
            context,
            if (isEn) "Summary copied to clipboard!" else "Rekapitulasi berhasil disalin ke clipboard!",
            Toast.LENGTH_SHORT
        ).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 768.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Kartu Ringkasan Eksekutif
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardElevation().let {
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(WaliPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = WaliPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isEn) "Multi-School Summary" else "Rekapitulasi Multi-Sekolah",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEn) {
                                        "Tenant Quota & Wali Asuh Account Monitoring"
                                    } else {
                                        "Monitoring Kuota & Akun Wali Asuh Seluruh Tenant"
                                    },
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 4 KPI Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportStatBox(
                            title = if (isEn) "Total Schools" else "Total Sekolah",
                            value = "$totalActiveSchools",
                            subtitle = if (isEn) "Active Schools" else "Sekolah Aktif",
                            color = WaliPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatBox(
                            title = if (isEn) "Total Wali Asuh" else "Total Wali Asuh",
                            value = "$totalWaliAccounts",
                            subtitle = if (isEn) "Caregiver Accounts" else "Akun Pengasuh",
                            color = Color(0xFF16A34A),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportStatBox(
                            title = if (isEn) "Avg. Wali Asuh" else "Rata-rata Wali",
                            value = avgWaliPerSchool,
                            subtitle = if (isEn) "per School" else "per Sekolah",
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatBox(
                            title = if (isEn) "Total Students" else "Total Siswa",
                            value = "$totalStudentsCount",
                            subtitle = if (isEn) "Foster Students" else "Anak Asuh",
                            color = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Action Buttons Row: WhatsApp & Copy
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { shareViaWhatsApp() },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(42.dp)
                                .testTag("btn_share_report_wa"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isEn) "Share to WhatsApp" else "Bagikan ke WhatsApp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { copyToClipboard() },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("btn_copy_report"),
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEn) "Copy Summary" else "Salin Rekap", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Search Input
        item {
            OutlinedTextField(
                value = reportSearchQuery,
                onValueChange = { reportSearchQuery = it },
                placeholder = {
                    Text(
                        if (isEn) "Search school name, city, code, PIC name..."
                        else "Cari nama sekolah, kota, kode, nama PJ..."
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = WaliPrimary)
                },
                trailingIcon = {
                    if (reportSearchQuery.isNotBlank()) {
                        IconButton(onClick = { reportSearchQuery = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = if (isEn) "Clear" else "Hapus pencarian"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_report_input")
            )
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEn) {
                        "School Details (${filteredSchools.size})"
                    } else {
                        "Rincian Masing-Masing Sekolah (${filteredSchools.size})"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (filteredSchools.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (isEn) "No Schools Found" else "Tidak Ada Sekolah Ditemukan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEn) {
                                "Your search keyword did not match any school."
                            } else {
                                "Kata kunci pencarian tidak cocok dengan sekolah manapun."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(
                filteredSchools,
                key = { "${it.schoolCode}_${it.originalRegistration?.id ?: 0L}" }
            ) { schoolItem ->
                SuperadminReportSchoolCard(
                    school = schoolItem,
                    onContactWa = {
                        val cleanPhone = schoolItem.picPhone.replace("-", "").replace(" ", "").trim()
                        val intlPhone = if (cleanPhone.startsWith("0")) "62" + cleanPhone.drop(1) else cleanPhone
                        val msg = if (isEn) {
                            "Hello Dear ${schoolItem.picName} (${schoolItem.schoolName}), I am contacting you from Wali Asuh Care Superadmin regarding your school's Wali Asuh accounts and quota."
                        } else {
                            "Halo Yth. ${schoolItem.picName} (${schoolItem.schoolName}), saya menghubungi dari Superadmin Wali Asuh Care mengenai akun dan kuota wali asuh sekolah Anda."
                        }
                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$intlPhone&text=${Uri.encode(msg)}")
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                if (isEn) "Unable to open WhatsApp" else "Tidak dapat membuka WhatsApp",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onCopySchoolInfo = {
                        val text = if (isEn) {
                            """
                                School: ${schoolItem.schoolName} [${schoolItem.schoolCode}]
                                Region: ${schoolItem.city}
                                Address: ${schoolItem.address}
                                Wali Asuh Count: ${schoolItem.waliCount} Users
                                PIC: ${schoolItem.picName} (${schoolItem.picPhone})
                            """.trimIndent()
                        } else {
                            """
                                Sekolah: ${schoolItem.schoolName} [${schoolItem.schoolCode}]
                                Wilayah: ${schoolItem.city}
                                Alamat: ${schoolItem.address}
                                Jumlah Wali Asuh: ${schoolItem.waliCount} Orang
                                PJ: ${schoolItem.picName} (${schoolItem.picPhone})
                            """.trimIndent()
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText(
                                if (isEn) "School Info" else "Info Sekolah",
                                text
                            )
                        )
                        Toast.makeText(
                            context,
                            if (isEn) "${schoolItem.schoolName} info copied!" else "Info ${schoolItem.schoolName} disalin!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onDeleteSchool = schoolItem.originalRegistration?.let { reg ->
                        { onDeleteRegistration(reg) }
                    },
                    onClick = {
                        schoolItem.originalRegistration?.let { reg ->
                            onSelectSchool(reg)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ReportStatBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = color.copy(alpha = 0.85f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuperadminReportSchoolCard(
    school: SchoolReportItem,
    onContactWa: () -> Unit,
    onCopySchoolInfo: () -> Unit,
    onDeleteSchool: (() -> Unit)? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("report_school_${school.schoolCode}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardElevation().let {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Nama Sekolah, Kode Sekolah & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = school.schoolName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WaliPrimary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = school.schoolCode,
                                color = WaliPrimary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = school.city,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val badgeColor = when {
                    school.isPrimaryPilot -> Color(0xFF8B5CF6)
                    school.status == "Disuspend" || school.status == "Suspended" -> Color(0xFFEA580C)
                    else -> Color(0xFF16A34A)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.12f))
                        .border(
                            1.dp,
                            badgeColor.copy(alpha = 0.4f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = school.status,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // HIGHLIGHT BOX: REKAP JUMLAH USER WALI ASUH
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0FDF4))
                    .border(1.5.dp, Color(0xFF86EFAC), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "Wali Asuh Account Users" else "Pengguna Akun Wali Asuh",
                                fontSize = 11.sp,
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (isEn) "${school.waliCount} Wali Asuh Users" else "${school.waliCount} User Wali Asuh",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF16A34A))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isEn) "${school.waliCount} USERS" else "${school.waliCount} USER",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Informasi Alamat & Penanggung Jawab
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = WaliPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = school.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = WaliPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEn) {
                            "PIC: ${school.picName} (${school.picPhone})"
                        } else {
                            "Penanggung Jawab: ${school.picName} (${school.picPhone})"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Tampilkan daftar nama akun wali asuh jika tersedia (default tersembunyi, bisa dibuka-tutup)
            if (school.waliNames.isNotEmpty()) {
                var isWaliListExpanded by remember(school.schoolCode) { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isWaliListExpanded = !isWaliListExpanded }
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                            .testTag("toggle_wali_list_${school.schoolCode}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) {
                                "Active Wali Asuh Users (${school.waliNames.size})"
                            } else {
                                "Daftar User Wali Asuh Aktif (${school.waliNames.size})"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = if (isWaliListExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isWaliListExpanded) {
                                if (isEn) "Hide Wali Asuh list" else "Sembunyikan daftar wali asuh"
                            } else {
                                if (isEn) "Show Wali Asuh list" else "Tampilkan daftar wali asuh"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    AnimatedVisibility(visible = isWaliListExpanded) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            school.waliNames.forEach { waliName ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = waliName,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopySchoolInfo,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isEn) "Copy Info" else "Salin Info", fontSize = 11.sp)
                }

                Button(
                    onClick = onContactWa,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isEn) "Contact PIC" else "Hubungi PJ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (onDeleteSchool != null) {
                    OutlinedButton(
                        onClick = onDeleteSchool,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isEn) "Delete" else "Hapus", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SuperAdminSqlEditorView(
    viewModel: HealthViewModel,
    isSyncing: Boolean,
    onTestConnection: (onResult: (Boolean, String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    var testResultMsg by remember { mutableStateOf<String?>(null) }
    var testResultSuccess by remember { mutableStateOf(true) }
    var showCloudConfigDialog by remember { mutableStateOf(false) }
    var selectedCodeMode by remember { mutableStateOf(0) } // 0 = Skrip SSH VPS (Auto Update), 1 = JSON Import PocketBase
    var expandedCollectionName by remember { mutableStateOf<String?>(null) }

    val activeSchemaCode = remember(selectedCodeMode) {
        if (selectedCodeMode == 0) {
            PocketBaseSchemaProvider.POCKETBASE_SSH_SYNC_SCRIPT
        } else {
            PocketBaseSchemaProvider.POCKETBASE_COLLECTIONS_JSON
        }
    }
    val activeCodeLabel = if (selectedCodeMode == 0) {
        if (isEn) "Table Update SSH Script" else "Skrip SSH Update Tabel"
    } else {
        if (isEn) "PocketBase Import JSON" else "JSON Import PocketBase"
    }

    fun copyActiveCode() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(activeCodeLabel, activeSchemaCode))
        Toast.makeText(
            context,
            if (isEn) "$activeCodeLabel copied to clipboard!" else "$activeCodeLabel berhasil disalin ke clipboard!",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun shareActiveCode() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$activeCodeLabel - Wali Asuh Care (${PocketBaseSchemaProvider.SCHEMA_VERSION})")
            putExtra(Intent.EXTRA_TEXT, activeSchemaCode)
        }
        try {
            context.startActivity(
                Intent.createChooser(
                    intent,
                    if (isEn) "Share $activeCodeLabel" else "Bagikan $activeCodeLabel"
                )
            )
        } catch (_: Exception) {
            copyActiveCode()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 768.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(WaliPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = if (isEn) "PocketBase Database Schema" else "Skema Database PocketBase",
                                tint = WaliPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Latest PocketBase Table Schema" else "Kode Tabel PocketBase Terbaru",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn) {
                                    "Version: ${PocketBaseSchemaProvider.SCHEMA_VERSION}"
                                } else {
                                    "Versi: ${PocketBaseSchemaProvider.SCHEMA_VERSION}"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WaliPrimary
                            )
                        }
                    }

                    Text(
                        text = if (isEn) {
                            "This schema code is automatically updated with all app features & columns (${com.example.data.remote.pocketbase.PocketBaseConfig.EMBEDDED_URL}). Safe to re-run on VPS SSH (adds new tables/columns without deleting old data) or import via Settings → Import collections."
                        } else {
                            "Kode skema ini selalu diperbarui otomatis mengikuti seluruh fitur & kolom aplikasi (${com.example.data.remote.pocketbase.PocketBaseConfig.EMBEDDED_URL}). Aman dijalankan ulang di SSH VPS (menambah tabel/kolom baru tanpa menghapus data lama) atau diimpor via Settings → Import collections."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCodeMode == 0,
                            onClick = { selectedCodeMode = 0 },
                            label = {
                                Text(
                                    if (isEn) "VPS SSH Script (Auto Update)" else "Skrip SSH VPS (Auto Update)",
                                    fontSize = 11.5.sp
                                )
                            },
                            modifier = Modifier.testTag("chip_pb_mode_ssh")
                        )
                        FilterChip(
                            selected = selectedCodeMode == 1,
                            onClick = { selectedCodeMode = 1 },
                            label = { Text("JSON Import", fontSize = 11.5.sp) },
                            modifier = Modifier.testTag("chip_pb_mode_json")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { copyActiveCode() },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("btn_copy_sql_superadmin"),
                            colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = if (isEn) "Copy Table Code" else "Salin Kode Tabel",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedCodeMode == 0) {
                                    if (isEn) "Copy Full SSH Script" else "Salin Skrip SSH Lengkap"
                                } else {
                                    if (isEn) "Copy Collection JSON" else "Salin JSON Koleksi"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { shareActiveCode() },
                            modifier = Modifier
                                .weight(0.8f)
                                .height(48.dp)
                                .testTag("btn_share_sql_superadmin"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = if (isEn) "Share Code" else "Bagikan Kode",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEn) "Share" else "Bagikan", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            onTestConnection { success, msg ->
                                testResultSuccess = success
                                testResultMsg = msg
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = !isSyncing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_test_pocketbase_superadmin"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isEn) "Testing PocketBase Connection..." else "Menguji Koneksi PocketBase...", fontSize = 12.sp)
                        } else {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = if (isEn) "Test PocketBase Connection" else "Uji Koneksi PocketBase",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF16A34A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isEn) "Test PocketBase Connection" else "Uji Koneksi PocketBase",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showCloudConfigDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_open_pocketbase_sync_superadmin"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = if (isEn) "Manage Sync & PocketBase Server" else "Kelola Sinkronisasi & Server PocketBase",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF16A34A)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isEn) "Manage Sync & PocketBase Server" else "Kelola Sinkronisasi & Server PocketBase",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (testResultMsg != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (testResultSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = testResultMsg ?: "",
                                fontSize = 11.5.sp,
                                color = if (testResultSuccess) Color(0xFF166534) else Color(0xFF991B1B),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = if (isEn) {
                    "Collections & Columns (${PocketBaseSchemaProvider.COLLECTIONS.size} Tables — Tap to view fields):"
                } else {
                    "Daftar Koleksi & Kolom (${PocketBaseSchemaProvider.COLLECTIONS.size} Tabel — Ketuk untuk lihat field):"
                },
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(PocketBaseSchemaProvider.COLLECTIONS, key = { it.collectionName }) { collectionMeta ->
            val isExpanded = expandedCollectionName == collectionMeta.collectionName
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedCollectionName = if (isExpanded) null else collectionMeta.collectionName
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = if (isEn) "Active" else "Aktif",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = collectionMeta.collectionName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${collectionMeta.description} (${collectionMeta.fields.size} ${if (isEn) "fields" else "field"})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WaliPrimary.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = collectionMeta.multiTenantField,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = WaliPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) {
                                    if (isEn) "Hide column details" else "Tutup detail kolom"
                                } else {
                                    if (isEn) "View column details" else "Lihat detail kolom"
                                },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "id (text • 15 char primary key)",
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF93C5FD)
                            )
                            collectionMeta.fields.forEach { field ->
                                Text(
                                    text = "• ${field.name} (${field.type})",
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (selectedCodeMode == 0) {
                        if (isEn) {
                            "PocketBase Table Update Script Code (Copy-Paste to VPS SSH):"
                        } else {
                            "Kode Skrip Update Tabel PocketBase (Copy-Paste ke SSH VPS):"
                        }
                    } else {
                        if (isEn) {
                            "PocketBase Collection Import JSON (Settings → Import collections):"
                        } else {
                            "Kode JSON Import Koleksi PocketBase (Settings → Import collections):"
                        }
                    },
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    SelectionContainer {
                        Text(
                            text = activeSchemaCode,
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (showCloudConfigDialog) {
        PocketBaseConfigDialog(
            viewModel = viewModel,
            onDismiss = { showCloudConfigDialog = false }
        )
    }
}
