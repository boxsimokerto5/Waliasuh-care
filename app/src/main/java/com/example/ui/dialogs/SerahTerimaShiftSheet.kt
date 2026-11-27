package com.example.ui.dialogs

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ChildStudent
import com.example.data.model.FastingRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolProfile
import com.example.data.model.ShiftHandoverHelper
import com.example.data.model.ShiftHandoverPayload
import com.example.data.model.ShiftHandoverReport
import com.example.data.model.ShiftMessage
import com.example.data.model.WaliAccount
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliScheduleHelper
import com.example.data.model.WaliShiftCode
import com.example.ui.components.ConfigureEdgeToEdgeDialogWindow
import com.example.ui.components.rememberEdgeToEdgeDialogSafeInsets
import com.example.util.TopOnAdManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SerahTerimaShiftSheet(
    isEn: Boolean,
    reports: List<ShiftHandoverReport>,
    schoolProfile: SchoolProfile,
    initialMonthKey: String,
    initialDay: Int,
    allScheduleEntries: List<WaliScheduleEntry>,
    shiftCodes: List<WaliShiftCode>,
    students: List<ChildStudent>,
    medicalRecords: List<MedicalRecord>,
    fastingRecords: List<FastingRecord>,
    shiftMessages: List<ShiftMessage>,
    waliAccounts: List<WaliAccount>,
    currentUsername: String?,
    currentWaliName: String,
    onSyncOnline: (() -> Unit) -> Unit,
    onSaveReport: (
        existing: ShiftHandoverReport?,
        monthKey: String,
        day: Int,
        handoverType: String,
        status: String,
        title: String,
        schoolName: String,
        serviceUnit: String,
        dateLabel: String,
        handoverTime: String,
        fromShiftLabel: String,
        toShiftLabel: String,
        totalStudents: Int,
        presentStudents: Int,
        sickStudentsCount: Int,
        fastingStudentsCount: Int,
        payload: ShiftHandoverPayload,
        onDone: (Boolean, String, ShiftHandoverReport?) -> Unit
    ) -> Unit,
    onDeleteReport: (ShiftHandoverReport, (Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var editingExistingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var newDraftCandidate by remember { mutableStateOf<ShiftHandoverReport?>(null) }
    var createMonthKey by rememberSaveable { mutableStateOf(initialMonthKey) }
    var createDay by rememberSaveable {
        mutableIntStateOf(initialDay.coerceIn(1, WaliScheduleHelper.daysInMonth(initialMonthKey)))
    }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var statusFilter by rememberSaveable { mutableStateOf("ALL") } // ALL, DRAFT, FINAL
    var isSyncingNow by remember { mutableStateOf(false) }
    var pendingDeleteReport by remember { mutableStateOf<ShiftHandoverReport?>(null) }

    val liveEditingReport = remember(editingExistingId, reports) {
        editingExistingId?.let { targetId -> reports.firstOrNull { it.id == targetId } }
    }
    if (editingExistingId != null && liveEditingReport == null) {
        editingExistingId = null
    }

    Dialog(
        onDismissRequest = {
            if (liveEditingReport != null || newDraftCandidate != null) {
                editingExistingId = null
                newDraftCandidate = null
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        ConfigureEdgeToEdgeDialogWindow(lightStatusBars = true)

        val editorReport = liveEditingReport ?: newDraftCandidate
        if (editorReport != null) {
            SerahTerimaShiftEditor(
                initialReport = editorReport,
                isExistingRecord = liveEditingReport != null,
                isEn = isEn,
                schoolProfile = schoolProfile,
                allScheduleEntries = allScheduleEntries,
                shiftCodes = shiftCodes,
                students = students,
                medicalRecords = medicalRecords,
                fastingRecords = fastingRecords,
                shiftMessages = shiftMessages,
                waliAccounts = waliAccounts,
                currentUsername = currentUsername,
                currentWaliName = currentWaliName,
                onSaveReport = { existing, mKey, dayVal, hType, st, titleStr, sName, sUnit, dLabel, hTime, fromLbl, toLbl, tot, pres, sickC, fastC, payloadObj, cb ->
                    onSaveReport(
                        existing,
                        mKey,
                        dayVal,
                        hType,
                        st,
                        titleStr,
                        sName,
                        sUnit,
                        dLabel,
                        hTime,
                        fromLbl,
                        toLbl,
                        tot,
                        pres,
                        sickC,
                        fastC,
                        payloadObj
                    ) { ok, msg, saved ->
                        if (ok && saved != null) {
                            editingExistingId = saved.id
                            newDraftCandidate = null
                        }
                        cb(ok, msg, saved)
                    }
                },
                onBack = {
                    editingExistingId = null
                    newDraftCandidate = null
                }
            )
            return@Dialog
        }

        BackHandler { onDismiss() }

        val safeInsets = rememberEdgeToEdgeDialogSafeInsets()
        val draftReports = remember(reports) {
            reports.filter { it.status.equals(ShiftHandoverHelper.STATUS_DRAFT, ignoreCase = true) }
        }
        val finalReports = remember(reports) {
            reports.filter { it.status.equals(ShiftHandoverHelper.STATUS_FINAL, ignoreCase = true) }
        }
        val filteredReports = remember(reports, searchQuery, statusFilter) {
            val q = searchQuery.trim().lowercase()
            reports.filter { item ->
                val matchesFilter = when (statusFilter) {
                    ShiftHandoverHelper.STATUS_DRAFT -> item.status.equals(ShiftHandoverHelper.STATUS_DRAFT, ignoreCase = true)
                    ShiftHandoverHelper.STATUS_FINAL -> item.status.equals(ShiftHandoverHelper.STATUS_FINAL, ignoreCase = true)
                    else -> true
                }
                if (!matchesFilter) return@filter false
                if (q.isBlank()) return@filter true
                item.title.lowercase().contains(q) ||
                    item.dateLabel.lowercase().contains(q) ||
                    item.fromShiftLabel.lowercase().contains(q) ||
                    item.toShiftLabel.lowercase().contains(q) ||
                    item.authorName.lowercase().contains(q) ||
                    item.lastEditedBy.lowercase().contains(q) ||
                    item.payloadJson.lowercase().contains(q)
            }
        }
        val suggestedType = remember { ShiftHandoverHelper.suggestHandoverTypeForHour() }
        val createMaxDays = remember(createMonthKey) { WaliScheduleHelper.daysInMonth(createMonthKey) }
        val safeCreateDay = createDay.coerceIn(1, createMaxDays)

        fun launchNewDraft(type: String) {
            TopOnAdManager.onUserActionClick(context)
            val (draftReport, _) = ShiftHandoverHelper.buildAutoPopulatedDraft(
                schoolProfile = schoolProfile,
                monthKey = createMonthKey,
                day = safeCreateDay,
                handoverType = type,
                allScheduleEntries = allScheduleEntries,
                shiftCodes = shiftCodes,
                students = students,
                medicalRecords = medicalRecords,
                fastingRecords = fastingRecords,
                shiftMessages = shiftMessages,
                authorUsername = currentUsername.orEmpty(),
                authorName = currentWaliName
            )
            editingExistingId = null
            newDraftCandidate = draftReport
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(safeInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F766E).copy(alpha = 0.12f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Shift Handover Report" else "Serah Terima Shift",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = buildString {
                                    append(schoolProfile.shortName.ifBlank { schoolProfile.schoolName })
                                    append(" • ")
                                    append(if (isEn) "${reports.size} reports" else "${reports.size} laporan")
                                    if (draftReports.isNotEmpty()) {
                                        append(" • ${draftReports.size} draft")
                                    }
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = {
                                if (!isSyncingNow) {
                                    isSyncingNow = true
                                    onSyncOnline {
                                        isSyncingNow = false
                                        Toast.makeText(
                                            context,
                                            if (isEn) "Handover reports synced." else "Laporan serah terima tersinkron.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_handover_sync_pocketbase")
                        ) {
                            if (isSyncingNow) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = if (isEn) "Sync PocketBase" else "Sinkronkan PocketBase",
                                    tint = Color(0xFF0369A1)
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_close_serah_terima_shift")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isEn) "Close" else "Tutup"
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(safeInsets.only(WindowInsetsSides.Horizontal)),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 680.dp)
                        .testTag("list_serah_terima_shift"),
                    contentPadding = PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        top = 12.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quick Resume Draft Banner (jika ada draft yg belum diselesaikan)
                    if (draftReports.isNotEmpty()) {
                        val latestDraft = draftReports.maxByOrNull { it.updatedAt } ?: draftReports.first()
                        item(key = "resume_draft_banner") {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFFFBEB),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_resume_latest_handover_draft")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF59E0B).copy(alpha = 0.16f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = Color(0xFFB45309),
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(22.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isEn) {
                                                "${draftReports.size} Draft Waiting to Continue"
                                            } else {
                                                "${draftReports.size} Draft Belum Diselesaikan"
                                            },
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                        Text(
                                            text = "${latestDraft.title} • ${latestDraft.dateLabel}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF78350F),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            TopOnAdManager.onUserActionClick(context)
                                            newDraftCandidate = null
                                            editingExistingId = latestDraft.id
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("btn_resume_latest_draft")
                                    ) {
                                        Text(
                                            text = if (isEn) "Resume" else "Lanjutkan",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Quick Create Card
                    item(key = "quick_create_card") {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, Color(0xFF0F766E).copy(alpha = 0.25f)),
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF0F766E),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Create New Handover Report (Auto-Fill)"
                                        } else {
                                            "Buat Laporan Baru (Tarik Data Otomatis)"
                                        },
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F766E)
                                    )
                                }
                                Text(
                                    text = if (isEn) {
                                        "Automatically pulls roster staff, sick/UKS students, leave links, outside RS duty, fasting count, and can be saved as Draft or Final."
                                    } else {
                                        "Otomatis menarik petugas menyerahkan/menerima, siswa sakit/UKS, data izin & link bukti, jaga RS/luar asrama, dan dapat disimpan sebagai Draft atau Selesai."
                                    },
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Target Date Switcher
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (safeCreateDay > 1) {
                                                    createDay = safeCreateDay - 1
                                                } else {
                                                    val prevMonth = WaliScheduleHelper.shiftMonth(createMonthKey, -1)
                                                    createMonthKey = prevMonth
                                                    createDay = WaliScheduleHelper.daysInMonth(prevMonth)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("btn_handover_create_prev_day")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                contentDescription = if (isEn) "Previous Date" else "Tanggal Sebelumnya"
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = if (isEn) "Report Date" else "Tanggal Laporan",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = ShiftHandoverHelper.formatFullIndonesianDate(createMonthKey, safeCreateDay),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (safeCreateDay < createMaxDays) {
                                                    createDay = safeCreateDay + 1
                                                } else {
                                                    val nextMonth = WaliScheduleHelper.shiftMonth(createMonthKey, 1)
                                                    createMonthKey = nextMonth
                                                    createDay = 1
                                                }
                                            },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("btn_handover_create_next_day")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = if (isEn) "Next Date" else "Tanggal Berikutnya"
                                            )
                                        }
                                    }
                                }

                                // 4 Quick Transition Buttons
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        HandoverPresetButton(
                                            label = if (isEn) "Morning → Afternoon" else "Pagi → Sore",
                                            subLabel = "15:00 WIB",
                                            highlighted = suggestedType == ShiftHandoverHelper.TYPE_PAGI_KE_SORE,
                                            accent = Color(0xFF0F766E),
                                            testTag = "btn_create_handover_pagi_sore",
                                            onClick = { launchNewDraft(ShiftHandoverHelper.TYPE_PAGI_KE_SORE) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        HandoverPresetButton(
                                            label = if (isEn) "Afternoon → Night" else "Sore → Malam",
                                            subLabel = "23:00 WIB",
                                            highlighted = suggestedType == ShiftHandoverHelper.TYPE_SORE_KE_MALAM,
                                            accent = Color(0xFF0369A1),
                                            testTag = "btn_create_handover_sore_malam",
                                            onClick = { launchNewDraft(ShiftHandoverHelper.TYPE_SORE_KE_MALAM) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        HandoverPresetButton(
                                            label = if (isEn) "Night → Morning" else "Malam → Pagi",
                                            subLabel = "07:00 WIB",
                                            highlighted = suggestedType == ShiftHandoverHelper.TYPE_MALAM_KE_PAGI,
                                            accent = Color(0xFF4338CA),
                                            testTag = "btn_create_handover_malam_pagi",
                                            onClick = { launchNewDraft(ShiftHandoverHelper.TYPE_MALAM_KE_PAGI) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        HandoverPresetButton(
                                            label = if (isEn) "Custom Shift" else "Custom Shift",
                                            subLabel = if (isEn) "Manual edit" else "Judul & jam bebas",
                                            highlighted = false,
                                            accent = Color(0xFF64748B),
                                            testTag = "btn_create_handover_custom",
                                            onClick = { launchNewDraft(ShiftHandoverHelper.TYPE_CUSTOM) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Search & Filter Controls
                    item(key = "handover_search_filter") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = if (isEn) {
                                            "Search report, date, caregiver, notes..."
                                        } else {
                                            "Cari judul, tanggal, petugas, atau catatan..."
                                        },
                                        fontSize = 12.5.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_search_handover_reports")
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = statusFilter == "ALL",
                                    onClick = { statusFilter = "ALL" },
                                    label = {
                                        Text(
                                            text = if (isEn) "All (${reports.size})" else "Semua (${reports.size})",
                                            fontSize = 12.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("chip_handover_filter_all")
                                )
                                FilterChip(
                                    selected = statusFilter == ShiftHandoverHelper.STATUS_DRAFT,
                                    onClick = { statusFilter = ShiftHandoverHelper.STATUS_DRAFT },
                                    label = {
                                        Text(
                                            text = "Draft (${draftReports.size})",
                                            fontSize = 12.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("chip_handover_filter_draft")
                                )
                                FilterChip(
                                    selected = statusFilter == ShiftHandoverHelper.STATUS_FINAL,
                                    onClick = { statusFilter = ShiftHandoverHelper.STATUS_FINAL },
                                    label = {
                                        Text(
                                            text = if (isEn) "Final (${finalReports.size})" else "Selesai (${finalReports.size})",
                                            fontSize = 12.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("chip_handover_filter_final")
                                )
                            }
                        }
                    }

                    // List of Reports or Empty State
                    if (filteredReports.isEmpty()) {
                        item(key = "handover_empty_state") {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Text(
                                        text = if (reports.isEmpty()) {
                                            if (isEn) "No shift handover reports yet" else "Belum ada laporan serah terima shift"
                                        } else {
                                            if (isEn) "No matching reports found" else "Tidak ada laporan yang cocok"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Tap any shift transition preset above to auto-fill student, leave, and duty roster data."
                                        } else {
                                            "Pilih salah satu tombol pergantian shift di atas untuk membuat laporan otomatis dan siap dibagikan ke WhatsApp."
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredReports, key = { it.id }) { report ->
                            ShiftHandoverCard(
                                report = report,
                                isEn = isEn,
                                onEdit = {
                                    TopOnAdManager.onUserActionClick(context)
                                    newDraftCandidate = null
                                    editingExistingId = report.id
                                },
                                onShareWa = {
                                    TopOnAdManager.onUserActionClick(context)
                                    val text = ShiftHandoverHelper.formatWhatsAppReport(
                                        report = report,
                                        allScheduleEntries = allScheduleEntries,
                                        shiftCodes = shiftCodes
                                    )
                                    shareHandoverToWhatsApp(context, text, isEn)
                                },
                                onCopy = {
                                    val text = ShiftHandoverHelper.formatWhatsAppReport(
                                        report = report,
                                        allScheduleEntries = allScheduleEntries,
                                        shiftCodes = shiftCodes
                                    )
                                    copyHandoverText(context, text, isEn)
                                },
                                onDelete = { pendingDeleteReport = report }
                            )
                        }
                    }

                    item(key = "handover_bottom_safe_spacer") {
                        Spacer(
                            modifier = Modifier.windowInsetsPadding(
                                safeInsets.only(WindowInsetsSides.Bottom)
                            )
                        )
                    }
                }
            }
        }

        pendingDeleteReport?.let { toDelete ->
            AlertDialog(
                onDismissRequest = { pendingDeleteReport = null },
                title = {
                    Text(
                        text = if (isEn) "Delete Handover Report?" else "Hapus Laporan Serah Terima?",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = if (isEn) {
                            "Remove \"${toDelete.title}\" (${toDelete.dateLabel}) across devices?"
                        } else {
                            "Hapus laporan \"${toDelete.title}\" (${toDelete.dateLabel})? Perubahan akan disinkronkan ke server PocketBase."
                        },
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteReport(toDelete) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                            pendingDeleteReport = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("btn_confirm_delete_handover")
                    ) {
                        Text(if (isEn) "Delete" else "Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteReport = null }) {
                        Text(if (isEn) "Cancel" else "Batal")
                    }
                }
            )
        }
    }
}

@Composable
private fun HandoverPresetButton(
    label: String,
    subLabel: String,
    highlighted: Boolean,
    accent: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (highlighted) accent else accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, if (highlighted) accent else accent.copy(alpha = 0.35f)),
        modifier = modifier
            .heightIn(min = 52.dp)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlighted) Color.White else accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subLabel,
                fontSize = 10.5.sp,
                color = if (highlighted) Color.White.copy(alpha = 0.88f) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShiftHandoverCard(
    report: ShiftHandoverReport,
    isEn: Boolean,
    onEdit: () -> Unit,
    onShareWa: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val isDraft = report.status.equals(ShiftHandoverHelper.STATUS_DRAFT, ignoreCase = true)
    val payload = remember(report.id, report.payloadJson) {
        ShiftHandoverHelper.parsePayload(report.payloadJson)
    }
    val updatedLabel = remember(report.updatedAt) {
        runCatching {
            SimpleDateFormat("dd MMM • HH:mm", Locale("id", "ID")).format(Date(report.updatedAt))
        }.getOrDefault("")
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isDraft) Color(0xFFF59E0B).copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
        ),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_handover_report_${report.id}")
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
                    color = if (isDraft) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                ) {
                    Text(
                        text = if (isDraft) {
                            if (isEn) "DRAFT • INCOMPLETE" else "DRAFT • LANJUTKAN"
                        } else {
                            if (isEn) "FINAL" else "SELESAI"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDraft) Color(0xFFB45309) else Color(0xFF065F46),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = report.handoverTime,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F766E)
                )
            }

            Text(
                text = report.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${report.dateLabel} • ${report.fromShiftLabel} → ${report.toShiftLabel}",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatPill("Total ${report.totalStudents}", Color(0xFF334155))
                StatPill("Hadir ${report.presentStudents}", Color(0xFF0F766E))
                StatPill("Sakit ${report.sickStudentsCount}", Color(0xFFDC2626))
                StatPill("Puasa ${report.fastingStudentsCount}", Color(0xFF0369A1))
                if (payload.leaveEntries.isNotEmpty()) {
                    StatPill("Izin Wali ${payload.leaveEntries.size}", Color(0xFFB45309))
                }
                if (payload.outsideDutyStaff.isNotEmpty()) {
                    StatPill("Jaga RS ${payload.outsideDutyStaff.size}", Color(0xFF4338CA))
                }
            }

            if (payload.followUpNotes.isNotBlank()) {
                Text(
                    text = "📝 ${payload.followUpNotes.trim()}",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = buildString {
                    append(if (isEn) "Author: " else "Penyusun: ")
                    append(report.authorName.ifBlank { "Wali Asuh" })
                    if (report.lastEditedBy.isNotBlank() && report.lastEditedBy != report.authorName) {
                        append(" • ")
                        append(if (isEn) "Edited: " else "Edit: ")
                        append(report.lastEditedBy)
                    }
                    if (updatedLabel.isNotBlank()) {
                        append(" ($updatedLabel)")
                    }
                },
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDraft) Color(0xFFD97706) else Color(0xFF0F766E)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("btn_edit_handover_${report.id}")
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDraft) {
                            if (isEn) "Continue Draft" else "Lanjutkan Draft"
                        } else {
                            if (isEn) "Edit" else "Edit"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = onShareWa,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("btn_share_wa_handover_${report.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "WA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
                    )
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_copy_handover_${report.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = if (isEn) "Copy report" else "Salin teks laporan",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_delete_handover_${report.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = if (isEn) "Delete report" else "Hapus laporan",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}
