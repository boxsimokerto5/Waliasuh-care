package com.example.ui.dialogs

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
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
fun CreatePlanDialog(
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
        planDate: Long,
        departureTime: String,
        escort: String,
        notes: String
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

    var selectedTime by remember(existingRecord?.id) {
        mutableStateOf(existingRecord?.departureTime?.ifBlank { "08:30" } ?: "08:30")
    }

    var complaint by remember(existingRecord?.id) { mutableStateOf(existingRecord?.complaint ?: "") }
    var escortName by remember(existingRecord?.id) { mutableStateOf(existingRecord?.escortName ?: "") }
    var expandedEscortDropdown by remember { mutableStateOf(false) }
    var notes by remember(existingRecord?.id) { mutableStateOf(existingRecord?.notes ?: "") }

    val calToday = remember { Calendar.getInstance().timeInMillis }
    val calTomorrow = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
    }
    val calIn2Days = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }.timeInMillis
    }

    var selectedDateMillis by remember(existingRecord?.id) {
        mutableLongStateOf(existingRecord?.planDate ?: calToday)
    }
    var showDatePicker by remember { mutableStateOf(false) }
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
                        imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) {
                            if (isEn) "Edit Medical Plan" else "Edit Rencana Berobat"
                        } else {
                            if (isEn) "Create Medical Plan" else "Buat Rencana Berobat"
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
                            .testTag("btn_search_student_plan")
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
                        .testTag("select_student_search_card")
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
                                text = if (isEn) "Allergies: ${student.allergies}" else "Alergi: ${student.allergies}",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Pilih Faskes
                Text(
                    text = if (isEn) "Destination Faskes / Health Facility:" else "Faskes / Fasilitas Kesehatan Tujuan:",
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
                                .testTag("chip_puskesmas_${idx + 1}")
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

                // Estimasi Jam Berangkat
                Text(
                    text = if (isEn) "Estimated Departure Time:" else "Estimasi Jam Berangkat:",
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
                    label = { Text(if (isEn) "Complaint / Symptoms *" else "Keluhan / Gejala Sakit *", fontSize = 12.sp) },
                    placeholder = { Text(if (isEn) "e.g.: Fever 38°C since morning" else "misal: Demam 38°C sejak pagi", fontSize = 12.sp) },
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_complaint")
                )

                // Pilihan Tanggal Rencana
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isEn) "Planned Date:" else "Tanggal Rencana:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("btn_icon_open_date_picker")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = if (isEn) "Pick Planned Date" else "Pilih Tanggal Rencana",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                val isCustomDate = !DateUtils.isToday(selectedDateMillis) &&
                        !DateUtils.isTomorrow(selectedDateMillis) &&
                        selectedDateMillis != calIn2Days

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !isCustomDate && DateUtils.isToday(selectedDateMillis),
                        onClick = { selectedDateMillis = calToday },
                        label = { Text(s.today, fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = !isCustomDate && DateUtils.isTomorrow(selectedDateMillis),
                        onClick = { selectedDateMillis = calTomorrow },
                        label = { Text(s.tomorrow, fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = !isCustomDate && selectedDateMillis == calIn2Days,
                        onClick = { selectedDateMillis = calIn2Days },
                        label = { Text(s.dayAfterTomorrow, fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = isCustomDate,
                        onClick = { showDatePicker = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (isCustomDate) DateUtils.formatShortDate(selectedDateMillis) else if (isEn) "Pick Date" else "Pilih Tanggal",
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("chip_pick_custom_date")
                    )
                }

                Card(
                    onClick = { showDatePicker = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_jadwal_date_picker")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = if (isEn) "Select Day" else "Pilih Hari",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                        Column {
                            Text(
                                text = if (isEn) {
                                    "Schedule: ${DateUtils.formatFullDate(selectedDateMillis)} ($selectedTime WIB)"
                                } else {
                                    "Jadwal: ${DateUtils.formatFullDate(selectedDateMillis)} ($selectedTime WIB)"
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEn) {
                                    "Tap to pick a date from the pop-up calendar"
                                } else {
                                    "Klik untuk memilih hari melalui kalender pop-up"
                                },
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Petugas Pengantar dengan Dropdown Shift
                Text(
                    text = if (isEn) "Escort Officer:" else "Petugas Pengantar:",
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
                            .testTag("input_escort_shift_dropdown")
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
                                modifier = Modifier.testTag("menu_item_${shift.lowercase().replace(" ", "_")}")
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
                                .testTag("chip_shift_${shift.lowercase().replace(" ", "_")}")
                        )
                    }
                }

                // Catatan
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isEn) "Asrama Notes" else "Catatan Asrama", fontSize = 12.sp) },
                    placeholder = { Text(if (isEn) "e.g.: Applied warm compress at UKS" else "misal: Sudah dikompres UKS", fontSize = 12.sp) },
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
                        Text(s.cancel, fontSize = 12.sp)
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
                        Text(
                            if (isEditMode) {
                                if (isEn) "Save Changes" else "Simpan Perubahan"
                            } else {
                                if (isEn) "Save Plan" else "Simpan Rencana"
                            },
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val initialUtcMillis = remember(selectedDateMillis) {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
            val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            }
            utcCal.timeInMillis
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialUtcMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = utcMillis
                            }
                            val localCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                                set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                                set(Calendar.HOUR_OF_DAY, 8)
                                set(Calendar.MINUTE, 30)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            selectedDateMillis = localCal.timeInMillis
                        }
                        showDatePicker = false
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_confirm_date_picker")
                ) {
                    Text(if (isEn) "Select Day" else "Pilih Hari")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDatePicker = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(s.cancel)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = if (isEn) "Select Planned Treatment Date" else "Pilih Tanggal Rencana Berobat",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
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

