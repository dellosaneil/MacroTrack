package org.thelazybattley.macrotrack.features.profile.pastgoal

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.Goal

interface PastGoalCallbacks {
    fun onMonthTapped(month: LocalDate)
    fun onOpenDateSheet(side: DateSide)
    fun onSheetMonthChanged(month: LocalDate)
    fun onDatePicked(date: LocalDate)
    fun onCloseDateSheet()
    fun onGoalSelected(goal: Goal)
    fun onApply()

    companion object {
        fun default() = object : PastGoalCallbacks {
            override fun onMonthTapped(month: LocalDate) = Unit
            override fun onOpenDateSheet(side: DateSide) = Unit
            override fun onSheetMonthChanged(month: LocalDate) = Unit
            override fun onDatePicked(date: LocalDate) = Unit
            override fun onCloseDateSheet() = Unit
            override fun onGoalSelected(goal: Goal) = Unit
            override fun onApply() = Unit
        }
    }
}
