package org.thelazybattley.macrotrack.features.calendar

import kotlinx.datetime.LocalDate

/**
 * Hands a date picked on the calendar back to Food Log, which reads it the next time it is shown.
 * Food Log's view model lives in the bottom-nav graph, so a back-stack result can't reach it.
 */
class LogDateRequest {
    private var pending: LocalDate? = null

    fun request(date: LocalDate) {
        pending = date
    }

    fun consume(): LocalDate? = pending.also { pending = null }
}
