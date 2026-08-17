package com.mknlabs.expensetracker.ui.components

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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.monetization.AdPlacement
import com.mknlabs.expensetracker.ui.theme.surfaceGradient
import dagger.hilt.EntryPoints

import com.mknlabs.expensetracker.di.MonetizationEntryPoint

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * A real implementation of the Native Ad component that integrates with AdMob.
 * Blends seamlessly into the UI with the "Fintech Premium" aesthetic.
 *
 * Phase 2 (ADS_UI_JANK_FIX_PLAN §5):
 * - Renders through the `AndroidView(factory, update, onReset, onRelease)` recycle overload
 *   (Compose UI 1.4.0+, this app is on 1.11.x), so inside a LazyColumn the inflated
 *   [NativeAdView] is **pooled and re-bound** on scroll entries instead of re-inflated —
 *   eliminating the main-thread `LayoutInflater` cost on every scroll (finding #1).
 * - Asset views are found **once** in [factory] and cached in [NativeAdViewHolder]; `update`
 *   only refreshes theme colors (no findViewById, no re-populate) per the plan.
 * - Ad lifecycle is owned by the coordinator via retain/release: a composed card retains the
 *   placement, and release never destroys the cached ad (it is the "cached next ad" — Phase 1
 *   measured destroy-on-scroll-out regressing scrollTransactions_free P90 17.1 → 31.4 ms);
 *   unused ads are destroyed by the coordinator's hourly refresh sweep instead.
 */
@Composable
fun NativeAdCard(
    placement: AdPlacement,
    modifier: Modifier = Modifier,
    // When true, renders the tall media-first layout (native_ad_large_layout.xml)
    // used on wide/tablet windows; otherwise the compact banner row.
    large: Boolean = false
) {
    // In preview/design mode, show a shimmer placeholder
    if (LocalInspectionMode.current) {
        if (large) NativeAdLargeShimmer() else NativeAdShimmer()
        return
    }

    val context = LocalContext.current
    val adsCoordinator = remember {
        EntryPoints.get(context.applicationContext, MonetizationEntryPoint::class.java).adsCoordinator()
    }

    // Phase 2: retain while any card holds this placement; release when it leaves composition.
    // Release never destroys — the freshly-shown ad stays cached for instant re-entry, and the
    // coordinator's hourly refresh sweep destroys unused ads to bound memory (finding #6).
    DisposableEffect(placement) {
        adsCoordinator.retainNativeAd(placement)
        onDispose {
            adsCoordinator.releaseNativeAd(placement)
        }
    }

    var nativeAd by remember(placement) { mutableStateOf<NativeAd?>(adsCoordinator.getNativeAd(placement)) }
    var isLoading by remember(placement) { mutableStateOf(nativeAd == null) }

    // Theme-aware colors from Compose
    val headlineColor = MaterialTheme.colorScheme.onSurface
    val bodyColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(placement) {
        if (nativeAd == null) {
            adsCoordinator.loadNativeAd(placement) { ad ->
                nativeAd = ad
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        // Bounded shimmer: animated sweep for the first 1 s, then static skeleton.
        if (large) {
            AdLoadingShimmer(skeleton = { NativeAdLargeShimmer() })
        } else {
            AdLoadingShimmer()
        }
    } else {
        nativeAd?.let { ad ->
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceGradient())
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        // Inflate once per view instance (recycled afterwards by the LazyColumn).
                        val adView = LayoutInflater.from(ctx)
                            .inflate(
                                if (large) R.layout.native_ad_large_layout else R.layout.native_ad_banner_layout,
                                null
                            ) as NativeAdView
                        val holder = NativeAdViewHolder(adView)
                        adView.tag = holder
                        holder.applyColors(headlineColor, bodyColor, primaryColor)
                        holder.bind(ad)
                        adView
                    },
                    update = { adView ->
                        // Phase 2: no findViewById, no re-populate. Only refresh theme colors and
                        // re-bind if the coordinator replaced the underlying ad instance.
                        val holder = adView.tag as? NativeAdViewHolder ?: return@AndroidView
                        holder.applyColors(headlineColor, bodyColor, primaryColor)
                        if (holder.boundAd !== ad) holder.bind(ad)
                    },
                    onReset = { adView ->
                        // Recycled view is about to be re-attached to the same placement.
                        // Re-apply colors; `update` follows immediately with the (same) ad.
                        (adView.tag as? NativeAdViewHolder)?.applyColors(headlineColor, bodyColor, primaryColor)
                    },
                    onRelease = { adView ->
                        // View permanently leaving composition: drop the reference so it never
                        // renders a coordinator-destroyed ad. The ad itself stays owned by the
                        // coordinator (retain/release + hourly sweep manage its lifetime).
                        (adView.tag as? NativeAdViewHolder)?.clear()
                    }
                )
            }
        }
    }
}

