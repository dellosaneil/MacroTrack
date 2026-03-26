package org.thelazybattley.macrotrack.domain.usecase.foodlog

import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository

class GetAllFoodLogsUseCase(private val repository: FoodLogRepository) {

    suspend operator fun invoke() = repository.getAllFoodLogs()

}
