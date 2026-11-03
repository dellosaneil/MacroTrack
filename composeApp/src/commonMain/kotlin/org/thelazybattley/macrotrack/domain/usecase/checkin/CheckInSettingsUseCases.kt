package org.thelazybattley.macrotrack.domain.usecase.checkin

import org.thelazybattley.macrotrack.domain.model.CheckInSettings
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository

/** Saves how far back the check-in looks and how it measures weight change. */
class SetCheckInSettingsUseCase(private val repository: AppFlagRepository) {
    suspend operator fun invoke(settings: CheckInSettings) {
        repository.setCheckInSettings(settings = settings)
    }
}
