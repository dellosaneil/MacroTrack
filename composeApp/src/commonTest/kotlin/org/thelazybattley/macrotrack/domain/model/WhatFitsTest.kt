package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WhatFitsTest {

    private val today = LocalDate(year = 2026, month = 10, day = 8)

    // The design's sample: 1,050 kcal, 86 g protein, 80 g carbs and 40 g fat left.
    private val left = FitMacros(calories = 1050.0, protein = 86.0, carbs = 80.0, fat = 40.0)

    private val chicken = FitFood(
        name = "Chicken breast",
        source = FitSource.MY_FOOD,
        unit = "g",
        baseAmount = 100.0,
        baseMacros = FitMacros(calories = 165.0, protein = 31.0, carbs = 0.0, fat = 3.6),
        dominantMacro = MacroType.PROTEIN,
        history = listOf(
            FitPastAmount(date = LocalDate(year = 2026, month = 10, day = 5), mealType = MealType.DINNER, amount = 180.0),
            FitPastAmount(date = LocalDate(year = 2026, month = 9, day = 29), mealType = MealType.DINNER, amount = 150.0),
            FitPastAmount(date = LocalDate(year = 2026, month = 9, day = 24), mealType = MealType.LUNCH, amount = 150.0)
        )
    )

    private val salmon = FitFood(
        name = "Salmon, rice & broccoli",
        source = FitSource.RECIPE,
        unit = "",
        baseAmount = FIT_FULL_RECIPE_PERCENT,
        baseMacros = FitMacros(calories = 575.0, protein = 38.0, carbs = 58.0, fat = 20.0),
        dominantMacro = MacroType.CARBS,
        history = listOf(FitPastAmount(date = LocalDate(year = 2026, month = 10, day = 6), mealType = MealType.DINNER, amount = 100.0))
    )

    @Test
    fun ideasStartAtTheLastAmountLogged() {
        val ideas = findFitIdeas(foods = listOf(chicken), left = left, settings = WhatFitsSettings(), mealType = MealType.DINNER, eatenToday = emptySet())
        assertEquals(expected = 180.0, actual = ideas.ideas.single().amount)
        assertEquals(expected = false, actual = ideas.ideas.single().isShrunk)
    }

    @Test
    fun usualIsTheAmountLoggedMostOften() {
        assertEquals(expected = 150.0, actual = chicken.usualAmount)
        assertEquals(expected = 2, actual = chicken.usualCount)
        val ideas = findFitIdeas(
            foods = listOf(chicken),
            left = left,
            settings = WhatFitsSettings(amountBasis = FitAmountBasis.USUAL),
            mealType = MealType.LUNCH,
            eatenToday = emptySet()
        )
        assertEquals(expected = 150.0, actual = ideas.ideas.single().amount)
    }

    @Test
    fun anAmountThatGoesOverIsCutDownToTheLargestStepThatFits() {
        // 200 kcal left, nothing kept spare: 180 g is 297 kcal, 120 g is the most that fits.
        val tight = FitMacros(calories = 200.0, protein = 20.0, carbs = 15.0, fat = 8.0)
        val idea = findFitIdeas(
            foods = listOf(chicken),
            left = tight,
            settings = WhatFitsSettings(spareCalories = 0),
            mealType = MealType.DINNER,
            eatenToday = emptySet()
        ).ideas.single()
        assertEquals(expected = 120.0, actual = idea.amount)
        assertTrue(actual = idea.isShrunk)
        assertEquals(expected = 180.0, actual = idea.startAmount)
    }

    @Test
    fun anIdeaBelowItsMinimumIsLeftOutAndCounted() {
        // Half the recipe is 288 kcal, more than the 200 left.
        val tight = FitMacros(calories = 200.0, protein = 20.0, carbs = 15.0, fat = 8.0)
        val ideas = findFitIdeas(
            foods = listOf(salmon, chicken),
            left = tight,
            settings = WhatFitsSettings(spareCalories = 0),
            mealType = MealType.DINNER,
            eatenToday = emptySet()
        )
        assertEquals(expected = listOf("Chicken breast"), actual = ideas.ideas.map { it.food.name })
        assertEquals(expected = 1, actual = ideas.hiddenCount)
    }

    @Test
    fun leaveItOutHidesWhatDoesntFitAtItsAmount() {
        val tight = FitMacros(calories = 200.0, protein = 20.0, carbs = 15.0, fat = 8.0)
        val ideas = findFitIdeas(
            foods = listOf(chicken),
            left = tight,
            settings = WhatFitsSettings(spareCalories = 0, tooBig = FitTooBig.HIDE),
            mealType = MealType.DINNER,
            eatenToday = emptySet()
        )
        assertTrue(actual = ideas.ideas.isEmpty())
        assertEquals(expected = 1, actual = ideas.hiddenCount)
    }

    @Test
    fun spareCaloriesAreKeptOutOfWhatIdeasUse() {
        // 297 kcal fits in 300, but not once 100 are kept spare.
        val ideas = findFitIdeas(
            foods = listOf(chicken),
            left = left.copy(calories = 300.0),
            settings = WhatFitsSettings(spareCalories = 100),
            mealType = MealType.DINNER,
            eatenToday = emptySet()
        )
        assertEquals(expected = 120.0, actual = ideas.ideas.single().amount)
    }

    @Test
    fun proteinFirstPutsTheBestProteinFoodFirst() {
        val ideas = findFitIdeas(foods = listOf(salmon, chicken), left = left, settings = WhatFitsSettings(), mealType = MealType.DINNER, eatenToday = emptySet())
        assertEquals(expected = "Chicken breast", actual = ideas.ideas.first().food.name)
    }

    @Test
    fun foodAlreadyEatenTodayIsSkippedUnlessTurnedOff() {
        val skipped = findFitIdeas(foods = listOf(chicken), left = left, settings = WhatFitsSettings(), mealType = MealType.DINNER, eatenToday = setOf("Chicken breast"))
        assertTrue(actual = skipped.ideas.isEmpty())
        val kept = findFitIdeas(
            foods = listOf(chicken),
            left = left,
            settings = WhatFitsSettings(isSkippingEatenToday = false),
            mealType = MealType.DINNER,
            eatenToday = setOf("Chicken breast")
        )
        assertEquals(expected = 1, actual = kept.ideas.size)
    }

    @Test
    fun aSourceTurnedOffGivesNoIdeas() {
        val ideas = findFitIdeas(
            foods = listOf(salmon, chicken),
            left = left,
            settings = WhatFitsSettings(isRecipesOn = false),
            mealType = MealType.DINNER,
            eatenToday = emptySet()
        )
        assertEquals(expected = listOf("Chicken breast"), actual = ideas.ideas.map { it.food.name })
    }

    @Test
    fun anAmountSetByHandIsUsedAsItIs() {
        val ideas = findFitIdeas(
            foods = listOf(chicken),
            left = left.copy(calories = 200.0),
            settings = WhatFitsSettings(spareCalories = 0),
            mealType = MealType.DINNER,
            eatenToday = emptySet(),
            amounts = mapOf(chicken.key to 250.0)
        )
        assertEquals(expected = 250.0, actual = ideas.ideas.single().amount)
        assertEquals(expected = false, actual = ideas.ideas.single().isShrunk)
    }

    @Test
    fun fillProteinUsesWhatsLeftToAStep() {
        // 86 g of protein at 0.31 g a gram is 277 g, so 270 g.
        assertEquals(expected = 270.0, actual = chicken.amountForProtein(protein = 86.0))
        assertNull(actual = chicken.copy(baseMacros = chicken.baseMacros.copy(protein = 0.0)).amountForProtein(protein = 86.0))
    }

    @Test
    fun ideasAreForTheMealAfterTheLastOneLogged() {
        assertEquals(expected = MealType.DINNER, actual = defaultFitMeal(loggedMeals = setOf(MealType.BREAKFAST, MealType.LUNCH)))
        assertEquals(expected = MealType.BREAKFAST, actual = defaultFitMeal(loggedMeals = emptySet()))
        assertEquals(expected = MealType.SNACK, actual = defaultFitMeal(loggedMeals = setOf(MealType.SNACK)))
    }

    @Test
    fun onlyFoodEatenAtTheMealIsSuggestedAndTheRestCounted() {
        // The salmon recipe has only been eaten at dinner.
        val ideas = findFitIdeas(foods = listOf(salmon, chicken), left = left, settings = WhatFitsSettings(), mealType = MealType.LUNCH, eatenToday = emptySet())
        assertEquals(expected = listOf("Chicken breast"), actual = ideas.ideas.map { it.food.name })
        assertEquals(expected = 1, actual = ideas.otherMealCount)
        val everyMeal = findFitIdeas(
            foods = listOf(salmon, chicken),
            left = left,
            settings = WhatFitsSettings(isMealOnly = false),
            mealType = MealType.LUNCH,
            eatenToday = emptySet()
        )
        assertEquals(expected = 2, actual = everyMeal.ideas.size)
        assertEquals(expected = 0, actual = everyMeal.otherMealCount)
    }

    @Test
    fun amountsStartFromTheLastTimeAtThatMeal() {
        // 180 g at dinner, 150 g the last time at lunch.
        val lunch = findFitIdeas(foods = listOf(chicken), left = left, settings = WhatFitsSettings(), mealType = MealType.LUNCH, eatenToday = emptySet())
        assertEquals(expected = 150.0, actual = lunch.ideas.single().amount)
        assertEquals(expected = 2, actual = chicken.timesAt(mealType = MealType.DINNER))
        // Never had at breakfast: from every meal.
        assertEquals(expected = 180.0, actual = chicken.startAmount(basis = FitAmountBasis.LAST_TIME, mealType = MealType.BREAKFAST))
    }

    @Test
    fun fitFoodsComeFromLogsNewestFirst() {
        val food = Food(
            macros = FoodMacros(protein = 31.0, carbs = 0.0, fat = 3.6, calories = 165),
            name = "Chicken breast",
            weight = 100.0,
            dominantMacro = MacroType.PROTEIN,
            unit = "g"
        )
        val recipe = Food(
            macros = FoodMacros(protein = 38.0, carbs = 58.0, fat = 20.0, calories = 600),
            name = "Salmon bowl",
            weight = 0.0,
            dominantMacro = MacroType.CARBS,
            unit = "serving"
        )
        val logs = listOf(
            log(id = 1, name = "Chicken breast", date = LocalDate(year = 2026, month = 10, day = 1), weight = 150.0, calories = 247),
            log(id = 2, name = "Chicken breast", date = LocalDate(year = 2026, month = 10, day = 5), weight = 180.0, calories = 297),
            log(id = 3, name = "Salmon bowl", date = LocalDate(year = 2026, month = 10, day = 6), weight = 0.0, calories = 300),
            log(id = 4, name = "Gone food", date = LocalDate(year = 2026, month = 10, day = 6), weight = 100.0, calories = 100),
            log(id = 5, name = "Typed in", date = LocalDate(year = 2026, month = 10, day = 6), weight = 0.0, calories = 400, isQuickAdd = true)
        )
        val fitFoods = buildFitFoods(logs = logs, foods = listOf(food), recipes = listOf(recipe), libraryFoods = emptyList(), today = today)
        assertEquals(expected = setOf("Chicken breast", "Salmon bowl"), actual = fitFoods.map { it.name }.toSet())
        val chickenFit = fitFoods.single { it.name == "Chicken breast" }
        assertEquals(expected = listOf(180.0, 150.0), actual = chickenFit.history.map { it.amount })
        // A recipe log of 300 of its 600 kcal is half a serving.
        assertEquals(expected = 50.0, actual = fitFoods.single { it.name == "Salmon bowl" }.last.amount)
    }

    @Test
    fun libraryFoodOnlyCountsWhenLoggedRecently() {
        val oats = Food(
            macros = FoodMacros(protein = 13.0, carbs = 68.0, fat = 7.0, calories = 389),
            name = "Oats",
            weight = 100.0,
            dominantMacro = MacroType.CARBS,
            unit = "g"
        )
        val old = log(id = 1, name = "Oats", date = LocalDate(year = 2026, month = 7, day = 1), weight = 50.0, calories = 195)
        assertTrue(actual = buildFitFoods(logs = listOf(old), foods = emptyList(), recipes = emptyList(), libraryFoods = listOf(oats), today = today).isEmpty())
        val recent = old.copy(date = LocalDate(year = 2026, month = 10, day = 1))
        val fitFoods = buildFitFoods(logs = listOf(recent), foods = emptyList(), recipes = emptyList(), libraryFoods = listOf(oats), today = today)
        assertEquals(expected = FitSource.RECENT, actual = fitFoods.single().source)
    }

    private fun log(id: Long, name: String, date: LocalDate, weight: Double, calories: Int, isQuickAdd: Boolean = false) = FoodLog(
        mealType = MealType.DINNER,
        calories = calories,
        protein = 0.0,
        carbs = 0.0,
        fat = 0.0,
        id = id,
        foodName = name,
        weight = weight,
        dominantMacro = MacroType.PROTEIN,
        date = date,
        isQuickAdd = isQuickAdd
    )
}
