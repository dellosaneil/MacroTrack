package org.thelazybattley.macrotrack.domain.repository

import org.thelazybattley.macrotrack.domain.model.OnlineFood
import org.thelazybattley.macrotrack.domain.model.OnlineSearchPage
import org.thelazybattley.macrotrack.domain.model.ScannedProduct

interface ProductRepository {

    /** The product with this barcode, or null when there isn't one with usable nutrition. Fails when offline. */
    suspend fun getProduct(barcode: String): Result<ScannedProduct?>

    /** The same look-up as [getProduct], with the extra nutrients and where they come from. */
    suspend fun getOnlineFood(barcode: String): Result<OnlineFood?>

    /** Packaged foods matching [query], [page] counting from 1. Only the query is sent. Fails when offline. */
    suspend fun searchProducts(query: String, page: Int): Result<OnlineSearchPage>
}
