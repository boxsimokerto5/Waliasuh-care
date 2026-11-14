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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
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
import com.example.util.i18n.LocalAppStrings

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
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language
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
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header ringkas 1 baris judul + 1 baris subjudul
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Sahur & Berbuka Checklist" else "Checklist Sahur & Berbuka",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isEn) "Check Sahur & Berbuka attendance & PDF" else "Centang sahur & berbuka serta cetak PDF",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_checklist_puasa")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close, modifier = Modifier.size(20.dp))
                    }
                }

                if (sortedSessions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (isEn) "No Puasa sessions have been created by Admin Sekolah yet." else "Belum ada sesi puasa yang dibuat oleh Admin Sekolah.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    // Pilih Sesi Ringkas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sortedSessions.forEach { session ->
                            val selected = selectedSession?.id == session.id
                            FilterChip(
                                selected = selected,
                                onClick = { selectedSessionId = session.id },
                                label = {
                                    Text(
                                        text = "${session.title} (${DateUtils.formatShortDate(session.sessionDate, lang)})",
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(13.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = amberLight,
                                    selectedLabelColor = amberDark,
                                    selectedLeadingIconColor = amberDark
                                ),
                                modifier = Modifier.heightIn(min = 34.dp)
                            )
                        }
                    }

                    // Kategori Tab: Sahur vs Berbuka (tetap terlihat di atas agar mudah berpindah)
                    TabRow(
                        selectedTabIndex = selectedCategoryTab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedCategoryTab == 0,
                            onClick = { selectedCategoryTab = 0 },
                            modifier = Modifier
                                .heightIn(min = 42.dp)
                                .testTag("tab_checklist_sahur"),
                            content = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.NightsStay, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Sahur ($sahurDoneCount/${sessionRecords.size})",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedCategoryTab == 0) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedCategoryTab == 1,
                            onClick = { selectedCategoryTab = 1 },
                            modifier = Modifier
                                .heightIn(min = 42.dp)
                                .testTag("tab_checklist_berbuka"),
                            content = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Berbuka ($bukaDoneCount/${sessionRecords.size})",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedCategoryTab == 1) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        )
                    }

                    pendingPdfSession?.let { sessToExport ->
                        RewardAdDeclarationDialog(
                            title = if (isEn) "Download Checklist PDF Report" else "Unduh Laporan Checklist PDF",
                            actionDescription = if (isEn) {
                                "download and share the Checklist Report for ${sessToExport.title}"
                            } else {
                                "mengunduh dan membagikan Laporan Checklist ${sessToExport.title}"
                            },
                            confirmButtonText = if (isEn) "Watch & Download PDF" else "Tonton & Unduh PDF",
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
                                            subject = if (isEn) "Sahur & Berbuka Checklist Report - ${sessToExport.title}" else "Laporan Checklist Sahur & Berbuka - ${sessToExport.title}",
                                            text = if (isEn) {
                                                "Attached is the Sahur ($sahurDoneCount/${sessionRecords.size}) & Berbuka ($bukaDoneCount/${sessionRecords.size}) Checklist Report for ${sessToExport.title}."
                                            } else {
                                                "Terlampir Laporan Checklist Sahur ($sahurDoneCount/${sessionRecords.size}) & Berbuka ($bukaDoneCount/${sessionRecords.size}) untuk ${sessToExport.title}."
                                            }
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            if (isEn) "Failed to generate Puasa checklist PDF." else "Gagal membuat PDF checklist puasa.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            onDismiss = { pendingPdfSession = null }
                        )
                    }

                    // Aksi, Filter, Search & Daftar Checklist dalam 1 LazyColumn agar daftar siswa jauh lebih lapang
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedSession?.let { sess ->
                            item(key = "chk_actions") {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { pendingPdfSession = sess },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = amberDark),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_pdf_checklist_puasa")
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                if (isEn) "Checklist PDF" else "Checklist PDF",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val waText = PdfReportGenerator.buildFastingChecklistWaSummary(
                                                    session = sess,
                                                    records = sessionRecords,
                                                    waliName = waliName,
                                                    schoolProfile = schoolProfile,
                                                    language = lang
                                                )
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, waText)
                                                }
                                                context.startActivity(
                                                    Intent.createChooser(
                                                        intent,
                                                        if (isEn) "Share Checklist to WA" else "Bagikan Checklist ke WA"
                                                    )
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                            modifier = Modifier
                                                .weight(0.9f)
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_wa_checklist_puasa")
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Share WA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), maxLines = 1)
                                        }
                                    }

                                    RewardAdDeclarationHint(
                                        text = if (isEn) "Watch a short video ad to download Checklist PDF" else "Tonton iklan singkat untuk mengunduh Checklist PDF",
                                        accentColor = amberDark,
                                        compact = true
                                    )
                                }
                            }
                        }

                        item(key = "chk_filter_search") {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Semua", "Sudah", "Belum").forEach { opt ->
                                        val displayOpt = when (opt) {
                                            "Semua" -> if (isEn) "All" else "Semua"
                                            "Sudah" -> if (isEn) "Checked" else "Sudah"
                                            else -> if (isEn) "Pending" else "Belum"
                                        }
                                        FilterChip(
                                            selected = statusFilter == opt,
                                            onClick = { statusFilter = opt },
                                            label = { Text(displayOpt, fontSize = 11.sp, maxLines = 1) },
                                            modifier = Modifier
                                                .heightIn(min = 34.dp)
                                                .testTag("filter_checklist_$opt")
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            if (isEn) "Search student name..." else "Cari nama siswa...",
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_search_checklist_puasa")
                                )
                            }
                        }

                        if (filteredRecords.isEmpty()) {
                            item(key = "chk_empty") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (sessionRecords.isEmpty()) {
                                            if (isEn) {
                                                "No Puasa participants in this session yet. Register students via Input Puasa first."
                                            } else {
                                                "Belum ada peserta puasa pada sesi ini. Input dulu melalui menu Input Puasa."
                                            }
                                        } else {
                                            if (isEn) "No students match the current filter." else "Tidak ada siswa sesuai filter."
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
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
                                                val timeStr = checkedAt?.let { DateUtils.formatTime(it, lang) } ?: ""
                                                Text(
                                                    text = if (isEn) {
                                                        "By $checker${if (timeStr.isNotBlank()) " • $timeStr" else ""}"
                                                    } else {
                                                        "Oleh $checker${if (timeStr.isNotBlank()) " • $timeStr" else ""}"
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF15803D),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
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
                                                    if (selectedCategoryTab == 0) {
                                                        if (isEn) "Sahur Done" else "Sudah Sahur"
                                                    } else {
                                                        if (isEn) "Berbuka Done" else "Sudah Buka"
                                                    }
                                                } else {
                                                    if (isEn) "Pending" else "Belum"
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
