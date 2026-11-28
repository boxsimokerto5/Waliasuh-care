package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShiftMessage
import com.example.ui.HealthViewModel
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings

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
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    var searchQuery by remember { mutableStateOf("") }
    var selectedMonthKey by remember { mutableStateOf("ALL") }
    var selectedStatus by remember { mutableStateOf("Semua") } // Semua, Belum Selesai, Selesai

    val pastMessages = remember(messages) {
        ShiftHistoryFilterHelper.getPastMessages(messages)
    }

    val monthOptions = remember(pastMessages, s.language) {
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

    val sheetBg = if (isLight) Color(0xFFFFF7FA) else MaterialTheme.colorScheme.surface
    val plumAccent = if (isLight) Color(0xFF9D174D) else Color(0xFFF9A8D4)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        containerColor = sheetBg
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
            ) {
                // Header Gradien Berry–Plum Lembut
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF701A75),
                                    Color(0xFF9D174D),
                                    Color(0xFFBE185D)
                                )
                            )
                        )
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.48f))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.5.dp, Color(0xFFFBCFE8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color(0xFF9D174D),
                                modifier = Modifier.size(21.dp)
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
                                    text = if (isEn) "Shift Handover History" else "Riwayat Operan Shift",
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                                if (uncompletedPastCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isEn) "$uncompletedPastCount Missed" else "$uncompletedPastCount Terlewat",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                } else if (totalPastCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(Color(0xFFDCFCE7))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isEn) "$totalPastCount Archived" else "$totalPastCount Arsip",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (isEn) {
                                    "Archive of past shift handover messages prior to today"
                                } else {
                                    "Arsip pesan serah terima yang sudah terlewat dari hari ini"
                                },
                                fontSize = 11.5.sp,
                                color = Color(0xFFFCE7F3)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.20f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = s.nav.close,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Isi Riwayat Bernuansa Pastel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search Bar Pastel Lembut
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = if (isEn) "Search title, handover note, or wali name..." else "Cari judul, isi operan, atau nama wali...",
                                fontSize = 12.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = if (isEn) "Search History" else "Cari Riwayat",
                                tint = plumAccent
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
                                        contentDescription = if (isEn) "Clear Search" else "Hapus Pencarian"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = plumAccent,
                            unfocusedBorderColor = if (isLight) Color(0xFFFBCFE8) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = if (isLight) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            unfocusedContainerColor = if (isLight) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_shift_history")
                    )

                    // Month Filter Bar (Nuansa Sky Pastel)
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
                                tint = if (isLight) Color(0xFF0369A1) else Color(0xFF7DD3FC),
                                modifier = Modifier.size(16.dp)
                            )
                            val allMonthSel = selectedMonthKey == "ALL"
                            FilterChip(
                                selected = allMonthSel,
                                onClick = { selectedMonthKey = "ALL" },
                                label = {
                                    Text(
                                        text = if (isEn) "All Months" else "Semua Bulan",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (allMonthSel) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = allMonthSel,
                                    borderColor = if (isLight) Color(0xFF7DD3FC) else Color(0xFF0284C7).copy(alpha = 0.5f),
                                    selectedBorderColor = if (isLight) Color(0xFF0284C7) else Color(0xFF7DD3FC)
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                                    labelColor = if (isLight) Color(0xFF0369A1) else Color(0xFF7DD3FC),
                                    selectedContainerColor = if (isLight) Color(0xFFE0F2FE) else Color(0xFF0C4A6E).copy(alpha = 0.7f),
                                    selectedLabelColor = if (isLight) Color(0xFF0369A1) else Color(0xFFE0F2FE)
                                ),
                                modifier = Modifier.testTag("chip_history_month_all")
                            )
                            monthOptions.forEach { monthOpt ->
                                val monthSel = selectedMonthKey == monthOpt.key
                                FilterChip(
                                    selected = monthSel,
                                    onClick = { selectedMonthKey = monthOpt.key },
                                    label = {
                                        Text(
                                            text = monthOpt.label,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (monthSel) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = monthSel,
                                        borderColor = if (isLight) Color(0xFF7DD3FC) else Color(0xFF0284C7).copy(alpha = 0.5f),
                                        selectedBorderColor = if (isLight) Color(0xFF0284C7) else Color(0xFF7DD3FC)
                                    ),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                                        labelColor = if (isLight) Color(0xFF0369A1) else Color(0xFF7DD3FC),
                                        selectedContainerColor = if (isLight) Color(0xFFE0F2FE) else Color(0xFF0C4A6E).copy(alpha = 0.7f),
                                        selectedLabelColor = if (isLight) Color(0xFF0369A1) else Color(0xFFE0F2FE)
                                    ),
                                    modifier = Modifier.testTag("chip_history_month_${monthOpt.key}")
                                )
                            }
                        }
                    }

                    // Status Filter Chips (Multi-Pastel: Lavender, Amber, Mint)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val allStatusSel = selectedStatus == "Semua"
                        FilterChip(
                            selected = allStatusSel,
                            onClick = { selectedStatus = "Semua" },
                            label = {
                                Text(
                                    text = "${s.nav.all} ($baseAllCount)",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (allStatusSel) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = allStatusSel,
                                borderColor = if (isLight) Color(0xFFD8B4FE) else Color(0xFF7E22CE).copy(alpha = 0.5f),
                                selectedBorderColor = if (isLight) Color(0xFF9333EA) else Color(0xFFD8B4FE)
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                                labelColor = if (isLight) Color(0xFF6B21A8) else Color(0xFFE9D5FF),
                                selectedContainerColor = if (isLight) Color(0xFFF3E8FF) else Color(0xFF581C87).copy(alpha = 0.7f),
                                selectedLabelColor = if (isLight) Color(0xFF6B21A8) else Color(0xFFF3E8FF)
                            ),
                            modifier = Modifier.testTag("chip_history_status_all")
                        )

                        val pendingSel = selectedStatus == "Belum Selesai"
                        FilterChip(
                            selected = pendingSel,
                            onClick = { selectedStatus = "Belum Selesai" },
                            label = {
                                Text(
                                    text = "${if (isEn) "Pending" else "Belum Selesai"} ($baseUncompletedCount)",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (pendingSel) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = pendingSel,
                                borderColor = if (isLight) Color(0xFFFCD34D) else Color(0xFFD97706).copy(alpha = 0.5f),
                                selectedBorderColor = if (isLight) Color(0xFFD97706) else Color(0xFFFCD34D)
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                                labelColor = if (isLight) Color(0xFFB45309) else Color(0xFFFDE68A),
                                selectedContainerColor = if (isLight) Color(0xFFFEF3C7) else Color(0xFF78350F).copy(alpha = 0.7f),
                                selectedLabelColor = if (isLight) Color(0xFFB45309) else Color(0xFFFEF3C7)
                            ),
                            modifier = Modifier.testTag("chip_history_status_uncompleted")
                        )

                        val completedSel = selectedStatus == "Selesai"
                        FilterChip(
                            selected = completedSel,
                            onClick = { selectedStatus = "Selesai" },
                            label = {
                                Text(
                                    text = "${if (isEn) "Completed" else "Selesai"} ($baseCompletedCount)",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (completedSel) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = completedSel,
                                borderColor = if (isLight) Color(0xFF86EFAC) else Color(0xFF16A34A).copy(alpha = 0.5f),
                                selectedBorderColor = if (isLight) Color(0xFF16A34A) else Color(0xFF86EFAC)
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                                labelColor = if (isLight) Color(0xFF15803D) else Color(0xFF86EFAC),
                                selectedContainerColor = if (isLight) Color(0xFFDCFCE7) else Color(0xFF14532D).copy(alpha = 0.7f),
                                selectedLabelColor = if (isLight) Color(0xFF15803D) else Color(0xFFDCFCE7)
                            ),
                            modifier = Modifier.testTag("chip_history_status_completed")
                        )
                    }

                    // List Riwayat
                    if (filteredMessages.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isLight) Color(0xFFFBCFE8) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLight) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
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
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(if (isLight) Color(0xFFFCE7F3) else Color(0xFF831843).copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = plumAccent,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Text(
                                    text = if (pastMessages.isEmpty()) {
                                        if (isEn) "No shift handover history from previous days yet."
                                        else "Belum ada riwayat operan shift dari hari-hari sebelumnya."
                                    } else {
                                        if (isEn) "No handover history matches the filter or search."
                                        else "Tidak ada riwayat operan yang sesuai dengan filter atau pencarian."
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (pastMessages.isEmpty()) {
                                        if (isEn) "Shift messages whose follow-up date has passed will automatically appear here."
                                        else "Pesan shift yang tanggal tindak lanjutnya sudah terlewat dari hari ini akan otomatis masuk ke sini."
                                    } else {
                                        if (isEn) "Change your search keyword, month, or status filter to view other history."
                                        else "Ubah kata kunci pencarian, bulan, atau filter status untuk melihat riwayat lainnya."
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
                                        Text(
                                            text = if (isEn) "Reset All Filters" else "Reset Semua Filter",
                                            color = plumAccent,
                                            fontWeight = FontWeight.Bold
                                        )
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
                                            val status = if (isEn) {
                                                if (!msg.isCompleted) "marked as completed" else "reopened"
                                            } else {
                                                if (!msg.isCompleted) "ditandai selesai" else "dibuka kembali"
                                            }
                                            Toast.makeText(
                                                context,
                                                if (isEn) "Handover history $status." else "Riwayat operan $status.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onDelete = {
                                        viewModel.deleteShiftMessage(msg) {
                                            Toast.makeText(
                                                context,
                                                if (isEn) "Handover history deleted." else "Riwayat operan dihapus.",
                                                Toast.LENGTH_SHORT
                                            ).show()
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
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isLight) Color(0xFFFBCFE8) else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("btn_close_shift_history")
                        ) {
                            Text(
                                text = s.nav.close,
                                fontWeight = FontWeight.SemiBold,
                                color = plumAccent,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
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
    val isEn = LocalAppStrings.current.isEnglish
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val isUrgent = message.priority == "URGENT"
    val isSedang = message.priority == "SEDANG"

    val priorityColor = when {
        isUrgent -> if (isLight) Color(0xFFBE123C) else Color(0xFFFDA4AF)
        isSedang -> if (isLight) Color(0xFFB45309) else Color(0xFFFCD34D)
        else -> if (isLight) Color(0xFF15803D) else Color(0xFF86EFAC)
    }

    val priorityBg = when {
        isUrgent -> if (isLight) Color(0xFFFFE4E6) else Color(0xFF881337).copy(alpha = 0.55f)
        isSedang -> if (isLight) Color(0xFFFEF3C7) else Color(0xFF78350F).copy(alpha = 0.55f)
        else -> if (isLight) Color(0xFFDCFCE7) else Color(0xFF14532D).copy(alpha = 0.55f)
    }

    // Kartu pastel berbeda sesuai status selesai & tingkat prioritas
    val (cardBg, cardBorder, innerNoteBg) = when {
        message.isCompleted -> Triple(
            if (isLight) Color(0xFFF0FDF6) else Color(0xFF10241C),
            if (isLight) Color(0xFF86EFAC) else Color(0xFF16A34A).copy(alpha = 0.45f),
            if (isLight) Color(0xFFE6FAEE) else Color(0xFF173327)
        )
        isUrgent -> Triple(
            if (isLight) Color(0xFFFFF5F6) else Color(0xFF2B1418),
            if (isLight) Color(0xFFFDA4AF) else Color(0xFFE11D48).copy(alpha = 0.55f),
            if (isLight) Color(0xFFFFECEF) else Color(0xFF391B21)
        )
        isSedang -> Triple(
            if (isLight) Color(0xFFFFFCF0) else Color(0xFF282012),
            if (isLight) Color(0xFFFCD34D) else Color(0xFFD97706).copy(alpha = 0.5f),
            if (isLight) Color(0xFFFEF6D8) else Color(0xFF342A18)
        )
        else -> Triple(
            if (isLight) Color(0xFFF7F5FF) else Color(0xFF1D1A30),
            if (isLight) Color(0xFFC4B5FD) else Color(0xFF7C3AED).copy(alpha = 0.45f),
            if (isLight) Color(0xFFEDE9FE) else Color(0xFF26223D)
        )
    }

    val recipientTint = if (isLight) Color(0xFF9D174D) else Color(0xFFF9A8D4)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_shift_history_${message.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardBg),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (message.isCompleted) 0.5.dp else 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
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
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                if (message.isCompleted) {
                                    if (isLight) Color(0xFFDCFCE7) else Color(0xFF14532D).copy(alpha = 0.65f)
                                } else {
                                    if (isLight) Color(0xFFFEF3C7) else Color(0xFF78350F).copy(alpha = 0.65f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (message.isCompleted) {
                                if (isEn) "Completed" else "Selesai"
                            } else {
                                if (isEn) "Missed • Pending" else "Terlewat • Belum Selesai"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (message.isCompleted) {
                                if (isLight) Color(0xFF15803D) else Color(0xFF86EFAC)
                            } else {
                                if (isLight) Color(0xFFB45309) else Color(0xFFFCD34D)
                            },
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(priorityBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = when {
                                isUrgent -> "🔴 URGENT"
                                isSedang -> if (isEn) "🟠 MEDIUM" else "🟠 SEDANG"
                                else -> if (isEn) "🟢 NORMAL" else "🟢 BIASA"
                            },
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
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (isLight) Color(0xFFE0F2FE) else Color(0xFF0C4A6E).copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = message.shift,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLight) Color(0xFF0369A1) else Color(0xFF7DD3FC),
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
                        contentDescription = if (isEn) "Delete History" else "Hapus Riwayat",
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
                        tint = recipientTint,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${if (isEn) "To" else "Kepada"}: ${message.targetWali}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = recipientTint
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
                        text = "${if (isEn) "Task Date" else "Tanggal Tugas"}: ${DateUtils.formatFullDate(message.followUpDate)}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Isi Pesan
            Card(
                colors = CardDefaults.cardColors(containerColor = innerNoteBg),
                shape = RoundedCornerShape(10.dp),
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
                        text = "${if (isEn) "Followed up" else "Ditindaklanjuti"}: ${DateUtils.formatDateTime(message.completedAt)}",
                        fontSize = 11.sp,
                        color = Color(0xFF16A34A)
                    )
                }
            }

            // Footer: Pembuat Pesan & Toggle Selesai
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isEn) "Created by" else "Dibuat oleh"}: ${message.senderName}",
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
                        contentDescription = if (message.isCompleted) {
                            if (isEn) "Completed" else "Selesai"
                        } else {
                            if (isEn) "Mark Completed" else "Tandai Selesai"
                        },
                        tint = if (message.isCompleted) Color(0xFF16A34A) else Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (message.isCompleted) {
                            "${if (isEn) "Done" else "Selesai"} (${message.completedBy.ifBlank { "Wali" }})"
                        } else {
                            if (isEn) "Mark Done" else "Tandai Selesai"
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
