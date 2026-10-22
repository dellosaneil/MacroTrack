package org.thelazybattley.macrotrack.domain.model

import org.thelazybattley.macrotrack.domain.repository.StepSensorReading
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StepCountingTest {

    private val hour = 3_600_000L

    @Test
    fun countsTheDifferenceSinceTheLastReading() {
        assertEquals(500, stepsSince(previous = StepSensorReading(count = 1_000, atMillis = 0), count = 1_500))
    }

    @Test
    fun theFirstReadingStartsFromThere() {
        assertEquals(0, stepsSince(previous = null, count = 12_345))
    }

    @Test
    fun aLowerCountMeansThePhoneRestarted() {
        assertEquals(200, stepsSince(previous = StepSensorReading(count = 9_000, atMillis = 0), count = 200))
    }

    @Test
    fun stepsWhileTheAppWasClosedAreSharedOverTheHoursInBetween() {
        // Half past one hour to half past the hour after next: a half, a whole and a half hour.
        val from = 100 * hour + hour / 2
        val spread = spreadSteps(steps = 400, fromMillis = from, toMillis = from + 2 * hour)
        assertEquals(listOf(100, 200, 100), spread.map { it.steps })
        assertEquals(3, spread.map { it.date to it.hour }.toSet().size)
    }

    @Test
    fun aShortGapStaysInOneHourAndNothingIsLostToRounding() {
        val from = 100 * hour + 60_000
        assertEquals(listOf(7), spreadSteps(steps = 7, fromMillis = from, toMillis = from + 5_000).map { it.steps })
        val uneven = spreadSteps(steps = 1_001, fromMillis = from, toMillis = from + 3 * hour)
        assertEquals(1_001, uneven.sumOf { it.steps })
        assertTrue(spreadSteps(steps = 0, fromMillis = from, toMillis = from + hour).isEmpty())
    }
}
