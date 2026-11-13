package com.example.ui.dialogs

import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.SchoolProfile
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import com.example.util.TopOnAdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistPuasaSheet(
    sessions: List<FastingSession>,
    fastingRecords: List<FastingRecord>,
    schoolProfile: SchoolProfile,
    waliName: String,
    onToggleSahur: (FastingRecord) -> Unit,
    onToggleBuka: (FastingRecord) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sortedSessions = remember(sessions) {
        sessions.sortedWith(
            compareByDescending<FastingSession> { it.isActive }
                .thenByDescending { it.sessionDate }
        )
    }

    var selectedSessionId by remember(sortedSessions) {
        mutableStateOf(sortedSessions.firstOrNull()?.id)
    }
    val selectedSession = remember(sortedSessions, selectedSessionId) {
        sortedSessions.firstOrNull { it.id == selectedSessionId } ?: sortedSessions.firstOrNull()
    }

    // 0 = Sahur, 1 = Berbuka
    var selectedCategoryTab by remember { mutableIntStateOf(0) }
    var statusFilter by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    var pendingPdfSession by remember { mutableStateOf<FastingSession?>(null) }

    val sessionRecords = remember(fastingRecords, selectedSession) {
        if (selectedSession == null) emptyList()
        else fastingRecords.filter { it.sessionId == selectedSession.id }
            .sortedBy { it.studentName.lowercase() }
    }

    val sahurDoneCount = remember(sessionRecords) { sessionRecords.count { it.isSahurChecked } }
    val bukaDoneCount = remember(sessionRecords) { sessionRecords.count { it.isBukaChecked } }

    val filteredRecords = remember(sessionRecords, selectedCategoryTab, statusFilter, searchQuery) {
        val q = searchQuery.trim().lowercase()
        sessionRecords.filter { rec ->
            val checked = if (selectedCategoryTab == 0) rec.isSahurChecked else rec.isBukaChecked
            val matchStatus = when (statusFilter) {
                "Sudah" -> checked
                "Belum" -> !checked
                else -> true
            }
            val matchSearch = q.isEmpty() ||
                rec.studentName.lowercase().contains(q) ||
                rec.studentClass.lowercase().contains(q) ||
                rec.studentDorm.lowercase().contains(q)
            matchStatus && matchSearch
        }
    }

    val amberDark = Color(0xFFB45309)
    val amberLight = Color(0xFFFEF3C7)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Checklist Sahur & Berbuka",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Centang kehadiran sahur & berbuka siswa serta cetak PDF",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_checklist_puasa")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                if (sortedSessions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Belum ada sesi puasa yang dibuat oleh Admin Sekolah.",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    // Pilih Sesi
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sortedSessions.forEach { session ->
                            val selected = selectedSession?.id == session.id
                            FilterChip(
                                selected = selected,
                                onClick = { selectedSessionId = session.id },
                                label = {
                                    Text(
                                        text = "${session.title} (${DateUtils.formatShortDate(session.sessionDate)})",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(14.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = amberLight,
                                    selectedLabelColor = amberDark,
                                    selectedLeadingIconColor = amberDark
                                )
                            )
                        }
                    }

                    // Kategori Tab: Sahur vs Berbuka
                    TabRow(
                        selectedTabIndex = selectedCategoryTab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedCategoryTab == 0,
                            onClick = { selectedCategoryTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.NightsStay, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sahur ($sahurDoneCount/${sessionRecords.size})",
                                        fontSize = 12.5.sp,
                                        fontWeight = if (selectedCategoryTab == 0) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_checklist_sahur")
                        )
                        Tab(
                            selected = selectedCategoryTab == 1,
                            onClick = { selectedCategoryTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Berbuka ($bukaDoneCount/${sessionRecords.size})",
                                        fontSize = 12.5.sp,
                                        fontWeight = if (selectedCategoryTab == 1) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_checklist_berbuka")
                        )
                    }

                    // Action Bar: Cetak PDF & Bagikan WA
                    selectedSession?.let { sess ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    pendingPdfSession = sess
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = amberDark),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_pdf_checklist_puasa")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Laporan Checklist PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val waText = PdfReportGenerator.buildFastingChecklistWaSummary(
                                        session = sess,
                                        records = sessionRecords,
                                        waliName = waliName,
                                        schoolProfile = schoolProfile
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, waText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Bagikan Checklist ke WA"))
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .weight(0.85f)
                                    .heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share WA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            }
                        }

                        RewardAdDeclarationHint(
                            text = "Tonton iklan video singkat untuk mengunduh Laporan Checklist PDF",
                            accentColor = amberDark
                        )
                    }

                    pendingPdfSession?.let { sessToExport ->
                        RewardAdDeclarationDialog(
                            title = "Unduh Laporan Checklist PDF",
                            actionDescription = "mengunduh dan membagikan Laporan Checklist ${sessToExport.title}",
                            confirmButtonText = "Tonton & Unduh PDF",
                            onConfirmWatchAd = {
                                pendingPdfSession = null
                                TopOnAdManager.showRewardAdForPdfReport(context) {
                                    val pdf = PdfReportGenerator.generateFastingChecklistPdf(
                                        context = context,
                                        session = sessToExport,
                                        records = sessionRecords,
                                        waliName = waliName,
                                        schoolProfile = schoolProfile
                                    )
                                    if (pdf != null) {
                                        PdfReportGenerator.shareFastingPdf(
                                            context = context,
                                            pdfFile = pdf,
                                            subject = "Laporan Checklist Sahur & Berbuka - ${sessToExport.title}",
                                            text = "Terlampir Laporan Checklist Sahur ($sahurDoneCount/${sessionRecords.size}) & Berbuka ($bukaDoneCount/${sessionRecords.size}) untuk ${sessToExport.title}."
                                        )
                                    } else {
                                        Toast.makeText(context, "Gagal membuat PDF checklist puasa.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onDismiss = { pendingPdfSession = null }
                        )
                    }

                    // Filter & Search
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Semua", "Sudah", "Belum").forEach { opt ->
                            FilterChip(
                                selected = statusFilter == opt,
                                onClick = { statusFilter = opt },
                                label = { Text(opt, fontSize = 11.5.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari nama siswa...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (filteredRecords.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (sessionRecords.isEmpty()) "Belum ada peserta puasa pada sesi ini. Input dulu melalui menu Input Puasa."
                                else "Tidak ada siswa sesuai filter.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 320.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredRecords, key = { "chk_${selectedCategoryTab}_${it.id}" }) { rec ->
                                val isChecked = if (selectedCategoryTab == 0) rec.isSahurChecked else rec.isBukaChecked
                                val checker = if (selectedCategoryTab == 0) rec.sahurCheckedBy else rec.bukaCheckedBy
                                val checkedAt = if (selectedCategoryTab == 0) rec.sahurCheckedAt else rec.bukaCheckedAt
                                val jenjang = FastingJenjangHelper.resolveJenjang(rec)
                                val onToggle = {
                                    if (selectedCategoryTab == 0) onToggleSahur(rec) else onToggleBuka(rec)
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggle() }
                                        .testTag("checklist_item_${rec.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isChecked) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isChecked) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { onToggle() },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFF16A34A),
                                                checkmarkColor = Color.White
                                            )
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = rec.studentName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "$jenjang • ${rec.studentClass.ifBlank { "-" }} • ${rec.studentDorm}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (isChecked && (checker.isNotBlank() || checkedAt != null)) {
                                                val timeStr = checkedAt?.let { DateUtils.formatTime(it) } ?: ""
                                                Text(
                                                    text = "Dicek oleh $checker${if (timeStr.isNotBlank()) " • $timeStr" else ""}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF15803D)
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isChecked) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isChecked) {
                                                    if (selectedCategoryTab == 0) "Sudah Sahur" else "Sudah Buka"
                                                } else {
                                                    "Belum"
                                                },
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isChecked) Color(0xFF15803D) else Color(0xFF64748B)
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
