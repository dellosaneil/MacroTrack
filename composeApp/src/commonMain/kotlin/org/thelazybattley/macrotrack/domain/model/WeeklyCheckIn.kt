package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/** A check-in looks back over this many days, ending yesterday. Days with nothing logged are left out. */
const val CHECK_IN_WINDOW_DAYS = 21

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

sealed interface WeeklyCheckIn {

    /** Not enough logged days or weigh-ins yet. [firstCheckIn] is the earliest day there could be enough. */
    data class Collecting(
        val loggedDays: Int,
        val weighIns: Int,
        val firstCheckIn: LocalDate
    ) : WeeklyCheckIn

    /**
     * The measured burn: average intake on logged days, plus the energy the trend weight change stands for.
     * Weights are in kg, energy in kcal a day.
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
        val pace: CheckInPace
    ) : WeeklyCheckIn {
        val weightChange: Double get() = endWeight - startWeight

        val targetChange: Int get() = suggestedTarget - currentTarget

        val isNoChange: Boolean get() = abs(targetChange) <= CHECK_IN_NO_CHANGE_KCAL
    }
}

/**
 * Works out what the user really burns over the [CHECK_IN_WINDOW_DAYS] days before [today], and the target that
 * keeps their [goal] at [goalPace] (kg a month). The weight change comes from a straight line fitted through the
 * weigh-ins, so one heavy or light morning doesn't swing it.
 */
fun weeklyCheckIn(
    today: LocalDate,
    foodLogs: List<FoodLog>,
    weights: List<Weight>,
    goal: Goal,
    goalPace: Double,
    currentTarget: Int,
    estimatedBurn: Int
): WeeklyCheckIn {
    val windowEnd = today.minus(period = DatePeriod(days = 1))
    val windowStart = windowEnd.minus(period = DatePeriod(days = CHECK_IN_WINDOW_DAYS - 1))
    val dailyIntake = foodLogs.filter { it.date in windowStart..windowEnd }
        .groupBy { it.date }
        .mapValues { (_, logs) -> logs.sumOf { it.calories } }
    val weighIns = weights.filter { it.date in windowStart..windowEnd }
    if (dailyIntake.size < CHECK_IN_MIN_LOGGED_DAYS || weighIns.size < CHECK_IN_MIN_WEIGH_INS) {
        val daysShort = max(CHECK_IN_MIN_LOGGED_DAYS - dailyIntake.size, CHECK_IN_MIN_WEIGH_INS - weighIns.size)
        return WeeklyCheckIn.Collecting(
            loggedDays = dailyIntake.size,
            weighIns = weighIns.size,
            firstCheckIn = today.plus(period = DatePeriod(days = max(daysShort, 1)))
        )
    }

    val slope = slopePerDay(weights = weighIns)
    val startWeight = weighIns.fitAt(date = windowStart, slope = slope)
    val change = slope * CHECK_IN_WINDOW_DAYS
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
        endWeight = startWeight + change,
        energyFromWeight = energyFromWeight,
        measuredBurn = measuredBurn,
        estimatedBurn = estimatedBurn,
        currentTarget = currentTarget,
        suggestedTarget = (measuredBurn + goal.calorieAdjust(paceKgPerMonth = goalPace).toDouble()).roundToTen(),
        monthlyChange = monthlyChange,
        pace = paceOf(goal = goal, goalPace = goalPace, monthlyChange = monthlyChange)
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
