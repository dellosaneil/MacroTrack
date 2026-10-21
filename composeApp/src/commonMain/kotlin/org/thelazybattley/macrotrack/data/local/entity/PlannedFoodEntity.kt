package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlanFood
import org.thelazybattley.macrotrack.domain.model.PlanStatus
import org.thelazybattley.macrotrack.domain.model.PlannedFood

/** Added by AppDatabase.MIGRATION_15_16, with [RepeatingMealEntity]. */
@Entity
@Serializable
data class PlannedFoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val mealType: MealType,
    val foodName: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val weight: Double,
    val dominantMacro: String,
    val isQuickAdd: Boolean = false,
    /** The repeating meal that added it; null when it was planned by hand. */
    val repeatingMealId: Long? = null,
    val status: PlanStatus = PlanStatus.PENDING,
    /** The food log it became once eaten. */
    val logId: Long? = null
)

fun PlannedFoodEntity.toDomain() = PlannedFood(
    id = id,
    date = date,
    mealType = mealType,
    food = PlanFood(
        foodName = foodName,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        weight = weight,
        dominantMacro = MacroType.valueOf(value = dominantMacro),
        isQuickAdd = isQuickAdd
    ),
    repeatingMealId = repeatingMealId,
    status = status,
    logId = logId
)

fun PlannedFood.toEntity() = PlannedFoodEntity(
    id = id,
    date = date,
    mealType = mealType,
    foodName = food.foodName,
    calories = food.calories,
    protein = food.protein,
    carbs = food.carbs,
    fat = food.fat,
    weight = food.weight,
    dominantMacro = food.dominantMacro.name,
    isQuickAdd = food.isQuickAdd,
    repeatingMealId = repeatingMealId,
    status = status,
    logId = logId
)
