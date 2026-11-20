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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.Surface
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
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.ChildStudent
import com.example.data.model.KonselingHelper
import com.example.data.model.KonselingLog
import com.example.data.model.KonselingRecord
import com.example.data.model.SchoolProfile
import com.example.util.DateUtils
import com.example.util.KonselingPhotoHelper
import com.example.util.PdfReportGenerator
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KonselingStudentDetailSheet(
    student: ChildStudent,
    studentRecords: List<KonselingRecord>,
    studentLogs: List<KonselingLog>,
    waliName: String,
    schoolProfile: SchoolProfile,
    isRecordOwnedByCurrentWali: (KonselingRecord) -> Boolean = { true },
    onQuickInput: (String) -> Unit,
    onEditRecord: (KonselingRecord) -> Unit,
    onToggleStatus: (KonselingRecord) -> Unit,
    onDeleteRecord: (KonselingRecord) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language
    val coroutineScope = rememberCoroutineScope()

    val stats = remember(studentRecords, student) {
        KonselingHelper.computeStudentStats(studentRecords, student)
    }

    // 0=Semua, 1=Sesi, 2=Perkembangan, 3=Pantau, 4=Log Audit
    var selectedFilter by remember { mutableIntStateOf(0) }
    var recordToDelete by remember { mutableStateOf<KonselingRecord?>(null) }
    var readyPdfFile by remember { mutableStateOf<File?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var previewPhotoModel by remember { mutableStateOf<Any?>(null) }

    val filteredRecords = remember(studentRecords, selectedFilter) {
        studentRecords
            .filter { rec ->
                when (selectedFilter) {
                    1 -> !KonselingHelper.isGrowthEntry(rec.entryType)
                    2 -> KonselingHelper.isGrowthEntry(rec.entryType)
                    3 -> !KonselingHelper.isCompleted(rec.status)
                    else -> true
                }
            }
            .sortedWith(compareByDescending<KonselingRecord> { it.sessionDate }.thenByDescending { it.createdAt })
    }
    val sortedLogs = remember(studentLogs) {
        studentLogs.sortedByDescending { it.createdAt }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 2.dp),
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
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // Header Ringkas (Aman di Bawah Status Bar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.name,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6D28D9), modifier = Modifier.size(11.dp))
                            Text(
                                text = "${student.classGrade.ifBlank { "-" }} • ${student.dormitory.ifBlank { "-" }} • ${if (isEn) "Owner Wali Only" else "Rahasia Wali Asuh"}",
                                fontSize = 10.5.sp,
                                color = Color(0xFF6D28D9),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("btn_close_konseling_detail")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close, modifier = Modifier.size(20.dp))
                    }
                }

                // Summary Card Rapat (Tanpa Teks Terjepit)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_konseling_detail_summary"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                    border = BorderStroke(1.dp, Color(0xFFDDD6FE))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) {
                                    "CONFIDENTIAL • MANAGING CAREGIVER ONLY"
                                } else {
                                    "RAHASIA • KHUSUS WALI ASUH BERSANGKUTAN"
                                },
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF5B21B6),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEDE9FE)
                            ) {
                                Text(
                                    text = "${stats.totalEntries} ${if (isEn) "notes" else "catatan"}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5B21B6),
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            DetailMiniStat("${stats.sessionsCount}", if (isEn) "Sessions" else "Sesi", Color(0xFFEDE9FE), Color(0xFF5B21B6), Modifier.weight(1f))
                            DetailMiniStat("${stats.growthCount}", if (isEn) "Growth" else "Tumbuh", Color(0xFFDCFCE7), Color(0xFF15803D), Modifier.weight(1f))
                            DetailMiniStat("${stats.followUpCount}", if (isEn) "Monitor" else "Pantau", Color(0xFFFEF3C7), Color(0xFFB45309), Modifier.weight(1f))
                            DetailMiniStat("${stats.completedCount}", if (isEn) "Resolved" else "Selesai", Color(0xFFE0F2FE), Color(0xFF0369A1), Modifier.weight(1f))
                        }
                    }
                }

                // Quick Action Row Ramping
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { onQuickInput(KonselingHelper.ENTRY_SESI) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_konseling_detail_add_sesi")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isEn) "Session" else "Sesi", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = { onQuickInput(KonselingHelper.ENTRY_PERKEMBANGAN) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_konseling_detail_add_growth")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isEn) "Growth" else "Tumbuh", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    OutlinedButton(
                        enabled = !isGeneratingPdf,
                        onClick = {
                            if (isGeneratingPdf) return@OutlinedButton
                            isGeneratingPdf = true
                            coroutineScope.launch {
                                val pdf = PdfReportGenerator.generateKonselingStudentPdfWithPhotos(
                                    context = context,
                                    student = student,
                                    records = studentRecords,
                                    waliName = waliName,
                                    schoolProfile = schoolProfile
                                )
                                isGeneratingPdf = false
                                if (pdf != null) {
                                    readyPdfFile = pdf
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isEn) "Failed to create PDF." else "Gagal membuat PDF konseling.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("btn_konseling_detail_pdf")
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isGeneratingPdf) "..." else "PDF",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                // Filter Chips Kompak
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val filters = listOf(
                        if (isEn) "All (${studentRecords.size})" else "Semua (${studentRecords.size})",
                        if (isEn) "Sessions" else "Sesi",
                        if (isEn) "Growth" else "Perkembangan",
                        if (isEn) "Follow-Up" else "Pantau",
                        if (isEn) "Audit Log (${sortedLogs.size})" else "Log Audit (${sortedLogs.size})"
                    )
                    filters.forEachIndexed { idx, label ->
                        val selected = selectedFilter == idx
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) Color(0xFFEDE9FE) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (selected) Color(0xFF6D28D9) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .clickable { selectedFilter = idx }
                                .testTag("chip_konseling_detail_filter_$idx")
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color(0xFF5B21B6) else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Timeline / Logs List (Memanfaatkan Ruang Tersedia Secara Maksimal)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 200.dp, max = 460.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    if (selectedFilter == 4) {
                        if (sortedLogs.isEmpty()) {
                            item {
                                Text(
                                    text = if (isEn) "No audit log entries yet." else "Belum ada riwayat log audit konseling.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            items(sortedLogs, key = { "k_det_log_${it.id}" }) { log ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("card_konseling_detail_log_${log.id}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
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
                                                text = KonselingHelper.actionLogLabel(log.actionType, isEn),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF6D28D9)
                                            )
                                            Text(
                                                text = DateUtils.formatDateTime(log.createdAt),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = log.summary,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    } else if (filteredRecords.isEmpty()) {
                        item {
                            Text(
                                text = if (isEn) "No private counseling records yet." else "Belum ada catatan konseling rahasia untuk anak ini.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        items(filteredRecords, key = { "k_det_rec_${it.id}" }) { rec ->
                            val completed = KonselingHelper.isCompleted(rec.status)
                            val growth = KonselingHelper.isGrowthEntry(rec.entryType)
                            val isOwned = isRecordOwnedByCurrentWali(rec)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_konseling_record_${rec.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(
                                    1.dp,
                                    if (!completed && KonselingHelper.isHighPriority(rec.urgencyLevel)) Color(0xFFFCA5A5)
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(9.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            KonselingBadgePill(
                                                text = if (growth) (if (isEn) "Growth" else "Perkembangan") else (if (isEn) "Session" else "Sesi"),
                                                bg = if (growth) Color(0xFFDCFCE7) else Color(0xFFEDE9FE),
                                                fg = if (growth) Color(0xFF15803D) else Color(0xFF5B21B6)
                                            )
                                            KonselingBadgePill(
                                                text = KonselingHelper.formatStatusLabel(rec.status, isEn),
                                                bg = if (completed) Color(0xFFE0F2FE) else Color(0xFFFEF3C7),
                                                fg = if (completed) Color(0xFF0369A1) else Color(0xFFB45309)
                                            )
                                        }
                                        Text(
                                            text = DateUtils.formatShortDate(rec.sessionDate, lang),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = rec.title.ifBlank { KonselingHelper.formatTopicLabel(rec.topic, isEn) },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!isOwned) {
                                            KonselingBadgePill(
                                                text = if (isEn) {
                                                    "Shared • By ${rec.ownerWaliName} (Read-Only)"
                                                } else {
                                                    "Dibagikan • Oleh ${rec.ownerWaliName} (Baca-Saja)"
                                                },
                                                bg = Color(0xFFCCFBF1),
                                                fg = Color(0xFF0F766E)
                                            )
                                        } else if (rec.shareWithTandem) {
                                            KonselingBadgePill(
                                                text = if (isEn) "Shared with Tandem" else "Dibagikan ke Tandem",
                                                bg = Color(0xFFE0E7FF),
                                                fg = Color(0xFF4338CA)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${KonselingHelper.formatTopicLabel(rec.topic, isEn)} • Mood: ${KonselingHelper.formatMoodLabel(rec.moodLevel, isEn)} • ${KonselingHelper.formatUrgencyLabel(rec.urgencyLevel, isEn)}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (rec.summary.isNotBlank()) {
                                        Text(
                                            text = rec.summary,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (rec.intervention.isNotBlank()) {
                                        Text(
                                            text = "${if (isEn) "Support" else "Pendampingan"}: ${rec.intervention}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF4C1D95)
                                        )
                                    }
                                    if (rec.followUpPlan.isNotBlank() || rec.followUpDate != null) {
                                        val fuDate = rec.followUpDate?.let { " (${DateUtils.formatShortDate(it, lang)})" } ?: ""
                                        Text(
                                            text = "${if (isEn) "Follow-Up" else "Tindak Lanjut"}$fuDate: ${rec.followUpPlan.ifBlank { "-" }}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFB45309)
                                        )
                                    }

                                    val photoSlots = remember(rec.photoPaths, rec.photoUrls) {
                                        KonselingPhotoHelper.extractSlots(rec)
                                    }
                                    if (photoSlots.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 2.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoLibrary,
                                                    contentDescription = null,
                                                    tint = Color(0xFF6D28D9),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = if (isEn) {
                                                        "Session Photos (${photoSlots.size})"
                                                    } else {
                                                        "Foto Dokumentasi (${photoSlots.size})"
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF6D28D9)
                                                )
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                photoSlots.forEachIndexed { idx, slot ->
                                                    val model = slot.displayModel()
                                                    if (model != null) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(72.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .border(
                                                                    1.dp,
                                                                    Color(0xFFDDD6FE),
                                                                    RoundedCornerShape(8.dp)
                                                                )
                                                                .clickable { previewPhotoModel = model }
                                                                .testTag("img_konseling_detail_photo_${rec.id}_$idx")
                                                        ) {
                                                            AsyncImage(
                                                                model = model,
                                                                contentDescription = if (isEn) {
                                                                    "Counseling photo ${idx + 1}"
                                                                } else {
                                                                    "Foto konseling ${idx + 1}"
                                                                },
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier.matchParentSize()
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (isOwned) {
                                        // Row Actions Kompak (Hanya untuk Wali pemilik catatan)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = { onEditRecord(rec) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                                modifier = Modifier
                                                    .height(36.dp)
                                                    .testTag("btn_edit_konseling_${rec.id}")
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isEn) "Edit" else "Ubah", fontSize = 11.sp, maxLines = 1)
                                            }
                                            OutlinedButton(
                                                onClick = { onToggleStatus(rec) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(36.dp)
                                                    .testTag("btn_toggle_konseling_${rec.id}")
                                            ) {
                                                Icon(
                                                    imageVector = if (completed) Icons.Default.Refresh else Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (completed) {
                                                        if (isEn) "Reopen" else "Pantau Lagi"
                                                    } else {
                                                        if (isEn) "Mark Resolved" else "Tandai Selesai"
                                                    },
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            IconButton(
                                                onClick = { recordToDelete = rec },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .testTag("btn_delete_konseling_${rec.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = if (isEn) "Delete Note" else "Hapus Catatan",
                                                    tint = Color(0xFFDC2626),
                                                    modifier = Modifier.size(19.dp)
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

    recordToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text(if (isEn) "Delete Confidential Note?" else "Hapus Catatan Konseling?") },
            text = {
                Text(
                    if (isEn) {
                        "Are you sure you want to delete '${target.title}'? The action will only be logged privately in your own counseling audit log."
                    } else {
                        "Yakin ingin menghapus '${target.title}'? Penghapusan hanya dicatat di log audit privat Anda dan tidak diumumkan ke sekolah."
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
            title = { Text(if (isEn) "Confidential PDF Ready" else "PDF Konseling Rahasia Siap") },
            text = {
                Text(
                    if (isEn) "Choose to open or share this confidential student counseling dossier."
                    else "Pilih untuk membuka atau membagikan dokumen PDF konseling rahasia anak ini."
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
                            subject = if (isEn) "Confidential Counseling Dossier" else "Catatan Konseling Rahasia",
                            text = if (isEn) "Confidential student counseling dossier (Caregiver Only)." else "Terlampir catatan konseling rahasia anak binaan (Khusus Wali Asuh)."
                        )
                    }
                ) {
                    Text(if (isEn) "Share PDF" else "Bagikan")
                }
            }
        )
    }

    previewPhotoModel?.let { model ->
        Dialog(onDismissRequest = { previewPhotoModel = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    AsyncImage(
                        model = model,
                        contentDescription = if (isEn) "Counseling photo preview" else "Pratinjau foto konseling",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 440.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    TextButton(
                        onClick = { previewPhotoModel = null },
                        modifier = Modifier.testTag("btn_close_konseling_detail_photo_preview")
                    ) {
                        Text(s.nav.close)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMiniStat(
    value: String,
    label: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(9.dp),
        color = bg
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = fg, maxLines = 1)
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = fg.copy(alpha = 0.85f), maxLines = 1)
        }
    }
}

@Composable
internal fun KonselingBadgePill(
    text: String,
    bg: Color,
    fg: Color
) {
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}
