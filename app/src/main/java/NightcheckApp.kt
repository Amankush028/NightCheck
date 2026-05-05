package com.nightcheck

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.nightcheck.ads.AdManager
import com.nightcheck.billing.BillingManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NightcheckApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    // Both are @Singleton — injecting them here triggers their init {} blocks.
    @Inject lateinit var billingManager: BillingManager
    @Inject lateinit var adManager: AdManager

    override fun onCreate() {
        super.onCreate()
        // Initialize AdMob SDK as early as possible
        adManager.initialize()
        // Eagerly pre-load interstitial so it's ready by the first trigger
        adManager.preloadInterstitial()
        // BillingManager connects automatically in its init {} block
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}