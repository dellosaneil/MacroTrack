package org.thelazybattley.macrotrack.features.addingredient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.usecase.CalculateCaloriesFromMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.InsertFoodUseCase
import org.thelazybattley.macrotrack.features.addingredient.ui.AddIngredientTextFieldType

class AddIngredientViewModel(
    private val insertFoodUseCase: InsertFoodUseCase,
    private val calculateCaloriesFromMacrosUseCase: CalculateCaloriesFromMacrosUseCase,
    private val getAllFoodUseCase: GetAllFoodUseCase
) : ViewModel(), AddIngredientCallbacks {

    private val _state = MutableStateFlow(value = AddIngredientViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAllFoodUseCase().collect { foodList ->
                _state.update { currentState ->
                    currentState.copy(
                        foodNameList = foodList.map { food -> food.name }
                    )
                }
            }
        }
    }


    override fun onSaveIngredient() {
        viewModelScope.launch {
            with(receiver = state.value) {
                insertFoodUseCase(
                    food = Food(
                        macros = FoodMacros(
                            calories = calories,
                            protein = protein!!,
                            carbs = carbs!!,
                            fat = fat!!
                        ),
                        name = state.value.name,
                        weight = state.value.weight
                    )
                ).also {
                    _state.update { currentState ->
                        currentState.copy(ingredientSaved = true)
                    }
                }
            }
        }
    }

    override fun onTextFieldUpdated(
        value: String,
        type: AddIngredientTextFieldType
    ) {
        _state.update { currentState ->
            when (type) {
                AddIngredientTextFieldType.INGREDIENT_NAME -> {
                    val duplicateFood = currentState.foodNameList.any { food ->
                        food.equals(
                            other = value,
                            ignoreCase = true
                        )
                    }
                    currentState.copy(name = value, duplicateFood = duplicateFood)
                }

                AddIngredientTextFieldType.AMOUNT_IN_GRAMS ->
                    currentState.copy(weight = value.toDoubleOrNull() ?: 0.0)

                AddIngredientTextFieldType.FATS -> currentState.copy(
                    fat = value.toDoubleOrNull() ?: 0.0,
                    calories = calculateCaloriesFromMacrosUseCase(
                        protein = currentState.protein ?: 0.0,
                        carbs = currentState.carbs ?: 0.0,
                        fat = value.toDoubleOrNull() ?: 0.0
                    )
                )

                AddIngredientTextFieldType.PROTEIN -> currentState.copy(
                    protein = value.toDoubleOrNull() ?: 0.0,
                    calories = calculateCaloriesFromMacrosUseCase(
                        protein = value.toDoubleOrNull() ?: 0.0,
                        carbs = currentState.carbs ?: 0.0,
                        fat = currentState.fat ?: 0.0
                    )
                )

                AddIngredientTextFieldType.CARBS -> currentState.copy(
                    carbs = value.toDoubleOrNull() ?: 0.0,
                    calories = calculateCaloriesFromMacrosUseCase(
                        protein = currentState.protein ?: 0.0,
                        carbs = value.toDoubleOrNull() ?: 0.0,
                        fat = currentState.fat ?: 0.0
                    )
                )
            }
        }
        val isButtonEnabled = with(receiver = state.value) {
            name.isNotBlank() && weight > 0 && calories > 0 && fat != null && protein != null && carbs != null && !duplicateFood
        }
        _state.update { currentState ->
            currentState.copy(buttonEnabled = isButtonEnabled)
        }
        if (type != AddIngredientTextFieldType.INGREDIENT_NAME && type != AddIngredientTextFieldType.AMOUNT_IN_GRAMS) {
            calculateMacroPercentage()
        }
    }

    private fun calculateMacroPercentage() {
        _state.value.let { currentState ->
            if (currentState.calories == 0) {
                _state.update {
                    it.copy(
                        proteinPercentage = 0.0,
                        carbsPercentage = 0.0,
                        fatPercentage = 0.0
                    )
                }
                return
            }

            val calFromProtein = (currentState.protein ?: 0.0) * 4
            val calFromCarbs = (currentState.carbs ?: 0.0) * 4
            val calFromFat = (currentState.fat ?: 0.0) * 9
            val proteinPercent = calFromProtein / currentState.calories
            val fatPercent = calFromFat / currentState.calories
            val carbsPercent = calFromCarbs / currentState.calories
            _state.update {
                it.copy(
                    proteinPercentage = proteinPercent,
                    carbsPercentage = carbsPercent,
                    fatPercentage = fatPercent
                )
            }
        }
    }
}
