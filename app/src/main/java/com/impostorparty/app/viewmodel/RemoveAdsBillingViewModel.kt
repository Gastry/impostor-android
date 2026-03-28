package com.impostorparty.app.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.impostorparty.app.ads.RemoveAdsBillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RemoveAdsBillingViewModel @Inject constructor(
    private val billingManager: RemoveAdsBillingManager,
) : ViewModel() {

    val uiState = billingManager.uiState

    fun refresh() {
        billingManager.refresh()
    }

    fun launchPurchase(activity: Activity) {
        billingManager.launchPurchase(activity)
    }

    fun clearMessage() {
        billingManager.clearMessage()
    }
}
