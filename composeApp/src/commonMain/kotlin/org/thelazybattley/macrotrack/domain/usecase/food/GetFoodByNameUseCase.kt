package org.thelazybattley.macrotrack.domain.usecase.food

import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.repository.FoodRepository

class GetFoodByNameUseCase(private val repository: FoodRepository) {
    suspend operator fun invoke(name: String): List<Food> = repository.getFoodByName(name)
}
