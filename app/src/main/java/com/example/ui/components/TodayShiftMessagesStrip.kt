package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShiftMessage
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings

private data class SoftMessagePalette(
    val gradientStart: Color,
    val gradientEnd: Color,
    val borderColor: Color,
    val accentColor: Color,
    val badgeBg: Color
)

private val softPastelMessagePalettes = listOf(
    SoftMessagePalette(
        gradientStart = Color(0xFFECFDF5),
        gradientEnd = Color(0xFFD1FAE5),
        borderColor = Color(0xFFA7F3D0),
        accentColor = Color(0xFF047857),
        badgeBg = Color(0xFFDCFCE7)
    ),
    SoftMessagePalette(
        gradientStart = Color(0xFFEFF6FF),
        gradientEnd = Color(0xFFDBEAFE),
        borderColor = Color(0xFFBFDBFE),
        accentColor = Color(0xFF1D4ED8),
        badgeBg = Color(0xFFE0F2FE)
    ),
    SoftMessagePalette(
        gradientStart = Color(0xFFFFF7ED),
        gradientEnd = Color(0xFFFFEDD5),
        borderColor = Color(0xFFFED7AA),
        accentColor = Color(0xFFC2410C),
        badgeBg = Color(0xFFFFEDD5)
    ),
    SoftMessagePalette(
        gradientStart = Color(0xFFF5F3FF),
        gradientEnd = Color(0xFFEDE9FE),
        borderColor = Color(0xFFDDD6FE),
        accentColor = Color(0xFF6D28D9),
        badgeBg = Color(0xFFEDE9FE)
    ),
    SoftMessagePalette(
        gradientStart = Color(0xFFFFF1F2),
        gradientEnd = Color(0xFFFFE4E6),
        borderColor = Color(0xFFFECDD3),
        accentColor = Color(0xFFBE123C),
        badgeBg = Color(0xFFFFE4E6)
    )
)

@Composable
fun TodayShiftMessagesStrip(
    messages: List<ShiftMessage>,
    onMessageClick: (ShiftMessage) -> Unit,
    onOpenAllMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (messages.isEmpty()) return

    val isEn = LocalAppStrings.current.isEnglish
    val listState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val urgentCount = messages.count { it.priority == "URGENT" && !it.isCompleted }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("today_shift_messages_strip"),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = WaliPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(
                    text = if (isEn) "Today's Messages" else "Pesan Hari Ini",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (urgentCount > 0) Color(0xFFFEE2E2) else Color(0xFFE2E8F0))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (urgentCount > 0) {
                            "${messages.size} • $urgentCount Urgent"
                        } else {
                            "${messages.size}"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (urgentCount > 0) Color(0xFFB91C1C) else Color(0xFF334155),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenAllMessages)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
                    .testTag("btn_see_all_today_messages")
            ) {
                Text(
                    text = if (isEn) "View All" else "Semua",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WaliPrimary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (isEn) "View all shift messages" else "Lihat semua pesan shift",
                    tint = WaliPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cardWidth = if (messages.size == 1) {
                maxWidth
            } else {
                (maxWidth * 0.85f).coerceIn(248.dp, 360.dp)
            }

            LazyRow(
                state = listState,
                flingBehavior = snapBehavior,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_shift_messages_carousel"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = if (messages.size > 1) 4.dp else 0.dp)
            ) {
                itemsIndexed(
                    items = messages,
                    key = { _, msg -> "today_msg_${msg.id}" }
                ) { index, msg ->
                    val palette = softPastelMessagePalettes[index % softPastelMessagePalettes.size]
                    SlimTodayShiftMessageCard(
                        message = msg,
                        palette = palette,
                        onClick = { onMessageClick(msg) },
                        modifier = Modifier.width(cardWidth)
                    )
                }
            }
        }
    }
}

