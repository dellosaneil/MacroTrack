package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/** How a drink was added: the quick-add glass or bottle, or an amount typed in. */
enum class WaterKind { GLASS, BOTTLE, OTHER }

/** One drink of water. Water is tracked on its own and never changes calories. */
data class WaterEntry(
    val id: Long = 0,
    val date: LocalDate,
    val amountMl: Int,
    val kind: WaterKind,
    val loggedAtMillis: Long
)

/** Millilitres of water per kilogram of body weight in a goal set from weight. */
const val WATER_ML_PER_KG = 35

/** The goal when there's no weight to work from. */
const val DEFAULT_WATER_GOAL_ML = 2_500

const val WATER_GOAL_MIN_ML = 500
const val WATER_GOAL_MAX_ML = 6_000
const val WATER_AMOUNT_MIN_ML = 10
const val WATER_AMOUNT_MAX_ML = 3_000
const val WATER_SIZE_MAX_ML = 2_000

/** The usual pace spreads the goal evenly over these hours of the day, in minutes after midnight. */
const val WATER_DAY_START_MINUTES = 7 * 60
const val WATER_DAY_END_MINUTES = 21 * 60

/**
 * Water settings. The goal follows weigh-ins when [isGoalFromWeight], else it's [customGoalMl]. Reminders come every
 * [reminderEveryHours] between [reminderStartMinutes] and [reminderEndMinutes], only when behind the usual pace.
 */
@Serializable
data class WaterSettings(
    val isGoalFromWeight: Boolean = true,
    val customGoalMl: Int = DEFAULT_WATER_GOAL_ML,
    val glassMl: Int = 250,
    val bottleMl: Int = 500,
    val isReminderOn: Boolean = false,
    val reminderEveryHours: Int = 2,
    val reminderStartMinutes: Int = 9 * 60,
    val reminderEndMinutes: Int = 21 * 60,
    val isShownOnToday: Boolean = true
) {
    fun goalMl(weightKg: Double?): Int = if (isGoalFromWeight) waterGoalFromWeight(weightKg = weightKg) else customGoalMl

    fun sizeOf(kind: WaterKind): Int? = when (kind) {
        WaterKind.GLASS -> glassMl
        WaterKind.BOTTLE -> bottleMl
        WaterKind.OTHER -> null
    }

    companion object {
        val REMINDER_HOURS = listOf(1, 2, 3)
    }
}

/** [WATER_ML_PER_KG] for each kilogram, rounded to 100 ml; [DEFAULT_WATER_GOAL_ML] without a weight. */
fun waterGoalFromWeight(weightKg: Double?): Int {
    if (weightKg == null || weightKg <= 0) return DEFAULT_WATER_GOAL_ML
    return ((weightKg * WATER_ML_PER_KG / 100).roundToInt() * 100).coerceIn(minimumValue = WATER_GOAL_MIN_ML, maximumValue = WATER_GOAL_MAX_ML)
}

/** Where someone drinking at an even pace through the day would be by [minutes] after midnight, rounded to 50 ml. */
fun waterPaceMl(goalMl: Int, minutes: Int): Int {
    val share = ((minutes - WATER_DAY_START_MINUTES).toDouble() / (WATER_DAY_END_MINUTES - WATER_DAY_START_MINUTES)).coerceIn(0.0, 1.0)
    return (goalMl * share / 50).roundToInt() * 50
}

/** The minutes after midnight a water reminder checks the pace, from the start of the window to its end. */
fun WaterSettings.reminderMinutes(): List<Int> {
    if (!isReminderOn || reminderEndMinutes <= reminderStartMinutes) return emptyList()
    return generateSequence(seed = reminderStartMinutes + reminderEveryHours * 60) { it + reminderEveryHours * 60 }
        .takeWhile { it <= reminderEndMinutes }
        .toList()
}

/** Water drunk on one day, for the last-7-days chart. */
data class WaterDay(val date: LocalDate, val totalMl: Int)

/**
 * Today's water: what's been drunk, against [goalMl]. [autoGoalMl] is what the goal from weight comes to and
 * [weightKg] what it's from, null without one. [days] are the last 7, today last.
 */
data class WaterSummary(
    val date: LocalDate,
    val entries: List<WaterEntry>,
    val goalMl: Int,
    val autoGoalMl: Int,
    val weightKg: Double?,
    val settings: WaterSettings,
    val days: List<WaterDay>
) {
    val totalMl: Int get() = entries.sumOf { it.amountMl }
    val leftMl: Int get() = (goalMl - totalMl).coerceAtLeast(minimumValue = 0)
    val isGoalMet: Boolean get() = totalMl >= goalMl
    val progress: Float get() = if (goalMl <= 0) 0f else (totalMl.toFloat() / goalMl).coerceIn(0f, 1f)
}

/** A water amount in the unit shown: whole ml, or whole fl oz in imperial. */
fun UnitSystem.waterFromMl(ml: Int): Int = if (this == UnitSystem.IMPERIAL) (ml / UnitSystem.ML_PER_FLUID_OUNCE).roundToInt() else ml

fun UnitSystem.waterToMl(shown: Int): Int = if (this == UnitSystem.IMPERIAL) (shown * UnitSystem.ML_PER_FLUID_OUNCE).roundToInt() else shown

val UnitSystem.waterUnit: String get() = if (this == UnitSystem.IMPERIAL) UnitSystem.FLUID_OUNCE else "ml"

/** How much the quick-add sizes move per tap: 50 ml, or 1 fl oz. */
val UnitSystem.waterSizeStepMl: Int get() = if (this == UnitSystem.IMPERIAL) UnitSystem.ML_PER_FLUID_OUNCE.roundToInt() else 50
