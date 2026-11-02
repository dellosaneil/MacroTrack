package org.thelazybattley.macrotrack.domain.usecase.whatfits

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.FitFood
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlanFood
import org.thelazybattley.macrotrack.domain.model.WhatFitsSettings
import org.thelazybattley.macrotrack.domain.model.buildFitFoods
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.InsertFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.library.GetFoodLibraryUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.PlanFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.features.addmeal.toRecipeMeals
import kotlin.math.roundToInt

/** Every food and recipe the user has logged, which is all What fits? ever suggests. */
class GetFitFoodsUseCase(
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val getAllRecipeUseCase: GetAllRecipeUseCase,
    private val getFoodLibraryUseCase: GetFoodLibraryUseCase,
    private val calculateAdjustMacrosUseCase: CalculateAdjustMacrosUseCase
) {
    operator fun invoke(): Flow<List<FitFood>> = combine(
        flow = getAllFoodLogUseCase(),
        flow2 = flow { emitAll(getAllFoodUseCase()) },
        flow3 = flow { emitAll(getAllRecipeUseCase()) },
        flow4 = getFoodLibraryUseCase()
    ) { logs, foods, recipes, libraryFoods ->
        buildFitFoods(
            logs = logs,
            foods = foods,
            recipes = recipes.toRecipeMeals(foods = foods, calculateAdjustMacros = calculateAdjustMacrosUseCase).map { it.food },
            libraryFoods = libraryFoods.map { it.food },
            today = getCurrentDate()
        )
    }
}

/** The foods and settings What fits? needs, together, for the count of ideas on Today. */
class GetWhatFitsInputsUseCase(
    private val getFitFoodsUseCase: GetFitFoodsUseCase,
    private val getWhatFitsSettingsUseCase: GetWhatFitsSettingsUseCase
) {
    operator fun invoke(): Flow<Pair<List<FitFood>, WhatFitsSettings>> =
        combine(flow = getFitFoodsUseCase(), flow2 = getWhatFitsSettingsUseCase(), transform = ::Pair)
}

class GetWhatFitsSettingsUseCase(private val repository: AppFlagRepository) {
    operator fun invoke(): Flow<WhatFitsSettings> = repository.getWhatFitsSettings()
}

class SetWhatFitsSettingsUseCase(private val repository: AppFlagRepository) {
    suspend operator fun invoke(settings: WhatFitsSettings) {
        repository.setWhatFitsSettings(settings = settings)
    }
}

/** A picked idea: [amount] of [food] in its stored unit, or a percentage of a recipe. */
data class FitPick(val food: FitFood, val amount: Double)

/** Recipes are kept with weight 0.0, which the food log uses to tell them from food. */
fun FitPick.toPlanFood(): PlanFood {
    val macros = food.macrosFor(amount = amount)
    return PlanFood(
        foodName = food.name,
        calories = macros.calories.roundToInt(),
        protein = macros.protein,
        carbs = macros.carbs,
        fat = macros.fat,
        weight = if (food.isRecipe) 0.0 else amount,
        dominantMacro = food.dominantMacro
    )
}

/** Logs [picks] to [mealType] today. */
class LogFitPicksUseCase(
    private val insertFoodLogUseCase: InsertFoodLogUseCase,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(picks: List<FitPick>, mealType: MealType) {
        val today = getCurrentDate()
        picks.map { it.toPlanFood() }.forEach { food ->
            insertFoodLogUseCase(
                foodName = food.foodName,
                mealType = mealType,
                calories = food.calories,
                fat = food.fat,
                carbs = food.carbs,
                protein = food.protein,
                weight = food.weight,
                dominantMacro = food.dominantMacro,
                date = today
            )
        }
        telemetry.logEvent(name = TelemetryEvent.WHAT_FITS_USED, params = mapOf(TelemetryEvent.PARAM_RESULT to "log"))
    }
}

/** Plans [picks] for [mealType] later today; they show under Planned on Today and count once marked eaten. */
class PlanFitPicksUseCase(
    private val planFoodUseCase: PlanFoodUseCase,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(picks: List<FitPick>, mealType: MealType) {
        val today = getCurrentDate()
        picks.forEach { pick -> planFoodUseCase(food = pick.toPlanFood(), date = today, mealType = mealType) }
        telemetry.logEvent(name = TelemetryEvent.WHAT_FITS_USED, params = mapOf(TelemetryEvent.PARAM_RESULT to "plan"))
    }
}
