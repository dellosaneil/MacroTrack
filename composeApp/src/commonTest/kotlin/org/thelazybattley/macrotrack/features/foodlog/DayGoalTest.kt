package org.thelazybattley.macrotrack.features.foodlog

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Goal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DayGoalTest {

    private val today = LocalDate(2026, 10, 9)
    private val deficit = MacroGoals(calories = 2350, protein = 148, carbs = 322, fat = 52)
    private val maintain = MacroGoals(calories = 2650, protein = 148, carbs = 397, fat = 52)
    private val surplus = MacroGoals(calories = 2950, protein = 148, carbs = 472, fat = 52)

    private val history = listOf(
        DailyGoal(date = LocalDate(2026, 10, 1), goal = Goal.MAINTAIN_WEIGHT, macroGoals = maintain),
        DailyGoal(date = LocalDate(2026, 10, 5), goal = Goal.LOSE_WEIGHT, macroGoals = deficit)
    )

    private fun resolve(date: LocalDate) = resolveDayGoal(
        date = date,
        today = today,
        dailyGoals = history,
        currentGoal = Goal.GAIN_WEIGHT,
        currentMacroGoals = surplus
    )

    @Test
    fun todayUsesCurrentGoal() {
        val dayGoal = resolve(today)!!
        assertEquals(Goal.GAIN_WEIGHT, dayGoal.goal)
        assertFalse(dayGoal.isEstimated)
    }

    @Test
    fun pastDayUsesGoalRecordedThatDay() {
        assertEquals(Goal.LOSE_WEIGHT, resolve(LocalDate(2026, 10, 5))!!.goal)
    }

    @Test
    fun pastDayWithoutRecordUsesMostRecentEarlierRecord() {
        val dayGoal = resolve(LocalDate(2026, 10, 3))!!
        assertEquals(Goal.MAINTAIN_WEIGHT, dayGoal.goal)
        assertEquals(2650, dayGoal.macroGoals.calories)
        assertFalse(dayGoal.isEstimated)
    }

    @Test
    fun dayBeforeAnyRecordFallsBackToCurrentAndIsEstimated() {
        val dayGoal = resolve(LocalDate(2026, 9, 20))!!
        assertEquals(Goal.GAIN_WEIGHT, dayGoal.goal)
        assertTrue(dayGoal.isEstimated)
    }

    @Test
    fun noUserMeansNoGoal() {
        assertNull(resolveDayGoal(today, today, emptyList(), null, null))
    }
}
