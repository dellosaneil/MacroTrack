package org.thelazybattley.macrotrack.features.createrecipe

import org.thelazybattley.macrotrack.domain.model.Food

data class CreateRecipeViewState(
    val recipeName: String = "",
    val savedRecipesName: List<String> = emptyList(),
    val selectedIngredients: List<Food> = emptyList(),
    val ingredients: List<Food> = emptyList(),
    val filteredIngredients: List<Food> = emptyList()
)
