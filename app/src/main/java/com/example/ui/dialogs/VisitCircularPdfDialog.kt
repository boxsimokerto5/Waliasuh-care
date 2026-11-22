package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ChildStudent
import com.example.data.model.VisitHelper
import com.example.data.model.VisitLaunchStage
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionSwap
import com.example.util.VisitCircularPdfConfig
import com.example.util.VisitCircularPdfHelper
import com.example.util.i18n.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VisitCircularPdfDialog(
    sessions: List<VisitSession>,
    students: List<ChildStudent>,
    swaps: List<VisitSessionSwap>,
    defaultSignerName: String,
    onDismiss: () -> Unit,
    onGeneratePdf: (selectedSessions: List<VisitSession>, config: VisitCircularPdfConfig) -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val locale = s.language.locale

    val availableMonths = remember(sessions) {
        sessions.map { it.monthYear }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedDescending()
    }
    var selectedMonth by remember(availableMonths) {
        mutableStateOf(availableMonths.firstOrNull() ?: "ALL")
    }

    val sessionsInScope = remember(sessions, selectedMonth) {
        sessions
            .filter { selectedMonth == "ALL" || it.monthYear.equals(selectedMonth, ignoreCase = true) }
            .sortedWith(
                compareBy<VisitSession> { it.getVisitDatesList().firstOrNull() ?: Long.MAX_VALUE }
                    .thenBy { it.title.lowercase(locale) }
            )
    }

    var selectedSessionIds by remember(sessionsInScope) {
        val activeIds = sessionsInScope.filter { it.isActive }.map { it.id }.toSet()
        mutableStateOf(activeIds.ifEmpty { sessionsInScope.map { it.id }.toSet() })
    }

    val periodLabel = remember(selectedMonth, locale, isEn) {
        if (selectedMonth == "ALL" || selectedMonth.isBlank()) {
            if (isEn) "All Active Visit Sessions" else "Seluruh Sesi Aktif"
        } else {
            VisitHelper.formatMonthYearLabel(selectedMonth, locale)
        }
    }

    var circularNumber by remember { mutableStateOf("") }
    var openingParagraph by remember(isEn) {
        mutableStateOf(VisitCircularPdfHelper.defaultOpeningParagraph(periodLabel, isEn))
    }
    var openingWasEdited by remember { mutableStateOf(false) }
    LaunchedEffect(periodLabel, isEn) {
        if (!openingWasEdited) {
            openingParagraph = VisitCircularPdfHelper.defaultOpeningParagraph(periodLabel, isEn)
        }
    }

    var guidelinesText by remember(isEn) {
        mutableStateOf(VisitCircularPdfHelper.defaultGuidelinesText(isEn))
    }
    var signerName by remember(defaultSignerName) { mutableStateOf(defaultSignerName) }
    var pageBreakPerSession by remember { mutableStateOf(true) }
    var includeUnscheduledAppendix by remember { mutableStateOf(true) }
    var showCustomizeText by remember { mutableStateOf(false) }

    val selectedSessions = remember(sessionsInScope, selectedSessionIds) {
        sessionsInScope.filter { it.id in selectedSessionIds }
    }
    val sessionRosterCounts = remember(sessionsInScope, students, swaps, sessions) {
        sessionsInScope.associate { sess ->
            sess.id to VisitHelper.effectiveScheduledStudentsForSession(
                session = sess,
                allStudents = students,
                swaps = swaps,
                allSessions = sessions
            )
        }
    }
    val coveredStudentIds = remember(selectedSessions, sessionRosterCounts) {
        selectedSessions.flatMap { sess -> sessionRosterCounts[sess.id].orEmpty().map { it.id } }.toSet()
    }
    val unscheduledCount = (students.size - coveredStudentIds.size).coerceAtLeast(0)
    val dateFmt = remember(locale) { SimpleDateFormat("EEE, d MMM yyyy", locale) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 560.dp)
                .testTag("dialog_visit_circular_pdf"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 8.dp, top = 14.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFCCFBF1),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "Monthly Visit Circular PDF" else "Generate PDF Edaran Kunjungan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isEn) {
                                    "1 combined PDF with cover notice & session-grouped student lists"
                                } else {
                                    "1 file PDF surat edaran & lampiran seluruh anak per sesi"
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = s.close
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Month filter chips
                    if (availableMonths.isNotEmpty()) {
                        Text(
                            text = if (isEn) "Select Month / Period" else "Pilih Bulan Edaran",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableMonths.forEach { ym ->
                                FilterChip(
                                    selected = selectedMonth.equals(ym, ignoreCase = true),
                                    onClick = { selectedMonth = ym },
                                    label = {
                                        Text(
                                            text = VisitHelper.formatMonthYearLabel(ym, locale),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFCCFBF1),
                                        selectedLabelColor = Color(0xFF0F766E)
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("chip_circular_month_$ym")
                                )
                            }
                            if (availableMonths.size > 1) {
                                FilterChip(
                                    selected = selectedMonth == "ALL",
                                    onClick = { selectedMonth = "ALL" },
                                    label = {
                                        Text(
                                            text = if (isEn) "All Months" else "Semua Bulan",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("chip_circular_month_ALL")
                                )
                            }
                        }
                    }

                    // Session checklist
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) {
                                "Sessions Included (${selectedSessions.size}/${sessionsInScope.size})"
                            } else {
                                "Sesi yang Dilampirkan (${selectedSessions.size}/${sessionsInScope.size})"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8)
                        )
                        if (sessionsInScope.size > 1) {
                            val allSelected = selectedSessionIds.size == sessionsInScope.size
                            OutlinedButton(
                                onClick = {
                                    selectedSessionIds = if (allSelected) emptySet()
                                    else sessionsInScope.map { it.id }.toSet()
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.heightIn(min = 40.dp)
                            ) {
                                Text(
                                    text = if (allSelected) {
                                        if (isEn) "Clear All" else "Kosongkan"
                                    } else {
                                        if (isEn) "Select All" else "Pilih Semua"
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    sessionsInScope.forEach { sess ->
                        val isChecked = sess.id in selectedSessionIds
                        val rosterCount = sessionRosterCounts[sess.id]?.size ?: 0
                        val datesText = sess.getVisitDatesList()
                            .joinToString(", ") { dateFmt.format(Date(it)) }
                            .ifBlank { "-" }
                        val stage = sess.launchStage()
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isChecked) Color(0xFFF0FDFA) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.dp,
                                if (isChecked) Color(0xFF5EEAD4) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSessionIds = if (isChecked) {
                                        selectedSessionIds - sess.id
                                    } else {
                                        selectedSessionIds + sess.id
                                    }
                                }
                                .testTag("row_circular_session_${sess.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedSessionIds = if (checked) {
                                            selectedSessionIds + sess.id
                                        } else {
                                            selectedSessionIds - sess.id
                                        }
                                    }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = sess.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (stage) {
                                                VisitLaunchStage.PARENT_LIVE -> Color(0xFFDCFCE7)
                                                VisitLaunchStage.WALI_PREVIEW -> Color(0xFFDBEAFE)
                                                VisitLaunchStage.DRAFT -> Color(0xFFFEF3C7)
                                                VisitLaunchStage.CLOSED -> Color(0xFFE2E8F0)
                                            }
                                        ) {
                                            Text(
                                                text = when (stage) {
                                                    VisitLaunchStage.PARENT_LIVE -> if (isEn) "Live" else "Live Ortu"
                                                    VisitLaunchStage.WALI_PREVIEW -> if (isEn) "Wali" else "Preview"
                                                    VisitLaunchStage.DRAFT -> if (isEn) "Draft" else "Draf"
                                                    VisitLaunchStage.CLOSED -> if (isEn) "Closed" else "Selesai"
                                                },
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (stage) {
                                                    VisitLaunchStage.PARENT_LIVE -> Color(0xFF15803D)
                                                    VisitLaunchStage.WALI_PREVIEW -> Color(0xFF1D4ED8)
                                                    VisitLaunchStage.DRAFT -> Color(0xFFB45309)
                                                    VisitLaunchStage.CLOSED -> Color(0xFF475569)
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "$datesText • ${sess.displayTimeRange()}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Districts: ${sess.getDistrictsList().joinToString(", ") { VisitHelper.displayDistrict(it, true) }} • $rosterCount student(s)"
                                        } else {
                                            "Kecamatan: ${sess.getDistrictsList().joinToString(", ") { VisitHelper.displayDistrict(it, false) }} • $rosterCount anak"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F766E)
                                    )
                                }
                            }
                        }
                    }

                    // Live summary card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (unscheduledCount > 0 && selectedSessions.isNotEmpty()) Color(0xFFFFFBEB) else Color(0xFFEFF6FF),
                        border = BorderStroke(
                            1.dp,
                            if (unscheduledCount > 0 && selectedSessions.isNotEmpty()) Color(0xFFFCD34D) else Color(0xFFBFDBFE)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_circular_pdf_summary")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (unscheduledCount > 0 && selectedSessions.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isEn) {
                                    "Ready: ${selectedSessions.size} session(s) • ${coveredStudentIds.size}/${students.size} students grouped." +
                                        if (unscheduledCount > 0) " ($unscheduledCount not yet in any selected session)" else ""
                                } else {
                                    "Siap cetak: ${selectedSessions.size} sesi • ${coveredStudentIds.size}/${students.size} siswa terkelompok." +
                                        if (unscheduledCount > 0) " ($unscheduledCount anak belum masuk sesi terpilih)" else ""
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (unscheduledCount > 0 && selectedSessions.isNotEmpty()) Color(0xFF92400E) else Color(0xFF1E40AF)
                            )
                        }
                    }

                    // Toggle options
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clickable { pageBreakPerSession = !pageBreakPerSession },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEn) "Start each session on a new page" else "Pisahkan tiap sesi di halaman baru",
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = pageBreakPerSession,
                                    onCheckedChange = { pageBreakPerSession = it },
                                    modifier = Modifier.testTag("switch_circular_page_break")
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clickable { includeUnscheduledAppendix = !includeUnscheduledAppendix },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEn) "Include list of ungrouped students" else "Lampirkan siswa yang belum masuk sesi",
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = includeUnscheduledAppendix,
                                    onCheckedChange = { includeUnscheduledAppendix = it },
                                    modifier = Modifier.testTag("switch_circular_unscheduled")
                                )
                            }
                        }
                    }

                    // Expandable circular wording customization
                    OutlinedButton(
                        onClick = { showCustomizeText = !showCustomizeText },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_toggle_circular_text_editor")
                    ) {
                        Icon(
                            imageVector = if (showCustomizeText) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) {
                                if (showCustomizeText) "Hide Letter Wording Options" else "Customize Letter Number, Intro & Rules"
                            } else {
                                if (showCustomizeText) "Tutup Pengaturan Teks Edaran" else "Atur Nomor Surat, Pembuka & Tata Tertib"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (showCustomizeText) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = circularNumber,
                                onValueChange = { circularNumber = it },
                                label = { Text(if (isEn) "Circular / Letter Number (Optional)" else "Nomor Surat Edaran (Opsional)") },
                                placeholder = { Text("B-01/SRT/XI/2026") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_circular_number")
                            )
                            OutlinedTextField(
                                value = openingParagraph,
                                onValueChange = {
                                    openingParagraph = it
                                    openingWasEdited = true
                                },
                                label = { Text(if (isEn) "Circular Opening Paragraph" else "Paragraf Pembuka Surat") },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_circular_opening")
                            )
                            OutlinedTextField(
                                value = guidelinesText,
                                onValueChange = { guidelinesText = it },
                                label = { Text(if (isEn) "Visiting Guidelines & Notes" else "Ketentuan & Tata Tertib Kunjungan") },
                                minLines = 3,
                                maxLines = 6,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_circular_guidelines")
                            )
                            OutlinedTextField(
                                value = signerName,
                                onValueChange = { signerName = it },
                                label = { Text(if (isEn) "Signer Name / Title" else "Penandatangan Surat") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_circular_signer")
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.4f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_cancel_visit_circular_pdf")
                    ) {
                        Text(s.cancel)
                    }
                    Button(
                        onClick = {
                            onGeneratePdf(
                                selectedSessions,
                                VisitCircularPdfConfig(
                                    monthYear = selectedMonth,
                                    circularNumber = circularNumber.trim(),
                                    openingParagraph = openingParagraph.trim(),
                                    guidelinesText = guidelinesText.trim(),
                                    pageBreakPerSession = pageBreakPerSession,
                                    includeUnscheduledAppendix = includeUnscheduledAppendix,
                                    signerName = signerName.trim()
                                )
                            )
                        },
                        enabled = selectedSessions.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                        modifier = Modifier
                            .weight(0.6f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_generate_visit_circular_pdf")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Generate Circular PDF" else "Generate PDF Edaran",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }
    }
}
