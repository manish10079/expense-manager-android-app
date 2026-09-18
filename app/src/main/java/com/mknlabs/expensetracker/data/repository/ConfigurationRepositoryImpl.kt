package com.mknlabs.expensetracker.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.repository.ConfigurationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurationRepositoryImpl @Inject constructor() : ConfigurationRepository {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    private val _minRequiredVersion = MutableStateFlow(0)
    override val minRequiredVersion: StateFlow<Int> = _minRequiredVersion.asStateFlow()

    private val _isUnderMaintenance = MutableStateFlow(false)
    override val isUnderMaintenance: StateFlow<Boolean> = _isUnderMaintenance.asStateFlow()

    private val _currentPromoCode = MutableStateFlow("")
    override val currentPromoCode: StateFlow<String> = _currentPromoCode.asStateFlow()

    private val _isProPassEnabled = MutableStateFlow(true)
    override val isProPassEnabled: StateFlow<Boolean> = _isProPassEnabled.asStateFlow()

    private val _isSyncEnabled = MutableStateFlow(false)

    override val isSyncEnabled: StateFlow<Boolean> = _isSyncEnabled.asStateFlow()

    private val _maxSyncDevices = MutableStateFlow(4)
    override val maxSyncDevices: StateFlow<Int> = _maxSyncDevices.asStateFlow()

    private val _googleSheetsFeedbackUrl = MutableStateFlow("")
    override val googleSheetsFeedbackUrl: StateFlow<String> = _googleSheetsFeedbackUrl.asStateFlow()

    private val _isProGatingEnabled = MutableStateFlow(true)
    override val isProGatingEnabled: StateFlow<Boolean> = _isProGatingEnabled.asStateFlow()

    private val _adPassDurationMinutes = MutableStateFlow(DEFAULT_AD_PASS_DURATION_MINUTES)
    override val adPassDurationMinutes: StateFlow<Int> = _adPassDurationMinutes.asStateFlow()

    /** Process-lifetime scope for periodic Remote Config refreshes. */
    private val configScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Interval between periodic Remote Config fetches (15 minutes). */
    private val PERIODIC_REFRESH_MILLIS = 15 * 60 * 1000L

    /** 0 = every fetch hits the network. Mirrored in [UpdateRepositoryImpl]. */
    private val MIN_FETCH_INTERVAL_SECONDS = 0L

    init {
        // `setConfigSettingsAsync` / `setDefaultsAsync` apply asynchronously, so fetching
        // straight after them races both — the request can go out under the SDK's default
        // 12-hour interval, come back throttled, and still report success while serving the
        // cached template. Chain them so both are in effect before the first fetch.
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = MIN_FETCH_INTERVAL_SECONDS
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
            // All in-app defaults live in one place: res/xml/remote_config_defaults.xml.
            // UpdateRepositoryImpl loads the same file, so defaults are consistent
            // regardless of which singleton initializes first.
            .continueWithTask { remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults) }
            .addOnCompleteListener {
                // Publish the in-app defaults before the network answers, so flags like
                // `pro_gating_enabled` have a real value instead of a hardcoded guess.
                updateState()
                fetchAndActivate()
            }
        listenForRealtimeUpdates()
        startPeriodicRefresh()
    }

    private fun listenForRealtimeUpdates() {
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Log.d("ConfigRepo", "Real-time Remote Config updated keys: ${configUpdate.updatedKeys}")
                remoteConfig.activate().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateState()
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.w("ConfigRepo", "Real-time Remote Config listener error", error)
            }
        })
    }

    /**
     * Periodically re-fetches Remote Config every [PERIODIC_REFRESH_MILLIS] so flags like
     * `pro_gating_enabled` propagate within a session without requiring an app restart.
     */
    private fun startPeriodicRefresh() {
        configScope.launch {
            while (true) {
                delay(PERIODIC_REFRESH_MILLIS)
                Log.d("ConfigRepo", "Periodic Remote Config refresh")
                fetchAndActivate()
            }
        }
    }

    override fun fetchAndActivate() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("ConfigRepo", "Remote Config fetch failed", task.exception)
                    return@addOnCompleteListener
                }
                // Throttled fetches complete successfully while serving the cached template,
                // so distinguish them from a real refresh instead of always claiming success.
                if (remoteConfig.info.lastFetchStatus ==
                    FirebaseRemoteConfig.LAST_FETCH_STATUS_THROTTLED
                ) {
                    Log.w("ConfigRepo", "Remote Config fetch throttled — serving cached values")
                } else {
                    Log.d("ConfigRepo", "Remote Config updated successfully")
                }
                updateState()
            }
    }

    private fun updateState() {
        _minRequiredVersion.value = remoteConfig.getLong("min_required_version").toInt()
        _isUnderMaintenance.value = remoteConfig.getBoolean("is_under_maintenance")
        _currentPromoCode.value = remoteConfig.getString("current_promo_code")
        _isProPassEnabled.value = remoteConfig.getBoolean("is_pro_pass_enabled")
        _isSyncEnabled.value = remoteConfig.getBoolean("is_sync_enabled")
        _maxSyncDevices.value = remoteConfig.getLong("max_sync_devices").toInt()
        _googleSheetsFeedbackUrl.value = remoteConfig.getString("google_sheets_feedback_url")
        _isProGatingEnabled.value = remoteConfig.getBoolean("pro_gating_enabled")
        // Guard the lower bound: an expiry in the past would mean the user watches an ad and
        // gets no pass at all. A missing/blank server value falls back to the in-app default.
        _adPassDurationMinutes.value = remoteConfig.getLong("ad_pass_duration_minutes")
            .toInt()
            .coerceAtLeast(1)
    }

    override fun isUpdateRequired(): Boolean {
        return BuildConfig.VERSION_CODE < minRequiredVersion.value
    }

    companion object {
        /**
         * Rewarded-ad pass length used until Remote Config answers (or when it can't be
         * reached). Mirrors the `ad_pass_duration_minutes` entry in
         * `res/xml/remote_config_defaults.xml`.
         */
        const val DEFAULT_AD_PASS_DURATION_MINUTES = 60
    }
}
