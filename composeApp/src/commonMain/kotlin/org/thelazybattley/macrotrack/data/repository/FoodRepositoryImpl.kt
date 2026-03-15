package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.thelazybattley.macrotrack.data.local.dao.dao.FoodDao
import org.thelazybattley.macrotrack.data.local.entity.food.toFood
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.toEntity
import org.thelazybattley.macrotrack.domain.repository.FoodRepository

class FoodRepositoryImpl(
    private val dao: FoodDao
) : FoodRepository {
    override suspend fun insertFood(food: Food) {
        dao.insertFood(food = food.toEntity())
    }

    override suspend fun getAllFoods(): Flow<List<Food>> {
        return dao.getAllFoods().map { flow ->
            flow.map { foodEntity ->
                foodEntity.toFood()
            }
        }
    }

    override suspend fun getFoodByName(name: String): List<Food> {
        return dao.getFoodByName(name = name).map { food ->
            food.toFood()
        }
    }
}
