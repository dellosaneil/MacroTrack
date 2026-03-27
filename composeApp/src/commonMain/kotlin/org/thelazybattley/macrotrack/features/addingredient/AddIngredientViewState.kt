package org.thelazybattley.macrotrack.features.addingredient

data class AddIngredientViewState(
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val calories: Double = 0.0,
    val weight: Double = 0.0,
    val name: String = "",
    val buttonEnabled: Boolean = false
)
