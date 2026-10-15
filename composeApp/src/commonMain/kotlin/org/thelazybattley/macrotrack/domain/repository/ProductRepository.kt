package org.thelazybattley.macrotrack.domain.repository

import org.thelazybattley.macrotrack.domain.model.ScannedProduct

interface ProductRepository {

    /** The product with this barcode, or null when there isn't one with usable nutrition. Fails when offline. */
    suspend fun getProduct(barcode: String): Result<ScannedProduct?>
}
