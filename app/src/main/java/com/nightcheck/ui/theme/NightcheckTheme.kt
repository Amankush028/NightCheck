package com.nightcheck.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
//  SINGLE SOURCE OF TRUTH — all colors are defined here ONLY.
//  Every screen/component must use MaterialTheme.colorScheme.* or the
//  NightcheckColors extensions below. No hardcoded Color(...) anywhere else.
// ─────────────────────────────────────────────────────────────────────────────

// ── Dark palette ──────────────────────────────────────────────────────────────
// Source of truth: HomeScreen dark aesthetic
private object Dark {
    val Background     = Color(0xFF000000) // Pure black — main background
    val Surface        = Color(0xFF111111) // Task cards, note cards
    val SurfaceVariant = Color(0xFF1C1C24) // Input fields, inner cards, toggles
    val SurfaceHigh    = Color(0xFF262438) // Icon backgrounds, elevated surfaces
    val Overlay        = Color(0xFF322F44) // Selected toggle pill, chip backgrounds

    val Primary        = Color(0xFFA78BFA) // Purple — main accent
    val PrimaryDim     = Color(0xFF7C6AF5) // Deeper purple — buttons, FAB
    val PrimaryMuted   = Color(0xFF5D4EC4) // Primary container

    val OnBackground   = Color(0xFFFFFFFF)
    val OnSurface      = Color(0xFFFFFFFF)
    val OnSurfaceVar   = Color(0xFFAAAAAA)
    val OnPrimary      = Color(0xFFFFFFFF)
    val Outline        = Color(0x26FFFFFF) // ~15% white border

    val Error          = Color(0xFFF2B8B8)
    val ErrorContainer = Color(0xFF8C1D18)
    val OnError        = Color(0xFF601410)
    val OnErrorCont    = Color(0xFFF9DEDC)

    // Semantic extras exposed via NightcheckColors
    val ReviewBg       = Color(0xFF1A1235)  // EndOfDayReview wallpaper tint
    val ReviewSheet    = Color(0xEB161226)  // Review bottom sheet bg
    val TextMuted      = Color(0x59FFFFFF)  // ~35% white — secondary text
    val TextFaint      = Color(0x33FFFFFF)  // ~20% white — placeholder / label
    val BorderMuted    = Color(0x1AFFFFFF)  // ~10% white — dividers
    val Destructive    = Color(0xFFE57373)  // Delete / destructive action
}

// ── Light palette ─────────────────────────────────────────────────────────────
// Warm cream/ivory with amber-orange replacing purple for a complementary feel
private object Light {
    val Background     = Color(0xFFFEFAF3) // Warm ivory — main background
    val Surface        = Color(0xFFFFFFFF) // Cards on cream
    val SurfaceVariant = Color(0xFFF5EFE3) // Input fields, inner cards
    val SurfaceHigh    = Color(0xFFEDE4D3) // Elevated surfaces, icon bg
    val Overlay        = Color(0xFFE8DCC8) // Selected toggle pill

    val Primary        = Color(0xFFD97706) // Amber-orange — main accent
    val PrimaryDim     = Color(0xFFB45309) // Deeper amber — buttons, FAB
    val PrimaryMuted   = Color(0xFFFDE68A) // Primary container (soft yellow)

    val OnBackground   = Color(0xFF1C1006) // Near-black warm
    val OnSurface      = Color(0xFF1C1006)
    val OnSurfaceVar   = Color(0xFF6B5C3E) // Warm brown-grey
    val OnPrimary      = Color(0xFFFFFFFF)
    val Outline        = Color(0x26000000) // ~15% black border

    val Error          = Color(0xFFB3261E)
    val ErrorContainer = Color(0xFFF9DEDC)
    val OnError        = Color(0xFFFFFFFF)
    val OnErrorCont    = Color(0xFF410E0B)

    // Semantic extras
    val ReviewBg       = Color(0xFFF5EAD3)  // EndOfDayReview wallpaper tint
    val ReviewSheet    = Color(0xF0FFFBF2)  // Review bottom sheet bg
    val TextMuted      = Color(0x996B5C3E)  // ~60% warm brown
    val TextFaint      = Color(0x4D6B5C3E)  // ~30% warm brown
    val BorderMuted    = Color(0x1A000000)  // ~10% black
    val Destructive    = Color(0xFFE57373)
}

// ─────────────────────────────────────────────────────────────────────────────
//  NightcheckColors — semantic extras not covered by Material3 ColorScheme.
//  Access via LocalNightcheckColors.current in any Composable.
// ─────────────────────────────────────────────────────────────────────────────

