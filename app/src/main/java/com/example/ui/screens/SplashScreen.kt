package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.R
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class SplashPillarNode(
    val icon: ImageVector,
    val accent: Color,
    val angleDeg: Float,
    val labelId: String,
    val labelEn: String
)

private data class SplashAtmosphere(
    val bgColors: List<Color>,
    val orbPrimary: Color,
    val orbSecondary: Color,
    val greetingId: String,
    val greetingEn: String,
    val isNight: Boolean
)

/**
 * SuperApp-style Choreographed Splash Screen for Wali Asuh Care.
 * Features:
 * - Full-bleed time-aware atmospheric ocean-emerald gradient & undulating horizon waves
 * - 4 converging pillar nodes (Kesehatan, Ibadah, Asrama, Orang Tua) merging into the official crest
 * - Spring logo reveal with guardian ripple rings & diagonal shimmer sweep
 * - Clean 5-second sequence with subtle breathing dots & second shimmer sweep (no heavy progress bar/countdown)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SplashScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    // Ensure crisp light system icons over the rich deep splash canvas, then restore
    if (!view.isInEditMode) {
        DisposableEffect(view) {
            val window = (view.context as? Activity)?.window
            val controller = window?.let { WindowCompat.getInsetsController(it, view) }
            val prevLightStatus = controller?.isAppearanceLightStatusBars
            val prevLightNav = controller?.isAppearanceLightNavigationBars
            controller?.isAppearanceLightStatusBars = false
            controller?.isAppearanceLightNavigationBars = false
            onDispose {
                if (prevLightStatus != null) controller.isAppearanceLightStatusBars = prevLightStatus
                if (prevLightNav != null) controller.isAppearanceLightNavigationBars = prevLightNav
            }
        }
    }

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val atmosphere = remember(hour) {
        when (hour) {
            in 4..10 -> SplashAtmosphere(
                bgColors = listOf(Color(0xFF06396B), Color(0xFF0A667E), Color(0xFF0E9384)),
                orbPrimary = Color(0xFFFDE68A).copy(alpha = 0.22f),
                orbSecondary = Color(0xFF5EEAD4).copy(alpha = 0.18f),
                greetingId = "Selamat Pagi",
                greetingEn = "Good Morning",
                isNight = false
            )
            in 11..14 -> SplashAtmosphere(
                bgColors = listOf(Color(0xFF073E73), Color(0xFF085C8E), Color(0xFF0E8898)),
                orbPrimary = Color(0xFF7DD3FC).copy(alpha = 0.24f),
                orbSecondary = Color(0xFF34D399).copy(alpha = 0.18f),
                greetingId = "Selamat Siang",
                greetingEn = "Good Afternoon",
                isNight = false
            )
            in 15..17 -> SplashAtmosphere(
                bgColors = listOf(Color(0xFF073B6D), Color(0xFF0B627D), Color(0xFF0F766E)),
                orbPrimary = Color(0xFFFDBA74).copy(alpha = 0.22f),
                orbSecondary = Color(0xFF38BDF8).copy(alpha = 0.18f),
                greetingId = "Selamat Sore",
                greetingEn = "Good Evening",
                isNight = false
            )
            else -> SplashAtmosphere(
                bgColors = listOf(Color(0xFF041C3C), Color(0xFF084276), Color(0xFF0A5F6A)),
                orbPrimary = Color(0xFF38BDF8).copy(alpha = 0.20f),
                orbSecondary = Color(0xFF2DD4BF).copy(alpha = 0.16f),
                greetingId = "Selamat Malam",
                greetingEn = "Good Night",
                isNight = true
            )
        }
    }

    val pillars = remember {
        listOf(
            SplashPillarNode(Icons.Default.Favorite, Color(0xFFFDA4AF), -135f, "Kesehatan", "Health"),
            SplashPillarNode(Icons.Default.AutoAwesome, Color(0xFFFCD34D), -45f, "Ibadah", "Worship"),
            SplashPillarNode(Icons.Default.School, Color(0xFF6EE7B7), 135f, "Asrama", "Dormitory"),
            SplashPillarNode(Icons.Default.FamilyRestroom, Color(0xFF7DD3FC), 45f, "Orang Tua", "Parents")
        )
    }

    // Core choreographed animatables
    val convergenceProgress = remember { Animatable(0f) } // 0 = outer orbit, 1 = merged center
    val logoScale = remember { Animatable(0.35f) }
    val logoAlpha = remember { Animatable(0f) }
    val burstProgress = remember { Animatable(0f) }
    val shimmerSweep = remember { Animatable(-0.6f) }
    val exitAlpha = remember { Animatable(1f) }
    val exitScale = remember { Animatable(1f) }

    var showBrandCopy by remember { mutableStateOf(false) }
    var showPillarsStrip by remember { mutableStateOf(false) }
    var isFinishing by remember { mutableStateOf(false) }

    fun finishGracefully() {
        if (isFinishing) return
        isFinishing = true
        scope.launch {
            launch { exitScale.animateTo(1.05f, tween(240, easing = FastOutSlowInEasing)) }
            exitAlpha.animateTo(0f, tween(240, easing = FastOutSlowInEasing))
            onFinish()
        }
    }

    LaunchedEffect(Unit) {
        // Phase 1: 4 Pillars glide inward toward the center crest
        launch {
            convergenceProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
            )
        }

        // Phase 2: Crest springs into view as pillars meet (t = 480ms)
        delay(480)
        launch {
            logoAlpha.animateTo(1f, tween(durationMillis = 320))
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            burstProgress.animateTo(1f, tween(durationMillis = 950, easing = FastOutSlowInEasing))
        }

        // Phase 3: Brand title + greeting + diagonal shimmer (t = 980ms)
        delay(500)
        showBrandCopy = true
        launch {
            shimmerSweep.animateTo(1.8f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
        }

        // Phase 4: 4-Pillar ecosystem chips appear (t = 1320ms)
        delay(340)
        showPillarsStrip = true

        // Phase 5: Second gentle light shimmer sweep during atmospheric hold (t = 3100ms)
        delay(1780)
        launch {
            shimmerSweep.snapTo(-0.6f)
            shimmerSweep.animateTo(1.8f, tween(durationMillis = 950, easing = FastOutSlowInEasing))
        }

        // Finish at ~5.0 seconds total (4760ms + 240ms graceful exit = 5000ms)
        delay(1660)
        finishGracefully()
    }

    // Continuous subtle ambient loops (wave phase, guardian pulse, halo rotation, breathing dots)
    val infiniteTransition = rememberInfiniteTransition(label = "splash_ambient")
    val ambientPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_phase"
    )
    val haloRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_rotation"
    )
    val rippleCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_cycle"
    )
    val sparklePulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_pulse"
    )

    val clickInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(exitAlpha.value)
            .scale(exitScale.value)
            .background(Brush.verticalGradient(colors = atmosphere.bgColors))
            .clickable(
                interactionSource = clickInteraction,
                indication = null,
                onClick = { finishGracefully() }
            )
    ) {
        // 1. Full-Bleed Atmospheric Canvas (Ambient Orbs, Sparkles, Horizon Waves & Guardian Ripples)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerX = w * 0.5f
            val centerY = h * 0.44f

            // Floating ambient glow orbs
            val orb1X = w * 0.22f + cos(ambientPhase) * w * 0.05f
            val orb1Y = h * 0.20f + sin(ambientPhase) * h * 0.03f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(atmosphere.orbPrimary, Color.Transparent),
                    center = Offset(orb1X, orb1Y),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(orb1X, orb1Y)
            )

            val orb2X = w * 0.80f - cos(ambientPhase) * w * 0.05f
            val orb2Y = h * 0.68f - sin(ambientPhase) * h * 0.03f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(atmosphere.orbSecondary, Color.Transparent),
                    center = Offset(orb2X, orb2Y),
                    radius = w * 0.60f
                ),
                radius = w * 0.60f,
                center = Offset(orb2X, orb2Y)
            )

            // Delicate twinkling starlight / morning light motes
            val stars = listOf(
                Triple(0.16f, 0.14f, 3.2f),
                Triple(0.84f, 0.17f, 2.8f),
                Triple(0.12f, 0.36f, 2.2f),
                Triple(0.89f, 0.33f, 3.0f),
                Triple(0.24f, 0.62f, 2.4f),
                Triple(0.76f, 0.60f, 2.6f),
                Triple(0.52f, 0.11f, 2.5f)
            )
            stars.forEachIndexed { idx, (fx, fy, baseR) ->
                val factor = if (idx % 2 == 0) sparklePulse else (1.35f - sparklePulse).coerceIn(0.25f, 1f)
                val alphaFactor = if (atmosphere.isNight) 0.65f else 0.38f
                drawCircle(
                    color = Color.White.copy(alpha = alphaFactor * factor),
                    radius = baseR * density,
                    center = Offset(w * fx, h * fy)
                )
            }

            // Undulating Horizon Waves at the bottom
            val wave1Path = Path().apply {
                val baseY = h * 0.84f
                moveTo(0f, h)
                lineTo(0f, baseY)
                cubicTo(
                    w * 0.28f, baseY - sin(ambientPhase) * 24f * density,
                    w * 0.68f, baseY + cos(ambientPhase) * 28f * density,
                    w, baseY - 12f * density
                )
                lineTo(w, h)
                close()
            }
            drawPath(wave1Path, color = Color.White.copy(alpha = 0.055f))

            val wave2Path = Path().apply {
                val baseY = h * 0.89f
                moveTo(0f, h)
                lineTo(0f, baseY)
                cubicTo(
                    w * 0.35f, baseY + cos(ambientPhase * 1.1f) * 22f * density,
                    w * 0.72f, baseY - sin(ambientPhase * 1.1f) * 20f * density,
                    w, baseY + 6f * density
                )
                lineTo(w, h)
                close()
            }
            drawPath(wave2Path, color = Color.White.copy(alpha = 0.085f))

            // Initial convergence burst ring
            val burst = burstProgress.value
            if (burst in 0.01f..0.99f) {
                val burstR = (68f + burst * 115f) * density
                drawCircle(
                    color = Color(0xFF99F6E4).copy(alpha = (1f - burst) * 0.55f),
                    radius = burstR,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.5f * density)
                )
            }

            // Staggered Guardian Ripple Rings behind central crest
            val logoPresence = logoAlpha.value
            if (logoPresence > 0.1f) {
                for (i in 0..1) {
                    val phase = (rippleCycle + i * 0.5f) % 1f
                    val r = (74f + phase * 82f) * density
                    val a = ((1f - phase) * 0.32f * logoPresence).coerceIn(0f, 1f)
                    drawCircle(
                        color = Color.White.copy(alpha = a),
                        radius = r,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1.6f * density)
                    )
                }

                // Subtle rotating orbital arc halo
                rotate(degrees = haloRotation, pivot = Offset(centerX, centerY)) {
                    val arcSize = 186f * density
                    val topLeft = Offset(centerX - arcSize / 2f, centerY - arcSize / 2f)
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.48f * logoPresence),
                                Color(0xFF5EEAD4).copy(alpha = 0.35f * logoPresence),
                                Color.Transparent
                            ),
                            center = Offset(centerX, centerY)
                        ),
                        startAngle = 0f,
                        sweepAngle = 210f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                        style = Stroke(width = 2f * density, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // 2. Top-Right "Lewati" Pill inside Safe Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                onClick = { finishGracefully() },
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.14f),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(24.dp))
                    .testTag("btn_skip_splash")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = if (isEn) "Skip" else "Lewati",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = if (isEn) "Skip Splash" else "Lewati Splash",
                        tint = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // 3. Center Hero Stage (Converging Pillars + Official Crest + Brand Copy)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.95f))

            // Hero Crest + Converging Orbit Stage
            Box(
                modifier = Modifier.size(260.dp),
                contentAlignment = Alignment.Center
            ) {
                // Converging 4 Pillar Nodes (visible during Phase 1 -> merge into emblem)
                val conv = convergenceProgress.value
                val nodeAlpha = (1f - ((conv - 0.55f) / 0.45f).coerceIn(0f, 1f))
                if (nodeAlpha > 0.01f) {
                    val orbitRadiusDp = 112f * (1f - conv)
                    val swirlDeg = conv * 42f
                    pillars.forEach { pillar ->
                        val rad = ((pillar.angleDeg + swirlDeg) * PI / 180.0)
                        val offsetX = (cos(rad) * orbitRadiusDp).dp
                        val offsetY = (sin(rad) * orbitRadiusDp).dp
                        val nodeScale = 1f - (conv * 0.38f)

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.16f),
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        offsetX.toPx().roundToInt(),
                                        offsetY.toPx().roundToInt()
                                    )
                                }
                                .scale(nodeScale)
                                .alpha(nodeAlpha)
                                .size(50.dp)
                                .border(1.2.dp, pillar.accent.copy(alpha = 0.75f), CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = pillar.icon,
                                    contentDescription = if (isEn) pillar.labelEn else pillar.labelId,
                                    tint = pillar.accent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Soft Luminous Halo Backing behind Crest
                Box(
                    modifier = Modifier
                        .size(176.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF5EEAD4).copy(alpha = 0.34f),
                                    Color(0xFF38BDF8).copy(alpha = 0.14f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Official Wali Asuh Care Crest with Frosted Ring & Shimmer Light Sweep
                val sweep = shimmerSweep.value
                Box(
                    modifier = Modifier
                        .size(144.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .clip(CircleShape)
                        .background(Color(0xFF084C82))
                        .border(2.5.dp, Color.White.copy(alpha = 0.88f), CircleShape)
                        .drawWithContent {
                            drawContent()
                            if (sweep > -0.55f && sweep < 1.75f) {
                                val startX = size.width * sweep
                                drawRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.32f),
                                            Color.Transparent
                                        ),
                                        start = Offset(startX - size.width * 0.38f, 0f),
                                        end = Offset(startX + size.width * 0.18f, size.height)
                                    )
                                )
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_sr_care),
                        contentDescription = if (isEn) "Official Wali Asuh Care Logo" else "Logo Resmi Wali Asuh Care",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Staggered Brand Typography
            AnimatedVisibility(
                visible = showBrandCopy,
                enter = fadeIn(tween(420)) + slideInVertically(
                    animationSpec = tween(420, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 3 }
                )
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 460.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Wali Asuh Care",
                        fontSize = 29.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.4.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (isEn) {
                            "Integrated Health, Worship & Dormitory Care"
                        } else {
                            "Pendampingan Kesehatan, Ibadah & Asrama Terpadu"
                        },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.90f),
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )

                    // Time-of-Day Greeting Pill
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.13f),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (atmosphere.isNight) Icons.Default.NightsStay else Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = if (atmosphere.isNight) Color(0xFFBAE6FD) else Color(0xFFFDE68A),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (isEn) {
                                    "${atmosphere.greetingEn} • Sekolah Rakyat"
                                } else {
                                    "${atmosphere.greetingId} • Sekolah Rakyat"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4-Pillar Ecosystem Chips
            AnimatedVisibility(
                visible = showPillarsStrip,
                enter = fadeIn(tween(420)) + slideInVertically(
                    animationSpec = tween(420, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 2 }
                )
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pillars.forEach { pillar ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.10f),
                            modifier = Modifier.border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(20.dp)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = pillar.icon,
                                    contentDescription = null,
                                    tint = pillar.accent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isEn) pillar.labelEn else pillar.labelId,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.94f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.05f))

            // Subtle SuperApp Breathing Dots & Footer (replaces heavy progress bar)
            Column(
                modifier = Modifier.padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (dotIdx in 0..2) {
                        val phaseShift = ((ambientPhase * 1.6f) - dotIdx * 0.7f)
                        val pulse = ((sin(phaseShift) + 1f) * 0.5f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .size((6f + pulse * 2.5f).dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.35f + pulse * 0.55f))
                        )
                    }
                }
                Text(
                    text = if (isEn) "Tap anywhere to continue" else "Ketuk layar untuk lanjut",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.62f)
                )
            }
        }
    }
}
