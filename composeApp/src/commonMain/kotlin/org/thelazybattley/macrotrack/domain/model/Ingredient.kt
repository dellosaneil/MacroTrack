package org.thelazybattley.macrotrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val name: String,
    val weight: Double
)
