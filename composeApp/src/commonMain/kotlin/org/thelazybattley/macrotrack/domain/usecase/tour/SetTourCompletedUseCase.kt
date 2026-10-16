package org.thelazybattley.macrotrack.domain.usecase.tour

import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository

class SetTourCompletedUseCase(private val repository: AppFlagRepository, private val telemetry: Telemetry) {

    suspend operator fun invoke(isCompleted: Boolean) {
        repository.setTourCompleted(isCompleted = isCompleted)
        if (isCompleted) telemetry.logEvent(name = TelemetryEvent.TOUR_COMPLETE)
    }
}
