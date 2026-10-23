package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WeeklyCheckInTest {

    // Thursday, Oct 8, 2026; the window is Sep 17 – Oct 7.
    private val today = LocalDate(year = 2026, month = 10, day = 8)

    private fun daysAgo(days: Int) = today.minus(period = DatePeriod(days = days))

    private fun logs(days: IntRange, calories: Int) = days.map { dummyFoodLog.copy(id = it.toLong(), date = daysAgo(it), calories = calories) }

    /** A weigh-in every third day on a straight line, losing [kgPerDay]. */
    private fun weights(kgPerDay: Double) = (1..21 step 3).map { Weight(date = daysAgo(it), weight = 74.4 + kgPerDay * (it - 1)) }

    private fun checkIn(foodLogs: List<FoodLog>, weights: List<Weight>, goal: Goal = Goal.LOSE_WEIGHT) = weeklyCheckIn(
        today = today,
        foodLogs = foodLogs,
        weights = weights,
        goal = goal,
        goalPace = GoalPace.DEFAULT_KG_PER_MONTH,
        currentTarget = 2150,
        estimatedBurn = 2450
    )

    @Test
    fun measuredBurnAddsTheEnergyOfTheWeightLost() {
        // 0.6 kg over 21 days is 220 kcal a day on top of what was eaten.
        val result = assertIs<WeeklyCheckIn.Result>(checkIn(foodLogs = logs(days = 1..19, calories = 2110), weights = weights(kgPerDay = 0.6 / 21)))
        assertEquals(2110, result.averageIntake)
        assertEquals(220, result.energyFromWeight)
        assertEquals(2330, result.measuredBurn)
        assertEquals(2030, result.suggestedTarget)
        assertEquals(-120, result.targetChange)
        assertEquals(CheckInPace.SLOWER, result.pace)
        assertEquals(-0.6, result.weightChange, absoluteTolerance = 1e-9)
    }

    @Test
    fun todayAndDaysWithoutLogsAreLeftOut() {
        val foodLogs = logs(days = 1..14, calories = 2000) + dummyFoodLog.copy(id = 99, date = today, calories = 5000)
        val result = assertIs<WeeklyCheckIn.Result>(checkIn(foodLogs = foodLogs, weights = weights(kgPerDay = 0.0)))
        assertEquals(14, result.loggedDays)
        assertEquals(2000, result.averageIntake)
        assertEquals(2000, result.measuredBurn)
    }

    @Test
    fun withinATenthOfAKiloOfTheGoalPaceIsOnPace() {
        // Goal: 1.2 kg a month. 1.29 is on pace; 1.31 is faster; 1.09 is slower.
        val onPace = assertIs<WeeklyCheckIn.Result>(checkIn(foodLogs = logs(days = 1..20, calories = 2140), weights = weights(kgPerDay = 1.29 / 30.4)))
        assertEquals(CheckInPace.ON_PACE, onPace.pace)
        val faster = assertIs<WeeklyCheckIn.Result>(checkIn(foodLogs = logs(days = 1..20, calories = 2140), weights = weights(kgPerDay = 1.31 / 30.4)))
        assertEquals(CheckInPace.FASTER, faster.pace)
        val slower = assertIs<WeeklyCheckIn.Result>(checkIn(foodLogs = logs(days = 1..20, calories = 2140), weights = weights(kgPerDay = 1.09 / 30.4)))
        assertEquals(CheckInPace.SLOWER, slower.pace)
    }

    @Test
    fun aSuggestionWithinFiftyKcalOfTheTargetNeedsNoChange() {
        val close = assertIs<WeeklyCheckIn.Result>(checkIn(foodLogs = logs(days = 1..20, calories = 2140), weights = weights(kgPerDay = 1.29 / 30.4)))
        assertTrue(close.isNoChange)
        // 2,330 burned less the goal's 300 is 2,030: 120 under the 2,150 target.
        val lower = assertIs<WeeklyCheckIn.Result>(checkIn(foodLogs = logs(days = 1..19, calories = 2110), weights = weights(kgPerDay = 0.6 / 21)))
        assertFalse(lower.isNoChange)
    }

    @Test
    fun tooFewLoggedDaysOrWeighInsKeepsCollecting() {
        val collecting = assertIs<WeeklyCheckIn.Collecting>(checkIn(foodLogs = logs(days = 1..9, calories = 2000), weights = weights(kgPerDay = 0.0)))
        assertEquals(9, collecting.loggedDays)
        assertEquals(today.minus(period = DatePeriod(days = -5)), collecting.firstCheckIn)
        assertIs<WeeklyCheckIn.Collecting>(checkIn(foodLogs = logs(days = 1..20, calories = 2000), weights = weights(kgPerDay = 0.0).take(n = 3)))
    }

    @Test
    fun maintainingIsOnPaceWhileWeightHoldsSteady() {
        val steady = assertIs<WeeklyCheckIn.Result>(checkIn(foodLogs = logs(days = 1..20, calories = 2400), weights = weights(kgPerDay = 0.0), goal = Goal.MAINTAIN_WEIGHT))
        assertEquals(CheckInPace.ON_PACE, steady.pace)
        assertEquals(2400, steady.suggestedTarget)
        val drifting = assertIs<WeeklyCheckIn.Result>(checkIn(foodLogs = logs(days = 1..20, calories = 2400), weights = weights(kgPerDay = -0.05), goal = Goal.MAINTAIN_WEIGHT))
        assertEquals(CheckInPace.NOT_STEADY, drifting.pace)
    }
}
