package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.Measurement
import org.thelazybattley.macrotrack.domain.model.MeasurementType

@Entity(primaryKeys = ["date", "type"])
data class MeasurementEntity(
    val date: LocalDate,
    val type: MeasurementType,
    val valueCm: Double,
    val note: String? = null
)

fun MeasurementEntity.toDomain() = Measurement(
    date = date,
    type = type,
    valueCm = valueCm,
    note = note
)

fun Measurement.toEntity() = MeasurementEntity(
    date = date,
    type = type,
    valueCm = valueCm,
    note = note
)
