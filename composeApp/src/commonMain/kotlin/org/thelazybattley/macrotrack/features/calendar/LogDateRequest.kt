package org.thelazybattley.macrotrack.features.calendar

import kotlinx.datetime.LocalDate

/**
 * Hands a date back to Food Log, which reads it the next time it is shown: a day picked on the calendar, or the day
 * Add meal was opened for. Food Log's view model lives in the bottom-nav graph, so a back-stack result can't reach it.
 */
class LogDateRequest {
    private var pending: LocalDate? = null
    private var pendingEntryId: Long? = null

    /** [entryId] opens that logged entry once the day shows, as food history does. */
    fun request(date: LocalDate, entryId: Long? = null) {
        pending = date
        pendingEntryId = entryId
    }

    fun consume(): LocalDate? = pending.also { pending = null }

    fun consumeEntry(): Long? = pendingEntryId.also { pendingEntryId = null }
}
