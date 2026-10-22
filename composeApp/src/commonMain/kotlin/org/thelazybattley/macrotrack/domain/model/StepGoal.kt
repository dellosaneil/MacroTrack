package org.thelazybattley.macrotrack.domain.model

import kotlin.math.ceil

/** The daily step goal: one number for every day, counted midnight to midnight. */
object StepGoal {
    const val DEFAULT = 8_000
    const val MIN = 1_000
    const val MAX = 50_000

    /** − and + move the goal by this much, and suggestions round up to it. */
    const val STEP = 500

    /** The quick picks next to the suggestion. */
    val PRESETS = listOf(6_000, 8_000, 10_000, 12_000)

    /** Days the suggestion and the goal preview look back over. */
    const val LOOKBACK_DAYS = 14

    /** The evening nudge fires at this hour, only when more than [NUDGE_MIN_SHORT] steps short. */
    const val NUDGE_HOUR = 18
    const val NUDGE_MIN_SHORT = 1_000

    /** A goal above the average by this share is a big jump; below it by [EASY_SHARE] is easy. */
    const val BIG_JUMP_SHARE = 1.25
    const val EASY_SHARE = 0.7

    fun isValid(goal: Int): Boolean = goal in MIN..MAX

    /** The recent average, rounded up to the next [STEP]; null with nothing counted yet. */
    fun suggested(dailySteps: List<Int>): Int? {
        val counted = dailySteps.filter { it > 0 }
        if (counted.isEmpty()) return null
        val average = counted.average()
        return (ceil(x = average / STEP) * STEP).toInt().coerceIn(minimumValue = MIN, maximumValue = MAX)
    }

    /** How many of [dailySteps] reached [goal]. */
    fun daysHit(dailySteps: List<Int>, goal: Int): Int = dailySteps.count { it >= goal }

    /** The next goal down or up by [STEP] from [goal], snapping to the grid first. */
    fun stepDown(goal: Int): Int = ((goal + STEP - 1) / STEP * STEP - STEP).coerceIn(minimumValue = MIN, maximumValue = MAX)

    fun stepUp(goal: Int): Int = (goal / STEP * STEP + STEP).coerceIn(minimumValue = MIN, maximumValue = MAX)
}
