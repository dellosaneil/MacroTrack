package org.thelazybattley.macrotrack.domain.model

/** Where an online food's numbers come from, as far as can be told. */
enum class OnlineFoodSource {
    /** Has a brand and a completed nutrition label: copied from the maker's label. */
    BRAND,

    /** Added by someone and not completed or checked; worth comparing with the label. */
    COMMUNITY
}

/** A nutrient an online food can be missing, listed on a community food's warning. */
enum class OnlineNutrient { PROTEIN, CARBS, FAT, FIBER, SUGAR, SODIUM }

/**
 * A packaged food found by searching online. [food] holds the nutrition per 100 g or 100 ml and carries the
 * barcode; [fiber] and [sugar] (grams) and [sodium] (milligrams) are per 100 too, null when the source has none.
 * [servingLabel] and [servingGrams] are the label's own serving when it's known: searches don't send it, a look-up
 * by barcode does.
 */
data class OnlineFood(
    val food: Food,
    val brand: String?,
    val source: OnlineFoodSource,
    val fiber: Double?,
    val sugar: Double?,
    val sodium: Double?,
    val servingLabel: String? = null,
    val servingGrams: Double? = null,
    val missing: List<OnlineNutrient> = emptyList()
) {
    val barcode: String get() = food.barcode.orEmpty()

    /** The portion it's picked with: the label's serving, else 100 g or ml. */
    val portion: Double get() = servingGrams ?: PER_100

    fun toScannedProduct() = ScannedProduct(food = food, brand = brand, servingLabel = servingLabel, servingGrams = servingGrams)

    companion object {
        const val PER_100 = 100.0
    }
}

/** One page of online results; [totalCount] is how many there are in all, which can be an estimate. */
data class OnlineSearchPage(val foods: List<OnlineFood>, val totalCount: Int)
