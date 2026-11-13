package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.util.DateUtils
import java.util.Calendar

private data class StudentFastingRankItem(
    val key: String,
    val studentName: String,
    val jenjang: String,
    val studentClass: String,
    val studentDorm: String,
    val sessionCount: Int,
    val sahurCount: Int,
    val bukaCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingPuasaSheet(
    sessions: List<FastingSession>,
    fastingRecords: List<FastingRecord>,
    onDismiss: () -> Unit
) {
    // 0 = Ranking Puasa, 1 = History Sesi
    var selectedTab by remember { mutableIntStateOf(0) }
    var periodFilter by remember { mutableStateOf("Semua Sesi") }
    var jenjangFilter by remember { mutableStateOf("Semua") }
    var selectedHistorySession by remember { mutableStateOf<FastingSession?>(null) }

    val purpleDark = Color(0xFF7E22CE)

    val calNow = Calendar.getInstance()
    val currentMonth = calNow.get(Calendar.MONTH)
    val currentYear = calNow.get(Calendar.YEAR)

    val sessionDateMap = remember(sessions) {
        sessions.associate { it.id to it.sessionDate }
    }

    val filteredRecordsForRanking = remember(fastingRecords, sessionDateMap, periodFilter, jenjangFilter) {
        fastingRecords.filter { rec ->
            val matchPeriod = if (periodFilter == "Bulan Ini") {
                val dateMs = sessionDateMap[rec.sessionId] ?: rec.sessionDate.takeIf { it > 0L } ?: rec.createdAt
                val c = Calendar.getInstance().apply { timeInMillis = dateMs }
                c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
            } else true

            val jenjang = FastingJenjangHelper.resolveJenjang(rec)
            val matchJenjang = jenjangFilter == "Semua" || jenjang == jenjangFilter
            matchPeriod && matchJenjang
        }
    }

    // Ranking dihitung berdasarkan jumlah sesi siswa ikut / terdaftar puasa
    val rankedStudents = remember(filteredRecordsForRanking) {
        filteredRecordsForRanking
            .groupBy { rec ->
                if (rec.studentId > 0L) "id_${rec.studentId}"
                else if (rec.studentNik.isNotBlank()) "nik_${rec.studentNik.trim().lowercase()}"
                else "name_${rec.studentName.trim().lowercase()}"
            }
            .map { (key, recs) ->
                val latest = recs.maxByOrNull { it.createdAt } ?: recs.first()
                val distinctSessions = recs.map { it.sessionId }.distinct().size
                StudentFastingRankItem(
                    key = key,
                    studentName = latest.studentName,
                    jenjang = FastingJenjangHelper.resolveJenjang(latest),
                    studentClass = latest.studentClass,
                    studentDorm = latest.studentDorm,
                    sessionCount = distinctSessions,
                    sahurCount = recs.count { it.isSahurChecked },
                    bukaCount = recs.count { it.isBukaChecked }
                )
            }
            .sortedWith(
                compareByDescending<StudentFastingRankItem> { it.sessionCount }
                    .thenByDescending { it.bukaCount + it.sahurCount }
                    .thenBy { it.studentName.lowercase() }
            )
    }

    val sortedSessions = remember(sessions) {
        sessions.sortedByDescending { it.sessionDate }
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
                            text = "Ranking & History Sesi Puasa",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Peringkat keaktifan puasa siswa & riwayat seluruh sesi",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_ranking_puasa")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ranking (${rankedStudents.size})",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_ranking_puasa")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "History Sesi (${sortedSessions.size})",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_history_sesi_puasa")
                    )
                }

                if (selectedTab == 0) {
                    // Filter Periode & Jenjang
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Semua Sesi", "Bulan Ini").forEach { p ->
                            FilterChip(
                                selected = periodFilter == p,
                                onClick = { periodFilter = p },
                                label = { Text(p, fontSize = 11.5.sp) }
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        listOf("Semua", "SMA", "SMP", "SD").forEach { j ->
                            FilterChip(
                                selected = jenjangFilter == j,
                                onClick = { jenjangFilter = j },
                                label = { Text(j, fontSize = 11.5.sp) }
                            )
                        }
                    }

                    if (rankedStudents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada riwayat siswa yang terdaftar berpuasa pada filter ini.",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp, max = 380.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(rankedStudents, key = { _, item -> "rank_${item.key}" }) { index, item ->
                                val rank = index + 1
                                val medalEmoji = when (rank) {
                                    1 -> "🥇"
                                    2 -> "🥈"
                                    3 -> "🥉"
                                    else -> "#$rank"
                                }
                                val isTop3 = rank <= 3
                                val cardBg = when (rank) {
                                    1 -> Color(0xFFFFFBEB)
                                    2 -> Color(0xFFF8FAFC)
                                    3 -> Color(0xFFFFF7ED)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                                val borderColor = when (rank) {
                                    1 -> Color(0xFFFACC15)
                                    2 -> Color(0xFFCBD5E1)
                                    3 -> Color(0xFFFDBA74)
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    border = BorderStroke(if (isTop3) 1.5.dp else 1.dp, borderColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (isTop3) borderColor.copy(alpha = 0.25f) else Color(0xFFF3E8FF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = medalEmoji,
                                                fontSize = if (isTop3) 18.sp else 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = purpleDark
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.studentName,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${item.jenjang} • ${item.studentClass.ifBlank { "-" }} • ${item.studentDorm}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Sahur: ${item.sahurCount}x • Berbuka: ${item.bukaCount}x",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFF3E8FF))
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = "${item.sessionCount}x Puasa",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = purpleDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (sortedSessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada riwayat sesi puasa yang dibuat oleh Admin Sekolah.",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp, max = 380.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(sortedSessions, key = { "hist_sess_${it.id}" }) { session ->
                                val recs = fastingRecords.filter { it.sessionId == session.id }
                                val sma = recs.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SMA }
                                val smp = recs.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SMP }
                                val sd = recs.count { FastingJenjangHelper.resolveJenjang(it) == FastingJenjangHelper.JENJANG_SD }
                                val sahur = recs.count { it.isSahurChecked }
                                val buka = recs.count { it.isBukaChecked }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedHistorySession = session },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = session.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (session.isActive) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (session.isActive) "Aktif" else "Selesai",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (session.isActive) Color(0xFF15803D) else Color(0xFF475569)
                                                )
                                            }
                                        }
                                        Text(
                                            text = DateUtils.formatFullDate(session.sessionDate),
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Total: ${recs.size} Siswa ($sma SMA • $smp SMP • $sd SD)",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0F766E)
                                        )
                                        Text(
                                            text = "Sahur: $sahur/${recs.size} • Berbuka: $buka/${recs.size} • Ketuk untuk lihat peserta",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.outline
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

    selectedHistorySession?.let { sess ->
        val sessRecords = fastingRecords.filter { it.sessionId == sess.id }.sortedBy { it.studentName }
        AlertDialog(
            onDismissRequest = { selectedHistorySession = null },
            title = {
                Column {
                    Text(sess.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        DateUtils.formatFullDate(sess.sessionDate),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                if (sessRecords.isEmpty()) {
                    Text("Belum ada peserta terdaftar pada sesi ini.", fontSize = 13.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(sessRecords) { idx, rec ->
                            val jenjang = FastingJenjangHelper.resolveJenjang(rec)
                            Column {
                                Text(
                                    text = "${idx + 1}. ${rec.studentName} ($jenjang)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Sahur: ${if (rec.isSahurChecked) "✓" else "-"} • Berbuka: ${if (rec.isBukaChecked) "✓" else "-"}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedHistorySession = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}
