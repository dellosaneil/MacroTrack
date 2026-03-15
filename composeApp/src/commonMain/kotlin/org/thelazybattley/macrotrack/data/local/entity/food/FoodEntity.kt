package org.thelazybattley.macrotrack.data.local.entity.food

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros

@Entity
data class FoodEntity(
    @PrimaryKey val name: String,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val calories: Double,
    val weight: Int
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
