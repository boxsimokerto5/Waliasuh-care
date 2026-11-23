@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.model.VisitHelper
import com.example.data.model.VisitSession
import com.example.util.i18n.LocalAppStrings
import java.util.Locale

@Composable
fun SiswaPerKecamatanSheet(
    students: List<ChildStudent>,
    visitSessions: List<VisitSession>,
    onStudentClick: (ChildStudent) -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val amberPrimary = Color(0xFFB45309)
    val amberLight = Color(0xFFFEF3C7)
    val tealPrimary = Color(0xFF0F766E)

    val dynamicGroups = remember(students, isEn) {
        VisitHelper.extractDynamicDistricts(students, isEn)
    }

    val activeSessionDistrictsLower = remember(visitSessions) {
        val activeSession = visitSessions.firstOrNull { it.isActive } ?: visitSessions.firstOrNull()
        activeSession?.getDistrictsList()?.map { it.lowercase(Locale.US) }?.toSet() ?: emptySet()
    }

    var selectedDistrictKey by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }

    val visibleGroups = remember(dynamicGroups, selectedDistrictKey, searchQuery) {
        val q = searchQuery.trim().lowercase(Locale.US)
        dynamicGroups
            .filter { selectedDistrictKey == "Semua" || it.normalizedKey.equals(selectedDistrictKey, ignoreCase = true) }
            .mapNotNull { grp ->
                val filteredStudents = if (q.isEmpty()) grp.students else {
                    grp.students.filter { st ->
                        st.name.lowercase(Locale.US).contains(q) ||
                            st.classGrade.lowercase(Locale.US).contains(q) ||
                            st.dormitory.lowercase(Locale.US).contains(q) ||
                            st.nis.lowercase(Locale.US).contains(q) ||
                            grp.displayName.lowercase(Locale.US).contains(q)
                    }
                }
                if (filteredStudents.isEmpty()) null else grp.copy(students = filteredStudents)
            }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(amberLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocationCity, contentDescription = null, tint = amberPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "Students by District (Kecamatan)" else "Daftar Siswa per Kecamatan",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEn) {
                                    "${dynamicGroups.size} districts extracted dynamically from ${students.size} students"
                                } else {
                                    "${dynamicGroups.size} kecamatan muncul otomatis sesuai data ${students.size} siswa"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_siswa_per_kecamatan")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close, modifier = Modifier.size(20.dp))
                    }
                }

                if (dynamicGroups.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (isEn) {
                                "No students in this school yet. Add or import students with District (Kecamatan) data first."
                            } else {
                                "Belum ada data siswa di sekolah ini. Tambahkan atau impor siswa terlebih dahulu."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    // Chip Kecamatan Dinamis
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedDistrictKey == "Semua",
                            onClick = { selectedDistrictKey = "Semua" },
                            label = {
                                Text(
                                    if (isEn) "All Districts (${students.size})" else "Semua (${students.size})",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedDistrictKey == "Semua") FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = amberLight,
                                selectedLabelColor = amberPrimary
                            )
                        )
                        dynamicGroups.forEach { grp ->
                            val isSel = selectedDistrictKey.equals(grp.normalizedKey, ignoreCase = true)
                            val isScheduledNow = grp.normalizedKey.lowercase(Locale.US) in activeSessionDistrictsLower
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedDistrictKey = if (isSel) "Semua" else grp.normalizedKey },
                                label = {
                                    Text(
                                        text = "${grp.displayName} (${grp.students.size})${if (isScheduledNow) " •" else ""}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(13.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (isScheduledNow) Color(0xFFCCFBF1) else amberLight,
                                    selectedLabelColor = if (isScheduledNow) tealPrimary else amberPrimary,
                                    selectedLeadingIconColor = if (isScheduledNow) tealPrimary else amberPrimary
                                ),
                                modifier = Modifier.testTag("chip_kecamatan_filter_${grp.normalizedKey}")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                if (isEn) "Search student name, class, or district..." else "Cari nama siswa, kelas, atau kecamatan...",
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = s.nav.close, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_siswa_kecamatan")
                    )

                    if (visibleGroups.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isEn) "No matching students found." else "Tidak ada siswa yang sesuai pencarian.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .testTag("list_siswa_per_kecamatan"),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            visibleGroups.forEach { grp ->
                                val isScheduledNow = grp.normalizedKey.lowercase(Locale.US) in activeSessionDistrictsLower
                                item(key = "dist_hdr_${grp.normalizedKey}") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp, bottom = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = amberPrimary, modifier = Modifier.size(15.dp))
                                            Text(
                                                text = "${grp.displayName} (${grp.students.size} ${if (isEn) "Students" else "Siswa"})",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = amberPrimary
                                            )
                                        }
                                        if (isScheduledNow) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFCCFBF1)
                                            ) {
                                                Text(
                                                    text = if (isEn) "In Active Session" else "Terjadwal Bulan Ini",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tealPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                itemsIndexed(grp.students, key = { _, st -> "dist_st_${grp.normalizedKey}_${st.id}" }) { idx, st ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onStudentClick(st) }
                                            .testTag("card_student_kecamatan_${st.id}"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${idx + 1}.",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = amberPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(st.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(
                                                    text = "${st.classGrade.ifBlank { "-" }} • ${st.dormitory.ifBlank { "-" }} • Wali: ${st.motherName.ifBlank { "-" }}",
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Icon(
                                                Icons.Default.ChevronRight,
                                                contentDescription = if (isEn) "Open Student Detail" else "Lihat Profil Siswa",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
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
