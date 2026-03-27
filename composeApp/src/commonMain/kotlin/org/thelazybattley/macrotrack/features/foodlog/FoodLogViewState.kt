package org.thelazybattley.macrotrack.features.foodlog

import org.thelazybattley.macrotrack.domain.model.Recipe

data class FoodLogViewState(
    val breakfastFood : List<Recipe> = emptyList(),
    val lunchFood : List<Recipe> = emptyList(),
    val dinnerFood : List<Recipe> = emptyList(),
    val allRecipe : List<Recipe> = emptyList()
)
