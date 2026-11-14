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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MedicalRecordScreen
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
    val dashboardModalState = rememberDashboardModalState()
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
                notificationCount = pendingControls.size + activePlans.size,
                onNotificationClick = {
                    TopOnAdManager.onUserActionClick(context)
                    showNotifModal = true
                },
                onProfileClick = {
                    TopOnAdManager.onUserActionClick(context)
                    currentScreen = WaliNavScreen.PROFIL
                },
                isDoctorIcon = currentScreen == WaliNavScreen.TINDAKAN,
                schoolName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName }
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

                    // 3. Tindakan / Rekam Medis (from Mockup 2 & 4)
                    NavigationBarItem(
                        selected = currentScreen == WaliNavScreen.TINDAKAN,
                        onClick = {
                            TopOnAdManager.onUserActionClick(context)
                            currentScreen = WaliNavScreen.TINDAKAN
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = s.nav.navMedical,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = s.nav.navMedical,
                                fontWeight = if (currentScreen == WaliNavScreen.TINDAKAN) FontWeight.Bold else FontWeight.Medium,
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
                        modifier = Modifier.testTag("nav_item_tindakan")
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
                        renderInlineCategoryHub = false
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
                        onLogout = onLogout
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
                }
            )
        }
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
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showNotifModal = false },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                        ) {
                            Text(s.nav.legalUnderstoodBtn, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
