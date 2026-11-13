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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HaidDateCalculator
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
    var selectedDetailRecord by remember { mutableStateOf<HaidRecord?>(null) }
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
                            val accentColor = when {
                                !isCurrentSuciStatus -> Color(0xFF64748B)
                                isStrictWatch -> Color(0xFF0284C7)
                                else -> Color(0xFF16A34A)
                            }
                            val trackColor = if (isStrictWatch) Color(0xFFE0F2FE) else Color(0xFFDCFCE7)

                            Card(
                                onClick = { selectedDetailRecord = record },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("item_suci_${record.id}"),
                                shape = RoundedCornerShape(12.dp),
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
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(7.dp)
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
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                                    tint = accentColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = record.studentName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    color = if (isStrictWatch) Color(0xFF0C4A6E) else Color(0xFF14532D),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = record.studentClass.ifBlank { record.studentDorm.ifBlank { "-" } },
                                                    fontSize = 11.5.sp,
                                                    color = (if (isStrictWatch) Color(0xFF0369A1) else Color(0xFF15803D)).copy(alpha = 0.85f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(accentColor)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = if (isCurrentSuciStatus) "Suci Hari ke-$suciDay" else "Arsip (${record.durationDays}h)",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = "Lihat detail ${record.studentName}",
                                                tint = accentColor.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // Garis progress tipis masa suci 1..15 hari untuk status terkini
                                    if (isCurrentSuciStatus) {
                                        val progressFraction = suciDay.coerceIn(1, HaidDateCalculator.MIN_SUCI_DAYS) /
                                            HaidDateCalculator.MIN_SUCI_DAYS.toFloat()
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(3.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(trackColor)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(progressFraction)
                                                    .height(3.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(accentColor)
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

    // Pop-up Detail Lengkap saat kartu slim Daftar Suci diklik
    selectedDetailRecord?.let { rec ->
        val isCurrentSuciStatus = latestSuciIdByStudentKey[studentIdentityKey(rec)] == rec.id
        val suciDay = rec.suciDayCount
        val isStrictWatch = isCurrentSuciStatus && rec.isInMinimumSuciPeriod
        val startStr = dateFormat.format(Date(rec.startDate))
        val endMillis = rec.effectiveEndDate ?: rec.endDate ?: System.currentTimeMillis()
        val endStr = dateFormat.format(Date(endMillis))

        ModalBottomSheet(
            onDismissRequest = { selectedDetailRecord = null },
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
                        .widthIn(max = 560.dp)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isStrictWatch) Color(0xFFE0F2FE) else Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isStrictWatch) Color(0xFF0284C7) else Color(0xFF16A34A),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rec.studentName,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${rec.studentClass} • ${rec.studentDorm}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (rec.studentNik.isNotBlank()) {
                                    Text(
                                        text = "NIK: ${rec.studentNik}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { selectedDetailRecord = null },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup detail")
                        }
                    }

                    if (isCurrentSuciStatus) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isStrictWatch) Color(0xFFF0F9FF) else Color(0xFFF0FDF4)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isStrictWatch) Color(0xFFBAE6FD) else Color(0xFFBBF7D0)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                SuciTimelineBar(suciDay = suciDay)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (rec.isAutoCompletedByLimit) Color(0xFFFEF3C7) else Color(0xFFDCFCE7))
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = if (rec.isAutoCompletedByLimit) {
                                "Otomatis Pindah ke Daftar Suci (Melewati 15 Hari Haid)"
                            } else {
                                "Sudah Mandi Wajib (Siap Sholat, Puasa, Mengaji)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (rec.isAutoCompletedByLimit) Color(0xFF92400E) else Color(0xFF15803D)
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Siklus haid: ${rec.durationDays} hari ($startStr s.d. $endStr)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Verifikasi: ${rec.waliName}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (rec.notes.isNotBlank()) {
                                Text(
                                    text = "Catatan: ${rec.notes}",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedDetailRecord = null
                                recordToDelete = rec
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_detail_delete_suci_${rec.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hapus", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                        }

                        if (isCurrentSuciStatus) {
                            Button(
                                onClick = {
                                    selectedDetailRecord = null
                                    if (rec.isInMinimumSuciPeriod) {
                                        recordPendingTissueCheck = rec
                                    } else {
                                        onMoveToHaid(rec)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_move_suci_to_haid_${rec.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pindah ke Haid",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
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
