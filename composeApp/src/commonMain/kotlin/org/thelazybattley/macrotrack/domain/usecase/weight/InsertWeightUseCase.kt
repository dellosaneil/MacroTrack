package org.thelazybattley.macrotrack.domain.usecase.weight

import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.repository.WeightRepository

class InsertWeightUseCase(private val repository: WeightRepository) {

    suspend operator fun invoke(weight: Double) =
        repository.insertWeight(
            weight = Weight(
                weight = weight,
                date = getCurrentDate()
            )
        )

}
