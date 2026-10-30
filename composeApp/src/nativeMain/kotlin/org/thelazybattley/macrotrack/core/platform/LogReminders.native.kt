package org.thelazybattley.macrotrack.core.platform

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.thelazybattley.macrotrack.domain.model.LogReminder
import org.thelazybattley.macrotrack.features.reminders.logReminderRoute
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val ID_PREFIX = "log-reminder-"

/**
 * iOS can't check the log when a reminder is due, so each one is set ahead, written against the log as it is, and
 * they're all set again whenever the log changes while the app is open: logging lunch takes back today's lunch
 * reminder. A few days ahead keeps well under the 64 iOS allows, alongside photo reminders. A tap opens the
 * reminder's screen; the buttons are Android only for now.
 */
class IosLogReminderScheduler : LogReminderScheduler {

    override val daysAhead: Int = 4

    // Shows while the app is open through the notification delegate.
    override val canSendTest: Boolean = true

    @OptIn(ExperimentalTime::class)
    override suspend fun schedule(upcoming: List<LogReminder>, message: suspend (LogReminder) -> LogReminderMessage) {
        val now = Clock.System.now().toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
        platform.Foundation.NSLog("DBGNOTIF log schedule upcoming=${upcoming.size} now=$now first=${upcoming.firstOrNull()}")
        // A time already past can't be set here.
        val requests = upcoming.filter { it.at > now }.map { reminder ->
            val written = message(reminder)
            val components = NSDateComponents().apply {
                year = reminder.at.year.toLong()
                month = reminder.at.month.ordinal + 1L
                day = reminder.at.day.toLong()
                hour = reminder.at.hour.toLong()
                minute = reminder.at.minute.toLong()
            }
            UNNotificationRequest.requestWithIdentifier(
                identifier = "$ID_PREFIX${reminder.kind.name}-${reminder.at.date}",
                content = written.toContent(route = logReminderRoute(kind = reminder.kind, date = reminder.at.date)),
                trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents = components, repeats = false)
            )
        }
        replacePendingNotifications(prefix = ID_PREFIX, requests = requests)
    }

    override suspend fun sendTest(message: LogReminderMessage) {
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "${ID_PREFIX}test",
            content = message.toContent(),
            trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(timeInterval = 1.0, repeats = false)
        )
        if (!requestNotificationAuthorization()) return
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request = request, withCompletionHandler = null)
    }
}

private fun LogReminderMessage.toContent(route: String? = null) = UNMutableNotificationContent().apply {
    setTitle(title)
    setBody(body)
    setSound(UNNotificationSound.defaultSound)
    route?.let { setUserInfo(mapOf(NOTIFICATION_ROUTE_KEY to it)) }
}
