package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DashboardIconKey
import com.example.ui.components.dashboardIconUiMeta
import com.example.ui.theme.WaliPrimary
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardLayoutEditorSheet(
    currentOrder: List<DashboardIconKey>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (List<DashboardIconKey>) -> Unit,
    onResetDefault: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    var draftOrder by remember(currentOrder) {
        mutableStateOf(DashboardIconKey.normalizeOrder(currentOrder.map { it.key }))
    }
    var selectedSwapIndex by remember { mutableStateOf<Int?>(null) }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
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
                                imageVector = Icons.Default.Tune,
                                contentDescription = if (isEn) "Arrange Dashboard Icons" else "Atur Tata Letak Ikon",
                                tint = WaliPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Arrange Dashboard Icons" else "Atur Tata Letak Ikon Dashboard",
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEn) {
                                "Synced via PocketBase cloud to all Wali Asuh dashboards"
                            } else {
                                "Disinkronkan ke PocketBase untuk seluruh dashboard Wali Asuh"
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live 2x4 Tap-to-Swap Preview
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = WaliPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (selectedSwapIndex == null) {
                                    if (isEn) {
                                        "Live 2×4 Preview (tap 2 icons to swap, or use arrows below)"
                                    } else {
                                        "Pratinjau 2×4 (ketuk 2 ikon untuk tukar posisi, atau pakai panah)"
                                    }
                                } else {
                                    val pickedMeta = dashboardIconUiMeta(draftOrder[selectedSwapIndex!!], isEn)
                                    if (isEn) {
                                        "Selected '${pickedMeta.title}' — tap another icon to swap"
                                    } else {
                                        "Dipilih '${pickedMeta.title}' — ketuk ikon tujuan untuk menukar"
                                    }
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selectedSwapIndex != null) WaliPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        draftOrder.chunked(4).forEachIndexed { rowIdx, rowKeys ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowKeys.forEachIndexed { colIdx, key ->
                                    val absoluteIdx = rowIdx * 4 + colIdx
                                    val meta = dashboardIconUiMeta(key, isEn)
                                    val isPicked = selectedSwapIndex == absoluteIdx
                                    Surface(
                                        onClick = {
                                            val curPicked = selectedSwapIndex
                                            if (curPicked == null) {
                                                selectedSwapIndex = absoluteIdx
                                            } else if (curPicked == absoluteIdx) {
                                                selectedSwapIndex = null
                                            } else {
                                                val mutable = draftOrder.toMutableList()
                                                val temp = mutable[curPicked]
                                                mutable[curPicked] = mutable[absoluteIdx]
                                                mutable[absoluteIdx] = temp
                                                draftOrder = mutable
                                                selectedSwapIndex = null
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isPicked) WaliPrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            width = if (isPicked) 1.8.dp else 0.8.dp,
                                            color = if (isPicked) WaliPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("preview_swap_tile_${key.key.lowercase()}")
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp, horizontal = 3.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(meta.tileBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(id = meta.imageRes),
                                                    contentDescription = meta.title,
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            Text(
                                                text = meta.title,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Detailed Ordered List with Up / Down controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 270.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    draftOrder.forEachIndexed { index, key ->
                        val meta = dashboardIconUiMeta(key, isEn)
                        val rowNum = (index / 4) + 1
                        val colNum = (index % 4) + 1
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(WaliPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WaliPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(meta.tileBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = meta.imageRes),
                                        contentDescription = meta.title,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = meta.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Row $rowNum • Column $colNum"
                                        } else {
                                            "Baris $rowNum • Kolom $colNum"
                                        },
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val mutable = draftOrder.toMutableList()
                                            val item = mutable.removeAt(index)
                                            mutable.add(index - 1, item)
                                            draftOrder = mutable
                                            selectedSwapIndex = null
                                        }
                                    },
                                    enabled = index > 0 && !isSaving,
                                    modifier = Modifier.testTag("btn_move_up_${key.key.lowercase()}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = if (isEn) "Move ${meta.title} up" else "Geser ${meta.title} naik"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (index < draftOrder.lastIndex) {
                                            val mutable = draftOrder.toMutableList()
                                            val item = mutable.removeAt(index)
                                            mutable.add(index + 1, item)
                                            draftOrder = mutable
                                            selectedSwapIndex = null
                                        }
                                    },
                                    enabled = index < draftOrder.lastIndex && !isSaving,
                                    modifier = Modifier.testTag("btn_move_down_${key.key.lowercase()}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isEn) "Move ${meta.title} down" else "Geser ${meta.title} turun"
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Actions Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            draftOrder = DashboardIconKey.DEFAULT_ORDER
                            selectedSwapIndex = null
                            onResetDefault()
                        },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_reset_dashboard_layout")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isEn) "Reset" else "Reset Default",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { onSave(draftOrder) },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_save_dashboard_layout")
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
                                text = if (isEn) "Save & Sync Cloud" else "Simpan & Sinkronkan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
