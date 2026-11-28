package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.i18n.LocalAppStrings
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FlexibleUpdateUiState(
    val isDownloading: Boolean = false,
    val downloadProgress: Float? = null,
    val isDownloadedReadyToInstall: Boolean = false,
    val isBannerDismissed: Boolean = false
)

class InAppUpdateCoordinator(context: Context) {

    private val appUpdateManager: AppUpdateManager? = runCatching {
        AppUpdateManagerFactory.create(context.applicationContext)
    }.onFailure { err ->
        Log.w(TAG, "Play In-App Update unavailable: ${err.message}")
    }.getOrNull()

    private val _uiState = MutableStateFlow(FlexibleUpdateUiState())
    val uiState: StateFlow<FlexibleUpdateUiState> = _uiState.asStateFlow()

    private var hasPromptedThisSession = false
    private var isListenerRegistered = false

    private val installStateListener = InstallStateUpdatedListener { state ->
        handleInstallState(state)
    }

    fun registerListener() {
        val manager = appUpdateManager ?: return
        if (isListenerRegistered) return
        runCatching {
            manager.registerListener(installStateListener)
            isListenerRegistered = true
        }.onFailure { err ->
            Log.w(TAG, "Failed to register in-app update listener: ${err.message}")
        }
    }

    fun checkForFlexibleUpdate(updateLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        val manager = appUpdateManager ?: return
        runCatching {
            manager.appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        _uiState.update {
                            it.copy(
                                isDownloading = false,
                                downloadProgress = 1f,
                                isDownloadedReadyToInstall = true,
                                isBannerDismissed = false
                            )
                        }
                        return@addOnSuccessListener
                    }

                    if (!hasPromptedThisSession &&
                        appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    ) {
                        hasPromptedThisSession = true
                        runCatching {
                            manager.startUpdateFlowForResult(
                                appUpdateInfo,
                                updateLauncher,
                                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                            )
                        }.onFailure { err ->
                            Log.w(TAG, "Unable to launch flexible update flow: ${err.message}")
                        }
                    }
                }
                .addOnFailureListener { err ->
                    // Expected when app is sideloaded or Play Store is unavailable
                    Log.d(TAG, "In-app update check skipped: ${err.message}")
                }
        }
    }

    fun checkDownloadedUpdateOnResume() {
        val manager = appUpdateManager ?: return
        runCatching {
            manager.appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        _uiState.update {
                            it.copy(
                                isDownloading = false,
                                downloadProgress = 1f,
                                isDownloadedReadyToInstall = true,
                                isBannerDismissed = false
                            )
                        }
                    }
                }
        }
    }

    fun onUpdateFlowResult(resultCode: Int) {
        if (resultCode != Activity.RESULT_OK) {
            hasPromptedThisSession = true
            _uiState.update { it.copy(isDownloading = false) }
        }
    }

    fun completeUpdate() {
        val manager = appUpdateManager ?: return
        runCatching {
            manager.completeUpdate()
        }.onFailure { err ->
            Log.w(TAG, "Failed to complete flexible update: ${err.message}")
        }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(isBannerDismissed = true) }
    }

    fun unregisterListener() {
        val manager = appUpdateManager ?: return
        if (!isListenerRegistered) return
        runCatching {
            manager.unregisterListener(installStateListener)
        }
        isListenerRegistered = false
    }

    internal fun handleInstallState(state: InstallState) {
        handleInstallStatus(
            status = state.installStatus(),
            bytesDownloaded = state.bytesDownloaded(),
            totalBytesToDownload = state.totalBytesToDownload()
        )
    }

    internal fun handleInstallStatus(
        status: Int,
        bytesDownloaded: Long = 0L,
        totalBytesToDownload: Long = 0L
    ) {
        when (status) {
            InstallStatus.PENDING,
            InstallStatus.DOWNLOADING -> {
                val progress = if (totalBytesToDownload > 0L) {
                    (bytesDownloaded.toFloat() / totalBytesToDownload.toFloat()).coerceIn(0f, 1f)
                } else {
                    null
                }
                _uiState.update {
                    it.copy(
                        isDownloading = true,
                        downloadProgress = progress,
                        isDownloadedReadyToInstall = false
                    )
                }
            }

            InstallStatus.DOWNLOADED -> {
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgress = 1f,
                        isDownloadedReadyToInstall = true,
                        isBannerDismissed = false
                    )
                }
            }

            InstallStatus.INSTALLED,
            InstallStatus.CANCELED,
            InstallStatus.FAILED -> {
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgress = null,
                        isDownloadedReadyToInstall = false
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "InAppUpdateCoordinator"
    }
}

@Composable
fun FlexibleUpdatePromptBanner(
    state: FlexibleUpdateUiState,
    onRestartClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEn = LocalAppStrings.current.isEnglish
    val showBanner = (state.isDownloadedReadyToInstall || state.isDownloading) && !state.isBannerDismissed

    AnimatedVisibility(
        visible = showBanner,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        val isReady = state.isDownloadedReadyToInstall
        val accentTeal = Color(0xFF0F766E)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, accentTeal.copy(alpha = 0.28f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 540.dp)
                .testTag("banner_flexible_in_app_update")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accentTeal.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isReady) Icons.Default.SystemUpdate else Icons.Default.Downloading,
                                contentDescription = if (isEn) "App Update" else "Pembaruan Aplikasi",
                                tint = accentTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                isReady && isEn -> "App update ready to install"
                                isReady -> "Pembaruan aplikasi siap dipasang"
                                isEn -> "Downloading app update..."
                                else -> "Mengunduh pembaruan aplikasi..."
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                isReady && isEn -> "Restart the app to apply the latest Play Store version."
                                isReady -> "Muat ulang aplikasi untuk memakai versi terbaru."
                                isEn -> "You can keep using the app while it downloads."
                                else -> "Anda tetap bisa memakai aplikasi selama unduhan berjalan."
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }

                    if (isReady) {
                        Button(
                            onClick = onRestartClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentTeal,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("btn_flexible_update_restart")
                        ) {
                            Text(
                                text = if (isEn) "Restart" else "Pasang",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_flexible_update_dismiss")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Dismiss update notice" else "Tutup pemberitahuan update",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!isReady && state.isDownloading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = state.downloadProgress
                    if (progress != null) {
                        LinearProgressIndicator(
                            progress = { progress },
                            color = accentTeal,
                            trackColor = accentTeal.copy(alpha = 0.16f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .testTag("progress_flexible_update_download")
                        )
                    } else {
                        LinearProgressIndicator(
                            color = accentTeal,
                            trackColor = accentTeal.copy(alpha = 0.16f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .testTag("progress_flexible_update_download")
                        )
                    }
                }
            }
        }
    }
}
