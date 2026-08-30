package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    paletteId: String = "PURPLE",
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current

    // Setup WindowCompat to make status bar and navigation bar icons perfectly crisp in light & dark mode
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // In Light theme, status bar icons MUST be dark (isAppearanceLightStatusBars = true)
                // In Dark theme, status bar icons MUST be light (isAppearanceLightStatusBars = false)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val isDynamic = paletteId == "DYNAMIC" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val preset = ColorThemePreset.values().find { it.id == paletteId } ?: ColorThemePreset.PURPLE

    val colorScheme = when {
        isDynamic -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            darkColorScheme(
                primary = preset.primary,
                secondary = preset.secondary,
                background = DarkBackground,
                surface = DarkSurface,
                surfaceVariant = DarkSurfaceVariant,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = DarkTextPrimary,
                onSurface = DarkTextPrimary,
                onSurfaceVariant = DarkTextSecondary,
                outline = DarkBorder
            )
        }
        else -> {
            lightColorScheme(
                primary = preset.primary,
                secondary = preset.secondary,
                background = LightBackground,
                surface = LightSurface,
                surfaceVariant = LightSurfaceVariant,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = LightTextPrimary,
                onSurface = LightTextPrimary,
                onSurfaceVariant = LightTextSecondary,
                outline = LightBorder
            )
        }
    }

    val primaryColor = colorScheme.primary
    val secondaryColor = colorScheme.secondary
    val primaryGradient = Brush.horizontalGradient(listOf(primaryColor, secondaryColor))

    val appColors = if (darkTheme) {
        AppColors(
            background = DarkBackground,
            surface = DarkSurface,
            surfaceVariant = DarkSurfaceVariant,
            surfaceHighlight = DarkSurfaceHighlight,
            border = DarkBorder,
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            textTertiary = DarkTextTertiary,
            primary = primaryColor,
            secondary = secondaryColor,
            primaryGradient = primaryGradient,
            isDark = true
        )
    } else {
        AppColors(
            background = LightBackground,
            surface = LightSurface,
            surfaceVariant = LightSurfaceVariant,
            surfaceHighlight = LightSurfaceHighlight,
            border = LightBorder,
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textTertiary = LightTextTertiary,
            primary = primaryColor,
            secondary = secondaryColor,
            primaryGradient = primaryGradient,
            isDark = false
        )
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}


