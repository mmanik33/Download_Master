package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Download Master Base Dark Theme
val DarkBackground = Color(0xFF0A0D18)
val DarkSurface = Color(0xFF131724)
val DarkSurfaceVariant = Color(0xFF1C2234)
val DarkSurfaceHighlight = Color(0xFF252C42)
val DarkBorder = Color(0xFF2A334D)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextTertiary = Color(0xFF64748B)

// Download Master Base Light Theme (Soft crisp off-white for comfortable reading)
val LightBackground = Color(0xFFF6F8FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEDF2F7)
val LightSurfaceHighlight = Color(0xFFE2E8F0)
val LightBorder = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextTertiary = Color(0xFF94A3B8)

// Color Palettes
enum class ColorThemePreset(val id: String, val label: String, val primary: Color, val secondary: Color) {
    DYNAMIC("DYNAMIC", "Material You", Color(0xFF6750A4), Color(0xFF625B71)),
    PURPLE("PURPLE", "Neon Violet", Color(0xFF8B5CF6), Color(0xFFEC4899)),
    BLUE("BLUE", "Ocean Blue", Color(0xFF2563EB), Color(0xFF06B6D4)),
    GREEN("GREEN", "Emerald", Color(0xFF10B981), Color(0xFF14B8A6)),
    ORANGE("ORANGE", "Sunset Amber", Color(0xFFF59E0B), Color(0xFFEF4444)),
    ROSE("ROSE", "Cyber Rose", Color(0xFFE11D48), Color(0xFFDB2777))
}

// Default / Accent colors
val PrimaryPurple = Color(0xFF8B5CF6)
val PrimaryPurpleDark = Color(0xFF7C3AED)
val AccentPink = Color(0xFFEC4899)
val AccentPinkDark = Color(0xFFDB2777)
val AccentCyan = Color(0xFF06B6D4)
val AccentBlue = Color(0xFF3B82F6)
val AccentGreen = Color(0xFF10B981)
val AccentOrange = Color(0xFFF59E0B)

// Backwards compatibility aliases
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFF64748B)

// Default Gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
)

val PrimaryGradientVertical = Brush.verticalGradient(
    colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
)

val CyanBlueGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
)

val CardGlowGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1E2638), Color(0xFF131724))
)

// App Theme Color Holder with Dynamic Theme Support
data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceHighlight: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val primary: Color = PrimaryPurple,
    val secondary: Color = AccentPink,
    val primaryGradient: Brush = PrimaryGradient,
    val isDark: Boolean
)

val DarkAppColors = AppColors(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    surfaceHighlight = DarkSurfaceHighlight,
    border = DarkBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    primary = PrimaryPurple,
    secondary = AccentPink,
    primaryGradient = PrimaryGradient,
    isDark = true
)

val LightAppColors = AppColors(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    surfaceHighlight = LightSurfaceHighlight,
    border = LightBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    primary = PrimaryPurple,
    secondary = AccentPink,
    primaryGradient = PrimaryGradient,
    isDark = false
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

// Material Theme Mappings
val OceanPrimaryDark = PrimaryPurple
val OceanOnPrimaryDark = Color(0xFFFFFFFF)
val OceanPrimaryContainerDark = Color(0xFF2E1B5B)
val OceanOnPrimaryContainerDark = Color(0xFFE9D5FF)

val OceanSecondaryDark = AccentPink
val OceanOnSecondaryDark = Color(0xFFFFFFFF)
val OceanSecondaryContainerDark = Color(0xFF5B163B)
val OceanOnSecondaryContainerDark = Color(0xFFFCE7F3)

val OceanTertiaryDark = AccentCyan
val OceanOnTertiaryDark = Color(0xFF00363F)
val OceanTertiaryContainerDark = Color(0xFF004F5C)
val OceanOnTertiaryContainerDark = Color(0xFFCFFAFE)

val OceanBackgroundDark = DarkBackground
val OceanOnBackgroundDark = DarkTextPrimary
val OceanSurfaceDark = DarkSurface
val OceanOnSurfaceDark = DarkTextPrimary
val OceanSurfaceVariantDark = DarkSurfaceVariant
val OceanOnSurfaceVariantDark = DarkTextSecondary
val OceanOutlineDark = DarkBorder

val OceanPrimaryLight = PrimaryPurple
val OceanOnPrimaryLight = Color.White
val OceanPrimaryContainerLight = Color(0xFFF3E8FF)
val OceanOnPrimaryContainerLight = Color(0xFF3B0764)
val OceanSecondaryLight = AccentPink
val OceanOnSecondaryLight = Color.White
val OceanSecondaryContainerLight = Color(0xFFFDF2F8)
val OceanOnSecondaryContainerLight = Color(0xFF700B3E)
val OceanTertiaryLight = AccentCyan
val OceanOnTertiaryLight = Color.White
val OceanTertiaryContainerLight = Color(0xFFE0F2FE)
val OceanOnTertiaryContainerLight = Color(0xFF034A63)

val OceanBackgroundLight = LightBackground
val OceanOnBackgroundLight = LightTextPrimary
val OceanSurfaceLight = LightSurface
val OceanOnSurfaceLight = LightTextPrimary
val OceanSurfaceVariantLight = LightSurfaceVariant
val OceanOnSurfaceVariantLight = LightTextSecondary
val OceanOutlineLight = LightBorder

// Platform Brand Colors
val YouTubeRed = Color(0xFFFF0000)
val TikTokCyan = Color(0xFF00F2FE)
val TwitterBlue = Color(0xFF1DA1F2)
val InstagramPink = Color(0xFFE1306C)
val InstagramPurple = InstagramPink
val BilibiliPink = Color(0xFFFB7299)
val FacebookBlue = Color(0xFF1877F2)
val VimeoBlue = Color(0xFF1AB7EA)
val DailymotionBlue = Color(0xFF0066DC)
val LikeePink = Color(0xFFFF2442)
val PinterestRed = Color(0xFFBD081C)
val RedditOrange = Color(0xFFFF4500)
val SoundCloudOrange = Color(0xFFFF5500)
val TumblrBlue = Color(0xFF35465C)

