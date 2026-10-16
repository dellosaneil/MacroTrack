package org.thelazybattley.macrotrack.domain.usecase.review

import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository

class GetReviewHistoryUseCase(private val repository: AppFlagRepository) {

    operator fun invoke() = repository.getReviewHistory()
}
