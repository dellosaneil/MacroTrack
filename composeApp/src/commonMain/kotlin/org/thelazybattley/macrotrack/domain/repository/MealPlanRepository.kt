package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.PlanStatus
import org.thelazybattley.macrotrack.domain.model.PlannedFood
import org.thelazybattley.macrotrack.domain.model.RepeatingMeal

interface MealPlanRepository {

    /** Adds [plannedFood]; its id is ignored and the new one returned. */
    suspend fun insertPlannedFood(plannedFood: PlannedFood): Long

    suspend fun updatePlannedStatus(id: Long, status: PlanStatus, logId: Long?)

    suspend fun deletePlannedFood(id: Long)

    /** Takes out what [repeatingMealId] planned from [fromDate] on that's still waiting; eaten and skipped days stay. */
    suspend fun deletePendingFrom(repeatingMealId: Long, fromDate: LocalDate)

    fun getPlannedFoods(): Flow<List<PlannedFood>>

    /** Adds [repeatingMeal], or replaces the one with its id; returns the id. */
    suspend fun saveRepeatingMeal(repeatingMeal: RepeatingMeal): Long

    suspend fun setRepeatingMealActive(id: Long, isActive: Boolean, updatedOn: LocalDate)

    suspend fun deleteRepeatingMeal(id: Long)

    fun getRepeatingMeals(): Flow<List<RepeatingMeal>>
}
