package org.thelazybattley.macrotrack.features.water

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.MINUTES_PER_DAY
import org.thelazybattley.macrotrack.domain.model.WATER_GOAL_MAX_ML
import org.thelazybattley.macrotrack.domain.model.WATER_GOAL_MIN_ML
import org.thelazybattley.macrotrack.domain.model.WATER_SIZE_MAX_ML
import org.thelazybattley.macrotrack.domain.model.WaterKind
import org.thelazybattley.macrotrack.domain.model.WaterSettings
import org.thelazybattley.macrotrack.domain.model.WaterSummary
import org.thelazybattley.macrotrack.domain.usecase.water.GetWaterSummaryUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.SaveWaterSettingsUseCase

data class WaterSettingsViewState(val summary: WaterSummary? = null, val isTypingGoal: Boolean = false)

interface WaterSettingsCallbacks {
    fun onGoalFromWeight(isFromWeight: Boolean)
    fun onOpenGoalKeypad()
    fun onCloseGoalKeypad()
    fun onCustomGoal(ml: Int)
    /** Moves a quick-add size by [stepMl], up or down. */
    fun onSizeStep(kind: WaterKind, stepMl: Int)
    fun onReminderToggled()
    fun onReminderEvery(hours: Int)
    /** Moves the start ([isStart]) or end of the reminder window by [hours]. */
    fun onReminderWindowStep(isStart: Boolean, hours: Int)
    fun onShowOnTodayToggled()

    companion object {
        fun default() = object : WaterSettingsCallbacks {
            override fun onGoalFromWeight(isFromWeight: Boolean) {}
            override fun onOpenGoalKeypad() {}
            override fun onCloseGoalKeypad() {}
            override fun onCustomGoal(ml: Int) {}
            override fun onSizeStep(kind: WaterKind, stepMl: Int) {}
            override fun onReminderToggled() {}
            override fun onReminderEvery(hours: Int) {}
            override fun onReminderWindowStep(isStart: Boolean, hours: Int) {}
            override fun onShowOnTodayToggled() {}
        }
    }
}

class WaterSettingsViewModel(
    getWaterSummaryUseCase: GetWaterSummaryUseCase,
    private val saveWaterSettingsUseCase: SaveWaterSettingsUseCase
) : ViewModel(), WaterSettingsCallbacks {

    private val _state = MutableStateFlow(value = WaterSettingsViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getWaterSummaryUseCase().collect { summary -> _state.update { it.copy(summary = summary) } }
        }
    }

    override fun onGoalFromWeight(isFromWeight: Boolean) = save { settings ->
        // Setting your own starts from the goal shown now, so nothing jumps.
        val current = _state.value.summary?.goalMl ?: settings.customGoalMl
        settings.copy(isGoalFromWeight = isFromWeight, customGoalMl = if (isFromWeight) settings.customGoalMl else current)
    }

    override fun onOpenGoalKeypad() {
        _state.update { it.copy(isTypingGoal = true) }
    }

    override fun onCloseGoalKeypad() {
        _state.update { it.copy(isTypingGoal = false) }
    }

    override fun onCustomGoal(ml: Int) {
        _state.update { it.copy(isTypingGoal = false) }
        save { it.copy(customGoalMl = ml.coerceIn(minimumValue = WATER_GOAL_MIN_ML, maximumValue = WATER_GOAL_MAX_ML)) }
    }

    override fun onSizeStep(kind: WaterKind, stepMl: Int) = save { settings ->
        val min = kotlin.math.abs(stepMl) * 2
        when (kind) {
            WaterKind.GLASS -> settings.copy(glassMl = (settings.glassMl + stepMl).coerceIn(minimumValue = min, maximumValue = WATER_SIZE_MAX_ML))
            WaterKind.BOTTLE -> settings.copy(bottleMl = (settings.bottleMl + stepMl).coerceIn(minimumValue = min, maximumValue = WATER_SIZE_MAX_ML))
            WaterKind.OTHER -> settings
        }
    }

    override fun onReminderToggled() = save { it.copy(isReminderOn = !it.isReminderOn) }

    override fun onReminderEvery(hours: Int) = save { it.copy(reminderEveryHours = hours) }

    /** The window stays at least one check long and inside the day. */
    override fun onReminderWindowStep(isStart: Boolean, hours: Int) = save { settings ->
        val step = hours * 60
        if (isStart) {
            val start = (settings.reminderStartMinutes + step).coerceIn(minimumValue = 0, maximumValue = settings.reminderEndMinutes - settings.reminderEveryHours * 60)
            settings.copy(reminderStartMinutes = start)
        } else {
            val end = (settings.reminderEndMinutes + step)
                .coerceIn(minimumValue = settings.reminderStartMinutes + settings.reminderEveryHours * 60, maximumValue = MINUTES_PER_DAY - 60)
            settings.copy(reminderEndMinutes = end)
        }
    }

    override fun onShowOnTodayToggled() = save { it.copy(isShownOnToday = !it.isShownOnToday) }

    private fun save(update: (WaterSettings) -> WaterSettings) {
        viewModelScope.launch { saveWaterSettingsUseCase(update = update) }
    }
}
