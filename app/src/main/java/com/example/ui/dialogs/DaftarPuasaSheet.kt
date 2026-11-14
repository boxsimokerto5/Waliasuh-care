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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
fun DaftarPuasaSheet(
    sessions: List<FastingSession>,
    fastingRecords: List<FastingRecord>,
    schoolProfile: SchoolProfile,
    waliName: String,
    onDeleteRecord: (FastingRecord) -> Unit,
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

    var selectedJenjangFilter by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    var pendingPdfSession by remember { mutableStateOf<FastingSession?>(null) }

    val sessionRecords = remember(fastingRecords, selectedSession) {
        if (selectedSession == null) emptyList()
        else fastingRecords.filter { it.sessionId == selectedSession.id }
            .sortedBy { it.studentName.lowercase() }
    }

    val smaCount = remember(sessionRecords) {
        sessionRecords.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SMA }
    }
    val smpCount = remember(sessionRecords) {
        sessionRecords.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SMP }
    }
    val sdCount = remember(sessionRecords) {
        sessionRecords.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SD }
    }
    val otherCount = sessionRecords.size - smaCount - smpCount - sdCount

    val filteredRecords = remember(sessionRecords, selectedJenjangFilter, searchQuery) {
        val q = searchQuery.trim().lowercase()
        sessionRecords.filter { rec ->
            val jenjang = FastingJenjangHelper.resolveJenjang(rec)
            val matchJenjang = selectedJenjangFilter == "Semua" || jenjang == selectedJenjangFilter
            val matchSearch = q.isEmpty() ||
                rec.studentName.lowercase().contains(q) ||
                rec.studentClass.lowercase().contains(q) ||
                rec.studentDorm.lowercase().contains(q)
            matchJenjang && matchSearch
        }
    }

    val accentBlue = Color(0xFF0369A1)
    val accentBlueLight = Color(0xFFE0F2FE)

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
                // Header ringkas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Daftar Puasa Students" else "Daftar Siswa Puasa",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isEn) "SD, SMP, SMA summary & PDF" else "Rekap SD, SMP, SMA & Laporan PDF",
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
                            .testTag("btn_close_daftar_puasa")
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
                                    selectedContainerColor = accentBlueLight,
                                    selectedLabelColor = accentBlue,
                                    selectedLeadingIconColor = accentBlue
                                ),
                                modifier = Modifier.heightIn(min = 34.dp)
                            )
                        }
                    }

                    // Gabungan Rekapitulasi & Filter Jenjang (1 baris interaktif - hapus chip duplikat)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SummaryJenjangTile(
                            label = if (isEn) "All" else "Semua",
                            count = sessionRecords.size,
                            bg = Color(0xFF0F766E),
                            fg = Color.White,
                            isSelected = selectedJenjangFilter == "Semua",
                            onClick = { selectedJenjangFilter = "Semua" },
                            modifier = Modifier
                                .weight(1.05f)
                                .testTag("filter_daftar_Semua")
                        )
                        SummaryJenjangTile(
                            label = "SMA",
                            count = smaCount,
                            bg = Color(0xFFF3E8FF),
                            fg = Color(0xFF7E22CE),
                            isSelected = selectedJenjangFilter == "SMA",
                            onClick = { selectedJenjangFilter = "SMA" },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("filter_daftar_SMA")
                        )
                        SummaryJenjangTile(
                            label = "SMP",
                            count = smpCount,
                            bg = Color(0xFFE0F2FE),
                            fg = Color(0xFF0369A1),
                            isSelected = selectedJenjangFilter == "SMP",
                            onClick = { selectedJenjangFilter = "SMP" },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("filter_daftar_SMP")
                        )
                        SummaryJenjangTile(
                            label = "SD",
                            count = sdCount,
                            bg = Color(0xFFDCFCE7),
                            fg = Color(0xFF15803D),
                            isSelected = selectedJenjangFilter == "SD",
                            onClick = { selectedJenjangFilter = "SD" },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("filter_daftar_SD")
                        )
                    }

                    if (otherCount > 0) {
                        Text(
                            text = if (isEn) "*Includes $otherCount students from other levels" else "*Termasuk $otherCount siswa jenjang lainnya",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    pendingPdfSession?.let { sessToExport ->
                        RewardAdDeclarationDialog(
                            title = if (isEn) "Download Puasa PDF Report" else "Unduh Laporan PDF Puasa",
                            actionDescription = if (isEn) {
                                "download and share the PDF Report for ${sessToExport.title}"
                            } else {
                                "mengunduh dan membagikan Laporan PDF ${sessToExport.title}"
                            },
                            confirmButtonText = if (isEn) "Watch & Download PDF" else "Tonton & Unduh PDF",
                            onConfirmWatchAd = {
                                pendingPdfSession = null
                                TopOnAdManager.showRewardAdForPdfReport(context) {
                                    val pdf = PdfReportGenerator.generateFastingListPdf(
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
                                            subject = if (isEn) "Daftar Puasa Report - ${sessToExport.title}" else "Laporan Daftar Puasa - ${sessToExport.title}",
                                            text = if (isEn) {
                                                "Attached is the Daftar Puasa Students Report (${sessToExport.title}, Total ${sessionRecords.size} Students: $smaCount SMA, $smpCount SMP, $sdCount SD)."
                                            } else {
                                                "Terlampir Laporan Daftar Siswa Puasa (${sessToExport.title}, Total ${sessionRecords.size} Anak: $smaCount SMA, $smpCount SMP, $sdCount SD)."
                                            }
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            if (isEn) "Failed to generate Puasa PDF report." else "Gagal membuat PDF laporan puasa.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            onDismiss = { pendingPdfSession = null }
                        )
                    }

                    // Aksi PDF/WA, Pencarian & Daftar Siswa menyatu dalam 1 LazyColumn agar ruang baca jauh lebih lega
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedSession?.let { sess ->
                            item(key = "daftar_actions") {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { pendingPdfSession = sess },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_pdf_daftar_puasa")
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                if (isEn) "PDF Report" else "Laporan PDF",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val waText = PdfReportGenerator.buildFastingListWaSummary(
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
                                                        if (isEn) "Share Puasa Summary to WA" else "Bagikan Rekap Puasa ke WA"
                                                    )
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_wa_daftar_puasa")
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                if (isEn) "Share WA" else "Bagikan WA",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF16A34A),
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    RewardAdDeclarationHint(
                                        text = if (isEn) "Watch a short video ad to download PDF" else "Tonton iklan singkat untuk mengunduh PDF",
                                        accentColor = Color(0xFFDC2626),
                                        compact = true
                                    )
                                }
                            }
                        }

                        item(key = "daftar_search") {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        if (isEn) "Search student name or class..." else "Cari nama siswa atau kelas...",
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
                                    .testTag("input_search_daftar_puasa")
                            )
                        }

                        if (filteredRecords.isEmpty()) {
                            item(key = "daftar_empty") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (sessionRecords.isEmpty()) {
                                            if (isEn) "No students recorded for Puasa in this session yet." else "Belum ada siswa yang tercatat berpuasa pada sesi ini."
                                        } else {
                                            if (isEn) "No students match the current filter." else "Tidak ada siswa yang sesuai filter."
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(filteredRecords, key = { _, rec -> "daftar_puasa_${rec.id}" }) { idx, rec ->
                                val jenjang = FastingJenjangHelper.resolveJenjang(rec)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                                                .background(accentBlueLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${idx + 1}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = accentBlue
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
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
                                            Text(
                                                text = if (isEn) {
                                                    "Recorded by: ${rec.waliName.ifBlank { "-" }}"
                                                } else {
                                                    "Diinput oleh: ${rec.waliName.ifBlank { "-" }}"
                                                },
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteRecord(rec) },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = if (isEn) "Remove Participant" else "Hapus Peserta",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
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

@Composable
private fun SummaryJenjangTile(
    label: String,
    count: Int,
    bg: Color,
    fg: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = if (isSelected) BorderStroke(2.dp, if (fg == Color.White) Color(0xFF99F6E4) else fg) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$count", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = fg, maxLines = 1)
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fg.copy(alpha = 0.92f), maxLines = 1)
        }
    }
}
