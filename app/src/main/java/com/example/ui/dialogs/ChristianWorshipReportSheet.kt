package com.example.ui.dialogs

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
import com.example.data.model.SchoolProfile
import com.example.ui.components.StudentAvatar
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import com.example.util.i18n.LocalAppStrings
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChristianWorshipReportSheet(
    christianStudents: List<ChildStudent>,
    records: List<ChristianWorshipRecord>,
    schoolProfile: SchoolProfile,
    waliName: String,
    initialTab: Int = 0,
    onDeleteRecord: (ChristianWorshipRecord) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var selectedStudentId by remember { mutableStateOf<Long?>(null) }
    var previewRecord by remember { mutableStateOf<ChristianWorshipRecord?>(null) }
    var readyPdfFile by remember { mutableStateOf<File?>(null) }

    val filteredRecords = remember(records, selectedTypeFilter, selectedStudentId) {
        records.filter { rec ->
            !rec.isDeleted &&
                (selectedTypeFilter == "ALL" || ChristianWorshipHelper.normalizeType(rec.activityType) == selectedTypeFilter) &&
                (selectedStudentId == null || rec.studentId == selectedStudentId)
        }.sortedWith(compareByDescending<ChristianWorshipRecord> { it.activityDate }.thenByDescending { it.createdAt })
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
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("sheet_christian_worship_report")
            ) {
                // Header Gradient Biru-Indigo + Drag Pill
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0369A1), Color(0xFF1D4ED8), Color(0xFF4338CA))
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
                                    text = if (isEn) "Christian Worship Reports" else "Laporan & Galeri Ibadah Kristen",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isEn) {
                                        "${filteredRecords.size} records • PDF Print & WhatsApp Share"
                                    } else {
                                        "${filteredRecords.size} catatan • Cetak PDF & Bagikan WA"
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Aksi Export PDF & Share WA (Rapat & Berwarna)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val pdf = PdfReportGenerator.generateChristianWorshipPdf(
                                    context = context,
                                    records = filteredRecords,
                                    students = christianStudents,
                                    schoolProfile = schoolProfile,
                                    filterTitle = selectedStudentId?.let { id ->
                                        christianStudents.firstOrNull { it.id == id }?.name
                                    } ?: ChristianWorshipHelper.activityLabel(selectedTypeFilter, isEn).takeIf { selectedTypeFilter != "ALL" }.orEmpty(),
                                    waliName = waliName
                                )
                                if (pdf != null) {
                                    readyPdfFile = pdf
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isEn) "Failed to generate PDF report." else "Gagal membuat PDF laporan ibadah.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp)
                                .testTag("btn_christian_export_pdf")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEn) "Print PDF" else "Cetak PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val waText = ChristianWorshipHelper.buildDailyWhatsappSummary(
                                    records = filteredRecords,
                                    students = christianStudents,
                                    filterTitle = if (isEn) "Christian Worship Log" else "Rekap Ibadah Agama Kristen",
                                    waliName = waliName,
                                    isEn = isEn
                                )
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, waText)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                runCatching {
                                    context.startActivity(
                                        Intent.createChooser(
                                            sendIntent,
                                            if (isEn) "Share Report" else "Bagikan Laporan Ibadah"
                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isDark) Color(0xFF064E3B).copy(alpha = 0.3f) else Color(0xFFF0FDF4)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF16A34A)),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp)
                                .testTag("btn_christian_share_wa")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEn) "Share WA" else "Bagikan WA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }
                    }

                    // Filter Jenis Kegiatan dengan Warna Pastel Khas Masing-masing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        FilterChip(
                            selected = selectedTypeFilter == "ALL",
                            onClick = { selectedTypeFilter = "ALL" },
                            label = { Text(if (isEn) "All Types" else "Semua", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE0E7FF),
                                selectedLabelColor = Color(0xFF3730A3)
                            )
                        )
                        ChristianWorshipHelper.ALL_TYPES.forEach { t ->
                            val vis = christianActivityVisual(t)
                            FilterChip(
                                selected = selectedTypeFilter == t,
                                onClick = { selectedTypeFilter = t },
                                label = { Text(ChristianWorshipHelper.activityShortLabel(t, isEn), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (isDark) vis.bgDark else vis.bgLight,
                                    selectedLabelColor = if (isDark) Color.White else vis.accent
                                )
                            )
                        }
                    }

                    // Filter Siswa
                    if (christianStudents.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            FilterChip(
                                selected = selectedStudentId == null,
                                onClick = { selectedStudentId = null },
                                label = { Text(if (isEn) "All Students" else "Semua Anak", fontSize = 11.sp) }
                            )
                            christianStudents.forEach { st ->
                                FilterChip(
                                    selected = selectedStudentId == st.id,
                                    onClick = { selectedStudentId = st.id },
                                    label = { Text(st.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 200.dp, max = 410.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        if (filteredRecords.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isEn) "No Christian worship records match this filter." else "Belum ada catatan ibadah untuk filter ini.",
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(filteredRecords, key = { "rep_chris_${it.id}" }) { rec ->
                                val hasPhoto = ChristianWorshipHelper.hasAnyPhoto(rec)
                                val remDays = ChristianWorshipHelper.remainingDays(rec)
                                val vis = christianActivityVisual(rec.activityType)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("card_christian_record_${rec.id}"),
                                    shape = RoundedCornerShape(13.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, vis.borderLight.copy(alpha = 0.7f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .fillMaxHeight()
                                                .background(vis.accent)
                                        )
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 11.dp, vertical = 9.dp),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(rec.studentName, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (isDark) vis.bgDark else vis.bgLight,
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "${ChristianWorshipHelper.activityLabel(rec.activityType, isEn)} • ${DateUtils.formatShortDate(rec.activityDate, lang)}",
                                                            fontSize = 10.sp,
                                                            color = if (isDark) Color.White else vis.accent,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                IconButton(
                                                    onClick = { onDeleteRecord(rec) },
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .testTag("btn_delete_christian_record_${rec.id}")
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = if (isEn) "Delete" else "Hapus",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            if (rec.title.isNotBlank()) {
                                                Text(rec.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            if (rec.notes.isNotBlank()) {
                                                Text(rec.notes, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }

                                            if (hasPhoto && !ChristianWorshipHelper.isProofExpired(rec)) {
                                                val imgModel = rec.photoUrl.ifBlank {
                                                    rec.photoFallbackUrl.ifBlank { rec.localPhotoPath }
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(130.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .clickable { previewRecord = rec }
                                                ) {
                                                    Image(
                                                        painter = rememberAsyncImagePainter(imgModel),
                                                        contentDescription = rec.title.ifBlank { rec.studentName },
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.matchParentSize()
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
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
                                                            Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                                            Text(
                                                                text = if (remDays > 0) "${remDays}h" else "Foto",
                                                                fontSize = 10.sp,
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold
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
        }
    }

    // Photo Zoom Sheet (Slider dari bawah)
    previewRecord?.let { rec ->
        val imgModel = rec.photoUrl.ifBlank { rec.photoFallbackUrl.ifBlank { rec.localPhotoPath } }
        ModalBottomSheet(
            onDismissRequest = { previewRecord = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(rec.studentName, fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${ChristianWorshipHelper.activityLabel(rec.activityType, isEn)} • ${rec.title}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { previewRecord = null }) {
                        Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                    }
                }
                Image(
                    painter = rememberAsyncImagePainter(imgModel),
                    contentDescription = rec.title.ifBlank { rec.studentName },
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Button(
                    onClick = { previewRecord = null },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text(if (isEn) "Close" else "Tutup")
                }
            }
        }
    }

    // PDF Share Sheet (Slider dari bawah)
    readyPdfFile?.let { file ->
        ModalBottomSheet(
            onDismissRequest = { readyPdfFile = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isEn) "Christian Worship PDF Ready" else "PDF Ibadah Kristen Siap",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEn) "Share or open this Christian worship PDF report." else "Bagikan atau buka laporan PDF kegiatan ibadah Kristen ini.",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { readyPdfFile = null },
                        modifier = Modifier.weight(0.8f).heightIn(min = 48.dp)
                    ) {
                        Text(if (isEn) "Close" else "Tutup")
                    }
                    Button(
                        onClick = {
                            PdfReportGenerator.shareChristianWorshipPdf(
                                context = context,
                                pdfFile = file,
                                subject = if (isEn) "Christian Worship Report" else "Rekap Ibadah Agama Kristen",
                                text = ChristianWorshipHelper.buildDailyWhatsappSummary(
                                    records = filteredRecords,
                                    students = christianStudents,
                                    filterTitle = "",
                                    waliName = waliName,
                                    isEn = isEn
                                )
                            )
                            readyPdfFile = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                        modifier = Modifier.weight(1.4f).heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEn) "Share PDF" else "Bagikan PDF", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SundayChurchScheduleSheet(
    christianStudents: List<ChildStudent>,
    records: List<ChristianWorshipRecord>,
    waliName: String,
    onQuickMarkChurch: (student: ChildStudent, churchName: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    var churchName by remember { mutableStateOf("Ibadah Sekolah Minggu / Gereja") }
    var chaperoneNotes by remember { mutableStateOf("") }
    val todayKey = ChristianWorshipHelper.todayDateKey()

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
                    .testTag("sheet_sunday_church_schedule")
            ) {
                // Header Gradient Teal-Emerald + Drag Pill
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0F766E), Color(0xFF0D9488), Color(0xFF0369A1))
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
                                    .border(1.5.dp, Color(0xFF99F6E4), CircleShape),
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
                                    text = if (isEn) "Sunday Church & Escort Roster" else "Jadwal & Pendampingan Gereja",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isEn) {
                                        "1-tap attendance check when departing to church"
                                    } else {
                                        "Tandai kehadiran ke gereja dalam 1 ketukan per anak"
                                    },
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFCCFBF1),
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

                // Body Formulir Rapat & Roster Gereja
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tealFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0xFF134E4A).copy(alpha = 0.45f) else Color(0xFFF0FDFA),
                        unfocusedContainerColor = if (isDark) Color(0xFF134E4A).copy(alpha = 0.25f) else Color(0xFFF7FEFC),
                        focusedBorderColor = Color(0xFF0F766E),
                        unfocusedBorderColor = if (isDark) Color(0xFF2DD4BF).copy(alpha = 0.45f) else Color(0xFF99F6E4),
                        focusedLabelColor = Color(0xFF0F766E)
                    )

                    OutlinedTextField(
                        value = churchName,
                        onValueChange = { churchName = it },
                        label = { Text(if (isEn) "Church / Worship Location" else "Nama Gereja / Lokasi Ibadah", fontSize = 11.5.sp) },
                        leadingIcon = { Icon(Icons.Default.Church, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = tealFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = chaperoneNotes,
                        onValueChange = { chaperoneNotes = it },
                        label = { Text(if (isEn) "Chaperone / Departure Notes" else "Catatan Pendamping / Jam Berangkat", fontSize = 11.5.sp) },
                        leadingIcon = { Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = Color(0xFF0369A1), modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = tealFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 170.dp, max = 330.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (christianStudents.isEmpty()) {
                            item {
                                Text(
                                    text = if (isEn) "Claim Christian students first to build the church roster." else "Klaim anak Kristen terlebih dahulu untuk menyusun daftar ke gereja.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 18.dp)
                                )
                            }
                        } else {
                            items(christianStudents, key = { "sun_church_${it.id}" }) { student ->
                                val markedToday = records.any { rec ->
                                    !rec.isDeleted &&
                                        rec.dateKey == todayKey &&
                                        ChristianWorshipHelper.normalizeType(rec.activityType) == ChristianWorshipRecord.TYPE_KE_GEREJA &&
                                        ChristianWorshipHelper.matchesStudent(rec, student)
                                }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            markedToday && isDark -> Color(0xFF064E3B).copy(alpha = 0.5f)
                                            markedToday -> Color(0xFFECFDF5)
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    ),
                                    border = BorderStroke(1.dp, if (markedToday) Color(0xFF34D399) else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StudentAvatar(
                                            name = student.name,
                                            gender = student.gender,
                                            size = 34.dp,
                                            photoUrl = student.photoUrl,
                                            photoFallbackUrl = student.photoFallbackUrl
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(student.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${student.classGrade.ifBlank { "-" }} • ${student.dormitory.ifBlank { "-" }}",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (markedToday) {
                                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF15803D)) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                                    Text(if (isEn) "Attended" else "Sudah Hadir", fontSize = 10.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            Button(
                                                onClick = { onQuickMarkChurch(student, churchName, chaperoneNotes) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                                shape = RoundedCornerShape(9.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .heightIn(min = 38.dp)
                                                    .testTag("btn_mark_church_${student.id}")
                                            ) {
                                                Text(if (isEn) "Mark Church" else "Tandai Gereja", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (christianStudents.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                val todayChurchLogs = records.filter {
                                    !it.isDeleted && it.dateKey == todayKey &&
                                        ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_KE_GEREJA
                                }
                                val msg = ChristianWorshipHelper.buildDailyWhatsappSummary(
                                    records = todayChurchLogs,
                                    students = christianStudents,
                                    filterTitle = "$churchName ($todayKey)",
                                    waliName = waliName,
                                    isEn = isEn
                                )
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, msg)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                runCatching {
                                    context.startActivity(Intent.createChooser(sendIntent, if (isEn) "Share Sunday Roster" else "Bagikan Laporan Gereja").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isDark) Color(0xFF064E3B).copy(alpha = 0.25f) else Color(0xFFF0FDF4)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF16A34A)),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Share Coordinator WA Summary" else "Kirim Rekap Pendamping ke WA",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }
        }
    }
}
