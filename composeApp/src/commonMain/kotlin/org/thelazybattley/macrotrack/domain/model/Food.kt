package org.thelazybattley.macrotrack.domain.model

import org.thelazybattley.macrotrack.data.local.entity.FoodEntity

data class Food(
    val macros: FoodMacros,
    val name: String,
    val weight: Double,
    val dominantMacro: MacroType
)

fun Food.toEntity() = FoodEntity(
    name = name,
    protein = macros.protein,
    carbs = macros.carbs,
    fat = macros.fat,
    calories = macros.calories,
    weight = weight,
    dominantMacro = dominantMacro.name
)


val dummyFood = Food(
    name = "Rice",
    weight = 100.0,
    macros = FoodMacros(
        calories = 242,
        carbs = 10.0,
        protein = 34.2,
        fat = 12.3
    ),
    dominantMacro = MacroType.PROTEIN
)
