package org.thelazybattley.macrotrack.features.profile

import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.Goal

data class ProfileViewState(
    val macroGoals: MacroGoals? = null,
    val currentGoal: Goal? = null
)
