package org.thelazybattley.macrotrack.features.createrecipe

data class CreateRecipeViewState(
    val name: String = "",
    val savedRecipesName: List<String> = emptyList()
)
