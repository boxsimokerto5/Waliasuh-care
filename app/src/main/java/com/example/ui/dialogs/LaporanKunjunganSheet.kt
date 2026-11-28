@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.dialogs

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.ChildStudent
import com.example.data.model.SchoolProfile
import com.example.data.model.VisitHelper
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionSwap
import com.example.ui.components.VisitMonthSessionFilterRow
import com.example.ui.components.VisitSheetGradientHeader
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import com.example.util.TopOnAdManager
import com.example.util.i18n.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun LaporanKunjunganSheet(
    sessions: List<VisitSession>,
    students: List<ChildStudent>,
    visitRecords: List<VisitRecord>,
    visitSessionSwaps: List<VisitSessionSwap> = emptyList(),
    schoolProfile: SchoolProfile,
    waliName: String,
    onQuickMarkManual: ((VisitSession, ChildStudent, Long) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language

    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val emeraldPrimary = Color(0xFF047857)
    val plumPrimary = Color(0xFF7E22CE)
    val sheetBg = if (isLight) Color(0xFFFAF5FF) else MaterialTheme.colorScheme.surface

    val sortedSessions = remember(sessions) {
        sessions.sortedWith(
            compareByDescending<VisitSession> { it.isActive }
                .thenByDescending { it.monthYear }
                .thenByDescending { it.createdAt }
        )
    }

    val availableMonths = remember(sortedSessions) {
        sortedSessions.map { it.monthYear }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedDescending()
    }
    var selectedMonth by remember(availableMonths, sortedSessions) {
        mutableStateOf(sortedSessions.firstOrNull()?.monthYear?.takeIf { it.isNotBlank() } ?: availableMonths.firstOrNull() ?: "ALL")
    }
    val sessionsInMonth = remember(sortedSessions, selectedMonth) {
        if (selectedMonth == "ALL" || availableMonths.isEmpty()) sortedSessions
        else sortedSessions.filter { it.monthYear.equals(selectedMonth, ignoreCase = true) }.ifEmpty { sortedSessions }
    }

    var selectedSessionId by remember(sortedSessions) { mutableStateOf(sortedSessions.firstOrNull()?.id) }
    LaunchedEffect(selectedMonth, sessionsInMonth) {
        if (sessionsInMonth.none { it.id == selectedSessionId }) {
            selectedSessionId = sessionsInMonth.firstOrNull()?.id
        }
    }
    val selectedSession = sessionsInMonth.firstOrNull { it.id == selectedSessionId } ?: sessionsInMonth.firstOrNull()

    // null = Semua Tanggal Sesi, > 0 = Tanggal spesifik ("hari itu")
    var selectedDateFilter by remember(selectedSession?.id) {
        val today = VisitHelper.normalizeDayStart(System.currentTimeMillis())
        val initial = if (selectedSession?.isDateScheduled(today) == true) today else null
        mutableStateOf<Long?>(initial)
    }
    var selectedDistrictFilter by remember(selectedSession?.id) { mutableStateOf("Semua") }
    var statusTab by remember { mutableIntStateOf(0) } // 0 = Belum Berkunjung, 1 = Sudah Hadir, 2 = Semua
    var showPdfRewardDialog by remember { mutableStateOf(false) }

    val shortDateFmt = remember(lang.locale) { SimpleDateFormat("EEE, d MMM", lang.locale) }
    val timeFmt = remember(lang.locale) { SimpleDateFormat("HH:mm", lang.locale) }

    val dateOptions = remember(selectedSession) {
        val today = VisitHelper.normalizeDayStart(System.currentTimeMillis())
        val base = selectedSession?.getVisitDatesList().orEmpty()
        (base + today).distinct().sorted()
    }

    val allScheduledStudents = remember(selectedSession, students, visitSessionSwaps, sortedSessions, selectedDistrictFilter) {
        if (selectedSession == null) emptyList()
        else {
            val base = VisitHelper.effectiveScheduledStudentsForSession(
                session = selectedSession,
                allStudents = students,
                swaps = visitSessionSwaps,
                allSessions = sortedSessions
            )
            if (selectedDistrictFilter == "Semua") base
            else base.filter { VisitHelper.resolveStudentDistrictKey(it).equals(selectedDistrictFilter, ignoreCase = true) }
        }
    }

    val filteredRecords = remember(visitRecords, selectedSession, selectedDateFilter, selectedDistrictFilter) {
        if (selectedSession == null) emptyList()
        else {
            val targetDay = selectedDateFilter?.let { VisitHelper.normalizeDayStart(it) }
            visitRecords.filter { rec ->
                rec.sessionId == selectedSession.id &&
                    (targetDay == null || VisitHelper.normalizeDayStart(rec.visitDate) == targetDay) &&
                    (selectedDistrictFilter == "Semua" ||
                        VisitHelper.normalizeDistrict(rec.studentDistrict).equals(selectedDistrictFilter, ignoreCase = true))
            }.sortedByDescending { it.checkedInAt }
        }
    }

    val attendedList = remember(allScheduledStudents, filteredRecords, selectedDateFilter) {
        val scheduledMatched = allScheduledStudents.mapNotNull { st ->
            VisitHelper.findVisitForStudent(filteredRecords, st, selectedDateFilter)?.let { st to it }
        }
        val extraGuestRecords = filteredRecords.filter { rec ->
            allScheduledStudents.none { st -> VisitHelper.matchesRecordToStudent(rec, st) }
        }
        scheduledMatched to extraGuestRecords
    }
    val presentPairs = attendedList.first
    val extraRecords = attendedList.second
    val totalPresentCount = presentPairs.size + extraRecords.size

    val notVisitedStudents = remember(allScheduledStudents, filteredRecords, selectedDateFilter) {
        allScheduledStudents.filter { st ->
            VisitHelper.findVisitForStudent(filteredRecords, st, selectedDateFilter) == null
        }
    }

    val attendancePct = if (allScheduledStudents.isEmpty()) 0 else {
        ((presentPairs.size * 100) / allScheduledStudents.size).coerceIn(0, 100)
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
                title = if (isEn) "Visit Attendance Report" else "Laporan Kehadiran Kunjungan",
                subtitle = if (isEn) "Track attended vs not-yet-visited students" else "Rekap siswa hadir vs belum berkunjung",
                icon = Icons.Default.Assessment,
                iconTint = plumPrimary,
                gradientColors = listOf(Color(0xFF6B21A8), Color(0xFF9333EA), Color(0xFFEC4899)),
                closeContentDescription = s.nav.close,
                closeTestTag = "btn_close_laporan_kunjungan",
                onDismiss = onDismiss,
                badgeText = if (selectedSession != null) "$attendancePct%" else null
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (sortedSessions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (isEn) "No visit sessions have been created yet." else "Belum ada sesi kunjungan yang dibuat oleh Admin Sekolah.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    // Dropdown Bulan -> Dropdown Sesi (scrollable ke bawah)
                    VisitMonthSessionFilterRow(
                        allSessions = sortedSessions,
                        selectedMonth = selectedMonth,
                        onSelectMonth = { monthKey -> selectedMonth = monthKey },
                        sessionsInMonth = sessionsInMonth,
                        selectedSessionId = selectedSession?.id,
                        onSelectSession = { sess -> if (sess != null) selectedSessionId = sess.id },
                        locale = lang.locale,
                        isEn = isEn,
                        monthAccentColor = plumPrimary,
                        monthContainerColor = Color(0xFFF3E8FF),
                        monthBorderColor = Color(0xFFD8B4FE),
                        sessionAccentColor = Color(0xFFBE185D),
                        sessionContainerColor = Color(0xFFFCE7F3),
                        sessionBorderColor = Color(0xFFF9A8D4),
                        monthDropdownTag = "dropdown_visit_report_month",
                        monthOptionTagPrefix = "option_visit_report_month",
                        sessionDropdownTag = "dropdown_visit_report_session",
                        sessionOptionTagPrefix = "option_visit_report_session"
                    )

                    // Filter Hari/Tanggal ("Semua Tanggal Sesi" vs Tanggal Tertentu)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedDateFilter == null,
                            onClick = { selectedDateFilter = null },
                            label = {
                                Text(
                                    if (isEn) "All Session Dates" else "Semua Tanggal Sesi",
                                    fontSize = 10.5.sp,
                                    fontWeight = if (selectedDateFilter == null) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                        dateOptions.forEach { dMillis ->
                            val isSel = selectedDateFilter != null &&
                                VisitHelper.normalizeDayStart(selectedDateFilter!!) == VisitHelper.normalizeDayStart(dMillis)
                            val dText = buildString {
                                append(shortDateFmt.format(Date(dMillis)))
                                if (DateUtils.isToday(dMillis)) append(if (isEn) " (Today)" else " (Hari Ini)")
                            }
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedDateFilter = dMillis },
                                label = { Text(dText, fontSize = 10.5.sp) },
                                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                    }

                    // Filter Kecamatan
                    selectedSession?.let { sess ->
                        val districts = sess.getDistrictsList()
                        if (districts.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedDistrictFilter == "Semua",
                                    onClick = { selectedDistrictFilter = "Semua" },
                                    label = { Text(if (isEn) "All Districts" else "Semua Kecamatan", fontSize = 10.5.sp) }
                                )
                                districts.forEach { dKey ->
                                    val isSel = selectedDistrictFilter.equals(dKey, ignoreCase = true)
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedDistrictFilter = if (isSel) "Semua" else dKey },
                                        label = { Text(VisitHelper.displayDistrict(dKey, isEn), fontSize = 10.5.sp) },
                                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    )
                                }
                            }
                        }
                    }

                    // Ringkasan 3 Kartu: Terjadwal, Sudah Hadir, Belum Berkunjung bernuansa pastel lembut
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLight) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isLight) Color(0xFFE9D5FF) else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ReportStatPill(
                                    label = if (isEn) "Scheduled" else "Terjadwal",
                                    value = "${allScheduledStudents.size}",
                                    bg = if (isLight) Color(0xFFE0F2FE) else Color(0xFF0C4A6E),
                                    fg = if (isLight) Color(0xFF0369A1) else Color(0xFFBAE6FD),
                                    modifier = Modifier.weight(1f)
                                )
                                ReportStatPill(
                                    label = if (isEn) "Attended" else "Sudah Hadir",
                                    value = "$totalPresentCount",
                                    bg = if (isLight) Color(0xFFDCFCE7) else Color(0xFF14532D),
                                    fg = if (isLight) Color(0xFF15803D) else Color(0xFFBBF7D0),
                                    modifier = Modifier.weight(1f)
                                )
                                ReportStatPill(
                                    label = if (isEn) "Not Visited" else "Belum Hadir",
                                    value = "${notVisitedStudents.size}",
                                    bg = if (isLight) Color(0xFFFCE7F3) else Color(0xFF831843),
                                    fg = if (isLight) Color(0xFFBE185D) else Color(0xFFFBCFE8),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEn) "Attendance Progress" else "Capaian Kehadiran",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$attendancePct%",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLight) plumPrimary else Color(0xFFD8B4FE)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { attendancePct / 100f },
                                color = if (isLight) plumPrimary else Color(0xFFC084FC),
                                trackColor = if (isLight) Color(0xFFF3E8FF) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }

                    // Tabs Status: Belum Berkunjung vs Sudah Hadir vs Semua
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = statusTab == 0,
                            onClick = { statusTab = 0 },
                            label = {
                                Text(
                                    if (isEn) "Not Visited (${notVisitedStudents.size})" else "Belum Hadir (${notVisitedStudents.size})",
                                    fontSize = 11.sp,
                                    fontWeight = if (statusTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEE2E2),
                                selectedLabelColor = Color(0xFFDC2626)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("filter_report_not_visited")
                        )
                        FilterChip(
                            selected = statusTab == 1,
                            onClick = { statusTab = 1 },
                            label = {
                                Text(
                                    if (isEn) "Attended ($totalPresentCount)" else "Sudah Hadir ($totalPresentCount)",
                                    fontSize = 11.sp,
                                    fontWeight = if (statusTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDCFCE7),
                                selectedLabelColor = Color(0xFF15803D)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("filter_report_visited")
                        )
                        FilterChip(
                            selected = statusTab == 2,
                            onClick = { statusTab = 2 },
                            label = { Text(if (isEn) "All" else "Semua", fontSize = 11.sp) },
                            modifier = Modifier.testTag("filter_report_all")
                        )
                    }

                    // Tombol Share WA & Unduh PDF Laporan
                    selectedSession?.let { sess ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showPdfRewardDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_pdf_laporan_kunjungan")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEn) "PDF Report" else "Cetak PDF Laporan", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = {
                                    val waText = PdfReportGenerator.buildVisitReportWaSummary(
                                        session = sess,
                                        scheduledStudents = allScheduledStudents,
                                        sessionRecords = filteredRecords,
                                        targetDateMillis = selectedDateFilter,
                                        waliName = waliName,
                                        schoolProfile = schoolProfile,
                                        language = lang
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, waText)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(intent, if (isEn) "Share Visit Report" else "Bagikan Laporan Kunjungan")
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_wa_laporan_kunjungan")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = emeraldPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEn) "Share WA" else "Bagikan WA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = emeraldPrimary, maxLines = 1)
                            }
                        }
                    }

                    // Daftar Siswa sesuai Tab Filter
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .testTag("list_laporan_kunjungan"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (statusTab == 0 || statusTab == 2) {
                            if (notVisitedStudents.isEmpty() && statusTab == 0) {
                                item(key = "report_all_visited") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (allScheduledStudents.isEmpty()) {
                                                if (isEn) "No scheduled students match this session/district." else "Belum ada siswa terjadwal pada kecamatan sesi ini."
                                            } else {
                                                if (isEn) "All scheduled students have attended!" else "Alhamdulillah, seluruh siswa terjadwal sudah dikunjungi!"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                }
                            } else {
                                itemsIndexed(notVisitedStudents, key = { _, st -> "absent_${st.id}" }) { idx, st ->
                                    val dist = VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(st), isEn)
                                    val targetMarkDate = selectedDateFilter ?: dateOptions.firstOrNull() ?: System.currentTimeMillis()
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isLight) Color(0xFFFFF1F2) else Color(0xFF2E141B)
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isLight) Color(0xFFFECDD3) else Color(0xFFFB7185).copy(alpha = 0.45f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(
                                                    Icons.Default.HourglassTop,
                                                    contentDescription = null,
                                                    tint = if (isLight) Color(0xFFBE123C) else Color(0xFFFDA4AF),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    val incSwap = selectedSession?.let { VisitHelper.findIncomingSwapToSession(it.id, st, visitSessionSwaps) }
                                                    Text("${idx + 1}. ${st.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text(
                                                        text = "$dist • ${st.classGrade.ifBlank { "-" }} • Wali: ${st.motherName.ifBlank { st.emergencyContact.ifBlank { "-" } }}" +
                                                            if (incSwap != null) (if (isEn) " • Moved" else " • Pindahan") else "",
                                                        fontSize = 10.5.sp,
                                                        color = if (isLight) Color(0xFF9F1239) else Color(0xFFFECDD3),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            if (onQuickMarkManual != null && selectedSession != null) {
                                                OutlinedButton(
                                                    onClick = { onQuickMarkManual(selectedSession, st, targetMarkDate) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.heightIn(min = 34.dp)
                                                ) {
                                                    Text(if (isEn) "+ Visit" else "+ Hadir", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = emeraldPrimary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (statusTab == 1 || statusTab == 2) {
                            val combinedPresent = presentPairs.map { it.second } + extraRecords
                            if (combinedPresent.isEmpty() && statusTab == 1) {
                                item(key = "report_none_visited") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isEn) "No check-ins recorded yet for this date." else "Belum ada siswa yang tercatat hadir berkunjung.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                itemsIndexed(combinedPresent, key = { _, rec -> "present_${rec.id}" }) { idx, rec ->
                                    val dist = VisitHelper.displayDistrict(rec.studentDistrict, isEn)
                                    val visitor = listOf(rec.visitorRelation, rec.visitorName).filter { it.isNotBlank() }.joinToString(": ")
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isLight) Color(0xFFECFDF5) else Color(0xFF102A22)
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isLight) Color(0xFF86EFAC) else Color(0xFF16A34A).copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("${idx + 1}. ${rec.studentName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(
                                                    text = "$dist • ${shortDateFmt.format(Date(rec.visitDate))} ${timeFmt.format(Date(rec.checkedInAt))} WIB • $visitor",
                                                    fontSize = 10.5.sp,
                                                    color = if (isLight) Color(0xFF166534) else Color(0xFFBBF7D0),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (rec.isOutOfScheduleDistrict) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                                            ) {
                                                Text(
                                                    text = if (rec.isOutOfScheduleDistrict) {
                                                        if (isEn) "Extra" else "Di Luar Jadwal"
                                                    } else {
                                                        rec.checkInMethod
                                                    },
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (rec.isOutOfScheduleDistrict) Color(0xFF92400E) else Color(0xFF15803D),
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

    if (showPdfRewardDialog && selectedSession != null) {
        RewardAdDeclarationDialog(
            title = if (isEn) "Download Visit Attendance PDF" else "Unduh Laporan PDF Kunjungan",
            actionDescription = if (isEn) {
                "download and share the Visit Attendance Report PDF for ${selectedSession.title}"
            } else {
                "mengunduh dan membagikan Laporan PDF Kehadiran Kunjungan ${selectedSession.title}"
            },
            confirmButtonText = if (isEn) "Watch & Download PDF" else "Tonton & Unduh PDF",
            onConfirmWatchAd = {
                showPdfRewardDialog = false
                TopOnAdManager.showRewardAdForPdfReport(context) {
                    val file = PdfReportGenerator.generateVisitAttendanceReportPdf(
                        context = context,
                        session = selectedSession,
                        scheduledStudents = allScheduledStudents,
                        sessionRecords = filteredRecords,
                        targetDateMillis = selectedDateFilter,
                        waliName = waliName,
                        schoolProfile = schoolProfile
                    )
                    if (file != null) {
                        PdfReportGenerator.shareFastingPdf(
                            context = context,
                            pdfFile = file,
                            subject = "Laporan Kunjungan - ${selectedSession.title}",
                            text = if (isEn) {
                                "Attached is the Parent Visit Attendance Report (${selectedSession.title}: $totalPresentCount attended, ${notVisitedStudents.size} not visited)."
                            } else {
                                "Terlampir Laporan Kehadiran Kunjungan (${selectedSession.title}: $totalPresentCount hadir, ${notVisitedStudents.size} belum berkunjung)."
                            }
                        )
                    } else {
                        Toast.makeText(
                            context,
                            if (isEn) "Failed to generate report PDF." else "Gagal membuat PDF laporan kehadiran.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            onDismiss = { showPdfRewardDialog = false }
        )
    }
}

@Composable
private fun ReportStatPill(
    label: String,
    value: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = fg, maxLines = 1)
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
