package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType

interface FoodLogRepository {

    suspend fun insertFoodLog(
        foodName: String,
        mealType: MealType,
        calories: Int,
        fat: Double,
        carbs: Double,
        protein: Double,
        weight: Double,
        dominantMacro: MacroType
    ) : Long

    suspend fun deleteFoodLog(id: Long)

    fun getAllFoodLogs(): Flow<List<FoodLog>>
}
