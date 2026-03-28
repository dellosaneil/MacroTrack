package org.thelazybattley.macrotrack.domain.usecase

import kotlin.math.roundToInt

class CalculateCaloriesFromMacrosUseCase {

    operator fun invoke(protein: Double, carbs: Double, fat: Double) : Int {
        val proteinCalories = protein * 4
        val carbsCalories = carbs * 4
        val fatCalories = fat * 9
        return (proteinCalories + carbsCalories + fatCalories).roundToInt()
    }

}
