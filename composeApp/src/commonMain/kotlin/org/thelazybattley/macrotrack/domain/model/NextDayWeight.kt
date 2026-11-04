package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.roundToInt

/** A dinner needs this many counted nights before it shows a number. */
const val NEXT_DAY_MIN_NIGHTS = 3

/** The heads-up while logging dinner shows for dinners that usually add at least this much, in kg. */
const val NEXT_DAY_HEADS_UP_KG = 0.5

/** A dinner that usually adds at least this much, in kg, raises it. */
const val NEXT_DAY_RAISES_KG = 0.4

/** A dinner that usually takes off at least this much, in kg, lowers it. */
const val NEXT_DAY_LOWERS_KG = 0.2

/** Foods under this many kcal (a drink, a side) don't change which dinner it is. */
const val NEXT_DAY_MINOR_KCAL = 100

/** A night more than this share below or above the usual dinner's kcal is a light or a big one. */
const val NEXT_DAY_SIZE_BAND = 0.15

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

/** How far back the next-day weight screens look; [days] is null for all time. */
enum class NextDayPeriod(val days: Int?) {
    MONTHS_3(days = 90),
    YEAR(days = 365),
    ALL(days = null)
}

/** Whether the heads-up shows while logging dinner, the dinners it's turned off for, and if the feature's been opened. */
@Serializable
data class NextDaySettings(
    val isHeadsUpOn: Boolean = true,
    val mutedDinners: Set<String> = emptySet(),
    val isSeen: Boolean = false
) {
    fun isHeadsUpShown(key: String): Boolean = isHeadsUpOn && key !in mutedDinners

    fun withHeadsUp(key: String, isShown: Boolean): NextDaySettings =
        copy(mutedDinners = if (isShown) mutedDinners - key else mutedDinners + key)
}

/** A food and a recipe can share a name, and a quick add is only a label, so the key keeps them apart. */
fun nextDayFoodKey(name: String, isRecipe: Boolean, isQuickAdd: Boolean = false): String = when {
    isQuickAdd -> "quick:"
    isRecipe -> "recipe:"
    else -> "food:"
} + name

/**
 * One thing logged under Dinner: [amount] in its stored unit, or the percentage of a recipe serving (0 for a quick
 * add). [log] is the log itself, kept so the dinner can be logged again.
 */
data class DinnerItem(
    val name: String,
    val isRecipe: Boolean,
    val isQuickAdd: Boolean,
    val date: LocalDate,
    val amount: Double,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val log: FoodLog? = null
) {
    val key: String get() = nextDayFoodKey(name = name, isRecipe = isRecipe, isQuickAdd = isQuickAdd)
}

/**
 * Everything logged under Dinner. A recipe log keeps only its calories, so its percentage of a serving comes from
 * [recipeCalories], the calories of one full serving by recipe name; a deleted recipe counts as one serving.
 */
fun dinnerItems(logs: List<FoodLog>, recipeCalories: Map<String, Int>): List<DinnerItem> = logs
    .filter { it.mealType == MealType.DINNER }
    .map { log ->
        val full = recipeCalories[log.foodName]
        DinnerItem(
            name = log.foodName,
            isRecipe = log.isRecipe,
            isQuickAdd = log.isQuickAdd,
            date = log.date,
            amount = when {
                log.isQuickAdd -> 0.0
                !log.isRecipe -> log.weight
                full != null && full > 0 -> (log.calories * 100.0 / full).roundToInt().toDouble()
                else -> 100.0
            },
            calories = log.calories,
            protein = log.protein,
            carbs = log.carbs,
            fat = log.fat,
            log = log
        )
    }

/**
 * Which dinner [items] make: the foods in it, whatever the amounts. Foods under [NEXT_DAY_MINOR_KCAL] don't count,
 * unless the whole dinner is made of them.
 */