data class NightcheckColors(
    /** Deeper/dimmer shade of primary for buttons & FABs */
    val primaryDim: Color,
    /** Muted primary for overlay / selected-pill backgrounds */
    val overlay: Color,
    /** Elevated icon-background surface */
    val surfaceHigh: Color,
    /** Wallpaper tint for EndOfDayReview activity */
    val reviewBg: Color,
    /** Bottom-sheet background in EndOfDayReview */
    val reviewSheet: Color,
    /** ~35% opacity text — secondary labels */
    val textMuted: Color,
    /** ~20% opacity text — placeholders, faint labels */
    val textFaint: Color,
    /** Thin border / divider color */
    val borderMuted: Color,
    /** Destructive action color (delete, error buttons) */
    val destructive: Color,
)

val LocalNightcheckColors = compositionLocalOf {
    NightcheckColors(
        primaryDim   = Dark.PrimaryDim,
        overlay      = Dark.Overlay,
        surfaceHigh  = Dark.SurfaceHigh,
        reviewBg     = Dark.ReviewBg,
        reviewSheet  = Dark.ReviewSheet,
        textMuted    = Dark.TextMuted,
        textFaint    = Dark.TextFaint,
        borderMuted  = Dark.BorderMuted,
        destructive  = Dark.Destructive,
    )
}

val LocalThemeToggle  = compositionLocalOf<() -> Unit> { {} }
val LocalIsDarkTheme  = compositionLocalOf<Boolean> { true }

// ─────────────────────────────────────────────────────────────────────────────
//  Material3 ColorScheme instances
// ─────────────────────────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary             = Dark.Primary,
    onPrimary           = Dark.OnPrimary,
    primaryContainer    = Dark.PrimaryMuted,
    onPrimaryContainer  = Dark.OnPrimary,
    background          = Dark.Background,
    onBackground        = Dark.OnBackground,
    surface             = Dark.Surface,
    onSurface           = Dark.OnSurface,
    surfaceVariant      = Dark.SurfaceVariant,
    onSurfaceVariant    = Dark.OnSurfaceVar,
    outline             = Dark.Outline,
    error               = Dark.Error,
    errorContainer      = Dark.ErrorContainer,
    onError             = Dark.OnError,
    onErrorContainer    = Dark.OnErrorCont,
)

private val LightColorScheme = lightColorScheme(
    primary             = Light.Primary,
    onPrimary           = Light.OnPrimary,
    primaryContainer    = Light.PrimaryMuted,
    onPrimaryContainer  = Light.OnBackground,
    background          = Light.Background,
    onBackground        = Light.OnBackground,
    surface             = Light.Surface,
    onSurface           = Light.OnSurface,
    surfaceVariant      = Light.SurfaceVariant,
    onSurfaceVariant    = Light.OnSurfaceVar,
    outline             = Light.Outline,
    error               = Light.Error,
    errorContainer      = Light.ErrorContainer,
    onError             = Light.OnError,
    onErrorContainer    = Light.OnErrorCont,
)

private val DarkNightcheckColors = NightcheckColors(
    primaryDim  = Dark.PrimaryDim,
    overlay     = Dark.Overlay,
    surfaceHigh = Dark.SurfaceHigh,
    reviewBg    = Dark.ReviewBg,
    reviewSheet = Dark.ReviewSheet,
    textMuted   = Dark.TextMuted,
    textFaint   = Dark.TextFaint,
    borderMuted = Dark.BorderMuted,
    destructive = Dark.Destructive,
)

private val LightNightcheckColors = NightcheckColors(
    primaryDim  = Light.PrimaryDim,
    overlay     = Light.Overlay,
    surfaceHigh = Light.SurfaceHigh,
    reviewBg    = Light.ReviewBg,
    reviewSheet = Light.ReviewSheet,
    textMuted   = Light.TextMuted,
    textFaint   = Light.TextFaint,
    borderMuted = Light.BorderMuted,
    destructive = Light.Destructive,
)

// ─────────────────────────────────────────────────────────────────────────────
//  Theme entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NightcheckTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    onThemeToggle: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val colorScheme      = if (darkTheme) DarkColorScheme      else LightColorScheme
    val nightcheckColors = if (darkTheme) DarkNightcheckColors else LightNightcheckColors

    CompositionLocalProvider(
        LocalIsDarkTheme        provides darkTheme,
        LocalThemeToggle        provides onThemeToggle,
        LocalNightcheckColors   provides nightcheckColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = NightcheckTypography,
            content     = content
        )
    }
}