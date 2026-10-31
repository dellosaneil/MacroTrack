package org.thelazybattley.macrotrack.features.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.RepeatingMeal
import org.thelazybattley.macrotrack.domain.usecase.mealplan.DeleteRepeatingMealUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.GetRepeatingMealsUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.RestoreRepeatingMealUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.SetRepeatingMealActiveUseCase

/**
 * [menuMealId] is the one whose ⋮ menu is open, on its delete confirm when [isConfirmingDelete]. [deleted] is the one
 * just deleted, kept for Undo while its toast shows.
 */
data class RepeatingMealsViewState(
    val today: LocalDate,
    val repeatingMeals: List<RepeatingMeal> = emptyList(),
    val isLoaded: Boolean = false,
    val menuMealId: Long? = null,
    val isConfirmingDelete: Boolean = false,
    val deleted: RepeatingMeal? = null
) {
    val menuMeal: RepeatingMeal? get() = repeatingMeals.find { it.id == menuMealId }
}

interface RepeatingMealsCallbacks {
    fun onActiveToggled(repeatingMeal: RepeatingMeal)
    fun onMenuOpened(repeatingMeal: RepeatingMeal)
    fun onMenuDismissed()
    fun onDeleteAsked()

    /** Deletes [id], from the menu's confirm or from Edit repeating meal, with Undo. */
    fun onDelete(id: Long)
    fun onUndoDelete()
    fun onDeletedToastDismissed()

    companion object {
        fun default() = object : RepeatingMealsCallbacks {
            override fun onActiveToggled(repeatingMeal: RepeatingMeal) = Unit
            override fun onMenuOpened(repeatingMeal: RepeatingMeal) = Unit
            override fun onMenuDismissed() = Unit
            override fun onDeleteAsked() = Unit
            override fun onDelete(id: Long) = Unit
            override fun onUndoDelete() = Unit
            override fun onDeletedToastDismissed() = Unit
        }
    }
}

class RepeatingMealsViewModel(
    private val getRepeatingMealsUseCase: GetRepeatingMealsUseCase,
    private val setRepeatingMealActiveUseCase: SetRepeatingMealActiveUseCase,
    private val deleteRepeatingMealUseCase: DeleteRepeatingMealUseCase,
    private val restoreRepeatingMealUseCase: RestoreRepeatingMealUseCase
) : ViewModel(), RepeatingMealsCallbacks {

    private val _state = MutableStateFlow(value = RepeatingMealsViewState(today = getCurrentDate()))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getRepeatingMealsUseCase().collect { meals ->
                _state.update { it.copy(repeatingMeals = meals.sortedBy { meal -> meal.mealType.ordinal }, isLoaded = true) }
            }
        }
    }

    override fun onActiveToggled(repeatingMeal: RepeatingMeal) {
        _state.update { it.copy(menuMealId = null, isConfirmingDelete = false) }
        viewModelScope.launch {
            setRepeatingMealActiveUseCase(id = repeatingMeal.id, isActive = !repeatingMeal.isActive)
        }
    }

    override fun onMenuOpened(repeatingMeal: RepeatingMeal) {
        _state.update { it.copy(menuMealId = repeatingMeal.id, isConfirmingDelete = false) }
    }

    override fun onMenuDismissed() {
        _state.update { it.copy(menuMealId = null, isConfirmingDelete = false) }
    }

    override fun onDeleteAsked() {
        _state.update { it.copy(isConfirmingDelete = true) }
    }

    override fun onDelete(id: Long) {
        _state.update { it.copy(menuMealId = null, isConfirmingDelete = false) }
        viewModelScope.launch {
            val meal = getRepeatingMealsUseCase().first().find { it.id == id } ?: return@launch
            deleteRepeatingMealUseCase(id = id)
            _state.update { it.copy(deleted = meal) }
        }
    }

    override fun onUndoDelete() {
        val deleted = _state.value.deleted ?: return
        _state.update { it.copy(deleted = null) }
        viewModelScope.launch { restoreRepeatingMealUseCase(repeatingMeal = deleted) }
    }

    override fun onDeletedToastDismissed() {
        _state.update { it.copy(deleted = null) }
    }
}
