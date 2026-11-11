package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WaliAccount
import com.example.ui.HealthViewModel
import com.example.ui.dialogs.AddWaliAccountDialog
import com.example.ui.dialogs.EditWaliAccountDialog
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import com.example.util.PasswordGenerator

import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.School
import com.example.ui.dialogs.EditSchoolProfileDialog
import com.example.ui.dialogs.ImportStudentsCsvDialog
import com.example.ui.dialogs.ImportWaliCsvDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: HealthViewModel,
    onNavigateToApp: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val waliAccounts by viewModel.waliAccounts.collectAsStateWithLifecycle()
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()

    var showEditSchoolDialog by remember { mutableStateOf(false) }
    var showImportStudentsDialog by remember { mutableStateOf(false) }
    var showImportWaliDialog by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<WaliAccount?>(null) }
    var accountToDelete by remember { mutableStateOf<WaliAccount?>(null) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterDormitory by remember { mutableStateOf("Semua") }

    // Tangani tombol Back fisik/gesture pada perangkat Android
    BackHandler {
        showExitConfirmDialog = true
    }

    val filteredAccounts = waliAccounts.filter { account ->
        val effUser = account.username.ifBlank {
            account.name.trim().lowercase().replace(" ", "").filter { it.isLetterOrDigit() }
        }
        val matchSearch = searchQuery.isBlank() ||
                account.name.contains(searchQuery, ignoreCase = true) ||
                effUser.contains(searchQuery, ignoreCase = true) ||
                account.phone.contains(searchQuery, ignoreCase = true)
        val matchDorm = if (selectedFilterDormitory == "Semua") true else account.dormitory.contains(selectedFilterDormitory, ignoreCase = true)
        matchSearch && matchDorm
    }

    val putraCount = waliAccounts.count { it.dormitory.contains("Putra", ignoreCase = true) }
    val putriCount = waliAccounts.count { it.dormitory.contains("Putri", ignoreCase = true) }

    fun shareAllAccountsSummary() {
        if (waliAccounts.isEmpty()) {
            Toast.makeText(context, "Belum ada akun wali asuh untuk dibagikan.", Toast.LENGTH_SHORT).show()
            return
        }
        val sb = StringBuilder()
        sb.appendLine("*DAFTAR KARTU AKSES WALI ASUH*")
        sb.appendLine("_${schoolProfile.schoolName}_")
        sb.appendLine("Tanggal: ${DateUtils.formatFullDate(System.currentTimeMillis())}")
        sb.appendLine("Total: ${waliAccounts.size} Akun Terdaftar")
        sb.appendLine("----------------------------------------")
        waliAccounts.forEachIndexed { index, acc ->
            val effectiveUser = acc.username.ifBlank {
                acc.name.trim().lowercase().replace(" ", "").filter { it.isLetterOrDigit() }
            }
            sb.appendLine("${index + 1}. *${acc.name}* (${acc.dormitory})")
            sb.appendLine("   • Username: $effectiveUser")
            sb.appendLine("   • Password: ${acc.password}")
            if (acc.phone.isNotBlank()) {
                sb.appendLine("   • WA: ${acc.phone}")
            }
            sb.appendLine("")
        }
        sb.appendLine("_Dikelola oleh Administrator Utama (${schoolProfile.adminName})_")
        val summaryText = sb.toString().trim()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summaryText)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Bagikan Rekap Akun Wali Asuh"))
        } catch (e: Exception) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Rekap Akun Wali Asuh", summaryText))
            Toast.makeText(context, "Rekap akun disalin ke clipboard!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Portal Administrator",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${schoolProfile.shortName} • ${schoolProfile.city}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    // Tombol Bagikan Rekap Seluruh Akun
                    IconButton(
                        onClick = { shareAllAccountsSummary() },
                        modifier = Modifier.testTag("btn_admin_share_all")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan Rekap Seluruh Akun",
                            tint = Color.White
                        )
                    }
                    // Tombol Buka Dashboard Aplikasi
                    IconButton(
                        onClick = onNavigateToApp,
                        modifier = Modifier.testTag("btn_admin_to_app")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Buka Dashboard Aplikasi",
                            tint = Color.White
                        )
                    }
                    // Tombol Logout
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("btn_admin_logout")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Keluar / Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WaliPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = WaliPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_wali")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Wali Asuh", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Tambah Wali Asuh", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Multi-Tenant Core Configuration Card
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(WaliPrimary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = WaliPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Pengaturan Sekolah & Multi-Tenant",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${schoolProfile.schoolName} (${schoolProfile.tenantCode})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Kustomisasi nama sekolah, faskes rujukan, serta impor data anak asuh & wali asuh secara massal menggunakan file CSV.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // 3 Action Buttons: Edit School, Import Siswa CSV, Import Wali CSV
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showEditSchoolDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_edit_school_profile")
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp), tint = WaliPrimary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Profil Sekolah", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Nama & Faskes", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showImportStudentsDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_import_students_csv")
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF16A34A))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Import Siswa", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("${students.size} Terdaftar", fontSize = 9.sp, color = Color(0xFF16A34A))
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showImportWaliDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_import_wali_csv")
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF2563EB))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Import Wali", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("${waliAccounts.size} Akun", fontSize = 9.sp, color = Color(0xFF2563EB))
                                    }
                                }
                            }
                        }
                    }
                }

                // Summary Card Admin
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Kelola Akun Wali Asuh",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(WaliPrimary.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${waliAccounts.size} Wali Terdaftar",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WaliPrimary
                                    )
                                }
                            }

                            Text(
                                text = "Setiap wali asuh memiliki Kartu Akun login resmi dengan username & password acak yang dapat dibagikan langsung ke WhatsApp.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Asrama Putra", fontSize = 10.5.sp, color = Color(0xFF0369A1))
                                        Text("$putraCount Akun", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                    }
                                }
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE7F3)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Asrama Putri", fontSize = 10.5.sp, color = Color(0xFFBE185D))
                                        Text("$putriCount Akun", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBE185D))
                                    }
                                }
                            }
                        }
                    }
                }

                // Search & Filter Bar
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari nama wali asuh...", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = WaliPrimary, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("admin_search_wali")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("Semua", "Putra", "Putri").forEach { dormFilter ->
                                FilterChip(
                                    selected = selectedFilterDormitory == dormFilter,
                                    onClick = { selectedFilterDormitory = dormFilter },
                                    label = { Text(dormFilter, fontSize = 11.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
                }

                // List of Wali Account Cards
                if (filteredAccounts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Wali asuh dengan kata kunci '$searchQuery' tidak ditemukan." else "Belum ada akun wali asuh yang terdaftar. Tekan tombol '+ Tambah Wali Asuh' untuk mendaftarkan.",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredAccounts, key = { it.id }) { account ->
                        WaliAccountCardItem(
                            account = account,
                            onCopy = { text, label ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(label, text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "$label berhasil disalin!", Toast.LENGTH_SHORT).show()
                            },
                            onShareWhatsApp = {
                                val message = PasswordGenerator.formatWhatsAppMessage(account)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    val url = if (account.phone.isNotBlank()) {
                                        val cleanPhone = if (account.phone.startsWith("0")) "62" + account.phone.substring(1) else account.phone
                                        "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
                                    } else {
                                        "https://api.whatsapp.com/send?text=${Uri.encode(message)}"
                                    }
                                    data = Uri.parse(url)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback to plain chooser
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, message)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Bagikan Akun Wali Asuh"))
                                }
                            },
                            onEdit = { accountToEdit = account },
                            onResetPassword = {
                                viewModel.resetWaliPassword(account) { newPass ->
                                    Toast.makeText(context, "Password baru untuk ${account.name}: $newPass", Toast.LENGTH_LONG).show()
                                }
                            },
                            onDelete = { accountToDelete = account }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddWaliAccountDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, username, dormitory, phone, password ->
                viewModel.addWaliAccount(name, username, dormitory, phone, password) {
                    Toast.makeText(context, "Kartu akun untuk $name berhasil dibuat!", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
            }
        )
    }

    accountToEdit?.let { account ->
        EditWaliAccountDialog(
            account = account,
            onDismiss = { accountToEdit = null },
            onConfirm = { updated ->
                viewModel.updateWaliAccount(updated) {
                    Toast.makeText(context, "Akun ${updated.name} berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                }
                accountToEdit = null
            }
        )
    }

    accountToDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            title = { Text("Hapus Akun Wali Asuh?") },
            text = {
                Text("Akun login untuk '${account.name}' (${account.dormitory}) akan dihapus. Wali asuh tidak dapat masuk ke aplikasi dengan akun ini lagi.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWaliAccount(account)
                        Toast.makeText(context, "Akun ${account.name} dihapus.", Toast.LENGTH_SHORT).show()
                        accountToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { accountToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("Opsi Navigasi Portal Admin") },
            text = { Text("Pilih tindakan yang ingin Anda lakukan:") },
            confirmButton = {
                Button(onClick = {
                    showExitConfirmDialog = false
                    onNavigateToApp()
                }) {
                    Text("Buka Aplikasi")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showExitConfirmDialog = false }) {
                        Text("Batal")
                    }
                    OutlinedButton(onClick = {
                        showExitConfirmDialog = false
                        onLogout()
                    }) {
                        Text("Logout")
                    }
                }
            }
        )
    }

    if (showEditSchoolDialog) {
        EditSchoolProfileDialog(
            currentProfile = schoolProfile,
            onDismiss = { showEditSchoolDialog = false },
            onSave = { updated ->
                viewModel.updateSchoolProfile(updated) {
                    Toast.makeText(context, "Profil sekolah & faskes berhasil disimpan!", Toast.LENGTH_SHORT).show()
                }
                showEditSchoolDialog = false
            }
        )
    }

    if (showImportStudentsDialog) {
        ImportStudentsCsvDialog(
            onDismiss = { showImportStudentsDialog = false },
            onImportConfirm = { studentList, replace ->
                viewModel.saveImportedStudents(studentList, replace) { count ->
                    showImportStudentsDialog = false
                    Toast.makeText(context, "Berhasil mengimpor $count siswa ke database!", Toast.LENGTH_LONG).show()
                }
            },
            onLoadDefaultKediri = { replace ->
                viewModel.loadDefaultKediriStudents { count ->
                    showImportStudentsDialog = false
                    Toast.makeText(context, "Berhasil memuat $count siswa SRT 1 Kediri!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showImportWaliDialog) {
        ImportWaliCsvDialog(
            onDismiss = { showImportWaliDialog = false },
            onImportConfirm = { accountList, replace ->
                viewModel.saveImportedWaliAccounts(accountList, replace) { count ->
                    showImportWaliDialog = false
                    Toast.makeText(context, "Berhasil mengimpor $count akun wali asuh!", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

/**
 * Kartu Akun Login Wali Asuh (Digital ID Card)
 * Dirancang khusus elegan seperti kartu tanda pengenal dinas resmi.
 */
@Composable
fun WaliAccountCardItem(
    account: WaliAccount,
    onCopy: (text: String, label: String) -> Unit,
    onShareWhatsApp: () -> Unit,
    onEdit: () -> Unit,
    onResetPassword: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val isPutri = account.dormitory.contains("Putri", ignoreCase = true)
    val dormAccentColor = if (isPutri) Color(0xFFBE185D) else Color(0xFF0369A1)
    val dormBgColor = if (isPutri) Color(0xFFFDF2F8) else Color(0xFFF0F9FF)

    val effectiveUsername = account.username.ifBlank {
        account.name.trim().lowercase().replace(" ", "").filter { it.isLetterOrDigit() }
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_wali_account_${account.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Kartu Akun: Gradasi Elegan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(dormAccentColor, dormAccentColor.copy(alpha = 0.82f))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "KARTU AKSES WALI ASUH",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Sekolah Rakyat SRT 1 Kab. Kediri",
                            fontSize = 9.5.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = account.dormitory,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Body Kartu
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Info Identitas Wali Asuh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(dormAccentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = dormAccentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.name,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (account.phone.isNotBlank()) {
                            Text(
                                text = "WhatsApp: ${account.phone}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Wali Asuh Aktif • ${account.dormitory}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Kotak Kredensial Login (Username & Password Acak)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dormAccentColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = dormBgColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Baris Username
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Username:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = effectiveUsername,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dormAccentColor
                                )
                            }

                            IconButton(
                                onClick = { onCopy(effectiveUsername, "Username") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Salin Username",
                                    tint = dormAccentColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // Baris Password
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Password:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (passwordVisible) account.password else "••••••••",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Sembunyikan" else "Lihat",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onCopy(account.password, "Password") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Salin Password",
                                        tint = dormAccentColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Tombol Aksi di Baris Bawah Kartu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Hapus, Edit & Acak Ulang di Kiri
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Hapus Akun",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Akun",
                                tint = dormAccentColor,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = onResetPassword,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Acak Password", fontSize = 10.5.sp)
                        }
                    }

                    // Tombol Bagikan ke WhatsApp
                    Button(
                        onClick = onShareWhatsApp,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("btn_share_wa_${account.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kirim Akun ke WA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
