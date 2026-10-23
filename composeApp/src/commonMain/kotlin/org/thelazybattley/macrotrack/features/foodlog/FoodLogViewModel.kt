package org.thelazybattley.macrotrack.features.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlanStatus
import org.thelazybattley.macrotrack.domain.model.PlannedFood
import org.thelazybattley.macrotrack.domain.model.WeeklyCheckIn
import org.thelazybattley.macrotrack.domain.model.isCheckInDue
import org.thelazybattley.macrotrack.domain.usecase.checkin.GetWeeklyCheckInUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.SaveDailyGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.CopyFoodLogsUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.DeleteFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.UndoFoodLogCopyUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.UpdateFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.library.GetFoodLibraryUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.GetPlannedTodayUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetTodayActivityUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.PlannedFoodActions
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.domain.model.PortionUnit
import org.thelazybattley.macrotrack.domain.usecase.units.GetUnitSystemUseCase
import org.thelazybattley.macrotrack.domain.usecase.review.GetReviewHistoryUseCase
import org.thelazybattley.macrotrack.domain.usecase.review.RecordReviewAskedUseCase
import org.thelazybattley.macrotrack.features.addmeal.FULL_RECIPE_PERCENT
import org.thelazybattley.macrotrack.features.addmeal.recipePercentEaten
import org.thelazybattley.macrotrack.features.addmeal.toRecipeMeals
import org.thelazybattley.macrotrack.features.calendar.LogDateRequest
import org.thelazybattley.macrotrack.features.calendar.currentStreak

