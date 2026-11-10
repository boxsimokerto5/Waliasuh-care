package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.ui.HealthViewModel
import com.example.ui.components.FaskesBadge
import com.example.ui.components.QuickMenuTiles
import com.example.ui.components.StatusBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.dialogs.CreatePlanDialog
import com.example.ui.dialogs.DirectTreatmentDialog
import com.example.ui.dialogs.InputTreatmentResultDialog
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import java.util.Calendar
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.rememberModalBottomSheetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HealthViewModel,
    waliName: String,
    activePlans: List<MedicalRecord>,
    students: List<ChildStudent>,
    onNavigateToPlan: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDirectTreatmentDialog by remember { mutableStateOf(false) }
    var showPuskesmasInfoSheet by remember { mutableStateOf(false) }
    var recordToDiagnose by remember { mutableStateOf<MedicalRecord?>(null) }
    var showGridTiles by remember { mutableStateOf(true) }

    val todayDateFormatted = remember {
        DateUtils.formatFullDate(Calendar.getInstance().timeInMillis)
    }

    val todayPlans = remember(activePlans) {
        activePlans.filter { DateUtils.isToday(it.planDate) }
    }
    val upcomingPlans = remember(activePlans) {
        activePlans.filter { !DateUtils.isToday(it.planDate) }
    }
    var selectedPlanFilter by remember { mutableStateOf("Semua") }

    // Counts for today
    val watesTodayCount = todayPlans.count { it.faskes.contains("Wates", ignoreCase = true) }
    val klatenTodayCount = todayPlans.count { it.faskes.contains("Klaten", ignoreCase = true) || it.faskes.contains("Plosoklaten", ignoreCase = true) }
    val totalToday = todayPlans.size
    val totalSchoolStudents = if (students.isNotEmpty()) students.size else 342
    val progressRatio = (totalToday.toFloat() / 20f).coerceIn(0f, 1f)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = WaliPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_dashboard_add")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Anak Sakit / Rencana",
                    modifier = Modifier.size(26.dp)
                )
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
                    .widthIn(max = 640.dp),
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // Wali Subtitle Row (from Image 3: "Wali: Sari Devi")
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wali: $waliName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable(onClick = onNavigateToProfile)
                        )
                    }
                }

                // Summary Card from Image 3
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_summary_today"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Rencana Berobat Hari Ini ($todayDateFormatted)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(1.dp))

                            Text(
                                text = "Puskesmas Wates: $watesTodayCount Anak",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Puskesmas Klaten: $klatenTodayCount Anak",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = WaliPrimary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$totalToday Anak Dijadwalkan Hari Ini ($totalToday/$totalSchoolStudents Siswa)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (upcomingPlans.isNotEmpty()) {
                                    Text(
                                        text = "📅 +${upcomingPlans.size} mendatang",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WaliPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Menu Tiles with Cute 3D Cartoon Icons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Layanan Kesehatan Anak",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { showGridTiles = !showGridTiles },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (showGridTiles) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = "Toggle Grid Menu",
                                tint = WaliPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                if (showGridTiles) {
                    item {
                        QuickMenuTiles(
                            onNavigateToPlan = onNavigateToPlan,
                            onNavigateToDirectTreatment = { showDirectTreatmentDialog = true },
                            onNavigateToRecords = onNavigateToRecords,
                            onNavigateToStudents = onNavigateToStudents,
                            onShowPuskesmasInfo = { showPuskesmasInfoSheet = true }
                        )
                    }
                }

                // Section Header: Daftar Rencana Berobat ke Faskes
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daftar Rencana Berobat ke Faskes",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Hari ini: $todayDateFormatted",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Filter Chips: Semua, Hari Ini, Mendatang
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = selectedPlanFilter == "Semua",
                                onClick = { selectedPlanFilter = "Semua" },
                                label = { Text("Semua (${activePlans.size})", fontSize = 11.sp) },
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("chip_filter_all_plans")
                            )
                            FilterChip(
                                selected = selectedPlanFilter == "Hari Ini",
                                onClick = { selectedPlanFilter = "Hari Ini" },
                                label = { Text("Hari Ini (${todayPlans.size})", fontSize = 11.sp) },
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("chip_filter_today_plans")
                            )
                            FilterChip(
                                selected = selectedPlanFilter == "Mendatang",
                                onClick = { selectedPlanFilter = "Mendatang" },
                                label = { Text("Mendatang (${upcomingPlans.size})", fontSize = 11.sp) },
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("chip_filter_upcoming_plans")
                            )
                        }
                    }
                }

                val displayedPlans = when (selectedPlanFilter) {
                    "Hari Ini" -> todayPlans
                    "Mendatang" -> upcomingPlans
                    else -> activePlans
                }

                if (displayedPlans.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (selectedPlanFilter) {
                                        "Hari Ini" -> "Alhamdulillah, tidak ada anak yang dijadwalkan berobat hari ini."
                                        "Mendatang" -> "Belum ada rencana berobat untuk tanggal mendatang."
                                        else -> "Alhamdulillah, belum ada rencana berobat aktif."
                                    },
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(displayedPlans, key = { _, record -> record.id }) { index, record ->
                        DashboardSickChildItem(
                            number = index + 1,
                            record = record,
                            students = students,
                            onUpdateStatus = { newStatus -> viewModel.updateStatus(record.id, newStatus) },
                            onDiagnose = { recordToDiagnose = record },
                            onDelete = { viewModel.deleteRecord(record) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlanDialog(
            students = students,
            onDismiss = { showCreateDialog = false },
            onConfirm = { student, faskes, complaint, planDate, time, escort, notes ->
                viewModel.createTreatmentPlan(student, faskes, complaint, planDate, time, escort, notes)
                showCreateDialog = false
            }
        )
    }

    recordToDiagnose?.let { record ->
        InputTreatmentResultDialog(
            record = record,
            onDismiss = { recordToDiagnose = null },
            onConfirm = { diagnosis, actionsTaken, prescription, needsControl, controlDate, notes ->
                viewModel.submitTreatmentResult(
                    record = record,
                    diagnosis = diagnosis,
                    actionsTaken = actionsTaken,
                    prescription = prescription,
                    needsControl = needsControl,
                    controlDate = controlDate,
                    additionalNotes = notes
                )
                recordToDiagnose = null
            }
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

    if (showPuskesmasInfoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPuskesmasInfoSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Faskes & Puskesmas Rujukan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Fasilitas kesehatan resmi mitra Sekolah Rakyat SRT 1 Kabupaten Kediri:",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("1. Puskesmas Wates", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("UGD 24 Jam", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WaliPrimary)
                            }
                            Text("Jl. Raya Wates No. 45, Kab. Kediri", fontSize = 11.sp)
                            Text("Telepon: (0354) 441012 • Siaga Rujukan Anak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("2. Puskesmas Plosoklaten", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("UGD 24 Jam", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WaliPrimary)
                            }
                            Text("Jl. Raya Plosoklaten No. 12, Kab. Kediri", fontSize = 11.sp)
                            Text("Telepon: (0354) 479155 • Siaga Rujukan Anak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = { showPuskesmasInfoSheet = false },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Tutup", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardSickChildItem(
    number: Int,
    record: MedicalRecord,
    students: List<ChildStudent>,
    onUpdateStatus: (String) -> Unit,
    onDiagnose: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    // Find student details for age and gender
    val student = students.find { it.id == record.studentId }
    val ageText = student?.let { " (${it.age}th)" } ?: ""
    val gender = student?.gender ?: "L"

    val isToday = DateUtils.isToday(record.planDate)
    val isTomorrow = DateUtils.isTomorrow(record.planDate)
    val planDateFormatted = DateUtils.formatFullDate(record.planDate)
    val relativeDateLabel = DateUtils.getRelativeDateLabel(record.planDate)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sick_child_item_${record.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Student Portrait Avatar (from Image 3)
            StudentAvatar(
                name = record.studentName,
                gender = gender,
                size = 42.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Student Info with prominent Tanggal ke Faskes
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$number. ${record.studentName}$ageText",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    StatusBadge(status = record.status)
                }

                if (record.studentDormitory.isNotBlank()) {
                    Text(
                        text = record.studentDormitory,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // VIEW TANGGAL KE FASILITAS KESEHATAN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else Color(0xFFFEF3C7).copy(alpha = 0.6f)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Tanggal ke Faskes",
                                    tint = if (isToday) WaliPrimary else Color(0xFFB45309),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tanggal ke Faskes:",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) WaliPrimary else Color(0xFFB45309)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isToday) WaliPrimary.copy(alpha = 0.15f)
                                        else Color(0xFFF59E0B).copy(alpha = 0.25f)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = relativeDateLabel,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) WaliPrimary else Color(0xFFB45309)
                                )
                            }
                        }

                        Text(
                            text = "$planDateFormatted • Pukul ${record.departureTime} WIB",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Tujuan:",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FaskesBadge(faskes = record.faskes)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "• Keluhan: ${record.complaint}",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (record.escortName.isNotBlank()) {
                    Text(
                        text = "• Pengantar: ${record.escortName}",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3-dots action menu (from Image 3)
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opsi Aksi",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tandai Siap Jalan", fontSize = 12.sp)
                            }
                        },
                        onClick = {
                            onUpdateStatus("Siap Jalan")
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tandai Dianter", fontSize = 12.sp)
                            }
                        },
                        onClick = {
                            onUpdateStatus("Dianter")
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Input Diagnosa Selesai", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onDiagnose()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hapus Rencana", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
