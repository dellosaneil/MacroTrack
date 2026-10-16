package org.thelazybattley.macrotrack.domain.usecase.units

import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository

class GetUnitSystemUseCase(private val repository: AppFlagRepository) {

    operator fun invoke() = repository.getUnitSystem()
}
