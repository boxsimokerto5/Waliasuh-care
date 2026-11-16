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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganTransaction
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabunganStudentListSheet(
    claimedStudents: List<ChildStudent>,
    allStudents: List<ChildStudent>,
    transactions: List<TabunganTransaction>,
    isAnakSuperAdmin: Boolean,
    onSelectStudent: (ChildStudent) -> Unit,
    onQuickDeposit: (ChildStudent) -> Unit,
    onOpenPilihAnakAsuh: () -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish

    var searchQuery by remember { mutableStateOf("") }
    var scopeFilter by remember { mutableIntStateOf(0) } // 0 = Binaan Saya, 1 = Semua Sekolah (AnakSuperAdmin)
    var balanceFilter by remember { mutableIntStateOf(0) } // 0 = Semua, 1 = Ada Saldo, 2 = Saldo 0

    val claimedIds = remember(claimedStudents) { claimedStudents.map { it.id }.toSet() }

    val baseStudents = remember(claimedStudents, allStudents, transactions, isAnakSuperAdmin, scopeFilter, claimedIds) {
        if (!isAnakSuperAdmin || scopeFilter == 0) {
            claimedStudents
        } else {
            val txExtraStudents = transactions.map { it.studentId }.filter { it > 0L && it !in claimedIds }.toSet()
                .mapNotNull { sid ->
                    allStudents.firstOrNull { it.id == sid } ?: transactions.firstOrNull { it.studentId == sid }?.let { sample ->
                        ChildStudent(
                            id = sid,
                            name = sample.studentName,
                            nis = sample.studentNis,
                            gender = "L",
                            age = 12,
                            dormitory = sample.studentDorm,
                            classGrade = sample.studentClass,
                            assignedWaliUsername = sample.ownerWaliUsername,
                            assignedWaliName = sample.ownerWaliName,
                            schoolId = sample.schoolId
                        )
                    }
                }
            (claimedStudents + txExtraStudents + allStudents)
                .distinctBy { st -> st.nis.trim().uppercase().ifBlank { "id:${st.id}" } }
        }.sortedBy { it.name.lowercase() }
    }

    val filteredStudents = remember(baseStudents, transactions, searchQuery, balanceFilter) {
        val q = searchQuery.trim().lowercase()
        baseStudents.filter { st ->
            val sum = TabunganHelper.computeSummaryForStudent(transactions, st)
            val matchBalance = when (balanceFilter) {
                1 -> sum.balance > 0L
                2 -> sum.balance == 0L
                else -> true
            }
            val matchSearch = q.isEmpty() ||
                st.name.lowercase().contains(q) ||
                st.classGrade.lowercase().contains(q) ||
                st.dormitory.lowercase().contains(q) ||
                st.nis.lowercase().contains(q)
            matchBalance && matchSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Balance Per Foster Student" else "Saldo Tabungan Per Anak",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(12.dp))
                            Text(
                                text = if (isEn) "Tap student card for details, + / - & logs" else "Ketuk nama anak untuk detail pemasukan, pengeluaran & log",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_tabungan_student_list")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close)
                    }
                }

                if (isAnakSuperAdmin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = scopeFilter == 0,
                            onClick = { scopeFilter = 0 },
                            label = { Text(if (isEn) "My Claimed (${claimedStudents.size})" else "Binaan Saya (${claimedStudents.size})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = scopeFilter == 1,
                            onClick = { scopeFilter = 1 },
                            label = { Text(if (isEn) "All School (Super)" else "Semua Sekolah (Super)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDCFCE7),
                                selectedLabelColor = Color(0xFF15803D)
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isEn) "Search student name / class..." else "Cari nama / kelas anak asuh...", fontSize = 12.5.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_tabungan_students")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = balanceFilter == 0,
                        onClick = { balanceFilter = 0 },
                        label = { Text(if (isEn) "All (${baseStudents.size})" else "Semua (${baseStudents.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = balanceFilter == 1,
                        onClick = { balanceFilter = 1 },
                        label = { Text(if (isEn) "Has Balance" else "Ada Saldo", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEF3C7),
                            selectedLabelColor = Color(0xFFB45309)
                        )
                    )
                    FilterChip(
                        selected = balanceFilter == 2,
                        onClick = { balanceFilter = 2 },
                        label = { Text(if (isEn) "Zero Balance" else "Saldo Rp 0", fontSize = 11.sp) }
                    )
                }

                if (baseStudents.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFCD34D))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(28.dp))
                            Text(
                                text = if (isEn) "No Claimed Foster Students" else "Belum Ada Anak Binaan dari Hubungan Ortu",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenPilihAnakAsuh()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(if (isEn) "Select Foster Students" else "Pilih Anak Asuh", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 460.dp)
                            .testTag("list_tabungan_students"),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredStudents, key = { it.id }) { student ->
                            val stSummary = TabunganHelper.computeSummaryForStudent(transactions, student)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectStudent(student) }
                                    .testTag("card_tabungan_list_student_${student.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(
                                    1.dp,
                                    if (stSummary.balance > 0L) Color(0xFFFCD34D) else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
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
                                        Text(
                                            text = "+${TabunganHelper.formatRupiah(stSummary.totalIn)}  |  -${TabunganHelper.formatRupiah(stSummary.totalOut)}",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (stSummary.balance > 0L) Color(0xFFFEF3C7) else Color(0xFFF1F5F9)
                                    ) {
                                        Text(
                                            text = TabunganHelper.formatRupiah(stSummary.balance),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (stSummary.balance > 0L) Color(0xFFB45309) else Color(0xFF64748B),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onQuickDeposit(student) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("btn_quick_deposit_student_${student.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = if (isEn) "Quick Deposit" else "Tambah Titipan",
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(20.dp)
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
