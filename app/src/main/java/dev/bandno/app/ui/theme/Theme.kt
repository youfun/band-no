package dev.bandno.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Warm iron-oxide seed. Every M3 role is filled so default purple
 * (secondaryContainer / tertiary / surfaceTint) cannot leak through.
 */
private val LightColors = lightColorScheme(
    primary = Color(0xFF8F2A24),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD5),
    onPrimaryContainer = Color(0xFF3B0907),
    inversePrimary = Color(0xFFFFB4AB),
    secondary = Color(0xFF6B5C58),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8E2DA),
    onSecondaryContainer = Color(0xFF241E1C),
    tertiary = Color(0xFF5E5A33),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE4DFB6),
    onTertiaryContainer = Color(0xFF1C1B08),
    background = Color(0xFFF6F3EC),
    onBackground = Color(0xFF1C1B19),
    surface = Color(0xFFF6F3EC),
    onSurface = Color(0xFF1C1B19),
    surfaceVariant = Color(0xFFE7E2DA),
    onSurfaceVariant = Color(0xFF4A453F),
    surfaceTint = Color(0xFF8F2A24),
    inverseSurface = Color(0xFF31302E),
    inverseOnSurface = Color(0xFFF4F0E9),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF7A746C),
    outlineVariant = Color(0xFFCBC5BC),
    scrim = Color(0xFF000000),
    surfaceDim = Color(0xFFD6D3CC),
    surfaceBright = Color(0xFFF6F3EC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0EDE6),
    surfaceContainer = Color(0xFFEBE7E0),
    surfaceContainerHigh = Color(0xFFE5E1DA),
    surfaceContainerHighest = Color(0xFFDFDBD4),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF561E19),
    primaryContainer = Color(0xFF731F1B),
    onPrimaryContainer = Color(0xFFFFDAD5),
    inversePrimary = Color(0xFF8F2A24),
    secondary = Color(0xFFD0C4BC),
    onSecondary = Color(0xFF362F2C),
    secondaryContainer = Color(0xFF4D4541),
    onSecondaryContainer = Color(0xFFEDE4DC),
    tertiary = Color(0xFFC8C39C),
    onTertiary = Color(0xFF31310B),
    tertiaryContainer = Color(0xFF474721),
    onTertiaryContainer = Color(0xFFE4DFB6),
    background = Color(0xFF141311),
    onBackground = Color(0xFFE6E2DB),
    surface = Color(0xFF141311),
    onSurface = Color(0xFFE6E2DB),
    surfaceVariant = Color(0xFF4A453F),
    onSurfaceVariant = Color(0xFFCBC5BC),
    surfaceTint = Color(0xFFFFB4AB),
    inverseSurface = Color(0xFFE6E2DB),
    inverseOnSurface = Color(0xFF31302E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF958F88),
    outlineVariant = Color(0xFF4A453F),
    scrim = Color(0xFF000000),
    surfaceDim = Color(0xFF141311),
    surfaceBright = Color(0xFF3A3936),
    surfaceContainerLowest = Color(0xFF0F0E0C),
    surfaceContainerLow = Color(0xFF1C1B19),
    surfaceContainer = Color(0xFF201F1D),
    surfaceContainerHigh = Color(0xFF2B2A27),
    surfaceContainerHighest = Color(0xFF363532),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun BandNoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        shapes = AppShapes,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bandNoTopAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.surface,
    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
)
