package org.thelazybattley.macrotrack.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color


// ─────────────────────────────────────────────
// Light Mode — Blue Primary
// ─────────────────────────────────────────────

val LightPrimary = Color(0xFF1565C0)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFBBDEFB)
val LightOnPrimaryContainer = Color(0xFF0D47A1)

val LightSecondary = Color(0xFF146C2E)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFC3EFCF)
val LightOnSecondaryContainer = Color(0xFF002109)

val LightTertiary = Color(0xFF7E5700)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFFFDEA8)
val LightOnTertiaryContainer = Color(0xFF281800)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

val LightBackground = Color(0xFFFAFCFF)
val LightOnBackground = Color(0xFF1C1B1F)

val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1C1B1F)
val LightSurfaceVariant = Color(0xFFF2F5FA)
val LightOnSurfaceVariant = Color(0xFF49454F)

val LightOutline = Color(0xFF79747E)
val LightOutlineVariant = Color(0xFFCAC4D0)

val LightInverseSurface = Color(0xFF313033)
val LightInverseOnSurface = Color(0xFFF4EFF4)
val LightInversePrimary = Color(0xFF9ECAFF)

val LightScrim = Color(0xFF000000)

// App-specific macro colors (light)
val LightProtein = Color(0xFF1565C0)
val LightCarbs = Color(0xFF1D9E75)
val LightFat = Color(0xFFEF9F27)
val LightCalorieRing = Color(0xFF378ADD)

// ─────────────────────────────────────────────
// Dark Mode — Purple Primary (Material You)
// ─────────────────────────────────────────────

val DarkPrimary = Color(0xFFD0BCFF)
val DarkOnPrimary = Color(0xFF381E72)
val DarkPrimaryContainer = Color(0xFF4A4458)
val DarkOnPrimaryContainer = Color(0xFFEADDFF)

val DarkSecondary = Color(0xFF6DD58C)
val DarkOnSecondary = Color(0xFF003919)
val DarkSecondaryContainer = Color(0xFF1A3323)
val DarkOnSecondaryContainer = Color(0xFFC3EFCF)

val DarkTertiary = Color(0xFFA8C7FA)
val DarkOnTertiary = Color(0xFF003062)
val DarkTertiaryContainer = Color(0xFF00468B)
val DarkOnTertiaryContainer = Color(0xFFD6E3FF)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

val DarkBackground = Color(0xFF1C1B1F)
val DarkOnBackground = Color(0xFFE6E1E5)

val DarkSurface = Color(0xFF2B2930)
val DarkOnSurface = Color(0xFFE6E1E5)
val DarkSurfaceVariant = Color(0xFF49454F)
val DarkOnSurfaceVariant = Color(0xFFCAC4D0)

val DarkOutline = Color(0xFF938F99)
val DarkOutlineVariant = Color(0xFF49454F)

val DarkInverseSurface = Color(0xFFE6E1E5)
val DarkInverseOnSurface = Color(0xFF313033)
val DarkInversePrimary = Color(0xFF1565C0)

val DarkScrim = Color(0xFF000000)

// App-specific macro colors (dark)
val DarkProtein = Color(0xFFD0BCFF)
val DarkCarbs = Color(0xFF6DD58C)
val DarkFat = Color(0xFFFFB4AB)
val DarkCalorieRing = Color(0xFFA8C7FA)

// ─────────────────────────────────────────────
// Color Schemes
// ─────────────────────────────────────────────

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,

    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,

    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,

    background = DarkBackground,
    onBackground = DarkOnBackground,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,

    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,

    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary,

    scrim = DarkScrim,
)

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,

    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,

    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,

    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,

    background = LightBackground,
    onBackground = LightOnBackground,

    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,

    outline = LightOutline,
    outlineVariant = LightOutlineVariant,

    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,

    scrim = LightScrim,
)
