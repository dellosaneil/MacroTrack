package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.domain.MacroGoals
import kotlin.math.floor
import kotlin.math.roundToInt

/** Food logged within this many days counts as recent food; saved foods and recipes count whenever they were logged. */
const val WHAT_FITS_RECENT_DAYS = 60

/** At most this many ideas are shown, best first. */
const val WHAT_FITS_MAX_IDEAS = 10

/** A recipe portion as a percentage of one full serving, the way recipe logs keep it. */
const val FIT_FULL_RECIPE_PERCENT = 100.0

/** Calories and macros, as Doubles so portions can be added up before rounding. */
data class FitMacros(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
) {
    operator fun plus(other: FitMacros) = FitMacros(
        calories = calories + other.calories,
        protein = protein + other.protein,
        carbs = carbs + other.carbs,
        fat = fat + other.fat
    )

    operator fun minus(other: FitMacros) = FitMacros(
        calories = calories - other.calories,
        protein = protein - other.protein,
        carbs = carbs - other.carbs,
        fat = fat - other.fat
    )

    operator fun times(factor: Double) = FitMacros(
        calories = calories * factor,
        protein = protein * factor,
        carbs = carbs * factor,
        fat = fat * factor
    )

    operator fun get(macro: MacroType): Double = when (macro) {
        MacroType.PROTEIN -> protein
        MacroType.CARBS -> carbs
        MacroType.FAT -> fat
    }
}

fun Iterable<FitMacros>.sum(): FitMacros = fold(initial = FitMacros()) { total, macros -> total + macros }

fun MacroGoals.toFitMacros() = FitMacros(
    calories = calories.toDouble(),
    protein = protein.toDouble(),
    carbs = carbs.toDouble(),
    fat = fat.toDouble()
)

fun FoodMacros.toFitMacros() = FitMacros(calories = calories.toDouble(), protein = protein, carbs = carbs, fat = fat)

fun FoodLog.toFitMacros() = FitMacros(calories = calories.toDouble(), protein = protein, carbs = carbs, fat = fat)

/** The meal ideas are for unless another is picked: the first one after the last meal logged today, or Snack. */
fun defaultFitMeal(loggedMeals: Set<MealType>): MealType {
    val lastLogged = MealType.entries.lastOrNull { it in loggedMeals }
    return MealType.entries.firstOrNull { lastLogged == null || it > lastLogged } ?: MealType.SNACK
}

/** Where an idea comes from, and the switch in settings that turns it off. */
enum class FitSource { RECIPE, MY_FOOD, RECENT }

/** What amount each idea starts at. */
enum class FitAmountBasis { LAST_TIME, USUAL, SERVING }

/** What happens to an idea whose starting amount would go over what's left. */
enum class FitTooBig { SHRINK, HIDE }

/** Which ideas go first. */
enum class FitRanking { PROTEIN, CALORIES, FEWEST_CARBS }

@Serializable
data class WhatFitsSettings(
    val amountBasis: FitAmountBasis = FitAmountBasis.LAST_TIME,
    val tooBig: FitTooBig = FitTooBig.SHRINK,
    val ranking: FitRanking = FitRanking.PROTEIN,
    /** Calories kept out of what the ideas may use. */
    val spareCalories: Int = 100,
    val isRecipesOn: Boolean = true,
    val isMyFoodsOn: Boolean = true,
    val isRecentOn: Boolean = true,
    /** Leaves out food already logged today, so ideas aren't a repeat of lunch. */
    val isSkippingEatenToday: Boolean = true,
    /** Only food eaten at the meal ideas are for: breakfast ideas come from breakfasts, and so on. */
    val isMealOnly: Boolean = true
) {
    fun isOn(source: FitSource): Boolean = when (source) {
        FitSource.RECIPE -> isRecipesOn
        FitSource.MY_FOOD -> isMyFoodsOn
        FitSource.RECENT -> isRecentOn
    }

    companion object {
        val SPARE_OPTIONS = listOf(0, 50, 100, 200)
    }
}

