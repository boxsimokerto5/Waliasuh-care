package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.ui.components.ControlBadge
import com.example.ui.components.FaskesBadge
import com.example.ui.dialogs.DirectTreatmentDialog
import com.example.ui.dialogs.ShareWhatsAppDialog
import com.example.util.DateUtils

@Composable
fun CompletedHistoryScreen(
    viewModel: HealthViewModel,
    completedRecords: List<MedicalRecord>,
    students: List<ChildStudent>,
    searchQuery: String,
    selectedFaskes: String,
    modifier: Modifier = Modifier
) {
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    var showDirectTreatmentDialog by remember { mutableStateOf(false) }
    var recordToShare by remember { mutableStateOf<MedicalRecord?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDirectTreatmentDialog = true },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.testTag("fab_direct_treatment")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = "Pengobatan Langsung", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = "Pengobatan Langsung", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                    .widthIn(max = 680.dp)
            ) {
                // Search Bar (Compact)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setHistorySearchQuery(it) },
                    placeholder = { Text("Cari siswa, diagnosa, obat...", fontSize = 12.sp) },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("input_history_search")
                )

                // Faskes Filter Chips (Dinamis mengikuti daftar Faskes sekolah)
                val faskesFilterOptions = remember(schoolProfile) { schoolProfile.getAllFaskes() }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Faskes:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilterChip(
                        selected = selectedFaskes == "Semua",
                        onClick = { viewModel.setFaskesFilter("Semua") },
                        label = { Text("Semua (${completedRecords.size})", fontSize = 11.sp) },
                        modifier = Modifier.height(30.dp)
                    )
                    faskesFilterOptions.forEachIndexed { idx, faskesName ->
                        FilterChip(
                            selected = selectedFaskes.equals(faskesName, ignoreCase = true),
                            onClick = { viewModel.setFaskesFilter(faskesName) },
                            label = { Text(faskesName, fontSize = 11.sp) },
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("filter_history_faskes_$idx")
                        )
                    }
                }

                if (completedRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) "Tidak ditemukan catatan yang cocok." else "Belum ada riwayat pengobatan.",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Gunakan tombol '+ Pengobatan Langsung' untuk mendata tindakan yang sudah dilakukan di puskesmas.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(completedRecords, key = { it.id }) { record ->
                            CompactCompletedRecordCard(
                                record = record,
                                onShare = { recordToShare = record },
                                onDelete = { viewModel.deleteRecord(record) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDirectTreatmentDialog) {
        DirectTreatmentDialog(
            students = students,
            primaryFaskesName = schoolProfile.primaryFaskesName,
            secondaryFaskesName = schoolProfile.secondaryFaskesName,
            allFaskesList = schoolProfile.getAllFaskes(),
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
            schoolProfile = schoolProfile,
            onDismiss = { recordToShare = null }
        )
    }
}

@Composable
fun CompactCompletedRecordCard(
    record: MedicalRecord,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = if (record.faskes.contains("Wates", ignoreCase = true)) Color(0xFF00695C) else Color(0xFF2E7D32)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_completed_${record.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Accent Stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 11.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Header: Nama Siswa & Tanggal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = record.studentName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = record.studentDormitory,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val dateLabel = record.treatmentDate?.let { DateUtils.formatShortDate(it) }
                        ?: DateUtils.formatShortDate(record.planDate)
                    Text(
                        text = dateLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FaskesBadge(faskes = record.faskes)

                    if (record.isDirectTreatment) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = "Langsung",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Keluhan & Diagnosa (Clean layout)
                Text(
                    text = "Keluhan: ${record.complaint}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                if (record.diagnosis.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Diagnosa: ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = record.diagnosis,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (record.prescription.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp).padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Obat: ${record.prescription}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Status Kontrol
                ControlBadge(
                    needsControl = record.needsControl,
                    controlDateFormatted = record.controlDate?.let { DateUtils.formatFullDate(it) },
                    isCompleted = record.isControlCompleted
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 1.dp))

                // Footer Row: Pendamping & WhatsApp Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (record.escortName.isNotBlank()) "👤 ${record.escortName}" else "",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Hapus Riwayat",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        OutlinedButton(
                            onClick = onShare,
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("btn_share_wa_${record.id}"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Laporan WA", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
