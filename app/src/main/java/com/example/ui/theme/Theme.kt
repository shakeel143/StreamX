package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TubeDarkColorScheme = darkColorScheme(
    primary = TubeRed,
    secondary = TubeAmber,
    tertiary = GlowBlue,
    background = OledBackground,
    surface = DarkSurface,
    onPrimary = PureWhite,
    onSecondary = OledBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = LightSurface,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // We enforce our highly-optimized nighttime OLED Crimson theme
    MaterialTheme(
        colorScheme = TubeDarkColorScheme,
        typography = Typography,
        content = content
    )
}
