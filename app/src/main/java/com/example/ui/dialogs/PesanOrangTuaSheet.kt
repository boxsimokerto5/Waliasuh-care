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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.ParentMessage
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
    onSendMessage: (ChildStudent, String) -> Unit,
    onMarkRead: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    var selectedStudent by remember(initialSelectedStudent) { mutableStateOf(initialSelectedStudent) }
    var searchQuery by remember { mutableStateOf("") }
    var replyText by remember { mutableStateOf("") }

    val hasClaimed = remember(students, isStudentClaimedByCurrentWali) {
        students.any { isStudentClaimedByCurrentWali(it) }
    }
    var selectedScope by remember(hasClaimed, isAdmin) {
        mutableStateOf(if (hasClaimed && !isAdmin) 0 else 1)
    }

    val messagesByStudentId = remember(parentMessages) {
        parentMessages.groupBy { it.studentId }
    }

    val filteredStudents = remember(students, searchQuery, selectedScope, isStudentClaimedByCurrentWali, messagesByStudentId) {
        val q = searchQuery.trim().lowercase()
        students.filter { st ->
            val scopeMatch = if (selectedScope == 0) isStudentClaimedByCurrentWali(st) else true
            val queryMatch = q.isBlank() ||
                st.name.lowercase().contains(q) ||
                st.motherName.lowercase().contains(q) ||
                st.classGrade.lowercase().contains(q)
            scopeMatch && queryMatch
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
                    // Conversation List
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
                                    text = if (isEn) "Select a student to open chat with parents" else "Pilih siswa untuk membuka percakapan dengan orang tua",
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

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (isEn) "Search student or parent..." else "Cari siswa atau orang tua...", fontSize = 12.5.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_pesan_ortu")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val mineCount = students.count { isStudentClaimedByCurrentWali(it) }
                        FilterChip(
                            selected = selectedScope == 0,
                            onClick = { selectedScope = 0 },
                            label = { Text(if (isEn) "My Foster ($mineCount)" else "Anak Asuh Saya ($mineCount)", fontSize = 11.5.sp) }
                        )
                        FilterChip(
                            selected = selectedScope == 1,
                            onClick = { selectedScope = 1 },
                            label = { Text(if (isEn) "All Students (${students.size})" else "Semua Siswa (${students.size})", fontSize = 11.5.sp) }
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 200.dp, max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (filteredStudents.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isEn) "No student conversations found." else "Belum ada daftar siswa.",
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
                                            Text(student.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(
                                                text = lastMsg?.let { "${it.senderName}: ${it.messageContent}" }
                                                    ?: if (isEn) "Tap to send a message to parents" else "Ketuk untuk kirim pesan ke orang tua",
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

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 200.dp, max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (threadMessages.isEmpty()) {
                            item {
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
                            }
                        } else {
                            items(threadMessages, key = { "pmsg_${it.id}" }) { msg ->
                                val isWaliMsg = msg.senderRole.equals("WALI", ignoreCase = true)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isWaliMsg) Arrangement.End else Arrangement.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (isWaliMsg) 14.dp else 4.dp,
                                            bottomEnd = if (isWaliMsg) 4.dp else 14.dp
                                        ),
                                        color = if (isWaliMsg) Color(0xFF4F46E5) else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.widthIn(max = 280.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = msg.senderName.ifBlank { if (isWaliMsg) "Wali Asuh" else "Orang Tua" },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isWaliMsg) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = msg.messageContent,
                                                fontSize = 13.sp,
                                                color = if (isWaliMsg) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = DateUtils.formatShortDate(msg.createdAt, s.language),
                                                fontSize = 9.5.sp,
                                                color = if (isWaliMsg) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

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
