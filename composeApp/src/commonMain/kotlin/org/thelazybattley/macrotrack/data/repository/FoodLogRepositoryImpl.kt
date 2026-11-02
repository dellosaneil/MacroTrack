package org.thelazybattley.macrotrack.data.repository

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.dao.FoodLogDao
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.FoodLogSource
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository

@OptIn(ExperimentalTime::class)
class FoodLogRepositoryImpl(private val dao: FoodLogDao) : FoodLogRepository {
    override suspend fun insertFoodLog(
        foodName: String,
        mealType: MealType,
        calories: Int,
        fat: Double,
        carbs: Double,
        protein: Double,
        weight: Double,
        dominantMacro: MacroType,
        date: LocalDate,
        isQuickAdd: Boolean,
        source: FoodLogSource?,
        copiedFrom: LocalDate?
    ) = dao.insertFoodLog(
        foodLogEntity = FoodLogEntity(
            date = date,
            foodName = foodName,
            mealType = mealType,
            calories = calories,
            fat = fat,
            carbs = carbs,
            protein = protein,
            weight = weight,
            dominantMacro = dominantMacro.name,
            isQuickAdd = isQuickAdd,
            loggedAt = Clock.System.now().toEpochMilliseconds(),
            source = source?.name,
            copiedFrom = copiedFrom
        )
    )

    override suspend fun updateFoodLog(
        id: Long,
        mealType: MealType,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        weight: Double,
        editedFrom: Double?
    ) = dao.updateFoodLog(
        id = id,
        mealType = mealType,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        weight = weight,
        editedFrom = editedFrom
    )

    override suspend fun deleteFoodLog(
        id: Long
    ) = dao.deleteFoodLog(id = id)

    override fun getAllFoodLogs(): Flow<List<FoodLog>> {
        return dao.getAllFoodLogs().map { flow ->
            flow.map { entity ->
                entity.toDomain()
            }
        }
    }

    override fun getFoodLogByDate(localDate: LocalDate): Flow<List<FoodLog>> {
        return dao.getFoodLogByDate(date = localDate).map { flow ->
            flow.map { entity ->
                entity.toDomain()
            }
        }
    }
}
