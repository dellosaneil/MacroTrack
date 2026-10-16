package org.thelazybattley.macrotrack.features.createfood

import org.thelazybattley.macrotrack.domain.model.UnitSystem

data class CreateFoodViewState(
    val protein: String? = null,
    val carbs: String? = null,
    val fat: String? = null,
    val calories: Int = 0,
    val weight: Double = 0.0,
    val name: String = "",
    val buttonEnabled: Boolean = false,
    val proteinPercentage: Double = 0.0,
    val carbsPercentage: Double = 0.0,
    val fatPercentage: Double = 0.0,
    val foodNameList: List<String> = emptyList(),
    val duplicateFood: Boolean = false,
    val foodSaved: Boolean = false,
    val unit: String = "",
    val isUpdating: Boolean = false,
    val barcode: String? = null,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    /**
     * The serving as stored, when it's shown converted (a food in grams shown in oz). Saving without touching
     * the amount or unit keeps it exactly, so rounding to a tenth of an ounce never changes the food.
     */
    val storedServing: Pair<Double, String>? = null,
    /** What [storedServing] looked like when shown. */
    val shownServing: Pair<Double, String>? = null
) {
    /** The serving to save: imperial oz and fl oz go back to grams and ml, which is how foods are kept. */
    val servingToSave: Pair<Double, String>
        get() {
            if (storedServing != null && shownServing == (weight to unit)) return storedServing
            if (unitSystem != UnitSystem.IMPERIAL) return weight to unit
            return when (unit.trim().lowercase()) {
                UnitSystem.OUNCE -> weight * UnitSystem.GRAMS_PER_OUNCE to "g"
                UnitSystem.FLUID_OUNCE -> weight * UnitSystem.ML_PER_FLUID_OUNCE to "ml"
                else -> weight to unit
            }
        }
}
