package org.thelazybattley.macrotrack.features.createrecipe

import org.thelazybattley.macrotrack.domain.model.Food

data class CreateRecipeViewState(
    val recipeName: String = "",
    val savedRecipesName: List<String> = emptyList(),
    val selectedIngredients: List<Food> = emptyList(),
    val ingredients: List<Food> = emptyList(),
    val filteredIngredients: List<Food> = emptyList(),
    val macros: CreateRecipeMacros = CreateRecipeMacros()
)

data class CreateRecipeMacros(
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val proteinPercentage: Double = 0.0,
    val carbsPercentage: Double = 0.0,
    val fatPercentage: Double = 0.0,
    val calories: Int = 0
)
