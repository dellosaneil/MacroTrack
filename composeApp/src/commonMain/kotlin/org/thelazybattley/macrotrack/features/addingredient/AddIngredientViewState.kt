package org.thelazybattley.macrotrack.features.addingredient

import org.thelazybattley.macrotrack.domain.model.FoodMacros

data class AddIngredientViewState(
    val macros: FoodMacros? = null,
    val weight: Double = 0.0,
    val name: String = ""
)
