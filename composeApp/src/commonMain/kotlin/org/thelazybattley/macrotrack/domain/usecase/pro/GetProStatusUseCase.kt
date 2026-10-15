package org.thelazybattley.macrotrack.domain.usecase.pro

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.ProStatus
import org.thelazybattley.macrotrack.domain.repository.ProRepository

class GetProStatusUseCase(private val repository: ProRepository) {

    operator fun invoke(): Flow<ProStatus> = combine(
        flow = repository.isPurchased(),
        flow2 = repository.getTrialStart()
    ) { isPurchased, trialStart ->
        ProStatus(isPurchased = isPurchased, trialStart = trialStart, today = getCurrentDate())
    }
}
