package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipLocalSave
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.SchoolProfile
import com.example.ui.components.ConfigureEdgeToEdgeDialogWindow
import com.example.ui.components.ParentReadyPdfDialogData
import com.example.ui.dialogs.RewardAdDeclarationDialog
import com.example.ui.dialogs.RewardAdDeclarationHint
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import com.example.util.TopOnAdManager
import com.example.util.i18n.LocalAppStrings
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun isChristianMillisInCurrentMonth(millis: Long): Boolean {
    if (millis <= 0L) return false
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = millis }
    return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        now.get(Calendar.MONTH) == target.get(Calendar.MONTH)
}

private fun currentChristianMonthLabel(isEn: Boolean): String {
    val locale = if (isEn) Locale.US else Locale("id", "ID")
    return SimpleDateFormat("MMMM yyyy", locale).format(Date())
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentChristianWorshipContent(
    student: ChildStudent?,
    records: List<ChristianWorshipRecord>,
    localSavesByRecordId: Map<Long, ChristianWorshipLocalSave>,
    schoolProfile: SchoolProfile,
    waliName: String,
    isSavingMedia: Boolean,
    onSavePhotoToDevice: (record: ChristianWorshipRecord, silentAuto: Boolean) -> Unit,
    onSaveAllPending: (List<ChristianWorshipRecord>) -> Unit,
    onOpenBible: (() -> Unit)? = null,
    onPdfReady: ((ParentReadyPdfDialogData) -> Unit)? = null
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language

    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var monthOnly by remember { mutableStateOf(false) }
    var previewState by remember { mutableStateOf<Pair<ChristianWorshipRecord, Int>?>(null) }
    var readyPdfFile by remember { mutableStateOf<File?>(null) }
    var pendingChristianPdfReward by remember { mutableStateOf(false) }

    val periodLabel = remember(monthOnly, isEn) {
        if (monthOnly) currentChristianMonthLabel(isEn) else if (isEn) "All History" else "Semua Riwayat"
    }

    val activeRecords = remember(records, monthOnly) {
        records.filter { !it.isDeleted && (!monthOnly || isChristianMillisInCurrentMonth(it.activityDate)) }
            .sortedWith(compareByDescending<ChristianWorshipRecord> { it.activityDate }.thenByDescending { it.createdAt })
    }

    val filteredRecords = remember(activeRecords, selectedTypeFilter) {
        if (selectedTypeFilter == "ALL") activeRecords
        else activeRecords.filter { ChristianWorshipHelper.normalizeType(it.activityType) == selectedTypeFilter }
    }

    val pendingPhotoRecords = remember(activeRecords, localSavesByRecordId) {
        activeRecords.filter { rec ->
            ChristianWorshipHelper.hasRemotePhoto(rec) &&
                !ChristianWorshipHelper.isProofExpired(rec) &&
                !ChristianWorshipHelper.hasSavedAllPhotosLocally(rec, localSavesByRecordId[rec.id])
        }
    }

    val pagiCount = activeRecords.count { ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_PAGI }
    val malamCount = activeRecords.count { ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_DOA_MALAM }
    val gerejaCount = activeRecords.count { ChristianWorshipHelper.normalizeType(it.activityType) == ChristianWorshipRecord.TYPE_KE_GEREJA }
    val extraCount = activeRecords.count { ChristianWorshipHelper.isExtraType(it.activityType) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_christian_content")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("parent_christian_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "parent_chris_hero") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, Color(0xFF818CF8))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF3730A3), Color(0xFF1D4ED8))
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Church,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isEn) "Christian Worship & Growth" else "Ibadah & Pembinaan Kristen",
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = student?.name ?: if (isEn) "Your Child" else "Ananda",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.88f)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Text(
                                        text = if (isEn) "Auto" else "Otomatis",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ChristianParentStatPill(
                                label = if (isEn) "Morning" else "Pagi",
                                value = "$pagiCount",
                                modifier = Modifier.weight(1f)
                            )
                            ChristianParentStatPill(
                                label = if (isEn) "Evening" else "Malam",
                                value = "$malamCount",
                                modifier = Modifier.weight(1f)
                            )
                            ChristianParentStatPill(
                                label = if (isEn) "Church" else "Gereja",
                                value = "$gerejaCount",
                                modifier = Modifier.weight(1f)
                            )
                            ChristianParentStatPill(
                                label = if (isEn) "Nurture" else "Bina",
                                value = "$extraCount",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            fun executeChristianPdfExport() {
                                val filterSummaryTitle = buildString {
                                    append(student?.name.orEmpty())
                                    if (periodLabel.isNotBlank()) {
                                        if (isNotEmpty()) append(" • ")
                                        append(periodLabel)
                                    }
                                }
                                val pdf = PdfReportGenerator.generateChristianWorshipPdf(
                                    context = context,
                                    students = listOfNotNull(student),
                                    records = filteredRecords,
                                    schoolProfile = schoolProfile,
                                    filterTitle = filterSummaryTitle,
                                    waliName = waliName
                                )
                                if (pdf != null) {
                                    val ready = ParentReadyPdfDialogData(
                                        file = pdf,
                                        title = if (isEn) "Worship Report PDF Ready" else "PDF Laporan Ibadah Siap",
                                        description = if (isEn) {
                                            "Open or share your child's Christian worship report PDF ($periodLabel)."
                                        } else {
                                            "Pilih buka PDF atau bagikan laporan ibadah ananda ($periodLabel)."
                                        },
                                        shareSubject = if (isEn) {
                                            "Christian Worship Report - ${student?.name.orEmpty()}"
                                        } else {
                                            "Laporan Ibadah Kristen - ${student?.name.orEmpty()}"
                                        },
                                        shareText = ChristianWorshipHelper.buildDailyWhatsappSummary(
                                            records = filteredRecords,
                                            students = listOfNotNull(student),
                                            filterTitle = filterSummaryTitle,
                                            waliName = waliName,
                                            isEn = isEn
                                        )
                                    )
                                    if (onPdfReady != null) {
                                        onPdfReady(ready)
                                    } else {
                                        readyPdfFile = pdf
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isEn) "Failed to generate PDF report." else "Gagal membuat laporan PDF.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            Button(
                                onClick = {
                                    if (TopOnAdManager.isRewardAdReady()) {
                                        pendingChristianPdfReward = true
                                    } else {
                                        TopOnAdManager.showRewardAdForPdfReport(context) {
                                            executeChristianPdfExport()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF1E3A8A)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_parent_christian_pdf")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEn) "Print PDF" else "Cetak PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            if (pendingPhotoRecords.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { onSaveAllPending(pendingPhotoRecords) },
                                    enabled = !isSavingMedia,
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_parent_christian_save_all")
                                ) {
                                    if (isSavingMedia) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                                    } else {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEn) "Save Photos (${pendingPhotoRecords.size})" else "Simpan Foto (${pendingPhotoRecords.size})",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (onOpenBible != null) {
                item(key = "parent_chris_bible_cta") {
                    Card(
                        onClick = {
                            TopOnAdManager.onUserActionClick(context)
                            onOpenBible()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_parent_christian_open_bible"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF3)),
                        border = BorderStroke(1.2.dp, Color(0xFFE2C788))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_menu_bible),
                                contentDescription = if (isEn) "Digital Bible" else "Alkitab Digital",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) "Open Digital Bible" else "Buka Alkitab Digital",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF74470B)
                                )
                                Text(
                                    text = if (isEn) {
                                        "66 Books IND/ENG • Devotionals, Audio & Notes"
                                    } else {
                                        "66 Kitab IND/ENG • Renungan, Audio & Catatan"
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B7149),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = if (isEn) "66 Books" else "66 Kitab",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item(key = "parent_chris_filters") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = monthOnly,
                            onClick = {
                                TopOnAdManager.onUserActionClick(context)
                                monthOnly = true
                            },
                            label = {
                                Text(
                                    text = if (isEn) "This Month" else "Bulan Ini",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            modifier = Modifier.testTag("btn_parent_christian_pdf_filter_month")
                        )
                        FilterChip(
                            selected = !monthOnly,
                            onClick = {
                                TopOnAdManager.onUserActionClick(context)
                                monthOnly = false
                            },
                            label = {
                                Text(
                                    text = if (isEn) "All History" else "Semua Riwayat",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            modifier = Modifier.testTag("btn_parent_christian_pdf_filter_all")
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedTypeFilter == "ALL",
                            onClick = {
                                TopOnAdManager.onUserActionClick(context)
                                selectedTypeFilter = "ALL"
                            },
                            label = { Text(if (isEn) "All (${activeRecords.size})" else "Semua (${activeRecords.size})", fontSize = 11.5.sp) }
                        )
                        ChristianWorshipHelper.ALL_TYPES.forEach { type ->
                            FilterChip(
                                selected = selectedTypeFilter == type,
                                onClick = {
                                    TopOnAdManager.onUserActionClick(context)
                                    selectedTypeFilter = type
                                },
                                label = { Text(ChristianWorshipHelper.activityShortLabel(type, isEn), fontSize = 11.5.sp) }
                            )
                        }
                    }
                }
            }

            if (filteredRecords.isEmpty()) {
                item(key = "parent_chris_empty") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Church,
                                contentDescription = null,
                                tint = Color(0xFF4338CA),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = if (isEn) {
                                    "No Christian worship activity recorded yet"
                                } else {
                                    "Belum ada laporan ibadah Kristen tercatat"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEn) {
                                    "Morning prayer, evening prayer, church service, and memory verses logged by Wali Asuh will appear here automatically."
                                } else {
                                    "Laporan Ibadah Pagi, Ibadah Malam, Kegiatan Gereja, dan Ayat Hafalan dari Wali Asuh akan tampil otomatis di sini."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredRecords, key = { "parent_chris_${it.id}" }) { rec ->
                    val localSave = localSavesByRecordId[rec.id]
                    val photoEntries = ChristianWorshipHelper.extractPhotoEntries(rec, localSave)
                    val isExpired = ChristianWorshipHelper.isProofExpired(rec)
                    val remDays = ChristianWorshipHelper.remainingDays(rec)
                    val allSavedLocally = ChristianWorshipHelper.hasSavedAllPhotosLocally(rec, localSave)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_parent_christian_${rec.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE0E7FF)
                                ) {
                                    Text(
                                        text = ChristianWorshipHelper.activityLabel(rec.activityType, isEn),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3730A3),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = DateUtils.formatShortDate(rec.activityDate, lang),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (rec.title.isNotBlank()) {
                                Text(
                                    text = rec.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (rec.notes.isNotBlank()) {
                                Text(
                                    text = rec.notes,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (photoEntries.isNotEmpty() && (!isExpired || allSavedLocally)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    photoEntries.forEachIndexed { idx, photoItem ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(if (photoEntries.size == 1) 170.dp else 145.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { previewState = rec to idx }
                                                .testTag("img_parent_christian_photo_${rec.id}_$idx")
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(model = photoItem.primaryModel),
                                                contentDescription = ChristianWorshipHelper.activityLabel(rec.activityType, isEn),
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(999.dp),
                                                color = Color.Black.copy(alpha = 0.55f),
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                                    val badgeText = when {
                                                        photoEntries.size > 1 -> "${idx + 1}/${photoEntries.size}"
                                                        remDays > 0L -> if (isEn) "${remDays}d photo" else "Aktif ${remDays}h"
                                                        else -> if (isEn) "Photo" else "Foto"
                                                    }
                                                    Text(
                                                        text = badgeText,
                                                        fontSize = 10.sp,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (allSavedLocally) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(14.dp))
                                            Text(
                                                text = if (photoEntries.size > 1) {
                                                    if (isEn) "${photoEntries.size} Photos Saved in Phone Gallery"
                                                    else "Tersimpan di Galeri HP (${photoEntries.size} Foto)"
                                                } else {
                                                    if (isEn) "Saved to Phone Gallery" else "Sudah Tersimpan di Galeri HP"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF15803D)
                                            )
                                        }
                                    }
                                } else if (ChristianWorshipHelper.hasRemotePhoto(rec)) {
                                    OutlinedButton(
                                        onClick = { onSavePhotoToDevice(rec, false) },
                                        enabled = !isSavingMedia,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .testTag("btn_parent_save_chris_photo_${rec.id}")
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (photoEntries.size > 1) {
                                                if (isEn) "Save ${photoEntries.size} Photos to Phone Gallery"
                                                else "Simpan ${photoEntries.size} Foto ke Galeri HP"
                                            } else {
                                                if (isEn) "Save Photo to Phone Gallery" else "Simpan Foto ke Galeri HP"
                                            },
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            } else if (rec.isMediaPurgedFromServer || isExpired) {
                                Text(
                                    text = if (isEn) "Server photo retention (90 days) ended." else "Foto arsip server (masa simpan 90 hari selesai).",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "${if (isEn) "Logged by" else "Pembina"}: ${rec.waliName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    previewState?.let { (rec, initialIdx) ->
        val localSave = localSavesByRecordId[rec.id]
        val entries = ChristianWorshipHelper.extractPhotoEntries(rec, localSave)
        var activeIdx by remember(rec.id, initialIdx) {
            mutableStateOf(initialIdx.coerceIn(0, (entries.size - 1).coerceAtLeast(0)))
        }
        val activeEntry = entries.getOrNull(activeIdx)
        val fallbackModel = activeEntry?.fallbackUrl?.takeIf { it.isNotBlank() }
            ?: rec.photoFallbackUrl.takeIf { it.isNotBlank() }
        var useFallback by remember(rec.id, activeIdx) { mutableStateOf(false) }
        val primaryCandidate: Any? = activeEntry?.primaryModel
            ?: localSave?.localGalleryUri?.takeIf { it.isNotBlank() }
            ?: rec.localPhotoPath.takeIf { it.isNotBlank() }
            ?: rec.photoUrl.takeIf { it.isNotBlank() }
            ?: fallbackModel
        val activeModel: Any? = if (useFallback && !fallbackModel.isNullOrBlank()) fallbackModel else primaryCandidate

        var scale by remember(rec.id, activeIdx) { mutableFloatStateOf(1f) }
        var offset by remember(rec.id, activeIdx) { mutableStateOf(Offset.Zero) }
        var containerSize by remember { mutableStateOf(IntSize.Zero) }

        Dialog(
            onDismissRequest = { previewState = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            ConfigureEdgeToEdgeDialogWindow(
                lightStatusBars = false,
                lightNavigationBars = false
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.96f))
                    .clipToBounds()
                    .onSizeChanged { containerSize = it }
                    .pointerInput(rec.id, activeIdx) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val oldScale = scale
                            val newScale = (oldScale * zoom).coerceIn(1f, 5f)
                            scale = newScale
                            if (newScale <= 1.01f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                val scaleFactor = if (oldScale > 0f) newScale / oldScale else 1f
                                val center = Offset(containerSize.width / 2f, containerSize.height / 2f)
                                val centroidFromCenter = centroid - center
                                val maxX = (containerSize.width * (newScale - 1f)) / 2f
                                val maxY = (containerSize.height * (newScale - 1f)) / 2f
                                val nextOffset = (offset - centroidFromCenter) * scaleFactor + centroidFromCenter + pan
                                offset = Offset(
                                    x = nextOffset.x.coerceIn(-maxX, maxX),
                                    y = nextOffset.y.coerceIn(-maxY, maxY)
                                )
                            }
                        }
                    }
                    .pointerInput(rec.id, activeIdx) {
                        detectTapGestures(
                            onDoubleTap = { tapOffset ->
                                if (scale > 1.2f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    val targetScale = 2.5f
                                    val center = Offset(containerSize.width / 2f, containerSize.height / 2f)
                                    val tapFromCenter = tapOffset - center
                                    val maxX = (containerSize.width * (targetScale - 1f)) / 2f
                                    val maxY = (containerSize.height * (targetScale - 1f)) / 2f
                                    scale = targetScale
                                    offset = Offset(
                                        x = (-tapFromCenter.x * (targetScale - 1f)).coerceIn(-maxX, maxX),
                                        y = (-tapFromCenter.y * (targetScale - 1f)).coerceIn(-maxY, maxY)
                                    )
                                }
                            }
                        )
                    }
            ) {
                if (activeModel != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(activeModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = rec.title.ifBlank { rec.studentName },
                        contentScale = ContentScale.Fit,
                        onError = {
                            if (!useFallback && !fallbackModel.isNullOrBlank() && activeModel != fallbackModel) {
                                useFallback = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                            .testTag("img_parent_christian_preview_zoomable")
                    )
                }

                // Header bar dengan judul kegiatan, indikator zoom, dan tombol tutup (min 48.dp)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = rec.title.ifBlank { ChristianWorshipHelper.activityLabel(rec.activityType, isEn) },
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = DateUtils.formatShortDate(rec.activityDate, lang),
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 11.5.sp,
                            maxLines = 1
                        )
                    }

                    if (scale > 1.05f) {
                        Surface(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            },
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.22f),
                            modifier = Modifier
                                .heightIn(min = 36.dp)
                                .testTag("btn_reset_parent_christian_zoom")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomOutMap,
                                    contentDescription = if (isEn) "Reset zoom" else "Reset zoom",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${(scale * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { previewState = null },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_parent_christian_preview")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close" else "Tutup",
                            tint = Color.White
                        )
                    }
                }

                // Footer: petunjuk zoom 2 jari + tombol ganti Foto 1 / Foto 2
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (scale <= 1.05f) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.Black.copy(alpha = 0.58f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isEn) "Pinch with 2 fingers or double-tap to zoom" else "Cubit 2 jari atau ketuk 2x untuk zoom",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (entries.size > 1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            entries.forEachIndexed { idx, _ ->
                                val isSelected = idx == activeIdx
                                Button(
                                    onClick = { activeIdx = idx },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF4F46E5) else Color.White.copy(alpha = 0.22f),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_parent_christian_preview_photo_$idx")
                                ) {
                                    Text(
                                        text = if (isEn) "Photo ${idx + 1}" else "Foto ${idx + 1}",
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

    if (pendingChristianPdfReward) {
        RewardAdDeclarationDialog(
            title = if (isEn) "Download Christian Worship PDF" else "Unduh Laporan Ibadah Kristen PDF",
            actionDescription = if (isEn) {
                "download and open your child's Christian worship PDF report"
            } else {
                "mengunduh dan membuka laporan PDF ibadah Kristen ananda"
            },
            confirmButtonText = if (isEn) "Watch & Download PDF" else "Tonton & Unduh PDF",
            onConfirmWatchAd = {
                pendingChristianPdfReward = false
                TopOnAdManager.showRewardAdForPdfReport(context) {
                    val filterSummaryTitle = buildString {
                        append(student?.name.orEmpty())
                        if (periodLabel.isNotBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append(periodLabel)
                        }
                    }
                    val pdf = PdfReportGenerator.generateChristianWorshipPdf(
                        context = context,
                        students = listOfNotNull(student),
                        records = filteredRecords,
                        schoolProfile = schoolProfile,
                        filterTitle = filterSummaryTitle,
                        waliName = waliName
                    )
                    if (pdf != null) {
                        val ready = ParentReadyPdfDialogData(
                            file = pdf,
                            title = if (isEn) "Worship Report PDF Ready" else "PDF Laporan Ibadah Siap",
                            description = if (isEn) {
                                "Open or share your child's Christian worship report PDF ($periodLabel)."
                            } else {
                                "Pilih buka PDF atau bagikan laporan ibadah ananda ($periodLabel)."
                            },
                            shareSubject = if (isEn) {
                                "Christian Worship Report - ${student?.name.orEmpty()}"
                            } else {
                                "Laporan Ibadah Kristen - ${student?.name.orEmpty()}"
                            },
                            shareText = ChristianWorshipHelper.buildDailyWhatsappSummary(
                                records = filteredRecords,
                                students = listOfNotNull(student),
                                filterTitle = filterSummaryTitle,
                                waliName = waliName,
                                isEn = isEn
                            )
                        )
                        if (onPdfReady != null) {
                            onPdfReady(ready)
                        } else {
                            readyPdfFile = pdf
                        }
                    } else {
                        Toast.makeText(
                            context,
                            if (isEn) "Failed to generate PDF report." else "Gagal membuat laporan PDF.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            onDismiss = { pendingChristianPdfReward = false }
        )
    }

    readyPdfFile?.let { file ->
        AlertDialog(
            onDismissRequest = { readyPdfFile = null },
            title = { Text(if (isEn) "Worship Report PDF Ready" else "PDF Laporan Ibadah Siap") },
            text = {
                Text(
                    if (isEn) "Open or share your child's Christian worship report PDF."
                    else "Pilih buka PDF atau bagikan laporan ibadah ananda."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        PdfReportGenerator.openPdfFile(context, file)
                        readyPdfFile = null
                    }
                ) {
                    Text(if (isEn) "Open PDF" else "Buka PDF")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            PdfReportGenerator.shareChristianWorshipPdf(
                                context = context,
                                pdfFile = file,
                                subject = if (isEn) "Christian Worship Report - ${student?.name.orEmpty()}" else "Laporan Ibadah Kristen - ${student?.name.orEmpty()}",
                                text = ChristianWorshipHelper.buildDailyWhatsappSummary(
                                    records = filteredRecords,
                                    students = listOfNotNull(student),
                                    filterTitle = student?.name.orEmpty(),
                                    waliName = waliName,
                                    isEn = isEn
                                )
                            )
                            readyPdfFile = null
                        }
                    ) {
                        Text(if (isEn) "Share" else "Bagikan")
                    }
                    OutlinedButton(onClick = { readyPdfFile = null }) {
                        Text(if (isEn) "Close" else "Tutup")
                    }
                }
            }
        )
    }
}

@Composable
private fun ChristianParentStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.88f), maxLines = 1)
        }
    }
}
