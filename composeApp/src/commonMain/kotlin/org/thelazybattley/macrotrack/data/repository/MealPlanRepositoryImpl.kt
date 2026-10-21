package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.dao.MealPlanDao
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.data.local.entity.toEntity
import org.thelazybattley.macrotrack.domain.model.PlanStatus
import org.thelazybattley.macrotrack.domain.model.PlannedFood
import org.thelazybattley.macrotrack.domain.model.RepeatingMeal
import org.thelazybattley.macrotrack.domain.repository.MealPlanRepository

class MealPlanRepositoryImpl(private val dao: MealPlanDao) : MealPlanRepository {

    override suspend fun insertPlannedFood(plannedFood: PlannedFood): Long =
        dao.insertPlannedFood(plannedFood = plannedFood.copy(id = 0).toEntity())

    override suspend fun updatePlannedStatus(id: Long, status: PlanStatus, logId: Long?) =
        dao.updatePlannedStatus(id = id, status = status, logId = logId)

    override suspend fun deletePlannedFood(id: Long) = dao.deletePlannedFood(id = id)

    override suspend fun deletePendingFrom(repeatingMealId: Long, fromDate: LocalDate) =
        dao.deletePendingFrom(repeatingMealId = repeatingMealId, fromDate = fromDate)

    override fun getPlannedFoods(): Flow<List<PlannedFood>> =
        dao.getPlannedFoods().map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveRepeatingMeal(repeatingMeal: RepeatingMeal): Long {
        val id = dao.upsertRepeatingMeal(repeatingMeal = repeatingMeal.toEntity())
        return if (repeatingMeal.id == 0L) id else repeatingMeal.id
    }

    override suspend fun setRepeatingMealActive(id: Long, isActive: Boolean, updatedOn: LocalDate) =
        dao.setRepeatingMealActive(id = id, isActive = isActive, updatedOn = updatedOn)

    override suspend fun deleteRepeatingMeal(id: Long) = dao.deleteRepeatingMeal(id = id)

    override fun getRepeatingMeals(): Flow<List<RepeatingMeal>> =
        dao.getRepeatingMeals().map { entities -> entities.map { it.toDomain() } }
}
