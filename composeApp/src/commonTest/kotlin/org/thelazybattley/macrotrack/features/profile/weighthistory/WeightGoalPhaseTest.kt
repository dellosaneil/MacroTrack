package org.thelazybattley.macrotrack.features.profile.weighthistory

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Goal
import kotlin.test.Test
import kotlin.test.assertEquals

class WeightGoalPhaseTest {

    private val today = LocalDate(2026, 10, 14)

    private fun record(day: Int, goal: Goal, calories: Int = 2000) = DailyGoal(
        date = LocalDate(2026, 10, day),
        goal = goal,
        macroGoals = MacroGoals(calories = calories, protein = 150, carbs = 200, fat = 60)
    )

    @Test
    fun noRecordsUsesTheCurrentGoalForToday() {
        assertEquals(
            listOf(GoalPhase(start = today, end = today, goal = Goal.LOSE_WEIGHT)),
            goalPhases(records = emptyList(), today = today, currentGoal = Goal.LOSE_WEIGHT)
        )
    }

    @Test
    fun mergesRecordsWithTheSameGoalEvenWhenCaloriesDiffer() {
        val phases = goalPhases(
            records = listOf(
                record(day = 1, goal = Goal.MAINTAIN_WEIGHT, calories = 2200),
                record(day = 4, goal = Goal.MAINTAIN_WEIGHT, calories = 2300),
                record(day = 8, goal = Goal.LOSE_WEIGHT)
            ),
            today = today,
            currentGoal = Goal.LOSE_WEIGHT
        )

        assertEquals(
            listOf(
                GoalPhase(start = LocalDate(2026, 10, 1), end = LocalDate(2026, 10, 7), goal = Goal.MAINTAIN_WEIGHT),
                GoalPhase(start = LocalDate(2026, 10, 8), end = today, goal = Goal.LOSE_WEIGHT)
            ),
            phases
        )
    }

    @Test
    fun currentGoalReplacesTodaysRecord() {
        val phases = goalPhases(
            records = listOf(record(day = 1, goal = Goal.MAINTAIN_WEIGHT), record(day = 14, goal = Goal.MAINTAIN_WEIGHT)),
            today = today,
            currentGoal = Goal.GAIN_WEIGHT
        )

        assertEquals(
            listOf(
                GoalPhase(start = LocalDate(2026, 10, 1), end = LocalDate(2026, 10, 13), goal = Goal.MAINTAIN_WEIGHT),
                GoalPhase(start = today, end = today, goal = Goal.GAIN_WEIGHT)
            ),
            phases
        )
    }
}
