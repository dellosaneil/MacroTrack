package org.thelazybattley.macrotrack.domain.usecase.pro

import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.repository.ProRepository

class StartProTrialUseCase(private val repository: ProRepository) {

    suspend operator fun invoke() = repository.startTrialIfNeeded(date = getCurrentDate())
}
