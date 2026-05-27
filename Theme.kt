package com.buttonautomation.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NeonGreen = Color(0xFF00FF94)
val DeepBlack = Color(0xFF0A0A0F)
val SurfaceDark = Color(0xFF141420)
val SurfaceVariantDark = Color(0xFF1E1E2E)
val PrimaryPurple = Color(0xFF7C4DFF)
val SecondaryBlue = Color(0xFF40C4FF)
val OnSurface = Color(0xFFE8E8F0)
val OnSurfaceMuted = Color(0xFF9090A8)
val ErrorRed = Color(0xFFFF5252)
val SuccessGreen = Color(0xFF69FF87)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A2C9E),
    onPrimaryContainer = Color(0xFFE8DDFF),
    secondary = SecondaryBlue,
    onSecondary = DeepBlack,
    secondaryContainer = Color(0xFF004A6B),
    onSecondaryContainer = Color(0xFFBDE9FF),
    background = DeepBlack,
    onBackground = OnSurface,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceMuted,
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF3A3A55)
)

@Composable
fun ButtonAutomationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
