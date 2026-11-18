package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import java.io.File

@Composable
fun StudentAvatar(
    name: String,
    gender: String = "L",
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    photoUrl: String = "",
    photoFallbackUrl: String = ""
) {
    val context = LocalContext.current
    val primaryUrl = photoUrl.trim()
    val fallbackUrl = photoFallbackUrl.trim()
    var useFallback by remember(primaryUrl, fallbackUrl) { mutableStateOf(false) }
    var imageFailed by remember(primaryUrl, fallbackUrl) { mutableStateOf(false) }

    val activeUrl = when {
        imageFailed -> ""
        !useFallback && primaryUrl.isNotBlank() -> primaryUrl
        fallbackUrl.isNotBlank() -> fallbackUrl
        else -> ""
    }

    // Elegant soft gradient backgrounds inspired by the student portraits
    val gradientColors = if (gender.equals("P", ignoreCase = true)) {
        listOf(Color(0xFFFDE8E8), Color(0xFFFBCFE8), Color(0xFFF472B6))
    } else {
        listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD), Color(0xFF38BDF8))
    }

    val initials = name.trim().split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .map { it.first().uppercase() }
        .joinToString("")

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradientColors))
            .border(1.5.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (initials.isNotEmpty()) {
            Text(
                text = initials,
                fontSize = (size.value * 0.38f).sp,
                fontWeight = FontWeight.Bold,
                color = if (gender.equals("P", ignoreCase = true)) Color(0xFF9D174D) else Color(0xFF0369A1)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = name,
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f)
            )
        }

        if (activeUrl.isNotBlank()) {
            val requestData: Any = if (activeUrl.startsWith("/")) File(activeUrl) else activeUrl
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(requestData)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                onError = {
                    if (!useFallback && fallbackUrl.isNotBlank() && fallbackUrl != primaryUrl) {
                        useFallback = true
                    } else {
                        imageFailed = true
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}

