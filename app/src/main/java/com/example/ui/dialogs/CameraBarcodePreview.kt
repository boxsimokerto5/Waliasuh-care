package com.example.ui.dialogs

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

internal fun extractBarcodePayload(rawValue: String): String {
    val trimmed = rawValue.trim()
    if (trimmed.isEmpty()) return ""

    val lines = trimmed.lines().map { it.trim() }.filter { it.isNotEmpty() }
    for (line in lines) {
        val lower = line.lowercase()
        if (lower.startsWith("nik:") || lower.startsWith("nik=")) {
            val value = line.substring(4).trim()
            if (value.isNotEmpty()) return value
        }
        if (lower.startsWith("nis:") || lower.startsWith("nis=")) {
            val value = line.substring(4).trim()
            if (value.isNotEmpty()) return value
        }
    }
    return trimmed
}

@Composable
fun CameraBarcodePreview(
    isFlashOn: Boolean,
    onBarcodeDetected: (String) -> Unit,
    onFlashAvailabilityChanged: (Boolean) -> Unit,
    onCameraError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentOnBarcodeDetected by rememberUpdatedState(onBarcodeDetected)
    val currentOnFlashAvailabilityChanged by rememberUpdatedState(onFlashAvailabilityChanged)
    val currentOnCameraError by rememberUpdatedState(onCameraError)

    val previewView = remember(context) {
        PreviewView(context).apply {
            // COMPATIBLE (TextureView) clips cleanly inside rounded corners & ModalBottomSheet
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    val hasTriggeredScan = remember { AtomicBoolean(false) }

    LaunchedEffect(isFlashOn, boundCamera) {
        val camera = boundCamera ?: return@LaunchedEffect
        if (camera.cameraInfo.hasFlashUnit()) {
            try {
                camera.cameraControl.enableTorch(isFlashOn)
            } catch (_: Exception) {
            }
        }
    }

    DisposableEffect(lifecycleOwner, previewView) {
        var isDisposed = false
        var cameraProvider: ProcessCameraProvider? = null
        var imageAnalysis: ImageAnalysis? = null

        val analysisExecutor = Executors.newSingleThreadExecutor()
        val scannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        val barcodeScanner = BarcodeScanning.getClient(scannerOptions)
        val mainExecutor = ContextCompat.getMainExecutor(context)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                if (isDisposed) return@addListener
                try {
                    val provider = cameraProviderFuture.get()
                    if (isDisposed) return@addListener
                    cameraProvider = provider

                    val cameraSelector = when {
                        provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                        provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                        else -> {
                            currentOnCameraError("Kamera perangkat tidak tersedia.")
                            return@addListener
                        }
                    }

                    val preview = Preview.Builder()
                        .build()
                        .also { it.surfaceProvider = previewView.surfaceProvider }

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { useCase ->
                            useCase.setAnalyzer(analysisExecutor) { imageProxy ->
                                processBarcodeFrame(
                                    imageProxy = imageProxy,
                                    barcodeScanner = barcodeScanner,
                                    hasTriggeredScan = hasTriggeredScan,
                                    onDetected = { code ->
                                        mainExecutor.execute {
                                            if (!isDisposed) {
                                                currentOnBarcodeDetected(code)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    imageAnalysis = analysis

                    provider.unbindAll()
                    val camera = provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        analysis
                    )
                    boundCamera = camera
                    currentOnFlashAvailabilityChanged(camera.cameraInfo.hasFlashUnit())
                } catch (_: Exception) {
                    if (!isDisposed) {
                        currentOnFlashAvailabilityChanged(false)
                        currentOnCameraError("Kamera tidak dapat diakses. Gunakan input NIK atau pilih cepat.")
                    }
                }
            },
            mainExecutor
        )

        onDispose {
            isDisposed = true
            try {
                boundCamera?.cameraControl?.enableTorch(false)
            } catch (_: Exception) {
            }
            boundCamera = null
            try {
                imageAnalysis?.clearAnalyzer()
            } catch (_: Exception) {
            }
            try {
                cameraProvider?.unbindAll()
            } catch (_: Exception) {
            }
            try {
                barcodeScanner.close()
            } catch (_: Exception) {
            }
            analysisExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@OptIn(ExperimentalGetImage::class)
private fun processBarcodeFrame(
    imageProxy: ImageProxy,
    barcodeScanner: BarcodeScanner,
    hasTriggeredScan: AtomicBoolean,
    onDetected: (String) -> Unit
) {
    if (hasTriggeredScan.get()) {
        imageProxy.close()
        return
    }
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    barcodeScanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            val rawCode = barcodes.firstNotNullOfOrNull { barcode ->
                (barcode.rawValue ?: barcode.displayValue)?.takeIf { it.isNotBlank() }
            }
            if (rawCode != null) {
                val extracted = extractBarcodePayload(rawCode)
                if (extracted.isNotBlank() && hasTriggeredScan.compareAndSet(false, true)) {
                    onDetected(extracted)
                }
            }
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
