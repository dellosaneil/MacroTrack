package org.thelazybattley.macrotrack.features.calendar

import kotlinx.datetime.LocalDate

interface CalendarCallbacks {
    fun onPreviousMonth()
    fun onNextMonth()
    fun onSelectDate(date: LocalDate)

    /** Asks Food Log to show [date] when it is next shown. */
    fun onOpenInFoodLog(date: LocalDate)

    companion object {
        fun default() = object : CalendarCallbacks {
            override fun onPreviousMonth() = Unit
            override fun onNextMonth() = Unit
            override fun onSelectDate(date: LocalDate) = Unit
            override fun onOpenInFoodLog(date: LocalDate) = Unit
        }
    }
}
