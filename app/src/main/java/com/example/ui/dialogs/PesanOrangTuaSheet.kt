package com.example.ui.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.ParentMessage
import com.example.ui.components.ParentChatMessagesList
import com.example.ui.components.StudentAvatar
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesanOrangTuaSheet(
    students: List<ChildStudent>,
    parentMessages: List<ParentMessage>,
    isStudentClaimedByCurrentWali: (ChildStudent) -> Boolean,
    isAdmin: Boolean,
    initialSelectedStudent: ChildStudent? = null,
    onOpenPilihAnakAsuh: (() -> Unit)? = null,
    onSendMessage: (ChildStudent, String) -> Unit,
    onMarkRead: (Long) -> Unit,
    onDeleteMessage: (ParentMessage) -> Unit = {},
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    var selectedStudent by remember(initialSelectedStudent) { mutableStateOf(initialSelectedStudent) }
    var searchQuery by remember { mutableStateOf("") }
    var replyText by remember { mutableStateOf("") }

    val claimedStudents = students.filter { isStudentClaimedByCurrentWali(it) }

    val messagesByStudentId = remember(parentMessages) {
        parentMessages.groupBy { it.studentId }
    }

    val filteredStudents = remember(claimedStudents, searchQuery, messagesByStudentId) {
        val q = searchQuery.trim().lowercase()
        claimedStudents.filter { st ->
            q.isBlank() ||
                st.name.lowercase().contains(q) ||
                st.motherName.lowercase().contains(q) ||
                st.classGrade.lowercase().contains(q)
        }.sortedWith(
            compareByDescending<ChildStudent> { st ->
                messagesByStudentId[st.id].orEmpty().count { !it.isReadByWali && it.senderRole == "PARENT" }
            }.thenByDescending { st ->
                messagesByStudentId[st.id].orEmpty().maxOfOrNull { it.createdAt } ?: 0L
            }.thenBy { it.name }
        )
    }

    if (selectedStudent != null && initialSelectedStudent == null) {
        BackHandler {
            selectedStudent = null
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
                    .widthIn(max = 600.dp)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val activeStudent = selectedStudent
                if (activeStudent == null) {
                    // Conversation List (Hanya Anak Asuh Terpilih)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isEn) "Parent Messages" else "Pesan Orang Tua",
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isEn) {
                                        "Showing foster students selected in Select Foster Kids"
                                    } else {
                                        "Khusus siswa yang dipilih di menu Pilih Anak Asuh"
                                    },
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                        }
                    }

                    if (claimedStudents.isNotEmpty()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(if (isEn) "Search my foster students or parents..." else "Cari anak asuh saya atau orang tua...", fontSize = 12.5.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = if (isEn) "Clear" else "Bersihkan", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_search_pesan_ortu")
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEEF2FF),
                            border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                        ) {
                            Text(
                                text = if (isEn) {
                                    "My Selected Foster Kids: ${claimedStudents.size}"
                                } else {
                                    "Anak Asuh Terpilih: ${claimedStudents.size} siswa"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4338CA),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 200.dp, max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (claimedStudents.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .testTag("empty_pesan_ortu_no_claimed"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 22.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE0E7FF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PersonAdd,
                                                contentDescription = null,
                                                tint = Color(0xFF4338CA),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Text(
                                            text = if (isEn) {
                                                "No Foster Students Selected Yet"
                                            } else {
                                                "Belum Ada Anak Asuh Dipilih"
                                            },
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isEn) {
                                                "Only students claimed in Select Foster Kids appear here so you can chat directly with their parents."
                                            } else {
                                                "Hanya siswa yang sudah dipilih di menu Pilih Anak Asuh yang tampil di sini agar percakapan lebih fokus."
                                            },
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        if (onOpenPilihAnakAsuh != null) {
                                            Button(
                                                onClick = onOpenPilihAnakAsuh,
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("btn_open_pilih_anak_asuh_from_chat")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PersonAdd,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isEn) "Select Foster Students" else "Pilih Anak Asuh Sekarang",
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (filteredStudents.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isEn) "No matching foster students found." else "Tidak ada anak asuh yang cocok dengan pencarian.",
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(filteredStudents, key = { "chat_st_${it.id}" }) { student ->
                                val thread = messagesByStudentId[student.id].orEmpty().sortedBy { it.createdAt }
                                val unreadCount = thread.count { !it.isReadByWali && it.senderRole == "PARENT" }
                                val lastMsg = thread.lastOrNull()
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedStudent = student }
                                        .testTag("card_chat_student_${student.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (unreadCount > 0) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        width = if (unreadCount > 0) 1.3.dp else 1.dp,
                                        color = if (unreadCount > 0) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StudentAvatar(name = student.name, gender = student.gender, size = 40.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = student.name,
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (lastMsg != null) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = DateUtils.formatTime(lastMsg.createdAt, s.language),
                                                        fontSize = 10.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Text(
                                                text = lastMsg?.let {
                                                    val prefix = if (it.senderRole.equals("WALI", ignoreCase = true)) {
                                                        if (isEn) "You: " else "Anda: "
                                                    } else {
                                                        ""
                                                    }
                                                    "$prefix${it.messageContent}"
                                                } ?: if (isEn) "Tap to send a message to parents" else "Ketuk untuk kirim pesan ke orang tua",
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (unreadCount > 0) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFDC2626)
                                            ) {
                                                Text(
                                                    text = "$unreadCount",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Thread View
                    val threadMessages = remember(parentMessages, activeStudent.id) {
                        parentMessages.filter { it.studentId == activeStudent.id }.sortedBy { it.createdAt }
                    }
                    LaunchedEffect(activeStudent.id, threadMessages.size) {
                        if (threadMessages.any { !it.isReadByWali && it.senderRole == "PARENT" }) {
                            onMarkRead(activeStudent.id)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = {
                                    if (initialSelectedStudent == null) {
                                        selectedStudent = null
                                    } else {
                                        onDismiss()
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (isEn) "Back" else "Kembali")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeStudent.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isEn) {
                                        "Parent: ${activeStudent.motherName.ifBlank { "-" }}"
                                    } else {
                                        "Orang Tua: ${activeStudent.motherName.ifBlank { "-" }}"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                        }
                    }

                    val threadListState = rememberLazyListState()
                    ParentChatMessagesList(
                        messages = threadMessages,
                        myRole = "WALI",
                        isEn = isEn,
                        lang = s.language,
                        ownBubbleColor = Color(0xFF4F46E5),
                        listState = threadListState,
                        onDeleteMessage = onDeleteMessage,
                        emptyContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isEn) "No messages yet. Start the conversation below." else "Belum ada pesan. Mulai percakapan di bawah.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 200.dp, max = 380.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text(if (isEn) "Write message to parent..." else "Tulis pesan ke orang tua...", fontSize = 12.5.sp) },
                            maxLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_parent_chat_reply")
                        )
                        IconButton(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    onSendMessage(activeStudent, replyText.trim())
                                    replyText = ""
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF4F46E5),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_send_parent_message")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = if (isEn) "Send" else "Kirim"
                            )
                        }
                    }
                }
            }
        }
    }
}
