package com.nightcheck.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val PurplePrimary = Color(0xFF7C6AF5)

val LocalThemeToggle = compositionLocalOf<() -> Unit> { {} }
val LocalIsDarkTheme = compositionLocalOf<Boolean> { false }

private val md_theme_dark_primary            = PurplePrimary
private val md_theme_dark_onPrimary          = Color(0xFFFFFFFF)
private val md_theme_dark_primaryContainer   = Color(0xFF5D4EC4)
private val md_theme_dark_onPrimaryContainer = Color(0xFFFFFFFF)
private val md_theme_dark_background         = Color(0xFF0E0E12)
private val md_theme_dark_onBackground       = Color(0xFFF0EDE8)
private val md_theme_dark_surface            = Color(0xFF141418)
private val md_theme_dark_onSurface          = Color(0xFFF0EDE8)
private val md_theme_dark_surfaceVariant     = Color(0xFF1C1C24)
private val md_theme_dark_onSurfaceVariant   = Color(0xFFAAAAAA)
private val md_theme_dark_outline            = Color(0x26FFFFFF)
private val md_theme_dark_error              = Color(0xFFF2B8B8)
private val md_theme_dark_errorContainer     = Color(0xFF8C1D18)
private val md_theme_dark_onError            = Color(0xFF601410)
private val md_theme_dark_onErrorContainer   = Color(0xFFF9DEDC)

private val md_theme_light_primary            = PurplePrimary
private val md_theme_light_onPrimary          = Color(0xFFFFFFFF)
private val md_theme_light_primaryContainer   = Color(0xFFEADDFF)
private val md_theme_light_onPrimaryContainer = Color(0xFF21005D)
private val md_theme_light_background         = Color(0xFFF4F3EF)
private val md_theme_light_onBackground       = Color(0xFF1A1917)
private val md_theme_light_surface            = Color(0xFFFFFFFF)
private val md_theme_light_onSurface          = Color(0xFF1A1917)
private val md_theme_light_surfaceVariant     = Color(0xFFE7E0EC)
private val md_theme_light_onSurfaceVariant   = Color(0xFF666666)
private val md_theme_light_outline            = Color(0x26000000)
private val md_theme_light_error              = Color(0xFFB3261E)
private val md_theme_light_errorContainer     = Color(0xFFF9DEDC)
private val md_theme_light_onError            = Color(0xFFFFFFFF)
private val md_theme_light_onErrorContainer   = Color(0xFF410E0B)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer
)

@Composable
fun NightcheckTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    onThemeToggle: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalThemeToggle provides onThemeToggle
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NightcheckTypography,
            content = content
        )
    }
}