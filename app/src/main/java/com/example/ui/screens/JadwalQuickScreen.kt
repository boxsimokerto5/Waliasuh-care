package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.SchoolProfile
import com.example.data.model.WaliAccount
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliScheduleHelper
import com.example.data.model.WaliShiftCode
import com.example.ui.HealthViewModel
import com.example.ui.components.DashboardModalState
import com.example.ui.components.jadwalDailyRosterItems
import com.example.data.model.ShiftHandoverHelper
import com.example.ui.dialogs.EditJadwalSelDialog
import com.example.ui.dialogs.ImportJadwalMatriksDialog
import com.example.ui.dialogs.JadwalPersonalSheet
import com.example.ui.dialogs.SerahTerimaShiftSheet
import com.example.util.TopOnAdManager
import com.example.util.i18n.LocalAppStrings

@Composable
fun JadwalQuickScreen(
    viewModel: HealthViewModel,
    schoolProfile: SchoolProfile,
    shiftCodes: List<WaliShiftCode>,
    scheduleEntries: List<WaliScheduleEntry>,
    canManageSchedule: Boolean,
    canPinScheduleTasks: Boolean = canManageSchedule,
    waliAccounts: List<WaliAccount>,
    currentUsername: String?,
    currentWaliName: String,
    modalState: DashboardModalState
) {
    val canEditAnyCell = canManageSchedule || canPinScheduleTasks
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedGroup by rememberSaveable { mutableStateOf("ALL") }

    val shiftHandovers by viewModel.shiftHandovers.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val fastingRecords by viewModel.fastingRecords.collectAsStateWithLifecycle()
    val haidRecords by viewModel.allHaidRecords.collectAsStateWithLifecycle()
    val shiftMessages by viewModel.shiftMessages.collectAsStateWithLifecycle()
    val shiftHandoverDraftCount = remember(shiftHandovers) {
        shiftHandovers.count { it.status.equals(ShiftHandoverHelper.STATUS_DRAFT, ignoreCase = true) }
    }

    LaunchedEffect(Unit) {
        viewModel.syncShiftHandoversOnlineQuietly()
    }
    LaunchedEffect(modalState.showSerahTerimaShiftSheet) {
        if (modalState.showSerahTerimaShiftSheet) {
            viewModel.syncShiftHandoversOnlineQuietly()
        }
    }

    val monthEntries = remember(scheduleEntries, modalState.jadwalSelectedMonthKey) {
        scheduleEntries.filter { it.monthKey == modalState.jadwalSelectedMonthKey }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .testTag("jadwal_quick_screen_list"),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            jadwalDailyRosterItems(
                isEn = isEn,
                schoolProfile = schoolProfile,
                monthKey = modalState.jadwalSelectedMonthKey,
                selectedDay = modalState.jadwalSelectedDay,
                shiftCodes = shiftCodes,
                monthEntries = monthEntries,
                canManageSchedule = canManageSchedule,
                canPinScheduleTasks = canPinScheduleTasks,
                searchQuery = searchQuery,
                selectedGroup = selectedGroup,
                onSearchChange = { searchQuery = it },
                onGroupChange = { selectedGroup = it },
                onChangeMonth = { newMonth ->
                    modalState.jadwalSelectedMonthKey = newMonth
                    val maxD = WaliScheduleHelper.daysInMonth(newMonth)
                    if (modalState.jadwalSelectedDay > maxD) {
                        modalState.jadwalSelectedDay = maxD
                    }
                },
                onChangeDay = { newDay ->
                    modalState.jadwalSelectedDay = newDay
                },
                onOpenPersonal = { entry ->
                    TopOnAdManager.onUserActionClick(context)
                    modalState.jadwalPersonalTargetEntry = entry
                    modalState.showJadwalPersonalSheet = true
                },
                onEditCell = { entry, day ->
                    TopOnAdManager.onUserActionClick(context)
                    if (canEditAnyCell) {
                        modalState.editingScheduleCellTarget = entry to day
                    } else {
                        modalState.jadwalPersonalTargetEntry = entry
                        modalState.showJadwalPersonalSheet = true
                    }
                },
                onOpenImportMatriks = if (canManageSchedule) {
                    {
                        TopOnAdManager.onUserActionClick(context)
                        modalState.importJadwalInitialTab = 0
                        modalState.showImportJadwalMatriksDialog = true
                    }
                } else null,
                shiftHandoverDraftCount = shiftHandoverDraftCount,
                onOpenSerahTerimaShift = {
                    TopOnAdManager.onUserActionClick(context)
                    modalState.showSerahTerimaShiftSheet = true
                }
            )
        }
    }

    if (modalState.showJadwalPersonalSheet) {
        val rawTarget = modalState.jadwalPersonalTargetEntry
        val liveEntry = remember(rawTarget, monthEntries, currentUsername, currentWaliName) {
            when {
                rawTarget != null -> monthEntries.firstOrNull { it.id == rawTarget.id } ?: rawTarget
                else -> monthEntries.firstOrNull {
                    WaliScheduleHelper.matchesWaliEntry(it, currentUsername, currentWaliName)
                }
            }
        }
        if (liveEntry != null) {
            JadwalPersonalSheet(
                entry = liveEntry,
                monthKey = modalState.jadwalSelectedMonthKey,
                shiftCodes = shiftCodes,
                schoolProfile = schoolProfile,
                canManageSchedule = canManageSchedule,
                canPinTasks = canPinScheduleTasks,
                onEditDay = { day ->
                    if (canEditAnyCell) {
                        modalState.editingScheduleCellTarget = liveEntry to day
                    }
                },
                onDismiss = {
                    modalState.showJadwalPersonalSheet = false
                    modalState.jadwalPersonalTargetEntry = null
                }
            )
        } else {
            modalState.showJadwalPersonalSheet = false
        }
    }

    if (canEditAnyCell) {
        modalState.editingScheduleCellTarget?.let { (rawEntry, targetDay) ->
            val liveEntry = monthEntries.firstOrNull { it.id == rawEntry.id } ?: rawEntry
            EditJadwalSelDialog(
                entry = liveEntry,
                day = targetDay,
                monthKey = modalState.jadwalSelectedMonthKey,
                shiftCodes = shiftCodes,
                schoolProfile = schoolProfile,
                pinOnlyMode = !canManageSchedule,
                onSave = { code, pins, leave ->
                    viewModel.updateWaliScheduleDayCell(
                        entry = liveEntry,
                        day = targetDay,
                        shiftCode = code,
                        pinsForDay = pins,
                        leaveForDay = leave
                    ) { ok, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (ok) modalState.editingScheduleCellTarget = null
                    }
                },
                onOpenPatternEditor = if (canManageSchedule) {
                    {
                        modalState.editingScheduleCellTarget = null
                        modalState.importJadwalInitialTab = 1
                        modalState.showImportJadwalMatriksDialog = true
                    }
                } else null,
                onDismiss = { modalState.editingScheduleCellTarget = null }
            )
        }
    }

    if (canManageSchedule) {

        if (modalState.showImportJadwalMatriksDialog) {
            ImportJadwalMatriksDialog(
                monthKey = modalState.jadwalSelectedMonthKey,
                entries = monthEntries,
                shiftCodes = shiftCodes,
                schoolProfile = schoolProfile,
                waliAccounts = waliAccounts,
                initialTab = modalState.importJadwalInitialTab,
                onSyncWaliAccounts = {
                    viewModel.syncScheduleMembersFromWaliAccounts(modalState.jadwalSelectedMonthKey) { _, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                onAddManualMember = { name, code ->
                    viewModel.addWaliScheduleMember(
                        monthKey = modalState.jadwalSelectedMonthKey,
                        waliName = name,
                        waliCode = code
                    ) { _, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                onDeleteMember = { entry ->
                    viewModel.deleteWaliScheduleMember(entry) { _, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                onApplyPattern = { entry, startDay, pattern, repeat ->
                    viewModel.applyShiftPatternToRow(
                        entry = entry,
                        startDay = startDay,
                        patternCodes = pattern,
                        repeatUntilEndOfMonth = repeat
                    ) { _, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                onImportCsv = { csv ->
                    viewModel.importScheduleMatrixCsv(modalState.jadwalSelectedMonthKey, csv) { ok, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (ok) modalState.showImportJadwalMatriksDialog = false
                    }
                },
                onDismiss = { modalState.showImportJadwalMatriksDialog = false }
            )
        }
    }

    if (modalState.showSerahTerimaShiftSheet) {
        SerahTerimaShiftSheet(
            isEn = isEn,
            reports = shiftHandovers,
            schoolProfile = schoolProfile,
            initialMonthKey = modalState.jadwalSelectedMonthKey,
            initialDay = modalState.jadwalSelectedDay,
            allScheduleEntries = scheduleEntries,
            shiftCodes = shiftCodes,
            students = students,
            medicalRecords = allRecords,
            fastingRecords = fastingRecords,
            haidRecords = haidRecords,
            shiftMessages = shiftMessages,
            waliAccounts = waliAccounts,
            currentUsername = currentUsername,
            currentWaliName = currentWaliName,
            onSyncOnline = { onDone ->
                viewModel.syncShiftHandoversOnlineQuietly(onDone)
            },
            onSaveReport = { existing, mKey, dayVal, hType, st, titleStr, sName, sUnit, dLabel, hTime, fromLbl, toLbl, tot, pres, sickC, fastC, payloadObj, cb ->
                viewModel.saveShiftHandover(
                    existing = existing,
                    monthKey = mKey,
                    day = dayVal,
                    handoverType = hType,
                    status = st,
                    title = titleStr,
                    schoolName = sName,
                    serviceUnit = sUnit,
                    dateLabel = dLabel,
                    handoverTime = hTime,
                    fromShiftLabel = fromLbl,
                    toShiftLabel = toLbl,
                    totalStudents = tot,
                    presentStudents = pres,
                    sickStudentsCount = sickC,
                    fastingStudentsCount = fastC,
                    payload = payloadObj,
                    onResult = cb
                )
            },
            onDeleteReport = { report, cb ->
                viewModel.deleteShiftHandover(report, cb)
            },
            onDismiss = { modalState.showSerahTerimaShiftSheet = false }
        )
    }
}
