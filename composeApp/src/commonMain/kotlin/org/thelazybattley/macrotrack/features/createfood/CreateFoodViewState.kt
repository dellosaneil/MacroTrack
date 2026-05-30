package org.thelazybattley.macrotrack.features.createfood

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
    val isUpdating: Boolean = false
)
