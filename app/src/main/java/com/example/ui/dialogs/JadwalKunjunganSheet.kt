@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.TandemWaliHelper
import com.example.data.model.VisitHelper
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionSwap
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JadwalKunjunganSheet(
    sessions: List<VisitSession>,
    students: List<ChildStudent>,
    visitRecords: List<VisitRecord>,
    visitSessionSwaps: List<VisitSessionSwap> = emptyList(),
    isAdmin: Boolean = false,
    currentUsername: String = "",
    waliName: String = "Wali Asuh",
    isAnakSuperAdmin: Boolean = false,
    onOpenDaftarHadir: () -> Unit,
    onOpenLaporan: () -> Unit,
    onSwapStudentSession: ((student: ChildStudent, fromSession: VisitSession, targetSession: VisitSession, reason: String, clearPriorCheckIn: Boolean) -> Unit)? = null,
    onRevertSessionSwap: ((VisitSessionSwap) -> Unit)? = null,
    onEditSession: ((VisitSession) -> Unit)? = null,
    onCreateSession: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val locale = s.language.locale

    val sortedSessions = remember(sessions) {
        sessions.sortedWith(
            compareByDescending<VisitSession> { it.isActive }
                .thenByDescending { it.monthYear }
                .thenByDescending { it.createdAt }
        )
    }

    var selectedSessionId by remember(sortedSessions) {
        mutableStateOf(sortedSessions.firstOrNull()?.id)
    }
    val selectedSession = sortedSessions.firstOrNull { it.id == selectedSessionId } ?: sortedSessions.firstOrNull()

    var selectedDistrictFilter by remember(selectedSession?.id) { mutableStateOf("Semua") }
    var onlyMyFosterStudents by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var swapTargetStudent by remember { mutableStateOf<ChildStudent?>(null) }

    val dateFormatter = remember(locale) { SimpleDateFormat("EEEE, d MMM yyyy", locale) }
    val primaryBlue = Color(0xFF1D4ED8)
    val softBlue = Color(0xFFDBEAFE)

    val scheduledStudents = remember(selectedSession, students, visitSessionSwaps, sortedSessions) {
        if (selectedSession == null) emptyList()
        else VisitHelper.effectiveScheduledStudentsForSession(selectedSession, students, visitSessionSwaps, sortedSessions)
    }
    val myFosterInSessionCount = remember(scheduledStudents, currentUsername, waliName) {
        scheduledStudents.count { st ->
            TandemWaliHelper.hasPrimaryWali(st) && TandemWaliHelper.isAssignedWali(st, currentUsername, waliName)
        }
    }
    val swappedOutPairs = remember(selectedSession, students, visitSessionSwaps, sortedSessions) {
        if (selectedSession == null) emptyList()
        else VisitHelper.swappedOutFromSession(selectedSession, students, visitSessionSwaps, sortedSessions)
    }
    val visibleSwappedOutPairs = remember(swappedOutPairs, onlyMyFosterStudents, currentUsername, waliName) {
        if (!onlyMyFosterStudents) swappedOutPairs
        else swappedOutPairs.filter { (st, _) ->
            TandemWaliHelper.hasPrimaryWali(st) && TandemWaliHelper.isAssignedWali(st, currentUsername, waliName)
        }
    }
    val sessionRecords = remember(visitRecords, selectedSession) {
        if (selectedSession == null) emptyList()
        else visitRecords.filter { it.sessionId == selectedSession.id }
    }
    val filteredStudents = remember(scheduledStudents, selectedDistrictFilter, onlyMyFosterStudents, currentUsername, waliName, searchQuery) {
        val q = searchQuery.trim().lowercase(Locale.US)
        scheduledStudents.filter { st ->
            val key = VisitHelper.resolveStudentDistrictKey(st)
            val distMatch = selectedDistrictFilter == "Semua" || key.equals(selectedDistrictFilter, ignoreCase = true)
            val fosterMatch = !onlyMyFosterStudents || (
                TandemWaliHelper.hasPrimaryWali(st) &&
                    TandemWaliHelper.isAssignedWali(st, currentUsername, waliName)
                )
            val queryMatch = q.isEmpty() ||
                st.name.lowercase(Locale.US).contains(q) ||
                st.classGrade.lowercase(Locale.US).contains(q) ||
                key.lowercase(Locale.US).contains(q)
            distMatch && fosterMatch && queryMatch
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(softBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = primaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Parent Visit Schedule" else "Jadwal Kunjungan Bulanan",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isEn) {
                                    "Monthly sessions, dates & districts"
                                } else {
                                    "Per sesi bulan, tanggal & kecamatan"
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
                            .testTag("btn_close_jadwal_kunjungan")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close, modifier = Modifier.size(18.dp))
                    }
                }

                if (sortedSessions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = primaryBlue)
                                Text(
                                    text = if (isEn) "No Visit Sessions Configured Yet" else "Belum Ada Sesi Kunjungan Terjadwal",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryBlue
                                )
                            }
                            Text(
                                text = if (isEn) {
                                    "Visit sessions are organized monthly by School Admin by picking visit dates (e.g., Week 2, 3, or 4 Sundays) and participating districts (Kecamatan)."
                                } else {
                                    "Sesi kunjungan diatur per bulan oleh Admin Sekolah dengan menentukan beberapa tanggal kunjungan (mis. Minggu ke-2, 3, atau 4) dan memilih kecamatan pesertanya di halaman Admin → Tab Kunjungan."
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF1E3A8A)
                            )
                            if (isAdmin && onCreateSession != null) {
                                Button(
                                    onClick = onCreateSession,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_create_visit_from_jadwal")
                                ) {
                                    Text(if (isEn) "Create Visit Session Now" else "Buat Sesi Kunjungan Sekarang", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Dropdown Pemilih Sesi Bulanan
                    VisitSessionDropdownSelector(
                        sessions = sortedSessions,
                        selectedSession = selectedSession,
                        locale = locale,
                        isEn = isEn,
                        accentColor = primaryBlue,
                        containerColor = Color(0xFFEFF6FF),
                        borderColor = Color(0xFF93C5FD),
                        dropdownTag = "dropdown_jadwal_session",
                        optionTagPrefix = "option_jadwal_session",
                        onSelectSession = { sess -> selectedSessionId = sess.id }
                    )

                    selectedSession?.let { sess ->
                        val datesList = sess.getVisitDatesList()
                        val districtKeys = sess.getDistrictsList()

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .testTag("list_jadwal_kunjungan"),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Card Info Sesi + Multi-Tanggal + Kecamatan
                            item(key = "session_overview") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
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
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = sess.title,
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${VisitHelper.formatMonthYearLabel(sess.monthYear, locale)} • ${scheduledStudents.size} ${if (isEn) "Students Scheduled" else "Siswa Terjadwal"}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = primaryBlue,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                val stage = sess.launchStage()
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = when (stage) {
                                                        com.example.data.model.VisitLaunchStage.PARENT_LIVE -> Color(0xFFDCFCE7)
                                                        com.example.data.model.VisitLaunchStage.WALI_PREVIEW -> Color(0xFFDBEAFE)
                                                        com.example.data.model.VisitLaunchStage.DRAFT -> Color(0xFFFEF3C7)
                                                        com.example.data.model.VisitLaunchStage.CLOSED -> Color(0xFFE2E8F0)
                                                    }
                                                ) {
                                                    Text(
                                                        text = when (stage) {
                                                            com.example.data.model.VisitLaunchStage.PARENT_LIVE -> if (isEn) "Live to Parents" else "Live Ortu"
                                                            com.example.data.model.VisitLaunchStage.WALI_PREVIEW -> if (isEn) "Wali Preview" else "Preview Wali"
                                                            com.example.data.model.VisitLaunchStage.DRAFT -> if (isEn) "Draft" else "Draf Admin"
                                                            com.example.data.model.VisitLaunchStage.CLOSED -> if (isEn) "Closed" else "Selesai"
                                                        },
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (stage) {
                                                            com.example.data.model.VisitLaunchStage.PARENT_LIVE -> Color(0xFF15803D)
                                                            com.example.data.model.VisitLaunchStage.WALI_PREVIEW -> Color(0xFF1D4ED8)
                                                            com.example.data.model.VisitLaunchStage.DRAFT -> Color(0xFFB45309)
                                                            com.example.data.model.VisitLaunchStage.CLOSED -> Color(0xFF475569)
                                                        },
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                                if (isAdmin && onEditSession != null) {
                                                    IconButton(
                                                        onClick = { onEditSession(sess) },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Edit,
                                                            contentDescription = if (isEn) "Edit Session" else "Edit Sesi",
                                                            tint = primaryBlue,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (sess.description.isNotBlank()) {
                                            Text(
                                                text = sess.description,
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Daftar Multi-Tanggal Kunjungan
                                        Text(
                                            text = if (isEn) "Visit Dates (${datesList.size}d):" else "Tanggal Kunjungan (${datesList.size} hari):",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            datesList.forEach { dMillis ->
                                                val isTodayDate = DateUtils.isToday(dMillis)
                                                val dayCheckInCount = sessionRecords.count {
                                                    VisitHelper.normalizeDayStart(it.visitDate) == VisitHelper.normalizeDayStart(dMillis)
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isTodayDate) Color(0xFFDCFCE7) else softBlue,
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (isTodayDate) Color(0xFF16A34A) else Color(0xFF93C5FD)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.EventAvailable,
                                                            contentDescription = null,
                                                            tint = if (isTodayDate) Color(0xFF15803D) else primaryBlue,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Text(
                                                            text = buildString {
                                                                append(dateFormatter.format(Date(dMillis)))
                                                                if (isTodayDate) append(if (isEn) " (Today)" else " (Hari Ini)")
                                                                if (dayCheckInCount > 0) append(" • $dayCheckInCount✓")
                                                            },
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = if (isTodayDate) Color(0xFF15803D) else Color(0xFF1E3A8A)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Kecamatan Peserta Terpilih
                                        Text(
                                            text = if (isEn) "Participating Districts (${districtKeys.size}):" else "Kecamatan Peserta Terjadwal (${districtKeys.size}):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (districtKeys.isEmpty()) {
                                            Text(
                                                text = if (isEn) "No districts assigned yet." else "Belum ada kecamatan yang dipilih Admin.",
                                                fontSize = 11.sp,
                                                color = Color(0xFFDC2626)
                                            )
                                        } else {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                FilterChip(
                                                    selected = selectedDistrictFilter == "Semua",
                                                    onClick = { selectedDistrictFilter = "Semua" },
                                                    label = { Text(if (isEn) "All (${scheduledStudents.size})" else "Semua (${scheduledStudents.size})", fontSize = 11.sp) }
                                                )
                                                districtKeys.forEach { dKey ->
                                                    val cnt = scheduledStudents.count {
                                                        VisitHelper.resolveStudentDistrictKey(it).equals(dKey, ignoreCase = true)
                                                    }
                                                    val isSel = selectedDistrictFilter.equals(dKey, ignoreCase = true)
                                                    FilterChip(
                                                        selected = isSel,
                                                        onClick = { selectedDistrictFilter = if (isSel) "Semua" else dKey },
                                                        label = {
                                                            Text(
                                                                "${VisitHelper.displayDistrict(dKey, isEn)} ($cnt)",
                                                                fontSize = 11.sp,
                                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                                            )
                                                        },
                                                        leadingIcon = {
                                                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(13.dp))
                                                        },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = Color(0xFFCCFBF1),
                                                            selectedLabelColor = Color(0xFF0F766E),
                                                            selectedLeadingIconColor = Color(0xFF0F766E)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Tombol Aksi Cepat ke Daftar Hadir & Laporan
                            item(key = "jadwal_quick_actions") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onOpenDaftarHadir()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            if (isEn) "Attendance" else "Daftar Hadir",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            onDismiss()
                                            onOpenLaporan()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp), tint = primaryBlue)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            if (isEn) "Report" else "Laporan",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryBlue,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            // Filter cepat Semua Siswa vs Binaan Saya (untuk fokus Tukar Sesi anak binaan)
                            if (currentUsername.isNotBlank() || waliName.isNotBlank()) {
                                item(key = "jadwal_foster_scope") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val allSel = !onlyMyFosterStudents
                                        Surface(
                                            onClick = { onlyMyFosterStudents = false },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (allSel) primaryBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .testTag("chip_jadwal_scope_all")
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = if (isEn) "All (${scheduledStudents.size})" else "Semua Siswa (${scheduledStudents.size})",
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
                                            color = if (!allSel) Color(0xFF0F766E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .testTag("chip_jadwal_scope_foster")
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = if (isEn) "My Foster ($myFosterInSessionCount)" else "Binaan Saya ($myFosterInSessionCount)",
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
                            }

                            // Cari Siswa Terjadwal (1 baris ringkas)
                            item(key = "jadwal_search") {
                                CompactVisitInputField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = if (isEn) "Search student or district..." else "Cari siswa atau kecamatan...",
                                    leadingIcon = Icons.Default.Search,
                                    clearDescription = s.nav.close,
                                    modifier = Modifier.testTag("input_jadwal_search")
                                )
                            }

                            if (filteredStudents.isEmpty()) {
                                item(key = "jadwal_students_empty") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isEn) "No scheduled students match this filter." else "Belum ada siswa terjadwal untuk filter ini.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                itemsIndexed(filteredStudents, key = { _, st -> "jadwal_st_${st.id}" }) { idx, st ->
                                    val rec = VisitHelper.findVisitForStudent(sessionRecords, st)
                                    val distLabel = VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(st), isEn)
                                    val incomingSwap = VisitHelper.findIncomingSwapToSession(sess.id, st, visitSessionSwaps)
                                    val canSwap = onSwapStudentSession != null && VisitHelper.canWaliSwapStudent(
                                        student = st,
                                        isAdmin = isAdmin,
                                        currentUsername = currentUsername,
                                        currentWaliName = waliName,
                                        isAnakSuperAdmin = isAnakSuperAdmin
                                    )
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (rec != null) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (rec != null) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(if (rec != null) Color(0xFFDCFCE7) else softBlue),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${idx + 1}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (rec != null) Color(0xFF15803D) else primaryBlue
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = st.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "$distLabel • ${st.classGrade.ifBlank { "-" }} • ${st.dormitory.ifBlank { "-" }}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (canSwap) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFCCFBF1),
                                                        modifier = Modifier.testTag("badge_jadwal_my_foster_${st.id}")
                                                    ) {
                                                        Text(
                                                            text = if (isEn) "My Foster Child" else "Anak Binaan Saya",
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF0F766E),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                if (incomingSwap != null) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFEDE9FE),
                                                        border = BorderStroke(1.dp, Color(0xFFC4B5FD)),
                                                        modifier = Modifier.testTag("badge_jadwal_swapped_in_${st.id}")
                                                    ) {
                                                        Text(
                                                            text = if (isEn) {
                                                                "Moved from ${incomingSwap.fromSessionTitle}"
                                                            } else {
                                                                "Pindahan dari ${incomingSwap.fromSessionTitle}"
                                                            },
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF6D28D9),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (rec != null) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            tint = Color(0xFF16A34A),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Text(
                                                            text = if (isEn) "Visited" else "Hadir",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF15803D)
                                                        )
                                                    }
                                                }
                                                if (canSwap) {
                                                    OutlinedButton(
                                                        onClick = { swapTargetStudent = st },
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                        modifier = Modifier
                                                            .heightIn(min = 48.dp)
                                                            .testTag("btn_jadwal_swap_${st.id}")
                                                    ) {
                                                        Icon(
                                                            Icons.Default.SwapHoriz,
                                                            contentDescription = if (isEn) "Move Session" else "Tukar Sesi",
                                                            tint = primaryBlue,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = if (isEn) "Swap" else "Tukar",
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = primaryBlue
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (visibleSwappedOutPairs.isNotEmpty()) {
                                item(key = "jadwal_swapped_out_hdr") {
                                    Text(
                                        text = if (isEn) {
                                            "Moved to Another Session (${visibleSwappedOutPairs.size}):"
                                        } else {
                                            "Dipindah ke Sesi Lain (${visibleSwappedOutPairs.size}):"
                                        },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6D28D9),
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                                itemsIndexed(visibleSwappedOutPairs, key = { _, pair -> "jadwal_out_${pair.first.id}" }) { _, pair ->
                                    val st = pair.first
                                    val swap = pair.second
                                    val canRevert = onRevertSessionSwap != null && VisitHelper.canWaliSwapStudent(
                                        student = st,
                                        isAdmin = isAdmin,
                                        currentUsername = currentUsername,
                                        currentWaliName = waliName,
                                        isAnakSuperAdmin = isAnakSuperAdmin
                                    )
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("card_jadwal_swapped_out_${st.id}"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                                        border = BorderStroke(1.dp, Color(0xFFDDD6FE))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(st.name, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(
                                                    text = if (isEn) {
                                                        "→ ${swap.toSessionTitle}" + if (swap.reason.isNotBlank()) " • ${swap.reason}" else ""
                                                    } else {
                                                        "→ Dipindah ke: ${swap.toSessionTitle}" + if (swap.reason.isNotBlank()) " • ${swap.reason}" else ""
                                                    },
                                                    fontSize = 10.5.sp,
                                                    color = Color(0xFF5B21B6),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            if (canRevert) {
                                                OutlinedButton(
                                                    onClick = { onRevertSessionSwap?.invoke(swap) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier
                                                        .heightIn(min = 48.dp)
                                                        .testTag("btn_jadwal_revert_swap_${st.id}")
                                                ) {
                                                    Icon(Icons.Default.Restore, contentDescription = null, tint = Color(0xFF6D28D9), modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(if (isEn) "Restore" else "Kembalikan", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9))
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
    }

    val swapStudent = swapTargetStudent
    val activeSess = selectedSession
    if (swapStudent != null && activeSess != null && onSwapStudentSession != null) {
        val priorRec = VisitHelper.findVisitForStudent(sessionRecords, swapStudent)
        val existingIncoming = VisitHelper.findIncomingSwapToSession(activeSess.id, swapStudent, visitSessionSwaps)
        VisitSessionSwapDialog(
            student = swapStudent,
            currentSession = activeSess,
            allSessions = sortedSessions,
            priorRecordInCurrentSession = priorRec,
            existingIncomingSwap = existingIncoming,
            onConfirmSwap = { targetSession, reason, clearPrior ->
                swapTargetStudent = null
                onSwapStudentSession(swapStudent, activeSess, targetSession, reason, clearPrior)
            },
            onRevertSwap = if (onRevertSessionSwap != null) {
                { swap ->
                    swapTargetStudent = null
                    onRevertSessionSwap(swap)
                }
            } else null,
            onDismiss = { swapTargetStudent = null }
        )
    }
}

@Composable
internal fun CompactVisitSheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(34.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f))
        )
    }
}

@Composable
internal fun VisitSessionDropdownSelector(
    sessions: List<VisitSession>,
    selectedSession: VisitSession?,
    locale: Locale,
    isEn: Boolean,
    accentColor: Color,
    containerColor: Color,
    borderColor: Color,
    dropdownTag: String,
    optionTagPrefix: String,
    onSelectSession: (VisitSession) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val active = selectedSession ?: sessions.firstOrNull() ?: return
    val monthLabel = VisitHelper.formatMonthYearLabel(active.monthYear, locale)

    Box(modifier = Modifier.fillMaxWidth()) {
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
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = active.title,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$monthLabel • ${active.displayTimeRange()}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (sessions.size > 1) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (isEn) "${sessions.size} Sessions" else "${sessions.size} Sesi",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isEn) "Select session" else "Pilih sesi",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 260.dp, max = 420.dp)
        ) {
            sessions.forEachIndexed { idx, sess ->
                val isSel = sess.id == active.id
                val sessMonth = VisitHelper.formatMonthYearLabel(sess.monthYear, locale)
                DropdownMenuItem(
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = sess.title,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) accentColor else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$sessMonth • ${sess.displayTimeRange()}",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = if (isSel) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = if (isSel) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    onClick = {
                        expanded = false
                        onSelectSession(sess)
                    },
                    modifier = Modifier.testTag("${optionTagPrefix}_${sess.id}")
                )
                if (idx < sessions.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
internal fun CompactVisitInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    clearDescription: String = "Clear",
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (value.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onValueChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = clearDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

