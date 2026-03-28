package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros

@Entity
@Serializable
data class FoodEntity(
    @PrimaryKey val name: String,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val calories: Int,
    val weight: Double
)

fun FoodEntity.toFood() = Food(
    macros = FoodMacros(
        protein = protein,
        carbs = carbs,
        fat = fat,
        calories = calories
    ),
    name = name,
    weight = weight
)
