package com.mknlabs.expensetracker.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
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
 * [ConfigurationRepositoryImpl], so this repository never relies on that one having
 * initialized first: it applies the fetch interval itself (idempotently) and only then
 * issues a fetch.
 */
@Singleton
class UpdateRepositoryImpl @Inject constructor() : UpdateRepository {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    private val _updateInfo = MutableStateFlow(UpdateInfo())
    override val updateInfo: StateFlow<UpdateInfo> = _updateInfo.asStateFlow()

    init {
        // Order matters. `setConfigSettingsAsync` and `setDefaultsAsync` are applied
        // asynchronously, so fetching straight after them races both: the fetch can be issued
        // under the SDK's default minimum interval (12 hours), come back *throttled*, and
        // still report success while serving the previously cached template. The symptom is
        // an update dialog with a fresh version but a stale `update_message`. Chain them so
        // settings and defaults are in effect before any request goes out.
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings { minimumFetchIntervalInSeconds = MIN_FETCH_INTERVAL_SECONDS }
        )
            .continueWithTask { remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults) }
            .addOnCompleteListener {
                refreshFromCache()
                fetchAndActivate()
            }
        listenForActivatedUpdates()
    }

    override fun fetchAndActivate() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e(TAG, "Remote Config fetch failed for update params", task.exception)
                    return@addOnCompleteListener
                }
                // A throttled fetch completes *successfully* but serves the cached template, so
                // "success" is not proof the values changed. Report it rather than logging a
                // lie that hides a stale dialog.
                if (remoteConfig.info.lastFetchStatus ==
                    FirebaseRemoteConfig.LAST_FETCH_STATUS_THROTTLED
                ) {
                    Log.w(
                        TAG,
                        "Remote Config fetch throttled — serving cached update params " +
                            "(latest_version=${remoteConfig.getString(KEY_LATEST_VERSION)})"
                    )
                }
                refreshFromCache()
            }
    }

    /**
     * Re-reads the update parameters whenever Remote Config activates a new template from any
     * other trigger (the realtime listener / 15-minute periodic refresh in
     * [ConfigurationRepositoryImpl]), so an open dialog never outlives the values it shows.
     */
    private fun listenForActivatedUpdates() {
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Log.d(TAG, "Remote Config update for update params: ${configUpdate.updatedKeys}")
                remoteConfig.activate().addOnCompleteListener { task ->
                    if (task.isSuccessful) refreshFromCache()
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.w(TAG, "Realtime Remote Config listener error", error)
            }
        })
    }

    private fun refreshFromCache() {
        val info = UpdateInfo(
            latestVersionCode = remoteConfig.getLong(KEY_LATEST_VERSION_CODE).toInt(),
            latestVersion = remoteConfig.getString(KEY_LATEST_VERSION),
            forceUpdate = remoteConfig.getBoolean(KEY_FORCE_UPDATE),
            updateTitle = remoteConfig.getString(KEY_UPDATE_TITLE),
            updateMessage = remoteConfig.getString(KEY_UPDATE_MESSAGE)
        )
        if (info != _updateInfo.value) {
            Log.d(
                TAG,
                "Update params: latest_version=${info.latestVersion} " +
                    "(code ${info.latestVersionCode}), force=${info.forceUpdate}, " +
                    "message=${info.updateMessage.length} chars"
            )
        }
        _updateInfo.value = info
    }

    private companion object {
        const val TAG = "UpdateRepository"

        /** Mirrors the interval applied in [ConfigurationRepositoryImpl]; 0 = always refetch. */
        const val MIN_FETCH_INTERVAL_SECONDS = 0L

        const val KEY_LATEST_VERSION = "latest_version"
        const val KEY_LATEST_VERSION_CODE = "latest_version_code"
        const val KEY_FORCE_UPDATE = "force_update"
        const val KEY_UPDATE_TITLE = "update_title"
        const val KEY_UPDATE_MESSAGE = "update_message"
    }
}