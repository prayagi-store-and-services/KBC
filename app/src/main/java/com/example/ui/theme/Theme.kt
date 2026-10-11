package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TarkShastraColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = NavyDeepest,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,
    secondary = InfoCyan,
    onSecondary = NavyDeepest,
    secondaryContainer = NavyCardElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = PurpleAccent,
    onTertiary = TextPrimary,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavyCard,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder,
    error = ErrorRed,
    onError = TextPrimary
)

private val NewKbcColorScheme = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color(0xFF1A1000),
    primaryContainer = Color(0xFF8D5A00),
    onPrimaryContainer = Color(0xFFFFE0B2),
    secondary = Color(0xFF4DD0E1),
    onSecondary = Color(0xFF00252B),
    secondaryContainer = Color(0xFF231F3D),
    onSecondaryContainer = Color(0xFFEDE7F6),
    tertiary = Color(0xFFB39DDB),
    onTertiary = Color(0xFF1A0F33),
    background = Color(0xFF0F0B1E),
    onBackground = Color(0xFFEDE7F6),
    surface = Color(0xFF171230),
    onSurface = Color(0xFFEDE7F6),
    surfaceVariant = Color(0xFF211B40),
    onSurfaceVariant = Color(0xFFCFC5E8),
    outline = Color(0xFF3B3366),
    error = ErrorRed,
    onError = TextPrimary
)

@Composable
fun TarkShastraTheme(
    darkTheme: Boolean = true, // We optimize for dramatic, immersive hot-seat dark palette
    content: @Composable () -> Unit
) {
    val colorScheme = com.example.festival.festiveScheme(if (RedesignGate.isOn()) NewKbcColorScheme else TarkShastraColorScheme)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = NavyDeepest.toArgb()
            window.navigationBarColor = NavyDeepest.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
