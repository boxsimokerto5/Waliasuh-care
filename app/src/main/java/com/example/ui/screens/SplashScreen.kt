package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.WaliPrimary

/**
 * Splash Screen Beranimasi Kartun Panda Dokter Berlari.
 * Durasi: 9 Detik dengan indikator progress visual dan tombol "Lewati" jika diperlukan cepat.
 */
@Composable
fun SplashScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Progress Animatable dari 0f ke 1f selama 9.000 ms (9 detik)
    val progressAnimatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progressAnimatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 9000, easing = LinearEasing)
        )
        onFinish()
    }

    val currentProgress = progressAnimatable.value

    // Infinite Transition untuk Animasi Panda Berlari (Bobbing & Tilt)
    val infiniteTransition = rememberInfiniteTransition(label = "panda_running_animation")

    // Gerakan memantul naik-turun (bobbing) 0 dp ke -14 dp
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Kemiringan badan panda saat lari bergoyang (-3 derajat ke +3 derajat)
    val tiltAngle by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tilt"
    )

    // Efek garis lari di tanah (speed lines moving)
    val groundTrackOffset by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ground_track"
    )

    // Efek denyut nadi / pulse glow di belakang panda
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    // Pesan status rotasi berdasarkan progres 9 detik (berganti tiap 3 detik)
    val statusMessage = when {
        currentProgress < 0.35f -> "🐼 Dokter Panda sedang menyiapkan data kesehatan anak asuh..."
        currentProgress < 0.70f -> "🏥 Menghubungkan faskes Puskesmas Wates & Puskesmas Plosoklaten..."
        else -> "✨ Siaga melayani dan memantau kesehatan siswa SRT 1 Kediri!"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0F2FE), // Soft Sky Medical Blue
                            Color(0xFFF0FDF4), // Mint Medical Green
                            Color(0xFFFFFFFF)  // Clean Crisp White
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Tombol Lewati / Skip di pojok kanan atas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    onClick = onFinish,
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    shadowElevation = 2.dp,
                    modifier = Modifier.testTag("btn_skip_splash")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Lewati",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WaliPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Lewati Splash",
                            tint = WaliPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Elemen Konten Tengah
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 1. Karakter Panda Dokter Berlari
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Lingkaran Cahaya Denyut di Belakang Panda
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .scale(pulseGlow)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        WaliPrimary.copy(alpha = 0.25f),
                                        Color(0xFF0288D1).copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Gambar Panda Dokter dengan animasi memantul & bergoyang (running motion)
                    Box(
                        modifier = Modifier
                            .offset(y = bounceOffset.dp)
                            .rotate(tiltAngle),
                        contentAlignment = Alignment.Center
                    ) {
                        ElevatedCard(
                            shape = CircleShape,
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                            modifier = Modifier
                                .size(185.dp)
                                .border(3.dp, Color.White, CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_sr_care),
                                contentDescription = "Logo Resmi Wali Asuh Care",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Badge Medis Kecil di Sudut Atas Panda
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Garis Lintasan Lari Dinamis (Running Dust & Track Dashes) di Bawah Kaki Panda
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = (-8).dp)
                            .fillMaxWidth(0.7f),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.offset(x = groundTrackOffset.dp)
                        ) {
                            Box(modifier = Modifier.size(24.dp, 3.dp).clip(RoundedCornerShape(2.dp)).background(WaliPrimary.copy(alpha = 0.45f)))
                            Box(modifier = Modifier.size(38.dp, 3.5.dp).clip(RoundedCornerShape(2.dp)).background(WaliPrimary.copy(alpha = 0.65f)))
                            Box(modifier = Modifier.size(16.dp, 3.dp).clip(RoundedCornerShape(2.dp)).background(WaliPrimary.copy(alpha = 0.35f)))
                            Box(modifier = Modifier.size(30.dp, 3.5.dp).clip(RoundedCornerShape(2.dp)).background(WaliPrimary.copy(alpha = 0.55f)))
                            Box(modifier = Modifier.size(20.dp, 3.dp).clip(RoundedCornerShape(2.dp)).background(WaliPrimary.copy(alpha = 0.4f)))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Judul Aplikasi & Identitas Sekolah Rakyat
                Text(
                    text = "Wali Asuh Care",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Sekolah Rakyat SRT 1 Kabupaten Kediri",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WaliPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.75f))
                        .border(1.dp, WaliPrimary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Layanan Pemantauan Kesehatan & Rujukan Faskes",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 3. Progress Bar 9 Detik Visual
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { currentProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shadow(1.dp, RoundedCornerShape(4.dp)),
                        color = WaliPrimary,
                        trackColor = WaliPrimary.copy(alpha = 0.18f),
                        strokeCap = StrokeCap.Round
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Memuat sistem... ${(currentProgress * 100).toInt()}%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )

                        val remainingSeconds = (9 - (currentProgress * 9).toInt()).coerceAtLeast(1)
                        Text(
                            text = "${remainingSeconds}s",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = WaliPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Pesan Status Interaktif dengan Animasi Fade yang Berubah Halus
                AnimatedContent(
                    targetState = statusMessage,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                    },
                    label = "status_message_animation"
                ) { msg ->
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
