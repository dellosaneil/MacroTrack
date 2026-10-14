package org.thelazybattley.macrotrack.domain.usecase.tour

import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository

class IsTourCompletedUseCase(private val repository: AppFlagRepository) {

    operator fun invoke() = repository.isTourCompleted()
}
