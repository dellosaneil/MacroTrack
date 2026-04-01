package org.thelazybattley.macrotrack.domain.usecase.foodlog

import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository

class InsertFoodLogUseCase(
    private val repository: FoodLogRepository
) {

    suspend operator fun invoke(
        foodName: String,
        mealType: MealType,
        calories: Int,
        fat: Double,
        carbs: Double,
        protein: Double,
        weight: Double,
        dominantMacro: MacroType
    ) = repository.insertFoodLog(
        foodName = foodName,
        mealType = mealType,
        calories = calories,
        fat = fat,
        carbs = carbs,
        protein = protein,
        weight = weight,
        dominantMacro = dominantMacro
    )
}
