package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import com.example.data.model.SchoolProfile
import com.example.ui.SwitchableSchoolOption
import com.example.ui.theme.WaliPrimary
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnakSuperAdminSchoolSwitcherCard(
    activeSchoolProfile: SchoolProfile,
    availableSchools: List<SwitchableSchoolOption>,
    isSwitchingSchool: Boolean,
    onSelectSchool: (SwitchableSchoolOption) -> Unit,
    onRefreshSchools: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    var expanded by remember { mutableStateOf(false) }

    val activeCode = activeSchoolProfile.tenantCode.ifBlank { "SRT1-KDR" }
    val selectedOption = availableSchools.firstOrNull {
        it.tenantCode.equals(activeCode, ignoreCase = true)
    } ?: SwitchableSchoolOption(
        tenantCode = activeCode,
        schoolName = activeSchoolProfile.schoolName.ifBlank { activeCode },
        city = activeSchoolProfile.city.ifBlank { "Indonesia" }
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_anaksuperadmin_school_switcher"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, WaliPrimary.copy(alpha = 0.28f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(WaliPrimary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = if (isEn) "Active school switcher" else "Pemilih sekolah aktif",
                                tint = WaliPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Active School (Tenant)" else "Pilih Sekolah Aktif",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn) {
                                    "${availableSchools.size} active schools registered"
                                } else {
                                    "${availableSchools.size} sekolah aktif tersedia"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WaliPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = selectedOption.tenantCode,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = WaliPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (isSwitchingSchool) {
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = WaliPrimary
                                )
                            }
                        } else {
                            IconButton(
                                onClick = onRefreshSchools,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("btn_refresh_schools_dropdown")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = if (isEn) "Refresh school list" else "Muat ulang daftar sekolah",
                                    tint = WaliPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        if (!isSwitchingSchool) {
                            expanded = !expanded
                        }
                    }
                ) {
                    OutlinedTextField(
                        value = selectedOption.schoolName,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = {
                            Text(
                                text = if (isEn) "Target School" else "Sekolah Tujuan Data & Aktivitas",
                                fontSize = 12.sp
                            )
                        },
                        supportingText = {
                            Text(
                                text = "${selectedOption.tenantCode} • ${selectedOption.city}",
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = WaliPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WaliPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                            .testTag("dropdown_anaksuperadmin_school")
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableSchools.forEach { option ->
                            val isSelected = option.tenantCode.equals(selectedOption.tenantCode, ignoreCase = true)
                            DropdownMenuItem(
                                modifier = Modifier.testTag("school_option_${option.tenantCode}"),
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.School,
                                        contentDescription = null,
                                        tint = if (isSelected) WaliPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                text = {
                                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(
                                            text = option.schoolName,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected) WaliPrimary else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = buildString {
                                                append(option.tenantCode)
                                                if (option.city.isNotBlank()) {
                                                    append(" • ")
                                                    append(option.city)
                                                }
                                                if (option.isDefaultPilot) {
                                                    append(if (isEn) " • Pilot" else " • Utama")
                                                }
                                            },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                trailingIcon = if (isSelected) {
                                    {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = WaliPrimary.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = if (isEn) "Active" else "Aktif",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WaliPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                } else null,
                                onClick = {
                                    expanded = false
                                    if (!isSelected) {
                                        onSelectSchool(option)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(0.dp))
                Text(
                    text = if (isEn) {
                        "Switching school automatically isolates student records and routes all new activities to ${selectedOption.tenantCode}."
                    } else {
                        "Semua data siswa & pencatatan aktivitas baru otomatis masuk ke sekolah terpilih (${selectedOption.tenantCode})."
                    },
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
