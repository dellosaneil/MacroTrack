package org.thelazybattley.macrotrack.features.createrecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase

class CreateRecipeViewModel(
    private val getAllRecipeUseCase: GetAllRecipeUseCase,
    private val getAllFoodUseCase: GetAllFoodUseCase
) : ViewModel(), CreateRecipeCallbacks {

    private val _state = MutableStateFlow(value = CreateRecipeViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAllRecipeUseCase().collect { recipes ->
                _state.update { currentState ->
                    currentState.copy(
                        savedRecipesName = recipes.map { it.name }
                    )
                }
            }
        }
        viewModelScope.launch {
            getAllFoodUseCase().collect { foods ->
                _state.update { currentState ->
                    currentState.copy(
                        ingredients = foods
                    )
                }
            }
        }
    }
}
