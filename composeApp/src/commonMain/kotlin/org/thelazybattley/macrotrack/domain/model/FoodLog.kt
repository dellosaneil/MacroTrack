package org.thelazybattley.macrotrack.domain.model

data class FoodLog(
    val mealType: MealType,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val id: Long,
    val foodName: String,
    val weight: Double,
    val dominantMacro: MacroType
)

val dummyFoodLog = FoodLog(
    mealType = MealType.BREAKFAST,
    calories = 123,
    protein = 21.3,
    carbs = 12.3,
    fat = 12.3,
    id = 1,
    foodName = "Chicken Breast",
    weight = 100.0,
    dominantMacro = MacroType.PROTEIN
)

