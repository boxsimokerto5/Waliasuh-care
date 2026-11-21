package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.SchoolProfile
import com.example.data.model.WaliGalleryHelper
import com.example.data.model.WaliGalleryLocalSave
import com.example.data.model.WaliGalleryPost
import com.example.ui.components.ParentMenuPdfHeaderCard
import com.example.ui.components.ParentReadyPdfDialogData
import com.example.ui.dialogs.GalleryFullscreenPreviewDialog
import com.example.ui.dialogs.GalleryRemoteOrLocalImage
import com.example.util.DateUtils
import com.example.util.ParentPortalPdfHelper
import com.example.util.PdfReportGenerator
import com.example.util.i18n.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun isGalleryMillisInCurrentMonth(millis: Long): Boolean {
    if (millis <= 0L) return false
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = millis }
    return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        now.get(Calendar.MONTH) == target.get(Calendar.MONTH)
}

private fun currentGalleryMonthLabel(isEn: Boolean): String {
    val locale = if (isEn) Locale.US else Locale("id", "ID")
    return SimpleDateFormat("MMMM yyyy", locale).format(Date())
}

@Composable
fun ParentGalleryContent(
    posts: List<WaliGalleryPost>,
    localSavesByPostId: Map<Long, WaliGalleryLocalSave>,
    waliName: String,
    autoSaveEnabled: Boolean,
    isSavingMedia: Boolean,
    onToggleAutoSave: (Boolean) -> Unit,
    onSavePostToDevice: (WaliGalleryPost, Boolean) -> Unit,
    onSaveAllPending: (List<WaliGalleryPost>) -> Unit,
    student: ChildStudent? = null,
    schoolProfile: SchoolProfile = SchoolProfile(),
    onPdfReady: ((ParentReadyPdfDialogData) -> Unit)? = null
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language

    var previewData by remember { mutableStateOf<Triple<WaliGalleryPost, String, String>?>(null) }
    var monthOnly by remember { mutableStateOf(false) }
    val displayedPosts = remember(posts, monthOnly) {
        if (monthOnly) posts.filter { isGalleryMillisInCurrentMonth(it.createdAt) } else posts
    }
    val periodLabel = remember(monthOnly, isEn) {
        if (monthOnly) currentGalleryMonthLabel(isEn) else if (isEn) "All History" else "Semua Riwayat"
    }

    val pendingDownloadPosts = remember(posts, localSavesByPostId) {
        posts.filter { post ->
            val saved = localSavesByPostId[post.id]
            saved == null && !post.isMediaPurgedFromServer &&
                (post.photoUrl.isNotBlank() || post.photoFallbackUrl.isNotBlank())
        }
    }

    // Mode WhatsApp: otomatis unduh & simpan ke Galeri HP Orang Tua saat tab dibuka
    LaunchedEffect(autoSaveEnabled, pendingDownloadPosts.size) {
        if (autoSaveEnabled && pendingDownloadPosts.isNotEmpty()) {
            pendingDownloadPosts.take(5).forEach { post ->
                onSavePostToDevice(post, true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_gallery_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "parent_gallery_header") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                border = BorderStroke(1.dp, Color(0xFF99F6E4))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Caregiver Activity Gallery" else "Galeri Wali Asuh",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F766E)
                            )
                            Text(
                                text = if (waliName.isNotBlank()) {
                                    if (isEn) "Photos shared by $waliName" else "Momen kiriman $waliName"
                                } else {
                                    if (isEn) "Photos from your child's caregiver" else "Momen kegiatan dari Wali Asuh ananda"
                                },
                                fontSize = 11.5.sp,
                                color = Color(0xFF134E4A)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F766E)
                        ) {
                            Text(
                                text = "${posts.size} ${if (isEn) "Photos" else "Foto"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Auto-save to Phone Gallery" else "Simpan Otomatis ke Galeri HP",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF134E4A)
                            )
                            Text(
                                text = if (isEn) {
                                    "Works like WhatsApp: saved locally to Pictures/WaliAsuhCare so server quota stays light."
                                } else {
                                    "Seperti WhatsApp: otomatis disimpan ke Pictures/WaliAsuhCare agar tetap ada walau file sementara di server dibersihkan."
                                },
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp,
                                color = Color(0xFF115E59)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = autoSaveEnabled,
                            onCheckedChange = onToggleAutoSave,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF0F766E)
                            ),
                            modifier = Modifier.testTag("switch_parent_gallery_autosave")
                        )
                    }

                    if (pendingDownloadPosts.isNotEmpty()) {
                        Button(
                            onClick = { onSaveAllPending(pendingDownloadPosts) },
                            enabled = !isSavingMedia,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_parent_save_all_gallery")
                        ) {
                            if (isSavingMedia) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isEn) "Saving to Phone Gallery..." else "Menyimpan ke Galeri HP...")
                            } else {
                                Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEn) {
                                        "Save All (${pendingDownloadPosts.size}) to Phone Gallery"
                                    } else {
                                        "Simpan Semua (${pendingDownloadPosts.size}) ke Galeri HP"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (student != null) {
            item(key = "parent_gallery_pdf_header") {
                val savedCount = displayedPosts.count { localSavesByPostId[it.id]?.localGalleryUri?.isNotBlank() == true }
                ParentMenuPdfHeaderCard(
                    title = if (isEn) "Caregiver Gallery Album PDF" else "Album Dokumentasi Galeri Wali",
                    subtitle = if (isEn) {
                        "${displayedPosts.size} photo(s) • $savedCount saved locally ($periodLabel)"
                    } else {
                        "${displayedPosts.size} foto • $savedCount tersimpan di HP ($periodLabel)"
                    },
                    accentColor = Color(0xFF0F766E),
                    containerColor = Color(0xFFF0FDFA),
                    borderColor = Color(0xFF99F6E4),
                    isEn = isEn,
                    selectedMonthOnly = monthOnly,
                    onToggleMonthOnly = { monthOnly = it },
                    pdfButtonTestTag = "btn_parent_gallery_pdf",
                    onDownloadPdf = {
                        val file = ParentPortalPdfHelper.generateStudentGalleryReportPdf(
                            context = context,
                            student = student,
                            posts = displayedPosts,
                            localSavesByPostId = localSavesByPostId,
                            periodLabel = periodLabel,
                            waliName = waliName.ifBlank { "Wali Asuh" },
                            schoolProfile = schoolProfile
                        )
                        if (file != null) {
                            val ready = ParentReadyPdfDialogData(
                                file = file,
                                title = if (isEn) "Gallery Album PDF Ready" else "PDF Album Galeri Siap",
                                description = if (isEn) {
                                    "Open or share the activity gallery documentation PDF for ${student.name}."
                                } else {
                                    "Pilih buka PDF atau bagikan album dokumentasi galeri wali untuk ${student.name}."
                                },
                                shareSubject = if (isEn) "Caregiver Gallery Report: ${student.name}" else "Laporan Galeri Wali: ${student.name}",
                                shareText = if (isEn) {
                                    "Attached caregiver gallery documentation PDF for ${student.name} ($periodLabel)."
                                } else {
                                    "Terlampir PDF dokumentasi galeri wali asuh ${student.name} ($periodLabel)."
                                }
                            )
                            if (onPdfReady != null) {
                                onPdfReady(ready)
                            } else {
                                PdfReportGenerator.openPdfFile(context, file)
                            }
                        } else {
                            Toast.makeText(
                                context,
                                if (isEn) "Unable to create Gallery PDF" else "Gagal membuat PDF Galeri Wali",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }

        if (displayedPosts.isEmpty()) {
            item(key = "parent_gallery_empty") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_parent_gallery"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = Color(0xFF0F766E),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = if (monthOnly && posts.isNotEmpty()) {
                                if (isEn) "No Gallery Photos This Month" else "Belum Ada Foto Bulan Ini"
                            } else {
                                if (isEn) "No Gallery Photos Yet" else "Belum Ada Foto dari Wali Asuh"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (monthOnly && posts.isNotEmpty()) {
                                if (isEn) {
                                    "Switch to \"All History\" above to view photos from previous months."
                                } else {
                                    "Pilih \"Semua Riwayat\" di atas untuk melihat dokumentasi bulan sebelumnya."
                                }
                            } else if (isEn) {
                                "When your child's foster caregiver posts activity photos, they will appear here and save to your gallery."
                            } else {
                                "Saat Wali Asuh ananda membagikan foto kegiatan, fotonya akan muncul di sini dan langsung dapat tersimpan ke Galeri HP Anda."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(displayedPosts, key = { "parent_gal_${it.id}" }) { post ->
                val localSave = localSavesByPostId[post.id]
                val localUri = localSave?.localGalleryUri.orEmpty()
                val isSavedLocally = localUri.isNotBlank()
                val primaryDisplayUrl = if (isSavedLocally) localUri else post.photoUrl
                val fallbackDisplayUrl = if (isSavedLocally) post.photoUrl else post.photoFallbackUrl
                val canViewImage = primaryDisplayUrl.isNotBlank() || fallbackDisplayUrl.isNotBlank()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_parent_gallery_post_${post.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFCCFBF1)
                                ) {
                                    Text(
                                        text = WaliGalleryHelper.tagLabel(post.activityTag, isEn),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F766E),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = post.waliName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = DateUtils.formatDateTime(post.createdAt, lang),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (canViewImage) {
                            GalleryRemoteOrLocalImage(
                                primaryUrl = primaryDisplayUrl,
                                fallbackUrl = fallbackDisplayUrl,
                                contentDescription = post.caption.ifBlank { post.waliName },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 10f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        previewData = Triple(post, primaryDisplayUrl, fallbackDisplayUrl)
                                    }
                            )
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Temporary media on server has expired/been cleared to save storage."
                                        } else {
                                            "Foto sementara di server sudah dibersihkan/kedaluwarsa karena melewati masa simpan untuk menghemat penyimpanan."
                                        },
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }

                        if (post.caption.isNotBlank()) {
                            Text(
                                text = post.caption,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (isSavedLocally) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (isEn) "Saved in Phone Gallery" else "Tersimpan di Galeri HP ✓",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                }
                            } else if (!post.isMediaPurgedFromServer && canViewImage) {
                                OutlinedButton(
                                    onClick = { onSavePostToDevice(post, false) },
                                    enabled = !isSavingMedia,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF0F766E)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_save_post_to_gallery_${post.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = Color(0xFF0F766E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEn) "Save to Phone Gallery" else "Simpan ke Galeri HP",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F766E)
                                    )
                                }
                            } else {
                                Text(
                                    text = if (isEn) "Unavailable" else "Sudah tidak di server",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (!post.isMediaPurgedFromServer) {
                                Text(
                                    text = WaliGalleryHelper.remainingDaysText(post.expiresAt, isEn = isEn),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    previewData?.let { (post, pri, fb) ->
        GalleryFullscreenPreviewDialog(
            title = post.waliName,
            caption = post.caption,
            primaryUrl = pri,
            fallbackUrl = fb,
            onDismiss = { previewData = null }
        )
    }
}
