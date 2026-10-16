package org.thelazybattley.macrotrack.domain.model

import kotlin.math.round

/**
 * How body weight, height and measurements are shown and typed. Everything is stored in kg and cm;
 * imperial only changes what the screens show and how typed values are read.
 */
enum class UnitSystem {
    METRIC,
    IMPERIAL;

    fun weightFromKg(kg: Double): Double = if (this == IMPERIAL) kg / KG_PER_LB else kg

    fun weightToKg(value: Double): Double = if (this == IMPERIAL) value * KG_PER_LB else value

    fun lengthFromCm(cm: Double): Double = if (this == IMPERIAL) cm / CM_PER_INCH else cm

    fun lengthToCm(value: Double): Double = if (this == IMPERIAL) value * CM_PER_INCH else value

    /** How a food amount stored in [storedUnit] is shown: imperial shows grams as oz and ml as fl oz. */
    fun portionUnit(storedUnit: String): PortionUnit = when {
        this != IMPERIAL -> PortionUnit(label = storedUnit)
        storedUnit.lowercase() == "g" -> PortionUnit(label = OUNCE, storedPerShown = GRAMS_PER_OUNCE)
        storedUnit.lowercase() == "ml" -> PortionUnit(label = FLUID_OUNCE, storedPerShown = ML_PER_FLUID_OUNCE)
        else -> PortionUnit(label = storedUnit)
    }

    companion object {
        const val KG_PER_LB = 0.45359237
        const val CM_PER_INCH = 2.54
        const val INCHES_PER_FOOT = 12
        const val GRAMS_PER_OUNCE = 28.349523125
        const val ML_PER_FLUID_OUNCE = 29.5735295625
        const val OUNCE = "oz"
        const val FLUID_OUNCE = "fl oz"

        /** The countries that still weigh people in pounds; everywhere else starts on metric. */
        private val imperialRegions = setOf("US", "LR", "MM")

        fun defaultFor(region: String): UnitSystem = if (region.uppercase() in imperialRegions) IMPERIAL else METRIC
    }
}

/**
 * A food unit as shown, with how many stored units make one shown unit. Foods and logs keep their
 * amounts in the stored unit; only what's shown and typed changes.
 */
data class PortionUnit(val label: String, val storedPerShown: Double = 1.0) {

    val isConverted: Boolean get() = storedPerShown != 1.0

    fun fromStored(amount: Double): Double = amount / storedPerShown

    fun toStored(value: Double): Double = value * storedPerShown

    /** A stored amount as shown: converted amounts to a tenth ("3.5 oz"), others exactly as stored. */
    fun format(amount: Double): String {
        val shown = fromStored(amount = amount)
        val value = if (isConverted) round(shown * 10) / 10 else shown
        return if (value == value.toInt().toDouble()) value.toInt().toString() else value.toString()
    }
}

/** A height split into whole feet and inches rounded to the nearest tenth ("5 ft 9.5 in"). */
data class FeetInches(val feet: Int, val inches: Double) {

    val cm: Double get() = (feet * UnitSystem.INCHES_PER_FOOT + inches) * UnitSystem.CM_PER_INCH

    companion object {
        fun fromCm(cm: Double): FeetInches {
            val tenthsOfInch = round(cm / UnitSystem.CM_PER_INCH * 10).toInt()
            val tenthsPerFoot = UnitSystem.INCHES_PER_FOOT * 10
            return FeetInches(feet = tenthsOfInch / tenthsPerFoot, inches = (tenthsOfInch % tenthsPerFoot) / 10.0)
        }
    }
}
