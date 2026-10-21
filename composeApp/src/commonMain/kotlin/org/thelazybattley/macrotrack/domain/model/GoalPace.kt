package org.thelazybattley.macrotrack.domain.model

import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * How fast to lose or gain weight. Stored in kg per month; the stepper moves in 0.1 kg or 0.2 lb steps, whichever unit
 * is shown.
 */
object GoalPace {
    const val DEFAULT_KG_PER_MONTH = 1.2

    /** At or under these the pace stops being recommended and gets a warning. */
    const val MAX_COMFORTABLE_DEFICIT = 600
    const val MAX_COMFORTABLE_SURPLUS = 500

    private const val KCAL_PER_KG = 7700.0
    private const val DAYS_PER_MONTH = 30.4
    private const val RECOMMENDED_KCAL = 300

    private val metricRange = PaceRange(step = 0.1, min = 0.2, max = 4.0)
    private val imperialRange = PaceRange(step = 0.2, min = 0.4, max = 8.8)

    /** The daily calorie change for [kgPerMonth], rounded to 10 kcal: 1.2 kg a month is 300 kcal a day. */
    fun dailyCalories(kgPerMonth: Double): Int = (kgPerMonth * KCAL_PER_KG / DAYS_PER_MONTH / 10).roundToInt() * 10

    fun isRecommended(kgPerMonth: Double): Boolean = dailyCalories(kgPerMonth = kgPerMonth) == RECOMMENDED_KCAL

    /** [kgPerMonth] as shown in [unitSystem], to a tenth. */
    fun shown(kgPerMonth: Double, unitSystem: UnitSystem): Double = round(unitSystem.weightFromKg(kg = kgPerMonth) * 10) / 10

    /** One step faster ([faster]) or slower in the shown unit, kept within its range; returns kg per month. */
    fun step(kgPerMonth: Double, unitSystem: UnitSystem, faster: Boolean): Double {
        val range = rangeFor(unitSystem = unitSystem)
        val next = shown(kgPerMonth = kgPerMonth, unitSystem = unitSystem) + if (faster) range.step else -range.step
        return unitSystem.weightToKg(value = round(next.coerceIn(range.min, range.max) * 10) / 10)
    }

    fun canStep(kgPerMonth: Double, unitSystem: UnitSystem, faster: Boolean): Boolean {
        val range = rangeFor(unitSystem = unitSystem)
        val shown = shown(kgPerMonth = kgPerMonth, unitSystem = unitSystem)
        return if (faster) shown < range.max - EPSILON else shown > range.min + EPSILON
    }

    /** The fastest step in [unitSystem] no faster than [kgPerMonth], in kg per month; null if that's under the slowest. */
    fun stepAtOrBelow(kgPerMonth: Double, unitSystem: UnitSystem): Double? {
        val range = rangeFor(unitSystem = unitSystem)
        val shown = floor(unitSystem.weightFromKg(kg = kgPerMonth) / range.step + EPSILON) * range.step
        if (shown < range.min - EPSILON) return null
        return unitSystem.weightToKg(value = round(shown.coerceAtMost(range.max) * 10) / 10)
    }

    private fun rangeFor(unitSystem: UnitSystem) = if (unitSystem == UnitSystem.IMPERIAL) imperialRange else metricRange

    private data class PaceRange(val step: Double, val min: Double, val max: Double)

    private const val EPSILON = 1e-9
}

/** The daily calorie change for this goal at [paceKgPerMonth]: negative to lose, positive to gain, 0 to maintain. */
fun Goal.calorieAdjust(paceKgPerMonth: Double): Int = direction * GoalPace.dailyCalories(kgPerMonth = paceKgPerMonth)
