package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.domain.model.Ingredient
import org.thelazybattley.macrotrack.domain.model.Recipe

@Entity
@Serializable
data class RecipeEntity(
    @PrimaryKey val name: String,
    val ingredients: List<Ingredient>
)

fun RecipeEntity.toDomain() = Recipe(
    name = name,
    ingredients = ingredients
)
