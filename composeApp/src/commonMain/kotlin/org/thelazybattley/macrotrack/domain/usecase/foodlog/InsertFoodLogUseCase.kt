package org.thelazybattley.macrotrack.domain.usecase.foodlog

import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository

class InsertFoodLogUseCase(
    private val repository: FoodLogRepository,
    private val telemetry: Telemetry
) {

    suspend operator fun invoke(
        foodName: String,
        mealType: MealType,
        calories: Int,
        fat: Double,
        carbs: Double,
        protein: Double,
        weight: Double,
        dominantMacro: MacroType,
        isQuickAdd: Boolean = false
    ) = repository.insertFoodLog(
        foodName = foodName,
        mealType = mealType,
        calories = calories,
        fat = fat,
        carbs = carbs,
        protein = protein,
        weight = weight,
        dominantMacro = dominantMacro,
        isQuickAdd = isQuickAdd
    ).also {
        telemetry.logEvent(
            name = TelemetryEvent.FOOD_LOGGED,
            params = mapOf(
                TelemetryEvent.PARAM_MEAL_TYPE to mealType.name.lowercase(),
                TelemetryEvent.PARAM_QUICK_ADD to isQuickAdd
            )
        )
    }
}
