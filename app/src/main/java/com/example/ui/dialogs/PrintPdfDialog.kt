package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolProfile
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import com.example.util.TopOnAdManager
import com.example.util.i18n.LocalAppStrings
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintPdfDialog(
    student: ChildStudent,
    allRecords: List<MedicalRecord>,
    waliName: String = "Wali Asuh",
    schoolProfile: SchoolProfile = SchoolProfile(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentCal = remember { Calendar.getInstance() }
    val defaultMonth = currentCal.get(Calendar.MONTH) // 0-indexed
    val defaultYear = currentCal.get(Calendar.YEAR)

    // Mode filter: 0 = Rekap Bulanan, 1 = Rekap Tahunan, 2 = Semua Riwayat
    var selectedFilterMode by remember { mutableIntStateOf(0) }

    var selectedMonth by remember { mutableIntStateOf(defaultMonth) }
    var selectedYear by remember { mutableIntStateOf(defaultYear) }

    var expandedMonthDropdown by remember { mutableStateOf(false) }
    var expandedYearDropdown by remember { mutableStateOf(false) }
    var pendingPdfAction by remember { mutableStateOf<String?>(null) } // "OPEN" or "SHARE"

    val monthNames = remember(s.language) {
        if (isEn) {
            listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
        } else {
            listOf(
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
            )
        }
    }
    val availableYears = listOf(defaultYear, defaultYear - 1, defaultYear - 2)

    // Filter rekam medis sesuai pilihan periode
    val studentRecords = allRecords.filter { it.studentId == student.id }
    val calRecord = Calendar.getInstance()

    val filteredRecords = when (selectedFilterMode) {
        0 -> { // Bulanan
            studentRecords.filter { record ->
                val time = record.treatmentDate ?: record.planDate
                calRecord.timeInMillis = time
                calRecord.get(Calendar.MONTH) == selectedMonth && calRecord.get(Calendar.YEAR) == selectedYear
            }.sortedBy { it.treatmentDate ?: it.planDate }
        }
        1 -> { // Tahunan
            studentRecords.filter { record ->
                val time = record.treatmentDate ?: record.planDate
                calRecord.timeInMillis = time
                calRecord.get(Calendar.YEAR) == selectedYear
            }.sortedBy { it.treatmentDate ?: it.planDate }
        }
        else -> { // Semua
            studentRecords.sortedBy { it.treatmentDate ?: it.planDate }
        }
    }

    val periodLabel = when (selectedFilterMode) {
        0 -> "${monthNames[selectedMonth]} $selectedYear"
        1 -> if (isEn) "Year $selectedYear" else "Tahun $selectedYear"
        else -> if (isEn) "All Screening History" else "Seluruh Riwayat Screening"
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
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Print Screening PDF Recap" else "Cetak Rekap Screening PDF",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isEn) {
                                    "Official document • ${schoolProfile.schoolName.ifBlank { schoolProfile.shortName }}"
                                } else {
                                    "Dokumen resmi ${schoolProfile.schoolName.ifBlank { schoolProfile.shortName }}"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = s.close)
                    }
                }

                // Identitas Anak Asuh
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StudentAvatar(
                            name = student.name,
                            gender = student.gender,
                            size = 42.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = student.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isEn) "NIS: ${student.nis} • Grade: ${student.classGrade}" else "NIS: ${student.nis} • Kelas: ${student.classGrade}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Asrama: ${student.dormitory}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Pilihan Periode Cetak (Bulan dan Tahun)
                Text(
                    text = if (isEn) "Select Report Period:" else "Pilih Periode Laporan:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilterMode == 0,
                        onClick = { selectedFilterMode = 0 },
                        label = { Text(if (isEn) "Monthly" else "Rekap Bulan", fontSize = 11.5.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WaliPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = WaliPrimary
                        )
                    )

                    FilterChip(
                        selected = selectedFilterMode == 1,
                        onClick = { selectedFilterMode = 1 },
                        label = { Text(if (isEn) "Yearly" else "Rekap Tahun", fontSize = 11.5.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WaliPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = WaliPrimary
                        )
                    )

                    FilterChip(
                        selected = selectedFilterMode == 2,
                        onClick = { selectedFilterMode = 2 },
                        label = { Text(if (isEn) "All" else "Semua", fontSize = 11.5.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.History, null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WaliPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = WaliPrimary
                        )
                    )
                }

                // Dropdown Pemilih Bulan & Tahun
                if (selectedFilterMode == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dropdown Bulan
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { expandedMonthDropdown = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = monthNames[selectedMonth], fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedMonthDropdown,
                                onDismissRequest = { expandedMonthDropdown = false }
                            ) {
                                monthNames.forEachIndexed { idx, name ->
                                    DropdownMenuItem(
                                        text = { Text(name, fontSize = 12.sp) },
                                        onClick = {
                                            selectedMonth = idx
                                            expandedMonthDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Dropdown Tahun
                        Box(modifier = Modifier.weight(0.8f)) {
                            OutlinedButton(
                                onClick = { expandedYearDropdown = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "$selectedYear", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedYearDropdown,
                                onDismissRequest = { expandedYearDropdown = false }
                            ) {
                                availableYears.forEach { year ->
                                    DropdownMenuItem(
                                        text = { Text("$year", fontSize = 12.sp) },
                                        onClick = {
                                            selectedYear = year
                                            expandedYearDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else if (selectedFilterMode == 1) {
                    // Dropdown Tahun Saja
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedYearDropdown = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isEn) "Report Year: $selectedYear" else "Tahun Laporan: $selectedYear",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = expandedYearDropdown,
                            onDismissRequest = { expandedYearDropdown = false }
                        ) {
                            availableYears.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(if (isEn) "Year $year" else "Tahun $year", fontSize = 12.sp) },
                                    onClick = {
                                        selectedYear = year
                                        expandedYearDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Ringkasan Pratinjau Dokumen yang Akan Dicetak
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (filteredRecords.size >= 2) Color(0xFFFEE2E2) else Color(0xFFF0FDF4)
                    ),
                    border = BorderStroke(1.dp, if (filteredRecords.size >= 2) Color(0xFFFECACA) else Color(0xFFBBF7D0)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (filteredRecords.size >= 2) Color(0xFFDC2626) else Color(0xFF16A34A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Recap Preview: $periodLabel" else "Pratinjau Rekap: $periodLabel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (filteredRecords.size >= 2) Color(0xFFB91C1C) else Color(0xFF15803D)
                            )
                        }

                        Text(
                            text = if (filteredRecords.isNotEmpty()) {
                                if (isEn) {
                                    "Found ${filteredRecords.size} screening / treatment records to include on the A4 PDF sheet."
                                } else {
                                    "Ditemukan ${filteredRecords.size} catatan screening / pengobatan yang akan dimuat ke dalam lembar PDF A4."
                                }
                            } else {
                                if (isEn) {
                                    "No treatment records for this period. The PDF sheet will indicate 'Healthy Condition'."
                                } else {
                                    "Tidak ada catatan berobat pada periode ini. Lembar PDF akan mencantumkan status anak asuh 'Kondisi Sehat'."
                                }
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (filteredRecords.isNotEmpty()) {
                            Text(
                                text = (if (isEn) "Diagnosis: " else "Diagnosa: ") +
                                    filteredRecords.joinToString("; ") { it.diagnosis.ifBlank { it.complaint } },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Tombol Aksi: Cetak / Buka PDF & Bagikan File PDF
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            pendingPdfAction = "SHARE"
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Share PDF" else "Bagikan PDF", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            pendingPdfAction = "OPEN"
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_print_pdf"),
                        colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(if (isEn) "Print / Open PDF" else "Cetak / Buka PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                RewardAdDeclarationHint(
                    text = if (isEn) {
                        "Watch a short video ad to print or share the PDF Report"
                    } else {
                        "Tonton iklan video singkat untuk mencetak atau membagikan Laporan PDF"
                    },
                    accentColor = WaliPrimary
                )
            }
        }
    }

    pendingPdfAction?.let { actionType ->
        RewardAdDeclarationDialog(
            title = if (actionType == "OPEN") {
                if (isEn) "Print / Open PDF Report" else "Cetak / Buka Laporan PDF"
            } else {
                if (isEn) "Share PDF Report" else "Bagikan Laporan PDF"
            },
            actionDescription = if (actionType == "OPEN") {
                if (isEn) {
                    "create and open Medical Record PDF Report (${student.name})"
                } else {
                    "membuat dan membuka Laporan PDF Rekam Medis (${student.name})"
                }
            } else {
                if (isEn) {
                    "create and share Medical Record PDF Report (${student.name})"
                } else {
                    "membuat dan membagikan Laporan PDF Rekam Medis (${student.name})"
                }
            },
            confirmButtonText = if (isEn) "Watch & Download PDF" else "Tonton & Unduh PDF",
            onConfirmWatchAd = {
                pendingPdfAction = null
                TopOnAdManager.showRewardAdForPdfReport(context) {
                    val pdfFile = PdfReportGenerator.generateHealthReportPdf(
                        context = context,
                        student = student,
                        records = filteredRecords,
                        periodType = when (selectedFilterMode) {
                            0 -> PdfReportGenerator.ReportPeriodType.MONTHLY
                            1 -> PdfReportGenerator.ReportPeriodType.YEARLY
                            else -> PdfReportGenerator.ReportPeriodType.ALL
                        },
                        periodLabel = periodLabel,
                        waliName = waliName,
                        schoolProfile = schoolProfile
                    )
                    if (pdfFile != null) {
                        if (actionType == "OPEN") {
                            PdfReportGenerator.openPdfFile(context, pdfFile)
                        } else {
                            PdfReportGenerator.sharePdfFile(
                                context = context,
                                pdfFile = pdfFile,
                                studentName = student.name,
                                schoolName = schoolProfile.schoolName
                            )
                        }
                    } else {
                        Toast.makeText(
                            context,
                            if (isEn) "Failed to create PDF document." else "Gagal membuat dokumen PDF.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            onDismiss = { pendingPdfAction = null }
        )
    }
}