@Composable
private fun SlimTodayShiftMessageCard(
    message: ShiftMessage,
    palette: SoftMessagePalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    val isUrgentActive = message.priority == "URGENT" && !message.isCompleted
    val cardBorderColor = if (isUrgentActive) Color(0xFFFCA5A5) else palette.borderColor
    val titleColor = Color(0xFF0F172A)
    val bodyColor = Color(0xFF334155)

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("card_today_shift_msg_${message.id}"),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (isUrgentActive) 1.3.dp else 1.dp, cardBorderColor),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(palette.gradientStart, palette.gradientEnd)
                    )
                )
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isUrgentActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFEE2E2))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "URGENT",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFB91C1C),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        } else if (message.priority == "SEDANG" && !message.isCompleted) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isEn) "MEDIUM" else "SEDANG",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.72f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = message.shift,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.accentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    if (message.isCompleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = if (isEn) "Done" else "Selesai",
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = if (isEn) "Done" else "Selesai",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    } else {
                        Text(
                            text = if (isEn) "Read ›" else "Detail ›",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.accentColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Text(
                    text = message.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (message.isCompleted) titleColor.copy(alpha = 0.72f) else titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = message.messageContent,
                    fontSize = 11.5.sp,
                    color = if (message.isCompleted) bodyColor.copy(alpha = 0.75f) else bodyColor,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${message.senderName} → ${message.targetWali}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.accentColor.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = DateUtils.formatTime(message.createdAt),
                        fontSize = 10.sp,
                        color = bodyColor.copy(alpha = 0.75f),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShiftMessageDetailSheet(
    message: ShiftMessage,
    onToggleComplete: () -> Unit,
    onOpenAllMessages: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("shift_message_detail_sheet")
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 26.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = WaliPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isEn) "Shift Message Detail" else "Detail Pesan Hari Ini",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = DateUtils.formatFullDate(message.followUpDate, s.language),
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_shift_detail")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close detail" else "Tutup detail"
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(priorityBg)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when {
                                isUrgent -> "URGENT"
                                isSedang -> if (isEn) "MEDIUM" else "SEDANG"
                                else -> if (isEn) "NORMAL" else "BIASA"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFFE0F2FE))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = message.shift,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WaliPrimary
                        )
                    }

                    if (message.isCompleted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isEn) {
                                    "Done (${message.completedBy.ifBlank { "Wali" }})"
                                } else {
                                    "Selesai (${message.completedBy.ifBlank { "Wali" }})"
                                },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }

                Text(
                    text = message.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = WaliPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (isEn) {
                                    "From ${message.senderName} → To ${message.targetWali}"
                                } else {
                                    "Dari ${message.senderName} → Kepada ${message.targetWali}"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${if (isEn) "Schedule" else "Jadwal"}: ${DateUtils.formatFullDate(message.followUpDate, s.language)}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${if (isEn) "Sent at" else "Dikirim pukul"}: ${DateUtils.formatTime(message.createdAt)} WIB",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8FAFC)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SelectionContainer {
                        Text(
                            text = message.messageContent,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF0F172A),
                            modifier = Modifier
                                .padding(14.dp)
                                .testTag("shift_message_detail_content")
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = "${message.title}\n(${message.senderName} → ${message.targetWali})\n\n${message.messageContent}"
                            clipboard.setPrimaryClip(ClipData.newPlainText("ShiftMessage", clipText))
                            Toast.makeText(
                                context,
                                if (isEn) "Message copied" else "Isi pesan berhasil disalin",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.9f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_copy_shift_detail")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEn) "Copy" else "Salin", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onToggleComplete,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (message.isCompleted) Color(0xFF64748B) else Color(0xFF16A34A)
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_toggle_complete_shift_detail")
                    ) {
                        Icon(
                            imageVector = if (message.isCompleted) Icons.Default.RadioButtonUnchecked else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (message.isCompleted) {
                                if (isEn) "Reopen" else "Buka Lagi"
                            } else {
                                if (isEn) "Mark Done" else "Tandai Selesai"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenAllMessages()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("btn_open_all_from_detail")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEn) "Open Full Shift Messages" else "Buka Halaman Pesan Shift Lengkap",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
