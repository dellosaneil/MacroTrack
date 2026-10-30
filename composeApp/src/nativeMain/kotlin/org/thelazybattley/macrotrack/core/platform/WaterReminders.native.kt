package org.thelazybattley.macrotrack.core.platform

import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound

private const val ID_PREFIX = "water-reminder-"

/**
 * iOS can't look at the water drunk when a reminder is due, so each is written ahead and they're all set again as
 * drinks are logged while the app is open: catching up takes back the ones no longer behind. Today and tomorrow
 * stay well inside the 64 iOS allows, alongside the other reminders.
 */
class IosWaterReminderScheduler : WaterReminderScheduler {

    override val days: Int = 2

    override suspend fun schedule(upcoming: List<WaterReminder>) {
        val requests = upcoming.map { reminder ->
            val content = UNMutableNotificationContent().apply {
                setTitle(reminder.message.title)
                setBody(reminder.message.body)
                setSound(UNNotificationSound.defaultSound)
                setUserInfo(mapOf(NOTIFICATION_ROUTE_KEY to AppDestinations.Root.Water.route))
            }
            val components = NSDateComponents().apply {
                year = reminder.at.year.toLong()
                month = reminder.at.month.ordinal + 1L
                day = reminder.at.day.toLong()
                hour = reminder.at.hour.toLong()
                minute = reminder.at.minute.toLong()
            }
            UNNotificationRequest.requestWithIdentifier(
                identifier = "$ID_PREFIX${reminder.at}",
                content = content,
                trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents = components, repeats = false)
            )
        }
        replacePendingNotifications(prefix = ID_PREFIX, requests = requests)
    }
}
