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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FastingSession
import com.example.util.DateUtils
import com.example.util.i18n.AppLanguage

fun List<FastingSession>.sortedNewestFirst(): List<FastingSession> =
    sortedWith(
        compareByDescending<FastingSession> { it.sessionDate }
            .thenByDescending { it.createdAt }
            .thenByDescending { it.id }
    )

@Composable
fun FastingSessionDropdown(
    sessions: List<FastingSession>,
    selectedSession: FastingSession?,
    accentColor: Color,
    accentLightColor: Color,
    isEn: Boolean,
    lang: AppLanguage,
    onSelectSession: (FastingSession) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "dropdown_fasting_session",
    trailingAction: (@Composable () -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    val selectedLabel = remember(selectedSession, lang, isEn) {
        if (selectedSession == null) {
            if (isEn) "Select Puasa Session" else "Pilih Sesi Puasa"
        } else {
            val dateStr = DateUtils.formatShortDate(selectedSession.sessionDate, lang)
            val doneSuffix = if (!selectedSession.isActive) {
                if (isEn) " • Done" else " • Selesai"
            } else ""
            "${selectedSession.title} ($dateStr)$doneSuffix"
        }
    }

    val newestSessionId = sessions.firstOrNull()?.id
    val filteredSessions = remember(sessions, searchQuery, lang) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            sessions
        } else {
            sessions.filter { session ->
                val dateStr = DateUtils.formatShortDate(session.sessionDate, lang).lowercase()
                session.title.lowercase().contains(q) || dateStr.contains(q)
            }
        }
    }

    val listState = rememberLazyListState()
    val triggerBg = if (isLight) {
        accentLightColor.copy(alpha = if (expanded) 0.85f else 0.65f)
    } else {
        accentColor.copy(alpha = if (expanded) 0.26f else 0.18f)
    }
    val triggerTint = if (isLight) accentColor else accentLightColor
    val panelBg = if (isLight) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.surfaceContainerHigh
    val selectedRowBg = if (isLight) {
        accentLightColor.copy(alpha = 0.55f)
    } else {
        accentColor.copy(alpha = 0.22f)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = triggerBg,
                border = BorderStroke(
                    width = if (expanded) 1.5.dp else 1.dp,
                    color = accentColor.copy(alpha = if (expanded) 0.85f else 0.45f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = sessions.isNotEmpty()) {
                        expanded = !expanded
                        if (!expanded) searchQuery = ""
                    }
                    .testTag(testTagPrefix)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = triggerTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = selectedLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = triggerTint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (sessions.size > 1) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = if (isLight) 0.14f else 0.30f)
                        ) {
                            Text(
                                text = "${sessions.size}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = triggerTint,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isEn) "Choose session" else "Pilih sesi",
                        tint = triggerTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            trailingAction?.invoke()
        }

        // Dropdown membuka ke bawah (inline) dengan daftar scrollable agar tidak menutupi header atas
        AnimatedVisibility(
            visible = expanded && sessions.isNotEmpty(),
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${testTagPrefix}_panel"),
                shape = RoundedCornerShape(14.dp),
                color = panelBg,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Bar info ringkas jumlah sesi & petunjuk scroll
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isLight) accentLightColor.copy(alpha = 0.32f)
                                else accentColor.copy(alpha = 0.14f)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEn) {
                                "${filteredSessions.size} of ${sessions.size} sessions • Scroll list"
                            } else {
                                "${filteredSessions.size} dari ${sessions.size} sesi • Gulir untuk lihat lainnya"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = triggerTint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (isEn) "Close" else "Tutup",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = triggerTint,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    expanded = false
                                    searchQuery = ""
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Kolom cari cepat muncul otomatis jika sesi lebih dari 4 agar mudah menemukan sesi lama
                    if (sessions.size > 4) {
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
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = if (isEn) "Search session or date..." else "Cari nama sesi atau tanggal...",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            cursorBrush = SolidColor(accentColor),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("${testTagPrefix}_search")
                                        )
                                    }
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchQuery = "" },
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
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }

                    if (filteredSessions.isEmpty()) {
                        Text(
                            text = if (isEn) "No matching session found." else "Sesi tidak ditemukan.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(vertical = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
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
                                            color = accentColor.copy(alpha = 0.45f),
                                            topLeft = Offset(size.width - 5.dp.toPx(), thumbY + 3.dp.toPx()),
                                            size = Size(3.5.dp.toPx(), (thumbHeight - 6.dp.toPx()).coerceAtLeast(14.dp.toPx())),
                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                    }
                                }
                        ) {
                            itemsIndexed(
                                items = filteredSessions,
                                key = { _, session -> session.id }
                            ) { idx, session ->
                                val isSelected = selectedSession?.id == session.id
                                val isNewest = session.id == newestSessionId
                                val dateStr = DateUtils.formatShortDate(session.sessionDate, lang)

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .background(if (isSelected) selectedRowBg else Color.Transparent)
                                            .clickable {
                                                onSelectSession(session)
                                                expanded = false
                                                searchQuery = ""
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("${testTagPrefix}_item_${session.id}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Text(
                                                    text = session.title,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                    color = if (isSelected) triggerTint else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (isNewest) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (isLight) accentLightColor else accentColor.copy(alpha = 0.28f)
                                                    ) {
                                                        Text(
                                                            text = if (isEn) "Latest" else "Terbaru",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = triggerTint,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                if (!session.isActive) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = MaterialTheme.colorScheme.surfaceVariant
                                                    ) {
                                                        Text(
                                                            text = if (isEn) "Done" else "Selesai",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = dateStr,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = triggerTint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    if (idx < filteredSessions.lastIndex) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
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
}

