package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.ui.theme.WaliPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportStudentsCsvDialog(
    onDismiss: () -> Unit,
    onImportConfirm: (studentList: List<ChildStudent>, replace: Boolean) -> Unit,
    onLoadDefaultKediri: ((replace: Boolean) -> Unit)? = null
) {
    var csvText by remember { mutableStateOf("") }
    var replaceExisting by remember { mutableStateOf(true) }
    var parsedStudents by remember { mutableStateOf<List<ChildStudent>>(emptyList()) }
    var parseError by remember { mutableStateOf<String?>(null) }

    fun parseCsv(text: String): List<ChildStudent> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val result = mutableListOf<ChildStudent>()
        val startIdx = if (lines.first().contains("Nama", ignoreCase = true) || lines.first().contains("NIK", ignoreCase = true)) 1 else 0

        for (i in startIdx until lines.size) {
            val line = lines[i]
            val tokens = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (tokens.size >= 2) {
                val name = tokens.getOrNull(1)?.ifBlank { tokens.getOrNull(0) ?: "" } ?: ""
                if (name.isBlank() || name.equals("Nama", ignoreCase = true)) continue
                val gender = if (tokens.any { it.equals("P", ignoreCase = true) || it.contains("Perempuan", ignoreCase = true) }) "P" else "L"
                val dorm = if (gender == "P") "Asrama Putri" else "Asrama Putra"
                val classGrade = tokens.getOrNull(2)?.ifBlank { "Kelas -" } ?: "Kelas -"

                result.add(
                    ChildStudent(
                        id = (i + 1).toLong(),
                        name = name,
                        nis = tokens.getOrNull(11)?.ifBlank { "SRT1-${i + 1}" } ?: "SRT1-${i + 1}",
                        gender = gender,
                        age = 12,
                        dormitory = dorm,
                        classGrade = classGrade,
                        allergies = "Tidak ada",
                        bloodType = "-",
                        emergencyContact = "Pengasuh Asrama",
                        specialNotes = "",
                        nik = tokens.getOrNull(6) ?: "",
                        noKk = tokens.getOrNull(7) ?: "",
                        birthPlace = tokens.getOrNull(8) ?: "",
                        birthDate = tokens.getOrNull(9) ?: "",
                        motherName = tokens.getOrNull(10) ?: "",
                        address = tokens.getOrNull(4) ?: "",
                        roomNumber = tokens.getOrNull(12) ?: "",
                        jenjang = tokens.getOrNull(3) ?: ""
                    )
                )
            }
        }
        return result
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = WaliPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Impor Data Siswa (CSV)",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Perbarui database siswa via CSV atau gunakan database bawaan",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Opsi Muat Database Default Kediri
                if (onLoadDefaultKediri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Database Resmi SRT 1 Kediri (342 Siswa)",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = "Gunakan data lengkap 342 siswa anak asuh Sekolah Rakyat SRT 1 Kediri bawaan sistem.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Button(
                                onClick = { onLoadDefaultKediri(replaceExisting) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("load_default_students_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Muat 342 Siswa Kediri")
                            }
                        }
                    }
                }

                Text(
                    text = "Atau Tempel / Masukkan Format CSV:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = csvText,
                    onValueChange = {
                        csvText = it
                        try {
                            parsedStudents = parseCsv(it)
                            parseError = null
                        } catch (e: Exception) {
                            parseError = e.message
                        }
                    },
                    label = { Text("Isi Data CSV Siswa") },
                    placeholder = { Text("No,Nama,Kelas,Jenjang,Alamat,Jenis Kelamin,NIK,NO KK...") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("csv_text_input")
                )

                if (parsedStudents.isNotEmpty()) {
                    Text(
                        text = "Terdeteksi ${parsedStudents.size} data siswa siap diimpor.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WaliPrimary
                    )
                }

                if (parseError != null) {
                    Text(
                        text = "Error parsing: $parseError",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = replaceExisting,
                        onCheckedChange = { replaceExisting = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gantikan (hapus) seluruh siswa lama sebelum impor",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Tutup")
                    }

                    Button(
                        onClick = {
                            if (parsedStudents.isNotEmpty()) {
                                onImportConfirm(parsedStudents, replaceExisting)
                            }
                        },
                        enabled = parsedStudents.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("confirm_import_students_btn")
                    ) {
                        Text("Impor (${parsedStudents.size})")
                    }
                }
            }
        }
    }
}
