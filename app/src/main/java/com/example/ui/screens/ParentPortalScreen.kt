package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChildStudent
import com.example.data.model.FastingRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentMessage
import com.example.ui.HealthViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.components.StudentAvatar
import com.example.util.DateUtils
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.LocalAppStrings

private enum class ParentPortalTab(
    val titleId: String,
    val titleEn: String,
    val icon: ImageVector,
    val tag: String
) {
    HOME("Ringkasan", "Overview", Icons.Default.Home, "parent_tab_home"),
    MENGAJI("Mengaji", "Mengaji", Icons.AutoMirrored.Filled.MenuBook, "parent_tab_mengaji"),
    MEDICAL("Riwayat Sakit", "Medical", Icons.Default.LocalHospital, "parent_tab_medical"),
    FASTING("Puasa", "Fasting", Icons.Default.NightlightRound, "parent_tab_fasting"),
    MESSAGES("Pesan", "Messages", Icons.Default.ChatBubbleOutline, "parent_tab_messages")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPortalScreen(
    viewModel: HealthViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val activeLink by viewModel.activeParentAccessLink.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val mengajiRecords by viewModel.mengajiRecords.collectAsStateWithLifecycle()
    val medicalRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val fastingRecords by viewModel.fastingRecords.collectAsStateWithLifecycle()
    val parentMessages by viewModel.parentMessages.collectAsStateWithLifecycle()
    val currentParentStudentId by viewModel.currentParentStudentId.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    val resolvedStudentId = activeLink?.studentId ?: currentParentStudentId
    val student = remember(students, resolvedStudentId) {
        students.firstOrNull { it.id == resolvedStudentId }
    } ?: remember(activeLink, resolvedStudentId) {
        activeLink?.let { link ->
            ChildStudent(
                id = link.studentId,
                name = link.studentName,
                nis = link.studentNis,
                gender = "L",
                age = 12,
                dormitory = link.studentDorm,
                classGrade = link.studentClass,
                allergies = "-",
                bloodType = "-",
                emergencyContact = link.parentPhone,
                specialNotes = "",
                motherName = link.parentName,
                parentPhone = link.parentPhone,
                assignedWaliUsername = link.assignedWaliUsername,
                assignedWaliName = link.assignedWaliName,
                schoolId = link.schoolId
            )
        }
    }

    val visibleTabs = remember(activeLink) {
        buildList {
            add(ParentPortalTab.HOME)
            if (activeLink?.canViewMengaji == true) add(ParentPortalTab.MENGAJI)
            if (activeLink?.canViewMedical == true) add(ParentPortalTab.MEDICAL)
            if (activeLink?.canViewFasting == true) add(ParentPortalTab.FASTING)
            add(ParentPortalTab.MESSAGES)
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val safeTabIndex = selectedTabIndex.coerceIn(0, (visibleTabs.size - 1).coerceAtLeast(0))
    val currentTab = visibleTabs.getOrElse(safeTabIndex) { ParentPortalTab.HOME }

    val studentMengaji = remember(mengajiRecords, student, resolvedStudentId) {
        if (student != null) {
            MengajiHelper.recordsForStudent(mengajiRecords, student).sortedByDescending { it.sessionDate }
        } else {
            mengajiRecords.filter { it.studentId == resolvedStudentId }.sortedByDescending { it.sessionDate }
        }
    }
    val studentMedical = remember(medicalRecords, resolvedStudentId) {
        medicalRecords.filter { it.studentId == resolvedStudentId }.sortedByDescending { it.planDate }
    }
    val studentFasting = remember(fastingRecords, resolvedStudentId) {
        fastingRecords.filter { it.studentId == resolvedStudentId }.sortedByDescending { it.sessionDate }
    }
    val threadMessages = remember(parentMessages, resolvedStudentId) {
        parentMessages.filter { it.studentId == resolvedStudentId }.sortedBy { it.createdAt }
    }
    val unreadFromWaliCount = remember(threadMessages) {
        threadMessages.count { !it.isReadByParent && !it.senderRole.equals("PARENT", ignoreCase = true) }
    }

    LaunchedEffect(currentTab, resolvedStudentId, threadMessages.size) {
        if (currentTab == ParentPortalTab.MESSAGES && resolvedStudentId > 0L && unreadFromWaliCount > 0) {
            viewModel.markParentMessagesAsRead(resolvedStudentId, "PARENT")
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_portal_screen"),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4338CA),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = {
                    Column {
                        Text(
                            text = if (isEn) "Parent Care Portal" else "Portal Orang Tua",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = schoolProfile.shortName.ifBlank { schoolProfile.schoolName },
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    Surface(
                        onClick = {
                            val next = if (appLanguage == AppLanguage.INDONESIAN) AppLanguage.ENGLISH else AppLanguage.INDONESIAN
                            viewModel.setAppLanguage(next)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.18f),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = if (appLanguage == AppLanguage.INDONESIAN) "ID" else "EN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.syncAllWithCloud { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isSyncing,
                        modifier = Modifier.testTag("btn_parent_sync")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = if (isEn) "Sync" else "Sinkron")
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("btn_parent_logout")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = if (isEn) "Logout" else "Keluar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
            ) {
                ScrollableTabRow(
                    selectedTabIndex = safeTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color(0xFF4338CA),
                    edgePadding = 8.dp
                ) {
                    visibleTabs.forEachIndexed { idx, tab ->
                        val labelText = if (tab == ParentPortalTab.MESSAGES && unreadFromWaliCount > 0) {
                            "${if (isEn) tab.titleEn else tab.titleId} ($unreadFromWaliCount)"
                        } else {
                            if (isEn) tab.titleEn else tab.titleId
                        }
                        Tab(
                            selected = safeTabIndex == idx,
                            onClick = { selectedTabIndex = idx },
                            text = {
                                Text(
                                    text = labelText,
                                    fontSize = 12.sp,
                                    fontWeight = if (safeTabIndex == idx) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }

                when (currentTab) {
                    ParentPortalTab.HOME -> ParentOverviewContent(
                        student = student,
                        link = activeLink,
                        mengajiCount = studentMengaji.size,
                        medicalCount = studentMedical.size,
                        fastingCount = studentFasting.size,
                        unreadMessageCount = unreadFromWaliCount,
                        onOpenTab = { targetTab ->
                            val idx = visibleTabs.indexOf(targetTab)
                            if (idx >= 0) selectedTabIndex = idx
                        }
                    )
                    ParentPortalTab.MENGAJI -> ParentMengajiContent(records = studentMengaji)
                    ParentPortalTab.MEDICAL -> ParentMedicalContent(records = studentMedical)
                    ParentPortalTab.FASTING -> ParentFastingContent(records = studentFasting)
                    ParentPortalTab.MESSAGES -> ParentChatContent(
                        student = student,
                        link = activeLink,
                        messages = threadMessages,
                        onSend = { msgText ->
                            val target = student
                            if (target != null) {
                                viewModel.sendParentMessage(target, msgText, "PARENT")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentOverviewContent(
    student: ChildStudent?,
    link: ParentAccessLink?,
    mengajiCount: Int,
    medicalCount: Int,
    fastingCount: Int,
    unreadMessageCount: Int,
    onOpenTab: (ParentPortalTab) -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color(0xFF4338CA), Color(0xFF6D28D9))))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StudentAvatar(
                            name = student?.name ?: link?.studentName ?: "Ananda",
                            gender = student?.gender ?: "L",
                            size = 54.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student?.name ?: link?.studentName.orEmpty().ifBlank { "-" },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = if (isEn) {
                                    "Class ${student?.classGrade ?: link?.studentClass ?: "-"} • ${student?.dormitory ?: link?.studentDorm ?: "-"}"
                                } else {
                                    "Kelas ${student?.classGrade ?: link?.studentClass ?: "-"} • ${student?.dormitory ?: link?.studentDorm ?: "-"}"
                                },
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            if (!student?.nis.isNullOrBlank()) {
                                Text(
                                    text = "NIS/NISN: ${student?.nis}",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            val waliName = student?.assignedWaliName?.ifBlank { link?.assignedWaliName.orEmpty() }
                ?: link?.assignedWaliName.orEmpty()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF4338CA))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Foster Caregiver" else "Wali Asuh Pembina",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = waliName.ifBlank { if (isEn) "Not assigned yet" else "Belum ditentukan" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF4338CA),
                        modifier = Modifier
                            .clickable { onOpenTab(ParentPortalTab.MESSAGES) }
                            .testTag("btn_overview_open_chat")
                    ) {
                        Text(
                            text = if (unreadMessageCount > 0) {
                                if (isEn) "Chat ($unreadMessageCount)" else "Pesan ($unreadMessageCount)"
                            } else {
                                if (isEn) "Message" else "Kirim Pesan"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = if (isEn) "Accessible Reports" else "Laporan Yang Dibuka Untuk Anda",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (link?.canViewMengaji == true) {
            item {
                PortalModuleSummaryCard(
                    title = if (isEn) "Mengaji History" else "Riwayat Mengaji",
                    subtitle = if (isEn) "$mengajiCount records available" else "$mengajiCount catatan setoran mengaji",
                    accentColor = Color(0xFF047857),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    onClick = { onOpenTab(ParentPortalTab.MENGAJI) },
                    testTag = "card_parent_summary_mengaji"
                )
            }
        }
        if (link?.canViewMedical == true) {
            item {
                PortalModuleSummaryCard(
                    title = if (isEn) "Medical History" else "Riwayat Sakit & Perawatan",
                    subtitle = if (isEn) "$medicalCount health records" else "$medicalCount catatan kesehatan",
                    accentColor = Color(0xFF0284C7),
                    icon = Icons.Default.LocalHospital,
                    onClick = { onOpenTab(ParentPortalTab.MEDICAL) },
                    testTag = "card_parent_summary_medical"
                )
            }
        }
        if (link?.canViewFasting == true) {
            item {
                PortalModuleSummaryCard(
                    title = if (isEn) "Fasting History" else "Riwayat Puasa",
                    subtitle = if (isEn) "$fastingCount fasting sessions" else "$fastingCount catatan sahur & berbuka",
                    accentColor = Color(0xFF0F766E),
                    icon = Icons.Default.NightlightRound,
                    onClick = { onOpenTab(ParentPortalTab.FASTING) },
                    testTag = "card_parent_summary_fasting"
                )
            }
        }
    }
}

@Composable
private fun PortalModuleSummaryCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ParentMengajiContent(records: List<MengajiRecord>) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_mengaji_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (records.isEmpty()) {
            item {
                ParentEmptyNotice(if (isEn) "No mengaji records yet." else "Belum ada catatan mengaji.")
            }
        } else {
            items(records, key = { "pmengaji_${it.id}" }) { rec ->
                val isFluent = MengajiHelper.isFluent(rec.fluencyStatus)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (isFluent) Color(0xFFBBF7D0) else Color(0xFFFCA5A5))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = MengajiHelper.formatCompactPosition(rec),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isFluent) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    text = MengajiHelper.fluencyLabel(rec.fluencyStatus, isEn),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFluent) Color(0xFF15803D) else Color(0xFFDC2626),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = "${DateUtils.formatShortDate(rec.sessionDate, s.language)} • ${if (isEn) "Caregiver" else "Pembina"}: ${rec.waliName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (rec.notes.isNotBlank()) {
                            Text(rec.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentMedicalContent(records: List<MedicalRecord>) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_medical_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (records.isEmpty()) {
            item {
                ParentEmptyNotice(if (isEn) "No medical records yet." else "Alhamdulillah, belum ada riwayat sakit.")
            }
        } else {
            items(records, key = { "pmed_${it.id}" }) { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateUtils.formatShortDate(rec.planDate, s.language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatusBadge(status = rec.status)
                        }
                        Text(
                            text = "${if (isEn) "Complaint" else "Keluhan"}: ${rec.complaint.ifBlank { "-" }}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${if (isEn) "Facility" else "Faskes"}: ${rec.faskes}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (rec.diagnosis.isNotBlank()) {
                            Text("${if (isEn) "Diagnosis" else "Diagnosa"}: ${rec.diagnosis}", fontSize = 12.sp)
                        }
                        if (rec.prescription.isNotBlank()) {
                            Text("${if (isEn) "Prescription" else "Resep"}: ${rec.prescription}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentFastingContent(records: List<FastingRecord>) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_fasting_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (records.isEmpty()) {
            item {
                ParentEmptyNotice(if (isEn) "No fasting records yet." else "Belum ada catatan riwayat puasa.")
            }
        } else {
            items(records, key = { "pfast_${it.id}" }) { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFF99F6E4))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(rec.sessionTitle, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                        Text(
                            text = DateUtils.formatShortDate(rec.sessionDate, s.language),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val sahurLabel = if (rec.isSahurChecked) "✓ Sahur" else "○ Sahur"
                            val bukaLabel = if (rec.isBukaChecked) "✓ ${if (isEn) "Iftar" else "Buka"}" else "○ ${if (isEn) "Iftar" else "Buka"}"
                            Text(
                                text = sahurLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (rec.isSahurChecked) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = bukaLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (rec.isBukaChecked) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentChatContent(
    student: ChildStudent?,
    link: ParentAccessLink?,
    messages: List<ParentMessage>,
    onSend: (String) -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    var draft by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val waliDisplay = student?.assignedWaliName?.ifBlank { link?.assignedWaliName.orEmpty() }
            ?: link?.assignedWaliName.orEmpty()
        Text(
            text = if (isEn) {
                "Chat with ${waliDisplay.ifBlank { "Foster Caregiver" }}"
            } else {
                "Percakapan dengan ${waliDisplay.ifBlank { "Wali Asuh" }}"
            },
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    ParentEmptyNotice(
                        if (isEn) "No messages yet. Write a message to your child's caregiver below."
                        else "Belum ada pesan. Tulis pesan untuk wali asuh ananda di bawah."
                    )
                }
            } else {
                items(messages, key = { "ppmsg_${it.id}" }) { msg ->
                    val isParent = msg.senderRole.equals("PARENT", ignoreCase = true)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isParent) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = if (isParent) 14.dp else 4.dp,
                                bottomEnd = if (isParent) 4.dp else 14.dp
                            ),
                            color = if (isParent) Color(0xFF4338CA) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = msg.senderName.ifBlank { if (isParent) "Orang Tua" else "Wali Asuh" },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isParent) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = msg.messageContent,
                                    fontSize = 13.sp,
                                    color = if (isParent) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = DateUtils.formatShortDate(msg.createdAt, s.language),
                                    fontSize = 9.5.sp,
                                    color = if (isParent) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
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
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text(if (isEn) "Write message to caregiver..." else "Tulis pesan ke wali asuh...", fontSize = 12.5.sp) },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_parent_portal_message")
            )
            IconButton(
                onClick = {
                    if (draft.isNotBlank()) {
                        onSend(draft.trim())
                        draft = ""
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF4338CA),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("btn_parent_portal_send")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = if (isEn) "Send" else "Kirim")
            }
        }
    }
}

@Composable
private fun ParentEmptyNotice(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
