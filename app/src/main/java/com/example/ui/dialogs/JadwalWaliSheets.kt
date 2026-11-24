package com.example.ui.dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PinnedTaskItem
import com.example.data.model.SchoolProfile
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliScheduleHelper
import com.example.data.model.WaliShiftCode
import com.example.ui.theme.WaliPrimary
import com.example.util.PdfReportGenerator
import com.example.util.i18n.LocalAppStrings
import java.io.File

internal fun parseJadwalShiftColor(hex: String, fallback: Color = Color(0xFF0284C7)): Color {
    return try {
        val clean = hex.trim()
        if (clean.isBlank()) fallback else Color(android.graphics.Color.parseColor(clean))
    } catch (_: Exception) {
        fallback
    }
}

internal fun openJadwalExternalUrl(context: Context, rawUrl: String, isEn: Boolean) {
    val clean = rawUrl.trim()
    if (clean.isBlank()) {
        Toast.makeText(
            context,
            if (isEn) "Drive link is not attached yet." else "Link Drive belum dilampirkan admin.",
            Toast.LENGTH_SHORT
        ).show()
        return
    }
    val normalized = if (clean.startsWith("http://", ignoreCase = true) ||
        clean.startsWith("https://", ignoreCase = true)
    ) {
        clean
    } else {
        "https://$clean"
    }
    val ok = runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }.isSuccess
    if (!ok) {
        Toast.makeText(
            context,
            if (isEn) "Unable to open link: $normalized" else "Tidak dapat membuka tautan: $normalized",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun SchedulePinChip(
    pin: PinnedTaskItem,
    modifier: Modifier = Modifier
) {
    val isFaskes = pin.category.equals(WaliScheduleHelper.PIN_FASKES, ignoreCase = true)
    val bgColor = if (isFaskes) Color(0xFFFEE2E2) else Color(0xFFE0E7FF)
    val borderColor = if (isFaskes) Color(0xFFFCA5A5) else Color(0xFFC7D2FE)
    val fgColor = if (isFaskes) Color(0xFFB91C1C) else Color(0xFF3730A3)
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isFaskes) Icons.Default.LocalHospital else Icons.Default.PushPin,
                contentDescription = null,
                tint = fgColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = pin.label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = fgColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalMatriksSheet(
    monthKey: String,
    entries: List<WaliScheduleEntry>,
    shiftCodes: List<WaliShiftCode>,
    schoolProfile: SchoolProfile,
    canManageSchedule: Boolean,
    canPinTasks: Boolean = canManageSchedule,
    actorName: String,
    onChangeMonth: (String) -> Unit,
    onSelectCellToEdit: (WaliScheduleEntry, Int) -> Unit,
    onOpenPersonal: (WaliScheduleEntry) -> Unit,
    onOpenImportDialog: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    val daysInMonth = remember(monthKey) { WaliScheduleHelper.daysInMonth(monthKey) }
    val todayMonth = remember { WaliScheduleHelper.currentMonthKey() }
    val todayDay = remember { WaliScheduleHelper.currentDayOfMonth() }
    val effectiveCodes = remember(shiftCodes, schoolProfile.tenantCode) {
        shiftCodes.ifEmpty { WaliScheduleHelper.defaultShiftCodes(schoolProfile.tenantCode) }
    }
    val shiftMap = remember(effectiveCodes, schoolProfile.tenantCode) {
        WaliScheduleHelper.buildShiftMap(effectiveCodes, schoolProfile.tenantCode)
    }

    var searchQuery by remember { mutableStateOf("") }
    var showBreakdown by remember { mutableStateOf(true) }
    var inspectedCell by remember { mutableStateOf<Pair<WaliScheduleEntry, Int>?>(null) }
    var readyPdfFile by remember { mutableStateOf<File?>(null) }

    val filteredEntries = remember(entries, searchQuery) {
        val sorted = entries.sortedWith(compareBy<WaliScheduleEntry> { it.sortOrder }.thenBy { it.waliName.lowercase() })
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) sorted
        else sorted.filter {
            it.waliName.lowercase().contains(q) ||
                it.waliCode.lowercase().contains(q) ||
                it.waliUsername.lowercase().contains(q)
        }
    }

    val horizontalScrollState = rememberScrollState()
    val nameColWidth = 148.dp
    val dayCellWidth = 42.dp
    val statColWidth = 36.dp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("sheet_jadwal_matriks"),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isEn) "Monthly Schedule Matrix" else "Matriks Jadwal Bulanan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${WaliScheduleHelper.formatMonthTitle(monthKey, isEn)} • ${filteredEntries.size} ${if (isEn) "Caregivers" else "Wali Asuh"}",
                                fontSize = 11.5.sp,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_close_jadwal_matriks")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isEn) "Close" else "Tutup",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val file = PdfReportGenerator.generateJadwalMatriksPdf(
                                    context = context,
                                    monthKey = monthKey,
                                    entries = entries,
                                    shiftCodes = effectiveCodes,
                                    waliName = actorName,
                                    schoolProfile = schoolProfile
                                )
                                if (file != null) {
                                    readyPdfFile = file
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isEn) "Failed to generate schedule PDF." else "Gagal membuat PDF matriks jadwal.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.testTag("btn_jadwal_matriks_pdf")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = if (isEn) "Export Matrix PDF" else "Ekspor PDF Matriks",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0F766E),
                        titleContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Month Switcher + Search + Quick PDF / Import
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onChangeMonth(WaliScheduleHelper.shiftMonth(monthKey, -1)) },
                                modifier = Modifier.testTag("btn_matriks_prev_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = if (isEn) "Previous Month" else "Bulan Sebelumnya"
                                )
                            }
                            Text(
                                text = WaliScheduleHelper.formatMonthTitle(monthKey, isEn),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F766E),
                                modifier = Modifier.testTag("text_matriks_month_title")
                            )
                            IconButton(
                                onClick = { onChangeMonth(WaliScheduleHelper.shiftMonth(monthKey, 1)) },
                                modifier = Modifier.testTag("btn_matriks_next_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = if (isEn) "Next Month" else "Bulan Berikutnya"
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        if (isEn) "Search caregiver..." else "Cari nama Wali Asuh...",
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_matriks_search")
                            )

                            if (canManageSchedule && onOpenImportDialog != null) {
                                OutlinedButton(
                                    onClick = onOpenImportDialog,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_matriks_open_import")
                                ) {
                                    Text(
                                        text = if (isEn) "Setup" else "Kelola",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = if (isEn) {
                                    "No schedule matrix entries for ${WaliScheduleHelper.formatMonthTitle(monthKey, true)} yet."
                                } else {
                                    "Belum ada data jadwal bulan ${WaliScheduleHelper.formatMonthTitle(monthKey, false)}."
                                },
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (canManageSchedule && onOpenImportDialog != null) {
                                Button(
                                    onClick = onOpenImportDialog,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(if (isEn) "Sync / Import Roster" else "Sinkron / Impor Jadwal")
                                }
                            }
                        }
                    }
                } else {
                    // Matrix Header Row (Sticky Left Name Column + Scrollable Day & Stat Headers)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F766E))
                    ) {
                        Box(
                            modifier = Modifier
                                .width(nameColWidth)
                                .height(42.dp)
                                .border(0.5.dp, Color.White.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = if (isEn) "Wali Name" else "Nama Wali Asuh",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }

                        Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                            for (day in 1..daysInMonth) {
                                val dow = WaliScheduleHelper.dayOfWeekShort(monthKey, day, isEn)
                                val isTodayCell = monthKey == todayMonth && day == todayDay
                                val isWeekend = dow.equals("Min", true) || dow.equals("Sun", true) ||
                                    dow.equals("Sab", true) || dow.equals("Sat", true)
                                Box(
                                    modifier = Modifier
                                        .width(dayCellWidth)
                                        .height(42.dp)
                                        .background(
                                            when {
                                                isTodayCell -> Color(0xFF0284C7)
                                                isWeekend -> Color(0xFFBE123C)
                                                else -> Color(0xFF0F766E)
                                            }
                                        )
                                        .border(0.5.dp, Color.White.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = dow,
                                            fontSize = 9.sp,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                            listOf("PF", "S", "M", "LP", "OFF", "JK").forEach { statLabel ->
                                Box(
                                    modifier = Modifier
                                        .width(if (statLabel == "JK") 46.dp else statColWidth)
                                        .height(42.dp)
                                        .background(Color(0xFF115E59))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = statLabel,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Matrix Data Rows + Optional Breakdown Footer
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        itemsIndexed(filteredEntries, key = { _, e -> "${e.id}_${e.waliName}" }) { idx, entry ->
                            val dayCodes = remember(entry.dayCodesJson) { WaliScheduleHelper.parseDayCodes(entry.dayCodesJson) }
                            val dayPins = remember(entry.dayPinsJson) { WaliScheduleHelper.parseDayPins(entry.dayPinsJson) }
                            val dayLeaves = remember(entry.dayLeaveJson) { WaliScheduleHelper.parseDayLeaves(entry.dayLeaveJson) }
                            val rowStats = remember(entry.dayCodesJson, daysInMonth, shiftMap) {
                                WaliScheduleHelper.computeRowStats(entry, daysInMonth, shiftMap)
                            }
                            val rowBg = if (idx % 2 == 0) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowBg),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sticky Left Caregiver Cell
                                Row(
                                    modifier = Modifier
                                        .width(nameColWidth)
                                        .height(44.dp)
                                        .clickable { onOpenPersonal(entry) }
                                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        .padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${idx + 1}.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.waliName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (entry.waliCode.isNotBlank()) {
                                            Text(
                                                text = entry.waliCode,
                                                fontSize = 9.5.sp,
                                                color = Color(0xFF0F766E),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                // Scrollable Day Cells + Row Summary
                                Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                                    for (day in 1..daysInMonth) {
                                        val code = dayCodes[day]?.let { WaliScheduleHelper.normalizeCode(it) }.orEmpty()
                                        val pins = dayPins[day].orEmpty()
                                        val leave = dayLeaves[day]
                                        val shiftDef = shiftMap[code]
                                        val cellColor = if (code.isNotBlank()) {
                                            parseJadwalShiftColor(shiftDef?.colorHex ?: "#64748B")
                                        } else {
                                            Color.Transparent
                                        }
                                        val isTodayCell = monthKey == todayMonth && day == todayDay

                                        Box(
                                            modifier = Modifier
                                                .width(dayCellWidth)
                                                .height(44.dp)
                                                .background(
                                                    if (code.isNotBlank()) cellColor.copy(alpha = 0.14f)
                                                    else if (isTodayCell) Color(0xFF0284C7).copy(alpha = 0.06f)
                                                    else Color.Transparent
                                                )
                                                .border(
                                                    width = if (isTodayCell) 1.2.dp else 0.5.dp,
                                                    color = if (isTodayCell) Color(0xFF0284C7) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                                )
                                                .clickable {
                                                    if (canManageSchedule || canPinTasks) {
                                                        onSelectCellToEdit(entry, day)
                                                    } else {
                                                        inspectedCell = entry to day
                                                    }
                                                }
                                                .testTag("jadwal_matriks_cell_${entry.id}_$day"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (code.isNotBlank()) {
                                                Text(
                                                    text = code,
                                                    fontSize = if (code.length >= 4) 9.5.sp else 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = cellColor,
                                                    maxLines = 1
                                                )
                                            } else {
                                                Text(
                                                    text = "-",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                            if (pins.isNotEmpty() || leave != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(3.dp)
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            when {
                                                                leave != null -> Color(0xFFDC2626)
                                                                pins.any { it.category == WaliScheduleHelper.PIN_FASKES } -> Color(0xFFE11D48)
                                                                else -> Color(0xFF0F766E)
                                                            }
                                                        )
                                                )
                                            }
                                        }
                                    }
                                    val statsVals = listOf(
                                        rowStats.pFull,
                                        rowStats.sCount,
                                        rowStats.mCount,
                                        rowStats.lpCount,
                                        rowStats.offCount,
                                        rowStats.totalJamKerja
                                    )
                                    statsVals.forEachIndexed { sIdx, value ->
                                        val isJk = sIdx == statsVals.lastIndex
                                        Box(
                                            modifier = Modifier
                                                .width(if (isJk) 46.dp else statColWidth)
                                                .height(44.dp)
                                                .background(if (isJk) Color(0xFFF0FDFA) else Color.Transparent)
                                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = value.toString(),
                                                fontSize = 11.sp,
                                                fontWeight = if (isJk || sIdx == 0) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isJk) Color(0xFF0F766E) else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Daily Shift Breakdown Section
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showBreakdown = !showBreakdown }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                            .testTag("btn_toggle_matriks_breakdown"),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isEn) "Daily Shift Staffing Breakdown" else "Ringkasan Kebutuhan Shift Harian",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F766E)
                                        )
                                        Icon(
                                            imageVector = if (showBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = Color(0xFF0F766E)
                                        )
                                    }
                                    if (showBreakdown) {
                                        val breakdowns = remember(entries, monthKey, shiftMap, daysInMonth) {
                                            (1..daysInMonth).map { d ->
                                                WaliScheduleHelper.computeDayBreakdown(entries, d, shiftMap)
                                            }
                                        }
                                        val summarySpecs = listOf(
                                            Triple(if (isEn) "P1 (07-15)" else "(P1) Pagi 1", false) { b: com.example.data.model.DayShiftBreakdown -> b.p1 },
                                            Triple(if (isEn) "P2 (08-16)" else "(P2) Pagi 2", false) { b -> b.p2 },
                                            Triple(if (isEn) "P3 (Ceremony)" else "(P3) Upacara", false) { b -> b.p3 },
                                            Triple(if (isEn) "P4 (Visit)" else "(P4) Kunjungan", false) { b -> b.p4 },
                                            Triple(if (isEn) "PAGI FULL" else "PAGI FULL", true) { b -> b.pagiFull },
                                            Triple(if (isEn) "TOTAL SORE" else "TOTAL SORE", true) { b -> b.totalSore },
                                            Triple(if (isEn) "TOTAL MALAM" else "TOTAL MALAM", true) { b -> b.totalMalam },
                                            Triple(if (isEn) "LEAVE / CUTI" else "IZIN / CUTI", false) { b -> b.izin + b.cuti },
                                            Triple(if (isEn) "OFF + LP" else "OFF / LEPAS", false) { b -> b.offAndLepas }
                                        )
                                        summarySpecs.forEach { (label, boldRow, extract) ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (boldRow) Color(0xFFF0FDFA)
                                                        else MaterialTheme.colorScheme.surface
                                                    ),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(nameColWidth)
                                                        .height(30.dp)
                                                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                                        .padding(horizontal = 8.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    Text(
                                                        text = label,
                                                        fontSize = 10.sp,
                                                        fontWeight = if (boldRow) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (boldRow) Color(0xFF0F766E) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1
                                                    )
                                                }
                                                Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                                                    breakdowns.forEach { bd ->
                                                        val cnt = extract(bd)
                                                        Box(
                                                            modifier = Modifier
                                                                .width(dayCellWidth)
                                                                .height(30.dp)
                                                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = cnt.toString(),
                                                                fontSize = 10.sp,
                                                                fontWeight = if (boldRow && cnt > 0) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (cnt > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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

    // Read-only cell inspection popup for regular Wali Asuh
    inspectedCell?.let { (entry, day) ->
        val dayCodes = WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)
        val dayPins = WaliScheduleHelper.parseDayPins(entry.dayPinsJson)[day].orEmpty()
        val leave = WaliScheduleHelper.parseDayLeaves(entry.dayLeaveJson)[day]
        val code = dayCodes[day]?.let { WaliScheduleHelper.normalizeCode(it) }.orEmpty()
        val shift = shiftMap[code]
        AlertDialog(
            onDismissRequest = { inspectedCell = null },
            title = {
                Text(
                    text = "${entry.waliName} • ${WaliScheduleHelper.formatDateChipLabel(monthKey, day, isEn)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (code.isBlank()) {
                        Text(if (isEn) "No shift assigned on this date." else "Belum ada shift pada tanggal ini.")
                    } else {
                        Text(
                            text = if (shift != null) "${shift.name} (${shift.code}) • ${shift.timeRange}" else code,
                            fontWeight = FontWeight.Bold,
                            color = parseJadwalShiftColor(shift?.colorHex ?: "#0F766E")
                        )
                        if (shift?.sopNotes?.isNotBlank() == true) {
                            Text(
                                text = shift.sopNotes,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (dayPins.isNotEmpty()) {
                        Text(
                            text = if (isEn) "Pinned Tasks:" else "Tugas Disematkan:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        dayPins.forEach { pin ->
                            SchedulePinChip(pin = pin)
                            if (pin.note.isNotBlank()) {
                                Text(
                                    text = pin.note,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (leave != null) {
                        Text(
                            text = "${leave.status}${if (leave.note.isNotBlank()) ": ${leave.note}" else ""}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB91C1C)
                        )
                        if (leave.driveUrl.isNotBlank()) {
                            OutlinedButton(
                                onClick = { openJadwalExternalUrl(context, leave.driveUrl, isEn) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEn) "Open Drive Link" else "Buka Link Drive")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectedCell = null }) {
                    Text(if (isEn) "Close" else "Tutup")
                }
            }
        )
    }

    readyPdfFile?.let { file ->
        AlertDialog(
            onDismissRequest = { readyPdfFile = null },
            title = { Text(if (isEn) "Schedule Matrix PDF Ready" else "PDF Matriks Jadwal Siap") },
            text = {
                Text(
                    if (isEn) {
                        "Official landscape matrix PDF has been generated. Choose to open or share."
                    } else {
                        "Dokumen PDF resmi Matriks Jadwal Kerja Wali Asuh siap dibuka atau dibagikan."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        readyPdfFile = null
                        PdfReportGenerator.openPdfFile(context, file)
                    }
                ) {
                    Text(if (isEn) "Open PDF" else "Buka PDF")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        readyPdfFile = null
                        PdfReportGenerator.shareJadwalPdf(
                            context = context,
                            pdfFile = file,
                            subject = if (isEn) "Official Wali Asuh Schedule ($monthKey)" else "Jadwal Resmi Wali Asuh ($monthKey)",
                            text = if (isEn) "Attached is the official Wali Asuh work schedule matrix." else "Terlampir Jadwal Kerja Resmi Wali Asuh."
                        )
                    }
                ) {
                    Text(if (isEn) "Share PDF" else "Bagikan")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalPersonalSheet(
    entry: WaliScheduleEntry,
    monthKey: String,
    shiftCodes: List<WaliShiftCode>,
    schoolProfile: SchoolProfile,
    canManageSchedule: Boolean,
    canPinTasks: Boolean = canManageSchedule,
    onEditDay: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    val daysInMonth = remember(monthKey) { WaliScheduleHelper.daysInMonth(monthKey) }
    val todayMonth = remember { WaliScheduleHelper.currentMonthKey() }
    val todayDay = remember { WaliScheduleHelper.currentDayOfMonth() }
    val effectiveCodes = remember(shiftCodes, schoolProfile.tenantCode) {
        shiftCodes.ifEmpty { WaliScheduleHelper.defaultShiftCodes(schoolProfile.tenantCode) }
    }
    val shiftMap = remember(effectiveCodes, schoolProfile.tenantCode) {
        WaliScheduleHelper.buildShiftMap(effectiveCodes, schoolProfile.tenantCode)
    }
    val dayCodes = remember(entry.dayCodesJson) { WaliScheduleHelper.parseDayCodes(entry.dayCodesJson) }
    val dayPins = remember(entry.dayPinsJson) { WaliScheduleHelper.parseDayPins(entry.dayPinsJson) }
    val dayLeaves = remember(entry.dayLeaveJson) { WaliScheduleHelper.parseDayLeaves(entry.dayLeaveJson) }
    val stats = remember(entry.dayCodesJson, daysInMonth, shiftMap) {
        WaliScheduleHelper.computeRowStats(entry, daysInMonth, shiftMap)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("sheet_jadwal_personal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0F766E).copy(alpha = 0.14f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF0F766E))
                        }
                    }
                    Column {
                        Text(
                            text = entry.waliName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${WaliScheduleHelper.formatMonthTitle(monthKey, isEn)} • ${stats.totalJamKerja} ${if (isEn) "Work Hours" else "Jam Kerja"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F766E)
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_jadwal_personal")
                ) {
                    Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                }
            }

            // KPI Summary Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Triple("Pagi", stats.pFull.toString(), Color(0xFF0284C7)),
                    Triple("Sore", stats.sCount.toString(), Color(0xFFD97706)),
                    Triple("Malam", stats.mCount.toString(), Color(0xFF7C3AED)),
                    Triple("LP/Off", (stats.lpCount + stats.offCount).toString(), Color(0xFF475569)),
                    Triple(if (isEn) "Hrs" else "JK", "${stats.totalJamKerja}j", Color(0xFF0F766E))
                ).forEach { (lbl, value, accent) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accent.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = accent)
                            Text(lbl, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items((1..daysInMonth).toList(), key = { it }) { day ->
                    val code = dayCodes[day]?.let { WaliScheduleHelper.normalizeCode(it) }.orEmpty()
                    val pins = dayPins[day].orEmpty()
                    val leave = dayLeaves[day]
                    val shift = shiftMap[code]
                    val shiftColor = if (code.isNotBlank()) parseJadwalShiftColor(shift?.colorHex ?: "#64748B") else Color(0xFF94A3B8)
                    val isToday = monthKey == todayMonth && day == todayDay

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (canManageSchedule || canPinTasks) Modifier.clickable { onEditDay(day) }
                                else Modifier
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isToday) Color(0xFFF0FDFA) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isToday) 3.dp else 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isToday) Color(0xFF0F766E) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.width(56.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = day.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = WaliScheduleHelper.dayOfWeekShort(monthKey, day, isEn),
                                        fontSize = 10.sp,
                                        color = if (isToday) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = when {
                                        shift != null -> "${shift.name} (${shift.code})"
                                        code.isNotBlank() -> code
                                        else -> if (isEn) "Unassigned" else "Belum Diatur"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (code.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (shift != null && shift.timeRange.isNotBlank() && shift.timeRange != "-") {
                                    Text(
                                        text = "${shift.timeRange}${if (shift.zoneLabel.isNotBlank()) " • ${shift.zoneLabel}" else ""}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                pins.forEach { pin ->
                                    SchedulePinChip(pin = pin)
                                }
                                if (leave != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEE2E2)
                                        ) {
                                            Text(
                                                text = leave.status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB91C1C),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (leave.driveUrl.isNotBlank()) {
                                            TextButton(
                                                onClick = { openJadwalExternalUrl(context, leave.driveUrl, isEn) },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                            ) {
                                                Text(
                                                    text = if (isEn) "Drive Link" else "Link Drive",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = shiftColor
                            ) {
                                Text(
                                    text = code.ifBlank { "-" },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PetugasJamIniDialog(
    monthKey: String,
    day: Int,
    entries: List<WaliScheduleEntry>,
    shiftCodes: List<WaliShiftCode>,
    schoolProfile: SchoolProfile,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    val effectiveCodes = remember(shiftCodes, schoolProfile.tenantCode) {
        shiftCodes.ifEmpty { WaliScheduleHelper.defaultShiftCodes(schoolProfile.tenantCode) }
    }
    val shiftMap = remember(effectiveCodes, schoolProfile.tenantCode) {
        WaliScheduleHelper.buildShiftMap(effectiveCodes, schoolProfile.tenantCode)
    }
    var selectedHour by remember { mutableIntStateOf(WaliScheduleHelper.currentHourOfDay()) }

    val activeAtHour = remember(entries, day, selectedHour, shiftMap) {
        entries.sortedBy { it.sortOrder }.mapNotNull { entry ->
            val code = WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)[day]
                ?.let { WaliScheduleHelper.normalizeCode(it) }.orEmpty()
            if (code.isBlank()) return@mapNotNull null
            val def = shiftMap[code] ?: return@mapNotNull null
            if (!WaliScheduleHelper.isShiftActiveAtHour(def, selectedHour)) return@mapNotNull null
            val pins = WaliScheduleHelper.parseDayPins(entry.dayPinsJson)[day].orEmpty()
            Triple(entry, def, pins)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_petugas_jam_ini"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF0F766E))
                Column {
                    Text(
                        text = if (isEn) "Caregivers On Duty Now" else "Petugas Aktif Jam Ini",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${WaliScheduleHelper.formatDateChipLabel(monthKey, day, isEn)} • ${String.format("%02d:00 WIB", selectedHour)}",
                        fontSize = 11.5.sp,
                        color = Color(0xFF0F766E)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items((0..23).toList()) { hr ->
                        FilterChip(
                            selected = selectedHour == hr,
                            onClick = { selectedHour = hr },
                            label = {
                                Text(
                                    text = String.format("%02d:00", hr),
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedHour == hr) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("chip_hour_$hr")
                        )
                    }
                }

                if (activeAtHour.isEmpty()) {
                    Text(
                        text = if (isEn) "No caregiver shifts active at ${String.format("%02d:00", selectedHour)}."
                        else "Tidak ada Wali Asuh terjadwal aktif pada pukul ${String.format("%02d:00", selectedHour)} WIB.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(activeAtHour, key = { it.first.id.toString() + it.first.waliName }) { (entry, shift, pins) ->
                            val badgeColor = parseJadwalShiftColor(shift.colorHex)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = entry.waliName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(shape = RoundedCornerShape(6.dp), color = badgeColor) {
                                            Text(
                                                text = shift.code,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${shift.name} • ${shift.timeRange}${if (shift.zoneLabel.isNotBlank()) " (${shift.zoneLabel})" else ""}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    pins.forEach { pin ->
                                        SchedulePinChip(pin = pin)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("btn_close_petugas_jam_ini")
            ) {
                Text(if (isEn) "Done" else "Tutup")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekapIzinJadwalSheet(
    monthKey: String,
    entries: List<WaliScheduleEntry>,
    canManageSchedule: Boolean,
    onEditCell: (WaliScheduleEntry, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    val daysInMonth = remember(monthKey) { WaliScheduleHelper.daysInMonth(monthKey) }

    val leaveItems = remember(entries, daysInMonth) {
        buildList {
            entries.sortedBy { it.sortOrder }.forEach { entry ->
                val leaves = WaliScheduleHelper.parseDayLeaves(entry.dayLeaveJson)
                leaves.entries.sortedBy { it.key }.forEach { (day, info) ->
                    if (day in 1..daysInMonth) {
                        add(Triple(entry, day, info))
                    }
                }
            }
        }.sortedBy { it.second }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("sheet_rekap_izin_jadwal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEn) "Monthly Leave & Sick Recap" else "Rekap Rekan Izin / Sakit / Cuti",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${WaliScheduleHelper.formatMonthTitle(monthKey, isEn)} • ${leaveItems.size} ${if (isEn) "Entries" else "Catatan"}",
                        fontSize = 12.sp,
                        color = Color(0xFFBE123C),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                }
            }

            if (leaveItems.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isEn) "No leave or sick notes recorded in this month."
                        else "Belum ada rekan Wali Asuh yang tercatat izin, sakit, atau cuti di bulan ini.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 460.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(leaveItems, key = { "${it.first.id}_${it.second}" }) { (entry, day, leave) ->
                        ElevatedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.waliName,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = WaliScheduleHelper.formatDateChipLabel(monthKey, day, isEn),
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFEE2E2)
                                    ) {
                                        Text(
                                            text = leave.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFB91C1C),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                if (leave.note.isNotBlank()) {
                                    Text(
                                        text = leave.note,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (leave.driveUrl.isNotBlank()) {
                                        Button(
                                            onClick = { openJadwalExternalUrl(context, leave.driveUrl, isEn) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_open_leave_drive_${entry.id}_$day")
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isEn) "Open Drive Link" else "Buka Link Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (canManageSchedule) {
                                        OutlinedButton(
                                            onClick = { onEditCell(entry, day) },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.heightIn(min = 48.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isEn) "Edit" else "Ubah", fontSize = 12.sp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarKodeSopSheet(
    shiftCodes: List<WaliShiftCode>,
    schoolProfile: SchoolProfile,
    canManageSchedule: Boolean,
    onOpenManageShiftCodes: () -> Unit,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    val effectiveCodes = remember(shiftCodes, schoolProfile.tenantCode) {
        shiftCodes.ifEmpty { WaliScheduleHelper.defaultShiftCodes(schoolProfile.tenantCode) }
            .sortedBy { it.sortOrder }
    }
    var selectedGroup by remember { mutableStateOf("ALL") }
    var search by remember { mutableStateOf("") }

    val filtered = remember(effectiveCodes, selectedGroup, search) {
        val q = search.trim().lowercase()
        effectiveCodes.filter { sc ->
            val groupMatch = selectedGroup == "ALL" || sc.shiftGroup.equals(selectedGroup, ignoreCase = true)
            val textMatch = q.isBlank() ||
                sc.code.lowercase().contains(q) ||
                sc.name.lowercase().contains(q) ||
                sc.zoneLabel.lowercase().contains(q) ||
                sc.sopNotes.lowercase().contains(q)
            groupMatch && textMatch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("sheet_daftar_kode_sop")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEn) "Shift Codes & Duty SOP" else "Daftar Kode Shift & SOP Tugas",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
                        fontSize = 12.sp,
                        color = Color(0xFF0F766E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                }
            }

            if (canManageSchedule) {
                Button(
                    onClick = {
                        onDismiss()
                        onOpenManageShiftCodes()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("btn_open_manage_shift_codes")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEn) "Manage School Shift Codes (Admin)" else "Kelola Kode Shift Sekolah (Admin)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text(if (isEn) "Search shift code or SOP..." else "Cari kode shift, area, atau SOP...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val tabs = listOf(
                    "ALL" to if (isEn) "All" else "Semua",
                    WaliScheduleHelper.GROUP_PAGI to "Pagi",
                    WaliScheduleHelper.GROUP_SORE to "Sore",
                    WaliScheduleHelper.GROUP_MALAM to "Malam",
                    WaliScheduleHelper.GROUP_LEPAS to "Lepas/Libur",
                    WaliScheduleHelper.GROUP_IZIN to "Izin/Cuti"
                )
                items(tabs) { (key, lbl) ->
                    FilterChip(
                        selected = selectedGroup == key,
                        onClick = { selectedGroup = key },
                        label = { Text(lbl, fontSize = 11.5.sp) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { "${it.id}_${it.code}" }) { sc ->
                    val color = parseJadwalShiftColor(sc.colorHex)
                    ElevatedCard(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = color) {
                                        Text(
                                            text = sc.code,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Column {
                                        Text(sc.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                        if (sc.zoneLabel.isNotBlank()) {
                                            Text(sc.zoneLabel, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                                Text(
                                    text = "${sc.timeRange} (${sc.workHours}j)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (sc.sopNotes.isNotBlank()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                                Text(
                                    text = sc.sopNotes,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PenyematanTugasSheet(
    monthKey: String,
    selectedDay: Int,
    entries: List<WaliScheduleEntry>,
    shiftCodes: List<WaliShiftCode>,
    schoolProfile: SchoolProfile,
    canManageSchedule: Boolean,
    canPinTasks: Boolean = canManageSchedule,
    onEditCell: (WaliScheduleEntry, Int) -> Unit,
    onOpenRosterSetup: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    val canInteractPins = canManageSchedule || canPinTasks
    val shiftMap = remember(shiftCodes, schoolProfile.tenantCode) {
        WaliScheduleHelper.buildShiftMap(shiftCodes, schoolProfile.tenantCode)
    }
    val maxDay = remember(monthKey) { WaliScheduleHelper.daysInMonth(monthKey) }
    var activeDay by remember(selectedDay, maxDay) {
        mutableIntStateOf(selectedDay.coerceIn(1, maxDay))
    }
    var fullMonthMode by remember { mutableStateOf(false) }
    var manageAllMode by remember(canInteractPins) { mutableStateOf(canInteractPins) }
    var filterCat by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val pinnedRows = remember(entries, activeDay, fullMonthMode, filterCat, searchQuery) {
        val q = searchQuery.trim()
        buildList {
            entries.sortedBy { it.sortOrder }.forEach { entry ->
                if (q.isNotBlank() && !entry.waliName.contains(q, ignoreCase = true) && !entry.waliUsername.contains(q, ignoreCase = true)) {
                    return@forEach
                }
                val dayCodes = WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)
                val dayPins = WaliScheduleHelper.parseDayPins(entry.dayPinsJson)
                val targetDays = if (fullMonthMode) dayPins.keys.sorted() else listOf(activeDay)
                targetDays.forEach { d ->
                    val pins = dayPins[d].orEmpty().filter { pin ->
                        filterCat == "ALL" || pin.category.equals(filterCat, ignoreCase = true)
                    }
                    if (pins.isNotEmpty()) {
                        add(Triple(entry, d, dayCodes[d].orEmpty() to pins))
                    }
                }
            }
        }
    }

    val manageableRows = remember(entries, activeDay, filterCat, searchQuery) {
        val q = searchQuery.trim()
        entries.sortedBy { it.sortOrder }.mapNotNull { entry ->
            if (q.isNotBlank() && !entry.waliName.contains(q, ignoreCase = true) && !entry.waliUsername.contains(q, ignoreCase = true)) {
                return@mapNotNull null
            }
            val code = WaliScheduleHelper.parseDayCodes(entry.dayCodesJson)[activeDay].orEmpty()
            val pins = WaliScheduleHelper.parseDayPins(entry.dayPinsJson)[activeDay].orEmpty()
            if (filterCat == WaliScheduleHelper.PIN_FASKES &&
                pins.none { it.category.equals(WaliScheduleHelper.PIN_FASKES, ignoreCase = true) }
            ) {
                return@mapNotNull null
            }
            Triple(entry, code, pins)
        }.sortedWith(
            compareByDescending<Triple<WaliScheduleEntry, String, List<PinnedTaskItem>>> { it.third.isNotEmpty() }
                .thenBy { it.first.sortOrder }
                .thenBy { it.first.waliName.lowercase() }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("sheet_penyematan_tugas")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEn) "Pinned Tasks & Faskes Duties" else "Penyematan Tugas & Jaga Faskes",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (fullMonthMode) WaliScheduleHelper.formatMonthTitle(monthKey, isEn)
                        else WaliScheduleHelper.formatDateChipLabel(monthKey, activeDay, isEn),
                        fontSize = 12.sp,
                        color = Color(0xFF0F766E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_penyematan_tugas")
                ) {
                    Icon(Icons.Default.Close, contentDescription = if (isEn) "Close" else "Tutup")
                }
            }

            if (canInteractPins) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (canManageSchedule) Color(0xFFEEF2FF) else Color(0xFFF0FDFA),
                    border = BorderStroke(1.dp, if (canManageSchedule) Color(0xFFC7D2FE) else Color(0xFF99F6E4)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("badge_penyematan_manage_mode")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (canManageSchedule) Icons.Default.AdminPanelSettings else Icons.Default.PushPin,
                            contentDescription = null,
                            tint = if (canManageSchedule) Color(0xFF4338CA) else Color(0xFF0F766E),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (canManageSchedule) {
                                if (isEn) {
                                    "Management Mode active: tap any Wali Asuh below to change shifts or attach/remove hospital & extra tasks."
                                } else {
                                    "Mode Kelola Tugas aktif: ketuk nama Wali Asuh untuk langsung mengganti shift (P1–M3/LP/L) atau menyematkan tugas RS & asrama."
                                }
                            } else {
                                if (isEn) {
                                    "Task Pin Mode active: tap any Wali Asuh below to pin/remove Extra Tasks & RS/Faskes duties (shifts locked)."
                                } else {
                                    "Mode Penyematan Tugas aktif: ketuk nama Wali Asuh untuk menyematkan/mencabut Tugas Tambahan & Jaga RS/Faskes (shift terkunci)."
                                }
                            },
                            fontSize = 11.sp,
                            color = if (canManageSchedule) Color(0xFF312E81) else Color(0xFF115E59),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            if (!fullMonthMode) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { activeDay = if (activeDay <= 1) maxDay else activeDay - 1 },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_pin_prev_day")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = if (isEn) "Previous Day" else "Hari Sebelumnya"
                            )
                        }
                        Text(
                            text = WaliScheduleHelper.formatDateChipLabel(monthKey, activeDay, isEn),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E)
                        )
                        IconButton(
                            onClick = { activeDay = if (activeDay >= maxDay) 1 else activeDay + 1 },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_pin_next_day")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = if (isEn) "Next Day" else "Hari Berikutnya"
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (canInteractPins) {
                    FilterChip(
                        selected = !fullMonthMode && manageAllMode,
                        onClick = {
                            fullMonthMode = false
                            manageAllMode = true
                        },
                        label = {
                            Text(
                                text = if (isEn) "Manage All (${entries.size})" else "Kelola Semua (${entries.size})",
                                fontSize = 11.5.sp
                            )
                        },
                        modifier = Modifier.testTag("chip_pin_mode_kelola")
                    )
                }
                FilterChip(
                    selected = !fullMonthMode && (!canInteractPins || !manageAllMode),
                    onClick = {
                        fullMonthMode = false
                        manageAllMode = false
                    },
                    label = {
                        Text(
                            text = if (isEn) "Pinned Only" else "Sudah Dipin (${pinnedRows.size})",
                            fontSize = 11.5.sp
                        )
                    },
                    modifier = Modifier.testTag("chip_pin_mode_sudah_dipin")
                )
                FilterChip(
                    selected = fullMonthMode,
                    onClick = {
                        fullMonthMode = true
                        manageAllMode = false
                    },
                    label = { Text(if (isEn) "Full Month" else "Satu Bulan", fontSize = 11.5.sp) },
                    modifier = Modifier.testTag("chip_pin_mode_sebulan")
                )
                FilterChip(
                    selected = filterCat == WaliScheduleHelper.PIN_FASKES,
                    onClick = {
                        filterCat = if (filterCat == WaliScheduleHelper.PIN_FASKES) "ALL" else WaliScheduleHelper.PIN_FASKES
                    },
                    label = { Text("RS/Faskes", fontSize = 11.5.sp) },
                    modifier = Modifier.testTag("chip_pin_mode_faskes")
                )
            }

            if (canInteractPins && entries.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isEn) "Search Wali Asuh..." else "Cari nama Wali Asuh...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_penyematan_wali")
                )
            }

            val showInteractiveAllList = canInteractPins && manageAllMode && !fullMonthMode

            if (showInteractiveAllList) {
                if (manageableRows.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (entries.isEmpty()) {
                                    if (isEn) "No caregiver roster entries found for this month yet."
                                    else "Belum ada data personel Wali Asuh di jadwal bulan ini."
                                } else {
                                    if (isEn) "No matching Wali Asuh for this filter."
                                    else "Tidak ada Wali Asuh yang cocok dengan pencarian/filter."
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (canManageSchedule && entries.isEmpty() && onOpenRosterSetup != null) {
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onOpenRosterSetup()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_pin_sync_roster")
                                ) {
                                    Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isEn) "Pull Wali Asuh Into Schedule" else "Tarik Daftar Wali Asuh ke Jadwal",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .testTag("list_penyematan_kelola_wali"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(manageableRows, key = { it.first.id }) { (entry, code, pins) ->
                            val normalizedCode = WaliScheduleHelper.normalizeCode(code)
                            val shift = shiftMap[normalizedCode]
                            val badgeColor = parseJadwalShiftColor(shift?.colorHex ?: "#64748B", Color(0xFF64748B))
                            ElevatedCard(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = if (pins.isNotEmpty()) Color(0xFFF0FDFA) else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditCell(entry, activeDay) }
                                    .testTag("card_manage_pin_wali_${entry.id}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = entry.waliName,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (normalizedCode.isBlank()) Color(0xFFE2E8F0) else badgeColor
                                                ) {
                                                    Text(
                                                        text = normalizedCode.ifBlank { "-" },
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (normalizedCode.isBlank()) Color(0xFF475569) else Color.White,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = shift?.name ?: if (isEn) "Shift not set" else "Shift belum diatur",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { onEditCell(entry, activeDay) },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Color(0xFF0F766E)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_manage_pin_wali_${entry.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = Color(0xFF0F766E),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (canManageSchedule) {
                                                    if (isEn) "Shift / Pin" else "Ubah / Pin"
                                                } else {
                                                    if (isEn) "Pin Task" else "Pin Tugas"
                                                },
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F766E)
                                            )
                                        }
                                    }
                                    if (pins.isNotEmpty()) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            pins.forEach { pin ->
                                                SchedulePinChip(pin = pin)
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = if (canManageSchedule) {
                                                if (isEn) "No extra task pinned yet • Tap to change shift or pin RS/dorm task"
                                                else "Belum ada pin tugas • Ketuk untuk ganti shift atau sematkan tugas RS/Asrama"
                                            } else {
                                                if (isEn) "No extra task pinned yet • Tap to pin extra task or RS/Faskes duty"
                                                else "Belum ada pin tugas • Ketuk untuk sematkan tugas tambahan atau jaga RS/Faskes"
                                            },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (pinnedRows.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isEn) "No pinned tasks or referral duties in this view."
                            else "Belum ada penyematan tugas khusus atau jaga faskes pada periode ini.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (canInteractPins && entries.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    fullMonthMode = false
                                    manageAllMode = true
                                },
                                modifier = Modifier.testTag("btn_pin_switch_to_manage_all")
                            ) {
                                Text(
                                    text = if (isEn) "Switch to Manage All Wali Asuh" else "Buka Mode Kelola Semua Wali Asuh",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pinnedRows, key = { "${it.first.id}_${it.second}" }) { (entry, day, codeAndPins) ->
                        val (code, pins) = codeAndPins
                        val shift = shiftMap[WaliScheduleHelper.normalizeCode(code)]
                        ElevatedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.then(
                                if (canInteractPins) Modifier.clickable { onEditCell(entry, day) } else Modifier
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(entry.waliName, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${WaliScheduleHelper.formatDateChipLabel(monthKey, day, isEn)} • ${shift?.name ?: code.ifBlank { "-" }}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (canInteractPins) {
                                        IconButton(
                                            onClick = { onEditCell(entry, day) },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = if (isEn) "Edit Pins" else "Ubah Pin",
                                                tint = Color(0xFF0F766E)
                                            )
                                        }
                                    }
                                }
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    pins.forEach { pin ->
                                        SchedulePinChip(pin = pin)
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
