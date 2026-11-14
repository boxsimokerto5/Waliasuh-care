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
import com.example.util.StudentClassifier
import com.example.util.StudentCsvParser
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportStudentsCsvDialog(
    currentSchoolId: String = "SRT1-KDR",
    schoolName: String = "Sekolah Rakyat SRT 1 Kediri",
    onDismiss: () -> Unit,
    onImportConfirm: (studentList: List<ChildStudent>, replace: Boolean) -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    var csvText by remember { mutableStateOf("") }
    var uploadedFileName by remember { mutableStateOf<String?>(null) }
    var replaceExisting by remember { mutableStateOf(false) }
    var parsedStudents by remember { mutableStateOf<List<ChildStudent>>(emptyList()) }
    var parseError by remember { mutableStateOf<String?>(null) }

    val studentTemplateCsv = StudentCsvParser.STANDARD_TEMPLATE_CSV

    // Launcher untuk download / simpan file CSV template ke perangkat (UTF-8 + BOM agar rapi di Excel)
    val createTemplateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    val contentWithBom = "\uFEFF$studentTemplateCsv"
                    os.write(contentWithBom.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(
                    context,
                    if (isEn) "Standard Student CSV template downloaded!" else "Template CSV Siswa standar berhasil diunduh!",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    if (isEn) "Failed to download template: ${e.message}" else "Gagal mengunduh template: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Launcher untuk upload / pilih file CSV dari perangkat
    val pickCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                val content = rawBytes?.let { StudentCsvParser.decodeCsvBytes(it) }
                if (!content.isNullOrBlank()) {
                    csvText = content
                    uploadedFileName = uri.lastPathSegment ?: "file_siswa.csv"
                    parsedStudents = StudentCsvParser.parseCsv(content, currentSchoolId)
                    parseError = null
                    Toast.makeText(
                        context,
                        if (isEn) {
                            "CSV file loaded: ${parsedStudents.size} students detected!"
                        } else {
                            "File CSV berhasil dimuat: ${parsedStudents.size} siswa terdeteksi!"
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        if (isEn) "CSV file is empty." else "File CSV kosong.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                val errMsg = if (isEn) "Failed to read file: ${e.message}" else "Gagal membaca file: ${e.message}"
                parseError = errMsg
                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun copyTemplateToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Template CSV Siswa", studentTemplateCsv))
        Toast.makeText(
            context,
            if (isEn) "Student CSV template copied to clipboard!" else "Format template CSV siswa disalin ke clipboard!",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun shareTemplate() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_SUBJECT,
                if (isEn) "Student Data CSV Template - $schoolName" else "Template CSV Data Siswa - $schoolName"
            )
            putExtra(Intent.EXTRA_TEXT, studentTemplateCsv)
        }
        context.startActivity(
            Intent.createChooser(
                intent,
                if (isEn) "Share Student CSV Template" else "Bagikan Template CSV Siswa"
            )
        )
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
                            text = if (isEn) "Import Student Data (CSV)" else "Impor Data Siswa (CSV)",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEn) {
                                "Upload a CSV file or use the official template with school ID"
                            } else {
                                "Unggah file CSV atau gunakan template resmi ber-ID sekolah"
                            },
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
                                    text = if (isEn) {
                                        "Data locked to ID: $currentSchoolId (Safe & Isolated)"
                                    } else {
                                        "Data terkunci ke ID: $currentSchoolId (Aman & Tidak Bercampur)"
                                    },
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
                                text = if (isEn) {
                                    "Standard Student CSV Template (12 Complete Columns)"
                                } else {
                                    "Template CSV Siswa Standar (12 Kolom Lengkap)"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (isEn) {
                                "Official 12-column format: No, Nama, Kelas, Jenjang, Alamat, Jenis Kelamin, NIK, NO KK, TEMPAT LAHIR, TANGGAL LAHIR, NAMA IBU, NISN. Automatically cleans hidden characters, Excel title rows, & commas in addresses."
                            } else {
                                "Format resmi 12 kolom: No, Nama, Kelas, Jenjang, Alamat, Jenis Kelamin, NIK, NO KK, TEMPAT LAHIR, TANGGAL LAHIR, NAMA IBU, NISN. Otomatis membersihkan karakter tersembunyi, baris judul Excel, & koma pada alamat."
                            },
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
                                Text(
                                    if (isEn) "Download Template" else "Download Template",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
                                Text(if (isEn) "Copy Format" else "Salin Format", fontSize = 11.sp)
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
                                Text(if (isEn) "Share" else "Bagikan", fontSize = 11.sp)
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
                                text = if (isEn) "File Selected: $uploadedFileName" else "File Berhasil Dipilih: $uploadedFileName",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                            Text(
                                text = if (isEn) {
                                    "${parsedStudents.size} students ready to import into $schoolName"
                                } else {
                                    "${parsedStudents.size} data siswa siap diimpor ke $schoolName"
                                },
                                fontSize = 11.5.sp,
                                color = Color(0xFF166534)
                            )
                        } else {
                            Text(
                                text = if (isEn) "Select & Upload CSV File from Device" else "Pilih & Upload File CSV dari Perangkat",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn) {
                                    "Tap to choose the completed .csv template file"
                                } else {
                                    "Ketuk untuk memilih file .csv hasil pengisian template"
                                },
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
                            Text(
                                text = if (uploadedFileName != null) {
                                    if (isEn) "Change CSV File" else "Ganti File CSV"
                                } else {
                                    if (isEn) "Choose File (.csv)" else "Pilih File (.csv)"
                                },
                                fontSize = 12.sp
                            )
                        }
                    }
                }


                // 4. PREVIEW & EDIT MANUAL
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isEn) {
                            "Or Enter / Edit CSV Text Manually:"
                        } else {
                            "Atau Masukkan / Edit Teks CSV Secara Manual:"
                        },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = csvText,
                        onValueChange = {
                            csvText = it
                            try {
                                parsedStudents = StudentCsvParser.parseCsv(it, currentSchoolId)
                                parseError = null
                            } catch (e: Exception) {
                                parseError = e.message
                            }
                        },
                        label = { Text(if (isEn) "CSV Text Content" else "Isi Teks CSV") },
                        placeholder = { Text(StudentCsvParser.STANDARD_HEADER) },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("csv_text_input")
                    )
                }

                if (parsedStudents.isNotEmpty()) {
                    val countPutra = parsedStudents.count { StudentClassifier.isMaleStudent(it) }
                    val countPutri = parsedStudents.count { StudentClassifier.isFemaleStudent(it) }
                    val countWithNik = parsedStudents.count { it.nik.isNotBlank() }
                    val countWithKk = parsedStudents.count { it.noKk.isNotBlank() }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF0FDF4))
                            .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) {
                                    "✅ ${parsedStudents.size} Valid Student Records"
                                } else {
                                    "✅ ${parsedStudents.size} Data Siswa Valid"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFDBEAFE))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        if (isEn) "Boys: $countPutra" else "Putra: $countPutra",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFCE7F3))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        if (isEn) "Girls: $countPutri" else "Putri: $countPutri",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF9D174D)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isEn) {
                                "Completeness Detected: NIK ($countWithNik/${parsedStudents.size}) • Family Card No. ($countWithKk/${parsedStudents.size})"
                            } else {
                                "Kelengkapan Terbaca: NIK ($countWithNik/${parsedStudents.size}) • No. KK ($countWithKk/${parsedStudents.size})"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF166534)
                        )

                        parsedStudents.take(3).forEach { sample ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = sample.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF1E293B),
                                            modifier = Modifier.weight(1f)
                                        )
                                        val isFemaleSample = StudentClassifier.isFemaleStudent(sample)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isFemaleSample) Color(0xFFFCE7F3) else Color(0xFFDBEAFE))
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "${sample.jenjang.ifBlank { "-" }} • ${sample.classGrade}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isFemaleSample) Color(0xFF9D174D) else Color(0xFF1E40AF)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "NIK: ${sample.nik.ifBlank { "-" }} • KK: ${sample.noKk.ifBlank { "-" }}",
                                        fontSize = 10.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF334155)
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "NISN: ${sample.nis.ifBlank { "-" }} • Birth: ${sample.birthPlace.ifBlank { "-" }}, ${sample.birthDate.ifBlank { "-" }} (${sample.age} yrs)"
                                        } else {
                                            "NISN: ${sample.nis.ifBlank { "-" }} • TTL: ${sample.birthPlace.ifBlank { "-" }}, ${sample.birthDate.ifBlank { "-" }} (${sample.age} thn)"
                                        },
                                        fontSize = 10.sp,
                                        color = Color(0xFF475569)
                                    )
                                    if (sample.motherName.isNotBlank()) {
                                        Text(
                                            text = if (isEn) "Mother: ${sample.motherName}" else "Ibu: ${sample.motherName}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                    if (sample.address.isNotBlank()) {
                                        Text(
                                            text = if (isEn) "Address: ${sample.address}" else "Alamat: ${sample.address}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (parseError != null) {
                    Text(
                        text = if (isEn) "Parsing error: $parseError" else "Error parsing: $parseError",
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
                        text = if (isEn) {
                            "Replace (delete) all existing students for school $currentSchoolId before importing"
                        } else {
                            "Gantikan (hapus) seluruh siswa lama sekolah $currentSchoolId sebelum impor"
                        },
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
                        Text(if (isEn) "Close" else "Tutup")
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
                        Text(
                            if (isEn) {
                                "Import (${parsedStudents.size}) Students"
                            } else {
                                "Impor (${parsedStudents.size}) Siswa"
                            }
                        )
                    }
                }
            }
        }
    }
}