fun dinnerKey(items: List<DinnerItem>): String {
    val main = items.filter { it.calories >= NEXT_DAY_MINOR_KCAL }.ifEmpty { items }
    return main.map { it.key }.distinct().sorted().joinToString(separator = "|")
}

/**
 * Dinner on [date], with the weigh-in that morning ([before]) and on each of the [NEXT_DAY_FADE_DAYS] mornings after
 * ([after]); null where there wasn't one.
 */
data class DinnerNight(
    val date: LocalDate,
    val items: List<DinnerItem>,
    val before: Double?,
    val after: List<Double?>
) {
    val calories: Int get() = items.sumOf { it.calories }
    val protein: Double get() = items.sumOf { it.protein }
    val carbs: Double get() = items.sumOf { it.carbs }
    val fat: Double get() = items.sumOf { it.fat }

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

/** How big a dinner was next to the usual one, by kcal. */
enum class DinnerSize { LIGHT, USUAL, BIG }

/** What a dinner usually does to the next morning's weight. */
enum class DinnerEffect { RAISES, LOWERS, EVEN, WAITING }

/** The counted nights of one dinner at one [size]. */
data class DinnerSizeGroup(val size: DinnerSize, val nights: List<DinnerNight>) {
    val changes: List<Double> get() = nights.mapNotNull { it.change }
    val average: Double get() = changes.average()
    val min: Double get() = changes.min()
    val max: Double get() = changes.max()
    val calories: Int get() = nights.map { it.calories }.average().roundToInt()
}

/** One food in a dinner, averaged over the nights it was in: [amount] as in [DinnerItem]. */
data class DinnerFood(
    val name: String,
    val isRecipe: Boolean,
    val isQuickAdd: Boolean,
    val amount: Double,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

/** The same foods eaten at dinner on several nights, and what the scale did after; [nights] are newest first. */
data class NextDayDinner(
    val key: String,
    val nights: List<DinnerNight>
) {
    private val mainKeys: Set<String> get() = key.split("|").toSet()

    /** The foods that make it this dinner, the most kcal first: "Chicken wings", "Rice". */
    val foods: List<String>
        get() = nights.flatMap { it.items }
            .filter { it.key in mainKeys }
            .groupBy { it.key }
            .entries
            .sortedByDescending { (_, items) -> items.sumOf { it.calories } }
            .map { (_, items) -> items.first().name }

    val name: String get() = foods.joinToString(separator = " + ")

    val counted: List<DinnerNight> get() = nights.filter { it.isCounted }

    val isReady: Boolean get() = counted.size >= NEXT_DAY_MIN_NIGHTS

    /** The average next-morning change; null until [isReady]. */
    val average: Double? get() = if (isReady) counted.mapNotNull { it.change }.average() else null

    val effect: DinnerEffect
        get() {
            val average = average ?: return DinnerEffect.WAITING
            return when {
                average >= NEXT_DAY_RAISES_KG -> DinnerEffect.RAISES
                average <= -NEXT_DAY_LOWERS_KG -> DinnerEffect.LOWERS
                else -> DinnerEffect.EVEN
            }
        }

    /** The middle night's kcal, which sets what a usual-sized dinner is. */
    val usualCalories: Int
        get() {
            val sorted = (counted.ifEmpty { nights }).map { it.calories }.sorted()
            return if (sorted.isEmpty()) 0 else sorted[sorted.size / 2]
        }

    fun sizeOf(calories: Int): DinnerSize {
        val usual = usualCalories.takeIf { it > 0 } ?: return DinnerSize.USUAL
        return when {
            calories < usual * (1 - NEXT_DAY_SIZE_BAND) -> DinnerSize.LIGHT
            calories > usual * (1 + NEXT_DAY_SIZE_BAND) -> DinnerSize.BIG
            else -> DinnerSize.USUAL
        }
    }

    /** The counted nights by size, light to big, leaving out sizes it hasn't been eaten at. */
    val sizes: List<DinnerSizeGroup>
        get() = counted.groupBy { sizeOf(calories = it.calories) }
            .map { (size, nights) -> DinnerSizeGroup(size = size, nights = nights) }
            .sortedBy { it.size.ordinal }

    fun size(size: DinnerSize): DinnerSizeGroup? = sizes.find { it.size == size }

    /** How much of each main food it usually has: the amount eaten most often, the newer one on a tie. */
    val usualFoods: List<DinnerFood> get() = foodsIn(nights = counted.ifEmpty { nights }, isUsualAmount = true)

    /** Each food in the [size] nights (every night when there are none), averaged over the nights it was in. */
    fun contents(size: DinnerSize?): List<DinnerFood> {
        val nights = size?.let { size(size = it)?.nights } ?: counted.ifEmpty { this.nights }
        return foodsIn(nights = nights, isUsualAmount = false)
    }

    private fun foodsIn(nights: List<DinnerNight>, isUsualAmount: Boolean): List<DinnerFood> = nights
        .flatMap { it.items }
        .filter { !isUsualAmount || it.key in mainKeys }
        .groupBy { it.key }
        .entries
        .sortedByDescending { (_, items) -> items.sumOf { it.calories } }
        .map { (_, items) ->
            val first = items.first()
            val amounts = items.map { it.amount }
            DinnerFood(
                name = first.name,
                isRecipe = first.isRecipe,
                isQuickAdd = first.isQuickAdd,
                amount = if (isUsualAmount) {
                    amounts.groupBy { it }.maxWith(compareBy<Map.Entry<Double, List<Double>>> { it.value.size }.thenByDescending { amounts.indexOf(it.key) }).key
                } else {
                    amounts.average()
                },
                calories = items.map { it.calories }.average().roundToInt(),
                protein = items.map { it.protein }.average(),
                carbs = items.map { it.carbs }.average(),
                fat = items.map { it.fat }.average()
            )
        }

    /**
     * The next-morning change expected after this dinner at [calories] in all; null until [isReady]. It follows a
     * straight line through every counted night by kcal, or scales the average when they were all the same size.
     */
    fun estimate(calories: Int): Double? {
        if (!isReady) return null
        val points = counted.mapNotNull { night -> night.change?.let { night.calories.toDouble() to it } }
        val meanX = points.map { it.first }.average()
        val meanY = points.map { it.second }.average()
        val spread = points.sumOf { (x, _) -> (x - meanX) * (x - meanX) }
        if (spread == 0.0) return if (meanX > 0) meanY * calories / meanX else meanY
        val slope = points.sumOf { (x, y) -> (x - meanX) * (y - meanY) } / spread
        return meanY + slope * (calories - meanX)
    }

    /**
     * The average change on each of the [NEXT_DAY_FADE_DAYS] mornings after a [size] dinner (all sizes when there are no
     * counted nights at it); null for a morning with no weigh-ins to go on.
     */
    fun fade(size: DinnerSize?): List<Double?> {
        val nights = size?.let { size(size = it)?.nights } ?: counted
        return (1..NEXT_DAY_FADE_DAYS).map { day ->
            nights.mapNotNull { it.changeOnDay(day = day) }.takeIf { it.isNotEmpty() }?.average()
        }
    }

    /** The first morning after the dinner when the weight is usually back, or null when it isn't by the last one. */
    fun backOnDay(size: DinnerSize?): Int? = fade(size = size)
        .withIndex()
        .firstOrNull { (_, change) -> change != null && change <= NEXT_DAY_BACK_KG }
        ?.let { it.index + 1 }

    /** Whether any food in it, main or minor, has [query] in its name. */
    fun hasFood(query: String): Boolean = nights.any { night -> night.items.any { it.name.contains(other = query, ignoreCase = true) } }
}

/** The order of All dinners. */
enum class DinnerSort { MOST_UP, MOST_DOWN, MOST_OFTEN }

/** Every dinner eaten on more than one night in a period, and how many nights had weigh-ins either side. */
data class NextDayOverview(
    val dinners: List<NextDayDinner>,
    val nights: Int
) {
    /** The dinner with the biggest average jump, if any dinner raises it. */
    val biggest: NextDayDinner? get() = raises.firstOrNull()

    /** Dinners that raise it, the most first. */
    val raises: List<NextDayDinner> get() = dinners.filter { it.effect == DinnerEffect.RAISES }.sortedByDescending { it.average }

    /** Dinners that lower it, the most first. */
    val lowers: List<NextDayDinner> get() = dinners.filter { it.effect == DinnerEffect.LOWERS }.sortedBy { it.average }

    fun count(effect: DinnerEffect): Int = dinners.count { it.effect == effect }

    fun dinner(key: String): NextDayDinner? = dinners.find { it.key == key }

    /** Dinners with a food matching [query] in them (all of them for a blank one). */
    fun matching(query: String): List<NextDayDinner> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) dinners else dinners.filter { it.hasFood(query = trimmed) }
    }
}

