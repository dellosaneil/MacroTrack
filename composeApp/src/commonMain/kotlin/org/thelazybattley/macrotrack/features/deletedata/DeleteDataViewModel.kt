package org.thelazybattley.macrotrack.features.deletedata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.domain.usecase.deletedata.DeleteAllDataUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.measurement.GetAllMeasurementsUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.features.backup.export.ExportData

/** Each step is near-instant, so it's held on screen this long to be read as it's ticked off. */
private const val MIN_STEP_MILLIS = 400L

class DeleteDataViewModel(
    getAllFoodLogUseCase: GetAllFoodLogUseCase,
    getAllWeightUseCase: GetAllWeightUseCase,
    getAllMeasurementsUseCase: GetAllMeasurementsUseCase,
    getAllFoodUseCase: GetAllFoodUseCase,
    getAllRecipeUseCase: GetAllRecipeUseCase,
    getAllDailyGoalsUseCase: GetAllDailyGoalsUseCase,
    private val deleteAllDataUseCase: DeleteAllDataUseCase
) : ViewModel(), DeleteDataCallbacks {

    private val _state = MutableStateFlow(value = DeleteDataViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getAllFoodLogUseCase(),
                flow2 = getAllWeightUseCase(),
                flow3 = getAllMeasurementsUseCase(),
                flow4 = flow { emitAll(getAllFoodUseCase()) },
                flow5 = combine(flow = flow { emitAll(getAllRecipeUseCase()) }, flow2 = getAllDailyGoalsUseCase()) { recipes, goals ->
                    recipes to goals
                }
            ) { logs, weights, measurements, foods, (recipes, goals) ->
                DeleteDataSummary(
                    logCount = logs.size,
                    firstLogDate = logs.minOfOrNull { it.date },
                    weighInCount = weights.size,
                    measurementCount = measurements.size,
                    foodCount = foods.size,
                    recipeCount = recipes.size,
                    goalChangeCount = ExportData(goals = goals).goalChanges().size
                )
            }.collect { summary ->
                // The counts drop to zero while deleting; the form keeps showing what was there.
                if (_state.value.stage == DeleteDataStage.FORM) _state.update { it.copy(summary = summary) }
            }
        }
    }

    override fun onTyped(text: String) {
        _state.update { it.copy(typed = text) }
    }

    override fun onDelete() {
        if (!_state.value.canDelete || _state.value.stage != DeleteDataStage.FORM) return
        _state.update { it.copy(stage = DeleteDataStage.DELETING, hasFailed = false) }
        viewModelScope.launch {
            runCatching {
                deleteAllDataUseCase { step ->
                    if (_state.value.currentStep != null) delay(timeMillis = MIN_STEP_MILLIS)
                    _state.update { it.copy(currentStep = step) }
                }
                delay(timeMillis = MIN_STEP_MILLIS)
            }.onSuccess {
                _state.update { it.copy(stage = DeleteDataStage.DONE, currentStep = null) }
            }.onFailure {
                _state.update { it.copy(stage = DeleteDataStage.FORM, currentStep = null, hasFailed = true) }
            }
        }
    }
}
