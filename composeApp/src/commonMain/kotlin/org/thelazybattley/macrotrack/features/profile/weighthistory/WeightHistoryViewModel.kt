package org.thelazybattley.macrotrack.features.profile.weighthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase

class WeightHistoryViewModel(
    private val getAllWeightUseCase: GetAllWeightUseCase
) : ViewModel(), WeightHistoryCallbacks {

    private val _state = MutableStateFlow(value = WeightHistoryViewState())

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAllWeightUseCase().collect { weightList ->
                _state.update { currentState ->
                    currentState.copy(
                        weightList = weightList
                    )
                }
            }
        }
    }
}
