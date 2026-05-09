package org.thelazybattley.macrotrack.features.profile.weighthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryTimeRangeEnum

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
                        completeWeightList = weightList,
                        averageWeight = weightList.map { it.weight }.average(),
                        filteredWeightList = weightList
                    )
                }
            }
        }
    }

    override fun onTimePeriodSelect(timeRange: WeightHistoryTimeRangeEnum) {
        _state.update { currentState ->
            when (timeRange) {
                WeightHistoryTimeRangeEnum.ALL -> {
                    currentState.copy(
                        selectedIndex = 0,
                        timeRange = timeRange,
                        filteredWeightList = currentState.completeWeightList
                    )
                }

                WeightHistoryTimeRangeEnum.WEEK -> {
                    currentState.copy(
                        selectedIndex = 0,
                        timeRange = timeRange,
                    )
                }

                WeightHistoryTimeRangeEnum.MONTH -> {
                    currentState.copy(
                        selectedIndex = 0,
                        timeRange = timeRange,
                    )
                }

                WeightHistoryTimeRangeEnum.THREE_MONTHS -> {
                    currentState.copy(
                        selectedIndex = 0,
                        timeRange = timeRange,
                    )
                }
            }
        }
    }
}
