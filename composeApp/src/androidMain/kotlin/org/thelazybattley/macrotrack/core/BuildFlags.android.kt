package org.thelazybattley.macrotrack.core

import org.thelazybattley.macrotrack.BuildConfig

actual val isPaywallEnabled: Boolean = !BuildConfig.DEBUG
