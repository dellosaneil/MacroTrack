package org.thelazybattley.macrotrack.domain.usecase.checkin

import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository

/** Opening the check-in clears its "New" dot on Today until the suggestion moves again. */
class MarkCheckInSeenUseCase(private val appFlagRepository: AppFlagRepository) {
    suspend operator fun invoke(suggestedTarget: Int) {
        appFlagRepository.setSeenCheckInSuggestion(calories = suggestedTarget)
    }
}
