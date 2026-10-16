package org.thelazybattley.macrotrack.features.createfood

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
import org.thelazybattley.macrotrack.domain.usecase.library.GetFoodLibraryUseCase
import org.thelazybattley.macrotrack.domain.usecase.units.GetUnitSystemUseCase
import org.thelazybattley.macrotrack.features.createfood.ui.AddFoodTextFieldType
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.BARCODE
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.FOOD_NAME
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.LIBRARY_FOOD
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.PREFILL_NAME

class CreateFoodViewModel(
    private val insertFoodUseCase: InsertFoodUseCase,
    private val calculateCaloriesFromMacrosUseCase: CalculateCaloriesFromMacrosUseCase,
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val calculateMacroPercentageUseCase: CalculateMacroPercentageUseCase,
    private val updateFoodUseCase: UpdateFoodUseCase,
    private val getFoodLibraryUseCase: GetFoodLibraryUseCase,
    private val getUnitSystemUseCase: GetUnitSystemUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), CreateFoodCallbacks {

    private val _state = MutableStateFlow(value = CreateFoodViewState())
    val state = _state.asStateFlow()

    init {
        val foodName: String? = savedStateHandle[FOOD_NAME]
        val scannedBarcode: String? = savedStateHandle[BARCODE]
        val prefillName: String? = savedStateHandle[PREFILL_NAME]
        val libraryFoodName: String? = savedStateHandle[LIBRARY_FOOD]
        viewModelScope.launch {
            val unitSystem = getUnitSystemUseCase().first()
            // "Make my own version" starts a new food from a library entry's values.
            val libraryFood = libraryFoodName?.let { name ->
                getFoodLibraryUseCase().first().find { it.food.name == name }?.food
            }
            getAllFoodUseCase().collect { foodList ->
                val foodDetails = foodList.find { food ->
                    food.name == foodName
                } ?: libraryFood
                val name = foodName?.ifEmpty { null } ?: libraryFood?.name ?: prefillName?.trim() ?: ""
                // Grams and ml are shown as oz and fl oz in imperial units.
                val portionUnit = unitSystem.portionUnit(storedUnit = foodDetails?.unit.orEmpty())
                val shownServing = foodDetails?.takeIf { portionUnit.isConverted }?.let {
                    portionUnit.format(amount = it.weight).toDouble() to portionUnit.label
                }
                _state.update { currentState ->
                    currentState.copy(
                        foodNameList = foodList.map { food -> food.name },
                        isUpdating = !foodName.isNullOrEmpty(),
                        name = name,
                        duplicateFood = foodName.isNullOrEmpty() && foodList.any { it.name.equals(other = name, ignoreCase = true) },
                        protein = foodDetails?.macros?.protein?.toString(),
                        carbs = foodDetails?.macros?.carbs?.toString(),
                        fat = foodDetails?.macros?.fat?.toString(),
                        unit = shownServing?.second ?: foodDetails?.unit ?: "",
                        weight = shownServing?.first ?: foodDetails?.weight ?: 0.0,
                        unitSystem = unitSystem,
                        storedServing = foodDetails?.takeIf { shownServing != null }?.let { it.weight to it.unit },
                        shownServing = shownServing,
                        calories = foodDetails?.macros?.calories ?: 0,
                        // Editing keeps the food's barcode; a new food can arrive with the one that was just scanned.
                        barcode = foodDetails?.barcode ?: scannedBarcode
                    )
                }
                if (libraryFood != null) {
                    _state.update { currentState -> currentState.copy(buttonEnabled = currentState.canSave) }
                }
                calculateMacroPercentage()
            }
        }
    }

    override fun onSaveFood() {
        viewModelScope.launch {
            with(receiver = state.value) {
                val (weight, unit) = servingToSave
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
                        protein = protein?.toDoubleOrNull() ?: 0.0,
                        carbs = carbs?.toDoubleOrNull() ?: 0.0,
                        fat = fat?.toDoubleOrNull() ?: 0.0
                    ),
                    name = state.value.name,
                    weight = weight,
                    dominantMacro = dominantMacro,
                    unit = unit,
                    barcode = state.value.barcode
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
                    fat = value,
                    calories = calculateCaloriesFromMacrosUseCase(
                        protein = currentState.protein?.toDoubleOrNull() ?: 0.0,
                        carbs = currentState.carbs?.toDoubleOrNull() ?: 0.0,
                        fat = value.toDoubleOrNull() ?: 0.0
                    )
                )

                AddFoodTextFieldType.PROTEIN -> currentState.copy(
                    protein = value,
                    calories = calculateCaloriesFromMacrosUseCase(
                        protein = value.toDoubleOrNull() ?: 0.0,
                        carbs = currentState.carbs?.toDoubleOrNull() ?: 0.0,
                        fat = currentState.fat?.toDoubleOrNull() ?: 0.0
                    )
                )

                AddFoodTextFieldType.CARBS -> currentState.copy(
                    carbs = value,
                    calories = calculateCaloriesFromMacrosUseCase(
                        protein = currentState.protein?.toDoubleOrNull() ?: 0.0,
                        carbs = value.toDoubleOrNull() ?: 0.0,
                        fat = currentState.fat?.toDoubleOrNull() ?: 0.0
                    )
                )

                AddFoodTextFieldType.UNIT -> currentState.copy(
                    unit = value
                )
            }
        }
        _state.update { currentState ->
            currentState.copy(buttonEnabled = currentState.canSave)
        }
        if (type != AddFoodTextFieldType.FOOD_NAME && type != AddFoodTextFieldType.AMOUNT) {
            calculateMacroPercentage()
        }
    }

    private val CreateFoodViewState.canSave: Boolean
        get() = name.isNotBlank() && weight > 0 && calories > 0 && fat != null && protein != null && carbs != null &&
            !duplicateFood && unit.isNotBlank()

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
                    macroValue = updatedState.protein?.toDoubleOrNull() ?: 0.0,
                    macroType = MacroType.PROTEIN
                ),
                carbsPercentage = calculateMacroPercentageUseCase(
                    totalCalories = updatedState.calories,
                    macroValue = updatedState.carbs?.toDoubleOrNull() ?: 0.0,
                    macroType = MacroType.CARBS
                ),
                fatPercentage = calculateMacroPercentageUseCase(
                    totalCalories = updatedState.calories,
                    macroValue = updatedState.fat?.toDoubleOrNull() ?: 0.0,
                    macroType = MacroType.FAT
                ),
            )
        }
    }
}
