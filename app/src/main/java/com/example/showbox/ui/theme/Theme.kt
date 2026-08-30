package com.example.showbox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * One fixed dark scheme with yellow text and controls. The app does not follow
 * the system light/dark setting and does not use Material You wallpaper
 * colours — the yellow-on-black look is the point.
 */
private val ShowboxColors = darkColorScheme(
    primary = Yellow,
    onPrimary = OnYellow,
    primaryContainer = YellowDeep,
    onPrimaryContainer = YellowBright,

    secondary = YellowBright,
    onSecondary = OnYellow,
    secondaryContainer = YellowDeep,
    onSecondaryContainer = YellowBright,

    tertiary = YellowDim,
    onTertiary = OnYellow,
    tertiaryContainer = YellowDeep,
    onTertiaryContainer = YellowBright,

    background = Ink,
    onBackground = YellowBright,
    surface = Ink,
    onSurface = YellowBright,
    surfaceVariant = InkVariant,
    onSurfaceVariant = YellowDim,
    surfaceContainer = InkRaised,
    surfaceContainerHigh = InkVariant,

    outline = InkOutline,
    outlineVariant = YellowDeep,

    error = Danger,
    onError = OnDanger,
    errorContainer = Danger,
    onErrorContainer = OnDanger,
)

private val ShowboxTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
)

@Composable
fun ShowboxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ShowboxColors,
        typography = ShowboxTypography,
        content = content,
    )
}
