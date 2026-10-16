package org.thelazybattley.macrotrack.features.pro

import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.core.billing.BillingService
import org.thelazybattley.macrotrack.core.billing.ProPurchaseState
import org.thelazybattley.macrotrack.core.billing.PurchaseLaunchResult
import org.thelazybattley.macrotrack.domain.usecase.pro.DismissTrialEndedNoticeUseCase
import org.thelazybattley.macrotrack.domain.usecase.pro.GetProStatusUseCase
import org.thelazybattley.macrotrack.domain.usecase.pro.IsTrialEndedNoticeDismissedUseCase
import org.thelazybattley.macrotrack.domain.usecase.pro.SetProPurchasedUseCase
import org.thelazybattley.macrotrack.domain.usecase.pro.StartProTrialUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase

/** Lives as long as the app's navigation, so every screen shares one answer to "is Pro open?". */
class ProViewModel(
    getProStatusUseCase: GetProStatusUseCase,
    getAllRecipeUseCase: GetAllRecipeUseCase,
    isTrialEndedNoticeDismissedUseCase: IsTrialEndedNoticeDismissedUseCase,
    startProTrialUseCase: StartProTrialUseCase,
    private val setProPurchasedUseCase: SetProPurchasedUseCase,
    private val dismissTrialEndedNoticeUseCase: DismissTrialEndedNoticeUseCase,
    private val billingService: BillingService,
    private val telemetry: Telemetry
) : ViewModel(), ProCallbacks {

    private val ui = MutableStateFlow(value = UiState())

    /** Set while the purchase sheet is open, so a purchase that lands afterwards gets a thank-you. */
    private var isAwaitingPurchase = false

    val state = combine(
        flow = getProStatusUseCase(),
        flow2 = flow { emitAll(getAllRecipeUseCase()) }.map { it.size },
        flow3 = combine(flow = billingService.price, flow2 = billingService.purchaseState, transform = ::Pair),
        flow4 = isTrialEndedNoticeDismissedUseCase(),
        flow5 = ui
    ) { status, recipeCount, (price, purchaseState), isNoticeDismissed, ui ->
        ProViewState(
            status = status,
            recipeCount = recipeCount,
            price = price,
            purchaseState = purchaseState,
            isStoreSupported = billingService.isSupported,
            isTrialEndedNoticeDismissed = isNoticeDismissed,
            isBusy = ui.isBusy,
            message = ui.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ProViewState(isStoreSupported = billingService.isSupported)
    )

    init {
        viewModelScope.launch {
            startProTrialUseCase()
            billingService.refresh()
        }
        billingService.purchaseState.onEach { purchaseState ->
            when (purchaseState) {
                ProPurchaseState.OWNED -> {
                    setProPurchasedUseCase(isPurchased = true)
                    if (isAwaitingPurchase) {
                        telemetry.logEvent(name = TelemetryEvent.PRO_UNLOCKED)
                        showMessage(message = ProMessage.UNLOCKED)
                    }
                    isAwaitingPurchase = false
                }
                ProPurchaseState.PENDING -> if (isAwaitingPurchase) showMessage(message = ProMessage.PENDING)
                // Only a full answer from the store clears Pro, which is how a refund takes it away again.
                ProPurchaseState.NOT_OWNED -> setProPurchasedUseCase(isPurchased = false)
                ProPurchaseState.UNKNOWN -> Unit
            }
        }.launchIn(scope = viewModelScope)
    }

    override fun onUnlockClicked() {
        if (ui.value.isBusy) return
        viewModelScope.launch {
            ui.update { it.copy(isBusy = true) }
            isAwaitingPurchase = true
            telemetry.logEvent(name = TelemetryEvent.PURCHASE_STARTED)
            when (billingService.launchPurchase()) {
                PurchaseLaunchResult.STARTED -> Unit
                PurchaseLaunchResult.ALREADY_OWNED -> showMessage(message = ProMessage.RESTORED)
                PurchaseLaunchResult.UNAVAILABLE -> {
                    isAwaitingPurchase = false
                    showMessage(message = ProMessage.UNAVAILABLE)
                }
            }
            ui.update { it.copy(isBusy = false) }
        }
    }

    override fun onRestoreClicked() {
        if (ui.value.isBusy) return
        viewModelScope.launch {
            ui.update { it.copy(isBusy = true) }
            billingService.refresh()
            showMessage(
                message = when (billingService.purchaseState.value) {
                    ProPurchaseState.OWNED -> ProMessage.RESTORED
                    ProPurchaseState.PENDING -> ProMessage.PENDING
                    ProPurchaseState.NOT_OWNED -> ProMessage.NOTHING_TO_RESTORE
                    ProPurchaseState.UNKNOWN -> ProMessage.UNAVAILABLE
                }
            )
            ui.update { it.copy(isBusy = false) }
        }
    }

    override fun onTrialEndedNoticeDismissed() {
        viewModelScope.launch {
            dismissTrialEndedNoticeUseCase()
        }
    }

    override fun onMessageShown() {
        ui.update { it.copy(message = null) }
    }

    private fun showMessage(message: ProMessage) {
        ui.update { it.copy(message = message) }
    }

    private data class UiState(
        val isBusy: Boolean = false,
        val message: ProMessage? = null
    )
}
