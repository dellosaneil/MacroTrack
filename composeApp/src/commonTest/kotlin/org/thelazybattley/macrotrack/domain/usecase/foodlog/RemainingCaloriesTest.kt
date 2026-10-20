package org.thelazybattley.macrotrack.domain.usecase.foodlog

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.dummyFoodLog
import kotlin.test.Test
import kotlin.test.assertEquals

class RemainingCaloriesTest {

    // Thursday, Oct 8, 2026.
    private val today = LocalDate(year = 2026, month = 10, day = 8)

    private fun oct(day: Int) = LocalDate(year = 2026, month = 10, day = day)

    private fun dailyGoal(date: LocalDate, goal: Goal, calories: Int) = DailyGoal(
        date = date,
        goal = goal,
        macroGoals = MacroGoals(calories = calories, protein = 150, carbs = 200, fat = 60)
    )

    // Maintain from Oct 1 and a deficit from Oct 5; the goal today is a surplus.
    private val dailyGoals = listOf(
        dailyGoal(date = oct(1), goal = Goal.MAINTAIN_WEIGHT, calories = 2450),
        dailyGoal(date = oct(5), goal = Goal.LOSE_WEIGHT, calories = 2150)
    )
    private val current = dailyGoal(date = today, goal = Goal.GAIN_WEIGHT, calories = 2750)

    private val foodLogs = listOf(
        dummyFoodLog.copy(id = 1, date = oct(4), calories = 600),
        dummyFoodLog.copy(id = 2, date = oct(4), calories = 400),
        dummyFoodLog.copy(id = 3, date = today, calories = 500)
    )

    private fun remainingOn(date: LocalDate) = remainingCalories(
        date = date,
        today = today,
        foodLogs = foodLogs,
        dailyGoals = dailyGoals,
        current = current
    )

    @Test
    fun todayUsesTheCurrentGoal() {
        assertEquals(2250, remainingOn(date = today))
    }

    @Test
    fun pastDayUsesTheGoalItWasTrackedWith() {
        assertEquals(1450, remainingOn(date = oct(4)))
        assertEquals(2150, remainingOn(date = oct(6)))
    }

    @Test
    fun dayBeforeAnyRecordedGoalUsesTheCurrentGoal() {
        assertEquals(2750, remainingOn(date = LocalDate(year = 2026, month = 9, day = 20)))
    }
}
