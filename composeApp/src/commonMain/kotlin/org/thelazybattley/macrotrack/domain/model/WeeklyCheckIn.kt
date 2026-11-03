package org.thelazybattley.macrotrack.domain.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/** A check-in looks back over this many days by default, ending yesterday. Days with nothing logged are left out. */
const val CHECK_IN_WINDOW_DAYS = 21

/** Average weight compares the average of the first this-many days of the window with the last this-many. */
const val CHECK_IN_AVERAGE_WEEK_DAYS = 7

/** Average weight needs at least this many weigh-ins in both its first and last week. */
const val CHECK_IN_MIN_WEEK_WEIGH_INS = 2

/** Below these, the window says too little about the user's burn, so the target keeps the profile estimate. */
const val CHECK_IN_MIN_LOGGED_DAYS = 14
const val CHECK_IN_MIN_WEIGH_INS = 4

/**
 * A suggested target this close to the current one needs no change. A measured burn this close to the profile's
 * estimate reads as "close to what your profile estimated", and a suggestion that moves this much is new.
 */
const val CHECK_IN_NO_CHANGE_KCAL = 50

/** Weight moving within this many kg a month of the goal's pace is on pace; further off, slower or faster. */
const val CHECK_IN_PACE_TOLERANCE_KG = 0.1

private const val KCAL_PER_KG = 7700.0
private const val DAYS_PER_MONTH = 30.4

enum class CheckInPace { ON_PACE, SLOWER, FASTER, NOT_STEADY }

/** How the weight change over the window is measured. */
enum class CheckInMethod {
    /** A straight line fitted through every weigh-in, from the start to the end of the window. */
    TREND,

    /** The average of the first 7 days against the last 7, spread over the days between them. */
    AVERAGE
}

/**
 * How far back the check-in looks and how it measures the weight change. Saved; it drives the check-in, its daily
 * suggestion and the Today card alike.
 */
@Serializable
data class CheckInSettings(
    val windowDays: Int = CHECK_IN_WINDOW_DAYS,
    val method: CheckInMethod = CheckInMethod.TREND
) {
    /** A saved window that's no longer offered falls back to the default. */
    val window: Int get() = windowDays.takeIf { it in WINDOW_OPTIONS } ?: CHECK_IN_WINDOW_DAYS

    companion object {
        /** 2, 3, 4 or 8 weeks. */
        val WINDOW_OPTIONS = listOf(14, 21, 28, 56)
    }
}

/** One of average weight's two weeks: its days and how many weigh-ins fell in them. */
data class CheckInWeek(val start: LocalDate, val end: LocalDate, val weighIns: Int)

sealed interface WeeklyCheckIn {

    /** Not enough logged days or weigh-ins yet. [firstCheckIn] is the earliest day there could be enough. */
    data class Collecting(
        val loggedDays: Int,
        val weighIns: Int,
        val firstCheckIn: LocalDate,
        val windowDays: Int = CHECK_IN_WINDOW_DAYS,
        /** Enough overall, but average weight is short of weigh-ins in its first or last week. */
        val isShortOfWeekWeighIns: Boolean = false
    ) : WeeklyCheckIn

    /**
     * The measured burn: average intake on logged days, plus the energy the weight change stands for. Weights are
     * in kg, energy in kcal a day. [startWeight] and [endWeight] are the trend line's ends, or with
     * [CheckInMethod.AVERAGE] the first and last week's averages; the change is spread over [weightSpanDays].
     */
    data class Result(
        val windowStart: LocalDate,
        val windowEnd: LocalDate,
        val loggedDays: Int,
        val weighIns: Int,
        val averageIntake: Int,
        val startWeight: Double,
        val endWeight: Double,
        /** Positive when weight was lost: the body used that energy on top of what was eaten. */
        val energyFromWeight: Int,
        val measuredBurn: Int,
        /** The burn the profile formula estimates. */
        val estimatedBurn: Int,
        val currentTarget: Int,
        val suggestedTarget: Int,
        /** Signed trend change in kg a month. */
        val monthlyChange: Double,
        val pace: CheckInPace,
        val windowDays: Int = CHECK_IN_WINDOW_DAYS,
        val method: CheckInMethod = CheckInMethod.TREND,
        val weightSpanDays: Int = windowDays,
        /** Average weight's first and last week; null for the trend line. */
        val firstWeek: CheckInWeek? = null,
        val lastWeek: CheckInWeek? = null
    ) : WeeklyCheckIn {
        val weightChange: Double get() = endWeight - startWeight

        val targetChange: Int get() = suggestedTarget - currentTarget

        val isNoChange: Boolean get() = abs(targetChange) <= CHECK_IN_NO_CHANGE_KCAL
    }
}

/**
 * Works out what the user really burns over the [CheckInSettings.window] days before [today], and the target that
 * keeps their [goal] at [goalPace] (kg a month). The weight change comes from a straight line fitted through the
 * weigh-ins, so one heavy or light morning doesn't swing it, or with [CheckInMethod.AVERAGE] from the first week's
 * average weight against the last week's.
 */
