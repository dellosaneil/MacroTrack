package org.thelazybattley.macrotrack.features.foodlog

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Goal

/** The goal shown for a day. [isEstimated] means nothing was recorded, so the current goal is used. */
data class DayGoal(
    val goal: Goal,
    val macroGoals: MacroGoals,
    val isEstimated: Boolean
)

/**
 * Today always uses the current goal. A past day uses the goal recorded on that day, or the most
 * recent one recorded before it (the goal had not changed since). Days before any record fall back
 * to the current goal and are marked as estimated.
 */
fun resolveDayGoal(
    date: LocalDate,
    today: LocalDate,
    dailyGoals: List<DailyGoal>,
    currentGoal: Goal?,
    currentMacroGoals: MacroGoals?
): DayGoal? {
    if (date != today) {
        val recorded = dailyGoals.filter { it.date <= date }.maxByOrNull { it.date }
        if (recorded != null) {
            return DayGoal(goal = recorded.goal, macroGoals = recorded.macroGoals, isEstimated = false)
        }
    }
    if (currentGoal == null || currentMacroGoals == null) return null
    return DayGoal(goal = currentGoal, macroGoals = currentMacroGoals, isEstimated = date != today)
}
