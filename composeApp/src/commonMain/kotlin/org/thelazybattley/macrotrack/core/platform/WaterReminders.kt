package org.thelazybattley.macrotrack.core.platform

import kotlinx.datetime.LocalDateTime

/** A water reminder as it shows on the phone. */
data class WaterReminderMessage(val title: String, val body: String)

data class WaterReminder(val at: LocalDateTime, val message: WaterReminderMessage)

/** Sets the water reminders on the phone, replacing any set before. */
interface WaterReminderScheduler {

    /** How many days, today first, [schedule] wants reminders for. */
    val days: Int

    /**
     * [upcoming] is soonest first. Where the phone can check when one is due (Android), it's only the times;
     * elsewhere each shows as written.
     */
    suspend fun schedule(upcoming: List<WaterReminder>)
}
