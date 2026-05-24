package com.mkn0079.expensetracker.ui.components

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.monetization.AdsCoordinator
import com.mkn0079.expensetracker.ui.theme.surfaceGradient
import dagger.hilt.EntryPoints

import com.mkn0079.expensetracker.di.MonetizationEntryPoint

/**
 * A real implementation of the Native Ad component that integrates with AdMob.
 * Blends seamlessly into the UI with the "Fintech Premium" aesthetic.
 */
@Composable
fun NativeAdCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adsCoordinator = remember {
        EntryPoints.get(context.applicationContext, MonetizationEntryPoint::class.java).adsCoordinator()
    }
    
    var nativeAd by remember { mutableStateOf<NativeAd?>(adsCoordinator.getNativeAd()) }
    var isLoading by remember { mutableStateOf(nativeAd == null) }

    LaunchedEffect(Unit) {
        if (nativeAd == null) {
            adsCoordinator.loadNativeAd { ad ->
                nativeAd = ad
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        NativeAdShimmer()
    } else {
        nativeAd?.let { ad ->
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(surfaceGradient())
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        val adView = LayoutInflater.from(ctx)
                            .inflate(R.layout.native_ad_layout, null) as NativeAdView
                        populateNativeAdView(ad, adView)
                        adView
                    },
                    update = { adView ->
                        populateNativeAdView(ad, adView)
                    }
                )
            }
        }
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    // Set other ad assets.
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    adView.priceView = adView.findViewById(R.id.ad_price)
    adView.starRatingView = adView.findViewById(R.id.ad_stars)
    adView.storeView = adView.findViewById(R.id.ad_store)
    adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

    // The headline is guaranteed to be in every NativeAd.
    (adView.headlineView as TextView).text = nativeAd.headline

    // These assets aren't guaranteed to be in every NativeAd, so it's important to check before trying to display them.
    if (nativeAd.body == null) {
        adView.bodyView?.visibility = View.INVISIBLE
    } else {
        adView.bodyView?.visibility = View.VISIBLE
        (adView.bodyView as TextView).text = nativeAd.body
    }

    if (nativeAd.callToAction == null) {
        adView.callToActionView?.visibility = View.INVISIBLE
    } else {
        adView.callToActionView?.visibility = View.VISIBLE
        (adView.callToActionView as Button).text = nativeAd.callToAction
        // Apply some styling to the button if needed, but XML should handle it mostly.
    }

    if (nativeAd.icon == null) {
        adView.iconView?.visibility = View.GONE
    } else {
        (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
        adView.iconView?.visibility = View.VISIBLE
    }

    if (nativeAd.price == null) {
        adView.priceView?.visibility = View.GONE
    } else {
        adView.priceView?.visibility = View.VISIBLE
        (adView.priceView as TextView).text = nativeAd.price
    }

    if (nativeAd.store == null) {
        adView.storeView?.visibility = View.GONE
    } else {
        adView.storeView?.visibility = View.VISIBLE
        (adView.storeView as TextView).text = nativeAd.store
    }

    if (nativeAd.starRating == null) {
        adView.starRatingView?.visibility = View.GONE
    } else {
        (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
        adView.starRatingView?.visibility = View.VISIBLE
    }

    if (nativeAd.advertiser == null) {
        adView.advertiserView?.visibility = View.GONE
    } else {
        (adView.advertiserView as TextView).text = nativeAd.advertiser
        adView.advertiserView?.visibility = View.VISIBLE
    }

    // This method tells the Google Mobile Ads SDK that you have finished populating your
    // native ad view with this native ad.
    adView.setNativeAd(nativeAd)
}

