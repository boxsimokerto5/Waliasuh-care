package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.SchoolProfile
import com.example.ui.components.ControlBadge
import com.example.ui.components.FaskesBadge
import com.example.ui.components.StudentAvatar
import com.example.util.DateUtils
import com.example.util.StudentClassifier
import com.example.util.StudentCsvParser
import com.example.util.i18n.LocalAppStrings
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudentDetailDialog(
    student: ChildStudent,
    studentRecords: List<MedicalRecord>,
    schoolProfile: SchoolProfile = SchoolProfile(),
    waliName: String = "Wali Asuh",
    mengajiRecords: List<MengajiRecord> = emptyList(),
    onInputMengaji: ((ChildStudent) -> Unit)? = null,
    onPrintKartuMengaji: ((ChildStudent) -> Unit)? = null,
    onEditStudent: ((ChildStudent) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPrintPdfDialog by remember { mutableStateOf(false) }
    var showCardGeneratorForStudent by remember { mutableStateOf(false) }
    var showInlineKartuMengaji by remember { mutableStateOf(false) }
    var showAllMengajiHistory by remember { mutableStateOf(false) }
    val orderedMengajiRecords = remember(mengajiRecords, student) {
        MengajiHelper.recordsForStudent(mengajiRecords, student)
    }

    val calNow = Calendar.getInstance()
    val curMonth = calNow.get(Calendar.MONTH)
    val curYear = calNow.get(Calendar.YEAR)
    val calItem = Calendar.getInstance()
    val thisMonthCount = studentRecords.count { record ->
        val time = record.treatmentDate ?: record.planDate
        calItem.timeInMillis = time
        calItem.get(Calendar.MONTH) == curMonth && calItem.get(Calendar.YEAR) == curYear
    }

    val isMale = StudentClassifier.isMaleStudent(student)
    val resolvedJenjang = student.jenjang.ifBlank {
        StudentCsvParser.deriveJenjang(student.classGrade)
    }
    val cleanNik = remember(student.nik) { StudentCsvParser.cleanDigitsOnly(student.nik) }
    val cleanNoKk = remember(student.noKk) { StudentCsvParser.cleanDigitsOnly(student.noKk) }

    fun copyValue(label: String, value: String) {
        if (value.isBlank() || value == "-") return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        Toast.makeText(
            context,
            if (isEn) "$label copied: $value" else "$label disalin: $value",
            Toast.LENGTH_SHORT
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
                    .widthIn(max = 620.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("student_detail_sheet"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StudentAvatar(
                            name = student.name,
                            gender = student.gender,
                            size = 50.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = student.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                StudentChipBadge(
                                    text = if (isMale) "Putra (L)" else "Putri (P)",
                                    bgColor = if (isMale) Color(0xFFDBEAFE) else Color(0xFFFCE7F3),
                                    textColor = if (isMale) Color(0xFF1E40AF) else Color(0xFF9D174D)
                                )
                                if (resolvedJenjang.isNotBlank()) {
                                    StudentChipBadge(
                                        text = resolvedJenjang,
                                        bgColor = Color(0xFFDCFCE7),
                                        textColor = Color(0xFF166534)
                                    )
                                }
                                if (student.classGrade.isNotBlank()) {
                                    StudentChipBadge(
                                        text = student.classGrade,
                                        bgColor = MaterialTheme.colorScheme.secondaryContainer,
                                        textColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "NIS / NISN: ${student.nis.ifBlank { "-" }} • ${if (isEn) "School ID" else "ID Sekolah"}: ${student.schoolId}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onEditStudent != null) {
                            IconButton(
                                onClick = { onEditStudent(student) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_edit_student_detail")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = if (isEn) "Edit Student Profile" else "Edit Profil Siswa",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_close_student_detail")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = s.nav.close
                            )
                        }
                    }
                }

                // Peringatan frekuensi sakit / alergi
                if (thisMonthCount >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEE2E2))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isEn) {
                                "⚠️ Alert: This student has been sick $thisMonthCount times this month"
                            } else {
                                "⚠️ Perhatian: Anak ini $thisMonthCount kali sakit bulan ini"
                            },
                            fontSize = 11.5.sp,
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (thisMonthCount == 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0F2FE))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isEn) "🩺 1 medical visit this month" else "🩺 1 kali berobat bulan ini",
                            fontSize = 11.5.sp,
                            color = Color(0xFF0369A1),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (student.allergies.isNotBlank() && !student.allergies.equals("Tidak ada", ignoreCase = true) && student.allergies != "-") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Allergies: ${student.allergies}" else "Alergi: ${student.allergies}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Kartu 1: Identitas Kependudukan & Akademik Lengkap
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    SelectionContainer {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "Civil & School Identity" else "Identitas Kependudukan & Sekolah",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                            ProfileDetailRow(
                                label = "NIK",
                                value = cleanNik.ifBlank { "-" },
                                isMonospace = true,
                                copyContentDescPrefix = if (isEn) "Copy" else "Salin",
                                onCopy = if (cleanNik.isNotBlank()) {
                                    { copyValue("NIK", cleanNik) }
                                } else null
                            )
                            ProfileDetailRow(
                                label = if (isEn) "KK No." else "No. KK",
                                value = cleanNoKk.ifBlank { "-" },
                                isMonospace = true,
                                copyContentDescPrefix = if (isEn) "Copy" else "Salin",
                                onCopy = if (cleanNoKk.isNotBlank()) {
                                    { copyValue(if (isEn) "KK No." else "No. KK", cleanNoKk) }
                                } else null
                            )
                            ProfileDetailRow(
                                label = "NIS / NISN",
                                value = student.nis.ifBlank { "-" },
                                isMonospace = true,
                                copyContentDescPrefix = if (isEn) "Copy" else "Salin",
                                onCopy = if (student.nis.isNotBlank()) {
                                    { copyValue("NIS/NISN", student.nis) }
                                } else null
                            )
                            ProfileDetailRow(
                                label = if (isEn) "Level / Class" else "Jenjang / Kelas",
                                value = "${resolvedJenjang.ifBlank { "-" }} • ${student.classGrade.ifBlank { "-" }}"
                            )
                            ProfileDetailRow(
                                label = if (isEn) "Gender" else "Jenis Kelamin",
                                value = if (isEn) {
                                    if (isMale) "Male (Putra)" else "Female (Putri)"
                                } else {
                                    if (isMale) "Laki-laki (Putra)" else "Perempuan (Putri)"
                                }
                            )
                            val ttlFormatted = listOf(student.birthPlace, student.birthDate)
                                .filter { it.isNotBlank() }
                                .joinToString(", ")
                                .ifBlank { "-" }
                            ProfileDetailRow(
                                label = if (isEn) "Birth Place, Date" else "Tempat, Tgl Lahir",
                                value = ttlFormatted
                            )
                            ProfileDetailRow(
                                label = if (isEn) "Age" else "Usia",
                                value = if (isEn) "${student.age} Years" else "${student.age} Tahun"
                            )
                            ProfileDetailRow(
                                label = if (isEn) "Biological Mother" else "Nama Ibu Kandung",
                                value = student.motherName.ifBlank { "-" }
                            )
                            ProfileDetailRow(
                                label = if (isEn) "Full Address" else "Alamat Lengkap",
                                value = student.address.ifBlank { "-" }
                            )
                            ProfileDetailRow(
                                label = if (isEn) "District" else "Kecamatan",
                                value = student.district.ifBlank { "-" }
                            )
                        }
                    }
                }

                // Kartu 2: Profil Asrama, Kontak & Kesehatan
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Asrama, Contact & Health" else "Asrama, Kontak & Kesehatan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                        ProfileDetailRow(
                            label = "Asrama",
                            value = student.dormitory.ifBlank { "-" }
                        )
                        ProfileDetailRow(
                            label = if (isEn) "Room" else "Kamar",
                            value = student.roomNumber.ifBlank { "-" }
                        )
                        ProfileDetailRow(
                            label = if (isEn) "Guardian / Mother Contact" else "Kontak Wali / Ibu",
                            value = student.emergencyContact.ifBlank { "-" }
                        )
                        ProfileDetailRow(
                            label = if (isEn) "Blood Type" else "Golongan Darah",
                            value = student.bloodType.ifBlank { "-" }
                        )
                        ProfileDetailRow(
                            label = if (isEn) "Allergy History" else "Riwayat Alergi",
                            value = student.allergies.ifBlank {
                                if (isEn) "None" else "Tidak ada"
                            }.let { raw ->
                                if (isEn && raw.equals("Tidak ada", ignoreCase = true)) "None" else raw
                            }
                        )
                        val cleanNotes = student.specialNotes.trim()
                        if (cleanNotes.isNotBlank() && !cleanNotes.equals("Alamat Asal: ${student.address}", ignoreCase = true)) {
                            ProfileDetailRow(
                                label = if (isEn) "Special Notes" else "Catatan Khusus",
                                value = cleanNotes
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // Riwayat Mengaji Melekat pada Siswa
                val emeraldPrimary = Color(0xFF047857)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = emeraldPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) {
                                "Mengaji History (${orderedMengajiRecords.size}):"
                            } else {
                                "Riwayat Mengaji (${orderedMengajiRecords.size} Setoran):"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (onInputMengaji != null) {
                            OutlinedButton(
                                onClick = { onInputMengaji(student) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("btn_student_input_mengaji")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = emeraldPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(if (isEn) "Input" else "Input", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = emeraldPrimary)
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                if (onPrintKartuMengaji != null) {
                                    onPrintKartuMengaji(student)
                                } else {
                                    showInlineKartuMengaji = true
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("btn_student_kartu_mengaji")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = emeraldPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(if (isEn) "Card" else "Kartu", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = emeraldPrimary)
                        }
                    }
                }

                val latestMengaji = orderedMengajiRecords.firstOrNull()
                if (latestMengaji == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isEn) {
                                "No mengaji records for this student yet."
                            } else {
                                "Belum ada riwayat setoran mengaji untuk siswa ini."
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val lastFluent = MengajiHelper.isFluent(latestMengaji.fluencyStatus)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (lastFluent) Color(0xFFD1FAE5) else Color(0xFFFEF3C7))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("badge_student_latest_mengaji")
                    ) {
                        Text(
                            text = if (isEn) {
                                "Latest Position: ${MengajiHelper.formatCompactPosition(latestMengaji)} (${MengajiHelper.fluencyLabel(latestMengaji.fluencyStatus, true)})"
                            } else {
                                "Bacaan Terakhir: ${MengajiHelper.formatCompactPosition(latestMengaji)} (${MengajiHelper.fluencyLabel(latestMengaji.fluencyStatus, false)})"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (lastFluent) Color(0xFF065F46) else Color(0xFF92400E)
                        )
                    }

                    val displayedMengaji = if (showAllMengajiHistory) {
                        orderedMengajiRecords
                    } else {
                        orderedMengajiRecords.take(5)
                    }
                    displayedMengaji.forEach { rec ->
                        val isFluent = MengajiHelper.isFluent(rec.fluencyStatus)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_mengaji_item_${rec.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = MengajiHelper.formatCompactPosition(rec),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${DateUtils.formatShortDate(rec.sessionDate, s.language)} • Wali: ${rec.waliName}",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (rec.notes.isNotBlank()) {
                                        Text(
                                            text = rec.notes,
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isFluent) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = MengajiHelper.fluencyLabel(rec.fluencyStatus, isEn),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFluent) Color(0xFF15803D) else Color(0xFFB91C1C)
                                    )
                                }
                            }
                        }
                    }

                    if (orderedMengajiRecords.size > 5) {
                        TextButton(
                            onClick = { showAllMengajiHistory = !showAllMengajiHistory },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .testTag("btn_student_toggle_all_mengaji")
                        ) {
                            Text(
                                text = if (showAllMengajiHistory) {
                                    if (isEn) "Show less" else "Ringkas riwayat mengaji"
                                } else {
                                    if (isEn) {
                                        "Show all ${orderedMengajiRecords.size} sessions"
                                    } else {
                                        "Lihat semua (${orderedMengajiRecords.size} setoran)"
                                    }
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = emeraldPrimary
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // Riwayat Kunjungan Pengobatan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) {
                                "Medical History (${studentRecords.size} Visits):"
                            } else {
                                "Riwayat Berobat (${studentRecords.size} Kali):"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showPrintPdfDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Print PDF" else "Cetak PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (studentRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isEn) {
                                "No treatment history for this student yet."
                            } else {
                                "Belum ada riwayat pengobatan untuk siswa ini."
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    studentRecords.forEach { record ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FaskesBadge(faskes = record.faskes)
                                    val dateStr = record.treatmentDate?.let { DateUtils.formatShortDate(it) }
                                        ?: DateUtils.formatShortDate(record.planDate)
                                    Text(
                                        text = dateStr,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = if (isEn) "Complaint: ${record.complaint}" else "Keluhan: ${record.complaint}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                if (record.diagnosis.isNotBlank()) {
                                    Text(
                                        text = if (isEn) "Diagnosis: ${record.diagnosis}" else "Diagnosa: ${record.diagnosis}",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (record.prescription.isNotBlank()) {
                                    Text(
                                        text = if (isEn) "Medicine: ${record.prescription}" else "Obat: ${record.prescription}",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                ControlBadge(
                                    needsControl = record.needsControl,
                                    controlDateFormatted = record.controlDate?.let { DateUtils.formatShortDate(it) },
                                    isCompleted = record.isControlCompleted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = { showCardGeneratorForStudent = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_open_student_card_qr")
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(if (isEn) "Card & QR" else "Kartu & QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    if (onEditStudent != null) {
                        OutlinedButton(
                            onClick = { onEditStudent(student) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_edit_student_profile")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEn) "Edit Profile" else "Edit Profil", fontSize = 12.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(s.nav.close, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showPrintPdfDialog) {
        PrintPdfDialog(
            student = student,
            allRecords = studentRecords,
            waliName = waliName,
            schoolProfile = schoolProfile,
            onDismiss = { showPrintPdfDialog = false }
        )
    }

    if (showCardGeneratorForStudent) {
        StudentCardGeneratorSheet(
            students = listOf(student),
            schoolProfile = schoolProfile,
            initialStudent = student,
            onDismiss = { showCardGeneratorForStudent = false }
        )
    }

    if (showInlineKartuMengaji) {
        KartuMengajiSheet(
            students = listOf(student),
            mengajiRecords = orderedMengajiRecords,
            schoolProfile = schoolProfile,
            waliName = waliName,
            initialStudent = student,
            onOpenInputForStudent = { st ->
                showInlineKartuMengaji = false
                onInputMengaji?.invoke(st)
            },
            onDismiss = { showInlineKartuMengaji = false }
        )
    }
}

@Composable
private fun StudentChipBadge(
    text: String,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    isMonospace: Boolean = false,
    copyContentDescPrefix: String = "Salin",
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(114.dp)
        )
        Text(
            text = ":",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isMonospace && value != "-") FontFamily.Monospace else FontFamily.Default,
            color = if (value == "-") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (onCopy != null) {
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "$copyContentDescPrefix $label",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
