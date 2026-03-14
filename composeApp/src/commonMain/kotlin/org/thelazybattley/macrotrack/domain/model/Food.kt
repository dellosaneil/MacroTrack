package org.thelazybattley.macrotrack.domain.model

data class Food(
    val macros: FoodMacros,
    val name: String,
    val weight: Int
)
