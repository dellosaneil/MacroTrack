package org.thelazybattley.macrotrack.features.profile.weighthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.core.getCurrentDate
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
                val monthlyWeightList = weightList.groupBy { weight -> weight.date.month }
                _state.update { currentState ->
                    currentState.copy(
                        completeWeightList = weightList,
                        averageWeight = weightList.map { it.weight }.average(),
                        filteredWeightList = weightList,
                        monthlyWeightHistory = MonthlyWeightHistory(
                            monthSelected = getCurrentDate().month,
                            weightsByMonth = monthlyWeightList,
                        )
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
                        timeRange = timeRange,
                        filteredWeightList = currentState.completeWeightList
                    )
                }

                WeightHistoryTimeRangeEnum.WEEK -> {
                    currentState.copy(
                        timeRange = timeRange,
                    )
                }

                WeightHistoryTimeRangeEnum.MONTH -> {
                    currentState.copy(
                        timeRange = timeRange,
                        filteredWeightList = currentState.monthlyWeightHistory.weightsByMonth[getCurrentDate().month]
                            ?: emptyList(),
                        filteredValue = getCurrentDate().month.name
                    )
                }

                WeightHistoryTimeRangeEnum.THREE_MONTHS -> {
                    currentState.copy(
                        timeRange = timeRange,
                    )
                }
            }
        }
    }
}
