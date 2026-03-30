package org.thelazybattley.macrotrack.features.home

import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

data class HomeTabViewState(
    val userDetails: UserDetails? = null,
    val macroGoals: MacroGoals? = null,
    val onNavigation: AppDestinations.Root? = null,
    val isNavigationTriggered: Boolean = false
)
