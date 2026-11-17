package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
    testTagPrefix: String = "dropdown_fasting_session"
) {
    var expanded by remember { mutableStateOf(false) }

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

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = accentLightColor.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = sessions.isNotEmpty()) { expanded = true }
                .testTag(testTagPrefix)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = selectedLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isEn) "Choose session" else "Pilih sesi",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .heightIn(max = 300.dp)
        ) {
            sessions.forEachIndexed { index, session ->
                val isSelected = selectedSession?.id == session.id
                val dateStr = DateUtils.formatShortDate(session.sessionDate, lang)
                DropdownMenuItem(
                    onClick = {
                        onSelectSession(session)
                        expanded = false
                    },
                    modifier = Modifier
                        .background(if (isSelected) accentLightColor.copy(alpha = 0.45f) else Color.Transparent)
                        .testTag("${testTagPrefix}_item_${session.id}"),
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (index == 0) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = accentLightColor
                                        ) {
                                            Text(
                                                text = if (isEn) "Latest" else "Terbaru",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = accentColor,
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
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
