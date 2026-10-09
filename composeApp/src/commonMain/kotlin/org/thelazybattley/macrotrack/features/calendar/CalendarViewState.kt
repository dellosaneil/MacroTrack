package org.thelazybattley.macrotrack.features.calendar

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.core.getCurrentDate
import kotlin.math.roundToInt

data class CalendarViewState(
    val today: LocalDate = getCurrentDate(),
    /** What was logged on each day that has at least one log. */
    val days: Map<LocalDate, DaySummary> = emptyMap(),
    /** The first day of the month on screen. */
    val visibleMonth: LocalDate = today.firstOfMonth(),
    val selectedDate: LocalDate = today
) {
    private val loggedDates: Set<LocalDate> get() = days.keys

    val currentStreak: StreakRun? get() = currentStreakRun(loggedDates = loggedDates, today = today)

    val bestStreak: StreakRun? get() = bestStreakRun(loggedDates = loggedDates)

    val lastBrokenStreak: StreakRun? get() = lastBrokenStreakRun(loggedDates = loggedDates, today = today)

    val streakState: StreakState
        get() = when {
            today in loggedDates -> StreakState.LOGGED_TODAY
            currentStreak != null -> StreakState.AT_RISK
            else -> StreakState.ENDED
        }

    /** Monday to Sunday of the current week, true for each logged day. */
    val thisWeek: List<WeekDay>
        get() {
            val monday = today.minus(period = DatePeriod(days = today.dayOfWeek.isoDayNumber - 1))
            return (0 until 7).map { offset ->
                val date = monday.plus(period = DatePeriod(days = offset))
                WeekDay(date = date, isLogged = date in loggedDates, isToday = date == today, isFuture = date > today)
            }
        }

    /** The earliest month the calendar can go back to: the first logged month, or this month. */
    val firstMonth: LocalDate get() = (loggedDates.minOrNull() ?: today).firstOfMonth()

    val canGoBack: Boolean get() = visibleMonth > firstMonth

    val canGoForward: Boolean get() = visibleMonth < today.firstOfMonth()

    /** Empty cells (null) pad the first week so the grid starts on Monday. */
    val monthCells: List<LocalDate?>
        get() = List(size = visibleMonth.dayOfWeek.isoDayNumber - 1) { null } + daysOf(month = visibleMonth)

    val monthStats: MonthStats
        get() {
            val pastDays = daysOf(month = visibleMonth).filter { it <= today }
            val completed = pastDays.filter { it != today }.mapNotNull { days[it] }
            return MonthStats(
                daysLogged = pastDays.count { it in loggedDates },
                daysSoFar = pastDays.size,
                daysOnTarget = completed.count { statusOf(date = it.date) == DayStatus.ON_TARGET },
                daysWithGoal = completed.count { it.goalCalories != null },
                averageCalories = completed.takeIf { it.isNotEmpty() }?.map { it.calories }?.average()?.roundToInt()
            )
        }

    val selectedDay: DaySummary? get() = days[selectedDate]

    val canOpenSelected: Boolean get() = selectedDate in loggedDates || selectedDate == today

    fun statusOf(date: LocalDate): DayStatus {
        val day = days[date]
        return when {
            date > today -> DayStatus.FUTURE
            day == null -> if (date == today) DayStatus.TODAY_EMPTY else DayStatus.NOT_LOGGED
            date == today -> DayStatus.TODAY
            day.goalCalories == null -> DayStatus.LOGGED
            isOnTarget(calories = day.calories, goalCalories = day.goalCalories) -> DayStatus.ON_TARGET
            else -> DayStatus.OFF_TARGET
        }
    }
}

data class DaySummary(
    val date: LocalDate,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val mealCount: Int,
    /** The calorie goal that applied on this day, if one is known. */
    val goalCalories: Int?
)

data class WeekDay(
    val date: LocalDate,
    val isLogged: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean
)

data class MonthStats(
    val daysLogged: Int,
    val daysSoFar: Int,
    val daysOnTarget: Int,
    val daysWithGoal: Int,
    val averageCalories: Int?
)

enum class StreakState { LOGGED_TODAY, AT_RISK, ENDED }

enum class DayStatus {
    ON_TARGET,
    OFF_TARGET,

    /** Logged, but there is no calorie goal to compare against. */
    LOGGED,
    NOT_LOGGED,

    /** Today, with something logged; it isn't judged until the day is over. */
    TODAY,
    TODAY_EMPTY,
    FUTURE
}

fun LocalDate.firstOfMonth(): LocalDate = LocalDate(year = year, month = month, day = 1)

private fun daysOf(month: LocalDate): List<LocalDate> {
    val next = month.plus(period = DatePeriod(months = 1))
    return generateSequence(seed = month) { it.plus(period = DatePeriod(days = 1)) }.takeWhile { it < next }.toList()
}
