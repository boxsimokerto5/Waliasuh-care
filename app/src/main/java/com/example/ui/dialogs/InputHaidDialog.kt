package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ChildStudent
import com.example.data.model.HaidRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InputHaidDialog(
    students: List<ChildStudent>,
    activeHaidRecords: List<HaidRecord>,
    waliName: String,
    schoolId: String,
    onSaveHaid: (HaidRecord) -> Unit,
    onDismiss: () -> Unit
) {
    // Siswi putri saja yang relevan
    val femaleStudents = remember(students) {
        students.filter { it.gender.equals("P", ignoreCase = true) || it.dormitory.contains("Putri", ignoreCase = true) }
    }

    var selectedStudent by remember { mutableStateOf<ChildStudent?>(null) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var showManualSearchDialog by remember { mutableStateOf(false) }

    var startDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedSymptom by remember { mutableStateOf("Normal (Tanpa Keluhan)") }
    var selectedTreatment by remember { mutableStateOf("Tidak butuh obat") }
    var notesInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm", Locale("id", "ID")) }

    // Cek apakah siswi yang dipilih sedang dalam haid aktif
    val existingActiveHaid = remember(selectedStudent, activeHaidRecords) {
        selectedStudent?.let { s ->
            activeHaidRecords.firstOrNull { it.studentId == s.id || (it.studentNik.isNotBlank() && it.studentNik == s.nik) }
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 500.dp)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
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
                                text = "Pendataan Haid Siswi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Scan Kartu / NIK Santriwati Asrama",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("btn_close_input_haid")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Scan Barcode or Search
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showScannerDialog = true },
                        modifier = Modifier
                            .weight(1.3f)
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
                        modifier = Modifier.weight(1f).testTag("btn_search_female_student"),
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFBCFE8), RoundedCornerShape(14.dp))
                            .background(Color(0xFFFDF2F8))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Silakan Scan Barcode Kartu Siswa atau Pilih Siswi terlebih dahulu",
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
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF472B6))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
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
                    }

                    if (existingActiveHaid != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Perhatian: ${s.name} saat ini sedang tercatat haid (Hari ke-${existingActiveHaid.durationDays}). Anda dapat mencatat mandi suci di menu 'Daftar Haid'.",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Waktu Mulai Haid
                Text(
                    text = "Waktu Mulai Haid:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dateFormat.format(Date(startDateMillis)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Keluhan / Gejala Fisik
                Text(
                    text = "Keluhan / Kondisi Fisik Siswi:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val symptomsList = listOf(
                        "Normal (Tanpa Keluhan)",
                        "Nyeri Haid (Dismenore)",
                        "Kram Perut",
                        "Pusing / Sakit Kepala",
                        "Mual",
                        "Sakit Pinggang",
                        "Lemas / Pucat"
                    )
                    symptomsList.forEach { item ->
                        val isSelected = selectedSymptom == item
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSymptom = item },
                            label = { Text(item, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFCE7F3),
                                selectedLabelColor = Color(0xFF9D174D)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Penanganan / Kebutuhan Obat di UKS
                Text(
                    text = "Tindakan & Obat di UKS:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val treatmentsList = listOf(
                        "Tidak butuh obat",
                        "Diberi Paracetamol",
                        "Diberi Minuman Hangat",
                        "Jamu Kunyit Asam",
                        "Kompres Hangat",
                        "Istirahat di UKS"
                    )
                    treatmentsList.forEach { tr ->
                        val isSelected = selectedTreatment == tr
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTreatment = tr },
                            label = { Text(tr, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDCFCE7),
                                selectedLabelColor = Color(0xFF166534)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Catatan Tambahan
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Catatan Tambahan (Opsional)", fontSize = 12.sp) },
                    placeholder = { Text("Misal: Mengeluh kram saat jam pelajaran ke-3", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("input_haid_notes"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
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

                        val notesCombined = buildString {
                            if (selectedTreatment != "Tidak butuh obat") {
                                append("Tindakan: $selectedTreatment. ")
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
                            startDate = startDateMillis,
                            endDate = null,
                            status = "HAID",
                            symptoms = selectedSymptom,
                            notes = notesCombined.trim(),
                            waliName = waliName,
                            schoolId = schoolId
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
                    Text("Simpan Pendataan Haid", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Barcode Scanner Modal
    if (showScannerDialog) {
        BarcodeScannerDialog(
            femaleStudents = femaleStudents,
            onScanned = { code ->
                val q = code.trim().lowercase()
                val matched = femaleStudents.firstOrNull {
                    it.nik.trim().equals(q, ignoreCase = true) ||
                    it.nis.trim().equals(q, ignoreCase = true) ||
                    it.id.toString() == q ||
                    it.name.lowercase().contains(q)
                } ?: femaleStudents.firstOrNull()

                if (matched != null) {
                    selectedStudent = matched
                    errorMessage = ""
                } else {
                    errorMessage = "Kartu NIK/NIS '$code' tidak ditemukan di database siswi putri."
                }
                showScannerDialog = false
            },
            onDismiss = { showScannerDialog = false }
        )
    }

    // Manual Search Student Dialog
    if (showManualSearchDialog) {
        SearchStudentDialog(
            students = femaleStudents,
            selectedStudent = selectedStudent,
            onSelect = { s ->
                selectedStudent = s
                errorMessage = ""
                showManualSearchDialog = false
            },
            onDismiss = { showManualSearchDialog = false }
        )
    }
}
