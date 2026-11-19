package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.Composable
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
import com.example.data.model.ChildStudent
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.ui.HealthViewModel
import com.example.ui.components.StudentAvatar
import com.example.ui.dialogs.BarcodeScannerDialog
import com.example.ui.dialogs.ChangeWaliPasswordDialog
import com.example.ui.dialogs.InputMengajiDialog
import com.example.ui.dialogs.KartuMengajiSheet
import com.example.ui.dialogs.ScannerFeedbackStatus
import com.example.ui.dialogs.ScannerFloatingFeedback
import com.example.util.DateUtils
import com.example.util.StudentClassifier
import com.example.util.i18n.LocalAppStrings

private data class GuruStudentMengajiRow(
    val student: ChildStudent,
    val latest: MengajiRecord?,
    val totalCount: Int
)

/**
 * Portal khusus akun Guru Ngaji:
 * - Hanya membuka halaman & fitur Mengaji (tanpa menu kesehatan, berobat, puasa, haid, atau admin)
 * - Mencakup semua siswa di sekolah aktif (Putra & Putri)
 * - Hak akses input setoran, scan QR kartu siswa, lihat riwayat & cetak Kartu Mengaji (tanpa hak hapus/edit)
 */
@Composable
fun GuruNgajiScreen(
    viewModel: HealthViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language

    val emeraldPrimary = Color(0xFF047857)
    val emeraldDark = Color(0xFF065F46)
    val emeraldLight = Color(0xFFD1FAE5)

    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val mengajiRecords by viewModel.mengajiRecords.collectAsStateWithLifecycle()
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

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Semua Siswa, 1 = Riwayat Setoran
    var searchQuery by remember { mutableStateOf("") }
    var studentFilter by remember { mutableStateOf("ALL") } // ALL | PUTRA | PUTRI | BELUM | TIDAK_LANCAR
    var historyFilter by remember { mutableStateOf("ALL") } // ALL | LANCAR | TIDAK_LANCAR

    var showInputMengajiDialog by remember { mutableStateOf(false) }
    var inputTargetStudent by remember { mutableStateOf<ChildStudent?>(null) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var scannerFeedback by remember { mutableStateOf<ScannerFloatingFeedback?>(null) }
    var showKartuMengajiSheet by remember { mutableStateOf(false) }
    var kartuTargetStudent by remember { mutableStateOf<ChildStudent?>(null) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val sortedRecords = remember(mengajiRecords) {
        mengajiRecords.sortedWith(
            compareByDescending<MengajiRecord> { it.sessionDate }.thenByDescending { it.createdAt }
        )
    }

    val todayCount = remember(sortedRecords) {
        sortedRecords.count { DateUtils.isToday(it.sessionDate) }
    }

    val studentRows = remember(students, sortedRecords) {
        students.map { st ->
            val recs = MengajiHelper.recordsForStudent(sortedRecords, st)
            GuruStudentMengajiRow(
                student = st,
                latest = recs.firstOrNull(),
                totalCount = recs.size
            )
        }.sortedWith(
            compareByDescending<GuruStudentMengajiRow> { it.latest != null }
                .thenBy { it.student.name.lowercase() }
        )
    }

    val studentsWithProgressCount = remember(studentRows) {
        studentRows.count { it.latest != null }
    }

    val filteredStudents = remember(studentRows, searchQuery, studentFilter) {
        val q = searchQuery.trim().lowercase()
        studentRows.filter { row ->
            val st = row.student
            val matchSearch = q.isBlank() ||
                st.name.lowercase().contains(q) ||
                st.classGrade.lowercase().contains(q) ||
                st.dormitory.lowercase().contains(q) ||
                st.nis.lowercase().contains(q) ||
                (row.latest?.jilidOrJuz?.lowercase()?.contains(q) == true)
            val matchFilter = when (studentFilter) {
                "PUTRA" -> StudentClassifier.isMaleStudent(st)
                "PUTRI" -> StudentClassifier.isFemaleStudent(st)
                "BELUM" -> row.latest == null
                "TIDAK_LANCAR" -> row.latest != null && !MengajiHelper.isFluent(row.latest.fluencyStatus)
                else -> true
            }
            matchSearch && matchFilter
        }
    }

    val filteredHistory = remember(sortedRecords, searchQuery, historyFilter) {
        val q = searchQuery.trim().lowercase()
        sortedRecords.filter { rec ->
            val matchSearch = q.isBlank() ||
                rec.studentName.lowercase().contains(q) ||
                rec.bookType.lowercase().contains(q) ||
                rec.jilidOrJuz.lowercase().contains(q) ||
                rec.studentClass.lowercase().contains(q) ||
                rec.waliName.lowercase().contains(q)
            val matchFilter = when (historyFilter) {
                "LANCAR" -> MengajiHelper.isFluent(rec.fluencyStatus)
                "TIDAK_LANCAR" -> !MengajiHelper.isFluent(rec.fluencyStatus)
                else -> true
            }
            matchSearch && matchFilter
        }
    }

    fun resolveStudent(rec: MengajiRecord): ChildStudent {
        return students.firstOrNull { MengajiHelper.matchesStudent(rec, it) }
            ?: ChildStudent(
                id = rec.studentId,
                name = rec.studentName,
                nis = rec.studentNis,
                gender = "L",
                age = 12,
                dormitory = rec.studentDorm,
                classGrade = rec.studentClass,
                nik = rec.studentNik,
                jenjang = rec.studentJenjang,
                schoolId = rec.schoolId
            )
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
                                    .background(emeraldLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = emeraldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) "Mengaji Teacher Portal" else "Portal Guru Ngaji",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = emeraldDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${waliName.ifBlank { "Guru Ngaji" }} • ${schoolProfile.shortName.ifBlank { schoolProfile.schoolName }}",
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
                                    .testTag("btn_guru_ngaji_sync")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = emeraldPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = if (isEn) "Sync Mengaji Data" else "Sinkronisasi Cloud",
                                        tint = emeraldPrimary
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showChangePasswordDialog = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_guru_ngaji_profile")
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
                                        contentDescription = if (isEn) "Profile, Photo & Password" else "Profil, Foto & Kata Sandi",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showLogoutDialog = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_guru_ngaji_logout")
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .testTag("guru_ngaji_lazy_list"),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hero Card + Ringkasan + Tombol Aksi Utama
                item(key = "guru_ngaji_hero") {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(emeraldPrimary, emeraldDark)
                                    )
                                )
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isEn) "Continuous Mengaji Input" else "Pencatatan Mengaji Santri",
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Access to all ${students.size} students • Input & Print Card only"
                                        } else {
                                            "Akses seluruh ${students.size} siswa • Khusus Input & Kartu Mengaji"
                                        },
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.88f)
                                    )
                                }
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isEn) "INPUT ONLY" else "KHUSUS NGAJI",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // 3 Stat Tiles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GuruNgajiStatTile(
                                    label = if (isEn) "Today" else "Hari Ini",
                                    value = "$todayCount",
                                    modifier = Modifier.weight(1f)
                                )
                                GuruNgajiStatTile(
                                    label = if (isEn) "Students Logged" else "Sudah Mengaji",
                                    value = "$studentsWithProgressCount/${students.size}",
                                    modifier = Modifier.weight(1f)
                                )
                                GuruNgajiStatTile(
                                    label = if (isEn) "Total Entries" else "Total Setoran",
                                    value = "${sortedRecords.size}",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // 3 Action Buttons: Input Manual, Scan QR Kartu, Kartu Mengaji
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        inputTargetStudent = null
                                        showInputMengajiDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = emeraldDark
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_guru_ngaji_input")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEn) "Input" else "Input Setoran",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Button(
                                    onClick = { showBarcodeScanner = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF10B981),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_guru_ngaji_scan_qr")
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEn) "Scan QR" else "Scan QR",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        kartuTargetStudent = null
                                        showKartuMengajiSheet = true
                                    },
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_guru_ngaji_kartu")
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEn) "Cards" else "Kartu PDF",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Switcher Tab + Pencarian + Filter Chips
                item(key = "guru_ngaji_controls") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                0 to (if (isEn) "All Students (${students.size})" else "Semua Siswa (${students.size})"),
                                1 to (if (isEn) "History (${sortedRecords.size})" else "Riwayat Setoran (${sortedRecords.size})")
                            ).forEach { (idx, title) ->
                                val active = selectedTab == idx
                                Surface(
                                    onClick = { selectedTab = idx },
                                    shape = RoundedCornerShape(9.dp),
                                    color = if (active) emeraldPrimary else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .testTag(if (idx == 0) "tab_guru_ngaji_students" else "tab_guru_ngaji_history")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = title,
                                            fontSize = 12.sp,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                            color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = if (isEn) "Search student name, class, or volume..." else "Cari nama siswa, kelas, jilid/juz...",
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = emeraldPrimary)
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
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_guru_ngaji_search")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (selectedTab == 0) {
                                listOf(
                                    "ALL" to (if (isEn) "All" else "Semua"),
                                    "PUTRA" to "Putra",
                                    "PUTRI" to "Putri",
                                    "BELUM" to (if (isEn) "No Log Yet" else "Belum Setor"),
                                    "TIDAK_LANCAR" to (if (isEn) "Needs Practice" else "Tidak Lancar")
                                ).forEach { (key, label) ->
                                    FilterChip(
                                        selected = studentFilter == key,
                                        onClick = { studentFilter = key },
                                        label = { Text(label, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = emeraldLight,
                                            selectedLabelColor = emeraldDark
                                        )
                                    )
                                }
                            } else {
                                listOf(
                                    "ALL" to (if (isEn) "All" else "Semua"),
                                    "LANCAR" to (if (isEn) "Fluent" else "Lancar"),
                                    "TIDAK_LANCAR" to (if (isEn) "Needs Practice" else "Tidak Lancar")
                                ).forEach { (key, label) ->
                                    FilterChip(
                                        selected = historyFilter == key,
                                        onClick = { historyFilter = key },
                                        label = { Text(label, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = emeraldLight,
                                            selectedLabelColor = emeraldDark
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Isi Tab Aktif
                if (selectedTab == 0) {
                    if (filteredStudents.isEmpty()) {
                        item(key = "empty_guru_students") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = emeraldPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = if (isEn) "No matching students found." else "Tidak ada siswa yang sesuai.",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredStudents, key = { "guru_st_${it.student.id}" }) { row ->
                            val st = row.student
                            val latest = row.latest
                            val isMale = StudentClassifier.isMaleStudent(st)
                            val jenjang = FastingJenjangHelper.resolveJenjang(st)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .testTag("guru_ngaji_student_card_${st.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StudentAvatar(
                                            name = st.name,
                                            gender = st.gender,
                                            size = 42.dp,
                                            photoUrl = st.photoUrl,
                                            photoFallbackUrl = st.photoFallbackUrl
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = st.name,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${if (isMale) "Putra" else "Putri"} • $jenjang ${st.classGrade.ifBlank { "-" }} • ${row.totalCount}x ${if (isEn) "logged" else "setor"}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Bacaan terakhir
                                    if (latest != null) {
                                        val isFluent = MengajiHelper.isFluent(latest.fluencyStatus)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isFluent) Color(0xFFECFDF5) else Color(0xFFFEF3C7))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${MengajiHelper.formatCompactPosition(latest)} • ${DateUtils.formatShortDate(latest.sessionDate, lang)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isFluent) emeraldDark else Color(0xFF92400E),
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = MengajiHelper.fluencyLabel(latest.fluencyStatus, isEn),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isFluent) Color(0xFF15803D) else Color(0xFFB45309)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = if (isEn) "No Mengaji record yet" else "Belum ada riwayat setoran mengaji",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Tombol khusus Input & Cetak Kartu (TIDAK ada tombol hapus)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                inputTargetStudent = st
                                                showInputMengajiDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                                            shape = RoundedCornerShape(9.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .testTag("btn_guru_ngaji_student_input_${st.id}")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (latest == null) {
                                                    if (isEn) "Record First" else "Input Perdana"
                                                } else {
                                                    if (isEn) "Continue" else "Lanjut Setor"
                                                },
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                kartuTargetStudent = st
                                                showKartuMengajiSheet = true
                                            },
                                            shape = RoundedCornerShape(9.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .height(40.dp)
                                                .testTag("btn_guru_ngaji_student_kartu_${st.id}")
                                        ) {
                                            Icon(Icons.Default.Print, contentDescription = null, tint = emeraldPrimary, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isEn) "Card" else "Kartu",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = emeraldPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (filteredHistory.isEmpty()) {
                        item(key = "empty_guru_history") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = emeraldPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = if (isEn) "No Mengaji entries yet." else "Belum ada riwayat setoran mengaji.",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredHistory, key = { "guru_rec_${it.id}" }) { rec ->
                            val isFluent = MengajiHelper.isFluent(rec.fluencyStatus)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .testTag("guru_ngaji_history_item_${rec.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = rec.studentName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${MengajiHelper.formatCompactPosition(rec)} • ${DateUtils.formatShortDate(rec.sessionDate, lang)}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = emeraldPrimary
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isFluent) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = MengajiHelper.fluencyLabel(rec.fluencyStatus, isEn),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isFluent) Color(0xFF15803D) else Color(0xFFB91C1C)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${if (isEn) "Class" else "Kelas"}: ${rec.studentClass.ifBlank { "-" }} • ${if (isEn) "Teacher" else "Penguji"}: ${rec.waliName}",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (rec.notes.isNotBlank()) {
                                        Text(
                                            text = rec.notes,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = {
                                                kartuTargetStudent = resolveStudent(rec)
                                                showKartuMengajiSheet = true
                                            },
                                            modifier = Modifier.testTag("btn_guru_ngaji_card_${rec.id}")
                                        ) {
                                            Icon(Icons.Default.Print, contentDescription = null, tint = emeraldPrimary, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isEn) "Card" else "Kartu", color = emeraldPrimary, fontSize = 11.5.sp)
                                        }
                                        TextButton(
                                            onClick = {
                                                inputTargetStudent = resolveStudent(rec)
                                                showInputMengajiDialog = true
                                            },
                                            modifier = Modifier.testTag("btn_guru_ngaji_continue_${rec.id}")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = emeraldPrimary, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isEn) "Input Next" else "Lanjut Setor", color = emeraldPrimary, fontSize = 11.5.sp)
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

    if (showInputMengajiDialog) {
        InputMengajiDialog(
            students = students,
            mengajiRecords = mengajiRecords,
            initialStudent = inputTargetStudent,
            recordToEdit = null, // Guru Ngaji hak input saja (bukan edit/hapus)
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
                showInputMengajiDialog = false
                inputTargetStudent = null
            },
            onDismiss = {
                showInputMengajiDialog = false
                inputTargetStudent = null
            }
        )
    }

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            femaleStudents = students,
            onScanned = { scannedCode ->
                val matched = StudentClassifier.findMatchingStudent(students, scannedCode)
                if (matched == null) {
                    scannerFeedback = ScannerFloatingFeedback(
                        id = System.nanoTime(),
                        status = ScannerFeedbackStatus.ERROR,
                        title = if (isEn) "Student card not found" else "Kartu siswa tidak ditemukan",
                        subtitle = if (isEn) "Code \"$scannedCode\" is not registered" else "Kode \"$scannedCode\" belum terdaftar"
                    )
                } else {
                    scannerFeedback = null
                    showBarcodeScanner = false
                    inputTargetStudent = matched
                    showInputMengajiDialog = true
                }
            },
            onDismiss = {
                showBarcodeScanner = false
                scannerFeedback = null
            },
            dialogTitle = if (isEn) "Scan Student Card QR" else "Scan QR Kartu Siswa",
            dialogSubtitle = if (isEn) {
                "Matches any student (Putra/Putri) and opens Mengaji input"
            } else {
                "Cocokkan QR kartu siswa (Putra/Putri) lalu langsung input setoran"
            },
            quickPickTitle = if (isEn) {
                "Quick Pick Student (${students.size}):"
            } else {
                "Pilih Cepat Siswa (${students.size} Siswa):"
            },
            searchPlaceholder = if (isEn) "Search name, class, NIK..." else "Cari nama, kelas, atau NIK...",
            emptyListMessage = if (isEn) "No students registered in this school." else "Belum ada siswa terdaftar di sekolah ini.",
            accentColor = emeraldPrimary,
            accentBgColor = emeraldLight,
            cardBgColor = Color(0xFFF0FDF4),
            avatarBgColor = Color(0xFFA7F3D0),
            avatarTextColor = emeraldPrimary,
            titleTextColor = Color(0xFF064E3B),
            continuousScan = false,
            floatingFeedback = scannerFeedback
        )
    }

    if (showKartuMengajiSheet) {
        KartuMengajiSheet(
            students = students,
            mengajiRecords = mengajiRecords,
            schoolProfile = schoolProfile,
            waliName = waliName,
            initialStudent = kartuTargetStudent,
            onOpenInputForStudent = { st ->
                showKartuMengajiSheet = false
                kartuTargetStudent = null
                inputTargetStudent = st
                showInputMengajiDialog = true
            },
            onDismiss = {
                showKartuMengajiSheet = false
                kartuTargetStudent = null
            }
        )
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
            title = { Text(if (isEn) "Sign Out?" else "Keluar dari Portal Guru Ngaji?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (isEn) {
                        "You will return to the login screen."
                    } else {
                        "Anda akan keluar dari sesi Guru Ngaji dan kembali ke halaman masuk."
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
                    modifier = Modifier.testTag("btn_confirm_logout_guru_ngaji")
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

@Composable
private fun GuruNgajiStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.16f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
