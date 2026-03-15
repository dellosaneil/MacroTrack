package org.thelazybattley.macrotrack.domain.usecase

import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.repository.FoodRepository

class InsertFoodUseCase(private val repository: FoodRepository) {
    suspend operator fun invoke(food: Food) {
        repository.insertFood(food)
    }
}
