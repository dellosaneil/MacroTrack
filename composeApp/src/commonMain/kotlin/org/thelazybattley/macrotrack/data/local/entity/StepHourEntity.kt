package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.domain.model.StepHour

/** Added by AppDatabase.MIGRATION_16_17. One row per hour that had steps. */
@Entity(primaryKeys = ["date", "hour"])
@Serializable
data class StepHourEntity(
    val date: LocalDate,
    val hour: Int,
    val steps: Int
)

fun StepHourEntity.toDomain() = StepHour(date = date, hour = hour, steps = steps)
