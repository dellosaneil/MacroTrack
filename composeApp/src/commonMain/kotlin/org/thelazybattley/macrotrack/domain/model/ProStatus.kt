package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

/** How long a new install gets every Pro feature before it drops to the free version. */
const val PRO_TRIAL_DAYS = 30

/** Free recipes before saving another one needs Pro. Recipes already saved always stay usable. */
const val FREE_RECIPE_LIMIT = 20

/**
 * Whether Pro features are open: always after the one-time purchase, otherwise for [PRO_TRIAL_DAYS]
 * from [trialStart].
 */
data class ProStatus(
    val isPurchased: Boolean = false,
    val trialStart: LocalDate? = null,
    val today: LocalDate? = null
) {
    val trialEnd: LocalDate? get() = trialStart?.plus(period = DatePeriod(days = PRO_TRIAL_DAYS))

    /** Whole days of trial left, counting today; 0 once it has ended. */
    val trialDaysLeft: Int
        get() {
            val end = trialEnd ?: return 0
            val now = today ?: return 0
            return now.daysUntil(other = end).coerceAtLeast(minimumValue = 0)
        }

    val isTrialActive: Boolean get() = !isPurchased && trialDaysLeft > 0

    val hasTrialEnded: Boolean get() = !isPurchased && trialStart != null && trialDaysLeft == 0

    /** Before the trial start is known (the first moments after launch), Pro stays open rather than flashing locks. */
    val isActive: Boolean get() = isPurchased || trialStart == null || trialDaysLeft > 0
}
