package com.example.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.model.ChildStudent
import com.example.ui.components.StudentAvatar
import com.example.util.i18n.LocalAppStrings
import java.io.File
import kotlin.math.roundToInt

@Composable
fun StudentPhotoPreviewDialog(
    studentName: String,
    gender: String = "L",
    subtitle: String = "",
    photoUrl: String,
    photoFallbackUrl: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    val primaryUrl = photoUrl.trim()
    val fallbackUrl = photoFallbackUrl.trim()
    var useFallback by remember(primaryUrl, fallbackUrl) { mutableStateOf(false) }
    var imageFailed by remember(primaryUrl, fallbackUrl) { mutableStateOf(false) }

    val activeUrl = when {
        imageFailed -> ""
        !useFallback && primaryUrl.isNotBlank() -> primaryUrl
        fallbackUrl.isNotBlank() -> fallbackUrl
        else -> ""
    }

    var scale by remember(activeUrl) { mutableFloatStateOf(1f) }
    var offset by remember(activeUrl) { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    fun clampOffset(rawOffset: Offset, targetScale: Float): Offset {
        if (targetScale <= 1.01f || containerSize.width <= 0 || containerSize.height <= 0) {
            return Offset.Zero
        }
        val maxX = (containerSize.width * (targetScale - 1f)) / 2f
        val maxY = (containerSize.height * (targetScale - 1f)) / 2f
        return Offset(
            x = rawOffset.x.coerceIn(-maxX, maxX),
            y = rawOffset.y.coerceIn(-maxY, maxY)
        )
    }

    fun applyZoomLevel(targetScale: Float) {
        val clampedScale = targetScale.coerceIn(1f, 4f)
        if (clampedScale <= 1.01f) {
            scale = 1f
            offset = Offset.Zero
        } else {
            val ratio = if (scale > 0f) clampedScale / scale else 1f
            scale = clampedScale
            offset = clampOffset(offset * ratio, clampedScale)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.90f))
                .padding(16.dp)
                .testTag("dialog_student_photo_preview"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = studentName,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .testTag("btn_close_student_photo_preview")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close" else "Tutup",
                            tint = Color.White
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.22f))
                ) {
                    if (activeUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                                .onSizeChanged { containerSize = it }
                                .pointerInput(activeUrl) {
                                    detectTransformGestures { centroid, pan, zoom, _ ->
                                        val oldScale = scale
                                        val newScale = (oldScale * zoom).coerceIn(1f, 4f)
                                        scale = newScale
                                        if (newScale <= 1.01f) {
                                            scale = 1f
                                            offset = Offset.Zero
                                        } else {
                                            val scaleFactor = if (oldScale > 0f) newScale / oldScale else 1f
                                            val center = Offset(
                                                containerSize.width / 2f,
                                                containerSize.height / 2f
                                            )
                                            val centroidFromCenter = centroid - center
                                            val nextOffset =
                                                (offset - centroidFromCenter) * scaleFactor + centroidFromCenter + pan
                                            offset = clampOffset(nextOffset, newScale)
                                        }
                                    }
                                }
                                .pointerInput(activeUrl) {
                                    detectTapGestures(
                                        onDoubleTap = { tapOffset ->
                                            if (scale > 1.2f) {
                                                scale = 1f
                                                offset = Offset.Zero
                                            } else {
                                                val targetScale = 2.5f
                                                val center = Offset(
                                                    containerSize.width / 2f,
                                                    containerSize.height / 2f
                                                )
                                                val tapFromCenter = tapOffset - center
                                                scale = targetScale
                                                offset = clampOffset(
                                                    -tapFromCenter * (targetScale - 1f),
                                                    targetScale
                                                )
                                            }
                                        }
                                    )
                                }
                                .testTag("box_student_photo_zoom_canvas"),
                            contentAlignment = Alignment.Center
                        ) {
                            val requestData: Any = if (activeUrl.startsWith("/")) File(activeUrl) else activeUrl
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(requestData)
                                    .crossfade(true)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = studentName,
                                contentScale = ContentScale.Fit,
                                onError = {
                                    if (!useFallback && fallbackUrl.isNotBlank() && fallbackUrl != primaryUrl) {
                                        useFallback = true
                                    } else {
                                        imageFailed = true
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
                                    .testTag("img_student_photo_preview")
                            )

                            if (scale > 1.02f) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color.Black.copy(alpha = 0.68f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                        .testTag("badge_student_photo_zoom")
                                ) {
                                    Text(
                                        text = "${(scale * 100).roundToInt()}%",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                StudentAvatar(
                                    name = studentName,
                                    gender = gender,
                                    size = 132.dp
                                )
                                Text(
                                    text = if (isEn) {
                                        "No profile photo uploaded yet (showing temporary app logo)."
                                    } else {
                                        "Belum ada foto profil yang diunggah (menampilkan logo aplikasi sementara)."
                                    },
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                if (activeUrl.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { applyZoomLevel(scale - 0.5f) },
                                enabled = scale > 1.02f,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_student_photo_zoom_out")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomOut,
                                    contentDescription = if (isEn) "Zoom out" else "Perkecil foto",
                                    tint = if (scale > 1.02f) Color.White else Color.White.copy(alpha = 0.35f)
                                )
                            }

                            TextButton(
                                onClick = { applyZoomLevel(1f) },
                                enabled = scale > 1.02f,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_student_photo_zoom_reset")
                            ) {
                                if (scale > 1.02f) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = if (scale > 1.02f) {
                                        "${(scale * 100).roundToInt()}% • Reset"
                                    } else {
                                        "100%"
                                    },
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            IconButton(
                                onClick = { applyZoomLevel(scale + 0.5f) },
                                enabled = scale < 3.98f,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_student_photo_zoom_in")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = if (isEn) "Zoom in" else "Perbesar foto",
                                    tint = if (scale < 3.98f) Color.White else Color.White.copy(alpha = 0.35f)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isEn) {
                            "Pinch with 2 fingers or double-tap to zoom and pan"
                        } else {
                            "Cubit 2 jari atau ketuk 2x untuk perbesar & geser foto"
                        },
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("text_student_photo_zoom_hint")
                    )
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        text = if (isEn) "Close Preview" else "Tutup Pratinjau",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPhotoManagerDialog(
    student: ChildStudent,
    isUploading: Boolean,
    onPickPhoto: (Uri) -> Unit,
    onRemovePhoto: () -> Unit,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPreview by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }
    val hasPhoto = student.photoUrl.isNotBlank() || student.photoFallbackUrl.isNotBlank()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPickPhoto(uri)
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isUploading) onDismiss() },
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
                    .widthIn(max = 520.dp)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
                    .testTag("dialog_student_photo_manager"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isEn) "Student Profile Photo" else "Foto Profil Siswa",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isUploading,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close" else "Tutup"
                        )
                    }
                }

                Text(
                    text = "${student.name} • ${student.classGrade.ifBlank { "-" }}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    StudentAvatar(
                        name = student.name,
                        gender = student.gender,
                        size = 118.dp,
                        photoUrl = student.photoUrl,
                        photoFallbackUrl = student.photoFallbackUrl
                    )
                    if (isUploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                if (isUploading) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = if (isEn) {
                                "Compressing & uploading photo to cloud..."
                            } else {
                                "Mengompresi & mengunggah foto ke cloud..."
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isUploading,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("btn_pick_student_photo")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasPhoto) {
                            if (isEn) "Change Photo from Gallery" else "Pilih / Ganti Foto dari Galeri"
                        } else {
                            if (isEn) "Choose Photo from Gallery" else "Pilih Foto dari Galeri"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                if (hasPhoto) {
                    OutlinedButton(
                        onClick = { showPreview = true },
                        enabled = !isUploading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_preview_student_photo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEn) "View Full Photo" else "Lihat Ukuran Penuh",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = { showConfirmDelete = true },
                        enabled = !isUploading,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_remove_student_photo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEn) "Remove Profile Photo" else "Hapus Foto Profil",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = if (isEn) {
                        "Photos are automatically resized & compressed to lightweight WebP, synced to PocketBase, and shown on the Parent Portal."
                    } else {
                        "Foto otomatis dirapikan & dikompres ke format WebP ringan, disinkronkan ke server, dan langsung tampil di halaman Orang Tua."
                    },
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }

    if (showPreview) {
        StudentPhotoPreviewDialog(
            studentName = student.name,
            gender = student.gender,
            subtitle = "${student.classGrade.ifBlank { "-" }} • ${student.dormitory.ifBlank { "-" }}",
            photoUrl = student.photoUrl,
            photoFallbackUrl = student.photoFallbackUrl,
            onDismiss = { showPreview = false }
        )
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = {
                Text(
                    text = if (isEn) "Remove Profile Photo?" else "Hapus Foto Profil?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEn) {
                        "The profile photo for ${student.name} will be removed and revert to the temporary app logo on both Wali Asuh and Parent Portal."
                    } else {
                        "Foto profil ${student.name} akan dihapus dan dikembalikan ke logo aplikasi sementara di halaman Wali Asuh maupun Orang Tua."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDelete = false
                        onRemovePhoto()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("btn_confirm_remove_student_photo")
                ) {
                    Text(if (isEn) "Remove" else "Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }
}
