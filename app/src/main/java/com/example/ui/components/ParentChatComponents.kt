package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ChatMediaLocalSave
import com.example.data.model.ParentMessage
import com.example.data.model.ParentMessageHelper
import com.example.ui.dialogs.GalleryFullscreenPreviewDialog
import com.example.ui.dialogs.GalleryRemoteOrLocalImage
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
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    localSavesByMessageId: Map<Long, ChatMediaLocalSave> = emptyMap(),
    autoSaveMediaEnabled: Boolean = true,
    isSavingMedia: Boolean = false,
    onSavePhotoToGallery: ((ParentMessage, Boolean) -> Unit)? = null,
    onPurgeServerMedia: ((ParentMessage) -> Unit)? = null
) {
    val groupedItems = remember(messages, lang) {
        buildGroupedChatItems(messages, lang)
    }
    var pendingDeleteMessage by remember { mutableStateOf<ParentMessage?>(null) }
    var previewPhotoMessage by remember { mutableStateOf<ParentMessage?>(null) }
    var attemptedAutoSaveIds by remember { mutableStateOf(emptySet<Long>()) }

    LaunchedEffect(groupedItems.size) {
        if (groupedItems.isNotEmpty()) {
            listState.animateScrollToItem(groupedItems.lastIndex)
        }
    }

    LaunchedEffect(messages, localSavesByMessageId, autoSaveMediaEnabled) {
        if (!autoSaveMediaEnabled || onSavePhotoToGallery == null) return@LaunchedEffect
        val pending = messages.filter { msg ->
            ParentMessageHelper.hasServerMedia(msg) &&
                localSavesByMessageId[msg.id] == null &&
                msg.id !in attemptedAutoSaveIds
        }
        if (pending.isNotEmpty()) {
            attemptedAutoSaveIds = attemptedAutoSaveIds + pending.map { it.id }
            pending.forEach { msg ->
                onSavePhotoToGallery(msg, true)
            }
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
                        val localSave = localSavesByMessageId[msg.id]
                        val hasServerMedia = ParentMessageHelper.hasServerMedia(msg)
                        val isPhoto = ParentMessageHelper.isPhotoMessage(msg, localSave)
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
                                    .widthIn(min = 72.dp, max = 284.dp)
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
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    if (isPhoto) {
                                        if (localSave != null || hasServerMedia) {
                                            val primaryImg = localSave?.localGalleryUri?.takeIf { it.isNotBlank() } ?: msg.photoUrl
                                            val fallbackImg = msg.photoFallbackUrl.ifBlank { msg.photoUrl }
                                            GalleryRemoteOrLocalImage(
                                                primaryUrl = primaryImg,
                                                fallbackUrl = fallbackImg,
                                                contentDescription = msg.messageContent.ifBlank {
                                                    if (isEn) "Chat photo" else "Foto chat"
                                                },
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(min = 140.dp, max = 220.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable { previewPhotoMessage = msg }
                                                    .testTag("chat_photo_${msg.id}")
                                            )

                                            if (localSave != null) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isOwn) {
                                                        Color.White.copy(alpha = 0.16f)
                                                    } else {
                                                        Color(0xFFDCFCE7)
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("badge_chat_photo_saved_${msg.id}")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            tint = if (isOwn) Color(0xFFA7F3D0) else Color(0xFF047857),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Text(
                                                            text = if (isEn) {
                                                                "Saved in Phone Gallery"
                                                            } else {
                                                                "Tersimpan di Galeri HP ✓"
                                                            },
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = if (isOwn) Color.White else Color(0xFF065F46),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            } else if (hasServerMedia && onSavePhotoToGallery != null) {
                                                OutlinedButton(
                                                    onClick = { onSavePhotoToGallery(msg, false) },
                                                    enabled = !isSavingMedia,
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (isOwn) Color.White.copy(alpha = 0.65f) else Color(0xFF0F766E)
                                                    ),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = if (isOwn) Color.White else Color(0xFF0F766E)
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(min = 48.dp)
                                                        .testTag("btn_save_chat_photo_${msg.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Download,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = if (isEn) "Save to Phone Gallery" else "Simpan ke Galeri HP",
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            if (isOwn && hasServerMedia && onPurgeServerMedia != null) {
                                                TextButton(
                                                    onClick = { onPurgeServerMedia(msg) },
                                                    colors = ButtonDefaults.textButtonColors(
                                                        contentColor = Color.White.copy(alpha = 0.9f)
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier
                                                        .heightIn(min = 48.dp)
                                                        .testTag("btn_purge_chat_photo_${msg.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CleaningServices,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(5.dp))
                                                    Text(
                                                        text = if (isEn) {
                                                            if (msg.isDownloadedByRecipient) "Clean R2 Relay (Downloaded)" else "Clean R2 Relay"
                                                        } else {
                                                            if (msg.isDownloadedByRecipient) "Hemat R2 (Sudah Diunduh)" else "Hemat Server R2"
                                                        },
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isOwn) {
                                                    Color.White.copy(alpha = 0.14f)
                                                } else {
                                                    MaterialTheme.colorScheme.surface
                                                },
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isOwn) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("chat_photo_purged_${msg.id}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CloudOff,
                                                        contentDescription = null,
                                                        tint = if (isOwn) Color.White.copy(alpha = 0.85f) else Color(0xFF64748B),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        text = if (isEn) {
                                                            "Photo relay cleaned from server R2 after delivery."
                                                        } else {
                                                            "Foto telah dibersihkan dari server R2 untuk menghemat ruang."
                                                        },
                                                        fontSize = 11.sp,
                                                        lineHeight = 14.sp,
                                                        color = if (isOwn) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (msg.messageContent.isNotBlank()) {
                                        Text(
                                            text = msg.messageContent,
                                            fontSize = 13.5.sp,
                                            lineHeight = 18.sp,
                                            color = if (isOwn) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
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

    val previewMsg = previewPhotoMessage
    if (previewMsg != null) {
        val localSave = localSavesByMessageId[previewMsg.id]
        GalleryFullscreenPreviewDialog(
            title = previewMsg.senderName.ifBlank { previewMsg.studentName.ifBlank { if (isEn) "Chat Photo" else "Foto Chat" } },
            caption = previewMsg.messageContent,
            primaryUrl = localSave?.localGalleryUri?.takeIf { it.isNotBlank() } ?: previewMsg.photoUrl,
            fallbackUrl = previewMsg.photoFallbackUrl.ifBlank { previewMsg.photoUrl },
            onDismiss = { previewPhotoMessage = null }
        )
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
                            text = ParentMessageHelper.displayPreviewText(targetToDelete, isEn),
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

@Composable
fun ChatAutoSaveToggleStrip(
    autoSaveEnabled: Boolean,
    isSavingMedia: Boolean,
    isEn: Boolean,
    onToggleAutoSave: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF0FDFA),
        border = BorderStroke(0.75.dp, Color(0xFF99F6E4)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                tint = Color(0xFF0F766E),
                modifier = Modifier.size(16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEn) "Auto-save chat photos to Phone Gallery" else "Simpan otomatis foto chat ke Galeri HP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF134E4A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isEn) {
                        "R2 temporary relay -> Pictures/WaliAsuhCare"
                    } else {
                        "Estafet R2 sementara -> Pictures/WaliAsuhCare"
                    },
                    fontSize = 10.sp,
                    color = Color(0xFF0F766E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isSavingMedia) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF0F766E)
                )
            }
            Switch(
                checked = autoSaveEnabled,
                onCheckedChange = onToggleAutoSave,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF0F766E)
                ),
                modifier = Modifier.testTag("switch_chat_auto_save_media")
            )
        }
    }
}

@Composable
fun ChatPhotoComposerBar(
    draftText: String,
    onDraftChange: (String) -> Unit,
    placeholderText: String,
    isEn: Boolean,
    isSendingPhoto: Boolean,
    supportsPhotoUpload: Boolean,
    onSendText: (String) -> Unit,
    onSendPhoto: ((Uri, String) -> Unit)?,
    inputTestTag: String,
    sendButtonTestTag: String,
    attachButtonTestTag: String,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF4338CA)
) {
    val context = LocalContext.current
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingPhotoUri = uri
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val currentPhoto = pendingPhotoUri
        if (currentPhoto != null) {
            Surface(
                color = Color(0xFFF0FDFA),
                border = BorderStroke(1.dp, Color(0xFF99F6E4)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("chat_pending_photo_preview")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentPhoto)
                            .crossfade(true)
                            .build(),
                        contentDescription = if (isEn) "Selected photo preview" else "Pratinjau foto terpilih",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Photo ready to send" else "Foto siap dikirim",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF134E4A)
                        )
                        Text(
                            text = if (isEn) {
                                "Compressed & relayed via R2 -> Phone Gallery"
                            } else {
                                "Dikompresi & estafet R2 -> Galeri HP penerima"
                            },
                            fontSize = 10.5.sp,
                            color = Color(0xFF0F766E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { pendingPhotoUri = null },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_clear_chat_photo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Remove photo" else "Hapus foto pilihan",
                            tint = Color(0xFF134E4A)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (supportsPhotoUpload && onSendPhoto != null) {
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isSendingPhoto,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFFCCFBF1),
                        contentColor = Color(0xFF0F766E)
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(attachButtonTestTag)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = if (isEn) "Send photo" else "Kirim foto"
                    )
                }
            }

            OutlinedTextField(
                value = draftText,
                onValueChange = onDraftChange,
                placeholder = {
                    Text(
                        text = if (pendingPhotoUri != null) {
                            if (isEn) "Optional photo caption..." else "Keterangan foto (opsional)..."
                        } else {
                            placeholderText
                        },
                        fontSize = 12.5.sp
                    )
                },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag(inputTestTag)
            )

            val canSend = !isSendingPhoto && (draftText.isNotBlank() || pendingPhotoUri != null)
            IconButton(
                onClick = {
                    val photoUri = pendingPhotoUri
                    val cleanText = draftText.trim()
                    if (photoUri != null && onSendPhoto != null) {
                        onSendPhoto(photoUri, cleanText)
                        pendingPhotoUri = null
                        onDraftChange("")
                    } else if (cleanText.isNotBlank()) {
                        onSendText(cleanText)
                        onDraftChange("")
                    }
                },
                enabled = canSend,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = accentColor,
                    contentColor = Color.White,
                    disabledContainerColor = accentColor.copy(alpha = 0.35f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag(sendButtonTestTag)
            ) {
                if (isSendingPhoto) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = if (isEn) "Send" else "Kirim"
                    )
                }
            }
        }
    }
}
