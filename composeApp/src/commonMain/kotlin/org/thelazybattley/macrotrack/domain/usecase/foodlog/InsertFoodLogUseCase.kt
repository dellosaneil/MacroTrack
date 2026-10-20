package org.thelazybattley.macrotrack.domain.usecase.foodlog

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository

class InsertFoodLogUseCase(
    private val repository: FoodLogRepository,
    private val telemetry: Telemetry
) {

    /** Logs to [date], which is today or a past day the user is filling in. */
    suspend operator fun invoke(
        foodName: String,
        mealType: MealType,
        calories: Int,
        fat: Double,
        carbs: Double,
        protein: Double,
        weight: Double,
        dominantMacro: MacroType,
        date: LocalDate,
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
        date = date,
        isQuickAdd = isQuickAdd
    ).also {
        telemetry.logEvent(
            name = TelemetryEvent.FOOD_LOGGED,
            params = mapOf(
                TelemetryEvent.PARAM_MEAL_TYPE to mealType.name.lowercase(),
                TelemetryEvent.PARAM_QUICK_ADD to isQuickAdd,
                TelemetryEvent.PARAM_PAST_DAY to (date < getCurrentDate())
            )
        )
    }
}
