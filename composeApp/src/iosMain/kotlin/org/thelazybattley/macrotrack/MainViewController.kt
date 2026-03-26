package org.thelazybattley.macrotrack

import androidx.compose.ui.window.ComposeUIViewController
import org.thelazybattley.macrotrack.core.di.initKoin
import org.thelazybattley.macrotrack.features.navigation.SplashScreenNavigationNavigation

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { SplashScreenNavigationNavigation() }
