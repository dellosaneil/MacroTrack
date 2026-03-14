package org.thelazybattley.macrotrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf


val LocalColors = staticCompositionLocalOf<MyColors> {
    error("No MyThemeColors provided")
}


@Composable
fun MyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        myColorsDark
    } else {
        myColorsDark
    }
    CompositionLocalProvider(
        value = LocalColors provides colors,
        content = content
    )
}
