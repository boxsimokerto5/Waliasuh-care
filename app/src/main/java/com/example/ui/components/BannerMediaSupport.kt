package com.example.ui.components

import android.content.Context
import android.graphics.Matrix
import android.os.Build
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

@Composable
fun rememberBannerGifImageLoader(context: Context): ImageLoader {
    val appContext = context.applicationContext ?: context
    return remember(appContext) {
        ImageLoader.Builder(appContext)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .crossfade(true)
            .build()
    }
}

private fun applyCenterCropTransform(
    textureView: TextureView,
    videoWidth: Int,
    videoHeight: Int
) {
    val viewWidth = textureView.width.toFloat()
    val viewHeight = textureView.height.toFloat()
    if (viewWidth <= 0f || viewHeight <= 0f || videoWidth <= 0 || videoHeight <= 0) return

    val videoAspect = videoWidth.toFloat() / videoHeight.toFloat()
    val viewAspect = viewWidth / viewHeight

    var scaleX = 1f
    var scaleY = 1f
    if (videoAspect > viewAspect) {
        scaleX = videoAspect / viewAspect
    } else {
        scaleY = viewAspect / videoAspect
    }

    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
    textureView.setTransform(matrix)
}

@OptIn(UnstableApi::class)
@Composable
fun BannerVideoSurface(
    primaryUrl: String,
    fallbackUrl: String,
    isActive: Boolean,
    isMuted: Boolean,
    modifier: Modifier = Modifier,
    onAllFailed: () -> Unit = {}
) {
    val isRobolectric = remember {
        Build.FINGERPRINT.equals("robolectric", ignoreCase = true)
    }
    if (isRobolectric) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F766E), Color(0xFF0284C7), Color(0xFF1E3A8A))
                    )
                )
        )
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var activeUrl by remember(primaryUrl, fallbackUrl) {
        mutableStateOf(primaryUrl.ifBlank { fallbackUrl })
    }
    var triedFallback by remember(primaryUrl, fallbackUrl) { mutableStateOf(false) }
    var isLifecycleResumed by remember { mutableStateOf(true) }
    var hasRenderedFrame by remember(primaryUrl, fallbackUrl) { mutableStateOf(false) }
    var lastVideoSize by remember { mutableStateOf(VideoSize.UNKNOWN) }

    val exoPlayer = remember(context) {
        runCatching {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
            }
        }.getOrNull()
    }

    if (exoPlayer == null) {
        LaunchedEffect(Unit) { onAllFailed() }
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F766E), Color(0xFF0284C7), Color(0xFF1D4ED8))
                    )
                )
        )
        return
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isLifecycleResumed = true
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> isLifecycleResumed = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                lastVideoSize = videoSize
            }

            override fun onRenderedFirstFrame() {
                hasRenderedFrame = true
            }

            override fun onPlayerError(error: PlaybackException) {
                if (!triedFallback && fallbackUrl.isNotBlank() && activeUrl != fallbackUrl) {
                    triedFallback = true
                    hasRenderedFrame = false
                    activeUrl = fallbackUrl
                } else {
                    onAllFailed()
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
            hasRenderedFrame = false
            exoPlayer.setMediaItem(MediaItem.fromUri(activeUrl))
            exoPlayer.prepare()
        }
    }

    LaunchedEffect(exoPlayer, isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    LaunchedEffect(exoPlayer, isActive, isLifecycleResumed, activeUrl) {
        val shouldPlay = isActive && isLifecycleResumed && activeUrl.isNotBlank()
        exoPlayer.playWhenReady = shouldPlay
        if (!shouldPlay && exoPlayer.isPlaying) {
            exoPlayer.pause()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    isOpaque = false
                    addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                        applyCenterCropTransform(
                            this,
                            lastVideoSize.width,
                            lastVideoSize.height
                        )
                    }
                    exoPlayer.setVideoTextureView(this)
                }
            },
            update = { textureView ->
                applyCenterCropTransform(
                    textureView,
                    lastVideoSize.width,
                    lastVideoSize.height
                )
            },
            modifier = Modifier.fillMaxSize()
        )
        if (!hasRenderedFrame) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0F766E), Color(0xFF0284C7), Color(0xFF1D4ED8))
                        )
                    )
            )
        }
    }
}
