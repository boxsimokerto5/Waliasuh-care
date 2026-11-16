package com.example.ui.dialogs

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ChildStudent
import com.example.data.model.KonselingHelper
import com.example.data.model.KonselingRecord
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InputKonselingDialog(
    claimedStudents: List<ChildStudent>,
    defaultEntryType: String = KonselingHelper.ENTRY_SESI,
    initialStudent: ChildStudent? = null,
    recordToEdit: KonselingRecord? = null,
    onOpenPilihAnakAsuh: () -> Unit,
    onSave: (
        student: ChildStudent,
        entryType: String,
        topic: String,
        urgencyLevel: String,
        moodLevel: String,
        title: String,
        summary: String,
        intervention: String,
        followUpPlan: String,
        followUpDate: Long?,
        sessionDate: Long,
        markCompleted: Boolean,
        existingRecord: KonselingRecord?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language
    val isEditMode = recordToEdit != null

    val resolvedEditStudent = remember(recordToEdit?.id, claimedStudents, initialStudent) {
        if (recordToEdit == null) {
            initialStudent
        } else {
            initialStudent
                ?: claimedStudents.firstOrNull { KonselingHelper.matchesStudent(recordToEdit, it) }
                ?: ChildStudent(
                    id = recordToEdit.studentId,
                    name = recordToEdit.studentName,
                    nis = recordToEdit.studentNis,
                    gender = "L",
                    age = 12,
                    dormitory = recordToEdit.studentDorm,
                    classGrade = recordToEdit.studentClass,
                    assignedWaliUsername = recordToEdit.ownerWaliUsername,
                    assignedWaliName = recordToEdit.ownerWaliName,
                    schoolId = recordToEdit.schoolId
                )
        }
    }

    val selectableStudents = remember(claimedStudents, resolvedEditStudent) {
        val combined = if (resolvedEditStudent != null && claimedStudents.none { it.id == resolvedEditStudent.id }) {
            listOf(resolvedEditStudent) + claimedStudents
        } else {
            claimedStudents
        }
        combined.sortedBy { it.name.lowercase() }
    }

    var selectedStudent by remember(resolvedEditStudent?.id, recordToEdit?.id) {
        mutableStateOf(
            resolvedEditStudent ?: if (selectableStudents.size == 1) selectableStudents.first() else null
        )
    }
    var studentSearch by remember { mutableStateOf("") }
    var entryType by remember(defaultEntryType, recordToEdit?.id) {
        val raw = recordToEdit?.entryType ?: defaultEntryType
        mutableStateOf(
            if (KonselingHelper.isGrowthEntry(raw)) KonselingHelper.ENTRY_PERKEMBANGAN else KonselingHelper.ENTRY_SESI
        )
    }
    var topic by remember(recordToEdit?.id, defaultEntryType) {
        mutableStateOf(
            recordToEdit?.topic ?: if (KonselingHelper.isGrowthEntry(defaultEntryType)) {
                KonselingHelper.TOPIC_APRESIASI
            } else {
                KonselingHelper.TOPIC_ADAPTASI
            }
        )
    }
    var urgencyLevel by remember(recordToEdit?.id) {
        mutableStateOf(recordToEdit?.urgencyLevel ?: KonselingHelper.URGENCY_RINGAN)
    }
    var moodLevel by remember(recordToEdit?.id) {
        mutableStateOf(
            recordToEdit?.moodLevel ?: if (KonselingHelper.isGrowthEntry(defaultEntryType)) {
                KonselingHelper.MOOD_BAIK
            } else {
                KonselingHelper.MOOD_NETRAL
            }
        )
    }
    var titleText by remember(recordToEdit?.id) {
        mutableStateOf(recordToEdit?.title ?: "")
    }
    var summaryText by remember(recordToEdit?.id) {
        mutableStateOf(recordToEdit?.summary ?: "")
    }
    var interventionText by remember(recordToEdit?.id) {
        mutableStateOf(recordToEdit?.intervention ?: "")
    }
    var followUpPlanText by remember(recordToEdit?.id) {
        mutableStateOf(recordToEdit?.followUpPlan ?: "")
    }
    var sessionDate by remember(recordToEdit?.id) {
        mutableLongStateOf(recordToEdit?.sessionDate ?: System.currentTimeMillis())
    }
    var followUpDate by remember(recordToEdit?.id) {
        mutableStateOf<Long?>(recordToEdit?.followUpDate)
    }
    var markCompleted by remember(recordToEdit?.id) {
        mutableStateOf(recordToEdit?.let { KonselingHelper.isCompleted(it.status) } ?: false)
    }

    val isGrowth = KonselingHelper.isGrowthEntry(entryType)
    val canSave = selectedStudent != null && (summaryText.isNotBlank() || titleText.isNotBlank())
    val quickTitles = if (isGrowth) {
        if (isEn) listOf("Better Dorm Adaptation", "Improved Study Focus", "More Cheerful & Sociable")
        else listOf("Lebih Betah di Asrama", "Semangat Belajar Meningkat", "Lebih Ceria & Komunikatif")
    } else {
        if (isEn) listOf("Personal Listening Session", "Homesickness Check-In", "Peer Conflict Mediation")
        else listOf("Pendampingan Personal", "Rindu Rumah & Adaptasi", "Mediasi Pertemanan")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp)
                .widthIn(max = 560.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                isEditMode -> if (isEn) "Edit Confidential Note" else "Edit Catatan Konseling"
                                isGrowth -> if (isEn) "Log Student Growth" else "Catat Perkembangan Anak"
                                else -> if (isEn) "Record Counseling Session" else "Catat Sesi Konseling"
                            },
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF6D28D9),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isEn) {
                                    "Confidential • Only visible to you (owning Wali Asuh)"
                                } else {
                                    "Catatan Rahasia • Hanya dilihat Wali Asuh bersangkutan"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF6D28D9),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_input_konseling")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close)
                    }
                }

                // Confidentiality Lock Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_input_konseling_privacy"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                    border = BorderStroke(1.dp, Color(0xFFDDD6FE))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF6D28D9),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isEn) {
                                "Strictly confidential: hidden from School Admin, other Wali Asuh, Guru Ngaji, and Parents."
                            } else {
                                "Privasi mutlak: Admin Sekolah, Wali Asuh lain, Guru Ngaji, dan Orang Tua tidak dapat melihat catatan ini."
                            },
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = Color(0xFF4C1D95)
                        )
                    }
                }

                if (selectableStudents.isEmpty() && !isEditMode) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty_input_konseling_no_claimed"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                        border = BorderStroke(1.dp, Color(0xFFDDD6FE))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isEn) "Select Your Foster Students First" else "Pilih Anak Binaan Anda Terlebih Dahulu",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF4C1D95)
                            )
                            Text(
                                text = if (isEn) {
                                    "Counseling notes are private to your own foster students. Claim students under Parent Connect → Select Foster Kids."
                                } else {
                                    "Konseling hanya dapat dicatat untuk anak asuh binaan Anda sendiri agar privasinya terjaga."
                                },
                                fontSize = 11.5.sp,
                                color = Color(0xFF5B21B6)
                            )
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenPilihAnakAsuh()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_input_konseling_open_pilih")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEn) "Select Foster Students" else "Pilih Anak Asuh", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Entry Type Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isGrowth,
                            onClick = { entryType = KonselingHelper.ENTRY_SESI },
                            label = {
                                Text(
                                    text = if (isEn) "Counseling Session" else "Sesi Konseling",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEDE9FE),
                                selectedLabelColor = Color(0xFF5B21B6),
                                selectedLeadingIconColor = Color(0xFF5B21B6)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("chip_konseling_type_sesi")
                        )
                        FilterChip(
                            selected = isGrowth,
                            onClick = { entryType = KonselingHelper.ENTRY_PERKEMBANGAN },
                            label = {
                                Text(
                                    text = if (isEn) "Growth Note" else "Perkembangan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDCFCE7),
                                selectedLabelColor = Color(0xFF166534),
                                selectedLeadingIconColor = Color(0xFF166534)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("chip_konseling_type_perkembangan")
                        )
                    }

                    // Student Selector
                    Text(
                        text = if (isEn) "Foster Student" else "Anak Binaan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isEditMode && selectedStudent != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${selectedStudent!!.name} (${selectedStudent!!.classGrade.ifBlank { "-" }})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        if (selectableStudents.size > 3) {
                            OutlinedTextField(
                                value = studentSearch,
                                onValueChange = { studentSearch = it },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                placeholder = { Text(if (isEn) "Search student..." else "Cari nama anak...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_konseling_student_search")
                            )
                        }
                        val filteredStudents = remember(selectableStudents, studentSearch) {
                            val q = studentSearch.trim().lowercase()
                            if (q.isEmpty()) selectableStudents
                            else selectableStudents.filter {
                                it.name.lowercase().contains(q) || it.classGrade.lowercase().contains(q)
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredStudents.forEach { st ->
                                val isSel = selectedStudent?.id == st.id
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedStudent = st },
                                    label = {
                                        Text(
                                            text = "${st.name} (${st.classGrade.ifBlank { "-" }})",
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("chip_konseling_student_${st.id}")
                                )
                            }
                        }
                        if (selectedStudent == null) {
                            Text(
                                text = if (isEn) "Select a foster student above first." else "Pilih satu anak binaan di atas terlebih dahulu.",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Session Date Picker
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = sessionDate }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val chosen = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, y)
                                        set(Calendar.MONTH, m)
                                        set(Calendar.DAY_OF_MONTH, d)
                                        set(Calendar.HOUR_OF_DAY, 10)
                                        set(Calendar.MINUTE, 0)
                                    }
                                    sessionDate = chosen.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_konseling_pick_date")
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${if (isEn) "Session Date" else "Tanggal Catatan"}: ${DateUtils.formatShortDate(sessionDate, lang)}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Topic Selector
                    Text(
                        text = if (isEn) "Focus Topic" else "Fokus Topik",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        KonselingHelper.ALL_TOPICS.forEach { t ->
                            val isSel = topic.equals(t, ignoreCase = true)
                            FilterChip(
                                selected = isSel,
                                onClick = { topic = t },
                                label = {
                                    Text(
                                        text = KonselingHelper.formatTopicLabel(t, isEn),
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier.testTag("chip_konseling_topic_${t.lowercase()}")
                            )
                        }
                    }

                    // Urgency & Mood
                    Text(
                        text = if (isEn) "Attention Priority" else "Tingkat Perhatian",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            KonselingHelper.URGENCY_RINGAN,
                            KonselingHelper.URGENCY_PANTAU,
                            KonselingHelper.URGENCY_PRIORITAS
                        ).forEach { urg ->
                            val isSel = urgencyLevel.equals(urg, ignoreCase = true)
                            FilterChip(
                                selected = isSel,
                                onClick = { urgencyLevel = urg },
                                label = {
                                    Text(
                                        text = KonselingHelper.formatUrgencyLabel(urg, isEn),
                                        fontSize = 10.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (urg) {
                                        KonselingHelper.URGENCY_PRIORITAS -> Color(0xFFFEE2E2)
                                        KonselingHelper.URGENCY_PANTAU -> Color(0xFFFEF3C7)
                                        else -> Color(0xFFDCFCE7)
                                    },
                                    selectedLabelColor = when (urg) {
                                        KonselingHelper.URGENCY_PRIORITAS -> Color(0xFFB91C1C)
                                        KonselingHelper.URGENCY_PANTAU -> Color(0xFFB45309)
                                        else -> Color(0xFF15803D)
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_konseling_urgency_${urg.lowercase()}")
                            )
                        }
                    }

                    Text(
                        text = if (isEn) "Student Emotional Condition" else "Kondisi Emosional Anak",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            KonselingHelper.MOOD_BAIK,
                            KonselingHelper.MOOD_NETRAL,
                            KonselingHelper.MOOD_SEDIH,
                            KonselingHelper.MOOD_TERTEKAN
                        ).forEach { m ->
                            val isSel = moodLevel.equals(m, ignoreCase = true)
                            FilterChip(
                                selected = isSel,
                                onClick = { moodLevel = m },
                                label = {
                                    Text(
                                        text = KonselingHelper.formatMoodLabel(m, isEn),
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier.testTag("chip_konseling_mood_${m.lowercase()}")
                            )
                        }
                    }

                    // Quick Title Suggestions + Title Field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickTitles.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier.clickable { titleText = preset }
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text(if (isEn) "Session Title / Subject" else "Judul Singkat / Tema") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_konseling_title")
                    )

                    OutlinedTextField(
                        value = summaryText,
                        onValueChange = { summaryText = it },
                        label = { Text(if (isEn) "Observation & Conversation Summary *" else "Ringkasan Curhat / Observasi *") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_konseling_summary")
                    )

                    OutlinedTextField(
                        value = interventionText,
                        onValueChange = { interventionText = it },
                        label = { Text(if (isEn) "Caregiver Support / Advice Given" else "Pendampingan / Nasehat yang Diberikan") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_konseling_intervention")
                    )

                    OutlinedTextField(
                        value = followUpPlanText,
                        onValueChange = { followUpPlanText = it },
                        label = { Text(if (isEn) "Follow-Up Plan (Optional)" else "Rencana Tindak Lanjut (Opsional)") },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_konseling_followup")
                    )

                    // Follow-Up Date Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = followUpDate ?: (System.currentTimeMillis() + 3 * 86_400_000L)
                                }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val chosen = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, y)
                                            set(Calendar.MONTH, m)
                                            set(Calendar.DAY_OF_MONTH, d)
                                            set(Calendar.HOUR_OF_DAY, 15)
                                            set(Calendar.MINUTE, 0)
                                        }
                                        followUpDate = chosen.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_konseling_pick_followup_date")
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = followUpDate?.let {
                                    "${if (isEn) "Follow-up" else "Jadwal Pantau"}: ${DateUtils.formatShortDate(it, lang)}"
                                } ?: if (isEn) "Set Follow-Up Date (Optional)" else "Atur Jadwal Pantau (Opsional)",
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (followUpDate != null) {
                            TextButton(
                                onClick = { followUpDate = null },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text(if (isEn) "Clear" else "Hapus", fontSize = 11.5.sp)
                            }
                        }
                    }

                    // Mark Completed Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { markCompleted = !markCompleted }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = markCompleted,
                            onCheckedChange = { markCompleted = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF15803D)),
                            modifier = Modifier.testTag("switch_konseling_completed")
                        )
                        Text(
                            text = if (isEn) "Mark as Resolved / Improved immediately" else "Tandai langsung Selesai / Membaik",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Action Footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(0.4f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text(s.nav.cancel)
                        }
                        Button(
                            onClick = {
                                val st = selectedStudent ?: return@Button
                                onSave(
                                    st,
                                    entryType,
                                    topic,
                                    urgencyLevel,
                                    moodLevel,
                                    titleText,
                                    summaryText,
                                    interventionText,
                                    followUpPlanText,
                                    followUpDate,
                                    sessionDate,
                                    markCompleted,
                                    recordToEdit
                                )
                            },
                            enabled = canSave,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                            modifier = Modifier
                                .weight(0.6f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_save_konseling")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEditMode) {
                                    if (isEn) "Save Changes" else "Simpan Perubahan"
                                } else {
                                    if (isEn) "Save Private Note" else "Simpan Rahasia"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
