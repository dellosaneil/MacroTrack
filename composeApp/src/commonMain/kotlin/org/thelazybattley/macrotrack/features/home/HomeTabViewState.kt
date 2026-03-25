package org.thelazybattley.macrotrack.features.home

import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.UserDetails

data class HomeTabViewState(
    val userDetails: UserDetails? = null,
    val macroGoals: MacroGoals? = null,
)
