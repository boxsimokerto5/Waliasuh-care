package com.example.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DashboardBannerActionType
import com.example.data.model.DashboardBannerFeatureTarget
import com.example.data.model.DashboardBannerItem
import com.example.data.model.UploadedBannerMedia
import com.example.ui.theme.WaliPrimary
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBannerFormDialog(
    initialItem: DashboardBannerItem?,
    isUploadingImage: Boolean,
    onUploadImage: (Uri, String, String, (Boolean, String, UploadedBannerMedia?) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (DashboardBannerItem) -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    val isEditing = initialItem != null
    val itemId = remember(initialItem) {
        initialItem?.id ?: "banner_${System.currentTimeMillis()}"
    }

    var title by remember(initialItem) { mutableStateOf(initialItem?.title.orEmpty()) }
    var subtitle by remember(initialItem) { mutableStateOf(initialItem?.subtitle.orEmpty()) }
    var badgeText by remember(initialItem) { mutableStateOf(initialItem?.badgeText.orEmpty()) }
    var imageUrl by remember(initialItem) { mutableStateOf(initialItem?.imageUrl.orEmpty()) }
    var fallbackImageUrl by remember(initialItem) { mutableStateOf(initialItem?.fallbackImageUrl.orEmpty()) }
    var pbRecordId by remember(initialItem) { mutableStateOf(initialItem?.pbRecordId.orEmpty()) }
    var pbCollectionId by remember(initialItem) { mutableStateOf(initialItem?.pbCollectionId.orEmpty()) }
    var pbFilename by remember(initialItem) { mutableStateOf(initialItem?.pbFilename.orEmpty()) }

    var actionType by remember(initialItem) {
        mutableStateOf(initialItem?.actionType ?: DashboardBannerActionType.INTERNAL_FEATURE)
    }
    var selectedFeatureKey by remember(initialItem) {
        mutableStateOf(
            if (initialItem?.actionType == DashboardBannerActionType.INTERNAL_FEATURE) {
                initialItem.actionTarget.ifBlank { DashboardBannerFeatureTarget.FEATURE_FASKES_INFO.key }
            } else {
                DashboardBannerFeatureTarget.FEATURE_CREATE_PLAN.key
            }
        )
    }
    var externalUrl by remember(initialItem) {
        mutableStateOf(
            if (initialItem?.actionType == DashboardBannerActionType.EXTERNAL_LINK) {
                initialItem.actionTarget
            } else {
                ""
            }
        )
    }

    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }
    var uploadStatusIsError by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showFeaturePicker by remember { mutableStateOf(false) }
    var featureSearchQuery by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { pickedUri ->
        if (pickedUri != null) {
            uploadStatusMessage = if (isEn) "Uploading to PocketBase & R2 CDN..." else "Mengunggah ke PocketBase & R2 CDN..."
            uploadStatusIsError = false
            onUploadImage(pickedUri, itemId, title) { ok, msg, media ->
                uploadStatusMessage = msg
                uploadStatusIsError = !ok
                if (ok && media != null) {
                    imageUrl = media.cdnImageUrl
                    fallbackImageUrl = media.fallbackApiFileUrl
                    pbRecordId = media.pbRecordId
                    pbCollectionId = media.pbCollectionId
                    pbFilename = media.pbFilename
                    validationError = null
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isUploadingImage) onDismiss() },
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
                    .widthIn(max = 580.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(WaliPrimary, Color(0xFF0F766E))))
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    initialItem?.isDefaultFaskes == true -> if (isEn) "Customize Faskes Banner" else "Custom Banner Faskes"
                                    isEditing -> if (isEn) "Edit Banner Slide" else "Edit Slide Banner"
                                    else -> if (isEn) "Add Global Banner" else "Tambah Banner Global"
                                },
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isEn) {
                                    "Stored via PocketBase + Cloudflare R2 CDN"
                                } else {
                                    "Gambar disimpan di R2 Cloudflare CDN untuk muat cepat"
                                },
                                fontSize = 10.5.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            enabled = !isUploadingImage,
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
                                    imageVector = Icons.Default.Close,
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
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    // Image Preview Box
                    val previewUrl = imageUrl.trim().ifBlank { fallbackImageUrl.trim() }
                    if (previewUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(104.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(previewUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = if (isEn) "Banner image preview" else "Pratinjau gambar banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Upload from Photo Picker (R2 CDN)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isUploadingImage,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_pick_banner_image")
                        ) {
                            if (isUploadingImage) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEn) "Uploading..." else "Mengunggah...",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "Pick & Upload Image (R2)" else "Upload Gambar ke R2 CDN",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (imageUrl.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    imageUrl = ""
                                    fallbackImageUrl = ""
                                    pbRecordId = ""
                                    pbCollectionId = ""
                                    pbFilename = ""
                                    uploadStatusMessage = null
                                },
                                enabled = !isUploadingImage,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("btn_clear_banner_image")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = if (isEn) "Clear image" else "Hapus gambar",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    uploadStatusMessage?.let { msg ->
                        Text(
                            text = msg,
                            fontSize = 11.sp,
                            color = if (uploadStatusIsError) MaterialTheme.colorScheme.error else Color(0xFF047857),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = {
                            imageUrl = it
                            validationError = null
                        },
                        label = {
                            Text(
                                if (isEn) "Image CDN URL (optional for default Faskes)"
                                else "URL Gambar CDN R2 (opsional utk Faskes bawaan)",
                                fontSize = 11.5.sp
                            )
                        },
                        placeholder = {
                            Text("https://waliasuhcareapp.areksurabaya.my.id/...", fontSize = 11.sp)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("banner_image_url_input")
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            validationError = null
                        },
                        label = {
                            Text(
                                if (isEn) "Banner Title (optional if graphic has text)"
                                else "Judul Banner (kosongkan jika teks sudah di gambar)",
                                fontSize = 11.5.sp
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("banner_title_input")
                    )

                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = {
                            Text(if (isEn) "Short Subtitle (optional)" else "Subjudul Singkat (opsional)", fontSize = 11.5.sp)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("banner_subtitle_input")
                    )

                    OutlinedTextField(
                        value = badgeText,
                        onValueChange = { badgeText = it },
                        label = {
                            Text(if (isEn) "Badge Label (e.g. INFO / NEW)" else "Label Badge Pojok (misal INFO / BARU)", fontSize = 11.5.sp)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("banner_badge_input")
                    )

                    // Action when banner clicked
                    Text(
                        text = if (isEn) "When Banner Tapped" else "Aksi Saat Banner Diklik",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = actionType == DashboardBannerActionType.NONE,
                            onClick = { actionType = DashboardBannerActionType.NONE },
                            label = { Text(if (isEn) "Display Only" else "Tampil Saja", fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_action_none")
                        )
                        FilterChip(
                            selected = actionType == DashboardBannerActionType.INTERNAL_FEATURE,
                            onClick = { actionType = DashboardBannerActionType.INTERNAL_FEATURE },
                            label = { Text(if (isEn) "Open Feature" else "Buka Fitur", fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_action_feature")
                        )
                        FilterChip(
                            selected = actionType == DashboardBannerActionType.EXTERNAL_LINK,
                            onClick = { actionType = DashboardBannerActionType.EXTERNAL_LINK },
                            label = { Text(if (isEn) "External Link" else "Link Luar", fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_action_link")
                        )
                    }

                    if (actionType == DashboardBannerActionType.INTERNAL_FEATURE) {
                        val selectedTarget = DashboardBannerFeatureTarget.fromKey(selectedFeatureKey)
                            ?: DashboardBannerFeatureTarget.FEATURE_FASKES_INFO
                        Surface(
                            onClick = { showFeaturePicker = !showFeaturePicker },
                            shape = RoundedCornerShape(12.dp),
                            color = WaliPrimary.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, WaliPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_open_feature_picker")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = WaliPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${selectedTarget.group(isEn)} • ${selectedTarget.label(isEn)}",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (showFeaturePicker) {
                                            if (isEn) "Tap to collapse feature list" else "Ketuk untuk tutup daftar fitur"
                                        } else {
                                            if (isEn) "Tap to choose from all app features" else "Ketuk untuk pilih dari semua fitur aplikasi"
                                        },
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (showFeaturePicker) {
                            OutlinedTextField(
                                value = featureSearchQuery,
                                onValueChange = { featureSearchQuery = it },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                                },
                                placeholder = {
                                    Text(if (isEn) "Search app feature..." else "Cari fitur aplikasi...", fontSize = 12.sp)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("banner_feature_search_input")
                            )

                            val q = featureSearchQuery.trim()
                            val filteredTargets = remember(q, isEn) {
                                if (q.isBlank()) {
                                    DashboardBannerFeatureTarget.entries
                                } else {
                                    DashboardBannerFeatureTarget.entries.filter {
                                        it.label(isEn).contains(q, ignoreCase = true) ||
                                            it.group(isEn).contains(q, ignoreCase = true)
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                filteredTargets.forEach { target ->
                                    val isSelected = selectedFeatureKey == target.key
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedFeatureKey = target.key
                                                showFeaturePicker = false
                                            }
                                            .heightIn(min = 48.dp)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("feature_option_${target.key.lowercase()}"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                selectedFeatureKey = target.key
                                                showFeaturePicker = false
                                            }
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = target.label(isEn),
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                            Text(
                                                text = target.group(isEn),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (actionType == DashboardBannerActionType.EXTERNAL_LINK) {
                        OutlinedTextField(
                            value = externalUrl,
                            onValueChange = {
                                externalUrl = it
                                validationError = null
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Link, contentDescription = null)
                            },
                            label = {
                                Text(if (isEn) "External URL (https://...)" else "Link URL Eksternal (https://...)", fontSize = 11.5.sp)
                            },
                            placeholder = {
                                Text("https://kemensos.go.id", fontSize = 11.5.sp)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("banner_external_url_input")
                        )
                    }

                    validationError?.let { err ->
                        Text(
                            text = err,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isUploadingImage,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(0.38f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text(if (isEn) "Cancel" else "Batal", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                val cleanImage = imageUrl.trim()
                                val cleanTitle = title.trim()
                                val isFaskes = initialItem?.isDefaultFaskes == true
                                if (!isFaskes && cleanImage.isBlank() && cleanTitle.isBlank()) {
                                    validationError = if (isEn) {
                                        "Please upload an image (or enter CDN URL) or fill a banner title."
                                    } else {
                                        "Silakan unggah gambar, isi URL CDN, atau isi judul banner."
                                    }
                                    return@Button
                                }
                                val resolvedTarget = when (actionType) {
                                    DashboardBannerActionType.NONE -> ""
                                    DashboardBannerActionType.INTERNAL_FEATURE -> selectedFeatureKey
                                    DashboardBannerActionType.EXTERNAL_LINK -> {
                                        val rawUrl = externalUrl.trim()
                                        if (rawUrl.isBlank()) {
                                            validationError = if (isEn) {
                                                "Please enter an external URL."
                                            } else {
                                                "Silakan masukkan link URL eksternal."
                                            }
                                            return@Button
                                        }
                                        if (rawUrl.startsWith("http://", ignoreCase = true) ||
                                            rawUrl.startsWith("https://", ignoreCase = true)
                                        ) {
                                            rawUrl
                                        } else {
                                            "https://$rawUrl"
                                        }
                                    }
                                }
                                val savedItem = DashboardBannerItem(
                                    id = itemId,
                                    title = cleanTitle,
                                    subtitle = subtitle.trim(),
                                    badgeText = badgeText.trim(),
                                    imageUrl = cleanImage,
                                    fallbackImageUrl = fallbackImageUrl.trim(),
                                    pbRecordId = pbRecordId,
                                    pbCollectionId = pbCollectionId,
                                    pbFilename = pbFilename,
                                    actionType = actionType,
                                    actionTarget = resolvedTarget,
                                    isDefaultFaskes = isFaskes,
                                    isEnabled = initialItem?.isEnabled ?: true,
                                    sortOrder = initialItem?.sortOrder ?: 0
                                )
                                onConfirm(savedItem)
                            },
                            enabled = !isUploadingImage,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                            modifier = Modifier
                                .weight(0.62f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_confirm_banner_item")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Apply" else "Terapkan",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
