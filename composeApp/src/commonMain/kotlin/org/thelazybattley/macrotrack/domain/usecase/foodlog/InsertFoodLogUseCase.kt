package org.thelazybattley.macrotrack.domain.usecase.foodlog

import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository

class InsertFoodLogUseCase(
    private val repository: FoodLogRepository
) {

    suspend operator fun invoke(
        recipeName: String,
        mealType: MealType,
        percentageEaten: Int
    ) {
        repository.insertFoodLog(
            recipeName = recipeName,
            mealType = mealType,
            percentageEaten = percentageEaten
        )
    }

}
