package org.thelazybattley.macrotrack.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** iOS asks when the first photo reminder is scheduled, so there's nothing to ask for up front. */
@Composable
actual fun rememberNotificationPermissionState(): PermissionState =
    remember { PermissionState(isGranted = true, isPermanentlyDenied = false, request = {}) }
