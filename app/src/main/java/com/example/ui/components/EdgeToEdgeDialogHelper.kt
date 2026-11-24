package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Configures a full-screen Compose Dialog window for true edge-to-edge layout across
 * Android versions (API 24..36+) and OEM skins (e.g. HyperOS/MIUI 3-button nav bar),
 * ensuring the dialog covers behind status/navigation bars without floating top gaps
 * and ensuring status/navigation bar icon contrast matches the dialog surface.
 */
@Composable
fun ConfigureEdgeToEdgeDialogWindow(
    lightStatusBars: Boolean,
    lightNavigationBars: Boolean = lightStatusBars,
    adjustSoftInput: Boolean = false
) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val context = LocalContext.current
    val hostActivity = remember(context) { context.findActivity() }
    val dialogWindow = (view.parent as? DialogWindowProvider)?.window

    SideEffect {
        val window = dialogWindow ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
        @Suppress("DEPRECATION")
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_DIM_BEHIND or
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
        window.addFlags(
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        window.attributes = window.attributes.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
            gravity = Gravity.FILL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                } else {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                fitInsetsTypes = 0
                fitInsetsSides = 0
            }
        }
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
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = lightStatusBars
            isAppearanceLightNavigationBars = lightNavigationBars
        }
    }

    DisposableEffect(hostActivity, dialogWindow, lightStatusBars, lightNavigationBars) {
        val activityWindow = hostActivity?.window
        val activityController = activityWindow?.let {
            WindowCompat.getInsetsController(it, it.decorView)
        }
        val prevLightStatus = activityController?.isAppearanceLightStatusBars
        val prevLightNav = activityController?.isAppearanceLightNavigationBars

        activityController?.apply {
            isAppearanceLightStatusBars = lightStatusBars
            isAppearanceLightNavigationBars = lightNavigationBars
        }
        dialogWindow?.let { dw ->
            WindowCompat.getInsetsController(dw, dw.decorView).apply {
                isAppearanceLightStatusBars = lightStatusBars
                isAppearanceLightNavigationBars = lightNavigationBars
            }
        }

        onDispose {
            if (activityController != null && prevLightStatus != null && prevLightNav != null) {
                activityController.isAppearanceLightStatusBars = prevLightStatus
                activityController.isAppearanceLightNavigationBars = prevLightNav
            }
        }
    }

    LaunchedEffect(dialogWindow) {
        if (dialogWindow != null) {
            ViewCompat.requestApplyInsets(dialogWindow.decorView)
        }
    }
}

private data class InsetPxTuple(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0
)

/**
 * Returns safe drawing [WindowInsets] inside a Compose Dialog, taking the union of the
 * dialog's own [WindowInsets.safeDrawing] and the host Activity's root system bars/cutout
 * insets so bottom FABs/bars never drop behind a 3-button system navigation bar even on
 * OEMs (e.g. HyperOS/MIUI) where Dialog windows report zero bottom insets.
 */
@Composable
fun rememberEdgeToEdgeDialogSafeInsets(): WindowInsets {
    val dialogSafeDrawing = WindowInsets.safeDrawing
    val view = LocalView.current
    if (view.isInEditMode) return dialogSafeDrawing

    val context = LocalContext.current
    val hostActivity = remember(context) { context.findActivity() }
    val hostDecorView = hostActivity?.window?.decorView

    fun readHostInsets(): InsetPxTuple {
        val target = hostDecorView ?: return InsetPxTuple()
        val root = ViewCompat.getRootWindowInsets(target) ?: return InsetPxTuple()
        val sys = root.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        return InsetPxTuple(
            left = sys.left,
            top = sys.top,
            right = sys.right,
            bottom = sys.bottom
        )
    }

    var hostInsets by remember(hostDecorView) { mutableStateOf(readHostInsets()) }

    DisposableEffect(hostDecorView) {
        if (hostDecorView == null) {
            onDispose { }
        } else {
            val listener = ViewTreeObserver.OnGlobalLayoutListener {
                val next = readHostInsets()
                if (next != hostInsets) {
                    hostInsets = next
                }
            }
            hostDecorView.viewTreeObserver.addOnGlobalLayoutListener(listener)
            onDispose {
                if (hostDecorView.viewTreeObserver.isAlive) {
                    hostDecorView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
                }
            }
        }
    }

    val hostWindowInsets = remember(hostInsets) {
        WindowInsets(
            left = hostInsets.left,
            top = hostInsets.top,
            right = hostInsets.right,
            bottom = hostInsets.bottom
        )
    }
    return dialogSafeDrawing.union(hostWindowInsets)
}
