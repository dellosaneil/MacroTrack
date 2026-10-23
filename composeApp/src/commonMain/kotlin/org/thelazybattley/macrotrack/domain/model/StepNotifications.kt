package org.thelazybattley.macrotrack.domain.model

import kotlinx.serialization.Serializable

/** The second number the steps notification shows next to today's steps. */
enum class StepNotificationExtra {
    STEPS_TO_GOAL,
    DISTANCE,

    /** Calories burned; Pro, like calories burned everywhere else. */
    CALORIES
}

/**
 * The silent all-day steps notification and the one-off step alerts. The evening nudge's on/off is kept apart, as
 * it was before these settings existed; only its hour is here.
 */
@Serializable
data class StepNotificationSettings(
    /** Show today's steps: off until turned on in Profile › Steps & walks. */
    val isOn: Boolean = false,
    val isOnLockScreen: Boolean = true,
    val extra: StepNotificationExtra = StepNotificationExtra.DISTANCE,
    /** Once a day, the moment the goal is reached. */
    val isGoalAlertOn: Boolean = true,
    val nudgeHour: Int = StepGoal.NUDGE_HOUR
) {
    companion object {
        /** The evening nudge times offered: 5 PM to 8 PM. */
        val NUDGE_HOURS = listOf(17, 18, 19, 20)

        /** The steps notification is refreshed about this often. */
        const val REFRESH_MINUTES = 15
    }
}
