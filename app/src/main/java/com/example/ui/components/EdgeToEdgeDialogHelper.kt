package com.example.ui.components

import android.graphics.Color as AndroidColor
import android.os.Build
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat

/**
 * Configures a full-screen Compose Dialog window for true edge-to-edge layout across
 * Android versions (API 24..36+) and OEM skins (e.g. HyperOS/MIUI 3-button nav bar),
 * ensuring [androidx.compose.foundation.layout.WindowInsets.safeDrawing], status bar,
 * and navigation bar insets are properly dispatched into the dialog's Compose tree.
 */
@Composable
fun ConfigureEdgeToEdgeDialogWindow(
    lightStatusBars: Boolean,
    lightNavigationBars: Boolean = lightStatusBars,
    adjustSoftInput: Boolean = false
) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val dialogWindow = (view.parent as? DialogWindowProvider)?.window

    SideEffect {
        val window = dialogWindow ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        window.setDimAmount(0f)
        if (adjustSoftInput) {
            @Suppress("DEPRECATION")
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.statusBarColor = AndroidColor.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = AndroidColor.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = lightStatusBars
            isAppearanceLightNavigationBars = lightNavigationBars
        }
    }

    LaunchedEffect(dialogWindow) {
        if (dialogWindow != null) {
            ViewCompat.requestApplyInsets(dialogWindow.decorView)
        }
    }
}
