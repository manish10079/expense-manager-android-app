package com.mknlabs.expensetracker.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.AdLoader
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Defines all locations where ads can be displayed.
 * Maps to unique AdMob unit IDs for granular analytics.
 */
enum class AdPlacement {
    HOME_DASHBOARD,
    HOME_BANNER,
    TRANSACTIONS_LIST,
    TRANSACTIONS_LIST_2,
    ANALYTICS_INSIGHTS,
    BUDGET_CALENDAR,
    SETTINGS_GENERAL
}

/**
 * Defines unique identifiers for Rewarded ads.
 */
enum class RewardedPlacement {
    FEATURE_UNLOCK,
    AD_FREE_ACCESS
}

/**
 * Defines unique identifiers for Interstitial ads.
 */
enum class InterstitialPlacement {
    DATA_ACTION
}

/**
 * Centrally manages ad loading, caching, display, and privacy consent (UMP).
 * Aligned with Phase 1 of the Monetization Roadmap.
 *
 * Phase 1 (ADS_UI_JANK_FIX_PLAN) native-ad lifecycle:
 * - **Preload all placements at SDK init** so screen composition never triggers a
 *   first-visit network ad request (finding #3).
 * - **Hourly refresh** — AdMob native ads expire after ~1 hour; the cache is refreshed
 *   at [NATIVE_AD_REFRESH_MILLIS] and never outlives the expiry.
 * - **Background construction** — `AdLoader` is built on [Dispatchers.IO] per AdMob
 *   guidance; GMA callbacks arrive on the main thread and are the only place the maps
 *   are mutated by the SDK.
 * - **Retain/release (Phase 2)** — screens `retain` a placement while visible and
 *   `release` when they leave. Release never destroys the cached `NativeAd`: the freshly
 *   shown ad is the "cached next ad" and must stay warm so an ad card scrolling back into
 *   a LazyColumn renders instantly (Phase 1 *measured* destroy-on-scroll-out regressing
 *   scrollTransactions_free P90 17.1 → 31.4 ms). Stale/unused ads are destroyed by the
 *   hourly refresh sweep instead (bounded memory — finding #6). The coordinator is the
 *   source of truth; screens are holders.
 * - **Multi-holder callbacks** — every caller of [loadNativeAd] is notified when the ad
 *   arrives, so two cards for the same placement (e.g. Analytics, finding #7) both
 *   resolve instead of one being stuck on the shimmer forever.
 * - **No retry from the failure callback** — failures are logged once; retries happen
 *   only on explicit triggers (next screen entry / foreground).
 *
 * Thread-safety: maps that can be touched from the SDK callbacks (main) and from the
 * refresh jobs (background) are `ConcurrentHashMap`; the in-flight-load flag is guarded
 * by [nativeAdLock].
 */
