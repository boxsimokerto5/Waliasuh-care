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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VisitMonthSessionFilterRow
import com.example.ui.components.VisitSheetGradientHeader
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

    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    val amberPrimary = Color(0xFFB45309)
    val amberAction = Color(0xFFD97706)
    val tealPrimary = Color(0xFF0F766E)
    val primaryIndigo = Color(0xFF1D4ED8)
    val softIndigo = if (isLight) Color(0xFFEFF6FF) else Color(0xFF1E3A8A).copy(alpha = 0.28f)
    val sheetBg = if (isLight) Color(0xFFFFFBF2) else MaterialTheme.colorScheme.surface

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
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        ),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = null,
        containerColor = sheetBg
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VisitSheetGradientHeader(
                title = if (isEn) "Circular to Parents" else "Edaran Kepada Orang Tua",
                subtitle = if (isEn) {
                    "Visit schedule, student sessions & visiting rules"
                } else {
                    "Jadwal, sesi kunjungan anak asuh & aturan"
                },
                icon = Icons.Default.Description,
                iconTint = amberPrimary,
                gradientColors = listOf(Color(0xFFB45309), Color(0xFFD97706), Color(0xFFF59E0B)),
                closeContentDescription = s.nav.close,
                closeTestTag = "btn_close_edaran_orang_tua",
                onDismiss = onDismiss,
                badgeText = if (scheduledCount > 0) {
                    if (isEn) "$scheduledCount Sched" else "$scheduledCount Jadwal"
                } else null
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (availableMonths.isNotEmpty() || sessionsInMonth.isNotEmpty()) {
                    VisitMonthSessionFilterRow(
                        allSessions = sessions,
                        selectedMonth = selectedMonth,
                        onSelectMonth = { selectedMonth = it },
                        sessionsInMonth = sessionsInMonth,
                        selectedSessionId = selectedSessionFilterId,
                        onSelectSession = { selectedSessionFilterId = it?.id },
                        locale = locale,
                        isEn = isEn,
                        monthAccentColor = amberPrimary,
                        monthContainerColor = Color(0xFFFFFBEB),
                        monthBorderColor = Color(0xFFFDE68A),
                        sessionAccentColor = tealPrimary,
                        sessionContainerColor = Color(0xFFF0FDFA),
                        sessionBorderColor = Color(0xFF99F6E4),
                        monthDropdownTag = "dropdown_edaran_month",
                        monthOptionTagPrefix = "option_edaran_month",
                        sessionDropdownTag = "dropdown_edaran_session",
                        sessionOptionTagPrefix = "option_edaran_session",
                        allowAllSessions = true
                    )
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
                            shape = RoundedCornerShape(10.dp),
                            color = if (allSel) amberPrimary else {
                                if (isLight) Color(0xFFFEF3C7).copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            border = if (!allSel && isLight) BorderStroke(1.dp, Color(0xFFFDE68A)) else null,
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
                            shape = RoundedCornerShape(10.dp),
                            color = if (!allSel) tealPrimary else {
                                if (isLight) Color(0xFFCCFBF1).copy(alpha = 0.55f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            border = if (allSel && isLight) BorderStroke(1.dp, Color(0xFF99F6E4)) else null,
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
                    colors = ButtonDefaults.buttonColors(containerColor = amberAction),
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
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLight) Color(0xFFF0FDFA) else MaterialTheme.colorScheme.surfaceContainer
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isLight) Color(0xFF99F6E4) else MaterialTheme.colorScheme.outlineVariant
                            )
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
                                            tint = if (isLight) tealPrimary else Color(0xFF5EEAD4),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = if (isEn) "Visiting Rules & Circular Text" else "Aturan & Ketentuan Edaran",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLight) tealPrimary else Color(0xFF5EEAD4)
                                        )
                                    }
                                    Surface(
                                        onClick = { showCustomizeEditor = !showCustomizeEditor },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isLight) Color.White else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isLight) Color(0xFF99F6E4) else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("btn_toggle_edaran_rules_editor")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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
                                                color = if (isLight) tealPrimary else Color(0xFF5EEAD4)
                                            )
                                            Icon(
                                                imageVector = if (showCustomizeEditor) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = if (isLight) tealPrimary else Color(0xFF5EEAD4),
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
                                        color = if (isLight) Color(0xFF1E293B) else MaterialTheme.colorScheme.onSurface
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
                            containerColor = if (isLight) Color(0xFFFFFDF7) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            borderColor = if (isLight) Color(0xFFFDE68A) else MaterialTheme.colorScheme.outlineVariant,
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
                                color = if (isLight) amberPrimary else Color(0xFFFCD34D)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isLight) Color(0xFFFEF3C7) else Color(0xFF78350F).copy(alpha = 0.45f)
                            ) {
                                Text(
                                    text = if (isEn) "$scheduledCount Scheduled" else "$scheduledCount Terjadwal",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLight) amberPrimary else Color(0xFFFDE68A),
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

                            val cardBg = if (!isLight) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else if (sess == null) {
                                Color(0xFFFFFBEB)
                            } else {
                                when (idx % 3) {
                                    0 -> Color(0xFFF0F9FF)
                                    1 -> Color(0xFFECFDF5)
                                    else -> Color(0xFFF5F3FF)
                                }
                            }
                            val cardBorder = if (!isLight) {
                                MaterialTheme.colorScheme.outlineVariant
                            } else if (sess == null) {
                                Color(0xFFFDE68A)
                            } else {
                                when (idx % 3) {
                                    0 -> Color(0xFFBAE6FD)
                                    1 -> Color(0xFFA7F3D0)
                                    else -> Color(0xFFDDD6FE)
                                }
                            }
                            val numBadgeBg = if (sess == null) {
                                if (isLight) Color(0xFFFEF3C7) else Color(0xFF78350F).copy(alpha = 0.45f)
                            } else {
                                when (idx % 3) {
                                    0 -> if (isLight) Color(0xFFDBEAFE) else Color(0xFF1E3A8A).copy(alpha = 0.4f)
                                    1 -> if (isLight) Color(0xFFD1FAE5) else Color(0xFF064E3B).copy(alpha = 0.4f)
                                    else -> if (isLight) Color(0xFFEDE9FE) else Color(0xFF4C1D95).copy(alpha = 0.4f)
                                }
                            }
                            val numBadgeText = if (sess == null) {
                                if (isLight) amberPrimary else Color(0xFFFDE68A)
                            } else {
                                when (idx % 3) {
                                    0 -> if (isLight) primaryIndigo else Color(0xFF93C5FD)
                                    1 -> if (isLight) tealPrimary else Color(0xFF6EE7B7)
                                    else -> if (isLight) Color(0xFF6D28D9) else Color(0xFFC4B5FD)
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_edaran_student_${st.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, cardBorder)
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
                                            .background(numBadgeBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = numBadgeText
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
                                                color = if (isLight) Color(0xFFFFFFFF).copy(alpha = 0.85f) else Color(0xFF1E3A8A).copy(alpha = 0.35f),
                                                border = BorderStroke(0.8.dp, cardBorder)
                                            ) {
                                                Text(
                                                    text = "${sess.title} • $dateSummary • ${sess.displayTimeRange()}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = numBadgeText,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                            if (row.incomingSwap != null) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isLight) Color(0xFFEDE9FE) else Color(0xFF4C1D95).copy(alpha = 0.4f)
                                                ) {
                                                    Text(
                                                        text = if (isEn) {
                                                            "Moved from ${row.incomingSwap.fromSessionTitle}"
                                                        } else {
                                                            "Pindahan dari ${row.incomingSwap.fromSessionTitle}"
                                                        },
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isLight) Color(0xFF6D28D9) else Color(0xFFC4B5FD),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isLight) Color(0xFFFEF3C7) else Color(0xFF78350F).copy(alpha = 0.45f)
                                            ) {
                                                Text(
                                                    text = if (isEn) "Not assigned to selected session" else "Belum masuk jadwal sesi terpilih",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isLight) amberPrimary else Color(0xFFFDE68A),
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
                    colors = ButtonDefaults.buttonColors(containerColor = amberAction),
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
