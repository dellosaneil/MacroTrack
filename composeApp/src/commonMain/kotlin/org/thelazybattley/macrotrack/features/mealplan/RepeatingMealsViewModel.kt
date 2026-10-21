package org.thelazybattley.macrotrack.features.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.RepeatingMeal
import org.thelazybattley.macrotrack.domain.usecase.mealplan.GetRepeatingMealsUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.SetRepeatingMealActiveUseCase

data class RepeatingMealsViewState(
    val today: LocalDate,
    val repeatingMeals: List<RepeatingMeal> = emptyList()
)

interface RepeatingMealsCallbacks {
    fun onActiveToggled(repeatingMeal: RepeatingMeal)

    companion object {
        fun default() = object : RepeatingMealsCallbacks {
            override fun onActiveToggled(repeatingMeal: RepeatingMeal) = Unit
        }
    }
}

class RepeatingMealsViewModel(
    getRepeatingMealsUseCase: GetRepeatingMealsUseCase,
    private val setRepeatingMealActiveUseCase: SetRepeatingMealActiveUseCase
) : ViewModel(), RepeatingMealsCallbacks {

    private val _state = MutableStateFlow(value = RepeatingMealsViewState(today = getCurrentDate()))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getRepeatingMealsUseCase().collect { meals ->
                _state.update { it.copy(repeatingMeals = meals.sortedBy { meal -> meal.mealType.ordinal }) }
            }
        }
    }

    override fun onActiveToggled(repeatingMeal: RepeatingMeal) {
        viewModelScope.launch {
            setRepeatingMealActiveUseCase(id = repeatingMeal.id, isActive = !repeatingMeal.isActive)
        }
    }
}
