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
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.ParentAccessLink
import com.example.util.StudentClassifier
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PilihanAksesOrangTuaSheet(
    students: List<ChildStudent>,
    parentAccessLinks: List<ParentAccessLink>,
    isStudentClaimedByCurrentWali: (ChildStudent) -> Boolean,
    isAdmin: Boolean,
    onOpenPilihAnakAsuh: (() -> Unit)? = null,
    onEnsureAccessLink: (ChildStudent) -> Unit,
    onUpdatePermissions: (ParentAccessLink, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    val claimedStudents = students.filter { isStudentClaimedByCurrentWali(it) }
    var searchQuery by remember { mutableStateOf("") }
    // Untuk Wali non-Admin: selalu fokus ke Anak Asuh Saya (0). Admin bisa memilih Semua Siswa (1) atau Anak Asuh Saya (0).
    var selectedScope by remember(isAdmin) {
        mutableStateOf(if (!isAdmin) 0 else 1)
    }

    val linkByStudentId = remember(parentAccessLinks) {
        parentAccessLinks.associateBy { it.studentId }
    }

    val effectiveScope = if (!isAdmin) 0 else selectedScope
    val baseScopeStudents = if (effectiveScope == 0) claimedStudents else students

    val filteredStudents = remember(baseScopeStudents, searchQuery, linkByStudentId) {
        val q = searchQuery.trim().lowercase()
        baseScopeStudents.filter { st ->
            q.isBlank() ||
                st.name.lowercase().contains(q) ||
                st.classGrade.lowercase().contains(q) ||
                st.dormitory.lowercase().contains(q)
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
                                    "Choose modules & profile parents can view"
                                } else {
                                    "Centang profil, tabungan & riwayat yang boleh dilihat orang tua"
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

                if (baseScopeStudents.isNotEmpty() || isAdmin) {
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
                                    Icon(Icons.Default.Close, contentDescription = if (isEn) "Clear" else "Bersihkan", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_pilihan_akses")
                    )
                }

                if (isAdmin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedScope == 0,
                            onClick = { selectedScope = 0 },
                            label = { Text(if (isEn) "My Foster (${claimedStudents.size})" else "Anak Asuh Saya (${claimedStudents.size})", fontSize = 11.5.sp) }
                        )
                        FilterChip(
                            selected = selectedScope == 1,
                            onClick = { selectedScope = 1 },
                            label = { Text(if (isEn) "All Students (${students.size})" else "Semua Siswa (${students.size})", fontSize = 11.5.sp) }
                        )
                    }
                } else if (claimedStudents.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Text(
                            text = if (isEn) {
                                "My Selected Foster Kids: ${claimedStudents.size}"
                            } else {
                                "Anak Asuh Terpilih: ${claimedStudents.size} siswa"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 200.dp, max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (effectiveScope == 0 && claimedStudents.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .testTag("empty_pilihan_akses_no_claimed"),
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
                                            .background(Color(0xFFDCFCE7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Text(
                                        text = if (isEn) "No Foster Students Selected Yet" else "Belum Ada Anak Asuh Dipilih",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Select your foster students in Select Foster Kids first to configure their parent access permissions."
                                        } else {
                                            "Pilih siswa binaan terlebih dahulu di menu Pilih Anak Asuh agar dapat mengatur izin akses orang tua."
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    if (onOpenPilihAnakAsuh != null) {
                                        Button(
                                            onClick = onOpenPilihAnakAsuh,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_open_pilih_anak_asuh_from_perm")
                                        ) {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
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
                                    .padding(vertical = 28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isEn) "No students to configure." else "Belum ada siswa yang cocok.",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredStudents, key = { "perm_${it.id}" }) { student ->
                            val link = linkByStudentId[student.id]
                            val isFemale = StudentClassifier.isFemaleStudent(student)
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
                                        val isChristian = com.example.data.model.StudentReligionHelper.isChristian(student, link)
                                        PermissionCheckboxRow(
                                            label = if (isEn) "Child Profile & Biodata" else "Profil & Biodata Anak",
                                            checked = link.canViewProfile,
                                            accentColor = Color(0xFF4338CA),
                                            onCheckedChange = { checked ->
                                                onUpdatePermissions(link, link.canViewMengaji, link.canViewMedical, link.canViewFasting, link.canViewHaid, checked, link.canViewTabungan, link.canViewSholat)
                                            },
                                            testTag = "chk_access_profile_${student.id}"
                                        )
                                        if (isChristian) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFEEF2FF),
                                                border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("info_christian_auto_portal_${student.id}")
                                            ) {
                                                Text(
                                                    text = if (isEn) {
                                                        "✝️ Christian Student: Christian Worship menu & reports are auto-enabled in Parent Portal (5 Prayers & Mengaji are hidden)."
                                                    } else {
                                                        "✝️ Anak Agama Kristen: Menu Ibadah Kristen & laporannya aktif otomatis di Portal Orang Tua (Sholat & Mengaji disembunyikan)."
                                                    },
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFF3730A3),
                                                    fontWeight = FontWeight.Medium,
                                                    lineHeight = 16.sp,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                                )
                                            }
                                        } else {
                                            PermissionCheckboxRow(
                                                label = if (isEn) "5 Daily Prayers Attendance (Fajr - Isha)" else "Pantauan Sholat 5 Waktu (Subuh - Isya)",
                                                checked = link.canViewSholat,
                                                accentColor = Color(0xFF059669),
                                                onCheckedChange = { checked ->
                                                    onUpdatePermissions(link, link.canViewMengaji, link.canViewMedical, link.canViewFasting, link.canViewHaid, link.canViewProfile, link.canViewTabungan, checked)
                                                },
                                                testTag = "chk_access_sholat_${student.id}"
                                            )
                                            PermissionCheckboxRow(
                                                label = if (isEn) "Mengaji History (Progress & Fluency)" else "Riwayat Mengaji (Jilid & Kelancaran)",
                                                checked = link.canViewMengaji,
                                                accentColor = Color(0xFF047857),
                                                onCheckedChange = { checked ->
                                                    onUpdatePermissions(link, checked, link.canViewMedical, link.canViewFasting, link.canViewHaid, link.canViewProfile, link.canViewTabungan, link.canViewSholat)
                                                },
                                                testTag = "chk_access_mengaji_${student.id}"
                                            )
                                        }
                                        PermissionCheckboxRow(
                                            label = if (isEn) "Medical History (Illness & Treatment)" else "Riwayat Sakit & Rekam Medis",
                                            checked = link.canViewMedical,
                                            accentColor = Color(0xFF0284C7),
                                            onCheckedChange = { checked ->
                                                onUpdatePermissions(link, link.canViewMengaji, checked, link.canViewFasting, link.canViewHaid, link.canViewProfile, link.canViewTabungan, link.canViewSholat)
                                            },
                                            testTag = "chk_access_medical_${student.id}"
                                        )
                                        PermissionCheckboxRow(
                                            label = if (isEn) "Fasting History (Sahur & Iftar)" else "Riwayat Puasa (Sahur & Berbuka)",
                                            checked = link.canViewFasting,
                                            accentColor = Color(0xFF0F766E),
                                            onCheckedChange = { checked ->
                                                onUpdatePermissions(link, link.canViewMengaji, link.canViewMedical, checked, link.canViewHaid, link.canViewProfile, link.canViewTabungan, link.canViewSholat)
                                            },
                                            testTag = "chk_access_fasting_${student.id}"
                                        )
                                        if (isFemale) {
                                            PermissionCheckboxRow(
                                                label = if (isEn) "Haid History (Cycle & Suci)" else "Riwayat Haid (Siklus & Masa Suci)",
                                                checked = link.canViewHaid,
                                                accentColor = Color(0xFFDB2777),
                                                onCheckedChange = { checked ->
                                                    onUpdatePermissions(link, link.canViewMengaji, link.canViewMedical, link.canViewFasting, checked, link.canViewProfile, link.canViewTabungan, link.canViewSholat)
                                                },
                                                testTag = "chk_access_haid_${student.id}"
                                            )
                                        }
                                        PermissionCheckboxRow(
                                            label = if (isEn) "Child Savings Ledger (Balance & History)" else "Tabungan Anak (Saldo & Mutasi Titipan)",
                                            checked = link.canViewTabungan,
                                            accentColor = Color(0xFFD97706),
                                            onCheckedChange = { checked ->
                                                onUpdatePermissions(link, link.canViewMengaji, link.canViewMedical, link.canViewFasting, link.canViewHaid, link.canViewProfile, checked, link.canViewSholat)
                                            },
                                            testTag = "chk_access_tabungan_${student.id}"
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
