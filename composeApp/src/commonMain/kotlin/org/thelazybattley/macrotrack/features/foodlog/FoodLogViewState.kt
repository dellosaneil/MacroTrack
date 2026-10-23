package org.thelazybattley.macrotrack.features.foodlog

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.DayActivity
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.FoodLogCopy
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlannedFood
import org.thelazybattley.macrotrack.domain.model.StepGoal
import org.thelazybattley.macrotrack.domain.model.WeeklyCheckIn

data class FoodLogViewState(
    val breakfast: FoodLogFoodListByMealType = FoodLogFoodListByMealType(
        mealType = MealType.BREAKFAST
    ),
    val lunch: FoodLogFoodListByMealType = FoodLogFoodListByMealType(
        mealType = MealType.LUNCH
    ),
    val dinner: FoodLogFoodListByMealType = FoodLogFoodListByMealType(
        mealType = MealType.DINNER
    ),
    val snack: FoodLogFoodListByMealType = FoodLogFoodListByMealType(
        mealType = MealType.SNACK
    ),
    val filteredFoodLog: List<FoodLog> = emptyList(),
    val allFoodLog: List<FoodLog> = emptyList(),
    val macroGoals: MacroGoals? = null,
    val dayGoal: DayGoal? = null,
    val dailyGoals: List<DailyGoal> = emptyList(),
    val currentGoal: Goal? = null,
    val currentMacroGoals: MacroGoals? = null,
    val totalFoodMacros: FoodMacros? = null,
    val navigateMealTypeParameter : MealType? = null,
    val availableDates: List<LocalDate> = emptyList(),
    val selectedDate: LocalDate = getCurrentDate(),
    val currentStreak: Int = 0,
    val foodUnit: Map<String, String> = emptyMap(),
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    /** Calories in one full serving of each recipe, to work out the percentage a log ate. */
    val recipeCalories: Map<String, Int> = emptyMap(),
    /** The logged entry open in the edit sheet. */
    val entryEdit: EntryEdit? = null,
    /** True when the streak has earned asking for a store rating and the user hasn't been asked lately. */
    val isReviewDue: Boolean = false,
    /** This week's check-in result, while it's due and has enough data to suggest a target. */
    val checkIn: WeeklyCheckIn.Result? = null,
    /** The copy just made, shown with Undo until it's dismissed. */
    val copyToast: FoodLogCopy? = null,
    /** Food planned for today that waits for Ate it or Skip, and what was already done with it. */
    val plannedToday: List<PlannedFood> = emptyList(),
    /** Planned food just marked eaten, shown with Undo until it's dismissed. */
    val plannedToast: PlannedToast? = null,
    /** Today's steps, walks and runs for the steps card. */
    val todayActivity: DayActivity = DayActivity(totalSteps = 0, sessions = emptyList()),
    val stepGoal: Int = StepGoal.DEFAULT,
    /** Calories burned today; null until the profile is set up. */
    val activeKcal: Int? = null,
    /** Burned calories added on top of today's budget: zero unless turned on with Pro. */
    val budgetBurn: Int = 0
)

data class FoodLogFoodListByMealType(
    val foodList: List<FoodLog> = emptyList(),
    val mealType: MealType,
    val calories: Int = 0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
)

/** Planned food in [mealType] just logged with Ate it or Ate all; [foodName] is set when it was one item. */
data class PlannedToast(
    val plannedIds: List<Long>,
    val mealType: MealType,
    val foodName: String?
)
