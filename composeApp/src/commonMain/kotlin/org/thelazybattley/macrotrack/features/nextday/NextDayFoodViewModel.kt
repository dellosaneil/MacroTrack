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
import org.thelazybattley.macrotrack.domain.model.NextDayFood
import org.thelazybattley.macrotrack.domain.model.NextDayPeriod
import org.thelazybattley.macrotrack.domain.model.NextDayPortion
import org.thelazybattley.macrotrack.domain.model.WeightReminder
import org.thelazybattley.macrotrack.domain.model.nextDayFoodKey
import org.thelazybattley.macrotrack.domain.model.nextDayOverview
import org.thelazybattley.macrotrack.domain.usecase.nextday.GetNextDayInputsUseCase
import org.thelazybattley.macrotrack.domain.usecase.nextday.NextDayInputs
import org.thelazybattley.macrotrack.domain.usecase.nextday.SetNextDayHeadsUpUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.GetLogReminderSettingsUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.FOOD_NAME
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.IS_RECIPE

/** Most portions offered on the amount picker. */
private const val MAX_PORTIONS = 4

/**
 * What [name] at dinner does to the next morning's weigh-in, over the last 90 days. [picked] is the amount chosen on
 * the picker, the usual one until then. [weighInReminder] is null while reminders are off altogether.
 */
data class NextDayFoodViewState(
    val name: String,
    val isRecipe: Boolean,
    val today: LocalDate = getCurrentDate(),
    val inputs: NextDayInputs? = null,
    val picked: Double? = null,
    val weighInReminder: WeightReminder? = null
) {
    val period: NextDayPeriod get() = NextDayPeriod.DAYS_90

    val key: String get() = nextDayFoodKey(name = name, isRecipe = isRecipe)

    val isLoaded: Boolean get() = inputs != null

    val food: NextDayFood? = inputs?.let {
        nextDayOverview(entries = it.entries, weights = it.weights, today = today, days = period.days).food(key = key)
    }

    val unit: String get() = inputs?.unitOf(name = name, isRecipe = isRecipe) ?: "g"

    /** The amount the numbers are for. */
    val amount: Double? get() = picked ?: food?.usual

    /** The amounts on the picker: the most eaten ones, smallest first, always with the one picked. */
    val portions: List<NextDayPortion>
        get() {
            val all = food?.portions.orEmpty()
            val top = all.sortedByDescending { it.nights.size }.take(n = MAX_PORTIONS).toMutableList()
            all.find { it.amount == amount }?.let { if (it !in top) top[top.lastIndex] = it }
            return top.sortedBy { it.amount }
        }

    val portion: NextDayPortion? get() = amount?.let { food?.portion(amount = it) }

    val isHeadsUpOn: Boolean get() = inputs?.settings?.isHeadsUpShown(key = key) ?: true
}

interface NextDayFoodCallbacks {
    fun onPortionSelected(amount: Double)
    fun onHeadsUpToggled()

    companion object {
        fun default() = object : NextDayFoodCallbacks {
            override fun onPortionSelected(amount: Double) = Unit
            override fun onHeadsUpToggled() = Unit
        }
    }
}

class NextDayFoodViewModel(
    getNextDayInputsUseCase: GetNextDayInputsUseCase,
    getLogReminderSettingsUseCase: GetLogReminderSettingsUseCase,
    private val setNextDayHeadsUpUseCase: SetNextDayHeadsUpUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), NextDayFoodCallbacks {

    private val _state = MutableStateFlow(
        value = NextDayFoodViewState(
            name = savedStateHandle.get<String>(key = FOOD_NAME).orEmpty(),
            isRecipe = savedStateHandle.get<String>(key = IS_RECIPE) == "true"
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

    override fun onPortionSelected(amount: Double) {
        _state.update { it.copy(picked = amount) }
    }

    override fun onHeadsUpToggled() {
        val currentState = _state.value
        viewModelScope.launch {
            setNextDayHeadsUpUseCase(key = currentState.key, isShown = !currentState.isHeadsUpOn)
        }
    }
}
