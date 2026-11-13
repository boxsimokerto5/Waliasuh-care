package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.HaidDateCalculator
import com.example.data.model.HaidRecord
import com.example.util.StudentClassifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun matchHaidRecordToStudent(record: HaidRecord, student: ChildStudent): Boolean {
    return (student.id > 0L && record.studentId == student.id) ||
        (record.studentNik.isNotBlank() && student.nik.isNotBlank() && record.studentNik.equals(student.nik, ignoreCase = true)) ||
        (record.studentNis.isNotBlank() && student.nis.isNotBlank() && record.studentNis.equals(student.nis, ignoreCase = true))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InputHaidDialog(
    students: List<ChildStudent>,
    activeHaidRecords: List<HaidRecord>,
    suciRecords: List<HaidRecord> = emptyList(),
    waliName: String,
    schoolId: String,
    initialStudent: ChildStudent? = null,
    initialTissueVerified: Boolean = false,
    onSaveHaid: (HaidRecord) -> Unit,
    onCompleteHaid: (Long) -> Unit = {},
    onDismiss: () -> Unit
) {
    val femaleStudents = remember(students) {
        students.filter { StudentClassifier.isFemaleStudent(it) }
    }

    var selectedStudent by remember { mutableStateOf(initialStudent) }
    var selectedHaidDay by remember { mutableIntStateOf(1) }
    var notesInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    var showScannerDialog by remember { mutableStateOf(false) }
    var showManualSearchDialog by remember { mutableStateOf(false) }

    // Pop-up verifikasi jika siswi terdeteksi masih berada di Daftar Suci (< 15 hari)
    var pendingTissueVerification by remember { mutableStateOf<Pair<ChildStudent, HaidRecord>?>(null) }
    var verifiedFromSuciDay by remember { mutableStateOf<Int?>(null) }

    // Pop-up jika siswi sudah tercatat masih haid aktif di Daftar Haid
    var pendingAlreadyActiveHaid by remember { mutableStateOf<Pair<ChildStudent, HaidRecord>?>(null) }

    val dateFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")) }

    fun findLatestSuciRecord(student: ChildStudent): HaidRecord? {
        return suciRecords
            .filter { matchHaidRecordToStudent(it, student) }
            .maxByOrNull { it.effectiveEndDate ?: it.endDate ?: it.startDate }
    }

    fun handleStudentChosen(student: ChildStudent, bypassTissueCheck: Boolean = false) {
        errorMessage = ""
        val activeMatch = activeHaidRecords.firstOrNull { matchHaidRecordToStudent(it, student) }
        if (activeMatch != null) {
            pendingAlreadyActiveHaid = student to activeMatch
            return
        }

        val latestSuci = findLatestSuciRecord(student)
        if (!bypassTissueCheck && latestSuci != null && latestSuci.isInMinimumSuciPeriod) {
            pendingTissueVerification = student to latestSuci
            return
        }

        selectedStudent = student
        verifiedFromSuciDay = if (bypassTissueCheck && latestSuci != null && latestSuci.isInMinimumSuciPeriod) {
            latestSuci.suciDayCount
        } else {
            null
        }
    }

    LaunchedEffect(initialStudent, initialTissueVerified) {
        if (initialStudent != null) {
            handleStudentChosen(initialStudent, bypassTissueCheck = initialTissueVerified)
        }
    }

    val latestSuciForSelected = remember(selectedStudent, suciRecords, activeHaidRecords) {
        selectedStudent?.let { s ->
            val isActive = activeHaidRecords.any { matchHaidRecordToStudent(it, s) }
            if (isActive) null else findLatestSuciRecord(s)
        }
    }

    val calculatedStartMillis = remember(selectedHaidDay) {
        HaidDateCalculator.calculateStartDateForHaidDay(selectedHaidDay)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
                    .padding(bottom = 28.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFCE7F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Female,
                                contentDescription = null,
                                tint = Color(0xFFDB2777),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Input Pendataan Haid",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pilih siswi, tentukan hari haid ke-..., & catatan",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_input_haid")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: Scan Barcode or Search
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showScannerDialog = true },
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_open_barcode_scanner"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777))
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Kartu Siswa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showManualSearchDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_search_female_student"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pilih Siswi", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Card Siswi Terpilih
                if (selectedStudent == null) {
                    val emptyPromptText = when {
                        students.isEmpty() -> "Belum ada data siswa di sekolah ini. Admin perlu mengunggah CSV atau menambah siswa terlebih dahulu."
                        femaleStudents.isEmpty() -> "Belum ada data siswi putri di sekolah ini. Pastikan kolom Jenis Kelamin bernilai 'P / Perempuan' atau Asrama Putri."
                        else -> "Silakan Scan Barcode Kartu Siswa atau Pilih Siswi terlebih dahulu (${femaleStudents.size} siswi tersedia)"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFFBCFE8), RoundedCornerShape(14.dp))
                            .background(Color(0xFFFDF2F8))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyPromptText,
                            fontSize = 11.5.sp,
                            color = Color(0xFF9D174D),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    val s = selectedStudent!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F8)),
                        border = BorderStroke(1.dp, Color(0xFFF472B6))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFBCFE8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = s.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color(0xFF9D174D)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = s.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF831843)
                                    )
                                    Text(
                                        text = "${s.classGrade} • ${s.dormitory}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF9D174D)
                                    )
                                    Text(
                                        text = "NIK: ${s.nik.ifBlank { s.nis.ifBlank { "ID #" + s.id } }}",
                                        fontSize = 10.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFBE185D)
                                    )
                                }
                            }

                            if (verifiedFromSuciDay != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFEF3C7))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Terverifikasi tes tisu (sebelumnya di Daftar Suci Hari ke-${verifiedFromSuciDay})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            } else if (latestSuciForSelected != null && !latestSuciForSelected.isInMinimumSuciPeriod) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Riwayat terakhir: Suci Hari ke-${latestSuciForSelected.suciDayCount} (masa minimal ≥ 15 hari terpenuhi)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fitur Utama: Pilih Haid Hari ke-...
                Text(
                    text = "Haid Hari ke-...",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Timeline berjalan otomatis setiap hari dan berpindah ke Daftar Suci setelah hari ke-15.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F8)),
                    border = BorderStroke(1.dp, Color(0xFFFBCFE8))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = { if (selectedHaidDay > 1) selectedHaidDay-- },
                                enabled = selectedHaidDay > 1,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_decrease_haid_day")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Kurangi Hari Haid"
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Hari ke-$selectedHaidDay",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selectedHaidDay == HaidDateCalculator.MAX_HAID_DAYS) {
                                        Color(0xFFDC2626)
                                    } else {
                                        Color(0xFFDB2777)
                                    },
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Mulai Hari ke-1: ${dateFormat.format(Date(calculatedStartMillis))}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9D174D),
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }

                            FilledTonalIconButton(
                                onClick = {
                                    if (selectedHaidDay < HaidDateCalculator.MAX_HAID_DAYS) selectedHaidDay++
                                },
                                enabled = selectedHaidDay < HaidDateCalculator.MAX_HAID_DAYS,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_increase_haid_day")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah Hari Haid"
                                )
                            }
                        }

                        // Pilihan Cepat Hari ke-1 s.d. 15
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(1, 2, 3, 4, 5, 6, 7, 10, 14, 15).forEach { dayOption ->
                                val isSelected = selectedHaidDay == dayOption
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedHaidDay = dayOption },
                                    label = {
                                        Text(
                                            text = "H-$dayOption",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFDB2777),
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("chip_haid_day_$dayOption")
                                )
                            }
                        }

                        // Preview Timeline Berjalan
                        HaidTimelineBar(currentDay = selectedHaidDay)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tambah Catatan Saja
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Tambah Catatan (Opsional)", fontSize = 12.sp) },
                    placeholder = { Text("Misal: Lapor hari ke-2, atau keterangan khusus asrama", fontSize = 11.sp) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_haid_notes"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = {
                        val student = selectedStudent
                        if (student == null) {
                            errorMessage = "Pilih atau scan kartu siswi terlebih dahulu!"
                            return@Button
                        }

                        val finalNotes = buildString {
                            if (verifiedFromSuciDay != null) {
                                append("[Terverifikasi tes tisu dari Daftar Suci Hari ke-${verifiedFromSuciDay}]")
                                if (notesInput.isNotBlank()) append(" ")
                            }
                            if (notesInput.isNotBlank()) {
                                append(notesInput.trim())
                            }
                        }

                        val record = HaidRecord(
                            studentId = student.id,
                            studentName = student.name,
                            studentNik = student.nik,
                            studentNis = student.nis,
                            studentClass = student.classGrade,
                            studentDorm = student.dormitory,
                            startDate = calculatedStartMillis,
                            endDate = null,
                            status = "HAID",
                            symptoms = "Normal",
                            notes = finalNotes.trim(),
                            waliName = waliName,
                            schoolId = schoolId.ifBlank { "SRT1-KDR" }
                        )

                        onSaveHaid(record)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_save_haid_record"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777))
                ) {
                    Icon(imageVector = Icons.Default.Healing, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Haid Hari ke-$selectedHaidDay", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Barcode Scanner Bottom Sheet
    if (showScannerDialog) {
        BarcodeScannerDialog(
            femaleStudents = femaleStudents,
            onScanned = { code ->
                val matched = StudentClassifier.findMatchingFemaleStudent(femaleStudents, code)
                showScannerDialog = false
                if (matched != null) {
                    handleStudentChosen(matched)
                } else {
                    selectedStudent = null
                    verifiedFromSuciDay = null
                    errorMessage = "Kartu NIK/NIS '$code' tidak ditemukan di database siswi putri."
                }
            },
            onDismiss = { showScannerDialog = false }
        )
    }

    // Manual Search Student Bottom Sheet
    if (showManualSearchDialog) {
        SearchStudentDialog(
            students = femaleStudents,
            selectedStudent = selectedStudent,
            onSelect = { s ->
                showManualSearchDialog = false
                handleStudentChosen(s)
            },
            onDismiss = { showManualSearchDialog = false }
        )
    }

    // Pop-up Peringatan & Verifikasi Tes Tisu jika siswi masih berada di Daftar Suci (< 15 hari)
    pendingTissueVerification?.let { (student, suciRec) ->
        SuciTissueVerificationSheet(
            studentName = student.name,
            suciRecord = suciRec,
            onCancelStaySuci = {
                pendingTissueVerification = null
                if (selectedStudent?.id == student.id) {
                    selectedStudent = null
                }
                errorMessage = "Dibatalkan: ${student.name} tetap di Daftar Suci (Hari ke-${suciRec.suciDayCount})."
            },
            onConfirmMoveToHaid = {
                pendingTissueVerification = null
                selectedStudent = student
                verifiedFromSuciDay = suciRec.suciDayCount
                errorMessage = ""
            }
        )
    }

    // Pop-up Informasi jika siswi yang di-scan masih tercatat HAID AKTIF di Daftar Haid
    pendingAlreadyActiveHaid?.let { (student, activeRec) ->
        ModalBottomSheet(
            onDismissRequest = { pendingAlreadyActiveHaid = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
                        .widthIn(max = 540.dp)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Siswi Masih di Daftar Haid",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${student.name} • Haid Hari ke-${activeRec.durationDays}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFDB2777)
                            )
                        }
                    }

                    Text(
                        text = "${student.name} saat ini masih aktif di Daftar Haid (Hari ke-${activeRec.durationDays} dari maksimal 15 hari). Jika hari ini sudah selesai bersuci, Anda dapat langsung memindahkannya ke Daftar Suci.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { pendingAlreadyActiveHaid = null },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Tutup")
                        }
                        Button(
                            onClick = {
                                onCompleteHaid(activeRec.id)
                                pendingAlreadyActiveHaid = null
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Pindahkan ke Daftar Suci", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
