package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SophisticatedBlue,
    onPrimary = PaperWhite,
    primaryContainer = SophisticatedBlueSoft,
    onPrimaryContainer = PrimaryText,
    secondary = EmeraldGreen,
    onSecondary = PaperWhite,
    secondaryContainer = ForestSoft,
    onSecondaryContainer = PrimaryText,
    background = BackgroundColor,
    onBackground = PrimaryText,
    surface = SurfaceCard,
    onSurface = PrimaryText,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = SecondaryText,
    outline = BorderLight,
    outlineVariant = BorderSubtle
)

@Composable
fun CallMeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = CallMeTypography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CallMeTheme(darkTheme = false, content = content)
}
