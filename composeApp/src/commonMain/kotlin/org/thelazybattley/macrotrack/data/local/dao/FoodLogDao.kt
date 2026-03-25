package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity

@Dao
interface FoodLogDao {

    @Insert
    suspend fun insertFoodLog(foodLogEntity: FoodLogEntity)

    @Delete
    suspend fun deleteFoodLog(foodLogEntity: FoodLogEntity)

    @Query(value = "SELECT * FROM FoodLogEntity")
    fun getAllFoodLogs(): Flow<List<FoodLogEntity>>

}
