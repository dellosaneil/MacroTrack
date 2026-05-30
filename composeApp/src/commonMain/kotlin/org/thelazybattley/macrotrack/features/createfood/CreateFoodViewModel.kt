package org.thelazybattley.macrotrack.features.createfood

import androidx.lifecycle.SavedStateHandle
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
import org.thelazybattley.macrotrack.domain.usecase.food.UpdateFoodUseCase
import org.thelazybattley.macrotrack.features.createfood.ui.AddFoodTextFieldType
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.FOOD_NAME

class CreateFoodViewModel(
    private val insertFoodUseCase: InsertFoodUseCase,
    private val calculateCaloriesFromMacrosUseCase: CalculateCaloriesFromMacrosUseCase,
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val calculateMacroPercentageUseCase: CalculateMacroPercentageUseCase,
    private val updateFoodUseCase: UpdateFoodUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), CreateFoodCallbacks {

    private val _state = MutableStateFlow(value = CreateFoodViewState())
    val state = _state.asStateFlow()

    init {
        val foodName: String? = savedStateHandle[FOOD_NAME]
        viewModelScope.launch {
            getAllFoodUseCase().collect { foodList ->
                val foodDetails = foodList.find { food ->
                    food.name == foodName
                }
                _state.update { currentState ->
                    currentState.copy(
                        foodNameList = foodList.map { food -> food.name },
                        isUpdating = !foodName.isNullOrEmpty(),
                        name = foodName ?: "",
                        protein = foodDetails?.macros?.protein,
                        carbs = foodDetails?.macros?.carbs,
                        fat = foodDetails?.macros?.fat,
                        unit = foodDetails?.unit ?: "",
                        weight = foodDetails?.weight ?: 0.0,
                        calories = foodDetails?.macros?.calories ?: 0,
                    )
                }
                calculateMacroPercentage()
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
                val food = Food(
                    macros = FoodMacros(
                        calories = calories,
                        protein = protein!!,
                        carbs = carbs!!,
                        fat = fat!!
                    ),
                    name = state.value.name,
                    weight = state.value.weight,
                    dominantMacro = dominantMacro,
                    unit = state.value.unit
                )
                if (isUpdating) {
                    updateFoodUseCase(
                        food = food
                    ).also {
                        _state.update { currentState ->
                            currentState.copy(foodSaved = true)
                        }
                    }
                    return@launch
                }
                insertFoodUseCase(
                    food = food
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

                AddFoodTextFieldType.AMOUNT ->
                    currentState.copy(weight = value.toDoubleOrNull() ?: 0.0)

                AddFoodTextFieldType.FATS -> currentState.copy(
                    fat = value.toDoubleOrNull(),
                    calories = calculateCaloriesFromMacrosUseCase(
                        protein = currentState.protein ?: 0.0,
                        carbs = currentState.carbs ?: 0.0,
                        fat = value.toDoubleOrNull() ?: 0.0
                    )
                )

                AddFoodTextFieldType.PROTEIN -> currentState.copy(
                    protein = value.toDoubleOrNull(),
                    calories = calculateCaloriesFromMacrosUseCase(
                        protein = value.toDoubleOrNull() ?: 0.0,
                        carbs = currentState.carbs ?: 0.0,
                        fat = currentState.fat ?: 0.0
                    )
                )

                AddFoodTextFieldType.CARBS -> currentState.copy(
                    carbs = value.toDoubleOrNull(),
                    calories = calculateCaloriesFromMacrosUseCase(
                        protein = currentState.protein ?: 0.0,
                        carbs = value.toDoubleOrNull() ?: 0.0,
                        fat = currentState.fat ?: 0.0
                    )
                )

                AddFoodTextFieldType.UNIT -> currentState.copy(
                    unit = value
                )
            }
        }
        val isButtonEnabled = with(receiver = state.value) {
            name.isNotBlank() && weight > 0 && calories > 0 && fat != null && protein != null && carbs != null && !duplicateFood && unit.isNotBlank()
        }
        _state.update { currentState ->
            currentState.copy(buttonEnabled = isButtonEnabled)
        }
        if (type != AddFoodTextFieldType.FOOD_NAME && type != AddFoodTextFieldType.AMOUNT) {
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
