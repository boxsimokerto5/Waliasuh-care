@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.ui.HealthViewModel
import com.example.ui.components.FaskesBadge
import com.example.ui.components.QuickMenuTiles
import com.example.ui.components.StatusBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.dialogs.ChecklistPuasaSheet
import com.example.ui.dialogs.CreatePlanDialog
import com.example.ui.dialogs.CreateShiftMessageDialog
import com.example.ui.dialogs.DaftarHaidSheet
import com.example.ui.dialogs.DaftarPuasaSheet
import com.example.ui.dialogs.DaftarSuciSheet
import com.example.ui.dialogs.DepartReferralDialog
import com.example.ui.dialogs.DirectTreatmentDialog
import com.example.ui.dialogs.FaskesReferralSheet
import com.example.ui.dialogs.FiqihHaidDialog
import com.example.ui.dialogs.InputHaidDialog
import com.example.ui.dialogs.InputPuasaDialog
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalHospital
import com.example.ui.dialogs.InputTreatmentResultDialog
import com.example.ui.dialogs.PrintPdfDialog
import com.example.ui.dialogs.RankingPuasaSheet
import com.example.ui.dialogs.ShareWhatsAppDialog
import com.example.ui.dialogs.ShiftHistorySheet
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
import androidx.compose.ui.text.style.TextOverflow

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
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val fastingSessions by viewModel.fastingSessions.collectAsStateWithLifecycle()
    val fastingRecords by viewModel.fastingRecords.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDirectTreatmentDialog by remember { mutableStateOf(false) }
    var showPuskesmasInfoSheet by remember { mutableStateOf(false) }
    var showShiftMessagesSheet by remember { mutableStateOf(false) }
    var showCreateShiftMessageDialog by remember { mutableStateOf(false) }
    var showShiftHistorySheet by remember { mutableStateOf(false) }
    var showInputHaidDialog by remember { mutableStateOf(false) }
    var showDaftarHaidSheet by remember { mutableStateOf(false) }
    var showDaftarSuciSheet by remember { mutableStateOf(false) }
    var showFiqihHaidDialog by remember { mutableStateOf(false) }
    var showInputPuasaDialog by remember { mutableStateOf(false) }
    var showDaftarPuasaSheet by remember { mutableStateOf(false) }
    var showChecklistPuasaSheet by remember { mutableStateOf(false) }
    var showRankingPuasaSheet by remember { mutableStateOf(false) }
    var recordToRefer by remember { mutableStateOf<MedicalRecord?>(null) }
    var recordToDiagnose by remember { mutableStateOf<MedicalRecord?>(null) }
    var recordPlanToEdit by remember { mutableStateOf<MedicalRecord?>(null) }
    var recordTreatmentToEdit by remember { mutableStateOf<MedicalRecord?>(null) }
    var selectedInpatientRecordForDetail by remember { mutableStateOf<MedicalRecord?>(null) }
    var inpatientPickerForFaskes by remember { mutableStateOf<Pair<String, List<MedicalRecord>>?>(null) }
    var recordToShare by remember { mutableStateOf<MedicalRecord?>(null) }
    var studentForPdfPrint by remember { mutableStateOf<ChildStudent?>(null) }
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
    val inpatientRecords = remember(allRecords) {
        allRecords
            .filter { it.isInpatient && it.status != "CANCELLED" }
            .sortedByDescending { it.treatmentDate ?: it.planDate }
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
                                val faskesInpatients = inpatientRecords.filter {
                                    it.faskes.contains(faskesName, ignoreCase = true)
                                }
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "$faskesName: $count Anak",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                    if (faskesInpatients.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFDC2626).copy(alpha = 0.12f))
                                                .clickable {
                                                    TopOnAdManager.onUserActionClick(context)
                                                    if (faskesInpatients.size == 1) {
                                                        selectedInpatientRecordForDetail = faskesInpatients.first()
                                                    } else {
                                                        inpatientPickerForFaskes = faskesName to faskesInpatients
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                                .testTag("badge_inpatient_${faskesName.lowercase().replace(" ", "_")}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocalHospital,
                                                    contentDescription = "Rawat Inap",
                                                    tint = Color(0xFFDC2626),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "${faskesInpatients.size} Anak Rawat Inap",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFB91C1C)
                                                )
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                    contentDescription = null,
                                                    tint = Color(0xFFB91C1C),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Jika ada anak rawat inap di faskes custom / "Lainnya" di luar daftar utama sekolah
                            val customFaskesInpatients = inpatientRecords.filter { rec ->
                                allFaskes.none { f -> rec.faskes.contains(f, ignoreCase = true) }
                            }.groupBy { it.faskes.ifBlank { "Faskes Rujukan" } }

                            customFaskesInpatients.forEach { (faskesName, list) ->
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "$faskesName: 0 Anak",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFDC2626).copy(alpha = 0.12f))
                                            .clickable {
                                                TopOnAdManager.onUserActionClick(context)
                                                if (list.size == 1) {
                                                    selectedInpatientRecordForDetail = list.first()
                                                } else {
                                                    inpatientPickerForFaskes = faskesName to list
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                            .testTag("badge_inpatient_custom")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalHospital,
                                                contentDescription = "Rawat Inap",
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "${list.size} Anak Rawat Inap",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB91C1C)
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = Color(0xFFB91C1C),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Geser untuk lebih banyak fitur",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = WaliPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
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
                            onOpenShiftHistory = {
                                TopOnAdManager.onUserActionClick(context)
                                showShiftHistorySheet = true
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
                            onOpenInputPuasa = {
                                TopOnAdManager.onUserActionClick(context)
                                showInputPuasaDialog = true
                            },
                            onOpenDaftarPuasa = {
                                TopOnAdManager.onUserActionClick(context)
                                showDaftarPuasaSheet = true
                            },
                            onOpenChecklistPuasa = {
                                TopOnAdManager.onUserActionClick(context)
                                showChecklistPuasaSheet = true
                            },
                            onOpenRankingPuasa = {
                                TopOnAdManager.onUserActionClick(context)
                                showRankingPuasaSheet = true
                            },
                            studentsCount = students.size,
                            activeShiftMessagesCount = shiftMessages.count { !it.isCompleted && DateUtils.isToday(it.followUpDate) },
                            urgentShiftMessagesCount = shiftMessages.count { it.priority == "URGENT" && !it.isCompleted && DateUtils.isToday(it.followUpDate) },
                            pastShiftMessagesCount = shiftMessages.count { DateUtils.isBeforeToday(it.followUpDate) },
                            uncompletedPastShiftCount = shiftMessages.count { !it.isCompleted && DateUtils.isBeforeToday(it.followUpDate) },
                            activeHaidCount = activeHaidRecords.size,
                            suciCount = suciRecords.size,
                            istihadhahCount = activeHaidRecords.count { it.isIstihadhahWarning },
                            fastingSessionsCount = fastingSessions.size,
                            todayFastingCount = run {
                                val latestSession = fastingSessions.firstOrNull { it.isActive } ?: fastingSessions.firstOrNull()
                                if (latestSession == null) 0 else fastingRecords.count { it.sessionId == latestSession.id }
                            },
                            schoolShortName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
                            faskesBannerSubtitle = schoolProfile.getReferralFaskesSummary()
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daftar Rencana Berobat",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = todayDateFormatted,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
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
                            onEdit = {
                                TopOnAdManager.onUserActionClick(context)
                                recordPlanToEdit = record
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

    recordPlanToEdit?.let { editingRecord ->
        CreatePlanDialog(
            students = students,
            primaryFaskesName = schoolProfile.primaryFaskesName,
            secondaryFaskesName = schoolProfile.secondaryFaskesName,
            allFaskesList = schoolProfile.getAllFaskes(),
            existingRecord = editingRecord,
            onDismiss = { recordPlanToEdit = null },
            onConfirm = { student, faskes, complaint, planDate, time, escort, notes ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.editTreatmentPlan(
                    existingRecord = editingRecord,
                    student = student,
                    faskes = faskes,
                    complaint = complaint,
                    planDate = planDate,
                    departureTime = time,
                    escortName = escort,
                    notes = notes
                )
                recordPlanToEdit = null
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
            onConfirm = { diagnosis, actionsTaken, prescription, needsControl, controlDate, notes, isInpatient, inpatientRoom ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.submitTreatmentResult(
                    record = record,
                    diagnosis = diagnosis,
                    actionsTaken = actionsTaken,
                    prescription = prescription,
                    needsControl = needsControl,
                    controlDate = controlDate,
                    additionalNotes = notes,
                    isInpatient = isInpatient,
                    inpatientRoom = inpatientRoom
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
            onConfirm = { student, faskes, complaint, diagnosis, actions, prescription, needsCtrl, ctrlDate, escort, notes, isInpatient, inpatientRoom ->
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
                    notes = notes,
                    isInpatient = isInpatient,
                    inpatientRoom = inpatientRoom
                )
                showDirectTreatmentDialog = false
            }
        )
    }

    recordTreatmentToEdit?.let { editingRecord ->
        DirectTreatmentDialog(
            students = students,
            primaryFaskesName = schoolProfile.primaryFaskesName,
            secondaryFaskesName = schoolProfile.secondaryFaskesName,
            allFaskesList = schoolProfile.getAllFaskes(),
            existingRecord = editingRecord,
            onDismiss = { recordTreatmentToEdit = null },
            onConfirm = { student, faskes, complaint, diagnosis, actions, prescription, needsCtrl, ctrlDate, escort, notes, isInpatient, inpatientRoom ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.editCompletedTreatment(
                    existingRecord = editingRecord,
                    student = student,
                    faskes = faskes,
                    complaint = complaint,
                    diagnosis = diagnosis,
                    actionsTaken = actions,
                    prescription = prescription,
                    needsControl = needsCtrl,
                    controlDate = ctrlDate,
                    escortName = escort,
                    notes = notes,
                    isInpatient = isInpatient,
                    inpatientRoom = inpatientRoom
                )
                recordTreatmentToEdit = null
            }
        )
    }

    // Picker bila ada >1 anak rawat inap di faskes yang sama
    inpatientPickerForFaskes?.let { (faskesTitle, records) ->
        ModalBottomSheet(
            onDismissRequest = { inpatientPickerForFaskes = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Anak Sedang Rawat Inap",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$faskesTitle • Ketuk anak untuk membuka kartu tindakan",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                records.forEach { rec ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFDC2626).copy(alpha = 0.06f)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                inpatientPickerForFaskes = null
                                selectedInpatientRecordForDetail = rec
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rec.studentName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = buildString {
                                        append(rec.diagnosis.ifBlank { rec.complaint })
                                        if (rec.inpatientRoom.isNotBlank()) append(" • Ruang ${rec.inpatientRoom}")
                                    },
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Buka Kartu Tindakan",
                                tint = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }
    }

    // Kartu Detail Tindakan langsung saat notif Rawat Inap di Dashboard diklik
    selectedInpatientRecordForDetail?.let { record ->
        val student = students.find { it.id == record.studentId }
        val allStudentRecords = allRecords.filter { it.studentId == record.studentId }
            .sortedByDescending { it.treatmentDate ?: it.planDate }
        val calNow = Calendar.getInstance()
        val curM = calNow.get(Calendar.MONTH)
        val curY = calNow.get(Calendar.YEAR)
        val thisMonthCount = allStudentRecords.count { rec ->
            val cal = Calendar.getInstance().apply { timeInMillis = rec.treatmentDate ?: rec.planDate }
            cal.get(Calendar.MONTH) == curM && cal.get(Calendar.YEAR) == curY
        }

        MedicalRecordDetailSheet(
            record = record,
            student = student,
            allStudentRecords = allStudentRecords,
            thisMonthCount = thisMonthCount,
            onDismiss = { selectedInpatientRecordForDetail = null },
            onPrintPdf = {
                TopOnAdManager.onUserActionClick(context)
                student?.let { studentForPdfPrint = it }
            },
            onShare = {
                TopOnAdManager.onUserActionClick(context)
                selectedInpatientRecordForDetail = null
                recordToShare = record
            },
            onEdit = {
                TopOnAdManager.onUserActionClick(context)
                selectedInpatientRecordForDetail = null
                recordTreatmentToEdit = record
            },
            onDelete = {
                TopOnAdManager.onUserActionClick(context)
                viewModel.deleteRecord(record)
                selectedInpatientRecordForDetail = null
            },
            onDischargeInpatient = { updDiagnosis, updPrescription, needsCtrl, ctrlDate, dischargeNotes ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.dischargeInpatient(
                    record = record,
                    updatedDiagnosis = updDiagnosis,
                    updatedPrescription = updPrescription,
                    needsControl = needsCtrl,
                    controlDate = ctrlDate,
                    dischargeNotes = dischargeNotes
                )
                Toast.makeText(context, "${record.studentName} ditandai sudah pulang rawat inap", Toast.LENGTH_SHORT).show()
                selectedInpatientRecordForDetail = null
            }
        )
    }

    recordToShare?.let { record ->
        ShareWhatsAppDialog(
            record = record,
            schoolProfile = schoolProfile,
            onDismiss = { recordToShare = null }
        )
    }

    studentForPdfPrint?.let { student ->
        PrintPdfDialog(
            student = student,
            allRecords = allRecords,
            waliName = waliName,
            schoolProfile = schoolProfile,
            onDismiss = { studentForPdfPrint = null }
        )
    }

    if (showPuskesmasInfoSheet) {
        FaskesReferralSheet(
            schoolProfile = schoolProfile,
            title = "Faskes & Puskesmas Rujukan",
            onDismiss = { showPuskesmasInfoSheet = false }
        )
    }

    // Sinkronkan pesan & centang operan shift online setiap kali sheet pesan/riwayat dibuka
    LaunchedEffect(showShiftMessagesSheet, showShiftHistorySheet) {
        if (showShiftMessagesSheet || showShiftHistorySheet) {
            viewModel.syncShiftMessagesOnlineQuietly()
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

    // Modal Bottom Sheet: Riwayat Operan (Arsip Pesan Lewat Hari Ini)
    if (showShiftHistorySheet) {
        ShiftHistorySheet(
            viewModel = viewModel,
            messages = shiftMessages,
            onDismiss = { showShiftHistorySheet = false }
        )
    }

    var preselectedHaidStudent by remember { mutableStateOf<com.example.data.model.ChildStudent?>(null) }
    var preselectedTissueVerified by remember { mutableStateOf(false) }

    // Modal Bottom Sheet: Pendataan Haid Siswi (Scan Barcode Kartu Siswa)
    if (showInputHaidDialog) {
        InputHaidDialog(
            students = students,
            activeHaidRecords = activeHaidRecords,
            suciRecords = suciRecords,
            waliName = waliName,
            schoolId = schoolProfile.tenantCode,
            initialStudent = preselectedHaidStudent,
            initialTissueVerified = preselectedTissueVerified,
            onSaveHaid = { record ->
                viewModel.saveHaidRecord(record)
            },
            onCompleteHaid = { id ->
                viewModel.completeHaidRecord(id)
            },
            onDismiss = {
                showInputHaidDialog = false
                preselectedHaidStudent = null
                preselectedTissueVerified = false
            }
        )
    }

    // Modal Bottom Sheet: Daftar Siswi Sedang Haid
    if (showDaftarHaidSheet) {
        DaftarHaidSheet(
            activeHaidRecords = activeHaidRecords,
            schoolProfile = schoolProfile,
            waliName = waliName,
            onCompleteHaid = { id ->
                viewModel.completeHaidRecord(id)
            },
            onUpdateRecord = { rec ->
                viewModel.updateHaidRecord(rec)
            },
            onDeleteRecord = { rec ->
                viewModel.deleteHaidRecord(rec)
            },
            onOpenInputHaid = {
                showDaftarHaidSheet = false
                preselectedHaidStudent = null
                preselectedTissueVerified = false
                showInputHaidDialog = true
            },
            onDismiss = { showDaftarHaidSheet = false }
        )
    }

    // Modal Bottom Sheet: Daftar Siswi Suci & Timeline Masa Suci
    if (showDaftarSuciSheet) {
        DaftarSuciSheet(
            suciRecords = suciRecords,
            activeHaidRecords = activeHaidRecords,
            onDeleteRecord = { rec ->
                viewModel.deleteHaidRecord(rec)
            },
            onMoveToHaid = { rec ->
                val matchedStudent = students.firstOrNull {
                    (rec.studentId > 0L && it.id == rec.studentId) ||
                        (rec.studentNik.isNotBlank() && it.nik.equals(rec.studentNik, ignoreCase = true)) ||
                        (rec.studentNis.isNotBlank() && it.nis.equals(rec.studentNis, ignoreCase = true))
                } ?: com.example.data.model.ChildStudent(
                    id = rec.studentId,
                    name = rec.studentName,
                    nis = rec.studentNis,
                    gender = "P",
                    age = 15,
                    dormitory = rec.studentDorm,
                    classGrade = rec.studentClass,
                    nik = rec.studentNik,
                    schoolId = rec.schoolId
                )
                preselectedHaidStudent = matchedStudent
                preselectedTissueVerified = true
                showDaftarSuciSheet = false
                showInputHaidDialog = true
            },
            onDismiss = { showDaftarSuciSheet = false }
        )
    }

    // Modal Bottom Sheet: Panduan Aplikasi & Fiqih Haid Wanita
    if (showFiqihHaidDialog) {
        FiqihHaidDialog(
            onDismiss = { showFiqihHaidDialog = false }
        )
    }

    // Sinkronkan sesi & checklist sahur/berbuka puasa online setiap kali modul puasa dibuka
    LaunchedEffect(
        showInputPuasaDialog,
        showDaftarPuasaSheet,
        showChecklistPuasaSheet,
        showRankingPuasaSheet
    ) {
        if (showInputPuasaDialog || showDaftarPuasaSheet || showChecklistPuasaSheet || showRankingPuasaSheet) {
            viewModel.syncFastingOnlineQuietly()
        }
    }

    // Modal Bottom Sheet 1 (Puasa): Input Puasa (Sesi, Scan Barcode & Cari Siswa)
    if (showInputPuasaDialog) {
        InputPuasaDialog(
            sessions = fastingSessions,
            students = students,
            fastingRecords = fastingRecords,
            onRegisterStudent = { session, student, onResult ->
                viewModel.registerStudentForFasting(session, student, onResult)
            },
            onDismiss = { showInputPuasaDialog = false }
        )
    }

    // Modal Bottom Sheet 2 (Puasa): Daftar Siswa Puasa (SD/SMP/SMA + Total & PDF)
    if (showDaftarPuasaSheet) {
        DaftarPuasaSheet(
            sessions = fastingSessions,
            fastingRecords = fastingRecords,
            schoolProfile = schoolProfile,
            waliName = waliName,
            onDeleteRecord = { rec ->
                viewModel.deleteFastingRecord(rec)
            },
            onDismiss = { showDaftarPuasaSheet = false }
        )
    }

    // Modal Bottom Sheet 3 (Puasa): Checklist Sahur & Berbuka + PDF
    if (showChecklistPuasaSheet) {
        ChecklistPuasaSheet(
            sessions = fastingSessions,
            fastingRecords = fastingRecords,
            schoolProfile = schoolProfile,
            waliName = waliName,
            onToggleSahur = { rec ->
                viewModel.toggleFastingSahur(rec)
            },
            onToggleBuka = { rec ->
                viewModel.toggleFastingBuka(rec)
            },
            onDismiss = { showChecklistPuasaSheet = false }
        )
    }

    // Modal Bottom Sheet 4 (Puasa): Ranking Siswa & History Sesi Puasa
    if (showRankingPuasaSheet) {
        RankingPuasaSheet(
            sessions = fastingSessions,
            fastingRecords = fastingRecords,
            onDismiss = { showRankingPuasaSheet = false }
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
    onEdit: () -> Unit = {},
    onDelete: () -> Unit
) {
    val accentColor = if (record.faskes.contains("Wates", ignoreCase = true)) Color(0xFF00695C) else Color(0xFF2E7D32)
    val isToday = DateUtils.isToday(record.planDate)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sick_child_item_${record.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
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
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Baris 1: Nama + Asrama di kiri, Status + Edit/Hapus di kanan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$number. ${record.studentName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = record.studentDormitory,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        StatusBadge(status = record.status)
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("btn_edit_plan_${record.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Rencana",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
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
                    }
                }

                // Baris 2: Strip Jadwal & Tujuan Faskes sejajar (1 baris ringkas tanpa pengulangan tanggal)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f)
                            else accentColor.copy(alpha = 0.08f)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Jadwal ke Faskes",
                            tint = accentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        val scheduleLabel = buildString {
                            append(DateUtils.getRelativeDateLabel(record.planDate))
                            if (record.departureTime.isNotBlank()) {
                                append(" • ${record.departureTime} WIB")
                            }
                        }
                        Text(
                            text = scheduleLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    FaskesBadge(faskes = record.faskes)
                }

                // Baris 3: Keluhan
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                append("Keluhan: ")
                            }
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                append(record.complaint.ifBlank { "-" })
                            }
                        },
                        fontSize = 11.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Baris 4: Info Pendamping & Penginput sejajar rapi (tidak terjepit tombol)
                val escortLabel = record.escortName.trim()
                val creatorLabel = record.createdBy.ifBlank { escortLabel.ifBlank { "-" } }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (escortLabel.isNotBlank()) "👤 $escortLabel" else "",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Input: $creatorLabel",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Baris 5: Tombol Aksi seimbang memanfaatkan ruang penuh
                if (record.status == "PLANNED" || record.status == "MENUNGGU" || record.status == "SIAP JALAN") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onTreatHere,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("btn_treat_here_${record.id}"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Healing,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Dirawat di Sini",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = onDepart,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusDeparted),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("btn_depart_${record.id}"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Berangkatkan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else if (record.status == "DEPARTED" || record.status == "DIANTER") {
                    Button(
                        onClick = onDiagnose,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
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
