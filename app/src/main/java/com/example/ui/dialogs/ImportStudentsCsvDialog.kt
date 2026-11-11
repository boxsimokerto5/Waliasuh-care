package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.ui.theme.WaliPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportStudentsCsvDialog(
    currentSchoolId: String = "SRT1-KDR",
    schoolName: String = "Sekolah Rakyat SRT 1 Kediri",
    onDismiss: () -> Unit,
    onImportConfirm: (studentList: List<ChildStudent>, replace: Boolean) -> Unit
) {
    val context = LocalContext.current
    var csvText by remember { mutableStateOf("") }
    var uploadedFileName by remember { mutableStateOf<String?>(null) }
    var replaceExisting by remember { mutableStateOf(false) }
    var parsedStudents by remember { mutableStateOf<List<ChildStudent>>(emptyList()) }
    var parseError by remember { mutableStateOf<String?>(null) }

    val studentTemplateCsv = """
        No,Nama Lengkap,Kelas,Jenjang,Alamat,Jenis Kelamin,NIK,No KK,Tempat Lahir,Tanggal Lahir,Nama Ibu,NIS,No Kamar
        1,Ahmad Fauzi,VII-A,SMP,Jl. Pemuda No. 10,L,3506012345670001,3506012345670002,Kediri,2011-05-12,Siti Aminah,${currentSchoolId}-001,Kamar 01
        2,Siti Nurhaliza,VIII-B,SMP,Jl. Melati No. 5,P,3506012345670003,3506012345670004,Kediri,2010-08-20,Nurhayati,${currentSchoolId}-002,Kamar 05
        3,Rian Ardiansyah,IX-C,SMP,Jl. Kenanga No. 22,L,3506012345670005,3506012345670006,Kediri,2009-11-03,Dewi Sartika,${currentSchoolId}-003,Kamar 02
    """.trimIndent()

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
                        nis = tokens.getOrNull(11)?.ifBlank { "${currentSchoolId}-${i + 1}" } ?: "${currentSchoolId}-${i + 1}",
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
                        jenjang = tokens.getOrNull(3) ?: "",
                        schoolId = currentSchoolId
                    )
                )
            }
        }
        return result
    }

    // Launcher untuk download / simpan file CSV template ke perangkat
    val createTemplateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(studentTemplateCsv.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Template CSV Siswa berhasil diunduh!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal mengunduh template: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher untuk upload / pilih file CSV dari perangkat
    val pickCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    csvText = content
                    uploadedFileName = uri.lastPathSegment ?: "file_siswa.csv"
                    parsedStudents = parseCsv(content)
                    parseError = null
                    Toast.makeText(context, "File CSV berhasil dimuat: ${parsedStudents.size} siswa terdeteksi!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "File CSV kosong.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                parseError = "Gagal membaca file: ${e.message}"
                Toast.makeText(context, "Gagal membaca file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun copyTemplateToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Template CSV Siswa", studentTemplateCsv))
        Toast.makeText(context, "Format template CSV siswa disalin ke clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun shareTemplate() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Template CSV Data Siswa - $schoolName")
            putExtra(Intent.EXTRA_TEXT, studentTemplateCsv)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan Template CSV Siswa"))
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
                // Header Dialog
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
                            text = "Unggah file CSV atau gunakan template resmi ber-ID sekolah",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 1. KARTU IDENTITAS SEKOLAH & ISOLASI DATA (ID SEKOLAH)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = schoolName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "Data terkunci ke ID: $currentSchoolId (Aman & Tidak Bercampur)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF16A34A))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentSchoolId,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // 2. BAGIAN DOWNLOAD & BAGIKAN TEMPLATE CSV
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = WaliPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Template CSV Siswa Standar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Unduh template berisi format kolom lengkap (Nama, Kelas, NIK, No KK, NIS, dsb) untuk diisi di Excel / Google Sheets.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    createTemplateLauncher.launch("template_siswa_${currentSchoolId.lowercase()}.csv")
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(38.dp)
                                    .testTag("btn_download_student_template"),
                                colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download Template", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { copyTemplateToClipboard() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salin Format", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { shareTemplate() },
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(38.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bagikan", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // 3. KOLOM UPLOAD FILE CSV DARI PERANGKAT
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pickCsvLauncher.launch("*/*") },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uploadedFileName != null) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (uploadedFileName != null) Color(0xFF16A34A) else WaliPrimary.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (uploadedFileName != null) Color(0xFFDCFCE7) else WaliPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uploadedFileName != null) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                                contentDescription = null,
                                tint = if (uploadedFileName != null) Color(0xFF16A34A) else WaliPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        if (uploadedFileName != null) {
                            Text(
                                text = "File Berhasil Dipilih: $uploadedFileName",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                            Text(
                                text = "${parsedStudents.size} data siswa siap diimpor ke $schoolName",
                                fontSize = 11.5.sp,
                                color = Color(0xFF166534)
                            )
                        } else {
                            Text(
                                text = "Pilih & Upload File CSV dari Perangkat",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ketuk untuk memilih file .csv hasil pengisian template",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { pickCsvLauncher.launch("*/*") },
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("btn_upload_student_csv"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uploadedFileName != null) Color(0xFF16A34A) else WaliPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (uploadedFileName != null) "Ganti File CSV" else "Pilih File (.csv)", fontSize = 12.sp)
                        }
                    }
                }


                // 4. PREVIEW & EDIT MANUAL
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Atau Masukkan / Edit Teks CSV Secara Manual:",
                        fontSize = 12.5.sp,
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
                        label = { Text("Isi Teks CSV") },
                        placeholder = { Text("No,Nama Lengkap,Kelas,Jenjang,Alamat,Jenis Kelamin,NIK,No KK...") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("csv_text_input")
                    )
                }

                if (parsedStudents.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✅ Terdeteksi ${parsedStudents.size} data siswa siap diimpor untuk ID $currentSchoolId",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF15803D)
                        )
                    }
                }

                if (parseError != null) {
                    Text(
                        text = "Error parsing: $parseError",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Checkbox replace
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
                        text = "Gantikan (hapus) seluruh siswa lama sekolah $currentSchoolId sebelum impor",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
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
                            .weight(1.3f)
                            .height(46.dp)
                            .testTag("confirm_import_students_btn")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Impor (${parsedStudents.size}) Siswa")
                    }
                }
            }
        }
    }
}
