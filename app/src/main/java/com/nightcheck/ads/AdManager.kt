package com.nightcheck.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages AdMob ad loading and presentation.
 *
 * Uses TEST ad unit IDs by default — replace with real IDs before release.
 * Real IDs are injected via BuildConfig fields in build.gradle.kts.
 *
 * Interstitial flow:
 *  1. [preloadInterstitial] is called eagerly on app start (after billing check).
 *  2. [showInterstitialIfReady] shows if loaded; always reloads after dismiss/fail.
 */
@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // ── Ad unit IDs (test IDs — swap in release) ──────────────────────────────
    // In your build.gradle.kts add:
    //   buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-REAL_ID\"")
    // and reference it as BuildConfig.ADMOB_INTERSTITIAL_ID
    object AdUnitIds {
        // Google's official test IDs — safe to use during development
        const val INTERSTITIAL  = "ca-app-pub-3940256099942544/1033173712"
        const val BANNER        = "ca-app-pub-3940256099942544/6300978111"
        const val NATIVE        = "ca-app-pub-3940256099942544/2247696110"
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private val _interstitialReady = MutableStateFlow(false)
    val interstitialReady: StateFlow<Boolean> = _interstitialReady.asStateFlow()

    private var interstitialAd: InterstitialAd? = null

    // ── Init ──────────────────────────────────────────────────────────────────

    fun initialize() {
        MobileAds.initialize(context)
    }

    // ── Interstitial ──────────────────────────────────────────────────────────

    fun preloadInterstitial() {
        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdUnitIds.INTERSTITIAL,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd    = ad
                    _interstitialReady.value = true
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd           = null
                    _interstitialReady.value = false
                }
            }
        )
    }

    /**
     * Shows the interstitial if loaded. Always reloads after the ad closes or fails.
     * [onDismissed] is called when the user dismisses (or on failure).
     */
    fun showInterstitialIfReady(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            onDismissed()
            preloadInterstitial()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd           = null
                _interstitialReady.value = false
                preloadInterstitial()  // eager reload for next trigger
                onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd           = null
                _interstitialReady.value = false
                preloadInterstitial()
                onDismissed()
            }
        }
        ad.show(activity)
    }
}