package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WaliAccount
import com.example.ui.components.StudentAvatar
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaliOfficialProfileSheet(
    waliName: String,
    waliAccount: WaliAccount?,
    schoolName: String,
    dormitory: String = "",
    roleBadgeLabel: String = "",
    shiftInfoLabel: String = "",
    isOnDutyNow: Boolean = false,
    onOpenChat: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPhotoZoom by remember { mutableStateOf(false) }

    val displayName = waliAccount?.name?.takeIf { it.isNotBlank() }
        ?: waliName.ifBlank { if (isEn) "Foster Caregiver" else "Wali Asuh Pembina" }
    val displayTitle = waliAccount?.effectiveTitle(isEn) ?: if (isEn) "Foster Caregiver" else "Wali Asuh Pembina"
    val displayDorm = waliAccount?.dormitory?.takeIf { it.isNotBlank() } ?: dormitory
    val displayPhone = waliAccount?.phone?.trim().orEmpty()
    val displayEmployeeCode = waliAccount?.employeeCode?.trim().orEmpty()
    val displayBio = waliAccount?.bio?.trim().orEmpty()
    val photoUrl = waliAccount?.photoUrl?.trim().orEmpty()
    val photoFallbackUrl = waliAccount?.photoFallbackUrl?.trim().orEmpty()
    val hasPhoto = photoUrl.isNotBlank() || photoFallbackUrl.isNotBlank()
    val resolvedSchool = schoolName.ifBlank { if (isEn) "Registered School" else "Sekolah Terdaftar" }
    val avatarGender = if (displayDorm.contains("putri", ignoreCase = true) || displayDorm.contains("banat", ignoreCase = true)) {
        "P"
    } else {
        "L"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 580.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 28.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("sheet_wali_official_profile"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Title + Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = if (isEn) "Verified Caregiver Profile" else "Profil Resmi Wali Asuh",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn) "Official identity registered at school" else "Identitas resmi pembina terdaftar di sekolah",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_wali_official_profile")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close" else "Tutup"
                        )
                    }
                }

                // Hero Card with Photo + Verified School Badge
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, Color(0xFF3730A3).copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E3A8A), Color(0xFF3730A3), Color(0xFF4338CA))
                                )
                            )
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.24f),
                            border = BorderStroke(1.dp, Color(0xFF6EE7B7).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color(0xFF6EE7B7),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isEn) "OFFICIAL SCHOOL CAREGIVER" else "PEMBINA RESMI TERVERIFIKASI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFD1FAE5),
                                    letterSpacing = 0.4.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.22f))
                                .let { mod ->
                                    if (hasPhoto) {
                                        mod.clickable { showPhotoZoom = true }
                                            .testTag("btn_zoom_wali_official_photo")
                                    } else mod
                                }
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            StudentAvatar(
                                name = displayName,
                                gender = avatarGender,
                                size = 94.dp,
                                photoUrl = photoUrl,
                                photoFallbackUrl = photoFallbackUrl
                            )
                            if (hasPhoto) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF1E3A8A),
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
                            text = displayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = displayTitle,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE0E7FF),
                            textAlign = TextAlign.Center
                        )
                        if (roleBadgeLabel.isNotBlank() || isOnDutyNow || shiftInfoLabel.isNotBlank()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (roleBadgeLabel.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color.White.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = roleBadgeLabel,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (isOnDutyNow) {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.28f),
                                        border = BorderStroke(1.dp, Color(0xFF6EE7B7))
                                    ) {
                                        Text(
                                            text = if (isEn) "On Duty Now" else "Sedang Piket",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFD1FAE5),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                } else if (shiftInfoLabel.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color.White.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = shiftInfoLabel,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFE0E7FF),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                        if (hasPhoto) {
                            Text(
                                text = if (isEn) "Tap photo to zoom full size" else "Ketuk foto untuk memperbesar",
                                fontSize = 10.5.sp,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                    }
                }

                // Credential & Identity Details Card (Never shows username or password!)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isEn) "Caregiver Credentials & Assignment" else "Identitas & Penugasan Pembina",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3730A3)
                        )
                        WaliInfoRow(
                            icon = Icons.Default.School,
                            label = if (isEn) "Institution / School" else "Sekolah / Lembaga",
                            value = resolvedSchool
                        )
                        if (displayDorm.isNotBlank()) {
                            WaliInfoRow(
                                icon = Icons.Default.Apartment,
                                label = if (isEn) "Dormitory Unit" else "Unit Asrama Binaan",
                                value = displayDorm
                            )
                        }
                        if (displayEmployeeCode.isNotBlank()) {
                            WaliInfoRow(
                                icon = Icons.Default.Badge,
                                label = if (isEn) "Official ID / NIP" else "NIP / ID Resmi Pembina",
                                value = displayEmployeeCode
                            )
                        }
                        if (displayPhone.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color(0xFF047857),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isEn) "WhatsApp / Contact" else "Nomor Kontak / WhatsApp",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = displayPhone,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("WhatsApp", displayPhone))
                                            Toast.makeText(
                                                context,
                                                if (isEn) "Phone number copied" else "Nomor kontak disalin",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = if (isEn) "Copy phone" else "Salin nomor",
                                            tint = Color(0xFF3730A3),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Surface(
                                        onClick = {
                                            val digits = displayPhone.filter { it.isDigit() }
                                            val waPhone = if (digits.startsWith("0")) "62" + digits.substring(1) else digits
                                            runCatching {
                                                context.startActivity(
                                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$waPhone"))
                                                )
                                            }.onFailure {
                                                Toast.makeText(
                                                    context,
                                                    if (isEn) "Cannot open WhatsApp" else "Tidak dapat membuka WhatsApp",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFDCFCE7),
                                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                                        modifier = Modifier.testTag("btn_wali_profile_open_wa")
                                    ) {
                                        Text(
                                            text = "WhatsApp",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (displayBio.isNotBlank()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isEn) "Caregiver Greeting & Profile Note" else "Sambutan & Profil Singkat Pembina",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEEF2FF).copy(alpha = 0.65f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "\"$displayBio\"",
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }

                        if ((waliAccount?.profileUpdatedAt ?: 0L) > 0L) {
                            Text(
                                text = "${if (isEn) "Profile updated:" else "Profil diperbarui:"} ${DateUtils.formatDateTime(waliAccount!!.profileUpdatedAt, s.language)}",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Legitimacy Assurance Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 1.dp)
                        )
                        Text(
                            text = if (isEn) {
                                "This caregiver account is verified under $resolvedSchool to safeguard parent communication and foster child records."
                            } else {
                                "Akun pembina ini terdaftar dan terverifikasi di bawah naungan $resolvedSchool untuk menjamin keamanan komunikasi dan keabsahan data ananda."
                            },
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                if (onOpenChat != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenChat()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3730A3)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_wali_official_profile_chat")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEn) "Send Message to Caregiver" else "Kirim Pesan ke Wali Asuh",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(if (isEn) "Close" else "Tutup", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showPhotoZoom && hasPhoto) {
        StudentPhotoPreviewDialog(
            studentName = displayName,
            gender = avatarGender,
            subtitle = "$displayTitle • $resolvedSchool",
            photoUrl = photoUrl,
            photoFallbackUrl = photoFallbackUrl,
            onDismiss = { showPhotoZoom = false }
        )
    }
}

@Composable
private fun WaliInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF4338CA),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
