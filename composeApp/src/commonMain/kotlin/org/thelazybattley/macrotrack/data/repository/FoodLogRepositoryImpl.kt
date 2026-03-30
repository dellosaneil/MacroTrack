package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.thelazybattley.macrotrack.data.local.dao.FoodLogDao
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository

class FoodLogRepositoryImpl(private val dao: FoodLogDao) : FoodLogRepository {
    override suspend fun insertFoodLog(
        foodName: String,
        mealType: MealType,
        calories: Int,
        fat: Double,
        carbs: Double,
        protein: Double,
        weight: Double,
        dominantMacro: MacroType
    ) = dao.insertFoodLog(
        foodLogEntity = FoodLogEntity(
            foodName = foodName,
            mealType = mealType,
            calories = calories,
            fat = fat,
            carbs = carbs,
            protein = protein,
            weight = weight,
            dominantMacro = dominantMacro.name
        )
    )

    override suspend fun deleteFoodLog(
        id: Int
    ) = dao.deleteFoodLog(id = id)

    override fun getAllFoodLogs(): Flow<List<FoodLog>> {
        return dao.getAllFoodLogs().map { flow ->
            flow.map { entity ->
                entity.toDomain()
            }
        }
    }
}
