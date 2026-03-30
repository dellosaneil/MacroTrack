package org.thelazybattley.macrotrack.domain.usecase

import org.thelazybattley.macrotrack.domain.model.MacroType
import kotlin.math.roundToInt

class CalculateCaloriesFromMacrosUseCase {

    operator fun invoke(protein: Double, carbs: Double, fat: Double) : Int {
        val proteinCalories = protein * MacroType.PROTEIN.calories
        val carbsCalories = carbs * MacroType.CARBS.calories
        val fatCalories = fat * MacroType.FAT.calories
        return (proteinCalories + carbsCalories + fatCalories).roundToInt()
    }

}
