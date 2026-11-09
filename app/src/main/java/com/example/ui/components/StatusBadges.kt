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
import com.example.ui.theme.StatusDeparted
import com.example.ui.theme.StatusNeedControl
import com.example.ui.theme.StatusNoControl
import com.example.ui.theme.StatusPlanned

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        "PLANNED" -> Triple(StatusPlanned.copy(alpha = 0.12f), StatusPlanned, "Rencana")
        "DEPARTED" -> Triple(StatusDeparted.copy(alpha = 0.14f), StatusDeparted, "Berangkat")
        "COMPLETED" -> Triple(StatusCompleted.copy(alpha = 0.14f), StatusCompleted, "Telah Diobati")
        "CANCELLED" -> Triple(Color.Gray.copy(alpha = 0.14f), Color.Gray, "Batal")
        else -> Triple(Color.LightGray.copy(alpha = 0.2f), Color.DarkGray, status)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
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
    val (bgColor, textColor, label) = when {
        !needsControl -> Triple(
            StatusNoControl.copy(alpha = 0.10f),
            StatusNoControl,
            "Tidak Perlu Kontrol"
        )
        isCompleted -> Triple(
            StatusCompleted.copy(alpha = 0.12f),
            StatusCompleted,
            "Kontrol Selesai"
        )
        else -> Triple(
            StatusNeedControl.copy(alpha = 0.12f),
            StatusNeedControl,
            "Kontrol: ${controlDateFormatted ?: "Terjadwal"}"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
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
    } else if (faskes.contains("Plosoklaten", ignoreCase = true)) {
        Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
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
