package org.thelazybattley.macrotrack

import androidx.compose.ui.window.ComposeUIViewController
import org.thelazybattley.macrotrack.core.di.initKoin
import org.thelazybattley.macrotrack.ui.App

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { App() }
