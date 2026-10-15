package org.thelazybattley.macrotrack.core

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual val isPaywallEnabled: Boolean = !Platform.isDebugBinary
