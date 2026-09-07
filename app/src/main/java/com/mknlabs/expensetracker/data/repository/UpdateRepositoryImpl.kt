package com.mknlabs.expensetracker.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.models.UpdateInfo
import com.mknlabs.expensetracker.domain.repository.UpdateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the "latest version" parameters from Firebase Remote Config.
 *
 * Shares the single [FirebaseRemoteConfig] instance with
 * [ConfigurationRepositoryImpl]. Fetch throttling is configured there
 * (0s in debug, 1h in release), so this repository never overrides the
 * fetch interval; it only fetches and re-emits the update parameters.
 */
@Singleton
class UpdateRepositoryImpl @Inject constructor() : UpdateRepository {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    private val _updateInfo = MutableStateFlow(UpdateInfo())
    override val updateInfo: StateFlow<UpdateInfo> = _updateInfo.asStateFlow()

    init {
        // Idempotent: same defaults map as ConfigurationRepositoryImpl, so the
        // update params always have sane values even if this singleton is
        // created before the configuration one.
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        refreshFromCache()
        fetchAndActivate()
    }

    override fun fetchAndActivate() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    refreshFromCache()
                } else {
                    Log.e(TAG, "Remote Config fetch failed for update params")
                }
            }
    }

    private fun refreshFromCache() {
        _updateInfo.value = UpdateInfo(
            latestVersionCode = remoteConfig.getLong(KEY_LATEST_VERSION_CODE).toInt(),
            latestVersion = remoteConfig.getString(KEY_LATEST_VERSION),
            forceUpdate = remoteConfig.getBoolean(KEY_FORCE_UPDATE),
            updateTitle = remoteConfig.getString(KEY_UPDATE_TITLE),
            updateMessage = remoteConfig.getString(KEY_UPDATE_MESSAGE)
        )
    }

    private companion object {
        const val TAG = "UpdateRepository"
        const val KEY_LATEST_VERSION = "latest_version"
        const val KEY_LATEST_VERSION_CODE = "latest_version_code"
        const val KEY_FORCE_UPDATE = "force_update"
        const val KEY_UPDATE_TITLE = "update_title"
        const val KEY_UPDATE_MESSAGE = "update_message"
    }
}