/** One time a food was logged: [amount] is in the food's stored unit, or a percentage of a recipe. */
data class FitPastAmount(
    val date: LocalDate,
    val mealType: MealType,
    val amount: Double
)

/**
 * A food or recipe the user has logged. [baseMacros] are for [baseAmount] (the saved serving, or 100% of a recipe),
 * and [history] holds every log of it, newest first, so it is never empty.
 */
data class FitFood(
    val name: String,
    val source: FitSource,
    /** The stored unit, like "g"; empty for a recipe, whose amounts are percentages of a serving. */
    val unit: String,
    val baseAmount: Double,
    val baseMacros: FitMacros,
    val dominantMacro: MacroType,
    val history: List<FitPastAmount>
) {
    val isRecipe: Boolean get() = source == FitSource.RECIPE

    val key: String get() = (if (isRecipe) "recipe:" else "food:") + name

    val last: FitPastAmount get() = history.first()

    /** The amount logged most often; a tie goes to the one logged most recently. */
    val usualAmount: Double get() = usualOf(logs = history)

    val usualCount: Int get() = history.count { it.amount == usualAmount }

    /** Its logs at [mealType], newest first. */
    fun historyAt(mealType: MealType): List<FitPastAmount> = history.filter { it.mealType == mealType }

    /** How many times it was logged at [mealType]. */
    fun timesAt(mealType: MealType): Int = history.count { it.mealType == mealType }

    /** The last time it was logged at [mealType], or at any meal when never at that one. */
    fun lastFor(mealType: MealType): FitPastAmount = historyAt(mealType = mealType).firstOrNull() ?: last

    /** The usual amount at [mealType], or at any meal when never at that one. */
    fun usualFor(mealType: MealType): Double = usualOf(logs = historyAt(mealType = mealType).ifEmpty { history })

    /** How many of [usualFor]'s logs had that amount, out of how many. */
    fun usualCountFor(mealType: MealType): Pair<Int, Int> {
        val logs = historyAt(mealType = mealType).ifEmpty { history }
        val usual = usualOf(logs = logs)
        return logs.count { it.amount == usual } to logs.size
    }

    private fun usualOf(logs: List<FitPastAmount>): Double = logs.groupBy { it.amount }.maxWith(
        comparator = compareBy<Map.Entry<Double, List<FitPastAmount>>> { it.value.size }
            .thenByDescending { entry -> logs.indexOfFirst { it.amount == entry.key } }
    ).key

    val servingAmount: Double get() = if (isRecipe) FIT_FULL_RECIPE_PERCENT else baseAmount

    private val isWeighed: Boolean get() = unit.lowercase() in setOf("g", "ml")

    /** How far − and + move the amount: 10 g or ml, half a recipe serving, or half of anything else. */
    val step: Double
        get() = when {
            isRecipe -> FIT_FULL_RECIPE_PERCENT / 2
            isWeighed -> 10.0
            else -> 0.5
        }

    /** The smallest amount worth suggesting: 50 g or ml, or half a serving. */
    val minimum: Double
        get() = when {
            isRecipe -> FIT_FULL_RECIPE_PERCENT / 2
            isWeighed -> 50.0
            else -> baseAmount / 2
        }

    fun macrosFor(amount: Double): FitMacros = if (baseAmount > 0) baseMacros * (amount / baseAmount) else FitMacros()

    /** Where an idea at [mealType] starts: from that meal's logs when there are any, otherwise from every log. */
    fun startAmount(basis: FitAmountBasis, mealType: MealType): Double = when (basis) {
        FitAmountBasis.LAST_TIME -> lastFor(mealType = mealType).amount
        FitAmountBasis.USUAL -> usualFor(mealType = mealType)
        FitAmountBasis.SERVING -> servingAmount
    }

    /** The logged amounts, each once, newest first: the last [count] different ones. */
    fun recentAmounts(count: Int): List<FitPastAmount> = history.distinctBy { it.amount }.take(n = count)

    /** Whether it was logged in [mealType] before. */
    fun isUsualIn(mealType: MealType): Boolean = history.any { it.mealType == mealType }

    /** The largest step-sized amount whose calories fit in [calories]; null when even [minimum] doesn't. */
    fun largestAmountWithin(calories: Double): Double? {
        val perUnit = if (baseAmount > 0) baseMacros.calories / baseAmount else 0.0
        if (perUnit <= 0) return null
        val amount = floor(calories / perUnit / step) * step
        return roundToTenth(value = amount).takeIf { it >= minimum && it > 0 }
    }

    /** The amount that uses up [protein] grams, to a step; null for food without protein. */
    fun amountForProtein(protein: Double): Double? {
        val perUnit = if (baseAmount > 0) baseMacros.protein / baseAmount else 0.0
        if (perUnit <= 0 || protein <= 0) return null
        return roundToTenth(value = floor(protein / perUnit / step) * step).takeIf { it > 0 }
    }

    /** One step either way from [amount], never below one step. */
    fun stepped(amount: Double, steps: Int): Double = roundToTenth(value = (amount + step * steps).coerceAtLeast(minimumValue = step))
}

