package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.ChildStudent
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipLocalSave
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.SchoolProfile
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import com.example.util.i18n.LocalAppStrings
import java.io.File

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
    onSaveAllPending: (List<ChristianWorshipRecord>) -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language

    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var previewRecord by remember { mutableStateOf<ChristianWorshipRecord?>(null) }
    var readyPdfFile by remember { mutableStateOf<File?>(null) }

    val activeRecords = remember(records) {
        records.filter { !it.isDeleted }
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
                localSavesByRecordId[rec.id] == null
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
                            Button(
                                onClick = {
                                    val pdf = PdfReportGenerator.generateChristianWorshipPdf(
                                        context = context,
                                        students = listOfNotNull(student),
                                        records = filteredRecords,
                                        schoolProfile = schoolProfile,
                                        filterTitle = student?.name.orEmpty(),
                                        waliName = waliName
                                    )
                                    if (pdf != null) {
                                        readyPdfFile = pdf
                                    } else {
                                        Toast.makeText(
                                            context,
                                            if (isEn) "Failed to generate PDF report." else "Gagal membuat laporan PDF.",
                                            Toast.LENGTH_SHORT
                                        ).show()
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

            item(key = "parent_chris_filters") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedTypeFilter == "ALL",
                        onClick = { selectedTypeFilter = "ALL" },
                        label = { Text(if (isEn) "All (${activeRecords.size})" else "Semua (${activeRecords.size})", fontSize = 11.5.sp) }
                    )
                    ChristianWorshipHelper.ALL_TYPES.forEach { type ->
                        FilterChip(
                            selected = selectedTypeFilter == type,
                            onClick = { selectedTypeFilter = type },
                            label = { Text(ChristianWorshipHelper.activityShortLabel(type, isEn), fontSize = 11.5.sp) }
                        )
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
                    val hasPhoto = ChristianWorshipHelper.hasAnyPhoto(rec)
                    val isExpired = ChristianWorshipHelper.isProofExpired(rec)
                    val remDays = ChristianWorshipHelper.remainingDays(rec)
                    val photoModel: Any? = localSave?.localGalleryUri?.takeIf { it.isNotBlank() }
                        ?: rec.localPhotoPath.takeIf { it.isNotBlank() }
                        ?: rec.photoUrl.takeIf { it.isNotBlank() }
                        ?: rec.photoFallbackUrl.takeIf { it.isNotBlank() }

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

                            if (hasPhoto && !isExpired && photoModel != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { previewRecord = rec }
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = photoModel),
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
                                            Text(
                                                text = if (remDays > 0L) {
                                                    if (isEn) "${remDays}d photo" else "Aktif ${remDays}h"
                                                } else if (isEn) "Photo" else "Foto",
                                                fontSize = 10.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                if (localSave != null) {
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
                                                text = if (isEn) "Saved to Phone Gallery" else "Sudah Tersimpan di Galeri HP",
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
                                            .heightIn(min = 44.dp)
                                            .testTag("btn_parent_save_chris_photo_${rec.id}")
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isEn) "Save Photo to Phone Gallery" else "Simpan Foto ke Galeri HP", fontSize = 12.sp)
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

    previewRecord?.let { rec ->
        val imgModel: Any? = localSavesByRecordId[rec.id]?.localGalleryUri?.takeIf { it.isNotBlank() }
            ?: rec.localPhotoPath.takeIf { it.isNotBlank() }
            ?: rec.photoUrl.takeIf { it.isNotBlank() }
            ?: rec.photoFallbackUrl.takeIf { it.isNotBlank() }
        Dialog(
            onDismissRequest = { previewRecord = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .padding(16.dp)
            ) {
                if (imgModel != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imgModel),
                        contentDescription = rec.title.ifBlank { rec.studentName },
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                IconButton(
                    onClick = { previewRecord = null },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup", tint = Color.White)
                }
            }
        }
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
