package org.thelazybattley.macrotrack.domain.usecase.weight

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.repository.WeightRepository

class InsertWeightUseCase(private val repository: WeightRepository) {

    suspend operator fun invoke(weight: Double, date: LocalDate = getCurrentDate()) =
        repository.insertWeight(
            weight = Weight(
                weight = weight,
                date = date
            )
        )

}
