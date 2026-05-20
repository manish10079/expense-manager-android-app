package com.mkn0079.expensetracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * A lifecycle-aware Composable wrapper for AdMob Banner Ads.
 * This component handles the initialization and loading of the AdView.
 * 
 * @param modifier Modifier for the container.
 * @param adUnitId The AdMob Ad Unit ID (Defaults to Google Test ID).
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // Google Test Banner ID
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(adUnitId)
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            // Update logic can be added here if the ad needs to be refreshed manually
        }
    )
}
