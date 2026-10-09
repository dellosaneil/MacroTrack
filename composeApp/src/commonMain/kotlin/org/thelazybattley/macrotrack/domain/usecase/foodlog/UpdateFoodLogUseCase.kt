package org.thelazybattley.macrotrack.domain.usecase.foodlog

import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository

class UpdateFoodLogUseCase(private val repository: FoodLogRepository) {

    suspend operator fun invoke(
        id: Long,
        mealType: MealType,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        weight: Double
    ) {
        repository.updateFoodLog(
            id = id,
            mealType = mealType,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            weight = weight
        )
    }
}
