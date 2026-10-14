package org.thelazybattley.macrotrack.domain.usecase.tour

import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository

class SetTourCompletedUseCase(private val repository: AppFlagRepository) {

    suspend operator fun invoke(isCompleted: Boolean) = repository.setTourCompleted(isCompleted = isCompleted)
}
