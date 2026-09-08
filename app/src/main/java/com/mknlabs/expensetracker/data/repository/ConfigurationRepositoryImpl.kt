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

    /** Process-lifetime scope for periodic Remote Config refreshes. */
    private val configScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Interval between periodic Remote Config fetches (15 minutes). */
    private val PERIODIC_REFRESH_MILLIS = 15 * 60 * 1000L

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        // All in-app defaults live in one place: res/xml/remote_config_defaults.xml.
        // UpdateRepositoryImpl loads the same file, so defaults are consistent
        // regardless of which singleton initializes first.
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        fetchAndActivate()
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
                if (task.isSuccessful) {
                    Log.d("ConfigRepo", "Remote Config updated successfully")
                    updateState()
                } else {
                    Log.e("ConfigRepo", "Remote Config fetch failed")
                }
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
    }

    override fun isUpdateRequired(): Boolean {
        return BuildConfig.VERSION_CODE < minRequiredVersion.value
    }
}
