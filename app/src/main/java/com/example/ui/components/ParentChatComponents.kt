package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.model.ParentMessage
import com.example.util.DateUtils
import com.example.util.i18n.AppLanguage

sealed interface ParentChatListItem {
    data class DateHeader(val dayKey: String, val label: String) : ParentChatListItem
    data class MessageItem(val message: ParentMessage) : ParentChatListItem
}

fun buildGroupedChatItems(
    messages: List<ParentMessage>,
    lang: AppLanguage
): List<ParentChatListItem> {
    if (messages.isEmpty()) return emptyList()
    val sorted = messages.sortedBy { it.createdAt }
    val result = mutableListOf<ParentChatListItem>()
    var lastDayKey: String? = null
    for (msg in sorted) {
        val dayKey = DateUtils.formatChatDayKey(msg.createdAt)
        if (dayKey != lastDayKey) {
            result.add(
                ParentChatListItem.DateHeader(
                    dayKey = dayKey,
                    label = DateUtils.formatChatDateChip(msg.createdAt, lang)
                )
            )
            lastDayKey = dayKey
        }
        result.add(ParentChatListItem.MessageItem(msg))
    }
    return result
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ParentChatMessagesList(
    messages: List<ParentMessage>,
    myRole: String,
    isEn: Boolean,
    lang: AppLanguage,
    emptyContent: @Composable () -> Unit,
    onDeleteMessage: (ParentMessage) -> Unit,
    modifier: Modifier = Modifier,
    ownBubbleColor: Color = Color(0xFF4338CA),
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
) {
    val groupedItems = remember(messages, lang) {
        buildGroupedChatItems(messages, lang)
    }
    var pendingDeleteMessage by remember { mutableStateOf<ParentMessage?>(null) }

    LaunchedEffect(groupedItems.size) {
        if (groupedItems.isNotEmpty()) {
            listState.animateScrollToItem(groupedItems.lastIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (groupedItems.isEmpty()) {
            item(key = "chat_empty") {
                emptyContent()
            }
        } else {
            items(
                items = groupedItems,
                key = { item ->
                    when (item) {
                        is ParentChatListItem.DateHeader -> "chat_hdr_${item.dayKey}"
                        is ParentChatListItem.MessageItem -> "chat_msg_${item.message.id}"
                    }
                }
            ) { item ->
                when (item) {
                    is ParentChatListItem.DateHeader -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                                border = BorderStroke(
                                    width = 0.75.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("chat_date_divider_${item.dayKey}")
                            ) {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    is ParentChatListItem.MessageItem -> {
                        val msg = item.message
                        val isOwn = msg.senderRole.equals(myRole, ignoreCase = true)
                        val bubbleShape = RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isOwn) 14.dp else 4.dp,
                            bottomEnd = if (isOwn) 4.dp else 14.dp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isOwn) {
                                IconButton(
                                    onClick = { pendingDeleteMessage = msg },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .size(48.dp)
                                        .testTag("btn_delete_chat_msg_${msg.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = if (isEn) "Delete or cancel message" else "Hapus atau batalkan pesan",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.72f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(2.dp))
                            }

                            Surface(
                                shape = bubbleShape,
                                color = if (isOwn) ownBubbleColor else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .widthIn(min = 72.dp, max = 276.dp)
                                    .clip(bubbleShape)
                                    .then(
                                        if (isOwn) {
                                            Modifier.combinedClickable(
                                                onClick = {},
                                                onLongClick = { pendingDeleteMessage = msg }
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .testTag("chat_bubble_${msg.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = msg.messageContent,
                                        fontSize = 13.5.sp,
                                        lineHeight = 18.sp,
                                        color = if (isOwn) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = DateUtils.formatTime(msg.createdAt, lang),
                                        fontSize = 10.sp,
                                        color = if (isOwn) {
                                            Color.White.copy(alpha = 0.78f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val targetToDelete = pendingDeleteMessage
    if (targetToDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteMessage = null },
            modifier = Modifier.testTag("dialog_delete_chat_msg"),
            title = {
                Text(
                    text = if (isEn) "Cancel / Delete Message?" else "Batalkan / Hapus Pesan?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isEn) {
                            "This message will be permanently removed from the conversation."
                        } else {
                            "Pesan ini akan dihapus secara permanen dari percakapan."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = targetToDelete.messageContent,
                            fontSize = 12.5.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMessage(targetToDelete)
                        pendingDeleteMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_delete_chat_msg")
                ) {
                    Text(if (isEn) "Delete Message" else "Hapus Pesan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDeleteMessage = null },
                    modifier = Modifier.testTag("btn_cancel_delete_chat_msg")
                ) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }
}
