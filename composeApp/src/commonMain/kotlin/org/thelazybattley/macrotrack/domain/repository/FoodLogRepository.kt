package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MealType

interface FoodLogRepository {

    suspend fun insertFoodLog(
        recipeName: String, mealType: MealType,
        percentageEaten: Int
    )

    suspend fun deleteFoodLog(id: Int)

    fun getAllFoodLogs(): Flow<List<FoodLog>>
}
