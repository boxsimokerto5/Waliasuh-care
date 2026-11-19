package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun StudentLargeProfilePhoto(
    name: String,
    gender: String = "L",
    photoUrl: String = "",
    photoFallbackUrl: String = "",
    emptyHint: String = "",
    showZoomBadge: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    borderColor: Color = Color.White.copy(alpha = 0.75f),
    placeholderAvatarSize: Dp = 88.dp,
    modifier: Modifier = Modifier
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

    val bgGradient = if (gender.equals("P", ignoreCase = true)) {
        listOf(Color(0xFFFDF2F8), Color(0xFFFCE7F3), Color(0xFFFBCFE8))
    } else {
        listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE), Color(0xFFBAE6FD))
    }
    val accentTint = if (gender.equals("P", ignoreCase = true)) Color(0xFF9D174D) else Color(0xFF0369A1)

    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(bgGradient))
            .border(1.5.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
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
                modifier = Modifier.fillMaxSize()
            )
            if (showZoomBadge) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = name,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(12.dp)
            ) {
                StudentAvatar(
                    name = name,
                    gender = gender,
                    size = placeholderAvatarSize
                )
                if (emptyHint.isNotBlank()) {
                    Text(
                        text = emptyHint,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentTint,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

