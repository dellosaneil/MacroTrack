package org.thelazybattley.macrotrack.core.billing

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent

/** Sells "MacroTrack Pro" as a one-time Google Play in-app product. */
class PlayBillingService(
    context: Context,
    private val telemetry: Telemetry
) : BillingService, PurchasesUpdatedListener {

    private val application = context.applicationContext as Application
    private val scope = CoroutineScope(context = SupervisorJob() + Dispatchers.Main.immediate)
    private val connectionLock = Mutex()
    private var resumedActivity: WeakReference<Activity>? = null
    private var productDetails: ProductDetails? = null

    /** The store's latest answer, reported when a purchase cannot start. */
    private var lastResult: BillingResult? = null

    private val client = BillingClient.newBuilder(application)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    private val _purchaseState = MutableStateFlow(value = ProPurchaseState.UNKNOWN)
    override val purchaseState: StateFlow<ProPurchaseState> = _purchaseState.asStateFlow()

    private val _price = MutableStateFlow<String?>(value = null)
    override val price: StateFlow<String?> = _price.asStateFlow()

    override val isSupported: Boolean = true

    init {
        // The purchase sheet needs the activity on screen, which shared Compose code cannot hand over.
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                resumedActivity = WeakReference(activity)
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (resumedActivity?.get() === activity) resumedActivity = null
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        })
    }

    override suspend fun refresh() {
        if (!connect()) return
        loadProductDetails()
        val result = client.queryPurchasesAsync(
            params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        )
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            handlePurchases(purchases = result.purchasesList, isCompleteList = true)
        }
    }

    override suspend fun launchPurchase(): PurchaseLaunchResult {
        if (!connect()) return unavailable(step = "connect")
        val details = productDetails ?: loadProductDetails() ?: return unavailable(step = "product_details")
        val activity = resumedActivity?.get() ?: return unavailable(step = "activity")
        val offer = details.offer ?: return unavailable(step = "offer")
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply { offer.offerToken?.let(::setOfferToken) }
            .build()
        val result = client.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productParams)).build()
        )
        return when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> PurchaseLaunchResult.STARTED
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                refresh()
                PurchaseLaunchResult.ALREADY_OWNED
            }
            else -> unavailable(step = "launch", result = result)
        }
    }

    /** Records why the purchase sheet could not open, so a "Couldn't reach the store" report can be traced. */
    private fun unavailable(step: String, result: BillingResult? = lastResult): PurchaseLaunchResult {
        val code = result?.responseCode
        Log.w(TAG, "Purchase unavailable at $step: code=$code, ${result?.debugMessage}")
        telemetry.logEvent(
            name = TelemetryEvent.PURCHASE_UNAVAILABLE,
            params = buildMap {
                put(TelemetryEvent.PARAM_STEP, step)
                if (code != null) put(TelemetryEvent.PARAM_CODE, code)
            }
        )
        return PurchaseLaunchResult.UNAVAILABLE
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> scope.launch {
                handlePurchases(purchases = purchases.orEmpty(), isCompleteList = false)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> scope.launch { refresh() }
            else -> Unit
        }
    }

    private suspend fun connect(): Boolean = connectionLock.withLock {
        if (client.isReady) return@withLock true
        suspendCancellableCoroutine { continuation ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    lastResult = result
                    if (continuation.isActive) {
                        continuation.resume(value = result.responseCode == BillingClient.BillingResponseCode.OK)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    if (continuation.isActive) continuation.resume(value = false)
                }
            })
        }
    }

    private suspend fun loadProductDetails(): ProductDetails? {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(BillingService.PRO_PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val result = client.queryProductDetails(
            params = QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        )
        lastResult = result.billingResult
        val details = result.productDetailsList?.firstOrNull { it.productId == BillingService.PRO_PRODUCT_ID }
        if (details != null) {
            productDetails = details
            _price.value = details.offer?.formattedPrice
        }
        return details
    }

    /**
     * Products made with Play Console's newer purchase options only list their offers in
     * [ProductDetails.getOneTimePurchaseOfferDetailsList]; the single-offer getter is null for them unless the
     * option is marked backwards compatible.
     */
    private val ProductDetails.offer: ProductDetails.OneTimePurchaseOfferDetails?
        get() = oneTimePurchaseOfferDetailsList?.firstOrNull() ?: oneTimePurchaseOfferDetails

    /**
     * [isCompleteList] is true for the store's full list of owned items, where a missing Pro purchase means it
     * was never bought or was refunded; a purchase update only lists what just changed.
     */
    private suspend fun handlePurchases(purchases: List<Purchase>, isCompleteList: Boolean) {
        val purchase = purchases.firstOrNull { BillingService.PRO_PRODUCT_ID in it.products }
        _purchaseState.value = when (purchase?.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                // Play refunds purchases that are not acknowledged within three days.
                if (!purchase.isAcknowledged) {
                    client.acknowledgePurchase(
                        params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                    )
                }
                ProPurchaseState.OWNED
            }
            Purchase.PurchaseState.PENDING -> ProPurchaseState.PENDING
            else -> if (isCompleteList) ProPurchaseState.NOT_OWNED else _purchaseState.value
        }
    }

    private companion object {
        const val TAG = "PlayBillingService"
    }
}
