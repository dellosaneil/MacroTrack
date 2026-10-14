package org.thelazybattley.macrotrack.features.tour

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.usecase.tour.IsTourCompletedUseCase
import org.thelazybattley.macrotrack.domain.usecase.tour.SetTourCompletedUseCase

class TourViewModel(
    private val isTourCompletedUseCase: IsTourCompletedUseCase,
    private val setTourCompletedUseCase: SetTourCompletedUseCase
) : ViewModel(), TourCallbacks {

    private val _state = MutableStateFlow(value = TourViewState())
    val state = _state.asStateFlow()

    private var hasCheckedFirstRun = false

    /** Starts the tour the first time the main screen shows, unless it was finished or skipped before. */
    fun onMainScreenShown() {
        if (hasCheckedFirstRun) return
        hasCheckedFirstRun = true
        viewModelScope.launch {
            if (!isTourCompletedUseCase().first()) {
                _state.update { it.copy(step = TourStep.entries.first(), outcome = null) }
            }
        }
    }

    override fun onNextClicked() {
        val step = _state.value.step ?: return
        if (step.isLast) {
            end(outcome = TourOutcome.FINISHED)
        } else {
            _state.update { it.copy(step = TourStep.entries[step.ordinal + 1]) }
        }
    }

    override fun onBackClicked() {
        val step = _state.value.step ?: return
        if (step.isFirst) return
        _state.update { it.copy(step = TourStep.entries[step.ordinal - 1]) }
    }

    override fun onSkipClicked() {
        end(outcome = TourOutcome.SKIPPED)
    }

    override fun onReplayClicked() {
        _state.update { it.copy(step = TourStep.entries.first(), outcome = null) }
    }

    override fun onOutcomeDismissed() {
        _state.update { it.copy(outcome = null) }
    }

    private fun end(outcome: TourOutcome) {
        _state.update { it.copy(step = null, outcome = outcome) }
        viewModelScope.launch {
            setTourCompletedUseCase(isCompleted = true)
        }
    }
}
