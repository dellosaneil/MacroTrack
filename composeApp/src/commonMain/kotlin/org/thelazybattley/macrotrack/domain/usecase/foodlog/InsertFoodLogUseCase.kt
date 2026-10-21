package org.thelazybattley.macrotrack.domain.usecase.foodlog

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlanFood
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository
import org.thelazybattley.macrotrack.domain.usecase.mealplan.PlanFoodUseCase

class InsertFoodLogUseCase(
    private val repository: FoodLogRepository,
    private val planFoodUseCase: PlanFoodUseCase,
    private val telemetry: Telemetry
) {

    /**
     * Logs to [date], which is today or a past day the user is filling in. A later day can't be logged, so food
     * added to one from the meal plan is planned there instead; the id returned is then the planned food's.
     */
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
    ): Long = if (date > getCurrentDate()) {
        planFoodUseCase(
            food = PlanFood(
                foodName = foodName,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                weight = weight,
                dominantMacro = dominantMacro,
                isQuickAdd = isQuickAdd
            ),
            date = date,
            mealType = mealType
        )
    } else {
        repository.insertFoodLog(
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
}
