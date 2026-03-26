package org.thelazybattley.macrotrack.domain.model

import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity

data class FoodLog(
    val mealType: MealType,
    val recipeName: String,
    val percentageEaten: Int
)

fun FoodLog.toEntity() = FoodLogEntity(
    mealType = mealType,
    recipeName = recipeName,
    percentageEaten = percentageEaten
)


