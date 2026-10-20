package org.thelazybattley.macrotrack.features.quickadd

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetRemainingCaloriesUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.InsertFoodLogUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.LOG_DATE
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.MEAL_TYPE
import org.thelazybattley.macrotrack.ui.navigation.logDateOf

class QuickAddViewModel(
    getRemainingCaloriesUseCase: GetRemainingCaloriesUseCase,
    private val insertFoodLogUseCase: InsertFoodLogUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), QuickAddCallbacks {

    private val _state = MutableStateFlow(
        value = QuickAddViewState(
            mealType = MealType.entries.find { it.name == savedStateHandle[MEAL_TYPE] } ?: MealType.BREAKFAST,
            logDate = logDateOf(argument = savedStateHandle[LOG_DATE])
        )
    )
    val state = _state.asStateFlow()

    private var isSaving = false

    init {
        viewModelScope.launch {
            getRemainingCaloriesUseCase(date = _state.value.logDate).collect { remaining ->
                // While saving, the new entry is already counted; keep showing the figure from before it.
                _state.update { if (isSaving) it else it.copy(remainingCalories = remaining) }
            }
        }
    }

    override fun onModeSelected(mode: QuickAddMode) {
        _state.update { it.withMode(mode = mode) }
    }

    override fun onFieldSelected(field: QuickAddField) {
        _state.update { it.withActiveField(field = field) }
    }

    override fun onKey(key: Char) {
        _state.update { it.typed(key = key) }
    }

    override fun onLabelChanged(label: String) {
        if (label.length > MAX_LABEL_LENGTH) return
        _state.update { it.copy(label = label) }
    }

    override fun onMealSelected(mealType: MealType) {
        _state.update { it.copy(mealType = mealType) }
    }

    override fun onAdd(defaultName: String) {
        val currentState = _state.value
        if (!currentState.canAdd || isSaving) return
        // The screen closes once saved, so the log is written before isSaved is set.
        isSaving = true
        val isMacros = currentState.mode == QuickAddMode.MACROS
        viewModelScope.launch {
            insertFoodLogUseCase(
                foodName = currentState.label.trim().ifEmpty { defaultName },
                mealType = currentState.mealType,
                calories = currentState.totalCalories,
                protein = if (isMacros) currentState.gramsOf(field = QuickAddField.PROTEIN) else 0.0,
                carbs = if (isMacros) currentState.gramsOf(field = QuickAddField.CARBS) else 0.0,
                fat = if (isMacros) currentState.gramsOf(field = QuickAddField.FAT) else 0.0,
                weight = 0.0,
                dominantMacro = currentState.dominantMacro,
                date = currentState.logDate,
                isQuickAdd = true
            )
            _state.update { it.copy(isSaved = true) }
        }
    }

    private companion object {
        const val MAX_LABEL_LENGTH = 60
    }
}
