package org.thelazybattley.macrotrack.core

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual val isPaywallEnabled: Boolean = !Platform.isDebugBinary

@OptIn(ExperimentalNativeApi::class)
actual val isAnalyticsAllowed: Boolean = !Platform.isDebugBinary

@OptIn(ExperimentalNativeApi::class)
actual val isActivityEnabled: Boolean = Platform.isDebugBinary
