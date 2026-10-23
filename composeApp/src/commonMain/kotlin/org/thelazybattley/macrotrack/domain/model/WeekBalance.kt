package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.abs
import kotlin.math.roundToInt

/** The week balance can be looked at for this week and the one before. */
const val WEEK_BALANCE_WEEKS = 2

private const val DAYS_PER_WEEK = 7

enum class WeekDayStatus {
    /** A finished day with food logged: it adds to the balance. */
    COUNTED,

    /** Counts once it ends. */
    TODAY,
    UPCOMING,

    /** A finished day with nothing logged, left out. */
    NOT_LOGGED
}

data class WeekBalanceDay(
    val date: LocalDate,
    /** Calories logged so far; null when nothing was. */
    val eaten: Int?,
    val target: Int,
    val status: WeekDayStatus
) {
    /** Eaten − target, signed. Zero unless the day counts. */
    val difference: Int get() = if (status == WeekDayStatus.COUNTED) (eaten ?: 0) - target else 0
}

/** Monday to Sunday: each finished, logged day's eaten − that day's target, added up. */
data class WeekBalance(val days: List<WeekBalanceDay>) {

    val weekStart: LocalDate get() = days.first().date
    val weekEnd: LocalDate get() = days.last().date

    val total: Int get() = days.sumOf { it.difference }

    val countedDays: Int get() = days.count { it.status == WeekDayStatus.COUNTED }

    val hasNotLoggedDays: Boolean get() = days.any { it.status == WeekDayStatus.NOT_LOGGED }

    val isThisWeek: Boolean get() = days.any { it.status == WeekDayStatus.TODAY }

    /** Today and the days after it. */
    val daysLeft: Int get() = days.count { it.status == WeekDayStatus.TODAY || it.status == WeekDayStatus.UPCOMING }

    /** What each day left, today included, can be under (or over) target to finish the week even; null when it is. */
    val evenPerDay: Int?
        get() = if (daysLeft == 0 || total == 0) null else (abs(total).toDouble() / daysLeft).roundToInt()

    /** The balance once [index]'s day is added. */
    fun runningTotal(index: Int): Int = days.take(n = index + 1).sumOf { it.difference }
}

/** The Monday on or before this day. */
fun LocalDate.weekStart(): LocalDate = minus(period = DatePeriod(days = dayOfWeek.isoDayNumber - 1))

/** [intake] is calories logged by day; [targetFor] the calorie target that applied on a day. */
fun weekBalance(
    weekStart: LocalDate,
    today: LocalDate,
    intake: Map<LocalDate, Int>,
    targetFor: (LocalDate) -> Int
): WeekBalance = WeekBalance(
    days = List(size = DAYS_PER_WEEK) { index ->
        val date = weekStart.plus(period = DatePeriod(days = index))
        val eaten = intake[date]
        WeekBalanceDay(
            date = date,
            eaten = eaten,
            target = targetFor(date),
            status = when {
                date == today -> WeekDayStatus.TODAY
                date > today -> WeekDayStatus.UPCOMING
                eaten == null -> WeekDayStatus.NOT_LOGGED
                else -> WeekDayStatus.COUNTED
            }
        )
    }
)
