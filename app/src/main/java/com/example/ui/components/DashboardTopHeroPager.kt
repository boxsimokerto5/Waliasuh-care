package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicalRecord
import com.example.data.model.WaliAccount
import com.example.util.CityWeatherForecast
import com.example.util.IndonesiaCitySpec
import com.example.util.PrayerTimeHelper
import com.example.util.WeatherConditionCategory
import com.example.util.WeatherForecastHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun DashboardTopHeroPager(
    todayShortFormatted: String,
    waliName: String,
    currentWaliAccount: WaliAccount?,
    schoolCityName: String,
    sortedFaskesEntries: List<Triple<String, Int, List<MedicalRecord>>>,
    customFaskesInpatients: Map<String, List<MedicalRecord>>,
    totalToday: Int,
    totalSchoolStudents: Int,
    upcomingCount: Int,
    progressRatio: Float,
    isEn: Boolean,
    tourRegistry: TourCoordinateRegistry,
    onNavigateToProfile: () -> Unit,
    onAddPlan: () -> Unit,
    onSelectInpatientRecord: (MedicalRecord) -> Unit,
    onOpenInpatientPicker: (String, List<MedicalRecord>) -> Unit,
    onOpenAbsenSholat: () -> Unit,
    modifier: Modifier = Modifier,
    weatherFetcher: (suspend (IndonesiaCitySpec) -> String?)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    var savedCityId by remember { mutableStateOf(PrayerTimeHelper.getSavedCityId(context)) }
    var showCityPicker by remember { mutableStateOf(false) }
    var clockTickMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            clockTickMillis = System.currentTimeMillis()
        }
    }

    val activeCity = remember(schoolCityName, savedCityId) {
        PrayerTimeHelper.resolveCity(schoolCityName, savedCityId)
    }
    val prayerSchedule = remember(activeCity, clockTickMillis, isEn) {
        val cal = Calendar.getInstance().apply { timeInMillis = clockTickMillis }
        PrayerTimeHelper.calculateDailyPrayerSchedule(
            city = activeCity,
            calendar = cal,
            isEn = isEn
        )
    }

    var weatherForecast by remember(activeCity.id) {
        mutableStateOf(
            WeatherForecastHelper.getCachedWeather(
                context = context,
                city = activeCity,
                allowExpired = true
            )
        )
    }
    var isWeatherLoading by remember(activeCity.id) { mutableStateOf(false) }
    var isWeatherError by remember(activeCity.id) { mutableStateOf(false) }
    var weatherRefreshTrigger by remember { mutableIntStateOf(0) }
    var lastHandledWeatherTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(activeCity.id, weatherRefreshTrigger) {
        val force = weatherRefreshTrigger != lastHandledWeatherTrigger
        lastHandledWeatherTrigger = weatherRefreshTrigger
        val freshCached = if (!force) {
            WeatherForecastHelper.getCachedWeather(
                context = context,
                city = activeCity,
                allowExpired = false
            )
        } else null

        if (freshCached != null) {
            weatherForecast = freshCached
            isWeatherLoading = false
            isWeatherError = false
        } else {
            isWeatherLoading = true
            isWeatherError = false
            val result = WeatherForecastHelper.fetchWeatherForCity(
                context = context,
                city = activeCity,
                forceRefresh = force,
                customFetcher = weatherFetcher
            )
            result.onSuccess { fetched ->
                weatherForecast = fetched
                isWeatherError = false
            }.onFailure {
                isWeatherError = weatherForecast == null
            }
            isWeatherLoading = false
        }
    }

    val isDarkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 10.dp,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_hero_pager")
        ) { page ->
            when (page) {
                0 -> TodayCareHeroCard(
                    todayShortFormatted = todayShortFormatted,
                    waliName = waliName,
                    currentWaliAccount = currentWaliAccount,
                    sortedFaskesEntries = sortedFaskesEntries,
                    customFaskesInpatients = customFaskesInpatients,
                    totalToday = totalToday,
                    totalSchoolStudents = totalSchoolStudents,
                    upcomingCount = upcomingCount,
                    progressRatio = progressRatio,
                    isEn = isEn,
                    isDarkSurface = isDarkSurface,
                    tourRegistry = tourRegistry,
                    onNavigateToProfile = onNavigateToProfile,
                    onAddPlan = onAddPlan,
                    onSelectInpatientRecord = onSelectInpatientRecord,
                    onOpenInpatientPicker = onOpenInpatientPicker
                )

                1 -> TodayPrayerTimesHeroCard(
                    schedule = prayerSchedule,
                    todayShortFormatted = todayShortFormatted,
                    isManualCity = savedCityId != null,
                    isEn = isEn,
                    isDarkSurface = isDarkSurface,
                    onOpenCityPicker = { showCityPicker = true },
                    onOpenAbsenSholat = onOpenAbsenSholat
                )

                else -> TodayWeatherHeroCard(
                    city = activeCity,
                    forecast = weatherForecast,
                    isLoading = isWeatherLoading,
                    isError = isWeatherError,
                    todayShortFormatted = todayShortFormatted,
                    isEn = isEn,
                    isDarkSurface = isDarkSurface,
                    onOpenCityPicker = { showCityPicker = true },
                    onRefreshWeather = { weatherRefreshTrigger++ }
                )
            }
        }

        // Compact 3-tab quick selector + animated swipe dots
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroPagerIndicatorChip(
                    label = if (isEn) "Today's Care" else "Berobat",
                    selected = pagerState.currentPage == 0,
                    activeColor = if (isDarkSurface) Color(0xFF10B981) else Color(0xFF047857),
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    modifier = Modifier.testTag("hero_pager_tab_care")
                )

                HeroPagerIndicatorChip(
                    label = if (isEn) "Prayer" else "Jadwal Sholat",
                    selected = pagerState.currentPage == 1,
                    activeColor = if (isDarkSurface) Color(0xFFF59E0B) else Color(0xFFB45309),
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    modifier = Modifier.testTag("hero_pager_tab_prayer")
                )

                HeroPagerIndicatorChip(
                    label = if (isEn) "Weather" else "Cuaca",
                    selected = pagerState.currentPage == 2,
                    activeColor = if (isDarkSurface) Color(0xFF38BDF8) else Color(0xFF0284C7),
                    onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                    modifier = Modifier.testTag("hero_pager_tab_weather")
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { idx ->
                    val selected = pagerState.currentPage == idx
                    val dotColor by animateColorAsState(
                        targetValue = if (selected) {
                            when (idx) {
                                0 -> if (isDarkSurface) Color(0xFF10B981) else Color(0xFF047857)
                                1 -> if (isDarkSurface) Color(0xFFF59E0B) else Color(0xFFB45309)
                                else -> if (isDarkSurface) Color(0xFF38BDF8) else Color(0xFF0284C7)
                            }
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        label = "hero_dot_color_$idx"
                    )
                    Box(
                        modifier = Modifier
                            .height(4.5.dp)
                            .width(if (selected) 16.dp else 5.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }

    if (showCityPicker) {
        PrayerCityPickerSheet(
            cities = PrayerTimeHelper.INDONESIA_CITIES,
            activeCity = activeCity,
            schoolDefaultCity = PrayerTimeHelper.findBestCityMatch(schoolCityName),
            isUsingSchoolDefault = savedCityId == null,
            isEn = isEn,
            onSelectCity = { city ->
                PrayerTimeHelper.saveSelectedCityId(context, city.id)
                savedCityId = city.id
                showCityPicker = false
            },
            onResetToSchoolCity = {
                PrayerTimeHelper.saveSelectedCityId(context, null)
                savedCityId = null
                showCityPicker = false
            },
            onDismiss = { showCityPicker = false }
        )
    }
}

@Composable
private fun HeroPagerIndicatorChip(
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) activeColor.copy(alpha = 0.14f) else Color.Transparent,
        border = if (selected) BorderStroke(0.8.dp, activeColor.copy(alpha = 0.45f)) else null,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
        )
    }
}