/** Dinners with a number first, in [sort] order; dinners still collecting after them, unless sorting by how often. */
fun List<NextDayDinner>.sortedFor(sort: DinnerSort): List<NextDayDinner> = when (sort) {
    DinnerSort.MOST_UP -> sortedWith(compareByDescending<NextDayDinner> { it.isReady }.thenByDescending { it.average ?: 0.0 })
    DinnerSort.MOST_DOWN -> sortedWith(compareByDescending<NextDayDinner> { it.isReady }.thenBy { it.average ?: 0.0 })
    DinnerSort.MOST_OFTEN -> sortedWith(compareByDescending<NextDayDinner> { it.nights.size }.thenByDescending { it.counted.size })
}

/** What dinners with one food in them do on average: how many, over how many counted nights, and the change. */
data class FoodDinnersAverage(val dinners: Int, val nights: Int, val average: Double)

/** Every counted night of the dinners with a number, averaged; null when none have one. */
fun List<NextDayDinner>.foodAverage(): FoodDinnersAverage? {
    val ready = filter { it.isReady }
    val changes = ready.flatMap { dinner -> dinner.counted.mapNotNull { it.change } }
    if (changes.isEmpty()) return null
    return FoodDinnersAverage(dinners = ready.size, nights = changes.size, average = changes.average())
}

