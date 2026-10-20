package org.thelazybattley.macrotrack.domain.usecase.foodlog

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.withCurrentGoal
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase

/** Calories left on a day against that day's goal; null until the user's details are set up. */
class GetRemainingCaloriesUseCase(
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val getAllDailyGoalsUseCase: GetAllDailyGoalsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase
) {
    suspend operator fun invoke(date: LocalDate): Flow<Int?> = combine(
        flow = getAllFoodLogUseCase(),
        flow2 = getUserDetailsUseCase(),
        flow3 = getAllDailyGoalsUseCase()
    ) { foodLogs, userDetails, dailyGoals ->
        val today = getCurrentDate()
        userDetails?.let {
            val currentMacroGoals = calculateMacrosGoalUseCase(
                height = it.height,
                weight = it.weight,
                age = it.age,
                gender = it.sex,
                activityLevel = it.activityLevel,
                goal = it.goal,
                goalPace = it.goalPace
            )
            remainingCalories(
                date = date,
                today = today,
                foodLogs = foodLogs,
                dailyGoals = dailyGoals,
                current = DailyGoal(date = today, goal = it.goal, macroGoals = currentMacroGoals)
            )
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
