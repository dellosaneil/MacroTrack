package org.thelazybattley.macrotrack.features.mealplan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlanFood
import org.thelazybattley.macrotrack.domain.model.PlanStatus
import org.thelazybattley.macrotrack.domain.model.RepeatingMeal
import org.thelazybattley.macrotrack.domain.model.toPlanFood
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.library.GetFoodLibraryUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.GetPlannedFoodsUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.GetRepeatingMealsUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.SaveRepeatingMealUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.SetRepeatingMealActiveUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.features.addmeal.toRecipeMeals
import org.thelazybattley.macrotrack.features.copy.toggled
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.MEAL_TYPE
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.REPEATING_MEAL_ID
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.SOURCE_DATE

enum class RepeatEnd { NEVER, ON_DATE, AFTER_TIMES }

/** The day patterns offered above the week row; anything else is custom. */
enum class DayPattern(val days: Set<DayOfWeek>) {
    EVERY_DAY(days = RepeatingMeal.EVERY_DAY),
    WEEKDAYS(days = RepeatingMeal.WEEKDAYS),
    WEEKENDS(days = RepeatingMeal.WEEKENDS);

    companion object {
        fun of(days: Set<DayOfWeek>): DayPattern? = entries.find { it.days == days }
    }
}

/** Where the Add food sheet picks from. */
enum class FoodSource { RECENT, MY_FOODS, RECIPES }

/**
 * Setting up a repeating meal from a logged meal ([sourceDate]), from scratch, or changing [existing]. From a logged
 * meal every food starts picked and [unchecked] holds the indexes of the ones left out; otherwise [foods] is exactly
 * what's added each time, picked from [recentFoods], [myFoods] and [recipes].
 */
data class RepeatMealViewState(
    val today: LocalDate,
    val existing: RepeatingMeal? = null,
    val sourceDate: LocalDate? = null,
    val foods: List<PlanFood> = emptyList(),
    val unchecked: Set<Int> = emptySet(),
    val name: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val days: Set<DayOfWeek> = RepeatingMeal.WEEKDAYS,
    val end: RepeatEnd = RepeatEnd.NEVER,
    val endDate: LocalDate = today.plus(period = DatePeriod(days = DEFAULT_END_DAYS)),
    val times: Int = DEFAULT_TIMES,
    val isAutoLog: Boolean = false,
    /** Food logged lately, newest first, each once, at the amount last logged. */
    val recentFoods: List<PlanFood> = emptyList(),
    /** One serving of each saved food, A to Z. */
    val myFoods: List<PlanFood> = emptyList(),
    /** Each recipe as one full serving, A to Z. */
    val recipes: List<PlanFood> = emptyList(),
    /** How many days [existing] has been eaten on. */
    val eatenTimes: Int = 0,
    val foodUnit: Map<String, String> = emptyMap(),
    val isLoaded: Boolean = false,
    val isDone: Boolean = false
) {
    val isEditing: Boolean get() = existing != null

    /** Started from a logged meal: its food is ticked or left out rather than added and removed. */
    val isFromMeal: Boolean get() = existing == null && sourceDate != null

    val selectedFoods: List<PlanFood>
        get() = if (isFromMeal) foods.filterIndexed { index, _ -> index !in unchecked } else foods

    fun isAdded(food: PlanFood): Boolean = foods.any { it.foodName == food.foodName }

    /** The Add food sheet's rows: [source], or, while searching, everything saved that matches [query]. */
    fun pickerFoods(source: FoodSource, query: String): List<PlanFood> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return when (source) {
                FoodSource.RECENT -> recentFoods
                FoodSource.MY_FOODS -> myFoods
                FoodSource.RECIPES -> recipes
            }
        }
        return (recentFoods + myFoods + recipes)
            .distinctBy { it.foodName }
            .filter { it.foodName.contains(other = trimmed, ignoreCase = true) }
    }

    val selectedCalories: Int get() = selectedFoods.sumOf { it.calories }

    val pattern: DayPattern? get() = DayPattern.of(days = days)

    /** What would be saved; a new one starts tomorrow, since the meal it comes from was usually eaten today. */
    val draft: RepeatingMeal
        get() = RepeatingMeal(
            id = existing?.id ?: 0,
            name = name.trim(),
            mealType = mealType,
            days = days,
            foods = selectedFoods,
            startDate = existing?.startDate ?: today.plus(period = DatePeriod(days = 1)),
            endDate = endDate.takeIf { end == RepeatEnd.ON_DATE },
            maxTimes = times.takeIf { end == RepeatEnd.AFTER_TIMES },
            isAutoLog = isAutoLog,
            isActive = existing?.isActive ?: true,
            updatedOn = today
        )

    /** The next few days it would run on. */
    val upcoming: List<LocalDate> get() = draft.copy(isActive = true).nextDates(from = today, count = PREVIEW_DAYS)

    val canSave: Boolean get() = selectedFoods.isNotEmpty() && days.isNotEmpty()

    companion object {
        const val DEFAULT_TIMES = 10
        const val MAX_TIMES = 99
        const val PREVIEW_DAYS = 4
        private const val DEFAULT_END_DAYS = 28
        const val RECENT_LIMIT = 20
    }
}

