package org.thelazybattley.macrotrack.domain.usecase.telemetry

import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository

class SetTelemetryEnabledUseCase(private val repository: AppFlagRepository) {

    suspend operator fun invoke(isEnabled: Boolean) = repository.setTelemetryEnabled(isEnabled = isEnabled)
}
