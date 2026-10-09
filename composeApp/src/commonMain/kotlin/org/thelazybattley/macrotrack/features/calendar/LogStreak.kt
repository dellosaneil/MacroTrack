package org.thelazybattley.macrotrack.features.calendar

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.abs

/** A day is on target when its calories are within 10% of that day's calorie goal. */
const val ON_TARGET_TOLERANCE = 0.1

private val oneDay = DatePeriod(days = 1)

/** A run of consecutive logged days. */
data class StreakRun(
    val start: LocalDate,
    val end: LocalDate,
    val length: Int
)

/**
 * The run of logged days ending today, or ending yesterday while today has nothing logged yet (the
 * streak isn't broken until today is over). Null when there is no such run.
 */
fun currentStreakRun(loggedDates: Set<LocalDate>, today: LocalDate): StreakRun? {
    val end = if (today in loggedDates) today else today.minus(period = oneDay)
    return runEndingOn(loggedDates = loggedDates, end = end)
}

fun currentStreak(loggedDates: Set<LocalDate>, today: LocalDate): Int =
    currentStreakRun(loggedDates = loggedDates, today = today)?.length ?: 0

/** The longest run of logged days; the most recent one wins a tie. */
fun bestStreakRun(loggedDates: Set<LocalDate>): StreakRun? {
    var best: StreakRun? = null
    var start: LocalDate? = null
    var previous: LocalDate? = null
    var length = 0
    loggedDates.sorted().forEach { date ->
        if (previous != null && previous.plus(period = oneDay) == date) {
            length++
        } else {
            start = date
            length = 1
        }
        if (length >= (best?.length ?: 0)) {
            best = StreakRun(start = start!!, end = date, length = length)
        }
        previous = date
    }
    return best
}

fun bestStreak(loggedDates: Set<LocalDate>): Int = bestStreakRun(loggedDates = loggedDates)?.length ?: 0

/** The most recent run that ended before yesterday, i.e. a streak that has been broken. */
fun lastBrokenStreakRun(loggedDates: Set<LocalDate>, today: LocalDate): StreakRun? {
    val yesterday = today.minus(period = oneDay)
    val end = loggedDates.filter { it < yesterday }.maxOrNull() ?: return null
    return runEndingOn(loggedDates = loggedDates, end = end)
}

private fun runEndingOn(loggedDates: Set<LocalDate>, end: LocalDate): StreakRun? {
    var day = end
    var length = 0
    while (day in loggedDates) {
        length++
        day = day.minus(period = oneDay)
    }
    if (length == 0) return null
    return StreakRun(start = day.plus(period = oneDay), end = end, length = length)
}

fun isOnTarget(calories: Int, goalCalories: Int): Boolean =
    goalCalories > 0 && abs(calories - goalCalories) <= goalCalories * ON_TARGET_TOLERANCE