@Singleton
class AdsCoordinator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val rewardedAds = mutableMapOf<RewardedPlacement, RewardedAd?>()
    private val isRewardedAdLoading = mutableMapOf<RewardedPlacement, Boolean>()

    private val interstitialAds = mutableMapOf<InterstitialPlacement, InterstitialAd?>()
    private val isInterstitialAdLoading = mutableMapOf<InterstitialPlacement, Boolean>()
    
    private val lastInterstitialTime = mutableMapOf<InterstitialPlacement, Long>()
    private val INTERSTITIAL_COOLDOWN_MILLIS = 15 * 60 * 1000L // 15 minutes

    // --- Native ad state (Phase 1) ---
    private val nativeAds = ConcurrentHashMap<AdPlacement, NativeAd>()
    private val isNativeAdLoading = ConcurrentHashMap<AdPlacement, Boolean>()
    /** Number of screens currently displaying a placement (retain/release use-count). */
    private val nativeAdUseCounts = ConcurrentHashMap<AdPlacement, Int>()
    /** Callers waiting for a load to complete (all of them are notified on arrival). */
    private val nativeAdCallbacks = ConcurrentHashMap<AdPlacement, CopyOnWriteArrayList<(NativeAd) -> Unit>>()
    private val nativeAdRefreshJobs = ConcurrentHashMap<AdPlacement, Job>()
    private val nativeAdLock = Any()

    /** AdMob native ads expire after ~1 hour; refresh the cache well before that. */
    private val NATIVE_AD_REFRESH_MILLIS = 55 * 60 * 1000L

    /** All placements preloaded once the SDK initializes (finding #3). HOME_BANNER shares
     *  HOME_DASHBOARD's unit ID and is unused by any screen, so it is not preloaded. */
    private val PRELOAD_NATIVE_PLACEMENTS = listOf(
        AdPlacement.HOME_DASHBOARD,
        AdPlacement.TRANSACTIONS_LIST,
        AdPlacement.TRANSACTIONS_LIST_2,
        AdPlacement.ANALYTICS_INSIGHTS,
        AdPlacement.BUDGET_CALENDAR,
        AdPlacement.SETTINGS_GENERAL
    )

    /** Process-lifetime scope for ad refreshes / background AdLoader construction. */
    private val adScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val isMobileAdsSdkInitialized = AtomicBoolean(false)
    private lateinit var consentInformation: ConsentInformation

    /**
     * Whether the UMP privacy options form must be surfaced to the user (GDPR / US state
     * privacy regulations, e.g. CCPA/CPRA). Set after the consent info update completes.
     * The Settings screen shows the "Privacy Options" entry point only when this is
     * [PrivacyOptionsRequirementStatus.REQUIRED].
     */
    private val _privacyOptionsRequirementStatus =
        MutableStateFlow(PrivacyOptionsRequirementStatus.UNKNOWN)
    val privacyOptionsRequirementStatus: StateFlow<PrivacyOptionsRequirementStatus> =
        _privacyOptionsRequirementStatus.asStateFlow()

    // Google Test Ad IDs
    private val REWARDED_TEST_ID = "ca-app-pub-3940256099942544/5224354917"
    private val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
    private val NATIVE_TEST_ID = "ca-app-pub-3940256099942544/2247696110"

    // Real Native Ad Unit IDs
    private val NATIVE_HOME_ID = "ca-app-pub-3052232912913226/1140149865"
    private val NATIVE_TRANSACTIONS_ID = "ca-app-pub-3052232912913226/4486018419"
    private val NATIVE_TRANSACTIONS_2_ID = "ca-app-pub-3052232912913226/5599898304"
    private val NATIVE_BUDGET_CALENDAR_ID = "ca-app-pub-3052232912913226/6920610063"
    private val NATIVE_SETTINGS_ID = "ca-app-pub-3052232912913226/2261659841"
    private val NATIVE_ANALYTICS_ID = "ca-app-pub-3052232912913226/9786446554"

    // Real Interstitial Ad Unit IDs
    private val INTERSTITIAL_DATA_ACTION_ID = "ca-app-pub-3052232912913226/9797369089"

    // Real Rewarded Ad Unit IDs
    private val REWARDED_AD_FREE_ID = "ca-app-pub-3052232912913226/5216950076"

    /**
     * Maps placements to their respective AdMob Unit IDs.
     */
    private fun getNativeAdUnitId(placement: AdPlacement): String {
        // ALWAYS use test IDs in Debug builds to avoid account suspension
        if (com.mknlabs.expensetracker.BuildConfig.DEBUG) return NATIVE_TEST_ID

        return when (placement) {
            AdPlacement.HOME_DASHBOARD -> NATIVE_HOME_ID
            AdPlacement.HOME_BANNER -> NATIVE_HOME_ID // Using Home ID for test
            AdPlacement.TRANSACTIONS_LIST -> NATIVE_TRANSACTIONS_ID
            AdPlacement.TRANSACTIONS_LIST_2 -> NATIVE_TRANSACTIONS_2_ID
            AdPlacement.ANALYTICS_INSIGHTS -> NATIVE_ANALYTICS_ID
            AdPlacement.BUDGET_CALENDAR -> NATIVE_BUDGET_CALENDAR_ID
            AdPlacement.SETTINGS_GENERAL -> NATIVE_SETTINGS_ID
        }
    }

    private fun getRewardedAdUnitId(placement: RewardedPlacement): String {
        if (com.mknlabs.expensetracker.BuildConfig.DEBUG) return REWARDED_TEST_ID

        return when (placement) {
            RewardedPlacement.FEATURE_UNLOCK -> REWARDED_AD_FREE_ID
            RewardedPlacement.AD_FREE_ACCESS -> REWARDED_AD_FREE_ID
        }
    }

    private fun getInterstitialAdUnitId(placement: InterstitialPlacement): String {
        if (com.mknlabs.expensetracker.BuildConfig.DEBUG) return INTERSTITIAL_TEST_ID

        return when (placement) {
            InterstitialPlacement.DATA_ACTION -> INTERSTITIAL_DATA_ACTION_ID
        }
    }

    /**
     * Initializes the privacy consent flow (UMP).
     * Should be called from the Activity.
     *
     * Consent handling: `requestConsentInfoUpdate` can fail (e.g. the AdMob account's
     * Privacy & messaging configuration is missing, or a transient network error). A
     * failed/unknown consent state must NOT block ad serving entirely, so the SDK is
     * always initialized regardless and ad requests still reach Google. Trade-off: in
     * the EU/EEA, requests made while consent is unknown carry no consent signals until
     * the console's UMP form is configured (then the form gates personalized ads).
     */
    fun initPrivacyFlow(activity: Activity, onComplete: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Consent info is now available: read whether the privacy options form is
                // required so the Settings entry point can toggle its visibility.
                refreshPrivacyOptionsStatus()

                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w("AdsCoordinator", "Consent form error: code=${formError.errorCode}, message=${formError.message}")
                    }
                    // Form shown/dismissed (or failed to load) — initialize regardless.
                    initializeMobileAdsSdk(onComplete)
                }
            },
            { requestConsentError ->
                Log.w("AdsCoordinator", "Consent info update failed: code=${requestConsentError.errorCode}, message=${requestConsentError.message}")
                // Consent state unknown — still initialize the SDK so ad requests can
                // proceed; EEA consent enforcement is handled server-side by the SDK.
                // The privacy-options requirement can't be determined, so leave it UNKNOWN
                // (the Settings entry point stays hidden).
                initializeMobileAdsSdk(onComplete)
            }
        )

        // Fast path: consent already known (e.g. stored from a previous session).
        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk(onComplete)
        }
    }

    /**
     * Reads the current UMP privacy-options requirement status and publishes it so the
     * Settings screen can conditionally show the "Privacy Options" entry point. Runs on
     * the main thread (called from [initPrivacyFlow]); [MutableStateFlow] is thread-safe.
     */
    private fun refreshPrivacyOptionsStatus() {
        val status = try {
            consentInformation.getPrivacyOptionsRequirementStatus()
        } catch (e: Exception) {
            Log.w("AdsCoordinator", "Failed to read privacy options requirement status: ${e.message}")
            PrivacyOptionsRequirementStatus.UNKNOWN
        }
        _privacyOptionsRequirementStatus.value = status
        Log.d("AdsCoordinator", "Privacy options requirement status: $status")
    }

    /**
     * Re-opens the UMP privacy options form (required under GDPR / US state privacy
     * regulations, e.g. CCPA/CPRA) so the user can change their consent choices at any
     * time. Must be called on the main thread with an Activity in the RESUMED state.
     *
     * The caller should only surface the entry point when [privacyOptionsRequirementStatus]
     * is [PrivacyOptionsRequirementStatus.REQUIRED]; this method is safe to call regardless
     * (the form simply won't be available when it isn't required).
     */
    /**
     * Test seam: publishes a privacy-options requirement status without going through the
     * UMP SDK so ViewModel tests can drive the Settings entry-point visibility. Unit tests
     * only — production code paths publish via [refreshPrivacyOptionsStatus].
     */
    @VisibleForTesting
    internal fun publishPrivacyOptionsRequirementStatusForTesting(status: PrivacyOptionsRequirementStatus) {
        _privacyOptionsRequirementStatus.value = status
    }

    fun showPrivacyOptionsForm(activity: Activity, onDismiss: () -> Unit = {}) {
        try {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) {
                Log.d("AdsCoordinator", "Privacy options form dismissed.")
                onDismiss()
            }
        } catch (e: Exception) {
            Log.e("AdsCoordinator", "Failed to show privacy options form: ${e.message}")
        }
    }

    private fun initializeMobileAdsSdk(onComplete: () -> Unit) {
        // Atomic gate: exactly one caller proceeds to MobileAds.initialize(). Note that
        // onComplete may fire more than once (here, the trailing canRequestAds() fast
        // path, and the init callback) — the current caller's lambda is a no-op, and
        // preloading only happens inside the init callback, so this is safe.
        if (isMobileAdsSdkInitialized.getAndSet(true)) {
            onComplete()
            return
        }

        Log.d("AdsCoordinator", "Initializing Google Mobile Ads SDK...")
        MobileAds.initialize(context) {
            Log.d("AdsCoordinator", "Google Mobile Ads SDK initialized.")
            // Phase 1: preload the rewarded ad plus every native placement up front, so
            // screen composition never triggers a first-visit network ad request.
            loadRewardedAd(RewardedPlacement.FEATURE_UNLOCK)
            PRELOAD_NATIVE_PLACEMENTS.forEach { loadNativeAd(it) }
            onComplete()
        }
    }

    /**
     * Loads an Interstitial Ad for a specific placement.
     */
    fun loadInterstitialAd(placement: InterstitialPlacement, onAdLoaded: (() -> Unit)? = null) {
        if (interstitialAds[placement] != null || isInterstitialAdLoading[placement] == true) return
        if (!isMobileAdsSdkInitialized.get()) {
            Log.w("AdsCoordinator", "Interstitial ad ($placement) load skipped: Mobile Ads SDK not initialized yet.")
            return
        }

        isInterstitialAdLoading[placement] = true
        val adUnitId = getInterstitialAdUnitId(placement)
        // Plain request: consent decisions are owned by [initPrivacyFlow] (which keeps the
        // SDK initialized so requests always reach Google; UMP consent signals ride along
        // once the console form is configured).
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("AdsCoordinator", "Interstitial ad ($placement) failed to load: code=${adError.code}, domain=${adError.domain}, message=${adError.message}")
                interstitialAds[placement] = null
                isInterstitialAdLoading[placement] = false
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d("AdsCoordinator", "Interstitial ad ($placement) was loaded.")
                interstitialAds[placement] = ad
                isInterstitialAdLoading[placement] = false
                onAdLoaded?.invoke()
            }
        })
    }

    /**
     * Checks if an interstitial ad can be shown based on frequency capping (15 min cooldown).
     */
    fun canShowInterstitial(placement: InterstitialPlacement): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastTime = lastInterstitialTime[placement] ?: 0
        val timeSinceLastAd = currentTime - lastTime
        val isCooldownOver = timeSinceLastAd >= INTERSTITIAL_COOLDOWN_MILLIS
        
        return interstitialAds[placement] != null && isCooldownOver
    }

    /**
     * Shows the Interstitial Ad if ready and cooldown is over.
     */
    fun showInterstitial(activity: Activity, placement: InterstitialPlacement, onAdDismissed: () -> Unit = {}) {
        if (canShowInterstitial(placement)) {
            interstitialAds[placement]?.let { ad ->
                ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("AdsCoordinator", "Interstitial ad ($placement) was dismissed.")
                        interstitialAds[placement] = null
                        lastInterstitialTime[placement] = System.currentTimeMillis()
                        loadInterstitialAd(placement) // Preload next
                        onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        Log.e("AdsCoordinator", "Interstitial ad ($placement) failed to show: code=${adError.code}, domain=${adError.domain}, message=${adError.message}")
                        interstitialAds[placement] = null
                        onAdDismissed()
                    }
                }
                ad.show(activity)
            }
        } else {
            Log.d("AdsCoordinator", "Interstitial ad ($placement) not ready or cooldown in progress.")
            if (interstitialAds[placement] == null) loadInterstitialAd(placement)
            onAdDismissed()
        }
    }

    fun loadRewardedAd(placement: RewardedPlacement, onAdLoaded: (() -> Unit)? = null) {
        if (rewardedAds[placement] != null || isRewardedAdLoading[placement] == true) return
        if (!isMobileAdsSdkInitialized.get()) {
            Log.w("AdsCoordinator", "Rewarded ad ($placement) load skipped: Mobile Ads SDK not initialized yet.")
            return
        }

        isRewardedAdLoading[placement] = true
        val adUnitId = getRewardedAdUnitId(placement)
        // See note in [loadInterstitialAd]: plain request; EEA consent is handled by the SDK.
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("AdsCoordinator", "Rewarded ad ($placement) failed to load: code=${adError.code}, domain=${adError.domain}, message=${adError.message}")
                rewardedAds[placement] = null
                isRewardedAdLoading[placement] = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("AdsCoordinator", "Rewarded ad ($placement) was loaded.")
                rewardedAds[placement] = ad
                isRewardedAdLoading[placement] = false
                onAdLoaded?.invoke()
            }
        })
    }

    fun showRewardedAd(activity: Activity, placement: RewardedPlacement, onUserEarnedReward: () -> Unit) {
        rewardedAds[placement]?.let { ad ->
            ad.show(activity) { rewardItem ->
                // User earned the reward!
                onUserEarnedReward()
                rewardedAds[placement] = null
                loadRewardedAd(placement) // Preload the next one
            }
        } ?: run {
            Log.d("AdsCoordinator", "The rewarded ad ($placement) wasn't ready yet.")
            loadRewardedAd(placement)
        }
    }
    
    fun isRewardedAdReady(placement: RewardedPlacement): Boolean = rewardedAds[placement] != null

    /**
     * Loads (or returns from cache) a Native Ad for a specific placement.
     *
     * Phase 1 lifecycle:
     * - Every caller's callback is registered and fired when the ad arrives, so multiple
     *   holders of the same placement all resolve (fixes the Analytics double-card).
     * - Cache hits are delivered synchronously.
     * - The [AdLoader] is constructed on [Dispatchers.IO] per AdMob guidance.
     * - Failed loads are logged once and never retried from the failure callback.
     */
    fun loadNativeAd(placement: AdPlacement, onAdLoaded: ((NativeAd) -> Unit)? = null) {
        onAdLoaded?.let {
            nativeAdCallbacks.getOrPut(placement) { CopyOnWriteArrayList() }.add(it)
        }
        // Cache hit: deliver synchronously (e.g. card composed after the load completed).
        nativeAds[placement]?.let { ad ->
            deliverNativeAdCallbacks(placement, ad)
            return
        }
        if (!isMobileAdsSdkInitialized.get()) {
            Log.w("AdsCoordinator", "Native ad ($placement) load skipped: Mobile Ads SDK not initialized yet.")
            return
        }

        synchronized(nativeAdLock) {
            if (isNativeAdLoading[placement] == true) return
            isNativeAdLoading[placement] = true
        }

        adScope.launch {
            val adUnitId = getNativeAdUnitId(placement)
            val adLoader = withContext(Dispatchers.IO) {
                AdLoader.Builder(context, adUnitId)
                    .forNativeAd { ad -> onNativeAdLoaded(placement, ad) }
                    .withAdListener(object : com.google.android.gms.ads.AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e("AdsCoordinator", "Native ad ($placement) failed to load: code=${adError.code}, domain=${adError.domain}, message=${adError.message}")
                            synchronized(nativeAdLock) {
                                isNativeAdLoading[placement] = false
                            }
                            nativeAdCallbacks.remove(placement)
                        }
                    })
                    .withNativeAdOptions(NativeAdOptions.Builder().build())
                    .build()
            }
            // AdLoader.Builder ran off-main per AdMob guidance; keep loadAd() itself on the
            // main thread (matches the pre-Phase-1 behavior; the SDK posts callbacks to main).
            // Plain request — EEA consent enforcement is handled server-side by the SDK.
            withContext(Dispatchers.Main) {
                adLoader.loadAd(AdRequest.Builder().build())
            }
        }
    }

    /** GMA callback (main thread): cache the ad, schedule refresh, notify all waiters. */
    private fun onNativeAdLoaded(placement: AdPlacement, ad: NativeAd) {
        synchronized(nativeAdLock) {
            nativeAds[placement]?.takeIf { it !== ad }?.destroy()
            nativeAds[placement] = ad
            isNativeAdLoading[placement] = false
        }
        scheduleNativeAdRefresh(placement)
        deliverNativeAdCallbacks(placement, ad)
    }

    private fun deliverNativeAdCallbacks(placement: AdPlacement, ad: NativeAd) {
        nativeAdCallbacks.remove(placement)?.forEach { callback ->
            try {
                callback(ad)
            } catch (e: Exception) {
                Log.w("AdsCoordinator", "Native ad callback failed for $placement: $e")
            }
        }
    }

    /**
     * Marks a screen as currently displaying this placement. While the use-count is
     * positive the coordinator keeps the ad alive even if other holders release it.
     * Call from a [androidx.compose.runtime.DisposableEffect] on entry.
     */
    fun retainNativeAd(placement: AdPlacement) {
        synchronized(nativeAdLock) {
            nativeAdUseCounts[placement] = (nativeAdUseCounts[placement] ?: 0) + 1
        }
    }

    /**
     * Marks a holder as done with this placement.
     *
     * Phase 2 semantics: the cached [NativeAd] is **not** destroyed when the last holder
     * releases — it is the "cached next ad" and stays warm so the next card composition
     * (e.g. an ad item scrolling back into a LazyColumn viewport) renders instantly with
     * zero network cost. Phase 1 measured that destroy-on-scroll-out re-triggered a load
     * mid-scroll and regressed `scrollTransactions_free` P90 17.1 → 31.4 ms. Memory is
     * still bounded: the hourly refresh sweep ([scheduleNativeAdRefresh]) destroys
     * stale/unused cached ads.
     *
     * The count update is atomic under [nativeAdLock], so a concurrent retain can never
     * race a release (the coordinator's cache is the source of truth; screens are holders).
     */
    fun releaseNativeAd(placement: AdPlacement) {
        synchronized(nativeAdLock) {
            val remaining = (nativeAdUseCounts[placement] ?: 1) - 1
            if (remaining <= 0) {
                nativeAdUseCounts.remove(placement)
            } else {
                nativeAdUseCounts[placement] = remaining
            }
        }
    }

    /**
     * Destroys the cached ad for a placement and cancels its pending refresh. Safe to
     * call from any thread. Used by the hourly-refresh sweep and any explicit teardown
     * (e.g. the Pro transition); release keeps the ad cached as the "next ad" (Phase 2).
     */
    fun destroyNativeAd(placement: AdPlacement) {
        nativeAdRefreshJobs.remove(placement)?.cancel()
        synchronized(nativeAdLock) {
            nativeAds.remove(placement)?.destroy()
            isNativeAdLoading[placement] = false
        }
    }

    /**
     * Refreshes the cache before AdMob's 1-hour expiry.
     *
     * - If the cached ad is unused (use-count == 0) it is destroyed to bound memory
     *   (finding #6), instead of being re-loaded.
     * - If the ad is still visible (use-count > 0) the cycle is **re-armed** so it is
     *   checked again later — a visible ad must not be destroyed mid-view (Phase 1
     *   measured destroy-on-scroll-out regressing scrolls). Since Phase 2's
     *   [releaseNativeAd] never destroys, this re-arm is what eventually destroys an
     *   ad once the last holder leaves, so no shown ad lives for the whole session.
     */
    private fun scheduleNativeAdRefresh(placement: AdPlacement) {
        nativeAdRefreshJobs.remove(placement)?.cancel()
        nativeAdRefreshJobs[placement] = adScope.launch {
            delay(NATIVE_AD_REFRESH_MILLIS)
            // Check + destroy are atomic under nativeAdLock, so a retain that lands
            // concurrently is never clobbered.
            val destroyNow = synchronized(nativeAdLock) {
                if (nativeAds[placement] == null) {
                    false
                } else {
                    val inUse = (nativeAdUseCounts[placement] ?: 0) > 0
                    if (inUse) {
                        false
                    } else {
                        Log.d("AdsCoordinator", "Native ad ($placement) expired unused; destroying cache entry.")
                        nativeAds.remove(placement)?.destroy()
                        isNativeAdLoading[placement] = false
                        true
                    }
                }
            }
            if (destroyNow) {
                nativeAdRefreshJobs.remove(placement)
            } else if (nativeAds.containsKey(placement)) {
                // Still cached but in use (or race): re-arm so the next check can destroy it.
                scheduleNativeAdRefresh(placement)
            }
        }
    }

    fun getNativeAd(placement: AdPlacement): NativeAd? = nativeAds[placement]
}
