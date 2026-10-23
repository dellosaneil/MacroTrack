package org.thelazybattley.macrotrack.features.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.StepNotifier
import org.thelazybattley.macrotrack.core.platform.StepNudgeScheduler
import org.thelazybattley.macrotrack.domain.model.DayActivity
import org.thelazybattley.macrotrack.domain.model.StepGoal
import org.thelazybattley.macrotrack.domain.model.StepNotificationExtra
import org.thelazybattley.macrotrack.domain.model.StepNotificationSettings
import org.thelazybattley.macrotrack.domain.usecase.activity.GetBurnProfileUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetDayActivityUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetStepGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetStepNotificationSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.IsStepNudgeEnabledUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.SetStepNudgeEnabledUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.UpdateStepNotificationSettingsUseCase

data class StepsSettingsViewState(
    val settings: StepNotificationSettings = StepNotificationSettings(),
    val isNudgeOn: Boolean = true,
    val goal: Int = StepGoal.DEFAULT,
    /** Today so far, for the preview. */
    val day: DayActivity = DayActivity(totalSteps = 0, sessions = emptyList()),
    val distanceMeters: Double = 0.0,
    /** Null until the profile is set up. */
    val activeKcal: Int? = null,
    val isNotificationSupported: Boolean = true,
    val isNudgeSupported: Boolean = true
) {
    val stepsLeft: Int get() = (goal - day.totalSteps).coerceAtLeast(minimumValue = 0)
}

interface StepsSettingsCallbacks {
    fun onShowStepsToggled()
    fun onLockScreenToggled()
    fun onExtraPicked(extra: StepNotificationExtra)
    fun onGoalAlertToggled()
    fun onNudgeToggled()
    fun onNudgeHourPicked(hour: Int)

    companion object {
        fun default() = object : StepsSettingsCallbacks {
            override fun onShowStepsToggled() = Unit
            override fun onLockScreenToggled() = Unit
            override fun onExtraPicked(extra: StepNotificationExtra) = Unit
            override fun onGoalAlertToggled() = Unit
            override fun onNudgeToggled() = Unit
            override fun onNudgeHourPicked(hour: Int) = Unit
        }
    }
}

/** Profile › Steps & walks: the steps notification, the step alerts, and a preview of what they'll say. */
class StepsSettingsViewModel(
    getStepNotificationSettingsUseCase: GetStepNotificationSettingsUseCase,
    isStepNudgeEnabledUseCase: IsStepNudgeEnabledUseCase,
    getStepGoalUseCase: GetStepGoalUseCase,
    getDayActivityUseCase: GetDayActivityUseCase,
    getBurnProfileUseCase: GetBurnProfileUseCase,
    stepNotifier: StepNotifier,
    stepNudgeScheduler: StepNudgeScheduler,
    private val updateStepNotificationSettingsUseCase: UpdateStepNotificationSettingsUseCase,
    private val setStepNudgeEnabledUseCase: SetStepNudgeEnabledUseCase
) : ViewModel(), StepsSettingsCallbacks {

    private val _state = MutableStateFlow(
        value = StepsSettingsViewState(isNotificationSupported = stepNotifier.isSupported, isNudgeSupported = stepNudgeScheduler.isSupported)
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getStepNotificationSettingsUseCase(),
                flow2 = isStepNudgeEnabledUseCase(),
                flow3 = getStepGoalUseCase()
            ) { settings, isNudgeOn, goal ->
                _state.update { it.copy(settings = settings, isNudgeOn = isNudgeOn, goal = goal) }
            }.collect {}
        }
        viewModelScope.launch {
            combine(flow = getDayActivityUseCase(date = getCurrentDate()), flow2 = getBurnProfileUseCase()) { day, profile ->
                _state.update {
                    it.copy(
                        day = day,
                        distanceMeters = day.distanceMeters(profile = profile),
                        activeKcal = profile?.let { burn -> day.activeKcal(profile = burn) }
                    )
                }
            }.collect {}
        }
    }

    override fun onShowStepsToggled() = update { it.copy(isOn = !it.isOn) }

    override fun onLockScreenToggled() = update { it.copy(isOnLockScreen = !it.isOnLockScreen) }

    override fun onExtraPicked(extra: StepNotificationExtra) = update { it.copy(extra = extra) }

    override fun onGoalAlertToggled() = update { it.copy(isGoalAlertOn = !it.isGoalAlertOn) }

    override fun onNudgeHourPicked(hour: Int) = update { it.copy(nudgeHour = hour) }

    override fun onNudgeToggled() {
        val isOn = !_state.value.isNudgeOn
        _state.update { it.copy(isNudgeOn = isOn) }
        viewModelScope.launch { setStepNudgeEnabledUseCase(isEnabled = isOn) }
    }

    private fun update(change: (StepNotificationSettings) -> StepNotificationSettings) {
        _state.update { it.copy(settings = change(it.settings)) }
        viewModelScope.launch { updateStepNotificationSettingsUseCase(change = change) }
    }
}
