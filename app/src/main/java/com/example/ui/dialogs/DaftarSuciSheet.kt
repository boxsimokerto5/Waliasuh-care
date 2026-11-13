package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HaidRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun studentIdentityKey(record: HaidRecord): String {
    return when {
        record.studentId > 0L -> "id:${record.studentId}"
        record.studentNik.isNotBlank() -> "nik:${record.studentNik.trim().lowercase()}"
        record.studentNis.isNotBlank() -> "nis:${record.studentNis.trim().lowercase()}"
        else -> "name:${record.studentName.trim().lowercase()}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarSuciSheet(
    suciRecords: List<HaidRecord>,
    activeHaidRecords: List<HaidRecord> = emptyList(),
    onDeleteRecord: (HaidRecord) -> Unit,
    onMoveToHaid: (HaidRecord) -> Unit = {},
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var recordToDelete by remember { mutableStateOf<HaidRecord?>(null) }
    var recordPendingTissueCheck by remember { mutableStateOf<HaidRecord?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    val activeStudentKeys = remember(activeHaidRecords) {
        activeHaidRecords.map { studentIdentityKey(it) }.toSet()
    }

    // ID record suci terbaru untuk tiap siswi (selama siswi tsb tidak sedang aktif haid lagi)
    val latestSuciIdByStudentKey = remember(suciRecords, activeStudentKeys) {
        val map = mutableMapOf<String, Long>()
        suciRecords
            .sortedByDescending { it.effectiveEndDate ?: it.endDate ?: it.startDate }
            .forEach { rec ->
                val key = studentIdentityKey(rec)
                if (!activeStudentKeys.contains(key) && !map.containsKey(key)) {
                    map[key] = rec.id
                }
            }
        map
    }

    val filteredRecords = remember(searchQuery, suciRecords) {
        val sorted = suciRecords.sortedByDescending { it.effectiveEndDate ?: it.endDate ?: it.startDate }
        if (searchQuery.isBlank()) {
            sorted
        } else {
            val q = searchQuery.trim().lowercase()
            sorted.filter {
                it.studentName.lowercase().contains(q) ||
                    it.studentClass.lowercase().contains(q) ||
                    it.studentDorm.lowercase().contains(q) ||
                    it.studentNik.contains(q)
            }
        }
    }

    val inMinPeriodCount = remember(suciRecords, latestSuciIdByStudentKey) {
        suciRecords.count { rec ->
            latestSuciIdByStudentKey[studentIdentityKey(rec)] == rec.id && rec.isInMinimumSuciPeriod
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
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Daftar Siswi Suci & Timeline",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${suciRecords.size} Catatan Suci • Pantau Hari Suci ke-...",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_daftar_suci")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ringkasan Statistik Suci
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Total Catatan Suci", fontSize = 10.sp, color = Color(0xFF15803D))
                            Text(
                                text = "${suciRecords.size} Siklus",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Masa Pantau (<15h Suci)", fontSize = 10.sp, color = Color(0xFF0369A1))
                            Text(
                                text = "$inMinPeriodCount Siswi Aktif",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF075985)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama siswi suci...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_daftar_suci")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // List Siswi Suci
                if (filteredRecords.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (suciRecords.isEmpty()) "Belum ada siswi di Daftar Suci" else "Tidak ada siswi yang cocok dengan pencarian",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Siswi yang mandi suci atau melewati 15 hari haid akan otomatis masuk ke sini.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = 430.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredRecords, key = { it.id }) { record ->
                            val isCurrentSuciStatus = latestSuciIdByStudentKey[studentIdentityKey(record)] == record.id
                            val suciDay = record.suciDayCount
                            val isStrictWatch = isCurrentSuciStatus && record.isInMinimumSuciPeriod

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("item_suci_${record.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isStrictWatch) Color(0xFFF0F9FF) else Color(0xFFF0FDF4)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isStrictWatch) Color(0xFFBAE6FD) else Color(0xFFBBF7D0)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isStrictWatch) Color(0xFFE0F2FE) else Color(0xFFDCFCE7)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = if (isStrictWatch) Color(0xFF0284C7) else Color(0xFF16A34A),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = record.studentName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    color = if (isStrictWatch) Color(0xFF0C4A6E) else Color(0xFF14532D)
                                                )
                                                Text(
                                                    text = "${record.studentClass} • ${record.studentDorm}${if (record.studentNik.isNotBlank()) " • NIK: ${record.studentNik}" else ""}",
                                                    fontSize = 10.5.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFF15803D).copy(alpha = 0.8f)
                                                )
                                            }
                                        }

                                        // Badge Hari Suci Berjalan (atau Riwayat Lama)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when {
                                                        !isCurrentSuciStatus -> Color(0xFF64748B)
                                                        isStrictWatch -> Color(0xFF0284C7)
                                                        else -> Color(0xFF16A34A)
                                                    }
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isCurrentSuciStatus) "Suci Hari ke-$suciDay" else "Arsip (${record.durationDays}h)",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // Timeline Masa Suci Berjalan untuk status suci terkini siswi
                                    if (isCurrentSuciStatus) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        SuciTimelineBar(suciDay = suciDay)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Info Periode Haid Sebelumnya
                                    val startStr = dateFormat.format(Date(record.startDate))
                                    val endMillis = record.effectiveEndDate ?: record.endDate ?: System.currentTimeMillis()
                                    val endStr = dateFormat.format(Date(endMillis))
                                    Text(
                                        text = "Siklus haid: ${record.durationDays} hari ($startStr s.d. $endStr)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (record.isAutoCompletedByLimit) Color(0xFFFEF3C7) else Color(0xFFDCFCE7))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (record.isAutoCompletedByLimit) {
                                                "Otomatis Pindah ke Daftar Suci (Melewati 15 Hari Haid)"
                                            } else {
                                                "Sudah Mandi Wajib (Siap Sholat, Puasa, Mengaji)"
                                            },
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (record.isAutoCompletedByLimit) Color(0xFF92400E) else Color(0xFF15803D)
                                        )
                                    }

                                    if (record.notes.isNotBlank()) {
                                        Text(
                                            text = "Catatan: ${record.notes}",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Verifikasi: ${record.waliName}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = { recordToDelete = record },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Hapus",
                                                    tint = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            if (isCurrentSuciStatus) {
                                                OutlinedButton(
                                                    onClick = {
                                                        if (record.isInMinimumSuciPeriod) {
                                                            recordPendingTissueCheck = record
                                                        } else {
                                                            onMoveToHaid(record)
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFDB2777)),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier
                                                        .heightIn(min = 40.dp)
                                                        .testTag("btn_move_suci_to_haid_${record.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.WaterDrop,
                                                        contentDescription = null,
                                                        tint = Color(0xFFDB2777),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Pindah ke Haid",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFDB2777)
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

    // Pop-up Peringatan & Verifikasi Tes Tisu bila siswi Suci (< 15 hari) hendak dipindah ke Haid
    recordPendingTissueCheck?.let { rec ->
        SuciTissueVerificationSheet(
            studentName = rec.studentName,
            suciRecord = rec,
            onCancelStaySuci = { recordPendingTissueCheck = null },
            onConfirmMoveToHaid = {
                recordPendingTissueCheck = null
                onMoveToHaid(rec)
            }
        )
    }

    // Bottom Sheet Konfirmasi Hapus
    if (recordToDelete != null) {
        val rec = recordToDelete!!
        ModalBottomSheet(
            onDismissRequest = { recordToDelete = null },
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
                        .widthIn(max = 520.dp)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "Hapus Riwayat Suci?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Text(
                        text = "Hapus data riwayat siklus haid ${rec.studentName}?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { recordToDelete = null },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                onDeleteRecord(rec)
                                recordToDelete = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Hapus", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
