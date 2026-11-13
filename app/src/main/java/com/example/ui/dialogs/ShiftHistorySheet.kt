package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShiftMessage
import com.example.ui.HealthViewModel
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils

data class ShiftHistoryMonthOption(
    val key: String,
    val label: String
)

object ShiftHistoryFilterHelper {
    fun getPastMessages(messages: List<ShiftMessage>): List<ShiftMessage> {
        return messages
            .filter { DateUtils.isBeforeToday(it.followUpDate) }
            .sortedByDescending { it.followUpDate }
    }

    fun getAvailableMonths(pastMessages: List<ShiftMessage>): List<ShiftHistoryMonthOption> {
        return pastMessages
            .map { DateUtils.getMonthKey(it.followUpDate) to DateUtils.formatMonthYear(it.followUpDate) }
            .distinctBy { it.first }
            .sortedByDescending { it.first }
            .map { ShiftHistoryMonthOption(key = it.first, label = it.second) }
    }

    fun filterMessages(
        pastMessages: List<ShiftMessage>,
        selectedMonthKey: String,
        selectedStatus: String,
        searchQuery: String
    ): List<ShiftMessage> {
        val cleanQuery = searchQuery.trim().lowercase()
        return pastMessages.filter { msg ->
            val matchesMonth = selectedMonthKey == "ALL" ||
                DateUtils.getMonthKey(msg.followUpDate) == selectedMonthKey
            val matchesStatus = when (selectedStatus) {
                "Belum Selesai" -> !msg.isCompleted
                "Selesai" -> msg.isCompleted
                else -> true
            }
            val matchesQuery = cleanQuery.isBlank() ||
                msg.title.lowercase().contains(cleanQuery) ||
                msg.messageContent.lowercase().contains(cleanQuery) ||
                msg.senderName.lowercase().contains(cleanQuery) ||
                msg.targetWali.lowercase().contains(cleanQuery) ||
                msg.shift.lowercase().contains(cleanQuery) ||
                msg.completedBy.lowercase().contains(cleanQuery)
            matchesMonth && matchesStatus && matchesQuery
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShiftHistorySheet(
    viewModel: HealthViewModel,
    messages: List<ShiftMessage>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedMonthKey by remember { mutableStateOf("ALL") }
    var selectedStatus by remember { mutableStateOf("Semua") } // Semua, Belum Selesai, Selesai

    val pastMessages = remember(messages) {
        ShiftHistoryFilterHelper.getPastMessages(messages)
    }

    val monthOptions = remember(pastMessages) {
        ShiftHistoryFilterHelper.getAvailableMonths(pastMessages)
    }

    // Base after month + search (for accurate status chip counts)
    val monthAndSearchFiltered = remember(pastMessages, selectedMonthKey, searchQuery) {
        ShiftHistoryFilterHelper.filterMessages(
            pastMessages = pastMessages,
            selectedMonthKey = selectedMonthKey,
            selectedStatus = "Semua",
            searchQuery = searchQuery
        )
    }

    val filteredMessages = remember(monthAndSearchFiltered, selectedStatus) {
        when (selectedStatus) {
            "Belum Selesai" -> monthAndSearchFiltered.filter { !it.isCompleted }
            "Selesai" -> monthAndSearchFiltered.filter { it.isCompleted }
            else -> monthAndSearchFiltered
        }
    }

    val totalPastCount = pastMessages.size
    val uncompletedPastCount = pastMessages.count { !it.isCompleted }
    val baseAllCount = monthAndSearchFiltered.size
    val baseUncompletedCount = monthAndSearchFiltered.count { !it.isCompleted }
    val baseCompletedCount = monthAndSearchFiltered.count { it.isCompleted }

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
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Riwayat Operan Shift",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                                if (uncompletedPastCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$uncompletedPastCount Terlewat",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                } else if (totalPastCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFDCFCE7))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$totalPastCount Arsip",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Arsip pesan serah terima yang sudah terlewat dari hari ini",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Search Bar (Judul, Isi, Nama Wali, Shift)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Cari judul, isi operan, atau nama wali...",
                            fontSize = 12.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari Riwayat",
                            tint = WaliPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_clear_shift_history_search")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus Pencarian"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_shift_history")
                )

