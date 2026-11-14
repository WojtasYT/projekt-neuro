package lab.neurotrino.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import lab.neurotrino.app.R

// Bundled OFL families (static instances, full Polish latin-ext coverage):
//  Serif  — Source Serif 4 (opsz 14 for text, opsz 60 for display)
//  Sans   — Inter
//  Mono   — JetBrains Mono
val LabSerif = FontFamily(
    Font(R.font.serif_regular, FontWeight.Normal),
    Font(R.font.serif_medium, FontWeight.Medium),
    Font(R.font.serif_semibold, FontWeight.SemiBold),
    Font(R.font.serif_bold, FontWeight.Bold),
)
val LabSerifDisplay = FontFamily(
    Font(R.font.serif_display_semibold, FontWeight.SemiBold),
    Font(R.font.serif_display_bold, FontWeight.Bold),
)
val LabSans = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)
val LabMono = FontFamily(
    Font(R.font.mono_regular, FontWeight.Normal),
    Font(R.font.mono_medium, FontWeight.Medium),
    Font(R.font.mono_semibold, FontWeight.SemiBold),
)

val LabTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = LabSerifDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 31.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.2).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = LabSerifDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 33.sp,
        letterSpacing = (-0.1).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = LabSerifDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.1).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = LabSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 21.sp,
        lineHeight = 28.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = LabSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 25.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = LabSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = LabSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.5.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = LabSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.5.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = LabSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = LabSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = LabSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = LabMono,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 1.5.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = LabMono,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = LabMono,
        fontWeight = FontWeight.Normal,
        fontSize = 9.5.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.8.sp,
    ),
)
