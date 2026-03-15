package org.thelazybattley.macrotrack.data.local.dao.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.thelazybattley.macrotrack.data.local.entity.food.FoodEntity

@Dao
interface FoodDao {
    @Insert
    suspend fun insertFood(food: FoodEntity)

    @Query(value = "SELECT * FROM FoodEntity")
    suspend fun getAllFoods(): List<FoodEntity>

    @Query(value = "SELECT * FROM FoodEntity WHERE name = :name")
    suspend fun getFoodByName(name: String): FoodEntity
}
