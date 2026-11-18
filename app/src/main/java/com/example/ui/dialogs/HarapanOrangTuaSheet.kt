package com.example.ui.dialogs

import android.app.Activity
import android.content.ActivityNotFoundException
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.ChildStudent
import com.example.data.model.ParentExpectation
import com.example.data.model.ParentExpectationHelper
import com.example.ui.components.StudentAvatar
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings

private enum class HarapanFilterMode {
    ALL,
    HAS_HOPE,
    UNREAD
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HarapanOrangTuaSheet(
    students: List<ChildStudent>,
    parentExpectations: List<ParentExpectation>,
    isStudentClaimedByCurrentWali: (ChildStudent) -> Boolean,
    isAdmin: Boolean,
    onOpenPilihAnakAsuh: (() -> Unit)? = null,
    onMarkReadByWali: (Long) -> Unit,
    onSaveFollowUpNote: (ParentExpectation, String) -> Unit,
    onOpenChatWithStudent: (ChildStudent) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language

    val claimedStudents = remember(students) {
        students.filter { isStudentClaimedByCurrentWali(it) }
    }
    var showAllForAdmin by remember(isAdmin, claimedStudents.size) {
        mutableStateOf(isAdmin && claimedStudents.isEmpty())
    }
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf(HarapanFilterMode.ALL) }
    var activeReplyExpectationId by remember { mutableStateOf<Long?>(null) }
    val draftFollowUps = remember { mutableStateMapOf<Long, String>() }

