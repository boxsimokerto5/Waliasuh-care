package com.example.ui.dialogs

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SchoolProfile
import com.example.ui.UserRole
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageGuideSheet(
    schoolProfile: SchoolProfile,
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onStartInteractiveTour: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val faskesSummaryId = remember(schoolProfile) {
        schoolProfile.getAllFaskes()
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" / ")
            ?: "Faskes Rujukan"
    }
    val faskesSummaryEn = remember(schoolProfile) {
        schoolProfile.getAllFaskes()
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" / ")
            ?: "Referral Facility"
    }

    val roleOverviews = remember { buildGuideRoleOverviews() }
    val allSections = remember(faskesSummaryId, faskesSummaryEn) {
        buildGuideSections(faskesSummaryId = faskesSummaryId, faskesSummaryEn = faskesSummaryEn)
    }

    val activeUserFilter = remember(currentRole) { GuideRoleFilter.fromUserRole(currentRole) }
    var selectedRole by rememberSaveable { mutableStateOf(GuideRoleFilter.ALL) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var expandedIds by remember { mutableStateOf(setOf("kesehatan")) }

    val filteredSections = remember(allSections, searchQuery, selectedRole, isEn) {
        allSections.filter { it.matches(query = searchQuery, roleFilter = selectedRole, isEn = isEn) }
    }
    val filteredRoles = remember(roleOverviews, selectedRole) {
        if (selectedRole == GuideRoleFilter.ALL) roleOverviews else roleOverviews.filter { it.role == selectedRole }
    }
    val allFilteredExpanded = filteredSections.isNotEmpty() && filteredSections.all { it.id in expandedIds }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                // Header & Controls (pinned above scroll list)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                                        )
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) "Complete App Usage Guide" else "Panduan Lengkap Aplikasi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEn) {
                                        "Covers ${allSections.size} core modules & all user roles"
                                    } else {
                                        "Mencakup ${allSections.size} modul fitur lengkap & semua peran"
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    expandedIds = if (allFilteredExpanded) {
                                        emptySet()
                                    } else {
                                        filteredSections.map { it.id }.toSet()
                                    }
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("guide_toggle_all_btn")
                            ) {
                                Icon(
                                    imageVector = if (allFilteredExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                                    contentDescription = if (allFilteredExpanded) {
                                        if (isEn) "Collapse all sections" else "Tutup semua kategori"
                                    } else {
                                        if (isEn) "Expand all sections" else "Buka semua kategori"
                                    },
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = if (isEn) {
                                    "Search feature (e.g. Quran, Christian, Visit, PDF)..."
                                } else {
                                    "Cari panduan (misal: Al-Qur'an, Kristen, Kunjungan, PDF)..."
                                },
                                fontSize = 12.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = if (isEn) "Search guide" else "Cari panduan",
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = if (isEn) "Clear search" else "Hapus pencarian",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("guide_search_input")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(GuideRoleFilter.entries, key = { it.id }) { roleFilter ->
                            val isSelected = selectedRole == roleFilter
                            val isCurrentUserRole = roleFilter != GuideRoleFilter.ALL && roleFilter == activeUserFilter
                            val baseLabel = if (isEn) roleFilter.labelEn else roleFilter.labelId
                            val label = if (isCurrentUserRole) {
                                if (isEn) "$baseLabel (You)" else "$baseLabel (Anda)"
                            } else {
                                baseLabel
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedRole = roleFilter },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("guide_role_chip_${roleFilter.id}")
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Scrollable guide content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (searchQuery.isBlank() && filteredRoles.isNotEmpty()) {
                        item(key = "guide_roles_header") {
                            Text(
                                text = if (isEn) "Roles & Access Scope" else "Peran & Cakupan Akses",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        item(key = "guide_roles_row") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredRoles, key = { it.role.id }) { overview ->
                                    RoleOverviewCard(
                                        overview = overview,
                                        isCurrentUser = overview.role == activeUserFilter,
                                        isEn = isEn
                                    )
                                }
                            }
                        }
                    }

                    item(key = "guide_features_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) {
                                    "Feature Guide (${filteredSections.size})"
                                } else {
                                    "Panduan Fitur (${filteredSections.size})"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (searchQuery.isNotBlank() || selectedRole != GuideRoleFilter.ALL) {
                                TextButton(
                                    onClick = {
                                        searchQuery = ""
                                        selectedRole = GuideRoleFilter.ALL
                                    },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(if (isEn) "Reset Filter" else "Reset Filter", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (filteredSections.isEmpty()) {
                        item(key = "guide_empty") {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = if (isEn) "No matching guide topic found" else "Topik panduan tidak ditemukan",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp
                                    )
                                    Text(
                                        text = if (isEn) {
                                            "Try another keyword or reset the role filter."
                                        } else {
                                            "Coba kata kunci lain atau tekan Reset Filter."
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredSections, key = { it.id }) { section ->
                            val isExpanded = searchQuery.isNotBlank() || section.id in expandedIds
                            GuideAccordionCard(
                                section = section,
                                isExpanded = isExpanded,
                                isEn = isEn,
                                onToggle = {
                                    expandedIds = if (section.id in expandedIds) {
                                        expandedIds - section.id
                                    } else {
                                        expandedIds + section.id
                                    }
                                }
                            )
                        }
                    }

                    item(key = "guide_disclaimer") {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                0.8.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isEn) "About & App Status" else "Tentang & Status Aplikasi",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.disclaimer_full),
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Bottom Sticky Action Bar
                Surface(
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onStartInteractiveTour()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_guide_start_tour")
                            ) {
                                Text(
                                    text = if (isEn) "Interactive Tour" else "Putar Tur Fitur",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_guide_close")
                            ) {
                                Text(s.nav.closeBtn)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleOverviewCard(
    overview: GuideRoleOverview,
    isCurrentUser: Boolean,
    isEn: Boolean
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) {
                overview.accent.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ),
        border = BorderStroke(
            width = if (isCurrentUser) 1.2.dp else 0.8.dp,
            color = if (isCurrentUser) overview.accent.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.width(216.dp)
    ) {
        Column(
            modifier = Modifier.padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(overview.accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = overview.icon,
                        contentDescription = null,
                        tint = overview.accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEn) overview.titleEn else overview.titleId,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isCurrentUser) {
                        Text(
                            text = if (isEn) "Active Role" else "Peran Aktif",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = overview.accent
                        )
                    }
                }
            }
            Text(
                text = if (isEn) overview.summaryEn else overview.summaryId,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GuideAccordionCard(
    section: GuideSection,
    isExpanded: Boolean,
    isEn: Boolean,
    onToggle: () -> Unit
) {
    val steps = if (isEn) section.stepsEn else section.stepsId
    val tip = if (isEn) section.tipEn else section.tipId

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) {
                section.accent.copy(alpha = 0.06f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f)
            }
        ),
        border = BorderStroke(
            width = if (isExpanded) 1.dp else 0.8.dp,
            color = if (isExpanded) section.accent.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("guide_section_${section.id}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(section.accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = null,
                        tint = section.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isEn) section.titleEn else section.titleId,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEn) section.subtitleEn else section.subtitleId,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = section.accent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${steps.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = section.accent,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) {
                        if (isEn) "Collapse" else "Tutup"
                    } else {
                        if (isEn) "Expand" else "Buka"
                    },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    HorizontalDivider(color = section.accent.copy(alpha = 0.2f))
                    steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(section.accent.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = section.accent
                                )
                            }
                            Text(
                                text = step,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (!tip.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = section.accent.copy(alpha = 0.1f),
                            border = BorderStroke(0.6.dp, section.accent.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = section.accent,
                                    modifier = Modifier
                                        .padding(top = 1.dp)
                                        .size(15.dp)
                                )
                                Text(
                                    text = tip,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
