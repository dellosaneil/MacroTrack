package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GoalForecastTest {

    private val today = LocalDate(year = 2026, month = 10, day = 8)

    private fun day(daysAgo: Int) = today.minus(period = DatePeriod(days = daysAgo))

    /** A weigh-in every day for [days] days up to today, moving [perDay] kg a day from [start]. */
    private fun steady(days: Int, start: Double, perDay: Double) =
        (0 until days).map { i -> Weight(date = day(daysAgo = days - 1 - i), weight = start + perDay * i) }

    private fun forecast(
        weights: List<Weight>,
        target: Double? = 70.0,
        startWeight: Double? = weights.firstOrNull()?.weight,
        startDate: LocalDate? = weights.firstOrNull()?.date,
        basis: ForecastBasis = ForecastBasis.LAST_4_WEEKS,
        foodLogs: List<FoodLog> = emptyList()
    ) = goalForecast(
        today = today,
        weights = weights,
        foodLogs = foodLogs,
        targetWeight = target,
        startWeight = startWeight,
        startDate = startDate,
        maintenance = 2650,
        basis = basis
    )

    @Test
    fun noGoalOrNoWeighInsGivesNoForecast() {
        assertNull(forecast(weights = steady(days = 30, start = 80.0, perDay = -0.1), target = null))
        assertNull(forecast(weights = emptyList()))
    }

    @Test
    fun steadyLossGivesADateFromThePace() {
        val result = assertNotNull(forecast(weights = steady(days = 60, start = 80.0, perDay = -0.1)))
        val status = assertIs<ForecastStatus.OnTheWay>(result.status)
        assertEquals(-0.7, result.pace!!, absoluteTolerance = 0.001)
        assertEquals(0.7, status.kgPerWeek, absoluteTolerance = 0.001)
        // Trend weight trails the last weigh-in by half the 7-day window.
        val expectedDays = kotlin.math.ceil(result.remainingKg / 0.7 * 7).toInt()
        assertEquals(today.plus(period = DatePeriod(days = expectedDays)), status.date)
        assertTrue(status.earliest < status.date && status.date < status.latest)
    }

    @Test
    fun underFourteenWeighInsIsStillCollecting() {
        val result = assertNotNull(forecast(weights = steady(days = 8, start = 82.0, perDay = -0.1)))
        val status = assertIs<ForecastStatus.Collecting>(result.status)
        assertNull(status.roughDate)
    }

    @Test
    fun collectingGivesARoughGuessFromCalories() {
        val logs = (1..10).map { daysAgo ->
            FoodLog(
                mealType = MealType.LUNCH, calories = 2150, protein = 0.0, carbs = 0.0, fat = 0.0, id = daysAgo.toLong(),
                foodName = "Food", weight = 100.0, dominantMacro = MacroType.CARBS, date = day(daysAgo = daysAgo)
            )
        }
        val result = assertNotNull(forecast(weights = steady(days = 8, start = 82.0, perDay = -0.1), foodLogs = logs))
        assertNotNull(assertIs<ForecastStatus.Collecting>(result.status).roughDate)
        assertEquals(-500 * 7 / 7700.0, result.paceByBasis[ForecastBasis.FROM_CALORIES]!!, absoluteTolerance = 0.001)
    }

    @Test
    fun threeFlatWeeksGiveNoDate() {
        val weights = (0 until 30).map { i -> Weight(date = day(daysAgo = 29 - i), weight = 74.2 + if (i % 2 == 0) 0.05 else -0.05) }
        assertEquals(ForecastStatus.Flat, forecast(weights = weights, startWeight = 78.0)!!.status)
    }

    @Test
    fun gainingWhileTryingToLoseIsTheWrongWay() {
        assertEquals(ForecastStatus.WrongWay, forecast(weights = steady(days = 30, start = 76.0, perDay = 0.05), startWeight = 78.0)!!.status)
    }

    @Test
    fun goalReachedOnTheDayTheTrendCrossedIt() {
        val result = assertNotNull(forecast(weights = steady(days = 60, start = 75.0, perDay = -0.1)))
        val status = assertIs<ForecastStatus.Reached>(result.status)
        assertTrue(status.date <= today)
        assertTrue(result.trend.first { it.date == status.date }.weight <= 70.0)
    }

    @Test
    fun gainingTowardAHigherGoalWorksTheSameWay() {
        val result = assertNotNull(forecast(weights = steady(days = 40, start = 60.0, perDay = 0.05), target = 65.0))
        assertEquals(1, result.direction)
        assertIs<ForecastStatus.OnTheWay>(result.status)
    }

    @Test
    fun whatIfWarnsPastOnePercentAWeek() {
        val result = assertNotNull(forecast(weights = steady(days = 60, start = 80.0, perDay = -0.1)))
        val range = result.whatIfRange()
        assertEquals(1500, range.first)
        assertEquals(2650, range.last)
        val gentle = result.whatIf(kcal = 2150)
        assertFalse(gentle.isTooFast)
        assertEquals(500 * 7 / 7700.0, gentle.kgPerWeek, absoluteTolerance = 0.001)
        val fast = result.whatIf(kcal = 1500)
        assertTrue(fast.isTooFast)
        assertFalse(result.whatIf(kcal = fast.safeKcal).isTooFast)
        assertEquals(0, fast.safeKcal % FORECAST_KCAL_STEP)
        assertNull(result.whatIf(kcal = 2650).date)
    }

    @Test
    fun deadlineTooSoonOffersTheEarliestSafeDate() {
        val result = assertNotNull(forecast(weights = steady(days = 60, start = 80.0, perDay = -0.1)))
        val soon = result.deadlinePlan(date = today.plus(period = DatePeriod(days = 14)))
        assertTrue(soon.isTooSoon)
        assertNotNull(soon.earliestSafeDate)
        val later = result.deadlinePlan(date = today.plus(period = DatePeriod(days = 200)))
        assertFalse(later.isTooSoon)
        assertEquals(0, later.kcal % 10)
        assertTrue(later.kcal < 2650)
    }

    @Test
    fun milestonesRunFromStartToGoalWithoutNearDuplicates() {
        val weights = steady(days = 60, start = 80.0, perDay = -0.1)
        val result = assertNotNull(forecast(weights = weights))
        val milestones = result.milestones(unitSystem = UnitSystem.METRIC, custom = emptyList())
        assertEquals(
            listOf(MilestoneKind.FIRST_UNIT, MilestoneKind.FIVE_PERCENT, MilestoneKind.FIVE_UNITS, MilestoneKind.TEN_PERCENT, MilestoneKind.GOAL),
            milestones.map { it.kind }
        )
        assertTrue(milestones.first().isReached)
        assertNull(milestones.last().reachedOn)
        assertNotNull(milestones.last().expectedOn)
        milestones.zipWithNext().forEach { (a, b) -> assertTrue(abs(a.weight - b.weight) >= 0.3) }
    }

    @Test
    fun customMilestoneShowsWhetherItIsOnTrack() {
        val result = assertNotNull(forecast(weights = steady(days = 60, start = 80.0, perDay = -0.1)))
        val soon = today.plus(period = DatePeriod(days = 14))
        val custom = listOf(
            CustomMilestone(id = 1, name = "Easy", weight = 73.5, byDate = soon),
            CustomMilestone(id = 2, name = "Hard", weight = 71.0, byDate = soon)
        )
        val tracks = result.milestones(unitSystem = UnitSystem.METRIC, custom = custom)
            .filter { it.kind == MilestoneKind.CUSTOM }
            .associate { it.custom!!.name to it.track }
        assertEquals(MilestoneTrack.ON_TRACK, tracks["Easy"])
        assertEquals(MilestoneTrack.BEHIND, tracks["Hard"])
    }

    @Test
    fun appliedCaloriesComeOutExactlyAsCalculatedPace() {
        listOf(50, 500, 1150).forEach { difference ->
            assertEquals(difference, GoalPace.dailyCalories(kgPerMonth = difference * 30.4 / 7700.0))
        }
    }
}
