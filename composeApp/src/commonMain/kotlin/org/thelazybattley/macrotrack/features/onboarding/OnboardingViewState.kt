package org.thelazybattley.macrotrack.features.onboarding

import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserGender

data class OnboardingViewState(
    val selectedGoal: Goal? = null,
    val selectedActivityLevel: ActivityLevel? = null,
    val selectedGender: UserGender? = null,
    val currentIndex: Int = 0,
)
