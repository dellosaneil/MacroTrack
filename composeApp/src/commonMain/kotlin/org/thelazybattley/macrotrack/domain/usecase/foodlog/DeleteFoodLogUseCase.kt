package org.thelazybattley.macrotrack.domain.usecase.foodlog

import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository

class DeleteFoodLogUseCase(private val repository: FoodLogRepository) {

    suspend operator fun invoke(id: Long) {
        repository.deleteFoodLog(id = id)
    }

}
