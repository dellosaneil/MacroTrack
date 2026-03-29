package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MealType

@Entity
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foodName: String,
    val mealType: MealType,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

fun FoodLogEntity.toDomain() = FoodLog(
    mealType = mealType,
    id = id,
    foodName = foodName,
    calories = calories,
    fat = fat,
    protein = protein,
    carbs = carbs
)
