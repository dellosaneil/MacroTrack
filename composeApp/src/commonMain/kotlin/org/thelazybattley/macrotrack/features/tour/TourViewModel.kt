package org.thelazybattley.macrotrack.features.tour

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.data.local.tour.TourSampleData
import org.thelazybattley.macrotrack.domain.usecase.tour.IsTourCompletedUseCase
import org.thelazybattley.macrotrack.domain.usecase.tour.SetTourCompletedUseCase

class TourViewModel(
    private val isTourCompletedUseCase: IsTourCompletedUseCase,
    private val setTourCompletedUseCase: SetTourCompletedUseCase,
    private val sampleData: TourSampleData
) : ViewModel(), TourCallbacks {

    private val _state = MutableStateFlow(value = TourViewState())
    val state = _state.asStateFlow()

    private var hasCheckedFirstRun = false

    /** Starts the tour the first time the main screen shows, unless it was finished or skipped before. */
    fun onMainScreenShown() {
        if (hasCheckedFirstRun) return
        hasCheckedFirstRun = true
        viewModelScope.launch {
            if (!isTourCompletedUseCase().first()) start()
        }
    }

    override fun onNextClicked() {
        val step = _state.value.step ?: return
        if (step.isLast) {
            end(outcome = TourOutcome.FINISHED)
        } else {
            _state.update { it.copy(step = TourStep.shown[step.index + 1]) }
        }
    }

    override fun onBackClicked() {
        val step = _state.value.step ?: return
        if (step.isFirst) return
        _state.update { it.copy(step = TourStep.shown[step.index - 1]) }
    }

    override fun onSkipClicked() {
        end(outcome = TourOutcome.SKIPPED)
    }

    override fun onReplayClicked() {
        viewModelScope.launch { start() }
    }

    override fun onOutcomeDismissed() {
        _state.update { it.copy(outcome = null) }
    }

    /** Every screen behind the tour shows the example log, so it's in place before the first step shows. */
    private suspend fun start() {
        sampleData.start()
        _state.update { it.copy(step = TourStep.shown.first(), outcome = null) }
    }

    override fun onCleared() {
        sampleData.stop()
    }

    private fun end(outcome: TourOutcome) {
        sampleData.stop()
        _state.update { it.copy(step = null, outcome = outcome) }
        viewModelScope.launch {
            setTourCompletedUseCase(isCompleted = true)
        }
    }
}
