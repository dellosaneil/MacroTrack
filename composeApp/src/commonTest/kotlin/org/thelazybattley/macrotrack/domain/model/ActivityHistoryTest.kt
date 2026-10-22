package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ActivityHistoryTest {

    // Thursday, Oct 8, 2026.
    private val today = LocalDate(year = 2026, month = 10, day = 8)

    private fun day(month: Int, day: Int) = LocalDate(year = 2026, month = month, day = day)

    private fun run(meters: Double, seconds: Int) = ActivitySession(
        type = ActivityType.RUN, startMillis = 0, endMillis = seconds * 1000L, movingSeconds = seconds,
        distanceMeters = meters, steps = (meters / 1.05).toInt(), climbMeters = 0.0
    )

    @Test
    fun aRangeEndsTodayAndComparesWithTheDaysJustBefore() {
        val week = HistoryRange.WEEK.period(today = today, firstDate = null)
        assertEquals(HistoryPeriod(start = day(10, 2), end = today), week)
        assertEquals(HistoryPeriod(start = day(9, 25), end = day(10, 1)), HistoryRange.WEEK.previous(period = week))
    }

    @Test
    fun allTimeStartsAtTheFirstRecordingWithNothingToCompare() {
        val all = HistoryRange.ALL.period(today = today, firstDate = day(9, 1))
        assertEquals(38, all.days)
        assertNull(HistoryRange.ALL.previous(period = all))
    }

    @Test
    fun ninetyDaysChartsByWeekAndAllTimeByMonth() {
        val quarter = HistoryRange.QUARTER.period(today = today, firstDate = null)
        val weeks = buckets(sessions = emptyList(), period = quarter, size = HistoryRange.QUARTER.bucketSize)
        assertEquals(13, weeks.size)
        assertEquals(today, weeks.last().end)
        assertEquals(quarter.start, weeks.first().start)

        val all = HistoryRange.ALL.period(today = today, firstDate = day(9, 14))
        val months = buckets(sessions = emptyList(), period = all, size = HistoryRange.ALL.bucketSize)
        assertEquals(listOf(day(9, 14) to day(9, 30), day(10, 1) to today), months.map { it.start to it.end })
    }

    @Test
    fun longestStreakCountsDaysInARow() {
        val dates = setOf(day(10, 2), day(10, 3), day(10, 5), day(10, 6), day(10, 7))
        assertEquals(3, longestStreak(dates = dates, period = HistoryPeriod(start = day(10, 2), end = today)))
    }

    @Test
    fun changeIsAgainstThePeriodBefore() {
        assertEquals(10, percentChange(current = 110.0, previous = 100.0))
        assertEquals(-25, percentChange(current = 75.0, previous = 100.0))
        assertNull(percentChange(current = 50.0, previous = 0.0))
    }

    @Test
    fun theFastestRunHasToBeAtLeastAKilometre() {
        val sprint = run(meters = 400.0, seconds = 80)
        val fiveK = run(meters = 5_000.0, seconds = 1_500)
        val tenK = run(meters = 10_000.0, seconds = 3_300)
        val bests = PersonalBests.of(sessions = listOf(sprint, fiveK, tenK), profile = null)
        assertEquals(fiveK, bests.fastestRun)
        assertEquals(tenK, bests.longestRun)
        assertNull(bests.longestWalk)
        assertNull(bests.mostBurned)
    }

    @Test
    fun averagePaceWeighsByDistance() {
        assertEquals(320.0, averagePace(sessions = listOf(run(meters = 5_000.0, seconds = 1_500), run(meters = 10_000.0, seconds = 3_300))))
        assertNull(averagePace(sessions = emptyList()))
    }
}