fun weeklyCheckIn(
    today: LocalDate,
    foodLogs: List<FoodLog>,
    weights: List<Weight>,
    goal: Goal,
    goalPace: Double,
    currentTarget: Int,
    estimatedBurn: Int,
    settings: CheckInSettings = CheckInSettings()
): WeeklyCheckIn {
    val windowDays = settings.window
    val windowEnd = today.minus(period = DatePeriod(days = 1))
    val windowStart = windowEnd.minus(period = DatePeriod(days = windowDays - 1))
    val dailyIntake = foodLogs.filter { it.date in windowStart..windowEnd }
        .groupBy { it.date }
        .mapValues { (_, logs) -> logs.sumOf { it.calories } }
    val weighIns = weights.filter { it.date in windowStart..windowEnd }
    if (dailyIntake.size < CHECK_IN_MIN_LOGGED_DAYS || weighIns.size < CHECK_IN_MIN_WEIGH_INS) {
        val daysShort = max(CHECK_IN_MIN_LOGGED_DAYS - dailyIntake.size, CHECK_IN_MIN_WEIGH_INS - weighIns.size)
        return WeeklyCheckIn.Collecting(
            loggedDays = dailyIntake.size,
            weighIns = weighIns.size,
            firstCheckIn = today.plus(period = DatePeriod(days = max(daysShort, 1))),
            windowDays = windowDays
        )
    }

    val firstWeek = CheckInWeek(
        start = windowStart,
        end = windowStart.plus(period = DatePeriod(days = CHECK_IN_AVERAGE_WEEK_DAYS - 1)),
        weighIns = 0
    ).let { week -> week.copy(weighIns = weighIns.count { it.date in week.start..week.end }) }
    val lastWeek = CheckInWeek(
        start = windowEnd.minus(period = DatePeriod(days = CHECK_IN_AVERAGE_WEEK_DAYS - 1)),
        end = windowEnd,
        weighIns = 0
    ).let { week -> week.copy(weighIns = weighIns.count { it.date in week.start..week.end }) }
    val isAverage = settings.method == CheckInMethod.AVERAGE
    if (isAverage && (firstWeek.weighIns < CHECK_IN_MIN_WEEK_WEIGH_INS || lastWeek.weighIns < CHECK_IN_MIN_WEEK_WEIGH_INS)) {
        // The first week only fills as the window moves on, so the soonest is when the last week has its weigh-ins.
        return WeeklyCheckIn.Collecting(
            loggedDays = dailyIntake.size,
            weighIns = weighIns.size,
            firstCheckIn = today.plus(period = DatePeriod(days = max(CHECK_IN_MIN_WEEK_WEIGH_INS - lastWeek.weighIns, 1))),
            windowDays = windowDays,
            isShortOfWeekWeighIns = true
        )
    }

    val startWeight: Double
    val endWeight: Double
    val spanDays: Int
    if (isAverage) {
        startWeight = weighIns.filter { it.date in firstWeek.start..firstWeek.end }.map { it.weight }.average()
        endWeight = weighIns.filter { it.date in lastWeek.start..lastWeek.end }.map { it.weight }.average()
        spanDays = windowDays - CHECK_IN_AVERAGE_WEEK_DAYS
    } else {
        val slope = slopePerDay(weights = weighIns)
        startWeight = weighIns.fitAt(date = windowStart, slope = slope)
        endWeight = startWeight + slope * windowDays
        spanDays = windowDays
    }
    val slope = (endWeight - startWeight) / spanDays
    val averageIntake = dailyIntake.values.average().roundToInt()
    val energyFromWeight = (-slope * KCAL_PER_KG).roundToTen()
    val measuredBurn = averageIntake + energyFromWeight
    val monthlyChange = slope * DAYS_PER_MONTH
    return WeeklyCheckIn.Result(
        windowStart = windowStart,
        windowEnd = windowEnd,
        loggedDays = dailyIntake.size,
        weighIns = weighIns.size,
        averageIntake = averageIntake,
        startWeight = startWeight,
        endWeight = endWeight,
        energyFromWeight = energyFromWeight,
        measuredBurn = measuredBurn,
        estimatedBurn = estimatedBurn,
        currentTarget = currentTarget,
        suggestedTarget = (measuredBurn + goal.calorieAdjust(paceKgPerMonth = goalPace).toDouble()).roundToTen(),
        monthlyChange = monthlyChange,
        pace = paceOf(goal = goal, goalPace = goalPace, monthlyChange = monthlyChange),
        windowDays = windowDays,
        method = settings.method,
        weightSpanDays = spanDays,
        firstWeek = firstWeek.takeIf { isAverage },
        lastWeek = lastWeek.takeIf { isAverage }
    )
}

private fun paceOf(goal: Goal, goalPace: Double, monthlyChange: Double): CheckInPace {
    if (goal.direction == 0) {
        return if (abs(monthlyChange) <= CHECK_IN_PACE_TOLERANCE_KG) CheckInPace.ON_PACE else CheckInPace.NOT_STEADY
    }
    val towardGoal = monthlyChange * goal.direction
    return when {
        towardGoal < goalPace - CHECK_IN_PACE_TOLERANCE_KG -> CheckInPace.SLOWER
        towardGoal > goalPace + CHECK_IN_PACE_TOLERANCE_KG -> CheckInPace.FASTER
        else -> CheckInPace.ON_PACE
    }
}

/** Least-squares slope through the weigh-ins, in kg a day. */
private fun slopePerDay(weights: List<Weight>): Double {
    val points = weights.map { it.date.toEpochDays().toDouble() to it.weight }
    val meanDay = points.sumOf { it.first } / points.size
    val meanWeight = points.sumOf { it.second } / points.size
    val variance = points.sumOf { (day, _) -> (day - meanDay) * (day - meanDay) }
    if (variance == 0.0) return 0.0
    return points.sumOf { (day, weight) -> (day - meanDay) * (weight - meanWeight) } / variance
}

private fun List<Weight>.fitAt(date: LocalDate, slope: Double): Double {
    val meanDay = sumOf { it.date.toEpochDays().toDouble() } / size
    val meanWeight = sumOf { it.weight } / size
    return meanWeight + slope * (date.toEpochDays() - meanDay)
}

private fun Double.roundToTen(): Int = (this / 10).roundToInt() * 10
