package org.thelazybattley.macrotrack.features.addingredient

data class AddIngredientViewState(
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val calories: Int = 0,
    val weight: Double = 0.0,
    val name: String = "",
    val buttonEnabled: Boolean = false,
    val proteinPercentage: Double = 0.0,
    val carbsPercentage: Double = 0.0,
    val fatPercentage: Double = 0.0,
    val foodNameList: List<String> = emptyList(),
    val duplicateFood: Boolean = false,
    val ingredientSaved: Boolean = false
)
