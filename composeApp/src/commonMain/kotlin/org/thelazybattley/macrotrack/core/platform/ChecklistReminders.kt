package org.thelazybattley.macrotrack.core.platform

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/** A checklist reminder as it shows on the phone. */
data class ChecklistReminderMessage(val title: String, val body: String)

/** A reminder for [itemId] on the checklist day [date], going off at [at]. */
data class ChecklistReminder(val itemId: Long, val date: LocalDate, val at: LocalDateTime, val message: ChecklistReminderMessage)

/** The buttons on a checklist reminder. */
enum class ChecklistReminderAction { DONE, SNOOZE, SKIP }

/** Sets the checklist reminders on the phone, replacing any set before. */
interface ChecklistReminderScheduler {

    /** How many days, today first, [schedule] wants reminders for. */
    val days: Int

    /** [upcoming] is soonest first. */
    suspend fun schedule(upcoming: List<ChecklistReminder>)
}
