package org.thelazybattley.macrotrack.features.profile.nutritiongoals

import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.GoalPeriod
import org.thelazybattley.macrotrack.domain.model.UserDetails

data class NutritionGoalsViewState(
    val userDetails: UserDetails? = null,
    val selectedGoal: Goal? = null,
    /** kg per month. */
    val goalPace: Double? = null,
    val maintenanceCalories: Int? = null,
    val macroGoals: MacroGoals? = null,
    /** Newest first, ending with today's goal. */
    val history: List<GoalPeriod> = emptyList(),
    val isSaved: Boolean = false
) {
    val canSave: Boolean
        get() = userDetails != null && selectedGoal != null &&
            (selectedGoal != userDetails.goal || (selectedGoal != Goal.MAINTAIN_WEIGHT && goalPace != userDetails.goalPace))
}
