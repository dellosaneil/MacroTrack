package org.thelazybattley.macrotrack.domain.usecase.measurement

import org.thelazybattley.macrotrack.domain.model.Measurement
import org.thelazybattley.macrotrack.domain.repository.MeasurementRepository

class InsertMeasurementsUseCase(private val repository: MeasurementRepository) {

    suspend operator fun invoke(measurements: List<Measurement>) {
        if (measurements.isEmpty()) return
        repository.insertMeasurements(measurements = measurements)
    }
}
