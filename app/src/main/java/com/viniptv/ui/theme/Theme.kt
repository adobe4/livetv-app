package com.viniptv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VinColorScheme = darkColorScheme(
    primary = VinColors.Accent,
    onPrimary = VinColors.TextPrimary,
    secondary = VinColors.AccentDim,
    tertiary = VinColors.AccentGlow,
    background = VinColors.Background,
    surface = VinColors.Surface,
    surfaceVariant = VinColors.SurfaceLight,
    onBackground = VinColors.TextPrimary,
    onSurface = VinColors.TextPrimary,
    onSurfaceVariant = VinColors.TextSecondary,
    outline = VinColors.Separator,
    outlineVariant = VinColors.CardBorder,
    error = VinColors.Live,
    onError = VinColors.TextPrimary
)

@Composable
fun VinIPTVTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VinColorScheme,
        typography = VinTypography,
        content = content
    )
}
