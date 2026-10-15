package org.thelazybattley.macrotrack.domain.usecase.food

import org.thelazybattley.macrotrack.domain.model.BarcodeLookup
import org.thelazybattley.macrotrack.domain.repository.FoodRepository
import org.thelazybattley.macrotrack.domain.repository.ProductRepository

/** Finds a barcode in the user's own foods first, then in Open Food Facts. */
class LookUpBarcodeUseCase(
    private val foodRepository: FoodRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(barcode: String): BarcodeLookup {
        foodRepository.getFoodByBarcode(barcode = barcode)?.let { return BarcodeLookup.Saved(food = it) }
        return productRepository.getProduct(barcode = barcode).fold(
            onSuccess = { product -> product?.let { BarcodeLookup.Product(product = it) } ?: BarcodeLookup.NotFound },
            onFailure = { BarcodeLookup.Unavailable }
        )
    }
}
