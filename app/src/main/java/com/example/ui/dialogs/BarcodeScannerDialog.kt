package com.example.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.ChildStudent
import com.example.ui.components.ConfigureEdgeToEdgeDialogWindow
import com.example.ui.components.rememberEdgeToEdgeDialogSafeInsets
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.delay

enum class ScannerFeedbackStatus {
    SUCCESS,
    WARNING,
    ERROR
}

data class ScannerFloatingFeedback(
    val id: Long,
    val status: ScannerFeedbackStatus,
    val title: String,
    val subtitle: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerDialog(
    femaleStudents: List<ChildStudent>,
    onScanned: (String) -> Unit,
    onDismiss: () -> Unit,
    dialogTitle: String? = null,
    dialogSubtitle: String? = null,
    quickPickTitle: String? = null,
    searchPlaceholder: String? = null,
    emptyListMessage: String? = null,
    accentColor: Color = Color(0xFFDB2777),
    accentBgColor: Color = Color(0xFFFCE7F3),
    cardBgColor: Color = Color(0xFFFDF2F8),
    avatarBgColor: Color = Color(0xFFFBCFE8),
    avatarTextColor: Color = Color(0xFF9D174D),
    titleTextColor: Color = Color(0xFF831843),
    continuousScan: Boolean = false,
    sessionParticipantCount: Int? = null,
    floatingFeedback: ScannerFloatingFeedback? = null
) {
    val context = LocalContext.current
    val appStrings = LocalAppStrings.current
    val isEn = appStrings.isEnglish
    val resolvedDialogTitle = dialogTitle ?: if (isEn) "Scan Student Card Barcode" else "Scan Barcode Kartu Siswa"
    val resolvedDialogSubtitle = dialogSubtitle ?: if (isEn) "Automatically reads Student Card NIK / NIS" else "Membaca NIK / NIS Kartu Siswa Otomatis"
    val resolvedSearchPlaceholder = searchPlaceholder ?: if (isEn) "Search name, class, room, or NIK..." else "Cari nama, kelas, kamar, atau NIK siswi..."
    val resolvedEmptyListMessage = emptyListMessage ?: if (isEn) "No female students registered yet." else "Belum ada data siswi putri terdaftar."

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val hapticFeedback = LocalHapticFeedback.current
    var manualNikInput by remember { mutableStateOf("") }
    var quickSearchQuery by remember { mutableStateOf("") }
    var isFlashOn by remember { mutableStateOf(false) }
    var hasFlashUnit by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var visibleFeedback by remember { mutableStateOf<ScannerFloatingFeedback?>(null) }
    var showManualPickerSheet by remember { mutableStateOf(false) }

    LaunchedEffect(floatingFeedback?.id) {
        if (floatingFeedback != null) {
            visibleFeedback = floatingFeedback
            delay(2500L)
            if (visibleFeedback?.id == floatingFeedback.id) {
                visibleFeedback = null
            }
        }
    }

    val filteredFemaleStudents = remember(femaleStudents, quickSearchQuery) {
        val q = quickSearchQuery.trim().lowercase()
        if (q.isEmpty()) {
            femaleStudents
        } else {
            femaleStudents.filter { s ->
                s.name.lowercase().contains(q) ||
                    s.classGrade.lowercase().contains(q) ||
                    s.dormitory.lowercase().contains(q) ||
                    s.roomNumber.lowercase().contains(q) ||
                    s.nik.lowercase().contains(q) ||
                    s.nis.lowercase().contains(q)
            }
        }
    }

    // Animasi garis laser vertikal halus dalam bingkai bidik portrait lebar
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.94f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserProgress"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        ConfigureEdgeToEdgeDialogWindow(
            lightStatusBars = false,
            lightNavigationBars = false
        )
        BackHandler {
            if (showManualPickerSheet) {
                showManualPickerSheet = false
            } else {
                onDismiss()
            }
        }

        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        val safeDrawingInsets = rememberEdgeToEdgeDialogSafeInsets()
        val topSafePx = safeDrawingInsets.getTop(density).toFloat()
        val bottomSafePx = safeDrawingInsets.getBottom(density).toFloat()
        val leftSafePx = safeDrawingInsets.getLeft(density, layoutDirection).toFloat()
        val rightSafePx = safeDrawingInsets.getRight(density, layoutDirection).toFloat()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090F1A))
                .testTag("scanner_fullscreen_root")
        ) {
            val containerMaxHeight = maxHeight
            val maxWidthPx = with(density) { maxWidth.toPx() }
            val usableWidthPx = (maxWidthPx - leftSafePx - rightSafePx).coerceAtLeast(180f)
            val reticleMinWidthPx = with(density) { 180.dp.toPx() }
            val reticleMaxWidthPx = with(density) { 360.dp.toPx() }
            val reticleWidthPx = (usableWidthPx * 0.80f).coerceIn(
                minOf(reticleMinWidthPx, usableWidthPx * 0.88f),
                reticleMaxWidthPx
            )

            // 1. LIVE FULL-SCREEN PORTRAIT CAMERA VIEWFINDER
            if (hasCameraPermission && cameraError == null) {
                CameraBarcodePreview(
                    isFlashOn = isFlashOn,
                    continuousScan = continuousScan,
                    onBarcodeDetected = { detectedCode ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onScanned(detectedCode)
                        if (!continuousScan) {
                            onDismiss()
                        }
                    },
                    onFlashAvailabilityChanged = { available ->
                        hasFlashUnit = available
                        if (!available) isFlashOn = false
                    },
                    onCameraError = { msg ->
                        cameraError = msg
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fallback Fullscreen View when camera unavailable or permission denied
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(safeDrawingInsets)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 28.dp, vertical = 96.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(76.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.72f),
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = cameraError
                            ?: if (hasCameraPermission) {
                                if (isEn) "Preparing wide portrait camera..." else "Menyiapkan kamera portrait lebar..."
                            } else {
                                if (isEn) "Camera Permission Needed for Auto Scan" else "Izin Kamera Dibutuhkan untuk Scan Otomatis"
                            },
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    if (!hasCameraPermission) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                cameraError = null
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEn) "Grant Camera Access" else "Izinkan Akses Kamera",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. WIDE PORTRAIT VIEWFINDER OVERLAY (Scrim + Transparent Cutout + Modern Corner Brackets + Laser)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            ) {
                val topBandPx = topSafePx + 74.dp.toPx()
                val bottomBandPx = size.height - bottomSafePx - 130.dp.toPx()
                val availableMidHeight = (bottomBandPx - topBandPx).coerceAtLeast(130.dp.toPx())
                val rectW = reticleWidthPx.coerceAtMost(usableWidthPx * 0.86f)
                val rectH = (availableMidHeight * 0.72f).coerceIn(
                    minOf(150.dp.toPx(), availableMidHeight * 0.90f),
                    320.dp.toPx()
                )
                val left = leftSafePx + (usableWidthPx - rectW) / 2f
                val top = topBandPx + (availableMidHeight - rectH) / 2f
                val cornerRadius = 20.dp.toPx()

                // Scrim gelap lembut di luar area bidik
                drawRect(color = Color.Black.copy(alpha = 0.46f))

                // Lubang jendela bidik transparan penuh
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(left, top),
                    size = Size(rectW, rectH),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    blendMode = BlendMode.Clear
                )

                // Garis tepi tipis halus di sekeliling kotak
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(left, top),
                    size = Size(rectW, rectH),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Siku sudut modern (Corner Brackets)
                val bracketLen = 30.dp.toPx()
                val strokeW = 4.dp.toPx()
                val bracketsPath = Path().apply {
                    // Kiri-Atas
                    moveTo(left, top + bracketLen)
                    lineTo(left, top + cornerRadius)
                    quadraticTo(left, top, left + cornerRadius, top)
                    lineTo(left + bracketLen, top)

                    // Kanan-Atas
                    moveTo(left + rectW - bracketLen, top)
                    lineTo(left + rectW - cornerRadius, top)
                    quadraticTo(left + rectW, top, left + rectW, top + cornerRadius)
                    lineTo(left + rectW, top + bracketLen)

                    // Kiri-Bawah
                    moveTo(left, top + rectH - bracketLen)
                    lineTo(left, top + rectH - cornerRadius)
                    quadraticTo(left, top + rectH, left + cornerRadius, top + rectH)
                    lineTo(left + bracketLen, top + rectH)

                    // Kanan-Bawah
                    moveTo(left + rectW - bracketLen, top + rectH)
                    lineTo(left + rectW - cornerRadius, top + rectH)
                    quadraticTo(left + rectW, top + rectH, left + rectW, top + rectH - cornerRadius)
                    lineTo(left + rectW, top + rectH - bracketLen)
                }
                drawPath(
                    path = bracketsPath,
                    color = accentColor,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )

                // Garis laser scanning di dalam area jendela bidik
                val laserY = top + (rectH * laserProgress)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFFF1493),
                            Color.White,
                            Color(0xFFFF1493),
                            Color.Transparent
                        ),
                        startX = left + 12.dp.toPx(),
                        endX = left + rectW - 12.dp.toPx()
                    ),
                    start = Offset(left + 12.dp.toPx(), laserY),
                    end = Offset(left + rectW - 12.dp.toPx(), laserY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 3. FLOATING TOP BAR OVER LIVE CAMERA
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.78f),
                                Color.Black.copy(alpha = 0.36f),
                                Color.Transparent
                            )
                        )
                    )
                    .windowInsetsPadding(
                        safeDrawingInsets.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp)
                        .align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accentBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = resolvedDialogTitle,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = resolvedDialogSubtitle,
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (continuousScan && sessionParticipantCount != null) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = accentBgColor,
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = if (isEn) "$sessionParticipantCount Students" else "$sessionParticipantCount Anak",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    if (hasCameraPermission && cameraError == null && hasFlashUnit) {
                        IconButton(
                            onClick = { isFlashOn = !isFlashOn },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_toggle_scanner_flash")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.50f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = if (isFlashOn) {
                                        if (isEn) "Turn Off Flash" else "Matikan Lampu Kilat"
                                    } else {
                                        if (isEn) "Turn On Flash" else "Nyalakan Lampu Kilat"
                                    },
                                    tint = if (isFlashOn) Color(0xFFFACC15) else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_scanner")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.50f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = appStrings.nav.close,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 4. FLOATING CONTINUOUS SCAN FEEDBACK POPUP (Muncul otomatis di atas bingkai)
            AnimatedVisibility(
                visible = visibleFeedback != null,
                enter = fadeIn(animationSpec = tween(220)) + slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = tween(250)
                ),
                exit = fadeOut(animationSpec = tween(260)) + slideOutVertically(
                    targetOffsetY = { -it / 2 },
                    animationSpec = tween(260)
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(
                        safeDrawingInsets.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                        )
                    )
                    .padding(top = 74.dp, start = 20.dp, end = 20.dp)
                    .widthIn(max = 520.dp)
            ) {
                visibleFeedback?.let { fb ->
                    val bgColor = when (fb.status) {
                        ScannerFeedbackStatus.SUCCESS -> Color(0xFF15803D)
                        ScannerFeedbackStatus.WARNING -> Color(0xFFD97706)
                        ScannerFeedbackStatus.ERROR -> Color(0xFFDC2626)
                    }
                    val borderColor = when (fb.status) {
                        ScannerFeedbackStatus.SUCCESS -> Color(0xFF86EFAC)
                        ScannerFeedbackStatus.WARNING -> Color(0xFFFDE68A)
                        ScannerFeedbackStatus.ERROR -> Color(0xFFFCA5A5)
                    }
                    val icon = when (fb.status) {
                        ScannerFeedbackStatus.SUCCESS -> Icons.Default.CheckCircle
                        ScannerFeedbackStatus.WARNING -> Icons.Default.Info
                        ScannerFeedbackStatus.ERROR -> Icons.Default.ErrorOutline
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = bgColor,
                        shadowElevation = 10.dp,
                        border = BorderStroke(1.5.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("floating_scan_feedback_popup")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fb.title,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (fb.subtitle.isNotBlank()) {
                                    Text(
                                        text = fb.subtitle,
                                        fontSize = 11.5.sp,
                                        color = Color.White.copy(alpha = 0.92f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. FLOATING BOTTOM DOCK: Panduan + Tombol Pilih / Input Manual + Tombol Selesai (Continuous Scan)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.72f),
                                Color.Black.copy(alpha = 0.90f)
                            )
                        )
                    )
                    .windowInsetsPadding(
                        safeDrawingInsets.only(
                            WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.Black.copy(alpha = 0.56f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
                    ) {
                        Text(
                            text = if (isEn) {
                                "Point Student Card Barcode / QR into the Box"
                            } else {
                                "Arahkan Barcode / QR Kartu Siswa ke Kotak"
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showManualPickerSheet = true },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White.copy(alpha = 0.14f),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_open_manual_student_picker")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEn) {
                                    "Manual / Pick (${femaleStudents.size})"
                                } else {
                                    "Input Manual / Pilih (${femaleStudents.size})"
                                },
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (continuousScan) {
                            Button(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_done_continuous_scan")
                            ) {
                                Text(
                                    text = if (isEn) {
                                        "Done${sessionParticipantCount?.let { " ($it)" } ?: ""}"
                                    } else {
                                        "Selesai${sessionParticipantCount?.let { " ($it)" } ?: ""}"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 6. BOTTOM SHEET INPUT MANUAL & PILIH CEPAT SISWA (Muncul hanya saat dibutuhkan)
            if (showManualPickerSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showManualPickerSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    modifier = Modifier.windowInsetsPadding(
                        safeDrawingInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .align(Alignment.CenterHorizontally)
                            .windowInsetsPadding(
                                safeDrawingInsets
                                    .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                                    .union(WindowInsets.ime.only(WindowInsetsSides.Bottom))
                            )
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) "Manual Input & Student List" else "Input Manual & Pilih Cepat Siswa",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = resolvedDialogTitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { showManualPickerSheet = false },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_close_manual_picker_sheet")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = appStrings.nav.close
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Manual NIK / NIS Input Bar
                        Text(
                            text = if (isEn) "Enter Student NIK / NIS Manually:" else "Input NIK / NIS Siswa Manual:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = manualNikInput,
                                onValueChange = { manualNikInput = it },
                                placeholder = {
                                    Text(
                                        if (isEn) "Example: 3506123456780001" else "Contoh: 3506123456780001",
                                        fontSize = 12.sp
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_manual_nik_haid")
                            )

                            Button(
                                onClick = {
                                    if (manualNikInput.isNotBlank()) {
                                        val code = manualNikInput.trim()
                                        onScanned(code)
                                        if (continuousScan) {
                                            manualNikInput = ""
                                        } else {
                                            showManualPickerSheet = false
                                            onDismiss()
                                        }
                                    }
                                },
                                enabled = manualNikInput.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_confirm_manual_nik")
                            ) {
                                Text(
                                    if (continuousScan) "Input" else if (isEn) "Select" else "Pilih",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = quickPickTitle ?: if (isEn) {
                                "Quick Pick Asrama Putri Card (${filteredFemaleStudents.size} Students):"
                            } else {
                                "Pilih Cepat Kartu Siswi Asrama Putri (${filteredFemaleStudents.size} Siswi):"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (femaleStudents.isNotEmpty()) {
                            OutlinedTextField(
                                value = quickSearchQuery,
                                onValueChange = { quickSearchQuery = it },
                                placeholder = { Text(resolvedSearchPlaceholder, fontSize = 11.5.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = if (isEn) "Search Student" else "Cari Siswa",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_quick_pick_haid_search")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (filteredFemaleStudents.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (femaleStudents.isEmpty()) {
                                        resolvedEmptyListMessage
                                    } else {
                                        if (isEn) {
                                            "No students match search \"$quickSearchQuery\"."
                                        } else {
                                            "Tidak ada siswa yang cocok dengan pencarian \"$quickSearchQuery\"."
                                        }
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(
                                        min = 120.dp,
                                        max = (containerMaxHeight * 0.36f).coerceIn(140.dp, 320.dp)
                                    ),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(filteredFemaleStudents, key = { it.id }) { student ->
                                    Card(
                                        onClick = {
                                            val cleanNik = com.example.util.StudentCsvParser.cleanDigitsOnly(student.nik)
                                            val codeToUse = cleanNik.ifBlank { student.nis.ifBlank { student.id.toString() } }
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onScanned(codeToUse)
                                            if (!continuousScan) {
                                                showManualPickerSheet = false
                                                onDismiss()
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(avatarBgColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = student.name.take(1).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = avatarTextColor
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = student.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = titleTextColor
                                                )
                                                val displayNik = com.example.util.StudentCsvParser.cleanDigitsOnly(student.nik)
                                                Text(
                                                    text = "${student.classGrade} • ${student.dormitory} • NIK: ${displayNik.ifBlank { student.nis.ifBlank { student.id.toString() } }}",
                                                    fontSize = 10.5.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = avatarTextColor.copy(alpha = 0.85f)
                                                )
                                            }

                                            Text(
                                                text = if (continuousScan) "Input" else "Scan",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = accentColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
