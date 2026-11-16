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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ChildStudent
import com.example.data.model.KonselingHelper
import com.example.data.model.KonselingLog
import com.example.data.model.KonselingRecord
import com.example.data.model.SchoolProfile
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import com.example.util.i18n.LocalAppStrings
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KonselingFollowUpSheet(
    records: List<KonselingRecord>,
    onOpenStudentById: (Long) -> Unit,
    onToggleStatus: (KonselingRecord) -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language
    var filterMode by remember { mutableIntStateOf(0) } // 0=All Active, 1=High Priority, 2=Scheduled

    val activeFollowUps = remember(records, filterMode) {
        records
            .filter { !KonselingHelper.isCompleted(it.status) }
            .filter { rec ->
                when (filterMode) {
                    1 -> KonselingHelper.isHighPriority(rec.urgencyLevel)
                    2 -> rec.followUpDate != null || rec.followUpPlan.isNotBlank()
                    else -> true
                }
            }
            .sortedWith(
                compareBy<KonselingRecord> {
                    when (it.urgencyLevel.uppercase()) {
                        KonselingHelper.URGENCY_PRIORITAS -> 0
                        KonselingHelper.URGENCY_PANTAU -> 1
                        else -> 2
                    }
                }.thenBy { it.followUpDate ?: Long.MAX_VALUE }
                    .thenByDescending { it.sessionDate }
            )
    }

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
        Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Follow-Up & Active Monitoring" else "Pantauan & Tindak Lanjut",
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6D28D9), modifier = Modifier.size(12.dp))
                            Text(
                                text = if (isEn) "Confidential • Owner Wali Asuh only" else "Rahasia • Hanya dilihat Wali Asuh bersangkutan",
                                fontSize = 11.sp,
                                color = Color(0xFF6D28D9),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_konseling_followup")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        if (isEn) "All Monitored" else "Semua Pantauan",
                        if (isEn) "High Priority" else "Prioritas Tinggi",
                        if (isEn) "Scheduled" else "Ada Jadwal"
                    ).forEachIndexed { idx, label ->
                        FilterChip(
                            selected = filterMode == idx,
                            onClick = { filterMode = idx },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_followup_filter_$idx")
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activeFollowUps.isEmpty()) {
                        item {
                            Text(
                                text = if (isEn) "No active counseling follow-ups." else "Tidak ada kasus konseling yang sedang perlu tindak lanjut.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        items(activeFollowUps, key = { "k_fu_${it.id}" }) { rec ->
                            val isHigh = KonselingHelper.isHighPriority(rec.urgencyLevel)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenStudentById(rec.studentId) }
                                    .testTag("card_followup_konseling_${rec.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, if (isHigh) Color(0xFFFCA5A5) else Color(0xFFFDE68A))
                            ) {
                                Column(
                                    modifier = Modifier.padding(11.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${rec.studentName} (${rec.studentClass.ifBlank { "-" }})",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        KonselingBadgePill(
                                            text = KonselingHelper.formatUrgencyLabel(rec.urgencyLevel, isEn),
                                            bg = if (isHigh) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                                            fg = if (isHigh) Color(0xFFB91C1C) else Color(0xFFB45309)
                                        )
                                    }
                                    Text(
                                        text = rec.title.ifBlank { KonselingHelper.formatTopicLabel(rec.topic, isEn) },
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF4C1D95)
                                    )
                                    if (rec.followUpPlan.isNotBlank()) {
                                        Text(
                                            text = rec.followUpPlan,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = rec.followUpDate?.let { DateUtils.formatShortDate(it, lang) }
                                                    ?: DateUtils.formatShortDate(rec.sessionDate, lang),
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Button(
                                            onClick = { onToggleStatus(rec) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                            contentPadding = PaddingValues(horizontal = 10.dp),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_followup_resolve_${rec.id}")
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isEn) "Resolve" else "Selesai", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KonselingHistoryLogsSheet(
    records: List<KonselingRecord>,
    logs: List<KonselingLog>,
    studentsForSummary: List<ChildStudent>,
    waliName: String,
    schoolProfile: SchoolProfile,
    initialTab: Int = 0,
    onOpenStudentById: (Long) -> Unit,
    onEditRecord: (KonselingRecord) -> Unit,
    onToggleStatus: (KonselingRecord) -> Unit,
    onDeleteRecord: (KonselingRecord) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language

    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var searchQuery by remember { mutableStateOf("") }
    var recordFilter by remember { mutableIntStateOf(0) } // 0=All, 1=Sesi, 2=Perkembangan, 3=Pantau, 4=Selesai
    var recordToDelete by remember { mutableStateOf<KonselingRecord?>(null) }
    var readyPdfFile by remember { mutableStateOf<File?>(null) }

    val filteredRecords = remember(records, searchQuery, recordFilter) {
        val q = searchQuery.trim().lowercase()
        records
            .filter { rec ->
                val matchFilter = when (recordFilter) {
                    1 -> !KonselingHelper.isGrowthEntry(rec.entryType)
                    2 -> KonselingHelper.isGrowthEntry(rec.entryType)
                    3 -> !KonselingHelper.isCompleted(rec.status)
                    4 -> KonselingHelper.isCompleted(rec.status)
                    else -> true
                }
                val matchSearch = q.isEmpty() ||
                    rec.studentName.lowercase().contains(q) ||
                    rec.title.lowercase().contains(q) ||
                    rec.summary.lowercase().contains(q) ||
                    KonselingHelper.formatTopicLabel(rec.topic, isEn).lowercase().contains(q)
                matchFilter && matchSearch
            }
            .sortedWith(compareByDescending<KonselingRecord> { it.sessionDate }.thenByDescending { it.createdAt })
    }

    val filteredLogs = remember(logs, searchQuery) {
        val q = searchQuery.trim().lowercase()
        logs
            .filter { log ->
                q.isEmpty() ||
                    log.studentName.lowercase().contains(q) ||
                    log.summary.lowercase().contains(q)
            }
            .sortedByDescending { it.createdAt }
    }

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
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Counseling History & Audit Log" else "Riwayat & Log Audit Konseling",
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6D28D9), modifier = Modifier.size(12.dp))
                            Text(
                                text = if (isEn) "Confidential • Owner Wali Asuh only" else "Catatan Rahasia • Hanya dilihat Wali Asuh bersangkutan",
                                fontSize = 11.sp,
                                color = Color(0xFF6D28D9),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                val pdf = PdfReportGenerator.generateKonselingSummaryPdf(
                                    context = context,
                                    students = studentsForSummary,
                                    records = records,
                                    waliName = waliName,
                                    schoolProfile = schoolProfile
                                )
                                if (pdf != null) {
                                    readyPdfFile = pdf
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isEn) "Failed to generate recap PDF." else "Gagal membuat PDF rekap konseling.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("btn_konseling_history_pdf")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_close_konseling_history")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = s.nav.close)
                        }
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = if (isEn) "Notes (${records.size})" else "Catatan (${records.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("tab_konseling_notes")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = if (isEn) "Audit Log (${logs.size})" else "Log Audit (${logs.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("tab_konseling_audit_logs")
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    placeholder = { Text(if (isEn) "Search student, topic, notes..." else "Cari anak, topik, catatan...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_konseling_history_search")
                )

                if (selectedTab == 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            if (isEn) "All" else "Semua",
                            if (isEn) "Session" else "Sesi",
                            if (isEn) "Growth" else "Perkembangan",
                            if (isEn) "Follow-Up" else "Pantau",
                            if (isEn) "Resolved" else "Selesai"
                        ).forEachIndexed { idx, label ->
                            FilterChip(
                                selected = recordFilter == idx,
                                onClick = { recordFilter = idx },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedTab == 0) {
                        if (filteredRecords.isEmpty()) {
                            item {
                                Text(
                                    text = if (isEn) "No matching confidential counseling records." else "Belum ada catatan konseling rahasia.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            items(filteredRecords, key = { "k_hist_rec_${it.id}" }) { rec ->
                                val completed = KonselingHelper.isCompleted(rec.status)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenStudentById(rec.studentId) }
                                        .testTag("card_konseling_history_${rec.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${rec.studentName} (${rec.studentClass.ifBlank { "-" }})",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = DateUtils.formatShortDate(rec.sessionDate, lang),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = rec.title.ifBlank { KonselingHelper.formatTopicLabel(rec.topic, isEn) },
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF4C1D95)
                                        )
                                        if (rec.summary.isNotBlank()) {
                                            Text(
                                                text = rec.summary,
                                                fontSize = 11.5.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            KonselingBadgePill(
                                                text = KonselingHelper.formatStatusLabel(rec.status, isEn),
                                                bg = if (completed) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                                fg = if (completed) Color(0xFF15803D) else Color(0xFFB45309)
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(
                                                onClick = { onEditRecord(rec) },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = if (isEn) "Edit" else "Ubah", modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = { onToggleStatus(rec) },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (completed) Icons.Default.Refresh else Icons.Default.CheckCircle,
                                                    contentDescription = if (isEn) "Toggle status" else "Ubah status",
                                                    tint = Color(0xFF15803D),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { recordToDelete = rec },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = if (isEn) "Delete" else "Hapus", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (filteredLogs.isEmpty()) {
                            item {
                                Text(
                                    text = if (isEn) "No private counseling audit logs yet." else "Belum ada log audit konseling.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            items(filteredLogs, key = { "k_hist_log_${it.id}" }) { log ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenStudentById(log.studentId) }
                                        .testTag("card_konseling_log_${log.id}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${log.studentName} • ${KonselingHelper.actionLogLabel(log.actionType, isEn)}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF5B21B6),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = DateUtils.formatDateTime(log.createdAt),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = log.summary,
                                            fontSize = 11.5.sp,
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

    recordToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text(if (isEn) "Delete Confidential Note?" else "Hapus Catatan Konseling?") },
            text = {
                Text(
                    if (isEn) {
                        "Are you sure you want to delete '${target.title}'? This stays strictly in your private audit log."
                    } else {
                        "Yakin ingin menghapus '${target.title}'? Catatan audit penghapusan hanya disimpan secara privat di akun Anda."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteRecord(target)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(if (isEn) "Delete" else "Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text(s.nav.cancel)
                }
            }
        )
    }

    readyPdfFile?.let { file ->
        AlertDialog(
            onDismissRequest = { readyPdfFile = null },
            title = { Text(if (isEn) "Confidential Recap PDF Ready" else "PDF Rekap Konseling Siap") },
            text = {
                Text(
                    if (isEn) "Choose to open or share your confidential counseling recap PDF."
                    else "Pilih untuk membuka atau membagikan laporan PDF rekap konseling rahasia Anda."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        readyPdfFile = null
                        PdfReportGenerator.openPdfFile(context, file)
                    }
                ) {
                    Text(if (isEn) "Open PDF" else "Buka PDF")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        readyPdfFile = null
                        PdfReportGenerator.shareFastingPdf(
                            context = context,
                            pdfFile = file,
                            subject = if (isEn) "Confidential Counseling Recap" else "Rekap Konseling Rahasia",
                            text = if (isEn) "Attached is the confidential counseling summary (Owner Wali Asuh Only)." else "Terlampir rekap catatan konseling rahasia Wali Asuh."
                        )
                    }
                ) {
                    Text(if (isEn) "Share PDF" else "Bagikan")
                }
            }
        )
    }
}

@Composable
fun KonselingGuideDialog(
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .widthIn(max = 540.dp)
                .testTag("dialog_konseling_guide"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF6D28D9), modifier = Modifier.size(22.dp))
                        Text(
                            text = if (isEn) "Counseling Ethics & Guide" else "Panduan & Etika Konseling",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_konseling_guide")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close)
                    }
                }

                GuideSectionCard(
                    title = if (isEn) "1. Strict Confidentiality Pledge" else "1. Jaminan Kerahasiaan Mutlak",
                    body = if (isEn) {
                        "Every note in Counseling is tagged confidential and strictly visible ONLY to you as the managing Caregiver (Wali Asuh). Never share sensitive student struggles openly in public chats."
                    } else {
                        "Seluruh catatan di menu Konseling bersifat RAHASIA MUTLAK dan hanya dapat dilihat oleh Wali Asuh bersangkutan. Admin Sekolah, Guru Ngaji, Wali lain, maupun Orang Tua tidak memiliki akses ke isi catatan ini."
                    },
                    bg = Color(0xFFF5F3FF),
                    border = Color(0xFFDDD6FE),
                    titleColor = Color(0xFF5B21B6)
                )

                GuideSectionCard(
                    title = if (isEn) "2. 4-Step Empathetic Mentoring" else "2. 4 Langkah Pendampingan Empatik",
                    body = if (isEn) {
                        "Listen without judgment → Validate feelings gently → Agree on small practical steps together → Schedule a calm follow-up check-in."
                    } else {
                        "• Dengarkan aktif tanpa menghakimi atau membandingkan.\n• Validasi perasaan anak dengan kalimat menenangkan.\n• Sepakati 1–2 langkah kecil yang realistis bersama anak.\n• Catat jadwal pantau untuk mengecek perkembangannya."
                    },
                    bg = Color(0xFFF0FDF4),
                    border = Color(0xFFBBF7D0),
                    titleColor = Color(0xFF15803D)
                )

                GuideSectionCard(
                    title = if (isEn) "3. Recording Growth & Positive Praise" else "3. Catat Perkembangan Positif",
                    body = if (isEn) {
                        "Counseling isn't only for problems! Use the 'Growth Note' mode to record positive shifts—improved dorm adaptation, stronger resilience, or peer leadership."
                    } else {
                        "Konseling bukan hanya saat ada masalah. Gunakan mode 'Perkembangan' untuk merekam kemajuan kecil anak: mulai betah di asrama, lebih berani berpendapat, atau makin rajin belajar."
                    },
                    bg = Color(0xFFEFF6FF),
                    border = Color(0xFFBFDBFE),
                    titleColor = Color(0xFF1D4ED8)
                )

                GuideSectionCard(
                    title = if (isEn) "4. When to Escalate High Priority" else "4. Kapan Menandai Prioritas Tinggi",
                    body = if (isEn) {
                        "Use 'High Priority' for persistent anxiety, severe homesickness affecting health/appetite, or safety concerns that need close follow-up."
                    } else {
                        "Tandai 'Prioritas Tinggi' bila anak mengalami kecemasan berkepanjangan, murung berat hingga mengganggu makan/tidur, atau konflik sosial yang butuh pantauan intensif."
                    },
                    bg = Color(0xFFFEF2F2),
                    border = Color(0xFFFECACA),
                    titleColor = Color(0xFFB91C1C)
                )

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(if (isEn) "Understood" else "Mengerti", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GuideSectionCard(
    title: String,
    body: String,
    bg: Color,
    border: Color,
    titleColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.dp, border)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Text(body, fontSize = 11.5.sp, lineHeight = 16.sp, color = Color(0xFF1E293B))
        }
    }
}
