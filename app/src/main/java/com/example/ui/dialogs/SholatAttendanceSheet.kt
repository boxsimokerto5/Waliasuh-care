package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.HaidRecord
import com.example.data.model.SchoolProfile
import com.example.data.model.SholatDisciplineTag
import com.example.data.model.SholatHelper
import com.example.data.model.SholatRecord
import com.example.data.model.SholatStatus
import com.example.data.model.SholatWaktu
import com.example.ui.components.StudentAvatar
import com.example.util.StudentClassifier
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SholatAttendanceSheet(
    students: List<ChildStudent>,
    sholatRecords: List<SholatRecord>,
    haidRecords: List<HaidRecord>,
    currentWaliUsername: String,
    currentWaliName: String,
    schoolProfile: SchoolProfile,
    initialStudent: ChildStudent? = null,
    initialTab: Int = 0,
    initialScope: String = "ALL",
    autoOpenScanner: Boolean = false,
    onSaveRecord: (
        student: ChildStudent,
        dateKey: String,
        waktu: SholatWaktu,
        status: SholatStatus,
        disciplineTag: SholatDisciplineTag,
        notes: String,
        inputMethod: String
    ) -> Unit,
    onBulkMarkJamaah: (
        targetStudents: List<ChildStudent>,
        dateKey: String,
        waktu: SholatWaktu,
        onResult: (jamaahCount: Int, autoHaidCount: Int) -> Unit
    ) -> Unit,
    onDeleteRecord: (SholatRecord) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish

    val emeraldPrimary = Color(0xFF047857)
    val emeraldDark = Color(0xFF065F46)
    val emeraldSoft = Color(0xFFECFDF5)

    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    var selectedDateKey by remember { mutableStateOf(SholatHelper.todayDateKey()) }
    var selectedMonthKey by remember { mutableStateOf(SholatHelper.monthKeyFromDateKey(SholatHelper.todayDateKey())) }
    var selectedWaktu by remember { mutableStateOf(SholatHelper.detectActiveWaktu()) }
    var scopeFilter by remember(initialScope) { mutableStateOf(initialScope) } // ALL, MINE, PUTRA, PUTRI
    var searchQuery by remember(initialStudent) {
        mutableStateOf(initialStudent?.name.orEmpty())
    }
    var showBarcodeScanner by remember(autoOpenScanner) { mutableStateOf(autoOpenScanner) }
    var scannerFeedback by remember { mutableStateOf<ScannerFloatingFeedback?>(null) }
    var editingNoteTarget by remember { mutableStateOf<Pair<ChildStudent, SholatRecord?>?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val eligibleStudents = remember(students) {
        students.filter { !com.example.data.model.StudentReligionHelper.isChristian(it) }
    }

    val myStudentIds = remember(eligibleStudents, currentWaliUsername, currentWaliName) {
        eligibleStudents.filter { st ->
            com.example.data.model.TandemWaliHelper.isAssignedWali(st, currentWaliUsername, currentWaliName)
        }.map { it.id }.toSet()
    }

    val scopedStudents = remember(eligibleStudents, scopeFilter, myStudentIds) {
        val base = when (scopeFilter) {
            "MINE" -> if (myStudentIds.isNotEmpty()) eligibleStudents.filter { it.id in myStudentIds } else eligibleStudents
            "PUTRA" -> eligibleStudents.filter { !StudentClassifier.isFemaleStudent(it) }
            "PUTRI" -> eligibleStudents.filter { StudentClassifier.isFemaleStudent(it) }
            else -> eligibleStudents
        }
        base.sortedBy { it.name.lowercase() }
    }

    val displayedStudents = remember(scopedStudents, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) scopedStudents
        else scopedStudents.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.classGrade.contains(q, ignoreCase = true) ||
                it.dormitory.contains(q, ignoreCase = true) ||
                it.nis.contains(q, ignoreCase = true)
        }
    }

    val dailyMatrix = remember(sholatRecords, selectedDateKey) {
        SholatHelper.buildDailyMatrix(sholatRecords, selectedDateKey)
    }

    val unmarkedInScopeCount = remember(displayedStudents, dailyMatrix, selectedWaktu) {
        displayedStudents.count { st -> dailyMatrix[st.id]?.get(selectedWaktu) == null }
    }

    val tabTitles = if (isEn) {
        listOf("Quick Check", "Monitor & Praise", "Recap & PDF")
    } else {
        listOf("Absen Cepat", "Pantauan", "Rekap & PDF")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .heightIn(max = 760.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("sheet_absen_sholat"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF065F46), Color(0xFF059669))
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isEn) "5 Daily Prayers Attendance" else "Absen Sholat 5 Waktu",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isEn) {
                                    "Subuh · Dzuhur · Ashar · Maghrib · Isya — Parent Synced"
                                } else {
                                    "Subuh · Dzuhur · Ashar · Maghrib · Isya — Terhubung Orang Tua"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFFD1FAE5),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_absen_sholat")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = s.nav.close,
                            tint = Color.White
                        )
                    }
                }
            }

            // 3 Main Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = emeraldSoft,
                contentColor = emeraldDark,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = emeraldPrimary
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                tabTitles.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("tab_sholat_$idx")
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .testTag("list_sholat_absen_cepat"),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 14.dp)
                    ) {
                        item(key = "sholat_date_picker") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = {
                                        selectedDateKey = SholatHelper.shiftDateKey(selectedDateKey, -1)
                                        selectedMonthKey = SholatHelper.monthKeyFromDateKey(selectedDateKey)
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("btn_sholat_prev_day")
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = if (isEn) "Previous Day" else "Hari Sebelumnya"
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = emeraldPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = SholatHelper.formatDateKeyDisplay(selectedDateKey, isEn),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (selectedDateKey != SholatHelper.todayDateKey()) {
                                        TextButton(
                                            onClick = {
                                                val today = SholatHelper.todayDateKey()
                                                selectedDateKey = today
                                                selectedMonthKey = SholatHelper.monthKeyFromDateKey(today)
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(
                                                text = if (isEn) "Today" else "Hari Ini",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = emeraldPrimary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                IconButton(
                                    onClick = {
                                        selectedDateKey = SholatHelper.shiftDateKey(selectedDateKey, 1)
                                        selectedMonthKey = SholatHelper.monthKeyFromDateKey(selectedDateKey)
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("btn_sholat_next_day")
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = if (isEn) "Next Day" else "Hari Berikutnya"
                                    )
                                }
                            }
                        }

                        item(key = "sholat_waktu_pills") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SholatWaktu.entries.forEach { waktu ->
                                    val doneCount = scopedStudents.count { st -> dailyMatrix[st.id]?.get(waktu) != null }
                                    val selected = selectedWaktu == waktu
                                    FilterChip(
                                        selected = selected,
                                        onClick = { selectedWaktu = waktu },
                                        label = {
                                            Text(
                                                text = "${waktu.label(isEn)} ($doneCount/${scopedStudents.size})",
                                                fontSize = 11.5.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = emeraldPrimary,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFFF1F5F9),
                                            labelColor = Color(0xFF334155)
                                        ),
                                        modifier = Modifier.testTag("chip_sholat_waktu_${waktu.code}")
                                    )
                                }
                            }
                        }

                        item(key = "sholat_scope_and_search") {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val scopes = listOf(
                                        "ALL" to (if (isEn) "All (${students.size})" else "Semua (${students.size})"),
                                        "MINE" to (if (isEn) "My Foster (${myStudentIds.size})" else "Anak Binaan (${myStudentIds.size})"),
                                        "PUTRA" to (if (isEn) "Boys" else "Putra"),
                                        "PUTRI" to (if (isEn) "Girls" else "Putri")
                                    )
                                    scopes.forEach { (key, label) ->
                                        FilterChip(
                                            selected = scopeFilter == key,
                                            onClick = { scopeFilter = key },
                                            label = { Text(label, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFFD1FAE5),
                                                selectedLabelColor = emeraldDark
                                            ),
                                            modifier = Modifier.testTag("chip_sholat_scope_$key")
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            if (isEn) "Search student, class, dorm, or NIS..." else "Cari nama siswa, kelas, asrama, atau NIS...",
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = s.nav.close, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_search_sholat_student")
                                )
                            }
                        }

                        item(key = "sholat_bulk_and_scan") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onBulkMarkJamaah(displayedStudents, selectedDateKey, selectedWaktu) { jamaahCnt, haidCnt ->
                                            val msg = if (isEn) {
                                                "Marked $jamaahCnt as Congregation" + if (haidCnt > 0) " & $haidCnt as Period Excused." else "."
                                            } else {
                                                "Ditandai $jamaahCnt Jamaah" + if (haidCnt > 0) " & $haidCnt Udzur Haid otomatis." else "."
                                            }
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = displayedStudents.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_bulk_sholat_jamaah")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEn) {
                                            "Unmarked ($unmarkedInScopeCount) = Jamaah"
                                        } else {
                                            "Tandai Belum ($unmarkedInScopeCount) = Jamaah"
                                        },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showBarcodeScanner = true },
                                    border = BorderStroke(1.dp, emeraldPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_scan_sholat_qr")
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = emeraldPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEn) "Scan QR" else "Scan Kartu",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = emeraldPrimary
                                    )
                                }
                            }
                        }

                        if (displayedStudents.isEmpty()) {
                            item(key = "sholat_empty_students") {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isEn) "No matching students found." else "Tidak ada data siswa yang sesuai filter.",
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        } else {
                            items(displayedStudents, key = { "st_${it.id}" }) { student ->
                                val dayMap = dailyMatrix[student.id].orEmpty()
                                val currentRecord = dayMap[selectedWaktu]
                                val currentStatus = currentRecord?.let { SholatStatus.fromCode(it.status) }
                                val currentTag = currentRecord?.let { SholatDisciplineTag.fromCode(it.disciplineTag) } ?: SholatDisciplineTag.NONE
                                val activeHaid = SholatHelper.findActiveHaidOnDate(student, haidRecords, selectedDateKey)
                                val isFemale = StudentClassifier.isFemaleStudent(student)

                                SholatStudentRowCard(
                                    student = student,
                                    selectedWaktu = selectedWaktu,
                                    dayMap = dayMap,
                                    currentRecord = currentRecord,
                                    currentStatus = currentStatus,
                                    currentTag = currentTag,
                                    activeHaid = activeHaid,
                                    isFemale = isFemale,
                                    isEn = isEn,
                                    onSelectWaktu = { selectedWaktu = it },
                                    onPickStatus = { chosenStatus ->
                                        val method = if (chosenStatus == SholatStatus.UDZUR_HAID && activeHaid != null) {
                                            SholatHelper.METHOD_AUTO_HAID
                                        } else {
                                            SholatHelper.METHOD_MANUAL
                                        }
                                        val keepTag = if (chosenStatus == SholatStatus.JAMAAH) currentTag else SholatDisciplineTag.NONE
                                        onSaveRecord(
                                            student,
                                            selectedDateKey,
                                            selectedWaktu,
                                            chosenStatus,
                                            keepTag,
                                            currentRecord?.notes.orEmpty(),
                                            method
                                        )
                                    },
                                    onPickDiscipline = { chosenTag ->
                                        onSaveRecord(
                                            student,
                                            selectedDateKey,
                                            selectedWaktu,
                                            SholatStatus.JAMAAH,
                                            chosenTag,
                                            currentRecord?.notes.orEmpty(),
                                            currentRecord?.inputMethod ?: SholatHelper.METHOD_MANUAL
                                        )
                                    },
                                    onEditNote = { editingNoteTarget = student to currentRecord },
                                    onDelete = { currentRecord?.let(onDeleteRecord) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    SholatMonitoringTabContent(
                        students = scopedStudents,
                        records = sholatRecords,
                        selectedMonthKey = selectedMonthKey,
                        onChangeMonthKey = { selectedMonthKey = it },
                        isEn = isEn,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                else -> {
                    SholatRecapAndReportTabContent(
                        students = scopedStudents,
                        records = sholatRecords,
                        selectedDateKey = selectedDateKey,
                        selectedMonthKey = selectedMonthKey,
                        selectedWaktu = selectedWaktu,
                        onChangeMonthKey = { selectedMonthKey = it },
                        schoolProfile = schoolProfile,
                        waliName = currentWaliName.ifBlank { "Wali Asuh" },
                        isEn = isEn,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    }

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            femaleStudents = eligibleStudents,
            onScanned = { scannedCode ->
                val matched = StudentClassifier.findMatchingStudent(eligibleStudents, scannedCode)
                if (matched == null) {
                    scannerFeedback = ScannerFloatingFeedback(
                        id = System.nanoTime(),
                        status = ScannerFeedbackStatus.ERROR,
                        title = if (isEn) "Card Not Found" else "Kartu Tidak Dikenal",
                        subtitle = if (isEn) "Code \"$scannedCode\" is not registered" else "Kode \"$scannedCode\" tidak terdaftar di sekolah ini"
                    )
                } else {
                    val activeHaid = SholatHelper.findActiveHaidOnDate(matched, haidRecords, selectedDateKey)
                    val targetStatus = if (activeHaid != null) SholatStatus.UDZUR_HAID else SholatStatus.JAMAAH
                    val method = if (activeHaid != null) SholatHelper.METHOD_AUTO_HAID else SholatHelper.METHOD_BARCODE
                    onSaveRecord(
                        matched,
                        selectedDateKey,
                        selectedWaktu,
                        targetStatus,
                        SholatDisciplineTag.TEPAT_WAKTU,
                        "",
                        method
                    )
                    scannerFeedback = ScannerFloatingFeedback(
                        id = System.nanoTime(),
                        status = if (activeHaid != null) ScannerFeedbackStatus.WARNING else ScannerFeedbackStatus.SUCCESS,
                        title = matched.name,
                        subtitle = "${selectedWaktu.label(isEn)} • ${targetStatus.label(isEn)}"
                    )
                }
            },
            onDismiss = {
                showBarcodeScanner = false
                scannerFeedback = null
            },
            dialogTitle = if (isEn) "Scan Student Card — ${selectedWaktu.labelEn}" else "Scan Kartu Sholat ${selectedWaktu.labelId}",
            dialogSubtitle = if (isEn) {
                "Auto marks Congregation (or Udzur Haid if active)"
            } else {
                "Otomatis catat Jamaah Tepat Waktu (atau Udzur Haid bila aktif)"
            },
            quickPickTitle = if (isEn) "Quick Pick Student (${eligibleStudents.size}):" else "Pilih Cepat Siswa (${eligibleStudents.size}):",
            searchPlaceholder = if (isEn) "Search student or NIK/NIS..." else "Cari nama, kelas, kamar, atau NIK...",
            emptyListMessage = if (isEn) "No students registered yet." else "Belum ada siswa terdaftar.",
            accentColor = emeraldPrimary,
            accentBgColor = emeraldSoft,
            cardBgColor = Color.White,
            avatarBgColor = Color(0xFFD1FAE5),
            avatarTextColor = emeraldDark,
            titleTextColor = emeraldDark,
            continuousScan = true,
            sessionParticipantCount = scopedStudents.count { dailyMatrix[it.id]?.get(selectedWaktu) != null },
            floatingFeedback = scannerFeedback
        )
    }

    editingNoteTarget?.let { (student, existing) ->
        var draftNote by remember(student.id, existing?.id) { mutableStateOf(existing?.notes.orEmpty()) }
        val statusToSave = existing?.let { SholatStatus.fromCode(it.status) } ?: SholatStatus.JAMAAH
        val tagToSave = existing?.let { SholatDisciplineTag.fromCode(it.disciplineTag) } ?: SholatDisciplineTag.NONE
        AlertDialog(
            onDismissRequest = { editingNoteTarget = null },
            title = {
                Text(
                    text = if (isEn) "Note for ${student.name}" else "Catatan Pembinaan — ${student.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${selectedWaktu.label(isEn)} • ${SholatHelper.formatDateKeyDisplay(selectedDateKey, isEn)}",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = draftNote,
                        onValueChange = { draftNote = it },
                        label = { Text(if (isEn) "Caregiver / Parent Note" else "Catatan Pembina (tampil ke Orang Tua)") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveRecord(
                            student,
                            selectedDateKey,
                            selectedWaktu,
                            statusToSave,
                            tagToSave,
                            draftNote,
                            existing?.inputMethod ?: SholatHelper.METHOD_MANUAL
                        )
                        editingNoteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary)
                ) {
                    Text(if (isEn) "Save Note" else "Simpan Catatan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNoteTarget = null }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }
}

@Composable
private fun SholatStudentRowCard(
    student: ChildStudent,
    selectedWaktu: SholatWaktu,
    dayMap: Map<SholatWaktu, SholatRecord>,
    currentRecord: SholatRecord?,
    currentStatus: SholatStatus?,
    currentTag: SholatDisciplineTag,
    activeHaid: HaidRecord?,
    isFemale: Boolean,
    isEn: Boolean,
    onSelectWaktu: (SholatWaktu) -> Unit,
    onPickStatus: (SholatStatus) -> Unit,
    onPickDiscipline: (SholatDisciplineTag) -> Unit,
    onEditNote: () -> Unit,
    onDelete: () -> Unit
) {
    val availableStatuses = remember(isFemale, currentStatus) {
        if (isFemale || currentStatus == SholatStatus.UDZUR_HAID) {
            SholatStatus.entries
        } else {
            SholatStatus.entries.filter { it != SholatStatus.UDZUR_HAID }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = 1.dp,
            color = if (currentStatus == SholatStatus.ALFA) Color(0xFFFCA5A5)
            else if (currentStatus != null) Color(0xFFA7F3D0)
            else Color(0xFFE2E8F0)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_sholat_student_${student.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Header: Avatar, Name/Class, 5-Waktu mini S·D·A·M·I indicator strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    StudentAvatar(
                        name = student.name,
                        gender = student.gender,
                        size = 42.dp,
                        photoUrl = student.photoUrl,
                        photoFallbackUrl = student.photoFallbackUrl
                    )
                    Column {
                        Text(
                            text = student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = listOf(student.classGrade, student.dormitory)
                                .filter { it.isNotBlank() }
                                .joinToString(" • ")
                                .ifBlank { "-" },
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 5-Waktu daily mini strip (S D A M I)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SholatWaktu.entries.forEach { w ->
                        val rec = dayMap[w]
                        val st = rec?.let { SholatStatus.fromCode(it.status) }
                        val (bg, fg) = if (st != null) sholatStatusColors(st) else (Color(0xFFF1F5F9) to Color(0xFF94A3B8))
                        val isFocused = w == selectedWaktu
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(bg)
                                .border(
                                    width = if (isFocused) 1.5.dp else 0.5.dp,
                                    color = if (isFocused) Color(0xFF047857) else Color(0xFFCBD5E1),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onSelectWaktu(w) }
                        ) {
                            Text(
                                text = st?.shortCode ?: w.shortCode.lowercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = fg
                            )
                        }
                    }
                }
            }

            // Active Haid Suggestion Banner for Putri
            if (activeHaid != null && currentStatus != SholatStatus.UDZUR_HAID) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFDF2F8))
                        .border(1.dp, Color(0xFFFBCFE8), RoundedCornerShape(10.dp))
                        .clickable { onPickStatus(SholatStatus.UDZUR_HAID) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEn) {
                            "Active Period Logged (Day ${activeHaid.durationDays}) • Tap for Udzur Haid"
                        } else {
                            "Sedang Masa Haid (Hari ke-${activeHaid.durationDays}) • Ketuk untuk Udzur Syar'i"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9D174D),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (isEn) "Apply" else "Terapkan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBE185D)
                    )
                }
            }

            // Quick Status Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableStatuses.forEach { st ->
                    val isSelected = currentStatus == st
                    val (bg, fg) = sholatStatusColors(st)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) bg else Color(0xFFF8FAFC))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) fg else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(9.dp)
                            )
                            .clickable { onPickStatus(st) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("btn_sholat_status_${student.id}_${st.code}")
                    ) {
                        Text(
                            text = st.label(isEn),
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) fg else Color(0xFF475569)
                        )
                    }
                }
            }

            // Discipline Sub-tags (only shown if Jamaah is selected) + Note & Delete actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStatus == SholatStatus.JAMAAH) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SholatDisciplineTag.entries.forEach { tag ->
                            val selected = currentTag == tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(if (selected) Color(0xFF047857) else Color(0xFFF1F5F9))
                                    .clickable { onPickDiscipline(tag) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tag.label(isEn),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = if (currentRecord != null && currentRecord.notes.isNotBlank()) {
                            "\"${currentRecord.notes}\""
                        } else if (currentRecord != null) {
                            "${currentRecord.recordedBy} • ${selectedWaktu.label(isEn)}"
                        } else {
                            if (isEn) "Not marked for ${selectedWaktu.labelEn}" else "Belum diabsen untuk ${selectedWaktu.labelId}"
                        },
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditNote,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("btn_sholat_note_${student.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = if (isEn) "Add Note" else "Catatan Sholat",
                            tint = if (!currentRecord?.notes.isNullOrBlank()) Color(0xFF047857) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (currentRecord != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_sholat_delete_${student.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = if (isEn) "Delete Record" else "Hapus Absen",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
