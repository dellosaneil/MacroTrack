package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.entity.WeightEntity

data class Weight(
    val date: LocalDate,
    val weight: Double
)

fun Weight.toEntity() = WeightEntity(
    weight = weight
)
