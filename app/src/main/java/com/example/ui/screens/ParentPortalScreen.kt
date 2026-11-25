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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.data.model.ChristianWorshipHelper
import com.example.data.model.ChristianWorshipLocalSave
import com.example.data.model.ChristianWorshipRecord
import com.example.data.model.FastingRecord
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.MengajiHelper
import com.example.data.model.MengajiRecord
import com.example.data.model.ParentAccessLink
import com.example.data.model.ParentExpectation
import com.example.data.model.ParentMessage
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.example.data.model.ResolvedWaliBadgeInfo
import com.example.data.model.SchoolProfile
import com.example.data.model.SholatDisciplineTag
import com.example.data.model.SholatHelper
import com.example.data.model.SholatRecord
import com.example.data.model.SholatStatus
import com.example.data.model.SholatWaktu
import com.example.data.model.StudentReligionHelper
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganTransaction
import com.example.data.model.TandemWaliHelper
import com.example.data.model.VisitConfirmation
import com.example.data.model.VisitHelper
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionSwap
import com.example.data.model.WaliAccount
import com.example.data.model.WaliGalleryHelper
import com.example.data.model.WaliGalleryLocalSave
import com.example.data.model.WaliGalleryPost
import com.example.ui.HealthViewModel
import com.example.ui.components.ParentGreetingHeroCard
import com.example.ui.components.ParentMenuPdfHeaderCard
import com.example.ui.components.ParentMenuTileSpec
import com.example.ui.components.ParentPdfReadyDialog
import com.example.ui.components.ParentReadyPdfDialogData
import com.example.ui.components.ParentSuperAppIconGrid
import com.example.ui.components.ParentSwipeableHighlightsSection
import com.example.ui.components.StatusBadge
import com.example.ui.components.ParentChatMessagesList
import com.example.ui.components.StudentAvatar
import com.example.ui.components.StudentLargeProfilePhoto
import com.example.ui.dialogs.ChangeParentPasswordDialog
import com.example.ui.dialogs.DeleteParentAccountDialog
import com.example.ui.dialogs.StudentPhotoPreviewDialog
import com.example.ui.dialogs.WaliOfficialProfileSheet
import com.example.ui.dialogs.sholatStatusColors
import com.example.util.DateUtils
import com.example.util.ParentPortalPdfHelper
import com.example.util.PdfReportGenerator
import com.example.util.StudentClassifier
import com.example.util.i18n.AppLanguage
import com.example.util.i18n.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class ParentPortalTab(
    val titleId: String,
    val titleEn: String,
    val icon: ImageVector,
    val tag: String
) {
    HOME("Ringkasan", "Overview", Icons.Default.Home, "parent_tab_home"),
    PROFILE("Profil Anak", "Profile", Icons.Default.Badge, "parent_tab_profile"),
    KRISTEN("Agama Kristen", "Christian", Icons.Default.Church, "parent_tab_kristen"),
    SHOLAT("Sholat 5 Waktu", "Prayers", Icons.Default.AutoAwesome, "parent_tab_sholat"),
    MENGAJI("Mengaji", "Mengaji", Icons.AutoMirrored.Filled.MenuBook, "parent_tab_mengaji"),
    MEDICAL("Riwayat Sakit", "Medical", Icons.Default.LocalHospital, "parent_tab_medical"),
    FASTING("Puasa", "Fasting", Icons.Default.NightlightRound, "parent_tab_fasting"),
    HAID("Riwayat Haid", "Haid Cycle", Icons.Default.Favorite, "parent_tab_haid"),
    TABUNGAN("Tabungan", "Savings", Icons.Default.AccountBalanceWallet, "parent_tab_tabungan"),
    VISIT("Kunjungan", "Visits", Icons.Default.EventAvailable, "parent_tab_visit"),
    GALLERY("Galeri Wali", "Gallery", Icons.Default.PhotoLibrary, "parent_tab_gallery"),
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
    val sholatRecords by viewModel.sholatRecords.collectAsStateWithLifecycle()
    val medicalRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val fastingRecords by viewModel.fastingRecords.collectAsStateWithLifecycle()
    val haidRecords by viewModel.allHaidRecords.collectAsStateWithLifecycle()
    val christianWorshipRecords by viewModel.christianWorshipRecords.collectAsStateWithLifecycle()
    val christianWorshipLocalSaves by viewModel.christianWorshipLocalSaves.collectAsStateWithLifecycle()
    val isSavingChristianMedia by viewModel.isSavingChristianMedia.collectAsStateWithLifecycle()
    val tabunganRecords by viewModel.parentTabunganTransactions.collectAsStateWithLifecycle()
    val parentMessages by viewModel.parentMessages.collectAsStateWithLifecycle()
    val parentExpectations by viewModel.parentExpectations.collectAsStateWithLifecycle()
    val waliGalleryPosts by viewModel.waliGalleryPosts.collectAsStateWithLifecycle()
    val waliGalleryLocalSaves by viewModel.waliGalleryLocalSaves.collectAsStateWithLifecycle()
    val parentGalleryAutoSaveEnabled by viewModel.parentGalleryAutoSaveEnabled.collectAsStateWithLifecycle()
    val isSavingGalleryMedia by viewModel.isSavingGalleryMedia.collectAsStateWithLifecycle()
    val chatMediaLocalSaves by viewModel.chatMediaLocalSaves.collectAsStateWithLifecycle()
    val chatAutoSaveMediaEnabled by viewModel.chatAutoSaveMediaEnabled.collectAsStateWithLifecycle()
    val isSendingChatPhoto by viewModel.isSendingChatPhoto.collectAsStateWithLifecycle()
    val isSavingChatMedia by viewModel.isSavingChatMedia.collectAsStateWithLifecycle()
    val chatLocalSavesByMsgId = remember(chatMediaLocalSaves) {
        chatMediaLocalSaves.associateBy { it.messageId }
    }
    val visitSessions by viewModel.parentVisibleVisitSessions.collectAsStateWithLifecycle()
    val allVisitSessions by viewModel.visitSessions.collectAsStateWithLifecycle()
    val visitRecords by viewModel.visitRecords.collectAsStateWithLifecycle()
    val visitConfirmations by viewModel.visitConfirmations.collectAsStateWithLifecycle()
    val visitSessionSwaps by viewModel.visitSessionSwaps.collectAsStateWithLifecycle()
    val waliAccounts by viewModel.waliAccounts.collectAsStateWithLifecycle()
    val schoolShiftCodes by viewModel.schoolShiftCodes.collectAsStateWithLifecycle()
    val schoolScheduleEntries by viewModel.schoolScheduleEntries.collectAsStateWithLifecycle()
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
                district = link.studentDistrict,
                allergies = "-",
                bloodType = "-",
                emergencyContact = link.parentPhone,
                specialNotes = "",
                motherName = link.parentName,
                parentPhone = link.parentPhone,
                assignedWaliUsername = link.assignedWaliUsername,
                assignedWaliName = link.assignedWaliName,
                tandemWalisJson = link.tandemWalisJson,
                schoolId = link.schoolId,
                religion = link.studentReligion,
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
    val resolvedWaliBadges = remember(
        student,
        activeLink,
        waliAccounts,
        schoolScheduleEntries,
        schoolShiftCodes,
        schoolProfile.tenantCode
    ) {
        TandemWaliHelper.resolveWaliBadges(
            student = student,
            link = activeLink,
            waliAccounts = waliAccounts,
            scheduleEntries = schoolScheduleEntries,
            shiftCodes = schoolShiftCodes,
            schoolId = schoolProfile.tenantCode
        )
    }
    val assignedWaliAccount = remember(resolvedWaliBadges, waliAccounts, assignedWaliUsername, assignedWaliName) {
        resolvedWaliBadges.firstOrNull { it.isPrimary }?.account
            ?: resolvedWaliBadges.firstOrNull()?.account
            ?: waliAccounts.firstOrNull { acc ->
                (assignedWaliUsername.isNotBlank() && acc.username.equals(assignedWaliUsername, ignoreCase = true)) ||
                    (assignedWaliName.isNotBlank() && acc.name.equals(assignedWaliName, ignoreCase = true))
            }
    }
    val allAssignedWaliRefs = remember(student, activeLink) {
        TandemWaliHelper.allAssignedWaliRefs(student, activeLink)
    }
    val resolvedSchoolName = schoolProfile.schoolName.ifBlank { schoolProfile.shortName }
    var showPhotoPreview by remember { mutableStateOf(false) }
    var selectedWaliBadgeForSheet by remember { mutableStateOf<ResolvedWaliBadgeInfo?>(null) }

    val isFemaleStudent = remember(student) {
        student != null && StudentClassifier.isFemaleStudent(student)
    }
    val isChristianStudent = remember(student, activeLink) {
        StudentReligionHelper.isChristian(student, activeLink)
    }

    val visibleTabs = remember(activeLink, isFemaleStudent, isChristianStudent) {
        buildList {
            add(ParentPortalTab.HOME)
            if (activeLink?.canViewProfile == true) add(ParentPortalTab.PROFILE)
            if (isChristianStudent) {
                add(ParentPortalTab.KRISTEN)
            } else {
                if (activeLink?.canViewSholat == true) add(ParentPortalTab.SHOLAT)
                if (activeLink?.canViewMengaji == true) add(ParentPortalTab.MENGAJI)
            }
            if (activeLink?.canViewMedical == true) add(ParentPortalTab.MEDICAL)
            if (activeLink?.canViewFasting == true) add(ParentPortalTab.FASTING)
            if (isFemaleStudent && activeLink?.canViewHaid == true) add(ParentPortalTab.HAID)
            if (activeLink?.canViewTabungan == true) add(ParentPortalTab.TABUNGAN)
            add(ParentPortalTab.VISIT)
            add(ParentPortalTab.GALLERY)
            add(ParentPortalTab.HARAPAN)
            add(ParentPortalTab.MESSAGES)
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showChangePasswordSheet by remember { mutableStateOf(false) }
    var showDeleteAccountSheet by remember { mutableStateOf(false) }
    var showQuranSheet by remember { mutableStateOf(false) }
    var showBibleSheet by remember { mutableStateOf(false) }
    var readyPdfDialogData by remember { mutableStateOf<ParentReadyPdfDialogData?>(null) }
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
    val studentSholat = remember(sholatRecords, student, resolvedStudentId) {
        if (student != null) {
            SholatHelper.recordsForStudent(sholatRecords, student)
        } else {
            sholatRecords.filter { it.studentId == resolvedStudentId }
                .sortedWith(compareByDescending<SholatRecord> { it.dateKey }.thenByDescending { it.updatedAt })
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
    val parentGalleryPosts = remember(waliGalleryPosts, student, activeLink, assignedWaliUsername, assignedWaliName, allAssignedWaliRefs) {
        waliGalleryPosts.filter { post ->
            WaliGalleryHelper.matchesParentStudent(
                post = post,
                student = student,
                link = activeLink,
                assignedWaliUsername = assignedWaliUsername,
                assignedWaliName = assignedWaliName,
                assignedWalis = allAssignedWaliRefs
            )
        }.sortedByDescending { it.createdAt }
    }
    val localSavesByPostId = remember(waliGalleryLocalSaves) {
        waliGalleryLocalSaves.associateBy { it.postId }
    }
    val studentChristianWorship = remember(christianWorshipRecords, student, resolvedStudentId) {
        if (student != null) {
            ChristianWorshipHelper.recordsForStudent(christianWorshipRecords, student)
        } else {
            christianWorshipRecords.filter { !it.isDeleted && it.studentId == resolvedStudentId }
                .sortedWith(compareByDescending<ChristianWorshipRecord> { it.activityDate }.thenByDescending { it.createdAt })
        }
    }
    val localChristianSavesByRecordId = remember(christianWorshipLocalSaves) {
        christianWorshipLocalSaves.associateBy { it.recordId }
    }
    val galleryRecipientKey = remember(resolvedStudentId, activeLink) {
        WaliGalleryHelper.buildParentRecipientKey(resolvedStudentId, activeLink?.username.orEmpty())
    }

    LaunchedEffect(Unit) {
        viewModel.syncParentRelationsOnlineQuietly()
        viewModel.syncTabunganOnlineQuietly()
        viewModel.syncSholatOnlineQuietly()
        viewModel.syncWaliGalleryOnlineQuietly()
        viewModel.syncChristianWorshipOnlineQuietly()
        viewModel.syncVisitsOnlineQuietly()
    }

    LaunchedEffect(currentTab) {
        if (currentTab == ParentPortalTab.TABUNGAN) {
            viewModel.syncParentRelationsOnlineQuietly()
            viewModel.syncTabunganOnlineQuietly()
        } else if (currentTab == ParentPortalTab.SHOLAT) {
            viewModel.syncSholatOnlineQuietly()
        } else if (currentTab == ParentPortalTab.GALLERY) {
            viewModel.syncWaliGalleryOnlineQuietly()
        } else if (currentTab == ParentPortalTab.KRISTEN) {
            viewModel.syncChristianWorshipOnlineQuietly()
        } else if (currentTab == ParentPortalTab.VISIT) {
            viewModel.syncVisitsOnlineQuietly()
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
            if (ParentPortalTab.KRISTEN in visibleTabs) add(ParentPortalTab.KRISTEN)
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

    var showTopOverflowMenu by remember { mutableStateOf(false) }

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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val schoolLabel = schoolProfile.shortName.ifBlank { schoolProfile.schoolName }
                        if (schoolLabel.isNotBlank()) {
                            Text(
                                text = schoolLabel,
                                fontSize = 10.5.sp,
                                lineHeight = 13.sp,
                                color = Color.White.copy(alpha = 0.88f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
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
                    Box {
                        IconButton(
                            onClick = { showTopOverflowMenu = true },
                            modifier = Modifier.testTag("btn_parent_top_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = if (isEn) "More Options" else "Menu Lainnya"
                            )
                        }
                        DropdownMenu(
                            expanded = showTopOverflowMenu,
                            onDismissRequest = { showTopOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (appLanguage == AppLanguage.INDONESIAN) {
                                            "Bahasa: Indonesia (Ubah ke EN)"
                                        } else {
                                            "Language: English (Switch to ID)"
                                        },
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = Color(0xFF3730A3)
                                    )
                                },
                                onClick = {
                                    showTopOverflowMenu = false
                                    val next = if (appLanguage == AppLanguage.INDONESIAN) {
                                        AppLanguage.ENGLISH
                                    } else {
                                        AppLanguage.INDONESIAN
                                    }
                                    viewModel.setAppLanguage(next)
                                },
                                modifier = Modifier.testTag("btn_parent_toggle_language")
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (isEn) "Change Password" else "Ganti Password",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LockReset,
                                        contentDescription = null,
                                        tint = Color(0xFF3730A3)
                                    )
                                },
                                onClick = {
                                    showTopOverflowMenu = false
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
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (isEn) "Logout" else "Keluar",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFDC2626)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626)
                                    )
                                },
                                onClick = {
                                    showTopOverflowMenu = false
                                    onLogout()
                                },
                                modifier = Modifier.testTag("btn_parent_logout")
                            )
                        }
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
                        waliBadges = resolvedWaliBadges,
                        schoolName = resolvedSchoolName,
                        isFemaleStudent = isFemaleStudent,
                        isChristianStudent = isChristianStudent,
                        mengajiRecords = studentMengaji,
                        sholatRecords = studentSholat,
                        christianWorshipRecords = studentChristianWorship,
                        medicalRecords = studentMedical,
                        fastingRecords = studentFasting,
                        haidRecords = studentHaid,
                        tabunganTransactions = sortedTabungan,
                        tabunganBalance = tabunganTotals.totalBalance,
                        messages = threadMessages,
                        expectations = studentExpectations,
                        galleryPosts = parentGalleryPosts,
                        localSavesByPostId = localSavesByPostId,
                        visitSessions = visitSessions,
                        allVisitSessions = allVisitSessions,
                        visitSessionSwaps = visitSessionSwaps,
                        visitConfirmations = visitConfirmations,
                        visitRecords = visitRecords,
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
                        onOpenWaliProfile = {
                            selectedWaliBadgeForSheet = resolvedWaliBadges.firstOrNull { it.isOnDutyNow }
                                ?: resolvedWaliBadges.firstOrNull()
                                ?: ResolvedWaliBadgeInfo(
                                    username = assignedWaliUsername,
                                    name = assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName,
                                    isPrimary = true,
                                    account = assignedWaliAccount
                                )
                        },
                        onOpenQuran = { showQuranSheet = true },
                        onOpenBible = { showBibleSheet = true },
                        onDownloadComprehensivePdf = if (student != null) {
                            {
                                val teamName = resolvedWaliBadges.joinToString(", ") { it.name }.ifBlank {
                                    assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName
                                }
                                val file = ParentPortalPdfHelper.generateParentComprehensiveReportPdf(
                                    context = context,
                                    student = student,
                                    schoolProfile = schoolProfile,
                                    waliName = teamName.ifBlank { "Wali Asuh" },
                                    isChristianStudent = isChristianStudent,
                                    isFemaleStudent = isFemaleStudent,
                                    sholatRecords = studentSholat,
                                    mengajiRecords = studentMengaji,
                                    christianRecords = studentChristianWorship,
                                    fastingRecords = studentFasting,
                                    haidRecords = studentHaid,
                                    medicalRecords = studentMedical,
                                    tabunganTransactions = sortedTabungan,
                                    expectations = studentExpectations
                                )
                                if (file != null) {
                                    readyPdfDialogData = ParentReadyPdfDialogData(
                                        file = file,
                                        title = if (isEn) "Integrated Report PDF Ready" else "PDF Rapor Terpadu Siap",
                                        description = if (isEn) {
                                            "Open or share the complete development summary for ${student.name}."
                                        } else {
                                            "Pilih buka PDF atau bagikan rapor perkembangan lengkap ${student.name}."
                                        },
                                        shareSubject = if (isEn) "Integrated Report: ${student.name}" else "Rapor Terpadu: ${student.name}",
                                        shareText = if (isEn) {
                                            "Attached integrated Parent Portal PDF report for ${student.name}."
                                        } else {
                                            "Terlampir PDF Rapor Terpadu Portal Orang Tua untuk ${student.name}."
                                        }
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isEn) "Unable to create integrated PDF" else "Gagal membuat PDF Rapor Terpadu",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else null
                    )
                    ParentPortalTab.PROFILE -> ParentProfileContent(
                        student = student,
                        link = activeLink,
                        waliAccount = assignedWaliAccount,
                        waliBadges = resolvedWaliBadges,
                        schoolName = resolvedSchoolName,
                        onOpenWaliProfile = {
                            selectedWaliBadgeForSheet = resolvedWaliBadges.firstOrNull { it.isOnDutyNow }
                                ?: resolvedWaliBadges.firstOrNull()
                                ?: ResolvedWaliBadgeInfo(
                                    username = assignedWaliUsername,
                                    name = assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName,
                                    isPrimary = true,
                                    account = assignedWaliAccount
                                )
                        },
                        onOpenWaliBadge = { badge ->
                            selectedWaliBadgeForSheet = badge
                        },
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
                        onPreviewPhoto = { showPhotoPreview = true },
                        onDownloadComprehensivePdf = if (student != null) {
                            {
                                val teamName = resolvedWaliBadges.joinToString(", ") { it.name }.ifBlank {
                                    assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName
                                }
                                val file = ParentPortalPdfHelper.generateParentComprehensiveReportPdf(
                                    context = context,
                                    student = student,
                                    schoolProfile = schoolProfile,
                                    waliName = teamName.ifBlank { "Wali Asuh" },
                                    isChristianStudent = isChristianStudent,
                                    isFemaleStudent = isFemaleStudent,
                                    sholatRecords = studentSholat,
                                    mengajiRecords = studentMengaji,
                                    christianRecords = studentChristianWorship,
                                    fastingRecords = studentFasting,
                                    haidRecords = studentHaid,
                                    medicalRecords = studentMedical,
                                    tabunganTransactions = sortedTabungan,
                                    expectations = studentExpectations
                                )
                                if (file != null) {
                                    readyPdfDialogData = ParentReadyPdfDialogData(
                                        file = file,
                                        title = if (isEn) "Integrated Report PDF Ready" else "PDF Rapor Terpadu Siap",
                                        description = if (isEn) {
                                            "Open or share the complete development & profile summary for ${student.name}."
                                        } else {
                                            "Pilih buka PDF atau bagikan rapor profil & perkembangan ${student.name}."
                                        },
                                        shareSubject = if (isEn) "Student Report: ${student.name}" else "Rapor Anak: ${student.name}",
                                        shareText = if (isEn) {
                                            "Attached Student Profile & Development Report PDF for ${student.name}."
                                        } else {
                                            "Terlampir PDF Rapor Profil & Perkembangan ${student.name}."
                                        }
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isEn) "Unable to create report PDF" else "Gagal membuat PDF Rapor Anak",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else null
                    )
                    ParentPortalTab.KRISTEN -> ParentChristianWorshipContent(
                        student = student,
                        records = studentChristianWorship,
                        localSavesByRecordId = localChristianSavesByRecordId,
                        schoolProfile = schoolProfile,
                        waliName = assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName,
                        isSavingMedia = isSavingChristianMedia,
                        onSavePhotoToDevice = { record, silentAuto ->
                            viewModel.saveChristianWorshipPhotoToParentDevice(
                                record = record,
                                silentAuto = silentAuto
                            ) { _, msg ->
                                if (!silentAuto) {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onSaveAllPending = { pendingRecords ->
                            viewModel.saveAllPendingChristianPhotosToDevice(pendingRecords) { saved, failed ->
                                val msg = when {
                                    saved > 0 && failed == 0 -> if (isEn) {
                                        "$saved worship photo(s) saved to Phone Gallery!"
                                    } else {
                                        "$saved foto ibadah berhasil disimpan ke Galeri HP!"
                                    }
                                    saved > 0 -> if (isEn) "$saved saved, $failed failed." else "$saved tersimpan, $failed gagal."
                                    failed > 0 -> if (isEn) "Failed to download photos." else "Gagal mengunduh foto ke galeri."
                                    else -> if (isEn) "All photos already saved." else "Semua foto sudah tersimpan."
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onOpenBible = { showBibleSheet = true },
                        onPdfReady = { readyPdfDialogData = it }
                    )
                    ParentPortalTab.SHOLAT -> ParentSholatContent(
                        student = student,
                        records = studentSholat,
                        haidRecords = studentHaid,
                        schoolProfile = schoolProfile,
                        waliName = assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName,
                        onPdfReady = { readyPdfDialogData = it }
                    )
                    ParentPortalTab.MENGAJI -> ParentMengajiContent(
                        student = student,
                        records = studentMengaji,
                        schoolProfile = schoolProfile,
                        waliName = assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName,
                        onOpenQuran = { showQuranSheet = true },
                        onPdfReady = { readyPdfDialogData = it }
                    )
                    ParentPortalTab.MEDICAL -> ParentMedicalContent(
                        student = student,
                        records = studentMedical,
                        schoolProfile = schoolProfile,
                        waliName = assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName,
                        onPdfReady = { readyPdfDialogData = it }
                    )
                    ParentPortalTab.FASTING -> ParentFastingContent(
                        student = student,
                        records = studentFasting,
                        schoolProfile = schoolProfile,
                        waliName = assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName,
                        onPdfReady = { readyPdfDialogData = it }
                    )
                    ParentPortalTab.HAID -> ParentHaidContent(
                        student = student,
                        records = studentHaid,
                        schoolProfile = schoolProfile,
                        waliName = assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName,
                        onPdfReady = { readyPdfDialogData = it }
                    )
                    ParentPortalTab.TABUNGAN -> ParentTabunganContent(
                        student = student,
                        transactions = sortedTabungan,
                        waliName = resolvedWaliBadges.joinToString(", ") { it.name }.ifBlank {
                            assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName
                        },
                        schoolProfile = schoolProfile,
                        onPdfReady = { readyPdfDialogData = it }
                    )
                    ParentPortalTab.VISIT -> ParentVisitContent(
                        student = student,
                        sessions = visitSessions,
                        confirmations = visitConfirmations,
                        visitRecords = visitRecords,
                        allVisitSessions = allVisitSessions,
                        visitSessionSwaps = visitSessionSwaps,
                        defaultParentName = activeLink?.parentName.orEmpty(),
                        schoolProfile = schoolProfile,
                        waliName = resolvedWaliBadges.joinToString(", ") { it.name }.ifBlank {
                            assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName
                        },
                        onSaveConfirmation = { sess, targetStudent, visitDate, visitorName, relation, status, eta, notes ->
                            viewModel.saveVisitConfirmation(
                                session = sess,
                                student = targetStudent,
                                visitDate = visitDate,
                                visitorName = visitorName,
                                visitorRelation = relation,
                                attendanceStatus = status,
                                estimatedArrivalTime = eta,
                                notes = notes,
                                parentUsername = activeLink?.username.orEmpty()
                            ) { _, feedback ->
                                Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onPdfReady = { readyPdfDialogData = it }
                    )
                    ParentPortalTab.GALLERY -> ParentGalleryContent(
                        posts = parentGalleryPosts,
                        localSavesByPostId = localSavesByPostId,
                        waliName = assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName,
                        autoSaveEnabled = parentGalleryAutoSaveEnabled,
                        isSavingMedia = isSavingGalleryMedia,
                        onToggleAutoSave = { enabled ->
                            viewModel.setParentGalleryAutoSaveEnabled(enabled)
                        },
                        onSavePostToDevice = { post, silentAuto ->
                            viewModel.saveGalleryPostToParentDevice(
                                post = post,
                                recipientKey = galleryRecipientKey,
                                silentAuto = silentAuto
                            ) { _, msg ->
                                if (!silentAuto) {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onSaveAllPending = { pendingPosts ->
                            viewModel.saveAllPendingGalleryPostsToDevice(
                                posts = pendingPosts,
                                recipientKey = galleryRecipientKey
                            ) { savedCount, failCount ->
                                val msg = when {
                                    savedCount > 0 && failCount == 0 -> if (isEn) {
                                        "$savedCount photo(s) saved permanently to Phone Gallery!"
                                    } else {
                                        "$savedCount foto berhasil disimpan permanen ke Galeri HP!"
                                    }
                                    savedCount > 0 -> if (isEn) {
                                        "$savedCount saved, $failCount failed."
                                    } else {
                                        "$savedCount tersimpan, $failCount gagal."
                                    }
                                    failCount > 0 -> if (isEn) {
                                        "Failed to download photos."
                                    } else {
                                        "Gagal mengunduh foto ke galeri."
                                    }
                                    else -> if (isEn) {
                                        "All photos are already in your Phone Gallery."
                                    } else {
                                        "Semua foto sudah ada di Galeri HP Anda."
                                    }
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        student = student,
                        schoolProfile = schoolProfile,
                        onPdfReady = { readyPdfDialogData = it }
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
                        },
                        schoolProfile = schoolProfile,
                        waliName = resolvedWaliBadges.joinToString(", ") { it.name }.ifBlank {
                            assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName
                        }
                    )
                    ParentPortalTab.MESSAGES -> ParentChatContent(
                        student = student,
                        link = activeLink,
                        waliAccount = assignedWaliAccount,
                        waliBadges = resolvedWaliBadges,
                        messages = threadMessages,
                        localSavesByMessageId = chatLocalSavesByMsgId,
                        autoSaveMediaEnabled = chatAutoSaveMediaEnabled,
                        isSendingPhoto = isSendingChatPhoto,
                        isSavingMedia = isSavingChatMedia,
                        schoolProfile = schoolProfile,
                        onPdfReady = { readyPdfDialogData = it },
                        onOpenWaliProfile = {
                            selectedWaliBadgeForSheet = resolvedWaliBadges.firstOrNull { it.isOnDutyNow }
                                ?: resolvedWaliBadges.firstOrNull()
                                ?: ResolvedWaliBadgeInfo(
                                    username = assignedWaliUsername,
                                    name = assignedWaliAccount?.name?.ifBlank { assignedWaliName } ?: assignedWaliName,
                                    isPrimary = true,
                                    account = assignedWaliAccount
                                )
                        },
                        onSend = { msgText ->
                            val target = student
                            if (target != null) {
                                viewModel.sendParentMessage(target, msgText, "PARENT")
                            }
                        },
                        onSendPhoto = { uri, caption ->
                            val target = student
                            if (target != null) {
                                viewModel.sendParentMessageWithPhoto(target, uri, caption, "PARENT") { _, feedback ->
                                    Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onToggleAutoSaveMedia = { enabled ->
                            viewModel.setChatAutoSaveMediaEnabled(enabled)
                        },
                        onSavePhotoToGallery = { msg, silent ->
                            viewModel.saveChatPhotoToDevice(msg, "PARENT", silentAuto = silent) { _, feedback ->
                                if (!silent) {
                                    Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onPurgeServerMedia = { msg ->
                            viewModel.purgeChatPhotoServerMediaNow(msg) { _, feedback ->
                                Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
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
    selectedWaliBadgeForSheet?.let { badge ->
        val roleText = if (badge.isPrimary) {
            if (isEn) "Lead Caregiver" else "Wali Utama"
        } else {
            if (isEn) "Tandem Caregiver" else "Wali Tandem"
        }
        WaliOfficialProfileSheet(
            waliName = badge.name.ifBlank { assignedWaliName },
            waliAccount = badge.account ?: assignedWaliAccount,
            schoolName = resolvedSchoolName,
            dormitory = student?.dormitory ?: currentActiveLink?.studentDorm.orEmpty(),
            roleBadgeLabel = roleText,
            shiftInfoLabel = badge.shiftLabel,
            isOnDutyNow = badge.isOnDutyNow,
            onOpenChat = {
                val idx = visibleTabs.indexOf(ParentPortalTab.MESSAGES)
                if (idx >= 0) selectedTabIndex = idx
            },
            onDismiss = { selectedWaliBadgeForSheet = null }
        )
    }
    if (showQuranSheet && !isChristianStudent) {
        com.example.ui.dialogs.QuranReaderSheet(
            initialEnglish = isEn,
            onRecordToMengaji = null,
            onDismiss = { showQuranSheet = false }
        )
    }
    if (showBibleSheet && isChristianStudent) {
        ParentBibleReaderHost(
            viewModel = viewModel,
            isEn = isEn,
            onDismiss = { showBibleSheet = false }
        )
    }
    readyPdfDialogData?.let { pdfData ->
        ParentPdfReadyDialog(
            data = pdfData,
            isEn = isEn,
            onDismiss = { readyPdfDialogData = null }
        )
    }
}

@Composable
private fun ParentOverviewContent(
    student: ChildStudent?,
    link: ParentAccessLink?,
    waliAccount: WaliAccount?,
    waliBadges: List<ResolvedWaliBadgeInfo> = emptyList(),
    schoolName: String,
    isFemaleStudent: Boolean,
    isChristianStudent: Boolean = false,
    mengajiRecords: List<MengajiRecord>,
    sholatRecords: List<SholatRecord> = emptyList(),
    christianWorshipRecords: List<ChristianWorshipRecord> = emptyList(),
    medicalRecords: List<MedicalRecord>,
    fastingRecords: List<FastingRecord>,
    haidRecords: List<HaidRecord>,
    tabunganTransactions: List<TabunganTransaction>,
    tabunganBalance: Long,
    messages: List<ParentMessage>,
    expectations: List<ParentExpectation>,
    galleryPosts: List<WaliGalleryPost> = emptyList(),
    localSavesByPostId: Map<Long, WaliGalleryLocalSave> = emptyMap(),
    visitSessions: List<VisitSession> = emptyList(),
    allVisitSessions: List<VisitSession> = emptyList(),
    visitSessionSwaps: List<VisitSessionSwap> = emptyList(),
    visitConfirmations: List<VisitConfirmation> = emptyList(),
    visitRecords: List<VisitRecord> = emptyList(),
    unreadMessageCount: Int,
    onOpenTab: (ParentPortalTab) -> Unit,
    onOpenRequestDeleteAccount: (() -> Unit)? = null,
    onCancelDeletionRequest: (() -> Unit)? = null,
    onPreviewPhoto: (() -> Unit)? = null,
    onOpenWaliProfile: (() -> Unit)? = null,
    onOpenQuran: () -> Unit = {},
    onOpenBible: () -> Unit = {},
    onDownloadComprehensivePdf: (() -> Unit)? = null
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val lang = s.language

    val matchedActiveVisitSessions = remember(visitSessions, allVisitSessions, visitSessionSwaps, student) {
        if (student == null) emptyList()
        else {
            val allSess = allVisitSessions.ifEmpty { visitSessions }
            visitSessions
                .filter {
                    it.publishedToParent &&
                        it.isActive &&
                        VisitHelper.isStudentEffectiveInSession(it, student, visitSessionSwaps, allSess)
                }
                .sortedBy { it.getVisitDatesList().firstOrNull() ?: Long.MAX_VALUE }
        }
    }
    val nextVisitSession = matchedActiveVisitSessions.firstOrNull()
    val nextVisitIncomingSwap = remember(nextVisitSession, student, visitSessionSwaps) {
        val sess = nextVisitSession ?: return@remember null
        val targetStudent = student ?: return@remember null
        VisitHelper.findIncomingSwapToSession(sess.id, targetStudent, visitSessionSwaps)
    }
    val nextVisitConf = remember(nextVisitSession, visitConfirmations, student) {
        val sess = nextVisitSession ?: return@remember null
        val targetStudent = student ?: return@remember null
        val d = sess.getVisitDatesList().firstOrNull()
        VisitHelper.findConfirmationForStudent(visitConfirmations, sess.id, targetStudent, d)
    }

    val menuTiles = remember(
        link,
        isFemaleStudent,
        isChristianStudent,
        mengajiRecords.size,
        sholatRecords,
        christianWorshipRecords.size,
        medicalRecords.size,
        fastingRecords.size,
        haidRecords,
        tabunganTransactions.size,
        expectations.size,
        galleryPosts,
        localSavesByPostId,
        matchedActiveVisitSessions,
        nextVisitConf,
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
            if (isChristianStudent) {
                add(
                    ParentMenuTileSpec(
                        id = "kristen",
                        title = if (isEn) "Christian" else "Agama Kristen",
                        imageRes = R.drawable.img_menu_kristen,
                        accentColor = Color(0xFF4338CA),
                        softBg = Color(0xFFEEF2FF),
                        badgeText = if (christianWorshipRecords.isNotEmpty()) "${christianWorshipRecords.size}" else if (isEn) "Active" else "Aktif",
                        badgeColor = Color(0xFF4338CA),
                        testTag = "card_parent_summary_kristen",
                        onClick = { onOpenTab(ParentPortalTab.KRISTEN) }
                    )
                )
                add(
                    ParentMenuTileSpec(
                        id = "bible",
                        title = if (isEn) "Digital Bible" else "Alkitab Digital",
                        imageRes = R.drawable.img_menu_bible,
                        accentColor = Color(0xFFB45309),
                        softBg = Color(0xFFFFFBEB),
                        badgeText = if (isEn) "66 Books" else "66 Kitab",
                        badgeColor = Color(0xFFB45309),
                        testTag = "card_parent_summary_bible",
                        onClick = onOpenBible
                    )
                )
            }
            if (!isChristianStudent && link?.canViewSholat == true) {
                val todayKey = SholatHelper.todayDateKey()
                val todayPrayerCount = sholatRecords.count { it.dateKey == todayKey }
                add(
                    ParentMenuTileSpec(
                        id = "sholat",
                        title = if (isEn) "5 Prayers" else "Pantauan Sholat",
                        imageRes = R.drawable.img_menu_sholat,
                        accentColor = Color(0xFF0F766E),
                        softBg = Color(0xFFF0FDFA),
                        badgeText = if (todayPrayerCount > 0) "$todayPrayerCount/5" else if (sholatRecords.isNotEmpty()) "${sholatRecords.size}" else null,
                        badgeColor = Color(0xFF0D9488),
                        testTag = "card_parent_summary_sholat",
                        onClick = { onOpenTab(ParentPortalTab.SHOLAT) }
                    )
                )
            }
            if (!isChristianStudent && link?.canViewMengaji == true) {
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
            if (!isChristianStudent) {
                add(
                    ParentMenuTileSpec(
                        id = "alquran",
                        title = if (isEn) "Al-Qur'an" else "Al-Qur'an Digital",
                        imageRes = R.drawable.img_menu_alquran,
                        accentColor = Color(0xFFB7791F),
                        softBg = Color(0xFFFFFBEB),
                        badgeText = "114 Surah",
                        badgeColor = Color(0xFFB7791F),
                        testTag = "card_parent_summary_alquran",
                        onClick = onOpenQuran
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
            val unconfirmedVisitCount = if (student == null) 0 else matchedActiveVisitSessions.count { sess ->
                val d = sess.getVisitDatesList().firstOrNull()
                VisitHelper.findConfirmationForStudent(visitConfirmations, sess.id, student, d) == null
            }
            add(
                ParentMenuTileSpec(
                    id = "visit",
                    title = if (isEn) "Visits" else "Kunjungan",
                    imageRes = R.drawable.img_menu_kunjungan,
                    accentColor = Color(0xFF0F766E),
                    softBg = Color(0xFFF0FDFA),
                    badgeText = when {
                        unconfirmedVisitCount > 0 -> if (isEn) "$unconfirmedVisitCount Schedule" else "$unconfirmedVisitCount Jadwal"
                        nextVisitConf?.isHadir == true -> if (isEn) "QR Ready" else "QR Siap"
                        nextVisitConf != null -> if (isEn) "Absent" else "Berhalangan"
                        matchedActiveVisitSessions.isNotEmpty() -> "${matchedActiveVisitSessions.size}"
                        else -> null
                    },
                    badgeColor = when {
                        unconfirmedVisitCount > 0 -> Color(0xFFDC2626)
                        nextVisitConf?.isHadir == true -> Color(0xFF0F766E)
                        else -> Color(0xFFD97706)
                    },
                    testTag = "card_parent_summary_visit",
                    onClick = { onOpenTab(ParentPortalTab.VISIT) }
                )
            )
            val unsavedGalleryCount = galleryPosts.count { post ->
                !post.isMediaPurgedFromServer && localSavesByPostId[post.id] == null &&
                    (post.photoUrl.isNotBlank() || post.photoFallbackUrl.isNotBlank())
            }
            add(
                ParentMenuTileSpec(
                    id = "gallery",
                    title = if (isEn) "Wali Gallery" else "Galeri Wali Asuh",
                    imageRes = R.drawable.img_menu_kunjungan,
                    accentColor = Color(0xFF0F766E),
                    softBg = Color(0xFFF0FDFA),
                    badgeText = when {
                        unsavedGalleryCount > 0 -> "$unsavedGalleryCount ${if (isEn) "New" else "Baru"}"
                        galleryPosts.isNotEmpty() -> "${galleryPosts.size}"
                        else -> null
                    },
                    badgeColor = if (unsavedGalleryCount > 0) Color(0xFFDC2626) else Color(0xFF0F766E),
                    testTag = "card_parent_summary_gallery",
                    onClick = { onOpenTab(ParentPortalTab.GALLERY) }
                )
            )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 14.dp)
            .testTag("scroll_parent_home"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                onOpenWaliProfile = onOpenWaliProfile,
                waliBadges = waliBadges,
                isChristianStudent = isChristianStudent
            )
        }

        if (link?.deletionRequested == true) {
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

        if (nextVisitSession != null) {
            val nextDateMillis = nextVisitSession.getVisitDatesList().firstOrNull() ?: 0L
            val nextDateLabel = if (nextDateMillis > 0L) {
                SimpleDateFormat("EEEE, d MMM yyyy", lang.locale).format(Date(nextDateMillis))
            } else "-"
            val distLabel = student?.let { VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(it), isEn) }.orEmpty()
            Card(
                onClick = { onOpenTab(ParentPortalTab.VISIT) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("banner_parent_home_visit_schedule"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                border = BorderStroke(1.dp, Color(0xFF5EEAD4))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F766E).copy(alpha = 0.14f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isEn) "Parent Visit Invitation" else "Undangan Kunjungan Orang Tua",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F766E)
                            )
                            if (nextVisitIncomingSwap != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEEF2FF),
                                    border = BorderStroke(1.dp, Color(0xFFA5B4FC))
                                ) {
                                    Text(
                                        text = if (isEn) "Rescheduled" else "Jadwal Dipindah",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4338CA),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (nextVisitSession.updatedNoticeAfterParentLaunch) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEF3C7),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                                ) {
                                    Text(
                                        text = if (isEn) "Updated" else "Diperbarui",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${nextVisitSession.title} • $nextDateLabel",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (isEn) {
                                "Visiting Hours: ${nextVisitSession.displayTimeRange()} • $distLabel"
                            } else {
                                "Pukul ${nextVisitSession.displayTimeRange()} • Wilayah $distLabel"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1)
                        )
                        Text(
                            text = when {
                                nextVisitConf?.isHadir == true -> if (isEn) {
                                    "Confirmed attending • Tap to open QR pass"
                                } else {
                                    "Sudah konfirmasi hadir • Ketuk untuk buka barcode"
                                }
                                nextVisitConf != null -> if (isEn) {
                                    "Marked unable to attend • Tap to review"
                                } else {
                                    "Tercatat berhalangan hadir • Ketuk untuk ubah"
                                }
                                else -> if (isEn) {
                                    "District $distLabel scheduled • Tap to confirm & get QR"
                                } else {
                                    "Wilayah $distLabel terjadwal • Ketuk untuk konfirmasi & barcode"
                                }
                            },
                            fontSize = 11.sp,
                            color = if (nextVisitConf == null) Color(0xFFB45309) else Color(0xFF0F766E)
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            ParentSuperAppIconGrid(
                tiles = menuTiles,
                isEn = isEn
            )
        }

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
            sholatRecords = sholatRecords,
            isChristianStudent = isChristianStudent,
            christianWorshipRecords = christianWorshipRecords,
            isEn = isEn,
            lang = lang,
            onOpenMengaji = { onOpenTab(ParentPortalTab.MENGAJI) },
            onOpenSholat = { onOpenTab(ParentPortalTab.SHOLAT) },
            onOpenChristianWorship = { onOpenTab(ParentPortalTab.KRISTEN) },
            onOpenTabungan = { onOpenTab(ParentPortalTab.TABUNGAN) },
            onOpenMedical = { onOpenTab(ParentPortalTab.MEDICAL) },
            onOpenFasting = { onOpenTab(ParentPortalTab.FASTING) },
            onOpenHaid = { onOpenTab(ParentPortalTab.HAID) },
            onOpenMessages = { onOpenTab(ParentPortalTab.MESSAGES) },
            onOpenHarapan = { onOpenTab(ParentPortalTab.HARAPAN) }
        )

        if (onDownloadComprehensivePdf != null) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ParentMenuPdfHeaderCard(
                    title = if (isEn) "Integrated Development Report" else "Rapor Terpadu Ananda",
                    subtitle = if (isEn) {
                        "Download complete worship, health, fasting & savings summary in 1 PDF"
                    } else {
                        "Unduh ringkasan lengkap ibadah, kesehatan, puasa & tabungan dalam 1 PDF"
                    },
                    accentColor = Color(0xFF0F766E),
                    containerColor = Color(0xFFF0FDFA),
                    borderColor = Color(0xFF99F6E4),
                    isEn = isEn,
                    onDownloadPdf = onDownloadComprehensivePdf,
                    pdfButtonTestTag = "btn_parent_home_comprehensive_pdf",
                    buttonLabel = if (isEn) "Full PDF" else "Rapor PDF",
                    modifier = Modifier.testTag("card_parent_home_comprehensive_pdf")
                )
            }
        }

        // Sembunyikan kartu hapus akun di Beranda selama Profil Anak aktif (sudah tersedia di Profil Anak);
        // tampilkan hanya sebagai cadangan bila izin Profil Anak dinonaktifkan.
        if (link != null && !link.canViewProfile && !link.deletionRequested && onOpenRequestDeleteAccount != null) {
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
    waliBadges: List<ResolvedWaliBadgeInfo> = emptyList(),
    schoolName: String,
    onOpenWaliProfile: (() -> Unit)? = null,
    onOpenWaliBadge: ((ResolvedWaliBadgeInfo) -> Unit)? = null,
    onOpenWaliChat: (() -> Unit)? = null,
    onOpenChangePassword: (() -> Unit)? = null,
    onOpenRequestDeleteAccount: (() -> Unit)? = null,
    onCancelDeletionRequest: (() -> Unit)? = null,
    onPreviewPhoto: (() -> Unit)? = null,
    onDownloadComprehensivePdf: (() -> Unit)? = null
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudentLargeProfilePhoto(
                            name = student?.name ?: link?.studentName ?: "Ananda",
                            gender = student?.gender ?: "L",
                            photoUrl = profilePhotoUrl,
                            photoFallbackUrl = profilePhotoFallbackUrl,
                            emptyHint = if (isEn) {
                                "Student profile photo not uploaded yet"
                            } else {
                                "Foto profil ananda belum diunggah"
                            },
                            showZoomBadge = hasProfilePhoto,
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                            shape = RoundedCornerShape(16.dp),
                            borderColor = Color.White.copy(alpha = 0.78f),
                            placeholderAvatarSize = 98.dp,
                            modifier = Modifier
                                .width(204.dp)
                                .aspectRatio(3f / 4f)
                                .let { mod ->
                                    if (onPreviewPhoto != null) {
                                        mod.clickable(onClick = onPreviewPhoto).testTag("btn_parent_preview_student_photo")
                                    } else mod
                                }
                        )
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
        if (onDownloadComprehensivePdf != null) {
            item {
                ParentMenuPdfHeaderCard(
                    title = if (isEn) "Student Profile & Report Card" else "Rapor Profil & Perkembangan",
                    subtitle = if (isEn) {
                        "Download official profile, health info & activity summary"
                    } else {
                        "Unduh biodata resmi, info kesehatan dasar & ringkasan kegiatan ananda"
                    },
                    accentColor = Color(0xFF4338CA),
                    containerColor = Color(0xFFEEF2FF),
                    borderColor = Color(0xFFC7D2FE),
                    isEn = isEn,
                    onDownloadPdf = onDownloadComprehensivePdf,
                    pdfButtonTestTag = "btn_parent_profile_pdf",
                    buttonLabel = if (isEn) "PDF Report" else "Unduh PDF"
                )
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
                    val teamSummaryText = when {
                        waliBadges.isNotEmpty() -> waliBadges.joinToString(", ") { b ->
                            if (b.isPrimary) "${b.name} (${if (isEn) "Lead" else "Utama"})" else "${b.name} (Tandem)"
                        }
                        else -> waliAccount?.name?.ifBlank {
                            student?.assignedWaliName?.ifBlank { link?.assignedWaliName.orEmpty() }
                                ?: link?.assignedWaliName.orEmpty()
                        }?.ifBlank { "-" } ?: "-"
                    }
                    ParentProfileRow(
                        if (isEn) "Caregiver Team" else "Tim Wali Asuh",
                        teamSummaryText
                    )
                }
            }
        }

        // Kartu Legalitas Resmi Tim Wali Asuh (1 Wali Utama + maks. 2 Wali Tandem)
        item {
            val fallbackName = waliAccount?.name?.takeIf { it.isNotBlank() }
                ?: student?.assignedWaliName?.ifBlank { link?.assignedWaliName.orEmpty() }
                ?: link?.assignedWaliName.orEmpty()
            val displayBadges = remember(waliBadges, waliAccount, fallbackName) {
                if (waliBadges.isNotEmpty()) {
                    waliBadges
                } else {
                    listOf(
                        ResolvedWaliBadgeInfo(
                            username = waliAccount?.username.orEmpty(),
                            name = fallbackName,
                            isPrimary = true,
                            account = waliAccount
                        )
                    )
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
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
                                text = if (displayBadges.size > 1) {
                                    if (isEn) "Caregiver Team (${displayBadges.size})" else "Tim Wali Asuh Ananda (${displayBadges.size})"
                                } else {
                                    if (isEn) "Verified Foster Caregiver" else "Profil Resmi Wali Asuh"
                                },
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

                    displayBadges.forEachIndexed { idx, badge ->
                        val acc = badge.account
                        val waliDisplayName = badge.name.ifBlank { if (isEn) "Not assigned yet" else "Belum ditentukan" }
                        val waliDorm = acc?.dormitory?.takeIf { it.isNotBlank() }
                            ?: student?.dormitory ?: link?.studentDorm.orEmpty()
                        val waliAvatarGender = if (waliDorm.contains("putri", ignoreCase = true)) "P" else "L"
                        val openOfficialAction: (() -> Unit)? = when {
                            onOpenWaliBadge != null -> { { onOpenWaliBadge(badge) } }
                            else -> onOpenWaliProfile
                        }

                        if (idx > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_parent_wali_member_${badge.username.ifBlank { idx.toString() }}"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StudentAvatar(
                                    name = waliDisplayName.ifBlank { "Wali" },
                                    gender = waliAvatarGender,
                                    size = 52.dp,
                                    photoUrl = acc?.photoUrl.orEmpty(),
                                    photoFallbackUrl = acc?.photoFallbackUrl.orEmpty()
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = waliDisplayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    @OptIn(ExperimentalLayoutApi::class)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (badge.isPrimary) Color(0xFFEDE9FE) else Color(0xFFCCFBF1)
                                        ) {
                                            Text(
                                                text = if (badge.isPrimary) {
                                                    if (isEn) "Lead Caregiver" else "Wali Utama"
                                                } else {
                                                    if (isEn) "Tandem Caregiver" else "Wali Tandem"
                                                },
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (badge.isPrimary) Color(0xFF5B21B6) else Color(0xFF0F766E),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (badge.isOnDutyNow) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFDCFCE7),
                                                border = BorderStroke(1.dp, Color(0xFF86EFAC))
                                            ) {
                                                Text(
                                                    text = if (isEn) "On Duty Now" else "Sedang Piket",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF15803D),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else if (badge.shiftLabel.isNotBlank()) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFF3F4F6)
                                            ) {
                                                Text(
                                                    text = badge.shiftLabel,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF4B5563),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = buildString {
                                            append(acc?.effectiveTitle(isEn) ?: if (isEn) "Foster Caregiver" else "Wali Asuh Pembina")
                                            if (waliDorm.isNotBlank()) append(" • $waliDorm")
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val codePhone = listOfNotNull(
                                        acc?.employeeCode?.takeIf { it.isNotBlank() }?.let { "ID: $it" },
                                        acc?.phone?.takeIf { it.isNotBlank() }?.let { "WA: $it" }
                                    )
                                    if (codePhone.isNotEmpty()) {
                                        Text(
                                            text = codePhone.joinToString(" • "),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF047857)
                                        )
                                    }
                                }
                            }

                            if (!acc?.bio.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF0FDF4),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "\"${acc?.bio}\"",
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
                                if (openOfficialAction != null) {
                                    OutlinedButton(
                                        onClick = openOfficialAction,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFF047857)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag(if (idx == 0) "btn_parent_open_wali_legitimacy" else "btn_parent_open_wali_legitimacy_${badge.username.ifBlank { idx.toString() }}")
                                    ) {
                                        Text(
                                            text = if (isEn) "Official Card" else "Kartu Resmi",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF047857)
                                        )
                                    }
                                }
                                val waPhoneDigits = acc?.phone?.filter { it.isDigit() }.orEmpty()
                                if (waPhoneDigits.isNotBlank()) {
                                    val parentCtx = LocalContext.current
                                    OutlinedButton(
                                        onClick = {
                                            val waPhone = if (waPhoneDigits.startsWith("0")) "62" + waPhoneDigits.substring(1) else waPhoneDigits
                                            runCatching {
                                                parentCtx.startActivity(
                                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$waPhone"))
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFF15803D)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("btn_parent_wa_wali_${badge.username.ifBlank { idx.toString() }}")
                                    ) {
                                        Text(
                                            text = "WhatsApp",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (onOpenWaliChat != null) {
                        Button(
                            onClick = onOpenWaliChat,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3730A3)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_parent_profile_chat_wali")
                        ) {
                            Text(
                                text = if (isEn) "Message Caregiver Team" else "Hubungi / Pesan Tim Wali Asuh",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
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
private fun ParentSholatContent(
    student: ChildStudent?,
    records: List<SholatRecord>,
    haidRecords: List<HaidRecord>,
    schoolProfile: SchoolProfile,
    waliName: String,
    onPdfReady: ((ParentReadyPdfDialogData) -> Unit)? = null
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val currentMonthKey = remember { SholatHelper.todayDateKey().substring(0, 7) }
    val todayKey = remember { SholatHelper.todayDateKey() }
    var monthOnly by remember { mutableStateOf(true) }
    val selectedMonthKey = if (monthOnly) currentMonthKey else null
    val todayRecords = remember(records, todayKey) { records.filter { it.dateKey == todayKey } }
    val todayByWaktu = remember(todayRecords) { todayRecords.associateBy { SholatWaktu.fromCode(it.waktu) } }

    val analytics = remember(student, records, selectedMonthKey) {
        if (student != null) {
            SholatHelper.computeStudentAnalytics(
                students = listOf(student),
                records = records,
                monthKey = selectedMonthKey
            ).first()
        } else {
            val periodRows = if (selectedMonthKey != null) {
                records.filter { SholatHelper.monthKeyFromDateKey(it.dateKey) == selectedMonthKey }
            } else {
                records
            }
            val j = periodRows.count { SholatStatus.fromCode(it.status) == SholatStatus.JAMAAH }
            val m = periodRows.count { SholatStatus.fromCode(it.status) == SholatStatus.MUNFARID }
            val u = periodRows.count { SholatStatus.fromCode(it.status) == SholatStatus.UDZUR_HAID }
            val sk = periodRows.count { SholatStatus.fromCode(it.status) == SholatStatus.SAKIT }
            val iz = periodRows.count { SholatStatus.fromCode(it.status) == SholatStatus.IZIN }
            val al = periodRows.count { SholatStatus.fromCode(it.status) == SholatStatus.ALFA }
            val ms = periodRows.count { SholatDisciplineTag.fromCode(it.disciplineTag) == SholatDisciplineTag.MASBUK }
            val sd = periodRows.count { SholatDisciplineTag.fromCode(it.disciplineTag) == SholatDisciplineTag.SAF_DEPAN }
            val sbJ = periodRows.count { it.waktu == SholatWaktu.SUBUH.code && it.status == SholatStatus.JAMAAH.code }
            val denom = j + m + sk + iz + al
            val rate = if (denom > 0) ((j * 100f) / denom).toInt() else 0
            com.example.data.model.SholatStudentAnalytics(
                student = ChildStudent(name = "Ananda", nis = "", gender = "L", age = 12, dormitory = "", classGrade = ""),
                totalLogged = periodRows.size,
                jamaahCount = j,
                munfaridCount = m,
                udzurHaidCount = u,
                sakitCount = sk,
                izinCount = iz,
                alfaCount = al,
                masbukCount = ms,
                safDepanCount = sd,
                subuhJamaahCount = sbJ,
                jamaahRatePercent = rate
            )
        }
    }

    val isActiveHaidToday = remember(student, haidRecords, todayKey) {
        if (student != null) {
            SholatHelper.findActiveHaidOnDate(student, haidRecords, todayKey) != null
        } else {
            haidRecords.any { it.isActive }
        }
    }

    val filteredRecords = remember(records, selectedMonthKey) {
        if (selectedMonthKey != null) {
            records.filter { SholatHelper.monthKeyFromDateKey(it.dateKey) == selectedMonthKey }
        } else {
            records
        }
    }
    val groupedByDate = remember(filteredRecords) {
        filteredRecords.groupBy { it.dateKey }.entries.sortedByDescending { it.key }
    }

    fun formatRespectfulStatus(status: SholatStatus): String {
        return if (status == SholatStatus.UDZUR_HAID) {
            if (isEn) "Udzur Syar'i (Haid)" else "Udzur Syar'i (Haid)"
        } else {
            status.label(isEn)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_sholat_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parent_sholat_content"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                border = BorderStroke(1.dp, Color(0xFF99F6E4))
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Today's 5 Daily Prayers" else "Pantauan Sholat Hari Ini",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F766E)
                            )
                            Text(
                                text = SholatHelper.formatDateKeyDisplay(todayKey, isEn),
                                fontSize = 11.5.sp,
                                color = Color(0xFF115E59)
                            )
                        }
                        if (student != null) {
                            OutlinedButton(
                                onClick = {
                                    val file = PdfReportGenerator.generateSholatStudentReportPdf(
                                        context = context,
                                        schoolProfile = schoolProfile,
                                        student = student,
                                        sholatRecords = records,
                                        monthKey = if (monthOnly) currentMonthKey else "ALL",
                                        waliName = waliName.ifBlank { "Wali Asuh" }
                                    )
                                    if (file != null) {
                                        val ready = ParentReadyPdfDialogData(
                                            file = file,
                                            title = if (isEn) "Prayer Report PDF Ready" else "PDF Rapor Sholat Siap",
                                            description = if (isEn) {
                                                "Open or share your child's 5-time prayer report PDF."
                                            } else {
                                                "Pilih buka PDF atau bagikan rapor sholat 5 waktu ananda ${student.name}."
                                            },
                                            shareSubject = if (isEn) "Prayer Report: ${student.name}" else "Rapor Sholat: ${student.name}",
                                            shareText = if (isEn) "Attached 5-time prayer report for ${student.name}." else "Terlampir PDF Rapor Sholat 5 Waktu ${student.name}."
                                        )
                                        if (onPdfReady != null) {
                                            onPdfReady(ready)
                                        } else {
                                            PdfReportGenerator.shareSholatPdf(
                                                context = context,
                                                pdfFile = file,
                                                subject = ready.shareSubject,
                                                text = ready.shareText
                                            )
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            if (isEn) "Unable to create Sholat PDF" else "Gagal membuat PDF Rapor Sholat",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF0F766E)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_parent_sholat_pdf")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFF0F766E),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isEn) "PDF Report" else "Rapor PDF",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SholatWaktu.entries.forEach { w ->
                            val rec = todayByWaktu[w]
                            val st = rec?.let { SholatStatus.fromCode(it.status) }
                            val (bg, fg) = if (st != null) sholatStatusColors(st) else Color.White to Color(0xFF94A3B8)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = w.shortCode,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = fg
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = st?.shortCode ?: "•",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = fg
                                )
                                Text(
                                    text = when (st) {
                                        SholatStatus.JAMAAH -> "Jamaah"
                                        SholatStatus.MUNFARID -> "Sendiri"
                                        SholatStatus.UDZUR_HAID -> "Udzur"
                                        SholatStatus.SAKIT -> "Sakit"
                                        SholatStatus.IZIN -> "Izin"
                                        SholatStatus.ALFA -> "Alfa"
                                        null -> if (isEn) "Pending" else "Belum"
                                    },
                                    fontSize = 9.sp,
                                    color = fg,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    if (isActiveHaidToday) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFDF2F8),
                            border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isEn) {
                                    "Ananda is currently on her menstrual period — respected as Udzur Syar'i (Haid)."
                                } else {
                                    "Ananda sedang berada dalam masa haid — status otomatis dicatat sebagai Udzur Syar'i (Haid)."
                                },
                                fontSize = 11.5.sp,
                                color = Color(0xFF9D174D),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = monthOnly,
                            onClick = { monthOnly = true },
                            label = {
                                Text(
                                    text = if (isEn) "This Month" else "Bulan Ini",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0F766E).copy(alpha = 0.14f),
                                selectedLabelColor = Color(0xFF0F766E)
                            ),
                            modifier = Modifier.testTag("btn_parent_sholat_pdf_filter_month")
                        )
                        FilterChip(
                            selected = !monthOnly,
                            onClick = { monthOnly = false },
                            label = {
                                Text(
                                    text = if (isEn) "All History" else "Semua Riwayat",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0F766E).copy(alpha = 0.14f),
                                selectedLabelColor = Color(0xFF0F766E)
                            ),
                            modifier = Modifier.testTag("btn_parent_sholat_pdf_filter_all")
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
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
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
                        Text(
                            text = if (monthOnly) {
                                if (isEn) {
                                    "Monthly Discipline (${SholatHelper.formatMonthKeyDisplay(currentMonthKey, true)})"
                                } else {
                                    "Statistik Bulan ${SholatHelper.formatMonthKeyDisplay(currentMonthKey, false)}"
                                }
                            } else {
                                if (isEn) "All-Time Prayer Statistics" else "Statistik Semua Riwayat Sholat"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "${analytics.jamaahRatePercent}% Jamaah",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val exc = analytics.udzurHaidCount + analytics.sakitCount + analytics.izinCount
                        listOf<Triple<String, Int, Pair<Color, Color>>>(
                            Triple("Jamaah", analytics.jamaahCount, Color(0xFFDCFCE7) to Color(0xFF166534)),
                            Triple("Subuh J", analytics.subuhJamaahCount, Color(0xFFE0F2FE) to Color(0xFF0369A1)),
                            Triple("Munfarid", analytics.munfaridCount, Color(0xFFFEF3C7) to Color(0xFF92400E)),
                            Triple("Udzur", exc, Color(0xFFFCE7F3) to Color(0xFF9D174D)),
                            Triple("Alfa", analytics.alfaCount, Color(0xFFFEE2E2) to Color(0xFFDC2626))
                        ).forEach { (lbl, cnt, col) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(col.first)
                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(cnt.toString(), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = col.second)
                                Text(lbl, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = col.second, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        if (groupedByDate.isEmpty()) {
            item {
                ParentEmptyNotice(
                    if (isEn) "No 5-time prayer attendance logs yet." else "Belum ada catatan sholat 5 waktu dari pembina."
                )
            }
        } else {
            items(groupedByDate, key = { "psholat_${it.key}" }) { (dateKey, dayRecords) ->
                val byWaktu = dayRecords.associateBy { SholatWaktu.fromCode(it.waktu) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFF99F6E4))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = SholatHelper.formatDateKeyDisplay(dateKey, isEn),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            SholatWaktu.entries.forEach { w ->
                                val rec = byWaktu[w]
                                val st = rec?.let { SholatStatus.fromCode(it.status) }
                                val (bg, fg) = if (st != null) sholatStatusColors(st) else Color(0xFFF1F5F9) to Color(0xFF94A3B8)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .padding(horizontal = 3.dp, vertical = 5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(w.shortCode, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = fg)
                                    Text(st?.shortCode ?: "•", fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = fg)
                                }
                            }
                        }

                        dayRecords.sortedBy { SholatWaktu.fromCode(it.waktu).ordinal }.forEach { rec ->
                            val w = SholatWaktu.fromCode(rec.waktu)
                            val st = SholatStatus.fromCode(rec.status)
                            val tag = SholatDisciplineTag.fromCode(rec.disciplineTag)
                            val tagText = if (tag != SholatDisciplineTag.NONE) " (${tag.label(isEn)})" else ""
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${w.label(isEn)}: ${formatRespectfulStatus(st)}$tagText",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val noteLine = listOfNotNull(
                                    rec.notes.takeIf { it.isNotBlank() },
                                    rec.recordedBy.takeIf { it.isNotBlank() }?.let { "${if (isEn) "Caregiver" else "Pembina"}: $it" }
                                ).joinToString(" • ")
                                if (noteLine.isNotBlank()) {
                                    Text(
                                        text = noteLine,
                                        fontSize = 11.sp,
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
}

private fun isTimestampInCurrentMonth(millis: Long): Boolean {
    if (millis <= 0L) return false
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = millis }
    return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        now.get(Calendar.MONTH) == target.get(Calendar.MONTH)
}

private fun currentMonthPeriodLabel(isEn: Boolean): String {
    val locale = if (isEn) Locale.US else Locale("id", "ID")
    return SimpleDateFormat("MMMM yyyy", locale).format(Date())
}

@Composable
private fun ParentMengajiContent(
    student: ChildStudent?,
    records: List<MengajiRecord>,
    schoolProfile: SchoolProfile,
    waliName: String,
    onOpenQuran: () -> Unit = {},
    onPdfReady: (ParentReadyPdfDialogData) -> Unit = {}
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    var monthOnly by remember { mutableStateOf(true) }
    val filteredRecords = remember(records, monthOnly) {
        if (monthOnly) records.filter { isTimestampInCurrentMonth(it.sessionDate) } else records
    }
    val periodLabel = remember(monthOnly, isEn) {
        if (monthOnly) currentMonthPeriodLabel(isEn) else if (isEn) "All History" else "Semua Riwayat"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_mengaji_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "parent_mengaji_quran_cta") {
            Card(
                onClick = onOpenQuran,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_parent_open_alquran"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF3)),
                border = BorderStroke(1.2.dp, Color(0xFFE2C788))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_menu_alquran),
                        contentDescription = "Al-Qur'an Digital",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Open Al-Qur'an Digital" else "Buka Al-Qur'an Digital",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF74470B)
                        )
                        Text(
                            text = if (isEn) {
                                "Calligraphic Ivory-Gold Mushaf • Latin & Audio"
                            } else {
                                "Mushaf Kaligrafi Emas & Krem Gading • Latin & Audio"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF8B7149)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "114 Surah",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB7791F),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
        if (student != null) {
            item(key = "parent_mengaji_pdf_header") {
                val fluentCount = filteredRecords.count { MengajiHelper.isFluent(it.fluencyStatus) }
                ParentMenuPdfHeaderCard(
                    title = if (isEn) "Mengaji Progress Card" else "Kartu Kendali Mengaji",
                    subtitle = if (isEn) {
                        "${filteredRecords.size} session(s) • $fluentCount fluent ($periodLabel)"
                    } else {
                        "${filteredRecords.size} sesi tercatat • $fluentCount lancar ($periodLabel)"
                    },
                    accentColor = Color(0xFF15803D),
                    containerColor = Color(0xFFF0FDF4),
                    borderColor = Color(0xFFBBF7D0),
                    isEn = isEn,
                    selectedMonthOnly = monthOnly,
                    onToggleMonthOnly = { monthOnly = it },
                    pdfButtonTestTag = "btn_parent_mengaji_pdf",
                    onDownloadPdf = {
                        val file = PdfReportGenerator.generateKartuMengajiPdf(
                            context = context,
                            student = student,
                            records = filteredRecords,
                            periodLabel = periodLabel,
                            waliName = waliName.ifBlank { "Wali Asuh" },
                            schoolProfile = schoolProfile
                        )
                        if (file != null) {
                            onPdfReady(
                                ParentReadyPdfDialogData(
                                    file = file,
                                    title = if (isEn) "Mengaji Card PDF Ready" else "PDF Kartu Mengaji Siap",
                                    description = if (isEn) {
                                        "Open or share ${student.name}'s Mengaji Progress Card ($periodLabel)."
                                    } else {
                                        "Pilih buka PDF atau bagikan Kartu Kendali Mengaji ${student.name} ($periodLabel)."
                                    },
                                    shareSubject = if (isEn) "Mengaji Card: ${student.name}" else "Kartu Mengaji: ${student.name}",
                                    shareText = if (isEn) {
                                        "Attached Mengaji Progress Card PDF for ${student.name} ($periodLabel)."
                                    } else {
                                        "Terlampir PDF Kartu Kendali Mengaji ${student.name} ($periodLabel)."
                                    }
                                )
                            )
                        } else {
                            Toast.makeText(
                                context,
                                if (isEn) "Unable to create Mengaji PDF" else "Gagal membuat PDF Kartu Mengaji",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
        if (filteredRecords.isEmpty()) {
            item {
                ParentEmptyNotice(if (isEn) "No mengaji records yet." else "Belum ada catatan mengaji.")
            }
        } else {
            items(filteredRecords, key = { "pmengaji_${it.id}" }) { rec ->
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
private fun ParentMedicalContent(
    student: ChildStudent?,
    records: List<MedicalRecord>,
    schoolProfile: SchoolProfile,
    waliName: String,
    onPdfReady: (ParentReadyPdfDialogData) -> Unit = {}
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    var monthOnly by remember { mutableStateOf(false) }
    val filteredRecords = remember(records, monthOnly) {
        if (monthOnly) records.filter { isTimestampInCurrentMonth(it.planDate) } else records
    }
    val periodLabel = remember(monthOnly, isEn) {
        if (monthOnly) currentMonthPeriodLabel(isEn) else if (isEn) "All History" else "Semua Riwayat"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_medical_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (student != null) {
            item(key = "parent_medical_pdf_header") {
                ParentMenuPdfHeaderCard(
                    title = if (isEn) "Health & Medical Visits" else "Rekap Kesehatan & Berobat",
                    subtitle = if (isEn) {
                        "${filteredRecords.size} visit record(s) ($periodLabel)"
                    } else {
                        "${filteredRecords.size} riwayat pemeriksaan ($periodLabel)"
                    },
                    accentColor = Color(0xFF0284C7),
                    containerColor = Color(0xFFF0F9FF),
                    borderColor = Color(0xFFBAE6FD),
                    isEn = isEn,
                    selectedMonthOnly = monthOnly,
                    onToggleMonthOnly = { monthOnly = it },
                    pdfButtonTestTag = "btn_parent_medical_pdf",
                    onDownloadPdf = {
                        val file = PdfReportGenerator.generateHealthReportPdf(
                            context = context,
                            student = student,
                            records = filteredRecords,
                            periodType = if (monthOnly) PdfReportGenerator.ReportPeriodType.MONTHLY else PdfReportGenerator.ReportPeriodType.ALL,
                            periodLabel = periodLabel,
                            waliName = waliName.ifBlank { "Wali Asuh" },
                            schoolProfile = schoolProfile
                        )
                        if (file != null) {
                            onPdfReady(
                                ParentReadyPdfDialogData(
                                    file = file,
                                    title = if (isEn) "Medical Report PDF Ready" else "PDF Rekap Kesehatan Siap",
                                    description = if (isEn) {
                                        "Open or share ${student.name}'s health screening & medical visits PDF ($periodLabel)."
                                    } else {
                                        "Pilih buka PDF atau bagikan rekap riwayat kesehatan & berobat ${student.name} ($periodLabel)."
                                    },
                                    shareSubject = if (isEn) "Health Report: ${student.name}" else "Rekap Kesehatan: ${student.name}",
                                    shareText = if (isEn) {
                                        "Attached health & medical visit recap PDF for ${student.name} ($periodLabel)."
                                    } else {
                                        "Terlampir PDF rekap riwayat sakit & berobat ${student.name} ($periodLabel)."
                                    }
                                )
                            )
                        } else {
                            Toast.makeText(
                                context,
                                if (isEn) "Unable to create Medical PDF" else "Gagal membuat PDF Rekap Kesehatan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
        if (filteredRecords.isEmpty()) {
            item {
                ParentEmptyNotice(if (isEn) "No medical records yet." else "Alhamdulillah, belum ada riwayat sakit.")
            }
        } else {
            items(filteredRecords, key = { "pmed_${it.id}" }) { rec ->
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
private fun ParentFastingContent(
    student: ChildStudent?,
    records: List<FastingRecord>,
    schoolProfile: SchoolProfile,
    waliName: String,
    onPdfReady: (ParentReadyPdfDialogData) -> Unit = {}
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    var monthOnly by remember { mutableStateOf(true) }
    val filteredRecords = remember(records, monthOnly) {
        if (monthOnly) records.filter { isTimestampInCurrentMonth(it.sessionDate) } else records
    }
    val periodLabel = remember(monthOnly, isEn) {
        if (monthOnly) currentMonthPeriodLabel(isEn) else if (isEn) "All History" else "Semua Riwayat"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_fasting_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (student != null) {
            item(key = "parent_fasting_pdf_header") {
                val fullDone = filteredRecords.count { it.isSahurChecked && it.isBukaChecked }
                ParentMenuPdfHeaderCard(
                    title = if (isEn) "Fasting Development Recap" else "Rekap Ibadah Puasa Ananda",
                    subtitle = if (isEn) {
                        "${filteredRecords.size} session(s) • $fullDone complete ($periodLabel)"
                    } else {
                        "${filteredRecords.size} sesi puasa • $fullDone tuntas penuh ($periodLabel)"
                    },
                    accentColor = Color(0xFF0F766E),
                    containerColor = Color(0xFFF0FDFA),
                    borderColor = Color(0xFF99F6E4),
                    isEn = isEn,
                    selectedMonthOnly = monthOnly,
                    onToggleMonthOnly = { monthOnly = it },
                    pdfButtonTestTag = "btn_parent_fasting_pdf",
                    onDownloadPdf = {
                        val file = ParentPortalPdfHelper.generateStudentFastingReportPdf(
                            context = context,
                            student = student,
                            records = filteredRecords,
                            periodLabel = periodLabel,
                            waliName = waliName.ifBlank { "Wali Asuh" },
                            schoolProfile = schoolProfile
                        )
                        if (file != null) {
                            onPdfReady(
                                ParentReadyPdfDialogData(
                                    file = file,
                                    title = if (isEn) "Fasting Report PDF Ready" else "PDF Rekap Puasa Siap",
                                    description = if (isEn) {
                                        "Open or share ${student.name}'s fasting history PDF ($periodLabel)."
                                    } else {
                                        "Pilih buka PDF atau bagikan laporan riwayat puasa ${student.name} ($periodLabel)."
                                    },
                                    shareSubject = if (isEn) "Fasting Report: ${student.name}" else "Rekap Puasa: ${student.name}",
                                    shareText = if (isEn) {
                                        "Attached fasting history PDF for ${student.name} ($periodLabel)."
                                    } else {
                                        "Terlampir PDF rekap ibadah puasa ${student.name} ($periodLabel)."
                                    }
                                )
                            )
                        } else {
                            Toast.makeText(
                                context,
                                if (isEn) "Unable to create Fasting PDF" else "Gagal membuat PDF Rekap Puasa",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
        if (filteredRecords.isEmpty()) {
            item {
                ParentEmptyNotice(if (isEn) "No fasting records yet." else "Belum ada catatan riwayat puasa.")
            }
        } else {
            items(filteredRecords, key = { "pfast_${it.id}" }) { rec ->
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
private fun ParentHaidContent(
    student: ChildStudent?,
    records: List<HaidRecord>,
    schoolProfile: SchoolProfile,
    waliName: String,
    onPdfReady: (ParentReadyPdfDialogData) -> Unit = {}
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    var monthOnly by remember { mutableStateOf(false) }
    val filteredRecords = remember(records, monthOnly) {
        if (monthOnly) records.filter { isTimestampInCurrentMonth(it.startDate) } else records
    }
    val periodLabel = remember(monthOnly, isEn) {
        if (monthOnly) currentMonthPeriodLabel(isEn) else if (isEn) "All History" else "Semua Riwayat"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_haid_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (student != null) {
            item(key = "parent_haid_pdf_header") {
                ParentMenuPdfHeaderCard(
                    title = if (isEn) "Haid Cycle Monitoring" else "Pemantauan Siklus Haid Siswi",
                    subtitle = if (isEn) {
                        "${filteredRecords.size} cycle record(s) ($periodLabel)"
                    } else {
                        "${filteredRecords.size} siklus tercatat ($periodLabel)"
                    },
                    accentColor = Color(0xFFBE185D),
                    containerColor = Color(0xFFFDF2F8),
                    borderColor = Color(0xFFF9A8D4),
                    isEn = isEn,
                    selectedMonthOnly = monthOnly,
                    onToggleMonthOnly = { monthOnly = it },
                    pdfButtonTestTag = "btn_parent_haid_pdf",
                    onDownloadPdf = {
                        val file = ParentPortalPdfHelper.generateStudentHaidReportPdf(
                            context = context,
                            student = student,
                            records = filteredRecords,
                            periodLabel = periodLabel,
                            waliName = waliName.ifBlank { "Wali Asuh" },
                            schoolProfile = schoolProfile
                        )
                        if (file != null) {
                            onPdfReady(
                                ParentReadyPdfDialogData(
                                    file = file,
                                    title = if (isEn) "Haid Cycle PDF Ready" else "PDF Rekap Haid Siap",
                                    description = if (isEn) {
                                        "Open or share ${student.name}'s haid cycle monitoring PDF ($periodLabel)."
                                    } else {
                                        "Pilih buka PDF atau bagikan rekap pemantauan siklus haid ${student.name} ($periodLabel)."
                                    },
                                    shareSubject = if (isEn) "Haid Cycle Report: ${student.name}" else "Rekap Siklus Haid: ${student.name}",
                                    shareText = if (isEn) {
                                        "Attached haid cycle monitoring PDF for ${student.name} ($periodLabel)."
                                    } else {
                                        "Terlampir PDF pemantauan siklus haid ${student.name} ($periodLabel)."
                                    }
                                )
                            )
                        } else {
                            Toast.makeText(
                                context,
                                if (isEn) "Unable to create Haid PDF" else "Gagal membuat PDF Rekap Haid",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
        if (filteredRecords.isEmpty()) {
            item {
                ParentEmptyNotice(
                    if (isEn) "No haid cycle records yet."
                    else "Belum ada catatan riwayat haid."
                )
            }
        } else {
            val latest = filteredRecords.first()
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

            items(filteredRecords, key = { "phaid_${it.id}" }) { rec ->
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
    waliBadges: List<ResolvedWaliBadgeInfo> = emptyList(),
    messages: List<ParentMessage>,
    localSavesByMessageId: Map<Long, com.example.data.model.ChatMediaLocalSave> = emptyMap(),
    autoSaveMediaEnabled: Boolean = true,
    isSendingPhoto: Boolean = false,
    isSavingMedia: Boolean = false,
    schoolProfile: SchoolProfile = SchoolProfile(),
    onPdfReady: ((ParentReadyPdfDialogData) -> Unit)? = null,
    onOpenWaliProfile: (() -> Unit)? = null,
    onSend: (String) -> Unit,
    onSendPhoto: ((android.net.Uri, String) -> Unit)? = null,
    onToggleAutoSaveMedia: (Boolean) -> Unit = {},
    onSavePhotoToGallery: ((ParentMessage, Boolean) -> Unit)? = null,
    onPurgeServerMedia: ((ParentMessage) -> Unit)? = null,
    onDeleteMessage: (ParentMessage) -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    var draft by remember { mutableStateOf("") }
    var monthOnly by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val displayedMessages = remember(messages, monthOnly) {
        if (monthOnly) messages.filter { isTimestampInCurrentMonth(it.createdAt) } else messages
    }
    val periodLabel = remember(monthOnly, isEn) {
        if (monthOnly) currentMonthPeriodLabel(isEn) else if (isEn) "All History" else "Semua Riwayat"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_chat_content")
    ) {
        val onDutyBadge = waliBadges.firstOrNull { it.isOnDutyNow }
        val primaryBadge = onDutyBadge ?: waliBadges.firstOrNull()
        val displayAcc = primaryBadge?.account ?: waliAccount
        val waliDisplay = when {
            waliBadges.size > 1 -> waliBadges.joinToString(", ") { it.name }
            else -> primaryBadge?.name?.takeIf { it.isNotBlank() }
                ?: waliAccount?.name?.takeIf { it.isNotBlank() }
                ?: student?.assignedWaliName?.ifBlank { link?.assignedWaliName.orEmpty() }
                ?: link?.assignedWaliName.orEmpty()
        }
        val waliAvatarGender = if (
            (displayAcc?.dormitory ?: student?.dormitory ?: link?.studentDorm.orEmpty()).contains("putri", ignoreCase = true)
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
                    photoUrl = displayAcc?.photoUrl.orEmpty(),
                    photoFallbackUrl = displayAcc?.photoFallbackUrl.orEmpty()
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
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (displayAcc != null) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = if (isEn) "Verified" else "Terverifikasi",
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (onDutyBadge != null) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = if (isEn) "On Duty: ${onDutyBadge.name}" else "Piket: ${onDutyBadge.name}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = buildString {
                            if (waliBadges.size > 1) {
                                append(if (isEn) "Caregiver Team (${waliBadges.size})" else "Tim Wali Asuh (${waliBadges.size})")
                            } else {
                                append(displayAcc?.effectiveTitle(isEn) ?: if (isEn) "Foster Caregiver" else "Wali Asuh Pembina")
                            }
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
                if (student != null) {
                    OutlinedButton(
                        onClick = {
                            val resolvedWali = waliDisplay.ifBlank { "Wali Asuh" }
                            val file = ParentPortalPdfHelper.generateStudentChatTranscriptPdf(
                                context = context,
                                student = student,
                                messages = displayedMessages,
                                localSavesByMessageId = localSavesByMessageId,
                                periodLabel = periodLabel,
                                waliName = resolvedWali,
                                schoolProfile = schoolProfile
                            )
                            if (file != null) {
                                val ready = ParentReadyPdfDialogData(
                                    file = file,
                                    title = if (isEn) "Chat Transcript PDF Ready" else "PDF Transkrip Pesan Siap",
                                    description = if (isEn) {
                                        "Open or share the parent-caregiver chat transcript PDF for ${student.name}."
                                    } else {
                                        "Pilih buka PDF atau bagikan transkrip percakapan orang tua & wali untuk ${student.name}."
                                    },
                                    shareSubject = if (isEn) "Chat Transcript: ${student.name}" else "Transkrip Pesan: ${student.name}",
                                    shareText = if (isEn) {
                                        "Attached parent & caregiver chat transcript PDF for ${student.name} ($periodLabel)."
                                    } else {
                                        "Terlampir PDF transkrip percakapan orang tua & wali ${student.name} ($periodLabel)."
                                    }
                                )
                                if (onPdfReady != null) {
                                    onPdfReady(ready)
                                } else {
                                    PdfReportGenerator.openPdfFile(context, file)
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isEn) "Unable to create Chat Transcript PDF" else "Gagal membuat PDF Transkrip Pesan",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF4338CA)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("btn_parent_chat_pdf")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = if (isEn) "Download chat transcript PDF" else "Unduh PDF transkrip pesan",
                            tint = Color(0xFF4338CA),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PDF",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4338CA)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = monthOnly,
                onClick = { monthOnly = true },
                label = {
                    Text(
                        text = if (isEn) "This Month" else "Bulan Ini",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4338CA).copy(alpha = 0.14f),
                    selectedLabelColor = Color(0xFF4338CA)
                ),
                modifier = Modifier.testTag("btn_parent_chat_pdf_filter_month")
            )
            FilterChip(
                selected = !monthOnly,
                onClick = { monthOnly = false },
                label = {
                    Text(
                        text = if (isEn) "All History" else "Semua Riwayat",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4338CA).copy(alpha = 0.14f),
                    selectedLabelColor = Color(0xFF4338CA)
                ),
                modifier = Modifier.testTag("btn_parent_chat_pdf_filter_all")
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${displayedMessages.size} ${if (isEn) "msg" else "pesan"}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        com.example.ui.components.ChatAutoSaveToggleStrip(
            autoSaveEnabled = autoSaveMediaEnabled,
            isSavingMedia = isSavingMedia,
            isEn = isEn,
            onToggleAutoSave = onToggleAutoSaveMedia
        )

        ParentChatMessagesList(
            messages = displayedMessages,
            myRole = "PARENT",
            isEn = isEn,
            lang = s.language,
            ownBubbleColor = Color(0xFF4338CA),
            listState = listState,
            onDeleteMessage = onDeleteMessage,
            localSavesByMessageId = localSavesByMessageId,
            autoSaveMediaEnabled = autoSaveMediaEnabled,
            isSavingMedia = isSavingMedia,
            onSavePhotoToGallery = onSavePhotoToGallery,
            onPurgeServerMedia = onPurgeServerMedia,
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

        com.example.ui.components.ChatPhotoComposerBar(
            draftText = draft,
            onDraftChange = { draft = it },
            placeholderText = if (isEn) "Write message to caregiver..." else "Tulis pesan ke wali asuh...",
            isEn = isEn,
            isSendingPhoto = isSendingPhoto,
            supportsPhotoUpload = onSendPhoto != null,
            onSendText = onSend,
            onSendPhoto = onSendPhoto,
            inputTestTag = "input_parent_portal_message",
            sendButtonTestTag = "btn_parent_portal_send",
            attachButtonTestTag = "btn_attach_chat_photo_parent"
        )
    }
}

@Composable
private fun ParentTabunganContent(
    student: ChildStudent?,
    transactions: List<TabunganTransaction>,
    waliName: String,
    schoolProfile: SchoolProfile,
    onPdfReady: (ParentReadyPdfDialogData) -> Unit = {}
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    var monthOnly by remember { mutableStateOf(false) }
    val filteredTransactions = remember(transactions, monthOnly) {
        if (monthOnly) transactions.filter { isTimestampInCurrentMonth(it.transactionDate) } else transactions
    }
    val periodLabel = remember(monthOnly, isEn) {
        if (monthOnly) currentMonthPeriodLabel(isEn) else if (isEn) "All History" else "Semua Riwayat"
    }
    val totals = remember(filteredTransactions) { TabunganHelper.computeAggregateTotals(filteredTransactions) }
    val holderBreakdowns = remember(filteredTransactions, waliName) {
        filteredTransactions
            .groupBy { tx ->
                tx.ownerWaliUsername.trim().ifBlank { tx.ownerWaliName.trim() }.ifBlank { "default" }.lowercase()
            }
            .values
            .map { groupTxs ->
                val holderLabel = groupTxs.firstNotNullOfOrNull { it.ownerWaliName.takeIf { n -> n.isNotBlank() } }
                    ?: groupTxs.firstNotNullOfOrNull { it.ownerWaliUsername.takeIf { u -> u.isNotBlank() } }
                    ?: waliName.ifBlank { if (isEn) "Foster Caregiver" else "Wali Asuh" }
                holderLabel to TabunganHelper.computeAggregateTotals(groupTxs)
            }
    }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Allowance Balance ($periodLabel)" else "Saldo Tabungan Titipan ($periodLabel)",
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
                        }
                        if (student != null) {
                            OutlinedButton(
                                onClick = {
                                    val file = PdfReportGenerator.generateTabunganStudentPdf(
                                        context = context,
                                        student = student,
                                        transactions = filteredTransactions,
                                        waliName = waliName.ifBlank { "Wali Asuh" },
                                        schoolProfile = schoolProfile
                                    )
                                    if (file != null) {
                                        onPdfReady(
                                            ParentReadyPdfDialogData(
                                                file = file,
                                                title = if (isEn) "Savings Ledger PDF Ready" else "PDF Mutasi Tabungan Siap",
                                                description = if (isEn) {
                                                    "Open or share ${student.name}'s allowance savings ledger PDF ($periodLabel)."
                                                } else {
                                                    "Pilih buka PDF atau bagikan laporan mutasi tabungan titipan ${student.name} ($periodLabel)."
                                                },
                                                shareSubject = if (isEn) "Savings Ledger: ${student.name}" else "Mutasi Tabungan: ${student.name}",
                                                shareText = if (isEn) {
                                                    "Attached personal allowance savings ledger PDF for ${student.name} ($periodLabel)."
                                                } else {
                                                    "Terlampir PDF laporan tabungan titipan uang ${student.name} ($periodLabel)."
                                                }
                                            )
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            if (isEn) "Unable to create Savings PDF" else "Gagal membuat PDF Tabungan",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFB45309)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_parent_tabungan_pdf")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = if (isEn) "Download Savings PDF" else "Unduh PDF Tabungan",
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isEn) "PDF" else "Unduh PDF",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = monthOnly,
                            onClick = { monthOnly = true },
                            label = {
                                Text(
                                    text = if (isEn) "This Month" else "Bulan Ini",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFB45309).copy(alpha = 0.14f),
                                selectedLabelColor = Color(0xFFB45309)
                            ),
                            modifier = Modifier.testTag("btn_parent_tabungan_pdf_filter_month")
                        )
                        FilterChip(
                            selected = !monthOnly,
                            onClick = { monthOnly = false },
                            label = {
                                Text(
                                    text = if (isEn) "All History" else "Semua Riwayat",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFB45309).copy(alpha = 0.14f),
                                selectedLabelColor = Color(0xFFB45309)
                            ),
                            modifier = Modifier.testTag("btn_parent_tabungan_pdf_filter_all")
                        )
                    }
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
                    if (holderBreakdowns.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isEn) "Balance by Money Holder:" else "Rincian Saldo per Pemegang Titipan:",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                holderBreakdowns.forEach { (holder, holderTotals) ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFFCD34D))
                                    ) {
                                        Text(
                                            text = "$holder: ${TabunganHelper.formatRupiah(holderTotals.totalBalance)}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        text = if (isEn) {
                            "Managed by: ${waliName.ifBlank { "Foster Caregiver" }} • Isolated per caregiver custody"
                        } else {
                            "Tim Pembina: ${waliName.ifBlank { "Wali Asuh Pembina" }} • Saldo dikelola terpisah per wali pemegang"
                        },
                        fontSize = 10.5.sp,
                        color = Color(0xFF78350F)
                    )
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            item {
                ParentEmptyNotice(
                    if (monthOnly && transactions.isNotEmpty()) {
                        if (isEn) "No savings transactions in this month." else "Belum ada transaksi tabungan pada bulan ini."
                    } else if (isEn) {
                        "No savings transactions recorded yet."
                    } else {
                        "Belum ada catatan transaksi tabungan titipan."
                    }
                )
            }
        } else {
            items(filteredTransactions, key = { "ptab_${it.id}" }) { tx ->
                val isIncome = TabunganHelper.isIncome(tx.transactionType)
                val accent = if (isIncome) Color(0xFF15803D) else Color(0xFFDC2626)
                val badgeBg = if (isIncome) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                val holderLabel = tx.ownerWaliName.ifBlank { tx.ownerWaliUsername }.ifBlank { waliName }
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
                            if (holderLabel.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEF3C7),
                                    modifier = Modifier.testTag("label_parent_tabungan_holder_${tx.id}")
                                ) {
                                    Text(
                                        text = if (isEn) "Holder: $holderLabel" else "Pemegang: $holderLabel",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
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
private fun ParentBibleReaderHost(
    viewModel: HealthViewModel,
    isEn: Boolean,
    onDismiss: () -> Unit
) {
    val bibleBookmarks by viewModel.bibleBookmarks.collectAsStateWithLifecycle()
    val bibleNotes by viewModel.bibleNotes.collectAsStateWithLifecycle()
    com.example.ui.dialogs.BibleReaderSheet(
        bookmarks = bibleBookmarks,
        notes = bibleNotes,
        initialEnglish = isEn,
        useDialogWindow = true,
        onSyncRequested = { viewModel.syncBibleOnlineQuietly() },
        onToggleBookmark = { bookId, chapter, verse, textId, textEn, color, onResult ->
            viewModel.toggleBibleBookmark(
                bookId = bookId,
                chapter = chapter,
                verse = verse,
                verseTextId = textId,
                verseTextEn = textEn,
                highlightColor = color,
                onResult = onResult
            )
        },
        onDeleteBookmark = { bm, onResult ->
            viewModel.deleteBibleBookmark(bm, onResult)
        },
        onSaveNote = { existingNote, bookId, chapter, verse, title, content, tag, onResult ->
            viewModel.saveBibleNote(
                existingNote = existingNote,
                bookId = bookId,
                chapter = chapter,
                verse = verse,
                title = title,
                content = content,
                categoryTag = tag,
                onResult = onResult
            )
        },
        onDeleteNote = { note, onResult ->
            viewModel.deleteBibleNote(note, onResult)
        },
        onDismiss = onDismiss
    )
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
