@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.ui.HealthViewModel
import com.example.ui.components.ControlBadge
import com.example.ui.components.FaskesBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.dialogs.DirectTreatmentDialog
import com.example.ui.dialogs.PrintPdfDialog
import com.example.ui.dialogs.ShareWhatsAppDialog
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import java.util.Calendar

/**
 * Menghitung catatan rekam medis anak asuh pada bulan berjalan.
 * Memastikan bila anak asuh sebulan sakit dua kali atau lebih, frekuensi terikat terdeteksi akurat.
 */
fun getStudentRecordsThisMonth(studentId: Long, records: List<MedicalRecord>): List<MedicalRecord> {
    val calNow = Calendar.getInstance()
    val curMonth = calNow.get(Calendar.MONTH)
    val curYear = calNow.get(Calendar.YEAR)
    val calItem = Calendar.getInstance()
    return records.filter { record ->
        if (record.studentId != studentId) return@filter false
        val time = record.treatmentDate ?: record.planDate
        calItem.timeInMillis = time
        calItem.get(Calendar.MONTH) == curMonth && calItem.get(Calendar.YEAR) == curYear
    }
}

@Composable
fun MedicalRecordScreen(
    viewModel: HealthViewModel,
    waliName: String,
    completedRecords: List<MedicalRecord>,
    allRecords: List<MedicalRecord> = completedRecords,
    students: List<ChildStudent>,
    searchQuery: String,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDirectTreatmentDialog by remember { mutableStateOf(false) }
    var recordToShare by remember { mutableStateOf<MedicalRecord?>(null) }
    var selectedRecordForDetail by remember { mutableStateOf<MedicalRecord?>(null) }
    var selectedStudentForPassport by remember { mutableStateOf<ChildStudent?>(null) }
    var studentForPdfPrint by remember { mutableStateOf<ChildStudent?>(null) }

    // Tab mode: 0 = Riwayat Tindakan (Slim Cards), 1 = Buku Screening Anak (Anak sebagai Primary)
    var selectedTabMode by remember { mutableIntStateOf(0) }

    var selectedDateFilter by remember { mutableStateOf("Semua Tanggal") }

    val todayStr = remember { DateUtils.formatShortDate(Calendar.getInstance().timeInMillis) }

    val filteredList = completedRecords.filter { record ->
        val matchesDate = if (selectedDateFilter == "Hari Ini") {
            DateUtils.isToday(record.treatmentDate ?: record.planDate)
        } else {
            true
        }
        val matchesSearch = if (searchQuery.isBlank()) {
            true
        } else {
            record.studentName.contains(searchQuery, ignoreCase = true) ||
                    record.diagnosis.contains(searchQuery, ignoreCase = true) ||
                    record.complaint.contains(searchQuery, ignoreCase = true) ||
                    record.studentDormitory.contains(searchQuery, ignoreCase = true) ||
                    record.faskes.contains(searchQuery, ignoreCase = true)
        }
        matchesDate && matchesSearch
    }

    val filteredStudents = students.filter { student ->
        if (searchQuery.isBlank()) true
        else {
            student.name.contains(searchQuery, ignoreCase = true) ||
                    student.dormitory.contains(searchQuery, ignoreCase = true) ||
                    student.nis.contains(searchQuery, ignoreCase = true) ||
                    student.classGrade.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDirectTreatmentDialog = true },
                containerColor = WaliPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_direct_treatment")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = "Tindakan Langsung", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Tindakan Langsung", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
            ) {
                // Header Titles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rekam Medis & Tindakan",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Wali: $waliName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable(onClick = onNavigateToProfile)
                    )
                }

                // Tab Switcher: Tab 0 = Kunjungan Medis, Tab 1 = Buku Screening Anak (Primary)
                TabRow(
                    selectedTabIndex = selectedTabMode,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = WaliPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Tab(
                        selected = selectedTabMode == 0,
                        onClick = { selectedTabMode = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Riwayat Tindakan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabMode == 1,
                        onClick = { selectedTabMode = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Buku Screening Anak", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                // Search Field (Full Width, Menyesuaikan Layar)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setHistorySearchQuery(it) },
                    placeholder = {
                        Text(
                            text = if (selectedTabMode == 0) "Cari tindakan, siswa, diagnosa, faskes..." else "Cari nama anak asuh, NIS, asrama...",
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Cari", modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setHistorySearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("input_medical_search")
                )

                // Filter Chips & Info Bar (FlowRow Menyesuaikan Berbagai Lebar Perangkat)
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (selectedTabMode == 0) {
                        FilterChip(
                            selected = selectedDateFilter == "Semua Tanggal",
                            onClick = { selectedDateFilter = "Semua Tanggal" },
                            label = { Text("Semua Tanggal", fontSize = 11.5.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WaliPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = WaliPrimary
                            )
                        )
                        FilterChip(
                            selected = selectedDateFilter == "Hari Ini",
                            onClick = { selectedDateFilter = "Hari Ini" },
                            label = { Text("Hari Ini ($todayStr)", fontSize = 11.5.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WaliPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = WaliPrimary
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (selectedTabMode == 0) "${filteredList.size} Riwayat Tindakan" else "${filteredStudents.size} Anak Asuh",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WaliPrimary
                        )
                    }
                }

                if (selectedTabMode == 0) {
                    // TAB 0: KARTU SLIM HASIL TINDAKAN
                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AssignmentTurnedIn,
                                    contentDescription = null,
                                    tint = WaliPrimary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Tidak ditemukan catatan medis yang cocok." else "Belum ada rekam medis pengobatan selesai.",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Gunakan tombol '+ Tindakan Langsung' untuk mendata hasil berobat anak di puskesmas.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(filteredList, key = { _, item -> item.id }) { index, record ->
                                val student = students.find { it.id == record.studentId }
                                val thisMonthRecords = getStudentRecordsThisMonth(record.studentId, allRecords)
                                val allStudentRecords = allRecords.filter { it.studentId == record.studentId }

                                SlimMedicalRecordCardItem(
                                    number = index + 1,
                                    record = record,
                                    student = student,
                                    thisMonthCount = thisMonthRecords.size,
                                    totalScreeningCount = allStudentRecords.size,
                                    onClick = { selectedRecordForDetail = record },
                                    onShare = { recordToShare = record }
                                )
                            }
                        }
                    }
                } else {
                    // TAB 1: BUKU SCREENING KESEHATAN PER ANAK (ANAK SEBAGAI PRIMARY ENTITY)
                    if (filteredStudents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ditemukan data anak asuh.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredStudents, key = { it.id }) { student ->
                                val studentRecords = allRecords.filter { it.studentId == student.id }
                                val thisMonthRecords = getStudentRecordsThisMonth(student.id, allRecords)
                                val latestRecord = studentRecords.maxByOrNull { it.treatmentDate ?: it.planDate }

                                ChildHealthPrimaryCardItem(
                                    student = student,
                                    thisMonthCount = thisMonthRecords.size,
                                    totalRecordsCount = studentRecords.size,
                                    latestRecord = latestRecord,
                                    onClick = { selectedStudentForPassport = student }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal data lengkap saat kartu tindakan diklik
    selectedRecordForDetail?.let { record ->
        val student = students.find { it.id == record.studentId }
        val allStudentRecords = allRecords.filter { it.studentId == record.studentId }
            .sortedByDescending { it.treatmentDate ?: it.planDate }
        val thisMonthRecords = getStudentRecordsThisMonth(record.studentId, allRecords)

        MedicalRecordDetailSheet(
            record = record,
            student = student,
            allStudentRecords = allStudentRecords,
            thisMonthCount = thisMonthRecords.size,
            onDismiss = { selectedRecordForDetail = null },
            onPrintPdf = {
                student?.let { studentForPdfPrint = it }
            },
            onShare = {
                selectedRecordForDetail = null
                recordToShare = record
            },
            onDelete = {
                viewModel.deleteRecord(record)
                selectedRecordForDetail = null
            }
        )
    }

    // Modal Buku Screening Kesehatan Anak Asuh (Anak sebagai Primary)
    selectedStudentForPassport?.let { student ->
        val studentRecords = allRecords.filter { it.studentId == student.id }
            .sortedByDescending { it.treatmentDate ?: it.planDate }
        val thisMonthRecords = getStudentRecordsThisMonth(student.id, allRecords)

        ChildHealthPassportSheet(
            student = student,
            records = studentRecords,
            thisMonthCount = thisMonthRecords.size,
            onDismiss = { selectedStudentForPassport = null },
            onPrintPdf = {
                studentForPdfPrint = student
            },
            onShareRecord = { record ->
                selectedStudentForPassport = null
                recordToShare = record
            }
        )
    }

    // Dialog Cetak Rekap Screening PDF (Bulan & Tahun)
    studentForPdfPrint?.let { student ->
        PrintPdfDialog(
            student = student,
            allRecords = allRecords,
            waliName = waliName,
            onDismiss = { studentForPdfPrint = null }
        )
    }

    if (showDirectTreatmentDialog) {
        DirectTreatmentDialog(
            students = students,
            onDismiss = { showDirectTreatmentDialog = false },
            onConfirm = { student, faskes, complaint, diagnosis, actions, prescription, needsCtrl, ctrlDate, escort, notes ->
                viewModel.recordDirectTreatment(
                    student = student,
                    faskes = faskes,
                    complaint = complaint,
                    diagnosis = diagnosis,
                    actionsTaken = actions,
                    prescription = prescription,
                    needsControl = needsCtrl,
                    controlDate = ctrlDate,
                    escortName = escort,
                    notes = notes
                )
                showDirectTreatmentDialog = false
            }
        )
    }

    recordToShare?.let { record ->
        ShareWhatsAppDialog(
            record = record,
            onDismiss = { recordToShare = null }
        )
    }
}

/**
 * Kartu slim, manis dan cantik untuk menampilkan ringkasan tindakan medis.
 * Menampilkan data anak asuh sebagai primary dengan indikator frekuensi sakit bulan ini
 * yang mengikat pada user anak tersebut.
 */
@Composable
fun SlimMedicalRecordCardItem(
    number: Int,
    record: MedicalRecord,
    student: ChildStudent?,
    thisMonthCount: Int,
    totalScreeningCount: Int,
    onClick: () -> Unit,
    onShare: () -> Unit
) {
    val dateStr = record.treatmentDate?.let { DateUtils.formatShortDate(it) }
        ?: DateUtils.formatShortDate(record.planDate)
    val timeStr = "${record.departureTime} WIB"
    val gradeText = student?.classGrade ?: "Kelas Siswa"
    val dormText = record.studentDormitory
    val gender = student?.gender ?: "L"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("card_medical_record_${record.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 3.dp
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Baris 1: Avatar + Nama Anak Asuh (Primary) & Aksi Cepat (WA Share + Arrow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(
                    name = record.studentName,
                    gender = gender,
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "$number. ${record.studentName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$gradeText • Asrama: $dormText",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Tombol Bagikan WhatsApp & Indikator Detail
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_share_wa_${record.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Laporan WhatsApp",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Lihat Detail Lengkap",
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Baris 2: Diagnosa Medis (Sleek tinted container, nyaman dibaca di layar apapun)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0284C7).copy(alpha = 0.08f))
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Diagnosa: ${record.diagnosis.ifBlank { record.complaint.ifBlank { "Pemeriksaan medis" } }}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Baris 3: Tanggal, Faskes, Kontrol, dan Indikator 2x Sakit (FlowRow responsif tanpa terjepit)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tanggal & Jam
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 6.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = "📅 $dateStr, $timeStr",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Faskes
                FaskesBadge(faskes = record.faskes)

                // Status Kontrol jika ada
                if (record.needsControl) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "Kontrol: ${record.controlDate?.let { DateUtils.formatShortDate(it) } ?: "Terjadwal"}",
                            color = Color(0xFFD97706),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Indikator Frekuensi Sakit Bulan Ini jika >= 2
                if (thisMonthCount >= 2) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFFEE2E2))
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "⚠️ ${thisMonthCount}x Sakit Bulan Ini",
                            color = Color(0xFFDC2626),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Kartu Anak Asuh sebagai Primary Entity di Halaman Tindakan (Tab Buku Screening Anak).
 * Menampilkan riwayat screening kesehatan yang mengikat secara utuh pada user anak tersebut.
 */
@Composable
fun ChildHealthPrimaryCardItem(
    student: ChildStudent,
    thisMonthCount: Int,
    totalRecordsCount: Int,
    latestRecord: MedicalRecord?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("card_child_primary_${student.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Baris Atas: Avatar + Nama Siswa + Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(
                    name = student.name,
                    gender = student.gender,
                    size = 44.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${student.classGrade} • NIS: ${student.nis} • Asrama: ${student.dormitory}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Buka Buku Kesehatan",
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Baris Tengah: Status Kesehatan & Riwayat Screening (FlowRow Responsif)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (thisMonthCount >= 2) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFFEE2E2))
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "⚠️ $thisMonthCount Kali Sakit Bulan Ini",
                            color = Color(0xFFDC2626),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (thisMonthCount == 1) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFE0F2FE))
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "🩺 1 Kali Sakit Bulan Ini",
                            color = Color(0xFF0369A1),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "✅ Sehat Bulan Ini",
                            color = Color(0xFF15803D),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 6.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = "📋 $totalRecordsCount Riwayat Screening",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Info Screening Terakhir jika ada
            if (latestRecord != null) {
                val lastDateStr = latestRecord.treatmentDate?.let { DateUtils.formatShortDate(it) }
                    ?: DateUtils.formatShortDate(latestRecord.planDate)
                Text(
                    text = "Terakhir: ${latestRecord.diagnosis.ifBlank { latestRecord.complaint }} ($lastDateStr • ${latestRecord.faskes.replace("Puskesmas ", "")})",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Bottom Sheet Modal yang menampilkan data lengkap rekam medis tindakan yang diklik,
 * serta menampilkan seluruh riwayat screening kesehatan yang mengikat pada user anak tersebut.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalRecordDetailSheet(
    record: MedicalRecord,
    student: ChildStudent?,
    allStudentRecords: List<MedicalRecord>,
    thisMonthCount: Int,
    onDismiss: () -> Unit,
    onPrintPdf: () -> Unit = {},
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val treatmentDateStr = record.treatmentDate?.let { DateUtils.formatFullDate(it) }
        ?: DateUtils.formatFullDate(record.planDate)
    val gradeText = student?.classGrade ?: "Kelas Siswa"
    val gender = student?.gender ?: "L"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Profil Siswa (Anak Asuh Sebagai Primary Entity)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        StudentAvatar(
                            name = record.studentName,
                            gender = gender,
                            size = 46.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = record.studentName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$gradeText • Asrama: ${record.studentDormitory} • NIS: ${student?.nis ?: "-"}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Gender: ${if (gender == "L") "Putra" else "Putri"} • Usia: ${student?.age ?: "-"} Tahun",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", modifier = Modifier.size(18.dp))
                    }
                }

                // Ringkasan Frekuensi Sakit Bulan Ini yang Mengikat pada Anak Asuh (FlowRow responsif)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (thisMonthCount >= 2) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "⚠️ $thisMonthCount Kali Sakit Bulan Ini",
                                color = Color(0xFFDC2626),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (thisMonthCount == 1) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE0F2FE))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🩺 1 Kali Sakit Bulan Ini",
                                color = Color(0xFF0369A1),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    FaskesBadge(faskes = record.faskes)

                    ControlBadge(
                        needsControl = record.needsControl,
                        controlDateFormatted = record.controlDate?.let { DateUtils.formatShortDate(it) },
                        isCompleted = record.isControlCompleted
                    )
                }

                // Card 1: Informasi Kunjungan & Waktu
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🏥 Informasi Kunjungan Faskes:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = "📍 Tempat: ${record.faskes}", fontSize = 11.5.sp)
                        Text(text = "📅 Tanggal: $treatmentDateStr", fontSize = 11.5.sp)
                        Text(text = "⏰ Jam Keberangkatan: ${record.departureTime} WIB", fontSize = 11.5.sp)
                        Text(text = "👤 Pendamping: ${record.escortName.ifBlank { "Wali Asuh SRT 1" }}", fontSize = 11.5.sp)
                    }
                }

                // Card 2: Keluhan / Gejala Awal
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "🔍 Keluhan & Gejala Awal:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = record.complaint.ifBlank { "Tidak ada catatan keluhan awal khusus." },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Card 3: Hasil Diagnosa Dokter Puskesmas
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0284C7).copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Hasil Diagnosa Dokter:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF0369A1)
                            )
                        }
                        Text(
                            text = record.diagnosis.ifBlank { "Pemeriksaan kesehatan umum puskesmas." },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Card 4: Tindakan Medis yang Dilakukan
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Tindakan Medis di Puskesmas:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF047857)
                            )
                        }
                        Text(
                            text = record.actionsTaken.ifBlank { "Observasi dan penanganan dasar." },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Card 5: Resep Obat & Aturan Minum
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF8B5CF6).copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Resep Obat & Aturan Minum:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF6D28D9)
                            )
                        }
                        Text(
                            text = record.prescription.ifBlank { "Tidak ada resep obat tambahan." },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Card 6: Kontrol Lanjutan jika ada
                if (record.needsControl) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF59E0B).copy(alpha = 0.08f)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Instruksi Kontrol Ulang:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                            Text(
                                text = record.controlDate?.let { "Tanggal Kontrol: ${DateUtils.formatFullDate(it)}" }
                                    ?: "Jadwal kontrol terdaftar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (record.isControlCompleted) "Status: Sudah selesai kontrol" else "Status: Wajib kontrol kembali ke faskes",
                                fontSize = 11.sp,
                                color = if (record.isControlCompleted) Color(0xFF059669) else Color(0xFFD97706),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Card 7: Riwayat Lengkap Seluruh Screening yang Mengikat pada Siswa Ini
                if (allStudentRecords.size > 1) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = null,
                                    tint = WaliPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Riwayat Screening Terikat Siswa Ini (${allStudentRecords.size} Catatan):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            allStudentRecords.forEachIndexed { idx, itemRecord ->
                                val itemDateStr = itemRecord.treatmentDate?.let { DateUtils.formatShortDate(it) }
                                    ?: DateUtils.formatShortDate(itemRecord.planDate)
                                val isCurrent = itemRecord.id == record.id

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isCurrent) WaliPrimary.copy(alpha = 0.08f)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${idx + 1}. $itemDateStr • ${itemRecord.faskes.replace("Puskesmas ", "")}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) WaliPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isCurrent) {
                                                Text(
                                                    text = "(Kunjungan Ini)",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WaliPrimary
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Diagnosa: ${itemRecord.diagnosis.ifBlank { itemRecord.complaint }}",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (itemRecord.actionsTaken.isNotBlank()) {
                                            Text(
                                                text = "Tindakan: ${itemRecord.actionsTaken}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tombol Cetak Rekap PDF Siswa (Bulan / Tahun)
                OutlinedButton(
                    onClick = onPrintPdf,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_detail_print_pdf")
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cetak Rekap Screening PDF (Bulan / Tahun)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Baris Aksi Bawah: Hapus & Laporan WhatsApp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Riwayat",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus Riwayat", fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = onShare,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Laporan WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Bottom Sheet Buku Rekam Screening Kesehatan Anak Asuh.
 * Menempatkan anak asuh sebagai Primary Entity, menampilkan seluruh rekam medis dan screening yang mengikat padanya.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildHealthPassportSheet(
    student: ChildStudent,
    records: List<MedicalRecord>,
    thisMonthCount: Int,
    onDismiss: () -> Unit,
    onPrintPdf: () -> Unit = {},
    onShareRecord: (MedicalRecord) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Profil Utama Siswa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        StudentAvatar(
                            name = student.name,
                            gender = student.gender,
                            size = 50.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = student.name,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "NIS: ${student.nis} • ${student.classGrade}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Asrama: ${student.dormitory} • Gol. Darah: ${student.bloodType}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Banner Status Kesehatan & Frekuensi Sakit Bulan Ini
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (thisMonthCount >= 2) Color(0xFFFEE2E2) else Color(0xFFF0FDF4)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = if (thisMonthCount >= 2) "⚠️ Perhatian Kesehatan Anak:" else "🩺 Ringkasan Screening:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (thisMonthCount >= 2) Color(0xFFDC2626) else Color(0xFF16A34A)
                        )
                        Text(
                            text = "Anak asuh ini tercatat $thisMonthCount kali sakit / berobat dalam bulan berjalan.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Total seluruh riwayat screening kesehatan terikat: ${records.size} catatan.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tombol Cetak Rekap PDF (Bulanan / Tahunan)
                Button(
                    onClick = onPrintPdf,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WaliPrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_passport_print_pdf")
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cetak Rekap Screening PDF (Bulan / Tahun)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Timeline Seluruh Riwayat Screening
                Text(
                    text = "Riwayat Screening & Pengobatan Terikat (${records.size} Kunjungan):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (records.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada catatan rekam medis atau screening untuk anak asuh ini.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    records.forEachIndexed { index, rec ->
                        val recDateStr = rec.treatmentDate?.let { DateUtils.formatFullDate(it) }
                            ?: DateUtils.formatFullDate(rec.planDate)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Kunjungan #${records.size - index} • $recDateStr",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = WaliPrimary
                                    )
                                    FaskesBadge(faskes = rec.faskes)
                                }

                                Text(
                                    text = "Keluhan: ${rec.complaint}",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (rec.diagnosis.isNotBlank()) {
                                    Text(
                                        text = "Diagnosa: ${rec.diagnosis}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0369A1)
                                    )
                                }

                                if (rec.actionsTaken.isNotBlank()) {
                                    Text(
                                        text = "Tindakan: ${rec.actionsTaken}",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (rec.prescription.isNotBlank()) {
                                    Text(
                                        text = "Obat: ${rec.prescription}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF6D28D9)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ControlBadge(
                                        needsControl = rec.needsControl,
                                        controlDateFormatted = rec.controlDate?.let { DateUtils.formatShortDate(it) },
                                        isCompleted = rec.isControlCompleted
                                    )

                                    OutlinedButton(
                                        onClick = { onShareRecord(rec) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Laporan WA", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup", fontSize = 12.sp)
                }
            }
        }
    }
}
