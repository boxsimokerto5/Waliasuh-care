@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.VisitHelper
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
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
    isAdmin: Boolean = false,
    onOpenDaftarHadir: () -> Unit,
    onOpenLaporan: () -> Unit,
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
    var searchQuery by remember { mutableStateOf("") }

    val dateFormatter = remember(locale) { SimpleDateFormat("EEEE, d MMM yyyy", locale) }
    val primaryBlue = Color(0xFF1D4ED8)
    val softBlue = Color(0xFFDBEAFE)

    val scheduledStudents = remember(selectedSession, students) {
        if (selectedSession == null) emptyList()
        else VisitHelper.scheduledStudentsForSession(selectedSession, students)
    }
    val sessionRecords = remember(visitRecords, selectedSession) {
        if (selectedSession == null) emptyList()
        else visitRecords.filter { it.sessionId == selectedSession.id }
    }
    val filteredStudents = remember(scheduledStudents, selectedDistrictFilter, searchQuery) {
        val q = searchQuery.trim().lowercase(Locale.US)
        scheduledStudents.filter { st ->
            val key = VisitHelper.resolveStudentDistrictKey(st)
            val distMatch = selectedDistrictFilter == "Semua" || key.equals(selectedDistrictFilter, ignoreCase = true)
            val queryMatch = q.isEmpty() ||
                st.name.lowercase(Locale.US).contains(q) ||
                st.classGrade.lowercase(Locale.US).contains(q) ||
                key.lowercase(Locale.US).contains(q)
            distMatch && queryMatch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
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
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(softBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = primaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "Parent Visit Schedule" else "Jadwal Kunjungan Bulanan",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEn) {
                                    "Monthly sessions, multi-date schedule & selected districts"
                                } else {
                                    "Jadwal per sesi bulan, multi-tanggal & kecamatan peserta"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_jadwal_kunjungan")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close, modifier = Modifier.size(20.dp))
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
                    // Selector Sesi Bulanan
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sortedSessions.forEach { sess ->
                            val selected = selectedSession?.id == sess.id
                            FilterChip(
                                selected = selected,
                                onClick = { selectedSessionId = sess.id },
                                label = {
                                    Text(
                                        text = sess.title,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = softBlue,
                                    selectedLabelColor = primaryBlue,
                                    selectedLeadingIconColor = primaryBlue
                                ),
                                modifier = Modifier
                                    .heightIn(min = 36.dp)
                                    .testTag("chip_jadwal_session_${sess.id}")
                            )
                        }
                    }

                    selectedSession?.let { sess ->
                        val datesList = sess.getVisitDatesList()
                        val districtKeys = sess.getDistrictsList()

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .testTag("list_jadwal_kunjungan"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Card Info Sesi + Multi-Tanggal + Kecamatan
                            item(key = "session_overview") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = sess.title,
                                                    fontSize = 14.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${VisitHelper.formatMonthYearLabel(sess.monthYear, locale)} • ${scheduledStudents.size} ${if (isEn) "Students Scheduled" else "Siswa Terjadwal"}",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = primaryBlue
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (sess.isActive) Color(0xFFDCFCE7) else Color(0xFFE2E8F0)
                                                ) {
                                                    Text(
                                                        text = if (sess.isActive) {
                                                            if (isEn) "Active" else "Aktif"
                                                        } else {
                                                            if (isEn) "Closed" else "Selesai"
                                                        },
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (sess.isActive) Color(0xFF15803D) else Color(0xFF475569),
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
                                            text = if (isEn) "Scheduled Dates (${datesList.size} days):" else "Tanggal Pelaksanaan Kunjungan (${datesList.size} hari):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            datesList.forEach { dMillis ->
                                                val isTodayDate = DateUtils.isToday(dMillis)
                                                val dayCheckInCount = sessionRecords.count {
                                                    VisitHelper.normalizeDayStart(it.visitDate) == VisitHelper.normalizeDayStart(dMillis)
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isTodayDate) Color(0xFFDCFCE7) else softBlue,
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (isTodayDate) Color(0xFF16A34A) else Color(0xFF93C5FD)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.EventAvailable,
                                                            contentDescription = null,
                                                            tint = if (isTodayDate) Color(0xFF15803D) else primaryBlue,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Text(
                                                            text = buildString {
                                                                append(dateFormatter.format(Date(dMillis)))
                                                                if (isTodayDate) append(if (isEn) " (Today)" else " (Hari Ini)")
                                                                if (dayCheckInCount > 0) append(" • $dayCheckInCount✓")
                                                            },
                                                            fontSize = 11.sp,
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

                            // Cari Siswa Terjadwal
                            item(key = "jadwal_search") {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            if (isEn) "Search scheduled student or district..." else "Cari siswa terjadwal atau kecamatan...",
                                            fontSize = 12.sp
                                        )
                                    },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = s.nav.close, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
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
                                            Column(modifier = Modifier.weight(1f)) {
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
                                            }
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
