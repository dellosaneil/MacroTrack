package org.thelazybattley.macrotrack.core.platform

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.mp.KoinPlatform
import org.thelazybattley.macrotrack.features.reminders.NotificationRouteRequest
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** The key in a notification's user info for the screen a tap opens. */
internal const val NOTIFICATION_ROUTE_KEY = "route"

// One replace at a time: two overlapping ones could each take away what the other just set.
private val replaceLock = Mutex()

/**
 * Takes away every pending notification whose id starts with [prefix] and sets [requests] in their place, once
 * notifications are allowed. Waits for iOS to finish, so the next replace sees this one's.
 */
internal suspend fun replacePendingNotifications(prefix: String, requests: List<UNNotificationRequest>) = replaceLock.withLock {
    val center = UNUserNotificationCenter.currentNotificationCenter()
    val old = suspendCoroutine { continuation ->
        center.getPendingNotificationRequestsWithCompletionHandler { pending ->
            continuation.resume(
                value = pending.orEmpty().filterIsInstance<UNNotificationRequest>().map { it.identifier }.filter { it.startsWith(prefix = prefix) }
            )
        }
    }
    platform.Foundation.NSLog("DBGNOTIF replace prefix=$prefix old=${old.size} new=${requests.size}")
    center.removePendingNotificationRequestsWithIdentifiers(identifiers = old)
    if (requests.isEmpty()) return@withLock
    val granted = requestNotificationAuthorization()
    platform.Foundation.NSLog("DBGNOTIF granted=$granted")
    if (!granted) return@withLock
    requests.forEach { request ->
        suspendCoroutine { continuation ->
            center.addNotificationRequest(request = request) { error ->
                platform.Foundation.NSLog("DBGNOTIF add ${request.identifier} next=${(request.trigger as? platform.UserNotifications.UNCalendarNotificationTrigger)?.nextTriggerDate()} error=${error?.localizedDescription}")
                continuation.resume(value = Unit)
            }
        }
    }
    platform.Foundation.NSLog("DBGNOTIF done prefix=$prefix")
}

// A screen asked for before the app had started, by a tapped notification or app icon shortcut.
private var pendingRoute: String? = null

/** Opens [route] over the main screen, or once the app has started when it hasn't yet (see [deliverPendingRoute]). */
internal fun requestRoute(route: String) {
    val request = KoinPlatform.getKoinOrNull()?.getOrNull<NotificationRouteRequest>()
    if (request != null) request.request(route = route) else pendingRoute = route
}

/** Shows reminders while the app is open too (iOS hides them otherwise) and opens the screen a tapped one asks for. */
private class NotificationDelegate : NSObject(), UNUserNotificationCenterDelegateProtocol {

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
    ) {
        withCompletionHandler(UNNotificationPresentationOptionBanner or UNNotificationPresentationOptionList or UNNotificationPresentationOptionSound)
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        val route = didReceiveNotificationResponse.notification.request.content.userInfo[NOTIFICATION_ROUTE_KEY] as? String
        if (route != null) requestRoute(route = route)
        withCompletionHandler()
    }
}

// iOS keeps only a weak reference to its delegate.
private val delegate = NotificationDelegate()

/** Called as the app launches, before iOS hands over a tapped notification. */
fun installNotificationDelegate() {
    UNUserNotificationCenter.currentNotificationCenter().delegate = delegate
}

/** Opens the screen a notification or shortcut tapped before the app had started asked for. */
fun deliverPendingRoute() {
    val route = pendingRoute ?: return
    pendingRoute = null
    KoinPlatform.getKoinOrNull()?.getOrNull<NotificationRouteRequest>()?.request(route = route)
}
