package com.example.ui.dialogs

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.StudentReligionHelper
import com.example.ui.components.StudentAvatar
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimChristianStudentsSheet(
    students: List<ChildStudent>,
    onClaimStudents: (List<ChildStudent>, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf("ALL") } // ALL | KRISTEN | BELUM
    val selectedIds = remember { mutableStateListOf<Long>() }

    val christianCount = remember(students) { students.count { StudentReligionHelper.isChristian(it) } }
    val filtered = remember(students, searchQuery, filterMode) {
        val q = searchQuery.trim().lowercase()
        students.filter { st ->
            val isChris = StudentReligionHelper.isChristian(st)
            val matchSearch = q.isBlank() ||
                st.name.lowercase().contains(q) ||
                st.classGrade.lowercase().contains(q) ||
                st.dormitory.lowercase().contains(q)
            val matchFilter = when (filterMode) {
                "KRISTEN" -> isChris
                "BELUM" -> !isChris
                else -> true
            }
            matchSearch && matchFilter
        }.sortedWith(
            compareByDescending<ChildStudent> { StudentReligionHelper.isChristian(it) }
                .thenBy { it.name.lowercase() }
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
                    .padding(bottom = 16.dp)
                    .testTag("sheet_claim_christian_students"),
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
                                .background(Color(0xFFE0E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Church, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "Claim Christian Students" else "Klaim Anak Agama Kristen",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEn) {
                                    "$christianCount claimed • Excluded from Sholat & Mengaji, Puasa kept"
                                } else {
                                    "$christianCount diklaim • Dikecualikan dari Sholat & Mengaji, Puasa tetap aktif"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
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
                    placeholder = { Text(if (isEn) "Search student name, class..." else "Cari nama siswa, kelas, asrama...", fontSize = 12.5.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = if (isEn) "Clear" else "Hapus", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_claim_christian")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterMode == "ALL",
                        onClick = { filterMode = "ALL" },
                        label = { Text(if (isEn) "All (${students.size})" else "Semua (${students.size})", fontSize = 11.5.sp) }
                    )
                    FilterChip(
                        selected = filterMode == "KRISTEN",
                        onClick = { filterMode = "KRISTEN" },
                        label = { Text(if (isEn) "Christian ($christianCount)" else "Kristen ($christianCount)", fontSize = 11.5.sp) }
                    )
                    FilterChip(
                        selected = filterMode == "BELUM",
                        onClick = { filterMode = "BELUM" },
                        label = { Text(if (isEn) "Unclaimed (${students.size - christianCount})" else "Belum Klaim (${students.size - christianCount})", fontSize = 11.5.sp) }
                    )
                }

                if (selectedIds.isNotEmpty()) {
                    val pickedStudents = students.filter { it.id in selectedIds }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onClaimStudents(pickedStudents, true)
                                selectedIds.clear()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_confirm_claim_christian_batch")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEn) "Claim (${pickedStudents.size})" else "Klaim Kristen (${pickedStudents.size})", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                onClaimStudents(pickedStudents, false)
                                selectedIds.clear()
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_unclaim_christian_batch")
                        ) {
                            Text(
                                text = if (isEn) "Remove Tag" else "Lepas Klaim",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 200.dp, max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { "claim_chris_${it.id}" }) { student ->
                        val isChristian = StudentReligionHelper.isChristian(student)
                        val isChecked = student.id in selectedIds
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedIds.remove(student.id) else selectedIds.add(student.id)
                                }
                                .testTag("row_claim_christian_${student.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChristian) Color(0xFFEEF2FF) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isChristian) Color(0xFF818CF8) else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            if (student.id !in selectedIds) selectedIds.add(student.id)
                                        } else {
                                            selectedIds.remove(student.id)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4338CA)),
                                    modifier = Modifier.testTag("chk_claim_christian_${student.id}")
                                )
                                StudentAvatar(
                                    name = student.name,
                                    gender = student.gender,
                                    size = 34.dp,
                                    photoUrl = student.photoUrl,
                                    photoFallbackUrl = student.photoFallbackUrl
                                )
                                Spacer(modifier = Modifier.width(8.dp))
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
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isChristian) Color(0xFF4338CA) else Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .clickable { onClaimStudents(listOf(student), !isChristian) }
                                        .testTag("btn_toggle_christian_${student.id}")
                                ) {
                                    Text(
                                        text = if (isChristian) {
                                            if (isEn) "Christian ✓" else "Kristen ✓"
                                        } else {
                                            if (isEn) "+ Claim" else "+ Klaim"
                                        },
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChristian) Color.White else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InputChristianWorshipDialog(
    christianStudents: List<ChildStudent>,
    initialTab: Int = 0,
    defaultActivityType: String = ChristianWorshipRecord.TYPE_DOA_PAGI,
    targetStudent: ChildStudent? = null,
    isSaving: Boolean = false,
    onOpenClaimChristian: () -> Unit,
    onSaveSingle: (student: ChildStudent, activityType: String, activityDate: Long, title: String, notes: String, imageUri: Uri?) -> Unit,
    onSaveBatch: (students: List<ChildStudent>, activityType: String, activityDate: Long, title: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var selectedType by remember(defaultActivityType) {
        mutableStateOf(ChristianWorshipHelper.normalizeType(defaultActivityType))
    }
    var selectedStudent by remember(targetStudent, christianStudents) {
        mutableStateOf(targetStudent ?: christianStudents.firstOrNull())
    }
    val batchSelectedIds = remember(christianStudents) {
        mutableStateListOf<Long>().apply {
            if (targetStudent != null && StudentReligionHelper.isChristian(targetStudent)) {
                add(targetStudent.id)
            }
        }
    }
    var titleInput by remember(selectedType) {
        mutableStateOf(ChristianWorshipHelper.activityLabel(selectedType, isEn))
    }
    var notesInput by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) selectedPhotoUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Church, contentDescription = null, tint = Color(0xFF4338CA))
                Text(
                    text = if (isEn) "Log Christian Worship" else "Input Ibadah Agama Kristen",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (christianStudents.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                        border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isEn) {
                                    "No students are marked Christian yet. Please claim Christian students first."
                                } else {
                                    "Belum ada siswa diklaim beragama Kristen. Silakan klaim terlebih dahulu."
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF312E81)
                            )
                            Button(
                                onClick = onOpenClaimChristian,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                                modifier = Modifier.fillMaxWidth().testTag("btn_input_open_claim_christian")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEn) "Claim Christian Students" else "Klaim Anak Kristen")
                            }
                        }
                    }
                } else {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text(if (isEn) "Single + Photo" else "Satuan + Foto", fontSize = 12.sp) },
                            modifier = Modifier.testTag("tab_christian_input_single")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(if (isEn) "Batch Checklist" else "Checklist Massal", fontSize = 12.sp) },
                            modifier = Modifier.testTag("tab_christian_input_batch")
                        )
                    }

                    Text(
                        text = if (isEn) "Activity Type" else "Jenis Kegiatan Ibadah",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ChristianWorshipHelper.ALL_TYPES.forEach { t ->
                            FilterChip(
                                selected = selectedType == t,
                                onClick = {
                                    selectedType = t
                                    titleInput = ChristianWorshipHelper.activityLabel(t, isEn)
                                },
                                label = { Text(ChristianWorshipHelper.activityShortLabel(t, isEn), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE0E7FF),
                                    selectedLabelColor = Color(0xFF3730A3)
                                ),
                                modifier = Modifier.testTag("chip_christian_type_$t")
                            )
                        }
                    }

                    if (selectedTab == 0) {
                        Text(
                            text = if (isEn) "Select Christian Student" else "Pilih Anak Asuh Kristen",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            christianStudents.forEach { st ->
                                FilterChip(
                                    selected = selectedStudent?.id == st.id,
                                    onClick = { selectedStudent = st },
                                    label = { Text(st.name, fontSize = 11.5.sp) },
                                    modifier = Modifier.testTag("chip_christian_student_${st.id}")
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_pick_christian_photo")
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedPhotoUri != null) {
                                    if (isEn) "Change Activity Photo" else "Ganti Foto Kegiatan"
                                } else {
                                    if (isEn) "Attach Photo (90-day retention)" else "Lampirkan Foto Bukti (Simpan 90 Hari)"
                                },
                                fontSize = 12.sp
                            )
                        }

                        selectedPhotoUri?.let { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = if (isEn) "Selected photo" else "Foto terpilih",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "Select Students (${batchSelectedIds.size}/${christianStudents.size})" else "Centang Peserta (${batchSelectedIds.size}/${christianStudents.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = {
                                    if (batchSelectedIds.size == christianStudents.size) {
                                        batchSelectedIds.clear()
                                    } else {
                                        batchSelectedIds.clear()
                                        batchSelectedIds.addAll(christianStudents.map { it.id })
                                    }
                                }
                            ) {
                                Text(
                                    text = if (batchSelectedIds.size == christianStudents.size) {
                                        if (isEn) "Clear All" else "Batal Semua"
                                    } else {
                                        if (isEn) "Select All" else "Pilih Semua"
                                    },
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            christianStudents.forEach { st ->
                                val checked = st.id in batchSelectedIds
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (checked) Color(0xFFEEF2FF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (checked) batchSelectedIds.remove(st.id) else batchSelectedIds.add(st.id)
                                        }
                                        .testTag("row_batch_christian_${st.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = { c ->
                                                if (c && st.id !in batchSelectedIds) batchSelectedIds.add(st.id)
                                                else if (!c) batchSelectedIds.remove(st.id)
                                            }
                                        )
                                        Text("${st.name} (${st.classGrade.ifBlank { "-" }})", fontSize = 12.5.sp)
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = {
                            Text(
                                if (isEn) "Title / Bible Verse / Church" else "Judul / Ayat Renungan / Nama Gereja",
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_christian_title")
                    )

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text(if (isEn) "Caregiver Notes & Reflection" else "Catatan Pembina / Keterangan", fontSize = 12.sp) },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_christian_notes")
                    )
                }
            }
        },
        confirmButton = {
            if (christianStudents.isNotEmpty()) {
                Button(
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                    onClick = {
                        val now = System.currentTimeMillis()
                        if (selectedTab == 0) {
                            val st = selectedStudent
                            if (st == null) {
                                Toast.makeText(context, if (isEn) "Select a student" else "Pilih siswa terlebih dahulu", Toast.LENGTH_SHORT).show()
                            } else {
                                onSaveSingle(st, selectedType, now, titleInput, notesInput, selectedPhotoUri)
                            }
                        } else {
                            val list = christianStudents.filter { it.id in batchSelectedIds }
                            if (list.isEmpty()) {
                                Toast.makeText(context, if (isEn) "Select at least 1 student" else "Pilih minimal 1 siswa", Toast.LENGTH_SHORT).show()
                            } else {
                                onSaveBatch(list, selectedType, now, titleInput, notesInput)
                            }
                        }
                    },
                    modifier = Modifier.testTag("btn_save_christian_worship")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(if (isEn) "Save Log" else "Simpan Ibadah")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isEn) "Close" else "Tutup")
            }
        }
    )
}
