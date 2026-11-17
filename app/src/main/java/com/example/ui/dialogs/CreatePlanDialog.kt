package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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

    // Tema warna senada ikon induk Rencana Berobat (Biru Langit Cerah + Mint Kesehatan + Pastel Lembut)
    val skyPrimary = Color(0xFF0284C7)
    val skyBright = Color(0xFF0EA5E9)
    val skySoftBg = Color(0xFFF0F9FF)
    val skyBorder = Color(0xFFBAE6FD)
    val skyTextDark = Color(0xFF0369A1)
    val mintSoftBg = Color(0xFFECFDF5)
    val mintBorder = Color(0xFFA7F3D0)
    val mintPrimary = Color(0xFF047857)
    val quickComplaints = if (isEn) {
        listOf("Fever & Headache", "Cough & Cold", "Stomachache", "Skin / Wound")
    } else {
        listOf("Demam & Pusing", "Batuk & Pilek", "Sakit Perut / Maag", "Luka / Gatal Kulit")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 580.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Gradient Senada Ikon Induk Rencana Berobat + Drag Pill
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(skyPrimary, skyBright)))
                        .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.45f))
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.5.dp, skyBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_menu_rencana),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEditMode) {
                                        if (isEn) "Edit Medical Plan" else "Edit Rencana Berobat"
                                    } else {
                                        if (isEn) "Create Medical Plan" else "Buat Rencana Berobat"
                                    },
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isEn) {
                                        "Schedule Faskes visit, symptoms & escort"
                                    } else {
                                        "Jadwalkan kunjungan Faskes, keluhan & pengantar"
                                    },
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFE0F2FE),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = s.nav.close,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // Isi Form Kompak, Lembut & Penuh Warna
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 13.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Riwayat Diinput / Diedit oleh
                    existingRecord?.let { rec ->
                        val inputBy = rec.createdBy.ifBlank { rec.escortName.ifBlank { "-" } }
                        Surface(
                            color = skySoftBg,
                            border = BorderStroke(1.dp, skyBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = if (isEn) "Entered by: $inputBy" else "Diinput oleh: $inputBy",
                                    fontSize = 11.sp,
                                    color = skyTextDark
                                )
                                if (rec.editedBy.isNotBlank()) {
                                    Text(
                                        text = buildString {
                                            append(if (isEn) "Edited by: ${rec.editedBy}" else "Diedit oleh: ${rec.editedBy}")
                                            rec.editedAt?.let { append(" (${DateUtils.formatShortDate(it)})") }
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = skyPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Header Anak Asuh + Tombol Cari Sejajar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Foster Student" else "Anak Asuh",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = skyTextDark
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = skySoftBg,
                            border = BorderStroke(1.dp, skyBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showStudentSearchDialog = true }
                                .testTag("btn_search_student_plan")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = skyPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isEn) "Search Name" else "Cari Nama",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = skyPrimary
                                )
                            }
                        }
                    }

                    // Kartu Siswa Pastel Lembut
                    Card(
                        onClick = { showStudentSearchDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = skySoftBg),
                        border = BorderStroke(1.dp, skyBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("select_student_search_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 11.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0F2FE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = skyPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = selectedStudent?.name
                                            ?: if (isEn) "Tap to select a foster student..." else "Ketuk untuk memilih anak asuh...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = if (selectedStudent != null) Color(0xFF0C4A6E) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = selectedStudent?.let { "${it.dormitory} • ${it.classGrade}" }
                                            ?: if (isEn) "Search name, dorm, or grade" else "Cari nama, asrama, atau kelas",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = skyTextDark
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, skyBorder)
                            ) {
                                Text(
                                    text = if (isEn) "Change" else "Ganti",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = skyPrimary,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Peringatan Alergi jika ada
                    selectedStudent?.let { student ->
                        if (student.allergies.isNotBlank() && !student.allergies.equals("Tidak ada", ignoreCase = true)) {
                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isEn) "Allergies: ${student.allergies}" else "Perhatian Alergi: ${student.allergies}",
                                        color = Color(0xFFB91C1C),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Pilih Faskes (Pastel Surface Pills Kompak)
                    Text(
                        text = if (isEn) "Destination Health Facility" else "Faskes Tujuan",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        faskesOptions.forEachIndexed { idx, faskesName ->
                            val isSel = selectedFaskes == faskesName
                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = if (isSel) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, if (isSel) skyPrimary else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .clickable { selectedFaskes = faskesName }
                                    .testTag("chip_puskesmas_${idx + 1}")
                            ) {
                                Text(
                                    text = faskesName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) skyTextDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                        val isOtherSel = selectedFaskes == "Lainnya"
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (isOtherSel) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, if (isOtherSel) Color(0xFFD97706) else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .clickable { selectedFaskes = "Lainnya" }
                        ) {
                            Text(
                                text = if (isEn) "Other" else "Lainnya",
                                fontSize = 11.sp,
                                fontWeight = if (isOtherSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isOtherSel) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
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

                    // Tanggal & Jam Rencana Sejajar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Planned Schedule & Time" else "Tanggal & Jam Berangkat",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_icon_open_date_picker")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = if (isEn) "Pick Planned Date" else "Pilih Tanggal Rencana",
                                tint = skyPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    val isCustomDate = !DateUtils.isToday(selectedDateMillis) &&
                        !DateUtils.isTomorrow(selectedDateMillis) &&
                        selectedDateMillis != calIn2Days

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Triple(s.today, calToday, !isCustomDate && DateUtils.isToday(selectedDateMillis)),
                            Triple(s.tomorrow, calTomorrow, !isCustomDate && DateUtils.isTomorrow(selectedDateMillis)),
                            Triple(s.dayAfterTomorrow, calIn2Days, !isCustomDate && selectedDateMillis == calIn2Days)
                        ).forEach { (label, targetMillis, isSel) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) skySoftBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, if (isSel) skyPrimary else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedDateMillis = targetMillis }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) skyTextDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCustomDate) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, if (isCustomDate) Color(0xFFD97706) else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showDatePicker = true }
                                .testTag("chip_pick_custom_date")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = if (isCustomDate) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isCustomDate) DateUtils.formatShortDate(selectedDateMillis) else if (isEn) "Pick Date" else "Pilih Tgl",
                                    fontSize = 11.sp,
                                    fontWeight = if (isCustomDate) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCustomDate) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Estimasi Jam Berangkat Pills (Mint Pastel)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf("08:30", "09:00", "09:30", "10:00").forEach { time ->
                            val isSel = selectedTime == time
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) mintSoftBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, if (isSel) mintPrimary else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedTime = time }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = time,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) mintPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Kartu Ringkasan Jadwal Berwarna Lembut
                    Card(
                        onClick = { showDatePicker = true },
                        colors = CardDefaults.cardColors(containerColor = skySoftBg),
                        border = BorderStroke(1.dp, skyBorder),
                        shape = RoundedCornerShape(10.dp),
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
                                tint = skyPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isEn) {
                                    "Schedule: ${DateUtils.formatFullDate(selectedDateMillis)} • $selectedTime WIB"
                                } else {
                                    "Jadwal: ${DateUtils.formatFullDate(selectedDateMillis)} • $selectedTime WIB"
                                },
                                fontSize = 11.5.sp,
                                color = skyTextDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Keluhan + Preset Keluhan Cepat
                    OutlinedTextField(
                        value = complaint,
                        onValueChange = { complaint = it },
                        label = { Text(if (isEn) "Complaint / Symptoms *" else "Keluhan / Gejala Sakit *", fontSize = 12.sp) },
                        placeholder = { Text(if (isEn) "e.g.: Fever 38°C since morning" else "misal: Demam 38°C sejak pagi", fontSize = 12.sp) },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_complaint")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        quickComplaints.forEach { preset ->
                            val isSel = complaint.equals(preset, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, if (isSel) skyPrimary else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { complaint = preset }
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) skyTextDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Petugas Pengantar dengan Dropdown Shift
                    ExposedDropdownMenuBox(
                        expanded = expandedEscortDropdown,
                        onExpandedChange = { expandedEscortDropdown = !expandedEscortDropdown }
                    ) {
                        OutlinedTextField(
                            value = escortName,
                            onValueChange = { escortName = it },
                            label = { Text(if (isEn) "Escort Officer / Shift" else "Petugas Pengantar / Shift", fontSize = 12.sp) },
                            placeholder = { Text(if (isEn) "Select shift or type name..." else "Pilih shift atau ketik nama...", fontSize = 12.sp) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEscortDropdown)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = skyPrimary,
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf(
                            Triple("Shift Pagi", Color(0xFFFEF3C7), Color(0xFFB45309)),
                            Triple("Shift Sore", Color(0xFFE0F2FE), Color(0xFF0369A1)),
                            Triple("Shift Malam", Color(0xFFEDE9FE), Color(0xFF5B21B6))
                        ).forEach { (shift, bgCol, fgCol) ->
                            val isSel = escortName == shift || escortName.startsWith(shift)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) bgCol else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, if (isSel) fgCol else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { escortName = shift }
                                    .testTag("chip_shift_${shift.lowercase().replace(" ", "_")}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = shift,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) fgCol else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Catatan
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (isEn) "Asrama Notes (Optional)" else "Catatan Asrama (Opsional)", fontSize = 12.sp) },
                        placeholder = { Text(if (isEn) "e.g.: Applied warm compress at UKS" else "misal: Sudah dikompres UKS", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = skyBorder.copy(alpha = 0.7f))

                    // Bottom Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(0.38f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text(s.cancel, fontSize = 12.5.sp)
                        }

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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = skyPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(0.62f)
                                .heightIn(min = 48.dp)
                                .testTag("confirm_create_plan_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isEditMode) {
                                    if (isEn) "Save Changes" else "Simpan Perubahan"
                                } else {
                                    if (isEn) "Save Plan" else "Simpan Rencana"
                                },
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
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
                        color = skyPrimary
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
