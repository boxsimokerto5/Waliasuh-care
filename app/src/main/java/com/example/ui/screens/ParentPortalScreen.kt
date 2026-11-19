package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ChildStudent
import com.example.data.model.FastingRecord
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentExpectation
import com.example.data.model.ParentMessage
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganTransaction
import com.example.data.model.WaliAccount
import com.example.ui.HealthViewModel
import com.example.ui.components.ParentGreetingHeroCard
import com.example.ui.components.ParentMenuTileSpec
import com.example.ui.components.ParentSuperAppIconGrid
import com.example.ui.components.ParentSwipeableHighlightsSection
import com.example.ui.components.StatusBadge
import com.example.ui.components.ParentChatMessagesList
import com.example.ui.components.StudentAvatar
import com.example.ui.dialogs.ChangeParentPasswordDialog
import com.example.ui.dialogs.DeleteParentAccountDialog
import com.example.ui.dialogs.StudentPhotoPreviewDialog
import com.example.ui.dialogs.WaliOfficialProfileSheet
import com.example.util.DateUtils
import com.example.util.StudentClassifier
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.LocalAppStrings

private enum class ParentPortalTab(
    val titleId: String,
    val titleEn: String,
    val icon: ImageVector,
    val tag: String
) {
    HOME("Ringkasan", "Overview", Icons.Default.Home, "parent_tab_home"),
    PROFILE("Profil Anak", "Profile", Icons.Default.Badge, "parent_tab_profile"),
    MENGAJI("Mengaji", "Mengaji", Icons.AutoMirrored.Filled.MenuBook, "parent_tab_mengaji"),
    MEDICAL("Riwayat Sakit", "Medical", Icons.Default.LocalHospital, "parent_tab_medical"),
    FASTING("Puasa", "Fasting", Icons.Default.NightlightRound, "parent_tab_fasting"),
    HAID("Riwayat Haid", "Haid Cycle", Icons.Default.Favorite, "parent_tab_haid"),
    TABUNGAN("Tabungan", "Savings", Icons.Default.AccountBalanceWallet, "parent_tab_tabungan"),
    HARAPAN("Harapan", "Hopes", Icons.Default.VolunteerActivism, "parent_tab_harapan"),
    MESSAGES("Pesan", "Messages", Icons.Default.ChatBubbleOutline, "parent_tab_messages")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPortalScreen(
    viewModel: HealthViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val activeLink by viewModel.activeParentAccessLink.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val mengajiRecords by viewModel.mengajiRecords.collectAsStateWithLifecycle()
    val medicalRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val fastingRecords by viewModel.fastingRecords.collectAsStateWithLifecycle()
    val haidRecords by viewModel.allHaidRecords.collectAsStateWithLifecycle()
    val tabunganRecords by viewModel.parentTabunganTransactions.collectAsStateWithLifecycle()
    val parentMessages by viewModel.parentMessages.collectAsStateWithLifecycle()
    val parentExpectations by viewModel.parentExpectations.collectAsStateWithLifecycle()
    val waliAccounts by viewModel.waliAccounts.collectAsStateWithLifecycle()
    val currentParentStudentId by viewModel.currentParentStudentId.collectAsStateWithLifecycle()
    val isCurrentParentAccountDeleted by viewModel.isCurrentParentAccountDeleted.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    val resolvedStudentId = activeLink?.studentId ?: currentParentStudentId
    val student = remember(students, resolvedStudentId) {
        students.firstOrNull { it.id == resolvedStudentId }
    } ?: remember(activeLink, resolvedStudentId) {
        activeLink?.let { link ->
            val guessedGender = if (link.studentDorm.contains("putri", ignoreCase = true) ||
                link.studentDorm.contains("banat", ignoreCase = true)
            ) "P" else "L"
            ChildStudent(
                id = link.studentId,
                name = link.studentName,
                nis = link.studentNis,
                gender = guessedGender,
                age = 12,
                dormitory = link.studentDorm,
                classGrade = link.studentClass,
                allergies = "-",
                bloodType = "-",
                emergencyContact = link.parentPhone,
                specialNotes = "",
                motherName = link.parentName,
                parentPhone = link.parentPhone,
                assignedWaliUsername = link.assignedWaliUsername,
                assignedWaliName = link.assignedWaliName,
                schoolId = link.schoolId,
                photoUrl = link.studentPhotoUrl,
                photoFallbackUrl = link.studentPhotoFallbackUrl
            )
        }
    }
    val resolvedPhotoUrl = remember(student, activeLink) {
        student?.photoUrl?.takeIf { it.isNotBlank() } ?: activeLink?.studentPhotoUrl.orEmpty()
    }
    val resolvedPhotoFallbackUrl = remember(student, activeLink) {
        student?.photoFallbackUrl?.takeIf { it.isNotBlank() } ?: activeLink?.studentPhotoFallbackUrl.orEmpty()
    }
    val assignedWaliUsername = remember(student, activeLink) {
        student?.assignedWaliUsername?.ifBlank { activeLink?.assignedWaliUsername.orEmpty() }
            ?: activeLink?.assignedWaliUsername.orEmpty()
    }
    val assignedWaliName = remember(student, activeLink) {
        student?.assignedWaliName?.ifBlank { activeLink?.assignedWaliName.orEmpty() }
            ?: activeLink?.assignedWaliName.orEmpty()
    }
    val assignedWaliAccount = remember(waliAccounts, assignedWaliUsername, assignedWaliName) {
        waliAccounts.firstOrNull { acc ->
            (assignedWaliUsername.isNotBlank() && acc.username.equals(assignedWaliUsername, ignoreCase = true)) ||
                (assignedWaliName.isNotBlank() && acc.name.equals(assignedWaliName, ignoreCase = true))
        }
    }
    val resolvedSchoolName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName }
    var showPhotoPreview by remember { mutableStateOf(false) }
    var showWaliProfileSheet by remember { mutableStateOf(false) }

    val isFemaleStudent = remember(student) {
        student != null && StudentClassifier.isFemaleStudent(student)
    }

    val visibleTabs = remember(activeLink, isFemaleStudent) {
        buildList {
            add(ParentPortalTab.HOME)
            if (activeLink?.canViewProfile == true) add(ParentPortalTab.PROFILE)
            if (activeLink?.canViewMengaji == true) add(ParentPortalTab.MENGAJI)
            if (activeLink?.canViewMedical == true) add(ParentPortalTab.MEDICAL)
            if (activeLink?.canViewFasting == true) add(ParentPortalTab.FASTING)
            if (isFemaleStudent && activeLink?.canViewHaid == true) add(ParentPortalTab.HAID)
            if (activeLink?.canViewTabungan == true) add(ParentPortalTab.TABUNGAN)
            add(ParentPortalTab.HARAPAN)
            add(ParentPortalTab.MESSAGES)
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showChangePasswordSheet by remember { mutableStateOf(false) }
    var showDeleteAccountSheet by remember { mutableStateOf(false) }
    val safeTabIndex = selectedTabIndex.coerceIn(0, (visibleTabs.size - 1).coerceAtLeast(0))
    val currentTab = visibleTabs.getOrElse(safeTabIndex) { ParentPortalTab.HOME }

    LaunchedEffect(isCurrentParentAccountDeleted) {
        if (isCurrentParentAccountDeleted) {
            Toast.makeText(
                context,
                if (isEn) {
                    "Your parent account has been deleted with Wali Asuh approval."
                } else {
                    "Akun orang tua Anda telah dihapus atas persetujuan Wali Asuh."
                },
                Toast.LENGTH_LONG
            ).show()
            viewModel.logout()
            onLogout()
        }
    }

    val studentMengaji = remember(mengajiRecords, student, resolvedStudentId) {
        if (student != null) {
            MengajiHelper.recordsForStudent(mengajiRecords, student).sortedByDescending { it.sessionDate }
        } else {
            mengajiRecords.filter { it.studentId == resolvedStudentId }.sortedByDescending { it.sessionDate }
        }
    }
    val studentMedical = remember(medicalRecords, resolvedStudentId) {
        medicalRecords.filter { it.studentId == resolvedStudentId }.sortedByDescending { it.planDate }
    }
    val studentFasting = remember(fastingRecords, resolvedStudentId) {
        fastingRecords.filter { it.studentId == resolvedStudentId }.sortedByDescending { it.sessionDate }
    }
    val studentHaid = remember(haidRecords, student, resolvedStudentId) {
        haidRecords.filter { rec ->
            rec.studentId == resolvedStudentId ||
                (student != null && (
                    (rec.studentNik.isNotBlank() && student.nik.isNotBlank() && rec.studentNik == student.nik) ||
                        rec.studentName.equals(student.name, ignoreCase = true)
                    ))
        }.sortedByDescending { it.startDate }
    }
    val threadMessages = remember(parentMessages, resolvedStudentId) {
        parentMessages.filter { it.studentId == resolvedStudentId }.sortedBy { it.createdAt }
    }
    val studentExpectations = remember(parentExpectations, resolvedStudentId) {
        parentExpectations.filter { it.studentId == resolvedStudentId }.sortedByDescending { it.updatedAt }
    }
    val unreadFromWaliCount = remember(threadMessages) {
        threadMessages.count { !it.isReadByParent && !it.senderRole.equals("PARENT", ignoreCase = true) }
    }

    LaunchedEffect(Unit) {
        viewModel.syncParentRelationsOnlineQuietly()
        viewModel.syncTabunganOnlineQuietly()
    }

    LaunchedEffect(currentTab) {
        if (currentTab == ParentPortalTab.TABUNGAN) {
            viewModel.syncParentRelationsOnlineQuietly()
            viewModel.syncTabunganOnlineQuietly()
        }
    }

    LaunchedEffect(currentTab, resolvedStudentId, threadMessages.size) {
        if (currentTab == ParentPortalTab.MESSAGES && resolvedStudentId > 0L && unreadFromWaliCount > 0) {
            viewModel.markParentMessagesAsRead(resolvedStudentId, "PARENT")
        }
    }

    val bottomNavTabs = remember(visibleTabs) {
        buildList {
            add(ParentPortalTab.HOME)
            if (ParentPortalTab.MENGAJI in visibleTabs) add(ParentPortalTab.MENGAJI)
            if (ParentPortalTab.TABUNGAN in visibleTabs) add(ParentPortalTab.TABUNGAN)
            if (size < 3 && ParentPortalTab.MEDICAL in visibleTabs) add(ParentPortalTab.MEDICAL)
            if (size < 3 && ParentPortalTab.PROFILE in visibleTabs) add(ParentPortalTab.PROFILE)
            if (ParentPortalTab.HARAPAN in visibleTabs) add(ParentPortalTab.HARAPAN)
            add(ParentPortalTab.MESSAGES)
        }.distinct()
    }

    val isSubScreen = currentTab != ParentPortalTab.HOME
    BackHandler(enabled = isSubScreen) {
        selectedTabIndex = 0
    }

    val currentPageTitle = when (currentTab) {
        ParentPortalTab.HOME -> if (isEn) "Parent Care Portal" else "Portal Orang Tua"
        else -> if (isEn) currentTab.titleEn else currentTab.titleId
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_portal_screen"),
        containerColor = Color(0xFFF6F7FB),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3730A3),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    if (isSubScreen) {
                        IconButton(
                            onClick = { selectedTabIndex = 0 },
                            modifier = Modifier.testTag("btn_parent_back_home")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (isEn) "Back to Home" else "Kembali ke Beranda"
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp, end = 4.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_sr_care),
                                contentDescription = "Wali Asuh Care",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                },
                title = {
                    Column {
                        Text(
                            text = currentPageTitle,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = schoolProfile.shortName.ifBlank { schoolProfile.schoolName },
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    Surface(
                        onClick = {
                            val next = if (appLanguage == AppLanguage.INDONESIAN) AppLanguage.ENGLISH else AppLanguage.INDONESIAN
                            viewModel.setAppLanguage(next)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.18f),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = if (appLanguage == AppLanguage.INDONESIAN) "ID" else "EN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            if (activeLink != null) {
                                showChangePasswordSheet = true
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isEn) "Account data is not loaded yet. Please sync first." else "Data akun belum termuat. Silakan sinkronkan terlebih dahulu.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.testTag("btn_parent_change_password")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockReset,
                            contentDescription = if (isEn) "Change Password" else "Ganti Password"
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.syncAllWithCloud { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isSyncing,
                        modifier = Modifier.testTag("btn_parent_sync")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = if (isEn) "Sync" else "Sinkron")
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("btn_parent_logout")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = if (isEn) "Logout" else "Keluar")
                    }
                }
            )
        },
        bottomBar = {
            if (currentTab != ParentPortalTab.MESSAGES) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    bottomNavTabs.forEach { navTab ->
                        val isSelected = currentTab == navTab
                        val navLabel = when (navTab) {
                            ParentPortalTab.HOME -> if (isEn) "Home" else "Beranda"
                            else -> if (isEn) navTab.titleEn else navTab.titleId
                        }
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                val targetIdx = visibleTabs.indexOf(navTab)
                                if (targetIdx >= 0) selectedTabIndex = targetIdx
                            },
                            icon = {
                                if (navTab == ParentPortalTab.MESSAGES && unreadFromWaliCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = Color(0xFFDC2626)) {
                                                Text(
                                                    text = unreadFromWaliCount.toString(),
                                                    fontSize = 9.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(navTab.icon, contentDescription = navLabel, modifier = Modifier.size(22.dp))
                                    }
                                } else {
                                    Icon(navTab.icon, contentDescription = navLabel, modifier = Modifier.size(22.dp))
                                }
                            },
                            label = {
                                Text(
                                    text = navLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF3730A3),
                                selectedTextColor = Color(0xFF3730A3),
                                indicatorColor = Color(0xFFE0E7FF),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(navTab.tag)
                        )
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
            ) {
                val sortedTabungan = remember(tabunganRecords) {
                    tabunganRecords.sortedWith(
                        compareByDescending<TabunganTransaction> { it.transactionDate }
                            .thenByDescending { it.createdAt }
                    )
                }
                val tabunganTotals = remember(sortedTabungan) {
                    TabunganHelper.computeAggregateTotals(sortedTabungan)
                }

                when (currentTab) {
                    ParentPortalTab.HOME -> ParentOverviewContent(
                        student = student,
                        link = activeLink,
                        waliAccount = assignedWaliAccount,
                        schoolName = resolvedSchoolName,
                        isFemaleStudent = isFemaleStudent,
                        mengajiRecords = studentMengaji,
                        medicalRecords = studentMedical,
                        fastingRecords = studentFasting,
                        haidRecords = studentHaid,
                        tabunganTransactions = sortedTabungan,
                        tabunganBalance = tabunganTotals.totalBalance,
                        messages = threadMessages,
                        expectations = studentExpectations,
                        unreadMessageCount = unreadFromWaliCount,
                        onOpenTab = { targetTab ->
                            val idx = visibleTabs.indexOf(targetTab)
                            if (idx >= 0) selectedTabIndex = idx
                        },
                        onOpenRequestDeleteAccount = if (activeLink != null) {
                            { showDeleteAccountSheet = true }
                        } else null,
                        onCancelDeletionRequest = if (activeLink != null) {
                            {
                                viewModel.cancelParentAccountDeletionRequest { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else null,
                        onPreviewPhoto = { showPhotoPreview = true },
                        onOpenWaliProfile = { showWaliProfileSheet = true }
                    )
                    ParentPortalTab.PROFILE -> ParentProfileContent(
                        student = student,
                        link = activeLink,
                        waliAccount = assignedWaliAccount,
                        schoolName = resolvedSchoolName,
                        onOpenWaliProfile = { showWaliProfileSheet = true },
                        onOpenWaliChat = {
                            val idx = visibleTabs.indexOf(ParentPortalTab.MESSAGES)
                            if (idx >= 0) selectedTabIndex = idx
                        },
                        onOpenChangePassword = if (activeLink != null) {
                            { showChangePasswordSheet = true }
                        } else null,
                        onOpenRequestDeleteAccount = if (activeLink != null) {
                            { showDeleteAccountSheet = true }
                        } else null,
                        onCancelDeletionRequest = if (activeLink != null) {
                            {
                                viewModel.cancelParentAccountDeletionRequest { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else null,
                        onPreviewPhoto = { showPhotoPreview = true }
                    )
                    ParentPortalTab.MENGAJI -> ParentMengajiContent(records = studentMengaji)
                    ParentPortalTab.MEDICAL -> ParentMedicalContent(records = studentMedical)
                    ParentPortalTab.FASTING -> ParentFastingContent(records = studentFasting)
                    ParentPortalTab.HAID -> ParentHaidContent(records = studentHaid)
                    ParentPortalTab.TABUNGAN -> ParentTabunganContent(
                        transactions = sortedTabungan,
                        waliName = assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName
                    )
                    ParentPortalTab.HARAPAN -> ParentHarapanContent(
                        student = student,
                        expectations = studentExpectations,
                        onSaveExpectation = { focusTags, charExp, adviceNotes, prayerMsg, existing ->
                            val target = student ?: return@ParentHarapanContent
                            viewModel.saveParentExpectation(
                                student = target,
                                focusTags = focusTags,
                                characterExpectation = charExp,
                                adviceFocusNotes = adviceNotes,
                                parentPrayerMessage = prayerMsg,
                                existingExpectation = existing
                            ) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDeleteExpectation = { exp ->
                            viewModel.deleteParentExpectation(exp) {
                                Toast.makeText(
                                    context,
                                    if (isEn) "Expectation deleted." else "Harapan karakter dihapus.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                    ParentPortalTab.MESSAGES -> ParentChatContent(
                        student = student,
                        link = activeLink,
                        waliAccount = assignedWaliAccount,
                        messages = threadMessages,
                        onOpenWaliProfile = { showWaliProfileSheet = true },
                        onSend = { msgText ->
                            val target = student
                            if (target != null) {
                                viewModel.sendParentMessage(target, msgText, "PARENT")
                            }
                        },
                        onDeleteMessage = { msg ->
                            viewModel.deleteParentMessage(msg)
                        }
                    )
                }
            }
        }
    }

    val currentActiveLink = activeLink
    if (showChangePasswordSheet && currentActiveLink != null) {
        ChangeParentPasswordDialog(
            viewModel = viewModel,
            link = currentActiveLink,
            studentName = student?.name ?: currentActiveLink.studentName,
            onDismiss = { showChangePasswordSheet = false }
        )
    }
    if (showDeleteAccountSheet && currentActiveLink != null) {
        DeleteParentAccountDialog(
            viewModel = viewModel,
            link = currentActiveLink,
            studentName = student?.name ?: currentActiveLink.studentName,
            assignedWaliName = assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName,
            onDismiss = { showDeleteAccountSheet = false }
        )
    }
    if (showPhotoPreview) {
        StudentPhotoPreviewDialog(
            studentName = student?.name ?: currentActiveLink?.studentName ?: "Ananda",
            gender = student?.gender ?: "L",
            subtitle = listOfNotNull(
                (student?.classGrade ?: currentActiveLink?.studentClass)?.takeIf { it.isNotBlank() },
                (student?.dormitory ?: currentActiveLink?.studentDorm)?.takeIf { it.isNotBlank() }
            ).joinToString(" • "),
            photoUrl = resolvedPhotoUrl,
            photoFallbackUrl = resolvedPhotoFallbackUrl,
            onDismiss = { showPhotoPreview = false }
        )
    }
    if (showWaliProfileSheet) {
        WaliOfficialProfileSheet(
            waliName = assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName,
            waliAccount = assignedWaliAccount,
            schoolName = resolvedSchoolName,
            dormitory = student?.dormitory ?: currentActiveLink?.studentDorm.orEmpty(),
            onOpenChat = {
                val idx = visibleTabs.indexOf(ParentPortalTab.MESSAGES)
                if (idx >= 0) selectedTabIndex = idx
            },
            onDismiss = { showWaliProfileSheet = false }
        )
    }
}

@Composable
private fun ParentOverviewContent(
    student: ChildStudent?,
    link: ParentAccessLink?,
    waliAccount: WaliAccount?,
    schoolName: String,
    isFemaleStudent: Boolean,
    mengajiRecords: List<MengajiRecord>,
    medicalRecords: List<MedicalRecord>,
    fastingRecords: List<FastingRecord>,
    haidRecords: List<HaidRecord>,
    tabunganTransactions: List<TabunganTransaction>,
    tabunganBalance: Long,
    messages: List<ParentMessage>,
    expectations: List<ParentExpectation>,
    unreadMessageCount: Int,
    onOpenTab: (ParentPortalTab) -> Unit,
    onOpenRequestDeleteAccount: (() -> Unit)? = null,
    onCancelDeletionRequest: (() -> Unit)? = null,
    onPreviewPhoto: (() -> Unit)? = null,
    onOpenWaliProfile: (() -> Unit)? = null
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language

    val menuTiles = remember(
        link,
        isFemaleStudent,
        mengajiRecords.size,
        medicalRecords.size,
        fastingRecords.size,
        haidRecords,
        tabunganTransactions.size,
        expectations.size,
        unreadMessageCount,
        isEn
    ) {
        buildList {
            if (link?.canViewProfile == true) {
                add(
                    ParentMenuTileSpec(
                        id = "profile",
                        title = if (isEn) "Child Profile" else "Profil Anak",
                        imageRes = R.drawable.img_menu_anak_asuh,
                        accentColor = Color(0xFF4338CA),
                        softBg = Color(0xFFEEF2FF),
                        testTag = "card_parent_summary_profile",
                        onClick = { onOpenTab(ParentPortalTab.PROFILE) }
                    )
                )
            }
            if (link?.canViewMengaji == true) {
                add(
                    ParentMenuTileSpec(
                        id = "mengaji",
                        title = "Mengaji",
                        imageRes = R.drawable.img_menu_mengaji,
                        accentColor = Color(0xFF047857),
                        softBg = Color(0xFFECFDF5),
                        badgeText = if (mengajiRecords.isNotEmpty()) "${mengajiRecords.size}" else null,
                        badgeColor = Color(0xFF059669),
                        testTag = "card_parent_summary_mengaji",
                        onClick = { onOpenTab(ParentPortalTab.MENGAJI) }
                    )
                )
            }
            if (link?.canViewMedical == true) {
                add(
                    ParentMenuTileSpec(
                        id = "medical",
                        title = if (isEn) "Health" else "Kesehatan",
                        imageRes = R.drawable.img_menu_rekam_medis,
                        accentColor = Color(0xFF0284C7),
                        softBg = Color(0xFFF0F9FF),
                        badgeText = if (medicalRecords.isNotEmpty()) "${medicalRecords.size}" else null,
                        badgeColor = Color(0xFF0284C7),
                        testTag = "card_parent_summary_medical",
                        onClick = { onOpenTab(ParentPortalTab.MEDICAL) }
                    )
                )
            }
            if (link?.canViewFasting == true) {
                add(
                    ParentMenuTileSpec(
                        id = "fasting",
                        title = if (isEn) "Fasting" else "Puasa",
                        imageRes = R.drawable.img_menu_input_puasa,
                        accentColor = Color(0xFF0F766E),
                        softBg = Color(0xFFF0FDFA),
                        badgeText = if (fastingRecords.isNotEmpty()) "${fastingRecords.size}" else null,
                        badgeColor = Color(0xFF0F766E),
                        testTag = "card_parent_summary_fasting",
                        onClick = { onOpenTab(ParentPortalTab.FASTING) }
                    )
                )
            }
            if (isFemaleStudent && link?.canViewHaid == true) {
                val activeHaid = haidRecords.firstOrNull { it.isActive }
                add(
                    ParentMenuTileSpec(
                        id = "haid",
                        title = if (isEn) "Haid Cycle" else "Haid & Suci",
                        imageRes = R.drawable.img_menu_daftar_haid,
                        accentColor = Color(0xFFDB2777),
                        softBg = Color(0xFFFDF2F8),
                        badgeText = if (activeHaid != null) "H-${activeHaid.durationDays}" else if (haidRecords.isNotEmpty()) "${haidRecords.size}" else null,
                        badgeColor = Color(0xFFDB2777),
                        testTag = "card_parent_summary_haid",
                        onClick = { onOpenTab(ParentPortalTab.HAID) }
                    )
                )
            }
            if (link?.canViewTabungan == true) {
                add(
                    ParentMenuTileSpec(
                        id = "tabungan",
                        title = if (isEn) "Savings" else "Tabungan",
                        imageRes = R.drawable.img_menu_tabungan,
                        accentColor = Color(0xFFD97706),
                        softBg = Color(0xFFFFFBEB),
                        badgeText = if (tabunganTransactions.isNotEmpty()) "${tabunganTransactions.size}" else null,
                        badgeColor = Color(0xFFD97706),
                        testTag = "card_parent_summary_tabungan",
                        onClick = { onOpenTab(ParentPortalTab.TABUNGAN) }
                    )
                )
            }
            add(
                ParentMenuTileSpec(
                    id = "harapan",
                    title = if (isEn) "Character Hopes" else "Harapan Karakter",
                    imageRes = R.drawable.img_menu_konseling,
                    accentColor = Color(0xFF047857),
                    softBg = Color(0xFFECFDF5),
                    badgeText = if (expectations.isNotEmpty()) "${expectations.size}" else null,
                    badgeColor = Color(0xFF059669),
                    testTag = "card_parent_summary_harapan",
                    onClick = { onOpenTab(ParentPortalTab.HARAPAN) }
                )
            )
            add(
                ParentMenuTileSpec(
                    id = "messages",
                    title = if (isEn) "Chat Wali" else "Pesan Wali",
                    imageRes = R.drawable.img_menu_orang_tua,
                    accentColor = Color(0xFF4F46E5),
                    softBg = Color(0xFFEEF2FF),
                    badgeText = if (unreadMessageCount > 0) "$unreadMessageCount" else null,
                    badgeColor = Color(0xFFDC2626),
                    testTag = "card_parent_summary_messages",
                    onClick = { onOpenTab(ParentPortalTab.MESSAGES) }
                )
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                val hasHeroPhoto = !(student?.photoUrl.isNullOrBlank() &&
                    student?.photoFallbackUrl.isNullOrBlank() &&
                    link?.studentPhotoUrl.isNullOrBlank() &&
                    link?.studentPhotoFallbackUrl.isNullOrBlank())
                ParentGreetingHeroCard(
                    student = student,
                    link = link,
                    unreadMessageCount = unreadMessageCount,
                    isEn = isEn,
                    onOpenChat = { onOpenTab(ParentPortalTab.MESSAGES) },
                    onOpenProfile = if (link?.canViewProfile == true) {
                        { onOpenTab(ParentPortalTab.PROFILE) }
                    } else null,
                    onAvatarClick = if (hasHeroPhoto) onPreviewPhoto else null,
                    waliAccount = waliAccount,
                    schoolName = schoolName,
                    onOpenWaliProfile = onOpenWaliProfile
                )
            }
        }

        if (link?.deletionRequested == true) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("banner_parent_home_deletion_pending"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEn) "Account Deletion Pending Approval" else "Pengajuan Hapus Akun Menunggu Wali Asuh",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                        Text(
                            text = if (isEn) {
                                "Your account will only be permanently deleted once approved by Wali Asuh."
                            } else {
                                "Akun baru akan dihapus permanen setelah disetujui oleh Wali Asuh ananda."
                            },
                            fontSize = 11.5.sp,
                            color = Color(0xFF78350F)
                        )
                        if (link.deletionReason.isNotBlank()) {
                            Text(
                                text = "${if (isEn) "Reason:" else "Alasan:"} ${link.deletionReason}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF92400E)
                            )
                        }
                        if (onCancelDeletionRequest != null) {
                            OutlinedButton(
                                onClick = onCancelDeletionRequest,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFB45309)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_home_parent_cancel_delete")
                            ) {
                                Text(
                                    text = if (isEn) "Cancel Deletion Request" else "Batalkan Pengajuan Hapus",
                                    color = Color(0xFF92400E),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ParentSuperAppIconGrid(
                    tiles = menuTiles,
                    isEn = isEn
                )
            }
        }

        item {
            ParentSwipeableHighlightsSection(
                link = link,
                isFemaleStudent = isFemaleStudent,
                mengajiRecords = mengajiRecords,
                medicalRecords = medicalRecords,
                fastingRecords = fastingRecords,
                haidRecords = haidRecords,
                tabunganTransactions = tabunganTransactions,
                tabunganBalance = tabunganBalance,
                messages = messages,
                expectations = expectations,
                isEn = isEn,
                lang = lang,
                onOpenMengaji = { onOpenTab(ParentPortalTab.MENGAJI) },
                onOpenTabungan = { onOpenTab(ParentPortalTab.TABUNGAN) },
                onOpenMedical = { onOpenTab(ParentPortalTab.MEDICAL) },
                onOpenFasting = { onOpenTab(ParentPortalTab.FASTING) },
                onOpenHaid = { onOpenTab(ParentPortalTab.HAID) },
                onOpenMessages = { onOpenTab(ParentPortalTab.MESSAGES) },
                onOpenHarapan = { onOpenTab(ParentPortalTab.HARAPAN) }
            )
        }

        if (link != null && !link.deletionRequested && onOpenRequestDeleteAccount != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("card_parent_home_delete_account"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isEn) "Portal Account Control" else "Pengaturan Akun Portal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEn) {
                                "Need to remove this Parent Portal login? Submission requires Wali Asuh approval first."
                            } else {
                                "Ingin menghapus akun login Portal Orang Tua? Pengajuan memerlukan persetujuan Wali Asuh terlebih dahulu."
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = onOpenRequestDeleteAccount,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFDC2626)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_home_parent_delete_account")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Request Account Deletion" else "Ajukan Hapus Akun",
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun maskIdNumber(raw: String): String {
    val clean = raw.trim()
    if (clean.isBlank()) return "-"
    if (clean.length <= 4) return clean
    return "••••••••${clean.takeLast(4)}"
}

@Composable
private fun ParentProfileContent(
    student: ChildStudent?,
    link: ParentAccessLink?,
    waliAccount: WaliAccount?,
    schoolName: String,
    onOpenWaliProfile: (() -> Unit)? = null,
    onOpenWaliChat: (() -> Unit)? = null,
    onOpenChangePassword: (() -> Unit)? = null,
    onOpenRequestDeleteAccount: (() -> Unit)? = null,
    onCancelDeletionRequest: (() -> Unit)? = null,
    onPreviewPhoto: (() -> Unit)? = null
) {
    val isEn = LocalAppStrings.current.isEnglish
    if (student == null && link == null) {
        ParentEmptyNotice(if (isEn) "Student profile is not available yet." else "Data profil anak belum tersedia.")
        return
    }
    val genderLabel = when {
        student != null && StudentClassifier.isFemaleStudent(student) -> if (isEn) "Female" else "Perempuan"
        else -> if (isEn) "Male" else "Laki-laki"
    }
    val birthPlaceDate = listOfNotNull(
        student?.birthPlace?.takeIf { it.isNotBlank() },
        student?.birthDate?.takeIf { it.isNotBlank() }
    ).joinToString(", ").ifBlank { "-" }
    val profilePhotoUrl = student?.photoUrl?.takeIf { it.isNotBlank() } ?: link?.studentPhotoUrl.orEmpty()
    val profilePhotoFallbackUrl = student?.photoFallbackUrl?.takeIf { it.isNotBlank() }
        ?: link?.studentPhotoFallbackUrl.orEmpty()
    val hasProfilePhoto = profilePhotoUrl.isNotBlank() || profilePhotoFallbackUrl.isNotBlank()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_profile_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { mod ->
                        if (onPreviewPhoto != null) {
                            mod.clickable(onClick = onPreviewPhoto)
                        } else mod
                    }
                    .testTag("card_parent_profile_photo_header"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF312E81), Color(0xFF4338CA), Color(0xFF6D28D9))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(98.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.22f))
                                .let { mod ->
                                    if (onPreviewPhoto != null) {
                                        mod.clickable(onClick = onPreviewPhoto).testTag("btn_parent_preview_student_photo")
                                    } else mod
                                }
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            StudentAvatar(
                                name = student?.name ?: link?.studentName ?: "Ananda",
                                gender = student?.gender ?: "L",
                                size = 92.dp,
                                photoUrl = profilePhotoUrl,
                                photoFallbackUrl = profilePhotoFallbackUrl
                            )
                            if (hasProfilePhoto) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF312E81),
                                    border = BorderStroke(1.5.dp, Color.White),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ZoomIn,
                                            contentDescription = if (isEn) "Zoom photo" else "Perbesar foto",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = student?.name ?: link?.studentName ?: "-",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = listOfNotNull(
                                (student?.classGrade ?: link?.studentClass)?.takeIf { it.isNotBlank() }?.let {
                                    if (isEn) "Class $it" else "Kelas $it"
                                },
                                (student?.dormitory ?: link?.studentDorm)?.takeIf { it.isNotBlank() },
                                (student?.nis ?: link?.studentNis)?.takeIf { it.isNotBlank() }?.let { "NIS $it" }
                            ).joinToString(" • ").ifBlank { "-" },
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
                        ) {
                            Text(
                                text = if (hasProfilePhoto) {
                                    if (isEn) "Tap photo to zoom full size" else "Ketuk foto untuk memperbesar"
                                } else {
                                    if (isEn) "Profile photo not uploaded by Wali Asuh yet" else "Foto profil belum diunggah oleh Wali Asuh"
                                },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFC7D2FE))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isEn) "Student Identity & Boarding" else "Identitas & Asrama Ananda",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4338CA)
                    )
                    ParentProfileRow(if (isEn) "Full Name" else "Nama Lengkap", student?.name ?: link?.studentName ?: "-")
                    ParentProfileRow("NIS / NISN", (student?.nis ?: link?.studentNis).orEmpty().ifBlank { "-" })
                    ParentProfileRow(if (isEn) "Gender" else "Jenis Kelamin", genderLabel)
                    if (student != null && student.age > 0) {
                        ParentProfileRow(if (isEn) "Age" else "Usia", if (isEn) "${student.age} years" else "${student.age} tahun")
                    }
                    ParentProfileRow(if (isEn) "Birth Place & Date" else "Tempat, Tanggal Lahir", birthPlaceDate)
                    ParentProfileRow(
                        if (isEn) "Level & Class" else "Jenjang & Kelas",
                        listOfNotNull(
                            student?.jenjang?.takeIf { it.isNotBlank() },
                            (student?.classGrade ?: link?.studentClass)?.takeIf { it.isNotBlank() }
                        ).joinToString(" • ").ifBlank { "-" }
                    )
                    ParentProfileRow(
                        if (isEn) "Dormitory & Room" else "Asrama & Kamar",
                        listOfNotNull(
                            (student?.dormitory ?: link?.studentDorm)?.takeIf { it.isNotBlank() },
                            student?.roomNumber?.takeIf { it.isNotBlank() }?.let { if (isEn) "Room $it" else "Kamar $it" }
                        ).joinToString(" • ").ifBlank { "-" }
                    )
                    if (!student?.nik.isNullOrBlank()) {
                        ParentProfileRow("NIK", maskIdNumber(student?.nik.orEmpty()))
                    }
                    if (!student?.noKk.isNullOrBlank()) {
                        ParentProfileRow("No. KK", maskIdNumber(student?.noKk.orEmpty()))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isEn) "Health Profile" else "Profil Kesehatan Dasar",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                    ParentProfileRow(
                        if (isEn) "Blood Type" else "Golongan Darah",
                        student?.bloodType?.ifBlank { "-" } ?: "-"
                    )
                    ParentProfileRow(
                        if (isEn) "Allergies" else "Riwayat Alergi",
                        student?.allergies?.ifBlank { if (isEn) "None" else "Tidak ada" } ?: "-"
                    )
                    if (!student?.specialNotes.isNullOrBlank()) {
                        ParentProfileRow(
                            if (isEn) "Special Notes" else "Catatan Khusus",
                            student?.specialNotes.orEmpty()
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isEn) "Family & Address" else "Keluarga, Pembina & Alamat",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857)
                    )
                    ParentProfileRow(
                        if (isEn) "Parent / Mother" else "Nama Ibu / Orang Tua",
                        student?.motherName?.ifBlank { link?.parentName.orEmpty() }?.ifBlank { "-" }
                            ?: link?.parentName?.ifBlank { "-" } ?: "-"
                    )
                    ParentProfileRow(
                        if (isEn) "Parent Phone" else "No. HP Orang Tua",
                        student?.parentPhone?.ifBlank { link?.parentPhone.orEmpty() }?.ifBlank { "-" }
                            ?: link?.parentPhone?.ifBlank { "-" } ?: "-"
                    )
                    ParentProfileRow(
                        if (isEn) "District" else "Kecamatan",
                        student?.district?.ifBlank { "-" } ?: "-"
                    )
                    ParentProfileRow(
                        if (isEn) "Address" else "Alamat",
                        student?.address?.ifBlank { "-" } ?: "-"
                    )
                    ParentProfileRow(
                        if (isEn) "Foster Caregiver" else "Wali Asuh Pembina",
                        waliAccount?.name?.ifBlank {
                            student?.assignedWaliName?.ifBlank { link?.assignedWaliName.orEmpty() }
                                ?: link?.assignedWaliName.orEmpty()
                        }?.ifBlank { "-" } ?: "-"
                    )
                }
            }
        }

        // Kartu Legalitas Resmi Wali Asuh Pembina
        item {
            val waliDisplayName = waliAccount?.name?.takeIf { it.isNotBlank() }
                ?: student?.assignedWaliName?.ifBlank { link?.assignedWaliName.orEmpty() }
                ?: link?.assignedWaliName.orEmpty()
            val waliDorm = waliAccount?.dormitory?.takeIf { it.isNotBlank() }
                ?: student?.dormitory ?: link?.studentDorm.orEmpty()
            val waliAvatarGender = if (waliDorm.contains("putri", ignoreCase = true)) "P" else "L"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { mod -> if (onOpenWaliProfile != null) mod.clickable(onClick = onOpenWaliProfile) else mod }
                    .testTag("card_parent_wali_legitimacy"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isEn) "Verified Foster Caregiver" else "Profil Resmi Wali Asuh",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFFECFDF5)
                        ) {
                            Text(
                                text = if (isEn) "Verified" else "Terverifikasi",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StudentAvatar(
                            name = waliDisplayName.ifBlank { "Wali" },
                            gender = waliAvatarGender,
                            size = 56.dp,
                            photoUrl = waliAccount?.photoUrl.orEmpty(),
                            photoFallbackUrl = waliAccount?.photoFallbackUrl.orEmpty()
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = waliDisplayName.ifBlank { if (isEn) "Not assigned yet" else "Belum ditentukan" },
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = buildString {
                                    append(waliAccount?.effectiveTitle(isEn) ?: if (isEn) "Foster Caregiver" else "Wali Asuh Pembina")
                                    if (waliDorm.isNotBlank()) append(" • $waliDorm")
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val codePhone = listOfNotNull(
                                waliAccount?.employeeCode?.takeIf { it.isNotBlank() }?.let { "ID: $it" },
                                waliAccount?.phone?.takeIf { it.isNotBlank() }?.let { "WA: $it" }
                            )
                            if (codePhone.isNotEmpty()) {
                                Text(
                                    text = codePhone.joinToString(" • "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }

                    if (!waliAccount?.bio.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF0FDF4),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "\"${waliAccount?.bio}\"",
                                fontSize = 11.5.sp,
                                color = Color(0xFF065F46),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onOpenWaliProfile != null) {
                            OutlinedButton(
                                onClick = onOpenWaliProfile,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF047857)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_parent_open_wali_legitimacy")
                            ) {
                                Text(
                                    text = if (isEn) "Official Card" else "Kartu Resmi",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                        if (onOpenWaliChat != null) {
                            Button(
                                onClick = onOpenWaliChat,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3730A3)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_parent_profile_chat_wali")
                            ) {
                                Text(
                                    text = if (isEn) "Message" else "Hubungi",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (link != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_parent_account_security"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isEn) "Portal Account & Security" else "Akun & Keamanan Portal",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3730A3)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "Username (Locked)" else "Username (Terkunci)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = link.username,
                                fontSize = 12.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3730A3)
                            )
                        }
                        if (onOpenChangePassword != null) {
                            OutlinedButton(
                                onClick = onOpenChangePassword,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF3730A3)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_profile_parent_change_password")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockReset,
                                    contentDescription = null,
                                    tint = Color(0xFF3730A3),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEn) "Change Portal Password" else "Ganti Password Portal",
                                    color = Color(0xFF3730A3),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                        if (link.deletionRequested) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_parent_deletion_pending"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassTop,
                                            contentDescription = null,
                                            tint = Color(0xFFB45309),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isEn) "Waiting for Wali Asuh Approval" else "Menunggu Persetujuan Wali Asuh",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                    Text(
                                        text = if (isEn) {
                                            "Your deletion request has been submitted. Account removal will happen only after Wali Asuh approves it."
                                        } else {
                                            "Pengajuan hapus akun sudah dikirim. Akun hanya akan dihapus setelah disetujui oleh Wali Asuh."
                                        },
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF78350F)
                                    )
                                    if (link.deletionReason.isNotBlank()) {
                                        Text(
                                            text = "${if (isEn) "Reason:" else "Alasan:"} ${link.deletionReason}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                    if (link.deletionRequestedAt > 0L) {
                                        Text(
                                            text = "${if (isEn) "Submitted:" else "Diajukan:"} ${DateUtils.formatDateTime(link.deletionRequestedAt, LocalAppStrings.current.language)}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                    if (onCancelDeletionRequest != null) {
                                        OutlinedButton(
                                            onClick = onCancelDeletionRequest,
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Color(0xFFB45309)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_profile_parent_cancel_delete")
                                        ) {
                                            Text(
                                                text = if (isEn) "Cancel Deletion Request" else "Batalkan Pengajuan Hapus",
                                                color = Color(0xFF92400E),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (onOpenRequestDeleteAccount != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (isEn) {
                                        "Account deletion requires confirmation & approval from your child's Wali Asuh."
                                    } else {
                                        "Penghapusan akun hanya dapat diproses setelah disetujui oleh Wali Asuh pembina ananda."
                                    },
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = onOpenRequestDeleteAccount,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFDC2626)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_profile_parent_delete_account")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isEn) "Request Account Deletion" else "Ajukan Hapus Akun",
                                        color = Color(0xFFDC2626),
                                        fontWeight = FontWeight.Bold
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

@Composable
private fun ParentProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
private fun PortalModuleSummaryCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ParentMengajiContent(records: List<MengajiRecord>) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_mengaji_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (records.isEmpty()) {
            item {
                ParentEmptyNotice(if (isEn) "No mengaji records yet." else "Belum ada catatan mengaji.")
            }
        } else {
            items(records, key = { "pmengaji_${it.id}" }) { rec ->
                val isFluent = MengajiHelper.isFluent(rec.fluencyStatus)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (isFluent) Color(0xFFBBF7D0) else Color(0xFFFCA5A5))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = MengajiHelper.formatCompactPosition(rec),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isFluent) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    text = MengajiHelper.fluencyLabel(rec.fluencyStatus, isEn),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFluent) Color(0xFF15803D) else Color(0xFFDC2626),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = "${DateUtils.formatShortDate(rec.sessionDate, s.language)} • ${if (isEn) "Caregiver" else "Pembina"}: ${rec.waliName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (rec.notes.isNotBlank()) {
                            Text(rec.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentMedicalContent(records: List<MedicalRecord>) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_medical_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (records.isEmpty()) {
            item {
                ParentEmptyNotice(if (isEn) "No medical records yet." else "Alhamdulillah, belum ada riwayat sakit.")
            }
        } else {
            items(records, key = { "pmed_${it.id}" }) { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateUtils.formatShortDate(rec.planDate, s.language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatusBadge(status = rec.status)
                        }
                        Text(
                            text = "${if (isEn) "Complaint" else "Keluhan"}: ${rec.complaint.ifBlank { "-" }}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${if (isEn) "Facility" else "Faskes"}: ${rec.faskes}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (rec.diagnosis.isNotBlank()) {
                            Text("${if (isEn) "Diagnosis" else "Diagnosa"}: ${rec.diagnosis}", fontSize = 12.sp)
                        }
                        if (rec.prescription.isNotBlank()) {
                            Text("${if (isEn) "Prescription" else "Resep"}: ${rec.prescription}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentFastingContent(records: List<FastingRecord>) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_fasting_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (records.isEmpty()) {
            item {
                ParentEmptyNotice(if (isEn) "No fasting records yet." else "Belum ada catatan riwayat puasa.")
            }
        } else {
            items(records, key = { "pfast_${it.id}" }) { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFF99F6E4))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(rec.sessionTitle, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                        Text(
                            text = DateUtils.formatShortDate(rec.sessionDate, s.language),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val sahurLabel = if (rec.isSahurChecked) "✓ Sahur" else "○ Sahur"
                            val bukaLabel = if (rec.isBukaChecked) "✓ ${if (isEn) "Iftar" else "Buka"}" else "○ ${if (isEn) "Iftar" else "Buka"}"
                            Text(
                                text = sahurLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (rec.isSahurChecked) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = bukaLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (rec.isBukaChecked) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentHaidContent(records: List<HaidRecord>) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_haid_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (records.isEmpty()) {
            item {
                ParentEmptyNotice(
                    if (isEn) "No haid cycle records yet."
                    else "Belum ada catatan riwayat haid."
                )
            }
        } else {
            val latest = records.first()
            item {
                val statusBannerColor = if (latest.isActive) Color(0xFFFDF2F8) else Color(0xFFF0FDF4)
                val statusBorderColor = if (latest.isActive) Color(0xFFF9A8D4) else Color(0xFF86EFAC)
                val statusTextColor = if (latest.isActive) Color(0xFFBE185D) else Color(0xFF15803D)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = statusBannerColor),
                    border = BorderStroke(1.dp, statusBorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (latest.isActive) {
                                if (isEn) "Current Status: Active Haid (Day ${latest.durationDays})"
                                else "Status Saat Ini: Sedang Haid (Hari ke-${latest.durationDays})"
                            } else {
                                if (isEn) "Current Status: Suci Period (Day ${latest.suciDayCount})"
                                else "Status Saat Ini: Masa Suci (Hari ke-${latest.suciDayCount})"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor
                        )
                        if (latest.isIstihadhahWarning) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isEn) "Reached 15-day max limit (Istihadhah check recommended)"
                                    else "Mencapai batas 15 hari (perlu pemantauan istihadhah)",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            items(records, key = { "phaid_${it.id}" }) { rec ->
                val isOngoing = rec.isActive
                val accent = if (isOngoing) Color(0xFFDB2777) else Color(0xFF047857)
                val badgeBg = if (isOngoing) Color(0xFFFCE7F3) else Color(0xFFDCFCE7)
                val statusLabel = when {
                    isOngoing -> if (isEn) "Haid Day ${rec.durationDays}" else "Haid Hari ke-${rec.durationDays}"
                    rec.isAutoCompletedByLimit -> if (isEn) "Auto Suci (15d)" else "Suci Otomatis (15 Hr)"
                    else -> if (isEn) "Completed (${rec.durationDays}d)" else "Suci (${rec.durationDays} Hari)"
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (isEn) "Start" else "Mulai"}: ${DateUtils.formatShortDate(rec.startDate, s.language)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(shape = RoundedCornerShape(6.dp), color = badgeBg) {
                                Text(
                                    text = statusLabel,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        rec.effectiveEndDate?.let { endMillis ->
                            Text(
                                text = "${if (isEn) "Suci Date" else "Tanggal Mandi Suci"}: ${DateUtils.formatShortDate(endMillis, s.language)}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (rec.symptoms.isNotBlank() && rec.symptoms != "-") {
                            Text(
                                text = "${if (isEn) "Symptoms / Condition" else "Keluhan / Kondisi"}: ${rec.symptoms}",
                                fontSize = 12.sp
                            )
                        }
                        if (rec.notes.isNotBlank()) {
                            Text(
                                text = "${if (isEn) "Notes" else "Catatan"}: ${rec.notes}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${if (isEn) "Recorded by" else "Dicatat oleh"}: ${rec.waliName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentChatContent(
    student: ChildStudent?,
    link: ParentAccessLink?,
    waliAccount: WaliAccount?,
    messages: List<ParentMessage>,
    onOpenWaliProfile: (() -> Unit)? = null,
    onSend: (String) -> Unit,
    onDeleteMessage: (ParentMessage) -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val waliDisplay = waliAccount?.name?.takeIf { it.isNotBlank() }
            ?: student?.assignedWaliName?.ifBlank { link?.assignedWaliName.orEmpty() }
            ?: link?.assignedWaliName.orEmpty()
        val waliAvatarGender = if (
            (waliAccount?.dormitory ?: student?.dormitory ?: link?.studentDorm.orEmpty()).contains("putri", ignoreCase = true)
        ) "P" else "L"
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .let { mod ->
                    if (onOpenWaliProfile != null) mod.clickable(onClick = onOpenWaliProfile) else mod
                }
                .testTag("header_parent_chat_wali")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StudentAvatar(
                    name = waliDisplay.ifBlank { "Wali" },
                    gender = waliAvatarGender,
                    size = 34.dp,
                    photoUrl = waliAccount?.photoUrl.orEmpty(),
                    photoFallbackUrl = waliAccount?.photoFallbackUrl.orEmpty()
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = waliDisplay.ifBlank { if (isEn) "Foster Caregiver" else "Wali Asuh" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (waliAccount != null) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = if (isEn) "Verified" else "Terverifikasi",
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = buildString {
                            append(waliAccount?.effectiveTitle(isEn) ?: if (isEn) "Foster Caregiver" else "Wali Asuh Pembina")
                            if (onOpenWaliProfile != null) {
                                append(" • ")
                                append(if (isEn) "Tap for official profile" else "Ketuk lihat profil resmi")
                            }
                        },
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        ParentChatMessagesList(
            messages = messages,
            myRole = "PARENT",
            isEn = isEn,
            lang = s.language,
            ownBubbleColor = Color(0xFF4338CA),
            listState = listState,
            onDeleteMessage = onDeleteMessage,
            emptyContent = {
                ParentEmptyNotice(
                    if (isEn) "No messages yet. Write a message to your child's caregiver below."
                    else "Belum ada pesan. Tulis pesan untuk wali asuh ananda di bawah."
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text(if (isEn) "Write message to caregiver..." else "Tulis pesan ke wali asuh...", fontSize = 12.5.sp) },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_parent_portal_message")
            )
            IconButton(
                onClick = {
                    if (draft.isNotBlank()) {
                        onSend(draft.trim())
                        draft = ""
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF4338CA),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("btn_parent_portal_send")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = if (isEn) "Send" else "Kirim")
            }
        }
    }
}

@Composable
private fun ParentTabunganContent(
    transactions: List<TabunganTransaction>,
    waliName: String
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val totals = remember(transactions) { TabunganHelper.computeAggregateTotals(transactions) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_tabungan_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isEn) "Current Allowance Balance" else "Saldo Tabungan Titipan Anak",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = TabunganHelper.formatRupiah(totals.totalBalance),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB45309)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Total Deposit (+)" else "Total Titip Masuk (+)",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = TabunganHelper.formatRupiah(totals.totalIn),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Total Expense (-)" else "Total Pengeluaran (-)",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = TabunganHelper.formatRupiah(totals.totalOut),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                    Text(
                        text = if (isEn) {
                            "Managed by: ${waliName.ifBlank { "Foster Caregiver" }} • Shared strictly with parent permission"
                        } else {
                            "Pemegang Titipan: ${waliName.ifBlank { "Wali Asuh Pembina" }} • Akses khusus atas izin Wali Asuh"
                        },
                        fontSize = 10.5.sp,
                        color = Color(0xFF78350F)
                    )
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                ParentEmptyNotice(
                    if (isEn) "No savings transactions recorded yet."
                    else "Belum ada catatan transaksi tabungan titipan."
                )
            }
        } else {
            items(transactions, key = { "ptab_${it.id}" }) { tx ->
                val isIncome = TabunganHelper.isIncome(tx.transactionType)
                val accent = if (isIncome) Color(0xFF15803D) else Color(0xFFDC2626)
                val badgeBg = if (isIncome) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = tx.title.ifBlank {
                                    if (isIncome) "Titip Masuk" else "Pengeluaran"
                                },
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = DateUtils.formatDateTime(tx.transactionDate),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (tx.notes.isNotBlank()) {
                                Text(
                                    text = tx.notes,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = badgeBg) {
                            Text(
                                text = "${if (isIncome) "+" else "-"}${TabunganHelper.formatRupiah(tx.amount)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentEmptyNotice(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
