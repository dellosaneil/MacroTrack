package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** A stretch of days that shared one goal and calorie target. [end] is inclusive. */
data class GoalPeriod(
    val start: LocalDate,
    val end: LocalDate,
    val goal: Goal,
    val calories: Int
)

/**
 * Setting [newGoal] for [start] to [end]. [following] keeps the goal that applied the day after
 * [end] so later days don't change; it is null when that day already has its own record or is
 * after today.
 */
data class PastGoalChange(
    val start: LocalDate,
    val end: LocalDate,
    val newGoal: DailyGoal,
    val following: DailyGoal?
)

private val oneDay = DatePeriod(days = 1)

/**
 * Daily goal records act as a step function: each one applies until the next. Today always uses
 * [current], the goal from the user's details.
 */
fun withCurrentGoal(records: List<DailyGoal>, today: LocalDate, current: DailyGoal?): List<DailyGoal> {
    val past = records.filter { it.date < today }
    return (past + listOfNotNull(current?.copy(date = today))).sortedBy { it.date }
}

/** Groups records into periods, oldest first, merging neighbours with the same goal and targets. */
fun goalPeriods(records: List<DailyGoal>, today: LocalDate): List<GoalPeriod> {
    val sorted = records.filter { it.date <= today }.sortedBy { it.date }
    val merged = sorted.filterIndexed { index, record ->
        index == 0 || sorted[index - 1].let { it.goal != record.goal || it.macroGoals != record.macroGoals }
    }
    return merged.mapIndexed { index, record ->
        GoalPeriod(
            start = record.date,
            end = merged.getOrNull(index = index + 1)?.date?.minus(period = oneDay) ?: today,
            goal = record.goal,
            calories = record.macroGoals.calories
        )
    }
}

fun pastGoalChange(
    records: List<DailyGoal>,
    today: LocalDate,
    current: DailyGoal?,
    start: LocalDate,
    end: LocalDate,
    newGoal: DailyGoal
): PastGoalChange {
    val all = withCurrentGoal(records = records, today = today, current = current)
    val after = end.plus(period = oneDay)
    val following = when {
        after > today || records.any { it.date == after } -> null
        else -> (all.lastOrNull { it.date <= after } ?: current)?.copy(date = after)
    }
    return PastGoalChange(start = start, end = end, newGoal = newGoal.copy(date = start), following = following)
}

/** The records as they will be once [change] is applied. */
fun applyChange(records: List<DailyGoal>, change: PastGoalChange): List<DailyGoal> {
    val kept = records.filter { it.date < change.start || it.date > change.end }
    return (kept + change.newGoal + listOfNotNull(change.following)).sortedBy { it.date }
}
