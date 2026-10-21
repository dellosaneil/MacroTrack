package org.thelazybattley.macrotrack.features.copy

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.CopyFoodLogsUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.library.GetFoodLibraryUseCase
import org.thelazybattley.macrotrack.features.foodlog.FoodLogCopyEvents
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.MEAL_TYPE
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.SOURCE_DATE
import org.thelazybattley.macrotrack.ui.navigation.logDateOf

interface CopyToCallbacks {
    fun onLogToggled(id: Long)

    /** Picks or clears every log in [mealType], for a whole day. */
    fun onMealToggled(mealType: MealType)
    fun onAllToggled()
    fun onDateToggled(date: LocalDate)

    /** A day picked from the calendar, added to the ones picked. */
    fun onDatePicked(date: LocalDate)
    fun onTargetMealSelected(mealType: MealType)
    fun onReplaceSelected(isReplace: Boolean)
    fun onCopy()

    companion object {
        fun default() = object : CopyToCallbacks {
            override fun onLogToggled(id: Long) = Unit
            override fun onMealToggled(mealType: MealType) = Unit
            override fun onAllToggled() = Unit
            override fun onDateToggled(date: LocalDate) = Unit
            override fun onDatePicked(date: LocalDate) = Unit
            override fun onTargetMealSelected(mealType: MealType) = Unit
            override fun onReplaceSelected(isReplace: Boolean) = Unit
            override fun onCopy() = Unit
        }
    }
}

class CopyToViewModel(
    getAllFoodLogUseCase: GetAllFoodLogUseCase,
    getAllFoodUseCase: GetAllFoodUseCase,
    getFoodLibraryUseCase: GetFoodLibraryUseCase,
    private val copyFoodLogsUseCase: CopyFoodLogsUseCase,
    private val copyEvents: FoodLogCopyEvents,
    savedStateHandle: SavedStateHandle
) : ViewModel(), CopyToCallbacks {

    private val _state = MutableStateFlow(
        value = run {
            val today = getCurrentDate()
            val sourceDate = logDateOf(argument = savedStateHandle[SOURCE_DATE])
            CopyToViewState(
                sourceDate = sourceDate,
                mealType = MealType.entries.find { it.name == savedStateHandle[MEAL_TYPE] },
                today = today,
                // A past day most often goes to today; today itself has no default.
                targetDates = if (sourceDate < today) setOf(today) else emptySet()
            )
        }
    )
    val state = _state.asStateFlow()

    private var isCopying = false

    init {
        viewModelScope.launch {
            combine(
                flow = getAllFoodLogUseCase(),
                flow2 = getAllFoodUseCase(),
                flow3 = getFoodLibraryUseCase()
            ) { logs, foods, libraryFoods ->
                Triple(logs, foods, libraryFoods)
            }.collect { (logs, foods, libraryFoods) ->
                _state.update { currentState ->
                    currentState.copy(
                        allLogs = logs,
                        // While copying, the source keeps the logs it had when the copy started.
                        sourceLogs = if (isCopying) {
                            currentState.sourceLogs
                        } else {
                            logs.filter {
                                it.date == currentState.sourceDate &&
                                    (currentState.mealType == null || it.mealType == currentState.mealType)
                            }.sortedBy { it.mealType.ordinal }
                        },
                        foodUnit = libraryFoods.associate { it.food.name to it.food.unit } + foods.associate { it.name to it.unit }
                    )
                }
            }
        }
    }

    override fun onLogToggled(id: Long) {
        _state.update { it.copy(unchecked = it.unchecked.toggled(element = id)) }
    }

    override fun onMealToggled(mealType: MealType) {
        _state.update { currentState ->
            val ids = currentState.sourceLogs.filter { it.mealType == mealType }.map { it.id }
            val isAllPicked = ids.none { it in currentState.unchecked }
            currentState.copy(unchecked = if (isAllPicked) currentState.unchecked + ids else currentState.unchecked - ids.toSet())
        }
    }

    override fun onAllToggled() {
        _state.update { currentState ->
            currentState.copy(
                unchecked = if (currentState.unchecked.isEmpty()) currentState.sourceLogs.map { it.id }.toSet() else emptySet()
            )
        }
    }

    override fun onDateToggled(date: LocalDate) {
        _state.update { it.copy(targetDates = it.targetDates.toggled(element = date)) }
    }

    override fun onDatePicked(date: LocalDate) {
        _state.update { currentState ->
            if (date > currentState.today || date == currentState.sourceDate) return@update currentState
            currentState.copy(targetDates = currentState.targetDates + date)
        }
    }

    override fun onTargetMealSelected(mealType: MealType) {
        _state.update { it.copy(targetMeal = mealType) }
    }

    override fun onReplaceSelected(isReplace: Boolean) {
        _state.update { it.copy(isReplace = isReplace) }
    }

    override fun onCopy() {
        val currentState = _state.value
        if (!currentState.canCopy || isCopying) return
        isCopying = true
        viewModelScope.launch {
            val copy = copyFoodLogsUseCase(
                logs = currentState.selectedLogs,
                targetDates = currentState.targetDates.sortedDescending(),
                mealType = if (currentState.isMeal) currentState.targetMeal else null,
                replace = currentState.hasConflict && currentState.isReplace
            )
            copyEvents.post(copy = copy)
            _state.update { it.copy(isDone = true) }
        }
    }
}

internal fun <T> Set<T>.toggled(element: T): Set<T> = if (element in this) this - element else this + element
