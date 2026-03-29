package org.thelazybattley.macrotrack.features.foodlog

import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MealType

data class FoodLogViewState(
    val breakfast: FoodLogFoodListByMealType = FoodLogFoodListByMealType(
        mealType = MealType.BREAKFAST
    ),
    val lunch: FoodLogFoodListByMealType = FoodLogFoodListByMealType(
        mealType = MealType.LUNCH
    ),
    val dinner: FoodLogFoodListByMealType = FoodLogFoodListByMealType(
        mealType = MealType.DINNER
    ),
    val snack: FoodLogFoodListByMealType = FoodLogFoodListByMealType(
        mealType = MealType.SNACK
    ),
    val allFood: List<FoodLog> = emptyList(),
    val calorieGoal: Int = 0,
    val totalCalories: Int = 0
)

data class FoodLogFoodListByMealType(
    val foodList: List<FoodLog> = emptyList(),
    val mealType: MealType,
    val calories: Int = 0
)
