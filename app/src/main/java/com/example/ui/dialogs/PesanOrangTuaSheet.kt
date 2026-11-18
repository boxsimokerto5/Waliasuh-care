package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

    val handleBackNavigation: () -> Unit = {
        if (selectedStudent != null && initialSelectedStudent == null) {
            selectedStudent = null
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = handleBackNavigation,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val activeStudent = selectedStudent
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("screen_pesan_ortu_full"),
            containerColor = Color(0xFFF6F7FB),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF3730A3),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = handleBackNavigation,
                            modifier = Modifier.testTag("btn_back_pesan_ortu")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (isEn) "Back" else "Kembali",
                                tint = Color.White
                            )
                        }
                    },
                    title = {
                        if (activeStudent == null) {
                            Column {
                                Text(
                                    text = if (isEn) "Parent Messages" else "Pesan Orang Tua",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isEn) {
                                        "Foster students selected in Select Foster Kids"
                                    } else {
                                        "Siswa binaan yang dipilih di Pilih Anak Asuh"
                                    },
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Column {
                                Text(
                                    text = activeStudent.name,
                                    fontSize = 16.sp,
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
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    actions = {
                        if (activeStudent != null) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("btn_close_pesan_ortu")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = if (isEn) "Close" else "Tutup",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
                    .imePadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                if (activeStudent == null) {
                    // Conversation List (Full-Screen Spacious Layout)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 640.dp)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (claimedStudents.isNotEmpty()) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        if (isEn) "Search my foster students or parents..." else "Cari anak asuh saya atau orang tua...",
                                        fontSize = 12.5.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = if (isEn) "Clear" else "Bersihkan",
                                                modifier = Modifier.size(16.dp)
                                            )
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
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 16.dp),
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
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 28.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(52.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE0E7FF)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PersonAdd,
                                                    contentDescription = null,
                                                    tint = Color(0xFF4338CA),
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }
                                            Text(
                                                text = if (isEn) {
                                                    "No Foster Students Selected Yet"
                                                } else {
                                                    "Belum Ada Anak Asuh Dipilih"
                                                },
                                                fontSize = 15.sp,
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
                                            .padding(vertical = 32.dp),
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
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            StudentAvatar(
                                                name = student.name,
                                                gender = student.gender,
                                                size = 42.dp,
                                                photoUrl = student.photoUrl,
                                                photoFallbackUrl = student.photoFallbackUrl
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = student.name,
                                                        fontSize = 14.sp,
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
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            if (unreadCount > 0) {
                                                Spacer(modifier = Modifier.width(6.dp))
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
                    }
                } else {
                    // Thread View (Spacious Full-Height Layout matching ParentPortalScreen)
                    val threadMessages = remember(parentMessages, activeStudent.id) {
                        parentMessages.filter { it.studentId == activeStudent.id }.sortedBy { it.createdAt }
                    }
                    LaunchedEffect(activeStudent.id, threadMessages.size) {
                        if (threadMessages.any { !it.isReadByWali && it.senderRole == "PARENT" }) {
                            onMarkRead(activeStudent.id)
                        }
                    }

                    val threadListState = rememberLazyListState()
                    val parentLabel = activeStudent.motherName.ifBlank {
                        if (isEn) "Parent" else "Orang Tua"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 640.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isEn) {
                                    "Chat with $parentLabel"
                                } else {
                                    "Percakapan dengan $parentLabel"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }

                        ParentChatMessagesList(
                            messages = threadMessages,
                            myRole = "WALI",
                            isEn = isEn,
                            lang = s.language,
                            ownBubbleColor = Color(0xFF4338CA),
                            listState = threadListState,
                            onDeleteMessage = onDeleteMessage,
                            emptyContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isEn) "No messages yet. Start the conversation below." else "Belum ada pesan. Mulai percakapan di bawah.",
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                placeholder = {
                                    Text(
                                        if (isEn) "Write message to parent..." else "Tulis pesan ke orang tua...",
                                        fontSize = 12.5.sp
                                    )
                                },
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
                                    containerColor = Color(0xFF4338CA),
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
}
