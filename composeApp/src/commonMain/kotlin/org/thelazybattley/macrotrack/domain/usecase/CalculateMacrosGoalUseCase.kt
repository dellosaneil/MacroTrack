package org.thelazybattley.macrotrack.domain.usecase

import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.MacroBasis
import org.thelazybattley.macrotrack.domain.model.MacroTargets
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.calorieAdjust
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.domain.model.carbsFor

class CalculateMacrosGoalUseCase(
    private val calculateTDEEUseCase: CalculateTDEEUseCase
) {
    /**
     * Protein and fat come from [MacroTargets.basisWeight]; carbs get the calories left over and never go below
     * 0 g.
     */
    operator fun invoke(
        height: Double,
        weight: Double,
        age: Int,
        gender: UserSex,
        activityLevel: ActivityLevel,
        goal: Goal,
        goalPace: Double,
        /** A burn measured by a weekly check-in replaces the formula's estimate. */
        measuredMaintenance: Int? = null,
        targetWeight: Double? = null,
        macroBasis: MacroBasis = MacroBasis.TARGET_WEIGHT,
        bodyFat: Double? = null
    ): MacroGoals {
        val bmr = measuredMaintenance ?: calculateTDEEUseCase(
            height = height,
            weight = weight,
            age = age,
            gender = gender,
            activityLevel = activityLevel
        )
        val basisWeight = MacroTargets.basisWeight(
            weight = weight,
            height = height,
            targetWeight = targetWeight,
            basis = macroBasis,
            bodyFat = bodyFat
        )
        val protein = MacroTargets.proteinGrams(basisWeight = basisWeight)
        val fat = MacroTargets.fatGrams(basisWeight = basisWeight)
        val calories = bmr + goal.calorieAdjust(paceKgPerMonth = goalPace)
        return MacroGoals(
            calories = calories,
            protein = protein,
            carbs = carbsFor(calories = calories, protein = protein, fat = fat),
            fat = fat
        )
    }

    /** The user's targets today: their custom ones when set, else calculated from their details. */
    operator fun invoke(userDetails: UserDetails): MacroGoals =
        userDetails.customMacroGoals ?: calculated(userDetails = userDetails)

    /** Calculated targets for [userDetails], with [goal], [goalPace] and [weight] in place of theirs when given. */
    fun calculated(
        userDetails: UserDetails,
        goal: Goal = userDetails.goal,
        goalPace: Double = userDetails.goalPace,
        weight: Double = userDetails.weight,
        macroBasis: MacroBasis = userDetails.macroBasis,
        bodyFat: Double? = userDetails.bodyFat
    ): MacroGoals = invoke(
        height = userDetails.height,
        weight = weight,
        age = userDetails.age,
        gender = userDetails.sex,
        activityLevel = userDetails.activityLevel,
        goal = goal,
        goalPace = goalPace,
        measuredMaintenance = userDetails.measuredMaintenance,
        targetWeight = userDetails.targetWeight,
        macroBasis = macroBasis,
        bodyFat = bodyFat
    )
}
