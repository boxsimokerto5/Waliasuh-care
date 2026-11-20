package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.IncomingTandemRequestBundle
import com.example.data.model.TandemWaliHelper
import com.example.data.model.WaliAccount
import com.example.ui.components.StudentAvatar
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PilihAnakAsuhSheet(
    students: List<ChildStudent>,
    currentWaliDisplayName: String,
    isStudentClaimedByCurrentWali: (ChildStudent) -> Boolean,
    isStudentClaimedByOtherWali: (ChildStudent) -> Boolean,
    canReleaseStudent: (ChildStudent) -> Boolean,
    onClaimStudents: (List<ChildStudent>) -> Unit,
    onReleaseStudent: (ChildStudent) -> Unit,
    onDismiss: () -> Unit,
    incomingTandemBundles: List<IncomingTandemRequestBundle> = emptyList(),
    waliAccounts: List<WaliAccount> = emptyList(),
    currentUsername: String? = null,
    isStudentPrimaryWali: (ChildStudent) -> Boolean = { st ->
        TandemWaliHelper.isPrimaryWali(st, currentUsername, currentWaliDisplayName) ||
            (isStudentClaimedByCurrentWali(st) && !TandemWaliHelper.isTandemMember(st, currentUsername, currentWaliDisplayName))
    },
    isStudentTandemWali: (ChildStudent) -> Boolean = { st ->
        TandemWaliHelper.isTandemMember(st, currentUsername, currentWaliDisplayName)
    },
    hasPendingTandemRequest: (ChildStudent) -> Boolean = { st ->
        TandemWaliHelper.hasPendingRequest(st, currentUsername, currentWaliDisplayName)
    },
    onRequestTandemJoin: (List<ChildStudent>) -> Unit = {},
    onCancelTandemRequest: (ChildStudent) -> Unit = {},
    onApproveTandemBundle: (
        requesterUsername: String,
        requesterName: String,
        approvedStudentIds: Set<Long>,
        allBundleStudentIds: Set<Long>
    ) -> Unit = { _, _, _, _ -> },
    onRejectTandemBundle: (
        requesterUsername: String,
        requesterName: String,
        allBundleStudentIds: Set<Long>
    ) -> Unit = { _, _, _ -> },
    onInviteTandemWaliBatch: (targetWali: WaliAccount, targetStudents: List<ChildStudent>) -> Unit = { _, _ -> },
    onRemoveTandemMember: (student: ChildStudent, memberUsername: String, memberName: String) -> Unit = { _, _, _ -> },
    onPromoteTandemToPrimary: (student: ChildStudent, memberUsername: String, memberName: String) -> Unit = { _, _, _ -> }
) {
    val isEn = LocalAppStrings.current.isEnglish
    var searchQuery by remember { mutableStateOf("") }
    // 0 = Semua, 1 = Anak Asuh Saya, 2 = Belum Ada Wali, 3 = Bisa Tandem
    var selectedFilter by remember { mutableStateOf(0) }
    val pendingClaimIds = remember { mutableStateListOf<Long>() }
    val pendingTandemIds = remember { mutableStateListOf<Long>() }
    var showInviteTandemDialog by remember { mutableStateOf(false) }

    val myClaimedStudents = remember(students, isStudentClaimedByCurrentWali) {
        students.filter { isStudentClaimedByCurrentWali(it) }.sortedBy { it.name }
    }
    val myPrimaryStudents = remember(myClaimedStudents, isStudentPrimaryWali) {
        myClaimedStudents.filter { isStudentPrimaryWali(it) }
    }
    val pendingClaimStudents = remember(students, pendingClaimIds.toList()) {
        students.filter { it.id in pendingClaimIds }.sortedBy { it.name }
    }
    val pendingTandemStudents = remember(students, pendingTandemIds.toList()) {
        students.filter { it.id in pendingTandemIds }.sortedBy { it.name }
    }
    val invitableWalis = remember(waliAccounts, currentUsername, currentWaliDisplayName) {
        waliAccounts.filter { acc ->
            acc.role.equals("WALI", ignoreCase = true) &&
                !TandemWaliHelper.matchesIdentity(
                    acc.username,
                    acc.name,
                    currentUsername,
                    currentWaliDisplayName
                )
        }.sortedBy { it.name }
    }

    val joinableCount = remember(students, isStudentClaimedByCurrentWali, isStudentClaimedByOtherWali, hasPendingTandemRequest) {
        students.count { st ->
            !isStudentClaimedByCurrentWali(st) &&
                isStudentClaimedByOtherWali(st) &&
                TandemWaliHelper.remainingTandemSlots(st) > 0 &&
                !hasPendingTandemRequest(st)
        }
    }

    val filteredStudents = remember(
        students,
        searchQuery,
        selectedFilter,
        isStudentClaimedByCurrentWali,
        isStudentClaimedByOtherWali,
        hasPendingTandemRequest
    ) {
        val q = searchQuery.trim().lowercase()
        students.filter { st ->
            val teamNames = TandemWaliHelper.allAssignedWaliRefs(st)
                .joinToString(" ") { "${it.first} ${it.second}" }
                .lowercase()
            val matchesQuery = q.isBlank() ||
                st.name.lowercase().contains(q) ||
                st.nis.lowercase().contains(q) ||
                st.classGrade.lowercase().contains(q) ||
                st.dormitory.lowercase().contains(q) ||
                st.assignedWaliName.lowercase().contains(q) ||
                teamNames.contains(q)
            val matchesFilter = when (selectedFilter) {
                1 -> isStudentClaimedByCurrentWali(st)
                2 -> !isStudentClaimedByCurrentWali(st) && !isStudentClaimedByOtherWali(st)
                3 -> !isStudentClaimedByCurrentWali(st) &&
                    isStudentClaimedByOtherWali(st) &&
                    TandemWaliHelper.remainingTandemSlots(st) > 0 &&
                    !hasPendingTandemRequest(st)
                else -> true
            }
            matchesQuery && matchesFilter
        }.sortedWith(
            compareByDescending<ChildStudent> { isStudentClaimedByCurrentWali(it) }
                .thenBy { isStudentClaimedByOtherWali(it) }
                .thenBy { it.name }
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = Color(0xFF4338CA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "Foster Students & Tandem Team" else "Anak Asuh & Tim Tandem",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEn) {
                                    "Caregiver: $currentWaliDisplayName • ${myClaimedStudents.size} active"
                                } else {
                                    "Pembina: $currentWaliDisplayName • ${myClaimedStudents.size} anak aktif"
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (myPrimaryStudents.isNotEmpty() && invitableWalis.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { showInviteTandemDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = BorderStroke(1.dp, Color(0xFF4F46E5).copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .testTag("btn_open_invite_tandem")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GroupAdd,
                                    contentDescription = null,
                                    tint = Color(0xFF4338CA),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isEn) "Invite Tandem" else "Undang Tandem",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4338CA)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isEn) "Close" else "Tutup"
                            )
                        }
                    }
                }

                // Incoming Tandem Request Bundles (untuk Wali Utama)
                if (incomingTandemBundles.isNotEmpty()) {
                    IncomingTandemRequestsSection(
                        bundles = incomingTandemBundles,
                        isEn = isEn,
                        onApproveBundle = onApproveTandemBundle,
                        onRejectBundle = onRejectTandemBundle
                    )
                }

                // Selected / Claimed Preview Strip
                if (myClaimedStudents.isNotEmpty() || pendingClaimStudents.isNotEmpty() || pendingTandemStudents.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                        border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isEn) {
                                    "My Foster Kids (${myClaimedStudents.size}) • Claim New (${pendingClaimStudents.size}) • Tandem Req (${pendingTandemStudents.size})"
                                } else {
                                    "Binaan Aktif (${myClaimedStudents.size}) • Klaim Baru (${pendingClaimStudents.size}) • Ajuan Tandem (${pendingTandemStudents.size})"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3730A3)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                pendingClaimStudents.forEach { st ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0xFF4F46E5),
                                        modifier = Modifier.clickable { pendingClaimIds.remove(st.id) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(st.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                pendingTandemStudents.forEach { st ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0xFF0284C7),
                                        modifier = Modifier.clickable { pendingTandemIds.remove(st.id) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                            Text(st.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                myClaimedStudents.forEach { st ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFA5B4FC))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(12.dp))
                                            Text(st.name, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1B4B))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (isEn) "Search student, class, dorm, or caregiver..." else "Cari nama siswa, kelas, asrama, atau wali...",
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
                        .testTag("input_search_pilih_anak_asuh")
                )

                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == 0,
                        onClick = { selectedFilter = 0 },
                        label = { Text(if (isEn) "All (${students.size})" else "Semua (${students.size})", fontSize = 11.5.sp) },
                        modifier = Modifier.testTag("chip_pilih_filter_all")
                    )
                    FilterChip(
                        selected = selectedFilter == 1,
                        onClick = { selectedFilter = 1 },
                        label = { Text(if (isEn) "Mine (${myClaimedStudents.size})" else "Anak Asuh Saya (${myClaimedStudents.size})", fontSize = 11.5.sp) },
                        modifier = Modifier.testTag("chip_pilih_filter_mine")
                    )
                    val availableCount = students.count { !isStudentClaimedByCurrentWali(it) && !isStudentClaimedByOtherWali(it) }
                    FilterChip(
                        selected = selectedFilter == 2,
                        onClick = { selectedFilter = 2 },
                        label = { Text(if (isEn) "Available ($availableCount)" else "Belum Ada Wali ($availableCount)", fontSize = 11.5.sp) },
                        modifier = Modifier.testTag("chip_pilih_filter_available")
                    )
                    FilterChip(
                        selected = selectedFilter == 3,
                        onClick = { selectedFilter = 3 },
                        label = { Text(if (isEn) "Join Tandem ($joinableCount)" else "Bisa Tandem ($joinableCount)", fontSize = 11.5.sp) },
                        modifier = Modifier.testTag("chip_pilih_filter_tandem")
                    )
                }

                // List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 180.dp, max = 380.dp),
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
                                    text = if (isEn) "No matching students found." else "Tidak ada siswa yang sesuai.",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredStudents, key = { "claim_${it.id}" }) { student ->
                            val isMine = isStudentClaimedByCurrentWali(student)
                            val isPrimary = isMine && isStudentPrimaryWali(student)
                            val isTandem = isMine && !isPrimary && isStudentTandemWali(student)
                            val isOther = isStudentClaimedByOtherWali(student)
                            val isPendingRequest = isOther && hasPendingTandemRequest(student)
                            val remainingSlots = TandemWaliHelper.remainingTandemSlots(student)
                            val canRequestJoin = isOther && !isPendingRequest && remainingSlots > 0
                            val canRelease = canReleaseStudent(student)
                            val isPendingClaimChecked = student.id in pendingClaimIds
                            val isPendingTandemChecked = student.id in pendingTandemIds
                            val tandemMembers = remember(student.tandemWalisJson) {
                                TandemWaliHelper.tandemMembers(student)
                            }
                            val totalWalis = TandemWaliHelper.totalAssignedCount(student)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = (!isMine && !isOther) || canRequestJoin) {
                                        if (canRequestJoin) {
                                            if (isPendingTandemChecked) {
                                                pendingTandemIds.remove(student.id)
                                            } else {
                                                pendingTandemIds.add(student.id)
                                            }
                                        } else {
                                            if (isPendingClaimChecked) {
                                                pendingClaimIds.remove(student.id)
                                            } else {
                                                pendingClaimIds.add(student.id)
                                            }
                                        }
                                    }
                                    .testTag("card_pilih_student_${student.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isMine -> Color(0xFFF0FDF4)
                                        isPendingClaimChecked -> Color(0xFFEEF2FF)
                                        isPendingTandemChecked -> Color(0xFFE0F2FE)
                                        isPendingRequest -> Color(0xFFFFFBEB)
                                        isOther && remainingSlots == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (isMine || isPendingClaimChecked || isPendingTandemChecked) 1.3.dp else 1.dp,
                                    color = when {
                                        isMine -> Color(0xFF86EFAC)
                                        isPendingClaimChecked -> Color(0xFF6366F1)
                                        isPendingTandemChecked -> Color(0xFF0284C7)
                                        isPendingRequest -> Color(0xFFFCD34D)
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StudentAvatar(
                                            name = student.name,
                                            gender = student.gender,
                                            size = 40.dp,
                                            photoUrl = student.photoUrl,
                                            photoFallbackUrl = student.photoFallbackUrl
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = student.name,
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (totalWalis > 0) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (totalWalis >= TandemWaliHelper.MAX_TOTAL_WALIS) {
                                                            Color(0xFFFEE2E2)
                                                        } else {
                                                            Color(0xFFE0F2FE)
                                                        }
                                                    ) {
                                                        Text(
                                                            text = "$totalWalis/${TandemWaliHelper.MAX_TOTAL_WALIS}",
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (totalWalis >= TandemWaliHelper.MAX_TOTAL_WALIS) {
                                                                Color(0xFFB91C1C)
                                                            } else {
                                                                Color(0xFF0369A1)
                                                            },
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${student.classGrade.ifBlank { "-" }} • ${student.dormitory.ifBlank { "-" }}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            val ownerLabel = student.assignedWaliName.ifBlank { student.assignedWaliUsername }
                                            if (isMine) {
                                                FlowRow(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier.padding(top = 3.dp)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (isPrimary) Color(0xFFDCFCE7) else Color(0xFFE0F2FE)
                                                    ) {
                                                        Text(
                                                            text = when {
                                                                isPrimary && isEn -> "Lead Caregiver (You)"
                                                                isPrimary -> "Wali Utama (Anda)"
                                                                isTandem && isEn -> "Tandem • Lead: $ownerLabel"
                                                                isTandem -> "Wali Tandem • Utama: $ownerLabel"
                                                                else -> ownerLabel
                                                            },
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isPrimary) Color(0xFF15803D) else Color(0xFF0369A1),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    if (tandemMembers.isNotEmpty() && isPrimary) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = Color(0xFFEDE9FE)
                                                        ) {
                                                            Text(
                                                                text = "Tandem: ${tandemMembers.joinToString(", ") { it.name }}",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = Color(0xFF5B21B6),
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            } else if (isOther) {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                                    modifier = Modifier.padding(top = 3.dp)
                                                ) {
                                                    Text(
                                                        text = if (isEn) {
                                                            "Lead: $ownerLabel" + if (tandemMembers.isNotEmpty()) {
                                                                " • Tandem: ${tandemMembers.joinToString(", ") { it.name }}"
                                                            } else ""
                                                        } else {
                                                            "Wali Utama: $ownerLabel" + if (tandemMembers.isNotEmpty()) {
                                                                " • Tandem: ${tandemMembers.joinToString(", ") { it.name }}"
                                                            } else ""
                                                        },
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF334155),
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    when {
                                                        isPendingRequest -> {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.HourglassTop,
                                                                    contentDescription = null,
                                                                    tint = Color(0xFFB45309),
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                                Text(
                                                                    text = if (isEn) "Waiting for Lead Caregiver approval" else "Menunggu persetujuan Wali Utama",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFFB45309)
                                                                )
                                                            }
                                                        }
                                                        remainingSlots > 0 -> {
                                                            Text(
                                                                text = if (isEn) {
                                                                    "$remainingSlots tandem slot(s) open — check to join"
                                                                } else {
                                                                    "Tersedia $remainingSlots slot tandem — centang untuk gabung"
                                                                },
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = Color(0xFF0284C7)
                                                            )
                                                        }
                                                        else -> {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Lock,
                                                                    contentDescription = null,
                                                                    tint = Color(0xFFDC2626),
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                                Text(
                                                                    text = if (isEn) "Tandem team full (3/3)" else "Tim penuh (3/3 wali asuh)",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = Color(0xFFDC2626)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        when {
                                            canRelease -> {
                                                OutlinedButton(
                                                    onClick = { onReleaseStudent(student) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier
                                                        .heightIn(min = 40.dp)
                                                        .testTag("btn_release_student_${student.id}")
                                                ) {
                                                    Text(
                                                        text = if (isEn) "Release" else "Lepas",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB45309)
                                                    )
                                                }
                                            }
                                            isPendingRequest -> {
                                                OutlinedButton(
                                                    onClick = { onCancelTandemRequest(student) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                                    modifier = Modifier
                                                        .heightIn(min = 40.dp)
                                                        .testTag("btn_cancel_tandem_req_${student.id}")
                                                ) {
                                                    Text(
                                                        text = if (isEn) "Cancel" else "Batal",
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB45309)
                                                    )
                                                }
                                            }
                                            canRequestJoin -> {
                                                Checkbox(
                                                    checked = isPendingTandemChecked,
                                                    onCheckedChange = { checked ->
                                                        if (checked) {
                                                            if (student.id !in pendingTandemIds) pendingTandemIds.add(student.id)
                                                        } else {
                                                            pendingTandemIds.remove(student.id)
                                                        }
                                                    },
                                                    modifier = Modifier.testTag("chk_tandem_student_${student.id}")
                                                )
                                            }
                                            isOther -> {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = if (isEn) "Full" else "Penuh",
                                                    tint = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            else -> {
                                                Checkbox(
                                                    checked = isPendingClaimChecked,
                                                    onCheckedChange = { checked ->
                                                        if (checked) {
                                                            if (student.id !in pendingClaimIds) pendingClaimIds.add(student.id)
                                                        } else {
                                                            pendingClaimIds.remove(student.id)
                                                        }
                                                    },
                                                    modifier = Modifier.testTag("chk_claim_student_${student.id}")
                                                )
                                            }
                                        }
                                    }

                                    // Kontrol kelola anggota tandem bagi Wali Utama
                                    if (isPrimary && tandemMembers.isNotEmpty()) {
                                        HorizontalDivider(color = Color(0xFFBBF7D0))
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            tandemMembers.forEach { member ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "• ${member.name}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF166534),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        TextButton(
                                                            onClick = {
                                                                onPromoteTandemToPrimary(student, member.username, member.name)
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                            modifier = Modifier
                                                                .heightIn(min = 32.dp)
                                                                .testTag("btn_promote_tandem_${student.id}_${member.username}")
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Star,
                                                                contentDescription = null,
                                                                tint = Color(0xFF4F46E5),
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(
                                                                text = if (isEn) "Set Lead" else "Jadikan Utama",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF4F46E5)
                                                            )
                                                        }
                                                        TextButton(
                                                            onClick = {
                                                                onRemoveTandemMember(student, member.username, member.name)
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                            modifier = Modifier
                                                                .heightIn(min = 32.dp)
                                                                .testTag("btn_remove_tandem_${student.id}_${member.username}")
                                                        ) {
                                                            Text(
                                                                text = if (isEn) "Remove" else "Keluarkan",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFDC2626)
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
                }

                // Bottom Confirm Actions (Primary Claim dan/atau Ajukan Gabung Tandem)
                if (pendingClaimStudents.isNotEmpty() || pendingTandemStudents.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (pendingClaimStudents.isNotEmpty()) {
                            Button(
                                onClick = {
                                    onClaimStudents(pendingClaimStudents.toList())
                                    pendingClaimIds.clear()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_claim_selected_students")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEn) {
                                        "Save ${pendingClaimStudents.size} Selected Student(s)"
                                    } else {
                                        "Simpan ${pendingClaimStudents.size} Anak Asuh Terpilih"
                                    },
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (pendingTandemStudents.isNotEmpty()) {
                            Button(
                                onClick = {
                                    onRequestTandemJoin(pendingTandemStudents.toList())
                                    pendingTandemIds.clear()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_request_tandem_selected")
                            ) {
                                Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEn) {
                                        "Request Tandem Join (${pendingTandemStudents.size} Student(s))"
                                    } else {
                                        "Ajukan Gabung Tandem (${pendingTandemStudents.size} Anak)"
                                    },
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInviteTandemDialog) {
        InviteTandemDialog(
            invitableWalis = invitableWalis,
            myPrimaryStudents = myPrimaryStudents,
            isEn = isEn,
            onConfirmInvite = { targetWali, selected ->
                onInviteTandemWaliBatch(targetWali, selected)
                showInviteTandemDialog = false
            },
            onDismiss = { showInviteTandemDialog = false }
        )
    }
}

@Composable
private fun IncomingTandemRequestsSection(
    bundles: List<IncomingTandemRequestBundle>,
    isEn: Boolean,
    onApproveBundle: (String, String, Set<Long>, Set<Long>) -> Unit,
    onRejectBundle: (String, String, Set<Long>) -> Unit
) {
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }
    val selectedIdsByKey = remember { mutableStateMapOf<String, Set<Long>>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp)
            .verticalScroll(rememberScrollState())
            .testTag("section_incoming_tandem_requests"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        bundles.forEach { bundle ->
            val bundleKey = bundle.requesterUsername.ifBlank { bundle.requesterName }.lowercase()
            val allIds = remember(bundle) { bundle.students.map { it.id }.toSet() }
            val checkedIds = selectedIdsByKey[bundleKey] ?: allIds
            val isExpanded = expandedMap[bundleKey] ?: false

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = BorderStroke(1.2.dp, Color(0xFFF59E0B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_tandem_bundle_$bundleKey")
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) {
                                        "${bundle.requesterName} requests Tandem"
                                    } else {
                                        "${bundle.requesterName} mengajukan Tandem"
                                    },
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isEn) {
                                        "${bundle.students.size} foster student(s) • ${checkedIds.size} selected"
                                    } else {
                                        "${bundle.students.size} anak asuh • ${checkedIds.size} dipilih"
                                    },
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                        IconButton(
                            onClick = { expandedMap[bundleKey] = !isExpanded },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Tutup daftar" else "Lihat daftar anak",
                                tint = Color(0xFF92400E)
                            )
                        }
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.78f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            bundle.students.forEach { st ->
                                val isChecked = st.id in checkedIds
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val updated = if (isChecked) checkedIds - st.id else checkedIds + st.id
                                            selectedIdsByKey[bundleKey] = updated
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            val updated = if (checked) checkedIds + st.id else checkedIds - st.id
                                            selectedIdsByKey[bundleKey] = updated
                                        }
                                    )
                                    Text(
                                        text = "${st.name} (${st.classGrade.ifBlank { "-" }})",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onRejectBundle(bundle.requesterUsername, bundle.requesterName, allIds)
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(0.4f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_reject_tandem_all_$bundleKey")
                        ) {
                            Text(
                                text = if (isEn) "Decline All" else "Tolak",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                        Button(
                            onClick = {
                                onApproveBundle(
                                    bundle.requesterUsername,
                                    bundle.requesterName,
                                    checkedIds,
                                    allIds
                                )
                            },
                            enabled = checkedIds.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(0.6f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_approve_tandem_all_$bundleKey")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (checkedIds.size == bundle.students.size) {
                                    if (isEn) "Approve All (${bundle.students.size})" else "Setujui Semua (${bundle.students.size})"
                                } else {
                                    if (isEn) "Approve Selected (${checkedIds.size})" else "Setujui Terpilih (${checkedIds.size})"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InviteTandemDialog(
    invitableWalis: List<WaliAccount>,
    myPrimaryStudents: List<ChildStudent>,
    isEn: Boolean,
    onConfirmInvite: (WaliAccount, List<ChildStudent>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedWali by remember { mutableStateOf(invitableWalis.firstOrNull()) }
    val eligibleStudents = remember(myPrimaryStudents, selectedWali) {
        val wali = selectedWali
        myPrimaryStudents.filter { st ->
            TandemWaliHelper.remainingTandemSlots(st) > 0 &&
                (wali == null || !TandemWaliHelper.isAssignedWali(st, wali.username, wali.name))
        }
    }
    val chosenIds = remember(eligibleStudents.map { it.id }) {
        mutableStateListOf<Long>().apply { addAll(eligibleStudents.map { it.id }) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEn) "Invite Tandem Caregiver" else "Undang Rekan Wali Tandem",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isEn) {
                        "Select colleague (max 2 tandem partners per student):"
                    } else {
                        "Pilih rekan Wali Asuh (maksimal 2 tandem per anak):"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    invitableWalis.forEach { acc ->
                        val isSel = selectedWali?.username == acc.username && selectedWali?.name == acc.name
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) Color(0xFFEEF2FF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, if (isSel) Color(0xFF4F46E5) else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedWali = acc }
                                .testTag("invite_wali_option_${acc.username}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(acc.name, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                    if (acc.titlePosition.isNotBlank()) {
                                        Text(acc.titlePosition, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (isSel) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Apply to Students (${chosenIds.size}/${eligibleStudents.size})" else "Anak Asuh (${chosenIds.size}/${eligibleStudents.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (eligibleStudents.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                if (chosenIds.size == eligibleStudents.size) chosenIds.clear()
                                else {
                                    chosenIds.clear()
                                    chosenIds.addAll(eligibleStudents.map { it.id })
                                }
                            }
                        ) {
                            Text(
                                text = if (chosenIds.size == eligibleStudents.size) {
                                    if (isEn) "Clear All" else "Batal Semua"
                                } else {
                                    if (isEn) "Select All" else "Pilih Semua"
                                },
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                if (eligibleStudents.isEmpty()) {
                    Text(
                        text = if (isEn) "No eligible students with open tandem slots." else "Semua anak utama Anda sudah penuh atau sudah terhubung dengan rekan ini.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    eligibleStudents.forEach { st ->
                        val checked = st.id in chosenIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (checked) chosenIds.remove(st.id) else chosenIds.add(st.id)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { c ->
                                    if (c) {
                                        if (st.id !in chosenIds) chosenIds.add(st.id)
                                    } else {
                                        chosenIds.remove(st.id)
                                    }
                                }
                            )
                            Text("${st.name} (${st.classGrade.ifBlank { "-" }})", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val wali = selectedWali ?: return@Button
                    val targetList = eligibleStudents.filter { it.id in chosenIds }
                    onConfirmInvite(wali, targetList)
                },
                enabled = selectedWali != null && chosenIds.isNotEmpty(),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("btn_confirm_invite_tandem")
            ) {
                Text(if (isEn) "Add Tandem (${chosenIds.size})" else "Tambahkan (${chosenIds.size})")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(if (isEn) "Cancel" else "Batal")
            }
        }
    )
}
