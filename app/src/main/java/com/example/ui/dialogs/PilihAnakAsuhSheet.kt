package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ui.components.StudentAvatar
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PilihAnakAsuhSheet(
    students: List<ChildStudent>,
    currentWaliDisplayName: String,
    isStudentClaimedByCurrentWali: (ChildStudent) -> Boolean,
    isStudentClaimedByOtherWali: (ChildStudent) -> Boolean,
    canReleaseStudent: (ChildStudent) -> Boolean,
    onClaimStudents: (List<ChildStudent>) -> Unit,
    onReleaseStudent: (ChildStudent) -> Unit,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    var searchQuery by remember { mutableStateOf("") }
    // 0 = Semua, 1 = Anak Asuh Saya, 2 = Tersedia (Belum Dibina)
    var selectedFilter by remember { mutableStateOf(0) }
    val pendingSelectedIds = remember { mutableStateListOf<Long>() }

    val myClaimedStudents = remember(students, isStudentClaimedByCurrentWali) {
        students.filter { isStudentClaimedByCurrentWali(it) }.sortedBy { it.name }
    }
    val pendingStudents = remember(students, pendingSelectedIds.toList()) {
        students.filter { it.id in pendingSelectedIds }.sortedBy { it.name }
    }

    val filteredStudents = remember(students, searchQuery, selectedFilter, isStudentClaimedByCurrentWali, isStudentClaimedByOtherWali) {
        val q = searchQuery.trim().lowercase()
        students.filter { st ->
            val matchesQuery = q.isBlank() ||
                st.name.lowercase().contains(q) ||
                st.nis.lowercase().contains(q) ||
                st.classGrade.lowercase().contains(q) ||
                st.dormitory.lowercase().contains(q) ||
                st.assignedWaliName.lowercase().contains(q)
            val matchesFilter = when (selectedFilter) {
                1 -> isStudentClaimedByCurrentWali(st)
                2 -> !isStudentClaimedByCurrentWali(st) && !isStudentClaimedByOtherWali(st)
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
                                text = if (isEn) "Select Foster Students" else "Pilih Anak Asuh Binaan",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEn) {
                                    "Builder: $currentWaliDisplayName • ${myClaimedStudents.size} assigned"
                                } else {
                                    "Pembina: $currentWaliDisplayName • ${myClaimedStudents.size} anak asuh aktif"
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close" else "Tutup"
                        )
                    }
                }

                // Selected / Claimed Preview Strip
                if (myClaimedStudents.isNotEmpty() || pendingStudents.isNotEmpty()) {
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
                                    "Selected & My Foster Students (${myClaimedStudents.size} active, ${pendingStudents.size} new)"
                                } else {
                                    "Terpilih & Binaan Saya (${myClaimedStudents.size} aktif, ${pendingStudents.size} baru dipilih)"
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
                                pendingStudents.forEach { st ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0xFF4F46E5),
                                        modifier = Modifier.clickable { pendingSelectedIds.remove(st.id) }
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
                        Text(if (isEn) "Search student name, NISN, class, dorm..." else "Cari nama siswa, NISN, kelas, asrama...", fontSize = 12.5.sp)
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
                            val isOther = isStudentClaimedByOtherWali(student)
                            val canRelease = canReleaseStudent(student)
                            val isPendingChecked = student.id in pendingSelectedIds

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isMine && !isOther) {
                                        if (isPendingChecked) {
                                            pendingSelectedIds.remove(student.id)
                                        } else {
                                            pendingSelectedIds.add(student.id)
                                        }
                                    }
                                    .testTag("card_pilih_student_${student.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isMine -> Color(0xFFF0FDF4)
                                        isPendingChecked -> Color(0xFFEEF2FF)
                                        isOther -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (isMine || isPendingChecked) 1.3.dp else 1.dp,
                                    color = when {
                                        isMine -> Color(0xFF86EFAC)
                                        isPendingChecked -> Color(0xFF6366F1)
                                        isOther -> MaterialTheme.colorScheme.outlineVariant
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
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
                                        Text(
                                            text = student.name,
                                            fontSize = 13.5.sp,
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
                                        if (isOther) {
                                            val owner = student.assignedWaliName.ifBlank { student.assignedWaliUsername }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(12.dp))
                                                Text(
                                                    text = if (isEn) "Claimed by: $owner" else "Sudah dipilih: $owner",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFFDC2626)
                                                )
                                            }
                                        } else if (isMine) {
                                            val owner = student.assignedWaliName.ifBlank { currentWaliDisplayName }
                                            Text(
                                                text = if (isEn) "Assigned to: $owner" else "Anak Asuh Anda ($owner)",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF15803D)
                                            )
                                        }
                                    }

                                    when {
                                        canRelease -> {
                                            OutlinedButton(
                                                onClick = { onReleaseStudent(student) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier
                                                    .heightIn(min = 36.dp)
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
                                        isOther -> {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = if (isEn) "Locked" else "Terkunci",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        else -> {
                                            Checkbox(
                                                checked = isPendingChecked,
                                                onCheckedChange = { checked ->
                                                    if (checked) {
                                                        if (student.id !in pendingSelectedIds) pendingSelectedIds.add(student.id)
                                                    } else {
                                                        pendingSelectedIds.remove(student.id)
                                                    }
                                                },
                                                modifier = Modifier.testTag("chk_claim_student_${student.id}")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Confirm Action
                if (pendingStudents.isNotEmpty()) {
                    Button(
                        onClick = {
                            onClaimStudents(pendingStudents.toList())
                            pendingSelectedIds.clear()
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
                                "Save ${pendingStudents.size} Selected Student(s)"
                            } else {
                                "Simpan ${pendingStudents.size} Anak Asuh Terpilih"
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
