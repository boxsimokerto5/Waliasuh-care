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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SchoolProfile
import com.example.data.model.ShiftMessage
import com.example.data.model.WaliAccount
import com.example.ui.HealthViewModel
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShiftMessagesSheet(
    viewModel: HealthViewModel,
    messages: List<ShiftMessage>,
    waliAccounts: List<WaliAccount>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
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

    val sheetBg = if (isLight) Color(0xFFF6F5FF) else MaterialTheme.colorScheme.surface
    val indigoPrimary = if (isLight) Color(0xFF4F46E5) else Color(0xFF818CF8)

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
                // Header Gradien Indigo–Royal Violet Lembut
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF3730A3),
                                    Color(0xFF4F46E5),
                                    Color(0xFF7C3AED)
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.5.dp, Color(0xFFC7D2FE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                            ShiftHeaderTitleBlock(
                                urgentCount = urgentCount,
                                modifier = Modifier.weight(1f)
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

                // Isi Sheet Bernuansa Pastel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tombol + Tinggalkan Pesan
                    Button(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F766E),
                            contentColor = Color.White
                        ),
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
                            text = if (isEn) "Leave Shift Message" else "Tinggalkan Pesan Shift",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Tab Kategori Tanggal Pill Pastel: Hari Ini vs Akan Datang
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isLight) Color(0xFFEDE9FE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isLight) Color(0xFFC4B5FD) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val tab0Selected = selectedDateTab == 0
                            val tab1Selected = selectedDateTab == 1

                            Surface(
                                onClick = { selectedDateTab = 0 },
                                shape = RoundedCornerShape(10.dp),
                                color = if (tab0Selected) {
                                    if (isLight) Color(0xFF4F46E5) else Color(0xFF818CF8)
                                } else {
                                    Color.Transparent
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("tab_shift_today")
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                    Text(
                                        text = if (isEn) "Today (${todayMessages.size})" else "Hari Ini (${todayMessages.size})",
                                        fontSize = 12.5.sp,
                                        fontWeight = if (tab0Selected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (tab0Selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                onClick = { selectedDateTab = 1 },
                                shape = RoundedCornerShape(10.dp),
                                color = if (tab1Selected) {
                                    if (isLight) Color(0xFF0F766E) else Color(0xFF14B8A6)
                                } else {
                                    Color.Transparent
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("tab_shift_upcoming")
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                    Text(
                                        text = if (isEn) "Upcoming (${upcomingMessages.size})" else "Akan Datang (${upcomingMessages.size})",
                                        fontSize = 12.5.sp,
                                        fontWeight = if (tab1Selected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (tab1Selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Secondary Status Filter Bar Berwarna Pastel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Aktif", "Urgent", "Selesai", "Semua").forEach { filterOpt ->
                            val isSelected = selectedFilter == filterOpt
                            val filterLabel = when (filterOpt) {
                                "Aktif" -> if (isEn) "Active" else "Aktif"
                                "Urgent" -> "Urgent"
                                "Selesai" -> if (isEn) "Completed" else "Selesai"
                                else -> s.nav.all
                            }
                            val countText = when (filterOpt) {
                                "Aktif" -> " ($activeCount)"
                                "Urgent" -> if (urgentCount > 0) " ($urgentCount)" else ""
                                "Selesai" -> " ($completedCount)"
                                else -> " (${categoryMessages.size})"
                            }
                            val (selBg, selTxt, chipBorder) = when (filterOpt) {
                                "Aktif" -> Triple(
                                    if (isLight) Color(0xFFCCFBF1) else Color(0xFF134E4A).copy(alpha = 0.7f),
                                    if (isLight) Color(0xFF0F766E) else Color(0xFF5EEAD4),
                                    if (isLight) Color(0xFF5EEAD4) else Color(0xFF0F766E).copy(alpha = 0.5f)
                                )
                                "Urgent" -> Triple(
                                    if (isLight) Color(0xFFFFE4E6) else Color(0xFF881337).copy(alpha = 0.7f),
                                    if (isLight) Color(0xFFBE123C) else Color(0xFFFDA4AF),
                                    if (isLight) Color(0xFFFDA4AF) else Color(0xFFBE123C).copy(alpha = 0.5f)
                                )
                                "Selesai" -> Triple(
                                    if (isLight) Color(0xFFDCFCE7) else Color(0xFF14532D).copy(alpha = 0.7f),
                                    if (isLight) Color(0xFF15803D) else Color(0xFF86EFAC),
                                    if (isLight) Color(0xFF86EFAC) else Color(0xFF16A34A).copy(alpha = 0.5f)
                                )
                                else -> Triple(
                                    if (isLight) Color(0xFFEDE9FE) else Color(0xFF4C1D95).copy(alpha = 0.7f),
                                    if (isLight) Color(0xFF6D28D9) else Color(0xFFC4B5FD),
                                    if (isLight) Color(0xFFC4B5FD) else Color(0xFF7C3AED).copy(alpha = 0.5f)
                                )
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = filterOpt },
                                label = {
                                    Text(
                                        text = "$filterLabel$countText",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = chipBorder,
                                    selectedBorderColor = selTxt.copy(alpha = 0.65f)
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = if (isLight) Color.White else MaterialTheme.colorScheme.surface,
                                    labelColor = selTxt,
                                    selectedContainerColor = selBg,
                                    selectedLabelColor = selTxt
                                ),
                                modifier = Modifier.testTag("chip_shift_filter_${filterOpt.lowercase()}")
                            )
                        }
                    }

                    // List Kartu Pesan
                    if (filteredMessages.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isLight) Color(0xFFDDD6FE) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
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
                                        .background(if (isLight) Color(0xFFEDE9FE) else Color(0xFF312E81)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = indigoPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                val activeFilterDisplay = when (selectedFilter) {
                                    "Aktif" -> if (isEn) "Active" else "Aktif"
                                    "Urgent" -> "Urgent"
                                    "Selesai" -> if (isEn) "Completed" else "Selesai"
                                    else -> s.nav.all
                                }
                                Text(
                                    text = when {
                                        selectedDateTab == 0 && selectedFilter == "Aktif" ->
                                            if (isEn) "No active shift handover messages for today." else "Tidak ada pesan operan shift aktif untuk hari ini."
                                        selectedDateTab == 1 && selectedFilter == "Aktif" ->
                                            if (isEn) "No upcoming shift messages (tomorrow & later)." else "Belum ada pesan shift yang akan datang (besok & seterusnya)."
                                        selectedDateTab == 0 ->
                                            if (isEn) "No messages today under filter $activeFilterDisplay." else "Tidak ada pesan hari ini pada filter $selectedFilter."
                                        else ->
                                            if (isEn) "No upcoming messages under filter $activeFilterDisplay." else "Tidak ada pesan yang akan datang pada filter $selectedFilter."
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (isEn) {
                                        "Tap '+ Leave Shift Message' to write a shift handover note for its scheduled date."
                                    } else {
                                        "Tekan tombol '+ Tinggalkan Pesan Shift' untuk menulis pesan operan jaga sesuai tanggal tugasnya."
                                    },
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
                                            val status = if (isEn) {
                                                if (!msg.isCompleted) "marked as completed" else "reopened"
                                            } else {
                                                if (!msg.isCompleted) "ditandai selesai" else "dibuka kembali"
                                            }
                                            Toast.makeText(
                                                context,
                                                if (isEn) "Shift message $status." else "Pesan shift $status.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onDelete = {
                                        viewModel.deleteShiftMessage(msg) {
                                            Toast.makeText(
                                                context,
                                                if (isEn) "Shift message deleted." else "Pesan shift dihapus.",
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
                                if (isLight) Color(0xFFC4B5FD) else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("btn_close_shift_messages")
                        ) {
                            Text(
                                text = s.nav.close,
                                fontWeight = FontWeight.SemiBold,
                                color = indigoPrimary
                            )
                        }
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
        val reminderText = remember(msg.id, schoolProfile, s.language) {
            buildShiftReminderWhatsAppText(msg, schoolProfile, isEn)
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
            dragHandle = null,
            containerColor = if (isLight) Color(0xFFF2FBF7) else MaterialTheme.colorScheme.surface
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
                        .widthIn(max = 560.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF064E3B),
                                        Color(0xFF047857),
                                        Color(0xFF0D9488)
                                    )
                                )
                            )
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 12.dp)
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
                                        .background(Color.White),
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
                                        text = if (isEn) "Remind via WhatsApp" else "Ingatkan via WhatsApp",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${if (isEn) "To" else "Kepada"}: ${msg.targetWali} • ${msg.shift}",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFD1FAE5),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(
                                onClick = { messageToShare = null },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = s.nav.close, tint = Color.White)
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                            .padding(bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isLight) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.dp,
                                if (isLight) Color(0xFFA7F3D0) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
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
                                        Toast.makeText(
                                            context,
                                            if (isEn) "Unable to open WhatsApp." else "Tidak dapat membuka WhatsApp.",
                                            Toast.LENGTH_SHORT
                                        ).show()
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
                                    text = if (isEn) "Send WA Directly to ${matchedWali.name}" else "Chat Langsung WA ke ${matchedWali.name}",
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
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText(
                                            if (isEn) "Shift Reminder" else "Pengingat Shift",
                                            reminderText
                                        )
                                    )
                                    Toast.makeText(
                                        context,
                                        if (isEn) "Reminder text copied!" else "Teks pengingat disalin!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_copy_shift_wa_${msg.id}")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEn) "Copy Text" else "Salin Teks", fontSize = 12.sp)
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
                                            Intent.createChooser(
                                                sendIntent,
                                                if (isEn) "Share Shift Reminder to WhatsApp" else "Bagikan Pengingat Shift ke WhatsApp"
                                            )
                                        )
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            if (isEn) "Failed to open share menu." else "Gagal membuka menu berbagi.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (waPhone == null) Color(0xFF059669) else Color(0xFF4F46E5)
                                ),
                                modifier = Modifier
                                    .weight(1.35f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_share_group_shift_${msg.id}")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "Share to WA Group" else "Bagikan ke Grup WA",
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
    schoolProfile: SchoolProfile,
    isEn: Boolean = false
): String {
    val priorityBadge = when (msg.priority) {
        "URGENT" -> "🔴 *URGENT*"
        "SEDANG" -> if (isEn) "🟠 *MEDIUM*" else "🟠 *SEDANG*"
        else -> if (isEn) "🟢 *NORMAL*" else "🟢 *BIASA*"
    }
    val schoolLabel = schoolProfile.schoolName.trim().ifBlank {
        schoolProfile.shortName.trim().ifBlank { "Wali Asuh Care" }
    }
    val statusLabel = if (isEn) {
        if (msg.isCompleted) "✅ Completed" else "⏳ Pending (Please Follow Up)"
    } else {
        if (msg.isCompleted) "✅ Sudah Selesai" else "⏳ Belum Selesai (Mohon Tindak Lanjut)"
    }
    return buildString {
        appendLine(
            if (isEn) "🔔 *WALI ASUH SHIFT HANDOVER REMINDER*"
            else "🔔 *PENGINGAT OPERAN SHIFT WALI ASUH*"
        )
        appendLine("*$schoolLabel*")
        appendLine("----------------------------------------")
        appendLine("$priorityBadge • ${msg.shift}")
        appendLine("${if (isEn) "📌 *Task:*" else "📌 *Tugas:*"} ${msg.title}")
        appendLine("${if (isEn) "👤 *To:*" else "👤 *Kepada:*"} ${msg.targetWali}")
        appendLine("${if (isEn) "📅 *Follow-Up Date:*" else "📅 *Tindak Lanjut:*"} ${DateUtils.formatFullDate(msg.followUpDate)}")
        appendLine(if (isEn) "📝 *Message:*" else "📝 *Isi Pesan:*")
        appendLine(msg.messageContent)
        appendLine("----------------------------------------")
        appendLine("${if (isEn) "Status" else "Status"}: $statusLabel")
        append("${if (isEn) "Created by" else "Dibuat oleh"}: ${msg.senderName}")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShiftHeaderTitleBlock(
    urgentCount: Int,
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isEn) "Next Shift Messages" else "Pesan Shift Berikutnya",
                fontSize = 16.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            if (urgentCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEE2E2))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$urgentCount URGENT",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB91C1C),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
        Text(
            text = if (isEn) {
                "Duty handover & operational notes between Wali Asuh"
            } else {
                "Serah terima tugas jaga & operan antar wali asuh"
            },
            fontSize = 11.5.sp,
            color = Color(0xFFE0E7FF)
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
        isUrgent -> if (isLight) Color(0xFFFFE4E6) else Color(0xFF881337).copy(alpha = 0.5f)
        isSedang -> if (isLight) Color(0xFFFEF3C7) else Color(0xFF78350F).copy(alpha = 0.5f)
        else -> if (isLight) Color(0xFFDCFCE7) else Color(0xFF14532D).copy(alpha = 0.5f)
    }

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
            if (isLight) Color(0xFFFFFCF2) else Color(0xFF282012),
            if (isLight) Color(0xFFFDE68A) else Color(0xFFD97706).copy(alpha = 0.5f),
            if (isLight) Color(0xFFFEF7DC) else Color(0xFF342A18)
        )
        else -> Triple(
            if (isLight) Color(0xFFF5F7FF) else Color(0xFF191D30),
            if (isLight) Color(0xFFC7D2FE) else Color(0xFF4F46E5).copy(alpha = 0.45f),
            if (isLight) Color(0xFFEEF2FF) else Color(0xFF222840)
        )
    }

    val (shiftBg, shiftFg) = when {
        message.shift.contains("Pagi", ignoreCase = true) ->
            (if (isLight) Color(0xFFFEF3C7) else Color(0xFF78350F).copy(alpha = 0.55f)) to
                (if (isLight) Color(0xFFB45309) else Color(0xFFFDE68A))
        message.shift.contains("Siang", ignoreCase = true) || message.shift.contains("Sore", ignoreCase = true) ->
            (if (isLight) Color(0xFFE0F2FE) else Color(0xFF0C4A6E).copy(alpha = 0.55f)) to
                (if (isLight) Color(0xFF0369A1) else Color(0xFF7DD3FC))
        message.shift.contains("Malam", ignoreCase = true) ->
            (if (isLight) Color(0xFFEDE9FE) else Color(0xFF4C1D95).copy(alpha = 0.55f)) to
                (if (isLight) Color(0xFF6D28D9) else Color(0xFFC4B5FD))
        else ->
            (if (isLight) Color(0xFFCCFBF1) else Color(0xFF134E4A).copy(alpha = 0.55f)) to
                (if (isLight) Color(0xFF0F766E) else Color(0xFF5EEAD4))
    }

    val recipientTint = if (isLight) Color(0xFF4F46E5) else Color(0xFFA5B4FC)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_shift_message_${message.id}"),
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
            // Header Baris 1: Badges Prioritas & Shift + Share/Delete Button (48dp target)
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

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(shiftBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = message.shift,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = shiftFg,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onShareWhatsApp,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_share_shift_message_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = if (isEn) "Remind via WhatsApp" else "Ingatkan via WhatsApp",
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
                            contentDescription = if (isEn) "Delete Message" else "Hapus Pesan",
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
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            // Info Penerima & Tanggal Tindak Lanjut
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
                        text = "${if (isEn) "Follow-Up" else "Tindak Lanjut"}: ${DateUtils.formatFullDate(message.followUpDate)}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Konten Isi Pesan
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

            // Footer: Pembuat Pesan & Checklist Selesai
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
                        .testTag("btn_toggle_shift_complete_${message.id}")
                ) {
                    Icon(
                        imageVector = if (message.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (message.isCompleted) {
                            if (isEn) "Completed" else "Selesai"
                        } else {
                            if (isEn) "Mark Completed" else "Tandai Selesai"
                        },
                        tint = if (message.isCompleted) Color(0xFF16A34A) else recipientTint,
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
                        fontWeight = if (message.isCompleted) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (message.isCompleted) Color(0xFF16A34A) else recipientTint,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
