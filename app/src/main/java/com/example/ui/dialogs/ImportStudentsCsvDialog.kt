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
        No,Nama,Kelas,Kamar,Jenis Kelamin,NIK,Tempat Lahir,Tanggal Lahir,Nama Ibu,Alamat
        1,Adam Julian Shano,SD 1-2,PL_103,Laki-laki,3508220907180002,KEDIRI,09-07-2018,RUMIYATI,"DSN. MANUKAN RT 02 RW 08 DS. JABON KEC. BANYAKAN KAB. KEDIRI"
        2,Azzura Fauziah,SD 1-2,PP_103,Perempuan,1771063004190001,BENGKULU,30-04-2019,AULIA HANIFAH,"Karangtalun, Kras, KAB. KEDIRI"
        3,Ahmad Syaiful Arsyad,VII-1,PL_104,Laki-laki,3506012001140003,KEDIRI,20-01-2014,SITI MUNJIAH,"Pagung, Semen, KAB. KEDIRI"
        4,Siti Nurhaliza,VIII-1,PP_104,Perempuan,3506011408130004,KEDIRI,14-08-2013,SITI MASRUROH,"Bulu, Semen, KAB. KEDIRI"
    """.trimIndent()

    // Helper untuk memecah baris CSV dengan aturan RFC-4180 (aman terhadap koma di dalam tanda petik ganda)
    fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    // Helper untuk membersihkan NIK / KK dari notasi eksponensial (misal: 3.50822E+15)
    fun cleanNumericId(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val trimmed = raw.trim().removeSurrounding("\"").trim()
        if (trimmed.contains("E+", ignoreCase = true) || trimmed.contains("E-", ignoreCase = true)) {
            try {
                return java.math.BigDecimal(trimmed).toBigInteger().toString()
            } catch (_: Exception) {
                return trimmed
            }
        }
        return trimmed
    }

    // Estimasi usia siswa dari string tanggal lahir (misal: 9/7/2018, 28-04-2019, 2011-05-12)
    fun estimateAge(birthDate: String): Int {
        if (birthDate.isBlank()) return 12
        val yearMatch = Regex("\\b(19\\d\\d|20\\d\\d)\\b").find(birthDate)
        val birthYear = yearMatch?.value?.toIntOrNull()
        if (birthYear != null) {
            val calculated = 2026 - birthYear
            if (calculated in 5..25) return calculated
        }
        return 12
    }

    // Ekstraksi otomatis jenjang pendidikan (SD / SMP / SMA) dari kolom kelas
    fun deriveJenjang(classGrade: String): String {
        val upper = classGrade.uppercase()
        return when {
            upper.contains("SD") || upper.contains("MI") -> "SD"
            upper.contains("VII") || upper.contains("VIII") || upper.contains("IX") || upper.contains("SMP") || upper.contains("MTS") -> "SMP"
            upper.contains("X") || upper.contains("XI") || upper.contains("XII") || upper.contains("SMA") || upper.contains("SMK") || upper.contains("MA") -> "SMA"
            else -> "SMP"
        }
    }

    fun parseCsv(text: String): List<ChildStudent> {
        // Bersihkan UTF-8 Byte Order Mark (BOM) jika ada
        val cleanText = text.removePrefix("\uFEFF").trim()
        val lines = cleanText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val result = mutableListOf<ChildStudent>()
        val firstLineTokens = parseCsvLine(lines.first())

        // Deteksi apakah baris pertama adalah baris nama kolom (Header)
        val hasHeader = firstLineTokens.any { token ->
            val t = token.lowercase()
            t.contains("nama") || t.contains("kelas") || t.contains("nik") || t.contains("kamar") || t.contains("kelamin")
        }

        var idxNo = -1
        var idxName = -1
        var idxClass = -1
        var idxRoom = -1
        var idxGender = -1
        var idxNik = -1
        var idxNoKk = -1
        var idxBirthPlace = -1
        var idxBirthDate = -1
        var idxMother = -1
        var idxAddress = -1
        var idxJenjang = -1
        var idxNis = -1

        if (hasHeader) {
            firstLineTokens.forEachIndexed { idx, rawToken ->
                val col = rawToken.lowercase().replace("_", " ").replace("-", " ").trim()
                when {
                    col == "no" || col == "nomor" -> if (idxNo == -1) idxNo = idx
                    col.contains("nama lengkap") || col.contains("nama siswa") || col == "nama" -> if (idxName == -1) idxName = idx
                    col.contains("kelas") || col.contains("tingkat") -> if (idxClass == -1) idxClass = idx
                    col.contains("kamar") || col.contains("ruang") -> if (idxRoom == -1) idxRoom = idx
                    col.contains("kelamin") || col.contains("gender") || col == "jk" -> if (idxGender == -1) idxGender = idx
                    col == "nik" || col.contains("no nik") || col.contains("nomor nik") -> if (idxNik == -1) idxNik = idx
                    col.contains("kk") -> if (idxNoKk == -1) idxNoKk = idx
                    col.contains("tempat lahir") || col.contains("tmp lahir") || col == "tempat" -> if (idxBirthPlace == -1) idxBirthPlace = idx
                    col.contains("tanggal lahir") || col.contains("tgl lahir") || col == "tanggal" || col == "tgl" -> if (idxBirthDate == -1) idxBirthDate = idx
                    col.contains("ibu") || col.contains("wali") || col.contains("orang tua") -> if (idxMother == -1) idxMother = idx
                    col.contains("alamat") || col.contains("domisili") || col.contains("asal") -> if (idxAddress == -1) idxAddress = idx
                    col.contains("jenjang") -> if (idxJenjang == -1) idxJenjang = idx
                    col.contains("nis") || col.contains("nisn") -> if (idxNis == -1) idxNis = idx
                }
            }
        }

        // Fallback dinamis jika header tidak ada atau format 10 kolom sekolah langsung
        val isStandard10ColFormat = firstLineTokens.size in 9..11
        if (idxName == -1) idxName = if (firstLineTokens.size > 1) 1 else 0
        if (idxClass == -1) idxClass = 2

        if (isStandard10ColFormat && (!hasHeader || idxRoom == -1 || idxGender == -1)) {
            if (idxRoom == -1) idxRoom = 3
            if (idxGender == -1) idxGender = 4
            if (idxNik == -1) idxNik = 5
            if (idxBirthPlace == -1) idxBirthPlace = 6
            if (idxBirthDate == -1) idxBirthDate = 7
            if (idxMother == -1) idxMother = 8
            if (idxAddress == -1) idxAddress = 9
        }

        val startIdx = if (hasHeader) 1 else 0

        for (i in startIdx until lines.size) {
            val line = lines[i]
            val tokens = parseCsvLine(line)
            if (tokens.size >= 2) {
                val rawName = if (idxName in tokens.indices) tokens[idxName] else tokens.getOrNull(1) ?: tokens.getOrNull(0) ?: ""
                val name = rawName.trim().removeSurrounding("\"")
                if (name.isBlank() || name.equals("Nama", ignoreCase = true) || name.equals("Nama Lengkap", ignoreCase = true)) continue

                val rawClass = (if (idxClass in tokens.indices) tokens[idxClass] else "").trim().removeSurrounding("\"")
                val classGrade = rawClass.ifBlank { "Kelas -" }

                val rawRoom = (if (idxRoom in tokens.indices) tokens[idxRoom] else "").trim().removeSurrounding("\"")
                val rawGender = (if (idxGender in tokens.indices) tokens[idxGender] else "").trim().removeSurrounding("\"")

                // Deteksi jenis kelamin: periksa nilai kolom, atau inferensikan dari kode kamar (PL = Putra, PP = Putri)
                val isFemale = rawGender.contains("Perempuan", ignoreCase = true) ||
                        rawGender.equals("P", ignoreCase = true) ||
                        rawGender.contains("Putri", ignoreCase = true) ||
                        rawRoom.startsWith("PP", ignoreCase = true) ||
                        rawRoom.startsWith("AP", ignoreCase = true)
                val gender = if (isFemale) "P" else "L"

                // Deteksi otomatis asrama: dari prefix kamar (PL = Putra, PP = Putri) atau jenis kelamin
                val dormitory = when {
                    rawRoom.startsWith("PP", ignoreCase = true) || rawRoom.startsWith("AP", ignoreCase = true) -> "Asrama Putri"
                    rawRoom.startsWith("PL", ignoreCase = true) || rawRoom.startsWith("AL", ignoreCase = true) -> "Asrama Putra"
                    isFemale -> "Asrama Putri"
                    else -> "Asrama Putra"
                }

                val nik = cleanNumericId(if (idxNik in tokens.indices) tokens[idxNik] else "")
                val noKk = cleanNumericId(if (idxNoKk in tokens.indices) tokens[idxNoKk] else "")
                val birthPlace = (if (idxBirthPlace in tokens.indices) tokens[idxBirthPlace] else "").trim().removeSurrounding("\"")
                val birthDate = (if (idxBirthDate in tokens.indices) tokens[idxBirthDate] else "").trim().removeSurrounding("\"")
                val motherName = (if (idxMother in tokens.indices) tokens[idxMother] else "").trim().removeSurrounding("\"")
                val address = (if (idxAddress in tokens.indices) tokens[idxAddress] else "").trim().removeSurrounding("\"")

                val jenjangVal = if (idxJenjang in tokens.indices && tokens[idxJenjang].isNotBlank()) {
                    tokens[idxJenjang].trim().removeSurrounding("\"")
                } else {
                    deriveJenjang(classGrade)
                }

                val rawNo = if (idxNo in tokens.indices) tokens[idxNo].trim() else ""
                val nis = if (idxNis in tokens.indices && tokens[idxNis].isNotBlank()) {
                    tokens[idxNis].trim().removeSurrounding("\"")
                } else if (rawNo.toIntOrNull() != null) {
                    "${currentSchoolId}-${rawNo}"
                } else {
                    "${currentSchoolId}-${i + 1}"
                }

                val age = estimateAge(birthDate)
                val emergency = if (motherName.isNotBlank()) "Ibu: $motherName" else "Pengasuh $dormitory"

                result.add(
                    ChildStudent(
                        id = 0L,
                        name = name,
                        nis = nis,
                        gender = gender,
                        age = age,
                        dormitory = dormitory,
                        classGrade = classGrade,
                        allergies = "Tidak ada",
                        bloodType = "-",
                        emergencyContact = emergency,
                        specialNotes = if (address.isNotBlank()) "Alamat Asal: $address" else "",
                        nik = nik,
                        noKk = noKk,
                        birthPlace = birthPlace,
                        birthDate = birthDate,
                        motherName = motherName,
                        address = address,
                        roomNumber = rawRoom,
                        jenjang = jenjangVal,
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
                            text = "Unduh template 10 kolom standar Sekolah Rakyat (No, Nama, Kelas, Kamar, Jenis Kelamin, NIK, Tempat Lahir, Tanggal Lahir, Nama Ibu, Alamat). Mendukung tanda koma pada alamat & format Excel.",
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
                    val countPutra = parsedStudents.count { it.dormitory.contains("Putra") }
                    val countPutri = parsedStudents.count { it.dormitory.contains("Putri") }

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
                                text = "✅ ${parsedStudents.size} Data Siswa Valid",
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
                                    Text("Putra: $countPutra", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFCE7F3))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Putri: $countPutri", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9D174D))
                                }
                            }
                        }

                        Text(
                            text = "Contoh Hasil Pemetaan Kolom:",
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
                                            color = Color(0xFF1E293B)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (sample.gender == "P") Color(0xFFFCE7F3) else Color(0xFFDBEAFE))
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "${sample.dormitory} • ${sample.roomNumber.ifBlank { "-" }}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (sample.gender == "P") Color(0xFF9D174D) else Color(0xFF1E40AF)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Kelas: ${sample.classGrade} (${sample.jenjang}) • NIK: ${sample.nik.ifBlank { "-" }} • Usia: ${sample.age} thn",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF475569)
                                    )
                                    if (sample.address.isNotBlank()) {
                                        Text(
                                            text = "Alamat: ${sample.address}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
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
