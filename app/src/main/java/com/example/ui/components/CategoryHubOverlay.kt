package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChildStudent
import com.example.data.model.VisitSessionSwap
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.FastingRecord
import com.example.data.model.StudentReligionHelper
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.KonselingHelper
import com.example.data.model.KonselingLog
import com.example.data.model.KonselingRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentExpectation
import com.example.data.model.ParentMessage
import com.example.data.model.SchoolProfile
import com.example.data.model.ShiftMessage
import com.example.data.model.SholatHelper
import com.example.data.model.SholatRecord
import com.example.data.model.SholatStatus
import com.example.data.model.SholatWaktu
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganLog
import com.example.data.model.TabunganTransaction
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.WaliScheduleEntry
import com.example.data.model.WaliScheduleHelper
import com.example.data.model.WaliShiftCode
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings

@Composable
fun CategoryHubOverlay(
    category: DashboardCategoryHub?,
    schoolProfile: SchoolProfile,
    activePlans: List<MedicalRecord>,
    inpatientCount: Int,
    fastingSessions: List<FastingSession>,
    fastingRecords: List<FastingRecord>,
    activeHaidRecords: List<HaidRecord>,
    suciRecords: List<HaidRecord>,
    shiftMessages: List<ShiftMessage>,
    mengajiRecords: List<MengajiRecord> = emptyList(),
    students: List<ChildStudent> = emptyList(),
    visitSessions: List<VisitSession> = emptyList(),
    visitRecords: List<VisitRecord> = emptyList(),
    visitSessionSwaps: List<VisitSessionSwap> = emptyList(),
    parentAccessLinks: List<ParentAccessLink> = emptyList(),
    parentMessages: List<ParentMessage> = emptyList(),
    parentExpectations: List<ParentExpectation> = emptyList(),
    myFosterStudentsCount: Int = 0,
    isAdmin: Boolean = false,
    onDismiss: () -> Unit,
    onNavigateToPlan: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onOpenCreatePlan: () -> Unit,
    onOpenDirectTreatment: () -> Unit,
    onOpenFaskesInfo: () -> Unit,
    onOpenInputPuasa: () -> Unit,
    onOpenDaftarPuasa: () -> Unit,
    onOpenChecklistPuasa: () -> Unit,
    onOpenRankingPuasa: () -> Unit,
    onOpenCreatePuasaSession: () -> Unit = {},
    onOpenInputHaid: () -> Unit,
    onOpenDaftarHaid: () -> Unit,
    onOpenDaftarSuci: () -> Unit,
    onOpenFiqihHaid: () -> Unit,
    onOpenShiftMessages: () -> Unit,
    onOpenCreateShiftMessage: () -> Unit,
    onOpenShiftHistory: () -> Unit,
    onOpenInputMengaji: () -> Unit = {},
    onOpenRiwayatMengaji: () -> Unit = {},
    onOpenKartuMengaji: () -> Unit = {},
    onOpenRekapMengaji: () -> Unit = {},
    onOpenQuranFromMengaji: () -> Unit = {},
    onOpenJadwalKunjungan: () -> Unit = {},
    onOpenDaftarHadirKunjungan: (Int) -> Unit = {},
    onOpenLaporanKunjungan: () -> Unit = {},
    onOpenSiswaPerKecamatan: () -> Unit = {},
    onOpenEdaranOrangTua: () -> Unit = {},
    onOpenCreateVisitSession: () -> Unit = {},
    onOpenPilihAnakAsuh: () -> Unit = {},
    incomingTandemRequestCount: Int = 0,
    onOpenKartuAksesOrtu: () -> Unit = {},
    onOpenPilihanAksesOrtu: () -> Unit = {},
    onOpenPesanOrtu: () -> Unit = {},
    onOpenHarapanOrtu: () -> Unit = {},
    waliGalleryPostsCount: Int = 0,
    onOpenWaliGallery: () -> Unit = {},
    tabunganTransactions: List<TabunganTransaction> = emptyList(),
    tabunganLogs: List<TabunganLog> = emptyList(),
    claimedFosterStudents: List<ChildStudent> = emptyList(),
    isAnakSuperAdmin: Boolean = false,
    onOpenInputTabungan: (String, ChildStudent?) -> Unit = { _, _ -> },
    onOpenTabunganStudentList: () -> Unit = {},
    onOpenTabunganStudentDetail: (ChildStudent) -> Unit = {},
    onOpenTabunganHistory: () -> Unit = {},
    onOpenTabunganPdfSummary: () -> Unit = {},
    konselingRecords: List<KonselingRecord> = emptyList(),
    konselingLogs: List<KonselingLog> = emptyList(),
    onOpenInputKonseling: (String, ChildStudent?) -> Unit = { _, _ -> },
    onOpenKonselingFollowUp: () -> Unit = {},
    onOpenKonselingStudentDetail: (ChildStudent) -> Unit = {},
    onOpenKonselingHistory: (Int) -> Unit = {},
    onOpenKonselingGuide: () -> Unit = {},
    onOpenKonselingPdfSummary: () -> Unit = {},
    jadwalMonthKey: String = WaliScheduleHelper.currentMonthKey(),
    jadwalSelectedDay: Int = WaliScheduleHelper.currentDayOfMonth(),
    shiftCodes: List<WaliShiftCode> = emptyList(),
    scheduleEntries: List<WaliScheduleEntry> = emptyList(),
    canManageSchedule: Boolean = false,
    isAnakAdminSekolah: Boolean = false,
    scheduleWaliAccounts: List<com.example.data.model.WaliAccount> = emptyList(),
    currentUsername: String? = null,
    currentWaliName: String = "",
    onChangeJadwalMonth: (String) -> Unit = {},
    onChangeJadwalDay: (Int) -> Unit = {},
    onOpenJadwalMatriks: () -> Unit = {},
    onOpenJadwalPersonal: (WaliScheduleEntry) -> Unit = {},
    onOpenPetugasJamIni: () -> Unit = {},
    onOpenRekapIzinJadwal: () -> Unit = {},
    onOpenKodeSopJadwal: () -> Unit = {},
    onOpenPinTugasJadwal: () -> Unit = {},
    onOpenManageShiftCodes: () -> Unit = {},
    onOpenImportJadwalMatriks: () -> Unit = {},
    onOpenUploadJadwalCsv: () -> Unit = onOpenImportJadwalMatriks,
    onEditScheduleCell: (WaliScheduleEntry, Int) -> Unit = { _, _ -> },
    onShareJadwalWhatsapp: () -> Unit = {},
    onExportJadwalMatriksPdf: () -> Unit = {},
    sholatRecords: List<SholatRecord> = emptyList(),
    onOpenAbsenSholatSheet: (tabIndex: Int, autoScanner: Boolean, scopeFilter: String, targetStudent: ChildStudent?) -> Unit = { _, _, _, _ -> },
    christianWorshipRecords: List<ChristianWorshipRecord> = emptyList(),
    canManageChristianClaim: Boolean = false,
    onOpenClaimChristianStudents: () -> Unit = {},
    onOpenInputChristianWorship: (initialTab: Int, defaultType: String, targetStudent: ChildStudent?) -> Unit = { _, _, _ -> },
    onOpenChristianReport: (initialTab: Int) -> Unit = {},
    onOpenBibleFromKristen: () -> Unit = {},
    onOpenSundayChurch: () -> Unit = {}
) {
    var lastCategory by remember { mutableStateOf(category) }
    var jadwalSearchQuery by remember(category) { mutableStateOf("") }
    var jadwalGroupFilter by remember(category) { mutableStateOf("ALL") }
    if (category != null) {
        lastCategory = category
    }
    val visible = category != null

    if (visible) {
        BackHandler { onDismiss() }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(animationSpec = tween(260)) { fullWidth -> fullWidth } + fadeIn(animationSpec = tween(200)),
        exit = slideOutHorizontally(animationSpec = tween(220)) { fullWidth -> fullWidth } + fadeOut(animationSpec = tween(180))
    ) {
        val current = category ?: lastCategory ?: return@AnimatedVisibility
        val s = LocalAppStrings.current
        val isEn = s.isEnglish
        val lang = s.language

        val todayPlans = remember(activePlans) { activePlans.filter { DateUtils.isToday(it.planDate) } }
        val upcomingPlans = remember(activePlans) { activePlans.filter { !DateUtils.isToday(it.planDate) } }

        val gradientColors = when (current) {
            DashboardCategoryHub.BEROBAT -> listOf(Color(0xFF0369A1), Color(0xFF0F766E))
            DashboardCategoryHub.KUNJUNGAN -> listOf(Color(0xFF1D4ED8), Color(0xFF0F766E))
            DashboardCategoryHub.PUASA -> listOf(Color(0xFF065F46), Color(0xFF0F766E))
            DashboardCategoryHub.HAID -> listOf(Color(0xFF9D174D), Color(0xFFDB2777))
            DashboardCategoryHub.PESAN -> listOf(Color(0xFFB45309), Color(0xFFD97706))
            DashboardCategoryHub.MENGAJI -> listOf(Color(0xFF047857), Color(0xFF0F766E))
            DashboardCategoryHub.ORANG_TUA -> listOf(Color(0xFF4338CA), Color(0xFF7C3AED))
            DashboardCategoryHub.TABUNGAN -> listOf(Color(0xFFB45309), Color(0xFF047857))
            DashboardCategoryHub.KONSELING -> listOf(Color(0xFF4C1D95), Color(0xFF6D28D9))
            DashboardCategoryHub.JADWAL_WALI -> listOf(Color(0xFF0F766E), Color(0xFF0369A1))
            DashboardCategoryHub.SHOLAT -> listOf(Color(0xFF065F46), Color(0xFF059669))
            DashboardCategoryHub.KRISTEN -> listOf(Color(0xFF3730A3), Color(0xFF0369A1))
        }

        val heroTitle = when (current) {
            DashboardCategoryHub.BEROBAT -> if (isEn) "Health & Treatment" else "Kesehatan & Berobat"
            DashboardCategoryHub.KUNJUNGAN -> if (isEn) "Parent Visit Center" else "Pusat Kunjungan Orang Tua"
            DashboardCategoryHub.PUASA -> if (isEn) "Student Puasa Center" else "Pusat Pendataan Puasa"
            DashboardCategoryHub.HAID -> if (isEn) "Haid & Fiqih Care" else "Pendataan & Fiqih Haid"
            DashboardCategoryHub.PESAN -> if (isEn) "Shift Handover Notes" else "Pesan & Operan Shift"
            DashboardCategoryHub.MENGAJI -> if (isEn) "Mengaji & Progress Card" else "Mengaji & Kartu Kendali"
            DashboardCategoryHub.ORANG_TUA -> if (isEn) "Parent Connect Center" else "Hubungan Orang Tua"
            DashboardCategoryHub.TABUNGAN -> if (isEn) "Private Student Savings" else "Tabungan Anak Asuh"
            DashboardCategoryHub.KONSELING -> if (isEn) "Confidential Counseling" else "Konseling Rahasia Anak"
            DashboardCategoryHub.JADWAL_WALI -> if (isEn) "Wali Asuh Duty Roster" else "Jadwal Kerja Wali Asuh"
            DashboardCategoryHub.SHOLAT -> if (isEn) "5 Daily Prayers Attendance" else "Absen Sholat 5 Waktu"
            DashboardCategoryHub.KRISTEN -> if (isEn) "Christian Faith & Worship" else "Pembinaan Agama Kristen"
        }

        val heroSubtitle = when (current) {
            DashboardCategoryHub.BEROBAT -> if (isEn) {
                "${todayPlans.size} plans today • ${upcomingPlans.size} upcoming"
            } else {
                "${todayPlans.size} rencana hari ini • ${upcomingPlans.size} mendatang"
            }
            DashboardCategoryHub.KUNJUNGAN -> {
                val activeVisit = visitSessions.firstOrNull { it.isActive } ?: visitSessions.firstOrNull()
                if (activeVisit != null) {
                    activeVisit.title
                } else if (isEn) {
                    "Monthly Schedule, Attendance & Districts"
                } else {
                    "Jadwal Bulanan, Daftar Hadir, Laporan & Kecamatan"
                }
            }
            DashboardCategoryHub.PUASA -> {
                val activeSess = fastingSessions.firstOrNull { it.isActive } ?: fastingSessions.firstOrNull()
                if (activeSess != null) activeSess.title else if (isEn) "Scan, Roster, Sahur & Berbuka" else "Input, Daftar, Sahur & Berbuka"
            }
            DashboardCategoryHub.HAID -> if (isEn) {
                "${activeHaidRecords.size} active Haid • ${suciRecords.size} Suci"
            } else {
                "${activeHaidRecords.size} sedang haid • ${suciRecords.size} masa suci"
            }
            DashboardCategoryHub.PESAN -> {
                val activeCount = shiftMessages.count { !it.isCompleted && DateUtils.isToday(it.followUpDate) }
                if (isEn) "$activeCount active handover notes today" else "$activeCount pesan operan aktif hari ini"
            }
            DashboardCategoryHub.MENGAJI -> {
                val todayCount = mengajiRecords.count { DateUtils.isToday(it.sessionDate) }
                if (isEn) {
                    "$todayCount entries today • ${mengajiRecords.size} total logs"
                } else {
                    "$todayCount setoran hari ini • ${mengajiRecords.size} total catatan"
                }
            }
            DashboardCategoryHub.ORANG_TUA -> {
                val unreadCount = parentMessages.count { !it.isReadByWali && it.senderRole.equals("PARENT", ignoreCase = true) }
                val expCount = parentExpectations.size
                if (isEn) {
                    "$myFosterStudentsCount foster kids • $expCount hopes • $unreadCount unread"
                } else {
                    "$myFosterStudentsCount anak asuh • $expCount harapan • $unreadCount pesan baru"
                }
            }
            DashboardCategoryHub.TABUNGAN -> {
                val totals = TabunganHelper.computeAggregateTotals(tabunganTransactions)
                if (isEn) {
                    "Balance: ${TabunganHelper.formatRupiah(totals.totalBalance)} • ${totals.holdersCount} Holders (Private)"
                } else {
                    "Saldo: ${TabunganHelper.formatRupiah(totals.totalBalance)} • ${totals.holdersCount} Anak (Pribadi)"
                }
            }
            DashboardCategoryHub.KONSELING -> {
                val agg = KonselingHelper.computeAggregateStats(konselingRecords)
                if (isEn) {
                    "${agg.totalEntries} Notes • ${agg.followUpCount} Monitor (Owner Wali Only)"
                } else {
                    "${agg.totalEntries} Catatan • ${agg.followUpCount} Pantau (Khusus Wali Asuh)"
                }
            }
            DashboardCategoryHub.JADWAL_WALI -> {
                val monthEntriesCount = scheduleEntries.count { it.monthKey == jadwalMonthKey }
                if (isEn) {
                    "${WaliScheduleHelper.formatDateChipLabel(jadwalMonthKey, jadwalSelectedDay, true)} • $monthEntriesCount Staff"
                } else {
                    "${WaliScheduleHelper.formatDateChipLabel(jadwalMonthKey, jadwalSelectedDay, false)} • $monthEntriesCount Personel"
                }
            }
            DashboardCategoryHub.SHOLAT -> {
                val todayKey = SholatHelper.todayDateKey()
                val activeWaktu = SholatHelper.detectActiveWaktu()
                val eligibleMuslimStudents = students.filter { !StudentReligionHelper.isChristian(it) }
                val todayWaktuRecords = sholatRecords.filter {
                    it.dateKey == todayKey && SholatWaktu.fromCode(it.waktu) == activeWaktu
                }
                val jamaahCount = todayWaktuRecords.count { SholatStatus.fromCode(it.status) == SholatStatus.JAMAAH }
                if (isEn) {
                    "${activeWaktu.labelEn} • $jamaahCount/${eligibleMuslimStudents.size} Congregation Today"
                } else {
                    "${activeWaktu.labelId} • $jamaahCount/${eligibleMuslimStudents.size} Jamaah Hari Ini"
                }
            }
            DashboardCategoryHub.KRISTEN -> {
                val christianCount = students.count { StudentReligionHelper.isChristian(it) }
                val activeLogs = christianWorshipRecords.count { !it.isDeleted }
                if (isEn) {
                    "$christianCount Christian Kids • $activeLogs Worship Logs"
                } else {
                    "$christianCount Anak Kristen • $activeLogs Catatan Ibadah"
                }
            }
        }

        val heroImage = when (current) {
            DashboardCategoryHub.BEROBAT -> R.drawable.img_menu_rencana
            DashboardCategoryHub.KUNJUNGAN -> R.drawable.img_menu_kunjungan
            DashboardCategoryHub.PUASA -> R.drawable.img_menu_input_puasa
            DashboardCategoryHub.HAID -> R.drawable.img_menu_daftar_haid
            DashboardCategoryHub.PESAN -> R.drawable.img_menu_pesan_shift
            DashboardCategoryHub.MENGAJI -> R.drawable.img_menu_mengaji
            DashboardCategoryHub.ORANG_TUA -> R.drawable.img_menu_orang_tua
            DashboardCategoryHub.TABUNGAN -> R.drawable.img_menu_tabungan
            DashboardCategoryHub.KONSELING -> R.drawable.img_menu_konseling
            DashboardCategoryHub.JADWAL_WALI -> R.drawable.img_menu_jadwal
            DashboardCategoryHub.SHOLAT -> R.drawable.img_menu_sholat
            DashboardCategoryHub.KRISTEN -> R.drawable.img_menu_kristen
        }

        val contextPillText = schoolProfile.shortName
            .ifBlank { schoolProfile.schoolName }
            .ifBlank { "Waliasuh Care" }

        val shieldInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradientColors))
                .testTag("category_hub_overlay_${current.name.lowercase()}")
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(interactionSource = shieldInteraction, indication = null, onClick = {})
            )
            Column(modifier = Modifier.fillMaxSize()) {
                // Hero Banner ala GoFood dengan tombol X lingkaran kiri atas & badge lokasi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp)
                            .align(Alignment.Center),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top Floating Bar ala GoFood: [Circle Close Button] [Context Pill] ... [Quick Action Circle]
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
                                    color = Color.White,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .clickable(onClick = onDismiss)
                                        .testTag("btn_close_category_hub")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = s.nav.close,
                                            tint = Color(0xFF0F172A),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.Black.copy(alpha = 0.28f))
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = contextPillText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Tombol Aksi Cepat Lingkaran di Kanan Atas
                            val quickActionIcon = when (current) {
                                DashboardCategoryHub.KUNJUNGAN, DashboardCategoryHub.PUASA, DashboardCategoryHub.HAID, DashboardCategoryHub.MENGAJI, DashboardCategoryHub.SHOLAT -> Icons.Default.QrCodeScanner
                                else -> Icons.Default.Add
                            }
                            val quickActionDesc = when (current) {
                                DashboardCategoryHub.BEROBAT -> if (isEn) "Create Plan" else "Buat Rencana"
                                DashboardCategoryHub.KUNJUNGAN -> if (isEn) "Scan Visit Check-in" else "Scan Daftar Hadir Kunjungan"
                                DashboardCategoryHub.PUASA -> if (isEn) "Scan Input Puasa" else "Scan Input Puasa"
                                DashboardCategoryHub.HAID -> if (isEn) "Scan Input Haid" else "Scan Input Haid"
                                DashboardCategoryHub.PESAN -> if (isEn) "Write Note" else "Tulis Pesan"
                                DashboardCategoryHub.MENGAJI -> if (isEn) "Scan Input Mengaji" else "Scan Input Mengaji"
                                DashboardCategoryHub.ORANG_TUA -> if (isEn) "Pick Foster Students" else "Pilih Anak Asuh"
                                DashboardCategoryHub.TABUNGAN -> if (isEn) "Add Savings Entry" else "Catat Titip Uang"
                                DashboardCategoryHub.KONSELING -> if (isEn) "Record Counseling" else "Catat Konseling"
                                DashboardCategoryHub.JADWAL_WALI -> if (canManageSchedule) {
                                    if (isEn) "Setup Schedule / Matrix" else "Kelola Jadwal & Matriks"
                                } else {
                                    if (isEn) "Monthly Matrix" else "Matriks Jadwal Bulanan"
                                }
                                DashboardCategoryHub.SHOLAT -> if (isEn) "Scan Prayer Card QR" else "Scan Kartu Sholat"
                                DashboardCategoryHub.KRISTEN -> if (isEn) "Log Christian Worship" else "Catat Ibadah Kristen"
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        when (current) {
                                            DashboardCategoryHub.BEROBAT -> onOpenCreatePlan()
                                            DashboardCategoryHub.KUNJUNGAN -> onOpenDaftarHadirKunjungan(0)
                                            DashboardCategoryHub.PUASA -> onOpenInputPuasa()
                                            DashboardCategoryHub.HAID -> onOpenInputHaid()
                                            DashboardCategoryHub.PESAN -> onOpenCreateShiftMessage()
                                            DashboardCategoryHub.MENGAJI -> onOpenInputMengaji()
                                            DashboardCategoryHub.ORANG_TUA -> onOpenPilihAnakAsuh()
                                            DashboardCategoryHub.TABUNGAN -> onOpenInputTabungan(TabunganHelper.TYPE_IN, null)
                                            DashboardCategoryHub.KONSELING -> onOpenInputKonseling(KonselingHelper.ENTRY_SESI, null)
                                            DashboardCategoryHub.JADWAL_WALI -> {
                                                if (canManageSchedule) onOpenImportJadwalMatriks()
                                                else onOpenJadwalMatriks()
                                            }
                                            DashboardCategoryHub.SHOLAT -> onOpenAbsenSholatSheet(0, true, "ALL", null)
                                            DashboardCategoryHub.KRISTEN -> onOpenInputChristianWorship(0, ChristianWorshipRecord.TYPE_DOA_PAGI, null)
                                        }
                                    }
                                    .testTag("btn_quick_action_category_hub")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = quickActionIcon,
                                        contentDescription = quickActionDesc,
                                        tint = gradientColors.first(),
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                            }
                        }

                        // Hero Headline + Ilustrasi Kartun
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = heroImage),
                                    contentDescription = heroTitle,
                                    modifier = Modifier.size(50.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = heroTitle,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = heroSubtitle,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Konten Sub-Menu + Ringkasan Live di Panel Putih Melengkung
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 640.dp)
                                .navigationBarsPadding()
                                .testTag("list_category_hub_content"),
                            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 28.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            when (current) {
                                DashboardCategoryHub.BEROBAT -> berobatCategoryItems(
                                    isEn = isEn,
                                    todayPlans = todayPlans,
                                    upcomingPlans = upcomingPlans,
                                    inpatientCount = inpatientCount,
                                    onNavigateToPlan = onNavigateToPlan,
                                    onOpenDirectTreatment = onOpenDirectTreatment,
                                    onNavigateToRecords = onNavigateToRecords,
                                    onOpenFaskesInfo = onOpenFaskesInfo,
                                    onOpenCreatePlan = onOpenCreatePlan
                                )
                                DashboardCategoryHub.PUASA -> puasaCategoryItems(
                                    isEn = isEn,
                                    lang = lang,
                                    fastingSessions = fastingSessions,
                                    fastingRecords = fastingRecords,
                                    onOpenInputPuasa = onOpenInputPuasa,
                                    onOpenDaftarPuasa = onOpenDaftarPuasa,
                                    onOpenChecklistPuasa = onOpenChecklistPuasa,
                                    onOpenRankingPuasa = onOpenRankingPuasa,
                                    onOpenCreatePuasaSession = onOpenCreatePuasaSession
                                )
                                DashboardCategoryHub.HAID -> haidCategoryItems(
                                    isEn = isEn,
                                    activeHaidRecords = activeHaidRecords,
                                    suciRecords = suciRecords,
                                    onOpenInputHaid = onOpenInputHaid,
                                    onOpenDaftarHaid = onOpenDaftarHaid,
                                    onOpenDaftarSuci = onOpenDaftarSuci,
                                    onOpenFiqihHaid = onOpenFiqihHaid
                                )
                                DashboardCategoryHub.PESAN -> pesanCategoryItems(
                                    isEn = isEn,
                                    shiftMessages = shiftMessages,
                                    onOpenShiftMessages = onOpenShiftMessages,
                                    onOpenCreateShiftMessage = onOpenCreateShiftMessage,
                                    onOpenShiftHistory = onOpenShiftHistory
                                )
                                DashboardCategoryHub.MENGAJI -> mengajiCategoryItems(
                                    isEn = isEn,
                                    lang = lang,
                                    mengajiRecords = mengajiRecords,
                                    onOpenInputMengaji = onOpenInputMengaji,
                                    onOpenRiwayatMengaji = onOpenRiwayatMengaji,
                                    onOpenKartuMengaji = onOpenKartuMengaji,
                                    onOpenRekapMengaji = onOpenRekapMengaji,
                                    onOpenQuran = onOpenQuranFromMengaji
                                )
                                DashboardCategoryHub.KUNJUNGAN -> kunjunganCategoryItems(
                                    isEn = isEn,
                                    lang = lang,
                                    students = students,
                                    visitSessions = visitSessions,
                                    visitRecords = visitRecords,
                                    visitSessionSwaps = visitSessionSwaps,
                                    isAdmin = isAdmin,
                                    onOpenJadwalKunjungan = onOpenJadwalKunjungan,
                                    onOpenDaftarHadirKunjungan = onOpenDaftarHadirKunjungan,
                                    onOpenLaporanKunjungan = onOpenLaporanKunjungan,
                                    onOpenSiswaPerKecamatan = onOpenSiswaPerKecamatan,
                                    onOpenEdaranOrangTua = onOpenEdaranOrangTua,
                                    onOpenCreateVisitSession = onOpenCreateVisitSession
                                )
                                DashboardCategoryHub.ORANG_TUA -> orangTuaCategoryItems(
                                    isEn = isEn,
                                    students = students,
                                    parentAccessLinks = parentAccessLinks,
                                    parentMessages = parentMessages,
                                    parentExpectations = parentExpectations,
                                    myFosterStudentsCount = myFosterStudentsCount,
                                    incomingTandemRequestCount = incomingTandemRequestCount,
                                    onOpenPilihAnakAsuh = onOpenPilihAnakAsuh,
                                    onOpenKartuAksesOrtu = onOpenKartuAksesOrtu,
                                    onOpenPilihanAksesOrtu = onOpenPilihanAksesOrtu,
                                    onOpenPesanOrtu = onOpenPesanOrtu,
                                    onOpenHarapanOrtu = onOpenHarapanOrtu,
                                    waliGalleryPostsCount = waliGalleryPostsCount,
                                    onOpenWaliGallery = onOpenWaliGallery
                                )
                                DashboardCategoryHub.TABUNGAN -> tabunganCategoryItems(
                                    isEn = isEn,
                                    allStudents = students,
                                    claimedStudents = claimedFosterStudents,
                                    transactions = tabunganTransactions,
                                    logs = tabunganLogs,
                                    isAnakSuperAdmin = isAnakSuperAdmin,
                                    onOpenInputTabungan = onOpenInputTabungan,
                                    onOpenStudentList = onOpenTabunganStudentList,
                                    onOpenStudentDetail = onOpenTabunganStudentDetail,
                                    onOpenHistoryLogs = onOpenTabunganHistory,
                                    onOpenPdfSummary = onOpenTabunganPdfSummary,
                                    onOpenPilihAnakAsuh = onOpenPilihAnakAsuh,
                                    onOpenPilihanAksesOrtu = onOpenPilihanAksesOrtu
                                )
                                DashboardCategoryHub.KONSELING -> konselingCategoryItems(
                                    isEn = isEn,
                                    claimedStudents = claimedFosterStudents,
                                    records = konselingRecords,
                                    logs = konselingLogs,
                                    onOpenInputKonseling = onOpenInputKonseling,
                                    onOpenFollowUpSheet = onOpenKonselingFollowUp,
                                    onOpenStudentDetail = onOpenKonselingStudentDetail,
                                    onOpenHistoryLogs = onOpenKonselingHistory,
                                    onOpenGuideDialog = onOpenKonselingGuide,
                                    onOpenPdfSummary = onOpenKonselingPdfSummary,
                                    onOpenPilihAnakAsuh = onOpenPilihAnakAsuh
                                )
                                DashboardCategoryHub.JADWAL_WALI -> jadwalWaliCategoryItems(
                                    isEn = isEn,
                                    schoolProfile = schoolProfile,
                                    monthKey = jadwalMonthKey,
                                    selectedDay = jadwalSelectedDay,
                                    shiftCodes = shiftCodes,
                                    monthEntries = scheduleEntries.filter { it.monthKey == jadwalMonthKey },
                                    canManageSchedule = canManageSchedule,
                                    isAnakAdminSekolah = isAnakAdminSekolah,
                                    scheduleWaliAccounts = scheduleWaliAccounts,
                                    currentUsername = currentUsername,
                                    currentWaliName = currentWaliName,
                                    searchQuery = jadwalSearchQuery,
                                    selectedGroup = jadwalGroupFilter,
                                    onSearchChange = { jadwalSearchQuery = it },
                                    onGroupChange = { jadwalGroupFilter = it },
                                    onChangeMonth = onChangeJadwalMonth,
                                    onChangeDay = onChangeJadwalDay,
                                    onOpenMatriks = onOpenJadwalMatriks,
                                    onOpenPersonal = onOpenJadwalPersonal,
                                    onOpenPetugasJamIni = onOpenPetugasJamIni,
                                    onOpenRekapIzin = onOpenRekapIzinJadwal,
                                    onOpenKodeSop = onOpenKodeSopJadwal,
                                    onOpenPinTugas = onOpenPinTugasJadwal,
                                    onOpenManageShiftCodes = onOpenManageShiftCodes,
                                    onOpenImportMatriks = onOpenImportJadwalMatriks,
                                    onOpenUploadCsv = onOpenUploadJadwalCsv,
                                    onEditCell = onEditScheduleCell,
                                    onShareDailyWhatsapp = onShareJadwalWhatsapp,
                                    onExportMatriksPdf = onExportJadwalMatriksPdf
                                )
                                DashboardCategoryHub.SHOLAT -> sholatCategoryItems(
                                    isEn = isEn,
                                    allStudents = students,
                                    claimedStudents = claimedFosterStudents,
                                    sholatRecords = sholatRecords,
                                    haidRecords = activeHaidRecords + suciRecords,
                                    onOpenAbsenSholatSheet = onOpenAbsenSholatSheet,
                                    onOpenPilihAnakAsuh = onOpenPilihAnakAsuh,
                                    onOpenPilihanAksesOrtu = onOpenPilihanAksesOrtu
                                )
                                DashboardCategoryHub.KRISTEN -> kristenCategoryItems(
                                    isEn = isEn,
                                    lang = lang,
                                    allStudents = students,
                                    claimedStudents = claimedFosterStudents,
                                    christianWorshipRecords = christianWorshipRecords,
                                    waliName = currentWaliName.ifBlank { "Wali Asuh" },
                                    canManageChristianClaim = canManageChristianClaim,
                                    onOpenClaimChristianStudents = onOpenClaimChristianStudents,
                                    onOpenInputChristianWorship = onOpenInputChristianWorship,
                                    onOpenChristianReport = onOpenChristianReport,
                                    onOpenBible = onOpenBibleFromKristen,
                                    onOpenSundayChurch = onOpenSundayChurch
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
