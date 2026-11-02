package org.thelazybattley.macrotrack.features.whatfits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.FitAmountBasis
import org.thelazybattley.macrotrack.domain.model.FitFood
import org.thelazybattley.macrotrack.domain.model.FitIdeas
import org.thelazybattley.macrotrack.domain.model.FitMacros
import org.thelazybattley.macrotrack.domain.model.FitRanking
import org.thelazybattley.macrotrack.domain.model.FitSource
import org.thelazybattley.macrotrack.domain.model.FitTooBig
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.WhatFitsSettings
import org.thelazybattley.macrotrack.domain.model.findFitIdeas
import org.thelazybattley.macrotrack.domain.model.fitMealOptions
import org.thelazybattley.macrotrack.domain.model.sum
import org.thelazybattley.macrotrack.domain.model.toFitMacros
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetFoodLogByDateUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.whatfits.FitPick
import org.thelazybattley.macrotrack.domain.usecase.whatfits.GetFitFoodsUseCase
import org.thelazybattley.macrotrack.domain.usecase.whatfits.GetWhatFitsSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.whatfits.LogFitPicksUseCase
import org.thelazybattley.macrotrack.domain.usecase.whatfits.PlanFitPicksUseCase
import org.thelazybattley.macrotrack.domain.usecase.whatfits.SetWhatFitsSettingsUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.MEAL_TYPE

/**
 * What's left of today's goal and the ideas that fit it. [amounts] holds amounts set by hand or frozen when an idea
 * was picked; [picked] keeps the picked food keys in the order picked. [amountKey] is the food open on the amount
 * screen, at [draftAmount].
 */
data class WhatFitsViewState(
    val isLoaded: Boolean = false,
    val foods: List<FitFood> = emptyList(),
    val settings: WhatFitsSettings = WhatFitsSettings(),
    val goal: MacroGoals? = null,
    val eaten: FitMacros = FitMacros(),
    val eatenNames: Set<String> = emptySet(),
    val loggedMeals: Set<MealType> = emptySet(),
    val mealType: MealType = MealType.DINNER,
    val amounts: Map<String, Double> = emptyMap(),
    val picked: List<String> = emptyList(),
    val amountKey: String? = null,
    val draftAmount: Double = 0.0,
    /** Set once picks are logged or planned, so the screens go back to Today. */
    val isDone: Boolean = false
) {
    val goalMacros: FitMacros get() = goal?.toFitMacros() ?: FitMacros()

    val left: FitMacros get() = goalMacros - eaten

    val ideas: FitIdeas
        get() = findFitIdeas(
            foods = foods,
            left = left,
            settings = settings,
            mealType = mealType,
            eatenToday = eatenNames,
            amounts = amounts
        )

    val mealOptions: List<MealType> get() = fitMealOptions(loggedMeals = loggedMeals, selected = mealType)

    val picks: List<FitPick>
        get() = picked.mapNotNull { key ->
            val food = foods.find { it.key == key } ?: return@mapNotNull null
            FitPick(food = food, amount = amounts[key] ?: food.startAmount(basis = settings.amountBasis))
        }

    val pickedMacros: FitMacros get() = picks.map { it.food.macrosFor(amount = it.amount) }.sum()

    val leftAfterPicks: FitMacros get() = left - pickedMacros

    val amountFood: FitFood? get() = amountKey?.let { key -> foods.find { it.key == key } }

    /** What's left once the open food is eaten at [draftAmount], with the other picks counted too. */
    val leftAfterDraft: FitMacros
        get() {
            val food = amountFood ?: return left
            val others = picks.filter { it.food.key != food.key }.map { it.food.macrosFor(amount = it.amount) }.sum()
            return left - others - food.macrosFor(amount = draftAmount)
        }

    /** The amount that fills the protein left, or null when the food has none. */
    val fillProteinAmount: Double?
        get() {
            val food = amountFood ?: return null
            val others = picks.filter { it.food.key != food.key }.sumOf { it.food.macrosFor(amount = it.amount).protein }
            return food.amountForProtein(protein = left.protein - others)
        }
}

