package org.thelazybattley.macrotrack.features.nextday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.NextDayOverview
import org.thelazybattley.macrotrack.domain.model.NextDayPeriod
import org.thelazybattley.macrotrack.domain.model.nextDayOverview
import org.thelazybattley.macrotrack.domain.usecase.nextday.GetNextDayInputsUseCase
import org.thelazybattley.macrotrack.domain.usecase.nextday.NextDayInputs
import org.thelazybattley.macrotrack.domain.usecase.nextday.SetNextDaySettingsUseCase

/** The summary: the biggest jump, every dinner split by what it does, and the top few each way, over [period]. */
data class NextDayWeightViewState(
    val today: LocalDate = getCurrentDate(),
    val inputs: NextDayInputs? = null,
    val period: NextDayPeriod = NextDayPeriod.YEAR
) {
    val overview: NextDayOverview? =
        inputs?.let { nextDayOverview(items = it.items, weights = it.weights, today = today, period = period) }

    val isHeadsUpOn: Boolean get() = inputs?.settings?.isHeadsUpOn ?: true

    fun unitOf(name: String, isRecipe: Boolean): String = inputs?.unitOf(name = name, isRecipe = isRecipe) ?: "g"
}

interface NextDayWeightCallbacks {
    fun onPeriodSelected(period: NextDayPeriod)
    fun onHeadsUpToggled()

    companion object {
        fun default() = object : NextDayWeightCallbacks {
            override fun onPeriodSelected(period: NextDayPeriod) = Unit
            override fun onHeadsUpToggled() = Unit
        }
    }
}

class NextDayWeightViewModel(
    getNextDayInputsUseCase: GetNextDayInputsUseCase,
    private val setNextDaySettingsUseCase: SetNextDaySettingsUseCase
) : ViewModel(), NextDayWeightCallbacks {

    private val _state = MutableStateFlow(value = NextDayWeightViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getNextDayInputsUseCase().collect { inputs ->
                _state.update { it.copy(inputs = inputs) }
            }
        }
        // Opening it once takes the NEW tag off its rows on Progress and Weight history.
        viewModelScope.launch {
            val settings = getNextDayInputsUseCase().first().settings
            if (!settings.isSeen) setNextDaySettingsUseCase(settings = settings.copy(isSeen = true))
        }
    }

    override fun onPeriodSelected(period: NextDayPeriod) {
        _state.update { it.copy(period = period) }
    }

    override fun onHeadsUpToggled() {
        val settings = _state.value.inputs?.settings ?: return
        viewModelScope.launch {
            setNextDaySettingsUseCase(settings = settings.copy(isHeadsUpOn = !settings.isHeadsUpOn))
        }
    }
}
