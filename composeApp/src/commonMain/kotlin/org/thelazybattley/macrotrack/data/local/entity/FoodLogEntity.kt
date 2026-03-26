package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MealType

@Entity
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mealType: MealType,
    val recipeName: String,
    val percentageEaten: Int
)

fun FoodLogEntity.toDomain() = FoodLog(
    mealType = mealType,
    recipeName = recipeName,
    percentageEaten = percentageEaten
)
