package org.thelazybattley.macrotrack.core

import org.thelazybattley.macrotrack.BuildConfig

actual val isPaywallEnabled: Boolean = !BuildConfig.DEBUG

actual val isAnalyticsAllowed: Boolean = !BuildConfig.DEBUG

actual val isActivityEnabled: Boolean = BuildConfig.DEBUG
