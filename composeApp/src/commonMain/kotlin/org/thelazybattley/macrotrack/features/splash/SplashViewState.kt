package org.thelazybattley.macrotrack.features.splash

import org.thelazybattley.macrotrack.ui.navigation.MacroTrackDestination

data class SplashViewState(
    val destination: MacroTrackDestination? = null,
    val isLoading: Boolean = true
)
