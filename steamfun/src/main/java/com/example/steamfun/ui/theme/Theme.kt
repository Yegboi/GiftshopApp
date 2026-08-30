package com.example.steamfun.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Steam's own dark blues, so a round feels like the store page it came from. */
val SteamNight = Color(0xFF171A21)
val SteamBlue = Color(0xFF1B2838)
val SteamSlate = Color(0xFF2A475E)
val SteamHighlight = Color(0xFF66C0F4)
val SteamText = Color(0xFFE6EDF3)
val SteamMuted = Color(0xFF8F98A0)
val SteamGreen = Color(0xFFA4D007)
val SteamRed = Color(0xFFE05C5C)

private val Colors = darkColorScheme(
    primary = SteamHighlight,
    onPrimary = SteamNight,
    primaryContainer = SteamSlate,
    onPrimaryContainer = SteamHighlight,

    secondary = SteamGreen,
    onSecondary = SteamNight,
    secondaryContainer = SteamSlate,
    onSecondaryContainer = SteamText,

    background = SteamNight,
    onBackground = SteamText,
    surface = SteamBlue,
    onSurface = SteamText,
    surfaceVariant = SteamSlate,
    onSurfaceVariant = SteamMuted,
    surfaceContainer = SteamBlue,
    surfaceContainerHigh = SteamSlate,

    outline = SteamSlate,
    outlineVariant = SteamSlate,

    error = SteamRed,
    onError = SteamNight,
)

private val SteamTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
)

@Composable
fun SteamFunTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = SteamTypography, content = content)
}