/** An idea at [amount]: [startAmount] from the chosen basis, cut down to fit when [isShrunk]. */
data class FitIdea(
    val food: FitFood,
    val amount: Double,
    val startAmount: Double,
    val isShrunk: Boolean,
    val macros: FitMacros,
    /** The share of what's left of each macro it uses, 0 to 1. */
    val shares: Map<MacroType, Double>,
    /** The macros it would go over. */
    val over: Set<MacroType>
)

/** The ideas, best first, how many didn't fit at all, and how many were left out as never eaten at that meal. */
data class FitIdeas(
    val ideas: List<FitIdea>,
    val hiddenCount: Int,
    val otherMealCount: Int = 0
)

/**
 * The foods that fit in [left] once the settings' spare calories are kept back, best first by the settings' ranking.
 * [amounts] holds amounts the user set by hand, which are used as they are. With the meal filter on, only food logged
 * at [mealType] before is suggested; with it off, that food still ranks a little higher.
 */
fun findFitIdeas(
    foods: List<FitFood>,
    left: FitMacros,
    settings: WhatFitsSettings,
    mealType: MealType,
    eatenToday: Set<String>,
    amounts: Map<String, Double> = emptyMap(),
    limit: Int = WHAT_FITS_MAX_IDEAS
): FitIdeas {
    val budget = left.calories - settings.spareCalories
    var hiddenCount = 0
    var otherMealCount = 0
    val scored = foods
        .filter { settings.isOn(source = it.source) }
        .filterNot { settings.isSkippingEatenToday && it.name in eatenToday && it.key !in amounts }
        .mapNotNull { food ->
            if (settings.isMealOnly && !food.isUsualIn(mealType = mealType) && food.key !in amounts) {
                otherMealCount++
                return@mapNotNull null
            }
            val start = food.startAmount(basis = settings.amountBasis, mealType = mealType)
            val setByHand = amounts[food.key]
            var amount = setByHand ?: start
            var isShrunk = false
            if (setByHand == null && food.macrosFor(amount = amount).calories > budget) {
                val smaller = if (settings.tooBig == FitTooBig.SHRINK) food.largestAmountWithin(calories = budget) else null
                if (smaller == null) {
                    hiddenCount++
                    return@mapNotNull null
                }
                amount = smaller
                isShrunk = true
            }
            val macros = food.macrosFor(amount = amount)
            val shares = MacroType.entries.associateWith { macro ->
                if (left[macro] > 0) (macros[macro] / left[macro]).coerceIn(minimumValue = 0.0, maximumValue = 1.0) else 1.0
            }
            val over = MacroType.entries.filter { macros[it] > left[it].coerceAtLeast(minimumValue = 0.0) + 0.5 }.toSet()
            val idea = FitIdea(
                food = food,
                amount = amount,
                startAmount = start,
                isShrunk = isShrunk,
                macros = macros,
                shares = shares,
                over = over
            )
            idea to score(idea = idea, left = left, budget = budget, ranking = settings.ranking) +
                if (food.isUsualIn(mealType = mealType)) MEAL_BONUS else 0.0
        }
    return FitIdeas(
        ideas = scored.sortedByDescending { it.second }.take(n = limit).map { it.first },
        hiddenCount = hiddenCount,
        otherMealCount = otherMealCount
    )
}

