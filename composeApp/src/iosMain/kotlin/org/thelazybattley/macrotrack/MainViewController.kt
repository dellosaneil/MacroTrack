package org.thelazybattley.macrotrack

import androidx.compose.ui.window.ComposeUIViewController
import org.thelazybattley.macrotrack.core.di.initKoin
import org.thelazybattley.macrotrack.ui.MacroNavigation

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { MacroNavigation() }
