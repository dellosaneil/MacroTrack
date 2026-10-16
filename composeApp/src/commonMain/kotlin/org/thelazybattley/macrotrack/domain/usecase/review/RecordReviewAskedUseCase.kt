package org.thelazybattley.macrotrack.domain.usecase.review

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository

class RecordReviewAskedUseCase(private val repository: AppFlagRepository, private val telemetry: Telemetry) {

    suspend operator fun invoke(date: LocalDate) {
        repository.recordReviewAsked(date = date)
        telemetry.logEvent(name = TelemetryEvent.REVIEW_ASKED)
    }
}
