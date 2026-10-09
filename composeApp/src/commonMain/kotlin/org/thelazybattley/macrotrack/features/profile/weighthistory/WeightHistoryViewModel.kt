package org.thelazybattley.macrotrack.features.profile.weighthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.InsertWeightUseCase
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryTimeRangeEnum

class WeightHistoryViewModel(
    private val getAllWeightUseCase: GetAllWeightUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val insertWeightUseCase: InsertWeightUseCase,
    private val insertUserDetailsUseCase: InsertUserDetailsUseCase
) : ViewModel(), WeightHistoryCallbacks {

    private val _state = MutableStateFlow(value = WeightHistoryViewState())

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getAllWeightUseCase(),
                flow2 = getUserDetailsUseCase()
            ) { weights, userDetails ->
                _state.update { currentState ->
                    val sortedWeights = weights.sortedBy { it.date }
                    val period = currentState.period
                        ?.takeIf { it.range != WeightHistoryTimeRangeEnum.ALL }
                        ?: periodContaining(
                            range = currentState.timeRange,
                            date = getCurrentDate(),
                            earliestEntry = sortedWeights.firstOrNull()?.date
                        )
                    currentState.copy(
                        userDetails = userDetails,
                        allWeights = sortedWeights
                    ).withPeriod(period = period)
                }
            }.collect()
        }
    }

    override fun onTimePeriodSelect(timeRange: WeightHistoryTimeRangeEnum) {
        _state.update { currentState ->
            currentState.copy(timeRange = timeRange).withPeriod(
                period = periodContaining(
                    range = timeRange,
                    date = getCurrentDate(),
                    earliestEntry = currentState.allWeights.firstOrNull()?.date
                )
            )
        }
    }

    override fun onPreviousPeriod() {
        _state.update { currentState ->
            if (!currentState.canGoBack) return@update currentState
            val period = currentState.period ?: return@update currentState
            currentState.withPeriod(period = period.shift(steps = -1))
        }
    }

    override fun onNextPeriod() {
        _state.update { currentState ->
            if (!currentState.canGoNext) return@update currentState
            val period = currentState.period ?: return@update currentState
            currentState.withPeriod(period = period.shift(steps = 1))
        }
    }

    override fun onSaveWeight(weight: String) {
        val newWeight = weight.toDoubleOrNull()?.takeIf { it > 0 } ?: return
        val userDetails = _state.value.userDetails
        viewModelScope.launch {
            insertWeightUseCase(weight = newWeight)
            if (userDetails != null) {
                insertUserDetailsUseCase(userDetails = userDetails.copy(weight = newWeight))
            }
        }
    }

    private fun WeightHistoryViewState.withPeriod(period: WeightPeriod): WeightHistoryViewState {
        val today = getCurrentDate()
        val earliest = allWeights.firstOrNull()?.date
        val isAll = period.range == WeightHistoryTimeRangeEnum.ALL
        val previousPeriod = if (isAll) null else period.shift(steps = -1)
        return copy(
            period = period,
            periodWeights = allWeights.inPeriod(period = period),
            previousPeriod = previousPeriod,
            previousWeights = previousPeriod?.let { allWeights.inPeriod(period = it) }.orEmpty(),
            canGoBack = !isAll && earliest != null && earliest < period.start,
            canGoNext = !isAll && period.end < today
        )
    }
}
