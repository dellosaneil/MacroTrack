package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.Food

interface FoodRepository {

    suspend fun insertFood(food: Food)

    suspend fun getAllFoods(): Flow<List<Food>>

    suspend fun getFoodByName(name: String): Flow<List<Food>>

}
