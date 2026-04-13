package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.Weight

@Entity
data class WeightEntity(
    @PrimaryKey val date: LocalDate = getCurrentDate(),
    val weight: Double
)

fun WeightEntity.toDomain() = Weight(
    date = date,
    weight = weight
)
