package org.thelazybattley.macrotrack.domain.usecase.foodlog

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.withCurrentGoal
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetBudgetBurnUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.GetPlannedFoodsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase

/**
 * Calories left on a day against that day's goal; null until the user's details are set up. A later day has
 * nothing logged, so what's left there is after the food planned for it. Today's includes calories burned when
 * the user added them to the budget.
 */
class GetRemainingCaloriesUseCase(
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val getAllDailyGoalsUseCase: GetAllDailyGoalsUseCase,
    private val getPlannedFoodsUseCase: GetPlannedFoodsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val getBudgetBurnUseCase: GetBudgetBurnUseCase
) {
    suspend operator fun invoke(date: LocalDate): Flow<Int?> = combine(
        flow = getAllFoodLogUseCase(),
        flow2 = getUserDetailsUseCase(),
        flow3 = getAllDailyGoalsUseCase(),
        flow4 = getPlannedFoodsUseCase(),
        flow5 = getBudgetBurnUseCase(date = getCurrentDate())
    ) { foodLogs, userDetails, dailyGoals, plannedFoods, budgetBurn ->
        val today = getCurrentDate()
        userDetails?.let {
            val currentMacroGoals = calculateMacrosGoalUseCase(userDetails = it)
            val planned = if (date > today) {
                plannedFoods.filter { planned -> planned.date == date && planned.isPending }.sumOf { planned -> planned.food.calories }
            } else {
                0
            }
            remainingCalories(
                date = date,
                today = today,
                foodLogs = foodLogs,
                dailyGoals = dailyGoals,
                current = DailyGoal(date = today, goal = it.goal, macroGoals = currentMacroGoals)
            ) - planned + if (date == today) budgetBurn else 0
        }
    }
}

/**
 * [date]'s calorie goal minus what's logged on it. Today uses [current]; a past day uses the goal it was tracked
 * with, or [current] when it's older than every recorded goal.
 */
internal fun remainingCalories(
    date: LocalDate,
    today: LocalDate,
    foodLogs: List<FoodLog>,
    dailyGoals: List<DailyGoal>,
    current: DailyGoal
): Int {
    val goal = withCurrentGoal(records = dailyGoals, today = today, current = current).lastOrNull { it.date <= date } ?: current
    return goal.macroGoals.calories - foodLogs.filter { it.date == date }.sumOf { it.calories }
}
