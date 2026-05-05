package com.nightcheck.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lightweight ViewModel that any screen can inject to read premium state.
 *
 * Scoped to the Activity via:
 *   val premiumVm: PremiumViewModel = hiltViewModel(
 *       viewModelStoreOwner = LocalContext.current as ComponentActivity
 *   )
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    premiumCache: PremiumCache,
    val billingManager: BillingManager
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = premiumCache.isPremiumFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Call in onResume to verify purchases are still active. */
    fun refreshPurchaseState() {
        viewModelScope.launch {
            billingManager.queryActivePurchases()
        }
    }
}