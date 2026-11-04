package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.roundToInt

/** A food needs this many counted dinners before it shows a number. */
const val NEXT_DAY_MIN_NIGHTS = 3

/** The heads-up while logging dinner shows for foods that usually add at least this much, in kg. */
const val NEXT_DAY_HEADS_UP_KG = 0.5

/** How many mornings after a dinner are followed. */
const val NEXT_DAY_FADE_DAYS = 3

/** Within this much of the morning before the dinner, in kg, the weight counts as back. */
const val NEXT_DAY_BACK_KG = 0.15

/** A next-morning jump at least this big, in kg, is explained when last night's dinner usually does it. */
const val NEXT_DAY_JUMP_KG = 0.3

/** Today's weight is "right on" the expected change when it's within this much of it, in kg. */
const val NEXT_DAY_MATCH_KG = 0.2

/** Roughly how much eating above maintenance makes 1 kg of body fat. */
const val KCAL_PER_KG_FAT = 7700.0

/** How the next-day weight screens look back, in days. */
enum class NextDayPeriod(val days: Int) {
    DAYS_30(days = 30),
    DAYS_60(days = 60),
    DAYS_90(days = 90)
}

/** Whether the heads-up shows while logging dinner, the foods it's turned off for, and if the feature's been opened. */
@Serializable
data class NextDaySettings(
    val isHeadsUpOn: Boolean = true,
    val mutedFoods: Set<String> = emptySet(),
    val isSeen: Boolean = false
) {
    fun isHeadsUpShown(key: String): Boolean = isHeadsUpOn && key !in mutedFoods

    fun withHeadsUp(key: String, isShown: Boolean): NextDaySettings =
        copy(mutedFoods = if (isShown) mutedFoods - key else mutedFoods + key)
}

/** A food and a recipe can share a name, so the key keeps them apart. */
fun nextDayFoodKey(name: String, isRecipe: Boolean): String = (if (isRecipe) "recipe:" else "food:") + name

/** One food logged at dinner on [date]: [amount] in its stored unit, or the percentage of a recipe serving. */
data class DinnerEntry(
    val name: String,
    val isRecipe: Boolean,
    val date: LocalDate,
    val amount: Double,
    val calories: Int,
    val carbs: Double
) {
    val key: String get() = nextDayFoodKey(name = name, isRecipe = isRecipe)
}

/**
 * Every food logged at dinner. A recipe log keeps only its calories, so its percentage of a serving comes from
 * [recipeCalories], the calories of one full serving by recipe name; a deleted recipe counts as one serving.
 */
fun dinnerEntries(logs: List<FoodLog>, recipeCalories: Map<String, Int>): List<DinnerEntry> = logs
    .filter { it.mealType == MealType.DINNER && !it.isQuickAdd }
    .map { log ->
        val full = recipeCalories[log.foodName]
        DinnerEntry(
            name = log.foodName,
            isRecipe = log.isRecipe,
            date = log.date,
            amount = when {
                !log.isRecipe -> log.weight
                full != null && full > 0 -> (log.calories * 100.0 / full).roundToInt().toDouble()
                else -> 100.0
            },
            calories = log.calories,
            carbs = log.carbs
        )
    }

/**
 * A food eaten at dinner on [date], with the weigh-in that morning ([before]) and on each of the
 * [NEXT_DAY_FADE_DAYS] mornings after ([after]); null where there wasn't one.
 */
data class DinnerNight(
    val date: LocalDate,
    val amount: Double,
    val calories: Int,
    val carbs: Double,
    val before: Double?,
    val after: List<Double?>
) {
    val nextMorning: Double? get() = after.firstOrNull()

    /** Next morning minus that morning; null without both weigh-ins, and then the dinner isn't counted. */
    val change: Double? get() = changeOnDay(day = 1)

    val isCounted: Boolean get() = change != null

    /** The weight on morning [day] after the dinner (1 is the next morning) minus that morning's. */
    fun changeOnDay(day: Int): Double? {
        val start = before ?: return null
        return after.getOrNull(index = day - 1)?.let { it - start }
    }
}

/** The counted dinners of one food at one [amount]. */
data class NextDayPortion(val amount: Double, val nights: List<DinnerNight>) {
    val changes: List<Double> get() = nights.mapNotNull { it.change }
    val average: Double get() = changes.average()
    val min: Double get() = changes.min()
    val max: Double get() = changes.max()
}