    var activeVoiceExpectationId by remember { mutableStateOf<Long?>(null) }
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val expId = activeVoiceExpectationId
        activeVoiceExpectationId = null
        if (result.resultCode == Activity.RESULT_OK && expId != null) {
            val transcript = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (transcript.isNotBlank()) {
                val prev = draftFollowUps[expId].orEmpty()
                draftFollowUps[expId] = appendVoiceTranscript(prev, transcript)
            }
        }
    }

    fun startVoiceFollowUp(expId: Long) {
        activeVoiceExpectationId = expId
        val intent = buildGoogleSpeechToTextIntent(
            promptText = if (isEn) {
                "Speak caregiver mentoring & advice note..."
            } else {
                "Ucapkan catatan ikhtiar & nasehat Wali Asuh..."
            },
            languageTag = if (isEn) "en-US" else "id-ID"
        )
        try {
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            activeVoiceExpectationId = null
            Toast.makeText(
                context,
                if (isEn) {
                    "Google Voice-to-Text service is not available on this device."
                } else {
                    "Layanan Suara ke Teks Google belum tersedia di perangkat ini."
                },
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            activeVoiceExpectationId = null
            Toast.makeText(
                context,
                if (isEn) {
                    "Unable to launch Google Voice input: ${e.message ?: "Error"}"
                } else {
                    "Gagal membuka Suara ke Teks Google: ${e.message ?: "Error"}"
                },
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val scopedStudents = remember(students, claimedStudents, isAdmin, showAllForAdmin) {
        if (isAdmin && showAllForAdmin) students else claimedStudents
    }

    val expectationsByStudent = remember(scopedStudents, parentExpectations) {
        scopedStudents.associate { student ->
            student.id to parentExpectations
                .filter { ParentExpectationHelper.matchesStudent(it, student) }
                .sortedByDescending { it.updatedAt }
        }
    }

    val filteredStudents = remember(scopedStudents, searchQuery, filterMode, expectationsByStudent) {
        val q = searchQuery.trim().lowercase()
        scopedStudents.filter { st ->
            val matchesQuery = q.isBlank() ||
                st.name.lowercase().contains(q) ||
                st.motherName.lowercase().contains(q) ||
                st.classGrade.lowercase().contains(q) ||
                expectationsByStudent[st.id].orEmpty().any {
                    it.characterExpectation.lowercase().contains(q) ||
                        it.adviceFocusNotes.lowercase().contains(q)
                }
            if (!matchesQuery) return@filter false

            val exps = expectationsByStudent[st.id].orEmpty()
            when (filterMode) {
                HarapanFilterMode.ALL -> true
                HarapanFilterMode.HAS_HOPE -> exps.isNotEmpty()
                HarapanFilterMode.UNREAD -> exps.any { !it.isReadByWali }
            }
        }.sortedWith(
            compareByDescending<ChildStudent> { st ->
                expectationsByStudent[st.id].orEmpty().count { !it.isReadByWali }
            }.thenByDescending { st ->
                expectationsByStudent[st.id].orEmpty().maxOfOrNull { it.updatedAt } ?: 0L
            }.thenBy { it.name }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sheet_harapan_ortu"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD1FAE5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolunteerActivism,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "Parent Character Expectations" else "Harapan Orang Tua",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEn) {
                                    "Shared effort to advise & shape student character"
                                } else {
                                    "Ikhtiar bersama menasehati karakter anak asuh"
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

                if (isAdmin) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !showAllForAdmin,
                            onClick = { showAllForAdmin = false },
                            label = {
                                Text(
                                    if (isEn) "My Foster (${claimedStudents.size})" else "Anak Asuh Saya (${claimedStudents.size})",
                                    fontSize = 11.5.sp
                                )
                            },
                            modifier = Modifier.testTag("chip_harapan_scope_mine")
                        )
                        FilterChip(
                            selected = showAllForAdmin,
                            onClick = { showAllForAdmin = true },
                            label = {
                                Text(
                                    if (isEn) "All Students (${students.size})" else "Semua Siswa (${students.size})",
                                    fontSize = 11.5.sp
                                )
                            },
                            modifier = Modifier.testTag("chip_harapan_scope_all")
                        )
                    }
                }

                if (scopedStudents.isNotEmpty()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                if (isEn) "Search student, parent, or expectation..." else "Cari siswa, orang tua, atau isi harapan...",
                                fontSize = 12.5.sp
                            )
                        },
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
                            .testTag("input_search_harapan_ortu")
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val hasHopeCount = scopedStudents.count { expectationsByStudent[it.id].orEmpty().isNotEmpty() }
                        val unreadCount = scopedStudents.sumOf { st ->
                            expectationsByStudent[st.id].orEmpty().count { !it.isReadByWali }
                        }
                        FilterChip(
                            selected = filterMode == HarapanFilterMode.ALL,
                            onClick = { filterMode = HarapanFilterMode.ALL },
                            label = { Text(if (isEn) "All (${scopedStudents.size})" else "Semua (${scopedStudents.size})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = filterMode == HarapanFilterMode.HAS_HOPE,
                            onClick = { filterMode = HarapanFilterMode.HAS_HOPE },
                            label = { Text(if (isEn) "Submitted ($hasHopeCount)" else "Ada Harapan ($hasHopeCount)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = filterMode == HarapanFilterMode.UNREAD,
                            onClick = { filterMode = HarapanFilterMode.UNREAD },
                            label = { Text(if (isEn) "Unread ($unreadCount)" else "Belum Dibaca ($unreadCount)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEE2E2),
                                selectedLabelColor = Color(0xFFB91C1C)
                            )
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 200.dp, max = 460.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (scopedStudents.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .testTag("empty_harapan_ortu_no_claimed"),
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
                                            .background(Color(0xFFD1FAE5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = Color(0xFF047857),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Text(
                                        text = if (isEn) {
                                            "No Foster Kids Claimed Yet"
                                        } else {
                                            "Belum Ada Anak Asuh Terpilih"
                                        },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Claim your foster students first so parent character expectations appear here for shared mentoring."
                                        } else {
                                            "Silakan klaim anak asuh Anda terlebih dahulu agar harapan karakter dari orang tua tampil di sini untuk dinasehatkan bersama."
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    if (onOpenPilihAnakAsuh != null) {
                                        Button(
                                            onClick = onOpenPilihAnakAsuh,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                            modifier = Modifier
                                                .sizeIn(minHeight = 48.dp)
                                                .testTag("btn_open_pilih_anak_asuh_from_harapan")
                                        ) {
                                            Text(if (isEn) "Open Claim Foster Kids" else "Buka Klaim Anak Asuh")
                                        }
                                    }
                                }
                            }
                        }
                    } else if (filteredStudents.isEmpty()) {
                        item {
                            Text(
                                text = if (isEn) "No matching student expectations found." else "Tidak ada data harapan orang tua yang cocok.",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(filteredStudents, key = { it.id }) { student ->
                            val studentExps = expectationsByStudent[student.id].orEmpty()
                            val unreadForStudent = studentExps.count { !it.isReadByWali }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_harapan_student_${student.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (unreadForStudent > 0) Color(0xFF34D399)
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Identitas Siswa & Status Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
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
                                            Text(
                                                text = student.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${student.classGrade.ifBlank { "-" }} • ${student.dormitory.ifBlank { "-" }}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (student.motherName.isNotBlank()) {
                                                Text(
                                                    text = (if (isEn) "Parent: " else "Orang Tua: ") + student.motherName,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF047857),
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        if (unreadForStudent > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(999.dp),
                                                color = Color(0xFFFEE2E2)
                                            ) {
                                                Text(
                                                    text = "$unreadForStudent ${if (isEn) "New" else "Baru"}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFFDC2626),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        } else if (studentExps.isNotEmpty()) {
                                            Surface(
                                                shape = RoundedCornerShape(999.dp),
                                                color = Color(0xFFDCFCE7)
                                            ) {
                                                Text(
                                                    text = "${studentExps.size} ${if (isEn) "Hopes" else "Harapan"}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (studentExps.isEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                        ) {
                                            Text(
                                                text = if (isEn) {
                                                    "Parent has not submitted character expectations yet."
                                                } else {
                                                    "Orang tua belum menuliskan harapan karakter untuk ananda."
                                                },
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp)
                                            )
                                        }
                                    } else {
                                        studentExps.forEach { exp ->
                                            val focusList = remember(exp.characterFocusCsv) {
                                                ParentExpectationHelper.parseFocusList(exp.characterFocusCsv)
                                            }
                                            val isEditingReply = activeReplyExpectationId == exp.id
                                            val currentDraft = draftFollowUps[exp.id] ?: exp.waliFollowUpNote

                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFFF8FAFC),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = DateUtils.formatDateTime(exp.updatedAt, lang),
                                                            fontSize = 10.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        if (!exp.isReadByWali) {
                                                            TextButton(
                                                                onClick = { onMarkReadByWali(student.id) },
                                                                modifier = Modifier.testTag("btn_mark_read_harapan_${exp.id}")
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.DoneAll,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(15.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = if (isEn) "Mark Read" else "Tandai Dibaca",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }

                                                    if (focusList.isNotEmpty()) {
                                                        FlowRow(
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            focusList.forEach { key ->
                                                                Surface(
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = Color(0xFFECFDF5)
                                                                ) {
                                                                    Text(
                                                                        text = ParentExpectationHelper.displayFocusLabel(key, isEn),
                                                                        fontSize = 10.5.sp,
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        color = Color(0xFF047857),
                                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    Text(
                                                        text = exp.characterExpectation,
                                                        fontSize = 13.sp,
                                                        lineHeight = 18.5.sp,
                                                        color = Color(0xFF0F172A)
                                                    )

                                                    if (exp.adviceFocusNotes.isNotBlank()) {
                                                        Text(
                                                            text = (if (isEn) "Advice Requested: " else "Perlu Dinasehatkan: ") + exp.adviceFocusNotes,
                                                            fontSize = 11.5.sp,
                                                            color = Color(0xFF334155)
                                                        )
                                                    }

                                                    if (exp.parentPrayerMessage.isNotBlank()) {
                                                        Text(
                                                            text = (if (isEn) "Parent Prayer: " else "Doa Orang Tua: ") + exp.parentPrayerMessage,
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = Color(0xFF047857)
                                                        )
                                                    }

                                                    if (exp.waliFollowUpNote.isNotBlank() && !isEditingReply) {
                                                        HorizontalDivider()
                                                        Surface(
                                                            shape = RoundedCornerShape(10.dp),
                                                            color = Color(0xFFECFDF5),
                                                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                                                        ) {
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(8.dp),
                                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                                            ) {
                                                                Text(
                                                                    text = if (isEn) "Your Mentoring Note:" else "Catatan Ikhtiar & Nasehat Anda:",
                                                                    fontSize = 10.5.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF065F46)
                                                                )
                                                                Text(
                                                                    text = exp.waliFollowUpNote,
                                                                    fontSize = 12.sp,
                                                                    color = Color(0xFF0F172A)
                                                                )
                                                            }
                                                        }
                                                    }

                                                    if (isEditingReply) {
                                                        OutlinedTextField(
                                                            value = currentDraft,
                                                            onValueChange = { draftFollowUps[exp.id] = it },
                                                            label = {
                                                                Text(
                                                                    if (isEn) "Wali Asuh Mentoring & Advice Note" else "Catatan Ikhtiar & Nasehat Wali Asuh",
                                                                    fontSize = 11.5.sp
                                                                )
                                                            },
                                                            placeholder = {
                                                                Text(
                                                                    if (isEn) "Record advice given or child's progress..." else "Catat nasehat yang sudah disampaikan atau perkembangan ananda...",
                                                                    fontSize = 11.5.sp
                                                                )
                                                            },
                                                            trailingIcon = {
                                                                IconButton(
                                                                    onClick = { startVoiceFollowUp(exp.id) },
                                                                    modifier = Modifier
                                                                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                                                        .testTag("btn_voice_wali_harapan_note_${exp.id}")
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Mic,
                                                                        contentDescription = if (isEn) "Voice note" else "Suara ke teks catatan nasehat",
                                                                        tint = Color(0xFF047857)
                                                                    )
                                                                }
                                                            },
                                                            minLines = 2,
                                                            maxLines = 4,
                                                            shape = RoundedCornerShape(10.dp),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .testTag("input_wali_harapan_note_${exp.id}")
                                                        )
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.End,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            TextButton(
                                                                onClick = { activeReplyExpectationId = null }
                                                            ) {
                                                                Text(if (isEn) "Cancel" else "Batal")
                                                            }
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Button(
                                                                onClick = {
                                                                    onSaveFollowUpNote(exp, currentDraft)
                                                                    activeReplyExpectationId = null
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                                                modifier = Modifier.testTag("btn_save_wali_harapan_note_${exp.id}")
                                                            ) {
                                                                Text(if (isEn) "Save Note" else "Simpan Catatan", fontSize = 12.sp)
                                                            }
                                                        }
                                                    } else {
                                                        OutlinedButton(
                                                            onClick = {
                                                                if (!draftFollowUps.containsKey(exp.id)) {
                                                                    draftFollowUps[exp.id] = exp.waliFollowUpNote
                                                                }
                                                                activeReplyExpectationId = exp.id
                                                            },
                                                            shape = RoundedCornerShape(10.dp),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .sizeIn(minHeight = 48.dp)
                                                                .testTag("btn_toggle_reply_harapan_${exp.id}")
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.EditNote,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = if (exp.waliFollowUpNote.isBlank()) {
                                                                    if (isEn) "Write Mentoring & Advice Note" else "Tulis Catatan Ikhtiar & Nasehat"
                                                                } else {
                                                                    if (isEn) "Edit Mentoring Note" else "Ubah Catatan Nasehat"
                                                                },
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Tombol Cepat ke Chat Orang Tua
                                    OutlinedButton(
                                        onClick = { onOpenChatWithStudent(student) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .sizeIn(minHeight = 48.dp)
                                            .testTag("btn_open_chat_from_harapan_${student.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubbleOutline,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isEn) "Open Parent Chat" else "Buka Chat Orang Tua",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
