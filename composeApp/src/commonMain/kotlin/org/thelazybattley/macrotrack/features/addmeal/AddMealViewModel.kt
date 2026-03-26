package org.thelazybattley.macrotrack.features.addmeal

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.ui.FoodFilter

class AddMealViewModel: ViewModel(), AddMealCallbacks {

    private val _state = MutableStateFlow(value = AddMealViewState())
    val state = _state.asStateFlow()

    override fun onMealTypeSelected(mealType: MealType) {
        _state.update { currentState ->
            currentState.copy(
                selectedMealType = mealType
            )
        }
    }

    override fun onFoodFilterSelected(foodFilter: FoodFilter) {
        _state.update { currentState ->
            currentState.copy(
                selectedFoodFilter = foodFilter
            )
        }
    }

}
