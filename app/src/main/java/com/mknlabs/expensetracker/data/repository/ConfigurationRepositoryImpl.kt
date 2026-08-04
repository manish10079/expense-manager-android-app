package com.mknlabs.expensetracker.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.domain.repository.ConfigurationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(
            mapOf(
                "min_required_version" to 1,
                "is_under_maintenance" to false,
                "current_promo_code" to "",
                "is_pro_pass_enabled" to true,
                "is_sync_enabled" to true,
                "max_sync_devices" to 4,
                "google_sheets_feedback_url" to ""
            )
        )
        fetchAndActivate()
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
    }

    override fun isUpdateRequired(): Boolean {
        return BuildConfig.VERSION_CODE < minRequiredVersion.value
    }
}
