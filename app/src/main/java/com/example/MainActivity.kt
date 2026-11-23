package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.util.NotificationHelper
import com.example.util.TopOnAdManager
import com.example.util.i18n.AppLanguageManager
import com.example.util.i18n.LocalAppStrings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HealthViewModel
import com.example.ui.UserRole
import com.example.ui.components.CategoryHubOverlay
import com.example.ui.components.WaliTopBar
import com.example.ui.components.rememberDashboardModalState
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JadwalQuickScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MedicalRecordScreen
import com.example.ui.screens.ParentPortalScreen
import com.example.ui.screens.PlanScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.screens.SuperAdminScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils

enum class WaliNavScreen(val title: String) {
    DASHBOARD("Dashboard"),
    RENCANA("Rencana"),
    JADWAL("Jadwal"),
    TINDAKAN("Tindakan"),
    ANAK_ASUH("Anak Asuh"),
    PROFIL("Profil")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppLanguageManager.init(applicationContext)
        NotificationHelper.createNotificationChannel(applicationContext)
        TopOnAdManager.init(this)
        val targetScreen = intent?.getStringExtra("EXTRA_NAV_SCREEN")

        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            val viewModel: HealthViewModel = viewModel()
            val userRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
            val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
            val baseContext = LocalContext.current
            val localizedContext = remember(baseContext, appLanguage) {
                AppLanguageManager.createLocalizedContext(baseContext, appLanguage)
            }
            val appStrings = remember(appLanguage) {
                AppLanguageManager.stringsFor(appLanguage)
            }
            var showSplashScreen by rememberSaveable { mutableStateOf(true) }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration,
                LocalAppStrings provides appStrings
            ) {
                MyApplicationTheme(darkTheme = isDarkTheme) {
                    if (showSplashScreen) {
                        SplashScreen(
                            onFinish = { showSplashScreen = false }
                        )
                    } else {
                        when {
                            userRole == UserRole.NOT_LOGGED_IN -> {
                                LoginScreen(
                                    viewModel = viewModel,
                                    onLoginSuccessAdmin = {},
                                    onLoginSuccessWali = { _ -> },
                                    onLoginSuccessSuperAdmin = {}
                                )
                            }
                            userRole == UserRole.SUPERADMIN -> {
                                SuperAdminScreen(
                                    viewModel = viewModel,
                                    onLogout = { viewModel.logout() }
                                )
                            }
                            userRole == UserRole.ADMIN -> {
                                AdminScreen(
                                    viewModel = viewModel,
                                    onLogout = { viewModel.logout() }
                                )
                            }
                            userRole == UserRole.PARENT -> {
                                ParentPortalScreen(
                                    viewModel = viewModel,
                                    onLogout = { viewModel.logout() }
                                )
                            }
                            userRole == UserRole.GURU_NGAJI -> {
                                com.example.ui.screens.GuruNgajiScreen(
                                    viewModel = viewModel,
                                    onLogout = { viewModel.logout() }
                                )
                            }
                            userRole == UserRole.PETUGAS_KESEHATAN -> {
                                com.example.ui.screens.PetugasKesehatanScreen(
                                    viewModel = viewModel,
                                    onLogout = { viewModel.logout() }
                                )
                            }
                            else -> {
                                WaliAsuhCareApp(
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                                    initialScreen = if (targetScreen == "TINDAKAN") WaliNavScreen.TINDAKAN else WaliNavScreen.DASHBOARD,
                                    viewModel = viewModel,
                                    onLogout = {
                                        viewModel.logout()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WaliAsuhCareApp(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    initialScreen: WaliNavScreen = WaliNavScreen.DASHBOARD,
    viewModel: HealthViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    WaliAsuhCareAppContent(
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        initialScreen = initialScreen,
        viewModel = viewModel,
        onLogout = onLogout
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun WaliAsuhCareAppContent(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    initialScreen: WaliNavScreen = WaliNavScreen.DASHBOARD,
    viewModel: HealthViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    var currentScreen by rememberSaveable { mutableStateOf(initialScreen) }
    var showNotifModal by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val s = LocalAppStrings.current

    // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { _ -> }

        LaunchedEffect(Unit) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Collect Reactive StateFlows
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val activePlans by viewModel.activePlans.collectAsStateWithLifecycle()
    val completedRecords by viewModel.filteredCompletedRecords.collectAsStateWithLifecycle()
    val pendingControls by viewModel.pendingControlRecords.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()

    val waliName by viewModel.waliName.collectAsStateWithLifecycle()
    val historySearchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()
    val studentSearchQuery by viewModel.studentSearchQuery.collectAsStateWithLifecycle()
    val shiftMessages by viewModel.shiftMessages.collectAsStateWithLifecycle()
    val activeHaidRecords by viewModel.activeHaidRecords.collectAsStateWithLifecycle()
    val suciRecords by viewModel.suciRecords.collectAsStateWithLifecycle()
    val fastingSessions by viewModel.fastingSessions.collectAsStateWithLifecycle()
    val fastingRecords by viewModel.fastingRecords.collectAsStateWithLifecycle()
    val mengajiRecords by viewModel.mengajiRecords.collectAsStateWithLifecycle()
    val visitSessions by viewModel.waliVisibleVisitSessions.collectAsStateWithLifecycle()
    val visitRecords by viewModel.visitRecords.collectAsStateWithLifecycle()
    val visitSessionSwaps by viewModel.visitSessionSwaps.collectAsStateWithLifecycle()
    val parentAccessLinks by viewModel.parentAccessLinks.collectAsStateWithLifecycle()
    val parentMessages by viewModel.parentMessages.collectAsStateWithLifecycle()
    val parentExpectations by viewModel.parentExpectations.collectAsStateWithLifecycle()
    val tabunganTransactions by viewModel.tabunganTransactions.collectAsStateWithLifecycle()
    val tabunganLogs by viewModel.tabunganLogs.collectAsStateWithLifecycle()
    val konselingRecords by viewModel.konselingRecords.collectAsStateWithLifecycle()
    val konselingLogs by viewModel.konselingLogs.collectAsStateWithLifecycle()
    val schoolShiftCodes by viewModel.schoolShiftCodes.collectAsStateWithLifecycle()
    val schoolScheduleEntries by viewModel.schoolScheduleEntries.collectAsStateWithLifecycle()
    val sholatRecords by viewModel.sholatRecords.collectAsStateWithLifecycle()
    val christianWorshipRecords by viewModel.christianWorshipRecords.collectAsStateWithLifecycle()
    val waliGalleryPosts by viewModel.waliGalleryPosts.collectAsStateWithLifecycle()
    val deletionLogs by viewModel.deletionLogs.collectAsStateWithLifecycle()
    val unreadDeletionLogsCount by viewModel.unreadDeletionLogsCount.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val currentUsername by viewModel.currentUsername.collectAsStateWithLifecycle()
    val waliAccounts by viewModel.waliAccounts.collectAsStateWithLifecycle()
    val currentWaliAccount by viewModel.currentWaliAccount.collectAsStateWithLifecycle()
    val isAnakSuperAdmin by viewModel.isAnakSuperAdmin.collectAsStateWithLifecycle()
    val isAnakAdminSekolah by viewModel.isAnakAdminSekolah.collectAsStateWithLifecycle()
    val canManageMainSchedule = currentUserRole == UserRole.ADMIN ||
        currentUserRole == UserRole.SUPERADMIN ||
        isAnakSuperAdmin ||
        isAnakAdminSekolah
    val mainCoroutineScope = rememberCoroutineScope()
    val dashboardModalState = rememberDashboardModalState()
    var readyMainTabunganSummaryPdf by remember { mutableStateOf<java.io.File?>(null) }
    var readyMainKonselingSummaryPdf by remember { mutableStateOf<java.io.File?>(null) }
    var isGeneratingMainKonselingPdf by remember { mutableStateOf(false) }
    var readyMainJadwalPdf by remember { mutableStateOf<java.io.File?>(null) }

    LaunchedEffect(
        dashboardModalState.activeCategoryHub,
        dashboardModalState.showInputTabunganDialog,
        dashboardModalState.showTabunganStudentListSheet,
        dashboardModalState.showTabunganHistoryLogsSheet,
        dashboardModalState.tabunganStudentForDetail
    ) {
        if (dashboardModalState.activeCategoryHub == com.example.ui.components.DashboardCategoryHub.TABUNGAN ||
            dashboardModalState.showInputTabunganDialog ||
            dashboardModalState.showTabunganStudentListSheet ||
            dashboardModalState.showTabunganHistoryLogsSheet ||
            dashboardModalState.tabunganStudentForDetail != null
        ) {
            viewModel.syncTabunganOnlineQuietly()
        }
    }
    LaunchedEffect(
        dashboardModalState.activeCategoryHub,
        dashboardModalState.showInputKonselingDialog,
        dashboardModalState.showKonselingFollowUpSheet,
        dashboardModalState.showKonselingHistoryLogsSheet,
        dashboardModalState.konselingStudentForDetail
    ) {
        if (dashboardModalState.activeCategoryHub == com.example.ui.components.DashboardCategoryHub.KONSELING ||
            dashboardModalState.showInputKonselingDialog ||
            dashboardModalState.showKonselingFollowUpSheet ||
            dashboardModalState.showKonselingHistoryLogsSheet ||
            dashboardModalState.konselingStudentForDetail != null
        ) {
            viewModel.syncKonselingOnlineQuietly()
        }
    }
    LaunchedEffect(
        currentScreen,
        dashboardModalState.activeCategoryHub,
        dashboardModalState.showJadwalMatriksSheet,
        dashboardModalState.showDaftarKodeSopSheet,
        dashboardModalState.showManageShiftCodesDialog,
        dashboardModalState.showImportJadwalMatriksDialog,
        schoolProfile.tenantCode
    ) {
        if (currentScreen == WaliNavScreen.JADWAL ||
            dashboardModalState.activeCategoryHub == com.example.ui.components.DashboardCategoryHub.JADWAL_WALI ||
            dashboardModalState.showJadwalMatriksSheet ||
            dashboardModalState.showDaftarKodeSopSheet ||
            dashboardModalState.showManageShiftCodesDialog ||
            dashboardModalState.showImportJadwalMatriksDialog
        ) {
            viewModel.ensureSchoolShiftCodesSeeded(schoolProfile.tenantCode)
        }
    }
    LaunchedEffect(
        dashboardModalState.activeCategoryHub,
        dashboardModalState.showAbsenSholatSheet
    ) {
        if (dashboardModalState.activeCategoryHub == com.example.ui.components.DashboardCategoryHub.SHOLAT ||
            dashboardModalState.showAbsenSholatSheet
        ) {
            viewModel.syncSholatOnlineQuietly()
        }
    }
    LaunchedEffect(
        dashboardModalState.activeCategoryHub,
        dashboardModalState.showWaliGallerySheet
    ) {
        if (dashboardModalState.activeCategoryHub == com.example.ui.components.DashboardCategoryHub.ORANG_TUA ||
            dashboardModalState.showWaliGallerySheet
        ) {
            viewModel.syncWaliGalleryOnlineQuietly()
        }
    }
    LaunchedEffect(
        dashboardModalState.activeCategoryHub,
        dashboardModalState.showClaimChristianSheet,
        dashboardModalState.showInputChristianWorshipDialog,
        dashboardModalState.showChristianReportSheet,
        dashboardModalState.showSundayChurchSheet
    ) {
        if (dashboardModalState.activeCategoryHub == com.example.ui.components.DashboardCategoryHub.KRISTEN ||
            dashboardModalState.showClaimChristianSheet ||
            dashboardModalState.showInputChristianWorshipDialog ||
            dashboardModalState.showChristianReportSheet ||
            dashboardModalState.showSundayChurchSheet
        ) {
            viewModel.syncChristianWorshipOnlineQuietly()
        }
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
    val myFosterParentExpectations = remember(parentExpectations, myFosterStudentIds, currentUserRole) {
        val isAdminRole = currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.SUPERADMIN
        if (isAdminRole && myFosterStudentIds.isEmpty()) parentExpectations
        else parentExpectations.filter { it.studentId in myFosterStudentIds }
    }
    val myWaliGalleryPostsCount = remember(waliGalleryPosts, currentUsername, waliName, currentUserRole) {
        val isAdminRole = currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.SUPERADMIN
        waliGalleryPosts.count { post ->
            !post.isDeleted && (
                isAdminRole || com.example.data.model.WaliGalleryHelper.matchesWaliOwner(
                    post = post,
                    waliUsername = currentUsername.orEmpty(),
                    waliName = waliName
                )
            )
        }
    }
    val inpatientCount = remember(allRecords) {
        allRecords.count { it.isInpatient && it.status != "CANCELLED" }
    }

    // Handle back button for secondary screens -> return to Dashboard
    if (currentScreen != WaliNavScreen.DASHBOARD) {
        BackHandler {
            currentScreen = WaliNavScreen.DASHBOARD
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            WaliTopBar(
                notificationCount = pendingControls.size + activePlans.size + unreadDeletionLogsCount,
                onNotificationClick = {
                    TopOnAdManager.onUserActionClick(context)
                    viewModel.markDeletionLogsRead()
                    showNotifModal = true
                },
                onProfileClick = {
                    TopOnAdManager.onUserActionClick(context)
                    currentScreen = WaliNavScreen.PROFIL
                },
                isDoctorIcon = currentScreen == WaliNavScreen.TINDAKAN,
                schoolName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
                waliName = currentWaliAccount?.name?.ifBlank { waliName } ?: waliName,
                waliPhotoUrl = currentWaliAccount?.photoUrl.orEmpty(),
                waliPhotoFallbackUrl = currentWaliAccount?.photoFallbackUrl.orEmpty(),
                waliDormitory = currentWaliAccount?.dormitory.orEmpty()
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp)
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    // 1. Dashboard (from Mockup 3 & 4)
                    NavigationBarItem(
                        selected = currentScreen == WaliNavScreen.DASHBOARD,
                        onClick = {
                            TopOnAdManager.onUserActionClick(context)
                            currentScreen = WaliNavScreen.DASHBOARD
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = s.nav.navHome,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = s.nav.navHome,
                                fontWeight = if (currentScreen == WaliNavScreen.DASHBOARD) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WaliPrimary,
                            selectedTextColor = WaliPrimary,
                            indicatorColor = WaliPrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    // 2. Rencana (from Mockup 3 & 4)
                    NavigationBarItem(
                        selected = currentScreen == WaliNavScreen.RENCANA,
                        onClick = {
                            TopOnAdManager.onUserActionClick(context)
                            currentScreen = WaliNavScreen.RENCANA
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = s.nav.navPlans,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = s.nav.navPlans,
                                fontWeight = if (currentScreen == WaliNavScreen.RENCANA) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WaliPrimary,
                            selectedTextColor = WaliPrimary,
                            indicatorColor = WaliPrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_item_rencana")
                    )

                    // 3. Jadwal Harian Cepat (menggantikan slot Tindakan di bottom bar; Tindakan tetap dibuka lewat Dashboard -> Berobat / Notifikasi)
                    NavigationBarItem(
                        selected = currentScreen == WaliNavScreen.JADWAL,
                        onClick = {
                            TopOnAdManager.onUserActionClick(context)
                            currentScreen = WaliNavScreen.JADWAL
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = s.nav.navSchedule,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = s.nav.navSchedule,
                                fontWeight = if (currentScreen == WaliNavScreen.JADWAL) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WaliPrimary,
                            selectedTextColor = WaliPrimary,
                            indicatorColor = WaliPrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_item_jadwal")
                    )

                    // 4. Anak Asuh (from Mockup 1, 2, 3, 4)
                    NavigationBarItem(
                        selected = currentScreen == WaliNavScreen.ANAK_ASUH,
                        onClick = {
                            TopOnAdManager.onUserActionClick(context)
                            currentScreen = WaliNavScreen.ANAK_ASUH
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = s.nav.navStudents,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = s.nav.navStudents,
                                fontWeight = if (currentScreen == WaliNavScreen.ANAK_ASUH) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WaliPrimary,
                            selectedTextColor = WaliPrimary,
                            indicatorColor = WaliPrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_item_anak_asuh")
                    )

                    // 5. Profil (from Mockup 1)
                    NavigationBarItem(
                        selected = currentScreen == WaliNavScreen.PROFIL,
                        onClick = {
                            TopOnAdManager.onUserActionClick(context)
                            currentScreen = WaliNavScreen.PROFIL
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = s.nav.navProfile,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = s.nav.navProfile,
                                fontWeight = if (currentScreen == WaliNavScreen.PROFIL) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WaliPrimary,
                            selectedTextColor = WaliPrimary,
                            indicatorColor = WaliPrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_item_profil")
                    )
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
            when (currentScreen) {
                WaliNavScreen.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        waliName = waliName,
                        activePlans = activePlans,
                        students = students,
                        onNavigateToPlan = {
                            TopOnAdManager.onUserActionClick(context)
                            currentScreen = WaliNavScreen.RENCANA
                        },
                        onNavigateToRecords = {
                            TopOnAdManager.onUserActionClick(context)
                            currentScreen = WaliNavScreen.TINDAKAN
                        },
                        onNavigateToStudents = {
                            TopOnAdManager.onUserActionClick(context)
                            currentScreen = WaliNavScreen.ANAK_ASUH
                        },
                        onNavigateToProfile = {
                            TopOnAdManager.onUserActionClick(context)
                            currentScreen = WaliNavScreen.PROFIL
                        },
                        modalState = dashboardModalState,
                        renderInlineCategoryHub = false,
                        renderInlineBibleSheet = false,
                        renderInlineQuranSheet = false
                    )
                }
                WaliNavScreen.RENCANA -> {
                    PlanScreen(
                        viewModel = viewModel,
                        plans = activePlans,
                        pendingControls = pendingControls,
                        students = students
                    )
                }
                WaliNavScreen.JADWAL -> {
                    JadwalQuickScreen(
                        viewModel = viewModel,
                        schoolProfile = schoolProfile,
                        shiftCodes = schoolShiftCodes,
                        scheduleEntries = schoolScheduleEntries,
                        canManageSchedule = canManageMainSchedule,
                        waliAccounts = waliAccounts,
                        currentUsername = currentUsername,
                        currentWaliName = waliName,
                        modalState = dashboardModalState
                    )
                }
                WaliNavScreen.TINDAKAN -> {
                    MedicalRecordScreen(
                        viewModel = viewModel,
                        waliName = waliName,
                        completedRecords = completedRecords,
                        allRecords = allRecords,
                        students = students,
                        searchQuery = historySearchQuery,
                        onNavigateToProfile = { currentScreen = WaliNavScreen.PROFIL }
                    )
                }
                WaliNavScreen.ANAK_ASUH -> {
                    StudentsScreen(
                        viewModel = viewModel,
                        students = students,
                        records = allRecords,
                        searchQuery = studentSearchQuery
                    )
                }
                WaliNavScreen.PROFIL -> {
                    ProfileScreen(
                        viewModel = viewModel,
                        waliName = waliName,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        records = allRecords,
                        onLogout = onLogout,
                        onStartInteractiveTour = {
                            dashboardModalState.activeCategoryHub = null
                            currentScreen = WaliNavScreen.DASHBOARD
                            viewModel.startDashboardTour()
                        }
                    )
                }
            }
        }
        }

        // Halaman Sub-Kategori Full-Screen ala Gojek/GoFood (Slide dari kanan)
        if (currentScreen == WaliNavScreen.DASHBOARD) {
            CategoryHubOverlay(
                category = dashboardModalState.activeCategoryHub,
                schoolProfile = schoolProfile,
                activePlans = activePlans,
                inpatientCount = inpatientCount,
                fastingSessions = fastingSessions,
                fastingRecords = fastingRecords,
                activeHaidRecords = activeHaidRecords,
                suciRecords = suciRecords,
                shiftMessages = shiftMessages,
                mengajiRecords = mengajiRecords,
                students = students,
                visitSessions = visitSessions,
                visitRecords = visitRecords,
                visitSessionSwaps = visitSessionSwaps,
                parentAccessLinks = parentAccessLinks,
                parentMessages = myFosterParentMessages,
                parentExpectations = myFosterParentExpectations,
                myFosterStudentsCount = myFosterStudentsCount,
                isAdmin = (currentUserRole == com.example.ui.UserRole.ADMIN || currentUserRole == com.example.ui.UserRole.SUPERADMIN),
                onDismiss = { dashboardModalState.activeCategoryHub = null },
                onNavigateToPlan = {
                    dashboardModalState.activeCategoryHub = null
                    TopOnAdManager.onUserActionClick(context)
                    currentScreen = WaliNavScreen.RENCANA
                },
                onNavigateToRecords = {
                    dashboardModalState.activeCategoryHub = null
                    TopOnAdManager.onUserActionClick(context)
                    currentScreen = WaliNavScreen.TINDAKAN
                },
                onOpenCreatePlan = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showCreateDialog = true
                },
                onOpenDirectTreatment = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showDirectTreatmentDialog = true
                },
                onOpenFaskesInfo = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showPuskesmasInfoSheet = true
                },
                onOpenInputPuasa = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showInputPuasaDialog = true
                },
                onOpenDaftarPuasa = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showDaftarPuasaSheet = true
                },
                onOpenChecklistPuasa = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showChecklistPuasaSheet = true
                },
                onOpenRankingPuasa = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showRankingPuasaSheet = true
                },
                onOpenCreatePuasaSession = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showCreatePuasaSessionDialog = true
                },
                onOpenInputHaid = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showInputHaidDialog = true
                },
                onOpenDaftarHaid = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showDaftarHaidSheet = true
                },
                onOpenDaftarSuci = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showDaftarSuciSheet = true
                },
                onOpenFiqihHaid = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showFiqihHaidDialog = true
                },
                onOpenShiftMessages = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showShiftMessagesSheet = true
                },
                onOpenCreateShiftMessage = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showCreateShiftMessageDialog = true
                },
                onOpenShiftHistory = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showShiftHistorySheet = true
                },
                onOpenInputMengaji = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.mengajiTargetStudent = null
                    dashboardModalState.mengajiRecordToEdit = null
                    dashboardModalState.showInputMengajiDialog = true
                },
                onOpenRiwayatMengaji = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.riwayatMengajiInitialTab = 0
                    dashboardModalState.showRiwayatMengajiSheet = true
                },
                onOpenKartuMengaji = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.mengajiTargetStudent = null
                    dashboardModalState.showKartuMengajiSheet = true
                },
                onOpenRekapMengaji = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.riwayatMengajiInitialTab = 1
                    dashboardModalState.showRiwayatMengajiSheet = true
                },
                onOpenQuranFromMengaji = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showQuranSheet = true
                },
                onOpenJadwalKunjungan = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showJadwalKunjunganSheet = true
                },
                onOpenDaftarHadirKunjungan = { tabIdx ->
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.daftarHadirKunjunganInitialTab = tabIdx
                    dashboardModalState.showDaftarHadirKunjunganSheet = true
                },
                onOpenLaporanKunjungan = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showLaporanKunjunganSheet = true
                },
                onOpenSiswaPerKecamatan = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showSiswaPerKecamatanSheet = true
                },
                onOpenEdaranOrangTua = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showEdaranOrangTuaSheet = true
                },
                onOpenCreateVisitSession = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.visitSessionToEdit = null
                    dashboardModalState.showCreateVisitSessionDialog = true
                },
                onOpenPilihAnakAsuh = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showPilihAnakAsuhSheet = true
                },
                onOpenKartuAksesOrtu = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showKartuAksesOrtuSheet = true
                },
                onOpenPilihanAksesOrtu = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showPilihanAksesOrtuSheet = true
                },
                onOpenPesanOrtu = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.selectedParentChatStudent = null
                    dashboardModalState.showPesanOrtuSheet = true
                },
                onOpenHarapanOrtu = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showHarapanOrtuSheet = true
                },
                waliGalleryPostsCount = myWaliGalleryPostsCount,
                onOpenWaliGallery = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showWaliGallerySheet = true
                },
                tabunganTransactions = tabunganTransactions,
                tabunganLogs = tabunganLogs,
                claimedFosterStudents = myFosterStudents,
                isAnakSuperAdmin = isAnakSuperAdmin,
                onOpenInputTabungan = { defaultType, targetStudent ->
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.tabunganTxToEdit = null
                    dashboardModalState.inputTabunganDefaultType = defaultType
                    dashboardModalState.inputTabunganTargetStudent = targetStudent
                    dashboardModalState.showInputTabunganDialog = true
                },
                onOpenTabunganStudentList = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showTabunganStudentListSheet = true
                },
                onOpenTabunganStudentDetail = { student ->
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.tabunganStudentForDetail = student
                },
                onOpenTabunganHistory = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.tabunganHistoryInitialTab = 0
                    dashboardModalState.showTabunganHistoryLogsSheet = true
                },
                onOpenTabunganPdfSummary = {
                    TopOnAdManager.onUserActionClick(context)
                    val summaryStudents = com.example.data.model.TabunganHelper.buildSummaryStudents(
                        myFosterStudents = myFosterStudents,
                        allStudents = students,
                        transactions = tabunganTransactions
                    )
                    val pdf = com.example.util.PdfReportGenerator.generateTabunganSummaryPdf(
                        context = context,
                        students = summaryStudents,
                        transactions = tabunganTransactions,
                        waliName = waliName,
                        schoolProfile = schoolProfile
                    )
                    if (pdf != null) {
                        readyMainTabunganSummaryPdf = pdf
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            if (s.isEnglish) "Failed to generate savings recap PDF." else "Gagal membuat PDF rekap tabungan.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                konselingRecords = konselingRecords,
                konselingLogs = konselingLogs,
                onOpenInputKonseling = { defaultEntryType, targetStudent ->
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.konselingRecordToEdit = null
                    dashboardModalState.inputKonselingDefaultEntryType = defaultEntryType
                    dashboardModalState.inputKonselingTargetStudent = targetStudent
                    dashboardModalState.showInputKonselingDialog = true
                },
                onOpenKonselingFollowUp = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showKonselingFollowUpSheet = true
                },
                onOpenKonselingStudentDetail = { student ->
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.konselingStudentForDetail = student
                },
                onOpenKonselingHistory = { initialTab ->
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.konselingHistoryInitialTab = initialTab
                    dashboardModalState.showKonselingHistoryLogsSheet = true
                },
                onOpenKonselingGuide = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showKonselingGuideDialog = true
                },
                onOpenKonselingPdfSummary = {
                    TopOnAdManager.onUserActionClick(context)
                    if (!isGeneratingMainKonselingPdf) {
                        isGeneratingMainKonselingPdf = true
                        android.widget.Toast.makeText(
                            context,
                            if (s.isEnglish) "Preparing counseling PDF with photos..." else "Menyiapkan PDF konseling beserta foto...",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        mainCoroutineScope.launch {
                            val pdf = com.example.util.PdfReportGenerator.generateKonselingSummaryPdfWithPhotos(
                                context = context,
                                students = myFosterStudents,
                                records = konselingRecords,
                                waliName = waliName,
                                schoolProfile = schoolProfile
                            )
                            isGeneratingMainKonselingPdf = false
                            if (pdf != null) {
                                readyMainKonselingSummaryPdf = pdf
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    if (s.isEnglish) "Failed to generate confidential counseling PDF." else "Gagal membuat PDF rekap konseling rahasia.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
                jadwalMonthKey = dashboardModalState.jadwalSelectedMonthKey,
                jadwalSelectedDay = dashboardModalState.jadwalSelectedDay,
                shiftCodes = schoolShiftCodes,
                scheduleEntries = schoolScheduleEntries,
                canManageSchedule = canManageMainSchedule,
                isAnakAdminSekolah = isAnakAdminSekolah,
                scheduleWaliAccounts = waliAccounts,
                currentUsername = currentUsername,
                currentWaliName = waliName,
                onChangeJadwalMonth = { newMonth ->
                    dashboardModalState.jadwalSelectedMonthKey = newMonth
                    val maxD = com.example.data.model.WaliScheduleHelper.daysInMonth(newMonth)
                    if (dashboardModalState.jadwalSelectedDay > maxD) {
                        dashboardModalState.jadwalSelectedDay = maxD
                    }
                },
                onChangeJadwalDay = { newDay ->
                    dashboardModalState.jadwalSelectedDay = newDay
                },
                onOpenJadwalMatriks = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showJadwalMatriksSheet = true
                },
                onOpenJadwalPersonal = { entry ->
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.jadwalPersonalTargetEntry = entry
                    dashboardModalState.showJadwalPersonalSheet = true
                },
                onOpenPetugasJamIni = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showPetugasJamIniDialog = true
                },
                onOpenRekapIzinJadwal = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showRekapIzinJadwalSheet = true
                },
                onOpenKodeSopJadwal = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showDaftarKodeSopSheet = true
                },
                onOpenPinTugasJadwal = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showPenyematanTugasSheet = true
                },
                onOpenManageShiftCodes = {
                    TopOnAdManager.onUserActionClick(context)
                    if (canManageMainSchedule) {
                        dashboardModalState.showManageShiftCodesDialog = true
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            if (s.isEnglish) "Only school admin can edit shift codes." else "Hanya admin sekolah yang dapat mengatur daftar kode shift.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onOpenImportJadwalMatriks = {
                    TopOnAdManager.onUserActionClick(context)
                    if (canManageMainSchedule) {
                        dashboardModalState.importJadwalInitialTab = 0
                        dashboardModalState.showImportJadwalMatriksDialog = true
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            if (s.isEnglish) "Only school admin can manage the roster." else "Hanya admin sekolah yang dapat mengatur jadwal.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onOpenUploadJadwalCsv = {
                    TopOnAdManager.onUserActionClick(context)
                    if (canManageMainSchedule) {
                        dashboardModalState.importJadwalInitialTab = 2
                        dashboardModalState.showImportJadwalMatriksDialog = true
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            if (s.isEnglish) "Only school admin can manage the roster." else "Hanya admin sekolah yang dapat mengatur jadwal.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onEditScheduleCell = { entry, day ->
                    TopOnAdManager.onUserActionClick(context)
                    if (canManageMainSchedule) {
                        dashboardModalState.editingScheduleCellTarget = entry to day
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            if (s.isEnglish) "Schedule is managed by the school admin." else "Jadwal hanya dapat diubah oleh Admin Sekolah.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onShareJadwalWhatsapp = {
                    TopOnAdManager.onUserActionClick(context)
                    val waText = com.example.data.model.WaliScheduleHelper.buildDailyWhatsappSummary(
                        monthKey = dashboardModalState.jadwalSelectedMonthKey,
                        day = dashboardModalState.jadwalSelectedDay,
                        schoolName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName },
                        entries = schoolScheduleEntries.filter { it.monthKey == dashboardModalState.jadwalSelectedMonthKey },
                        shiftMap = com.example.data.model.WaliScheduleHelper.buildShiftMap(
                            schoolShiftCodes,
                            schoolProfile.tenantCode
                        ),
                        isEn = s.isEnglish
                    )
                    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, waText)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        context.startActivity(
                            android.content.Intent.createChooser(
                                sendIntent,
                                if (s.isEnglish) "Share Daily Roster" else "Bagikan Jadwal Harian"
                            ).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
                        )
                    }
                },
                onExportJadwalMatriksPdf = {
                    TopOnAdManager.onUserActionClick(context)
                    val monthEntries = schoolScheduleEntries.filter { it.monthKey == dashboardModalState.jadwalSelectedMonthKey }
                    val pdf = com.example.util.PdfReportGenerator.generateJadwalMatriksPdf(
                        context = context,
                        monthKey = dashboardModalState.jadwalSelectedMonthKey,
                        entries = monthEntries,
                        shiftCodes = schoolShiftCodes,
                        waliName = waliName,
                        schoolProfile = schoolProfile
                    )
                    if (pdf != null) {
                        readyMainJadwalPdf = pdf
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            if (s.isEnglish) "Failed to generate duty roster PDF." else "Gagal membuat PDF matriks jadwal.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                sholatRecords = sholatRecords,
                onOpenAbsenSholatSheet = { tabIdx, autoScan, scopeFilter, targetStudent ->
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.absenSholatInitialTab = tabIdx
                    dashboardModalState.absenSholatAutoOpenScanner = autoScan
                    dashboardModalState.absenSholatInitialScope = scopeFilter
                    dashboardModalState.absenSholatInitialStudent = targetStudent
                    dashboardModalState.showAbsenSholatSheet = true
                },
                christianWorshipRecords = christianWorshipRecords,
                onOpenClaimChristianStudents = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showClaimChristianSheet = true
                },
                onOpenInputChristianWorship = { initialTab, defaultType, targetStudent ->
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.christianInputInitialTab = initialTab
                    dashboardModalState.christianInputDefaultType = defaultType
                    dashboardModalState.christianInputTargetStudent = targetStudent
                    dashboardModalState.showInputChristianWorshipDialog = true
                },
                onOpenChristianReport = { initialTab ->
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.christianReportInitialTab = initialTab
                    dashboardModalState.showChristianReportSheet = true
                },
                onOpenBibleFromKristen = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showBibleSheet = true
                },
                onOpenSundayChurch = {
                    TopOnAdManager.onUserActionClick(context)
                    dashboardModalState.showSundayChurchSheet = true
                }
            )
        }

        if (currentScreen == WaliNavScreen.DASHBOARD) {
            com.example.ui.screens.DashboardBibleModalsHost(
                viewModel = viewModel,
                modalState = dashboardModalState,
                isEn = s.isEnglish,
                useDialogWindow = false
            )
            com.example.ui.screens.DashboardQuranModalsHost(
                modalState = dashboardModalState,
                isEn = s.isEnglish
            )
        }
    }

    readyMainTabunganSummaryPdf?.let { pdfFile ->
        AlertDialog(
            onDismissRequest = { readyMainTabunganSummaryPdf = null },
            title = { Text(if (s.isEnglish) "Savings Recap PDF Ready" else "PDF Rekap Tabungan Siap") },
            text = {
                Text(
                    if (s.isEnglish) "Choose to open the private savings recap PDF or share it."
                    else "Pilih untuk membuka atau membagikan laporan PDF saldo tabungan anak binaan."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        readyMainTabunganSummaryPdf = null
                        com.example.util.PdfReportGenerator.openPdfFile(context, pdfFile)
                    }
                ) {
                    Text(if (s.isEnglish) "Open PDF" else "Buka PDF")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        readyMainTabunganSummaryPdf = null
                        com.example.util.PdfReportGenerator.shareFastingPdf(
                            context = context,
                            pdfFile = pdfFile,
                            subject = if (s.isEnglish) "Private Savings Recap" else "Rekap Tabungan Anak Binaan",
                            text = if (s.isEnglish) "Attached is the private student savings balance recap PDF." else "Terlampir rekap saldo tabungan pribadi wali asuh."
                        )
                    }
                ) {
                    Text(if (s.isEnglish) "Share PDF" else "Bagikan")
                }
            }
        )
    }

    readyMainKonselingSummaryPdf?.let { pdfFile ->
        AlertDialog(
            onDismissRequest = { readyMainKonselingSummaryPdf = null },
            title = { Text(if (s.isEnglish) "Confidential Counseling PDF Ready" else "PDF Rekap Konseling Rahasia Siap") },
            text = {
                Text(
                    if (s.isEnglish) {
                        "Strictly confidential for your Wali Asuh account only. Choose to open or share carefully."
                    } else {
                        "Dokumen bersifat RAHASIA dan hanya untuk Wali Asuh bersangkutan. Pilih untuk membuka atau membagikan secara terbatas."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        readyMainKonselingSummaryPdf = null
                        com.example.util.PdfReportGenerator.openPdfFile(context, pdfFile)
                    }
                ) {
                    Text(if (s.isEnglish) "Open PDF" else "Buka PDF")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        readyMainKonselingSummaryPdf = null
                        com.example.util.PdfReportGenerator.shareFastingPdf(
                            context = context,
                            pdfFile = pdfFile,
                            subject = if (s.isEnglish) "Confidential Counseling Recap" else "Rekap Konseling Rahasia Wali Asuh",
                            text = if (s.isEnglish) "Attached is the confidential foster student counseling recap PDF." else "Terlampir rekap konseling rahasia anak binaan Wali Asuh."
                        )
                    }
                ) {
                    Text(if (s.isEnglish) "Share PDF" else "Bagikan")
                }
            }
        )
    }

    readyMainJadwalPdf?.let { pdfFile ->
        AlertDialog(
            onDismissRequest = { readyMainJadwalPdf = null },
            title = { Text(if (s.isEnglish) "Duty Roster Matrix PDF Ready" else "PDF Matriks Jadwal Siap") },
            text = {
                Text(
                    if (s.isEnglish) "Choose to open the monthly landscape duty roster PDF or share it."
                    else "Pilih untuk membuka atau membagikan dokumen PDF matriks jadwal resmi bulanan."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        readyMainJadwalPdf = null
                        com.example.util.PdfReportGenerator.openPdfFile(context, pdfFile)
                    }
                ) {
                    Text(if (s.isEnglish) "Open PDF" else "Buka PDF")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        readyMainJadwalPdf = null
                        com.example.util.PdfReportGenerator.shareJadwalPdf(
                            context = context,
                            pdfFile = pdfFile,
                            subject = if (s.isEnglish) "Wali Asuh Duty Roster" else "Jadwal Resmi Wali Asuh",
                            text = if (s.isEnglish) "Attached is the official monthly duty roster matrix PDF." else "Terlampir PDF matriks jadwal kerja Wali Asuh."
                        )
                    }
                ) {
                    Text(if (s.isEnglish) "Share PDF" else "Bagikan")
                }
            }
        )
    }

    // Modal Notifikasi Ringkas saat klik Lonceng Atas (Google Maps Style Bottom Sheet)
    if (showNotifModal) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showNotifModal = false },
            sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = WaliPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (s.isEnglish) "Today's Treatment Reminders" else "Pengingat Berobat Hari Ini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (s.isEnglish) {
                                    "📋 ${activePlans.size} students scheduled for checkup today."
                                } else {
                                    "📋 ${activePlans.size} Anak dijadwalkan periksa hari ini."
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (pendingControls.isNotEmpty()) {
                                Text(
                                    text = if (s.isEnglish) {
                                        "🗓️ ${pendingControls.size} students have follow-up checkups."
                                    } else {
                                        "🗓️ ${pendingControls.size} Anak memiliki jadwal kontrol kembali."
                                    },
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    val referralTargetText = schoolProfile.getAllFaskes().drop(1)
                        .joinToString(" / ")
                        .ifBlank {
                            schoolProfile.primaryFaskesName.ifBlank {
                                if (s.isEnglish) "referral health facility" else "fasilitas kesehatan rujukan"
                            }
                        }
                    Text(
                        text = if (s.isEnglish) {
                            "Ensure students have eaten breakfast and bring their ID/BPJS before departing to $referralTargetText."
                        } else {
                            "Pastikan anak asuh telah sarapan dan membawa identitas/BPJS sebelum diberangkatkan ke $referralTargetText."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Notifikasi & Riwayat Penghapusan Data Siswa (Mengaji / Puasa / Berobat)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notif_deletion_logs_section"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color(0xFFB45309),
                                modifier = Modifier.size(19.dp)
                            )
                            Text(
                                text = if (s.isEnglish) {
                                    "History Deletion Log (Wali Asuh)"
                                } else {
                                    "Notifikasi Hapus Riwayat (Wali Asuh)"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (deletionLogs.isEmpty()) {
                            Text(
                                text = if (s.isEnglish) {
                                    "No student Mengaji, Fasting, or Medical history has been deleted."
                                } else {
                                    "Belum ada penghapusan riwayat Mengaji, Puasa, maupun Berobat."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            deletionLogs.take(20).forEach { log ->
                                val (catLabel, catBg, catFg) = when (log.category.uppercase()) {
                                    com.example.data.model.DeletionLog.CATEGORY_MENGAJI -> Triple(
                                        "Mengaji",
                                        androidx.compose.ui.graphics.Color(0xFFDCFCE7),
                                        androidx.compose.ui.graphics.Color(0xFF166534)
                                    )
                                    com.example.data.model.DeletionLog.CATEGORY_PUASA -> Triple(
                                        if (s.isEnglish) "Fasting" else "Puasa",
                                        androidx.compose.ui.graphics.Color(0xFFFEF3C7),
                                        androidx.compose.ui.graphics.Color(0xFF92400E)
                                    )
                                    com.example.data.model.DeletionLog.CATEGORY_SHOLAT -> Triple(
                                        if (s.isEnglish) "Prayer" else "Sholat",
                                        androidx.compose.ui.graphics.Color(0xFFD1FAE5),
                                        androidx.compose.ui.graphics.Color(0xFF065F46)
                                    )
                                    else -> Triple(
                                        if (s.isEnglish) "Medical" else "Berobat",
                                        androidx.compose.ui.graphics.Color(0xFFFEE2E2),
                                        androidx.compose.ui.graphics.Color(0xFFB91C1C)
                                    )
                                }
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .testTag("notif_deletion_log_item_${log.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(catBg, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = catLabel,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = catFg
                                                )
                                            }
                                            Text(
                                                text = DateUtils.formatDateTime(log.createdAt),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        val clsSuffix = if (log.studentClass.isNotBlank()) " (${log.studentClass})" else ""
                                        Text(
                                            text = "${log.studentName}$clsSuffix",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (log.deletedSummary.isNotBlank()) {
                                            Text(
                                                text = log.deletedSummary,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = if (s.isEnglish) {
                                                "Deleted by: ${log.deletedBy}"
                                            } else {
                                                "Dihapus oleh: ${log.deletedBy}"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = androidx.compose.ui.graphics.Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showNotifModal = false },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(s.nav.legalUnderstoodBtn, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