@Composable
private fun TodayCareHeroCard(
    todayShortFormatted: String,
    waliName: String,
    currentWaliAccount: WaliAccount?,
    sortedFaskesEntries: List<Triple<String, Int, List<MedicalRecord>>>,
    customFaskesInpatients: Map<String, List<MedicalRecord>>,
    totalToday: Int,
    totalSchoolStudents: Int,
    upcomingCount: Int,
    progressRatio: Float,
    isEn: Boolean,
    isDarkSurface: Boolean,
    tourRegistry: TourCoordinateRegistry,
    onNavigateToProfile: () -> Unit,
    onAddPlan: () -> Unit,
    onSelectInpatientRecord: (MedicalRecord) -> Unit,
    onOpenInpatientPicker: (String, List<MedicalRecord>) -> Unit
) {
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
            .tourTarget(tourRegistry, TourTargetId.HERO_CARE)
            .testTag("card_summary_today"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 124.dp)
                .background(Brush.linearGradient(heroGradient))
                .border(1.dp, heroBorderColor, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
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
                            if (currentWaliAccount?.hasProfilePhoto() == true) {
                                StudentAvatar(
                                    name = currentWaliAccount.name.ifBlank { waliName },
                                    gender = if (currentWaliAccount.dormitory.contains("putri", ignoreCase = true)) "P" else "L",
                                    size = 18.dp,
                                    photoUrl = currentWaliAccount.photoUrl,
                                    photoFallbackUrl = currentWaliAccount.photoFallbackUrl
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = heroSubtitleColor,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
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
                            .clickable(onClick = onAddPlan)
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
                                .border(1.dp, pillBorder, RoundedCornerShape(8.dp))
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
                                        if (faskesInpatients.size == 1) {
                                            onSelectInpatientRecord(faskesInpatients.first())
                                        } else {
                                            onOpenInpatientPicker(faskesName, faskesInpatients)
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
                                .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
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
                                    if (list.size == 1) {
                                        onSelectInpatientRecord(list.first())
                                    } else {
                                        onOpenInpatientPicker(faskesName, list)
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
                        if (upcomingCount > 0) {
                            Text(
                                text = if (isEn) "+$upcomingCount upcoming" else "+$upcomingCount mendatang",
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

@Composable
private fun TodayPrayerTimesHeroCard(
    schedule: com.example.util.DailyPrayerSchedule,
    todayShortFormatted: String,
    isManualCity: Boolean,
    isEn: Boolean,
    isDarkSurface: Boolean,
    onOpenCityPicker: () -> Unit,
    onOpenAbsenSholat: () -> Unit
) {
    val bgGradient = if (isDarkSurface) {
        listOf(Color(0xFF0F3D3E), Color(0xFF064E3B), Color(0xFF134E4A))
    } else {
        listOf(Color(0xFFF0FDFA), Color(0xFFECFDF5), Color(0xFFFEFCE8))
    }
    val borderColor = if (isDarkSurface) {
        Color(0xFF2DD4BF).copy(alpha = 0.45f)
    } else {
        Color(0xFF5EEAD4)
    }
    val titleColor = if (isDarkSurface) Color(0xFFF0FDFA) else Color(0xFF0F766E)
    val subColor = if (isDarkSurface) Color(0xFF99F6E4) else Color(0xFF115E59)
    val goldAccent = if (isDarkSurface) Color(0xFFFBBF24) else Color(0xFFB45309)
    val goldSoftBg = if (isDarkSurface) Color(0xFF78350F).copy(alpha = 0.55f) else Color(0xFFFEF3C7)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_prayer_times_today"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 124.dp)
                .background(Brush.linearGradient(bgGradient))
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header: Title + Next prayer countdown + City Dropdown selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = goldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isEn) "Prayer Times" else "Jadwal Sholat",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                                maxLines = 1
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDarkSurface) Color(0xFF042F2E) else Color.White.copy(alpha = 0.88f),
                                border = BorderStroke(0.8.dp, borderColor.copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = todayShortFormatted,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = subColor,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = goldSoftBg,
                            modifier = Modifier.testTag("badge_next_prayer")
                        ) {
                            Text(
                                text = if (isEn) {
                                    "Next ${schedule.nextPrayerNameEn} ${schedule.nextPrayerTime} (${schedule.countdownEn})"
                                } else {
                                    "Menuju ${schedule.nextPrayerNameId} ${schedule.nextPrayerTime} (${schedule.countdownId})"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Dropdown City Pill
                    Surface(
                        onClick = onOpenCityPicker,
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDarkSurface) Color(0xFF042F2E).copy(alpha = 0.85f) else Color.White,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.testTag("btn_prayer_city_dropdown")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = if (isEn) "Select city" else "Pilih kota",
                                tint = goldAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = schedule.city.name
                                    .replace("Kabupaten ", "Kab. ")
                                    .replace("Kota ", ""),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isEn) "Change city" else "Ganti kota",
                                tint = subColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // 5-Waktu Compact Grid Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("row_prayer_times_strip"),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    schedule.slots.forEachIndexed { index, slot ->
                        val isNext = index == schedule.nextSlotIndex
                        val slotBg = when {
                            isNext && isDarkSurface -> Color(0xFF78350F).copy(alpha = 0.55f)
                            isNext -> Color(0xFFFFFBEB)
                            isDarkSurface -> Color(0xFF042F2E).copy(alpha = 0.65f)
                            else -> Color.White.copy(alpha = 0.88f)
                        }
                        val slotBorder = when {
                            isNext -> goldAccent
                            isDarkSurface -> Color(0xFF2DD4BF).copy(alpha = 0.25f)
                            else -> Color(0xFF99F6E4)
                        }
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = slotBg,
                            border = BorderStroke(if (isNext) 1.2.dp else 0.8.dp, slotBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("slot_prayer_${slot.key.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    text = if (isEn) slot.labelEn else slot.labelId,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isNext) goldAccent else subColor,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = slot.timeText,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isNext) goldAccent else titleColor,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Footer: Imsak + Zone + Quick Action to Absen Sholat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) {
                            "Imsak ${schedule.imsak} • ${schedule.city.zoneLabel}${if (isManualCity) "" else " (School)"}"
                        } else {
                            "Imsak ${schedule.imsak} • ${schedule.city.zoneLabel} • Kemenag"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = subColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onOpenAbsenSholat)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                            .testTag("btn_prayer_card_open_absen"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Prayer Attendance" else "Absen Sholat",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldAccent
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = goldAccent,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayWeatherHeroCard(
    city: IndonesiaCitySpec,
    forecast: CityWeatherForecast?,
    isLoading: Boolean,
    isError: Boolean,
    todayShortFormatted: String,
    isEn: Boolean,
    isDarkSurface: Boolean,
    onOpenCityPicker: () -> Unit,
    onRefreshWeather: () -> Unit
) {
    val bgGradient = if (isDarkSurface) {
        listOf(Color(0xFF0C2D48), Color(0xFF0F3D5E), Color(0xFF164E63))
    } else {
        listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE), Color(0xFFF0FDFA))
    }
    val borderColor = if (isDarkSurface) {
        Color(0xFF38BDF8).copy(alpha = 0.45f)
    } else {
        Color(0xFF7DD3FC)
    }
    val titleColor = if (isDarkSurface) Color(0xFFF0F9FF) else Color(0xFF0369A1)
    val subColor = if (isDarkSurface) Color(0xFFBAE6FD) else Color(0xFF0C4A6E)
    val skyAccent = if (isDarkSurface) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val rainAlertAccent = if (isDarkSurface) Color(0xFFFBBF24) else Color(0xFFB45309)
    val isRainHighlight = forecast != null && (forecast.currentDescriptor.isRainRisk || forecast.currentRainChancePercent >= 50)
    val badgeBg = when {
        isRainHighlight && isDarkSurface -> Color(0xFF78350F).copy(alpha = 0.55f)
        isRainHighlight -> Color(0xFFFEF3C7)
        isDarkSurface -> Color(0xFF082F49).copy(alpha = 0.72f)
        else -> Color(0xFFE0F2FE)
    }
    val badgeTextColor = if (isRainHighlight) rainAlertAccent else skyAccent

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_weather_today"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 124.dp)
                .background(Brush.linearGradient(bgGradient))
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header: Title + Current summary badge + Shared City Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = forecast?.let {
                                    weatherCategoryIcon(it.currentDescriptor.category, it.isDay)
                                } ?: Icons.Default.Cloud,
                                contentDescription = null,
                                tint = if (forecast?.currentDescriptor?.category == WeatherConditionCategory.SUNNY) {
                                    Color(0xFFF59E0B)
                                } else {
                                    skyAccent
                                },
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isEn) "Weather" else "Prakiraan Cuaca",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDarkSurface) Color(0xFF082F49) else Color.White.copy(alpha = 0.88f),
                                border = BorderStroke(0.8.dp, borderColor.copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = todayShortFormatted,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = subColor,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeBg,
                            modifier = Modifier.testTag("badge_weather_summary")
                        ) {
                            val summaryText = when {
                                forecast != null -> {
                                    val desc = if (isEn) forecast.currentDescriptor.labelEn else forecast.currentDescriptor.labelId
                                    if (isRainHighlight) {
                                        if (isEn) {
                                            "${forecast.currentTempC}°C • $desc • Rain ${forecast.currentRainChancePercent}%"
                                        } else {
                                            "${forecast.currentTempC}°C • $desc • Siaga Hujan ${forecast.currentRainChancePercent}%"
                                        }
                                    } else {
                                        if (isEn) {
                                            "${forecast.currentTempC}°C • $desc • Rain ${forecast.currentRainChancePercent}%"
                                        } else {
                                            "${forecast.currentTempC}°C • $desc • Hujan ${forecast.currentRainChancePercent}%"
                                        }
                                    }
                                }
                                isLoading -> if (isEn) "Loading weather forecast..." else "Memuat prakiraan cuaca..."
                                else -> if (isEn) "Weather unavailable" else "Prakiraan cuaca belum termuat"
                            }
                            Text(
                                text = summaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Shared City Dropdown Pill (synced with Prayer Times via activeCity ID)
                    Surface(
                        onClick = onOpenCityPicker,
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDarkSurface) Color(0xFF082F49).copy(alpha = 0.85f) else Color.White,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.testTag("btn_weather_city_dropdown")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = if (isEn) "Select city" else "Pilih kota",
                                tint = skyAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = city.name
                                    .replace("Kabupaten ", "Kab. ")
                                    .replace("Kota ", ""),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isEn) "Change city" else "Ganti kota",
                                tint = subColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Middle Strip: Pagi / Siang / Sore / Malam forecast slots
                if (forecast != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("row_weather_periods_strip"),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        forecast.periods.forEach { slot ->
                            val isActive = slot.key == forecast.activePeriodKey
                            val slotBg = when {
                                isActive && isDarkSurface -> Color(0xFF075985).copy(alpha = 0.72f)
                                isActive -> Color(0xFFF0F9FF)
                                isDarkSurface -> Color(0xFF082F49).copy(alpha = 0.65f)
                                else -> Color.White.copy(alpha = 0.88f)
                            }
                            val slotBorder = when {
                                isActive -> skyAccent
                                isDarkSurface -> Color(0xFF38BDF8).copy(alpha = 0.25f)
                                else -> Color(0xFFBAE6FD)
                            }
                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = slotBg,
                                border = BorderStroke(if (isActive) 1.2.dp else 0.8.dp, slotBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("slot_weather_${slot.key.lowercase()}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 3.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Text(
                                        text = if (isEn) slot.labelEn else slot.labelId,
                                        fontSize = 9.sp,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isActive) skyAccent else subColor,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = weatherCategoryIcon(
                                                slot.descriptor.category,
                                                slot.targetHour in 6..17
                                            ),
                                            contentDescription = if (isEn) slot.descriptor.labelEn else slot.descriptor.labelId,
                                            tint = if (slot.descriptor.category == WeatherConditionCategory.SUNNY) {
                                                Color(0xFFF59E0B)
                                            } else if (slot.descriptor.isRainRisk) {
                                                skyAccent
                                            } else {
                                                subColor
                                            },
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "${slot.tempC}°",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isActive) skyAccent else titleColor,
                                            maxLines = 1
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (slot.rainChancePercent >= 45) {
                                                Icons.Default.Umbrella
                                            } else {
                                                Icons.Default.WaterDrop
                                            },
                                            contentDescription = null,
                                            tint = if (slot.rainChancePercent >= 45) rainAlertAccent else subColor.copy(alpha = 0.72f),
                                            modifier = Modifier.size(9.dp)
                                        )
                                        Text(
                                            text = "${slot.rainChancePercent}%",
                                            fontSize = 8.5.sp,
                                            fontWeight = if (slot.rainChancePercent >= 45) FontWeight.Bold else FontWeight.Medium,
                                            color = if (slot.rainChancePercent >= 45) rainAlertAccent else subColor.copy(alpha = 0.85f),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (isError) {
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = if (isDarkSurface) Color(0xFF082F49).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.9f),
                        border = BorderStroke(0.8.dp, borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) {
                                    "Unable to reach weather service"
                                } else {
                                    "Gagal memuat data cuaca online"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = subColor,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                onClick = onRefreshWeather,
                                shape = RoundedCornerShape(8.dp),
                                color = skyAccent,
                                modifier = Modifier.testTag("btn_weather_retry")
                            ) {
                                Text(
                                    text = if (isEn) "Retry" else "Coba Lagi",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkSurface) Color(0xFF082F49) else Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("row_weather_periods_loading"),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf("Pagi", "Siang", "Sore", "Malam").forEach { label ->
                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = if (isDarkSurface) Color(0xFF082F49).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.75f),
                                border = BorderStroke(0.8.dp, borderColor.copy(alpha = 0.6f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        color = subColor
                                    )
                                    Text(
                                        text = "--°",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = titleColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer: Feels-like, humidity, wind + Manual refresh action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val footerText = if (forecast != null) {
                        val updatedTime = WeatherForecastHelper.formatFetchedTime(
                            forecast.fetchedAtMillis,
                            city.utcOffsetHours
                        )
                        if (isEn) {
                            "Feels ${forecast.apparentTempC}° • Hum ${forecast.humidityPercent}% • Wind ${forecast.windKmh} km/h • $updatedTime"
                        } else {
                            "Terasa ${forecast.apparentTempC}° • Lembap ${forecast.humidityPercent}% • Angin ${forecast.windKmh} km/j"
                        }
                    } else {
                        if (isEn) {
                            "${city.zoneLabel} • Synced with Prayer City ID"
                        } else {
                            "${city.zoneLabel} • Sinkron ID Kota Sholat"
                        }
                    }
                    Text(
                        text = footerText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = subColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = !isLoading, onClick = onRefreshWeather)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                            .testTag("btn_weather_refresh"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 1.5.dp,
                                color = skyAccent
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = if (isEn) "Refresh weather" else "Perbarui cuaca",
                                tint = skyAccent,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = when {
                                isLoading -> if (isEn) "Updating" else "Memuat"
                                forecast?.isFromCache == true -> if (isEn) "Cache" else "Perbarui"
                                else -> if (isEn) "Refresh" else "Perbarui"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = skyAccent
                        )
                    }
                }
            }
        }
    }
}

private fun weatherCategoryIcon(
    category: WeatherConditionCategory,
    isDay: Boolean
): ImageVector {
    return when (category) {
        WeatherConditionCategory.SUNNY -> if (isDay) Icons.Default.WbSunny else Icons.Default.NightsStay
        WeatherConditionCategory.PARTLY_CLOUDY -> Icons.Default.Cloud
        WeatherConditionCategory.CLOUDY -> Icons.Default.Cloud
        WeatherConditionCategory.FOG -> Icons.Default.Cloud
        WeatherConditionCategory.DRIZZLE -> Icons.Default.Grain
        WeatherConditionCategory.RAIN -> Icons.Default.WaterDrop
        WeatherConditionCategory.THUNDERSTORM -> Icons.Default.Thunderstorm
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerCityPickerSheet(
    cities: List<IndonesiaCitySpec>,
    activeCity: IndonesiaCitySpec,
    schoolDefaultCity: IndonesiaCitySpec,
    isUsingSchoolDefault: Boolean,
    isEn: Boolean,
    onSelectCity: (IndonesiaCitySpec) -> Unit,
    onResetToSchoolCity: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    val filteredCities = remember(cities, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) cities
        else cities.filter {
            it.name.lowercase().contains(q) ||
                it.province.lowercase().contains(q) ||
                it.zoneLabel.lowercase().contains(q)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .testTag("prayer_city_picker_sheet"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEn) "Select City (Prayer & Weather)" else "Pilih Wilayah (Sholat & Cuaca)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEn) {
                            "Synced for Prayer Times & Weather • Active: ${activeCity.name}"
                        } else {
                            "Sinkron Jadwal Sholat & Prakiraan Cuaca • Aktif: ${activeCity.name}"
                        },
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isEn) "Close" else "Tutup"
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                placeholder = {
                    Text(if (isEn) "Search city or province..." else "Cari nama kota/kabupaten atau provinsi...")
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_prayer_city")
            )

            Surface(
                onClick = onResetToSchoolCity,
                shape = RoundedCornerShape(12.dp),
                color = if (isUsingSchoolDefault) Color(0xFFECFDF5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(
                    1.dp,
                    if (isUsingSchoolDefault) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chip_prayer_city_school_default")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = if (isEn) "Follow School City (Default)" else "Sesuai Kota Sekolah (Bawaan)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${schoolDefaultCity.name} • ${schoolDefaultCity.province} (${schoolDefaultCity.zoneLabel})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (isUsingSchoolDefault) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = if (isEn) "Selected" else "Terpilih",
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredCities, key = { it.id }) { city ->
                    val isSelected = city.id == activeCity.id
                    Surface(
                        onClick = { onSelectCity(city) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_prayer_city_${city.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = city.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = city.province,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = city.zoneLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = if (isEn) "Selected" else "Terpilih",
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(18.dp)
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
