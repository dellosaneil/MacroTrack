package org.thelazybattley.macrotrack.domain.usecase

import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserGender
import kotlin.math.roundToInt

class CalculateMacrosGoalUseCase(
    private val calculateTDEEUseCase: CalculateTDEEUseCase
) {
    operator fun invoke(
        height: Double,
        weight: Double,
        age: Int,
        gender: UserGender,
        activityLevel: ActivityLevel,
        goal: Goal
    ): MacroGoals {
        val bmr = calculateTDEEUseCase(
            height = height,
            weight = weight,
            age = age,
            gender = gender,
            activityLevel = activityLevel
        )
        val protein = (weight * 2).roundToInt()
        val fat = (weight * 0.7).roundToInt()
        val calories = bmr + goal.calorieAdjust
        val carbs = (calories - (protein * 4) - (fat * 9)) / 4
        return MacroGoals(
            calories =calories,
            protein = protein,
            carbs = carbs,
            fat = fat
        )
    }
}
