package org.thelazybattley.macrotrack.domain.usecase.pro

import org.thelazybattley.macrotrack.domain.repository.ProRepository

class IsTrialEndedNoticeDismissedUseCase(private val repository: ProRepository) {

    operator fun invoke() = repository.isTrialEndedNoticeDismissed()
}

class DismissTrialEndedNoticeUseCase(private val repository: ProRepository) {

    suspend operator fun invoke() = repository.dismissTrialEndedNotice()
}
