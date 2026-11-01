package org.thelazybattley.macrotrack.widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider

/** The colours a widget is drawn in. Each has a light and a dark value, which the phone picks between. */
internal data class WidgetPalette(
    val background: ColorProvider,
    val ink: ColorProvider,
    val muted: ColorProvider,
    val accent: ColorProvider,
    val onAccent: ColorProvider,
    val tonal: ColorProvider,
    val tonalInk: ColorProvider,
    val track: ColorProvider,
    val protein: ColorProvider,
    val carbs: ColorProvider,
    val fat: ColorProvider,
    val water: ColorProvider,
    val waterSoft: ColorProvider,
    val warm: ColorProvider
)

/** What a widget's colours follow: the phone's wallpaper colours, or the app's own greens. */
internal enum class WidgetColours { WALLPAPER, APP }

private fun pair(day: Long, night: Long) = ColorProvider(day = Color(day), night = Color(night))

/** The app's own colours, as on the Today screen. */
private val appPalette = WidgetPalette(
    background = pair(0xFFFFFFFF, 0xFF1C1D20),
    ink = pair(0xFF15171A, 0xFFEEEDE8),
    muted = pair(0xFF6B6E75, 0xFFA3A6AD),
    accent = pair(0xFF0E6E52, 0xFF3FBF8F),
    onAccent = pair(0xFFFFFFFF, 0xFF08241A),
    tonal = pair(0xFFDDEFE7, 0xFF15322A),
    tonalInk = pair(0xFF0E6E52, 0xFF7FD8B5),
    track = pair(0xFFECEAE3, 0xFF2E3136),
    protein = pair(0xFF2A5BD7, 0xFF7AA2F7),
    carbs = pair(0xFFE0A12B, 0xFFF0B44C),
    fat = pair(0xFFD4603F, 0xFFF08A6C),
    water = pair(0xFF2468B8, 0xFF6FAFEF),
    waterSoft = pair(0xFFDCEAF8, 0xFF16283B),
    warm = pair(0xFFB07A12, 0xFFF0B44C)
)

/** Muted greens close to a typical wallpaper theme, for phones before Android 12 that have no wallpaper colours. */
private val wallpaperFallback = WidgetPalette(
    background = pair(0xFFE3EDE6, 0xFF1E2622),
    ink = pair(0xFF17201B, 0xFFDDE5DF),
    muted = pair(0xFF4F5B54, 0xFFA6B2AB),
    accent = pair(0xFF2D6A52, 0xFF94D3B6),
    onAccent = pair(0xFFFFFFFF, 0xFF00382A),
    tonal = pair(0xFFC4E8D6, 0xFF2F4A3E),
    tonalInk = pair(0xFF0F2A1F, 0xFFC9EBD9),
    track = pair(0xFFC9D7CE, 0xFF36413B),
    protein = pair(0xFF3D5BA9, 0xFFAFC2FF),
    carbs = pair(0xFF9A6B14, 0xFFF1C27A),
    fat = pair(0xFFA3513A, 0xFFF2A48C),
    water = pair(0xFF2F638F, 0xFF9CCBF3),
    waterSoft = pair(0xFFCFE5F7, 0xFF1F3443),
    warm = pair(0xFF8A5A0B, 0xFFF1C27A)
)

/** Wallpaper colours are only on Android 12 and later; before that a widget always uses [WidgetColours.APP]'s look. */
internal val hasWallpaperColours: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** The palette for [colours]. Wallpaper colours take the phone's theme and keep the macro colours readable on it. */
@Composable
internal fun widgetPalette(colours: WidgetColours): WidgetPalette {
    if (colours == WidgetColours.APP) return appPalette
    if (!hasWallpaperColours) return wallpaperFallback
    val theme = GlanceTheme.colors
    return wallpaperFallback.copy(
        background = theme.widgetBackground,
        ink = theme.onSurface,
        muted = theme.onSurfaceVariant,
        accent = theme.primary,
        onAccent = theme.onPrimary,
        tonal = theme.secondaryContainer,
        tonalInk = theme.onSecondaryContainer,
        track = theme.surfaceVariant
    )
}
