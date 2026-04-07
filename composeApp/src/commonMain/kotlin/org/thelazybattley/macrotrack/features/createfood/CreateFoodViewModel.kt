package org.thelazybattley.macrotrack.features.createfood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.usecase.CalculateCaloriesFromMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacroPercentageUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.InsertFoodUseCase
import org.thelazybattley.macrotrack.features.createfood.ui.AddFoodTextFieldType

class CreateFoodViewModel(
    private val insertFoodUseCase: InsertFoodUseCase,
    private val calculateCaloriesFromMacrosUseCase: CalculateCaloriesFromMacrosUseCase,
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val calculateMacroPercentageUseCase: CalculateMacroPercentageUseCase
) : ViewModel(), CreateFoodCallbacks {

    private val _state = MutableStateFlow(value = CreateFoodViewState())
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


    override fun onSaveFood() {
        viewModelScope.launch {
            with(receiver = state.value) {
                val dominantMacro = listOf(
                    MacroType.PROTEIN to proteinPercentage,
                    MacroType.CARBS to carbsPercentage,
                    MacroType.FAT to fatPercentage
                ).maxByOrNull {
                    it.second
                }?.first ?: MacroType.PROTEIN

                insertFoodUseCase(
                    food = Food(
                        macros = FoodMacros(
                            calories = calories,
                            protein = protein!!,
                            carbs = carbs!!,
                            fat = fat!!
                        ),
                        name = state.value.name,
                        weight = state.value.weight,
                        dominantMacro = dominantMacro
                    )
                ).also {
                    _state.update { currentState ->
                        currentState.copy(foodSaved = true)
                    }
                }
            }
        }
    }

    override fun onTextFieldUpdated(
        value: String,
        type: AddFoodTextFieldType
    ) {
        _state.update { currentState ->
            when (type) {
                AddFoodTextFieldType.FOOD_NAME -> {
                    val duplicateFood = currentState.foodNameList.any { food ->
                        food.equals(
                            other = value,
                            ignoreCase = true
                        )
                    }
                    currentState.copy(name = value, duplicateFood = duplicateFood)
                }

                AddFoodTextFieldType.AMOUNT_IN_GRAMS ->
                    currentState.copy(weight = value.toDoubleOrNull() ?: 0.0)

                AddFoodTextFieldType.FATS -> currentState.copy(
                    fat = value.toDoubleOrNull() ?: 0.0,
                    calories = calculateCaloriesFromMacrosUseCase(
                        protein = currentState.protein ?: 0.0,
                        carbs = currentState.carbs ?: 0.0,
                        fat = value.toDoubleOrNull() ?: 0.0
                    )
                )

                AddFoodTextFieldType.PROTEIN -> currentState.copy(
                    protein = value.toDoubleOrNull() ?: 0.0,
                    calories = calculateCaloriesFromMacrosUseCase(
                        protein = value.toDoubleOrNull() ?: 0.0,
                        carbs = currentState.carbs ?: 0.0,
                        fat = currentState.fat ?: 0.0
                    )
                )

                AddFoodTextFieldType.CARBS -> currentState.copy(
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
        if (type != AddFoodTextFieldType.FOOD_NAME && type != AddFoodTextFieldType.AMOUNT_IN_GRAMS) {
            calculateMacroPercentage()
        }
    }

    private fun calculateMacroPercentage() {
        _state.update { updatedState ->
            if (updatedState.calories == 0) {
                return@update updatedState.copy(
                    proteinPercentage = 0.0,
                    carbsPercentage = 0.0,
                    fatPercentage = 0.0
                )
            }
            updatedState.copy(
                proteinPercentage = calculateMacroPercentageUseCase(
                    totalCalories = updatedState.calories,
                    macroValue = updatedState.protein ?: 0.0,
                    macroType = MacroType.PROTEIN
                ),
                carbsPercentage = calculateMacroPercentageUseCase(
                    totalCalories = updatedState.calories,
                    macroValue = updatedState.carbs ?: 0.0,
                    macroType = MacroType.CARBS
                ),
                fatPercentage = calculateMacroPercentageUseCase(
                    totalCalories = updatedState.calories,
                    macroValue = updatedState.fat ?: 0.0,
                    macroType = MacroType.FAT
                ),
            )
        }
    }
}
