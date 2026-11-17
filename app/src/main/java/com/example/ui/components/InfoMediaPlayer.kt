package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.TextureView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.InfoPentingHelper
import kotlinx.coroutines.delay

fun formatVideoDurationMs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val mins = totalSec / 60L
    val secs = totalSec % 60L
    return String.format("%02d:%02d", mins, secs)
}

fun openInfoDocumentOrLink(context: Context, rawUrl: String, isEn: Boolean) {
    val normalized = InfoPentingHelper.normalizeExternalLink(rawUrl)
    if (normalized.isBlank()) return
    val opened = runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }.isSuccess
    if (!opened) {
        Toast.makeText(
            context,
            if (isEn) "Unable to open attachment/link." else "Tidak dapat membuka lampiran/tautan.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@OptIn(UnstableApi::class)
@Composable
fun InfoVideoPlayerCard(
    postId: Long,
    primaryUrl: String,
    fallbackUrl: String,
    title: String,
    isEn: Boolean,
    onOpenFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRobolectric = remember {
        Build.FINGERPRINT.equals("robolectric", ignoreCase = true)
    }
    var isPlaying by remember(postId, primaryUrl) { mutableStateOf(false) }
    var isMuted by remember(postId) { mutableStateOf(false) }
    var durationMs by remember(postId, primaryUrl) { mutableLongStateOf(0L) }
    var positionMs by remember(postId, primaryUrl) { mutableLongStateOf(0L) }
    var hasRenderedFrame by remember(postId, primaryUrl) { mutableStateOf(false) }
    var hasError by remember(postId, primaryUrl) { mutableStateOf(false) }

    if (isRobolectric) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF115E59), Color(0xFF0369A1))
                    )
                )
                .testTag("info_video_player_card_$postId")
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier
                        .size(52.dp)
                        .clickable { isPlaying = !isPlaying }
                        .testTag("btn_video_play_pause_$postId")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause video" else "Play video",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Text(
                    text = title.ifBlank { if (isEn) "Video Attachment" else "Pemutar Video Saluran" },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formatVideoDurationMs(positionMs)} / ${formatVideoDurationMs(durationMs)}",
                    fontSize = 10.sp,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Mute toggle",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onOpenFullscreen,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_video_fullscreen_$postId")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var activeUrl by remember(primaryUrl, fallbackUrl) {
        mutableStateOf(primaryUrl.ifBlank { fallbackUrl })
    }
    var triedFallback by remember(primaryUrl, fallbackUrl) { mutableStateOf(false) }

    val exoPlayer = remember(context, postId) {
        runCatching { ExoPlayer.Builder(context).build() }.getOrNull()
    }

    if (exoPlayer == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isEn) "Video player unavailable" else "Pemutar video tidak tersedia",
                color = Color.White,
                fontSize = 12.sp
            )
        }
        return
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                exoPlayer.pause()
                isPlaying = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                hasRenderedFrame = true
            }

            override fun onIsPlayingChanged(playingNow: Boolean) {
                isPlaying = playingNow
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    durationMs = exoPlayer.duration.coerceAtLeast(0L)
                    hasError = false
                } else if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                    exoPlayer.seekTo(0L)
                    exoPlayer.pause()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                if (!triedFallback && fallbackUrl.isNotBlank() && activeUrl != fallbackUrl) {
                    triedFallback = true
                    hasRenderedFrame = false
                    activeUrl = fallbackUrl
                } else {
                    hasError = true
                    isPlaying = false
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            runCatching { exoPlayer.release() }
        }
    }

    LaunchedEffect(exoPlayer, activeUrl) {
        if (activeUrl.isNotBlank()) {
            hasError = false
            hasRenderedFrame = false
            exoPlayer.setMediaItem(MediaItem.fromUri(activeUrl))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = false
        }
    }

    LaunchedEffect(exoPlayer, isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    LaunchedEffect(exoPlayer, isPlaying) {
        while (true) {
            positionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            if (exoPlayer.duration > 0L) durationMs = exoPlayer.duration
            delay(if (isPlaying) 300L else 900L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A))
            .testTag("info_video_player_card_$postId")
    ) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    isOpaque = false
                    exoPlayer.setVideoTextureView(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!hasRenderedFrame) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0F172A), Color(0xFF0F766E), Color(0xFF0284C7))
                        )
                    )
            )
        }

        // Center Play / Pause Tap Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    if (isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (!isPlaying || !hasRenderedFrame) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.62f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("btn_video_play_pause_$postId")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        if (hasError) {
            Surface(
                color = Color(0xCC991B1B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = if (isEn) "Unable to stream video" else "Gagal memuat video",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Bottom Controls Bar (Play/Pause, Seekbar, Time, Mute, Fullscreen)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                    )
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val maxDur = durationMs.coerceAtLeast(1L).toFloat()
            Slider(
                value = positionMs.toFloat().coerceIn(0f, maxDur),
                onValueChange = { target ->
                    positionMs = target.toLong()
                    exoPlayer.seekTo(target.toLong())
                },
                valueRange = 0f..maxDur,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF2DD4BF),
                    activeTrackColor = Color(0xFF2DD4BF),
                    inactiveTrackColor = Color.White.copy(alpha = 0.32f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${formatVideoDurationMs(positionMs)} / ${formatVideoDurationMs(durationMs)}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            exoPlayer.pause()
                            onOpenFullscreen()
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("btn_video_fullscreen_$postId")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun FullscreenVideoPlayerDialog(
    primaryUrl: String,
    fallbackUrl: String,
    title: String,
    isEn: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val context = LocalContext.current
        val isRobolectric = remember { Build.FINGERPRINT.equals("robolectric", ignoreCase = true) }
        var activeUrl by remember(primaryUrl, fallbackUrl) {
            mutableStateOf(primaryUrl.ifBlank { fallbackUrl })
        }
        var triedFallback by remember(primaryUrl, fallbackUrl) { mutableStateOf(false) }
        var isPlaying by remember { mutableStateOf(true) }
        var isMuted by remember { mutableStateOf(false) }
        var durationMs by remember { mutableLongStateOf(0L) }
        var positionMs by remember { mutableLongStateOf(0L) }

        val exoPlayer = remember(context) {
            if (isRobolectric) null else runCatching { ExoPlayer.Builder(context).build() }.getOrNull()
        }

        if (exoPlayer != null) {
            DisposableEffect(exoPlayer) {
                val listener = object : Player.Listener {
                    override fun onIsPlayingChanged(playingNow: Boolean) {
                        isPlaying = playingNow
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            durationMs = exoPlayer.duration.coerceAtLeast(0L)
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        if (!triedFallback && fallbackUrl.isNotBlank() && activeUrl != fallbackUrl) {
                            triedFallback = true
                            activeUrl = fallbackUrl
                        }
                    }
                }
                exoPlayer.addListener(listener)
                onDispose {
                    exoPlayer.removeListener(listener)
                    runCatching { exoPlayer.release() }
                }
            }

            LaunchedEffect(exoPlayer, activeUrl) {
                if (activeUrl.isNotBlank()) {
                    exoPlayer.setMediaItem(MediaItem.fromUri(activeUrl))
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true
                }
            }

            LaunchedEffect(exoPlayer, isMuted) {
                exoPlayer.volume = if (isMuted) 0f else 1f
            }

            LaunchedEffect(exoPlayer, isPlaying) {
                while (true) {
                    positionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                    if (exoPlayer.duration > 0L) durationMs = exoPlayer.duration
                    delay(350L)
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (exoPlayer != null) {
                    AndroidView(
                        factory = { ctx ->
                            TextureView(ctx).apply {
                                isOpaque = false
                                exoPlayer.setVideoTextureView(this)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                    )
                }

                // Top Bar
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title.ifBlank { if (isEn) "Channel Video" else "Video Info Penting" },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close" else "Tutup",
                            tint = Color.White
                        )
                    }
                }

                // Bottom Playback Bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.68f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    val maxDur = durationMs.coerceAtLeast(1L).toFloat()
                    Slider(
                        value = positionMs.toFloat().coerceIn(0f, maxDur),
                        onValueChange = { pos ->
                            positionMs = pos.toLong()
                            exoPlayer?.seekTo(pos.toLong())
                        },
                        valueRange = 0f..maxDur,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF2DD4BF),
                            activeTrackColor = Color(0xFF2DD4BF),
                            inactiveTrackColor = Color.White.copy(alpha = 0.35f)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (exoPlayer != null) {
                                        if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                    } else {
                                        isPlaying = !isPlaying
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = "${formatVideoDurationMs(positionMs)} / ${formatVideoDurationMs(durationMs)}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = { isMuted = !isMuted }) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Mute toggle",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullscreenImagePreviewDialog(
    primaryUrl: String,
    fallbackUrl: String,
    title: String,
    isEn: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val gifImageLoader = rememberBannerGifImageLoader(context)
    var useFallback by remember(primaryUrl, fallbackUrl) { mutableStateOf(false) }
    val activeUrl = if (useFallback && fallbackUrl.isNotBlank()) fallbackUrl else primaryUrl.ifBlank { fallbackUrl }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(activeUrl)
                        .crossfade(true)
                        .build(),
                    imageLoader = gifImageLoader,
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    onError = {
                        if (!useFallback && fallbackUrl.isNotBlank() && activeUrl != fallbackUrl) {
                            useFallback = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title.ifBlank { if (isEn) "Photo Attachment" else "Lampiran Foto" },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close" else "Tutup",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoDocumentAttachmentCard(
    fileName: String,
    fileSizeBytes: Long,
    primaryUrl: String,
    fallbackUrl: String,
    isEn: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resolvedUrl = primaryUrl.ifBlank { fallbackUrl }
    val ext = fileName.substringAfterLast('.', "DOC").uppercase().take(5)
    val formattedSize = InfoPentingHelper.formatFileSize(fileSizeBytes)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF0FDFA),
        border = BorderStroke(1.dp, Color(0xFF99F6E4))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F766E),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName.ifBlank { if (isEn) "Attached Document" else "Dokumen Lampiran" },
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(
                        ext.takeIf { it.isNotBlank() },
                        formattedSize.takeIf { it.isNotBlank() }
                    ).joinToString(" • ").ifBlank { if (isEn) "Document" else "Dokumen" },
                    fontSize = 10.5.sp,
                    color = Color(0xFF0F766E),
                    fontWeight = FontWeight.Medium
                )
            }
            if (resolvedUrl.isNotBlank()) {
                Button(
                    onClick = { openInfoDocumentOrLink(context, resolvedUrl, isEn) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F766E),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.heightIn(min = 40.dp)
                ) {
                    Text(
                        text = if (isEn) "Open" else "Buka",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoLinkPreviewCard(
    url: String,
    isEn: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val normalizedUrl = InfoPentingHelper.normalizeExternalLink(url)
    if (normalizedUrl.isBlank()) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { openInfoDocumentOrLink(context, normalizedUrl, isEn) },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEFF6FF),
        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF1D4ED8),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEn) "Attached Link" else "Tautan Lampiran",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E40AF)
                )
                Text(
                    text = normalizedUrl,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D4ED8),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = if (isEn) "Open Link" else "Buka Tautan",
                tint = Color(0xFF1D4ED8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
