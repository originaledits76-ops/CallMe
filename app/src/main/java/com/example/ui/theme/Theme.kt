package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SolidBlack,
    onPrimary = PureWhite,
    primaryContainer = SoftGraySurface,
    onPrimaryContainer = SolidBlack,
    secondary = CharcoalBlack,
    onSecondary = PureWhite,
    secondaryContainer = SoftGraySurface,
    onSecondaryContainer = SolidBlack,
    tertiary = CharcoalBlack,
    onTertiary = PureWhite,
    background = PureWhite,
    onBackground = SolidBlack,
    surface = PureWhite,
    onSurface = SolidBlack,
    surfaceVariant = OffWhite,
    onSurfaceVariant = CharcoalBlack,
    outline = CardBorderGray,
    outlineVariant = SoftGraySurface
)

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = SolidBlack,
    primaryContainer = DarkSurface,
    onPrimaryContainer = PureWhite,
    secondary = PureWhite,
    onSecondary = SolidBlack,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = PureWhite,
    tertiary = PureWhite,
    onTertiary = SolidBlack,
    background = DarkBackground,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder,
    outlineVariant = DarkBorder
)

@Composable
fun CallMeTheme(
    darkTheme: Boolean = false, // Default is false to strictly adhere to user's "white being the background on black text" preference
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CallMeTypography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CallMeTheme(darkTheme = darkTheme, content = content)
}
