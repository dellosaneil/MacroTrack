package org.thelazybattley.macrotrack.features.profile.pastgoal

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.GoalPeriod
import org.thelazybattley.macrotrack.domain.model.applyChange
import org.thelazybattley.macrotrack.domain.model.goalPeriods
import org.thelazybattley.macrotrack.domain.model.pastGoalChange
import org.thelazybattley.macrotrack.domain.model.withCurrentGoal
import org.thelazybattley.macrotrack.features.foodlog.resolveDayGoal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PastGoalTest {

    private val today = LocalDate(year = 2026, month = 10, day = 8)
    private fun date(month: Int, day: Int) = LocalDate(year = 2026, month = month, day = day)

    private val maintain = MacroGoals(calories = 2650, protein = 148, carbs = 397, fat = 52)
    private val deficit = MacroGoals(calories = 2350, protein = 148, carbs = 322, fat = 52)

    // Maintain from Mar 2 (re-recorded on Jun 10), Deficit from Oct 5.
    private val records = listOf(
        DailyGoal(date = date(3, 2), goal = Goal.MAINTAIN_WEIGHT, macroGoals = maintain),
        DailyGoal(date = date(6, 10), goal = Goal.MAINTAIN_WEIGHT, macroGoals = maintain),
        DailyGoal(date = date(10, 5), goal = Goal.LOSE_WEIGHT, macroGoals = deficit)
    )
    private val current = DailyGoal(date = today, goal = Goal.LOSE_WEIGHT, macroGoals = deficit)

    private fun change(start: LocalDate, end: LocalDate) = pastGoalChange(
        records = records,
        today = today,
        current = current,
        start = start,
        end = end,
        newGoal = DailyGoal(date = start, goal = Goal.LOSE_WEIGHT, macroGoals = deficit)
    )

    @Test
    fun periodsMergeRepeatedRecordsAndEndToday() {
        assertEquals(
            listOf(
                GoalPeriod(start = date(3, 2), end = date(10, 4), goal = Goal.MAINTAIN_WEIGHT, calories = 2650),
                GoalPeriod(start = date(10, 5), end = today, goal = Goal.LOSE_WEIGHT, calories = 2350)
            ),
            goalPeriods(records = withCurrentGoal(records = records, today = today, current = current), today = today)
        )
    }

    @Test
    fun changingMayToAugustKeepsTheGoalsAroundIt() {
        val change = change(start = date(5, 1), end = date(8, 31))
        assertEquals(DailyGoal(date = date(9, 1), goal = Goal.MAINTAIN_WEIGHT, macroGoals = maintain), change.following)

        val updated = applyChange(records = records, change = change)
        // The Jun 10 record inside the range is replaced.
        assertFalse(updated.any { it.date == date(6, 10) })
        assertEquals(
            listOf(
                GoalPeriod(start = date(3, 2), end = date(4, 30), goal = Goal.MAINTAIN_WEIGHT, calories = 2650),
                GoalPeriod(start = date(5, 1), end = date(8, 31), goal = Goal.LOSE_WEIGHT, calories = 2350),
                GoalPeriod(start = date(9, 1), end = date(10, 4), goal = Goal.MAINTAIN_WEIGHT, calories = 2650),
                GoalPeriod(start = date(10, 5), end = today, goal = Goal.LOSE_WEIGHT, calories = 2350)
            ),
            goalPeriods(records = withCurrentGoal(records = updated, today = today, current = current), today = today)
        )
        // Food log and the calendar read the new goal for those days.
        assertEquals(Goal.LOSE_WEIGHT, resolveDayGoal(date(7, 15), today, updated, current.goal, current.macroGoals)?.goal)
        assertEquals(Goal.MAINTAIN_WEIGHT, resolveDayGoal(date(9, 15), today, updated, current.goal, current.macroGoals)?.goal)
    }

    @Test
    fun noFollowingRecordWhenTheNextDayAlreadyHasOne() {
        assertNull(change(start = date(9, 1), end = date(10, 4)).following)
    }

    @Test
    fun noFollowingRecordWhenTheRangeEndsYesterday() {
        // The day after is today, which always follows the current goal.
        val change = change(start = date(9, 1), end = date(10, 7))
        assertEquals(DailyGoal(date = today, goal = Goal.LOSE_WEIGHT, macroGoals = deficit), change.following)
    }

    @Test
    fun viewStateLimitsDatesToHistoryBeforeToday() {
        val state = PastGoalViewState(
            today = today,
            records = records,
            current = current,
            earliestDate = date(3, 2),
            loggedDates = setOf(date(5, 3), date(7, 1), date(9, 2)),
            start = date(5, 1),
            end = date(8, 31),
            selectedGoal = Goal.LOSE_WEIGHT,
            targets = mapOf(Goal.LOSE_WEIGHT to deficit)
        )
        assertEquals(123, state.dayCount)
        assertEquals(2, state.loggedDayCount)
        assertEquals(date(3, 1), state.months.first())
        assertEquals(date(10, 1), state.months.last())
        assertFalse(state.isSelectable(date = today, side = DateSide.UNTIL))
        assertFalse(state.isSelectable(date = date(3, 1), side = DateSide.FROM))
        assertFalse(state.isSelectable(date = date(9, 1), side = DateSide.FROM))
        assertFalse(state.isSelectable(date = date(4, 30), side = DateSide.UNTIL))
        assertTrue(state.isSelectable(date = date(10, 7), side = DateSide.UNTIL))
        assertEquals(Goal.LOSE_WEIGHT, state.preview.first().goal)
        assertEquals(4, state.preview.size)
    }
}
