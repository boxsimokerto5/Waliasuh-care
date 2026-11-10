package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
    primary = WaliPrimaryDark,
    onPrimary = WaliOnPrimaryDark,
    primaryContainer = WaliPrimaryContainerDark,
    onPrimaryContainer = WaliOnPrimaryContainerDark,
    secondary = WaliSecondaryDark,
    onSecondary = WaliOnSecondaryDark,
    secondaryContainer = WaliSecondaryContainerDark,
    onSecondaryContainer = WaliOnSecondaryContainerDark,
    background = WaliBackgroundDark,
    surface = WaliSurfaceDark,
    surfaceVariant = WaliSurfaceVariantDark,
    onSurface = WaliOnSurfaceDark,
    onSurfaceVariant = WaliOnSurfaceVariantDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = WaliPrimary,
    onPrimary = WaliOnPrimary,
    primaryContainer = WaliPrimaryContainer,
    onPrimaryContainer = WaliOnPrimaryContainer,
    secondary = WaliSecondary,
    onSecondary = WaliOnSecondary,
    secondaryContainer = WaliSecondaryContainer,
    onSecondaryContainer = WaliOnSecondaryContainer,
    tertiary = WaliTertiary,
    onTertiary = WaliOnTertiary,
    tertiaryContainer = WaliTertiaryContainer,
    onTertiaryContainer = WaliOnTertiaryContainer,
    background = WaliBackground,
    surface = WaliSurface,
    surfaceVariant = WaliSurfaceVariant,
    onSurface = WaliOnSurface,
    onSurfaceVariant = WaliOnSurfaceVariant,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
