package org.thelazybattley.macrotrack.features.profile

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.calendar.bestStreak
import org.thelazybattley.macrotrack.features.calendar.currentStreak
import org.thelazybattley.macrotrack.features.calendar.isOnTarget
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

private const val KCAL_PER_KG = 7700.0
private const val DAYS_PER_WEEK = 7

fun calculateGoalProgress(
    userDetails: UserDetails,
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

    val goalMatchesDirection = when (userDetails.goal) {
        Goal.LOSE_WEIGHT -> !isGaining
        Goal.GAIN_WEIGHT -> isGaining
        Goal.MAINTAIN_WEIGHT -> false
    }
    val weeklyKg = abs(userDetails.goal.calorieAdjust) * DAYS_PER_WEEK / KCAL_PER_KG

    val status = when {
        remainingKg <= 0.0 -> GoalStatus.REACHED
        !goalMatchesDirection || weeklyKg == 0.0 -> GoalStatus.GOAL_MISMATCH
        else -> GoalStatus.ON_TRACK
    }
    val estimatedDate = if (status == GoalStatus.ON_TRACK) {
        val days = ceil(remainingKg / weeklyKg * DAYS_PER_WEEK).toInt()
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
        estimatedDate = estimatedDate
    )
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
