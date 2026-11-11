package lab.neurotrino.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Cyan,
    onPrimary = Graphite,
    secondary = Amber,
    onSecondary = Graphite,
    tertiary = Indigo,
    background = Graphite,
    onBackground = Ivory,
    surface = GraphiteElevated,
    onSurface = Ivory,
    surfaceVariant = GraphitePanel,
    onSurfaceVariant = IvoryMuted,
    outline = Line,
    error = Danger,
    onError = Graphite,
)

private val LightColors = lightColorScheme(
    primary = LightCyan,
    onPrimary = Color.White,
    secondary = LightAmber,
    onSecondary = Color.White,
    tertiary = Color(0xFF3B5CB0),
    background = LightBg,
    onBackground = LightText,
    surface = LightElevated,
    onSurface = LightText,
    surfaceVariant = Color(0xFFEBE4D4),
    onSurfaceVariant = LightMuted,
    outline = Color(0x33282012),
    error = Color(0xFFB34B3C),
)

@Composable
fun NeurotrinoTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = LabTypography,
        content = content,
    )
}
