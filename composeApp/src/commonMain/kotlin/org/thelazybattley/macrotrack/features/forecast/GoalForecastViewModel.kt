package org.thelazybattley.macrotrack.features.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.DeadlinePlan
import org.thelazybattley.macrotrack.domain.model.ForecastBasis
import org.thelazybattley.macrotrack.domain.model.WhatIfResult
import org.thelazybattley.macrotrack.domain.model.deadlinePlan
import org.thelazybattley.macrotrack.domain.model.whatIf
import org.thelazybattley.macrotrack.domain.model.whatIfRange
import org.thelazybattley.macrotrack.domain.usecase.forecast.AddCustomMilestoneUseCase
import org.thelazybattley.macrotrack.domain.usecase.forecast.ApplyDailyCaloriesUseCase
import org.thelazybattley.macrotrack.domain.usecase.forecast.DeleteCustomMilestoneUseCase
import org.thelazybattley.macrotrack.domain.usecase.forecast.ForecastSnapshot
import org.thelazybattley.macrotrack.domain.usecase.forecast.GetGoalForecastUseCase
import org.thelazybattley.macrotrack.domain.usecase.forecast.SetForecastSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase

/** The deadlines Pick a date offers before the user picks their own, in weeks from today. */
val DEADLINE_PRESET_WEEKS = listOf(8, 12, 16, 24)

data class GoalForecastViewState(
    val snapshot: ForecastSnapshot? = null,
    val isLoaded: Boolean = false,
    /** The calories What if is set to; null until dragged, which shows today's target. */
    val whatIfKcal: Int? = null,
    /** The deadline picked on Pick a date; null shows the first preset. */
    val deadline: LocalDate? = null,
    val isDeadlineMilestone: Boolean = true,
    /** Set once a new target is saved, so the screen can close. */
    val isApplied: Boolean = false
) {
    val currentKcal: Int? get() = snapshot?.macroGoals?.calories

    val shownWhatIfKcal: Int?
        get() {
            val forecast = snapshot?.forecast ?: return null
            return whatIfKcal ?: currentKcal?.coerceIn(range = forecast.whatIfRange())
        }

    val whatIf: WhatIfResult? get() = shownWhatIfKcal?.let { snapshot?.forecast?.whatIf(kcal = it) }

    /** What If at today's target, to compare against. */
    val whatIfNow: WhatIfResult? get() = currentKcal?.let { snapshot?.forecast?.whatIf(kcal = it) }

    val deadlineOptions: List<LocalDate>
        get() {
            val today = snapshot?.today ?: return emptyList()
            return DEADLINE_PRESET_WEEKS.map { today.plus(period = DatePeriod(days = it * 7)) }
        }

    val shownDeadline: LocalDate? get() = deadline ?: deadlineOptions.firstOrNull()

    val deadlinePlan: DeadlinePlan? get() = shownDeadline?.let { snapshot?.forecast?.deadlinePlan(date = it) }
}

interface GoalForecastCallbacks {
    fun onBasisSelect(basis: ForecastBasis)
    fun onRangeShownToggle()
    fun onWhatIfKcalChange(kcal: Int)
    fun onApplyWhatIf()
    fun onDeadlineSelect(date: LocalDate)
    fun onDeadlineMilestoneToggle()

    /** Saves the deadline's calories; [milestoneName] names the milestone added when that's switched on. */
    fun onApplyDeadline(milestoneName: String)
    fun onAddMilestone(name: String, weightKg: Double, byDate: LocalDate?)
    fun onDeleteMilestone(id: Long)
    fun onSwitchToMaintenance()
    fun onSaveTargetWeight(targetKg: Double)

    companion object {
        fun default() = object : GoalForecastCallbacks {
            override fun onBasisSelect(basis: ForecastBasis) = Unit
            override fun onRangeShownToggle() = Unit
            override fun onWhatIfKcalChange(kcal: Int) = Unit
            override fun onApplyWhatIf() = Unit
            override fun onDeadlineSelect(date: LocalDate) = Unit
            override fun onDeadlineMilestoneToggle() = Unit
            override fun onApplyDeadline(milestoneName: String) = Unit
            override fun onAddMilestone(name: String, weightKg: Double, byDate: LocalDate?) = Unit
            override fun onDeleteMilestone(id: Long) = Unit
            override fun onSwitchToMaintenance() = Unit
            override fun onSaveTargetWeight(targetKg: Double) = Unit
        }
    }
}

