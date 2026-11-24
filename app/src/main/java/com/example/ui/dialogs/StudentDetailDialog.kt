package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.FastingRecord
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.SchoolProfile
import com.example.data.model.SholatDisciplineTag
import com.example.data.model.SholatHelper
import com.example.data.model.SholatRecord
import com.example.data.model.SholatStatus
import com.example.data.model.SholatWaktu
import com.example.data.model.StudentReligionHelper
import com.example.ui.components.ControlBadge
import com.example.ui.components.FaskesBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.components.StudentLargeProfilePhoto
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import com.example.util.StudentClassifier
import com.example.util.StudentCsvParser
import com.example.util.i18n.LocalAppStrings
import java.util.Calendar

enum class StudentDetailTab {
    BIODATA,
    MENGAJI,
    IBADAH_KRISTEN,
    SHOLAT,
    BEROBAT,
    PUASA
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudentDetailDialog(
    student: ChildStudent,
    studentRecords: List<MedicalRecord>,
    schoolProfile: SchoolProfile = SchoolProfile(),
    waliName: String = "Wali Asuh",
    mengajiRecords: List<MengajiRecord> = emptyList(),
    christianWorshipRecords: List<ChristianWorshipRecord> = emptyList(),
    fastingRecords: List<FastingRecord> = emptyList(),
    sholatRecords: List<SholatRecord> = emptyList(),
    haidRecords: List<HaidRecord> = emptyList(),
    initialTab: StudentDetailTab = StudentDetailTab.BIODATA,
    allowedTabs: List<StudentDetailTab> = StudentDetailTab.entries,
    allowCardQr: Boolean = true,
    isPetugasKesehatan: Boolean = false,
    onInputMengaji: ((ChildStudent) -> Unit)? = null,
    onInputChristianWorship: ((ChildStudent) -> Unit)? = null,
    onInputSholat: ((ChildStudent) -> Unit)? = null,
    onPrintKartuMengaji: ((ChildStudent) -> Unit)? = null,
    onEditStudent: ((ChildStudent) -> Unit)? = null,
    onDeleteMengajiRecord: ((MengajiRecord) -> Unit)? = null,
    onDeleteChristianWorshipRecord: ((ChristianWorshipRecord) -> Unit)? = null,
    onDeleteMedicalRecord: ((MedicalRecord) -> Unit)? = null,
    onDeleteFastingRecord: ((FastingRecord) -> Unit)? = null,
    onDeleteSholatRecord: ((SholatRecord) -> Unit)? = null,
    isUploadingPhoto: Boolean = false,
    onUploadStudentPhoto: ((ChildStudent, Uri, (Boolean, String) -> Unit) -> Unit)? = null,
    onRemoveStudentPhoto: ((ChildStudent, (Boolean, String) -> Unit) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val isChristianStudent = remember(student) { StudentReligionHelper.isChristian(student) }
    val effectiveAllowedTabs = remember(allowedTabs, isChristianStudent) {
        val filtered = if (isChristianStudent) {
            allowedTabs.filter { tab ->
                tab != StudentDetailTab.MENGAJI && tab != StudentDetailTab.SHOLAT
            }
        } else {
            allowedTabs.filter { tab ->
                tab != StudentDetailTab.IBADAH_KRISTEN
            }
        }
        filtered.ifEmpty { listOf(StudentDetailTab.BIODATA) }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by rememberSaveable(student.id) {
        mutableStateOf(
            if (initialTab in effectiveAllowedTabs) initialTab else effectiveAllowedTabs.first()
        )
    }
    LaunchedEffect(effectiveAllowedTabs) {
        if (selectedTab !in effectiveAllowedTabs) {
            selectedTab = effectiveAllowedTabs.first()
        }
    }
    val contentScrollState = rememberScrollState()
    LaunchedEffect(selectedTab) {
        contentScrollState.scrollTo(0)
    }
    var showPrintPdfDialog by remember { mutableStateOf(false) }
    var showCardGeneratorForStudent by remember { mutableStateOf(false) }
    var showInlineKartuMengaji by remember { mutableStateOf(false) }
    var showAllMengajiHistory by remember { mutableStateOf(false) }
    var showPhotoManager by remember { mutableStateOf(false) }
    var showPhotoPreview by remember { mutableStateOf(false) }
    var previewChristianWorship by remember { mutableStateOf<ChristianWorshipRecord?>(null) }
    var pendingDeleteMengaji by remember { mutableStateOf<MengajiRecord?>(null) }
    var pendingDeleteChristianWorship by remember { mutableStateOf<ChristianWorshipRecord?>(null) }
    var pendingDeleteMedical by remember { mutableStateOf<MedicalRecord?>(null) }
    var pendingDeleteFasting by remember { mutableStateOf<FastingRecord?>(null) }
    var pendingDeleteSholat by remember { mutableStateOf<SholatRecord?>(null) }
    val orderedMengajiRecords = remember(mengajiRecords, student) {
        MengajiHelper.recordsForStudent(mengajiRecords, student)
    }
    val orderedChristianRecords = remember(christianWorshipRecords, student) {
        ChristianWorshipHelper.recordsForStudent(christianWorshipRecords, student)
    }
    val orderedSholatRecords = remember(sholatRecords, student) {
        SholatHelper.recordsForStudent(sholatRecords, student)
    }
    val currentSholatMonthKey = remember { SholatHelper.todayDateKey().substring(0, 7) }
    val sholatAnalytics = remember(student, orderedSholatRecords, currentSholatMonthKey) {
        SholatHelper.computeStudentAnalytics(
            students = listOf(student),
            records = orderedSholatRecords,
            monthKey = currentSholatMonthKey
        ).first()
    }
    val orderedFastingRecords = remember(fastingRecords, student) {
        val cleanStudentNik = StudentCsvParser.cleanDigitsOnly(student.nik)
        fastingRecords
            .filter { rec ->
                val sameSchool = rec.schoolId.isBlank() || student.schoolId.isBlank() ||
                    rec.schoolId.equals(student.schoolId, ignoreCase = true)
                val recCleanNik = StudentCsvParser.cleanDigitsOnly(rec.studentNik)
                sameSchool && (
                    (student.id > 0L && rec.studentId == student.id) ||
                        (cleanStudentNik.isNotBlank() && recCleanNik == cleanStudentNik) ||
                        (student.nis.isNotBlank() && rec.studentNis.equals(student.nis, ignoreCase = true)) ||
                        (rec.studentName.equals(student.name, ignoreCase = true))
                    )
            }
            .sortedWith(
                compareByDescending<FastingRecord> { it.sessionDate.takeIf { d -> d > 0L } ?: it.createdAt }
                    .thenByDescending { it.updatedAt }
            )
    }

    val calNow = Calendar.getInstance()
    val curMonth = calNow.get(Calendar.MONTH)
    val curYear = calNow.get(Calendar.YEAR)
    val calItem = Calendar.getInstance()
    val thisMonthCount = studentRecords.count { record ->
        val time = record.treatmentDate ?: record.planDate
        calItem.timeInMillis = time
        calItem.get(Calendar.MONTH) == curMonth && calItem.get(Calendar.YEAR) == curYear
    }

    val isMale = StudentClassifier.isMaleStudent(student)
    val resolvedJenjang = student.jenjang.ifBlank {
        StudentCsvParser.deriveJenjang(student.classGrade)
    }
    val cleanNik = remember(student.nik) { StudentCsvParser.cleanDigitsOnly(student.nik) }
    val cleanNoKk = remember(student.noKk) { StudentCsvParser.cleanDigitsOnly(student.noKk) }

    val emeraldPrimary = Color(0xFF047857)
    val christianIndigo = Color(0xFF4338CA)
    val sholatTeal = Color(0xFF0F766E)
    val medicalSky = Color(0xFF0369A1)
    val fastingAmber = Color(0xFFB45309)

    fun copyValue(label: String, value: String) {
        if (value.isBlank() || value == "-") return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        Toast.makeText(
            context,
            if (isEn) "$label copied: $value" else "$label disalin: $value",
            Toast.LENGTH_SHORT
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
                    .widthIn(max = 620.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .testTag("student_detail_sheet"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val hasPhoto = student.photoUrl.isNotBlank() || student.photoFallbackUrl.isNotBlank()
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .let { mod ->
                                    when {
                                        onUploadStudentPhoto != null -> mod.clickable { showPhotoManager = true }
                                        hasPhoto -> mod.clickable { showPhotoPreview = true }
                                        else -> mod
                                    }
                                }
                                .testTag("btn_student_photo_action"),
                            contentAlignment = Alignment.Center
                        ) {
                            StudentAvatar(
                                name = student.name,
                                gender = student.gender,
                                size = 54.dp,
                                photoUrl = student.photoUrl,
                                photoFallbackUrl = student.photoFallbackUrl
                            )
                            if (onUploadStudentPhoto != null) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .border(1.5.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = if (isEn) "Manage Student Photo" else "Kelola Foto Siswa",
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = student.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                StudentChipBadge(
                                    text = if (isMale) "Putra (L)" else "Putri (P)",
                                    bgColor = if (isMale) Color(0xFFDBEAFE) else Color(0xFFFCE7F3),
                                    textColor = if (isMale) Color(0xFF1E40AF) else Color(0xFF9D174D)
                                )
                                if (resolvedJenjang.isNotBlank()) {
                                    StudentChipBadge(
                                        text = resolvedJenjang,
                                        bgColor = Color(0xFFDCFCE7),
                                        textColor = Color(0xFF166534)
                                    )
                                }
                                if (student.classGrade.isNotBlank()) {
                                    StudentChipBadge(
                                        text = student.classGrade,
                                        bgColor = MaterialTheme.colorScheme.secondaryContainer,
                                        textColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                if (isChristianStudent) {
                                    Box(modifier = Modifier.testTag("tag_student_detail_christian_badge")) {
                                        StudentChipBadge(
                                            text = if (isEn) "✝ Christian" else "✝ Agama Kristen",
                                            bgColor = Color(0xFFE0E7FF),
                                            textColor = Color(0xFF3730A3)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "NIS / NISN: ${student.nis.ifBlank { "-" }} • ${if (isEn) "School ID" else "ID Sekolah"}: ${student.schoolId}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onEditStudent != null) {
                            IconButton(
                                onClick = { onEditStudent(student) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_edit_student_detail")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = if (isEn) "Edit Student Profile" else "Edit Profil Siswa",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_close_student_detail")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = s.nav.close
                            )
                        }
                    }
                }

                // Peringatan frekuensi sakit / alergi
                if (thisMonthCount >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEE2E2))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isEn) {
                                "⚠️ Alert: This student has been sick $thisMonthCount times this month"
                            } else {
                                "⚠️ Perhatian: Anak ini $thisMonthCount kali sakit bulan ini"
                            },
                            fontSize = 11.5.sp,
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (thisMonthCount == 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0F2FE))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isEn) "🩺 1 medical visit this month" else "🩺 1 kali berobat bulan ini",
                            fontSize = 11.5.sp,
                            color = Color(0xFF0369A1),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (student.allergies.isNotBlank() && !student.allergies.equals("Tidak ada", ignoreCase = true) && student.allergies != "-") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Allergies: ${student.allergies}" else "Alergi: ${student.allergies}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Slim Horizontal Category Pill Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(4.dp)
                        .horizontalScroll(rememberScrollState())
                        .testTag("student_detail_tab_row"),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (StudentDetailTab.BIODATA in effectiveAllowedTabs) {
                        SlimCategoryTabPill(
                            label = if (isEn) "Profile" else "Biodata",
                            icon = Icons.Default.Badge,
                            selected = selectedTab == StudentDetailTab.BIODATA,
                            accentColor = MaterialTheme.colorScheme.primary,
                            countBadge = null,
                            modifier = Modifier.testTag("tab_student_detail_biodata"),
                            onClick = { selectedTab = StudentDetailTab.BIODATA }
                        )
                    }
                    if (StudentDetailTab.IBADAH_KRISTEN in effectiveAllowedTabs) {
                        SlimCategoryTabPill(
                            label = if (isEn) "Christian" else "Ibadah Kristen",
                            icon = Icons.Default.Church,
                            selected = selectedTab == StudentDetailTab.IBADAH_KRISTEN,
                            accentColor = christianIndigo,
                            countBadge = orderedChristianRecords.size,
                            modifier = Modifier.testTag("tab_student_detail_kristen"),
                            onClick = { selectedTab = StudentDetailTab.IBADAH_KRISTEN }
                        )
                    }
                    if (StudentDetailTab.MENGAJI in effectiveAllowedTabs) {
                        SlimCategoryTabPill(
                            label = "Mengaji",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            selected = selectedTab == StudentDetailTab.MENGAJI,
                            accentColor = emeraldPrimary,
                            countBadge = orderedMengajiRecords.size,
                            modifier = Modifier.testTag("tab_student_detail_mengaji"),
                            onClick = { selectedTab = StudentDetailTab.MENGAJI }
                        )
                    }
                    if (StudentDetailTab.SHOLAT in effectiveAllowedTabs) {
                        SlimCategoryTabPill(
                            label = if (isEn) "Prayers" else "Sholat",
                            icon = Icons.Default.AutoAwesome,
                            selected = selectedTab == StudentDetailTab.SHOLAT,
                            accentColor = sholatTeal,
                            countBadge = orderedSholatRecords.size,
                            modifier = Modifier.testTag("tab_student_detail_sholat"),
                            onClick = { selectedTab = StudentDetailTab.SHOLAT }
                        )
                    }
                    if (StudentDetailTab.BEROBAT in effectiveAllowedTabs) {
                        SlimCategoryTabPill(
                            label = if (isEn) "Medical" else "Berobat",
                            icon = Icons.Default.LocalHospital,
                            selected = selectedTab == StudentDetailTab.BEROBAT,
                            accentColor = medicalSky,
                            countBadge = studentRecords.size,
                            modifier = Modifier.testTag("tab_student_detail_berobat"),
                            onClick = { selectedTab = StudentDetailTab.BEROBAT }
                        )
                    }
                    if (StudentDetailTab.PUASA in effectiveAllowedTabs) {
                        SlimCategoryTabPill(
                            label = if (isEn) "Fasting" else "Puasa",
                            icon = Icons.Default.NightsStay,
                            selected = selectedTab == StudentDetailTab.PUASA,
                            accentColor = fastingAmber,
                            countBadge = orderedFastingRecords.size,
                            modifier = Modifier.testTag("tab_student_detail_puasa"),
                            onClick = { selectedTab = StudentDetailTab.PUASA }
                        )
                    }
                }

                // Konten Tab yang dapat di-scroll independen dari bar kategori atas
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(contentScrollState)
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                // Isi Tab Kategori Aktif
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(160)) togetherWith fadeOut(animationSpec = tween(120))
                    },
                    label = "student_detail_tab_content"
                ) { currentTab ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (currentTab) {
                            StudentDetailTab.BIODATA -> {
                                // Kartu Foto Profil Besar Siswa (antara blok kategori Biodata & Identitas Kependudukan)
                                val hasDetailPhoto = student.photoUrl.isNotBlank() || student.photoFallbackUrl.isNotBlank()
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .testTag("card_student_detail_large_photo")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddAPhoto,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = if (isEn) "Student Profile Photo" else "Foto Profil Siswa",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            if (onUploadStudentPhoto != null) {
                                                Surface(
                                                    onClick = { showPhotoManager = true },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                                    ),
                                                    modifier = Modifier.testTag("btn_student_large_photo_manage")
                                                ) {
                                                    Text(
                                                        text = if (hasDetailPhoto) {
                                                            if (isEn) "Change Photo" else "Ganti Foto"
                                                        } else {
                                                            if (isEn) "Upload Photo" else "Unggah Foto"
                                                        },
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            StudentLargeProfilePhoto(
                                                name = student.name,
                                                gender = student.gender,
                                                photoUrl = student.photoUrl,
                                                photoFallbackUrl = student.photoFallbackUrl,
                                                emptyHint = if (isEn) {
                                                    "No student profile photo uploaded yet"
                                                } else {
                                                    "Belum ada foto profil siswa"
                                                },
                                                showZoomBadge = hasDetailPhoto,
                                                contentScale = ContentScale.Fit,
                                                shape = RoundedCornerShape(14.dp),
                                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                                placeholderAvatarSize = 96.dp,
                                                modifier = Modifier
                                                    .width(180.dp)
                                                    .aspectRatio(3f / 4f)
                                                    .let { mod ->
                                                        when {
                                                            hasDetailPhoto -> mod.clickable { showPhotoPreview = true }
                                                            onUploadStudentPhoto != null -> mod.clickable { showPhotoManager = true }
                                                            else -> mod
                                                        }
                                                    }
                                                    .testTag("img_student_detail_large_photo")
                                            )
                                        }
                                    }
                                }

                                // Kartu 1: Identitas Kependudukan & Akademik Lengkap
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .testTag("section_student_biodata")
                                ) {
                                    SelectionContainer {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Badge,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isEn) "Civil & School Identity" else "Identitas Kependudukan & Sekolah",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                                            ProfileDetailRow(
                                                label = "NIK",
                                                value = cleanNik.ifBlank { "-" },
                                                isMonospace = true,
                                                copyContentDescPrefix = if (isEn) "Copy" else "Salin",
                                                onCopy = if (cleanNik.isNotBlank()) {
                                                    { copyValue("NIK", cleanNik) }
                                                } else null
                                            )
                                            ProfileDetailRow(
                                                label = if (isEn) "KK No." else "No. KK",
                                                value = cleanNoKk.ifBlank { "-" },
                                                isMonospace = true,
                                                copyContentDescPrefix = if (isEn) "Copy" else "Salin",
                                                onCopy = if (cleanNoKk.isNotBlank()) {
                                                    { copyValue(if (isEn) "KK No." else "No. KK", cleanNoKk) }
                                                } else null
                                            )
                                            ProfileDetailRow(
                                                label = "NIS / NISN",
                                                value = student.nis.ifBlank { "-" },
                                                isMonospace = true,
                                                copyContentDescPrefix = if (isEn) "Copy" else "Salin",
                                                onCopy = if (student.nis.isNotBlank()) {
                                                    { copyValue("NIS/NISN", student.nis) }
                                                } else null
                                            )
                                            ProfileDetailRow(
                                                label = if (isEn) "Level / Class" else "Jenjang / Kelas",
                                                value = "${resolvedJenjang.ifBlank { "-" }} • ${student.classGrade.ifBlank { "-" }}"
                                            )
                                            ProfileDetailRow(
                                                label = if (isEn) "Gender" else "Jenis Kelamin",
                                                value = if (isEn) {
                                                    if (isMale) "Male (Putra)" else "Female (Putri)"
                                                } else {
                                                    if (isMale) "Laki-laki (Putra)" else "Perempuan (Putri)"
                                                }
                                            )
                                            if (isChristianStudent) {
                                                ProfileDetailRow(
                                                    label = if (isEn) "Religion" else "Agama",
                                                    value = if (isEn) "Christian" else "Kristen"
                                                )
                                            }
                                            val ttlFormatted = listOf(student.birthPlace, student.birthDate)
                                                .filter { it.isNotBlank() }
                                                .joinToString(", ")
                                                .ifBlank { "-" }
                                            ProfileDetailRow(
                                                label = if (isEn) "Birth Place, Date" else "Tempat, Tgl Lahir",
                                                value = ttlFormatted
                                            )
                                            ProfileDetailRow(
                                                label = if (isEn) "Age" else "Usia",
                                                value = if (isEn) "${student.age} Years" else "${student.age} Tahun"
                                            )
                                            ProfileDetailRow(
                                                label = if (isEn) "Biological Mother" else "Nama Ibu Kandung",
                                                value = student.motherName.ifBlank { "-" }
                                            )
                                            ProfileDetailRow(
                                                label = if (isEn) "Full Address" else "Alamat Lengkap",
                                                value = student.address.ifBlank { "-" }
                                            )
                                            ProfileDetailRow(
                                                label = if (isEn) "District" else "Kecamatan",
                                                value = student.district.ifBlank { "-" }
                                            )
                                        }
                                    }
                                }

                                // Kartu 2: Profil Asrama, Kontak & Kesehatan
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                            RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocalHospital,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isEn) "Asrama, Contact & Health" else "Asrama, Kontak & Kesehatan",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                                        ProfileDetailRow(
                                            label = "Asrama",
                                            value = student.dormitory.ifBlank { "-" }
                                        )
                                        ProfileDetailRow(
                                            label = if (isEn) "Room" else "Kamar",
                                            value = student.roomNumber.ifBlank { "-" }
                                        )
                                        ProfileDetailRow(
                                            label = if (isEn) "Parent WhatsApp / Phone" else "No. HP Orang Tua",
                                            value = student.parentPhone.ifBlank { "-" }
                                        )
                                        if (com.example.data.model.TandemWaliHelper.hasPrimaryWali(student)) {
                                            ProfileDetailRow(
                                                label = if (isEn) "Caregiver Team" else "Tim Wali Asuh Pembina",
                                                value = com.example.data.model.TandemWaliHelper.formatAllWaliSummary(student, isEn)
                                            )
                                        }
                                        ProfileDetailRow(
                                            label = if (isEn) "Guardian / Mother Contact" else "Kontak Wali / Ibu",
                                            value = student.emergencyContact.ifBlank { "-" }
                                        )
                                        ProfileDetailRow(
                                            label = if (isEn) "Blood Type" else "Golongan Darah",
                                            value = student.bloodType.ifBlank { "-" }
                                        )
                                        ProfileDetailRow(
                                            label = if (isEn) "Allergy History" else "Riwayat Alergi",
                                            value = student.allergies.ifBlank {
                                                if (isEn) "None" else "Tidak ada"
                                            }.let { raw ->
                                                if (isEn && raw.equals("Tidak ada", ignoreCase = true)) "None" else raw
                                            }
                                        )
                                        val cleanNotes = student.specialNotes.trim()
                                        if (cleanNotes.isNotBlank() && !cleanNotes.equals("Alamat Asal: ${student.address}", ignoreCase = true)) {
                                            ProfileDetailRow(
                                                label = if (isEn) "Special Notes" else "Catatan Khusus",
                                                value = cleanNotes
                                            )
                                        }
                                    }
                                }
                            }

                            StudentDetailTab.MENGAJI -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = emeraldPrimary,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isEn) {
                                                "Mengaji History (${orderedMengajiRecords.size})"
                                            } else {
                                                "Riwayat Mengaji (${orderedMengajiRecords.size} Setoran)"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (onInputMengaji != null) {
                                            OutlinedButton(
                                                onClick = { onInputMengaji(student) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier
                                                    .height(34.dp)
                                                    .testTag("btn_student_input_mengaji")
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = emeraldPrimary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(if (isEn) "Input" else "Input", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = emeraldPrimary)
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                if (onPrintKartuMengaji != null) {
                                                    onPrintKartuMengaji(student)
                                                } else {
                                                    showInlineKartuMengaji = true
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("btn_student_kartu_mengaji")
                                        ) {
                                            Icon(Icons.Default.Print, contentDescription = null, tint = emeraldPrimary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(if (isEn) "Card" else "Kartu", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = emeraldPrimary)
                                        }
                                    }
                                }

                                val latestMengaji = orderedMengajiRecords.firstOrNull()
                                if (latestMengaji == null) {
                                    EmptyTabStateCard(
                                        icon = Icons.AutoMirrored.Filled.MenuBook,
                                        accentColor = emeraldPrimary,
                                        title = if (isEn) "No Mengaji Records Yet" else "Belum Ada Setoran Mengaji",
                                        subtitle = if (isEn) {
                                            "Tap Input above to record this student's Iqro' or Al-Qur'an progress."
                                        } else {
                                            "Ketuk tombol Input di atas untuk mencatat setoran Iqro' atau Al-Qur'an siswa ini."
                                        }
                                    )
                                } else {
                                    val lastFluent = MengajiHelper.isFluent(latestMengaji.fluencyStatus)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (lastFluent) Color(0xFFD1FAE5) else Color(0xFFFEF3C7))
                                            .padding(horizontal = 10.dp, vertical = 7.dp)
                                            .testTag("badge_student_latest_mengaji")
                                    ) {
                                        Text(
                                            text = if (isEn) {
                                                "Latest Position: ${MengajiHelper.formatCompactPosition(latestMengaji)} (${MengajiHelper.fluencyLabel(latestMengaji.fluencyStatus, true)})"
                                            } else {
                                                "Bacaan Terakhir: ${MengajiHelper.formatCompactPosition(latestMengaji)} (${MengajiHelper.fluencyLabel(latestMengaji.fluencyStatus, false)})"
                                            },
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (lastFluent) Color(0xFF065F46) else Color(0xFF92400E)
                                        )
                                    }

                                    val displayedMengaji = if (showAllMengajiHistory) {
                                        orderedMengajiRecords
                                    } else {
                                        orderedMengajiRecords.take(8)
                                    }
                                    displayedMengaji.forEach { rec ->
                                        val isFluent = MengajiHelper.isFluent(rec.fluencyStatus)
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .testTag("student_mengaji_item_${rec.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = MengajiHelper.formatCompactPosition(rec),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "${DateUtils.formatShortDate(rec.sessionDate, s.language)} • Wali: ${rec.waliName}",
                                                        fontSize = 10.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    if (rec.notes.isNotBlank()) {
                                                        Text(
                                                            text = rec.notes,
                                                            fontSize = 10.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (isFluent) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = MengajiHelper.fluencyLabel(rec.fluencyStatus, isEn),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isFluent) Color(0xFF15803D) else Color(0xFFB91C1C)
                                                        )
                                                    }
                                                    if (onDeleteMengajiRecord != null) {
                                                        IconButton(
                                                            onClick = { pendingDeleteMengaji = rec },
                                                            modifier = Modifier.testTag("btn_delete_mengaji_${rec.id}")
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.DeleteOutline,
                                                                contentDescription = if (isEn) "Delete Mengaji record" else "Hapus riwayat mengaji",
                                                                tint = MaterialTheme.colorScheme.error,
                                                                modifier = Modifier.size(19.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (orderedMengajiRecords.size > 8) {
                                        TextButton(
                                            onClick = { showAllMengajiHistory = !showAllMengajiHistory },
                                            modifier = Modifier
                                                .align(Alignment.CenterHorizontally)
                                                .testTag("btn_student_toggle_all_mengaji")
                                        ) {
                                            Text(
                                                text = if (showAllMengajiHistory) {
                                                    if (isEn) "Show less" else "Ringkas riwayat mengaji"
                                                } else {
                                                    if (isEn) {
                                                        "Show all ${orderedMengajiRecords.size} sessions"
                                                    } else {
                                                        "Lihat semua (${orderedMengajiRecords.size} setoran)"
                                                    }
                                                },
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = emeraldPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            StudentDetailTab.IBADAH_KRISTEN -> {
                                val morningCount = orderedChristianRecords.count {
                                    ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_PAGI
                                }
                                val eveningCount = orderedChristianRecords.count {
                                    ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_MALAM
                                }
                                val churchCount = orderedChristianRecords.count {
                                    ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_KE_GEREJA
                                }
                                val nurtureCount = orderedChristianRecords.count {
                                    ChristianWorshipHelper.isExtraType(it.activityType)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Church,
                                            contentDescription = null,
                                            tint = christianIndigo,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isEn) {
                                                "Christian Worship (${orderedChristianRecords.size})"
                                            } else {
                                                "Riwayat Ibadah Kristen (${orderedChristianRecords.size})"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (onInputChristianWorship != null) {
                                        FilledTonalButton(
                                            onClick = { onInputChristianWorship(student) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = Color(0xFFE0E7FF),
                                                contentColor = christianIndigo
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_student_detail_input_christian")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isEn) "Add Worship" else "Input Ibadah",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FastingStatMiniPill(
                                        label = if (isEn) "Morning" else "Ibadah Pagi",
                                        count = morningCount,
                                        bgColor = Color(0xFFFEF3C7),
                                        textColor = Color(0xFFB45309),
                                        modifier = Modifier.weight(1f)
                                    )
                                    FastingStatMiniPill(
                                        label = if (isEn) "Evening" else "Ibadah Malam",
                                        count = eveningCount,
                                        bgColor = Color(0xFFE0E7FF),
                                        textColor = Color(0xFF3730A3),
                                        modifier = Modifier.weight(1f)
                                    )
                                    FastingStatMiniPill(
                                        label = if (isEn) "Church" else "Gereja",
                                        count = churchCount,
                                        bgColor = Color(0xFFCCFBF1),
                                        textColor = Color(0xFF0F766E),
                                        modifier = Modifier.weight(1f)
                                    )
                                    FastingStatMiniPill(
                                        label = if (isEn) "Nurture" else "Pembinaan",
                                        count = nurtureCount,
                                        bgColor = Color(0xFFF3E8FF),
                                        textColor = Color(0xFF7E22CE),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (orderedChristianRecords.isEmpty()) {
                                    EmptyTabStateCard(
                                        icon = Icons.Default.Church,
                                        accentColor = christianIndigo,
                                        title = if (isEn) "No Christian Worship Records Yet" else "Belum Ada Catatan Ibadah Kristen",
                                        subtitle = if (isEn) {
                                            "Record morning/evening worship, church service, memory verses, or prayer notes for this student."
                                        } else {
                                            "Catat ibadah pagi/malam, ke gereja, ayat hafalan, atau pokok doa siswa melalui menu Agama Kristen."
                                        },
                                        modifier = Modifier.testTag("empty_student_christian_state")
                                    )
                                } else {
                                    orderedChristianRecords.forEach { rec ->
                                        val vis = christianActivityVisual(rec.activityType)
                                        val hasPhoto = ChristianWorshipHelper.hasAnyPhoto(rec)
                                        val remDays = ChristianWorshipHelper.remainingDays(rec)
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    1.dp,
                                                    vis.borderLight.copy(alpha = 0.65f),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .testTag("student_christian_item_${rec.id}")
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = vis.bgLight
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = vis.icon,
                                                                contentDescription = null,
                                                                tint = vis.accent,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Text(
                                                                text = ChristianWorshipHelper.activityLabel(rec.activityType, isEn),
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = vis.accent
                                                            )
                                                        }
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = DateUtils.formatShortDate(rec.activityDate, s.language),
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        if (onDeleteChristianWorshipRecord != null) {
                                                            IconButton(
                                                                onClick = { pendingDeleteChristianWorship = rec },
                                                                modifier = Modifier
                                                                    .size(48.dp)
                                                                    .testTag("btn_delete_christian_${rec.id}")
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.DeleteOutline,
                                                                    contentDescription = if (isEn) "Delete worship log" else "Hapus catatan ibadah Kristen",
                                                                    tint = MaterialTheme.colorScheme.error,
                                                                    modifier = Modifier.size(19.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                if (rec.title.isNotBlank()) {
                                                    Text(
                                                        text = rec.title,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                if (rec.notes.isNotBlank()) {
                                                    Text(
                                                        text = rec.notes,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                if (rec.waliName.isNotBlank()) {
                                                    Text(
                                                        text = if (isEn) "Caregiver: ${rec.waliName}" else "Pembina: ${rec.waliName}",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                if (hasPhoto && !ChristianWorshipHelper.isProofExpired(rec)) {
                                                    val imgModel = rec.photoUrl.ifBlank {
                                                        rec.photoFallbackUrl.ifBlank { rec.localPhotoPath }
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(110.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable { previewChristianWorship = rec }
                                                            .testTag("btn_student_christian_photo_${rec.id}")
                                                    ) {
                                                        Image(
                                                            painter = rememberAsyncImagePainter(imgModel),
                                                            contentDescription = rec.title.ifBlank { student.name },
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.matchParentSize()
                                                        )
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = Color.Black.copy(alpha = 0.6f),
                                                            modifier = Modifier
                                                                .align(Alignment.BottomEnd)
                                                                .padding(6.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.ZoomIn,
                                                                    contentDescription = null,
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(13.dp)
                                                                )
                                                                Text(
                                                                    text = if (remDays > 0) "${remDays}h" else if (isEn) "Photo" else "Foto",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.White
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

                            StudentDetailTab.BEROBAT -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.HistoryEdu,
                                            contentDescription = null,
                                            tint = medicalSky,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isEn) {
                                                "Medical History (${studentRecords.size} Visits)"
                                            } else {
                                                "Riwayat Berobat (${studentRecords.size} Kali)"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { showPrintPdfDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("btn_student_print_medical_pdf")
                                    ) {
                                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp), tint = medicalSky)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isEn) "Print PDF" else "Cetak PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = medicalSky)
                                    }
                                }

                                if (studentRecords.isEmpty()) {
                                    EmptyTabStateCard(
                                        icon = Icons.Default.LocalHospital,
                                        accentColor = medicalSky,
                                        title = if (isEn) "No Medical Visits Yet" else "Belum Ada Riwayat Berobat",
                                        subtitle = if (isEn) {
                                            "This student has no recorded health complaints or clinic referrals."
                                        } else {
                                            "Siswa ini belum memiliki riwayat keluhan sakit atau rujukan faskes."
                                        }
                                    )
                                } else {
                                    studentRecords.forEach { record ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .testTag("student_medical_item_${record.id}")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    FaskesBadge(faskes = record.faskes)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        val dateStr = record.treatmentDate?.let { DateUtils.formatShortDate(it) }
                                                            ?: DateUtils.formatShortDate(record.planDate)
                                                        Text(
                                                            text = dateStr,
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        if (onDeleteMedicalRecord != null) {
                                                            IconButton(
                                                                onClick = { pendingDeleteMedical = record },
                                                                modifier = Modifier.testTag("btn_delete_medical_${record.id}")
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.DeleteOutline,
                                                                    contentDescription = if (isEn) "Delete medical record" else "Hapus riwayat berobat",
                                                                    tint = MaterialTheme.colorScheme.error,
                                                                    modifier = Modifier.size(19.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = if (isEn) "Complaint: ${record.complaint}" else "Keluhan: ${record.complaint}",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Medium
                                                )

                                                if (record.diagnosis.isNotBlank()) {
                                                    Text(
                                                        text = if (isEn) "Diagnosis: ${record.diagnosis}" else "Diagnosa: ${record.diagnosis}",
                                                        fontSize = 11.5.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }

                                                if (record.prescription.isNotBlank()) {
                                                    Text(
                                                        text = if (isEn) "Medicine: ${record.prescription}" else "Obat: ${record.prescription}",
                                                        fontSize = 10.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                if (record.isInpatient) {
                                                    StudentChipBadge(
                                                        text = if (isEn) {
                                                            "Inpatient${if (record.inpatientRoom.isNotBlank()) " • ${record.inpatientRoom}" else ""}"
                                                        } else {
                                                            "Rawat Inap${if (record.inpatientRoom.isNotBlank()) " • ${record.inpatientRoom}" else ""}"
                                                        },
                                                        bgColor = Color(0xFFFEE2E2),
                                                        textColor = Color(0xFFB91C1C)
                                                    )
                                                }

                                                ControlBadge(
                                                    needsControl = record.needsControl,
                                                    controlDateFormatted = record.controlDate?.let { DateUtils.formatShortDate(it) },
                                                    isCompleted = record.isControlCompleted
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            StudentDetailTab.PUASA -> {
                                val sahurCount = orderedFastingRecords.count { it.isSahurChecked }
                                val bukaCount = orderedFastingRecords.count { it.isBukaChecked }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NightsStay,
                                        contentDescription = null,
                                        tint = fastingAmber,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEn) {
                                            "Fasting History (${orderedFastingRecords.size} Sessions)"
                                        } else {
                                            "Riwayat Puasa (${orderedFastingRecords.size} Sesi)"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                // Ringkasan statistik puasa siswa
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FastingStatMiniPill(
                                        label = if (isEn) "Registered" else "Terdaftar",
                                        count = orderedFastingRecords.size,
                                        bgColor = Color(0xFFFEF3C7),
                                        textColor = Color(0xFF92400E),
                                        modifier = Modifier.weight(1f)
                                    )
                                    FastingStatMiniPill(
                                        label = "Sahur",
                                        count = sahurCount,
                                        bgColor = Color(0xFFDCFCE7),
                                        textColor = Color(0xFF166534),
                                        modifier = Modifier.weight(1f)
                                    )
                                    FastingStatMiniPill(
                                        label = if (isEn) "Iftar" else "Berbuka",
                                        count = bukaCount,
                                        bgColor = Color(0xFFE0F2FE),
                                        textColor = Color(0xFF075985),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (orderedFastingRecords.isEmpty()) {
                                    EmptyTabStateCard(
                                        icon = Icons.Default.NightsStay,
                                        accentColor = fastingAmber,
                                        title = if (isEn) "No Fasting History Yet" else "Belum Ada Riwayat Puasa",
                                        subtitle = if (isEn) {
                                            "This student has not been registered in any fasting session yet."
                                        } else {
                                            "Siswa ini belum tercatat mengikuti sesi puasa sunnah maupun wajib."
                                        },
                                        modifier = Modifier.testTag("empty_student_fasting_state")
                                    )
                                } else {
                                    orderedFastingRecords.forEach { rec ->
                                        val recDate = rec.sessionDate.takeIf { it > 0L } ?: rec.createdAt
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .testTag("student_fasting_item_${rec.id}")
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = rec.sessionTitle.ifBlank {
                                                            if (isEn) "Fasting Session" else "Sesi Puasa"
                                                        },
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.weight(1f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = DateUtils.formatShortDate(recDate, s.language),
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        if (onDeleteFastingRecord != null) {
                                                            IconButton(
                                                                onClick = { pendingDeleteFasting = rec },
                                                                modifier = Modifier.testTag("btn_delete_fasting_${rec.id}")
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.DeleteOutline,
                                                                    contentDescription = if (isEn) "Delete fasting record" else "Hapus riwayat puasa",
                                                                    tint = MaterialTheme.colorScheme.error,
                                                                    modifier = Modifier.size(19.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = "Wali: ${rec.waliName.ifBlank { "-" }}",
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    FastingCheckStatusChip(
                                                        label = "Sahur",
                                                        checked = rec.isSahurChecked,
                                                        checkedBy = rec.sahurCheckedBy,
                                                        isEn = isEn,
                                                        activeBg = Color(0xFFDCFCE7),
                                                        activeText = Color(0xFF166534),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    FastingCheckStatusChip(
                                                        label = if (isEn) "Iftar" else "Berbuka",
                                                        checked = rec.isBukaChecked,
                                                        checkedBy = rec.bukaCheckedBy,
                                                        isEn = isEn,
                                                        activeBg = Color(0xFFFEF3C7),
                                                        activeText = Color(0xFF92400E),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }

                                                if (rec.notes.isNotBlank()) {
                                                    Text(
                                                        text = rec.notes,
                                                        fontSize = 10.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            StudentDetailTab.SHOLAT -> {
                                val todayActiveHaid = remember(student, haidRecords) {
                                    SholatHelper.findActiveHaidOnDate(
                                        student = student,
                                        haidRecords = haidRecords,
                                        dateKey = SholatHelper.todayDateKey()
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = sholatTeal,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isEn) {
                                                "5-Time Prayer History (${orderedSholatRecords.size})"
                                            } else {
                                                "Pantauan Sholat 5 Waktu (${orderedSholatRecords.size})"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (onInputSholat != null) {
                                            FilledTonalButton(
                                                onClick = { onInputSholat(student) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier
                                                    .height(34.dp)
                                                    .testTag("btn_student_detail_input_sholat")
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isEn) "Absen" else "Catat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val file = PdfReportGenerator.generateSholatStudentReportPdf(
                                                    context = context,
                                                    schoolProfile = schoolProfile,
                                                    student = student,
                                                    sholatRecords = orderedSholatRecords,
                                                    monthKey = currentSholatMonthKey,
                                                    waliName = waliName
                                                )
                                                if (file != null) {
                                                    PdfReportGenerator.shareSholatPdf(
                                                        context = context,
                                                        pdfFile = file,
                                                        subject = if (isEn) "Prayer Report: ${student.name}" else "Rapor Sholat: ${student.name}",
                                                        text = if (isEn) "Attached 5-time prayer report for ${student.name}." else "Terlampir PDF Rapor Sholat 5 Waktu ${student.name}."
                                                    )
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        if (isEn) "Unable to create Sholat PDF" else "Gagal membuat PDF Rapor Sholat",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("btn_student_detail_sholat_pdf")
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp), tint = sholatTeal)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = sholatTeal)
                                        }
                                    }
                                }

                                // Ringkasan KPI bulan berjalan
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FastingStatMiniPill(
                                        label = "Jamaah",
                                        count = sholatAnalytics.jamaahCount,
                                        bgColor = Color(0xFFDCFCE7),
                                        textColor = Color(0xFF166534),
                                        modifier = Modifier.weight(1f)
                                    )
                                    FastingStatMiniPill(
                                        label = "Munfarid",
                                        count = sholatAnalytics.munfaridCount,
                                        bgColor = Color(0xFFFEF3C7),
                                        textColor = Color(0xFF92400E),
                                        modifier = Modifier.weight(1f)
                                    )
                                    FastingStatMiniPill(
                                        label = if (isEn) "Excused" else "Udzur/Izin",
                                        count = sholatAnalytics.udzurHaidCount + sholatAnalytics.sakitCount + sholatAnalytics.izinCount,
                                        bgColor = Color(0xFFFCE7F3),
                                        textColor = Color(0xFF9D174D),
                                        modifier = Modifier.weight(1f)
                                    )
                                    FastingStatMiniPill(
                                        label = "Alfa",
                                        count = sholatAnalytics.alfaCount,
                                        bgColor = Color(0xFFFEE2E2),
                                        textColor = Color(0xFFDC2626),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = sholatTeal.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, sholatTeal.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isEn) {
                                                "Discipline (${SholatHelper.formatMonthKeyDisplay(currentSholatMonthKey, true)})"
                                            } else {
                                                "Disiplin Jamaah (${SholatHelper.formatMonthKeyDisplay(currentSholatMonthKey, false)})"
                                            },
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = sholatTeal
                                        )
                                        Text(
                                            text = "${sholatAnalytics.jamaahRatePercent}% • ${sholatAnalytics.subuhJamaahCount} Subuh",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = sholatTeal
                                        )
                                    }
                                }

                                if (todayActiveHaid != null) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFDF2F8),
                                        border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (isEn) {
                                                "Active haid recorded (Day ${todayActiveHaid.durationDays}) — automatically respected as Udzur Syar'i."
                                            } else {
                                                "Sedang tercatat masa haid aktif (Hari ke-${todayActiveHaid.durationDays}) — otomatis berstatus Udzur Syar'i (Haid)."
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF9D174D),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                        )
                                    }
                                }

                                if (orderedSholatRecords.isEmpty()) {
                                    EmptyTabStateCard(
                                        icon = Icons.Default.AutoAwesome,
                                        accentColor = sholatTeal,
                                        title = if (isEn) "No 5-Time Prayer History Yet" else "Belum Ada Catatan Sholat 5 Waktu",
                                        subtitle = if (isEn) {
                                            "Record daily Subuh, Dzuhur, Ashar, Maghrib, and Isya attendance via the Absen Sholat menu."
                                        } else {
                                            "Catat kehadiran Subuh, Dzuhur, Ashar, Maghrib & Isya melalui menu Absen Sholat di beranda."
                                        },
                                        modifier = Modifier.testTag("empty_student_sholat_state")
                                    )
                                } else {
                                    val groupedByDate = remember(orderedSholatRecords) {
                                        orderedSholatRecords.groupBy { it.dateKey }.entries.sortedByDescending { it.key }
                                    }
                                    groupedByDate.forEach { (dateKey, dayRecords) ->
                                        val byWaktu = dayRecords.associateBy { SholatWaktu.fromCode(it.waktu) }
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .testTag("student_sholat_day_$dateKey")
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(7.dp)
                                            ) {
                                                Text(
                                                    text = SholatHelper.formatDateKeyDisplay(dateKey, isEn),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    SholatWaktu.entries.forEach { w ->
                                                        val rec = byWaktu[w]
                                                        val st = rec?.let { SholatStatus.fromCode(it.status) }
                                                        val (bg, fg) = if (st != null) sholatStatusColors(st) else Color(0xFFF1F5F9) to Color(0xFF94A3B8)
                                                        Column(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(bg)
                                                                .padding(horizontal = 3.dp, vertical = 5.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            Text(
                                                                text = w.shortCode,
                                                                fontSize = 9.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = fg
                                                            )
                                                            Text(
                                                                text = st?.shortCode ?: "•",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = fg
                                                            )
                                                        }
                                                    }
                                                }
                                                dayRecords.sortedBy { SholatWaktu.fromCode(it.waktu).ordinal }.forEach { rec ->
                                                    val w = SholatWaktu.fromCode(rec.waktu)
                                                    val st = SholatStatus.fromCode(rec.status)
                                                    val tag = SholatDisciplineTag.fromCode(rec.disciplineTag)
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            val tagSuffix = if (tag != SholatDisciplineTag.NONE) " • ${tag.label(isEn)}" else ""
                                                            Text(
                                                                text = "${w.label(isEn)}: ${st.label(isEn)}$tagSuffix",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            if (rec.notes.isNotBlank() || rec.recordedBy.isNotBlank()) {
                                                                Text(
                                                                    text = listOfNotNull(
                                                                        rec.notes.takeIf { it.isNotBlank() },
                                                                        rec.recordedBy.takeIf { it.isNotBlank() }?.let { "Wali: $it" }
                                                                    ).joinToString(" • "),
                                                                    fontSize = 10.sp,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    maxLines = 2,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                        if (onDeleteSholatRecord != null) {
                                                            IconButton(
                                                                onClick = { pendingDeleteSholat = rec },
                                                                modifier = Modifier.testTag("btn_delete_sholat_${rec.id}")
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.DeleteOutline,
                                                                    contentDescription = if (isEn) "Delete prayer record" else "Hapus catatan sholat",
                                                                    tint = MaterialTheme.colorScheme.error,
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
                }

                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (allowCardQr) {
                        FilledTonalButton(
                            onClick = { showCardGeneratorForStudent = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_open_student_card_qr")
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(if (isEn) "Card & QR" else "Kartu & QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (onEditStudent != null) {
                        OutlinedButton(
                            onClick = { onEditStudent(student) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_edit_student_profile")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEn) "Edit Profile" else "Edit Profil", fontSize = 12.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(s.nav.close, fontSize = 12.sp)
                    }
                }
                }
            }
        }
    }

    if (showPrintPdfDialog) {
        PrintPdfDialog(
            student = student,
            allRecords = studentRecords,
            waliName = waliName,
            schoolProfile = schoolProfile,
            isPetugasKesehatan = isPetugasKesehatan,
            onDismiss = { showPrintPdfDialog = false }
        )
    }

    if (showCardGeneratorForStudent) {
        StudentCardGeneratorSheet(
            students = listOf(student),
            schoolProfile = schoolProfile,
            initialStudent = student,
            onDismiss = { showCardGeneratorForStudent = false }
        )
    }

    if (showInlineKartuMengaji) {
        KartuMengajiSheet(
            students = listOf(student),
            mengajiRecords = orderedMengajiRecords,
            schoolProfile = schoolProfile,
            waliName = waliName,
            initialStudent = student,
            onOpenInputForStudent = { st ->
                showInlineKartuMengaji = false
                onInputMengaji?.invoke(st)
            },
            onDismiss = { showInlineKartuMengaji = false }
        )
    }

    pendingDeleteMengaji?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDeleteMengaji = null },
            title = {
                Text(
                    text = if (isEn) "Delete Mengaji Record?" else "Hapus Riwayat Mengaji?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEn) {
                        "Are you sure you want to delete ${MengajiHelper.formatCompactPosition(target)} for ${student.name}? Colleague Wali Asuh will receive a notification on the bell icon."
                    } else {
                        "Yakin ingin menghapus catatan ${MengajiHelper.formatCompactPosition(target)} milik ${student.name}? Rekan Wali Asuh di sekolah akan mendapat notifikasi pada ikon lonceng."
                    },
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMengajiRecord?.invoke(target)
                        pendingDeleteMengaji = null
                        Toast.makeText(
                            context,
                            if (isEn) "Mengaji record deleted" else "Riwayat mengaji dihapus & rekan Wali Asuh diberi notifikasi",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_delete_student_history")
                ) {
                    Text(if (isEn) "Delete" else "Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteMengaji = null }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    pendingDeleteMedical?.let { target ->
        val summary = target.complaint.ifBlank { target.diagnosis }.ifBlank { target.faskes }
        AlertDialog(
            onDismissRequest = { pendingDeleteMedical = null },
            title = {
                Text(
                    text = if (isEn) "Delete Medical Record?" else "Hapus Riwayat Berobat?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEn) {
                        "Are you sure you want to delete medical record ($summary) for ${student.name}? Colleague Wali Asuh will receive a notification on the bell icon."
                    } else {
                        "Yakin ingin menghapus riwayat berobat ($summary) milik ${student.name}? Rekan Wali Asuh di sekolah akan mendapat notifikasi pada ikon lonceng."
                    },
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMedicalRecord?.invoke(target)
                        pendingDeleteMedical = null
                        Toast.makeText(
                            context,
                            if (isEn) "Medical record deleted" else "Riwayat berobat dihapus & rekan Wali Asuh diberi notifikasi",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_delete_student_history")
                ) {
                    Text(if (isEn) "Delete" else "Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteMedical = null }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    pendingDeleteFasting?.let { target ->
        val sessionLabel = target.sessionTitle.ifBlank { if (isEn) "Fasting Session" else "Sesi Puasa" }
        AlertDialog(
            onDismissRequest = { pendingDeleteFasting = null },
            title = {
                Text(
                    text = if (isEn) "Delete Fasting Record?" else "Hapus Riwayat Puasa?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEn) {
                        "Are you sure you want to delete fasting record ($sessionLabel) for ${student.name}? Colleague Wali Asuh will receive a notification on the bell icon."
                    } else {
                        "Yakin ingin menghapus riwayat puasa ($sessionLabel) milik ${student.name}? Rekan Wali Asuh di sekolah akan mendapat notifikasi pada ikon lonceng."
                    },
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFastingRecord?.invoke(target)
                        pendingDeleteFasting = null
                        Toast.makeText(
                            context,
                            if (isEn) "Fasting record deleted" else "Riwayat puasa dihapus & rekan Wali Asuh diberi notifikasi",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_delete_student_history")
                ) {
                    Text(if (isEn) "Delete" else "Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteFasting = null }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    pendingDeleteSholat?.let { target ->
        val waktuLabel = SholatWaktu.fromCode(target.waktu).label(isEn)
        val statusLabel = SholatStatus.fromCode(target.status).label(isEn)
        AlertDialog(
            onDismissRequest = { pendingDeleteSholat = null },
            title = {
                Text(
                    text = if (isEn) "Delete Prayer Record?" else "Hapus Catatan Sholat?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEn) {
                        "Are you sure you want to delete $waktuLabel ($statusLabel) on ${target.dateKey} for ${student.name}?"
                    } else {
                        "Yakin ingin menghapus catatan sholat $waktuLabel ($statusLabel) tanggal ${target.dateKey} milik ${student.name}?"
                    },
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSholatRecord?.invoke(target)
                        pendingDeleteSholat = null
                        Toast.makeText(
                            context,
                            if (isEn) "Prayer record deleted" else "Catatan sholat dihapus & rekan Wali Asuh diberi notifikasi",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_delete_student_history")
                ) {
                    Text(if (isEn) "Delete" else "Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteSholat = null }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    pendingDeleteChristianWorship?.let { target ->
        val actLabel = ChristianWorshipHelper.activityLabel(target.activityType, isEn)
        AlertDialog(
            onDismissRequest = { pendingDeleteChristianWorship = null },
            title = {
                Text(
                    text = if (isEn) "Delete Christian Worship Log?" else "Hapus Catatan Ibadah Kristen?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEn) {
                        "Are you sure you want to delete \"$actLabel\" (${target.title.ifBlank { target.dateKey }}) for ${student.name}?"
                    } else {
                        "Yakin ingin menghapus catatan \"$actLabel\" (${target.title.ifBlank { target.dateKey }}) milik ${student.name}?"
                    },
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteChristianWorshipRecord?.invoke(target)
                        pendingDeleteChristianWorship = null
                        Toast.makeText(
                            context,
                            if (isEn) "Christian worship log deleted" else "Catatan ibadah Kristen dihapus",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("btn_confirm_delete_student_christian")
                ) {
                    Text(if (isEn) "Delete" else "Hapus")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDeleteChristianWorship = null },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    previewChristianWorship?.let { rec ->
        val imgModel = rec.photoUrl.ifBlank { rec.photoFallbackUrl.ifBlank { rec.localPhotoPath } }
        ModalBottomSheet(
            onDismissRequest = { previewChristianWorship = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .testTag("sheet_student_christian_photo_preview"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(student.name, fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${ChristianWorshipHelper.activityLabel(rec.activityType, isEn)} • ${rec.title}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { previewChristianWorship = null },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close)
                    }
                }
                Image(
                    painter = rememberAsyncImagePainter(imgModel),
                    contentDescription = rec.title.ifBlank { student.name },
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            }
        }
    }

    if (showPhotoManager && onUploadStudentPhoto != null) {
        StudentPhotoManagerDialog(
            student = student,
            isUploading = isUploadingPhoto,
            onPickPhoto = { pickedUri ->
                onUploadStudentPhoto(student, pickedUri) { ok, msg ->
                    Toast.makeText(context, msg, if (ok) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
                }
            },
            onRemovePhoto = {
                onRemoveStudentPhoto?.invoke(student) { ok, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (ok) {
                        showPhotoManager = false
                    }
                }
            },
            onDismiss = { showPhotoManager = false }
        )
    }

    if (showPhotoPreview) {
        StudentPhotoPreviewDialog(
            studentName = student.name,
            gender = student.gender,
            subtitle = "${student.classGrade.ifBlank { "-" }} • ${student.dormitory.ifBlank { "-" }}",
            photoUrl = student.photoUrl,
            photoFallbackUrl = student.photoFallbackUrl,
            onDismiss = { showPhotoPreview = false }
        )
    }
}

@Composable
private fun SlimCategoryTabPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    accentColor: Color,
    countBadge: Int?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        animationSpec = tween(160),
        label = "tab_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(160),
        label = "tab_content"
    )
    val borderColor = if (selected) accentColor.copy(alpha = 0.45f) else Color.Transparent

    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            softWrap = false
        )
        if (countBadge != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (selected) accentColor.copy(alpha = 0.14f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                    .padding(horizontal = 5.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = countBadge.toString(),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun EmptyTabStateCard(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor.copy(alpha = 0.8f),
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FastingStatMiniPill(
    label: String,
    count: Int,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bgColor.copy(alpha = 0.7f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
private fun FastingCheckStatusChip(
    label: String,
    checked: Boolean,
    checkedBy: String,
    isEn: Boolean,
    activeBg: Color,
    activeText: Color,
    modifier: Modifier = Modifier
) {
    val bg = if (checked) activeBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val fg = if (checked) activeText else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(14.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (checked) "$label • ${if (isEn) "Checked" else "Terlaksana"}" else "$label • ${if (isEn) "Pending" else "Belum"}",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (checked && checkedBy.isNotBlank()) {
                Text(
                    text = checkedBy,
                    fontSize = 9.5.sp,
                    color = fg.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
private fun StudentChipBadge(
    text: String,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    isMonospace: Boolean = false,
    copyContentDescPrefix: String = "Salin",
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(114.dp)
        )
        Text(
            text = ":",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isMonospace && value != "-") FontFamily.Monospace else FontFamily.Default,
            color = if (value == "-") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (onCopy != null) {
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "$copyContentDescPrefix $label",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