/** The nights of dinner in [period] before [today], each against the weigh-ins around it. Tonight's has no next morning yet. */
fun dinnerNights(items: List<DinnerItem>, weights: List<Weight>, today: LocalDate, period: NextDayPeriod): List<DinnerNight> {
    val byDate = weights.associate { it.date to it.weight }
    val from = period.days?.let { today.minus(period = DatePeriod(days = it)) }
    return items
        .filter { it.date < today && (from == null || it.date >= from) }
        .groupBy { it.date }
        .map { (date, dinner) ->
            DinnerNight(
                date = date,
                items = dinner,
                before = byDate[date],
                after = (1..NEXT_DAY_FADE_DAYS).map { byDate[date.plus(period = DatePeriod(days = it))] }
            )
        }
        .sortedByDescending { it.date }
}

/**
 * The dinners in [period] before [today], grouped by the foods in them. A dinner eaten only once is left out until
 * it repeats.
 */
fun nextDayOverview(items: List<DinnerItem>, weights: List<Weight>, today: LocalDate, period: NextDayPeriod): NextDayOverview {
    val nights = dinnerNights(items = items, weights = weights, today = today, period = period)
    val dinners = nights.groupBy { dinnerKey(items = it.items) }
        .filter { (_, same) -> same.size > 1 }
        .map { (key, same) -> NextDayDinner(key = key, nights = same) }
    return NextDayOverview(dinners = dinners, nights = nights.count { it.isCounted })
}

/** What tonight's dinner, so far, usually does to tomorrow's weigh-in. */
data class DinnerHeadsUp(
    val dinner: NextDayDinner,
    val calories: Int,
    val estimate: Double,
    /** At its usual size the estimate is that size's average; otherwise it's worked out for this size. */
    val isUsualSize: Boolean,
    val isOff: Boolean
)

