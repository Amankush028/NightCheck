package com.nightcheck.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.emptyList
import kotlin.coroutines.resume

/**
 * Wraps Google Play Billing Library v6.
 *
 * - Connects on first use and auto-reconnects on disconnection.
 * - Exposes [isPremium] and [products] as hot StateFlows.
 * - On successful purchase / restore, writes to [PremiumCache] so the rest of
 *   the app doesn't need to touch BillingClient directly.
 *
 * SKU conventions:
 *   nightcheck_premium_monthly  — $1.99/month subscription
 *   nightcheck_premium_yearly   — $9.99/year subscription
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val premiumCache: PremiumCache
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Public state ──────────────────────────────────────────────────────────

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    // ── BillingClient ─────────────────────────────────────────────────────────

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        // Seed UI from local cache immediately (no billing round-trip needed)
        scope.launch {
            _isPremium.value = premiumCache.isCurrentlyPremium()
        }
        connectAndQuery()
    }

    // ── Connection ────────────────────────────────────────────────────────────

    private fun connectAndQuery() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        queryActivePurchases()
                        queryProductDetails()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry — simple backoff would be production-grade but scope is fine for now
                connectAndQuery()
            }
        })
    }

    // ── Query existing purchases (runs on every cold start & foreground) ──────

    suspend fun queryActivePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val result = billingClient.queryPurchasesAsync(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val active = result.purchasesList.any { purchase ->
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.products.any { it in SUBSCRIPTION_SKUS }
            }
            _isPremium.value = active
            premiumCache.setPremium(active)
        }
    }

    // ── Query product details for the paywall screen ──────────────────────────

    private suspend fun queryProductDetails() {
        val productList = SUBSCRIPTION_SKUS.map { sku ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = billingClient.queryProductDetailsAsync(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _products.value = result.productDetailsList ?: emptyList()
        }
    }

    // ── Launch purchase flow ──────────────────────────────────────────────────

    fun launchPurchase(activity: Activity, productDetails: ProductDetails, offerToken: String) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        _billingState.value = BillingState.Processing
        billingClient.launchBillingFlow(activity, flowParams)
    }

    // ── PurchasesUpdatedListener ──────────────────────────────────────────────

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    scope.launch { handlePurchase(purchase) }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingState.value = BillingState.Cancelled
            }
            else -> {
                _billingState.value = BillingState.Error(result.debugMessage)
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(ackParams)
        }
        val isPremiumProduct = purchase.products.any { it in SUBSCRIPTION_SKUS }
        if (isPremiumProduct) {
            _isPremium.value = true
            premiumCache.setPremium(true)
            _billingState.value = BillingState.Success
        }
    }

    companion object {
        val SUBSCRIPTION_SKUS = listOf(
            "nightcheck_premium_monthly",
            "nightcheck_premium_yearly"
        )
        // Request code constants for identifying offers
        const val SKU_MONTHLY = "nightcheck_premium_monthly"
        const val SKU_YEARLY  = "nightcheck_premium_yearly"
    }
}

sealed class BillingState {
    data object Idle       : BillingState()
    data object Processing : BillingState()
    data object Success    : BillingState()
    data object Cancelled  : BillingState()
    data class  Error(val message: String) : BillingState()
}