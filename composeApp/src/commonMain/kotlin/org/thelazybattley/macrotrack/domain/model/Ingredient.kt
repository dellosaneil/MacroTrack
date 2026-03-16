package org.thelazybattley.macrotrack.domain.model

import org.thelazybattley.macrotrack.data.local.entity.IngredientEntity


data class Ingredient(
    val name: String,
    val weight: Double
)

fun Ingredient.toEntity() = IngredientEntity(
    name = name,
    weight = weight
)
