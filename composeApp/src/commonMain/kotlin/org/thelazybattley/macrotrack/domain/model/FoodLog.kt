package org.thelazybattley.macrotrack.domain.model

import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity

data class FoodLog(
    val mealType: MealType,
    val recipeName: String,
    val id: Int
)

fun FoodLog.toEntity() = FoodLogEntity(
    mealType = mealType,
    recipeName = recipeName,
    id = id
)


