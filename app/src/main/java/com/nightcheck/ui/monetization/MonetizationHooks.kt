package com.nightcheck.ui.monetization

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.nightcheck.ads.AdManager
import com.nightcheck.ui.paywall.LimitReachedDialog
import com.nightcheck.ui.paywall.PaywallReason
import com.nightcheck.ui.paywall.PaywallSheet

/**
 * Drop-in helper composable that handles the interstitial → paywall dialog → sheet chain.
 *
 * Usage in a screen:
 *
 *   MonetizationHooks(
 *       showTaskLimitDialog       = uiState.showTaskLimitDialog,
 *       showPaywall               = uiState.showPaywall,
 *       shouldShowInterstitial    = uiState.shouldShowSessionInterstitial,
 *       paywallReason             = PaywallReason.TaskLimit,
 *       onDismissLimitDialog      = viewModel::dismissTaskLimitDialog,
 *       onUpgradeFromLimitDialog  = viewModel::openPaywallFromLimit,
 *       onDismissPaywall          = viewModel::dismissPaywall,
 *       onInterstitialShown       = viewModel::onSessionInterstitialShown,
 *       adManager                 = adManager,
 *       onContinueAfterInterstitial = { /* navigate or save */ }
 *   )
 */
@Composable
fun MonetizationHooks(
    showLimitDialog: Boolean,
    showPaywall: Boolean,
    shouldShowInterstitial: Boolean,
    paywallReason: PaywallReason,
    limitDialogTitle: String,
    limitDialogMessage: String,
    onDismissLimitDialog: () -> Unit,
    onUpgradeFromLimitDialog: () -> Unit,
    onDismissPaywall: () -> Unit,
    onInterstitialShown: () -> Unit,
    adManager: AdManager,
    onContinueAfterInterstitial: () -> Unit = {}
) {
    val activity = LocalContext.current as? Activity

    // ── Session interstitial trigger ───────────────────────────────────────
    LaunchedEffect(shouldShowInterstitial) {
        if (shouldShowInterstitial && activity != null) {
            adManager.showInterstitialIfReady(activity) {
                onInterstitialShown()
                onContinueAfterInterstitial()
            }
        }
    }

    // ── Limit dialog ──────────────────────────────────────────────────────
    if (showLimitDialog) {
        LimitReachedDialog(
            title    = limitDialogTitle,
            message  = limitDialogMessage,
            onUpgrade = onUpgradeFromLimitDialog,
            onDismiss = onDismissLimitDialog
        )
    }

    // ── Paywall sheet ──────────────────────────────────────────────────────
    if (showPaywall) {
        PaywallSheet(
            reason    = paywallReason,
            onDismiss = onDismissPaywall
        )
    }
}