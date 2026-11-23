package com.example.ui.dialogs

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LeaveDayInfo
import com.example.data.model.PinnedTaskItem
import com.example.data.model.SchoolProfile
import com.example.data.model.WaliAccount
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliScheduleHelper
import com.example.data.model.WaliShiftCode
import com.example.util.StudentCsvParser
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditJadwalSelDialog(
    entry: WaliScheduleEntry,
    day: Int,
    monthKey: String,
    shiftCodes: List<WaliShiftCode>,
    schoolProfile: SchoolProfile,
    onSave: (shiftCode: String, pins: List<PinnedTaskItem>, leave: LeaveDayInfo?) -> Unit,
    onOpenPatternEditor: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    val effectiveCodes = remember(shiftCodes, schoolProfile.tenantCode) {
        shiftCodes
            .ifEmpty { WaliScheduleHelper.defaultShiftCodes(schoolProfile.tenantCode) }
            .sortedWith(compareBy<WaliShiftCode> { it.sortOrder }.thenBy { it.code })
            .distinctBy { WaliScheduleHelper.normalizeCode(it.code) }
    }
    val initialCode = remember(entry.dayCodesJson, day) {
        WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)[day]?.let { WaliScheduleHelper.normalizeCode(it) }.orEmpty()
    }
    val initialPins = remember(entry.dayPinsJson, day) {
        WaliScheduleHelper.parseDayPins(entry.dayPinsJson)[day].orEmpty()
    }
    val initialLeave = remember(entry.dayLeaveJson, day) {
        WaliScheduleHelper.parseDayLeaves(entry.dayLeaveJson)[day]
    }

    var selectedCode by remember(entry.id, day) { mutableStateOf(initialCode) }
    val pins = remember(entry.id, day) { mutableStateListOf<PinnedTaskItem>().apply { addAll(initialPins) } }
    var customPinLabel by remember { mutableStateOf("") }
    var customPinCategory by remember { mutableStateOf(WaliScheduleHelper.PIN_TUGAS) }

    var leaveStatus by remember(entry.id, day) { mutableStateOf(initialLeave?.status.orEmpty()) }
    var leaveDriveUrl by remember(entry.id, day) { mutableStateOf(initialLeave?.driveUrl.orEmpty()) }
    var leaveNote by remember(entry.id, day) { mutableStateOf(initialLeave?.note.orEmpty()) }

    val schoolFaskes = remember(schoolProfile) { schoolProfile.getAllFaskes().distinctBy { it.trim().lowercase() } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .heightIn(max = 680.dp)
                    .testTag("dialog_edit_jadwal_sel"),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Sticky Header Ringkas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Edit Schedule Cell" else "Atur Shift & Pin Tugas",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${entry.waliName} • ${WaliScheduleHelper.formatDateChipLabel(monthKey, day, isEn)}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F766E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = if (isEn) "Close" else "Tutup",
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    HorizontalDivider(thickness = 0.7.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                    // Body Rapat (scrollable hanya jika layar kecil / form izin terbuka)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Shift Code Picker (4 Kolom Rapat & Bebas Duplikat)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "1. Shift Code" else "1. Pilih Kode Shift",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (onOpenPatternEditor != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFE0F2FE),
                                        modifier = Modifier.clickable {
                                            onDismiss()
                                            onOpenPatternEditor()
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Repeat,
                                                contentDescription = null,
                                                tint = Color(0xFF0369A1),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = if (isEn) "Pattern" else "Pola Rotasi",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF0369A1),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .clickable { selectedCode = "" }
                                        .testTag("btn_clear_jadwal_shift")
                                ) {
                                    Text(
                                        text = if (isEn) "Clear" else "Kosongkan",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            effectiveCodes.chunked(4).forEach { rowCodes ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    rowCodes.forEach { sc ->
                                        val color = parseJadwalShiftColor(sc.colorHex)
                                        val isSelected = selectedCode.equals(sc.code, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(9.dp),
                                            color = if (isSelected) color else color.copy(alpha = 0.11f),
                                            border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, color),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedCode = sc.code }
                                                .testTag("chip_pick_shift_${sc.code}")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = sc.code,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isSelected) Color.White else color,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = sc.name,
                                                    fontSize = 8.5.sp,
                                                    lineHeight = 10.sp,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.92f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                    repeat(4 - rowCodes.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        HorizontalDivider(thickness = 0.6.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

                        // 2. Pin Tugas & Jaga Faskes (Chip Kompak Rapat)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "2. Pin Task / Referral Faskes" else "2. Sematkan Tugas / Jaga Faskes",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (pins.isNotEmpty()) {
                                Text(
                                    text = "${pins.size} ${if (isEn) "pinned" else "tersemat"}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E)
                                )
                            }
                        }

                        if (pins.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                pins.forEachIndexed { idx, pin ->
                                    val isFaskes = pin.category.equals(WaliScheduleHelper.PIN_FASKES, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(7.dp),
                                        color = if (isFaskes) Color(0xFFFEE2E2) else Color(0xFFE0E7FF),
                                        border = BorderStroke(0.9.dp, if (isFaskes) Color(0xFFFCA5A5) else Color(0xFFC7D2FE))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(start = 7.dp, end = 4.dp, top = 3.dp, bottom = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = pin.label,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isFaskes) Color(0xFFB91C1C) else Color(0xFF3730A3),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = if (isEn) "Remove pin" else "Hapus pin",
                                                tint = if (isFaskes) Color(0xFFB91C1C) else Color(0xFF3730A3),
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { pins.removeAt(idx) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Preset Tugas Cepat + Faskes Sekolah (Chip Pill Rapat tanpa jarak raksasa)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            WaliScheduleHelper.DEFAULT_PIN_PRESETS.forEach { preset ->
                                val exists = pins.any { it.label.equals(preset, ignoreCase = true) }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (exists) Color(0xFFE0E7FF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (exists) Color(0xFF4F46E5) else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.clickable {
                                        if (exists) {
                                            pins.removeAll { it.label.equals(preset, ignoreCase = true) }
                                        } else {
                                            pins.add(PinnedTaskItem(label = preset, category = WaliScheduleHelper.PIN_TUGAS))
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PushPin,
                                            contentDescription = null,
                                            tint = if (exists) Color(0xFF3730A3) else Color(0xFF0284C7),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = preset,
                                            fontSize = 10.5.sp,
                                            fontWeight = if (exists) FontWeight.Bold else FontWeight.Medium,
                                            color = if (exists) Color(0xFF312E81) else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            schoolFaskes.forEach { faskesName ->
                                val label = "Jaga $faskesName"
                                val exists = pins.any { it.label.equals(label, ignoreCase = true) }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (exists) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (exists) Color(0xFFDC2626) else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.clickable {
                                        if (exists) {
                                            pins.removeAll { it.label.equals(label, ignoreCase = true) }
                                        } else {
                                            pins.add(PinnedTaskItem(label = label, category = WaliScheduleHelper.PIN_FASKES))
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.LocalHospital,
                                            contentDescription = null,
                                            tint = if (exists) Color(0xFFB91C1C) else Color(0xFF0369A1),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = label,
                                            fontSize = 10.5.sp,
                                            fontWeight = if (exists) FontWeight.Bold else FontWeight.Medium,
                                            color = if (exists) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Input Pin Kustom Ringkas 1 Baris + Pilihan Kategori Mini
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = customPinLabel,
                                onValueChange = { customPinLabel = it },
                                placeholder = {
                                    Text(
                                        text = if (isEn) "Custom task / faskes..." else "Tugas / Faskes kustom...",
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                textStyle = TextStyle(fontSize = 12.sp),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_pin_custom_label")
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (customPinCategory == WaliScheduleHelper.PIN_FASKES) Color(0xFFFEE2E2) else Color(0xFFE0E7FF),
                                border = BorderStroke(
                                    1.dp,
                                    if (customPinCategory == WaliScheduleHelper.PIN_FASKES) Color(0xFFFCA5A5) else Color(0xFFC7D2FE)
                                ),
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .clickable {
                                        customPinCategory = if (customPinCategory == WaliScheduleHelper.PIN_TUGAS) {
                                            WaliScheduleHelper.PIN_FASKES
                                        } else {
                                            WaliScheduleHelper.PIN_TUGAS
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (customPinCategory == WaliScheduleHelper.PIN_FASKES) {
                                            if (isEn) "Faskes" else "Faskes"
                                        } else {
                                            if (isEn) "Task" else "Tugas"
                                        },
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (customPinCategory == WaliScheduleHelper.PIN_FASKES) Color(0xFFB91C1C) else Color(0xFF3730A3),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    val clean = customPinLabel.trim()
                                    if (clean.isNotBlank() && pins.none { it.label.equals(clean, ignoreCase = true) }) {
                                        pins.add(PinnedTaskItem(label = clean, category = customPinCategory))
                                        customPinLabel = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("btn_add_custom_pin")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = if (isEn) "Add Pin" else "Tambah Pin", modifier = Modifier.size(18.dp))
                            }
                        }

                        HorizontalDivider(thickness = 0.6.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

                        // 3. Izin / Sakit / Cuti (Segmented 4 Kolom Seimbang, Tidak Akan Patah Vertikal)
                        Text(
                            text = if (isEn) "3. Leave / Sick Status & Drive Link" else "3. Keterangan Izin / Sakit / Cuti",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            listOf(
                                "" to if (isEn) "Normal" else "Normal",
                                "IZIN" to "IZIN",
                                "SAKIT" to "SAKIT",
                                "CUTI" to "CUTI"
                            ).forEach { (key, lbl) ->
                                val selected = leaveStatus == key
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) {
                                        if (key.isEmpty()) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    },
                                    border = BorderStroke(
                                        if (selected) 1.3.dp else 0.9.dp,
                                        if (selected) {
                                            if (key.isEmpty()) Color(0xFF15803D) else Color(0xFFD97706)
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { leaveStatus = key }
                                        .testTag("chip_leave_status_${key.ifBlank { "NORMAL" }}")
                                ) {
                                    Text(
                                        text = lbl,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (selected) {
                                            if (key.isEmpty()) Color(0xFF166534) else Color(0xFF92400E)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        maxLines = 1,
                                        softWrap = false,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        if (leaveStatus.isNotBlank()) {
                            OutlinedTextField(
                                value = leaveDriveUrl,
                                onValueChange = { leaveDriveUrl = it },
                                placeholder = { Text(if (isEn) "Google Drive Letter Link (Optional)" else "Tautan Google Drive Surat (Opsional)", fontSize = 11.5.sp) },
                                textStyle = TextStyle(fontSize = 12.sp),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_leave_drive_url")
                            )
                            OutlinedTextField(
                                value = leaveNote,
                                onValueChange = { leaveNote = it },
                                placeholder = { Text(if (isEn) "Note / Reason" else "Catatan / Keterangan Izin", fontSize = 11.5.sp) },
                                textStyle = TextStyle(fontSize = 12.sp),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Sticky Footer (Selalu Terlihat & Aman di Atas Navigasi Sistem)
                    HorizontalDivider(thickness = 0.7.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text(
                                text = if (isEn) "Cancel" else "Batal",
                                fontSize = 12.5.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Button(
                            onClick = {
                                val leave = if (leaveStatus.isBlank()) null else LeaveDayInfo(
                                    status = leaveStatus,
                                    driveUrl = leaveDriveUrl.trim(),
                                    note = leaveNote.trim()
                                )
                                val finalCode = selectedCode.ifBlank {
                                    if (leaveStatus == "CUTI") "C" else if (leaveStatus.isNotBlank()) "IZIN" else ""
                                }
                                onSave(finalCode, pins.toList(), leave)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(44.dp)
                                .testTag("btn_save_jadwal_sel")
                        ) {
                            Text(
                                text = if (isEn) "Save Schedule" else "Simpan Perubahan",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageShiftCodesDialog(
    shiftCodes: List<WaliShiftCode>,
    schoolProfile: SchoolProfile,
    onSaveCode: (
        existing: WaliShiftCode?,
        code: String,
        name: String,
        shiftGroup: String,
        zoneLabel: String,
        timeRange: String,
        startHour: Int,
        endHour: Int,
        workHours: Int,
        colorHex: String,
        sopNotes: String
    ) -> Unit,
    onDeleteCode: (WaliShiftCode) -> Unit,
    onResetDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    val effectiveCodes = remember(shiftCodes, schoolProfile.tenantCode) {
        shiftCodes.ifEmpty { WaliScheduleHelper.defaultShiftCodes(schoolProfile.tenantCode) }
            .sortedBy { it.sortOrder }
    }
    var editingTarget by remember { mutableStateOf<WaliShiftCode?>(null) }
    var showForm by remember { mutableStateOf(false) }

    var formCode by remember { mutableStateOf("") }
    var formName by remember { mutableStateOf("") }
    var formGroup by remember { mutableStateOf(WaliScheduleHelper.GROUP_PAGI) }
    var formZone by remember { mutableStateOf("") }
    var formTimeRange by remember { mutableStateOf("07:00 - 15:00") }
    var formStartHour by remember { mutableStateOf("7") }
    var formEndHour by remember { mutableStateOf("15") }
    var formWorkHours by remember { mutableStateOf("8") }
    var formColorHex by remember { mutableStateOf("#0284C7") }
    var formSopNotes by remember { mutableStateOf("") }
    var confirmReset by remember { mutableStateOf(false) }

    fun openFormFor(sc: WaliShiftCode?) {
        editingTarget = sc
        formCode = sc?.code.orEmpty()
        formName = sc?.name.orEmpty()
        formGroup = sc?.shiftGroup ?: WaliScheduleHelper.GROUP_PAGI
        formZone = sc?.zoneLabel.orEmpty()
        formTimeRange = sc?.timeRange ?: "07:00 - 15:00"
        formStartHour = (sc?.startHour ?: 7).toString()
        formEndHour = (sc?.endHour ?: 15).toString()
        formWorkHours = (sc?.workHours ?: 8).toString()
        formColorHex = sc?.colorHex ?: "#0284C7"
        formSopNotes = sc?.sopNotes.orEmpty()
        showForm = true
    }

    val colorPalette = listOf("#0284C7", "#0F766E", "#EA580C", "#D97706", "#7C3AED", "#9333EA", "#15803D", "#DC2626", "#475569")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .testTag("dialog_manage_shift_codes"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Customize School Shift Codes" else "Atur Kode Shift Sekolah",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
                            fontSize = 11.5.sp,
                            color = Color(0xFF0F766E),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                    }
                }

                if (!showForm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { openFormFor(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_add_new_shift_code")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEn) "Add Code" else "Tambah Kode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { confirmReset = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("btn_reset_default_shift_codes")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEn) "Preset" else "Preset Resmi", fontSize = 12.sp)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(effectiveCodes, key = { "${it.id}_${it.code}" }) { sc ->
                            val badgeColor = parseJadwalShiftColor(sc.colorHex)
                            ElevatedCard(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = badgeColor) {
                                        Text(
                                            text = sc.code,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sc.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${sc.shiftGroup} • ${sc.timeRange} (${sc.workHours}j)${if (sc.zoneLabel.isNotBlank()) " • ${sc.zoneLabel}" else ""}",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = { openFormFor(sc) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = if (isEn) "Edit" else "Ubah", tint = Color(0xFF0284C7))
                                    }
                                    IconButton(
                                        onClick = { onDeleteCode(sc) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = if (isEn) "Delete" else "Hapus", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 460.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formCode,
                                onValueChange = { formCode = it.uppercase() },
                                label = { Text(if (isEn) "Code (e.g. S5)" else "Kode (mis. S5)") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(0.42f)
                                    .testTag("input_shift_code_key")
                            )
                            OutlinedTextField(
                                value = formName,
                                onValueChange = { formName = it },
                                label = { Text(if (isEn) "Shift Name" else "Nama Shift") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(0.58f)
                                    .testTag("input_shift_code_name")
                            )
                        }

                        Text(if (isEn) "Shift Category" else "Kategori Grup Shift", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(WaliScheduleHelper.ALL_GROUPS) { grp ->
                                FilterChip(
                                    selected = formGroup == grp,
                                    onClick = { formGroup = grp },
                                    label = { Text(grp, fontSize = 11.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = formZone,
                            onValueChange = { formZone = it },
                            label = { Text(if (isEn) "Area / Duty Zone" else "Zona / Area Tugas (mis. Kantin SMA)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = formTimeRange,
                            onValueChange = { formTimeRange = it },
                            label = { Text(if (isEn) "Time Display (e.g. 15:00 - 23:00)" else "Rentang Jam (mis. 15:00 - 23:00)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formStartHour,
                                onValueChange = { formStartHour = it.filter(Char::isDigit).take(2) },
                                label = { Text(if (isEn) "Start Hr" else "Jam Mulai") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formEndHour,
                                onValueChange = { formEndHour = it.filter(Char::isDigit).take(2) },
                                label = { Text(if (isEn) "End Hr" else "Jam Selesai") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formWorkHours,
                                onValueChange = { formWorkHours = it.filter(Char::isDigit).take(2) },
                                label = { Text(if (isEn) "Hrs" else "Durasi (j)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(if (isEn) "Badge Color" else "Warna Kode Shift", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(colorPalette) { hex ->
                                val col = parseJadwalShiftColor(hex)
                                val selected = formColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(if (selected) 3.dp else 1.dp, if (selected) Color.Black else Color.White, CircleShape)
                                        .clickable { formColorHex = hex }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = formSopNotes,
                            onValueChange = { formSopNotes = it },
                            label = { Text(if (isEn) "Duty SOP Description" else "Deskripsi SOP / Rincian Tugas") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showForm = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(if (isEn) "Back" else "Kembali")
                            }
                            Button(
                                onClick = {
                                    onSaveCode(
                                        editingTarget,
                                        formCode,
                                        formName,
                                        formGroup,
                                        formZone,
                                        formTimeRange,
                                        formStartHour.toIntOrNull() ?: 7,
                                        formEndHour.toIntOrNull() ?: 15,
                                        formWorkHours.toIntOrNull() ?: 8,
                                        formColorHex,
                                        formSopNotes
                                    )
                                    showForm = false
                                },
                                enabled = formCode.isNotBlank() && formName.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_save_shift_code")
                            ) {
                                Text(if (isEn) "Save Code" else "Simpan Kode", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(if (isEn) "Restore Official Shift Codes?" else "Pulihkan Preset Kode Resmi?") },
            text = {
                Text(
                    if (isEn) "Current school shift codes will be replaced with the official P1-P5, S1A-S4A, M1-M3, LP, L, IZIN, C catalog."
                    else "Daftar kode shift sekolah ini akan dikembalikan ke preset resmi P1–P5, S1A–S4A, M1–M3, LP, L, IZIN, dan C."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmReset = false
                        onResetDefault()
                    }
                ) {
                    Text(if (isEn) "Restore" else "Pulihkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportJadwalMatriksDialog(
    monthKey: String,
    entries: List<WaliScheduleEntry>,
    shiftCodes: List<WaliShiftCode>,
    schoolProfile: SchoolProfile,
    waliAccounts: List<WaliAccount> = emptyList(),
    initialTab: Int = 0,
    onSyncWaliAccounts: () -> Unit,
    onAddManualMember: (name: String, waliCode: String) -> Unit,
    onDeleteMember: (WaliScheduleEntry) -> Unit,
    onApplyPattern: (entry: WaliScheduleEntry, startDay: Int, patternCodes: List<String>, repeatUntilEnd: Boolean) -> Unit,
    onImportCsv: (csvText: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    val effectiveCodes = remember(shiftCodes, schoolProfile.tenantCode) {
        shiftCodes.ifEmpty { WaliScheduleHelper.defaultShiftCodes(schoolProfile.tenantCode) }
            .sortedBy { it.sortOrder }
    }
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    var manualName by remember { mutableStateOf("") }
    var manualCode by remember { mutableStateOf("") }

    var selectedPatternEntry by remember(entries) { mutableStateOf(entries.firstOrNull()) }
    var patternStartDay by remember { mutableStateOf("1") }
    var patternRepeat by remember { mutableStateOf(true) }
    val patternSequence = remember { mutableStateListOf<String>() }

    var csvText by remember { mutableStateOf("") }
    var uploadedCsvFileName by remember { mutableStateOf<String?>(null) }
    val templateCsvContent = remember(monthKey, entries, waliAccounts) {
        WaliScheduleHelper.buildScheduleTemplateCsv(
            monthKey = monthKey,
            existingEntries = entries,
            waliAccounts = waliAccounts,
            includeExistingShifts = true
        )
    }
    val csvPreview = remember(csvText, monthKey, entries, waliAccounts) {
        if (csvText.isBlank()) emptyList()
        else WaliScheduleHelper.parseMatrixCsv(
            rawText = csvText,
            monthKey = monthKey,
            schoolId = schoolProfile.tenantCode,
            existingEntries = entries,
            waliAccounts = waliAccounts
        )
    }

    val createTemplateCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    val contentWithBom = "\uFEFF$templateCsvContent"
                    os.write(contentWithBom.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(
                    context,
                    if (isEn) "Duty schedule CSV template downloaded!" else "Template CSV Jadwal Wali Asuh berhasil diunduh!",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    if (isEn) "Failed to download template: ${e.message}" else "Gagal mengunduh template: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val pickScheduleCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                val decoded = rawBytes?.let { StudentCsvParser.decodeCsvBytes(it) }.orEmpty()
                if (decoded.isNotBlank()) {
                    csvText = decoded
                    uploadedCsvFileName = uri.lastPathSegment?.substringAfterLast('/') ?: "jadwal_wali_asuh.csv"
                    val detected = WaliScheduleHelper.parseMatrixCsv(
                        rawText = decoded,
                        monthKey = monthKey,
                        schoolId = schoolProfile.tenantCode,
                        existingEntries = entries,
                        waliAccounts = waliAccounts
                    )
                    Toast.makeText(
                        context,
                        if (isEn) "CSV loaded: ${detected.size} caregiver schedules detected!"
                        else "File CSV dimuat: ${detected.size} jadwal Wali Asuh terdeteksi!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        if (isEn) "CSV file is empty." else "File CSV kosong.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    if (isEn) "Failed to read CSV file: ${e.message}" else "Gagal membaca file CSV: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .testTag("dialog_import_jadwal_matriks"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Setup & Import Month Schedule" else "Kelola Anggota & Impor Jadwal",
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${WaliScheduleHelper.formatMonthTitle(monthKey, isEn)} • ${schoolProfile.shortName.ifBlank { schoolProfile.tenantCode }}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF0F766E),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(if (isEn) "Members" else "Anggota", fontSize = 11.5.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(if (isEn) "Pattern" else "Pola Rotasi", fontSize = 11.5.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("CSV / Tabel", fontSize = 11.5.sp) }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        Button(
                            onClick = onSyncWaliAccounts,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_sync_wali_members")
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Sync All School Wali Asuh Accounts" else "Sinkron Semua Akun Wali Asuh Sekolah",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = manualCode,
                                onValueChange = { manualCode = it.uppercase() },
                                label = { Text("Kode", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(0.28f)
                            )
                            OutlinedTextField(
                                value = manualName,
                                onValueChange = { manualName = it },
                                label = { Text(if (isEn) "Manual Name" else "Tambah Nama Wali Manual", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(0.52f)
                                    .testTag("input_manual_wali_name")
                            )
                            Button(
                                onClick = {
                                    if (manualName.isNotBlank()) {
                                        onAddManualMember(manualName.trim(), manualCode.trim())
                                        manualName = ""
                                        manualCode = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_add_manual_member")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = if (isEn) "Add" else "Tambah")
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(entries.sortedBy { it.sortOrder }, key = { "${it.id}_${it.waliName}" }) { item ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (item.waliCode.isNotBlank()) "[${item.waliCode}] ${item.waliName}" else item.waliName,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        IconButton(
                                            onClick = { onDeleteMember(item) },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = if (isEn) "Remove" else "Hapus",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        if (entries.isEmpty()) {
                            Text(
                                text = if (isEn) "Sync or add Wali Asuh members in the Members tab first."
                                else "Tambahkan / sinkron anggota Wali Asuh terlebih dahulu pada tab Anggota.",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 420.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(if (isEn) "Select Caregiver Row:" else "Pilih Wali Asuh:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(entries.sortedBy { it.sortOrder }) { entry ->
                                        FilterChip(
                                            selected = selectedPatternEntry?.id == entry.id && selectedPatternEntry?.waliName == entry.waliName,
                                            onClick = { selectedPatternEntry = entry },
                                            label = { Text(entry.waliName, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = patternStartDay,
                                        onValueChange = { patternStartDay = it.filter(Char::isDigit).take(2) },
                                        label = { Text(if (isEn) "Start Day" else "Mulai Tgl") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.width(100.dp)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Switch(checked = patternRepeat, onCheckedChange = { patternRepeat = it })
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isEn) "Repeat to end of month" else "Ulangi s.d akhir bulan", fontSize = 11.5.sp)
                                    }
                                }

                                Text(
                                    text = if (isEn) "Tap shift codes in rotation order (e.g. P1 → S3A → M3 → LP → L):"
                                    else "Ketuk urutan kode pola rotasi (mis. P1 → S3A → M3 → LP → L):",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    effectiveCodes.forEach { sc ->
                                        FilterChip(
                                            selected = false,
                                            onClick = { patternSequence.add(sc.code) },
                                            label = { Text("+ ${sc.code}", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (patternSequence.isEmpty()) {
                                                if (isEn) "Sequence is empty" else "Urutan pola masih kosong"
                                            } else {
                                                patternSequence.joinToString(" → ")
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (patternSequence.isNotEmpty()) {
                                            TextButton(onClick = { patternSequence.clear() }) {
                                                Text(if (isEn) "Clear" else "Reset", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        val target = selectedPatternEntry
                                        if (target != null && patternSequence.isNotEmpty()) {
                                            onApplyPattern(
                                                target,
                                                patternStartDay.toIntOrNull() ?: 1,
                                                patternSequence.toList(),
                                                patternRepeat
                                            )
                                        }
                                    },
                                    enabled = selectedPatternEntry != null && patternSequence.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_apply_shift_pattern")
                                ) {
                                    Text(if (isEn) "Apply Pattern to Row" else "Terapkan Pola Rotasi", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    else -> {
                        val maxDay = WaliScheduleHelper.daysInMonth(monthKey)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 460.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF0FDFA),
                                border = BorderStroke(1.dp, Color(0xFF99F6E4)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isEn) {
                                            "Quick Workflow: Download the CSV template ($maxDay days pre-filled with this school's Wali Asuh list), edit shift codes in Excel/Sheets, then upload the CSV file."
                                        } else {
                                            "Cara Cepat: Unduh template CSV ($maxDay hari, otomatis berisi daftar Wali Asuh sekolah ini), isi kode shift di Excel/Sheets, lalu unggah kembali file CSV-nya."
                                        },
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF115E59),
                                        lineHeight = 16.sp
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val safeCode = schoolProfile.tenantCode.ifBlank { "SRT1" }
                                                    .replace(Regex("[^A-Za-z0-9_-]"), "_")
                                                createTemplateCsvLauncher.launch("Template_Jadwal_Wali_${safeCode}_${monthKey}.csv")
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_download_jadwal_csv_template")
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (isEn) "Download CSV" else "Template CSV",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { pickScheduleCsvLauncher.launch("*/*") },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_upload_jadwal_csv_file")
                                        ) {
                                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0369A1))
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (isEn) "Upload CSV" else "Upload File CSV",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0369A1)
                                            )
                                        }
                                    }
                                    if (!uploadedCsvFileName.isNullOrBlank()) {
                                        Text(
                                            text = if (isEn) "Loaded file: $uploadedCsvFileName" else "File dimuat: $uploadedCsvFileName",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0F766E)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (isEn) "Or paste CSV / Tab-separated matrix table from Excel or Google Sheets (No, Nama, 1..$maxDay):"
                                else "Atau tempel tabel matriks CSV / Excel / Spreadsheet (No, Nama, 1..$maxDay):",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = csvText,
                                onValueChange = { csvText = it },
                                placeholder = {
                                    Text(
                                        "No,Nama,1,2,3,4,5\n1,Eko Wahyudi,P1,S3A,M3,LP,L",
                                        fontSize = 11.sp
                                    )
                                },
                                minLines = 4,
                                maxLines = 7,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_csv_matrix")
                            )
                            if (csvPreview.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFDCFCE7),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF166534))
                                        Text(
                                            text = if (isEn) "${csvPreview.size} valid caregiver rows detected."
                                            else "Terdeteksi ${csvPreview.size} baris jadwal Wali Asuh siap diimpor.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                }
                            }
                            Button(
                                onClick = { onImportCsv(csvText) },
                                enabled = csvPreview.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_import_csv_matrix")
                            ) {
                                Text(if (isEn) "Import Schedule Matrix" else "Impor Matriks Jadwal", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaAnakAdminDialog(
    schoolProfile: SchoolProfile,
    waliAccounts: List<WaliAccount>,
    onToggleAnakAdmin: (WaliAccount, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    val activeSchoolId = schoolProfile.tenantCode.ifBlank { "SRT1-KDR" }
    val eligibleAccounts = remember(waliAccounts, activeSchoolId) {
        waliAccounts.filter { acc ->
            (acc.schoolId.isBlank() || acc.schoolId.equals(activeSchoolId, ignoreCase = true)) &&
                !acc.isSpecialPortalRole()
        }.sortedWith(
            compareByDescending<WaliAccount> { it.isAnakAdminSekolah() }.thenBy { it.name.lowercase() }
        )
    }
    val anakAdminCount = remember(eligibleAccounts) {
        eligibleAccounts.count { it.isAnakAdminSekolah() }
    }
    var searchQuery by remember { mutableStateOf("") }
    val filteredAccounts = remember(eligibleAccounts, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) eligibleAccounts
        else eligibleAccounts.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.username.contains(q, ignoreCase = true) ||
                it.dormitory.contains(q, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("dialog_kelola_anak_admin")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE0E7FF),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color(0xFF4338CA),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isEn) "Pin Schedule Sub-Admins" else "Sematkan Fitur Anak Admin Jadwal",
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${schoolProfile.schoolName.ifBlank { activeSchoolId }} • $anakAdminCount/${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL} ${if (isEn) "Quota" else "Kuota Terpakai"}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF4338CA),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_kelola_anak_admin")
                ) {
                    Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEEF2FF),
                border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isEn) {
                        "Select up to ${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL} Wali Asuh in this school. Pinned accounts stay on their normal Wali dashboard, with live permission to change shifts, pin RS/dorm tasks, and upload/download schedule CSVs."
                    } else {
                        "Pilih maksimal ${WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL} Wali Asuh di sekolah ini. Mereka tetap login sebagai Wali Asuh biasa, namun dapat langsung klik-ganti shift, menyematkan tugas RS/Asrama, serta upload & download CSV jadwal."
                    },
                    fontSize = 11.sp,
                    color = Color(0xFF312E81),
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isEn) "Search Wali Asuh..." else "Cari nama / asrama Wali Asuh...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_anak_admin_picker")
            )

            if (filteredAccounts.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isEn) {
                            "No eligible Wali Asuh accounts found for this school yet."
                        } else {
                            "Belum ada akun Wali Asuh terdaftar untuk sekolah ini."
                        },
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .testTag("list_picker_anak_admin"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredAccounts, key = { it.id }) { account ->
                        val isPinned = account.isAnakAdminSekolah()
                        val canEnable = isPinned || anakAdminCount < WaliAccount.MAX_ANAK_ADMIN_PER_SCHOOL
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPinned) Color(0xFFEEF2FF) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isPinned) Color(0xFF818CF8) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = canEnable) {
                                    onToggleAnakAdmin(account, !isPinned)
                                }
                                .testTag("row_picker_anak_admin_${account.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = account.name,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isPinned) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF4338CA)
                                            ) {
                                                Text(
                                                    text = if (isEn) "Sub-Admin" else "Anak Admin",
                                                    fontSize = 9.5.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${account.dormitory} • @${account.username}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isPinned,
                                    enabled = canEnable,
                                    onCheckedChange = { checked ->
                                        onToggleAnakAdmin(account, checked)
                                    },
                                    modifier = Modifier.testTag("switch_picker_anak_admin_${account.id}")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
