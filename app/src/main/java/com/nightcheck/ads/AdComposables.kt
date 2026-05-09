package com.nightcheck.ads

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAdView

// ── Banner Ad ─────────────────────────────────────────────────────────────────

/**
 * Standard 320×50 banner.
 * Only render this when [showAds] is true (i.e. user is on free tier).
 */
@Composable
fun BannerAdView(
    adUnitId: String = AdManager.AdUnitIds.BANNER,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
) {
    AndroidView(
        modifier = modifier,
        factory  = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}