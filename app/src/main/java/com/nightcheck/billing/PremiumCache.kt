package com.nightcheck.billing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local DataStore cache for premium status.
 *
 * This is the only source of truth the UI reads. [BillingManager] writes here
 * after every successful purchase query / purchase event. On reinstall, Play
 * Billing restores purchases and [BillingManager.queryActivePurchases()] will
 * set premium = true again after the billing client connects.
 */
@Singleton
class PremiumCache @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val IS_PREMIUM            = booleanPreferencesKey("is_premium")
        val LAST_VERIFIED_MILLIS  = longPreferencesKey("premium_last_verified")
    }

    val isPremiumFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[Keys.IS_PREMIUM] ?: false }

    suspend fun isCurrentlyPremium(): Boolean =
        dataStore.data.first()[Keys.IS_PREMIUM] ?: false

    suspend fun setPremium(premium: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_PREMIUM]           = premium
            prefs[Keys.LAST_VERIFIED_MILLIS] = System.currentTimeMillis()
        }
    }
}