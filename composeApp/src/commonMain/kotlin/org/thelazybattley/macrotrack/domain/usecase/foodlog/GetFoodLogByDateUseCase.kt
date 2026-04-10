package org.thelazybattley.macrotrack.domain.usecase.foodlog

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository


class GetFoodLogByDateUseCase(private val repository: FoodLogRepository) {

    operator fun invoke(localDate: LocalDate) = repository.getFoodLogByDate(localDate = localDate)

}
