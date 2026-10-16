package org.thelazybattley.macrotrack.features.foodlog

import org.thelazybattley.macrotrack.core.AmountInput
import org.thelazybattley.macrotrack.core.roundToHundredths
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PortionUnit
import org.thelazybattley.macrotrack.features.addmeal.FULL_RECIPE_PERCENT
import kotlin.math.round
import kotlin.math.roundToInt

private const val RECIPE_STEP_PERCENT = 25.0
private const val OUNCE_STEP = 0.5

/**
 * A logged entry being changed. New macros are scaled from the log itself, so the edit matches
 * what was logged even if the food or recipe has changed since.
 */
data class EntryEdit(
    val log: FoodLog,
    /** The portion that was logged, in the food's stored unit, or the percentage eaten for a recipe. */
    val originalAmount: Double,
    /** The food's stored unit, or "%" for a recipe. */
    val unit: String,
    /** How the portion is shown and typed, e.g. oz for a food stored in grams when imperial. */
    val portionUnit: PortionUnit = PortionUnit(label = unit),
    val input: AmountInput = AmountInput.of(value = portionUnit.fromStored(amount = originalAmount)),
    val mealType: MealType = log.mealType
) {
    val amountText: String get() = input.text
    val replaceOnType: Boolean get() = input.replaceOnType

    /** The typed portion, in [portionUnit]. */
    val amount: Double get() = input.value

    /** The logged portion in [portionUnit]. */
    val shownOriginalAmount: Double get() = portionUnit.fromStored(amount = originalAmount)

    val unitLabel: String get() = portionUnit.label

    val isRecipe: Boolean get() = log.weight == 0.0

    /** Calories of a full recipe serving, or of the logged portion for a food. */
    val baseCalories: Int
        get() = if (isRecipe && originalAmount > 0) {
            (log.calories * FULL_RECIPE_PERCENT / originalAmount).roundToInt()
        } else {
            log.calories
        }

    private val scale: Double get() = if (shownOriginalAmount > 0) amount / shownOriginalAmount else 1.0

    val calories: Int get() = (log.calories * scale).roundToInt()
    val protein: Double get() = log.protein * scale
    val carbs: Double get() = log.carbs * scale
    val fat: Double get() = log.fat * scale

    val caloriesChange: Int get() = calories - log.calories

    /** The readout shows the logged amount to 2 decimal places, so that is what an unchanged amount looks like. */
    val hasChanges: Boolean get() = amount != shownOriginalAmount.roundToHundredths() || mealType != log.mealType

    val canSave: Boolean get() = hasChanges && amount > 0

    val step: Double
        get() = when {
            isRecipe -> RECIPE_STEP_PERCENT
            portionUnit.isConverted -> OUNCE_STEP
            originalAmount > 10 -> 10.0
            else -> 0.5
        }

    /** Quick picks: quarters of a recipe serving, or ½×, 1×, 1.5× and 2× the logged portion. */
    val presets: List<Double>
        get() = if (isRecipe) {
            listOf(25.0, 50.0, 75.0, FULL_RECIPE_PERCENT)
        } else {
            listOf(0.5, 1.0, 1.5, 2.0).map { (shownOriginalAmount * it).roundTo(step = step) }.distinct()
        }

    /** Weight to store: the new portion for a food; recipes keep 0.0, which marks them as recipes. */
    val weight: Double get() = if (isRecipe) 0.0 else portionUnit.toStored(value = amount)

    fun stepped(up: Boolean): Double = (if (up) amount + step else amount - step).roundTo(step = step)

    fun withAmount(value: Double): EntryEdit = if (value > 0) copy(input = AmountInput.of(value = value)) else this

    fun startTyping(): EntryEdit = copy(input = input.copy(replaceOnType = true))

    fun typed(key: Char): EntryEdit = copy(input = input.typed(key = key))
}

private fun Double.roundTo(step: Double): Double = (round(this / step) * step).let { if (it <= 0) step else it }
