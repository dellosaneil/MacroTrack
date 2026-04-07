package org.thelazybattley.macrotrack.domain.model

import org.thelazybattley.macrotrack.data.local.entity.RecipeEntity

data class Recipe(
    val name: String,
    val ingredients: List<Ingredient>,
    val dominantMacro: MacroType
)

fun Recipe.toEntity() = RecipeEntity(
    name = name,
    ingredients = ingredients,
    dominantMacro = dominantMacro.name
)


