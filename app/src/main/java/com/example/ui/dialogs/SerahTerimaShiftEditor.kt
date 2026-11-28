package com.example.ui.dialogs

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.FastingRecord
import com.example.data.model.HaidRecord
import com.example.data.model.HandoverLeaveItem
import com.example.data.model.HandoverOutsideDutyItem
import com.example.data.model.HandoverSickStudentItem
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
import com.example.ui.components.rememberEdgeToEdgeDialogSafeInsets
import java.util.UUID

internal fun shareHandoverToWhatsApp(context: Context, text: String, isEn: Boolean) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(
            Intent.createChooser(
                intent,
                if (isEn) "Share Shift Handover Report" else "Bagikan Laporan Serah Terima Shift"
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        )
    }
}

internal fun copyHandoverText(context: Context, text: String, isEn: Boolean) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    cm?.setPrimaryClip(ClipData.newPlainText("Laporan Serah Terima Shift", text))
    Toast.makeText(
        context,
        if (isEn) "Report text copied!" else "Teks laporan serah terima disalin!",
        Toast.LENGTH_SHORT
    ).show()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SerahTerimaShiftEditor(
    initialReport: ShiftHandoverReport,
    isExistingRecord: Boolean,
    isEn: Boolean,
    schoolProfile: SchoolProfile,
    allScheduleEntries: List<WaliScheduleEntry>,
    shiftCodes: List<WaliShiftCode>,
    students: List<ChildStudent>,
    medicalRecords: List<MedicalRecord>,
    fastingRecords: List<FastingRecord>,
    haidRecords: List<HaidRecord> = emptyList(),
    shiftMessages: List<ShiftMessage>,
    waliAccounts: List<WaliAccount>,
    currentUsername: String?,
    currentWaliName: String,
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
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val safeInsets = rememberEdgeToEdgeDialogSafeInsets()
    val initialPayload = remember(initialReport.id, initialReport.payloadJson) {
        ShiftHandoverHelper.parsePayload(initialReport.payloadJson)
    }

    var monthKey by remember(initialReport.id) { mutableStateOf(initialReport.monthKey) }
    var day by remember(initialReport.id) { mutableIntStateOf(initialReport.day) }
    var handoverType by remember(initialReport.id) { mutableStateOf(initialReport.handoverType) }
    var status by remember(initialReport.id) { mutableStateOf(initialReport.status) }
    var title by remember(initialReport.id) { mutableStateOf(initialReport.title) }
    var schoolName by remember(initialReport.id) { mutableStateOf(initialReport.schoolName) }
    var serviceUnit by remember(initialReport.id) { mutableStateOf(initialReport.serviceUnit) }
    var dateLabel by remember(initialReport.id) { mutableStateOf(initialReport.dateLabel) }
    var handoverTime by remember(initialReport.id) { mutableStateOf(initialReport.handoverTime) }
    var fromShiftLabel by remember(initialReport.id) { mutableStateOf(initialReport.fromShiftLabel) }
    var toShiftLabel by remember(initialReport.id) { mutableStateOf(initialReport.toShiftLabel) }

    var totalStudentsText by remember(initialReport.id) { mutableStateOf(initialReport.totalStudents.toString()) }
    var presentStudentsText by remember(initialReport.id) { mutableStateOf(initialReport.presentStudents.toString()) }
    var sickCountText by remember(initialReport.id) { mutableStateOf(initialReport.sickStudentsCount.toString()) }
    var fastingCountText by remember(initialReport.id) { mutableStateOf(initialReport.fastingStudentsCount.toString()) }
    var haidCountEditedManually by remember(initialReport.id) { mutableStateOf(false) }
    var haidCountText by remember(initialReport.id) {
        val initialHaid = initialPayload.haidStudentsCount ?: if (haidRecords.isNotEmpty()) {
            ShiftHandoverHelper.countActiveHaidForDate(haidRecords, initialReport.monthKey, initialReport.day)
        } else {
            0
        }
        mutableStateOf(initialHaid.toString())
    }

    var sickStudents by remember(initialReport.id) { mutableStateOf(initialPayload.sickStudents) }
    var leaveEntries by remember(initialReport.id) { mutableStateOf(initialPayload.leaveEntries) }
    var handingOverStaff by remember(initialReport.id) { mutableStateOf(initialPayload.handingOverStaff) }
    var receivingStaff by remember(initialReport.id) { mutableStateOf(initialPayload.receivingStaff) }
    var outsideDutyStaff by remember(initialReport.id) { mutableStateOf(initialPayload.outsideDutyStaff) }
    var followUpNotes by remember(initialReport.id) { mutableStateOf(initialPayload.followUpNotes) }
    var lastAutoFollowUpNotes by remember(initialReport.id) {
        mutableStateOf(if (isExistingRecord) "" else initialPayload.followUpNotes)
    }
    var completedActivities by remember(initialReport.id) { mutableStateOf(initialPayload.completedActivities) }
    var footerText by remember(initialReport.id) { mutableStateOf(initialPayload.footerText) }
    var syncedShiftMessageId by remember(initialReport.id) { mutableStateOf(initialPayload.syncedShiftMessageId) }
    var autoSyncShiftMessage by remember(initialReport.id) {
        mutableStateOf(
            if (isExistingRecord) {
                initialPayload.autoSyncShiftMessage || initialPayload.syncedShiftMessageId > 0L
            } else {
                initialPayload.followUpNotes.isBlank()
            }
        )
    }

    LaunchedEffect(initialReport.id, allScheduleEntries, shiftCodes, haidRecords, monthKey, day) {
        if (allScheduleEntries.isNotEmpty() && leaveEntries.isNotEmpty()) {
            val enriched = ShiftHandoverHelper.enrichLeaveEntriesFromSchedule(
                existingLeaves = leaveEntries,
                schoolId = initialReport.schoolId.ifBlank { schoolProfile.tenantCode },
                monthKey = monthKey,
                day = day,
                allScheduleEntries = allScheduleEntries,
                shiftCodes = shiftCodes
            )
            if (enriched != leaveEntries) {
                leaveEntries = enriched
            }
        }
        if (initialPayload.haidStudentsCount == null && !haidCountEditedManually && haidRecords.isNotEmpty()) {
            haidCountText = ShiftHandoverHelper.countActiveHaidForDate(haidRecords, monthKey, day).toString()
        }
    }

    val followUpVoiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val transcript = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (transcript.isNotBlank()) {
                followUpNotes = appendVoiceTranscript(followUpNotes, transcript)
                autoSyncShiftMessage = true
            }
        }
    }

    fun startFollowUpVoiceInput() {
        val intent = buildGoogleSpeechToTextIntent(
            promptText = if (isEn) {
                "Speak follow-up notes for next shift..."
            } else {
                "Ucapkan catatan tindak lanjut untuk shift berikutnya..."
            },
            languageTag = if (isEn) "en-US" else "id-ID"
        )
        try {
            followUpVoiceLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                if (isEn) {
                    "Google Voice-to-Text service is not available on this device."
                } else {
                    "Layanan Suara ke Teks Google belum tersedia di perangkat ini."
                },
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                if (isEn) {
                    "Unable to launch voice input: ${e.message ?: "Error"}"
                } else {
                    "Gagal membuka input suara: ${e.message ?: "Error"}"
                },
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    var showPreview by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showStudentPicker by remember { mutableStateOf(false) }
    // 0 = none, 1 = handingOver, 2 = receiving
    var waliPickerTarget by remember { mutableIntStateOf(0) }
    var newActivityText by remember { mutableStateOf("") }

    fun currentPayload(): ShiftHandoverPayload = ShiftHandoverPayload(
        sickStudents = sickStudents,
        leaveEntries = leaveEntries,
        handingOverStaff = handingOverStaff,
        receivingStaff = receivingStaff,
        outsideDutyStaff = outsideDutyStaff,
        followUpNotes = followUpNotes,
        completedActivities = completedActivities,
        footerText = footerText,
        syncedShiftMessageId = syncedShiftMessageId,
        autoSyncShiftMessage = autoSyncShiftMessage,
        haidStudentsCount = haidCountText.toIntOrNull()?.coerceAtLeast(0) ?: 0
    )

    fun currentPreviewReport(targetStatus: String = status): ShiftHandoverReport = initialReport.copy(
        monthKey = monthKey,
        day = day,
        handoverType = handoverType,
        status = targetStatus,
        title = title,
        schoolName = schoolName,
        serviceUnit = serviceUnit,
        dateLabel = dateLabel,
        handoverTime = handoverTime,
        fromShiftLabel = fromShiftLabel,
        toShiftLabel = toShiftLabel,
        totalStudents = totalStudentsText.toIntOrNull() ?: 0,
        presentStudents = presentStudentsText.toIntOrNull() ?: 0,
        sickStudentsCount = sickCountText.toIntOrNull() ?: sickStudents.size,
        fastingStudentsCount = fastingCountText.toIntOrNull() ?: 0
    )

    fun reloadAutoData(targetMonthKey: String, targetDay: Int, targetType: String) {
        val canRefreshFollowUp = followUpNotes.isBlank() || followUpNotes == lastAutoFollowUpNotes
        val (autoRep, autoPay) = ShiftHandoverHelper.buildAutoPopulatedDraft(
            schoolProfile = schoolProfile,
            monthKey = targetMonthKey,
            day = targetDay,
            handoverType = targetType,
            allScheduleEntries = allScheduleEntries,
            shiftCodes = shiftCodes,
            students = students,
            medicalRecords = medicalRecords,
            fastingRecords = fastingRecords,
            shiftMessages = shiftMessages,
            authorUsername = currentUsername.orEmpty(),
            authorName = currentWaliName,
            existingFollowUpNotes = if (canRefreshFollowUp) "" else followUpNotes,
            existingActivities = completedActivities,
            haidRecords = haidRecords
        )
        monthKey = autoRep.monthKey
        day = autoRep.day
        handoverType = autoRep.handoverType
        title = autoRep.title
        schoolName = autoRep.schoolName
        serviceUnit = autoRep.serviceUnit
        dateLabel = autoRep.dateLabel
        handoverTime = autoRep.handoverTime
        fromShiftLabel = autoRep.fromShiftLabel
        toShiftLabel = autoRep.toShiftLabel
        totalStudentsText = autoRep.totalStudents.toString()
        presentStudentsText = autoRep.presentStudents.toString()
        sickCountText = autoRep.sickStudentsCount.toString()
        fastingCountText = autoRep.fastingStudentsCount.toString()
        haidCountText = (autoPay.haidStudentsCount ?: 0).toString()
        haidCountEditedManually = false
        sickStudents = autoPay.sickStudents
        leaveEntries = autoPay.leaveEntries
        handingOverStaff = autoPay.handingOverStaff
        receivingStaff = autoPay.receivingStaff
        outsideDutyStaff = autoPay.outsideDutyStaff
        if (canRefreshFollowUp) {
            followUpNotes = autoPay.followUpNotes
            lastAutoFollowUpNotes = autoPay.followUpNotes
            if (syncedShiftMessageId == 0L) autoSyncShiftMessage = false
        }
        if (footerText.isBlank()) footerText = autoPay.footerText
    }

    fun triggerSave(targetStatus: String, closeAfter: Boolean = false, onSaved: (ShiftHandoverReport) -> Unit = {}) {
        status = targetStatus
        onSaveReport(
            if (isExistingRecord) initialReport else null,
            monthKey,
            day,
            handoverType,
            targetStatus,
            title,
            schoolName,
            serviceUnit,
            dateLabel,
            handoverTime,
            fromShiftLabel,
            toShiftLabel,
            totalStudentsText.toIntOrNull() ?: 0,
            presentStudentsText.toIntOrNull() ?: 0,
            sickCountText.toIntOrNull() ?: sickStudents.size,
            fastingCountText.toIntOrNull() ?: 0,
            currentPayload()
        ) { ok, msg, saved ->
            if (msg.isNotBlank()) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            if (ok && saved != null) {
                val savedPayload = ShiftHandoverHelper.parsePayload(saved.payloadJson)
                if (savedPayload.syncedShiftMessageId > 0L) {
                    syncedShiftMessageId = savedPayload.syncedShiftMessageId
                }
                onSaved(saved)
                if (closeAfter) onBack()
            }
        }
    }

    val currentWaText = remember(
        monthKey, day, handoverType, status, title, schoolName, serviceUnit, dateLabel,
        handoverTime, fromShiftLabel, toShiftLabel, totalStudentsText, presentStudentsText,
        sickCountText, fastingCountText, haidCountText, sickStudents, leaveEntries, handingOverStaff,
        receivingStaff, outsideDutyStaff, followUpNotes, completedActivities, footerText,
        allScheduleEntries, shiftCodes, haidRecords
    ) {
        ShiftHandoverHelper.formatWhatsAppReport(
            report = currentPreviewReport(),
            payload = currentPayload(),
            allScheduleEntries = allScheduleEntries,
            shiftCodes = shiftCodes,
            haidRecords = haidRecords
        )
    }

    BackHandler {
        showUnsavedDialog = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(safeInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showUnsavedDialog = true },
                        modifier = Modifier.size(48.dp).testTag("btn_handover_editor_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isEn) "Back" else "Kembali"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Handover Editor" else "Editor Serah Terima",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val isFinal = status == ShiftHandoverHelper.STATUS_FINAL
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isFinal) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = if (isFinal) {
                                    if (isEn) "FINAL" else "SELESAI"
                                } else "DRAFT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isFinal) Color(0xFF166534) else Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    TextButton(
                        onClick = { triggerSave(ShiftHandoverHelper.STATUS_DRAFT, closeAfter = false) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("btn_handover_top_draft")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Draft", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = { shareHandoverToWhatsApp(context, currentWaText, isEn) },
                        modifier = Modifier.size(48.dp).testTag("btn_handover_top_share")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share WhatsApp", tint = Color(0xFF0F766E))
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            safeInsets.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                                .union(WindowInsets.ime.only(WindowInsetsSides.Bottom))
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { triggerSave(ShiftHandoverHelper.STATUS_DRAFT, closeAfter = true) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("btn_handover_save_draft")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Save Draft" else "Simpan Draft", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = { triggerSave(ShiftHandoverHelper.STATUS_FINAL, closeAfter = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("btn_handover_save_final")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Finalize" else "Selesai & Simpan", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(safeInsets.only(WindowInsetsSides.Horizontal))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Header & Pergantian Shift
                EditorSectionCard(title = "📋 Header & Pergantian Shift") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val newM: String
                                val newD: Int
                                if (day > 1) {
                                    newM = monthKey
                                    newD = day - 1
                                } else {
                                    newM = WaliScheduleHelper.shiftMonth(monthKey, -1)
                                    newD = WaliScheduleHelper.daysInMonth(newM)
                                }
                                reloadAutoData(newM, newD, handoverType)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Hari Sebelumnya")
                        }
                        Text(
                            text = dateLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E)
                        )
                        IconButton(
                            onClick = {
                                val maxD = WaliScheduleHelper.daysInMonth(monthKey)
                                val newM: String
                                val newD: Int
                                if (day < maxD) {
                                    newM = monthKey
                                    newD = day + 1
                                } else {
                                    newM = WaliScheduleHelper.shiftMonth(monthKey, 1)
                                    newD = 1
                                }
                                reloadAutoData(newM, newD, handoverType)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Hari Berikutnya")
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            ShiftHandoverHelper.TYPE_PAGI_KE_SORE to "Pagi → Sore",
                            ShiftHandoverHelper.TYPE_SORE_KE_MALAM to "Sore → Malam",
                            ShiftHandoverHelper.TYPE_MALAM_KE_PAGI to "Malam → Pagi",
                            ShiftHandoverHelper.TYPE_CUSTOM to "Custom"
                        )
                        presets.forEach { (typeKey, label) ->
                            val selected = handoverType == typeKey
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) Color(0xFF0F766E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .heightIn(min = 38.dp)
                                    .clickable {
                                        if (typeKey == ShiftHandoverHelper.TYPE_CUSTOM) {
                                            handoverType = ShiftHandoverHelper.TYPE_CUSTOM
                                        } else {
                                            reloadAutoData(monthKey, day, typeKey)
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            reloadAutoData(monthKey, day, handoverType)
                            Toast.makeText(
                                context,
                                if (isEn) "Reloaded from schedule & student records." else "Data jadwal & siswa berhasil dimuat ulang.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp).testTag("btn_handover_reload_auto")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Refresh Auto-Fill from Schedule & Records" else "Muat Ulang Otomatis dari Jadwal & Data Siswa",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Laporan Pergantian") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dateLabel,
                            onValueChange = { dateLabel = it },
                            label = { Text("Hari / Tanggal") },
                            singleLine = true,
                            modifier = Modifier.weight(1.4f)
                        )
                        OutlinedTextField(
                            value = handoverTime,
                            onValueChange = { handoverTime = it },
                            label = { Text("Waktu") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fromShiftLabel,
                            onValueChange = { fromShiftLabel = it },
                            label = { Text("Shift Menyerahkan") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = toShiftLabel,
                            onValueChange = { toShiftLabel = it },
                            label = { Text("Shift Menerima") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = schoolName,
                            onValueChange = { schoolName = it },
                            label = { Text("Nama Sekolah") },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f)
                        )
                        OutlinedTextField(
                            value = serviceUnit,
                            onValueChange = { serviceUnit = it },
                            label = { Text("Unit Layanan") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 2. Kondisi Siswa & Asrama
                EditorSectionCard(title = "📊 Kondisi Siswa & Asrama") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = totalStudentsText,
                            onValueChange = { totalStudentsText = it.filter(Char::isDigit) },
                            label = { Text("Total Siswa") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = presentStudentsText,
                            onValueChange = { presentStudentsText = it.filter(Char::isDigit) },
                            label = { Text("Hadir di Asrama") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sickCountText,
                            onValueChange = { sickCountText = it.filter(Char::isDigit) },
                            label = { Text("Sakit / di UKS") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = fastingCountText,
                            onValueChange = { fastingCountText = it.filter(Char::isDigit) },
                            label = { Text("Siswa Berpuasa") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = haidCountText,
                            onValueChange = {
                                haidCountText = it.filter(Char::isDigit)
                                haidCountEditedManually = true
                            },
                            label = { Text("Siswi Sedang Haid") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_handover_haid_count")
                        )
                        OutlinedButton(
                            onClick = {
                                haidCountText = ShiftHandoverHelper.countActiveHaidForDate(
                                    haidRecords = haidRecords,
                                    monthKey = monthKey,
                                    day = day
                                ).toString()
                                haidCountEditedManually = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp)
                                .testTag("btn_handover_sync_haid")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEn) "Pull Haid Data" else "Ambil Data Haid",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 3. Data Siswa Sakit / UKS
                EditorSectionCard(title = "🏥 Data Siswa Sakit / UKS (${sickStudents.size})") {
                    sickStudents.forEachIndexed { idx, item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Siswa #${idx + 1}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                                    IconButton(
                                        onClick = {
                                            val updated = sickStudents.filterIndexed { i, _ -> i != idx }
                                            sickStudents = updated
                                            sickCountText = updated.size.toString()
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = Color(0xFFDC2626))
                                    }
                                }
                                var isNameFocused by remember(item.id) { mutableStateOf(false) }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = item.studentName,
                                        onValueChange = { newVal ->
                                            sickStudents = sickStudents.mapIndexed { i, s -> if (i == idx) s.copy(studentName = newVal) else s }
                                        },
                                        label = { Text("Nama Siswa") },
                                        placeholder = { Text(if (isEn) "Type name for auto-suggest..." else "Ketik nama untuk saran...") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1.6f)
                                            .onFocusChanged { isNameFocused = it.isFocused }
                                            .testTag("input_sick_student_name_$idx")
                                    )
                                    OutlinedTextField(
                                        value = item.classGrade,
                                        onValueChange = { newVal ->
                                            sickStudents = sickStudents.mapIndexed { i, s -> if (i == idx) s.copy(classGrade = newVal) else s }
                                        },
                                        label = { Text("Kelas") },
                                        singleLine = true,
                                        modifier = Modifier.weight(0.9f)
                                    )
                                }
                                val matchingStudents = remember(item.studentName, item.classGrade, isNameFocused, students) {
                                    val q = item.studentName.trim().lowercase()
                                    val alreadyExactMatched = q.isNotBlank() && students.any { st ->
                                        st.name.trim().equals(item.studentName.trim(), ignoreCase = true) &&
                                            (item.classGrade.isBlank() || st.classGrade.trim().equals(item.classGrade.trim(), ignoreCase = true))
                                    }
                                    if (alreadyExactMatched || (!isNameFocused && q.isBlank())) {
                                        emptyList()
                                    } else if (q.isBlank()) {
                                        students.sortedBy { it.name.lowercase() }.take(5)
                                    } else {
                                        students
                                            .filter { st ->
                                                st.name.lowercase().contains(q) || st.classGrade.lowercase().contains(q)
                                            }
                                            .sortedWith(
                                                compareByDescending<ChildStudent> { it.name.lowercase().startsWith(q) }
                                                    .thenBy { it.name.lowercase() }
                                            )
                                            .take(6)
                                    }
                                }
                                AnimatedVisibility(visible = matchingStudents.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, Color(0xFF0F766E).copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("card_sick_student_suggestions_$idx")
                                    ) {
                                        Column {
                                            Text(
                                                text = if (isEn) {
                                                    "Database Suggestions (tap to fill name & class):"
                                                } else {
                                                    "Saran Database Siswa (ketuk untuk isi nama & kelas):"
                                                },
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F766E),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                            matchingStudents.forEach { candidate ->
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(min = 48.dp)
                                                        .clickable {
                                                            sickStudents = sickStudents.mapIndexed { i, s ->
                                                                if (i == idx) {
                                                                    s.copy(
                                                                        studentName = candidate.name.trim().uppercase(),
                                                                        classGrade = candidate.classGrade.trim()
                                                                    )
                                                                } else s
                                                            }
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                                        .testTag("btn_suggest_sick_student_${idx}_${candidate.id}"),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = candidate.name.trim().uppercase(),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = listOfNotNull(
                                                                candidate.classGrade.takeIf { it.isNotBlank() },
                                                                candidate.dormitory.takeIf { it.isNotBlank() }
                                                            ).joinToString(" • "),
                                                            fontSize = 10.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Text(
                                                        text = if (isEn) "Select" else "Pilih",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F766E)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = item.statusText,
                                    onValueChange = { newVal ->
                                        sickStudents = sickStudents.mapIndexed { i, s -> if (i == idx) s.copy(statusText = newVal) else s }
                                    },
                                    label = { Text("Keterangan / Lokasi (misal: Pemulihan di asrama / Ranap di RS)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showStudentPicker = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 44.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pilih Siswa", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val updated = sickStudents + HandoverSickStudentItem()
                                sickStudents = updated
                                sickCountText = updated.size.toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 44.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Manual", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 4. Data Izin Wali Asuh
                EditorSectionCard(title = "📄 Data Izin Wali Asuh (${leaveEntries.size})") {
                    leaveEntries.forEachIndexed { idx, item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Izin #${idx + 1}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                                    IconButton(
                                        onClick = { leaveEntries = leaveEntries.filterIndexed { i, _ -> i != idx } },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = Color(0xFFDC2626))
                                    }
                                }
                                OutlinedTextField(
                                    value = item.waliName,
                                    onValueChange = { newVal ->
                                        leaveEntries = leaveEntries.mapIndexed { i, l -> if (i == idx) l.copy(waliName = newVal) else l }
                                    },
                                    label = { Text("Nama Wali Asuh") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = item.leaveDetail,
                                    onValueChange = { newVal ->
                                        leaveEntries = leaveEntries.mapIndexed { i, l -> if (i == idx) l.copy(leaveDetail = newVal) else l }
                                    },
                                    label = { Text("Keterangan (misal: Dinas Luar / Izin Sakit)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = item.driveUrl,
                                    onValueChange = { newVal ->
                                        leaveEntries = leaveEntries.mapIndexed { i, l -> if (i == idx) l.copy(driveUrl = newVal) else l }
                                    },
                                    label = { Text("🔗 Link Bukti (Google Drive / URL)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { leaveEntries = leaveEntries + HandoverLeaveItem(leaveDetail = "Izin") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Data Izin Wali Asuh", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 5. Petugas yang Menyerahkan
                StaffListEditorCard(
                    title = "👤 Petugas yang Menyerahkan (${handingOverStaff.size})",
                    subLabel = fromShiftLabel,
                    staffList = handingOverStaff,
                    onUpdateList = { handingOverStaff = it },
                    onOpenPicker = { waliPickerTarget = 1 }
                )

                // 6. Petugas yang Menerima
                StaffListEditorCard(
                    title = "👥 Petugas yang Menerima (${receivingStaff.size})",
                    subLabel = toShiftLabel,
                    staffList = receivingStaff,
                    onUpdateList = { receivingStaff = it },
                    onOpenPicker = { waliPickerTarget = 2 }
                )

                // 7. Petugas Jaga di Luar Sekolah Rakyat
                EditorSectionCard(title = "🚑 Petugas Jaga di Luar Sekolah Rakyat (${outsideDutyStaff.size})") {
                    outsideDutyStaff.forEachIndexed { idx, item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Jaga Luar #${idx + 1}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                    IconButton(
                                        onClick = { outsideDutyStaff = outsideDutyStaff.filterIndexed { i, _ -> i != idx } },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = Color(0xFFDC2626))
                                    }
                                }
                                OutlinedTextField(
                                    value = item.waliName,
                                    onValueChange = { newVal ->
                                        outsideDutyStaff = outsideDutyStaff.mapIndexed { i, o -> if (i == idx) o.copy(waliName = newVal) else o }
                                    },
                                    label = { Text("Nama Petugas") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = item.locationLabel,
                                        onValueChange = { newVal ->
                                            outsideDutyStaff = outsideDutyStaff.mapIndexed { i, o -> if (i == idx) o.copy(locationLabel = newVal) else o }
                                        },
                                        label = { Text("Lokasi (misal: Jaga RSUD Gambiran)") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1.3f)
                                    )
                                    OutlinedTextField(
                                        value = item.shiftInfo,
                                        onValueChange = { newVal ->
                                            outsideDutyStaff = outsideDutyStaff.mapIndexed { i, o -> if (i == idx) o.copy(shiftInfo = newVal) else o }
                                        },
                                        label = { Text("Shift (misal: Shif Pagi - P1)") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { outsideDutyStaff = outsideDutyStaff + HandoverOutsideDutyItem() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Petugas Jaga Luar", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 8. Catatan Tindak Lanjut
                EditorSectionCard(title = "📝 Catatan Tindak Lanjut") {
                    OutlinedTextField(
                        value = followUpNotes,
                        onValueChange = { newVal ->
                            followUpNotes = newVal
                            if (newVal.isNotBlank()) {
                                autoSyncShiftMessage = true
                            }
                        },
                        label = { Text("Catatan Tindak Lanjut Shift Berikutnya") },
                        placeholder = {
                            Text(
                                text = if (isEn) {
                                    "Type or tap mic to dictate follow-up notes..."
                                } else {
                                    "Ketik manual atau ketuk mikrofon untuk dikte suara..."
                                },
                                fontSize = 12.sp
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { startFollowUpVoiceInput() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_handover_voice_followup_inline")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = if (isEn) "Voice to text" else "Dikte suara ke teks",
                                    tint = Color(0xFF0F766E)
                                )
                            }
                        },
                        minLines = 3,
                        maxLines = 8,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_handover_followup_notes")
                    )

                    val matchedShiftMsgs = remember(
                        shiftMessages,
                        monthKey,
                        day,
                        handoverType,
                        toShiftLabel,
                        handoverTime,
                        syncedShiftMessageId
                    ) {
                        ShiftHandoverHelper.filterShiftMessagesForHandover(
                            shiftMessages = shiftMessages,
                            monthKey = monthKey,
                            day = day,
                            handoverType = handoverType,
                            toShiftLabel = toShiftLabel,
                            handoverTime = handoverTime,
                            excludeMessageId = syncedShiftMessageId
                        )
                    }
                    val targetShiftLabel = remember(handoverType, toShiftLabel) {
                        ShiftHandoverHelper.resolveTargetShiftMessageLabel(handoverType, toShiftLabel)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { startFollowUpVoiceInput() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_handover_voice_followup")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isEn) "Voice to Text" else "Dikte Suara",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        if (matchedShiftMsgs.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    val imported = ShiftHandoverHelper.formatShiftMessagesAsFollowUpNotes(
                                        messages = matchedShiftMsgs,
                                        limit = 6,
                                        bulletPrefix = "• "
                                    )
                                    val wasEmpty = followUpNotes.isBlank()
                                    val existingNorm = followUpNotes.trim()
                                    val existingLines = existingNorm.lines().map { it.removePrefix("• ").trim() }.toSet()
                                    val newLines = imported.lines().filter { line ->
                                        line.removePrefix("• ").trim() !in existingLines
                                    }
                                    if (newLines.isNotEmpty()) {
                                        val appended = newLines.joinToString("\n")
                                        followUpNotes = if (wasEmpty) imported else "$existingNorm\n$appended"
                                    }
                                    if (wasEmpty && syncedShiftMessageId == 0L) {
                                        autoSyncShiftMessage = false
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_handover_import_shift_msgs")
                            ) {
                                Text(
                                    text = if (isEn) {
                                        "Pull Shift Notes (${matchedShiftMsgs.size})"
                                    } else {
                                        "Ambil Pesan (${matchedShiftMsgs.size} Sesuai)"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (matchedShiftMsgs.isEmpty()) {
                        Text(
                            text = if (isEn) {
                                "No active shift messages match $dateLabel • $targetShiftLabel (left blank unless typed manually)."
                            } else {
                                "Belum ada pesan shift aktif untuk $dateLabel • $targetShiftLabel (dibiarkan kosong jika belum diisi)."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("text_handover_no_matching_shift_msgs")
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (autoSyncShiftMessage) Color(0xFFECFDF5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(
                            1.dp,
                            if (autoSyncShiftMessage) Color(0xFF10B981).copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { autoSyncShiftMessage = !autoSyncShiftMessage }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) {
                                        "Auto-save to Next Shift Messages"
                                    } else {
                                        "Otomatis simpan ke Pesan Shift Berikutnya"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (autoSyncShiftMessage) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = buildString {
                                        append(if (isEn) "Target: " else "Target: ")
                                        append(targetShiftLabel)
                                        append(" • Sync PocketBase")
                                        if (syncedShiftMessageId > 0L) {
                                            append(if (isEn) " • Linked" else " • Sudah tertaut")
                                        }
                                    },
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoSyncShiftMessage,
                                onCheckedChange = { autoSyncShiftMessage = it },
                                modifier = Modifier.testTag("switch_handover_auto_shift_message")
                            )
                        }
                    }
                }

                // 9. Kegiatan yang Telah Dilaksanakan
                EditorSectionCard(title = "📌 Kegiatan yang Telah Dilaksanakan (${completedActivities.size})") {
                    completedActivities.forEachIndexed { idx, act ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("${idx + 1}.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                            OutlinedTextField(
                                value = act,
                                onValueChange = { newVal ->
                                    completedActivities = completedActivities.mapIndexed { i, a -> if (i == idx) newVal else a }
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { completedActivities = completedActivities.filterIndexed { i, _ -> i != idx } },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = Color(0xFFDC2626))
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newActivityText,
                            onValueChange = { newActivityText = it },
                            placeholder = { Text("Tulis kegiatan baru...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val clean = newActivityText.trim()
                                if (clean.isNotBlank()) {
                                    completedActivities = completedActivities + clean
                                    newActivityText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Kegiatan")
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ShiftHandoverHelper.DEFAULT_ACTIVITY_PRESETS.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF0FDFA),
                                border = BorderStroke(1.dp, Color(0xFF99F6E4)),
                                modifier = Modifier.clickable {
                                    if (completedActivities.none { it.equals(preset, ignoreCase = true) }) {
                                        completedActivities = completedActivities + preset
                                    }
                                }
                            ) {
                                Text(
                                    text = "+ $preset",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F766E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // 10. Footer & Pratinjau WhatsApp
                EditorSectionCard(title = "✨ Footer & Pratinjau Pesan WhatsApp") {
                    OutlinedTextField(
                        value = footerText,
                        onValueChange = { footerText = it },
                        label = { Text("Footer Laporan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { copyHandoverText(context, currentWaText, isEn) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 46.dp).testTag("btn_handover_copy_wa")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salin Teks WA", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { shareHandoverToWhatsApp(context, currentWaText, isEn) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 46.dp).testTag("btn_handover_share_wa")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share WhatsApp", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth().clickable { showPreview = !showPreview }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showPreview) "Sembunyikan Pratinjau WhatsApp" else "Lihat Pratinjau Teks WhatsApp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E)
                            )
                            Icon(
                                imageVector = if (showPreview) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color(0xFF0F766E)
                            )
                        }
                    }
                    AnimatedVisibility(visible = showPreview) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentWaText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF064E3B),
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showStudentPicker) {
        StudentPickerForHandoverDialog(
            students = students,
            onSelect = { student ->
                val updated = sickStudents + HandoverSickStudentItem(
                    studentName = student.name.trim().uppercase(),
                    classGrade = student.classGrade.trim(),
                    statusText = "Pemulihan di asrama"
                )
                sickStudents = updated
                sickCountText = updated.size.toString()
                showStudentPicker = false
            },
            onDismiss = { showStudentPicker = false }
        )
    }

    if (waliPickerTarget != 0) {
        val allNames = remember(waliAccounts, allScheduleEntries) {
            (waliAccounts.filter { !it.isSpecialPortalRole() }.map { it.name.trim() } +
                allScheduleEntries.map { it.waliName.trim() })
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .sorted()
        }
        val currentSelection = if (waliPickerTarget == 1) handingOverStaff else receivingStaff
        WaliMultiPickerDialog(
            title = if (waliPickerTarget == 1) "Pilih Petugas Menyerahkan" else "Pilih Petugas Menerima",
            availableNames = allNames,
            selectedNames = currentSelection,
            onConfirm = { picked ->
                if (waliPickerTarget == 1) handingOverStaff = picked else receivingStaff = picked
                waliPickerTarget = 0
            },
            onDismiss = { waliPickerTarget = 0 }
        )
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Tutup Editor?") },
            text = { Text("Simpan laporan ini sebagai Draft di PocketBase sebelum keluar agar dapat dilanjutkan kembali nanti?") },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        triggerSave(ShiftHandoverHelper.STATUS_DRAFT, closeAfter = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
                ) {
                    Text("Simpan Draft")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            showUnsavedDialog = false
                            onBack()
                        }
                    ) {
                        Text("Keluar Tanpa Simpan", color = Color(0xFFDC2626))
                    }
                    TextButton(onClick = { showUnsavedDialog = false }) {
                        Text("Batal")
                    }
                }
            }
        )
    }
}

@Composable
private fun EditorSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            content()
        }
    }
}

@Composable
private fun StaffListEditorCard(
    title: String,
    subLabel: String,
    staffList: List<String>,
    onUpdateList: (List<String>) -> Unit,
    onOpenPicker: () -> Unit
) {
    var manualName by remember { mutableStateOf("") }
    EditorSectionCard(title = title) {
        if (subLabel.isNotBlank()) {
            Text(subLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F766E))
        }
        staffList.forEachIndexed { idx, name ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("${idx + 1}.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                OutlinedTextField(
                    value = name,
                    onValueChange = { newVal ->
                        onUpdateList(staffList.mapIndexed { i, n -> if (i == idx) newVal else n })
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onUpdateList(staffList.filterIndexed { i, _ -> i != idx }) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = Color(0xFFDC2626))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = manualName,
                onValueChange = { manualName = it },
                placeholder = { Text("Tambah nama petugas...") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = {
                    val clean = manualName.trim()
                    if (clean.isNotBlank()) {
                        onUpdateList(staffList + clean)
                        manualName = ""
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
            Button(
                onClick = onOpenPicker,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Pilih Wali")
            }
        }
    }
}

@Composable
private fun StudentPickerForHandoverDialog(
    students: List<ChildStudent>,
    onSelect: (ChildStudent) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(students, query) {
        val q = query.trim().lowercase()
        students.filter {
            q.isBlank() || it.name.lowercase().contains(q) || it.classGrade.lowercase().contains(q)
        }.take(60)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Siswa Sakit / UKS", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Cari nama / kelas siswa...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered, key = { it.id }) { st ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(st) }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Text(st.name, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                Text("${st.classGrade} • ${st.dormitory}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Composable
private fun WaliMultiPickerDialog(
    title: String,
    availableNames: List<String>,
    selectedNames: List<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var current by remember(selectedNames) { mutableStateOf(selectedNames) }
    val filtered = remember(availableNames, query) {
        val q = query.trim().lowercase()
        availableNames.filter { q.isBlank() || it.lowercase().contains(q) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Cari nama Wali Asuh...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filtered, key = { it }) { name ->
                        val checked = current.any { it.equals(name, ignoreCase = true) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    current = if (checked) {
                                        current.filterNot { it.equals(name, ignoreCase = true) }
                                    } else {
                                        current + name
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    current = if (isChecked) current + name
                                    else current.filterNot { it.equals(name, ignoreCase = true) }
                                }
                            )
                            Text(name, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(current) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
            ) {
                Text("Terapkan (${current.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
