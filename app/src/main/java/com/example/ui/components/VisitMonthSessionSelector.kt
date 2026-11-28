package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VisitHelper
import com.example.data.model.VisitSession
import java.util.Locale

private enum class VisitActiveDropdownMenu {
    NONE,
    MONTH,
    SESSION
}

@Composable
fun VisitSheetGradientHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    gradientColors: List<Color>,
    closeContentDescription: String,
    closeTestTag: String,
    onDismiss: () -> Unit,
    badgeText: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(gradientColors))
            .padding(top = 8.dp, bottom = 14.dp, start = 16.dp, end = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Subtle pill handle inside the gradient header
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.42f))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .align(Alignment.CenterHorizontally),
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
                        color = Color.White.copy(alpha = 0.94f),
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = title,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (!badgeText.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.22f),
                                    border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = badgeText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(closeTestTag)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = closeContentDescription,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VisitMonthSessionFilterRow(
    allSessions: List<VisitSession>,
    selectedMonth: String,
    onSelectMonth: (String) -> Unit,
    sessionsInMonth: List<VisitSession>,
    selectedSessionId: Long?,
    onSelectSession: (VisitSession?) -> Unit,
    locale: Locale,
    isEn: Boolean,
    monthAccentColor: Color,
    monthContainerColor: Color,
    monthBorderColor: Color,
    sessionAccentColor: Color,
    sessionContainerColor: Color,
    sessionBorderColor: Color,
    monthDropdownTag: String,
    monthOptionTagPrefix: String,
    sessionDropdownTag: String,
    sessionOptionTagPrefix: String,
    allowAllSessions: Boolean = false,
    modifier: Modifier = Modifier
) {
    var activeMenu by remember { mutableStateOf(VisitActiveDropdownMenu.NONE) }
    var sessionSearch by remember { mutableStateOf("") }
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    val availableMonths = remember(allSessions) {
        allSessions.map { it.monthYear }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedDescending()
    }

    val monthLabel = remember(selectedMonth, locale, isEn) {
        if (selectedMonth == "ALL" || selectedMonth.isBlank()) {
            if (isEn) "All Months" else "Semua Bulan"
        } else {
            VisitHelper.formatMonthYearLabel(selectedMonth, locale)
        }
    }

    val activeSession = remember(sessionsInMonth, selectedSessionId, allowAllSessions) {
        if (allowAllSessions && selectedSessionId == null) null
        else sessionsInMonth.firstOrNull { it.id == selectedSessionId }
            ?: if (allowAllSessions) null else sessionsInMonth.firstOrNull()
    }

    val sessionLabel = remember(activeSession, sessionsInMonth, allowAllSessions, isEn) {
        when {
            activeSession != null -> activeSession.title
            allowAllSessions -> if (isEn) "All Sessions (${sessionsInMonth.size})" else "Semua Sesi (${sessionsInMonth.size})"
            else -> if (isEn) "Select Session" else "Pilih Sesi"
        }
    }

    val filteredSessions = remember(sessionsInMonth, sessionSearch, locale) {
        val q = sessionSearch.trim().lowercase(locale)
        if (q.isEmpty()) sessionsInMonth
        else sessionsInMonth.filter { sess ->
            val monthText = VisitHelper.formatMonthYearLabel(sess.monthYear, locale).lowercase(locale)
            sess.title.lowercase(locale).contains(q) ||
                monthText.contains(q) ||
                sess.selectedDistrictsCsv.lowercase(locale).contains(q)
        }
    }

    val monthTint = if (isLight) monthAccentColor else Color(0xFF93C5FD)
    val monthBg = if (isLight) monthContainerColor else monthAccentColor.copy(alpha = 0.22f)
    val sessionTint = if (isLight) sessionAccentColor else Color(0xFFCCFBF1)
    val sessionBg = if (isLight) sessionContainerColor else sessionAccentColor.copy(alpha = 0.22f)
    val panelBg = if (isLight) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.surfaceContainerHigh

    val listState = rememberLazyListState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (availableMonths.isNotEmpty()) {
                val monthOpen = activeMenu == VisitActiveDropdownMenu.MONTH
                Surface(
                    onClick = {
                        activeMenu = if (monthOpen) VisitActiveDropdownMenu.NONE else VisitActiveDropdownMenu.MONTH
                        sessionSearch = ""
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = monthBg,
                    border = BorderStroke(if (monthOpen) 1.5.dp else 1.dp, if (monthOpen) monthTint else monthBorderColor),
                    modifier = Modifier
                        .weight(0.95f)
                        .heightIn(min = 48.dp)
                        .testTag(monthDropdownTag)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = monthTint,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = monthLabel,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = monthTint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (monthOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isEn) "Choose month" else "Pilih bulan",
                            tint = monthTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (sessionsInMonth.isNotEmpty()) {
                val sessionOpen = activeMenu == VisitActiveDropdownMenu.SESSION
                Surface(
                    onClick = {
                        activeMenu = if (sessionOpen) VisitActiveDropdownMenu.NONE else VisitActiveDropdownMenu.SESSION
                        if (activeMenu == VisitActiveDropdownMenu.NONE) sessionSearch = ""
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = sessionBg,
                    border = BorderStroke(if (sessionOpen) 1.5.dp else 1.dp, if (sessionOpen) sessionTint else sessionBorderColor),
                    modifier = Modifier
                        .weight(1.25f)
                        .heightIn(min = 48.dp)
                        .testTag(sessionDropdownTag)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = sessionTint,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sessionLabel,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = sessionTint,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val subText = if (activeSession != null) {
                                activeSession.displayTimeRange()
                            } else {
                                if (isEn) "Filter by session" else "Pilih sesi kunjungan"
                            }
                            Text(
                                text = subText,
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (sessionsInMonth.size > 1) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = sessionTint.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "${sessionsInMonth.size}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = sessionTint,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Icon(
                            imageVector = if (sessionOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isEn) "Choose session" else "Pilih sesi",
                            tint = sessionTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Inline downward-expanding scrollable panel (tidak melayang ke atas & hemat ruang)
        AnimatedVisibility(
            visible = activeMenu != VisitActiveDropdownMenu.NONE,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) + fadeOut()
        ) {
            val isMonthMenu = activeMenu == VisitActiveDropdownMenu.MONTH
            val activeAccent = if (isMonthMenu) monthTint else sessionTint
            val activeHeaderBg = if (isMonthMenu) {
                if (isLight) monthContainerColor.copy(alpha = 0.65f) else monthAccentColor.copy(alpha = 0.18f)
            } else {
                if (isLight) sessionContainerColor.copy(alpha = 0.65f) else sessionAccentColor.copy(alpha = 0.18f)
            }
            val activeBorder = if (isMonthMenu) monthBorderColor else sessionBorderColor

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = panelBg,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, activeBorder)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(activeHeaderBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isMonthMenu) {
                                if (isEn) "Choose Visit Month (${availableMonths.size}) • Scroll"
                                else "Pilih Bulan Kunjungan (${availableMonths.size}) • Gulir"
                            } else {
                                if (isEn) "${filteredSessions.size} of ${sessionsInMonth.size} sessions • Scroll"
                                else "${filteredSessions.size} dari ${sessionsInMonth.size} sesi • Gulir ke bawah"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = activeAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (isEn) "Close" else "Tutup",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeAccent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    activeMenu = VisitActiveDropdownMenu.NONE
                                    sessionSearch = ""
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (!isMonthMenu && sessionsInMonth.size > 4) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isLight) Color(0xFFF8FAFC) else MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 40.dp)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (sessionSearch.isEmpty()) {
                                            Text(
                                                text = if (isEn) "Search session or district..." else "Cari sesi atau kecamatan...",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        BasicTextField(
                                            value = sessionSearch,
                                            onValueChange = { sessionSearch = it },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            cursorBrush = SolidColor(activeAccent),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    if (sessionSearch.isNotEmpty()) {
                                        IconButton(
                                            onClick = { sessionSearch = "" },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = if (isEn) "Clear" else "Hapus",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    }

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(vertical = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 196.dp)
                            .drawWithContent {
                                drawContent()
                                val layoutInfo = listState.layoutInfo
                                val totalItems = layoutInfo.totalItemsCount
                                val visibleItems = layoutInfo.visibleItemsInfo
                                if (totalItems > visibleItems.size && visibleItems.isNotEmpty()) {
                                    val firstVisible = visibleItems.first()
                                    val avgItemHeight = visibleItems.sumOf { it.size }.toFloat() / visibleItems.size.coerceAtLeast(1)
                                    val estimatedTotalHeight = (avgItemHeight * totalItems).coerceAtLeast(size.height)
                                    val thumbHeight = ((size.height / estimatedTotalHeight) * size.height)
                                        .coerceIn(22.dp.toPx(), size.height * 0.7f)
                                    val scrollOffset = (firstVisible.index * avgItemHeight) - firstVisible.offset
                                    val maxScroll = (estimatedTotalHeight - size.height).coerceAtLeast(1f)
                                    val fraction = (scrollOffset / maxScroll).coerceIn(0f, 1f)
                                    val thumbY = fraction * (size.height - thumbHeight)
                                    drawRoundRect(
                                        color = activeAccent.copy(alpha = 0.45f),
                                        topLeft = Offset(size.width - 5.dp.toPx(), thumbY + 3.dp.toPx()),
                                        size = Size(3.5.dp.toPx(), (thumbHeight - 6.dp.toPx()).coerceAtLeast(14.dp.toPx())),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }
                            }
                    ) {
                        if (isMonthMenu) {
                            val monthOptions = availableMonths.map { ym ->
                                val count = allSessions.count { it.monthYear.equals(ym, ignoreCase = true) }
                                ym to VisitHelper.formatMonthYearLabel(ym, locale) to count
                            } + listOf(("ALL" to (if (isEn) "All Months" else "Semua Bulan")) to allSessions.size)

                            itemsIndexed(monthOptions, key = { _, item -> "month_${item.first.first}" }) { idx, (pair, count) ->
                                val (key, title) = pair
                                val isSel = key == selectedMonth
                                val rowBg = if (isSel) {
                                    if (isLight) monthContainerColor.copy(alpha = 0.55f) else monthAccentColor.copy(alpha = 0.24f)
                                } else Color.Transparent

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .background(rowBg)
                                            .clickable {
                                                activeMenu = VisitActiveDropdownMenu.NONE
                                                sessionSearch = ""
                                                onSelectMonth(key)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("${monthOptionTagPrefix}_$key"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = title,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (isSel) monthTint else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = monthTint.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = if (isEn) "$count sess" else "$count sesi",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = monthTint,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (isSel) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = monthTint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    if (idx < monthOptions.lastIndex) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                                    }
                                }
                            }
                        } else {
                            if (allowAllSessions) {
                                item(key = "session_ALL") {
                                    val isSel = selectedSessionId == null
                                    val rowBg = if (isSel) {
                                        if (isLight) sessionContainerColor.copy(alpha = 0.55f) else sessionAccentColor.copy(alpha = 0.24f)
                                    } else Color.Transparent

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                                .background(rowBg)
                                                .clickable {
                                                    activeMenu = VisitActiveDropdownMenu.NONE
                                                    sessionSearch = ""
                                                    onSelectSession(null)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .testTag("${sessionOptionTagPrefix}_ALL"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = if (isEn) "All Sessions (${sessionsInMonth.size})" else "Semua Sesi (${sessionsInMonth.size})",
                                                fontSize = 12.5.sp,
                                                fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                color = if (isSel) sessionTint else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isSel) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = sessionTint,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                                    }
                                }
                            }

                            if (filteredSessions.isEmpty()) {
                                item(key = "session_empty") {
                                    Text(
                                        text = if (isEn) "No matching session found." else "Sesi tidak ditemukan.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                                    )
                                }
                            } else {
                                itemsIndexed(filteredSessions, key = { _, s -> "sess_${s.id}" }) { idx, sess ->
                                    val isSel = activeSession?.id == sess.id
                                    val sessMonth = VisitHelper.formatMonthYearLabel(sess.monthYear, locale)
                                    val rowBg = if (isSel) {
                                        if (isLight) sessionContainerColor.copy(alpha = 0.55f) else sessionAccentColor.copy(alpha = 0.24f)
                                    } else Color.Transparent

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                                .background(rowBg)
                                                .clickable {
                                                    activeMenu = VisitActiveDropdownMenu.NONE
                                                    sessionSearch = ""
                                                    onSelectSession(sess)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .testTag("${sessionOptionTagPrefix}_${sess.id}"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = sess.title,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                    color = if (isSel) sessionTint else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "$sessMonth • ${sess.displayTimeRange()}",
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            if (isSel) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = sessionTint,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        if (idx < filteredSessions.lastIndex) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
