package org.thelazybattley.macrotrack.features.profile

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
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
import kotlin.test.assertTrue

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
    fun goalProgressEstimatesFromWeightTrendSinceStart() {
        // Steady 0.3 kg a week loss from 78.0 kg on Jul 6.
        val startDate = LocalDate(2026, 7, 6)
        val weights = (1..13).map { week ->
            Weight(date = startDate.plus(DatePeriod(days = week * 7)), weight = 78.0 - week * 0.3)
        }
        val progress = calculateGoalProgress(
            userDetails = user(Goal.LOSE_WEIGHT, weight = 74.1).copy(
                targetStartWeight = 78.0,
                targetSetDate = startDate
            ),
            weights = weights,
            today = today
        )!!

        assertEquals(78.0, progress.startWeight)
        assertEquals(startDate, progress.startDate)
        assertEquals(3.9, progress.achievedKg, absoluteTolerance = 0.0001)
        assertEquals(8.4, progress.totalKg, absoluteTolerance = 0.0001)
        assertEquals(GoalStatus.ON_TRACK, progress.status)
        assertEquals(-0.3, progress.weeklyChange!!, absoluteTolerance = 0.0001)
        // 4.5 kg left at 0.3 kg/week is 105 days.
        assertEquals(today.plus(DatePeriod(days = 105)), progress.estimatedDate)
    }

    @Test
    fun weeklyTrendSmoothsOutDailyFluctuation() {
        val startDate = LocalDate(2026, 9, 24)
        val weights = listOf(
            Weight(date = LocalDate(2026, 9, 27), weight = 79.0),
            Weight(date = LocalDate(2026, 10, 1), weight = 78.2),
            Weight(date = LocalDate(2026, 10, 4), weight = 78.9),
            Weight(date = LocalDate(2026, 10, 8), weight = 77.6)
        )
        val trend = weeklyTrend(startDate = startDate, startWeight = 79.5, weights = weights, today = today)!!
        assertTrue(trend < 0)
        // The noisy last two weigh-ins alone would suggest −2.6 kg a week.
        assertTrue(trend > -1.0)
    }

    @Test
    fun goalProgressNeedsAWeekOfWeighIns() {
        val progress = calculateGoalProgress(
            userDetails = user(Goal.LOSE_WEIGHT, weight = 77.5).copy(
                targetStartWeight = 78.0,
                targetSetDate = LocalDate(2026, 10, 4)
            ),
            weights = listOf(Weight(date = LocalDate(2026, 10, 8), weight = 77.5)),
            today = today
        )!!
        assertEquals(GoalStatus.NEEDS_MORE_DATA, progress.status)
        assertNull(progress.weeklyChange)
        assertNull(progress.estimatedDate)
    }

    @Test
    fun goalProgressWithoutRecordedStartUsesCurrentWeight() {
        val progress = calculateGoalProgress(userDetails = user(Goal.LOSE_WEIGHT), weights = emptyList(), today = today)!!
        assertEquals(74.2, progress.startWeight)
        assertNull(progress.startDate)
        assertEquals(0.0, progress.achievedKg)
        assertEquals(GoalStatus.NEEDS_MORE_DATA, progress.status)
    }

    @Test
    fun goalProgressIsOffTrackWhenTrendMovesAwayFromTarget() {
        val startDate = LocalDate(2026, 9, 1)
        val progress = calculateGoalProgress(
            userDetails = user(Goal.LOSE_WEIGHT, weight = 75.0).copy(
                targetStartWeight = 74.0,
                targetSetDate = startDate
            ),
            weights = listOf(
                Weight(date = LocalDate(2026, 9, 15), weight = 74.5),
                Weight(date = LocalDate(2026, 10, 8), weight = 75.0)
            ),
            today = today
        )!!
        assertEquals(GoalStatus.OFF_TRACK, progress.status)
        assertTrue(progress.weeklyChange!! > 0)
        assertNull(progress.estimatedDate)
    }

    @Test
    fun goalProgressIsReachedWhenPastTarget() {
        val progress = calculateGoalProgress(
            userDetails = user(Goal.LOSE_WEIGHT, weight = 69.0).copy(targetStartWeight = 78.0),
            weights = emptyList(),
            today = today
        )!!
        assertEquals(GoalStatus.REACHED, progress.status)
        assertEquals(1f, progress.progress)
    }

    @Test
    fun startWeightUsesLastWeighInOnOrBeforeDate() {
        val weights = listOf(
            Weight(date = LocalDate(2026, 9, 1), weight = 80.0),
            Weight(date = LocalDate(2026, 9, 10), weight = 79.0),
            Weight(date = LocalDate(2026, 9, 20), weight = 78.0)
        )
        assertEquals(79.0, startWeightOn(date = LocalDate(2026, 9, 15), weights = weights)?.weight)
        assertEquals(78.0, startWeightOn(date = LocalDate(2026, 9, 20), weights = weights)?.weight)
        assertEquals(80.0, startWeightOn(date = LocalDate(2026, 8, 1), weights = weights)?.weight)
        assertNull(startWeightOn(date = today, weights = emptyList()))
    }

    @Test
    fun goalProgressIsNullWithoutTarget() {
        assertNull(calculateGoalProgress(userDetails = user(Goal.LOSE_WEIGHT, target = null), weights = emptyList(), today = today))
    }
}
