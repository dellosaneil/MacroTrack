package org.thelazybattley.macrotrack.features.pro

import org.thelazybattley.macrotrack.core.billing.ProPurchaseState
import org.thelazybattley.macrotrack.domain.model.ProStatus

enum class ProMessage {
    UNAVAILABLE,
    PENDING,
    UNLOCKED,
    RESTORED,
    NOTHING_TO_RESTORE
}

data class ProViewState(
    val status: ProStatus = ProStatus(),
    val recipeCount: Int = 0,
    val price: String? = null,
    val purchaseState: ProPurchaseState = ProPurchaseState.UNKNOWN,
    val isStoreSupported: Boolean = true,
    val isTrialEndedNoticeDismissed: Boolean = true,
    val isBusy: Boolean = false,
    val message: ProMessage? = null
)
