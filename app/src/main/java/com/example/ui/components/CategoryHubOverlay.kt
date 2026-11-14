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
import com.example.data.model.FastingRecord
import com.example.data.model.FastingSession
import com.example.data.model.HaidRecord
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolProfile
import com.example.data.model.ShiftMessage
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
    onOpenInputHaid: () -> Unit,
    onOpenDaftarHaid: () -> Unit,
    onOpenDaftarSuci: () -> Unit,
    onOpenFiqihHaid: () -> Unit,
    onOpenShiftMessages: () -> Unit,
    onOpenCreateShiftMessage: () -> Unit,
    onOpenShiftHistory: () -> Unit
) {
    var lastCategory by remember { mutableStateOf(category) }
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
            DashboardCategoryHub.PUASA -> listOf(Color(0xFF065F46), Color(0xFF0F766E))
            DashboardCategoryHub.HAID -> listOf(Color(0xFF9D174D), Color(0xFFDB2777))
            DashboardCategoryHub.PESAN -> listOf(Color(0xFFB45309), Color(0xFFD97706))
        }

        val heroTitle = when (current) {
            DashboardCategoryHub.BEROBAT -> if (isEn) "Health & Treatment" else "Kesehatan & Berobat"
            DashboardCategoryHub.PUASA -> if (isEn) "Student Puasa Center" else "Pusat Pendataan Puasa"
            DashboardCategoryHub.HAID -> if (isEn) "Haid & Fiqih Care" else "Pendataan & Fiqih Haid"
            DashboardCategoryHub.PESAN -> if (isEn) "Shift Handover Notes" else "Pesan & Operan Shift"
        }

        val heroSubtitle = when (current) {
            DashboardCategoryHub.BEROBAT -> if (isEn) {
                "${todayPlans.size} plans today • ${upcomingPlans.size} upcoming"
            } else {
                "${todayPlans.size} rencana hari ini • ${upcomingPlans.size} mendatang"
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
        }

        val heroImage = when (current) {
            DashboardCategoryHub.BEROBAT -> R.drawable.img_menu_rencana
            DashboardCategoryHub.PUASA -> R.drawable.img_menu_input_puasa
            DashboardCategoryHub.HAID -> R.drawable.img_menu_daftar_haid
            DashboardCategoryHub.PESAN -> R.drawable.img_menu_pesan_shift
        }

        val contextPillText = schoolProfile.shortName
            .ifBlank { schoolProfile.schoolName }
            .ifBlank { "Waliasuh Care" }

        val shieldInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradientColors))
                .clickable(interactionSource = shieldInteraction, indication = null, onClick = {})
                .testTag("category_hub_overlay_${current.name.lowercase()}")
        ) {
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
                                DashboardCategoryHub.PUASA, DashboardCategoryHub.HAID -> Icons.Default.QrCodeScanner
                                else -> Icons.Default.Add
                            }
                            val quickActionDesc = when (current) {
                                DashboardCategoryHub.BEROBAT -> if (isEn) "Create Plan" else "Buat Rencana"
                                DashboardCategoryHub.PUASA -> if (isEn) "Scan Input Puasa" else "Scan Input Puasa"
                                DashboardCategoryHub.HAID -> if (isEn) "Scan Input Haid" else "Scan Input Haid"
                                DashboardCategoryHub.PESAN -> if (isEn) "Write Note" else "Tulis Pesan"
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
                                            DashboardCategoryHub.PUASA -> onOpenInputPuasa()
                                            DashboardCategoryHub.HAID -> onOpenInputHaid()
                                            DashboardCategoryHub.PESAN -> onOpenCreateShiftMessage()
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
                        .fillMaxSize()
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
                                .navigationBarsPadding(),
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
                                    onOpenRankingPuasa = onOpenRankingPuasa
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
                            }
                        }
                    }
                }
            }
        }
    }
}