interface WhatFitsCallbacks {
    fun onMealSelected(mealType: MealType)
    fun onTogglePick(key: String)
    fun onOpenAmount(key: String)
    fun onDraftAmount(amount: Double)
    fun onDraftStep(steps: Int)

    /** Picks the open food at the amount chosen. */
    fun onPickDraft()

    /** Logs only the open food, at the amount chosen. */
    fun onLogDraft()
    fun onStepPick(key: String, steps: Int)
    fun onLogPicks()
    fun onPlanPicks()
    fun onAmountBasis(basis: FitAmountBasis)
    fun onTooBig(tooBig: FitTooBig)
    fun onRanking(ranking: FitRanking)
    fun onSpareCalories(calories: Int)
    fun onToggleSource(source: FitSource)
    fun onToggleSkipEatenToday()

    companion object {
        fun default() = object : WhatFitsCallbacks {
            override fun onMealSelected(mealType: MealType) = Unit
            override fun onTogglePick(key: String) = Unit
            override fun onOpenAmount(key: String) = Unit
            override fun onDraftAmount(amount: Double) = Unit
            override fun onDraftStep(steps: Int) = Unit
            override fun onPickDraft() = Unit
            override fun onLogDraft() = Unit
            override fun onStepPick(key: String, steps: Int) = Unit
            override fun onLogPicks() = Unit
            override fun onPlanPicks() = Unit
            override fun onAmountBasis(basis: FitAmountBasis) = Unit
            override fun onTooBig(tooBig: FitTooBig) = Unit
            override fun onRanking(ranking: FitRanking) = Unit
            override fun onSpareCalories(calories: Int) = Unit
            override fun onToggleSource(source: FitSource) = Unit
            override fun onToggleSkipEatenToday() = Unit
        }
    }
}

