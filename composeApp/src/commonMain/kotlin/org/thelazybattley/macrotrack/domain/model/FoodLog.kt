package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate

data class FoodLog(
    val mealType: MealType,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val id: Long,
    val foodName: String,
    val weight: Double,
    val dominantMacro: MacroType,
    val date: LocalDate,
    /** Calories and macros typed in with Quick add; [foodName] is the user's label, and no food or recipe has it. */
    val isQuickAdd: Boolean = false
) {
    /** A recipe log keeps a weight of 0.0 and the percentage eaten is worked out from its calories. */
    val isRecipe: Boolean get() = !isQuickAdd && weight == 0.0
}

val dummyFoodLog = FoodLog(
    mealType = MealType.BREAKFAST,
    calories = 123,
    protein = 21.3,
    carbs = 12.3,
    fat = 12.3,
    id = 1,
    foodName = "Chicken Breast",
    weight = 100.0,
    dominantMacro = MacroType.PROTEIN,
    date = getCurrentDate()
)

