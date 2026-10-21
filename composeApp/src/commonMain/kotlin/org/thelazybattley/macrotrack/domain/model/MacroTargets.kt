package org.thelazybattley.macrotrack.domain.model

import org.thelazybattley.macrotrack.domain.MacroGoals
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/** The weight protein and fat targets are worked out from. */
enum class MacroBasis {
    /** The target weight, or the weight at a BMI of 25 when there's no target. */
    TARGET_WEIGHT,

    /** Today's weight without body fat; needs a body fat percentage. */
    LEAN_MASS
}

object MacroTargets {
    const val PROTEIN_GRAMS_PER_KG = 2.0
    const val FAT_GRAMS_PER_KG = 0.7

    /** Under this many grams of carbs a calculated plan gets the "Very low carbs" warning. */
    const val LOW_CARB_GRAMS = 50

    /** Custom macros further than this from the calorie goal get the "don't add up" check. */
    const val MATCH_TOLERANCE_KCAL = 25

    const val MIN_CUSTOM_CALORIES = 800.0
    const val MAX_CUSTOM_CALORIES = 6000.0
    const val MIN_BODY_FAT = 3.0
    const val MAX_BODY_FAT = 70.0

    private const val HEALTHY_BMI = 25.0

    /**
     * The weight in kg that protein and fat are planned from. It never goes above today's [weight], so a
     * heavy user's protein and fat don't take up the calories carbs need.
     */
    fun basisWeight(
        weight: Double,
        height: Double,
        targetWeight: Double?,
        basis: MacroBasis,
        bodyFat: Double?
    ): Double = if (basis == MacroBasis.LEAN_MASS && bodyFat != null) {
        leanMass(weight = weight, bodyFat = bodyFat)
    } else {
        min(weight, targetWeight ?: healthyWeight(height = height))
    }

    fun leanMass(weight: Double, bodyFat: Double): Double = weight * (1 - bodyFat / 100)

    /** The weight at a BMI of 25 for [height] in cm. */
    fun healthyWeight(height: Double): Double = HEALTHY_BMI * (height / 100).pow(2)

    fun proteinGrams(basisWeight: Double): Int = (basisWeight * PROTEIN_GRAMS_PER_KG).roundToInt()

    fun fatGrams(basisWeight: Double): Int = (basisWeight * FAT_GRAMS_PER_KG).roundToInt()

    /**
     * The fastest weight-loss pace, in kg a month to a tenth, that still leaves [LOW_CARB_GRAMS] of carbs after
     * [protein] and [fat] from a [maintenance] burn. Null when even no deficit leaves that much.
     */
    fun fastestPaceKeepingCarbs(maintenance: Int, protein: Int, fat: Int): Double? {
        val deficit = maintenance - protein * MacroType.PROTEIN.calories - fat * MacroType.FAT.calories -
            LOW_CARB_GRAMS * MacroType.CARBS.calories
        val pace = floor(deficit / KCAL_PER_KG_MONTH * 10) / 10
        return pace.takeIf { it > 0 }
    }

    /** Mifflin-St Jeor, the burn at rest before activity. */
    fun restingBurn(height: Double, weight: Double, age: Int, sex: UserSex): Int =
        ((10 * weight) + (6.25 * height) - (5 * age) + (if (sex == UserSex.MALE) 5 else -161)).roundToInt()

    private const val KCAL_PER_KG_MONTH = 7700.0 / 30.4
}

/** Carbs that take the calories left after protein and fat, never below 0 g. */
fun carbsFor(calories: Int, protein: Int, fat: Int): Int =
    max(0, (calories - protein * MacroType.PROTEIN.calories - fat * MacroType.FAT.calories) / MacroType.CARBS.calories)

/** The calories the macros add up to. */
val MacroGoals.macroCalories: Int
    get() = protein * MacroType.PROTEIN.calories + carbs * MacroType.CARBS.calories + fat * MacroType.FAT.calories

/** A split of calories between protein, carbs and fat, in percent. */
enum class MacroSplit(val protein: Int, val carbs: Int, val fat: Int) {
    BALANCED(protein = 30, carbs = 40, fat = 30),
    HIGH_PROTEIN(protein = 40, carbs = 35, fat = 25),
    KETO(protein = 25, carbs = 5, fat = 70),
    LOW_FAT(protein = 30, carbs = 55, fat = 15);

    fun goalsFor(calories: Int) = MacroGoals(
        calories = calories,
        protein = (calories * protein / 100.0 / MacroType.PROTEIN.calories).roundToInt(),
        carbs = (calories * carbs / 100.0 / MacroType.CARBS.calories).roundToInt(),
        fat = (calories * fat / 100.0 / MacroType.FAT.calories).roundToInt()
    )

    /** Whether [goals] are this split, give or take a percent each. */
    fun matches(goals: MacroGoals): Boolean {
        if (goals.calories <= 0) return false
        fun pct(grams: Int, type: MacroType) = (grams * type.calories * 100.0 / goals.calories).roundToInt()
        return kotlin.math.abs(pct(goals.protein, MacroType.PROTEIN) - protein) <= 1 &&
            kotlin.math.abs(pct(goals.carbs, MacroType.CARBS) - carbs) <= 1 &&
            kotlin.math.abs(pct(goals.fat, MacroType.FAT) - fat) <= 1
    }
}
