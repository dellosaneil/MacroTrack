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

    private fun item(daysAgo: Int, name: String, calories: Int, amount: Double = 200.0, carbs: Double = 40.0) = DinnerItem(
        name = name,
        isRecipe = false,
        isQuickAdd = false,
        date = daysAgo(days = daysAgo),
        amount = amount,
        calories = calories,
        protein = 20.0,
        carbs = carbs,
        fat = 10.0
    )

    /** Chicken wings and rice on [daysAgo], [scale] times the usual amounts. */
    private fun wingsAndRice(daysAgo: Int, scale: Double = 1.0) = listOf(
        item(daysAgo = daysAgo, name = "Chicken wings", calories = (720 * scale).toInt(), amount = 8 * scale),
        item(daysAgo = daysAgo, name = "Rice", calories = (325 * scale).toInt(), amount = 250 * scale, carbs = 70 * scale)
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
    fun aDinnerIsTheFoodsLoggedThatDayWhateverTheAmounts() {
        val items = wingsAndRice(daysAgo = 2) + wingsAndRice(daysAgo = 9, scale = 1.5)
        val dinner = nextDayOverview(items = items, weights = emptyList(), today = today, period = NextDayPeriod.YEAR).dinners.single()
        assertEquals(expected = listOf("Chicken wings", "Rice"), actual = dinner.foods)
        assertEquals(expected = "Chicken wings + Rice", actual = dinner.name)
        assertEquals(expected = 2, actual = dinner.nights.size)
    }

    @Test
    fun aSmallSideOrDrinkDoesNotChangeTheDinner() {
        val items = wingsAndRice(daysAgo = 2) + item(daysAgo = 2, name = "Iced tea", calories = 60) + wingsAndRice(daysAgo = 9)
        val overview = nextDayOverview(items = items, weights = emptyList(), today = today, period = NextDayPeriod.YEAR)
        assertEquals(expected = 1, actual = overview.dinners.size)
        assertEquals(expected = "Chicken wings + Rice", actual = overview.dinners.single().name)
    }

    @Test
    fun aDinnerEatenOnceIsLeftOutUntilItRepeats() {
        val items = wingsAndRice(daysAgo = 2) + wingsAndRice(daysAgo = 9) + item(daysAgo = 5, name = "Pad thai", calories = 700)
        val overview = nextDayOverview(items = items, weights = emptyList(), today = today, period = NextDayPeriod.YEAR)
        assertEquals(expected = listOf("Chicken wings + Rice"), actual = overview.dinners.map { it.name })
    }

    @Test
    fun tonightAndDinnersBeforeThePeriodAreLeftOut() {
        val items = wingsAndRice(daysAgo = 0) + wingsAndRice(daysAgo = 5) + wingsAndRice(daysAgo = 20) + wingsAndRice(daysAgo = 200)
        val dinner = nextDayOverview(items = items, weights = emptyList(), today = today, period = NextDayPeriod.MONTHS_3).dinners.single()
        assertEquals(expected = listOf(daysAgo(days = 5), daysAgo(days = 20)), actual = dinner.nights.map { it.date })
        val all = nextDayOverview(items = items, weights = emptyList(), today = today, period = NextDayPeriod.ALL).dinners.single()
        assertEquals(expected = 3, actual = all.nights.size)
    }

    @Test
    fun aDinnerNeedsThreeCountedNights() {
        val items = listOf(2, 9, 16).flatMap { wingsAndRice(daysAgo = it) }
        val twoNights = night(daysAgo = 2, before = 75.0, after = 76.0) + night(daysAgo = 9, before = 75.0, after = 76.2)
        val waiting = nextDayOverview(items = items, weights = twoNights, today = today, period = NextDayPeriod.YEAR).dinners.single()
        assertFalse(waiting.isReady)
        assertEquals(expected = DinnerEffect.WAITING, actual = waiting.effect)
        val ready = nextDayOverview(items = items, weights = twoNights + night(daysAgo = 16, before = 75.0, after = 76.1), today = today, period = NextDayPeriod.YEAR)
            .dinners.single()
        assertClose(expected = 1.1, actual = ready.average)
        assertEquals(expected = DinnerEffect.RAISES, actual = ready.effect)
    }

    @Test
    fun effectsSplitAtThreeTenthsEitherWay() {
        fun dinnerWith(change: Double): NextDayDinner {
            val items = listOf(2, 9, 16).flatMap { wingsAndRice(daysAgo = it) }
            val weights = listOf(2, 9, 16).flatMap { night(daysAgo = it, before = 75.0, after = 75.0 + change) }
            return nextDayOverview(items = items, weights = weights, today = today, period = NextDayPeriod.YEAR).dinners.single()
        }
        assertEquals(expected = DinnerEffect.RAISES, actual = dinnerWith(change = 0.3).effect)
        assertEquals(expected = DinnerEffect.EVEN, actual = dinnerWith(change = 0.29).effect)
        assertEquals(expected = DinnerEffect.EVEN, actual = dinnerWith(change = -0.29).effect)
        assertEquals(expected = DinnerEffect.LOWERS, actual = dinnerWith(change = -0.3).effect)
    }

    @Test
    fun nightsAreGroupedBySizeAgainstTheUsualDinner() {
        val items = wingsAndRice(daysAgo = 2) + wingsAndRice(daysAgo = 9) + wingsAndRice(daysAgo = 16) +
            wingsAndRice(daysAgo = 23, scale = 0.7) + wingsAndRice(daysAgo = 30, scale = 1.4)
        val weights = night(daysAgo = 2, before = 75.0, after = 76.1) +
            night(daysAgo = 9, before = 75.0, after = 76.1) +
            night(daysAgo = 16, before = 75.0, after = 76.1) +
            night(daysAgo = 23, before = 75.0, after = 75.8) +
            night(daysAgo = 30, before = 75.0, after = 76.4)
        val dinner = nextDayOverview(items = items, weights = weights, today = today, period = NextDayPeriod.YEAR).dinners.single()
        assertEquals(expected = listOf(DinnerSize.LIGHT, DinnerSize.USUAL, DinnerSize.BIG), actual = dinner.sizes.map { it.size })
        assertClose(expected = 0.8, actual = dinner.size(size = DinnerSize.LIGHT)?.average)
        assertClose(expected = 1.1, actual = dinner.size(size = DinnerSize.USUAL)?.average)
        assertClose(expected = 1.4, actual = dinner.size(size = DinnerSize.BIG)?.average)
        assertEquals(expected = 3, actual = dinner.size(size = DinnerSize.USUAL)?.nights?.size)
    }

    @Test
    fun otherSizesFollowALineThroughTheNightsByKcal() {
        val items = wingsAndRice(daysAgo = 2, scale = 0.5) + wingsAndRice(daysAgo = 9) + wingsAndRice(daysAgo = 16, scale = 1.5)
        val weights = night(daysAgo = 2, before = 75.0, after = 75.5) +
            night(daysAgo = 9, before = 75.0, after = 76.0) +
            night(daysAgo = 16, before = 75.0, after = 76.5)
        val dinner = nextDayOverview(items = items, weights = weights, today = today, period = NextDayPeriod.YEAR).dinners.single()
        val estimate = dinner.estimate(calories = 1045 * 2)
        assertNotNull(estimate)
        assertTrue(abs(2.0 - estimate) < 0.01, "was $estimate")
    }

    @Test
    fun usualFoodsAreTheAmountsEatenMostOften() {
        val items = wingsAndRice(daysAgo = 2) + wingsAndRice(daysAgo = 9) + wingsAndRice(daysAgo = 16, scale = 1.5)
        val dinner = nextDayOverview(items = items, weights = emptyList(), today = today, period = NextDayPeriod.YEAR).dinners.single()
        assertEquals(expected = listOf(8.0, 250.0), actual = dinner.usualFoods.map { it.amount })
    }

    @Test
    fun fadeAveragesEachMorningAndFindsWhenItsBack() {
        val items = listOf(10, 20, 30).flatMap { wingsAndRice(daysAgo = it) }
        val weights = listOf(10, 20, 30).flatMap { back ->
            val date = daysAgo(days = back)
            listOf(75.0, 76.1, 75.6, 75.1).mapIndexed { day, kg -> Weight(date = date.plus(period = DatePeriod(days = day)), weight = kg) }
        }
        val dinner = nextDayOverview(items = items, weights = weights, today = today, period = NextDayPeriod.YEAR).dinners.single()
        val fade = dinner.fade(size = DinnerSize.USUAL)
        assertClose(expected = 1.1, actual = fade[0])
        assertClose(expected = 0.6, actual = fade[1])
        assertClose(expected = 0.1, actual = fade[2])
        assertEquals(expected = 3, actual = dinner.backOnDay(size = DinnerSize.USUAL))
    }

    @Test
    fun raisesAndLowersAreSortedAndBiggestIsTheTopJump() {
        val items = listOf(2, 9, 16).flatMap { wingsAndRice(daysAgo = it) } +
            listOf(4, 11, 18).flatMap { listOf(item(daysAgo = it, name = "Grilled chicken", calories = 300), item(daysAgo = it, name = "Salad", calories = 120)) } +
            listOf(5, 12, 19).flatMap { listOf(item(daysAgo = it, name = "Pork adobo", calories = 600), item(daysAgo = it, name = "Rice", calories = 325)) }
        val weights = listOf(2, 9, 16).flatMap { night(daysAgo = it, before = 75.0, after = 76.1) } +
            listOf(4, 11, 18).flatMap { night(daysAgo = it, before = 75.0, after = 74.7) } +
            listOf(5, 12, 19).flatMap { night(daysAgo = it, before = 75.0, after = 75.9) }
        val overview = nextDayOverview(items = items, weights = weights, today = today, period = NextDayPeriod.YEAR)
        assertEquals(expected = listOf("Chicken wings + Rice", "Pork adobo + Rice"), actual = overview.raises.map { it.name })
        assertEquals(expected = listOf("Grilled chicken + Salad"), actual = overview.lowers.map { it.name })
        assertEquals(expected = "Chicken wings + Rice", actual = overview.biggest?.name)
        assertEquals(expected = 9, actual = overview.nights)
    }

    @Test
    fun searchFindsEveryDinnerWithThatFoodAndAveragesThem() {
        val items = listOf(2, 9, 16).flatMap { wingsAndRice(daysAgo = it) } +
            listOf(5, 12, 19).flatMap { listOf(item(daysAgo = it, name = "Pork adobo", calories = 600), item(daysAgo = it, name = "Rice", calories = 325)) } +
            listOf(4, 11).flatMap { listOf(item(daysAgo = it, name = "Grilled chicken", calories = 300)) }
        val weights = listOf(2, 9, 16).flatMap { night(daysAgo = it, before = 75.0, after = 76.0) } +
            listOf(5, 12, 19).flatMap { night(daysAgo = it, before = 75.0, after = 75.8) }
        val overview = nextDayOverview(items = items, weights = weights, today = today, period = NextDayPeriod.YEAR)
        val rice = overview.matching(query = " RICE ")
        assertEquals(expected = 2, actual = rice.size)
        val average = rice.foodAverage()
        assertNotNull(average)
        assertEquals(expected = 2, actual = average.dinners)
        assertEquals(expected = 6, actual = average.nights)
        assertClose(expected = 0.9, actual = average.average)
        assertEquals(expected = 3, actual = overview.matching(query = "").size)
    }

    @Test
    fun sortsPutDinnersWithANumberFirst() {
        val items = listOf(2, 9, 16).flatMap { wingsAndRice(daysAgo = it) } +
            listOf(4, 11, 18, 25).flatMap { listOf(item(daysAgo = it, name = "Grilled chicken", calories = 300)) }
        val weights = listOf(2, 9, 16).flatMap { night(daysAgo = it, before = 75.0, after = 76.0) }
        val dinners = nextDayOverview(items = items, weights = weights, today = today, period = NextDayPeriod.YEAR).dinners
        assertEquals(expected = "Chicken wings + Rice", actual = dinners.sortedFor(sort = DinnerSort.MOST_UP).first().name)
        assertEquals(expected = "Chicken wings + Rice", actual = dinners.sortedFor(sort = DinnerSort.MOST_DOWN).first().name)
        assertEquals(expected = "Grilled chicken", actual = dinners.sortedFor(sort = DinnerSort.MOST_OFTEN).first().name)
    }

    @Test
    fun headsUpShowsForAMatchingDinnerThatUsuallyAddsHalfAKilo() {
        val items = listOf(2, 9, 16).flatMap { wingsAndRice(daysAgo = it) }
        val weights = listOf(2, 9, 16).flatMap { night(daysAgo = it, before = 75.0, after = 76.1) }
        val overview = nextDayOverview(items = items, weights = weights, today = today, period = NextDayPeriod.YEAR)
        val tonight = wingsAndRice(daysAgo = 0)
        val headsUp = dinnerHeadsUp(items = tonight, overview = overview, settings = NextDaySettings(), keepOff = emptySet())
        assertNotNull(headsUp)
        assertTrue(headsUp.isUsualSize)
        assertClose(expected = 1.1, actual = headsUp.estimate)
        // Only one of the two foods so far isn't this dinner yet.
        assertNull(dinnerHeadsUp(items = tonight.take(n = 1), overview = overview, settings = NextDaySettings(), keepOff = emptySet()))
        // Turned off for this dinner, or for every dinner.
        val key = headsUp.dinner.key
        val muted = NextDaySettings().withHeadsUp(key = key, isShown = false)
        assertNull(dinnerHeadsUp(items = tonight, overview = overview, settings = muted, keepOff = emptySet()))
        assertTrue(dinnerHeadsUp(items = tonight, overview = overview, settings = muted, keepOff = setOf(key))?.isOff == true)
        assertNull(dinnerHeadsUp(items = tonight, overview = overview, settings = NextDaySettings(isHeadsUpOn = false), keepOff = emptySet()))
    }

    @Test
    fun noHeadsUpForADinnerThatBarelyMovesIt() {
        val items = listOf(2, 9, 16).flatMap { wingsAndRice(daysAgo = it) }
        val weights = listOf(2, 9, 16).flatMap { night(daysAgo = it, before = 75.0, after = 75.2) }
        val overview = nextDayOverview(items = items, weights = weights, today = today, period = NextDayPeriod.YEAR)
        assertNull(dinnerHeadsUp(items = wingsAndRice(daysAgo = 0), overview = overview, settings = NextDaySettings(), keepOff = emptySet()))
    }

    @Test
    fun aJumpThatLastNightsDinnerUsuallyMakesIsExplained() {
        val items = listOf(1, 9, 16, 23).flatMap { wingsAndRice(daysAgo = it) }
        val weights = listOf(9, 16, 23).flatMap { night(daysAgo = it, before = 75.0, after = 76.1) } + night(daysAgo = 1, before = 74.2, after = 75.3)
        val explanation = morningExplanation(date = today, items = items, weights = weights)
        assertNotNull(explanation)
        assertEquals(expected = "Chicken wings + Rice", actual = explanation.dinner.name)
        assertClose(expected = 1.1, actual = explanation.expected)
        assertEquals(expected = MorningMatch.RIGHT_ON, actual = explanation.match)
        // Last night isn't one of the nights it's judged on.
        assertEquals(expected = 3, actual = explanation.dinner.counted.size)
        assertEquals(expected = daysAgo(days = 1), actual = explanation.night.date)
    }

    @Test
    fun noExplanationForASmallChangeOrADinnerWithoutANumber() {
        val items = listOf(1, 9, 16, 23).flatMap { wingsAndRice(daysAgo = it) }
        val history = listOf(9, 16, 23).flatMap { night(daysAgo = it, before = 75.0, after = 76.1) }
        assertNull(morningExplanation(date = today, items = items, weights = history + night(daysAgo = 1, before = 75.0, after = 75.1)))
        val other = items.filter { it.date != daysAgo(days = 1) } + item(daysAgo = 1, name = "Pad thai", calories = 700)
        assertNull(morningExplanation(date = today, items = other, weights = history + night(daysAgo = 1, before = 75.0, after = 76.0)))
    }

    @Test
    fun dinnerItemsAreEverythingUnderDinner() {
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
        val items = dinnerItems(
            logs = listOf(
                log(mealType = MealType.DINNER, weight = 0.0, calories = 644),
                log(mealType = MealType.LUNCH, weight = 0.0, calories = 644),
                log(mealType = MealType.DINNER, weight = 0.0, calories = 500, isQuickAdd = true, name = "Dinner out"),
                log(mealType = MealType.DINNER, weight = 150.0, calories = 248, name = "Chicken")
            ),
            recipeCalories = mapOf("Carbonara" to 920)
        )
        assertEquals(expected = listOf(70.0, 0.0, 150.0), actual = items.map { it.amount })
        assertEquals(expected = listOf("recipe:Carbonara", "quick:Dinner out", "food:Chicken"), actual = items.map { it.key })
    }

    @Test
    fun maxFatGainIsCaloriesOverSevenThousandSevenHundred() {
        assertClose(expected = 1045 / 7700.0, actual = maxFatGain(calories = 1045))
    }
}
