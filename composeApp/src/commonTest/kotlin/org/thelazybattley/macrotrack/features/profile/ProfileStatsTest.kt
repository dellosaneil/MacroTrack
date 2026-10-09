package org.thelazybattley.macrotrack.features.profile

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.domain.model.Weight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileStatsTest {

    // Thursday
    private val today = LocalDate(year = 2026, month = 10, day = 8)

    private fun log(date: LocalDate, calories: Int, protein: Double = 30.0) = FoodLog(
        mealType = MealType.LUNCH,
        calories = calories,
        protein = protein,
        carbs = 0.0,
        fat = 0.0,
        id = 0,
        foodName = "Food",
        weight = 100.0,
        dominantMacro = MacroType.PROTEIN,
        date = date
    )

    private fun user(goal: Goal, weight: Double = 74.2, target: Double? = 69.6) = UserDetails(
        weight = weight,
        age = 29,
        height = 175.0,
        sex = UserSex.MALE,
        activityLevel = ActivityLevel.MODERATELY_ACTIVE,
        goal = goal,
        targetWeight = target
    )

    @Test
    fun weekSummaryCountsStreaksAndAverages() {
        val logs = listOf(
            log(LocalDate(2026, 9, 20), 2000),
            log(LocalDate(2026, 10, 5), 2300),
            log(LocalDate(2026, 10, 6), 2400),
            log(LocalDate(2026, 10, 7), 1500),
            log(LocalDate(2026, 10, 8), 1200),
            log(LocalDate(2026, 10, 8), 1100)
        )
        val summary = calculateWeekSummary(
            foodLogs = logs,
            weights = emptyList(),
            macroGoals = MacroGoals(calories = 2350, protein = 148, carbs = 0, fat = 0),
            today = today
        )

        assertEquals(LocalDate(2026, 10, 5), summary.weekStart)
        assertEquals(listOf(true, true, true, true, false, false, false), summary.loggedDays)
        assertEquals(3, summary.todayIndex)
        assertEquals(4, summary.currentStreak)
        assertEquals(4, summary.bestStreak)
        assertEquals(4, summary.daysLogged)
        assertEquals((2300 + 2400 + 1500 + 2300) / 4, summary.averageCalories)
        // 2300, 2400 and 2300 are within 10% of 2350; 1500 is not.
        assertEquals(3, summary.daysOnTarget)
        assertNull(summary.weightChange)
    }

    @Test
    fun streakContinuesFromYesterdayWhenTodayIsNotLogged() {
        val logs = listOf(log(LocalDate(2026, 10, 6), 2000), log(LocalDate(2026, 10, 7), 2000))
        val summary = calculateWeekSummary(foodLogs = logs, weights = emptyList(), macroGoals = null, today = today)
        assertEquals(2, summary.currentStreak)
    }

    @Test
    fun weekWeightChangeUsesLastWeightBeforeTheWeek() {
        val weights = listOf(
            Weight(date = LocalDate(2026, 10, 1), weight = 74.8),
            Weight(date = LocalDate(2026, 10, 5), weight = 74.5),
            Weight(date = LocalDate(2026, 10, 8), weight = 74.2)
        )
        val summary = calculateWeekSummary(foodLogs = emptyList(), weights = weights, macroGoals = null, today = today)
        assertEquals(-0.6, summary.weightChange!!, absoluteTolerance = 0.0001)
    }

    @Test
    fun goalProgressStartsFromWeightWhenTargetWasSet() {
        val progress = calculateGoalProgress(
            userDetails = user(Goal.LOSE_WEIGHT).copy(
                targetStartWeight = 78.0,
                targetSetDate = LocalDate(2026, 7, 6)
            ),
            today = today
        )!!

        assertEquals(78.0, progress.startWeight)
        assertEquals(LocalDate(2026, 7, 6), progress.startDate)
        assertEquals(3.8, progress.achievedKg, absoluteTolerance = 0.0001)
        assertEquals(8.4, progress.totalKg, absoluteTolerance = 0.0001)
        assertEquals(GoalStatus.ON_TRACK, progress.status)
        // 4.6 kg left at 300 kcal/day (~0.273 kg/week) is 118.07 days, rounded up to 119.
        assertEquals(LocalDate(2027, 2, 4), progress.estimatedDate)
    }

    @Test
    fun goalProgressWithoutRecordedStartUsesCurrentWeight() {
        val progress = calculateGoalProgress(userDetails = user(Goal.LOSE_WEIGHT), today = today)!!
        assertEquals(74.2, progress.startWeight)
        assertNull(progress.startDate)
        assertEquals(0.0, progress.achievedKg)
    }

    @Test
    fun goalProgressFlagsGoalThatDoesNotMatchTarget() {
        val progress = calculateGoalProgress(userDetails = user(Goal.GAIN_WEIGHT), today = today)!!
        assertEquals(GoalStatus.GOAL_MISMATCH, progress.status)
        assertNull(progress.estimatedDate)
    }

    @Test
    fun goalProgressIsReachedWhenPastTarget() {
        val progress = calculateGoalProgress(
            userDetails = user(Goal.LOSE_WEIGHT, weight = 69.0).copy(targetStartWeight = 78.0),
            today = today
        )!!
        assertEquals(GoalStatus.REACHED, progress.status)
        assertEquals(1f, progress.progress)
    }

    @Test
    fun goalProgressIsNullWithoutTarget() {
        assertNull(calculateGoalProgress(userDetails = user(Goal.LOSE_WEIGHT, target = null), today = today))
    }
}
