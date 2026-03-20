package org.thelazybattley.macrotrack.features.onboarding

import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserGender

interface OnboardingCallbacks {

    fun onGoalSelected(goal: Goal)

    fun onActivityLevelSelected(activityLevel: ActivityLevel)

    fun onGenderSelected(gender: UserGender)

}
