package org.thelazybattley.macrotrack.domain.usecase.deletedata

import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.DeleteDataStep
import org.thelazybattley.macrotrack.domain.repository.DeleteDataRepository

class DeleteAllDataUseCase(private val repository: DeleteDataRepository, private val telemetry: Telemetry) {

    /** Deletes each step in order, calling [onStep] as each one starts. */
    suspend operator fun invoke(onStep: suspend (DeleteDataStep) -> Unit) {
        DeleteDataStep.entries.forEach { step ->
            onStep(step)
            repository.delete(step = step)
        }
        telemetry.logEvent(name = TelemetryEvent.DATA_DELETED)
    }
}
