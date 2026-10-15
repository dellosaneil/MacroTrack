package org.thelazybattley.macrotrack.domain.model

/**
 * A packaged food found in Open Food Facts. [food] holds the label's nutrition per 100 g or 100 ml and carries the
 * barcode; [servingLabel] and [servingGrams] are the label's own serving ("1 cup (170 g)", 170.0) when it has one,
 * in the same unit as [food].
 */
data class ScannedProduct(
    val food: Food,
    val brand: String?,
    val servingLabel: String?,
    val servingGrams: Double?
)

/** What a scanned barcode turned out to be. */
sealed interface BarcodeLookup {
    /** A food the user already saved with this barcode. */
    data class Saved(val food: Food) : BarcodeLookup

    /** A product from Open Food Facts that isn't saved yet. */
    data class Product(val product: ScannedProduct) : BarcodeLookup

    data object NotFound : BarcodeLookup

    /** Open Food Facts couldn't be reached, usually because the phone is offline. */
    data object Unavailable : BarcodeLookup
}
