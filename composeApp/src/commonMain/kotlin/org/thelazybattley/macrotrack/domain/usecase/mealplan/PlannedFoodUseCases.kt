package org.thelazybattley.macrotrack.domain.usecase.mealplan

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlanFood
import org.thelazybattley.macrotrack.domain.model.PlanStatus
import org.thelazybattley.macrotrack.domain.model.PlannedFood
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository
import org.thelazybattley.macrotrack.domain.repository.MealPlanRepository

class GetPlannedFoodsUseCase(private val repository: MealPlanRepository) {
    operator fun invoke(): Flow<List<PlannedFood>> = repository.getPlannedFoods()
}

/**
 * Today's planned food for the food log, waiting or already eaten or skipped. A repeating meal that logs itself is
 * left out: it's in the food log already, with nothing to tap.
 */
class GetPlannedTodayUseCase(private val repository: MealPlanRepository) {
    operator fun invoke(): Flow<List<PlannedFood>> = combine(
        flow = repository.getPlannedFoods(),
        flow2 = repository.getRepeatingMeals()
    ) { planned, repeatingMeals ->
        val today = getCurrentDate()
        val autoLogIds = repeatingMeals.filter { it.isAutoLog }.map { it.id }.toSet()
        planned.filter { it.date == today && it.repeatingMealId !in autoLogIds }
    }
}

/** What the food log does with today's planned food, together so it takes one dependency. */
class PlannedFoodActions(
    val sync: SyncRepeatingMealsUseCase,
    val markEaten: MarkPlannedEatenUseCase,
    val skip: SkipPlannedFoodUseCase,
    val undo: UndoPlannedFoodUseCase
)

/** Plans [food] for [date], a day after today; it isn't counted until it's marked eaten on the day. */
class PlanFoodUseCase(
    private val repository: MealPlanRepository,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(food: PlanFood, date: LocalDate, mealType: MealType): Long =
        repository.insertPlannedFood(
            plannedFood = PlannedFood(id = 0, date = date, mealType = mealType, food = food)
        ).also {
            telemetry.logEvent(
                name = TelemetryEvent.FOOD_PLANNED,
                params = mapOf(TelemetryEvent.PARAM_MEAL_TYPE to mealType.name.lowercase())
            )
        }
}

/** Ate it: logs each of [planned] that's still waiting, on its own day, and remembers the log so it can be undone. */
class MarkPlannedEatenUseCase(
    private val repository: MealPlanRepository,
    private val foodLogRepository: FoodLogRepository,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(planned: List<PlannedFood>) {
        val today = getCurrentDate()
        val toLog = planned.filter { it.isPending && it.date <= today }
        toLog.forEach { repository.markEaten(plannedFood = it, foodLogRepository = foodLogRepository) }
        if (toLog.isEmpty()) return
        telemetry.logEvent(
            name = TelemetryEvent.PLANNED_FOOD_DONE,
            params = mapOf(TelemetryEvent.PARAM_RESULT to if (toLog.size > 1) "ate_all" else "ate")
        )
    }
}

class SkipPlannedFoodUseCase(
    private val repository: MealPlanRepository,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(planned: PlannedFood) {
        repository.updatePlannedStatus(id = planned.id, status = PlanStatus.SKIPPED, logId = null)
        telemetry.logEvent(name = TelemetryEvent.PLANNED_FOOD_DONE, params = mapOf(TelemetryEvent.PARAM_RESULT to "skip"))
    }
}

/** Puts [planned] back to waiting, taking out the log Ate it added. */
class UndoPlannedFoodUseCase(
    private val repository: MealPlanRepository,
    private val foodLogRepository: FoodLogRepository
) {
    suspend operator fun invoke(planned: PlannedFood) {
        planned.logId?.let { foodLogRepository.deleteFoodLog(id = it) }
        repository.updatePlannedStatus(id = planned.id, status = PlanStatus.PENDING, logId = null)
    }
}

/**
 * Takes [planned] off its day. Food planned by hand is deleted; a repeating meal's is skipped instead, so that day
 * isn't filled in again and the schedule stays as it is.
 */
class RemovePlannedFoodUseCase(private val repository: MealPlanRepository) {
    suspend operator fun invoke(planned: PlannedFood) {
        if (planned.repeatingMealId == null) {
            repository.deletePlannedFood(id = planned.id)
        } else {
            repository.updatePlannedStatus(id = planned.id, status = PlanStatus.SKIPPED, logId = null)
        }
    }
}

internal suspend fun MealPlanRepository.markEaten(plannedFood: PlannedFood, foodLogRepository: FoodLogRepository) {
    val food = plannedFood.food
    val logId = foodLogRepository.insertFoodLog(
        foodName = food.foodName,
        mealType = plannedFood.mealType,
        calories = food.calories,
        fat = food.fat,
        carbs = food.carbs,
        protein = food.protein,
        weight = food.weight,
        dominantMacro = food.dominantMacro,
        date = plannedFood.date,
        isQuickAdd = food.isQuickAdd
    )
    updatePlannedStatus(id = plannedFood.id, status = PlanStatus.EATEN, logId = logId)
}
