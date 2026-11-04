package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NextDayWeightTest {

    private val today = LocalDate(year = 2026, month = 10, day = 8)

    private fun daysAgo(days: Int): LocalDate = today.minus(period = DatePeriod(days = days))

    private fun dinner(daysAgo: Int, name: String = "Carbonara", isRecipe: Boolean = true, amount: Double = 70.0) = DinnerEntry(
        name = name,
        isRecipe = isRecipe,
        date = daysAgo(days = daysAgo),
        amount = amount,
        calories = (920 * amount / 100).toInt(),
        carbs = 96 * amount / 100
    )

    /** A weigh-in of [before] on the dinner's day and [after] the next morning. */
    private fun night(daysAgo: Int, before: Double, after: Double): List<Weight> {
        val date = daysAgo(days = daysAgo)
        return listOf(Weight(date = date, weight = before), Weight(date = date.plus(period = DatePeriod(days = 1)), weight = after))
    }

    private fun assertClose(expected: Double, actual: Double?) {
        assertNotNull(actual)
        assertTrue(abs(expected - actual) < 1e-9, "expected $expected, was $actual")
    }

    @Test
    fun aDinnerOnlyCountsWithAWeighInThatMorningAndTheNext() {
        val entries = listOf(dinner(daysAgo = 10), dinner(daysAgo = 20), dinner(daysAgo = 30))
        val weights = night(daysAgo = 10, before = 75.0, after = 76.0) + Weight(date = daysAgo(days = 20), weight = 75.0)
        val food = nextDayOverview(entries = entries, weights = weights, today = today, days = 90).foods.single()
        assertEquals(expected = 3, actual = food.nights.size)
        assertEquals(expected = 1, actual = food.counted.size)
        assertFalse(food.isReady)
        assertNull(food.average)
        assertNull(food.estimate(amount = 70.0))
    }

    @Test
    fun aFoodNeedsThreeCountedDinners() {
        val entries = listOf(dinner(daysAgo = 2), dinner(daysAgo = 9), dinner(daysAgo = 16))
        val weights = night(daysAgo = 2, before = 75.0, after = 76.0) +
            night(daysAgo = 9, before = 75.0, after = 75.9) +
            night(daysAgo = 16, before = 75.0, after = 76.1)
        val food = nextDayOverview(entries = entries, weights = weights, today = today, days = 90).foods.single()
        assertTrue(food.isReady)
        assertClose(expected = 1.0, actual = food.average)
        assertEquals(expected = 70.0, actual = food.usual)
    }

    @Test
    fun todaysDinnerAndOnesBeforeThePeriodAreLeftOut() {
        val entries = listOf(dinner(daysAgo = 0), dinner(daysAgo = 5), dinner(daysAgo = 40))
        val overview = nextDayOverview(entries = entries, weights = emptyList(), today = today, days = 30)
        assertEquals(expected = listOf(daysAgo(days = 5)), actual = overview.foods.single().nights.map { it.date })
    }

    @Test
    fun theSameFoodTwiceAtOneDinnerIsOneNight() {
        val entries = listOf(dinner(daysAgo = 3, amount = 50.0), dinner(daysAgo = 3, amount = 20.0))
        val food = nextDayOverview(entries = entries, weights = emptyList(), today = today, days = 90).foods.single()
        assertEquals(expected = 1, actual = food.nights.size)
        assertEquals(expected = 70.0, actual = food.nights.single().amount)
    }

    @Test
    fun aRecipeAndAFoodWithTheSameNameStayApart() {
        val entries = listOf(dinner(daysAgo = 3), dinner(daysAgo = 3, isRecipe = false, amount = 200.0))
        val overview = nextDayOverview(entries = entries, weights = emptyList(), today = today, days = 90)
        assertEquals(expected = 2, actual = overview.foods.size)
    }

    @Test
    fun anAmountEatenBeforeUsesThoseDinners() {
        val entries = listOf(dinner(daysAgo = 2), dinner(daysAgo = 9), dinner(daysAgo = 16), dinner(daysAgo = 23, amount = 100.0))
        val weights = night(daysAgo = 2, before = 75.0, after = 76.0) +
            night(daysAgo = 9, before = 75.0, after = 76.0) +
            night(daysAgo = 16, before = 75.0, after = 76.0) +
            night(daysAgo = 23, before = 75.0, after = 76.4)
        val food = nextDayOverview(entries = entries, weights = weights, today = today, days = 90).foods.single()
        assertClose(expected = 1.0, actual = food.estimate(amount = 70.0))
        assertClose(expected = 1.4, actual = food.estimate(amount = 100.0))
        assertEquals(expected = listOf(70.0, 100.0), actual = food.portions.map { it.amount })
    }

    @Test
    fun otherAmountsFollowALineThroughTheDinners() {
        // +0.6 at 50%, +1.0 at 70% and +1.4 at 90%: 0.02 kg for every percent.
        val entries = listOf(dinner(daysAgo = 2, amount = 50.0), dinner(daysAgo = 9), dinner(daysAgo = 16, amount = 90.0))
        val weights = night(daysAgo = 2, before = 75.0, after = 75.6) +
            night(daysAgo = 9, before = 75.0, after = 76.0) +
            night(daysAgo = 16, before = 75.0, after = 76.4)
        val food = nextDayOverview(entries = entries, weights = weights, today = today, days = 90).foods.single()
        assertClose(expected = 1.2, actual = food.estimate(amount = 80.0))
    }

    @Test
    fun withOneAmountOtherAmountsScaleTheAverage() {
        val entries = listOf(dinner(daysAgo = 2), dinner(daysAgo = 9), dinner(daysAgo = 16))
        val weights = night(daysAgo = 2, before = 75.0, after = 75.7) +
            night(daysAgo = 9, before = 75.0, after = 75.7) +
            night(daysAgo = 16, before = 75.0, after = 75.7)
        val food = nextDayOverview(entries = entries, weights = weights, today = today, days = 90).foods.single()
        assertClose(expected = 1.4, actual = food.estimate(amount = 140.0))
    }

    @Test
    fun fadeAveragesEachMorningAndFindsWhenItsBack() {
        val entries = listOf(dinner(daysAgo = 10), dinner(daysAgo = 20), dinner(daysAgo = 30))
        val weights = listOf(10, 20, 30).flatMap { back ->
            val date = daysAgo(days = back)
            listOf(75.0, 76.0, 75.5, 75.1).mapIndexed { day, kg -> Weight(date = date.plus(period = DatePeriod(days = day)), weight = kg) }
        }
        val food = nextDayOverview(entries = entries, weights = weights, today = today, days = 90).foods.single()
        val fade = food.fade(amount = 70.0)
        assertClose(expected = 1.0, actual = fade[0])
        assertClose(expected = 0.5, actual = fade[1])
        assertClose(expected = 0.1, actual = fade[2])
        assertEquals(expected = 3, actual = food.backOnDay(amount = 70.0))
    }

    @Test
    fun foodsWithANumberSortFirstAndBiggestIsTheTopJump() {
        val entries = listOf(2, 9, 16).flatMap { back ->
            listOf(dinner(daysAgo = back), dinner(daysAgo = back + 1, name = "Salad", isRecipe = false, amount = 200.0))
        } + dinner(daysAgo = 4, name = "Lasagna")
        val weights = listOf(2, 9, 16).flatMap { night(daysAgo = it, before = 75.0, after = 76.0) } +
            listOf(3, 10, 17).flatMap { night(daysAgo = it, before = 75.0, after = 74.7) }
        val overview = nextDayOverview(entries = entries, weights = weights, today = today, days = 90)
        assertEquals(expected = listOf("Carbonara", "Salad", "Lasagna"), actual = overview.sorted(raisesFirst = true).map { it.name })
        assertEquals(expected = listOf("Salad", "Carbonara", "Lasagna"), actual = overview.sorted(raisesFirst = false).map { it.name })
        assertEquals(expected = "Carbonara", actual = overview.biggest?.name)
    }

    @Test
    fun noBiggestWhenNothingRaisesIt() {
        val entries = listOf(2, 9, 16).map { dinner(daysAgo = it, name = "Salad", isRecipe = false, amount = 200.0) }
        val weights = listOf(2, 9, 16).flatMap { night(daysAgo = it, before = 75.0, after = 74.8) }
        assertNull(nextDayOverview(entries = entries, weights = weights, today = today, days = 90).biggest)
    }

    @Test
    fun usualDinnerAveragesEveryCountedNightOnce() {
        val entries = listOf(dinner(daysAgo = 2), dinner(daysAgo = 2, name = "Bread", isRecipe = false, amount = 50.0), dinner(daysAgo = 5))
        val weights = night(daysAgo = 2, before = 75.0, after = 76.0) + night(daysAgo = 5, before = 75.0, after = 74.8)
        val overview = nextDayOverview(entries = entries, weights = weights, today = today, days = 90)
        assertEquals(expected = 2, actual = overview.nights)
        assertClose(expected = 0.4, actual = overview.usual)
    }

    @Test
    fun aJumpThatLastNightsDinnerUsuallyMakesIsExplained() {
        val entries = listOf(dinner(daysAgo = 1), dinner(daysAgo = 9), dinner(daysAgo = 16), dinner(daysAgo = 23))
        val weights = night(daysAgo = 9, before = 75.0, after = 76.0) +
            night(daysAgo = 16, before = 75.0, after = 76.0) +
            night(daysAgo = 23, before = 75.0, after = 76.0) +
            night(daysAgo = 1, before = 74.2, after = 75.2)
        val explanation = morningExplanation(date = today, entries = entries, weights = weights)
        assertNotNull(explanation)
        assertEquals(expected = "Carbonara", actual = explanation.food.name)
        assertClose(expected = 1.0, actual = explanation.expected)
        assertClose(expected = 1.0, actual = explanation.change)
        assertEquals(expected = MorningMatch.RIGHT_ON, actual = explanation.match)
        // Last night isn't one of the dinners it's judged on.
        assertEquals(expected = 3, actual = explanation.food.counted.size)
    }

    @Test
    fun noExplanationForASmallChangeOrAnUnknownFood() {
        val entries = listOf(dinner(daysAgo = 1), dinner(daysAgo = 9), dinner(daysAgo = 16), dinner(daysAgo = 23))
        val history = night(daysAgo = 9, before = 75.0, after = 76.0) +
            night(daysAgo = 16, before = 75.0, after = 76.0) +
            night(daysAgo = 23, before = 75.0, after = 76.0)
        assertNull(morningExplanation(date = today, entries = entries, weights = history + night(daysAgo = 1, before = 75.0, after = 75.1)))
        assertNull(morningExplanation(date = today, entries = listOf(dinner(daysAgo = 1)), weights = history + night(daysAgo = 1, before = 75.0, after = 76.0)))
    }

    @Test
    fun dinnerEntriesSkipOtherMealsAndQuickAdds() {
        fun log(mealType: MealType, weight: Double, calories: Int, isQuickAdd: Boolean = false, name: String = "Carbonara") = FoodLog(
            mealType = mealType,
            calories = calories,
            protein = 0.0,
            carbs = 10.0,
            fat = 0.0,
            id = 1,
            foodName = name,
            weight = weight,
            dominantMacro = MacroType.CARBS,
            date = today,
            isQuickAdd = isQuickAdd
        )
        val entries = dinnerEntries(
            logs = listOf(
                log(mealType = MealType.DINNER, weight = 0.0, calories = 644),
                log(mealType = MealType.LUNCH, weight = 0.0, calories = 644),
                log(mealType = MealType.DINNER, weight = 0.0, calories = 500, isQuickAdd = true),
                log(mealType = MealType.DINNER, weight = 150.0, calories = 248, name = "Chicken")
            ),
            recipeCalories = mapOf("Carbonara" to 920)
        )
        assertEquals(expected = listOf(70.0, 150.0), actual = entries.map { it.amount })
        assertEquals(expected = listOf(true, false), actual = entries.map { it.isRecipe })
    }

    @Test
    fun headsUpCanBeTurnedOffPerFoodOrForEveryFood() {
        val key = nextDayFoodKey(name = "Carbonara", isRecipe = true)
        val off = NextDaySettings().withHeadsUp(key = key, isShown = false)
        assertFalse(off.isHeadsUpShown(key = key))
        assertTrue(off.withHeadsUp(key = key, isShown = true).isHeadsUpShown(key = key))
        assertFalse(NextDaySettings(isHeadsUpOn = false).isHeadsUpShown(key = key))
    }

    @Test
    fun maxFatGainIsCaloriesOverSevenThousandSevenHundred() {
        assertClose(expected = 644 / 7700.0, actual = maxFatGain(calories = 644))
    }
}