/** One food eaten at dinner and what the scale did after; [nights] are newest first. */
data class NextDayFood(
    val name: String,
    val isRecipe: Boolean,
    val nights: List<DinnerNight>
) {
    val key: String get() = nextDayFoodKey(name = name, isRecipe = isRecipe)

    val counted: List<DinnerNight> get() = nights.filter { it.isCounted }

    val isReady: Boolean get() = counted.size >= NEXT_DAY_MIN_NIGHTS

    /** The average next-morning change; null until [isReady]. */
    val average: Double? get() = if (isReady) counted.mapNotNull { it.change }.average() else null

    /** The amount eaten most often at dinner; a tie goes to the newer amount. */
    val usual: Double?
        get() {
            val amounts = (counted.ifEmpty { nights }).map { it.amount }
            if (amounts.isEmpty()) return null
            return amounts.groupBy { it }
                .maxWith(compareBy<Map.Entry<Double, List<Double>>> { it.value.size }.thenByDescending { amounts.indexOf(it.key) })
                .key
        }

    /** The counted dinners grouped by amount, smallest first. */
    val portions: List<NextDayPortion>
        get() = counted.groupBy { it.amount }.map { (amount, nights) -> NextDayPortion(amount = amount, nights = nights) }.sortedBy { it.amount }

    fun portion(amount: Double): NextDayPortion? = portions.find { it.amount == amount }

    /**
     * The next-morning change expected after eating [amount]; null until [isReady]. An amount eaten before uses those
     * dinners; any other follows a straight line through every counted dinner, or scales the average when they were
     * all the same amount.
     */
    fun estimate(amount: Double): Double? {
        if (!isReady) return null
        portion(amount = amount)?.let { return it.average }
        val points = counted.mapNotNull { night -> night.change?.let { night.amount to it } }
        val meanX = points.map { it.first }.average()
        val meanY = points.map { it.second }.average()
        val spread = points.sumOf { (x, _) -> (x - meanX) * (x - meanX) }
        if (spread == 0.0) return if (meanX > 0) meanY * amount / meanX else meanY
        val slope = points.sumOf { (x, y) -> (x - meanX) * (y - meanY) } / spread
        return meanY + slope * (amount - meanX)
    }

    /**
     * The average change on each of the [NEXT_DAY_FADE_DAYS] mornings after eating [amount] (all amounts when there
     * are no counted dinners at it); null for a morning with no weigh-ins to go on.
     */
    fun fade(amount: Double?): List<Double?> {
        val atAmount = counted.filter { it.amount == amount }.ifEmpty { counted }
        return (1..NEXT_DAY_FADE_DAYS).map { day ->
            atAmount.mapNotNull { it.changeOnDay(day = day) }.takeIf { it.isNotEmpty() }?.average()
        }
    }

    /** The first morning after the dinner when the weight is usually back, or null when it isn't by the last one. */
    fun backOnDay(amount: Double?): Int? = fade(amount = amount)
        .withIndex()
        .firstOrNull { (_, change) -> change != null && change <= NEXT_DAY_BACK_KG }
        ?.let { it.index + 1 }
}

/** The dinner foods in a period, how many dinners were counted, and the usual change after one. */
data class NextDayOverview(
    val foods: List<NextDayFood>,
    val nights: Int,
    val usual: Double?
) {
    val ready: List<NextDayFood> get() = foods.filter { it.isReady }

    /** The food with the biggest average jump, if any food usually raises it. */
    val biggest: NextDayFood? get() = ready.filter { (it.average ?: 0.0) > 0 }.maxByOrNull { it.average ?: 0.0 }

    /**
     * Foods with a number first, the ones that raise it most at the top when [raisesFirst], otherwise the ones that
     * lower it; then foods still collecting, the closest to a number first.
     */
    fun sorted(raisesFirst: Boolean): List<NextDayFood> {
        val byAverage = compareBy<NextDayFood> { it.average ?: 0.0 }
        val ready = ready.sortedWith(if (raisesFirst) byAverage.reversed() else byAverage)
        val waiting = foods.filterNot { it.isReady }.sortedWith(compareByDescending<NextDayFood> { it.counted.size }.thenBy { it.name.lowercase() })
        return ready + waiting
    }

    fun food(key: String): NextDayFood? = foods.find { it.key == key }
}

