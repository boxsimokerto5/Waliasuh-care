@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens

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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.DashboardBannerActionType
import com.example.data.model.DashboardBannerFeatureTarget
import com.example.data.model.DashboardBannerItem
import com.example.data.model.KonselingHelper
import com.example.data.model.MedicalRecord
import com.example.data.model.ShiftMessage
import com.example.data.model.TabunganHelper
import com.example.ui.HealthViewModel
import com.example.ui.components.CategoryHubOverlay
import com.example.ui.components.DashboardBannerCarousel
import com.example.ui.components.DashboardCategoryHub
import com.example.ui.components.DashboardModalState
import com.example.ui.components.FaskesBadge
import com.example.ui.components.QuickMenuTiles
import com.example.ui.components.ShiftMessageDetailSheet
import com.example.ui.components.StatusBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.components.TodayShiftMessagesStrip
import com.example.ui.components.rememberDashboardModalState
import com.example.ui.dialogs.AddStudentDialog
import com.example.ui.dialogs.ChecklistPuasaSheet
import com.example.ui.dialogs.CreateFastingSessionDialog
import com.example.ui.dialogs.CreatePlanDialog
import com.example.ui.dialogs.CreateShiftMessageDialog
import com.example.ui.dialogs.CreateVisitSessionDialog
import com.example.ui.dialogs.DaftarHaidSheet
import com.example.ui.dialogs.DaftarHadirKunjunganSheet
import com.example.ui.dialogs.DaftarPuasaSheet
import com.example.ui.dialogs.DaftarSuciSheet
import com.example.ui.dialogs.DashboardBannerEditorSheet
import com.example.ui.dialogs.DashboardLayoutEditorSheet
import com.example.ui.dialogs.DepartReferralDialog
import com.example.ui.dialogs.DirectTreatmentDialog
import com.example.ui.dialogs.FaskesReferralSheet
import com.example.ui.dialogs.FiqihHaidDialog
import com.example.ui.dialogs.InputHaidDialog
import com.example.ui.dialogs.InputKonselingDialog
import com.example.ui.dialogs.InputMengajiDialog
import com.example.ui.dialogs.InputPuasaDialog
import com.example.ui.dialogs.InputTabunganDialog
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalHospital
import com.example.ui.dialogs.InputTreatmentResultDialog
import com.example.ui.dialogs.JadwalKunjunganSheet
import com.example.ui.dialogs.KartuAksesOrangTuaSheet
import com.example.ui.dialogs.KartuMengajiSheet
import com.example.ui.dialogs.KonselingFollowUpSheet
import com.example.ui.dialogs.KonselingGuideDialog
import com.example.ui.dialogs.KonselingHistoryLogsSheet
import com.example.ui.dialogs.KonselingStudentDetailSheet
import com.example.ui.dialogs.LaporanKunjunganSheet
import com.example.ui.dialogs.PesanOrangTuaSheet
import com.example.ui.dialogs.PilihAnakAsuhSheet
import com.example.ui.dialogs.PilihanAksesOrangTuaSheet
import com.example.ui.dialogs.PrintPdfDialog
import com.example.ui.dialogs.RankingPuasaSheet
import com.example.ui.dialogs.RiwayatMengajiSheet
import com.example.ui.dialogs.ShareWhatsAppDialog
import com.example.ui.dialogs.ShiftHistorySheet
import com.example.ui.dialogs.ShiftMessagesSheet
import com.example.ui.dialogs.SiswaPerKecamatanSheet
import com.example.ui.dialogs.StudentDetailDialog
import com.example.ui.dialogs.TabunganHistoryLogsSheet
import com.example.ui.dialogs.TabunganStudentDetailSheet
import com.example.ui.dialogs.TabunganStudentListSheet
import com.example.ui.theme.StatusDeparted
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import com.example.util.TopOnAdManager
import com.example.util.i18n.LocalAppStrings
import java.io.File
import java.util.Calendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HealthViewModel,
    waliName: String,
    activePlans: List<MedicalRecord>,
    students: List<ChildStudent>,
    onNavigateToPlan: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    modalState: DashboardModalState = rememberDashboardModalState(),
    renderInlineCategoryHub: Boolean = true
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val shiftMessages by viewModel.shiftMessages.collectAsStateWithLifecycle()
    val waliAccounts by viewModel.waliAccounts.collectAsStateWithLifecycle()
    val activeHaidRecords by viewModel.activeHaidRecords.collectAsStateWithLifecycle()
    val suciRecords by viewModel.suciRecords.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val fastingSessions by viewModel.fastingSessions.collectAsStateWithLifecycle()
    val fastingRecords by viewModel.fastingRecords.collectAsStateWithLifecycle()
    val mengajiRecords by viewModel.mengajiRecords.collectAsStateWithLifecycle()
    val visitSessions by viewModel.visitSessions.collectAsStateWithLifecycle()
    val visitRecords by viewModel.visitRecords.collectAsStateWithLifecycle()
    val parentAccessLinks by viewModel.parentAccessLinks.collectAsStateWithLifecycle()
    val parentMessages by viewModel.parentMessages.collectAsStateWithLifecycle()
    val tabunganTransactions by viewModel.tabunganTransactions.collectAsStateWithLifecycle()
    val tabunganLogs by viewModel.tabunganLogs.collectAsStateWithLifecycle()
    val konselingRecords by viewModel.konselingRecords.collectAsStateWithLifecycle()
    val konselingLogs by viewModel.konselingLogs.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val currentUsername by viewModel.currentUsername.collectAsStateWithLifecycle()
    val isAnakSuperAdmin by viewModel.isAnakSuperAdmin.collectAsStateWithLifecycle()
    val dashboardIconOrder by viewModel.dashboardIconOrder.collectAsStateWithLifecycle()
    val isSavingDashboardLayout by viewModel.isSavingDashboardLayout.collectAsStateWithLifecycle()
    val dashboardBannerConfig by viewModel.dashboardBannerConfig.collectAsStateWithLifecycle()
    val isSavingDashboardBanner by viewModel.isSavingDashboardBanner.collectAsStateWithLifecycle()
    val isUploadingBannerImage by viewModel.isUploadingBannerImage.collectAsStateWithLifecycle()
    val isAdminUser = currentUserRole == com.example.ui.UserRole.ADMIN || currentUserRole == com.example.ui.UserRole.SUPERADMIN

    LaunchedEffect(Unit) {
        viewModel.syncDashboardIconLayoutNow()
        viewModel.syncDashboardBannersNow()
    }
    LaunchedEffect(
        modalState.activeCategoryHub,
        modalState.showInputTabunganDialog,
        modalState.showTabunganStudentListSheet,
        modalState.showTabunganHistoryLogsSheet,
        modalState.tabunganStudentForDetail
    ) {
        if (modalState.activeCategoryHub == DashboardCategoryHub.TABUNGAN ||
            modalState.showInputTabunganDialog ||
            modalState.showTabunganStudentListSheet ||
            modalState.showTabunganHistoryLogsSheet ||
            modalState.tabunganStudentForDetail != null
        ) {
            viewModel.syncTabunganOnlineQuietly()
        }
    }
    LaunchedEffect(
        modalState.activeCategoryHub,
        modalState.showInputKonselingDialog,
        modalState.showKonselingFollowUpSheet,
        modalState.showKonselingHistoryLogsSheet,
        modalState.konselingStudentForDetail
    ) {
        if (modalState.activeCategoryHub == DashboardCategoryHub.KONSELING ||
            modalState.showInputKonselingDialog ||
            modalState.showKonselingFollowUpSheet ||
            modalState.showKonselingHistoryLogsSheet ||
            modalState.konselingStudentForDetail != null
        ) {
            viewModel.syncKonselingOnlineQuietly()
        }
    }
    val tabunganTotals = remember(tabunganTransactions) {
        TabunganHelper.computeAggregateTotals(tabunganTransactions)
    }
    val konselingStats = remember(konselingRecords) {
        KonselingHelper.computeAggregateStats(konselingRecords)
    }
    val myFosterStudents = remember(students, waliName, currentUsername) {
        students.filter { viewModel.isStudentClaimedByCurrentWali(it) }
    }
    val myFosterStudentIds = remember(myFosterStudents) {
        myFosterStudents.map { it.id }.toSet()
    }
    val myFosterStudentsCount = myFosterStudents.size
    val myFosterParentMessages = remember(parentMessages, myFosterStudentIds) {
        parentMessages.filter { it.studentId in myFosterStudentIds }
    }
    val unreadParentMessagesCount = remember(myFosterParentMessages) {
        myFosterParentMessages.count { !it.isReadByWali && it.senderRole.equals("PARENT", ignoreCase = true) }
    }
    var recordToRefer by remember { mutableStateOf<MedicalRecord?>(null) }
    var recordToDiagnose by remember { mutableStateOf<MedicalRecord?>(null) }
    var recordPlanToEdit by remember { mutableStateOf<MedicalRecord?>(null) }
    var recordTreatmentToEdit by remember { mutableStateOf<MedicalRecord?>(null) }
    var selectedInpatientRecordForDetail by remember { mutableStateOf<MedicalRecord?>(null) }
    var inpatientPickerForFaskes by remember { mutableStateOf<Pair<String, List<MedicalRecord>>?>(null) }
    var recordToShare by remember { mutableStateOf<MedicalRecord?>(null) }
    var studentForPdfPrint by remember { mutableStateOf<ChildStudent?>(null) }
    var readyTabunganSummaryPdf by remember { mutableStateOf<File?>(null) }
    var readyKonselingSummaryPdf by remember { mutableStateOf<File?>(null) }
    var showGridTiles by remember { mutableStateOf(true) }
    var showLayoutEditorSheet by remember { mutableStateOf(false) }
    var showBannerEditorSheet by remember { mutableStateOf(false) }
    var selectedTodayShiftMessageId by remember { mutableStateOf<Long?>(null) }

    fun handleBannerItemClick(banner: DashboardBannerItem) {
        TopOnAdManager.onUserActionClick(context)
        when (banner.actionType) {
            DashboardBannerActionType.NONE -> Unit
            DashboardBannerActionType.EXTERNAL_LINK -> {
                val rawUrl = banner.actionTarget.trim()
                if (rawUrl.isBlank()) return
                val normalizedUrl = if (rawUrl.startsWith("http://", ignoreCase = true) ||
                    rawUrl.startsWith("https://", ignoreCase = true)
                ) {
                    rawUrl
                } else {
                    "https://$rawUrl"
                }
                val opened = runCatching {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }.isSuccess
                if (!opened) {
                    Toast.makeText(
                        context,
                        if (isEn) "Unable to open link: $normalizedUrl" else "Tidak dapat membuka tautan: $normalizedUrl",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            DashboardBannerActionType.INTERNAL_FEATURE -> {
                when (DashboardBannerFeatureTarget.fromKey(banner.actionTarget)) {
                    DashboardBannerFeatureTarget.FEATURE_FASKES_INFO -> modalState.showPuskesmasInfoSheet = true
                    DashboardBannerFeatureTarget.FEATURE_BEROBAT_HUB -> modalState.activeCategoryHub = DashboardCategoryHub.BEROBAT
                    DashboardBannerFeatureTarget.FEATURE_CREATE_PLAN -> modalState.showCreateDialog = true
                    DashboardBannerFeatureTarget.FEATURE_DIRECT_TREATMENT -> modalState.showDirectTreatmentDialog = true
                    DashboardBannerFeatureTarget.FEATURE_PLAN_PAGE -> onNavigateToPlan()
                    DashboardBannerFeatureTarget.FEATURE_RECORDS_PAGE -> onNavigateToRecords()

                    DashboardBannerFeatureTarget.FEATURE_PUASA_HUB -> modalState.activeCategoryHub = DashboardCategoryHub.PUASA
                    DashboardBannerFeatureTarget.FEATURE_INPUT_PUASA -> modalState.showInputPuasaDialog = true
                    DashboardBannerFeatureTarget.FEATURE_CREATE_PUASA_SESSION -> modalState.showCreatePuasaSessionDialog = true
                    DashboardBannerFeatureTarget.FEATURE_DAFTAR_PUASA -> modalState.showDaftarPuasaSheet = true
                    DashboardBannerFeatureTarget.FEATURE_CHECKLIST_PUASA -> modalState.showChecklistPuasaSheet = true
                    DashboardBannerFeatureTarget.FEATURE_RANKING_PUASA -> modalState.showRankingPuasaSheet = true

                    DashboardBannerFeatureTarget.FEATURE_HAID_HUB -> modalState.activeCategoryHub = DashboardCategoryHub.HAID
                    DashboardBannerFeatureTarget.FEATURE_INPUT_HAID -> modalState.showInputHaidDialog = true
                    DashboardBannerFeatureTarget.FEATURE_DAFTAR_HAID -> modalState.showDaftarHaidSheet = true
                    DashboardBannerFeatureTarget.FEATURE_DAFTAR_SUCI -> modalState.showDaftarSuciSheet = true
                    DashboardBannerFeatureTarget.FEATURE_FIQIH_HAID -> modalState.showFiqihHaidDialog = true

                    DashboardBannerFeatureTarget.FEATURE_MENGAJI_HUB -> modalState.activeCategoryHub = DashboardCategoryHub.MENGAJI
                    DashboardBannerFeatureTarget.FEATURE_INPUT_MENGAJI -> {
                        modalState.mengajiTargetStudent = null
                        modalState.mengajiRecordToEdit = null
                        modalState.showInputMengajiDialog = true
                    }
                    DashboardBannerFeatureTarget.FEATURE_RIWAYAT_MENGAJI -> modalState.showRiwayatMengajiSheet = true
                    DashboardBannerFeatureTarget.FEATURE_KARTU_MENGAJI -> {
                        modalState.mengajiTargetStudent = null
                        modalState.showKartuMengajiSheet = true
                    }

                    DashboardBannerFeatureTarget.FEATURE_PESAN_HUB -> modalState.activeCategoryHub = DashboardCategoryHub.PESAN
                    DashboardBannerFeatureTarget.FEATURE_DAFTAR_PESAN -> modalState.showShiftMessagesSheet = true
                    DashboardBannerFeatureTarget.FEATURE_BUAT_PESAN -> modalState.showCreateShiftMessageDialog = true
                    DashboardBannerFeatureTarget.FEATURE_RIWAYAT_PESAN -> modalState.showShiftHistorySheet = true

                    DashboardBannerFeatureTarget.FEATURE_ORTU_HUB -> modalState.activeCategoryHub = DashboardCategoryHub.ORANG_TUA
                    DashboardBannerFeatureTarget.FEATURE_PILIH_ANAK_ASUH -> modalState.showPilihAnakAsuhSheet = true
                    DashboardBannerFeatureTarget.FEATURE_KARTU_AKSES_ORTU -> modalState.showKartuAksesOrtuSheet = true
                    DashboardBannerFeatureTarget.FEATURE_PILIHAN_AKSES_ORTU -> modalState.showPilihanAksesOrtuSheet = true
                    DashboardBannerFeatureTarget.FEATURE_CHAT_ORTU -> modalState.showPesanOrtuSheet = true

                    DashboardBannerFeatureTarget.FEATURE_KUNJUNGAN_HUB -> modalState.activeCategoryHub = DashboardCategoryHub.KUNJUNGAN
                    DashboardBannerFeatureTarget.FEATURE_JADWAL_KUNJUNGAN -> modalState.showJadwalKunjunganSheet = true
                    DashboardBannerFeatureTarget.FEATURE_DAFTAR_HADIR_KUNJUNGAN -> {
                        modalState.daftarHadirKunjunganInitialTab = 0
                        modalState.showDaftarHadirKunjunganSheet = true
                    }
                    DashboardBannerFeatureTarget.FEATURE_LAPORAN_KUNJUNGAN -> modalState.showLaporanKunjunganSheet = true
                    DashboardBannerFeatureTarget.FEATURE_SISWA_PER_KECAMATAN -> modalState.showSiswaPerKecamatanSheet = true
                    DashboardBannerFeatureTarget.FEATURE_TABUNGAN_HUB -> modalState.activeCategoryHub = DashboardCategoryHub.TABUNGAN
                    DashboardBannerFeatureTarget.FEATURE_KONSELING_HUB -> modalState.activeCategoryHub = DashboardCategoryHub.KONSELING

                    DashboardBannerFeatureTarget.FEATURE_STUDENTS_PAGE -> onNavigateToStudents()
                    DashboardBannerFeatureTarget.FEATURE_PROFILE_PAGE -> onNavigateToProfile()
                    null -> modalState.showPuskesmasInfoSheet = true
                }
            }
        }
    }

    val todayDateFormatted = remember(s.language) {
        DateUtils.formatFullDate(Calendar.getInstance().timeInMillis)
    }
    val todayShortFormatted = remember(s.language) {
        DateUtils.formatShortDate(Calendar.getInstance().timeInMillis, s.language)
    }

    val todayShiftMessages = remember(shiftMessages) {
        shiftMessages
            .filter { DateUtils.isToday(it.followUpDate) }
            .sortedWith(
                compareBy<ShiftMessage> { it.isCompleted }
                    .thenBy {
                        when (it.priority) {
                            "URGENT" -> 0
                            "SEDANG" -> 1
                            else -> 2
                        }
                    }
                    .thenByDescending { it.createdAt }
            )
    }
    val selectedTodayShiftMessage = remember(todayShiftMessages, shiftMessages, selectedTodayShiftMessageId) {
        val targetId = selectedTodayShiftMessageId ?: return@remember null
        shiftMessages.firstOrNull { it.id == targetId }
            ?: todayShiftMessages.firstOrNull { it.id == targetId }
    }

    val todayPlans = remember(activePlans) {
        activePlans.filter { DateUtils.isToday(it.planDate) }
    }
    val upcomingPlans = remember(activePlans) {
        activePlans.filter { !DateUtils.isToday(it.planDate) }
    }
    val inpatientRecords = remember(allRecords) {
        allRecords
            .filter { it.isInpatient && it.status != "CANCELLED" }
            .sortedByDescending { it.treatmentDate ?: it.planDate }
    }
    var selectedPlanFilter by remember { mutableStateOf("Hari Ini") }

    val allFaskes = schoolProfile.getAllFaskes()
    val totalToday = todayPlans.size
    val totalSchoolStudents = students.size
    val progressRatio = (totalToday.toFloat() / 20f).coerceIn(0f, 1f)

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .testTag("dashboard_lazy_list"),
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Compact Gojek-Style Top Faskes Notification Strip Card
                item {
                    val customFaskesInpatients = remember(allFaskes, inpatientRecords, isEn) {
                        inpatientRecords.filter { rec ->
                            allFaskes.none { f -> rec.faskes.contains(f, ignoreCase = true) }
                        }.groupBy { it.faskes.ifBlank { if (isEn) "Referral Faskes" else "Faskes Rujukan" } }
                    }
                    val sortedFaskesEntries = remember(allFaskes, todayPlans, inpatientRecords) {
                        allFaskes.map { faskesName ->
                            val count = todayPlans.count { it.faskes.contains(faskesName, ignoreCase = true) }
                            val faskesInpatients = inpatientRecords.filter {
                                it.faskes.contains(faskesName, ignoreCase = true)
                            }
                            Triple(faskesName, count, faskesInpatients)
                        }.sortedWith(
                            compareByDescending<Triple<String, Int, List<MedicalRecord>>> { it.third.isNotEmpty() }
                                .thenByDescending { it.second > 0 }
                        )
                    }

                    val isDarkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                    val heroGradient = if (isDarkSurface) {
                        listOf(Color(0xFF064E3B), Color(0xFF065F46), Color(0xFF047857))
                    } else {
                        listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7), Color(0xFFD1FAE5))
                    }
                    val heroBorderColor = if (isDarkSurface) {
                        Color(0xFF34D399).copy(alpha = 0.45f)
                    } else {
                        Color(0xFF86EFAC)
                    }
                    val heroTitleColor = if (isDarkSurface) Color(0xFFECFDF5) else Color(0xFF064E3B)
                    val heroSubtitleColor = if (isDarkSurface) Color(0xFFA7F3D0) else Color(0xFF14532D)
                    val heroAccentGreen = if (isDarkSurface) Color(0xFF10B981) else Color(0xFF047857)
                    val heroDateBg = if (isDarkSurface) Color(0xFF022C22).copy(alpha = 0.72f) else Color.White.copy(alpha = 0.88f)
                    val heroDateText = if (isDarkSurface) Color(0xFF6EE7B7) else Color(0xFF065F46)

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_summary_today"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.linearGradient(heroGradient))
                                .border(1.dp, heroBorderColor, RoundedCornerShape(14.dp))
                        ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Row 1: Concise Title + Short Date Chip + Wali Shortcut + Slim "+ Rencana" CTA
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (isEn) "Today's Care" else "Berobat Hari Ini",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = heroTitleColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = heroDateBg,
                                            border = BorderStroke(0.8.dp, heroBorderColor.copy(alpha = 0.8f))
                                        ) {
                                            Text(
                                                text = todayShortFormatted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = heroDateText,
                                                maxLines = 1,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable(onClick = onNavigateToProfile)
                                            .padding(vertical = 1.dp, horizontal = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = heroSubtitleColor,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "Wali: $waliName",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = heroSubtitleColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(heroAccentGreen)
                                        .clickable {
                                            TopOnAdManager.onUserActionClick(context)
                                            modalState.showCreateDialog = true
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("fab_dashboard_add"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val ctaTextColor = if (isDarkSurface) Color(0xFF022C22) else Color.White
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = if (isEn) "Add Sick Student / Treatment Plan" else "Tambah Anak Sakit / Rencana",
                                        tint = ctaTextColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (isEn) "Plan" else "Rencana",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ctaTextColor,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Row 2: Horizontal Scrollable Faskes Pill Strip (Inpatients & Active Plans First)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .testTag("row_faskes_strip"),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                sortedFaskesEntries.forEach { (faskesName, count, faskesInpatients) ->
                                    val hasActive = count > 0 || faskesInpatients.isNotEmpty()
                                    val faskesSlug = faskesName.lowercase().replace(" ", "_")
                                    val pillBg = when {
                                        isDarkSurface && hasActive -> Color(0xFF022C22).copy(alpha = 0.85f)
                                        isDarkSurface -> Color(0xFF022C22).copy(alpha = 0.55f)
                                        hasActive -> Color.White
                                        else -> Color.White.copy(alpha = 0.85f)
                                    }
                                    val pillBorder = when {
                                        isDarkSurface && hasActive -> Color(0xFF6EE7B7).copy(alpha = 0.7f)
                                        isDarkSurface -> Color(0xFF34D399).copy(alpha = 0.3f)
                                        hasActive -> Color(0xFF34D399)
                                        else -> Color(0xFFA7F3D0)
                                    }
                                    val pillLabelColor = when {
                                        isDarkSurface -> Color(0xFFECFDF5)
                                        hasActive -> Color(0xFF064E3B)
                                        else -> Color(0xFF1E293B)
                                    }
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(pillBg)
                                            .border(
                                                width = 1.dp,
                                                color = pillBorder,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.5.dp)
                                            .testTag("pill_faskes_$faskesSlug"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.5.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        faskesInpatients.isNotEmpty() -> Color(0xFFDC2626)
                                                        count > 0 -> heroAccentGreen
                                                        else -> if (isDarkSurface) Color(0xFF6EE7B7).copy(alpha = 0.6f) else Color(0xFF64748B).copy(alpha = 0.55f)
                                                    }
                                                )
                                        )
                                        Text(
                                            text = faskesName,
                                            fontSize = 11.sp,
                                            fontWeight = if (hasActive) FontWeight.Bold else FontWeight.Medium,
                                            color = pillLabelColor,
                                            maxLines = 1
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = when {
                                                count > 0 -> heroAccentGreen
                                                isDarkSurface -> Color(0xFF065F46)
                                                else -> Color(0xFFECFDF5)
                                            }
                                        ) {
                                            Text(
                                                text = if (count > 0) {
                                                    if (isEn) "$count Students" else "$count Anak"
                                                } else {
                                                    "0"
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    count > 0 -> if (isDarkSurface) Color(0xFF022C22) else Color.White
                                                    isDarkSurface -> Color(0xFFA7F3D0)
                                                    else -> Color(0xFF065F46)
                                                },
                                                maxLines = 1,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                        if (faskesInpatients.isNotEmpty()) {
                                            Surface(
                                                onClick = {
                                                    TopOnAdManager.onUserActionClick(context)
                                                    if (faskesInpatients.size == 1) {
                                                        selectedInpatientRecordForDetail = faskesInpatients.first()
                                                    } else {
                                                        inpatientPickerForFaskes = faskesName to faskesInpatients
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFFEE2E2),
                                                modifier = Modifier.testTag("badge_inpatient_$faskesSlug")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocalHospital,
                                                        contentDescription = if (isEn) "Inpatient" else "Rawat Inap",
                                                        tint = Color(0xFFDC2626),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = if (isEn) "${faskesInpatients.size} Inpatient" else "${faskesInpatients.size} Rawat Inap",
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB91C1C),
                                                        maxLines = 1
                                                    )
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                        contentDescription = null,
                                                        tint = Color(0xFFB91C1C),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                customFaskesInpatients.forEach { (faskesName, list) ->
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = if (isDarkSurface) 0.12f else 0.92f))
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xFFDC2626).copy(alpha = 0.35f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            text = faskesName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkSurface) Color(0xFFECFDF5) else Color(0xFF0F172A),
                                            maxLines = 1
                                        )
                                        Surface(
                                            onClick = {
                                                TopOnAdManager.onUserActionClick(context)
                                                if (list.size == 1) {
                                                    selectedInpatientRecordForDetail = list.first()
                                                } else {
                                                    inpatientPickerForFaskes = faskesName to list
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFEE2E2),
                                            modifier = Modifier.testTag("badge_inpatient_custom")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocalHospital,
                                                    contentDescription = if (isEn) "Inpatient" else "Rawat Inap",
                                                    tint = Color(0xFFDC2626),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = if (isEn) "${list.size} Inpatient" else "${list.size} Rawat Inap",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFB91C1C),
                                                    maxLines = 1
                                                )
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                    contentDescription = null,
                                                    tint = Color(0xFFB91C1C),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Row 3: Slim Progress Bar + Compact 1-Line Footer
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                LinearProgressIndicator(
                                    progress = { progressRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.5.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = heroAccentGreen,
                                    trackColor = if (isDarkSurface) Color(0xFF022C22).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.75f)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isEn) {
                                            "$totalToday/$totalSchoolStudents Students Scheduled"
                                        } else {
                                            "$totalToday/$totalSchoolStudents Siswa Dijadwalkan"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = heroSubtitleColor
                                    )
                                    if (upcomingPlans.isNotEmpty()) {
                                        Text(
                                            text = if (isEn) "+${upcomingPlans.size} upcoming" else "+${upcomingPlans.size} mendatang",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = heroDateText
                                        )
                                    }
                                }
                            }
                        }
                        }
                    }
                }

                // Super-App Hub Grid ala Gojek (Semua Kategori & Layanan)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Categories & Services" else "Kategori & Layanan",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isAnakSuperAdmin) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WaliPrimary.copy(alpha = 0.12f))
                                        .clickable {
                                            TopOnAdManager.onUserActionClick(context)
                                            showLayoutEditorSheet = true
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("btn_customize_dashboard_layout"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = if (isEn) "Arrange dashboard icons" else "Atur tata letak ikon",
                                        tint = WaliPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isEn) "Arrange" else "Atur Ikon",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WaliPrimary
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    TopOnAdManager.onUserActionClick(context)
                                    showGridTiles = !showGridTiles
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (showGridTiles) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                                    contentDescription = "Toggle Grid Menu",
                                    tint = WaliPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                if (showGridTiles) {
                    item {
                        QuickMenuTiles(
                            onOpenCategoryHub = { category ->
                                TopOnAdManager.onUserActionClick(context)
                                modalState.activeCategoryHub = category
                            },
                            onNavigateToPlan = onNavigateToPlan,
                            onNavigateToDirectTreatment = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showDirectTreatmentDialog = true
                            },
                            onNavigateToRecords = onNavigateToRecords,
                            onNavigateToStudents = onNavigateToStudents,
                            onShowPuskesmasInfo = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showPuskesmasInfoSheet = true
                            },
                            onOpenShiftMessages = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showShiftMessagesSheet = true
                            },
                            onOpenCreateShiftMessage = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showCreateShiftMessageDialog = true
                            },
                            onOpenShiftHistory = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showShiftHistorySheet = true
                            },
                            onOpenInputHaid = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showInputHaidDialog = true
                            },
                            onOpenDaftarHaid = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showDaftarHaidSheet = true
                            },
                            onOpenDaftarSuci = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showDaftarSuciSheet = true
                            },
                            onOpenFiqihHaid = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showFiqihHaidDialog = true
                            },
                            onOpenInputPuasa = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showInputPuasaDialog = true
                            },
                            onOpenDaftarPuasa = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showDaftarPuasaSheet = true
                            },
                            onOpenChecklistPuasa = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showChecklistPuasaSheet = true
                            },
                            onOpenRankingPuasa = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showRankingPuasaSheet = true
                            },
                            onOpenInputMengaji = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.mengajiTargetStudent = null
                                modalState.mengajiRecordToEdit = null
                                modalState.showInputMengajiDialog = true
                            },
                            todayPlansCount = totalToday,
                            inpatientCount = inpatientRecords.size,
                            studentsCount = students.size,
                            activeShiftMessagesCount = shiftMessages.count { !it.isCompleted && DateUtils.isToday(it.followUpDate) },
                            urgentShiftMessagesCount = shiftMessages.count { it.priority == "URGENT" && !it.isCompleted && DateUtils.isToday(it.followUpDate) },
                            pastShiftMessagesCount = shiftMessages.count { DateUtils.isBeforeToday(it.followUpDate) },
                            uncompletedPastShiftCount = shiftMessages.count { !it.isCompleted && DateUtils.isBeforeToday(it.followUpDate) },
                            activeHaidCount = activeHaidRecords.size,
                            suciCount = suciRecords.size,
                            istihadhahCount = activeHaidRecords.count { it.isIstihadhahWarning },
                            fastingSessionsCount = fastingSessions.size,
                            todayFastingCount = run {
                                val latestSession = fastingSessions.firstOrNull { it.isActive } ?: fastingSessions.firstOrNull()
                                if (latestSession == null) 0 else fastingRecords.count { it.sessionId == latestSession.id }
                            },
                            mengajiTodayCount = mengajiRecords.count { DateUtils.isToday(it.sessionDate) },
                            mengajiTotalCount = mengajiRecords.size,
                            visitSessionsCount = visitSessions.size,
                            todayVisitsCount = visitRecords.count { DateUtils.isToday(it.visitDate) },
                            unreadParentMessagesCount = unreadParentMessagesCount,
                            myFosterStudentsCount = myFosterStudentsCount,
                            tabunganTotalBalance = tabunganTotals.totalBalance,
                            tabunganHoldersCount = tabunganTotals.holdersCount,
                            konselingFollowUpCount = konselingStats.followUpCount,
                            konselingTotalCount = konselingStats.totalEntries,
                            schoolShortName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
                            faskesBannerSubtitle = schoolProfile.getReferralFaskesSummary(),
                            iconOrder = dashboardIconOrder,
                            showPuskesmasCard = false
                        )
                    }
                    item {
                        DashboardBannerCarousel(
                            config = dashboardBannerConfig,
                            faskesDefaultSubtitle = schoolProfile.getReferralFaskesSummary(),
                            isAnakSuperAdmin = isAnakSuperAdmin,
                            onBannerClick = { banner -> handleBannerItemClick(banner) },
                            onManageBannersClick = {
                                TopOnAdManager.onUserActionClick(context)
                                showBannerEditorSheet = true
                            }
                        )
                    }
                }

                if (todayShiftMessages.isNotEmpty()) {
                    item {
                        TodayShiftMessagesStrip(
                            messages = todayShiftMessages,
                            onMessageClick = { msg ->
                                TopOnAdManager.onUserActionClick(context)
                                selectedTodayShiftMessageId = msg.id
                            },
                            onOpenAllMessages = {
                                TopOnAdManager.onUserActionClick(context)
                                modalState.showShiftMessagesSheet = true
                            }
                        )
                    }
                }

                // Section Header: Daftar Rencana Berobat ke Faskes
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "Treatment Plan List" else "Daftar Rencana Berobat",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = todayDateFormatted,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        // Filter Chips: Hari Ini, Mendatang, Semua
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = selectedPlanFilter == "Hari Ini",
                                onClick = {
                                    TopOnAdManager.onUserActionClick(context)
                                    selectedPlanFilter = "Hari Ini"
                                },
                                label = { Text(if (isEn) "Today (${todayPlans.size})" else "Hari Ini (${todayPlans.size})", fontSize = 11.sp) },
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("chip_filter_today_plans")
                            )
                            FilterChip(
                                selected = selectedPlanFilter == "Mendatang",
                                onClick = {
                                    TopOnAdManager.onUserActionClick(context)
                                    selectedPlanFilter = "Mendatang"
                                },
                                label = { Text(if (isEn) "Upcoming (${upcomingPlans.size})" else "Mendatang (${upcomingPlans.size})", fontSize = 11.sp) },
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("chip_filter_upcoming_plans")
                            )
                            FilterChip(
                                selected = selectedPlanFilter == "Semua",
                                onClick = {
                                    TopOnAdManager.onUserActionClick(context)
                                    selectedPlanFilter = "Semua"
                                },
                                label = { Text(if (isEn) "All (${activePlans.size})" else "Semua (${activePlans.size})", fontSize = 11.sp) },
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("chip_filter_all_plans")
                            )
                        }
                    }
                }

                val displayedPlans = when (selectedPlanFilter) {
                    "Hari Ini" -> todayPlans
                    "Mendatang" -> upcomingPlans
                    else -> activePlans
                }

                if (displayedPlans.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isEn) {
                                        when (selectedPlanFilter) {
                                            "Hari Ini" -> if (upcomingPlans.isNotEmpty()) {
                                                "No students scheduled for treatment today. There are ${upcomingPlans.size} plans under 'Upcoming'."
                                            } else {
                                                "No students scheduled for treatment today."
                                            }
                                            "Mendatang" -> "No upcoming treatment plans yet."
                                            else -> "No active treatment plans yet."
                                        }
                                    } else {
                                        when (selectedPlanFilter) {
                                            "Hari Ini" -> if (upcomingPlans.isNotEmpty()) {
                                                "Alhamdulillah, tidak ada anak yang dijadwalkan berobat hari ini. Terdapat ${upcomingPlans.size} rencana di jadwal 'Mendatang'."
                                            } else {
                                                "Alhamdulillah, tidak ada anak yang dijadwalkan berobat hari ini."
                                            }
                                            "Mendatang" -> "Belum ada rencana berobat untuk tanggal mendatang."
                                            else -> "Alhamdulillah, belum ada rencana berobat aktif."
                                        }
                                    },
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(displayedPlans, key = { _, record -> record.id }) { index, record ->
                        DashboardSickChildItem(
                            number = index + 1,
                            record = record,
                            onTreatHere = {
                                TopOnAdManager.onUserActionClick(context)
                                val uksFaskes = schoolProfile.primaryFaskesName.ifBlank { "UKS Sekolah" }
                                recordToDiagnose = record.copy(
                                    faskes = if (record.faskes.contains("UKS", ignoreCase = true)) record.faskes else uksFaskes
                                )
                            },
                            onDepart = {
                                TopOnAdManager.onUserActionClick(context)
                                recordToRefer = record
                            },
                            onDiagnose = {
                                TopOnAdManager.onUserActionClick(context)
                                recordToDiagnose = record
                            },
                            onEdit = {
                                TopOnAdManager.onUserActionClick(context)
                                recordPlanToEdit = record
                            },
                            onDelete = {
                                TopOnAdManager.onUserActionClick(context)
                                viewModel.deleteRecord(record)
                            }
                        )
                    }
                }
            }
        }
    }

        // Fallback inline overlay bila DashboardScreen dipanggil langsung tanpa MainActivity wrapper
        if (renderInlineCategoryHub) {
            CategoryHubOverlay(
                category = modalState.activeCategoryHub,
                schoolProfile = schoolProfile,
                activePlans = activePlans,
                inpatientCount = inpatientRecords.size,
                fastingSessions = fastingSessions,
                fastingRecords = fastingRecords,
                activeHaidRecords = activeHaidRecords,
                suciRecords = suciRecords,
                shiftMessages = shiftMessages,
                mengajiRecords = mengajiRecords,
                students = students,
                visitSessions = visitSessions,
                visitRecords = visitRecords,
                parentAccessLinks = parentAccessLinks,
                parentMessages = myFosterParentMessages,
                myFosterStudentsCount = myFosterStudentsCount,
                isAdmin = isAdminUser,
                onDismiss = { modalState.activeCategoryHub = null },
                onNavigateToPlan = {
                    modalState.activeCategoryHub = null
                    onNavigateToPlan()
                },
                onNavigateToRecords = {
                    modalState.activeCategoryHub = null
                    onNavigateToRecords()
                },
                onOpenCreatePlan = { modalState.showCreateDialog = true },
                onOpenDirectTreatment = { modalState.showDirectTreatmentDialog = true },
                onOpenFaskesInfo = { modalState.showPuskesmasInfoSheet = true },
                onOpenInputPuasa = { modalState.showInputPuasaDialog = true },
                onOpenDaftarPuasa = { modalState.showDaftarPuasaSheet = true },
                onOpenChecklistPuasa = { modalState.showChecklistPuasaSheet = true },
                onOpenRankingPuasa = { modalState.showRankingPuasaSheet = true },
                onOpenCreatePuasaSession = { modalState.showCreatePuasaSessionDialog = true },
                onOpenInputHaid = { modalState.showInputHaidDialog = true },
                onOpenDaftarHaid = { modalState.showDaftarHaidSheet = true },
                onOpenDaftarSuci = { modalState.showDaftarSuciSheet = true },
                onOpenFiqihHaid = { modalState.showFiqihHaidDialog = true },
                onOpenShiftMessages = { modalState.showShiftMessagesSheet = true },
                onOpenCreateShiftMessage = { modalState.showCreateShiftMessageDialog = true },
                onOpenShiftHistory = { modalState.showShiftHistorySheet = true },
                onOpenInputMengaji = {
                    modalState.mengajiTargetStudent = null
                    modalState.mengajiRecordToEdit = null
                    modalState.showInputMengajiDialog = true
                },
                onOpenRiwayatMengaji = {
                    modalState.riwayatMengajiInitialTab = 0
                    modalState.showRiwayatMengajiSheet = true
                },
                onOpenKartuMengaji = {
                    modalState.mengajiTargetStudent = null
                    modalState.showKartuMengajiSheet = true
                },
                onOpenRekapMengaji = {
                    modalState.riwayatMengajiInitialTab = 1
                    modalState.showRiwayatMengajiSheet = true
                },
                onOpenJadwalKunjungan = { modalState.showJadwalKunjunganSheet = true },
                onOpenDaftarHadirKunjungan = { tabIdx ->
                    modalState.daftarHadirKunjunganInitialTab = tabIdx
                    modalState.showDaftarHadirKunjunganSheet = true
                },
                onOpenLaporanKunjungan = { modalState.showLaporanKunjunganSheet = true },
                onOpenSiswaPerKecamatan = { modalState.showSiswaPerKecamatanSheet = true },
                onOpenCreateVisitSession = {
                    modalState.visitSessionToEdit = null
                    modalState.showCreateVisitSessionDialog = true
                },
                onOpenPilihAnakAsuh = { modalState.showPilihAnakAsuhSheet = true },
                onOpenKartuAksesOrtu = { modalState.showKartuAksesOrtuSheet = true },
                onOpenPilihanAksesOrtu = { modalState.showPilihanAksesOrtuSheet = true },
                onOpenPesanOrtu = {
                    modalState.selectedParentChatStudent = null
                    modalState.showPesanOrtuSheet = true
                },
                tabunganTransactions = tabunganTransactions,
                tabunganLogs = tabunganLogs,
                claimedFosterStudents = myFosterStudents,
                isAnakSuperAdmin = isAnakSuperAdmin,
                onOpenInputTabungan = { defaultType, targetStudent ->
                    modalState.tabunganTxToEdit = null
                    modalState.inputTabunganDefaultType = defaultType
                    modalState.inputTabunganTargetStudent = targetStudent
                    modalState.showInputTabunganDialog = true
                },
                onOpenTabunganStudentList = { modalState.showTabunganStudentListSheet = true },
                onOpenTabunganStudentDetail = { student -> modalState.tabunganStudentForDetail = student },
                onOpenTabunganHistory = {
                    modalState.tabunganHistoryInitialTab = 0
                    modalState.showTabunganHistoryLogsSheet = true
                },
                onOpenTabunganPdfSummary = {
                    val summaryStudents = TabunganHelper.buildSummaryStudents(
                        myFosterStudents = myFosterStudents,
                        allStudents = students,
                        transactions = tabunganTransactions
                    )
                    val pdf = PdfReportGenerator.generateTabunganSummaryPdf(
                        context = context,
                        students = summaryStudents,
                        transactions = tabunganTransactions,
                        waliName = waliName,
                        schoolProfile = schoolProfile
                    )
                    if (pdf != null) {
                        readyTabunganSummaryPdf = pdf
                    } else {
                        Toast.makeText(
                            context,
                            if (isEn) "Failed to generate savings recap PDF." else "Gagal membuat PDF rekap tabungan.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                konselingRecords = konselingRecords,
                konselingLogs = konselingLogs,
                onOpenInputKonseling = { defaultEntryType, targetStudent ->
                    modalState.konselingRecordToEdit = null
                    modalState.inputKonselingDefaultEntryType = defaultEntryType
                    modalState.inputKonselingTargetStudent = targetStudent
                    modalState.showInputKonselingDialog = true
                },
                onOpenKonselingFollowUp = { modalState.showKonselingFollowUpSheet = true },
                onOpenKonselingStudentDetail = { student -> modalState.konselingStudentForDetail = student },
                onOpenKonselingHistory = { initialTab ->
                    modalState.konselingHistoryInitialTab = initialTab
                    modalState.showKonselingHistoryLogsSheet = true
                },
                onOpenKonselingGuide = { modalState.showKonselingGuideDialog = true },
                onOpenKonselingPdfSummary = {
                    val pdf = PdfReportGenerator.generateKonselingSummaryPdf(
                        context = context,
                        students = myFosterStudents,
                        records = konselingRecords,
                        waliName = waliName,
                        schoolProfile = schoolProfile
                    )
                    if (pdf != null) {
                        readyKonselingSummaryPdf = pdf
                    } else {
                        Toast.makeText(
                            context,
                            if (isEn) "Failed to generate confidential counseling PDF." else "Gagal membuat PDF rekap konseling rahasia.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }

    if (modalState.showCreateDialog) {
        CreatePlanDialog(
            students = students,
            primaryFaskesName = schoolProfile.primaryFaskesName,
            secondaryFaskesName = schoolProfile.secondaryFaskesName,
            allFaskesList = schoolProfile.getAllFaskes(),
            onDismiss = { modalState.showCreateDialog = false },
            onConfirm = { student, faskes, complaint, planDate, time, escort, notes ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.createTreatmentPlan(student, faskes, complaint, planDate, time, escort, notes)
                modalState.showCreateDialog = false
            }
        )
    }

    recordPlanToEdit?.let { editingRecord ->
        CreatePlanDialog(
            students = students,
            primaryFaskesName = schoolProfile.primaryFaskesName,
            secondaryFaskesName = schoolProfile.secondaryFaskesName,
            allFaskesList = schoolProfile.getAllFaskes(),
            existingRecord = editingRecord,
            onDismiss = { recordPlanToEdit = null },
            onConfirm = { student, faskes, complaint, planDate, time, escort, notes ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.editTreatmentPlan(
                    existingRecord = editingRecord,
                    student = student,
                    faskes = faskes,
                    complaint = complaint,
                    planDate = planDate,
                    departureTime = time,
                    escortName = escort,
                    notes = notes
                )
                recordPlanToEdit = null
            }
        )
    }

    recordToRefer?.let { record ->
        DepartReferralDialog(
            record = record,
            allFaskesList = schoolProfile.getAllFaskes(),
            defaultEscortName = record.escortName.ifBlank { waliName },
            onDismiss = { recordToRefer = null },
            onConfirmDeparture = { targetFaskes, escortName ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.markDepartedToFaskes(record.id, targetFaskes, escortName)
                recordToRefer = null
            },
            onDirectDiagnose = { targetFaskes, escortName ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.markDepartedToFaskes(record.id, targetFaskes, escortName)
                recordToDiagnose = record.copy(faskes = targetFaskes, escortName = escortName, status = "DEPARTED")
                recordToRefer = null
            }
        )
    }

    recordToDiagnose?.let { record ->
        InputTreatmentResultDialog(
            record = record,
            onDismiss = { recordToDiagnose = null },
            onConfirm = { diagnosis, actionsTaken, prescription, needsControl, controlDate, notes, isInpatient, inpatientRoom ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.submitTreatmentResult(
                    record = record,
                    diagnosis = diagnosis,
                    actionsTaken = actionsTaken,
                    prescription = prescription,
                    needsControl = needsControl,
                    controlDate = controlDate,
                    additionalNotes = notes,
                    isInpatient = isInpatient,
                    inpatientRoom = inpatientRoom
                )
                recordToDiagnose = null
            }
        )
    }

    if (modalState.showDirectTreatmentDialog) {
        DirectTreatmentDialog(
            students = students,
            primaryFaskesName = schoolProfile.primaryFaskesName,
            secondaryFaskesName = schoolProfile.secondaryFaskesName,
            allFaskesList = schoolProfile.getAllFaskes(),
            onDismiss = { modalState.showDirectTreatmentDialog = false },
            onConfirm = { student, faskes, complaint, diagnosis, actions, prescription, needsCtrl, ctrlDate, escort, notes, isInpatient, inpatientRoom ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.recordDirectTreatment(
                    student = student,
                    faskes = faskes,
                    complaint = complaint,
                    diagnosis = diagnosis,
                    actionsTaken = actions,
                    prescription = prescription,
                    needsControl = needsCtrl,
                    controlDate = ctrlDate,
                    escortName = escort,
                    notes = notes,
                    isInpatient = isInpatient,
                    inpatientRoom = inpatientRoom
                )
                modalState.showDirectTreatmentDialog = false
            }
        )
    }

    recordTreatmentToEdit?.let { editingRecord ->
        DirectTreatmentDialog(
            students = students,
            primaryFaskesName = schoolProfile.primaryFaskesName,
            secondaryFaskesName = schoolProfile.secondaryFaskesName,
            allFaskesList = schoolProfile.getAllFaskes(),
            existingRecord = editingRecord,
            onDismiss = { recordTreatmentToEdit = null },
            onConfirm = { student, faskes, complaint, diagnosis, actions, prescription, needsCtrl, ctrlDate, escort, notes, isInpatient, inpatientRoom ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.editCompletedTreatment(
                    existingRecord = editingRecord,
                    student = student,
                    faskes = faskes,
                    complaint = complaint,
                    diagnosis = diagnosis,
                    actionsTaken = actions,
                    prescription = prescription,
                    needsControl = needsCtrl,
                    controlDate = ctrlDate,
                    escortName = escort,
                    notes = notes,
                    isInpatient = isInpatient,
                    inpatientRoom = inpatientRoom
                )
                recordTreatmentToEdit = null
            }
        )
    }

    // Picker bila ada >1 anak rawat inap di faskes yang sama
    inpatientPickerForFaskes?.let { (faskesTitle, records) ->
        ModalBottomSheet(
            onDismissRequest = { inpatientPickerForFaskes = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isEn) "Currently Inpatient" else "Anak Sedang Rawat Inap",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEn) "$faskesTitle • Tap a student to open action card" else "$faskesTitle • Ketuk anak untuk membuka kartu tindakan",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                records.forEach { rec ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFDC2626).copy(alpha = 0.06f)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                inpatientPickerForFaskes = null
                                selectedInpatientRecordForDetail = rec
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rec.studentName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = buildString {
                                        append(rec.diagnosis.ifBlank { rec.complaint })
                                        if (rec.inpatientRoom.isNotBlank()) {
                                            append(if (isEn) " • Room ${rec.inpatientRoom}" else " • Ruang ${rec.inpatientRoom}")
                                        }
                                    },
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = if (isEn) "Open Action Card" else "Buka Kartu Tindakan",
                                tint = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }
    }

    // Kartu Detail Tindakan langsung saat notif Rawat Inap di Dashboard diklik
    selectedInpatientRecordForDetail?.let { record ->
        val student = students.find { it.id == record.studentId }
        val allStudentRecords = allRecords.filter { it.studentId == record.studentId }
            .sortedByDescending { it.treatmentDate ?: it.planDate }
        val calNow = Calendar.getInstance()
        val curM = calNow.get(Calendar.MONTH)
        val curY = calNow.get(Calendar.YEAR)
        val thisMonthCount = allStudentRecords.count { rec ->
            val cal = Calendar.getInstance().apply { timeInMillis = rec.treatmentDate ?: rec.planDate }
            cal.get(Calendar.MONTH) == curM && cal.get(Calendar.YEAR) == curY
        }

        MedicalRecordDetailSheet(
            record = record,
            student = student,
            allStudentRecords = allStudentRecords,
            thisMonthCount = thisMonthCount,
            onDismiss = { selectedInpatientRecordForDetail = null },
            onPrintPdf = {
                TopOnAdManager.onUserActionClick(context)
                student?.let { studentForPdfPrint = it }
            },
            onShare = {
                TopOnAdManager.onUserActionClick(context)
                selectedInpatientRecordForDetail = null
                recordToShare = record
            },
            onEdit = {
                TopOnAdManager.onUserActionClick(context)
                selectedInpatientRecordForDetail = null
                recordTreatmentToEdit = record
            },
            onDelete = {
                TopOnAdManager.onUserActionClick(context)
                viewModel.deleteRecord(record)
                selectedInpatientRecordForDetail = null
            },
            onDischargeInpatient = { updDiagnosis, updPrescription, needsCtrl, ctrlDate, dischargeNotes ->
                TopOnAdManager.onUserActionClick(context)
                viewModel.dischargeInpatient(
                    record = record,
                    updatedDiagnosis = updDiagnosis,
                    updatedPrescription = updPrescription,
                    needsControl = needsCtrl,
                    controlDate = ctrlDate,
                    dischargeNotes = dischargeNotes
                )
                Toast.makeText(
                    context,
                    if (isEn) "${record.studentName} marked as discharged" else "${record.studentName} ditandai sudah pulang rawat inap",
                    Toast.LENGTH_SHORT
                ).show()
                selectedInpatientRecordForDetail = null
            }
        )
    }

    recordToShare?.let { record ->
        ShareWhatsAppDialog(
            record = record,
            schoolProfile = schoolProfile,
            onDismiss = { recordToShare = null }
        )
    }

    studentForPdfPrint?.let { student ->
        PrintPdfDialog(
            student = student,
            allRecords = allRecords,
            waliName = waliName,
            schoolProfile = schoolProfile,
            onDismiss = { studentForPdfPrint = null }
        )
    }

    if (modalState.showPuskesmasInfoSheet) {
        FaskesReferralSheet(
            schoolProfile = schoolProfile,
            title = if (isEn) "Referral Faskes & Puskesmas" else "Faskes & Puskesmas Rujukan",
            onDismiss = { modalState.showPuskesmasInfoSheet = false }
        )
    }

    // Sinkronkan pesan & centang operan shift online setiap kali sheet pesan/riwayat dibuka
    LaunchedEffect(modalState.showShiftMessagesSheet, modalState.showShiftHistorySheet) {
        if (modalState.showShiftMessagesSheet || modalState.showShiftHistorySheet) {
            viewModel.syncShiftMessagesOnlineQuietly()
        }
    }

    // Modal Bottom Sheet: Pesan Shift Berikutnya (Daftar Kartu Operan Jaga)
    if (modalState.showShiftMessagesSheet) {
        ShiftMessagesSheet(
            viewModel = viewModel,
            messages = shiftMessages,
            waliAccounts = waliAccounts,
            onDismiss = { modalState.showShiftMessagesSheet = false }
        )
    }

    // Modal Bottom Sheet: Form Tinggalkan Pesan Shift
    if (modalState.showCreateShiftMessageDialog) {
        CreateShiftMessageDialog(
            viewModel = viewModel,
            waliAccounts = waliAccounts,
            onDismiss = { modalState.showCreateShiftMessageDialog = false }
        )
    }

    // Modal Bottom Sheet: Riwayat Operan (Arsip Pesan Lewat Hari Ini)
    if (modalState.showShiftHistorySheet) {
        ShiftHistorySheet(
            viewModel = viewModel,
            messages = shiftMessages,
            onDismiss = { modalState.showShiftHistorySheet = false }
        )
    }

    selectedTodayShiftMessage?.let { msgDetail ->
        ShiftMessageDetailSheet(
            message = msgDetail,
            onToggleComplete = {
                TopOnAdManager.onUserActionClick(context)
                viewModel.toggleShiftMessageCompleted(msgDetail)
            },
            onOpenAllMessages = {
                modalState.showShiftMessagesSheet = true
            },
            onDismiss = { selectedTodayShiftMessageId = null }
        )
    }

    var preselectedHaidStudent by remember { mutableStateOf<com.example.data.model.ChildStudent?>(null) }
    var preselectedTissueVerified by remember { mutableStateOf(false) }

    // Modal Bottom Sheet: Pendataan Haid Siswi (Scan Barcode Kartu Siswa)
    if (modalState.showInputHaidDialog) {
        InputHaidDialog(
            students = students,
            activeHaidRecords = activeHaidRecords,
            suciRecords = suciRecords,
            waliName = waliName,
            schoolId = schoolProfile.tenantCode,
            initialStudent = preselectedHaidStudent,
            initialTissueVerified = preselectedTissueVerified,
            onSaveHaid = { record ->
                viewModel.saveHaidRecord(record)
            },
            onCompleteHaid = { id ->
                viewModel.completeHaidRecord(id)
            },
            onDismiss = {
                modalState.showInputHaidDialog = false
                preselectedHaidStudent = null
                preselectedTissueVerified = false
            }
        )
    }

    // Modal Bottom Sheet: Daftar Siswi Sedang Haid
    if (modalState.showDaftarHaidSheet) {
        DaftarHaidSheet(
            activeHaidRecords = activeHaidRecords,
            schoolProfile = schoolProfile,
            waliName = waliName,
            onCompleteHaid = { id ->
                viewModel.completeHaidRecord(id)
            },
            onUpdateRecord = { rec ->
                viewModel.updateHaidRecord(rec)
            },
            onDeleteRecord = { rec ->
                viewModel.deleteHaidRecord(rec)
            },
            onOpenInputHaid = {
                modalState.showDaftarHaidSheet = false
                preselectedHaidStudent = null
                preselectedTissueVerified = false
                modalState.showInputHaidDialog = true
            },
            onDismiss = { modalState.showDaftarHaidSheet = false }
        )
    }

    // Modal Bottom Sheet: Daftar Siswi Suci & Timeline Masa Suci
    if (modalState.showDaftarSuciSheet) {
        DaftarSuciSheet(
            suciRecords = suciRecords,
            activeHaidRecords = activeHaidRecords,
            onDeleteRecord = { rec ->
                viewModel.deleteHaidRecord(rec)
            },
            onMoveToHaid = { rec ->
                val matchedStudent = students.firstOrNull {
                    (rec.studentId > 0L && it.id == rec.studentId) ||
                        (rec.studentNik.isNotBlank() && it.nik.equals(rec.studentNik, ignoreCase = true)) ||
                        (rec.studentNis.isNotBlank() && it.nis.equals(rec.studentNis, ignoreCase = true))
                } ?: com.example.data.model.ChildStudent(
                    id = rec.studentId,
                    name = rec.studentName,
                    nis = rec.studentNis,
                    gender = "P",
                    age = 15,
                    dormitory = rec.studentDorm,
                    classGrade = rec.studentClass,
                    nik = rec.studentNik,
                    schoolId = rec.schoolId
                )
                preselectedHaidStudent = matchedStudent
                preselectedTissueVerified = true
                modalState.showDaftarSuciSheet = false
                modalState.showInputHaidDialog = true
            },
            onDismiss = { modalState.showDaftarSuciSheet = false }
        )
    }

    // Modal Bottom Sheet: Panduan Aplikasi & Fiqih Haid Wanita
    if (modalState.showFiqihHaidDialog) {
        FiqihHaidDialog(
            onDismiss = { modalState.showFiqihHaidDialog = false }
        )
    }

    // Sinkronkan sesi & checklist sahur/berbuka puasa serta kunjungan online setiap kali modul dibuka
    LaunchedEffect(
        modalState.showInputPuasaDialog,
        modalState.showDaftarPuasaSheet,
        modalState.showChecklistPuasaSheet,
        modalState.showRankingPuasaSheet,
        modalState.showJadwalKunjunganSheet,
        modalState.showDaftarHadirKunjunganSheet,
        modalState.showLaporanKunjunganSheet,
        modalState.showSiswaPerKecamatanSheet,
        modalState.activeCategoryHub
    ) {
        if (modalState.showInputPuasaDialog || modalState.showDaftarPuasaSheet ||
            modalState.showChecklistPuasaSheet || modalState.showRankingPuasaSheet ||
            modalState.activeCategoryHub == DashboardCategoryHub.PUASA
        ) {
            viewModel.syncFastingOnlineQuietly()
        }
        if (modalState.activeCategoryHub == DashboardCategoryHub.PESAN) {
            viewModel.syncShiftMessagesOnlineQuietly()
        }
        if (modalState.showJadwalKunjunganSheet || modalState.showDaftarHadirKunjunganSheet ||
            modalState.showLaporanKunjunganSheet || modalState.showSiswaPerKecamatanSheet ||
            modalState.activeCategoryHub == DashboardCategoryHub.KUNJUNGAN
        ) {
            viewModel.syncVisitsOnlineQuietly()
        }
    }

    // Modal Bottom Sheet 1 (Puasa): Input Puasa (Sesi, Scan Barcode & Cari Siswa)
    if (modalState.showInputPuasaDialog) {
        InputPuasaDialog(
            sessions = fastingSessions,
            students = students,
            fastingRecords = fastingRecords,
            onRegisterStudent = { session, student, onResult ->
                viewModel.registerStudentForFasting(session, student, onResult)
            },
            onCreateSession = { title, dateMillis, description, onCreated ->
                viewModel.createFastingSession(
                    title = title,
                    sessionDate = dateMillis,
                    description = description,
                    onComplete = onCreated
                )
            },
            onDismiss = { modalState.showInputPuasaDialog = false }
        )
    }

    if (modalState.showCreatePuasaSessionDialog) {
        CreateFastingSessionDialog(
            onDismiss = { modalState.showCreatePuasaSessionDialog = false },
            onConfirm = { title, dateMillis, description ->
                viewModel.createFastingSession(
                    title = title,
                    sessionDate = dateMillis,
                    description = description
                ) { created ->
                    if (created != null) {
                        Toast.makeText(
                            context,
                            if (isEn) {
                                "Puasa session \"${created.title}\" created!"
                            } else {
                                "Sesi puasa \"${created.title}\" berhasil dibuat!"
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            if (isEn) {
                                "Please log in first to create a Puasa session."
                            } else {
                                "Silakan login terlebih dahulu untuk membuat sesi puasa."
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                modalState.showCreatePuasaSessionDialog = false
            }
        )
    }

    // Modal Bottom Sheet 2 (Puasa): Daftar Siswa Puasa (SD/SMP/SMA + Total & PDF)
    if (modalState.showDaftarPuasaSheet) {
        DaftarPuasaSheet(
            sessions = fastingSessions,
            fastingRecords = fastingRecords,
            schoolProfile = schoolProfile,
            waliName = waliName,
            onDeleteRecord = { rec ->
                viewModel.deleteFastingRecord(rec)
            },
            onDismiss = { modalState.showDaftarPuasaSheet = false }
        )
    }

    // Modal Bottom Sheet 3 (Puasa): Checklist Sahur & Berbuka + PDF
    if (modalState.showChecklistPuasaSheet) {
        ChecklistPuasaSheet(
            sessions = fastingSessions,
            fastingRecords = fastingRecords,
            schoolProfile = schoolProfile,
            waliName = waliName,
            onToggleSahur = { rec ->
                viewModel.toggleFastingSahur(rec)
            },
            onToggleBuka = { rec ->
                viewModel.toggleFastingBuka(rec)
            },
            onDismiss = { modalState.showChecklistPuasaSheet = false }
        )
    }

    // Modal Bottom Sheet 4 (Puasa): Ranking Siswa & History Sesi Puasa
    if (modalState.showRankingPuasaSheet) {
        RankingPuasaSheet(
            sessions = fastingSessions,
            fastingRecords = fastingRecords,
            onDismiss = { modalState.showRankingPuasaSheet = false }
        )
    }

    // Modal Bottom Sheet 1 (Mengaji): Input Mengaji Berkelanjutan
    if (modalState.showInputMengajiDialog) {
        InputMengajiDialog(
            students = students,
            mengajiRecords = mengajiRecords,
            initialStudent = modalState.mengajiTargetStudent,
            recordToEdit = modalState.mengajiRecordToEdit,
            onSave = { student, dateMillis, bookType, jilidOrJuz, pageOrVerse, fluencyStatus, notes, existingId ->
                viewModel.saveMengajiRecord(
                    student = student,
                    sessionDate = dateMillis,
                    bookType = bookType,
                    jilidOrJuz = jilidOrJuz,
                    pageOrVerse = pageOrVerse,
                    fluencyStatus = fluencyStatus,
                    notes = notes,
                    existingRecordId = existingId
                ) { _, _ ->
                    Toast.makeText(
                        context,
                        if (isEn) {
                            "Mengaji saved for ${student.name}: $jilidOrJuz • $pageOrVerse"
                        } else {
                            "Setoran mengaji ${student.name} tersimpan: $jilidOrJuz • $pageOrVerse"
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
                modalState.showInputMengajiDialog = false
                modalState.mengajiTargetStudent = null
                modalState.mengajiRecordToEdit = null
            },
            onDismiss = {
                modalState.showInputMengajiDialog = false
                modalState.mengajiTargetStudent = null
                modalState.mengajiRecordToEdit = null
            }
        )
    }

    // Modal Bottom Sheet 2 (Mengaji): Cetak Kartu Mengaji Bulanan & PDF
    if (modalState.showKartuMengajiSheet) {
        KartuMengajiSheet(
            students = students,
            mengajiRecords = mengajiRecords,
            schoolProfile = schoolProfile,
            waliName = waliName,
            initialStudent = modalState.mengajiTargetStudent,
            onOpenInputForStudent = { st ->
                modalState.showKartuMengajiSheet = false
                modalState.mengajiTargetStudent = st
                modalState.mengajiRecordToEdit = null
                modalState.showInputMengajiDialog = true
            },
            onDismiss = {
                modalState.showKartuMengajiSheet = false
                modalState.mengajiTargetStudent = null
            }
        )
    }

    // Modal Bottom Sheet 3 (Mengaji): Riwayat Harian & Rekap Jilid Anak
    if (modalState.showRiwayatMengajiSheet) {
        RiwayatMengajiSheet(
            students = students,
            mengajiRecords = mengajiRecords,
            initialTab = modalState.riwayatMengajiInitialTab,
            onContinueStudent = { st ->
                modalState.showRiwayatMengajiSheet = false
                modalState.mengajiTargetStudent = st
                modalState.mengajiRecordToEdit = null
                modalState.showInputMengajiDialog = true
            },
            onOpenKartuForStudent = { st ->
                modalState.showRiwayatMengajiSheet = false
                modalState.mengajiTargetStudent = st
                modalState.showKartuMengajiSheet = true
            },
            onEditRecord = { rec ->
                modalState.showRiwayatMengajiSheet = false
                modalState.mengajiRecordToEdit = rec
                modalState.mengajiTargetStudent = null
                modalState.showInputMengajiDialog = true
            },
            onDeleteRecord = { rec ->
                viewModel.deleteMengajiRecord(rec)
                Toast.makeText(
                    context,
                    if (isEn) "Mengaji record deleted." else "Catatan mengaji dihapus.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = { modalState.showRiwayatMengajiSheet = false }
        )
    }

    // Modal Kunjungan 1: Jadwal Kunjungan Bulanan & Multi-Tanggal
    if (modalState.showJadwalKunjunganSheet) {
        JadwalKunjunganSheet(
            sessions = visitSessions,
            students = students,
            visitRecords = visitRecords,
            isAdmin = isAdminUser,
            onOpenDaftarHadir = {
                modalState.daftarHadirKunjunganInitialTab = 0
                modalState.showDaftarHadirKunjunganSheet = true
            },
            onOpenLaporan = { modalState.showLaporanKunjunganSheet = true },
            onEditSession = if (isAdminUser) { sess ->
                modalState.visitSessionToEdit = sess
                modalState.showCreateVisitSessionDialog = true
            } else null,
            onCreateSession = if (isAdminUser) {
                {
                    modalState.visitSessionToEdit = null
                    modalState.showCreateVisitSessionDialog = true
                }
            } else null,
            onDismiss = { modalState.showJadwalKunjunganSheet = false }
        )
    }

    // Modal Kunjungan 2: Daftar Hadir (Scan Barcode & List Cetak PDF Manual)
    if (modalState.showDaftarHadirKunjunganSheet) {
        DaftarHadirKunjunganSheet(
            sessions = visitSessions,
            students = students,
            visitRecords = visitRecords,
            schoolProfile = schoolProfile,
            waliName = waliName,
            initialTab = modalState.daftarHadirKunjunganInitialTab,
            onRecordVisitCheckIn = { sess, st, visitDate, relation, vName, method, onResult ->
                viewModel.recordStudentVisitCheckIn(
                    session = sess,
                    student = st,
                    visitDate = visitDate,
                    visitorRelation = relation,
                    visitorName = vName,
                    checkInMethod = method,
                    onResult = onResult
                )
            },
            onDeleteVisitRecord = { rec ->
                viewModel.deleteVisitRecord(rec)
                Toast.makeText(
                    context,
                    if (isEn) "Visit check-in removed." else "Data kehadiran kunjungan dihapus.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = { modalState.showDaftarHadirKunjunganSheet = false }
        )
    }

    // Modal Kunjungan 3: Laporan Kehadiran (Sudah Hadir vs Belum Berkunjung)
    if (modalState.showLaporanKunjunganSheet) {
        LaporanKunjunganSheet(
            sessions = visitSessions,
            students = students,
            visitRecords = visitRecords,
            schoolProfile = schoolProfile,
            waliName = waliName,
            onQuickMarkManual = { sess, st, targetDate ->
                viewModel.recordStudentVisitCheckIn(
                    session = sess,
                    student = st,
                    visitDate = targetDate,
                    visitorRelation = "Orang Tua / Wali",
                    checkInMethod = com.example.data.model.VisitRecord.METHOD_MANUAL
                ) { _, msg, _ ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { modalState.showLaporanKunjunganSheet = false }
        )
    }

    // Modal Kunjungan 4: Daftar Siswa per Kecamatan (Dinamis sesuai data siswa sekolah)
    if (modalState.showSiswaPerKecamatanSheet) {
        SiswaPerKecamatanSheet(
            students = students,
            visitSessions = visitSessions,
            onStudentClick = { st -> modalState.visitStudentForDetail = st },
            onDismiss = { modalState.showSiswaPerKecamatanSheet = false }
        )
    }

    if (modalState.showCreateVisitSessionDialog && isAdminUser) {
        CreateVisitSessionDialog(
            students = students,
            initialSession = modalState.visitSessionToEdit,
            onDismiss = {
                modalState.showCreateVisitSessionDialog = false
                modalState.visitSessionToEdit = null
            },
            onConfirm = { title, monthYear, dates, districts, desc ->
                val editing = modalState.visitSessionToEdit
                if (editing != null) {
                    viewModel.updateVisitSession(editing, title, monthYear, dates, districts, desc) { updated ->
                        if (updated != null) {
                            Toast.makeText(
                                context,
                                if (isEn) "Visit session updated!" else "Sesi kunjungan diperbarui!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    viewModel.createVisitSession(title, monthYear, dates, districts, desc) { created ->
                        if (created != null) {
                            Toast.makeText(
                                context,
                                if (isEn) "Visit session created!" else "Sesi kunjungan \"${created.title}\" dibuat!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                modalState.showCreateVisitSessionDialog = false
                modalState.visitSessionToEdit = null
            }
        )
    }

    modalState.visitStudentForDetail?.let { detailStudent ->
        val stRecords = allRecords.filter { it.studentId == detailStudent.id }
        StudentDetailDialog(
            student = detailStudent,
            studentRecords = stRecords,
            schoolProfile = schoolProfile,
            waliName = waliName,
            mengajiRecords = mengajiRecords,
            fastingRecords = fastingRecords,
            onDeleteMengajiRecord = { rec -> viewModel.deleteMengajiRecord(rec) },
            onDeleteMedicalRecord = { rec -> viewModel.deleteRecord(rec) },
            onDeleteFastingRecord = { rec -> viewModel.deleteFastingRecord(rec) },
            onDismiss = { modalState.visitStudentForDetail = null }
        )
    }

    // Modal Hubungan Orang Tua 1: Pilih Anak Asuh (Klaim Eksklusif & Lepas oleh Admin / Wali Bersangkutan)
    if (modalState.showPilihAnakAsuhSheet) {
        PilihAnakAsuhSheet(
            students = students,
            currentWaliDisplayName = waliName,
            isStudentClaimedByCurrentWali = { viewModel.isStudentClaimedByCurrentWali(it) },
            isStudentClaimedByOtherWali = { viewModel.isStudentClaimedByOtherWali(it) },
            canReleaseStudent = { viewModel.canCurrentUserReleaseStudent(it) },
            onClaimStudents = { selectedList ->
                viewModel.claimStudentsForCurrentWali(selectedList) { claimed, skipped ->
                    val msg = if (isEn) {
                        "Claimed $claimed student(s)" + if (skipped > 0) ", $skipped already assigned." else "!"
                    } else {
                        "$claimed anak asuh dipilih & kartu akses dibuat!" + if (skipped > 0) " ($skipped sudah dibina wali lain)" else ""
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onReleaseStudent = { st ->
                viewModel.releaseStudentClaim(st) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { modalState.showPilihAnakAsuhSheet = false }
        )
    }

    // Modal Hubungan Orang Tua 2: Kartu Akses Orang Tua Otomatis + Share WA / Biasa
    if (modalState.showKartuAksesOrtuSheet) {
        KartuAksesOrangTuaSheet(
            students = students,
            parentAccessLinks = parentAccessLinks,
            schoolProfile = schoolProfile,
            isStudentClaimedByCurrentWali = { viewModel.isStudentClaimedByCurrentWali(it) },
            isAdmin = isAdminUser,
            onOpenPilihAnakAsuh = {
                modalState.showKartuAksesOrtuSheet = false
                modalState.showPilihAnakAsuhSheet = true
            },
            onGenerateAccessCard = { st ->
                viewModel.ensureParentAccessLinkForStudent(st) {
                    Toast.makeText(
                        context,
                        if (isEn) "Parent access card ready for ${st.name}!" else "Kartu akses orang tua untuk ${st.name} siap!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onResetPassword = { link ->
                viewModel.resetParentAccessPassword(link) { newPass ->
                    Toast.makeText(
                        context,
                        if (isEn) "New password generated: $newPass" else "Password baru dibuat: $newPass",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onEditStudentProfile = { st ->
                modalState.studentToEditParentPhone = st
            },
            onDismiss = { modalState.showKartuAksesOrtuSheet = false }
        )
    }

    // Modal Hubungan Orang Tua 3: Pilihan Akses (Profil, Mengaji, Sakit, Puasa, Haid)
    if (modalState.showPilihanAksesOrtuSheet) {
        PilihanAksesOrangTuaSheet(
            students = students,
            parentAccessLinks = parentAccessLinks,
            isStudentClaimedByCurrentWali = { viewModel.isStudentClaimedByCurrentWali(it) },
            isAdmin = isAdminUser,
            onOpenPilihAnakAsuh = {
                modalState.showPilihanAksesOrtuSheet = false
                modalState.showPilihAnakAsuhSheet = true
            },
            onEnsureAccessLink = { st ->
                viewModel.ensureParentAccessLinkForStudent(st)
            },
            onUpdatePermissions = { link, canMengaji, canMedical, canFasting, canHaid, canProfile, canTabungan ->
                viewModel.updateParentAccessPermissions(
                    link = link,
                    canMengaji = canMengaji,
                    canMedical = canMedical,
                    canFasting = canFasting,
                    canHaid = canHaid,
                    canProfile = canProfile,
                    canTabungan = canTabungan
                )
            },
            onDismiss = { modalState.showPilihanAksesOrtuSheet = false }
        )
    }

    // Modal Hubungan Orang Tua 4: Pesan Chat 2 Arah dengan Orang Tua
    if (modalState.showPesanOrtuSheet) {
        PesanOrangTuaSheet(
            students = students,
            parentMessages = parentMessages,
            isStudentClaimedByCurrentWali = { viewModel.isStudentClaimedByCurrentWali(it) },
            isAdmin = isAdminUser,
            initialSelectedStudent = modalState.selectedParentChatStudent,
            onOpenPilihAnakAsuh = {
                modalState.showPesanOrtuSheet = false
                modalState.showPilihAnakAsuhSheet = true
            },
            onSendMessage = { st, content ->
                viewModel.sendParentMessage(st, content, "WALI")
            },
            onMarkRead = { stId ->
                viewModel.markParentMessagesAsRead(stId, "WALI")
            },
            onDismiss = {
                modalState.showPesanOrtuSheet = false
                modalState.selectedParentChatStudent = null
            }
        )
    }

    // Dialog Edit Profil / Tambah No. HP Orang Tua langsung dari Kartu Akses Orang Tua
    modalState.studentToEditParentPhone?.let { editingStudent ->
        AddStudentDialog(
            initialStudent = editingStudent,
            activeSchoolId = schoolProfile.tenantCode.ifBlank { "SRT1-KDR" },
            onDismiss = { modalState.studentToEditParentPhone = null },
            onConfirmStudent = { updated ->
                viewModel.updateStudent(updated) {
                    Toast.makeText(
                        context,
                        if (isEn) "Profile & parent phone updated!" else "Profil & No. HP orang tua diperbarui!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                modalState.studentToEditParentPhone = null
            }
        )
    }

    // Modal Bottom Sheet: Atur Tata Letak Ikon Dashboard (Khusus Anak Superadmin + Sinkron PocketBase)
    if (showLayoutEditorSheet && isAnakSuperAdmin) {
        DashboardLayoutEditorSheet(
            currentOrder = dashboardIconOrder,
            isSaving = isSavingDashboardLayout,
            onDismiss = { showLayoutEditorSheet = false },
            onSave = { newOrder ->
                viewModel.saveDashboardIconOrder(newOrder) { success, message ->
                    Toast.makeText(
                        context,
                        message,
                        if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                    ).show()
                    if (success) {
                        showLayoutEditorSheet = false
                    }
                }
            },
            onResetDefault = {
                viewModel.resetDashboardIconOrder { success, message ->
                    Toast.makeText(
                        context,
                        message,
                        if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    // Modal Bottom Sheet: Atur Banner Slider Global + Cloudflare R2 CDN (Khusus Anak Superadmin)
    if (showBannerEditorSheet && isAnakSuperAdmin) {
        DashboardBannerEditorSheet(
            currentConfig = dashboardBannerConfig,
            isSaving = isSavingDashboardBanner,
            isUploadingImage = isUploadingBannerImage,
            onUploadImage = { uri, bannerId, title, onResult ->
                viewModel.uploadBannerImageToCloud(uri, bannerId, title, onResult)
            },
            onDismiss = { showBannerEditorSheet = false },
            onSave = { newConfig ->
                viewModel.saveDashboardBannerConfig(newConfig) { success, message ->
                    Toast.makeText(
                        context,
                        message,
                        if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                    ).show()
                    if (success) {
                        showBannerEditorSheet = false
                    }
                }
            }
        )
    }

    // Modal Tabungan 1: Input / Edit Titip Masuk (+) / Pengeluaran (-)
    if (modalState.showInputTabunganDialog) {
        InputTabunganDialog(
            claimedStudents = myFosterStudents,
            allStudents = students,
            transactions = tabunganTransactions,
            defaultType = modalState.inputTabunganDefaultType,
            initialStudent = modalState.inputTabunganTargetStudent,
            txToEdit = modalState.tabunganTxToEdit,
            isAnakSuperAdmin = isAnakSuperAdmin,
            onOpenPilihAnakAsuh = { modalState.showPilihAnakAsuhSheet = true },
            onSave = { targetStudent, txType, amount, title, notes, transactionDate, existingTx ->
                if (existingTx != null) {
                    viewModel.editTabunganTransaction(
                        existingTx = existingTx,
                        transactionType = txType,
                        amount = amount,
                        title = title,
                        notes = notes,
                        transactionDate = transactionDate
                    ) { ok, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (ok) {
                            modalState.showInputTabunganDialog = false
                            modalState.inputTabunganTargetStudent = null
                            modalState.tabunganTxToEdit = null
                        }
                    }
                } else {
                    viewModel.saveTabunganTransaction(
                        student = targetStudent,
                        transactionType = txType,
                        amount = amount,
                        title = title,
                        notes = notes,
                        transactionDate = transactionDate
                    ) { ok, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (ok) {
                            modalState.showInputTabunganDialog = false
                            modalState.inputTabunganTargetStudent = null
                            modalState.tabunganTxToEdit = null
                        }
                    }
                }
            },
            onDismiss = {
                modalState.showInputTabunganDialog = false
                modalState.inputTabunganTargetStudent = null
                modalState.tabunganTxToEdit = null
            }
        )
    }

    // Modal Tabungan 2: Daftar Saldo Per Anak
    if (modalState.showTabunganStudentListSheet) {
        TabunganStudentListSheet(
            claimedStudents = myFosterStudents,
            allStudents = students,
            transactions = tabunganTransactions,
            isAnakSuperAdmin = isAnakSuperAdmin,
            onSelectStudent = { selectedSt ->
                modalState.showTabunganStudentListSheet = false
                modalState.tabunganStudentForDetail = selectedSt
            },
            onQuickDeposit = { selectedSt ->
                modalState.tabunganTxToEdit = null
                modalState.inputTabunganDefaultType = TabunganHelper.TYPE_IN
                modalState.inputTabunganTargetStudent = selectedSt
                modalState.showInputTabunganDialog = true
            },
            onOpenPilihAnakAsuh = { modalState.showPilihAnakAsuhSheet = true },
            onDismiss = { modalState.showTabunganStudentListSheet = false }
        )
    }

    // Modal Tabungan 3: Pop-Up Detail Tabungan Anak (Pemasukan, Pengeluaran, Ubah, Batalkan, Hapus & Log)
    modalState.tabunganStudentForDetail?.let { detailStudent ->
        val studentTxs = remember(tabunganTransactions, detailStudent) {
            tabunganTransactions.filter { TabunganHelper.matchesStudent(it, detailStudent) }
        }
        val studentTxIds = remember(studentTxs) { studentTxs.map { it.id }.toSet() }
        val studentLogs = remember(tabunganLogs, detailStudent, studentTxIds) {
            tabunganLogs.filter { log ->
                (detailStudent.id > 0L && log.studentId == detailStudent.id) ||
                    (log.transactionId > 0L && log.transactionId in studentTxIds) ||
                    (log.studentId <= 0L && log.studentName.equals(detailStudent.name, ignoreCase = true))
            }
        }
        TabunganStudentDetailSheet(
            student = detailStudent,
            studentTransactions = studentTxs,
            studentLogs = studentLogs,
            waliName = waliName,
            schoolProfile = schoolProfile,
            onQuickInput = { type ->
                modalState.tabunganTxToEdit = null
                modalState.inputTabunganDefaultType = type
                modalState.inputTabunganTargetStudent = detailStudent
                modalState.showInputTabunganDialog = true
            },
            onEditTransaction = { tx ->
                modalState.tabunganTxToEdit = tx
                modalState.inputTabunganDefaultType = tx.transactionType
                modalState.inputTabunganTargetStudent = detailStudent
                modalState.showInputTabunganDialog = true
            },
            onCancelTransaction = { tx, reason ->
                viewModel.cancelTabunganTransaction(tx, reason) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteTransaction = { tx ->
                viewModel.deleteTabunganTransaction(tx) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { modalState.tabunganStudentForDetail = null }
        )
    }

    // Modal Tabungan 4: Riwayat Transaksi & Log Audit Pribadi
    if (modalState.showTabunganHistoryLogsSheet) {
        val summaryStudents = remember(myFosterStudents, students, tabunganTransactions) {
            TabunganHelper.buildSummaryStudents(
                myFosterStudents = myFosterStudents,
                allStudents = students,
                transactions = tabunganTransactions
            )
        }
        TabunganHistoryLogsSheet(
            transactions = tabunganTransactions,
            logs = tabunganLogs,
            studentsForSummary = summaryStudents,
            waliName = waliName,
            schoolProfile = schoolProfile,
            initialTab = modalState.tabunganHistoryInitialTab,
            onOpenStudentById = { studentId ->
                val target = TabunganHelper.resolveStudentByIdOrFallback(
                    studentId = studentId,
                    myFosterStudents = myFosterStudents,
                    allStudents = students,
                    transactions = tabunganTransactions,
                    logs = tabunganLogs
                )
                if (target != null) {
                    modalState.showTabunganHistoryLogsSheet = false
                    modalState.tabunganStudentForDetail = target
                }
            },
            onEditTransaction = { tx ->
                val target = TabunganHelper.resolveStudentByIdOrFallback(
                    studentId = tx.studentId,
                    myFosterStudents = myFosterStudents,
                    allStudents = students,
                    transactions = tabunganTransactions,
                    logs = tabunganLogs
                )
                modalState.tabunganTxToEdit = tx
                modalState.inputTabunganDefaultType = tx.transactionType
                modalState.inputTabunganTargetStudent = target
                modalState.showInputTabunganDialog = true
            },
            onCancelTransaction = { tx, reason ->
                viewModel.cancelTabunganTransaction(tx, reason) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteTransaction = { tx ->
                viewModel.deleteTabunganTransaction(tx) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { modalState.showTabunganHistoryLogsSheet = false }
        )
    }

    readyTabunganSummaryPdf?.let { file ->
        AlertDialog(
            onDismissRequest = { readyTabunganSummaryPdf = null },
            title = { Text(if (isEn) "Savings Recap PDF Ready" else "PDF Rekap Tabungan Siap") },
            text = {
                Text(
                    if (isEn) "Choose to open the private savings recap PDF or share it."
                    else "Pilih untuk membuka atau membagikan laporan PDF saldo tabungan anak binaan."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        readyTabunganSummaryPdf = null
                        PdfReportGenerator.openPdfFile(context, file)
                    }
                ) {
                    Text(if (isEn) "Open PDF" else "Buka PDF")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        readyTabunganSummaryPdf = null
                        PdfReportGenerator.shareFastingPdf(
                            context = context,
                            pdfFile = file,
                            subject = if (isEn) "Private Savings Recap" else "Rekap Tabungan Anak Binaan",
                            text = if (isEn) "Attached is the private student savings balance recap PDF." else "Terlampir rekap saldo tabungan pribadi wali asuh."
                        )
                    }
                ) {
                    Text(if (isEn) "Share PDF" else "Bagikan")
                }
            }
        )
    }

    // =====================================================================
    // MODUL KONSELING RAHASIA WALI ASUH (HANYA DILIHAT WALI BERSANGKUTAN)
    // =====================================================================
    if (modalState.showInputKonselingDialog) {
        InputKonselingDialog(
            claimedStudents = myFosterStudents,
            defaultEntryType = modalState.inputKonselingDefaultEntryType,
            initialStudent = modalState.inputKonselingTargetStudent,
            recordToEdit = modalState.konselingRecordToEdit,
            onOpenPilihAnakAsuh = {
                modalState.showInputKonselingDialog = false
                modalState.showPilihAnakAsuhSheet = true
            },
            onSave = { student, entryType, topic, urgencyLevel, moodLevel, title, summary, intervention, followUpPlan, followUpDate, sessionDate, markCompleted, existingRecord ->
                TopOnAdManager.onUserActionClick(context)
                if (existingRecord == null) {
                    viewModel.saveKonselingRecord(
                        student = student,
                        entryType = entryType,
                        topic = topic,
                        urgencyLevel = urgencyLevel,
                        moodLevel = moodLevel,
                        title = title,
                        summary = summary,
                        intervention = intervention,
                        followUpPlan = followUpPlan,
                        followUpDate = followUpDate,
                        sessionDate = sessionDate,
                        markCompleted = markCompleted
                    ) { ok, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (ok) {
                            modalState.showInputKonselingDialog = false
                            modalState.konselingRecordToEdit = null
                            modalState.inputKonselingTargetStudent = null
                        }
                    }
                } else {
                    viewModel.editKonselingRecord(
                        existingRecord = existingRecord,
                        entryType = entryType,
                        topic = topic,
                        urgencyLevel = urgencyLevel,
                        moodLevel = moodLevel,
                        title = title,
                        summary = summary,
                        intervention = intervention,
                        followUpPlan = followUpPlan,
                        followUpDate = followUpDate,
                        sessionDate = sessionDate,
                        markCompleted = markCompleted
                    ) { ok, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (ok) {
                            modalState.showInputKonselingDialog = false
                            modalState.konselingRecordToEdit = null
                            modalState.inputKonselingTargetStudent = null
                        }
                    }
                }
            },
            onDismiss = {
                modalState.showInputKonselingDialog = false
                modalState.konselingRecordToEdit = null
                modalState.inputKonselingTargetStudent = null
            }
        )
    }

    modalState.konselingStudentForDetail?.let { detailStudent ->
        val studentRecs = remember(konselingRecords, detailStudent) {
            konselingRecords.filter { KonselingHelper.matchesStudent(it, detailStudent) }
        }
        val studentLogItems = remember(konselingLogs, detailStudent) {
            konselingLogs.filter {
                it.studentId == detailStudent.id ||
                    it.studentName.equals(detailStudent.name, ignoreCase = true)
            }
        }
        KonselingStudentDetailSheet(
            student = detailStudent,
            studentRecords = studentRecs,
            studentLogs = studentLogItems,
            waliName = waliName,
            schoolProfile = schoolProfile,
            onQuickInput = { entryType ->
                modalState.konselingRecordToEdit = null
                modalState.inputKonselingDefaultEntryType = entryType
                modalState.inputKonselingTargetStudent = detailStudent
                modalState.showInputKonselingDialog = true
            },
            onEditRecord = { rec ->
                modalState.konselingRecordToEdit = rec
                modalState.inputKonselingDefaultEntryType = rec.entryType
                modalState.inputKonselingTargetStudent = detailStudent
                modalState.showInputKonselingDialog = true
            },
            onToggleStatus = { rec ->
                viewModel.toggleKonselingStatus(rec) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteRecord = { rec ->
                viewModel.deleteKonselingRecord(rec) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { modalState.konselingStudentForDetail = null }
        )
    }

    if (modalState.showKonselingFollowUpSheet) {
        KonselingFollowUpSheet(
            records = konselingRecords,
            onOpenStudentById = { studentId ->
                val target = KonselingHelper.resolveStudentByIdOrFallback(
                    studentId = studentId,
                    myFosterStudents = myFosterStudents,
                    records = konselingRecords,
                    logs = konselingLogs
                )
                if (target != null) {
                    modalState.showKonselingFollowUpSheet = false
                    modalState.konselingStudentForDetail = target
                }
            },
            onToggleStatus = { rec ->
                viewModel.toggleKonselingStatus(rec) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { modalState.showKonselingFollowUpSheet = false }
        )
    }

    if (modalState.showKonselingHistoryLogsSheet) {
        KonselingHistoryLogsSheet(
            records = konselingRecords,
            logs = konselingLogs,
            studentsForSummary = myFosterStudents,
            waliName = waliName,
            schoolProfile = schoolProfile,
            initialTab = modalState.konselingHistoryInitialTab,
            onOpenStudentById = { studentId ->
                val target = KonselingHelper.resolveStudentByIdOrFallback(
                    studentId = studentId,
                    myFosterStudents = myFosterStudents,
                    records = konselingRecords,
                    logs = konselingLogs
                )
                if (target != null) {
                    modalState.showKonselingHistoryLogsSheet = false
                    modalState.konselingStudentForDetail = target
                }
            },
            onEditRecord = { rec ->
                val target = KonselingHelper.resolveStudentByIdOrFallback(
                    studentId = rec.studentId,
                    myFosterStudents = myFosterStudents,
                    records = konselingRecords,
                    logs = konselingLogs
                )
                modalState.konselingRecordToEdit = rec
                modalState.inputKonselingDefaultEntryType = rec.entryType
                modalState.inputKonselingTargetStudent = target
                modalState.showInputKonselingDialog = true
            },
            onToggleStatus = { rec ->
                viewModel.toggleKonselingStatus(rec) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteRecord = { rec ->
                viewModel.deleteKonselingRecord(rec) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { modalState.showKonselingHistoryLogsSheet = false }
        )
    }

    if (modalState.showKonselingGuideDialog) {
        KonselingGuideDialog(
            onDismiss = { modalState.showKonselingGuideDialog = false }
        )
    }

    readyKonselingSummaryPdf?.let { file ->
        AlertDialog(
            onDismissRequest = { readyKonselingSummaryPdf = null },
            title = { Text(if (isEn) "Confidential Counseling PDF Ready" else "PDF Rekap Konseling Rahasia Siap") },
            text = {
                Text(
                    if (isEn) {
                        "Strictly confidential for your Wali Asuh account only. Choose to open or share carefully."
                    } else {
                        "Dokumen bersifat RAHASIA dan hanya untuk Wali Asuh bersangkutan. Pilih untuk membuka atau membagikan secara terbatas."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        readyKonselingSummaryPdf = null
                        PdfReportGenerator.openPdfFile(context, file)
                    }
                ) {
                    Text(if (isEn) "Open PDF" else "Buka PDF")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        readyKonselingSummaryPdf = null
                        PdfReportGenerator.shareFastingPdf(
                            context = context,
                            pdfFile = file,
                            subject = if (isEn) "Confidential Counseling Recap" else "Rekap Konseling Rahasia Wali Asuh",
                            text = if (isEn) "Attached is the confidential foster student counseling recap PDF." else "Terlampir rekap konseling rahasia anak binaan Wali Asuh."
                        )
                    }
                ) {
                    Text(if (isEn) "Share PDF" else "Bagikan")
                }
            }
        )
    }
}

@Composable
fun DashboardSickChildItem(
    number: Int,
    record: MedicalRecord,
    onTreatHere: () -> Unit,
    onDepart: () -> Unit,
    onDiagnose: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    val accentColor = if (record.faskes.contains("Wates", ignoreCase = true)) Color(0xFF00695C) else Color(0xFF2E7D32)
    val isToday = DateUtils.isToday(record.planDate)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sick_child_item_${record.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left Accent Color Stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Baris 1: Nama + Asrama di kiri, Status + Edit/Hapus di kanan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$number. ${record.studentName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = record.studentDormitory,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        StatusBadge(status = record.status)
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("btn_edit_plan_${record.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = if (isEn) "Edit Plan" else "Edit Rencana",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = if (isEn) "Delete" else "Hapus",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Baris 2: Strip Jadwal & Tujuan Faskes sejajar (1 baris ringkas tanpa pengulangan tanggal)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f)
                            else accentColor.copy(alpha = 0.08f)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = if (isEn) "Faskes Schedule" else "Jadwal ke Faskes",
                            tint = accentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        val scheduleLabel = buildString {
                            append(DateUtils.getRelativeDateLabel(record.planDate))
                            if (record.departureTime.isNotBlank()) {
                                append(" • ${record.departureTime} WIB")
                            }
                        }
                        Text(
                            text = scheduleLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    FaskesBadge(faskes = record.faskes)
                }

                // Baris 3: Keluhan
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                append(if (isEn) "Complaint: " else "Keluhan: ")
                            }
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                append(record.complaint.ifBlank { "-" })
                            }
                        },
                        fontSize = 11.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Baris 4: Info Pendamping & Penginput sejajar rapi (tidak terjepit tombol)
                val escortLabel = record.escortName.trim()
                val creatorLabel = record.createdBy.ifBlank { escortLabel.ifBlank { "-" } }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (escortLabel.isNotBlank()) "👤 $escortLabel" else "",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Input: $creatorLabel",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Baris 5: Tombol Aksi seimbang memanfaatkan ruang penuh
                if (record.status == "PLANNED" || record.status == "MENUNGGU" || record.status == "SIAP JALAN") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onTreatHere,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("btn_treat_here_${record.id}"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Healing,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEn) "Treat Here" else "Dirawat di Sini",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = onDepart,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusDeparted),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("btn_depart_${record.id}"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEn) "Depart" else "Berangkatkan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else if (record.status == "DEPARTED" || record.status == "DIANTER") {
                    Button(
                        onClick = onDiagnose,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("btn_input_result_${record.id}"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Log Diagnosis" else "Input Diagnosa", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