/**
 * Cached references to the asset views of one inflated [NativeAdView].
 *
 * Phase 2 (ADS_UI_JANK_FIX_PLAN §5): the previous implementation ran 8× findViewById + full
 * re-population inside the `AndroidView.update` lambda — i.e. on **every recomposition**. This
 * holder looks the views up once at inflation time and keeps the currently-bound [NativeAd], so
 * `update` only refreshes theme colors and re-binds when the coordinator swaps the ad instance.
 */
private class NativeAdViewHolder(private val adView: NativeAdView) {
    private val headlineView: TextView = adView.findViewById(R.id.ad_headline)
    private val bodyView: TextView = adView.findViewById(R.id.ad_body)
    private val callToActionView: Button = adView.findViewById(R.id.ad_call_to_action)
    private val iconView: ImageView = adView.findViewById(R.id.ad_app_icon)
    private val priceView: TextView = adView.findViewById(R.id.ad_price)
    private val starRatingView: RatingBar = adView.findViewById(R.id.ad_stars)
    private val storeView: TextView = adView.findViewById(R.id.ad_store)
    private val advertiserView: TextView = adView.findViewById(R.id.ad_advertiser)
    // Only present in the large (media-first) layout; null for the compact banner.
    private val mediaView: MediaView? = adView.findViewById(R.id.ad_media)

    /** The ad currently bound to [adView]; null once [clear] has run. */
    var boundAd: NativeAd? = null
        private set

    fun applyColors(headline: Color, body: Color, primary: Color) {
        headlineView.setTextColor(headline.toArgb())
        bodyView.setTextColor(body.toArgb())
        callToActionView.setTextColor(primary.toArgb())
    }

    fun bind(nativeAd: NativeAd) {
        // Register the asset views with the SDK so clicks/impressions keep working (compliance).
        adView.headlineView = headlineView
        adView.bodyView = bodyView
        adView.callToActionView = callToActionView
        adView.iconView = iconView
        adView.priceView = priceView
        adView.starRatingView = starRatingView
        adView.storeView = storeView
        adView.advertiserView = advertiserView

        // Large layout only: show the media (image/video) when the ad provides it.
        // The media area itself is sized to a 16:9 ratio in the layout XML, so images
        // and video ads fill it without letterboxing.
        mediaView?.let { media ->
            val mediaContent = nativeAd.mediaContent
            if (mediaContent != null) {
                media.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                media.mediaContent = mediaContent
                media.visibility = View.VISIBLE
                adView.mediaView = media
            } else {
                media.visibility = View.GONE
                adView.mediaView = null
            }
        }

        // The headline is guaranteed to be in every NativeAd.
        headlineView.text = nativeAd.headline

        // These assets aren't guaranteed to be in every NativeAd, so it's important to check
        // before trying to display them.
        if (nativeAd.body == null) {
            bodyView.visibility = View.INVISIBLE
        } else {
            bodyView.visibility = View.VISIBLE
            bodyView.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            callToActionView.visibility = View.INVISIBLE
        } else {
            callToActionView.visibility = View.VISIBLE
            callToActionView.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            iconView.visibility = View.GONE
        } else {
            iconView.setImageDrawable(nativeAd.icon?.drawable)
            iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            priceView.visibility = View.GONE
        } else {
            priceView.visibility = View.VISIBLE
            priceView.text = nativeAd.price
        }

        if (nativeAd.store == null) {
            storeView.visibility = View.GONE
        } else {
            storeView.visibility = View.VISIBLE
            storeView.text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            starRatingView.visibility = View.GONE
        } else {
            starRatingView.rating = nativeAd.starRating!!.toFloat()
            starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            advertiserView.visibility = View.GONE
        } else {
            advertiserView.text = nativeAd.advertiser
            advertiserView.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
        boundAd = nativeAd
    }

    /** Drops the bound-ad reference (called when the view permanently leaves composition). */
    fun clear() {
        boundAd = null
    }
}

