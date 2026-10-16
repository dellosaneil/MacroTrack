package org.thelazybattley.macrotrack.domain.usecase.weight

import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.repository.WeightRepository

class InsertWeightUseCase(private val repository: WeightRepository, private val telemetry: Telemetry) {

    suspend operator fun invoke(weight: Double, date: LocalDate = getCurrentDate()) =
        repository.insertWeight(
            weight = Weight(
                weight = weight,
                date = date
            )
        ).also { telemetry.logEvent(name = TelemetryEvent.WEIGHT_LOGGED) }

}
