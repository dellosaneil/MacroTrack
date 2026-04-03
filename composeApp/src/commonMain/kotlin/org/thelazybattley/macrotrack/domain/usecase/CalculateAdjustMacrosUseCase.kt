package org.thelazybattley.macrotrack.domain.usecase

import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros

class CalculateAdjustMacrosUseCase {

    operator fun invoke(
        portionSize: Double,
        originalFood: Food
    ): FoodMacros {
        val originalWeight = originalFood.weight
        val weightQuotient = portionSize / originalWeight
        return FoodMacros(
            calories = (originalFood.macros.calories * weightQuotient).toInt(),
            protein = originalFood.macros.protein * weightQuotient,
            fat = originalFood.macros.fat * weightQuotient,
            carbs = originalFood.macros.carbs * weightQuotient
        )
    }
}

