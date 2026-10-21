package org.thelazybattley.macrotrack.domain.usecase.mealplan

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.PlannedFood
import org.thelazybattley.macrotrack.domain.model.RepeatingMeal
import org.thelazybattley.macrotrack.domain.model.datesToFill
import org.thelazybattley.macrotrack.domain.repository.FoodLogRepository
import org.thelazybattley.macrotrack.domain.repository.MealPlanRepository

class GetRepeatingMealsUseCase(private val repository: MealPlanRepository) {
    operator fun invoke(): Flow<List<RepeatingMeal>> = repository.getRepeatingMeals()
}

/** Screens sync as they open, sometimes together; one at a time keeps a day from being filled in twice. */
private val syncLock = Mutex()

/**
 * Fills the plan in from the repeating meals: each one's food on its days over the coming week. The ones that log
 * themselves then log whatever they have waiting on today or earlier.
 */
class SyncRepeatingMealsUseCase(
    private val repository: MealPlanRepository,
    private val foodLogRepository: FoodLogRepository
) {
    suspend operator fun invoke() = syncLock.withLock {
        val today = getCurrentDate()
        val meals = repository.getRepeatingMeals().first()
        val existing = repository.getPlannedFoods().first()
        meals.forEach { meal ->
            meal.datesToFill(existing = existing, today = today).forEach { date ->
                meal.foods.forEach { food ->
                    repository.insertPlannedFood(
                        plannedFood = PlannedFood(id = 0, date = date, mealType = meal.mealType, food = food, repeatingMealId = meal.id)
                    )
                }
            }
        }
        val autoLogIds = meals.filter { it.isAutoLog && it.isActive }.map { it.id }.toSet()
        if (autoLogIds.isEmpty()) return@withLock
        repository.getPlannedFoods().first()
            .filter { it.isPending && it.date <= today && it.repeatingMealId in autoLogIds }
            .forEach { repository.markEaten(plannedFood = it, foodLogRepository = foodLogRepository) }
    }
}

/**
 * Adds [RepeatingMeal] or saves changes to one. Changes apply from today: what it planned from today on and is
 * still waiting is taken out and filled in again; days already eaten or skipped stay as they were.
 */
class SaveRepeatingMealUseCase(
    private val repository: MealPlanRepository,
    private val syncRepeatingMealsUseCase: SyncRepeatingMealsUseCase,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(repeatingMeal: RepeatingMeal): Long {
        val today = getCurrentDate()
        if (repeatingMeal.id != 0L) repository.deletePendingFrom(repeatingMealId = repeatingMeal.id, fromDate = today)
        val id = repository.saveRepeatingMeal(repeatingMeal = repeatingMeal.copy(updatedOn = today))
        syncRepeatingMealsUseCase()
        telemetry.logEvent(
            name = TelemetryEvent.REPEATING_MEAL_SAVED,
            params = mapOf(
                TelemetryEvent.PARAM_MEAL_TYPE to repeatingMeal.mealType.name.lowercase(),
                TelemetryEvent.PARAM_RESULT to if (repeatingMeal.isAutoLog) "log" else "plan"
            )
        )
        return id
    }
}

/** Pauses or resumes a repeating meal. Paused, it takes back what it planned and is still waiting, from today. */
class SetRepeatingMealActiveUseCase(
    private val repository: MealPlanRepository,
    private val syncRepeatingMealsUseCase: SyncRepeatingMealsUseCase
) {
    suspend operator fun invoke(id: Long, isActive: Boolean) {
        val today = getCurrentDate()
        repository.setRepeatingMealActive(id = id, isActive = isActive, updatedOn = today)
        if (isActive) {
            syncRepeatingMealsUseCase()
        } else {
            repository.deletePendingFrom(repeatingMealId = id, fromDate = today)
        }
    }
}

/** Stops a repeating meal for good; what it already logged stays in the food log. */
class DeleteRepeatingMealUseCase(private val repository: MealPlanRepository) {
    suspend operator fun invoke(id: Long) {
        repository.deletePendingFrom(repeatingMealId = id, fromDate = getCurrentDate())
        repository.deleteRepeatingMeal(id = id)
    }
}
