package org.thelazybattley.macrotrack.features.scanbarcode

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
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
    /** The day a found food is logged to: today, or a past day opened in Food Log. */
    val logDate: LocalDate = getCurrentDate(),
    val mode: ScanMode = ScanMode.SCANNING,
    val barcode: String? = null,
    val manualBarcode: String = "",
    val scanned: ScannedFood? = null,
    /** The name the food is saved under; editable until it's in the user's foods. */
    val name: String = "",
    /** The portion being logged, in [Food.unit]. */
    val amount: Double = 0.0,
    /** Macros for [amount]. */
    val portionMacros: FoodMacros? = null,
    val remainingCalories: Int? = null,
    val isTorchOn: Boolean = false,
    val destinationRoute: String? = null,
    /** The portion was logged. */
    val isSaved: Boolean = false,
    /** The food was kept for later, so the scanner closes back to the food list. */
    val isClosed: Boolean = false
) {
    val isSheetOpen: Boolean get() = mode != ScanMode.SCANNING

    val canSave: Boolean get() = scanned?.isInMyFoods == true || name.isNotBlank()

    val isManualBarcodeValid: Boolean get() = normalizeBarcode(raw = manualBarcode) != null

    val remainingAfterAdding: Int?
        get() = remainingCalories?.let { remaining -> remaining - (portionMacros?.calories ?: 0) }

    /** Grams and millilitres move in steps of 5; pieces and other units in halves. */
    val amountStep: Double
        get() = if (scanned?.food?.unit?.lowercase() in setOf("g", "ml")) 5.0 else 0.5
}

/**
 * A food the barcode matched. [food] holds the macros for [Food.weight]. [isInMyFoods] is false for an Open Food
 * Facts product until the user saves or logs it.
 */
data class ScannedFood(
    val food: Food,
    val brand: String?,
    val isInMyFoods: Boolean,
    val servingLabel: String?,
    val servings: List<ServingOption>
)

/** A quick portion pick. [label] is the package's own serving text; [isHalf] marks half of a serving. */
data class ServingOption(
    val amount: Double,
    val label: String? = null,
    val isHalf: Boolean = false
)
