package org.thelazybattley.macrotrack.features.profile.weighthistory

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Goal

/** A stretch of days that shared one goal, regardless of calorie target. [end] is inclusive. */
data class GoalPhase(
    val start: LocalDate,
    val end: LocalDate,
    val goal: Goal
)

/**
 * Daily goal records as goal phases, oldest first. Records act as a step function, and today
 * always uses [currentGoal], the goal from the user's details.
 */
fun goalPhases(records: List<DailyGoal>, today: LocalDate, currentGoal: Goal?): List<GoalPhase> {
    val steps = records.filter { it.date < today }.sortedBy { it.date }.map { it.date to it.goal } +
        listOfNotNull(currentGoal?.let { today to it })
    val changes = steps.filterIndexed { index, step -> index == 0 || steps[index - 1].second != step.second }
    return changes.mapIndexed { index, (start, goal) ->
        GoalPhase(
            start = start,
            end = changes.getOrNull(index = index + 1)?.first?.minus(period = DatePeriod(days = 1)) ?: today,
            goal = goal
        )
    }
}
