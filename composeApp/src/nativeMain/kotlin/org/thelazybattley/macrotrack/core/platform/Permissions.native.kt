package org.thelazybattley.macrotrack.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.launch
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private suspend fun notificationStatus(): UNAuthorizationStatus = suspendCoroutine { continuation ->
    UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
        continuation.resume(value = settings?.authorizationStatus ?: UNAuthorizationStatusDenied)
    }
}

/** Asks the first time; after that it answers straight away with what was chosen. */
internal suspend fun requestNotificationAuthorization(): Boolean = suspendCoroutine { continuation ->
    UNUserNotificationCenter.currentNotificationCenter()
        .requestAuthorizationWithOptions(options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { isGranted, _ ->
            continuation.resume(value = isGranted)
        }
}

/**
 * Until iOS has been asked, this reads as not granted, so turning reminders on asks. Once someone says no, iOS
 * won't ask again and [PermissionState.request] opens the app's settings.
 */
@Composable
actual fun rememberNotificationPermissionState(): PermissionState {
    val scope = rememberCoroutineScope()
    // Null until the first answer, read as granted so a banner doesn't flash up.
    var status by remember { mutableStateOf<UNAuthorizationStatus?>(value = null) }
    // Picks up access changed in settings while the app was in the background.
    LifecycleResumeEffect(key1 = Unit) {
        scope.launch { status = notificationStatus() }
        onPauseOrDispose {}
    }
    val current = status
    return PermissionState(
        isGranted = current == null || current == UNAuthorizationStatusAuthorized ||
            current == UNAuthorizationStatusProvisional || current == UNAuthorizationStatusEphemeral,
        isPermanentlyDenied = current == UNAuthorizationStatusDenied,
        request = {
            if (current == UNAuthorizationStatusDenied) {
                NSURL.URLWithString(URLString = UIApplicationOpenSettingsURLString)?.let { url ->
                    UIApplication.sharedApplication.openURL(url = url, options = emptyMap<Any?, Any>(), completionHandler = null)
                }
            } else {
                scope.launch {
                    requestNotificationAuthorization()
                    status = notificationStatus()
                }
            }
        }
    )
}

/** iOS delivers scheduled notifications on time, so there's nothing to ask for. */
@Composable
actual fun rememberExactAlarmPermissionState(): PermissionState =
    remember { PermissionState(isGranted = true, isPermanentlyDenied = false, request = {}) }
