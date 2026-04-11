package org.thelazybattley.macrotrack.features.foodlog

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.FoodMacros
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
    val filteredFoodLog: List<FoodLog> = emptyList(),
    val allFoodLog: List<FoodLog> = emptyList(),
    val macroGoals: MacroGoals? = null,
    val totalFoodMacros: FoodMacros? = null,
    val navigateMealTypeParameter : MealType? = null,
    val availableDates: List<LocalDate> = emptyList(),
    val selectedDate: LocalDate = getCurrentDate()
)

data class FoodLogFoodListByMealType(
    val foodList: List<FoodLog> = emptyList(),
    val mealType: MealType,
    val calories: Int = 0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
)
