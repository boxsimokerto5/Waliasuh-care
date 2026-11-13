package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SchoolProfile
import com.example.data.model.ShiftMessage
import com.example.data.model.WaliAccount
import com.example.ui.HealthViewModel
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftMessagesSheet(
    viewModel: HealthViewModel,
    messages: List<ShiftMessage>,
    waliAccounts: List<WaliAccount>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var messageToShare by remember { mutableStateOf<ShiftMessage?>(null) }
    // 0 = Hari Ini (default), 1 = Akan Datang (Besok & Seterusnya)
    var selectedDateTab by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf("Aktif") } // Aktif, Urgent, Selesai, Semua

    val todayMessages = remember(messages) {
        messages.filter { DateUtils.isToday(it.followUpDate) }
    }
    val upcomingMessages = remember(messages) {
        messages.filter { DateUtils.isTomorrowOrLater(it.followUpDate) }
            .sortedBy { it.followUpDate }
    }

    val categoryMessages = if (selectedDateTab == 0) todayMessages else upcomingMessages

    val filteredMessages = remember(categoryMessages, selectedFilter) {
        when (selectedFilter) {
            "Aktif" -> categoryMessages.filter { !it.isCompleted }
            "Urgent" -> categoryMessages.filter { it.priority == "URGENT" && !it.isCompleted }
            "Selesai" -> categoryMessages.filter { it.isCompleted }
            else -> categoryMessages
        }
    }

    val activeCount = categoryMessages.count { !it.isCompleted }
    val urgentCount = categoryMessages.count { it.priority == "URGENT" && !it.isCompleted }
    val completedCount = categoryMessages.count { it.isCompleted }

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
                // Adaptive Header Bar dengan Tombol Tinggalkan Pesan
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isCompactWidth = maxWidth < 360.dp
                    if (isCompactWidth) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ShiftHeaderTitleBlock(urgentCount = urgentCount)
                            Button(
                                onClick = { showCreateDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_open_create_shift_message")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tinggalkan Pesan",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShiftHeaderTitleBlock(
                                urgentCount = urgentCount,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { showCreateDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_open_create_shift_message")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tinggalkan Pesan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // Tab Kategori Tanggal: Hari Ini vs Akan Datang (Besok & Seterusnya)
                PrimaryTabRow(
                    selectedTabIndex = selectedDateTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = WaliPrimary
                ) {
                    Tab(
                        selected = selectedDateTab == 0,
                        onClick = { selectedDateTab = 0 },
                        modifier = Modifier.testTag("tab_shift_today"),
                        text = {
                            Text(
                                text = "Hari Ini (${todayMessages.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedDateTab == 0) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                    Tab(
                        selected = selectedDateTab == 1,
                        onClick = { selectedDateTab = 1 },
                        modifier = Modifier.testTag("tab_shift_upcoming"),
                        text = {
                            Text(
                                text = "Akan Datang (${upcomingMessages.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedDateTab == 1) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }

                // Secondary Status Filter Bar (Scrollable horizontally to prevent squeezing)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Aktif", "Urgent", "Selesai", "Semua").forEach { filterOpt ->
                        val countText = when (filterOpt) {
                            "Aktif" -> " ($activeCount)"
                            "Urgent" -> if (urgentCount > 0) " ($urgentCount)" else ""
                            "Selesai" -> " ($completedCount)"
                            else -> " (${categoryMessages.size})"
                        }
                        FilterChip(
                            selected = selectedFilter == filterOpt,
                            onClick = { selectedFilter = filterOpt },
                            label = {
                                Text(
                                    text = "$filterOpt$countText",
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            modifier = Modifier.testTag("chip_shift_filter_${filterOpt.lowercase()}")
                        )
                    }
                }

                // List Kartu Pesan
                if (filteredMessages.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
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
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = WaliPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = when {
                                    selectedDateTab == 0 && selectedFilter == "Aktif" ->
                                        "Tidak ada pesan operan shift aktif untuk hari ini."
                                    selectedDateTab == 1 && selectedFilter == "Aktif" ->
                                        "Belum ada pesan shift yang akan datang (besok & seterusnya)."
                                    selectedDateTab == 0 ->
                                        "Tidak ada pesan hari ini pada filter $selectedFilter."
                                    else ->
                                        "Tidak ada pesan yang akan datang pada filter $selectedFilter."
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Tekan tombol '+ Tinggalkan Pesan' untuk menulis pesan operan jaga sesuai tanggal tugasnya.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = 440.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredMessages, key = { it.id }) { msg ->
                            ShiftMessageCardItem(
                                message = msg,
                                onShareWhatsApp = {
                                    messageToShare = msg
                                },
                                onToggleComplete = {
                                    viewModel.toggleShiftMessageCompleted(msg) {
                                        val status = if (!msg.isCompleted) "ditandai selesai" else "dibuka kembali"
                                        Toast.makeText(context, "Pesan shift $status.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDelete = {
                                    viewModel.deleteShiftMessage(msg) {
                                        Toast.makeText(context, "Pesan shift dihapus.", Toast.LENGTH_SHORT).show()
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
                            .testTag("btn_close_shift_messages")
                    ) {
                        Text("Tutup")
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateShiftMessageDialog(
            viewModel = viewModel,
            waliAccounts = waliAccounts,
            onDismiss = { showCreateDialog = false }
        )
    }

    // Pop-up Share Pengingat Pesan Shift via WhatsApp
    messageToShare?.let { msg ->
        val reminderText = remember(msg.id, schoolProfile) {
            buildShiftReminderWhatsAppText(msg, schoolProfile)
        }
        val matchedWali = remember(msg.targetWali, waliAccounts) {
            val cleanTarget = msg.targetWali.trim()
            waliAccounts.firstOrNull {
                it.name.trim().equals(cleanTarget, ignoreCase = true) ||
                    it.username.trim().equals(cleanTarget, ignoreCase = true)
            }
        }
        val waPhone = remember(matchedWali) {
            matchedWali?.phone?.let { formatWhatsAppPhone(it) }
        }

        ModalBottomSheet(
            onDismissRequest = { messageToShare = null },
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ingatkan via WhatsApp",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Kepada: ${msg.targetWali} • ${msg.shift}",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = { messageToShare = null },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = reminderText,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }

                    if (waPhone != null && matchedWali != null) {
                        Button(
                            onClick = {
                                val uri = Uri.parse("https://wa.me/$waPhone?text=${Uri.encode(reminderText)}")
                                val waIntent = Intent(Intent.ACTION_VIEW, uri)
                                runCatching {
                                    messageToShare = null
                                    context.startActivity(waIntent)
                                }.onFailure {
                                    Toast.makeText(context, "Tidak dapat membuka WhatsApp.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_direct_wa_shift_${msg.id}")
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Chat Langsung WA ke ${matchedWali.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Pengingat Shift", reminderText))
                                Toast.makeText(context, "Teks pengingat disalin!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_copy_shift_wa_${msg.id}")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salin Teks", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, reminderText)
                                }
                                runCatching {
                                    messageToShare = null
                                    context.startActivity(
                                        Intent.createChooser(sendIntent, "Bagikan Pengingat Shift ke WhatsApp")
                                    )
                                }.onFailure {
                                    Toast.makeText(context, "Gagal membuka menu berbagi.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (waPhone == null) Color(0xFF059669) else WaliPrimary
                            ),
                            modifier = Modifier
                                .weight(1.35f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_share_group_shift_${msg.id}")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bagikan ke Grup WA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatWhatsAppPhone(raw: String): String? {
    val digits = raw.filter { it.isDigit() }
    if (digits.length < 8) return null
    return when {
        digits.startsWith("0") -> "62${digits.drop(1)}"
        digits.startsWith("62") -> digits
        else -> digits
    }
}

private fun buildShiftReminderWhatsAppText(
    msg: ShiftMessage,
    schoolProfile: SchoolProfile
): String {
    val priorityBadge = when (msg.priority) {
        "URGENT" -> "🔴 *URGENT*"
        "SEDANG" -> "🟠 *SEDANG*"
        else -> "🟢 *BIASA*"
    }
    val schoolLabel = schoolProfile.schoolName.trim().ifBlank {
        schoolProfile.shortName.trim().ifBlank { "Wali Asuh Care" }
    }
    val statusLabel = if (msg.isCompleted) "✅ Sudah Selesai" else "⏳ Belum Selesai (Mohon Tindak Lanjut)"
    return buildString {
        appendLine("🔔 *PENGINGAT OPERAN SHIFT WALI ASUH*")
        appendLine("*$schoolLabel*")
        appendLine("----------------------------------------")
        appendLine("$priorityBadge • ${msg.shift}")
        appendLine("📌 *Tugas:* ${msg.title}")
        appendLine("👤 *Kepada:* ${msg.targetWali}")
        appendLine("📅 *Tindak Lanjut:* ${DateUtils.formatFullDate(msg.followUpDate)}")
        appendLine("📝 *Isi Pesan:*")
        appendLine(msg.messageContent)
        appendLine("----------------------------------------")
        appendLine("Status: $statusLabel")
        append("Dibuat oleh: ${msg.senderName}")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShiftHeaderTitleBlock(
    urgentCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Pesan Shift Berikutnya",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            if (urgentCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDC2626))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$urgentCount URGENT",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
        Text(
            text = "Serah terima tugas jaga & operan antar wali asuh",
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiftMessageCardItem(
    message: ShiftMessage,
    onShareWhatsApp: () -> Unit = {},
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

    val cardBorder = if (isUrgent && !message.isCompleted) Color(0xFFFCA5A5) else Color.Transparent

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_shift_message_${message.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (message.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (message.isCompleted) 0.5.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(if (isUrgent && !message.isCompleted) 1.5.dp else 0.dp, cardBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Baris 1: Badges Prioritas & Shift (wrapping safely) + Delete Button (48dp target)
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

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onShareWhatsApp,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_share_shift_message_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Ingatkan via WhatsApp",
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_delete_shift_message_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Pesan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Judul Pesan
            Text(
                text = message.title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (message.isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            // Info Penerima & Tanggal Tindak Lanjut (No text collision on small screens)
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
                        text = "Tindak Lanjut: ${DateUtils.formatFullDate(message.followUpDate)}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Konten Isi Pesan
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

            // Footer: Pembuat Pesan & Checklist Selesai
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
                        .testTag("btn_toggle_shift_complete_${message.id}")
                ) {
                    Icon(
                        imageVector = if (message.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (message.isCompleted) "Selesai" else "Tandai Selesai",
                        tint = if (message.isCompleted) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (message.isCompleted) "Selesai (${message.completedBy.ifBlank { "Wali" }})" else "Tandai Selesai",
                        fontSize = 11.5.sp,
                        fontWeight = if (message.isCompleted) FontWeight.Bold else FontWeight.Medium,
                        color = if (message.isCompleted) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
