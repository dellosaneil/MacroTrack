package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewHistoryTest {

    private val today = LocalDate(2026, 10, 16)

    @Test
    fun aWeekLongStreakIsTheFirstAsk() {
        assertTrue(ReviewHistory().isDue(currentStreak = REVIEW_STREAK_DAYS, today = today))
    }

    @Test
    fun aShorterStreakIsNotAsked() {
        assertFalse(ReviewHistory().isDue(currentStreak = REVIEW_STREAK_DAYS - 1, today = today))
    }

    @Test
    fun notAskedAgainSoonAfterTheLastAsk() {
        val history = ReviewHistory(lastAsked = LocalDate(2026, 7, 1), timesAsked = 1)
        assertFalse(history.isDue(currentStreak = 30, today = today))
    }

    @Test
    fun askedAgainOnceEnoughDaysHavePassed() {
        val history = ReviewHistory(lastAsked = LocalDate(2026, 6, 18), timesAsked = 1)
        assertTrue(history.isDue(currentStreak = REVIEW_STREAK_DAYS, today = today))
    }

    @Test
    fun neverAskedMoreThanTheLimit() {
        val history = ReviewHistory(lastAsked = LocalDate(2025, 1, 1), timesAsked = REVIEW_MAX_ASKS)
        assertFalse(history.isDue(currentStreak = 100, today = today))
    }
}
