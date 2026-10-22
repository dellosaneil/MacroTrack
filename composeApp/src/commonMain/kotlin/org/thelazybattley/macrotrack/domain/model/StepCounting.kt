package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.repository.StepSensorReading

/** Steps taken in one hour of one day. */
data class HourSteps(val date: LocalDate, val hour: Int, val steps: Int)

private const val MILLIS_PER_HOUR = 3_600_000L

/**
 * Steps since [previous] for a step counter that only counts up. A lower count than before means the phone
 * restarted and the counter began again at zero. The very first reading has nothing to compare with, so it
 * starts counting from there.
 */
fun stepsSince(previous: StepSensorReading?, count: Long): Long = when {
    previous == null -> 0
    count >= previous.count -> count - previous.count
    else -> count
}

/**
 * Shares [steps] taken between [fromMillis] and [toMillis] out over the hours in between, by how much of each
 * hour the gap covers. Steps counted while the app was closed land across that time rather than all in the
 * hour the app was opened. Hours are cut on the hour by the clock, which matches local hours in every time zone
 * but the few that are off by a half or quarter hour.
 */
fun spreadSteps(steps: Long, fromMillis: Long, toMillis: Long): List<HourSteps> {
    if (steps <= 0) return emptyList()
    val start = minOf(fromMillis, toMillis)
    if (toMillis - start <= 0) return listOf(hourOf(millis = toMillis, steps = steps.toInt()))
    val pieces = mutableListOf<Pair<Long, Long>>()
    var cursor = start
    while (cursor < toMillis) {
        val hourEnd = (cursor / MILLIS_PER_HOUR + 1) * MILLIS_PER_HOUR
        val pieceEnd = minOf(hourEnd, toMillis)
        pieces += cursor to pieceEnd
        cursor = pieceEnd
    }
    val total = (toMillis - start).toDouble()
    var given = 0L
    return pieces.mapIndexed { index, (pieceStart, pieceEnd) ->
        // The last hour takes what rounding left over, so nothing is lost.
        val share = if (index == pieces.lastIndex) steps - given else (steps * (pieceEnd - pieceStart) / total).toLong()
        given += share
        hourOf(millis = pieceStart, steps = share.toInt())
    }.filter { it.steps > 0 }
        .groupBy { it.date to it.hour }
        .map { (key, hours) -> HourSteps(date = key.first, hour = key.second, steps = hours.sumOf { it.steps }) }
}

private fun hourOf(millis: Long, steps: Int): HourSteps {
    val time = millis.toLocalDateTime()
    return HourSteps(date = time.date, hour = time.hour, steps = steps)
}
