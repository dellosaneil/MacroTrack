package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity)

    @Query(value = "SELECT * FROM FoodEntity")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query(value = "SELECT * FROM FoodEntity WHERE name = :name")
    fun getFoodByName(name: String): Flow<List<FoodEntity>>
}
