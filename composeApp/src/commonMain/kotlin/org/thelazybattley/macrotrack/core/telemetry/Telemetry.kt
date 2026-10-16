package org.thelazybattley.macrotrack.core.telemetry

/**
 * Crash reports and usage analytics. Firebase backs it on both platforms; when Firebase is not set up, every call
 * does nothing. Never pass food names, weights or anything else the user typed: events say what happened, not what
 * was in it.
 */
interface Telemetry {

    fun logEvent(name: String, params: Map<String, Any> = emptyMap())

    fun logScreen(name: String) = logEvent(name = SCREEN_VIEW, params = mapOf(SCREEN_NAME to name))

    /** Sends a caught exception as a non-fatal crash report. */
    fun recordException(throwable: Throwable)

    /** A breadcrumb attached to the next crash report. */
    fun log(message: String)

    fun setCollectionEnabled(crashReports: Boolean, analytics: Boolean)

    companion object {
        // The names Firebase uses for its own screen tracking, so these land in its screens report.
        private const val SCREEN_VIEW = "screen_view"
        private const val SCREEN_NAME = "screen_name"
    }
}

object NoOpTelemetry : Telemetry {
    override fun logEvent(name: String, params: Map<String, Any>) = Unit
    override fun recordException(throwable: Throwable) = Unit
    override fun log(message: String) = Unit
    override fun setCollectionEnabled(crashReports: Boolean, analytics: Boolean) = Unit
}

/** Event and parameter names. Firebase allows letters, digits and underscores, up to 40 characters. */
object TelemetryEvent {
    const val ONBOARDING_COMPLETE = "onboarding_complete"
    const val TOUR_COMPLETE = "tour_complete"
    const val FOOD_LOGGED = "food_logged"
    const val FOOD_CREATED = "food_created"
    const val RECIPE_CREATED = "recipe_created"
    const val WEIGHT_LOGGED = "weight_logged"
    const val BARCODE_LOOKUP = "barcode_lookup"
    const val PAYWALL_SHOWN = "paywall_shown"
    const val PURCHASE_STARTED = "purchase_started"
    const val PRO_UNLOCKED = "pro_unlocked"
    const val BACKUP_CREATED = "backup_created"
    const val BACKUP_RESTORED = "backup_restored"

    const val PARAM_GOAL = "goal"
    const val PARAM_MEAL_TYPE = "meal_type"
    const val PARAM_QUICK_ADD = "quick_add"
    const val PARAM_RESULT = "result"
    const val PARAM_FEATURE = "feature"
}
