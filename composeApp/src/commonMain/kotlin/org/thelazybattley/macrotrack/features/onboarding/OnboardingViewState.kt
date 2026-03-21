package org.thelazybattley.macrotrack.features.onboarding

import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserGender
import org.thelazybattley.macrotrack.features.onboarding.ui.OnboardingStep

data class OnboardingViewState(
    val selectedGoal: Goal? = null,
    val selectedActivityLevel: ActivityLevel? = null,
    val selectedGender: UserGender? = null,
    val currentStep: OnboardingStep = OnboardingStep.GOAL_AND_STATS,
)
