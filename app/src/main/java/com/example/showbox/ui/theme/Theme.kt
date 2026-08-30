package com.example.showbox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * One fixed dark scheme: white text on near-black, with a single yellow for
 * accents. The app ignores the system light/dark setting and Material You —
 * the look is the point.
 */
private val ShowboxColors = darkColorScheme(
    primary = Yellow,
    onPrimary = OnYellow,
    // A translucent wash of the same yellow, so highlights add no second tone.
    primaryContainer = Yellow.copy(alpha = 0.16f),
    onPrimaryContainer = Yellow,

    secondary = TextWhite,
    onSecondary = Ink,
    secondaryContainer = InkVariant,
    onSecondaryContainer = TextWhite,

    tertiary = TextMuted,
    onTertiary = Ink,
    tertiaryContainer = InkVariant,
    onTertiaryContainer = TextWhite,

    background = Ink,
    onBackground = TextWhite,
    surface = Ink,
    onSurface = TextWhite,
    surfaceVariant = InkVariant,
    onSurfaceVariant = TextMuted,
    surfaceContainer = InkRaised,
    surfaceContainerHigh = InkVariant,

    outline = InkOutline,
    outlineVariant = InkOutline,

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
