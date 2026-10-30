package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.domain.model.WaterEntry
import org.thelazybattley.macrotrack.domain.model.WaterKind

/** One drink of water: [amountMl] on [date], added at [loggedAt] (epoch milliseconds). */
@Entity(indices = [Index(value = ["date"])])
@Serializable
data class WaterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val amountMl: Int,
    val kind: String,
    val loggedAt: Long
)

fun WaterEntity.toDomain() = WaterEntry(
    id = id,
    date = date,
    amountMl = amountMl,
    kind = WaterKind.entries.find { it.name == kind } ?: WaterKind.OTHER,
    loggedAtMillis = loggedAt
)

fun WaterEntry.toEntity() = WaterEntity(id = id, date = date, amountMl = amountMl, kind = kind.name, loggedAt = loggedAtMillis)
