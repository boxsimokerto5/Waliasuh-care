package com.example.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ChildStudent
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
    dialogTitle: String = "Scan Barcode Kartu Siswa",
    dialogSubtitle: String = "Membaca NIK / NIS Kartu Siswa Otomatis",
    quickPickTitle: String? = null,
    searchPlaceholder: String = "Cari nama, kelas, kamar, atau NIK siswi...",
    emptyListMessage: String = "Belum ada data siswi putri terdaftar.",
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

    // Laser Animation Effect
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 132f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserOffset"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
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
                        Column {
                            Text(
                                text = dialogTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = dialogSubtitle,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (continuousScan && sessionParticipantCount != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentBgColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$sessionParticipantCount Anak",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_scanner")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scanner Viewfinder Card (CameraX + ML Kit Live Preview)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
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

                            // Subtle scrim to keep reticle & hint readable
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.16f))
                            )
                        } else {
                            // Fallback Viewfinder when permission denied or camera unavailable
                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = cameraError
                                        ?: if (hasCameraPermission) {
                                            "Menyiapkan kamera scanner..."
                                        } else {
                                            "Izin Kamera Dibutuhkan untuk Scan Otomatis"
                                        },
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Framing Guide Rect
                        Box(
                            modifier = Modifier
                                .size(width = 240.dp, height = 140.dp)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(
                                        listOf(accentColor, accentColor.copy(alpha = 0.65f), accentColor)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            // Animated Laser Line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .offset { IntOffset(0, laserOffset.dp.roundToPx()) }
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color(0xFFFF1493),
                                                Color(0xFFFFFFFF),
                                                Color(0xFFFF1493),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }

                        if (hasCameraPermission && cameraError == null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Arahkan Barcode / QR Kartu Siswa ke Kotak",
                                    color = Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Flashlight Toggle Button Top Right
                        if (hasCameraPermission && cameraError == null && hasFlashUnit) {
                            IconButton(
                                onClick = { isFlashOn = !isFlashOn },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(48.dp)
                                    .testTag("btn_toggle_scanner_flash")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                        contentDescription = if (isFlashOn) "Matikan Lampu Kilat" else "Nyalakan Lampu Kilat",
                                        tint = if (isFlashOn) Color(0xFFFACC15) else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (!hasCameraPermission) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            cameraError = null
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Izinkan Akses Kamera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Manual NIK / NIS Input Bar
                Text(
                    text = "Atau Input NIK / NIS Siswa Manual:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
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
                        placeholder = { Text("Contoh: 3506123456780001", fontSize = 12.sp) },
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
                        Text(if (continuousScan) "Input" else "Pilih", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Pick: Daftar Kartu Siswa Terdaftar (Menampilkan seluruh siswa tanpa batas + filter cepat)
                Text(
                    text = quickPickTitle ?: "Pilih Cepat Kartu Siswi Asrama Putri (${filteredFemaleStudents.size} Siswi):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (femaleStudents.isNotEmpty()) {
                    OutlinedTextField(
                        value = quickSearchQuery,
                        onValueChange = { quickSearchQuery = it },
                        placeholder = { Text(searchPlaceholder, fontSize = 11.5.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cari Siswa",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_quick_pick_haid_search")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
                                emptyListMessage
                            } else {
                                "Tidak ada siswa yang cocok dengan pencarian \"$quickSearchQuery\"."
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 210.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredFemaleStudents, key = { it.id }) { student ->
                            Card(
                                onClick = {
                                    val codeToUse = if (student.nik.isNotBlank()) student.nik else if (student.nis.isNotBlank()) student.nis else student.id.toString()
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onScanned(codeToUse)
                                    if (!continuousScan) {
                                        onDismiss()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBgColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
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
                                            fontSize = 12.5.sp,
                                            color = titleTextColor
                                        )
                                        Text(
                                            text = "${student.classGrade} • ${student.dormitory} • NIK: ${if (student.nik.isNotBlank()) student.nik else student.nis.ifBlank { student.id.toString() }}",
                                            fontSize = 10.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = avatarTextColor.copy(alpha = 0.85f)
                                        )
                                    }

                                    Text(
                                        text = if (continuousScan) "Input" else "Scan",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }
                }

                if (continuousScan) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_done_continuous_scan")
                    ) {
                        Text(
                            text = "Selesai & Tutup Kamera${sessionParticipantCount?.let { " ($it Terinput)" } ?: ""}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Pop-Up Mengambang Otomatis Muncul & Hilang Saat Scan Berkelanjutan
            androidx.compose.animation.AnimatedVisibility(
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
                    .padding(horizontal = 20.dp, vertical = 14.dp)
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
        }
    }
}
