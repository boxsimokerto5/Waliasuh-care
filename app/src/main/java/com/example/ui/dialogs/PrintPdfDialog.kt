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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintPdfDialog(
    student: ChildStudent,
    allRecords: List<MedicalRecord>,
    waliName: String = "Wali Asuh",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
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

    val monthNames = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
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
        1 -> "Tahun $selectedYear"
        else -> "Seluruh Riwayat Screening"
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Column {
                            Text(
                                text = "Cetak Rekap Screening PDF",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Dokumen resmi Sekolah Rakyat SRT 1 Kediri",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
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
                                text = "NIS: ${student.nis} • Kelas: ${student.classGrade}",
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
                    text = "Pilih Periode Laporan:",
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
                        label = { Text("Rekap Bulan", fontSize = 11.5.sp) },
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
                        label = { Text("Rekap Tahun", fontSize = 11.5.sp) },
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
                        label = { Text("Semua", fontSize = 11.5.sp) },
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
                            Text(text = "Tahun Laporan: $selectedYear", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = expandedYearDropdown,
                            onDismissRequest = { expandedYearDropdown = false }
                        ) {
                            availableYears.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text("Tahun $year", fontSize = 12.sp) },
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
                                text = "Pratinjau Rekap: $periodLabel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (filteredRecords.size >= 2) Color(0xFFB91C1C) else Color(0xFF15803D)
                            )
                        }

                        Text(
                            text = if (filteredRecords.isNotEmpty()) {
                                "Ditemukan ${filteredRecords.size} catatan screening / pengobatan yang akan dimuat ke dalam lembar PDF A4."
                            } else {
                                "Tidak ada catatan berobat pada periode ini. Lembar PDF akan mencantumkan status anak asuh 'Kondisi Sehat'."
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (filteredRecords.isNotEmpty()) {
                            Text(
                                text = "Diagnosa: " + filteredRecords.joinToString("; ") { it.diagnosis.ifBlank { it.complaint } },
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
                                waliName = waliName
                            )
                            if (pdfFile != null) {
                                PdfReportGenerator.sharePdfFile(context, pdfFile, student.name)
                            } else {
                                Toast.makeText(context, "Gagal membuat dokumen PDF.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan PDF", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
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
                                waliName = waliName
                            )
                            if (pdfFile != null) {
                                PdfReportGenerator.openPdfFile(context, pdfFile)
                            } else {
                                Toast.makeText(context, "Gagal membuat dokumen PDF.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_print_pdf"),
                        colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Cetak / Buka PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
