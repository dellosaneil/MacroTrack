package org.thelazybattley.macrotrack.domain.usecase.measurement

import org.thelazybattley.macrotrack.domain.repository.MeasurementRepository

class GetAllMeasurementsUseCase(private val repository: MeasurementRepository) {

    operator fun invoke() = repository.getAllMeasurements()
}
