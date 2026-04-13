package org.thelazybattley.macrotrack.domain.usecase.weight

import org.thelazybattley.macrotrack.domain.repository.WeightRepository

class GetAllWeightUseCase(private val repository: WeightRepository) {

    operator fun invoke() = repository.getAllWeight()

}
