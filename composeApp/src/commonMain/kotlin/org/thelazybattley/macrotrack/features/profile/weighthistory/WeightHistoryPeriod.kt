package org.thelazybattley.macrotrack.features.profile.weighthistory

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryTimeRangeEnum

data class WeightPeriod(
    val range: WeightHistoryTimeRangeEnum,
    val start: LocalDate,
    val end: LocalDate
) {
    operator fun contains(date: LocalDate): Boolean = date in start..end
}

/** The calendar period of [range] that contains [date]. ALL spans from the first entry to [date]. */
fun periodContaining(
    range: WeightHistoryTimeRangeEnum,
    date: LocalDate,
    earliestEntry: LocalDate?
): WeightPeriod = when (range) {
    WeightHistoryTimeRangeEnum.WEEK -> {
        val start = date.minus(period = DatePeriod(days = date.dayOfWeek.isoDayNumber - 1))
        WeightPeriod(range = range, start = start, end = start.plus(period = DatePeriod(days = 6)))
    }

    WeightHistoryTimeRangeEnum.MONTH -> monthsPeriod(range = range, firstMonthStart = date.firstOfMonth(), months = 1)

    WeightHistoryTimeRangeEnum.THREE_MONTHS -> monthsPeriod(
        range = range,
        firstMonthStart = date.firstOfMonth().minus(period = DatePeriod(months = 2)),
        months = 3
    )

    WeightHistoryTimeRangeEnum.ALL -> WeightPeriod(
        range = range,
        start = minOf(earliestEntry ?: date, date),
        end = date
    )
}

/** Moves the period by [steps] whole periods. ALL never moves. */
fun WeightPeriod.shift(steps: Int): WeightPeriod = when (range) {
    WeightHistoryTimeRangeEnum.WEEK -> copy(
        start = start.plus(period = DatePeriod(days = 7 * steps)),
        end = end.plus(period = DatePeriod(days = 7 * steps))
    )

    WeightHistoryTimeRangeEnum.MONTH -> monthsPeriod(
        range = range,
        firstMonthStart = start.plus(period = DatePeriod(months = steps)),
        months = 1
    )

    WeightHistoryTimeRangeEnum.THREE_MONTHS -> monthsPeriod(
        range = range,
        firstMonthStart = start.plus(period = DatePeriod(months = 3 * steps)),
        months = 3
    )

    WeightHistoryTimeRangeEnum.ALL -> this
}

fun List<Weight>.inPeriod(period: WeightPeriod): List<Weight> =
    filter { it.date in period }.sortedBy { it.date }

private fun monthsPeriod(
    range: WeightHistoryTimeRangeEnum,
    firstMonthStart: LocalDate,
    months: Int
): WeightPeriod = WeightPeriod(
    range = range,
    start = firstMonthStart,
    end = firstMonthStart.plus(period = DatePeriod(months = months)).minus(period = DatePeriod(days = 1))
)

private fun LocalDate.firstOfMonth(): LocalDate = LocalDate(year = year, month = month, day = 1)
