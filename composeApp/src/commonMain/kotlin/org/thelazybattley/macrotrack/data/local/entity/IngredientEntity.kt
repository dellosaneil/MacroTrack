package org.thelazybattley.macrotrack.data.local.entity

import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.domain.model.Ingredient

@Serializable
data class IngredientEntity(
    val name: String,
    val weight: Double
)

fun IngredientEntity.toDomain() = Ingredient(
    name = name,
    weight = weight
)


