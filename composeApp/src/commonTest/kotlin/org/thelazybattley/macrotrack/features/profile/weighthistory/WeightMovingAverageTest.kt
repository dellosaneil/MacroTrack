package org.thelazybattley.macrotrack.features.profile.weighthistory

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.MovingAverageWindow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WeightMovingAverageTest {

    private fun weight(day: Int, kg: Double) = Weight(date = LocalDate(2026, 10, day), weight = kg)

    private fun assertAverages(expected: List<Double>, actual: List<Weight>) {
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEach { (kg, weight) -> assertEquals(kg, weight.weight, absoluteTolerance = 1e-9) }
    }

    @Test
    fun emptyListHasNoAverage() {
        assertEquals(emptyList(), emptyList<Weight>().movingAverage(windowDays = 7))
    }

    @Test
    fun averagesEveryWeighInWithinTheLastSevenDays() {
        val averages = listOf(
            weight(day = 1, kg = 75.0),
            weight(day = 2, kg = 74.0),
            weight(day = 7, kg = 73.0),
            weight(day = 8, kg = 72.0)
        ).movingAverage(windowDays = 7)

        assertEquals(listOf(1, 2, 7, 8), averages.map { it.date.day })
        // Oct 8's window is Oct 2–8, so Oct 1 drops out.
        assertAverages(expected = listOf(75.0, 74.5, 74.0, 73.0), actual = averages)
    }

    @Test
    fun gapLongerThanTheWindowRestartsTheAverage() {
        val averages = listOf(
            weight(day = 1, kg = 80.0),
            weight(day = 20, kg = 70.0)
        ).movingAverage(windowDays = 7)

        assertAverages(expected = listOf(80.0, 70.0), actual = averages)
    }

    @Test
    fun sortsByDateBeforeAveraging() {
        val averages = listOf(
            weight(day = 3, kg = 72.0),
            weight(day = 1, kg = 74.0)
        ).movingAverage(windowDays = 7)

        assertEquals(listOf(1, 3), averages.map { it.date.day })
        assertAverages(expected = listOf(74.0, 73.0), actual = averages)
    }

    @Test
    fun customWindowLength() {
        val averages = listOf(
            weight(day = 1, kg = 76.0),
            weight(day = 2, kg = 74.0),
            weight(day = 3, kg = 72.0)
        ).movingAverage(windowDays = 2)

        assertAverages(expected = listOf(76.0, 75.0, 73.0), actual = averages)
    }

    @Test
    fun averageOnADateUsesOnlyItsWindow() {
        val weights = listOf(weight(day = 1, kg = 76.0), weight(day = 5, kg = 74.0), weight(day = 8, kg = 72.0))

        assertEquals(73.0, assertNotNull(weights.movingAverageOn(date = LocalDate(2026, 10, 8), windowDays = 5)))
        assertNull(weights.movingAverageOn(date = LocalDate(2026, 10, 4), windowDays = 3))
    }

    @Test
    fun summaryComparesWithTheAverageSevenDaysEarlier() {
        val weights = listOf(
            weight(day = 1, kg = 76.0),
            weight(day = 2, kg = 75.0),
            weight(day = 8, kg = 73.0),
            weight(day = 9, kg = 72.0)
        )

        val summary = assertNotNull(
            weights.movingAverageSummary(date = LocalDate(2026, 10, 9), window = MovingAverageWindow.THREE)
        )
        // Oct 7–9 averages 72.5; Sep 30 – Oct 2 averages 75.5.
        assertEquals(72.5, summary.average, absoluteTolerance = 1e-9)
        assertEquals(-3.0, assertNotNull(summary.change), absoluteTolerance = 1e-9)
    }

    @Test
    fun summaryHasNoChangeWithoutEarlierWeighIns() {
        val summary = assertNotNull(
            listOf(weight(day = 8, kg = 73.0)).movingAverageSummary(
                date = LocalDate(2026, 10, 8),
                window = MovingAverageWindow.SEVEN
            )
        )
        assertNull(summary.change)
    }

    @Test
    fun changesFromPreviousSkipTheFirstEntry() {
        val changes = listOf(
            weight(day = 5, kg = 74.5),
            weight(day = 1, kg = 75.0),
            weight(day = 8, kg = 74.5)
        ).changesFromPrevious()

        assertEquals(setOf(5, 8), changes.keys.map { it.day }.toSet())
        assertEquals(-0.5, assertNotNull(changes[LocalDate(2026, 10, 5)]), absoluteTolerance = 1e-9)
        assertEquals(0.0, assertNotNull(changes[LocalDate(2026, 10, 8)]), absoluteTolerance = 1e-9)
    }
}