/** Backs the goal forecast and its What if, Pick a date, Milestones and settings screens. */
class GoalForecastViewModel(
    getGoalForecastUseCase: GetGoalForecastUseCase,
    private val setForecastSettingsUseCase: SetForecastSettingsUseCase,
    private val addCustomMilestoneUseCase: AddCustomMilestoneUseCase,
    private val deleteCustomMilestoneUseCase: DeleteCustomMilestoneUseCase,
    private val applyDailyCaloriesUseCase: ApplyDailyCaloriesUseCase,
    private val insertUserDetailsUseCase: InsertUserDetailsUseCase
) : ViewModel(), GoalForecastCallbacks {

    private val _state = MutableStateFlow(value = GoalForecastViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getGoalForecastUseCase().collect { snapshot ->
                _state.update { it.copy(snapshot = snapshot, isLoaded = true) }
            }
        }
    }

    override fun onBasisSelect(basis: ForecastBasis) {
        val settings = _state.value.snapshot?.settings ?: return
        viewModelScope.launch { setForecastSettingsUseCase(settings = settings.copy(basis = basis)) }
    }

    override fun onRangeShownToggle() {
        val settings = _state.value.snapshot?.settings ?: return
        viewModelScope.launch { setForecastSettingsUseCase(settings = settings.copy(isRangeShown = !settings.isRangeShown)) }
    }

    override fun onWhatIfKcalChange(kcal: Int) {
        val range = _state.value.snapshot?.forecast?.whatIfRange() ?: return
        _state.update { it.copy(whatIfKcal = kcal.coerceIn(range = range)) }
    }

    override fun onApplyWhatIf() {
        val kcal = _state.value.shownWhatIfKcal ?: return
        applyCalories(kcal = kcal)
    }

    override fun onDeadlineSelect(date: LocalDate) {
        _state.update { it.copy(deadline = date) }
    }

    override fun onDeadlineMilestoneToggle() {
        _state.update { it.copy(isDeadlineMilestone = !it.isDeadlineMilestone) }
    }

    override fun onApplyDeadline(milestoneName: String) {
        val currentState = _state.value
        val plan = currentState.deadlinePlan ?: return
        val forecast = currentState.snapshot?.forecast ?: return
        if (plan.isTooSoon) return
        viewModelScope.launch {
            if (currentState.isDeadlineMilestone) {
                addCustomMilestoneUseCase(name = milestoneName, weight = forecast.targetWeight, byDate = plan.date)
            }
        }
        applyCalories(kcal = plan.kcal)
    }

    override fun onAddMilestone(name: String, weightKg: Double, byDate: LocalDate?) {
        if (name.isBlank()) return
        viewModelScope.launch { addCustomMilestoneUseCase(name = name.trim(), weight = weightKg, byDate = byDate) }
    }

    override fun onDeleteMilestone(id: Long) {
        viewModelScope.launch { deleteCustomMilestoneUseCase(id = id) }
    }

    override fun onSwitchToMaintenance() {
        val maintenance = _state.value.snapshot?.maintenance ?: return
        applyCalories(kcal = maintenance)
    }

    /** A new goal starts from today's weight, as setting one on Profile does. */
    override fun onSaveTargetWeight(targetKg: Double) {
        val userDetails = _state.value.snapshot?.userDetails ?: return
        if (targetKg <= 0) return
        viewModelScope.launch {
            insertUserDetailsUseCase(
                userDetails = userDetails.copy(
                    targetWeight = targetKg,
                    targetStartWeight = userDetails.weight,
                    targetSetDate = getCurrentDate()
                )
            )
        }
    }

    private fun applyCalories(kcal: Int) {
        val snapshot = _state.value.snapshot ?: return
        if (_state.value.isApplied) return
        _state.update { it.copy(isApplied = true) }
        viewModelScope.launch {
            applyDailyCaloriesUseCase(userDetails = snapshot.userDetails, kcal = kcal, maintenance = snapshot.maintenance)
        }
    }
}
