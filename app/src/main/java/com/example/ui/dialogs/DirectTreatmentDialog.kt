package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DirectTreatmentDialog(
    students: List<ChildStudent>,
    primaryFaskesName: String = "UKS Sekolah",
    secondaryFaskesName: String = "Puskesmas Wates",
    allFaskesList: List<String> = emptyList(),
    existingRecord: MedicalRecord? = null,
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
        notes: String,
        isInpatient: Boolean,
        inpatientRoom: String
    ) -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val isEditMode = existingRecord != null
    var selectedStudent by remember(existingRecord?.id) {
        mutableStateOf(
            if (existingRecord != null) {
                students.firstOrNull { it.id == existingRecord.studentId } ?: students.firstOrNull()
            } else {
                students.firstOrNull()
            }
        )
    }
    LaunchedEffect(students, existingRecord?.id) {
        if (existingRecord != null) {
            students.firstOrNull { it.id == existingRecord.studentId }?.let { selectedStudent = it }
        } else if (selectedStudent == null && students.isNotEmpty()) {
            selectedStudent = students.firstOrNull()
        }
    }
    var showStudentSearchDialog by remember { mutableStateOf(false) }

    val faskesOptions = remember(primaryFaskesName, secondaryFaskesName, allFaskesList) {
        if (allFaskesList.isNotEmpty()) {
            allFaskesList
        } else {
            listOf(primaryFaskesName, secondaryFaskesName).filter { it.isNotBlank() }
        }
    }
    val matchedExistingFaskes = remember(existingRecord?.id, faskesOptions) {
        val raw = existingRecord?.faskes?.trim().orEmpty()
        if (raw.isBlank()) null else faskesOptions.firstOrNull { it.equals(raw, ignoreCase = true) }
    }
    var selectedFaskes by remember(existingRecord?.id) {
        mutableStateOf(
            when {
                existingRecord == null -> faskesOptions.firstOrNull() ?: primaryFaskesName
                matchedExistingFaskes != null -> matchedExistingFaskes
                else -> "Lainnya"
            }
        )
    }
    var customFaskes by remember(existingRecord?.id) {
        mutableStateOf(
            if (existingRecord != null && matchedExistingFaskes == null) existingRecord.faskes else ""
        )
    }

    var complaint by remember(existingRecord?.id) { mutableStateOf(existingRecord?.complaint ?: "") }
    var diagnosis by remember(existingRecord?.id) { mutableStateOf(existingRecord?.diagnosis ?: "") }
    var actionsTaken by remember(existingRecord?.id) { mutableStateOf(existingRecord?.actionsTaken ?: "") }
    var prescription by remember(existingRecord?.id) { mutableStateOf(existingRecord?.prescription ?: "") }
    var escortName by remember(existingRecord?.id) { mutableStateOf(existingRecord?.escortName ?: "") }
    var expandedEscortDropdown by remember { mutableStateOf(false) }
    var notes by remember(existingRecord?.id) { mutableStateOf(existingRecord?.notes ?: "") }

    var isInpatient by remember(existingRecord?.id) { mutableStateOf(existingRecord?.isInpatient ?: false) }
    var inpatientRoom by remember(existingRecord?.id) { mutableStateOf(existingRecord?.inpatientRoom ?: "") }
    var needsControl by remember(existingRecord?.id) { mutableStateOf(existingRecord?.needsControl ?: false) }

    val calIn3Days = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }.timeInMillis
    }
    val calIn5Days = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 5) }.timeInMillis
    }
    val calIn7Days = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis
    }

    var selectedControlDateMillis by remember(existingRecord?.id) {
        mutableLongStateOf(existingRecord?.controlDate ?: calIn3Days)
    }
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
                        imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEditMode) {
                            if (isEn) "Edit Treatment Record" else "Edit Rekam Tindakan"
                        } else {
                            if (isEn) "Direct Treatment Entry" else "Input Tindakan Langsung"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Riwayat Diinput / Diedit oleh (tampil di dalam pop-up)
                existingRecord?.let { rec ->
                    val inputBy = rec.createdBy.ifBlank { rec.escortName.ifBlank { "-" } }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = if (isEn) "Entered by: $inputBy" else "Diinput oleh: $inputBy",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (rec.editedBy.isNotBlank()) {
                                Text(
                                    text = buildString {
                                        append(if (isEn) "Edited by: ${rec.editedBy}" else "Diedit oleh: ${rec.editedBy}")
                                        rec.editedAt?.let { append(" (${DateUtils.formatShortDate(it)})") }
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Text(
                    text = if (isEn) {
                        "Record Puskesmas actions directly without a prior scheduled plan."
                    } else {
                        "Catat tindakan puskesmas tanpa jadwal rencana sebelumnya."
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Pilih Siswa (dengan fitur Pencarian Cepat)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Select Foster Student:" else "Pilih Anak Asuh:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedButton(
                        onClick = { showStudentSearchDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("btn_search_student_direct")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isEn) "Search Name..." else "Cari Nama...",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    onClick = { showStudentSearchDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("select_student_search_direct_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = selectedStudent?.name
                                        ?: if (isEn) "Tap to select a foster student..." else "Ketuk untuk memilih anak asuh...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = if (selectedStudent != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedStudent?.let { "${it.dormitory} • ${it.classGrade}" }
                                        ?: if (isEn) "Search name, Asrama, or grade" else "Cari nama, asrama, atau kelas",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = s.search,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
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
                                text = if (isEn) "Allergies: ${student.allergies}" else "Alergi: ${student.allergies}",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Puskesmas Tujuan
                Text(
                    text = if (isEn) "Treatment Faskes / Health Facility:" else "Faskes / Fasilitas Kesehatan Pengobatan:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    faskesOptions.forEachIndexed { idx, faskesName ->
                        FilterChip(
                            selected = selectedFaskes == faskesName,
                            onClick = { selectedFaskes = faskesName },
                            label = { Text(faskesName, fontSize = 11.sp, maxLines = 1) },
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("chip_direct_faskes_${idx + 1}")
                        )
                    }
                    FilterChip(
                        selected = selectedFaskes == "Lainnya",
                        onClick = { selectedFaskes = "Lainnya" },
                        label = { Text(if (isEn) "Other" else "Lainnya", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }

                if (selectedFaskes == "Lainnya") {
                    OutlinedTextField(
                        value = customFaskes,
                        onValueChange = { customFaskes = it },
                        label = { Text(if (isEn) "Faskes / Hospital Name" else "Nama Faskes / RS", fontSize = 12.sp) },
                        placeholder = { Text(if (isEn) "e.g.: RSUD Gambiran" else "misal: RSUD Gambiran", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Keluhan
                OutlinedTextField(
                    value = complaint,
                    onValueChange = { complaint = it },
                    label = { Text(if (isEn) "Initial Complaint *" else "Keluhan Awal *", fontSize = 12.sp) },
                    placeholder = { Text(if (isEn) "e.g.: Vomiting, diarrhea, wound" else "misal: Muntah-muntah, diare, luka", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Diagnosa
                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text(if (isEn) "Doctor / Clinician Diagnosis *" else "Diagnosa Dokter / Nakes *", fontSize = 12.sp) },
                    placeholder = { Text(if (isEn) "e.g.: Acute Gastroenteritis" else "misal: Gastroenteritis Akut", fontSize = 12.sp) },
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
                    label = { Text(if (isEn) "Medical Action Taken" else "Tindakan yang Dilakukan", fontSize = 12.sp) },
                    placeholder = { Text(if (isEn) "e.g.: Nebulization, wound cleaning" else "misal: Nebulisasi, pembersihan luka", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Obat (Opsional saat Rawat Inap karena obat pulang biasanya menyusul)
                OutlinedTextField(
                    value = prescription,
                    onValueChange = { prescription = it },
                    label = {
                        Text(
                            text = if (isInpatient) {
                                if (isEn) "Prescription / Temporary Therapy (Optional)" else "Resep Obat / Terapi Sementara (Opsional)"
                            } else {
                                if (isEn) "Prescription & Dosage *" else "Resep Obat & Dosis *"
                            },
                            fontSize = 12.sp
                        )
                    },
                    placeholder = {
                        Text(
                            text = if (isInpatient) {
                                if (isEn) "e.g.: RL IV drip / leave blank if admitted" else "misal: Infus RL / kosongkan dulu jika opname"
                            } else {
                                if (isEn) "e.g.: Oralit 3 sachets, Zinc 20mg 1x1" else "misal: Oralit 3 sachet, Zinc 20mg 1x1"
                            },
                            fontSize = 12.sp
                        )
                    },
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("direct_prescription_input")
                )

                // Toggle Rawat Inap (Opname / IGD)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isInpatient) {
                            Color(0xFFDC2626).copy(alpha = 0.08f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        }
                    ),
                    border = if (isInpatient) BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.35f)) else null,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalHospital,
                                    contentDescription = null,
                                    tint = if (isInpatient) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isEn) "Currently Inpatient (Admitted)?" else "Sedang Rawat Inap (Opname)?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isInpatient) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isInpatient) {
                                            if (isEn) {
                                                "Inpatient notice appears on Dashboard & prescription can follow later"
                                            } else {
                                                "Notif rawat inap tampil di Dashboard & resep boleh menyusul"
                                            }
                                        } else {
                                            if (isEn) {
                                                "Enable if student is admitted or treated in ER"
                                            } else {
                                                "Aktifkan jika anak opname / dirawat di IGD"
                                            }
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isInpatient,
                                onCheckedChange = { isInpatient = it },
                                modifier = Modifier.testTag("direct_switch_inpatient")
                            )
                        }

                        if (isInpatient) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = inpatientRoom,
                                onValueChange = { inpatientRoom = it },
                                label = { Text(if (isEn) "Room / Ward Name (Optional)" else "Nama Ruang / Kamar (Opsional)", fontSize = 12.sp) },
                                placeholder = { Text(if (isEn) "e.g.: Melati Ward Fl. 2 / ER" else "misal: Ruang Melati Lt. 2 / IGD", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("direct_input_inpatient_room")
                            )
                        }
                    }
                }

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
                                    text = if (isEn) "Follow-up Control Plan?" else "Rencana Kontrol Kembali?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (needsControl) {
                                        if (isEn) "Student must attend follow-up control" else "Anak wajib kontrol ulang"
                                    } else {
                                        if (isEn) "No follow-up needed" else "Tidak perlu kontrol"
                                    },
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
                                    label = { Text(if (isEn) "+3 Days" else "+3 Hari", fontSize = 11.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                                FilterChip(
                                    selected = selectedControlDateMillis == calIn5Days,
                                    onClick = { selectedControlDateMillis = calIn5Days },
                                    label = { Text(if (isEn) "+5 Days" else "+5 Hari", fontSize = 11.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                                FilterChip(
                                    selected = selectedControlDateMillis == calIn7Days,
                                    onClick = { selectedControlDateMillis = calIn7Days },
                                    label = { Text(if (isEn) "+1 Week" else "+1 Minggu", fontSize = 11.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (isEn) {
                                    "Date: ${DateUtils.formatFullDate(selectedControlDateMillis)}"
                                } else {
                                    "Tanggal: ${DateUtils.formatFullDate(selectedControlDateMillis)}"
                                },
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
                                    text = if (isEn) "No follow-up control needed (Cleared)" else "Tidak perlu kontrol kembali (Tuntas)",
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
                    text = if (isEn) "Escort Officer:" else "Petugas / Pengasuh Pengantar:",
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
                        label = { Text(if (isEn) "Select Shift / Escort Officer" else "Pilih Shift / Petugas Pengantar", fontSize = 12.sp) },
                        placeholder = { Text(if (isEn) "Select shift or type name..." else "Pilih shift atau ketik nama...", fontSize = 12.sp) },
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
                    label = { Text(if (isEn) "Additional Notes" else "Catatan Tambahan", fontSize = 12.sp) },
                    placeholder = { Text(if (isEn) "e.g.: Outpatient rest in Asrama room" else "misal: Rawat jalan di kamar asrama", fontSize = 12.sp) },
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
                        Text(s.cancel, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val isFormValid = selectedStudent != null &&
                        complaint.isNotBlank() &&
                        diagnosis.isNotBlank() &&
                        (isInpatient || prescription.isNotBlank())

                    Button(
                        onClick = {
                            val targetFaskes = if (selectedFaskes == "Lainnya") {
                                if (customFaskes.isNotBlank()) customFaskes.trim() else "Faskes Rujukan"
                            } else {
                                selectedFaskes
                            }
                            selectedStudent?.let { student ->
                                if (isFormValid) {
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
                                        notes.trim(),
                                        isInpatient,
                                        inpatientRoom.trim()
                                    )
                                }
                            }
                        },
                        enabled = isFormValid,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_direct_treatment_button")
                    ) {
                        Text(
                            text = when {
                                isEditMode -> if (isEn) "Save Changes" else "Simpan Perubahan"
                                isInpatient -> if (isEn) "Save Inpatient" else "Simpan Rawat Inap"
                                else -> if (isEn) "Save Treatment" else "Simpan Pengobatan"
                            },
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    if (showStudentSearchDialog) {
        SearchStudentDialog(
            students = students,
            selectedStudent = selectedStudent,
            onSelect = { selectedStudent = it },
            onDismiss = { showStudentSearchDialog = false }
        )
    }
}