interface RepeatMealCallbacks {
    fun onNameChanged(name: String)
    fun onFoodToggled(index: Int)
    fun onMealSelected(mealType: MealType)
    fun onPatternSelected(pattern: DayPattern?)
    fun onDayToggled(day: DayOfWeek)
    fun onEndSelected(end: RepeatEnd)
    fun onEndDatePicked(date: LocalDate)
    fun onTimesChanged(times: Int)
    fun onAutoLogSelected(isAutoLog: Boolean)

    /** Adds [food] from the Add food sheet, or takes it out when it's already added. */
    fun onFoodPicked(food: PlanFood)
    fun onFoodRemoved(index: Int)

    /** Saves, named [suggestedName] when the name was left empty. */
    fun onSave(suggestedName: String)

    /** Pauses [RepeatMealViewState.existing], offered instead of deleting it. */
    fun onPause()

    companion object {
        fun default() = object : RepeatMealCallbacks {
            override fun onNameChanged(name: String) = Unit
            override fun onFoodToggled(index: Int) = Unit
            override fun onMealSelected(mealType: MealType) = Unit
            override fun onPatternSelected(pattern: DayPattern?) = Unit
            override fun onDayToggled(day: DayOfWeek) = Unit
            override fun onEndSelected(end: RepeatEnd) = Unit
            override fun onEndDatePicked(date: LocalDate) = Unit
            override fun onTimesChanged(times: Int) = Unit
            override fun onAutoLogSelected(isAutoLog: Boolean) = Unit
            override fun onFoodPicked(food: PlanFood) = Unit
            override fun onFoodRemoved(index: Int) = Unit
            override fun onSave(suggestedName: String) = Unit
            override fun onPause() = Unit
        }
    }
}

