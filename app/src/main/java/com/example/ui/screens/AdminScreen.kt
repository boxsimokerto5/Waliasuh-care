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
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.School
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.rememberDatePickerState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.FileDownload
import com.example.data.model.ChildStudent
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.FastingSession
import com.example.ui.components.StudentAvatar
import com.example.ui.dialogs.AddStudentDialog
import com.example.ui.dialogs.ChangeAdminPasswordDialog
import com.example.ui.dialogs.EditSchoolProfileDialog
import com.example.ui.dialogs.ImportStudentsCsvDialog
import com.example.ui.dialogs.ImportWaliCsvDialog
import com.example.ui.dialogs.StudentDetailDialog
import com.example.util.StudentClassifier
import com.example.util.StudentCsvParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: HealthViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val waliAccounts by viewModel.waliAccounts.collectAsStateWithLifecycle()
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val schoolRegistrations by viewModel.schoolRegistrations.collectAsStateWithLifecycle()
    val currentUsername by viewModel.currentUsername.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val fastingSessions by viewModel.fastingSessions.collectAsStateWithLifecycle()
    val fastingRecords by viewModel.fastingRecords.collectAsStateWithLifecycle()

    var selectedAdminTab by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var showEditSchoolDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showImportStudentsDialog by remember { mutableStateOf(false) }
    var showImportWaliDialog by remember { mutableStateOf(false) }
    var showClearDataConfirmDialog by remember { mutableStateOf(false) }

    // State untuk Kelola Sesi Puasa (khusus Admin Sekolah)
    var showCreateFastingSessionDialog by remember { mutableStateOf(false) }
    var fastingSessionToDelete by remember { mutableStateOf<FastingSession?>(null) }

    // State untuk Kelola Data Siswa
    var studentSearchQuery by remember { mutableStateOf("") }
    var selectedStudentGenderFilter by remember { mutableStateOf("Semua") }
    var studentToDelete by remember { mutableStateOf<ChildStudent?>(null) }
    var selectedDetailStudent by remember { mutableStateOf<ChildStudent?>(null) }
    var studentToEdit by remember { mutableStateOf<ChildStudent?>(null) }
    var showAddStudentDialog by remember { mutableStateOf(false) }

    // State untuk Kelola Akun Wali Asuh
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

    val activeSchoolCode = schoolProfile.tenantCode.ifBlank { "SRT1-KDR" }
    // Isolasi ketat: Hanya tampilkan akun dan siswa yang benar-benar terdaftar di sekolah aktif ini
    val schoolAccounts = waliAccounts.filter {
        it.schoolId.equals(activeSchoolCode, ignoreCase = true)
    }
    val schoolStudents = students.filter {
        it.schoolId.equals(activeSchoolCode, ignoreCase = true)
    }
    val schoolFastingSessions = remember(fastingSessions, activeSchoolCode) {
        fastingSessions
            .filter { it.schoolId.equals(activeSchoolCode, ignoreCase = true) }
            .sortedByDescending { it.sessionDate }
    }

    val exportStudentsCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csvContent = "\uFEFF" + StudentCsvParser.exportStudentsToCsv(schoolStudents)
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(
                    context,
                    "Berhasil mengekspor ${schoolStudents.size} data siswa ke CSV!",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal mengekspor CSV siswa: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val exportWaliCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csvContent = "\uFEFF" + StudentCsvParser.exportWaliToCsv(schoolAccounts)
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(
                    context,
                    "Berhasil mengekspor ${schoolAccounts.size} akun wali ke CSV!",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal mengekspor CSV wali: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val filteredAccounts = schoolAccounts.filter { account ->
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

    val putraCount = schoolAccounts.count { it.dormitory.contains("Putra", ignoreCase = true) }
    val putriCount = schoolAccounts.count { it.dormitory.contains("Putri", ignoreCase = true) }

    val filteredStudents = schoolStudents.filter { student ->
        val matchSearch = studentSearchQuery.isBlank() ||
                student.name.contains(studentSearchQuery, ignoreCase = true) ||
                student.nis.contains(studentSearchQuery, ignoreCase = true) ||
                student.nik.contains(studentSearchQuery, ignoreCase = true) ||
                student.noKk.contains(studentSearchQuery, ignoreCase = true) ||
                student.motherName.contains(studentSearchQuery, ignoreCase = true) ||
                student.address.contains(studentSearchQuery, ignoreCase = true) ||
                student.jenjang.contains(studentSearchQuery, ignoreCase = true) ||
                student.roomNumber.contains(studentSearchQuery, ignoreCase = true) ||
                student.classGrade.contains(studentSearchQuery, ignoreCase = true) ||
                student.dormitory.contains(studentSearchQuery, ignoreCase = true)
        val matchGender = when (selectedStudentGenderFilter) {
            "Putra" -> StudentClassifier.isMaleStudent(student)
            "Putri" -> StudentClassifier.isFemaleStudent(student)
            else -> true
        }
        matchSearch && matchGender
    }

    val studentPutraCount = schoolStudents.count { StudentClassifier.isMaleStudent(it) }
    val studentPutriCount = schoolStudents.count { StudentClassifier.isFemaleStudent(it) }

    fun shareAllAccountsSummary() {
        if (schoolAccounts.isEmpty()) {
            Toast.makeText(context, "Belum ada akun wali asuh untuk dibagikan.", Toast.LENGTH_SHORT).show()
            return
        }
        val sb = StringBuilder()
        sb.appendLine("*DAFTAR KARTU AKSES WALI ASUH*")
        sb.appendLine("_${schoolProfile.schoolName} [${activeSchoolCode}]_")
        sb.appendLine("Tanggal: ${DateUtils.formatFullDate(System.currentTimeMillis())}")
        sb.appendLine("Total: ${schoolAccounts.size} Akun Terdaftar")
        sb.appendLine("----------------------------------------")
        schoolAccounts.forEachIndexed { index, acc ->
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
        sb.appendLine("_Dikelola oleh Administrator (${schoolProfile.adminName})_")
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
                            text = "${schoolProfile.schoolName.ifBlank { schoolProfile.shortName }} • ${schoolProfile.city}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
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
                onClick = {
                    when (selectedAdminTab) {
                        0 -> showAddStudentDialog = true
                        1 -> showAddDialog = true
                        else -> showCreateFastingSessionDialog = true
                    }
                },
                containerColor = WaliPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_admin_action")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (selectedAdminTab) {
                            0 -> Icons.Default.PersonAdd
                            2 -> Icons.Default.EventNote
                            else -> Icons.Default.Add
                        },
                        contentDescription = when (selectedAdminTab) {
                            0 -> "Tambah Siswa"
                            2 -> "Buat Sesi Puasa"
                            else -> "Tambah Wali Asuh"
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (selectedAdminTab) {
                            0 -> "Tambah Siswa"
                            2 -> "Buat Sesi Puasa"
                            else -> "Tambah Wali Asuh"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
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
                                        if (schoolProfile.address.isNotBlank()) {
                                            Text(
                                                text = schoolProfile.address,
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Kustomisasi nama sekolah, alamat kop surat, faskes rujukan, serta impor/ekspor data anak asuh & wali asuh secara massal menggunakan file CSV.",
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
                                        Text("Nama, Alamat & Faskes", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                                        Text("${schoolStudents.size} Terdaftar", fontSize = 9.sp, color = Color(0xFF16A34A))
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
                                        Text("${schoolAccounts.size} Akun", fontSize = 9.sp, color = Color(0xFF2563EB))
                                    }
                                }
                            }

                            // 2 Action Buttons: Export Siswa CSV & Export Wali CSV (cadangan backup)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (schoolStudents.isEmpty()) {
                                            Toast.makeText(context, "Belum ada data siswa untuk diekspor.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val dateSlug = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                                            val codeSlug = activeSchoolCode.lowercase(Locale.US).replace(Regex("[^a-z0-9_-]"), "_")
                                            exportStudentsCsvLauncher.launch("data_siswa_${codeSlug}_$dateSlug.csv")
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_export_students_csv")
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0F766E))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Export Siswa (.csv)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (schoolAccounts.isEmpty()) {
                                            Toast.makeText(context, "Belum ada akun wali asuh untuk diekspor.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val dateSlug = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                                            val codeSlug = activeSchoolCode.lowercase(Locale.US).replace(Regex("[^a-z0-9_-]"), "_")
                                            exportWaliCsvLauncher.launch("data_wali_${codeSlug}_$dateSlug.csv")
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_export_wali_csv")
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1D4ED8))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Export Wali (.csv)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Tombol Ganti Password Admin Sekolah (Username dikunci, sinkron ke Superadmin)
                            OutlinedButton(
                                onClick = { showChangePasswordDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_admin_change_password"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp), tint = WaliPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ganti Password Admin Sekolah", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = WaliPrimary)
                            }

                            // Tombol Sinkronkan Data Sekolah ke Cloud (Supabase)
                            Button(
                                onClick = {
                                    viewModel.syncAllWithSupabase { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                },
                                enabled = !isSyncing,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_admin_sync_cloud"),
                                colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Menyinkronkan ke Cloud Supabase...", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.CloudSync, contentDescription = "Sinkron Cloud", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sinkronkan Semua Data Sekolah ke Cloud", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Tombol Bersihkan / Reset Data Siswa & Wali Sekolah Ini
                            OutlinedButton(
                                onClick = { showClearDataConfirmDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_clear_school_data"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bersihkan Data Siswa & Wali Sekolah Ini (Reset ke 0)", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Tab Navigasi: Kelola Data Siswa vs Akun Wali vs Sesi Puasa
                item {
                    TabRow(
                        selectedTabIndex = selectedAdminTab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Tab(
                            selected = selectedAdminTab == 0,
                            onClick = { selectedAdminTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Siswa (${schoolStudents.size})",
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = if (selectedAdminTab == 0) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_admin_students")
                        )
                        Tab(
                            selected = selectedAdminTab == 1,
                            onClick = { selectedAdminTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Wali (${schoolAccounts.size})",
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = if (selectedAdminTab == 1) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_admin_wali")
                        )
                        Tab(
                            selected = selectedAdminTab == 2,
                            onClick = { selectedAdminTab = 2 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.RestaurantMenu,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Sesi Puasa (${schoolFastingSessions.size})",
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = if (selectedAdminTab == 2) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_admin_sesi_puasa")
                        )
                    }
                }

                if (selectedAdminTab == 0) {
                    // ==========================================
                    // TAB 0: KELOLA MASTER DATA SISWA
                    // ==========================================
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
                                        text = "Kelola Master Data Siswa",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF16A34A).copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "${schoolStudents.size} Siswa Terdaftar",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A)
                                        )
                                    }
                                }

                                Text(
                                    text = "Daftar lengkap anak asuh sekolah ini. Admin dapat mencari anak, melihat riwayat medis, mendaftarkan anak baru, atau menghapus satu anak yang keluar/pindah.",
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
                                            Text("Siswa Putra", fontSize = 10.5.sp, color = Color(0xFF0369A1))
                                            Text("$studentPutraCount Siswa", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                        }
                                    }
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE7F3)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Siswa Putri", fontSize = 10.5.sp, color = Color(0xFFBE185D))
                                            Text("$studentPutriCount Siswa", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBE185D))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Search & Filter Bar Siswa
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = studentSearchQuery,
                                onValueChange = { studentSearchQuery = it },
                                placeholder = { Text("Cari nama, NIK, KK, NISN, kelas, ibu...", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = WaliPrimary, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("admin_search_student")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("Semua", "Putra", "Putri").forEach { filterOpt ->
                                    FilterChip(
                                        selected = selectedStudentGenderFilter == filterOpt,
                                        onClick = { selectedStudentGenderFilter = filterOpt },
                                        label = {
                                            val labelText = when (filterOpt) {
                                                "Putra" -> "Putra ($studentPutraCount)"
                                                "Putri" -> "Putri ($studentPutriCount)"
                                                else -> "Semua (${schoolStudents.size})"
                                            }
                                            Text(labelText, fontSize = 11.sp)
                                        },
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                            }
                        }
                    }

                    // List of Student Cards
                    if (filteredStudents.isEmpty()) {
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
                                        text = if (studentSearchQuery.isNotBlank()) "Siswa dengan kata kunci '$studentSearchQuery' tidak ditemukan." else "Belum ada data siswa di sekolah ini. Anda dapat mengimpor file CSV siswa atau menekan '+ Tambah Siswa'.",
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredStudents, key = { "student_${it.id}" }) { student ->
                            AdminStudentCardItem(
                                student = student,
                                onViewDetail = { selectedDetailStudent = student },
                                onDelete = { studentToDelete = student }
                            )
                        }
                    }
                } else if (selectedAdminTab == 1) {
                    // ==========================================
                    // TAB 1: KELOLA AKUN WALI ASUH
                    // ==========================================
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
                                        text = "${schoolAccounts.size} Wali Terdaftar",
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
                            schoolName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
                            onCopy = { text, label ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(label, text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "$label berhasil disalin!", Toast.LENGTH_SHORT).show()
                            },
                            onShareWhatsApp = {
                                val message = PasswordGenerator.formatWhatsAppMessage(account, schoolProfile)
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
                } else {
                    // ==========================================
                    // TAB 2: KELOLA SESI PUASA (KHUSUS ADMIN)
                // ==========================================
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Manajemen Sesi Puasa Siswa",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFCCFBF1))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${schoolFastingSessions.count { it.isActive }} Sesi Aktif",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F766E)
                                    )
                                }
                            }

                            Text(
                                text = "Hanya Admin Sekolah yang dapat membuat sesi puasa (contoh: Puasa Senin Kamis, 27 Oktober 2026). Wali Asuh akan menggunakan sesi aktif ini untuk scan kartu siswa dan checklist sahur/buka.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = { showCreateFastingSessionDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_admin_create_fasting_session")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buat Sesi Puasa Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (schoolFastingSessions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada sesi puasa. Tekan '+ Buat Sesi Puasa Baru' untuk membuka sesi pendataan puasa siswa.",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(schoolFastingSessions, key = { "admin_fasting_${it.id}" }) { session ->
                        val recs = fastingRecords.filter { it.sessionId == session.id }
                        val sma = recs.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SMA }
                        val smp = recs.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SMP }
                        val sd = recs.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SD }

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_fasting_session_${session.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = session.title,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = DateUtils.formatFullDate(session.sessionDate),
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF0F766E),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (session.isActive) "Aktif" else "Selesai",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (session.isActive) Color(0xFF15803D) else Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Switch(
                                            checked = session.isActive,
                                            onCheckedChange = {
                                                viewModel.toggleFastingSessionActive(session)
                                            }
                                        )
                                    }
                                }

                                if (session.description.isNotBlank()) {
                                    Text(
                                        text = session.description,
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Peserta: ${recs.size} Siswa ($sma SMA • $smp SMP • $sd SD)",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    IconButton(
                                        onClick = { fastingSessionToDelete = session },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Hapus Sesi Puasa",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }

    if (showAddDialog) {
        AddWaliAccountDialog(
            schoolName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
            onDismiss = { showAddDialog = false },
            onConfirm = { name, username, dormitory, phone, password ->
                viewModel.addWaliAccount(name, username, dormitory, phone, password) {
                    Toast.makeText(context, "Kartu akun untuk $name berhasil dibuat!", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
            }
        )
    }

    if (showAddStudentDialog) {
        AddStudentDialog(
            activeSchoolId = activeSchoolCode,
            onDismiss = { showAddStudentDialog = false },
            onConfirmStudent = { newStudent ->
                viewModel.addStudent(
                    name = newStudent.name,
                    nis = newStudent.nis,
                    gender = newStudent.gender,
                    age = newStudent.age,
                    dormitory = newStudent.dormitory,
                    classGrade = newStudent.classGrade,
                    allergies = newStudent.allergies,
                    bloodType = newStudent.bloodType,
                    emergencyContact = newStudent.emergencyContact,
                    specialNotes = newStudent.specialNotes,
                    nik = newStudent.nik,
                    noKk = newStudent.noKk,
                    birthPlace = newStudent.birthPlace,
                    birthDate = newStudent.birthDate,
                    motherName = newStudent.motherName,
                    address = newStudent.address,
                    roomNumber = newStudent.roomNumber,
                    jenjang = newStudent.jenjang
                )
                Toast.makeText(context, "Siswa ${newStudent.name} berhasil didaftarkan ke ${schoolProfile.shortName}!", Toast.LENGTH_SHORT).show()
                showAddStudentDialog = false
            }
        )
    }

    studentToEdit?.let { editingStudent ->
        AddStudentDialog(
            initialStudent = editingStudent,
            activeSchoolId = activeSchoolCode,
            onDismiss = { studentToEdit = null },
            onConfirmStudent = { updated ->
                viewModel.updateStudent(updated) {
                    Toast.makeText(context, "Profil ${updated.name} berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                }
                studentToEdit = null
            }
        )
    }

    selectedDetailStudent?.let { student ->
        StudentDetailDialog(
            student = student,
            studentRecords = allRecords.filter { it.studentId == student.id },
            schoolProfile = schoolProfile,
            onEditStudent = { toEdit ->
                selectedDetailStudent = null
                studentToEdit = toEdit
            },
            onDismiss = { selectedDetailStudent = null }
        )
    }

    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Data Siswa?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus data siswa:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = student.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "NIS: ${student.nis.ifBlank { "-" }} • ${student.dormitory} • Kelas ${student.classGrade}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Tindakan ini permanen. Siswa akan dihapus dari sekolah ini dan seluruh riwayat berobat/rencana terkait akan dibersihkan.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val studentName = student.name
                        viewModel.deleteStudent(student)
                        Toast.makeText(context, "Siswa $studentName berhasil dihapus dari sistem.", Toast.LENGTH_SHORT).show()
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_delete_student")
                ) {
                    Text("Hapus Siswa", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { studentToDelete = null }) {
                    Text("Batal")
                }
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
            title = { Text("Keluar dari Portal Admin?") },
            text = { Text("Apakah Anda yakin ingin keluar (logout) dari sesi Administrator Sekolah?") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("Batal")
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

    if (showChangePasswordDialog) {
        val activeReg = schoolRegistrations.firstOrNull { reg ->
            (!currentUsername.isNullOrBlank() && reg.generatedAdminUsername.equals(currentUsername, ignoreCase = true)) ||
                reg.generatedSchoolCode.equals(activeSchoolCode, ignoreCase = true)
        }
        val lockedUsername = activeReg?.generatedAdminUsername?.takeIf { it.isNotBlank() }
            ?: currentUsername?.takeIf { it.isNotBlank() }
            ?: if (activeSchoolCode.equals("SRT1-KDR", ignoreCase = true)) "ekowahyudiadmin" else "admin_${activeSchoolCode.lowercase()}"
        val currentPass = activeReg?.generatedAdminPassword?.takeIf { it.isNotBlank() }
            ?: if (lockedUsername.equals("ekowahyudiadmin", ignoreCase = true)) "Woyowoyo12@" else ""

        ChangeAdminPasswordDialog(
            viewModel = viewModel,
            adminUsername = lockedUsername,
            currentPassword = currentPass,
            schoolName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
            schoolCode = activeSchoolCode,
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    if (showImportStudentsDialog) {
        ImportStudentsCsvDialog(
            currentSchoolId = activeSchoolCode,
            schoolName = schoolProfile.schoolName,
            onDismiss = { showImportStudentsDialog = false },
            onImportConfirm = { studentList, replace ->
                viewModel.saveImportedStudents(studentList, replace, activeSchoolCode) { count ->
                    showImportStudentsDialog = false
                    Toast.makeText(context, "Berhasil mengimpor $count siswa ke database $activeSchoolCode!", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    if (showClearDataConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirmDialog = false },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Bersihkan Data Sekolah Ini?") },
            text = {
                Text(
                    "Tindakan ini akan menghapus seluruh data siswa dan akun wali asuh yang terdaftar pada sekolah ${schoolProfile.schoolName} (${activeSchoolCode}). Data akan di-reset menjadi 0 dan bersih total."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearSchoolData(activeSchoolCode) {
                            showClearDataConfirmDialog = false
                            Toast.makeText(context, "Seluruh data siswa dan wali untuk $activeSchoolCode telah dihapus bersih!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ya, Bersihkan Semua", color = Color.White)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showClearDataConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showCreateFastingSessionDialog) {
        CreateFastingSessionDialog(
            onDismiss = { showCreateFastingSessionDialog = false },
            onConfirm = { title, dateMillis, description ->
                viewModel.createFastingSession(
                    title = title,
                    sessionDate = dateMillis,
                    description = description
                ) { created ->
                    if (created != null) {
                        Toast.makeText(context, "Sesi puasa \"${created.title}\" berhasil dibuat!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Hanya Admin Sekolah yang dapat membuat sesi puasa.", Toast.LENGTH_SHORT).show()
                    }
                }
                showCreateFastingSessionDialog = false
            }
        )
    }

    fastingSessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { fastingSessionToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hapus Sesi Puasa?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Sesi \"${session.title}\" beserta data peserta puasa di dalamnya akan dihapus.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFastingSession(session) {
                            Toast.makeText(context, "Sesi \"${session.title}\" dihapus.", Toast.LENGTH_SHORT).show()
                        }
                        fastingSessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { fastingSessionToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showImportWaliDialog) {
        ImportWaliCsvDialog(
            currentSchoolId = activeSchoolCode,
            schoolName = schoolProfile.schoolName,
            onDismiss = { showImportWaliDialog = false },
            onImportConfirm = { accountList, replace ->
                viewModel.saveImportedWaliAccounts(accountList, replace, activeSchoolCode) { count ->
                    showImportWaliDialog = false
                    Toast.makeText(context, "Berhasil mengimpor $count akun wali asuh ke database $activeSchoolCode!", Toast.LENGTH_LONG).show()
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
    schoolName: String = "Sekolah Anda",
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
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "KARTU AKSES WALI ASUH",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = schoolName.ifBlank { "Sekolah Anda" },
                            fontSize = 9.5.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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

                        // Keterangan status sinkronisasi
                        Text(
                            text = "✓ Password tersinkron otomatis dari profil wali asuh",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = dormAccentColor.copy(alpha = 0.85f)
                        )
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

@Composable
fun AdminStudentCardItem(
    student: ChildStudent,
    onViewDetail: () -> Unit,
    onDelete: () -> Unit
) {
    val isPutri = StudentClassifier.isFemaleStudent(student)
    val accentColor = if (isPutri) Color(0xFFBE185D) else Color(0xFF0369A1)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_student_card_${student.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(name = student.name, gender = student.gender, size = 44.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = student.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor.copy(alpha = 0.12f))
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = if (isPutri) "Putri" else "Putra",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                        if (student.jenjang.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = student.jenjang,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (student.nis.isNotBlank()) {
                            Text(
                                text = "NISN/NIS: ${student.nis}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(text = "•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        if (student.classGrade.isNotBlank()) {
                            Text(
                                text = "Kelas ${student.classGrade}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "NIK: ${student.nik.ifBlank { "-" }} • KK: ${student.noKk.ifBlank { "-" }}",
                        fontSize = 10.5.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = if (student.nik.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val ttlSummary = listOf(student.birthPlace, student.birthDate)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                    if (ttlSummary.isNotBlank() || student.motherName.isNotBlank()) {
                        Text(
                            text = buildString {
                                if (ttlSummary.isNotBlank()) append("TTL: $ttlSummary (${student.age} Th)")
                                if (ttlSummary.isNotBlank() && student.motherName.isNotBlank()) append(" • ")
                                if (student.motherName.isNotBlank()) append("Ibu: ${student.motherName}")
                            },
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    if (student.address.isNotBlank()) {
                        Text(
                            text = "Alamat: ${student.address}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    if (student.dormitory.isNotBlank()) {
                        Text(
                            text = "🏠 ${student.dormitory}${if (student.roomNumber.isNotBlank()) " • Kamar ${student.roomNumber}" else ""}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Optional note/allergy badges if available
            val hasAllergy = student.allergies.isNotBlank() && !student.allergies.contains("tidak", ignoreCase = true) && student.allergies != "-"
            val hasBlood = student.bloodType.isNotBlank() && student.bloodType != "-"
            if (hasAllergy || hasBlood) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasAllergy) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFEF2F2))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚠️ Alergi: ${student.allergies}",
                                fontSize = 10.sp,
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (hasBlood) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEFF6FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🩸 Gol: ${student.bloodType}",
                                fontSize = 10.sp,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewDetail,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("btn_student_detail_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = WaliPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Detail & Rekam Medis", fontSize = 10.5.sp, color = WaliPrimary, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("btn_delete_student_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus Siswa", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFastingSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, dateMillis: Long, description: String) -> Unit
) {
    var title by remember { mutableStateOf("Puasa Senin Kamis") }
    var description by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val quickTitles = listOf(
        "Puasa Senin Kamis",
        "Puasa Ayyamul Bidh",
        "Puasa Ramadhan",
        "Puasa Daud",
        "Puasa Sunnah"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = Color(0xFF0F766E))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buat Sesi Puasa Baru", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Pilih cepat jenis puasa:",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickTitles.forEach { qt ->
                        FilterChip(
                            selected = title.equals(qt, ignoreCase = true),
                            onClick = { title = qt },
                            label = { Text(qt, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Sesi Puasa *") },
                    placeholder = { Text("Contoh: Puasa Senin Kamis") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fasting_session_title")
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_pick_fasting_session_date")
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0F766E))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tanggal: ${DateUtils.formatFullDate(selectedDateMillis)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan (Opsional)") },
                    placeholder = { Text("Contoh: Sahur pukul 03.30 WIB di dapur umum") },
                    maxLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), selectedDateMillis, description.trim())
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                modifier = Modifier.testTag("btn_save_fasting_session")
            ) {
                Text("Buat Sesi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


