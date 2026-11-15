package com.example.ui.dialogs

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.BottomSheetDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.DashboardBannerActionType
import com.example.data.model.DashboardBannerConfig
import com.example.data.model.DashboardBannerFeatureTarget
import com.example.data.model.DashboardBannerItem
import com.example.data.model.UploadedBannerMedia
import com.example.ui.theme.WaliPrimary
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBannerEditorSheet(
    currentConfig: DashboardBannerConfig,
    isSaving: Boolean,
    isUploadingImage: Boolean,
    onUploadImage: (Uri, String, String, (Boolean, String, UploadedBannerMedia?) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onSave: (DashboardBannerConfig) -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    var draftItems by remember(currentConfig) {
        mutableStateOf(currentConfig.normalized().items)
    }
    var draftInterval by remember(currentConfig) {
        mutableStateOf(DashboardBannerConfig.clampIntervalSeconds(currentConfig.slideIntervalSeconds))
    }
    var showFormForNew by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<DashboardBannerItem?>(null) }

    val hasFaskesBanner = remember(draftItems) {
        draftItems.any { it.isDefaultFaskes || it.id == DashboardBannerItem.DEFAULT_FASKES_BANNER_ID }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving && !isUploadingImage) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
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
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WaliPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = if (isEn) "Manage Global Banners" else "Atur Banner Global",
                                tint = WaliPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Global Dashboard Banners" else "Atur Banner Slider Global",
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEn) {
                                "Global across all schools • Images hosted on Cloudflare R2 CDN"
                            } else {
                                "Berlaku global seluruh sekolah • CDN: ${DashboardBannerItem.R2_CDN_BASE_URL}"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Auto-slide Interval Picker (2, 3, 4, 5 Detik)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = WaliPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isEn) "Auto-Slide Duration" else "Durasi Geser Otomatis",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DashboardBannerConfig.ALLOWED_INTERVAL_SECONDS.forEach { sec ->
                                FilterChip(
                                    selected = draftInterval == sec,
                                    onClick = { draftInterval = sec },
                                    label = {
                                        Text(
                                            text = if (isEn) "${sec}s" else "$sec Detik",
                                            fontSize = 11.5.sp,
                                            fontWeight = if (draftInterval == sec) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chip_banner_interval_$sec")
                                )
                            }
                        }
                    }
                }

                // Add & Restore Faskes Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showFormForNew = true },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_add_banner_item")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Add Banner" else "Tambah Banner Baru",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!hasFaskesBanner) {
                        OutlinedButton(
                            onClick = {
                                val restored = DashboardBannerItem.defaultFaskes()
                                    .copy(sortOrder = draftItems.size)
                                draftItems = (draftItems + restored).mapIndexed { i, it -> it.copy(sortOrder = i) }
                            },
                            enabled = !isSaving,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("btn_restore_faskes_banner")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEn) "Restore Faskes" else "Pulihkan Faskes",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Banner Items List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp, max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (draftItems.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isEn) {
                                    "All banners removed (including Faskes). Save to apply or add a new banner above."
                                } else {
                                    "Semua banner telah dihapus (termasuk Faskes). Simpan untuk menerapkan, atau tambah banner baru di atas."
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        draftItems.forEachIndexed { index, item ->
                            val displayTitle = item.title.ifBlank {
                                if (item.isDefaultFaskes) {
                                    if (isEn) "Referral Faskes & Puskesmas" else "Faskes & Puskesmas Rujukan"
                                } else {
                                    if (isEn) "Image Banner #${index + 1}" else "Banner Gambar #${index + 1}"
                                }
                            }
                            val actionSummary = when (item.actionType) {
                                DashboardBannerActionType.NONE -> if (isEn) "Display only" else "Tanpa aksi klik"
                                DashboardBannerActionType.EXTERNAL_LINK -> "Link: ${item.actionTarget}"
                                DashboardBannerActionType.INTERNAL_FEATURE -> {
                                    val targetLabel = DashboardBannerFeatureTarget.fromKey(item.actionTarget)?.label(isEn)
                                        ?: item.actionTarget
                                    if (isEn) "Feature: $targetLabel" else "Fitur: $targetLabel"
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("editor_banner_row_${item.id}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val thumbUrl = item.resolvePrimaryImageUrl()
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (thumbUrl.isNotBlank()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(context)
                                                        .data(thumbUrl)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = displayTitle,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else if (item.isDefaultFaskes) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.img_menu_puskesmas),
                                                    contentDescription = displayTitle,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoLibrary,
                                                    contentDescription = displayTitle,
                                                    tint = WaliPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = displayTitle,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = actionSummary,
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Switch(
                                            checked = item.isEnabled,
                                            onCheckedChange = { checked ->
                                                draftItems = draftItems.mapIndexed { idx, cur ->
                                                    if (idx == index) cur.copy(isEnabled = checked) else cur
                                                }
                                            }
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (index > 0) {
                                                    val mutable = draftItems.toMutableList()
                                                    val moved = mutable.removeAt(index)
                                                    mutable.add(index - 1, moved)
                                                    draftItems = mutable.mapIndexed { i, it -> it.copy(sortOrder = i) }
                                                }
                                            },
                                            enabled = index > 0 && !isSaving
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = if (isEn) "Move banner up" else "Geser banner naik"
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (index < draftItems.lastIndex) {
                                                    val mutable = draftItems.toMutableList()
                                                    val moved = mutable.removeAt(index)
                                                    mutable.add(index + 1, moved)
                                                    draftItems = mutable.mapIndexed { i, it -> it.copy(sortOrder = i) }
                                                }
                                            },
                                            enabled = index < draftItems.lastIndex && !isSaving
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = if (isEn) "Move banner down" else "Geser banner turun"
                                            )
                                        }
                                        IconButton(
                                            onClick = { editingItem = item },
                                            enabled = !isSaving,
                                            modifier = Modifier.testTag("btn_edit_banner_${item.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = if (isEn) "Edit banner" else "Edit banner",
                                                tint = WaliPrimary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                draftItems = draftItems
                                                    .filterNot { it.id == item.id }
                                                    .mapIndexed { i, it -> it.copy(sortOrder = i) }
                                            },
                                            enabled = !isSaving,
                                            modifier = Modifier.testTag("btn_delete_banner_${item.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = if (isEn) "Delete banner" else "Hapus banner",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Button(
                    onClick = {
                        onSave(
                            DashboardBannerConfig(
                                items = draftItems.mapIndexed { i, it -> it.copy(sortOrder = i) },
                                slideIntervalSeconds = draftInterval,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    },
                    enabled = !isSaving && !isUploadingImage,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_save_dashboard_banners")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEn) "Syncing..." else "Menyinkronkan...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Save & Sync Global Banners" else "Simpan & Sinkronkan Global",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showFormForNew || editingItem != null) {
        DashboardBannerFormDialog(
            initialItem = editingItem,
            isUploadingImage = isUploadingImage,
            onUploadImage = onUploadImage,
            onDismiss = {
                showFormForNew = false
                editingItem = null
            },
            onConfirm = { savedItem ->
                val exists = draftItems.any { it.id == savedItem.id }
                draftItems = if (exists) {
                    draftItems.map { if (it.id == savedItem.id) savedItem else it }
                } else {
                    (draftItems + savedItem.copy(sortOrder = draftItems.size))
                }.mapIndexed { i, it -> it.copy(sortOrder = i) }
                showFormForNew = false
                editingItem = null
            }
        )
    }
}