class RepeatMealViewModel(
    getAllFoodLogUseCase: GetAllFoodLogUseCase,
    getRepeatingMealsUseCase: GetRepeatingMealsUseCase,
    getAllFoodUseCase: GetAllFoodUseCase,
    getFoodLibraryUseCase: GetFoodLibraryUseCase,
    getAllRecipeUseCase: GetAllRecipeUseCase,
    getPlannedFoodsUseCase: GetPlannedFoodsUseCase,
    calculateAdjustMacrosUseCase: CalculateAdjustMacrosUseCase,
    private val saveRepeatingMealUseCase: SaveRepeatingMealUseCase,
    private val setRepeatingMealActiveUseCase: SetRepeatingMealActiveUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), RepeatMealCallbacks {

    private val _state = MutableStateFlow(value = RepeatMealViewState(today = getCurrentDate()))
    val state = _state.asStateFlow()

    private var isSaving = false

    init {
        val repeatingMealId = savedStateHandle.get<String>(key = REPEATING_MEAL_ID)?.toLongOrNull()
        val sourceDate = savedStateHandle.get<String>(key = SOURCE_DATE)?.let { runCatching { LocalDate.parse(input = it) }.getOrNull() }
        val sourceMeal = MealType.entries.find { it.name == savedStateHandle.get<String>(key = MEAL_TYPE) }
        viewModelScope.launch {
            val existing = repeatingMealId?.let { id -> getRepeatingMealsUseCase().first().find { it.id == id } }
            val logs = getAllFoodLogUseCase().first()
            val eatenTimes = existing?.let { meal ->
                getPlannedFoodsUseCase().first()
                    .filter { it.repeatingMealId == meal.id && it.status == PlanStatus.EATEN }
                    .distinctBy { it.date }
                    .size
            } ?: 0
            _state.update { currentState ->
                when {
                    existing != null -> currentState.copy(
                        existing = existing,
                        foods = existing.foods,
                        name = existing.name,
                        mealType = existing.mealType,
                        days = existing.days,
                        end = when {
                            existing.endDate != null -> RepeatEnd.ON_DATE
                            existing.maxTimes != null -> RepeatEnd.AFTER_TIMES
                            else -> RepeatEnd.NEVER
                        },
                        endDate = existing.endDate ?: currentState.endDate,
                        times = existing.maxTimes ?: currentState.times,
                        isAutoLog = existing.isAutoLog,
                        eatenTimes = eatenTimes
                    )
                    sourceDate != null && sourceMeal != null -> currentState.withSource(
                        date = sourceDate,
                        mealType = sourceMeal,
                        logs = logs.filter { it.date == sourceDate && it.mealType == sourceMeal }
                    )
                    else -> currentState
                }.copy(isLoaded = true, recentFoods = recentFoods(logs = logs))
            }
        }
        viewModelScope.launch {
            combine(flow = getAllFoodUseCase(), flow2 = getFoodLibraryUseCase()) { foods, libraryFoods ->
                libraryFoods.associate { it.food.name to it.food.unit } + foods.associate { it.name to it.unit }
            }.collect { foodUnit ->
                _state.update { it.copy(foodUnit = foodUnit) }
            }
        }
        viewModelScope.launch {
            combine(flow = getAllFoodUseCase(), flow2 = getAllRecipeUseCase()) { foods, recipes ->
                foods to recipes.toRecipeMeals(foods = foods, calculateAdjustMacros = calculateAdjustMacrosUseCase)
            }.collect { (foods, recipeMeals) ->
                _state.update { currentState ->
                    currentState.copy(
                        myFoods = foods.sortedBy { it.name.lowercase() }.map { it.toPlanFood() },
                        recipes = recipeMeals.sortedBy { it.name.lowercase() }.map { it.food.toPlanFood() }
                    )
                }
            }
        }
    }

    override fun onNameChanged(name: String) {
        _state.update { it.copy(name = name) }
    }

    override fun onFoodToggled(index: Int) {
        _state.update { it.copy(unchecked = it.unchecked.toggled(element = index)) }
    }

    override fun onMealSelected(mealType: MealType) {
        _state.update { it.copy(mealType = mealType) }
    }

    override fun onPatternSelected(pattern: DayPattern?) {
        // Custom starts from Mon, Wed, Fri, a common pick that's none of the patterns.
        _state.update { it.copy(days = pattern?.days ?: CUSTOM_DAYS) }
    }

    override fun onDayToggled(day: DayOfWeek) {
        _state.update { it.copy(days = it.days.toggled(element = day)) }
    }

    override fun onEndSelected(end: RepeatEnd) {
        _state.update { it.copy(end = end) }
    }

    override fun onEndDatePicked(date: LocalDate) {
        _state.update { it.copy(end = RepeatEnd.ON_DATE, endDate = maxOf(date, it.today.plus(period = DatePeriod(days = 1)))) }
    }

    override fun onTimesChanged(times: Int) {
        _state.update { it.copy(times = times.coerceIn(minimumValue = 1, maximumValue = RepeatMealViewState.MAX_TIMES)) }
    }

    override fun onAutoLogSelected(isAutoLog: Boolean) {
        _state.update { it.copy(isAutoLog = isAutoLog) }
    }

    override fun onFoodPicked(food: PlanFood) {
        _state.update { currentState ->
            currentState.copy(
                foods = if (currentState.isAdded(food = food)) {
                    currentState.foods.filterNot { it.foodName == food.foodName }
                } else {
                    currentState.foods + food
                }
            )
        }
    }

    override fun onFoodRemoved(index: Int) {
        _state.update { it.copy(foods = it.foods.filterIndexed { i, _ -> i != index }) }
    }

    override fun onSave(suggestedName: String) {
        val currentState = _state.value
        if (!currentState.canSave || isSaving) return
        isSaving = true
        val draft = currentState.draft
        viewModelScope.launch {
            saveRepeatingMealUseCase(repeatingMeal = draft.copy(name = draft.name.ifEmpty { suggestedName }))
            _state.update { it.copy(isDone = true) }
        }
    }

    override fun onPause() {
        val existing = _state.value.existing ?: return
        if (isSaving) return
        isSaving = true
        viewModelScope.launch {
            setRepeatingMealActiveUseCase(id = existing.id, isActive = false)
            _state.update { it.copy(isDone = true) }
        }
    }

    private fun RepeatMealViewState.withSource(date: LocalDate, mealType: MealType, logs: List<FoodLog>) = copy(
        sourceDate = date,
        foods = logs.map { it.toPlanFood() },
        unchecked = emptySet(),
        mealType = mealType
    )

    companion object {
        private val CUSTOM_DAYS = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)

        /** Each food logged, newest first, at the amount it was last logged. Quick adds are left out. */
        internal fun recentFoods(logs: List<FoodLog>): List<PlanFood> =
            logs.filter { !it.isQuickAdd }
                .sortedWith(compareByDescending<FoodLog> { it.date }.thenByDescending { it.loggedAtMillis ?: it.id })
                .distinctBy { it.foodName }
                .take(n = RepeatMealViewState.RECENT_LIMIT)
                .map { it.toPlanFood() }
    }
}
