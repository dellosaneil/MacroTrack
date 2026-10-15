package org.thelazybattley.macrotrack.core.billing

import kotlinx.coroutines.flow.StateFlow

/** What the store says about the Pro purchase. */
enum class ProPurchaseState {
    /** The store has not answered yet, or cannot be reached; keep whatever was saved last. */
    UNKNOWN,
    NOT_OWNED,

    /** Paid with a slow method (like cash at a store); Pro unlocks once the payment clears. */
    PENDING,
    OWNED
}

enum class PurchaseLaunchResult {
    STARTED,
    ALREADY_OWNED,
    UNAVAILABLE
}

/** The app store's one-time "MacroTrack Pro" product. Google Play on Android; not available on iOS yet. */
interface BillingService {

    val isSupported: Boolean

    val purchaseState: StateFlow<ProPurchaseState>

    /** The store's localised price, like "$4.99", or null until it loads. */
    val price: StateFlow<String?>

    /** Asks the store again for the purchase and price, for app start and "Restore purchase". */
    suspend fun refresh()

    /** Opens the store's purchase sheet. The outcome arrives later through [purchaseState]. */
    suspend fun launchPurchase(): PurchaseLaunchResult

    companion object {
        const val PRO_PRODUCT_ID = "macrotrack_pro"
    }
}
