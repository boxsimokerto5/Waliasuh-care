package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.MedicalRecord
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportDialog(
    records: List<MedicalRecord>,
    students: List<ChildStudent>,
    waliName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentCal = remember { Calendar.getInstance() }
    var selectedMonth by remember { mutableIntStateOf(currentCal.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCal.get(Calendar.YEAR)) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val monthNames = remember {
        listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
    }

    val periodLabel = "${monthNames[selectedMonth]} $selectedYear"

    // Filter records for the selected month and year
    val monthRecords = remember(records, selectedMonth, selectedYear) {
        records.filter { record ->
            val timestamp = record.treatmentDate ?: record.planDate
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            cal.get(Calendar.MONTH) == selectedMonth && cal.get(Calendar.YEAR) == selectedYear
        }.sortedByDescending { it.treatmentDate ?: it.planDate }
    }

    // Key Statistics
    val totalStudents = students.size
    val totalVisits = monthRecords.size
    val uniqueSickStudents = monthRecords.map { it.studentId }.distinct().size
    val maleCount = monthRecords.count { it.studentDormitory.contains("Putra", ignoreCase = true) }
    val femaleCount = monthRecords.count { it.studentDormitory.contains("Putri", ignoreCase = true) }
    val tuntasCount = monthRecords.count { !it.needsControl }
    val kontrolCount = monthRecords.count { it.needsControl }

    val watesCount = monthRecords.count { it.faskes.contains("Wates", ignoreCase = true) }
    val klatenCount = monthRecords.count { it.faskes.contains("Plosoklaten", ignoreCase = true) || it.faskes.contains("Klaten", ignoreCase = true) }
    val otherFaskesCount = totalVisits - watesCount - klatenCount

    val shiftPagiCount = monthRecords.count { it.escortName.contains("Pagi", ignoreCase = true) }
    val shiftSoreCount = monthRecords.count { it.escortName.contains("Sore", ignoreCase = true) }
    val shiftMalamCount = monthRecords.count { it.escortName.contains("Malam", ignoreCase = true) }

    val morbidityPct = if (totalStudents > 0) (uniqueSickStudents.toFloat() / totalStudents * 100).toInt() else 0

    // Top 5 Diagnoses
    val topDiagnoses = remember(monthRecords) {
        val countMap = mutableMapOf<String, Int>()
        monthRecords.forEach { r ->
            val raw = if (r.diagnosis.isNotBlank() && r.diagnosis != "-") r.diagnosis else r.complaint
            val clean = raw.split(",", ";", "/").firstOrNull()?.trim() ?: raw.trim()
            val key = if (clean.isBlank()) "Pemeriksaan Umum" else clean
            countMap[key] = (countMap[key] ?: 0) + 1
        }
        countMap.entries.sortedByDescending { it.value }.take(5)
    }

    // Text for WhatsApp Share
    val whatsappReportText = remember(monthRecords, periodLabel, waliName, totalStudents) {
        buildString {
            appendLine("📋 *LAPORAN REKAPITULASI KESEHATAN BULANAN*")
            appendLine("*Sekolah Rakyat SRT 1 Kabupaten Kediri*")
            appendLine("*Periode:* $periodLabel")
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📊 *RINGKASAN STATISTIK:*")
            appendLine("• Total Kunjungan Berobat: $totalVisits Kali")
            appendLine("• Jumlah Siswa Sakit: $uniqueSickStudents Anak ($morbidityPct% dari $totalStudents anak asuh)")
            appendLine("• Distribusi: Asrama Putra: $maleCount | Asrama Putri: $femaleCount")
            appendLine("• Status: Sembuh/Tuntas: $tuntasCount | Perlu Kontrol: $kontrolCount")
            appendLine("")
            appendLine("🏥 *FASKES RUJUKAN:*")
            appendLine("• Puskesmas Wates: $watesCount rujukan")
            appendLine("• Puskesmas Plosoklaten: $klatenCount rujukan")
            if (otherFaskesCount > 0) {
                appendLine("• Faskes Lainnya: $otherFaskesCount rujukan")
            }
            appendLine("")
            appendLine("🚗 *PETUGAS PENGANTAR (SHIFT):*")
            appendLine("• Shift Pagi: $shiftPagiCount | Shift Sore: $shiftSoreCount | Shift Malam: $shiftMalamCount")
            appendLine("")
            if (topDiagnoses.isNotEmpty()) {
                appendLine("🩺 *DIAGNOSA TERBANYAK:*")
                topDiagnoses.forEachIndexed { i, entry ->
                    appendLine("${i + 1}. ${entry.key}: ${entry.value} kasus")
                }
                appendLine("")
            }
            if (monthRecords.isNotEmpty()) {
                appendLine("📝 *DAFTAR ANAK SAKIT:*")
                monthRecords.take(10).forEachIndexed { i, r ->
                    val tgl = r.treatmentDate?.let { DateUtils.formatShortDate(it) } ?: DateUtils.formatShortDate(r.planDate)
                    val diag = if (r.diagnosis.isNotBlank() && r.diagnosis != "-") r.diagnosis else r.complaint
                    appendLine("${i + 1}. $tgl - ${r.studentName} (${r.studentDormitory}): $diag")
                }
                if (monthRecords.size > 10) {
                    appendLine("... dan ${monthRecords.size - 10} kunjungan lainnya tercatat di aplikasi.")
                }
                appendLine("")
            }
            appendLine("👤 *Penanggung Jawab / Wali Asuh:* $waliName")
            appendLine("🕒 *Tanggal Cetak:* ${DateUtils.formatFullDate(System.currentTimeMillis())}")
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Laporan Bulanan Kesehatan",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Sekolah Rakyat SRT 1 Kabupaten Kediri",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("btn_close_monthly_report")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Month / Year Selector Bar
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedMonth == 0) {
                                    selectedMonth = 11
                                    selectedYear--
                                } else {
                                    selectedMonth--
                                }
                            },
                            modifier = Modifier.size(32.dp).testTag("btn_prev_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Bulan Sebelumnya",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = periodLabel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${monthRecords.size} catatan kunjungan berobat",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                if (selectedMonth == 11) {
                                    selectedMonth = 0
                                    selectedYear++
                                } else {
                                    selectedMonth++
                                }
                            },
                            modifier = Modifier.size(32.dp).testTag("btn_next_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Bulan Berikutnya",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Primary Tab Row
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Ringkasan", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Daftar Kasus (${monthRecords.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Format WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                // Tab Content
                Box(modifier = Modifier.weight(1f, fill = false).heightIn(min = 200.dp, max = 400.dp)) {
                    when (selectedTab) {
                        0 -> {
                            // Ringkasan & Statistik
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 4 Key Metric Cards
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MetricBox(
                                        title = "Total Berobat",
                                        value = "$totalVisits Kali",
                                        subtitle = "Selama $periodLabel",
                                        color = WaliPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    MetricBox(
                                        title = "Anak Sakit",
                                        value = "$uniqueSickStudents Anak",
                                        subtitle = "$morbidityPct% dari $totalStudents siswa",
                                        color = Color(0xFFEA580C),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MetricBox(
                                        title = "Asrama",
                                        value = "$maleCount Pa / $femaleCount Pi",
                                        subtitle = "Putra vs Putri",
                                        color = Color(0xFF2563EB),
                                        modifier = Modifier.weight(1f)
                                    )
                                    MetricBox(
                                        title = "Status Kasus",
                                        value = "$tuntasCount Sembuh",
                                        subtitle = "$kontrolCount wajib kontrol",
                                        color = Color(0xFF16A34A),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Top 5 Diagnoses
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "🩺 Diagnosa / Keluhan Terbanyak",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        if (topDiagnoses.isEmpty()) {
                                            Text(
                                                text = "Tidak ada keluhan sakit tercatat di bulan ini.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            topDiagnoses.forEach { entry ->
                                                val pct = if (totalVisits > 0) (entry.value.toFloat() / totalVisits) else 0f
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(text = entry.key, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                                                        Text(text = "${entry.value} kasus (${(pct * 100).toInt()}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    LinearProgressIndicator(
                                                        progress = { pct },
                                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                                        color = MaterialTheme.colorScheme.primary,
                                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Faskes & Shift Breakdown
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "🏥 Distribusi Faskes & Petugas Pengantar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "• Faskes: Pusk. Wates ($watesCount) • Pusk. Plosoklaten ($klatenCount)${if (otherFaskesCount > 0) " • Lainnya ($otherFaskesCount)" else ""}",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "• Shift Pengantar: Pagi ($shiftPagiCount) • Sore ($shiftSoreCount) • Malam ($shiftMalamCount)",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Daftar Kasus
                            if (monthRecords.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Kondisi Nihil: Tidak ada anak asuh yang berobat pada bulan ini.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(monthRecords, key = { it.id }) { r ->
                                        val dateStr = r.treatmentDate?.let { DateUtils.formatFullDate(it) }
                                            ?: DateUtils.formatFullDate(r.planDate)
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(text = r.studentName, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                                    Text(
                                                        text = if (r.needsControl) "Perlu Kontrol" else "Tuntas",
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (r.needsControl) MaterialTheme.colorScheme.error else Color(0xFF16A34A)
                                                    )
                                                }
                                                Text(
                                                    text = "$dateStr • ${r.studentDormitory} • ${r.faskes}",
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "Keluhan: ${r.complaint}${if (r.diagnosis.isNotBlank() && r.diagnosis != "-") " | Diagnosa: ${r.diagnosis}" else ""}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                if (r.prescription.isNotBlank() && r.prescription != "-") {
                                                    Text(
                                                        text = "Obat: ${r.prescription}",
                                                        fontSize = 10.5.sp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                if (r.escortName.isNotBlank()) {
                                                    Text(
                                                        text = "Pengantar: ${r.escortName}",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // Format WhatsApp
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = whatsappReportText,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Laporan Bulanan", whatsappReportText))
                                            Toast.makeText(context, "Laporan disalin ke papan klip!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Salin Teks", fontSize = 11.5.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, whatsappReportText)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan ke WhatsApp"))
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Kirim WA", fontSize = 11.5.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Action Buttons Bottom
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                val pdfFile = PdfReportGenerator.generateMonthlyAggregatePdf(
                                    context = context,
                                    records = monthRecords,
                                    totalStudentsCount = totalStudents,
                                    monthYearLabel = periodLabel,
                                    waliName = waliName
                                )
                                if (pdfFile != null) {
                                    PdfReportGenerator.openPdfFile(context, pdfFile)
                                } else {
                                    Toast.makeText(context, "Gagal membuat dokumen PDF", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_export_monthly_pdf")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cetak PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, whatsappReportText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan Bulanan"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_share_monthly_wa")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bagikan WA", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_close_monthly_report_bottom")
                    ) {
                        Text("Tutup", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = title, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = subtitle, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
