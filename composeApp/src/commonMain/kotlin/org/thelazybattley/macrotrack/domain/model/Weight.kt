package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate

data class Weight(
    val date: LocalDate,
    val weight: Double
)
