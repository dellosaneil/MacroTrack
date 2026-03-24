package org.thelazybattley.macrotrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

data class MacroTrackTypography(
    val bold11: TextStyle,
    val bold12: TextStyle,
    val bold13: TextStyle,
    val bold15: TextStyle,
    val bold16: TextStyle,
    val bold24: TextStyle,
    val bold36: TextStyle,

    val regular8: TextStyle,
    val regular10: TextStyle,
    val regular11: TextStyle,
    val regular13: TextStyle,

    val medium13: TextStyle
)

val lightMacroTrackColors = MacroTrackColors(
    white = White,
    blue = Blue,
    lightBlue = LightBlue,
    black = Black,
    gray = Gray,
    green = Green,
    orange = Orange,
    whiteSmoke = WhiteSmoke,
    lightGray = LightGray,
    skyBlue = SkyBlue,
    babyBlue = BabyBlue,
    charcoalGray = CharcoalGray,
    mediumGray = MediumGray,
    offWhite = OffWhite,
    deepBlue = DeepBlue,
    royalBlue = RoyalBlue,
    brightBlue = BrightBlue,
    deepBlueSplash = DeepBlue,
    whiteSplash = White
)

val darkMacroTrackColors = MacroTrackColors(
    white = DarkWhite,
    blue = DarkBlue,
    lightBlue = DarkLightBlue,
    black = DarkBlack,
    gray = DarkGray,
    green = DarkGreen,
    orange = DarkOrange,
    whiteSmoke = DarkWhiteSmoke,
    lightGray = DarkLightGray,
    skyBlue = DarkSkyBlue,
    babyBlue = DarkBabyBlue,
    charcoalGray = DarkCharcoalGray,
    mediumGray = DarkMediumGray,
    offWhite = DarkOffWhite,
    deepBlue = DarkDeepBlue,
    royalBlue = DarkRoyalBlue,
    brightBlue = DarkBrightBlue,
    deepBlueSplash = DarkDeepBlueSplash,
    whiteSplash = DarkBlack
)


val LocalMacroTrackColors = staticCompositionLocalOf { lightMacroTrackColors }

val LocalMacroTrackTypography = staticCompositionLocalOf { getMacroTrackTypography() }


object MacroTrackTheme {
    val colors: MacroTrackColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMacroTrackColors.current

    val typography: MacroTrackTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalMacroTrackTypography.current
}

@Composable
fun MacroTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) {
        darkMacroTrackColors
    } else {
        lightMacroTrackColors
    }

    CompositionLocalProvider(
        LocalMacroTrackColors provides colors,
        LocalMacroTrackTypography provides getMacroTrackTypography(),
        content = content,
    )
}
