package org.thelazybattley.macrotrack.core.billing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Stands in until the App Store purchase is built; the trial still works, buying Pro does not yet. */
class UnavailableBillingService : BillingService {

    override val isSupported: Boolean = false

    override val purchaseState: StateFlow<ProPurchaseState> = MutableStateFlow(value = ProPurchaseState.UNKNOWN)

    override val price: StateFlow<String?> = MutableStateFlow(value = null)

    override suspend fun refresh() = Unit

    override suspend fun launchPurchase(): PurchaseLaunchResult = PurchaseLaunchResult.UNAVAILABLE
}
