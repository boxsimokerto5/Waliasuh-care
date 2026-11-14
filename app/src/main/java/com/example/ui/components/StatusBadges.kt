package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusDianter
import com.example.ui.theme.StatusDianterBg
import com.example.ui.theme.StatusMenunggu
import com.example.ui.theme.StatusMenungguBg
import com.example.ui.theme.StatusNeedControl
import com.example.ui.theme.StatusNoControl
import com.example.ui.theme.StatusPlanned
import com.example.ui.theme.StatusSiapJalan
import com.example.ui.theme.StatusSiapJalanBg
import com.example.util.i18n.LocalAppStrings

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "MENUNGGU", "PLANNED" -> Triple(StatusMenungguBg, StatusMenunggu, if (isEn) "Waiting" else "Menunggu")
        "SIAP JALAN" -> Triple(StatusSiapJalanBg, StatusSiapJalan, if (isEn) "Ready to Go" else "Siap Jalan")
        "DIANTER", "DEPARTED" -> Triple(StatusDianterBg, StatusDianter, if (isEn) "Escorted" else "Dianter")
        "SELESAI", "COMPLETED" -> Triple(Color(0xFFDBEAFE), Color(0xFF1D4ED8), if (isEn) "Completed" else "Selesai")
        "CANCELLED", "BATAL" -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), if (isEn) "Cancelled" else "Batal")
        else -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), status)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 2.5.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ControlBadge(
    needsControl: Boolean,
    controlDateFormatted: String?,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    val (bgColor, textColor, label) = when {
        !needsControl -> Triple(
            StatusNoControl.copy(alpha = 0.10f),
            StatusNoControl,
            if (isEn) "No Follow-up" else "Tidak Perlu Kontrol"
        )
        isCompleted -> Triple(
            StatusCompleted.copy(alpha = 0.12f),
            StatusCompleted,
            if (isEn) "Follow-up Done" else "Kontrol Selesai"
        )
        else -> Triple(
            StatusNeedControl.copy(alpha = 0.12f),
            StatusNeedControl,
            if (isEn) "Follow-up: ${controlDateFormatted ?: "Scheduled"}" else "Kontrol: ${controlDateFormatted ?: "Terjadwal"}"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 2.5.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun FaskesBadge(
    faskes: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = if (faskes.contains("Wates", ignoreCase = true)) {
        Pair(Color(0xFFE0F2F1), Color(0xFF00695C))
    } else if (faskes.contains("Klaten", ignoreCase = true) || faskes.contains("Plosoklaten", ignoreCase = true)) {
        Pair(Color(0xFFE8F5E9), Color(0xFF1B5E20))
    } else {
        Pair(Color(0xFFEDE7F6), Color(0xFF512DA8))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = faskes,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
