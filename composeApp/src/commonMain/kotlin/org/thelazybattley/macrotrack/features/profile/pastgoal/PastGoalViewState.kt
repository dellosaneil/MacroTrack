package org.thelazybattley.macrotrack.features.profile.pastgoal

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.GoalPeriod
import org.thelazybattley.macrotrack.domain.model.PastGoalChange
import org.thelazybattley.macrotrack.domain.model.applyChange
import org.thelazybattley.macrotrack.domain.model.goalPeriods
import org.thelazybattley.macrotrack.domain.model.pastGoalChange
import org.thelazybattley.macrotrack.domain.model.withCurrentGoal
import org.thelazybattley.macrotrack.features.calendar.firstOfMonth

enum class DateSide { FROM, UNTIL }

data class PastGoalViewState(
    val today: LocalDate = getCurrentDate(),
    val records: List<DailyGoal> = emptyList(),
    /** Today's goal from the user's details. */
    val current: DailyGoal? = null,
    /** The first day with anything logged or recorded; nothing before it can be changed. */
    val earliestDate: LocalDate? = null,
    val loggedDates: Set<LocalDate> = emptySet(),
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    /** True after the first month is tapped, until the last one is. */
    val isPickingLastMonth: Boolean = false,
    val selectedGoal: Goal = Goal.MAINTAIN_WEIGHT,
    /** Targets for each goal, worked out from the weight at [start]. */
    val targets: Map<Goal, MacroGoals> = emptyMap(),
    val weightAtStart: Double? = null,
    val sheet: DateSide? = null,
    val sheetMonth: LocalDate? = null,
    val isSaved: Boolean = false
) {
    /** Only days before today can be changed; today follows the current goal. */
    val latestDate: LocalDate get() = today.minus(period = DatePeriod(days = 1))

    val hasPastDays: Boolean get() = earliestDate != null && earliestDate <= latestDate

    val months: List<LocalDate>
        get() {
            val first = earliestDate?.takeIf { hasPastDays }?.firstOfMonth() ?: return emptyList()
            return generateSequence(seed = first) { it.plus(period = DatePeriod(months = 1)) }
                .takeWhile { it <= latestDate }
                .toList()
        }

    val change: PastGoalChange?
        get() {
            val start = start ?: return null
            val end = end ?: return null
            val macroGoals = targets[selectedGoal] ?: return null
            return pastGoalChange(
                records = records,
                today = today,
                current = current,
                start = start,
                end = end,
                newGoal = DailyGoal(date = start, goal = selectedGoal, macroGoals = macroGoals)
            )
        }

    /** Newest first, as it will be after saving. */
    val preview: List<GoalPeriod>
        get() {
            val change = change ?: return emptyList()
            val updated = withCurrentGoal(records = applyChange(records = records, change = change), today = today, current = current)
            return goalPeriods(records = updated, today = today).reversed()
        }

    val dayCount: Int get() = if (start == null || end == null) 0 else end.toEpochDays().minus(start.toEpochDays()).toInt() + 1

    val loggedDayCount: Int
        get() {
            val start = start ?: return 0
            val end = end ?: return 0
            return loggedDates.count { it in start..end }
        }

    fun isSelectable(date: LocalDate, side: DateSide): Boolean {
        val earliest = earliestDate ?: return false
        if (date < earliest || date > latestDate) return false
        return when (side) {
            DateSide.FROM -> end == null || date <= end
            DateSide.UNTIL -> start == null || date >= start
        }
    }
}
