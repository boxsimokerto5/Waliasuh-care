package com.example.ui.dialogs

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.SchoolProfile
import com.example.data.model.SholatHelper
import com.example.data.model.SholatRecord
import com.example.data.model.SholatStatus
import com.example.data.model.SholatWaktu
import com.example.util.PdfReportGenerator
import java.io.File

internal fun sholatStatusColors(status: SholatStatus): Pair<Color, Color> = when (status) {
    SholatStatus.JAMAAH -> Color(0xFFDCFCE7) to Color(0xFF15803D)
    SholatStatus.MUNFARID -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
    SholatStatus.UDZUR_HAID -> Color(0xFFFCE7F3) to Color(0xFFBE185D)
    SholatStatus.SAKIT -> Color(0xFFFEF3C7) to Color(0xFFB45309)
    SholatStatus.IZIN -> Color(0xFFEDE9FE) to Color(0xFF6D28D9)
    SholatStatus.ALFA -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
}

@Composable
internal fun SholatMonitoringTabContent(
    students: List<ChildStudent>,
    records: List<SholatRecord>,
    selectedMonthKey: String,
    onChangeMonthKey: (String) -> Unit,
    isEn: Boolean,
    modifier: Modifier = Modifier
) {
    val scopedStudentIds = remember(students) { students.map { it.id }.toSet() }
    val monthRecords = remember(records, selectedMonthKey, scopedStudentIds, students) {
        records.filter { rec ->
            rec.dateKey.startsWith(selectedMonthKey) &&
                (rec.studentId in scopedStudentIds || students.any { SholatHelper.matchesStudent(rec, it) })
        }
    }
    val waktuSummaries = remember(monthRecords, students.size) {
        SholatHelper.computeWaktuSummaries(monthRecords, students.size)
    }
    val studentAnalytics = remember(students, monthRecords, selectedMonthKey) {
        SholatHelper.computeStudentAnalytics(students, monthRecords, selectedMonthKey)
    }
    val leaderboard = remember(studentAnalytics) {
        studentAnalytics
            .filter { it.totalLogged > 0 }
            .sortedWith(
                compareByDescending<com.example.data.model.SholatStudentAnalytics> { it.jamaahRatePercent }
                    .thenByDescending { it.subuhJamaahCount }
                    .thenByDescending { it.jamaahCount }
            )
            .take(8)
    }
    val needGuidance = remember(studentAnalytics) {
        studentAnalytics
            .filter { it.alfaCount > 0 || it.masbukCount >= 2 }
            .sortedWith(
                compareByDescending<com.example.data.model.SholatStudentAnalytics> { it.alfaCount }
                    .thenByDescending { it.masbukCount }
            )
    }

    val totalJamaah = monthRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.JAMAAH }
    val totalUdzur = monthRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.UDZUR_HAID }
    val totalAlfa = monthRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.ALFA }
    val wajibDenom = (monthRecords.size - totalUdzur).coerceAtLeast(0)
    val overallPct = if (wajibDenom > 0) ((totalJamaah * 100f) / wajibDenom).toInt().coerceIn(0, 100) else 0

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("list_sholat_monitoring"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item(key = "month_nav") {
            SholatMonthNavigator(
                monthKey = selectedMonthKey,
                onChangeMonthKey = onChangeMonthKey,
                isEn = isEn
            )
        }

        item(key = "overview_kpis") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SholatKpiBox(
                    label = if (isEn) "Congregation" else "Jamaah %",
                    value = "$overallPct%",
                    sub = "$totalJamaah / ${wajibDenom.coerceAtLeast(monthRecords.size)}",
                    bgColor = Color(0xFFECFDF5),
                    accentColor = Color(0xFF047857),
                    modifier = Modifier.weight(1f)
                )
                SholatKpiBox(
                    label = if (isEn) "Total Logs" else "Total Catatan",
                    value = "${monthRecords.size}",
                    sub = if (isEn) "${students.size} students" else "${students.size} siswa",
                    bgColor = Color(0xFFF0F9FF),
                    accentColor = Color(0xFF0369A1),
                    modifier = Modifier.weight(1f)
                )
                SholatKpiBox(
                    label = "Alfa",
                    value = "$totalAlfa",
                    sub = if (totalAlfa > 0) {
                        if (isEn) "Need coaching" else "Perlu pembinaan"
                    } else {
                        if (isEn) "Zero absent" else "Nihil alfa"
                    },
                    bgColor = if (totalAlfa > 0) Color(0xFFFEF2F2) else Color(0xFFF8FAFC),
                    accentColor = if (totalAlfa > 0) Color(0xFFB91C1C) else Color(0xFF475569),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item(key = "waktu_breakdown") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isEn) "Congregation Rate per Prayer Time" else "Kedisiplinan Jamaah Per Waktu Sholat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                    waktuSummaries.forEach { ws ->
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ws.waktu.label(isEn),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = if (ws.totalLogged == 0) {
                                        if (isEn) "No logs" else "Belum ada data"
                                    } else {
                                        "${ws.jamaahPercent}% (${ws.jamaahCount}/${ws.totalLogged}) • Alfa ${ws.alfaCount}"
                                    },
                                    fontSize = 11.sp,
                                    color = if (ws.alfaCount > 0) Color(0xFFB91C1C) else Color(0xFF475569)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (ws.jamaahPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF059669),
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }
        }

        item(key = "leaderboard_header") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                Text(
                    text = if (isEn) "Top Steadfast & Fajr Warriors" else "Apresiasi Istiqomah Jamaah & Pejuang Subuh",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
            }
        }

        if (leaderboard.isEmpty()) {
            item(key = "leaderboard_empty") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isEn) {
                            "Record daily prayer attendance first to generate student appreciation rankings."
                        } else {
                            "Mulai catat absen sholat harian untuk melihat peringkat apresiasi anak asuh."
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            itemsIndexed(leaderboard, key = { _, item -> "lb_${item.student.id}" }) { idx, item ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (idx == 0) Color(0xFFFFFBEB) else Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (idx == 0) Color(0xFFFDE68A) else Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (idx < 3) Color(0xFF059669) else Color(0xFFE2E8F0))
                            ) {
                                Text(
                                    text = "#${idx + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (idx < 3) Color.White else Color(0xFF334155)
                                )
                            }
                            Column {
                                Text(
                                    text = item.student.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isEn) {
                                        "Fajr: ${item.subuhJamaahCount}x • Front Row: ${item.safDepanCount}x • ${item.student.classGrade}"
                                    } else {
                                        "Subuh: ${item.subuhJamaahCount}x • Saf Depan: ${item.safDepanCount}x • ${item.student.classGrade}"
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${item.jamaahRatePercent}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }
        }

        if (needGuidance.isNotEmpty()) {
            item(key = "guidance_header") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp))
                    Text(
                        text = if (isEn) "Needs Follow-up (Alfa / Repeated Masbuq)" else "Perlu Pembinaan (Alfa / Masbuk Berulang)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF991B1B)
                    )
                }
            }
            items(needGuidance.take(10), key = { "ng_${it.student.id}" }) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.student.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF7F1D1D))
                            Text(
                                text = "${item.student.classGrade} • ${item.student.dormitory.ifBlank { "-" }}",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Text(
                            text = "Alfa ${item.alfaCount}x • Masbuk ${item.masbukCount}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB91C1C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SholatRecapAndReportTabContent(
    students: List<ChildStudent>,
    records: List<SholatRecord>,
    selectedDateKey: String,
    selectedMonthKey: String,
    selectedWaktu: SholatWaktu,
    onChangeMonthKey: (String) -> Unit,
    schoolProfile: SchoolProfile,
    waliName: String,
    isEn: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isMonthlyMode by remember { mutableStateOf(false) }
    var showStudentPickerForPdf by remember { mutableStateOf(false) }
    var lastGeneratedPdf by remember { mutableStateOf<File?>(null) }

    val orderedStudents = remember(students) { students.sortedBy { it.name.lowercase() } }
    val scopedStudentIds = remember(orderedStudents) { orderedStudents.map { it.id }.toSet() }
    val filteredRecords = remember(records, isMonthlyMode, selectedDateKey, selectedMonthKey, scopedStudentIds, orderedStudents) {
        records.filter { rec ->
            val inPeriod = if (isMonthlyMode) rec.dateKey.startsWith(selectedMonthKey) else rec.dateKey == selectedDateKey
            inPeriod && (rec.studentId in scopedStudentIds || orderedStudents.any { SholatHelper.matchesStudent(rec, it) })
        }
    }
    val dailyMatrix = remember(filteredRecords, selectedDateKey) {
        SholatHelper.buildDailyMatrix(filteredRecords, selectedDateKey)
    }
    val monthlyAnalytics = remember(orderedStudents, filteredRecords, selectedMonthKey) {
        SholatHelper.computeStudentAnalytics(orderedStudents, filteredRecords, selectedMonthKey)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("list_sholat_recap"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item(key = "recap_mode_toggle") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isMonthlyMode,
                    onClick = { isMonthlyMode = false },
                    label = {
                        Text(
                            if (isEn) "Daily Matrix (S·D·A·M·I)" else "Matriks Harian (S·D·A·M·I)",
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD1FAE5),
                        selectedLabelColor = Color(0xFF065F46)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chip_sholat_recap_daily")
                )
                FilterChip(
                    selected = isMonthlyMode,
                    onClick = { isMonthlyMode = true },
                    label = {
                        Text(
                            if (isEn) "Monthly Recap" else "Rekap Bulanan",
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD1FAE5),
                        selectedLabelColor = Color(0xFF065F46)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chip_sholat_recap_monthly")
                )
            }
        }

        if (isMonthlyMode) {
            item(key = "recap_month_nav") {
                SholatMonthNavigator(
                    monthKey = selectedMonthKey,
                    onChangeMonthKey = onChangeMonthKey,
                    isEn = isEn
                )
            }
        }

        item(key = "recap_export_actions") {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isEn) "Export & Share Official Reports" else "Ekspor & Bagikan Laporan Resmi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF065F46)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val periodLbl = if (isMonthlyMode) {
                                    SholatHelper.formatMonthKeyDisplay(selectedMonthKey, isEn)
                                } else {
                                    SholatHelper.formatDateKeyDisplay(selectedDateKey, isEn)
                                }
                                val file = PdfReportGenerator.generateSholatRecapPdf(
                                    context = context,
                                    schoolProfile = schoolProfile,
                                    students = orderedStudents,
                                    sholatRecords = records,
                                    periodLabel = periodLbl,
                                    dateKeyOrMonthKey = if (isMonthlyMode) selectedMonthKey else selectedDateKey,
                                    isMonthly = isMonthlyMode,
                                    waliName = waliName
                                )
                                if (file != null) {
                                    lastGeneratedPdf = file
                                    PdfReportGenerator.shareSholatPdf(
                                        context = context,
                                        pdfFile = file,
                                        subject = if (isEn) "Prayer Attendance Recap $periodLbl" else "Rekap Absen Sholat $periodLbl",
                                        text = if (isEn) "Attached Prayer Attendance PDF Recap for $periodLbl." else "Terlampir PDF Rekap Absensi Sholat 5 Waktu periode $periodLbl."
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isEn) "Failed to generate recap PDF." else "Gagal membuat PDF rekap sholat.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_export_sholat_recap_pdf")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Export Recap PDF" else "PDF Rekap Sekolah",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val waText = SholatHelper.buildWhatsappRecap(
                                    schoolProfile = schoolProfile,
                                    dateKey = selectedDateKey,
                                    selectedWaktu = selectedWaktu,
                                    students = orderedStudents,
                                    records = records,
                                    isEn = isEn
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, waText)
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        intent,
                                        if (isEn) "Share to WhatsApp" else "Bagikan Rekap ke WA"
                                    )
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF16A34A)),
                            modifier = Modifier
                                .weight(0.85f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_share_sholat_wa")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share WA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), maxLines = 1)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showStudentPickerForPdf = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF047857)),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_export_sholat_student_pdf")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Per-Student Monthly PDF" else "PDF Rapor Per Anak",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF047857),
                                maxLines = 1
                            )
                        }

                        if (lastGeneratedPdf != null) {
                            OutlinedButton(
                                onClick = {
                                    lastGeneratedPdf?.let { pdf ->
                                        PdfReportGenerator.printPdfWithSystemPrintManager(
                                            context = context,
                                            pdfFile = pdf,
                                            jobName = "Rekap Sholat"
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_print_sholat_pdf")
                            ) {
                                Icon(Icons.Default.Print, contentDescription = if (isEn) "Print PDF" else "Cetak PDF", tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEn) "Print" else "Cetak", fontSize = 12.sp, color = Color(0xFF0F172A))
                            }
                        }
                    }
                }
            }
        }

        if (!isMonthlyMode) {
            item(key = "daily_matrix_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF065F46))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Student (${SholatHelper.formatDateKeyDisplay(selectedDateKey, true)})" else "Siswa (${SholatHelper.formatDateKeyDisplay(selectedDateKey, false)})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    SholatWaktu.entries.forEach { w ->
                        Text(
                            text = w.shortCode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(28.dp)
                        )
                    }
                }
            }

            items(orderedStudents, key = { "dm_${it.id}" }) { student ->
                val map = dailyMatrix[student.id].orEmpty()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(student.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = "${student.classGrade} • ${student.dormitory.ifBlank { "-" }}",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    SholatWaktu.entries.forEach { w ->
                        val rec = map[w]
                        val st = rec?.let { SholatStatus.fromCode(it.status) }
                        val (bg, fg) = if (st != null) sholatStatusColors(st) else (Color(0xFFF1F5F9) to Color(0xFF94A3B8))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(bg)
                        ) {
                            Text(
                                text = st?.shortCode ?: "·",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = fg
                            )
                        }
                    }
                }
            }
        } else {
            items(monthlyAnalytics, key = { "ma_${it.student.id}" }) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(row.student.name, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = "Jam: ${row.jamaahCount} • Munf: ${row.munfaridCount} • Haid: ${row.udzurHaidCount} • Alfa: ${row.alfaCount}",
                            fontSize = 10.5.sp,
                            color = if (row.alfaCount > 0) Color(0xFFB91C1C) else Color(0xFF475569)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (row.jamaahRatePercent >= 80) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${row.jamaahRatePercent}%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (row.jamaahRatePercent >= 80) Color(0xFF15803D) else Color(0xFF334155)
                        )
                    }
                }
            }
        }
    }

    if (showStudentPickerForPdf) {
        var studentQuery by remember { mutableStateOf("") }
        val pickerStudents = remember(orderedStudents, studentQuery) {
            if (studentQuery.isBlank()) orderedStudents
            else orderedStudents.filter {
                it.name.contains(studentQuery, ignoreCase = true) ||
                    it.classGrade.contains(studentQuery, ignoreCase = true)
            }
        }
        AlertDialog(
            onDismissRequest = { showStudentPickerForPdf = false },
            title = {
                Text(
                    if (isEn) "Select Student for Monthly Report PDF" else "Pilih Siswa untuk Rapor Sholat PDF",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = studentQuery,
                        onValueChange = { studentQuery = it },
                        placeholder = { Text(if (isEn) "Search student..." else "Cari nama siswa...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(pickerStudents, key = { "pick_${it.id}" }) { st ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .clickable {
                                        showStudentPickerForPdf = false
                                        val file = PdfReportGenerator.generateSholatStudentReportPdf(
                                            context = context,
                                            schoolProfile = schoolProfile,
                                            student = st,
                                            sholatRecords = records,
                                            monthKey = selectedMonthKey,
                                            waliName = waliName
                                        )
                                        if (file != null) {
                                            lastGeneratedPdf = file
                                            PdfReportGenerator.shareSholatPdf(
                                                context = context,
                                                pdfFile = file,
                                                subject = if (isEn) "Prayer Report - ${st.name}" else "Rapor Sholat - ${st.name}",
                                                text = if (isEn) {
                                                    "Attached monthly prayer report for ${st.name} (${SholatHelper.formatMonthKeyDisplay(selectedMonthKey, true)})."
                                                } else {
                                                    "Terlampir Rapor Pembinaan Sholat 5 Waktu ananda ${st.name} bulan ${SholatHelper.formatMonthKeyDisplay(selectedMonthKey, false)}."
                                                }
                                            )
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(st.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("${st.classGrade} • ${st.dormitory}", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStudentPickerForPdf = false }) {
                    Text(if (isEn) "Close" else "Tutup")
                }
            }
        )
    }
}

@Composable
private fun SholatMonthNavigator(
    monthKey: String,
    onChangeMonthKey: (String) -> Unit,
    isEn: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { onChangeMonthKey(SholatHelper.shiftMonthKey(monthKey, -1)) },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = if (isEn) "Previous Month" else "Bulan Sebelumnya")
        }
        Text(
            text = SholatHelper.formatMonthKeyDisplay(monthKey, isEn),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF0F172A)
        )
        IconButton(
            onClick = { onChangeMonthKey(SholatHelper.shiftMonthKey(monthKey, 1)) },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = if (isEn) "Next Month" else "Bulan Berikutnya")
        }
    }
}

@Composable
private fun SholatKpiBox(
    label: String,
    value: String,
    sub: String,
    bgColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, fontSize = 10.5.sp, fontWeight = FontWeight.Medium, color = accentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
        Text(sub, fontSize = 10.sp, color = Color(0xFF475569), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
