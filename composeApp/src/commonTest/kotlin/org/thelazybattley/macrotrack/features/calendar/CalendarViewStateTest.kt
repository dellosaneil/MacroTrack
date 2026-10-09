package org.thelazybattley.macrotrack.features.calendar

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CalendarViewStateTest {

    // Thursday, Oct 8, 2026.
    private val today = LocalDate(year = 2026, month = 10, day = 8)

    private fun oct(day: Int) = LocalDate(year = 2026, month = 10, day = day)
    private fun sep(day: Int) = LocalDate(year = 2026, month = 9, day = day)

    private fun summary(date: LocalDate, calories: Int, goal: Int? = 2150) = DaySummary(
        date = date, calories = calories, protein = 0.0, carbs = 0.0, fat = 0.0, mealCount = 3, goalCalories = goal
    )

    // Sep 1–16 logged (best run), Oct 1–2 and Oct 5–8 logged.
    private val days = ((1..16).map { sep(it) } + listOf(oct(1), oct(2), oct(5), oct(6), oct(7), oct(8)))
        .associateWith { summary(date = it, calories = 2150) }
        .plus(oct(2) to summary(date = oct(2), calories = 2860))
        .plus(oct(1) to summary(date = oct(1), calories = 1500, goal = null))

    private val state = CalendarViewState(today = today, days = days)

    @Test
    fun streakCountsBackFromToday() {
        assertEquals(StreakState.LOGGED_TODAY, state.streakState)
        assertEquals(StreakRun(start = oct(5), end = oct(8), length = 4), state.currentStreak)
        assertEquals(StreakRun(start = sep(1), end = sep(16), length = 16), state.bestStreak)
    }

    @Test
    fun streakIsAtRiskUntilTodayEnds() {
        val atRisk = state.copy(days = days - oct(8))
        assertEquals(StreakState.AT_RISK, atRisk.streakState)
        assertEquals(3, atRisk.currentStreak?.length)
    }

    @Test
    fun streakEndsAfterAMissedDay() {
        val ended = state.copy(days = days - oct(8) - oct(7))
        assertEquals(StreakState.ENDED, ended.streakState)
        assertNull(ended.currentStreak)
        assertEquals(StreakRun(start = oct(5), end = oct(6), length = 2), ended.lastBrokenStreak)
    }

    @Test
    fun dayStatusComparesWithThatDaysGoal() {
        assertEquals(DayStatus.ON_TARGET, state.statusOf(date = oct(7)))
        assertEquals(DayStatus.OFF_TARGET, state.statusOf(date = oct(2)))
        assertEquals(DayStatus.LOGGED, state.statusOf(date = oct(1)))
        assertEquals(DayStatus.NOT_LOGGED, state.statusOf(date = oct(3)))
        assertEquals(DayStatus.TODAY, state.statusOf(date = today))
        assertEquals(DayStatus.TODAY_EMPTY, state.copy(days = days - today).statusOf(date = today))
        assertEquals(DayStatus.FUTURE, state.statusOf(date = oct(9)))
    }

    @Test
    fun monthStatsLeaveTodayOutOfTargetsAndAverage() {
        val stats = state.monthStats
        assertEquals(6, stats.daysLogged)
        assertEquals(8, stats.daysSoFar)
        // Oct 5–7 on target; Oct 2 over; Oct 1 has no goal; today isn't judged.
        assertEquals(3, stats.daysOnTarget)
        assertEquals(4, stats.daysWithGoal)
        assertEquals((1500 + 2860 + 2150 * 3) / 5, stats.averageCalories)
    }

    @Test
    fun monthGridStartsOnMonday() {
        // Oct 1, 2026 is a Thursday: three empty cells first.
        assertEquals(listOf(null, null, null, oct(1)), state.monthCells.take(4))
        assertEquals(34, state.monthCells.size)
    }

    @Test
    fun monthNavigationStaysWithinLoggedHistory() {
        assertTrue(state.canGoBack)
        assertFalse(state.canGoForward)
        val september = state.copy(visibleMonth = sep(1))
        assertFalse(september.canGoBack)
        assertTrue(september.canGoForward)
    }

    @Test
    fun onlyLoggedDaysAndTodayCanBeOpened() {
        assertTrue(state.copy(selectedDate = oct(7)).canOpenSelected)
        assertFalse(state.copy(selectedDate = oct(3)).canOpenSelected)
        assertTrue(state.copy(days = days - today, selectedDate = today).canOpenSelected)
    }
}
