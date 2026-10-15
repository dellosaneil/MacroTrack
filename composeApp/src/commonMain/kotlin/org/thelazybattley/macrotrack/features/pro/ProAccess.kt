package org.thelazybattley.macrotrack.features.pro

import androidx.compose.runtime.compositionLocalOf

/** Pro state for any screen, with a way to open the paywall, provided once at the root of the app. */
class ProAccess(
    val state: ProViewState = ProViewState(),
    val openPaywall: (ProFeature?) -> Unit = {},
    val callbacks: ProCallbacks = ProCallbacks.default()
) {
    val isActive: Boolean get() = state.status.isActive
}

val LocalProAccess = compositionLocalOf { ProAccess() }
