@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.SchoolProfile
import com.example.data.model.VisitHelper
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionSwap
import com.example.util.PdfReportGenerator
import com.example.util.VisitCircularPdfConfig
import com.example.util.VisitCircularPdfHelper
import com.example.util.i18n.LocalAppStrings
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class StudentEdaranRow(
    val student: ChildStudent,
    val assignedSession: VisitSession?,
    val incomingSwap: VisitSessionSwap?
)

@Composable
fun EdaranOrangTuaKunjunganSheet(
    sessions: List<VisitSession>,
    students: List<ChildStudent>,
    myFosterStudents: List<ChildStudent> = emptyList(),
    visitSessionSwaps: List<VisitSessionSwap> = emptyList(),
    schoolProfile: SchoolProfile,
    defaultSignerName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val locale = s.language.locale

    val primaryIndigo = Color(0xFF1D4ED8)
    val tealPrimary = Color(0xFF0F766E)
    val softIndigo = Color(0xFFEFF6FF)

    val availableMonths = remember(sessions) {
        sessions.map { it.monthYear }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedDescending()
    }
    var selectedMonth by remember(availableMonths) {
        mutableStateOf(availableMonths.firstOrNull() ?: "ALL")
    }

    val sessionsInMonth = remember(sessions, selectedMonth, locale) {
        sessions
            .filter { selectedMonth == "ALL" || it.monthYear.equals(selectedMonth, ignoreCase = true) }
            .sortedWith(
                compareBy<VisitSession> { it.getVisitDatesList().firstOrNull() ?: Long.MAX_VALUE }
                    .thenBy { it.title.lowercase(locale) }
            )
    }

    // null berarti semua sesi di bulan terpilih
    var selectedSessionFilterId by remember(selectedMonth, sessionsInMonth) {
        mutableStateOf<Long?>(null)
    }
    val effectiveSessions = remember(sessionsInMonth, selectedSessionFilterId) {
        if (selectedSessionFilterId == null) sessionsInMonth
        else sessionsInMonth.filter { it.id == selectedSessionFilterId }
    }

    var onlyMyFosterStudents by remember(myFosterStudents.size) {
        mutableStateOf(false)
    }
    val scopedStudents = remember(students, myFosterStudents, onlyMyFosterStudents) {
        if (onlyMyFosterStudents && myFosterStudents.isNotEmpty()) {
            myFosterStudents.sortedBy { it.name.lowercase(Locale.US) }
        } else {
            students.sortedBy { it.name.lowercase(Locale.US) }
        }
    }

    val periodLabel = remember(selectedMonth, locale, isEn) {
        if (selectedMonth == "ALL" || selectedMonth.isBlank()) {
            if (isEn) "All Active Visit Sessions" else "Seluruh Sesi Kunjungan Aktif"
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
    var showCustomizeEditor by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var readyPdfFile by remember { mutableStateOf<File?>(null) }

    val shortDateFmt = remember(locale) { SimpleDateFormat("EEE, d MMM yyyy", locale) }

    // Petakan setiap siswa ke sesi kunjungannya (memperhitungkan tukar sesi)
    val studentRows = remember(scopedStudents, effectiveSessions, sessions, visitSessionSwaps) {
        val sessionRosterMap = effectiveSessions.associate { sess ->
            sess.id to VisitHelper.effectiveScheduledStudentsForSession(
                session = sess,
                allStudents = scopedStudents,
                swaps = visitSessionSwaps,
                allSessions = sessions
            ).map { it.id }.toSet()
        }
        scopedStudents.map { st ->
            val matchedSession = effectiveSessions.firstOrNull { sess ->
                st.id in sessionRosterMap[sess.id].orEmpty()
            }
            val swap = matchedSession?.let { sess ->
                VisitHelper.findIncomingSwapToSession(sess.id, st, visitSessionSwaps)
            }
            StudentEdaranRow(
                student = st,
                assignedSession = matchedSession,
                incomingSwap = swap
            )
        }.sortedWith(
            compareBy<StudentEdaranRow> { it.assignedSession == null }
                .thenBy { it.assignedSession?.getVisitDatesList()?.firstOrNull() ?: Long.MAX_VALUE }
                .thenBy { it.student.name.lowercase(Locale.US) }
        )
    }

    val scheduledCount = remember(studentRows) { studentRows.count { it.assignedSession != null } }

    val filteredRows = remember(studentRows, searchQuery, isEn) {
        val q = searchQuery.trim().lowercase(Locale.US)
        if (q.isEmpty()) studentRows
        else studentRows.filter { row ->
            val st = row.student
            val dist = VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(st), isEn).lowercase(Locale.US)
            val sessTitle = row.assignedSession?.title?.lowercase(Locale.US).orEmpty()
            st.name.lowercase(Locale.US).contains(q) ||
                st.classGrade.lowercase(Locale.US).contains(q) ||
                st.dormitory.lowercase(Locale.US).contains(q) ||
                dist.contains(q) ||
                sessTitle.contains(q)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = { CompactVisitSheetHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header ringkas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(softIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = primaryIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Circular to Parents" else "Edaran Kepada Orang Tua",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isEn) {
                                    "Visit schedule, student sessions & visiting rules"
                                } else {
                                    "Jadwal, sesi kunjungan anak asuh & aturan"
                                },
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_edaran_orang_tua")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close, modifier = Modifier.size(18.dp))
                    }
                }

                // Filter Dropdown Bulan + Filter Dropdown Sesi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (availableMonths.isNotEmpty()) {
                        EdaranFilterDropdown(
                            label = periodLabel,
                            options = (availableMonths.map { ym ->
                                ym to VisitHelper.formatMonthYearLabel(ym, locale)
                            } + ("ALL" to (if (isEn) "All Months" else "Semua Bulan"))),
                            selectedKey = selectedMonth,
                            accentColor = tealPrimary,
                            containerColor = Color(0xFFF0FDFA),
                            borderColor = Color(0xFF99F6E4),
                            dropdownTag = "dropdown_edaran_month",
                            optionTagPrefix = "option_edaran_month",
                            onSelect = { key -> selectedMonth = key },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (sessionsInMonth.isNotEmpty()) {
                        val activeSessLabel = if (selectedSessionFilterId == null) {
                            if (isEn) "All Sessions (${sessionsInMonth.size})" else "Semua Sesi (${sessionsInMonth.size})"
                        } else {
                            sessionsInMonth.firstOrNull { it.id == selectedSessionFilterId }?.title ?: "-"
                        }
                        val sessionOpts = listOf<Pair<String, String>>(
                            "ALL" to (if (isEn) "All Sessions (${sessionsInMonth.size})" else "Semua Sesi (${sessionsInMonth.size})")
                        ) + sessionsInMonth.map { it.id.toString() to it.title }
                        EdaranFilterDropdown(
                            label = activeSessLabel,
                            options = sessionOpts,
                            selectedKey = selectedSessionFilterId?.toString() ?: "ALL",
                            accentColor = primaryIndigo,
                            containerColor = softIndigo,
                            borderColor = Color(0xFF93C5FD),
                            dropdownTag = "dropdown_edaran_session",
                            optionTagPrefix = "option_edaran_session",
                            onSelect = { key ->
                                selectedSessionFilterId = if (key == "ALL") null else key.toLongOrNull()
                            },
                            modifier = Modifier.weight(1.25f)
                        )
                    }
                }

                // Toggle cepat Anak Binaan Saya vs Semua Siswa (jika wali memiliki anak binaan)
                if (myFosterStudents.isNotEmpty() && myFosterStudents.size < students.size) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val allSel = !onlyMyFosterStudents
                        Surface(
                            onClick = { onlyMyFosterStudents = false },
                            shape = RoundedCornerShape(8.dp),
                            color = if (allSel) primaryIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_edaran_scope_all")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                Text(
                                    text = if (isEn) "All Students (${students.size})" else "Semua Siswa (${students.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (allSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Surface(
                            onClick = { onlyMyFosterStudents = true },
                            shape = RoundedCornerShape(8.dp),
                            color = if (!allSel) tealPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_edaran_scope_foster")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                Text(
                                    text = if (isEn) "My Foster (${myFosterStudents.size})" else "Binaan Saya (${myFosterStudents.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!allSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Tombol Utama Generate "Edaran Orang Tua"
                Button(
                    onClick = {
                        if (effectiveSessions.isEmpty()) {
                            Toast.makeText(
                                context,
                                if (isEn) "No visit sessions available to generate." else "Belum ada sesi kunjungan untuk dibuat edaran.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val generated = VisitCircularPdfHelper.generateMonthlyCircularPdf(
                                context = context,
                                sessions = effectiveSessions,
                                allStudents = scopedStudents,
                                swaps = visitSessionSwaps,
                                allSchoolSessions = sessions,
                                schoolProfile = schoolProfile,
                                config = VisitCircularPdfConfig(
                                    monthYear = selectedMonth,
                                    circularNumber = circularNumber.trim(),
                                    openingParagraph = openingParagraph.trim(),
                                    guidelinesText = guidelinesText.trim(),
                                    pageBreakPerSession = pageBreakPerSession,
                                    includeUnscheduledAppendix = includeUnscheduledAppendix,
                                    signerName = signerName.trim()
                                )
                            )
                            if (generated != null) {
                                readyPdfFile = generated
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isEn) "Failed to generate circular PDF" else "Gagal membuat PDF Edaran Orang Tua",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    enabled = effectiveSessions.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tealPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("btn_generate_edaran_orang_tua")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEn) "Generate Parent Circular" else "Generate Edaran Orang Tua",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Daftar Anak Asuh + Kartu Aturan Kunjungan dalam LazyColumn agar nyaman di-scroll
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .testTag("list_edaran_orang_tua_students"),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Kartu Aturan Kunjungan & Kustomisasi Teks Surat
                    item(key = "edaran_rules_card") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                            border = BorderStroke(1.dp, Color(0xFF99F6E4))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.Gavel,
                                            contentDescription = null,
                                            tint = tealPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = if (isEn) "Visiting Rules & Circular Text" else "Aturan & Ketentuan Edaran",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tealPrimary
                                        )
                                    }
                                    Surface(
                                        onClick = { showCustomizeEditor = !showCustomizeEditor },
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFF99F6E4)),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("btn_toggle_edaran_rules_editor")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = if (showCustomizeEditor) {
                                                    if (isEn) "Collapse" else "Tutup"
                                                } else {
                                                    if (isEn) "Edit Rules" else "Ubah Aturan"
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tealPrimary
                                            )
                                            Icon(
                                                imageVector = if (showCustomizeEditor) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = tealPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                if (!showCustomizeEditor) {
                                    Text(
                                        text = guidelinesText,
                                        fontSize = 10.5.sp,
                                        lineHeight = 14.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                } else {
                                    OutlinedTextField(
                                        value = circularNumber,
                                        onValueChange = { circularNumber = it },
                                        label = { Text(if (isEn) "Circular Ref No (Optional)" else "Nomor Surat (Opsional)", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_edaran_circular_number")
                                    )
                                    OutlinedTextField(
                                        value = openingParagraph,
                                        onValueChange = {
                                            openingWasEdited = true
                                            openingParagraph = it
                                        },
                                        label = { Text(if (isEn) "Opening Notice" else "Paragraf Pengantar", fontSize = 11.sp) },
                                        minLines = 2,
                                        maxLines = 4,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = guidelinesText,
                                        onValueChange = { guidelinesText = it },
                                        label = { Text(if (isEn) "Visiting Rules & Guidelines" else "Aturan & Tata Tertib Kunjungan", fontSize = 11.sp) },
                                        minLines = 3,
                                        maxLines = 6,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_edaran_guidelines")
                                    )
                                    OutlinedTextField(
                                        value = signerName,
                                        onValueChange = { signerName = it },
                                        label = { Text(if (isEn) "Signer Name" else "Penanda Tangan", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isEn) "New page per session" else "Halaman baru per sesi",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Switch(
                                            checked = pageBreakPerSession,
                                            onCheckedChange = { pageBreakPerSession = it }
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isEn) "Include unscheduled students" else "Lampiran anak belum masuk sesi",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Switch(
                                            checked = includeUnscheduledAppendix,
                                            onCheckedChange = { includeUnscheduledAppendix = it }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Search Bar + Header Daftar Anak Asuh
                    item(key = "edaran_students_search") {
                        CompactVisitInputField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = if (isEn) "Search student, district, or session..." else "Cari anak asuh, kecamatan, atau sesi...",
                            leadingIcon = Icons.Default.Search,
                            clearDescription = s.nav.close,
                            modifier = Modifier.testTag("input_edaran_search")
                        )
                    }

                    item(key = "edaran_students_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) {
                                    "Foster Student Schedule List (${filteredRows.size})"
                                } else {
                                    "Daftar Anak Asuh & Jadwal Sesi (${filteredRows.size})"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryIndigo
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDBEAFE)
                            ) {
                                Text(
                                    text = if (isEn) "$scheduledCount Scheduled" else "$scheduledCount Terjadwal",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryIndigo,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    if (filteredRows.isEmpty()) {
                        item(key = "edaran_students_empty") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isEn) "No matching students found." else "Tidak ada daftar anak asuh yang sesuai.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        itemsIndexed(filteredRows, key = { _, row -> "edaran_st_${row.student.id}" }) { idx, row ->
                            val st = row.student
                            val sess = row.assignedSession
                            val distLabel = VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(st), isEn)
                            val dateSummary = sess?.getVisitDatesList()
                                ?.joinToString(", ") { shortDateFmt.format(Date(it)) }
                                ?.ifBlank { "-" }
                                ?: "-"

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_edaran_student_${st.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (sess != null) MaterialTheme.colorScheme.surface else Color(0xFFFFFBEB)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (sess != null) MaterialTheme.colorScheme.outlineVariant else Color(0xFFFDE68A)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(if (sess != null) softIndigo else Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sess != null) primaryIndigo else Color(0xFFB45309)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = st.name,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$distLabel • ${st.classGrade.ifBlank { "-" }} • ${st.dormitory.ifBlank { "-" }}",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (sess != null) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFDBEAFE)
                                            ) {
                                                Text(
                                                    text = "${sess.title} • $dateSummary • ${sess.displayTimeRange()}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = primaryIndigo,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                            if (row.incomingSwap != null) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFEDE9FE)
                                                ) {
                                                    Text(
                                                        text = if (isEn) {
                                                            "Moved from ${row.incomingSwap.fromSessionTitle}"
                                                        } else {
                                                            "Pindahan dari ${row.incomingSwap.fromSessionTitle}"
                                                        },
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF6D28D9),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFFEF3C7)
                                            ) {
                                                Text(
                                                    text = if (isEn) "Not assigned to selected session" else "Belum masuk jadwal sesi terpilih",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFB45309),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

    readyPdfFile?.let { pdfFile ->
        AlertDialog(
            onDismissRequest = { readyPdfFile = null },
            modifier = Modifier.testTag("dialog_edaran_orang_tua_pdf_ready"),
            title = {
                Text(
                    text = if (isEn) "Parent Circular PDF Ready" else "PDF Edaran Orang Tua Siap",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEn) {
                        "The parent circular containing schedules, sessions, student list (${scopedStudents.size} students), and visiting rules has been generated."
                    } else {
                        "PDF Edaran Orang Tua lengkap dengan jadwal, sesi kunjungan, aturan, dan daftar ${scopedStudents.size} anak asuh berhasil dibuat."
                    },
                    fontSize = 12.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { PdfReportGenerator.openPdfFile(context, pdfFile) },
                    colors = ButtonDefaults.buttonColors(containerColor = tealPrimary),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("btn_open_edaran_pdf")
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isEn) "Open PDF" else "Buka PDF", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            PdfReportGenerator.shareDocumentPdf(
                                context = context,
                                pdfFile = pdfFile,
                                subject = if (isEn) "Official Parent Visit Circular" else "Edaran Jadwal Kunjungan Orang Tua",
                                text = if (isEn) {
                                    "Please find attached the official parent visit schedule circular ($periodLabel) including session rosters and visiting guidelines."
                                } else {
                                    "Berikut kami sampaikan Surat Edaran Resmi Jadwal Kunjungan Orang Tua ($periodLabel) lengkap beserta pembagian sesi anak asuh dan aturan kunjungan."
                                },
                                chooserTitle = if (isEn) "Share Parent Circular PDF" else "Bagikan Edaran Orang Tua"
                            )
                        },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("btn_share_edaran_pdf")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Share" else "Bagikan", fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = { readyPdfFile = null },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("btn_close_edaran_pdf_ready")
                    ) {
                        Text(s.nav.close)
                    }
                }
            }
        )
    }
}

@Composable
private fun EdaranFilterDropdown(
    label: String,
    options: List<Pair<String, String>>,
    selectedKey: String,
    accentColor: Color,
    containerColor: Color,
    borderColor: Color,
    dropdownTag: String,
    optionTagPrefix: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(10.dp),
            color = containerColor,
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag(dropdownTag)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 210.dp, max = 360.dp)
        ) {
            options.forEachIndexed { idx, (key, title) ->
                val isSel = key == selectedKey
                DropdownMenuItem(
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) accentColor else MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = if (isSel) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    } else null,
                    onClick = {
                        expanded = false
                        onSelect(key)
                    },
                    modifier = Modifier.testTag("${optionTagPrefix}_$key")
                )
                if (idx < options.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}
