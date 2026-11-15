@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.SchoolProfile
import com.example.data.model.VisitHelper
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import com.example.util.StudentClassifier
import com.example.util.TopOnAdManager
import com.example.util.i18n.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class VisitPdfExportAction {
    PRINT,
    OPEN,
    SHARE
}

@Composable
fun DaftarHadirKunjunganSheet(
    sessions: List<VisitSession>,
    students: List<ChildStudent>,
    visitRecords: List<VisitRecord>,
    schoolProfile: SchoolProfile,
    waliName: String,
    initialTab: Int = 0,
    onRecordVisitCheckIn: (
        session: VisitSession,
        student: ChildStudent,
        visitDate: Long,
        visitorRelation: String,
        visitorName: String,
        checkInMethod: String,
        onResult: (added: Boolean, message: String, record: VisitRecord?) -> Unit
    ) -> Unit,
    onDeleteVisitRecord: (VisitRecord) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val locale = s.language.locale

    val tealPrimary = Color(0xFF0F766E)
    val tealLight = Color(0xFFCCFBF1)
    val indigoPrimary = Color(0xFF1D4ED8)
    val indigoLight = Color(0xFFDBEAFE)

    val sortedSessions = remember(sessions) {
        sessions.sortedWith(
            compareByDescending<VisitSession> { it.isActive }
                .thenByDescending { it.monthYear }
                .thenByDescending { it.createdAt }
        )
    }

    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var selectedSessionId by remember(sortedSessions) { mutableStateOf(sortedSessions.firstOrNull()?.id) }
    val selectedSession = sortedSessions.firstOrNull { it.id == selectedSessionId } ?: sortedSessions.firstOrNull()

    val dateSlots = remember(selectedSession) {
        val today = VisitHelper.normalizeDayStart(System.currentTimeMillis())
        val base = selectedSession?.getVisitDatesList().orEmpty()
        (base + today).distinct().sorted()
    }
    var selectedVisitDateMillis by remember(selectedSession?.id, dateSlots) {
        val today = VisitHelper.normalizeDayStart(System.currentTimeMillis())
        val sessionDates = selectedSession?.getVisitDatesList().orEmpty()
        val best = sessionDates.firstOrNull { it == today } ?: sessionDates.firstOrNull() ?: today
        mutableStateOf(best)
    }

    var selectedVisitorRelation by remember { mutableStateOf("Ibu") }
    var visitorNameInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDistrictFilter by remember(selectedSession?.id) { mutableStateOf("Semua") }

    var showBarcodeScanner by remember { mutableStateOf(false) }
    var scannerFeedback by remember { mutableStateOf<ScannerFloatingFeedback?>(null) }
    var outOfScheduleStudentConfirm by remember { mutableStateOf<Pair<ChildStudent, String>?>(null) }
    var pendingPdfAction by remember { mutableStateOf<VisitPdfExportAction?>(null) }

    val shortDateFmt = remember(locale) { SimpleDateFormat("EEE, d MMM", locale) }
    val timeFmt = remember(locale) { SimpleDateFormat("HH:mm", locale) }

    val sessionRecordsForDate = remember(visitRecords, selectedSession, selectedVisitDateMillis) {
        if (selectedSession == null) emptyList()
        else {
            val day = VisitHelper.normalizeDayStart(selectedVisitDateMillis)
            visitRecords
                .filter { it.sessionId == selectedSession.id && VisitHelper.normalizeDayStart(it.visitDate) == day }
                .sortedByDescending { it.checkedInAt }
        }
    }

    val scheduledStudents = remember(selectedSession, students) {
        if (selectedSession == null) emptyList()
        else {
            val byDistrict = VisitHelper.scheduledStudentsForSession(selectedSession, students)
            byDistrict.ifEmpty { students.sortedBy { it.name.lowercase(Locale.US) } }
        }
    }

    val filteredManualRoster = remember(scheduledStudents, selectedDistrictFilter, searchQuery) {
        val q = searchQuery.trim().lowercase(Locale.US)
        scheduledStudents.filter { st ->
            val key = VisitHelper.resolveStudentDistrictKey(st)
            val distMatch = selectedDistrictFilter == "Semua" || key.equals(selectedDistrictFilter, ignoreCase = true)
            val queryMatch = q.isEmpty() ||
                st.name.lowercase(Locale.US).contains(q) ||
                st.classGrade.lowercase(Locale.US).contains(q) ||
                st.nik.contains(q) ||
                st.nis.lowercase(Locale.US).contains(q) ||
                key.lowercase(Locale.US).contains(q)
            distMatch && queryMatch
        }
    }

    val scanSearchResults = remember(students, searchQuery) {
        val q = searchQuery.trim().lowercase(Locale.US)
        if (q.isEmpty()) scheduledStudents.take(8)
        else students.filter { st ->
            st.name.lowercase(Locale.US).contains(q) ||
                st.nik.contains(q) ||
                st.nis.lowercase(Locale.US).contains(q) ||
                st.classGrade.lowercase(Locale.US).contains(q) ||
                VisitHelper.normalizeDistrict(st.district).lowercase(Locale.US).contains(q)
        }.take(20)
    }

    fun triggerCheckIn(student: ChildStudent, method: String, bypassOutOfScheduleCheck: Boolean = false) {
        val sess = selectedSession ?: return
        val outOfSchedule = sess.getDistrictsList().isNotEmpty() &&
            !VisitHelper.isStudentInSessionSchedule(sess, student)
        if (outOfSchedule && !bypassOutOfScheduleCheck) {
            outOfScheduleStudentConfirm = student to method
            return
        }
        onRecordVisitCheckIn(
            sess,
            student,
            selectedVisitDateMillis,
            selectedVisitorRelation,
            visitorNameInput,
            method
        ) { added, message, _ ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            val dist = VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(student), isEn)
            scannerFeedback = if (added) {
                ScannerFloatingFeedback(
                    id = System.nanoTime(),
                    status = ScannerFeedbackStatus.SUCCESS,
                    title = if (isEn) "Checked in: ${student.name}" else "Hadir Kunjungan: ${student.name}",
                    subtitle = "$dist • ${student.classGrade}"
                )
            } else {
                ScannerFloatingFeedback(
                    id = System.nanoTime(),
                    status = ScannerFeedbackStatus.WARNING,
                    title = if (isEn) "Already checked in" else "Sudah tercatat hadir",
                    subtitle = message
                )
            }
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(tealLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HowToReg, contentDescription = null, tint = tealPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "Parent Visit Attendance" else "Daftar Hadir Kunjungan",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEn) "Barcode Scan & Printable Manual PDF Sheet" else "2 Kategori: Scan Barcode & List Cetak PDF Manual",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_daftar_hadir_kunjungan")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close, modifier = Modifier.size(20.dp))
                    }
                }

                // 2 Kategori Tab: Scan Barcode vs List & PDF Manual
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.testTag("tab_visit_scan"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(15.dp))
                                Text(
                                    text = if (isEn) "Barcode Scan" else "Scan Barcode",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.testTag("tab_visit_manual_pdf"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                                Text(
                                    text = if (isEn) "List & Manual PDF" else "List & PDF Manual",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    )
                }

                if (sortedSessions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (isEn) {
                                "No visit sessions exist yet. Ask School Admin to create a monthly visit session & pick districts in Admin → Visit tab."
                            } else {
                                "Belum ada sesi kunjungan. Admin Sekolah perlu membuat sesi kunjungan bulanan & memilih kecamatan di halaman Admin → Tab Kunjungan."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    // Selector Sesi & Tanggal Kunjungan
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sortedSessions.forEach { sess ->
                            val isSel = selectedSession?.id == sess.id
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedSessionId = sess.id },
                                label = { Text(sess.title, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = indigoLight,
                                    selectedLabelColor = indigoPrimary
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Visit Date:" else "Tanggal:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        dateSlots.forEach { slotMillis ->
                            val isSel = VisitHelper.normalizeDayStart(selectedVisitDateMillis) == VisitHelper.normalizeDayStart(slotMillis)
                            val labelText = buildString {
                                append(shortDateFmt.format(Date(slotMillis)))
                                if (DateUtils.isToday(slotMillis)) append(if (isEn) " (Today)" else " (Hari Ini)")
                            }
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedVisitDateMillis = slotMillis },
                                label = { Text(labelText, fontSize = 10.5.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tealLight,
                                    selectedLabelColor = tealPrimary,
                                    selectedLeadingIconColor = tealPrimary
                                )
                            )
                        }
                    }

                    if (selectedTab == 0) {
                        // ==========================================
                        // KATEGORI 1: BARCODE SCAN CHECK-IN
                        // ==========================================
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .testTag("list_visit_barcode_tab"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item(key = "scan_visitor_controls") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                                    border = BorderStroke(1.dp, Color(0xFF99F6E4))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (isEn) "Visitor Relation & Quick Barcode Scan:" else "Hubungan Pengunjung & Scan Kartu:",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tealPrimary
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("Ibu", "Ayah", "Wali / Keluarga").forEach { rel ->
                                                val isSel = selectedVisitorRelation == rel
                                                FilterChip(
                                                    selected = isSel,
                                                    onClick = { selectedVisitorRelation = rel },
                                                    label = { Text(rel, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = tealPrimary,
                                                        selectedLabelColor = Color.White
                                                    )
                                                )
                                            }
                                        }

                                        OutlinedTextField(
                                            value = visitorNameInput,
                                            onValueChange = { visitorNameInput = it },
                                            label = { Text(if (isEn) "Visitor Name (Optional)" else "Nama Orang Tua / Wali (Opsional)", fontSize = 11.sp) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Button(
                                            onClick = { showBarcodeScanner = true },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = tealPrimary),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_open_visit_scanner")
                                        ) {
                                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isEn) "Scan Student Barcode Now" else "Scan Barcode Kartu Siswa",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            item(key = "scan_search_box") {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text(if (isEn) "Or search student name / NIK to check in..." else "Atau cari nama / NIK / kecamatan siswa...", fontSize = 12.sp) },
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

                            item(key = "checked_in_hdr") {
                                Text(
                                    text = if (isEn) {
                                        "Checked-in on Selected Date (${sessionRecordsForDate.size} Students)"
                                    } else {
                                        "Sudah Hadir Tanggal Ini (${sessionRecordsForDate.size} Siswa)"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }

                            if (sessionRecordsForDate.isNotEmpty()) {
                                itemsIndexed(sessionRecordsForDate, key = { _, rec -> "visit_checked_${rec.id}" }) { idx, rec ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${idx + 1}.",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF15803D)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(rec.studentName, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                val dist = VisitHelper.displayDistrict(rec.studentDistrict, isEn)
                                                val visitorDesc = listOf(rec.visitorRelation, rec.visitorName).filter { it.isNotBlank() }.joinToString(": ")
                                                Text(
                                                    text = "$dist • ${timeFmt.format(Date(rec.checkedInAt))} WIB • $visitorDesc (${rec.checkInMethod})",
                                                    fontSize = 10.5.sp,
                                                    color = Color(0xFF166534),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteVisitRecord(rec) },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.DeleteOutline,
                                                    contentDescription = if (isEn) "Remove Check-in" else "Hapus Kehadiran",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item(key = "quick_pick_students_hdr") {
                                Text(
                                    text = if (isEn) "Quick Tap Student Check-in:" else "Pilih Cepat Siswa untuk Catat Hadir:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            itemsIndexed(scanSearchResults, key = { _, st -> "visit_quick_${st.id}" }) { _, st ->
                                val existingRec = VisitHelper.findVisitForStudent(sessionRecordsForDate, st, selectedVisitDateMillis)
                                val dist = VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(st), isEn)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = existingRec == null) {
                                            triggerCheckIn(st, VisitRecord.METHOD_BARCODE)
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (existingRec != null) Color(0xFFF8FAFC) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(st.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(
                                                text = "$dist • ${st.classGrade.ifBlank { "-" }} • ${st.dormitory.ifBlank { "-" }}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (existingRec != null) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                        } else {
                                            Text(
                                                text = if (isEn) "+ Check in" else "+ Hadir",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tealPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // ==========================================
                        // KATEGORI 2: LIST & CETAK PDF MANUAL
                        // ==========================================
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .testTag("list_visit_manual_tab"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item(key = "manual_pdf_actions") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = if (isEn) {
                                                "Printable Manual Attendance Roster (Includes District & Signature Columns)"
                                            } else {
                                                "Cetak PDF Daftar Hadir Manual (Lengkap Kolom Jam & Tanda Tangan)"
                                            },
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = indigoPrimary
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Button(
                                                onClick = { pendingPdfAction = VisitPdfExportAction.PRINT },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = indigoPrimary),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("btn_print_visit_manual_pdf")
                                            ) {
                                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isEn) "Print" else "Cetak PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                            Button(
                                                onClick = { pendingPdfAction = VisitPdfExportAction.OPEN },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("btn_open_visit_manual_pdf")
                                            ) {
                                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isEn) "Download" else "Unduh PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                            OutlinedButton(
                                                onClick = { pendingPdfAction = VisitPdfExportAction.SHARE },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .weight(0.95f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("btn_share_visit_manual_pdf")
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp), tint = indigoPrimary)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isEn) "Share" else "Bagikan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = indigoPrimary, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }

                            selectedSession?.let { sess ->
                                val distList = sess.getDistrictsList()
                                if (distList.isNotEmpty()) {
                                    item(key = "manual_dist_filter") {
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
                                            distList.forEach { dKey ->
                                                val isSel = selectedDistrictFilter.equals(dKey, ignoreCase = true)
                                                FilterChip(
                                                    selected = isSel,
                                                    onClick = { selectedDistrictFilter = if (isSel) "Semua" else dKey },
                                                    label = { Text(VisitHelper.displayDistrict(dKey, isEn), fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item(key = "manual_search") {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text(if (isEn) "Filter roster list..." else "Cari nama siswa / kecamatan di daftar...", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            itemsIndexed(filteredManualRoster, key = { _, st -> "manual_row_${st.id}" }) { idx, st ->
                                val matchedRec = VisitHelper.findVisitForStudent(sessionRecordsForDate, st, selectedVisitDateMillis)
                                val distLabel = VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(st), isEn)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (matchedRec != null) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (matchedRec != null) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant
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
                                            Text(
                                                text = "${idx + 1}.",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = indigoPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(st.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(
                                                    text = "$distLabel • ${st.classGrade.ifBlank { "-" }} • ${st.dormitory.ifBlank { "-" }}",
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        if (matchedRec != null) {
                                            Surface(
                                                onClick = { onDeleteVisitRecord(matchedRec) },
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFDCFCE7)
                                            ) {
                                                Text(
                                                    text = "✓ ${timeFmt.format(Date(matchedRec.checkedInAt))}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                                )
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { triggerCheckIn(st, VisitRecord.METHOD_MANUAL) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.heightIn(min = 36.dp)
                                            ) {
                                                Text(
                                                    text = if (isEn) "Mark Present" else "Hadir Manual",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = tealPrimary
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

    if (showBarcodeScanner && selectedSession != null) {
        BarcodeScannerDialog(
            femaleStudents = students,
            onScanned = { scannedCode ->
                val matched = StudentClassifier.findMatchingStudent(students, scannedCode)
                if (matched == null) {
                    scannerFeedback = ScannerFloatingFeedback(
                        id = System.nanoTime(),
                        status = ScannerFeedbackStatus.ERROR,
                        title = if (isEn) "Card Not Found" else "Kartu Tidak Dikenal",
                        subtitle = if (isEn) "Code \"$scannedCode\" is not registered in this school" else "Kode \"$scannedCode\" tidak ditemukan di data sekolah"
                    )
                } else {
                    triggerCheckIn(matched, VisitRecord.METHOD_BARCODE, bypassOutOfScheduleCheck = true)
                }
            },
            onDismiss = {
                showBarcodeScanner = false
                scannerFeedback = null
            },
            dialogTitle = if (isEn) "Scan Parent Visit Check-in" else "Scan Barcode Kehadiran Kunjungan",
            dialogSubtitle = if (isEn) {
                "Session: ${selectedSession.title} • Camera stays active"
            } else {
                "Sesi: ${selectedSession.title} • Kamera tetap aktif untuk antrean tamu"
            },
            quickPickTitle = if (isEn) "Quick Pick Student (${students.size}):" else "Pilih Cepat Siswa (${students.size}):",
            searchPlaceholder = if (isEn) "Search name, class, district, or NIK..." else "Cari nama, kelas, kecamatan, atau NIK...",
            emptyListMessage = if (isEn) "No students registered in this school yet." else "Belum ada data siswa terdaftar.",
            accentColor = tealPrimary,
            accentBgColor = tealLight,
            cardBgColor = Color(0xFFF0FDFA),
            avatarBgColor = Color(0xFF99F6E4),
            avatarTextColor = tealPrimary,
            titleTextColor = Color(0xFF134E4A),
            continuousScan = true,
            sessionParticipantCount = sessionRecordsForDate.size,
            floatingFeedback = scannerFeedback
        )
    }

    outOfScheduleStudentConfirm?.let { (student, method) ->
        val stDist = VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(student), isEn)
        AlertDialog(
            onDismissRequest = { outOfScheduleStudentConfirm = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706)) },
            title = {
                Text(
                    if (isEn) "Outside Scheduled District" else "Di Luar Jadwal Kecamatan Sesi",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (isEn) {
                        "${student.name} belongs to district \"$stDist\", which is not in this session's scheduled district list. Record check-in anyway?"
                    } else {
                        "${student.name} berasal dari kecamatan \"$stDist\" yang belum termasuk daftar kecamatan sesi ini. Tetap catat hadir berkunjung?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        outOfScheduleStudentConfirm = null
                        triggerCheckIn(student, method, bypassOutOfScheduleCheck = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = tealPrimary)
                ) {
                    Text(if (isEn) "Record Anyway" else "Tetap Catat Hadir", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { outOfScheduleStudentConfirm = null }) {
                    Text(s.cancel)
                }
            }
        )
    }

    pendingPdfAction?.let { action ->
        val sess = selectedSession
        if (sess != null) {
            RewardAdDeclarationDialog(
                title = if (isEn) "Manual Visit Attendance PDF" else "PDF Daftar Hadir Kunjungan",
                actionDescription = if (isEn) {
                    "generate the Manual Attendance Sheet PDF for ${sess.title}"
                } else {
                    "membuat PDF Daftar Hadir Manual Kunjungan untuk ${sess.title}"
                },
                confirmButtonText = if (isEn) "Watch & Generate PDF" else "Tonton & Buat PDF",
                onConfirmWatchAd = {
                    pendingPdfAction = null
                    TopOnAdManager.showRewardAdForPdfReport(context) {
                        val file = PdfReportGenerator.generateVisitManualRosterPdf(
                            context = context,
                            session = sess,
                            scheduledStudents = filteredManualRoster,
                            existingRecords = sessionRecordsForDate,
                            targetDateMillis = selectedVisitDateMillis,
                            waliName = waliName,
                            schoolProfile = schoolProfile
                        )
                        if (file == null) {
                            Toast.makeText(
                                context,
                                if (isEn) "Failed to generate PDF roster." else "Gagal membuat PDF daftar hadir.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            when (action) {
                                VisitPdfExportAction.PRINT -> {
                                    PdfReportGenerator.printPdfWithSystemPrintManager(
                                        context = context,
                                        pdfFile = file,
                                        jobName = "Daftar Hadir Kunjungan - ${sess.title}"
                                    )
                                }
                                VisitPdfExportAction.OPEN -> {
                                    PdfReportGenerator.openPdfFile(context, file)
                                }
                                VisitPdfExportAction.SHARE -> {
                                    PdfReportGenerator.shareFastingPdf(
                                        context = context,
                                        pdfFile = file,
                                        subject = "Daftar Hadir Kunjungan - ${sess.title}",
                                        text = if (isEn) {
                                            "Attached is the Parent Visit Manual Attendance Roster (${sess.title}, ${filteredManualRoster.size} students)."
                                        } else {
                                            "Terlampir Daftar Hadir Manual Kunjungan Orang Tua (${sess.title}, ${filteredManualRoster.size} siswa)."
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                onDismiss = { pendingPdfAction = null }
            )
        } else {
            pendingPdfAction = null
        }
    }
}
