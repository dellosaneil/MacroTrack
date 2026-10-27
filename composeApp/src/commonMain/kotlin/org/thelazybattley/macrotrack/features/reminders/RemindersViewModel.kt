package org.thelazybattley.macrotrack.features.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.LogReminderScheduler
import org.thelazybattley.macrotrack.domain.model.LogReminderSettings
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.usualLogMinutes
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.GetLogReminderSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.SaveLogReminderSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.reminder.SendTestLogReminderUseCase

/** How long "Test reminder sent" shows. */
private const val TEST_SENT_MILLIS = 2_500L

class RemindersViewModel(
    getLogReminderSettingsUseCase: GetLogReminderSettingsUseCase,
    getAllFoodLogUseCase: GetAllFoodLogUseCase,
    scheduler: LogReminderScheduler,
    private val saveLogReminderSettingsUseCase: SaveLogReminderSettingsUseCase,
    private val sendTestLogReminderUseCase: SendTestLogReminderUseCase
) : ViewModel(), RemindersCallbacks {

    private val _state = MutableStateFlow(value = RemindersViewState(canSendTest = scheduler.canSendTest))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(flow = getLogReminderSettingsUseCase(), flow2 = getAllFoodLogUseCase()) { settings, foodLogs ->
                val today = getCurrentDate()
                val zone = TimeZone.currentSystemDefault()
                settings to MealType.entries.mapNotNull { mealType ->
                    usualLogMinutes(foodLogs = foodLogs, mealType = mealType, today = today, timeZone = zone)?.let { mealType to it }
                }.toMap()
            }.collect { (settings, usual) ->
                _state.update { it.copy(settings = settings, usualMinutes = usual) }
            }
        }
    }

    override fun onMasterToggled() = save { it.copy(isOn = !it.isOn) }

    override fun onMealToggled(mealType: MealType) = save { settings ->
        val meal = settings.meal(mealType = mealType)
        settings.copy(meals = settings.meals + (mealType to meal.copy(isOn = !meal.isOn)))
    }

    override fun onMealTimePicked(mealType: MealType, minutes: Int) = save { settings ->
        settings.copy(meals = settings.meals + (mealType to settings.meal(mealType = mealType).copy(isOn = true, minutes = minutes)))
    }

    override fun onWeightToggled() = save { it.copy(weight = it.weight.copy(isOn = !it.weight.isOn)) }

    override fun onWeightTimePicked(minutes: Int) = save { it.copy(weight = it.weight.copy(isOn = true, minutes = minutes)) }

    override fun onWeightDayToggled(day: DayOfWeek) = save { settings ->
        val days = settings.weight.days
        val number = day.isoDayNumber
        settings.copy(weight = settings.weight.copy(days = if (number in days) days - number else days + number))
    }

    override fun onQuickActionsToggled() = save { it.copy(hasQuickActions = !it.hasQuickActions) }

    override fun onEaseOffToggled() = save { it.copy(easesOff = !it.easesOff) }

    override fun onQuietWeekendsToggled() = save { it.copy(isQuietOnWeekends = !it.isQuietOnWeekends) }

    override fun onSendTest() {
        viewModelScope.launch {
            sendTestLogReminderUseCase()
            _state.update { it.copy(isTestSent = true) }
            delay(timeMillis = TEST_SENT_MILLIS)
            _state.update { it.copy(isTestSent = false) }
        }
    }

    private fun save(change: (LogReminderSettings) -> LogReminderSettings) {
        val settings = change(_state.value.settings)
        _state.update { it.copy(settings = settings) }
        viewModelScope.launch {
            saveLogReminderSettingsUseCase(settings = settings)
        }
    }
}
