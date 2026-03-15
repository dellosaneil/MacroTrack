package org.thelazybattley.macrotrack.domain.model

import org.thelazybattley.macrotrack.data.local.entity.FoodEntity

data class Food(
    val macros: FoodMacros,
    val name: String,
    val weight: Int
)

fun Food.toEntity() = FoodEntity(
    name = name,
    protein = macros.protein,
    carbs = macros.carbs,
    fat = macros.fat,
    calories = macros.calories,
    weight = weight
)
