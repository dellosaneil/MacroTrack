package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity

@Dao
interface FoodLogDao {

    @Insert
    suspend fun insertFoodLog(foodLogEntity: FoodLogEntity) : Long

    @Query("DELETE FROM foodlogentity WHERE id = :id")
    suspend fun deleteFoodLog(id: Long)

    @Query(value = "SELECT * FROM FoodLogEntity")
    fun getAllFoodLogs(): Flow<List<FoodLogEntity>>

}
