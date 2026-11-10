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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
fun DirectTreatmentDialog(
    students: List<ChildStudent>,
    onDismiss: () -> Unit,
    onConfirm: (
        student: ChildStudent,
        faskes: String,
        complaint: String,
        diagnosis: String,
        actionsTaken: String,
        prescription: String,
        needsControl: Boolean,
        controlDate: Long?,
        escortName: String,
        notes: String
    ) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var expandedDropdown by remember { mutableStateOf(false) }

    var selectedFaskes by remember { mutableStateOf("Puskesmas Wates") }
    var customFaskes by remember { mutableStateOf("") }

    var complaint by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var actionsTaken by remember { mutableStateOf("") }
    var prescription by remember { mutableStateOf("") }
    var escortName by remember { mutableStateOf("") }
    var expandedEscortDropdown by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    var needsControl by remember { mutableStateOf(false) }

    val calIn3Days = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }.timeInMillis
    }
    val calIn5Days = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 5) }.timeInMillis
    }
    val calIn7Days = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis
    }

    var selectedControlDateMillis by remember { mutableLongStateOf(calIn3Days) }
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Input Tindakan Langsung",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Catat tindakan puskesmas tanpa jadwal rencana sebelumnya.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Pilih Siswa
                Text(
                    text = "Pilih Anak Asuh:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedStudent?.let { "${it.name} (${it.dormitory})" } ?: "Pilih Siswa...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                            .testTag("select_student_direct_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
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
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                // Info Alergi
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

                // Puskesmas Tujuan
                Text(
                    text = "Puskesmas / Faskes Pengobatan:",
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
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = selectedFaskes == "Puskesmas Plosoklaten",
                        onClick = { selectedFaskes = "Puskesmas Plosoklaten" },
                        label = { Text("Pusk. Plosoklaten", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
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

                // Keluhan
                OutlinedTextField(
                    value = complaint,
                    onValueChange = { complaint = it },
                    label = { Text("Keluhan Awal *", fontSize = 12.sp) },
                    placeholder = { Text("misal: Muntah-muntah, diare, luka", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Diagnosa
                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text("Diagnosa Dokter / Nakes *", fontSize = 12.sp) },
                    placeholder = { Text("misal: Gastroenteritis Akut", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("direct_diagnosis_input")
                )

                // Tindakan
                OutlinedTextField(
                    value = actionsTaken,
                    onValueChange = { actionsTaken = it },
                    label = { Text("Tindakan yang Dilakukan", fontSize = 12.sp) },
                    placeholder = { Text("misal: Nebulisasi, pembersihan luka", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Obat
                OutlinedTextField(
                    value = prescription,
                    onValueChange = { prescription = it },
                    label = { Text("Resep Obat & Dosis *", fontSize = 12.sp) },
                    placeholder = { Text("misal: Oralit 3 sachet, Zinc 20mg 1x1", fontSize = 12.sp) },
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("direct_prescription_input")
                )

                // Kontrol Section (Compact)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (needsControl) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.22f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        }
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Rencana Kontrol Kembali?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (needsControl) "Anak wajib kontrol ulang" else "Tidak perlu kontrol",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = needsControl,
                                onCheckedChange = { needsControl = it },
                                modifier = Modifier.testTag("direct_switch_needs_control")
                            )
                        }

                        if (needsControl) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                FilterChip(
                                    selected = selectedControlDateMillis == calIn3Days,
                                    onClick = { selectedControlDateMillis = calIn3Days },
                                    label = { Text("+3 Hari", fontSize = 11.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                                FilterChip(
                                    selected = selectedControlDateMillis == calIn5Days,
                                    onClick = { selectedControlDateMillis = calIn5Days },
                                    label = { Text("+5 Hari", fontSize = 11.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                                FilterChip(
                                    selected = selectedControlDateMillis == calIn7Days,
                                    onClick = { selectedControlDateMillis = calIn7Days },
                                    label = { Text("+1 Minggu", fontSize = 11.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Tanggal: ${DateUtils.formatFullDate(selectedControlDateMillis)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tidak perlu kontrol kembali (Tuntas)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Petugas / Pengasuh Pengantar with Shift Dropdown
                Text(
                    text = "Petugas / Pengasuh Pengantar:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                ExposedDropdownMenuBox(
                    expanded = expandedEscortDropdown,
                    onExpandedChange = { expandedEscortDropdown = !expandedEscortDropdown }
                ) {
                    OutlinedTextField(
                        value = escortName,
                        onValueChange = { escortName = it },
                        label = { Text("Pilih Shift / Petugas Pengantar", fontSize = 12.sp) },
                        placeholder = { Text("Pilih shift atau ketik nama...", fontSize = 12.sp) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEscortDropdown)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                            .fillMaxWidth()
                            .testTag("input_direct_escort_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = expandedEscortDropdown,
                        onDismissRequest = { expandedEscortDropdown = false }
                    ) {
                        val shifts = listOf(
                            Triple("Shift Pagi", "07:00 - 14:00 WIB", "🌅"),
                            Triple("Shift Sore", "14:00 - 21:00 WIB", "🌇"),
                            Triple("Shift Malam", "21:00 - 07:00 WIB", "🌙")
                        )
                        shifts.forEach { (shift, hours, icon) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(text = icon, fontSize = 14.sp)
                                            Text(
                                                text = shift,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Text(
                                            text = hours,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    escortName = shift
                                    expandedEscortDropdown = false
                                },
                                modifier = Modifier.testTag("direct_menu_item_${shift.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }

                // Quick selection chips for Shift Pagi, Shift Sore, Shift Malam
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Shift Pagi", "Shift Sore", "Shift Malam").forEach { shift ->
                        FilterChip(
                            selected = escortName == shift || escortName.startsWith(shift),
                            onClick = { escortName = shift },
                            label = { Text(shift, fontSize = 11.sp) },
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("direct_chip_shift_${shift.lowercase().replace(" ", "_")}")
                        )
                    }
                }

                // Catatan
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Tambahan", fontSize = 12.sp) },
                    placeholder = { Text("misal: Rawat jalan di kamar asrama", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Action Buttons
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
                                if (complaint.isNotBlank() && diagnosis.isNotBlank() && prescription.isNotBlank()) {
                                    onConfirm(
                                        student,
                                        targetFaskes,
                                        complaint.trim(),
                                        diagnosis.trim(),
                                        actionsTaken.trim(),
                                        prescription.trim(),
                                        needsControl,
                                        if (needsControl) selectedControlDateMillis else null,
                                        escortName.trim(),
                                        notes.trim()
                                    )
                                }
                            }
                        },
                        enabled = selectedStudent != null && complaint.isNotBlank() && diagnosis.isNotBlank() && prescription.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_direct_treatment_button")
                    ) {
                        Text("Simpan Pengobatan", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
