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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.SchoolProfile
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import com.example.util.TopOnAdManager
import com.example.util.i18n.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date

private enum class KartuExportAction {
    PRINT,
    PDF_SHARE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KartuMengajiSheet(
    students: List<ChildStudent>,
    mengajiRecords: List<MengajiRecord>,
    schoolProfile: SchoolProfile,
    waliName: String,
    initialStudent: ChildStudent? = null,
    onOpenInputForStudent: (ChildStudent) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language

    val emeraldPrimary = Color(0xFF047857)
    val emeraldLight = Color(0xFFD1FAE5)

    val defaultStudent = remember(initialStudent, students, mengajiRecords) {
        initialStudent
            ?: students.firstOrNull { st -> mengajiRecords.any { MengajiHelper.matchesStudent(it, st) } }
            ?: students.firstOrNull()
    }
    var selectedStudent by remember(defaultStudent) { mutableStateOf(defaultStudent) }
    var showStudentPicker by remember { mutableStateOf(false) }
    var studentSearchQuery by remember { mutableStateOf("") }

    val studentRecords = remember(selectedStudent, mengajiRecords) {
        val st = selectedStudent ?: return@remember emptyList()
        MengajiHelper.recordsForStudent(mengajiRecords, st)
    }

    val currentMonthKey = remember { DateUtils.getMonthKey(System.currentTimeMillis()) }
    val monthOptions = remember(studentRecords, currentMonthKey) {
        (listOf(currentMonthKey) + MengajiHelper.availableMonthKeys(studentRecords) + listOf("ALL")).distinct()
    }
    var selectedMonthKey by remember(selectedStudent) {
        val keysForStudent = MengajiHelper.availableMonthKeys(studentRecords)
        val initialMonth = if (currentMonthKey in keysForStudent || keysForStudent.isEmpty()) {
            currentMonthKey
        } else {
            keysForStudent.first()
        }
        mutableStateOf(initialMonth)
    }

    val filteredMonthlyRecords = remember(studentRecords, selectedMonthKey) {
        MengajiHelper.filterByMonth(studentRecords, selectedMonthKey)
    }
    val periodLabel = remember(selectedMonthKey, isEn) {
        MengajiHelper.formatMonthLabel(selectedMonthKey, isEn)
    }

    var pendingExportAction by remember { mutableStateOf<KartuExportAction?>(null) }

    val dateShortFmt = remember(lang) { SimpleDateFormat("dd/MM", lang.locale) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 660.dp)
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Print Mengaji Progress Card" else "Cetak Kartu Mengaji Bulanan",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isEn) {
                                "Monthly columns: No, Date, Volume, Page, Fluency & Guardian Sign"
                            } else {
                                "Tabel bulanan: No, Tanggal, Jilid, Halaman, Keterangan & Paraf Wali"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_kartu_mengaji")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close, modifier = Modifier.size(20.dp))
                    }
                }

                // Reward Ad Declaration before Printing/PDF
                val currentStudent = selectedStudent
                pendingExportAction?.let { exportAction ->
                    if (currentStudent != null) {
                        RewardAdDeclarationDialog(
                            title = if (exportAction == KartuExportAction.PRINT) {
                                if (isEn) "Print Mengaji Card" else "Cetak Kartu Mengaji"
                            } else {
                                if (isEn) "Download Mengaji Card PDF" else "Unduh PDF Kartu Mengaji"
                            },
                            actionDescription = if (isEn) {
                                "generate the Mengaji Progress Card for ${currentStudent.name} ($periodLabel)"
                            } else {
                                "mencetak Kartu Mengaji ${currentStudent.name} ($periodLabel)"
                            },
                            confirmButtonText = if (isEn) "Watch & Continue" else "Tonton & Lanjutkan",
                            onConfirmWatchAd = {
                                pendingExportAction = null
                                TopOnAdManager.showRewardAdForPdfReport(context) {
                                    val pdf = PdfReportGenerator.generateKartuMengajiPdf(
                                        context = context,
                                        student = currentStudent,
                                        records = filteredMonthlyRecords,
                                        periodLabel = periodLabel,
                                        waliName = waliName,
                                        schoolProfile = schoolProfile
                                    )
                                    if (pdf == null) {
                                        Toast.makeText(
                                            context,
                                            if (isEn) "Failed to generate Mengaji Card PDF." else "Gagal membuat PDF Kartu Mengaji.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (exportAction == KartuExportAction.PRINT) {
                                        PdfReportGenerator.printPdfWithSystemPrintManager(
                                            context = context,
                                            pdfFile = pdf,
                                            jobName = "Kartu_Mengaji_${currentStudent.name}_$periodLabel"
                                        )
                                    } else {
                                        PdfReportGenerator.shareFastingPdf(
                                            context = context,
                                            pdfFile = pdf,
                                            subject = if (isEn) {
                                                "Mengaji Card - ${currentStudent.name} ($periodLabel)"
                                            } else {
                                                "Kartu Mengaji - ${currentStudent.name} ($periodLabel)"
                                            },
                                            text = if (isEn) {
                                                "Attached is the Monthly Mengaji Progress Card for ${currentStudent.name} ($periodLabel)."
                                            } else {
                                                "Terlampir Kartu Kendali Mengaji Bulanan untuk ${currentStudent.name} ($periodLabel)."
                                            }
                                        )
                                    }
                                }
                            },
                            onDismiss = { pendingExportAction = null }
                        )
                    }
                }

                if (students.isEmpty() || currentStudent == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (isEn) "No student data available yet." else "Belum ada data siswa terdaftar.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    // Kartu Pilihan Siswa Aktif + Tombol Ganti & Input Cepat
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentStudent.name,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF064E3B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${FastingJenjangHelper.resolveJenjang(currentStudent)} • ${currentStudent.classGrade.ifBlank { "-" }} • ${currentStudent.dormitory}",
                                        fontSize = 11.sp,
                                        color = emeraldPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { showStudentPicker = !showStudentPicker },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .testTag("btn_toggle_picker_kartu_mengaji")
                                    ) {
                                        Text(
                                            text = if (showStudentPicker) {
                                                if (isEn) "Hide" else "Tutup"
                                            } else {
                                                if (isEn) "Pick Student" else "Ganti Anak"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = emeraldPrimary
                                        )
                                    }
                                    Button(
                                        onClick = { onOpenInputForStudent(currentStudent) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .testTag("btn_quick_input_from_kartu")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(if (isEn) "Input" else "Input", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (showStudentPicker) {
                                OutlinedTextField(
                                    value = studentSearchQuery,
                                    onValueChange = { studentSearchQuery = it },
                                    placeholder = {
                                        Text(if (isEn) "Search student..." else "Cari nama siswa...", fontSize = 11.5.sp)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_search_kartu_student")
                                )
                                val filteredPicker = remember(students, studentSearchQuery) {
                                    val q = studentSearchQuery.trim().lowercase()
                                    students.filter { st ->
                                        q.isBlank() || st.name.lowercase().contains(q) || st.classGrade.lowercase().contains(q)
                                    }.take(8)
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    filteredPicker.forEach { st ->
                                        val count = mengajiRecords.count { MengajiHelper.matchesStudent(it, st) }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (st.id == currentStudent.id) emeraldLight else Color.White)
                                                .clickable {
                                                    selectedStudent = st
                                                    showStudentPicker = false
                                                }
                                                .padding(horizontal = 10.dp, vertical = 7.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${st.name} (${st.classGrade.ifBlank { "-" }})",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (isEn) "$count logs" else "$count setoran",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = emeraldPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Pilih Bulan Kartu Mengaji
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        monthOptions.forEach { monthKey ->
                            val selected = selectedMonthKey == monthKey
                            FilterChip(
                                selected = selected,
                                onClick = { selectedMonthKey = monthKey },
                                label = {
                                    Text(
                                        text = MengajiHelper.formatMonthLabel(monthKey, isEn),
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = emeraldLight,
                                    selectedLabelColor = emeraldPrimary
                                ),
                                modifier = Modifier.testTag("chip_month_kartu_$monthKey")
                            )
                        }
                    }

                    // Tombol Print Kartu, PDF & Share WA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { pendingExportAction = KartuExportAction.PRINT },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_print_kartu_mengaji")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEn) "Print" else "Print Kartu", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        Button(
                            onClick = { pendingExportAction = KartuExportAction.PDF_SHARE },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_pdf_kartu_mengaji")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEn) "PDF" else "PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                val waText = PdfReportGenerator.buildKartuMengajiWaSummary(
                                    student = currentStudent,
                                    records = filteredMonthlyRecords,
                                    periodLabel = periodLabel,
                                    waliName = waliName,
                                    schoolProfile = schoolProfile,
                                    language = lang
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, waText)
                                }
                                context.startActivity(
                                    Intent.createChooser(intent, if (isEn) "Share Mengaji Card" else "Bagikan Kartu Mengaji")
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_wa_kartu_mengaji")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share WA", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), maxLines = 1)
                        }
                    }

                    // Pratinjau Langsung Tabel Kartu Mengaji (No | Tgl | Jilid | Hal. | Ket. | Wali)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .testTag("list_kartu_mengaji_preview"),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        item(key = "kartu_table_header") {
                            Surface(
                                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                                color = emeraldLight,
                                border = BorderStroke(1.dp, emeraldPrimary.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("No", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = emeraldPrimary, modifier = Modifier.width(24.dp))
                                    Text(if (isEn) "Date" else "Tgl", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = emeraldPrimary, modifier = Modifier.width(42.dp))
                                    Text(if (isEn) "Volume" else "Jilid", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = emeraldPrimary, modifier = Modifier.weight(1.1f))
                                    Text(if (isEn) "Page" else "Hal.", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = emeraldPrimary, modifier = Modifier.weight(0.8f))
                                    Text(if (isEn) "Status" else "Ket.", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = emeraldPrimary, modifier = Modifier.weight(0.95f))
                                    Text(if (isEn) "Sign" else "Wali", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = emeraldPrimary, modifier = Modifier.weight(0.85f), textAlign = TextAlign.End)
                                }
                            }
                        }

                        if (filteredMonthlyRecords.isEmpty()) {
                            item(key = "kartu_empty_rows") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        .padding(18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isEn) {
                                            "No Mengaji entries for ${currentStudent.name} in $periodLabel."
                                        } else {
                                            "Belum ada setoran mengaji ${currentStudent.name} pada $periodLabel."
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(filteredMonthlyRecords, key = { _, rec -> "kartu_row_${rec.id}" }) { index, rec ->
                                val isFluent = MengajiHelper.isFluent(rec.fluencyStatus)
                                val bgColor = if (index % 2 == 0) MaterialTheme.colorScheme.surface else Color(0xFFF8FAFC)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bgColor)
                                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                        .padding(horizontal = 8.dp, vertical = 7.dp)
                                        .testTag("row_kartu_mengaji_${rec.id}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${index + 1}.", fontSize = 10.5.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(24.dp))
                                    Text(dateShortFmt.format(Date(rec.sessionDate)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(42.dp))
                                    Text(
                                        text = "${rec.bookType}\n${rec.jilidOrJuz}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1.1f)
                                    )
                                    Text(
                                        text = rec.pageOrVerse,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(0.8f)
                                    )
                                    Box(modifier = Modifier.weight(0.95f)) {
                                        Text(
                                            text = MengajiHelper.fluencyLabel(rec.fluencyStatus, isEn),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFluent) Color(0xFF15803D) else Color(0xFFB91C1C),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(if (isFluent) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                                .padding(horizontal = 5.dp, vertical = 2.dp),
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = rec.waliName.ifBlank { waliName },
                                        fontSize = 9.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(0.85f)
                                    )
                                }
                            }
                        }

                        // Blok Tanda Tangan Wali Asuh di Bagian Bawah Pratinjau Kartu
                        item(key = "kartu_signature_footer") {
                            Surface(
                                shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val fluentCount = filteredMonthlyRecords.count { MengajiHelper.isFluent(it.fluencyStatus) }
                                    val repeatCount = filteredMonthlyRecords.size - fluentCount
                                    Column {
                                        Text(
                                            text = if (isEn) {
                                                "Total: ${filteredMonthlyRecords.size} • Fluent: $fluentCount • Repeat: $repeatCount"
                                            } else {
                                                "Total: ${filteredMonthlyRecords.size}x • Lancar: $fluentCount • Ulang: $repeatCount"
                                            },
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = emeraldPrimary
                                        )
                                        Text(
                                            text = schoolProfile.schoolName.ifBlank { "Wali Asuh Care" },
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = emeraldPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = if (isEn) "Guardian Signature" else "TTD Wali Asuh",
                                                fontSize = 9.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = waliName.ifBlank { "Wali Asuh" },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