/**
 * The dinners from the [days] days before [today], each against the weigh-ins around it. Today's dinner has no next
 * morning yet, so it isn't in.
 */
fun nextDayOverview(entries: List<DinnerEntry>, weights: List<Weight>, today: LocalDate, days: Int): NextDayOverview {
    val byDate = weights.associate { it.date to it.weight }
    val from = today.minus(period = DatePeriod(days = days))
    val inPeriod = entries.filter { it.date >= from && it.date < today }
    val foods = inPeriod.groupBy { it.key }.map { (_, foodEntries) ->
        val first = foodEntries.first()
        NextDayFood(
            name = first.name,
            isRecipe = first.isRecipe,
            nights = foodEntries.groupBy { it.date }
                .map { (date, sameNight) ->
                    DinnerNight(
                        date = date,
                        amount = sameNight.sumOf { it.amount },
                        calories = sameNight.sumOf { it.calories },
                        carbs = sameNight.sumOf { it.carbs },
                        before = byDate[date],
                        after = (1..NEXT_DAY_FADE_DAYS).map { byDate[date.plus(period = DatePeriod(days = it))] }
                    )
                }
                .sortedByDescending { it.date }
        )
    }
    val dinnerChanges = inPeriod.map { it.date }.distinct().mapNotNull { date ->
        val before = byDate[date] ?: return@mapNotNull null
        val after = byDate[date.plus(period = DatePeriod(days = 1))] ?: return@mapNotNull null
        after - before
    }
    return NextDayOverview(foods = foods, nights = dinnerChanges.size, usual = dinnerChanges.takeIf { it.isNotEmpty() }?.average())
}

/** How the expected change compares with the one on the scale. */
enum class MorningMatch { RIGHT_ON, MORE, LESS }

/**
 * A weigh-in on [date] that jumped by [change] since the morning before, and the dinner food that usually does that:
 * [amount] of [food] last night is expected to add [expected], and the weight is usually back by [backBy].
 */
data class MorningExplanation(
    val date: LocalDate,
    val dinnerDate: LocalDate,
    val weight: Double,
    val change: Double,
    val food: NextDayFood,
    val amount: Double,
    val expected: Double,
    val backBy: LocalDate
) {
    val match: MorningMatch
        get() = when {
            abs(change - expected) <= NEXT_DAY_MATCH_KG -> MorningMatch.RIGHT_ON
            change > expected -> MorningMatch.MORE
            else -> MorningMatch.LESS
        }
}

/**
 * Why the weigh-in on [date] jumped, when it did by [NEXT_DAY_JUMP_KG] or more and a food from last night's dinner
 * usually adds that much. Each food is judged on the dinners before last night, from the [days] days before it.
 */
fun morningExplanation(
    date: LocalDate,
    entries: List<DinnerEntry>,
    weights: List<Weight>,
    days: Int = NextDayPeriod.DAYS_90.days
): MorningExplanation? {
    val byDate = weights.associate { it.date to it.weight }
    val dinnerDate = date.minus(period = DatePeriod(days = 1))
    val weight = byDate[date] ?: return null
    val before = byDate[dinnerDate] ?: return null
    val change = weight - before
    if (change < NEXT_DAY_JUMP_KG) return null
    val history = nextDayOverview(entries = entries, weights = weights, today = dinnerDate, days = days)
    val (food, amount, expected) = entries
        .filter { it.date == dinnerDate }
        .groupBy { it.key }
        .mapNotNull { (key, eaten) ->
            val food = history.food(key = key) ?: return@mapNotNull null
            val amount = eaten.sumOf { it.amount }
            val expected = food.estimate(amount = amount) ?: return@mapNotNull null
            Triple(food, amount, expected)
        }
        .filter { (_, _, expected) -> expected >= NEXT_DAY_JUMP_KG }
        .maxByOrNull { (_, _, expected) -> expected }
        ?: return null
    val backDay = food.backOnDay(amount = amount) ?: (NEXT_DAY_FADE_DAYS + 1)
    return MorningExplanation(
        date = date,
        dinnerDate = dinnerDate,
        weight = weight,
        change = change,
        food = food,
        amount = amount,
        expected = expected,
        backBy = dinnerDate.plus(period = DatePeriod(days = backDay))
    )
}

/** The most the [calories] could add as body fat, in kg, if every one of them were extra. */
fun maxFatGain(calories: Int): Double = calories / KCAL_PER_KG_FAT
