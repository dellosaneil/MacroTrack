package org.thelazybattley.macrotrack.core.platform

import androidx.compose.runtime.Composable

/**
 * Access to something the system guards, like notifications. [isPermanentlyDenied] means the system won't ask again,
 * so [request] opens the app's settings.
 */
class PermissionState(
    val isGranted: Boolean,
    val isPermanentlyDenied: Boolean,
    val request: () -> Unit
)

/** Permission to post photo reminders. Granted where the platform doesn't ask. */
@Composable
expect fun rememberNotificationPermissionState(): PermissionState
