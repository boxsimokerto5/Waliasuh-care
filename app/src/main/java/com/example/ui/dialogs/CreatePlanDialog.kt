package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanDialog(
    students: List<ChildStudent>,
    onDismiss: () -> Unit,
    onConfirm: (
        student: ChildStudent,
        faskes: String,
        complaint: String,
        planDate: Long,
        departureTime: String,
        escort: String,
        notes: String
    ) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var expandedStudentDropdown by remember { mutableStateOf(false) }

    var selectedFaskes by remember { mutableStateOf("Puskesmas Wates") }
    var customFaskes by remember { mutableStateOf("") }

    var selectedTime by remember { mutableStateOf("08:30") }

    var complaint by remember { mutableStateOf("") }
    var escortName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val calToday = remember { Calendar.getInstance().timeInMillis }
    val calTomorrow = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
    }
    val calIn2Days = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }.timeInMillis
    }

    var selectedDateMillis by remember { mutableLongStateOf(calToday) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), // Google Maps rounded top style
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buat Rencana Berobat",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Pilih Siswa Dropdown
                Text(
                    text = "Pilih Anak Asuh:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                ExposedDropdownMenuBox(
                    expanded = expandedStudentDropdown,
                    onExpandedChange = { expandedStudentDropdown = !expandedStudentDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedStudent?.let { "${it.name} (${it.dormitory})" } ?: "Pilih Siswa...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStudentDropdown) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("select_student_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = expandedStudentDropdown,
                        onDismissRequest = { expandedStudentDropdown = false }
                    ) {
                        students.forEach { student ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = "${student.dormitory} • ${student.classGrade}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedStudent = student
                                    expandedStudentDropdown = false
                                }
                            )
                        }
                    }
                }

                // Info Alergi jika ada
                selectedStudent?.let { student ->
                    if (student.allergies.isNotBlank() && !student.allergies.equals("Tidak ada", ignoreCase = true)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Alergi: ${student.allergies}",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Pilih Faskes
                Text(
                    text = "Puskesmas Tujuan:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFaskes == "Puskesmas Wates",
                        onClick = { selectedFaskes = "Puskesmas Wates" },
                        label = { Text("Pusk. Wates", fontSize = 11.sp) },
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("chip_puskesmas_wates")
                    )
                    FilterChip(
                        selected = selectedFaskes == "Puskesmas Plosoklaten",
                        onClick = { selectedFaskes = "Puskesmas Plosoklaten" },
                        label = { Text("Pusk. Plosoklaten", fontSize = 11.sp) },
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("chip_puskesmas_plosoklaten")
                    )
                    FilterChip(
                        selected = selectedFaskes == "Lainnya",
                        onClick = { selectedFaskes = "Lainnya" },
                        label = { Text("Lainnya", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }

                if (selectedFaskes == "Lainnya") {
                    OutlinedTextField(
                        value = customFaskes,
                        onValueChange = { customFaskes = it },
                        label = { Text("Nama Faskes / RS", fontSize = 12.sp) },
                        placeholder = { Text("misal: RSUD Gambiran", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Estimasi Jam Berangkat
                Text(
                    text = "Estimasi Jam Berangkat:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("08:30", "09:00", "09:30", "10:00").forEach { time ->
                        FilterChip(
                            selected = selectedTime == time,
                            onClick = { selectedTime = time },
                            label = { Text(time, fontSize = 11.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Keluhan
                OutlinedTextField(
                    value = complaint,
                    onValueChange = { complaint = it },
                    label = { Text("Keluhan / Gejala Sakit *", fontSize = 12.sp) },
                    placeholder = { Text("misal: Demam 38°C sejak pagi", fontSize = 12.sp) },
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_complaint")
                )

                // Pilihan Tanggal Rencana
                Text(
                    text = "Tanggal Rencana:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedDateMillis == calToday,
                        onClick = { selectedDateMillis = calToday },
                        label = { Text("Hari Ini", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = selectedDateMillis == calTomorrow,
                        onClick = { selectedDateMillis = calTomorrow },
                        label = { Text("Besok", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = selectedDateMillis == calIn2Days,
                        onClick = { selectedDateMillis = calIn2Days },
                        label = { Text("Lusa", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }
                Text(
                    text = "Jadwal: ${DateUtils.formatFullDate(selectedDateMillis)} ($selectedTime WIB)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                // Pendamping
                OutlinedTextField(
                    value = escortName,
                    onValueChange = { escortName = it },
                    label = { Text("Petugas Pengantar", fontSize = 12.sp) },
                    placeholder = { Text("misal: Sari Devi", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Catatan
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Asrama", fontSize = 12.sp) },
                    placeholder = { Text("misal: Sudah dikompres UKS", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val targetFaskes = if (selectedFaskes == "Lainnya") {
                                if (customFaskes.isNotBlank()) customFaskes.trim() else "Faskes Rujukan"
                            } else {
                                selectedFaskes
                            }
                            selectedStudent?.let { student ->
                                if (complaint.isNotBlank()) {
                                    onConfirm(student, targetFaskes, complaint.trim(), selectedDateMillis, selectedTime, escortName.trim(), notes.trim())
                                }
                            }
                        },
                        enabled = selectedStudent != null && complaint.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_create_plan_button")
                    ) {
                        Text("Simpan Rencana", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
