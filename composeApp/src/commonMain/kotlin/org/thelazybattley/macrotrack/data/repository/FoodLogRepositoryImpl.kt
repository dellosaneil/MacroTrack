package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.thelazybattley.macrotrack.data.local.dao.FoodLogDao
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository

class FoodLogRepositoryImpl(private val dao: FoodLogDao) : FoodLogRepository {
    override suspend fun insertFoodLog(
        recipeName: String,
        mealType: MealType,
        percentageEaten: Int
    ) = dao.insertFoodLog(
        foodLogEntity = FoodLogEntity(
            mealType = mealType,
            recipeName = recipeName,
            percentageEaten = percentageEaten
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
