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
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.TandemWaliHelper
import com.example.data.model.WaliAccount
import com.example.ui.HealthViewModel
import com.example.ui.dialogs.AddWaliAccountDialog
import com.example.ui.dialogs.CreateFastingSessionDialog
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.rememberDatePickerState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ScrollableTabRow
import com.example.data.model.ChildStudent
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.FastingSession
import com.example.data.model.MengajiHelper
import com.example.data.model.VisitHelper
import com.example.data.model.VisitSession
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliScheduleHelper
import com.example.ui.components.AdminCsvBackupSheet
import com.example.ui.components.AdminHeroSummaryCard
import com.example.ui.components.AdminQuickMenuGridCard
import com.example.ui.components.AdminSecurityMaintenanceSheet
import com.example.ui.components.StudentAvatar
import com.example.ui.dialogs.AddStudentDialog
import com.example.ui.dialogs.ChangeAdminPasswordDialog
import com.example.ui.dialogs.CreateVisitSessionDialog
import com.example.ui.dialogs.DaftarKodeSopSheet
import com.example.ui.dialogs.DeleteAccountBottomSheet
import com.example.ui.dialogs.EditJadwalSelDialog
import com.example.ui.dialogs.EditSchoolProfileDialog
import com.example.ui.dialogs.ImportJadwalMatriksDialog
import com.example.ui.dialogs.ImportStudentsCsvDialog
import com.example.ui.dialogs.ImportWaliCsvDialog
import com.example.ui.dialogs.InfoPentingFeedSheet
import com.example.ui.dialogs.InputMengajiDialog
import com.example.ui.dialogs.JadwalMatriksSheet
import com.example.ui.dialogs.JadwalPersonalSheet
import com.example.ui.dialogs.KartuMengajiSheet
import com.example.ui.dialogs.KelolaAnakAdminDialog
import com.example.ui.dialogs.ManageShiftCodesDialog
import com.example.ui.dialogs.PenyematanTugasSheet
import com.example.ui.dialogs.PetugasJamIniDialog
import com.example.ui.dialogs.RekapIzinJadwalSheet
import com.example.ui.dialogs.SchedulePinChip
import com.example.ui.dialogs.StudentDetailDialog
import com.example.ui.dialogs.StudentPhotoPreviewDialog
import com.example.ui.dialogs.openJadwalExternalUrl
import com.example.ui.dialogs.parseJadwalShiftColor
import com.example.util.PdfReportGenerator
import com.example.util.StudentClassifier
import com.example.util.StudentCsvParser
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.LocalAppStrings
import java.io.File
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
    val appStrings = LocalAppStrings.current
    val isEn = appStrings.isEnglish
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val waliAccounts by viewModel.waliAccounts.collectAsStateWithLifecycle()
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val schoolRegistrations by viewModel.schoolRegistrations.collectAsStateWithLifecycle()
    val currentUsername by viewModel.currentUsername.collectAsStateWithLifecycle()
    val waliName by viewModel.waliName.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val fastingSessions by viewModel.fastingSessions.collectAsStateWithLifecycle()
    val fastingRecords by viewModel.fastingRecords.collectAsStateWithLifecycle()
    val mengajiRecords by viewModel.mengajiRecords.collectAsStateWithLifecycle()
    val sholatRecords by viewModel.sholatRecords.collectAsStateWithLifecycle()
    val haidRecords by viewModel.allHaidRecords.collectAsStateWithLifecycle()
    val visitSessions by viewModel.visitSessions.collectAsStateWithLifecycle()
    val visitRecords by viewModel.visitRecords.collectAsStateWithLifecycle()
    val schoolShiftCodes by viewModel.schoolShiftCodes.collectAsStateWithLifecycle()
    val schoolScheduleEntries by viewModel.schoolScheduleEntries.collectAsStateWithLifecycle()
    val infoPentingPosts by viewModel.infoPentingPosts.collectAsStateWithLifecycle()
    val infoPentingReactions by viewModel.infoPentingReactions.collectAsStateWithLifecycle()
    val infoPentingComments by viewModel.infoPentingComments.collectAsStateWithLifecycle()
    val unreadInfoPentingCount by viewModel.unreadInfoPentingCount.collectAsStateWithLifecycle()
    val isAnakSuperAdmin by viewModel.isAnakSuperAdmin.collectAsStateWithLifecycle()
    val isUploadingStudentPhoto by viewModel.isUploadingStudentPhoto.collectAsStateWithLifecycle()

    var showInfoPentingSheet by remember { mutableStateOf(false) }
    var selectedAdminTab by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        viewModel.syncInfoPentingChannelNow()
    }
    LaunchedEffect(showInfoPentingSheet) {
        if (showInfoPentingSheet) {
            viewModel.syncInfoPentingChannelNow()
            viewModel.markInfoPentingRead()
        }
    }
    var jadwalMonthKey by remember { mutableStateOf(WaliScheduleHelper.currentMonthKey()) }
    var jadwalSelectedDay by remember { mutableIntStateOf(WaliScheduleHelper.currentDayOfMonth()) }
    var showJadwalMatriksSheet by remember { mutableStateOf(false) }
    var jadwalPersonalTarget by remember { mutableStateOf<WaliScheduleEntry?>(null) }
    var showPetugasJamIni by remember { mutableStateOf(false) }
    var showRekapIzinJadwal by remember { mutableStateOf(false) }
    var showDaftarKodeSop by remember { mutableStateOf(false) }
    var showPenyematanTugas by remember { mutableStateOf(false) }
    var showManageShiftCodes by remember { mutableStateOf(false) }
    var showImportJadwalMatriks by remember { mutableStateOf(false) }
    var importJadwalInitialTab by remember { mutableIntStateOf(0) }
    var editingJadwalCellTarget by remember { mutableStateOf<Pair<WaliScheduleEntry, Int>?>(null) }
    var readyAdminJadwalPdf by remember { mutableStateOf<File?>(null) }
    var showEditSchoolDialog by remember { mutableStateOf(false) }
    var showCsvBackupSheet by remember { mutableStateOf(false) }
    var showSecurityMaintenanceSheet by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showImportStudentsDialog by remember { mutableStateOf(false) }
    var showImportWaliDialog by remember { mutableStateOf(false) }
    var showClearDataConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteSchoolAccountSheet by remember { mutableStateOf(false) }
    var isDeletingSchoolAccount by remember { mutableStateOf(false) }
    var deleteSchoolAccountError by remember { mutableStateOf<String?>(null) }

    // State untuk Kelola Sesi Puasa (khusus Admin Sekolah)
    var showCreateFastingSessionDialog by remember { mutableStateOf(false) }
    var fastingSessionToDelete by remember { mutableStateOf<FastingSession?>(null) }

    // State untuk Kelola Sesi Kunjungan Orang Tua (khusus Admin Sekolah)
    var showCreateVisitSessionDialog by remember { mutableStateOf(false) }
    var visitSessionToEdit by remember { mutableStateOf<VisitSession?>(null) }
    var visitSessionToDelete by remember { mutableStateOf<VisitSession?>(null) }

    // State untuk Kelola Data Siswa
    var studentSearchQuery by remember { mutableStateOf("") }
    var selectedStudentGenderFilter by remember { mutableStateOf("Semua") }
    var studentToDelete by remember { mutableStateOf<ChildStudent?>(null) }
    var selectedDetailStudent by remember { mutableStateOf<ChildStudent?>(null) }
    var studentForMengajiInput by remember { mutableStateOf<ChildStudent?>(null) }
    var studentForKartuMengaji by remember { mutableStateOf<ChildStudent?>(null) }
    var studentToEdit by remember { mutableStateOf<ChildStudent?>(null) }
    var studentForManageTandem by remember { mutableStateOf<ChildStudent?>(null) }
    var showAddStudentDialog by remember { mutableStateOf(false) }

    // State untuk Kelola Akun Wali Asuh
    var showAddDialog by remember { mutableStateOf(false) }
    var showManageAnakAdminDialog by remember { mutableStateOf(false) }
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
    val schoolVisitSessions = remember(visitSessions, activeSchoolCode) {
        visitSessions
            .filter { it.schoolId.equals(activeSchoolCode, ignoreCase = true) }
            .sortedWith(
                compareByDescending<VisitSession> { it.isActive }
                    .thenByDescending { it.monthYear }
                    .thenByDescending { it.createdAt }
            )
    }
    val jadwalMonthEntries = remember(schoolScheduleEntries, activeSchoolCode, jadwalMonthKey) {
        schoolScheduleEntries
            .filter {
                it.schoolId.equals(activeSchoolCode, ignoreCase = true) &&
                    it.monthKey == jadwalMonthKey
            }
            .sortedWith(compareBy<WaliScheduleEntry> { it.sortOrder }.thenBy { it.waliName.lowercase() })
    }

    LaunchedEffect(
        selectedAdminTab,
        activeSchoolCode,
        showJadwalMatriksSheet,
        showManageShiftCodes,
        showImportJadwalMatriks
    ) {
        if (selectedAdminTab == 4 || showJadwalMatriksSheet || showManageShiftCodes || showImportJadwalMatriks) {
            viewModel.ensureSchoolShiftCodesSeeded(activeSchoolCode)
        }
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
                    if (isEn) {
                        "Successfully exported ${schoolStudents.size} student records to CSV!"
                    } else {
                        "Berhasil mengekspor ${schoolStudents.size} data siswa ke CSV!"
                    },
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    if (isEn) "Failed to export student CSV: ${e.message}" else "Gagal mengekspor CSV siswa: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
                    if (isEn) {
                        "Successfully exported ${schoolAccounts.size} Wali Asuh accounts to CSV!"
                    } else {
                        "Berhasil mengekspor ${schoolAccounts.size} akun wali ke CSV!"
                    },
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    if (isEn) "Failed to export Wali Asuh CSV: ${e.message}" else "Gagal mengekspor CSV wali: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val downloadJadwalTemplateCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csvContent = "\uFEFF" + WaliScheduleHelper.buildScheduleTemplateCsv(
                    monthKey = jadwalMonthKey,
                    existingEntries = jadwalMonthEntries,
                    waliAccounts = schoolAccounts,
                    includeExistingShifts = true
                )
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(
                    context,
                    if (isEn) {
                        "Duty schedule CSV template downloaded (${WaliScheduleHelper.formatMonthTitle(jadwalMonthKey, true)})!"
                    } else {
                        "Template CSV jadwal berhasil diunduh (${WaliScheduleHelper.formatMonthTitle(jadwalMonthKey, false)})!"
                    },
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    if (isEn) "Failed to download schedule CSV template: ${e.message}" else "Gagal mengunduh template CSV jadwal: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
                account.phone.contains(searchQuery, ignoreCase = true) ||
                account.roleLabel(isEn).contains(searchQuery, ignoreCase = true)
        val matchDorm = when (selectedFilterDormitory) {
            "Semua" -> true
            "Anak Admin" -> account.isAnakAdminSekolah()
            "Guru Ngaji" -> account.isGuruNgaji()
            else -> account.dormitory.contains(selectedFilterDormitory, ignoreCase = true)
        }
        matchSearch && matchDorm
    }

    val putraCount = schoolAccounts.count { it.dormitory.contains("Putra", ignoreCase = true) }
    val putriCount = schoolAccounts.count { it.dormitory.contains("Putri", ignoreCase = true) }
    val anakAdminCount = schoolAccounts.count { it.isAnakAdminSekolah() }

    val filteredStudents = schoolStudents.filter { student ->
        val matchSearch = studentSearchQuery.isBlank() ||
                student.name.contains(studentSearchQuery, ignoreCase = true) ||
                student.nis.contains(studentSearchQuery, ignoreCase = true) ||
                student.nik.contains(studentSearchQuery, ignoreCase = true) ||
                student.noKk.contains(studentSearchQuery, ignoreCase = true) ||
                student.motherName.contains(studentSearchQuery, ignoreCase = true) ||
                student.address.contains(studentSearchQuery, ignoreCase = true) ||
                student.district.contains(studentSearchQuery, ignoreCase = true) ||
                student.jenjang.contains(studentSearchQuery, ignoreCase = true) ||
                student.roomNumber.contains(studentSearchQuery, ignoreCase = true) ||
                student.classGrade.contains(studentSearchQuery, ignoreCase = true) ||
                student.dormitory.contains(studentSearchQuery, ignoreCase = true) ||
                TandemWaliHelper.allAssignedWaliRefs(student).any { (u, n) ->
                    n.contains(studentSearchQuery, ignoreCase = true) ||
                        u.contains(studentSearchQuery, ignoreCase = true)
                }
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
            Toast.makeText(
                context,
                if (isEn) "No Wali Asuh accounts to share yet." else "Belum ada akun wali asuh untuk dibagikan.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val sb = StringBuilder()
        if (isEn) {
            sb.appendLine("*WALI ASUH ACCESS CARDS LIST*")
            sb.appendLine("_${schoolProfile.schoolName} [${activeSchoolCode}]_")
            sb.appendLine("Date: ${DateUtils.formatFullDate(System.currentTimeMillis(), appLanguage)}")
            sb.appendLine("Total: ${schoolAccounts.size} Registered Accounts")
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
            sb.appendLine("_Managed by Administrator (${schoolProfile.adminName})_")
        } else {
            sb.appendLine("*DAFTAR KARTU AKSES WALI ASUH*")
            sb.appendLine("_${schoolProfile.schoolName} [${activeSchoolCode}]_")
            sb.appendLine("Tanggal: ${DateUtils.formatFullDate(System.currentTimeMillis(), appLanguage)}")
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
        }
        val summaryText = sb.toString().trim()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summaryText)
        }
        try {
            context.startActivity(
                Intent.createChooser(
                    intent,
                    if (isEn) "Share Wali Asuh Account Summary" else "Bagikan Rekap Akun Wali Asuh"
                )
            )
        } catch (e: Exception) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(
                ClipData.newPlainText(
                    if (isEn) "Wali Asuh Account Summary" else "Rekap Akun Wali Asuh",
                    summaryText
                )
            )
            Toast.makeText(
                context,
                if (isEn) "Account summary copied to clipboard!" else "Rekap akun disalin ke clipboard!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun triggerExportStudentsCsv() {
        if (schoolStudents.isEmpty()) {
            Toast.makeText(
                context,
                if (isEn) "No student data to export yet." else "Belum ada data siswa untuk diekspor.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val dateSlug = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val codeSlug = activeSchoolCode.lowercase(Locale.US).replace(Regex("[^a-z0-9_-]"), "_")
            exportStudentsCsvLauncher.launch("data_siswa_${codeSlug}_$dateSlug.csv")
        }
    }

    fun triggerExportWaliCsv() {
        if (schoolAccounts.isEmpty()) {
            Toast.makeText(
                context,
                if (isEn) "No Wali Asuh accounts to export yet." else "Belum ada akun wali asuh untuk diekspor.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val dateSlug = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val codeSlug = activeSchoolCode.lowercase(Locale.US).replace(Regex("[^a-z0-9_-]"), "_")
            exportWaliCsvLauncher.launch("data_wali_${codeSlug}_$dateSlug.csv")
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
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Admin Portal" else "Portal Admin",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${schoolProfile.schoolName.ifBlank { schoolProfile.shortName }} • ${schoolProfile.city}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val nextLang = if (appLanguage == AppLanguage.ENGLISH) {
                                AppLanguage.INDONESIAN
                            } else {
                                AppLanguage.ENGLISH
                            }
                            viewModel.setAppLanguage(nextLang)
                        },
                        modifier = Modifier
                            .size(width = 52.dp, height = 48.dp)
                            .testTag("btn_admin_toggle_language")
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = appLanguage.shortBadge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    // Tombol Info-Info Penting (Global Channel)
                    IconButton(
                        onClick = { showInfoPentingSheet = true },
                        modifier = Modifier.testTag("btn_admin_info_penting")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = if (isEn) "Important Info Channel" else "Channel Info-Info Penting",
                                tint = Color.White
                            )
                            if (unreadInfoPentingCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                        .border(1.dp, Color.White, CircleShape)
                                )
                            }
                        }
                    }
                    // Tombol Bagikan Rekap Seluruh Akun
                    IconButton(
                        onClick = { shareAllAccountsSummary() },
                        modifier = Modifier.testTag("btn_admin_share_all")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = if (isEn) "Share All Accounts Summary" else "Bagikan Rekap Seluruh Akun",
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
                            contentDescription = if (isEn) "Sign Out / Logout" else "Keluar / Logout",
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
                        2 -> showCreateFastingSessionDialog = true
                        3 -> {
                            visitSessionToEdit = null
                            showCreateVisitSessionDialog = true
                        }
                        else -> showImportJadwalMatriks = true
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
                            3 -> Icons.Default.CalendarMonth
                            4 -> Icons.Default.TableChart
                            else -> Icons.Default.Add
                        },
                        contentDescription = when (selectedAdminTab) {
                            0 -> if (isEn) "Add Student" else "Tambah Siswa"
                            2 -> if (isEn) "Create Puasa Session" else "Buat Sesi Puasa"
                            3 -> if (isEn) "Create Visit Session" else "Buat Sesi Kunjungan"
                            4 -> if (isEn) "Manage Roster" else "Atur Matriks Jadwal"
                            else -> if (isEn) "Add Wali Asuh" else "Tambah Wali Asuh"
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (selectedAdminTab) {
                            0 -> if (isEn) "Add Student" else "Tambah Siswa"
                            2 -> if (isEn) "Create Puasa Session" else "Buat Sesi Puasa"
                            3 -> if (isEn) "Create Visit Session" else "Buat Kunjungan"
                            4 -> if (isEn) "Manage Roster" else "Atur Jadwal"
                            else -> if (isEn) "Add Wali Asuh" else "Tambah Wali Asuh"
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
                    .widthIn(max = 680.dp)
                    .testTag("list_admin_content"),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Kartu Hero Ringkas Sekolah (senada gaya Beranda Wali Asuh)
                item(key = "admin_hero_card") {
                    AdminHeroSummaryCard(
                        schoolProfile = schoolProfile,
                        activeSchoolCode = activeSchoolCode,
                        studentsCount = schoolStudents.size,
                        waliCount = schoolAccounts.size,
                        jadwalCount = jadwalMonthEntries.size,
                        sessionsCount = schoolFastingSessions.size + schoolVisitSessions.size,
                        isSyncing = isSyncing,
                        isEn = isEn,
                        onSyncCloud = {
                            viewModel.syncAllWithCloud { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        onEditSchool = { showEditSchoolDialog = true },
                        onSelectMetricTab = { tabIdx -> selectedAdminTab = tabIdx }
                    )
                }

                // 2. Grid Kartu Menu Pengelola 2x4 (senada kartu QuickMenuTiles Wali Asuh)
                item(key = "admin_menu_hub_card") {
                    AdminQuickMenuGridCard(
                        selectedTab = selectedAdminTab,
                        studentsCount = schoolStudents.size,
                        waliCount = schoolAccounts.size,
                        jadwalCount = jadwalMonthEntries.size,
                        activeFastingCount = schoolFastingSessions.count { it.isActive },
                        activeVisitCount = schoolVisitSessions.count { it.isActive },
                        isEn = isEn,
                        onSelectTab = { tabIdx -> selectedAdminTab = tabIdx },
                        onOpenSchoolProfile = { showEditSchoolDialog = true },
                        onOpenCsvBackupSheet = { showCsvBackupSheet = true },
                        onOpenSecuritySheet = { showSecurityMaintenanceSheet = true }
                    )
                }

                // Tab Navigasi: Kelola Data Siswa vs Akun Wali vs Sesi Puasa vs Kunjungan
                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedAdminTab,
                        edgePadding = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .testTag("row_admin_tabs"),
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
                                        text = if (isEn) "Students (${schoolStudents.size})" else "Siswa (${schoolStudents.size})",
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
                                        text = if (isEn) "Puasa (${schoolFastingSessions.size})" else "Sesi Puasa (${schoolFastingSessions.size})",
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = if (selectedAdminTab == 2) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_admin_sesi_puasa")
                        )
                        Tab(
                            selected = selectedAdminTab == 3,
                            onClick = { selectedAdminTab = 3 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEn) "Visits (${schoolVisitSessions.size})" else "Kunjungan (${schoolVisitSessions.size})",
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = if (selectedAdminTab == 3) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_admin_visit")
                        )
                        Tab(
                            selected = selectedAdminTab == 4,
                            onClick = { selectedAdminTab = 4 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TableChart,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEn) "Roster (${jadwalMonthEntries.size})" else "Jadwal (${jadwalMonthEntries.size})",
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = if (selectedAdminTab == 4) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_admin_jadwal")
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
                                        text = if (isEn) "Manage Master Student Data" else "Kelola Master Data Siswa",
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
                                            text = if (isEn) "${schoolStudents.size} Registered Students" else "${schoolStudents.size} Siswa Terdaftar",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A)
                                        )
                                    }
                                }

                                Text(
                                    text = if (isEn) {
                                        "Complete list of students in this school. Admin can search students, view medical records, register new students, or delete transferred/graduated students."
                                    } else {
                                        "Daftar lengkap anak asuh sekolah ini. Admin dapat mencari anak, melihat riwayat medis, mendaftarkan anak baru, atau menghapus satu anak yang keluar/pindah."
                                    },
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
                                            Text(if (isEn) "Male Students" else "Siswa Putra", fontSize = 10.5.sp, color = Color(0xFF0369A1))
                                            Text(if (isEn) "$studentPutraCount Students" else "$studentPutraCount Siswa", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                        }
                                    }
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE7F3)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(if (isEn) "Female Students" else "Siswa Putri", fontSize = 10.5.sp, color = Color(0xFFBE185D))
                                            Text(if (isEn) "$studentPutriCount Students" else "$studentPutriCount Siswa", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBE185D))
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showImportStudentsDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f).testTag("btn_quick_import_students")
                                    ) {
                                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF16A34A))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(if (isEn) "Import CSV" else "Import CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF166534))
                                    }
                                    OutlinedButton(
                                        onClick = { triggerExportStudentsCsv() },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f).testTag("btn_quick_export_students")
                                    ) {
                                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF0F766E))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(if (isEn) "Export CSV" else "Export CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F766E))
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
                                placeholder = {
                                    Text(
                                        text = if (isEn) "Search name, NIK, KK, NISN, class, mother..." else "Cari nama, NIK, KK, NISN, kelas, ibu...",
                                        fontSize = 12.sp
                                    )
                                },
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
                                                "Putra" -> if (isEn) "Male ($studentPutraCount)" else "Putra ($studentPutraCount)"
                                                "Putri" -> if (isEn) "Female ($studentPutriCount)" else "Putri ($studentPutriCount)"
                                                else -> if (isEn) "All (${schoolStudents.size})" else "Semua (${schoolStudents.size})"
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
                                        text = if (studentSearchQuery.isNotBlank()) {
                                            if (isEn) {
                                                "No student found matching '$studentSearchQuery'."
                                            } else {
                                                "Siswa dengan kata kunci '$studentSearchQuery' tidak ditemukan."
                                            }
                                        } else {
                                            if (isEn) {
                                                "No students in this school yet. You can import a student CSV file or press '+ Add Student'."
                                            } else {
                                                "Belum ada data siswa di sekolah ini. Anda dapat mengimpor file CSV siswa atau menekan '+ Tambah Siswa'."
                                            }
                                        },
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
                                onManageTandem = { studentForManageTandem = student },
                                onReleaseWali = if (student.assignedWaliName.isNotBlank() || student.assignedWaliUsername.isNotBlank()) {
                                    {
                                        viewModel.releaseStudentClaim(student) { _, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else null,
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
                                    text = if (isEn) "Manage Wali Asuh Accounts" else "Kelola Akun Wali Asuh",
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
                                        text = if (isEn) "${schoolAccounts.size} Registered Wali" else "${schoolAccounts.size} Wali Terdaftar",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WaliPrimary
                                    )
                                }
                            }

                            Text(
                                text = if (isEn) {
                                    "Each Wali Asuh receives an official Access Card with a random username & password that can be shared directly via WhatsApp."
                                } else {
                                    "Setiap wali asuh memiliki Kartu Akun login resmi dengan username & password acak yang dapat dibagikan langsung ke WhatsApp."
                                },
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
                                        Text(if (isEn) "$putraCount Accounts" else "$putraCount Akun", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                    }
                                }
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE7F3)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Asrama Putri", fontSize = 10.5.sp, color = Color(0xFFBE185D))
                                        Text(if (isEn) "$putriCount Accounts" else "$putriCount Akun", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBE185D))
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEEF2FF),
                                border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showManageAnakAdminDialog = true }
                                    .testTag("card_admin_anak_admin_quota")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = Color(0xFF4338CA),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isEn) {
                                                "Schedule Sub-Admin Feature ($anakAdminCount/${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL})"
                                            } else {
                                                "Fitur Anak Admin Jadwal ($anakAdminCount/${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL} Wali)"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF312E81)
                                        )
                                        Text(
                                            text = if (isEn) {
                                                "Pin up to ${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL} Wali Asuh in this school to directly change shifts, pin hospital/extra tasks, & upload/download CSVs inside the app."
                                            } else {
                                                "Sematkan ke maks. ${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL} Wali Asuh agar dapat klik-edit shift, pin tugas (RS/Asrama), serta upload & download CSV langsung di aplikasi."
                                            },
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF3730A3),
                                            lineHeight = 14.sp
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { showManageAnakAdminDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFF4338CA)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .heightIn(min = 38.dp)
                                            .testTag("btn_admin_open_anak_admin_picker")
                                    ) {
                                        Text(
                                            text = if (isEn) "Select" else "Pilih",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4338CA)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showImportWaliDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_quick_import_wali")
                                ) {
                                    Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF2563EB))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(if (isEn) "Import Wali CSV" else "Import Wali CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D4ED8))
                                }
                                OutlinedButton(
                                    onClick = { triggerExportWaliCsv() },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_quick_export_wali")
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF1D4ED8))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(if (isEn) "Export Wali CSV" else "Export Wali CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D4ED8))
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
                            placeholder = { Text(if (isEn) "Search Wali Asuh name..." else "Cari nama wali asuh...", fontSize = 12.sp) },
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("Semua", "Putra", "Putri", "Anak Admin", "Guru Ngaji").forEach { dormFilter ->
                                val chipLabel = when (dormFilter) {
                                    "Semua" -> if (isEn) "All" else "Semua"
                                    "Anak Admin" -> if (isEn) "Sub-Admin ($anakAdminCount)" else "Anak Admin ($anakAdminCount)"
                                    "Guru Ngaji" -> if (isEn) "Mengaji Teacher" else "Guru Ngaji"
                                    else -> dormFilter
                                }
                                FilterChip(
                                    selected = selectedFilterDormitory == dormFilter,
                                    onClick = { selectedFilterDormitory = dormFilter },
                                    label = { Text(chipLabel, fontSize = 11.sp) },
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
                                    text = if (searchQuery.isNotBlank()) {
                                        if (isEn) {
                                            "No Wali Asuh found matching '$searchQuery'."
                                        } else {
                                            "Wali asuh dengan kata kunci '$searchQuery' tidak ditemukan."
                                        }
                                    } else {
                                        if (isEn) {
                                            "No Wali Asuh accounts registered yet. Press '+ Add Wali Asuh' to register."
                                        } else {
                                            "Belum ada akun wali asuh yang terdaftar. Tekan tombol '+ Tambah Wali Asuh' untuk mendaftarkan."
                                        }
                                    },
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
                                Toast.makeText(
                                    context,
                                    if (isEn) "$label copied!" else "$label berhasil disalin!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onShareWhatsApp = {
                                val message = PasswordGenerator.formatWhatsAppMessage(account, schoolProfile, isEn)
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
                                    context.startActivity(
                                        Intent.createChooser(
                                            sendIntent,
                                            if (isEn) "Share Wali Asuh Account" else "Bagikan Akun Wali Asuh"
                                        )
                                    )
                                }
                            },
                            onEdit = { accountToEdit = account },
                            anakAdminCount = anakAdminCount,
                            onToggleAnakAdmin = { enable ->
                                viewModel.setWaliAnakAdminStatus(account, enable) { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            onResetPassword = {
                                viewModel.resetWaliPassword(account) { newPass ->
                                    Toast.makeText(
                                        context,
                                        if (isEn) "New password for ${account.name}: $newPass" else "Password baru untuk ${account.name}: $newPass",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onDelete = { accountToDelete = account }
                        )
                    }
                }
                } else if (selectedAdminTab == 2) {
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
                                    text = if (isEn) "Student Puasa Session Management" else "Manajemen Sesi Puasa Siswa",
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
                                        text = if (isEn) {
                                            "${schoolFastingSessions.count { it.isActive }} Active"
                                        } else {
                                            "${schoolFastingSessions.count { it.isActive }} Sesi Aktif"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F766E)
                                    )
                                }
                            }

                            Text(
                                text = if (isEn) {
                                    "Wali Asuh and School Administrators can create Puasa sessions (e.g., Puasa Senin Kamis). Only School Administrators can delete or close Puasa sessions."
                                } else {
                                    "Wali Asuh dan Admin Sekolah dapat membuat sesi puasa (contoh: Puasa Senin Kamis, 27 Oktober 2026). Penghapusan atau penutupan sesi puasa hanya dapat dilakukan oleh Admin Sekolah."
                                },
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
                                Text(
                                    text = if (isEn) "Create New Puasa Session" else "Buat Sesi Puasa Baru",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
                                    text = if (isEn) {
                                        "No Puasa sessions yet. Press '+ Create New Puasa Session' to start tracking student Puasa attendance."
                                    } else {
                                        "Belum ada sesi puasa. Tekan '+ Buat Sesi Puasa Baru' untuk membuka sesi pendataan puasa siswa."
                                    },
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
                                            text = DateUtils.formatFullDate(session.sessionDate, appLanguage),
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF0F766E),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (session.isActive) {
                                                if (isEn) "Active" else "Aktif"
                                            } else {
                                                if (isEn) "Finished" else "Selesai"
                                            },
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
                                        text = if (isEn) {
                                            "Participants: ${recs.size} Students ($sma SMA • $smp SMP • $sd SD)"
                                        } else {
                                            "Peserta: ${recs.size} Siswa ($sma SMA • $smp SMP • $sd SD)"
                                        },
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
                                            contentDescription = if (isEn) "Delete Puasa Session" else "Hapus Sesi Puasa",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                } else if (selectedAdminTab == 3) {
                    // ==========================================
                    // TAB 3: KELOLA SESI KUNJUNGAN & KECAMATAN PESERTA
                    // ==========================================
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = Color(0xFF1D4ED8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isEn) "Parent Visit Sessions" else "Sesi Kunjungan Bulanan",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFDBEAFE))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (isEn) {
                                                "${schoolVisitSessions.count { it.isActive }} Active"
                                            } else {
                                                "${schoolVisitSessions.count { it.isActive }} Sesi Aktif"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1D4ED8)
                                        )
                                    }
                                }

                                Text(
                                    text = if (isEn) {
                                        "Create 1–4 visit sessions per month with a distinct date per session and exclusive districts (Kecamatan) so parents know their exact visit schedule."
                                    } else {
                                        "Buat 1–4 sesi kunjungan per bulan dengan tanggal berbeda di tiap sesi serta pembagian kecamatan eksklusif agar jadwal tampil otomatis di Portal Orang Tua."
                                    },
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = {
                                        visitSessionToEdit = null
                                        showCreateVisitSessionDialog = true
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_admin_create_visit_session")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEn) "Create Monthly Visit Session" else "Buat Sesi Kunjungan Baru",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (schoolVisitSessions.isEmpty()) {
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
                                        text = if (isEn) {
                                            "No visit sessions configured yet. Tap '+ Create Monthly Visit Session' to assign dates and participating districts."
                                        } else {
                                            "Belum ada sesi kunjungan bulanan. Tekan '+ Buat Sesi Kunjungan Baru' untuk memilih multi-tanggal & kecamatan pesertanya."
                                        },
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        val visitDateFmt = SimpleDateFormat("EEE, d MMM", appLanguage.locale)
                        items(schoolVisitSessions, key = { "admin_visit_${it.id}" }) { session ->
                            val scheduledList = VisitHelper.scheduledStudentsForSession(session, schoolStudents)
                            val sessionRecs = visitRecords.filter { it.sessionId == session.id }
                            val datesList = session.getVisitDatesList()
                            val districtsList = session.getDistrictsList()

                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_visit_session_${session.id}"),
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
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = VisitHelper.formatMonthYearLabel(session.monthYear, appLanguage.locale),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1D4ED8)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (session.isActive) {
                                                    if (isEn) "Active" else "Aktif"
                                                } else {
                                                    if (isEn) "Closed" else "Selesai"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (session.isActive) Color(0xFF15803D) else Color(0xFF64748B)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Switch(
                                                checked = session.isActive,
                                                onCheckedChange = { viewModel.toggleVisitSessionActive(session) }
                                            )
                                        }
                                    }

                                    if (datesList.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            datesList.forEach { d ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFEFF6FF))
                                                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = visitDateFmt.format(Date(d)),
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF1D4ED8)
                                                    )
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFFEF3C7))
                                                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = session.displayTimeRange(),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF92400E)
                                                )
                                            }
                                        }
                                    }

                                    if (districtsList.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            districtsList.forEach { dist ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFCCFBF1))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(12.dp))
                                                        Text(
                                                            text = VisitHelper.displayDistrict(dist, isEn),
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = Color(0xFF0F766E)
                                                        )
                                                    }
                                                }
                                            }
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
                                            text = if (isEn) {
                                                "Scheduled: ${scheduledList.size} Students • Attended: ${sessionRecs.size}"
                                            } else {
                                                "Terjadwal: ${scheduledList.size} Siswa • Hadir: ${sessionRecs.size}"
                                            },
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    visitSessionToEdit = session
                                                    showCreateVisitSessionDialog = true
                                                },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .testTag("btn_edit_visit_session_${session.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = if (isEn) "Edit Visit Session" else "Edit Sesi Kunjungan",
                                                    tint = Color(0xFF1D4ED8),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { visitSessionToDelete = session },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = if (isEn) "Delete Visit Session" else "Hapus Sesi Kunjungan",
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
                } else {
                    // ==========================================
                    // TAB 4: KELOLA JADWAL WALI ASUH & KODE SHIFT SEKOLAH
                    // ==========================================
                    val adminShiftMap = WaliScheduleHelper.buildShiftMap(schoolShiftCodes, activeSchoolCode)
                    val adminDaysInMonth = WaliScheduleHelper.daysInMonth(jadwalMonthKey)
                    val adminSafeDay = jadwalSelectedDay.coerceIn(1, adminDaysInMonth)
                    val adminActorName = schoolProfile.adminName.ifBlank { waliName.ifBlank { "Admin Sekolah" } }

                    item(key = "admin_jadwal_header") {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.TableChart,
                                            contentDescription = null,
                                            tint = Color(0xFF0F766E),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (isEn) "Wali Asuh Duty Roster" else "Jadwal Wali Asuh Sekolah",
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (isEn) {
                                                    "School-only management • Shift codes, pins & matrix PDF (no alarms)"
                                                } else {
                                                    "Dikelola Admin Sekolah • Kode shift & pin tugas bisa disesuaikan"
                                                },
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                jadwalMonthKey = WaliScheduleHelper.shiftMonth(jadwalMonthKey, -1)
                                                val maxD = WaliScheduleHelper.daysInMonth(jadwalMonthKey)
                                                if (jadwalSelectedDay > maxD) jadwalSelectedDay = maxD
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                contentDescription = if (isEn) "Previous Month" else "Bulan Sebelumnya",
                                                tint = Color(0xFF0F766E)
                                            )
                                        }
                                        Text(
                                            text = WaliScheduleHelper.formatMonthTitle(jadwalMonthKey, isEn),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F766E)
                                        )
                                        IconButton(
                                            onClick = {
                                                jadwalMonthKey = WaliScheduleHelper.shiftMonth(jadwalMonthKey, 1)
                                                val maxD = WaliScheduleHelper.daysInMonth(jadwalMonthKey)
                                                if (jadwalSelectedDay > maxD) jadwalSelectedDay = maxD
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = if (isEn) "Next Month" else "Bulan Berikutnya",
                                                tint = Color(0xFF0F766E)
                                            )
                                        }
                                    }
                                }

                                // Strip Pilih Tanggal 1..daysInMonth
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(adminDaysInMonth) { idx ->
                                        val dayNum = idx + 1
                                        val isSel = dayNum == adminSafeDay
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSel) Color(0xFF0F766E) else Color(0xFFF1F5F9),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSel) Color(0xFF0F766E) else Color(0xFFCBD5E1)
                                            ),
                                            modifier = Modifier
                                                .widthIn(min = 48.dp)
                                                .clickable { jadwalSelectedDay = dayNum }
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = WaliScheduleHelper.dayOfWeekShort(jadwalMonthKey, dayNum, isEn),
                                                    fontSize = 9.5.sp,
                                                    color = if (isSel) Color.White.copy(alpha = 0.9f) else Color(0xFF64748B)
                                                )
                                                Text(
                                                    text = dayNum.toString(),
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSel) Color.White else Color(0xFF0F172A)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Pintasan Pengaturan Anak Admin Jadwal (Maks 7 Wali Asuh per Sekolah)
                                val pinnedNamesPreview = schoolAccounts
                                    .filter { it.isAnakAdminSekolah() }
                                    .joinToString(", ") { it.name }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEEF2FF),
                                    border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showManageAnakAdminDialog = true }
                                        .testTag("card_admin_jadwal_anak_admin_banner")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = Color(0xFF4338CA),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isEn) {
                                                    "Schedule Sub-Admins ($anakAdminCount/${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL})"
                                                } else {
                                                    "Anak Admin Jadwal ($anakAdminCount/${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL} Wali)"
                                                },
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF312E81)
                                            )
                                            Text(
                                                text = pinnedNamesPreview.ifBlank {
                                                    if (isEn) "Tap to pin up to ${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL} Wali Asuh to edit shifts, pins & CSVs."
                                                    else "Ketuk untuk sematkan maks. ${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL} Wali Asuh agar bisa ubah jadwal & CSV."
                                                },
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF4338CA),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Button(
                                            onClick = { showManageAnakAdminDialog = true },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .heightIn(min = 40.dp)
                                                .testTag("btn_admin_manage_anak_admin_from_jadwal")
                                        ) {
                                            Text(
                                                text = if (isEn) "Pin / Set" else "Sematkan",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Barisan Tombol Kelola Admin
                                @OptIn(ExperimentalLayoutApi::class)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { showJadwalMatriksSheet = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("btn_admin_jadwal_matriks")
                                    ) {
                                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(if (isEn) "Full Matrix" else "Matriks Lengkap", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { showManageShiftCodes = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("btn_admin_manage_shift_codes")
                                    ) {
                                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(if (isEn) "Shift Codes" else "Kode Shift (${schoolShiftCodes.size})", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            importJadwalInitialTab = 0
                                            showImportJadwalMatriks = true
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("btn_admin_import_jadwal")
                                    ) {
                                        Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF0F766E))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(if (isEn) "Roster / Pattern" else "Personel / Pola", fontSize = 11.5.sp, color = Color(0xFF0F766E), fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val safeCode = activeSchoolCode.replace(Regex("[^A-Za-z0-9_-]"), "_")
                                            downloadJadwalTemplateCsvLauncher.launch("Template_Jadwal_Wali_${safeCode}_${jadwalMonthKey}.csv")
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("btn_admin_download_jadwal_csv_template")
                                    ) {
                                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF0F766E))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(if (isEn) "Template CSV" else "Template CSV", fontSize = 11.5.sp, color = Color(0xFF0F766E), fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            importJadwalInitialTab = 2
                                            showImportJadwalMatriks = true
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("btn_admin_upload_jadwal_csv")
                                    ) {
                                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF0369A1))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(if (isEn) "Upload CSV" else "Upload CSV", fontSize = 11.5.sp, color = Color(0xFF0369A1), fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { showPenyematanTugas = true },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("btn_admin_pin_tugas")
                                    ) {
                                        Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF4338CA))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(if (isEn) "Pinned Tasks" else "Pin Tugas", fontSize = 11.5.sp, color = Color(0xFF4338CA), fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { showPetugasJamIni = true },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("btn_admin_petugas_jam_ini")
                                    ) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF0284C7))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(if (isEn) "Active Now" else "Jam Ini", fontSize = 11.5.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val pdf = PdfReportGenerator.generateJadwalMatriksPdf(
                                                context = context,
                                                monthKey = jadwalMonthKey,
                                                entries = jadwalMonthEntries,
                                                shiftCodes = schoolShiftCodes,
                                                waliName = adminActorName,
                                                schoolProfile = schoolProfile
                                            )
                                            if (pdf != null) {
                                                readyAdminJadwalPdf = pdf
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    if (isEn) "Failed to generate duty roster PDF." else "Gagal membuat PDF matriks jadwal.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("btn_admin_export_jadwal_pdf")
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFFB91C1C))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(if (isEn) "Matrix PDF" else "PDF Matriks", fontSize = 11.5.sp, color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val waText = WaliScheduleHelper.buildDailyWhatsappSummary(
                                                monthKey = jadwalMonthKey,
                                                day = adminSafeDay,
                                                schoolName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
                                                entries = jadwalMonthEntries,
                                                shiftMap = adminShiftMap,
                                                isEn = isEn
                                            )
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, waText)
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            runCatching {
                                                context.startActivity(
                                                    Intent.createChooser(
                                                        sendIntent,
                                                        if (isEn) "Share Daily Roster" else "Bagikan Jadwal Harian"
                                                    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("btn_admin_share_jadwal_wa")
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF15803D))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text("WhatsApp", fontSize = 11.5.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Rekan Izin Banner
                    val adminLeavesToday = jadwalMonthEntries.mapNotNull { entry ->
                        val leave = WaliScheduleHelper.parseDayLeaves(entry.dayLeaveJson)[adminSafeDay]
                        val code = WaliScheduleHelper.normalizeCode(WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)[adminSafeDay].orEmpty())
                        if (leave != null || code == "IZIN" || code == "C") entry to leave else null
                    }
                    if (adminLeavesToday.isNotEmpty()) {
                        item(key = "admin_jadwal_izin_banner") {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFF1F2),
                                border = BorderStroke(1.dp, Color(0xFFFDA4AF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isEn) "Leave / Sick Today (${adminLeavesToday.size}):" else "Izin / Sakit Hari Ini (${adminLeavesToday.size}):",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFBE123C)
                                        )
                                        TextButton(onClick = { showRekapIzinJadwal = true }) {
                                            Text(if (isEn) "View Recap" else "Rekap Izin", fontSize = 11.sp, color = Color(0xFFBE123C))
                                        }
                                    }
                                    adminLeavesToday.forEach { (entry, leave) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${entry.waliName} (${leave?.status ?: "IZIN"})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (leave != null && leave.driveUrl.isNotBlank()) {
                                                TextButton(onClick = { openJadwalExternalUrl(context, leave.driveUrl, isEn) }) {
                                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF047857))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(if (isEn) "Drive" else "Buka Drive", fontSize = 11.sp, color = Color(0xFF047857))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (jadwalMonthEntries.isEmpty()) {
                        item(key = "admin_jadwal_empty") {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = if (isEn) {
                                            "No roster personnel listed for ${WaliScheduleHelper.formatMonthTitle(jadwalMonthKey, true)} yet."
                                        } else {
                                            "Belum ada daftar personel jadwal pada ${WaliScheduleHelper.formatMonthTitle(jadwalMonthKey, false)}."
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Pull all registered Wali Asuh accounts from this school automatically or paste a monthly CSV matrix."
                                        } else {
                                            "Tarik otomatis seluruh akun Wali Asuh sekolah ini atau tempel matriks CSV bulanan agar langsung terisi."
                                        },
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    @OptIn(ExperimentalLayoutApi::class)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.syncScheduleMembersFromWaliAccounts(jadwalMonthKey) { _, msg ->
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                            modifier = Modifier.testTag("btn_admin_sync_wali_jadwal")
                                        ) {
                                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isEn) "Sync Wali Asuh" else "Tarik Akun Wali", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val safeCode = activeSchoolCode.replace(Regex("[^A-Za-z0-9_-]"), "_")
                                                downloadJadwalTemplateCsvLauncher.launch("Template_Jadwal_Wali_${safeCode}_${jadwalMonthKey}.csv")
                                            }
                                        ) {
                                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0F766E))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isEn) "Template CSV" else "Template CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                importJadwalInitialTab = 2
                                                showImportJadwalMatriks = true
                                            }
                                        ) {
                                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0369A1))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isEn) "Upload CSV" else "Upload CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        val groupSections = listOf(
                            Triple(WaliScheduleHelper.GROUP_PAGI, if (isEn) "Morning Shift (Pagi)" else "Jaga Pagi (P1–P5)", Color(0xFF0284C7)),
                            Triple(WaliScheduleHelper.GROUP_SORE, if (isEn) "Afternoon Shift (Sore)" else "Jaga Sore (S)", Color(0xFFD97706)),
                            Triple(WaliScheduleHelper.GROUP_MALAM, if (isEn) "Night Shift (Malam)" else "Jaga Malam (M)", Color(0xFF7C3AED)),
                            Triple(WaliScheduleHelper.GROUP_LEPAS, if (isEn) "Off / Leave / Holiday" else "Lepas, Libur & Izin", Color(0xFF0D9488)),
                            Triple("UNASSIGNED", if (isEn) "Unassigned on This Day" else "Belum Diatur di Tanggal Ini", Color(0xFF64748B))
                        )
                        items(groupSections, key = { "admin_jadwal_group_${it.first}" }) { (groupKey, groupTitle, groupAccent) ->
                            val matchingEntries = jadwalMonthEntries.filter { entry ->
                                val rawCode = WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)[adminSafeDay].orEmpty()
                                val norm = WaliScheduleHelper.normalizeCode(rawCode)
                                when (groupKey) {
                                    "UNASSIGNED" -> norm.isBlank()
                                    WaliScheduleHelper.GROUP_LEPAS -> {
                                        val grp = WaliScheduleHelper.resolveShiftGroup(norm, adminShiftMap)
                                        norm.isNotBlank() && (grp == WaliScheduleHelper.GROUP_LEPAS || grp == WaliScheduleHelper.GROUP_IZIN)
                                    }
                                    else -> norm.isNotBlank() && WaliScheduleHelper.resolveShiftGroup(norm, adminShiftMap) == groupKey
                                }
                            }
                            if (matchingEntries.isNotEmpty()) {
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                                text = groupTitle,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = groupAccent
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(999.dp),
                                                color = groupAccent.copy(alpha = 0.14f)
                                            ) {
                                                Text(
                                                    text = if (isEn) "${matchingEntries.size} Staff" else "${matchingEntries.size} Org",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = groupAccent,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        matchingEntries.forEach { entry ->
                                            val dayCodesMap = WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)
                                            val dayPinsMap = WaliScheduleHelper.parseDayPins(entry.dayPinsJson)
                                            val code = WaliScheduleHelper.normalizeCode(dayCodesMap[adminSafeDay].orEmpty())
                                            val shiftObj = adminShiftMap[code]
                                            val pins = dayPinsMap[adminSafeDay].orEmpty()
                                            val chipColor = if (shiftObj != null) parseJadwalShiftColor(shiftObj.colorHex, groupAccent) else Color(0xFF94A3B8)

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFF8FAFC),
                                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { editingJadwalCellTarget = entry to adminSafeDay }
                                                    .testTag("admin_jadwal_row_${entry.id}")
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = entry.waliName,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            if (shiftObj != null) {
                                                                Text(
                                                                    text = listOfNotNull(
                                                                        shiftObj.zoneLabel.takeIf { it.isNotBlank() },
                                                                        shiftObj.timeRange.takeIf { it.isNotBlank() }
                                                                    ).joinToString(" • "),
                                                                    fontSize = 10.5.sp,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            Surface(
                                                                shape = RoundedCornerShape(8.dp),
                                                                color = chipColor
                                                            ) {
                                                                Text(
                                                                    text = code.ifBlank { "+" },
                                                                    fontSize = 11.5.sp,
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    color = Color.White,
                                                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                                                )
                                                            }
                                                            Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = if (isEn) "Edit Shift Cell" else "Ubah Shift & Pin",
                                                                tint = groupAccent,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                    if (pins.isNotEmpty()) {
                                                        @OptIn(ExperimentalLayoutApi::class)
                                                        FlowRow(
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            pins.forEach { pin ->
                                                                SchedulePinChip(pin = pin)
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
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddWaliAccountDialog(
            schoolName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
            anakAdminCount = anakAdminCount,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, username, dormitory, phone, password, role ->
                viewModel.addWaliAccount(
                    name = name,
                    username = username,
                    dormitory = dormitory,
                    phone = phone,
                    customPassword = password,
                    role = role,
                    onError = { errMsg ->
                        Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                    },
                    onComplete = {
                        Toast.makeText(
                            context,
                            if (isEn) "Access card for $name created!" else "Kartu akun untuk $name berhasil dibuat!",
                            Toast.LENGTH_SHORT
                        ).show()
                        showAddDialog = false
                    }
                )
            }
        )
    }

    if (showAddStudentDialog) {
        AddStudentDialog(
            activeSchoolId = activeSchoolCode,
            onDismiss = { showAddStudentDialog = false },
            onConfirmStudentWithPhoto = { newStudent, pickedPhotoUri, _ ->
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
                    district = newStudent.district,
                    roomNumber = newStudent.roomNumber,
                    jenjang = newStudent.jenjang,
                    parentPhone = newStudent.parentPhone,
                    onCreated = { inserted ->
                        if (pickedPhotoUri != null) {
                            viewModel.uploadStudentProfilePhoto(inserted, pickedPhotoUri) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                Toast.makeText(
                    context,
                    if (isEn) {
                        "Student ${newStudent.name} registered to ${schoolProfile.shortName}!"
                    } else {
                        "Siswa ${newStudent.name} berhasil didaftarkan ke ${schoolProfile.shortName}!"
                    },
                    Toast.LENGTH_SHORT
                ).show()
                showAddStudentDialog = false
            }
        )
    }

    studentToEdit?.let { editingStudent ->
        AddStudentDialog(
            initialStudent = editingStudent,
            activeSchoolId = activeSchoolCode,
            onDismiss = { studentToEdit = null },
            onConfirmStudentWithPhoto = { updated, pickedPhotoUri, removePhoto ->
                viewModel.updateStudent(updated) {
                    Toast.makeText(
                        context,
                        if (isEn) "Profile for ${updated.name} updated!" else "Profil ${updated.name} berhasil diperbarui!",
                        Toast.LENGTH_SHORT
                    ).show()
                    when {
                        pickedPhotoUri != null -> {
                            viewModel.uploadStudentProfilePhoto(updated, pickedPhotoUri) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                        removePhoto -> {
                            viewModel.removeStudentProfilePhoto(updated)
                        }
                    }
                }
                studentToEdit = null
            }
        )
    }

    selectedDetailStudent?.let { selected ->
        val student = students.firstOrNull { it.id == selected.id } ?: selected
        StudentDetailDialog(
            student = student,
            studentRecords = allRecords.filter { it.studentId == student.id },
            schoolProfile = schoolProfile,
            waliName = schoolProfile.adminName.ifBlank { waliName },
            mengajiRecords = MengajiHelper.recordsForStudent(mengajiRecords, student),
            fastingRecords = fastingRecords,
            sholatRecords = sholatRecords,
            haidRecords = haidRecords,
            onInputMengaji = { target ->
                selectedDetailStudent = null
                studentForMengajiInput = target
            },
            onPrintKartuMengaji = { target ->
                selectedDetailStudent = null
                studentForKartuMengaji = target
            },
            onEditStudent = { toEdit ->
                selectedDetailStudent = null
                studentToEdit = toEdit
            },
            onDeleteMengajiRecord = { rec -> viewModel.deleteMengajiRecord(rec) },
            onDeleteMedicalRecord = { rec -> viewModel.deleteRecord(rec) },
            onDeleteFastingRecord = { rec -> viewModel.deleteFastingRecord(rec) },
            onDeleteSholatRecord = { rec -> viewModel.deleteSholatRecord(rec) },
            isUploadingPhoto = isUploadingStudentPhoto,
            onUploadStudentPhoto = { target, uri, cb ->
                viewModel.uploadStudentProfilePhoto(target, uri, cb)
            },
            onRemoveStudentPhoto = { target, cb ->
                viewModel.removeStudentProfilePhoto(target, cb)
            },
            onDismiss = { selectedDetailStudent = null }
        )
    }

    studentForMengajiInput?.let { target ->
        InputMengajiDialog(
            students = schoolStudents,
            mengajiRecords = mengajiRecords,
            initialStudent = target,
            onSave = { st, dateMillis, bookType, jilidOrJuz, pageOrVerse, fluencyStatus, notes, existingId ->
                viewModel.saveMengajiRecord(
                    student = st,
                    sessionDate = dateMillis,
                    bookType = bookType,
                    jilidOrJuz = jilidOrJuz,
                    pageOrVerse = pageOrVerse,
                    fluencyStatus = fluencyStatus,
                    notes = notes,
                    existingRecordId = existingId
                ) { _, _ ->
                    Toast.makeText(
                        context,
                        if (isEn) {
                            "Mengaji saved for ${st.name}: $jilidOrJuz • $pageOrVerse"
                        } else {
                            "Setoran mengaji ${st.name} tersimpan: $jilidOrJuz • $pageOrVerse"
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
                studentForMengajiInput = null
            },
            onDismiss = { studentForMengajiInput = null }
        )
    }

    studentForKartuMengaji?.let { target ->
        KartuMengajiSheet(
            students = schoolStudents,
            mengajiRecords = mengajiRecords,
            schoolProfile = schoolProfile,
            waliName = schoolProfile.adminName.ifBlank { waliName },
            initialStudent = target,
            onOpenInputForStudent = { st ->
                studentForKartuMengaji = null
                studentForMengajiInput = st
            },
            onDismiss = { studentForKartuMengaji = null }
        )
    }

    studentForManageTandem?.let { targetStudent ->
        val freshStudent = schoolStudents.firstOrNull { it.id == targetStudent.id } ?: targetStudent
        AdminManageTandemDialog(
            student = freshStudent,
            waliAccounts = schoolAccounts.filter { !it.isGuruNgaji() },
            isEn = isEn,
            onSaveTeam = { primaryAcc, tandemAccs ->
                viewModel.adminAssignPrimaryAndTandems(
                    student = freshStudent,
                    primaryWali = primaryAcc,
                    tandemWalis = tandemAccs
                ) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
                studentForManageTandem = null
            },
            onDismiss = { studentForManageTandem = null }
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
                    text = if (isEn) "Delete Student Data?" else "Hapus Data Siswa?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isEn) "Are you sure you want to delete this student's data:" else "Apakah Anda yakin ingin menghapus data siswa:",
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
                                text = if (isEn) {
                                    "NIS: ${student.nis.ifBlank { "-" }} • ${student.dormitory} • Class ${student.classGrade}"
                                } else {
                                    "NIS: ${student.nis.ifBlank { "-" }} • ${student.dormitory} • Kelas ${student.classGrade}"
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = if (isEn) {
                            "This action is permanent. The student will be removed from this school and all related medical records/plans will be cleared."
                        } else {
                            "Tindakan ini permanen. Siswa akan dihapus dari sekolah ini dan seluruh riwayat berobat/rencana terkait akan dibersihkan."
                        },
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
                        Toast.makeText(
                            context,
                            if (isEn) "Student $studentName deleted from system." else "Siswa $studentName berhasil dihapus dari sistem.",
                            Toast.LENGTH_SHORT
                        ).show()
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_delete_student")
                ) {
                    Text(if (isEn) "Delete Student" else "Hapus Siswa", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { studentToDelete = null }) {
                    Text(appStrings.cancel)
                }
            }
        )
    }

    accountToEdit?.let { account ->
        EditWaliAccountDialog(
            account = account,
            anakAdminCount = anakAdminCount,
            onDismiss = { accountToEdit = null },
            onConfirm = { updated ->
                viewModel.updateWaliAccount(
                    account = updated,
                    onError = { errMsg ->
                        Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                    },
                    onComplete = {
                        Toast.makeText(
                            context,
                            if (isEn) "Account ${updated.name} updated!" else "Akun ${updated.name} berhasil diperbarui!",
                            Toast.LENGTH_SHORT
                        ).show()
                        accountToEdit = null
                    }
                )
            }
        )
    }

    accountToDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            title = { Text(if (isEn) "Delete Wali Asuh Account?" else "Hapus Akun Wali Asuh?") },
            text = {
                Text(
                    if (isEn) {
                        "Login account for '${account.name}' (${account.dormitory}) will be deleted. This Wali Asuh will no longer be able to sign in with this account."
                    } else {
                        "Akun login untuk '${account.name}' (${account.dormitory}) akan dihapus. Wali asuh tidak dapat masuk ke aplikasi dengan akun ini lagi."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWaliAccount(account)
                        Toast.makeText(
                            context,
                            if (isEn) "Account ${account.name} deleted." else "Akun ${account.name} dihapus.",
                            Toast.LENGTH_SHORT
                        ).show()
                        accountToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(appStrings.delete)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { accountToDelete = null }) {
                    Text(appStrings.cancel)
                }
            }
        )
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text(if (isEn) "Leave Admin Portal?" else "Keluar dari Portal Admin?") },
            text = {
                Text(
                    if (isEn) {
                        "Are you sure you want to sign out (logout) from the School Administrator session?"
                    } else {
                        "Apakah Anda yakin ingin keluar (logout) dari sesi Administrator Sekolah?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(appStrings.nav.logoutBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text(appStrings.cancel)
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
                    Toast.makeText(
                        context,
                        if (isEn) "School & facility profile saved!" else "Profil sekolah & faskes berhasil disimpan!",
                        Toast.LENGTH_SHORT
                    ).show()
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

    if (showCsvBackupSheet) {
        AdminCsvBackupSheet(
            studentsCount = schoolStudents.size,
            waliCount = schoolAccounts.size,
            isEn = isEn,
            onImportStudents = { showImportStudentsDialog = true },
            onExportStudents = { triggerExportStudentsCsv() },
            onImportWali = { showImportWaliDialog = true },
            onExportWali = { triggerExportWaliCsv() },
            onOpenSchoolProfile = { showEditSchoolDialog = true },
            onDismiss = { showCsvBackupSheet = false }
        )
    }

    if (showSecurityMaintenanceSheet) {
        AdminSecurityMaintenanceSheet(
            schoolName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
            activeSchoolCode = activeSchoolCode,
            isEn = isEn,
            onChangePassword = { showChangePasswordDialog = true },
            onClearSchoolData = { showClearDataConfirmDialog = true },
            onDeleteSchoolAccount = {
                deleteSchoolAccountError = null
                showDeleteSchoolAccountSheet = true
            },
            onDismiss = { showSecurityMaintenanceSheet = false }
        )
    }

    if (showDeleteSchoolAccountSheet) {
        val adminLabel = "${schoolProfile.schoolName.ifBlank { schoolProfile.shortName }} ($activeSchoolCode)"
        DeleteAccountBottomSheet(
            isSchoolAdmin = true,
            accountLabel = adminLabel,
            isProcessing = isDeletingSchoolAccount,
            errorMessage = deleteSchoolAccountError,
            onConfirmDelete = { password ->
                isDeletingSchoolAccount = true
                deleteSchoolAccountError = null
                viewModel.deleteCurrentSchoolAdminAccount(password) { success, message ->
                    isDeletingSchoolAccount = false
                    if (success) {
                        showDeleteSchoolAccountSheet = false
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        onLogout()
                    } else {
                        deleteSchoolAccountError = message
                    }
                }
            },
            onDismiss = {
                if (!isDeletingSchoolAccount) {
                    showDeleteSchoolAccountSheet = false
                    deleteSchoolAccountError = null
                }
            }
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
                    Toast.makeText(
                        context,
                        if (isEn) {
                            "Successfully imported $count students into $activeSchoolCode database!"
                        } else {
                            "Berhasil mengimpor $count siswa ke database $activeSchoolCode!"
                        },
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    if (showClearDataConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirmDialog = false },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(if (isEn) "Clear This School's Data?" else "Bersihkan Data Sekolah Ini?") },
            text = {
                Text(
                    if (isEn) {
                        "This will delete all student records and Wali Asuh accounts registered under ${schoolProfile.schoolName} (${activeSchoolCode}). Data will be reset to 0."
                    } else {
                        "Tindakan ini akan menghapus seluruh data siswa dan akun wali asuh yang terdaftar pada sekolah ${schoolProfile.schoolName} (${activeSchoolCode}). Data akan di-reset menjadi 0 dan bersih total."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearSchoolData(activeSchoolCode) {
                            showClearDataConfirmDialog = false
                            Toast.makeText(
                                context,
                                if (isEn) {
                                    "All student and Wali Asuh data for $activeSchoolCode cleared!"
                                } else {
                                    "Seluruh data siswa dan wali untuk $activeSchoolCode telah dihapus bersih!"
                                },
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isEn) "Yes, Clear All" else "Ya, Bersihkan Semua", color = Color.White)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showClearDataConfirmDialog = false }) {
                    Text(appStrings.cancel)
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
                        Toast.makeText(
                            context,
                            if (isEn) {
                                "Puasa session \"${created.title}\" created!"
                            } else {
                                "Sesi puasa \"${created.title}\" berhasil dibuat!"
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            if (isEn) {
                                "Please log in first to create a Puasa session."
                            } else {
                                "Silakan login terlebih dahulu untuk membuat sesi puasa."
                            },
                            Toast.LENGTH_SHORT
                        ).show()
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
            title = { Text(if (isEn) "Delete Puasa Session?" else "Hapus Sesi Puasa?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (isEn) {
                        "Session \"${session.title}\" and all Puasa participant records inside it will be deleted."
                    } else {
                        "Sesi \"${session.title}\" beserta data peserta puasa di dalamnya akan dihapus."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFastingSession(session) {
                            Toast.makeText(
                                context,
                                if (isEn) "Session \"${session.title}\" deleted." else "Sesi \"${session.title}\" dihapus.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        fastingSessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(appStrings.delete)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { fastingSessionToDelete = null }) {
                    Text(appStrings.cancel)
                }
            }
        )
    }

    if (showCreateVisitSessionDialog) {
        CreateVisitSessionDialog(
            students = schoolStudents,
            initialSession = visitSessionToEdit,
            existingSessions = schoolVisitSessions,
            onDismiss = {
                showCreateVisitSessionDialog = false
                visitSessionToEdit = null
            },
            onConfirmBatch = { drafts ->
                viewModel.createVisitSessionsBatch(drafts) { created, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
                showCreateVisitSessionDialog = false
                visitSessionToEdit = null
            },
            onConfirm = { title, monthYear, dates, districts, description, visitTimeRange ->
                val editing = visitSessionToEdit
                if (editing != null) {
                    viewModel.updateVisitSession(editing, title, monthYear, dates, districts, description, visitTimeRange) { updated ->
                        if (updated != null) {
                            Toast.makeText(
                                context,
                                if (isEn) "Visit session updated!" else "Sesi kunjungan diperbarui!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    viewModel.createVisitSession(title, monthYear, dates, districts, description, visitTimeRange) { created ->
                        if (created != null) {
                            Toast.makeText(
                                context,
                                if (isEn) "Visit session \"${created.title}\" created!" else "Sesi kunjungan \"${created.title}\" berhasil dibuat!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                showCreateVisitSessionDialog = false
                visitSessionToEdit = null
            }
        )
    }

    visitSessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { visitSessionToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(if (isEn) "Delete Visit Session?" else "Hapus Sesi Kunjungan?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (isEn) {
                        "Visit session \"${session.title}\" and all attendance records inside it will be deleted."
                    } else {
                        "Sesi kunjungan \"${session.title}\" beserta riwayat kehadiran di dalamnya akan dihapus."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVisitSession(session) {
                            Toast.makeText(
                                context,
                                if (isEn) "Visit session deleted." else "Sesi kunjungan dihapus.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        visitSessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(appStrings.delete)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { visitSessionToDelete = null }) {
                    Text(appStrings.cancel)
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
                    Toast.makeText(
                        context,
                        if (isEn) {
                            "Successfully imported $count Wali Asuh accounts into $activeSchoolCode database!"
                        } else {
                            "Berhasil mengimpor $count akun wali asuh ke database $activeSchoolCode!"
                        },
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    // =========================================================================
    // DIALOG & SHEET JADWAL KERJA WALI ASUH (KHUSUS ADMIN SEKOLAH)
    // =========================================================================
    if (showJadwalMatriksSheet) {
        JadwalMatriksSheet(
            monthKey = jadwalMonthKey,
            entries = jadwalMonthEntries,
            shiftCodes = schoolShiftCodes,
            schoolProfile = schoolProfile,
            canManageSchedule = true,
            actorName = schoolProfile.adminName.ifBlank { waliName.ifBlank { "Admin Sekolah" } },
            onChangeMonth = { newMonth ->
                jadwalMonthKey = newMonth
                val maxD = WaliScheduleHelper.daysInMonth(newMonth)
                if (jadwalSelectedDay > maxD) jadwalSelectedDay = maxD
            },
            onSelectCellToEdit = { entry, day ->
                editingJadwalCellTarget = entry to day
            },
            onOpenPersonal = { entry ->
                jadwalPersonalTarget = entry
            },
            onOpenImportDialog = { showImportJadwalMatriks = true },
            onDismiss = { showJadwalMatriksSheet = false }
        )
    }

    jadwalPersonalTarget?.let { rawTarget ->
        val liveEntry = jadwalMonthEntries.firstOrNull { it.id == rawTarget.id } ?: rawTarget
        JadwalPersonalSheet(
            entry = liveEntry,
            monthKey = jadwalMonthKey,
            shiftCodes = schoolShiftCodes,
            schoolProfile = schoolProfile,
            canManageSchedule = true,
            onEditDay = { day ->
                editingJadwalCellTarget = liveEntry to day
            },
            onDismiss = { jadwalPersonalTarget = null }
        )
    }

    if (showPetugasJamIni) {
        PetugasJamIniDialog(
            monthKey = jadwalMonthKey,
            day = jadwalSelectedDay,
            entries = jadwalMonthEntries,
            shiftCodes = schoolShiftCodes,
            schoolProfile = schoolProfile,
            onDismiss = { showPetugasJamIni = false }
        )
    }

    if (showRekapIzinJadwal) {
        RekapIzinJadwalSheet(
            monthKey = jadwalMonthKey,
            entries = jadwalMonthEntries,
            canManageSchedule = true,
            onEditCell = { entry, day ->
                editingJadwalCellTarget = entry to day
            },
            onDismiss = { showRekapIzinJadwal = false }
        )
    }

    if (showDaftarKodeSop) {
        DaftarKodeSopSheet(
            shiftCodes = schoolShiftCodes,
            schoolProfile = schoolProfile,
            canManageSchedule = true,
            onOpenManageShiftCodes = { showManageShiftCodes = true },
            onDismiss = { showDaftarKodeSop = false }
        )
    }

    if (showPenyematanTugas) {
        PenyematanTugasSheet(
            monthKey = jadwalMonthKey,
            selectedDay = jadwalSelectedDay,
            entries = jadwalMonthEntries,
            shiftCodes = schoolShiftCodes,
            schoolProfile = schoolProfile,
            canManageSchedule = true,
            onEditCell = { entry, day ->
                editingJadwalCellTarget = entry to day
            },
            onOpenRosterSetup = {
                importJadwalInitialTab = 0
                showImportJadwalMatriks = true
            },
            onDismiss = { showPenyematanTugas = false }
        )
    }

    if (showManageAnakAdminDialog) {
        KelolaAnakAdminDialog(
            schoolProfile = schoolProfile,
            waliAccounts = schoolAccounts,
            onToggleAnakAdmin = { targetAccount, enable ->
                viewModel.setWaliAnakAdminStatus(targetAccount, enable) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showManageAnakAdminDialog = false }
        )
    }

    editingJadwalCellTarget?.let { (rawEntry, targetDay) ->
        val liveEntry = jadwalMonthEntries.firstOrNull { it.id == rawEntry.id } ?: rawEntry
        EditJadwalSelDialog(
            entry = liveEntry,
            day = targetDay,
            monthKey = jadwalMonthKey,
            shiftCodes = schoolShiftCodes,
            schoolProfile = schoolProfile,
            onSave = { code, pins, leave ->
                viewModel.updateWaliScheduleDayCell(
                    entry = liveEntry,
                    day = targetDay,
                    shiftCode = code,
                    pinsForDay = pins,
                    leaveForDay = leave
                ) { ok, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (ok) editingJadwalCellTarget = null
                }
            },
            onOpenPatternEditor = {
                editingJadwalCellTarget = null
                showImportJadwalMatriks = true
            },
            onDismiss = { editingJadwalCellTarget = null }
        )
    }

    if (showManageShiftCodes) {
        ManageShiftCodesDialog(
            shiftCodes = schoolShiftCodes,
            schoolProfile = schoolProfile,
            onSaveCode = { existing, code, name, group, zone, timeRange, startH, endH, workH, hex, sop ->
                viewModel.saveWaliShiftCode(
                    existing = existing,
                    code = code,
                    name = name,
                    shiftGroup = group,
                    zoneLabel = zone,
                    timeRange = timeRange,
                    startHour = startH,
                    endHour = endH,
                    workHours = workH,
                    colorHex = hex,
                    sopNotes = sop
                ) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteCode = { sc ->
                viewModel.deleteWaliShiftCode(sc) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onResetDefault = {
                viewModel.resetSchoolShiftCodesToDefault { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showManageShiftCodes = false }
        )
    }

    if (showImportJadwalMatriks) {
        ImportJadwalMatriksDialog(
            monthKey = jadwalMonthKey,
            entries = jadwalMonthEntries,
            shiftCodes = schoolShiftCodes,
            schoolProfile = schoolProfile,
            waliAccounts = schoolAccounts,
            initialTab = importJadwalInitialTab,
            onSyncWaliAccounts = {
                viewModel.syncScheduleMembersFromWaliAccounts(jadwalMonthKey) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onAddManualMember = { name, code ->
                viewModel.addWaliScheduleMember(
                    monthKey = jadwalMonthKey,
                    waliName = name,
                    waliCode = code
                ) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteMember = { entry ->
                viewModel.deleteWaliScheduleMember(entry) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onApplyPattern = { entry, startDay, pattern, repeat ->
                viewModel.applyShiftPatternToRow(
                    entry = entry,
                    startDay = startDay,
                    patternCodes = pattern,
                    repeatUntilEndOfMonth = repeat
                ) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onImportCsv = { csv ->
                viewModel.importScheduleMatrixCsv(jadwalMonthKey, csv) { ok, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (ok) showImportJadwalMatriks = false
                }
            },
            onDismiss = { showImportJadwalMatriks = false }
        )
    }

    readyAdminJadwalPdf?.let { pdfFile ->
        AlertDialog(
            onDismissRequest = { readyAdminJadwalPdf = null },
            title = { Text(if (isEn) "Duty Roster Matrix PDF Ready" else "PDF Matriks Jadwal Siap") },
            text = {
                Text(
                    if (isEn) "Choose to open the monthly landscape duty roster PDF or share it."
                    else "Pilih untuk membuka atau membagikan dokumen PDF matriks jadwal resmi bulanan."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        readyAdminJadwalPdf = null
                        PdfReportGenerator.openPdfFile(context, pdfFile)
                    }
                ) {
                    Text(if (isEn) "Open PDF" else "Buka PDF")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        readyAdminJadwalPdf = null
                        PdfReportGenerator.shareJadwalPdf(
                            context = context,
                            pdfFile = pdfFile,
                            subject = if (isEn) "Wali Asuh Duty Roster" else "Jadwal Resmi Wali Asuh",
                            text = if (isEn) "Attached is the official monthly duty roster matrix PDF." else "Terlampir PDF matriks jadwal kerja Wali Asuh."
                        )
                    }
                ) {
                    Text(if (isEn) "Share PDF" else "Bagikan")
                }
            }
        )
    }

    if (showInfoPentingSheet) {
        InfoPentingFeedSheet(
            posts = infoPentingPosts,
            reactions = infoPentingReactions,
            comments = infoPentingComments,
            currentUserKey = viewModel.currentInfoPentingUserKey(),
            isAnakSuperAdmin = isAnakSuperAdmin,
            onOpenCreatePost = {},
            onRefreshSync = { viewModel.syncInfoPentingChannelNow() },
            onTogglePinPost = {},
            onDeletePost = {},
            onToggleReaction = { post, reactionType ->
                viewModel.toggleInfoPentingReaction(post, reactionType)
            },
            onSendComment = { post, commentText, replyTarget ->
                viewModel.addInfoPentingComment(post, commentText, replyTarget) { ok, msg ->
                    if (!ok) {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDeleteOwnComment = { comment ->
                viewModel.deleteOwnInfoPentingComment(comment) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showInfoPentingSheet = false }
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
    anakAdminCount: Int = 0,
    onToggleAnakAdmin: (Boolean) -> Unit = {},
    onResetPassword: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    var passwordVisible by remember { mutableStateOf(false) }
    var showPhotoZoom by remember { mutableStateOf(false) }
    val hasPhoto = account.hasProfilePhoto()
    val avatarGender = if (account.dormitory.contains("putri", ignoreCase = true)) "P" else "L"

    val isGuruNgaji = account.isGuruNgaji()
    val isAnakAdmin = account.isAnakAdminSekolah()
    val isPutri = account.dormitory.contains("Putri", ignoreCase = true)
    val dormAccentColor = when {
        isGuruNgaji -> Color(0xFF047857)
        isAnakAdmin -> Color(0xFF4338CA)
        isPutri -> Color(0xFFBE185D)
        else -> Color(0xFF0369A1)
    }
    val dormBgColor = when {
        isGuruNgaji -> Color(0xFFECFDF5)
        isAnakAdmin -> Color(0xFFEEF2FF)
        isPutri -> Color(0xFFFDF2F8)
        else -> Color(0xFFF0F9FF)
    }

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
                            text = when {
                                isGuruNgaji && isEn -> "MENGAJI TEACHER ACCESS CARD"
                                isGuruNgaji -> "KARTU AKSES GURU NGAJI"
                                isAnakAdmin && isEn -> "WALI ASUH • SCHEDULE SUB-ADMIN"
                                isAnakAdmin -> "WALI ASUH • ANAK ADMIN JADWAL"
                                isEn -> "WALI ASUH ACCESS CARD"
                                else -> "KARTU AKSES WALI ASUH"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = schoolName.ifBlank { if (isEn) "Your School" else "Sekolah Anda" },
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
                            text = when {
                                isGuruNgaji -> account.roleLabel(isEn)
                                isAnakAdmin -> if (isEn) "★ Sub-Admin" else "★ Anak Admin"
                                else -> account.dormitory
                            },
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
                // Info Identitas Wali Asuh / Guru Ngaji
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(dormAccentColor.copy(alpha = 0.12f))
                            .let { mod ->
                                if (hasPhoto) mod.clickable { showPhotoZoom = true } else mod
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        StudentAvatar(
                            name = account.name,
                            gender = avatarGender,
                            size = 46.dp,
                            photoUrl = account.photoUrl,
                            photoFallbackUrl = account.photoFallbackUrl
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = account.name,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (hasPhoto) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = if (isEn) "Profile photo uploaded" else "Foto profil terisi",
                                    tint = Color(0xFF047857),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        if (account.titlePosition.isNotBlank() || account.employeeCode.isNotBlank()) {
                            val titleLine = listOfNotNull(
                                account.titlePosition.takeIf { it.isNotBlank() },
                                account.employeeCode.takeIf { it.isNotBlank() }?.let { "ID: $it" }
                            ).joinToString(" • ")
                            Text(
                                text = titleLine,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = dormAccentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (account.phone.isNotBlank()) {
                            Text(
                                text = if (isGuruNgaji) {
                                    "${account.roleLabel(isEn)} • WhatsApp: ${account.phone}"
                                } else {
                                    "WhatsApp: ${account.phone}"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = when {
                                    isGuruNgaji && isEn -> "Active Mengaji Teacher • Mengaji Portal Only"
                                    isGuruNgaji -> "Guru Ngaji Aktif • Khusus Halaman Mengaji"
                                    isEn -> "Active Wali Asuh • ${account.dormitory}"
                                    else -> "Wali Asuh Aktif • ${account.dormitory}"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (showPhotoZoom && hasPhoto) {
                    StudentPhotoPreviewDialog(
                        studentName = account.name,
                        gender = avatarGender,
                        subtitle = "${account.effectiveTitle(isEn)} • $schoolName",
                        photoUrl = account.photoUrl,
                        photoFallbackUrl = account.photoFallbackUrl,
                        onDismiss = { showPhotoZoom = false }
                    )
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
                                    contentDescription = if (isEn) "Copy Username" else "Salin Username",
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
                                        contentDescription = if (passwordVisible) {
                                            if (isEn) "Hide" else "Sembunyikan"
                                        } else {
                                            if (isEn) "Show" else "Lihat"
                                        },
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
                                        contentDescription = if (isEn) "Copy Password" else "Salin Password",
                                        tint = dormAccentColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        // Keterangan status sinkronisasi
                        Text(
                            text = if (isEn) {
                                "✓ Password auto-synced with Wali Asuh profile"
                            } else {
                                "✓ Password tersinkron otomatis dari profil wali asuh"
                            },
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = dormAccentColor.copy(alpha = 0.85f)
                        )
                    }
                }

                // Tombol Penyematan Fitur Anak Admin Jadwal (Maks 7 Wali Asuh per Sekolah)
                if (!isGuruNgaji) {
                    val canPinMore = isAnakAdmin || anakAdminCount < WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL
                    OutlinedButton(
                        onClick = { onToggleAnakAdmin(!isAnakAdmin) },
                        enabled = canPinMore,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isAnakAdmin) Color(0xFF4338CA) else Color(0xFF94A3B8)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isAnakAdmin) Color(0xFFEEF2FF) else Color.Transparent,
                            contentColor = if (isAnakAdmin) Color(0xFF312E81) else Color(0xFF334155)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_toggle_anak_admin_${account.id}")
                    ) {
                        Icon(
                            imageVector = if (isAnakAdmin) Icons.Default.AdminPanelSettings else Icons.Default.PushPin,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isAnakAdmin && isEn -> "Unpin Schedule Sub-Admin ($anakAdminCount/${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL})"
                                isAnakAdmin -> "Anak Admin Jadwal Aktif • Ketuk untuk Cabut ($anakAdminCount/${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL})"
                                isEn -> "Pin as Schedule Sub-Admin ($anakAdminCount/${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL})"
                                else -> "Sematkan Anak Admin Jadwal ($anakAdminCount/${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL})"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
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
                                contentDescription = if (isEn) "Delete Account" else "Hapus Akun",
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
                                contentDescription = if (isEn) "Edit Account" else "Edit Akun",
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
                            Text(if (isEn) "Reset Pass" else "Acak Password", fontSize = 10.5.sp)
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
                        Text(if (isEn) "Send to WA" else "Kirim Akun ke WA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
    onManageTandem: (() -> Unit)? = null,
    onReleaseWali: (() -> Unit)? = null,
    onDelete: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
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
                StudentAvatar(
                    name = student.name,
                    gender = student.gender,
                    size = 44.dp,
                    photoUrl = student.photoUrl,
                    photoFallbackUrl = student.photoFallbackUrl
                )
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
                                text = if (isEn) {
                                    if (isPutri) "Female" else "Male"
                                } else {
                                    if (isPutri) "Putri" else "Putra"
                                },
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
                                text = if (isEn) "Class ${student.classGrade}" else "Kelas ${student.classGrade}",
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
                                if (ttlSummary.isNotBlank()) {
                                    append(if (isEn) "Birth: $ttlSummary (${student.age} yrs)" else "TTL: $ttlSummary (${student.age} Th)")
                                }
                                if (ttlSummary.isNotBlank() && student.motherName.isNotBlank()) append(" • ")
                                if (student.motherName.isNotBlank()) {
                                    append(if (isEn) "Mother: ${student.motherName}" else "Ibu: ${student.motherName}")
                                }
                            },
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    if (student.address.isNotBlank()) {
                        Text(
                            text = if (isEn) "Address: ${student.address}" else "Alamat: ${student.address}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    if (student.district.isNotBlank()) {
                        Text(
                            text = if (isEn) "District: Kec. ${student.district}" else "Kecamatan: ${student.district}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    if (student.dormitory.isNotBlank()) {
                        Text(
                            text = "🏠 ${student.dormitory}${if (student.roomNumber.isNotBlank()) {
                                if (isEn) " • Room ${student.roomNumber}" else " • Kamar ${student.roomNumber}"
                            } else ""}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (student.parentPhone.isNotBlank()) {
                        Text(
                            text = if (isEn) "📱 Parent Phone: ${student.parentPhone}" else "📱 HP Ortu: ${student.parentPhone}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F766E)
                        )
                    }
                    val assignedWalis = TandemWaliHelper.allAssignedWaliRefs(student)
                    if (assignedWalis.isNotEmpty() || onManageTandem != null) {
                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            assignedWalis.forEachIndexed { idx, (u, n) ->
                                val label = n.ifBlank { u }
                                val isPrimary = idx == 0
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isPrimary) Color(0xFFEDE9FE) else Color(0xFFCCFBF1))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isPrimary) {
                                            if (isEn) "Lead: $label" else "Utama: $label"
                                        } else {
                                            "Tandem: $label"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isPrimary) Color(0xFF5B21B6) else Color(0xFF0F766E)
                                    )
                                }
                            }
                            if (onManageTandem != null) {
                                OutlinedButton(
                                    onClick = onManageTandem,
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(24.dp)
                                        .testTag("btn_admin_manage_tandem_${student.id}")
                                ) {
                                    Text(
                                        text = if (isEn) "Manage Team (${assignedWalis.size}/3)" else "Tim Wali (${assignedWalis.size}/3)",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WaliPrimary
                                    )
                                }
                            }
                            if (onReleaseWali != null) {
                                OutlinedButton(
                                    onClick = onReleaseWali,
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(24.dp)
                                        .testTag("btn_admin_release_wali_${student.id}")
                                ) {
                                    Text(
                                        text = if (isEn) "Release" else "Lepas",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }
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
                                text = if (isEn) "⚠️ Allergy: ${student.allergies}" else "⚠️ Alergi: ${student.allergies}",
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
                                text = if (isEn) "🩸 Blood: ${student.bloodType}" else "🩸 Gol: ${student.bloodType}",
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
                    Text(
                        text = if (isEn) "Details & Medical Record" else "Detail & Rekam Medis",
                        fontSize = 10.5.sp,
                        color = WaliPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
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
                    Text(
                        text = if (isEn) "Delete Student" else "Hapus Siswa",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminManageTandemDialog(
    student: ChildStudent,
    waliAccounts: List<WaliAccount>,
    isEn: Boolean,
    onSaveTeam: (primary: WaliAccount?, tandems: List<WaliAccount>) -> Unit,
    onDismiss: () -> Unit
) {
    val initialPrimaryKey = remember(student) {
        waliAccounts.firstOrNull { acc ->
            TandemWaliHelper.matchesIdentity(student.assignedWaliUsername, student.assignedWaliName, acc.username, acc.name)
        }?.username?.ifBlank { null } ?: student.assignedWaliUsername
    }
    val initialTandemKeys = remember(student) {
        TandemWaliHelper.tandemMembers(student).mapNotNull { m ->
            waliAccounts.firstOrNull { acc ->
                TandemWaliHelper.matchesIdentity(m.username, m.name, acc.username, acc.name)
            }?.username?.ifBlank { m.username } ?: m.username.ifBlank { null }
        }.toSet()
    }
    var selectedPrimaryUser by remember(student) { mutableStateOf(initialPrimaryKey) }
    var selectedTandemUsers by remember(student) { mutableStateOf(initialTandemKeys) }
    var search by remember { mutableStateOf("") }

    val filteredAccounts = remember(waliAccounts, search) {
        val q = search.trim().lowercase()
        waliAccounts.filter {
            q.isEmpty() || it.name.lowercase().contains(q) ||
                it.username.lowercase().contains(q) ||
                it.dormitory.lowercase().contains(q)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (isEn) "Manage Caregiver Team (Max 3)" else "Kelola Tim Wali Asuh (Maks. 3)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${student.name} (${student.dormitory.ifBlank { "-" }})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isEn) {
                        "Pick 1 Lead Caregiver and up to 2 Tandem Caregivers."
                    } else {
                        "Pilih 1 Wali Utama dan maksimal 2 Wali Tandem."
                    },
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = {
                        Text(if (isEn) "Search wali name..." else "Cari nama wali asuh...", fontSize = 12.sp)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_admin_search_tandem_wali")
                )
                if (selectedPrimaryUser.isNotBlank()) {
                    TextButton(
                        onClick = {
                            selectedPrimaryUser = ""
                            selectedTandemUsers = emptySet()
                        },
                        modifier = Modifier.testTag("btn_admin_clear_team")
                    ) {
                        Text(
                            text = if (isEn) "Clear All Assignments" else "Kosongkan Semua Penugasan",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.5.sp
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredAccounts, key = { "adm_tw_${it.id}_${it.username}" }) { acc ->
                        val accKey = acc.username.ifBlank { acc.name }
                        val isPrimary = selectedPrimaryUser.equals(accKey, ignoreCase = true)
                        val isTandem = selectedTandemUsers.any { it.equals(accKey, ignoreCase = true) }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                isPrimary -> Color(0xFFEDE9FE)
                                isTandem -> Color(0xFFCCFBF1)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isPrimary -> Color(0xFF7C3AED)
                                    isTandem -> Color(0xFF0F766E)
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${acc.name} (${acc.dormitory.ifBlank { "-" }})",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = isPrimary,
                                        onClick = {
                                            selectedPrimaryUser = if (isPrimary) "" else accKey
                                            selectedTandemUsers = selectedTandemUsers.filterNot {
                                                it.equals(accKey, ignoreCase = true)
                                            }.toSet()
                                        },
                                        label = {
                                            Text(if (isEn) "Lead" else "Wali Utama", fontSize = 10.5.sp)
                                        },
                                        modifier = Modifier.testTag("chip_admin_set_primary_${accKey}")
                                    )
                                    FilterChip(
                                        selected = isTandem,
                                        enabled = selectedPrimaryUser.isNotBlank() && !isPrimary && (isTandem || selectedTandemUsers.size < 2),
                                        onClick = {
                                            selectedTandemUsers = if (isTandem) {
                                                selectedTandemUsers.filterNot { it.equals(accKey, ignoreCase = true) }.toSet()
                                            } else if (selectedTandemUsers.size < 2) {
                                                selectedTandemUsers + accKey
                                            } else {
                                                selectedTandemUsers
                                            }
                                        },
                                        label = {
                                            Text("Tandem", fontSize = 10.5.sp)
                                        },
                                        modifier = Modifier.testTag("chip_admin_set_tandem_${accKey}")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val primaryAcc = waliAccounts.firstOrNull {
                        it.username.ifBlank { it.name }.equals(selectedPrimaryUser, ignoreCase = true)
                    }
                    val tandemAccs = selectedTandemUsers.mapNotNull { tKey ->
                        waliAccounts.firstOrNull {
                            it.username.ifBlank { it.name }.equals(tKey, ignoreCase = true)
                        }
                    }.take(TandemWaliHelper.MAX_TANDEM_MEMBERS)
                    onSaveTeam(primaryAcc, tandemAccs)
                },
                modifier = Modifier.testTag("btn_admin_save_tandem_team")
            ) {
                Text(if (isEn) "Save Team" else "Simpan Tim", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEn) "Cancel" else "Batal")
            }
        }
    )
}


