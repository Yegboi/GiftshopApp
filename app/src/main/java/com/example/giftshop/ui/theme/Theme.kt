package com.example.giftshop.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Berry40,
    onPrimary = Neutral99,
    primaryContainer = Berry90,
    onPrimaryContainer = Berry10,
    secondary = Gold40,
    onSecondary = Neutral99,
    secondaryContainer = Gold90,
    onSecondaryContainer = Gold10,
    tertiary = Sage40,
    onTertiary = Neutral99,
    tertiaryContainer = Sage90,
    onTertiaryContainer = Sage10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Neutral90,
    onSurfaceVariant = Neutral30,
)

private val DarkColors = darkColorScheme(
    primary = Berry80,
    onPrimary = Berry10,
    primaryContainer = Berry40,
    onPrimaryContainer = Berry90,
    secondary = Gold80,
    onSecondary = Gold10,
    secondaryContainer = Gold40,
    onSecondaryContainer = Gold90,
    tertiary = Sage80,
    onTertiary = Sage10,
    tertiaryContainer = Sage40,
    onTertiaryContainer = Sage90,
    background = Neutral10,
    onBackground = Neutral95,
    surface = Neutral10,
    onSurface = Neutral95,
    surfaceVariant = Neutral20,
    onSurfaceVariant = Neutral90,
)

@Composable
fun GiftshopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /** Material You wallpaper colours, available from Android 12. */
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GiftshopTypography,
        content = content,
    )
}
