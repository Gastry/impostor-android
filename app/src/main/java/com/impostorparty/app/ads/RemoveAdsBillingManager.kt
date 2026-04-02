package com.impostorparty.app.ads

import android.app.Activity
import android.content.Context
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
import com.impostorparty.app.BuildConfig
import com.impostorparty.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class RemoveAdsBillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val productId = BuildConfig.REMOVE_ADS_PRODUCT_ID.trim()

    private val _uiState = MutableStateFlow(RemoveAdsPurchaseUiState())
    val uiState: StateFlow<RemoveAdsPurchaseUiState> = _uiState.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .enableAutoServiceReconnection()
        .build()

    private var currentProductDetails: ProductDetails? = null

    init {
        scope.launch {
            preferencesRepository.adsRemoved.collect { removed ->
                _uiState.update { it.copy(isAdsRemoved = removed) }
            }
        }
        refresh()
    }

    fun refresh() {
        if (productId.isBlank()) {
            currentProductDetails = null
            _uiState.value = RemoveAdsPurchaseUiState(
                isLoading = false,
                message = RemoveAdsPurchaseMessage.UNAVAILABLE,
            )
            return
        }

        scope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    message = if (it.message == RemoveAdsPurchaseMessage.PURCHASED) it.message else null,
                )
            }

            if (!ensureConnected()) {
                currentProductDetails = null
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isPurchaseAvailable = false,
                        priceLabel = null,
                        message = RemoveAdsPurchaseMessage.UNAVAILABLE,
                    )
                }
                return@launch
            }

            when (syncOwnedPurchases(showMessage = false)) {
                OwnedPurchaseState.OWNED -> {
                    currentProductDetails = null
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPurchaseAvailable = false,
                            hasPendingPurchase = false,
                            priceLabel = null,
                        )
                    }
                }

                OwnedPurchaseState.PENDING -> {
                    currentProductDetails = null
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPurchaseAvailable = false,
                            hasPendingPurchase = true,
                            priceLabel = null,
                        )
                    }
                }

                OwnedPurchaseState.NONE,
                OwnedPurchaseState.FAILED,
                -> {
                    queryProductDetails()
                }
            }
        }
    }

    fun launchPurchase(activity: Activity) {
        val productDetails = currentProductDetails
        val offerDetails = productDetails?.oneTimePurchaseOfferDetailsList?.firstOrNull()
        val offerToken = offerDetails?.offerToken
        if (productDetails == null || offerDetails == null || offerToken.isNullOrBlank()) {
            refresh()
            return
        }

        val billingResult = billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build(),
                    ),
                )
                .build(),
        )

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                _uiState.update {
                    it.copy(
                        isPurchaseInProgress = true,
                        message = null,
                    )
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _uiState.update { it.copy(isPurchaseInProgress = false) }
            }

            else -> {
                _uiState.update {
                    it.copy(
                        isPurchaseInProgress = false,
                        message = RemoveAdsPurchaseMessage.ERROR,
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                scope.launch {
                    syncOwnedPurchases(
                        purchases = purchases.orEmpty(),
                        showMessage = true,
                    )
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _uiState.update { it.copy(isPurchaseInProgress = false) }
            }

            else -> {
                _uiState.update {
                    it.copy(
                        isPurchaseInProgress = false,
                        message = RemoveAdsPurchaseMessage.ERROR,
                    )
                }
            }
        }
    }

    private suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) return true

        return suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (continuation.isActive) {
                            continuation.resume(
                                billingResult.responseCode == BillingClient.BillingResponseCode.OK,
                            )
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }
                },
            )
        }
    }

    private suspend fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()

        suspendCancellableCoroutine<Unit> { continuation ->
            billingClient.queryProductDetailsAsync(params) { billingResult, queryResult ->
                val details = if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryResult.productDetailsList.firstOrNull()
                } else {
                    null
                }
                currentProductDetails = details
                val offer = details?.oneTimePurchaseOfferDetailsList?.firstOrNull()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isPurchaseAvailable = offer != null,
                        hasPendingPurchase = false,
                        priceLabel = offer?.formattedPrice,
                        message = when {
                            offer == null -> RemoveAdsPurchaseMessage.UNAVAILABLE
                            it.message == RemoveAdsPurchaseMessage.PURCHASED -> it.message
                            else -> null
                        },
                    )
                }

                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }
    }

    private suspend fun syncOwnedPurchases(
        purchases: List<Purchase>? = null,
        showMessage: Boolean,
    ): OwnedPurchaseState {
        val ownedPurchases = purchases ?: queryExistingPurchases()
        if (ownedPurchases == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isPurchaseInProgress = false,
                )
            }
            return OwnedPurchaseState.FAILED
        }

        var hasPurchased = false
        var hasPending = false

        ownedPurchases
            .filter { productId in it.products }
            .forEach { purchase ->
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PURCHASED -> {
                        val acknowledged = purchase.isAcknowledged || acknowledgePurchase(purchase)
                        if (acknowledged) {
                            hasPurchased = true
                        }
                    }

                    Purchase.PurchaseState.PENDING -> hasPending = true
                }
            }

        if (hasPurchased) {
            preferencesRepository.saveAdsRemoved(true)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isPurchaseInProgress = false,
                    isPurchaseAvailable = false,
                    hasPendingPurchase = false,
                    priceLabel = null,
                    message = if (showMessage) RemoveAdsPurchaseMessage.PURCHASED else it.message,
                )
            }
            return OwnedPurchaseState.OWNED
        }

        preferencesRepository.saveAdsRemoved(false)
        _uiState.update {
            it.copy(
                isLoading = false,
                isPurchaseInProgress = false,
                hasPendingPurchase = hasPending,
                message = when {
                    hasPending && showMessage -> RemoveAdsPurchaseMessage.PENDING
                    !hasPending && showMessage && it.message == RemoveAdsPurchaseMessage.PURCHASED -> null
                    else -> it.message
                },
            )
        }
        return if (hasPending) OwnedPurchaseState.PENDING else OwnedPurchaseState.NONE
    }

    private suspend fun queryExistingPurchases(): List<Purchase>? {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        return suspendCancellableCoroutine { continuation ->
            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                val result = if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchases
                } else {
                    null
                }
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase): Boolean {
        if (purchase.isAcknowledged) return true

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        return suspendCancellableCoroutine { continuation ->
            billingClient.acknowledgePurchase(params) { billingResult ->
                if (continuation.isActive) {
                    continuation.resume(
                        billingResult.responseCode == BillingClient.BillingResponseCode.OK,
                    )
                }
            }
        }
    }

    private enum class OwnedPurchaseState {
        OWNED,
        PENDING,
        NONE,
        FAILED,
    }
}

data class RemoveAdsPurchaseUiState(
    val isLoading: Boolean = true,
    val isPurchaseAvailable: Boolean = false,
    val isPurchaseInProgress: Boolean = false,
    val isAdsRemoved: Boolean = false,
    val hasPendingPurchase: Boolean = false,
    val priceLabel: String? = null,
    val message: RemoveAdsPurchaseMessage? = null,
)

enum class RemoveAdsPurchaseMessage {
    PURCHASED,
    PENDING,
    ERROR,
    UNAVAILABLE,
}
