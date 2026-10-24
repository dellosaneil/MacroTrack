package org.thelazybattley.macrotrack.core.platform

import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.photo_reminder_body
import macrotrack.composeapp.generated.resources.photo_reminder_title
import org.jetbrains.compose.resources.getString
import org.thelazybattley.macrotrack.domain.model.PhotoAlbum
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

private const val ID_PREFIX = "photo-reminder-"

/** A repeating notification per album and day; iOS keeps them across restarts, so each change replaces them all. */
class IosPhotoReminderScheduler : PhotoReminderScheduler {

    override suspend fun schedule(albums: List<PhotoAlbum>) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val requests = albums.flatMap { album ->
            val title = getString(resource = Res.string.photo_reminder_title)
            val body = getString(resource = Res.string.photo_reminder_body, album.name)
            album.reminderDays.map { day ->
                val content = UNMutableNotificationContent().apply {
                    setTitle(title)
                    setBody(body)
                    setSound(UNNotificationSound.defaultSound)
                }
                // iOS counts weekdays from Sunday = 1.
                val components = NSDateComponents().apply {
                    weekday = (day.ordinal + 1) % 7 + 1L
                    hour = (album.reminderMinutes / 60).toLong()
                    minute = (album.reminderMinutes % 60).toLong()
                }
                UNNotificationRequest.requestWithIdentifier(
                    identifier = "$ID_PREFIX${album.id}-${day.name}",
                    content = content,
                    trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents = components, repeats = true)
                )
            }
        }
        center.getPendingNotificationRequestsWithCompletionHandler { pending ->
            val old = pending.orEmpty().filterIsInstance<UNNotificationRequest>().map { it.identifier }.filter { it.startsWith(prefix = ID_PREFIX) }
            center.removePendingNotificationRequestsWithIdentifiers(identifiers = old)
            if (requests.isEmpty()) return@getPendingNotificationRequestsWithCompletionHandler
            // Asks the first time; after that it answers straight away with what was chosen.
            center.requestAuthorizationWithOptions(options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { isGranted, _ ->
                if (isGranted) requests.forEach { center.addNotificationRequest(request = it, withCompletionHandler = null) }
            }
        }
    }
}
