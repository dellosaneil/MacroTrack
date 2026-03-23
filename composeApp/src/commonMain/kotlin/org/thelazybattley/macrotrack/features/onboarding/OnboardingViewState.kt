package org.thelazybattley.macrotrack.features.onboarding

import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserGender
import org.thelazybattley.macrotrack.features.onboarding.ui.OnboardingStep

data class OnboardingViewState(
    val selectedGoal: Goal? = null,
    val selectedActivityLevel: ActivityLevel? = null,
    val selectedGender: UserGender? = null,
    val currentStep: OnboardingStep = OnboardingStep.GOAL_AND_STATS,
    val age: Int = 0,
    val height: Double = 0.0,
    val weight: Double = 0.0,
    val macroGoals: MacroGoals? = null,
    val isFinished: Boolean = false
)
