package org.thelazybattley.macrotrack.domain.usecase.pro

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.ProStatus
import org.thelazybattley.macrotrack.domain.repository.ProRepository

class GetProStatusUseCase(
    private val repository: ProRepository,
    private val isPaywallEnabled: Boolean
) {

    operator fun invoke(): Flow<ProStatus> {
        if (!isPaywallEnabled) return flowOf(value = ProStatus(isPurchased = true))
        return combine(
            flow = repository.isPurchased(),
            flow2 = repository.getTrialStart()
        ) { isPurchased, trialStart ->
            ProStatus(isPurchased = isPurchased, trialStart = trialStart, today = getCurrentDate())
        }
    }
}
