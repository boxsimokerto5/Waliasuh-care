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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
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
import com.example.ui.components.StudentAvatar
import com.example.ui.dialogs.DirectTreatmentDialog
import com.example.ui.dialogs.ShareWhatsAppDialog
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import java.util.Calendar

@Composable
fun MedicalRecordScreen(
    viewModel: HealthViewModel,
    waliName: String,
    completedRecords: List<MedicalRecord>,
    students: List<ChildStudent>,
    searchQuery: String,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDirectTreatmentDialog by remember { mutableStateOf(false) }
    var recordToShare by remember { mutableStateOf<MedicalRecord?>(null) }

    var expandedDateDropdown by remember { mutableStateOf(false) }
    var selectedDateFilter by remember { mutableStateOf("Semua Tanggal") }

    val todayStr = remember { DateUtils.formatShortDate(Calendar.getInstance().timeInMillis) }

    val filteredList = completedRecords.filter { record ->
        if (selectedDateFilter == "Hari Ini") {
            DateUtils.isToday(record.treatmentDate ?: record.planDate)
        } else {
            true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
                // Header Titles from Image 2: "Rekam Medis di Puskesmas", "Wali: Sari Devi"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rekam Medis di Puskesmas",
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

                // Filter Row from Image 2: Date Dropdown ("Kamis, 13 Nov ▼") + Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date Dropdown
                    Box {
                        OutlinedButton(
                            onClick = { expandedDateDropdown = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(
                                text = selectedDateFilter,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = expandedDateDropdown,
                            onDismissRequest = { expandedDateDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Semua Tanggal", fontSize = 12.sp) },
                                onClick = {
                                    selectedDateFilter = "Semua Tanggal"
                                    expandedDateDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hari Ini ($todayStr)", fontSize = 12.sp) },
                                onClick = {
                                    selectedDateFilter = "Hari Ini"
                                    expandedDateDropdown = false
                                }
                            )
                        }
                    }

                    // Search Field from Image 2
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setHistorySearchQuery(it) },
                        placeholder = { Text("Cari Rekam Medis Anak...", fontSize = 11.5.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Cari", modifier = Modifier.size(15.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setHistorySearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus", modifier = Modifier.size(13.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("input_medical_search")
                    )
                }

                // Subtitle from Image 2: "Daftar Diagnosa Siswa Pasca Puskesmas (Hari Ini)"
                Text(
                    text = "Daftar Diagnosa Siswa Pasca Puskesmas (Hari Ini)",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )

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
                        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        itemsIndexed(filteredList, key = { _, item -> item.id }) { index, record ->
                            val student = students.find { it.id == record.studentId }
                            MedicalRecordCardItem(
                                number = index + 1,
                                record = record,
                                student = student,
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

@Composable
fun MedicalRecordCardItem(
    number: Int,
    record: MedicalRecord,
    student: ChildStudent?,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = record.treatmentDate?.let { DateUtils.formatFullDate(it) }
        ?: DateUtils.formatFullDate(record.planDate)
    val timeStr = "${record.departureTime} WIB"
    val gradeText = student?.classGrade ?: "Kelas Siswa"
    val dormText = record.studentDormitory
    val gender = student?.gender ?: "L"

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_medical_record_${record.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Student Avatar from Image 2
                StudentAvatar(
                    name = record.studentName,
                    gender = gender,
                    size = 46.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$number. ${record.studentName} ($gradeText)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dormText,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Kunjungan: $dateStr, $timeStr",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (record.diagnosis.isNotBlank()) {
                        Text(
                            text = "Diagnosa: ${record.diagnosis}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }

                    if (record.actionsTaken.isNotBlank()) {
                        Text(
                            text = "Tindakan: ${record.actionsTaken}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (record.prescription.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(13.dp).padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Obat: ${record.prescription}",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Badges row: Faskes & Kontrol
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FaskesBadge(faskes = record.faskes)

                ControlBadge(
                    needsControl = record.needsControl,
                    controlDateFormatted = record.controlDate?.let { DateUtils.formatShortDate(it) },
                    isCompleted = record.isControlCompleted
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

            // Action row: WhatsApp Share & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (record.escortName.isNotBlank()) "👤 ${record.escortName}" else "",
                    fontSize = 11.sp,
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

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedButton(
                        onClick = onShare,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("btn_share_wa_${record.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Laporan WA", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
