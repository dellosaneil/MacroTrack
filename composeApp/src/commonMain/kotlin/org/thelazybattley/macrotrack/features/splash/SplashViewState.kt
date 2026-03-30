package org.thelazybattley.macrotrack.features.splash

import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

data class SplashViewState(
    val destination: AppDestinations.Root? = null,
    val isLoading: Boolean = true
)
