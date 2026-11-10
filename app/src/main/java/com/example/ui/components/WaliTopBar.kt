package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WaliPrimary

@Composable
fun WaliTopBar(
    notificationCount: Int = 0,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    isDoctorIcon: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .displayCutoutPadding(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo & Brand Name: "Wali Asuh Care"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("brand_logo_title")
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(WaliPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = "Logo Wali Asuh Care",
                            tint = WaliPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(9.dp))
                    Column {
                        Text(
                            text = "Wali Asuh Care",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = WaliPrimary,
                            letterSpacing = 0.1.sp
                        )
                        Text(
                            text = "SRT 1 Kabupaten Kediri",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right Action: Notification Bell OR Doctor Avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isDoctorIcon) {
                        IconButton(
                            onClick = onNotificationClick,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("btn_doctor_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = "Rekam Medis Dokter",
                                tint = WaliPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onNotificationClick,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("btn_notification_bell")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (notificationCount > 0) {
                                        Badge(
                                            containerColor = Color(0xFFEF4444),
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = if (notificationCount > 99) "99+" else "$notificationCount",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifikasi Pengingat",
                                    tint = if (notificationCount > 0) WaliPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Avatar Thumbnail
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(WaliPrimary.copy(alpha = 0.14f))
                            .clickable(onClick = onProfileClick)
                            .testTag("btn_top_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil Wali",
                            tint = WaliPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
