package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.domain.model.MealType

@Dao
interface FoodLogDao {

    @Insert
    suspend fun insertFoodLog(foodLogEntity: FoodLogEntity) : Long

    @Query(
        "UPDATE foodlogentity SET mealType = :mealType, calories = :calories, protein = :protein, " +
            "carbs = :carbs, fat = :fat, weight = :weight WHERE id = :id"
    )
    suspend fun updateFoodLog(
        id: Long,
        mealType: MealType,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        weight: Double
    )

    @Query("DELETE FROM foodlogentity WHERE id = :id")
    suspend fun deleteFoodLog(id: Long)

    @Query(value = "SELECT * FROM FoodLogEntity")
    fun getAllFoodLogs(): Flow<List<FoodLogEntity>>

    @Query(value ="SELECT * FROM FoodLogEntity WHERE date = :date")
    fun getFoodLogByDate(date: LocalDate): Flow<List<FoodLogEntity>>

}
