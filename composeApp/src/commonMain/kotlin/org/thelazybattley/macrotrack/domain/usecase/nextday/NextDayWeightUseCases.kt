package org.thelazybattley.macrotrack.domain.usecase.nextday

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.DinnerItem
import org.thelazybattley.macrotrack.domain.model.MorningExplanation
import org.thelazybattley.macrotrack.domain.model.NextDaySettings
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.model.dinnerItems
import org.thelazybattley.macrotrack.domain.model.morningExplanation
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.library.GetFoodLibraryUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.features.addmeal.toRecipeMeals

/** Everything logged at dinner, every weigh-in, and the heads-up settings: all the next-day weight screens work from. */
data class NextDayInputs(
    val items: List<DinnerItem>,
    val weights: List<Weight>,
    val settings: NextDaySettings,
    /** The unit each saved or library food is stored in, by name; a saved food wins over a library food. */
    val units: Map<String, String> = emptyMap()
) {
    fun unitOf(name: String, isRecipe: Boolean): String = if (isRecipe) "" else units[name] ?: "g"
}

class GetNextDayInputsUseCase(
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getAllWeightUseCase: GetAllWeightUseCase,
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val getAllRecipeUseCase: GetAllRecipeUseCase,
    private val getFoodLibraryUseCase: GetFoodLibraryUseCase,
    private val calculateAdjustMacrosUseCase: CalculateAdjustMacrosUseCase,
    private val repository: AppFlagRepository
) {
    operator fun invoke(): Flow<NextDayInputs> = combine(
        flow = combine(
            flow = getAllFoodLogUseCase(),
            flow2 = flow { emitAll(getAllFoodUseCase()) },
            flow3 = flow { emitAll(getAllRecipeUseCase()) },
            flow4 = getFoodLibraryUseCase()
        ) { logs, foods, recipes, libraryFoods ->
            val recipeCalories = recipes
                .toRecipeMeals(foods = foods, calculateAdjustMacros = calculateAdjustMacrosUseCase)
                .associate { it.name to it.food.macros.calories }
            dinnerItems(logs = logs, recipeCalories = recipeCalories) to
                (libraryFoods.associate { it.food.name to it.food.unit } + foods.associate { it.name to it.unit })
        },
        flow2 = getAllWeightUseCase(),
        flow3 = repository.getNextDaySettings()
    ) { (items, units), weights, settings ->
        NextDayInputs(items = items, weights = weights.sortedBy { it.date }, settings = settings, units = units)
    }
}

/** Why the weigh-in on [date] jumped, when last night's dinner explains it. */
class GetMorningExplanationUseCase(private val getNextDayInputsUseCase: GetNextDayInputsUseCase) {
    suspend operator fun invoke(date: LocalDate): MorningExplanation? {
        val inputs = getNextDayInputsUseCase().first()
        return morningExplanation(date = date, items = inputs.items, weights = inputs.weights)
    }
}

class SetNextDaySettingsUseCase(private val repository: AppFlagRepository) {
    suspend operator fun invoke(settings: NextDaySettings) {
        repository.setNextDaySettings(settings = settings)
    }
}

/** Turns the heads-up while logging dinner on or off for one dinner. */
class SetNextDayHeadsUpUseCase(private val repository: AppFlagRepository) {
    suspend operator fun invoke(key: String, isShown: Boolean) {
        val settings = repository.getNextDaySettings().first()
        repository.setNextDaySettings(settings = settings.withHeadsUp(key = key, isShown = isShown))
    }
}
