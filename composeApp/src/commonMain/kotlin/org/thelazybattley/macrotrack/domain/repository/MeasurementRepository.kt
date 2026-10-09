package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.Measurement

interface MeasurementRepository {

    suspend fun insertMeasurements(measurements: List<Measurement>)

    fun getAllMeasurements(): Flow<List<Measurement>>
}
