package lab.neurotrino.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Lab palette ─────────────────────────────────────────────────────────────
// Deep graphite foundation with tonal layering. Accents are luminous but
// restrained: cyan (signal), indigo (structure), amber (attention) — never
// neon, never decorative noise.

val GraphiteVoid = Color(0xFF07090D)
val Graphite = Color(0xFF0C1016)
val GraphiteElevated = Color(0xFF131924)
val GraphitePanel = Color(0xFF1A2230)
val GraphiteWell = Color(0xFF0E141D)
val GraphiteLine = Color(0xFF232D3D)

val Cyan = Color(0xFF63E2CE)
val CyanDeep = Color(0xFF2E8F82)
val Indigo = Color(0xFF8FA8FF)
val IndigoDeep = Color(0xFF44549E)
val Amber = Color(0xFFE4B56A)
val AmberDeep = Color(0xFF8A5F28)

val Ivory = Color(0xFFEDE7D9)
val IvoryMuted = Color(0xFFB9B2A2)
val IvoryFaint = Color(0xFF8B8577)

val Danger = Color(0xFFE08A7A)
val Ok = Color(0xFF8BD9A9)
val Warn = Color(0xFFE4B56A)

// Light scheme counterparts
val LightBg = Color(0xFFF2EEE2)
val LightSurface = Color(0xFFFBF8EF)
val LightElevated = Color(0xFFFFFFFF)
val LightWell = Color(0xFFECE7D8)
val LightPanel = Color(0xFFE6E1D2)
val LightLine = Color(0xFFD8D2C2)
val LightText = Color(0xFF211D15)
val LightMuted = Color(0xFF5F594D)
val LightFaint = Color(0xFF8B8577)
val LightCyan = Color(0xFF177262)
val LightIndigo = Color(0xFF3F54A6)
val LightAmber = Color(0xFF7A5518)
val LightDanger = Color(0xFFB4543F)

/**
 * Semantic lab tokens beyond the Material color scheme — used by the custom
 * components (backdrop, panels, badges) so both dark and light themes stay
 * deliberate instead of inheriting hard-coded dark gradients.
 */
@Immutable
data class LabColors(
    val void: Color,
    val backdropTop: Color,
    val backdropBottom: Color,
    val dotGrid: Color,
    val fieldLine: Color,
    val ring: Color,
    val glowCyan: Color,
    val glowIndigo: Color,
    val glowAmber: Color,
    val panelTop: Color,
    val panelBottom: Color,
    val wellTop: Color,
    val wellBottom: Color,
    val panelBorder: Color,
    val panelBorderStrong: Color,
    val accentGood: Color,
    val accentWarn: Color,
    val accentBad: Color,
    val scrim: Color,
    val isDark: Boolean,
)

val LocalLabColors = compositionLocalOf { darkLabColors() }

fun darkLabColors() = LabColors(
    void = GraphiteVoid,
    backdropTop = Color(0xFF0B0F16),
    backdropBottom = Color(0xFF080B10),
    dotGrid = Color(0xFFFFFFFF).copy(alpha = 0.045f),
    fieldLine = Color(0xFFFFFFFF).copy(alpha = 0.028f),
    ring = Cyan.copy(alpha = 0.10f),
    glowCyan = Cyan.copy(alpha = 0.14f),
    glowIndigo = Indigo.copy(alpha = 0.12f),
    glowAmber = Amber.copy(alpha = 0.10f),
    panelTop = Color(0xFF171F2B).copy(alpha = 0.96f),
    panelBottom = Color(0xFF111823).copy(alpha = 0.92f),
    wellTop = Color(0xFF16202B).copy(alpha = 0.96f),
    wellBottom = Color(0xFF0D1219).copy(alpha = 0.96f),
    panelBorder = Color(0xFF2A3648),
    panelBorderStrong = Cyan.copy(alpha = 0.38f),
    accentGood = Ok,
    accentWarn = Amber,
    accentBad = Danger,
    scrim = Color(0xB307090D),
    isDark = true,
)

fun lightLabColors() = LabColors(
    void = LightBg,
    backdropTop = Color(0xFFF4F0E4),
    backdropBottom = Color(0xFFE9E4D5),
    dotGrid = Color(0xFF211D15).copy(alpha = 0.055f),
    fieldLine = Color(0xFF211D15).copy(alpha = 0.035f),
    ring = LightCyan.copy(alpha = 0.14f),
    glowCyan = LightCyan.copy(alpha = 0.10f),
    glowIndigo = LightIndigo.copy(alpha = 0.09f),
    glowAmber = LightAmber.copy(alpha = 0.09f),
    panelTop = Color(0xFFFDFCF7).copy(alpha = 0.97f),
    panelBottom = Color(0xFFF2EEE1).copy(alpha = 0.97f),
    wellTop = Color(0xFFFDFCF7),
    wellBottom = Color(0xFFEFEAE0),
    panelBorder = Color(0xFFD9D3C3),
    panelBorderStrong = LightCyan.copy(alpha = 0.45f),
    accentGood = Color(0xFF1E7A45),
    accentWarn = LightAmber,
    accentBad = LightDanger,
    scrim = Color(0x99211D15),
    isDark = false,
)
