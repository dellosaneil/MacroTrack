package org.thelazybattley.macrotrack.features.scanbarcode

import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.MealType

enum class ScanMode {
    SCANNING,
    LOOKING_UP,
    FOUND,
    NOT_FOUND,

    /** Open Food Facts couldn't be reached. */
    UNAVAILABLE,

    /** Typing the barcode number by hand. */
    MANUAL
}

data class ScanBarcodeViewState(
    val mealType: MealType = MealType.BREAKFAST,
    val mode: ScanMode = ScanMode.SCANNING,
    val barcode: String? = null,
    val manualBarcode: String = "",
    val scanned: ScannedFood? = null,
    /** The portion being logged, in [Food.unit]. */
    val amount: Double = 0.0,
    /** Macros for [amount]. */
    val portionMacros: FoodMacros? = null,
    val remainingCalories: Int? = null,
    val isTorchOn: Boolean = false,
    val destinationRoute: String? = null,
    val isSaved: Boolean = false
) {
    val isSheetOpen: Boolean
        get() = mode == ScanMode.FOUND || mode == ScanMode.NOT_FOUND || mode == ScanMode.UNAVAILABLE || mode == ScanMode.MANUAL

    val isManualBarcodeValid: Boolean get() = normalizeBarcode(raw = manualBarcode) != null

    val remainingAfterAdding: Int?
        get() = remainingCalories?.let { remaining -> remaining - (portionMacros?.calories ?: 0) }

    /** Grams and millilitres move in steps of 5; pieces and other units in halves. */
    val amountStep: Double
        get() = if (scanned?.food?.unit?.lowercase() in setOf("g", "ml")) 5.0 else 0.5
}

/**
 * A barcode that matched a food. [food] holds the macros for [Food.weight]; it's either one of the user's saved
 * foods or an Open Food Facts product that gets saved when it's logged or edited.
 */
data class ScannedFood(
    val food: Food,
    val brand: String?,
    val isSaved: Boolean,
    val servingLabel: String?,
    val servings: List<ServingOption>
)

/** A quick portion pick. [label] is the package's own serving text; [isHalf] marks half of a serving. */
data class ServingOption(
    val amount: Double,
    val label: String? = null,
    val isHalf: Boolean = false
)
