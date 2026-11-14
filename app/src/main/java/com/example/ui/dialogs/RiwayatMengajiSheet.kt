package com.example.ui.dialogs

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.FastingJenjangHelper
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings

private data class StudentMengajiSummary(
    val student: ChildStudent,
    val records: List<MengajiRecord>,
    val latest: MengajiRecord?,
    val fluentCount: Int,
    val notFluentCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatMengajiSheet(
    students: List<ChildStudent>,
    mengajiRecords: List<MengajiRecord>,
    initialTab: Int = 0,
    onContinueStudent: (ChildStudent) -> Unit,
    onOpenKartuForStudent: (ChildStudent) -> Unit,
    onEditRecord: (MengajiRecord) -> Unit,
    onDeleteRecord: (MengajiRecord) -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language

    val emeraldPrimary = Color(0xFF047857)
    val emeraldLight = Color(0xFFD1FAE5)

    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var searchQuery by remember { mutableStateOf("") }
    var fluencyFilter by remember { mutableStateOf("ALL") } // ALL | LANCAR | TIDAK_LANCAR
    var rekapFilter by remember { mutableStateOf("ALL") } // ALL | NEED_PRACTICE | NO_LOG

    val sortedRecords = remember(mengajiRecords) {
        mengajiRecords.sortedWith(
            compareByDescending<MengajiRecord> { it.sessionDate }.thenByDescending { it.createdAt }
        )
    }

    val filteredHistory = remember(sortedRecords, searchQuery, fluencyFilter) {
        val q = searchQuery.trim().lowercase()
        sortedRecords.filter { rec ->
            val matchQuery = q.isBlank() ||
                rec.studentName.lowercase().contains(q) ||
                rec.bookType.lowercase().contains(q) ||
                rec.jilidOrJuz.lowercase().contains(q) ||
                rec.studentClass.lowercase().contains(q)
            val matchFluency = when (fluencyFilter) {
                "LANCAR" -> MengajiHelper.isFluent(rec.fluencyStatus)
                "TIDAK_LANCAR" -> !MengajiHelper.isFluent(rec.fluencyStatus)
                else -> true
            }
            matchQuery && matchFluency
        }
    }

    val studentSummaries = remember(students, mengajiRecords) {
        students.map { st ->
            val recs = MengajiHelper.recordsForStudent(mengajiRecords, st)
            val fluent = recs.count { MengajiHelper.isFluent(it.fluencyStatus) }
            StudentMengajiSummary(
                student = st,
                records = recs,
                latest = recs.firstOrNull(),
                fluentCount = fluent,
                notFluentCount = recs.size - fluent
            )
        }.sortedWith(
            compareByDescending<StudentMengajiSummary> { it.latest != null }
                .thenByDescending { it.latest?.sessionDate ?: 0L }
                .thenBy { it.student.name.lowercase() }
        )
    }

    val filteredRekap = remember(studentSummaries, searchQuery, rekapFilter) {
        val q = searchQuery.trim().lowercase()
        studentSummaries.filter { item ->
            val matchQuery = q.isBlank() ||
                item.student.name.lowercase().contains(q) ||
                item.student.classGrade.lowercase().contains(q) ||
                (item.latest?.jilidOrJuz?.lowercase()?.contains(q) == true)
            val matchStatus = when (rekapFilter) {
                "NEED_PRACTICE" -> item.latest != null && !MengajiHelper.isFluent(item.latest.fluencyStatus)
                "NO_LOG" -> item.latest == null
                else -> true
            }
            matchQuery && matchStatus
        }
    }

    fun resolveStudentFromRecord(rec: MengajiRecord): ChildStudent {
        return students.firstOrNull { MengajiHelper.matchesStudent(rec, it) }
            ?: ChildStudent(
                id = rec.studentId,
                name = rec.studentName,
                nis = rec.studentNis,
                gender = "L",
                age = 12,
                dormitory = rec.studentDorm,
                classGrade = rec.studentClass,
                nik = rec.studentNik,
                jenjang = rec.studentJenjang,
                schoolId = rec.schoolId
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
                    .widthIn(max = 660.dp)
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Mengaji History & Volumes" else "Riwayat & Rekap Mengaji Anak",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isEn) {
                                "${mengajiRecords.size} total entries • Track continuous progress per student"
                            } else {
                                "${mengajiRecords.size} setoran tercatat • Pantau kelanjutan bacaan tiap anak"
                            },
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
                            .testTag("btn_close_riwayat_mengaji")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close, modifier = Modifier.size(20.dp))
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = emeraldPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = if (isEn) "Daily Log (${mengajiRecords.size})" else "Riwayat Harian (${mengajiRecords.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("tab_riwayat_mengaji")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = if (isEn) "Student Progress (${students.size})" else "Rekap Jilid Anak (${students.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("tab_rekap_mengaji")
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (isEn) "Search student, book, or volume..." else "Cari nama siswa, kitab, atau jilid...",
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = emeraldPrimary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = if (isEn) "Clear" else "Bersihkan")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_riwayat_mengaji")
                )

                if (selectedTab == 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filterItems = listOf(
                            "ALL" to if (isEn) "All" else "Semua",
                            "LANCAR" to if (isEn) "Fluent" else "Lancar",
                            "TIDAK_LANCAR" to if (isEn) "Needs Practice" else "Tidak Lancar"
                        )
                        filterItems.forEach { (key, label) ->
                            FilterChip(
                                selected = fluencyFilter == key,
                                onClick = { fluencyFilter = key },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = emeraldLight,
                                    selectedLabelColor = emeraldPrimary
                                )
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .testTag("list_riwayat_mengaji"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (filteredHistory.isEmpty()) {
                            item(key = "riwayat_empty") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isEn) "No Mengaji records yet." else "Belum ada riwayat mengaji yang tercatat.",
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(filteredHistory, key = { "hist_${it.id}" }) { rec ->
                                val isFluent = MengajiHelper.isFluent(rec.fluencyStatus)
                                val resolvedStudent = resolveStudentFromRecord(rec)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("card_riwayat_mengaji_${rec.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isFluent) Color(0xFFBBF7D0) else Color(0xFFFCA5A5)
                                    )
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
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = rec.studentName,
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${rec.studentClass.ifBlank { "-" }} • ${DateUtils.formatShortDate(rec.sessionDate, lang)} • ${rec.waliName}",
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isFluent) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                            ) {
                                                Text(
                                                    text = MengajiHelper.fluencyLabel(rec.fluencyStatus, isEn),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isFluent) Color(0xFF15803D) else Color(0xFFB91C1C),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(emeraldLight.copy(alpha = 0.55f))
                                                .padding(horizontal = 9.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = emeraldPrimary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = MengajiHelper.formatCompactPosition(rec),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF065F46)
                                            )
                                        }

                                        if (rec.notes.isNotBlank()) {
                                            Text(
                                                text = "${if (isEn) "Note" else "Catatan"}: ${rec.notes}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = { onContinueStudent(resolvedStudent) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp), tint = emeraldPrimary)
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(if (isEn) "Continue" else "Lanjut", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = emeraldPrimary)
                                            }
                                            OutlinedButton(
                                                onClick = { onOpenKartuForStudent(resolvedStudent) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF0369A1))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(if (isEn) "Card" else "Kartu", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(onClick = { onEditRecord(rec) }, modifier = Modifier.size(34.dp)) {
                                                Icon(Icons.Default.Edit, contentDescription = if (isEn) "Edit" else "Edit", tint = emeraldPrimary, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = { onDeleteRecord(rec) }, modifier = Modifier.size(34.dp)) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = if (isEn) "Delete" else "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val rekapItems = listOf(
                            "ALL" to if (isEn) "All Students" else "Semua Anak",
                            "NEED_PRACTICE" to if (isEn) "Needs Practice" else "Perlu Ulang",
                            "NO_LOG" to if (isEn) "No Log Yet" else "Belum Mengaji"
                        )
                        rekapItems.forEach { (key, label) ->
                            FilterChip(
                                selected = rekapFilter == key,
                                onClick = { rekapFilter = key },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = emeraldLight,
                                    selectedLabelColor = emeraldPrimary
                                )
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .testTag("list_rekap_mengaji"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredRekap, key = { "rekap_${it.student.id}" }) { summary ->
                            val latest = summary.latest
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_rekap_mengaji_${summary.student.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = summary.student.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${FastingJenjangHelper.resolveJenjang(summary.student)} • ${summary.student.classGrade.ifBlank { "-" }}",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        if (latest != null) {
                                            Text(
                                                text = "${MengajiHelper.formatCompactPosition(latest)} (${MengajiHelper.fluencyLabel(latest.fluencyStatus, isEn)})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (MengajiHelper.isFluent(latest.fluencyStatus)) emeraldPrimary else Color(0xFFB91C1C)
                                            )
                                            Text(
                                                text = if (isEn) {
                                                    "${summary.records.size} sessions (${summary.fluentCount} fluent, ${summary.notFluentCount} repeat)"
                                                } else {
                                                    "${summary.records.size}x setoran (${summary.fluentCount} lancar, ${summary.notFluentCount} ulang)"
                                                },
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            Text(
                                                text = if (isEn) "No Mengaji entries recorded yet" else "Belum ada setoran mengaji",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Button(
                                            onClick = { onContinueStudent(summary.student) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(if (isEn) "Next" else "Lanjut", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = { onOpenKartuForStudent(summary.student) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF0369A1))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(if (isEn) "Card" else "Kartu", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
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
