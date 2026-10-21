package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.entity.PlannedFoodEntity
import org.thelazybattley.macrotrack.data.local.entity.RepeatingMealEntity
import org.thelazybattley.macrotrack.domain.model.PlanStatus

@Dao
interface MealPlanDao {

    @Insert
    suspend fun insertPlannedFood(plannedFood: PlannedFoodEntity): Long

    @Query("UPDATE PlannedFoodEntity SET status = :status, logId = :logId WHERE id = :id")
    suspend fun updatePlannedStatus(id: Long, status: PlanStatus, logId: Long?)

    @Query("DELETE FROM PlannedFoodEntity WHERE id = :id")
    suspend fun deletePlannedFood(id: Long)

    @Query("DELETE FROM PlannedFoodEntity WHERE repeatingMealId = :repeatingMealId AND status = 'PENDING' AND date >= :fromDate")
    suspend fun deletePendingFrom(repeatingMealId: Long, fromDate: LocalDate)

    @Query("SELECT * FROM PlannedFoodEntity")
    fun getPlannedFoods(): Flow<List<PlannedFoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRepeatingMeal(repeatingMeal: RepeatingMealEntity): Long

    @Query("UPDATE RepeatingMealEntity SET isActive = :isActive, updatedOn = :updatedOn WHERE id = :id")
    suspend fun setRepeatingMealActive(id: Long, isActive: Boolean, updatedOn: LocalDate)

    @Query("DELETE FROM RepeatingMealEntity WHERE id = :id")
    suspend fun deleteRepeatingMeal(id: Long)

    @Query("SELECT * FROM RepeatingMealEntity")
    fun getRepeatingMeals(): Flow<List<RepeatingMealEntity>>
}
