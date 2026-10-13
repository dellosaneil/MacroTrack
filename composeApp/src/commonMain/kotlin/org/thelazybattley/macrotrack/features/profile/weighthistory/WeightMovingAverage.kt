package org.thelazybattley.macrotrack.features.profile.weighthistory

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.MovingAverageWindow

/** How far back the moving average is compared, in days. */
const val MOVING_AVERAGE_COMPARE_DAYS = 7

data class MovingAverageSummary(
    val window: MovingAverageWindow,
    val average: Double,
    /** The average minus the one [MOVING_AVERAGE_COMPARE_DAYS] days earlier; null when nothing was logged by then. */
    val change: Double?
)

/**
 * For each weigh-in, the average of every weigh-in in the [windowDays] calendar days ending on its date.
 * Days without a weigh-in are skipped rather than filled, so sparse logging still averages what was logged.
 */
fun List<Weight>.movingAverage(windowDays: Int): List<Weight> {
    val sorted = sortedBy { it.date }
    var windowStart = 0
    var windowSum = 0.0
    return sorted.mapIndexed { index, weight ->
        windowSum += weight.weight
        val earliestDay = weight.date.toEpochDays() - (windowDays - 1)
        while (sorted[windowStart].date.toEpochDays() < earliestDay) {
            windowSum -= sorted[windowStart].weight
            windowStart++
        }
        Weight(date = weight.date, weight = windowSum / (index - windowStart + 1))
    }
}

/** Each entry's value minus the one before it, keyed by date; the earliest entry has no change. */
fun List<Weight>.changesFromPrevious(): Map<LocalDate, Double> =
    sortedBy { it.date }.zipWithNext { previous, current -> current.date to current.weight - previous.weight }.toMap()

/** The average of the weigh-ins in the [windowDays] calendar days ending on [date], or null when there are none. */
fun List<Weight>.movingAverageOn(date: LocalDate, windowDays: Int): Double? {
    val windowStart = date.minus(period = DatePeriod(days = windowDays - 1))
    return filter { it.date in windowStart..date }
        .takeIf { it.isNotEmpty() }
        ?.map { it.weight }
        ?.average()
}

fun List<Weight>.movingAverageSummary(date: LocalDate, window: MovingAverageWindow): MovingAverageSummary? {
    val average = movingAverageOn(date = date, windowDays = window.days) ?: return null
    val earlier = movingAverageOn(
        date = date.minus(period = DatePeriod(days = MOVING_AVERAGE_COMPARE_DAYS)),
        windowDays = window.days
    )
    return MovingAverageSummary(window = window, average = average, change = earlier?.let { average - it })
}
