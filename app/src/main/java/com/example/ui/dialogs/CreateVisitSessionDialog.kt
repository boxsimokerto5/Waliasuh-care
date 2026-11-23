@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.VisitHelper
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionDraft
import com.example.util.i18n.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val VisitIndigo = Color(0xFF1D4ED8)
private val VisitIndigoLight = Color(0xFFDBEAFE)
private val VisitTeal = Color(0xFF0F766E)
private val VisitTealLight = Color(0xFFCCFBF1)

private data class SessionDraftUiState(
    val title: String,
    val hasCustomTitle: Boolean,
    val dateMillis: Long,
    val visitTimeRange: String,
    val selectedDistricts: Set<String>,
    val description: String
)

@Composable
fun CreateVisitSessionDialog(
    students: List<ChildStudent>,
    initialSession: VisitSession? = null,
    existingSessions: List<VisitSession> = emptyList(),
    onDismiss: () -> Unit,
    onConfirmBatch: ((List<VisitSessionDraft>) -> Unit)? = null,
    onConfirm: (
        title: String,
        monthYear: String,
        visitDates: List<Long>,
        selectedDistricts: List<String>,
        description: String,
        visitTimeRange: String
    ) -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val locale = s.language.locale

    val isEditMode = initialSession != null
    val defaultMonthYear = remember(initialSession) {
        initialSession?.monthYear?.ifBlank { VisitHelper.buildDefaultMonthYear() }
            ?: VisitHelper.buildDefaultMonthYear()
    }

    val monthChoices = remember(defaultMonthYear) {
        val cal = Calendar.getInstance()
        val list = mutableListOf<String>()
        cal.add(Calendar.MONTH, -1)
        repeat(5) {
            list.add(SimpleDateFormat("yyyy-MM", Locale.US).format(cal.time))
            cal.add(Calendar.MONTH, 1)
        }
        if (defaultMonthYear.isNotBlank() && defaultMonthYear !in list) {
            listOf(defaultMonthYear) + list
        } else {
            list
        }
    }

    var selectedMonthYear by remember { mutableStateOf(defaultMonthYear) }
    var sessionCount by remember { mutableIntStateOf(1) }
    var activeSessionIdx by remember { mutableIntStateOf(0) }

    val initialSundays = remember(defaultMonthYear) {
        computeDistinctMonthVisitDates(defaultMonthYear, 4)
    }

    val drafts = remember(initialSession) {
        mutableStateListOf<SessionDraftUiState>().apply {
            if (initialSession != null) {
                add(
                    SessionDraftUiState(
                        title = initialSession.title,
                        hasCustomTitle = true,
                        dateMillis = initialSession.getVisitDatesList().firstOrNull()
                            ?: initialSundays.firstOrNull()
                            ?: VisitHelper.normalizeDayStart(System.currentTimeMillis()),
                        visitTimeRange = initialSession.displayTimeRange(),
                        selectedDistricts = initialSession.getDistrictsList().toSet(),
                        description = initialSession.description
                    )
                )
            } else {
                val baseTitle = VisitHelper.defaultSessionTitleForMonthYear(defaultMonthYear, locale, isEn)
                for (i in 0 until 4) {
                    add(
                        SessionDraftUiState(
                            title = "$baseTitle - Sesi ${i + 1}",
                            hasCustomTitle = false,
                            dateMillis = initialSundays.getOrElse(i) {
                                initialSundays.firstOrNull() ?: VisitHelper.normalizeDayStart(System.currentTimeMillis())
                            },
                            visitTimeRange = VisitHelper.DEFAULT_VISIT_TIME_RANGE,
                            selectedDistricts = emptySet(),
                            description = ""
                        )
                    )
                }
            }
        }
    }

    var datePickerSessionIdx by remember { mutableStateOf<Int?>(null) }
    var validationBanner by remember { mutableStateOf<String?>(null) }

    val dynamicDistricts = remember(students, isEn) {
        VisitHelper.extractDynamicDistricts(students, isEn)
    }
    val dateFormatter = remember(locale) { SimpleDateFormat("EEE, d MMM yyyy", locale) }
    val shortDateFmt = remember(locale) { SimpleDateFormat("d MMM", locale) }

    val activeDraftCount = if (isEditMode) 1 else sessionCount.coerceIn(1, 4)
    val safeActiveIdx = activeSessionIdx.coerceIn(0, activeDraftCount - 1)
    val currentDraft = drafts[safeActiveIdx]

    // Districts locked by other sessions in this batch (for indices 0 until activeDraftCount)
    val lockedByBatch = remember(
        drafts.toList(),
        safeActiveIdx,
        activeDraftCount
    ) {
        VisitHelper.usedDistrictsByOtherDrafts(
            draftDistricts = drafts.take(activeDraftCount).map { it.selectedDistricts },
            excludeIndex = safeActiveIdx
        )
    }

    // Districts locked by existing active sessions in the same month (excluding self when editing)
    val lockedByExisting = remember(existingSessions, selectedMonthYear, initialSession?.id) {
        VisitHelper.usedDistrictsInExistingSessions(
            sessions = existingSessions,
            monthYear = selectedMonthYear,
            excludeSessionId = initialSession?.id ?: -1L
        )
    }

    val activeDateDuplicates = remember(drafts.toList(), activeDraftCount) {
        val dates = drafts.take(activeDraftCount).map { VisitHelper.normalizeDayStart(it.dateMillis) }
        dates.distinct().size != dates.size
    }

    val allDraftsValid = remember(drafts.toList(), activeDraftCount, dynamicDistricts.size) {
        val slice = drafts.take(activeDraftCount)
        val allTitlesOk = slice.all { it.title.isNotBlank() && it.visitTimeRange.isNotBlank() }
        val datesOk = slice.map { VisitHelper.normalizeDayStart(it.dateMillis) }.let { list ->
            list.all { it > 0L } && list.distinct().size == list.size
        }
        val districtsOk = if (dynamicDistricts.isEmpty()) true else slice.all { it.selectedDistricts.isNotEmpty() }
        allTitlesOk && datesOk && districtsOk
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Header Bottom Sheet (Rapi & 1 Baris)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = VisitIndigoLight,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = VisitIndigo,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEditMode) {
                                if (isEn) "Edit Visit Session" else "Edit Sesi Kunjungan"
                            } else {
                                if (isEn) "Create Visit Schedule" else "Buat Jadwal Kunjungan"
                            },
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isEditMode) {
                                if (isEn) "Update session date, hours & districts" else "Perbarui tanggal, jam kunjung & kecamatan"
                            } else {
                                if (isEn) "Configure 1–4 sessions with distinct dates & districts" else "Atur 1–4 sesi dengan tanggal & kecamatan berbeda"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_create_visit_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close" else "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))


                // Bagian Pinned: Pilihan Bulan, Jumlah Sesi (1-4), & Switcher Sesi
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Bulan Sesi
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isEn) "1. Visit Month" else "1. Bulan Jadwal Kunjungan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VisitIndigo
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            monthChoices.forEach { ym ->
                                val selected = selectedMonthYear == ym
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedMonthYear = ym
                                        validationBanner = null
                                        val monthDates = computeDistinctMonthVisitDates(ym, 4)
                                        val baseTitle = VisitHelper.defaultSessionTitleForMonthYear(ym, locale, isEn)
                                        for (i in 0 until drafts.size) {
                                            val prev = drafts[i]
                                            val updatedDate = if (!isEditMode) {
                                                monthDates.getOrElse(i) { prev.dateMillis }
                                            } else {
                                                prev.dateMillis
                                            }
                                            val updatedTitle = if (!prev.hasCustomTitle) {
                                                if (sessionCount == 1 && !isEditMode) baseTitle else "$baseTitle - Sesi ${i + 1}"
                                            } else {
                                                prev.title
                                            }
                                            drafts[i] = prev.copy(
                                                title = updatedTitle,
                                                dateMillis = updatedDate
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = VisitHelper.formatMonthYearLabel(ym, locale),
                                            fontSize = 12.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = VisitIndigoLight,
                                        selectedLabelColor = VisitIndigo
                                    ),
                                    modifier = Modifier.heightIn(min = 38.dp)
                                )
                            }
                        }
                    }

                    // 2. Pilihan Jumlah Sesi (1 - 4 Sesi) — pill rapi 1 baris
                    if (!isEditMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (isEn) "2. Number of Sessions (1–4)" else "2. Jumlah Sesi Kunjungan (1–4 Sesi)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = VisitIndigo
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                (1..4).forEach { count ->
                                    val isSel = sessionCount == count
                                    Surface(
                                        onClick = {
                                            sessionCount = count
                                            activeSessionIdx = activeSessionIdx.coerceIn(0, count - 1)
                                            validationBanner = null
                                            val baseTitle = VisitHelper.defaultSessionTitleForMonthYear(selectedMonthYear, locale, isEn)
                                            for (i in 0 until count) {
                                                val cur = drafts[i]
                                                if (!cur.hasCustomTitle) {
                                                    drafts[i] = cur.copy(
                                                        title = if (count == 1) baseTitle else "$baseTitle - Sesi ${i + 1}"
                                                    )
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSel) VisitIndigo else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSel) VisitIndigo else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("chip_visit_session_count_$count")
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = if (isEn) "$count Sess" else "$count Sesi",
                                                fontSize = 12.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Clip
                                            )
                                        }
                                    }
                                }
                            }

                            // Switcher antar Sesi 1..N
                            if (sessionCount > 1) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (idx in 0 until activeDraftCount) {
                                        val draftItem = drafts[idx]
                                        val isCurrent = idx == safeActiveIdx
                                        val isComplete = draftItem.title.isNotBlank() &&
                                            (dynamicDistricts.isEmpty() || draftItem.selectedDistricts.isNotEmpty())
                                        Surface(
                                            onClick = { activeSessionIdx = idx },
                                            shape = RoundedCornerShape(12.dp),
                                            color = when {
                                                isCurrent -> VisitIndigo
                                                isComplete -> Color(0xFFECFDF5)
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                            },
                                            border = BorderStroke(
                                                1.dp,
                                                when {
                                                    isCurrent -> VisitIndigo
                                                    isComplete -> Color(0xFF10B981)
                                                    else -> MaterialTheme.colorScheme.outlineVariant
                                                }
                                            ),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("tab_visit_draft_$idx")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (isComplete && !isCurrent) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = Color(0xFF059669),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = if (isEn) "Session ${idx + 1}" else "Sesi ${idx + 1}",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "${shortDateFmt.format(Date(draftItem.dateMillis))} • ${draftItem.selectedDistricts.size} Kec.",
                                                        fontSize = 10.sp,
                                                        color = if (isCurrent) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
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

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Konten Detail Sesi yang Bisa Di-scroll Lega
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Info Tahap Rilis Jadwal (Draf vs Preview Wali vs Live Ortu)
                    val dialogBannerBg = when {
                        initialSession?.publishedToParent == true -> Color(0xFFFEF3C7)
                        initialSession?.publishedToWali == true -> Color(0xFFDBEAFE)
                        else -> Color(0xFFF0FDF4)
                    }
                    val dialogBannerBorder = when {
                        initialSession?.publishedToParent == true -> Color(0xFFF59E0B)
                        initialSession?.publishedToWali == true -> Color(0xFF93C5FD)
                        else -> Color(0xFF86EFAC)
                    }
                    val dialogBannerTextColor = when {
                        initialSession?.publishedToParent == true -> Color(0xFF92400E)
                        initialSession?.publishedToWali == true -> Color(0xFF1E40AF)
                        else -> Color(0xFF166534)
                    }
                    val dialogBannerMessage = when {
                        initialSession?.publishedToParent == true -> if (isEn) {
                            "This session is LIVE to Parents. Any date, hour, or district edit updates the Parent Portal immediately."
                        } else {
                            "Jadwal ini sudah LIVE ke Wali Siswa / Orang Tua. Perubahan tanggal, jam, atau kecamatan akan memperbarui tampilan Orang Tua."
                        }
                        initialSession?.publishedToWali == true -> if (isEn) {
                            "This session is in Wali Asuh Preview (not yet live to Parents)."
                        } else {
                            "Jadwal ini dalam tahap Preview Wali Asuh (belum diluncurkan ke Orang Tua)."
                        }
                        isEditMode -> if (isEn) {
                            "Saved as Draft — parents cannot see this schedule until you launch it in Admin → Visits."
                        } else {
                            "Status masih Draf — belum tampil ke Wali Asuh/Orang Tua sampai Anda menekan tombol Luncurkan."
                        }
                        else -> if (isEn) {
                            "New sessions save as Draft first so you can finalize before launching to Wali Asuh & Parents."
                        } else {
                            "Jadwal baru otomatis tersimpan sebagai Draf dulu agar aman direvisi sebelum diluncurkan ke Wali Asuh & Orang Tua."
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = dialogBannerBg,
                        border = BorderStroke(1.dp, dialogBannerBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("banner_create_visit_launch_stage")
                    ) {
                        Text(
                            text = dialogBannerMessage,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = dialogBannerTextColor,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }

                    // 3. Detail Sesi Aktif (Nama, 1 Tanggal Pasti & Berbeda, Jam Kunjungan, Kecamatan Eksklusif)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                        border = BorderStroke(1.dp, VisitIndigo.copy(alpha = 0.22f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!isEditMode && activeDraftCount > 1) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = VisitIndigoLight.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = if (isEn) {
                                            "Configuring Session ${safeActiveIdx + 1} of $activeDraftCount"
                                        } else {
                                            "Pengaturan Sesi ${safeActiveIdx + 1} dari $activeDraftCount"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VisitIndigo,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = currentDraft.title,
                                onValueChange = { newTitle ->
                                    drafts[safeActiveIdx] = currentDraft.copy(
                                        title = newTitle,
                                        hasCustomTitle = true
                                    )
                                },
                                label = { Text(if (isEn) "Session Title *" else "Nama Sesi Kunjungan *") },
                                placeholder = {
                                    Text(if (isEn) "e.g., Visit Nov 2026 - Session ${safeActiveIdx + 1}" else "Contoh: Kunjungan November - Sesi ${safeActiveIdx + 1}")
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_visit_session_title")
                            )

                            // Tanggal Sesi Tunggal (wajib berbeda antar sesi)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (isEn) {
                                        "Visit Date (Unique per Session):"
                                    } else {
                                        "Tanggal Kunjungan Sesi Ini (Berbeda tiap sesi):"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VisitIndigo
                                )
                                OutlinedButton(
                                    onClick = { datePickerSessionIdx = safeActiveIdx },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                    border = BorderStroke(1.dp, VisitIndigo.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_pick_visit_date_$safeActiveIdx")
                                ) {
                                    Icon(
                                        Icons.Default.EventAvailable,
                                        contentDescription = null,
                                        tint = VisitIndigo,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dateFormatter.format(Date(currentDraft.dateMillis)),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VisitIndigo,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = if (isEn) "Change" else "Ubah Tanggal",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = VisitTeal
                                    )
                                }
                            }

                            // Rentang Jam Kunjungan Sesi Ini (tampil di undangan Portal Orang Tua)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = VisitIndigo, modifier = Modifier.size(15.dp))
                                    Text(
                                        text = if (isEn) {
                                            "Visiting Hours (Parent Invitation):"
                                        } else {
                                            "Rentang Jam Kunjungan (Undangan Orang Tua):"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VisitIndigo
                                    )
                                }
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    VisitHelper.VISIT_TIME_PRESETS.forEachIndexed { presetIdx, preset ->
                                        val isPresetSel = currentDraft.visitTimeRange.equals(preset, ignoreCase = true)
                                        FilterChip(
                                            selected = isPresetSel,
                                            onClick = {
                                                drafts[safeActiveIdx] = currentDraft.copy(visitTimeRange = preset)
                                            },
                                            label = {
                                                Text(
                                                    text = preset,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = if (isPresetSel) FontWeight.Bold else FontWeight.Medium,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = VisitIndigoLight,
                                                selectedLabelColor = VisitIndigo
                                            ),
                                            modifier = Modifier
                                                .heightIn(min = 38.dp)
                                                .testTag("chip_visit_time_preset_$presetIdx")
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = currentDraft.visitTimeRange,
                                    onValueChange = { newTimeRange ->
                                        drafts[safeActiveIdx] = currentDraft.copy(visitTimeRange = newTimeRange)
                                    },
                                    label = { Text(if (isEn) "Custom Time Window *" else "Jam Kunjungan Sesi *") },
                                    placeholder = { Text(VisitHelper.DEFAULT_VISIT_TIME_RANGE) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_visit_time_range")
                                )
                            }

                            // Kecamatan (Eksklusif per sesi: yang sudah dipakai sesi lain terkunci)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isEn) {
                                            "Districts (${currentDraft.selectedDistricts.size} selected):"
                                        } else {
                                            "Pilih Kecamatan Sesi Ini (${currentDraft.selectedDistricts.size}):"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VisitIndigo,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (dynamicDistricts.isNotEmpty()) {
                                        val unlockedKeys = dynamicDistricts.map { it.normalizedKey }.filter { key ->
                                            val low = key.lowercase(Locale.US)
                                            low !in lockedByBatch && low !in lockedByExisting
                                        }
                                        val allUnlockedSelected = unlockedKeys.isNotEmpty() && unlockedKeys.all { uKey ->
                                            currentDraft.selectedDistricts.any { it.equals(uKey, ignoreCase = true) }
                                        }
                                        TextButton(
                                            onClick = {
                                                val updatedSet = if (allUnlockedSelected) {
                                                    emptySet()
                                                } else {
                                                    unlockedKeys.toSet()
                                                }
                                                drafts[safeActiveIdx] = currentDraft.copy(selectedDistricts = updatedSet)
                                                validationBanner = null
                                            },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .heightIn(min = 38.dp)
                                                .testTag("btn_toggle_all_visit_districts")
                                        ) {
                                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (allUnlockedSelected) {
                                                    if (isEn) "Clear" else "Kosongkan"
                                                } else {
                                                    if (isEn) "Pick Remaining" else "Pilih Sisa"
                                                },
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (dynamicDistricts.isEmpty()) {
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                                    ) {
                                        Text(
                                            text = if (isEn) {
                                                "No student data in this school yet. Import/add students with their District (Kecamatan) first so districts appear automatically."
                                            } else {
                                                "Belum ada data siswa di sekolah ini. Tambahkan/impor siswa beserta Kecamatan terlebih dahulu agar daftar kecamatan muncul otomatis."
                                            },
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF92400E),
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                } else {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        dynamicDistricts.forEach { group ->
                                            val normLower = group.normalizedKey.lowercase(Locale.US)
                                            val batchOwnerIdx = lockedByBatch[normLower]
                                            val existingOwnerTitle = lockedByExisting[normLower]
                                            val isLocked = batchOwnerIdx != null || existingOwnerTitle != null

                                            val isSelected = !isLocked && currentDraft.selectedDistricts.any {
                                                it.equals(group.normalizedKey, ignoreCase = true)
                                            }

                                            val lockSuffix = when {
                                                batchOwnerIdx != null -> if (isEn) " • S${batchOwnerIdx + 1}" else " • Sesi ${batchOwnerIdx + 1}"
                                                existingOwnerTitle != null -> if (isEn) " • Used" else " • Terjadwal"
                                                else -> " (${group.students.size})"
                                            }

                                            FilterChip(
                                                selected = isSelected,
                                                enabled = !isLocked,
                                                onClick = {
                                                    if (!isLocked) {
                                                        val nextSet = if (isSelected) {
                                                            currentDraft.selectedDistricts.filterNot {
                                                                it.equals(group.normalizedKey, ignoreCase = true)
                                                            }.toSet()
                                                        } else {
                                                            currentDraft.selectedDistricts + group.normalizedKey
                                                        }
                                                        drafts[safeActiveIdx] = currentDraft.copy(selectedDistricts = nextSet)
                                                        validationBanner = null
                                                    }
                                                },
                                                label = {
                                                    Text(
                                                        text = "${group.displayName}$lockSuffix",
                                                        fontSize = 11.5.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LocationOn,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = VisitTealLight,
                                                    selectedLabelColor = VisitTeal,
                                                    selectedLeadingIconColor = VisitTeal,
                                                    disabledContainerColor = Color(0xFFF1F5F9),
                                                    disabledLabelColor = Color(0xFF94A3B8),
                                                    disabledLeadingIconColor = Color(0xFF94A3B8)
                                                ),
                                                modifier = Modifier
                                                    .heightIn(min = 38.dp)
                                                    .testTag("chip_admin_district_${group.normalizedKey}")
                                            )
                                        }
                                    }

                                    val sessionStudentCount = remember(students, currentDraft.selectedDistricts) {
                                        if (currentDraft.selectedDistricts.isEmpty()) 0
                                        else {
                                            val allowedLower = currentDraft.selectedDistricts.map { it.lowercase(Locale.US) }.toSet()
                                            students.count { st ->
                                                VisitHelper.resolveStudentDistrictKey(st).lowercase(Locale.US) in allowedLower
                                            }
                                        }
                                    }

                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (sessionStudentCount > 0) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (sessionStudentCount > 0) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Text(
                                            text = if (sessionStudentCount > 0) {
                                                if (isEn) {
                                                    "Session ${safeActiveIdx + 1}: $sessionStudentCount students from ${currentDraft.selectedDistricts.size} district(s)"
                                                } else {
                                                    "Sesi ${safeActiveIdx + 1}: $sessionStudentCount siswa dari ${currentDraft.selectedDistricts.size} kecamatan terpilih"
                                                }
                                            } else {
                                                if (isEn) {
                                                    "Districts selected in another session are locked automatically."
                                                } else {
                                                    "Pilih kecamatan untuk sesi ini. Kecamatan di sesi lain otomatis terkunci agar tidak bentrok."
                                                }
                                            },
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (sessionStudentCount > 0) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = currentDraft.description,
                                onValueChange = { desc ->
                                    drafts[safeActiveIdx] = currentDraft.copy(description = desc)
                                },
                                label = { Text(if (isEn) "Notes (Optional)" else "Keterangan Jadwal (Opsional)") },
                                placeholder = {
                                    Text(
                                        if (isEn) "e.g., Visit at Main Hall & Dormitory" else "Contoh: Kunjungan di Aula Utama & Asrama"
                                    )
                                },
                                maxLines = 2,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Ringkasan 1-4 Sesi sebelum simpan
                    if (!isEditMode && activeDraftCount > 1) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = if (isEn) "Sessions Summary ($activeDraftCount):" else "Ringkasan $activeDraftCount Sesi Kunjungan:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VisitIndigo
                                )
                                for (i in 0 until activeDraftCount) {
                                    val d = drafts[i]
                                    val distList = d.selectedDistricts.toList().sorted()
                                    val distSummary = if (distList.isEmpty()) {
                                        if (isEn) "No districts yet" else "Belum pilih kecamatan"
                                    } else {
                                        distList.joinToString(", ") { VisitHelper.displayDistrict(it, isEn) }
                                    }
                                    Text(
                                        text = "• Sesi ${i + 1} (${shortDateFmt.format(Date(d.dateMillis))}, ${VisitHelper.normalizeTimeRange(d.visitTimeRange)}): $distSummary",
                                        fontSize = 11.5.sp,
                                        color = if (distList.isEmpty()) Color(0xFFB45309) else Color(0xFF1E3A8A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    val shownWarning = validationBanner ?: when {
                        activeDateDuplicates -> if (isEn) {
                            "Every session must have a different date."
                        } else {
                            "Tanggal antar sesi wajib berbeda."
                        }
                        !allDraftsValid && dynamicDistricts.isNotEmpty() -> if (isEn) {
                            "Please select at least 1 district for every active session."
                        } else {
                            "Pastikan setiap sesi (${1}–$activeDraftCount) memiliki minimal 1 kecamatan terpilih."
                        }
                        else -> null
                    }
                    if (shownWarning != null) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                                Text(
                                    text = shownWarning,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }

                // Footer Aksi Bawah (Pinned)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_cancel_create_visit_sheet")
                    ) {
                        Text(s.cancel, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            val slice = drafts.take(activeDraftCount)
                            val uniqueDates = slice.map { VisitHelper.normalizeDayStart(it.dateMillis) }.distinct()
                            if (uniqueDates.size != slice.size) {
                                validationBanner = if (isEn) {
                                    "Every session must have a different date."
                                } else {
                                    "Tanggal tiap sesi tidak boleh sama."
                                }
                                return@Button
                            }
                            if (!isEditMode && onConfirmBatch != null && (activeDraftCount > 1 || slice.size == 1)) {
                                val batchDrafts = slice.map { d ->
                                    VisitSessionDraft(
                                        title = d.title.trim(),
                                        monthYear = selectedMonthYear,
                                        visitDateMillis = VisitHelper.normalizeDayStart(d.dateMillis),
                                        selectedDistricts = d.selectedDistricts.toList(),
                                        description = d.description.trim(),
                                        visitTimeRange = VisitHelper.normalizeTimeRange(d.visitTimeRange)
                                    )
                                }
                                onConfirmBatch(batchDrafts)
                            } else {
                                val d = slice.first()
                                onConfirm(
                                    d.title.trim(),
                                    selectedMonthYear,
                                    listOf(VisitHelper.normalizeDayStart(d.dateMillis)),
                                    d.selectedDistricts.toList(),
                                    d.description.trim(),
                                    VisitHelper.normalizeTimeRange(d.visitTimeRange)
                                )
                            }
                        },
                        enabled = allDraftsValid,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VisitIndigo),
                        modifier = Modifier
                            .weight(1.4f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_save_visit_session")
                    ) {
                        Text(
                            text = when {
                                isEditMode -> if (isEn) "Save Changes" else "Simpan Perubahan"
                                activeDraftCount > 1 -> if (isEn) "Save $activeDraftCount Sessions" else "Simpan $activeDraftCount Sesi"
                                else -> if (isEn) "Create Session" else "Simpan Sesi"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    datePickerSessionIdx?.let { targetIdx ->
        val initialPickerDate = drafts[targetIdx].dateMillis
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialPickerDate)
        DatePickerDialog(
            onDismissRequest = { datePickerSessionIdx = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { picked ->
                        val norm = VisitHelper.normalizeDayStart(picked)
                        val conflictingOther = (0 until activeDraftCount).firstOrNull { otherIdx ->
                            otherIdx != targetIdx && VisitHelper.normalizeDayStart(drafts[otherIdx].dateMillis) == norm
                        }
                        if (conflictingOther != null) {
                            validationBanner = if (isEn) {
                                "Date is already assigned to Session ${conflictingOther + 1}. Please pick a different date."
                            } else {
                                "Tanggal tersebut sudah dipakai Sesi ${conflictingOther + 1}. Pilih tanggal lain agar tiap sesi berbeda."
                            }
                        } else {
                            drafts[targetIdx] = drafts[targetIdx].copy(dateMillis = norm)
                            validationBanner = null
                        }
                    }
                    datePickerSessionIdx = null
                }) {
                    Text(if (isEn) "Select Date" else "Pilih Tanggal")
                }
            },
            dismissButton = {
                TextButton(onClick = { datePickerSessionIdx = null }) {
                    Text(s.cancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun computeDistinctMonthVisitDates(monthYear: String, count: Int): List<Long> {
    val parts = monthYear.trim().split("-")
    val cal = Calendar.getInstance()
    if (parts.size == 2) {
        val y = parts[0].toIntOrNull()
        val m = parts[1].toIntOrNull()
        if (y != null && m != null && m in 1..12) {
            cal.set(Calendar.YEAR, y)
            cal.set(Calendar.MONTH, m - 1)
        }
    }
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    val targetMonth = cal.get(Calendar.MONTH)
    val sundays = mutableListOf<Long>()
    while (cal.get(Calendar.MONTH) == targetMonth) {
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            sundays.add(VisitHelper.normalizeDayStart(cal.timeInMillis))
        }
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    val chosen = sundays.toMutableList()
    // Ensure we always have up to [count] distinct dates in the month
    var fallbackDay = 7
    while (chosen.size < count) {
        val c2 = (cal.clone() as Calendar).apply {
            set(Calendar.YEAR, parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR))
            set(Calendar.MONTH, (parts.getOrNull(1)?.toIntOrNull()?.minus(1)) ?: targetMonth)
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, fallbackDay.coerceAtMost(maxDay))
        }
        val dayMillis = VisitHelper.normalizeDayStart(c2.timeInMillis)
        if (dayMillis !in chosen) chosen.add(dayMillis)
        fallbackDay += 7
        if (fallbackDay > 35) break
    }
    return chosen.distinct().sorted().take(count)
}
