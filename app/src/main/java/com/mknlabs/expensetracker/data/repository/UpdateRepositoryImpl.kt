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
 * Reads force-update copy from Firebase Remote Config.
 *
 * Play In-App Updates decides whether a store build exists. This repository
 * only supplies [UpdateInfo.forceUpdate], title, and message.
 */
@Singleton
class UpdateRepositoryImpl @Inject constructor() : UpdateRepository {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    private val _updateInfo = MutableStateFlow(UpdateInfo())
    override val updateInfo: StateFlow<UpdateInfo> = _updateInfo.asStateFlow()

    init {
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
                if (remoteConfig.info.lastFetchStatus ==
                    FirebaseRemoteConfig.LAST_FETCH_STATUS_THROTTLED
                ) {
                    Log.w(TAG, "Remote Config fetch throttled — serving cached update params")
                }
                refreshFromCache()
            }
    }

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
            forceUpdate = remoteConfig.getBoolean(KEY_FORCE_UPDATE),
            updateTitle = remoteConfig.getString(KEY_UPDATE_TITLE),
            updateMessage = remoteConfig.getString(KEY_UPDATE_MESSAGE)
        )
        if (info != _updateInfo.value) {
            Log.d(
                TAG,
                "Update params: force=${info.forceUpdate}, message=${info.updateMessage.length} chars"
            )
        }
        _updateInfo.value = info
    }

    private companion object {
        const val TAG = "UpdateRepository"

        const val MIN_FETCH_INTERVAL_SECONDS = 0L

        const val KEY_FORCE_UPDATE = "force_update"
        const val KEY_UPDATE_TITLE = "update_title"
        const val KEY_UPDATE_MESSAGE = "update_message"
    }
}
