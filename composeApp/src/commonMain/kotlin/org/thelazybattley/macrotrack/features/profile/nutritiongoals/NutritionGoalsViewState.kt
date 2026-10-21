package org.thelazybattley.macrotrack.features.profile.nutritiongoals

import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.GoalPeriod
import org.thelazybattley.macrotrack.domain.model.MacroBasis
import org.thelazybattley.macrotrack.domain.model.MacroTargets
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.UserDetails

data class NutritionGoalsViewState(
    val userDetails: UserDetails? = null,
    val selectedGoal: Goal? = null,
    /** kg per month. */
    val goalPace: Double? = null,
    val maintenanceCalories: Int? = null,
    val restingBurn: Int? = null,
    /** The calculated targets for the selected goal, pace and basis. */
    val macroGoals: MacroGoals? = null,
    val macroBasis: MacroBasis = MacroBasis.TARGET_WEIGHT,
    val bodyFat: Double? = null,
    /** The weight in kg protein and fat come from, for each basis; lean mass is null without a body fat. */
    val targetBasisWeight: Double? = null,
    val leanBasisWeight: Double? = null,
    /** The fastest loss pace in kg a month that leaves [MacroTargets.LOW_CARB_GRAMS] of carbs. */
    val fastestPaceKeepingCarbs: Double? = null,
    val isCustom: Boolean = false,
    /** The custom targets being edited; starts from the saved ones, or the calculated ones the first time. */
    val customMacroGoals: MacroGoals? = null,
    /** Newest first, ending with today's goal. */
    val history: List<GoalPeriod> = emptyList(),
    val isSaved: Boolean = false
) {
    private val isLoss: Boolean get() = selectedGoal == Goal.LOSE_WEIGHT

    /** Protein and fat alone need more than the calorie goal, so carbs are at 0 g. */
    val isCarbsAtZero: Boolean
        get() = isLoss && macroGoals != null &&
            macroGoals.protein * MacroType.PROTEIN.calories + macroGoals.fat * MacroType.FAT.calories > macroGoals.calories

    val isLowCarb: Boolean
        get() = isLoss && !isCarbsAtZero && macroGoals != null && macroGoals.carbs < MacroTargets.LOW_CARB_GRAMS

    val isBelowRestingBurn: Boolean
        get() = isLoss && macroGoals != null && restingBurn != null && macroGoals.calories < restingBurn

    val basisWeight: Double?
        get() = if (macroBasis == MacroBasis.LEAN_MASS) leanBasisWeight ?: targetBasisWeight else targetBasisWeight

    val isCustomValid: Boolean
        get() = customMacroGoals != null &&
            customMacroGoals.calories.toDouble() in MacroTargets.MIN_CUSTOM_CALORIES..MacroTargets.MAX_CUSTOM_CALORIES

    val canSave: Boolean
        get() {
            val userDetails = userDetails ?: return false
            if (isCustom) return isCustomValid && customMacroGoals != userDetails.customMacroGoals
            val goal = selectedGoal ?: return false
            return userDetails.customMacroGoals != null ||
                goal != userDetails.goal ||
                (goal != Goal.MAINTAIN_WEIGHT && goalPace != userDetails.goalPace) ||
                macroBasis != userDetails.macroBasis ||
                bodyFat != userDetails.bodyFat
        }
}
