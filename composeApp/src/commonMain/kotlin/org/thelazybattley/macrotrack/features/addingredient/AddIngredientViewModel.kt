package org.thelazybattley.macrotrack.features.addingredient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.usecase.food.InsertFoodUseCase
import org.thelazybattley.macrotrack.features.addingredient.ui.AddIngredientTextFieldType

class AddIngredientViewModel(
    private val insertFoodUseCase: InsertFoodUseCase,
) : ViewModel(), AddIngredientCallbacks {

    private val _state = MutableStateFlow(value = AddIngredientViewState())
    val state = _state.asStateFlow()

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
                )
            }
        }
    }

    override fun onTextFieldUpdated(
        value: String,
        type: AddIngredientTextFieldType
    ) {
        _state.update { currentState ->
            when (type) {
                AddIngredientTextFieldType.INGREDIENT_NAME ->
                    currentState.copy(name = value)

                AddIngredientTextFieldType.AMOUNT_IN_GRAMS ->
                    currentState.copy(weight = value.toDoubleOrNull() ?: 0.0)

                AddIngredientTextFieldType.CALORIES -> currentState.copy(
                    calories = value.toDoubleOrNull() ?: 0.0
                )
                AddIngredientTextFieldType.FATS -> currentState.copy(
                    fat = value.toDoubleOrNull()
                )
                AddIngredientTextFieldType.PROTEIN -> currentState.copy(
                    protein = value.toDoubleOrNull()

                )
                AddIngredientTextFieldType.CARBS -> currentState.copy(
                    carbs = value.toDoubleOrNull()
                )
            }
        }
        val isButtonEnabled = with(receiver = state.value) {
            name.isNotBlank() && weight > 0 && calories > 0  && fat != null && protein != null && carbs != null
        }
        _state.update { currentState ->
            currentState.copy(buttonEnabled = isButtonEnabled)
        }
    }
}
