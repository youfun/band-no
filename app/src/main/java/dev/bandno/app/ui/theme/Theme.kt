package dev.bandno.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Barrier = Color(0xFF8B1E1E)
private val BarrierBright = Color(0xFFD4513D)
private val Paper = Color(0xFFF4F1EA)
private val Ink = Color(0xFF1A1816)
private val Night = Color(0xFF141311)
private val Fog = Color(0xFFE7E2DA)

private val LightColors = lightColorScheme(
    primary = Barrier,
    onPrimary = Color.White,
    secondary = Ink,
    onSecondary = Paper,
    secondaryContainer = Fog,
    onSecondaryContainer = Ink,
    tertiaryContainer = Fog,
    onTertiaryContainer = Ink,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Fog,
    onSurfaceVariant = Color(0xFF4A453F),
    surfaceContainerLowest = Paper,
    surfaceContainerLow = Paper,
    surfaceContainer = Color(0xFFEDE8DF),
    surfaceContainerHigh = Fog,
    surfaceContainerHighest = Fog,
    outline = Color(0xFF7A746C),
    error = Barrier,
)

private val DarkColors = darkColorScheme(
    primary = BarrierBright,
    onPrimary = Night,
    secondary = Paper,
    onSecondary = Night,
    secondaryContainer = Color(0xFF2A2724),
    onSecondaryContainer = Color(0xFFEDECE8),
    tertiaryContainer = Color(0xFF2A2724),
    onTertiaryContainer = Color(0xFFEDECE8),
    background = Night,
    onBackground = Color(0xFFEDECE8),
    surface = Night,
    onSurface = Color(0xFFEDECE8),
    surfaceVariant = Color(0xFF2A2724),
    onSurfaceVariant = Color(0xFFC4BDB4),
    surfaceContainerLowest = Night,
    surfaceContainerLow = Night,
    surfaceContainer = Color(0xFF1C1A18),
    surfaceContainerHigh = Color(0xFF2A2724),
    surfaceContainerHighest = Color(0xFF2A2724),
    outline = Color(0xFF8A837A),
    error = BarrierBright,
)

@Composable
fun BandNoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
