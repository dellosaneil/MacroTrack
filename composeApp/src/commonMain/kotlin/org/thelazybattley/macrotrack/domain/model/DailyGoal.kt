package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.MacroGoals

/** The nutrition goal and targets that applied on a given day. */
data class DailyGoal(
    val date: LocalDate,
    val goal: Goal,
    val macroGoals: MacroGoals
)
