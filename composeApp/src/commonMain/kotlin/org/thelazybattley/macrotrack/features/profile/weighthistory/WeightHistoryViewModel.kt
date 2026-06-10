package org.thelazybattley.macrotrack.features.profile.weighthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Month
import kotlinx.datetime.number
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
                _state.update { currentState ->
                    currentState.copy(
                        completeWeightList = weightList,
                        averageWeight = weightList.map { it.weight }.average(),
                        filteredWeightList = weightList,
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
                        filteredWeightList = currentState.completeWeightList,
                        weightHistoryFilter = WeightHistoryFilters.All
                    )
                }

                WeightHistoryTimeRangeEnum.WEEK -> {
                    currentState.copy(
                        timeRange = timeRange,
                    )
                }

                WeightHistoryTimeRangeEnum.MONTH -> {
                    val filteredWeight =
                        currentState.completeWeightList.filter { it.date.month == getCurrentDate().month }
                    currentState.copy(
                        timeRange = timeRange,
                        filteredWeightList = filteredWeight,
                        weightHistoryFilter = WeightHistoryFilters.Monthly(
                            month = getCurrentDate().month
                        ),
                        averageWeight = filteredWeight.map { it.weight }.average()
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

    override fun onFilterBackButtonClicked() {
        _state.update { currentState ->
            when (currentState.timeRange) {
                WeightHistoryTimeRangeEnum.ALL -> TODO()
                WeightHistoryTimeRangeEnum.WEEK -> TODO()
                WeightHistoryTimeRangeEnum.MONTH -> {
                    val currentHistoryFilter =
                        currentState.weightHistoryFilter as WeightHistoryFilters.Monthly
                    val previousMonth = if (currentHistoryFilter.month == Month.DECEMBER) {
                        Month.JANUARY
                    } else {
                        Month(number = currentHistoryFilter.month.number.dec())
                    }
                    val filteredWeight = currentState.completeWeightList.filter { it.date.month == previousMonth }
                    currentState.copy(
                        weightHistoryFilter = WeightHistoryFilters.Monthly(
                            month = previousMonth
                        ),
                        filteredWeightList = filteredWeight,
                        averageWeight = filteredWeight.map { it.weight }.average()
                    )
                }

                WeightHistoryTimeRangeEnum.THREE_MONTHS -> TODO()
            }
        }

    }

    override fun onFilterNextButtonClicked() {
        _state.update { currentState ->
            when (currentState.timeRange) {
                WeightHistoryTimeRangeEnum.ALL -> TODO()
                WeightHistoryTimeRangeEnum.WEEK -> TODO()
                WeightHistoryTimeRangeEnum.MONTH -> {
                    val currentHistoryFilter =
                        currentState.weightHistoryFilter as WeightHistoryFilters.Monthly
                    val nextMonth = if (currentHistoryFilter.month == Month.JANUARY) {
                        Month.DECEMBER
                    } else {
                        Month(number = currentHistoryFilter.month.number.inc())
                    }
                    val filteredWeight = currentState.completeWeightList.filter { it.date.month == nextMonth }
                    currentState.copy(
                        weightHistoryFilter = WeightHistoryFilters.Monthly(
                            month = nextMonth
                        ),
                        filteredWeightList = filteredWeight,
                        averageWeight = filteredWeight.map { it.weight }.average()
                    )
                }

                WeightHistoryTimeRangeEnum.THREE_MONTHS -> TODO()
            }
        }
    }
}
