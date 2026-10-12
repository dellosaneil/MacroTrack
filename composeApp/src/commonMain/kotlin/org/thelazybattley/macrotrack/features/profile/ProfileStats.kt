package org.thelazybattley.macrotrack.features.profile

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.calendar.bestStreak
import org.thelazybattley.macrotrack.features.calendar.currentStreak
import org.thelazybattley.macrotrack.features.calendar.isOnTarget
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

private const val DAYS_PER_WEEK = 7
private const val MIN_TREND_DAYS = 7
private const val MIN_WEEKLY_KG = 0.05

fun calculateGoalProgress(
    userDetails: UserDetails,
    weights: List<Weight>,
    today: LocalDate
): GoalProgress? {
    val target = userDetails.targetWeight ?: return null
    val current = userDetails.weight
    val start = userDetails.targetStartWeight ?: current
    val startDate = userDetails.targetSetDate
    val isGaining = target > start
    val totalKg = abs(target - start)
    val achievedKg = if (isGaining) current - start else start - current
    val progress = if (totalKg == 0.0) 1f else (achievedKg / totalKg).toFloat().coerceIn(0f, 1f)
    val remainingKg = if (isGaining) target - current else current - target

    val weeklyChange = startDate?.let {
        weeklyTrend(startDate = it, startWeight = start, weights = weights, today = today)
    }
    // Positive when the trend moves toward the target.
    val weeklyKgTowardTarget = weeklyChange?.let { if (isGaining) it else -it }

    val status = when {
        remainingKg <= 0.0 -> GoalStatus.REACHED
        weeklyKgTowardTarget == null -> GoalStatus.NEEDS_MORE_DATA
        weeklyKgTowardTarget < MIN_WEEKLY_KG -> GoalStatus.OFF_TRACK
        else -> GoalStatus.ON_TRACK
    }
    val estimatedDate = if (status == GoalStatus.ON_TRACK && weeklyKgTowardTarget != null) {
        val days = ceil(remainingKg / weeklyKgTowardTarget * DAYS_PER_WEEK).toInt()
        today.plus(period = DatePeriod(days = days))
    } else {
        null
    }

    return GoalProgress(
        startWeight = start,
        startDate = startDate,
        currentWeight = current,
        targetWeight = target,
        isGaining = isGaining,
        achievedKg = achievedKg,
        totalKg = totalKg,
        progress = progress,
        status = status,
        weeklyChange = weeklyChange,
        estimatedDate = estimatedDate
    )
}

/**
 * Least-squares slope, in kg per week, of the start weight and every weigh-in after the start date.
 * Null until the weigh-ins span at least a week, since a shorter span is mostly day-to-day noise.
 */
internal fun weeklyTrend(
    startDate: LocalDate,
    startWeight: Double,
    weights: List<Weight>,
    today: LocalDate
): Double? {
    val points = listOf(0.0 to startWeight) + weights
        .filter { it.date > startDate && it.date <= today }
        .map { (it.date.toEpochDays() - startDate.toEpochDays()).toDouble() to it.weight }
    if (points.size < 2 || points.maxOf { it.first } < MIN_TREND_DAYS) return null
    val meanDay = points.sumOf { it.first } / points.size
    val meanWeight = points.sumOf { it.second } / points.size
    val covariance = points.sumOf { (day, weight) -> (day - meanDay) * (weight - meanWeight) }
    val variance = points.sumOf { (day, _) -> (day - meanDay) * (day - meanDay) }
    return covariance / variance * DAYS_PER_WEEK
}

/**
 * The weight to start from on [date]: the last weigh-in on or before it, else the first after it.
 */
fun startWeightOn(date: LocalDate, weights: List<Weight>): Weight? {
    val sorted = weights.sortedBy { it.date }
    return sorted.lastOrNull { it.date <= date } ?: sorted.firstOrNull()
}

fun calculateWeekSummary(
    foodLogs: List<FoodLog>,
    weights: List<Weight>,
    macroGoals: MacroGoals?,
    today: LocalDate
): WeekSummary {
    val weekStart = today.minus(period = DatePeriod(days = today.dayOfWeek.isoDayNumber - 1))
    val weekDays = (0 until DAYS_PER_WEEK).map { weekStart.plus(period = DatePeriod(days = it)) }
    val logsByDate = foodLogs.groupBy { it.date }
    val loggedDates = logsByDate.keys

    val loggedThisWeek = weekDays.filter { it <= today && it in loggedDates }
    val dailyCalories = loggedThisWeek.map { date -> logsByDate.getValue(date).sumOf { it.calories } }
    val dailyProtein = loggedThisWeek.map { date -> logsByDate.getValue(date).sumOf { it.protein } }
    val daysOnTarget = macroGoals?.calories?.let { goal ->
        dailyCalories.count { calories -> isOnTarget(calories = calories, goalCalories = goal) }
    } ?: 0

    val sortedWeights = weights.sortedBy { it.date }
    val weekWeights = sortedWeights.filter { it.date in weekStart..today }
    val weightChange = weekWeights.lastOrNull()?.let { latest ->
        val baseline = sortedWeights.lastOrNull { it.date < weekStart } ?: weekWeights.first()
        latest.weight - baseline.weight
    }

    return WeekSummary(
        weekStart = weekStart,
        loggedDays = weekDays.map { it in loggedDates },
        todayIndex = today.dayOfWeek.isoDayNumber - 1,
        currentStreak = currentStreak(loggedDates = loggedDates, today = today),
        bestStreak = bestStreak(loggedDates = loggedDates),
        averageCalories = dailyCalories.takeIf { it.isNotEmpty() }?.average()?.roundToInt(),
        averageProtein = dailyProtein.takeIf { it.isNotEmpty() }?.average()?.roundToInt(),
        daysOnTarget = daysOnTarget,
        daysLogged = loggedThisWeek.size,
        weightChange = weightChange
    )
}
