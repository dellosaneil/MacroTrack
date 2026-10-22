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
    const val PURCHASE_UNAVAILABLE = "purchase_unavailable"
    const val PRO_UNLOCKED = "pro_unlocked"
    const val BACKUP_CREATED = "backup_created"
    const val BACKUP_RESTORED = "backup_restored"
    const val REVIEW_ASKED = "review_asked"
    const val DATA_DELETED = "data_deleted"
    const val CHECK_IN_DONE = "check_in_done"
    const val FOOD_COPIED = "food_copied"
    const val FOOD_PLANNED = "food_planned"
    const val PLANNED_FOOD_DONE = "planned_food_done"
    const val REPEATING_MEAL_SAVED = "repeating_meal_saved"
    const val ACTIVITY_RECORDED = "activity_recorded"
    const val STEP_GOAL_SET = "step_goal_set"

    const val PARAM_GOAL = "goal"
    const val PARAM_MEAL_TYPE = "meal_type"
    const val PARAM_QUICK_ADD = "quick_add"
    const val PARAM_PAST_DAY = "past_day"
    const val PARAM_RESULT = "result"
    const val PARAM_SCOPE = "scope"
    const val PARAM_FEATURE = "feature"
    const val PARAM_STEP = "step"
    const val PARAM_CODE = "code"
    const val PARAM_TYPE = "type"
    const val PARAM_NAMED = "named"
}
