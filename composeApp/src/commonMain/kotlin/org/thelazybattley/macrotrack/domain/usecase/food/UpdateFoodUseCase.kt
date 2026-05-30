package org.thelazybattley.macrotrack.domain.usecase.food

import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.repository.FoodRepository

class UpdateFoodUseCase(private val repository: FoodRepository) {

    suspend operator fun invoke(food: Food) = repository.updateFood(food = food)

}
