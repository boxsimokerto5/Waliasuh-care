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
import com.example.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePlanDialog(
    students: List<ChildStudent>,
    primaryFaskesName: String = "UKS Sekolah",
    secondaryFaskesName: String = "Puskesmas Wates",
    allFaskesList: List<String> = emptyList(),
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
    LaunchedEffect(students) {
        if (selectedStudent == null && students.isNotEmpty()) {
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
    var selectedFaskes by remember { mutableStateOf(faskesOptions.firstOrNull() ?: primaryFaskesName) }
    var customFaskes by remember { mutableStateOf("") }

    var selectedTime by remember { mutableStateOf("08:30") }

    var complaint by remember { mutableStateOf("") }
    var escortName by remember { mutableStateOf("") }
    var expandedEscortDropdown by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    val calToday = remember { Calendar.getInstance().timeInMillis }
    val calTomorrow = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
    }
    val calIn2Days = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }.timeInMillis
    }

    var selectedDateMillis by remember { mutableLongStateOf(calToday) }
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

                // Pilih Siswa (dengan fitur Pencarian Cepat)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih Anak Asuh:",
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
                        Text("Cari Nama...", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
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
                                    text = selectedStudent?.name ?: "Ketuk untuk memilih anak asuh...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = if (selectedStudent != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedStudent?.let { "${it.dormitory} • ${it.classGrade}" } ?: "Cari nama, asrama, atau kelas",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
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
                    text = "Faskes / Fasilitas Kesehatan Tujuan:",
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
                            text = "Tanggal Rencana:",
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
                                contentDescription = "Pilih Tanggal Rencana",
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
                        label = { Text("Hari Ini", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = !isCustomDate && DateUtils.isTomorrow(selectedDateMillis),
                        onClick = { selectedDateMillis = calTomorrow },
                        label = { Text("Besok", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = !isCustomDate && selectedDateMillis == calIn2Days,
                        onClick = { selectedDateMillis = calIn2Days },
                        label = { Text("Lusa", fontSize = 11.sp) },
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
                                text = if (isCustomDate) DateUtils.formatShortDate(selectedDateMillis) else "Pilih Tanggal",
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
                            contentDescription = "Pilih Hari",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                        Column {
                            Text(
                                text = "Jadwal: ${DateUtils.formatFullDate(selectedDateMillis)} ($selectedTime WIB)",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Klik untuk memilih hari melalui kalender pop-up",
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Petugas Pengantar dengan Dropdown Shift
                Text(
                    text = "Petugas Pengantar:",
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
                    Text("Pilih Hari")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDatePicker = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Pilih Tanggal Rencana Berobat",
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

