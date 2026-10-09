package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.thelazybattley.macrotrack.data.local.dao.MeasurementDao
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.data.local.entity.toEntity
import org.thelazybattley.macrotrack.domain.model.Measurement
import org.thelazybattley.macrotrack.domain.repository.MeasurementRepository

class MeasurementRepositoryImpl(private val dao: MeasurementDao) : MeasurementRepository {

    override suspend fun insertMeasurements(measurements: List<Measurement>) {
        dao.insertMeasurements(measurements = measurements.map { it.toEntity() })
    }

    override fun getAllMeasurements(): Flow<List<Measurement>> =
        dao.getAllMeasurements().map { entities -> entities.map { it.toDomain() } }
}
