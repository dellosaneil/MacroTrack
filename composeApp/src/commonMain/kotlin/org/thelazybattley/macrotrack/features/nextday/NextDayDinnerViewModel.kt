package org.thelazybattley.macrotrack.features.nextday

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
import org.thelazybattley.macrotrack.domain.model.DinnerSize
import org.thelazybattley.macrotrack.domain.model.DinnerSizeGroup
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.NextDayDinner
import org.thelazybattley.macrotrack.domain.model.NextDayPeriod
import org.thelazybattley.macrotrack.domain.model.WeightReminder
import org.thelazybattley.macrotrack.domain.model.nextDayOverview
import org.thelazybattley.macrotrack.domain.usecase.foodlog.CopyFoodLogsUseCase
import org.thelazybattley.macrotrack.domain.usecase.nextday.GetNextDayInputsUseCase
import org.thelazybattley.macrotrack.domain.usecase.nextday.NextDayInputs
import org.thelazybattley.macrotrack.domain.usecase.nextday.SetNextDayHeadsUpUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.GetLogReminderSettingsUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

/**
 * One dinner, the foods in [key], over [period]. [picked] is the size chosen, the usual one until then.
 * [weighInReminder] is null while reminders are off altogether; [isLoggedAgain] once it's been copied to today.
 */
data class NextDayDinnerViewState(
    val key: String,
    val period: NextDayPeriod = NextDayPeriod.YEAR,
    val today: LocalDate = getCurrentDate(),
    val inputs: NextDayInputs? = null,
    val picked: DinnerSize? = null,
    val weighInReminder: WeightReminder? = null,
    val isLoggedAgain: Boolean = false
) {
    val isLoaded: Boolean get() = inputs != null

    val dinner: NextDayDinner? = inputs?.let {
        nextDayOverview(items = it.items, weights = it.weights, today = today, period = period).dinner(key = key)
    }

    /** The size the numbers are for: the one picked, or the usual one, or whichever it's been eaten at. */
    val size: DinnerSizeGroup?
        get() {
            val sizes = dinner?.sizes.orEmpty()
            return sizes.find { it.size == picked } ?: sizes.find { it.size == DinnerSize.USUAL } ?: sizes.firstOrNull()
        }

    /** Whether the heads-up is on for this dinner; turning it off for every dinner is separate. */
    val isHeadsUpOn: Boolean get() = inputs?.settings?.mutedDinners?.contains(key) != true

    val isHeadsUpAllOff: Boolean get() = inputs?.settings?.isHeadsUpOn == false

    fun unitOf(name: String, isRecipe: Boolean): String = inputs?.unitOf(name = name, isRecipe = isRecipe) ?: "g"
}

interface NextDayDinnerCallbacks {
    fun onSizeSelected(size: DinnerSize)
    fun onHeadsUpToggled()

    /** Copies the latest time it was eaten to today's Dinner. */
    fun onLogAgain()

    companion object {
        fun default() = object : NextDayDinnerCallbacks {
            override fun onSizeSelected(size: DinnerSize) = Unit
            override fun onHeadsUpToggled() = Unit
            override fun onLogAgain() = Unit
        }
    }
}

class NextDayDinnerViewModel(
    getNextDayInputsUseCase: GetNextDayInputsUseCase,
    getLogReminderSettingsUseCase: GetLogReminderSettingsUseCase,
    private val setNextDayHeadsUpUseCase: SetNextDayHeadsUpUseCase,
    private val copyFoodLogsUseCase: CopyFoodLogsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), NextDayDinnerCallbacks {

    private val _state = MutableStateFlow(
        value = NextDayDinnerViewState(
            key = savedStateHandle.get<String>(key = AppDestinations.DINNER_KEY).orEmpty(),
            period = nextDayPeriodOf(argument = savedStateHandle.get<String>(key = AppDestinations.NEXT_DAY_PERIOD))
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(flow = getNextDayInputsUseCase(), flow2 = getLogReminderSettingsUseCase()) { inputs, reminders ->
                _state.update { it.copy(inputs = inputs, weighInReminder = reminders.weight.takeIf { reminders.isOn }) }
            }.collect {}
        }
    }

    override fun onSizeSelected(size: DinnerSize) {
        _state.update { it.copy(picked = size) }
    }

    override fun onHeadsUpToggled() {
        val currentState = _state.value
        viewModelScope.launch {
            setNextDayHeadsUpUseCase(key = currentState.key, isShown = !currentState.isHeadsUpOn)
        }
    }

    override fun onLogAgain() {
        val currentState = _state.value
        if (currentState.isLoggedAgain) return
        val logs = currentState.dinner?.nights?.firstOrNull()?.items?.mapNotNull { it.log } ?: return
        _state.update { it.copy(isLoggedAgain = true) }
        viewModelScope.launch {
            copyFoodLogsUseCase(logs = logs, targetDates = listOf(getCurrentDate()), mealType = MealType.DINNER, replace = false)
        }
    }
}
