package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
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

    /** Changes a log's meal and portion; [weight] stays 0.0 for a recipe. */
    suspend fun updateFoodLog(
        id: Long,
        mealType: MealType,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        weight: Double
    )

    suspend fun deleteFoodLog(id: Long)

    fun getAllFoodLogs(): Flow<List<FoodLog>>

    fun getFoodLogByDate(localDate: LocalDate): Flow<List<FoodLog>>

}
