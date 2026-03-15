package org.thelazybattley.macrotrack.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.repository.FoodRepository

class GetAllFoodUseCase(private val repository: FoodRepository) {

    suspend operator fun invoke(): Flow<List<Food>> = repository.getAllFoods()

}
