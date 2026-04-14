package org.thelazybattley.macrotrack.features.profile

import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.features.profile.ui.BMI

data class ProfileViewState(
    val macroGoals: MacroGoals? = null,
    val currentGoal: Goal? = null,
    val profileBMI: ProfileBMI = ProfileBMI(),
    val weightInput: String = "0.0",
    val currentWeight: String = "0.0"
)

data class ProfileBMI(
    val value: Double = 0.0,
    val category: BMI = BMI.NORMAL,
    val progress: Double = 0.0
)
