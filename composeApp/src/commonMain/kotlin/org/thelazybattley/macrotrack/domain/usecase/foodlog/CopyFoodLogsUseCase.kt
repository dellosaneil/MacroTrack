package org.thelazybattley.macrotrack.domain.usecase.foodlog

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.FoodLogCopy
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.copiedLogs
import org.thelazybattley.macrotrack.domain.model.logsToReplace
import org.thelazybattley.macrotrack.domain.model.toPlanFood
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository
import org.thelazybattley.macrotrack.domain.repository.MealPlanRepository
import org.thelazybattley.macrotrack.domain.usecase.mealplan.PlanFoodUseCase

/**
 * Copies [logs] to each of [targetDates], into [mealType] or, when null, each log's own meal. With [replace],
 * what's already in that meal (or that whole day) goes first. Later days can't be logged, so copies to them are
 * planned there instead, like filling a meal in the meal plan.
 */
class CopyFoodLogsUseCase(
    private val repository: FoodLogRepository,
    private val planFoodUseCase: PlanFoodUseCase,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(
        logs: List<FoodLog>,
        targetDates: List<LocalDate>,
        mealType: MealType?,
        replace: Boolean
    ): FoodLogCopy {
        val today = getCurrentDate()
        val dates = targetDates.filter { it <= today }.distinct()
        val plannedIds = copiedLogs(logs = logs, targetDates = targetDates.filter { it > today }.distinct(), mealType = mealType)
            .map { planFoodUseCase(food = it.toPlanFood(), date = it.date, mealType = it.mealType) }
        val replaced = if (replace) {
            logsToReplace(existing = repository.getAllFoodLogs().first(), targetDates = dates, mealType = mealType)
        } else {
            emptyList()
        }
        replaced.forEach { repository.deleteFoodLog(id = it.id) }
        val insertedIds = copiedLogs(logs = logs, targetDates = dates, mealType = mealType).map { repository.insert(log = it) }
        telemetry.logEvent(
            name = TelemetryEvent.FOOD_COPIED,
            params = mapOf(
                TelemetryEvent.PARAM_SCOPE to if (mealType == null) "day" else "meal",
                TelemetryEvent.PARAM_RESULT to if (replace) "replace" else "add"
            )
        )
        return FoodLogCopy(
            insertedIds = insertedIds,
            replaced = replaced,
            plannedIds = plannedIds,
            itemCount = logs.size,
            targetDates = targetDates.distinct()
        )
    }
}

/** Takes a copy back out and puts back what it replaced. */
class UndoFoodLogCopyUseCase(
    private val repository: FoodLogRepository,
    private val mealPlanRepository: MealPlanRepository
) {

    suspend operator fun invoke(copy: FoodLogCopy) {
        copy.insertedIds.forEach { repository.deleteFoodLog(id = it) }
        copy.plannedIds.forEach { mealPlanRepository.deletePlannedFood(id = it) }
        copy.replaced.forEach { repository.insert(log = it) }
    }
}

private suspend fun FoodLogRepository.insert(log: FoodLog): Long = insertFoodLog(
    foodName = log.foodName,
    mealType = log.mealType,
    calories = log.calories,
    fat = log.fat,
    carbs = log.carbs,
    protein = log.protein,
    weight = log.weight,
    dominantMacro = log.dominantMacro,
    date = log.date,
    isQuickAdd = log.isQuickAdd
)