class FoodLogViewModel(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val deleteFoodLogUseCase: DeleteFoodLogUseCase,
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val getAllDailyGoalsUseCase: GetAllDailyGoalsUseCase,
    private val saveDailyGoalUseCase: SaveDailyGoalUseCase,
    private val logDateRequest: LogDateRequest,
    private val getAllRecipeUseCase: GetAllRecipeUseCase,
    private val calculateAdjustMacrosUseCase: CalculateAdjustMacrosUseCase,
    private val updateFoodLogUseCase: UpdateFoodLogUseCase,
    private val getFoodLibraryUseCase: GetFoodLibraryUseCase,
    private val recordReviewAskedUseCase: RecordReviewAskedUseCase,
    getUnitSystemUseCase: GetUnitSystemUseCase,
    getReviewHistoryUseCase: GetReviewHistoryUseCase,
    getWeeklyCheckInUseCase: GetWeeklyCheckInUseCase,
    private val copyFoodLogsUseCase: CopyFoodLogsUseCase,
    private val undoFoodLogCopyUseCase: UndoFoodLogCopyUseCase,
    private val copyEvents: FoodLogCopyEvents,
    getPlannedTodayUseCase: GetPlannedTodayUseCase,
    private val plannedFoodActions: PlannedFoodActions,
    getTodayActivityUseCase: GetTodayActivityUseCase
) : ViewModel(), FoodLogCallbacks {

    private val _state = MutableStateFlow(value = FoodLogViewState())

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getAllFoodLogUseCase(),
                flow2 = getAllFoodUseCase(),
                flow3 = getAllRecipeUseCase(),
                flow4 = getFoodLibraryUseCase(),
                flow5 = getUnitSystemUseCase()
            ) { logs, food, recipes, libraryFoods, unitSystem ->
                val dates = logs.map { it.date }.toMutableSet()
                dates.add(element = getCurrentDate())
                _state.update { currentState ->
                    currentState.copy(
                        allFoodLog = logs,
                        unitSystem = unitSystem,
                        availableDates = dates.toList(),
                        currentStreak = currentStreak(loggedDates = logs.map { it.date }.toSet(), today = getCurrentDate()),
                        // Library foods are logged by name too; a saved food with the same name takes precedence.
                        foodUnit = libraryFoods.associate { it.food.name to it.food.unit } + food.associate {
                            it.name to it.unit
                        },
                        recipeCalories = recipes
                            .toRecipeMeals(foods = food, calculateAdjustMacros = calculateAdjustMacrosUseCase)
                            .associate { it.name to it.food.macros.calories }
                    )
                }.also {
                    val selectedDate = _state.value.selectedDate
                    filterFoodLogsByDate(foodLog = logs.filter { it.date == selectedDate })
                }
            }.collect()
        }
        viewModelScope.launch {
            combine(flow = getAllFoodLogUseCase(), flow2 = getReviewHistoryUseCase()) { logs, history ->
                val today = getCurrentDate()
                val streak = currentStreak(loggedDates = logs.map { it.date }.toSet(), today = today)
                history.isDue(currentStreak = streak, today = today)
            }.collect { isReviewDue ->
                _state.update { it.copy(isReviewDue = isReviewDue) }
            }
        }
        viewModelScope.launch {
            getWeeklyCheckInUseCase().collect { snapshot ->
                val isDue = snapshot != null && isCheckInDue(lastCheckIn = snapshot.lastCheckIn, today = snapshot.today)
                _state.update { it.copy(checkIn = (snapshot?.checkIn as? WeeklyCheckIn.Result)?.takeIf { isDue }) }
            }
        }
        viewModelScope.launch {
            copyEvents.latest.collect { copy -> _state.update { it.copy(copyToast = copy) } }
        }
        viewModelScope.launch {
            getPlannedTodayUseCase().collect { plannedToday ->
                _state.update { it.copy(plannedToday = plannedToday) }
            }
        }
        viewModelScope.launch {
            getTodayActivityUseCase(date = getCurrentDate()).collect { today ->
                _state.update {
                    it.copy(
                        todayActivity = today.day,
                        stepGoal = today.stepGoal,
                        activeKcal = today.activeKcal,
                        budgetBurn = today.budgetBurn
                    ).withDayGoal()
                }
            }
        }
        updateMacroGoals()
    }

    override fun onAtePlanned(planned: PlannedFood) {
        markEaten(planned = listOf(planned), mealType = planned.mealType)
    }

    override fun onAteAllPlanned(mealType: MealType) {
        markEaten(planned = _state.value.plannedToday.filter { it.mealType == mealType && it.isPending }, mealType = mealType)
    }

    private fun markEaten(planned: List<PlannedFood>, mealType: MealType) {
        if (planned.isEmpty()) return
        _state.update {
            it.copy(
                plannedToast = PlannedToast(
                    plannedIds = planned.map { item -> item.id },
                    mealType = mealType,
                    foodName = planned.singleOrNull()?.food?.foodName
                )
            )
        }
        viewModelScope.launch { plannedFoodActions.markEaten(planned = planned) }
    }

    override fun onSkipPlanned(planned: PlannedFood) {
        viewModelScope.launch { plannedFoodActions.skip(planned = planned) }
    }

    override fun onUndoPlanned(planned: PlannedFood) {
        viewModelScope.launch { plannedFoodActions.undo(planned = planned) }
    }

    override fun onUndoPlannedToast() {
        val toast = _state.value.plannedToast ?: return
        _state.update { it.copy(plannedToast = null) }
        // Read when undone, so each item carries the log Ate it made.
        val eaten = _state.value.plannedToday.filter { it.id in toast.plannedIds && it.status == PlanStatus.EATEN }
        viewModelScope.launch { eaten.forEach { plannedFoodActions.undo(planned = it) } }
    }

    override fun onDismissPlannedToast() {
        _state.update { it.copy(plannedToast = null) }
    }

    override fun onCopyMealToToday(mealType: MealType) {
        val currentState = _state.value
        val logs = currentState.allFoodLog.filter { it.date == currentState.selectedDate && it.mealType == mealType }
        if (logs.isEmpty()) return
        viewModelScope.launch {
            copyEvents.post(
                copy = copyFoodLogsUseCase(logs = logs, targetDates = listOf(getCurrentDate()), mealType = mealType, replace = false)
            )
        }
    }

    override fun onUndoCopy() {
        val copy = _state.value.copyToast ?: return
        copyEvents.clear()
        viewModelScope.launch { undoFoodLogCopyUseCase(copy = copy) }
    }

    override fun onDismissCopyToast() {
        copyEvents.clear()
    }

    private fun filterFoodLogsByDate(foodLog: List<FoodLog>) {
        val breakfastLogs = foodLog.filter { it.mealType == MealType.BREAKFAST }
        val lunchLogs = foodLog.filter { it.mealType == MealType.LUNCH }
        val dinnerLogs = foodLog.filter { it.mealType == MealType.DINNER }
        val snackLogs = foodLog.filter { it.mealType == MealType.SNACK }
        val breakfast = FoodLogFoodListByMealType(
            foodList = breakfastLogs,
            mealType = MealType.BREAKFAST,
            calories = breakfastLogs.sumOf { it.calories },
            protein = breakfastLogs.sumOf { it.protein },
            fat = breakfastLogs.sumOf { it.fat },
            carbs = breakfastLogs.sumOf { it.carbs }
        )
        val lunch = FoodLogFoodListByMealType(
            foodList = lunchLogs,
            mealType = MealType.LUNCH,
            calories = lunchLogs.sumOf { it.calories },
            protein = lunchLogs.sumOf { it.protein },
            fat = lunchLogs.sumOf { it.fat },
            carbs = lunchLogs.sumOf { it.carbs }
        )
        val dinner = FoodLogFoodListByMealType(
            foodList = dinnerLogs,
            mealType = MealType.DINNER,
            calories = dinnerLogs.sumOf { it.calories },
            protein = dinnerLogs.sumOf { it.protein },
            fat = dinnerLogs.sumOf { it.fat },
            carbs = dinnerLogs.sumOf { it.carbs }
        )
        val snack = FoodLogFoodListByMealType(
            foodList = snackLogs,
            mealType = MealType.SNACK,
            calories = snackLogs.sumOf { it.calories },
            protein = snackLogs.sumOf { it.protein },
            fat = snackLogs.sumOf { it.fat },
            carbs = snackLogs.sumOf { it.carbs }
        )
        _state.update { currentState ->
            currentState.copy(
                breakfast = breakfast,
                lunch = lunch,
                dinner = dinner,
                snack = snack,
                totalFoodMacros = FoodMacros(
                    calories = breakfast.calories + lunch.calories + dinner.calories + snack.calories,
                    protein = breakfast.protein + lunch.protein + dinner.protein + snack.protein,
                    fat = breakfast.fat + lunch.fat + dinner.fat + snack.fat,
                    carbs = breakfast.carbs + lunch.carbs + dinner.carbs + snack.carbs,
                ),
            )
        }
    }

    private fun updateMacroGoals() {
        viewModelScope.launch {
            combine(
                flow = flow { emitAll(getUserDetailsUseCase()) },
                flow2 = getAllDailyGoalsUseCase()
            ) { user, dailyGoals ->
                val today = getCurrentDate()
                val currentMacroGoals = user?.let {
                    calculateMacrosGoalUseCase(userDetails = it)
                }
                if (user != null && currentMacroGoals != null) {
                    // Record today's goal so past days keep the goal they were tracked with.
                    val todayGoal = DailyGoal(date = today, goal = user.goal, macroGoals = currentMacroGoals)
                    if (dailyGoals.find { it.date == today } != todayGoal) {
                        saveDailyGoalUseCase(dailyGoal = todayGoal)
                    }
                }
                _state.update { currentState ->
                    currentState.copy(
                        dailyGoals = dailyGoals,
                        currentGoal = user?.goal,
                        currentMacroGoals = currentMacroGoals
                    ).withDayGoal()
                }
            }.collect()
        }
    }

    private fun FoodLogViewState.withDayGoal(): FoodLogViewState {
        val dayGoal = resolveDayGoal(
            date = selectedDate,
            today = getCurrentDate(),
            dailyGoals = dailyGoals,
            currentGoal = currentGoal,
            currentMacroGoals = currentMacroGoals
        )
        // Burned calories go on top of today's budget only; the goal that's saved stays as it was.
        val burn = if (selectedDate == getCurrentDate()) budgetBurn else 0
        return copy(dayGoal = dayGoal, macroGoals = dayGoal?.macroGoals?.let { it.copy(calories = it.calories + burn) })
    }

    override fun onNavigate(mealType: MealType) {
        // Back from Add meal, Food Log shows the day that was added to rather than jumping to today.
        logDateRequest.request(date = _state.value.selectedDate)
        _state.update { currentState ->
            currentState.copy(
                navigateMealTypeParameter = mealType
            )
        }
    }

    override fun resetNavigateMealTypeParameter() {
        _state.update { currentState ->
            currentState.copy(
                navigateMealTypeParameter = null
            )
        }
    }

    override fun onDeleteFoodLog(id: Long) {
        viewModelScope.launch {
            deleteFoodLogUseCase(id = id)
        }
    }

    override fun onScreenShown() {
        onDateFilterSelected(date = logDateRequest.consume() ?: getCurrentDate())
        // Fills in today's repeating meals, and logs the ones that log themselves, on the first open of the day.
        viewModelScope.launch { plannedFoodActions.sync() }
    }

    override fun onOpenEntry(log: FoodLog) {
        _state.update { currentState ->
            // Quick adds are changed by percentage like recipes, from 100% of what was typed.
            val isRecipe = log.isRecipe || log.isQuickAdd
            val originalAmount = if (log.isQuickAdd) {
                FULL_RECIPE_PERCENT
            } else if (isRecipe) {
                currentState.recipeCalories[log.foodName]
                    ?.let { recipePercentEaten(loggedCalories = log.calories, fullCalories = it) }
                    ?: FULL_RECIPE_PERCENT
            } else {
                log.weight
            }
            val unit = if (isRecipe) "%" else currentState.foodUnit[log.foodName].orEmpty()
            currentState.copy(
                entryEdit = EntryEdit(
                    log = log,
                    originalAmount = originalAmount,
                    unit = unit,
                    portionUnit = if (isRecipe) PortionUnit(label = unit) else currentState.unitSystem.portionUnit(storedUnit = unit)
                )
            )
        }
    }

    override fun onEntryAmountChanged(amount: Double) {
        _state.update { it.copy(entryEdit = it.entryEdit?.withAmount(value = amount)) }
    }

    override fun onEntryTypingStarted() {
        _state.update { it.copy(entryEdit = it.entryEdit?.startTyping()) }
    }

    override fun onEntryKey(key: Char) {
        _state.update { it.copy(entryEdit = it.entryEdit?.typed(key = key)) }
    }

    override fun onEntryMealChanged(mealType: MealType) {
        _state.update { it.copy(entryEdit = it.entryEdit?.copy(mealType = mealType)) }
    }

    override fun onCloseEntry() {
        _state.update { it.copy(entryEdit = null) }
    }

    override fun onSaveEntry() {
        val edit = _state.value.entryEdit ?: return
        if (!edit.canSave) return
        _state.update { it.copy(entryEdit = null) }
        viewModelScope.launch {
            updateFoodLogUseCase(
                id = edit.log.id,
                mealType = edit.mealType,
                calories = edit.calories,
                protein = edit.protein,
                carbs = edit.carbs,
                fat = edit.fat,
                weight = edit.weight
            )
        }
    }

    override fun onDeleteEntry() {
        val edit = _state.value.entryEdit ?: return
        _state.update { it.copy(entryEdit = null) }
        onDeleteFoodLog(id = edit.log.id)
    }

    override fun onDateFilterSelected(date: LocalDate) {
        _state.update { currentState ->
            val filteredLogs = currentState.allFoodLog.filter { it.date == date }
            filterFoodLogsByDate(foodLog = filteredLogs)
            currentState.copy(
                selectedDate = date,
            ).withDayGoal()
        }
    }

    override fun onReviewAsked() {
        _state.update { it.copy(isReviewDue = false) }
        viewModelScope.launch {
            recordReviewAskedUseCase(date = getCurrentDate())
        }
    }
}
