package org.thelazybattley.macrotrack.domain.usecase.food

import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.BarcodeLookup
import org.thelazybattley.macrotrack.domain.repository.FoodRepository
import org.thelazybattley.macrotrack.domain.repository.ProductRepository

/** Finds a barcode in the user's own foods first, then in Open Food Facts. */
class LookUpBarcodeUseCase(
    private val foodRepository: FoodRepository,
    private val productRepository: ProductRepository,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(barcode: String): BarcodeLookup {
        val lookup = foodRepository.getFoodByBarcode(barcode = barcode)?.let { BarcodeLookup.Saved(food = it) }
            ?: productRepository.getProduct(barcode = barcode).fold(
                onSuccess = { product -> product?.let { BarcodeLookup.Product(product = it) } ?: BarcodeLookup.NotFound },
                onFailure = { BarcodeLookup.Unavailable }
            )
        telemetry.logEvent(name = TelemetryEvent.BARCODE_LOOKUP, params = mapOf(TelemetryEvent.PARAM_RESULT to lookup.resultName))
        return lookup
    }

    private val BarcodeLookup.resultName: String
        get() = when (this) {
            is BarcodeLookup.Saved -> "saved"
            is BarcodeLookup.Product -> "found"
            BarcodeLookup.NotFound -> "not_found"
            BarcodeLookup.Unavailable -> "unavailable"
        }
}