private const val MEAL_BONUS = 0.3

private fun score(idea: FitIdea, left: FitMacros, budget: Double, ranking: FitRanking): Double {
    val protein = idea.shares.getValue(MacroType.PROTEIN)
    val calories = if (budget > 0) idea.macros.calories / budget else 0.0
    return when (ranking) {
        FitRanking.CALORIES -> calories
        FitRanking.FEWEST_CARBS -> 1 - idea.macros.carbs / left.carbs.coerceAtLeast(minimumValue = 1.0) + protein
        FitRanking.PROTEIN -> 2 * protein + 0.5 * calories -
            (if (MacroType.CARBS in idea.over) 0.5 else 0.0) -
            (if (MacroType.FAT in idea.over) 0.5 else 0.0)
    }
}

/**
 * Every saved food, recipe and recent library food the user has logged, with its logs. Logs only keep a name, so a
 * saved food takes precedence over a library food with the same name; quick adds and food that's been deleted are
 * left out. [recipes] are full servings.
 */
fun buildFitFoods(
    logs: List<FoodLog>,
    foods: List<Food>,
    recipes: List<Food>,
    libraryFoods: List<Food>,
    today: LocalDate
): List<FitFood> {
    val foodsByName = foods.associateBy { it.name }
    val recipesByName = recipes.associateBy { it.name }
    val libraryByName = libraryFoods.associateBy { it.name }
    val recentFrom = today.minus(period = DatePeriod(days = WHAT_FITS_RECENT_DAYS))
    return logs
        .filterNot { it.isQuickAdd }
        .groupBy { (if (it.isRecipe) "recipe:" else "food:") + it.foodName }
        .mapNotNull { (_, group) ->
            val newestFirst = group.sortedWith(compareByDescending<FoodLog> { it.date }.thenByDescending { it.id })
            val log = newestFirst.first()
            val (source, base) = when {
                log.isRecipe -> FitSource.RECIPE to (recipesByName[log.foodName] ?: return@mapNotNull null)
                log.foodName in foodsByName -> FitSource.MY_FOOD to foodsByName.getValue(log.foodName)
                log.foodName in libraryByName && log.date >= recentFrom -> FitSource.RECENT to libraryByName.getValue(log.foodName)
                else -> return@mapNotNull null
            }
            val isRecipe = source == FitSource.RECIPE
            val history = newestFirst.mapNotNull { entry ->
                val amount = if (isRecipe) {
                    if (base.macros.calories > 0) (entry.calories * FIT_FULL_RECIPE_PERCENT / base.macros.calories).roundToInt().toDouble() else FIT_FULL_RECIPE_PERCENT
                } else {
                    entry.weight
                }
                FitPastAmount(date = entry.date, mealType = entry.mealType, amount = amount).takeIf { amount > 0 }
            }
            if (history.isEmpty()) return@mapNotNull null
            FitFood(
                name = log.foodName,
                source = source,
                unit = if (isRecipe) "" else base.unit,
                baseAmount = if (isRecipe) FIT_FULL_RECIPE_PERCENT else base.weight,
                baseMacros = FitMacros(
                    calories = base.macros.calories.toDouble(),
                    protein = base.macros.protein,
                    carbs = base.macros.carbs,
                    fat = base.macros.fat
                ),
                dominantMacro = base.dominantMacro,
                history = history
            )
        }
}

private fun roundToTenth(value: Double): Double = (value * 10).roundToInt() / 10.0
