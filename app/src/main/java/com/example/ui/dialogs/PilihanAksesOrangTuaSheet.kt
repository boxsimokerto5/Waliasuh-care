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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.data.model.ParentAccessLink
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PilihanAksesOrangTuaSheet(
    students: List<ChildStudent>,
    parentAccessLinks: List<ParentAccessLink>,
    isStudentClaimedByCurrentWali: (ChildStudent) -> Boolean,
    isAdmin: Boolean,
    onEnsureAccessLink: (ChildStudent) -> Unit,
    onUpdatePermissions: (ParentAccessLink, Boolean, Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    val hasClaimedStudents = remember(students, isStudentClaimedByCurrentWali) {
        students.any { isStudentClaimedByCurrentWali(it) }
    }
    var searchQuery by remember { mutableStateOf("") }
    var selectedScope by remember(hasClaimedStudents, isAdmin) {
        mutableStateOf(if (hasClaimedStudents && !isAdmin) 0 else 1)
    }

    val linkByStudentId = remember(parentAccessLinks) {
        parentAccessLinks.associateBy { it.studentId }
    }

    val filteredStudents = remember(students, searchQuery, selectedScope, isStudentClaimedByCurrentWali, linkByStudentId) {
        val q = searchQuery.trim().lowercase()
        students.filter { st ->
            val scopeMatch = if (selectedScope == 0) isStudentClaimedByCurrentWali(st) else true
            val queryMatch = q.isBlank() ||
                st.name.lowercase().contains(q) ||
                st.classGrade.lowercase().contains(q) ||
                st.dormitory.lowercase().contains(q)
            scopeMatch && queryMatch
        }.sortedWith(
            compareByDescending<ChildStudent> { linkByStudentId.containsKey(it.id) }
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
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "Parent Access Permissions" else "Pilihan Akses Orang Tua",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEn) {
                                    "Choose which history tabs parents can view"
                                } else {
                                    "Centang riwayat yang boleh dilihat oleh orang tua"
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

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(if (isEn) "Search student name..." else "Cari nama siswa...", fontSize = 12.5.sp)
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_pilihan_akses")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                        .heightIn(min = 200.dp, max = 440.dp),
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
                                    text = if (isEn) "No students to configure." else "Belum ada siswa yang dapat diatur.",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredStudents, key = { "perm_${it.id}" }) { student ->
                            val link = linkByStudentId[student.id]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_pilihan_akses_${student.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(student.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${student.classGrade.ifBlank { "-" }} • ${student.dormitory.ifBlank { "-" }}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (link == null) {
                                            Button(
                                                onClick = { onEnsureAccessLink(student) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier
                                                    .heightIn(min = 36.dp)
                                                    .testTag("btn_activate_access_${student.id}")
                                            ) {
                                                Text(
                                                    text = if (isEn) "Activate" else "Aktifkan Akses",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    if (link != null) {
                                        PermissionCheckboxRow(
                                            label = if (isEn) "Mengaji History (Progress & Fluency)" else "Riwayat Mengaji (Jilid & Kelancaran)",
                                            checked = link.canViewMengaji,
                                            accentColor = Color(0xFF047857),
                                            onCheckedChange = { checked ->
                                                onUpdatePermissions(link, checked, link.canViewMedical, link.canViewFasting)
                                            },
                                            testTag = "chk_access_mengaji_${student.id}"
                                        )
                                        PermissionCheckboxRow(
                                            label = if (isEn) "Medical History (Illness & Treatment)" else "Riwayat Sakit & Rekam Medis",
                                            checked = link.canViewMedical,
                                            accentColor = Color(0xFF0284C7),
                                            onCheckedChange = { checked ->
                                                onUpdatePermissions(link, link.canViewMengaji, checked, link.canViewFasting)
                                            },
                                            testTag = "chk_access_medical_${student.id}"
                                        )
                                        PermissionCheckboxRow(
                                            label = if (isEn) "Fasting History (Sahur & Iftar)" else "Riwayat Puasa (Sahur & Berbuka)",
                                            checked = link.canViewFasting,
                                            accentColor = Color(0xFF0F766E),
                                            onCheckedChange = { checked ->
                                                onUpdatePermissions(link, link.canViewMengaji, link.canViewMedical, checked)
                                            },
                                            testTag = "chk_access_fasting_${student.id}"
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

@Composable
private fun PermissionCheckboxRow(
    label: String,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = accentColor),
            modifier = Modifier.testTag(testTag)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
