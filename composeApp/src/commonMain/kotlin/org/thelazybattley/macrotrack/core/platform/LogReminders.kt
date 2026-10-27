package org.thelazybattley.macrotrack.core.platform

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.LogReminder
import org.thelazybattley.macrotrack.domain.model.LogReminderKind

/** A button on a missed-log reminder. */
enum class LogReminderAction {
    /** Opens Add meal, or Log weight. */
    LOG,
    SAME_AS_YESTERDAY,
    QUICK_ADD,
    LATER,
    SKIP,
    KEEP_ON,
    CHANGE_TIMES
}

/** A reminder as it shows on the phone. [actions] is empty when quick actions are off. */
data class LogReminderMessage(
    val kind: LogReminderKind,
    val date: LocalDate,
    val title: String,
    val body: String,
    val actions: List<Pair<LogReminderAction, String>>
)

/** Sets the missed-log reminders on the phone, replacing any set before. */
interface LogReminderScheduler {

    /** How far ahead, in days, [schedule] wants the reminders it's given. */
    val daysAhead: Int

    /** False where a test can't show while the app is open. */
    val canSendTest: Boolean

    /**
     * [upcoming] is soonest first and can start with reminders already due but not sent yet; [message] writes one
     * out, with the log as it is now.
     */
    suspend fun schedule(upcoming: List<LogReminder>, message: suspend (LogReminder) -> LogReminderMessage)

    suspend fun sendTest(message: LogReminderMessage)
}
