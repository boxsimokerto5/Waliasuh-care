package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.util.i18n.LocalAppStrings

/**
 * Dialog deklarasi eksplisit sebelum memutar Rewarded Video Ad
 * agar transparan dan mematuhi kebijakan Google Play & jaringan iklan.
 */
@Composable
fun RewardAdDeclarationDialog(
    title: String? = null,
    actionDescription: String? = null,
    confirmButtonText: String? = null,
    onConfirmWatchAd: () -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish

    val resolvedTitle = title ?: if (isEn) "Watch Video to Continue" else "Tonton Video untuk Lanjutkan"
    val resolvedActionDesc = actionDescription
        ?: if (isEn) "download and open the PDF Report" else "mengunduh dan membuka Laporan PDF"
    val resolvedConfirmText = confirmButtonText
        ?: if (isEn) "Watch & Continue" else "Tonton & Lanjutkan"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(WaliPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.OndemandVideo,
                    contentDescription = null,
                    tint = WaliPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = resolvedTitle,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isEn) {
                        "Watch a short video ad until the end to $resolvedActionDesc."
                    } else {
                        "Tonton iklan video singkat hingga selesai untuk $resolvedActionDesc."
                    },
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 19.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isEn) {
                            "If a video ad is unavailable right now, the process will continue immediately without waiting."
                        } else {
                            "Jika iklan video belum tersedia saat ini, proses akan langsung dilanjutkan tanpa menunggu."
                        },
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmWatchAd,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WaliPrimary),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("btn_confirm_reward_ad")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = resolvedConfirmText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("btn_cancel_reward_ad")
            ) {
                Text(s.cancel, fontSize = 12.5.sp)
            }
        }
    )
}

/**
 * Banner / catatan deklarasi singkat di UI bahwa fitur PDF/Laporan memakai iklan video berhadiah.
 */
@Composable
fun RewardAdDeclarationHint(
    text: String? = null,
    accentColor: Color = Color(0xFF0369A1),
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val resolvedText = text ?: if (s.isEnglish) {
        "Watch a short video ad to download/open the PDF Report"
    } else {
        "Tonton iklan video singkat untuk mengunduh/membuka Laporan PDF"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .border(1.dp, accentColor.copy(alpha = 0.22f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.OndemandVideo,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = resolvedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = accentColor,
            lineHeight = 15.sp
        )
    }
}