/** Shared by the ideas, amount, review and settings screens, which all open from the ideas screen's back stack entry. */
class WhatFitsViewModel(
    getFitFoodsUseCase: GetFitFoodsUseCase,
    getWhatFitsSettingsUseCase: GetWhatFitsSettingsUseCase,
    getFoodLogByDateUseCase: GetFoodLogByDateUseCase,
    getUserDetailsUseCase: GetUserDetailsUseCase,
    calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val setWhatFitsSettingsUseCase: SetWhatFitsSettingsUseCase,
    private val logFitPicksUseCase: LogFitPicksUseCase,
    private val planFitPicksUseCase: PlanFitPicksUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), WhatFitsCallbacks {

    private val requestedMeal: MealType? = savedStateHandle.get<String>(key = MEAL_TYPE)?.let { name ->
        MealType.entries.find { it.name == name }
    }
    private val _state = MutableStateFlow(value = WhatFitsViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getFitFoodsUseCase(),
                flow2 = getWhatFitsSettingsUseCase(),
                flow3 = getFoodLogByDateUseCase(localDate = getCurrentDate()),
                flow4 = flow { emitAll(getUserDetailsUseCase()) }
            ) { foods, settings, todayLogs, user ->
                val loggedMeals = todayLogs.map { it.mealType }.toSet()
                _state.update { currentState ->
                    currentState.copy(
                        isLoaded = true,
                        foods = foods,
                        settings = settings,
                        goal = user?.let { calculateMacrosGoalUseCase(userDetails = it) },
                        eaten = todayLogs.map { it.toFitMacros() }.sum(),
                        eatenNames = todayLogs.map { it.foodName }.toSet(),
                        loggedMeals = loggedMeals,
                        // The meal is picked once, from the button tapped or the next meal of the day.
                        mealType = if (currentState.isLoaded) {
                            currentState.mealType
                        } else {
                            requestedMeal ?: fitMealOptions(loggedMeals = loggedMeals, selected = null).first()
                        }
                    )
                }
            }.collect {}
        }
    }

    override fun onMealSelected(mealType: MealType) {
        _state.update { it.copy(mealType = mealType) }
    }

    override fun onTogglePick(key: String) {
        _state.update { currentState ->
            if (key in currentState.picked) {
                currentState.copy(picked = currentState.picked - key, amounts = currentState.amounts - key)
            } else {
                // Keeps the amount shown, even a cut-down one, as later picks change what's left.
                val amount = currentState.amounts[key] ?: currentState.ideas.ideas.find { it.food.key == key }?.amount
                    ?: return@update currentState
                currentState.copy(picked = currentState.picked + key, amounts = currentState.amounts + (key to amount))
            }
        }
    }

    override fun onOpenAmount(key: String) {
        _state.update { currentState ->
            val food = currentState.foods.find { it.key == key } ?: return@update currentState
            // Starts from the amount shown on the idea, or what it was last set to.
            val amount = currentState.amounts[key]
                ?: currentState.ideas.ideas.find { it.food.key == key }?.amount
                ?: food.last.amount
            currentState.copy(amountKey = key, draftAmount = amount)
        }
    }

    override fun onDraftAmount(amount: Double) {
        if (amount <= 0) return
        _state.update { it.copy(draftAmount = amount) }
    }

    override fun onDraftStep(steps: Int) {
        _state.update { currentState ->
            val food = currentState.amountFood ?: return@update currentState
            currentState.copy(draftAmount = food.stepped(amount = currentState.draftAmount, steps = steps))
        }
    }

    override fun onPickDraft() {
        _state.update { currentState ->
            val key = currentState.amountKey ?: return@update currentState
            currentState.copy(
                picked = if (key in currentState.picked) currentState.picked else currentState.picked + key,
                amounts = currentState.amounts + (key to currentState.draftAmount)
            )
        }
    }

    override fun onLogDraft() {
        val currentState = _state.value
        val food = currentState.amountFood ?: return
        finish { logFitPicksUseCase(picks = listOf(FitPick(food = food, amount = currentState.draftAmount)), mealType = currentState.mealType) }
    }

    override fun onStepPick(key: String, steps: Int) {
        _state.update { currentState ->
            val pick = currentState.picks.find { it.food.key == key } ?: return@update currentState
            currentState.copy(amounts = currentState.amounts + (key to pick.food.stepped(amount = pick.amount, steps = steps)))
        }
    }

    override fun onLogPicks() {
        val currentState = _state.value
        finish { logFitPicksUseCase(picks = currentState.picks, mealType = currentState.mealType) }
    }

    override fun onPlanPicks() {
        val currentState = _state.value
        finish { planFitPicksUseCase(picks = currentState.picks, mealType = currentState.mealType) }
    }

    private fun finish(save: suspend () -> Unit) {
        if (_state.value.isDone) return
        _state.update { it.copy(isDone = true) }
        viewModelScope.launch { save() }
    }

    override fun onAmountBasis(basis: FitAmountBasis) = saveSettings { it.copy(amountBasis = basis) }

    override fun onTooBig(tooBig: FitTooBig) = saveSettings { it.copy(tooBig = tooBig) }

    override fun onRanking(ranking: FitRanking) = saveSettings { it.copy(ranking = ranking) }

    override fun onSpareCalories(calories: Int) = saveSettings { it.copy(spareCalories = calories) }

    override fun onToggleSource(source: FitSource) = saveSettings { settings ->
        when (source) {
            FitSource.RECIPE -> settings.copy(isRecipesOn = !settings.isRecipesOn)
            FitSource.MY_FOOD -> settings.copy(isMyFoodsOn = !settings.isMyFoodsOn)
            FitSource.RECENT -> settings.copy(isRecentOn = !settings.isRecentOn)
        }
    }

    override fun onToggleSkipEatenToday() = saveSettings { it.copy(isSkippingEatenToday = !it.isSkippingEatenToday) }

    private fun saveSettings(change: (WhatFitsSettings) -> WhatFitsSettings) {
        val settings = change(_state.value.settings)
        // Shown at once; the saved copy comes back the same.
        _state.update { it.copy(settings = settings) }
        viewModelScope.launch { setWhatFitsSettingsUseCase(settings = settings) }
    }
}
