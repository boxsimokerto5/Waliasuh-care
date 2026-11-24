package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.LocalHospital
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import java.util.TimeZone

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
    var showControlDatePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun isSameDay(a: Long, b: Long): Boolean {
        val ca = Calendar.getInstance().apply { timeInMillis = a }
        val cb = Calendar.getInstance().apply { timeInMillis = b }
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
            ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }

    // Tema warna senada ikon induk Tindakan Berobat (Oranye Hangat ke Biru Medis + Aksen Palang Merah)
    val warmOrange = Color(0xFFEA580C)
    val medBlue = Color(0xFF0284C7)
    val warmSoftBg = Color(0xFFFFF7ED)
    val warmBorder = Color(0xFFFED7AA)
    val warmTextDark = Color(0xFF9A3412)
    val inpatientRed = Color(0xFFDC2626)

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
                // Header Gradient Senada Ikon Induk Tindakan Berobat + Drag Pill
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(warmOrange, medBlue)))
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
                                    .border(1.5.dp, warmBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_menu_tindakan),
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
                                        if (isEn) "Edit Treatment Record" else "Edit Rekam Tindakan"
                                    } else {
                                        if (isEn) "Direct Treatment Entry" else "Input Tindakan Berobat"
                                    },
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isEn) {
                                        "Log diagnosis, clinical action & prescription directly"
                                    } else {
                                        "Catat diagnosa, tindakan medis & resep obat langsung"
                                    },
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFFFEDD5),
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
                            color = warmSoftBg,
                            border = BorderStroke(1.dp, warmBorder),
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
                                    color = warmTextDark
                                )
                                if (rec.editedBy.isNotBlank()) {
                                    Text(
                                        text = buildString {
                                            append(if (isEn) "Edited by: ${rec.editedBy}" else "Diedit oleh: ${rec.editedBy}")
                                            rec.editedAt?.let { append(" (${DateUtils.formatShortDate(it)})") }
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = warmOrange
                                    )
                                }
                            }
                        }
                    }

                    // Header Anak Asuh + Cari Nama Sejajar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Foster Student" else "Anak Asuh",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = warmTextDark
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = warmSoftBg,
                            border = BorderStroke(1.dp, warmBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showStudentSearchDialog = true }
                                .testTag("btn_search_student_direct")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = warmOrange,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isEn) "Search Name" else "Cari Nama",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = warmOrange
                                )
                            }
                        }
                    }

                    // Kartu Siswa Pastel Hangat
                    Card(
                        onClick = { showStudentSearchDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = warmSoftBg),
                        border = BorderStroke(1.dp, warmBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("select_student_search_direct_card")
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
                                        .background(Color(0xFFFFEDD5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = warmOrange,
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
                                        color = if (selectedStudent != null) Color(0xFF7C2D12) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = selectedStudent?.let { "${it.dormitory} • ${it.classGrade}" }
                                            ?: if (isEn) "Search name, dorm, or grade" else "Cari nama, asrama, atau kelas",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = warmTextDark
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, warmBorder)
                            ) {
                                Text(
                                    text = if (isEn) "Change" else "Ganti",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = warmOrange,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Info Alergi
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
                                        tint = inpatientRed,
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

                    // Faskes Pengobatan (Surface Pills Kompak)
                    Text(
                        text = if (isEn) "Treatment Health Facility" else "Faskes Pengobatan",
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
                                color = if (isSel) Color(0xFFFFEDD5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, if (isSel) warmOrange else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .clickable { selectedFaskes = faskesName }
                                    .testTag("chip_direct_faskes_${idx + 1}")
                            ) {
                                Text(
                                    text = faskesName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) warmTextDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                        val isOtherSel = selectedFaskes == "Lainnya"
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (isOtherSel) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, if (isOtherSel) medBlue else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .clickable { selectedFaskes = "Lainnya" }
                        ) {
                            Text(
                                text = if (isEn) "Other" else "Lainnya",
                                fontSize = 11.sp,
                                fontWeight = if (isOtherSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isOtherSel) Color(0xFF0369A1) else MaterialTheme.colorScheme.onSurfaceVariant,
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

                    // Keluhan Awal
                    OutlinedTextField(
                        value = complaint,
                        onValueChange = { complaint = it },
                        label = { Text(if (isEn) "Initial Complaint *" else "Keluhan Awal *", fontSize = 12.sp) },
                        placeholder = { Text(if (isEn) "e.g.: Vomiting, diarrhea, wound" else "misal: Muntah-muntah, diare, luka", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Diagnosa Dokter / Nakes
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

                    // Tindakan Medis
                    OutlinedTextField(
                        value = actionsTaken,
                        onValueChange = { actionsTaken = it },
                        label = { Text(if (isEn) "Medical Action Taken" else "Tindakan yang Dilakukan", fontSize = 12.sp) },
                        placeholder = { Text(if (isEn) "e.g.: Nebulization, wound cleaning" else "misal: Nebulisasi, pembersihan luka", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Resep Obat & Dosis
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
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_prescription_input")
                    )

                    // Toggle Rawat Inap (Opname / IGD) Soft Coral Pastel Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isInpatient) Color(0xFFFEF2F2) else Color(0xFFFFFBEB)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isInpatient) Color(0xFFFCA5A5) else Color(0xFFFDE68A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (isInpatient) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalHospital,
                                            contentDescription = null,
                                            tint = if (isInpatient) inpatientRed else Color(0xFFB45309),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isEn) "Currently Inpatient (Admitted)?" else "Sedang Rawat Inap (Opname)?",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = if (isInpatient) Color(0xFFB91C1C) else Color(0xFF78350F)
                                        )
                                        Text(
                                            text = if (isInpatient) {
                                                if (isEn) {
                                                    "Inpatient badge on Dashboard • Rx optional"
                                                } else {
                                                    "Notif opname tampil di Dashboard • Resep menyusul"
                                                }
                                            } else {
                                                if (isEn) {
                                                    "Enable if student is admitted or treated in ER"
                                                } else {
                                                    "Aktifkan jika anak opname / dirawat di IGD"
                                                }
                                            },
                                            fontSize = 10.5.sp,
                                            color = if (isInpatient) Color(0xFF991B1B) else Color(0xFF92400E)
                                        )
                                    }
                                }
                                Switch(
                                    checked = isInpatient,
                                    onCheckedChange = { isInpatient = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = inpatientRed
                                    ),
                                    modifier = Modifier.testTag("direct_switch_inpatient")
                                )
                            }

                            AnimatedVisibility(visible = isInpatient) {
                                Column {
                                    Spacer(modifier = Modifier.height(7.dp))
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
                    }

                    // Rencana Kontrol Kembali Soft Sky/Mint Pastel Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (needsControl) Color(0xFFEFF6FF) else Color(0xFFF0FDF4)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (needsControl) Color(0xFF93C5FD) else Color(0xFFA7F3D0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isEn) "Follow-up Control Plan?" else "Rencana Kontrol Kembali?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = if (needsControl) Color(0xFF1E40AF) else Color(0xFF065F46)
                                    )
                                    Text(
                                        text = if (needsControl) {
                                            if (isEn) "Student must attend follow-up control" else "Anak dijadwalkan kontrol ulang"
                                        } else {
                                            if (isEn) "No follow-up needed (Cleared)" else "Tidak perlu kontrol kembali (Tuntas)"
                                        },
                                        fontSize = 10.5.sp,
                                        color = if (needsControl) Color(0xFF1D4ED8) else Color(0xFF047857)
                                    )
                                }
                                Switch(
                                    checked = needsControl,
                                    onCheckedChange = { needsControl = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = medBlue
                                    ),
                                    modifier = Modifier.testTag("direct_switch_needs_control")
                                )
                            }

                            if (needsControl) {
                                Spacer(modifier = Modifier.height(6.dp))
                                val isPreset3 = isSameDay(selectedControlDateMillis, calIn3Days)
                                val isPreset5 = isSameDay(selectedControlDateMillis, calIn5Days)
                                val isPreset7 = isSameDay(selectedControlDateMillis, calIn7Days)
                                val isCustomDate = !isPreset3 && !isPreset5 && !isPreset7

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    listOf(
                                        Triple(if (isEn) "+3 Days" else "+3 Hari", calIn3Days, isPreset3),
                                        Triple(if (isEn) "+5 Days" else "+5 Hari", calIn5Days, isPreset5),
                                        Triple(if (isEn) "+1 Week" else "+1 Minggu", calIn7Days, isPreset7)
                                    ).forEach { (label, millis, isSel) ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSel) Color(0xFFDBEAFE) else Color.White,
                                            border = BorderStroke(1.dp, if (isSel) medBlue else Color(0xFFBFDBFE)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedControlDateMillis = millis }
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSel) Color(0xFF1E40AF) else Color(0xFF3B82F6)
                                                )
                                            }
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isCustomDate) Color(0xFFDBEAFE) else Color.White,
                                        border = BorderStroke(1.dp, if (isCustomDate) medBlue else Color(0xFFBFDBFE)),
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showControlDatePicker = true }
                                            .testTag("direct_btn_pick_control_date")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = if (isEn) "Pick control date" else "Pilih tanggal kontrol",
                                                tint = if (isCustomDate) Color(0xFF1E40AF) else Color(0xFF3B82F6),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = if (isEn) "Date" else "Tanggal",
                                                fontSize = 11.sp,
                                                fontWeight = if (isCustomDate) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isCustomDate) Color(0xFF1E40AF) else Color(0xFF3B82F6),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showControlDatePicker = true }
                                        .testTag("direct_card_control_date_picker")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = medBlue, modifier = Modifier.size(15.dp))
                                            Text(
                                                text = if (isEn) {
                                                    "Control Date: ${DateUtils.formatFullDate(selectedControlDateMillis)}"
                                                } else {
                                                    "Tanggal Kontrol: ${DateUtils.formatFullDate(selectedControlDateMillis)}"
                                                },
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF1E40AF),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = if (isEn) "Change" else "Ubah",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = medBlue
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEn) "Status set to Cleared" else "Status dicatat Tuntas",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF047857),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Petugas / Pengasuh Pengantar with Shift Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedEscortDropdown,
                        onExpandedChange = { expandedEscortDropdown = !expandedEscortDropdown }
                    ) {
                        OutlinedTextField(
                            value = escortName,
                            onValueChange = { escortName = it },
                            label = { Text(if (isEn) "Escort Officer / Shift" else "Petugas / Pengasuh Pengantar", fontSize = 12.sp) },
                            placeholder = { Text(if (isEn) "Select shift or type name..." else "Pilih shift atau ketik nama...", fontSize = 12.sp) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEscortDropdown)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = warmOrange,
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
                                    .testTag("direct_chip_shift_${shift.lowercase().replace(" ", "_")}")
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

                    // Catatan Tambahan
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (isEn) "Additional Notes (Optional)" else "Catatan Tambahan (Opsional)", fontSize = 12.sp) },
                        placeholder = { Text(if (isEn) "e.g.: Outpatient rest in Asrama room" else "misal: Rawat jalan di kamar asrama", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = warmBorder.copy(alpha = 0.7f))

                    // Action Buttons
                    val isFormValid = selectedStudent != null &&
                        complaint.isNotBlank() &&
                        diagnosis.isNotBlank() &&
                        (isInpatient || prescription.isNotBlank())

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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isInpatient) inpatientRed else medBlue,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(0.62f)
                                .heightIn(min = 48.dp)
                                .testTag("confirm_direct_treatment_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = when {
                                    isEditMode -> if (isEn) "Save Changes" else "Simpan Perubahan"
                                    isInpatient -> if (isEn) "Save Inpatient" else "Simpan Rawat Inap"
                                    else -> if (isEn) "Save Treatment" else "Simpan Pengobatan"
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

    if (showStudentSearchDialog) {
        SearchStudentDialog(
            students = students,
            selectedStudent = selectedStudent,
            onSelect = { selectedStudent = it },
            onDismiss = { showStudentSearchDialog = false }
        )
    }

    if (showControlDatePicker) {
        val initialUtcMillis = remember(selectedControlDateMillis) {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedControlDateMillis }
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            }
            utcCal.timeInMillis
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialUtcMillis
        )
        DatePickerDialog(
            onDismissRequest = { showControlDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
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
                            selectedControlDateMillis = localCal.timeInMillis
                        }
                        showControlDatePicker = false
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("direct_btn_confirm_control_date_picker")
                ) {
                    Text(if (isEn) "Select Date" else "Pilih Tanggal")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showControlDatePicker = false },
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
                        text = if (isEn) "Select Follow-up Control Date" else "Pilih Tanggal Kontrol Kembali",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = medBlue
                    )
                }
            )
        }
    }
}
