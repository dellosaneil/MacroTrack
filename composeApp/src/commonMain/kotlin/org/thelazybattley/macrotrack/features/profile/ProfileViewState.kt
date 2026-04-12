package org.thelazybattley.macrotrack.features.profile

import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.features.profile.ui.BMI

data class ProfileViewState(
    val macroGoals: MacroGoals? = null,
    val currentGoal: Goal? = null,
    val bmiValue: Double? = null,
    val bmiCategory: BMI? = null
)
