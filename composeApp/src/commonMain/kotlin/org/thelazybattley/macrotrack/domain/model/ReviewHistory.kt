package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** Logging this many days in a row is the moment to ask for a rating. */
const val REVIEW_STREAK_DAYS = 7

/** Days to wait before asking again after the user was last asked. */
const val REVIEW_REPEAT_DAYS = 120

/** Never ask more often than this, however long the user keeps the app. */
const val REVIEW_MAX_ASKS = 3

/**
 * When the user was last asked to rate the app and how many times in all. The store decides whether its
 * rating dialog actually shows, so this only keeps the app from asking at the wrong moment or too often.
 */
data class ReviewHistory(
    val lastAsked: LocalDate? = null,
    val timesAsked: Int = 0
) {

    fun isDue(currentStreak: Int, today: LocalDate): Boolean {
        if (currentStreak < REVIEW_STREAK_DAYS || timesAsked >= REVIEW_MAX_ASKS) return false
        return lastAsked == null || lastAsked.daysUntil(other = today) >= REVIEW_REPEAT_DAYS
    }
}
