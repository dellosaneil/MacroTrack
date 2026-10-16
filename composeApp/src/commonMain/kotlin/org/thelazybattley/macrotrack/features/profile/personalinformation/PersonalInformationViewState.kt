package org.thelazybattley.macrotrack.features.profile.personalinformation

import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.FeetInches
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.domain.model.Weight
import kotlin.math.abs
import kotlin.math.round

data class PersonalInformationViewState(
    val userDetails: UserDetails? = null,
    val latestWeight: Weight? = null,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val age: String = "",
    /** Height in cm, typed while [unitSystem] is metric. */
    val height: String = "",
    /** Height in feet and inches, typed while [unitSystem] is imperial. */
    val heightFeet: String = "",
    val heightInches: String = "",
    val sex: UserSex? = null,
    val activityLevel: ActivityLevel? = null,
    val dailyCalories: Int? = null,
    val isSaved: Boolean = false
) {
    val parsedAge: Int? get() = age.toIntOrNull()?.takeIf { it in MIN_AGE..MAX_AGE }

    /** The typed height in cm, whichever unit it was typed in. */
    val parsedHeight: Double?
        get() = when (unitSystem) {
            UnitSystem.METRIC -> height.toDoubleOrNull()
            UnitSystem.IMPERIAL -> {
                val feet = heightFeet.toIntOrNull()
                val inches = heightInches.ifEmpty { "0" }.toDoubleOrNull()?.takeIf { it < UnitSystem.INCHES_PER_FOOT }
                if (feet == null || inches == null) null else FeetInches(feet = feet, inches = inches).cm
            }
        }?.takeIf { it in MIN_HEIGHT..MAX_HEIGHT }

    val currentWeight: Double? get() = latestWeight?.weight ?: userDetails?.weight

    /**
     * Whether the typed height differs from the saved one. Feet and inches are rounded to a tenth of an
     * inch, so a height that only moved by that rounding still counts as unchanged.
     */
    val isHeightChanged: Boolean
        get() {
            val saved = userDetails?.height ?: return false
            val typed = parsedHeight ?: return true
            return abs(typed - saved) > HEIGHT_ROUNDING_CM
        }

    val hasChanges: Boolean
        get() = userDetails != null && (
            parsedAge != userDetails.age ||
                isHeightChanged ||
                sex != userDetails.sex ||
                activityLevel != userDetails.activityLevel
            )

    val canSave: Boolean get() = parsedAge != null && parsedHeight != null && hasChanges

    /** Fills the height fields for [unitSystem] from a height in cm. */
    fun withHeightFields(cm: Double): PersonalInformationViewState {
        val feetInches = FeetInches.fromCm(cm = cm)
        return copy(
            height = (round(cm * 10) / 10).toCleanString(),
            heightFeet = feetInches.feet.toString(),
            heightInches = feetInches.inches.toCleanString()
        )
    }

    companion object {
        const val MIN_AGE = 10
        const val MAX_AGE = 120
        const val MIN_HEIGHT = 50.0
        const val MAX_HEIGHT = 272.0

        /** Half of a tenth of an inch, the most a height can move when shown in feet and inches. */
        private const val HEIGHT_ROUNDING_CM = UnitSystem.CM_PER_INCH / 20
    }
}