                // Month Filter Bar
                if (monthOptions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        FilterChip(
                            selected = selectedMonthKey == "ALL",
                            onClick = { selectedMonthKey = "ALL" },
                            label = {
                                Text(
                                    text = "Semua Bulan",
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            modifier = Modifier.testTag("chip_history_month_all")
                        )
                        monthOptions.forEach { monthOpt ->
                            FilterChip(
                                selected = selectedMonthKey == monthOpt.key,
                                onClick = { selectedMonthKey = monthOpt.key },
                                label = {
                                    Text(
                                        text = monthOpt.label,
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                modifier = Modifier.testTag("chip_history_month_${monthOpt.key}")
                            )
                        }
                    }
                }

                // Status Filter Chips (Semua, Belum Selesai, Selesai)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedStatus == "Semua",
                        onClick = { selectedStatus = "Semua" },
                        label = {
                            Text(
                                text = "Semua ($baseAllCount)",
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        modifier = Modifier.testTag("chip_history_status_all")
                    )
                    FilterChip(
                        selected = selectedStatus == "Belum Selesai",
                        onClick = { selectedStatus = "Belum Selesai" },
                        label = {
                            Text(
                                text = "Belum Selesai ($baseUncompletedCount)",
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEF3C7),
                            selectedLabelColor = Color(0xFFD97706)
                        ),
                        modifier = Modifier.testTag("chip_history_status_uncompleted")
                    )
                    FilterChip(
                        selected = selectedStatus == "Selesai",
                        onClick = { selectedStatus = "Selesai" },
                        label = {
                            Text(
                                text = "Selesai ($baseCompletedCount)",
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDCFCE7),
                            selectedLabelColor = Color(0xFF16A34A)
                        ),
                        modifier = Modifier.testTag("chip_history_status_completed")
                    )
                }

                // List Riwayat
                if (filteredMessages.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = if (pastMessages.isEmpty()) {
                                    "Belum ada riwayat operan shift dari hari-hari sebelumnya."
                                } else {
                                    "Tidak ada riwayat operan yang sesuai dengan filter atau pencarian."
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (pastMessages.isEmpty()) {
                                    "Pesan shift yang tanggal tindak lanjutnya sudah terlewat dari hari ini akan otomatis masuk ke sini."
                                } else {
                                    "Ubah kata kunci pencarian, bulan, atau filter status untuk melihat riwayat lainnya."
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            if (pastMessages.isNotEmpty() && (searchQuery.isNotBlank() || selectedMonthKey != "ALL" || selectedStatus != "Semua")) {
                                TextButton(
                                    onClick = {
                                        searchQuery = ""
                                        selectedMonthKey = "ALL"
                                        selectedStatus = "Semua"
                                    },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text("Reset Semua Filter")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredMessages, key = { it.id }) { msg ->
                            ShiftHistoryCardItem(
                                message = msg,
                                onToggleComplete = {
                                    viewModel.toggleShiftMessageCompleted(msg) {
                                        val status = if (!msg.isCompleted) "ditandai selesai" else "dibuka kembali"
                                        Toast.makeText(context, "Riwayat operan $status.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDelete = {
                                    viewModel.deleteShiftMessage(msg) {
                                        Toast.makeText(context, "Riwayat operan dihapus.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }

                // Tutup Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("btn_close_shift_history")
                    ) {
                        Text("Tutup", maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiftHistoryCardItem(
    message: ShiftMessage,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUrgent = message.priority == "URGENT"
    val isSedang = message.priority == "SEDANG"

    val priorityColor = when {
        isUrgent -> Color(0xFFDC2626)
        isSedang -> Color(0xFFD97706)
        else -> Color(0xFF16A34A)
    }

    val priorityBg = when {
        isUrgent -> Color(0xFFFEE2E2)
        isSedang -> Color(0xFFFEF3C7)
        else -> Color(0xFFDCFCE7)
    }

    val cardBorderColor = if (!message.isCompleted) Color(0xFFFCD34D) else Color.Transparent

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_shift_history_${message.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (message.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (message.isCompleted) 0.5.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(if (!message.isCompleted) 1.2.dp else 0.dp, cardBorderColor, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Badges Row + Delete Icon (48dp touch target)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Status Operan Terlewat / Selesai
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (message.isCompleted) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (message.isCompleted) "Selesai" else "Terlewat • Belum Selesai",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (message.isCompleted) Color(0xFF16A34A) else Color(0xFFD97706),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isUrgent) "🔴 URGENT" else if (isSedang) "🟠 SEDANG" else "🟢 BIASA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Shift Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE0F2FE))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = message.shift,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WaliPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_delete_shift_history_${message.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Riwayat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Judul
            Text(
                text = message.title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (message.isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            // Info Penerima & Tanggal Tugas
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = WaliPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Kepada: ${message.targetWali}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = WaliPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Tanggal Tugas: ${DateUtils.formatFullDate(message.followUpDate)}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Isi Pesan
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message.messageContent,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            if (message.isCompleted && message.completedAt > 0L) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Ditindaklanjuti: ${DateUtils.formatDateTime(message.completedAt)}",
                        fontSize = 11.sp,
                        color = Color(0xFF16A34A)
                    )
                }
            }

            // Footer: Pembuat Pesan & Toggle Selesai (tetap bisa menandai pesan kemarin yg terlewat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dibuat oleh: ${message.senderName}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleComplete)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("btn_toggle_history_complete_${message.id}")
                ) {
                    Icon(
                        imageVector = if (message.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (message.isCompleted) "Selesai" else "Tandai Selesai",
                        tint = if (message.isCompleted) Color(0xFF16A34A) else Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (message.isCompleted) {
                            "Selesai (${message.completedBy.ifBlank { "Wali" }})"
                        } else {
                            "Tandai Selesai"
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (message.isCompleted) Color(0xFF16A34A) else Color(0xFFD97706),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
