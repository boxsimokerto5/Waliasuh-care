package com.example.ui.dialogs

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.StudentReligionHelper
import com.example.ui.components.StudentAvatar
import com.example.util.i18n.LocalAppStrings

internal data class ChristianActivityVisual(
    val accent: Color,
    val bgLight: Color,
    val bgDark: Color,
    val borderLight: Color,
    val icon: ImageVector
)

internal fun christianActivityVisual(type: String): ChristianActivityVisual {
    return when (ChristianWorshipHelper.normalizeType(type)) {
        ChristianWorshipRecord.TYPE_DOA_PAGI -> ChristianActivityVisual(
            accent = Color(0xFFD97706),
            bgLight = Color(0xFFFEF3C7),
            bgDark = Color(0xFF78350F).copy(alpha = 0.42f),
            borderLight = Color(0xFFFCD34D),
            icon = Icons.Default.WbSunny
        )
        ChristianWorshipRecord.TYPE_DOA_MALAM -> ChristianActivityVisual(
            accent = Color(0xFF4338CA),
            bgLight = Color(0xFFE0E7FF),
            bgDark = Color(0xFF312E81).copy(alpha = 0.45f),
            borderLight = Color(0xFFA5B4FC),
            icon = Icons.Default.NightlightRound
        )
        ChristianWorshipRecord.TYPE_KE_GEREJA -> ChristianActivityVisual(
            accent = Color(0xFF0F766E),
            bgLight = Color(0xFFCCFBF1),
            bgDark = Color(0xFF134E4A).copy(alpha = 0.45f),
            borderLight = Color(0xFF5EEAD4),
            icon = Icons.Default.Church
        )
        ChristianWorshipRecord.TYPE_AYAT_HAFALAN -> ChristianActivityVisual(
            accent = Color(0xFF7E22CE),
            bgLight = Color(0xFFF3E8FF),
            bgDark = Color(0xFF581C87).copy(alpha = 0.45f),
            borderLight = Color(0xFFD8B4FE),
            icon = Icons.AutoMirrored.Filled.MenuBook
        )
        ChristianWorshipRecord.TYPE_POKOK_DOA -> ChristianActivityVisual(
            accent = Color(0xFFBE185D),
            bgLight = Color(0xFFFCE7F3),
            bgDark = Color(0xFF831843).copy(alpha = 0.45f),
            borderLight = Color(0xFFF9A8D4),
            icon = Icons.Default.Favorite
        )
        else -> ChristianActivityVisual(
            accent = Color(0xFF0369A1),
            bgLight = Color(0xFFE0F2FE),
            bgDark = Color(0xFF0C4A6E).copy(alpha = 0.45f),
            borderLight = Color(0xFF7DD3FC),
            icon = Icons.Default.EditNote
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimChristianStudentsSheet(
    students: List<ChildStudent>,
    onClaimStudents: (List<ChildStudent>, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
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
        dragHandle = null,
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
                    .widthIn(max = 600.dp)
                    .testTag("sheet_claim_christian_students")
            ) {
                // Header Gradient Indigo-Violet Senada Hub Agama Kristen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF3730A3), Color(0xFF4F46E5), Color(0xFF7C3AED))
                            )
                        )
                        .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.45f))
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.5.dp, Color(0xFFC7D2FE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_menu_kristen),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) "Claim Christian Students" else "Klaim Anak Agama Kristen",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isEn) {
                                        "$christianCount claimed • Sholat & Mengaji excluded, Fasting kept"
                                    } else {
                                        "$christianCount diklaim • Sholat & Mengaji otomatis dikecualikan"
                                    },
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFE0E7FF),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = if (isEn) "Close" else "Tutup",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // Body Rapat & Warna Indah
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                if (isEn) "Search student name, class..." else "Cari nama siswa, kelas, asrama...",
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = if (isEn) "Clear" else "Hapus",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDark) Color(0xFF1E1B4B).copy(alpha = 0.45f) else Color(0xFFF5F3FF),
                            unfocusedContainerColor = if (isDark) Color(0xFF1E1B4B).copy(alpha = 0.28f) else Color(0xFFFAFAFF),
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = if (isDark) Color(0xFF4338CA) else Color(0xFFC7D2FE)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_claim_christian")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = filterMode == "ALL",
                            onClick = { filterMode = "ALL" },
                            label = { Text(if (isEn) "All (${students.size})" else "Semua (${students.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE0E7FF),
                                selectedLabelColor = Color(0xFF3730A3)
                            )
                        )
                        FilterChip(
                            selected = filterMode == "KRISTEN",
                            onClick = { filterMode = "KRISTEN" },
                            label = { Text(if (isEn) "Christian ($christianCount)" else "Kristen ($christianCount)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFCCFBF1),
                                selectedLabelColor = Color(0xFF0F766E)
                            )
                        )
                        FilterChip(
                            selected = filterMode == "BELUM",
                            onClick = { filterMode = "BELUM" },
                            label = { Text(if (isEn) "Unclaimed (${students.size - christianCount})" else "Belum Klaim (${students.size - christianCount})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEF3C7),
                                selectedLabelColor = Color(0xFFB45309)
                            )
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
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_confirm_claim_christian_batch")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEn) "Claim (${pickedStudents.size})" else "Klaim Kristen (${pickedStudents.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    onClaimStudents(pickedStudents, false)
                                    selectedIds.clear()
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_unclaim_christian_batch")
                            ) {
                                Text(
                                    text = if (isEn) "Remove Tag" else "Lepas Klaim",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 200.dp, max = 410.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(filtered, key = { "claim_chris_${it.id}" }) { student ->
                            val isChristian = StudentReligionHelper.isChristian(student)
                            val isChecked = student.id in selectedIds
                            val rowBg = when {
                                isChristian && isDark -> Color(0xFF312E81).copy(alpha = 0.5f)
                                isChristian -> Color(0xFFEEF2FF)
                                isDark -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                            val rowBorder = when {
                                isChristian && isDark -> Color(0xFF818CF8)
                                isChristian -> Color(0xFFA5B4FC)
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selectedIds.remove(student.id) else selectedIds.add(student.id)
                                    }
                                    .testTag("row_claim_christian_${student.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = rowBg),
                                border = BorderStroke(1.dp, rowBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
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
                                        size = 32.dp,
                                        photoUrl = student.photoUrl,
                                        photoFallbackUrl = student.photoFallbackUrl
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${student.classGrade.ifBlank { "-" }} • ${student.dormitory.ifBlank { "-" }}",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isChristian) Color(0xFF4338CA) else Color(0xFFE2E8F0),
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
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

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

    val activeVisual = christianActivityVisual(selectedType)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .testTag("sheet_input_christian_worship"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 580.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Gradient Slider dari Bawah + Drag Pill
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF3730A3), Color(0xFF4F46E5), Color(0xFF0EA5E9))
                            )
                        )
                        .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.45f))
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.5.dp, Color(0xFFBAE6FD), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_menu_kristen),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) "Log Christian Worship" else "Input Ibadah Agama Kristen",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isEn) {
                                        "Compact form • Auto-sync to Parent Portal"
                                    } else {
                                        "Formulir rapat 1 halaman • Otomatis masuk Portal Ortu"
                                    },
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFE0F2FE),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = if (isEn) "Close" else "Tutup",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // Isi Formulir Rapat & Berwarna Indah
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (christianStudents.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF312E81).copy(alpha = 0.45f) else Color(0xFFEEF2FF)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFA5B4FC))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isEn) {
                                        "No students are marked Christian yet. Claim Christian students first."
                                    } else {
                                        "Belum ada siswa diklaim beragama Kristen. Silakan klaim terlebih dahulu."
                                    },
                                    fontSize = 12.sp,
                                    color = if (isDark) Color(0xFFE0E7FF) else Color(0xFF312E81)
                                )
                                Button(
                                    onClick = onOpenClaimChristian,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_input_open_claim_christian")
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isEn) "Claim Christian Students" else "Klaim Anak Kristen", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Segmented Pill Mode Toggle (Rapat)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF1E1B4B).copy(alpha = 0.55f) else Color(0xFFEEF2FF))
                                .border(1.dp, if (isDark) Color(0xFF4338CA) else Color(0xFFC7D2FE), RoundedCornerShape(12.dp))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val singleSelected = selectedTab == 0
                            Button(
                                onClick = { selectedTab = 0 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (singleSelected) Color(0xFF4338CA) else Color.Transparent,
                                    contentColor = if (singleSelected) Color.White else if (isDark) Color(0xFFC7D2FE) else Color(0xFF3730A3)
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                shape = RoundedCornerShape(9.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 40.dp)
                                    .testTag("tab_christian_input_single")
                            ) {
                                Text(
                                    text = if (isEn) "Single + Photo" else "Satuan + Foto",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            val batchSelected = selectedTab == 1
                            Button(
                                onClick = { selectedTab = 1 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (batchSelected) Color(0xFF0F766E) else Color.Transparent,
                                    contentColor = if (batchSelected) Color.White else if (isDark) Color(0xFFC7D2FE) else Color(0xFF3730A3)
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                shape = RoundedCornerShape(9.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 40.dp)
                                    .testTag("tab_christian_input_batch")
                            ) {
                                Text(
                                    text = if (isEn) "Batch Checklist" else "Checklist Massal",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        // Grid Rapat 2 Baris x 3 Kolom Berwarna Indah untuk 6 Jenis Ibadah
                        Text(
                            text = if (isEn) "Worship Activity Type" else "Pilih Jenis Ibadah / Pembinaan",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeVisual.accent
                        )
                        val activityRows = ChristianWorshipHelper.ALL_TYPES.chunked(3)
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            activityRows.forEach { rowTypes ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    rowTypes.forEach { t ->
                                        val vis = christianActivityVisual(t)
                                        val isPicked = selectedType == t
                                        val tileBg = when {
                                            isPicked -> vis.accent
                                            isDark -> vis.bgDark
                                            else -> vis.bgLight
                                        }
                                        val tileFg = if (isPicked) Color.White else if (isDark) Color.White else vis.accent
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = tileBg,
                                            border = BorderStroke(
                                                if (isPicked) 1.5.dp else 1.dp,
                                                if (isPicked) vis.accent else vis.borderLight
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    selectedType = t
                                                    titleInput = ChristianWorshipHelper.activityLabel(t, isEn)
                                                }
                                                .testTag("chip_christian_type_$t")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = vis.icon,
                                                    contentDescription = null,
                                                    tint = tileFg,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = ChristianWorshipHelper.activityShortLabel(t, isEn),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tileFg,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (selectedTab == 0) {
                            // Pilihan Anak + Lampiran Foto dalam Card Pastel Rapat
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF064E3B).copy(alpha = 0.25f) else Color(0xFFF0FDF4)
                                ),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF10B981).copy(alpha = 0.45f) else Color(0xFF86EFAC))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isEn) "Christian Student" else "Anak Asuh Kristen",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFF6EE7B7) else Color(0xFF15803D)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        christianStudents.forEach { st ->
                                            val isSt = selectedStudent?.id == st.id
                                            FilterChip(
                                                selected = isSt,
                                                onClick = { selectedStudent = st },
                                                label = {
                                                    Text(
                                                        st.name,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSt) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFF15803D),
                                                    selectedLabelColor = Color.White,
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                ),
                                                modifier = Modifier.testTag("chip_christian_student_${st.id}")
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Color(0xFF0EA5E9)),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isDark) Color(0xFF0C4A6E).copy(alpha = 0.4f) else Color(0xFFE0F2FE),
                                                contentColor = if (isDark) Color(0xFF7DD3FC) else Color(0xFF0369A1)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 42.dp)
                                                .testTag("btn_pick_christian_photo")
                                        ) {
                                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (selectedPhotoUri != null) {
                                                    if (isEn) "Change Photo" else "Ganti Foto (90h)"
                                                } else {
                                                    if (isEn) "Attach Photo (90d)" else "Foto Bukti (Simpan 90 Hari)"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (selectedPhotoUri != null) {
                                            IconButton(
                                                onClick = { selectedPhotoUri = null },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = if (isEn) "Remove photo" else "Hapus foto",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    selectedPhotoUri?.let { uri ->
                                        Image(
                                            painter = rememberAsyncImagePainter(uri),
                                            contentDescription = if (isEn) "Selected photo" else "Foto terpilih",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    }
                                }
                            }
                        } else {
                            // Batch Checklist Card Kompak
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF134E4A).copy(alpha = 0.25f) else Color(0xFFF0FDFA)
                                ),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF2DD4BF).copy(alpha = 0.45f) else Color(0xFF99F6E4))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isEn) "Participants (${batchSelectedIds.size}/${christianStudents.size})" else "Peserta (${batchSelectedIds.size}/${christianStudents.size})",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFF5EEAD4) else Color(0xFF0F766E)
                                        )
                                        TextButton(
                                            onClick = {
                                                if (batchSelectedIds.size == christianStudents.size) {
                                                    batchSelectedIds.clear()
                                                } else {
                                                    batchSelectedIds.clear()
                                                    batchSelectedIds.addAll(christianStudents.map { it.id })
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (batchSelectedIds.size == christianStudents.size) {
                                                    if (isEn) "Clear All" else "Batal Semua"
                                                } else {
                                                    if (isEn) "Select All" else "Pilih Semua"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    christianStudents.forEach { st ->
                                        val checked = st.id in batchSelectedIds
                                        Surface(
                                            shape = RoundedCornerShape(9.dp),
                                            color = when {
                                                checked && isDark -> Color(0xFF312E81).copy(alpha = 0.6f)
                                                checked -> Color(0xFFE0E7FF)
                                                else -> MaterialTheme.colorScheme.surface
                                            },
                                            border = BorderStroke(
                                                0.8.dp,
                                                if (checked) Color(0xFF6366F1) else MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (checked) batchSelectedIds.remove(st.id) else batchSelectedIds.add(st.id)
                                                }
                                                .testTag("row_batch_christian_${st.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = checked,
                                                    onCheckedChange = { c ->
                                                        if (c && st.id !in batchSelectedIds) batchSelectedIds.add(st.id)
                                                        else if (!c) batchSelectedIds.remove(st.id)
                                                    },
                                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4338CA))
                                                )
                                                Text(
                                                    text = "${st.name} (${st.classGrade.ifBlank { "-" }})",
                                                    fontSize = 12.sp,
                                                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Formulir Input Rapat dengan Tinted Pastel Colors
                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDark) activeVisual.bgDark else activeVisual.bgLight.copy(alpha = 0.45f),
                            unfocusedContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f) else Color(0xFFFAFAFF),
                            focusedBorderColor = activeVisual.accent,
                            unfocusedBorderColor = if (isDark) activeVisual.accent.copy(alpha = 0.45f) else activeVisual.borderLight,
                            focusedLabelColor = activeVisual.accent
                        )

                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = {
                                Text(
                                    if (isEn) "Title / Bible Verse / Church" else "Judul / Ayat Renungan / Nama Gereja",
                                    fontSize = 11.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    activeVisual.icon,
                                    contentDescription = null,
                                    tint = activeVisual.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_christian_title")
                        )

                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = {
                                Text(
                                    if (isEn) "Caregiver Notes & Reflection" else "Catatan Pembina / Keterangan",
                                    fontSize = 11.5.sp
                                )
                            },
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_christian_notes")
                        )

                        // Tombol Aksi Bawah Rapat (Tutup + Simpan Berwarna Indah)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, activeVisual.accent.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(0.85f)
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(
                                    text = if (isEn) "Close" else "Tutup",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeVisual.accent
                                )
                            }

                            Button(
                                enabled = !isSaving,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = activeVisual.accent,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                onClick = {
                                    val now = System.currentTimeMillis()
                                    if (selectedTab == 0) {
                                        val st = selectedStudent
                                        if (st == null) {
                                            Toast.makeText(
                                                context,
                                                if (isEn) "Select a student" else "Pilih siswa terlebih dahulu",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            onSaveSingle(st, selectedType, now, titleInput, notesInput, selectedPhotoUri)
                                        }
                                    } else {
                                        val list = christianStudents.filter { it.id in batchSelectedIds }
                                        if (list.isEmpty()) {
                                            Toast.makeText(
                                                context,
                                                if (isEn) "Select at least 1 student" else "Pilih minimal 1 siswa",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            onSaveBatch(list, selectedType, now, titleInput, notesInput)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.45f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_save_christian_worship")
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = if (isEn) "Save Log" else "Simpan Ibadah",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