/**
 * The heads-up for a dinner of [items], when it matches a past dinner with a number that usually adds
 * [NEXT_DAY_HEADS_UP_KG] or more, or would at this size. [keepOff] is the dinner just turned off, which stays to
 * offer Undo.
 */
fun dinnerHeadsUp(items: List<DinnerItem>, overview: NextDayOverview?, settings: NextDaySettings, keepOff: Set<String>): DinnerHeadsUp? {
    if (items.isEmpty() || overview == null || !settings.isHeadsUpOn) return null
    val key = dinnerKey(items = items)
    val isOff = key in settings.mutedDinners
    if (isOff && key !in keepOff) return null
    val dinner = overview.dinner(key = key)?.takeIf { it.isReady } ?: return null
    val calories = items.sumOf { it.calories }
    val size = dinner.sizeOf(calories = calories)
    val usual = dinner.size(size = DinnerSize.USUAL).takeIf { size == DinnerSize.USUAL }
    val estimate = usual?.average ?: dinner.estimate(calories = calories) ?: return null
    if (estimate < NEXT_DAY_HEADS_UP_KG && (dinner.average ?: 0.0) < NEXT_DAY_HEADS_UP_KG) return null
    return DinnerHeadsUp(dinner = dinner, calories = calories, estimate = estimate, isUsualSize = usual != null, isOff = isOff)
}

/** How the expected change compares with the one on the scale. */
enum class MorningMatch { RIGHT_ON, MORE, LESS }

/**
 * A weigh-in on [date] that jumped by [change] since the morning before, and the dinner that usually does that:
 * [night] last night is expected to add [expected], and the weight is usually back by [backBy].
 */
data class MorningExplanation(
    val date: LocalDate,
    val weight: Double,
    val change: Double,
    val dinner: NextDayDinner,
    val night: DinnerNight,
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
 * Why the weigh-in on [date] jumped, when it did by [NEXT_DAY_JUMP_KG] or more and last night's dinner usually adds
 * that much. The dinner is judged on the nights before last night, from [period] before it.
 */
fun morningExplanation(
    date: LocalDate,
    items: List<DinnerItem>,
    weights: List<Weight>,
    period: NextDayPeriod = NextDayPeriod.YEAR
): MorningExplanation? {
    val byDate = weights.associate { it.date to it.weight }
    val dinnerDate = date.minus(period = DatePeriod(days = 1))
    val weight = byDate[date] ?: return null
    val before = byDate[dinnerDate] ?: return null
    val change = weight - before
    if (change < NEXT_DAY_JUMP_KG) return null
    val lastNight = items.filter { it.date == dinnerDate }.takeIf { it.isNotEmpty() } ?: return null
    val history = nextDayOverview(items = items, weights = weights, today = dinnerDate, period = period)
    val dinner = history.dinners.find { it.key == dinnerKey(items = lastNight) }?.takeIf { it.isReady } ?: return null
    val night = DinnerNight(date = dinnerDate, items = lastNight, before = before, after = listOf(weight))
    val size = dinner.sizeOf(calories = night.calories)
    val expected = dinner.size(size = size)?.average ?: dinner.estimate(calories = night.calories) ?: return null
    if (expected < NEXT_DAY_JUMP_KG) return null
    val backDay = dinner.backOnDay(size = size) ?: (NEXT_DAY_FADE_DAYS + 1)
    return MorningExplanation(
        date = date,
        weight = weight,
        change = change,
        dinner = dinner,
        night = night,
        expected = expected,
        backBy = dinnerDate.plus(period = DatePeriod(days = backDay))
    )
}

/** The most the [calories] could add as body fat, in kg, if every one of them were extra. */
fun maxFatGain(calories: Int): Double = calories / KCAL_PER_KG_FAT
