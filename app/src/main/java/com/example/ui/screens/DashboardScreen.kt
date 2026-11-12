package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.dialogs.CreateShiftMessageDialog
import com.example.ui.dialogs.DaftarHaidSheet
import com.example.ui.dialogs.DaftarSuciSheet
import com.example.ui.dialogs.DepartReferralDialog
import com.example.ui.dialogs.DirectTreatmentDialog
import com.example.ui.dialogs.FiqihHaidDialog
import com.example.ui.dialogs.InputHaidDialog
import com.example.ui.dialogs.InputTreatmentResultDialog
import com.example.ui.dialogs.ShiftMessagesSheet
import com.example.ui.theme.StatusDeparted
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import com.example.util.TopOnAdManager
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
    val context = LocalContext.current
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val shiftMessages by viewModel.shiftMessages.collectAsStateWithLifecycle()
    val waliAccounts by viewModel.waliAccounts.collectAsStateWithLifecycle()
    val activeHaidRecords by viewModel.activeHaidRecords.collectAsStateWithLifecycle()
    val suciRecords by viewModel.suciRecords.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDirectTreatmentDialog by remember { mutableStateOf(false) }
    var showPuskesmasInfoSheet by remember { mutableStateOf(false) }
    var showShiftMessagesSheet by remember { mutableStateOf(false) }
    var showCreateShiftMessageDialog by remember { mutableStateOf(false) }
    var showInputHaidDialog by remember { mutableStateOf(false) }
    var showDaftarHaidSheet by remember { mutableStateOf(false) }
    var showDaftarSuciSheet by remember { mutableStateOf(false) }
    var showFiqihHaidDialog by remember { mutableStateOf(false) }
    var recordToRefer by remember { mutableStateOf<MedicalRecord?>(null) }
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
    var selectedPlanFilter by remember { mutableStateOf("Hari Ini") }

    val allFaskes = schoolProfile.getAllFaskes()
    val totalToday = todayPlans.size
    val totalSchoolStudents = students.size
    val progressRatio = (totalToday.toFloat() / 20f).coerceIn(0f, 1f)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    TopOnAdManager.onUserActionClick(context)
                    showCreateDialog = true
                },
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

                            allFaskes.forEach { faskesName ->
                                val count = todayPlans.count { it.faskes.contains(faskesName, ignoreCase = true) }
                                Text(
                                    text = "$faskesName: $count Anak",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

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
                            onClick = {
                                TopOnAdManager.onUserActionClick(context)
                                showGridTiles = !showGridTiles
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (showGridTiles) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
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
                            onNavigateToDirectTreatment = {
                                TopOnAdManager.onUserActionClick(context)
                                showDirectTreatmentDialog = true
                            },
                            onNavigateToRecords = onNavigateToRecords,
                            onNavigateToStudents = onNavigateToStudents,
                            onShowPuskesmasInfo = {
                                TopOnAdManager.onUserActionClick(context)
                                showPuskesmasInfoSheet = true
                            },
                            onOpenShiftMessages = {
                                TopOnAdManager.onUserActionClick(context)
                                showShiftMessagesSheet = true
                            },
                            onOpenCreateShiftMessage = {
                                TopOnAdManager.onUserActionClick(context)
                                showCreateShiftMessageDialog = true
                            },
                            onOpenInputHaid = {
                                TopOnAdManager.onUserActionClick(context)
                                showInputHaidDialog = true
                            },
                            onOpenDaftarHaid = {
                                TopOnAdManager.onUserActionClick(context)
                                showDaftarHaidSheet = true
                            },
                            onOpenDaftarSuci = {
                                TopOnAdManager.onUserActionClick(context)
                                showDaftarSuciSheet = true
                            },
                            onOpenFiqihHaid = {
                                TopOnAdManager.onUserActionClick(context)
                                showFiqihHaidDialog = true
                            },
                            studentsCount = students.size,
                            activeShiftMessagesCount = shiftMessages.count { !it.isCompleted },
                            urgentShiftMessagesCount = shiftMessages.count { it.priority == "URGENT" && !it.isCompleted },
                            activeHaidCount = activeHaidRecords.size,
                            suciCount = suciRecords.size,
                            istihadhahCount = activeHaidRecords.count { it.isIstihadhahWarning },
                            schoolShortName = schoolProfile.shortName
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

                        // Filter Chips: Hari Ini, Mendatang, Semua
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = selectedPlanFilter == "Hari Ini",
                                onClick = {
                                    TopOnAdManager.onUserActionClick(context)
                                    selectedPlanFilter = "Hari Ini"
                                },
                                label = { Text("Hari Ini (${todayPlans.size})", fontSize = 11.sp) },
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("chip_filter_today_plans")
                            )
                            FilterChip(
                                selected = selectedPlanFilter == "Mendatang",
                                onClick = {
                                    TopOnAdManager.onUserActionClick(context)
                                    selectedPlanFilter = "Mendatang"
                                },
                                label = { Text("Mendatang (${upcomingPlans.size})", fontSize = 11.sp) },
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("chip_filter_upcoming_plans")
                            )
                            FilterChip(
                                selected = selectedPlanFilter == "Semua",
                                onClick = {
                                    TopOnAdManager.onUserActionClick(context)
                                    selectedPlanFilter = "Semua"
                                },
                                label = { Text("Semua (${activePlans.size})", fontSize = 11.sp) },
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("chip_filter_all_plans")
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
                                        "Hari Ini" -> if (upcomingPlans.isNotEmpty()) {
                                            "Alhamdulillah, tidak ada anak yang dijadwalkan berobat hari ini. Terdapat ${upcomingPlans.size} rencana di jadwal 'Mendatang'."
                                        } else {
                                            "Alhamdulillah, tidak ada anak yang dijadwalkan berobat hari ini."
                                        }
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
                            onTreatHere = {
                                TopOnAdManager.onUserActionClick(context)
                                val uksFaskes = schoolProfile.primaryFaskesName.ifBlank { "UKS Sekolah" }
                                recordToDiagnose = record.copy(
                                    faskes = if (record.faskes.contains("UKS", ignoreCase = true)) record.faskes else uksFaskes
                                )
                            },
                            onDepart = {
                                TopOnAdManager.onUserActionClick(context)
                                recordToRefer = record
                            },
                            onDiagnose = {
                                TopOnAdManager.onUserActionClick(context)
                                recordToDiagnose = record
                            },
                            onDelete = {
                                TopOnAdManager.onUserActionClick(context)
                                viewModel.deleteRecord(record)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlanDialog(
            students = students,
            primaryFaskesName = schoolProfile.primaryFaskesName,
            secondaryFaskesName = schoolProfile.secondaryFaskesName,
            allFaskesList = schoolProfile.getAllFaskes(),
            onDismiss = { showCreateDialog = false },
            onConfirm = { student, faskes, complaint, planDate, time, escort, notes ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.createTreatmentPlan(student, faskes, complaint, planDate, time, escort, notes)
                showCreateDialog = false
            }
        )
    }

    recordToRefer?.let { record ->
        DepartReferralDialog(
            record = record,
            allFaskesList = schoolProfile.getAllFaskes(),
            defaultEscortName = record.escortName.ifBlank { waliName },
            onDismiss = { recordToRefer = null },
            onConfirmDeparture = { targetFaskes, escortName ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.markDepartedToFaskes(record.id, targetFaskes, escortName)
                recordToRefer = null
            },
            onDirectDiagnose = { targetFaskes, escortName ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.markDepartedToFaskes(record.id, targetFaskes, escortName)
                recordToDiagnose = record.copy(faskes = targetFaskes, escortName = escortName, status = "DEPARTED")
                recordToRefer = null
            }
        )
    }

    recordToDiagnose?.let { record ->
        InputTreatmentResultDialog(
            record = record,
            onDismiss = { recordToDiagnose = null },
            onConfirm = { diagnosis, actionsTaken, prescription, needsControl, controlDate, notes ->
                TopOnAdManager.onUserActionClick(context)
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
            primaryFaskesName = schoolProfile.primaryFaskesName,
            secondaryFaskesName = schoolProfile.secondaryFaskesName,
            allFaskesList = schoolProfile.getAllFaskes(),
            onDismiss = { showDirectTreatmentDialog = false },
            onConfirm = { student, faskes, complaint, diagnosis, actions, prescription, needsCtrl, ctrlDate, escort, notes ->
                TopOnAdManager.onUserActionClick(context)
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

    // Modal Bottom Sheet: Pesan Shift Berikutnya (Daftar Kartu Operan Jaga)
    if (showShiftMessagesSheet) {
        ShiftMessagesSheet(
            viewModel = viewModel,
            messages = shiftMessages,
            waliAccounts = waliAccounts,
            onDismiss = { showShiftMessagesSheet = false }
        )
    }

    // Modal Bottom Sheet: Form Tinggalkan Pesan Shift (Voice to Text)
    if (showCreateShiftMessageDialog) {
        CreateShiftMessageDialog(
            viewModel = viewModel,
            waliAccounts = waliAccounts,
            onDismiss = { showCreateShiftMessageDialog = false }
        )
    }

    // Modal Dialog: Pendataan Haid Siswi (Scan Barcode Kartu Siswa)
    if (showInputHaidDialog) {
        InputHaidDialog(
            students = students,
            activeHaidRecords = activeHaidRecords,
            waliName = waliName,
            schoolId = schoolProfile.tenantCode,
            onSaveHaid = { record ->
                viewModel.saveHaidRecord(record)
            },
            onDismiss = { showInputHaidDialog = false }
        )
    }

    // Modal Dialog: Daftar Siswi Sedang Haid
    if (showDaftarHaidSheet) {
        DaftarHaidSheet(
            activeHaidRecords = activeHaidRecords,
            onCompleteHaid = { id ->
                viewModel.completeHaidRecord(id)
            },
            onDeleteRecord = { rec ->
                viewModel.deleteHaidRecord(rec)
            },
            onOpenInputHaid = {
                showDaftarHaidSheet = false
                showInputHaidDialog = true
            },
            onDismiss = { showDaftarHaidSheet = false }
        )
    }

    // Modal Dialog: Daftar Siswi Suci & Riwayat Siklus
    if (showDaftarSuciSheet) {
        DaftarSuciSheet(
            suciRecords = suciRecords,
            onDeleteRecord = { rec ->
                viewModel.deleteHaidRecord(rec)
            },
            onDismiss = { showDaftarSuciSheet = false }
        )
    }

    // Modal Dialog: Panduan Aplikasi & Fiqih Haid Wanita
    if (showFiqihHaidDialog) {
        FiqihHaidDialog(
            onDismiss = { showFiqihHaidDialog = false }
        )
    }
}

@Composable
fun DashboardSickChildItem(
    number: Int,
    record: MedicalRecord,
    onTreatHere: () -> Unit,
    onDepart: () -> Unit,
    onDiagnose: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = if (record.faskes.contains("Wates", ignoreCase = true)) Color(0xFF00695C) else Color(0xFF2E7D32)
    val isToday = DateUtils.isToday(record.planDate)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sick_child_item_${record.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Accent Color Stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Header: Nama Siswa & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$number. ${record.studentName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = record.studentDormitory,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(status = record.status)
                }

                // VIEW TANGGAL KE FASILITAS KESEHATAN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else accentColor.copy(alpha = 0.08f)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
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
                                    tint = accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tanggal ke Fasilitas Kesehatan:",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(accentColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = DateUtils.getRelativeDateLabel(record.planDate),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                        }

                        Text(
                            text = "${DateUtils.formatFullDate(record.planDate)} • Pukul ${record.departureTime} WIB",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Tujuan Faskes:",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FaskesBadge(faskes = record.faskes)
                        }
                    }
                }

                // Compact Keluhan
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Keluhan: ${record.complaint}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Escort & Action Row (Tight layout)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (record.escortName.isNotBlank()) "👤 ${record.escortName}" else "",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Hapus",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (record.status == "PLANNED" || record.status == "MENUNGGU" || record.status == "SIAP JALAN") {
                            // Pilihan 1: Dirawat di Sini (UKS)
                            Button(
                                onClick = onTreatHere,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_treat_here_${record.id}"),
                                contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Healing,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Dirawat di Sini", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Pilihan 2: Berangkatkan / Rujuk ke Faskes
                            Button(
                                onClick = onDepart,
                                colors = ButtonDefaults.buttonColors(containerColor = StatusDeparted),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_depart_${record.id}"),
                                contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Berangkatkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (record.status == "DEPARTED" || record.status == "DIANTER") {
                            Button(
                                onClick = onDiagnose,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_input_result_${record.id}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Input Diagnosa", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
