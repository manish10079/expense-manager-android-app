package com.mknlabs.expensetracker.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.mknlabs.expensetracker.domain.update.PlayAppUpdateGateway
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [AppUpdateManager] wrapper. Play is the only source of "is there a store
 * update"; copy and force still come from Remote Config.
 */
@Singleton
class PlayAppUpdateGatewayImpl @Inject constructor(
    @ApplicationContext context: Context
) : PlayAppUpdateGateway {

    private val manager = AppUpdateManagerFactory.create(context)

    init {
        manager.registerListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                manager.completeUpdate()
            }
        }
    }

    override suspend fun isUpdateAvailable(): Boolean {
        return try {
            val info = manager.appUpdateInfo.await()
            val availability = info.updateAvailability()
            availability == UpdateAvailability.UPDATE_AVAILABLE ||
                availability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ||
                info.installStatus() == InstallStatus.DOWNLOADED
        } catch (e: Exception) {
            Log.w(TAG, "Play in-app update info unavailable", e)
            false
        }
    }

    override fun startUpdate(
        activity: Activity,
        immediate: Boolean,
        onFlowUnavailable: () -> Unit
    ) {
        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                val preferred =
                    if (immediate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
                val type = when {
                    info.isUpdateTypeAllowed(preferred) -> preferred
                    info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
                    info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
                    else -> null
                }
                if (type == null) {
                    onFlowUnavailable()
                    return@addOnSuccessListener
                }
                manager.startUpdateFlow(
                    info,
                    activity,
                    AppUpdateOptions.newBuilder(type).build()
                ).addOnFailureListener { e ->
                    Log.w(TAG, "Play in-app update flow failed", e)
                    onFlowUnavailable()
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Play in-app update info failed before start", e)
                onFlowUnavailable()
            }
    }

    override fun resumeInProgressUpdate(activity: Activity) {
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() !=
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                return@addOnSuccessListener
            }
            if (!info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) return@addOnSuccessListener
            manager.startUpdateFlow(
                info,
                activity,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        }
    }

    override fun completeDownloadedUpdate() {
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                manager.completeUpdate()
            }
        }
    }

    private companion object {
        const val TAG = "PlayAppUpdate"
    }
}
