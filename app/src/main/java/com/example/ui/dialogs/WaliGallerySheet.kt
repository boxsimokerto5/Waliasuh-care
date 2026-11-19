package com.example.ui.dialogs

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.model.ChildStudent
import com.example.data.model.WaliGalleryHelper
import com.example.data.model.WaliGalleryPost
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaliGallerySheet(
    posts: List<WaliGalleryPost>,
    myStudents: List<ChildStudent>,
    currentWaliUsername: String,
    currentWaliName: String,
    isAdminOrSuper: Boolean,
    isPublishing: Boolean,
    onPublishPost: (
        imageUri: Uri,
        caption: String,
        activityTag: String,
        targetScope: String,
        selectedStudentIds: Set<Long>,
        retentionDays: Int,
        onDone: (Boolean, String) -> Unit
    ) -> Unit,
    onPurgeServerMedia: (WaliGalleryPost, (Boolean, String) -> Unit) -> Unit,
    onDeletePost: (WaliGalleryPost, (Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language

    var showCreateDialog by remember { mutableStateOf(false) }
    var previewPost by remember { mutableStateOf<WaliGalleryPost?>(null) }
    var postPendingDelete by remember { mutableStateOf<WaliGalleryPost?>(null) }

    val visiblePosts = remember(posts, currentWaliUsername, currentWaliName, isAdminOrSuper) {
        posts.filter { post ->
            !post.isDeleted && (
                isAdminOrSuper || WaliGalleryHelper.matchesWaliOwner(post, currentWaliUsername, currentWaliName)
                )
        }.sortedByDescending { it.createdAt }
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
                    .widthIn(max = 620.dp)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .testTag("sheet_wali_gallery"),
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFCCFBF1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "Caregiver Activity Gallery" else "Galeri Wali Asuh",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEn) {
                                    "Temporary R2 relay -> auto-saved to parents' phone galleries"
                                } else {
                                    "Relay R2 sementara -> tersimpan di Galeri HP Orang Tua"
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                    }
                }

                // Info efisiensi penyimpanan R2 + PocketBase
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                    border = BorderStroke(1.dp, Color(0xFF99F6E4))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(11.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = Color(0xFF0F766E),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isEn) {
                                "Photos are compressed (~150KB), relayed via PocketBase + Cloudflare R2, and auto-saved into each matching parent's phone gallery. Server media is automatically cleared once all parents download or when retention expires."
                            } else {
                                "Foto dikompresi otomatis (~150KB), ditampung sementara di PocketBase + Cloudflare R2, dan langsung tersimpan ke Galeri HP orang tua yang memiliki wali asuh sama. File di server otomatis dibersihkan saat masa aktif habis atau semua ortu sudah mengunduh."
                            },
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = Color(0xFF134E4A),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Button(
                    onClick = { showCreateDialog = true },
                    enabled = !isPublishing,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("btn_open_create_wali_gallery")
                ) {
                    if (isPublishing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEn) "Compressing & Uploading..." else "Mengompresi & Mengunggah...",
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEn) "Post Activity Photo to Parents" else "Posting Foto Kegiatan ke Orang Tua",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 200.dp, max = 460.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (visiblePosts.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .testTag("empty_wali_gallery_posts"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = Color(0xFF0F766E),
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Text(
                                        text = if (isEn) "No Activity Photos Yet" else "Belum Ada Foto Galeri",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Share joyful moments of your foster kids. Only parents of your foster students will receive them."
                                        } else {
                                            "Bagikan momen kegiatan anak asuh. Foto hanya muncul di Portal Orang Tua dari siswa binaan Anda."
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(visiblePosts, key = { "wali_gal_${it.id}" }) { post ->
                            val downloadedCount = WaliGalleryHelper.downloadedCount(post)
                            val expectedCount = post.expectedRecipientCount
                            val progress = if (expectedCount > 0) {
                                (downloadedCount.toFloat() / expectedCount.toFloat()).coerceIn(0f, 1f)
                            } else 0f

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_wali_gallery_post_${post.id}"),
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
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
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
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (post.isMediaPurgedFromServer) Color(0xFFF1F5F9) else Color(0xFFDBEAFE)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (post.isMediaPurgedFromServer) Icons.Default.CloudOff else Icons.Default.Schedule,
                                                        contentDescription = null,
                                                        tint = if (post.isMediaPurgedFromServer) Color(0xFF64748B) else Color(0xFF1D4ED8),
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Text(
                                                        text = if (post.isMediaPurgedFromServer) {
                                                            if (isEn) "Server file cleared" else "File server dibersihkan"
                                                        } else {
                                                            WaliGalleryHelper.remainingDaysText(post.expiresAt, isEn = isEn)
                                                        },
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (post.isMediaPurgedFromServer) Color(0xFF475569) else Color(0xFF1D4ED8)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = DateUtils.formatDateTime(post.createdAt, lang),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (!post.isMediaPurgedFromServer && (post.photoUrl.isNotBlank() || post.photoFallbackUrl.isNotBlank())) {
                                        GalleryRemoteOrLocalImage(
                                            primaryUrl = post.photoUrl,
                                            fallbackUrl = post.photoFallbackUrl,
                                            contentDescription = post.caption.ifBlank { "Galeri Wali Asuh" },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(16f / 10f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { previewPost = post }
                                        )
                                    } else {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CleaningServices,
                                                    contentDescription = null,
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = if (isEn) {
                                                        "Server copy cleared to save storage; parents who downloaded still keep it in their phone gallery."
                                                    } else {
                                                        "File sementara di server/R2 sudah dibersihkan untuk hemat kuota. Orang tua yang sudah mengunduh tetap memilikinya di Galeri HP."
                                                    },
                                                    fontSize = 11.sp,
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

                                    // Progress bar penerimaan orang tua
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF059669),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = if (expectedCount > 0) {
                                                        if (isEn) "Saved on $downloadedCount / $expectedCount parent devices"
                                                        else "Tersimpan di $downloadedCount / $expectedCount Galeri HP Ortu"
                                                    } else {
                                                        if (isEn) "Saved on $downloadedCount parent device(s)"
                                                        else "Tersimpan di $downloadedCount Galeri HP Ortu"
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF047857)
                                                )
                                            }
                                            Text(
                                                text = if (post.targetScope == WaliGalleryHelper.SCOPE_SPECIFIC_STUDENTS) {
                                                    if (isEn) "Selected Kids" else "Siswa Terpilih"
                                                } else {
                                                    if (isEn) "All Foster Kids" else "Semua Anak Binaan"
                                                },
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (expectedCount > 0) {
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                color = Color(0xFF059669),
                                                trackColor = Color(0xFFD1FAE5),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(999.dp))
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (!post.isMediaPurgedFromServer) {
                                            OutlinedButton(
                                                onClick = {
                                                    onPurgeServerMedia(post) { _, msg ->
                                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.dp, Color(0xFF0F766E)),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("btn_purge_gallery_server_${post.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CleaningServices,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0F766E),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isEn) "Clear Server File" else "Hemat Server",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF0F766E),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { postPendingDelete = post },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_delete_gallery_post_${post.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = null,
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isEn) "Delete" else "Hapus Post",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFDC2626),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
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

    if (showCreateDialog) {
        CreateWaliGalleryPostDialog(
            myStudents = myStudents,
            isPublishing = isPublishing,
            onPublish = { uri, caption, tag, scope, selectedIds, days ->
                onPublishPost(uri, caption, tag, scope, selectedIds, days) { ok, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (ok) showCreateDialog = false
                }
            },
            onDismiss = { if (!isPublishing) showCreateDialog = false }
        )
    }

    previewPost?.let { post ->
        GalleryFullscreenPreviewDialog(
            title = post.waliName,
            caption = post.caption,
            primaryUrl = post.photoUrl,
            fallbackUrl = post.photoFallbackUrl,
            onDismiss = { previewPost = null }
        )
    }

    postPendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { postPendingDelete = null },
            title = { Text(if (isEn) "Delete Gallery Post?" else "Hapus Postingan Galeri?") },
            text = {
                Text(
                    if (isEn) {
                        "This removes the post from server/R2 and parent portals (parents who already saved to their phone gallery keep their local copy)."
                    } else {
                        "Postingan dan file di server/R2 akan dihapus dari daftar (orang tua yang sudah menyimpan ke Galeri HP tetap menyimpan salinannya)."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePost(target) { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        postPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(if (isEn) "Delete" else "Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { postPendingDelete = null }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }
}

@Composable
fun CreateWaliGalleryPostDialog(
    myStudents: List<ChildStudent>,
    isPublishing: Boolean,
    onPublish: (
        imageUri: Uri,
        caption: String,
        activityTag: String,
        targetScope: String,
        selectedStudentIds: Set<Long>,
        retentionDays: Int
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf(WaliGalleryHelper.TAG_KEGIATAN) }
    var targetScope by remember { mutableStateOf(WaliGalleryHelper.SCOPE_ALL_MY_STUDENTS) }
    var selectedStudentIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var retentionDays by remember { mutableIntStateOf(WaliGalleryHelper.DEFAULT_RETENTION_DAYS) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) selectedUri = uri }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .testTag("dialog_create_wali_gallery"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Post to Parent Gallery" else "Posting Galeri Wali Asuh",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, enabled = !isPublishing) {
                        Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                    }
                }

                // Zero-permission Photo Picker preview/button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = !isPublishing) {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("btn_pick_gallery_photo"),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0FDFA),
                    border = BorderStroke(1.2.dp, Color(0xFF5EEAD4))
                ) {
                    if (selectedUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 10f)
                        ) {
                            AsyncImage(
                                model = selectedUri,
                                contentDescription = "Preview Foto",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = if (isEn) "Change Photo" else "Ganti Foto",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 26.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                text = if (isEn) "Tap to Select Photo" else "Ketuk untuk Pilih Foto",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E)
                            )
                            Text(
                                text = if (isEn) "Automatically compressed before upload" else "Otomatis dikompres agar hemat kuota & R2",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text(if (isEn) "Activity Caption" else "Keterangan Kegiatan") },
                    placeholder = {
                        Text(
                            if (isEn) "e.g., Morning Quran recitation & breakfast together"
                            else "Misal: Kegiatan mengaji pagi & sarapan bersama asrama"
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_gallery_caption")
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isEn) "Category" else "Kategori Kegiatan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WaliGalleryHelper.ACTIVITY_TAGS.forEach { tag ->
                            FilterChip(
                                selected = selectedTag == tag,
                                onClick = { selectedTag = tag },
                                label = { Text(WaliGalleryHelper.tagLabel(tag, isEn), fontSize = 11.5.sp) }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isEn) "Target Parents" else "Sasaran Orang Tua",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = targetScope == WaliGalleryHelper.SCOPE_ALL_MY_STUDENTS,
                            onClick = { targetScope = WaliGalleryHelper.SCOPE_ALL_MY_STUDENTS },
                            label = {
                                Text(
                                    if (isEn) "All My Foster (${myStudents.size})"
                                    else "Semua Binaan (${myStudents.size})",
                                    fontSize = 11.5.sp
                                )
                            }
                        )
                        FilterChip(
                            selected = targetScope == WaliGalleryHelper.SCOPE_SPECIFIC_STUDENTS,
                            onClick = { targetScope = WaliGalleryHelper.SCOPE_SPECIFIC_STUDENTS },
                            label = {
                                Text(
                                    if (isEn) "Specific Kids" else "Pilih Siswa",
                                    fontSize = 11.5.sp
                                )
                            }
                        )
                    }

                    if (targetScope == WaliGalleryHelper.SCOPE_SPECIFIC_STUDENTS) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(6.dp)
                            ) {
                                if (myStudents.isEmpty()) {
                                    Text(
                                        text = if (isEn) "No foster students claimed yet." else "Belum ada anak asuh yang diklaim.",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                } else {
                                    myStudents.forEach { st ->
                                        val checked = st.id in selectedStudentIds
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedStudentIds = if (checked) {
                                                        selectedStudentIds - st.id
                                                    } else {
                                                        selectedStudentIds + st.id
                                                    }
                                                }
                                                .padding(horizontal = 4.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = checked,
                                                onCheckedChange = { isChecked ->
                                                    selectedStudentIds = if (isChecked) {
                                                        selectedStudentIds + st.id
                                                    } else {
                                                        selectedStudentIds - st.id
                                                    }
                                                }
                                            )
                                            Text(
                                                text = "${st.name} (${st.classGrade})",
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isEn) "Temporary Server Retention (Auto-Clean R2)" else "Masa Simpan Sementara di Server R2",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WaliGalleryHelper.RETENTION_DAY_OPTIONS.forEach { days ->
                            FilterChip(
                                selected = retentionDays == days,
                                onClick = { retentionDays = days },
                                label = {
                                    Text(
                                        if (isEn) "$days days" else "$days Hari",
                                        fontSize = 11.5.sp
                                    )
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val uri = selectedUri
                        if (uri == null) {
                            Toast.makeText(
                                context,
                                if (isEn) "Please select a photo first." else "Pilih foto terlebih dahulu.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            onPublish(uri, caption, selectedTag, targetScope, selectedStudentIds, retentionDays)
                        }
                    },
                    enabled = !isPublishing,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("btn_submit_create_wali_gallery")
                ) {
                    if (isPublishing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isEn) "Uploading..." else "Mengunggah...")
                    } else {
                        Text(
                            text = if (isEn) "Post Now" else "Posting Sekarang",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryRemoteOrLocalImage(
    primaryUrl: String,
    fallbackUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val p = primaryUrl.trim()
    val f = fallbackUrl.trim()
    var useFallback by remember(p, f) { mutableStateOf(false) }
    val active = when {
        !useFallback && p.isNotBlank() -> p
        f.isNotBlank() -> f
        else -> ""
    }
    if (active.isBlank()) {
        Box(
            modifier = modifier.background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = contentDescription, tint = Color(0xFF64748B))
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(active)
                .crossfade(true)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            onError = {
                if (!useFallback && f.isNotBlank() && f != p) {
                    useFallback = true
                }
            },
            modifier = modifier
        )
    }
}

@Composable
fun GalleryFullscreenPreviewDialog(
    title: String,
    caption: String,
    primaryUrl: String,
    fallbackUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .padding(16.dp)
                .testTag("dialog_gallery_fullscreen_preview")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }
                GalleryRemoteOrLocalImage(
                    primaryUrl = primaryUrl,
                    fallbackUrl = fallbackUrl,
                    contentDescription = caption.ifBlank { title },
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                )
                if (caption.isNotBlank()) {
                    Text(
                        text = caption,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
