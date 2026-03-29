package org.thelazybattley.macrotrack.domain.model

import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity

data class FoodLog(
    val mealType: MealType,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val id: Int,
    val foodName: String
)

fun FoodLog.toEntity() = FoodLogEntity(
    mealType = mealType,
    id = id,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    foodName = foodName
)


