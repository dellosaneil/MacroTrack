package org.thelazybattley.macrotrack.domain.usecase.telemetry

import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository

class IsTelemetryEnabledUseCase(private val repository: AppFlagRepository) {

    operator fun invoke() = repository.isTelemetryEnabled()
}
