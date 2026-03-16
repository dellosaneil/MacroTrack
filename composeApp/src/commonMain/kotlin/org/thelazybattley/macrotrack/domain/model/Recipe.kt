package org.thelazybattley.macrotrack.domain.model

import org.thelazybattley.macrotrack.data.local.entity.RecipeEntity

data class Recipe(
    val name: String,
    val ingredients: List<Ingredient>
)

fun Recipe.toEntity() = RecipeEntity(
    name = name,
    ingredients = ingredients.map { it.toEntity() }
)


