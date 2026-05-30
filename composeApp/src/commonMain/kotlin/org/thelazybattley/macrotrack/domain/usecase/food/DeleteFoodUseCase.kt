package org.thelazybattley.macrotrack.domain.usecase.food

import org.thelazybattley.macrotrack.domain.repository.FoodRepository

class DeleteFoodUseCase(private val repository: FoodRepository) {

    suspend operator fun invoke(name: String) = repository.deleteFood(name = name)

}
