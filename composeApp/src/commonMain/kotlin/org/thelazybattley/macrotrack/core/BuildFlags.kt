package org.thelazybattley.macrotrack.core

/** False on debug builds, so every Pro feature stays open and the trial and paywall never show while developing. */
expect val isPaywallEnabled: Boolean

/** False on debug builds, so taps while developing never reach Analytics. Crash reports are sent from every build. */
expect val isAnalyticsAllowed: Boolean

/** True on debug builds only: steps, walks and runs, and everything built on them, aren't shipped yet. */
expect val isActivityEnabled: Boolean
