package lab.neurotrino.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity

private val DarkColors = darkColorScheme(
    primary = Cyan,
    onPrimary = GraphiteVoid,
    primaryContainer = Color(0xFF17433E),
    onPrimaryContainer = Cyan,
    secondary = Amber,
    onSecondary = GraphiteVoid,
    secondaryContainer = Color(0xFF42301B),
    onSecondaryContainer = Amber,
    tertiary = Indigo,
    onTertiary = GraphiteVoid,
    tertiaryContainer = Color(0xFF2A3260),
    onTertiaryContainer = Indigo,
    background = Graphite,
    onBackground = Ivory,
    surface = GraphiteElevated,
    onSurface = Ivory,
    surfaceVariant = GraphitePanel,
    onSurfaceVariant = IvoryMuted,
    surfaceContainerLowest = GraphiteVoid,
    surfaceContainerLow = GraphiteWell,
    surfaceContainer = GraphiteElevated,
    surfaceContainerHigh = GraphitePanel,
    surfaceContainerHighest = Color(0xFF212B3B),
    outline = GraphiteLine,
    outlineVariant = Color(0xFF1C2532),
    error = Danger,
    onError = GraphiteVoid,
    errorContainer = Color(0xFF4A2620),
    onErrorContainer = Danger,
    scrim = Color(0xCC07090D),
)

private val LightColors = lightColorScheme(
    primary = LightCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEAE3),
    onPrimaryContainer = Color(0xFF0C4A3F),
    secondary = LightAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEDFC6),
    onSecondaryContainer = Color(0xFF4A3410),
    tertiary = LightIndigo,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCE2FB),
    onTertiaryContainer = Color(0xFF22306B),
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightPanel,
    onSurfaceVariant = LightMuted,
    surfaceContainerLowest = LightWell,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightElevated,
    surfaceContainerHighest = Color(0xFFFFFFFF),
    outline = LightLine,
    outlineVariant = Color(0xFFE3DED0),
    error = LightDanger,
    onError = Color.White,
    errorContainer = Color(0xFFF6DAD2),
    onErrorContainer = Color(0xFF5B1F14),
    scrim = Color(0x99211D15),
)

@Composable
fun NeurotrinoTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? ComponentActivity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }
    val lab = if (dark) darkLabColors() else lightLabColors()
    CompositionLocalProvider(LocalLabColors provides lab) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = LabTypography,
            content = content,
        )
    }
}
