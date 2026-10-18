package org.thelazybattley.macrotrack.features.onboarding

import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.GoalPace
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.features.onboarding.ui.OnboardingStep

data class OnboardingViewState(
    val selectedGoal: Goal? = null,
    /** kg per month. */
    val goalPace: Double = GoalPace.DEFAULT_KG_PER_MONTH,
    val selectedActivityLevel: ActivityLevel? = null,
    val selectedGender: UserSex? = null,
    val currentStep: OnboardingStep = OnboardingStep.GOAL_AND_STATS,
    val age: Int = 0,
    /** In cm, whichever unit it was typed in. */
    val height: Double = 0.0,
    /** In kg, whichever unit it was typed in. */
    val weight: Double = 0.0,
    val macroGoals: MacroGoals? = null,
    val maintenanceCalories: Int? = null,
    val isFinished: Boolean = false
) {
    val canContinue: Boolean
        get() = when (currentStep) {
            OnboardingStep.GOAL_AND_STATS ->
                age > 0 && height > 0 && weight > 0 && selectedGoal != null && selectedGender != null
            OnboardingStep.ACTIVITY_LEVEL -> selectedActivityLevel != null
            OnboardingStep.SUMMARY -> macroGoals != null
        }
}
