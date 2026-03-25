package org.thelazybattley.macrotrack.domain.usecase.food

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.repository.FoodRepository

class GetFoodByNameUseCase(private val repository: FoodRepository) {
    suspend operator fun invoke(name: String): Flow<List<Food>> = repository.getFoodByName(name)
}
